package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class Waiter extends Activity {
        TextView title;
        TextView content;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                try
                {
                        requestWindowFeature( Window.FEATURE_NO_TITLE );
                        // 2015-05-03
                        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN ) getWindow().setFlags( WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                                        WindowManager.LayoutParams.FLAG_BLUR_BEHIND );
                        setContentView( R.layout.new_progress_popup );
                        ActionManager.waiter = Waiter.this;
                        title = ( TextView ) findViewById( R.id.custom_title_sub );
                        title.setTypeface( Define.tfRegular );
                        /*
                         * title = (TextView)findViewById(R.id.custom_title2);
                         * content = (TextView)findViewById(R.id.content2);
                         * title.setText(getIntent().getStringExtra("TITLE"));
                         * content.setText(getIntent().getStringExtra("CONTENT"));
                         */
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }
        private static final String TAG = "/AtSmart/Waiter";

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
