package kr.co.ultari.atsmart.basic.view;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.control.tree.MessengerTree;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.SearchResultItemData;
import kr.co.ultari.atsmart.basic.subview.SelectedBuddyItem;
import kr.co.ultari.atsmart.basic.util.FmcSendBroadcast;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.URLRunner;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import kr.co.ultari.atsmart.basic.view.MainViewTabFragment.TabListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint( "HandlerLeak" )
public class BuddyView extends MessengerTree implements Runnable {
        private static final String TAG = "/AtSmart/BuddyView";
        private static BuddyView buddyViewInstance = null;
        public static SelectedBuddyItem selected = null;
        public static TableLayout m_SelectedUserListLayout;
        public static ListView m_SelectedUserList;
        public LayoutInflater inflater = null;
        //private ImageButton btnGroup;
        private Button btnUser, btnOrganization, btnSearch, btnBuddyChat, btnBuddyNote, btnOrgSearch;
        private Vector<ArrayList<String>> partArray;
        private Vector<ArrayList<String>> userArray;
        private TabListener uiCallback;
        private Thread thread = null;
        private AmCodec codec = null;
        private boolean onDestroy = false;
        private UltariSSLSocket sc = null;
        public String noopStr = null;
        private boolean isReload = false;
        
        LinearLayout bottomLayout;
        ImageButton ibChat,ibDc, ibNote;
        TextView tvUserList, tvUserCount;

        public static BuddyView instance()
        {
                if ( buddyViewInstance == null )
                {
                        buddyViewInstance = new BuddyView();
                        buddyViewInstance.layoutResource = R.layout.activity_buddy;
                }
                return buddyViewInstance;
        }

        public void showMenu()
        {
        	bottomLayout.setVisibility(View.VISIBLE);
        }
        
        public void hideMenu()
        {
        	bottomLayout.setVisibility(View.INVISIBLE);
        }
        
        @Override
        public void onActivityCreated( Bundle savedInstanceState )
        {
                startProcess();
                super.onActivityCreated( savedInstanceState );
        }

        @Override
        public void onDestroy()
        {
                TRACE( "onDestroy" );
                onDestroy = true;
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
                thread = null;
                if ( buddyViewInstance != null ) buddyViewInstance = null;
                super.onDestroy();
        }
        
        public void onChangeSelected()
        {
        	Log.d("TREEITEM", "SelectedChanged1");
        	selectedIdAr.clear();
        	selectedNameAr.clear();
        	
        	String nameList = "";
        	
        	for ( int i = 0 ; i < items.size() ; i++ )
        	{
        		if ( items.get(i).isSelected && !items.get(i).isFolder )
        		{
        			selectedIdAr.add(items.get(i).id);
        			selectedNameAr.add(items.get(i).text);
        			
        			if ( !nameList.equals("") ) nameList += ",";
        			nameList += items.get(i).text;
        		}
        	}
        	
        	Log.d("TREEITEM", "SelectedChanged2");
        	
        	if ( selectedIdAr.size() > 0 ) showButtonMenu();
        	else hideButtonMenu();
        	
        	tvUserList.setText(nameList);
        	tvUserCount.setText("" + selectedIdAr.size());
        }
        
        public void showButtonMenu()
        {
        	Log.d("TREEITEM", "ShowMenu : " + bottomLayout);
        	bottomLayout.setVisibility(View.VISIBLE);
        }
        
        public void hideButtonMenu()
        {
        	Log.d("TREEITEM", "HideMenu : " + bottomLayout);
        	bottomLayout.setVisibility(View.INVISIBLE);
        }

