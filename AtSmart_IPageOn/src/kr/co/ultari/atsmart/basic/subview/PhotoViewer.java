package kr.co.ultari.atsmart.basic.subview;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;

public class PhotoViewer extends MessengerActivity implements Runnable {
        private ProgressBar progress = null;
        private ImageView imgView = null;
        private String userId = null;
        private Bitmap bmp = null;

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                setContentView( R.layout.photo_viewer );
                progress = ( ProgressBar ) findViewById( R.id.photoProgressBar );
                imgView = ( ImageView ) findViewById( R.id.photo );
                progress.setProgress( 0 );
                byte[] byteArray = getIntent().getByteArrayExtra( "image" );
                if ( byteArray != null )
                {
                        bmp = BitmapFactory.decodeByteArray( byteArray, 0, byteArray.length );
                        progress.setVisibility( View.INVISIBLE );
                        imgView.setVisibility( View.VISIBLE );
                        imgView.setImageBitmap( bmp );
                }
                else
                {
                        userId = getIntent().getStringExtra( "userId" );
                        Thread thread = new Thread( this );
                        thread.start();
                }
        }
        Handler handler = new Handler() {
                public void handleMessage( Message msg )
                {
                        if ( msg.what == Define.AM_REFRESH )
                        {
                                if ( bmp != null )
                                {
                                        progress.setProgress( 100 );
                                        progress.setVisibility( View.INVISIBLE );
                                        imgView.setVisibility( View.VISIBLE );
                                        imgView.setImageBitmap( bmp );
                                }
                        }
                        super.handleMessage( msg );
                }
        };

        public void run()
        {
                Socket sc = null;
                InputStream is = null;
                OutputStream os = null;
                OutputStreamWriter ow = null;
                ByteArrayOutputStream bo = null;
                try
                {
                        sc = UltariSocketUtil.getProxySocket();
                        is = sc.getInputStream();
                        os = sc.getOutputStream();
                        ow = new OutputStreamWriter( os, "EUC-KR" );
                        String header = "GET /" + userId + " HTTP/1.1\r\n\r\n";
                        ow.write( header );
                        ow.flush();
                        int rcv;
                        StringBuffer gotHeader = new StringBuffer();
                        byte[] buffer = new byte[1024];
                        bo = new ByteArrayOutputStream();
                        long totalSize = 0;
                        long headerSize = 0;
                        while ( (rcv = is.read( buffer, 0, 1024 )) >= 0 )
                        {
                                totalSize += rcv;
                                bo.write( buffer, 0, rcv );
                                bo.flush();
                                String s = new String( buffer, 0, rcv, "EUC-KR" );
                                gotHeader.append( s );
                                int returnPos = -1;
                                if ( (returnPos = gotHeader.indexOf( "\r\n\r\n" )) >= 0 )
                                {
                                        String lengthStr = gotHeader.substring( gotHeader.indexOf( "Content-Length:" ) + 15 );
                                        lengthStr = lengthStr.substring( 0, lengthStr.indexOf( "\r\n" ) );
                                        lengthStr = lengthStr.trim();
                                        headerSize = returnPos + 4;
                                        break;
                                }
                        }
                        while ( (rcv = is.read( buffer, 0, 1024 )) >= 0 )
                        {
                                totalSize += rcv;
                                bo.write( buffer, 0, rcv );
                                bo.flush();
                        }
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = false;
                        bmp = BitmapFactory.decodeByteArray( bo.toByteArray(), ( int ) headerSize, ( int ) (totalSize - headerSize) );
                        Message msg = handler.obtainMessage( Define.AM_REFRESH, this );
                        handler.sendMessage( msg );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                        Message msg = handler.obtainMessage( Define.AM_REFRESH, this );
                        handler.sendMessage( msg );
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
                                catch ( Exception e )
                                {}
                        }
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
                        if ( os != null )
                        {
                                try
                                {
                                        os.close();
                                        os = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                        if ( bo != null )
                        {
                                try
                                {
                                        bo.close();
                                        bo = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                }
        }
}
