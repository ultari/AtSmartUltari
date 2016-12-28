package kr.co.ultari.atsmart.basic.subdata;

import android.view.View;

public class ContactEventTag {
        public View view;
        public int type;
        public Contact data;

        public ContactEventTag( int type, Contact data, View view )
        {
                this.type = type;
                this.data = data;
                this.view = view;
        }
}
