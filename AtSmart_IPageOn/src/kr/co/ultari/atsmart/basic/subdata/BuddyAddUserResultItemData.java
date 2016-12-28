package kr.co.ultari.atsmart.basic.subdata;

public class BuddyAddUserResultItemData {
        public String id;
        public String high;
        public String name;
        public int icon;
        public String nickName;
        public boolean checked;

        public BuddyAddUserResultItemData( String id, String high, String name, int icon, String nickName, boolean checked )
        {
                this.id = id;
                this.high = high;
                this.name = name;
                this.icon = icon;
                this.nickName = nickName;
                this.checked = checked;
        }
}
