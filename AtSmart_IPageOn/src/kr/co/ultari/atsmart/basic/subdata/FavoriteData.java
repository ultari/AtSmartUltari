package kr.co.ultari.atsmart.basic.subdata;

import java.io.Serializable;

public class FavoriteData implements Serializable {
        private long mPhotoid;
        private String mHomeNum;
        private String mPhonenum;
        private String mName;
        private String mType;
        private UserObject user;
        private String isCheck;
        public String userId = null;
        public String userName = null;

        public String getIsCheck()
        {
                return this.isCheck;
        }

        public void setIsCheck( String check )
        {
                this.isCheck = check;
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

        public UserObject getUser()
        {
                return user;
        }

        public void setUser( UserObject user )
        {
                this.user = user;
        }

        public String getPhonenum()
        {
                return mPhonenum;
        }

        public void setPhonenum( String phonenum )
        {
                this.mPhonenum = phonenum;
        }

        public String getHomenum()
        {
                return mHomeNum;
        }

        public void setHomenum( String homeNum )
        {
                this.mHomeNum = homeNum;
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
