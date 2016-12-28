package kr.co.ultari.atsmart.basic.service;

import kr.co.ultari.atsmart.basic.Define;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

public class GCMReceiver extends WakefulBroadcastReceiver {
        @Override
        public void onReceive( Context context, Intent intent )
        {
                Log.d( TAG, "wakefulBroadcastReceiver" );
                Log.d( TAG, intent.getAction() + " pkg:" + context.getPackageName() );
                Log.d( TAG, "Name:" + GcmIntentService.class.getName() );
                Define.regid = intent.getStringExtra( "registration_id" );
                ComponentName comp = new ComponentName( context.getPackageName(), GcmIntentService.class.getName() );
                startWakefulService( context, (intent.setComponent( comp )) );
                setResultCode( Activity.RESULT_OK );
        }
        private static final String TAG = "/AtSmart/GCM";

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
