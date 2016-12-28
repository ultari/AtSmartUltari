package kr.co.ultari.atsmart.basic.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint( "HandlerLeak" )
public class Launcher extends Activity implements Runnable {
        private Thread thread;
        private ProgressBar m_Progress = null;
        public String g_oldVersion;
        public String g_newVersion;
        public int DOWNLOAD_COMPLETE = 0x01;
        public int DOWNLOAD_ERROR = 0x02;
        public Launcher _instance;
        public boolean m_bInstall = false;

        /** Called when the activity is first created. */
        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                setContentView( R.layout.launcher );
                _instance = this;
        }

        @Override
        public void onStart()
        {
                super.onStart();
                g_oldVersion = getOldVersion();
                g_newVersion = getNewVersion();
                TextView oldVersionView = ( TextView ) findViewById( R.id.oldVersion );
                TextView newVersionView = ( TextView ) findViewById( R.id.newVersion );
                if ( oldVersionView != null ) oldVersionView.setText( getString( R.string.oldVersion ) + ": " + g_oldVersion );
                if ( newVersionView != null ) newVersionView.setText( getString( R.string.newVersion ) + ": " + g_newVersion );
                m_Progress = ( ProgressBar ) findViewById( R.id.progressBar );
                thread = new Thread( this );
                thread.start();
        }

        public String getOldVersion()
        {
                return Define.VERSION;
        }

        public String getNewVersion()
        {
                return Define.NEW_VERSION;
        }

        public void install()
        {
                m_bInstall = true;
                // File targetFile = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), "AtSmart_basic.apk" );
                File targetFile = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), Define.getBrandsApkName() );
                Uri apkUri = Uri.fromFile( targetFile );
                Intent intent = new Intent( Intent.ACTION_VIEW );
                intent.setDataAndType( apkUri, "application/vnd.android.package-archive" );
                startActivity( intent );
        }

        @Override
        public void onDestroy()
        {
                TRACE( "onDestroy" );
                if ( !m_bInstall )
                {
                        Intent intent = new Intent( this, kr.co.ultari.atsmart.basic.AtSmart.class );
                        startActivity( intent );
                }
                super.onDestroy();
        }

        public void run()
        {
                TRACE( "run()" );
                File targetFile = null;
                URL url = null;
                HttpURLConnection sc = null;
                InputStream is = null;
                FileOutputStream os = null;
                try
                {
                        if ( g_oldVersion.equals( g_newVersion ) )
                        {
                                Message m = myHandler.obtainMessage( DOWNLOAD_COMPLETE );
                                m.arg1 = 100;
                                myHandler.sendMessage( m );
                        }
                        else
                        {
                                // targetFile = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), "AtSmart_basic.apk"
                                // );
                                targetFile = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ),
                                                Define.getBrandsApkName() );
                                url = new URL( Define.UPDATE_URL );
                                sc = ( HttpURLConnection ) url.openConnection();
                                int totalSize = sc.getContentLength();
                                int rcvSize = 0;
                                is = sc.getInputStream();
                                os = new FileOutputStream( targetFile );
                                byte[] buf = new byte[4096];
                                int read = 0;
                                while ( (read = is.read( buf, 0, 4096 )) >= 0 )
                                {
                                        os.write( buf, 0, read );
                                        os.flush();
                                        rcvSize += read;
                                        int percent = ( int ) ((( double ) rcvSize / ( double ) totalSize) * ( double ) 100);
                                        if ( percent == 100 && rcvSize < totalSize ) percent = 99;
                                        Message m = myHandler.obtainMessage( DOWNLOAD_COMPLETE );
                                        m.arg1 = percent;
                                        myHandler.sendMessage( m );
                                }
                                buf = null;
                                install();
                        }
                        finish();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( sc != null )
                        {
                                try
                                {
                                        sc.disconnect();
                                        sc = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( is != null )
                        {
                                try
                                {
                                        is.close();
                                        is = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( os != null )
                        {
                                try
                                {
                                        os.close();
                                        os = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                }
        }
        public Handler myHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == DOWNLOAD_COMPLETE )
                                {
                                        int percent = msg.arg1;
                                        m_Progress.setProgress( percent );
                                }
                                else if ( msg.what == DOWNLOAD_ERROR )
                                {
                                        onPrompt( getString( R.string.update ) );
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        };

        public void onPrompt( String str )
        {
                try
                {
                        AlertDialog.Builder alert = new AlertDialog.Builder( this );
                        alert.setTitle( getString( R.string.app_name ) );
                        alert.setIcon( R.drawable.icon );
                        alert.setMessage( str );
                        alert.setCancelable( false );
                        alert.setPositiveButton( getString( R.string.ok ), new DialogInterface.OnClickListener() {
                                public void onClick( DialogInterface dialog, int which )
                                {
                                        dialog.dismiss();
                                        _instance.finish();
                                }
                        } );
                        alert.show();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }
        private static final String TAG = "/AtSmart/Launcher";

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