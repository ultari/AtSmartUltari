package kr.co.ultari.atsmart.basic.view;

import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.TabButton;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

@SuppressLint( "InflateParams" )
public class MainViewTabFragment extends Fragment implements OnClickListener {
        private static final String TAG = "/AtSmart/MainViewTabFragment";
        public static int tabHeight;
        public interface TabListener {
                public void onTabClicked( int resourceId );
        }
        private static TabListener uiCallback;
        public TableLayout m_Layout;
        private static TabButton m_btnUser = null;
        private static TabButton m_btnOrg = null;
        private static TabButton m_btnChat = null;
        private static TabButton m_btnNotify = null;
        private static TabButton m_btnSetting = null;
        private static TabButton m_btnCalllog = null;
        private static TabButton m_btnFavorite = null;
        private static TabButton m_btnKeypad = null;
        private static TabButton m_btnContact = null;
        private static TabButton m_btnMessage = null;
        public static TabHorizontalScrollView mHorizentalScrollView = null;

        @SuppressWarnings( "deprecation" )
        @SuppressLint( "NewApi" )
        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                View view = inflater.inflate( R.layout.tab_button, null );
                // lastUserTab = Define.TAB_USER_BUDDY;
                m_btnUser = new TabButton( getActivity().getApplicationContext(), R.drawable.btn_tab_buddy_normal, R.drawable.btn_tab_buddy_pressed, false,getString( R.string.user ) );
                m_btnOrg = new TabButton( getActivity().getApplicationContext(), R.drawable.btn_tab_org_normal, R.drawable.btn_tab_org_pressed,false, getString( R.string.tab_organization ) );
                m_btnChat = new TabButton( getActivity().getApplicationContext(), R.drawable.btn_tab_chat_normal, R.drawable.btn_tab_chat_pressed, false,getString( R.string.chat ) );
                m_btnNotify = new TabButton( getActivity().getApplicationContext(), R.drawable.btn_tab_notice_normal, R.drawable.btn_tab_notice_pressed,false, getString( R.string.tab_notification ) );
                m_btnSetting = new TabButton( getActivity().getApplicationContext(), R.drawable.btn_tab_config_normal,R.drawable.btn_tab_config_pressed, false, getString( R.string.config ) );
                m_btnCalllog = new TabButton( getActivity().getApplicationContext(), R.drawable.btn_tab_recent_normal, R.drawable.btn_tab_recent_pressed, false, getString( R.string.tab_calllog ) );
                m_btnFavorite = new TabButton( getActivity().getApplicationContext(), R.drawable.btn_tab_favorite_normal, R.drawable.btn_tab_favorite_pressed, false, getString( R.string.bookmark ) );
                m_btnKeypad = new TabButton( getActivity().getApplicationContext(), R.drawable.btn_tab_keypad_normal, R.drawable.btn_tab_keypad_pressed, false, getString( R.string.tab_dialer ) );
                m_btnContact = new TabButton( getActivity().getApplicationContext(), R.drawable.btn_tab_contact_normal, R.drawable.btn_tab_contact_pressed, false, getString( R.string.tab_contacts ) );
                m_btnMessage = new TabButton( getActivity().getApplicationContext(), R.drawable.btn_tab_memo_normal, R.drawable.btn_tab_memo_pressed, false, getString( R.string.tab_message ) );
                
                m_btnUser.setOnClickListener( this );
                m_btnOrg.setOnClickListener( this );
                m_btnChat.setOnClickListener( this );
                m_btnNotify.setOnClickListener( this );
                m_btnSetting.setOnClickListener( this );
                m_btnCalllog.setOnClickListener( this );
                m_btnFavorite.setOnClickListener( this );
                m_btnKeypad.setOnClickListener( this );
                m_btnContact.setOnClickListener( this );
                m_btnMessage.setOnClickListener( this );
                
