package kr.co.ultari.atsmart.basic.view;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class LogoDialog extends Dialog {
        private static final String TAG = "/AtSmart/LogoDialog";
        private LinearLayout l = null;
        private Context context;

        public LogoDialog( Context ct, int titleID, int messageID )
        {
                super( ct, R.drawable.dialog_style );
                context = ct;
                try
                {
                        this.setContentView( R.layout.activity_logo );
                        l = ( LinearLayout ) findViewById( R.id.logoBack );
                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) l.setBackground( new BitmapDrawable( context.getResources(),
                                        BitmapFactory.decodeResource( context.getResources(), R.drawable.splash ) ) );
                        else l.setBackgroundDrawable( new BitmapDrawable( context.getResources(), BitmapFactory.decodeResource( context.getResources(),
                                        R.drawable.splash ) ) );
                        Handler handler = new Handler() {
                                public void handleMessage( Message msg )
                                {
                                        dismiss();
                                }
                        };
                        handler.sendEmptyMessageDelayed( 0, 3000 );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        @Override
        public void onStop()
        {
                recycleView( findViewById( R.id.logoBack ) );
                super.onStop();
        }

        private void recycleView( View view )
        {
                try
                {
                        if ( view != null )
                        {
                                Drawable bg = view.getBackground();
                                if ( bg != null )
                                {
                                        bg.setCallback( null );
                                        (( BitmapDrawable ) bg).getBitmap().recycle();
                                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) view.setBackground( bg );
                                        else view.setBackgroundDrawable( bg );
                                }
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
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