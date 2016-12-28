package kr.co.ultari.atsmart.ipocall;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ipageon.p701.sdk.core.IpgP701Impl;
import com.ipageon.p701.sdk.core.IpgP701Util;
import com.ipageon.p701.sdk.interfaces.IpgP701;
import com.ipageon.p701.sdk.interfaces.IpgP701Call;
import com.ipageon.p701.sdk.interfaces.IpgP701CallParams;
import com.ipageon.p701.sdk.interfaces.IpgP701Listener;
import com.ipageon.p701.sdk.state.CallState;
import com.ipageon.p701.sdk.state.Reason;
import com.ipageon.p701.sdk.state.RegistrationState;
import com.ipageon.p701.sdk.tools.IpgP701Config;
import com.ipageon.p701.sdk.tools.IpgP701Tools;
import com.ipageon.p701.sdk.tools.TransportType;
import com.ipageon.p701.sdk.tools.VideoSize;

import android.R.integer;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;

public class CallService extends Service implements IpgP701Listener
{
	public static final String TAG = "IPageOn";
	
	public static boolean callListenerStarted = false;
	
	private static IpgP701 mP701 = null;
	public static CallService callService = null;
	
	private AudioManager mAudio = null;
	
	private BroadcastReceiver eventReceiver = null;
	
	private boolean m_bBluetoothConnected = false;
	
	private boolean isRecording = false;
	
	public boolean nowCalling = false; 
	private IpgP701Call nowCall = null;
	
	private String nowRecordFilePath = null;
	
	private String sCallId = "";
	
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		callService = this;
		
