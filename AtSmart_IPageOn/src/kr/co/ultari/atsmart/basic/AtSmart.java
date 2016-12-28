package kr.co.ultari.atsmart.basic;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import kr.co.ultari.atsmart.basic.control.PinEntryView;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.subview.PinInputDialog;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint( "HandlerLeak" )
public class AtSmart extends Activity {
        public String sUserID = "";
        private String ROOT_PATH = Environment.getExternalStorageDirectory() + "";
        private String ROOTING_PATH_1 = "/system/bin/su";
        private String ROOTING_PATH_2 = "/system/xbin/su";
        private String ROOTING_PATH_3 = "/system/app/SuperUser.apk";
        private String ROOTING_PATH_4 = "/data/data/com.noshufou.android.su";
        private String[] RootFilesPath = new String[] { ROOT_PATH + ROOTING_PATH_1, ROOT_PATH + ROOTING_PATH_2, ROOT_PATH + ROOTING_PATH_3,
                        ROOT_PATH + ROOTING_PATH_4 };

        @Override
        public void onDestroy()
        {
                super.onDestroy();
        }

        /** Called when the activity is first created. */
        @SuppressLint( "NewApi" )
        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                setContentView( R.layout.atsmart );
                try
                {
                        if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE );
                        if ( Define.useRootingCheck )
                        {
                                boolean isRootingFlag = false;
                                try
                                {
                                        Runtime.getRuntime().exec( "su" );
                                        isRootingFlag = true;
                                }
                                catch ( Exception e )
                                {
                                        isRootingFlag = false;
                                }
                                if ( !isRootingFlag )
                                {
                                        isRootingFlag = checkRootingFiles( createFiles( RootFilesPath ) );
                                }
                                if ( isRootingFlag )
                                {
                                	LayoutInflater inflater = getLayoutInflater();
                                    View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                                    TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                    text.setTypeface( Define.tfRegular );
                                    text.setText( getString( R.string.rooting_on_device ) );
                                    Toast toast = new Toast( AtSmart.this );
                                    toast.setGravity( Gravity.CENTER, 0, 0 );
                                    toast.setDuration( Toast.LENGTH_LONG );
                                    toast.setView( layout );
                                    toast.show();
                                    
                                    System.exit( 0 );
                                }
                                else
                                {
                                	 LayoutInflater inflater = getLayoutInflater();
                                     View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                                     TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                     text.setTypeface( Define.tfRegular );
                                     text.setText( getString( R.string.rooting_off_device ) );
                                     Toast toast = new Toast( AtSmart.this );
                                     toast.setGravity( Gravity.CENTER, 0, 0 );
                                     toast.setDuration( Toast.LENGTH_SHORT );
                                     toast.setView( layout );
                                     toast.show();
                                }
                        }
                        NotificationManager nm = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
                        nm.cancel( Define.AtSmartPushNotification );
                        nm.cancel( Define.AtSmartServiceFinished );
                        Intent i = new Intent( "android.intent.action.BADGE_COUNT_UPDATE" );
                        i.putExtra( "badge_count", 0 );
                        i.putExtra( "badge_count_package_name", "kr.co.ultari.atsmart.basic" );
                        i.putExtra( "badge_count_class_name", "kr.co.ultari.atsmart.basic.AtSmart" );
                        sendBroadcast( i );
                        Define.notoficationNumber = 1;
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics( displayMetrics );
                        Define.displayWidth = displayMetrics.widthPixels;
                        Define.displayHeight = displayMetrics.heightPixels;
                        getWindowManager().getDefaultDisplay().getMetrics( displayMetrics );
                        Intent intent = getIntent();
                        if ( intent != null )
                        {
                                if ( intent.getStringExtra( "ID" ) != null )
                                {
                                        Define.setMyId( intent.getStringExtra( "ID" ) );
                                        if ( intent.getStringExtra( "PW" ) == null || intent.getStringExtra( "PW" ).equals( "" ) ) Define.setMyPW( intent
                                                        .getStringExtra( "ID" ) );
                                        else Define.setMyPW( intent.getStringExtra( "PW" ) );
                                }
                        }
                        
                        //2016-04-26
                        initData();
                        
