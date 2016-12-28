package kr.co.ultari.atsmart.basic.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

public class NetworkStatusReceiver extends BroadcastReceiver {
        @SuppressWarnings( "unused" )
        private static String TAG = "NetworkStatusReceiver";
        private static String ssid = null;
        private static boolean isSetted = false;
        private static boolean isWifi = false;
        private static boolean isCompanyWifi = false;
        private static boolean isConnected = false;

        public synchronized static boolean isConnected( Context context )
        {
                if ( isSetted ) return isConnected;
                else
                {
                        ConnectivityManager cm = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
                        checkInternet( context, cm.getActiveNetworkInfo() );
                        return isConnected;
                }
        }

        public synchronized static boolean isWifi( Context context )
        {
                if ( isSetted ) return isWifi;
                else
                {
                        ConnectivityManager cm = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
                        checkInternet( context, cm.getActiveNetworkInfo() );
                        return isWifi;
                }
        }

        public synchronized static boolean isCompanyWifi( Context context )
        {
                if ( isSetted ) return isCompanyWifi;
                else
                {
                        ConnectivityManager cm = ( ConnectivityManager ) context.getSystemService( Context.CONNECTIVITY_SERVICE );
                        checkInternet( context, cm.getActiveNetworkInfo() );
                        return isCompanyWifi;
                }
        }

        @Override
        public void onReceive( Context context, Intent intent )
        {
                NetworkInfo networkInfo = intent.getParcelableExtra( WifiManager.EXTRA_NETWORK_INFO );
                if ( networkInfo != null )
                {
                        checkInternet( context, networkInfo );
                }
        }

        private synchronized static void checkInternet( Context context, NetworkInfo networkInfo )
        {
        		if ( networkInfo == null) isConnected = false; //2016-12-26 null 
        		else if ( networkInfo.getState() == NetworkInfo.State.DISCONNECTED )
                {
                        isConnected = false;
                }
                else if ( networkInfo.getState() == NetworkInfo.State.CONNECTED )
                {
                        isConnected = true;
                        if ( networkInfo.getType() == ConnectivityManager.TYPE_WIFI )
                        {
                                isWifi = true;
                                final WifiManager wifiManager = ( WifiManager ) context.getSystemService( Context.WIFI_SERVICE );
                                final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                                ssid = connectionInfo.getSSID();
                                isCompanyWifi = false;
                                InputStream is = null;
                                InputStreamReader ir = null;
                                BufferedReader br = null;
                                try
                                {
                                        is = context.getResources().openRawResource( kr.co.ultari.atsmart.basic.R.raw.wifi_ssid_list );
                                        ir = new InputStreamReader( is );
                                        br = new BufferedReader( ir );
                                        String line = null;
                                        while ( (line = br.readLine()) != null )
                                        {
                                                if ( quotesLessSSID( line ).equalsIgnoreCase( quotesLessSSID( ssid ) ) )
                                                {
                                                        isCompanyWifi = true;
                                                }
                                        }
                                }
                                catch ( Exception e )
                                {
                                        e.printStackTrace();
                                }
                                finally
                                {
                                        if ( is != null )
                                        {
                                                try
                                                {
                                                        is.close();
                                                        is = null;
                                                }
                                                catch ( Exception e )
                                                {}
                                        }
                                        if ( ir != null )
                                        {
                                                try
                                                {
                                                        ir.close();
                                                        ir = null;
                                                }
                                                catch ( Exception e )
                                                {}
                                        }
                                        if ( br != null )
                                        {
                                                try
                                                {
                                                        br.close();
                                                        br = null;
                                                }
                                                catch ( Exception e )
                                                {}
                                        }
                                }
                        }
                        else
                        {
                                isWifi = false;
                        }
                }
                isSetted = true;
        }

        public static String quotesLessSSID( String SSID )
        {
                if ( SSID != null )
                {
                        if ( SSID.length() > 3 )
                        {
                                char first = SSID.charAt( 0 );
                                char last = SSID.charAt( SSID.length() - 1 );
                                if ( first == '\"' && last == '\"' )
                                {
                                        return SSID.substring( 1, SSID.length() - 1 );
                                }
                                else return SSID;
                        }
                        else return SSID;
                }
                else return SSID;
        }
}
