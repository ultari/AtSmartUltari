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

public class ConfigPassword extends MessengerActivity implements OnClickListener 
{
        private static final String TAG = "/AtSmart/ConfigPassword";
        private EditText etOldPassword = null, etNewPassword = null;
        private Button btnOk = null, btnCancel = null;
        private TextView tvTitle = null;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.config_password );
                
                try
                {
                        etOldPassword = ( EditText ) findViewById( R.id.et_oldPassword );
                        etOldPassword.setText( Define.getMyPw( getApplicationContext() ) );
                        
                        etNewPassword = ( EditText ) findViewById( R.id.et_newPassword );
                        etNewPassword.setText( "" );
                
                        InputFilter[] filters = new InputFilter[] { new ByteLengthFilter( 13, "KSC5601" ) };
                        etOldPassword.setFilters( filters );
                        etNewPassword.setFilters( filters );
                        
                        etOldPassword.setOnFocusChangeListener( new OnFocusChangeListener() 
                        {
                                @Override
                                public void onFocusChange( View view, boolean bFocus )
                                {
                                        if ( !bFocus ) etOldPassword.setBackgroundResource( R.drawable.img_inputfield_whitepopup_2_normal );
                                        else etOldPassword.setBackgroundResource( R.drawable.img_inputfield_whitepopup_2_pressed );
                                }
                        } );
                        
                        etNewPassword.setOnFocusChangeListener( new OnFocusChangeListener() 
                        {
                                @Override
                                public void onFocusChange( View view, boolean bFocus )
                                {
                                        if ( !bFocus ) etNewPassword.setBackgroundResource( R.drawable.img_inputfield_whitepopup_2_normal );
                                        else etNewPassword.setBackgroundResource( R.drawable.img_inputfield_whitepopup_2_pressed );
                                }
                        } );
                        
                        btnOk = ( Button ) findViewById( R.id.savePassword );
                        btnCancel = ( Button ) findViewById( R.id.cancelPassword );
                        btnOk.setTypeface( Define.tfBold ); 
                        btnCancel.setTypeface( Define.tfBold );
                        btnOk.setOnClickListener( this );
                        btnCancel.setOnClickListener( this );
                        
                        tvTitle = ( TextView ) findViewById( R.id.password_custom_title );
                        tvTitle.setTypeface( Define.tfBold ); 
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public boolean dispatchKeyEvent( KeyEvent event )
        {
                boolean isResult = false;
                switch ( event.getKeyCode() )
                {
                        case KeyEvent.KEYCODE_ENTER :
                                switch ( event.getAction() )
                                {
                                case KeyEvent.ACTION_DOWN :
                                        
                                        if(etOldPassword != null)
                                        {
                                                etOldPassword.clearFocus();
                                                InputMethodManager imm = ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE );
                                                imm.hideSoftInputFromWindow( etOldPassword.getWindowToken(), 0 );
                                        }
                                        
                                        if(etNewPassword != null)
                                        {
                                                etNewPassword.clearFocus();
                                                InputMethodManager imm = ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE );
                                                imm.hideSoftInputFromWindow( etNewPassword.getWindowToken(), 0 );
                                        }
                                        
                                        break;
                                }
                                isResult = true;
                                break;
                        default :
                                isResult = super.dispatchKeyEvent( event );
                                break;
                }
                return isResult;
        }

        public void hideKeyboard()
        {
                if(etOldPassword != null)
                {
                        etOldPassword.clearFocus();
                        InputMethodManager imm = ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE );
                        imm.hideSoftInputFromWindow( etOldPassword.getWindowToken(), 0 );
                }
                
                if(etNewPassword != null)
                {
                        etNewPassword.clearFocus();
                        InputMethodManager imm = ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE );
                        imm.hideSoftInputFromWindow( etNewPassword.getWindowToken(), 0 );
                }
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
                String pwd = etNewPassword.getText().toString().trim();
                if(pwd.equals( "" )) return;
                
                StringBuffer message = new StringBuffer();
                message.append( "CHANGEPASSWORD" );
                message.append( "\t" );
                message.append( Define.getMyId( getApplicationContext() ) );
                message.append( "\t" );
                message.append( Define.getMyPw( getApplicationContext() ) );
                message.append( "\t" );
                message.append( pwd );
                
                Intent sendIntent = new Intent( Define.MSG_PASSWORD_CHANGE );
                sendIntent.putExtra( "MESSAGE", message.toString() );
                sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                sendBroadcast( sendIntent );
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