                m_Layout = ( TableLayout ) view.findViewById( R.id.TabTableLayout );
                TableRow tr = new TableRow( getActivity().getApplicationContext() );
                TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );
                tr.setLayoutParams( layoutParams );
                m_Layout.removeAllViews();
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point point = new Point();
                try
                {
                        display.getSize( point );
                }
                catch ( java.lang.NoSuchMethodError ignore )
                {
                        point.x = display.getWidth();
                        point.y = display.getHeight();
                }
                tabHeight = Define.getDpFromPx( getActivity(), Define.MAIN_TAB_HEIGHT );
                TableRow.LayoutParams layoutParam = new TableRow.LayoutParams( point.x / 5, tabHeight );
                Define.tabWidth = point.x;
                Define.tabHeight = point.y;
                mHorizentalScrollView = new TabHorizontalScrollView( getActivity().getApplicationContext() );
                mHorizentalScrollView.addView( tr );
                mHorizentalScrollView.setHorizontalScrollBarEnabled( false );
                m_Layout.addView( mHorizentalScrollView );
                
                m_btnUser.setLayoutParams( layoutParam );
                m_btnOrg.setLayoutParams( layoutParam );
                m_btnChat.setLayoutParams( layoutParam );
                m_btnNotify.setLayoutParams( layoutParam );
                m_btnSetting.setLayoutParams( layoutParam );
                m_btnCalllog.setLayoutParams( layoutParam );
                m_btnFavorite.setLayoutParams( layoutParam );
                m_btnKeypad.setLayoutParams( layoutParam );
                m_btnContact.setLayoutParams( layoutParam );
                m_btnMessage.setLayoutParams( layoutParam );
                // if(Define.usePresenceTab)
                // m_btnPresence.setLayoutParams( layoutParam );
                
                //2016-03-31
                m_btnUser.setBackgroundColor( Color.TRANSPARENT );
                m_btnOrg.setBackgroundColor( Color.TRANSPARENT );
                m_btnChat.setBackgroundColor( Color.TRANSPARENT );
                m_btnNotify.setBackgroundColor( Color.TRANSPARENT );
                m_btnSetting.setBackgroundColor( Color.TRANSPARENT );
                m_btnCalllog.setBackgroundColor( Color.TRANSPARENT );
                m_btnFavorite.setBackgroundColor( Color.TRANSPARENT );
                m_btnKeypad.setBackgroundColor( Color.TRANSPARENT );
                m_btnContact.setBackgroundColor( Color.TRANSPARENT );
                m_btnMessage.setBackgroundColor( Color.TRANSPARENT );
                //
                
                tr.addView( m_btnKeypad );
                tr.addView( m_btnContact );
                tr.addView( m_btnUser );
                tr.addView( m_btnOrg );
                tr.addView( m_btnChat );
                tr.addView( m_btnFavorite );
                tr.addView( m_btnCalllog );
                tr.addView( m_btnMessage );
                tr.addView( m_btnNotify );
                tr.addView( m_btnSetting );
                // if(Define.usePresenceTab)
                // tr.addView( m_btnPresence );
                ActionManager.talkTabButton = m_btnChat;
                ActionManager.notifyTabButton = m_btnNotify;
                ActionManager.messageTabButton = m_btnMessage;
                ActionManager.tabs = this;
                tr = null;
                return view;
        }

        @Override
        public void onDestroy()
        {
                if ( m_btnUser != null ) m_btnUser.onDestroy();
                if ( m_btnChat != null ) m_btnChat.onDestroy();
                if ( m_btnNotify != null ) m_btnNotify.onDestroy();
                if ( m_btnOrg != null ) m_btnOrg.onDestroy();
                if ( m_btnSetting != null ) m_btnSetting.onDestroy();
                if ( m_btnCalllog != null ) m_btnCalllog.onDestroy();
                if ( m_btnFavorite != null ) m_btnFavorite.onDestroy();
                if ( m_btnContact != null ) m_btnContact.onDestroy();
                if ( m_btnKeypad != null ) m_btnKeypad.onDestroy();
                if ( m_btnMessage != null ) m_btnMessage.onDestroy();
                /*
                 * if(Define.usePresenceTab)
                 * {
                 * if( m_btnPresence != null)
                 * m_btnPresence.onDestroy();
                 * }
                 */
                super.onDestroyView();
        }

        public void initTab()
        {
                m_btnUser.updateTab();
                m_btnChat.updateTab();
                m_btnNotify.updateTab();
                m_btnOrg.updateTab();
                m_btnSetting.updateTab();
                m_btnCalllog.updateTab();
                m_btnFavorite.updateTab();
                m_btnContact.updateTab();
                m_btnKeypad.updateTab();
                m_btnMessage.updateTab();
                // if(Define.usePresenceTab)
                // m_btnPresence.updateTab();
        }

        public static void setSubUserTab( int id )
        {
                // lastUserTab = id;
        }

        public static void setSubMoreTab( int id )
        {
                // lastMoreTab = id;
        }
        public Handler handler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_TAB_REFRESH )
                                {
                                        /*
                                         * if(Define.isBuddyAddMode)
                                         * mHorizentalScrollView.setVisibility( View.GONE );
                                         * else
                                         * mHorizentalScrollView.setVisibility( View.VISIBLE );
                                         */
                                        if ( Define.isBuddyAddMode ) MainActivity.Instance().layout_fragmentTab.setVisibility( View.GONE );
                                        else MainActivity.Instance().layout_fragmentTab.setVisibility( View.VISIBLE );
                                }
                                else if ( msg.what == Define.AM_TAB_SHOW )
                                {
                                        MainActivity.Instance().layout_fragmentTab.setVisibility( View.VISIBLE );
                                        // mHorizentalScrollView.setVisibility( View.VISIBLE );
                                }
                                else if ( msg.what == Define.AM_TAB_HIDE )
                                {
                                        MainActivity.Instance().layout_fragmentTab.setVisibility( View.GONE );
                                        // mHorizentalScrollView.setVisibility( View.GONE );
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };

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
                        e.printStackTrace();
                }
        }

        public void moveBackgroundToNotifyTab()
        {
                mHorizentalScrollView.scrollTo( ( int ) Define.tabWidth, 0 );
        }

        public void onTabSelected( int tab )
        {
                Log.d( "TabSelected", "Tab1 : " + tab );
                if ( tab == Define.TAB_USER_BUDDY )
                {
                        m_btnUser.setSelected( true );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnKeypad.setSelected( false );
                }
                else if ( tab == Define.TAB_ORGANIZATION )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( true );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnMessage.setSelected( false );
                        if ( !Define.isMovingForward ) mHorizentalScrollView.scrollTo( 0, 0 );
                }
                else if ( tab == Define.TAB_CHAT )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( true );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnMessage.setSelected( false );
                        mHorizentalScrollView.scrollTo( 0, 0 );
                        TalkView.instance().resetData();
                }
                else if ( tab == Define.TAB_NOTIFY )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( true );
                        m_btnSetting.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnMessage.setSelected( false );
                        NotifyView.instance().resetData();
                }
                else if ( tab == Define.TAB_SETTING )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( true );
                        m_btnCalllog.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnMessage.setSelected( false );
                        mHorizentalScrollView.scrollTo( ( int ) Define.tabWidth, 0 );
                }
                else if ( tab == Define.TAB_CALL_LOG )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnCalllog.setSelected( true );
                        m_btnFavorite.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnMessage.setSelected( false );
                        CallView.instance().callLog();
                }
                else if ( tab == Define.TAB_BOOKMARK )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnFavorite.setSelected( true );
                        m_btnContact.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnMessage.setSelected( false );
                        mHorizentalScrollView.scrollTo( ( int ) Define.tabWidth, 0 );
                        FavoriteView.instance().resetData();
                }
                else if ( tab == Define.TAB_KEYPAD )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnKeypad.setSelected( true );
                        m_btnMessage.setSelected( false );
                }
                else if ( tab == Define.TAB_CONTACT )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnContact.setSelected( true );
                        m_btnKeypad.setSelected( false );
                        m_btnMessage.setSelected( false );
                        ContactView.instance().displayListBasic();
                }
                else if ( tab == Define.TAB_MESSAGE )
                {
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnMessage.setSelected( true );
                }
                Message m = MainActivity.mainHandler.obtainMessage( Define.AM_TAB_REFRESH, null );
                MainActivity.mainHandler.sendMessage( m );
                m = KeypadView.instance().handler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                KeypadView.instance().handler.sendMessage( m );
                /*
                 * if(Define.isBuddyAddMode)
                 * {
                 * Define.isBuddyAddMode = false;
                 * BuddyView.updateBuddylist();
                 * MainActivity.mainHandler.sendEmptyMessageDelayed( Define.AM_MAIN_GROUPTAB_REFRESH, 100);
                 * ActionManager.tabs.handler.sendEmptyMessageDelayed( Define.AM_TAB_REFRESH, 100);
                 * }
                 */
        }

        @Override
        public void onClick( View v )
        {
                if ( v == m_btnUser )
                {
                        m_btnUser.setSelected( true );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnContact.setSelected( false );
                        BuddyView.instance().updateFontSize();
                        OrganizationView.instance().updateFontSize();
                        /*
                         * if ( lastUserTab == Define.TAB_USER_BUDDY )
                         * {
                         * uiCallback.onTabClicked( Define.TAB_USER_BUDDY );
                         * setSubUserTab( Define.TAB_USER_BUDDY );
                         * }
                         * else if ( lastUserTab == Define.TAB_ORGANIZATION )
                         * {
                         * uiCallback.onTabClicked( Define.TAB_ORGANIZATION );
                         * setSubUserTab( Define.TAB_ORGANIZATION );
                         * }
                         */
                        /*
                         * else if ( lastUserTab == Define.TAB_USER_SEARCH )
                         * {
                         * uiCallback.onTabClicked( Define.TAB_USER_SEARCH );
                         * setSubUserTab( Define.TAB_USER_SEARCH );
                         * }
                         */
                        // SearchView.closePopup();
                        uiCallback.onTabClicked( Define.TAB_USER_BUDDY );
                        setSubUserTab( Define.TAB_USER_BUDDY );
                }
                else if ( v == m_btnOrg )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( true );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnMessage.setSelected( false );
                        uiCallback.onTabClicked( Define.TAB_ORGANIZATION );
                        setSubUserTab( Define.TAB_ORGANIZATION );
                }
                else if ( v == m_btnChat )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( true );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnMessage.setSelected( false );
                        // 2015-05-11
                        // m_btnPresence.setSelected( false );
                        uiCallback.onTabClicked( Define.TAB_CHAT );
                        // SearchView.closePopup();
                }
                else if ( v == m_btnNotify )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( true );
                        m_btnSetting.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnMessage.setSelected( false );
                        // 2015-05-11
                        // m_btnPresence.setSelected( false );
                        uiCallback.onTabClicked( Define.TAB_NOTIFY );
                        // SearchView.closePopup();
                }
                else if ( v == m_btnSetting )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( true );
                        m_btnFavorite.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnMessage.setSelected( false );
                        /*
                         * if ( lastMoreTab == Define.TAB_MORE_CONFIG )
                         * {
                         * uiCallback.onTabClicked( Define.TAB_MORE_CONFIG );
                         * setSubMoreTab( Define.TAB_MORE_CONFIG );
                         * }
                         * else if ( lastMoreTab == Define.TAB_MORE_ACCOUNT )
                         * {
                         * uiCallback.onTabClicked( Define.TAB_MORE_ACCOUNT );
                         * setSubMoreTab( Define.TAB_MORE_ACCOUNT );
                         * }
                         */
                        // 2015-05-11
                        // m_btnPresence.setSelected( false );
                        uiCallback.onTabClicked( Define.TAB_SETTING );
                        setSubMoreTab( Define.TAB_SETTING );
                        // SearchView.closePopup();
                }
                else if ( v == m_btnCalllog )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnCalllog.setSelected( true );
                        m_btnKeypad.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnMessage.setSelected( false );
                        // 2015-05-11
                        // m_btnPresence.setSelected( false );
                        uiCallback.onTabClicked( Define.TAB_CALL_LOG );
                        setSubMoreTab( Define.TAB_CALL_LOG );
                        // SearchView.closePopup();
                }
                else if ( v == m_btnFavorite )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnFavorite.setSelected( true );
                        m_btnCalllog.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnMessage.setSelected( false );
                        // 2015-05-11
                        // m_btnPresence.setSelected( false );
                        uiCallback.onTabClicked( Define.TAB_BOOKMARK );
                        setSubMoreTab( Define.TAB_BOOKMARK );
                        // SearchView.closePopup();
                }
                else if ( v == m_btnContact )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnContact.setSelected( true );
                        m_btnMessage.setSelected( false );
                        // 2015-05-11
                        // m_btnPresence.setSelected( false );
                        uiCallback.onTabClicked( Define.TAB_CONTACT );
                        setSubMoreTab( Define.TAB_CONTACT );
                }
                else if ( v == m_btnKeypad )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnKeypad.setSelected( true );
                        m_btnContact.setSelected( false );
                        m_btnMessage.setSelected( false );
                        // 2015-05-11
                        // m_btnPresence.setSelected( false );
                        uiCallback.onTabClicked( Define.TAB_KEYPAD );
                        setSubMoreTab( Define.TAB_KEYPAD );
                }
                else if ( v == m_btnMessage )
                {
                        m_btnUser.setSelected( false );
                        m_btnOrg.setSelected( false );
                        m_btnChat.setSelected( false );
                        m_btnNotify.setSelected( false );
                        m_btnSetting.setSelected( false );
                        m_btnFavorite.setSelected( false );
                        m_btnCalllog.setSelected( false );
                        m_btnKeypad.setSelected( false );
                        m_btnContact.setSelected( false );
                        m_btnMessage.setSelected( true );
                        // 2015-05-11
                        // m_btnPresence.setSelected( false );
                        uiCallback.onTabClicked( Define.TAB_MESSAGE );
                        setSubMoreTab( Define.TAB_MESSAGE );
                }
                // 2015-05-11
                /*
                 * else if( v == m_btnPresence )
                 * {
                 * m_btnOrg.setSelected( false );
                 * m_btnChat.setSelected( false );
                 * m_btnNotify.setSelected( false );
                 * m_btnSetting.setSelected( false );
                 * m_btnFavorite.setSelected( false );
                 * m_btnCalllog.setSelected( false );
                 * m_btnKeypad.setSelected( false );
                 * m_btnContact.setSelected( false );
                 * m_btnPresence.setSelected( true );
                 * uiCallback.onTabClicked( Define.TAB_PRESENCE );
                 * setSubMoreTab( Define.TAB_PRESENCE );
                 * }
                 */
        }

        public void moveChatTab()
        {
                m_btnUser.setSelected( false );
                m_btnOrg.setSelected( false );
                m_btnChat.setSelected( true );
                m_btnNotify.setSelected( false );
                m_btnSetting.setSelected( false );
                m_btnFavorite.setSelected( false );
                m_btnCalllog.setSelected( false );
                m_btnKeypad.setSelected( false );
                m_btnContact.setSelected( false );
                m_btnMessage.setSelected( false );
                // 2015-05-11
                // if(Define.usePresenceTab)
                // m_btnPresence.setSelected( false );
                uiCallback.onTabClicked( Define.TAB_CHAT );
                SearchView.closePopup();
        }

        public void moveKeypad()
        {
                m_btnOrg.setSelected( false );
                m_btnChat.setSelected( false );
                m_btnNotify.setSelected( false );
                m_btnSetting.setSelected( false );
                m_btnFavorite.setSelected( false );
                m_btnCalllog.setSelected( false );
                m_btnKeypad.setSelected( true );
                m_btnContact.setSelected( false );
                m_btnMessage.setSelected( false );
                // if(Define.usePresenceTab)
                // m_btnPresence.setSelected( false );
                uiCallback.onTabClicked( Define.TAB_KEYPAD );
                setSubMoreTab( Define.TAB_KEYPAD );
                mHorizentalScrollView.scrollTo( 0, 0 ); //2016-12-13
        }

        public void moveFavorite()
        {
                m_btnOrg.setSelected( false );
                m_btnChat.setSelected( false );
                m_btnNotify.setSelected( false );
                m_btnSetting.setSelected( false );
                m_btnFavorite.setSelected( true );
                m_btnCalllog.setSelected( false );
                m_btnKeypad.setSelected( false );
                m_btnContact.setSelected( false );
                m_btnMessage.setSelected( false );
                // if(Define.usePresenceTab)
                // m_btnPresence.setSelected( false );
                uiCallback.onTabClicked( Define.TAB_BOOKMARK );
                setSubMoreTab( Define.TAB_BOOKMARK );
        }

        @SuppressWarnings( "deprecation" )
        @SuppressLint( "NewApi" )
        @Override
        public void onConfigurationChanged( Configuration newConfig )
        {
                super.onConfigurationChanged( newConfig );
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point point = new Point();
                try
                {
                        display.getSize( point );
                }
                catch ( java.lang.NoSuchMethodError ignore )
                {
                        point.x = display.getWidth();
                        point.y = display.getHeight();
                }
                TableRow.LayoutParams layoutParam = new TableRow.LayoutParams( point.x / 5, tabHeight );
                Define.tabWidth = point.x;
                Define.tabHeight = point.y;
                m_btnUser.setLayoutParams( layoutParam );
                m_btnOrg.setLayoutParams( layoutParam );
                m_btnChat.setLayoutParams( layoutParam );
                m_btnNotify.setLayoutParams( layoutParam );
                m_btnSetting.setLayoutParams( layoutParam );
                m_btnCalllog.setLayoutParams( layoutParam );
                m_btnFavorite.setLayoutParams( layoutParam );
                m_btnKeypad.setLayoutParams( layoutParam );
                m_btnContact.setLayoutParams( layoutParam );
                m_btnMessage.setLayoutParams( layoutParam );
                // 2015-05-11
                // if(Define.usePresenceTab)
                // m_btnPresence.setLayoutParams( layoutParam );
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
