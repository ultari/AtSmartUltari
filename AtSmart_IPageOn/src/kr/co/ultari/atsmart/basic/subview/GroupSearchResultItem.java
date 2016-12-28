package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.subdata.SearchResultItemData;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.view.GroupSearchView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GroupSearchResultItem extends ArrayAdapter<SearchResultItemData> implements OnClickListener {
        GroupSearchView parent;
        Context context;
        short type;

        public GroupSearchResultItem( Context context, GroupSearchView parent, short type )
        {
                super( context, android.R.layout.simple_list_item_1 );
                this.context = context;
                this.parent = parent;
                this.type = type;
        }

        public void addItem( SearchResultItemData item )
        {
                insert( item, 0 );
        }

        public void setCheck( String id, boolean checked )
        {
                for ( int i = 0; i < getCount(); i++ )
                {
                        if ( getItem( i ).id.equals( id ) )
                        {
                                getItem( i ).checked = checked;
                        }
                }
        }

        @SuppressLint( { "InflateParams", "ViewHolder" } )
        public View getView( int position, View convertView, ViewGroup viewGroup )
        {
                try
                {
                        LayoutInflater inflater = parent.inflater;
                        View row = ( View ) inflater.inflate( R.layout.sub_search_result_item, null );
                        int i = getItem( position ).icon;
                        UserImageView img = ( UserImageView ) row.findViewById( R.id.searchResultUser );
                        img.setUserId( getItem( position ).id );
                        TextView nameLabel = ( TextView ) row.findViewById( R.id.searchResultName );
                        nameLabel.setTypeface( Define.tfRegular );
                        String nick = StringUtil.getNickName( getItem( position ).nickName, i );
                        Log.d( "GroupSearch1", nick + ":" + getItem( position ).name );
                        switch ( Define.SET_COMPANY )
                        {
                        case Define.SAEHA :
                                nameLabel.setText( StringUtil.getNamePosition( getItem( position ).name ) );
                                break;
                        default :
                                nameLabel.setText( StringUtil.getNamePosition( getItem( position ).name ) );
                                // nameLabel.setText(StringUtil.getNamePosition(getItem(position).name) + "  (" +
                                // StringUtil.getNickName(getItem(position).nickName + ")", i));
                                break;
                        }
                        ImageView mobile = ( ImageView ) row.findViewById( R.id.searchMobileStatus );
                        if ( Define.searchMobileOn.get( (getItem( position ).id) ).equals( "0" ) ) 
                                mobile.setImageBitmap( parent.mobileOff );
                        else 
                                mobile.setImageBitmap( parent.mobileOn );
                        
                        ImageView icon = ( ImageView ) row.findViewById( R.id.searchResultUserIcon );
                        if ( Define.usePcState )
                        {
                                if ( i == 0 ) icon.setImageBitmap( parent.statusOfflineBitmap );
                                else if ( i == 1 ) icon.setImageBitmap( parent.statusOnlineBitmap );
                                else if ( i == 2 ) icon.setImageBitmap( parent.statusAwayBitmap );
                                else if ( i == 3 ) icon.setImageBitmap( parent.statusMeetingBitmap );
                                else if ( i == 4 ) icon.setImageBitmap( parent.statusBusyBitmap );
                                else if ( i == 5 ) icon.setImageBitmap( parent.statusPhoneBitmap );
                                icon.setVisibility( View.VISIBLE );
                        }
                        else 
                                icon.setVisibility( View.GONE );
                        
                        ImageView uc = ( ImageView ) row.findViewById( R.id.searchUcStatus );
                        if ( Define.usePhoneState )
                        {
                                uc.setVisibility( View.VISIBLE );
                                
                                String value = Define.searchUcOn.get( (getItem( position ).id) );
                                if ( value != null && value.trim().equals( Define.UC_IDLE ) ) uc.setImageBitmap( parent.ucOn );
                                else if ( value != null && value.trim().equals( Define.UC_RINGING ) || value.trim().equals( Define.UC_LINE_ENGAGED ) ) uc
                                                .setImageBitmap( parent.ucRinging );
                                else uc.setImageBitmap( parent.ucOff );
                        }
                        else 
                                uc.setVisibility( View.GONE );
                        
                        TextView nickNameLabel = ( TextView ) row.findViewById( R.id.searchResultNickName );
                        nickNameLabel.setTypeface( Define.tfRegular );
                        String[] ar = getItem( position ).name.split( "#" );
                        String part = ar[2];
                        if ( nick.equals( "" ) ) nickNameLabel.setText( part );
                        else nickNameLabel.setText( part + "(" + StringUtil.getNickName( getItem( position ).nickName + ")", i ) );
                        
                        ImageView check = ( ImageView ) row.findViewById( R.id.checkBoxSearchItem );
                        if ( type == GroupSearchView.TYPE_ORGANIZATION )
                                check.setVisibility( View.INVISIBLE );
                        else
                        {
                                if ( getItem( position ).checked ) 
                                        check.setBackgroundResource( R.drawable.radio_sel_whitebg_s );
                                else 
                                        check.setBackgroundResource( R.drawable.radio_nor_whitebg_s );
                                
                                check.setTag( getItem( position ) );
                                check.setOnClickListener( this );
                        }
                        
                        String[] s = new String[2];
                        s[0] = getItem( position ).id;
                        s[1] = getItem( position ).name;
                        row.setTag( s );
                        return row;
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                        return null;
                }
        }

        public void onClick( View view )
        {
                SearchResultItemData data = ( SearchResultItemData ) view.getTag();
                try
                {
                        if ( data != null )
                        {
                                data.checked = !data.checked;
                                if ( data.checked )
                                {
                                        if ( !parent.m_SelectedUserList.isExist( data.id ) ) parent.m_SelectedUserList.addUser( data.id,
                                                        StringUtil.getNamePosition( data.name ) );
                                }
                                else
                                {
                                        parent.m_SelectedUserList.removeUser( data.id );
                                }
                                Message m = parent.searchHandler.obtainMessage( Define.AM_SELECT_CHANGED, null );
                                parent.searchHandler.sendMessage( m );
                                notifyDataSetChanged();
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }
}
