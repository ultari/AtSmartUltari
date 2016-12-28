package kr.co.ultari.atsmart.basic.view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.control.tree.MessengerTree;
import kr.co.ultari.atsmart.basic.control.tree.TreeItem;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.SearchResultItemData;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint( "HandlerLeak" )
public class OrganizationView extends MessengerTree implements Runnable {
        private static final String TAG = "/AtSmart/OrganizationView";
        private static OrganizationView organizationViewInstance = null;
        private Button btnUser;
        private Button btnOrganization;
        private Button btnSearch;
        private Button btnOrgSearch;
        public TabListener uiCallback;
        private Thread thread;
        private AmCodec codec;
        private boolean onDestroy = false;
        private UltariSSLSocket sc = null;
        public String noopStr = null;
        
        LinearLayout bottomLayout;
        ImageButton ibChat,ibDc, ibNote;
        TextView tvUserList, tvUserCount;

        public static OrganizationView instance()
        {
                if ( organizationViewInstance == null )
                {
                        organizationViewInstance = new OrganizationView();
                        organizationViewInstance.layoutResource = R.layout.activity_organization;
                }
                return organizationViewInstance;
        }

        @Override
        public void onActivityCreated( Bundle savedInstanceState )
        {
                System.out.println( "onActivityCreated : " + Define.getMyId( context ) );
                if ( !(Define.getMyId( context ).equals( "" )) ) startProcess();
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
                organizationViewInstance = null;
                super.onDestroy();
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
                        android.util.Log.e( TAG, "Attach", e );
                }
        }

        @SuppressLint( "InflateParams" )
        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                View view = inflater.inflate( R.layout.activity_organization, null );
                super.init( view );
                btnUser = ( Button ) view.findViewById( R.id.userTabLeftButton );
                btnOrganization = ( Button ) view.findViewById( R.id.userTabCenterButton );
                btnSearch = ( Button ) view.findViewById( R.id.userTabRightButton );
                btnOrgSearch = ( Button ) view.findViewById( R.id.organization_search );
                btnOrgSearch.setTypeface( Define.tfRegular );
                btnOrgSearch.clearFocus();
                btnUser.setOnClickListener( this );
                btnOrganization.setOnClickListener( this );
                btnSearch.setOnClickListener( this );
                btnOrgSearch.setOnClickListener( this );
                
                bottomLayout = (LinearLayout) view.findViewById(R.id.layout_org_bottom);
                ibChat = (ImageButton) view.findViewById(R.id.btn_org_chat);
		ibDc = (ImageButton) view.findViewById(R.id.btn_org_dc);
                ibNote = (ImageButton) view.findViewById(R.id.btn_org_sms);
                tvUserCount = (TextView) view.findViewById(R.id.tv_org_userCount);
                tvUserList = (TextView) view.findViewById(R.id.tv_org_userlist);
                
                ibChat.setOnClickListener(this);
                ibDc.setOnClickListener(this);
                ibNote.setOnClickListener(this);
                
                
                return view;
        }

        public void onChangeSelected()
        {
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
        	
        	if ( selectedIdAr.size() > 0 ) showButtonMenu();
        	else hideButtonMenu();
        	
        	tvUserList.setText(nameList);
        	tvUserCount.setText("" + selectedIdAr.size());
        }
        
        public void showButtonMenu()
        {
        	bottomLayout.setVisibility(View.VISIBLE);
        }
        
        public void hideButtonMenu()
        {
        	bottomLayout.setVisibility(View.INVISIBLE);
        }
        		
        
        public void startProcess()
        {
                if ( thread == null )
                {
                        onDestroy = false;
                        codec = new AmCodec();
                        
                        thread = new Thread( this );
                        thread.start();
                }
        }

        @Override
        public void OnCustomClick( View view )
        {
                /*
                 * if ( view == btnUser )
                 * {
                 * uiCallback.onTabClicked( Define.TAB_USER_BUDDY );
                 * MainViewTabFragment.setSubUserTab( Define.TAB_USER_BUDDY );
                 * }
                 */
                if ( view == btnOrgSearch )
                {
                        Bundle bundle = new Bundle();
                        bundle.putString( "type", "organization" );
                        Intent intent = new Intent( context, kr.co.ultari.atsmart.basic.view.GroupSearchView.class );
                        intent.putExtras( bundle );
                        startActivity( intent );
                }
                else if ( view == ibChat )
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
                else if ( view == ibDc )
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
                /*
                 * else if ( view == btnOrganization )
                 * {
                 * uiCallback.onTabClicked( Define.TAB_USER_ORGANIZATION );
                 * MainViewTabFragment.setSubUserTab( Define.TAB_USER_ORGANIZATION );
                 * }
                 * else if ( view == btnSearch )
                 * {
                 * uiCallback.onTabClicked( Define.TAB_USER_SEARCH );
                 * MainViewTabFragment.setSubUserTab( Define.TAB_USER_SEARCH );
                 * }
                 */
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
                                sc.send( organizationViewInstance.noopStr );
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        };

        @Override
        public void run()
        {
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
                                sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                                .getServerPort( Define.mContext ) ) );
                                sc.setSoTimeout( 150000 );
                                ir = new InputStreamReader( sc.getInputStream() );
                                br = new BufferedReader( ir );
                                if ( organizationViewInstance == null ) return;
                                if ( organizationViewInstance.noopStr == null ) organizationViewInstance.noopStr = codec.EncryptSEED( "noop" ) + "\f";
                                if ( noopTimer == null )
                                {
                                        noopTimer = new Timer();
                                        noopTimer.schedule( new NoopTimer(), 100000, 100000 );
                                }
                                if ( userMap.size() > 0 )
                                {
                                        String idList = "";
                                        Iterator<String> it = userMap.keySet().iterator();
                                        while ( it.hasNext() )
                                        {
                                                if ( !idList.equals( "" ) ) idList += ",";
                                                idList += it.next();
                                        }
                                        if ( idList.equals( "" ) )
                                        {
                                                send( "USERSTATUS\t" + idList );
                                        }
                                }
                                if ( onDestroy == false )
                                {
                                        Message m = organizationHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                        organizationHandler.sendMessage( m );
                                        
                                        switch(Define.SET_COMPANY)
                                        {
                                                case Define.MBC:
                                                case Define.DEMO:
                                                        if(Define.usePhoneState)
                                                                send( "UcSubOrganizationRequest\t0" );
                                                        else
                                                                send( "SubOrganizationRequest\t0" );
                                                        break;
                                                
                                                default:
                                                        if ( Define.useMyTopPartVisible ) 
                                                        {
                                                                send( "SubOrganizationRequest\t0" + "\t" + Define.getMyId( context ) );
                                                        }
                                                        else 
                                                                send( "SubOrganizationRequest\t0" );
                                                        break;
                                        }
                                        
                                        /*if ( Define.useMyTopPartVisible ) 
                                                send( "SubOrganizationRequest\t0" + "\t" + Define.getMyId( context ) );
                                        else 
                                                send( "SubOrganizationRequest\t0" );*/
                                        
                                        while ( (rcv = br.read( buf, 0, 2047 )) >= 0 )
                                        {
                                                sb.append( new String( buf, 0, rcv ) );
                                                int pos;
                                                while ( (pos = sb.indexOf( "\f" )) >= 0 )
                                                {
                                                        String rcvStr = codec.DecryptSEED( sb.substring( 0, pos ) );
                                                        
                                                        //Log.d( TAG, "rcvStr Received : " + rcvStr );
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
                                if ( organizationViewInstance != null ) organizationViewInstance.noopStr = null;
                                if ( noopTimer != null )
                                {
                                        noopTimer.cancel();
                                        noopTimer = null;
                                }
                        }
                        if ( onDestroy ) return;
                        try
                        {
                                Thread.sleep( 5000 );
                        }
                        catch ( InterruptedException ie )
                        {}
                }
        }

        public void send( String msg ) throws Exception
        {
                if ( sc == null ) throw new Exception( "Not connected" );
                msg.replaceAll( "\f", "" );
                msg = codec.EncryptSEED( msg );
                msg += "\f";
                sc.send( msg );
        }
        public Handler organizationHandler = new Handler() {
                @SuppressWarnings( "unused" )
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_ADD_ORGANIZATION_PART )
                                {
                                        hideProgress();
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        System.out.println( "OrgPartParam : " + param.size() );
                                        if ( param.size() >= 4 )
                                        {
                                                String id = param.get( 0 );
                                                String high = param.get( 1 );
                                                String name = param.get( 2 );
                                                String order = param.get( 3 );
                                                if ( high.equals( "" ) ) high = "0";
                                                System.out.println( "[" + id + "]" + "[" + high + "]" + "[" + name + "]" + "[" + order + "]" );
                                                addFolder( id, high, name, order, false );
                                        }
                                }
                                else if ( msg.what == Define.AM_ADD_ORGANIZATION_USER )
                                {
                                        hideProgress();
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
                                                Log.d( TAG, "Uc : " + param.get( 7 ) );
                                                */
                                                
                                                addFile( id, high, name, param2, Integer.parseInt( icon ), order );
                                                if ( mobile != null && (mobile.equals( "0" ) || mobile.equals( "1" )) ) setMobileOn( param.get( 0 ),
                                                                Integer.parseInt( mobile ) );
                                                if ( Define.usePhoneState )
                                                {
                                                        if(param.size() > 9)
                                                        {
                                                                String uc = param.get( 9 );
                                                                if ( uc != null && !uc.equals( "" ) ) setUc( param.get( 0 ), Integer.parseInt( uc ) );
                                                        }
                                                }
                                        }
                                }
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
                                else if ( msg.what == Define.AM_HIDE_PROGRESS )
                                {
                                        hideProgress();
                                }
                                else if ( msg.what == 1111 )
                                {
                                	//Toast.makeText( Define.getContext(), (String)msg.obj, Toast.LENGTH_SHORT ).show();
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
                if ( command.equals( "Part" ) && param.size() >= 4 )
                {
                        Message m = organizationHandler.obtainMessage( Define.AM_ADD_ORGANIZATION_PART, param );
                        organizationHandler.sendMessage( m );
                }
                else if ( command.equals( "User" ) && param.size() >= 5 )
                {
                        Message m = organizationHandler.obtainMessage( Define.AM_ADD_ORGANIZATION_USER, param );
                        organizationHandler.sendMessage( m );
                }
                else if ( command.equals( "OrganizationEnd" ) )
                {
                        /*
                         * Message m = organizationHandler.obtainMessage( Define.AM_HIDE_PROGRESS, null );
                         * organizationHandler.sendMessage( m );
                         */
                        
                        try
                        {
                                Thread.sleep( 200 );
                        }
                        catch ( InterruptedException e )
                        {
                                e.printStackTrace();
                        }
                        ActionManager.hideProgressDialog();
                }
                else if ( command.equals( "Icon" ) )
                {
                        Message m = organizationHandler.obtainMessage( Define.AM_ICON, param );
                        organizationHandler.sendMessage( m );
                }
                else if ( command.equals( "PhoneStatus" ) )
                {
                        //Log.d( "ORG", "phoneStatus:" + param.toString() );
                        Message m = organizationHandler.obtainMessage( Define.AM_UC_ICON, param );
                        organizationHandler.sendMessage( m );
                }
                else if ( command.equals( "Nick" ) )
                {
                        Message m = organizationHandler.obtainMessage( Define.AM_NICK, param );
                        organizationHandler.sendMessage( m );
                }
        }

        public void OnFirstExpand( String str )
        {
                ActionManager.showProcessingDialog( context, getString( R.string.login ), getString( R.string.waitLogin ) );
                try
                {
                        //2016-02-01
                        switch(Define.SET_COMPANY)
                        {
                                case Define.MBC:
                                case Define.DEMO:
                                        if(Define.usePhoneState)
                                                send( "UcSubOrganizationRequest\t" + str );
                                        else
                                                send( "SubOrganizationRequest\t" + str );
                                        break;
                                
                                default:
                                        if ( Define.useMyTopPartVisible ) 
                                                send( "SubOrganizationRequest\t" + str + "\t" + Define.getMyId( context ) );
                                        else 
                                                send( "SubOrganizationRequest\t" + str );
                                        break;
                        }
                        
                        /*if ( Define.useMyTopPartVisible ) 
                                send( "SubOrganizationRequest\t" + str + "\t" + Define.getMyId( context ) );
                        else 
                                send( "SubOrganizationRequest\t" + str );*/
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
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
                android.util.Log.i( TAG, ":!!!" + s );
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
