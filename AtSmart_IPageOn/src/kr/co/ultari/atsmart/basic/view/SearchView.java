package kr.co.ultari.atsmart.basic.view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.subdata.SearchResultItemData;
import kr.co.ultari.atsmart.basic.subview.SearchEdit;
import kr.co.ultari.atsmart.basic.subview.SearchResultItem;
import kr.co.ultari.atsmart.basic.subview.SelectedUserItem;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import kr.co.ultari.atsmart.basic.view.MainViewTabFragment.TabListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

@SuppressLint( "HandlerLeak" )
public class SearchView extends Fragment implements OnClickListener, Runnable, OnItemClickListener, OnTouchListener {
        private static final String TAG = "/AtSmart/SearchView";
        private static SearchView searchViewInstance = null;
        private Button btnUser, btnOrganization, btnSearch, btnSearchType, btnClose, chatButton, noteButton;
        private int phoneNumberIndex;
        TabListener uiCallback;
        public LayoutInflater inflater;
        private static PopupWindow searchTypePopup = null;
        private static Button[] btnSearchs = new Button[6];
        private static final String[] searchType = new String[6];
        Context context;
        private ImageButton btnSearchStart = null;
        static SearchEdit edit = null;
        private Thread searchThread = null;
        private AmCodec codec;
        public String noopStr = null;
        boolean onDestroy = false;
        public Bitmap statusOnlineBitmap;
        public Bitmap statusAwayBitmap;
        public Bitmap statusBusyBitmap;
        public Bitmap statusPhoneBitmap;
        public Bitmap statusMeetingBitmap;
        public Bitmap statusOfflineBitmap;
        public Bitmap checkedImage;
        public Bitmap unCheckedImage;
        public ListView listView;
        private static SearchResultItem result;
        public static ListView m_SelectedUserList;
        public static SelectedUserItem selected;
        public static TableLayout m_SelectedUserListLayout;
        public static TableLayout m_BottomTab = null;
        UltariSSLSocket sc = null;
        Timer timer;
        private String nowSelectedUserId = null;
        private String nowSelectedUserName = null;

        public static SearchView instance()
        {
                if ( searchViewInstance == null ) searchViewInstance = new SearchView();
                return searchViewInstance;
        }

