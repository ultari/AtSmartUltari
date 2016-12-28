package kr.co.ultari.atsmart.basic.subview;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.List;

import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.subdata.PhotoUploader;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint( "HandlerLeak" )
public class ConfigPhoto extends MessengerActivity implements OnClickListener, DialogInterface.OnClickListener, Runnable {
        private Button btnFind = null;
        private Button btnUpload = null;
        private Button btnDelete = null;
        private Button btnCancel = null;
        private Button btnPick = null;
        private Button btnEdit = null;
        private Button btnClose = null; 
        private File m_nowSelectedFile = null;
        private ProgressDialog prog = null;
        private int CODE_UPLOAD = 1;
        private int CODE_CAMERA = 2;
        private int CODE_EDIT = 3;
        public Context context;
        public UserImageView img;
        public PhotoUploader uploader = null;
        private TextView tvTitle;
        private File file;
        private ConfigPhoto parent;
        private boolean isDelete = false;
        private boolean finished;
        private Thread thread;
        private Bitmap bitmap = null;
        private final String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AtSmart";
        private final String photoFileName = "myPhoto.jpg";

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.config_photo );
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE ); //2016-12-13
                context = this;
                Define.cameraTempFilePath = folderPath + File.separator + photoFileName;
                try
                {
                        tvTitle = ( TextView ) findViewById( R.id.photo_title );
                        tvTitle.setTypeface( Define.tfBold );
                        btnFind = ( Button ) findViewById( R.id.find );
                        btnPick = ( Button ) findViewById( R.id.pick );
                        btnClose = ( Button ) findViewById( R.id.photo_cancel ); 
                        btnUpload = ( Button ) findViewById( R.id.savePhoto );
                        btnDelete = ( Button ) findViewById( R.id.deletePhoto );
                        btnCancel = ( Button ) findViewById( R.id.cancelPhoto );
                        btnEdit = ( Button ) findViewById( R.id.edit );
                        btnFind.setTypeface( Define.tfRegular );
                        btnPick.setTypeface( Define.tfRegular );
                        btnUpload.setTypeface( Define.tfRegular );
                        btnDelete.setTypeface( Define.tfRegular );
                        btnCancel.setTypeface( Define.tfRegular );
                        btnEdit.setTypeface( Define.tfRegular );
                        btnFind.setOnClickListener( this );
                        btnUpload.setOnClickListener( this );
                        btnDelete.setOnClickListener( this );
                        btnCancel.setOnClickListener( this );
                        btnPick.setOnClickListener( this );
                        btnEdit.setOnClickListener( this );
                        btnClose.setOnClickListener( this );
                        img = (UserImageView)findViewById(R.id.UserIcon);
                        
                        /*try
                        {
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Config.RGB_565;
                                bitmap = BitmapFactory.decodeFile( Define.cameraTempFilePath, options );
                        }
                        catch ( Exception e )
                        {
                                TRACE( "PhotFileIsToBig : Resize" );
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Config.RGB_565;
                                options.inSampleSize = 2;
                                bitmap = BitmapFactory.decodeFile( Define.cameraTempFilePath, options );
                        }*/
                        // ActionManager.showProcessingDialog( ConfigPhoto.this, "", "" );
                        //img.setUserId(Define.getMyId(context), true);
                        //myHandler.sendEmptyMessageDelayed( Define.AM_REDRAW, 1000 );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        @Override
        protected void onResume()
        {
                super.onResume();
                myHandler.sendEmptyMessageDelayed( Define.AM_REDRAW, 1000 );
        }

        @Override
        public void onDestroy()
        {
                super.onDestroy();
                m_nowSelectedFile = null;
                if ( myHandler != null ) myHandler = null;
                if ( thread != null ) thread = null;
        }

        public void onClick( DialogInterface dialog, int which )
        {
                setData( false );
        }

        public void onClick( View view )
        {
                try
                {
                        if ( btnEdit == view )
                        {
                                File f = new File( Define.cameraTempFilePath );
                                if ( f.exists() )
                                {
                                        Intent intent = new Intent( this, kr.co.ultari.atsmart.basic.subview.PhotoEdit.class );
                                        startActivityForResult( intent, CODE_EDIT );
                                }
                        }
                        else if ( btnFind == view )
                        {
                                Intent intent = new Intent();
                                intent.setAction( Intent.ACTION_PICK );
                                intent.setType( "image/*" );
                                startActivityForResult( Intent.createChooser( intent, getString( R.string.selectPhoto ) ), CODE_UPLOAD );
                        }
                        else if ( btnUpload == view )
                        {
                                m_nowSelectedFile = new File( Define.cameraTempFilePath );
                                if ( m_nowSelectedFile == null ) return;
                                // if ( m_nowSelectedFile.length() > 1048576 )
                                // ActionManager.alert(context,
                                // getString(R.string.savePhoto));
                                prog = new ProgressDialog( this );
                                prog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
                                prog.setCancelable( true );
                                prog.setMessage( getString( R.string.readyImage ) );
                                prog.setMax( 100 );
                                prog.setProgress( 0 );
                                prog.show();
                                prog.setOnCancelListener( new DialogInterface.OnCancelListener() {
                                        public void onCancel( DialogInterface dialog )
                                        {
                                                if ( uploader != null ) uploader.finished();
                                        }
                                } );
                                file = m_nowSelectedFile;
                                isDelete = false;
                                finished = false;
                                // thread = new Thread(this);
                                // thread.start();
                                uploader = new PhotoUploader( context, m_nowSelectedFile, this, false );
                        }
                        else if ( btnDelete == view )
                        {
                                Define.removeBitmap( Define.getMyId( context ) );
                                uploader = new PhotoUploader( context, null, this, true );
                                setData( true );
                        }
                        else if ( btnCancel == view )
                        {
                                setData( false );
                                finish();
                        }
                        //2016-04-04
                        else if( btnClose == view )
                        {
                                finish();
                        }
                        //
                        else if ( btnPick == view )
                        {
                                Intent intent = new Intent();
                                Camera camera = Camera.open();
                                Camera.Parameters parameters = camera.getParameters();
                                List<Size> sizeList = parameters.getSupportedPictureSizes();
                                Camera.Size size = getOptimalPictureSize( parameters.getSupportedPictureSizes(), 1280, 720 );
                                parameters.setPreviewSize( size.width, size.height );
                                parameters.setPictureSize( size.width, size.height );
                                parameters.setRotation( 90 );
                                camera.setParameters( parameters );
                                camera.release();
                                Define.cameraTempFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AtSmart"
                                                + File.separator + "myPhoto" + ".jpg";
                                File fileFolderPath = new File( folderPath );
                                fileFolderPath.mkdir();
                                File file = new File( Define.cameraTempFilePath );
                                Uri outputFileUri = Uri.fromFile( file );
                                intent.setAction( MediaStore.ACTION_IMAGE_CAPTURE );
                                intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
                                startActivityForResult( intent, CODE_CAMERA );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void setData( boolean result )
        {
                Intent i = new Intent();
                if ( result == true ) setResult( RESULT_OK, i );
                else setResult( RESULT_CANCELED, i );
                finish();
        }

        @Override
        protected void onActivityResult( int requestCode, int resultCode, Intent data )
        {
                try
                {
                        if ( requestCode == CODE_UPLOAD && resultCode == RESULT_OK )
                        {
                                if ( requestCode == CODE_UPLOAD )
                                {
                                        String url = data.getData().toString();
                                        m_nowSelectedFile = new File( URLDecoder.decode( getPath( url ), "KSC5601" ) );
                                }
                                FileInputStream fis = null;
                                try
                                {
                                        if ( m_nowSelectedFile == null ) return;
                                        fis = new FileInputStream( m_nowSelectedFile );
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inSampleSize = 2;
                                        Bitmap bitmaps = BitmapFactory.decodeStream( fis, null, options );
                                        fis.close();
                                        bitmap = bitmaps;
                                        SaveBitmapToFileCache( bitmaps, folderPath, photoFileName );
                                        showToast();
                                        
                                        myHandler.sendEmptyMessageDelayed( Define.AM_REDRAW, 1000 );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                                finally
                                {
                                        if ( fis != null )
                                        {
                                                try
                                                {
                                                        fis.close();
                                                        fis = null;
                                                }
                                                catch ( Exception ee )
                                                {}
                                        }
                                }
                                
                                /*String url = data.getData().toString();
                                m_nowSelectedFile = new File( URLDecoder.decode( getPath( url ), "KSC5601" ) );
                                if ( m_nowSelectedFile.length() > 200000 )
                                {
                                        try
                                        {
                                                TRACE( "Resize From : " + m_nowSelectedFile.getCanonicalPath() );
                                                TRACE( "Resize To : " + Define.cameraTempFilePath );
                                        }
                                        catch ( Exception e )
                                        {}
                                        FileInputStream fis = null;
                                        try
                                        {
                                                if ( m_nowSelectedFile == null ) return;
                                                TRACE( "FileLength : " + m_nowSelectedFile.length() );
                                                fis = new FileInputStream( m_nowSelectedFile );
                                                BitmapFactory.Options options = new BitmapFactory.Options();
                                                options.inSampleSize = 2;
                                                TRACE( "Resize1 : " + m_nowSelectedFile.length() );
                                                bitmap = BitmapFactory.decodeStream( fis, null, options );
                                                fis.close();
                                                SaveBitmapToFileCache( bitmap, folderPath, photoFileName );
                                                
                                                myHandler.sendEmptyMessageDelayed( Define.AM_REDRAW, 1000 );
                                                showToast();
                                        }
                                        catch ( Exception e )
                                        {
                                                EXCEPTION( e );
                                        }
                                        finally
                                        {
                                                if ( fis != null )
                                                {
                                                        try
                                                        {
                                                                fis.close();
                                                                fis = null;
                                                        }
                                                        catch ( Exception ee )
                                                        {}
                                                }
                                        }
                                }
                                else
                                {
                                        try
                                        {
                                                TRACE( "Copy From : " + m_nowSelectedFile.getCanonicalPath() );
                                                TRACE( "Copy To : " + Define.cameraTempFilePath );
                                        }
                                        catch ( Exception e )
                                        {}
                                        if ( !m_nowSelectedFile.getCanonicalPath().equals( Define.cameraTempFilePath ) )
                                        {
                                                copyFile( m_nowSelectedFile, new File( Define.cameraTempFilePath ) );
                                        }
                                        showToast();
                                }*/
                        }
                        else if ( requestCode == CODE_EDIT )
                        {
                                if ( resultCode == RESULT_OK )
                                {
                                        m_nowSelectedFile = new File( Define.cameraTempFilePath );
                                        if ( m_nowSelectedFile == null ) return;
                                        FileInputStream fis = null;
                                        try
                                        {
                                                fis = new FileInputStream( m_nowSelectedFile );
                                                BitmapFactory.Options options = new BitmapFactory.Options();
                                                options.inSampleSize = 2;
                                                TRACE( "Resize2" );
                                                Bitmap bitmap = BitmapFactory.decodeStream( fis, null, options );
                                                fis.close();
                                                
                                                img.setImageBitmap(bitmap);
                                                showToast();
                                        }
                                        catch ( Exception e )
                                        {
                                                EXCEPTION( e );
                                        }
                                        finally
                                        {
                                                if ( fis != null )
                                                {
                                                        try
                                                        {
                                                                fis.close();
                                                                fis = null;
                                                        }
                                                        catch ( Exception ee )
                                                        {}
                                                }
                                        }
                                }
                        }
                        else if ( requestCode == CODE_CAMERA )
                        {
                                Log.d( "CameraPath", Define.cameraTempFilePath );
                                try
                                {
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        options.inPreferredConfig = Config.RGB_565;
                                        options.inSampleSize = 2;
                                        TRACE( "Resize3" );
                                        bitmap = BitmapFactory.decodeFile( Define.cameraTempFilePath, options );
                                        // iv_test.setImageBitmap(bitmap);
                                        ExifInterface exif;
                                        exif = new ExifInterface( Define.cameraTempFilePath );
                                        int exifOrientation = exif.getAttributeInt( ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL );
                                        int exifDegree = exifOrientationToDegrees( exifOrientation );
                                        bitmap = rotate( bitmap, exifDegree );
                                        showToast();
                                }
                                catch ( Exception e )
                                {
                                        e.printStackTrace();
                                }
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        private void copyFile( File file1, File file2 )
        {
                FileInputStream fi = null;
                FileOutputStream fo = null;
                byte[] buf = new byte[4096];
                int rcv = 0;
                try
                {
                        fi = new FileInputStream( file1 );
                        fo = new FileOutputStream( file2 );
                        while ( (rcv = fi.read( buf, 0, 4096 )) >= 0 )
                        {
                                fo.write( buf, 0, rcv );
                                fo.flush();
                        }
                }
                catch ( Exception e )
                {
                        Log.e( TAG, "copy", e );
                }
                finally
                {
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
                        if ( fo != null )
                        {
                                try
                                {
                                        fo.close();
                                        fo = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                }
        }

        // Bitmap to File
        public void SaveBitmapToFileCache( Bitmap bitmap, String strFilePath, String filename )
        {
                File file = new File( strFilePath );
                if ( !file.exists() )
                {
                        file.mkdirs();
                }
                File fileCacheItem = new File( strFilePath + File.separator + filename );
                OutputStream out = null;
                try
                {
                        fileCacheItem.createNewFile();
                        out = new FileOutputStream( fileCacheItem );
                        bitmap.compress( CompressFormat.JPEG, 100, out );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                finally
                {
                        try
                        {
                                out.close();
                        }
                        catch ( IOException e )
                        {
                                e.printStackTrace();
                        }
                }
        }

        /**
         * EXIF정보를 회전각도로 변환하는 메서드
         * 
         * @param exifOrientation EXIF 회전각
         * @return 실제 각도
         */
        public int exifOrientationToDegrees( int exifOrientation )
        {
                if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_90 )
                {
                        return 90;
                }
                else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_180 )
                {
                        return 180;
                }
                else if ( exifOrientation == ExifInterface.ORIENTATION_ROTATE_270 )
                {
                        return 270;
                }
                return 0;
        }

        /**
         * 이미지를 회전시킵니다.
         * 
         * @param bitmap 비트맵 이미지
         * @param degrees 회전 각도
         * @return 회전된 이미지
         */
        public Bitmap rotate( Bitmap bitmap, int degrees )
        {
                if ( degrees != 0 && bitmap != null )
                {
                        Matrix m = new Matrix();
                        m.setRotate( degrees, ( float ) bitmap.getWidth() / 2, ( float ) bitmap.getHeight() / 2 );
                        try
                        {
                                Bitmap converted = Bitmap.createBitmap( bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true );
                                if ( bitmap != converted )
                                {
                                        bitmap.recycle();
                                        bitmap = converted;
                                }
                        }
                        catch ( OutOfMemoryError ex )
                        {}
                }
                return bitmap;
        }

        // 지정한 해상도에 가장 최적화 된 카메라 캡쳐 사이즈 구해주는 메소드
        private Size getOptimalPictureSize( List<Size> sizeList, int width, int height )
        {
                Size prevSize = sizeList.get( 0 );
                Size optSize = sizeList.get( 1 );
                for ( Size size : sizeList )
                {
                        int diffWidth = Math.abs( (size.width - width) );
                        int diffHeight = Math.abs( (size.height - height) );
                        int diffWidthPrev = Math.abs( (prevSize.width - width) );
                        int diffHeightPrev = Math.abs( (prevSize.height - height) );
                        int diffWidthOpt = Math.abs( (optSize.width - width) );
                        int diffHeightOpt = Math.abs( (optSize.height - height) );
                        if ( diffWidth < diffWidthPrev && diffHeight <= diffHeightOpt )
                        {
                                optSize = size;
                        }
                        if ( diffHeight < diffHeightPrev && diffWidth <= diffWidthOpt )
                        {
                                optSize = size;
                        }
                        prevSize = size;
                }
                return optSize;
        }

        private void showToast()
        {
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                TextView text = ( TextView ) layout.findViewById( R.id.tv );
                text.setText( getString( R.string.save_photo ) );
                text.setTypeface( Define.tfRegular );
                Toast toast = new Toast( getApplicationContext() );
                toast.setGravity( Gravity.CENTER, 0, 0 );
                toast.setDuration( Toast.LENGTH_SHORT );
                toast.setView( layout );
                toast.show();
        }

        public String getPath( String url )
        {
                return getRealPathFromURI( Uri.parse( url ) );
        }

        public String getRealPathFromURI( Uri contentUri )
        {
                try
                {
                        String filePath = "";
                        Cursor c = getContentResolver().query( contentUri, null, null, null, null );
                        if ( c.moveToNext() )
                        {
                                filePath = c.getString( c.getColumnIndex( MediaStore.MediaColumns.DATA ) );
                        }
                        c.close();
                        return filePath;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
        }
        public Handler myHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REDRAW )
                                {
                                        if ( bitmap != null ) img.setImageBitmap( bitmap );
                                        else img.setUserId( Define.getMyId( context ) );
                                        //img.setUserId(Define.getMyId(context), true);
                                }
                                else if ( msg.what == Define.AM_SEND_COMPLETE )
                                {
                                        int complete = msg.getData().getInt( "complete" );
                                        if ( prog != null )
                                        {
                                                if ( complete == 0 ) prog.setMessage( getString( R.string.sendImage ) );
                                                prog.setProgress( complete );
                                                if ( complete == 100 )
                                                {
                                                        prog.dismiss();
                                                        setData( true );
                                                }
                                        }
                                        else if ( prog == null )
                                        {
                                                uploader.finished();
                                        }
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        };
        private static final String TAG = "/AtSmart/ConfigPhoto";

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
                TRACE( "UploadFile : " + file + "(" + isDelete + ")" );
                boolean complete = false;
                try
                {
                        sc = UltariSocketUtil.getProxySocket();
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
                        ow.write( sndMsg );
                        ow.flush();
                        byte[] buf = new byte[4096];
                        char[] cbuf = new char[1024];
                        int rcv = 0;
                        long totalSendSize = 0;
                        long fileLength = file.length();
                        rcv = ir.read( cbuf, 0, 4096 );
                        if ( new String( cbuf, 0, rcv ).indexOf( "ready" ) < 0 ) return;
                        int percent = 0;
                        Message m = myHandler.obtainMessage( Define.AM_SEND_COMPLETE );
                        Bundle b = new Bundle();
                        b.putInt( "complete", percent );
                        m.setData( b );
                        myHandler.sendMessage( m );
                        fi = new FileInputStream( file );
                        int oldPercent = -1;
                        TRACE( "TOTAL : " + file.length() );
                        while ( (rcv = fi.read( buf, 0, 4096 )) >= 0 && !finished )
                        {
                                if ( finished ) break;
                                os.write( buf, 0, rcv );
                                os.flush();
                                totalSendSize += rcv;
                                TRACE( "total:" + totalSendSize );
                                if ( !finished )
                                {
                                        percent = ( int ) ( long ) (( double ) totalSendSize / ( double ) fileLength * 100.);
                                        if ( oldPercent != percent )
                                        {
                                                m = myHandler.obtainMessage( Define.AM_SEND_COMPLETE );
                                                b = new Bundle();
                                                b.putInt( "complete", percent );
                                                m.setData( b );
                                                myHandler.sendMessage( m );
                                                oldPercent = percent;
                                        }
                                        if ( totalSendSize >= fileLength )
                                        {
                                                complete = true;
                                                break;
                                        }
                                }
                        }
                        while ( is.read() >= 0 )
                                ;
                        buf = null;
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
                                        if ( myHandler == null )
                                        {
                                                TRACE( "myHandler null" );
                                                return;
                                        }
                                        TRACE( "myHandler SEND_COMPLETE" );
                                        Message m = myHandler.obtainMessage( Define.AM_SEND_COMPLETE );
                                        Bundle b = new Bundle();
                                        b.putInt( "complete", 100 );
                                        m.setData( b );
                                        myHandler.sendMessage( m );
                                }
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
                        }
                }
        }
}
