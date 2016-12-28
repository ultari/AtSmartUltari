package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.view.ConfigView;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class NetworkInfo extends MessengerActivity implements OnClickListener {
        private Button btnSave, btnCancel;
        private RadioButton rBtnData, rBtnWifi, rBtnDisable;
        private TextView tvTitle;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.config_network );
                tvTitle = ( TextView ) findViewById( R.id.network_title );
                tvTitle.setTypeface( Define.tfBold ); // font
                btnSave = ( Button ) findViewById( R.id.network_save );
                btnSave.setOnClickListener( this );
                btnSave.setTypeface( Define.tfRegular ); // font
                btnCancel = ( Button ) findViewById( R.id.network_cancel );
                btnCancel.setOnClickListener( this );
                btnCancel.setTypeface( Define.tfRegular ); // font
                rBtnData = ( RadioButton ) findViewById( R.id.network_btn_data );
                rBtnData.setOnClickListener( optionOnClickListener );
                rBtnData.setTypeface( Define.tfRegular ); // font
                rBtnWifi = ( RadioButton ) findViewById( R.id.network_btn_wifi );
                rBtnWifi.setOnClickListener( optionOnClickListener );
                rBtnWifi.setTypeface( Define.tfRegular ); // font
                rBtnDisable = ( RadioButton ) findViewById( R.id.network_btn_disable );
                rBtnDisable.setOnClickListener( optionOnClickListener );
                rBtnDisable.setTypeface( Define.tfRegular ); // font
                String mode = Database.instance( getApplicationContext() ).selectConfig( "NETWORKMODE" );
                if ( mode.equals( Integer.toString( Define.NETWORK_MODE_LTE ) ) ) rBtnData.setChecked( true );
                else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_WIFI ) ) ) rBtnWifi.setChecked( true );
                else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_DISABLE ) ) || mode.equals( "" ) ) rBtnDisable.setChecked( true );
        }
        RadioButton.OnClickListener optionOnClickListener = new RadioButton.OnClickListener() {
                public void onClick( View v )
                {
                }
        };

        @Override
        public void onClick( View v )
        {
                if ( v == btnSave )
                {
                        if ( rBtnData.isChecked() )
                        {
                                Database.instance( getApplicationContext() ).updateConfig( "NETWORKMODE", Integer.toString( Define.NETWORK_MODE_LTE ) );
                        }
                        else if ( rBtnWifi.isChecked() )
                        {
                                Database.instance( getApplicationContext() ).updateConfig( "NETWORKMODE", Integer.toString( Define.NETWORK_MODE_WIFI ) );
                        }
                        else if ( rBtnDisable.isChecked() )
                        {
                                Database.instance( getApplicationContext() ).updateConfig( "NETWORKMODE", Integer.toString( Define.NETWORK_MODE_DISABLE ) );
                        }
                        ConfigView.instance().resetData();
                        finish();
                }
                else if ( v == btnCancel )
                {
                        finish();
                }
        }
}