                        switch(Define.SET_COMPANY)
                        {
                                case Define.IPAGEON:
                                        if ( !Define.USE_PIN_MAIN )
                                        {
                                                if ( !Define.getMyId( getApplicationContext() ).equals( "" )
                                                                && (Define.PIN_MAIN_CODE == null || Define.PIN_MAIN_CODE.equals( "" )) )
                                                {
                                                        // PIN 초기 입력창 다시 열기
                                                        String pw = Define.getMyPw( getApplicationContext() ).trim();
                                                        String id = Define.getMyId( getApplicationContext() ).trim();
                                                        Intent selectWindow = new Intent( AtSmart.this, PinInputDialog.class );
                                                        startActivity( selectWindow );
                                                }
                                                else
                                                {
                                                        Intent it = new Intent( getApplicationContext(), MainActivity.class );
                                                        String intentRoomId = getIntent().getStringExtra( "RoomId" );
                                                        if ( intentRoomId != null )
                                                        {
                                                                it.putExtra( "RoomId", intentRoomId );
                                                                Define.isAddUserMode = false;
                                                                if ( MainActivity.alert != null ) MainActivity.alert.finish();
                                                                if ( MainActivity.search != null ) MainActivity.search.finish();
                                                        }
                                                        String intentNotifyId = getIntent().getStringExtra( "NotifyId" );
                                                        if ( intentNotifyId != null ) it.putExtra( "NotifyId", intentNotifyId );
                                                        startActivity( it );
                                                }
                                        }
                                        else
                                        {
                                        		if ( Define.getMyId( getApplicationContext() ).equals( "" ) )
                                                {
                                                        Intent it = new Intent( getApplicationContext(), MainActivity.class );
                                                        String intentRoomId = getIntent().getStringExtra( "RoomId" );
                                                        if ( intentRoomId != null )
                                                        {
                                                                it.putExtra( "RoomId", intentRoomId );
                                                                Define.isAddUserMode = false;
                                                                if ( MainActivity.alert != null ) MainActivity.alert.finish();
                                                                if ( MainActivity.search != null ) MainActivity.search.finish();
                                                        }
                                                        String intentNotifyId = getIntent().getStringExtra( "NotifyId" );
                                                        if ( intentNotifyId != null ) it.putExtra( "NotifyId", intentNotifyId );
                                                        startActivity( it );
                                                }
                                                else
                                                {
                                                        Intent pinIt = new Intent( getApplicationContext(), PinEntryView.class );
                                                        String intentRoomId = getIntent().getStringExtra( "RoomId" );
                                                        if ( intentRoomId != null )
                                                        {
                                                                pinIt.putExtra( "RoomId", intentRoomId );
                                                                Define.isAddUserMode = false;
                                                                if ( MainActivity.alert != null ) MainActivity.alert.finish();
                                                                if ( MainActivity.search != null ) MainActivity.search.finish();
                                                        }
                                                        String intentNotifyId = getIntent().getStringExtra( "NotifyId" );
                                                        if ( intentNotifyId != null ) pinIt.putExtra( "NotifyId", intentNotifyId );
                                                        startActivity( pinIt );
                                                }
                                        }
                                        break;
                                
                                default:
                                        Intent it = new Intent( getApplicationContext(), kr.co.ultari.atsmart.basic.MainActivity.class );
                                        String intentRoomId = getIntent().getStringExtra( "RoomId" );
                                        if ( intentRoomId != null )
                                        {
                                                it.putExtra( "RoomId", intentRoomId );
                                                Define.isAddUserMode = false;
                                                if ( MainActivity.alert != null ) MainActivity.alert.finish();
                                                if ( MainActivity.search != null ) MainActivity.search.finish();
                                        }
                                        String intentNotifyId = getIntent().getStringExtra( "NotifyId" );
                                        if ( intentNotifyId != null ) it.putExtra( "NotifyId", intentNotifyId );
                                        initData();
                                        startActivity( it );
                                        break;
                        }
                        //
                        
