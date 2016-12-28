package kr.co.ultari.atsmart.basic.service;

import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MyPhoneStateListener extends PhoneStateListener {
        private String TAG = "CallCatcher";

        @Override
        public void onCallStateChanged( int state, String incomingNumber )
        {
                switch ( state )
                {
                case TelephonyManager.CALL_STATE_IDLE :
                        Log.i( TAG, "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_IDLE " + incomingNumber );
                        break;
                case TelephonyManager.CALL_STATE_OFFHOOK :
                        Log.i( TAG, "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_OFFHOOK " + incomingNumber );
                        break;
                case TelephonyManager.CALL_STATE_RINGING :
                        Log.i( TAG, "MyPhoneStateListener->onCallStateChanged() -> CALL_STATE_RINGING " + incomingNumber );
                        break;
                default :
                        Log.i( TAG, "MyPhoneStateListener->onCallStateChanged() -> default -> " + Integer.toString( state ) );
                        break;
                }
        }
}
