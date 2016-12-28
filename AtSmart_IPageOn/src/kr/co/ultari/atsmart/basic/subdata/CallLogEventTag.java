package kr.co.ultari.atsmart.basic.subdata;

import android.view.View;

public class CallLogEventTag {
        public View view;
        public int type;
        public CallLogData data;

        public CallLogEventTag( int type, CallLogData data, View view )
        {
                this.type = type;
                this.data = data;
                this.view = view;
        }
}
