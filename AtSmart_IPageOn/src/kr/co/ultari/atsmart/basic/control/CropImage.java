package kr.co.ultari.atsmart.basic.control;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.view.WindowManager;

public class CropImage extends ImageView {
        float sx, ex, sy, ey;
        static int DEP = 30;
        float left;
        float top;
        float dx = 0, dy = 0;
        float oldx, oldy;
        boolean bsx, bsy, bex, bey;
        boolean bMove = false;
        Context cnxt;
        Bitmap bitmap;
        float mWidth;
        float mHeight;
        Paint pnt;
        Bitmap hBmp;
        Bitmap wBmp;
        Display display;
        private String outFilePath = null;

        @SuppressWarnings( "deprecation" )
        @SuppressLint( "NewApi" )
        public CropImage( Context context, AttributeSet attrs )
        {
                super( context, attrs );
                display = (( WindowManager ) context.getSystemService( Context.WINDOW_SERVICE )).getDefaultDisplay();
                Point point = new Point();
                try
                {
                        display.getSize( point );
                }
                catch ( java.lang.NoSuchMethodError ignore )
                {
                        point.x = display.getWidth();
                        point.y = display.getHeight();
                }
                mWidth = point.x;
                mHeight = point.y;
                cnxt = context;
                // File f = new File( context.getFilesDir(), "tempImg.jpg" );
                // 2015-05-02
                File f = new File( Define.cameraTempFilePath );
                try
                {
                        outFilePath = f.getCanonicalPath();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                BitmapFactory.Options resizeOpts = new Options();
                resizeOpts.inSampleSize = 2;
                try
                {
                        bitmap = BitmapFactory.decodeStream( new FileInputStream( outFilePath ), null, resizeOpts );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                if ( bitmap.getWidth() > (point.x) ) mWidth = point.x;
                mHeight = bitmap.getHeight() * mWidth / bitmap.getWidth();
                if ( mHeight > (point.y - 60) )
                {
                        mHeight = point.y - 60;
                        mWidth = bitmap.getWidth() * mHeight / bitmap.getHeight();
                }
                if ( mHeight > point.x )
                {
                        mHeight = point.x - 10;
                        mWidth = bitmap.getWidth() * mHeight / bitmap.getHeight();
                }
                if ( mWidth > point.y )
                {
                        mWidth = point.y - 10;
                        mHeight = bitmap.getHeight() * mWidth / bitmap.getWidth();
                }
                left = 0;
                top = 0;
                sx = left;
                ex = mWidth + left;
                sy = top;
                ey = mHeight + top;
                bitmap = Bitmap.createScaledBitmap( bitmap, ( int ) mWidth, ( int ) mHeight, true );
                hBmp = BitmapFactory.decodeResource( getResources(), R.drawable.camera_crop_height );
                wBmp = BitmapFactory.decodeResource( getResources(), R.drawable.camera_crop_width );
                pnt = new Paint();
                pnt.setColor( Color.MAGENTA );
                pnt.setStrokeWidth( 3 );
        }

        public void onDestroy()
        {
                hBmp.recycle();
                wBmp.recycle();
                bitmap.recycle();
                hBmp = null;
                wBmp = null;
                bitmap = null;
        }

        public void rotate()
        {
                bitmap = rotate( bitmap, 90 );
                mWidth = bitmap.getWidth();
                mHeight = bitmap.getHeight();
                left = (mWidth / 2) - (mWidth / 2);
                top = (mHeight / 2) - (mHeight / 2);
                sx = left;
                ex = mWidth + left;
                sy = top;
                ey = mHeight + top;
                invalidate();
        }

        private Bitmap rotate( Bitmap b, int degrees )
        {
                if ( degrees != 0 && b != null )
                {
                        Matrix m = new Matrix();
                        m.setRotate( degrees, ( float ) b.getWidth() / 2, ( float ) b.getHeight() / 2 );
                        try
                        {
                                Bitmap b2 = Bitmap.createBitmap( b, 0, 0, b.getWidth(), b.getHeight(), m, false );
                                if ( b != b2 )
                                {
                                        b.recycle();
                                        b = b2;
                                }
                        }
                        catch ( OutOfMemoryError ex )
                        {
                                EXCEPTION( ex );
                        }
                }
                return b;
        }

        public void onDraw( Canvas canvas )
        {
                canvas.drawBitmap( bitmap, left, top, null );
                canvas.drawLine( sx, sy, ex, sy, pnt );
                canvas.drawLine( ex, sy, ex, ey, pnt );
                canvas.drawLine( sx, sy, sx, ey, pnt );
                canvas.drawLine( sx, ey, ex, ey, pnt );
                canvas.drawBitmap( hBmp, (ex + sx) / 2 - 19, sy - 19, null );
                canvas.drawBitmap( hBmp, (ex + sx) / 2 - 19, ey - 19, null );
                canvas.drawBitmap( wBmp, sx - 19, (ey + sy) / 2 - 19, null );
                canvas.drawBitmap( wBmp, ex - 19, (ey + sy) / 2 - 19, null );
        }

        @SuppressLint( "ClickableViewAccessibility" )
        @Override
        public boolean onTouchEvent( MotionEvent e )
        {
                int x = ( int ) e.getX();
                int y = ( int ) e.getY();
                if ( e.getAction() == MotionEvent.ACTION_DOWN )
                {
                        oldx = x;
                        oldy = y;
                        if ( (x > sx - DEP) && (x < sx + DEP) ) bsx = true;
                        else if ( (x > ex - DEP) && (x < ex + DEP) ) bex = true;
                        if ( (y > sy - DEP) && (y < sy + DEP) ) bsy = true;
                        else if ( (y > ey - DEP) && (y < ey + DEP) ) bey = true;
                        if ( (bsx || bex || bsy || bey) ) bMove = false;
                        else if ( ((x > sx + DEP) && (x < ex - DEP)) && ((y > sy + DEP) && (y < ey - DEP)) ) bMove = true;
                        return true;
                }
                if ( e.getAction() == MotionEvent.ACTION_MOVE )
                {
                        if ( bsx ) sx = x;
                        if ( bex ) ex = x;
                        if ( bsy ) sy = y;
                        if ( bey ) ey = y;
                        if ( ex <= sx + DEP )
                        {
                                ex = sx + DEP;
                                return true;
                        }
                        if ( ey <= sy + DEP )
                        {
                                ey = sy + DEP;
                                return true;
                        }
                        if ( bMove )
                        {
                                dx = oldx - x;
                                dy = oldy - y;
                                sx -= dx;
                                ex -= dx;
                                sy -= dy;
                                ey -= dy;
                                if ( sx <= 0 ) sx = 0;
                                if ( ex >= (mWidth + left) ) ex = (mWidth + left) - 1;
                                if ( sy <= 0 ) sy = 0;
                                if ( ey >= (mHeight + top) ) ey = (mHeight + top) - 1;
                        }
                        invalidate();
                        oldx = x;
                        oldy = y;
                        return true;
                }
                if ( e.getAction() == MotionEvent.ACTION_UP )
                {
                        bsx = bex = bsy = bey = bMove = false;
                        return true;
                }
                return false;
        }

        public void save()
        {
                if ( sy < top ) sy = top;
                if ( sx < left ) sx = left;
                if ( ex > (bitmap.getWidth() + left) ) ex = bitmap.getWidth() + left;
                if ( ey > (bitmap.getHeight() + top) ) ey = bitmap.getHeight() + top;
                Bitmap tmp = Bitmap.createBitmap( bitmap, ( int ) (sx - left), ( int ) (sy - top), ( int ) (ex - sx), ( int ) (ey - sy) );
                byte[] byteArray = bitmapToByteArray( tmp );
                File file = new File( outFilePath );
                try
                {
                        FileOutputStream fos = new FileOutputStream( file );
                        fos.write( byteArray );
                        fos.flush();
                        fos.close();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return;
                }
        }

        public byte[] bitmapToByteArray( Bitmap bitmap )
        {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress( CompressFormat.JPEG, 100, stream );
                byte[] byteArray = stream.toByteArray();
                return byteArray;
        }
        private static final String TAG = "/AtSmart/CropImage";

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