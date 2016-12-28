package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.subdata.SearchResultItemData;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.view.SearchView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class SelectedUserItem extends ArrayAdapter<SearchResultItemData> implements OnClickListener {
        SearchView parent;

        public SelectedUserItem( Context context, SearchView parent )
        {
                super( context, android.R.layout.simple_list_item_1 );
                this.parent = parent;
        }

        public void addItem( SearchResultItemData item )
        {
                insert( item, 0 );
        }

        @SuppressLint( { "ViewHolder", "InflateParams" } )
        public View getView( int position, View convertView, ViewGroup viewGroup )
        {
                try
                {
                        LayoutInflater inflater = parent.inflater;
                        View row = ( View ) inflater.inflate( R.layout.sub_selected_user_item, null );
                        UserImageView img = ( UserImageView ) row.findViewById( R.id.UserIcon );
                        img.setUserId( getItem( position ).id );
                        TextView nameLabel = ( TextView ) row.findViewById( R.id.selectedUserName );
                        nameLabel.setText( StringUtil.getNamePosition( getItem( position ).name ) );
                        ImageButton btn = ( ImageButton ) row.findViewById( R.id.DeleteSelected );
                        btn.setTag( getItem( position ) );
                        btn.setOnClickListener( this );
                        return row;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
        }

        public void onClick( View view )
        {
                SearchResultItemData data = ( SearchResultItemData ) view.getTag();
                if ( data != null )
                {
                        remove( data );
                        Message m = parent.searchHandler.obtainMessage( Define.AM_SELECT_CHANGED, null );
                        parent.searchHandler.sendMessage( m );
                }
        }
        private static final String TAG = "/AtSmart/SelectedUserItem";

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
