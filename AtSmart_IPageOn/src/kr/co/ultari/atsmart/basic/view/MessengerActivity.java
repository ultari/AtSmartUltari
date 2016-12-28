package kr.co.ultari.atsmart.basic.view;

import kr.co.ultari.atsmart.basic.Define;
import android.app.Activity;
import android.os.Bundle;

public class MessengerActivity extends Activity {
        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                Define.nowTopActivity = this;
        }

        @Override
        public void onDestroy()
        {
                super.onDestroy();
                if ( Define.nowTopActivity == this ) Define.nowTopActivity = null;
        }
}
