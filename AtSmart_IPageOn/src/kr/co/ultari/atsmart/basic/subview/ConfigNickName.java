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

public class ConfigNickName extends MessengerActivity implements OnClickListener {
        private EditText et = null;
        private Button btnOk = null;
        private Button btnCancel = null;
        private TextView tvTitle, tvCountNickName = null;
        private RadioButton rBtnInput, rBtnLeftseat, rBtnMeeting;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.config_nick );
                try
                {
                        et = ( EditText ) findViewById( R.id.nickName );
                        et.setText( Define.getMyNickName() );
                
                        InputFilter[] filters = new InputFilter[] { new ByteLengthFilter( 13, "KSC5601" ) };
                        et.setFilters( filters );
                        //2016-04-04
                        /*et.setOnFocusChangeListener( new OnFocusChangeListener() {
                                @Override
                                public void onFocusChange( View view, boolean bFocus )
                                {
                                        if ( !bFocus ) et.setBackgroundResource( R.drawable.img_inputfield_whitepopup_2_normal );
                                        else et.setBackgroundResource( R.drawable.img_inputfield_whitepopup_2_pressed );
                                }
                        } );*/
                        //
                        et.addTextChangedListener( new TextWatcher() {
                                @Override
                                public void onTextChanged( CharSequence arg0, int arg1, int arg2, int arg3 )
                                {
                                        tvCountNickName.setText( Integer.toString( arg0.toString().length() ) + getString( R.string.nickname_length ) );
                                }

                                @Override
                                public void beforeTextChanged( CharSequence s, int start, int count, int after )
                                {
                                }

                                @Override
                                public void afterTextChanged( Editable s )
                                {
                                }
                        } );
                        btnOk = ( Button ) findViewById( R.id.saveNickName );
                        btnCancel = ( Button ) findViewById( R.id.cancelNickName );
                        btnOk.setTypeface( Define.tfBold ); // font
                        btnCancel.setTypeface( Define.tfBold ); // font
                        btnOk.setOnClickListener( this );
                        btnCancel.setOnClickListener( this );
                        tvTitle = ( TextView ) findViewById( R.id.nickname_custom_title );
                        tvTitle.setTypeface( Define.tfBold ); // font
                        tvCountNickName = ( TextView ) findViewById( R.id.countNickName );
                        tvCountNickName.setTypeface( Define.tfRegular ); // font
                        tvCountNickName.setText( Define.getMyNickName().length() + getString( R.string.nickname_length ) );
                        rBtnInput = ( RadioButton ) findViewById( R.id.nick_btn_input );
                        rBtnLeftseat = ( RadioButton ) findViewById( R.id.nick_btn_leftseat );
                        rBtnMeeting = ( RadioButton ) findViewById( R.id.nick_btn_meeting );
                        rBtnInput.setTypeface( Define.tfBold ); // font
                        rBtnLeftseat.setTypeface( Define.tfBold ); // font
                        rBtnMeeting.setTypeface( Define.tfBold ); // font
                        rBtnInput.setOnClickListener( optionOnClickListener );
                        rBtnLeftseat.setOnClickListener( optionOnClickListener );
                        rBtnMeeting.setOnClickListener( optionOnClickListener );
                        rBtnInput.setChecked( true );
                        et.setEnabled( true );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }
        RadioButton.OnClickListener optionOnClickListener = new RadioButton.OnClickListener() {
                public void onClick( View v )
                {
                        if ( rBtnLeftseat.isChecked() )
                        {
                                et.setText( getString( R.string.leftSeat ) );
                                et.setEnabled( false );
                        }
                        else if ( rBtnMeeting.isChecked() )
                        {
                                et.setText( getString( R.string.meeting ) );
                                et.setEnabled( false );
                        }
                        else if ( rBtnInput.isChecked() ) et.setEnabled( true );
                }
        };

        public boolean dispatchKeyEvent( KeyEvent event )
        {
                boolean isResult = false;
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
                // MainActivity.sendNick(et.getText().toString());
                Intent sendIntent = new Intent( Define.MSG_SEND_NICK );
                sendIntent.putExtra( "NICK", et.getText().toString() );
                sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                sendBroadcast( sendIntent );
                Database.instance( this ).updateConfig( "USERNICKNAME", et.getText().toString() );
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