        @SuppressLint( "InflateParams" )
        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                this.inflater = inflater;
                View view = inflater.inflate( R.layout.activity_buddy, null );
                super.init( view );
                btnUser = ( Button ) view.findViewById( R.id.userTabLeftButton );
                btnOrganization = ( Button ) view.findViewById( R.id.userTabCenterButton );
                btnSearch = ( Button ) view.findViewById( R.id.userTabRightButton );
                btnUser.setOnClickListener( this );
                btnOrganization.setOnClickListener( this );
                btnSearch.setOnClickListener( this );
                btnBuddyChat = ( Button ) view.findViewById( R.id.buddyChatButton );
                //btnGroup = ( ImageButton ) view.findViewById( R.id.btn_group );
                btnBuddyChat.setOnClickListener( this );
                //btnGroup.setOnClickListener( this );
                btnOrgSearch = ( Button ) view.findViewById( R.id.buddy_search );
                btnOrgSearch.setOnClickListener( this );
                btnBuddyNote = ( Button ) view.findViewById( R.id.buddyNoteButton );
                btnBuddyNote.setOnClickListener( this );
                if ( Define.useSendNoteMsg ) btnBuddyNote.setVisibility( View.VISIBLE );
                else btnBuddyNote.setVisibility( View.GONE );
                selected = new SelectedBuddyItem( context, this );
                m_SelectedUserListLayout = ( TableLayout ) view.findViewById( R.id.buddySelectedUserLayout );
                m_SelectedUserList = ( ListView ) view.findViewById( R.id.buddySelectedUserResult );
                m_SelectedUserList.setAdapter( selected );
                
                
                bottomLayout = (LinearLayout) view.findViewById(R.id.layout_buddy_bottom);
                ibChat = (ImageButton) view.findViewById(R.id.btn_buddy_chat);
				ibDc = (ImageButton) view.findViewById(R.id.btn_buddy_dc);
                ibNote = (ImageButton) view.findViewById(R.id.btn_buddy_sms);
                tvUserCount = (TextView) view.findViewById(R.id.tv_buddy_userCount);
                tvUserList = (TextView) view.findViewById(R.id.tv_buddy_userlist);
                
