package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

public class NotifyDialog extends MessengerActivity implements OnClickListener {
        private ImageButton btnClose;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
                setContentView( R.layout.notify_detail_popup );
                btnClose = ( ImageButton ) findViewById( R.id.note_close2 );
                btnClose.setOnClickListener( this );
        }

        @Override
        public void onClick( View v )
        {
                if ( v == btnClose ) finish();
        }
}
