package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.view.SearchView;
import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class SearchEdit extends EditText {
        public SearchEdit( Context context, AttributeSet a_attributeSet )
        {
                super( context, a_attributeSet );
        }

        @Override
        public boolean onKeyPreIme( int keyCode, KeyEvent event )
        {
                if ( event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP )
                {
                        if ( SearchView.m_BottomTab != null ) SearchView.m_BottomTab.setVisibility( View.VISIBLE );
                }
                return super.dispatchKeyEvent( event );
        }
        private static final String TAG = "/AtSmart/SearchEdit";

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
