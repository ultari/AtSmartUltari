package kr.co.ultari.atsmart.basic.subview;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.Map.Entry;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.ChatRoomData;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import kr.co.ultari.atsmart.basic.view.TalkView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint( { "InflateParams", "ViewHolder" } )
public class ChatRoomItem extends ArrayAdapter<ChatRoomData> implements Runnable {
        public TalkView parent;
        private Context context;
        private String userId;
        private Bitmap myBitmap = null;
        private Thread thread;
        private UltariSSLSocket sc = null;
        private AmCodec codec;
        private boolean onDestroy = false;
        private String userIds = null;
        private String userNames = null;
        private String roomId = null;
        
        public ChatRoomItem( Context context, TalkView parent )
        {
                super( context, android.R.layout.simple_list_item_1 );
                this.context = context;
                this.parent = parent;
        }

        public void addItem( ChatRoomData item )
        {
                insert( item, 0 );
        }

        public View getView( int position, View convertView, ViewGroup viewGroup )
        {
                try
                {
                        LayoutInflater inflater = parent.inflater;
                        View row = ( View ) inflater.inflate( R.layout.sub_talk_room_list, null );
                        ChatRoomData data = getItem( position );
                        TextView nameLabel = ( TextView ) row.findViewById( R.id.userNames );
                        nameLabel.setTypeface( Define.tfRegular );
                        userIds = data.userIds;
                        userNames = data.userNames;
                        roomId = data.roomId;
                        String showStr = StringUtil.arrange( StringUtil.getChatRoomName( data.userNames, data.userIds ).trim() ); // 2015-06-09
                        if ( StringUtil.getJoinersCount( data.userIds ) > 2 )
                        {
                                nameLabel.setText( StringUtil.getParseJoinersName( context, showStr ) );
                                //nameLabel.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 16 );
                                //nameLabel.setTextColor( Color.rgb( 220, 220, 220 ) );
                        }
                        else
                        {
                                nameLabel.setText( StringUtil.getCommaString( showStr, StringUtil.getWidth( context ) * 1 / 2, nameLabel.getPaint() ) );
                                //nameLabel.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 16 );
                                //nameLabel.setTextColor( Color.rgb( 220, 220, 220 ) );
                        }
                        TextView lastMessageLabel = ( TextView ) row.findViewById( R.id.lastMessage );
                        String viewStr = data.lastMessage;
                        if ( viewStr.indexOf( "FILE://" ) >= 0 ) viewStr = viewStr.substring( viewStr.lastIndexOf( '/' ) + 1 );
                        else if ( viewStr.indexOf( "ATTACH://" ) >= 0 ) viewStr = viewStr.substring( viewStr.lastIndexOf( '/' ) + 1 );
                        viewStr = StringUtil.getNoEnterStr( viewStr );
                        String parse = "";
                        String tmp = "";
                        int pos = 0;
                        while ( pos < viewStr.length() )
                        {
                                if ( viewStr.charAt( pos ) == '/' && (pos + 1) < viewStr.length() && viewStr.charAt( pos + 1 ) == 'E' )
                                {
                                        tmp = viewStr.substring( pos + 2, viewStr.indexOf( "/", pos + 2 ) );
                                        parse += "<img src=\"" + tmp + "\" width=50 height=50>";
                                        pos = viewStr.indexOf( "/", pos + 2 ) + 1;
                                }
                                else
                                {
                                        parse += viewStr.charAt( pos );
                                        pos++;
                                }
                        }
                        Spanned htmlText = Html.fromHtml( parse, imageGetter, null );
                        lastMessageLabel.setTypeface( Define.tfLight );
                        lastMessageLabel.setText( htmlText );
                        //lastMessageLabel.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 15 );
                        if ( data.read.equals( "Y" ) )
                        {
                                lastMessageLabel.setPaintFlags( lastMessageLabel.getPaintFlags() & ~Paint.FAKE_BOLD_TEXT_FLAG );
                                //lastMessageLabel.setTextColor( Color.rgb( 150, 150, 150 ) );
                        }
                        else
                        {
                                lastMessageLabel.setPaintFlags( lastMessageLabel.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG );
                                //lastMessageLabel.setTextColor( Color.rgb( 150, 150, 150 ) );
                        }
                        TextView unreadCount = ( TextView ) row.findViewById( R.id.unreadnumber );
                        unreadCount.setTypeface( Define.tfRegular );
                        int count = Database.instance( context ).selectUnreadChatContentCount( data.roomId );
                        if ( count > 0 )
                        {
                                unreadCount.setBackgroundResource( R.drawable.message_badge_unread );
                                //unreadCount.setTextColor( Color.WHITE );
                                unreadCount.setText( Integer.toString( count ) );
                                unreadCount.setPaintFlags( unreadCount.getPaintFlags() | Paint.FAKE_BOLD_TEXT_FLAG );
                                //unreadCount.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 13 );
                                unreadCount.setGravity( Gravity.CENTER );
                        }
                        else
                        {
                                unreadCount.setBackgroundResource( R.drawable.transparent );
                                unreadCount.setText( "" );
                                unreadCount.setGravity( Gravity.CENTER );
                        }
                        TextView talkDateLabel = ( TextView ) row.findViewById( R.id.talkDate );
                        talkDateLabel.setTypeface( Define.tfRegular );
                        talkDateLabel.setText( StringUtil.getDateStr( data.talkDate ) );
                        // talkDateLabel.setTextColor( Color.rgb( 141, 157, 216 ) );
                        // talkDateLabel.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 12 );
                        UserImageView imgView = ( UserImageView ) row.findViewById( R.id.RoomIcon );
                        String[] userIds = StringUtil.getOtherIds( data.userIds, Define.getMyId( context ) );
                        String roomUserId = StringUtil.makeString( userIds );
                        roomUserId = roomUserId.replaceAll( ",", "&" );
                        imgView.setUserId( "[100:100]" + roomUserId );
                       
                        if ( Define.isResetChatRoomNamePosition )
                        {
                                codec = new AmCodec();
                                thread = new Thread( this );
                                thread.start();
                        }
                        return row;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
        }
        public Handler viewHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REDRAW_IMAGE )
                                {
                                        ImageView imgView = ( ImageView ) msg.obj;
                                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) 
                                                imgView.setBackground( getDrawableFromBitmap( Define.getSmallBitmap( userId ) ) );
                                        else 
                                                imgView.setBackgroundDrawable( getDrawableFromBitmap( Define.getSmallBitmap( userId ) ) );
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

