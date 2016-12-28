package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.util.ByteLengthFilter;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class ConfigCallSwitch extends MessengerActivity implements OnClickListener {
        private EditText et = null;
        private Button btnOk = null;
        private Button btnCancel = null;
        private TextView tvTitle = null;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.config_callswitch );
                try
                {
                        et = ( EditText ) findViewById( R.id.callSwitch );
                        String callSwitch = Database.instance( getApplicationContext() ).selectConfig("callSwitch");
                        if ( !callSwitch.equals("")) et.setText( callSwitch );
                
                        InputFilter[] filters = new InputFilter[] { new ByteLengthFilter( 11, "KSC5601" ) };
                        et.setFilters( filters );

                        btnOk = ( Button ) findViewById( R.id.saveCallSwitch );
                        btnCancel = ( Button ) findViewById( R.id.cancelCallSwitch );
                        btnOk.setTypeface( Define.tfBold ); // font
                        btnCancel.setTypeface( Define.tfBold ); // font
                        btnOk.setOnClickListener( this );
                        btnCancel.setOnClickListener( this );
                        tvTitle = ( TextView ) findViewById( R.id.callSwitch_custom_title );
                        tvTitle.setTypeface( Define.tfBold ); // font
                        et.setEnabled( true );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public boolean dispatchKeyEvent( KeyEvent event )
        {
                boolean isResult = false;
               
                try{
	                switch ( event.getKeyCode() )
	                {
	                case KeyEvent.KEYCODE_ENTER :
	                        switch ( event.getAction() )
	                        {
	                        case KeyEvent.ACTION_DOWN :
	                                et.clearFocus();
	                                InputMethodManager imm = ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE );
	                                imm.hideSoftInputFromWindow( et.getWindowToken(), 0 );
	                                break;
	                        }
	                        isResult = true;
	                        break;
	                default :
	                        isResult = super.dispatchKeyEvent( event );
	                        break;
	                }
                }catch(Exception e){e.printStackTrace();}
                return isResult;
        }

        public void hideKeyboard()
        {
                InputMethodManager imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                imm.hideSoftInputFromWindow( et.getWindowToken(), 0 );
        }

        public void onClick( View view )
        {
                this.hideKeyboard();
                if ( btnOk == view )
                {
                        setData();
                        finish();
                }
                else if ( btnCancel == view )
                {
                        finish();
                }
        }

        public void setData()
        {
        	Database.instance( getApplicationContext() ).updateConfig("callSwitch", et.getText().toString());
        }
        private static final String TAG = "/AtSmart/ConfigNickName";

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