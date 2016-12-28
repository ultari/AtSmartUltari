package kr.co.ultari.atsmart.basic.view;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.GcmManager;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.control.PinEntryView;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subview.NameLoginDialog;
import kr.co.ultari.atsmart.basic.subview.PinInputDialog;
import kr.co.ultari.atsmart.basic.util.AppUtil;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

@SuppressLint( { "HandlerLeak", "InflateParams" } )
public class AccountView extends MessengerActivity implements OnClickListener, Runnable, OnEditorActionListener {
        private static final String TAG = "/AtSmart/AccountView2";
        private ImageButton btnConfig;
        private ImageButton btnAccount;
        private ImageButton btnLogin;
        private Button btnRetain;
        private Button btnClose; 
        private TextView btnRetainTitle, tvTitle, tvAccountTitle, tvLoginTitle;
        public EditText idInput;
        public EditText passwordInput;
        private Timer accountTimer;
        public AccountView instance;
        public Thread thread;
        private ImageButton btnSaehaviewer;
        private LinearLayout layoutSaeha;
        private LinearLayout layoutIdFrame, layoutPwFrame;
        private ImageView ivUserIcon, ivPwIcon;
        private TextView tvLoginText;
        private String name = "";
        private String nickName = "";
        private String myPartName = "";
        private String tempMyId = "";
        public boolean m_bRetain;
        Context context = null;
        private LinearLayout layoutLoginMode;
        private RadioButton btId, btName;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                setContentView( R.layout.new_activity_account );
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE ); //2016-12-13
                context = getApplicationContext();
                if ( Database.instance( context ).selectConfig( "RETAIN" ).equals( "N" ) ) m_bRetain = false;
                else m_bRetain = true;
                btnConfig = ( ImageButton ) findViewById( R.id.configButton );
                btnAccount = ( ImageButton ) findViewById( R.id.accountButton );
                btnConfig.setOnClickListener( this );
                btnAccount.setOnClickListener( this );
                btnLogin = ( ImageButton ) findViewById( R.id.login );
                btnLogin.setOnClickListener( this );
                btnRetain = ( Button ) findViewById( R.id.btnRetain );
                btnRetain.setOnClickListener( this );
                
                btnClose = ( Button ) findViewById( R.id.account_close );
                btnClose.setOnClickListener( this );
                
                if ( !m_bRetain ) btnRetain.setBackgroundResource( R.drawable.btn_blackbg_uncheck );
                else btnRetain.setBackgroundResource( R.drawable.btn_blackbg_checked );
                tvLoginTitle = ( TextView ) findViewById( R.id.account_custom_title_sub );
                tvLoginTitle.setTypeface( Define.tfRegular );
                btnRetainTitle = ( TextView ) findViewById( R.id.btnRetainTitle );
                btnRetainTitle.setTypeface( Define.tfRegular );
                btnRetainTitle.setOnClickListener( this );
                idInput = ( EditText ) findViewById( R.id.idInput );
                passwordInput = ( EditText ) findViewById( R.id.passwordInput );
                idInput.setTypeface( Define.tfMedium );
                passwordInput.setTypeface( Define.tfMedium );
                if ( "YES".equals( getString( R.string.UPPERCASEID ) ) ) idInput.setInputType( InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
                                | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS );
                else idInput.setInputType( InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS );
                idInput.setText( Database.instance( context ).selectConfig( "USERID" ) );
                passwordInput.setText( Database.instance( context ).selectConfig( "USERPASSWORD" ) );
                tvTitle = ( TextView ) findViewById( R.id.account_title );
                tvAccountTitle = ( TextView ) findViewById( R.id.account_title_account );
                layoutIdFrame = ( LinearLayout ) findViewById( R.id.account_id_frame );
                ivUserIcon = ( ImageView ) findViewById( R.id.account_user_icon );
                layoutPwFrame = ( LinearLayout ) findViewById( R.id.account_password_frame );
                ivPwIcon = ( ImageView ) findViewById( R.id.account_password_icon );
                passwordInput.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                tvLoginText = ( TextView ) findViewById( R.id.account_logintext );
                tvLoginText.setTypeface( Define.tfRegular );
                idInput.addTextChangedListener( new TextWatcher() {
                        @Override
                        public void onTextChanged( CharSequence arg0, int arg1, int arg2, int arg3 )
                        {
                                if ( !idInput.getText().toString().equals( "" ) && !passwordInput.getText().toString().equals( "" ) )
                                {
                                        btnLogin.setBackgroundResource( R.drawable.btn_update_pressed );
                                        //tvLoginText.setTextColor( Color.rgb( 254, 254, 254 ) );
                                        tvLoginText.setTextColor( Color.rgb( 241, 241, 241 ) ); 
                                }
                                else
                                {
                                        btnLogin.setBackgroundResource( R.drawable.btn_update_normal );
                                        //tvLoginText.setTextColor( Color.rgb( 141, 157, 216 ) );
                                        tvLoginText.setTextColor( Color.rgb( 241, 241, 241 ) ); 
                                }
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
                idInput.setOnFocusChangeListener( new OnFocusChangeListener() {
                        @Override
                        public void onFocusChange( View view, boolean bFocus )
                        {
                                if ( !bFocus )
                                {
                                        layoutIdFrame.setBackgroundResource( R.drawable.img_inputfield_nor );
                                        ivUserIcon.setBackgroundResource( R.drawable.icon_username_nor );
                                }
                                else
                                {
                                        layoutIdFrame.setBackgroundResource( R.drawable.img_inputfield_pressed );
                                        ivUserIcon.setBackgroundResource( R.drawable.icon_username_pressed );
                                }
                        }
                } );
                passwordInput.addTextChangedListener( new TextWatcher() {
                        @Override
                        public void onTextChanged( CharSequence arg0, int arg1, int arg2, int arg3 )
                        {
                                if ( !idInput.getText().toString().equals( "" ) && !passwordInput.getText().toString().equals( "" ) )
                                {
                                        btnLogin.setBackgroundResource( R.drawable.btn_update_pressed );
                                        //tvLoginText.setTextColor( Color.rgb( 254, 254, 254 ) );
                                        tvLoginText.setTextColor( Color.rgb( 241, 241, 241 ) ); 
                                }
                                else
                                {
                                        btnLogin.setBackgroundResource( R.drawable.btn_update_normal );
                                        //tvLoginText.setTextColor( Color.rgb( 141, 157, 216 ) );
                                        tvLoginText.setTextColor( Color.rgb( 241, 241, 241 ) ); 
                                }
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
                passwordInput.setOnFocusChangeListener( new OnFocusChangeListener() {
                        @Override
                        public void onFocusChange( View view, boolean bFocus )
                        {
                                if ( !bFocus )
                                {
                                        layoutPwFrame.setBackgroundResource( R.drawable.img_inputfield_nor );
                                        ivPwIcon.setBackgroundResource( R.drawable.icon_password_nor );
                                }
                                else
                                {
                                        layoutPwFrame.setBackgroundResource( R.drawable.img_inputfield_pressed );
                                        ivPwIcon.setBackgroundResource( R.drawable.icon_password_pressed );
                                }
                        }
                } );
                idInput.setOnEditorActionListener( this );
                passwordInput.setOnEditorActionListener( this );
                if ( (Define.selectedFontSize).equals( "" ) )
                {
                        tvTitle.setTextSize( Define.spToPixels( Float.valueOf( 10 ) ) / 2 );
                        tvAccountTitle.setTextSize( Define.spToPixels( Float.valueOf( 10 ) ) / 2 );
                }
                else
                {
                        tvTitle.setTextSize( Define.spToPixels( Float.valueOf( Define.selectedFontSize ) ) / 2 );
                        tvAccountTitle.setTextSize( Define.spToPixels( Float.valueOf( Define.selectedFontSize ) ) / 2 );
                }
                RadioButton.OnClickListener optionOnClickListener = new RadioButton.OnClickListener() {
                        public void onClick( View v )
                        {
                                if ( btId.isChecked() )
                                {
                                        Database.instance( context ).updateConfig( "LOGINMODE", "ID" );
                                        Define.LOGIN_MODE = "ID";
                                }
                                else
                                {
                                        Database.instance( context ).updateConfig( "LOGINMODE", "NAME" );
                                        Define.LOGIN_MODE = "NAME";
                                }
                        }
                };
                btId = ( RadioButton ) findViewById( R.id.btnLoginId );
                btName = ( RadioButton ) findViewById( R.id.btnLoginName );
                btId.setOnClickListener( optionOnClickListener );
                btName.setOnClickListener( optionOnClickListener );
                Define.LOGIN_MODE = Database.instance( context ).selectConfig( "LOGINMODE" );
                if ( Define.LOGIN_MODE.equals( "" ) || Define.LOGIN_MODE == null || Define.LOGIN_MODE.equals( "ID" ) ) Define.LOGIN_MODE = "ID";
                else Define.LOGIN_MODE = "NAME";
                if ( Define.LOGIN_MODE.equals( "ID" ) )
                {
                        btId.setChecked( true );
                        btName.setChecked( false );
                }
                else
                {
                        btId.setChecked( false );
                        btName.setChecked( true );
                }
                layoutLoginMode = ( LinearLayout ) findViewById( R.id.btnLoginMode );
                if ( Define.useLoginMode ) layoutLoginMode.setVisibility( View.VISIBLE );
                else layoutLoginMode.setVisibility( View.GONE );
                layoutSaeha = ( LinearLayout ) findViewById( R.id.account_saeha_layout );
                btnSaehaviewer = ( ImageButton ) findViewById( R.id.account_saehaButton );
                btnSaehaviewer.setOnClickListener( this );
                switch ( Define.SET_COMPANY )
                {
                case Define.SAEHA :
                        btnSaehaviewer.setVisibility( View.VISIBLE );
                        break;
                default :
                        layoutSaeha.setVisibility( View.GONE );
                        break;
                }
                if ( accountTimer == null )
                {
                        accountTimer = new Timer();
                        accountTimer.schedule( accountTimerTask, 1000 );
                }
                
                //2016-12-26
                if ( Define.getMyId( context ).equals( "" ) )	btnClose.setVisibility(View.GONE); 
                else btnClose.setVisibility(View.VISIBLE);
                //
        }

        /*
         * public void imoButtonClick(boolean bFocus, View v) { int start =
         * passwordInput.getSelectionStart(); passwordInput.append("*"); int end
         * = passwordInput.getSelectionEnd();
         * Spannable span = passwordInput.getText(); Bitmap bm = null;
         * if(bFocus) BitmapFactory.decodeResource(getResources(),
         * R.drawable.img_inputfield_password_text_pressed); else
         * BitmapFactory.decodeResource(getResources(),
         * R.drawable.img_inputfield_password_text_noraml);
         * span.setSpan(new ImageSpan(bm), start, end,
         * Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); passwordInput.setText(span); }
         */
        public boolean dispatchKeyEvent( KeyEvent event )
        {
                boolean isResult = false;
                switch ( event.getKeyCode() )
                {
                case KeyEvent.KEYCODE_ENTER :
                        switch ( event.getAction() )
                        {
                        case KeyEvent.ACTION_DOWN :
                                idInput.clearFocus();
                                passwordInput.clearFocus();
                                InputMethodManager imm = ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE );
                                imm.hideSoftInputFromWindow( idInput.getWindowToken(), 0 );
                                imm.hideSoftInputFromWindow( passwordInput.getWindowToken(), 0 );
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

        @Override
        public void onDestroy()
        {
                btnConfig.setImageBitmap( null );
                btnAccount.setImageBitmap( null );
                btnLogin.setImageBitmap( null );
                super.onDestroy();
        }

        public void hideKeyboard()
        {
                /*
                 * if(idInput != null) { InputMethodManager imm = (
                 * InputMethodManager ) context.getSystemService(
                 * Activity.INPUT_METHOD_SERVICE ); imm.hideSoftInputFromWindow(
                 * idInput.getWindowToken(), 0 ); }
                 * if(passwordInput != null) { InputMethodManager imm = (
                 * InputMethodManager ) context.getSystemService(
                 * Activity.INPUT_METHOD_SERVICE ); imm.hideSoftInputFromWindow(
                 * passwordInput.getWindowToken(), 0 ); }
                 */
        }

        @Override
        public void onResume()
        {
                super.onResume();
                Log.d( TAG, TAG );
                resetData();
        }

        private void resetData()
        {
                idInput.setText( Database.instance( context ).selectConfig( "USERID" ) );
                passwordInput.setText( Database.instance( context ).selectConfig( "USERPASSWORD" ) );
                if ( Define.LOGIN_MODE.equals( "ID" ) || Define.LOGIN_MODE.equals( "" ) )
                {
                        btId.setChecked( true );
                        btName.setChecked( false );
                }
                else
                {
                        btId.setChecked( false );
                        btName.setChecked( true );
                }
                tvTitle.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 14 );
                tvAccountTitle.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 14 );
                
             	//2016-12-26
                if ( Define.getMyId( context ).equals( "" ) )	btnClose.setVisibility(View.GONE); 
                else btnClose.setVisibility(View.VISIBLE);
                //
        }
        TimerTask accountTimerTask = new TimerTask() {
                public void run()
                {
                        Log.v( TAG, "accountTimerTask run()" );
                        hideKeyboard();
                }
        };

        @SuppressLint( "DefaultLocale" )
        public void onClick( View view )
        {
                if ( view == btnLogin )
                {
                        hideKeyboard();
                        switch ( Define.SET_COMPANY )
                        {
                                case Define.REDCROSS :
                                        idInput.setText( idInput.getText().toString().toUpperCase() );
                                        break;
                                case Define.AMOTECH:
                                        idInput.setText( idInput.getText().toString().toLowerCase() );
                                        break;
                        default :
                                break;
                        }
                        ActionManager.showProcessingDialog( AccountView.this, getString( R.string.login ), getString( R.string.waitLogin ) );
                        thread = new Thread( this );
                        thread.start();
                }
                else if ( view == btnSaehaviewer )
                {
                        Intent intent = new Intent( android.content.Intent.ACTION_VIEW, Uri.parse( "svcviewer://" ) );
                        intent.addCategory( android.content.Intent.CATEGORY_BROWSABLE );
                        PackageManager pm = getApplicationContext().getPackageManager();
                        boolean isInstalled = !pm.queryIntentActivities( intent, PackageManager.MATCH_DEFAULT_ONLY ).isEmpty();
                        try
                        {
                                if ( !isInstalled ) AppUtil.install( AppUtil.MULTIVIEW, getApplicationContext() );
                                else
                                {
                                        Uri uri = Uri.parse( "svcviewer://" );
                                        Intent it = new Intent( Intent.ACTION_VIEW, uri );
                                        it.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                        startActivity( it );
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
                else if ( view == btnRetain || view == btnRetainTitle )
                {
                        onClickRetain();
                }
                else if( view == btnClose )
                {
                        finish();
                }
        }

        public void run()
        {
                Log.v( TAG, "run()" );
                UltariSSLSocket sc = null;
                InputStreamReader ir = null;
                BufferedReader br = null;
                PrintWriter pw = null;
                AmCodec codec = new AmCodec();
                try
                {
                        sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                        .getServerPort( Define.mContext ) ) );
                        ir = new InputStreamReader( sc.getInputStream() );
                        br = new BufferedReader( ir );
                        pw = new PrintWriter( sc.getWriter() );
                        int value = 1;
                        try
                        {
                                if ( Define.useGcmPush )
                                {
                                        if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH )
                                        {
                                        	Define.regid = "NoC2dm";
                                        	TRACE( "GCM regId:" + Define.regid );
                                        }
                                        else
                                        {
                                                if ( Define.regid == null || Define.regid.equals( "" ) )
                                                {
                                                        GcmManager gcm = new GcmManager( Define.mContext );
                                                        Define.regid = gcm.getPhoneRegistrationId();
                                                        TRACE( "GCM regId:" + Define.regid );
                                                }
                                        }
                                }
                                else
                                {
                                        Define.regid = "NoC2dm";
                                        TRACE( "GCM regId:" + Define.regid );
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                                Define.regid = "NoC2dm";
                                TRACE( "GCM regId:" + Define.regid );
                        }
                        String sm = "";
                        String userId = idInput.getText().toString().trim();
                        String userPw = passwordInput.getText().toString().trim();
                        if ( Define.LOGIN_MODE.equals( "ID" ) )
                        {
                                //Log.d( TAG, "Login\t" + userId + "\t" + userPw + "\t" + Define.regid + "\tAndroid\t" + value );
                                if(Define.usePhoneNumberLogin)
                                {
                                        TelephonyManager tm = ( TelephonyManager ) getSystemService( TELEPHONY_SERVICE );
                                        //String imei = tm.getDeviceId();
                                        String deviceNumber = tm.getLine1Number();
                                        sm = codec.EncryptSEED( "Login\t" + userId + "\t" + userPw + "\t" + Define.regid + "\tAndroid\t" + value +"\t" + deviceNumber ) + "\f";
                                }
                                else
                                {
                                        sm = codec.EncryptSEED( "Login\t" + userId + "\t" + userPw + "\t" + Define.regid + "\tAndroid\t" + value ) + "\f";
                                }
                        }
                        else sm = codec.EncryptSEED( "NameLogin\t" + userId + "\t" + userPw + "\t" + Define.regid + "\tAndroid\t" + value ) + "\f";
                        pw.print( sm );
                        pw.flush();
                        char[] buf = new char[2048];
                        int rcv = 0;
                        String sb;
                        while ( (rcv = br.read( buf, 0, 2048 )) >= 0 )
                        {
                                sb = new String( buf, 0, rcv );
                                if ( sb.indexOf( "\f" ) >= 0 )
                                {
                                        sb = sb.substring( 0, sb.length() - 1 );
                                        String rcvStr = codec.DecryptSEED( sb );
                                        Log.d( TAG, rcvStr );
                                        if ( rcvStr.indexOf( "Login\t" ) == 0 )
                                        {
                                                tempMyId = rcvStr.substring( 6 ).trim();
                                                pw.print( codec.EncryptSEED( "Login\t" + tempMyId + "\t" + passwordInput.getText() + "\t" + Define.regid
                                                                + "\tAndroid\t" + value )
                                                                + "\f" );
                                                pw.flush();
                                        }
                                        else if ( rcvStr.indexOf( "MultiUser\t" ) == 0 )
                                        {
                                                String userNames = rcvStr.substring( 10 ).trim();
                                                String pass = passwordInput.getText().toString();
                                                Intent intent = new Intent( context, NameLoginDialog.class );
                                                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                                intent.putExtra( "pass", pass );
                                                intent.putExtra( "regid", Define.regid );
                                                intent.putExtra( "value", Integer.toString( value ) );
                                                intent.putExtra( "name", userNames );
                                                getApplicationContext().startActivity( intent );
                                                Message m = accountHandler.obtainMessage( Define.AM_MULTIUSER, null );
                                                accountHandler.sendMessage( m );
                                                break;
                                        }
                                        else if ( rcvStr.indexOf( "NoUser" ) == 0 )
                                        {
                                                Message m = accountHandler.obtainMessage( Define.AM_NO_USER, null );
                                                accountHandler.sendMessage( m );
                                                break;
                                        }
                                        else if ( rcvStr.indexOf( "NoPassword" ) == 0 )
                                        {
                                                Message m = accountHandler.obtainMessage( Define.AM_NO_PASSWORD, null );
                                                accountHandler.sendMessage( m );
                                                break;
                                        }
                                        /*
                                         * else if ( rcvStr.indexOf( "BlockUser"
                                         * ) == 0 ) { Message m =
                                         * accountHandler.obtainMessage(
                                         * Define.AM_BLOCK_USER, null );
                                         * accountHandler.sendMessage( m );
                                         * break; }
                                         */
                                        else if ( rcvStr.indexOf( "Passport" ) == 0 )
                                        {
                                                Log.e( "Account", "Passport rcvStr:"+rcvStr );
                                                rcvStr = rcvStr.substring( 9 );
                                                if ( rcvStr.indexOf( "\t" ) < 0 )
                                                {
                                                        name = rcvStr;
                                                        nickName = "";
                                                }
                                                else
                                                {
                                                        name = rcvStr.substring( 0, rcvStr.indexOf( '\t' ) );
                                                        nickName = rcvStr.substring( rcvStr.indexOf( '\t' ) + 1 );
                                                        String[] parse = name.split( "#" );
                                                        myPartName = parse[2];
                                                }
                                                Message m = accountHandler.obtainMessage( Define.AM_LOGIN, null );
                                                accountHandler.sendMessage( m );
                                                if ( Define.LOGIN_MODE.equals( "ID" ) ) Database.instance( context ).updateConfig( "USERID", userId );
                                                else Database.instance( context ).updateConfig( "USERID", tempMyId );
                                                Database.instance( context ).updateConfig( "USERPASSWORD", passwordInput.getText().toString() );
                                                Database.instance( context ).updateConfig( "USERNAME", StringUtil.getNamePosition( name ) );
                                                Database.instance( context ).updateConfig( "USERNICKNAME", nickName );
                                                Database.instance( context ).updateConfig( "MYPARTNAME", myPartName );
                                                if ( Define.LOGIN_MODE.equals( "ID" ) ) Define.setMyId( userId );
                                                else Define.setMyId( tempMyId );
                                                Define.setMyPW( passwordInput.getText().toString() );
                                                Intent sendIntent = new Intent( Define.MSG_RESTART_SERVICE );
                                                sendIntent.putExtra( "USERID", userId.toString() );
                                                sendIntent.putExtra( "MUST_RESTART", "Y" );
                                                sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                                                sendBroadcast( sendIntent );
                                                Message mm = ConfigView.instance().configHandler.obtainMessage( Define.AM_REFRESH );
                                                ConfigView.instance().configHandler.sendMessage( mm );
                                                break;
                                        }
                                }
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( sc != null )
                        {
                                try
                                {
                                        sc.close();
                                        sc = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                        if ( ir != null )
                        {
                                try
                                {
                                        ir.close();
                                        ir = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                        if ( br != null )
                        {
                                try
                                {
                                        br.close();
                                        br = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                        if ( pw != null )
                        {
                                try
                                {
                                        pw.close();
                                        pw = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                }
        }

        public String getToUpperCaseId( String id )
        {
                return id.toUpperCase().trim();
        }

        public String getToLowerCaseId( String id )
        {
                return id.toLowerCase().trim();
        }
        public Handler accountHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                hideKeyboard();
                                if ( msg.what == Define.AM_NO_USER )
                                {
                                        ActionManager.hideProgressDialog();
                                        ActionManager.alert( AccountView.this, getString( R.string.noUser ) );
                                }
                                else if ( msg.what == Define.AM_NO_PASSWORD )
                                {
                                        ActionManager.hideProgressDialog();
                                        ActionManager.alert( AccountView.this, getString( R.string.noPassword ) );
                                }
                                else if ( msg.what == Define.AM_MULTIUSER )
                                {
                                        ActionManager.hideProgressDialog();
                                }
                                else if ( msg.what == Define.AM_HIDE_PROGRESS )
                                {
                                        ActionManager.hideProgressDialog();
                                }
                                else if ( msg.what == Define.AM_LOGIN )
                                {
                                        Thread.sleep( 200 );
                                        accountHandler.sendEmptyMessageDelayed( Define.AM_HIDE_PROGRESS, 1000 );
                                        Message m = MainActivity.mainHandler.obtainMessage( Define.AM_SELECT_TAB );
                                        m.arg1 = Define.TAB_KEYPAD;
                                        MainActivity.mainHandler.sendMessage( m );
                                        ActionManager.tabs.moveKeypad();
                                        if ( !m_bRetain )
                                        {
                                                Database.instance( context ).updateConfig( "RETAIN", "N" );
                                                btnRetain.setBackgroundResource( R.drawable.btn_blackbg_uncheck );
                                        }
                                        else
                                        {
                                                Database.instance( context ).updateConfig( "RETAIN", "Y" );
                                                btnRetain.setBackgroundResource( R.drawable.btn_blackbg_checked );
                                        }
                                        BuddyView.instance().reloadBuddy();
                                        startServiceMobile( idInput.getText().toString().trim(), passwordInput.getText().toString().trim() );
                                        
                                        OrganizationView.instance().startProcess();
                                        
                                        //2016-04-26
                                        switch(Define.SET_COMPANY)
                                        {
                                                case Define.IPAGEON:
                                                        if ( Define.USE_PIN_MAIN )
                                                        {
                                                            Intent pinIt = new Intent( getApplicationContext(), PinEntryView.class );
                                                            String intentRoomId = getIntent().getStringExtra( "RoomId" );
                                                            if ( intentRoomId != null )
                                                            {
                                                                pinIt.putExtra( "RoomId", intentRoomId );
                                                                Define.isAddUserMode = false;
                                                                if ( MainActivity.alert != null ) MainActivity.alert.finish();
                                                                if ( MainActivity.search != null ) MainActivity.search.finish();
                                                            }
                                                            String intentNotifyId = getIntent().getStringExtra( "NotifyId" );
                                                            if ( intentNotifyId != null ) pinIt.putExtra( "NotifyId", intentNotifyId );
                                                            startActivity( pinIt );
                                                        }
                                                        else
                                                        {
                                                            Intent selectWindow = new Intent( AccountView.this,PinInputDialog.class );
                                                            selectWindow.putExtra( "ID", idInput.getText().toString().trim() );
                                                            selectWindow.putExtra( "PW", passwordInput.getText().toString().trim() );
                                                            startActivity( selectWindow );
                                                        }
                                                        break;
                                                
                                                default:
                                                        break;
                                        }
                                        //
                                        
                                        finish();
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        };

        public void startServiceMobile( String _id, String _pw )
        {
                try
                {
                        Database.instance( MainActivity.context ).updateConfig( "USERID", _id );
                        Database.instance( MainActivity.context ).updateConfig( "USERPASSWORD", _pw );
                        Database.instance( MainActivity.context ).updateConfig( "SHOWUSERNAME", _id );
                        Intent sendIntent = new Intent( Define.MSG_RESTART_SERVICE );
                        sendIntent.putExtra( "USERID", _id.toString() );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        context.sendBroadcast( sendIntent );
                }
                catch ( Exception e )
                {
                        MainActivity.Instance().EXCEPTION( e );
                }
        }

        public void onClickRetain()
        {
                m_bRetain = !m_bRetain;
                if ( m_bRetain )
                {
                        btnRetain.setBackgroundResource( R.drawable.btn_blackbg_checked );
                        Database.instance( context ).updateConfig( "RETAIN", "Y" );
                }
                else
                {
                        btnRetain.setBackgroundResource( R.drawable.btn_blackbg_uncheck );
                        Database.instance( context ).updateConfig( "RETAIN", "N" );
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

        @Override
        public boolean onKeyDown( int keyCode, KeyEvent event )
        {
                if ( event.getAction() == KeyEvent.ACTION_DOWN )
                {
                        if ( keyCode == KeyEvent.KEYCODE_BACK )
                        {
                                finish();
                                if ( Define.getMyId( getApplicationContext() ).equals( "" ) ) MainActivity.Instance().finish();
                        }
                }
                return super.onKeyDown( keyCode, event );
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
        public boolean onEditorAction( TextView view, int actionId, KeyEvent arg2 )
        {
                if ( actionId == EditorInfo.IME_ACTION_NEXT )
                {
                        if ( view == idInput )
                        {
                                passwordInput.requestFocus();
                        }
                        else if ( view == passwordInput )
                        {
                                btnLogin.requestFocus();
                        }
                }
                return false;
        };
}
