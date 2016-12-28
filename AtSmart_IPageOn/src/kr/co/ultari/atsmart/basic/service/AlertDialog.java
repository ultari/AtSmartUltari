package kr.co.ultari.atsmart.basic.service;

import java.io.BufferedInputStream;
import java.net.URL;
import java.util.Map.Entry;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AlertDialog extends MessengerActivity implements OnClickListener {
        private Button m_btnRun;
        private Button m_btnCancel;
        private TextView m_txtMessage;
        private TextView m_Title;
        private ImageView iv_userImage;
        private String userId = "";
        private Bitmap myBitmap = null;
        // 2015-04-30
        public static AlertDialog alertDialog = null;

        public static AlertDialog Instance()
        {
                if ( alertDialog == null ) alertDialog = new AlertDialog();
                return alertDialog;
        }

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                Log.d( TAG, "AlertDialog" );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                setContentView( R.layout.alert_new_message );
                /*
                 * lock display FLAG_SHOW_WHEN_LOCKED FLAG_DISMISS_KEYGUARD
                 * FLAG_KEEP_SCREEN_ON FLAG_TURN_SCREEN_ON FLAG_SHOW_WALLPAPER
                 */
                getWindow().addFlags(
                                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER );
                MainActivity.alert = this;
                m_btnRun = ( Button ) findViewById( R.id.runAlert );
                m_btnCancel = ( Button ) findViewById( R.id.closeAlert );
                m_btnRun.setOnClickListener( this );
                m_btnCancel.setOnClickListener( this );
                m_txtMessage = ( TextView ) findViewById( R.id.detailMessage );
                String showTitle = getIntent().getStringExtra( "TITLE" );
                String showMessage = getIntent().getStringExtra( "MESSAGE" );
                String roomId = getIntent().getStringExtra( "RoomId" );
                if ( showMessage.indexOf( "ATTACH://" ) == 0 ) showMessage = StringUtil.getChatTypeString( showMessage ) + " 파일이 도착했습니다.";
                userId = getIntent().getStringExtra( "userId" );
                iv_userImage = ( ImageView ) findViewById( R.id.detailUserImage );
                if ( Define.getSmallBitmap( userId ) == null )
                {
                        if ( roomId.equals( "note" ) ) iv_userImage.setBackgroundResource( R.drawable.img_contract_list );
                        else downloadUserIcon();
                }
                else
                {
                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) iv_userImage.setBackground( getDrawableFromBitmap( Define
                                        .getSmallBitmap( userId ) ) );
                        else iv_userImage.setBackgroundDrawable( getDrawableFromBitmap( Define.getSmallBitmap( userId ) ) );
                }
                showTitle = showTitle.replaceAll( "<br />", " " );
                showTitle = showTitle.replaceAll( "\n", " " );
                String parse = "";
                String tmp = "";
                int pos = 0;
                while ( pos < showMessage.length() )
                {
                        if ( showMessage.charAt( pos ) == '/' && (pos + 1) < showMessage.length() && showMessage.charAt( pos + 1 ) == 'E' )
                        {
                                tmp = showMessage.substring( pos + 2, showMessage.indexOf( "/", pos + 2 ) );
                                parse += "<img src=\"" + tmp + "\" width=50 height=50>";
                                pos = showMessage.indexOf( "/", pos + 2 ) + 1;
                        }
                        else
                        {
                                parse += showMessage.charAt( pos );
                                pos++;
                        }
                }
                m_txtMessage.setText( Html.fromHtml( parse, imageGetter, null ) );
                // m_txtMessage.setText(Html.fromHtml(showMessage));
                m_Title = ( TextView ) findViewById( R.id.alertDialogTitle );
                m_Title.setText( showTitle );
        }

        private void downloadUserIcon()
        {
                AsyncTask.execute( new Runnable() {
                        public void run()
                        {
                                myBitmap = UltariSocketUtil.getUserImage( userId, 100, 100 );
                                if ( myBitmap != null )
                                {
                                        Message m = viewHandler.obtainMessage( Define.AM_REDRAW_IMAGE, null );
                                        viewHandler.sendMessage( m );
                                }
                        }
                } );
        }
        public Handler viewHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REDRAW_IMAGE )
                                {
                                        // 2015-05-03
                                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) iv_userImage
                                                        .setBackground( getDrawableFromBitmap( myBitmap ) );
                                        else iv_userImage.setBackgroundDrawable( getDrawableFromBitmap( myBitmap ) );
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

        public void onClick( View view )
        {
                if ( view == m_btnRun )
                {
                        Define.isAddUserMode = false; // 2015-04-30
                        String roomId = getIntent().getStringExtra( "RoomId" );
                        ActionManager.resumeActivity( this, roomId );
                        NotificationManager nm = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
                        nm.cancel( Define.AtSmartPushNotification );
                        nm.cancel( Define.AtSmartServiceFinished );
                        finish();
                }
                else if ( view == m_btnCancel )
                {
                        finish();
                }
        }

        @Override
        protected void onNewIntent( Intent intent )
        {
                super.onNewIntent( intent );
                setIntent( intent );
                String showTitle = intent.getStringExtra( "TITLE" );
                String showMessage = intent.getStringExtra( "MESSAGE" );
                showTitle = showTitle.replaceAll( "<br />", " " );
                showTitle = showTitle.replaceAll( "\n", " " );
                String parse = "";
                String tmp = "";
                int pos = 0;
                while ( pos < showMessage.length() )
                {
                        if ( showMessage.charAt( pos ) == '/' && (pos + 1) < showMessage.length() && showMessage.charAt( pos + 1 ) == 'E' )
                        {
                                tmp = showMessage.substring( pos + 2, showMessage.indexOf( "/", pos + 2 ) );
                                parse += "<img src=\"" + tmp + "\" width=50 height=50>";
                                pos = showMessage.indexOf( "/", pos + 2 ) + 1;
                        }
                        else
                        {
                                parse += showMessage.charAt( pos );
                                pos++;
                        }
                }
                m_txtMessage.setText( Html.fromHtml( parse, imageGetter, null ) );
                m_Title.setText( showTitle );
                userId = getIntent().getStringExtra( "userId" );
                iv_userImage = ( ImageView ) findViewById( R.id.detailUserImage );
                if ( Define.getSmallBitmap( userId ) == null ) downloadUserIcon();
                else
                {
                        // 2015-05-03
                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) iv_userImage.setBackground( getDrawableFromBitmap( Define
                                        .getSmallBitmap( userId ) ) );
                        else iv_userImage.setBackgroundDrawable( getDrawableFromBitmap( Define.getSmallBitmap( userId ) ) );
                }
        }

        public Drawable getDrawableFromBitmap( Bitmap bitmap )
        {
                Drawable d = new BitmapDrawable( getResources(), bitmap );
                return d;
        }

        private String getMapKey( String value )
        {
                for ( Entry<String, String> entry : Define.mEmoticonMappingNameMap.entrySet() )
                {
                        if ( entry.getValue().equals( value ) )
                        {
                                return entry.getKey();
                        }
                }
                return "";
        }
        ImageGetter imageGetter = new ImageGetter() {
                @Override
                public Drawable getDrawable( String source )
                {
                        try
                        {
                                int resID = getResources().getIdentifier( getMapKey( source ), "drawable", "kr.co.ultari.atsmart.basic" );
                                Drawable drawable = getResources().getDrawable( resID );
                                drawable.setBounds( 0, 0, 50, 50 );
                                return drawable;
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                        return null;
                }
        };

        @Override
        public void onDestroy()
        {
                // m_btnRun.setImageBitmap( null );
                // m_btnCancel.setImageBitmap( null );
                // 2015-04-30
                if ( alertDialog != null ) alertDialog = null;
                super.onDestroy();
        }
        private static final String TAG = "/AtSmart/AlertDialog";

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
