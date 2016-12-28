package kr.co.ultari.atsmart.basic.subview;

import java.util.List;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class m_Adapter extends ArrayAdapter<GroupUser> {
        Context context;
        List<GroupUser> list;

        public m_Adapter( Context context, int textViewResourceId, List<GroupUser> list )
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
                                view = inflater.inflate( R.layout.group_user_item, parent, false );
                        }
                        GroupUser f = getItem( position );
                        TextView tv = ( TextView ) view.findViewById( R.id.tv_username );
                        ImageView iv = ( ImageView ) view.findViewById( R.id.iv_usericon );
                        TextView tvNick = ( TextView ) view.findViewById( R.id.tv_nickname );
                        UserImageView iv_photoid = null;
                        iv_photoid = ( UserImageView ) view.findViewById( R.id.group_userphoto );
                        iv_photoid.setUserId( f.getId() );
                        tv.setText( f.getName() );
                        tv.setTypeface( Define.tfRegular );
                        iv.setBackgroundResource( f.getPhoto() );
                        tvNick.setText( f.getNick() );
                        tvNick.setTypeface( Define.tfRegular );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return view;
        }

        public void refresh()
        {
                setNotifyOnChange( true );
                notifyDataSetChanged();
        }
        private static final String TAG = "/AtSmart/Adapter";

        public void TRACE( String s )
        {
                if ( !Define.useTrace ) return;
                android.util.Log.i( TAG, s );
        }

        public void EXCEPTION( Throwable e )
        {
                android.util.Log.e( TAG, e.getMessage(), e );
                if ( Define.saveErrorLog )
                {
                        java.io.FileWriter fw = null;
                        java.io.PrintWriter pw = null;
                        try
                        {
                                fw = new java.io.FileWriter(
                                                android.os.Environment.getExternalStoragePublicDirectory( android.os.Environment.DIRECTORY_DOWNLOADS )
                                                                + java.io.File.separator + "AtSmartErrorLog.txt", true );
                                pw = new java.io.PrintWriter( fw, false );
                                pw.print( "[" + new java.util.Date() + "]" + "\n" );
                                e.printStackTrace( pw );
                                pw.flush();
                        }
                        catch ( Exception ie )
                        {
                                e.printStackTrace();
                        }
                        finally
                        {
                                if ( fw != null )
                                {
                                        try
                                        {
                                                fw.close();
                                                fw = null;
                                        }
                                        catch ( Exception ie )
                                        {}
                                }
                                if ( pw != null )
                                {
                                        try
                                        {
                                                pw.close();
                                                pw = null;
                                        }
                                        catch ( Exception ie )
                                        {}
                                }
                        }
                }
        }
}
