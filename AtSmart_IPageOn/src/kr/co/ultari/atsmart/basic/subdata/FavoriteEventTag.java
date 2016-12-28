package kr.co.ultari.atsmart.basic.subdata;

import android.view.View;

public class FavoriteEventTag {
        public View view;
        public int type;
        public FavoriteData data;

        public FavoriteEventTag( int type, FavoriteData data, View view )
        {
                this.type = type;
                this.data = data;
                this.view = view;
        }
}
