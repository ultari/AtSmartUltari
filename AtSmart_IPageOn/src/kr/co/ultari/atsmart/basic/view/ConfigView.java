package kr.co.ultari.atsmart.basic.view;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.ConfigData;
import kr.co.ultari.atsmart.basic.subview.ConfigItem;
import kr.co.ultari.atsmart.basic.subview.PinInputDialog;
import kr.co.ultari.atsmart.basic.util.AppUtil;
import kr.co.ultari.atsmart.basic.view.MainViewTabFragment.TabListener;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.util.TypedValue;

@SuppressLint( "InflateParams" )
public class ConfigView extends Fragment implements OnClickListener, AdapterView.OnItemClickListener {
        private static final String TAG = "/AtSmart/ConfigView";
        private static ConfigView configViewInstance = null;
        private ImageButton btnConfig;
        private ImageButton btnAccount;
        private TabListener uiCallback;
        public LayoutInflater inflater;
        public ConfigItem itemList = null;
        private ListView list;
        private TextView tvTitle, tvAccountTitle;
        private ImageButton btnSaehaviewer;
        private LinearLayout layoutSaeha;
        private final int resultBell = 111;

        public static ConfigView instance()
        {
                if ( configViewInstance == null ) configViewInstance = new ConfigView();
                return configViewInstance;
        }

        public void clear()
        {
                if ( configViewInstance != null ) configViewInstance = null;
        }

        public void onDestroy()
        {
                super.onDestroy();
                btnConfig.setImageBitmap( null );
                btnAccount.setImageBitmap( null );
                clear();
        }

