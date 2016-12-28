package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.subdata.BuddyAddUserResultItemData;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

//2015-03-01 myFolder edit
public class BuddyAddUserResultItem extends ArrayAdapter<BuddyAddUserResultItemData> implements OnClickListener {
        BuddyAddUser parent;
        Context context;

        public BuddyAddUserResultItem( Context context, BuddyAddUser parent )
        {
                super( context, android.R.layout.simple_list_item_1 );
                this.context = context;
                this.parent = parent;
        }

        public void addItem( BuddyAddUserResultItemData item )
        {
                insert( item, 0 );
        }

        public void setCheck( String id, boolean checked )
        {
                for ( int i = 0; i < getCount(); i++ )
                {
                        if ( getItem( i ).id.equals( id ) ) getItem( i ).checked = checked;
                }
                notifyDataSetChanged();
        }

        @SuppressLint( { "InflateParams", "ViewHolder" } )
        public View getView( int position, View convertView, ViewGroup viewGroup )
        {
                try
                {
                        LayoutInflater inflater = parent.inflater;
                        View row = ( View ) inflater.inflate( R.layout.sub_search_result_item, null );
                        int i = getItem( position ).icon;
                        TextView nameLabel = ( TextView ) row.findViewById( R.id.searchResultName );
                        String nick = StringUtil.getNickName( getItem( position ).nickName, i );
                        if ( nick.equals( "" ) ) nameLabel.setText( StringUtil.getNamePosition( getItem( position ).name ) + "   "
                                        + StringUtil.getNickName( getItem( position ).nickName, i ) );
                        else nameLabel.setText( StringUtil.getNamePosition( getItem( position ).name ) + "  ("
                                        + StringUtil.getNickName( getItem( position ).nickName + ")", i ) );
                        nameLabel.setTextSize( 14 );
                        ImageView icon = ( ImageView ) row.findViewById( R.id.searchResultUserIcon );
                        ImageView mobile = ( ImageView ) row.findViewById( R.id.searchMobileStatus );
                        if ( Define.searchMobileOn.get( (getItem( position ).id) ).equals( "0" ) )
                        {
                                mobile.setBackgroundResource( R.drawable.icon_mobile_off );
                                // mobile.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                                mobile.setBackgroundResource( R.drawable.icon_mobile_on );
                                // mobile.setVisibility(View.VISIBLE);
                        }
                        if ( i == 0 ) icon.setBackgroundResource( R.drawable.icon_pc_off );
                        else icon.setBackgroundResource( R.drawable.icon_pc_on );
                        /*
                         * else if ( i == 1 ) icon.setImageBitmap(parent.statusOnlineBitmap);
                         * else if ( i == 2 ) icon.setImageBitmap(parent.statusAwayBitmap);
                         * else if ( i == 3 ) icon.setImageBitmap(parent.statusMeetingBitmap);
                         * else if ( i == 4 ) icon.setImageBitmap(parent.statusBusyBitmap);
                         * else if ( i == 5 ) icon.setImageBitmap(parent.statusPhoneBitmap);
                         */
                        TextView nickNameLabel = ( TextView ) row.findViewById( R.id.searchResultNickName );
                        nickNameLabel.setText( StringUtil.getStatus( context, i ) );
                        nickNameLabel.setTextSize( 12 );
                        ImageView check = ( ImageView ) row.findViewById( R.id.checkBoxSearchItem );
                        if ( getItem( position ).checked ) check.setBackgroundResource( R.drawable.btn_blackbg_checked );
                        else check.setBackgroundResource( R.drawable.btn_blackbg_uncheck );
                        check.setTag( getItem( position ) );
                        check.setOnClickListener( this );
                        String[] s = new String[2];
                        s[0] = getItem( position ).id;
                        s[1] = getItem( position ).name;
                        row.setTag( s );
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
                BuddyAddUserResultItemData data = ( BuddyAddUserResultItemData ) view.getTag();
                try
                {
                        if ( data != null )
                        {
                                data.checked = !data.checked;
                                /*
                                 * if ( data.checked )
                                 * {
                                 * boolean exist = false;
                                 * for ( int i = 0 ; i < SearchView.selected.getCount() ; i++ )
                                 * {
                                 * if ( SearchView.selected.getItem(i).id.equals(data.id) )
                                 * {
                                 * exist = true;
                                 * }
                                 * }
                                 * if ( !exist )
                                 * {
                                 * SearchView.selected.addItem(new SearchResultItemData(data.id, data.high, data.name, data.icon, data.nickName, true));
                                 * }
                                 * }
                                 * else
                                 * {
                                 * for ( int i = ( SearchView.selected.getCount() - 1 ) ; i >= 0 ; i-- )
                                 * {
                                 * if ( SearchView.selected.getItem(i).id.equals(data.id) )
                                 * {
                                 * SearchView.selected.remove(SearchView.selected.getItem(i));
                                 * }
                                 * }
                                 * }
                                 */
                                // Message m = parent.MyFolderHandler.obtainMessage(Define.AM_SELECT_CHANGED, null);
                                // parent.MyFolderHandler.sendMessage(m);
                                notifyDataSetChanged();
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }
        private static final String TAG = "/AtSmart/SearchResultItem";

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
