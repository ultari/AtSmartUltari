package kr.co.ultari.atsmart.basic.subdata;

import java.io.InputStream;
import java.io.Serializable;
import kr.co.ultari.atsmart.basic.Define;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

public class CallLogData {
        private long mPhotoid = -1;
        private String mPhonenum;
        private String mName;
        private String mCallStatus;
        private String mCallDate;
        private String mCallDuration;
        private String mCallid;
        private String isCheck;
        private String logType;
        private Bitmap myBitmap = null;
        private boolean m_bBitmapSetted = false;
        private String contactId = null;
        public String userId = null;
        public String userName = null;

        public synchronized Bitmap getBitmap()
        {
                if ( m_bBitmapSetted ) return myBitmap;
                if ( m_bBitmapSetted == false )
                {
                        m_bBitmapSetted = true;
                        if ( contactId == null ) contactId = Define.getPhoneNumberToRawContactId( mPhonenum );
                        if ( contactId.equals( "" ) ) return null;
                        Uri contactUri = ContentUris.withAppendedId( Contacts.CONTENT_URI, Long.parseLong( contactId ) );
                        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream( Define.getContext().getContentResolver(), contactUri );
                        if ( input != null )
                        {
                                try
                                {
                                        myBitmap = BitmapFactory.decodeStream( input );
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
                                                {
                                                        Log.e( "kr.co.ultari.atsmart.basic.subdata.Contact", "getBitmap", e );
                                                }
                                        }
                                }
                        }
                }
                return myBitmap;
        }

        public void setBitmap( Bitmap bmp )
        {
                myBitmap = bmp;
        }

        public String getLogtype()
        {
                return this.logType;
        }

        public void setLogtype( String log )
        {
                this.logType = log;
        }

        public String getIsCheck()
        {
                return this.isCheck;
        }

        public void setIsCheck( String check )
        {
                this.isCheck = check;
        }

        public String getCallid()
        {
                return this.mCallid;
        }

        public void setCallid( String id )
        {
                this.mCallid = id;
        }

        public String getCallDate()
        {
                return this.mCallDate;
        }

        public void setCallDate( String date )
        {
                this.mCallDate = date;
        }

        public String getCallDuration()
        {
                return this.mCallDuration;
        }

        public void setCallDuration( String duration )
        {
                this.mCallDuration = duration;
        }

        public String getCallStatus()
        {
                return this.mCallStatus;
        }
        
        public String getCallStatusString()
        {
        	if ( this.mCallStatus.equals("1") ) return "수신";
        	else if ( this.mCallStatus.equals("2") ) return "발신";
        	else return "부재중";
        }

        public void setCallStatus( String status )
        {
                this.mCallStatus = status;
        }

        public long getPhotoid()
        {
                if ( mPhotoid < 0 )
                {
                        if ( contactId == null ) contactId = Define.getPhoneNumberToRawContactId( mPhonenum );
                        if ( contactId.equals( "" ) ) setPhotoid( 0 );
                        else setPhotoid( Long.parseLong( contactId ) );
                }
                return mPhotoid;
        }

        public void setPhotoid( long photoid )
        {
                this.mPhotoid = photoid;
        }

        public String getPhonenum()
        {
                return mPhonenum;
        }

        public void setPhonenum( String phonenum )
        {
                this.mPhonenum = phonenum;
        }

        public String getName()
        {
                return mName;
        }

        public void setName( String name )
        {
                this.mName = name;
        }

        public String getOrgUserName()
        {
                if ( userName == null ) return mName;
                else
                {
                        if ( userName.indexOf( '#' ) > 0 )
                        {
                                String name = userName.substring( 0, userName.indexOf( '#' ) );
                                String position = userName.substring( userName.indexOf( '#' ) + 1 );
                                if ( position.indexOf( '#' ) > 0 ) position = position.substring( 0, position.indexOf( '#' ) );
                                return name + " " + position;
                        }
                        else return userName;
                }
        }
}
