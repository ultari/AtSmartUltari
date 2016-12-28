package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.view.ConfigView;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class CallTypeInfo extends MessengerActivity implements OnClickListener {
        private Button btnSave, btnCancel;
        private RadioButton rBtnUdp, rBtnTsl;
        private TextView tvTitle;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.config_calltype );
                tvTitle = ( TextView ) findViewById( R.id.call_type_title );
                tvTitle.setTypeface( Define.tfBold ); // font
                btnSave = ( Button ) findViewById( R.id.call_type_save );
                btnSave.setOnClickListener( this );
                btnSave.setTypeface( Define.tfRegular ); // font
                btnCancel = ( Button ) findViewById( R.id.call_type_cancel );
                btnCancel.setOnClickListener( this );
                btnCancel.setTypeface( Define.tfRegular ); // font
                rBtnUdp = ( RadioButton ) findViewById( R.id.call_type_btn_udp );
                rBtnUdp.setOnClickListener( optionOnClickListener );
                rBtnUdp.setTypeface( Define.tfRegular ); // font
                rBtnTsl = ( RadioButton ) findViewById( R.id.call_type_btn_tls );
                rBtnTsl.setOnClickListener( optionOnClickListener );
                rBtnTsl.setTypeface( Define.tfRegular ); // font
                String mode = Database.instance( getApplicationContext() ).selectConfig( "CALLCONNECTTYPE" );
                if ( mode.equals( Integer.toString( Define.CALL_TYPE_MODE_UDP ) ) ) rBtnUdp.setChecked( true );
                else if ( mode.equals( Integer.toString( Define.CALL_TYPE_MODE_TLS ) ) ) rBtnTsl.setChecked( true );
        }
        RadioButton.OnClickListener optionOnClickListener = new RadioButton.OnClickListener() {
                public void onClick( View v )
                {
                }
        };

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == R.id.call_type_save )
                {
                	if ( rBtnUdp.isChecked() )
                    {
                            Database.instance( getApplicationContext() ).updateConfig( "CALLCONNECTTYPE", Integer.toString( Define.CALL_TYPE_MODE_UDP ) );
                    }
                    else if ( rBtnTsl.isChecked() )
                    {
                            Database.instance( getApplicationContext() ).updateConfig( "CALLCONNECTTYPE", Integer.toString( Define.CALL_TYPE_MODE_TLS ) );
                    }
                	callService();
                	finish();
                }
                else if ( v.getId() == R.id.call_type_cancel )
                {
                        finish();
                }
        }
        
        public void callService()
        {
        	Intent i = new Intent(Define.IPG_CALL_ACTION);
            i.putExtra("Action", "RESTART");
            sendBroadcast(i);
        }
}