                        finish();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        private File[] createFiles( String[] sfiles )
        {
                File[] rootingFiles = new File[sfiles.length];
                for ( int i = 0; i < sfiles.length; i++ )
                {
                        rootingFiles[i] = new File( sfiles[i] );
                }
                return rootingFiles;
        }

        private boolean checkRootingFiles( File... file )
        {
                boolean result = false;
                for ( File f : file )
                {
                        if ( f != null && f.exists() && f.isFile() )
                        {
                                result = true;
                                break;
                        }
                        else
                        {
                                result = false;
                        }
                }
                return result;
        }

        @SuppressLint( "NewApi" )
        private void initData()
        {
                //2016-04-26
                switch(Define.SET_COMPANY)
                {
                        case Define.IPAGEON:
                                String value = Database.instance( getApplicationContext() ).selectConfig( "PIN_MAIN" );
                                if ( value == null || value.equals( "" ) || value.equals( "OFF" ) ) Define.USE_PIN_MAIN = false;
                                else Define.USE_PIN_MAIN = true;

                                Define.PIN_MAIN_CODE = Database.instance( getApplicationContext() ).selectConfig( "PIN_MAIN_CODE" );
                                Define.PIN_PWD_COUNT = Database.instance( getApplicationContext() ).selectConfig( "PIN_COUNT" );
                                break;
                        
                        default:
                                break;
                }
                //
                
                Define.deviceBrand = Build.BRAND;
                TRACE( "deviceBrand:" + Define.deviceBrand );
                
                if ( Define.useResetChatRoomNamePosition )
                {
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get( cal.YEAR );
                        int month = cal.get( cal.MONTH ) + 1;
                        int date = cal.get( cal.DATE );
                        String ret = year + "/" + month + "/" + date;
                        String resetDate = Database.instance( getApplicationContext() ).selectConfig( "DATE" );
                        if ( resetDate == null || resetDate.equals( "" ) )
                        {
                                Define.isResetChatRoomNamePosition = true;
                                Database.instance( getApplicationContext() ).updateConfig( "DATE", year + "/" + month + "/" + date );
                        }
                        else if ( resetDate.equals( ret ) ) Define.isResetChatRoomNamePosition = false;
                        else
                        {
                                Define.isResetChatRoomNamePosition = true;
                                Database.instance( getApplicationContext() ).updateConfig( "DATE", year + "/" + month + "/" + date );
                        }
                }
                
                Define.resetServerInfo( this );
                String mode = Database.instance( getApplicationContext() ).selectConfig( "NETWORKMODE" );
                if ( mode.equals( Integer.toString( Define.NETWORK_MODE_LTE ) ) ) Define.NETWORK_MODE = Define.NETWORK_MODE_LTE;
                else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_WIFI ) ) ) Define.NETWORK_MODE = Define.NETWORK_MODE_WIFI;
                else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_DISABLE ) ) || mode.equals( "" ) ) Define.NETWORK_MODE = Define.NETWORK_MODE_DISABLE;
                SharedPreferences pref = getSharedPreferences( "pref", MODE_PRIVATE );
                pref.getString( "check", "" );
                if ( pref.getString( "check", "" ).isEmpty() )
                {
                        Intent shortcutIntent = new Intent( Intent.ACTION_MAIN );
                        shortcutIntent.addCategory( Intent.CATEGORY_LAUNCHER );
                        shortcutIntent.setClassName( this, getClass().getName() );
                        shortcutIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED );
                        Intent shortIt = new Intent();
                        shortIt.putExtra( Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent );
                        shortIt.putExtra( Intent.EXTRA_SHORTCUT_NAME, getResources().getString( R.string.app_name ) );
                        shortIt.putExtra( Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext( this, R.drawable.icon ) );
                        shortIt.putExtra( "duplicate", false );
                        shortIt.setAction( "com.android.launcher.action.INSTALL_SHORTCUT" );
                        sendBroadcast( shortIt );
                        Define.push = "ON";
                        Database.instance( this ).updateConfig( "push", Define.push );
                        Define.keyboard = "OFF";
                        Database.instance( this ).updateConfig( "keyboard", Define.keyboard );
                        Define.vibrator = "OFF";
                        Database.instance( this ).updateConfig( "vibrator", Define.vibrator );
                        Define.sound = "OFF";
                        Database.instance( this ).updateConfig( "sound", Define.sound );
                        Database.instance( this ).updateConfig( "CALLCONNECTTYPE" , Integer.toString( Define.CALL_TYPE_MODE_TLS ));
                }
                SharedPreferences.Editor editor = pref.edit();
                editor.putString( "check", "install" );
                editor.commit();
                ArrayList<ArrayList<String>> array = Database.instance( getApplicationContext() ).selectChatRoomInfo( null );
                if ( array != null )
                {
                        for ( int j = 0; j < array.size(); j++ )
                        {
                                ArrayList<String> result = array.get( j );
                                int count = StringUtil.getChatRoomCount( result.get( 1 ) );
                                if ( count == 2 )
                                {
                                        boolean isExist = false;
                                        ArrayList<ArrayList<String>> chatArr = Database.instance( getApplicationContext() ).selectChatContent( result.get( 0 ) );
                                        for ( int k = 0; k < chatArr.size(); k++ )
                                        {
                                                ArrayList<String> chat = chatArr.get( k );
                                                if ( chat != null )
                                                {
                                                        if ( chat.get( 6 ).indexOf( getString( R.string.outMessage ) ) >= 0 ) isExist = true;
                                                }
                                        }
                                        if ( isExist )
                                        {
                                                if ( !result.get( 0 ).startsWith( "GROUP_" ) ) updateRoomId( result.get( 2 ), result.get( 0 ), result.get( 1 ),
                                                                true );
                                        }
                                        else
                                        {
                                                String parse = StringUtil.arrange( result.get( 1 ) );
                                                String newRoomId = parse.replace( ",", "_" );
                                                if ( !result.get( 0 ).equals( newRoomId ) ) updateRoomId( result.get( 2 ), result.get( 0 ), result.get( 1 ),
                                                                false );
                                        }
                                }
                                else if ( count >= 3 )
                                {
                                        if ( !result.get( 0 ).startsWith( "GROUP_" ) ) updateRoomId( result.get( 2 ), result.get( 0 ), result.get( 1 ), true );
                                }
                        }
                }
        }
        private static final String TAG = "/AtSmart/AtSmart";

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

        public void updateRoomId( String userNames, String oldRoomId, String userIds, boolean isGroupChat )
        {
                String newRoomId = null;
                if ( isGroupChat )
                {
                        String parse = oldRoomId.substring( oldRoomId.indexOf( "_" ) + 1 );
                        newRoomId = "GROUP_" + parse; // GROUP_14xxxxx
                }
                else
                {
                        String parse = StringUtil.arrange( userIds ); // TEST1,TEST2
                        newRoomId = parse.replace( ",", "_" );
                }
                ArrayList<ArrayList<String>> ar = Database.instance( getApplicationContext() ).selectChatRoomInfo( newRoomId );
                if ( ar.size() == 0 )
                {
                        // Log.d( "AtSMart", "newRoom not Exist.. create!" );
                        Database.instance( getApplicationContext() ).insertChatRoomInfo( newRoomId, userIds, userNames, StringUtil.getNowDateTime(),
                                        getString( R.string.newRoom ) );
                }
                boolean send = false;
                boolean reserved = false;
                ArrayList<ArrayList<String>> array = Database.instance( getApplicationContext() ).selectChatContent( oldRoomId );
                for ( int i = 0; i < array.size(); i++ )
                {
                        ArrayList<String> oldData = array.get( i );
                        if ( oldData.get( 7 ).equals( "Y" ) ) send = true;
                        else send = false;
                        if ( oldData.get( 9 ).equals( "Y" ) ) reserved = true;
                        else reserved = false;
                        Database.instance( getApplicationContext() ).insertChatContent( oldData.get( 0 ), newRoomId, oldData.get( 2 ), oldData.get( 3 ),
                                        oldData.get( 4 ), oldData.get( 5 ), oldData.get( 6 ), oldData.get( 8 ), send, reserved );
                }
                Database.instance( getApplicationContext() ).deleteChatBysRoomId( oldRoomId );
                Database.instance( getApplicationContext() ).deleteChatRoomById( oldRoomId );
        }
}
