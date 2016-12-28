package kr.co.ultari.atsmart.basic.util;

import kr.co.ultari.atsmart.basic.R;
import android.app.Activity;
import android.widget.Toast;

public class BackPressCloseHandler {
        private long backKeyPressedTime = 0;
        private Toast toast;
        private Activity activity;

        public BackPressCloseHandler( Activity context )
        {
                this.activity = context;
        }

        public void onBackPressed()
        {
                if ( System.currentTimeMillis() > backKeyPressedTime + 2000 )
                {
                        backKeyPressedTime = System.currentTimeMillis();
                        showGuide();
                        return;
                }
                if ( System.currentTimeMillis() <= backKeyPressedTime + 2000 )
                {
                        activity.finish();
                        toast.cancel();
                }
        }

        public void showGuide()
        {
                // toast = Toast.makeText(activity,"\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
                toast = Toast.makeText( activity, activity.getString( R.string.back_press_msg ), Toast.LENGTH_SHORT );
                toast.show();
        }
}
