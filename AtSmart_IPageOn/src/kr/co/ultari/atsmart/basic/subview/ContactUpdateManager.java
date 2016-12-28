package kr.co.ultari.atsmart.basic.subview;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

public class ContactUpdateManager {
        // update Email
        public static void updateEmailData( ContentResolver cr, int rawContactId, String data )
        {
                String emailWhere = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Contacts.Data.MIMETYPE + " = ? AND "
                                + ContactsContract.CommonDataKinds.Email.TYPE + " = ?";
                String[] params = { String.valueOf( rawContactId ), ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                                String.valueOf( ContactsContract.CommonDataKinds.Email.TYPE_WORK ) };
                ContentValues values = new ContentValues();
                values.put( ContactsContract.CommonDataKinds.Email.DATA, data );
                int result = cr.update( ContactsContract.Data.CONTENT_URI, values, emailWhere, params );
                Log.d( "ContactUpdateManager", "update email result : " + result );
        }

        // update Phone
        public static void updatePhoneData( ContentResolver cr, int rawContactId, String data )
        {
                String phoneWhere = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Contacts.Data.MIMETYPE + " = ? AND "
                                + ContactsContract.CommonDataKinds.Phone.TYPE + "= ?";
                String[] params = { String.valueOf( rawContactId ), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                String.valueOf( ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ) };
                ContentValues values = new ContentValues();
                values.put( ContactsContract.CommonDataKinds.Phone.NUMBER, data );
                int result = cr.update( ContactsContract.Data.CONTENT_URI, values, phoneWhere, params );
                Log.d( "ContactUpdateManager", "update phone result : " + result );
        }

        // update home
        public static void updateHomeData( ContentResolver cr, int rawContactId, String data )
        {
                String phoneWhere = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Contacts.Data.MIMETYPE + " = ? AND "
                                + ContactsContract.CommonDataKinds.Phone.TYPE + "= ?";
                String[] params = { String.valueOf( rawContactId ), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                                String.valueOf( ContactsContract.CommonDataKinds.Phone.TYPE_HOME ) };
                ContentValues values = new ContentValues();
                values.put( ContactsContract.CommonDataKinds.Phone.NUMBER, data );
                int result = cr.update( ContactsContract.Data.CONTENT_URI, values, phoneWhere, params );
                Log.d( "ContactUpdateManager", "update home result : " + result );
        }

        // get RawId
        public static int getRawContactId( Context context, String phoneNumber )
        {
                String[] item = { Phone._ID, Phone.RAW_CONTACT_ID, Phone.DISPLAY_NAME };
                phoneNumber = phoneNumber.replace( "-", "" );
                String phoneNumberFormatNumber = PhoneNumberUtils.formatNumber( phoneNumber );
                // RawContacId를 가져올 where문 - Phone정보 중 매칭된것 하나만 있어도 조건충족, "-"이 들어간 전화번호도 검색.
                String where = Phone.NUMBER + " IN ('" + phoneNumber + "', '" + phoneNumberFormatNumber + "') ";
                ContentResolver cr = context.getContentResolver();
                Cursor cursor = cr.query( Phone.CONTENT_URI, item, where, null, null );
                cursor.moveToFirst();
                int rawContactId = cursor.getInt( cursor.getColumnIndex( Phone.RAW_CONTACT_ID ) );
                Log.d( "ContactUpdateManager", "rawId:" + rawContactId + ", phonumber:" + phoneNumber );
                return rawContactId;
        }

        // insert phone
        public static void insertPhoneNumberData( ContentResolver cv, int rawContactId, String phoneNumber )
        {
                String mimeType = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
                String typeKey = ContactsContract.CommonDataKinds.Phone.TYPE;
                ContentValues values = new ContentValues();
                values.put( ContactsContract.Data.RAW_CONTACT_ID, rawContactId );
                values.put( ContactsContract.Data.MIMETYPE, mimeType );
                values.put( typeKey, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE );
                values.put( ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber );
                Uri result = cv.insert( ContactsContract.Data.CONTENT_URI, values );
                Log.d( "ContactUpdateManager", "insert phone result : " + result );
        }

        // insert home
        public static void insertHomeNumberData( ContentResolver cv, int rawContactId, String phoneNumber )
        {
                String mimeType = ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE;
                String typeKey = ContactsContract.CommonDataKinds.Phone.TYPE;
                ContentValues values = new ContentValues();
                values.put( ContactsContract.Data.RAW_CONTACT_ID, rawContactId );
                values.put( ContactsContract.Data.MIMETYPE, mimeType );
                values.put( typeKey, ContactsContract.CommonDataKinds.Phone.TYPE_HOME );
                values.put( ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber );
                Uri result = cv.insert( ContactsContract.Data.CONTENT_URI, values );
                Log.d( "ContactUpdateManager", "insert home result : " + result );
        }

        // insert email
        public static void insertEmailNumberData( ContentResolver cv, int rawContactId, String email )
        {
                String mimeType = ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE;
                String typeKey = ContactsContract.CommonDataKinds.Email.TYPE;
                ContentValues values = new ContentValues();
                values.put( ContactsContract.Data.RAW_CONTACT_ID, rawContactId );
                values.put( ContactsContract.Data.MIMETYPE, mimeType );
                values.put( typeKey, ContactsContract.CommonDataKinds.Email.TYPE_WORK );
                values.put( ContactsContract.CommonDataKinds.Email.DATA, email );
                Uri result = cv.insert( ContactsContract.Data.CONTENT_URI, values );
                Log.d( "ContactUpdateManager", "insert Email result : " + result );
        }
}
