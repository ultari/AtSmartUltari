package kr.co.ultari.atsmart.basic.subview;

import java.io.InputStream;
import java.util.ArrayList;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.CallLogData;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.subdata.FavoriteData;
import kr.co.ultari.atsmart.basic.util.CallLogMoreDialog;
import kr.co.ultari.atsmart.basic.util.FmcSendBroadcast;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.view.CallView;
import kr.co.ultari.atsmart.basic.view.FavoriteView;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CallLogDetailView extends MessengerActivity implements OnClickListener {
        private Button btnRemove, btnAddContact, btnCall, btnMore, btnChat, btnOption, btnSms, btnFmcCall;
        private TextView tvName, tvAddMsg, tvPhone, tvTitle1, tvDate1, tvDuration1, tvTitle2, tvDate2, tvDuration2, tvTitle3, tvDate3, tvDuration3;
        private ImageView ivIcon1, ivIcon2, ivIcon3;
        private boolean isShow, isAddFavorite;
        private ArrayList<CallLogData> callArray = null;
        private String name = null, phoneNumber = null, userId = null, date, duration, status, type, orgUserName, userName, pNumber;
        private long photoId;
        private UserImageView iv_photoid = null;
        private TextView tvNumberTitle, tvRecordTitle;
        private LinearLayout layoutOption;

        @SuppressWarnings( "unchecked" )
        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
                setContentView( R.layout.calllog_detail );
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE ); //2016-12-13
                tvNumberTitle = ( TextView ) findViewById( R.id.calllog_number_title );
                tvRecordTitle = ( TextView ) findViewById( R.id.calllog_record_title );
                tvNumberTitle.setTypeface( Define.tfRegular );
                tvRecordTitle.setTypeface( Define.tfRegular );
                if ( getIntent() != null )
                {
                        callArray = CallView.instance().callLogDetail;
                        name = callArray.get( 0 ).getName();
                        if ( callArray.get( 0 ).userId == null ) name = callArray.get( 0 ).getName();
                        else
                        {
                                userId = callArray.get( 0 ).userId;
                                orgUserName = callArray.get( 0 ).getOrgUserName();
                                userName = callArray.get( 0 ).userName;
                        }
                        phoneNumber = callArray.get( 0 ).getPhonenum();
                        date = callArray.get( 0 ).getCallDate();
                        duration = callArray.get( 0 ).getCallDuration();
                        status = callArray.get( 0 ).getCallStatusString();
                        photoId = callArray.get( 0 ).getPhotoid();
                        type = callArray.get( 0 ).getIsCheck();
                }
                
                btnChat = ( Button ) findViewById( R.id.calllog_detail_chat );
                btnChat.setOnClickListener( this );
                if ( userId == null )
                        btnChat.setBackgroundResource( R.drawable.icon_chat_none );
                else
                        btnChat.setBackgroundResource( R.drawable.icon_chat );
                
                btnRemove = ( Button ) findViewById( R.id.calllog_detail_remove );
                btnRemove.setOnClickListener( this );
                btnRemove.setTypeface( Define.tfRegular );

                layoutOption = ( LinearLayout ) findViewById( R.id.calllog_detail_option_dialog );
                btnOption = ( Button ) findViewById( R.id.calllog_detail_option );
                btnOption.setOnClickListener( this );
                btnOption.setTypeface( Define.tfRegular );
                btnAddContact = ( Button ) findViewById( R.id.calllog_detail_add_contact );
                btnAddContact.setOnClickListener( this );
                btnCall = ( Button ) findViewById( R.id.calllog_detail_call );
                btnCall.setOnClickListener( this );
                btnMore = ( Button ) findViewById( R.id.calllog_detail_more );
                btnMore.setOnClickListener( this );
                btnMore.setTypeface( Define.tfRegular );
                
                btnSms = ( Button ) findViewById( R.id.calllog_detail_sms );
                btnSms.setOnClickListener( this );
                
                btnFmcCall = ( Button ) findViewById( R.id.calllog_detail_fmc_call );
                btnFmcCall.setOnClickListener( this );
                
                ivIcon1 = ( ImageView ) findViewById( R.id.callog_detail_icon1 );
                ivIcon2 = ( ImageView ) findViewById( R.id.callog_detail_icon2 );
                ivIcon3 = ( ImageView ) findViewById( R.id.callog_detail_icon3 );
                tvName = ( TextView ) findViewById( R.id.keypad_addcontact_title );
                tvAddMsg = ( TextView ) findViewById( R.id.calllog_detail_add_msg );
                tvPhone = ( TextView ) findViewById( R.id.calllog_detail_telephone );
                tvTitle1 = ( TextView ) findViewById( R.id.calllog_detail_title1 );
                tvDate1 = ( TextView ) findViewById( R.id.calllog_detail_date1 );
                tvDuration1 = ( TextView ) findViewById( R.id.calllog_detail_duration1 );
                tvTitle2 = ( TextView ) findViewById( R.id.calllog_detail_title2 );
                tvDate2 = ( TextView ) findViewById( R.id.calllog_detail_date2 );
                tvDuration2 = ( TextView ) findViewById( R.id.calllog_detail_duration2 );
                tvTitle3 = ( TextView ) findViewById( R.id.calllog_detail_title3 );
                tvDate3 = ( TextView ) findViewById( R.id.calllog_detail_date3 );
                tvDuration3 = ( TextView ) findViewById( R.id.calllog_detail_duration3 );
                int size = callArray.size();
                tvName.setTypeface( Define.tfRegular );
                tvPhone.setTypeface( Define.tfRegular );
                tvName.setText( callArray.get( 0 ).getName() );
                
                pNumber = PhoneNumberUtils.formatNumber( phoneNumber );
                if(pNumber == null || pNumber.equals( "" ))
                {
                        btnSms.setBackgroundResource( R.drawable.icon_message_none );
                        tvPhone.setText( "" );
                        btnCall.setBackgroundResource( R.drawable.icon_call_none );
                        btnFmcCall.setBackgroundResource( R.drawable.icon_call_fmc_none );
                }
                else
                {
                        btnSms.setBackgroundResource( R.drawable.icon_message );
                        tvPhone.setText( pNumber );
                        btnCall.setBackgroundResource( R.drawable.icon_call );
                        btnFmcCall.setBackgroundResource( R.drawable.icon_call_fmc );
                }
                
                if ( userId == null )
                {
                        boolean isExits = false;
                        if ( Define.contactArray != null )
                        {
                                for ( Contact data : Define.contactArray )
                                {
                                        if ( data.getPhonenum().replaceAll( "-", "" ).equals( phoneNumber ) )
                                        {
                                                isExits = true;
                                                break;
                                        }
                                }
                        }
                        if ( !isExits )
                        {
                                tvAddMsg.setTypeface( Define.tfRegular );
                                tvAddMsg.setText( getString( R.string.new_contact ) );
                                btnAddContact.setBackgroundResource( R.drawable.btn_add_contact );
                        }
                        else
                        {
                                isAddFavorite = false;
                                int count = FavoriteView.instance().adapter.getCount();
                                String phone = phoneNumber.replaceAll( "-", "" );
                                for ( int i = 0; i < count; i++ )
                                {
                                        if ( FavoriteView.instance().adapter.getItem( i ).getPhonenum().replaceAll( "-", "" ).equals( phone ) )
                                        {
                                                isAddFavorite = true;
                                                break;
                                        }
                                }
                                tvAddMsg.setTypeface( Define.tfRegular );
                                if ( isAddFavorite )
                                {
                                        tvAddMsg.setText( getString( R.string.bookmark ) );
                                        tvAddMsg.setTextColor( 0xFFFFFFFF );
                                        btnAddContact.setBackgroundResource( R.drawable.detailinfo_favories );
                                }
                                else
                                {
                                        tvAddMsg.setText( getString( R.string.bookmark ) );
                                        tvAddMsg.setTextColor( 0xFF333333 );
                                        btnAddContact.setBackgroundResource( R.drawable.detailinfo_favories_act );
                                }
                        }
                }
                else
                {
                        isAddFavorite = false;
                        int count = FavoriteView.instance().adapter.getCount();
                        String phone = phoneNumber.replaceAll( "-", "" );
                        for ( int i = 0; i < count; i++ )
                        {
                                if ( FavoriteView.instance().adapter.getItem( i ).getPhonenum().replaceAll( "-", "" ).equals( phone )
                                                && FavoriteView.instance().adapter.getItem( i ).getPhotoid() == photoId )
                                {
                                        isAddFavorite = true;
                                        break;
                                }
                        }
                        tvAddMsg.setTypeface( Define.tfRegular );
                        if ( isAddFavorite )
                        {
                                tvAddMsg.setText( getString( R.string.bookmark ) );
                                btnAddContact.setBackgroundResource( R.drawable.detailinfo_favories ); 
                        }
                        else
                        {
                                tvAddMsg.setText( getString( R.string.bookmark ) );
                                btnAddContact.setBackgroundResource( R.drawable.detailinfo_favories_act ); 
                        }
                }
                tvTitle1.setTypeface( Define.tfRegular );
                tvDate1.setTypeface( Define.tfRegular );
                tvDuration1.setTypeface( Define.tfRegular );
                tvTitle2.setTypeface( Define.tfRegular );
                tvDate2.setTypeface( Define.tfRegular );
                tvDuration2.setTypeface( Define.tfRegular );
                tvTitle3.setTypeface( Define.tfRegular );
                tvDate3.setTypeface( Define.tfRegular );
                tvDuration3.setTypeface( Define.tfRegular );
                if ( size == 0 )
                {
                        ivIcon1.setVisibility( View.INVISIBLE );
                        ivIcon2.setVisibility( View.INVISIBLE );
                        ivIcon3.setVisibility( View.INVISIBLE );
                }
                else if ( size == 1 )
                {
                        ivIcon1.setVisibility( View.VISIBLE );
                        ivIcon2.setVisibility( View.INVISIBLE );
                        ivIcon3.setVisibility( View.INVISIBLE );
                        if ( status.equals( getString( R.string.call_in ) ) ) ivIcon1.setBackgroundResource( R.drawable.img_incomingcall );
                        else if ( status.equals( getString( R.string.call_out ) ) ) ivIcon1.setBackgroundResource( R.drawable.img_sendcall );
                        else if ( status.equals( getString( R.string.call_missed ) ) ) ivIcon1.setBackgroundResource( R.drawable.img_missedcall );
                        duration = getDateStr( duration );
                        if ( status != null ) tvTitle1.setText( status );
                        if ( date != null ) tvDate1.setText( StringUtil.getNotifyTime(date) );
                        if ( duration != null ) tvDuration1.setText( duration );
                }
                else if ( size == 2 )
                {
                        ivIcon1.setVisibility( View.VISIBLE );
                        ivIcon2.setVisibility( View.VISIBLE );
                        ivIcon3.setVisibility( View.INVISIBLE );
                        if ( status.equals( getString( R.string.call_in ) ) ) ivIcon1.setBackgroundResource( R.drawable.img_incomingcall );
                        else if ( status.equals( getString( R.string.call_out ) ) ) ivIcon1.setBackgroundResource( R.drawable.img_sendcall );
                        else if ( status.equals( getString( R.string.call_missed ) ) ) ivIcon1.setBackgroundResource( R.drawable.img_missedcall );
                        duration = getDateStr( duration );
                        if ( status != null ) tvTitle1.setText( status );
                        if ( date != null ) tvDate1.setText( StringUtil.getNotifyTime(date) );
                        if ( duration != null ) tvDuration1.setText( duration );
                        status = callArray.get( 1 ).getCallStatusString();
                        date = callArray.get( 1 ).getCallDate();
                        duration = callArray.get( 1 ).getCallDuration();
                        if ( status.equals( getString( R.string.call_in ) ) ) ivIcon2.setBackgroundResource( R.drawable.img_incomingcall );
                        else if ( status.equals( getString( R.string.call_out ) ) ) ivIcon2.setBackgroundResource( R.drawable.img_sendcall );
                        else if ( status.equals( getString( R.string.call_missed ) ) ) ivIcon2.setBackgroundResource( R.drawable.img_missedcall );
                        duration = getDateStr( duration );
                        if ( status != null ) tvTitle2.setText( status );
                        if ( date != null ) tvDate2.setText( StringUtil.getNotifyTime(date) );
                        if ( duration != null ) tvDuration2.setText( duration );
                }
                else
                {
                        if ( status.equals( getString( R.string.call_in ) ) ) ivIcon1.setBackgroundResource( R.drawable.img_incomingcall );
                        else if ( status.equals( getString( R.string.call_out ) ) ) ivIcon1.setBackgroundResource( R.drawable.img_sendcall );
                        else if ( status.equals( getString( R.string.call_missed ) ) ) ivIcon1.setBackgroundResource( R.drawable.img_missedcall );
                        duration = getDateStr( duration );
                        if ( status != null ) tvTitle1.setText( status );
                        if ( date != null ) tvDate1.setText( StringUtil.getNotifyTime(date) );
                        if ( duration != null ) tvDuration1.setText( duration );
                        status = callArray.get( 1 ).getCallStatusString();
                        date = callArray.get( 1 ).getCallDate();
                        duration = callArray.get( 1 ).getCallDuration();
                        if ( status.equals( getString( R.string.call_in ) ) ) ivIcon2.setBackgroundResource( R.drawable.img_incomingcall );
                        else if ( status.equals( getString( R.string.call_out ) ) ) ivIcon2.setBackgroundResource( R.drawable.img_sendcall );
                        else if ( status.equals( getString( R.string.call_missed ) ) ) ivIcon2.setBackgroundResource( R.drawable.img_missedcall );
                        duration = getDateStr( duration );
                        if ( status != null ) tvTitle2.setText( status );
                        if ( date != null ) tvDate2.setText( StringUtil.getNotifyTime(date) );
                        if ( duration != null ) tvDuration2.setText( duration );
                        status = callArray.get( 2 ).getCallStatusString();
                        date = callArray.get( 2 ).getCallDate();
                        duration = callArray.get( 2 ).getCallDuration();
                        if ( status.equals( getString( R.string.call_in ) ) ) ivIcon3.setBackgroundResource( R.drawable.img_incomingcall );
                        else if ( status.equals( getString( R.string.call_out ) ) ) ivIcon3.setBackgroundResource( R.drawable.img_sendcall );
                        else if ( status.equals( getString( R.string.call_missed ) ) ) ivIcon3.setBackgroundResource( R.drawable.img_missedcall );
                        duration = getDateStr( duration );
                        if ( status != null ) tvTitle3.setText( status );
                        if ( date != null ) tvDate3.setText( StringUtil.getNotifyTime(date) );
                        if ( duration != null ) tvDuration3.setText( duration );
                }
                iv_photoid = ( UserImageView ) findViewById( R.id.calllog_detail_photo );
                if ( userId != null && !userId.equals( "" ) ) iv_photoid.setUserId( userId );
                else
                {
                        Bitmap bm = openPhoto( photoId );
                        if ( bm != null ) iv_photoid.setImageBitmap( bm );
                        else iv_photoid.setImageDrawable( getResources().getDrawable( R.drawable.img_contract_list ) );
                }
                isShow = false;
        }

        private String getDateStr( String value )
        {
                String ret = "";
                int totalSec = Integer.parseInt( value );
                int day = totalSec / (60 * 60 * 24);
                int hour = (totalSec - day * 60 * 60 * 24) / (60 * 60);
                int minute = (totalSec - day * 60 * 60 * 24 - hour * 3600) / 60;
                int second = totalSec % 60;
                ret = hour + "시간 " + minute + "분 " + second + "초";
                return ret;
        }

        public void onDestroy()
        {
                super.onDestroy();
        }

        @Override
        public void onClick( View v )
        {
                try
                {
                        if ( v.getId() == R.id.calllog_detail_remove )
                        {
                                if ( callArray.size() > 0 )
                                {
                                        String queryString = CallLog.Calls._ID + "=" + "'" + callArray.get( 0 ).getCallid() + "'";
                                        getContentResolver().delete( CallLog.Calls.CONTENT_URI, queryString, null );
                                        Database.instance( getApplicationContext() ).deleteCallLog( callArray.get( 0 ).getCallid() ); //2016-12-13
                                        finish();
                                        FavoriteView.instance().isLoadComplete = true;
                                        FavoriteView.instance().resetData();
                                        CallView.instance().isLoadComplete = true;
                                        CallView.instance().callLog();
                                }
                                layoutOption.setVisibility( View.GONE );
                        }
                        else if ( v.getId() == R.id.calllog_detail_option )
                        {
                                isShow = !isShow;
                                if ( isShow ) layoutOption.setVisibility( View.VISIBLE );
                                else layoutOption.setVisibility( View.GONE );
                        }
                        else if ( v.getId() == R.id.calllog_detail_add_contact )
                        {
                                if ( callArray.size() > 0 )
                                {
                                        if ( userId == null )
                                        {
                                                boolean isExits = false;
                                                for ( Contact data : Define.contactArray )
                                                {
                                                        if ( data.getPhonenum().replaceAll( "-", "" ).equals( phoneNumber ) )
                                                        {
                                                                isExits = true;
                                                                break;
                                                        }
                                                }
                                                if ( !isExits )
                                                {
                                                        Intent selectWindow = new Intent( CallLogDetailView.this,
                                                                        kr.co.ultari.atsmart.basic.subview.ContactAddView.class );
                                                        selectWindow.putExtra( "number", tvPhone.getText().toString().trim().replaceAll( "-", "" ) );
                                                        selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                                        startActivity( selectWindow );
                                                }
                                                else updateFavorite();
                                        }
                                        else updateFavorite();
                                }
                                FavoriteView.instance().isLoadComplete = true;
                                FavoriteView.instance().resetData();
                                CallView.instance().isLoadComplete = true;
                                CallView.instance().callLog();
                                layoutOption.setVisibility( View.GONE );
                        }
                        else if ( v.getId() == R.id.calllog_detail_call )
                        {
                                if ( callArray.size() > 0 )
                                {
                                        FmcSendBroadcast.FmcSendCall( callArray.get( 0 ).getPhonenum() ,1, getApplicationContext()); //2016-03-31
                                }
                                layoutOption.setVisibility( View.GONE );
                        }
                        else if( v.getId() == R.id.calllog_detail_fmc_call)
                        {
                                if ( callArray.size() > 0 )
                                {
                                        FmcSendBroadcast.FmcSendCall( callArray.get( 0 ).getPhonenum() ,0, getApplicationContext()); //2016-03-31
                                }
                                layoutOption.setVisibility( View.GONE );
                        }
                        else if( v.getId() == R.id.calllog_detail_sms )
                        {
                                Uri uri= Uri.parse("smsto:" + pNumber.replaceAll("-","") ); 
                                Intent i= new Intent(Intent.ACTION_SENDTO,uri); 
                                startActivity(i);
                        }
                        else if ( v.getId() == R.id.calllog_detail_more )
                        {
                                if ( callArray.size() > 0 )
                                {
                                        CallLogMoreDialog dialog = new CallLogMoreDialog( CallLogDetailView.this, callArray, name + "\t(" + phoneNumber + ")" );
                                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                                        lp.copyFrom( dialog.getWindow().getAttributes() );
                                        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                                        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
                                        dialog.show();
                                        dialog.getWindow().setAttributes( lp );
                                }
                                layoutOption.setVisibility( View.GONE );
                        }
                        else if ( v.getId() == R.id.calllog_detail_chat )
                        {
                                if ( userId == null ) return;
                                if ( !userId.equalsIgnoreCase( Define.getMyId( getApplicationContext() ) ) )
                                {
                                        String oUserIds = userId + "," + Define.getMyId( getApplicationContext() );
                                        String userIds = StringUtil.arrange( oUserIds );
                                        String userNames = orgUserName + "," + StringUtil.getNamePosition( Define.getMyName() );
                                        userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                                        String roomId = userIds.replace( ",", "_" );
                                        ArrayList<ArrayList<String>> array = Database.instance( getApplicationContext() ).selectChatRoomInfo( roomId );
                                        if ( array.size() == 0 ) Database.instance( getApplicationContext() ).insertChatRoomInfo( roomId, userIds, userNames,
                                                        StringUtil.getNowDateTime(), getString( R.string.newRoom ) );
                                        ActionManager.openChat( getApplicationContext(), roomId, userIds, userNames );
                                }
                                layoutOption.setVisibility( View.GONE );
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        private void updateFavorite()
        {
                if ( isAddFavorite )
                {
                        if ( callArray.get( 0 ).userId == null )
                        {
                                ContentValues values2 = new ContentValues();
                                String[] fv2 = new String[] { name };
                                values2.put( Contacts.STARRED, 0 );
                                getContentResolver().update( Contacts.CONTENT_URI, values2, Contacts.DISPLAY_NAME + "= ?", fv2 );
                                handler.sendEmptyMessageDelayed( Define.AM_FAVORITE_REMOVE, 200 );
                        }
                        else
                        {
                                Database.instance( getApplicationContext() ).deleteFavorite( userId );
                                handler.sendEmptyMessageDelayed( Define.AM_FAVORITE_REMOVE, 200 );
                        }
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                        text.setTypeface( Define.tfRegular );
                        text.setText( getString( R.string.favorite_del_msg ) );
                        Toast toast = new Toast( getApplicationContext() );
                        toast.setGravity( Gravity.CENTER, 0, 0 );
                        toast.setDuration( Toast.LENGTH_SHORT );
                        toast.setView( layout );
                        toast.show();
                }
                else
                {
                        if ( callArray.get( 0 ).userId == null )
                        {
                                ContentValues values2 = new ContentValues();
                                String[] fv2 = new String[] { name };
                                values2.put( Contacts.STARRED, 1 );
                                getContentResolver().update( Contacts.CONTENT_URI, values2, Contacts.DISPLAY_NAME + "= ?", fv2 );
                                handler.sendEmptyMessageDelayed( Define.AM_FAVORITE_ADD, 200 );
                        }
                        else
                        {
                                Database.instance( getApplicationContext() ).insertFavorite( userId, "0", userName, "0", "0", "0" );
                                // Database.instance(getApplicationContext()).insertFavorite( userId,"0", callArray.get( 0 ).userName, "0", "0", "0" );
                                handler.sendEmptyMessageDelayed( Define.AM_FAVORITE_ADD, 200 );
                        }
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                        text.setTypeface( Define.tfRegular );
                        text.setText( getString( R.string.favorite_add_msg ) );
                        Toast toast = new Toast( getApplicationContext() );
                        toast.setGravity( Gravity.CENTER, 0, 0 );
                        toast.setDuration( Toast.LENGTH_SHORT );
                        toast.setView( layout );
                        toast.show();
                        // isAddFavorite = true;
                        // }
                }
        }

        private Bitmap openPhoto( long contactId )
        {
                Uri contactUri = ContentUris.withAppendedId( Contacts.CONTENT_URI, contactId );
                InputStream input = ContactsContract.Contacts.openContactPhotoInputStream( getContentResolver(), contactUri );
                if ( input != null ) return BitmapFactory.decodeStream( input );
                return null;
        }
        public Handler handler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_FAVORITE_REMOVE )
                                {
                                        tvAddMsg.setTypeface( Define.tfRegular );
                                        tvAddMsg.setText( getString( R.string.bookmark ) );
                                        tvAddMsg.setTextColor( 0xFF333333 );
                                        btnAddContact.setBackgroundResource( R.drawable.detailinfo_favories_act ); // 2015-05-12
                                        isAddFavorite = false;
                                }
                                else if ( msg.what == Define.AM_FAVORITE_ADD )
                                {
                                        tvAddMsg.setTypeface( Define.tfRegular );
                                        tvAddMsg.setText( getString( R.string.bookmark ) );
                                        tvAddMsg.setTextColor( 0xFFFFFFFF );
                                        btnAddContact.setBackgroundResource( R.drawable.detailinfo_favories ); // 2015-05-12
                                        isAddFavorite = true;
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };
}
