package kr.co.ultari.atsmart.ipocall;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.android.internal.telephony.ITelephony;
import com.ipageon.p701.sdk.video.capture.AndroidCameraConfiguration.AndroidCamera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.service.NetworkStatusReceiver;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;

public class CallWindow extends Activity implements OnClickListener, SensorEventListener, OnTouchListener, Runnable
{
	public static final String TAG = "IPageOn";
	
	public static final int TYPE_CONNECT	= 0x00;
	public static final int TYPE_RECEIVE	= 0x01;
	public static final int TYPE_CALL		= 0x02;
	public static final int TYPE_KEYPAD		= 0x03;
	public static final int TYPE_FINISH		= 0x04;
	
	private LinearLayout logo;
	private int nowCallStatus = TYPE_CONNECT;
	
	private BroadcastReceiver eventReceiver = null;
	
	public static CallWindow window = null;
	
	private ImageButton btnCancelCall = null;
	
	private ImageButton btnRejectReceive = null;
	private ImageButton btnAcceptReceive = null;
	
	private ImageButton btnEndCall = null;
	private ImageButton btnEndCallKeypad = null;
	
	private LinearLayout btnSpeaker = null;
	private ImageButton iconSpeaker = null;
	private boolean m_bSpeakerMode = false;
	
	private LinearLayout btnMute = null;
	private ImageButton iconMute = null;
	private boolean m_bMute = false;
	
	private LinearLayout btnKeypad = null;
	private ImageButton iconKeypad = null;
	
	private ImageButton keypad_hide = null;
	
	private LinearLayout btnRecord = null;
	private ImageButton iconRecord = null;
	private boolean m_bRecord = false;
	private ImageView recordState = null;
	
	private LinearLayout btnHold = null;
	private ImageButton iconHold = null;
	private TextView titleHold = null;
	private boolean isHold = false;
	
	private LinearLayout btnSwitch = null;
	private ImageButton iconSwitch = null;
	
	private String otherNumber = null;
	
	private SensorManager mSensorManager;
	private Sensor mProximity;
	
	private Button k0, k1, k2, k3, k4, k5, k6, k7, k8, k9, kt, kh;
	
	private TextView callTimer;
	private int callTimerSecond = 0;
	
	private Button btnLater;
	private Button btnWait;
	private boolean iWantPause = false;
	
	private TimerTask mTask;
	private Timer mTimer;
	
	private String sCallId = null;
	private String sUserId = "";
	private String sName = "";
	private short iCallType;
	private int duration = 0;
	private String sNumber = "";
	private boolean isCallConnected = false;
	private boolean callLogAdded = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		window = this;

		sCallId = StringUtil.getNowDateTime().substring(0, 14);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
		
		setContentView(R.layout.activity_call_window);
		
		btnCancelCall = (ImageButton)findViewById(R.id.call_btn_cancel);
		btnCancelCall.setOnClickListener(this);
		
		btnRejectReceive = (ImageButton)findViewById(R.id.call_btn_cancel_receive);
		btnRejectReceive.setOnClickListener(this);
		
		btnAcceptReceive = (ImageButton)findViewById(R.id.call_btn_accept_receive);
		btnAcceptReceive.setOnClickListener(this);
		
		btnEndCall = (ImageButton)findViewById(R.id.call_btn_endcall_ing);
		btnEndCall.setOnClickListener(this);
		
		btnEndCallKeypad = (ImageButton)findViewById(R.id.call_keypad_endcall);
		btnEndCallKeypad.setOnClickListener(this);
		
		logo = (LinearLayout)findViewById(R.id.call_profile_logo);
		logo.setOnClickListener(this);
		
		btnSpeaker = (LinearLayout)findViewById(R.id.call_btn_speaker);
		iconSpeaker = (ImageButton)findViewById(R.id.call_speaker_icon);
		btnSpeaker.setOnClickListener(this);
		iconSpeaker.setOnClickListener(this);
		
