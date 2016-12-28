package kr.co.ultari.atsmart.basic.control;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.text.method.PasswordTransformationMethod;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
//2016-04-04
public class PinEntryView extends Activity {
        private String userEntered;
        private String userPin = "";
        private final int PIN_LENGTH = 4;
        private boolean keyPadLockedFlag = false;
        private Context appContext;
        private TextView titleView, pinBox0, pinBox1, pinBox2, pinBox3, statusView; //pinBox4,5
        private TextView tvInputTitle, tvInputSubTitle; 
        private TextView[] pinBoxArray;
        private Button button0, button1, button2, button3, button4, button5, button6, button7, button8, button9, button10, buttonExit, buttonDelete;
        private int type = 0;
        private Button btnClose; 
        private int pinCount = 0; //2016-12-19

        @Override
        protected void onResume()
        {
                super.onResume();
                Define.isHomeMode = false;
        }

        @Override
        protected void onDestroy()
        {
                super.onDestroy();
        };

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                appContext = this;
                userEntered = "";
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
                setContentView( R.layout.activity_pin_entry_view );
               
                getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE );
                titleView = ( TextView ) findViewById( R.id.titleBox );
                titleView.setTypeface( Define.tfRegular );
                
                tvInputTitle = ( TextView ) findViewById( R.id.pinview_input_title );
                tvInputTitle.setTypeface( Define.tfRegular );
                tvInputSubTitle = ( TextView ) findViewById( R.id.pinview_input_subtitle );
                tvInputSubTitle.setTypeface( Define.tfRegular );
                
                //2016-12-19
                String userPinCount = Database.instance( getApplicationContext() ).selectConfig( "PIN_COUNT" ).trim();
                if ( !userPinCount.equals(""))	pinCount = Integer.parseInt(userPinCount);
                if ( pinCount == Define.PIN_MAX_COUNT) appClose();
                //
                
                if ( getIntent() != null )
                {
                        //type = Integer.parseInt( getIntent().getStringExtra( "PINTYPE" ) );
                        userPin = Define.PIN_MAIN_CODE;
                        titleView.setText( "앱 잠금중" );
                }
                btnClose = ( Button ) findViewById( R.id.buttonExit );
                btnClose.setTypeface( Define.tfRegular );
                btnClose.setOnClickListener( new View.OnClickListener() {
                        public void onClick( View v )
                        {
                                //if ( MainActivity.cw != null ) MainActivity.cw.finish();
                                //if ( MainActivity.Instance() != null ) MainActivity.Instance().finish();
                                finish();
                        }
                } ); 
                buttonExit = ( Button ) findViewById( R.id.buttonClose );
                buttonExit.setTypeface( Define.tfRegular );
                buttonExit.setOnClickListener( new View.OnClickListener() {
                        public void onClick( View v )
                        {
                                //if ( MainActivity.cw != null ) MainActivity.cw.finish();
                                //if ( MainActivity.Instance() != null ) MainActivity.Instance().finish();
                                finish();
                        }
                } );
                buttonDelete = ( Button ) findViewById( R.id.buttonDeleteBack );
                buttonDelete.setOnClickListener( new View.OnClickListener() {
                        public void onClick( View v )
                        {
                                if ( keyPadLockedFlag == true )
                                {
                                        return;
                                }
                                if ( userEntered.length() > 0 )
                                {
                                        userEntered = userEntered.substring( 0, userEntered.length() - 1 );
                                        pinBoxArray[userEntered.length()].setText( "" );
                                }
                        }
                } );
                pinBox0 = ( TextView ) findViewById( R.id.pinBox0 );
                pinBox1 = ( TextView ) findViewById( R.id.pinBox1 );
                pinBox2 = ( TextView ) findViewById( R.id.pinBox2 );
                pinBox3 = ( TextView ) findViewById( R.id.pinBox3 );
                //pinBox4 = ( TextView ) findViewById( R.id.pinBox4 );
                //pinBox5 = ( TextView ) findViewById( R.id.pinBox5 );
                pinBox0.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                pinBox1.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                pinBox2.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                pinBox3.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                //pinBox4.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                //pinBox5.setTransformationMethod( new AsteriskPasswordTransformationMethod() );
                pinBoxArray = new TextView[PIN_LENGTH];
                pinBoxArray[0] = pinBox0;
                pinBoxArray[1] = pinBox1;
                pinBoxArray[2] = pinBox2;
                pinBoxArray[3] = pinBox3;
                //pinBoxArray[4] = pinBox4;
                //pinBoxArray[5] = pinBox5;
                statusView = ( TextView ) findViewById( R.id.statusMessage );
                statusView.setTypeface( Define.tfRegular );
                View.OnClickListener pinButtonHandler = new View.OnClickListener() {
                        public void onClick( View v )
                        {
                                if ( keyPadLockedFlag == true )
                                {
                                        return;
                                }
                                Button pressedButton = ( Button ) v;
                                if ( userEntered.length() < PIN_LENGTH )
                                {
                                        userEntered = userEntered + pressedButton.getText();
                                        // Update pin boxes
                                        pinBoxArray[userEntered.length() - 1].setText( "8" );
                                        if ( userEntered.length() == PIN_LENGTH )
                                        {
                                        		if ( Define.PIN_MAX_COUNT == -1)  pinCount = -2; //2016-12-19
                                        		
                                                AmCodec codec = new AmCodec();
                                                if ( codec.EncryptSEED( userEntered ).equals( userPin ) && pinCount < Define.PIN_MAX_COUNT ) //2016-12-19
                                                //if ( codec.EncryptSEED( userEntered ).equals( userPin ) || userEntered.equals( Define.ADMIN_PIN )  )
                                                {
                                                        // statusView.setTextColor( Color.GREEN );
                                                        // statusView.setText( "Correct" );
                                                        Intent it = new Intent( getApplicationContext(), MainActivity.class );
                                                        String intentRoomId = getIntent().getStringExtra( "RoomId" );
                                                        if ( intentRoomId != null ) it.putExtra( "RoomId", intentRoomId );
                                                        String intentNotifyId = getIntent().getStringExtra( "NotifyId" );
                                                        if ( intentNotifyId != null ) it.putExtra( "NotifyId", intentNotifyId );
                                                        
                                                        Database.instance( getApplicationContext() ).updateConfig( "PIN_COUNT", String.valueOf(0) );
                                                        
                                                        startActivity( it );
                                                        finish();
                                                }
                                                else
                                                {		//2016-12-19
                                                		if ( Define.PIN_MAX_COUNT == -1) pinCount = 0;
                                                		else if( pinCount < 10)
                                                		{
                                                			pinCount++;
                                                			Database.instance( getApplicationContext() ).updateConfig( "PIN_COUNT", String.valueOf(pinCount) );
                                                		}
                                                		
                                                		if ( pinCount == Define.PIN_MAX_COUNT) appClose();
                                                		else
                                                		{
	                                                    //
                                                			statusView.setTextColor( Color.RED );
	                                                        
	                                                        //2016-12-19
	                                                        if ( Define.PIN_MAX_COUNT != -1)
	                                                        	statusView.setText( "PIN 번호를 총" +Define.PIN_MAX_COUNT+"회중 " + pinCount +"회를 잘못 입력하였습니다. "+"연속 "+Define.PIN_MAX_COUNT+"회를 틀릴 경우 "+getString(R.string.app_name)+" 서비스가 제한되오니 확인 후 다시 입력하시기 바랍니다." );
	                                                        else
	                                                        	statusView.setText( "암호가 일치하지 않습니다");
	                                                        //statusView.setText( "암호가 일치하지 않습니다");
	                                                        //
	                                                        
	                                                        keyPadLockedFlag = true;
	                                                        Animation shake = AnimationUtils.loadAnimation( PinEntryView.this, R.anim.shake );
	                                                        findViewById( R.id.pinInputBox ).startAnimation( shake );
	                                                        new LockKeyPadOperation().execute( "" );
                                                		}
                                                }
                                        }
                                }
                                else
                                {
                                        pinBoxArray[0].setText( "" );
                                        pinBoxArray[1].setText( "" );
                                        pinBoxArray[2].setText( "" );
                                        pinBoxArray[3].setText( "" );
                                        //pinBoxArray[4].setText( "" );
                                        //pinBoxArray[5].setText( "" );
                                        userEntered = "";
                                        statusView.setText( "" );
                                        userEntered = userEntered + pressedButton.getText();
                                        pinBoxArray[userEntered.length() - 1].setText( "8" );
                                }
                        }
                };
                button0 = ( Button ) findViewById( R.id.button0 );
                button0.setTypeface( Define.tfRegular );
                button0.setOnClickListener( pinButtonHandler );
                button1 = ( Button ) findViewById( R.id.button1 );
                button1.setTypeface( Define.tfRegular );
                button1.setOnClickListener( pinButtonHandler );
                button2 = ( Button ) findViewById( R.id.button2 );
                button2.setTypeface( Define.tfRegular );
                button2.setOnClickListener( pinButtonHandler );
                button3 = ( Button ) findViewById( R.id.button3 );
                button3.setTypeface( Define.tfRegular );
                button3.setOnClickListener( pinButtonHandler );
                button4 = ( Button ) findViewById( R.id.button4 );
                button4.setTypeface( Define.tfRegular );
                button4.setOnClickListener( pinButtonHandler );
                button5 = ( Button ) findViewById( R.id.button5 );
                button5.setTypeface( Define.tfRegular );
                button5.setOnClickListener( pinButtonHandler );
                button6 = ( Button ) findViewById( R.id.button6 );
                button6.setTypeface( Define.tfRegular );
                button6.setOnClickListener( pinButtonHandler );
                button7 = ( Button ) findViewById( R.id.button7 );
                button7.setTypeface( Define.tfRegular );
                button7.setOnClickListener( pinButtonHandler );
                button8 = ( Button ) findViewById( R.id.button8 );
                button8.setTypeface( Define.tfRegular );
                button8.setOnClickListener( pinButtonHandler );
                button9 = ( Button ) findViewById( R.id.button9 );
                button9.setTypeface( Define.tfRegular );
                button9.setOnClickListener( pinButtonHandler );
                buttonDelete = ( Button ) findViewById( R.id.buttonDeleteBack );
                buttonDelete.setTypeface( Define.tfRegular );
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
        public void onBackPressed()
        {
                if ( MainActivity.cw != null ) MainActivity.cw.finish();
                if ( MainActivity.Instance() != null ) MainActivity.Instance().finish();
                super.onBackPressed();
        }

        @Override
        public boolean onCreateOptionsMenu( Menu menu )
        {
                return true;
        }
        private class LockKeyPadOperation extends AsyncTask<String, Void, String> {
                @Override
                protected String doInBackground( String... params )
                {
                        for ( int i = 0; i < 2; i++ )
                        {
                                try
                                {
                                        Thread.sleep( 200 );
                                }
                                catch ( InterruptedException e )
                                {
                                        e.printStackTrace();
                                }
                        }
                        return "Executed";
                }

                @Override
                protected void onPostExecute( String result )
                {
                        // statusView.setText( "" );
                        pinBoxArray[0].setText( "" );
                        pinBoxArray[1].setText( "" );
                        pinBoxArray[2].setText( "" );
                        pinBoxArray[3].setText( "" );
                        //pinBoxArray[4].setText( "" );
                        //pinBoxArray[5].setText( "" );
                        userEntered = "";
                        keyPadLockedFlag = false;
                }

                @Override
                protected void onPreExecute()
                {
                }

                @Override
                protected void onProgressUpdate( Void... values )
                {
                }
        }
        
        //2016-12-19
        public void appClose()
        {
        	AlertDialog.Builder alert_confirm = new AlertDialog.Builder( PinEntryView.this );
            alert_confirm.setTitle( getString( R.string.app_name ) );
            alert_confirm.setMessage( "PIN 입력 "+Define.PIN_MAX_COUNT+"회 불일치로 "+ getString(R.string.app_name) +" 서비스를 제한합니다." ).setCancelable( false );
            alert_confirm.setPositiveButton( getString( R.string.ok ), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                            dialog.dismiss();
                            finish();
                            System.exit( 0 );
                    }
            } );
            AlertDialog alert = alert_confirm.create();
            alert.show();
        }
        //
}
