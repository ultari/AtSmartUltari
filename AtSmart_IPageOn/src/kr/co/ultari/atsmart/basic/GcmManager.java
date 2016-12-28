package kr.co.ultari.atsmart.basic;

import java.io.IOException;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import kr.co.ultari.atsmart.basic.dbemulator.Database;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmManager {
        public static final String EXTRA_MESSAGE = "message";
        public static final String PROPERTY_REG_ID = "registration_id";
        private static final String PROPERTY_APP_VERSION = "appVersion";
        private String SENDER_ID = "587703202222";
        GoogleCloudMessaging gcm;
        private String regid;
        Context context;

        /**
         * @param mContext getApplicationContext
         */
        public GcmManager( Context mContext )
        {
                context = mContext;
                if ( checkPlayServices() )
                {
                        gcm = GoogleCloudMessaging.getInstance( context );
                        regid = getRegistrationId( context );
                        if ( regid == null || regid.equals( "" ) ) registerInBackground();
                        else
                        {
                        	TRACE( "stored regID : " + regid );
                        	if ( regid != null && !regid.equals("") )
                            	Database.instance(context).updateConfig("GCMID", regid);
                        }
                }
                else TRACE( "No valid Google Play Service APK found" );
        }

        public String getPhoneRegistrationId()
        {
                if ( regid != null )
                {
                        return regid;
                }
                else
                {
                        return "";
                }
        }

        private boolean checkPlayServices()
        {
                int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable( context );
                if ( resultCode != ConnectionResult.SUCCESS )
                {
                        if ( GooglePlayServicesUtil.isUserRecoverableError( resultCode ) )
                        {
                                TRACE( "isUserRecoverableError : " + resultCode );
                        }
                        else
                        {
                                TRACE( "This device is not supported." );
                        }
                        return false;
                }
                return true;
        }

        /**
         * Stores the registration ID and the app versionCode in the application's {@code SharedPreferences}.
         *
         * @param context application's context.
         * @param regId registration ID
         */
        private void storeRegistrationId( Context context, String regId )
        {
                final SharedPreferences prefs = getGcmPreferences( context );
                int appVersion = getAppVersion( context );
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString( PROPERTY_REG_ID, regId );
                editor.putInt( PROPERTY_APP_VERSION, appVersion );
                editor.commit();
        }

        /**
         * Gets the current registration ID for application on GCM service, if there is one.
         * <p>
         * If result is empty, the app needs to register.
         *
         * @return registration ID, or empty string if there is no existing
         *         registration ID.
         */
        private String getRegistrationId( Context context )
        {
                final SharedPreferences prefs = getGcmPreferences( context );
                String registrationId = prefs.getString( PROPERTY_REG_ID, "" );
                if ( registrationId == null || registrationId.equals( "" ) )
                {
                        TRACE( "Registration not found." );
                        return null;
                }
                TRACE( "get ID : " + registrationId );
                
                if ( registrationId != null && !registrationId.equals("") )
                	Database.instance(context).updateConfig("GCMID", registrationId);
                // Check if app was updated; if so, it must clear the registration ID
                // since the existing regID is not guaranteed to work with the new
                // app version.
                int registeredVersion = prefs.getInt( PROPERTY_APP_VERSION, Integer.MIN_VALUE );
                int currentVersion = getAppVersion( context );
                if ( registeredVersion != currentVersion )
                {
                        TRACE( "App version changed." );
                        return null;
                }
                return registrationId;
        }

        /**
         * Registers the application with GCM servers asynchronously.
         * <p>
         * Stores the registration ID and the app versionCode in the application's shared preferences.
         */
        private void registerInBackground()
        {
                new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground( Void... params )
                        {
                                String msg = "";
                                try
                                {
                                        if ( gcm == null ) gcm = GoogleCloudMessaging.getInstance( context );
                                        regid = gcm.register( SENDER_ID );
                                        msg = "Device registered, registration ID=" + regid;
                                        TRACE( "msg : Device registered, registration ID=" + regid );
                                        // For this demo: we don't need to send it because the device will send
                                        // upstream messages to a server that echo back the message using the
                                        // 'from' address in the message.
                                        // Persist the regID - no need to register again.
                                        storeRegistrationId( context, regid );
                                }
                                catch ( IOException ex )
                                {
                                        EXCEPTION( ex );
                                }
                                return msg;
                        }

                        @Override
                        protected void onPostExecute( String msg )
                        {
                                TRACE( msg );
                        }
                }.execute( null, null, null );
        }

        /**
         * @return Application's version code from the {@code PackageManager}.
         */
        private static int getAppVersion( Context context )
        {
                try
                {
                        PackageInfo packageInfo = context.getPackageManager().getPackageInfo( context.getPackageName(), 0 );
                        return packageInfo.versionCode;
                }
                catch ( NameNotFoundException e )
                {
                        // should never happen
                        throw new RuntimeException( "Could not get package name: " + e );
                }
        }

        /**
         * @return Application's {@code SharedPreferences}.
         */
        private SharedPreferences getGcmPreferences( Context context )
        {
                // This sample app persists the registration ID in shared preferences, but
                // how you store the regID in your app is up to you.
                return context.getSharedPreferences( MainActivity.class.getSimpleName(), Context.MODE_PRIVATE );
        }
        private static final String TAG = "/AtSmart/GcmManager";

        public void TRACE( String s )
        {
                if ( !Define.useTrace ) return;
                android.util.Log.i( TAG, s );
        }

        public void EXCEPTION( Throwable e )
        {
                android.util.Log.e( TAG, e.getMessage(), e );
                if ( Define.saveErrorLog )
                {
                        java.io.FileWriter fw = null;
                        java.io.PrintWriter pw = null;
                        try
                        {
                                fw = new java.io.FileWriter(
                                                android.os.Environment.getExternalStoragePublicDirectory( android.os.Environment.DIRECTORY_DOWNLOADS )
                                                                + java.io.File.separator + "AtSmartErrorLog.txt", true );
                                pw = new java.io.PrintWriter( fw, false );
                                pw.print( "[" + new java.util.Date() + "]" + "\n" );
                                e.printStackTrace( pw );
                                pw.flush();
                        }
                        catch ( Exception ie )
                        {
                                e.printStackTrace();
                        }
                        finally
                        {
                                if ( fw != null )
                                {
                                        try
                                        {
                                                fw.close();
                                                fw = null;
                                        }
                                        catch ( Exception ie )
                                        {}
                                }
                                if ( pw != null )
                                {
                                        try
                                        {
                                                pw.close();
                                                pw = null;
                                        }
                                        catch ( Exception ie )
                                        {}
                                }
                        }
                }
        }
}
