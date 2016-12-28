package kr.co.ultari.atsmart.basic.control;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class UserButton extends View implements OnTouchListener {
        private String userId;
        private String userName;
        private Paint textPaint;
        private Paint borderPaint;
        private Paint xPaint;
        private float heightPixel = 0.0f;
        private float textSize = 0;
        private float horizontalMargin = 0;
        private float verticalMargin = 10;
        private float borderSize = 5.0f;
        private RectF xButtonRect;
        private OnDeleteUserListener listener;
        private boolean isEditable = true;

        public UserButton( Context context, String userId, String userName, int dp )
        {
                super( context );
                this.listener = null;
                heightPixel = PixelFromDP( dp );
                textSize = heightPixel / 2 * 3;
                horizontalMargin = heightPixel / 2 * 3;
                verticalMargin = heightPixel * 2 / 3;
                borderSize = heightPixel / 8;
                this.userId = userId;
                this.userName = userName;
                textPaint = new Paint();
                textPaint.setTextSize( textSize );
                textPaint.setTypeface( Typeface.create( Typeface.DEFAULT, Typeface.BOLD ) );
                textPaint.setColor( Color.rgb( 128, 128, 128 ) );
                xPaint = new Paint();
                xPaint.setColor( Color.rgb( 141, 157, 216 ) );
                xPaint.setStrokeWidth( borderSize );
                borderPaint = new Paint();
                borderPaint.setColor( Color.rgb( 141, 157, 216 ) );
                borderPaint.setStrokeWidth( borderSize );
                borderPaint.setAntiAlias( true );
                borderPaint.setDither( true );
                borderPaint.setStyle( Paint.Style.STROKE );
                xButtonRect = new RectF();
                this.setFocusable( true );
        }

        // Public
        public void setEditable( boolean editable )
        {
                isEditable = editable;
                if ( !isEditable )
                {
                        textPaint.setColor( Color.WHITE );
                        borderPaint.setStyle( Paint.Style.FILL );
                        xPaint.setColor( Color.WHITE );
                }
                else
                {
                        textPaint.setColor( Color.rgb( 128, 128, 128 ) );
                        borderPaint.setStyle( Paint.Style.STROKE );
                        xPaint.setColor( Color.rgb( 141, 157, 216 ) );
                }
                invalidate();
        }

        public boolean isEditable()
        {
                return isEditable;
        }

        public void setOnDeleteUserListener( OnDeleteUserListener listener )
        {
                this.listener = listener;
                setOnTouchListener( this );
        }

        public String getUserId()
        {
                return userId;
        }

        public String getUserName()
        {
                return userName;
        }

        public float getItemWidth()
        {
                DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
                float totalWidth = displaymetrics.widthPixels - ( float ) (heightPixel * 1.7) - horizontalMargin;
                float itemWidth = (horizontalMargin * 2) + measureWidth() + (textSize / 2);
                if ( itemWidth > totalWidth )
                {
                        return totalWidth;
                }
                else
                {
                        return itemWidth + horizontalMargin;
                }
        }

        public float getItemHeight()
        {
                return (verticalMargin * 2) + measureHeight() + (borderSize * 2) + verticalMargin;
        }

        // Override
        @Override
        protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec )
        {
                setMeasuredDimension( ( int ) getItemWidth(), ( int ) getItemHeight() );
        }

        @Override
        protected void onDraw( Canvas canvas )
        {
                float maxWidth = canvas.getWidth() - textSize;
                float screenWidth = getResources().getDisplayMetrics().widthPixels - (textSize * 5 / 2);
                if ( canvas.getWidth() > screenWidth )
                {
                        canvas.drawRoundRect(
                                        new RectF( (horizontalMargin / 2) + borderSize, (verticalMargin / 2) + borderSize, screenWidth, canvas.getHeight()
                                                        - borderSize - (verticalMargin / 2) ), heightPixel, heightPixel, borderPaint );
                        maxWidth = screenWidth;
                }
                else
                {
                        canvas.drawRoundRect( new RectF( (horizontalMargin / 2) + borderSize, (verticalMargin / 2) + borderSize,
                                        canvas.getWidth() - borderSize, canvas.getHeight() - borderSize - (verticalMargin / 2) ), heightPixel, heightPixel,
                                        borderPaint );
                        maxWidth = canvas.getWidth() - borderSize;
                }
                float xSize = textSize * 2 / 3;
                xButtonRect.left = maxWidth - heightPixel - xSize;
                xButtonRect.top = (canvas.getHeight() / 2) - (xSize / 2);
                xButtonRect.right = xButtonRect.left + xSize;
                xButtonRect.bottom = xButtonRect.top + xSize;
                canvas.drawLine( xButtonRect.left, xButtonRect.top, xButtonRect.right, xButtonRect.bottom, xPaint );
                canvas.drawLine( xButtonRect.right, xButtonRect.top, xButtonRect.left, xButtonRect.bottom, xPaint );
                String viewName = getText( maxWidth - textSize );
                canvas.drawText( viewName, heightPixel + (horizontalMargin / 2), (verticalMargin / 2) + verticalMargin + measureHeight() - borderSize,
                                textPaint );
        }

        @Override
        public boolean onTouch( View v, MotionEvent event )
        {
                if ( event.getAction() == MotionEvent.ACTION_UP )
                {
                        if ( !isEditable ) return true;
                        changePushColor( false );
                        return true;
                }
                if ( event.getAction() == MotionEvent.ACTION_DOWN )
                {
                        changePushColor( true );
                        if ( !isEditable ) return true;
                        RectF largeRect = new RectF( xButtonRect.left - 10, xButtonRect.top - 10, xButtonRect.right + 10, xButtonRect.bottom + 10 );
                        if ( largeRect.contains( event.getX(), event.getY() ) )
                        {
                                Log.d( "UserButton", "DeleteUser : " + userId );
                                listener.onDeleteUser( this, userId );
                        }
                        return true;
                }
                return true;
        }

        // Private
        private void changePushColor( boolean isDown )
        {
                if ( isDown )
                {
                        textPaint.setColor( Color.WHITE );
                        borderPaint.setStyle( Paint.Style.FILL );
                        xPaint.setColor( Color.WHITE );
                }
                else
                {
                        textPaint.setColor( Color.rgb( 128, 128, 128 ) );
                        borderPaint.setStyle( Paint.Style.STROKE );
                        xPaint.setColor( Color.rgb( 141, 157, 216 ) );
                }
                invalidate();
        }

        private String getText( float maxWidth )
        {
                if ( measureWidth() > maxWidth )
                {
                        for ( int i = 0; i < userName.length(); i++ )
                        {
                                String oldName = userName.substring( 0, i ) + "...";
                                String newName = userName.substring( 0, i + 1 ) + "...";
                                if ( (measureWidth( newName ) + horizontalMargin) > (maxWidth - textSize) )
                                {
                                        return oldName;
                                }
                        }
                        return "...";
                }
                else
                {
                        return userName;
                }
        }

        private float measureWidth( String str )
        {
                return textPaint.measureText( str );
        }

        private float measureWidth()
        {
                return textPaint.measureText( userName );
        }

        private float measureHeight()
        {
                Rect rect = new Rect();
                textPaint.getTextBounds( userName, 0, userName.length(), rect );
                return rect.height();
        }

        private float PixelFromDP( int dp )
        {
                return dp * this.getContext().getResources().getDisplayMetrics().density;
        }
        public interface OnDeleteUserListener {
                public void onDeleteUser( View view, String userId );
        }
}
