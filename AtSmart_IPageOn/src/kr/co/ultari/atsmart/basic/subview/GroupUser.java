package kr.co.ultari.atsmart.basic.subview;

public class GroupUser {
        String id;
        String name;
        String nick;
        int photo;

        public GroupUser( String _id, String _name, int _photo, String _nick )
        {
                this.id = _id;
                this.name = _name;
                this.photo = _photo;
                this.nick = _nick;
        }

        public String getId()
        {
                return id;
        }

        public String getName()
        {
                return name;
        }

        public String getNick()
        {
                return nick;
        }

        public int getPhoto()
        {
                return photo;
        }
}