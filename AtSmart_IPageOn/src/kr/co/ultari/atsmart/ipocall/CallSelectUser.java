package kr.co.ultari.atsmart.ipocall;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.util.ImageUtil;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;

@SuppressLint("HandlerLeak")
public class CallSelectUser extends Activity implements OnClickListener, OnItemClickListener
{
	private Button tabContact;
	private Button tabOrganization;
	private EditText call_search_input;
	private ImageButton call_value_delete;
	private Button call_btn_search;
	private ListView list;
	private ArrayList<SearchData> searchResult;
	private CallSearchResult adapter;
	
	private boolean isContactSearch = false;
	private EditText switching_inputfield;
	
	private boolean onDestroy = false;
	private ProgressDialog m_WaitForSearchProgressDialog = null;
	
	public static CallSelectUser callSelectUser;
	
	private Button searchview_exit;
	private Button searchview_submit;
	
	private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_call_select_user);
		
		callSelectUser = this;
		
		tabContact = (Button)findViewById(R.id.call_tab_contact);
		tabOrganization = (Button)findViewById(R.id.call_tab_organization);
		
		tabContact.setOnClickListener(this);
		tabOrganization.setOnClickListener(this);
		
		call_search_input = (EditText)findViewById(R.id.call_search_input);
		
		setContactSearch();
		
		call_value_delete = (ImageButton)findViewById(R.id.call_value_delete);
		call_value_delete.setOnClickListener(this);
		
		call_btn_search = (Button)findViewById(R.id.call_btn_search);
		call_btn_search.setOnClickListener(this);
		
		list = (ListView)findViewById(R.id.callSearchResult);
		
		searchResult = new ArrayList<SearchData>();
		adapter = new CallSearchResult(this, R.layout.item_call_select_user, searchResult);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(this);
		
		switching_inputfield = (EditText)findViewById(R.id.switching_inputfield);
		
		searchview_exit = (Button)findViewById(R.id.searchview_exit);
		searchview_exit.setOnClickListener(this);
		
		searchview_submit = (Button)findViewById(R.id.searchview_submit);
		searchview_submit.setOnClickListener(this);
		
		loadingContactData();
	}
	
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

	@Override
	public void onClick(View v)
	{
		if ( v == tabContact )
		{
			setContactSearch();
			
			call_search_input.requestFocus();
		}
		else if ( v == tabOrganization )
		{
			setOrganizationSearch();
			
			call_search_input.requestFocus();
		}
		else if ( v == call_value_delete )
		{
			call_search_input.setText("");
		}
		else if ( v == call_btn_search )
		{
			search();
		}
		else if ( v == searchview_exit )
		{
			finish();
		}
		else if ( v == searchview_submit )
		{
			if ( switching_inputfield.getText().toString().equals("") )
			{
				ActionManager.alert(this, "전화번호를 입력해 주세요");
				
				return;
			}
			else
			{
				Intent i = new Intent(Define.IPG_CALL_ACTION);
	            i.putExtra("Action", "SWITCH");
	            i.putExtra("NUMBER", switching_inputfield.getText().toString().replaceAll("-", ""));
	            sendBroadcast(i);
			}
			
			finish();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void search()
	{
		searchResult.clear();
		
		hideKeyboard();
		
		if ( isContactSearch )
		{
			for ( Contact ct : Define.contactArray )
	        {
				if ( ct.getName() != null && StringUtil.startsWith( call_search_input.getText().toString(), ct.getName() ) )
				{
					String number = ct.getPhonenum();
					if ( number == null || number.equals("") )
					{
						number = ct.getTelnum();
					}
					
					searchResult.add( new SearchData(ct.getContactId(), false, ct.getName() + " " + ct.getPosition(), PhoneNumberUtils.formatNumber(number), ct.getCompany(), ct) );
				}
	        }
			
			adapter.notifyDataSetChanged();
		}
		else
		{
			new Search(call_search_input.getText().toString());
		}
	}
	
	@Override
	public void onDestroy()
	{
		onDestroy = true;
		
		super.onDestroy();
	}
	
	public Handler searchHandler = new Handler()
	{
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
                    if ( !call_search_input.getText().toString().equals( key ) ) return;
                    param.remove( param.size() - 1 );
                    
                    for ( int i = 0; i < searchResult.size(); i++ )
                    {
                        if ( searchResult.get( i ).id != null && searchResult.get( i ).id.equals( param.get( 0 ) ) )
                        {
                            return;
                        }
                    }
                    
                    String userName = param.get(3);
                    
                    String[] nameAr = userName.split("#");
                    
                    searchResult.add( new SearchData( param.get( 0 ), true, nameAr[0] + " " + nameAr[1], nameAr[3], nameAr[2], null));
                }
                else if ( msg.what == Define.AM_SEARCH_START )
                {
                    m_WaitForSearchProgressDialog = ProgressDialog.show( callSelectUser, "", "검색결과를 수신중입니다.", true );
                    m_WaitForSearchProgressDialog.show();
                }
                else if ( msg.what == Define.AM_SEARCH_END )
                {
                    ActionManager.hideProgressDialog();
                    adapter.notifyDataSetChanged();
                    
                    if ( m_WaitForSearchProgressDialog != null )
                    {
                        m_WaitForSearchProgressDialog.dismiss();
                        m_WaitForSearchProgressDialog = null;
                    }
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
            if ( call_search_input.getText().toString().equals( searchKey ) )
            {
                param.add( searchKey );
                Message m = searchHandler.obtainMessage( Define.AM_ADD_SEARCH, param );
                searchHandler.sendMessage( m );
            }
        }
	}
	
	class Search extends Thread
	{
        String searchKey = null;
        UltariSSLSocket sc = null;
        AmCodec codec;

        public Search( String searchKey )
        {
                this.searchKey = searchKey;
                this.codec = new AmCodec();
                
                this.start();
        }
        
        public void send( String msg ) throws Exception
        {
                msg.replaceAll( "\f", "" );
                sc.send( codec.EncryptSEED( msg ) + '\f' );
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
                        
                        short type = 0;
                        
                        send( "SearchRequest\t" + type + "\t" + searchKey );
                        
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
                                        	Message m = searchHandler.obtainMessage( Define.AM_SEARCH_END, param );
                                            searchHandler.sendMessage( m );
                                            
                                            return;
                                        }
                                        else
                                        {
                                                process( command, param, searchKey );
                                        }
                                }
                        }
                }
                catch ( SocketException se )
                {
                        Log.e( "CallSearch", se.getMessage() );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                finally
                {
                        if ( sc != null ) { try { sc.close(); sc = null; } catch ( Exception e ) {} }
                        if ( ir != null ) { try { ir.close(); ir = null; } catch ( Exception e ) {} }
                        if ( br != null ) { try { br.close(); br = null; } catch ( Exception e ) {} }
                        
                        Message msg = searchHandler.obtainMessage( Define.AM_SEARCH_END, null );
                        searchHandler.sendMessage( msg );
                }
        }
}
	
	public void setContactSearch()
	{
		if ( isContactSearch ) return;
		
		tabContact.setSelected(true);
		tabOrganization.setSelected(false);
		
		call_search_input.setHint("검색어 입력 - 연락처 내에서 검색");
		
		call_search_input.setText("");
		
		isContactSearch = true;
	}
	
	public void setOrganizationSearch()
	{
		if ( !isContactSearch ) return;
		
		tabContact.setSelected(false);
		tabOrganization.setSelected(true);
		
		call_search_input.setHint("검색어 입력 - 조직도에서 검색");
		
		call_search_input.setText("");
		
		isContactSearch = false;
	}
	
	private void hideKeyboard()
	{
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 

		inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	class SearchData
	{
		public String id;
		public boolean isOrganization;
		public String name;
		public String number;
		public String department;
		public Contact contact;
		
		public SearchData(String id, boolean isOrganization, String name, String number, String department, Contact contact)
		{
			this.id = id;
			this.isOrganization = isOrganization;
			this.name = name;
			this.number = number;
			this.department = department;
			this.contact = contact;
		}
	}
	
	@SuppressLint("InflateParams")
	class CallSearchResult extends ArrayAdapter<SearchData>
	{
		private ArrayList<SearchData> objects;
		
		public CallSearchResult(Context context, int resource, ArrayList<SearchData> objects)
		{
			super(context, resource, objects);
			
			this.objects = objects;
		}
		
		@SuppressWarnings("deprecation")
		@Override
		public View getView(int pos, View convertView, ViewGroup parent)
		{
			LayoutInflater mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			UserImageView call_search_photo = null;
			TextView call_search_name = null;
			TextView call_search_department = null;

			if (convertView == null)
			{
				convertView = mInflater.inflate(R.layout.item_call_select_user, null);
				
				call_search_photo = (UserImageView) convertView.findViewById(R.id.call_search_photo);
				call_search_name = (TextView) convertView.findViewById(R.id.call_search_name);
				call_search_department = (TextView) convertView.findViewById(R.id.call_search_department);
			}
			else
			{
				call_search_photo = (UserImageView) convertView.findViewById(R.id.call_search_photo);
				call_search_name = (TextView) convertView.findViewById(R.id.call_search_name);
				call_search_department = (TextView) convertView.findViewById(R.id.call_search_department);
			}

			SearchData data = objects.get(pos);
			
			call_search_name.setText(data.name);
			
			call_search_department.setText("(" + PhoneNumberUtils.formatNumber(data.number) + ") " + data.department);
			
			Bitmap pic = BitmapFactory.decodeResource( getResources(), R.drawable.img_profile_100x100 );
			call_search_photo.setImageBitmap( ImageUtil.getDrawOval( pic ) );
            
			if ( !data.isOrganization )
			{
				Bitmap bmp = data.contact.getBitmap();
                if ( bmp != null )
                {
                	call_search_photo.setImageBitmap( ImageUtil.getDrawOval(bmp)); 
                }
			}
			else
			{
				call_search_photo.setUserId(data.id);
			}

			return convertView;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		SearchData data = searchResult.get(position);
		
		switching_inputfield.setText(data.number);
	}
}
