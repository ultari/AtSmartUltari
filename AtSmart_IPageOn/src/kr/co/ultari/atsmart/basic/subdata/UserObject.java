package kr.co.ultari.atsmart.basic.subdata;

public class UserObject {
        private String mUserId;
        private String mParentId;
        private String mUserName;
        private String mUserInfo;
        private int icon;
        private String order;
        private String[] userInfo;

        public UserObject( String id, String high, String name, String info, int icon, String order )
        {
                this.mUserId = id;
                this.mParentId = high;
                this.mUserName = name;
                this.mUserInfo = info;
                this.icon = icon;
                this.order = order;
                init();
        }

        private void init()
        {
                // userInfo = mUserName.split("#", -1);
                // 2015-05-11
                userInfo = mUserInfo.split( "#", -1 );
        }

        public String getUserNameFieldIndex( int index )
        {
                String info = "";
                try
                {
                        info = userInfo[index];
                }
                catch ( Exception e )
                {
                        info = "";
                }
                return info;
        }

        public String getUserId()
        {
                return this.mUserId;
        }

        public String getParentId()
        {
                return this.mParentId;
        }

        public String getUserName()
        {
                return this.mUserName;
        }

        public String getInfo()
        {
                return this.mUserInfo;
        }

        public int getUserIcon()
        {
                return this.icon;
        }

        public String getUserOrder()
        {
                return this.order;
        }
}
