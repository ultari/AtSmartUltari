package kr.co.ultari.atsmart.basic.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.ChatRoomData;
import kr.co.ultari.atsmart.basic.subview.ChatRoomItem;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

@SuppressLint( "HandlerLeak" )
public class TalkView extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, OnClickListener {
        private static final String TAG = "/AtSmart/TalkView";
        private static TalkView talkViewInstance = null;
        public LayoutInflater inflater;
        ChatRoomItem itemList = null;
        ChatRoomData nowData = null;
        ListView listView;
        boolean isLongClick = false;
        String nowSelectedRoomId = null;
        public Handler talkHandler = null;
        private Button btn_addUser;

        public static TalkView instance()
        {
                if ( talkViewInstance == null ) talkViewInstance = new TalkView();
                return talkViewInstance;
        }

        @SuppressLint( "InflateParams" )
        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                this.inflater = inflater;
                View view = inflater.inflate( R.layout.activity_talk, null );
                try
                {
                        listView = ( ListView ) view.findViewById( R.id.chatRoomList );
                        itemList = new ChatRoomItem( getActivity().getApplicationContext(), this );
                        btn_addUser = ( Button ) view.findViewById( R.id.chatRoomList_adduser );
                        btn_addUser.setOnClickListener( this );
                        listView.setAdapter( itemList );
                        listView.setLongClickable( true );
                        listView.setOnItemClickListener( this );
                        listView.setOnItemLongClickListener( this );
                        registerForContextMenu( listView );
                        resetData();
                        String roomId = getActivity().getIntent().getStringExtra( "RoomId" );
                        if ( roomId != null )
                        {
                                for ( int i = 0; i < itemList.getCount(); i++ )
                                {
                                        if ( itemList.getItem( i ).roomId != null && itemList.getItem( i ).roomId.equals( roomId ) )
                                        {
                                                openTalk( itemList.getItem( i ) );
                                                break;
                                        }
                                }
                        }
                        talkHandler = new Handler() {
                                public void handleMessage( Message msg )
                                {
                                        try
                                        {
                                                if ( msg.what == Define.AM_POPUP_MENU )
                                                {
                                                        getActivity().openContextMenu( listView );
                                                }
                                                else if ( msg.what == Define.AM_CONFIRM_YES )
                                                {
                                                        deleteImage( nowSelectedRoomId );
                                                        String dateTime = StringUtil.getNowDateTime();
                                                        String msgId = Define.getMyId( getActivity().getApplicationContext() ) + "_" + dateTime;
                                                        ArrayList<ArrayList<String>> arar = Database.instance( getActivity().getApplicationContext() )
                                                                        .selectChatRoomInfo( nowSelectedRoomId );
                                                        if ( arar.size() != 1 ) return;
                                                        if ( StringUtil.getChatRoomCount( arar.get( 0 ).get( 2 ) ) > 2 )
                                                        {
                                                                sendChat( msgId, nowSelectedRoomId, arar.get( 0 ).get( 1 ), arar.get( 0 ).get( 2 ),
                                                                                "[ROOM_OUT]" );
                                                                // MainActivity.sendChat(
                                                                // msgId,
                                                                // nowSelectedRoomId,
                                                                // arar.get( 0
                                                                // ).get( 1 ),
                                                                // arar.get( 0
                                                                // ).get(2),
                                                                // "[ROOM_OUT]"
                                                                // );
                                                        }
                                                        Database.instance( getActivity().getApplicationContext() ).deleteChatBysRoomId( nowSelectedRoomId );
                                                        Database.instance( getActivity().getApplicationContext() ).deleteChatRoomById( nowSelectedRoomId );
                                                        resetData();
                                                }
                                                else if ( msg.what == Define.AM_REFRESH )
                                                {
                                                        resetData();
                                                }
                                                else if ( msg.what == Define.AM_REDRAW )
                                                {
                                                        itemList.notifyDataSetChanged();
                                                }
                                                else
                                                {
                                                        super.handleMessage( msg );
                                                }
                                        }
                                        catch ( Exception e )
                                        {
                                                EXCEPTION( e );
                                        }
                                }
                        };
                        talkHandler.sendEmptyMessageDelayed( Define.AM_REDRAW, 1500 );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return view;
        }

