package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ToggleButton;

public class OptionDialog extends MessengerActivity implements OnClickListener {
        private ToggleButton tBvib, tBsound, tBkeyboard, tBpush;
        private Button btnChatSound, btnAlarmSound, btnCancel, btnOK;
        private TextView tvChatSound, tvAlarmSound, tvTitle, tvGeneral, tvSound, tvVib, tvBgm, tvKeyboard, tvPush, tvVolume, tvOut1, tvOut2;
        private String isChkVib, isChkSound, isChkKeyboard, isChkPush;
        private Uri chatSoundUri = null, alarmSoundUri = null;
        private SeekBar seekVolumn;
        private static final int CHAT_SOUND_REQUEST_CODE = 999;
        private static final int ALARM_SOUND_REQUEST_CODE = 888;

        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.activity_option );
                
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE ); //2016-12-13
                
                tvTitle = ( TextView ) findViewById( R.id.custom_title_sub );
                tvGeneral = ( TextView ) findViewById( R.id.option_general_title );
                tvSound = ( TextView ) findViewById( R.id.option_sound_title );
                tvVib = ( TextView ) findViewById( R.id.option_vib_title );
                tvBgm = ( TextView ) findViewById( R.id.option_bmg_title );
                tvKeyboard = ( TextView ) findViewById( R.id.option_keyboard_title );
                tvPush = ( TextView ) findViewById( R.id.option_push_title );
                tvVolume = ( TextView ) findViewById( R.id.option_volume_title );
                tvOut1 = ( TextView ) findViewById( R.id.tv_out1 );
                tvOut2 = ( TextView ) findViewById( R.id.tv_out2 );
                tvTitle.setTypeface( Define.tfBold );  
                tvGeneral.setTypeface( Define.tfRegular );  
                tvSound.setTypeface( Define.tfRegular );  
                tvVib.setTypeface( Define.tfRegular );  
                tvBgm.setTypeface( Define.tfRegular );  
                tvKeyboard.setTypeface( Define.tfRegular );  
                tvPush.setTypeface( Define.tfRegular );  
                tvVolume.setTypeface( Define.tfRegular );  
                tvOut1.setTypeface( Define.tfRegular );  
                tvOut2.setTypeface( Define.tfRegular );  
                btnOK = ( Button ) findViewById( R.id.option_save );
                btnOK.setTypeface( Define.tfRegular );  
                btnOK.setOnClickListener( this );
                btnCancel = ( Button ) findViewById( R.id.option_cancel );
                btnCancel.setTypeface( Define.tfRegular );  
                btnCancel.setOnClickListener( this );
                tBvib = ( ToggleButton ) findViewById( R.id.tbVib );
                tBvib.setTypeface( Define.tfRegular );  
                tBvib.setOnCheckedChangeListener( new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
                        {
                                if ( isChecked ) isChkVib = "ON";
                                else isChkVib = "OFF";
                        }
                } );
                tBsound = ( ToggleButton ) findViewById( R.id.tbSound );
                tBsound.setTypeface( Define.tfRegular );  
                tBsound.setOnCheckedChangeListener( new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
                        {
                                if ( isChecked ) isChkSound = "ON";
                                else isChkSound = "OFF";
                        }
                } );
                tBkeyboard = ( ToggleButton ) findViewById( R.id.tbKeyboard );
                tBkeyboard.setTypeface( Define.tfRegular );  
                tBkeyboard.setOnCheckedChangeListener( new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
                        {
                                if ( isChecked ) isChkKeyboard = "ON";
                                else isChkKeyboard = "OFF";
                        }
                } );
                tBpush = ( ToggleButton ) findViewById( R.id.tbPush );
                tBpush.setTypeface( Define.tfRegular );  
                tBpush.setOnCheckedChangeListener( new OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged( CompoundButton buttonView, boolean isChecked )
                        {
                                if ( isChecked ) isChkPush = "ON";
                                else isChkPush = "OFF";
                        }
                } );
                btnChatSound = ( Button ) findViewById( R.id.btn_chatsound );
                btnAlarmSound = ( Button ) findViewById( R.id.btn_alarmsound );
                btnChatSound.setOnClickListener( this );
                btnAlarmSound.setOnClickListener( this );
                btnOK.setOnClickListener( this );
                btnCancel.setOnClickListener( this );
                tvChatSound = ( TextView ) findViewById( R.id.tv_chatsound );
                tvChatSound.setTypeface( Define.tfBold );  
                tvAlarmSound = ( TextView ) findViewById( R.id.tv_alarmsound );
                tvAlarmSound.setTypeface( Define.tfBold );  
                if ( Define.vibrator.equals( "ON" ) ) tBvib.setChecked( true );
                else tBvib.setChecked( false );
                if ( Define.sound.equals( "ON" ) ) tBsound.setChecked( true );
                else tBsound.setChecked( false );
                if ( Define.keyboard.equals( "ON" ) ) tBkeyboard.setChecked( true );
                else tBkeyboard.setChecked( false );
                if ( Define.push.equals( "ON" ) || Define.push.equals( "0" ) ) tBpush.setChecked( true );
                else tBpush.setChecked( false );
                isChkVib = Define.vibrator;
                isChkSound = Define.sound;
                isChkKeyboard = Define.keyboard;
                isChkPush = Define.push;
                chatSoundUri = Uri.parse( Database.instance( this ).selectConfig( "chatSound" ) );
                alarmSoundUri = Uri.parse( Database.instance( this ).selectConfig( "alarmSound" ) );
                seekVolumn = ( SeekBar ) findViewById( R.id.seekBar );
                final AudioManager audioManager = ( AudioManager ) getSystemService( AUDIO_SERVICE );
                int nMax = audioManager.getStreamMaxVolume( AudioManager.STREAM_RING );
                int nCurrentVolumn = audioManager.getStreamVolume( AudioManager.STREAM_RING );
                seekVolumn.setMax( nMax );
                seekVolumn.setProgress( nCurrentVolumn );
                seekVolumn.setOnSeekBarChangeListener( new OnSeekBarChangeListener() {
                        @Override
                        public void onStopTrackingTouch( SeekBar seekBar )
                        {
                        }

                        @Override
                        public void onStartTrackingTouch( SeekBar seekBar )
                        {
                        }

                        @Override
                        public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
                        {
                                audioManager.setStreamVolume( AudioManager.STREAM_RING, progress, 0 );
                        }
                } );
                try
                {
                        final Ringtone ringtone = RingtoneManager.getRingtone( this, chatSoundUri );
                        String title = ringtone.getTitle( this );
                        tvChatSound.setText( title );
                        // tvChatSound.setTextColor( Color.BLUE );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                try
                {
                        final Ringtone ringtone = RingtoneManager.getRingtone( this, alarmSoundUri );
                        String title = ringtone.getTitle( this );
                        tvAlarmSound.setText( title );
                        // tvAlarmSound.setTextColor( Color.BLUE );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        @Override
        public void onClick( View v )
        {
                if ( v == btnChatSound )
                {
                        Intent i = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER );
                        i.putExtra( RingtoneManager.EXTRA_RINGTONE_TITLE, getString( R.string.chatSoundSelect ) );
                        i.putExtra( RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false );
                        i.putExtra( RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false );
                        i.putExtra( RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION );
                        startActivityForResult( i, CHAT_SOUND_REQUEST_CODE );
                }
                else if ( v == btnAlarmSound )
                {
                        Intent i = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER );
                        i.putExtra( RingtoneManager.EXTRA_RINGTONE_TITLE, getString( R.string.noticeSoundSelect ) );
                        i.putExtra( RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false );
                        i.putExtra( RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false );
                        i.putExtra( RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION );
                        startActivityForResult( i, ALARM_SOUND_REQUEST_CODE );
                }
                else if ( v == btnOK )
                {
                        try
                        {
                                if ( isChkPush.equals( "ON" ) )
                                {
                                        Define.push = "ON";
                                        Database.instance( this ).updateConfig( "push", Define.push );
                                }
                                else
                                {
                                        Define.push = "OFF";
                                        Database.instance( this ).updateConfig( "push", Define.push );
                                }
                                if ( isChkKeyboard.equals( "ON" ) )
                                {
                                        Define.keyboard = "ON";
                                        Database.instance( this ).updateConfig( "keyboard", Define.keyboard );
                                }
                                else
                                {
                                        Define.keyboard = "OFF";
                                        Database.instance( this ).updateConfig( "keyboard", Define.keyboard );
                                }
                                if ( isChkVib.equals( "ON" ) )
                                {
                                        Define.vibrator = "ON";
                                        Database.instance( this ).updateConfig( "vibrator", Define.vibrator );
                                }
                                else
                                {
                                        Define.vibrator = "OFF";
                                        Database.instance( this ).updateConfig( "vibrator", Define.vibrator );
                                }
                                if ( isChkSound.equals( "ON" ) )
                                {
                                        Define.sound = "ON";
                                        Database.instance( this ).updateConfig( "sound", Define.sound );
                                }
                                else
                                {
                                        Define.sound = "OFF";
                                        Database.instance( this ).updateConfig( "sound", Define.sound );
                                }
                                
                                Database.instance( this ).updateConfig( "fontSize", Define.selectedFontSize );
                                if ( chatSoundUri != null )
                                {
                                        Define.chatSoundUri = chatSoundUri.toString();
                                        Database.instance( this ).updateConfig( "chatSound", chatSoundUri.toString() );
                                }
                                if ( alarmSoundUri != null )
                                {
                                        Define.alarmSoundUri = alarmSoundUri.toString();
                                        Database.instance( this ).updateConfig( "alarmSound", alarmSoundUri.toString() );
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                        finish();
                }
                else if ( v == btnCancel )
                {
                        finish();
                }
        }

        @Override
        protected void onActivityResult( int requestCode, int resultCode, Intent data )
        {
                switch ( requestCode )
                {
                        case CHAT_SOUND_REQUEST_CODE :
                                if ( resultCode != 0 )
                                {
                                        chatSoundUri = data.getParcelableExtra( RingtoneManager.EXTRA_RINGTONE_PICKED_URI );
                                        // chatSoundUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(),RingtoneManager.TYPE_NOTIFICATION);
                                        if ( chatSoundUri.toString().indexOf( "content://settings/system/ringtone" ) >= 0 ) chatSoundUri = RingtoneManager
                                                        .getActualDefaultRingtoneUri( getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION );
                                        final Ringtone ringtone = RingtoneManager.getRingtone( this, chatSoundUri );
                                        String title = ringtone.getTitle( this );
                                        tvChatSound.setText( title );
                                }
                                break;
                        case ALARM_SOUND_REQUEST_CODE :
                                if ( resultCode != 0 )
                                {
                                        alarmSoundUri = data.getParcelableExtra( RingtoneManager.EXTRA_RINGTONE_PICKED_URI );
                                        if ( alarmSoundUri.toString().indexOf( "content://settings/system/ringtone" ) >= 0 ) alarmSoundUri = RingtoneManager
                                                        .getActualDefaultRingtoneUri( getApplicationContext(), RingtoneManager.TYPE_NOTIFICATION );
                                        final Ringtone ringtone = RingtoneManager.getRingtone( this, alarmSoundUri );
                                        String title = ringtone.getTitle( this );
                                        tvAlarmSound.setText( title );
                                }
                                break;
                        default :
                                break;
                }
        }
        private static final String TAG = "/AtSmart/OptionDialog";

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
