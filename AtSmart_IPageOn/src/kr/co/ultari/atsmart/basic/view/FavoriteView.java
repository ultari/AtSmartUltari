package kr.co.ultari.atsmart.basic.view;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.android.gms.drive.internal.ac;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.CallLogData;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.subdata.FavoriteData;
import kr.co.ultari.atsmart.basic.subdata.UserObject;
import kr.co.ultari.atsmart.basic.util.FmcSendBroadcast;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import kr.co.ultari.atsmart.basic.view.CallView.GetUserInfoThread;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FavoriteView extends Fragment implements OnClickListener, OnScrollListener {
        private static final String TAG = "/AtSmart/FavoriteView";
        private static FavoriteView favoriteViewInstance = null;
        public LayoutInflater inflater;
        private View view;
        private ListView list_favorite;
        private Button btnRemoveAll, btnOption, btnCheck, btnRemove, btnCancel;
        private LinearLayout layoutOptionDialog;
        private boolean isShow = false, isCheckShow = false;
        public FavoriteAdapter adapter = null;
        public boolean isLoadComplete = true;
        private RelativeLayout layoutNormal, layoutSelect;
        private TextView tvTitle;
        private ArrayList<FavoriteData> favoriteArray = null;
        public String nowTelNumber = "";
        public String nowMobilePhoneNumber = "";

        public static FavoriteView instance()
        {
                if ( favoriteViewInstance == null ) favoriteViewInstance = new FavoriteView();
                return favoriteViewInstance;
        }

        @Override
        public void onDestroy()
        {
                super.onDestroy();
                favoriteViewInstance = null;
        }

        @Override
        public void onPause()
        {
                super.onPause();
        }

        @Override
        public void onResume()
        {
                super.onResume();
                Message msg = FavoriteViewHandler.obtainMessage( Define.AM_COMPLETE );
                FavoriteViewHandler.sendMessage( msg );
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                this.inflater = inflater;
                view = inflater.inflate( R.layout.activity_favorite, null );
                list_favorite = ( ListView ) view.findViewById( R.id.favorite_list );
                list_favorite.setOnScrollListener( this );
                btnRemoveAll = ( Button ) view.findViewById( R.id.favorite_removeall );
                btnRemoveAll.setOnClickListener( this );
                btnRemoveAll.setTypeface( Define.tfRegular );
                btnOption = ( Button ) view.findViewById( R.id.favorite_option );
                btnOption.setOnClickListener( this );
                btnCheck = ( Button ) view.findViewById( R.id.favorite_checkremove );
                btnCheck.setOnClickListener( this );
                btnCheck.setTypeface( Define.tfRegular );
                btnRemove = ( Button ) view.findViewById( R.id.favorite_remove );
                btnRemove.setOnClickListener( this );
                btnRemove.setTypeface( Define.tfRegular );
                btnRemove.setVisibility( View.GONE );
                btnCancel = ( Button ) view.findViewById( R.id.favorite_cancel );
                btnCancel.setOnClickListener( this );
                btnCancel.setTypeface( Define.tfRegular );
                btnCancel.setVisibility( View.GONE );
                layoutOptionDialog = ( LinearLayout ) view.findViewById( R.id.favorite_option_dialog );
                layoutOptionDialog.setVisibility( View.GONE );
                tvTitle = ( TextView ) view.findViewById( R.id.favorite_title );
                tvTitle.setTypeface( Define.tfMedium );
                layoutNormal = ( RelativeLayout ) view.findViewById( R.id.favorite_tab_normal );
                layoutSelect = ( RelativeLayout ) view.findViewById( R.id.favorite_tab_selected );
                favoriteArray = new ArrayList<FavoriteData>();
                favoriteArray.clear();
                adapter = new FavoriteAdapter( Define.mContext, R.layout.favorite_item, favoriteArray );
                list_favorite.setAdapter( adapter );
                resetData();
                return view;
        }
        class ResetFavorite extends Thread {
                public ResetFavorite()
                {
                        this.start();
                }

                public void run()
                {
                        if ( !isLoadComplete ) return;
                        isLoadComplete = false;
                        Message msg = FavoriteViewHandler.obtainMessage( Define.AM_REFRESH );
                        FavoriteViewHandler.sendMessage( msg );
                        ContentResolver cr = null;
                        Cursor cur = null;
                        try
                        {
                                String selection = ContactsContract.Contacts.STARRED + "='1'";
                                cr = Define.mContext.getContentResolver();
                                cur = cr.query( ContactsContract.Contacts.CONTENT_URI, null, selection, null, null );
                                /*
                                 * FavoriteData title = new FavoriteData();
                                 * title.setPhonenum( "1000" );
                                 * title.setName( "DeviceTitle" );
                                 * title.setType( "Device" );
                                 * title.setPhotoid( Long.parseLong( "0" ) );
                                 * title.setIsCheck( "false" );
                                 * msg = FavoriteViewHandler.obtainMessage( Define.AM_ADD_BUDDY, title );
                                 * FavoriteViewHandler.sendMessage( msg );
                                 */
                                if ( cur.getCount() > 0 )
                                {
                                        String tmpOfficeNo = "";
                                        String tmpPhoneNo = "";
                                        while ( cur.moveToNext() )
                                        {
                                                String id = cur.getString( cur.getColumnIndex( ContactsContract.Contacts._ID ) );
                                                String name = cur.getString( cur.getColumnIndex( ContactsContract.Contacts.DISPLAY_NAME ) );
                                                String phoneNumber = "";
                                                if ( Integer.parseInt( cur.getString( cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER ) ) ) > 0 )
                                                {
                                                        Cursor phones = Define.mContext.getContentResolver().query(
                                                                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                                                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null );
                                                        while ( phones.moveToNext() )
                                                        {
                                                                String PhoneType = phones.getString( phones
                                                                                .getColumnIndex( ContactsContract.CommonDataKinds.Phone.TYPE ) );
                                                                String PhoneNo = phones.getString( phones
                                                                                .getColumnIndex( ContactsContract.CommonDataKinds.Phone.DATA ) );
                                                                if ( PhoneType.equals( "1" ) ) tmpOfficeNo = PhoneNo;
                                                                else if ( PhoneType.equals( "2" ) ) tmpPhoneNo = PhoneNo;
                                                        }
                                                        phones.close();
                                                }
                                                // device
                                                FavoriteData acontact = new FavoriteData();
                                                acontact.setPhonenum( tmpPhoneNo.replaceAll( "-", "" ) );
                                                acontact.setHomenum( tmpOfficeNo.replaceAll( "-", "" ) );
                                                acontact.setName( name );
                                                acontact.setType( "Device" );
                                                acontact.setPhotoid( Long.parseLong( id ) );
                                                acontact.setIsCheck( "false" );
                                                msg = FavoriteViewHandler.obtainMessage( Define.AM_ADD_BUDDY_USER, acontact );
                                                FavoriteViewHandler.sendMessage( msg );
                                        }
                                }
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
                        }
                        finally
                        {
                                if ( cur != null ) cur.close();
                                cur = null;
                        }
                        try
                        {
                                ArrayList<ArrayList<String>> arr = Database.instance( Define.mContext ).selectFavorite();
                                if ( arr != null )
                                {
                                        // 0:1012@scm.com, 1:0, 2:도청-울타리#직위#경기도청#1012##010-9056-9435#1011@scm.com###, 3:얌얌, 4:0, 5:false
                                        for ( ArrayList<String> tmp : arr )
                                        {
                                                FavoriteData acontact = new FavoriteData();
                                                String[] parse = tmp.get( 2 ).split( "#" , -1); //2016-11-29 HHJ
                                                //String[] parse = tmp.get( 2 ).split( "#" );
                                                if ( parse != null )
                                                {
                                                        acontact.setHomenum( parse[3] );
                                                        acontact.setPhonenum( parse[5] );
                                                        acontact.setName( parse[0] );
                                                        acontact.userId = tmp.get( 0 );
                                                        acontact.userName = tmp.get( 2 );
                                                        acontact.setUser( null );
                                                        acontact.setType( "Organization" );
                                                        acontact.setIsCheck( "false" );
                                                }
                                                else
                                                {
                                                        acontact.setHomenum( "" );
                                                        acontact.setPhonenum( "" );
                                                        acontact.setName( "" );
                                                        acontact.userId = "";
                                                        acontact.userName = "";
                                                        acontact.setUser( null );
                                                        acontact.setType( "" );
                                                        acontact.setIsCheck( "false" );
                                                        Log.d( TAG, "Favorite list is null" );
                                                }
                                                msg = FavoriteViewHandler.obtainMessage( Define.AM_ADD_BUDDY_USER, acontact );
                                                FavoriteViewHandler.sendMessage( msg );
                                        }
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                        
                        if(list_favorite != null)
                        {
                                list_favorite.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick( AdapterView<?> contactlist, View v, int position, long resid )
                                        {
                                                FavoriteData phonenumber = ( FavoriteData ) contactlist.getItemAtPosition( position );
                                                if ( phonenumber == null ) return;
                                        }
                                } );
                                msg = FavoriteViewHandler.obtainMessage( Define.AM_COMPLETE );
                                FavoriteViewHandler.sendMessage( msg );
                                isLoadComplete = true;
                        }
                }
        }
        public Handler FavoriteViewHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REFRESH )
                                {
                                        if(adapter != null)
                                                adapter.clear();
                                }
                                else if ( msg.what == Define.AM_ADD_BUDDY_USER )
                                {
                                        FavoriteData data = ( FavoriteData ) msg.obj;
                                        boolean isCheck = false;
                                        
                                        if(adapter != null)
                                        {
                                                for ( int i = 0; i < adapter.getCount(); i++ )
                                                        if ( adapter.getItem( i ).getName() != null && adapter.getItem( i ).getName().equals( data.getName() )
                                                                        && adapter.getItem( i ).userId != null && adapter.getItem( i ).userId.equals( data.userId ) ) isCheck = true;
                                                if ( !isCheck ) adapter.insert( data, adapter.getCount() );
                                        }
                                }
                                else if ( msg.what == Define.AM_COMPLETE )
                                {
                                        if(list_favorite != null)
                                        {
                                                if ( list_favorite.getCount() < 20 )
                                                {
                                                        setIdAndPhoto( 0, list_favorite.getCount() - 1 );
                                                }
                                                else
                                                {
                                                        int from = 0;
                                                        int to = 0;
                                                        for ( int i = 0; i < list_favorite.getCount(); i++ )
                                                        {
                                                                if ( list_favorite.getChildAt( i ) != null )
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
                                }
                                else if ( msg.what == Define.AM_CALL_POPUP )
                                {
                                        View v = ( View ) msg.obj;
                                        registerForContextMenu( v );
                                        getActivity().openContextMenu( v );
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

        //2016-05-27
        private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
        //
        
        public void resetData()
        {
                //2016-05-27
                try
                {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) 
                                getActivity().requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
                        else
                        //
                                new ResetFavorite();
                }
                catch(Exception e)
                {
                        e.printStackTrace();
                }
        }

        private String timeToString( Long time )
        {
                SimpleDateFormat simpleFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
                String date = simpleFormat.format( new Date( time ) );
                return date;
        }
        public class FavoriteAdapter extends ArrayAdapter<FavoriteData> implements OnClickListener {
                private int resId;
                private ArrayList<FavoriteData> favoritelist;
                private LayoutInflater Inflater;
                private Context context;

                public FavoriteAdapter( Context context, int textViewResourceId, List<FavoriteData> objects )
                {
                        super( context, textViewResourceId, objects );
                        this.context = context;
                        resId = textViewResourceId;
                        favoritelist = ( ArrayList<FavoriteData> ) objects;
                        Inflater = ( LayoutInflater ) Define.mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                }

                @Override
                public View getView( int position, View v, ViewGroup parent )
                {
                        UserImageView iv_photoid = null;
                        TextView tv_name = null;
                        Button btn_fmcCall = null;
                        Button btn_call = null;
                        Button btn_chat = null;
                        Button btn_check = null;
                        LinearLayout layoutTitle = null;
                        ImageView iv_line = null;
                        FavoriteData acontact = favoritelist.get( position );
                        if ( v == null ) v = Inflater.inflate( resId, null );
                        tv_name = ( TextView ) v.findViewById( R.id.favorite_name );
                        tv_name.setTag( acontact );
                        tv_name.setOnClickListener( this );
                        iv_photoid = ( UserImageView ) v.findViewById( R.id.favorite_photo );
                        btn_call = ( Button ) v.findViewById( R.id.favorite_btncall );
                        btn_call.setTag( acontact );
                        btn_call.setOnClickListener( this );
                        
                        btn_fmcCall = ( Button ) v.findViewById( R.id.favorite_btnFmcCall );
                        btn_fmcCall.setTag( acontact );
                        btn_fmcCall.setOnClickListener( this );
                        
                        btn_chat = ( Button ) v.findViewById( R.id.favorite_btnchat );
                        btn_chat.setTag( acontact );
                        btn_chat.setOnClickListener( this );
                        btn_check = ( Button ) v.findViewById( R.id.favorite_btncheck );
                        btn_check.setTag( acontact );
                        btn_check.setOnClickListener( this );
                        layoutTitle = ( LinearLayout ) v.findViewById( R.id.favorite_titleicon );
                        iv_line = ( ImageView ) v.findViewById( R.id.favorite_line );
                        if ( !isCheckShow ) btn_check.setVisibility( View.GONE );
                        else btn_check.setVisibility( View.VISIBLE );
                        if ( acontact != null )
                        {
                                if ( acontact.getIsCheck().equals( "false" ) ) btn_check.setBackgroundResource( R.drawable.radio_nor_whitebg_s );
                                else btn_check.setBackgroundResource( R.drawable.radio_sel_whitebg_s );
                                if ( acontact.getType().equals( "Organization" ) )
                                {
                                        if ( acontact.getName().equals( "OrgTitle" ) )
                                        {
                                                tv_name.setTypeface( Define.tfMedium );
                                                tv_name.setText( getString( R.string.org_favorite_add ) );
                                                //tv_name.setTextColor( 0xFF979696 );
                                                btn_fmcCall.setVisibility( View.GONE );
                                                btn_call.setVisibility( View.GONE );
                                                //btn_chat.setVisibility( View.GONE );
                                                btn_check.setVisibility( View.GONE );
                                                iv_photoid.setVisibility( View.GONE );
                                                iv_line.setVisibility( View.GONE );
                                                layoutTitle.setVisibility( View.VISIBLE );
                                                //v.setBackgroundColor( 0xFF2C2C2C );
                                        }
                                        else
                                        {
                                                tv_name.setTypeface( Define.tfRegular );
                                                tv_name.setText( acontact.getName() );
                                                //btn_chat.setBackgroundResource( R.drawable.icon_chat );
                                                iv_photoid.setVisibility( View.VISIBLE );
                                                iv_line.setVisibility( View.VISIBLE );
                                                //tv_name.setTextColor( 0xFFD2D2D2 );
                                                layoutTitle.setVisibility( View.GONE );
                                                //v.setBackgroundColor( 0xFF232323 );
                                                if ( !isCheckShow )
                                                {
                                                        btn_fmcCall.setVisibility( View.VISIBLE );
                                                        btn_call.setVisibility( View.VISIBLE );
                                                        //btn_chat.setVisibility( View.VISIBLE );
                                                        iv_line.setVisibility( View.VISIBLE );
                                                }
                                                else
                                                {
                                                        iv_line.setVisibility( View.GONE );
                                                        btn_call.setVisibility( View.GONE );
                                                        btn_fmcCall.setVisibility( View.GONE );
                                                        //btn_chat.setVisibility( View.GONE );
                                                }
                                        }
                                }
                                else if ( acontact.getType().equals( "Device" ) )
                                {
                                        if ( acontact.getName().equals( "DeviceTitle" ) )
                                        {
                                                tv_name.setTypeface( Define.tfMedium );
                                                tv_name.setText( getString( R.string.favorite_device_title ) );
                                                //tv_name.setTextColor( 0xFF979696 );
                                                btn_call.setVisibility( View.GONE );
                                                btn_fmcCall.setVisibility( View.GONE );
                                                //btn_chat.setVisibility( View.GONE );
                                                btn_check.setVisibility( View.GONE );
                                                iv_photoid.setVisibility( View.GONE );
                                                //iv_line.setVisibility( View.GONE );
                                                layoutTitle.setVisibility( View.VISIBLE );
                                                //v.setBackgroundColor( 0xFF2C2C2C );
                                        }
                                        else
                                        {
                                                tv_name.setTypeface( Define.tfRegular );
                                                tv_name.setText( acontact.getName() );
                                                //btn_chat.setBackgroundResource( R.drawable.icon_chat_none );
                                                iv_photoid.setVisibility( View.VISIBLE );
                                                //tv_name.setTextColor( 0xFFD2D2D2 );
                                                layoutTitle.setVisibility( View.GONE );
                                                //v.setBackgroundColor( 0xFF232323 );
                                                if ( !isCheckShow )
                                                {
                                                        btn_call.setVisibility( View.VISIBLE );
                                                        btn_fmcCall.setVisibility( View.VISIBLE );
                                                        //btn_chat.setVisibility( View.GONE );
                                                        iv_line.setVisibility( View.VISIBLE );
                                                }
                                                else
                                                {
                                                        btn_call.setVisibility( View.GONE );
                                                        btn_fmcCall.setVisibility( View.GONE );
                                                        //btn_chat.setVisibility( View.GONE );
                                                        iv_line.setVisibility( View.GONE );
                                                }
                                        }
                                }
                                if ( acontact.userId != null && !acontact.userId.equals( "" ) ) iv_photoid.setUserId( acontact.userId );
                                else
                                {
                                        Bitmap bm = openPhoto( acontact.getPhotoid() );
                                        if ( bm != null ) iv_photoid.setImageBitmap( bm );
                                        else iv_photoid.setImageDrawable( getResources().getDrawable( R.drawable.img_profile_190x190 ) );
                                }
                        }
                        return v;
                }

                private Bitmap openPhoto( long contactId )
                {
                        Uri contactUri = ContentUris.withAppendedId( Contacts.CONTENT_URI, contactId );
                        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream( context.getContentResolver(), contactUri );
                        if ( input != null )
                        {
                                try
                                {
                                        return BitmapFactory.decodeStream( input );
                                }
                                catch ( Exception e )
                                {
                                        return null;
                                }
                                finally
                                {
                                        if ( input != null )
                                        {
                                                try
                                                {
                                                        input.close();
                                                        input = null;
                                                }
                                                catch ( Exception e )
                                                {}
                                        }
                                }
                        }
                        return null;
                }

                @Override
                public void onClick( View v )
                {
                        try
                        {
                                FavoriteData data = ( FavoriteData ) v.getTag();
                                if ( v.getId() == R.id.favorite_btncall )
                                {
                                        nowMobilePhoneNumber = data.getPhonenum();
                                        nowTelNumber = data.getHomenum();
                                        if ( nowMobilePhoneNumber != null && !nowMobilePhoneNumber.equals( "" ) && nowTelNumber != null
                                                        && !nowTelNumber.equals( "" ) )
                                        {
                                                Message msg = FavoriteViewHandler.obtainMessage( Define.AM_CALL_POPUP );
                                                msg.obj = v;
                                                FavoriteViewHandler.sendMessage( msg );
                                        }
                                        else if ( nowTelNumber != null && !nowTelNumber.equals( "" ) )
                                        {
                                                FmcSendBroadcast.FmcSendCall( nowTelNumber ,1, getActivity().getApplicationContext()); //2016-03-31
                                        }
                                        else
                                        {
                                                FmcSendBroadcast.FmcSendCall( nowMobilePhoneNumber ,1, getActivity().getApplicationContext()); //2016-03-31
                                        }
                                }
                                else if( v.getId() == R.id.favorite_btnFmcCall )
                                {
                                        nowMobilePhoneNumber = data.getPhonenum();
                                        nowTelNumber = data.getHomenum();
                                        if ( nowMobilePhoneNumber != null && !nowMobilePhoneNumber.equals( "" ) && nowTelNumber != null
                                                        && !nowTelNumber.equals( "" ) )
                                        {
                                                Message msg = FavoriteViewHandler.obtainMessage( Define.AM_CALL_POPUP );
                                                msg.obj = v;
                                                FavoriteViewHandler.sendMessage( msg );
                                        }
                                        else if ( nowTelNumber != null && !nowTelNumber.equals( "" ) )
                                        {
                                                FmcSendBroadcast.FmcSendCall( nowTelNumber ,0, getActivity().getApplicationContext()); //2016-03-31
                                        }
                                        else
                                        {
                                                FmcSendBroadcast.FmcSendCall( nowMobilePhoneNumber ,0, getActivity().getApplicationContext()); //2016-03-31
                                        }
                                }
                                else if ( v.getId() == R.id.favorite_name )
                                {
                                        if ( data.getType().equals( "Device" ) )
                                        {
                                                String contactId = Define.getContactIdFromName( data.getName() );
                                                if ( contactId == null )
                                                {
                                                        Contact acontact = new Contact();
                                                        acontact.setType( "Device" );
                                                        if ( data.userId != null ) acontact.userId = data.userId;
                                                        else acontact.userId = null;
                                                        if ( data.userName != null ) acontact.userName = data.userName;
                                                        else acontact.userName = null;
                                                        acontact.setPhotoid( data.getPhotoid() );
                                                        acontact.setUserid( "" );
                                                        acontact.setTelnum( data.getHomenum() );
                                                        acontact.setPhonenum( data.getPhonenum() );
                                                        acontact.setName( data.getName() );
                                                        acontact.setPosition( "" );
                                                        acontact.setCompany( "" );
                                                        acontact.setEmail( "" );
                                                        acontact.setNickName( "" );
                                                        Define.contactMap.put( "TmpContact", acontact );
                                                        Intent it = new Intent( context, kr.co.ultari.atsmart.basic.subview.ContactDetail.class );
                                                        it.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                                        it.putExtra( "contactId", "TmpContact" );
                                                        context.startActivity( it );
                                                }
                                                else
                                                {
                                                        Intent it = new Intent( context, kr.co.ultari.atsmart.basic.subview.ContactDetail.class );
                                                        it.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                                        it.putExtra( "contactId", contactId );
                                                        context.startActivity( it );
                                                }
                                        }
                                        else
                                        {
                                                String[] parse = data.userName.split( "#" );
                                                ActionManager.popupUserInfo( context, data.userId, data.userName, parse[2], "" );
                                        }
                                }
                                else if ( v.getId() == R.id.favorite_btnchat )
                                {
                                        String userId = data.userId;
                                        String userName = null;
                                        if ( userId != null ) userName = data.getOrgUserName();
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
                                else if ( v.getId() == R.id.favorite_btncheck )
                                {
                                        if ( data.getIsCheck().equals( "false" ) )
                                        {
                                                data.setIsCheck( "true" );
                                                v.setBackgroundResource( R.drawable.btn_blackbg_checked );
                                        }
                                        else
                                        {
                                                data.setIsCheck( "false" );
                                                v.setBackgroundResource( R.drawable.btn_blackbg_uncheck );
                                        }
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        }

        @Override
        public boolean onContextItemSelected( MenuItem item )
        {
                if ( item.getItemId() == 0 )
                {
                        FmcSendBroadcast.FmcSendCall( nowTelNumber ,0, getActivity().getApplicationContext()); //2016-03-31
                }
                else if ( item.getItemId() == 1 )
                {
                        FmcSendBroadcast.FmcSendCall( nowMobilePhoneNumber ,0, getActivity().getApplicationContext()); //2016-03-31
                }
                return super.onContextItemSelected( item );
        }

        @Override
        public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo )
        {
                super.onCreateContextMenu( menu, v, menuInfo );
                //if ( v.getId() == R.id.favorite_btncall )
                //{
                        menu.setHeaderTitle( getString( R.string.choicePhoneNumber ) );
                        menu.add( 0, 0, Menu.NONE, PhoneNumberUtils.formatNumber( nowTelNumber ) );
                        menu.add( 0, 1, Menu.NONE, PhoneNumberUtils.formatNumber( nowMobilePhoneNumber ) );
                //}
        }

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == R.id.favorite_option )
                {
                        isShow = !isShow;
                        if ( isShow ) layoutOptionDialog.setVisibility( View.VISIBLE );
                        else layoutOptionDialog.setVisibility( View.GONE );
                        btnCancel.setVisibility( View.GONE );
                        btnRemove.setVisibility( View.GONE );
                        isCheckShow = false;
                }
                else if ( v.getId() == R.id.favorite_cancel )
                {
                        for ( int i = 0; i < adapter.getCount(); i++ )
                                if ( adapter.getItem( i ).getIsCheck().equals( "true" ) ) adapter.getItem( i ).setIsCheck( "false" );
                        btnCancel.setVisibility( View.GONE );
                        btnRemove.setVisibility( View.GONE );
                        layoutOptionDialog.setVisibility( View.GONE );
                        isCheckShow = false;
                        if ( ActionManager.tabs != null ) ActionManager.tabs.handler.sendEmptyMessageDelayed( Define.AM_TAB_SHOW, 100 );
                        layoutNormal.setVisibility( View.VISIBLE );
                        layoutSelect.setVisibility( View.GONE );
                        resetData();
                }
                else if ( v.getId() == R.id.favorite_remove )
                {
                        for ( int i = 0; i < adapter.getCount(); i++ )
                        {
                                if ( adapter.getItem( i ).getIsCheck().equals( "true" ) )
                                {
                                        if ( adapter.getItem( i ).getType().equals( "Device" ) )
                                        {
                                                ContentValues values = new ContentValues();
                                                String[] fv = new String[] { adapter.getItem( i ).getName() };
                                                values.put( Contacts.STARRED, 0 );
                                                Define.mContext.getContentResolver().update( Contacts.CONTENT_URI, values, Contacts.DISPLAY_NAME + "= ?", fv );
                                        }
                                        else
                                        {
                                                Database.instance( Define.mContext ).deleteFavorite( adapter.getItem( i ).userId );
                                        }
                                }
                        }
                        btnCancel.setVisibility( View.GONE );
                        btnRemove.setVisibility( View.GONE );
                        layoutOptionDialog.setVisibility( View.GONE );
                        isCheckShow = false;
                        if ( ActionManager.tabs != null ) ActionManager.tabs.handler.sendEmptyMessageDelayed( Define.AM_TAB_SHOW, 100 );
                        layoutNormal.setVisibility( View.VISIBLE );
                        layoutSelect.setVisibility( View.GONE );
                        resetData();
                }
                else if ( v.getId() == R.id.favorite_checkremove )
                {
                        btnCancel.setVisibility( View.VISIBLE );
                        btnRemove.setVisibility( View.VISIBLE );
                        layoutOptionDialog.setVisibility( View.GONE );
                        isCheckShow = true;
                        if ( ActionManager.tabs != null ) ActionManager.tabs.handler.sendEmptyMessageDelayed( Define.AM_TAB_HIDE, 100 );
                        layoutSelect.setVisibility( View.VISIBLE );
                        layoutNormal.setVisibility( View.GONE );
                        resetData();
                }
                else if ( v.getId() == R.id.favorite_removeall )
                {
                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder( MainActivity.Instance() );
                        alert_confirm.setTitle( Define.mContext.getString( R.string.del_favorite_title ) );
                        alert_confirm.setMessage( Define.mContext.getString( R.string.del_favorite_all ) ).setCancelable( false )
                                        .setPositiveButton( Define.mContext.getString( R.string.ok ), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick( DialogInterface dialog, int which )
                                                {
                                                        for ( int i = 0; i < adapter.getCount(); i++ )
                                                        {
                                                                if ( adapter.getItem( i ).getType().equals( "Device" ) )
                                                                {
                                                                        ContentValues values = new ContentValues();
                                                                        String[] fv = new String[] { adapter.getItem( i ).getName() };
                                                                        values.put( Contacts.STARRED, 0 );
                                                                        Define.mContext.getContentResolver().update( Contacts.CONTENT_URI, values,
                                                                                        Contacts.DISPLAY_NAME + "= ?", fv );
                                                                }
                                                        }
                                                        Database.instance( Define.mContext ).deleteFavoriteAll();
                                                        btnCancel.setVisibility( View.GONE );
                                                        btnRemove.setVisibility( View.GONE );
                                                        layoutOptionDialog.setVisibility( View.GONE );
                                                        isCheckShow = false;
                                                        resetData();
                                                        dialog.dismiss();
                                                }
                                        } ).setNegativeButton( Define.mContext.getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick( DialogInterface dialog, int which )
                                                {
                                                        dialog.dismiss();
                                                        return;
                                                }
                                        } );
                        AlertDialog alert = alert_confirm.create();
                        alert.show();
                        layoutOptionDialog.setVisibility( View.GONE );
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
                setIdAndPhoto( list_favorite.getFirstVisiblePosition(), list_favorite.getLastVisiblePosition() );
        }

        public void setIdAndPhoto( int from, int to )
        {
                Log.d( "kr.co.ultari.atsmart.basic", "setIdAndPhoto : " + from + " > " + to );
                if ( from > to ) return;
                StringBuffer checkIds = new StringBuffer();
                for ( int i = from; i <= to; i++ )
                {
                        FavoriteData data = ( FavoriteData ) list_favorite.getAdapter().getItem( i );
                        if ( data.userId == null ) checkIds.append( "\t" + data.getPhonenum() );
                }
                if ( checkIds.length() > 0 ) new GetUserInfoThread( checkIds.toString(), list_favorite.getAdapter() );
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
                        BufferedWriter bw = null;
                        // Log.d( "GetUserIds", userIds );
                        try
                        {
                                sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                                .getServerPort( Define.mContext ) ) );
                                ir = new InputStreamReader( sc.getInputStream() );
                                br = new BufferedReader( ir );
                                bw = sc.getWriter();
                                send( "GETID" + userIds, bw );
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
                                                Log.d( "GetUser", rcvStr );
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
                                                if ( !process( command, param ) )
                                                {
                                                        m_bFinish = true;
                                                }
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
                                if ( bw != null )
                                {
                                        try
                                        {
                                                bw.close();
                                                sc = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
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

                public void send( String msg, BufferedWriter bw ) throws Exception
                {
                        if ( bw == null ) throw new Exception( "Not connected" );
                        msg.replaceAll( "\f", "" );
                        msg = codec.EncryptSEED( msg );
                        msg += "\f";
                        bw.write( msg );
                        bw.flush();
                }
        }
        public Handler phoneNumberHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_LIST_SET_ID ) // 부서
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        for ( int i = list_favorite.getFirstVisiblePosition(); i <= list_favorite.getLastVisiblePosition(); i++ )
                                        {
                                                FavoriteData data = ( FavoriteData ) list_favorite.getItemAtPosition( i );
                                                if ( param.size() > 1 && data.getPhonenum().equals( param.get( 0 ) ) )
                                                {
                                                        data.userId = param.get( 1 );
                                                        if ( param.size() == 3 ) data.userName = param.get( 2 );
                                                }
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
                View v = list_favorite.getChildAt( position - list_favorite.getFirstVisiblePosition() );
                if ( v == null ) return;
                
                FavoriteData acontact = favoriteArray.get( position );
                UserImageView iv_photoid = ( UserImageView ) v.findViewById( R.id.favorite_photo );
                ImageView iv_line = ( ImageView ) v.findViewById( R.id.favorite_line );

                if ( acontact != null && acontact.userId != null && !acontact.userId.equals( "" ) )
                {
                        if ( isCheckShow )
                                iv_line.setVisibility( View.GONE );
                        else
                                iv_line.setVisibility( View.VISIBLE );
                        
                        iv_photoid.setUserIdOval( acontact.userId, false );
                }
                else
                        iv_photoid.setImageDrawable( getResources().getDrawable( R.drawable.img_profile_190x190 ) );
        }
}