		btnMute = (LinearLayout)findViewById(R.id.call_btn_sound);
		iconMute = (ImageButton)findViewById(R.id.call_sound_icon);
		btnMute.setOnClickListener(this);
		iconMute.setOnClickListener(this);
		
		btnRecord = (LinearLayout)findViewById(R.id.call_btn_record);
		iconRecord = (ImageButton)findViewById(R.id.call_record_icon);
		recordState = (ImageView)findViewById(R.id.call_status_record);
		btnRecord.setOnClickListener(this);
		iconRecord.setOnClickListener(this);
		
		btnKeypad = (LinearLayout)findViewById(R.id.call_btn_keypad);
		iconKeypad = (ImageButton)findViewById(R.id.call_keypad_icon);
		btnKeypad.setOnClickListener(this);
		iconKeypad.setOnClickListener(this);
		
		keypad_hide = (ImageButton)findViewById(R.id.call_keypad_hide);
		keypad_hide.setOnClickListener(this);
		
		k0 = (Button)findViewById(R.id.keypad_0);
		k1 = (Button)findViewById(R.id.keypad_1);
		k2 = (Button)findViewById(R.id.keypad_2);
		k3 = (Button)findViewById(R.id.keypad_3);
		k4 = (Button)findViewById(R.id.keypad_4);
		k5 = (Button)findViewById(R.id.keypad_5);
		k6 = (Button)findViewById(R.id.keypad_6);
		k7 = (Button)findViewById(R.id.keypad_7);
		k8 = (Button)findViewById(R.id.keypad_8);
		k9 = (Button)findViewById(R.id.keypad_9);
		kt = (Button)findViewById(R.id.keypad_star);
		kh = (Button)findViewById(R.id.keypad_sharp);
		
		k0.setOnTouchListener(this);
		k1.setOnTouchListener(this);
		k2.setOnTouchListener(this);
		k3.setOnTouchListener(this);
		k4.setOnTouchListener(this);
		k5.setOnTouchListener(this);
		k6.setOnTouchListener(this);
		k7.setOnTouchListener(this);
		k8.setOnTouchListener(this);
		k9.setOnTouchListener(this);
		kt.setOnTouchListener(this);
		kh.setOnTouchListener(this);
		
		k0.setOnClickListener(this);
		k1.setOnClickListener(this);
		k2.setOnClickListener(this);
		k3.setOnClickListener(this);
		k4.setOnClickListener(this);
		k5.setOnClickListener(this);
		k6.setOnClickListener(this);
		k7.setOnClickListener(this);
		k8.setOnClickListener(this);
		k9.setOnClickListener(this);
		kt.setOnClickListener(this);
		kh.setOnClickListener(this);
		
		btnHold = (LinearLayout)findViewById(R.id.call_btn_hold);
		iconHold = (ImageButton)findViewById(R.id.call_hold_icon);
		btnHold.setOnClickListener(this);
		iconHold.setOnClickListener(this);
		titleHold = (TextView)findViewById(R.id.call_hold_title);
		
		btnWait = (Button)findViewById(R.id.call_btn_wait);
		btnWait.setOnClickListener(this);
		
		btnLater = (Button)findViewById(R.id.call_btn_later);
		btnLater.setOnClickListener(this);
		
		btnSwitch = (LinearLayout)findViewById(R.id.call_btn_switch);
		iconSwitch = (ImageButton)findViewById(R.id.call_switch_icon);
		btnSwitch.setOnClickListener(this);
		iconSwitch.setOnClickListener(this);
		
		eventReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				if ( intent.getAction().equals(Define.IPG_CALL_STATE_CHANGED) )
				{
					String state = intent.getStringExtra("State");
					
					if ( state == null ) return;
					
					if ( state.equals("Connected") )
					{
						Message m = callWindowHandler.obtainMessage(Define.CALL_CONNECTED);
						callWindowHandler.sendMessage(m);
					}
					else if ( state.equals("CallReleased") || state.equals("CallEnd") )
					{
						Message m = callWindowHandler.obtainMessage(Define.CALL_DISCONNECTED);
						callWindowHandler.sendMessage(m);
					}
					else if ( state.equals("CallPaused") )
					{
						isHold = true;
			            
			            btnHold.setSelected(true);
			            titleHold.setText("통화재개");
					}
					else if ( state.equals("PausedByRemote"))
					{
						Define.isCallPaused = true;
					}
				
					else if ( state.equals("IncomingPstnCalls") )
					{
						Log.e(TAG, "incoingCall :"+ nowCallStatus);
						if ( nowCallStatus != TYPE_FINISH )
						{
							try
							{
								TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
								
								Class<?> c = Class.forName(tm.getClass().getName());
								Method m = c.getDeclaredMethod("getITelephony");
								m.setAccessible(true);
								
								ITelephony telephonyService = (ITelephony) m.invoke(tm);
								telephonyService.endCall();
								
								String toNumber = intent.getStringExtra("Number");
								SmsManager mSmsManager = SmsManager.getDefault();
							    mSmsManager.sendTextMessage(toNumber, null, "잠시후 다시 연락 드리겠습니다.", null, null);
							}
							catch(Exception e)
							{
								android.util.Log.e(TAG, "PSTN", e);
							}
						}
					}
				}
				else if ( intent.getAction().equals(Define.IPG_CALL_NUMBER_INFO) )
				{
					new GetUserInfo(intent.getStringExtra("number"));
				}
				else if ( intent.getAction().equals(Define.IPG_CALL_USER_INFO) )
				{
					sName = intent.getStringExtra("NAME");
					sNumber = intent.getStringExtra("NUMBER");
					sUserId = intent.getStringExtra("ID");
					
					TextView name = (TextView)window.findViewById(R.id.call_profile_name);
					TextView department = (TextView)window.findViewById(R.id.call_profile_department);
					TextView number = (TextView)window.findViewById(R.id.call_profile_number);
					UserImageView profileImage = (UserImageView)window.findViewById(R.id.call_profileImage);
					TextView network = (TextView)window.findViewById(R.id.call_profile_network);
					
					name.setText(intent.getStringExtra("NAME"));
					department.setText(intent.getStringExtra("POSITION") + " | " + intent.getStringExtra("ORGANIZATION"));
					number.setText(intent.getStringExtra("NUMBER"));
					
					profileImage.setUserId(intent.getStringExtra("ID"));
					
					if ( NetworkStatusReceiver.isWifi( getApplicationContext() ) )
					{
						network.setText("Wi-Fi");
					}
					else
					{
						network.setText("LTE");
					}
				}
			}
		};
		
		IntentFilter theFilter = new IntentFilter();
		theFilter.addAction(Define.IPG_CALL_STATE_CHANGED);
		theFilter.addAction(Define.IPG_CALL_NUMBER_INFO);
		theFilter.addAction(Define.IPG_CALL_USER_INFO);
		registerReceiver(eventReceiver, theFilter);
		
		Bundle b = getIntent().getExtras();
		if ( b != null && b.getString("TYPE") != null && b.getString("TYPE").equals("Incoming") )
		{
			nowCallStatus = TYPE_RECEIVE;
			
			updateView();
			
			iCallType = Define.CALL_TYPE_INCOMING;
			
			new GetUserInfo(b.getString("NUMBER"));
		}
		else if ( b != null && b.getString("TYPE") != null && b.getString("TYPE").equals("Outgoing") )
		{
			nowCallStatus = TYPE_CONNECT;
			
			updateView();
			
			otherNumber = b.getString("NUMBER");
			
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "Connect");
            i.putExtra("NUMBER", otherNumber);
            sendBroadcast(i);
            
            iCallType = Define.CALL_TYPE_OUTGOING;
            
            sNumber = otherNumber;
            sName = otherNumber;
		}
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		
		callTimer = (TextView)findViewById(R.id.call_timer);
	}
	
	private class GetUserInfo extends Thread
	{
		private String userNumber;
		
		public GetUserInfo(String userNumber)
		{
			this.userNumber = userNumber;
			
			this.start();
		}
		
		public void run()
		{
			UltariSSLSocket sc = null;
			InputStreamReader ir = null;
			BufferedReader br = null;
			
			try
			{
				android.util.Log.d("IPageOn", "Connecting to " + Define.getServerIp( getApplicationContext() ) + ":" + Define.getServerPort( getApplicationContext() ));
				sc = new UltariSSLSocket( getApplicationContext(), Define.getServerIp( getApplicationContext() ), Integer.parseInt(Define.getServerPort( getApplicationContext() )) );
				
	            sc.setSoTimeout( 150000 );
	            ir = new InputStreamReader( sc.getInputStream() );
	            br = new BufferedReader( ir );
	            
	            AmCodec codec = new AmCodec();
	            String sendMsg = codec.EncryptSEED("CIDUSER\t" + userNumber);
	            android.util.Log.d("IPageOn", "Send : CIDUSER\t" + userNumber);
	            sc.send(sendMsg + "\f");
	            
	            int rcv = -1;
	            
	            StringBuffer sb = new StringBuffer();
	            char[] buf = new char[1025];
	            
	            while ( ( rcv = br.read(buf, 0, 1024)) >= 0 )
	            {
	            	sb.append(new String(buf, 0, rcv));
	            	
	            	android.util.Log.d("IPageOn", "Receive : " + sb.toString());
	            	
	            	if ( sb.indexOf("\f") >= 0 ) break;
	            }
	            
	            String userInfo = codec.DecryptSEED(sb.substring(0, sb.length() - 1));
	            
	            String id = "";
	            String name = userNumber;
	            String position = "";
	            String organization = "";
	            String number = userNumber;
	            
	            String[] ar = userInfo.split("\t");
	            
	            if ( ar.length == 5 )
	            {
	            	id = ar[1];
	            	name = ar[2];
	            	position = ar[3];
	            	organization = ar[4];
	            }
	            
	            Intent i = new Intent(Define.IPG_CALL_USER_INFO);
	            
	            i.putExtra("ID", id);
	            i.putExtra("NAME", name);
	            i.putExtra("POSITION", position);
	            i.putExtra("ORGANIZATION", organization);
	            i.putExtra("NUMBER", number);
	            sendBroadcast(i);
			}
			catch(Exception e)
			{
				android.util.Log.e(TAG, "UserInfo", e);
			}
			finally
			{
				if ( sc != null ) { try { sc.close(); sc = null; } catch(Exception e) {} }
				if ( ir != null ) { try { ir.close(); ir = null; } catch(Exception e) {} }
				if ( br != null ) { try { br.close(); br = null; } catch(Exception e) {} }
			}
		}
	}
	
	public static Handler callWindowHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			if ( msg.what == Define.CALL_CONNECTED )
			{
				window.nowCallStatus = TYPE_CALL;
				
				window.updateView();
				
				(new Thread(window)).start();
				
				if ( window.iWantPause )
				{
					android.util.Log.d(TAG, "Wait clicked");
					window.iWantPause = false;
					
					window.onClick(window.btnHold);
				}
			}
			else if ( msg.what == Define.CALL_DISCONNECTED )
			{
				if ( window.callLogAdded ) return;
				
				window.callLogAdded = true;
				
				window.nowCallStatus = TYPE_FINISH;
				
				//HERE
				if ( window.iCallType == Define.CALL_TYPE_INCOMING && !window.isCallConnected )
				{
					Log.e(TAG, "window iCallType 2:"+ window.iCallType + " / " + window.isCallConnected);
					window.iCallType = Define.CALL_TYPE_ABSENT;
				}
				
				window.duration = window.callTimerSecond;
				
				window.updateView();
				
				if ( !window.sNumber.equals("") && !window.sUserId.equals("") && !window.sName.equals(""))
					Database.instance(window).insertCallLog(window.sCallId, window.sNumber, window.sName, window.iCallType, window.duration, window.sUserId);
				
				window.mTask = new TimerTask() {
		            @Override
		            public void run()
		            {
		                window.finish();
		            }
		        };
		        
		        window.mTimer = new Timer();
		         
		        window.mTimer.schedule(window.mTask, 3000);
			}
			else if ( msg.what == Define.CALL_TIME_CHANGED )
			{
				int nowCallTimer = msg.arg1;
				
				android.util.Log.d(TAG, "CallTimer : " + nowCallTimer);
				
				int hour = nowCallTimer / 3600;
				int minute = ( nowCallTimer % 3600 ) / 60;
				int second = nowCallTimer % 60;
				
				android.util.Log.d(TAG, "CallTimer : " + hour + ":" + minute + ":" + second);
				
				String viewStr = "";
				
				if ( hour < 10 ) viewStr += "0" + hour;
				else viewStr += "" + hour;
				
				if ( minute < 10 ) viewStr += ":0" + minute;
				else viewStr += ":" + minute;
				
				if ( second < 10 ) viewStr += ":0" + second;
				else viewStr += ":" + second;
				
				window.callTimer.setText(viewStr);
			}
		}
	};
	
	public void run()
	{
		android.util.Log.d(TAG, "StartThread : " + nowCallStatus + ":" + TYPE_CALL);
		
		while ( nowCallStatus == TYPE_CALL || nowCallStatus == TYPE_KEYPAD )
		{
			try
			{
				Thread.sleep(1000);
			}
			catch(InterruptedException ie) {}
			
			Message m = callWindowHandler.obtainMessage(Define.CALL_TIME_CHANGED);
			m.arg1 = ++callTimerSecond;
			callWindowHandler.sendMessage(m);
		}
	}
	
	@Override
	public void onDestroy()
	{
		unregisterReceiver(eventReceiver);
		
		if ( mTimer != null)
			mTimer.cancel();
		
		super.onDestroy();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		mSensorManager.unregisterListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v)
	{
		if ( v == logo )
		{
			if ( nowCallStatus == TYPE_FINISH )
			{
				nowCallStatus = 0;
			}
			else
			{
				nowCallStatus += 1;
			}
			
			updateView();
		}
		else if ( v == btnCancelCall )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "EndCall");
            sendBroadcast(i);
		}
		else if ( v == btnRejectReceive )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "RejectCall");
            sendBroadcast(i);
		}
		else if ( v == btnAcceptReceive )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "AcceptCall");
            sendBroadcast(i);
            
            isCallConnected = true;
		}
		else if ( v == btnEndCall || v == btnEndCallKeypad )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "EndCall");
            sendBroadcast(i);
		}
		else if ( v == btnSpeaker || v == iconSpeaker )
		{
			if ( m_bSpeakerMode )
			{
				Intent i = new Intent(Define.IPG_CALL_ACTION);
	            i.putExtra("Action", "SpeakerOff");
	            sendBroadcast(i);
	            
	            m_bSpeakerMode = false;
	            
	            iconSpeaker.setSelected(false);
			}
			else
			{
				Intent i = new Intent(Define.IPG_CALL_ACTION);
	            i.putExtra("Action", "SpeakerOn");
	            sendBroadcast(i);
	            
	            m_bSpeakerMode = true;
	            
	            iconSpeaker.setSelected(true);
			}
		}
		else if ( v == btnMute || v == iconMute )
		{
			if ( m_bMute )
			{
				Intent i = new Intent(Define.IPG_CALL_ACTION);
	            i.putExtra("Action", "MuteOff");
	            sendBroadcast(i);
	            
	            m_bMute = false;
	            
	            iconMute.setSelected(false);
			}
			else
			{
				Intent i = new Intent(Define.IPG_CALL_ACTION);
	            i.putExtra("Action", "MuteOn");
	            sendBroadcast(i);
	            
	            m_bMute = true;
	            
	            iconMute.setSelected(true);
			}
		}
		else if ( v == btnRecord || v == iconRecord )
		{
			Toast.makeText(getApplicationContext(), "현재 녹음 서비스를 지원하지 않습니다.", Toast.LENGTH_SHORT).show();;
			/*if ( m_bRecord )
			{
				Intent i = new Intent(Define.IPG_CALL_ACTION);
	            i.putExtra("Action", "RecordStop");
	            sendBroadcast(i);
	            
	            m_bRecord = false;
	            
	            iconRecord.setSelected(false);
	            recordState.setVisibility(View.GONE);
			}
			else
			{
				Intent i = new Intent(Define.IPG_CALL_ACTION);
	            i.putExtra("Action", "RecordStart");
	            i.putExtra("PATH", getRecordFilePath());
	            sendBroadcast(i);
	            
	            m_bRecord = true;
	            
	            iconRecord.setSelected(true);
	            recordState.setVisibility(View.VISIBLE);
			}*/
		}
		else if ( v == btnKeypad || v == iconKeypad )
		{
			nowCallStatus = TYPE_KEYPAD;
			
			updateView();
		}
		else if ( v == keypad_hide )
		{
			nowCallStatus = TYPE_CALL;
			
			updateView();
		}
		else if ( v == k0 )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "0");
            sendBroadcast(i);
		}
		else if ( v == k1 )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "1");
            sendBroadcast(i);
		}
		else if ( v == k2 )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "2");
            sendBroadcast(i);
		}
		else if ( v == k3 )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "3");
            sendBroadcast(i);
		}
		else if ( v == k4 )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "4");
            sendBroadcast(i);
		}
		else if ( v == k5 )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "5");
            sendBroadcast(i);
		}
		else if ( v == k6 )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "6");
            sendBroadcast(i);
		}
		else if ( v == k7 )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "7");
            sendBroadcast(i);
		}
		else if ( v == k8 )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "8");
            sendBroadcast(i);
		}
		else if ( v == k9 )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "9");
            sendBroadcast(i);
		}
		else if ( v == kt )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "*");
            sendBroadcast(i);
		}
		else if ( v == kh )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "KEYPAD");
            i.putExtra("CHARACTER", "#");
            sendBroadcast(i);
		}
		else if ( v == btnHold || v == iconHold )
		{
			if ( Define.isCallPaused != true)
			{
				if ( !isHold )
				{
					Intent i = new Intent(Define.IPG_CALL_ACTION);
		            i.putExtra("Action", "PAUSE");
		            sendBroadcast(i);
		            
		            isHold = true;
		            
		            btnHold.setSelected(true);
		            titleHold.setText("통화재개");
				}
				else
				{
					Intent i = new Intent(Define.IPG_CALL_ACTION);
		            i.putExtra("Action", "RESUME");
		            sendBroadcast(i);
		            
		            isHold = false;
		            
		            btnHold.setSelected(false);
		            titleHold.setText("통화보류");
				}
			}
			else
				Toast.makeText(this, "상대방이 통화보류를 먼저 클릭하여 선택하실수 없습니다.", Toast.LENGTH_SHORT).show();
		}
		else if ( v == btnSwitch || v == iconSwitch )
		{
			Intent i = new Intent(this, kr.co.ultari.atsmart.ipocall.CallSelectUser.class);
            startActivity(i);
		}
		else if ( v == btnWait )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "AcceptCall");
            sendBroadcast(i);
            
            iWantPause = true;
		}
		else if ( v == btnLater )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "LATER");
            sendBroadcast(i);
		}
	}
	
	private String getRecordFilePath()
	{
		String folderPath = Environment.getExternalStorageDirectory() + File.separator + "AtSmartRecord";
		
		File f = new File(folderPath);
		if ( !f.exists() )
		{
			f.mkdir();
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.KOREA);
		Calendar calendar = Calendar.getInstance();
		return folderPath + File.separator + sdf.format(calendar.getTime()) + ".wav";
	}
	
	private void updateView()
	{
		switch(nowCallStatus)
		{
		case TYPE_CONNECT:
			
			findViewById(R.id.call_bg_receiving).setVisibility(View.INVISIBLE);
			findViewById(R.id.call_bg_ing).setVisibility(View.INVISIBLE);
			findViewById(R.id.call_bg_endcall).setVisibility(View.INVISIBLE);
			
			findViewById(R.id.call_status_receiving).setVisibility(View.GONE);
			findViewById(R.id.call_status_time).setVisibility(View.GONE);
			findViewById(R.id.call_status_end).setVisibility(View.GONE);
			findViewById(R.id.call_timer).setVisibility(View.GONE);
			
			findViewById(R.id.call_receive_btn_panel).setVisibility(View.GONE);
			findViewById(R.id.call_btn_panel).setVisibility(View.VISIBLE);
			
			findViewById(R.id.call_bottom_panel).setVisibility(View.VISIBLE);
			findViewById(R.id.call_receiving_bottom_panel).setVisibility(View.GONE);
			findViewById(R.id.call_ing_bottom_panel).setVisibility(View.GONE);
			
			findViewById(R.id.call_keypad_panel).setVisibility(View.GONE);
			
			break;
		case TYPE_RECEIVE:
			
			findViewById(R.id.call_bg_receiving).setVisibility(View.VISIBLE);
			findViewById(R.id.call_bg_ing).setVisibility(View.INVISIBLE);
			findViewById(R.id.call_bg_endcall).setVisibility(View.INVISIBLE);
			
			findViewById(R.id.call_status_receiving).setVisibility(View.VISIBLE);
			findViewById(R.id.call_status_time).setVisibility(View.GONE);
			findViewById(R.id.call_status_end).setVisibility(View.GONE);
			findViewById(R.id.call_timer).setVisibility(View.GONE);
			
			findViewById(R.id.call_receive_btn_panel).setVisibility(View.VISIBLE);
			findViewById(R.id.call_btn_panel).setVisibility(View.GONE);
			
			findViewById(R.id.call_bottom_panel).setVisibility(View.GONE);
			findViewById(R.id.call_receiving_bottom_panel).setVisibility(View.VISIBLE);
			findViewById(R.id.call_ing_bottom_panel).setVisibility(View.GONE);
			
			findViewById(R.id.call_keypad_panel).setVisibility(View.GONE);
			
			break;
		case TYPE_CALL:
			
			findViewById(R.id.call_bg_receiving).setVisibility(View.INVISIBLE);
			findViewById(R.id.call_bg_ing).setVisibility(View.VISIBLE);
			findViewById(R.id.call_bg_endcall).setVisibility(View.INVISIBLE);
			
			findViewById(R.id.call_status_receiving).setVisibility(View.GONE);
			findViewById(R.id.call_status_time).setVisibility(View.VISIBLE);
			findViewById(R.id.call_status_end).setVisibility(View.GONE);
			findViewById(R.id.call_timer).setVisibility(View.VISIBLE);
			
			findViewById(R.id.call_receive_btn_panel).setVisibility(View.GONE);
			findViewById(R.id.call_btn_panel).setVisibility(View.VISIBLE);
			
			findViewById(R.id.call_bottom_panel).setVisibility(View.GONE);
			findViewById(R.id.call_receiving_bottom_panel).setVisibility(View.GONE);
			findViewById(R.id.call_ing_bottom_panel).setVisibility(View.VISIBLE);
			
			findViewById(R.id.call_keypad_panel).setVisibility(View.GONE);
			
			break;
		case TYPE_KEYPAD:
			
			findViewById(R.id.call_bg_receiving).setVisibility(View.INVISIBLE);
			findViewById(R.id.call_bg_ing).setVisibility(View.VISIBLE);
			findViewById(R.id.call_bg_endcall).setVisibility(View.INVISIBLE);
			
			findViewById(R.id.call_status_receiving).setVisibility(View.GONE);
			findViewById(R.id.call_status_time).setVisibility(View.VISIBLE);
			findViewById(R.id.call_status_end).setVisibility(View.GONE);
			findViewById(R.id.call_timer).setVisibility(View.VISIBLE);
			
			findViewById(R.id.call_receive_btn_panel).setVisibility(View.GONE);
			findViewById(R.id.call_btn_panel).setVisibility(View.VISIBLE);
			
			findViewById(R.id.call_bottom_panel).setVisibility(View.GONE);
			findViewById(R.id.call_receiving_bottom_panel).setVisibility(View.GONE);
			findViewById(R.id.call_ing_bottom_panel).setVisibility(View.VISIBLE);
			
			findViewById(R.id.call_keypad_panel).setVisibility(View.VISIBLE);
			
			recordState.setVisibility(View.GONE);
			
			break;
		case TYPE_FINISH:
			
			findViewById(R.id.call_bg_receiving).setVisibility(View.INVISIBLE);
			findViewById(R.id.call_bg_ing).setVisibility(View.INVISIBLE);
			findViewById(R.id.call_bg_endcall).setVisibility(View.VISIBLE);
			
			findViewById(R.id.call_status_receiving).setVisibility(View.GONE);
			findViewById(R.id.call_status_time).setVisibility(View.GONE);
			findViewById(R.id.call_status_end).setVisibility(View.VISIBLE);
			findViewById(R.id.call_timer).setVisibility(View.GONE);
			
			findViewById(R.id.call_receive_btn_panel).setVisibility(View.GONE);
			findViewById(R.id.call_btn_panel).setVisibility(View.VISIBLE);
			
			findViewById(R.id.call_bottom_panel).setVisibility(View.VISIBLE);
			findViewById(R.id.call_receiving_bottom_panel).setVisibility(View.GONE);
			findViewById(R.id.call_ing_bottom_panel).setVisibility(View.GONE);
			
			findViewById(R.id.call_keypad_panel).setVisibility(View.GONE);
			
			recordState.setVisibility(View.GONE);
			
			break;
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		float distance = event.values[0];
		
		LinearLayout callLayout = (LinearLayout)findViewById(R.id.call_window_main);
		LinearLayout blackLayout = (LinearLayout)findViewById(R.id.call_black_screen);
		
		if ( nowCallStatus == TYPE_CALL && distance < 1 )
		{
			callLayout.setVisibility(View.GONE);
			blackLayout.setVisibility(View.VISIBLE);
		}
		else
		{
			callLayout.setVisibility(View.VISIBLE);
			blackLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		if ( v != k0 && v != k1 && v != k2 && v != k3 && v != k4 && v != k5 && v != k6 && v != k7 && v != k8 && v != k9 && v != kt && v != kh ) return false;
		
		if ( MotionEvent.ACTION_DOWN == event.getAction() )
        {
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "PlayDtmf");
            
            if ( v == k0 ) i.putExtra("CHARACTER", "0");
            else if ( v == k1 ) i.putExtra("CHARACTER", "1");
            else if ( v == k2 ) i.putExtra("CHARACTER", "2");
            else if ( v == k3 ) i.putExtra("CHARACTER", "3");
            else if ( v == k4 ) i.putExtra("CHARACTER", "4");
            else if ( v == k5 ) i.putExtra("CHARACTER", "5");
            else if ( v == k6 ) i.putExtra("CHARACTER", "6");
            else if ( v == k7 ) i.putExtra("CHARACTER", "7");
            else if ( v == k8 ) i.putExtra("CHARACTER", "8");
            else if ( v == k9 ) i.putExtra("CHARACTER", "9");
            else if ( v == kt ) i.putExtra("CHARACTER", "*");
            else if ( v == kh ) i.putExtra("CHARACTER", "#");
            
            sendBroadcast(i);
        }
		else if ( MotionEvent.ACTION_UP == event.getAction() )
        {
        	Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "StopDtmf");
            sendBroadcast(i);
        }
		
		return false;
	}
}
