package kr.co.ultari.atsmart.basic.view;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.CallLogData;
import kr.co.ultari.atsmart.basic.util.FmcSendBroadcast;
import kr.co.ultari.atsmart.basic.util.ImageUtil;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CallView extends Fragment implements OnClickListener, OnScrollListener 
{
        @SuppressWarnings( "unused" )
        private static final String TAG = "/AtSmart/CallView";
        private static CallView callViewInstance = null;
        public LayoutInflater inflater;
        private View view;
        private String[] items = { "", "", "", "", ""};
        private ListView list_callLog;
        private String selectMode = "";
        private Button btnRemoveAll, btnCheckRemove, btnOption, btnRemove, btnCancel;
        private LinearLayout layoutOptionDialog;
        private boolean isShow = false, isCheckShow = false;
        CallLogsAdapter adapter;
        public boolean isLoadComplete = true;
        private String logtype = "";
        public static final String MESSAGE_TYPE_INBOX = "1";
        public static final String MESSAGE_TYPE_SENT = "2";
        public static final String MESSAGE_TYPE_CONVERSATIONS = "3";
        public static final String MESSAGE_TYPE_NEW = "new";
        private final static String[] CALL_PROJECTION = { "is_read", "logtype", CallLog.Calls._ID, CallLog.Calls.TYPE, CallLog.Calls.CACHED_NAME,
                        CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.DURATION };
        private TextView tvTitle = null;
        private RelativeLayout layoutNormal, layoutSelect;
        private String oldFirstPhoneNumber = "";
        private String oldFirstCallTime = "";
        private String oldLastPhoneNumber = "";
        private String oldLastCallTime = "";
        private String oldSelect = "";
        private int oldCount = -1;
        private final String[] searchType = new String[5];
        private Button btnSearchType;
        private Button[] btnSearchs = new Button[5];
        private PopupWindow searchTypePopup = null;
        public ArrayList<CallLogData> callLogDetail = null;

        private Cursor getCallHistoryCursor( Context context )
        {
                Cursor cursor = context.getContentResolver().query( CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DEFAULT_SORT_ORDER );
                return cursor;
        }

        public static CallView instance()
        {
                if ( callViewInstance == null ) callViewInstance = new CallView();
                return callViewInstance;
        }

        @Override
        public void onDestroy()
        {
                super.onDestroy();
                callViewInstance = null;
        }

        @Override
        public void onResume()
        {
                super.onResume();
                callLog();
                isLoadComplete = true;
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                this.inflater = inflater;
                view = inflater.inflate( R.layout.activity_call, null );
                list_callLog = ( ListView ) view.findViewById( R.id.calllog_list );
                /*
                 * spin = ( Spinner ) view.findViewById( R.id.calllog_spinner );
                 * spin.setOnItemSelectedListener( this );
                 * ArrayAdapter<String> aa = new ArrayAdapter<String>(
                 * getActivity(), android.R.layout.simple_spinner_item, items );
                 * spin.setAdapter( aa );
                 */
                // 2015-05-10
                btnSearchType = ( Button ) view.findViewById( R.id.calllog_searchTypes );
                btnSearchType.setOnClickListener( this );
                btnSearchType.setTypeface( Define.tfRegular );
                searchType[0] = getString( R.string.call_all );
                searchType[1] = getString( R.string.call_in );
                searchType[2] = getString( R.string.call_out );
                searchType[3] = getString( R.string.call_missed );
                searchType[4] = getString( R.string.call_refusal );
                
                //
                list_callLog.setOnScrollListener( this );
                items[0] = getString( R.string.call_all );
                items[1] = getString( R.string.call_in );
                items[2] = getString( R.string.call_out );
                items[3] = getString( R.string.call_missed );
                items[4] = getString( R.string.call_refusal );
                
                selectMode = items[0];
                btnRemoveAll = ( Button ) view.findViewById( R.id.calllog_delete );
                btnRemoveAll.setOnClickListener( this );
                btnRemoveAll.setTypeface( Define.tfRegular );
                layoutOptionDialog = ( LinearLayout ) view.findViewById( R.id.calllog_option_dialog );
                layoutOptionDialog.setVisibility( View.GONE );
                btnCheckRemove = ( Button ) view.findViewById( R.id.calllog_checkremove );
                btnCheckRemove.setOnClickListener( this );
                btnCheckRemove.setTypeface( Define.tfRegular );
                btnOption = ( Button ) view.findViewById( R.id.calllog_option );
                btnOption.setOnClickListener( this );
                btnRemove = ( Button ) view.findViewById( R.id.calllog_remove );
                btnRemove.setOnClickListener( this );
                btnRemove.setVisibility( View.GONE );
                btnRemove.setTypeface( Define.tfRegular );
                btnCancel = ( Button ) view.findViewById( R.id.calllog_cancel );
                btnCancel.setOnClickListener( this );
                btnCancel.setVisibility( View.GONE );
                btnCancel.setTypeface( Define.tfRegular );
                layoutNormal = ( RelativeLayout ) view.findViewById( R.id.calllog_tab_normal );
                layoutSelect = ( RelativeLayout ) view.findViewById( R.id.calllog_tab_selected );
                tvTitle = ( TextView ) view.findViewById( R.id.calllog_title );
                tvTitle.setTypeface( Define.tfMedium );
                ArrayList<CallLogData> callLogArray = new ArrayList<CallLogData>();
                callLogArray.clear();
                adapter = new CallLogsAdapter( getActivity(), R.layout.calllog_item, callLogArray );
                list_callLog.setAdapter( adapter );
                return view;
        }
        
        class ResetCallLog extends Thread
        {
                public ResetCallLog()
                {
                        this.start();
                }

                public void run()
                {
                        if ( !isLoadComplete ) return;
                        isLoadComplete = false;
                        Long photoId = ( long ) 0;
                        int callcount = 0;
                        String callname = "";
                        String calltype = "";
                        String calllog = "";
                        String callNumber = "";
                        //Cursor curCallLog = null;
                        try
                        {
//                                curCallLog = getCallHistoryCursor( getActivity() );
//                                if ( curCallLog.getCount() <= 0 )
//                                {
//                                        Message msg = callViewHandler.obtainMessage( Define.AM_REFRESH );
//                                        callViewHandler.sendMessage( msg );
//                                        return;
//                                }
//                                curCallLog.moveToFirst();
//                                String tmpFirstPhoneNumber = curCallLog.getString( curCallLog.getColumnIndex( CallLog.Calls.NUMBER ) );
//                                String tmpFirstCallTime = curCallLog.getString( curCallLog.getColumnIndex( CallLog.Calls.DATE ) );
//                                curCallLog.moveToLast();
//                                String tmpLastPhoneNumber = curCallLog.getString( curCallLog.getColumnIndex( CallLog.Calls.NUMBER ) );
//                                String tmpLastCallTime = curCallLog.getString( curCallLog.getColumnIndex( CallLog.Calls.DATE ) );
//                                if ( oldFirstPhoneNumber.equals( tmpFirstPhoneNumber ) && oldLastPhoneNumber.equals( tmpLastPhoneNumber )
//                                                && oldFirstCallTime.equals( tmpFirstCallTime ) && oldLastCallTime.equals( tmpLastCallTime )
//                                                && curCallLog.getCount() == oldCount && selectMode.equals( oldSelect ) && !isCheckShow )
//                                {
//                                        isLoadComplete = true;
//                                        if ( curCallLog != null ) curCallLog.close();
//                                        curCallLog = null;
//                                        return;
//                                }
//                                oldFirstPhoneNumber = tmpFirstPhoneNumber;
//                                oldLastPhoneNumber = tmpLastPhoneNumber;
//                                oldFirstCallTime = tmpFirstCallTime;
//                                oldLastCallTime = tmpLastCallTime;
//                                oldCount = curCallLog.getCount();
//                                oldSelect = selectMode;
                        	
                                Message msg = callViewHandler.obtainMessage( Define.AM_REFRESH );
                                callViewHandler.sendMessage( msg );
                                
                                short iSelectType = 0;
                                
                                if ( selectMode.equals(getString( R.string.call_all ) ) ) iSelectType = 0; 
                                else if ( selectMode.equals(getString( R.string.call_in ) ) ) iSelectType = 1;
                                else if ( selectMode.equals(getString( R.string.call_out ) ) ) iSelectType = 2;
                                else if ( selectMode.equals(getString( R.string.call_missed ) ) ) iSelectType = 3;
                                else if ( selectMode.equals(getString( R.string.call_refusal ) ) ) iSelectType = 5;
                                
                                ArrayList<ArrayList<String>> ar = Database.instance(Define.mContext).selectCallLog(iSelectType);
                                
                                Log.d(TAG, "CallLogCount : " + ar.size());
                                
                                for ( int i = 0 ; i < ar.size() ; i++ )
                                {
                                        // String isRead =
                                        // curCallLog.getString(
                                        // curCallLog.getColumnIndex(
                                        // "is_read" ));
                                        logtype = "100";
                                        
                                        ArrayList<String> itemAr = ar.get(i);
                                        
                                        CallLogData acontact = new CallLogData();
                                        acontact.setCallid( itemAr.get(0) );
                                        acontact.setCallDate( itemAr.get(0) );
                                        acontact.setCallStatus( itemAr.get(3) );
                                        callNumber = itemAr.get(1);
                                        acontact.setPhonenum( callNumber );
                                        acontact.setName( itemAr.get(2) );
                                        acontact.setCallDuration( itemAr.get(4) );
                                        acontact.setIsCheck( "false" );
                                        acontact.setLogtype( logtype );
                                        msg = callViewHandler.obtainMessage( Define.AM_ADD_BUDDY_USER, acontact );
                                        callViewHandler.sendMessage( msg );
                                }
                        }
                        catch ( CursorIndexOutOfBoundsException ce )
                        {
                                ce.printStackTrace();
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
                        }
                        
                        list_callLog.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick( AdapterView<?> contactlist, View v, int position, long resid )
                                {
                                        CallLogData callData = ( CallLogData ) contactlist.getItemAtPosition( position );
                                        if ( callData == null ) return;
                                        callLogDetail = new ArrayList<CallLogData>();
                                        for ( int i = 0; i < adapter.getCount(); i++ )
                                        {
                                                if ( adapter.getItem( i ).getPhonenum().equals( callData.getPhonenum() ) ) callLogDetail.add( adapter
                                                                .getItem( i ) );
                                        }
                                        Intent it = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.CallLogDetailView.class );
                                        it.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                        it.putExtra( "obj", "callLogDetail" );
                                        startActivity( it );
                                }
                        } );
                        Message msg = callViewHandler.obtainMessage( Define.AM_COMPLETE );
                        callViewHandler.sendMessage( msg );
                        isLoadComplete = true;
                }
        }
        public Handler callViewHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REFRESH )
                                {
                                        adapter.clear();
                                }
                                else if ( msg.what == Define.AM_ADD_BUDDY_USER )
                                {
                                        CallLogData data = ( CallLogData ) msg.obj;
                                        /*
                                         * boolean isCheck = false;
                                         * if ( adapter.getCount() > 0 ) {
                                         * CallLogData previousData =
                                         * adapter.getItem( adapter.getCount() -
                                         * 1);
                                         * if (
                                         * previousData.getPhonenum().equals(
                                         * data.getPhonenum() ) ) { isCheck =
                                         * true; } }
                                         * if ( !isCheck ) adapter.insert( data,
                                         * adapter.getCount() );
                                         */
                                        // 2015-05-10
                                        adapter.insert( data, adapter.getCount() );
                                }
                                else if ( msg.what == Define.AM_COMPLETE )
                                {
                                		//2016-11-28 HHJ GC 문제 해결
                                		if ( list_callLog.getCount() < 20 )
                                        //if ( adapter.getCount() < 20 )
                                        {
                                			setIdAndPhoto( 0, list_callLog.getCount() - 1 );
                                            //setIdAndPhoto( 0, adapter.getCount() - 1 );
                                        }
                                        else
                                        {
                                            int from = 0;
                                            int to = 0;
                                            
                                            for ( int i = 0; i < list_callLog.getCount(); i++ )
                                            //for ( int i = 0; i < adapter.getCount(); i++ )
                                            {
                                            		if ( list_callLog.getChildAt( i ) != null )
                                                    //if ( adapter.getItem( i ) != null )
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
                                		//
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

        @TargetApi(23)
		@SuppressLint("NewApi")
		public void callLog()
        {
                //2016-05-27
                try
                {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) 
                                getActivity().requestPermissions(new String[]{Manifest.permission.READ_CALL_LOG}, PERMISSIONS_REQUEST_READ_CONTACTS);
                        else
                        //
                                new ResetCallLog();
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
        private class CallLogsAdapter extends ArrayAdapter<CallLogData> implements OnClickListener {
                private int resId;
                private ArrayList<CallLogData> contactlist;
                private LayoutInflater Inflater;
                private Context context;

                public CallLogsAdapter( Context context, int textViewResourceId, List<CallLogData> objects )
                {
                        super( context, textViewResourceId, objects );
                        this.context = context;
                        resId = textViewResourceId;
                        contactlist = ( ArrayList<CallLogData> ) objects;
                        Inflater = ( LayoutInflater ) (( Activity ) context).getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                }

                @Override
                public View getView( int position, View v, ViewGroup parent )
                {
                        ImageView iv_callStatus = null, iv_fmcStatus, iv_divide;
                        UserImageView iv_photoid = null;
                        TextView tv_name = null;
                        TextView tv_phonenumber = null;
                        TextView tv_date = null;
                        Button btn_call = null;
                        Button btn_remove = null;
                        Button btn_chatting = null;
                        LinearLayout layout_info = null;
                        CallLogData acontact = contactlist.get( position );
                        if ( v == null ) v = Inflater.inflate( resId, null );
                        tv_name = ( TextView ) v.findViewById( R.id.calllog_name );
                        tv_phonenumber = ( TextView ) v.findViewById( R.id.calllog_phonenumber );
                        iv_photoid = ( kr.co.ultari.atsmart.basic.control.UserImageView ) v.findViewById( R.id.calllog_photo );
                        iv_callStatus = ( ImageView ) v.findViewById( R.id.calllog_callstatus );
                        iv_fmcStatus = ( ImageView ) v.findViewById( R.id.calllog_fmcstatus );
                        btn_call = ( Button ) v.findViewById( R.id.calllog_call );
                        tv_date = ( TextView ) v.findViewById( R.id.calllog_date );
                        btn_remove = ( Button ) v.findViewById( R.id.calllog_btncheck );
                        if ( !isCheckShow ) btn_remove.setVisibility( View.GONE );
                        else btn_remove.setVisibility( View.VISIBLE );
                        btn_chatting = ( Button ) v.findViewById( R.id.calllog_chatting );
                        layout_info = ( LinearLayout ) v.findViewById( R.id.calllog_info );
                        tv_name.setTag( acontact );
                        tv_name.setOnClickListener( this );
                        tv_phonenumber.setTag( acontact );
                        tv_phonenumber.setOnClickListener( this );
                        btn_call.setTag( acontact );
                        btn_call.setOnClickListener( this );
                        tv_date.setTag( acontact );
                        tv_date.setOnClickListener( this );
                        btn_remove.setTag( acontact );
                        btn_remove.setOnClickListener( this );
                        btn_chatting.setTag( acontact );
                        btn_chatting.setOnClickListener( this );
                        layout_info.setTag( acontact );
                        layout_info.setOnClickListener( this );
                        iv_divide = ( ImageView ) v.findViewById( R.id.calllog_divide );
                        if ( acontact != null )
                        {
                                if ( acontact.getIsCheck().equals( "false" ) ) btn_remove.setBackgroundResource( R.drawable.btn_blackbg_uncheck );
                                else btn_remove.setBackgroundResource( R.drawable.btn_blackbg_checked );
                                tv_name.setTypeface( Define.tfRegular );
                                if ( acontact.userId == null )
                                {
                                        tv_name.setText( acontact.getName() );
                                        if ( !isCheckShow )
                                        {
                                                btn_call.setVisibility( View.VISIBLE ); 
                                                iv_divide.setVisibility( View.VISIBLE );
                                        }
                                        else
                                        {
                                                btn_call.setVisibility( View.GONE );
                                                iv_divide.setVisibility( View.GONE );
                                        }
                                }
                                else
                                {
                                        tv_name.setText( acontact.getName() );
                                        iv_divide.setVisibility( View.VISIBLE );
                                        if ( !isCheckShow )
                                        {
                                                btn_call.setVisibility( View.VISIBLE ); 
                                                btn_chatting.setVisibility( View.VISIBLE );
                                                iv_divide.setVisibility( View.VISIBLE );
                                        }
                                        else
                                        {
                                                iv_divide.setVisibility( View.GONE );
                                                btn_call.setVisibility( View.GONE );
                                                btn_chatting.setVisibility( View.GONE );
                                        }
                                }
                                if ( acontact.getLogtype().equals( "800" ) ) iv_fmcStatus.setVisibility( View.VISIBLE );
                                else if ( acontact.getLogtype().equals( "100" ) ) iv_fmcStatus.setVisibility( View.GONE );
                                tv_phonenumber.setTypeface( Define.tfRegular );
                                tv_phonenumber.setText( PhoneNumberUtils.formatNumber( acontact.getPhonenum() ) );
                                
                                tv_date.setTypeface( Define.tfRegular );
                                tv_date.setText( StringUtil.getNotifyTime(acontact.getCallDate()) );
                                
                                String status = acontact.getCallStatus();
                                if ( status.equals( Define.CALL_TYPE_INCOMING + "" ) ) iv_callStatus.setBackgroundResource( R.drawable.img_incomingcall );
                                else if ( status.equals( Define.CALL_TYPE_OUTGOING + "" ) ) iv_callStatus.setBackgroundResource( R.drawable.img_sendcall );
                                else if ( status.equals( Define.CALL_TYPE_ABSENT + "" ) ) iv_callStatus.setBackgroundResource( R.drawable.img_missedcall );
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
                        CallLogData data = ( CallLogData ) v.getTag();
                        if ( v.getId() == R.id.calllog_call )
                        {
                                FmcSendBroadcast.FmcSendCall( data.getPhonenum() ,1, getActivity().getApplicationContext()); //2016-03-31
                        }
                        else if ( v.getId() == R.id.calllog_btncheck )
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
                        else if ( v.getId() == R.id.calllog_chatting )
                        {
                                FmcSendBroadcast.FmcSendCall( data.getPhonenum() ,0, getActivity().getApplicationContext()); 
                                
                                /*String userId = data.userId;
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
                                }*/
                        }
                        else if ( v.getId() == R.id.calllog_name || v.getId() == R.id.calllog_phonenumber || v.getId() == R.id.calllog_date
                                        || v.getId() == R.id.calllog_info )
                        {
                                callLogDetail = new ArrayList<CallLogData>();
                                for ( int i = 0; i < contactlist.size(); i++ )
                                {
                                        if ( contactlist.get( i ).getPhonenum().equals( data.getPhonenum() ) )
                                        {
                                                //Log.d( "CallView", "find str:" + data.getPhonenum() + ", i:" + i + ", item phone:" + contactlist.get( i ).getPhonenum() + ", photoId:" + contactlist.get( i ).getPhotoid() );
                                                callLogDetail.add( contactlist.get( i ) );
                                        }
                                }
                                Intent it = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.CallLogDetailView.class );
                                it.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                it.putExtra( "obj", "callLogDetail" );
                                startActivity( it );
                        }
                }
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
                        selectMode = name;
                        Log.e(TAG, "updateSearchName:"+ name);
                        callLog();
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        @Override
        public void onClick( View v )
        {
                // 2015-05-10
                if ( searchTypePopup != null && v.getId() != R.id.searchTypeAllRecord && v.getId() != R.id.searchTypeReceiveRecord
                                && v.getId() != R.id.searchTypeSendRecord && v.getId() != R.id.searchTypeMissedRecord && v.getId() != R.id.searchTypeRefusalRecord )
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
                if ( v.getId() == R.id.calllog_searchTypes )
                {
                        View popupView = inflater.inflate( R.layout.searchtype_calllog, null );
                        // searchTypePopup = new PopupWindow(popupView,
                        // LayoutParams.WRAP_CONTENT,
                        // LayoutParams.WRAP_CONTENT);
                        searchTypePopup = new PopupWindow( popupView, Define.getDpFromPx( getActivity(), 300 ), LayoutParams.WRAP_CONTENT );
                        btnSearchs[0] = ( Button ) popupView.findViewById( R.id.searchTypeAllRecord );
                        btnSearchs[1] = ( Button ) popupView.findViewById( R.id.searchTypeReceiveRecord );
                        btnSearchs[2] = ( Button ) popupView.findViewById( R.id.searchTypeSendRecord );
                        btnSearchs[3] = ( Button ) popupView.findViewById( R.id.searchTypeMissedRecord );
                        btnSearchs[4] = ( Button ) popupView.findViewById( R.id.searchTypeRefusalRecord );
                        btnSearchs[0].setTypeface( Define.tfRegular );
                        btnSearchs[1].setTypeface( Define.tfRegular );
                        btnSearchs[2].setTypeface( Define.tfRegular );
                        btnSearchs[3].setTypeface( Define.tfRegular );
                        btnSearchs[4].setTypeface( Define.tfRegular );
                        for ( int i = 0; i < btnSearchs.length; i++ )
                                btnSearchs[i].setOnClickListener( this );
                        updateSearchPopup( btnSearchType.getText().toString() );
                        searchTypePopup.showAsDropDown( btnSearchType, 0, 0 );
                }
                else if ( v != null && v.getId() == R.id.searchTypeAllRecord )
                {
                        updateSearchName( btnSearchs[0].getText().toString() );
                }
                else if ( btnSearchs[1] != null && v.getId() == R.id.searchTypeReceiveRecord )
                {
                        updateSearchName( btnSearchs[1].getText().toString() );
                }
                else if ( btnSearchs[2] != null && v.getId() == R.id.searchTypeSendRecord )
                {
                        updateSearchName( btnSearchs[2].getText().toString() );
                }
                else if ( btnSearchs[3] != null && v.getId() == R.id.searchTypeMissedRecord )
                {
                        updateSearchName( btnSearchs[3].getText().toString() );
                }
                else if ( btnSearchs[4] != null && v.getId() == R.id.searchTypeRefusalRecord )
                {
                        updateSearchName( btnSearchs[4].getText().toString() );
                }
                //
                else if ( v.getId() == R.id.calllog_delete )
                {
                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder( MainActivity.Instance() );
                        alert_confirm.setTitle( getActivity().getString( R.string.record_del ) );
                        alert_confirm.setMessage( getActivity().getString( R.string.record_del_msg ) ).setCancelable( false )
                                        .setPositiveButton( getActivity().getString( R.string.ok ), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick( DialogInterface dialog, int which )
                                                {
                                                        getActivity().getContentResolver().delete( CallLog.Calls.CONTENT_URI, null, null );
                                                        isLoadComplete = true;
                                                        Database.instance( getActivity() ).deleteAllCallLog(); //2016-12-13
                                                        callLog();
                                                        dialog.dismiss();
                                                }
                                        } ).setNegativeButton( getActivity().getString( R.string.cancel ), new DialogInterface.OnClickListener() {
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
                else if ( v.getId() == R.id.calllog_checkremove )
                {
                        btnCancel.setVisibility( View.VISIBLE );
                        btnRemove.setVisibility( View.VISIBLE );
                        layoutOptionDialog.setVisibility( View.GONE );
                        isCheckShow = true;
                        isLoadComplete = true;
                        if ( ActionManager.tabs != null ) ActionManager.tabs.handler.sendEmptyMessageDelayed( Define.AM_TAB_HIDE, 100 );
                        layoutSelect.setVisibility( View.VISIBLE );
                        layoutNormal.setVisibility( View.GONE );
                        callLog();
                }
                else if ( v.getId() == R.id.calllog_option )
                {
                        isShow = !isShow;
                        if ( isShow ) layoutOptionDialog.setVisibility( View.VISIBLE );
                        else layoutOptionDialog.setVisibility( View.GONE );
                        btnCancel.setVisibility( View.GONE );
                        btnRemove.setVisibility( View.GONE );
                        isCheckShow = false;
                        isLoadComplete = false;
                }
                else if ( v.getId() == R.id.calllog_remove )
                {
                        for ( int i = 0; i < adapter.getCount(); i++ )
                        {
                                if ( adapter.getItem( i ).getIsCheck().equals( "true" ) )
                                {
                                        // ContentValues values = new
                                        // ContentValues();
                                        // String[] fv = new String[] {
                                        // adapter.getItem( i ).getName() };
                                        // values.put( Contacts.STARRED, 0 );
                                        // getActivity().getContentResolver().update(
                                        // Contacts.CONTENT_URI, values,
                                        // Contacts.DISPLAY_NAME + "= ?", fv );
                                        String queryString = CallLog.Calls._ID + "=" + "'" + adapter.getItem( i ).getCallid() + "'";
                                        getActivity().getContentResolver().delete( CallLog.Calls.CONTENT_URI, queryString, null );
                                        Database.instance( getActivity() ).deleteCallLog( adapter.getItem( i ).getCallid() ); //2016-12-13
                                }
                        }
                        btnCancel.setVisibility( View.GONE );
                        btnRemove.setVisibility( View.GONE );
                        layoutOptionDialog.setVisibility( View.GONE );
                        isCheckShow = false;
                        if ( ActionManager.tabs != null ) ActionManager.tabs.handler.sendEmptyMessageDelayed( Define.AM_TAB_SHOW, 100 );
                        layoutNormal.setVisibility( View.VISIBLE );
                        layoutSelect.setVisibility( View.GONE );
                        callLog();
                }
                else if ( v.getId() == R.id.calllog_cancel )
                {
                        for ( int i = 0; i < adapter.getCount(); i++ )
                        {
                                if ( adapter.getItem( i ).getIsCheck().equals( "true" ) ) adapter.getItem( i ).setIsCheck( "false" );
                        }
                        btnCancel.setVisibility( View.GONE );
                        btnRemove.setVisibility( View.GONE );
                        layoutOptionDialog.setVisibility( View.GONE );
                        isCheckShow = false;
                        if ( ActionManager.tabs != null ) ActionManager.tabs.handler.sendEmptyMessageDelayed( Define.AM_TAB_SHOW, 100 );
                        layoutNormal.setVisibility( View.VISIBLE );
                        layoutSelect.setVisibility( View.GONE );
                        // isLoadComplete = true;
                        callLog();
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
                setIdAndPhoto( list_callLog.getFirstVisiblePosition(), list_callLog.getLastVisiblePosition() );
        }

        public void setIdAndPhoto( int from, int to )
        {
                if ( from >= to ) return;
                StringBuffer checkIds = new StringBuffer();
                for ( int i = from; i <= to; i++ )
                {
                        CallLogData data = ( CallLogData ) list_callLog.getAdapter().getItem( i );
                        if ( data.userId == null ) checkIds.append( "\t" + data.getPhonenum() );
                }
                
                if ( checkIds.length() > 0 ) new GetUserInfoThread( checkIds.toString(), list_callLog.getAdapter() );
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
                                                //Log.d( "CallView", "getUserInfoThread rcvStr:"+rcvStr );
                                                //getUserInfoThread rcvStr:SETID        01051704572     kimsh   김상훈#대리#개발2팀#3011##01051704572#kimsh##1
                                                
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
                                        for ( int i = list_callLog.getFirstVisiblePosition(); i <= list_callLog.getLastVisiblePosition(); i++ )
                                        {
                                                CallLogData data = ( CallLogData ) list_callLog.getItemAtPosition( i );
                                                if ( param.size() > 1 && data.getPhonenum().equals( param.get( 0 ) ) )
                                                {
                                                        data.userId = param.get( 1 );
                                                        if ( param.size() == 3 ) data.userName = param.get( 2 );
                                                        
                                                        //SET_ID 0:3405, 1:samsung2, 2:삼성2##전산정보팀#3898#3405#010-3251-8941#
                                                        //SET_ID 0:01045586180, 1:a0701008, 2:금봉권#과장#전산정보팀#4244##010-4558-6180#3405#
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
                View v = list_callLog.getChildAt( position - list_callLog.getFirstVisiblePosition() );
                if ( v == null ) return;
                CallLogData acontact = adapter.getItem( position );
                
                UserImageView iv_photoid = ( UserImageView ) v.findViewById( R.id.calllog_photo );
                if ( acontact != null )
                {
                        if(acontact.getLogtype().equals( "800" ))
                        {
                                if(acontact.userName != null)
                                {
                                        String[] parse = acontact.userName.split( "#",-1 );
                                        if(parse != null)
                                        {
                                                TextView tv_name = ( TextView ) v.findViewById( R.id.calllog_name );
                                                tv_name.setText( parse[0] + " " + parse[1] );
                                        }
                                }
                        }
                        
                        if ( acontact.userId != null && !acontact.userId.equals( "" ) )
                        {
                                iv_photoid.setUserIdOval( acontact.userId, false );
                        }
                        else
                        {
                                Bitmap bmp = acontact.getBitmap();
                                if ( bmp != null ) iv_photoid.setImageBitmap( ImageUtil.getDrawOval(acontact.getBitmap()));
                                else 
                                {
                                        Bitmap pic = BitmapFactory.decodeResource( getResources(), R.drawable.img_profile_100x100 );
                                        iv_photoid.setImageBitmap( ImageUtil.getDrawOval( pic ) );
                                }
                                //else iv_photoid.setImageDrawable( getResources().getDrawable( R.drawable.img_profile_100x100 ) );
                        }
                }
        }
}
