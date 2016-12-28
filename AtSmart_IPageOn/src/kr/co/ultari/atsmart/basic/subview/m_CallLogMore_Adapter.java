package kr.co.ultari.atsmart.basic.subview;

import java.util.List;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.subdata.CallLogMoreData;
import kr.co.ultari.atsmart.basic.util.FmcSendBroadcast;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class m_CallLogMore_Adapter extends ArrayAdapter<CallLogMoreData> {
        private static final String TAG = "/AtSmart/m_CallLogMore_Adapter";
        private Context context;
        private List<CallLogMoreData> list;

        public m_CallLogMore_Adapter( Context context, int textViewResourceId, List<CallLogMoreData> list )
        {
                super( context, textViewResourceId, list );
                this.context = context;
                this.list = list;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent )
        {
                View view = convertView;
                try
                {
                        if ( view == null )
                        {
                                LayoutInflater inflater = ( LayoutInflater ) this.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                                view = inflater.inflate( R.layout.calllog_more_item, parent, false );
                        }
                        final CallLogMoreData data = getItem( position );
                        TextView tv_date = ( TextView ) view.findViewById( R.id.calllog_moreitem_date );
                        TextView tv_duration = ( TextView ) view.findViewById( R.id.calllog_moreitem_duration );
                        ImageView iv_icon = ( ImageView ) view.findViewById( R.id.calllog_moreitem_icon );
                        Button btn_call = ( Button ) view.findViewById( R.id.calllog_moreitem_call );
                        btn_call.setOnClickListener( new OnClickListener() {
                                public void onClick( View v )
                                {
                                        FmcSendBroadcast.FmcSendCall( data.getNumber() ,1, context); 
                                }
                        } );
                        
                        Button btn_fmccall = ( Button ) view.findViewById( R.id.calllog_moreitem_fmccall );
                        btn_fmccall.setOnClickListener( new OnClickListener() {
                                public void onClick( View v )
                                {
                                        FmcSendBroadcast.FmcSendCall( data.getNumber() ,0, context); 
                                }
                        } );
                        
                        tv_date.setTypeface( Define.tfRegular );
                        tv_duration.setTypeface( Define.tfRegular ); 
                        tv_date.setText( data.getDate() );
                        tv_duration.setText( getDateStr( data.getDuration() ) );
                        String status = data.getStatus();
                        if ( status.equals( this.context.getString( R.string.call_in ) ) ) iv_icon.setBackgroundResource( R.drawable.img_incomingcall );
                        else if ( status.equals( this.context.getString( R.string.call_out ) ) ) iv_icon.setBackgroundResource( R.drawable.img_sendcall );
                        else if ( status.equals( this.context.getString( R.string.call_missed ) ) ) iv_icon.setBackgroundResource( R.drawable.img_missedcall );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                return view;
        }

        private String getDateStr( String value )
        {
                String ret = "";
                int totalSec = Integer.parseInt( value );
                int day = totalSec / (60 * 60 * 24);
                int hour = (totalSec - day * 60 * 60 * 24) / (60 * 60);
                int minute = (totalSec - day * 60 * 60 * 24 - hour * 3600) / 60;
                int second = totalSec % 60;
                ret = "(" + hour + "시간 " + minute + "분 " + second + "초)";
                return ret;
        }

        public void refresh()
        {
                setNotifyOnChange( true );
                notifyDataSetChanged();
        }
}