        @SuppressLint( { "ClickableViewAccessibility", "InflateParams" } )
        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                this.inflater = inflater;
                context = getActivity().getApplicationContext();
                View view = inflater.inflate( R.layout.activity_search, null );
                try
                {
                        btnUser = ( Button ) view.findViewById( R.id.userTabLeftButton );
                        btnOrganization = ( Button ) view.findViewById( R.id.userTabCenterButton );
                        btnSearch = ( Button ) view.findViewById( R.id.userTabRightButton );
                        btnSearchType = ( Button ) view.findViewById( R.id.searchType );
                        btnSearchStart = ( ImageButton ) view.findViewById( R.id.search );
                        btnUser.setOnClickListener( this );
                        btnOrganization.setOnClickListener( this );
                        btnSearch.setOnClickListener( this );
                        btnSearchType.setOnClickListener( this );
                        btnSearchStart.setOnClickListener( this );
                        edit = ( SearchEdit ) view.findViewById( R.id.searchText );
                        edit.setOnEditorActionListener( new OnEditorActionListener() {
                                @Override
                                public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
                                {
                                        if ( actionId == EditorInfo.IME_ACTION_SEARCH )
                                        {
                                                searchStarts();
                                        }
                                        return false;
                                }
                        } );
                        codec = new AmCodec();
                        if ( Define.useStatusPcIcon )
                        {
                                statusOnlineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_online );
                                statusAwayBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_away );
                                statusBusyBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_busy );
                                statusPhoneBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_phone );
                                statusMeetingBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_meeting );
                                statusOfflineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_offline );
                        }
                        else
                        {
                                statusOnlineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.basic_status_online );
                                statusAwayBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.basic_status_away );
                                statusBusyBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.basic_status_busy );
                                statusPhoneBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.basic_status_phone );
                                statusMeetingBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.basic_status_meeting );
                                statusOfflineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.basic_status_offline );
                        }
                        checkedImage = BitmapFactory.decodeResource( getResources(), R.drawable.search_icon_check );
                        unCheckedImage = BitmapFactory.decodeResource( getResources(), R.drawable.search_icon_uncheck );
                        result = new SearchResultItem( getActivity().getApplicationContext(), this );
                        listView = ( ListView ) view.findViewById( R.id.SearchUserResult );
                        listView.setAdapter( result );
                        listView.setClickable( true );
                        listView.setOnItemClickListener( this );
                        selected = new SelectedUserItem( getActivity().getApplicationContext(), this );
                        m_SelectedUserListLayout = ( TableLayout ) view.findViewById( R.id.SelectedUserLayout );
                        m_SelectedUserList = ( ListView ) view.findViewById( R.id.SelectedUserResult );
                        m_SelectedUserList.setAdapter( selected );
                        chatButton = ( Button ) view.findViewById( R.id.chatButton );
                        chatButton.setOnClickListener( this );
                        noteButton = ( Button ) view.findViewById( R.id.noteButton );
                        noteButton.setOnClickListener( this );
                        if ( Define.useSendNoteMsg ) noteButton.setVisibility( View.VISIBLE );
                        else noteButton.setVisibility( View.GONE );
                        m_BottomTab = ( TableLayout ) view.findViewById( R.id.SearchBottomTab );
                        btnClose = ( Button ) view.findViewById( R.id.closeBtn );
                        btnClose.setOnClickListener( this );
                        if ( !Define.isAddUserMode ) btnClose.setVisibility( View.GONE );
                        searchType[0] = getString( R.string.search_type_name );
                        searchType[1] = getString( R.string.search_type_part );
                        searchType[2] = getString( R.string.search_type_id );
                        searchType[3] = getString( R.string.search_type_position );
                        searchType[4] = getString( R.string.search_type_phone );
                        searchType[5] = getString( R.string.search_type_mobile );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return view;
        }

        public void searchStarts()
        {
                try
                {
                        if ( !Define.getMyId( context ).equals( "" ) && !edit.getText().toString().equals( "" ) )
                        {
                                ActionManager.showProcessingDialog( getActivity(), getString( R.string.search ), getString( R.string.searchList ) );
                                if ( searchThread == null )
                                {
                                        searchThread = new Thread( this );
                                        searchThread.start();
                                }
                                else
                                {
                                        Message m = searchHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                        searchHandler.sendMessage( m );
                                        short type = 0;
                                        for ( int i = searchType.length - 1; i >= 0; i-- )
                                        {
                                                if ( btnSearchType.getText().equals( searchType[i] ) )
                                                {
                                                        type = ( short ) i;
                                                        break;
                                                }
                                        }
                                        try
                                        {
                                                send( "SearchRequest\t" + type + "\t" + edit.getText() );
                                        }
                                        catch ( Exception e )
                                        {
                                                EXCEPTION( e );
                                        }
                                }
                        }
                        if ( searchTypePopup != null )
                        {
                                searchTypePopup.dismiss();
                                searchTypePopup = null;
                                for ( int i = 0; i < btnSearchs.length; i++ )
                                        btnSearchs[i] = null;
                        }
                        if ( m_BottomTab != null ) m_BottomTab.setVisibility( View.VISIBLE );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void setVisibleBottomTab()
        {
                try
                {
                        if ( Define.isAddUserMode )
                        {
                                if ( m_BottomTab != null ) m_BottomTab.setVisibility( View.GONE );
                                btnClose.setVisibility( View.VISIBLE );
                        }
                        else
                        {
                                if ( m_BottomTab != null ) m_BottomTab.setVisibility( View.VISIBLE );
                                btnClose.setVisibility( View.GONE );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        @Override
        public boolean onContextItemSelected( MenuItem item )
        {
                try
                {
                        if ( item.getItemId() == Define.MENU_ID_CALL )
                        {
                                if ( nowSelectedUserName != null )
                                {
                                        switch ( Define.SET_COMPANY )
                                        {
                                        case Define.REDCROSS :
                                                String[] ar = StringUtil.parseName( nowSelectedUserName );
                                                if ( !ar[3].equals( "" ) || !ar[4].equals( "" ) )
                                                {
                                                        final String tmp[] = { getString( R.string.search_type_phone ) + ": " + ar[3],
                                                                        getString( R.string.search_type_mobile ) + ": " + ar[4] };
                                                        final String items[] = { ar[3], ar[4] };
                                                        phoneNumberIndex = 0;
                                                        AlertDialog.Builder ab = new AlertDialog.Builder( this.inflater.getContext() );
                                                        ab.setTitle( getString( R.string.select ) );
                                                        ab.setSingleChoiceItems( tmp, 0, new DialogInterface.OnClickListener() {
                                                                public void onClick( DialogInterface dialog, int whichButton )
                                                                {
                                                                        phoneNumberIndex = whichButton;
                                                                }
                                                        } ).setPositiveButton( getString( R.string.call ), new DialogInterface.OnClickListener() {
                                                                public void onClick( DialogInterface dialog, int whichButton )
                                                                {
                                                                        if ( !items[phoneNumberIndex].equals( "" ) ) ActionManager.callPhone( context,
                                                                                        items[phoneNumberIndex] );
                                                                }
                                                        } ).setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                                                                public void onClick( DialogInterface dialog, int whichButton )
                                                                {
                                                                }
                                                        } );
                                                        ab.show();
                                                }
                                                break;
                                        default :
                                                String[] ar2 = StringUtil.parseName( nowSelectedUserName );
                                                if ( !ar2[3].equals( "" ) || !ar2[5].equals( "" ) )
                                                {
                                                        final String tmp[] = { getString( R.string.search_type_phone ) + ": " + ar2[3],
                                                                        getString( R.string.search_type_mobile ) + ": " + ar2[5] };
                                                        final String items[] = { ar2[3], ar2[5] };
                                                        phoneNumberIndex = 0;
                                                        AlertDialog.Builder ab = new AlertDialog.Builder( this.inflater.getContext() );
                                                        ab.setTitle( getString( R.string.select ) );
                                                        ab.setSingleChoiceItems( tmp, 0, new DialogInterface.OnClickListener() {
                                                                public void onClick( DialogInterface dialog, int whichButton )
                                                                {
                                                                        phoneNumberIndex = whichButton;
                                                                }
                                                        } ).setPositiveButton( getString( R.string.call ), new DialogInterface.OnClickListener() {
                                                                public void onClick( DialogInterface dialog, int whichButton )
                                                                {
                                                                        if ( !items[phoneNumberIndex].equals( "" ) ) ActionManager.callPhone( context,
                                                                                        items[phoneNumberIndex] );
                                                                }
                                                        } ).setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                                                                public void onClick( DialogInterface dialog, int whichButton )
                                                                {
                                                                }
                                                        } );
                                                        ab.show();
                                                }
                                                break;
                                        }
                                }
                                nowSelectedUserName = null;
                        }
                        else if ( item.getItemId() == Define.MENU_ID_INFO )
                        {
                                if ( nowSelectedUserId != null && nowSelectedUserName != null ) ActionManager.popupUserInfo( getActivity(), nowSelectedUserId,
                                                nowSelectedUserName, Define.getPartNameByUserId( nowSelectedUserName ), "" );
                                nowSelectedUserId = null;
                                nowSelectedUserName = null;
                        }
                        else if ( item.getItemId() == Define.MENU_ID_CHAT )
                        {
                                if ( nowSelectedUserId != null && nowSelectedUserName != null )
                                {
                                        String oUserIds = nowSelectedUserId + "," + Define.getMyId( context );
                                        String userIds = StringUtil.arrange( oUserIds );
                                        String userNames = StringUtil.getNamePosition( nowSelectedUserName ) + ","
                                                        + StringUtil.getNamePosition( Define.getMyName() );
                                        userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                                        String roomId = userIds.replace( ",", "_" );
                                        ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfo( roomId );
                                        if ( array.size() == 0 ) Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames,
                                                        StringUtil.getNowDateTime(), getString( R.string.newRoom ) );
                                        ActionManager.openChat( getActivity(), roomId, userIds, userNames );
                                }
                                nowSelectedUserId = null;
                                nowSelectedUserName = null;
                        }
                        else if ( item.getItemId() == Define.MENU_ID_NOTE )
                        {
                                if ( nowSelectedUserId != null && nowSelectedUserName != null )
                                {
                                        String[] parse = nowSelectedUserName.split( "#" );
                                        Intent i = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.SendNote.class );
                                        i.putExtra( "USERID", nowSelectedUserId );
                                        i.putExtra( "USERNAME", parse[0] + " " + parse[1] );
                                        startActivity( i );
                                        nowSelectedUserId = null;
                                        nowSelectedUserName = null;
                                }
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return super.onContextItemSelected( item );
        }

        @Override
        public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo )
        {
                super.onCreateContextMenu( menu, v, menuInfo );
                menu.setHeaderTitle( StringUtil.getNamePosition( nowSelectedUserName ) );
                menu.add( 0, Define.MENU_ID_CHAT, Menu.NONE, getString( R.string.chat ) );
                menu.add( 0, Define.MENU_ID_INFO, Menu.NONE, getString( R.string.info ) );
                menu.add( 0, Define.MENU_ID_CALL, Menu.NONE, getString( R.string.calling ) );
                if ( Define.useSendNoteMsg ) menu.add( 0, Define.MENU_ID_NOTE, Menu.NONE, getString( R.string.send ) );
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
        public void onDestroy()
        {
                try
                {
                        onDestroy = true;
                        TRACE( "onDestroy" );
                        if ( sc != null )
                        {
                                try
                                {
                                        sc.close();
                                }
                                catch ( Exception e )
                                {
                                        e.printStackTrace();
                                }
                        }
                        searchViewInstance = null;
                        searchThread = null;
                        btnSearchStart.setImageBitmap( null );
                        statusOnlineBitmap.recycle();
                        statusAwayBitmap.recycle();
                        statusBusyBitmap.recycle();
                        statusPhoneBitmap.recycle();
                        statusMeetingBitmap.recycle();
                        statusOfflineBitmap.recycle();
                        checkedImage.recycle();
                        unCheckedImage.recycle();
                        statusOnlineBitmap = null;
                        statusAwayBitmap = null;
                        statusBusyBitmap = null;
                        statusPhoneBitmap = null;
                        statusMeetingBitmap = null;
                        statusOfflineBitmap = null;
                        checkedImage = null;
                        unCheckedImage = null;
                        searchViewInstance = null;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                super.onDestroy();
        }

        private void updateSearchPopup( String type )
        {
                try
                {
                        for ( int i = btnSearchs.length - 1; i >= 0; i-- )
                        {
                                if ( type.equals( btnSearchs[i].getText() ) ) btnSearchs[i].setBackgroundColor( 0xFF5077AD );
                                else btnSearchs[i].setBackgroundColor( 0xFF3B5981 );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        private void updateSearchName( String name )
        {
                try
                {
                        btnSearchType.setText( name );
                        searchTypePopup.dismiss();
                        searchTypePopup = null;
                        for ( int i = 0; i < btnSearchs.length; i++ )
                                btnSearchs[i] = null;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        @SuppressLint( "InflateParams" )
        public void onClick( View view )
        {
                if ( searchTypePopup != null && view != btnSearchs[0] && view != btnSearchs[1] && view != btnSearchs[2] && view != btnSearchs[3]
                                && view != btnSearchs[4] && view != btnSearchs[5] )
                {
                        try
                        {
                                searchTypePopup.dismiss();
                                searchTypePopup = null;
                                for ( int i = 0; i < btnSearchs.length; i++ )
                                        btnSearchs[i] = null;
                                return;
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
                try
                {
                        /*
                         * if ( view == btnUser )
                         * {
                         * uiCallback.onTabClicked(Define.TAB_USER_BUDDY);
                         * MainViewTabFragment.setSubUserTab(Define.TAB_USER_BUDDY);
                         * if(searchTypePopup != null)
                         * {
                         * searchTypePopup.dismiss();
                         * searchTypePopup = null;
                         * for(int i=0; i<btnSearchs.length; i++)
                         * btnSearchs[i] = null;
                         * }
                         * initSelectBox();
                         * hideKeyboard();
                         * Message m = searchHandler.obtainMessage(Define.AM_CLEAR_ITEM, null);
                         * searchHandler.sendMessage(m);
                         * }
                         */
                        /*
                         * else if ( view == btnOrganization )
                         * {
                         * uiCallback.onTabClicked(Define.TAB_USER_ORGANIZATION);
                         * MainViewTabFragment.setSubUserTab(Define.TAB_USER_ORGANIZATION);
                         * if(searchTypePopup != null)
                         * {
                         * searchTypePopup.dismiss();
                         * searchTypePopup = null;
                         * for(int i=0; i<btnSearchs.length; i++)
                         * btnSearchs[i] = null;
                         * }
                         * initSelectBox();
                         * hideKeyboard();
                         * Message m = searchHandler.obtainMessage(Define.AM_CLEAR_ITEM, null);
                         * searchHandler.sendMessage(m);
                         * }
                         * else if ( view == btnSearch )
                         * {
                         * uiCallback.onTabClicked(Define.TAB_USER_SEARCH);
                         * MainViewTabFragment.setSubUserTab(Define.TAB_USER_SEARCH);
                         * if(searchTypePopup != null)
                         * {
                         * searchTypePopup.dismiss();
                         * searchTypePopup = null;
                         * for(int i=0; i<btnSearchs.length; i++)
                         * btnSearchs[i] = null;
                         * }
                         * initSelectBox();
                         * hideKeyboard();
                         * Message m = searchHandler.obtainMessage(Define.AM_CLEAR_ITEM, null);
                         * searchHandler.sendMessage(m);
                         * }
                         */
                        if ( view == btnSearchStart )
                        {
                                searchStarts();
                                hideKeyboard();
                        }
                        else if ( view == btnSearchType )
                        {
                                View popupView = inflater.inflate( R.layout.search_type_popup, null );
                                searchTypePopup = new PopupWindow( popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
                                btnSearchs[0] = ( Button ) popupView.findViewById( R.id.searchTypeName );
                                btnSearchs[1] = ( Button ) popupView.findViewById( R.id.searchTypePart );
                                btnSearchs[2] = ( Button ) popupView.findViewById( R.id.searchTypeId );
                                btnSearchs[3] = ( Button ) popupView.findViewById( R.id.searchTypePosition );
                                btnSearchs[4] = ( Button ) popupView.findViewById( R.id.searchTypePhone );
                                btnSearchs[5] = ( Button ) popupView.findViewById( R.id.searchTypeMobile );
                                for ( int i = 0; i < btnSearchs.length; i++ )
                                        btnSearchs[i].setOnClickListener( this );
                                updateSearchPopup( btnSearchType.getText().toString() );
                                searchTypePopup.showAsDropDown( btnSearchType, 0, 0 );
                        }
                        else if ( btnSearchs[0] != null && view == btnSearchs[0] )
                        {
                                updateSearchName( btnSearchs[0].getText().toString() );
                        }
                        else if ( btnSearchs[1] != null && view == btnSearchs[1] )
                        {
                                updateSearchName( btnSearchs[1].getText().toString() );
                        }
                        else if ( btnSearchs[2] != null && view == btnSearchs[2] )
                        {
                                updateSearchName( btnSearchs[2].getText().toString() );
                        }
                        else if ( btnSearchs[3] != null && view == btnSearchs[3] )
                        {
                                updateSearchName( btnSearchs[3].getText().toString() );
                        }
                        else if ( btnSearchs[4] != null && view == btnSearchs[4] )
                        {
                                updateSearchName( btnSearchs[4].getText().toString() );
                        }
                        else if ( btnSearchs[5] != null && view == btnSearchs[5] )
                        {
                                updateSearchName( btnSearchs[5].getText().toString() );
                        }
                        else if ( view == chatButton )
                        {
                                openChat();
                                if ( Define.isAddUserMode )
                                {
                                        Define.isAddUserMode = false;
                                        btnClose.setVisibility( View.GONE );
                                }
                                initSelectBox();
                                hideKeyboard();
                                ActionManager.tabs.m_Layout.setVisibility( View.VISIBLE );
                                m_BottomTab.setVisibility( View.VISIBLE );
                                ActionManager.tabs.onTabSelected( Define.TAB_CHAT );
                                ActionManager.tabs.moveChatTab();
                                Message m = searchHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                searchHandler.sendMessage( m );
                        }
                        else if ( view == noteButton )
                        {
                                sendNote();
                        }
                        else if ( view == btnClose )
                        {
                                if ( Define.isAddUserMode )
                                {
                                        Define.isAddUserMode = false;
                                        m_BottomTab.setVisibility( View.VISIBLE );
                                        ActionManager.tabs.m_Layout.setVisibility( View.VISIBLE );
                                        btnClose.setVisibility( View.GONE );
                                        Intent intent = new Intent( getActivity().getApplicationContext(), kr.co.ultari.atsmart.basic.view.ChatWindow.class );
                                        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                        intent.putExtra( "roomId", Define.oldRoomId );
                                        intent.putExtra( "userIds", Define.oldRoomUserId );
                                        intent.putExtra( "userNames", Define.oldRoomUserName );
                                        getActivity().startActivityForResult( intent, Define.AR_ADD_USER );
                                        initSelectBox();
                                        hideKeyboard();
                                        ActionManager.tabs.onTabSelected( Define.TAB_CHAT );
                                        ActionManager.tabs.moveChatTab();
                                        Message m = searchHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                        searchHandler.sendMessage( m );
                                }
                                else
                                {
                                        Message m = searchHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                        searchHandler.sendMessage( m );
                                }
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        private void sendNote()
        {
                try
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
                        // clear
                        initSelectBox();
                        hideKeyboard();
                        ActionManager.tabs.m_Layout.setVisibility( View.VISIBLE );
                        m_BottomTab.setVisibility( View.VISIBLE );
                        Message m = searchHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                        searchHandler.sendMessage( m );
                        Intent i = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.SendNote.class );
                        i.putExtra( "USERID", userIds );
                        i.putExtra( "USERNAME", userNames );
                        startActivity( i );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public static void initSelectBox()
        {
                try
                {
                        selected.clear();
                        selected.notifyDataSetChanged();
                        TableLayout.LayoutParams wParams = new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT, 0 );
                        m_SelectedUserListLayout.setLayoutParams( wParams );
                        android.view.ViewGroup.LayoutParams lParams = m_SelectedUserList.getLayoutParams();
                        lParams.height = 0;
                        m_SelectedUserList.setLayoutParams( lParams );
                        edit.setText( "" );
                        result.clear();
                        result.notifyDataSetChanged();
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public static void closePopup()
        {
                try
                {
                        if ( searchTypePopup != null )
                        {
                                searchTypePopup.dismiss();
                                searchTypePopup = null;
                                for ( int i = 0; i < btnSearchs.length; i++ )
                                        btnSearchs[i] = null;
                        }
                        if ( m_BottomTab != null ) m_BottomTab.setVisibility( View.VISIBLE );
                        hideKeyboard();
                        result.clear();
                        result.notifyDataSetChanged();
                        initSelectBox();
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public static void showKeyboard()
        {
                try
                {
                        InputMethodManager imm = ( InputMethodManager ) searchViewInstance.getActivity().getSystemService( Activity.INPUT_METHOD_SERVICE );
                        imm.showSoftInput( edit, 0 );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public static void hideKeyboard()
        {
                try
                {
                        InputMethodManager imm = ( InputMethodManager ) searchViewInstance.getActivity().getSystemService( Activity.INPUT_METHOD_SERVICE );
                        imm.hideSoftInputFromWindow( edit.getWindowToken(), 0 );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public void openChat()
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
                String oUserIds = nowSelectedUserId + "," + Define.getMyId();
                String userIds = StringUtil.arrange( oUserIds );
                String userNames = nowSelectedUserName + "," + StringUtil.getNamePosition( Define.getMyName() );
                userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                String roomId = "";
                if ( Define.isAddUserMode )
                {
                        try
                        {
                                int count = StringUtil.getChatRoomCount( Define.oldRoomUserName );
                                if ( count > 2 ) // 그룹채팅방에서 사용자 추가
                                {
                                        String resName = getOtherUserName( userNames.split( "," ), Define.oldRoomUserName.split( "," ) );
                                        String resId = getOtherUserId( userIds.split( "," ), Define.oldRoomUserId.split( "," ) );
                                        String[] newName = resName.split( "," );
                                        String[] newId = resId.split( "," );
                                        roomId = Define.oldRoomId;
                                        String result = StringUtil.makeString( newId );
                                        result += "#" + StringUtil.makeString( newName );
                                        String dateTime = StringUtil.getNowDateTime() + "";
                                        resName = resName.substring( 0, resName.lastIndexOf( "," ) );
                                        String msgId = Database.instance( getActivity() ).insertChatContent( Define.getMyId( context ) + "_" + dateTime,
                                                        Define.oldRoomId, Define.getMyId( getActivity() ), Define.getMyName(), Define.getMyNickName(),
                                                        dateTime, resName + getString( R.string.gsInMessage ), userIds, true, true );
                                        sendChatRoomIn( resId, resName, msgId, Define.oldRoomId, Define.oldRoomUserId, Define.oldRoomUserName, "[ROOM_IN]" );
                                        // MainActivity.sendChatRoomIn(resName, msgId, Define.oldRoomId, userIds, userNames, "[ROOM_IN]");
                                        Database.instance( context ).updateChatRoomUsers( Define.oldRoomId, userIds, userNames );
                                        ActionManager.openChat( context, Define.oldRoomId, userIds, userNames );
                                        TalkView.instance().resetData();
                                        Define.oldRoomId = "";
                                        Define.oldRoomUserId = "";
                                        Define.oldRoomUserName = "";
                                        Define.isAddUserMode = false;
                                }
                                else if ( count == 2 ) // 1대1방에서 사용자 추가이거나 그룹방이었던 상태
                                {
                                        boolean isExist = false;
                                        ArrayList<ArrayList<String>> chatArr = Database.instance( context ).selectChatContent( Define.oldRoomId );
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
                                                String resName = getOtherUserName( userNames.split( "," ), Define.oldRoomUserName.split( "," ) );
                                                String resId = getOtherUserId( userIds.split( "," ), Define.oldRoomUserId.split( "," ) );
                                                String[] newName = resName.split( "," );
                                                String[] newId = resId.split( "," );
                                                roomId = Define.oldRoomId;
                                                String result = StringUtil.makeString( newId );
                                                result += "#" + StringUtil.makeString( newName );
                                                String dateTime = StringUtil.getNowDateTime();
                                                resName = resName.substring( 0, resName.lastIndexOf( "," ) );
                                                String msgId = Database.instance( getActivity() ).insertChatContent(
                                                                Define.getMyId( getActivity() ) + "_" + dateTime, Define.oldRoomId,
                                                                Define.getMyId( getActivity() ), Define.getMyName(), Define.getMyNickName(), dateTime,
                                                                resName + getString( R.string.gsInMessage ), userIds, true, true );
                                                sendChatRoomIn( resId, resName, msgId, Define.oldRoomId, Define.oldRoomUserId, Define.oldRoomUserName,
                                                                "[ROOM_IN]" );
                                                // MainActivity.sendChatRoomIn(resName, msgId, Define.oldRoomId, userIds, userNames, "[ROOM_IN]");
                                                Database.instance( getActivity() ).updateChatRoomUsers( Define.oldRoomId, userIds, userNames );
                                                ActionManager.openChat( getActivity(), Define.oldRoomId, userIds, userNames );
                                                TalkView.instance().resetData();
                                                Define.oldRoomId = "";
                                                Define.oldRoomUserId = "";
                                                Define.oldRoomUserName = "";
                                                Define.isAddUserMode = false;
                                        }
                                        else
                                        {
                                                // new Room
                                                roomId = "GROUP_" + StringUtil.getNowDateTime();
                                                ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfo( roomId );
                                                if ( array.size() == 0 ) Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames,
                                                                StringUtil.getNowDateTime(), getString( R.string.newRoom ) );
                                                Define.oldRoomId = "";
                                                Define.oldRoomUserId = "";
                                                Define.oldRoomUserName = "";
                                                Define.isAddUserMode = false;
                                                ActionManager.openChat( getActivity(), roomId, userIds, userNames );
                                        }
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
                else
                // 일반 검색 대화 모드
                {
                        try
                        {
                                if ( selected.getCount() > 1 ) roomId = "GROUP_" + StringUtil.getNowDateTime();
                                else roomId = userIds.replace( ",", "_" );
                                Define.oldRoomId = "";
                                Define.oldRoomUserId = "";
                                Define.oldRoomUserName = "";
                                Define.isAddUserMode = false;
                                ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfo( roomId );
                                if ( array.size() == 0 ) Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames,
                                                StringUtil.getNowDateTime(), getString( R.string.newRoom ) );
                                ActionManager.openChat( getActivity(), roomId, userIds, userNames );
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
                clear();
        }

        public void sendChatRoomIn( String addUserIds, String addUserNames, String msgId, String roomId, String userIds, String userNames, String talk )
        {
                try
                {
                        StringBuffer message = new StringBuffer();
                        message.append( addUserIds );
                        message.append( "\t" );
                        message.append( addUserNames );
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
                        Intent sendIntent = new Intent( Define.MSG_NEW_CHAT );
                        sendIntent.putExtra( "MESSAGE", message.toString() );
                        sendIntent.putExtra( "MESSAGEID", msgId.toString() );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        context.sendBroadcast( sendIntent );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public String getOtherUserId( String[] total, String[] old )
        {
                int totSize = total.length - 1;
                int oldSize = old.length - 1;
                String id = "";
                boolean isUse = false;
                if ( totSize > oldSize )
                {
                        for ( int i = totSize; i >= 0; i-- )
                        {
                                isUse = false;
                                for ( int j = oldSize; j >= 0; j-- )
                                {
                                        if ( total[i].equals( old[j] ) )
                                        {
                                                isUse = true;
                                                break;
                                        }
                                }
                                if ( !isUse ) id += total[i] + ",";
                        }
                }
                return id;
        }

        public String getOtherUserName( String[] total, String[] old )
        {
                int totSize = total.length - 1;
                int oldSize = old.length - 1;
                String names = "";
                boolean isUse = false;
                if ( totSize > oldSize )
                {
                        for ( int i = totSize; i >= 0; i-- )
                        {
                                isUse = false;
                                for ( int j = oldSize; j >= 0; j-- )
                                {
                                        if ( total[i].equals( old[j] ) )
                                        {
                                                isUse = true;
                                                break;
                                        }
                                }
                                if ( !isUse ) names += total[i] + ",";
                        }
                }
                return names;
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
                                sc.send( searchViewInstance.noopStr );
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
                char[] buf = new char[2048];
                int rcv = 0;
                StringBuffer sb = new StringBuffer();
                sc = null;
                InputStreamReader ir = null;
                BufferedReader br = null;
                Timer noopTimer = null;
                try
                {
                        sb.delete( 0, sb.length() );
                        sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                        .getServerPort( Define.mContext ) ) );
                        sc.setSoTimeout( 30000 );
                        ir = new InputStreamReader( sc.getInputStream() );
                        br = new BufferedReader( ir );
                        if ( searchViewInstance.noopStr == null )
                        {
                                searchViewInstance.noopStr = codec.EncryptSEED( "noop" ) + "\f";
                        }
                        noopTimer = new Timer();
                        noopTimer.schedule( new NoopTimer(), 15000, 15000 );
                        Message m = searchHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                        searchHandler.sendMessage( m );
                        short type = 0;
                        for ( int i = searchType.length - 1; i >= 0; i-- )
                        {
                                if ( btnSearchType.getText().equals( searchType[i] ) )
                                {
                                        type = ( short ) i;
                                        break;
                                }
                        }
                        send( "SearchRequest\t" + type + "\t" + edit.getText() );
                        while ( !onDestroy && (rcv = br.read( buf, 0, 2048 )) >= 0 )
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
                                        if ( command.equals( "SearchEnd" ) )
                                        {
                                                Thread.sleep( 500 );
                                                ActionManager.hideProgressDialog();
                                                Message msg = searchHandler.obtainMessage( Define.AM_SEARCH_END, null );
                                                searchHandler.sendMessage( msg );
                                        }
                                        else
                                        {
                                                process( command, param );
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
                        if ( searchViewInstance != null ) searchViewInstance.noopStr = null;
                        if ( noopTimer != null )
                        {
                                noopTimer.cancel();
                                noopTimer = null;
                        }
                }
                try
                {
                        Thread.sleep( 5000 );
                }
                catch ( InterruptedException ie )
                {}
        }

        public void send( String msg ) throws Exception
        {
                msg.replaceAll( "\f", "" );
                sc.send( codec.EncryptSEED( msg ) + '\f' );
        }
        public Handler searchHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_ADD_SEARCH )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        String nick = "";
                                        if ( param.size() > 5 ) nick = param.get( 6 );
                                        result.add( new SearchResultItemData( param.get( 0 ), param.get( 1 ), param.get( 3 ),
                                                        Integer.parseInt( param.get( 2 ) ), nick, false ) );
                                }
                                else if ( msg.what == Define.AM_ICON )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        SearchView.instance().setIcon( param.get( 0 ), Integer.parseInt( param.get( 1 ) ) );
                                }
                                else if ( msg.what == Define.AM_NICK )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        SearchView.instance().setNick( param.get( 0 ), param.get( 1 ) );
                                }
                                else if ( msg.what == Define.AM_CLEAR_ITEM )
                                {
                                        SearchView.instance().clear();
                                }
                                else if ( msg.what == Define.AM_SEARCH_END )
                                {
                                        result.notifyDataSetChanged();
                                }
                                else if ( msg.what == Define.AM_SELECT_CHANGED )
                                {
                                        selected.notifyDataSetChanged();
                                        for ( int i = 0; i < result.getCount(); i++ )
                                        {
                                                result.getItem( i ).checked = false;
                                        }
                                        for ( int i = 0; i < selected.getCount(); i++ )
                                        {
                                                result.setCheck( selected.getItem( i ).id, selected.getItem( i ).checked );
                                        }
                                        for ( int i = 0; i < result.getCount(); i++ )
                                        {
                                                android.util.Log.i( "Check", result.getItem( i ).id + ":" + result.getItem( i ).checked );
                                        }
                                        result.notifyDataSetChanged();
                                        if ( selected.getCount() == 0 )
                                        {
                                                TableLayout.LayoutParams wParams = new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT, 0 );
                                                m_SelectedUserListLayout.setLayoutParams( wParams );
                                                android.view.ViewGroup.LayoutParams lParams = m_SelectedUserList.getLayoutParams();
                                                lParams.height = 0;
                                                m_SelectedUserList.setLayoutParams( lParams );
                                        }
                                        else if ( selected.getCount() == 1 )
                                        {
                                                TableLayout.LayoutParams wParams = new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT,
                                                                Define.DPFromPixel( 80 ) );
                                                m_SelectedUserListLayout.setLayoutParams( wParams );
                                                android.view.ViewGroup.LayoutParams lParams = m_SelectedUserList.getLayoutParams();
                                                lParams.height = Define.DPFromPixel( 50 );
                                                m_SelectedUserList.setLayoutParams( lParams );
                                        }
                                        else if ( selected.getCount() == 2 )
                                        {
                                                TableLayout.LayoutParams wParams = new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT,
                                                                Define.DPFromPixel( 130 ) );
                                                m_SelectedUserListLayout.setLayoutParams( wParams );
                                                android.view.ViewGroup.LayoutParams lParams = m_SelectedUserList.getLayoutParams();
                                                lParams.height = Define.DPFromPixel( 100 );
                                                m_SelectedUserList.setLayoutParams( lParams );
                                        }
                                        else
                                        {
                                                TableLayout.LayoutParams wParams = new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT,
                                                                Define.DPFromPixel( 180 ) );
                                                m_SelectedUserListLayout.setLayoutParams( wParams );
                                                android.view.ViewGroup.LayoutParams lParams = m_SelectedUserList.getLayoutParams();
                                                lParams.height = Define.DPFromPixel( 150 );
                                                m_SelectedUserList.setLayoutParams( lParams );
                                        }
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
                if ( command.equals( "User" ) && param.size() >= 5 && !param.get( 0 ).equals( Define.getMyId( context ) ) )
                {
                        Define.searchMobileOn.put( param.get( 0 ), param.get( 8 ) );
                        Message m = searchHandler.obtainMessage( Define.AM_ADD_SEARCH, param );
                        searchHandler.sendMessage( m );
                }
                else if ( command.equals( "Icon" ) )
                {
                        Message m = searchHandler.obtainMessage( Define.AM_ICON, param );
                        searchHandler.sendMessage( m );
                }
                else if ( command.equals( "Nick" ) )
                {
                        Message m = searchHandler.obtainMessage( Define.AM_NICK, param );
                        searchHandler.sendMessage( m );
                }
        }

        public void clear()
        {
                result.clear();
                result.notifyDataSetChanged();
        }

        public void setIcon( String id, int icon )
        {
                boolean m_bChanged = false;
                for ( int i = 0; i < result.getCount(); i++ )
                {
                        SearchResultItemData data = result.getItem( i );
                        if ( data.id.equals( id ) && data.icon != icon )
                        {
                                data.icon = icon;
                                m_bChanged = true;
                        }
                }
                if ( m_bChanged ) result.notifyDataSetChanged();
        }

        public void setNick( String id, String nick )
        {
                boolean m_bChanged = false;
                for ( int i = 0; i < result.getCount(); i++ )
                {
                        SearchResultItemData data = result.getItem( i );
                        if ( data.id.equals( id ) && data.nickName != null && !data.nickName.equals( nick ) )
                        {
                                data.nickName = nick;
                                m_bChanged = true;
                        }
                }
                if ( m_bChanged ) result.notifyDataSetChanged();
        }

        @Override
        public void onItemClick( AdapterView<?> arg0, View arg1, int position, long arg3 )
        {
                nowSelectedUserId = result.getItem( position ).id;
                nowSelectedUserName = result.getItem( position ).name;
                registerForContextMenu( listView );
                getActivity().openContextMenu( listView );
                unregisterForContextMenu( listView );
        }

        @Override
        public boolean onTouch( View v, MotionEvent event )
        {
                int action = event.getAction();
                int id = v.getId();
                if ( action == MotionEvent.ACTION_DOWN )
                {
                        if ( id == R.id.SearchUserResult )
                        {
                                if ( searchTypePopup != null )
                                {
                                        searchTypePopup.dismiss();
                                        searchTypePopup = null;
                                        for ( int i = 0; i < btnSearchs.length; i++ )
                                                btnSearchs[i] = null;
                                }
                                return false;
                        }
                        else
                        {
                                return v.performClick();
                        }
                }
                return true;
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
