package kr.co.ultari.atsmart.basic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import com.smv.service.ISMVService;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.service.AlertDialogNotification;
import kr.co.ultari.atsmart.basic.service.AtSmartService;
import kr.co.ultari.atsmart.basic.service.AtSmartServiceStarter;
import kr.co.ultari.atsmart.basic.service.NetworkStatusReceiver;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.subview.ExLauncher;
import kr.co.ultari.atsmart.basic.subview.SendMessageView.MessageUserData;
import kr.co.ultari.atsmart.basic.util.AppUtil;
import kr.co.ultari.atsmart.basic.util.BackPressCloseHandler;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import kr.co.ultari.atsmart.basic.view.FavoriteView;
import kr.co.ultari.atsmart.basic.view.BuddyView;
import kr.co.ultari.atsmart.basic.view.CallView;
import kr.co.ultari.atsmart.basic.view.ChatWindow;
import kr.co.ultari.atsmart.basic.view.ConfigView;
import kr.co.ultari.atsmart.basic.view.GroupSearchView;
import kr.co.ultari.atsmart.basic.view.KeypadView;
import kr.co.ultari.atsmart.basic.view.LogoDialog;
import kr.co.ultari.atsmart.basic.view.MessageView;
import kr.co.ultari.atsmart.basic.view.NotifyView;
import kr.co.ultari.atsmart.basic.view.OrganizationView;
import kr.co.ultari.atsmart.basic.view.ContactView;
import kr.co.ultari.atsmart.basic.view.TalkView;
import kr.co.ultari.atsmart.basic.view.MainViewTabFragment.TabListener;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telecom.Call;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;

@SuppressLint( "HandlerLeak" )
public class MainActivity extends FragmentActivity implements TabListener, Runnable, OnClickListener {
        public ViewPager mPager;
        public FragmentPagerAdapter adapterViewPager;
        private BackPressCloseHandler backPressCloseHandler;
        public static GroupSearchView search = null;
        public static kr.co.ultari.atsmart.basic.service.AlertDialog alert = null;
        public static ChatWindow cw = null;
        public Messenger m_AtSmartService = null;
        final Messenger mMessenger = new Messenger( new IncomingHandler() );
        boolean mIsBound = false;
        public static Context context;
        public static MainActivity mainActivity = null;
        private Timer timer;
        public static ArrayList<Message> serviceMessageQueue = null;
        public AtSmartServiceStarter receiver = null;
        // private static LinearLayout topLayout = null;
        private Button btnTabCancel, btnTabChat;
        private ISMVService mSMVService = null;
        private boolean fmcIsBind = false;
        public ImageView swipe_left, swipe_right;
        public RelativeLayout layout_fragmentTab;
        public Button btnLauncher;
        
        public static MainActivity Instance()
        {
                if ( mainActivity == null ) mainActivity = new MainActivity();
                return mainActivity;
        }

        @Override
        protected void onDestroy()
        {
                super.onDestroy();
                TRACE( "onDestroy Main Avtivity" );
                try
                {
                        unregisterReceiver( mBroadcastReceiver );
                        unregisterReceiver( receiver );
                        /*
                         * if ( Database.instance(getApplicationContext()).selectConfig("RETAIN").equals("N") )
                         * {
                         * Define.setMyId("");
                         * Define.setMyPW("");
                         * Database.instance(getApplicationContext()).updateConfig("USERID", "");
                         * Database.instance(getApplicationContext()).updateConfig("USERPASSWORD", "");
                         * Database.instance(getApplicationContext()).updateConfig("USERNAME", "");
                         * Database.instance(getApplicationContext()).updateConfig("USERNICKNAME", "");
                         * timerTask.cancel();
                         * Message msg = Message.obtain(null, Define.AM_STOP_SERVICE);
                         * serviceMessageQueue.add(msg);
                         * while ( m_AtSmartService != null && serviceMessageQueue.size() > 0 )
                         * {
                         * try
                         * {
                         * m_AtSmartService.send( serviceMessageQueue.remove( 0 ) );
                         * }
                         * catch(Exception e)
                         * {
                         * EXCEPTION(e);
                         * break;
                         * }
                         * }
                         * stopService(new Intent(getApplicationContext(), kr.co.ultari.atsmart.basic.service.AtSmartService.class));
                         * }
                         */
                        if ( cw == null ) Define.clearUserImages();
                        ActionManager.talkTabButton = null;
                        ActionManager.notifyTabButton = null;
                        ActionManager.moreTabButton = null;
                        Define.isVisible = false;
                        Define.isBuddyAddMode = false;
                        timerTask.cancel();
                        serviceMessageQueue.clear();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                try
                {
                        doUnbindService();
                        if ( m_AtSmartService != null )
                        {
                                Message msg = Message.obtain( null, Define.AM_UNREGISTER_CLIENT );
                                msg.replyTo = mMessenger;
                                m_AtSmartService.send( msg );
                        }
                        if ( fmcIsBind )
                        {
                                Log.e( "MainActivity", "onDestroy() FMC service unBind!" );
                                context.unbindService( mSerConn );
                        }
                }
                catch ( Throwable t )
                {
                        EXCEPTION( t );
                }
                Define.contactArray.clear();
                mainActivity = null;
        }

        @Override
        public void onWindowFocusChanged( boolean hasFocus )
        {
                super.onWindowFocusChanged( hasFocus );
        }

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
        		TRACE("GCM01");
                Define.mContext = this;
                super.onCreate( savedInstanceState );
                TRACE("GCM02");
                ActionManager.m_bShowWaitMessage = false;
                new AmCodec();
                context = getApplicationContext();
                TRACE("GCM03");
                mainActivity = this;
                Define.setContext( getApplicationContext() );
                setContentView( R.layout.activity_main );
                TRACE("GCM04");
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE );
                