        public Drawable getDrawableFromBitmap( Bitmap bitmap )
        {
                Drawable d = new BitmapDrawable( context.getResources(), bitmap );
                return d;
        }
        private class backgroundTask extends AsyncTask<String, Integer, Bitmap> {
                @Override
                protected Bitmap doInBackground( String... urls )
                {
                        return UltariSocketUtil.getUserImage( urls[0], 190, 190 );
                }

                protected void onPostExecute( Bitmap img )
                {
                        myBitmap = img;
                }
        }

        private Bitmap downloadUserIconMultiple( final String userIds, final int width, final int height )
        {
                String url = "";
                backgroundTask task;
                task = new backgroundTask();
                task.execute( userIds );
                return myBitmap;
        }

        private Bitmap userIconMerge( int size, String[] userIds )
        {
                Bitmap tempIcon = Bitmap.createBitmap( 50, 50, Bitmap.Config.ARGB_8888 );
                Bitmap defaultIcon = BitmapFactory.decodeResource( context.getResources(), R.drawable.img_contract_list );
                Canvas canvas = new Canvas( tempIcon );
                try
                {
                        if ( userIds.length == 2 )
                        {
                                String reqMsg = "[100:100]" + userIds[0] + "&" + userIds[1] + ".jpg";
                                Bitmap bit = downloadUserIconMultiple( reqMsg, 50, 50 );
                                if ( bit != null )
                                {
                                        if ( bit.getWidth() > 50 ) bit = Bitmap.createScaledBitmap( bit, 50, 50, true );
                                        canvas.drawBitmap( bit, 0, 0, null );
                                }
                                else canvas.drawBitmap( defaultIcon, 0, 0, null );
                        }
                        else if ( userIds.length == 3 )
                        {
                                String reqMsg = "[100:100]" + userIds[0] + "&" + userIds[1] + "&" + userIds[2] + ".jpg";
                                Bitmap bit = downloadUserIconMultiple( reqMsg, 50, 50 );
                                if ( bit != null )
                                {
                                        if ( bit.getWidth() > 50 ) bit = Bitmap.createScaledBitmap( bit, 50, 50, true );
                                        canvas.drawBitmap( bit, 0, 0, null );
                                }
                                else canvas.drawBitmap( defaultIcon, 0, 0, null );
                        }
                        else if ( userIds.length > 3 )
                        {
                                String reqMsg = "[100:100]" + userIds[0] + "&" + userIds[1] + "&" + userIds[2] + "&" + userIds[3] + ".jpg";
                                Bitmap bit = downloadUserIconMultiple( reqMsg, 50, 50 );
                                if ( bit != null )
                                {
                                        if ( bit.getWidth() > 50 ) bit = Bitmap.createScaledBitmap( bit, 50, 50, true );
                                        canvas.drawBitmap( bit, 0, 0, null );
                                }
                                else canvas.drawBitmap( defaultIcon, 0, 0, null );
                        }
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                        return Bitmap.createScaledBitmap( BitmapFactory.decodeResource( context.getResources(), R.drawable.img_contract_list ), 50, 50, true );
                }
                return tempIcon;
        }

