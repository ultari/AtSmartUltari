package kr.co.ultari.atsmart.basic.subview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.view.ContactView;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactAddView extends MessengerActivity implements OnClickListener {
        private Button btnCancel, btnSave;
        private ImageView ivPhoto;
        private EditText etName, etMobile, etTelephone, etEmail, etCompany, etPosition;
        private static final int GET_PICTURE_URI = 100, REQ_CODE_PICK_IMAGE = 101;
        private Bitmap tempBitMap = null;
        private TextView tvTitle, tvNumberTitle, tvEmailTitle, tvOrgTitle, tvMobileMsg, tvTelephoneMsg, tvEmailMsg;
        private static final String TEMP_PHOTO_FILE = "temporary_holder.jpg";
        private Bitmap selectedImage = null;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
                setContentView( R.layout.contact_add_dialog );

                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE );
                
                btnCancel = ( Button ) findViewById( R.id.contact_add_btncancel );
                btnCancel.setOnClickListener( this );
                btnCancel.setTypeface( Define.tfRegular );
                btnSave = ( Button ) findViewById( R.id.contact_add_save );
                btnSave.setOnClickListener( this );
                btnSave.setTypeface( Define.tfRegular );
                ivPhoto = ( ImageView ) findViewById( R.id.contact_add_photo );
                ivPhoto.setOnClickListener( this );
                tvTitle = ( TextView ) findViewById( R.id.contact_add_title );
                tvNumberTitle = ( TextView ) findViewById( R.id.contact_number_title );
                tvEmailTitle = ( TextView ) findViewById( R.id.contact_email_title );
                tvOrgTitle = ( TextView ) findViewById( R.id.contact_org_title );
                tvMobileMsg = ( TextView ) findViewById( R.id.contact_add_mobile_msg );
                tvTelephoneMsg = ( TextView ) findViewById( R.id.contact_add_telephone_msg );
                tvEmailMsg = ( TextView ) findViewById( R.id.contact_add_person_msg );
                etName = ( EditText ) findViewById( R.id.contact_add_name );
                etMobile = ( EditText ) findViewById( R.id.contact_add_mobile );
                etTelephone = ( EditText ) findViewById( R.id.contact_add_telephone );
                etEmail = ( EditText ) findViewById( R.id.contact_add_email );
                etCompany = ( EditText ) findViewById( R.id.contact_add_company );
                etPosition = ( EditText ) findViewById( R.id.contact_add_position );
                tvTitle.setTypeface( Define.tfMedium );
                tvNumberTitle.setTypeface( Define.tfRegular );
                tvEmailTitle.setTypeface( Define.tfRegular );
                tvOrgTitle.setTypeface( Define.tfRegular );
                etName.setTypeface( Define.tfRegular );
                etMobile.setTypeface( Define.tfRegular );
                etTelephone.setTypeface( Define.tfRegular );
                etEmail.setTypeface( Define.tfRegular );
                etCompany.setTypeface( Define.tfRegular );
                etPosition.setTypeface( Define.tfRegular );
                tvMobileMsg.setTypeface( Define.tfLight );
                tvTelephoneMsg.setTypeface( Define.tfLight );
                tvEmailMsg.setTypeface( Define.tfLight );
                // 2015-05-01
                if ( getIntent().getStringExtra( "number" ) != null )
                {
                        String ret = getIntent().getStringExtra( "number" );
                        if ( isPhoneNumber( ret ) ) etMobile.setText( PhoneNumberUtils.formatNumber( ret ) );
                        else etTelephone.setText( PhoneNumberUtils.formatNumber( ret ) );
                }
        }

        private boolean isPhoneNumber( String number )
        {
                for ( int i = 0; i < StringUtil.number.length; i++ )
                {
                        if ( number.startsWith( StringUtil.number[i] ) ) return true;
                }
                return false;
        }

        public void hideKeyboard()
        {
                InputMethodManager imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                if ( etName.getWindowToken() != null ) imm.hideSoftInputFromWindow( etName.getWindowToken(), 0 );
                if ( etMobile.getWindowToken() != null ) imm.hideSoftInputFromWindow( etMobile.getWindowToken(), 0 );
                if ( etTelephone.getWindowToken() != null ) imm.hideSoftInputFromWindow( etTelephone.getWindowToken(), 0 );
                if ( etEmail.getWindowToken() != null ) imm.hideSoftInputFromWindow( etEmail.getWindowToken(), 0 );
                if ( etCompany.getWindowToken() != null ) imm.hideSoftInputFromWindow( etCompany.getWindowToken(), 0 );
                if ( etPosition.getWindowToken() != null ) imm.hideSoftInputFromWindow( etPosition.getWindowToken(), 0 );
        }

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == R.id.contact_add_btncancel )
                {
                        etName.setText( "" );
                        etMobile.setText( "" );
                        etTelephone.setText( "" );
                        etEmail.setText( "" );
                        etCompany.setText( "" );
                        etPosition.setText( "" );
                        hideKeyboard();
                        finish();
                }
                else if ( v.getId() == R.id.contact_add_save )
                {
                        inputContact( etName.getText().toString().trim(), etMobile.getText().toString().trim().replaceAll( "-", "" ), etTelephone.getText()
                                        .toString().trim().replaceAll( "-", "" ), etEmail.getText().toString().trim(), etCompany.getText().toString().trim(),
                                        etPosition.getText().toString().trim() );
                        etName.setText( "" );
                        etMobile.setText( "" );
                        etTelephone.setText( "" );
                        etEmail.setText( "" );
                        etCompany.setText( "" );
                        etPosition.setText( "" );
                        hideKeyboard();
                        finish();
                        ContactView.instance().isLoadComplete = true;
                        ContactView.instance().displayListBasic();
                }
                else if ( v.getId() == R.id.contact_add_photo )
                {
                        /*
                         * Intent i = new Intent(Intent.ACTION_PICK);
                         * i.setType( android.provider.MediaStore.Images.Media.CONTENT_TYPE );
                         * i.setData( android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                         * startActivityForResult( i, GET_PICTURE_URI );
                         */
                        // 2015-05-06
                        Intent photoPickerIntent = new Intent( Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                        photoPickerIntent.setType( "image/*" );
                        photoPickerIntent.putExtra( "crop", "true" );
                        photoPickerIntent.putExtra( MediaStore.EXTRA_OUTPUT, getTempUri() );
                        photoPickerIntent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
                        startActivityForResult( photoPickerIntent, REQ_CODE_PICK_IMAGE );
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

        private void inputContact( String name, String mobile, String telephone, String email, String company, String position )
        {
                String DisplayName = name;
                String MobileNumber = mobile;
                String HomeNumber = telephone;
                String emailID = email;
                String companyName = company;
                String userPosition = position;
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
                                        Toast toast = new Toast( this );
                                        toast.setGravity( Gravity.CENTER, 0, 0 );
                                        toast.setDuration( Toast.LENGTH_SHORT );
                                        toast.setView( layout );
                                        toast.show();
                                        // Toast.makeText(ContactAddView.this,"The contact name: " + name + " already exists", Toast.LENGTH_SHORT).show();
                                        return;
                                }
                        }
                }
                //
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
                // ------------------------------------------------------ Mobile Number
                if ( MobileNumber != null )
                {
                        ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ).build() );
                }
                // ------------------------------------------------------ Home Numbers
                if ( HomeNumber != null )
                {
                        ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.NUMBER, HomeNumber )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME ).build() );
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
                /*
                 * ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                 * .withValue(ContactsContract.Data.RAW_CONTACT_ID, 0)
                 * .withValue(ContactsContract.Data.MIMETYPE, Nickname.CONTENT_ITEM_TYPE)
                 * .withValue(Note.NOTE, "모바일팀")
                 * .build());
                 */
                // ---------------------------------------------------- Organization
                ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE )
                                .withValue( ContactsContract.CommonDataKinds.Organization.COMPANY, companyName )
                                .withValue( ContactsContract.CommonDataKinds.Organization.TITLE, userPosition )
                                /*
                                 * .withValue(ContactsContract.CommonDataKinds.Organization.JOB_DESCRIPTION, "모바일 안드로이드 클라이언트 개발")
                                 * .withValue(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, "모바일팀")
                                 * .withValue(ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION, "Office location")
                                 * .withValue(ContactsContract.CommonDataKinds.Organization.SYMBOL, "Symbol")
                                 * .withValue(ContactsContract.CommonDataKinds.Organization.PHONETIC_NAME, "Phonetic name")
                                 */
                                .withValue( ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK )
                                .build() );
                // Asking the Contact provider to create a new contact
                try
                {
                        getApplicationContext().getContentResolver().applyBatch( ContactsContract.AUTHORITY, ops );
                        Contact contact = new Contact();
                        String orgWhere = ContactsContract.Contacts.DISPLAY_NAME + " = ?";
                        String[] orgWhereParams = new String[] { DisplayName };
                        Cursor orgCur = cr.query( ContactsContract.Contacts.CONTENT_URI, null, orgWhere, orgWhereParams, null );
                        if ( orgCur.moveToFirst() )
                        {
                                String contactId = orgCur.getString( orgCur.getColumnIndex( ContactsContract.Contacts._ID ) );
                                Log.d( "SaveContactId", contactId );
                                contact.setContactId( contactId );
                        }
                        orgCur.close();
                        contact.setTelnum( HomeNumber.replaceAll( "-", "" ) );
                        contact.setPhonenum( MobileNumber.replaceAll( "-", "" ) );
                        contact.setName( name );
                        contact.setPosition( userPosition );
                        contact.setCompany( companyName );
                        contact.setEmail( email );
                        contact.setType( "Device" );
                        Define.contactArray.add( 0, contact );
                        // Define.contactMap.put(Define.getContactIdWithNameAndPhoneNumber(name, MobileNumber.replaceAll("-", "")), contact);
                        Define.contactMap.put( contact.getContactId(), contact );
                        LayoutInflater inflater = getLayoutInflater();
                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) findViewById( R.id.custom_toast_layout ) );
                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                        text.setText( getString( R.string.input_success ) );
                        text.setTypeface( Define.tfRegular );
                        Toast toast = new Toast( ContactAddView.this );
                        toast.setGravity( Gravity.CENTER, 0, 0 );
                        toast.setDuration( Toast.LENGTH_SHORT );
                        toast.setView( layout );
                        toast.show();
                        if ( selectedImage != null )
                        {
                                String raw_contact_id = Define.getRawIdWithContactId( contact.getContactId() );
                                ByteArrayOutputStream image = new ByteArrayOutputStream();
                                selectedImage.compress( Bitmap.CompressFormat.JPEG, 100, image );
                                if ( MobileNumber != null && !MobileNumber.equals( "" ) )
                                {
                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getContentResolver(),
                                                        image.toByteArray(), Long.parseLong( raw_contact_id ) );
                                }
                                else if ( HomeNumber != null && !HomeNumber.equals( "" ) )
                                {
                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getContentResolver(),
                                                        image.toByteArray(), Long.parseLong( raw_contact_id ) );
                                }
                        }
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
        }

        @Override
        protected void onActivityResult( int requestCode, int resultCode, Intent data )
        {
                if ( requestCode == GET_PICTURE_URI )
                {
                        if ( resultCode == Activity.RESULT_OK )
                        {
                                Uri uri = data.getData();
                                // uri:content://media/external/images/media/5333
                                tempBitMap = null;
                                try
                                {
                                        tempBitMap = Images.Media.getBitmap( getContentResolver(), uri );
                                        // 2015-05-03
                                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) ivPhoto
                                                        .setBackground( getDrawableFromBitmap( tempBitMap ) );
                                        else ivPhoto.setBackgroundDrawable( getDrawableFromBitmap( tempBitMap ) );
                                }
                                catch ( FileNotFoundException e )
                                {
                                        e.printStackTrace();
                                }
                                catch ( IOException e )
                                {
                                        e.printStackTrace();
                                }
                        }
                }
                // 2015-05-06
                else if ( requestCode == REQ_CODE_PICK_IMAGE )
                {
                        if ( resultCode == RESULT_OK )
                        {
                                if ( data != null )
                                {
                                        // tempBitMap = null;
                                        File tempFile = getTempFile();
                                        String filePath = Environment.getExternalStorageDirectory() + "/temporary_holder.jpg";
                                        // System.out.println("path "+filePath);
                                        selectedImage = BitmapFactory.decodeFile( filePath );
                                        // tempBitMap = (ImageView) findViewById(R.id.image);
                                        // tempBitMap.setImageBitmap(selectedImage );
                                        handler.sendEmptyMessageDelayed( Define.AM_REFRESH, 1500 );
                                        /*
                                         * //2015-05-03
                                         * if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                                         * ivPhoto.setBackground( getDrawableFromBitmap(selectedImage) );
                                         * else
                                         * ivPhoto.setBackgroundDrawable( getDrawableFromBitmap(selectedImage) );
                                         */
                                        /*
                                         * if(selectedImage != null)
                                         * {
                                         * ByteArrayOutputStream image = new ByteArrayOutputStream();
                                         * selectedImage.compress(Bitmap.CompressFormat.JPEG , 100, image);
                                         * if(etMobile.getText().toString() != null)
                                         * {
                                         * String raw_contact_id = Define.getPhoneNumberToRawContactRawId( etMobile.getText().toString() );
                                         * if(!raw_contact_id.equals( "" ) && raw_contact_id != null)
                                         * setContactPhoto( getContentResolver(), image.toByteArray(), Long.parseLong(raw_contact_id) );
                                         * }
                                         * else if(etTelephone.getText().toString() != null)
                                         * {
                                         * String raw_contact_id = Define.getPhoneNumberToRawContactRawId( etTelephone.getText().toString() );
                                         * if(!raw_contact_id.equals( "" ) && raw_contact_id != null)
                                         * setContactPhoto( getContentResolver(), image.toByteArray(), Long.parseLong(raw_contact_id) );
                                         * }
                                         * }
                                         */
                                }
                        }
                }
        }

        public Drawable getDrawableFromBitmap( Bitmap bitmap )
        {
                Drawable d = new BitmapDrawable( getResources(), bitmap );
                return d;
        }

        public void setContactPhoto( ContentResolver c, byte[] bytes, long rawContactId )
        {
                ContentValues values = new ContentValues();
                int photoRow = -1;
                String where = ContactsContract.Data.RAW_CONTACT_ID + " = " + rawContactId + "                     AND " + ContactsContract.Data.MIMETYPE
                                + "=='" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";
                Cursor cursor = c.query( ContactsContract.Data.CONTENT_URI, null, where, null, null );
                int idIdx = cursor.getColumnIndexOrThrow( ContactsContract.Data._ID );
                if ( cursor.moveToFirst() )
                {
                        photoRow = cursor.getInt( idIdx );
                }
                cursor.close();
                values.put( ContactsContract.Data.RAW_CONTACT_ID, rawContactId );
                values.put( ContactsContract.Data.IS_SUPER_PRIMARY, 1 );
                values.put( ContactsContract.CommonDataKinds.Photo.PHOTO, bytes );
                values.put( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE );
                if ( photoRow >= 0 )
                {
                        c.update( ContactsContract.Data.CONTENT_URI, values, ContactsContract.Data._ID + " = " + photoRow, null );
                }
                else
                {
                        c.insert( ContactsContract.Data.CONTENT_URI, values );
                }
                // handler.sendEmptyMessageDelayed( Define.AM_REFRESH, 1500 );
        }
        public Handler handler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REFRESH )
                                {
                                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN )
                                        {
                                                if ( selectedImage != null ) ivPhoto.setImageBitmap( selectedImage );
                                                // ivPhoto.setBackground( getDrawableFromBitmap(selectedImage) );
                                        }
                                        else
                                        {
                                                if ( selectedImage != null ) ivPhoto.setImageBitmap( selectedImage );
                                                // ivPhoto.setBackgroundDrawable( getDrawableFromBitmap(selectedImage) );
                                        }
                                        // iv_UserPhoto.setDeviceImage(userPhotoId);
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };
}