                TRACE("GCM1");
                try
                {
                        if ( Define.useGcmPush )
                        {
                        		TRACE("GCM2");
                                if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH )
                                {
                                	TRACE("GCM3");
                                	Define.regid = "NoC2dm";
                                	TRACE( "GCM regId:" + Define.regid );
                                }
                                else
                                {
                                		TRACE("GCM4");
                                        if ( Define.regid == null || Define.regid.equals( "" ) )
                                        {
                                                GcmManager gcm = new GcmManager( this );
                                                Define.regid = gcm.getPhoneRegistrationId();
                                                TRACE( "GCM regId:" + Define.regid );
                                                TRACE("GCM5");
                                        }
                                }
                        }
                        else
                        {
                        		TRACE("GCM6");
                                Define.regid = "NoC2dm";
                                TRACE( "GCM regId:" + Define.regid );
                        }
                }
                catch ( Exception e )
                {
                		TRACE("GCM7");
                        EXCEPTION(e);
                        Define.regid = "NoC2dm";
                        TRACE( "GCM regId:" + Define.regid );
                }
                TRACE( "onCreate : MainActivity" );
                receiver = new AtSmartServiceStarter();
                try
                {
                        IntentFilter mainFilter = new IntentFilter( "kr.co.ultari.atsmart.basic.service.AtSmartServiceStarter" );
                        mainFilter.addAction( Intent.ACTION_SCREEN_OFF );
                        mainFilter.addAction( Intent.ACTION_SCREEN_ON );
                        registerReceiver( receiver, mainFilter );
                        IntentFilter f = new IntentFilter();
                        f.addAction( Define.MSG_CHAT );
                        f.addAction( Define.MSG_NOTIFY );
                        f.addAction( Define.MSG_MY_NAME );
                        f.addAction( Define.AM_LOGOUT );
                        f.addAction( Define.MSG_SEND_COMPLETE );
                        f.addAction( Define.MSG_USER_POPUP );
                        registerReceiver( mBroadcastReceiver, new IntentFilter( f ) );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                serviceMessageQueue = new ArrayList<Message>();
                btnTabCancel = ( Button ) findViewById( R.id.main_exit );
                btnTabCancel.setOnClickListener( this );
                btnTabChat = ( Button ) findViewById( R.id.main_chat );
                btnTabChat.setOnClickListener( this );
                
                btnLauncher = ( Button ) findViewById( R.id.main_launcher );
                btnLauncher.setOnClickListener( this );
                
                switch(Define.SET_COMPANY)
                {
                        case Define.EX:
                                btnLauncher.setVisibility( View.VISIBLE );
                                break;
                        
                        default:
                                btnLauncher.setVisibility( View.GONE );
                                break;
                }
                
                loadingContactData();
                if ( NetworkStatusReceiver.isConnected( this ) )
                {
                        LayoutInflater inflater;
                        Toast toast;
                        View layout;
                        TextView text;
                        
                        String mode = Database.instance( getApplicationContext() ).selectConfig( "NETWORKMODE" );
                        if ( mode.equals( Integer.toString( Define.NETWORK_MODE_LTE ) ) ) Define.NETWORK_MODE = Define.NETWORK_MODE_LTE;
                        else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_WIFI ) ) ) Define.NETWORK_MODE = Define.NETWORK_MODE_WIFI;
                        else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_DISABLE ) ) || mode.equals( "" ) ) Define.NETWORK_MODE = Define.NETWORK_MODE_DISABLE;
                        
                        if ( !NetworkStatusReceiver.isWifi( this ) )
                        {
                                inflater = getLayoutInflater();
                                layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                                text = ( TextView ) layout.findViewById( R.id.tv );
                                text.setText( getString( R.string.network_connect_mobile ) );
                                text.setTypeface( Define.tfRegular );
                                toast = new Toast( getApplicationContext() );
                                toast.setGravity( Gravity.CENTER, 0, 0 );
                                toast.setDuration( Toast.LENGTH_SHORT );
                                toast.setView( layout );
                                toast.show();
                                if ( Define.NETWORK_MODE == Define.NETWORK_MODE_WIFI )
                                {
                                        Message m = mainHandler.obtainMessage( Define.AM_WIFI_MODE, null );
                                        mainHandler.sendMessage( m );
                                }
                        }
                        else
                        {
                                inflater = getLayoutInflater();
                                layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                                text = ( TextView ) layout.findViewById( R.id.tv );
                                text.setText( getString( R.string.network_connect_wifi ) );
                                text.setTypeface( Define.tfRegular );
                                toast = new Toast( getApplicationContext() );
                                toast.setGravity( Gravity.CENTER, 0, 0 );
                                toast.setDuration( Toast.LENGTH_SHORT );
                                toast.setView( layout );
                                toast.show();
                                if ( Define.NETWORK_MODE == Define.NETWORK_MODE_LTE )
                                {
                                        Message m = mainHandler.obtainMessage( Define.AM_LTE_MODE, null );
                                        mainHandler.sendMessage( m );
                                }
                        }
                }
                deleteChatAndNotifyByAutoDeleteDate();
                mPager = ( ViewPager ) findViewById( R.id.viewpager );
                adapterViewPager = new MyPagerAdapter( getSupportFragmentManager() );
                mPager.setAdapter( adapterViewPager );
                mPager.setOffscreenPageLimit( Define.pageCount );
                mPager.setOnPageChangeListener( new OnPageChangeListener() {
                        @Override
                        public void onPageSelected( int position )
                        {
                                Define.isMovingForward = Define.mLastVisitedPageIndex < position ? true : false;
                                Define.mLastVisitedPageIndex = position;
                                if ( ActionManager.tabs != null ) ActionManager.tabs.onTabSelected( mPager.getCurrentItem() );
                                if ( OrganizationView.instance() != null )
                                {
                                        OrganizationView.instance().clearCheckBtn();
                                        OrganizationView.instance().initSelectItem();
                                }
                        }

                        @Override
                        public void onPageScrolled( int position, float positionOffset, int positionOffsetPixels )
                        {
                        }

                        @Override
                        public void onPageScrollStateChanged( int state )
                        {
                        }
                } );
                Define.isVisible = true;
                timer = new Timer();
                timer.schedule( timerTask, 3000, 60000 );
                (new Thread( this )).start();
                if ( getIntent() != null )
                {
                        Log.d( "getIntent", "getIntent" );
                        try
                        {
                                startServiceMobile( Define.getMyId( context ), Define.getMyPw( context ) );
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
                try
                {
                        resetOptionMenu();
                        layout_fragmentTab = ( RelativeLayout ) findViewById( R.id.main_fragmenttab );
                        swipe_left = ( ImageView ) findViewById( R.id.main_swipe_left );
                        swipe_right = ( ImageView ) findViewById( R.id.main_swipe_right );
                        backPressCloseHandler = new BackPressCloseHandler( this );
                        Define.selectedFontSize = "15";
                        if ( Define.getMyId( context ).equals( "" ) )
                        {
                                Intent selectWindow = new Intent( this, kr.co.ultari.atsmart.basic.view.AccountView.class );
                                selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( selectWindow );
                        }
                        else if ( getIntent().getStringExtra( "RoomId" ) != null )
                        {
                                if ( getIntent().getStringExtra( "RoomId" ).equals( "note" ) )
                                {
                                        Message m = mainHandler.obtainMessage( Define.AM_SELECT_TAB, null );
                                        m.arg1 = Define.TAB_NOTIFY;
                                        mainHandler.sendMessage( m );
                                }
                                else if ( getIntent().getStringExtra( "RoomId" ).equals( "message" ) )
                                {
                                        Message m = mainHandler.obtainMessage( Define.AM_SELECT_TAB, null );
                                        m.arg1 = Define.TAB_MESSAGE;
                                        mainHandler.sendMessage( m );
                                }
                                else
                                {
                                        ActionManager.openChat( this, getIntent().getStringExtra( "RoomId" ), null, null );
                                        Message m = mainHandler.obtainMessage( Define.AM_SELECT_TAB, null );
                                        m.arg1 = Define.TAB_CHAT;
                                        mainHandler.sendMessage( m );
                                }
                        }
                        else
                        {
                                Message m;
                                switch ( Define.SET_COMPANY )
                                {
                                case Define.SAMSUNG :
                                        m = mainHandler.obtainMessage( Define.AM_SELECT_TAB, null );
                                        m.arg1 = Define.TAB_KEYPAD;
                                        mainHandler.sendMessage( m );
                                        ActionManager.tabs.moveKeypad();
                                        break;
                                default :
                                        m = mainHandler.obtainMessage( Define.AM_SELECT_TAB, null );
                                        m.arg1 = Define.TAB_KEYPAD;
                                        mainHandler.sendMessage( m );
                                        ActionManager.tabs.moveKeypad();
                                        break;
                                }
                                LogoDialog dialog = new LogoDialog( MainActivity.this, 0, 0 );
                                dialog.show();
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        //2016-05-27
        private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
        
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                               int[] grantResults) {
            if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Permission is granted
                        loadingContactData();
                        
                        if(FavoriteView.instance() != null)
                                FavoriteView.instance().resetData();
                        
                        if(CallView.instance() != null)
                                CallView.instance().callLog();
                } else {
                    Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
                }
            }
        }
        //
        
        @SuppressLint("NewApi")
		public void loadingContactData()
        {
                try
                {
                        //2016-05-27
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) 
                        {
                                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                        } 
                        else 
                        {
                        //
                                if ( Define.contactMap == null )
                                {
                                        Define.contactMap = new ConcurrentHashMap<String, Contact>();
                                }
                                Define.contactArray = new ArrayList<Contact>();
                                Define.contactArray.clear();
                                String id = "";
                                String name = "";
                                String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
                                ContentResolver cr = getContentResolver();
                                Cursor cur = cr.query( ContactsContract.Contacts.CONTENT_URI, null, null, null, sortOrder );
                                if ( cur.getCount() > 0 )
                                {
                                        while ( cur.moveToNext() )
                                        {
                                                id = cur.getString( cur.getColumnIndex( ContactsContract.Contacts._ID ) );
                                                name = cur.getString( cur.getColumnIndex( ContactsContract.Contacts.DISPLAY_NAME ) );
                                                if ( id == null || name == null ) continue;
                                                int hasPhoneNumber = Integer.parseInt( cur.getString( cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER ) ) );
                                                Contact acontact = new Contact();
                                                acontact.userId = null;
                                                acontact.userName = null;
                                                acontact.setContactId( id );
                                                acontact.setPhotoid( Long.parseLong( id ) );
                                                acontact.setName( name );
                                                acontact.setHasPhoneNumber( hasPhoneNumber );
                                                acontact.setUserid( "0" );
                                                acontact.setType( "Device" );
                                                Define.contactArray.add( acontact );
                                                Define.contactMap.put( id, acontact );
                                        }
                                }
                                cur.close();
                                Cursor pCur = cr.query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null );
                                while ( pCur.moveToNext() )
                                {
                                        String contactId = pCur.getString( pCur.getColumnIndex( ContactsContract.CommonDataKinds.Phone.CONTACT_ID ) );
                                        String PhoneType = pCur.getString( pCur.getColumnIndex( ContactsContract.CommonDataKinds.Phone.TYPE ) );
                                        String PhoneNo = pCur.getString( pCur.getColumnIndex( ContactsContract.CommonDataKinds.Phone.DATA ) );
                                        if ( PhoneType.equals( "1" ) )
                                        {
                                                Contact aContact = Define.contactMap.get( contactId );
                                                if ( aContact != null ) aContact.setTelnum( PhoneNo );
                                        }
                                        else if ( PhoneType.equals( "2" ) )
                                        {
                                                Contact aContact = Define.contactMap.get( contactId );
                                                if ( aContact != null ) aContact.setPhonenum( PhoneNo );
                                        }
                                }
                                pCur.close();
                        }
                }
                catch(Exception e)
                {
                        e.printStackTrace();
                }
        }

        private void resetOptionMenu()
        {
                Define.fontInit();
                Define.sound = Database.instance( context ).selectConfig( "sound" );
                if ( Define.sound.equals( "0" ) || Define.sound.equals( "" ) || Define.sound == null || Define.sound.equals( "OFF" ) ) Define.sound = "OFF";
                else Define.sound = "ON";
                Define.vibrator = Database.instance( context ).selectConfig( "vibrator" );
                if ( Define.vibrator.equals( "0" ) || Define.vibrator.equals( "" ) || Define.vibrator == null || Define.vibrator.equals( "OFF" ) ) Define.vibrator = "OFF";
                else Define.vibrator = "ON";
                Define.keyboard = Database.instance( context ).selectConfig( "keyboard" );
                if ( Define.keyboard.equals( "0" ) || Define.keyboard.equals( "" ) || Define.keyboard == null || Define.keyboard.equals( "OFF" ) ) Define.keyboard = "OFF";
                else Define.keyboard = "ON";
                Define.push = Database.instance( context ).selectConfig( "push" );
                if ( Define.push.equals( "0" ) || Define.push.equals( "" ) || Define.push == null || Define.push.equals( "OFF" ) ) Define.push = "OFF";
                else Define.push = "ON";
                Define.SELCT_BACKGROUND_MODE = Database.instance( context ).selectConfig( "backgroundMode" );
                if ( Define.SELCT_BACKGROUND_MODE.equals( "0" ) || Define.SELCT_BACKGROUND_MODE.equals( "" ) || Define.SELCT_BACKGROUND_MODE == null ) Define.SELCT_BACKGROUND_MODE = "IMAGE";
                String colorValue = Database.instance( context ).selectConfig( "colorValue" );
                if ( colorValue.equals( "0" ) || colorValue.equals( "" ) || colorValue == null ) Define.SELECT_BACKGROUND_COLOR = Color.rgb( 237, 237, 237 );
                else Define.SELECT_BACKGROUND_COLOR = Integer.parseInt( colorValue );
                /*
                 * Define.mEmoticonMappingNameMap.put("emoticon_adore", getString(R.string.emoticon_adore));
                 * Define.mEmoticonMappingNameMap.put("emoticon_ah", getString(R.string.emoticon_ah));
                 * Define.mEmoticonMappingNameMap.put("emoticon_amazing", getString(R.string.emoticon_amazing));
                 * Define.mEmoticonMappingNameMap.put("emoticon_angel", getString(R.string.emoticon_angel));
                 * Define.mEmoticonMappingNameMap.put("emoticon_angry", getString(R.string.emoticon_angry));
                 * Define.mEmoticonMappingNameMap.put("emoticon_baby", getString(R.string.emoticon_baby));
                 * Define.mEmoticonMappingNameMap.put("emoticon_bad_egg", getString(R.string.emoticon_bad_egg));
                 * Define.mEmoticonMappingNameMap.put("emoticon_baffle", getString(R.string.emoticon_baffle));
                 * Define.mEmoticonMappingNameMap.put("emoticon_batman", getString(R.string.emoticon_batman));
                 * Define.mEmoticonMappingNameMap.put("emoticon_beaten", getString(R.string.emoticon_beaten));
                 * Define.mEmoticonMappingNameMap.put("emoticon_bigsmile", getString(R.string.emoticon_bigsmile));
                 * Define.mEmoticonMappingNameMap.put("emoticon_bubblegum", getString(R.string.emoticon_bubblegum));
                 * Define.mEmoticonMappingNameMap.put("emoticon_bye_bye", getString(R.string.emoticon_bye_bye));
                 * Define.mEmoticonMappingNameMap.put("emoticon_confuse", getString(R.string.emoticon_confuse));
                 * Define.mEmoticonMappingNameMap.put("emoticon_cool", getString(R.string.emoticon_cool));
                 * Define.mEmoticonMappingNameMap.put("emoticon_crazy", getString(R.string.emoticon_crazy));
                 * Define.mEmoticonMappingNameMap.put("emoticon_cyclops", getString(R.string.emoticon_cyclops));
                 * Define.mEmoticonMappingNameMap.put("emoticon_doubt", getString(R.string.emoticon_doubt));
                 * Define.mEmoticonMappingNameMap.put("emoticon_exciting", getString(R.string.emoticon_exciting));
                 * Define.mEmoticonMappingNameMap.put("emoticon_eyes_droped", getString(R.string.emoticon_eyes_droped));
                 * Define.mEmoticonMappingNameMap.put("emoticon_face_monkey", getString(R.string.emoticon_face_monkey));
                 * Define.mEmoticonMappingNameMap.put("emoticon_face_panda", getString(R.string.emoticon_face_panda));
                 * Define.mEmoticonMappingNameMap.put("emoticon_greedy", getString(R.string.emoticon_greedy));
                 * Define.mEmoticonMappingNameMap.put("emoticon_grin", getString(R.string.emoticon_grin));
                 * Define.mEmoticonMappingNameMap.put("emoticon_happy", getString(R.string.emoticon_happy));
                 * Define.mEmoticonMappingNameMap.put("emoticon_horror", getString(R.string.emoticon_horror));
                 * Define.mEmoticonMappingNameMap.put("emoticon_hungry", getString(R.string.emoticon_hungry));
                 * Define.mEmoticonMappingNameMap.put("emoticon_love", getString(R.string.emoticon_love));
                 * Define.mEmoticonMappingNameMap.put("emoticon_mad", getString(R.string.emoticon_mad));
                 * Define.mEmoticonMappingNameMap.put("emoticon_medic", getString(R.string.emoticon_medic));
                 * Define.mEmoticonMappingNameMap.put("emoticon_misdoubt", getString(R.string.emoticon_misdoubt));
                 * Define.mEmoticonMappingNameMap.put("emoticon_mummy", getString(R.string.emoticon_mummy));
                 * Define.mEmoticonMappingNameMap.put("emoticon_question", getString(R.string.emoticon_question));
                 * Define.mEmoticonMappingNameMap.put("emoticon_red", getString(R.string.emoticon_red));
                 * Define.mEmoticonMappingNameMap.put("emoticon_sad", getString(R.string.emoticon_sad));
                 * Define.mEmoticonMappingNameMap.put("emoticon_shame", getString(R.string.emoticon_shame));
                 * Define.mEmoticonMappingNameMap.put("emoticon_shocked", getString(R.string.emoticon_shocked));
                 * Define.mEmoticonMappingNameMap.put("emoticon_silent", getString(R.string.emoticon_silent));
                 * Define.mEmoticonMappingNameMap.put("emoticon_sleep", getString(R.string.emoticon_sleep));
                 * Define.mEmoticonMappingNameMap.put("emoticon_smile", getString(R.string.emoticon_smile));
                 * Define.mEmoticonMappingNameMap.put("emoticon_spiderman", getString(R.string.emoticon_spiderman));
                 * Define.mEmoticonMappingNameMap.put("emoticon_star", getString(R.string.emoticon_star));
                 * Define.mEmoticonMappingNameMap.put("emoticon_surrender", getString(R.string.emoticon_surrender));
                 * Define.mEmoticonMappingNameMap.put("emoticon_tire", getString(R.string.emoticon_tire));
                 * Define.mEmoticonMappingNameMap.put("emoticon_tongue", getString(R.string.emoticon_tongue));
                 * Define.mEmoticonMappingNameMap.put("emoticon_waaaht", getString(R.string.emoticon_waaaht));
                 * Define.mEmoticonMappingNameMap.put("emoticon_what", getString(R.string.emoticon_what));
                 * Define.mEmoticonMappingNameMap.put("emoticon_whist", getString(R.string.emoticon_whist));
                 * Define.mEmoticonMappingNameMap.put("emoticon_wink", getString(R.string.emoticon_wink));
                 * Define.mEmoticonMappingNameMap.put("emoticon_ghost", getString(R.string.emoticon_ghost));
                 * Define.mEmoticonMappingNameMap.put("emoticon_folder", getString(R.string.emoticon_folder));
                 * Define.mEmoticonMappingNameMap.put("emoticon_add", getString(R.string.emoticon_add));
                 * Define.mEmoticonMappingNameMap.put("emoticon_dead", getString(R.string.emoticon_dead));
                 */
        }

        public void deleteChatAndNotifyByAutoDeleteDate()
        {
                try
                {
                        String autoDeleteChat = Database.instance( context ).selectConfig( "AutoDeleteChat" );
                        if ( autoDeleteChat != null && !autoDeleteChat.equals( "" ) )
                        {
                                Database.instance( context ).deleteChat( StringUtil.getUnderDateString( Integer.parseInt( autoDeleteChat ) ) );
                        }
                        ArrayList<ArrayList<String>> ar = Database.instance( context ).selectChatRoomInfo( null );
                        for ( int i = 0; i < ar.size(); i++ )
                        {
                                String nowRoomId = ar.get( i ).get( 0 );
                                if ( Database.instance( context ).selectChatContentCount( nowRoomId ) == 0 )
                                {
                                        Database.instance( context ).deleteChatRoomById( nowRoomId );
                                }
                        }
                        String autoDeleteNotify = Database.instance( context ).selectConfig( "AutoDeleteNotify" );
                        if ( autoDeleteNotify != null && !autoDeleteNotify.equals( "" ) )
                        {
                                Database.instance( context ).deleteAlarmUnderDate( StringUtil.getUnderDateString( Integer.parseInt( autoDeleteNotify ) ) );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void startServiceMobile( String _id, String _pw )
        {
                try
                {
                        Database.instance( context ).updateConfig( "USERID", _id );
                        Database.instance( context ).updateConfig( "USERPASSWORD", _pw );
                        Database.instance( context ).updateConfig( "SHOWUSERNAME", _id );
                        Intent sendIntent = new Intent( Define.MSG_RESTART_SERVICE );
                        sendIntent.putExtra( "USERID", _id.toString() );
                        sendIntent.putExtra( "MUST_RESTART", "Y" );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        sendBroadcast( sendIntent );
                }
                catch ( Exception e )
                {
                        MainActivity.Instance().EXCEPTION( e );
                }
        }
        public static Handler mainHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_OPEN_CHAT )
                                {
                                        String[] param = ( String[] ) msg.obj;
                                        Intent intent = new Intent( context, kr.co.ultari.atsmart.basic.view.ChatWindow.class );
                                        intent.putExtra( "roomId", param[0] );
                                        intent.putExtra( "userIds", param[1] );
                                        intent.putExtra( "userNames", param[2] );
                                        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                                        context.startActivity( intent );
                                }
                                else if ( msg.what == Define.AM_TAB_REFRESH )
                                {
                                        ActionManager.tabs.initTab();
                                }
                                else if ( msg.what == Define.AM_MAIN_GROUPTAB_REFRESH )
                                {
                                        /*
                                         * if(Define.isBuddyAddMode)
                                         * topLayout.setVisibility( View.VISIBLE );
                                         * else
                                         * topLayout.setVisibility( View.GONE );
                                         */
                                }
                                else if ( msg.what == Define.AM_NEW_CHAT )
                                {
                                        int cnt = Database.instance( context ).selectUnreadChatContentAll();
                                        MainActivity.Instance().TRACE( "NewChatCount : " + cnt );
                                        if ( ActionManager.talkTabButton != null ) ActionManager.talkTabButton.setNumber( cnt );
                                }
                                else if ( msg.what == Define.AM_NEW_NOTIFY )
                                {
                                        int cnt = Database.instance( context ).selectUnreadAlarmCount();
                                        if ( ActionManager.notifyTabButton == null ) return;
                                        ActionManager.notifyTabButton.setNumber( cnt );
                                }
                                else if ( msg.what == Define.AM_NEW_VERSION )
                                {
                                        // ActionManager.moreTabButton.setNumber(1);
                                        if ( ConfigView.instance().itemList != null ) ConfigView.instance().resetData();
                                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder( MainActivity.Instance() );
                                        alert_confirm.setTitle( context.getString( R.string.patchTitle ) );
                                        alert_confirm.setMessage( context.getString( R.string.patchMessage ) ).setCancelable( false )
                                                        .setPositiveButton( context.getString( R.string.ok ), new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick( DialogInterface dialog, int which )
                                                                {
                                                                        Intent i = new Intent( context, kr.co.ultari.atsmart.basic.service.Launcher.class );
                                                                        i.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                                                        context.startActivity( i );
                                                                        dialog.dismiss();
                                                                }
                                                        } ).setNegativeButton( context.getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick( DialogInterface dialog, int which )
                                                                {
                                                                        dialog.dismiss();
                                                                        return;
                                                                }
                                                        } );
                                        AlertDialog alert = alert_confirm.create();
                                        alert.show();
                                }
                                /*
                                 * else if ( msg.what == Define.AM_SAEHA_INSTALL )
                                 * {
                                 * AlertDialog.Builder alert_confirm = new AlertDialog.Builder(MainActivity.Instance());
                                 * alert_confirm.setTitle(context.getString(R.string.saehaInstallDialogTitle));
                                 * alert_confirm.setMessage(context.getString(R.string.saehaInstallDialogMessage)).setCancelable(false).setPositiveButton(context
                                 * .getString(R.string.saehaInstallPositiveBtn),
                                 * new DialogInterface.OnClickListener()
                                 * {
                                 * @Override
                                 * public void onClick( DialogInterface dialog, int which )
                                 * {
                                 * AppUtil.install( context );
                                 * dialog.dismiss();
                                 * System.exit( 0 );
                                 * }
                                 * });
                                 * AlertDialog alert = alert_confirm.create();
                                 * alert.show();
                                 * }
                                 */
                                else if ( msg.what == Define.AM_SELECT_TAB )
                                {
                                        mainActivity.onTabClicked( msg.arg1 );
                                }
                                else if ( msg.what == Define.AM_SEARCH )
                                {
                                        // mainActivity.onTabClicked(Define.TAB_USER_SEARCH);
                                        // SearchView.instance().clear();
                                        // String[] ar = (String[])msg.obj;
                                        // String userIds = ar[0];
                                        // String userNames = ar[1];
                                        // SearchView.selected.clear();
                                        // SearchView.selected.notifyDataSetChanged();
                                        /*
                                         * StringTokenizer st1 = new StringTokenizer(userIds, ",");
                                         * StringTokenizer st2 = new StringTokenizer(userNames, ",");
                                         * while ( st1.hasMoreElements() && st2.hasMoreElements() )
                                         * {
                                         * String id = st1.nextToken();
                                         * String name = st2.nextToken();
                                         * if ( id.equals(Define.getMyId())) {
                                         * continue;
                                         * }
                                         * SearchView.selected.add(new SearchResultItemData(id, "", name, 0, "", true));
                                         * }
                                         * Message m = SearchView.instance().searchHandler.obtainMessage(Define.AM_SELECT_CHANGED);
                                         * SearchView.instance().searchHandler.sendMessage(m);
                                         */
                                        // Define.isAddUserMode = true;
                                        // ActionManager.tabs.m_Layout.setVisibility(View.GONE);
                                        // SearchView.instance().setVisibleBottomTab();
                                }
                                else if ( msg.what == Define.AM_NO_USER )
                                {
                                        ActionManager.hideProgressDialog();
                                        ActionManager.alert( MainActivity.Instance(), context.getString( R.string.noUser ) );
                                }
                                else if ( msg.what == Define.AM_NO_PASSWORD )
                                {
                                        ActionManager.hideProgressDialog();
                                        ActionManager.alert( MainActivity.Instance(), context.getString( R.string.noPassword ) );
                                }
                                else if ( msg.what == Define.AM_WIFI_MODE )
                                {
                                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder( MainActivity.Instance() );
                                        alert_confirm.setTitle( context.getString( R.string.network_mode ) );
                                        alert_confirm.setMessage( context.getString( R.string.lte_msg ) ).setCancelable( false )
                                                        .setPositiveButton( context.getString( R.string.ok ), new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick( DialogInterface dialog, int which )
                                                                {
                                                                        Instance().finish();
                                                                        dialog.dismiss();
                                                                }
                                                        } );
                                        AlertDialog alert = alert_confirm.create();
                                        alert.show();
                                }
                                else if ( msg.what == Define.AM_LTE_MODE )
                                {
                                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder( MainActivity.Instance() );
                                        alert_confirm.setTitle( context.getString( R.string.network_mode ) );
                                        alert_confirm.setMessage( context.getString( R.string.wifi_msg ) ).setCancelable( false )
                                                        .setPositiveButton( context.getString( R.string.ok ), new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick( DialogInterface dialog, int which )
                                                                {
                                                                        Instance().finish();
                                                                        dialog.dismiss();
                                                                }
                                                        } );
                                        AlertDialog alert = alert_confirm.create();
                                        alert.show();
                                }
                                else if ( msg.what == Define.AM_HIDE_PROGRESS )
                                {
                                        ActionManager.hideProgressDialog();
                                }
                                else
                                {
                                        super.handleMessage( msg );
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };

        public void onConfigurationChanged( Configuration newConfig )
        {
                super.onConfigurationChanged( newConfig );
        }

        public void onTabClicked( int resourceId )
        {
                Log.d( "TabSelected", "Tab2 : " + resourceId );
                try
                {
                        if ( resourceId == Define.TAB_BOOKMARK )
                        {
                                switch ( Define.SET_COMPANY )
                                {
                                case Define.GG :
                                        mPager.setCurrentItem( 5 );
                                        break;
                                case Define.SAMSUNG :
                                        mPager.setCurrentItem( 5 );
                                        break;
                                default :
                                        mPager.setCurrentItem( 5 );
                                        break;
                                }
                        }
                        else if ( resourceId == Define.TAB_CONTACT )
                        {
                                mPager.setCurrentItem( 1 );
                                ContactView.instance().displayListBasic();
                        }
                        else if ( resourceId == Define.TAB_ORGANIZATION )
                        {
                                Define.selectedTreeNumber = Define.TAB_ORGANIZATION;
                                mPager.setCurrentItem( 3 );
                        }
                        else if ( resourceId == Define.TAB_CHAT )
                        {
                                mPager.setCurrentItem( 4 );
                                TalkView.instance().resetData();
                        }
                        else if ( resourceId == Define.TAB_KEYPAD )
                        {
                                switch ( Define.SET_COMPANY )
                                {
                                case Define.GG :
                                        mPager.setCurrentItem( 0 );
                                        break;
                                case Define.SAMSUNG :
                                        mPager.setCurrentItem( 0 );
                                        break;
                                default :
                                        mPager.setCurrentItem( 0 );
                                        break;
                                }
                        }
                        else if ( resourceId == Define.TAB_CALL_LOG )
                        {
                                mPager.setCurrentItem( 6 );
                                CallView.instance().callLog();
                        }
                        else if ( resourceId == Define.TAB_NOTIFY )
                        {
                                mPager.setCurrentItem( 8 );
                        }
                        else if ( resourceId == Define.TAB_MESSAGE )
                        {
                                mPager.setCurrentItem( 7 );
                        }
                        else if ( resourceId == Define.TAB_SETTING )
                        {
                                mPager.setCurrentItem( 9 );
                                ConfigView.instance().resetData();
                        }
                        else if ( resourceId == Define.TAB_USER_BUDDY )
                        {
                                Define.selectedTreeNumber = Define.TAB_USER_BUDDY;
                                mPager.setCurrentItem( 2 );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }
        class IncomingHandler extends Handler {
                @Override
                public void handleMessage( Message msg )
                {
                        try
                        {
                                switch ( msg.what )
                                {
                                case Define.AM_NEW_CHAT :
                                        Bundle b = msg.getData();
                                        // String userIds = b.getString( "userIds" );
                                        String title = b.getString( "title" );
                                        String content = b.getString( "content" );
                                        String talkId = b.getString( "talkId" );
                                        String roomId = b.getString( "RoomId" );
                                        String chatId = b.getString( Define.CHATID );
                                        if ( content.indexOf( "[READ_COMPLETE]" ) == 0 )
                                        {
                                                TalkView.instance().resetData();
                                                if ( cw != null && cw.roomId.equals( roomId ) )
                                                {
                                                        String[] ar = new String[2];
                                                        ar[0] = title;
                                                        ar[1] = talkId;
                                                        Message m = cw.uploadHandler.obtainMessage( Define.AM_READ_COMPLETE, ar );
                                                        cw.uploadHandler.sendMessage( m );
                                                }
                                        }
                                        else if ( content.equals( "[ROOM_OUT]" ) )
                                        {
                                                if ( talkId.equals( Define.getMyId( getApplicationContext() ) ) )
                                                {
                                                        if ( cw != null && cw.roomId.equals( roomId ) )
                                                        {
                                                                InputMethodManager imm = ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE );
                                                                imm.hideSoftInputFromWindow( cw.chatInput.getWindowToken(), 0 );
                                                                cw.finish();
                                                        }
                                                        Database.instance( context ).deleteChatBysRoomId( roomId );
                                                        Database.instance( context ).deleteChatRoomById( roomId );
                                                        TalkView.instance().resetData();
                                                }
                                                else
                                                {
                                                        if ( cw != null && cw.roomId.equals( roomId ) )
                                                        {
                                                                cw.insertData( chatId );
                                                                sendReadComplete( cw.roomId, cw.userIds, cw.userNames, getApplicationContext() );
                                                                Database.instance( context ).updateChatRoomRead( cw.roomId );
                                                                ArrayList<ArrayList<String>> arar = Database.instance( context ).selectChatRoomInfo( roomId );
                                                                if ( arar.size() != 1 ) return;
                                                                String[] ar = new String[2];
                                                                ar[0] = arar.get( 0 ).get( 1 );
                                                                ar[1] = arar.get( 0 ).get( 2 );
                                                                Message m = cw.uploadHandler.obtainMessage( Define.AM_USER_CHANGED, ar );
                                                                cw.uploadHandler.sendMessage( m );
                                                        }
                                                        TalkView.instance().resetData();
                                                }
                                        }
                                        else if ( content.equals( "[ROOM_IN]" ) )
                                        {
                                                if ( cw != null && cw.roomId.equals( roomId ) )
                                                {
                                                        cw.insertData( chatId );
                                                        sendReadComplete( cw.roomId, cw.userIds, cw.userNames, getApplicationContext() );
                                                        Database.instance( context ).updateChatRoomRead( cw.roomId );
                                                        ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfo( roomId );
                                                        if ( array == null || array.size() == 0 ) return;
                                                        String[] ar = new String[2];
                                                        ar[0] = array.get( 0 ).get( 1 );
                                                        ar[1] = array.get( 0 ).get( 2 );
                                                        Message m = cw.uploadHandler.obtainMessage( Define.AM_USER_CHANGED, ar );
                                                        cw.uploadHandler.sendMessage( m );
                                                }
                                                TalkView.instance().resetData();
                                        }
                                        else
                                        {
                                                if ( cw != null && cw.roomId.equals( roomId ) )
                                                {
                                                        cw.insertData( chatId );
                                                        sendReadComplete( cw.roomId, cw.userIds, cw.userNames, getApplicationContext() );
                                                        Database.instance( context ).updateChatRoomRead( cw.roomId );
                                                }
                                                else if ( cw != null && !cw.roomId.equals( roomId ) )
                                                {
                                                        if ( content.indexOf( "ATTACH://" ) >= 0 )
                                                        {
                                                                title = getString( R.string.fileReceive );
                                                                content = getString( R.string.fileReceiveMsg );
                                                        }
                                                        AlertDialogNotification.title = title;
                                                        AlertDialogNotification.message = content;
                                                        AlertDialogNotification nt = new AlertDialogNotification( cw );
                                                        nt.show();
                                                }
                                                else
                                                {
                                                        Log.d( "ShowDialog", "1" );
                                                        if ( !talkId.equals( Define.getMyId( context ) ) )
                                                        {
                                                                Log.d( "ShowDialog", "2" );
                                                                if ( Define.isVisible == false )
                                                                {
                                                                        Log.d( "ShowDialog", "3" );
                                                                        if ( content.indexOf( "ATTACH://" ) < 0 ) ActionManager.showAlertDialog( context,
                                                                                        title, content, roomId, talkId );
                                                                        else ActionManager.showAlertDialog( context, getString( R.string.fileReceive ),
                                                                                        getString( R.string.fileReceiveMsg ), roomId, talkId );
                                                                }
                                                                else
                                                                {
                                                                        Log.d( "ShowDialog", "4" );
                                                                        if ( content.indexOf( "ATTACH://" ) >= 0 )
                                                                        {
                                                                                title = getString( R.string.fileReceive );
                                                                                content = getString( R.string.fileReceiveMsg );
                                                                        }
                                                                        AlertDialogNotification.title = title;
                                                                        AlertDialogNotification.message = content;
                                                                        if ( Define.nowTopActivity != null )
                                                                        {
                                                                                AlertDialogNotification nt = new AlertDialogNotification( Define.nowTopActivity );
                                                                                nt.show();
                                                                        }
                                                                        else
                                                                        {
                                                                                AlertDialogNotification nt = new AlertDialogNotification(
                                                                                                MainActivity.Instance() );
                                                                                nt.show();
                                                                        }
                                                                }
                                                        }
                                                }
                                                TalkView.instance().resetData();
                                        }
                                        break;
                                case Define.AM_NEW_NOTIFY :
                                        Bundle ab = msg.getData();
                                        String aTitle = ab.getString( "title" );
                                        String aContent = ab.getString( "content" );
                                        // NotifyView.instance().resetData();
                                        if ( !Define.isVisible ) ActionManager.showAlertDialog( context, aTitle, aContent, "", "" );
                                        break;
                                case Define.AM_SEND_COMPLETE :
                                        if ( cw != null )
                                        {
                                                Bundle ac = msg.getData();
                                                String msgId = ac.getString( "msgId" );
                                                Message m = cw.uploadHandler.obtainMessage( Define.AM_SEND_COMPLETE, msgId );
                                                cw.uploadHandler.sendMessage( m );
                                        }
                                        break;
                                case Define.AM_USER_INFO :
                                        Bundle ac = msg.getData();
                                        String userId = ac.getString( "userId" );
                                        String userName = ac.getString( "userName" );
                                        ActionManager.popupUserInfo( getApplicationContext(), userId, userName, Define.getPartNameByUserId( userName ), "" );
                                        break;
                                /*
                                 * case Define.AM_NAME:
                                 * try
                                 * {
                                 * TextView nameView = (TextView)mainActivity.findViewById(R.id.nameLabel);
                                 * if ( nameView != null ) nameView.setText(Define.getMyName());
                                 * }
                                 * catch(Exception e)
                                 * {
                                 * e.printStackTrace();
                                 * }
                                 */
                                default :
                                        super.handleMessage( msg );
                                }
                        }
                        catch ( Exception e )
                        {
                                mainActivity.EXCEPTION( e );
                        }
                }
        }
        private ServiceConnection mConnection = new ServiceConnection() {
                public void onServiceConnected( ComponentName className, IBinder service )
                {
                        m_AtSmartService = new Messenger( service );
                        TRACE( "Service onServiceConnected" );
                        try
                        {
                                Message msg = Message.obtain( null, Define.AM_REGISTER_CLIENT );
                                msg.replyTo = mMessenger;
                                m_AtSmartService.send( msg );
                                synchronized ( serviceMessageQueue )
                                {
                                        while ( serviceMessageQueue.size() > 0 )
                                                m_AtSmartService.send( serviceMessageQueue.remove( 0 ) );
                                }
                        }
                        catch ( RemoteException e )
                        {
                                EXCEPTION( e );
                        }
                }

                public void onServiceDisconnected( ComponentName className )
                {
                        TRACE( "Service onServiceDisconnected" );
                        m_AtSmartService = null;
                        // doUnbindService();
                }
        };
        private ServiceConnection mSerConn = new ServiceConnection() {
                public void onServiceDisconnected( ComponentName p_name )
                {
                        mSMVService = null;
                }

                public void onServiceConnected( ComponentName name, IBinder service )
                {
                        mSMVService = ISMVService.Stub.asInterface( service );
                        String strWeWorkInfo = null;
                        try
                        {
                                if ( Define.getMyId( context ).equals( "" ) )
                                {
                                        strWeWorkInfo = mSMVService.getWeWorkInfo();
                                        if ( strWeWorkInfo != null )
                                        {
//                                                String[] parse = strWeWorkInfo.split( ":" );
//                                                
//                                                if ( Define.useFMCProvisioning )
//                                                {
//	                                                Define.PRIVATE_SERVER_IP = parse[2];
//	                                                Define.PRIVATE_SERVER_PORT = parse[3];
//	                                                Define.PUBLIC_SERVER_IP = parse[4];
//	                                                Define.PUBLIC_SERVER_PORT = parse[5];
//	                                                Define.ssid = parse[6];
//                                                }
                                                Define.isFmcConnected = true;
                                                LayoutInflater inflater = getLayoutInflater();
                                                View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                                                TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                                text.setTypeface( Define.tfRegular );
                                                text.setText( getString( R.string.provisioning ) );
                                                Toast toast = new Toast( getApplicationContext() );
                                                toast.setGravity( Gravity.CENTER, 0, 0 );
                                                toast.setDuration( Toast.LENGTH_SHORT );
                                                toast.setView( layout );
                                                toast.show();
                                        }
                                }
                        }
                        catch ( RemoteException e )
                        {
                                e.printStackTrace();
                        }
                }
        };
        TimerTask timerTask = new TimerTask() {
                public void run()
                {
                        try
                        {
                                CheckIfServiceIsRunning();
                                switch ( Define.SET_COMPANY )
                                {
                                case Define.SAMSUNG :
                                        if ( serviceList() ) getWeWorkInfo();
                                        break;
                                default :
                                        break;
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };

        private boolean serviceList()
        {
                ActivityManager am = ( ActivityManager ) getApplicationContext().getSystemService( Context.ACTIVITY_SERVICE );
                List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices( 100 );
                boolean isFmcServiceRunning = false;
                for ( int i = 0; i < rs.size(); i++ )
                {
                        ActivityManager.RunningServiceInfo rsi = rs.get( i );
                        if ( rsi.service.getPackageName().equals( "com.amc.ui" ) ) isFmcServiceRunning = true;
                }
                return isFmcServiceRunning;
        }

        private void getWeWorkInfo()
        {
                try
                {
                        if ( !fmcIsBind )
                        {
                                fmcIsBind = context.bindService( new Intent( ISMVService.class.getName() ), mSerConn, Context.BIND_AUTO_CREATE );
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        private void CheckIfServiceIsRunning()
        {
                if ( !isServiceRunningCheck() )
                {
                        TRACE( "AtSmartService restart!" );
                        startService( new Intent( this, kr.co.ultari.atsmart.basic.service.AtSmartService.class ) );
                }
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

        public boolean doBindService()
        {
                if ( !mIsBound )
                {
                        boolean bindResult = bindService( new Intent( this, kr.co.ultari.atsmart.basic.service.AtSmartService.class ), mConnection,
                                        Context.BIND_AUTO_CREATE );
                        if ( bindResult ) mIsBound = true;
                        else mIsBound = false;
                }
                return mIsBound;
        }

        void doUnbindService()
        {
                if ( mIsBound )
                {
                        if ( m_AtSmartService != null )
                        {
                                try
                                {
                                        Message msg = Message.obtain( null, Define.AM_UNREGISTER_CLIENT );
                                        msg.replyTo = mMessenger;
                                        m_AtSmartService.send( msg );
                                }
                                catch ( RemoteException e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        // 2015-09-05
                        // context.unbindService(mConnection);
                        mIsBound = false;
                }
        }

        public static void sendNick( String nick )
        {
                try
                {
                        Bundle b = new Bundle();
                        b.putString( "NICK", nick.toString() );
                        Message msg = Message.obtain( null, Define.AM_NICK );
                        msg.setData( b );
                        if ( MainActivity.Instance().m_AtSmartService != null ) MainActivity.Instance().m_AtSmartService.send( msg );
                        else serviceMessageQueue.add( msg );
                }
                catch ( RemoteException e )
                {
                        mainActivity.EXCEPTION( e );
                }
        }

        public static void requestUserInfo( String userId )
        {
                try
                {
                        StringBuffer message = new StringBuffer();
                        message.append( "UserNameRequest" );
                        message.append( "\t" );
                        message.append( userId );
                        Bundle b = new Bundle();
                        b.putString( "MESSAGE", message.toString() );
                        Message msg = Message.obtain( null, Define.AM_USER_INFO );
                        msg.setData( b );
                        if ( MainActivity.Instance().m_AtSmartService != null ) MainActivity.Instance().m_AtSmartService.send( msg );
                        else serviceMessageQueue.add( msg );
                }
                catch ( RemoteException e )
                {
                        mainActivity.EXCEPTION( e );
                }
        }

        public static void sendMessage( String msgId, String senderId, String senderName, String senderPart, String subject, String content,
                        ArrayList<File> attach, ArrayList<MessageUserData> receivers, String image )
        {
                try
                {
                        // 내용 구성
                        // 메시지번호\n
                        // 기본이미지파일패스\n
                        // 첨부파일정보 : 첨부파일명\파일크기/첨부파일명\파일크기/첨부파일명\파일크기\n
                        // 수신자정보 : 수신자이이디\수신자이름/수신자이이디\수신자이름/수신자이이디\수신자이름
                        // 내용
                        // MESSAGE 수신자아이디 보낸사람아이디 보낸사람이름 보낸사람닉네임 제목 내용
                        String attachString = "";
                        for ( int i = 0; i < attach.size(); i++ )
                        {
                                if ( !attachString.equals( "" ) ) attachString += "/";
                                attachString += attach.get( i ).getName() + "\\" + attach.get( i ).length();
                        }
                        String receiverString = "";
                        for ( int i = 0; i < receivers.size(); i++ )
                        {
                                if ( !receiverString.equals( "" ) ) receiverString += "/";
                                receiverString += receivers.get( i ).id + "\\" + receivers.get( i ).name;
                        }
                        for ( int i = 0; i < receivers.size(); i++ )
                        {
                                String rcvrId = receivers.get( i ).id;
                                StringBuffer message = new StringBuffer();
                                message.append( rcvrId );
                                message.append( '\t' );
                                message.append( senderId );
                                message.append( '\t' );
                                message.append( senderName );
                                message.append( '\t' );
                                message.append( senderPart );
                                message.append( '\t' );
                                message.append( subject );
                                message.append( '\t' );
                                message.append( msgId + "\n" );
                                message.append( image + "\n" );
                                message.append( attachString + "\n" );
                                message.append( receiverString + "\n" );
                                message.append( content );
                                Bundle b = new Bundle();
                                b.putString( "MESSAGE", message.toString() );
                                Message msg = Message.obtain( null, Define.AM_NEW_MESSAGE );
                                msg.setData( b );
                                if ( MainActivity.Instance().m_AtSmartService != null ) MainActivity.Instance().m_AtSmartService.send( msg );
                                else serviceMessageQueue.add( msg );
                        }
                        Database.instance( context ).insertMessage( msgId, senderId, senderName, senderPart, subject, content, attachString, receiverString,
                                        image );
                }
                catch ( RemoteException e )
                {
                        mainActivity.EXCEPTION( e );
                }
        }

        public static void sendChat( String msgId, String roomId, String userIds, String userNames, String talk )
        {
                try
                {
                        StringBuffer message = new StringBuffer();
                        message.append( Define.getMyId( context ) );
                        message.append( "\t" );
                        message.append( Define.getMyName() );
                        message.append( "\t" );
                        message.append( Define.getMyNickName() );
                        message.append( "\t" );
                        message.append( userIds );
                        message.append( "\t" );
                        message.append( roomId );
                        message.append( "\n" );
                        message.append( userIds );
                        message.append( "\n" );
                        message.append( userNames );
                        message.append( "\n" );
                        message.append( msgId );
                        message.append( "\t" );
                        message.append( StringUtil.getNowDateTime() );
                        message.append( "\t" );
                        message.append( talk );
                        Bundle b = new Bundle();
                        b.putString( "MESSAGE", message.toString() );
                        b.putString( "MESSAGEID", msgId.toString() );
                        Message msg = Message.obtain( null, Define.AM_NEW_CHAT );
                        msg.setData( b );
                        if ( MainActivity.Instance().m_AtSmartService != null ) MainActivity.Instance().m_AtSmartService.send( msg );
                        else serviceMessageQueue.add( msg );
                }
                catch ( RemoteException e )
                {
                        mainActivity.EXCEPTION( e );
                }
        }

        public void sendReadComplete( String roomId, String userIds, String userNames, Context context )
        {
                try
                {
                        ArrayList<ArrayList<String>> ar = Database.instance( context ).selectUnreadChatContent( roomId );
                        for ( int i = 0; i < ar.size(); i++ )
                        {
                                StringBuffer message = new StringBuffer();
                                message.append( Define.getMyId( context ) );
                                message.append( "\t" );
                                message.append( Define.getMyName() );
                                message.append( "\t" );
                                message.append( Define.getMyNickName() );
                                message.append( "\t" );
                                message.append( userIds );
                                message.append( "\t" );
                                message.append( roomId );
                                message.append( "\n" );
                                message.append( userIds );
                                message.append( "\n" );
                                message.append( userNames );
                                message.append( "\n" );
                                message.append( ar.get( i ).get( 0 ) );
                                message.append( "\t" );
                                message.append( ar.get( i ).get( 0 ) );
                                message.append( "\t" );
                                message.append( "[READ_COMPLETE]" );
                                /*
                                 * Bundle b = new Bundle();
                                 * b.putString("MESSAGE", message.toString());
                                 * Message msg = Message.obtain(null, Define.AM_READ_COMPLETE);
                                 * msg.setData(b);
                                 * if ( MainActivity.Instance().m_AtSmartService != null)
                                 * {
                                 * MainActivity.Instance().m_AtSmartService.send(msg);
                                 * mainActivity.TRACE("readcomplete message is send");
                                 * }
                                 * else
                                 * {
                                 * serviceMessageQueue.add(msg);
                                 * mainActivity.TRACE("readcomplete message is in queue");
                                 * }
                                 */
                                Intent sendIntent = new Intent( Define.MSG_READ_COMPLETE );
                                sendIntent.putExtra( "MESSAGE", message.toString() );
                                sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                                sendBroadcast( sendIntent );
                        }
                }
                /*
                 * catch(RemoteException e)
                 * {
                 * mainActivity.EXCEPTION(e);
                 * }
                 */
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public void run()
        {
                TRACE( "Update check thread start" );
                InputStreamReader ir = null;
                BufferedReader br = null;
                ByteArrayInputStream bi = null;
                try
                {
                        byte[] rcvByte = UltariSocketUtil.getProxyContent( "/downloadCenter/update/android.html" );
                        bi = new ByteArrayInputStream( rcvByte );
                        ir = new InputStreamReader( bi, "KSC5601" );
                        br = new BufferedReader( ir );
                        String line;
                        while ( (line = br.readLine()) != null )
                        {
                                if ( line.indexOf( "VERSION:" ) == 0 ) Define.NEW_VERSION = line.substring( 8 );
                                else if ( line.indexOf( "URL:" ) == 0 ) Define.UPDATE_URL = line.substring( 4 );
                        }
                        if ( Define.NEW_VERSION.equals( "" ) ) return;
                        float newVersion = Float.parseFloat( Define.NEW_VERSION );
                        float version = Float.parseFloat( Define.VERSION );
                        if ( newVersion > version )
                        {
                                Message m = mainHandler.obtainMessage( Define.AM_NEW_VERSION, null );
                                mainHandler.sendMessage( m );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( ir != null )
                        {
                                try
                                {
                                        ir.close();
                                        ir = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( br != null )
                        {
                                try
                                {
                                        br.close();
                                        br = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( bi != null )
                        {
                                try
                                {
                                        bi.close();
                                        bi = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                }
                TRACE( "Update check thread finished" );
        }

        public static void FileLog( String s )
        {
                if ( !Define.useFileLog ) return;
                java.io.FileWriter fw = null;
                try
                {
                        fw = new java.io.FileWriter( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ) + File.separator
                                        + "AtSmartLog.txt", true );
                        fw.write( new java.util.Date() + " : " + s + "\n" );
                        fw.flush();
                }
                catch ( Exception e )
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
                                catch ( Exception e )
                                {}
                        }
                }
        }
        private static final String TAG = "/AtSmart/MainActivity";

        public void TRACE( String s )
        {
                if ( !Define.useTrace ) return;
                android.util.Log.e( TAG, s );
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

        @Override
        public boolean onKeyDown( int keyCode, KeyEvent event )
        {
                if ( event.getAction() == KeyEvent.ACTION_DOWN )
                {
                        if ( keyCode == KeyEvent.KEYCODE_BACK )
                        {
                                if ( Define.isAddUserMode )
                                {
                                        Define.isAddUserMode = false;
                                        Intent intent = new Intent( getApplicationContext(), kr.co.ultari.atsmart.basic.view.ChatWindow.class );
                                        intent.putExtra( "roomId", Define.oldRoomId );
                                        intent.putExtra( "userIds", Define.oldRoomUserId );
                                        intent.putExtra( "userNames", Define.oldRoomUserName );
                                        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                        startActivity( intent );
                                        return true;
                                }
                        }
                }
                return super.onKeyDown( keyCode, event );
        }
        public static class MyPagerAdapter extends FragmentPagerAdapter {
                private static int NUM_ITEMS = Define.pageCount;

                public MyPagerAdapter( FragmentManager fragmentManager )
                {
                        super( fragmentManager );
                }

                @Override
                public int getCount()
                {
                        return NUM_ITEMS;
                }

                @Override
                public Fragment getItem( int position )
                {
                        switch ( position )
                        {
                                case Define.TAB_KEYPAD :
                                        return KeypadView.instance();
                                case Define.TAB_CONTACT :
                                        return ContactView.instance();
                                case Define.TAB_USER_BUDDY :
                                        return BuddyView.instance();
                                case Define.TAB_ORGANIZATION :
                                        return OrganizationView.instance();
                                case Define.TAB_CHAT :
                                        return TalkView.instance();
                                case Define.TAB_BOOKMARK :
                                        return FavoriteView.instance();
                                case Define.TAB_CALL_LOG :
                                        return CallView.instance();
                                case Define.TAB_MESSAGE :
                                        return MessageView.instance();
                                case Define.TAB_NOTIFY :
                                        return NotifyView.instance();
                                case Define.TAB_SETTING :
                                        return ConfigView.instance();
                                default :
                                        return null;
                        }
                }

                @Override
                public CharSequence getPageTitle( int position )
                {
                        return "Page " + position;
                }
        }
        private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive( Context context, Intent intent )
                {
                        String action = intent.getAction();
                        TRACE( "MainActivity Broadcast:" + action );
                        try
                        {
                                if ( action.equals( Define.AM_LOGOUT ) )
                                {
                                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder( MainActivity.Instance() );
                                        alert_confirm.setTitle( context.getString( R.string.app_name ) );
                                        alert_confirm.setMessage( context.getString( R.string.logout ) ).setCancelable( false );
                                        alert_confirm.setPositiveButton( context.getString( R.string.ok ), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick( DialogInterface dialog, int which )
                                                {
                                                        dialog.dismiss();
                                                        finish();
                                                        System.exit( 0 );
                                                }
                                        } );
                                        AlertDialog alert = alert_confirm.create();
                                        alert.show();
                                }
                                else if ( action.equals( Define.MSG_CHAT ) )
                                {
                                        // String userIds = intent.getStringExtra( Define.USERIDS );
                                        String title = intent.getStringExtra( Define.TITLE );
                                        String content = intent.getStringExtra( Define.CONTENT );
                                        String talkId = intent.getStringExtra( Define.TALKID );
                                        String roomId = intent.getStringExtra( Define.ROOMID );
                                        String chatId = intent.getStringExtra( Define.CHATID );
                                        if ( content.indexOf( "[READ_COMPLETE]" ) == 0 )
                                        {
                                                TalkView.instance().resetData();
                                                if ( cw != null && cw.roomId.equals( roomId ) )
                                                {
                                                        String[] ar = new String[2];
                                                        ar[0] = title;
                                                        ar[1] = talkId;
                                                        Message m = cw.uploadHandler.obtainMessage( Define.AM_READ_COMPLETE, ar );
                                                        cw.uploadHandler.sendMessage( m );
                                                }
                                        }
                                        else if ( content.equals( "[ROOM_OUT]" ) )
                                        {
                                                if ( talkId.equals( Define.getMyId( getApplicationContext() ) ) )
                                                {
                                                        if ( cw != null && cw.roomId.equals( roomId ) )
                                                        {
                                                                InputMethodManager imm = ( InputMethodManager ) getSystemService( INPUT_METHOD_SERVICE );
                                                                imm.hideSoftInputFromWindow( cw.chatInput.getWindowToken(), 0 );
                                                                cw.finish();
                                                        }
                                                        Database.instance( context ).deleteChatBysRoomId( roomId );
                                                        Database.instance( context ).deleteChatRoomById( roomId );
                                                        TalkView.instance().resetData();
                                                }
                                                else
                                                {
                                                        if ( cw != null && cw.roomId.equals( roomId ) )
                                                        {
                                                                cw.insertData( chatId );
                                                                sendReadComplete( cw.roomId, cw.userIds, cw.userNames, getApplicationContext() );
                                                                Database.instance( context ).updateChatRoomRead( cw.roomId );
                                                                ArrayList<ArrayList<String>> arar = Database.instance( context ).selectChatRoomInfo( roomId );
                                                                if ( arar.size() != 1 ) return;
                                                                String[] ar = new String[2];
                                                                ar[0] = arar.get( 0 ).get( 1 );
                                                                ar[1] = arar.get( 0 ).get( 2 );
                                                                Message m = cw.uploadHandler.obtainMessage( Define.AM_USER_CHANGED, ar );
                                                                cw.uploadHandler.sendMessage( m );
                                                        }
                                                        TalkView.instance().resetData();
                                                }
                                        }
                                        else if ( content.equals( "[ROOM_IN]" ) )
                                        {
                                                if ( cw != null && cw.roomId.equals( roomId ) )
                                                {
                                                        cw.insertData( chatId );
                                                        sendReadComplete( cw.roomId, cw.userIds, cw.userNames, getApplicationContext() );
                                                        Database.instance( context ).updateChatRoomRead( cw.roomId );
                                                        ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfo( roomId );
                                                        if ( array == null || array.size() == 0 ) return;
                                                        String[] ar = new String[2];
                                                        ar[0] = array.get( 0 ).get( 1 );
                                                        ar[1] = array.get( 0 ).get( 2 );
                                                        Message m = cw.uploadHandler.obtainMessage( Define.AM_USER_CHANGED, ar );
                                                        cw.uploadHandler.sendMessage( m );
                                                }
                                                TalkView.instance().resetData();
                                        }
                                        else
                                        {
                                                if ( cw != null && cw.roomId.equals( roomId ) )
                                                {
                                                        cw.insertData( chatId );
                                                        sendReadComplete( cw.roomId, cw.userIds, cw.userNames, getApplicationContext() );
                                                        Database.instance( context ).updateChatRoomRead( cw.roomId );
                                                        if ( !Define.isScreenVisible( Define.mContext ) )
                                                        {
                                                                ActionManager.showAlertDialog( context, title, content, roomId, talkId );
                                                        }
                                                }
                                                else if ( cw != null && !cw.roomId.equals( roomId ) )
                                                {
                                                        if ( content.indexOf( "ATTACH://" ) >= 0 )
                                                        {
                                                                title = getString( R.string.fileReceive );
                                                                content = getString( R.string.fileReceiveMsg );
                                                        }
                                                        if ( Define.isScreenVisible( Define.mContext ) )
                                                        {
                                                                AlertDialogNotification.title = title;
                                                                AlertDialogNotification.message = content;
                                                                if ( Define.nowTopActivity != null )
                                                                {
                                                                        AlertDialogNotification nt = new AlertDialogNotification( Define.nowTopActivity );
                                                                        nt.show();
                                                                }
                                                                else
                                                                {
                                                                        AlertDialogNotification nt = new AlertDialogNotification( MainActivity.Instance() );
                                                                        nt.show();
                                                                }
                                                        }
                                                        else
                                                        {
                                                                ActionManager.showAlertDialog( context, title, content, roomId, talkId );
                                                        }
                                                }
                                                else
                                                {
                                                        if ( !talkId.equals( Define.getMyId( context ) ) )
                                                        {
                                                                if ( Define.isVisible == false )
                                                                {
                                                                        Log.d( "ShowDialog", "13" );
                                                                        if ( content.indexOf( "ATTACH://" ) < 0 ) ActionManager.showAlertDialog( context,
                                                                                        title, content, roomId, talkId );
                                                                        else ActionManager.showAlertDialog( context, getString( R.string.fileReceive ),
                                                                                        getString( R.string.fileReceiveMsg ), roomId, talkId );
                                                                }
                                                                else
                                                                {
                                                                        if ( content.indexOf( "ATTACH://" ) >= 0 )
                                                                        {
                                                                                title = getString( R.string.fileReceive );
                                                                                content = getString( R.string.fileReceiveMsg );
                                                                        }
                                                                        if ( Define.isScreenVisible( Define.mContext ) )
                                                                        {
                                                                                AlertDialogNotification.title = title;
                                                                                AlertDialogNotification.message = content;
                                                                                if ( Define.nowTopActivity != null )
                                                                                {
                                                                                        AlertDialogNotification nt = new AlertDialogNotification(
                                                                                                        Define.nowTopActivity );
                                                                                        nt.show();
                                                                                }
                                                                                else
                                                                                {
                                                                                        AlertDialogNotification nt = new AlertDialogNotification(
                                                                                                        MainActivity.Instance() );
                                                                                        nt.show();
                                                                                }
                                                                        }
                                                                        else
                                                                        {
                                                                                ActionManager.showAlertDialog( context, title, content, roomId, talkId );
                                                                        }
                                                                }
                                                        }
                                                }
                                                TalkView.instance().resetData();
                                        }
                                }
                                else if ( action.equals( Define.MSG_NOTIFY ) )
                                {
                                        String aTitle = intent.getStringExtra( "title" );
                                        String aContent = intent.getStringExtra( "content" );
                                        String aType = intent.getStringExtra( Define.ROOMID );
                                        if ( aType.equals( "[MESSAGE]" ) )
                                        {
                                                Message m = MessageView.instance().msgBoxHandler.obtainMessage( Define.AM_REFRESH );
                                                MessageView.instance().msgBoxHandler.sendMessage( m );
                                        }
                                        else if ( aType.equals( "[ALARM]" ) )
                                        {
                                                Message m = NotifyView.instance().alertHandler.obtainMessage( Define.AM_REFRESH );
                                                NotifyView.instance().alertHandler.sendMessage( m );
                                        }
                                        AlertDialogNotification.title = aTitle;
                                        AlertDialogNotification.message = aContent;
                                        if ( Define.nowTopActivity != null )
                                        {
                                                AlertDialogNotification nt = new AlertDialogNotification( Define.nowTopActivity );
                                                nt.show();
                                        }
                                        else
                                        {
                                                AlertDialogNotification nt = new AlertDialogNotification( MainActivity.Instance() );
                                                nt.show();
                                        }
                                }
                                /*
                                 * else if ( action.equals( Define.MSG_MY_NAME ) )
                                 * {
                                 * try
                                 * {
                                 * TextView nameView = ( TextView ) mainActivity.findViewById( R.id.nameLabel );
                                 * if ( nameView != null ) nameView.setText( Define.getMyName() );
                                 * }
                                 * catch ( Exception e )
                                 * {
                                 * e.printStackTrace();
                                 * }
                                 * }
                                 */
                                else if ( action.equals( Define.MSG_SEND_COMPLETE ) )
                                {
                                        if ( cw != null )
                                        {
                                                String msgId = intent.getStringExtra( "msgId" );
                                                Message m = cw.uploadHandler.obtainMessage( Define.AM_SEND_COMPLETE, msgId );
                                                cw.uploadHandler.sendMessage( m );
                                        }
                                }
                                else if ( action.equals( Define.MSG_USER_POPUP ) )
                                {
                                        String userId = intent.getStringExtra( "userId" );
                                        String userName = intent.getStringExtra( "userName" );
                                        ActionManager.popupUserInfo( getApplicationContext(), userId, userName, Define.getPartNameByUserId( userName ), "" );
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };

        @Override
        protected void onResume()
        {
                super.onResume();
                Define.mContext = this;
                Define.isHomeMode = false;
                switch ( Define.SET_COMPANY )
                {
                case Define.SAEHA :
                        if ( !AppUtil.isSaehaViewerInstalledCheck( getApplicationContext() ) )
                        {
                                Intent it = new Intent( getApplicationContext(), kr.co.ultari.atsmart.basic.subview.AppInstall.class );
                                it.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( it );
                                finish();
                        }
                        break;
                default :
                        break;
                }
                Intent intent = new Intent( MainActivity.this, AtSmartService.class );
                if ( !isServiceRunningCheck() ) startService( intent );
                bindService( intent, mConnection, BIND_AUTO_CREATE );
        };

        @Override
        protected void onPause()
        {
                super.onPause();
                unbindService( mConnection );
        }
        
        private static final String serviceName = "kr.co.ultari.atsmart.basic.service.AtSmartService";
        public boolean isServiceRunningCheck()
        {
                ActivityManager manager = ( ActivityManager ) this.getSystemService( Activity.ACTIVITY_SERVICE );
                for ( RunningServiceInfo service : manager.getRunningServices( Integer.MAX_VALUE ) )
                {
                        if ( serviceName.equals( service.service.getClassName() ) )
                        {
                                return true;
                        }
                }
                return false;
        }

        @Override
        protected void onUserLeaveHint()
        {
                Define.isHomeMode = true;
                super.onUserLeaveHint();
        }

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == R.id.main_chat )
                {
                        BuddyView.instance().createRoom();
                        ActionManager.tabs.handler.sendEmptyMessageDelayed( Define.AM_TAB_REFRESH, 100 );
                        MainActivity.mainHandler.sendEmptyMessageDelayed( Define.AM_MAIN_GROUPTAB_REFRESH, 100 );
                }
                else if ( v.getId() == R.id.main_exit )
                {
                        // Define.isBuddyAddMode = false;
                        // BuddyView.updateBuddylist();
                        ActionManager.tabs.handler.sendEmptyMessageDelayed( Define.AM_TAB_REFRESH, 100 );
                        MainActivity.mainHandler.sendEmptyMessageDelayed( Define.AM_MAIN_GROUPTAB_REFRESH, 100 );
                }
                else if( v.getId() == R.id.main_launcher )
                {
                        Intent selectWindow = new Intent( this, ExLauncher.class );
                        selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                        startActivity( selectWindow );
                }
        }

        @Override
        public void onBackPressed()
        {
                // super.onBackPressed();
                backPressCloseHandler.onBackPressed();
        }
}
