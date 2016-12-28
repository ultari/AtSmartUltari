package kr.co.ultari.atsmart.basic.subview;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.view.KeypadView;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ContactResultDialog extends MessengerActivity implements OnClickListener {
        private ListView lv_contact_resultlist;
        private ArrayList<Contact> array;
        private Button btnCancel;
        private TextView tvTitle;

        @SuppressWarnings( "unchecked" )
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.contact_result_dialog );
                lv_contact_resultlist = ( ListView ) findViewById( R.id.lv_contact_resultlist );
                array = new ArrayList<Contact>();
                ArrayList<String> keyArray = ( ArrayList<String> ) getIntent().getSerializableExtra( "result" );
                for ( int i = 0; i < keyArray.size(); i++ )
                {
                        array.add( Define.contactMap.get( keyArray.get( i ) ) );
                }
                tvTitle = ( TextView ) findViewById( R.id.contact_result_title );
                tvTitle.setTypeface( Define.tfMedium );
                btnCancel = ( Button ) findViewById( R.id.contact_result_btnCancel );
                btnCancel.setTypeface( Define.tfRegular );
                btnCancel.setOnClickListener( this );
        }

        @Override
        public void onResume()
        {
                super.onResume();
                ContactsAdapter adapter = new ContactsAdapter( ContactResultDialog.this, R.layout.layout_contact_findlist, array );
                lv_contact_resultlist.setAdapter( adapter );
                lv_contact_resultlist.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick( AdapterView<?> contactlist, View v, int position, long resid )
                        {
                                Contact phonenumber = ( Contact ) contactlist.getItemAtPosition( position );
                                if ( phonenumber == null ) return;
                                Message m = KeypadView.instance().handler.obtainMessage( Define.AM_REFRESH, phonenumber.getPhonenum() );
                                KeypadView.instance().handler.sendMessage( m );
                                finish();
                        }
                } );
        }
        private class ContactsAdapter extends ArrayAdapter<Contact> {
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
                        ViewHolder holder;
                        Contact acontact = contactlist.get( position );
                        v = Inflater.inflate( resId, null );
                        holder = new ViewHolder();
                        holder.tv_name = ( TextView ) v.findViewById( R.id.contact_find_name );
                        holder.tv_phonenumber = ( TextView ) v.findViewById( R.id.contact_find_phonenumber );
                        holder.iv_photoid = ( ImageView ) v.findViewById( R.id.contact_find_photo );
                        v.setTag( holder );
                        if ( acontact != null )
                        {
                                holder.tv_name.setTypeface( Define.tfRegular );
                                holder.tv_phonenumber.setTypeface( Define.tfRegular );
                                holder.tv_name.setText( acontact.getName() );
                                if ( acontact.getPhonenum() != null && !acontact.getPhonenum().equals( "" ) ) holder.tv_phonenumber.setText( PhoneNumberUtils
                                                .formatNumber( acontact.getPhonenum() ) );
                                else holder.tv_phonenumber.setText( PhoneNumberUtils.formatNumber( acontact.getTelnum() ) );
                                Bitmap bm = null;
                                if ( acontact.getType().equals( "Buddy" ) )
                                {
                                        bm = Define.getBitmap( acontact.getUserid() );
                                        holder.iv_photoid.setImageBitmap( bm );
                                }
                                else if ( acontact.getType().equals( "Device" ) )
                                {
                                        bm = openPhoto( acontact.getPhotoid() );
                                        if ( bm != null ) holder.iv_photoid.setImageBitmap( bm );
                                        else holder.iv_photoid.setImageDrawable( getResources().getDrawable( R.drawable.img_contract_list ) );
                                }
                        }
                        return v;
                }

                private Bitmap openPhoto( long contactId )
                {
                        Log.d( "GetPhoto", "ContactId : " + contactId );
                        Uri contactUri = ContentUris.withAppendedId( Contacts.CONTENT_URI, contactId );
                        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream( context.getContentResolver(), contactUri );
                        if ( input != null ) return BitmapFactory.decodeStream( input );
                        return null;
                }
                private class ViewHolder {
                        ImageView iv_photoid;
                        TextView tv_name;
                        TextView tv_phonenumber;
                        // Button btn_chat;
                        // Button btn_call;
                }
                /*
                 * @Override public void onClick( View v ) { ContactEventTag tag
                 * = (ContactEventTag)v.getTag();
                 * if ( tag.type == Define.CONTACT_CHAT ) {
                 * } else if( tag.type == Define.CONTACT_CALL ) {
                 * startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
                 * + tag.data.getPhonenum()))); } }
                 */
        }

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == R.id.contact_result_btnCancel )
                {
                        finish();
                }
        }
}