        public void sendChat( String msgId, String roomId, String userIds, String userNames, String talk )
        {
                try
                {
                        StringBuffer message = new StringBuffer();
                        message.append( Define.getMyId( MainActivity.context ) );
                        message.append( "\t" );
                        message.append( Define.getMyName() );
                        message.append( "\t" );
                        message.append( Define.getMyNickName() );
                        message.append( "\t" );
                        message.append( userIds );
                        message.append( "\t" );
                        message.append( roomId );
                        message.append( "\n" );
                        message.append( userIds );
                        message.append( "\n" );
                        message.append( userNames );
                        message.append( "\n" );
                        message.append( msgId );
                        message.append( "\t" );
                        message.append( StringUtil.getNowDateTime() );
                        message.append( "\t" );
                        message.append( talk );
                        Intent sendIntent = new Intent( Define.MSG_NEW_CHAT );
                        sendIntent.putExtra( "MESSAGE", message.toString() );
                        sendIntent.putExtra( "MESSAGEID", msgId.toString() );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        getActivity().getApplicationContext().sendBroadcast( sendIntent );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        private void deleteImage( String roomId )
        {
                try
                {
                        ArrayList<ArrayList<String>> chatArr = Database.instance( getActivity().getApplicationContext() ).selectChatContent( roomId );
                        for ( int i = 0; i < chatArr.size(); i++ )
                        {
                                ArrayList<String> ar = chatArr.get( i );
                                if ( ar != null )
                                {
                                        String ext = ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) + 1 );
                                        if ( ar.get( 6 ).indexOf( "ATTACH://" ) >= 0
                                                        && (ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" )
                                                                        || ext.equalsIgnoreCase( "png" ) || ext.equalsIgnoreCase( "bmp" )) )
                                        {
                                                // File previewFile = new
                                                // File(getActivity().getFilesDir(),
                                                // "small_" + ar.get( 0 ));
                                                File previewFile = new File( getActivity().getFilesDir(), "small_" + ar.get( 0 )
                                                                + ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) ) );
                                                if ( previewFile.exists() )
                                                {
                                                        previewFile.delete();
                                                        previewFile = null;
                                                }
                                                File originalFile = new File( getActivity().getFilesDir(), ar.get( 0 )
                                                                + ar.get( 6 ).substring( ar.get( 6 ).lastIndexOf( '.' ) ) );
                                                if ( originalFile.exists() )
                                                {
                                                        originalFile.delete();
                                                        originalFile = null;
                                                }
                                        }
                                }
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        @Override
        public void onDestroy()
        {
                TRACE( "onDestroy" );
                talkViewInstance = null;
                super.onDestroy();
        }

        public void resetData()
        {
                if ( itemList == null ) return;
                try
                {
                        itemList.clear();
                        ArrayList<ArrayList<String>> ar = Database.instance( getActivity().getApplicationContext() ).selectChatRoomInfo( null );
                        if ( ar != null )
                        {
                                for ( int i = 0; i < ar.size(); i++ )
                                {
                                        ArrayList<String> result = ar.get( i );
                                        boolean isDuplicationRoom = checkDuplicationRoom( result.get( 0 ) );
                                        if ( !isDuplicationRoom )
                                        {
                                                if ( result.get( 4 ).indexOf( "ATTACH://" ) >= 0 || result.get( 4 ).indexOf( "FILE://" ) >= 0 )
                                                {
                                                        itemList.addItem( new ChatRoomData( result.get( 0 ), result.get( 1 ), result.get( 2 ), result.get( 3 ),
                                                                        StringUtil.getChatTypeString( result.get( 4 ) ), result.get( 5 ) ) );
                                                }
                                                // else if(result.get( 4
                                                // ).indexOf( "/E" ) >= 0)
                                                // itemList.addItem(new
                                                // ChatRoomData(result.get(0),
                                                // result.get(1), result.get(2),
                                                // result.get(3),
                                                // result.get(4).replaceAll(
                                                // "/E", "/" ), result.get(5)));
                                                else itemList.addItem( new ChatRoomData( result.get( 0 ), result.get( 1 ), result.get( 2 ), result.get( 3 ),
                                                                result.get( 4 ), result.get( 5 ) ) );
                                                // Log.d( "TalkView",
                                                // "roomId:"+result.get( 0 ) +
                                                // ", id:"+result.get( 1 ) +
                                                // ", name:"+result.get( 2 ));
                                        }
                                }
                                itemList.notifyDataSetChanged();
                        }
                        else TRACE( "NoChatRoom" );
                        Message m = MainActivity.mainHandler.obtainMessage( Define.AM_NEW_CHAT, null );
                        MainActivity.mainHandler.sendMessage( m );
                        sort();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public boolean checkDuplicationRoom( String roomId )
        {
                try
                {
                        int size = itemList.getCount();
                        for ( int i = 0; i < size; i++ )
                        {
                                if ( itemList.getItem( i ).roomId.trim().equals( roomId.trim() ) ) return true;
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return false;
        }

        @Override
        public void onActivityCreated( Bundle savedInstanceState )
        {
                super.onActivityCreated( savedInstanceState );
        }

        public void sort()
        {
                try
                {
                        for ( int i = 0; i < itemList.getCount(); i++ )
                        {
                                for ( int j = 0; j < i; j++ )
                                {
                                        if ( itemList.getItem( i ).talkDate.compareTo( itemList.getItem( j ).talkDate ) > 0 )
                                        {
                                                ChatRoomData data = itemList.getItem( i );
                                                itemList.remove( data );
                                                itemList.insert( data, j );
                                        }
                                }
                        }
                        itemList.notifyDataSetChanged();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void onItemClick( AdapterView<?> parent, View view, int position, long id )
        {
                if ( isLongClick )
                {
                        isLongClick = false;
                        return;
                }
                ChatRoomData data = itemList.getItem( position );
                openTalk( data );
        }

        public void openTalk( ChatRoomData data )
        {
                try
                {
                        if(data == null) return; //2016-03-31
                        
                        if ( !data.read.equals( "Y" ) )
                        {
                                data.read = "Y";
                                Message m = MainActivity.mainHandler.obtainMessage( Define.AM_NEW_CHAT, null );
                                MainActivity.mainHandler.sendMessage( m );
                        }
                        itemList.notifyDataSetChanged();
                        ActionManager.openChat( getActivity(), data.roomId, data.userIds, data.userNames );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        @Override
        public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo )
        {
                super.onCreateContextMenu( menu, v, menuInfo );
                menu.setHeaderTitle( getString( R.string.choice ) );
                menu.add( 0, Define.MENU_ID_ENTER, Menu.NONE, getString( R.string.roomEnter ) );
                menu.add( 0, Define.MENU_ID_DELETE, Menu.NONE, getString( R.string.delete ) );
        }

        @Override
        public boolean onItemLongClick( AdapterView<?> arg0, View arg1, int arg2, long arg3 )
        {
                isLongClick = true;
                nowSelectedRoomId = itemList.getItem( arg2 ).roomId;
                nowData = itemList.getItem( arg2 );
                Message m = talkHandler.obtainMessage( Define.AM_POPUP_MENU, null );
                talkHandler.sendMessage( m );
                isLongClick = false;
                return true;
        }

        @Override
        public boolean onContextItemSelected( MenuItem item )
        {
                try
                {
                        if ( item.getItemId() == Define.MENU_ID_DELETE )
                                ActionManager.confirm( getActivity(), getString( R.string.delRoom ), getString( R.string.delConfirm ), talkHandler );
                        else if ( item.getItemId() == Define.MENU_ID_ENTER ) 
                                openTalk( nowData );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return super.onContextItemSelected( item );
        }

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

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == R.id.chatRoomList_adduser )
                {
                        Bundle bundle = new Bundle();
                        bundle.putString( "type", "talk" );
                        Intent intent = new Intent( getActivity(), kr.co.ultari.atsmart.basic.view.GroupSearchView.class );
                        intent.putExtras( bundle );
                        startActivity( intent );
                }
        }
}
