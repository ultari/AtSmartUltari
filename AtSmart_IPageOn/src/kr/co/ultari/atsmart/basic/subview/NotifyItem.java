package kr.co.ultari.atsmart.basic.subview;

import java.util.ArrayList;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.NotifyData;
import kr.co.ultari.atsmart.basic.subdata.NotifyEventTag;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.view.NotifyView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint( "DefaultLocale" )
public class NotifyItem extends ArrayAdapter<NotifyData> implements OnClickListener {
        NotifyView parent;
        Context context;

        public NotifyItem( Context context, NotifyView parent )
        {
                super( context, android.R.layout.simple_list_item_1 );
                this.parent = parent;
                this.context = context;
        }

        @SuppressLint( { "InflateParams", "ViewHolder" } )
        public View getView( int position, View convertView, ViewGroup viewGroup )
        {
                try
                {
                        LayoutInflater inflater = parent.inflater;
                        View row = ( View ) inflater.inflate( R.layout.sub_notify_list, null );
                        NotifyData data = ( NotifyData ) getItem( position );
                        LinearLayout back = ( LinearLayout ) row.findViewById( R.id.notifyView );
                        //back.setBackgroundColor( 0xFF2b2b2b );
                        
                        ImageView icon = ( ImageView ) row.findViewById( R.id.notifyicon );
                        if ( data.read ) icon.setBackgroundResource( R.drawable.notice_read );
                        else icon.setBackgroundResource( R.drawable.notice_unread );
                        
                        TextView title = ( TextView ) row.findViewById( R.id.custom_title );
                        title.setTypeface( Define.tfRegular );
                        title.setText( Html.fromHtml( data.title ) );
                        
                        // Linkify.addLinks(title, Linkify.ALL);
                        // LayoutParams layoutParam = (LayoutParams)title.getLayoutParams();
                        // layoutParam.width = (int)(StringUtil.getWidth(context) - 100);
                        // title.setLayoutParams(layoutParam);
                        // title.setText(StringUtil.getCommaString(data.title, ( StringUtil.getWidth() * 1 / 2 ) - 30, title.getPaint()));
                        //if ( data.read ) title.setTextColor( 0xFF6F6F6F );
                        //else title.setTextColor( 0xFFDCDCDC );
                        TextView sender = ( TextView ) row.findViewById( R.id.sender );
                        sender.setTypeface( Define.tfRegular );
                        if ( data.senderName.indexOf( "\n" ) == -1 ) sender.setText( context.getString( R.string.sendPerson ) + data.senderName );
                        else
                        {
                                String senderName = data.senderName.substring( data.senderName.indexOf( "\n" ) + 1, data.senderName.lastIndexOf( "\n" ) );
                                sender.setText( context.getString( R.string.sendPerson ) + senderName );
                        }
                        //if ( data.read ) sender.setTextColor( 0xFF6F6F6F );
                        //else sender.setTextColor( 0xFF969696 );
                        TextView receiveDateTime = ( TextView ) row.findViewById( R.id.receiveDateTime );
                        receiveDateTime.setTypeface( Define.tfRegular );
                        receiveDateTime.setText( StringUtil.getCommaString( StringUtil.getNotifyTime( data.recvDate ), StringUtil.getWidth( context ) * 1 / 2, receiveDateTime.getPaint() ) );
                        //if ( data.read ) receiveDateTime.setTextColor( 0xFF6F6F6F );
                        //else receiveDateTime.setTextColor( 0xFF8D9DD8 );
                        TextView content = ( TextView ) row.findViewById( R.id.content );
                        // layoutParam = (LayoutParams)content.getLayoutParams();
                        // layoutParam.width = (int)(StringUtil.getWidth(context) - 100);
                        // content.setLayoutParams(layoutParam);
                        content.setTypeface( Define.tfRegular );
                        content.setText( Html.fromHtml( data.content.replace( "\n", "<br />" ) ) );
                        // Linkify.addLinks(content, Linkify.ALL);
                        //if ( data.read ) content.setTextColor( 0xFF6F6F6F );
                        //else content.setTextColor( 0xFFDCDCDC );
                        Button chatBtn = ( Button ) row.findViewById( R.id.notifyChatIcon );
                        chatBtn.setTag( new NotifyEventTag( Define.NOTIFY_CHAT, data, row ) );
                        chatBtn.setOnClickListener( this );
                        
                        Button okBtn = ( Button ) row.findViewById( R.id.notifyConfirmIcon );
                        okBtn.setTypeface( Define.tfRegular );
                        if ( data.read )
                        {
                                okBtn.setTag( new NotifyEventTag( Define.NOTIFY_DEL, data, row ) );
                                okBtn.setText( context.getString( R.string.delete ) );
                                //okBtn.setTextColor( 0xFF6F6F6F );
                        }
                        else
                        {
                                okBtn.setTag( new NotifyEventTag( Define.NOTIFY_READ, data, row ) );
                                okBtn.setText( context.getString( R.string.ok ) );
                                //okBtn.setTextColor( 0xFF878686 );
                        }
                        okBtn.setOnClickListener( this );
                        // Button delBtn = (Button)row.findViewById(R.id.notifyDeleteIcon);
                        // delBtn.setTag(new NotifyEventTag(Define.NOTIFY_DEL, data, row));
                        // delBtn.setOnClickListener(this);
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
                NotifyEventTag tag = ( NotifyEventTag ) view.getTag();
                if ( tag.type == Define.NOTIFY_DEL )
                {
                        try
                        {
                                this.remove( tag.data );
                                Database.instance( context ).deleteAlarm( tag.data.msgId );
                                parent.recalcUnreadCount();
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
                else if ( tag.type == Define.NOTIFY_READ )
                {
                        try
                        {
                                tag.data.read = true;
                                Database.instance( context ).updateAlarm( tag.data.msgId, "cRead", "Y" );
                                this.notifyDataSetChanged();
                                parent.recalcUnreadCount();
                                
                                ImageView icon = ( ImageView ) tag.view.findViewById( R.id.notifyicon );
                                if(icon != null) icon.setBackgroundResource( R.drawable.notice_read );
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
                else if ( tag.type == Define.NOTIFY_CHAT )
                {
                        try
                        {
                                tag.data.read = true;
                                Database.instance( context ).updateAlarm( tag.data.msgId, "cRead", "Y" );
                                this.notifyDataSetChanged();
                                parent.recalcUnreadCount();
                                if ( tag.data.senderName.indexOf( "\n" ) < 0 )
                                {
                                        ActionManager.alert( parent.getActivity(), context.getString( R.string.noChatUser ) );
                                        return;
                                }
                                String nowSelectedUserId = tag.data.senderName.substring( 0, tag.data.senderName.indexOf( "\n" ) );
                                String nowSelectedUserName = tag.data.senderName.substring( tag.data.senderName.indexOf( "\n" ) + 1,
                                                tag.data.senderName.lastIndexOf( "\n" ) );
                                String oUserIds = nowSelectedUserId + "," + Define.getMyId( context );
                                String userIds = StringUtil.arrange( oUserIds );
                                String userNames = nowSelectedUserName + "," + StringUtil.getNamePosition( Define.getMyName() );
                                userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                                String roomId = userIds.replace( ",", "_" );
                                ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfo( roomId );
                                if ( array.size() == 0 ) Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames,
                                                StringUtil.getNowDateTime(), context.getString( R.string.newRoom ) );
                                ActionManager.openChat( context, roomId, userIds, userNames );
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
                /*
                 * else if ( tag.type == Define.NOTIFY_NOTE )
                 * {
                 * try
                 * {
                 * tag.data.read = true;
                 * Database.instance(context).updateAlarm(tag.data.msgId, "cRead", "Y");
                 * this.notifyDataSetChanged();
                 * parent.recalcUnreadCount();
                 * if ( tag.data.senderName.indexOf( "\n" ) < 0 )
                 * {
                 * ActionManager.alert( parent.getActivity(), context.getString(R.string.noChatUser) );
                 * return;
                 * }
                 * String nowSelectedUserId = tag.data.senderName.substring(0, tag.data.senderName.indexOf("\n"));
                 * String nowSelectedUserName = tag.data.senderName.substring(tag.data.senderName.indexOf("\n")+1,tag.data.senderName.lastIndexOf("\n"));
                 * Intent i = new Intent(context, kr.co.ultari.atsmart.basic.subview.SendNote.class);
                 * i.putExtra( "USERID", nowSelectedUserId );
                 * i.putExtra( "USERNAME", nowSelectedUserName );
                 * i.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                 * context.startActivity(i);
                 * }
                 * catch(Exception e)
                 * {
                 * EXCEPTION( e );
                 * }
                 * }
                 */
        }
        private static final String TAG = "/AtSmart/NotifyItem";

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
