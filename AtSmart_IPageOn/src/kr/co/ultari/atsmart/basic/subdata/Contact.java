package kr.co.ultari.atsmart.basic.subdata;

import java.io.InputStream;
import java.io.Serializable;
import kr.co.ultari.atsmart.basic.Define;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

public class Contact {
        // 초기에 가져오는 데이터들
        private String mContactId = "";
        private long mPhotoid = -1;
        private String mUserId = "";
        private String mName = "";
        private int mHasPhoneNumber = -1;
        // 필요에 따라 가져오는 데이터들
        private String mPhonenum = "";
        private String mType = "";
        private String mTelNum = "";
        private String mEmail = "";
        private String mCompany = "";
        private String mPosition = "";
        public String userId = "";
        public String userName = "";
        private String mNickName = "";
        private Bitmap myBitmap = null;
        private boolean m_bBitmapSetted = false;
        private String job = "";
        private String grade = "";
        
        // public Bitmap photo = null;
        public synchronized Bitmap getBitmap()
        {
                if ( m_bBitmapSetted ) return myBitmap;
                if ( mContactId == null ) return null;
                if ( m_bBitmapSetted == false )
                {
                        m_bBitmapSetted = true;
                        Uri contactUri = ContentUris.withAppendedId( Contacts.CONTENT_URI, Long.parseLong( mContactId ) );
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

        public void setHasPhoneNumber( int i )
        {
                mHasPhoneNumber = i;
        }

        public void setContactId( String contactId )
        {
                mContactId = contactId;
                setPhotoid( Long.parseLong( mContactId ) );
        }

        public String getContactId()
        {
                return mContactId;
        }

        public String getNickName()
        {
                return this.mNickName;
        }

        public void setNickName( String nick )
        {
                this.mNickName = nick;
        }

        public String getPosition()
        {
                setCompanyTitle();
                return this.mPosition;
        }

        public void setPosition( String pos )
        {
                this.mPosition = pos;
        }
        
        public String getJob()
        {
                return this.job;
        }
        
        public void setJob(String job)
        {
                this.job = job;
        }

        public String getGrade()
        {
                return this.grade;
        }
        
        public void setGrade(String grade)
        {
                this.grade = grade;
        }
        
        private void setCompanyTitle()
        {
                if ( mCompany == null || mPosition == null )
                {
                        mPosition = "";
                        mCompany = "";
                        if ( mHasPhoneNumber > 0 )
                        {
                                ContentResolver cr = Define.getContext().getContentResolver();
                                String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                                String[] orgWhereParams = new String[] { mContactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };
                                Cursor orgCur = cr.query( ContactsContract.Data.CONTENT_URI, null, orgWhere, orgWhereParams, null );
                                if ( orgCur.moveToFirst() )
                                {
                                        mPosition = orgCur.getString( orgCur.getColumnIndex( ContactsContract.CommonDataKinds.Organization.TITLE ) );
                                        mCompany = orgCur.getString( orgCur.getColumnIndex( ContactsContract.CommonDataKinds.Organization.COMPANY ) );
                                }
                                orgCur.close();
                        }
                }
        }

        public String getCompany()
        {
                setCompanyTitle();
                return this.mCompany;
        }

        public void setCompany( String company )
        {
                this.mCompany = company;
        }

        public String getEmail()
        {
                if ( mEmail == null )
                {
                        mEmail = "";
                        if ( mHasPhoneNumber > 0 )
                        {
                                ContentResolver cr = Define.getContext().getContentResolver();
                                Cursor emailCur = cr.query( ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { mContactId }, null );
                                while ( emailCur.moveToNext() )
                                        mEmail = emailCur.getString( emailCur.getColumnIndex( ContactsContract.CommonDataKinds.Email.DATA ) );
                                emailCur.close();
                        }
                        else
                        {
                                mEmail = "";
                        }
                }
                return this.mEmail;
        }

        public void setEmail( String email )
        {
                this.mEmail = email;
        }

        public String getTelnum()
        {
                setPhoneTelNum();
                return this.mTelNum;
        }

        public void setTelnum( String telnum )
        {
                this.mTelNum = telnum;
        }

        public String getUserid()
        {
                return this.mUserId;
        }

        public void setUserid( String userid )
        {
                this.mUserId = userid;
        }

        public String getType()
        {
                return this.mType;
        }

        public void setType( String type )
        {
                this.mType = type;
        }

        public long getPhotoid()
        {
                return mPhotoid;
        }

        public void setPhotoid( long photoid )
        {
                this.mPhotoid = photoid;
        }

        private void setPhoneTelNum()
        {
                if ( mPhonenum == null && mTelNum == null )
                {
                        mTelNum = "";
                        mPhonenum = "";
                        if ( mHasPhoneNumber > 0 )
                        {
                                Cursor pCur = Define
                                                .getContext()
                                                .getContentResolver()
                                                .query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { mContactId }, null );
                                while ( pCur.moveToNext() )
                                {
                                        String PhoneType = pCur.getString( pCur.getColumnIndex( ContactsContract.CommonDataKinds.Phone.TYPE ) );
                                        String PhoneNo = pCur.getString( pCur.getColumnIndex( ContactsContract.CommonDataKinds.Phone.DATA ) );
                                        if ( PhoneType.equals( "1" ) ) mTelNum = PhoneNo;
                                        else if ( PhoneType.equals( "2" ) ) mPhonenum = PhoneNo;
                                }
                                pCur.close();
                        }
                }
        }

        public String getPhonenum()
        {
                setPhoneTelNum();
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