        //
        private String getMapKey( String value )
        {
                for ( Entry<String, String> entry : Define.mEmoticonMappingNameMap.entrySet() )
                {
                        if ( entry.getValue().equals( value ) )
                        {
                                return entry.getKey();
                        }
                }
                return "";
        }
        ImageGetter imageGetter = new ImageGetter() {
                @Override
                public Drawable getDrawable( String source )
                {
                        try
                        {
                                int resID = parent.getResources().getIdentifier( getMapKey( source ), "drawable", "kr.co.ultari.atsmart.basic" );
                                Drawable drawable = context.getResources().getDrawable( resID );
                                drawable.setBounds( 0, 0, 50, 50 );
                                return drawable;
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                        return null;
                }
        };
        private static final String TAG = "/AtSmart/ChatRoomItem";

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
        public void run()
        {
                char[] buf = new char[2048];
                int rcv = 0;
                StringBuffer sb = new StringBuffer();
                sc = null;
                InputStreamReader ir = null;
                BufferedReader br = null;
                Timer noopTimer = null;
                try
                {
                        sb.delete( 0, sb.length() );
                        sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                        .getServerPort( Define.mContext ) ) );
                        sc.setSoTimeout( 30000 );
                        ir = new InputStreamReader( sc.getInputStream() );
                        br = new BufferedReader( ir );
                        Log.e( "ChatRoomItem", "send:" + userIds );
                        send( "SearchNamePositionInfo\t" + roomId + "\t" + userIds );
                        while ( !onDestroy && (rcv = br.read( buf, 0, 2048 )) >= 0 )
                        {
                                sb.append( new String( buf, 0, rcv ) );
                                int pos;
                                while ( (pos = sb.indexOf( "\f" )) >= 0 )
                                {
                                        String rcvStr = codec.DecryptSEED( sb.substring( 0, pos ) );
                                        sb.delete( 0, pos + 1 );
                                        String command = "";
                                        ArrayList<String> param = new ArrayList<String>();
                                        String nowStr = "";
                                        for ( int i = 0; i < rcvStr.length(); i++ )
                                        {
                                                if ( rcvStr.charAt( i ) == '\t' )
                                                {
                                                        if ( command.equals( "" ) ) command = nowStr;
                                                        else param.add( nowStr );
                                                        nowStr = "";
                                                }
                                                else if ( i == (rcvStr.length() - 1) )
                                                {
                                                        nowStr += rcvStr.charAt( i );
                                                        if ( command.equals( "" ) ) command = nowStr;
                                                        else param.add( nowStr );
                                                        nowStr = "";
                                                }
                                                else
                                                {
                                                        nowStr += rcvStr.charAt( i );
                                                }
                                        }
                                        process( command, param );
                                }
                        }
                }
                catch ( SocketException se )
                {
                        Log.e( "ContactDetail", se.getMessage() );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                finally
                {
                        if ( sc != null )
                        {
                                try
                                {
                                        sc.close();
                                        sc = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                        if ( ir != null )
                        {
                                try
                                {
                                        ir.close();
                                        ir = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                        if ( br != null )
                        {
                                try
                                {
                                        br.close();
                                        br = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                        if ( noopTimer != null )
                        {
                                noopTimer.cancel();
                                noopTimer = null;
                        }
                }
                try
                {
                        Thread.sleep( 5000 );
                }
                catch ( InterruptedException ie )
                {}
        }

        public void process( String command, ArrayList<String> param )
        {
                if ( command.equals( "[NamePositionInfo]" ) )
                {
                        Message m = searchHandler.obtainMessage( Define.AM_ADD_SEARCH, param );
                        searchHandler.sendMessage( m );
                }
        }

        public void send( String msg ) throws Exception
        {
                msg.replaceAll( "\f", "" );
                sc.send( codec.EncryptSEED( msg ) + '\f' );
        }
        public Handler searchHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_ADD_SEARCH )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        Log.e( "ChatRoomItem", "0:" + param.get( 0 ) + ", 1:" + param.get( 1 ) );
                                        Database.instance( context ).updateChatRoomUserNames( param.get( 0 ), param.get( 1 ) );
                                        
                                        onDestroy = true;
                                }
                                else
                                {
                                        super.handleMessage( msg );
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };
}
