package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class ExLauncher extends Activity implements OnClickListener
{
        private Button btnTalk, btnAccident, btnOverload, btnConference, btnStory, btnClose;
        private TextView tvTalk, tvAccident, tvOverload, tvConference, tvStory;
        
        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                
                setContentView( R.layout.ex_launcher );
                
                btnClose = ( Button ) findViewById( R.id.exlauncher_close );
                btnClose.setOnClickListener( this );
                
                tvTalk = ( TextView ) findViewById( R.id.exlauncher_talk_title );
                tvAccident = ( TextView ) findViewById( R.id.exlauncher_accident_title );
                tvOverload = ( TextView ) findViewById( R.id.exlauncher_overload_title );
                tvConference = ( TextView ) findViewById( R.id.exlauncher_conference_title );
                tvStory = ( TextView ) findViewById( R.id.exlauncher_story_title );
                
                tvTalk.setTypeface( Define.tfRegular );
                tvAccident.setTypeface( Define.tfRegular );
                tvOverload.setTypeface( Define.tfRegular );
                tvConference.setTypeface( Define.tfRegular );
                tvStory.setTypeface( Define.tfRegular );
        }

        @Override
        public void onClick( View v )
        {
                if(v == btnClose)
                {
                        finish();
                }
        }
}
