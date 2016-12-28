package kr.co.ultari.atsmart.basic.subdata;

import android.view.View;

public class NotifyEventTag {
        public View view;
        public int type;
        public NotifyData data;

        public NotifyEventTag( int type, NotifyData data, View view )
        {
                this.type = type;
                this.data = data;
                this.view = view;
        }
}
