package kr.co.ultari.atsmart.basic.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Locale;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;

public class ImageUtil {
        private final static String TAG = "AtSmart/ImageUtil";
        private final static int THUMBNAIL_IMAGE_HEIGHT = 200;
        public final static int BIGINTEGER_RADIX = 16;

        public static Bitmap makeThumbnailImage( String filePath )
        {
                FileInputStream fis = null;
                Bitmap originalImage = null;
                try
                {
                        fis = new FileInputStream( filePath );
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 6;
                        originalImage = BitmapFactory.decodeStream( fis, null, options );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
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
                                catch ( Exception e )
                                {}
                        }
                }
                if ( originalImage == null )
                {
                        return null;
                }
                int original_height = originalImage.getHeight();
                if ( original_height > THUMBNAIL_IMAGE_HEIGHT )
                {
                        int newWidth = ( int ) (( float ) originalImage.getWidth() / ( float ) original_height * ( float ) THUMBNAIL_IMAGE_HEIGHT);
                        return Bitmap.createScaledBitmap( originalImage, newWidth, THUMBNAIL_IMAGE_HEIGHT, false );
                }
                return originalImage;
        }

        public static String[] saveResizeImageFile( Context context, Bitmap bitmap, String name )
        {
                File dir = context.getFilesDir();
                if ( dir == null )
                {
                        return null;
                }
                File saveFile = new File( dir, "small_" + name );
                if ( saveFile.exists() )
                {
                        saveFile.delete();
                }
                OutputStream outStream = null;
                try
                {
                        outStream = new FileOutputStream( saveFile );
                        if ( bitmap.compress( Bitmap.CompressFormat.JPEG, 100, outStream ) )
                        {
                                Log.d( TAG, "resize image is successfully made. - " + saveFile );
                        }
                        outStream.flush();
                        outStream.close();
                }
                catch ( FileNotFoundException e )
                {
                        Log.d( TAG, "resize image save error : FileNotFound" + e );
                        e.printStackTrace();
                }
                catch ( IOException e )
                {
                        Log.d( TAG, "resize image save error : IOException" + e );
                        e.printStackTrace();
                }
                finally
                {
                        if ( outStream != null )
                        {
                                try
                                {
                                        outStream.close();
                                        outStream = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                }
                String[] ret = new String[3];
                ret[0] = saveFile.getPath();
                ret[1] = bitmap.getWidth() + "";
                ret[2] = bitmap.getHeight() + "";
                return ret;
        }

        public static boolean checkTypeImageFile( String filename )
        {
                String lowercase = filename.toLowerCase( Locale.KOREA );
                if ( lowercase.endsWith( ".jpg" ) || lowercase.endsWith( ".jpeg" ) || lowercase.endsWith( ".png" ) || lowercase.endsWith( ".bmp" )
                                || lowercase.endsWith( ".gif" ) )
                {
                        return true;
                }
                return false;
        }

        public static Bitmap getDrawOval( Bitmap bitmap )
        {
                Bitmap output = Bitmap.createBitmap( bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888 );
                Canvas canvas = new Canvas( output );
                int color = 0xff424242;
                Paint paint = new Paint();
                Rect rect = new Rect( 0, 0, bitmap.getWidth(), bitmap.getHeight() );
                RectF rectF = new RectF( rect );
                paint.setAntiAlias( true );
                paint.setColor( color );
                canvas.drawARGB( 0, 0, 0, 0 );
                canvas.drawOval( rectF, paint );
                paint.setXfermode( new PorterDuffXfermode( Mode.SRC_IN ) );
                canvas.drawBitmap( bitmap, rect, rect, paint );
                // bitmap.recycle();
                return Bitmap.createScaledBitmap( output, 150, 150, false );
        }

        public static Bitmap getDrawRoundRect( Bitmap bitmap, int pixel )
        {
                Bitmap output = Bitmap.createBitmap( bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888 );
                Canvas canvas = new Canvas( output );
                int color = 0xff424242;
                Paint paint = new Paint();
                Rect rect = new Rect( 0, 0, bitmap.getWidth(), bitmap.getHeight() );
                RectF rectF = new RectF( rect );
                paint.setAntiAlias( true );
                paint.setColor( color );
                canvas.drawARGB( 0, 0, 0, 0 );
                canvas.drawRoundRect( rectF, pixel, pixel, paint );
                paint.setXfermode( new PorterDuffXfermode( Mode.SRC_IN ) );
                canvas.drawBitmap( bitmap, rect, rect, paint );
                bitmap.recycle();
                return output;
        }

        public static void getBitmapFromURL( final Context context, final String userId, final String strURL )
        {
                AsyncTask.execute( new Runnable() {
                        public void run()
                        {
                                Define.setSmallBitmap( userId, UltariSocketUtil.getUserImage( userId, 100, 100 ) );
                        }
                } );
        }

        public static Bitmap loadBitmapFromFileWithMaxWidth( String filePath, int maxWidth )
        {
                BitmapFactory.Options option = new BitmapFactory.Options();
                option.inPreferredConfig = Config.RGB_565;
                option.inJustDecodeBounds = true;
                BitmapFactory.decodeFile( filePath, option );
                Log.d( "kr.co.ultari.atsmart.basic", "original : " + option.outWidth + "," + option.outHeight + " => " + maxWidth );
                float widthScale = option.outWidth / maxWidth;
                option.inSampleSize = ( int ) widthScale;
                option.inJustDecodeBounds = false;
                return BitmapFactory.decodeFile( filePath, option );
        }

        public static Bitmap getDrawOvalNoResize( Bitmap bitmap, int maxSize )
        {
                Bitmap output = Bitmap.createBitmap( bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888 );
                Canvas canvas = new Canvas( output );
                final int color = 0xff424242;
                final Paint paint = new Paint();
                final Rect rect = new Rect( 0, 0, bitmap.getWidth(), bitmap.getHeight() );
                paint.setAntiAlias( true );
                canvas.drawARGB( 0, 0, 0, 0 );
                canvas.drawCircle( bitmap.getWidth() / 2, bitmap.getHeight() / 2, bitmap.getWidth() / 2, paint );
                paint.setXfermode( new PorterDuffXfermode( Mode.SRC_IN ) );
                canvas.drawBitmap( bitmap, rect, rect, paint );
                return output;
                // return Bitmap.createScaledBitmap(output, maxSize, maxSize, false);
        }
}
