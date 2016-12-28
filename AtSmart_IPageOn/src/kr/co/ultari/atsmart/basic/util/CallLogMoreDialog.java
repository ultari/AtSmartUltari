package kr.co.ultari.atsmart.basic.util;

import java.util.ArrayList;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.subdata.CallLogData;
import kr.co.ultari.atsmart.basic.subdata.CallLogMoreData;
import kr.co.ultari.atsmart.basic.subview.m_CallLogMore_Adapter;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class CallLogMoreDialog extends Dialog {
        private static final String TAG = "AtSmart/CallLogMoreDialog";
        public String msg;
        private Button exitBtn;
        private Context context;
        private ListView listview;
        private ArrayList<CallLogMoreData> list;
        private ArrayList<CallLogData> array;
        private m_CallLogMore_Adapter adapter;
        private TextView tv_title;

        public CallLogMoreDialog( Context context, ArrayList<CallLogData> array, String title )
        {
                super( context );
                this.context = context;
                this.array = array;
                this.msg = title;
        }

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( android.graphics.Color.TRANSPARENT ) );
                getWindow().getAttributes().windowAnimations = R.style.CustomDialogAnimation;
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE ); //2016-12-13
                setContentView( R.layout.calllog_more_dialog );
                tv_title = ( TextView ) findViewById( R.id.calllog_more_title );
                tv_title.setTypeface( Define.tfRegular );
                tv_title.setText( msg );
                listview = ( ListView ) findViewById( R.id.calllog_more_listView );
                list = new ArrayList<CallLogMoreData>();
                adapter = new m_CallLogMore_Adapter( this.context, R.layout.calllog_more_item, list );
                listview.setAdapter( adapter );
                exitBtn = ( Button ) findViewById( R.id.calllog_more_exitBtn );
                exitBtn.setOnClickListener( new View.OnClickListener() {
                        public void onClick( View v )
                        {
                                dismiss();
                        }
                } );
                for ( CallLogData data : array )
                        list.add( new CallLogMoreData( data.getCallDate(), data.getCallDuration(), data.getPhonenum(), data.getCallStatus() ) );
                adapter.refresh();
        }
}