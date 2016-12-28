package kr.co.ultari.atsmart.basic.subview;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.ArrayList;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import kr.co.ultari.atsmart.basic.view.MessageView;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MessageDetailView extends MessengerActivity implements OnClickListener {
        public static final String TAG = "MessageDetailView";
        private TextView subjectView = null;
        private TextView dateView = null;
        private TextView contentView = null;
        private ListView userList = null;
        private ArrayList<MessageUserData> users = null;
        private TextView userListCount = null;
        private TextView senderNameView = null;
        private TextView senderPartView = null;
        private ListView fileList = null;
        private ArrayList<String> files = null;
        private LinearLayout attach_title = null;
        private LinearLayout receiver_title = null;
        private LinearLayout sender_title = null;
        private RelativeLayout message_view_layout_sender = null;
        private int message_view_layout_sender_height = 0;
        private ImageButton btnClose = null;
        private Button btnDelete = null;
        private ImageView message_view_attach_image = null;
        private ProgressDialog prog = null;
        private AttachDownloader downloader = null;
        public String msgId = null;
        private ImageView message_sender_icon = null;
        private String senderInfo = null;

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                setContentView( R.layout.popup_view_message );
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE ); //2016-12-13
                message_view_attach_image = ( ImageView ) findViewById( R.id.message_view_attach_image );
                msgId = getIntent().getStringExtra( "msgId" );
                Database.instance( Define.mContext ).updateMessageRead( msgId );
                Message m = MessageView.instance().msgBoxHandler.obtainMessage( Define.AM_REFRESH );
                MessageView.instance().msgBoxHandler.sendMessage( m );
                ArrayList<ArrayList<String>> row = Database.instance( Define.mContext ).selectMessageData( msgId );
                ArrayList<String> cell = row.get( 0 );
                String subject = cell.get( 5 );
                String content = cell.get( 7 );
                String date = cell.get( 3 );
                String sender = cell.get( 1 );
                String senderPart = cell.get( 2 );
                senderInfo = cell.get( 0 ) + "\\" + cell.get( 1 );
                subjectView = ( TextView ) findViewById( R.id.message_view_subject );
                subjectView.setTypeface( Define.tfRegular );
                dateView = ( TextView ) findViewById( R.id.message_view_date );
                dateView.setTypeface( Define.tfRegular );
                contentView = ( TextView ) findViewById( R.id.message_view_content );
                contentView.setTypeface( Define.tfRegular );
                
                if ( subject != null && subject.indexOf( "√" ) >= 0 )
                {
                        subject = subject.substring( 1 );
                }
                subjectView.setText( subject );
                dateView.setText( StringUtil.getMessageDate( date ) );
                contentView.setText( content );
                users = new ArrayList<MessageUserData>();
                userList = ( ListView ) findViewById( R.id.message_view_receivers );
                userList.setAdapter( new MessageUserList( this, users ) );
                senderNameView = ( TextView ) findViewById( R.id.message_view_sender_name );
                senderNameView.setTypeface( Define.tfRegular );
                senderPartView = ( TextView ) findViewById( R.id.message_view_sender_part );
                senderPartView.setTypeface( Define.tfRegular );
                senderNameView.setText( sender );
                senderPartView.setText( senderPart );
                files = new ArrayList<String>();
                fileList = ( ListView ) findViewById( R.id.message_view_file_list );
                fileList.setAdapter( new MessageFileList( this, files ) );
                attach_title = ( LinearLayout ) findViewById( R.id.messge_view_attach_title );
                receiver_title = ( LinearLayout ) findViewById( R.id.messge_view_receiver_title );
                sender_title = ( LinearLayout ) findViewById( R.id.messge_view_sender_title );
                attach_title.setOnClickListener( this );
                receiver_title.setOnClickListener( this );
                sender_title.setOnClickListener( this );
                message_view_layout_sender = ( RelativeLayout ) findViewById( R.id.message_view_layout_sender );
                message_view_layout_sender_height = message_view_layout_sender.getHeight();
                btnClose = ( ImageButton ) findViewById( R.id.view_message_close );
                btnDelete = ( Button ) findViewById( R.id.deleteMessage );
                btnDelete.setTypeface( Define.tfRegular );
                btnClose.setOnClickListener( this );
                btnDelete.setOnClickListener( this );
                String[] parse = cell.get( 8 ).split( "/" );
                for ( int i = 0; i < parse.length; i++ )
                {
                        if ( parse[i].indexOf( "\\" ) <= 0 ) continue;
                        files.add( parse[i].substring( 0, parse[i].indexOf( "\\" ) ) );
                }
                parse = cell.get( 9 ).split( "/" );
                for ( int i = 0; i < parse.length; i++ )
                {
                        if ( parse[i].indexOf( "\\" ) <= 0 ) continue;
                        users.add( new MessageUserData( parse[i].substring( 0, parse[i].indexOf( "\\" ) ), parse[i].substring( parse[i].indexOf( "\\" ) + 1 ) ) );
                }
                userListCount = ( TextView ) findViewById( R.id.message_view_receivers_count );
                userListCount.setTypeface( Define.tfRegular );
                userListCount.setText( "" + users.size() );
                if ( cell.get( 6 ).equals( "" ) )
                {
                        message_view_attach_image.setVisibility( View.GONE );
                }
                else
                {
                        File defaultPng = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AtSmart" + File.separator
                                        + "MessageAttach" + File.separator + msgId + File.separator + "default.png" );
                        if ( defaultPng.exists() )
                        {
                                FileInputStream fis = null;
                                try
                                {
                                        fis = new FileInputStream( defaultPng );
                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                        if ( defaultPng.length() > 20000 )
                                        {
                                                options.inSampleSize = 2;
                                        }
                                        Bitmap bitmap = BitmapFactory.decodeStream( fis, null, options );
                                        message_view_attach_image.setImageBitmap( bitmap );
                                }
                                catch ( Exception e )
                                {
                                        if ( fis != null )
                                        {
                                                try
                                                {
                                                        fis.close();
                                                        fis = null;
                                                }
                                                catch ( Exception ee )
                                                {}
                                        }
                                }
                        }
                        else
                        {
                                prog = new ProgressDialog( this );
                                prog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
                                prog.setCancelable( true );
                                prog.setMessage( getString( R.string.message_sending ) );
                                prog.setMax( 100 );
                                prog.setProgress( 0 );
                                prog.show();
                                prog.setOnCancelListener( new DialogInterface.OnCancelListener() {
                                        public void onCancel( DialogInterface dialog )
                                        {
                                                if ( downloader != null )
                                                {
                                                        downloader.finish();
                                                        downloader = null;
                                                }
                                        }
                                } );
                                downloader = new AttachDownloader( msgId, "default.png" );
                        }
                }
                message_sender_icon = ( ImageView ) findViewById( R.id.message_sender_icon );
                message_sender_icon.setOnClickListener( this );
                if ( cell.get( 0 ).equals( Define.getMyId() ) )
                {
                        message_sender_icon.setVisibility( View.INVISIBLE );
                }
                resetSize();
        }
        public Handler msgVwHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        if ( msg.what == Define.AM_RECEIVE_COMPLETE )
                        {
                                if ( msg.arg1 < 0 )
                                {
                                        prog.dismiss();
                                        prog = null;
                                        downloader = null;
                                }
                                else if ( msg.arg1 == 100 )
                                {
                                        prog.dismiss();
                                        prog = null;
                                        if ( downloader.fileName.equals( "default.png" ) )
                                        {
                                                FileInputStream fis = null;
                                                try
                                                {
                                                        fis = new FileInputStream( downloader.myFilePath );
                                                        BitmapFactory.Options options = new BitmapFactory.Options();
                                                        options.inSampleSize = 2;
                                                        Bitmap bitmap = BitmapFactory.decodeStream( fis, null, options );
                                                        message_view_attach_image.setImageBitmap( bitmap );
                                                }
                                                catch ( Exception e )
                                                {
                                                        if ( fis != null )
                                                        {
                                                                try
                                                                {
                                                                        fis.close();
                                                                        fis = null;
                                                                }
                                                                catch ( Exception ee )
                                                                {}
                                                        }
                                                }
                                        }
                                        else
                                        {
                                                try
                                                {
                                                        runAttach( downloader.myFilePath.getCanonicalPath() );
                                                }
                                                catch ( Exception e )
                                                {}
                                        }
                                        downloader = null;
                                }
                                else
                                {
                                        prog.setProgress( msg.arg1 );
                                }
                        }
                        else if ( msg.what == Define.AM_RUN_ATTACH )
                        {
                                String fileName = ( String ) msg.obj;
                                downloadAndRun( fileName );
                        }
                        super.handleMessage( msg );
                }
        };

        public void runAttach( String filePath )
        {
                short type = getType( filePath );
                String mime = getMimeType( type );
                runAttach( new File( filePath ), mime );
        }

        private short getType( String content )
        {
                String ext = content.substring( content.lastIndexOf( '.' ) + 1 ).toLowerCase();
                if ( ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" ) || ext.equalsIgnoreCase( "png" )
                                || ext.equalsIgnoreCase( "bmp" ) ) return Define.TYPE_IMAGE;
                else if ( ext.equalsIgnoreCase( "mp4" ) || ext.equalsIgnoreCase( "avi" ) || ext.equalsIgnoreCase( "mpeg" ) || ext.equalsIgnoreCase( "mpg" )
                                || ext.equalsIgnoreCase( "mov" ) ) return Define.TYPE_MOVIE;
                else if ( ext.equalsIgnoreCase( "mp3" ) || ext.equalsIgnoreCase( "wav" ) || ext.equalsIgnoreCase( "au" ) ) return Define.TYPE_AUDIO;
                else if ( ext.equalsIgnoreCase( "txt" ) ) return Define.TYPE_TEXT;
                else if ( ext.equalsIgnoreCase( "doc" ) || ext.equalsIgnoreCase( "docx" ) ) return Define.TYPE_DOC;
                else if ( ext.equalsIgnoreCase( "xls" ) || ext.equalsIgnoreCase( "xlsx" ) ) return Define.TYPE_EXCEL;
                else if ( ext.equalsIgnoreCase( "ppt" ) || ext.equalsIgnoreCase( "pptx" ) ) return Define.TYPE_PPT;
                else if ( ext.equalsIgnoreCase( "pdf" ) ) return Define.TYPE_PDF;
                else if ( ext.equalsIgnoreCase( "hwp" ) || ext.equalsIgnoreCase( "x-hwp" ) ) return Define.TYPE_HWP;
                else return Define.TYPE_FILE;
        }

        public String getMimeType( short type )
        {
                switch ( type )
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

        public void runAttach( File f, String mimeType )
        {
                Intent intent = new Intent( android.content.Intent.ACTION_VIEW );
                Uri uri = Uri.fromFile( f );
                intent.setDataAndType( uri, mimeType );
                Define.getContext().startActivity( intent );
        }
        class AttachDownloader extends Thread {
                private String msgId;
                public String fileName;
                private Socket sc = null;
                public File myFilePath;
                public boolean m_bFinished = false;

                public AttachDownloader( String msgId, String fileName )
                {
                        this.msgId = msgId;
                        this.fileName = fileName;
                        String folderPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AtSmart" + File.separator
                                        + "MessageAttach" + File.separator + msgId;
                        File folder = new File( folderPath );
                        folder.mkdirs();
                        this.myFilePath = new File( folder, fileName );
                        this.start();
                }

                public void run()
                {
                        byte[] buf = new byte[4096];
                        char[] cbuf = new char[4096];
                        InputStream is = null;
                        OutputStream os = null;
                        FileOutputStream fo = null;
                        InputStreamReader ir = null;
                        OutputStreamWriter ow = null;
                        try
                        {
                                sc = UltariSocketUtil.getProxySocket();
                                is = sc.getInputStream();
                                os = sc.getOutputStream();
                                ir = new InputStreamReader( is, "EUC-KR" );
                                ow = new OutputStreamWriter( os, "EUC-KR" );
                                fo = new FileOutputStream( myFilePath );
                                String sndMsg;
                                if ( Define.useUnicode )
                                {
                                        sndMsg = "GETA\t" + msgId + "\t" + myFilePath.getName() + "\tM";
                                }
                                else
                                {
                                        if ( Define.useOldFileTransferProtocol ) sndMsg = "GETA\t" + msgId + "\t"
                                                        + URLEncoder.encode( myFilePath.getName(), "MS949" ) + "\tM"; // 93
                                        else sndMsg = "GETA\t" + msgId + "\t" + myFilePath.getName() + "\tM"; // scm
                                }
                                sndMsg = sndMsg.trim();
                                Log.d( TAG, "GetAttach : " + sndMsg );
                                ow.write( sndMsg );
                                ow.flush();
                                int rcv = ir.read( cbuf, 0, 4096 );
                                if ( rcv < 0 || new String( cbuf, 0, rcv ).indexOf( "ready" ) < 0 )
                                {
                                        return;
                                }
                                String rcvMsg = new String( cbuf, 0, rcv );
                                rcvMsg = rcvMsg.trim();
                                String msgAr[] = rcvMsg.split( "\t" );
                                String fileName = msgAr[1];
                                String fileLength = msgAr[2];
                                Log.d( "MessageReceiver", fileName + ":" + fileLength );
                                sndMsg = "ok\t0\f";
                                ow.write( sndMsg );
                                ow.flush();
                                long totalFileLength = Long.parseLong( fileLength );
                                long gotFileLength = 0;
                                int oldPercent = -1;
                                while ( (rcv = is.read( buf, 0, 4096 )) >= 0 )
                                {
                                        fo.write( buf, 0, rcv );
                                        fo.flush();
                                        gotFileLength += rcv;
                                        int percent = ( int ) (( double ) gotFileLength / ( double ) totalFileLength * ( double ) 100);
                                        if ( percent == 100 && gotFileLength < totalFileLength ) percent = 99;
                                        if ( oldPercent != percent )
                                        {
                                                Message m = msgVwHandler.obtainMessage( Define.AM_RECEIVE_COMPLETE );
                                                m.arg1 = percent;
                                                msgVwHandler.sendMessage( m );
                                                Log.d( "Percent", "Percent : " + percent );
                                                oldPercent = percent;
                                        }
                                        if ( gotFileLength >= totalFileLength )
                                        {
                                                sndMsg = "ok\t" + gotFileLength + "\f";
                                                ow.write( sndMsg );
                                                ow.flush();
                                                break;
                                        }
                                }
                                rcv = is.read( buf, 0, 4096 );
                        }
                        catch ( Exception e )
                        {
                                Log.e( TAG, "Downloader", e );
                                Message m = msgVwHandler.obtainMessage( Define.AM_RECEIVE_COMPLETE );
                                m.arg1 = -1;
                                msgVwHandler.sendMessage( m );
                        }
                        finally
                        {
                                if ( fo != null )
                                {
                                        try
                                        {
                                                fo.close();
                                                fo = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
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
                                Log.d( TAG, "ReceiveFileFinished" );
                        }
                }

                public void finish()
                {
                        if ( sc != null )
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
                        }
                        m_bFinished = true;
                }
        }

        private int getListViewHeight( ListView list )
        {
                ListAdapter adapter = list.getAdapter();
                int listviewHeight = 0;
                list.measure( MeasureSpec.makeMeasureSpec( MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED ),
                                MeasureSpec.makeMeasureSpec( 0, MeasureSpec.UNSPECIFIED ) );
                listviewHeight = list.getMeasuredHeight() * adapter.getCount() + (adapter.getCount() * list.getDividerHeight());
                return listviewHeight;
        }

        private void resetSize()
        {
                if ( files.size() == 0 )
                {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.0F );
                        attach_title.setLayoutParams( params );
                }
                {
                        int listHeight = getListViewHeight( fileList );
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, listHeight, 0.0F );
                        fileList.setLayoutParams( params );
                }
                if ( users.size() == 0 )
                {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.0F );
                        receiver_title.setLayoutParams( params );
                }
                {
                        int listHeight = getListViewHeight( userList );
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, listHeight, 0.0F );
                        userList.setLayoutParams( params );
                }
        }

        private void showHideFileList()
        {
                if ( files.size() > 0 )
                {
                        if ( fileList.getHeight() == 0 )
                        {
                                int listHeight = getListViewHeight( fileList );
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, listHeight, 0.0F );
                                fileList.setLayoutParams( params );
                        }
                        else
                        {
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.0F );
                                fileList.setLayoutParams( params );
                        }
                }
        }

        private void showHideSender()
        {
                if ( message_view_layout_sender.getHeight() == 0 )
                {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,
                                        message_view_layout_sender_height, 0.0f );
                        message_view_layout_sender.setLayoutParams( params );
                }
                else
                {
                        message_view_layout_sender_height = message_view_layout_sender.getHeight();
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.0f );
                        message_view_layout_sender.setLayoutParams( params );
                }
        }

        private void showHideReceiverList()
        {
                if ( users.size() > 0 )
                {
                        if ( userList.getHeight() == 0 )
                        {
                                int listHeight = getListViewHeight( userList );
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, listHeight, 0.0F );
                                userList.setLayoutParams( params );
                        }
                        else
                        {
                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.0F );
                                userList.setLayoutParams( params );
                        }
                }
        }
        class MessageUserData {
                public String id;
                public String name;

                public MessageUserData( String id, String name )
                {
                        this.id = id;
                        this.name = name;
                }
        }
        class MessageUserList extends ArrayAdapter<MessageUserData> {
                Context context;

                public MessageUserList( Context context, ArrayList<MessageUserData> users )
                {
                        super( context, R.layout.view_message_user_list, users );
                        this.context = context;
                }

                public View getView( int position, View convertView, ViewGroup viewGroup )
                {
                        View view = convertView;
                        if ( view == null )
                        {
                                LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                                view = inflater.inflate( R.layout.view_message_user_list, viewGroup, false );
                        }
                        ;
                        MessageUserData data = getItem( position );
                        UserImageView userImage = ( UserImageView ) view.findViewById( R.id.messageViewUserImage );
                        TextView userName = ( TextView ) view.findViewById( R.id.messageViewUserName );
                        userImage.setUserId( data.id );
                        userName.setText( data.name );
                        return view;
                }
        }
        class MessageFileList extends ArrayAdapter<String> implements OnClickListener {
                Context context;

                public MessageFileList( Context context, ArrayList<String> files )
                {
                        super( context, R.layout.message_view_file_list, files );
                        this.context = context;
                }

                public View getView( int position, View convertView, ViewGroup viewGroup )
                {
                        View view = convertView;
                        if ( view == null )
                        {
                                LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                                view = inflater.inflate( R.layout.message_view_file_list, viewGroup, false );
                        }
                        ;
                        String data = getItem( position );
                        TextView fileName = ( TextView ) view.findViewById( R.id.messageViewFileName );
                        ImageButton runButton = ( ImageButton ) view.findViewById( R.id.message_view_run_file );
                        fileName.setText( data );
                        runButton.setTag( data );
                        runButton.setTag( data );
                        runButton.setOnClickListener( this );
                        return view;
                }

                @Override
                public void onClick( View v )
                {
                        String fileName = ( String ) v.getTag();
                        if ( fileName != null && !fileName.equals( "" ) )
                        {
                                Message m = msgVwHandler.obtainMessage( Define.AM_RUN_ATTACH, fileName );
                                msgVwHandler.sendMessage( m );
                        }
                }
        }

        public void downloadAndRun( String fileName )
        {
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AtSmart" + File.separator + "MessageAttach"
                                + File.separator + msgId + File.separator + fileName;
                File targetFile = new File( filePath );
                if ( targetFile.exists() )
                {
                        runAttach( filePath );
                }
                else
                {
                        prog = new ProgressDialog( this );
                        prog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
                        prog.setCancelable( true );
                        prog.setMessage( getString( R.string.message_sending ) );
                        prog.setMax( 100 );
                        prog.setProgress( 0 );
                        prog.show();
                        prog.setOnCancelListener( new DialogInterface.OnCancelListener() {
                                public void onCancel( DialogInterface dialog )
                                {
                                        if ( downloader != null )
                                        {
                                                downloader.finish();
                                                downloader = null;
                                        }
                                }
                        } );
                        downloader = new AttachDownloader( msgId, fileName );
                }
        }

        @Override
        public void onClick( View v )
        {
                if ( v == sender_title ) showHideSender();
                else if ( v == receiver_title ) showHideReceiverList();
                else if ( v == attach_title ) showHideFileList();
                else if ( v == btnClose ) finish();
                else if ( v == message_sender_icon )
                {
                        Intent i = new Intent( Define.mContext, kr.co.ultari.atsmart.basic.subview.SendMessageView.class );
                        i.putExtra( "receivers", senderInfo );
                        startActivity( i );
                        finish();
                }
                else if ( v == btnDelete )
                {
                        AlertDialog.Builder alert_confirm = new AlertDialog.Builder( this );
                        alert_confirm.setMessage( "삭제하시겠습니까?" ).setCancelable( false ).setPositiveButton( "확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick( DialogInterface dialog, int which )
                                {
                                        Database.instance( Define.mContext ).deleteMessage( msgId );
                                        Message m = MessageView.instance().msgBoxHandler.obtainMessage( Define.AM_REFRESH );
                                        MessageView.instance().msgBoxHandler.sendMessage( m );
                                        finish();
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
        }
}
