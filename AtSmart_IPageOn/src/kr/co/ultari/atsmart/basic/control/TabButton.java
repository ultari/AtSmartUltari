package kr.co.ultari.atsmart.basic.control;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.view.MainViewTabFragment;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.ImageButton;

@SuppressLint( { "ViewConstructor", "DrawAllocation" } )
public class TabButton extends ImageButton {
        private Bitmap m_imgNormalBack;
        private Bitmap m_imgSelectedBack;
        private Bitmap m_imgNormal;
        private Bitmap m_imgSelected;
        private boolean m_bSelected;
        private Paint p;
        private Paint textPaint;
        private Paint countTextPaint;
        private Paint countPaint;
        private String title;
        private Paint subtextPaint;
        private Paint backPaint;
        private int count = 0;

        //2016-03-31
        private Bitmap m_imgActive;
        
        public TabButton( Context context, int normalResource, int selectedResource, boolean m_bSelected, String title )
        {
                super( context );
                this.m_bSelected = m_bSelected;
                this.title = title;
                m_imgNormalBack = BitmapFactory.decodeResource( getResources(), R.drawable.bar );
                m_imgSelectedBack = BitmapFactory.decodeResource( getResources(), R.drawable.bar );
                m_imgNormal = BitmapFactory.decodeResource( getResources(), normalResource );
                m_imgSelected = BitmapFactory.decodeResource( getResources(), selectedResource );
                
                //2016-03-31
                m_imgActive = BitmapFactory.decodeResource( getResources(), R.drawable.btn_active );
                
                p = new Paint();
                p.setAlpha( 0 );
                backPaint = new Paint();
                backPaint.setColor( 0x00000000 ); //2016-03-31
                //backPaint.setColor( 0xFF8d9dd8 ); 2016-03-31
                textPaint = new Paint();
                textPaint.setAntiAlias( true );
                textPaint.setTextSize( Define.getDpFromPx( context, 39 ) );
                textPaint.setColor( 0xFFFFFFFF );
                Typeface fontFace = Typeface.createFromAsset( context.getAssets(), "Regular.otf.mp3" );
                textPaint.setTypeface( fontFace );
                subtextPaint = new Paint();
                subtextPaint.setAntiAlias( true );
                subtextPaint.setTextSize( Define.getDpFromPx( context, 43 ) );
                subtextPaint.setColor( 0xFFFFFFFF );
                subtextPaint.setTypeface( fontFace );
                countTextPaint = new Paint();
                countTextPaint.setAntiAlias( true );
                countTextPaint.setTextSize( Define.getDpFromPx( context, 30 ) );
                countTextPaint.setColor( 0xFFFFFFFF );
                countTextPaint.setTypeface( fontFace );
                countPaint = new Paint();
                countPaint.setAntiAlias( true );
                countPaint.setColor( 0xFFFF0000 );
        }

        public void setSelected( boolean m_bSelected )
        {
                this.m_bSelected = m_bSelected;
                invalidate();
        }

        public void updateTab()
        {
                invalidate();
        }

        public void setNumber( int count )
        {
                this.count = count;
                this.invalidate();
        }

        public void onDestroy()
        {
                m_imgNormalBack.recycle();
                m_imgSelectedBack.recycle();
                m_imgNormal.recycle();
                m_imgSelected.recycle();
                m_imgNormalBack = null;
                m_imgSelectedBack = null;
                m_imgNormal = null;
                m_imgSelected = null;
                
                //2016-03-31
                m_imgActive.recycle();
                m_imgActive = null;
                //
        }

