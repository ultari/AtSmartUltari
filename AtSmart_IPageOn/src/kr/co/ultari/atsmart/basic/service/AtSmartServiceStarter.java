package kr.co.ultari.atsmart.basic.service;

import kr.co.ultari.atsmart.basic.Define;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public class AtSmartServiceStarter extends BroadcastReceiver {
        public static final String ScreenOff = "android.intent.action.SCREEN_OFF";
        public static final String ScreenOn = "android.intent.action.SCREEN_ON";

        @Override
        public void onReceive( Context context, Intent intent )
        {
                if ( intent.getAction().equals( "android.intent.action.BOOT_COMPLETED" ) )
                {
                        ComponentName cn = new ComponentName( context.getPackageName(), AtSmartService.class.getName() );
                        ComponentName svcName = context.startService( new Intent().setComponent( cn ) );
                        if ( svcName == null ) TRACE( "Could not start service " + cn.toString() );
                        else TRACE( "AtTalk started " + cn.toString() );
                        
                        Intent i = new Intent(context, kr.co.ultari.atsmart.ipocall.CallService.class);
                    	context.startService(i);
                }
                else if ( intent.getAction().equals( "ACTION.RESTART.PersistentService" ) )
                {
                        Intent i = new Intent( context, AtSmartService.class );
                        context.startService( i );
                        
                        i = new Intent(context, kr.co.ultari.atsmart.ipocall.CallService.class);
                    	context.startService(i);
                }
                else TRACE( "BROADCAST RECEIVER ACTION : " + intent.getAction() );
        }
        
        private static final String TAG = "/AtSmart/AtSmartServiceStarter";

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
