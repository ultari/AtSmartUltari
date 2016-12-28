package kr.co.ultari.atsmart.basic.subview;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
//2016-04-04
public class PinInputDialog extends Activity implements android.view.View.OnClickListener {
        private static final String TAG = "/AtSmart/PinInput";
        private Context context;
        private TextView tvTitle, tvSubTitle, tvSub2Title, tvSubMainTitle;
        private TextView[] box;
        private TextView[] Confirmbox;
        private TextView tvBox0, tvBox1, tvBox2, tvBox3, tvConfirmBox0, tvConfirmBox1, tvConfirmBox2, tvConfirmBox3; //tvBox4, tvBox5, tvConfirmBox4, tvConfirmBox5
        private EditText etInput, etReconfirm;
        private Button btnCancel, btnContinue;
        private LinearLayout layout_reconfirm;
        private boolean isSave = false;
        private String encResult = "";

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                context = getApplicationContext();
                setContentView( R.layout.pin_input );
                
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE );
                getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN );
                
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                
                tvBox0 = ( TextView ) findViewById( R.id.pin_pinBox0 );
                tvBox1 = ( TextView ) findViewById( R.id.pin_pinBox1 );
                tvBox2 = ( TextView ) findViewById( R.id.pin_pinBox2 );
                tvBox3 = ( TextView ) findViewById( R.id.pin_pinBox3 );
                //tvBox4 = ( TextView ) findViewById( R.id.pin_pinBox4 );
                //tvBox5 = ( TextView ) findViewById( R.id.pin_pinBox5 );
                tvBox0.setTypeface( Define.tfRegular );
                tvBox1.setTypeface( Define.tfRegular );
                tvBox2.setTypeface( Define.tfRegular );
                tvBox3.setTypeface( Define.tfRegular );
                //tvBox4.setTypeface( Define.tfRegular );
                //tvBox5.setTypeface( Define.tfRegular );
                tvBox0.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                tvBox1.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                tvBox2.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                tvBox3.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                //tvBox4.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                //tvBox5.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                box = new TextView[4];
                box[0] = tvBox0;
                box[1] = tvBox1;
                box[2] = tvBox2;
                box[3] = tvBox3;
                //box[4] = tvBox4;
                //box[5] = tvBox5;
                tvConfirmBox0 = ( TextView ) findViewById( R.id.pin_reconfirm_pinBox0 );
                tvConfirmBox1 = ( TextView ) findViewById( R.id.pin_reconfirm_pinBox1 );
                tvConfirmBox2 = ( TextView ) findViewById( R.id.pin_reconfirm_pinBox2 );
                tvConfirmBox3 = ( TextView ) findViewById( R.id.pin_reconfirm_pinBox3 );
                //tvConfirmBox4 = ( TextView ) findViewById( R.id.pin_reconfirm_pinBox4 );
                //tvConfirmBox5 = ( TextView ) findViewById( R.id.pin_reconfirm_pinBox5 );
                tvConfirmBox0.setTypeface( Define.tfRegular );
                tvConfirmBox1.setTypeface( Define.tfRegular );
                tvConfirmBox2.setTypeface( Define.tfRegular );
                tvConfirmBox3.setTypeface( Define.tfRegular );
                //tvConfirmBox4.setTypeface( Define.tfRegular );
                //tvConfirmBox5.setTypeface( Define.tfRegular );
                tvConfirmBox0.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                tvConfirmBox1.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                tvConfirmBox2.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                tvConfirmBox3.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                //tvConfirmBox4.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                //tvConfirmBox5.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                Confirmbox = new TextView[4];
                Confirmbox[0] = tvConfirmBox0;
                Confirmbox[1] = tvConfirmBox1;
                Confirmbox[2] = tvConfirmBox2;
                Confirmbox[3] = tvConfirmBox3;
                //Confirmbox[4] = tvConfirmBox4;
                //Confirmbox[5] = tvConfirmBox5;
                tvTitle = ( TextView ) findViewById( R.id.pin_title );
                tvTitle.setTypeface( Define.tfBold );
               
                tvSubMainTitle = ( TextView ) findViewById( R.id.pin_sub_main_title );
                tvSubMainTitle.setTypeface( Define.tfRegular );
                tvSub2Title = ( TextView ) findViewById( R.id.pin_sub2_title );
                tvSub2Title.setTypeface( Define.tfRegular );
                etReconfirm = ( EditText ) findViewById( R.id.pin_reconfirm );
                etReconfirm.setTransformationMethod( new PasswordTransformationMethod() );
                etReconfirm.setOnEditorActionListener( new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
                        {
                                if ( (actionId == EditorInfo.IME_ACTION_DONE) )
                                {
                                        if ( !etInput.getText().toString().equals( "" ) && !etReconfirm.getText().toString().equals( "" ) )
                                        {
                                                String str1 = etInput.getText().toString().trim();
                                                String str2 = etReconfirm.getText().toString().trim();
                                                if ( str1.equals( str2 ) )
                                                {
                                                        AmCodec codec = new AmCodec();
                                                        String ret = codec.EncryptSEED( str1 );
                                                        encResult = ret;
                                                        isSave = true;
                                                        
                                                        Define.USE_PIN_MAIN = true;
                                                        Define.PIN_MAIN_CODE = ret;
                                                        Database.instance( context ).updateConfig( "PIN_MAIN_CODE", encResult );
                                                        Database.instance( getApplicationContext() ).updateConfig( "PIN_MAIN", "ON" );
                                                        Toast.makeText( context, "PIN 설정 완료", Toast.LENGTH_SHORT ).show();
                                                        finish();
                                                }
                                                else Toast.makeText( context, "입력한 PIN 값이 다릅니다", Toast.LENGTH_SHORT ).show();
                                        }
                                }
                                return false;
                        }
                } );
                etReconfirm.addTextChangedListener( new TextWatcher() {
                        @Override
                        public void beforeTextChanged( CharSequence s, int start, int count, int after )
                        {
                        }

                        @Override
                        public void onTextChanged( CharSequence s, int start, int before, int count )
                        {
                                if ( s.length() == 0 )
                                {
                                        for ( int i = 0; i < 4; i++ )
                                                Confirmbox[i].setText( "" );
                                }
                                else if ( s.length() > 0 )
                                {
                                        for ( int i = 0; i < 4; i++ )
                                                Confirmbox[i].setText( "" );
                                        for ( int i = 0; i < s.length(); i++ )
                                                Confirmbox[i].setText( String.valueOf( s.charAt( i ) ) );
                                }
                        }

                        @Override
                        public void afterTextChanged( Editable s )
                        {
                        }
                } );
                layout_reconfirm = ( LinearLayout ) findViewById( R.id.pin_reconfirm_layout );
                layout_reconfirm.setVisibility( View.GONE );
                btnCancel = ( Button ) findViewById( R.id.pin_cancel );
                btnCancel.setOnClickListener( this );
                btnCancel.setTypeface( Define.tfRegular );
                btnContinue = ( Button ) findViewById( R.id.pin_continue );
                btnContinue.setOnClickListener( this );
                btnContinue.setTypeface( Define.tfRegular );
                etInput = ( EditText ) findViewById( R.id.pin_edit );
                etInput.setOnEditorActionListener( new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
                        {
                                if ( actionId == EditorInfo.IME_ACTION_NEXT )
                                {
                                        if ( !etInput.getText().toString().equals( "" ) && etReconfirm.getText().toString().equals( "" ) )
                                        {
                                                layout_reconfirm.setVisibility( View.VISIBLE );
                                                etReconfirm.requestFocus();
                                                etReconfirm.setFocusable( true );
                                                tvSubMainTitle.setText( "완료되면 완료 버튼을 누르세요." );
                                                btnContinue.setText( context.getString( R.string.completion ) );
                                        }
                                }
                                return false;
                        }
                } );
                etInput.addTextChangedListener( new TextWatcher() {
                        @Override
                        public void beforeTextChanged( CharSequence s, int start, int count, int after )
                        {
                        }

                        @Override
                        public void onTextChanged( CharSequence s, int start, int before, int count )
                        {
                                if ( s.length() == 0 )
                                {
                                        tvSubMainTitle.setText( "PIN 입력" );
                                        btnContinue.setEnabled( false );
                                }
                                else if ( s.length() > 0 && s.length() < 4 )
                                {
                                        tvSubMainTitle.setText( "4자리의 숫자를 입력해 주세요." );
                                        btnContinue.setEnabled( false );
                                }
                                else if ( s.length() > 3 )
                                {
                                        tvSubMainTitle.setText( "완료되면 [다음]을 누르세요." );
                                        btnContinue.setEnabled( true );
                                }
                                if ( s.length() == 0 )
                                {
                                        for ( int i = 0; i < 4; i++ )
                                                box[i].setText( "" );
                                }
                                else if ( s.length() > 0 )
                                {
                                        for ( int i = 0; i < 4; i++ )
                                                box[i].setText( "" );
                                        for ( int i = 0; i < s.length(); i++ )
                                                box[i].setText( String.valueOf( s.charAt( i ) ) );
                                }
                        }

                        @Override
                        public void afterTextChanged( Editable s )
                        {
                        }
                } );
                etInput.setTransformationMethod( new PasswordTransformationMethod() );
                etInput.setText( "" );
                etReconfirm.setText( "" );
                etInput.requestFocus();
                Timer timer = new Timer();
                timer.schedule( new TimerTask() {
                        @Override
                        public void run()
                        {
                                InputMethodManager m = ( InputMethodManager ) context.getSystemService( Context.INPUT_METHOD_SERVICE );
                                m.toggleSoftInput( 0, InputMethodManager.HIDE_NOT_ALWAYS );
                        }
                }, 500 );
        }
        public Handler handler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REDRAW )
                                {
                                        InputMethodManager m = ( InputMethodManager ) context.getSystemService( Context.INPUT_METHOD_SERVICE );
                                        m.toggleSoftInput( 0, InputMethodManager.HIDE_NOT_ALWAYS );
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };

        @Override
        public void onStop()
        {
                super.onStop();
        }

        public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
                @Override
                public CharSequence getTransformation( CharSequence source, View view )
                {
                        return new PasswordCharSequence( source );
                }
                private class PasswordCharSequence implements CharSequence {
                        private CharSequence mSource;

                        public PasswordCharSequence( CharSequence source )
                        {
                                mSource = source;
                        }

                        public char charAt( int index )
                        {
                                return '*';
                        }

                        public int length()
                        {
                                return mSource.length();
                        }

                        public CharSequence subSequence( int start, int end )
                        {
                                return mSource.subSequence( start, end );
                        }
                }
        }

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == R.id.pin_cancel )
                {
                        if ( !isSave )
                        {
                                if ( Define.PIN_MAIN_CODE.equals( "" ) ) 
                                        Database.instance( context ).updateConfig( "PIN_MAIN", "OFF" );
                        }
                        finish();
                }
                else if ( v.getId() == R.id.pin_continue )
                {
                        if ( !etInput.getText().toString().equals( "" ) && etReconfirm.getText().toString().equals( "" ) )
                        {
                                layout_reconfirm.setVisibility( View.VISIBLE );
                                etReconfirm.requestFocus();
                                etReconfirm.setFocusable( true );
                                tvSubMainTitle.setText( "완료되면 완료 버튼을 누르세요." );
                                btnContinue.setText( context.getString( R.string.completion ) );
                        }
                        else if ( !etInput.getText().toString().equals( "" ) && !etReconfirm.getText().toString().equals( "" ) )
                        {
                                String str1 = etInput.getText().toString().trim();
                                String str2 = etReconfirm.getText().toString().trim();
                                if ( str1.equals( str2 ) )
                                {
                                        AmCodec codec = new AmCodec();
                                        String ret = codec.EncryptSEED( str1 );
                                        encResult = ret;
                                        isSave = true;
                                        
                                        Define.USE_PIN_MAIN = true;
                                        Define.PIN_MAIN_CODE = ret;
                                        Database.instance( context ).updateConfig( "PIN_MAIN_CODE", encResult );
                                        Toast.makeText( context, "PIN 설정 완료", Toast.LENGTH_SHORT ).show();
                                        Database.instance( getApplicationContext() ).updateConfig( "PIN_MAIN", "ON" );
                                        finish();
                                }
                                else 
                                        Toast.makeText( context, "입력한 PIN 값이 다릅니다", Toast.LENGTH_SHORT ).show();
                        }
                }
        }
}