                ibChat.setOnClickListener(this);
                ibDc.setOnClickListener(this);
                ibNote.setOnClickListener(this);
                
                
                customHandler = new Handler() {
                        public void handleMessage( Message msg )
                        {
                                try
                                {
                                        if ( msg.what == Define.AM_SELECT_CHANGED )
                                        {
                                                selected.notifyDataSetChanged();
                                                if ( selected.getCount() == 0 )
                                                {
                                                        TableLayout.LayoutParams wParams = new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT,
                                                                        Define.DPFromPixel( 0 ) );
                                                        m_SelectedUserListLayout.setLayoutParams( wParams );
                                                        android.view.ViewGroup.LayoutParams lParams = m_SelectedUserList.getLayoutParams();
                                                        lParams.height = Define.DPFromPixel( 0 );
                                                        m_SelectedUserList.setLayoutParams( lParams );
                                                        Define.isBuddyAddMode = false;
                                                }
                                                else if ( selected.getCount() == 1 )
                                                {
                                                        TableLayout.LayoutParams wParams = new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT,
                                                                        Define.DPFromPixel( 80 ) );
                                                        m_SelectedUserListLayout.setLayoutParams( wParams );
                                                        android.view.ViewGroup.LayoutParams lParams = m_SelectedUserList.getLayoutParams();
                                                        lParams.height = Define.DPFromPixel( 50 );
                                                        m_SelectedUserList.setLayoutParams( lParams );
                                                        Define.isBuddyAddMode = true;
                                                }
                                                else if ( selected.getCount() == 2 )
                                                {
                                                        TableLayout.LayoutParams wParams = new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT,
                                                                        Define.DPFromPixel( 130 ) );
                                                        m_SelectedUserListLayout.setLayoutParams( wParams );
                                                        android.view.ViewGroup.LayoutParams lParams = m_SelectedUserList.getLayoutParams();
                                                        lParams.height = Define.DPFromPixel( 100 );
                                                        m_SelectedUserList.setLayoutParams( lParams );
                                                        Define.isBuddyAddMode = true;
                                                }
                                                else
                                                {
                                                        TableLayout.LayoutParams wParams = new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT,
                                                                        Define.DPFromPixel( 180 ) );
                                                        m_SelectedUserListLayout.setLayoutParams( wParams );
                                                        android.view.ViewGroup.LayoutParams lParams = m_SelectedUserList.getLayoutParams();
                                                        lParams.height = Define.DPFromPixel( 150 );
                                                        m_SelectedUserList.setLayoutParams( lParams );
                                                        Define.isBuddyAddMode = true;
                                                }
                                                updateBuddylist();
                                        }
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                };
                partArray = new Vector<ArrayList<String>>();
                partArray.clear();
                userArray = new Vector<ArrayList<String>>();
                userArray.clear();
                return view;
        }

        public void reloadBuddy()
        {
                try
                {
                        clear();
                        isReload = true;
                        sc.close();
                }
                catch ( Exception e )
                {}
        }

        public void startProcess()
        {
                if ( thread == null )
                {
                        TRACE( "buddyview startprocess : " + thread );
                        onDestroy = false;
                        codec = new AmCodec();
                        thread = new Thread( this );
                        thread.start();
                }
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
                        EXCEPTION( e );
                }
        }

        @Override
        public void OnCustomClick( View view )
        {
                if ( view == btnUser )
                {
                        // uiCallback.onTabClicked( Define.TAB_USER_BUDDY );
                        // MainViewTabFragment.setSubUserTab( Define.TAB_USER_BUDDY );
                }
                else if ( view == btnOrganization )
                {
                        Define.isBuddyAddMode = false;
                        updateBuddylist();
                        uiCallback.onTabClicked( Define.TAB_ORGANIZATION );
                        MainViewTabFragment.setSubUserTab( Define.TAB_ORGANIZATION );
                }
                /*
                 * else if ( view == btnSearch )
                 * {
                 * Define.isBuddyAddMode = false;
                 * updateBuddylist();
                 * uiCallback.onTabClicked( Define.TAB_USER_SEARCH );
                 * MainViewTabFragment.setSubUserTab( Define.TAB_USER_SEARCH );
                 * }
                 */
                else if ( view == btnBuddyChat )
                {
                        createRoom();
                }
                else if ( view == btnOrgSearch )
                {
                        Intent it = new Intent( context, kr.co.ultari.atsmart.basic.view.GroupSearchView.class );
                        startActivity( it );
                }
                else if ( view == btnBuddyNote )
                {
                        sendNote();
                }
                else if ( view == btnOrgSearch )
                {
                        Bundle bundle = new Bundle();
                        bundle.putString( "type", "organization" );
                        Intent intent = new Intent( context, kr.co.ultari.atsmart.basic.view.GroupSearchView.class );
                        intent.putExtras( bundle );
                        startActivity( intent );
                }
                else if ( view.getId() == R.id.btn_buddy_chat )
                {
                	String oUserIds = "";
                	boolean isMe = false;
                	
                	if ( selectedIdAr.size() == 1 && selectedIdAr.get(0).equals(Define.getMyId()) ) return;
                	
                	for ( int i = 0 ; i < selectedIdAr.size() ; i++ )
                	{
                		if ( !oUserIds.equals("") ) oUserIds += ",";
                		oUserIds += selectedIdAr.get(i);
                		
                		if ( selectedIdAr.get(i).equals(Define.getMyId() ) ) isMe = true;
                	}
                	
                	if ( !isMe )
                	{
                		if ( !oUserIds.equals("") ) oUserIds += ",";
                		oUserIds += Define.getMyId();
                	}
                	
                    String userIds = StringUtil.arrange( oUserIds );
                    
                    String userNames = "";
                    for ( int i = 0 ; i < selectedNameAr.size() ; i++ )
                	{
                		if ( !userNames.equals("") ) userNames += ",";
                		userNames += selectedNameAr.get(i);
                	}
                    
                    if ( !isMe )
                	{
                		if ( !userNames.equals("") ) userNames += ",";
                		userNames += Define.getMyName();
                	}
                    
                    userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                    
                    String roomId = userIds.replace( ",", "_" );
                    
                    ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfo( roomId );
                    
                    if ( array.size() == 0 ) Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames, StringUtil.getNowDateTime(), getString( R.string.newRoom ) );
                    
                    ActionManager.openChat( context, roomId, userIds, userNames );
                    
                    for ( int i = 0; i < items.size(); i++ )
                    {
                            if ( items.get( i ).isSelected )
                                    items.get( i ).onNormal();
                    }
                    
                    onChangeSelected();
                }
                else if ( view.getId() == R.id.btn_buddy_dc )
                {
                        //String url = "http://10.251.191.148:30006/UC/?C=kr.co.ultari.service.processor.web.UCProcessor&M=DispatchConference&Leader=2000&List=2001,2002";
                        //String url = "http://211.190.4.92:30001/call.log?caller=1111&type=call&start=20160106111111&end=20160106111111";
                        //new URLRunner(url, organizationHandler);
                        
                        if ( selectedIdAr.size() == 1 && selectedIdAr.get(0).equals(Define.getMyId()) ) return;
                        else if(selectedIdAr.size() == 1 && !selectedIdAr.get(0).equals(Define.getMyId()) )
                        {
                                String name = userMap.get( selectedIdAr.get(0) );
                                String[] ar = StringUtil.parseName( name );
                                
                                String mobile = ar[5];
                                String office = ar[3];
                                
                                if(mobile != null && !mobile.equals( "" ))
                                {
                                        mobile = mobile.replaceAll( "-", "" );
                                        FmcSendBroadcast.FmcSendCall( mobile ,0, context);
                                }
                                else if(office != null && !office.equals( "" ))
                                {
                                        office = office.replaceAll( "-", "" );
                                        FmcSendBroadcast.FmcSendCall( office ,0, context);
                                }
                        }
                        else
                        {
                                //D.C call
                        }
                        
                        for ( int i = 0; i < items.size(); i++ )
                        {
                                if ( items.get( i ).isSelected )
                                        items.get( i ).onNormal();
                        }
                        
                        onChangeSelected();
                }
                else if ( view == ibNote )
                {
                	String senderInfo = "";
                	
                	for ( int i = 0 ; i < selectedIdAr.size() ; i++ )
                	{
                		if ( selectedIdAr.get(i).equals(Define.getMyId()) ) continue;
                		
                		if ( !senderInfo.equals("") ) senderInfo += "/";
                		
                		senderInfo += selectedIdAr.get(i) + "\\" + selectedNameAr.get(i);
                	}
                	
                	if ( !senderInfo.equals(""))
                	{
	                	Intent i = new Intent( Define.mContext, kr.co.ultari.atsmart.basic.subview.SendMessageView.class );
	                    i.putExtra( "receivers", senderInfo );
	                    startActivity( i );
                	}
                	
                	for ( int i = 0; i < items.size(); i++ )
                        {
                                if ( items.get( i ).isSelected )
                                        items.get( i ).onNormal();
                        }
                	
                	onChangeSelected();
                }
                /*else if ( view == btnGroup )
                {
                        Define.isBuddyAddMode = !Define.isBuddyAddMode;
                        updateBuddylist();
                        if ( ActionManager.tabs != null ) ActionManager.tabs.handler.sendEmptyMessageDelayed( Define.AM_TAB_REFRESH, 100 );
                        if ( MainActivity.mainHandler != null ) MainActivity.mainHandler.sendEmptyMessageDelayed( Define.AM_MAIN_GROUPTAB_REFRESH, 100 );
                }*/
        }

        private void sendNote()
        {
                if ( Define.isBuddyAddMode )
                {
                        try
                        {
                                if ( selected.getCount() > 0 )
                                {
                                        String nowSelectedUserId = "";
                                        String nowSelectedUserName = "";
                                        for ( int i = 0; i < selected.getCount(); i++ )
                                        {
                                                SearchResultItemData data = selected.getItem( i );
                                                if ( !nowSelectedUserId.equals( "" ) ) nowSelectedUserId += ",";
                                                nowSelectedUserId += data.id;
                                                if ( !nowSelectedUserName.equals( "" ) ) nowSelectedUserName += ",";
                                                nowSelectedUserName += StringUtil.getNamePosition( data.name );
                                        }
                                        String oUserIds = nowSelectedUserId;
                                        String userIds = StringUtil.arrange( oUserIds );
                                        String userNames = StringUtil.arrangeNamesByIds( nowSelectedUserName, oUserIds );
                                        Define.isBuddyAddMode = false;
                                        updateBuddylist();
                                        if ( userIds != null && userNames != null )
                                        {
                                                Intent i = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.SendNote.class );
                                                i.putExtra( "USERID", userIds );
                                                i.putExtra( "USERNAME", userNames );
                                                startActivity( i );
                                        }
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        }

        public static void updateBuddylist()
        {
                try
                {
                        if ( Define.isBuddyAddMode == false )
                        {
                                BuddyView.instance().clearCheckBtn();
                                selected.clear();
                                selected.notifyDataSetChanged();
                                TableLayout.LayoutParams wParams = new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT, Define.DPFromPixel( 0 ) );
                                m_SelectedUserListLayout.setLayoutParams( wParams );
                                android.view.ViewGroup.LayoutParams lParams = m_SelectedUserList.getLayoutParams();
                                lParams.height = Define.DPFromPixel( 0 );
                                m_SelectedUserList.setLayoutParams( lParams );
                        }
                        else
                        {
                                BuddyView.instance().updateCheckBtn();
                        }
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
        }

        public void createRoom()
        {
                if ( Define.isBuddyAddMode )
                {
                        try
                        {
                                if ( selected.getCount() > 0 )
                                {
                                        String nowSelectedUserId = "";
                                        String nowSelectedUserName = "";
                                        for ( int i = 0; i < selected.getCount(); i++ )
                                        {
                                                SearchResultItemData data = selected.getItem( i );
                                                if ( !nowSelectedUserId.equals( "" ) ) nowSelectedUserId += ",";
                                                nowSelectedUserId += data.id;
                                                if ( !nowSelectedUserName.equals( "" ) ) nowSelectedUserName += ",";
                                                nowSelectedUserName += StringUtil.getNamePosition( data.name );
                                        }
                                        String oUserIds = nowSelectedUserId + "," + Define.getMyId( context );
                                        String userIds = StringUtil.arrange( oUserIds );
                                        String userNames = nowSelectedUserName + "," + StringUtil.getNamePosition( Define.getMyName() );
                                        userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                                        /*
                                         * String roomId = System.currentTimeMillis() + "";
                                         * ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfoByIds( userIds );
                                         * if ( array.size() == 0 )
                                         * {
                                         * Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames, StringUtil.getNowDateTime(), getString(
                                         * R.string.newRoom ) );
                                         * array = Database.instance( context ).selectChatRoomInfoByIds( userIds );
                                         * }
                                         * else
                                         * {
                                         * roomId = array.get( 0 ).get( 0 );
                                         * }
                                         */
                                        String roomId = "";
                                        if ( selected.getCount() > 1 ) roomId = "GROUP_" + StringUtil.getNowDateTime();
                                        else
                                        {
                                                String parse = StringUtil.arrange( userIds );
                                                roomId = parse.replace( ",", "_" );
                                        }
                                        ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfo( roomId );
                                        if ( array.size() == 0 )
                                        {
                                                Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames, StringUtil.getNowDateTime(),
                                                                getString( R.string.newRoom ) );
                                        }
                                        Define.isBuddyAddMode = false;
                                        updateBuddylist();
                                        ActionManager.openChat( context, roomId, userIds, userNames );
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        }

        @Override
        public void run()
        {
                TRACE( "BuddyView Thread Start" );
                StringBuffer sb = new StringBuffer();
                sc = null;
                InputStreamReader ir = null;
                BufferedReader br = null;
                char[] buf = new char[2048];
                int rcv = 0;
                Timer noopTimer = null;
                while ( onDestroy == false )
                {
                        try
                        {
                                sb.delete( 0, sb.length() );
                                Message m = buddyHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                buddyHandler.sendMessage( m );
                                if ( Define.getMyId( context ).equals( "" ) )
                                {
                                        try
                                        {
                                                Thread.sleep( 1000 );
                                        }
                                        catch ( Exception e )
                                        {}
                                        continue;
                                }
                                // showProgress( getString( R.string.buddy ), getString( R.string.buddylist ) );
                                System.out.println( "Connecting..." );
                                sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                                .getServerPort( Define.mContext ) ) );
                                sc.setSoTimeout( 150000 );
                                System.out.println( "Connected" );
                                ir = new InputStreamReader( sc.getInputStream() );
                                br = new BufferedReader( ir );
                                if ( buddyViewInstance == null ) return;
                                System.out.println( "ready to send : " + onDestroy );
                                if ( buddyViewInstance.noopStr == null ) buddyViewInstance.noopStr = codec.EncryptSEED( "noop" ) + "\f";
                                if ( noopTimer == null )
                                {
                                        noopTimer = new Timer();
                                        noopTimer.schedule( new NoopTimer(), 100000, 100000 );
                                }
                                if ( onDestroy == false )
                                {
                                    send( "MyFolderRequest\t" + Define.getMyId( context ) );
                                    while ( (rcv = br.read( buf, 0, 2047 )) >= 0 )
                                    {
                                        sb.append( new String( buf, 0, rcv ) );
                                        int pos;
                                        while ( (pos = sb.indexOf( "\f" )) >= 0 )
                                        {
                                            String rcvStr = codec.DecryptSEED( sb.substring( 0, pos ) );
                                            sb.delete( 0, pos + 1 );
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
                                            if ( Define.useTrace )
                                            {
                                                String vstr = command;
                                                for ( int i = 0; i < param.size(); i++ )
                                                {
                                                        vstr += ":";
                                                        vstr += param.get( i );
                                                }
                                                TRACE( "RCVVSTR : " + vstr );
                                            }
                                            if ( !command.equals( "noop" ) )
                                            {
                                                process( command, param );
                                            }
                                        }
                                    }
                                }
                        }
                        catch ( SocketException se )
                        {
                                TRACE( se.getMessage() );
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                        finally
                        {
                                TRACE( "buddyView run Finished" );
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
                                if ( buddyViewInstance != null ) buddyViewInstance.noopStr = null;
                                if ( noopTimer != null )
                                {
                                        noopTimer.cancel();
                                        noopTimer = null;
                                }
                        }
                        if ( onDestroy ) return;
                        if ( !isReload )
                        {
                                try
                                {
                                        Thread.sleep( 5000 );
                                }
                                catch ( InterruptedException ie )
                                {}
                        }
                        else
                        {
                                isReload = false;
                        }
                }
        }
        class NoopTimer extends TimerTask {
                public void run()
                {
                        if ( sc == null )
                        {
                                return;
                        }
                        try
                        {
                                sc.send( buddyViewInstance.noopStr );
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        };

        public void send( String msg ) throws Exception
        {
                msg.replaceAll( "\f", "" );
                msg = codec.EncryptSEED( msg );
                msg += "\f";
                sc.send( msg );
        }
        public Handler buddyHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_ADD_BUDDY_PART )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        if ( param.size() >= 5 )
                                        {
                                                String id = param.get( 0 );
                                                String high = param.get( 1 );
                                                String name = param.get( 3 );
                                                String order = param.get( 4 );
                                                addFolder( id, high, name, order, false );
                                        }
                                }
                                else if ( msg.what == Define.AM_ADD_BUDDY_USER )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        if ( param.size() >= 5 )
                                        {
                                                //Log.d( TAG, "ParamSize : " + param.size() );
                                                while ( param.size() < 9 )
                                                        param.add( "" );
                                                String id = param.get( 0 );
                                                String high = param.get( 1 );
                                                String icon = param.get( 2 );
                                                String name = param.get( 3 );
                                                String order = param.get( 4 );
                                                String param1 = param.get( 5 );
                                                String param2 = param.get( 6 );
                                                String mobile = param.get( 8 );
                                                /*
                                                Log.d( TAG, "Name : " + name );
                                                Log.d( TAG, "Order : " + order );
                                                Log.d( TAG, "Param1 : " + param1 );
                                                Log.d( TAG, "Param2 : " + param2 );
                                                Log.d( TAG, "Mobile : " + mobile );
                                                */
                                                addFile( id, high, name, param2, Integer.parseInt( icon ), order );
                                                if ( mobile != null && (mobile.equals( "0" ) || mobile.equals( "1" )) ) setMobileOn( param.get( 0 ),
                                                                Integer.parseInt( mobile ) );
                                                if ( Define.usePhoneState )
                                                {
                                                        if(param.size() > 9)
                                                        {
                                                                String uc = param.get( 9 );
                                                                //Log.d( TAG, "Uc : " + uc );
                                                                if ( uc != null && !uc.equals( "" ) ) setUc( param.get( 0 ), Integer.parseInt( uc ) );
                                                        }
                                                }
                                        }
                                }
                                /*
                                 * else if ( msg.what == Define.AM_MYFOLDER_ADD_USER )
                                 * {
                                 * @SuppressWarnings( "unchecked" )
                                 * ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                 * String id = param.get( 0 );
                                 * String high = param.get( 1 );
                                 * String name = param.get( 2 );
                                 * String icon = param.get( 3 );
                                 * String nick = "";
                                 * String order = "0";
                                 * if ( param.size() >= 5 )
                                 * nick = param.get( 4 );
                                 * if ( param.size() >= 6 )
                                 * order = param.get( 5 );
                                 * else
                                 * order = name;
                                 * if ( !"YES".equals( getString( R.string.SHOWME ) ) && id.equals( Define.getMyId( context ) ) )
                                 * return;
                                 * if ( icon.equals( "6" ) )
                                 * {
                                 * if ( name.equals( getString( R.string.myPart ) ) ) addFolder( id, high, name, icon, true );
                                 * else addFolder( id, high, name, icon, false );
                                 * }
                                 * else
                                 * {
                                 * if ( !id.equals( Define.getMyId( context ) ) )
                                 * {
                                 * addFile( high + "/" + id, high, name, nick, Integer.parseInt( icon ), order );
                                 * if ( param.size() >= 7 )
                                 * {
                                 * Define.searchMobileOn.put(high + "/" + id, param.get(6)); //2015-03-01
                                 * setMobileOn( high + "/" + id, Integer.parseInt( param.get( 6 ) ) );
                                 * }
                                 * else if ( param.size() == 6 )
                                 * {
                                 * Define.searchMobileOn.put(high + "/" + id, param.get(5)); //2015-03-01
                                 * setMobileOn( high + "/" + id, Integer.parseInt( param.get( 5 ) ) );
                                 * }
                                 * }
                                 * }
                                 * }
                                 */
                                else if ( msg.what == Define.AM_CLEAR_ITEM )
                                {
                                        clear();
                                }
                                else if ( msg.what == Define.AM_ICON )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        setIcon( param.get( 0 ), Integer.parseInt( param.get( 1 ) ) );
                                }
                                else if ( msg.what == Define.AM_UC_ICON )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        setUcIcon( param.get( 0 ), Integer.parseInt( param.get( 1 ) ) );
                                }
                                else if ( msg.what == Define.AM_NICK )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        if ( param.size() > 1 ) setInfo( param.get( 0 ), param.get( 1 ) );
                                        else setInfo( param.get( 0 ), "" );
                                }
                                else if ( msg.what == Define.AM_REFRESH )
                                {
                                        reloadBuddy();
                                        LayoutInflater inflater = getActivity().getLayoutInflater();
                                        View layout = inflater.inflate( R.layout.custom_toast,
                                                        ( ViewGroup ) getActivity().findViewById( R.id.custom_toast_layout ) );
                                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                        text.setText( getString( R.string.buddy_refresh ) );
                                        text.setTypeface( Define.tfRegular );
                                        Toast toast = new Toast( getActivity() );
                                        toast.setGravity( Gravity.CENTER, 0, 0 );
                                        toast.setDuration( Toast.LENGTH_SHORT );
                                        toast.setView( layout );
                                        toast.show();
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

        public void process( String command, ArrayList<String> param )
        {
                if ( command.equals( "MyFolder" ) && param.size() >= 4 )
                {
                        if ( !param.get( 2 ).equals( "6" ) )
                        {
                                userArray.add( param );
                        }
                        else
                        {
                                partArray.add( param );
                        }
                }
                else if ( command.equals( "Icon" ) )
                {
                        Message m = buddyHandler.obtainMessage( Define.AM_ICON, param );
                        buddyHandler.sendMessage( m );
                }
                else if ( command.equals( "PhoneStatus" ) )
                {
                        //Log.d( "Buddy", "phoneStatus:" + param.toString() );
                        Message m = buddyHandler.obtainMessage( Define.AM_UC_ICON, param );
                        buddyHandler.sendMessage( m );
                }
                else if ( command.equals( "Nick" ) )
                {
                        Message m = buddyHandler.obtainMessage( Define.AM_NICK, param );
                        buddyHandler.sendMessage( m );
                }
                else if ( command.equals( "MyFolderEnd" ) )
                {
                        for ( ArrayList<String> arr : partArray )
                        {
                                Message m = buddyHandler.obtainMessage( Define.AM_ADD_BUDDY_PART, arr );
                                buddyHandler.sendMessage( m );
                        }
                        for ( ArrayList<String> arr : userArray )
                        {
                                Message m = buddyHandler.obtainMessage( Define.AM_ADD_BUDDY_USER, arr );
                                buddyHandler.sendMessage( m );
                        }
                        partArray.clear();
                        userArray.clear();
                }
        }

        public void initSelectItem()
        {
                nowSelectedPartId = null;
                nowSelectedPartName = null;
                nowSelectedUserId = null;
                nowSelectedUserName = null;
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
}
