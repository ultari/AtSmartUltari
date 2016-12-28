package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.subdata.NotifyData;
import kr.co.ultari.atsmart.basic.subdata.NotifyEventTag;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.view.NotifyView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

@SuppressLint( "DefaultLocale" )
public class NotifyItem2 extends ArrayAdapter<NotifyData> implements OnClickListener {
        private NotifyView parent;
        private Context context;
        private TextView title, content;
        private ImageButton btnCheck;

        public NotifyItem2( Context context, NotifyView parent )
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
                        View row = ( View ) inflater.inflate( R.layout.sub_notify_list2, null );
                        NotifyData data = ( NotifyData ) getItem( position );
                        TableLayout back = ( TableLayout ) row.findViewById( R.id.notifyView2 );
                        title = ( TextView ) row.findViewById( R.id.custom_title2 );
                        if ( data.senderName.indexOf( "\n" ) < 0 )
                        {
                                if ( data.content.equals( "" ) ) title.setText( Html.fromHtml( context.getString( R.string.untitled ) ) );
                                else title.setText( Html.fromHtml( data.content ) );
                        }
                        else
                        {
                                if ( data.title.equals( "" ) ) title.setText( Html.fromHtml( context.getString( R.string.untitled ) ) );
                                else title.setText( Html.fromHtml( data.title ) );
                        }
                        btnCheck = ( ImageButton ) row.findViewById( R.id.notify_checkicon );
                        btnCheck.setOnClickListener( this );
                        Linkify.addLinks( title, Linkify.ALL );
                        LayoutParams layoutParam = ( LayoutParams ) title.getLayoutParams();
                        layoutParam.width = ( int ) (StringUtil.getWidth( context ) - 100);
                        title.setLayoutParams( layoutParam );
                        if ( data.read ) title.setTextColor( 0xFF878787 );
                        else title.setTextColor( 0xFF406FA8 );
                        content = ( TextView ) row.findViewById( R.id.content2 );
                        layoutParam = ( LayoutParams ) content.getLayoutParams();
                        layoutParam.width = ( int ) (StringUtil.getWidth( context ) - 100);
                        content.setLayoutParams( layoutParam );
                        if ( data.senderName.indexOf( "\n" ) < 0 ) content.setText( Html.fromHtml( data.title.replace( "\n", "<br />" ) ) );
                        else content.setText( Html.fromHtml( data.content.replace( "\n", "<br />" ) ) );
                        Linkify.addLinks( content, Linkify.ALL );
                        if ( data.read ) content.setTextColor( 0xFF878787 );
                        else content.setTextColor( 0xFF000000 );
                        title.setOnClickListener( this );
                        title.setTag( new NotifyEventTag( Define.NOTIFY_CHAT, data, row ) );
                        content.setOnClickListener( this );
                        content.setTag( new NotifyEventTag( Define.NOTIFY_CHAT, data, row ) );
                        /*
                         * if ( data.read )
                         * back.setBackgroundColor(0xFFFFFDE9);
                         * else
                         * back.setBackgroundColor(0xFFFFF9C2);
                         * ImageView icon = (ImageView)row.findViewById(R.id.notify_checkicon);
                         * if ( data.title.toLowerCase().indexOf("mail") >= 0 || data.title.indexOf(context.getString(R.string.mail)) >= 0 )
                         * {
                         * if ( data.read )
                         * icon.setImageResource(R.drawable.notice_icon_mail);
                         * else
                         * icon.setImageResource(R.drawable.notice_icon_mail_read);
                         * }
                         * else
                         * {
                         * if ( data.read )
                         * icon.setImageResource(R.drawable.notice_icon_doc);
                         * else
                         * icon.setImageResource(R.drawable.notice_icon_doc_read);
                         * }
                         */
                        /*
                         * TextView title = (TextView)row.findViewById(R.id.custom_title2);
                         * if ( data.senderName.indexOf( "\n" ) < 0 )
                         * title.setText(Html.fromHtml(data.content));
                         * else
                         * title.setText(Html.fromHtml(data.title));
                         * Linkify.addLinks(title, Linkify.ALL);
                         * LayoutParams layoutParam = (LayoutParams)title.getLayoutParams();
                         * layoutParam.width = (int)(StringUtil.getWidth(context) - 100);
                         * title.setLayoutParams(layoutParam);
                         * if ( data.read )
                         * title.setTextColor(0xFF878787);
                         * else
                         * title.setTextColor(0xFF406FA8);
                         * TextView sender = (TextView)row.findViewById(R.id.sender);
                         * if(data.senderName.indexOf("\n") == -1)
                         * sender.setText(context.getString(R.string.sendPerson) + data.senderName);
                         * else
                         * {
                         * String senderName =data.senderName.substring(data.senderName.indexOf("\n")+1,data.senderName.lastIndexOf("\n"));
                         * sender.setText(context.getString(R.string.sendPerson) + senderName);
                         * }
                         * if ( data.read )
                         * sender.setTextColor(0xFF878787);
                         * else
                         * sender.setTextColor(0xFF000000);
                         * TextView receiveDateTime = (TextView)row.findViewById(R.id.receiveDateTime);
                         * receiveDateTime.setText(StringUtil.getCommaString(context.getString(R.string.receiveDate) + StringUtil.getNotifyTime(data.recvDate),
                         * StringUtil.getWidth(context) * 1 / 2, receiveDateTime.getPaint()));
                         * if ( data.read )
                         * receiveDateTime.setTextColor(0xFF878787);
                         * else
                         * receiveDateTime.setTextColor(0xFF000000);
                         * TextView content = (TextView)row.findViewById(R.id.content2);
                         * layoutParam = (LayoutParams)content.getLayoutParams();
                         * layoutParam.width = (int)(StringUtil.getWidth(context) - 100);
                         * content.setLayoutParams(layoutParam);
                         * if ( data.senderName.indexOf( "\n" ) < 0 )
                         * content.setText(Html.fromHtml(data.title.replace( "\n", "<br />" )));
                         * else
                         * content.setText(Html.fromHtml(data.content.replace( "\n", "<br />" )));
                         * Linkify.addLinks(content, Linkify.ALL);
                         * if ( data.read )
                         * content.setTextColor(0xFF878787);
                         * else
                         * content.setTextColor(0xFF000000);
                         * Button chatBtn = (Button) row.findViewById(R.id.notifyChatIcon);
                         * chatBtn.setTag(new NotifyEventTag(Define.NOTIFY_CHAT, data, row));
                         * chatBtn.setOnClickListener(this);
                         * Button noteBtn = ( Button ) row.findViewById( R.id.notifyNoteIcon );
                         * noteBtn.setTag( new NotifyEventTag( Define.NOTIFY_NOTE, data, row ) );
                         * noteBtn.setOnClickListener( this );
                         * if(Define.useSendNoteMsg)
                         * noteBtn.setVisibility( View.VISIBLE );
                         * else
                         * noteBtn.setVisibility( View.GONE );
                         * Button okBtn = (Button)row.findViewById(R.id.notifyConfirmIcon);
                         * okBtn.setOnClickListener(this);
                         * Button delBtn = (Button)row.findViewById(R.id.notifyDeleteIcon);
                         * okBtn.setTag(new NotifyEventTag(Define.NOTIFY_READ, data, row));
                         * delBtn.setTag(new NotifyEventTag(Define.NOTIFY_DEL, data, row));
                         * delBtn.setOnClickListener(this);
                         */
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
                NotifyEventTag tag = ( NotifyEventTag ) view.getTag();
                if ( tag.type == Define.NOTIFY_CHAT )
                {
                        Intent i = new Intent( context, kr.co.ultari.atsmart.basic.subview.NotifyDialog.class );
                        i.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                        context.startActivity( i );
                }
                /*
                 * NotifyEventTag tag = (NotifyEventTag)view.getTag();
                 * if ( tag.type == Define.NOTIFY_DEL )
                 * {
                 * try
                 * {
                 * this.remove(tag.data);
                 * Database.instance(context).deleteAlarm(tag.data.msgId);
                 * parent.recalcUnreadCount();
                 * }
                 * catch(Exception e)
                 * {
                 * EXCEPTION( e );
                 * }
                 * }
                 * else if ( tag.type == Define.NOTIFY_READ )
                 * {
                 * try
                 * {
                 * tag.data.read = true;
                 * Database.instance(context).updateAlarm(tag.data.msgId, "cRead", "Y");
                 * this.notifyDataSetChanged();
                 * parent.recalcUnreadCount();
                 * }
                 * catch(Exception e)
                 * {
                 * EXCEPTION( e );
                 * }
                 * }
                 * else if ( tag.type == Define.NOTIFY_CHAT )
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
                 * String oUserIds = nowSelectedUserId + "," + Define.getMyId(context);
                 * String userIds = StringUtil.arrange(oUserIds);
                 * String userNames = nowSelectedUserName + "," + StringUtil.getNamePosition(Define.getMyName());
                 * userNames = StringUtil.arrangeNamesByIds(userNames, oUserIds);
                 * String roomId = userIds.replace( ",", "_" );
                 * ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfo( roomId );
                 * if ( array.size() == 0 )
                 * Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames, StringUtil.getNowDateTime(), context.getString( R.string.newRoom
                 * ) );
                 * ActionManager.openChat(context, roomId, userIds, userNames);
                 * }
                 * catch(Exception e)
                 * {
                 * EXCEPTION( e );
                 * }
                 * }
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
        private static final String TAG = "/AtSmart/NotifyItem2";
}
