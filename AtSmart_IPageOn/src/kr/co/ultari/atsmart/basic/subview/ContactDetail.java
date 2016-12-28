package kr.co.ultari.atsmart.basic.subview;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;

import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.GcmManager;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.control.tree.TreeItem;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.subdata.SearchResultItemData;
import kr.co.ultari.atsmart.basic.util.AppUtil;
import kr.co.ultari.atsmart.basic.util.FmcSendBroadcast;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import kr.co.ultari.atsmart.basic.view.AccountView;
import kr.co.ultari.atsmart.basic.view.CallView;
import kr.co.ultari.atsmart.basic.view.ContactView;
import kr.co.ultari.atsmart.basic.view.FavoriteView;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Contacts.People;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Contacts;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

@SuppressLint( "NewApi" )
public class ContactDetail extends MessengerActivity implements OnClickListener, Runnable {
		private static final String TAG = "/AtSmart/ContantDetail";
        private UserImageView iv_UserPhoto;
        private TextView tv_UserName, tv_UserPosition, tv_UserInfo, tv_PhoneNumber, tv_OfficeNumber, tv_email, tv_address, tv_favorite_msg, tv_NumberTitle,
                        tv_EmailTitle, tv_PersonTitle, tv_CompanyTitle, tv_partTitle, tv_jobTitle;
        private Button btn_delete, btn_edit, btn_favorite, btn_chatting, btn_call, btn_office_call, btn_sendEmail, btn_option, btn_contact_save, btn_sms, btn_fmcCall, btn_office_fmcCall, btn_office_chatting;
        private String userPhoneNumber, userName, userEmail, type, userId, deviceName, userOfficeNumber, orgName, orgId, orgPosition, orgCompany, nickName = "", userJob;
        private Long userPhotoId;
        private boolean isAddFavorite = false, isEdit = false;
        private ImageView iv_line;
        private static final int GET_PICTURE_URI = 100, REQ_CODE_PICK_IMAGE = 101;
        private Bitmap tempBitMap = null;
        private LinearLayout layoutOption, layoutContactSave;
        private RelativeLayout layoutTop = null;
        private TextView tvTitle, tv_Part, tv_contact_msg, tv_Job;
        private EditText etEmail, etOffice, etMobile;
        private Button btnCancel, btnSave;
        private String tmpName = "", tmpMobile = "", tmpTelephone = "", tmpEmail = "";
        private boolean isShowDialog;
        private Bitmap selectedImage = null;
        private Thread thread;
        private UltariSSLSocket sc = null;
        private AmCodec codec;
        private boolean onDestroy = false;
        private Contact contact = null;
        private String rawId = null;

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
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                super.onDestroy();
        }

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.contact_detail_layout );
                
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE ); //2016-12-13
                
                try
                {
                        if ( getIntent() != null )
                        {
                                String contactId = getIntent().getStringExtra( "contactId" );
                                rawId = Define.getRawIdWithContactId( contactId );
                                contact = Define.contactMap.get( contactId );
                                userPhotoId = contact.getPhotoid();
                                deviceName = contact.getName();
                                userPhoneNumber = contact.getPhonenum();
                                userEmail = contact.getEmail();
                                type = contact.getType();
                                userId = contact.userId;
                                userName = contact.userName;
                                userOfficeNumber = contact.getTelnum();
                                orgName = contact.getOrgUserName();
                                
                                userJob = "";
                                
                                if ( type.equals( "Org" ) )
                                {
                                        orgId = contact.getUserid();
                                        orgPosition = contact.getPosition();
                                        orgCompany = contact.getCompany();
                                        nickName = contact.getNickName();
                                        userJob = contact.getJob();
                                }

                        }
                        codec = new AmCodec();
                        if ( type.equals( "Org" ) && nickName.equals( "" ) )
                        {
                                thread = new Thread( this );
                                thread.start();
                        }
                        iv_UserPhoto = ( UserImageView ) findViewById( R.id.detail_photo );
                        iv_UserPhoto.setOnClickListener( this );
                        tv_NumberTitle = ( TextView ) findViewById( R.id.detail_number_title );
                        tv_EmailTitle = ( TextView ) findViewById( R.id.detail_email_title );
                        tv_PersonTitle = ( TextView ) findViewById( R.id.detail_person_title );
                        tv_CompanyTitle = ( TextView ) findViewById( R.id.detail_company_title );
                        tv_UserName = ( TextView ) findViewById( R.id.detail_name );
                        tv_UserPosition = ( TextView ) findViewById( R.id.detail_position );
                        tv_UserInfo = ( TextView ) findViewById( R.id.detail_info );
                        tv_PhoneNumber = ( TextView ) findViewById( R.id.detail_phonenumber );
                        tv_OfficeNumber = ( TextView ) findViewById( R.id.detail_office_number );
                        tv_email = ( TextView ) findViewById( R.id.detail_email_input );
                        tv_address = ( TextView ) findViewById( R.id.detail_part );
                        tv_favorite_msg = ( TextView ) findViewById( R.id.detail_favorite_msg );
                        tv_Part = ( TextView ) findViewById( R.id.detail_part );
                        tv_contact_msg = ( TextView ) findViewById( R.id.detail_contact_msg );
                        tv_partTitle = ( TextView ) findViewById( R.id.detail_part_title );
                        
                        tv_Job = ( TextView ) findViewById( R.id.detail_job );
                        tv_Job.setTypeface( Define.tfRegular );
                        tv_jobTitle = ( TextView ) findViewById( R.id.detail_job_title );
                        tv_jobTitle.setTypeface( Define.tfRegular );
                        
                        tv_UserName.setTypeface( Define.tfRegular );
                        tv_UserPosition.setTypeface( Define.tfRegular );
                        tv_UserInfo.setTypeface( Define.tfRegular );
                        tv_PhoneNumber.setTypeface( Define.tfRegular );
                        tv_OfficeNumber.setTypeface( Define.tfRegular );
                        tv_email.setTypeface( Define.tfRegular );
                        tv_address.setTypeface( Define.tfRegular );
                        tv_favorite_msg.setTypeface( Define.tfRegular );
                        tv_NumberTitle.setTypeface( Define.tfRegular );
                        tv_EmailTitle.setTypeface( Define.tfRegular );
                        tv_PersonTitle.setTypeface( Define.tfRegular );
                        tv_CompanyTitle.setTypeface( Define.tfRegular );
                        tv_Part.setTypeface( Define.tfRegular );
                        tv_contact_msg.setTypeface( Define.tfRegular );
                        tv_partTitle.setTypeface( Define.tfRegular );
                        layoutOption = ( LinearLayout ) findViewById( R.id.contact_detail_option_dialog );
                        btn_option = ( Button ) findViewById( R.id.contact_detail_option );
                        btn_option.setOnClickListener( this );
                        btn_option.setTypeface( Define.tfRegular );
                        layoutContactSave = ( LinearLayout ) findViewById( R.id.detail_contact_layout );
                        btn_contact_save = ( Button ) findViewById( R.id.detail_contact_save );
                        btn_contact_save.setOnClickListener( this );
                        if ( type.equals( "Org" ) )
                        {
                                layoutContactSave.setVisibility( View.VISIBLE );
                                btn_option.setVisibility( View.GONE );
                        }
                        else
                        {
                                layoutContactSave.setVisibility( View.GONE );
                                btn_option.setVisibility( View.VISIBLE );
                        }
                        isEdit = false;
                        layoutTop = ( RelativeLayout ) findViewById( R.id.detail_edit_layout );
                        layoutTop.setVisibility( View.GONE );
                        tvTitle = ( TextView ) findViewById( R.id.detail_edit_title );
                        etEmail = ( EditText ) findViewById( R.id.detail_email_editbox );
                        etOffice = ( EditText ) findViewById( R.id.detail_office_editbox );
                        etMobile = ( EditText ) findViewById( R.id.detail_mobile_editbox );
                        etEmail.setVisibility( View.GONE );
                        etMobile.setVisibility( View.GONE );
                        btnCancel = ( Button ) findViewById( R.id.detail_edit_cancel );
                        btnCancel.setOnClickListener( this );
                        btnSave = ( Button ) findViewById( R.id.detail_edit_save );
                        btnSave.setOnClickListener( this );
                        tvTitle.setTypeface( Define.tfMedium );
                        etEmail.setTypeface( Define.tfRegular );
                        etOffice.setTypeface( Define.tfRegular );
                        etMobile.setTypeface( Define.tfRegular );
                        btnCancel.setTypeface( Define.tfRegular );
                        btnSave.setTypeface( Define.tfRegular );
                        if ( userName != null )
                        {
                                String[] parse = userName.split( "#" );
                                tv_UserPosition.setText( parse[1]);
                                tv_Job.setText( userJob );
                        }
                        else
                        {
                                tv_UserPosition.setText( orgPosition );
                        }
                        tv_OfficeNumber.setTypeface( Define.tfRegular );
                        tv_PhoneNumber.setTypeface( Define.tfRegular );
                        tv_email.setTypeface( Define.tfRegular );
                        tv_Part.setTypeface( Define.tfRegular );
                        tv_UserInfo.setText( nickName );
                        tv_OfficeNumber.setText( PhoneNumberUtils.formatNumber( userOfficeNumber ) );
                        tv_PhoneNumber.setText( PhoneNumberUtils.formatNumber( userPhoneNumber ) );
                        tv_UserName.setText( deviceName );
                        tv_email.setText( userEmail );
                        tv_Part.setText( orgCompany );
                       
                        if ( type.equals( "Org" ) ) iv_UserPhoto.setUserId( orgId );
                        else if ( contact != null && contact.getBitmap() != null )  iv_UserPhoto.setImageBitmap( contact.getBitmap() );
                        else if ( userId != null && !userId.equals( "" ) ) iv_UserPhoto.setUserId( userId );
                       
                        //2016-07-27
                        iv_UserPhoto.setScaleType( ScaleType.FIT_XY ); 
                        //iv_UserPhoto.setScaleType( ScaleType.FIT_CENTER );  // View 영역에 맞춰 보여줌 가운데 정렬, 비율유지
                        
                        btn_delete = ( Button ) findViewById( R.id.detail_delete );
                        btn_delete.setOnClickListener( this );
                        btn_edit = ( Button ) findViewById( R.id.detail_edit );
                        btn_edit.setOnClickListener( this );
                        btn_favorite = ( Button ) findViewById( R.id.detail_favorite );
                        btn_favorite.setOnClickListener( this );
                        btn_delete.setTypeface( Define.tfRegular );
                        btn_edit.setTypeface( Define.tfRegular );
                        btn_favorite.setTypeface( Define.tfRegular );
                        int count = FavoriteView.instance().adapter.getCount();
                        String phone = userPhoneNumber.replaceAll( "-", "" );
                        
                        // Favorite
                        //2016-12-13
                        int isFavoriteCheck = 0;
                        if (  type.equals( "Org" ) && !orgId.equals( "" )  )
                        	isFavoriteCheck = Database.instance( getApplicationContext() ).selectFavorite( orgId );
                        else if ( userId != null && !userId.equals( "" ))
                        	isFavoriteCheck = Database.instance( getApplicationContext() ).selectFavorite( userId );
                        
                        isAddFavorite = false;
                        
                        if ( isFavoriteCheck > 0)
                        	isAddFavorite = true;
                        
                        /*for ( int i = 0; i < count; i++ )
                        {
                                if ( FavoriteView.instance().adapter.getItem( i ).getName().equals( deviceName ) )
                                {
                                	isAddFavorite = true;
                                	break; //2016-11-24 HHJ
                                }
                        }*/
                        //
                        
                        if ( isAddFavorite ) btn_favorite.setBackgroundResource( R.drawable.detailinfo_favories );
                        else btn_favorite.setBackgroundResource( R.drawable.detailinfo_favories_act );
                        
                        if ( (userId != null && userId.equals( Define.getMyId( getApplicationContext() ) ))
                                        || (orgId != null && orgId.equals( Define.getMyId( getApplicationContext() ) )) )
                        {
                                btn_favorite.setVisibility( View.GONE );
                                tv_favorite_msg.setVisibility( View.GONE );
                        }
                        else
                        {
                                btn_favorite.setVisibility( View.VISIBLE );
                                tv_favorite_msg.setVisibility( View.VISIBLE );
                        }
                        
                        btn_sms = ( Button ) findViewById( R.id.detail_message );
                        //btn_sms.setOnClickListener( this );
                        
                        btn_fmcCall = ( Button ) findViewById( R.id.detail_fmc );
                        //btn_fmcCall.setOnClickListener( this );
                        
                        btn_office_fmcCall = ( Button ) findViewById( R.id.detail_office_fmc );
                        //btn_office_fmcCall.setOnClickListener( this );
                        
                        btn_chatting = ( Button ) findViewById( R.id.detail_chatting );
                        //btn_chatting.setOnClickListener( this );
                        
                        btn_office_chatting = ( Button ) findViewById( R.id.detail_office_chatting );
                        //btn_office_chatting.setOnClickListener( this );
                        
                        //type:Device, orgId:null, userId:kimsh
                        if ( type.equals( "Org" ) && !orgId.equals( "" ) )
                        {
                                btn_chatting.setBackgroundResource( R.drawable.icon_chat );
                                btn_chatting.setOnClickListener( this );
                                btn_office_chatting.setBackgroundResource( R.drawable.icon_chat );
                                btn_office_chatting.setOnClickListener( this );
                        }
                        else if( userId != null )
                        {
                                btn_chatting.setBackgroundResource( R.drawable.icon_chat );
                                btn_chatting.setOnClickListener( this );
                                btn_office_chatting.setBackgroundResource( R.drawable.icon_chat );
                                btn_office_chatting.setOnClickListener( this );
                        }
                        else if ( userId == null )
                        {
                                btn_chatting.setBackgroundResource( R.drawable.icon_chat_none );
                                btn_office_chatting.setBackgroundResource( R.drawable.icon_chat_none );
                        }
                        
                        btn_call = ( Button ) findViewById( R.id.detail_call );
                        //btn_call.setOnClickListener( this );
                        
                        if ( tv_PhoneNumber.getText().toString().equals( "" ) )
                        {
                                btn_sms.setBackgroundResource( R.drawable.icon_message_none );
                                btn_fmcCall.setBackgroundResource( R.drawable.icon_call_fmc_none );
                                btn_call.setBackgroundResource( R.drawable.icon_call_none );
                        }
                        else
                        {
                                btn_sms.setBackgroundResource( R.drawable.icon_message );
                                btn_sms.setOnClickListener( this );
                                btn_fmcCall.setBackgroundResource( R.drawable.icon_call_fmc );
                                btn_fmcCall.setOnClickListener( this );
                                btn_call.setBackgroundResource( R.drawable.icon_call );
                                btn_call.setOnClickListener( this );
                        }
                        
                        btn_office_call = ( Button ) findViewById( R.id.detail_office_call );
                        //btn_office_call.setOnClickListener( this );
                        
                        if ( tv_OfficeNumber.getText().toString().equals( "" ) ) 
                        {
                                btn_office_fmcCall.setBackgroundResource( R.drawable.icon_call_fmc_none );
                                btn_office_call.setBackgroundResource( R.drawable.icon_call_none );
                        }
                        else
                        {
                                btn_office_fmcCall.setBackgroundResource( R.drawable.icon_call_fmc);
                                btn_office_fmcCall.setOnClickListener( this );
                                btn_office_call.setBackgroundResource( R.drawable.icon_call );
                                btn_office_call.setOnClickListener( this );
                        }
                        
                        btn_sendEmail = ( Button ) findViewById( R.id.detail_send_email );
                        btn_sendEmail.setOnClickListener( this );
                        
                        if ( tv_email.getText().toString().equals( "" ) ) 
                                btn_sendEmail.setVisibility( View.GONE );
                        
                        isShowDialog = false;
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public Drawable getDrawableFromBitmap( Bitmap bitmap )
        {
                Drawable d = new BitmapDrawable( getResources(), bitmap );
                return d;
        }

        private void deleteContact( String name )
        {
                ContentResolver cr = getContentResolver();
                String where = ContactsContract.Data.DISPLAY_NAME + " = ? ";
                String[] params = new String[] { name };
                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                ops.add( ContentProviderOperation.newDelete( ContactsContract.RawContacts.CONTENT_URI ).withSelection( where, params ).build() );
                try
                {
                        cr.applyBatch( ContactsContract.AUTHORITY, ops );
                }
                catch ( RemoteException e )
                {
                        e.printStackTrace();
                }
                catch ( OperationApplicationException e )
                {
                        e.printStackTrace();
                }
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                TextView text = ( TextView ) layout.findViewById( R.id.tv );
                text.setTypeface( Define.tfRegular );
                text.setText( getString( R.string.favorite_del_msg ) );
                Toast toast = new Toast( ContactDetail.this );
                toast.setGravity( Gravity.CENTER, 0, 0 );
                toast.setDuration( Toast.LENGTH_SHORT );
                toast.setView( layout );
                toast.show();
        }

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == R.id.detail_delete )
                {
                        layoutOption.setVisibility( View.GONE );
                        deleteContact( deviceName );
                        finish();
                        FavoriteView.instance().isLoadComplete = true;
                        FavoriteView.instance().resetData();
                        ContactView.instance().isLoadComplete = true;
                        ContactView.instance().displayListBasic();
                        MainActivity.Instance().loadingContactData();
                }
                else if( v.getId() == R.id.detail_message )
                {
                        Uri uri= Uri.parse("smsto:" + userPhoneNumber.replaceAll("-","") ); 
                        Intent i= new Intent(Intent.ACTION_SENDTO,uri); 
                        startActivity(i);
                }
                else if ( v.getId() == R.id.contact_detail_option )
                {
                        isShowDialog = !isShowDialog;
                        if ( isShowDialog ) layoutOption.setVisibility( View.VISIBLE );
                        else layoutOption.setVisibility( View.GONE );
                }
                else if ( v.getId() == R.id.detail_contact_save )
                {
                        if ( orgId != null && orgId.equals( Define.getMyId() ) )
                        {
                                ActionManager.alert( this, "자기 자신은 추가할 수 없습니다." );
                                return;
                        }
                        // 주소록에 저장
                        saveUserInfo( deviceName, userPhoneNumber, userOfficeNumber, userEmail, orgCompany, orgPosition );
                }
                else if ( v.getId() == R.id.detail_edit_cancel )
                {
                        layoutOption.setVisibility( View.GONE );
                        isEdit = !isEdit;
                        clear();
                }
                else if ( v.getId() == R.id.detail_edit_save )
                {
                        layoutOption.setVisibility( View.GONE );
                        // tmpName = etName.getText().toString();
                        tmpMobile = etMobile.getText().toString();
                        tmpTelephone = etOffice.getText().toString();
                        tmpEmail = etEmail.getText().toString();
                        isEdit = !isEdit;
                        save();
                        // ContactView.instance().updateContactData();
                }
                else if ( v.getId() == R.id.detail_edit )
                {
                        layoutOption.setVisibility( View.GONE );
                        isEdit = !isEdit;
                        if ( isEdit )
                        {
                                tmpName = tv_UserName.getText().toString();
                                tmpMobile = tv_PhoneNumber.getText().toString();
                                tmpTelephone = tv_OfficeNumber.getText().toString();
                                tmpEmail = tv_email.getText().toString();
                                showControl();
                        }
                        else clear();
                }
                else if ( v.getId() == R.id.detail_photo )
                {
                        if ( type.equals( "Device" ) )
                        {
                                if ( !isEdit && !isAddFavorite && contact != null && contact.getBitmap() != null )
                                {
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        contact.getBitmap().compress( Bitmap.CompressFormat.PNG, 100, stream );
                                        byte[] byteArray = stream.toByteArray();
                                        Intent intent = new Intent( this, kr.co.ultari.atsmart.basic.subview.PhotoViewer.class );
                                        intent.putExtra( "image", byteArray );
                                        startActivity( intent );
                                }
                                else if ( type.equals( "Org" ) )
                                {
                                        ActionManager.showPhoto( contact.getUserid() );
                                }
                                else
                                {
                                        Intent photoPickerIntent = new Intent( Intent.ACTION_PICK,
                                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                                        photoPickerIntent.setType( "image/*" );
                                        photoPickerIntent.putExtra( "crop", "true" );
                                        photoPickerIntent.putExtra( MediaStore.EXTRA_OUTPUT, getTempUri() );
                                        photoPickerIntent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
                                        startActivityForResult( photoPickerIntent, REQ_CODE_PICK_IMAGE );
                                }
                        }
                }
                else if ( v.getId() == R.id.detail_favorite )
                {
                        if ( isAddFavorite )
                        {
                                if ( type.equals( "Device" ) )
                                {
                                        if ( contact != null && contact.userId != null && contact.userId.equals( Define.getMyId() ) )
                                        {
                                                ActionManager.alert( this, "자기 자신은 즐겨찾기에 추가할 수 없습니다." );
                                                return;
                                        }
                                        if ( deviceName == null ) return;
                                        String[] fv = new String[] { deviceName };
                                        ContentValues values = new ContentValues();
                                        values.put( Contacts.STARRED, 0 );
                                        getApplicationContext().getContentResolver().update( Contacts.CONTENT_URI, values, Contacts.DISPLAY_NAME + "= ?", fv );
                                        handler.sendEmptyMessageDelayed( Define.AM_FAVORITE_REMOVE, 200 );
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
                                        if ( orgId != null && orgId.equals( Define.getMyId() ) )
                                        {
                                                ActionManager.alert( this, "자기 자신은 즐겨찾기에 추가할 수 없습니다." );
                                                return;
                                        }
                                        Database.instance( ContactDetail.this ).deleteFavorite( orgId );
                                        handler.sendEmptyMessageDelayed( Define.AM_FAVORITE_REMOVE, 200 );
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
                        }
                        else
                        {
                                if ( type.equals( "Device" ) )
                                {
                                        if ( deviceName == null ) return;
                                        String[] fv = new String[] { deviceName };
                                        ContentValues values = new ContentValues();
                                        values.put( Contacts.STARRED, 1 );
                                        getApplicationContext().getContentResolver().update( Contacts.CONTENT_URI, values, Contacts.DISPLAY_NAME + "= ?", fv );
                                        handler.sendEmptyMessageDelayed( Define.AM_FAVORITE_ADD, 200 );
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
                                }
                                else
                                {
                                        try
                                        {
                                                if ( orgId != null )
                                                {
                                                        Database.instance( ContactDetail.this ).insertFavorite( orgId, "0", userName, nickName, "0", "0" );
                                                        handler.sendEmptyMessageDelayed( Define.AM_FAVORITE_ADD, 200 );
                                                        LayoutInflater inflater = getLayoutInflater();
                                                        View layout = inflater.inflate( R.layout.custom_toast,
                                                                        ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                                                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                                        text.setText( getString( R.string.favorite_add_msg ) );
                                                        text.setTypeface( Define.tfRegular );
                                                        Toast toast = new Toast( ContactDetail.this );
                                                        toast.setGravity( Gravity.CENTER, 0, 0 );
                                                        toast.setDuration( Toast.LENGTH_SHORT );
                                                        toast.setView( layout );
                                                        toast.show();
                                                }
                                        }
                                        catch ( Exception e )
                                        {
                                                e.printStackTrace();
                                        }
                                }
                        }
                }
                else if ( v.getId() == R.id.detail_chatting || v.getId() == R.id.detail_office_chatting )
                {
                        if ( type.equals( "Org" ) && !orgId.equals( "" ) )
                        {
                                if ( !orgId.equalsIgnoreCase( Define.getMyId( getApplicationContext() ) ) )
                                {
                                        String oUserIds = orgId + "," + Define.getMyId( getApplicationContext() );
                                        String userIds = StringUtil.arrange( oUserIds );
                                        String userNames = deviceName + " " + orgPosition + "," + StringUtil.getNamePosition( Define.getMyName() );
                                        userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                                        String roomId = userIds.replace( ",", "_" );
                                        ArrayList<ArrayList<String>> array = Database.instance( getApplicationContext() ).selectChatRoomInfo( roomId );
                                        if ( array.size() == 0 ) Database.instance( getApplicationContext() ).insertChatRoomInfo( roomId, userIds, userNames,
                                                        StringUtil.getNowDateTime(), getString( R.string.newRoom ) );
                                        ActionManager.openChat( getApplicationContext(), roomId, userIds, userNames );
                                }
                                else
                                {
                                        ActionManager.alert( this, "자신과 대화할 수 없습니다." );
                                }
                        }
                        else if ( userId != null && !userId.equals( "" ) )
                        {
                                if ( !userId.equalsIgnoreCase( Define.getMyId( getApplicationContext() ) ) )
                                {
                                        String oUserIds = userId + "," + Define.getMyId( getApplicationContext() );
                                        String userIds = StringUtil.arrange( oUserIds );
                                        String userNames = orgName + "," + StringUtil.getNamePosition( Define.getMyName() );
                                        userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                                        String roomId = userIds.replace( ",", "_" );
                                        ArrayList<ArrayList<String>> array = Database.instance( getApplicationContext() ).selectChatRoomInfo( roomId );
                                        if ( array.size() == 0 ) Database.instance( getApplicationContext() ).insertChatRoomInfo( roomId, userIds, userNames,
                                                        StringUtil.getNowDateTime(), getString( R.string.newRoom ) );
                                        ActionManager.openChat( getApplicationContext(), roomId, userIds, userNames );
                                }
                        }
                }
                else if ( v.getId() == R.id.detail_call )
                {
                        if ( type.equals( "Org" ) && !orgId.equals( "" ) )
                        {
                                if ( orgId.equalsIgnoreCase( Define.getMyId( getApplicationContext() ) ) )
                                {
                                        ActionManager.alert( this, "자신에게 전화를 걸 수 없습니다." );
                                        return;
                                }
                        }
                        String phoneNumber = tv_PhoneNumber.getText().toString().trim();
                        if ( !phoneNumber.equals( "" ) )
                        {
                                FmcSendBroadcast.FmcSendCall( phoneNumber ,1, getApplicationContext()); 
                        }
                }
                else if ( v.getId() == R.id.detail_office_call )
                {
                        if ( type.equals( "Org" ) && !orgId.equals( "" ) )
                        {
                                if ( orgId.equalsIgnoreCase( Define.getMyId( getApplicationContext() ) ) )
                                {
                                        ActionManager.alert( this, "자신에게 전화를 걸 수 없습니다." );
                                        return;
                                }
                        }
                        
                        String number = tv_OfficeNumber.getText().toString().trim();
                        number = number.replaceAll( "-", "" );
                        
                        /* 2016-07-27
                        //2016-06-30
                        if(number.length() > 4)
                                number = number.substring( number.length()-4 );
                        //
*/                        
                        if ( !number.equals( "" ) )
                                FmcSendBroadcast.FmcSendCall( number ,1, getApplicationContext()); 
                }
                else if( v.getId() == R.id.detail_fmc )
                {
                        if ( type.equals( "Org" ) && !orgId.equals( "" ) )
                        {
                                if ( orgId.equalsIgnoreCase( Define.getMyId( getApplicationContext() ) ) )
                                {
                                        ActionManager.alert( this, "자신에게 전화를 걸 수 없습니다." );
                                        return;
                                }
                        }
                        String phoneNumber = tv_PhoneNumber.getText().toString().trim();
                        if ( !phoneNumber.equals( "" ) )
                        {
                                FmcSendBroadcast.FmcSendCall( phoneNumber ,0, getApplicationContext()); 
                        }
                }
                else if( v.getId() == R.id.detail_office_fmc )
                {
                        if ( type.equals( "Org" ) && !orgId.equals( "" ) )
                        {
                                if ( orgId.equalsIgnoreCase( Define.getMyId( getApplicationContext() ) ) )
                                {
                                        ActionManager.alert( this, "자신에게 전화를 걸 수 없습니다." );
                                        return;
                                }
                        }
                        
                        String number = tv_OfficeNumber.getText().toString().trim();
                        
                        /*2016-12-21
                        number = number.replaceAll( "-", "" );
                        
                        //2016-06-30
                        if(number.length() > 4)
                                number = number.substring( number.length()-4 );
                        //
                        */
                        
                        if ( !number.equals( "" ) )
                                FmcSendBroadcast.FmcSendCall( number ,0, getApplicationContext()); 
                }
                else if ( v.getId() == R.id.detail_send_email )
                {
                        String email = tv_email.getText().toString().trim();
                        if ( !email.equals( "" ) )
                        {
                                Uri uri = Uri.parse( "mailto:" + email );
                                Intent it = new Intent( Intent.ACTION_SENDTO, uri );
                                startActivity( it );
                        }
                }
        }

        // 2015-05-06
        private Uri getTempUri()
        {
                return Uri.fromFile( getTempFile() );
        }

        private File getTempFile()
        {
                if ( isSDCARDMounted() )
                {
                        File f = new File( Environment.getExternalStorageDirectory(), TEMP_PHOTO_FILE );
                        try
                        {
                                f.createNewFile();
                        }
                        catch ( IOException e )
                        {}
                        return f;
                }
                else
                {
                        return null;
                }
        }

        private boolean isSDCARDMounted()
        {
                String status = Environment.getExternalStorageState();
                if ( status.equals( Environment.MEDIA_MOUNTED ) ) return true;
                return false;
        }

        private void showControl()
        {
                // 상단레이아웃 보여주기 및 에디트박스들 보여주고 현재입력된값들로 채워준다.
                layoutTop.setVisibility( View.VISIBLE );
                etEmail.setVisibility( View.VISIBLE );
                etOffice.setVisibility( View.VISIBLE );
                etMobile.setVisibility( View.VISIBLE );
                // etName.setVisibility( View.VISIBLE );
                etEmail.setText( tmpEmail );
                etOffice.setText( tmpTelephone );
                etMobile.setText( tmpMobile );
                // etName.setText( tmpName );
                // tv_UserName.setVisibility( View.GONE );
                tv_PhoneNumber.setVisibility( View.GONE );
                tv_OfficeNumber.setVisibility( View.GONE );
                tv_email.setVisibility( View.GONE );
        }

        private void clear()
        {
                layoutTop.setVisibility( View.GONE );
                etEmail.setVisibility( View.GONE );
                etOffice.setVisibility( View.GONE );
                etMobile.setVisibility( View.GONE );
                // etName.setVisibility( View.GONE );
                // tv_UserName.setVisibility( View.VISIBLE );
                tv_PhoneNumber.setVisibility( View.VISIBLE );
                tv_OfficeNumber.setVisibility( View.VISIBLE );
                tv_email.setVisibility( View.VISIBLE );
                tv_UserName.setText( tmpName );
                tv_PhoneNumber.setText( tmpMobile );
                tv_OfficeNumber.setText( tmpTelephone );
                tv_email.setText( tmpEmail );
                tmpName = "";
                tmpMobile = "";
                tmpTelephone = "";
                tmpEmail = "";
        }

        private void save()
        {
                layoutTop.setVisibility( View.GONE );
                etEmail.setVisibility( View.GONE );
                etOffice.setVisibility( View.GONE );
                etMobile.setVisibility( View.GONE );
                // etName.setVisibility( View.GONE );
                // tv_UserName.setVisibility( View.VISIBLE );
                tv_PhoneNumber.setVisibility( View.VISIBLE );
                tv_OfficeNumber.setVisibility( View.VISIBLE );
                tv_email.setVisibility( View.VISIBLE );
                tv_UserName.setText( tmpName );
                tv_PhoneNumber.setText( tmpMobile );
                tv_OfficeNumber.setText( tmpTelephone );
                tv_email.setText( tmpEmail );
                updateContact( deviceName, tmpMobile, tmpTelephone, tmpEmail );
                tmpName = "";
                tmpMobile = "";
                tmpTelephone = "";
                tmpEmail = "";
                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                TextView text = ( TextView ) layout.findViewById( R.id.tv );
                text.setText( getString( R.string.contact_update_msg ) );
                text.setTypeface( Define.tfRegular );
                Toast toast = new Toast( ContactDetail.this );
                toast.setGravity( Gravity.CENTER, 0, 0 );
                toast.setDuration( Toast.LENGTH_SHORT );
                toast.setView( layout );
                toast.show();
        }
       
        public Handler handler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_FAVORITE_REMOVE )
                                {
                                        isAddFavorite = false;
                                        tv_favorite_msg.setTextColor( 0xFF333333 );
                                        btn_favorite.setBackgroundResource( R.drawable.detailinfo_favories_act ); 
                                }
                                else if ( msg.what == Define.AM_FAVORITE_ADD )
                                {
                                        isAddFavorite = true;
                                        tv_favorite_msg.setTextColor( 0xFFFFFFFF );
                                        btn_favorite.setBackgroundResource( R.drawable.detailinfo_favories ); 
                                }
                                else if ( msg.what == Define.AM_REFRESH )
                                {
                                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
                                        {
                                                if ( selectedImage != null ) iv_UserPhoto.setImageBitmap( selectedImage );
                                        }
                                        else
                                        {
                                                if ( selectedImage != null ) iv_UserPhoto.setImageBitmap( selectedImage );
                                        }
                                }
                                else if ( msg.what == Define.AM_CONFIRM_YES )
                                {
                                        // 주소록에 저장
                                        saveUserInfo( deviceName, userPhoneNumber, userOfficeNumber, userEmail, orgCompany, orgPosition );
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };

        private void updateContact( String name, String mobile, String telephone, String email )
        {
                try
                {
                        int raw_id = 0;
                        String[] projection = new String[] { ContactsContract.RawContacts._ID };
                        String selection = ContactsContract.RawContacts.CONTACT_ID + "=?";
                        String[] selectionArgs = new String[] { String.valueOf( userPhotoId ) };
                        raw_id = Integer.parseInt( rawId );
                        if ( mobile != null && !mobile.equals( "" ) )
                        {
                                if ( userPhoneNumber == null || userPhoneNumber.equals( "" ) )
                                        ContactUpdateManager.insertPhoneNumberData( getContentResolver(), raw_id, mobile );
                                else
                                        ContactUpdateManager.updatePhoneData( getContentResolver(), raw_id, mobile );
                        }
                        else
                        {
                                ContactUpdateManager.updatePhoneData( getContentResolver(), raw_id, " " );
                        }
                        if ( telephone != null && !telephone.equals( "" ) )
                        {
                                if ( userEmail == null || userEmail.equals( "" ) )
                                {
                                        ContactUpdateManager.insertHomeNumberData( getContentResolver(), raw_id, telephone );
                                }
                                else
                                {
                                        ContactUpdateManager.updateHomeData( getContentResolver(), raw_id, telephone );
                                }
                        }
                        else
                        {
                                ContactUpdateManager.updateHomeData( getContentResolver(), raw_id, " " );
                        }
                        if ( email != null && !email.equals( "" ) )
                        {
                                if ( userOfficeNumber == null || userOfficeNumber.equals( "" ) )
                                {
                                        // insert
                                        ContactUpdateManager.insertEmailNumberData( getContentResolver(), raw_id, email );
                                }
                                else
                                {
                                        // update
                                        ContactUpdateManager.updateEmailData( getContentResolver(), raw_id, email );
                                }
                        }
                        else ContactUpdateManager.updateEmailData( getContentResolver(), raw_id, " " );
                        if ( contact != null )
                        {
                                contact.setPhonenum( mobile );
                                contact.setTelnum( telephone );
                                contact.setEmail( email );
                        }
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
        }
        private static final String TEMP_PHOTO_FILE = "temporary_holder.jpg";

        @Override
        protected void onActivityResult( int requestCode, int resultCode, Intent data )
        {
                if ( requestCode == GET_PICTURE_URI )
                {
                        if ( resultCode == Activity.RESULT_OK )
                        {
                                Uri uri = data.getData();
                                tempBitMap = null;
                                try
                                {
                                        tempBitMap = Images.Media.getBitmap( getContentResolver(), uri );
                                }
                                catch ( FileNotFoundException e )
                                {
                                        e.printStackTrace();
                                }
                                catch ( IOException e )
                                {
                                        e.printStackTrace();
                                }
                                if ( tempBitMap != null )
                                {
                                        ByteArrayOutputStream image = new ByteArrayOutputStream();
                                        tempBitMap.compress( Bitmap.CompressFormat.JPEG, 100, image );
                                        if ( userPhoneNumber != null )
                                        {
                                                String raw_contact_id = rawId;
                                                if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getContentResolver(),
                                                                image.toByteArray(), Long.parseLong( raw_contact_id ), tempBitMap );
                                        }
                                        else if ( userOfficeNumber != null )
                                        {
                                                String raw_contact_id = rawId;
                                                if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getContentResolver(),
                                                                image.toByteArray(), Long.parseLong( raw_contact_id ), tempBitMap );
                                        }
                                }
                        }
                }
                else if ( requestCode == REQ_CODE_PICK_IMAGE )
                {
                        if ( resultCode == RESULT_OK )
                        {
                                if ( data != null )
                                {
                                        File tempFile = getTempFile();
                                        String filePath = Environment.getExternalStorageDirectory() + "/temporary_holder.jpg";
                                        selectedImage = BitmapFactory.decodeFile( filePath );
                                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) iv_UserPhoto
                                                        .setBackground( getDrawableFromBitmap( selectedImage ) );
                                        else iv_UserPhoto.setBackgroundDrawable( getDrawableFromBitmap( selectedImage ) );
                                        if ( selectedImage != null )
                                        {
                                                ByteArrayOutputStream image = new ByteArrayOutputStream();
                                                selectedImage.compress( Bitmap.CompressFormat.JPEG, 100, image );
                                                if ( userPhoneNumber != null )
                                                {
                                                        String raw_contact_id = rawId;
                                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getContentResolver(),
                                                                        image.toByteArray(), Long.parseLong( raw_contact_id ), selectedImage );
                                                }
                                                else if ( userOfficeNumber != null )
                                                {
                                                        String raw_contact_id = rawId;
                                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getContentResolver(),
                                                                        image.toByteArray(), Long.parseLong( raw_contact_id ), selectedImage );
                                                }
                                        }
                                }
                        }
                }
        }

        public void setContactPhoto( ContentResolver c, byte[] bytes, long rawContactId, Bitmap bmp )
        {
                int sdk = android.os.Build.VERSION.SDK_INT;
                int photoRow = -1;
                //Log.d( "sdk", "" + sdk );
                ContentValues values = new ContentValues();
                String where = ContactsContract.Data.RAW_CONTACT_ID + " = " + rawContactId + " " + "AND " + ContactsContract.Data.MIMETYPE + "=='"
                                + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";
                Cursor cursor = c.query( ContactsContract.Data.CONTENT_URI, null, where, null, null );
                //Log.d( "GetCount", "PhotoCount : " + cursor.getCount() );
                int idIdx = cursor.getColumnIndexOrThrow( ContactsContract.Data._ID );
                if ( cursor.moveToFirst() )
                {
                        photoRow = cursor.getInt( idIdx );
                }
                cursor.close();
                //Log.d( "PhotoRow", "" + photoRow );
                values.put( ContactsContract.Data.RAW_CONTACT_ID, rawContactId );
                values.put( ContactsContract.Data.IS_SUPER_PRIMARY, 1 );
                values.put( ContactsContract.CommonDataKinds.Photo.PHOTO, bytes );
                values.put( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE );
                if ( photoRow >= 0 )
                {
                        int cnt = c.update( ContactsContract.Data.CONTENT_URI, values, ContactsContract.Data._ID + "=" + photoRow, null );
                        //Log.d( "BitmapContact", "updateCount : " + cnt );
                }
                else
                {
                        c.insert( ContactsContract.Data.CONTENT_URI, values );
                }
                handler.sendEmptyMessageDelayed( Define.AM_REFRESH, 1500 );
                //Log.d( "BitmapContact", contact.getContactId() + ":" + contact.getPhonenum() );
                contact.setBitmap( bmp );
        }

        private void saveUserInfo( String name, String mobile, String telephone, String email, String company, String position )
        {
                //Log.d( "ContactSave", "saveUserInfo1" );
                String DisplayName = name;
                String MobileNumber = mobile;
                String HomeNumber = telephone;
                String emailID = email;
                String companyName = company;
                String userPosition = position;
                //Log.d( "ContactSave", "saveUserInfo2" );
                ContentResolver cr = getContentResolver();
                Cursor cur = cr.query( ContactsContract.Contacts.CONTENT_URI, null, null, null, null );
                if ( cur.getCount() > 0 )
                {
                        while ( cur.moveToNext() )
                        {
                                String existName = cur.getString( cur.getColumnIndex( ContactsContract.Contacts.DISPLAY_NAME ) );
                                if ( existName.contains( name ) )
                                {
                                        LayoutInflater inflater = getLayoutInflater();
                                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                        text.setText( getString( R.string.favorite_exist ) );
                                        text.setTypeface( Define.tfRegular );
                                        Toast toast = new Toast( ContactDetail.this );
                                        toast.setGravity( Gravity.CENTER, 0, 0 );
                                        toast.setDuration( Toast.LENGTH_SHORT );
                                        toast.setView( layout );
                                        toast.show();
                                        return;
                                }
                        }
                }
                //Log.d( "ContactSave", "saveUserInfo3" );
                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                ops.add( ContentProviderOperation.newInsert( ContactsContract.RawContacts.CONTENT_URI )
                                .withValue( ContactsContract.RawContacts.ACCOUNT_TYPE, null ).withValue( ContactsContract.RawContacts.ACCOUNT_NAME, null )
                                .build() );
                // ------------------------------------------------------ Names
                if ( DisplayName != null )
                {
                        ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE )
                                        .withValue( ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, DisplayName ).build() );
                }
                // ------------------------------------------------------ Mobile
                // Number
                if ( MobileNumber != null )
                {
                        ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ).build() );
                }
                // ------------------------------------------------------ Home
                // Numbers
                if ( HomeNumber != null )
                {
                        ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.NUMBER, HomeNumber )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK ).build() ); //2016-06-30
                                        //.withValue( ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME ).build() ); 
                }
                // ------------------------------------------------------ Email
                if ( emailID != null )
                {
                        ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE )
                                        .withValue( ContactsContract.CommonDataKinds.Email.DATA, emailID )
                                        .withValue( ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK ).build() );
                }
                // ----------------------------------------------------
                // Organization
                ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE )
                                .withValue( ContactsContract.CommonDataKinds.Organization.COMPANY, companyName )
                                .withValue( ContactsContract.CommonDataKinds.Organization.TITLE, userPosition )
                                .withValue( ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK )
                                .build() );
                try
                {
                        getContentResolver().applyBatch( ContactsContract.AUTHORITY, ops );
                        String orgWhere = ContactsContract.Contacts.DISPLAY_NAME + " = ?";
                        String[] orgWhereParams = new String[] { DisplayName };
                        Cursor orgCur = cr.query( ContactsContract.Contacts.CONTENT_URI, null, orgWhere, orgWhereParams, null );
                        if ( orgCur.moveToFirst() )
                        {
                                String contactId = orgCur.getString( orgCur.getColumnIndex( ContactsContract.Contacts._ID ) );
                                //Log.d( "SaveContactId", contactId );
                                contact.setContactId( contactId );
                        }
                        orgCur.close();
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                        text.setText( getString( R.string.input_success ) );
                        text.setTypeface( Define.tfRegular );
                        Toast toast = new Toast( ContactDetail.this );
                        toast.setGravity( Gravity.CENTER, 0, 0 );
                        toast.setDuration( Toast.LENGTH_SHORT );
                        toast.setView( layout );
                        toast.show();
                        String raw_contact_id = "";
                        Bitmap tempBitMap = Define.getBitmap( orgId );
                        if ( tempBitMap != null )
                        {
                                ByteArrayOutputStream image = new ByteArrayOutputStream();
                                tempBitMap.compress( Bitmap.CompressFormat.JPEG, 100, image );
                                if ( MobileNumber != null )
                                {
                                        raw_contact_id = rawId;
                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getContentResolver(),
                                                        image.toByteArray(), Long.parseLong( raw_contact_id ), tempBitMap );
                                }
                                else if ( HomeNumber != null )
                                {
                                        raw_contact_id = rawId;
                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getContentResolver(),
                                                        image.toByteArray(), Long.parseLong( raw_contact_id ), tempBitMap );
                                }
                                // nowSelectedUserId = null;
                                // nowSelectedUserName = null;
                        }
                        else new getUserPhotoData( MobileNumber, HomeNumber );
                        contact.setTelnum( HomeNumber.replaceAll( "-", "" ) );
                        contact.setPhonenum( MobileNumber.replaceAll( "-", "" ) );
                        contact.setName( name );
                        contact.setPosition( userPosition );
                        contact.setCompany( companyName );
                        contact.setEmail( email );
                        contact.setType( "Device" );
                        Define.contactArray.add( contact );
                        // Define.contactMap.put(Define.getContactIdWithNameAndPhoneNumber(name, MobileNumber.replaceAll("-", "")), contact);
                        Define.contactMap.put( contact.getContactId(), contact );
                        //Log.d( "ContactDetail", "UpdatePhone : " + MobileNumber + " : " + contact.getContactId() + " : " + contact.getUserid() );
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
        }
        private class getUserPhotoData extends Thread {
                String mobileNumber = "";
                String officeNumber = "";

                private getUserPhotoData( String mobile, String home )
                {
                        this.mobileNumber = mobile;
                        this.officeNumber = home;
                        this.start();
                }

                public void run()
                {
                        Bitmap pic = UltariSocketUtil.getUserImage( orgId, 200, 200 );
                        if ( pic != null )
                        {
                                ByteArrayOutputStream image = new ByteArrayOutputStream();
                                pic.compress( Bitmap.CompressFormat.JPEG, 100, image );
                                if ( mobileNumber != null )
                                {
                                        String raw_contact_id = rawId;
                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getContentResolver(),
                                                        image.toByteArray(), Long.parseLong( raw_contact_id ), pic );
                                }
                                else if ( officeNumber != null )
                                {
                                        String raw_contact_id = rawId;
                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getContentResolver(),
                                                        image.toByteArray(), Long.parseLong( raw_contact_id ), pic );
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
                                int tab = MainActivity.Instance().mPager.getCurrentItem();
                                switch ( tab )
                                {
                                case Define.TAB_BOOKMARK :
                                        FavoriteView.instance().isLoadComplete = true;
                                        FavoriteView.instance().resetData();
                                        break;
                                case Define.TAB_CONTACT :
                                        ContactView.instance().isLoadComplete = true;
                                        // ContactView.instance().displayListBasic();
                                        break;
                                case Define.TAB_CALL_LOG :
                                        CallView.instance().isLoadComplete = true;
                                        CallView.instance().callLog();
                                        break;
                                }
                                // MainActivity.Instance().mPager.getCurrentItem()
                                // == Define.TAB_BOOKMARK
                        }
                }
                return super.onKeyDown( keyCode, event );
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
                try
                {
                        sb.delete( 0, sb.length() );
                        sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                        .getServerPort( Define.mContext ) ) );
                        sc.setSoTimeout( 30000 );
                        ir = new InputStreamReader( sc.getInputStream() );
                        br = new BufferedReader( ir );
                        short type = 2; // 2->ID, 5->Mobile
                        send( "SearchRequest\t" + type + "\t" + orgId );
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
                                        process( command, param );
                                }
                        }
                }
                catch ( SocketException se )
                {
                        Log.e( "ContactDetail", se.getMessage() );
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
                }
                try
                {
                        Thread.sleep( 5000 );
                }
                catch ( InterruptedException ie )
                {}
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
                                        tv_UserInfo.setText( nick );
                                        onDestroy = true;
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

        public void process( String command, ArrayList<String> param )
        {
                if ( command.equals( "User" ) && param.size() >= 5 && !param.get( 0 ).equals( Define.getMyId( getApplicationContext() ) ) )
                {
                        Message m = searchHandler.obtainMessage( Define.AM_ADD_SEARCH, param );
                        searchHandler.sendMessage( m );
                }
        }

        public void send( String msg ) throws Exception
        {
                msg.replaceAll( "\f", "" );
                sc.send( codec.EncryptSEED( msg ) + '\f' );
        }
}
