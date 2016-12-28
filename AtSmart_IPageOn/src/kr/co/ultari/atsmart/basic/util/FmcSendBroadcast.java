package kr.co.ultari.atsmart.basic.util;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.view.CallView;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class FmcSendBroadcast {
        private final static int START_MAKECALL = 100;
        public final static String EXTRA_MAKING_CALL_TYPE = "type";
        public final static int MAKING_CALL_TYPE_DEFAULT = -1;
        public final static int MAKING_CALL_TYPE_3G = 0;
        public final static int MAKING_CALL_TYPE_VOIP_AVAILABLE = 1;
        public final static int MAKING_CALL_TYPE_CHOOSE = 2;
        public final static int MAKING_CALL_TYPE_VOIP_ONLY = 3;
        public static String number = "";

        //2016-03-31
        private final static int FMC_MODE = 0;
        private final static int DEFAULT_MODE = 1;
        //
        
        //2016-03-31
        public static void FmcSendCall( String phoneNumber, int MODE, Context context)
        {
                try
                {
                		//2016-12-27
                		if ( Define.phoneCallState )
                		{
                			Toast.makeText(context, "일반전화 통화시 VoIP 통화 기능을 사용하실수 없습니다.", Toast.LENGTH_SHORT).show();
                			return;
                		}
                		//
                		
                		if(FMC_MODE == MODE)
                        {
                                number = phoneNumber.replaceAll( "-", "" );
                                if(number == null || number.equals( "" )) return;
                                //Log.d( "FMC", "sendCall FMC MODE! number:"+number );
                                
                                Intent i = new Intent(context, kr.co.ultari.atsmart.ipocall.CallWindow.class);
                    			i.putExtra("TYPE", "Outgoing");
                    			i.putExtra("NUMBER", number);
                    			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    			context.startActivity(i);
                    			
                                // WE VoIP 로 발신 방법 Broadcast 로 전달
                                /*Intent i = new Intent( "MAKING_CALL_TYPE" );
                                i.putExtra( EXTRA_MAKING_CALL_TYPE, MAKING_CALL_TYPE_VOIP_AVAILABLE );
                                Define.mContext.sendBroadcast( i );
                                // WE VoIP 로 발신 방법 Broadcast 로 전달후 500 ms 이후 실제 발신하기 위한 Handler 전달
                                mHandler.sendEmptyMessageDelayed( START_MAKECALL, 500 );
                                CallView.instance().isLoadComplete = true;*/
                        }
                        else if(DEFAULT_MODE == MODE)
                        {
                                number = phoneNumber.replaceAll( "-", "" );
                                if(number == null || number.equals( "" )) return;
                                //Log.d( "FMC", "sendCall DEFAULT MODE! number:"+number );
                                
                                Uri uri = Uri.parse("tel:" + number);
                                Intent it = new Intent(Intent.ACTION_CALL, uri); 
                                //Intent it = new Intent(Intent.ACTION_DIAL, uri); 
                                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(it);
                        }
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
        }
        //
        
        private static Handler mHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                switch ( msg.what )
                                {
                                case START_MAKECALL :
                                        try
                                        {
                                                Intent intent = new Intent( Intent.ACTION_CALL, Uri.fromParts( "tel", number, null ) );
                                                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                                intent.putExtra( "type", "cdma" );
                                                Define.mContext.startActivity( intent );
                                        }
                                        catch ( Exception e )
                                        {
                                                Define.EXCEPTION( e );
                                        }
                                        break;
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };
}
