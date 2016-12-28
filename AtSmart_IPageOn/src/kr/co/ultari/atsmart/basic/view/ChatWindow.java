package kr.co.ultari.atsmart.basic.view;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.ChatData;
import kr.co.ultari.atsmart.basic.subview.ChatItem;
import kr.co.ultari.atsmart.basic.util.ImageUtil;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import kr.co.ultari.atsmart.basic.subview.CustomDeleteRoomDialog;
import kr.co.ultari.atsmart.basic.view.ChatWindow;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.DisplayMetrics;
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

@SuppressLint( { "HandlerLeak", "NewApi" } )
//ChatSelectRowsPerOneTime
public class ChatWindow extends MessengerActivity implements OnClickListener, OnScrollListener {
        private static final String TAG = "/AtSmart/ChatWindow";
        private static final String TEMP_PHOTO_FILE = "AtTalkTemp.jpg";
        private TableLayout bottomBar, emoticonLayout;
        private TableRow tableRow;
        private RelativeLayout searchTableRow;
        private ImageButton btnSearchUp, btnSearchDown, btnSearchDelete, btnEmoticon;
        // private ImageButton btnSearchChat;
        private EditText searchInput;
        private TextView resultCount;
        private boolean isSearchMode = false;
        private boolean isEmoticonMode = false;
        private boolean isOptionMode = false;
        private final int SELECT_IMAGE = 1;
        private final int SELECT_VIDEO = 2;
        private final int SELECT_CAMERA = 3;
        private final int SELECT_FILE = 4;
        private final int RE_SEND = 5;
        private final int DELETE_CHAT = 6;
        private final int COPY_TEXT = 7;
        private final int COPY_GALLERY = 8;
        // private final int OPTION_MAIL = 8;
        // private final int OPTION_ALBUM = 9;
        // private int select = OPTION_MAIL;
        /*
         * private Button btnDelChat; private Button btnMobileStatus;
         */
        private Button btnOption;
        private ImageButton btnSend;
        private ImageButton btnAddFile;
        // private ImageButton btnAddUser;
        private ImageButton btnClose;
        private TextView lblTitle;
        public EditText chatInput;
        public ListView chatOutput;
        private ChatItem ItemList = null;
        private ArrayList<ChatData> ItemData;
        public String roomId;
        public String userIds;
        public String userNames;
        private HashMap<String, String> map;
        private DisplayMetrics mMetrics;
        // private ChatOptionDialog mDialog;
        private ChatData contextMenuData = null;
        private LinearLayout popupLayout = null;
        private int refreshCount = 0;
        private boolean isReachFirst = false;
        private ArrayList<String> searchResult = null;
        private int nowSelectSearchResult = 0;
        private Button btnSearch, btnAlbum, btnUserList, btnAddGroupUser, btnDelroom; //2016-03-31
        private TextView tvOptionTitle; //2016-03-31

        @Override
        public void onDestroy()
        {
                TRACE( "onDestroy" );
                if ( MainActivity.cw == this ) MainActivity.cw = null;
                if ( MainActivity.mainActivity == null ) Define.clearUserImages();
                btnEmoticon.setImageBitmap( null );
                btnSend.setImageBitmap( null );
                btnAddFile.setImageBitmap( null );
                // btnAddUser.setImageBitmap( null );
                btnClose.setImageBitmap( null );
                // btnSearchChat.setImageBitmap( null );
                btnSearchUp.setImageBitmap( null );
                btnSearchDown.setImageBitmap( null );
                btnSearchDelete.setImageBitmap( null );
                map.clear();
                super.onDestroy();
        }

        public String getSearchKey()
        {
                if ( isSearchMode == false ) return null;
                else return searchInput.getText().toString();
        }

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                try
                {
                        Define.mContext = this;
                        setContentView( R.layout.popup_chat_window );
                        if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE );
                        userIds = getIntent().getStringExtra( "userIds" );
                        userNames = getIntent().getStringExtra( "userNames" );
                        roomId = getIntent().getStringExtra( "roomId" );
                        if ( roomId == null )
                        {
                                roomId = getIntent().getStringExtra( "RoomId" );
                                ArrayList<ArrayList<String>> ar = Database.instance( getApplicationContext() ).selectChatRoomInfo( roomId );
                                if ( ar != null )
                                {
                                        userIds = ar.get( 0 ).get( 1 );
                                        userNames = ar.get( 0 ).get( 2 );
                                }
                        }
                        map = new HashMap<String, String>();
                        initEmoticon();
                        GridView gridview = ( GridView ) findViewById( R.id.emoticon_list );
                        gridview.setAdapter( new ImageAdapter( this ) );
                        gridview.setOnItemClickListener( gridviewOnItemClickListener );
                        mMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics( mMetrics );
                        /*
                         * btnDelChat = (Button) findViewById(R.id.delChat);
                         * btnDelChat.setOnClickListener(this);
                         * btnMobileStatus = (Button)
                         * findViewById(R.id.mobileStatus);
                         * btnMobileStatus.setOnClickListener(this);
                         */
                        popupLayout = ( LinearLayout ) findViewById( R.id.popupchat_layout );
                        
                        tvOptionTitle = ( TextView ) findViewById( R.id.popupchat_chat_title ); 
                        btnSearch = ( Button ) findViewById( R.id.popupchat_chat_search );
                        btnAlbum = ( Button ) findViewById( R.id.popupchat_chat_album );
                        btnUserList = ( Button ) findViewById( R.id.popupchat_chat_userlist );
                        btnAddGroupUser = ( Button ) findViewById( R.id.popupchat_chat_adduser );
                        btnDelroom = ( Button ) findViewById( R.id.popupchat_chat_delroom );
                        tvOptionTitle.setTypeface( Define.tfRegular ); 
                        
