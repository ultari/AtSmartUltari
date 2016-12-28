package kr.co.ultari.atsmart.basic.subdata;

public class BuddyUserData {
        public String userId;
        public String userName;
        public String userTelNum;
        public String userMobileNum;

        public BuddyUserData( String id, String name, String tel, String mobile )
        {
                this.userId = id;
                this.userName = name;
                this.userTelNum = tel;
                this.userMobileNum = mobile;
        }
}
