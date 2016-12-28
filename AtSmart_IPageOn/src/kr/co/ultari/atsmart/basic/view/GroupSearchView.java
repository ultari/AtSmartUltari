package kr.co.ultari.atsmart.basic.view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.control.SelectedUserView;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.subdata.SearchResultItemData;
import kr.co.ultari.atsmart.basic.subview.GroupSearchResultItem;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import kr.co.ultari.atsmart.basic.control.SelectedUserView.OnRecalcHeightListener;
import kr.co.ultari.atsmart.basic.control.UserButton.OnDeleteUserListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class GroupSearchView extends MessengerActivity implements OnClickListener, OnItemClickListener, OnTouchListener, OnDeleteUserListener,
                OnRecalcHeightListener {
        private final String TAG = "/AtSmart/GroupSearchView";
        public static final short TYPE_ORGANIZATION = 0x01;
        public static final short TYPE_TALK = 0x02;
        public static final short TYPE_CHAT = 0x03;
        public static final short TYPE_MESSAGE = 0x04;
        private short searchViewType = 0x00;
        private Button btnSearchType, btnClose, chatButton;
        public LayoutInflater inflater;
        private PopupWindow searchTypePopup = null;
        private Button[] btnSearchs = new Button[7]; // Button[6]; //2016-12-16 HHJ
        private final String[] searchType = new String[7]; // String[6]; //2016-12-16 HHJ
        private Context context;
        private EditText edit = null;
        private AmCodec codec;
        private String noopStr = null;
        private boolean onDestroy = false;
        public Bitmap statusOnlineBitmap, statusAwayBitmap, statusBusyBitmap, statusPhoneBitmap, statusMeetingBitmap, statusOfflineBitmap, checkedImage,
                        unCheckedImage, mobileOn, mobileOff, ucOn, ucOff, ucRinging = null;
        private ListView listView = null;
        private GroupSearchResultItem result = null;
        public SelectedUserView m_SelectedUserList = null;
        private LinearLayout m_SelectedUserListLayout;
        private UltariSSLSocket sc = null;
        private Timer timer;
        private String nowSelectedUserId, nowSelectedUserName = null;
        private int phoneNumberIndex;
        private String noop = null;
        private TextView tvTitle;
        private Button btn_search_delete, btn_user_search;
        public static GroupSearchView groupSearchView = null;
        private boolean isActionResult = false;
        private ProgressDialog m_WaitForSearchProgressDialog = null;

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                groupSearchView = this;
                // requestWindowFeature(Window.FEATURE_NO_TITLE);
                // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                // WindowManager.LayoutParams.FLAG_FULLSCREEN);
                inflater = getLayoutInflater();
                context = getApplicationContext();
                setContentView( R.layout.activity_search_view );
                
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE ); //2016-12-13
                
                try
                {
                        MainActivity.search = this;
                        tvTitle = ( TextView ) findViewById( R.id.searchview_title );
                        tvTitle.setTypeface( Define.tfMedium );
                        btnClose = ( Button ) findViewById( R.id.searchview_exit );
                        btnClose.setOnClickListener( this );
                        btnClose.setTypeface( Define.tfRegular );
                        chatButton = ( Button ) findViewById( R.id.searchview_chat );
                        chatButton.setTypeface( Define.tfRegular );
                        chatButton.setOnClickListener( this );
                        chatButton.setVisibility( View.INVISIBLE );
                        btnSearchType = ( Button ) findViewById( R.id.searchview_searchTypes );
                        btnSearchType.setOnClickListener( this );
                        btnSearchType.setTypeface( Define.tfRegular );
                        statusOnlineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        statusAwayBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        statusBusyBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        statusPhoneBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        statusMeetingBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        statusOfflineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_off );
                        btn_search_delete = ( Button ) findViewById( R.id.searchview_value_delete );
                        btn_search_delete.setOnClickListener( this );
                        btn_user_search = ( Button ) findViewById( R.id.searchview_btn_search );
                        btn_user_search.setOnClickListener( this );
                        searchType[0] = getString( R.string.search_type_name );
                        searchType[1] = getString( R.string.search_type_part );
                        searchType[2] = getString( R.string.search_type_id );
                        searchType[3] = getString( R.string.search_type_position );
                        searchType[4] = getString( R.string.search_type_phone );
                        searchType[5] = getString( R.string.search_type_mobile );
                        searchType[6] = getString( R.string.search_type_job ); //2016-12-16
                        edit = ( EditText ) findViewById( R.id.searchview_search_input );
                        edit.setTypeface( Define.tfRegular );
                        edit.setMovementMethod( null );
                        edit.setSelection( edit.length() );
                        edit.setOnTouchListener( mTouchEvent );
                        edit.requestFocus();
                        edit.setFocusable( true );

                        //edit.setBackgroundResource( R.drawable.img_search_filter_pressed );
                        edit.setOnEditorActionListener( new TextView.OnEditorActionListener() {
                                @Override
                                public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
                                {
                                        if ( actionId == EditorInfo.IME_ACTION_SEARCH )
                                        {
                                                new Search( edit.getText().toString() );
                                                hideKeyboard();
                                        }
                                        return true;
                                }
                        } );
                        codec = new AmCodec();
                        checkedImage = BitmapFactory.decodeResource( getResources(), R.drawable.btn_blackbg_checked );
                        unCheckedImage = BitmapFactory.decodeResource( getResources(), R.drawable.btn_blackbg_uncheck );
                        mobileOn = BitmapFactory.decodeResource( getResources(), R.drawable.icon_mobile_on );
                        mobileOff = BitmapFactory.decodeResource( getResources(), R.drawable.icon_mobile_off );
                        ucOn = BitmapFactory.decodeResource( getResources(), R.drawable.icon_uc_on );
                        ucOff = BitmapFactory.decodeResource( getResources(), R.drawable.icon_uc_off );
                        ucRinging = BitmapFactory.decodeResource( getResources(), R.drawable.icon_uc_ring );
                        Bundle bundle = getIntent().getExtras();
                        if ( bundle != null && bundle.getString( "type" ) != null )
                        {
                                if ( bundle.getString( "type" ).equals( "message" ) )
                                {
                                        chatButton.setText( "확인" );
                                        chatButton.setVisibility( View.VISIBLE );
                                        isActionResult = true;
                                        searchViewType = TYPE_MESSAGE;
                                }
                                else if ( bundle.getString( "type" ).equals( "talk" ) )
                                {
                                        chatButton.setText( "대화" );
                                        chatButton.setVisibility( View.VISIBLE );
                                        isActionResult = false;
                                        searchViewType = TYPE_TALK;
                                }
                                else if ( bundle.getString( "type" ).equals( "organization" ) )
                                {
                                        chatButton.setVisibility( View.INVISIBLE );
                                        isActionResult = false;
                                        searchViewType = TYPE_ORGANIZATION;
                                }
                                else if ( bundle.getString( "type" ).equals( "chat" ) )
                                {
                                        chatButton.setText( "완료" );
                                        chatButton.setVisibility( View.VISIBLE );
                                        isActionResult = true;
                                        searchViewType = TYPE_CHAT;
                                }
                        }
                        result = new GroupSearchResultItem( getApplicationContext(), GroupSearchView.this, searchViewType );
                        listView = ( ListView ) findViewById( R.id.searchview_SearchUserResult );
                        listView.setAdapter( result );
                        listView.setClickable( true );
                        listView.setOnItemClickListener( this );
                        m_SelectedUserListLayout = ( LinearLayout ) findViewById( R.id.searchview_SelectedUserLayout );
                        m_SelectedUserList = new SelectedUserView( this );
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, 0 );
                        params.setMargins( 0, 0, 0, 0 );
                        m_SelectedUserList.setLayoutParams( params );
                        m_SelectedUserListLayout.addView( m_SelectedUserList );
                        m_SelectedUserList.setOnDeleteUserListener( this );
                        m_SelectedUserList.setOnRecalcHeightListener( this );
                        // if (!Define.isAddUserMode)
                        // chatButton.setVisibility(View.INVISIBLE);
                        noop = codec.EncryptSEED( "noop" ) + "\f";
                        if ( Define.isAddUserMode || isActionResult )
                        {
                                String userIds = bundle.getString( "userIds" );
                                String userNames = bundle.getString( "userNames" );
                                StringTokenizer st1 = new StringTokenizer( userIds, "," );
                                StringTokenizer st2 = new StringTokenizer( userNames, "," );
                                while ( st1.hasMoreElements() && st2.hasMoreElements() )
                                {
                                        String id = st1.nextToken();
                                        String name = st2.nextToken();
                                        if ( id.equals( Define.getMyId() ) )
                                        {
                                                continue;
                                        }
                                        m_SelectedUserList.addUser( id, StringUtil.getNamePosition( name ) );
                                        if ( searchViewType == TYPE_CHAT )
                                        {
                                                m_SelectedUserList.setEditable( id, false );
                                        }
                                }
                                Message m = searchHandler.obtainMessage( Define.AM_SELECT_CHANGED );
                                searchHandler.sendMessage( m );
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }
        private OnTouchListener mTouchEvent = new OnTouchListener() {
                public boolean onTouch( View v, MotionEvent event )
                {
                        EditText b = ( EditText ) v;
                        int action = event.getAction();
                        if ( action == MotionEvent.ACTION_DOWN )
                        {
                                b.setMovementMethod( new ScrollingMovementMethod() );
                                b.requestFocus();
                                edit.setFocusable( true );
                                edit.setSelection( edit.length() );
                                //b.setBackgroundResource( R.drawable.img_search_filter_pressed );
                        }
                        else if ( action == MotionEvent.ACTION_UP )
                        {}
                        return false;
                }
        };

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
                                        finish();
                                        return true;
                                }
                        }
                }
                return super.onKeyDown( keyCode, event );
        }

        /*
         * class EditMessageOnKeyListener implements OnKeyListener{
         * @Override public boolean onKey( View v, int keyCode, KeyEvent event )
         * { if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() ==
         * KeyEvent.ACTION_UP) //if(keyCode == KeyEvent.KEYCODE_ENTER) { Log.e(
         * "GroupSearchView", "ENTER click!" ); searchStarts(); hideKeyboard();
         * return true; }
         * return false; } }
         */
        public void searchStarts()
        {
                try
                {
                        if ( !Define.getMyId( getApplicationContext() ).equals( "" ) && !edit.getText().toString().equals( "" ) )
                        {
                                // ActionManager.showProcessingDialog(getApplicationContext(),
                                // getString(R.string.search),
                                // getString(R.string.searchList));
                                // new Search(edit.getText().toString());
                        }
                        if ( searchTypePopup != null )
                        {
                                searchTypePopup.dismiss();
                                searchTypePopup = null;
                                for ( int i = 0; i < btnSearchs.length; i++ )
                                        btnSearchs[i] = null;
                        }
                        new Search( edit.getText().toString() );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
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
                                if ( nowSelectedUserId != null && nowSelectedUserName != null ) ActionManager.popupUserInfo( getApplicationContext(),
                                                nowSelectedUserId, nowSelectedUserName, Define.getPartNameByUserId( nowSelectedUserName ), "" );
                                nowSelectedUserId = null;
                                nowSelectedUserName = null;
                                // 2015-05-11
                                // finish();
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
                                        ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfoByIds( userIds );
                                        if ( array.size() == 0 )
                                        {
                                                Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames, StringUtil.getNowDateTime(),
                                                                getString( R.string.newRoom ) );
                                        }
                                        else
                                        {
                                                roomId = array.get( 0 ).get( 0 );
                                        }
                                        ActionManager.openChat( getApplicationContext(), roomId, userIds, userNames );
                                }
                                nowSelectedUserId = null;
                                nowSelectedUserName = null;
                        }
                        /*
                         * else if( item.getItemId() == Define.MENU_ID_NOTE ) {
                         * if(nowSelectedUserId != null && nowSelectedUserName
                         * != null) { String[] parse =
                         * nowSelectedUserName.split( "#" );
                         * Intent i = new Intent(getApplicationContext(),
                         * kr.co.ultari.atsmart.basic.subview.SendNote.class);
                         * i.putExtra( "USERID", nowSelectedUserId );
                         * i.putExtra( "USERNAME", parse[0] + " " + parse[1] );
                         * startActivity(i);
                         * nowSelectedUserId = null; nowSelectedUserName = null;
                         * } }
                         */
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
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
                // if(Define.useSendNoteMsg)
                // menu.add(0, Define.MENU_ID_NOTE, Menu.NONE, getString(
                // R.string.send ));
        }

        @Override
        public void onDestroy()
        {
                try
                {
                        onDestroy = true;
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
                        // 2015-04-30
                        if ( groupSearchView != null ) groupSearchView = null;
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                super.onDestroy();
        }

        private void updateSearchPopup( String type )
        {
                for ( int i = btnSearchs.length - 1; i >= 0; i-- )
                {
                        if ( type.equals( btnSearchs[i].getText() ) )
                        {
                                btnSearchs[i].setTextColor( Color.WHITE );
                                btnSearchs[i].setBackgroundColor( 0xFF8d9dd8 );
                        }
                        else
                        {
                                btnSearchs[i].setTextColor( Color.rgb( 39, 39, 39 ) );
                                btnSearchs[i].setBackgroundColor( 0xFFFFFFFF );
                        }
                }
        }

        private void updateSearchName( String name )
        {
                try
                {
                        btnSearchType.setTypeface( Define.tfRegular );
                        btnSearchType.setText( name );
                        searchTypePopup.dismiss();
                        searchTypePopup = null;
                        for ( int i = 0; i < btnSearchs.length; i++ )
                                btnSearchs[i] = null;
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        @SuppressLint( "InflateParams" )
        public void onClick( View view )
        {
                if ( searchTypePopup != null && view != btnSearchs[0] && view != btnSearchs[1] && view != btnSearchs[2] && view != btnSearchs[3]
                                && view != btnSearchs[4] && view != btnSearchs[5] && view != btnSearchs[6] ) //2016-12-16
                //if ( searchTypePopup != null && view != btnSearchs[0] && view != btnSearchs[1] && view != btnSearchs[2] && view != btnSearchs[3]
                //    && view != btnSearchs[4] && view != btnSearchs[5] )
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
                                e.printStackTrace();
                        }
                }
                try
                {
                        if ( view == btnSearchType )
                        {
                                View popupView = inflater.inflate( R.layout.search_type_popup, null );
                                // searchTypePopup = new PopupWindow(popupView,
                                // LayoutParams.WRAP_CONTENT,
                                // LayoutParams.WRAP_CONTENT);
                                searchTypePopup = new PopupWindow( popupView, Define.getDpFromPx( context, 300 ), LayoutParams.WRAP_CONTENT );
                                btnSearchs[0] = ( Button ) popupView.findViewById( R.id.searchTypeName );
                                btnSearchs[1] = ( Button ) popupView.findViewById( R.id.searchTypePart );
                                btnSearchs[2] = ( Button ) popupView.findViewById( R.id.searchTypeId );
                                btnSearchs[3] = ( Button ) popupView.findViewById( R.id.searchTypePosition );
                                btnSearchs[4] = ( Button ) popupView.findViewById( R.id.searchTypePhone );
                                btnSearchs[5] = ( Button ) popupView.findViewById( R.id.searchTypeMobile );
                                btnSearchs[6] = ( Button ) popupView.findViewById( R.id.searchTypeJob ); //2016-12-16
                                btnSearchs[0].setTypeface( Define.tfRegular );
                                btnSearchs[1].setTypeface( Define.tfRegular );
                                btnSearchs[2].setTypeface( Define.tfRegular );
                                btnSearchs[3].setTypeface( Define.tfRegular );
                                btnSearchs[4].setTypeface( Define.tfRegular );
                                btnSearchs[5].setTypeface( Define.tfRegular );
                                btnSearchs[6].setTypeface( Define.tfRegular ); //2016-12-16
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
                        else if ( btnSearchs[6] != null && view == btnSearchs[6] ) //2016-12-16
                        {
                                updateSearchName( btnSearchs[6].getText().toString() );
                        }//
                        else if ( view == chatButton )
                        {
                                // 2015-05-01
                                String nowSelectedUserId = "";
                                String nowSelectedUserName = "";
                                ArrayList<SelectedUserView.UserData> users = m_SelectedUserList.getUsers();
                                for ( int i = 0; i < users.size(); i++ )
                                {
                                        SelectedUserView.UserData data = users.get( i );
                                        if ( !nowSelectedUserId.equals( "" ) ) nowSelectedUserId += ",";
                                        nowSelectedUserId += data.userId;
                                        if ( !nowSelectedUserName.equals( "" ) ) nowSelectedUserName += ",";
                                        nowSelectedUserName += StringUtil.getNamePosition( data.userName );
                                }
                                
                                //Log.d( TAG, "ChatButton : " + nowSelectedUserId );
                                if ( searchViewType == TYPE_MESSAGE )
                                {
                                        //Log.d( TAG, "isActionResult : " + nowSelectedUserName );
                                        Bundle b = new Bundle();
                                        b.putString( "userIds", nowSelectedUserId );
                                        b.putString( "userNames", nowSelectedUserName );
                                        Intent intent = new Intent();
                                        intent.putExtras( b );
                                        setResult( Activity.RESULT_OK, intent );
                                        finish();
                                }
                                else
                                {
                                        openChat();
                                        if ( Define.isAddUserMode ) Define.isAddUserMode = false;
                                        initSelectBox();
                                        hideKeyboard();
                                        Message m = searchHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                        searchHandler.sendMessage( m );
                                }
                        }
                        /*
                         * else if( view == noteButton ) { sendNote(); }
                         */
                        else if ( view == btnClose )
                        {
                                if ( Define.isAddUserMode )
                                {
                                        Define.isAddUserMode = false;
                                        initSelectBox();
                                        hideKeyboard();
                                        Message m = searchHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                        searchHandler.sendMessage( m );
                                        Intent intent = new Intent( getApplicationContext(), kr.co.ultari.atsmart.basic.view.ChatWindow.class );
                                        intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                        intent.putExtra( "roomId", Define.oldRoomId );
                                        intent.putExtra( "userIds", Define.oldRoomUserId );
                                        intent.putExtra( "userNames", Define.oldRoomUserName );
                                        startActivity( intent );
                                }
                                else
                                {
                                        Message m = searchHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                        searchHandler.sendMessage( m );
                                }
                                Message m = searchHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                searchHandler.sendMessage( m );
                                initSelectBox();
                                hideKeyboard();
                                finish();
                        }
                        else if ( view == btn_search_delete )
                        {
                                edit.setText( "" );
                                //edit.setBackgroundResource( R.drawable.img_search_filter_normal );
                                btn_search_delete.setVisibility( View.GONE );
                        }
                        else if ( view == btn_user_search )
                        {
                                searchStarts();
                                hideKeyboard();
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        /*
         * private void sendNote() { try { String nowSelectedUserId = ""; String
         * nowSelectedUserName = "";
         * for ( int i = 0 ; i < selected.getCount() ; i++ ) {
         * SearchResultItemData data = selected.getItem(i);
         * if ( !nowSelectedUserId.equals("") ) nowSelectedUserId += ",";
         * nowSelectedUserId += data.id;
         * if ( !nowSelectedUserName.equals("") ) nowSelectedUserName += ",";
         * nowSelectedUserName += StringUtil.getNamePosition(data.name); }
         * String oUserIds = nowSelectedUserId; String userIds =
         * StringUtil.arrange(oUserIds); String userNames =
         * StringUtil.arrangeNamesByIds(nowSelectedUserName, oUserIds);
         * initSelectBox(); hideKeyboard();
         * ActionManager.tabs.m_Layout.setVisibility(View.VISIBLE);
         * m_BottomTab.setVisibility(View.VISIBLE);
         * Message m = searchHandler.obtainMessage(Define.AM_CLEAR_ITEM, null);
         * searchHandler.sendMessage(m);
         * Intent i = new Intent(getActivity(),
         * kr.co.ultari.atsmart.basic.subview.SendNote.class); i.putExtra(
         * "USERID", userIds ); i.putExtra( "USERNAME", userNames );
         * startActivity(i); } catch(Exception e) { e.printStackTrace(); } }
         */
        public void initSelectBox()
        {
                try
                {
                        edit.setText( "" );
                        result.clear();
                        result.notifyDataSetChanged();
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public void closePopup()
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

        public void showKeyboard()
        {
                try
                {
                        InputMethodManager imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                        imm.showSoftInput( edit, 0 );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public void hideKeyboard()
        {
                try
                {
                        InputMethodManager imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                        if ( edit.getWindowToken() != null )
                        {
                                imm.hideSoftInputFromWindow( edit.getWindowToken(), 0 );
                                edit.clearFocus();
                        }
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
                ArrayList<SelectedUserView.UserData> users = m_SelectedUserList.getUsers();
                for ( int i = 0; i < users.size(); i++ )
                {
                        SelectedUserView.UserData data = users.get( i );
                        if ( !nowSelectedUserId.equals( "" ) ) nowSelectedUserId += ",";
                        nowSelectedUserId += data.userId;
                        if ( !nowSelectedUserName.equals( "" ) ) nowSelectedUserName += ",";
                        nowSelectedUserName += StringUtil.getNamePosition( data.userName );
                }
                String oUserIds = nowSelectedUserId + "," + Define.getMyId();
                String userIds = StringUtil.arrange( oUserIds );
                String userNames = nowSelectedUserName + "," + StringUtil.getNamePosition( Define.getMyName() );
                userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                
                //Log.d( "MadeIdAndNames", userIds + "/" + userNames );
                String roomId = "";
                if ( searchViewType == GroupSearchView.TYPE_CHAT )
                {
                        try
                        {
                                //Log.d( "UserCount", Define.oldRoomUserName );
                                int count = StringUtil.getChatRoomCount( Define.oldRoomUserName );
                                if ( count > 2 ) // 그룹채팅방에서 사용자 추가
                                {
                                        String[] getNewIdAndNames = getNewIdAndNames( userIds, userNames, Define.oldRoomUserId, Define.oldRoomUserName );
                                        String resName = getNewIdAndNames[1];
                                        String resId = getNewIdAndNames[0];
                                        String[] newName = resName.split( "," );
                                        String[] newId = resId.split( "," );
                                        roomId = Define.oldRoomId;
                                        String result = StringUtil.makeString( newId );
                                        result += "#" + StringUtil.makeString( newName );
                                        String dateTime = StringUtil.getNowDateTime() + "";
                                        // String msgId = Database.instance(
                                        // getApplicationContext()
                                        // ).insertChatContent( Define.getMyId(
                                        // context ) + "_" + dateTime,
                                        // Define.oldRoomId, Define.getMyId(
                                        // getApplicationContext() ),
                                        // Define.getMyName(),
                                        // Define.getMyNickName(), dateTime,
                                        // resName + getString(
                                        // R.string.gsInMessage ), userIds,
                                        // true, true );
                                        String msgId = Define.getMyId( context ) + "_" + dateTime;
                                        //Log.d( "ROOM_IN2", resId + ":" + Define.oldRoomUserId );
                                        sendChatRoomIn( resId, resName, msgId, Define.oldRoomId, Define.oldRoomUserId, Define.oldRoomUserName, "[ROOM_IN]" );
                                        Database.instance( context ).updateChatRoomUsers( Define.oldRoomId, userIds, userNames );
                                        ActionManager.openChat( context, Define.oldRoomId, userIds, userNames );
                                        TalkView.instance().resetData();
                                        Define.oldRoomId = "";
                                        Define.oldRoomUserId = "";
                                        Define.oldRoomUserName = "";
                                        Define.isAddUserMode = false;
                                }
                                else if ( count == 2 ) // 1대1방에서 사용자 추가이거나 그룹방이었던
                                {
                                        /*
                                         * String newRoomId = userIds.replace(
                                         * ",", "_" ); Log.e( TAG,
                                         * "old:"+Define.oldRoomId +
                                         * ", new:"+newRoomId );
                                         * if(!Define.oldRoomId.equals(
                                         * newRoomId )) {
                                         */
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
                                                String[] getNewIdAndNames = getNewIdAndNames( userIds, userNames, Define.oldRoomUserId, Define.oldRoomUserName );
                                                String resName = getNewIdAndNames[1];
                                                String resId = getNewIdAndNames[0];
                                                String[] newName = resName.split( "," );
                                                String[] newId = resId.split( "," );
                                                roomId = Define.oldRoomId;
                                                String result = StringUtil.makeString( newId );
                                                result += "#" + StringUtil.makeString( newName );
                                                String dateTime = StringUtil.getNowDateTime();
                                                String msgId = Database.instance( getApplicationContext() ).insertChatContent(
                                                                Define.getMyId( getApplicationContext() ) + "_" + dateTime, Define.oldRoomId,
                                                                Define.getMyId( getApplicationContext() ), Define.getMyName(), Define.getMyNickName(),
                                                                dateTime, resName + getString( R.string.gsInMessage ), userIds, true, true );
                                                
                                                //Log.d( "ROOM_IN1", resId + ":" + Define.oldRoomUserId );
                                                sendChatRoomIn( resId, resName, msgId, Define.oldRoomId, Define.oldRoomUserId, Define.oldRoomUserName,
                                                                "[ROOM_IN]" );
                                                Database.instance( getApplicationContext() ).updateChatRoomUsers( Define.oldRoomId, userIds, userNames );
                                                ActionManager.openChat( getApplicationContext(), Define.oldRoomId, userIds, userNames );
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
                                                ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfoByIds( userIds );
                                                if ( array.size() == 0 )
                                                {
                                                        Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames,
                                                                        StringUtil.getNowDateTime(), getString( R.string.newRoom ) );
                                                }
                                                else
                                                {
                                                        roomId = array.get( 0 ).get( 0 );
                                                }
                                                Define.oldRoomId = "";
                                                Define.oldRoomUserId = "";
                                                Define.oldRoomUserName = "";
                                                Define.isAddUserMode = false;
                                                ActionManager.openChat( getApplicationContext(), roomId, userIds, userNames );
                                        }
                                        // }
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
                else
                // 일반 검색 대화 모드
                {
                        try
                        {
                                if ( m_SelectedUserList.getUsers().size() > 1 ) roomId = "GROUP_" + StringUtil.getNowDateTime();
                                else roomId = userIds.replace( ",", "_" );
                                Define.oldRoomId = "";
                                Define.oldRoomUserId = "";
                                Define.oldRoomUserName = "";
                                Define.isAddUserMode = false;
                                ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfoByIds( userIds );
                                if ( array.size() == 0 )
                                {
                                        Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames, StringUtil.getNowDateTime(),
                                                        getString( R.string.newRoom ) );
                                }
                                else
                                {
                                        roomId = array.get( 0 ).get( 0 );
                                }
                                ActionManager.openChat( getApplicationContext(), roomId, userIds, userNames );
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
                clear();
                finish();
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
                        e.printStackTrace();
                }
        }

        public String[] getNewIdAndNames( String userIds, String userNames, String oldRoomUserId, String oldRoomUserName )
        {
                String[] newUserIds = userIds.split( "," );
                String[] newUserNames = userNames.split( "," );
                String[] oldUserIds = oldRoomUserId.split( "," );
                String newIds = "";
                String newNames = "";
                for ( int i = 0; i < newUserIds.length; i++ )
                {
                        if ( !isExist( newUserIds[i], oldUserIds ) )
                        {
                                if ( !newIds.equals( "" ) )
                                {
                                        newIds += ",";
                                        newNames += ",";
                                }
                                newIds += newUserIds[i];
                                newNames += newUserNames[i];
                        }
                }
                String[] retStr = new String[2];
                retStr[0] = newIds;
                retStr[1] = newNames;
                System.out.println( newIds + "/" + newNames );
                return retStr;
        }

        private boolean isExist( String id, String[] compare )
        {
                for ( int i = 0; i < compare.length; i++ )
                {
                        if ( compare[i].equals( id ) ) return true;
                }
                return false;
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
                                if ( !isUse )
                                {
                                        if ( !id.equals( "" ) ) id += ",";
                                        id += total[i];
                                }
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
        class Search extends Thread {
                String searchKey = null;

                public Search( String searchKey )
                {
                        this.searchKey = searchKey;
                        this.start();
                }

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
                        String searchKey = edit.getText().toString();
                        try
                        {
                                sb.delete( 0, sb.length() );
                                Message msg = searchHandler.obtainMessage( Define.AM_SEARCH_START, null );
                                searchHandler.sendMessage( msg );
                                sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                                .getServerPort( Define.mContext ) ) );
                                
                                sc.setSoTimeout( 30000 );
                                ir = new InputStreamReader( sc.getInputStream() );
                                br = new BufferedReader( ir );
                                
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
                                Log.e(TAG, "type :"+ type);
                                //2016-02-01
                                switch(Define.SET_COMPANY)
                                {
                                        case Define.MBC:
                                        case Define.DEMO:
                                                if(Define.usePhoneState) 
                                                        send("UcSearchRequest\t" + type + "\t" + searchKey ); 
                                                else 
                                                        send("SearchRequest\t" + type + "\t" + searchKey );
                                                break;
                                        case Define.IPAGEON:
                                        	if ( type == 6 ) type = 7;
                                        	if(Define.usePhoneState) 
                                                send("UcSearchRequest\t" + type + "\t" + searchKey ); 
                                        	else 
                                                send("SearchRequest\t" + type + "\t" + searchKey );
                                        	break;
                                        default:
                                                if ( Define.useMyTopPartVisible ) 
                                                        send( "SearchRequest\t" + type + "\t" + searchKey + "\t" + Define.getMyId( context ) );
                                                else 
                                                        send( "SearchRequest\t" + type + "\t" + searchKey );
                                                break;
                                }
                                /*if ( Define.useMyTopPartVisible ) 
                                        send( "SearchRequest\t" + type + "\t" + searchKey + "\t" + Define.getMyId( context ) );
                                else 
                                        send( "SearchRequest\t" + type + "\t" + searchKey );*/
                                
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
                                                                if ( command.equals( "" ) ) command = nowStr;
                                                                else param.add( nowStr );
                                                                nowStr = "";
                                                        }
                                                        else if ( i == (rcvStr.length() - 1) )
                                                        {
                                                                nowStr += rcvStr.charAt( i );
                                                                if ( command.equals( "" ) ) command = nowStr;
                                                                else param.add( nowStr );
                                                                nowStr = "";
                                                        }
                                                        else
                                                        {
                                                                nowStr += rcvStr.charAt( i );
                                                        }
                                                }
                                                if ( command.equals( "SearchEnd" ) )
                                                {
                                                        return;
                                                }
                                                else
                                                {
                                                        process( command, param, searchKey );
                                                        //Log.d( "Search", "AddResult" );
                                                }
                                        }
                                }
                        }
                        catch ( SocketException se )
                        {
                                Log.e( TAG, se.getMessage() );
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
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
                                if ( noopTimer != null )
                                {
                                        noopTimer.cancel();
                                        noopTimer = null;
                                }
                                Message msg = searchHandler.obtainMessage( Define.AM_SEARCH_END, null );
                                searchHandler.sendMessage( msg );
                        }
                }
        }

        public void send( String msg ) throws Exception
        {
                msg.replaceAll( "\f", "" );
                sc.send( codec.EncryptSEED( msg ) + '\f' );
        }
        public Handler searchHandler = new Handler() {
                @SuppressLint( "NewApi" )
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_ADD_SEARCH )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        String key = param.get( param.size() - 1 );
                                        if ( !edit.getText().toString().equals( key ) ) return;
                                        param.remove( param.size() - 1 );
                                        String nick = "";
                                        if ( param.size() > 5 ) nick = param.get( 6 );
                                        for ( int i = 0; i < result.getCount(); i++ )
                                        {
                                                if ( result.getItem( i ).id != null && result.getItem( i ).id.equals( param.get( 0 ) ) )
                                                {
                                                        return;
                                                }
                                        }
                                        result.add( new SearchResultItemData( param.get( 0 ), param.get( 1 ), param.get( 3 ),
                                                        Integer.parseInt( param.get( 2 ) ), nick, m_SelectedUserList.isExist( param.get( 0 ) ) ) );
                                }
                                else if ( msg.what == Define.AM_ICON )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        setIcon( param.get( 0 ), Integer.parseInt( param.get( 1 ) ) );
                                }
                                else if ( msg.what == Define.AM_NICK )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        setNick( param.get( 0 ), param.get( 1 ) );
                                }
                                else if ( msg.what == Define.AM_CLEAR_ITEM )
                                {
                                        clear();
                                }
                                else if ( msg.what == Define.AM_SEARCH_START )
                                {
                                        m_WaitForSearchProgressDialog = ProgressDialog.show( groupSearchView, "", "검색결과를 수신중입니다.", true );
                                        m_WaitForSearchProgressDialog.show();
                                }
                                else if ( msg.what == Define.AM_SEARCH_END )
                                {
                                        ActionManager.hideProgressDialog();
                                        result.notifyDataSetChanged();
                                        if ( m_WaitForSearchProgressDialog != null )
                                        {
                                                m_WaitForSearchProgressDialog.dismiss();
                                                m_WaitForSearchProgressDialog = null;
                                        }
                                }
                                else if ( msg.what == Define.AM_SELECT_CHANGED )
                                {
                                        for ( int i = 0; i < result.getCount(); i++ )
                                        {
                                                if ( m_SelectedUserList.isExist( result.getItem( i ).id ) ) result.setCheck( result.getItem( i ).id, true );
                                                else result.setCheck( result.getItem( i ).id, false );
                                        }
                                        result.notifyDataSetChanged();
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

        public void process( String command, ArrayList<String> param, String searchKey )
        {
                if ( command.equals( "User" ) && param.size() >= 5 )
                //if ( command.equals( "User" ) && param.size() >= 5 && !param.get( 0 ).equals( Define.getMyId( context ) ) )
                {
                        Define.searchMobileOn.put( param.get( 0 ), param.get( 8 ) );
                        if ( Define.usePhoneState )
                        {
                                if ( param.size() > 9 ) Define.searchUcOn.put( param.get( 0 ), param.get( 9 ) );
                        }
                        if ( edit.getText().toString().equals( searchKey ) )
                        {
                                param.add( searchKey );
                                Message m = searchHandler.obtainMessage( Define.AM_ADD_SEARCH, param );
                                searchHandler.sendMessage( m );
                        }
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
                if ( searchViewType == TYPE_ORGANIZATION )
                {
                        ActionManager.popupUserInfo( context, nowSelectedUserId, nowSelectedUserName, Define.getPartNameByUserId( nowSelectedUserName ),
                                        result.getItem( position ).nickName );
                }
                else
                {
                        int pos = -1;
                        boolean isExist = m_SelectedUserList.isExist( nowSelectedUserId );
                        if ( isExist )
                        {
                                m_SelectedUserList.removeUser( nowSelectedUserId );
                        }
                        else
                        {
                                m_SelectedUserList.addUser( nowSelectedUserId, StringUtil.getNamePosition( nowSelectedUserName ) );
                        }
                        Message m = searchHandler.obtainMessage( Define.AM_SELECT_CHANGED );
                        searchHandler.sendMessage( m );
                }
                /*
                 * registerForContextMenu(listView);
                 * openContextMenu(listView);
                 * unregisterForContextMenu(listView);
                 */
        }

        @Override
        public boolean onTouch( View v, MotionEvent event )
        {
                int action = event.getAction();
                int id = v.getId();
                if ( action == MotionEvent.ACTION_DOWN )
                {
                        if ( id == R.id.searchview_SearchUserResult )
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

        @Override
        public void onRecalcHeight( int height )
        {
                m_SelectedUserList.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.MATCH_PARENT, height ) );
        }

        @Override
        public void onDeleteUser( View view, String userId )
        {
                Message m = searchHandler.obtainMessage( Define.AM_SELECT_CHANGED );
                searchHandler.sendMessage( m );
        }
}
