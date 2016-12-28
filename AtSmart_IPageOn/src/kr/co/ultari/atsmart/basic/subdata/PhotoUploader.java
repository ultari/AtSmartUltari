package kr.co.ultari.atsmart.basic.subdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.subview.ConfigPhoto;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class PhotoUploader extends Thread {
        private static final String TAG = "AtSmart/PhotoUploader";
        private File file;
        private ConfigPhoto parent;
        private boolean isDelete = false;
        boolean finished;
        Context context;

        public PhotoUploader( Context context, File file, ConfigPhoto parent, boolean isDelete )
        {
                this.file = file;
                this.parent = parent;
                this.isDelete = isDelete;
                this.finished = false;
                this.context = context;
                this.start();
        }

        public void finished()
        {
                finished = true;
        }

        public void run()
        {
                Socket sc = null;
                FileInputStream fi = null;
                OutputStream os = null;
                InputStream is = null;
                InputStreamReader ir = null;
                OutputStreamWriter ow = null;
                boolean complete = false;
                try
                {
                        // String proxyIp = context.getString(R.string.PROXYSERVER_IP);
                        // int proxyPort = Integer.parseInt(context.getString(R.string.PROXYSERVER_PORT));
                        // 2015-03-10
                        Log.d( "SaveUserPicture", "getSocket" );
                        sc = UltariSocketUtil.getProxySocket();
                        Log.d( "SaveUserPicture", "getInputOutputStream" );
                        is = sc.getInputStream();
                        os = sc.getOutputStream();
                        ir = new InputStreamReader( is, "EUC-KR" );
                        ow = new OutputStreamWriter( os, "EUC-KR" );
                        String sndMsg = "";
                        if ( isDelete )
                        {
                                sndMsg = "putP\t" + Define.getMyId( context ) + "\t0";
                                ow.write( sndMsg );
                                ow.flush();
                                return;
                        }
                        sndMsg = "puts\t" + Define.getMyId( context ) + "\t" + file.length();
                        Log.d( "SaveUserPicture", sndMsg );
                        ow.write( sndMsg );
                        ow.flush();
                        byte[] buf = new byte[4096];
                        char[] cbuf = new char[1024];
                        int rcv = 0;
                        long totalSendSize = 0;
                        long fileLength = file.length();
                        rcv = ir.read( cbuf, 0, 1024 );
                        if ( new String( cbuf, 0, rcv ).indexOf( "ready" ) < 0 ) return;
                        Log.d( "SaveUserPicture", "got ready" );
                        int percent = 0;
                        Message m = parent.myHandler.obtainMessage( Define.AM_SEND_COMPLETE );
                        Bundle b = new Bundle();
                        b.putInt( "complete", percent );
                        m.setData( b );
                        parent.myHandler.sendMessage( m );
                        fi = new FileInputStream( file );
                        int oldPercent = -1;
                        while ( (rcv = fi.read( buf, 0, 4096 )) >= 0 && !finished )
                        {
                                if ( finished ) break;
                                os.write( buf, 0, rcv );
                                os.flush();
                                totalSendSize += rcv;
                                if ( !finished )
                                {
                                        percent = ( int ) ( long ) (( double ) totalSendSize / ( double ) fileLength * 100);
                                        if ( percent == 100 && totalSendSize < fileLength ) percent = 99;
                                        if ( oldPercent != percent && percent != 100 )
                                        {
                                                m = parent.myHandler.obtainMessage( Define.AM_SEND_COMPLETE );
                                                b = new Bundle();
                                                b.putInt( "complete", percent );
                                                m.setData( b );
                                                parent.myHandler.sendMessage( m );
                                                oldPercent = percent;
                                                Log.d( "SaveUserPicture", "upload : " + percent );
                                        }
                                        if ( totalSendSize >= fileLength )
                                        {
                                                complete = true;
                                                break;
                                        }
                                }
                        }
                        Log.d( "SaveUserPicture", "finish" );
                        while ( is.read() >= 0 )
                                ;
                        Log.d( "SaveUserPicture", "returned1" );
                        buf = null;
                }
                catch ( Exception e )
                {
                        Log.d( "SaveUserPicture", "returned2" );
                        EXCEPTION( e );
                }
                finally
                {
                        if ( sc != null )
                        {
                                try
                                {
                                        sc.close();
                                        sc = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( fi != null )
                        {
                                try
                                {
                                        fi.close();
                                        fi = null;
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
                        if ( ow != null )
                        {
                                try
                                {
                                        ow.close();
                                        ow = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( ir != null )
                        {
                                try
                                {
                                        ir.close();
                                        ir = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        try
                        {
                                if ( complete )
                                {
                                        if ( parent.myHandler == null )
                                        {
                                                TRACE( "parent handler null" );
                                                return;
                                        }
                                        Define.setBitmap( Define.getMyId(), BitmapFactory.decodeFile( file.getCanonicalPath() ) );
                                        Message m = parent.myHandler.obtainMessage( Define.AM_SEND_COMPLETE );
                                        Bundle b = new Bundle();
                                        b.putInt( "complete", 100 );
                                        m.setData( b );
                                        parent.myHandler.sendMessage( m );
                                }
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
                        }
                }
        }

        public void TRACE( String s )
        {
                if ( !Define.useTrace ) return;
                android.util.Log.i( TAG, s );
        }

        public void EXCEPTION( Throwable e )
        {
                android.util.Log.e( TAG, e.getMessage(), e );
        }
}