package kr.co.ultari.atsmart.basic.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ImageViwer extends MessengerActivity implements OnClickListener {
        private static final String TAG = "/AtSmart/ImageViewer";
        private Button close = null, save = null;
        private String fileName = null, filePath = null;
        private RelativeLayout mWebContainer;
        private WebView mWebView;
        private final int viewerClose = 1, viewerSave = 0;

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                try
                {
                        Define.mContext = this;
                        requestWindowFeature( Window.FEATURE_NO_TITLE );
                        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
                        setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );
                        setContentView( R.layout.imageviewer );
                        mWebContainer = ( RelativeLayout ) findViewById( R.id.viewer_layout );
                        mWebView = new WebView( getApplicationContext() );
                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT );
                        mWebView.setLayoutParams( params );
                        mWebContainer.addView( mWebView );
                        Intent intent = getIntent();
                        if ( intent != null )
                        {
                                filePath = intent.getExtras().getString( "path" );
                                fileName = intent.getExtras().getString( "name" );
                        }
                        setAlbumImageBackground( filePath );
                        close = new Button( this, null, R.style.ButtonText );
                        close.setId( viewerClose );
                        close.setOnClickListener( this );
                        close.setText( getString( R.string.close ) );
                        close.setBackgroundResource( R.drawable.btn_blue );
                        RelativeLayout.LayoutParams closeButtonParams = new RelativeLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT );
                        closeButtonParams.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );
                        closeButtonParams.addRule( RelativeLayout.ALIGN_PARENT_LEFT );
                        closeButtonParams.leftMargin = ( int ) (60f * this.getResources().getDisplayMetrics().density);
                        closeButtonParams.bottomMargin = ( int ) (5f * this.getResources().getDisplayMetrics().density);
                        close.setLayoutParams( closeButtonParams );
                        save = new Button( this, null, R.style.ButtonText );
                        save.setId( viewerSave );
                        save.setOnClickListener( this );
                        save.setText( getString( R.string.save ) );
                        save.setBackgroundResource( R.drawable.btn_blue );
                        RelativeLayout.LayoutParams saveButtonParams = new RelativeLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT );
                        saveButtonParams.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );
                        saveButtonParams.addRule( RelativeLayout.ALIGN_PARENT_LEFT );
                        saveButtonParams.leftMargin = ( int ) (5f * this.getResources().getDisplayMetrics().density);
                        saveButtonParams.bottomMargin = ( int ) (5f * this.getResources().getDisplayMetrics().density);
                        save.setLayoutParams( saveButtonParams );
                        mWebContainer.addView( save );
                        mWebContainer.addView( close );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        @Override
        public void onPause()
        {
                mWebView.getSettings().setBuiltInZoomControls( false );
                super.onPause();
        }

        @Override
        public void onResume()
        {
                super.onResume();
        };

        @Override
        protected void onUserLeaveHint()
        {
                finish();
                super.onUserLeaveHint();
        }

        @Override
        public void onDestroy()
        {
                mWebView.getSettings().setBuiltInZoomControls( false );
                recycleView( mWebView );
                mWebContainer.removeAllViews();
                mWebView.destroy();
                mWebView = null;
                super.onDestroy();
        }

        private void recycleView( View view )
        {
                if ( view != null )
                {
                        Drawable bg = view.getBackground();
                        if ( bg != null )
                        {
                                bg.setCallback( null );
                                (( BitmapDrawable ) bg).getBitmap().recycle();
                                // 2015-05-03
                                if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) view.setBackground( bg );
                                else view.setBackgroundDrawable( bg );
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

        @Override
        public void finish()
        {
                ViewGroup view = ( ViewGroup ) getWindow().getDecorView();
                view.removeAllViews();
                super.finish();
        }

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == viewerClose )
                {
                        finish();
                }
                else if ( v.getId() == viewerSave )
                {
                        saveImage();
                        finish();
                }
        }

        private void saveImage()
        {
                try
                {
                        File copyFile = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), fileName );
                        FileInputStream inputStream;
                        inputStream = new FileInputStream( new File( filePath ) );
                        FileOutputStream outputStream = new FileOutputStream( copyFile.getCanonicalPath() );
                        FileChannel fcin = inputStream.getChannel();
                        FileChannel fcout = outputStream.getChannel();
                        long size = fcin.size();
                        fcin.transferTo( 0, size, fcout );
                        fcout.close();
                        fcin.close();
                        outputStream.close();
                        inputStream.close();
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                        text.setText( getString( R.string.downloadSave ) );
                        text.setTypeface( Define.tfRegular );
                        Toast toast = new Toast( getApplicationContext() );
                        toast.setGravity( Gravity.CENTER, 0, 0 );
                        toast.setDuration( Toast.LENGTH_SHORT );
                        toast.setView( layout );
                        toast.show();
                }
                catch ( FileNotFoundException e )
                {
                        EXCEPTION( e );
                }
                catch ( Exception ee )
                {
                        EXCEPTION( ee );
                }
        }

        private void setAlbumImageBackground( String path )
        {
                mWebView.setVerticalScrollBarEnabled( false );
                mWebView.setVerticalScrollbarOverlay( false );
                mWebView.setHorizontalScrollBarEnabled( false );
                mWebView.setHorizontalScrollbarOverlay( false );
                mWebView.setInitialScale( 100 );
                mWebView.getSettings().setBuiltInZoomControls( true );
                mWebView.loadDataWithBaseURL( "file:///android_asset/", creHtmlBody( path ), "text/html", "utf-8", "file:///android_asset/" );
        }

        public String creHtmlBody( String url )
        {
                StringBuffer sb = new StringBuffer( "<HTML>" );
                sb.append( "<HEAD>" );
                sb.append( "</HEAD>" );
                sb.append( "<BODY style='margin:0; padding:0; text-align:center;'>" );
                sb.append( "<div style='display:table; width:100%; height:100%'>" );
                sb.append( "<div style='display:table-cell; text-align:center; vertical-align:middle;'>" );
                sb.append( "<img width='90%;' src=\"" + url + "\">" );
                sb.append( "</div>" );
                sb.append( "</div>" );
                sb.append( "</BODY>" );
                sb.append( "</HTML>" );
                return sb.toString();
        }

        @Override
        public boolean onKeyDown( int keyCode, KeyEvent event )
        {
                if ( event.getAction() == KeyEvent.ACTION_DOWN )
                {
                        if ( keyCode == KeyEvent.KEYCODE_BACK ) finish();
                }
                return super.onKeyDown( keyCode, event );
        }
}