                        btnOption = ( Button ) findViewById( R.id.chatOption );
                        btnOption.setOnClickListener( this );
                        btnClose = ( ImageButton ) findViewById( R.id.closeChat );
                        btnClose.setOnClickListener( this );
                        lblTitle = ( TextView ) findViewById( R.id.titleLabel );
                        lblTitle.setOnClickListener( this );
                        btnSend = ( ImageButton ) findViewById( R.id.btn_send );
                        btnSend.setOnClickListener( this );
                        btnAddFile = ( ImageButton ) findViewById( R.id.addFile );
                        btnAddFile.setOnClickListener( this );
                        btnEmoticon = ( ImageButton ) findViewById( R.id.btn_emoticon );
                        btnEmoticon.setOnClickListener( this );
                        switch ( Define.SET_COMPANY )
                        {
                        case Define.CU :
                                // case Define.REDCROSS:
                                btnAddFile.setVisibility( View.GONE );
                                break;
                        default :
                                btnAddFile.setVisibility( View.VISIBLE );
                        }
                        /*
                         * btnAddUser = (ImageButton)findViewById(R.id.addUser);
                         * btnAddUser.setOnClickListener(this);
                         */
                        /*
                         * btnSearchChat = ( ImageButton ) findViewById(
                         * R.id.searchChat ); btnSearchChat.setOnClickListener(
                         * this );
                         */
                        /*
                         * if(Define.useMessageSearch)
                         * btnSearchChat.setVisibility( View.VISIBLE ); else
                         * btnSearchChat.setVisibility( View.GONE );
                         */
                        btnSearchUp = ( ImageButton ) findViewById( R.id.searchUp );
                        btnSearchUp.setOnClickListener( this );
                        btnSearchDown = ( ImageButton ) findViewById( R.id.searchDown );
                        btnSearchDown.setOnClickListener( this );
                        btnSearchDelete = ( ImageButton ) findViewById( R.id.searchDelete );
                        btnSearchDelete.setOnClickListener( this );
                        searchInput = ( EditText ) findViewById( R.id.searchValue );
                        searchInput.setTypeface( Define.tfRegular );
                        searchInput.setOnEditorActionListener( new OnEditorActionListener() {
                                @Override
                                public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
                                {
                                        if ( actionId == EditorInfo.IME_ACTION_SEARCH ) searchData();
                                        return false;
                                }
                        } );
                        searchInput.setOnTouchListener( mTouchEvent );
                        searchInput.addTextChangedListener( new TextWatcher() {
                                public void afterTextChanged( Editable s )
                                {
                                }

                                public void beforeTextChanged( CharSequence s, int start, int count, int after )
                                {
                                }

                                public void onTextChanged( CharSequence s, int start, int before, int count )
                                {
                                        try
                                        {
                                                String searchKeyword = s.toString();
                                                if ( searchKeyword.equals( "" ) )
                                                {
                                                        btnSearchDelete.setVisibility( View.GONE );
                                                        searchInput.setMovementMethod( null );
                                                        searchInput.setBackgroundResource( R.drawable.talk_search_bg_selected );
                                                        hideKeyboard();
                                                }
                                                else
                                                {
                                                        btnSearchDelete.setVisibility( View.VISIBLE );
                                                        searchInput.setMovementMethod( new ScrollingMovementMethod() );
                                                        searchInput.requestFocus();
                                                        searchInput.setFocusable( true );
                                                        searchInput.setBackgroundResource( R.drawable.talk_search_bg_selected );
                                                }
                                        }
                                        catch ( Exception e )
                                        {
                                                e.printStackTrace();
                                        }
                                }
                        } );
                        searchResult = new ArrayList<String>();
                        tableRow = ( TableRow ) findViewById( R.id.TitleBar );
                        searchTableRow = ( RelativeLayout ) findViewById( R.id.searchTitleBar );
                        bottomBar = ( TableLayout ) findViewById( R.id.BottomBar );
                        emoticonLayout = ( TableLayout ) findViewById( R.id.emoticion_layout );
                        if ( !Define.useEmoticon )
                        {
                                emoticonLayout.setVisibility( View.GONE );
                                btnEmoticon.setVisibility( View.GONE );
                        }
                        resultCount = ( TextView ) findViewById( R.id.searchResultCount );
                        resultCount.setText( getString( R.string.result ) + "0" + getString( R.string.count ) );
                        int count = StringUtil.getChatRoomCount( userNames );
                        lblTitle.setTypeface( Define.tfRegular );
                        if ( count > 2 ) lblTitle.setText( getString( R.string.groupchat ) + count + getString( R.string.people ) );
                        else lblTitle.setText( StringUtil.getChatRoomName( userNames, userIds ) ); 
                        chatInput = ( EditText ) findViewById( R.id.chatInput );
                        chatInput.setTypeface( Define.tfRegular );
                        chatInput.setImeOptions( EditorInfo.IME_ACTION_NONE | EditorInfo.IME_FLAG_NO_FULLSCREEN );
                        /*chatInput.addTextChangedListener( new TextWatcher() {
                                String previousString = "";

                                @Override
                                public void onTextChanged( CharSequence s, int start, int before, int count )
                                {
                                }

                                @Override
                                public void beforeTextChanged( CharSequence s, int start, int count, int after )
                                {
                                        previousString = s.toString();
                                }

                                @Override
                                public void afterTextChanged( Editable s )
                                {
                                        if ( chatInput.getLineCount() >= 6 )
                                        {
                                                chatInput.setText( previousString );
                                                chatInput.setSelection( chatInput.length() );
                                        }
                                }
                        } );*/
                        chatOutput = ( ListView ) findViewById( R.id.chatView );
                        ItemData = new ArrayList<ChatData>();
                        ItemList = new ChatItem( this, ItemData, uploadHandler, ( View ) chatOutput, this );
                        chatOutput.setAdapter( ItemList );
                        chatOutput.setDivider( null );
                        chatOutput.setSelector( R.drawable.no_list_selector );
                        chatOutput.setTranscriptMode( ListView.TRANSCRIPT_MODE_NORMAL );
                        if ( Define.SELCT_BACKGROUND_MODE.equals( "IMAGE" ) )
                        {
                                String filePath = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ) + java.io.File.separator
                                                + TEMP_PHOTO_FILE;
                                File f = new File( filePath );
                                if ( f.exists() )
                                {
                                        Bitmap selectedImage = BitmapFactory.decodeFile( filePath );
                                        if ( selectedImage == null ) chatOutput.setBackgroundColor( 0xFFEdEdEd );
                                        else
                                        {
                                                if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
                                                // chatOutput.setBackground(getDrawableFromBitmap(selectedImage));
                                                getWindow().setBackgroundDrawable( getDrawableFromBitmap( selectedImage ) );
                                                else
                                                // chatOutput.setBackgroundDrawable(getDrawableFromBitmap(selectedImage));
                                                getWindow().setBackgroundDrawable( getDrawableFromBitmap( selectedImage ) );
                                        }
                                }
                        }
                        else
                        {
                                chatOutput.setBackgroundColor( Define.SELECT_BACKGROUND_COLOR );
                        }
                        resetChatData();// ChatSelectRowsPerOneTime
                        chatOutput.setSelection( chatOutput.getAdapter().getCount() - 1 );
                        chatOutput.setOnScrollListener( this ); // ChatSelectRowsPerOneTime
                        registerForContextMenu( btnAddFile );
                        registerForContextMenu( chatOutput );
                        if ( MainActivity.context != null ) sendReadComplete( roomId, userIds, userNames, MainActivity.context );
                        else sendReadComplete( roomId, userIds, userNames, this );
                        Database.instance( getApplicationContext() ).updateChatRoomRead( roomId );
                        Message m = MainActivity.mainHandler.obtainMessage( Define.AM_NEW_CHAT, null );
                        MainActivity.mainHandler.sendMessage( m );
                        if ( TalkView.instance().talkHandler != null )
                        {
                                m = TalkView.instance().talkHandler.obtainMessage( Define.AM_REFRESH, null );
                                TalkView.instance().talkHandler.sendMessage( m );
                        }
                        NotificationManager nm = ( NotificationManager ) getSystemService( NOTIFICATION_SERVICE );
                        nm.cancel( Define.AtSmartServiceFinished );
                        nm.cancel( Define.AtSmartPushNotification );
                        MainActivity.cw = this;
                        Define.isHomeMode = false;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void openPopupMenu( ChatData data )
        {
                contextMenuData = data;
                openContextMenu( chatOutput );
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
                                searchInput.setFocusable( true );
                                searchInput.setSelection( searchInput.length() );
                                b.setBackgroundResource( R.drawable.talk_search_bg_selected );
                        }
                        else if ( action == MotionEvent.ACTION_UP )
                        {}
                        return false;
                }
        };

        public Drawable getDrawableFromBitmap( Bitmap bitmap )
        {
                Drawable d = new BitmapDrawable( getResources(), bitmap );
                return d;
        }

        private void initEmoticon()
        {
                for ( int i = 0; i < Define.mThumbIds.length; i++ )
                        map.put( Integer.toString( Define.mThumbIds[i] ), Define.mThumbNames[i] );
        }
        private GridView.OnItemClickListener gridviewOnItemClickListener = new GridView.OnItemClickListener() {
                @SuppressWarnings( "deprecation" )
                public void onItemClick( AdapterView<?> arg0, View arg1, int arg2, long arg3 )
                {
                        Log.d( "OnItemClickListener", "OnItemClickListener" );
                        emoticonLayout.setVisibility( View.GONE );
                        isEmoticonMode = false;
                        int start = chatInput.getSelectionStart();
                        chatInput.append( "/E" + Define.mEmoticonMappingNameMap.get( map.get( arg0.getAdapter().getItem( arg2 ).toString() ) ) + "/" );
                        int end = chatInput.getSelectionEnd();
                        Spannable span = chatInput.getText();
                        Bitmap bm = BitmapFactory.decodeResource( getResources(), Integer.parseInt( arg0.getAdapter().getItem( arg2 ).toString() ) );
                        Bitmap resizeBm = Bitmap.createScaledBitmap( bm, bm.getWidth() * 2, bm.getHeight() * 2, true );
                        span.setSpan( new ImageSpan( resizeBm ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
                }
        };
        public class ImageAdapter extends BaseAdapter {
                private Context mContext;

                public ImageAdapter( Context c )
                {
                        mContext = c;
                }

                public int getCount()
                {
                        return Define.mThumbIds.length;
                }

                public Object getItem( int position )
                {
                        return Define.mThumbIds[position];
                }

                public long getItemId( int position )
                {
                        return position;
                }

                // create a new ImageView for each item referenced by the
                // Adapter
                public View getView( int position, View convertView, ViewGroup parent )
                {
                        int rowWidth = (mMetrics.widthPixels) / 12;
                        ImageView imageView;
                        if ( convertView == null )
                        {
                                imageView = new ImageView( mContext );
                                imageView.setLayoutParams( new GridView.LayoutParams( rowWidth, rowWidth ) );
                                imageView.setScaleType( ImageView.ScaleType.CENTER_CROP );
                                imageView.setPadding( 8, 8, 8, 8 );
                        }
                        else
                        {
                                imageView = ( ImageView ) convertView;
                        }
                        imageView.setImageResource( Define.mThumbIds[position] );
                        return imageView;
                }
        }

        private void sendReadComplete( String roomId, String userIds, String userNames, Context context )
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
                                Intent sendIntent = new Intent( Define.MSG_READ_COMPLETE );
                                sendIntent.putExtra( "MESSAGE", message.toString() );
                                sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                                sendBroadcast( sendIntent );
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        /*
         * private boolean isExist(String date, String content)
         * {
         * for (int i = (ItemList.getCount() - 1); i >= 0; i--)
         * {
         * if (ItemList.getItem(i).talkDate.equals(date) && ItemList.getItem(i).talkContent.equals(content))
         * return true;
         * }
         * return false;
         * }
         */
        public void insertData( String chatId )
        {
                ArrayList<ArrayList<String>> array = Database.instance( getApplicationContext() ).selectChatData( chatId );
                // "sChatId", "sRoomId", "sTalkerId", "sTalkerName", "sTalkerNickName", "sTalkDate", "sTalkerContent", "sSendComplete", "sUnReadUserIds",
                // "sReserved"
                if ( array.size() == 1 )
                {
                        boolean m_bSendComplete = false;
                        if ( array.get( 0 ).get( 7 ) != null && array.get( 0 ).get( 7 ).equals( "Y" ) ) m_bSendComplete = true;
                        insertData( array.get( 0 ).get( 0 ), array.get( 0 ).get( 1 ), array.get( 0 ).get( 2 ), array.get( 0 ).get( 3 ),
                                        array.get( 0 ).get( 4 ), array.get( 0 ).get( 5 ), array.get( 0 ).get( 6 ), 0, m_bSendComplete, array.get( 0 ).get( 8 ) );
                }
        }

        public void insertData( String msgId, String roomId, String talkerId, String talkerName, String talkerNickName, String talkDate, String talkContent,
                        int percent, boolean sendComplete, String unreadUserIds )
        {
                // ArrayList<ArrayList<String>> array = Database.instance(getApplicationContext()).selectChatContent(roomId);
                // if (array.size() == 0 || array == null)
                // return;
                // if (!isExist(array.get(array.size() - 1).get(5), array.get(array.size() - 1).get(6)))
                // {
                // ArrayList<String> ar = array.get(array.size() - 1);
                //
                ItemData.add( new ChatData( msgId, roomId, talkerId, talkerName, talkerNickName, talkDate, talkContent, percent, sendComplete, unreadUserIds ) );
                /*
                 * if (ar.get(7).equals("Y"))
                 * ItemList.add(new ChatData(msgId, roomId, talkerId, talkerName, talkerNickName, talkDate, talkContent, percent, sendComplete, unreadUserIds));
                 * else
                 * ItemList.add(new ChatData(ar.get(0), roomId, ar.get(2), ar.get(3), ar.get(4), ar.get(5), ar.get(6), 0, false, ar.get(8)));
                 */
                // }
                checkDate(); // 2015-09-05
                ItemList.notifyDataSetChanged();
                chatOutput.setSelection( ItemList.getCount() );
        }

        // 2015-09-05
        /*
         * private void checkDate(String oldDate, String newDate, boolean addLast)
         * {
         * if (oldDate.length() < 8)
         * return;
         * if (newDate.length() < 8)
         * return;
         * if (!oldDate.substring(0, 8).equals(newDate.substring(0, 8)))
         * {
         * try
         * {
         * if ( addLast )
         * {
         * ItemList.add(new ChatData(newDate.substring(0, 8), roomId, "system", "", "", newDate, newDate.substring(0, 4) + "." + newDate.substring(4, 6) + "." +
         * newDate.substring(6, 8) + " "
         * + StringUtil.getDateDay(newDate.substring(0, 4) + "-" + newDate.substring(4, 6) + "-" + newDate.substring(6, 8), "yyyy-MM-dd"), 0, true, ""));
         * }
         * else
         * {
         * ItemList.insert(new ChatData(newDate.substring(0, 8), roomId, "system", "", "", newDate, newDate.substring(0, 4) + "." + newDate.substring(4, 6) +
         * "." + newDate.substring(6, 8) + " "
         * + StringUtil.getDateDay(newDate.substring(0, 4) + "-" + newDate.substring(4, 6) + "-" + newDate.substring(6, 8), "yyyy-MM-dd"), 0, true, ""), 0);
         * }
         * }
         * catch (Exception e)
         * {
         * e.printStackTrace();
         * }
         * }
         * }
         */
        public void hideKeyboard()
        {
                InputMethodManager imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                if ( chatInput.getWindowToken() != null ) imm.hideSoftInputFromWindow( chatInput.getWindowToken(), 0 );
        }

        @Override
        public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo )
        {
                super.onCreateContextMenu( menu, v, menuInfo );
                if ( v == btnAddFile )
                {
                        menu.setHeaderTitle( getString( R.string.choiceFile ) );
                        menu.add( 0, SELECT_IMAGE, Menu.NONE, getString( R.string.album ) );
                        menu.add( 0, SELECT_VIDEO, Menu.NONE, getString( R.string.video ) );
                        menu.add( 0, SELECT_CAMERA, Menu.NONE, getString( R.string.camera ) );
                        menu.add( 0, SELECT_FILE, Menu.NONE, getString( R.string.explore ) );
                }
                else if ( v == chatOutput )
                {
                        if ( contextMenuData == null || contextMenuData.talkerId == null ) return;
                        menu.setHeaderTitle( getString( R.string.choiceChat ) );
                        menu.clear();
                        if ( contextMenuData.talkerId.equals( Define.getMyId() ) )
                        {
                                menu.add( 0, RE_SEND, Menu.NONE, getString( R.string.resendChat ) );
                        }
                        menu.add( 0, DELETE_CHAT, Menu.NONE, getString( R.string.delete ) );
                        if ( contextMenuData.dataType == Define.TYPE_CHAT )
                        {
                                menu.add( 0, COPY_TEXT, Menu.NONE, getString( R.string.copy ) );
                        }
                        if ( !contextMenuData.talkerId.equals( Define.getMyId() ) && contextMenuData.dataType == Define.TYPE_IMAGE )
                        {
                                menu.add( 0, COPY_GALLERY, Menu.NONE, getString( R.string.saveToGallery ) );
                        }
                }
        }

        @Override
        protected void onActivityResult( int requestCode, int resultCode, Intent intent )
        {
                super.onActivityResult( requestCode, resultCode, intent );
                try
                {
                        String filePath = null;
                        if ( resultCode == RESULT_OK )
                        {
                                if ( requestCode == SELECT_IMAGE )
                                {
                                        Uri _uri = intent.getData();
                                        Cursor c = getContentResolver().query( _uri, null, null, null, null );
                                        if ( c.moveToNext() )
                                        {
                                                filePath = c.getString( c.getColumnIndex( MediaStore.MediaColumns.DATA ) );
                                        }
                                        c.close();
                                        onActivityResult( filePath );
                                }
                                else if ( requestCode == SELECT_VIDEO )
                                {
                                        Uri _uri = intent.getData();
                                        Cursor c = getContentResolver().query( _uri, null, null, null, null );
                                        if ( c.moveToNext() )
                                        {
                                                filePath = c.getString( c.getColumnIndex( MediaStore.MediaColumns.DATA ) );
                                        }
                                        c.close();
                                        onActivityResult( filePath );
                                }
                                else if ( requestCode == SELECT_CAMERA )
                                {
                                        Uri _uri = intent.getData();
                                        Cursor c = getContentResolver().query( _uri, null, null, null, null );
                                        if ( c.moveToNext() ) filePath = c.getString( c.getColumnIndex( MediaStore.MediaColumns.DATA ) );
                                        c.close();
                                        onActivityResult( filePath );
                                }
                                else if ( requestCode == SELECT_FILE )
                                {
                                        filePath = intent.getStringExtra( "PATH" );
                                        onActivityResult( filePath );
                                }
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void onActivityResult( String filePath )
        {
                sendFile( filePath );
        }

        public void sendFile( String filePath )
        {
                try
                {
                        CharSequence chatMessage = "FILE://" + filePath;
                        Log.d( "chatMessage", chatMessage.toString() );
                        hideKeyboard();
                        String dateTime = StringUtil.getNowDateTime() + "";
                        String sMsgId = Database.instance( getApplicationContext() ).insertChatContent(
                                        Define.getMyId( getApplicationContext() ) + "_" + dateTime, roomId, Define.getMyId( getApplicationContext() ),
                                        Define.getMyName(), Define.getMyNickName(), dateTime, chatMessage.toString(), userIds, false, false );
                        checkDate();
                        Log.d( "checkImage", "Is image ? " + ImageUtil.checkTypeImageFile( filePath ) );
                        ChatData data = new ChatData( sMsgId, roomId, Define.getMyId( getApplicationContext() ), Define.getMyName(), Define.getMyNickName(),
                                        dateTime, chatMessage.toString(), 0, false, userIds );
                        if ( ImageUtil.checkTypeImageFile( filePath ) )
                        {
                                String[] ret = ImageUtil.saveResizeImageFile( getApplicationContext(), ImageUtil.makeThumbnailImage( filePath ), sMsgId
                                                + filePath.substring( filePath.lastIndexOf( '.' ) ) );
                                data.thumbnailFilePath = ret[0];
                                data.thumbnailWidth = ret[1];
                                data.thumbnailHeight = ret[2];
                                data.thumbnailFileName = "small_" + filePath.substring( filePath.lastIndexOf( '/' ) + 1 );
                        }
                        data.uploadFilePath = filePath;
                        data.uploadFileName = filePath.substring( filePath.lastIndexOf( '/' ) + 1 );
                        data.userIds = userIds;
                        data.userNames = userNames;
                        data.needUpload = true;
                        data.upload( this, ItemList.itemHandler );
                        ItemList.add( data );
                        ItemList.notifyDataSetChanged();
                        Database.instance( getApplicationContext() ).updateChatRoomInfo( roomId, dateTime, chatMessage.toString(), true );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        @Override
        public void onWindowFocusChanged( boolean hasFocus )
        {
                super.onWindowFocusChanged( hasFocus );
        }
        public Handler uploadHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_SEND_COMPLETE )
                                {
                                        String msgId = ( String ) msg.obj;
                                        Message m = ItemList.itemHandler.obtainMessage( Define.AM_SEND_COMPLETE, msgId );
                                        ItemList.itemHandler.sendMessage( m );
                                        Database.instance( getApplicationContext() ).updateChatContentComplete( msgId );
                                }
                                else if ( msg.what == Define.AM_READ_COMPLETE )
                                {
                                        String[] ar = ( String[] ) msg.obj;
                                        String msgId = ar[0];
                                        String talkerId = ar[1];
                                        for ( int i = 0; i < ItemList.getCount(); i++ )
                                        {
                                                if ( ItemList.getItem( i ).msgId.equals( msgId ) )
                                                {
                                                        ItemList.getItem( i ).unreadUserIds = StringUtil.makeString( StringUtil.getOtherIds(
                                                                        ItemList.getItem( i ).unreadUserIds, talkerId ) );
                                                }
                                        }
                                        Message m = ItemList.itemHandler.obtainMessage( Define.AM_READ_COMPLETE, msgId );
                                        ItemList.itemHandler.sendMessage( m );
                                }
                                else if ( msg.what == Define.AM_USER_CHANGED )
                                {
                                        String[] ar = ( String[] ) msg.obj;
                                        userIds = ar[0];
                                        userNames = ar[1];
                                        int count = StringUtil.getChatRoomCount( userNames );
                                        if ( count > 2 ) lblTitle.setText( getString( R.string.groupchat ) + count + getString( R.string.people ) );
                                        else lblTitle.setText( StringUtil.getChatRoomName( userNames, userIds ) ); // 2015-06-09
                                }
                                else if ( msg.what == Define.AM_SEARCH_ITEM_FOCUS )
                                {
                                        Animation anim = AnimationUtils.loadAnimation( getBaseContext(), R.anim.shake );
                                        String[] ar = ( String[] ) msg.obj;
                                        ItemList.getItem( Integer.parseInt( ar[0] ) ).view.startAnimation( anim );
                                        if ( ItemList.getItem( Integer.parseInt( ar[0] ) ).talkerId.equals( Define.getMyId( getApplicationContext() ) ) )
                                        {
                                                TextView tvContentsSentence = ( TextView ) ItemList.getItem( Integer.parseInt( ar[0] ) ).view.findViewById( R.id.right_bubble_content );
                                                setTextViewColorPartial( tvContentsSentence, ItemList.getItem( Integer.parseInt( ar[0] ) ).talkContent,
                                                                searchInput.getText().toString(), Color.rgb( 243, 161, 13 ) ); 
                                        }
                                        else
                                        {
                                                TextView tvContentsSentence = ( TextView ) ItemList.getItem( Integer.parseInt( ar[0] ) ).view.findViewById( R.id.left_bubble_content );
                                                setTextViewColorPartial( tvContentsSentence, ItemList.getItem( Integer.parseInt( ar[0] ) ).talkContent,
                                                                searchInput.getText().toString(), Color.rgb( 243, 161, 13 ) );
                                        }
                                }
                                else if ( msg.what == Define.AM_SEARCH_INIT_DRAW )
                                {
                                }
                                else if ( msg.what == Define.AM_COMPLETE )
                                {
                                        chatOutput.setSelection( ItemList.getCount() - 1 );
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
        
        public static void setTextViewColorPartial( TextView view, String fulltext, String subtext, int color )
        {
                view.setText( fulltext, TextView.BufferType.SPANNABLE );
                Spannable str = ( Spannable ) view.getText();
                int i = fulltext.indexOf( subtext );
                str.setSpan( new ForegroundColorSpan( color ), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
        }

        @Override
        public void onConfigurationChanged( Configuration newConfig )
        {
                super.onConfigurationChanged( newConfig );
        }

        @Override
        public boolean onContextItemSelected( MenuItem item )
        {
                super.onContextItemSelected( item );
                if ( item.getItemId() == SELECT_IMAGE )
                {
                        Intent intent = new Intent();
                        intent.setAction( Intent.ACTION_PICK );
                        intent.setType( "image/*" );
                        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                        startActivityForResult( Intent.createChooser( intent, getString( R.string.selectPhoto ) ), SELECT_IMAGE );
                        return true;
                }
                else if ( item.getItemId() == SELECT_VIDEO )
                {
                        Intent intent = new Intent();
                        intent.setAction( Intent.ACTION_PICK );
                        intent.setType( "video/*" );
                        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                        startActivityForResult( Intent.createChooser( intent, getString( R.string.selectVideo ) ), SELECT_VIDEO );
                        return true;
                }
                else if ( item.getItemId() == SELECT_CAMERA )
                {
                        Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
                        startActivityForResult( intent, SELECT_CAMERA );
                }
                else if ( item.getItemId() == SELECT_FILE )
                {
                        Intent intent = new Intent( getApplicationContext(), kr.co.ultari.atsmart.basic.view.FileBrowser.class );
                        startActivityForResult( intent, SELECT_FILE );
                        return true;
                }
                else if ( item.getItemId() == RE_SEND )
                {
                        AdapterView.AdapterContextMenuInfo info;
                        try
                        {
                                info = ( AdapterView.AdapterContextMenuInfo ) item.getMenuInfo();
                                if ( info != null )
                                {
                                        ChatData nowChatData = ItemList.getItem( info.position );
                                        if ( nowChatData.talkContent.indexOf( "FILE://" ) == 0 )
                                        {
                                                String filePath = nowChatData.talkContent.substring( 7 );
                                                sendFile( filePath );
                                        }
                                        else
                                        {
                                                chatInput.setText( nowChatData.talkContent );
                                                sendChat();
                                        }
                                }
                                else
                                {
                                        TRACE( "RED CLICK, RESEND null" );
                                }
                        }
                        catch ( ClassCastException e )
                        {
                                EXCEPTION( e );
                                return false;
                        }
                        return true;
                }
                else if ( item.getItemId() == DELETE_CHAT )
                {
                        AdapterView.AdapterContextMenuInfo info;
                        try
                        {
                                info = ( AdapterView.AdapterContextMenuInfo ) item.getMenuInfo();
                        }
                        catch ( ClassCastException e )
                        {
                                EXCEPTION( e );
                                return false;
                        }
                        if ( info != null ) deleteChat( info.position );
                }
                else if ( item.getItemId() == COPY_TEXT )
                {
                        AdapterView.AdapterContextMenuInfo info;
                        try
                        {
                                info = ( AdapterView.AdapterContextMenuInfo ) item.getMenuInfo();
                        }
                        catch ( ClassCastException e )
                        {
                                EXCEPTION( e );
                                return false;
                        }
                        if ( info != null )
                        {
                                ChatData nowChatData = ItemList.getItem( info.position );
                                copyToClipBoard( nowChatData.talkContent );
                        }
                        else
                        {
                                TRACE( "RED CLICK COPY null" );
                        }
                }
                else if ( item.getItemId() == COPY_GALLERY )
                {
                        AdapterView.AdapterContextMenuInfo info;
                        try
                        {
                                info = ( AdapterView.AdapterContextMenuInfo ) item.getMenuInfo();
                        }
                        catch ( ClassCastException e )
                        {
                                EXCEPTION( e );
                                return false;
                        }
                        ChatData nowChatData = ItemList.getItem( info.position );
                        File originalFile = new File( getFilesDir(), nowChatData.msgId
                                        + nowChatData.talkContent.substring( nowChatData.talkContent.lastIndexOf( '.' ) ) );
                        if ( !originalFile.exists() ) return false;
                        String atTalkGalleryFolder = Environment.getExternalStorageDirectory() + "/DCIM/Camera/weVoipTalk";
                        File f = new File( atTalkGalleryFolder );
                        if ( !f.exists() )
                        {
                                f.mkdirs();
                                Intent mediaScan = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE );
                                mediaScan.setData( Uri.fromFile( f ) );
                                sendBroadcast( mediaScan );
                        }
                        FileInputStream fi = null;
                        FileOutputStream fo = null;
                        try
                        {
                                fi = new FileInputStream( originalFile );
                                fo = new FileOutputStream( atTalkGalleryFolder + File.separator + originalFile.getName(), false );
                                byte[] buffer = new byte[4096];
                                int rcv = 0;
                                while ( (rcv = fi.read( buffer, 0, 4096 )) >= 0 )
                                {
                                        fo.write( buffer, 0, rcv );
                                }
                                fo.flush();
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                                return false;
                        }
                        finally
                        {
                                if ( fi != null )
                                {
                                        try
                                        {
                                                fi.close();
                                                fi = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                                if ( fo != null )
                                {
                                        try
                                        {
                                                fo.close();
                                                fi = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                        }
                        File updatedFile = new File( atTalkGalleryFolder + File.separator + originalFile.getName() );
                        Intent mediaScan = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE );
                        mediaScan.setData( Uri.fromFile( updatedFile ) );
                        sendBroadcast( mediaScan );
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                        text.setText( getString( R.string.saveGalleryResult ) );
                        text.setTypeface( Define.tfRegular );
                        Toast toast = new Toast( getApplicationContext() );
                        toast.setGravity( Gravity.CENTER, 0, 0 );
                        toast.setDuration( Toast.LENGTH_SHORT );
                        toast.setView( layout );
                        toast.show();
                }
                return false;
        }

        // Copy EditCopy text to the ClipBoard
        @SuppressWarnings( "deprecation" )
        private void copyToClipBoard( String str )
        {
                int sdk = android.os.Build.VERSION.SDK_INT;
                if ( sdk < android.os.Build.VERSION_CODES.HONEYCOMB )
                {
                        android.text.ClipboardManager clipboard = ( android.text.ClipboardManager ) getSystemService( Context.CLIPBOARD_SERVICE );
                        clipboard.setText( str );
                }
                else
                {
                        android.content.ClipboardManager clipboard = ( android.content.ClipboardManager ) getSystemService( Context.CLIPBOARD_SERVICE );
                        android.content.ClipData clip = android.content.ClipData.newPlainText( TAG, str );
                        clipboard.setPrimaryClip( clip );
                }
        }

        public void deleteChat( int position )
        {
                try
                {
                        ChatData chatData = ItemList.getItem( position );
                        String ext = chatData.talkContent.substring( chatData.talkContent.lastIndexOf( '.' ) + 1 );
                        Log.e( "test", "delete:" + ext );
                        if ( chatData.talkContent.indexOf( "ATTACH://" ) >= 0
                                        && (ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" )
                                                        || ext.equalsIgnoreCase( "png" ) || ext.equalsIgnoreCase( "bmp" )) )
                        {
                                // File previewFile = new File(getFilesDir(),
                                // "small_" + chatData.msgId);
                                File previewFile = new File( getFilesDir(), "small_" + chatData.msgId
                                                + chatData.talkContent.substring( chatData.talkContent.lastIndexOf( '.' ) ) );
                                if ( previewFile.exists() )
                                {
                                        Log.e( "test", "prev exist :" + previewFile.getPath() );
                                        previewFile.delete();
                                        previewFile = null;
                                }
                                else Log.e( "test", "prev exist null" );
                                File originalFile = new File( getFilesDir(), chatData.msgId
                                                + chatData.talkContent.substring( chatData.talkContent.lastIndexOf( '.' ) ) );
                                if ( originalFile.exists() )
                                {
                                        Log.e( "test", "original exist:" + originalFile.getPath() );
                                        originalFile.delete();
                                        originalFile = null;
                                }
                                else Log.e( "test", "ori exist null" );
                        }
                        Database.instance( getApplicationContext() ).deleteChatBysChatId( chatData.msgId );
                        ItemList.remove( chatData );
                        ItemList.notifyDataSetChanged();
                        String lastTime = ItemList.getItem( ItemList.getCount() - 1 ).talkDate;
                        String lastMessage = ItemList.getItem( ItemList.getCount() - 1 ).talkContent;
                        Database.instance( getApplicationContext() ).updateChatRoomInfo( roomId, lastTime, lastMessage, true );
                        TalkView.instance().resetData();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        private void deleteAllChat()
        {
                int size = ItemList.getCount();
                for ( int i = 0; i < size; i++ )
                {
                        ChatData chatData = ItemList.getItem( i );
                        String ext = chatData.talkContent.substring( chatData.talkContent.lastIndexOf( '.' ) + 1 );
                        if ( chatData.talkContent.indexOf( "ATTACH://" ) >= 0
                                        && (ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" )
                                                        || ext.equalsIgnoreCase( "png" ) || ext.equalsIgnoreCase( "bmp" )) )
                        {
                                // File previewFile = new File(getFilesDir(),
                                // "small_" + chatData.msgId);
                                File previewFile = new File( getFilesDir(), "small_" + chatData.msgId
                                                + chatData.talkContent.substring( chatData.talkContent.lastIndexOf( '.' ) ) );
                                if ( previewFile.exists() )
                                {
                                        previewFile.delete();
                                        previewFile = null;
                                }
                                File originalFile = new File( getFilesDir(), chatData.msgId
                                                + chatData.talkContent.substring( chatData.talkContent.lastIndexOf( '.' ) ) );
                                if ( originalFile.exists() )
                                {
                                        originalFile.delete();
                                        originalFile = null;
                                }
                        }
                }
        }
        public Handler alertHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_CONFIRM_YES )
                                {
                                        sendChat( "[ROOM_OUT]" );
                                        Database.instance( getApplicationContext() ).deleteChatBysRoomId( roomId );
                                        Database.instance( getApplicationContext() ).deleteChatRoomById( roomId );
                                        TalkView.instance().resetData();
                                        deleteAllChat();
                                        finish();
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        };

        @Override
        public void onClick( View view )
        {
                if ( view != btnEmoticon )
                {
                        emoticonLayout.setVisibility( View.GONE );
                        isEmoticonMode = false;
                }
                if ( view == btnSend )
                {
                        sendChat();
                }
                else if ( view == btnAddFile )
                {
                        openContextMenu( btnAddFile );
                }
                else if ( view == btnEmoticon )
                {
                        hideKeyboard();
                        if ( emoticonLayout.isShown() )
                        {
                                emoticonLayout.setVisibility( View.GONE );
                                isEmoticonMode = false;
                        }
                        else
                        {
                                isEmoticonMode = true;
                                emoticonLayout.setVisibility( View.VISIBLE );
                        }
                }
                else if ( view == btnOption )
                {
                        isOptionMode = !isOptionMode;
                        if ( isOptionMode ) popupLayout.setVisibility( View.VISIBLE );
                        else popupLayout.setVisibility( View.GONE );
                        // mDialog = new ChatOptionDialog();
                        // mDialog.show(getFragmentManager(), "TAG");
                }
                else if ( view == btnClose )
                {
                        hideKeyboard();
                        isSearchMode = false;
                        finish();
                }
                else if ( view == btnSearchUp )
                {
                        searchUp();
                }
                else if ( view == btnSearchDown )
                {
                        searchDown();
                }
                else if ( view == btnSearchDelete )
                {
                        searchInput.setText( "" );
                        searchInput.setBackgroundResource(R.drawable.talk_search_bg_selected);
                        btnSearchDelete.setVisibility( View.GONE );
                }
        }

        private void sendMailChats()
        {
                java.io.FileWriter fw = null;
                File targetFile = null;
                String subJect = null, writeDate = null;
                try
                {
                        targetFile = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), "AtTalkChats.txt" );
                        if ( targetFile.exists() )
                        {
                                targetFile.delete();
                        }
                        // 2015-06-09
                        subJect = Define.getMyName() + ", " + StringUtil.getChatRoomName( userNames, userIds ) + " (" + StringUtil.getChatRoomCount( userNames )
                                        + getString( R.string.people ) + ") " + getString( R.string.chat );
                        writeDate = getString( R.string.createDate ) + new java.util.Date();
                        fw = new java.io.FileWriter( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ) + File.separator
                                        + "AtTalkChats.txt", true );
                        fw.write( subJect + "\n" + writeDate + "\n" );
                        fw.flush();
                        ArrayList<ArrayList<String>> array = Database.instance( getApplicationContext() ).selectChatContent( roomId );
                        for ( int i = 0; i < array.size(); i++ )
                        {
                                ArrayList<String> ar = array.get( i );
                                // 3:name , 5:time , 6:content
                                if ( ar.get( 6 ).indexOf( "ATTACH://" ) >= 0 ) fw.write( "[" + StringUtil.getYMD( getApplicationContext(), ar.get( 5 ) ) + " "
                                                + StringUtil.getTimeStr( ar.get( 5 ) ) + "] " + ar.get( 3 ) + " : "
                                                + ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( ":" ) + 1 ) + "\n" );
                                else if ( ar.get( 6 ).indexOf( "FILE://" ) >= 0 ) fw.write( "[" + StringUtil.getYMD( getApplicationContext(), ar.get( 5 ) )
                                                + " " + StringUtil.getTimeStr( ar.get( 5 ) ) + "] " + ar.get( 3 ) + " : "
                                                + ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( "/" ) + 1 ) + "\n" );
                                else fw.write( "[" + StringUtil.getYMD( getApplicationContext(), ar.get( 5 ) ) + " " + StringUtil.getTimeStr( ar.get( 5 ) )
                                                + "] " + ar.get( 3 ) + " : " + ar.get( 6 ) + "\n" );
                        }
                        fw.flush();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
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
                File resultFile = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), "AtTalkChats.txt" );
                if ( !resultFile.exists() ) return;
                final Uri fileUri = Uri.fromFile( resultFile );
                Intent mail = new Intent( Intent.ACTION_SEND );
                mail.setType( "plain/text" );
                mail.putExtra( Intent.EXTRA_SUBJECT, subJect );
                mail.putExtra( Intent.EXTRA_TEXT, getString( R.string.chatOptionText ) );
                mail.putExtra( Intent.EXTRA_STREAM, fileUri );
                startActivity( mail );
        }

        public void resendChat( String msg )
        {
                try
                {
                        CharSequence chatMessage = msg;
                        if ( chatMessage.toString().equals( "" ) ) return;
                        String dateTime = StringUtil.getNowDateTime() + "";
                        String msgId = Database.instance( getApplicationContext() ).insertChatContent(
                                        Define.getMyId( getApplicationContext() ) + "_" + dateTime, roomId, Define.getMyId( getApplicationContext() ),
                                        Define.getMyName(), Define.getMyNickName(), dateTime, chatMessage.toString(), userIds, false, true );
                        sendBroadcastChat( msgId, roomId, userIds, userNames, chatMessage.toString() );
                        // String msgId, String roomId, String talkerId, String talkerName, String talkerNickName, String talkDate, String talkContent, int
                        // percent, boolean sendComplete, String unreadUserIds
                        insertData( msgId, roomId, Define.getMyId( getApplicationContext() ), Define.getMyName(), Define.getMyNickName(), dateTime,
                                        chatMessage.toString(), 0, false, userIds );
                        Database.instance( getApplicationContext() ).updateChatRoomInfo( roomId, dateTime, chatMessage.toString(), true );
                        TalkView.instance().resetData();
                        chatInput.setText( "" );
                        if ( Define.keyboard.equals( "ON" ) ) hideKeyboard();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public boolean sendChat()
        {
                CharSequence chatMessage = chatInput.getText();
                return sendChat( chatMessage );
        }

        public boolean sendChat( CharSequence chatMessage )
        {
                try
                {
                        if ( chatMessage.toString().equals( "" ) ) return false;
                        String dateTime = StringUtil.getNowDateTime() + "";
                        String msgId = Define.getMyId( getApplicationContext() ) + "_" + dateTime;
                        if ( !chatMessage.toString().equals( "[ROOM_OUT]" ) ) msgId = Database.instance( getApplicationContext() ).insertChatContent(
                                        Define.getMyId( getApplicationContext() ) + "_" + dateTime, roomId, Define.getMyId( getApplicationContext() ),
                                        Define.getMyName(), Define.getMyNickName(), dateTime, chatMessage.toString(), userIds, false, true );
                        if ( chatMessage.toString().equals( "[ROOM_OUT]" ) )
                        {
                                if ( StringUtil.getChatRoomCount( userNames ) > 2 ) sendBroadcastChat( msgId, roomId, userIds, userNames,
                                                chatMessage.toString() );
                        }
                        else sendBroadcastChat( msgId, roomId, userIds, userNames, chatMessage.toString() );
                        insertData( msgId, roomId, Define.getMyId( getApplicationContext() ), Define.getMyName(), Define.getMyNickName(), dateTime,
                                        chatMessage.toString(), 0, false, userIds );
                        // insertData();
                        Database.instance( getApplicationContext() ).updateChatRoomInfo( roomId, dateTime, chatMessage.toString(), true );
                        TalkView.instance().resetData();
                        chatInput.setText( "" );
                        if ( Define.keyboard.equals( "ON" ) ) hideKeyboard();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return true;
        }

        public void sendBroadcastChat( String msgId, String roomId, String userIds, String userNames, String talk )
        {
                try
                {
                        StringBuffer message = new StringBuffer();
                        message.append( Define.getMyId( MainActivity.context ) );
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
                        Intent sendIntent = new Intent( Define.MSG_NEW_CHAT );
                        sendIntent.putExtra( "MESSAGE", message.toString() );
                        sendIntent.putExtra( "MESSAGEID", msgId.toString() );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        getApplicationContext().sendBroadcast( sendIntent );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        //소프트키보드 엔터키시 채팅 자동전송
        /*@Override
        public boolean dispatchKeyEvent( KeyEvent event )
        {
                boolean isResult = false;
                switch ( event.getKeyCode() )
                {
                        case KeyEvent.KEYCODE_ENTER :
                                switch ( event.getAction() )
                                {
                                case KeyEvent.ACTION_DOWN :
                                        if ( !sendChat() ) return false;
                                        break;
                                }
                                isResult = true;
                                break;
                        default :
                                isResult = super.dispatchKeyEvent( event );
                                break;
                }
                return isResult;
        }*/

        private int getChatCount()
        {
                int cnt = 0;
                for ( int i = 0; i < ItemData.size(); i++ )
                {
                        if ( ItemData.get( i ).talkerId != null && !ItemData.get( i ).talkerId.equals( "system" ) ) cnt++;
                }
                return cnt;
        }

        // 2015-09-05
        public int resetChatData()
        {
                Log.d( TAG, "resetChatData : " + ++refreshCount );
                // ArrayList<ArrayList<String>> chatArr = Database.instance(getApplicationContext()).selectChatContent(roomId);
                ArrayList<ArrayList<String>> chatArr = Database.instance( getApplicationContext() ).selectChatContentOrder( roomId, getChatCount() );
                if ( chatArr.size() == 0 )
                {
                        isReachFirst = true;
                        return 0;
                }
                for ( int i = 0; i < chatArr.size(); i++ )
                {
                        ArrayList<String> ar = chatArr.get( i );
                        if ( ar != null )
                        {
                                /*
                                 * boolean isExist = false;
                                 * for(int j = 0; j < ItemList.getCount() ; j++ )
                                 * {
                                 * if ( ItemList.getItem( j ).talkDate.equals( ar.get( 5 ) ) && ItemList.getItem( j ).talkContent.equals( ar.get( 6 ) ) )
                                 * {
                                 * //Log.d( "CW", "ItemList talkDate:"+ItemList.getItem( j ).talkDate + ", content:"+ar.get( 6 ));
                                 * isExist = true;
                                 * }
                                 * }
                                 * if(isExist) continue;
                                 */
                                if ( ar.get( 7 ).equals( "Y" ) )
                                {
                                        if ( ar.get( 9 ) == null )
                                        {
                                                ItemData.add( 0,
                                                                new ChatData( ar.get( 0 ), roomId, ar.get( 2 ), ar.get( 3 ), ar.get( 4 ), ar.get( 5 ), ar
                                                                                .get( 6 ), 0, true, ar.get( 8 ) ) );
                                        }
                                        else
                                        {
                                                if ( ar.get( 9 ).equals( "Y" ) ) ItemData.add( 0, new ChatData( ar.get( 0 ), roomId, ar.get( 2 ), ar.get( 3 ),
                                                                ar.get( 4 ), ar.get( 5 ), ar.get( 6 ), 0, true, ar.get( 8 ) ) );
                                                else ItemData.add(
                                                                0,
                                                                new ChatData( ar.get( 0 ), roomId, ar.get( 2 ), ar.get( 3 ), ar.get( 4 ), ar.get( 5 ), ar
                                                                                .get( 6 ), 0, true, ar.get( 8 ) ) );
                                        }
                                }
                                else ItemData.add( 0, new ChatData( ar.get( 0 ), roomId, ar.get( 2 ), ar.get( 3 ), ar.get( 4 ), ar.get( 5 ), ar.get( 6 ), 0,
                                                false, ar.get( 8 ) ) );
                        }
                }
                checkDate();
                ItemList.notifyDataSetChanged();
                return chatArr.size();
        }

        //
        // 2015-09-05
        private void checkDate()
        {
                removeAllDateItem();
                String oldDate = "00000000";
                String newDate = "";
                for ( int i = 0; i < ItemData.size(); i++ )
                {
                        newDate = ItemData.get( i ).talkDate.substring( 0, 8 );
                        if ( !newDate.equals( oldDate ) )
                        {
                                addDate( i, newDate );
                                i++;
                        }
                        oldDate = newDate;
                }
        }

        private void removeAllDateItem()
        {
                for ( int i = (ItemData.size() - 1); i >= 0; i-- )
                {
                        if ( ItemData.get( i ).talkerId != null && ItemData.get( i ).talkerId.equals( "system" ) ) ItemData.remove( i );
                }
        }

        private void addDate( int i, String newDate )
        {
                ItemData.add( i, new ChatData( newDate.substring( 0, 8 ), roomId, "system", "", "", newDate, newDate.substring( 0, 4 )
                                + getString( R.string.year ) + newDate.substring( 4, 6 ) + getString( R.string.month ) + newDate.substring( 6, 8 )
                                + getString( R.string.day ), 0, true, "" ) );
        }

        //
        // 2015-09-05
        /*
         * public void resetChatData() //ChatSelectRowsPerOneTime
         * {
         * ArrayList<ArrayList<String>> sar = Database.instance(getApplicationContext()).selectChatContentOrder(roomId, ItemList.getTalkCount());
         * Log.d("GetChatCount", "Offset : " + ItemList.getTalkCount() + ", " + sar.size());
         * if ( sar.size() == 1 )
         * {
         * ArrayList<String> ar = sar.get(0);
         * if (ar.get(7).equals("Y"))
         * {
         * if (ar.get(9) == null)
         * ItemList.insert(new ChatData(ar.get(0), roomId, ar.get(2), ar.get(3), ar.get(4), ar.get(5), ar.get(6), 0, true, ar.get(8)), 0);
         * else
         * {
         * if (ar.get(9).equals("Y"))
         * ItemList.insert(new ChatData(ar.get(0), roomId, ar.get(2), ar.get(3), ar.get(4), ar.get(5), ar.get(6), 0, true, ar.get(8)), 0);
         * else
         * ItemList.insert(new ChatData(ar.get(0), roomId, ar.get(2), ar.get(3), ar.get(4), ar.get(5), ar.get(6), 0, true, ar.get(8)), 0);
         * }
         * }
         * else
         * ItemList.insert(new ChatData(ar.get(0), roomId, ar.get(2), ar.get(3), ar.get(4), ar.get(5), ar.get(6), 0, false, ar.get(8)), 0);
         * checkDate("00000000", ar.get(5), false);
         * }
         * else
         * {
         * for (int i = 0 ; i < ( sar.size() - 1 ) ; i++ )
         * {
         * ArrayList<String> ar = sar.get(i);
         * if (ar != null)
         * {
         * boolean isExist = false;
         * for (int j = 0; j < ItemList.getCount(); j++)
         * {
         * if (ItemList.getItem(j).talkDate.equals(ar.get(5)) && ItemList.getItem(j).talkContent.equals(ar.get(6)))
         * isExist = true;
         * }
         * if (isExist)
         * continue;
         * if (ar.get(7).equals("Y"))
         * {
         * if (ar.get(9) == null)
         * ItemList.insert(new ChatData(ar.get(0), roomId, ar.get(2), ar.get(3), ar.get(4), ar.get(5), ar.get(6), 0, true, ar.get(8)), 0);
         * else
         * {
         * if (ar.get(9).equals("Y"))
         * ItemList.insert(new ChatData(ar.get(0), roomId, ar.get(2), ar.get(3), ar.get(4), ar.get(5), ar.get(6), 0, true, ar.get(8)), 0);
         * else
         * ItemList.insert(new ChatData(ar.get(0), roomId, ar.get(2), ar.get(3), ar.get(4), ar.get(5), ar.get(6), 0, true, ar.get(8)), 0);
         * }
         * }
         * else
         * ItemList.insert(new ChatData(ar.get(0), roomId, ar.get(2), ar.get(3), ar.get(4), ar.get(5), ar.get(6), 0, false, ar.get(8)), 0);
         * // 그 다음것과 비교해서 날짜가 다르면 삽입
         * ArrayList<String> bar = sar.get(i + 1); // 이게 이전 데이터
         * checkDate(bar.get(5), ar.get(5), false);
         * }
         * }
         * }
         * ItemList.notifyDataSetChanged();
         * }
         */
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
        public boolean onKeyDown( int keyCode, KeyEvent event )
        {
                if ( event.getAction() == KeyEvent.ACTION_DOWN )
                {
                        if ( keyCode == KeyEvent.KEYCODE_BACK )
                        {
                                if ( isSearchMode )
                                {
                                        tableRow.setVisibility( View.VISIBLE );
                                        searchTableRow.setVisibility( View.GONE );
                                        bottomBar.setVisibility( View.VISIBLE );
                                        emoticonLayout.setVisibility( View.GONE );
                                        isSearchMode = false;
                                        for ( int i = 0; i < ItemList.getCount(); i++ )
                                        {
                                                if ( ItemList.getItem( i ).view != null )
                                                {
                                                        //2016-03-31
                                                        if ( ItemList.getItem( i ).talkerId.equals( Define.getMyId( getApplicationContext() ) ) )
                                                        {
                                                                TextView tvContentsSentence = ( TextView ) ItemList.getItem( i ).view.findViewById( R.id.right_bubble_content );
                                                                if ( tvContentsSentence != null )
                                                                        tvContentsSentence.setTextColor( Color.rgb( 33, 33, 33 ) );
                                                        }
                                                        else
                                                        {
                                                                TextView tvContentsSentence = ( TextView ) ItemList.getItem( i ).view.findViewById( R.id.left_bubble_content );
                                                                if ( tvContentsSentence != null )
                                                                        tvContentsSentence.setTextColor( Color.rgb( 33, 33, 33 ) );
                                                        }
                                                }
                                        }
                                        resultCount.setText( "" );
                                        searchInput.setText( "" );
                                        return true;
                                }
                                else if ( isEmoticonMode )
                                {
                                        emoticonLayout.setVisibility( View.GONE );
                                        isEmoticonMode = false;
                                        return true;
                                }
                                else if ( isOptionMode )
                                {
                                        popupLayout.setVisibility( View.GONE );
                                        isOptionMode = false;
                                        return true;
                                }
                        }
                }
                return super.onKeyDown( keyCode, event );
        }

        private void searchDown()
        {
                if ( nowSelectSearchResult > 0 )
                {
                        nowSelectSearchResult--;
                        Log.d( "SearchDown", "Next is " + nowSelectSearchResult + "/" + searchResult.size() );
                        setChatSelection( searchResult.get( nowSelectSearchResult ) );
                }
                else
                {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                        text.setText( getString( R.string.noResultsFound ) );
                        text.setTypeface( Define.tfRegular );
                        Toast toast = new Toast( getApplicationContext() );
                        toast.setGravity( Gravity.CENTER, 0, 0 );
                        toast.setDuration( Toast.LENGTH_SHORT );
                        toast.setView( layout );
                        toast.show();
                        return;
                }
        }

        private void searchUp()
        {
                if ( nowSelectSearchResult < (searchResult.size() - 1) )
                {
                        nowSelectSearchResult++;
                        Log.d( "SearchUp", "Next is " + nowSelectSearchResult + "/" + searchResult.size() );
                        setChatSelection( searchResult.get( nowSelectSearchResult ) );
                }
                else
                {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                        text.setText( getString( R.string.noResultsFound ) );
                        text.setTypeface( Define.tfRegular );
                        Toast toast = new Toast( getApplicationContext() );
                        toast.setGravity( Gravity.CENTER, 0, 0 );
                        toast.setDuration( Toast.LENGTH_SHORT );
                        toast.setView( layout );
                        toast.show();
                        return;
                }
        }

        private void searchData()
        {
                hideKeyboard();
                
                String searchText = searchInput.getText().toString();
                ArrayList<ArrayList<String>> result = Database.instance( getApplicationContext() ).searchChat( roomId, searchText );
                searchResult.clear();
                nowSelectSearchResult = 0;
                for ( int i = 0; i < result.size(); i++ )
                {
                        searchResult.add( result.get( i ).get( 5 ) );
                        Log.d( TAG, "SearchDate added : " + result.get( i ).get( 5 ) );
                }
                if ( searchResult.size() > 0 )
                {
                        resultCount.setText( getString( R.string.result ) + searchResult.size() + getString( R.string.count ) );
                        btnSearchUp.setEnabled( true );
                        btnSearchDown.setEnabled( true );
                        setChatSelection( searchResult.get( 0 ) );
                        nowSelectSearchResult = 0;
                }
                else
                {
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                        text.setText( getString( R.string.noResultsFound ) );
                        text.setTypeface( Define.tfRegular );
                        Toast toast = new Toast( getApplicationContext() );
                        toast.setGravity( Gravity.CENTER, 0, 0 );
                        toast.setDuration( Toast.LENGTH_SHORT );
                        toast.setView( layout );
                        toast.show();
                        resultCount.setText( getString( R.string.noSearchData ) );
                }
        }

        private void setChatSelection( String talkDate )
        {
                Log.d( TAG, "setChatSelection : " + talkDate );
                int index = -1;
                for ( int i = (ItemData.size() - 1); i >= 0; i-- )
                {
                        if ( ItemData.get( i ).talkDate.equals( talkDate ) )
                        {
                                index = i;
                                break;
                        }
                }
                if ( index >= 0 )
                {
                        chatOutput.setSelection( index );
                }
                else
                {
                        ArrayList<ArrayList<String>> chatArr = Database.instance( getApplicationContext() ).selectChatContentOrder( roomId, getChatCount(),
                                        talkDate );
                        if ( chatArr.size() == 0 )
                        {
                                return;
                        }
                        for ( int i = 0; i < chatArr.size(); i++ )
                        {
                                ArrayList<String> ar = chatArr.get( i );
                                if ( ar != null )
                                {
                                        if ( ar.get( 7 ).equals( "Y" ) )
                                        {
                                                if ( ar.get( 9 ) == null )
                                                {
                                                        ItemData.add( 0, new ChatData( ar.get( 0 ), roomId, ar.get( 2 ), ar.get( 3 ), ar.get( 4 ), ar.get( 5 ),
                                                                        ar.get( 6 ), 0, true, ar.get( 8 ) ) );
                                                }
                                                else
                                                {
                                                        if ( ar.get( 9 ).equals( "Y" ) ) ItemData.add( 0,
                                                                        new ChatData( ar.get( 0 ), roomId, ar.get( 2 ), ar.get( 3 ), ar.get( 4 ), ar.get( 5 ),
                                                                                        ar.get( 6 ), 0, true, ar.get( 8 ) ) );
                                                        else ItemData.add( 0,
                                                                        new ChatData( ar.get( 0 ), roomId, ar.get( 2 ), ar.get( 3 ), ar.get( 4 ), ar.get( 5 ),
                                                                                        ar.get( 6 ), 0, true, ar.get( 8 ) ) );
                                                }
                                        }
                                        else ItemData.add( 0, new ChatData( ar.get( 0 ), roomId, ar.get( 2 ), ar.get( 3 ), ar.get( 4 ), ar.get( 5 ),
                                                        ar.get( 6 ), 0, false, ar.get( 8 ) ) );
                                }
                        }
                        checkDate();
                        ItemList.notifyDataSetChanged();
                        chatOutput.setSelection( 1 );
                }
        }

        @Override
        public void onPause()
        {
                super.onPause();
        }

        @Override
        protected void onResume()
        {
                Define.mContext = this;
                Define.isHomeMode = false;
                super.onResume();
        };

        @Override
        protected void onUserLeaveHint()
        {
                Define.isHomeMode = true;
                super.onUserLeaveHint();
        }
        public class ChatOptionDialog extends DialogFragment {
                @Override
                public Dialog onCreateDialog( Bundle savedInstanceState )
                {
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder( getActivity() );
                        LayoutInflater mLayoutInflater = getActivity().getLayoutInflater();
                        mBuilder.setView( mLayoutInflater.inflate( R.layout.chat_option_dialog, null ) );
                        return mBuilder.create();
                }

                @Override
                public void onResume()
                {
                        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
                        params.width = Define.displayWidth / 2;
                        params.height = LayoutParams.MATCH_PARENT;
                        getDialog().getWindow().setAttributes( ( android.view.WindowManager.LayoutParams ) params );
                        getWindow().setBackgroundDrawable( new PaintDrawable( Color.TRANSPARENT ) );
                        Window window = getDialog().getWindow();
                        // window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        WindowManager.LayoutParams wlp = window.getAttributes();
                        wlp.x = Define.displayWidth - 300;
                        wlp.y = 100;
                        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                        window.setAttributes( wlp );
                        super.onResume();
                }

                @Override
                public void onStop()
                {
                        super.onStop();
                }
        }

        public void ONCLICK_DIALOG( View v )
        {
                switch ( v.getId() )
                {
                        case R.id.popupchat_chat_search: //2016-03-31
                                tableRow.setVisibility( View.GONE );
                                searchTableRow.setVisibility( View.VISIBLE );
                                bottomBar.setVisibility( View.GONE );
                                emoticonLayout.setVisibility( View.GONE );
                                isSearchMode = true;
                                btnSearchUp.setEnabled( false );
                                btnSearchDown.setEnabled( false );
                                hideKeyboard();
                                break;
                        /*case R.id.popupchat_mail :
                                sendMailChats();
                                break;*/
                        case R.id.popupchat_chat_album: //2016-03-31
                                Intent it = new Intent( getApplicationContext(), kr.co.ultari.atsmart.basic.subview.ImageSwitcherView.class );
                                it.putExtra( "RoomId", roomId );
                                startActivity( it );
                                break;
                        case R.id.popupchat_chat_userlist: //2016-03-31
                                Intent popup = new Intent( getApplicationContext(), kr.co.ultari.atsmart.basic.util.CustomDialog.class );
                                popup.putExtra( "RoomId", roomId );
                                popup.putExtra( "UserIds", userIds );
                                popup.putExtra( "UserNames", userNames );
                                popup.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( popup );
                                finish();
                                break;
                        case R.id.popupchat_chat_adduser: //2016-03-31
                                hideKeyboard();
                                Define.oldRoomUserId = userIds;
                                Define.oldRoomUserName = userNames;
                                Define.oldRoomId = roomId;
                                Bundle bundle = new Bundle();
                                bundle.putString( "type", "chat" );
                                bundle.putString( "userIds", userIds );
                                bundle.putString( "userNames", userNames );
                                Intent intent = new Intent( getBaseContext(), kr.co.ultari.atsmart.basic.view.GroupSearchView.class );
                                intent.putExtras( bundle );
                                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( intent );
                                finish();
                                break;
                        case R.id.popupchat_chat_delroom: 
                                hideKeyboard();
                                //2016-03-31
                                CustomDeleteRoomDialog cdd = new CustomDeleteRoomDialog(ChatWindow.this);
                                cdd.show();
                                
                                /*AlertDialog.Builder alertDialog = new AlertDialog.Builder( ChatWindow.this );
                                alertDialog.setTitle( getString( R.string.delRoom ) );
                                alertDialog.setMessage( getString( R.string.delConfirm ) );
                                alertDialog.setIcon( R.drawable.icon );
                                alertDialog.setPositiveButton( getString( R.string.ok ), new DialogInterface.OnClickListener() {
                                        public void onClick( DialogInterface dialog, int which )
                                        {
                                                Message m = alertHandler.obtainMessage( Define.AM_CONFIRM_YES, null );
                                                alertHandler.sendMessage( m );
                                        }
                                } );
                                alertDialog.setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                                        public void onClick( DialogInterface dialog, int which )
                                        {
                                                Message m = alertHandler.obtainMessage( Define.AM_CONFIRM_NO, null );
                                                alertHandler.sendMessage( m );
                                                dialog.cancel();
                                        }
                                } );
                                alertDialog.show();*/
                                break;
                }
                
                isOptionMode = false;
                popupLayout.setVisibility( View.GONE );
                // mDialog.dismiss();
        }

        @Override
        public void onScroll( AbsListView arg0, int arg1, int arg2, int arg3 ) // ChatSelectRowsPerOneTime
        {
        }

        @Override
        public void onScrollStateChanged( AbsListView arg0, int scrollState ) // ChatSelectRowsPerOneTime
        {
                switch ( scrollState )
                {
                case OnScrollListener.SCROLL_STATE_IDLE :
                        if ( chatOutput.getFirstVisiblePosition() == 0 && !isReachFirst && !isSearchMode )
                        {
                                this.resetChatData();
                        }
                        break;
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL :
                        break;
                case OnScrollListener.SCROLL_STATE_FLING :
                        break;
                }
        }
}
