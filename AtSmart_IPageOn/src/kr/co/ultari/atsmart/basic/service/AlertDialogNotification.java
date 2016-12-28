package kr.co.ultari.atsmart.basic.service;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

@SuppressLint( "HandlerLeak" )
public class AlertDialogNotification extends Dialog {
        Context context;
        public static String title;
        public static String message;
        private TextView m_txtMessage;
        private TextView m_Title;

        public AlertDialogNotification( Context context )
        {
                super( context );
                Log.d( "NotifyType", "NotifyAletDialogNotification" );
                this.context = context;
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                setContentView( R.layout.alert_new_dialog );
                m_txtMessage = ( TextView ) findViewById( R.id.alertMessage );
                message = message.replaceAll( "\n", " " );
                if ( message.indexOf( "ATTACH://" ) == 0 ) m_txtMessage.setText( StringUtil.getChatTypeString( message ) + " 파일이 도착했습니다." );
                else m_txtMessage.setText( message );
                m_Title = ( TextView ) findViewById( R.id.alertTitle );
                m_Title.setText( title );
                AudioManager audioManager = ( AudioManager ) context.getSystemService( Context.AUDIO_SERVICE );
                switch ( audioManager.getRingerMode() )
                {
                case AudioManager.RINGER_MODE_VIBRATE :
                        if ( Define.vibrator.equals( "ON" ) )
                        {
                                Vibrator mVib = ( Vibrator ) context.getSystemService( Context.VIBRATOR_SERVICE );
                                mVib.vibrate( 1000 );
                        }
                        break;
                case AudioManager.RINGER_MODE_NORMAL :
                        if ( Define.sound.equals( "ON" ) )
                        {
                                if ( message.indexOf( context.getString( R.string.note ) ) >= 0 || message.indexOf( "ATTACH" ) >= 0
                                                || message.indexOf( "TITLE" ) >= 0 || message.indexOf( context.getString( R.string.notice ) ) >= 0 )
                                {
                                        if ( Define.alarmSoundUri.equals( "" ) || Define.alarmSoundUri.equals( "0" ) || Define.alarmSoundUri == null )
                                        {
                                                Uri defaultNotificationUri = RingtoneManager.getActualDefaultRingtoneUri( context,
                                                                RingtoneManager.TYPE_NOTIFICATION );
                                                if ( defaultNotificationUri != null ) Define.alarmSoundUri = defaultNotificationUri.toString();
                                        }
                                        Ringtone ringtone = RingtoneManager.getRingtone( context, Uri.parse( Define.alarmSoundUri ) );
                                        ringtone.play();
                                }
                                else
                                {
                                        // content://media/internal/audio/media/16
                                        if ( Define.chatSoundUri.equals( "" ) || Define.chatSoundUri.equals( "0" ) || Define.chatSoundUri == null )
                                        {
                                                Uri defaultNotificationUri = RingtoneManager.getActualDefaultRingtoneUri( context,
                                                                RingtoneManager.TYPE_NOTIFICATION );
                                                if ( defaultNotificationUri != null ) Define.chatSoundUri = defaultNotificationUri.toString();
                                        }
                                        Ringtone ringtone = RingtoneManager.getRingtone( context, Uri.parse( Define.chatSoundUri ) );
                                        ringtone.play();
                                }
                        }
                        if ( Define.vibrator.equals( "ON" ) )
                        {
                                Vibrator mVib = ( Vibrator ) context.getSystemService( Context.VIBRATOR_SERVICE );
                                mVib.vibrate( 1000 );
                        }
                        break;
                case AudioManager.RINGER_MODE_SILENT :
                        if ( Define.vibrator.equals( "ON" ) )
                        {
                                Vibrator mVib = ( Vibrator ) context.getSystemService( Context.VIBRATOR_SERVICE );
                                mVib.vibrate( 1000 );
                        }
                        break;
                }
                Handler handler = new Handler() {
                        public void handleMessage( Message msg )
                        {
                                try
                                {
                                        dismiss();
                                }
                                catch ( Exception e )
                                {
                                        e.printStackTrace();
                                }
                        }
                };
                handler.sendEmptyMessageDelayed( 0, 1000 );
        }
        private static final String TAG = "/AtSmart/AlertDialogNotification";

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
}
