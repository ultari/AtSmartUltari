package kr.co.ultari.atsmart.ipocall;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.util.Log;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.dbemulator.Database;

public class PSTNStateListener extends PhoneStateListener
{
	public static String TAG = "IPageOn";
	private Context context;
	
	private static boolean isPstnCall = false;
	
	public PSTNStateListener(Context context)
	{
		super();
		this.context = context;
	}
	
	@Override
	public void onCallStateChanged(int state, String incomingNumber)
	{
		switch (state)
		{
		case TelephonyManager.CALL_STATE_IDLE:
			Define.phoneCallState = false;
			Log.i(TAG, "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_IDLE " + incomingNumber);
			break;
		case TelephonyManager.CALL_STATE_OFFHOOK:
			Log.i(TAG, "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_OFFHOOK " + incomingNumber);
			Define.phoneCallState = true;
			break;
		case TelephonyManager.CALL_STATE_RINGING:
			Log.i(TAG, "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_RINGING " + incomingNumber);
			Define.phoneCallState = true;
			
			if ( !isPstnCall )
			{
				isPstnCall = true;
				Intent i = new Intent(Define.IPG_CALL_STATE_CHANGED);
	            i.putExtra("State", "IncomingPstnCalls");
	            i.putExtra("Number", incomingNumber);
	            context.sendBroadcast(i);
	            callWindowHandler.sendEmptyMessageDelayed(0, 3000);
			}
            
			break;
		default:
			Log.i(TAG, "MyPhoneStateListener->onCallStateChanged() -> default -> " + Integer.toString(state));
			break;
		}
	}
	
	@Override
	public void onServiceStateChanged(ServiceState serviceState)
	{
		switch (serviceState.getState())
		{
		case ServiceState.STATE_IN_SERVICE:
			Log.i(TAG, "MyPhoneStateListener->onServiceStateChanged() -> STATE_IN_SERVICE");
			serviceState.setState(ServiceState.STATE_IN_SERVICE);
			break;
		case ServiceState.STATE_OUT_OF_SERVICE:
			Log.i(TAG, "MyPhoneStateListener->onServiceStateChanged() -> STATE_OUT_OF_SERVICE");
			serviceState.setState(ServiceState.STATE_OUT_OF_SERVICE);
			break;
		case ServiceState.STATE_EMERGENCY_ONLY:
			Log.i(TAG, "MyPhoneStateListener->onServiceStateChanged() -> STATE_EMERGENCY_ONLY");
			serviceState.setState(ServiceState.STATE_EMERGENCY_ONLY);
			break;
		case ServiceState.STATE_POWER_OFF:
			Log.i(TAG, "MyPhoneStateListener->onServiceStateChanged() -> STATE_POWER_OFF");
			serviceState.setState(ServiceState.STATE_POWER_OFF);
			break;
		default:
			Log.i(TAG, "MyPhoneStateListener->onServiceStateChanged() -> default -> "
					+ Integer.toString(serviceState.getState()));
			break;
		}
	}
	
	public static Handler callWindowHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			isPstnCall = false;
		}
	};
}
