package kr.co.ultari.atsmart.ipocall;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class CallServiceStarter extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		// TODO Auto-generated method stub
		if ( intent.getAction().equals( "android.intent.action.BOOT_COMPLETED" ) )
        {
                ComponentName cn = new ComponentName( context.getPackageName(), CallService.class.getName() );
                context.startService( new Intent().setComponent( cn ) );
        }
	}
}
