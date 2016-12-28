package kr.co.ultari.atsmart.basic.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Vector;

import com.google.android.gms.drive.internal.GetMetadataRequest;
import com.smv.service.ISMVService;

import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.GcmManager;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.util.ImageUtil;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

@SuppressLint( "HandlerLeak" )
public class AtSmartService extends Service implements Runnable {
        private static boolean isServiceRunning = false;
        private static final int REQUEST_CODE = 99;
        final Messenger mMessenger = new Messenger( new IncomingHandler() );
        ArrayList<Messenger> m_UIConnectors = new ArrayList<Messenger>();
        Thread thread = null;
        String loginIdInThread = null;
        AmCodec codec;
        public AtSmartService instance;
        private UltariSSLSocket sc = null;
        String noopStr = null;
        private Timer sendTimer = null;
        private Vector<String> sb = null;
        public boolean noWait = false;
        private BufferedReader br = null;
        public static String connectedId = null;

        @Override
        public IBinder onBind( Intent intent )
        {
                TRACE( "Service onBind!" );
                return mMessenger.getBinder();
        }
        class IncomingHandler extends Handler {
                @Override
                public void handleMessage( Message msg )
                {
                        switch ( msg.what )
                        {
                        case Define.AM_REGISTER_CLIENT :
                                m_UIConnectors.add( msg.replyTo );
                                break;
                        case Define.AM_UNREGISTER_CLIENT :
                                m_UIConnectors.remove( msg.replyTo );
                                break;
                        case Define.AM_NEW_MESSAGE :
                                String msgStr = msg.getData().getString( "MESSAGE" );
                                try
                                {
                                        send( "MESSAGE\t" + msgStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                                break;
                        case Define.AM_NEW_CHAT :
                                String talkStr = msg.getData().getString( "MESSAGE" );
                                String msgId = msg.getData().getString( "MESSAGEID" );
                                try
                                {
                                        send( "NOTIFY\t" + msgId + "\t" + talkStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                                break;
                        case Define.AM_NICK :
                                String nickStr = msg.getData().getString( "NICK" );
                                try
                                {
                                        send( "Nick\t" + nickStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                                break;
                        case Define.AM_RESTART_SERVICE :
                                String userId = msg.getData().getString( "USERID" );
                                if ( userId != null && userId.equals( loginIdInThread ) ) break;
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
                                break;
                        case Define.AM_STOP_SERVICE :
                                isServiceRunning = false;
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
                                break;
                        case Define.AM_READ_COMPLETE :
                                talkStr = msg.getData().getString( "MESSAGE" );
                                try
                                {
                                        send( "NOTIFY\t[RC]_" + StringUtil.getNowDateTime() + "\t" + talkStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                                break;
                        case Define.AM_USER_INFO :
                                talkStr = msg.getData().getString( "MESSAGE" );
                                try
                                {
                                        send( talkStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                                break;
                        default :
                                super.handleMessage( msg );
                        }
                }
        }

        public void showAlertDialog( Context context, String title, String content, String roomId, String talkId )
        {
                Log.d( "ShowDialog", "6" );
                Bundle bun = new Bundle();
                bun.putString( "TITLE", title );
                bun.putString( "MESSAGE", content );
                bun.putString( "RoomId", roomId );
                bun.putString( "userId", talkId );
                Intent popupIntent = new Intent( context, kr.co.ultari.atsmart.basic.service.AlertDialog.class );
                popupIntent.putExtras( bun );
                popupIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                PendingIntent pie = PendingIntent.getActivity( context, 0, popupIntent, PendingIntent.FLAG_UPDATE_CURRENT );
                try
                {
                        pie.send();
                        AudioManager audioManager = ( AudioManager ) context.getSystemService( Context.AUDIO_SERVICE );
                        switch ( audioManager.getRingerMode() )
                        {
                        case AudioManager.RINGER_MODE_VIBRATE :
                                if ( Define.vibrator.equals( "ON" ) )
                                {
                                        Vibrator mVib = ( Vibrator ) getSystemService( Context.VIBRATOR_SERVICE );
                                        try
                                        {
                                                mVib.vibrate( 1000 );
                                        }
                                        catch ( Exception e )
                                        {
                                                mVib.cancel();
                                                e.printStackTrace();
                                        }
                                }
                                break;
                        case AudioManager.RINGER_MODE_NORMAL :
                                if ( Define.sound.equals( "ON" ) )
                                {
                                        Ringtone ringtone = null;
                                        try
                                        {
                                                if ( roomId.equals( "note" ) || content.indexOf( getString( R.string.note ) ) >= 0
                                                                || content.indexOf( "ATTACH" ) >= 0 || content.indexOf( "TITLE" ) >= 0
                                                                || content.indexOf( getString( R.string.notice ) ) >= 0 )
                                                {
                                                        if ( Define.alarmSoundUri.equals( "" ) || Define.alarmSoundUri.equals( "0" )
                                                                        || Define.alarmSoundUri == null )
                                                        {
                                                                Uri defaultNotificationUri = RingtoneManager.getActualDefaultRingtoneUri( this,
                                                                                RingtoneManager.TYPE_NOTIFICATION );
                                                                if ( defaultNotificationUri != null ) Define.alarmSoundUri = defaultNotificationUri.toString();
                                                        }
                                                        ringtone = RingtoneManager.getRingtone( this, Uri.parse( Define.alarmSoundUri ) );
                                                        ringtone.play();
                                                }
                                                else
                                                {
                                                        // content://media/internal/audio/media/16
                                                        if ( Define.chatSoundUri.equals( "" ) || Define.chatSoundUri.equals( "0" )
                                                                        || Define.chatSoundUri == null )
                                                        {
                                                                Uri defaultNotificationUri = RingtoneManager.getActualDefaultRingtoneUri( this,
                                                                                RingtoneManager.TYPE_NOTIFICATION );
                                                                if ( defaultNotificationUri != null ) Define.chatSoundUri = defaultNotificationUri.toString();
                                                        }
                                                        ringtone = RingtoneManager.getRingtone( this, Uri.parse( Define.chatSoundUri ) );
                                                        ringtone.play();
                                                }
                                        }
                                        catch ( Exception e )
                                        {
                                                ringtone.stop();
                                                e.printStackTrace();
                                        }
                                }
                                if ( Define.vibrator.equals( "ON" ) )
                                {
                                        Vibrator mVib = ( Vibrator ) getSystemService( Context.VIBRATOR_SERVICE );
                                        try
                                        {
                                                mVib.vibrate( 1000 );
                                        }
                                        catch ( Exception e )
                                        {
                                                mVib.cancel();
                                                e.printStackTrace();
                                        }
                                }
                                break;
                        case AudioManager.RINGER_MODE_SILENT :
                                if ( Define.vibrator.equals( "ON" ) )
                                {
                                        Vibrator mVib = ( Vibrator ) getSystemService( Context.VIBRATOR_SERVICE );
                                        try
                                        {
                                                mVib.vibrate( 1000 );
                                        }
                                        catch ( Exception e )
                                        {
                                                mVib.cancel();
                                                e.printStackTrace();
                                        }
                                }
                                break;
                        }
                }
                catch ( CanceledException e )
                {
                        EXCEPTION( e );
                }
        }
        public Handler serviceHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                Define.push = Database.instance( instance ).selectConfig( "push" );
                                if ( Define.push.equals( "" ) || Define.push.equals( "0" ) ) Define.push = "ON";
                                if ( msg.what == Define.AM_NEW_CHAT )
                                {
                                        String[] valueAr = ( String[] ) msg.obj;
                                        if ( Define.isVisible == false )
                                        {
                                                if ( valueAr[1].indexOf( "[READ_COMPLETE]" ) < 0 && valueAr[1].indexOf( "[ROOM_IN]" ) < 0
                                                                && valueAr[1].indexOf( "[ROOM_OUT]" ) < 0 )
                                                {
                                                        if ( valueAr[3].indexOf( Define.getMyId( getBaseContext() ) ) == -1 && Define.push.equals( "ON" ) )
                                                        {
                                                                if ( Define.getRetain( getBaseContext() ).equals( "N" ) ) return;
                                                                Log.d( "ShowDialog", "7" );
                                                                if ( valueAr[1].indexOf( "ATTACH://" ) < 0 )
                                                                {
                                                                        showAlertDialog( getBaseContext(), valueAr[0], valueAr[1], valueAr[4], valueAr[3] );
                                                                        showNotification( instance, valueAr[0] + " : " + valueAr[1], valueAr[3], valueAr[4],
                                                                                        valueAr[2] );
                                                                }
                                                                else
                                                                {
                                                                        showAlertDialog( getBaseContext(), getString( R.string.fileReceive ),
                                                                                        getString( R.string.fileReceiveMsg ), valueAr[4], valueAr[3] );
                                                                        showNotification( instance, getString( R.string.fileReceive ) + " : "
                                                                                        + getString( R.string.fileReceiveMsg ), valueAr[3], valueAr[4], "" );
                                                                }
                                                        }
                                                }
                                        }
                                        else
                                        {
                                                Intent sendIntent = new Intent( Define.MSG_CHAT );
                                                sendIntent.putExtra( Define.TITLE, valueAr[0] );
                                                sendIntent.putExtra( Define.CONTENT, valueAr[1] );
                                                sendIntent.putExtra( Define.USERIDS, valueAr[2] );
                                                sendIntent.putExtra( Define.TALKID, valueAr[3] );
                                                sendIntent.putExtra( Define.ROOMID, valueAr[4] );
                                                sendIntent.putExtra( Define.CHATID, valueAr[5] );
                                                sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                                                sendBroadcast( sendIntent );
                                                // sendMessageToUI(
                                                // Define.AM_NEW_CHAT,
                                                // valueAr[0], valueAr[1],
                                                // valueAr[2], valueAr[3],
                                                // valueAr[4] );
                                        }
                                }
                                else if ( msg.what == Define.AM_NEW_MESSAGE )
                                {
                                        Define.push = Database.instance( instance ).selectConfig( "push" );
                                        if ( Define.push.equals( "" ) || Define.push.equals( "0" ) ) Define.push = "ON";
                                        String[] valueAr = ( String[] ) msg.obj;
                                        if ( Define.isVisible == false )
                                        {
                                                if ( Define.getRetain( getBaseContext() ).equals( "N" ) ) return;
                                                if ( valueAr[0].indexOf( Define.getMyId( instance ) ) == -1 && Define.push.equals( "ON" ) )
                                                {
                                                        String title = valueAr[0];
                                                        if ( title.indexOf( "\n" ) >= 0 ) title = title.substring( title.indexOf( "\n" ) + 1 );
                                                        if ( title.indexOf( "\n" ) >= 0 ) title = title.substring( 0, title.indexOf( "\n" ) );
                                                        String text = valueAr[1].replace( "<br>", "<br />" );
                                                        showAlertDialog( getBaseContext(), title, "[" + getString( R.string.message ) + "]" + "<br>" + text,
                                                                        "message", "" );
                                                        // showAlertDialog(getBaseContext(),
                                                        // valueAr[0],
                                                        // "["+getString(R.string.notice)
                                                        // + "]" +"<br>"+text,
                                                        // "note", "");
                                                }
                                        }
                                        else
                                        {
                                                Intent sendIntent = new Intent( Define.MSG_NOTIFY );
                                                sendIntent.putExtra( Define.TITLE, valueAr[0] );
                                                sendIntent.putExtra( Define.CONTENT, valueAr[1] );
                                                sendIntent.putExtra( Define.USERIDS, "" );
                                                sendIntent.putExtra( Define.TALKID, "" );
                                                sendIntent.putExtra( Define.ROOMID, "[MESSAGE]" );
                                                sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                                                sendBroadcast( sendIntent );
                                        }
                                }
                                else if ( msg.what == Define.AM_NEW_NOTIFY )
                                {
                                        Define.push = Database.instance( instance ).selectConfig( "push" );
                                        if ( Define.push.equals( "" ) || Define.push.equals( "0" ) ) Define.push = "ON";
                                        String[] valueAr = ( String[] ) msg.obj;
                                        if ( Define.isVisible == false )
                                        {
                                                if ( Define.getRetain( getBaseContext() ).equals( "N" ) ) return;
                                                if ( valueAr[0].indexOf( Define.getMyId( instance ) ) == -1 && Define.push.equals( "ON" ) )
                                                {
                                                        String title = valueAr[0];
                                                        if ( title.indexOf( "\n" ) >= 0 ) title = title.substring( title.indexOf( "\n" ) + 1 );
                                                        if ( title.indexOf( "\n" ) >= 0 ) title = title.substring( 0, title.indexOf( "\n" ) );
                                                        String text = valueAr[1].replace( "<br>", "<br />" );
                                                        Log.d( "ShowDialog", "9" );
                                                        showAlertDialog( getBaseContext(), title, "[" + getString( R.string.notice ) + "]" + "<br>" + text,
                                                                        "note", "" );
                                                        // showAlertDialog(getBaseContext(),
                                                        // valueAr[0],
                                                        // "["+getString(R.string.notice)
                                                        // + "]" +"<br>"+text,
                                                        // "note", "");
                                                }
                                        }
                                        else
                                        {
                                                Intent sendIntent = new Intent( Define.MSG_NOTIFY );
                                                sendIntent.putExtra( Define.TITLE, valueAr[0] );
                                                sendIntent.putExtra( Define.CONTENT, valueAr[1] );
                                                sendIntent.putExtra( Define.USERIDS, "" );
                                                sendIntent.putExtra( Define.TALKID, "" );
                                                sendIntent.putExtra( Define.ROOMID, "[ALARM]" );
                                                sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                                                sendBroadcast( sendIntent );
                                                // sendMessageToUI(
                                                // Define.AM_NEW_NOTIFY,
                                                // valueAr[0], valueAr[1], "",
                                                // "", "" );
                                        }
                                }
                                else if ( msg.what == Define.AM_NAME )
                                {
                                        Intent sendIntent = new Intent( Define.MSG_MY_NAME );
                                        sendIntent.putExtra( Define.NAMES, "" );
                                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                                        sendBroadcast( sendIntent );
                                        // sendMessageToUI( Define.AM_NAME, "",
                                        // "", "", "", "" );
                                }
                                else if ( msg.what == Define.AM_SHOW_WAIT )
                                {
                                        ActionManager.showWaitForConnect();
                                }
                                else if ( msg.what == Define.AM_HIDE_WAIT )
                                {
                                        ActionManager.hideWaitForConnect();
                                }
                                else
                                {
                                        super.handleMessage( msg );
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        };

        /*
         * private void sendMessageToUI(int msgIndex, String title, String content, String userIds, String talkId, String roomId)
         * {
         * for (int i = (m_UIConnectors.size() - 1); i >= 0; i--)
         * {
         * try
         * {
         * Message msg = Message.obtain(null, msgIndex);
         * Bundle b = new Bundle();
         * b.putString("title", title);
         * b.putString("content", content);
         * b.putString("userIds", userIds);
         * b.putString("talkId", talkId);
         * b.putString("RoomId", roomId);
         * msg.setData(b);
         * m_UIConnectors.get(i).send(msg);
         * }
         * catch (RemoteException e)
         * {
         * m_UIConnectors.remove(i);
         * EXCEPTION(e);
         * }
         * }
         * }
         */
        @Override
        public void onCreate()
        {
                super.onCreate();
                Define.resetServerInfo( this );
                connectFMC();
                isServiceRunning = true;
                instance = this;
                if ( thread == null )
                {
                        thread = new Thread( this );
                        thread.start();
                }
                Define.setContext( this );
                sendTimer = new Timer();
                sendTimer.schedule( sendServiceTimer, 1000, 20000 );
                sb = new Vector<String>();
                TRACE( "Service onCreate()" );
                IntentFilter f = new IntentFilter();
                f.addAction( Define.MSG_SEND_NICK );
                f.addAction( Define.MSG_USERINFO );
                f.addAction( Define.MSG_NEW_CHAT );
                f.addAction( Define.MSG_READ_COMPLETE );
                f.addAction( Define.MSG_RESTART_SERVICE );
                f.addAction( Define.MSG_MYFOLDER_USER_ADD );
                f.addAction( Define.MSG_MYFOLDER_USER_DEL );
                f.addAction( Define.MSG_MYFOLDER_GROUP_ADD );
                f.addAction( Define.MSG_MYFOLDER_GROUP_MOD );
                f.addAction( Define.MSG_MYFOLDER_GROUP_DEL );
                f.addAction( Define.MSG_MYFOLDER_SUB_GROUP_ADD );
                f.addAction( WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION );
                // f.addAction(Define.MSG_NETWORK_CHANGE);
                f.addAction( Define.MSG_PASSWORD_CHANGE );
                
                switch ( Define.SET_COMPANY )
                {
                case Define.SAMSUNG :
                        f.addAction( Define.FMC_PROVISIONING );
                        f.addAction( Define.FMC_CALL_STATE );
                        break;
                default :
                        break;
                }
                registerReceiver( mBroadcastReceiver, new IntentFilter( f ) );
        }

        /*
         * START_STICKY START_NOT_STICKY START_REDELIVER_INTENT
         */
        @Override
        public int onStartCommand( Intent intent, int flags, int startId )
        {
                if ( intent != null )
                {
                        Bundle b = intent.getExtras();
                        if ( b != null )
                        {
                                // 2015-04-17
                                /*
                                 * String serverIp = intent.getStringExtra(
                                 * "we_work_svr_pub_ip" ); String serverPort =
                                 * intent.getStringExtra( "we_work_svr_pub_port"
                                 * ); String proxyIp = intent.getStringExtra(
                                 * "we_work_svr_proxy_pub_ip" ); String
                                 * proxyPort = intent.getStringExtra(
                                 * "we_work_svr_proxy_pub_port" ); String id =
                                 * intent.getStringExtra( "we_work_usr_id" );
                                 * String pw = intent.getStringExtra(
                                 * "we_work_usr_pwd" );
                                 * Define.SERVER_IP = serverIp;
                                 * Define.SERVER_PORT = serverPort;
                                 * Define.PROXY_IP = proxyIp; Define.PROXY_PORT
                                 * = proxyPort;
                                 * Database.instance(getApplicationContext()).
                                 * updateConfig("SERVERIP", Define.SERVER_IP );
                                 * Database
                                 * .instance(getApplicationContext()).updateConfig
                                 * ("SERVERPORT", Define.SERVER_PORT );
                                 * Database.
                                 * instance(getApplicationContext()).updateConfig
                                 * ("PROXYIP", Define.PROXY_IP );
                                 * Database.instance
                                 * (getApplicationContext()).updateConfig
                                 * ("PROXYPORT", Define.PROXY_PORT );
                                 * Log.e("Service", "onStartCommand CAll!");
                                 */
                                // AtSmartSso sso= new AtSmartSso(userId,
                                // userPw);
                                // sso.start();
                                //
                                String id = b.getString( "ID" );
                                String pw = b.getString( "PW" );
                                if ( id != null )
                                {
                                        Define.setMyId( id );
                                        Define.setMyPW( pw );
                                        if ( thread == null )
                                        {
                                                thread = new Thread( this );
                                                thread.start();
                                        }
                                        else if ( loginIdInThread != null && !loginIdInThread.equals( id ) )
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
                                        }
                                }
                        }
                }
                return START_STICKY;
        }

        public static boolean isServiceRunning()
        {
                return isServiceRunning;
        }

        @Override
        public void onDestroy()
        {
                super.onDestroy();
                disconnectFMC();
                TRACE( "onServiceDestroy" );
                unregisterReceiver( mBroadcastReceiver );
                isServiceRunning = false;
                if ( sendTimer != null ) sendTimer.cancel();
                if ( sc != null )
                {
                        try
                        {
                                sc.close();
                        }
                        catch ( Exception e )
                        {}
                }
        }
        TimerTask sendServiceTimer = new TimerTask() {
                public void run()
                {
                        try
                        {
                                while ( sb.size() > 0 )
                                {
                                        String msg = sb.remove( 0 );
                                        send( msg );
                                }
                                /*
                                 * instance.requestMsgCount++; if(
                                 * instance.requestMsgCount >= 12) { send(
                                 * "RESERVED\tAndroid" );
                                 * instance.requestMsgCount = 0; }
                                 */
                                
                                //Log.d( "SERVICE", "timer call!" );
                                // Thread.sleep( 5000 );
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };
        class NoopTimer extends Thread {
                BufferedWriter bw = null;
                boolean m_bNoopFinished = false;

                public NoopTimer( BufferedWriter bw )
                {
                        this.bw = bw;
                        this.start();
                }

                public void finish()
                {
                        m_bNoopFinished = true;
                }

                public void run()
                {
                        while ( !m_bNoopFinished )
                        {
                                try
                                {
                                        sleep( 100000 );
                                        bw.write( instance.noopStr );
                                        bw.flush();
                                }
                                catch ( Exception e )
                                {
                                        return;
                                }
                        }
                }
        };

        public void run()
        {
                android.os.Process.setThreadPriority( android.os.Process.THREAD_PRIORITY_FOREGROUND );
                StringBuffer sb = new StringBuffer();
                codec = new AmCodec();
                sc = null;
                InputStreamReader ir = null;
                NoopTimer noopTimer = null;
                char[] buf = new char[1025];
                while ( isServiceRunning )
                {
                        ir = null;
                        br = null;
                        sc = null;
                        try
                        {
                                sb.delete( 0, sb.length() );
                                if ( Define.getMyId( this ) == null || Define.getMyId( this ).equals( "" ) )
                                {
                                        try
                                        {
                                                Thread.sleep( 10000 );
                                        }
                                        catch ( InterruptedException ie )
                                        {}
                                        continue;
                                }
                                loginIdInThread = Define.getMyId( this );
                                String ipInThread = "";
                                int portInThread = 0;
                                ipInThread = Define.getServerIp( this );
                                portInThread = Integer.parseInt( Define.getServerPort( this ) );
                                if ( ipInThread == null )
                                {
                                        try
                                        {
                                                Thread.sleep( 3000 );
                                        }
                                        catch ( InterruptedException ie )
                                        {}
                                        continue;
                                }
                                sc = new UltariSSLSocket( this, ipInThread, portInThread );
                                sc.setSoTimeout( 150000 );
                                ir = new InputStreamReader( sc.getInputStream() );
                                br = new BufferedReader( ir );
                                
                                //Message m = serviceHandler.obtainMessage( Define.AM_HIDE_WAIT );
                                //serviceHandler.sendMessage( m );
                                
                                WifiManager wifiManager = ( WifiManager ) getSystemService( WIFI_SERVICE );
                                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
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
                                String macAddr = wifiInfo.getMacAddress();

                                if ( Define.regid != null ) send( "HI\t" + loginIdInThread + "\tAndroid\t" + Define.regid );
                                else if ( macAddr != null ) send( "HI\t" + loginIdInThread + "\tAndroid\t" + macAddr );
                                else send( "HI\t" + loginIdInThread + "\tAndroid\tNoC2dm" );
                                int rcv = 0;
                                if ( instance.noopStr == null ) instance.noopStr = codec.EncryptSEED( "noop" ) + "\f";
                                if ( noopTimer == null ) noopTimer = new NoopTimer( sc.getWriter() );
                                while ( (rcv = br.read( buf, 0, 1024 )) >= 0 )
                                {
                                        sb.append( new String( buf, 0, rcv ) );
                                        int pos;
                                        while ( (pos = sb.indexOf( "\f" )) >= 0 )
                                        {
                                                String rcvStr = codec.DecryptSEED( sb.substring( 0, pos ) );
                                                sb.delete( 0, pos + 1 );
                                                TRACE( "RECEIVED : " + rcvStr );
                                                String command = "";
                                                ArrayList<String> param = new ArrayList<String>();
                                                String nowStr = "";
                                                for ( int i = 0; i < rcvStr.length(); i++ )
                                                {
                                                        if ( rcvStr.charAt( i ) == '\t' )
                                                        {
                                                                if ( command.equals( "" ) )
                                                                {
                                                                        command = nowStr;
                                                                }
                                                                else
                                                                {
                                                                        param.add( nowStr );
                                                                }
                                                                nowStr = "";
                                                        }
                                                        else if ( i == (rcvStr.length() - 1) )
                                                        {
                                                                nowStr += rcvStr.charAt( i );
                                                                if ( command.equals( "" ) )
                                                                {
                                                                        command = nowStr;
                                                                }
                                                                else
                                                                {
                                                                        param.add( nowStr );
                                                                }
                                                                nowStr = "";
                                                        }
                                                        else
                                                        {
                                                                nowStr += rcvStr.charAt( i );
                                                        }
                                                }
                                                process( command, param );
                                        }
                                }
                                Log.d( TAG, "Connection closed1" );
                        }
                        catch ( Exception e )
                        {
                                Log.d( TAG, "Connection closed2" );
                                EXCEPTION( e );
                        }
                        finally
                        {
                                Log.d( TAG, "Connection closed3" );
                                connectedId = null;
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
                                Log.d( TAG, "Service closed" );
                                if ( noopTimer != null )
                                {
                                        noopTimer.finish();
                                        noopTimer = null;
                                }
                                loginIdInThread = null;
                        }
                        
                        //Message m = serviceHandler.obtainMessage( Define.AM_SHOW_WAIT );
                        //serviceHandler.sendMessage( m );
                        
                        if ( !noWait )
                        {
                                try
                                {
                                        Thread.sleep( 10000 );
                                }
                                catch ( InterruptedException ie )
                                {}
                        }
                        noWait = false;
                }
                thread = null;
        }

        public void showNotification( Context context, String msg, String talkId, String roomId, String userIds )
        {
                NotificationManager nm = ( NotificationManager ) context.getSystemService( Context.NOTIFICATION_SERVICE );
                try
                {
                        NotificationCompat.Builder mNotifyBuilder = new NotificationCompat.Builder( this );
                        mNotifyBuilder.setNumber( Define.notoficationNumber++ );
                        mNotifyBuilder.setLights( Color.BLUE, 1000, 2000 );
                        mNotifyBuilder.setAutoCancel( true );
                        mNotifyBuilder.setSmallIcon( R.drawable.icon ).setContentTitle( getString( R.string.app_name ) ).setContentText( msg );
                        if ( Define.useNotificationLargeIcon )
                        {
                                if ( Define.getSmallBitmap( talkId ) == null ) ImageUtil.getBitmapFromURL( context, talkId,
                                                "http://" + Define.getProxyIp( this ) + ":" + Define.getProxyPort( this ) + "/"
                                                                + Define.requestPictureSizeShowNotification + talkId + ".jpg" );
                                else
                                {
                                        mNotifyBuilder.setLargeIcon( Define.getSmallBitmap( talkId ) );
                                        mNotifyBuilder.setTicker( msg );
                                }
                        }
                        int badgeCount = Define.notoficationNumber;
                        Intent intent = new Intent( "android.intent.action.BADGE_COUNT_UPDATE" );
                        intent.putExtra( "badge_count", badgeCount - 1 );
                        intent.putExtra( "badge_count_package_name", "kr.co.ultari.atsmart.basic" );
                        intent.putExtra( "badge_count_class_name", "kr.co.ultari.atsmart.basic.AtSmart" );
                        sendBroadcast( intent );
                        Bundle bun = new Bundle();
                        bun.putString( "RoomId", roomId );
                        bun.putString( "userId", userIds );
                        Intent popupIntent = new Intent( context, kr.co.ultari.atsmart.basic.AtSmart.class );
                        popupIntent.putExtras( bun );
                        PendingIntent contentIntent = PendingIntent.getActivity( context, REQUEST_CODE, popupIntent, PendingIntent.FLAG_UPDATE_CURRENT );
                        mNotifyBuilder.setContentIntent( contentIntent );
                        nm = ( NotificationManager ) getSystemService( Context.NOTIFICATION_SERVICE );
                        nm.notify( Define.AtSmartPushNotification, mNotifyBuilder.build() );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public boolean send( String msg ) throws Exception
        {
                if ( sc != null )
                {
                        msg.replaceAll( "\f", "" );
                        TRACE( "Send : " + msg );
                        msg = codec.EncryptSEED( msg );
                        msg += "\f";
                        sc.send( msg );
                        return true;
                }
                else
                {
                        sb.add( msg );
                        return false;
                }
        }

        public void process( String command, ArrayList<String> param )
        {
                for ( int i = 0; i < param.size(); i++ )
                {
                        String str = param.get( i );
                        if ( str.indexOf( "<BR>" ) >= 0 )
                        {
                                str = str.replaceAll( "<BR>", "\n" );
                        }
                        param.set( i, str );
                }
                try
                {
                        if ( command.equals( "SUCCESS" ) && param.size() == 1 )
                        {
                                TRACE( param.get( 0 ) );
                                Database.instance( this ).updateChatContentComplete( param.get( 0 ) );
                                Intent sendIntent = new Intent( Define.MSG_SEND_COMPLETE );
                                sendIntent.putExtra( Define.MSGID, param.get( 0 ) );
                                sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                                sendBroadcast( sendIntent );
                                /*
                                 * for ( int i = (m_UIConnectors.size() - 1); i
                                 * >= 0; i-- ) { try { Message msg =
                                 * Message.obtain( null, Define.AM_SEND_COMPLETE
                                 * );
                                 * Bundle b = new Bundle(); b.putString(
                                 * "msgId", param.get( 0 ) ); msg.setData( b );
                                 * m_UIConnectors.get( i ).send( msg ); } catch
                                 * ( RemoteException e ) {
                                 * m_UIConnectors.remove( i ); EXCEPTION( e ); }
                                 * }
                                 */
                        }
                        else if ( command.equals( "NOTIFY" ) && param.size() >= 5 )
                        {
                                // Mobile Chat
                                // 0:msgId, 1:senderId+Nick, 2:date, 3:CHAT,
                                // 4:msg 5:sender Info
                                // note & file & alarm
                                // 0:msgId, 1:senderId+Nick, 2:date, 3:title,
                                // 4:msg, 5:[쪽지]
                                // for(int i=0; i<param.size(); i++)
                                // Log.d( "SERVICE", "NOtIFY i:"+i +
                                // ", param:"+param.get( i ) );
                                if ( param.get( 5 ).equals( "[MESSAGE]" ) )
                                {
                                        String msgId = param.get( 0 );
                                        String senderInfo = param.get( 1 );
                                        // String date = param.get(2);
                                        String subject = param.get( 3 );
                                        String contentInfo = param.get( 4 );
                                        String[] senderAr = senderInfo.split( "\n" );
                                        if ( senderAr.length != 3 )
                                        {
                                                send( "SUCCESS\t" + msgId );
                                                return;
                                        }
                                        String senderId = senderAr[0];
                                        String senderName = senderAr[1];
                                        String senderPart = senderAr[2];
                                        String[] contentAr = contentInfo.split( "\n" );
                                        if ( contentAr.length < 5 )
                                        {
                                                send( "SUCCESS\t" + msgId );
                                                return;
                                        }
                                        String messageNumber = contentAr[0];
                                        String imagePath = contentAr[1];
                                        String attachList = contentAr[2];
                                        String receiverList = contentAr[3];
                                        String content = "";
                                        for ( int i = 4; i < contentAr.length; i++ )
                                        {
                                                if ( !content.equals( "" ) ) content += "\n";
                                                content += contentAr[i];
                                        }
                                        String imgFileName = "";
                                        if ( imagePath.indexOf( "/" ) >= 0 )
                                        {
                                                imgFileName = imagePath.substring( imagePath.lastIndexOf( '/' ) + 1 );
                                        }
                                        else if ( imagePath.indexOf( "\\" ) >= 0 )
                                        {
                                                imgFileName = imagePath.substring( imagePath.lastIndexOf( '\\' ) + 1 );
                                        }
                                        else
                                        {
                                                imgFileName = imagePath;
                                        }
                                        Database.instance( this ).insertMessage( messageNumber, senderId, senderName, senderPart, subject, content, attachList,
                                                        receiverList, imgFileName );
                                        send( "SUCCESS\t" + msgId );
                                        String[] valueAr = new String[2];
                                        valueAr[0] = senderName;
                                        valueAr[1] = subject;
                                        Message m = serviceHandler.obtainMessage( Define.AM_NEW_MESSAGE, ( Object ) valueAr );
                                        serviceHandler.sendMessage( m );
                                }
                                else if ( param.get( 3 ).equals( "CHAT" ) && param.size() == 6 )
                                {
                                        String msgId = param.get( 0 );
                                        String senderInfo = param.get( 1 );
                                        String sendDate = param.get( 2 );
                                        String message = param.get( 4 );
                                        String userInfo = param.get( 5 );
                                        String senderId = "";
                                        String senderName = "";
                                        String senderNickName = "";
                                        short type = 0;
                                        for ( int i = 0; i < senderInfo.length(); i++ )
                                        {
                                                if ( senderInfo.charAt( i ) == '\n' ) type++;
                                                else if ( type == 0 ) senderId += senderInfo.charAt( i );
                                                else if ( type == 1 ) senderName += senderInfo.charAt( i );
                                                else if ( type == 2 ) senderNickName += senderInfo.charAt( i );
                                        }
                                        String roomId = "";
                                        String userIds = "";
                                        String userNames = "";
                                        String chatId = "";
                                        type = 0;
                                        for ( int i = 0; i < userInfo.length(); i++ )
                                        {
                                                if ( userInfo.charAt( i ) == '\n' ) type++;
                                                else if ( type == 0 ) roomId += userInfo.charAt( i );
                                                else if ( type == 1 ) userIds += userInfo.charAt( i );
                                                else if ( type == 2 ) userNames += userInfo.charAt( i );
                                                else if ( type == 3 ) chatId += userInfo.charAt( i );
                                        }
                                        ArrayList<ArrayList<String>> result = Database.instance( this ).selectChatRoomInfoByIds( userIds );
                                        if ( result.size() == 0 )
                                        {
                                                if ( message.indexOf( "[READ_COMPLETE]" ) < 0 && !message.equals( "[ROOM_OUT]" )
                                                                && !message.equals( "[ROOM_IN]" ) ) Database.instance( this ).insertChatRoomInfo( roomId,
                                                                userIds, userNames, sendDate, message );
                                        }
                                        else
                                        {
                                                roomId = result.get( 0 ).get( 0 );
                                                if ( message.equals( "[ROOM_OUT]" ) )
                                                {
                                                        Database.instance( this )
                                                                        .updateChatRoomInfo( roomId, sendDate, senderName + getString( R.string.outMessage ),
                                                                                        senderId.equals( Define.getMyId( this ) ) );
                                                        TRACE( "Before UserIds and UserNames : " + userIds + " and " + userNames );
                                                        String[] ar = StringUtil.getOtherIds( userIds, senderId );
                                                        ArrayList<String> idAr = StringUtil.toArray( userIds );
                                                        ArrayList<String> nameAr = StringUtil.toArray( userNames );
                                                        if ( ar.length > 1 )
                                                        {
                                                                String returnIds = "";
                                                                String returnNames = "";
                                                                for ( int i = 0; i < idAr.size(); i++ )
                                                                {
                                                                        if ( !idAr.get( i ).equals( senderId ) )
                                                                        {
                                                                                if ( !returnIds.equals( "" ) )
                                                                                {
                                                                                        returnIds += ",";
                                                                                        returnNames += ",";
                                                                                }
                                                                                returnIds += idAr.get( i );
                                                                                returnNames += nameAr.get( i );
                                                                        }
                                                                }
                                                                Database.instance( this ).updateChatRoomUsers( roomId, returnIds, returnNames );
                                                                TRACE( "After UserIds and UserNames : " + returnIds + " and " + returnNames );
                                                        }
                                                }
                                                else if ( message.equals( "[ROOM_IN]" ) )
                                                {
                                                        String oUserIds = senderId + "," + userIds;
                                                        String resIds = StringUtil.arrange( oUserIds );
                                                        String resNames = senderName + "," + userNames;
                                                        resNames = StringUtil.arrangeNamesByIds( resNames, oUserIds );
                                                        Database.instance( this ).updateChatRoomInfo( roomId, sendDate,
                                                                        senderName + getString( R.string.gsInMessage ),
                                                                        senderId.equals( Define.getMyId( this ) ) );
                                                        // Database.instance(
                                                        // this
                                                        // ).updateChatRoomUsers(
                                                        // roomId, userIds,
                                                        // userNames );
                                                        Database.instance( this ).updateChatRoomUsers( roomId, resIds, resNames );
                                                        TRACE( "RoomIn After UserIds and UserNames : " + userIds + " and " + userNames );
                                                }
                                                else if ( message.indexOf( "[READ_COMPLETE]" ) < 0 )
                                                {
                                                        Database.instance( this ).updateChatRoomInfo( roomId, sendDate, message,
                                                                        senderId.equals( Define.getMyId( this ) ) );
                                                }
                                        }
                                        if ( message.indexOf( "[READ_COMPLETE]" ) < 0 )
                                        {
                                                boolean isDulpicateMsg = false;
                                                ArrayList<ArrayList<String>> ar = Database.instance( this ).selectChatContent( roomId );
                                                if ( ar.size() == 0 ) isDulpicateMsg = false;
                                                else if ( ar.size() > 0 )
                                                {
                                                        for ( int i = 0; i < ar.size(); i++ )
                                                        {
                                                                ArrayList<String> res = ar.get( i );
                                                                if ( (res.get( 5 )).equals( sendDate.trim() ) && (res.get( 6 )).equals( message.trim() ) )
                                                                {
                                                                        isDulpicateMsg = true;
                                                                        break;
                                                                }
                                                        }
                                                }
                                                if ( !isDulpicateMsg )
                                                {
                                                        if ( message.equals( "[ROOM_OUT]" ) ) Database.instance( this ).insertChatContent( chatId, roomId,
                                                                        senderId, senderName, senderNickName, sendDate,
                                                                        senderName + getString( R.string.outMessage ), userIds, true, true );
                                                        else if ( message.equals( "[ROOM_IN]" ) ) Database.instance( this ).insertChatContent( chatId, roomId,
                                                                        senderId, senderName, senderNickName, sendDate,
                                                                        senderName + getString( R.string.gsInMessage ), userIds, true, true );
                                                        else Database.instance( this ).insertChatContent( chatId, roomId, senderId, senderName, senderNickName,
                                                                        sendDate, message, userIds, true, true );
                                                }
                                                send( "SUCCESS\t" + msgId );
                                        }
                                        else
                                        {
                                                TRACE( "ReadComplete : " + chatId + " : " + senderId );
                                                if ( Database.instance( this ).updateChatReadComplete( chatId, senderId ) > 0 ) send( "SUCCESS\t" + msgId );
                                        }
                                        String[] valueAr = new String[6];
                                        if ( message.indexOf( "[READ_COMPLETE]" ) == 0 ) valueAr[0] = chatId;
                                        else if ( message.indexOf( "[ROOM_IN]" ) == 0 || message.indexOf( "[ROOM_OUT]" ) == 0 ) valueAr[0] = senderName;
                                        else valueAr[0] = senderName + getString( R.string.talk );
                                        valueAr[1] = message;
                                        valueAr[2] = userIds;
                                        valueAr[3] = senderId;
                                        valueAr[4] = roomId;
                                        valueAr[5] = chatId;
                                        Message m = serviceHandler.obtainMessage( Define.AM_NEW_CHAT, ( Object ) valueAr );
                                        serviceHandler.sendMessage( m );
                                }
                                else if ( param.size() > 5
                                                && (param.get( 5 ).startsWith( getString( R.string.sndreceive ) ) || param.get( 5 ).startsWith(
                                                                getString( R.string.snd ) )) )
                                {
                                        try
                                        {
                                                String msgId = param.get( 0 );
                                                String sender = param.get( 1 );
                                                String sendDate = param.get( 2 );
                                                String title = param.get( 3 );
                                                String content = param.get( 4 );
                                                String url = "";
                                                if ( param.size() == 6 ) url = param.get( 5 );
                                                if ( sender.indexOf( Define.getMyId( this ) ) == -1 )
                                                {
                                                        boolean isDulpicateMsg = isChatExist( msgId );
                                                        if ( !isDulpicateMsg )
                                                        {
                                                                if ( content.indexOf( getString( R.string.sndreceive ) ) >= 0 )
                                                                {
                                                                        content = content.substring( content.indexOf( getString( R.string.sndreceive ) ) + 7 );
                                                                        content.replace( "\n", "<br>" );
                                                                }
                                                                // Database.instance(
                                                                // this
                                                                // ).insertAlarm(
                                                                // msgId,
                                                                // Define.getMyId(
                                                                // this ),
                                                                // sender,
                                                                // sendDate,
                                                                // content,
                                                                // getString(
                                                                // R.string.sndreceive
                                                                // ), url );
                                                                Database.instance( this ).insertAlarm( msgId, Define.getMyId( this ), sender, sendDate, title,
                                                                                content, url );
                                                                send( "SUCCESS\t" + msgId );
                                                                String[] valueAr = new String[2];
                                                                if ( sender.indexOf( "\n" ) >= 0 )
                                                                {
                                                                        valueAr[0] = sender.substring( sender.indexOf( "\n" ) + 1 );
                                                                        if ( valueAr[0].indexOf( "\n" ) >= 0 ) valueAr[0] = valueAr[0].substring( 0,
                                                                                        valueAr[0].indexOf( "\n" ) ); // 2015-05-14
                                                                        // valueAr[0]
                                                                        // =
                                                                        // valueAr[0].substring(0,
                                                                        // sender.indexOf(
                                                                        // "\n"
                                                                        // )-1);
                                                                }
                                                                else valueAr[0] = sender;
                                                                valueAr[1] = content.replace( "\n", "<br>" );
                                                                // valueAr[1] =
                                                                // "<"+title +
                                                                // ">" + "<br>"
                                                                // + content;
                                                                Message m = serviceHandler.obtainMessage( Define.AM_NEW_NOTIFY, ( Object ) valueAr );
                                                                serviceHandler.sendMessage( m );
                                                        }
                                                }
                                                else send( "SUCCESS\t" + msgId );
                                        }
                                        catch ( Exception e )
                                        {
                                                e.printStackTrace();
                                        }
                                }
                                else
                                {
                                        String msgId = param.get( 0 );
                                        String sender = param.get( 1 );
                                        String sendDate = param.get( 2 );
                                        String title = param.get( 3 );
                                        String content = param.get( 4 );
                                        String url = "";
                                        if ( param.size() == 6 ) url = param.get( 5 );
                                        Database.instance( this ).insertAlarm( msgId, Define.getMyId( this ), sender, sendDate, title, content, url );
                                        send( "SUCCESS\t" + msgId );
                                        String[] valueAr = new String[2];
                                        valueAr[0] = sender;
                                        valueAr[1] = title;
                                        Log.d( "Title", title );
                                        Log.d( "Content", content );
                                        Message m = serviceHandler.obtainMessage( Define.AM_NEW_NOTIFY, ( Object ) valueAr );
                                        serviceHandler.sendMessage( m );
                                }
                        }
                        else if ( command.equals( "Name" ) && param.size() == 2 && param.get( 0 ).equals( Define.getMyId( this ) ) )
                        {
                        		//제갈량#차장#아이페이지온#0702003014#Android#01012345678#hansy##1#484e1375-478d-4125-bdf4-461ac84175de#
	                       		Log.d(TAG, "Name : " + param.get(1));
                                Database.instance( this ).updateConfig( "USERNAME", StringUtil.getNamePosition( param.get( 1 ) ) );
                                Define.setMyNameWithInfo( param.get( 1 ) );
                                connectedId = param.get( 0 );
                                Message m = serviceHandler.obtainMessage( Define.AM_NAME, null );
                                serviceHandler.sendMessage( m );
                                
                                String[] nameAr = param.get(1).split("#");
                                if ( nameAr.length >= 9 && !nameAr[9].equals("") )
                                {
                                	boolean needRestart = false;
                                	if ( !nameAr[9].equals(Database.instance(this).selectConfig("UUID")) )
                                	{
                                		needRestart = true;
                                	}
                                	
                                	Log.d(TAG, "CheckRestart : " + needRestart);
                                	Database.instance(this).updateConfig("UUID", nameAr[9]);
                                	Database.instance(this).updateConfig("PHONENUMBER", nameAr[3]);
                                	
                                	Intent i = new Intent(getApplicationContext(), kr.co.ultari.atsmart.ipocall.CallService.class);
                                	ComponentName name = startService(i);
                                	Log.d(TAG, "Started : " + name);
                                	
                                	if ( needRestart )
                                	{
	                                	i = new Intent(Define.IPG_CALL_ACTION);
	                                    i.putExtra("Action", "RESTART");
	                                    sendBroadcast(i);
                                	}
                                }
                        }
                        else if ( command.equals( "Nick" ) && param.size() == 1 )
                        {
                                Database.instance( this ).updateConfig( "USERNICKNAME", param.get( 0 ) );
                                Define.setMyNickName( param.get( 0 ) );
                        }
                        else if ( command.equals( "UserName" ) && param.size() >= 2 )
                        {
                                Intent sendIntent = new Intent( Define.MSG_USER_POPUP );
                                sendIntent.putExtra( Define.USERID, param.get( 0 ) );
                                sendIntent.putExtra( Define.USERNAME, param.get( 1 ) );
                                sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                                sendBroadcast( sendIntent );
                                /*
                                 * for ( int i = (m_UIConnectors.size() - 1); i
                                 * >= 0; i-- ) { try { Message msg =
                                 * Message.obtain( null, Define.AM_USER_INFO );
                                 * Bundle b = new Bundle(); b.putString(
                                 * "userId", param.get( 0 ) ); b.putString(
                                 * "userName", param.get( 1 ) );
                                 * msg.setData( b );
                                 * m_UIConnectors.get( i ).send( msg ); } catch
                                 * ( RemoteException e ) {
                                 * m_UIConnectors.remove( i ); EXCEPTION( e ); }
                                 * }
                                 */
                        }
                        else if ( command.equals( "LOGOUT_SERVICE" ) )
                        {
                                Database.instance( getApplicationContext() ).updateConfig( "USERID", "" );
                                Database.instance( getApplicationContext() ).updateConfig( "USERPASSWORD", "" );
                                Database.instance( getApplicationContext() ).updateConfig( "USERNAME", "" );
                                Database.instance( getApplicationContext() ).updateConfig( "USERNICKNAME", "" );
                                Define.setMyId( "" );
                                Define.setMyPW( "" );
                                // 채팅이랑 알림들 모두 삭제, 이미지 포함
                                Database.instance( getApplicationContext() ).deleteAllData();
                                deleteAllImage();
                                Intent sendIntent = new Intent( Define.AM_LOGOUT );
                                sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                                sendBroadcast( sendIntent );
                                stopService( new Intent( getApplicationContext(), kr.co.ultari.atsmart.basic.service.AtSmartService.class ) );
                        }
                        else if ( command.equals( "CONFIG" ) && param.size() == 2 )
                        {
                                if ( param.get( 0 ).equals( "WIFI_MOBILE_IP" ) )
                                {
                                        Define.PRIVATE_SERVER_IP = param.get( 1 );
                                        Database.instance( this ).updateConfig( "PRIVATE_SERVER_IP", Define.PRIVATE_SERVER_IP );
                                }
                                else if ( param.get( 0 ).equals( "WIFI_MOBILE_PORT" ) )
                                {
                                        Define.PRIVATE_SERVER_PORT = param.get( 1 );
                                        Database.instance( this ).updateConfig( "PRIVATE_SERVER_PORT", Define.PRIVATE_SERVER_PORT );
                                }
                                else if ( param.get( 0 ).equals( "WIFI_PROXY_IP" ) )
                                {
                                        Define.PRIVATE_PROXY_IP = param.get( 1 );
                                        Database.instance( this ).updateConfig( "PRIVATE_PROXY_IP", Define.PRIVATE_PROXY_IP );
                                }
                                else if ( param.get( 0 ).equals( "WIFI_PROXY_PORT" ) )
                                {
                                        Define.PRIVATE_PROXY_PORT = param.get( 1 );
                                        Database.instance( this ).updateConfig( "PRIVATE_PROXY_PORT", Define.PRIVATE_PROXY_PORT );
                                }
                                else if ( param.get( 0 ).equals( "WAN_MOBILE_IP" ) )
                                {
                                        Define.PUBLIC_SERVER_IP = param.get( 1 );
                                        Database.instance( this ).updateConfig( "PUBLIC_SERVER_IP", Define.PUBLIC_SERVER_IP );
                                }
                                else if ( param.get( 0 ).equals( "WAN_MOBILE_PORT" ) )
                                {
                                        Define.PUBLIC_SERVER_PORT = param.get( 1 );
                                        Database.instance( this ).updateConfig( "PUBLIC_SERVER_PORT", Define.PUBLIC_SERVER_PORT );
                                }
                                else if ( param.get( 0 ).equals( "WAN_PROXY_IP" ) )
                                {
                                        Define.PUBLIC_PROXY_IP = param.get( 1 );
                                        Database.instance( this ).updateConfig( "PUBLIC_PROXY_IP", Define.PUBLIC_PROXY_IP );
                                }
                                else if ( param.get( 0 ).equals( "WAN_PROXY_PORT" ) )
                                {
                                        Define.PUBLIC_PROXY_PORT = param.get( 1 );
                                        Database.instance( this ).updateConfig( "PUBLIC_PROXY_PORT", Define.PUBLIC_PROXY_PORT );
                                }
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        private void deleteAllImage()
        {
                try
                {
                        ArrayList<ArrayList<String>> chatArr = Database.instance( getApplicationContext() ).selectChatContent( null );
                        for ( int i = 0; i < chatArr.size(); i++ )
                        {
                                ArrayList<String> ar = chatArr.get( i );
                                if ( ar != null )
                                {
                                        String ext = ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) + 1 );
                                        if ( ar.get( 6 ).indexOf( "ATTACH://" ) >= 0
                                                        && (ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" )
                                                                        || ext.equalsIgnoreCase( "png" ) || ext.equalsIgnoreCase( "bmp" )) )
                                        {
                                                File previewFile = new File( getFilesDir(), "small_" + ar.get( 0 )
                                                                + ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) ) );
                                                if ( previewFile.exists() )
                                                {
                                                        previewFile.delete();
                                                        previewFile = null;
                                                }
                                                File originalFile = new File( getFilesDir(), ar.get( 0 )
                                                                + ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) ) );
                                                if ( originalFile.exists() )
                                                {
                                                        originalFile.delete();
                                                        originalFile = null;
                                                }
                                        }
                                }
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public boolean isChatExist( String sChatId )
        {
                ArrayList<ArrayList<String>> array = Database.instance( Define.getContext() ).selectAlarm( null );
                if ( array == null ) return false;
                if ( array.size() > 0 )
                {
                        int size = array.size();
                        for ( int j = 0; j < size; j++ )
                        {
                                ArrayList<String> ar = array.get( j );
                                if ( (ar.get( 0 ).trim()).equals( sChatId.trim() ) ) return true;
                        }
                }
                return false;
        }
        private static final String TAG = "/AtSmart/AtSmartService";

        public void TRACE( String s )
        {
                if ( !Define.useTrace ) return;
                android.util.Log.i( TAG, s );
        }

        public void LOG( String msg )
        {
                if ( Define.saveErrorLog )
                {
                        java.io.FileWriter fw = null;
                        java.io.PrintWriter pw = null;
                        try
                        {
                                fw = new java.io.FileWriter(
                                                android.os.Environment.getExternalStoragePublicDirectory( android.os.Environment.DIRECTORY_DOWNLOADS )
                                                                + java.io.File.separator + "AtSmartFmcLog.txt", true );
                                pw = new java.io.PrintWriter( fw, false );
                                pw.print( "[" + new java.util.Date() + "]" + " msg:" + msg + "\n" );
                                pw.flush();
                        }
                        catch ( Exception ie )
                        {
                                ie.printStackTrace();
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
        private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive( Context context, Intent intent )
                {
                        String action = intent.getAction();
                        TRACE( "Broadcast Action:" + action );
                        if ( action.equals( Define.MSG_SEND_NICK ) )
                        {
                                String nickStr = intent.getStringExtra( "NICK" );
                                try
                                {
                                        send( "Nick\t" + nickStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        else if( action.equals( Define.MSG_PASSWORD_CHANGE ))
                        {
                                String talkStr = intent.getStringExtra( Define.MESSAGE );
                                try
                                {
                                        if ( talkStr != null ) 
                                        {
                                                send( talkStr );
                                        }
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        else if ( action.equals( Define.MSG_USERINFO ) )
                        {
                                String talkStr = intent.getStringExtra( Define.MESSAGE );
                                try
                                {
                                        if ( talkStr != null ) send( talkStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        else if ( action.equals( Define.MSG_NEW_CHAT ) )
                        {
                                String talkStr = intent.getStringExtra( "MESSAGE" );
                                String msgId = intent.getStringExtra( "MESSAGEID" );
                                try
                                {
                                        send( "NOTIFY\t" + msgId + "\t" + talkStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        else if ( action.equals( Define.MSG_READ_COMPLETE ) )
                        {
                                String talkStr = intent.getStringExtra( "MESSAGE" );
                                try
                                {
                                        send( "NOTIFY\t[RC]_" + StringUtil.getNowDateTime() + "\t" + talkStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        else if ( action.equals( Define.MSG_RESTART_SERVICE ) )
                        {
                                String userId = intent.getStringExtra( "USERID" );
                                String m_sMustRestart = intent.getStringExtra( "MUST_RESTART" );
                                boolean m_bMustRestart = false;
                                if ( m_sMustRestart != null ) m_bMustRestart = true;
                                TRACE( "Check ID : " + userId + " == " + connectedId + "(" + sc + ")" );
                                if ( !m_bMustRestart && userId != null && userId.equals( connectedId ) ) return;
                                if ( sc != null )
                                {
                                        noWait = true;
                                        Log.d( TAG, "Connectinn RESTART : " + StringUtil.getNowDateTime() );
                                        try
                                        {
                                                sc.close();
                                                sc = null;
                                        }
                                        catch ( Exception e )
                                        {}
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
                                }
                                // }
                        }
                        // 2015-03-01 myFolder edit
                        else if ( action.equals( Define.MSG_MYFOLDER_USER_ADD ) )
                        {
                                String rcvStr = intent.getStringExtra( "MESSAGE" );
                                try
                                {
                                        // Log.d( "Service",
                                        // "BR[MSG_MYFOLDER_USER_ADD] ok! ->"+"MYFOLDER_USER_ADD\t"
                                        // + rcvStr );
                                        send( "MYFOLDER_USER_ADD\t" + rcvStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        else if ( action.equals( Define.MSG_MYFOLDER_USER_DEL ) )
                        {
                                String rcvStr = intent.getStringExtra( "MESSAGE" );
                                try
                                {
                                        TRACE( "Broadcast [MSG_MYFOLDER_USER_DEL]" );
                                        send( "MYFOLDER_USER_DEL\t" + rcvStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        else if ( action.equals( Define.MSG_MYFOLDER_GROUP_ADD ) )
                        {
                                String rcvStr = intent.getStringExtra( "MESSAGE" );
                                try
                                {
                                        TRACE( "Broadcast [MSG_MYFOLDER_GROUP_ADD]" );
                                        send( "MYFOLDER_GROUP_ADD\t" + rcvStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        else if ( action.equals( Define.MSG_MYFOLDER_GROUP_MOD ) )
                        {
                                String rcvStr = intent.getStringExtra( "MESSAGE" );
                                try
                                {
                                        // Log.d( "Service",
                                        // "BR[MSG_MYFOLDER_GROUP_MOD] ok!" );
                                        send( "MYFOLDER_GROUP_MOD\t" + rcvStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        else if ( action.equals( Define.MSG_MYFOLDER_GROUP_DEL ) )
                        {
                                String rcvStr = intent.getStringExtra( "MESSAGE" );
                                try
                                {
                                        // Log.d( "Service",
                                        // "BR[MSG_MYFOLDER_GROUP_DEL] ok!" );
                                        send( "MYFOLDER_GROUP_DEL\t" + rcvStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        else if ( action.equals( Define.MSG_MYFOLDER_SUB_GROUP_ADD ) )
                        {
                                String rcvStr = intent.getStringExtra( "MESSAGE" );
                                try
                                {
                                        // Log.d( "Service",
                                        // "BR[MSG_MYFOLDER_SUB_GROUP_ADD] ok!"
                                        // );
                                        send( "MYFOLDER_SUB_GROUP_ADD\t" + rcvStr );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        else if ( action.equals( Define.FMC_CALL_STATE ) )
                        {
                                int state = Integer.parseInt( intent.getStringExtra( "call_state" ) );
                                if ( state == 0 )
                                {
                                        Log.e( "SERVICE", "FMC STATE Idle:" + state );
                                }
                                else
                                {
                                        Log.e( "SERVICE", "FMC STATE Not Idle:" + state );
                                }
                        }
                        else if ( action.equals( WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION ) )
                        {
                                Log.d( "Service", "NetworkChanged" );
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
                        }
                        /*
                         * else if (action.equals(Define.MSG_NETWORK_CHANGE)) {
                         * Log.d("Service", "NetworkChanged");
                         * Define.checkWifiServerAvailable();
                         * if ( sc != null ) { try { sc.close(); sc = null; }
                         * catch ( IOException e ) {} } }
                         */
                }
        };
        boolean fmcIsBind = false;

        public void connectFMC()
        {
                try
                {
                        if ( !fmcIsBind )
                        {
                                fmcIsBind = bindService( new Intent( ISMVService.class.getName() ), mSerConn, Context.BIND_AUTO_CREATE );
                                if ( fmcIsBind ) TRACE( "fmc is bind" );
                                else TRACE( "fmc is not bind" );
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public void disconnectFMC()
        {
                if ( fmcIsBind )
                {
                        fmcIsBind = false;
                        unbindService( mSerConn );
                }
        }
        private ServiceConnection mSerConn = new ServiceConnection() {
                ISMVService mSMVService;

                public void onServiceDisconnected( ComponentName p_name )
                {
                        TRACE( "FMC service Disconnected" );
                        mSMVService = null;
                }

                public void onServiceConnected( ComponentName name, IBinder service )
                {
                        mSMVService = ISMVService.Stub.asInterface( service );
                        String strWeWorkInfo = null;
                        try
                        {
                                strWeWorkInfo = mSMVService.getWeWorkInfo();
                                if ( strWeWorkInfo != null )
                                {
                                        String[] parse = strWeWorkInfo.split( ":" );
                                        if ( parse.length < 5 ) return;
//                                        if ( Define.useFMCProvisioning )
//                                        {
//	                                        Define.PRIVATE_SERVER_IP = parse[2];
//	                                        Define.PRIVATE_SERVER_PORT = parse[3];
//	                                        Define.PUBLIC_SERVER_IP = parse[4];
//	                                        Define.PUBLIC_SERVER_PORT = parse[5];
//                                        }
                                        Define.isFmcConnected = true;
                                }
                        }
                        catch ( RemoteException e )
                        {
                                e.printStackTrace();
                        }
                }
        };
}
