package kr.co.ultari.atsmart.basic.subview;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.subdata.ChatData;
import kr.co.ultari.atsmart.basic.util.ImageUtil;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import kr.co.ultari.atsmart.basic.view.ChatWindow;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

@SuppressLint( "HandlerLeak" )
public class ChatItem extends ArrayAdapter<ChatData> implements OnClickListener, OnLongClickListener {
        private static final short CHAT_ITEM_TYPE_DATE = 0x01;
        private static final short CHAT_ITEM_TYPE_I_AM_TELLER = 0x02;
        private static final short CHAT_ITEM_TYPE_I_AM_LISTENER = 0x03;
        ChatWindow parent; 
        private Context context;
        private UserImageManager photoManager;
        private int photoMax;
        private int imageMax;
        private Handler uploadHandler;
        View parentView;

        public ChatItem( Context context, ArrayList<ChatData> arrays, Handler uploadHandler, View parentView, ChatWindow parent )
        {
                super( context, R.layout.chat_item_left, arrays );
                this.photoManager = new UserImageManager( itemHandler );
                this.uploadHandler = uploadHandler;
                this.parentView = parentView;
                this.parent = parent; 
                this.context = context;
                int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                photoMax = screenWidth / 8;
        }

        public int getTalkCount() // 날자 빼고 데이터만, ChatSelectRowsPerOneTime
        {
                int ret = 0;
                for ( int i = 0; i < getCount(); i++ )
                {
                        if ( getItem( i ).talkerId != null && !getItem( i ).talkerId.equals( "system" ) ) ret++;
                }
                return ret;
        }