        @SuppressLint( "DrawAllocation" )
        public void onDraw( Canvas canvas )
        {
                super.onDraw( canvas );
                if ( m_bSelected )
                {
                        // Rect src = new Rect( 0, 0, m_imgSelectedBack.getWidth(), m_imgSelectedBack.getHeight() );
                        // Rect dst = new Rect( 30, 0, getWidth() - 30, getHeight() );
                        // canvas.drawBitmap( m_imgSelectedBack, src, dst, null );
                        canvas.drawRect( 0, 0, getWidth(), getHeight(), backPaint );
                        int imgWidth = m_imgSelected.getWidth();
                        int width = getWidth();
                        if ( imgWidth > width )
                        {
                                imgWidth = width;
                        }
                        int imgHeight = ( int ) (( float ) imgWidth / ( float ) m_imgSelected.getWidth() * ( float ) m_imgSelected.getHeight());
                        if ( imgHeight > getHeight() )
                        {
                                imgHeight = getHeight();
                                imgWidth = ( int ) (( float ) m_imgSelected.getWidth() / ( float ) m_imgSelected.getHeight() * ( float ) imgHeight);
                        }
                        Rect src1 = new Rect( 0, 0, m_imgSelected.getWidth(), m_imgSelected.getHeight() );
                        Rect dst1 = new Rect( (getWidth() / 2) - (imgWidth / 2), (getHeight() / 2) - (imgHeight / 2) - Define.getDpFromPx( getContext(), 40 ),
                                        (getWidth() / 2) - (imgWidth / 2) + imgWidth, (getHeight() / 2) - (imgHeight / 2) + imgHeight
                                                        - Define.getDpFromPx( getContext(), 40 ) );
                        canvas.drawBitmap( m_imgSelected, src1, dst1, null );
                        int nameLeft = (getWidth() / 2) - (( int ) textPaint.measureText( title ) / 2);
                        canvas.drawText( title, nameLeft, (Define.tabHeight / 12), textPaint );
                        
                        //2016-03-31
                        canvas.drawBitmap( m_imgActive, (getWidth()/2 - m_imgActive.getWidth()/2), (Define.tabHeight / 11) + Define.getDpFromPx( getContext(), 5), null );
                        //
                        
                        textPaint.setColor( 0xFFFFFFFF );
                        countPaint.setAntiAlias( true );
                        countPaint.setColor( 0xFF343D6C );
                        countTextPaint.setAntiAlias( true );
                        countTextPaint.setColor( 0xFFFFFFFF );
                        if ( count > 0 )
                        {
                                String drawString = count + "";
                                float textWidth = countPaint.measureText( drawString );
                                float textHeight = countPaint.getTextSize();
                                float left = getWidth() - textWidth - 20;
                                float top = 10;
                                int remainder = ( int ) (textWidth - 6);
                                canvas.drawCircle( left - (textWidth / 2) + remainder, top + (textHeight / 2) + 14, textWidth + 10, countPaint );
                                canvas.drawText( drawString, left - (textWidth / 2) + remainder - textWidth, top + textHeight + 15, countTextPaint );
                        }
                }
                else
                {
                        canvas.drawRect( 0, 0, getWidth(), getHeight(), backPaint );
                        int imgWidth = m_imgNormal.getWidth();
                        int width = getWidth();
                        if ( imgWidth > width )
                        {
                                imgWidth = width;
                        }
                        int imgHeight = ( int ) (( float ) imgWidth / ( float ) m_imgNormal.getWidth() * ( float ) m_imgNormal.getHeight());
                        if ( imgHeight > getHeight() )
                        {
                                imgHeight = getHeight();
                                imgWidth = ( int ) (( float ) m_imgNormal.getWidth() / ( float ) m_imgNormal.getHeight() * ( float ) imgHeight);
                        }
                        Rect src1 = new Rect( 0, 0, m_imgNormal.getWidth(), m_imgNormal.getHeight() );
                        Rect dst1 = new Rect( (getWidth() / 2) - (imgWidth / 2), (getHeight() / 2) - (imgHeight / 2) - Define.getDpFromPx( getContext(), 40 ),
                                        (getWidth() / 2) - (imgWidth / 2) + imgWidth, (getHeight() / 2) - (imgHeight / 2) + imgHeight
                                                        - Define.getDpFromPx( getContext(), 40 ) );
                        canvas.drawBitmap( m_imgNormal, src1, dst1, null );
                        int nameLeft = (getWidth() / 2) - (( int ) textPaint.measureText( title ) / 2);
                        canvas.drawText( title, nameLeft, (Define.tabHeight / 12), textPaint );
                        textPaint.setColor( 0xFF363F6D );
                        countPaint.setAntiAlias( true );
                        countPaint.setColor( 0xFFFFFFFF );
                        countTextPaint.setAntiAlias( true );
                        countTextPaint.setColor( 0xFF9EACDE );
                        if ( count > 0 )
                        {
                                String drawString = count + "";
                                float textWidth = countPaint.measureText( drawString );
                                float textHeight = countPaint.getTextSize();
                                float left = getWidth() - textWidth - 20;
                                float top = 10;
                                int remainder = ( int ) (textWidth - 6);
                                canvas.drawCircle( left - (textWidth / 2) + remainder, top + (textHeight / 2) + 14, textWidth + 10, countPaint );
                                canvas.drawText( drawString, left - (textWidth / 2) + remainder - textWidth, top + textHeight + 15, countTextPaint );
                        }
                }
        }
        /*
         * @SuppressLint("DrawAllocation")
         * public void onDraw(Canvas canvas)
         * {
         * super.onDraw( canvas );
         * if ( m_bSelected )
         * {
         * //Rect src = new Rect( 0, 0, m_imgSelectedBack.getWidth(), m_imgSelectedBack.getHeight() );
         * //Rect dst = new Rect( 0, 0, getWidth(), getHeight() );
         * //canvas.drawBitmap( m_imgSelectedBack, src, dst, null );
         * canvas.drawRect( 0, 0, getWidth(), getHeight(), backPaint );
         * int imgWidth = m_imgSelected.getWidth();
         * int width = getWidth();
         * if ( imgWidth > width )
         * {
         * imgWidth = width;
         * }
         * int imgHeight = ( int ) (( float ) imgWidth / ( float ) m_imgSelected.getWidth() * ( float ) m_imgSelected.getHeight());
         * if ( imgHeight > getHeight() )
         * {
         * imgHeight = getHeight();
         * imgWidth = ( int ) (( float ) m_imgSelected.getWidth() / ( float ) m_imgSelected.getHeight() * ( float ) imgHeight);
         * }
         * Rect src1 = new Rect( 0, 0, m_imgSelected.getWidth(), m_imgSelected.getHeight() );
         * Rect dst1 = new Rect((getWidth() / 2) - (imgWidth / 5), (getHeight() / 2) - (imgHeight / 4) - imgHeight / 6, (getWidth() / 2) - (imgWidth / 4)+
         * imgWidth / 2 - Define.DPFromPixel( 5 ), (getHeight() / 2) - (imgHeight / 4) + imgHeight/5);
         * canvas.drawBitmap( m_imgSelected, src1, dst1, null );
         * int nameLeft = (getWidth() / 2) - (( int ) textPaint.measureText( title ) / 2);
         * canvas.drawText( title, nameLeft, (Define.tabHeight / 12), textPaint );
         * textPaint.setColor(0xFFFFFFFF);
         * }
         * else
         * {
         * //Rect src = new Rect( 0, 0, m_imgNormalBack.getWidth(), m_imgNormalBack.getHeight() );
         * //Rect dst = new Rect( 0, 0, getWidth(), getHeight() );
         * //canvas.drawBitmap( m_imgNormalBack, src, dst, null );
         * canvas.drawRect( 0, 0, getWidth(), getHeight(), backPaint );
         * int imgWidth = m_imgNormal.getWidth();
         * int width = getWidth();
         * if ( imgWidth > width )
         * {
         * imgWidth = width;
         * }
         * int imgHeight = ( int ) (( float ) imgWidth / ( float ) m_imgNormal.getWidth() * ( float ) m_imgNormal.getHeight());
         * if ( imgHeight > getHeight() )
         * {
         * imgHeight = getHeight();
         * imgWidth = ( int ) (( float ) m_imgNormal.getWidth() / ( float ) m_imgNormal.getHeight() * ( float ) imgHeight);
         * }
         * Rect src1 = new Rect( 0, 0, m_imgNormal.getWidth(), m_imgNormal.getHeight() );
         * Rect dst1 = new Rect((getWidth() / 2) - (imgWidth / 5), (getHeight() / 2) - (imgHeight / 4) - imgHeight / 6, (getWidth() / 2) - (imgWidth / 4)+
         * imgWidth / 2 - Define.DPFromPixel( 5 ), (getHeight() / 2) - (imgHeight / 4) + imgHeight/5);
         * canvas.drawBitmap( m_imgNormal, src1, dst1, null );
         * int nameLeft = (getWidth() / 2) - (( int ) textPaint.measureText( title ) / 2);
         * canvas.drawText( title, nameLeft, (Define.tabHeight / 12), textPaint );
         * textPaint.setColor(0xFF363F6D);
         * }
         * if ( count > 0 )
         * {
         * String drawString = count + "";
         * float textWidth = countPaint.measureText( drawString );
         * float textHeight = countPaint.getTextSize();
         * float left = getWidth() - textWidth - 20;
         * float top = 10;
         * int remainder = ( int ) (textWidth - 6);
         * canvas.drawCircle( left - (textWidth / 2) + remainder, top + (textHeight / 2) + 14, textWidth + 10, countPaint );
         * canvas.drawText(drawString, left - (textWidth / 2) + remainder - textWidth, top + textHeight + 15, countTextPaint);
         * }
         * }
         */
        private static final String TAG = "/AtSmart/TabButton";

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
