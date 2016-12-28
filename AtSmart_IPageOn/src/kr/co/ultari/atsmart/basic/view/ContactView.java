package kr.co.ultari.atsmart.basic.view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.subdata.SearchContact;
import kr.co.ultari.atsmart.basic.util.FmcSendBroadcast;
import kr.co.ultari.atsmart.basic.util.HangulUtils;
import kr.co.ultari.atsmart.basic.util.ImageUtil;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ContactView extends Fragment implements OnClickListener, OnScrollListener {
        private static final String TAG = "/AtSmart/ContactView";
        private static ContactView contactViewInstance = null;
        public LayoutInflater inflater;
        private View view;
        private ListView lv_contactlist;
        private EditText et_input;
        private Button btn_search, btn_search_delete;
        private ImageView iv_search;  
        private boolean isVisibleEdit;
        private String searchKeyword;
        private ArrayList<Contact> contactArray = null;
        private boolean m_bLoadList = false;
        public boolean isLoadComplete = true;
        ContactsAdapter adapter = null;
        private Context context;
        private String oldName = "";
        private String oldNumber = "";
        public String oldSearchKey = "";
        public String nowTelNumber = "";
        public String nowMobilePhoneNumber = "";

        public static ContactView instance()
        {
                if ( contactViewInstance == null ) contactViewInstance = new ContactView();
                return contactViewInstance;
        }

        @Override
        public void onDestroy()
        {
                super.onDestroy();
                contactViewInstance = null;
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                this.inflater = inflater;
                view = inflater.inflate( R.layout.activity_contactlist, null );
                lv_contactlist = ( ListView ) view.findViewById( R.id.lv_contactlist );
                et_input = ( EditText ) view.findViewById( R.id.contact_search_input );
                et_input.setTypeface( Define.tfRegular );
                
                context = container.getContext();
                lv_contactlist.setOnScrollListener( this );
                contactArray = new ArrayList<Contact>();
                contactArray.clear();
                adapter = new ContactsAdapter( context, R.layout.layout_phonelist, contactArray );
                lv_contactlist.setAdapter( adapter );
                lv_contactlist.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick( AdapterView<?> contactlist, View v, int position, long resid )
                        {
                                Contact contact = ( Contact ) contactlist.getItemAtPosition( position );
                                if ( contact == null ) return;
                                // et_input.setText("");
                                // btn_search_delete.setVisibility(View.GONE);
                                Intent it = new Intent( context, kr.co.ultari.atsmart.basic.subview.ContactDetail.class );
                                it.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                it.putExtra( "contactId", contact.getContactId() );
                                startActivity( it );
                        }
                } );
                try
                {
                        /*
                         * et_input.setOnFocusChangeListener(new
                         * OnFocusChangeListener() {
                         * @Override
                         * public void onFocusChange(View view, boolean bFocus)
                         * {
                         * Log.e( "ContactView", "focusChange:"+bFocus ); } });
                         */
                        et_input.setOnTouchListener( mTouchEvent );
                        et_input.addTextChangedListener( new TextWatcher() {
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
                                                searchKeyword = et_input.getText().toString();
                                                if ( oldSearchKey.equals( searchKeyword ) ) return;
                                                oldSearchKey = searchKeyword;
                                                synchronized ( this )
                                                {
                                                        if ( searchKeyword.length() == 0 && count == 0 )
                                                        {
                                                                btn_search_delete.setVisibility( View.GONE );
                                                                et_input.setMovementMethod( null );
                                                                iv_search.setBackgroundResource( R.drawable.icon_search );  
                                                                //et_input.setBackgroundResource( R.drawable.img_search_contact ); 2016-03-31
                                                                displayListBasic();
                                                        }
                                                        else
                                                        {
                                                                btn_search_delete.setVisibility( View.VISIBLE );
                                                                et_input.setMovementMethod( new ScrollingMovementMethod() );
                                                                et_input.requestFocus();
                                                                et_input.setFocusable( true );
                                                                iv_search.setBackgroundResource( R.drawable.icon_search_selected );  
                                                                //et_input.setBackgroundResource( R.drawable.img_search_contact_pressed ); 2016-03-31
                                                                displayListSearch();
                                                        }
                                                }
                                        }
                                        catch ( Exception e )
                                        {
                                                Define.EXCEPTION( e );
                                        }
                                }
                        } );
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
                btn_search = ( Button ) view.findViewById( R.id.contact_search_btn );
                btn_search.setOnClickListener( this );
                btn_search_delete = ( Button ) view.findViewById( R.id.contact_search_delete );
                btn_search_delete.setOnClickListener( this );
                
                iv_search = ( ImageView ) view.findViewById( R.id.contact_search_icon );
                
                isVisibleEdit = false;
                et_input.setVisibility( View.VISIBLE );
                et_input.setMovementMethod( null );
                et_input.setSelection( et_input.length() );
                return view;
        }

        public void hideKeyboard()
        {
                InputMethodManager imm = ( InputMethodManager ) context.getSystemService( Activity.INPUT_METHOD_SERVICE );
                if ( et_input.getWindowToken() != null )
                {
                        imm.hideSoftInputFromWindow( et_input.getWindowToken(), 0 );
                        et_input.clearFocus();
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
                                et_input.setFocusable( true );
                                et_input.setSelection( et_input.length() );
                                iv_search.setBackgroundResource( R.drawable.icon_search_selected );  
                                //b.setBackgroundResource( R.drawable.img_search_contact_pressed ); 2016-03-31
                        }
                        else if ( action == MotionEvent.ACTION_UP )
                        {}
                        return false;
                }
        };

        public String fPhoneType( String TypeID )
        {
                if ( "1".equals( TypeID ) )
                {
                        return "Home";
                }
                else if ( "2".equals( TypeID ) )
                {
                        return "Mobile";
                }
                return TypeID;
        }

        private void displayListSearch()
        {
                adapter.clear();
                for ( Contact ct : Define.contactArray )
                {
                        if ( ct.getName() != null && StringUtil.startsWith( searchKeyword, ct.getName() ) )
                        {
                                contactArray.add( ct );
                        }
                }
                Message m = ContactViewHandler.obtainMessage( Define.AM_COMPLETE );
                ContactViewHandler.sendMessage( m );
        }

        @SuppressWarnings( "unchecked" )
        public void displayListBasic()
        {
                boolean m_bChanged = false;
                
                try
                {
                        synchronized ( contactArray )
                        {
                                contactArray.clear();
                                contactArray.addAll( Define.contactArray );
                                m_bChanged = true;
                        }
                        adapter.notifyDataSetChanged();
                        if ( m_bChanged )
                        {
                                Message m = ContactViewHandler.obtainMessage( Define.AM_COMPLETE );
                                ContactViewHandler.sendMessage( m );
                        }
                }
                catch(Exception e)
                {
                        e.printStackTrace();
                }
        }

        @Override
        public void onResume()
        {
                super.onResume();
                Message m = ContactViewHandler.obtainMessage( Define.AM_COMPLETE );
                ContactViewHandler.sendMessage( m );
                String searchKeyword = et_input.getText().toString();
                synchronized ( this )
                {
                        if ( searchKeyword.length() == 0 )
                        {
                                btn_search_delete.setVisibility( View.GONE );
                                et_input.setMovementMethod( null );
                                iv_search.setBackgroundResource( R.drawable.icon_search );  
                                //et_input.setBackgroundResource( R.drawable.img_search_contact ); 2016-03-31
                                displayListBasic();
                        }
                        else
                        {
                                btn_search_delete.setVisibility( View.VISIBLE );
                                et_input.setMovementMethod( new ScrollingMovementMethod() );
                                et_input.requestFocus();
                                et_input.setFocusable( true );
                                iv_search.setBackgroundResource( R.drawable.icon_search_selected );  
                                //et_input.setBackgroundResource( R.drawable.img_search_contact_pressed ); 2016-03-31
                                displayListSearch();
                        }
                }
        }
        public Handler ContactViewHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REFRESH )
                                {
                                        adapter.clear();
                                        oldName = "";
                                        oldNumber = "";
                                }
                                else if ( msg.what == Define.AM_ADD_BUDDY_USER )
                                {
                                        Contact data = ( Contact ) msg.obj;
                                        if ( !oldName.equals( data.getName() ) || !oldNumber.equals( data.getPhonenum() ) )
                                        {
                                                adapter.insert( data, adapter.getCount() );
                                                // 2015-05-07
                                                Define.contactArray.add( data );
                                        }
                                        oldName = data.getName();
                                        oldNumber = data.getPhonenum();
                                }
                                else if ( msg.what == Define.AM_COMPLETE )
                                {
                                        if ( lv_contactlist.getCount() < 20 )
                                        {
                                                setIdAndPhoto( 0, lv_contactlist.getCount() - 1 );
                                        }
                                        else
                                        {
                                                int from = 0;
                                                int to = 0;
                                                for ( int i = 0; i < lv_contactlist.getCount(); i++ )
                                                {
                                                        if ( lv_contactlist.getChildAt( i ) != null )
                                                        {
                                                                to = i;
                                                        }
                                                        else
                                                        {
                                                                break;
                                                        }
                                                }
                                                setIdAndPhoto( from, to );
                                        }
                                }
                                else
                                {
                                        super.handleMessage( msg );
                                }
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
                        }
                }
        };
        private class ContactsAdapter extends ArrayAdapter<Contact> implements OnClickListener {
                private int resId;
                private ArrayList<Contact> contactlist;
                private LayoutInflater Inflater;
                private Context context;

                public ContactsAdapter( Context context, int textViewResourceId, List<Contact> objects )
                {
                        super( context, textViewResourceId, objects );
                        this.context = context;
                        resId = textViewResourceId;
                        contactlist = ( ArrayList<Contact> ) objects;
                        Inflater = ( LayoutInflater ) (( Activity ) context).getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                }

                @Override
                public View getView( int position, View v, ViewGroup parent )
                {
                        UserImageView iv_photoid = null;
                        TextView tv_name = null;
                        TextView tv_phonenumber = null;
                        Button btn_chat = null;
                        Button btn_call = null;
                        Button btn_fmc = null;  
                        ImageView iv_line = null;
                        Contact acontact = contactlist.get( position );
                        if ( v == null ) v = Inflater.inflate( resId, null );
                        tv_name = ( TextView ) v.findViewById( R.id.contact_name );
                        tv_phonenumber = ( TextView ) v.findViewById( R.id.contact_phonenumber );
                        iv_photoid = ( kr.co.ultari.atsmart.basic.control.UserImageView ) v.findViewById( R.id.contact_photo );
                        btn_chat = ( Button ) v.findViewById( R.id.contact_btnchat );
                        btn_chat.setTag( acontact );
                        btn_chat.setOnClickListener( this );
                        btn_chat.setFocusable( false );
                        btn_call = ( Button ) v.findViewById( R.id.contact_btncall );
                        btn_call.setTag( acontact );
                        btn_call.setOnClickListener( this );
                        btn_call.setFocusable( false );
                         
                        btn_fmc = ( Button ) v.findViewById( R.id.contact_btnfmc );
                        btn_fmc.setTag( acontact );
                        btn_fmc.setOnClickListener( this );
                        btn_fmc.setFocusable( false );
                        //
                        iv_line = ( ImageView ) v.findViewById( R.id.contact_divide );
                        if ( acontact != null )
                        {
                                tv_name.setTypeface( Define.tfRegular );
                                tv_phonenumber.setTypeface( Define.tfRegular );
                                if ( acontact.userId == null )
                                {
                                        tv_name.setText( acontact.getName() );
                                        //btn_chat.setVisibility( View.GONE ); 2016-03-31
                                }
                                else
                                {
                                        tv_name.setText( acontact.getName() );
                                        btn_chat.setBackgroundResource( R.drawable.icon_chat );
                                        //btn_chat.setVisibility( View.VISIBLE ); 2016-03-31
                                }
                                if ( !acontact.getPhonenum().equals( "" ) ) tv_phonenumber.setText( PhoneNumberUtils.formatNumber( acontact.getPhonenum() ) );
                                else if ( !acontact.getTelnum().equals( "" ) ) tv_phonenumber.setText( PhoneNumberUtils.formatNumber( acontact.getTelnum() ) );
                                else tv_phonenumber.setText( "" );
                                if ( (acontact.getName() != null && acontact.getName().equals( "NoName" ))
                                                || (acontact.getPhonenum() != null && acontact.getPhonenum().equals( "-1" )) )
                                {
                                        btn_chat.setBackgroundResource( R.drawable.icon_chat_none );
                                        btn_call.setBackgroundResource( R.drawable.icon_call_none );
                                        btn_fmc.setBackgroundResource( R.drawable.icon_call_fmc_none );  
                                }
                                
                                Bitmap pic = BitmapFactory.decodeResource( getResources(), R.drawable.img_profile_100x100 );
                                iv_photoid.setImageBitmap( ImageUtil.getDrawOval( pic ) );
                                //iv_photoid.setImageDrawable( getResources().getDrawable( R.drawable.img_profile_100x100 ) );
                        }
                        return v;
                }

                @Override
                public void onClick( View v )
                {
                        Contact data = ( Contact ) v.getTag();
                        if ( v.getId() == R.id.contact_btnchat )
                        {
                                String userId = data.userId;
                                String userName = data.getOrgUserName();
                                if ( userId == null || userName == null ) return;
                                if ( !userId.equalsIgnoreCase( Define.getMyId( context ) ) )
                                {
                                        String oUserIds = userId + "," + Define.getMyId( context );
                                        String userIds = StringUtil.arrange( oUserIds );
                                        String userNames = userName + "," + StringUtil.getNamePosition( Define.getMyName() );
                                        userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                                        String roomId = userIds.replace( ",", "_" );
                                        ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfo( roomId );
                                        if ( array.size() == 0 ) Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames,
                                                        StringUtil.getNowDateTime(), getString( R.string.newRoom ) );
                                        ActionManager.openChat( context, roomId, userIds, userNames );
                                }
                        }
                         
                        else if( v.getId() == R.id.contact_btnfmc)
                        {
                                nowMobilePhoneNumber = data.getPhonenum();
                                nowTelNumber = data.getTelnum();
                                if ( nowMobilePhoneNumber != null && !nowMobilePhoneNumber.equals( "" ) && nowTelNumber != null && !nowTelNumber.equals( "" ) )
                                {
                                        registerForContextMenu( v );
                                        getActivity().openContextMenu( v );
                                }
                                else if ( nowTelNumber != null && !nowTelNumber.equals( "" ) )
                                {
                                        FmcSendBroadcast.FmcSendCall( nowTelNumber, 0, context);
                                }
                                else
                                {
                                        FmcSendBroadcast.FmcSendCall( nowMobilePhoneNumber, 0, context);
                                }
                        }
                        else if ( v.getId() == R.id.contact_btncall )
                        {
                                nowMobilePhoneNumber = data.getPhonenum();
                                nowTelNumber = data.getTelnum();
                                if ( nowMobilePhoneNumber != null && !nowMobilePhoneNumber.equals( "" ) && nowTelNumber != null && !nowTelNumber.equals( "" ) )
                                {
                                        registerForContextMenu( v );
                                        getActivity().openContextMenu( v );
                                }
                                else if ( nowTelNumber != null && !nowTelNumber.equals( "" ) )
                                {
                                        FmcSendBroadcast.FmcSendCall( nowTelNumber, 1, context);
                                }
                                else
                                {
                                        FmcSendBroadcast.FmcSendCall( nowMobilePhoneNumber, 1, context);
                                }
                        }
                        //
                }
        }

        @Override
        public boolean onContextItemSelected( MenuItem item )
        {
                if ( item.getItemId() == 0 )
                {
                        FmcSendBroadcast.FmcSendCall( nowTelNumber ,0, getActivity().getApplicationContext());  
                }
                else if ( item.getItemId() == 1 )
                {
                        FmcSendBroadcast.FmcSendCall( nowMobilePhoneNumber ,0, getActivity().getApplicationContext());  
                }
                else if ( item.getItemId() == 2 )
                {
                        FmcSendBroadcast.FmcSendCall( nowTelNumber ,1, getActivity().getApplicationContext());  
                }
                else if ( item.getItemId() == 3 )
                {
                        FmcSendBroadcast.FmcSendCall( nowMobilePhoneNumber ,1, getActivity().getApplicationContext());  
                }
                
                return super.onContextItemSelected( item );
        }

        @Override
        public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo )
        {
                super.onCreateContextMenu( menu, v, menuInfo );
                
                 
                if ( v.getId() == R.id.contact_btncall )
                {
                        menu.setHeaderTitle( getString( R.string.choicePhoneNumber ) );
                        menu.add( 0, 2, Menu.NONE, PhoneNumberUtils.formatNumber( nowTelNumber ) );
                        menu.add( 0, 3, Menu.NONE, PhoneNumberUtils.formatNumber( nowMobilePhoneNumber ) );
                }
                else if( v.getId() == R.id.contact_btnfmc)
                {
                        menu.setHeaderTitle( getString( R.string.choicePhoneNumber ) );
                        menu.add( 0, 0, Menu.NONE, PhoneNumberUtils.formatNumber( nowTelNumber ) );
                        menu.add( 0, 1, Menu.NONE, PhoneNumberUtils.formatNumber( nowMobilePhoneNumber ) );
                }
                //
        }

        public Drawable getDrawableFromBitmap( Bitmap bitmap )
        {
                Drawable d = new BitmapDrawable( getResources(), bitmap );
                return d;
        }

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == R.id.contact_search_btn )
                {
                        Intent selectWindow = new Intent( context, kr.co.ultari.atsmart.basic.subview.ContactAddView.class );
                        selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                        startActivity( selectWindow );
                }
                else if ( v.getId() == R.id.contact_search_delete )
                {
                        et_input.setText( "" );
                        //et_input.setBackgroundResource( R.drawable.img_search_contact ); 2016-03-31
                        displayListBasic();
                        btn_search_delete.setVisibility( View.GONE );
                        this.hideKeyboard();
                }
        }

        public void onScrollStateChanged( AbsListView view, int scrollState )
        {
                switch ( scrollState )
                {
                case OnScrollListener.SCROLL_STATE_IDLE :
                        setIdAndPhoto();
                        break;
                case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL :
                        break;
                case OnScrollListener.SCROLL_STATE_FLING :
                        break;
                }
        }

        @Override
        public void onScroll( AbsListView arg0, int arg1, int arg2, int arg3 )
        {
        }

        public void setIdAndPhoto()
        {
                setIdAndPhoto( lv_contactlist.getFirstVisiblePosition(), lv_contactlist.getLastVisiblePosition() );
        }

        public void setIdAndPhoto( int from, int to )
        {
                Log.e( "kr.co.ultari.atsmart", "setIdAndPhoto : " + from + " > " + to );
                if ( from > to ) return;
                StringBuffer checkIds = new StringBuffer();
                for ( int i = from; i <= to; i++ )
                {
                        Contact data = ( Contact ) lv_contactlist.getAdapter().getItem( i );
                        if ( data.userId == null ) checkIds.append( "\t" + data.getPhonenum() );
                        else updateSingleItemView( i );
                }
                if ( checkIds.length() > 0 ) new GetUserInfoThread( checkIds.toString(), lv_contactlist.getAdapter() );
        }
        class GetUserInfoThread extends Thread {
                String userIds;
                kr.co.ultari.atsmart.basic.codec.AmCodec codec = null;

                public GetUserInfoThread( String userIds, ListAdapter data )
                {
                        this.userIds = userIds;
                        this.codec = new kr.co.ultari.atsmart.basic.codec.AmCodec();
                        this.start();
                }

                public void run()
                {
                        UltariSSLSocket sc = null;
                        InputStreamReader ir = null;
                        BufferedReader br = null;
                        try
                        {
                                sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                                .getServerPort( Define.mContext ) ) );
                                ir = new InputStreamReader( sc.getInputStream() );
                                br = new BufferedReader( ir );
                                send( "GETID" + userIds, sc );
                                int rcv = 0;
                                char[] buf = new char[2048];
                                StringBuffer sb = new StringBuffer();
                                boolean m_bFinish = false;
                                while ( !m_bFinish && (rcv = br.read( buf, 0, 2047 )) >= 0 )
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
                                                        else nowStr += rcvStr.charAt( i );
                                                }
                                                if ( !process( command, param ) ) m_bFinish = true;
                                        }
                                }
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
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
                                                sc = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                                if ( br != null )
                                {
                                        try
                                        {
                                                br.close();
                                                sc = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                                Message m = phoneNumberHandler.obtainMessage( Define.AM_COMPLETE );
                                phoneNumberHandler.sendMessage( m );
                        }
                }

                public boolean process( String command, ArrayList<String> param )
                {
                        if ( command.equals( "SETIDEND" ) ) return false;
                        if ( !command.equals( "SETID" ) ) return true;
                        Message m = phoneNumberHandler.obtainMessage( Define.AM_LIST_SET_ID, param );
                        phoneNumberHandler.sendMessage( m );
                        return true;
                }

                public void send( String msg, UltariSSLSocket sc ) throws Exception
                {
                        if ( sc == null ) throw new Exception( "Not connected" );
                        msg.replaceAll( "\f", "" );
                        msg = codec.EncryptSEED( msg );
                        msg += "\f";
                        sc.send( msg );
                }
        }
        public Handler phoneNumberHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_LIST_SET_ID )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        for ( int i = lv_contactlist.getFirstVisiblePosition(); i <= lv_contactlist.getLastVisiblePosition(); i++ )
                                        {
                                                Contact data = ( Contact ) lv_contactlist.getItemAtPosition( i );
                                                if ( param.size() > 1 && data.getPhonenum().equals( param.get( 0 ) ) )
                                                {
                                                        data.userId = param.get( 1 );
                                                        if ( param.size() == 3 ) data.userName = param.get( 2 );
                                                }
                                                updateSingleItemView( i );
                                        }
                                }
                                else if ( msg.what == Define.AM_COMPLETE )
                                {
                                        for ( int i = lv_contactlist.getFirstVisiblePosition(); i <= lv_contactlist.getLastVisiblePosition(); i++ )
                                        {
                                                updateSingleItemView( i );
                                        }
                                }
                                else
                                {
                                        super.handleMessage( msg );
                                }
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
                        }
                }
        };

        private void updateSingleItemView( int position )
        {
                if ( position < 0 ) return;
                Contact acontact = contactArray.get( position );
                View v = lv_contactlist.getChildAt( position - lv_contactlist.getFirstVisiblePosition() );
                if ( v == null ) return;
                UserImageView iv_photoid = ( UserImageView ) v.findViewById( R.id.contact_photo );
                Button btn_chat = ( Button ) v.findViewById( R.id.contact_btnchat );
                Bitmap bmp = acontact.getBitmap();
                if ( bmp != null )
                {
                        //iv_photoid.setImageBitmap( bmp );
                        iv_photoid.setImageBitmap( ImageUtil.getDrawOval(bmp)); 
                }
                else if ( acontact.userId != null )
                {
                        iv_photoid.setUserIdOval( acontact.userId, false );
                        //iv_photoid.setUserId( acontact.userId );
                }
                if ( acontact.userId != null )
                {
                        btn_chat.setBackgroundResource( R.drawable.icon_chat );
                        //btn_chat.setVisibility( View.VISIBLE ); 
                }
                /*else
                {
                        btn_chat.setVisibility( View.GONE );
                }*/
                
                /*if ( acontact.userId != null && !acontact.userId.equals( "" ) )
                {
                        iv_photoid.setUserIdOval( acontact.userId, false );
                }
                else
                {
                        if ( bmp != null ) iv_photoid.setImageBitmap( ImageUtil.getDrawOval(acontact.getBitmap()));
                        else 
                        {
                                Bitmap pic = BitmapFactory.decodeResource( getResources(), R.drawable.img_profile_100x100 );
                                iv_photoid.setImageBitmap( ImageUtil.getDrawOval( pic ) );
                        }
                }*/
        }
}
