package kr.co.ultari.atsmart.basic.control;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.util.ImageUtil;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

@SuppressLint( "HandlerLeak" )
public class UserImageView extends ImageView implements Runnable {
        private static final String TAG = "/AtSmart/UserImageView";
        private Thread thread;
        private Bitmap pic = null;
        private String userId;
        private int IMAGE_MAX_WIDTH = 200;
        private int IMAGE_MAX_HEIGHT = 200;
        public static ConcurrentHashMap<String, ConcurrentLinkedQueue<UserImageView>> threadList = null;
        private boolean isMyPhoto = false;
        
        public UserImageView( Context context )
        {
                super( context );
        }

        public UserImageView( Context context, AttributeSet attrs )
        {
                super( context, attrs );
        }

        public UserImageView( Context context, AttributeSet attrs, int defStyle )
        {
                super( context, attrs, defStyle );
        }

        public void setUserId( String userId )
        {
                this.setUserId( userId, false );
        }
        
        public void setMyPhoto()
        {
                this.isMyPhoto = true;
        }

        public synchronized void setUserIdOval( String m_userId, boolean m_bReload )
        {
                if ( UserImageView.threadList == null )
                {
                        UserImageView.threadList = new ConcurrentHashMap<String, ConcurrentLinkedQueue<UserImageView>>();
                }
                try
                {
                        if ( m_userId.indexOf( "/" ) >= 0 ) this.userId = m_userId.substring( m_userId.indexOf( "/" ) + 1 );
                        else this.userId = m_userId;
                        pic = Define.getBitmap( this.userId );
                        ConcurrentLinkedQueue<UserImageView> ar = UserImageView.threadList.get( userId );
                        if ( m_bReload == false && pic != null )
                        {
                                //setImageBitmap( pic );
                                setImageBitmap( ImageUtil.getDrawOval(pic) ); 
                        }
                        else if ( ar != null )
                        {
                                ar.add( this );
                        }
                        else
                        {
                                ar = new ConcurrentLinkedQueue<UserImageView>();
                                UserImageView.threadList.put( m_userId, ar );
                                thread = new Thread( this );
                                thread.start();
                                ar.add( this );
                        }
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
        }
        
        public synchronized void setUserId( String m_userId, boolean m_bReload )
        {
                if ( UserImageView.threadList == null )
                {
                        UserImageView.threadList = new ConcurrentHashMap<String, ConcurrentLinkedQueue<UserImageView>>();
                }
                try
                {
                        if ( m_userId.indexOf( "/" ) >= 0 ) this.userId = m_userId.substring( m_userId.indexOf( "/" ) + 1 );
                        else this.userId = m_userId;
                        pic = Define.getBitmap( this.userId );
                        ConcurrentLinkedQueue<UserImageView> ar = UserImageView.threadList.get( userId );
                        if ( m_bReload == false && pic != null )
                        {
                                setImageBitmap( pic );
                                //setImageBitmap( ImageUtil.getDrawOval(pic) ); 
                        }
                        else if ( ar != null )
                        {
                                ar.add( this );
                        }
                        else
                        {
                                ar = new ConcurrentLinkedQueue<UserImageView>();
                                UserImageView.threadList.put( m_userId, ar );
                                thread = new Thread( this );
                                thread.start();
                                ar.add( this );
                        }
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
        }
        
        public Handler viewHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REDRAW_IMAGE )
                                {
                                        if(pic == null) return; 
                                        
                                        if(isMyPhoto)
                                                setImageBitmap( pic );
                                        else
                                                setImageBitmap( ImageUtil.getDrawOval(pic) );
                                }
                                else
                                {
                                        super.handleMessage( msg );
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        };

        public void run()
        {
                pic = UltariSocketUtil.getUserImage( userId, IMAGE_MAX_WIDTH, IMAGE_MAX_HEIGHT );
                if ( pic != null )
                {
                        Define.setBitmap( userId, pic );
                        ConcurrentLinkedQueue<UserImageView> ar = UserImageView.threadList.get( userId );
                        while ( ar != null && ar.size() > 0 )
                        {
                                UserImageView view = ar.poll();
                                Message m = view.viewHandler.obtainMessage( Define.AM_REDRAW_IMAGE, null );
                                view.viewHandler.sendMessage( m );
                        }
                        UserImageView.threadList.remove( userId );
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
