package kr.co.ultari.atsmart.basic.view;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.HorizontalScrollView;

public class TabHorizontalScrollView extends HorizontalScrollView {
        protected final int borderWidth = 0;

        public TabHorizontalScrollView( Context context, AttributeSet attrs, int defStyle )
        {
                super( context, attrs, defStyle );
        }

        public TabHorizontalScrollView( Context context, AttributeSet attrs )
        {
                super( context, attrs );
        }

        public TabHorizontalScrollView( Context context )
        {
                super( context );
        }

        @Override
        public void onDraw( Canvas canvas )
        {
                super.onDraw( canvas );
                int offset = this.computeHorizontalScrollOffset();
                int scrollRange = this.computeHorizontalScrollRange();
                int width = this.getWidth();
                int height = this.getHeight();
                int toend = scrollRange - (width + offset);
                Paint backPaint = new Paint();
                backPaint.setColor( 0xFF8d9dd8 );
                
                if ( 40 < offset )
                        MainActivity.Instance().swipe_left.setVisibility( View.VISIBLE );
                else 
                        MainActivity.Instance().swipe_left.setVisibility( View.INVISIBLE );
                
                if ( 40 < toend )
                        MainActivity.Instance().swipe_right.setVisibility( View.VISIBLE );
                else 
                        MainActivity.Instance().swipe_right.setVisibility( View.INVISIBLE );
        }
}
