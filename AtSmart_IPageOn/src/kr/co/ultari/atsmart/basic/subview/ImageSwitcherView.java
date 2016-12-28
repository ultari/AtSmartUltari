package kr.co.ultari.atsmart.basic.subview;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.util.CustomWebView;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import com.garamsoft.util.GSHandler;
import com.garamsoft.util.GSHandler.IGSHandle;

public class ImageSwitcherView extends Activity implements OnClickListener, IGSHandle {
        private int thumCount = 0, originalCount = 0, lastPosition = 0, progress = 0;
        private HashMap<Integer, String> mThum = new HashMap<Integer, String>();
        private HashMap<Integer, Bitmap> mThumImage = new HashMap<Integer, Bitmap>();
        private HashMap<Integer, String> mOriImage = new HashMap<Integer, String>();
        private File previewFile = null, originalFile = null;
        private Button btnClose, btnDownload;
        private CustomWebView mWeb;
        private boolean downloadSuccess = false, isDestroyed = false;
        private String roomId = "";
        private ProgressDialog dialog;
        private GSHandler mHandler;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                Define.mContext = this;
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
                setContentView( R.layout.image_switcher );
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE );
                mHandler = new GSHandler( this );
                isDestroyed = false;
                roomId = getIntent().getStringExtra( "RoomId" );
                initData( roomId );
                dialog = new ProgressDialog( ImageSwitcherView.this );
                dialog.setTitle( "Downloading..." );
                dialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
                dialog.setOnCancelListener( new OnCancelListener() {
                        @Override
                        public void onCancel( DialogInterface dialog )
                        {
                                isDestroyed = true;
                                dialog.dismiss();
                        }
                } );
                btnClose = ( Button ) findViewById( R.id.switcher_close );
                btnClose.setOnClickListener( this );
                btnDownload = ( Button ) findViewById( R.id.switcher_download );
                btnDownload.setOnClickListener( this );
                mWeb = ( CustomWebView ) findViewById( R.id.webView_switcher );
                // mWeb = ( WebView ) findViewById( R.id.webView_switcher );
                mWeb.setVerticalScrollBarEnabled( false );
                mWeb.setVerticalScrollbarOverlay( false );
                mWeb.setHorizontalScrollBarEnabled( false );
                mWeb.setHorizontalScrollbarOverlay( false );
                mWeb.setInitialScale( 100 );
                mWeb.getSettings().setBuiltInZoomControls( true );
                mWeb.setGestureDetector( new GestureDetector( new CustomeGestureDetector() ) );
                
                // first item loading
                if ( mOriImage.size() > 0 )
                {
                        File file = new File( mOriImage.get( 0 ) );
                        if ( file.exists() )
                        {
                                btnDownload.setVisibility( View.INVISIBLE );
                                String path = "<img width='90%;' src=\"" + mOriImage.get( 0 ) + "\">";
                                mWeb.loadDataWithBaseURL( "file:///android_asset/", imageHtmlBody( path ), "text/html", "utf-8", "file:///android_asset/" );
                        }
                        else
                        {
                                btnDownload.setVisibility( View.VISIBLE );
                                String path = "<h1>원본 이미지 파일이 존재하지 않습니다.</h1><h1> 하단 다운로드 버튼을 눌러 주십시오.</h1> ";
                                mWeb.loadDataWithBaseURL( "file:///android_asset/", imageHtmlBody( path ), "text/html", "utf-8", "file:///android_asset/" );
                        }
                }
                @SuppressWarnings( "deprecation" )
                Gallery gallryview = ( Gallery ) findViewById( R.id.gallery );
                gallryview.setAdapter( new ImageAdapter( this ) );
                gallryview.setOnItemClickListener( new OnItemClickListener() {
                        @Override
                        public void onItemClick( AdapterView<?> parent, View view, int position, long id )
                        {
                                lastPosition = position;
                                
                                File file = new File( mOriImage.get( position ) );
                                if ( file.exists() )
                                {
                                        btnDownload.setVisibility( View.INVISIBLE );
                                        String path = "<img width='90%;' src=\"" + mOriImage.get( position ) + "\">";
                                        mWeb.loadDataWithBaseURL( "file:///android_asset/", imageHtmlBody( path ), "text/html", "utf-8",
                                                        "file:///android_asset/" );
                                }
                                else
                                {
                                        btnDownload.setVisibility( View.VISIBLE );
                                        String path = "<h1>원본 이미지 파일이 존재하지 않습니다.</h1><h1> 하단 다운로드 버튼을 눌러 주십시오.</h1> ";
                                        mWeb.loadDataWithBaseURL( "file:///android_asset/", imageHtmlBody( path ), "text/html", "utf-8",
                                                        "file:///android_asset/" );
                                }
                        }
                } );
        }
        private class CustomeGestureDetector extends SimpleOnGestureListener {
                @Override
                public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY )
                {
                        if ( e1 == null || e2 == null ) return false;
                        if ( e1.getPointerCount() > 1 || e2.getPointerCount() > 1 ) return false;
                        else
                        {
                                try
                                {
                                        if ( e1.getX() - e2.getX() > 100 && Math.abs( velocityX ) > 500 )
                                        {
                                                if ( lastPosition > 0 ) --lastPosition;
                                                File file = new File( mOriImage.get( lastPosition ) );
                                                if ( file.exists() )
                                                {
                                                        btnDownload.setVisibility( View.INVISIBLE );
                                                        String path = "<img width='90%;' src=\"" + mOriImage.get( lastPosition ) + "\">";
                                                        mWeb.loadDataWithBaseURL( "file:///android_asset/", imageHtmlBody( path ), "text/html", "utf-8",
                                                                        "file:///android_asset/" );
                                                }
                                                else
                                                {
                                                        btnDownload.setVisibility( View.VISIBLE );
                                                        String path = "<h1>원본 이미지 파일이 존재하지 않습니다.</h1><h1> 하단 다운로드 버튼을 눌러 주십시오.</h1> ";
                                                        mWeb.loadDataWithBaseURL( "file:///android_asset/", imageHtmlBody( path ), "text/html", "utf-8",
                                                                        "file:///android_asset/" );
                                                }
                                                return true;
                                        }
                                        else if ( e2.getX() - e1.getX() > 100 && Math.abs( velocityX ) > 500 )
                                        {
                                                if ( lastPosition < mOriImage.size() ) ++lastPosition;
                                                File file = new File( mOriImage.get( lastPosition ) );
                                                if ( file.exists() )
                                                {
                                                        btnDownload.setVisibility( View.INVISIBLE );
                                                        String path = "<img width='90%;' src=\"" + mOriImage.get( lastPosition ) + "\">";
                                                        mWeb.loadDataWithBaseURL( "file:///android_asset/", imageHtmlBody( path ), "text/html", "utf-8",
                                                                        "file:///android_asset/" );
                                                }
                                                else
                                                {
                                                        btnDownload.setVisibility( View.VISIBLE );
                                                        String path = "<h1>원본 이미지 파일이 존재하지 않습니다.</h1><h1> 하단 다운로드 버튼을 눌러 주십시오.</h1> ";
                                                        mWeb.loadDataWithBaseURL( "file:///android_asset/", imageHtmlBody( path ), "text/html", "utf-8",
                                                                        "file:///android_asset/" );
                                                }
                                                return true;
                                        }
                                        else if ( e1.getY() - e2.getY() > 100 && Math.abs( velocityY ) > 800
                                                        && mWeb.getScrollY() >= mWeb.getScale() * (mWeb.getContentHeight() - mWeb.getHeight()) )
                                        {
                                                return true;
                                        }
                                        else if ( e2.getY() - e1.getY() > 100 && Math.abs( velocityY ) > 800 )
                                        {
                                                return true;
                                        }
                                }
                                catch ( Exception e )
                                {}
                                return false;
                        }
                }
        }

        public String imageHtmlBody( String url )
        {
                StringBuffer sb = new StringBuffer( "<HTML>" );
                sb.append( "<HEAD>" );
                sb.append( "</HEAD>" );
                //sb.append( "<BODY style='margin:0; padding:0; text-align:center; background-color:#2d2e35'>" );
                sb.append( "<BODY style='margin:0; padding:0; text-align:center; background-color:#ffffff'>" ); //2016-03-31
                sb.append( "<div style='display:table; width:100%; height:100%'>" );
                sb.append( "<div style='display:table-cell; text-align:center; vertical-align:middle;'>" );
                sb.append( url );
                sb.append( "</div>" );
                sb.append( "</div>" );
                sb.append( "</BODY>" );
                sb.append( "</HTML>" );
                return sb.toString();
        }
        public class ImageAdapter extends BaseAdapter {
                int mGalleryItemBackground;
                private Context mContext;

                public ImageAdapter( Context c )
                {
                        this.mContext = c;
                        TypedArray a = obtainStyledAttributes( R.styleable.HelloGallery );
                        mGalleryItemBackground = a.getResourceId( R.styleable.HelloGallery_android_galleryItemBackground, 0 );
                        a.recycle();
                }

                @Override
                public int getCount()
                {
                        return mThumImage.size();
                }

                @Override
                public Object getItem( int position )
                {
                        return position;
                }

                @Override
                public long getItemId( int position )
                {
                        return position;
                }

                @Override
                public View getView( int position, View convertView, ViewGroup parent )
                {
                        ImageView iv = new ImageView( mContext );
                        iv.setImageBitmap( mThumImage.get( position ) );
                        //iv.setLayoutParams( new Gallery.LayoutParams( 190, 200 ) );
                        iv.setLayoutParams( new Gallery.LayoutParams( 110, 110 ) ); //2016-03-31
                        iv.setScaleType( ImageView.ScaleType.FIT_XY );
                        //iv.setBackgroundResource( mGalleryItemBackground );
                        iv.setBackgroundColor( Color.WHITE ); //2016-03-31
                        iv.setPadding( 10, 10, 10, 10 );
                        
                        return iv;
                }
        }

        @Override
        public boolean onKeyDown( int keyCode, KeyEvent event )
        {
                if ( event.getAction() == KeyEvent.ACTION_DOWN )
                {
                        if ( keyCode == KeyEvent.KEYCODE_BACK )
                        {
                                isDestroyed = true;
                                finish();
                        }
                }
                return super.onKeyDown( keyCode, event );
        }

        private void initData( String roomId )
        {
                ArrayList<ArrayList<String>> chatArr = Database.instance( getApplicationContext() ).selectChatContent( roomId );
                if ( chatArr == null ) return;
                for ( int i = 0; i < chatArr.size(); i++ )
                {
                        ArrayList<String> ar = chatArr.get( i );
                        if ( ar != null )
                        {
                                if ( ar.get( 6 ).startsWith( "ATTACH://" ) )
                                {
                                        String ext = ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) + 1 );
                                        if ( ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" )
                                                        || ext.equalsIgnoreCase( "png" ) || ext.equalsIgnoreCase( "bmp" ) )
                                        {
                                                try
                                                {
                                                        previewFile = new File( getFilesDir(), ar.get( 0 )
                                                                        + ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) ) );
                                                        mThum.put( thumCount, previewFile.getCanonicalPath() );
                                                        mThumImage.put( thumCount, BitmapFactory.decodeFile( previewFile.getCanonicalPath() ) );
                                                        thumCount++;
                                                        originalFile = new File( getFilesDir(), ar.get( 0 )
                                                                        + ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) ) );
                                                        mOriImage.put( originalCount++, originalFile.getCanonicalPath() );
                                                }
                                                catch ( IOException e )
                                                {
                                                        e.printStackTrace();
                                                }
                                        }
                                }
                                else if ( ar.get( 6 ).startsWith( "FILE://" ) )
                                {
                                        try
                                        {
                                                String ext = ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( "." ) + 1 );
                                                if ( ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" )
                                                                || ext.equalsIgnoreCase( "png" ) || ext.equalsIgnoreCase( "bmp" ) )
                                                {
                                                        previewFile = new File( getFilesDir(), "small_" + ar.get( 0 )
                                                                        + ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) ) );
                                                        mThum.put( thumCount, previewFile.getCanonicalPath() );
                                                        mThumImage.put( thumCount, BitmapFactory.decodeFile( previewFile.getCanonicalPath() ) );
                                                        thumCount++;
                                                        originalFile = new File( ar.get( 6 ).substring( 7 ) );
                                                        mOriImage.put( originalCount++, originalFile.getCanonicalPath() );
                                                }
                                        }
                                        catch ( IOException e )
                                        {
                                                e.printStackTrace();
                                        }
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
        public void onPause()
        {
                mWeb.getSettings().setBuiltInZoomControls( false );
                super.onPause();
        }

        @Override
        public void onResume()
        {
                Define.isHomeMode = false;
                super.onResume();
        }

        @Override
        protected void onUserLeaveHint()
        {
                Define.isHomeMode = true;
                finish();
                super.onUserLeaveHint();
        }

        @Override
        public void onDestroy()
        {
                mWeb.getSettings().setBuiltInZoomControls( false );
                recycleView( mWeb );
                mWeb.destroy();
                mWeb = null;
                isDestroyed = true;
                super.onDestroy();
        }

        private void recycleView( View view )
        {
                //2016-03-31 try~catch
                try
                {
                        if ( view != null )
                        {
                                Drawable bg = view.getBackground();
                                if ( bg != null )
                                {
                                        bg.setCallback( null );
                                        (( BitmapDrawable ) bg).getBitmap().recycle();
                                        view.setBackgroundDrawable( null );
                                }
                        }
                }
                catch(Exception e)
                {
                        e.printStackTrace();
                }
        }

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == R.id.switcher_close )
                {
                        finish();
                }
                else if ( v.getId() == R.id.switcher_download )
                {
                        String[] arr = getDownloadFileInfo();
                        if ( arr == null || arr[0].equals( "" ) || arr[1].equals( "" ) ) return;
                        NetworkTask myClientTask = new NetworkTask( arr[0], arr[1] );
                        myClientTask.execute();
                }
        }

        private String[] getDownloadFileInfo()
        {
                String[] res = { "", "" };
                ArrayList<ArrayList<String>> chatArr = Database.instance( getApplicationContext() ).selectChatContent( roomId );
                if ( chatArr == null ) return null;
                for ( int i = 0; i < chatArr.size(); i++ )
                {
                        ArrayList<String> ar = chatArr.get( i );
                        if ( ar != null )
                        {
                                if ( ar.get( 6 ).startsWith( "ATTACH://" ) )
                                {
                                        String ext = ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) + 1 );
                                        if ( ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" )
                                                        || ext.equalsIgnoreCase( "png" ) || ext.equalsIgnoreCase( "bmp" ) )
                                        {
                                                try
                                                {
                                                        String findData = ar.get( 0 ) + ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) );
                                                        if ( mOriImage.get( lastPosition ).indexOf( findData ) >= 0 )
                                                        {
                                                                res[0] = ar.get( 0 ); // msgId
                                                                res[1] = ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '/' ) + 1 ); // fineName
                                                        }
                                                }
                                                catch ( Exception e )
                                                {
                                                        e.printStackTrace();
                                                }
                                        }
                                }
                        }
                }
                return res;
        }

        @Override
        public void handleMessage( Message message )
        {
                try
                {
                        if ( message.what == Define.AM_REDRAW_IMAGE ) dialog.setProgress( progress );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }
        
        public class NetworkTask extends AsyncTask<Void, Void, Void> {
                String response;
                String msgId = "";
                String fileName = "";

                NetworkTask( String _msgId, String _fileName )
                {
                        this.msgId = _msgId;
                        this.fileName = _fileName;
                        dialog.show();
                }

                @Override
                protected Void doInBackground( Void... arg0 )
                {
                        FileOutputStream fo = null;
                        InputStream is = null;
                        OutputStream os = null;
                        InputStreamReader ir = null;
                        OutputStreamWriter ow = null;
                        long totalLength = 0;
                        long gotLength = 0;
                        byte[] buf = new byte[4096];
                        char[] cbuf = new char[4096];
                        File targetFile = new File( mOriImage.get( lastPosition ) ); // path
                        Socket sc = null;
                        downloadSuccess = false;
                        progress = 0;
                        try
                        {
                                fo = new FileOutputStream( targetFile );
                                sc = UltariSocketUtil.getProxySocket();
                                is = sc.getInputStream();
                                os = sc.getOutputStream();
                                ir = new InputStreamReader( is, "EUC-KR" );
                                ow = new OutputStreamWriter( os, "EUC-KR" );
                                String sndMsg;
                                if ( Define.useUnicode ) sndMsg = "GETA\t" + this.msgId + "\t" + this.msgId + "\tM";
                                else sndMsg = "GETA\t" + this.msgId + "\t" + URLEncoder.encode( this.fileName, "MS949" ) + "\tM";
                                // sndMsg = "GETA\t" + this.msgId + "\t" + this.msgId + "\tM";
                                sndMsg = sndMsg.trim();
                                ow.write( sndMsg );
                                ow.flush();
                                int rcv = ir.read( cbuf, 0, 4096 );
                                if ( rcv < 0 ) return null;
                                String rcvStr = new String( cbuf, 0, rcv );
                                rcvStr = rcvStr.substring( 0, rcvStr.indexOf( '\f' ) + 1 );
                                if ( rcvStr.indexOf( "ready" ) < 0 ) return null;
                                totalLength = Long.parseLong( rcvStr.substring( rcvStr.lastIndexOf( "\t" ) + 1 ).trim() );
                                // Log.d("Downloading", "ready TotalLength : " + totalLength);
                                sndMsg = "ok\t0\f";
                                ow.write( sndMsg );
                                ow.flush();
                                int oldPercent = 0;
                                while ( (rcv = is.read( buf, 0, 4096 )) >= 0 )
                                {
                                        if ( isDestroyed ) break;
                                        gotLength += rcv;
                                        if ( gotLength > totalLength )
                                        {
                                                rcv -= (gotLength - totalLength);
                                                gotLength = totalLength;
                                        }
                                        fo.write( buf, 0, rcv );
                                        fo.flush();
                                        int percent = ( int ) (( double ) gotLength / ( double ) totalLength * ( double ) 100);
                                        progress = percent;
                                        if ( oldPercent != percent )
                                        {
                                                oldPercent = percent;
                                                mHandler.sendEmptyMessage( Define.AM_REDRAW_IMAGE );
                                        }
                                        if ( gotLength == totalLength )
                                        {
                                                downloadSuccess = true;
                                                sndMsg = "ok\t" + gotLength + "\f";
                                                ow.write( sndMsg );
                                                ow.flush();
                                                Log.d( "Downloading", "Success" );
                                                // return null;
                                        }
                                }
                        }
                        catch ( Exception e )
                        {
                                android.util.Log.e( "AtSmart", "Download", e );
                        }
                        finally
                        {
                                if ( fo != null )
                                {
                                        try
                                        {
                                                fo.close();
                                                fo = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
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
                                if ( ow != null )
                                {
                                        try
                                        {
                                                ow.close();
                                                ow = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                                if ( !downloadSuccess ) targetFile.delete();
                                buf = null;
                        }
                        return null;
                }

                @Override
                protected void onPostExecute( Void result )
                {
                        if ( downloadSuccess )
                        {
                                btnDownload.setVisibility( View.INVISIBLE );
                                String path = "<img width='90%;' src=\"" + mOriImage.get( lastPosition ) + "\">";
                                mWeb.loadDataWithBaseURL( "file:///android_asset/", imageHtmlBody( path ), "text/html", "utf-8", "file:///android_asset/" );
                                // mWeb.loadDataWithBaseURL("file:///android_asset/", imageHtmlBody( mOriImage.get( lastPosition ) ), "text/html", "utf-8",
                                // "file:///android_asset/");
                        }
                        dialog.dismiss();
                        super.onPostExecute( result );
                }
        }
}