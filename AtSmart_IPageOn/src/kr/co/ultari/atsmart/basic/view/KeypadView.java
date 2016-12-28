package kr.co.ultari.atsmart.basic.view;

import java.util.ArrayList;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.subview.ContactResultDialog;
import kr.co.ultari.atsmart.basic.util.FmcSendBroadcast;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class KeypadView extends Fragment implements OnClickListener, OnLongClickListener, OnTouchListener {
        private static final String TAG = "/AtSmart/KeypadView";
        private static KeypadView keypadViewInstance = null;
        public LayoutInflater inflater;
        private View view;
        private Button btnAddContact, btnNum0, btnNum1, btnNum2, btnNum3, btnNum4, btnNum5, btnNum6, btnNum7, btnNum8, btnNum9,
                        btnCall, btnDel;
        private ImageButton btnArrow,btnNumStar,btnNumSharp;
        private TextView tvUserName, tvUserPhone, tvFindUserCount, etInputNumber, tvAddContactTitle, keypad_addcontact_title;
        private ImageView ivUserIcon, ivFmcLogo, ivHdLogo;
        private String ret = "";
        private ArrayList<Contact> result = null;
        private LinearLayout layout_find, layout_addContact;
        private LinearLayout keypad_layout = null;

        public static KeypadView instance()
        {
                if ( keypadViewInstance == null ) keypadViewInstance = new KeypadView();
                return keypadViewInstance;
        }

        @Override
        public void onDestroy()
        {
                super.onDestroy();
                if ( handler != null ) handler = null;
                keypadViewInstance = null;
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                this.inflater = inflater;
                view = inflater.inflate( R.layout.activity_keypad, null );
                getActivity().getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE );
                keypad_layout = ( LinearLayout ) view.findViewById( R.id.keypad_call_button );
                tvUserName = ( TextView ) view.findViewById( R.id.keypad_username );
                tvUserPhone = ( TextView ) view.findViewById( R.id.keypad_userphone );
                tvUserName.setOnClickListener( this );
                tvUserPhone.setOnClickListener( this );
                tvFindUserCount = ( TextView ) view.findViewById( R.id.keypad_usercount );
                tvUserName.setTypeface( Define.tfRegular );
                tvUserPhone.setTypeface( Define.tfRegular );
                tvFindUserCount.setTypeface( Define.tfRegular );
                tvAddContactTitle = ( TextView ) view.findViewById( R.id.keypad_addcontact_title );
                tvAddContactTitle.setTypeface( Define.tfRegular );
                ivUserIcon = ( ImageView ) view.findViewById( R.id.keypad_usericon );
                
//                ivFmcLogo = ( ImageView ) view.findViewById( R.id.keypad_iv_fmc_logo );
                ivHdLogo = ( ImageView ) view.findViewById( R.id.keypad_iv_hd );
//                switch(Define.SET_COMPANY)
//                {
//                        case Define.MBC:
//                                ivFmcLogo.setVisibility( View.INVISIBLE );
//                                ivHdLogo.setVisibility( View.INVISIBLE );
//                                break;
//                        default:
//                                ivFmcLogo.setVisibility( View.VISIBLE );
//                                ivHdLogo.setVisibility( View.VISIBLE );
//                                break;
//                }
                
                etInputNumber = ( TextView ) view.findViewById( R.id.keypad_tv_input_number );
                etInputNumber.setTypeface( Define.tfRegular );
                etInputNumber.setText( "" );
                btnAddContact = ( Button ) view.findViewById( R.id.keypad_btnaddcontact );
                btnAddContact.setOnClickListener( this );
                keypad_addcontact_title = ( TextView ) view.findViewById( R.id.keypad_addcontact_title );
                keypad_addcontact_title.setOnClickListener( this );
                btnNum0 = ( Button ) view.findViewById( R.id.keypad_0 );
                btnNum0.setOnClickListener( this );
                btnNum1 = ( Button ) view.findViewById( R.id.keypad_1 );
                btnNum1.setOnClickListener( this );
                btnNum2 = ( Button ) view.findViewById( R.id.keypad_2 );
                btnNum2.setOnClickListener( this );
                btnNum3 = ( Button ) view.findViewById( R.id.keypad_3 );
                btnNum3.setOnClickListener( this );
                btnNum4 = ( Button ) view.findViewById( R.id.keypad_4 );
                btnNum4.setOnClickListener( this );
                btnNum5 = ( Button ) view.findViewById( R.id.keypad_5 );
                btnNum5.setOnClickListener( this );
                btnNum6 = ( Button ) view.findViewById( R.id.keypad_6 );
                btnNum6.setOnClickListener( this );
                btnNum7 = ( Button ) view.findViewById( R.id.keypad_7 );
                btnNum7.setOnClickListener( this );
                btnNum8 = ( Button ) view.findViewById( R.id.keypad_8 );
                btnNum8.setOnClickListener( this );
                btnNum9 = ( Button ) view.findViewById( R.id.keypad_9 );
                btnNum9.setOnClickListener( this );
                btnNumStar = ( ImageButton ) view.findViewById( R.id.keypad_star );
                btnNumStar.setOnClickListener( this );
                btnNumSharp = ( ImageButton ) view.findViewById( R.id.keypad_sharp );
                btnNumSharp.setOnClickListener( this );
                btnCall = ( Button ) view.findViewById( R.id.keypad_btncall );
                btnCall.setOnClickListener( this );
                btnCall.setOnTouchListener( this );
                btnDel = ( Button ) view.findViewById( R.id.keypad_btndel );
                btnDel.setOnClickListener( this );
                btnDel.setOnLongClickListener( this );
                btnArrow = ( ImageButton ) view.findViewById( R.id.keypad_arrow );
                btnArrow.setOnClickListener( this );
                tvFindUserCount.setOnClickListener( this );
//                tvCall = ( TextView ) view.findViewById( R.id.keypad_tvcall );
//                tvCall.setTypeface( Define.tfRegular );
//                tvCall.setOnClickListener( this );
//                tvCall.setOnTouchListener( this );
                result = new ArrayList<Contact>();
                layout_find = ( LinearLayout ) view.findViewById( R.id.layout_find );
                layout_find.setVisibility( View.INVISIBLE );
                layout_addContact = ( LinearLayout ) view.findViewById( R.id.layout_addcontact );
                layout_addContact.setVisibility( View.GONE );
                return view;
        }

        @Override
        public void onClick( View v )
        {
                try
                {
                        switch ( v.getId() )
                        {
                        case R.id.keypad_userphone :
                        case R.id.keypad_username :
                                String number = tvUserPhone.getText().toString();
                                if ( !number.equals( "" ) ) etInputNumber.setText( number );
                                break;
                        case R.id.keypad_arrow :
                        case R.id.keypad_usercount :
                                showDialog();
                                break;
                        case R.id.keypad_0 :
                                ret = etInputNumber.getText().toString();
                                ret += "0";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_star :
                                ret = etInputNumber.getText().toString();
                                ret += "*";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_sharp :
                                ret = etInputNumber.getText().toString();
                                ret += "#";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_1 :
                                ret = etInputNumber.getText().toString();
                                ret += "1";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_2 :
                                ret = etInputNumber.getText().toString();
                                ret += "2";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_3 :
                                ret = etInputNumber.getText().toString();
                                ret += "3";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_4 :
                                ret = etInputNumber.getText().toString();
                                ret += "4";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_5 :
                                ret = etInputNumber.getText().toString();
                                ret += "5";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_6 :
                                ret = etInputNumber.getText().toString();
                                ret += "6";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_7 :
                                ret = etInputNumber.getText().toString();
                                ret += "7";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_8 :
                                ret = etInputNumber.getText().toString();
                                ret += "8";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_9 :
                                ret = etInputNumber.getText().toString();
                                ret += "9";
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
//                        case R.id.keypad_tvcall :
//                                FmcSendBroadcast.FmcSendCall( etInputNumber.getText().toString().trim() ,0, getActivity().getApplicationContext()); //2016-03-31
//                                etInputNumber.setText( "" );
//                                searchNumberData();
//                                break;
                        case R.id.keypad_btncall :
                    			FmcSendBroadcast.FmcSendCall( etInputNumber.getText().toString().trim() ,0, getActivity().getApplicationContext()); //2016-03-31
                            	etInputNumber.setText( "" );
                            	searchNumberData();
                                break;
                        case R.id.keypad_btndel :
                                Log.e( "Keypad", "onClick del" );
                                ret = etInputNumber.getText().toString();
                                ret = ret.substring( 0, ret.length() - 1 );
                                etInputNumber.setText( PhoneNumberUtils.formatNumber( ret ) );
                                searchNumberData();
                                break;
                        case R.id.keypad_btnaddcontact :
                        case R.id.keypad_addcontact_title :
                                Intent selectWindow = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.ContactAddView.class );
                                selectWindow.putExtra( "number", etInputNumber.getText().toString().trim().replaceAll( "-", "" ) );
                                selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( selectWindow );
                                break;
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        private void searchNumberData()
        {
                result.clear();
                String findStr = etInputNumber.getText().toString().trim().replaceAll( "-", "" );
                int findCount = 0;
                String firstFindName = "", firstFindNumber = "";
                if ( findStr.equals( "" ) )
                {
                        Message m = KeypadView.instance().handler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                        KeypadView.instance().handler.sendMessage( m );
                        return;
                }
                else btnDel.setVisibility( View.VISIBLE );
                for ( Contact ct : Define.contactArray )
                {
                        if ( ct == null || ct.getType() == null ) continue;
                        if ( ct.getType().equals( "Buddy" ) )
                        {
                                if ( ct.getPhonenum().indexOf( findStr ) >= 0 )
                                {
                                        if ( firstFindName.equals( "" ) )
                                        {
                                                firstFindName = ct.getName();
                                                firstFindNumber = ct.getPhonenum();
                                        }
                                        result.add( ct );
                                        findCount++;
                                }
                                if ( ct.getTelnum().indexOf( findStr ) >= 0 )
                                {
                                        if ( firstFindName.equals( "" ) )
                                        {
                                                firstFindName = ct.getName();
                                                firstFindNumber = ct.getTelnum();
                                        }
                                        result.add( ct );
                                        findCount++;
                                }
                        }
                        else if ( ct.getType().equals( "Device" ) )
                        {
                                if ( ct.getPhonenum().indexOf( findStr ) >= 0 )
                                {
                                        if ( firstFindName.equals( "" ) )
                                        {
                                                firstFindName = ct.getName();
                                                firstFindNumber = ct.getPhonenum();
                                        }
                                        result.add( ct );
                                        findCount++;
                                }
                                if ( ct.getTelnum().indexOf( findStr ) >= 0 )
                                {
                                        if ( firstFindNumber.equals( "" ) )
                                        {
                                                firstFindName = ct.getName();
                                                firstFindNumber = ct.getTelnum();
                                        }
                                        result.add( ct );
                                        findCount++;
                                }
                        }
                }
                if ( findCount > 0 )
                {
                        tvUserName.setVisibility( View.VISIBLE );
                        tvUserPhone.setVisibility( View.VISIBLE );
                        tvFindUserCount.setVisibility( View.VISIBLE );
                        ivUserIcon.setVisibility( View.VISIBLE );
                        btnArrow.setVisibility( View.VISIBLE );
                        tvUserName.setText( firstFindName );
                        tvUserPhone.setText( PhoneNumberUtils.formatNumber( firstFindNumber ) );
                        // tvUserPhone.setText( firstFindNumber.replaceAll( "-", "" ) );
                        tvFindUserCount.setText( Integer.toString( findCount ) );
                        layout_addContact.setVisibility( View.GONE );
                        layout_find.setVisibility( View.VISIBLE );
                }
                else
                {
                        if ( !findStr.equals( "" ) )
                        {
                                layout_addContact.setVisibility( View.VISIBLE );
                                layout_find.setVisibility( View.GONE );
                        }
                        else
                        {
                                layout_addContact.setVisibility( View.GONE );
                                layout_find.setVisibility( View.GONE );
                        }
                        tvUserName.setVisibility( View.INVISIBLE );
                        tvUserPhone.setVisibility( View.INVISIBLE );
                        tvFindUserCount.setVisibility( View.INVISIBLE );
                        ivUserIcon.setVisibility( View.INVISIBLE );
                        btnArrow.setVisibility( View.INVISIBLE );
                }
        }

        private void showDialog()
        {
                ArrayList<String> nowResult = new ArrayList<String>();
                for ( int i = 0; i < result.size(); i++ )
                {
                        nowResult.add( result.get( i ).getContactId() );
                }
                Intent it = new Intent( getActivity(), ContactResultDialog.class );
                it.putExtra( "result", nowResult );
                it.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                MainActivity.Instance().startActivity( it );
        }
        public Handler handler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REFRESH )
                                {
                                        String phoneNumber = ( String ) msg.obj;
                                        etInputNumber.setText( PhoneNumberUtils.formatNumber( phoneNumber ) );
                                        searchNumberData();
                                }
                                else if ( msg.what == Define.AM_CLEAR_ITEM )
                                {
                                        if ( tvUserName != null ) tvUserName.setVisibility( View.INVISIBLE );
                                        if ( tvUserPhone != null ) tvUserPhone.setVisibility( View.INVISIBLE );
                                        if ( tvFindUserCount != null ) tvFindUserCount.setVisibility( View.INVISIBLE );
                                        if ( ivUserIcon != null ) ivUserIcon.setVisibility( View.INVISIBLE );
                                        if ( btnArrow != null ) btnArrow.setVisibility( View.INVISIBLE );
                                        if ( etInputNumber != null ) etInputNumber.setText( "" );
                                        if ( btnDel != null ) btnDel.setVisibility( View.INVISIBLE );
                                        if ( layout_addContact != null ) layout_addContact.setVisibility( View.GONE );
                                        if ( layout_find != null ) layout_find.setVisibility( View.INVISIBLE );
                                }
                                else if ( msg.what == Define.AM_CALL_STATE_OFFHOOK )
                                {}
                                else if ( msg.what == Define.AM_CALL_STATE_IDLE )
                                {}
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };

        @Override
        public boolean onLongClick( View v )
        {
                Log.e( "Keypad", "onLongClick" );
                etInputNumber.setText( "" );
                searchNumberData();
                return true;
        }

        @Override
        public boolean onTouch( View v, MotionEvent event )
        {
                if ( event.getAction() == MotionEvent.ACTION_DOWN )
                {
                        keypad_layout.setBackgroundColor( 0xFF8D9DD8 );
                }
                else if ( event.getAction() == MotionEvent.ACTION_UP )
                {
                        keypad_layout.setBackgroundColor( Color.TRANSPARENT );
                }
                return false;
        }
}