        public View getView( int position, View convertView, ViewGroup viewGroup )
        {
                LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                String myId = Define.getMyId( context );
                ChatData data = getItem( position );
                if ( data.talkContent.indexOf( context.getString( R.string.gsInMessage ) ) >= 0
                                || data.talkContent.indexOf( context.getString( R.string.outMessage ) ) >= 0 ) data.talkerId = "system";
                
                short item_type;
                if ( data.talkerId != null && data.talkerId.equals( "system" ) ) item_type = CHAT_ITEM_TYPE_DATE;
                else if ( data.talkerId != null && data.talkerId.equals( myId ) ) item_type = CHAT_ITEM_TYPE_I_AM_TELLER;
                else item_type = CHAT_ITEM_TYPE_I_AM_LISTENER;
                // 뷰 생성
                if ( data.view == null )
                {
                        Log.d( "kr.co.ultari.atsmart.basic", "Item 생성 : " + data.msgId );
                        switch ( item_type )
                        {
                        case CHAT_ITEM_TYPE_DATE :
                                data.view = inflater.inflate( R.layout.chat_item_date, viewGroup, false );
                                break;
                        case CHAT_ITEM_TYPE_I_AM_TELLER :
                                data.view = inflater.inflate( R.layout.chat_item_right, viewGroup, false );
                                break;
                        case CHAT_ITEM_TYPE_I_AM_LISTENER :
                                data.view = inflater.inflate( R.layout.chat_item_left, viewGroup, false );
                                break;
                        }
                }
                imageMax = parentView.getWidth() / 3;
                // 뷰 데이터 세팅
                switch ( item_type )
                {
                case CHAT_ITEM_TYPE_DATE :
                {
                        TextView dd = ( TextView ) data.view.findViewById( R.id.talk_date_divider );
                        dd.setTypeface( Define.tfRegular );
                        dd.setText( data.talkContent );
                        dd.setClickable( false );
                        break;
                }
                case CHAT_ITEM_TYPE_I_AM_TELLER :
                {
                        int unReadCount = 0;
                        String[] unReadUserIds = data.unreadUserIds.split( "," );
                        for ( int i = 0; i < unReadUserIds.length; i++ )
                        {
                                if ( !myId.equals( unReadUserIds[i] ) ) unReadCount++;
                        }
                        TextView ct = ( TextView ) data.view.findViewById( R.id.un_read_count );
                        ct.setTypeface( Define.tfRegular );
                        if ( unReadCount == 0 ) ct.setVisibility( View.GONE );
                        else ct.setText( "" + unReadCount );
                        String timeStr = StringUtil.getTimeStr( data.talkDate );
                        View v = ( View ) data.view.findViewById( R.id.right_bubble_box );
                        v.setTag( data );
                        v.setOnClickListener( this );
                        v.setOnLongClickListener( this ); // GJ
                        TextView dt = ( TextView ) data.view.findViewById( R.id.right_bubble_time );
                        dt.setTypeface( Define.tfRegular );
                        dt.setText( timeStr );
                        TextView content = ( TextView ) data.view.findViewById( R.id.right_bubble_content );
                        content.setTypeface( Define.tfRegular );
                        ImageView fileIcon = ( ImageView ) data.view.findViewById( R.id.talk_file_icon );
                        TextView fileName = ( TextView ) data.view.findViewById( R.id.talk_fileName );
                        fileName.setTypeface( Define.tfRegular );
                        TextView fileSize = ( TextView ) data.view.findViewById( R.id.talk_fileSize );
                        fileSize.setTypeface( Define.tfRegular );
                        ProgressBar fileProg = ( ProgressBar ) data.view.findViewById( R.id.talk_progress );
                        if ( data.getAttachFile( itemHandler ) != null ) fileProg.setProgress( 100 );
                        else fileProg.setProgress( 0 );
                        ImageView sendResultIcon = ( ImageView ) data.view.findViewById( R.id.send_result );
                        if ( data.sendComplete )
                        {
                                sendResultIcon.setVisibility( View.GONE );
                                fileProg.setProgress( 100 );
                        }
                        short dataType = getDataType( data );
                        data.dataType = dataType;
                        switch ( dataType )
                        {
                        case Define.TYPE_IMAGE :
                                fileIcon.setVisibility( View.GONE );
                                fileName.setVisibility( View.GONE );
                                fileSize.setVisibility( View.GONE );
                                fileProg.setVisibility( View.GONE );
                                content.setVisibility( View.VISIBLE );
                                File f = data.getAttachFile( itemHandler );
                                try
                                {
                                        if ( f != null )
                                        {
                                                if ( data.bmp == null )
                                                {
                                                        data.bmp = ImageUtil.loadBitmapFromFileWithMaxWidth( f.getCanonicalPath(), ( int ) imageMax );
                                                }
                                                content.setBackground( new BitmapDrawable( context.getResources(), data.bmp ) );
                                        }
                                }
                                catch ( Exception e )
                                {
                                        Log.e( "kr.co.ultari.atsmart.basic", "ChatItem", e );
                                }
                                break;
                        case Define.TYPE_MOVIE :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_movie );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_AUDIO :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_audio );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_TEXT :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_text );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_FILE :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_file );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_EXCEL :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_excel );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_PPT :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_powerpoint );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_DOC :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_word );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_PDF :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_pdf );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_HWP :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_han );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        default :
                                fileIcon.setVisibility( View.GONE );
                                fileName.setVisibility( View.GONE );
                                fileSize.setVisibility( View.GONE );
                                fileProg.setVisibility( View.GONE );
                                content.setVisibility( View.VISIBLE );
                                if ( parent.getSearchKey() != null && data.talkContent.indexOf( parent.getSearchKey() ) >= 0 )
                                {
                                        Spannable spanString = new SpannableString( data.talkContent );
                                        int start = data.talkContent.indexOf( parent.getSearchKey() );
                                        int end = parent.getSearchKey().length();
                                        spanString.setSpan( new BackgroundColorSpan( Color.BLUE ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
                                        content.setText( spanString );
                                }
                                else
                                {
                                        content.setText( data.talkContent );
                                }
                                break;
                        }
                        break;
                }
                case CHAT_ITEM_TYPE_I_AM_LISTENER :
                {
                        TextView nt = ( TextView ) data.view.findViewById( R.id.left_bubble_name );
                        nt.setTypeface( Define.tfRegular );
                        nt.setText( data.talkerName );
                        String timeStr = StringUtil.getTimeStr( data.talkDate );
                        View v = ( View ) data.view.findViewById( R.id.left_bubble_box );
                        v.setTag( data );
                        v.setOnClickListener( this );
                        v.setOnLongClickListener( this ); // GJ
                        TextView dt = ( TextView ) data.view.findViewById( R.id.left_bubble_time );
                        dt.setTypeface( Define.tfRegular );
                        dt.setText( timeStr );
                        TextView content = ( TextView ) data.view.findViewById( R.id.left_bubble_content );
                        content.setTypeface( Define.tfRegular );
                        ImageView fileIcon = ( ImageView ) data.view.findViewById( R.id.talk_file_icon );
                        TextView fileName = ( TextView ) data.view.findViewById( R.id.talk_fileName );
                        fileName.setTypeface( Define.tfRegular );
                        TextView fileSize = ( TextView ) data.view.findViewById( R.id.talk_fileSize );
                        fileSize.setTypeface( Define.tfRegular );
                        ProgressBar fileProg = ( ProgressBar ) data.view.findViewById( R.id.talk_progress );
                        //ProgressBar loadingProg = ( ProgressBar ) data.view.findViewById( R.id.left_bubble_progress );
                        if ( data.getAttachFile( itemHandler ) == null ) fileProg.setProgress( 0 );
                        else fileProg.setProgress( 100 );
                        short dataType = getDataType( data );
                        data.dataType = dataType;
                        switch ( dataType )
                        {
                        case Define.TYPE_IMAGE :
                                fileIcon.setVisibility( View.GONE );
                                fileName.setVisibility( View.GONE );
                                fileSize.setVisibility( View.GONE );
                                fileProg.setVisibility( View.GONE );
                                content.setVisibility( View.VISIBLE );
                                File f = data.getAttachFile( itemHandler );
                                try
                                {
                                        if ( f != null )
                                        {
                                                if ( data.bmp == null )
                                                {
                                                        data.bmp = ImageUtil.loadBitmapFromFileWithMaxWidth( f.getCanonicalPath(), ( int ) imageMax );
                                                }
                                                content.setBackground( new BitmapDrawable( context.getResources(), data.bmp ) );
                                        }
                                        else
                                        {
                                                //loadingProg.setVisibility( View.VISIBLE );
                                                data.download();
                                        }
                                }
                                catch ( Exception e )
                                {
                                        Log.e( "kr.co.ultari.atsmart.basic", "ChatItem", e );
                                }
                                break;
                        case Define.TYPE_MOVIE :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_movie );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_AUDIO :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_audio );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_TEXT :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_text );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_FILE :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_file );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_EXCEL :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_excel );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_PPT :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_powerpoint );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_DOC :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_word );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_PDF :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_pdf );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        case Define.TYPE_HWP :
                                content.setVisibility( View.GONE );
                                fileIcon.setVisibility( View.VISIBLE );
                                fileName.setVisibility( View.VISIBLE );
                                fileSize.setVisibility( View.VISIBLE );
                                fileProg.setVisibility( View.VISIBLE );
                                fileIcon.setImageResource( R.drawable.icon_han );
                                fileName.setText( data.talkContent.substring( data.talkContent.lastIndexOf( '/' ) + 1 ) );
                                fileSize.setText( getFileSize( data.talkContent ) );
                                break;
                        default :
                                fileIcon.setVisibility( View.GONE );
                                fileName.setVisibility( View.GONE );
                                fileSize.setVisibility( View.GONE );
                                fileProg.setVisibility( View.GONE );
                                content.setVisibility( View.VISIBLE );
                                if ( parent.getSearchKey() != null && data.talkContent.indexOf( parent.getSearchKey() ) >= 0 )
                                {
                                        Spannable spanString = new SpannableString( data.talkContent );
                                        int start = data.talkContent.indexOf( parent.getSearchKey() );
                                        int end = parent.getSearchKey().length();
                                        spanString.setSpan( new BackgroundColorSpan( Color.BLUE ), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
                                        content.setText( spanString );
                                }
                                else
                                {
                                        content.setText( data.talkContent );
                                }
                                break;
                        }
                        Bitmap bmp = photoManager.receiveUserImage( data.talkerId, data.view );
                        if ( bmp != null )
                        {
                                ImageView imgView = ( ImageView ) data.view.findViewById( R.id.left_bubble_user_image );
                                imgView.setImageBitmap( bmp );
                                imgView.setTag( data.talkerId );
                                imgView.setOnClickListener( this );
                        }
                        break;
                }
                }
                return data.view;
        }

        private String getFileSize( String content )
        {
                String retSize = "";
                if ( content.indexOf( "FILE://" ) >= 0 )
                {
                        File f = new File( content.substring( 7 ) );
                        retSize = StringUtil.getFileSizeText( f.length() );
                }
                else if ( content.indexOf( "ATTACH://" ) >= 0 )
                {
                        retSize = StringUtil.getFileSizeText( Long.parseLong( content.substring( 9, content.lastIndexOf( "/" ) ) ) );
                }
                return retSize;
        }
        // 이미지 다운로드 이벤트 핸들러
        public Handler itemHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        if ( msg.what == Define.AM_REFRESH )
                        {
                                try
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<View> ar = ( ArrayList<View> ) msg.obj;
                                        String userId = msg.getData().getString( "userId" );
                                        Bitmap bmp = photoManager.getUserImage( userId );
                                        for ( int i = 0; i < ar.size(); i++ )
                                        {
                                                View v = ar.get( i );
                                                //ProgressBar loadingProg = ( ProgressBar ) v.findViewById( R.id.left_bubble_progress );
                                                //loadingProg.setVisibility( View.GONE );
                                                ImageView imgView = ( ImageView ) v.findViewById( R.id.left_bubble_user_image );
                                                imgView.setImageBitmap( bmp );
                                        }
                                }
                                catch(Exception e)
                                {
                                        e.printStackTrace();
                                }
                        }
                        else if ( msg.what == Define.AM_DOWNLOAD_RESULT )
                        {
                                try
                                {
                                        ChatData data = ( ChatData ) msg.obj;
                                        if ( data.dataType == Define.TYPE_IMAGE )
                                        {
                                                if ( msg.arg1 == 100 )
                                                {
                                                        if ( data.bmp == null )
                                                        {
                                                                File f = data.getAttachFile( itemHandler );
                                                                Log.d( "kr.co.ultari.atsmart.basic", "GotImage : " + f );
                                                                try
                                                                {
                                                                        data.bmp = ImageUtil.loadBitmapFromFileWithMaxWidth( f.getCanonicalPath(), ( int ) imageMax );
                                                                }
                                                                catch ( Exception e )
                                                                {
                                                                        Log.e( "kr.co.ultari.atsmart.basic", "ChatItemMessageHandler", e );
                                                                }
                                                        }
                                                        TextView rcontent = ( TextView ) data.view.findViewById( R.id.right_bubble_content );
                                                        if ( rcontent != null ) rcontent.setBackground( new BitmapDrawable( context.getResources(), data.bmp ) );
                                                        TextView lcontent = ( TextView ) data.view.findViewById( R.id.left_bubble_content );
                                                        if ( lcontent != null ) lcontent.setBackground( new BitmapDrawable( context.getResources(), data.bmp ) );
                                                        uploadHandler.sendEmptyMessage( Define.AM_COMPLETE );
                                                }
                                        }
                                        else if ( data.dataType != Define.TYPE_CHAT )
                                        {
                                                ProgressBar fileProg = ( ProgressBar ) data.view.findViewById( R.id.talk_progress );
                                                if ( fileProg != null ) fileProg.setProgress( msg.arg1 );
                                                if ( msg.arg1 == 100 ) data.runAttach( getMimeType( data ) );
                                        }
                                }
                                catch(Exception e)
                                {
                                        e.printStackTrace();
                                }
                        }
                        else if ( msg.what == Define.AM_SEND_COMPLETE )
                        {
                                try
                                {
                                        String msgId = ( String ) msg.obj;
                                        for ( int i = 0; i < getCount(); i++ )
                                        {
                                                if ( getItem( i ).msgId.equals( msgId ) )
                                                {
                                                        View v = getItem( i ).view;
                                                        ImageView sendResultIcon = ( ImageView ) v.findViewById( R.id.send_result );
                                                        if ( sendResultIcon != null )
                                                        {
                                                                sendResultIcon.setVisibility( View.GONE );
                                                        }
                                                }
                                        }
                                }
                                catch(Exception e)
                                {
                                        e.printStackTrace();
                                }
                        }
                        else if ( msg.what == Define.AM_READ_COMPLETE )
                        {
                                try
                                {
                                        String msgId = ( String ) msg.obj;
                                        for ( int i = 0; i < getCount(); i++ )
                                        {
                                                if ( getItem( i ).msgId.equals( msgId ) )
                                                {
                                                        View v = getItem( i ).view;
                                                        if ( v == null ) continue;
                                                        TextView ct = ( TextView ) v.findViewById( R.id.un_read_count );
                                                        ct.setTypeface( Define.tfRegular );
                                                        String[] ar = getItem( i ).unreadUserIds.split( "," );
                                                        int unreadcount = 0;
                                                        for ( int j = 0; j < ar.length; j++ )
                                                        {
                                                                if ( !ar[j].equals( Define.getMyId( context ) ) ) unreadcount++;
                                                        }
                                                        if ( ct != null )
                                                        {
                                                                if ( unreadcount == 0 ) ct.setVisibility( View.GONE );
                                                                else ct.setText( "" + unreadcount );
                                                        }
                                                }
                                        }
                                }
                                catch(Exception e)
                                {
                                        e.printStackTrace();
                                }
                        }
                        else if ( msg.what == Define.AM_UPLOAD_COMPLETE )
                        {
                                try
                                {
                                        ChatData data = ( ChatData ) msg.obj;
                                        int percent = msg.arg1;
                                        ProgressBar fileProg = ( ProgressBar ) data.view.findViewById( R.id.talk_progress );
                                        if ( fileProg != null ) fileProg.setProgress( percent );
                                }
                                catch(Exception e)
                                {
                                        e.printStackTrace();
                                }
                        }
                        else
                        {
                                super.handleMessage( msg );
                        }
                }
        };
        // 여기서부터는 사진 다운로더임
        // 중복으로 요청하지 않기 위해 별도로 매니저를 사용
        class UserImageManager {
                private ConcurrentHashMap<String, Bitmap> userImages = null;
                private ConcurrentHashMap<String, UserImageDownloader> userImageDownloaders = null;
                private Handler imgCompleteHandler;

                public UserImageManager( Handler imgCompleteHandler )
                {
                        userImages = new ConcurrentHashMap<String, Bitmap>();
                        userImageDownloaders = new ConcurrentHashMap<String, UserImageDownloader>();
                        this.imgCompleteHandler = imgCompleteHandler;
                }

                public synchronized Bitmap receiveUserImage( String userId, View view )
                {
                        if ( userImages.get( userId ) != null ) return userImages.get( userId );
                        UserImageDownloader downloader = userImageDownloaders.get( userId );
                        if ( downloader == null )
                        {
                                downloader = new UserImageDownloader( userId, imgDownloadCompleteHandler );
                                userImageDownloaders.put( userId, downloader );
                        }
                        downloader.addReceiveView( view );
                        return null;
                }

                public Bitmap getUserImage( String userId )
                {
                        return userImages.get( userId );
                }
                Handler imgDownloadCompleteHandler = new Handler() {
                        public void handleMessage( Message msg )
                        {
                                if ( msg.what == Define.AM_REFRESH )
                                {
                                        UserImageDownloader downloader = ( UserImageDownloader ) msg.obj;
                                        String userId = downloader.userId;
                                        if ( downloader.bmp == null )
                                        {
                                                Bitmap icon = BitmapFactory.decodeResource( context.getResources(), R.drawable.img_profile_100x100 );
                                                userImages.put( userId, icon );
                                        }
                                        else
                                        {
                                                userImages.put( userId, ImageUtil.getDrawOval( downloader.bmp) );
                                                //userImages.put( userId, ImageUtil.getDrawOvalNoResize( downloader.bmp, photoMax ) );
                                        }
                                        Message m = imgCompleteHandler.obtainMessage( Define.AM_REFRESH, downloader.rcvViewAr );
                                        Bundle b = new Bundle();
                                        b.putString( "userId", userId );
                                        m.setData( b );
                                        imgCompleteHandler.sendMessage( m );
                                        userImageDownloaders.remove( userId );
                                }
                                else
                                {
                                        super.handleMessage( msg );
                                }
                        }
                };
        }
        // 이미지 다운로더
        class UserImageDownloader extends Thread {
                public String userId = null;
                private Handler handler = null;
                private ArrayList<View> rcvViewAr = null;
                public Bitmap bmp = null;

                public UserImageDownloader( String userId, Handler handler )
                {
                        this.userId = userId;
                        this.handler = handler;
                        this.rcvViewAr = new ArrayList<View>();
                        this.start();
                }

                public void addReceiveView( View v )
                {
                        rcvViewAr.add( v );
                }

                public void run()
                {
                        Socket sc = null;
                        InputStream is = null;
                        OutputStream os = null;
                        InputStreamReader ir = null;
                        OutputStreamWriter ow = null;
                        ByteArrayOutputStream bo = null;
                        Log.d( "kr.co.ultari.atsmart.basic", "이미지 다운로드 시작 : " + userId );
                        try
                        {
                                sc = UltariSocketUtil.getProxySocket();
                                is = sc.getInputStream();
                                os = sc.getOutputStream();
                                ir = new InputStreamReader( is, "EUC-KR" );
                                ow = new OutputStreamWriter( os, "EUC-KR" );
                                String header = "GET /" + userId + " HTTP/1.1\r\n\r\n";
                                ow.write( header );
                                ow.flush();
                                int rcv;
                                StringBuffer gotHeader = new StringBuffer();
                                byte[] buffer = new byte[1024];
                                bo = new ByteArrayOutputStream();
                                long totalSize = 0;
                                long headerSize = 0;
                                while ( (rcv = is.read( buffer, 0, 1024 )) >= 0 )
                                {
                                        totalSize += rcv;
                                        bo.write( buffer, 0, rcv );
                                        bo.flush();
                                        String s = new String( buffer, 0, rcv, "EUC-KR" );
                                        gotHeader.append( s );
                                        int returnPos = -1;
                                        if ( (returnPos = gotHeader.indexOf( "\r\n\r\n" )) >= 0 )
                                        {
                                                String lengthStr = gotHeader.substring( gotHeader.indexOf( "Content-Length:" ) + 15 );
                                                lengthStr = lengthStr.substring( 0, lengthStr.indexOf( "\r\n" ) );
                                                lengthStr = lengthStr.trim();
                                                headerSize = returnPos + 4;
                                                break;
                                        }
                                }
                                while ( (rcv = is.read( buffer, 0, 1024 )) >= 0 )
                                {
                                        totalSize += rcv;
                                        bo.write( buffer, 0, rcv );
                                        bo.flush();
                                }
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inJustDecodeBounds = false;
                                bmp = BitmapFactory.decodeByteArray( bo.toByteArray(), ( int ) headerSize, ( int ) (totalSize - headerSize) );
                                Message msg = handler.obtainMessage( Define.AM_REFRESH, this );
                                handler.sendMessage( msg );
                                Log.d( "kr.co.ultari.atsmart.basic", "이미지 다운로드 성공 : " + userId );
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                                Message msg = handler.obtainMessage( Define.AM_REFRESH, this );
                                handler.sendMessage( msg );
                                Log.d( "kr.co.ultari.atsmart.basic", "이미지 다운로드 실패 : " + userId );
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
                                if ( is != null )
                                {
                                        try
                                        {
                                                is.close();
                                                is = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                                if ( os != null )
                                {
                                        try
                                        {
                                                os.close();
                                                os = null;
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
                                if ( ow != null )
                                {
                                        try
                                        {
                                                ow.close();
                                                ow = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                                if ( bo != null )
                                {
                                        try
                                        {
                                                bo.close();
                                                bo = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                        }
                }
        }

        public short getDataType( ChatData data )
        {
                return StringUtil.getChatType( data.talkContent );
        }

        @Override
        public void onClick( View v )
        {
                if ( v.getId() == R.id.left_bubble_user_image )
                {
                        String userId = ( String ) v.getTag();
                        ActionManager.showPhoto( userId );
                }
                else
                {
                        ChatData data = ( ChatData ) v.getTag();
                        if ( data.talkContent.indexOf( "ATTACH://" ) < 0 && data.talkContent.indexOf( "FILE://" ) < 0 ) return;
                        File f = data.getAttachFile( itemHandler );
                        if ( f == null ) data.download();
                        else
                        {
                                //2016-02-23
                                if(data.downloadSuccess)
                                {
                                        data.runAttach( getMimeType( data ) );
                                        //Log.d( "ChatItem", "downloadSuccess true" );
                                }
                                //else
                                        //Log.d( "ChatItem", "downloadSuccess false" );
                                
                                //data.runAttach( getMimeType( data ) );
                                //
                        }
                }
        }

        public String getMimeType( ChatData data )
        {
                switch ( data.dataType )
                {
                case Define.TYPE_IMAGE :
                        return "image/*";
                case Define.TYPE_MOVIE :
                        return "video/*";
                case Define.TYPE_AUDIO :
                        return "audio/*";
                case Define.TYPE_TEXT :
                        return "text/*";
                case Define.TYPE_FILE :
                        return "application/*";
                case Define.TYPE_EXCEL :
                        return "application/vnd.ms-excel";
                case Define.TYPE_PPT :
                        return "application/vnd.ms-powerpoint";
                case Define.TYPE_DOC :
                        return "application/msword";
                case Define.TYPE_PDF :
                        return "application/pdf";
                case Define.TYPE_HWP :
                        return "application/hwp";
                }
                return "application/*";
        }

        @Override
        public boolean onLongClick( View v )
        {
                ChatData data = ( ChatData ) v.getTag();
                parent.openPopupMenu( data );
                Log.d( "OnLongClick", "Return false" );
                return false;
        }
}
