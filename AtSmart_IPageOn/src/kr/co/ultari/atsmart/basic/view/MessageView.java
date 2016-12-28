package kr.co.ultari.atsmart.basic.view;

import java.util.ArrayList;
import android.support.v4.app.Fragment;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.util.StringUtil;

public class MessageView extends Fragment implements OnClickListener, OnItemClickListener {
        private static String TAG = "MessageView";
        private static MessageView messageViewInstance = null;
        private Button receiveBoxButton = null;
        private Button sendBoxButton = null;
        private Button deleteAllButton = null;
        private Button write = null;
        private ListView list = null;
        private ArrayList<MessageData> messages = null;
        private TextView unReadComment = null;
        private MessageList listAdapter = null;
        public boolean isReceiveBox = true;

        public static MessageView instance()
        {
                if ( messageViewInstance == null ) messageViewInstance = new MessageView();
                return messageViewInstance;
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                View view = inflater.inflate( R.layout.activity_message_view, null );
                receiveBoxButton = ( Button ) view.findViewById( R.id.receiveBox );
                sendBoxButton = ( Button ) view.findViewById( R.id.sendBox );
                deleteAllButton = ( Button ) view.findViewById( R.id.delete_all );
                write = ( Button ) view.findViewById( R.id.writeMessage );
                receiveBoxButton.setOnClickListener( this );
                sendBoxButton.setOnClickListener( this );
                deleteAllButton.setOnClickListener( this );
                write.setOnClickListener( this );
                messages = new ArrayList<MessageData>();
                unReadComment = ( TextView ) view.findViewById( R.id.message_title );
                unReadComment.setTypeface( Define.tfRegular );
                listAdapter = new MessageList( getActivity(), messages );
                list = ( ListView ) view.findViewById( R.id.message_List );
                list.setAdapter( listAdapter );
                list.setOnItemClickListener( this );
                resetUnreadTitle();
                onSelectBox( true );
                return view;
        }
        public Handler msgBoxHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REFRESH )
                                {
                                        ArrayList<ArrayList<String>> datas = Database.instance( getActivity().getApplicationContext() ).selectMessage(
                                                        isReceiveBox );
                                        if ( datas == null ) return;
                                        messages.clear();
                                        for ( int i = 0; i < datas.size(); i++ )
                                        {
                                                ArrayList<String> data = datas.get( i );
                                                messages.add( new MessageData( data.get( 0 ), data.get( 5 ), data.get( 1 ), data.get( 2 ), data.get( 4 ), data
                                                                .get( 7 ) ) );
                                        }
                                        listAdapter.notifyDataSetChanged();
                                        resetUnreadTitle();
                                }
                                else if ( msg.what == Define.AM_DELETE_ALL )
                                {
                                        if ( Database.instance( getActivity().getApplicationContext() ).deleteAllMessage( !isReceiveBox ) )
                                        {
                                                Message m = msgBoxHandler.obtainMessage( Define.AM_REFRESH );
                                                msgBoxHandler.sendMessage( m );
                                        }
                                }
                                super.handleMessage( msg );
                        }
                        catch ( Exception e )
                        {
                                Log.e( TAG, "Handler", e );
                        }
                }
        };

        private void onSelectBox( boolean isReceiveBox )
        {
                this.isReceiveBox = isReceiveBox;
                if ( isReceiveBox )
                {
                        receiveBoxButton.setBackgroundResource( R.drawable.noti_btn_pressed );
                        sendBoxButton.setBackgroundResource( R.drawable.noti_btn_normal );
                }
                else
                {
                        receiveBoxButton.setBackgroundResource( R.drawable.noti_btn_normal );
                        sendBoxButton.setBackgroundResource( R.drawable.noti_btn_pressed );
                }
                ArrayList<ArrayList<String>> datas = Database.instance( getActivity().getApplicationContext() ).selectMessage( isReceiveBox );
                if ( datas == null ) return;
                messages.clear();
                int unreadCount = 0;
                for ( int i = 0; i < datas.size(); i++ )
                {
                        ArrayList<String> data = datas.get( i );
                        if ( data.get( 7 ).equals( "N" ) ) unreadCount++;
                        messages.add( new MessageData( data.get( 0 ), data.get( 5 ), data.get( 1 ), data.get( 2 ), data.get( 4 ), data.get( 7 ) ) );
                }
                listAdapter.notifyDataSetChanged();
                if ( isReceiveBox )
                {
                        ActionManager.messageTabButton.setNumber( unreadCount );
                        resetUnreadTitle();
                }
        }

        @Override
        public void onClick( View arg0 )
        {
                if ( arg0 == receiveBoxButton )
                {
                        onSelectBox( true );
                }
                else if ( arg0 == sendBoxButton )
                {
                        onSelectBox( false );
                }
                else if ( arg0 == deleteAllButton )
                {
                        if ( messages.size() == 0 ) return;
                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder( getActivity() );
                        alert_confirm.setMessage( "삭제하시겠습니까?" ).setCancelable( false ).setPositiveButton( "확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick( DialogInterface dialog, int which )
                                {
                                        Message m = MessageView.instance().msgBoxHandler.obtainMessage( Define.AM_DELETE_ALL );
                                        MessageView.instance().msgBoxHandler.sendMessage( m );
                                }
                        } ).setNegativeButton( "취소", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick( DialogInterface dialog, int which )
                                {
                                        return;
                                }
                        } );
                        AlertDialog alert = alert_confirm.create();
                        alert.show();
                }
                else if ( arg0 == write )
                {
                        onSendMessage();
                }
        }

        public void onSendMessage()
        {
                Intent intent = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.SendMessageView.class );
                startActivity( intent );
        }

        private void resetUnreadTitle()
        {
                int count = Database.instance( getActivity().getApplicationContext() ).getUnreadMessageCount();
                ActionManager.messageTabButton.setNumber( count );
                if ( count == 0 )
                {
                        unReadComment.setText( "읽지 않은 쪽지가 없습니다." );
                }
                else
                {
                        unReadComment.setText( count + "개의 읽지 않은 쪽지가 있습니다." );
                }
        }
        class MessageData {
                public String msgId;
                public String subject;
                public String senderId;
                public String senderName;
                public String date;
                public String read;

                public MessageData( String msgId, String subject, String senderId, String senderName, String date, String read )
                {
                        this.msgId = msgId;
                        this.subject = subject;
                        this.senderId = senderId;
                        this.senderName = senderName;
                        this.date = date;
                        this.read = read;
                }
        }
        class MessageList extends ArrayAdapter<MessageData> {
                Context context;

                public MessageList( Context context, ArrayList<MessageData> data )
                {
                        super( context, R.layout.message_list, data );
                        this.context = context;
                }

                public View getView( int position, View convertView, ViewGroup viewGroup )
                {
                        View view = convertView;
                        if ( view == null )
                        {
                                LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                                view = inflater.inflate( R.layout.message_list, viewGroup, false );
                        }
                        ;
                        MessageData data = getItem( position );
                        TextView subject = ( TextView ) view.findViewById( R.id.message_subject );
                        subject.setTypeface( Define.tfRegular );
                        TextView sender = ( TextView ) view.findViewById( R.id.message_sender );
                        sender.setTypeface( Define.tfRegular );
                        TextView date = ( TextView ) view.findViewById( R.id.message_date );
                        date.setTypeface( Define.tfRegular );
                        ImageView read = ( ImageView ) view.findViewById( R.id.message_read );
                        if ( data.subject != null && data.subject.indexOf( "√" ) >= 0 )
                        {
                                data.subject = data.subject.substring( 1 );
                        }
                        subject.setText( data.subject );
                        sender.setText( data.senderName );
                        date.setText( StringUtil.getMessageDate( data.date ) );
                        if ( data.read.equals( "Y" ) ) 
                                read.setBackgroundResource( R.drawable.notice_read );
                        else 
                                read.setBackgroundResource( R.drawable.notice_unread );
                        return view;
                }
        }

        @Override
        public void onItemClick( AdapterView<?> arg0, View arg1, int arg2, long arg3 )
        {
                MessageData data = ( MessageData ) arg0.getItemAtPosition( arg2 );
                Intent intent = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.MessageDetailView.class );
                intent.putExtra( "msgId", data.msgId );
                startActivity( intent );
        }
}
