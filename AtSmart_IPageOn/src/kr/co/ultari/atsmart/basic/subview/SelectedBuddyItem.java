package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.subdata.SearchResultItemData;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.view.BuddyView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SelectedBuddyItem extends ArrayAdapter<SearchResultItemData> {
        BuddyView parent;

        public SelectedBuddyItem( Context context, BuddyView parent )
        {
                super( context, android.R.layout.simple_list_item_1 );
                this.parent = parent;
        }

        public void addItem( SearchResultItemData item )
        {
                insert( item, 0 );
        }

        @SuppressLint( { "InflateParams", "ViewHolder" } )
        public View getView( int position, View convertView, ViewGroup viewGroup )
        {
                try
                {
                        LayoutInflater inflater = parent.inflater;
                        View row = ( View ) inflater.inflate( R.layout.sub_selected_buddy_item, null );
                        UserImageView img = ( UserImageView ) row.findViewById( R.id.buddyUserIcon );
                        img.setUserId( getItem( position ).id );
                        TextView nameLabel = ( TextView ) row.findViewById( R.id.buddyselectedUserName );
                        nameLabel.setText( StringUtil.getNamePosition( getItem( position ).name ) );
                        return row;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
        }
        private static final String TAG = "/AtSmart/SelectedBuddyItem";

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