        @Override
        public void onResume()
        {
                super.onResume();
                resetData();
        }
        public Handler configHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        if ( msg.what == Define.AM_REFRESH )
                        {
                                resetData();
                        }
                        super.handleMessage( msg );
                }
        };

        public void onItemClick( AdapterView<?> parent, View view, int position, long id )
        {
                try
                {
                        ConfigData item = itemList.getItem( position );
                        if ( item.key.equals( "nickname" ) )
                        {
                                Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.ConfigNickName.class );
                                startActivity( selectWindow );
                        }
                        else if( item.key.equals( "password" ))
                        {
                                Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.ConfigPassword.class );
                                startActivity( selectWindow );
                        }
                        else if ( item.key.equals( "photo" ) )
                        {
                                // Intent selectWindow = new Intent(getActivity(), kr.co.ultari.atsmart.basic.subview.ConfigPhoto.class);
                                // startActivity(selectWindow);
                        }
                        else if ( item.key.equals( "autoDeleteChat" ) )
                        {
                                Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.ConfigAutoDelete.class );
                                selectWindow.putExtra( "KEY", "AutoDeleteChat" );
                                startActivity( selectWindow );
                        }
                        else if ( item.key.equals( "autoDeleteNotify" ) )
                        {
                                Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.ConfigAutoDelete.class );
                                selectWindow.putExtra( "KEY", "AutoDeleteNotify" );
                                startActivity( selectWindow );
                        }
                        else if ( item.key.equals( "update" ) )
                        {
                                float newVersion = Float.parseFloat( Define.NEW_VERSION );
                                float version = Float.parseFloat( Define.VERSION );
                                if ( newVersion > version )
                                {
                                        Intent i = new Intent( getActivity(), kr.co.ultari.atsmart.basic.service.Launcher.class );
                                        startActivity( i );
                                }
                                else
                                {
                                        LayoutInflater inflater = getActivity().getLayoutInflater();
                                        View layout = inflater.inflate( R.layout.custom_toast,
                                                        ( ViewGroup ) getActivity().findViewById( R.id.custom_toast_layout ) );
                                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                        text.setText( getString( R.string.lastversion ) );
                                        text.setTypeface( Define.tfRegular );
                                        Toast toast = new Toast( getActivity() );
                                        toast.setGravity( Gravity.CENTER, 0, 0 );
                                        toast.setDuration( Toast.LENGTH_SHORT );
                                        toast.setView( layout );
                                        toast.show();
                                }
                        }
                        else if ( item.key.equals( "option" ) )
                        {
                                Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.OptionDialog.class );
                                startActivity( selectWindow );
                        }
                        else if ( item.key.equals( "background" ) )
                        {
                                Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.backgroundDialog.class );
                                startActivity( selectWindow );
                        }
                        else if ( item.key.equals( "serverInfo" ) )
                        {
                                Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.ServerInfo.class );
                                startActivity( selectWindow );
                        }
                        else if ( item.key.equals( "account" ) )
                        {
                                Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.view.AccountView.class );
                                selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( selectWindow );
                        }
                        else if ( item.key.equals( "helpWebLink" ) )
                        {
                                Intent i = new Intent( Intent.ACTION_VIEW );
                                Uri u = Uri.parse( "http://www.google.com" );
                                i.setData( u );
                                startActivity( i );
                        }
                        else if ( item.key.equals( "qnaWebLink" ) )
                        {
                                Intent i = new Intent( Intent.ACTION_VIEW );
                                Uri u = Uri.parse( "http://www.google.com" );
                                i.setData( u );
                                startActivity( i );
                        }
                        else if ( item.key.equals( "networkInfo" ) )
                        {
                                Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.NetworkInfo.class );
                                selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( selectWindow );
                        }
                        //2016-04-26
                        else if ( item.key.equals( "lock" ) )
                        {
                                Define.USE_PIN_MAIN = true;
                                Database.instance( getActivity() ).updateConfig( "PIN_MAIN", "ON" );
                                Intent selectWindow = new Intent( getActivity(), PinInputDialog.class );
                                selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( selectWindow );
                        }
                        else if( item.key.equals( "notice" ))
                        {
                                Intent i = new Intent( Intent.ACTION_VIEW );
                                Uri u = Uri.parse( Define.noticeUrl );
                                i.setData( u );
                                startActivity( i );
                        }
                        else if ( item.key.equals( "callType" ))
                        {
                        	Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.CallTypeInfo.class );
                        	selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                            startActivity( selectWindow );
                        }
                        else if ( item.key.equals( "bell" ))
                        {
                        	Intent selectWindow = new Intent( RingtoneManager.ACTION_RINGTONE_PICKER );
                        	selectWindow.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                        	selectWindow.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                        	startActivityForResult(selectWindow, resultBell);
                        }
                        /*else if ( item.key.equals( "privacy" ))
                        {
                        	Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.PrivacyView.class );
                        	selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                        	startActivity( selectWindow );
                        }*/
                        //
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                this.inflater = inflater;
                View view = inflater.inflate( R.layout.activity_config, null );
                Log.d( TAG, "ConfigCreated" );
                try
                {
                        btnConfig = ( ImageButton ) view.findViewById( R.id.configButton );
                        btnAccount = ( ImageButton ) view.findViewById( R.id.accountButton );
                        btnConfig.setOnClickListener( this );
                        btnAccount.setOnClickListener( this );
                        itemList = new ConfigItem( getActivity().getApplicationContext(), this );
                        list = ( ListView ) view.findViewById( R.id.configList );
                        list.setAdapter( itemList );
                        list.setOnItemClickListener( this );
                        tvTitle = ( TextView ) view.findViewById( R.id.config_title );
                        tvAccountTitle = ( TextView ) view.findViewById( R.id.config_title_account );
                        tvTitle.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 14 );
                        tvAccountTitle.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 14 );
                        layoutSaeha = ( LinearLayout ) view.findViewById( R.id.config_saeha_layout );
                        btnSaehaviewer = ( ImageButton ) view.findViewById( R.id.config_saehaButton );
                        btnSaehaviewer.setOnClickListener( this );
                        switch ( Define.SET_COMPANY )
                        {
                        case Define.SAEHA :
                                btnSaehaviewer.setVisibility( View.VISIBLE );
                                break;
                        default :
                                layoutSaeha.setVisibility( View.GONE );
                                break;
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return view;
        }

        public void resetData()
        {
                try
                {
                        itemList.clear();
                        itemList.notifyDataSetChanged();
                        itemList.add( new ConfigData( "photo", getString( R.string.changePhoto ), "USERID://" + Define.getMyId( getActivity() ),
                                        getString( R.string.changeMyPhoto ) ) );
                        if ( Define.useManualLoginMenu )
                        {
                                itemList.add( new ConfigData( "account_title", getString( R.string.account ), "", "" ) );
                                itemList.add( new ConfigData( "account", getString( R.string.account ), Define.getMyId(), getString( R.string.accountInfo ) ) );
                        }
                        itemList.add( new ConfigData( "setting", getString( R.string.config ), "", "" ) );
                        
                        //2016-04-26
                        switch(Define.SET_COMPANY)
                        {
                                case Define.EX:
                                        itemList.add( new ConfigData( "notice", "공지사항", "공지", "게시판을 확인 합니다.") );
                                        break;
                                
                                default:
                                        break;
                        }
                        
                        float newVersion = Float.parseFloat( Define.NEW_VERSION );
                        float version = Float.parseFloat( Define.VERSION );
                        if ( newVersion <= version ) 
                                itemList.add( new ConfigData( "update", "update", getString( R.string.installVersion ) + " : " + Define.VERSION, getString( R.string.noUpdate ) ) );
                        else if ( newVersion > version ) 
                                itemList.add( new ConfigData( "update", "update", getString( R.string.installVersion ) + ": "+ Define.VERSION, getString( R.string.newUpdate ) + "(" + Define.NEW_VERSION + ")" ) );
                        
                        //itemList.add(new ConfigData("password", getString(R.string.config_password), "", getString(R.string.config_password_msg)));
                        //itemList.add(new ConfigData("nickname", getString(R.string.nick), Define.getMyNickName(), getString(R.string.changeMyNick)));

                        String autoDeleteChat = Database.instance( getActivity() ).selectConfig( "AutoDeleteChat" );
                        if ( autoDeleteChat.equals( "1" ) ) autoDeleteChat = getString( R.string._1day );
                        else if ( autoDeleteChat.equals( "7" ) ) autoDeleteChat = getString( R.string._7day );
                        else if ( autoDeleteChat.equals( "30" ) ) autoDeleteChat = getString( R.string._30day );
                        else if ( autoDeleteChat.equals( "90" ) ) autoDeleteChat = getString( R.string._90day );
                        else if ( autoDeleteChat.equals( "180" ) ) autoDeleteChat = getString( R.string._180day );
                        else if ( autoDeleteChat.equals( "365" ) ) autoDeleteChat = getString( R.string._365day );
                        else autoDeleteChat = getString( R.string._0day );
                        
                        itemList.add( new ConfigData( "autoDeleteChat", getString( R.string.autoDelChat ), autoDeleteChat, getString( R.string.autoDelChatDesc ) ) );
                        String autoDeleteNotify = Database.instance( getActivity() ).selectConfig( "AutoDeleteNotify" );
                        if ( autoDeleteNotify.equals( "1" ) ) autoDeleteNotify = getString( R.string._1day );
                        else if ( autoDeleteNotify.equals( "7" ) ) autoDeleteNotify = getString( R.string._7day );
                        else if ( autoDeleteNotify.equals( "30" ) ) autoDeleteNotify = getString( R.string._30day );
                        else if ( autoDeleteNotify.equals( "90" ) ) autoDeleteNotify = getString( R.string._90day );
                        else if ( autoDeleteNotify.equals( "180" ) ) autoDeleteNotify = getString( R.string._180day );
                        else if ( autoDeleteNotify.equals( "365" ) ) autoDeleteNotify = getString( R.string._365day );
                        else autoDeleteNotify = getString( R.string._0day );
                        
                        itemList.add( new ConfigData( "autoDeleteNotify", getString( R.string.autoDelAlarm ), autoDeleteNotify, getString( R.string.autoDelAlarmDesc ) ) );
                        itemList.add( new ConfigData( "option", getString( R.string.option ), getString( R.string.optionSetting ), getString( R.string.optionSettingMsg ) ) );
                        if ( Define.useBackgroundImage ) itemList.add( new ConfigData( "background", getString( R.string.background ), getString( R.string.backgroundSetting ), getString( R.string.backgroundSettingMsg ) ) );
                        
                        /*itemList.add( new ConfigData( "serverInfo", getString( R.string.server_info ), getString( R.string.serverSetting ), getString( R.string.serverSettingMsg ) ) );
                        itemList.add( new ConfigData( "helpWebLink", getString( R.string.helplink ), getString( R.string.helplink_setting ),getString( R.string.helpmsg ) ) );
                        itemList.add( new ConfigData( "qnaWebLink", getString( R.string.qnalink ), getString( R.string.qnalink_setting ),getString( R.string.qnamsg ) ) );
                        */
                        
                        //2016-04-26
                        switch(Define.SET_COMPANY)
                        {
                                case Define.IPAGEON:
                                        itemList.add( new ConfigData( "lock", "PIN 설정", "잠금 설정","PIN 암호를 변경 합니다." ) );
                                        break;
                                default:
                                        break;
                        }
                        //
                        
                        String networkMode = "";
                        String mode = Database.instance( getActivity() ).selectConfig( "NETWORKMODE" );
                        if ( mode.equals( Integer.toString( Define.NETWORK_MODE_LTE ) ) ) Define.NETWORK_MODE = Define.NETWORK_MODE_LTE;
                        else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_WIFI ) ) ) Define.NETWORK_MODE = Define.NETWORK_MODE_WIFI;
                        else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_DISABLE ) ) || mode.equals( "" ) ) Define.NETWORK_MODE = Define.NETWORK_MODE_DISABLE;
                        switch ( Define.NETWORK_MODE )
                        {
                        case Define.NETWORK_MODE_LTE :
                                networkMode = getString( R.string.data_connect );
                                break;
                        case Define.NETWORK_MODE_WIFI :
                                networkMode = getString( R.string.wifi_connect );
                                break;
                        case Define.NETWORK_MODE_DISABLE :
                                networkMode = getString( R.string.network_option_disable );
                                break;
                        }
                        itemList.add( new ConfigData( "networkInfo", getString( R.string.network ), networkMode, getString( R.string.networkmsg ) ) );
                        tvTitle.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 14 );
                        tvAccountTitle.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 14 );

                        switch(Define.SET_COMPANY)
                        {
                                case Define.IPAGEON:
                                		String calltype = "";
                                		String call_mode = Database.instance( getActivity() ).selectConfig( "CALLCONNECTTYPE" );
                                		
                                		if ( call_mode.equals("") )
                                			Database.instance( getActivity() ).updateConfig("CALLCONNECTTYPE", Integer.toString(Define.CALL_TYPE_MODE_TLS));
                                		
                                		if ( Define.CALL_TYPE_MODE_TLS == Integer.parseInt(call_mode))
                                			calltype = "TLS";
                                		else if ( Define.CALL_TYPE_MODE_UDP == Integer.parseInt(call_mode))
                                			calltype = "UDP";
                                		itemList.add( new ConfigData( "callType", "통화 연결 방식", calltype , "통화 연결 방식을 설정합니다."));
                                		itemList.add( new ConfigData( "bell", "벨소리", "벨소리" , "벨소리를 설정합니다."));
                                		//itemList.add( new ConfigData( "privacy", "개인정보 이용 동의", "동의 확인", "개인정보 이용 동의 "));
                                        break;
                                default:
                                        break;
                        }

                        itemList.notifyDataSetChanged();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        @Override
        public void onActivityCreated( Bundle savedInstanceState )
        {
                super.onActivityCreated( savedInstanceState );
        }

        @Override
        public void onAttach( Activity activity )
        {
                super.onAttach( activity );
                try
                {
                        uiCallback = ( TabListener ) activity;
                }
                catch ( ClassCastException e )
                {
                        android.util.Log.e( "AtSmart", "Config", e );
                }
        }

        public void onClick( View view )
        {
                if ( view == btnConfig )
                {
                        uiCallback.onTabClicked( Define.TAB_SETTING );
                        MainViewTabFragment.setSubMoreTab( Define.TAB_SETTING );
                }
                else if ( view == btnSaehaviewer )
                {
                        Intent intent = new Intent( android.content.Intent.ACTION_VIEW, Uri.parse( "svcviewer://" ) );
                        intent.addCategory( android.content.Intent.CATEGORY_BROWSABLE );
                        PackageManager pm = getActivity().getPackageManager();
                        boolean isInstalled = !pm.queryIntentActivities( intent, PackageManager.MATCH_DEFAULT_ONLY ).isEmpty();
                        try
                        {
                                if ( !isInstalled ) AppUtil.install( AppUtil.MULTIVIEW, getActivity() );
                                else
                                {
                                        Uri uri = Uri.parse( "svcviewer://" );
                                        Intent it = new Intent( Intent.ACTION_VIEW, uri );
                                        it.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                        startActivity( it );
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        }

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
        
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
        	switch (requestCode) {
        	case resultBell:
        		if ( resultCode != 0)
        		{
        			Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
    				RingtoneManager.setActualDefaultRingtoneUri(getActivity(), RingtoneManager.TYPE_RINGTONE, uri);
    				//content://media/internal/audio/media/52
    				Log.e(TAG, "bell uri :"+uri.toString() + " , "+ RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
    				restartFmc();
        		}
				break;

			default:
				break;
			}
        }
        
        private void restartFmc()
        {
        	Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "RESTART");
            MainActivity.Instance().sendBroadcast(i);
        }
}