		mAudio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		eventReceiver = new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				try{
						if ( !intent.getAction().equals(Define.IPG_CALL_ACTION) ) return;
						
						String action = intent.getStringExtra("Action");
						
						if ( action == null ) return;
						
						if ( mP701 == null ) return;
						
						if ( action.equals("EndCall") )
						{
							mP701.endCall(mP701.getCurrentCall());
						}
						else if ( action.equals("AcceptCall") )
						{
							IpgP701Call call = mP701.getCurrentCall();
		                    IpgP701CallParams params = mP701.createCallParams(call);
		                    
		                    mP701.setRecPath(Environment.getExternalStorageDirectory() + "/" + "voice" + ".wav");
		                    params.setVideoEnabled(false);
		                    params.setRecordFile(mP701.getRecPath());
		                    
		                    mP701.acceptCall(mP701.getCurrentCall(), params);
		                    
						}
						else if ( action.equals("RejectCall") )
						{
							mP701.endCall(mP701.getCurrentCall());
						}
						else if ( action.equals("Connect") )
						{
							String number = intent.getStringExtra("NUMBER");
							
							mP701.setRecPath(Environment.getExternalStorageDirectory() + "/" + "voice" + ".wav");
							mP701.startVoiceCall(number, null);
						}
						/*else if ( action.equals("BluetoothOn") )
						{
							mAudio.setBluetoothScoOn(true);
		        			mAudio.startBluetoothSco();
						}
						else if ( action.equals("BluetoothOff") )
						{
							mAudio.setBluetoothScoOn(false);
		        			mAudio.stopBluetoothSco();
						}*/
						else if ( action.equals("SpeakerOn") )
						{
							mAudio.setSpeakerphoneOn(true);
						}
						else if ( action.equals("SpeakerOff") )
						{
							mAudio.setSpeakerphoneOn(false);
						}
						else if ( action.equals("MuteOn") )
						{
							mAudio.setMicrophoneMute(true);
						}
						else if ( action.equals("MuteOff") )
						{
							mAudio.setMicrophoneMute(false);
							mAudio.setMode(AudioManager.MODE_IN_COMMUNICATION);
						}
						else if ( action.equals("RecordStart") )
						{
							nowRecordFilePath = intent.getStringExtra("PATH");
							
							startRecord();
						}
						else if ( action.equals("RecordStop") )
						{
							stopRecord();
						}
						else if ( action.equals("PlayDtmf") )
						{
							String character = intent.getStringExtra("CHARACTER");
							mP701.playDtmf(character.charAt(0));
						}
						else if ( action.equals("StopDtmf") )
						{
							mP701.stopDtmf();
						}
						else if ( action.equals("KEYPAD") )
						{
							String character = intent.getStringExtra("CHARACTER");
							mP701.getCurrentCall().sendDtmf(character.charAt(0));
						}
						else if ( action.equals("PAUSE") )
						{
							mP701.pauseCall(mP701.getCurrentCall());
						}
						else if ( action.equals("RESUME") )
						{
							if ( nowCall != null )
							{
								mP701.resumeCall(nowCall);
							}
						}
						else if ( action.equals("LATER") )
						{
							mP701.declineCall(mP701.getCurrentCall(), Reason.Busy);
						}
						else if ( action.equals("RESTART") )
						{
							Log.d(TAG, "CallRestart");
							callService.StopCallListener();
							callService.StartCallListener();
						}
						else if ( action.equals("SWITCH"))
						{
							String number = intent.getStringExtra("NUMBER");
							
							mP701.transferCall(mP701.getCurrentCall(), number);
						}
				}catch(Exception e) {e.printStackTrace();}
			}
		};
		
		IntentFilter theFilter = new IntentFilter();
		theFilter.addAction(Define.IPG_CALL_ACTION);
		registerReceiver(eventReceiver, theFilter);
		
		Log.d(TAG, "CallServiceStarted");
		
		StartCallListener();
		
		/*IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        IntentFilter filter3 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        this.registerReceiver(mBReceiver, filter1);
        this.registerReceiver(mBReceiver, filter2);
        this.registerReceiver(mBReceiver, filter3);*/
        
        /*if (mAudio.isBluetoothA2dpOn())
        {
        	m_bBluetoothConnected = true;
        }*/
	}
	
	public synchronized void StartCallListener()
	{
		Log.d(TAG, "CallListenerStarted : " + callListenerStarted);
		if ( callListenerStarted ) return;
		
		mP701 = IpgP701Tools.createIpgP701(this, IpgP701Config.IPG_P701_LOG_ENABLE);

		mP701.createP701Core(this);
        
        if (mP701 == null ) return;
        
        Log.d(TAG, "CallStarting...");
        
        mP701.setUserAgent(Define.IPG_P701_USER_AGENT, mP701.getP701Version());
        mP701.resetCamera();
        mP701.setRing(Define.IPG_P701_SAMPLE_RINGTONE);
        mP701.setRingback(Define.IPG_P701_SAMPLE_RINGBACK);
        mP701.setHoldRing(Define.IPG_P701_SAMPLE_HOLDTONE);

        mP701.setRootCertPath(Define.IPG_P701_SAMPLE_ROOT_CERT);
        mP701.setPrivateCertPath(Define.IPG_P701_SAMPLE_USER_CERT);
        mP701.setPrivateKeyPath(Define.IPG_P701_SAMPLE_USER_KEY);
        mP701.setKeepAlive(Define.IPG_P701_SAMPLE_KEEP_ALIVE_TIME);
        
        mP701.setUUID(Database.instance(this).selectConfig("UUID"));
        mP701.setGcmRegid(Database.instance(this).selectConfig("GCMID"));
        mP701.setComfortNoise(true);
        mP701.setVideoSize(VideoSize.VIDEO_TYPE_QVGA);
        

         //2016-12-20
        int type = 0;
        TransportType transportType = null;
        String port = "";
        
        try{
        	String iCallType = Database.instance(this).selectConfig("CALLCONNECTTYPE").trim();

        	if ( iCallType.equals(""))
        		Database.instance(this).updateConfig("CALLCONNECTTYPE", Integer.toString(Define.CALL_TYPE_MODE_TLS));
	        
        	type = Integer.parseInt(iCallType);
	        
	        if ( type == Define.CALL_TYPE_MODE_UDP)
	        {
	        	transportType =TransportType.TransportUdp;
	        	port = Define.udpGetPort();
	        }
	        else if( type == Define.CALL_TYPE_MODE_TLS)
	        {
	        	transportType = TransportType.TransportTls;
	        	port = Define.tlsGetPort();
	        }
	        
	        mP701.startClient(Define.getMyId(this),
	        		Define.getUserPw(Database.instance(this).selectConfig("UUID")),
	        		Database.instance(this).selectConfig("PHONENUMBER"),
	        		Define.getProxy(),
	        		port,
	        		Define.getDomain(),
	        		Define.IPG_P701_SAMPLE_HEARTBEAT,
	        		transportType);
        }
        catch(Exception e)
        {
        	e.printStackTrace();
        }
        
		callListenerStarted = true;
	}	
	public synchronized void StopCallListener()
	{
		if ( !callListenerStarted ) return;
		
		if (mP701 != null)
		{
			mP701.stopClient();
			
            mP701.destoryP701Core();
            mP701 = null;
        }
		
        callListenerStarted = false;
        Define.phoneCallState = false; //2016-12-27
	}
	
	@Override
	public void onDestroy()
	{
		unregisterReceiver(eventReceiver);
		//unregisterReceiver(bListener);
		
		StopCallListener();
		
		mAudio = null;
		
		Define.phoneCallState = false; //2016-12-27
		
		Log.d(TAG, "CallServiceDestroyed");
		
		super.onDestroy();
	}
	
	@Override
	public void onCoreMessage(String message)
	{
		Log.i(TAG, "onCoreMessage = " + message);
	}

	@Override
	public void onRegistrationState(RegistrationState state, String smessage)
	{
		Log.i(TAG, "onRegistrationState = " + state.toString());
	}

	@Override
	public void onCallState(IpgP701 core, IpgP701Call call, CallState state, String message)
	{
		Log.d(TAG, "Call : from " + call.getCallLog().getFrom().getUserName() + " to " + call.getCallLog().getTo().getUserName());
		
		if ( Define.phoneCallState && state == CallState.IncomingReceived )
		{
			Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "RejectCall");
            sendBroadcast(i);
            String otherNumber = call.getCallLog().getTo().getUserName();
            new GetUserInfo(otherNumber);
            return;
		}
		
		if (state == CallState.Idle) {}
		else if (state == CallState.OutgoingInit)
		{
            startVoipAudio();
            
            Log.d(TAG, "OutGoing : from " + call.getCallLog().getFrom().getUserName() + " to " + call.getCallLog().getTo().getUserName());
            
            String otherNumber = call.getCallLog().getTo().getUserName();
            
            Intent i = new Intent(Define.IPG_CALL_NUMBER_INFO);
            i.putExtra("number", otherNumber);
            sendBroadcast(i);
        }
		else if (state == CallState.OutgoingProgress) {}
		else if (state == CallState.OutgoingRinging) {}
		else if (state == CallState.IncomingReceived)
		{
            startVoipRing();
            
            Log.d(TAG, "Incoming : from " + call.getCallLog().getFrom().getUserName() + " to " + call.getCallLog().getTo().getUserName());
            
            String otherNumber = call.getCallLog().getFrom().getUserName();
            
            Intent i = new Intent(this, kr.co.ultari.atsmart.ipocall.CallWindow.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.putExtra("TYPE", "Incoming");
            i.putExtra("NUMBER", otherNumber);
            startActivity(i);
        }
		else if (state == CallState.Connected)
		{
            stopVoipRing();
            
            startVoipAudio();
            
            Intent i = new Intent(Define.IPG_CALL_STATE_CHANGED);
            i.putExtra("State", "Connected");
            sendBroadcast(i);
            
            Define.isCallPaused = false;//2016-12-26
        }
		else if (state == CallState.StreamsRunning)
		{
            nowCall = call;
            Define.isCallPaused = false;//2016-12-26
		}
		else if (state == CallState.CallUpdatedByRemote) {}
        else if (state == CallState.PausedByRemote)
        {
        	Intent i = new Intent(Define.IPG_CALL_STATE_CHANGED);
            i.putExtra("State", "PausedByRemote");
            sendBroadcast(i);
        }
        else if (state == CallState.CallReleased)
        {
            stopVoipAudio();
            
            Intent i = new Intent(Define.IPG_CALL_STATE_CHANGED);
            i.putExtra("State", "CallReleased");
            sendBroadcast(i);
        }
        else if (state == CallState.Paused)
        {
        	Intent i = new Intent(Define.IPG_CALL_STATE_CHANGED);
            i.putExtra("State", "Paused");
            sendBroadcast(i);
        }
        else if (state == CallState.Error)
        {
            Toast.makeText(this, call.getErrorInfo().getReason().toString(), Toast.LENGTH_SHORT).show();
        }
        else if (state == CallState.CallEnd)
        {
            stopVoipAudio();
            
            Intent i = new Intent(Define.IPG_CALL_STATE_CHANGED);
            i.putExtra("State", "CallEnd");
            sendBroadcast(i);
            
            nowCall = null;
        }
        else
        {
            Log.d(TAG, "Next version state...");
        }
	}
	
	public void startVoipAudio()
	{
		/*Log.d(TAG, "Bluetooth state : " + m_bBluetoothConnected);
		
		if ( m_bBluetoothConnected )
		{
			mAudio.setBluetoothScoOn(true);
			mAudio.startBluetoothSco();
		}
		else
		{
			mAudio.setBluetoothScoOn(false);
			mAudio.stopBluetoothSco();
		}*/
		
		nowCalling = true;
		
		mAudio.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    public void stopVoipAudio()
    {
    	mAudio.setMode(AudioManager.MODE_NORMAL);
    	mAudio.setSpeakerphoneOn(false);
    	
    	nowCalling = false;
    	
    	if ( isRecording )
    	{
    		stopRecord();
    	}
    }

    public void startVoipRing()
    {
        mAudio.setMode(AudioManager.MODE_RINGTONE);
        //mAudio.setSpeakerphoneOn(true);
    }

    public void stopVoipRing()
    {
        mAudio.setSpeakerphoneOn(false);
        mAudio.setMode(AudioManager.MODE_NORMAL);
    }
    
    private final BroadcastReceiver mBReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            /*if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action))
            {
                m_bBluetoothConnected = true;
                
                if ( nowCalling )
                {
	                mAudio.setBluetoothScoOn(true);
	    			mAudio.startBluetoothSco();
                }
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action))
            {
                m_bBluetoothConnected = false;
                
                if ( nowCalling )
                {
	                mAudio.setBluetoothScoOn(false);
	    			mAudio.stopBluetoothSco();
                }
            }*/
        }
    };
    
    private void startRecord()
    {
    	if ( nowCall == null ) return;
    	
    	nowCall.startRecording();
    	
    	isRecording = true;
    }
    
    private void stopRecord()
    {
    	if ( isRecording )
    	{
    		if ( nowCall != null )
    		{
    			nowCall.stopRecording();
    		}
    		
    		if ( nowRecordFilePath != null )
    		{
	    		try
	    		{
		    		File fromFile = new File(Environment.getExternalStorageDirectory() + "/" + "voice" + ".wav");
		    		File toFile = new File(nowRecordFilePath);
		    		
		    		fromFile.renameTo(toFile);
	    		}
	    		catch(Exception e)
	    		{
	    			Log.e(TAG, "FileRenameError", e);
	    		}
    		}
    		
    		isRecording = false;
    	}
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
	            //String position = "";
	            //String organization = "";
	            String number = userNumber;
	            
	            String[] ar = userInfo.split("\t");
	            
	            if ( ar.length == 5 )
	            {
	            	id = ar[1];
	            	name = ar[2];
	            	//position = ar[3];
	            	//organization = ar[4];
	            }
	            
	            sCallId = StringUtil.getNowDateTime().substring(0, 14);
	            Database.instance(getApplicationContext()).insertCallLog(sCallId, number, name, Define.CALL_TYPE_ABSENT, 0, id);
	            
	            /*Intent i = new Intent(Define.IPG_CALL_USER_INFO);
	            
	            i.putExtra("ID", id);
	            i.putExtra("NAME", name);
	            i.putExtra("POSITION", position);
	            i.putExtra("ORGANIZATION", organization);
	            i.putExtra("NUMBER", number);
	            sendBroadcast(i);*/
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
}
