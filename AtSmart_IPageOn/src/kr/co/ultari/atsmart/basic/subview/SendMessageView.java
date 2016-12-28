package kr.co.ultari.atsmart.basic.subview;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.StringTokenizer;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SendMessageView extends MessengerActivity implements OnClickListener {
        private ImageButton btnUser = null;
        private ImageButton btnPicture = null;
        private ImageButton btnFile = null;
        private Button btnSend = null;
        private LinearLayout layout_user = null;
        private RelativeLayout layout_picture = null;
        private LinearLayout layout_file = null;
        private ListView userList = null;
        private LinearLayout btnUserBottom = null;
        private LinearLayout btnPictureBottom = null;
        private LinearLayout btnFileBottom = null;
        public static String TAG = "SendMessageView";
        private ArrayList<MessageUserData> userAr = null;
        private MessageUserList userListAdapter = null;
        private ListView fileList = null;
        private ArrayList<File> fileAr = null;
        private MessageFileList fileListAdapter = null;
        private ImageButton btnClose = null;
        private TextView attach_image_comment = null;
        private ImageButton btnDeleteImage = null;
        private TextView add_user_comment = null;
        private TextView add_file_comment = null;
        private static final int GET_PICTURE_URI = 0x01;
        private static final int GET_USER_LIST = 0x02;
        private static final int SELECT_IMAGE = 0x03;
        private static final int SELECT_VIDEO = 0x04;
        private static final int SELECT_CAMERA = 0x05;
        private static final int SELECT_FILE = 0x06;
        private ImageView attach_image = null;
        private Uploader uploader = null;
        private ProgressDialog prog = null;
        private String msgId = null;
        private EditText subject = null;
        private EditText content = null;

        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                setContentView( R.layout.popup_write_message );
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE ); //2016-12-13
                btnUser = ( ImageButton ) findViewById( R.id.message_write_btn_user );
                btnPicture = ( ImageButton ) findViewById( R.id.message_write_btn_picture );
                btnFile = ( ImageButton ) findViewById( R.id.message_write_btn_file );
                btnClose = ( ImageButton ) findViewById( R.id.write_message_close );
                btnClose.setOnClickListener( this );
                btnUserBottom = ( LinearLayout ) findViewById( R.id.message_write_btn_user_bottom );
                btnPictureBottom = ( LinearLayout ) findViewById( R.id.message_write_btn_picture_bottom );
                btnFileBottom = ( LinearLayout ) findViewById( R.id.message_write_btn_file_bottom );
                btnUser.setOnClickListener( this );
                btnPicture.setOnClickListener( this );
                btnFile.setOnClickListener( this );
                layout_user = ( LinearLayout ) findViewById( R.id.message_write_user_layout );
                layout_picture = ( RelativeLayout ) findViewById( R.id.message_write_picture );
                layout_file = ( LinearLayout ) findViewById( R.id.message_write_file );
                userList = ( ListView ) findViewById( R.id.message_write_user );
                layout_user.setVisibility( View.VISIBLE );
                layout_picture.setVisibility( View.INVISIBLE );
                layout_file.setVisibility( View.INVISIBLE );
                //btnUser.setBackgroundColor( 0xFF232323 );
                //btnPicture.setBackgroundColor( 0xFF2C2C2C );
                //btnFile.setBackgroundColor( 0xFF2C2C2C );
                btnUserBottom.setBackgroundColor( 0xFF74AFEA );
                btnPictureBottom.setBackgroundColor( 0xFFD6D6D6 );
                btnFileBottom.setBackgroundColor( 0xFFD6D6D6 );
                userAr = new ArrayList<MessageUserData>();
                userListAdapter = new MessageUserList( this, userAr );
                userList.setAdapter( userListAdapter );
                fileList = ( ListView ) findViewById( R.id.message_write_file_list );
                fileAr = new ArrayList<File>();
                fileListAdapter = new MessageFileList( this, fileAr );
                fileList.setAdapter( fileListAdapter );
                attach_image_comment = ( TextView ) findViewById( R.id.messageWriteNoImageComment );
                attach_image_comment.setOnClickListener( this );
                add_user_comment = ( TextView ) findViewById( R.id.message_write_add_user );
                add_user_comment.setOnClickListener( this );
                add_file_comment = ( TextView ) findViewById( R.id.message_write_add_file );
                add_file_comment.setOnClickListener( this );
                registerForContextMenu( add_file_comment );
                attach_image = ( ImageView ) findViewById( R.id.messageWriteImage );
                attach_image.setOnClickListener( this );
                attach_image.setTag( "" );
                btnDeleteImage = ( ImageButton ) findViewById( R.id.message_write_delete_image );
                btnDeleteImage.setOnClickListener( this );
                btnSend = ( Button ) findViewById( R.id.sendMessage );
                btnSend.setTypeface( Define.tfRegular );
                btnSend.setOnClickListener( this );
                subject = ( EditText ) findViewById( R.id.message_write_subject );
                content = ( EditText ) findViewById( R.id.message_write_content );
                String receivers = getIntent().getStringExtra( "receivers" );
                if ( receivers != null )
                {
                        String[] userInfo = receivers.split( "/" );
                        for ( int i = 0; i < userInfo.length; i++ )
                        {
                                Log.d( TAG, "NowUserInfo : " + userInfo[i] );
                                int pos = userInfo[i].indexOf( "\\" );
                                if ( pos > 0 )
                                {
                                        userAr.add( new MessageUserData( userInfo[i].substring( 0, pos ), userInfo[i].substring( pos + 1 ) ) );
                                }
                        }
                        userListAdapter.notifyDataSetChanged();
                }
        }

        @Override
        public void onCreateContextMenu( ContextMenu menu, View view, ContextMenuInfo menuInfo )
        {
                menu.setHeaderTitle( "파일 선택" );
                menu.add( 0, SELECT_IMAGE, Menu.NONE, getString( R.string.album ) );
                menu.add( 0, SELECT_VIDEO, Menu.NONE, getString( R.string.video ) );
                menu.add( 0, SELECT_CAMERA, Menu.NONE, getString( R.string.camera ) );
                menu.add( 0, SELECT_FILE, Menu.NONE, getString( R.string.explore ) );
        }

        @Override
        public boolean onContextItemSelected( MenuItem item )
        {
                super.onContextItemSelected( item );
                if ( item.getItemId() == SELECT_IMAGE )
                {
                        Intent intent = new Intent();
                        intent.setAction( Intent.ACTION_PICK );
                        intent.setType( "image/*" );
                        startActivityForResult( Intent.createChooser( intent, getString( R.string.selectPhoto ) ), SELECT_IMAGE );
                        return true;
                }
                else if ( item.getItemId() == SELECT_VIDEO )
                {
                        Intent intent = new Intent();
                        intent.setAction( Intent.ACTION_PICK );
                        intent.setType( "video/*" );
                        startActivityForResult( Intent.createChooser( intent, getString( R.string.selectVideo ) ), SELECT_VIDEO );
                        return true;
                }
                else if ( item.getItemId() == SELECT_CAMERA )
                {
                        Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
                        startActivityForResult( intent, SELECT_CAMERA );
                }
                else if ( item.getItemId() == SELECT_FILE )
                {
                        Intent intent = new Intent( getApplicationContext(), kr.co.ultari.atsmart.basic.view.FileBrowser.class );
                        startActivityForResult( intent, SELECT_FILE );
                        return true;
                }
                return false;
        }

        private void onClickUser()
        {
                layout_user.setVisibility( View.VISIBLE );
                layout_picture.setVisibility( View.INVISIBLE );
                layout_file.setVisibility( View.INVISIBLE );
                //btnUser.setBackgroundColor( 0xFF232323 );
                //btnPicture.setBackgroundColor( 0xFF2C2C2C );
                //btnFile.setBackgroundColor( 0xFF2C2C2C );
                btnUserBottom.setBackgroundColor( 0xFF74AFEA );
                btnPictureBottom.setBackgroundColor( 0xFFD6D6D6 );
                btnFileBottom.setBackgroundColor( 0xFFD6D6D6 );
                selectUser();
        }

        private void onClickPicture()
        {
                layout_user.setVisibility( View.INVISIBLE );
                layout_picture.setVisibility( View.VISIBLE );
                layout_file.setVisibility( View.INVISIBLE );
                //btnUser.setBackgroundColor( 0xFF2C2C2C );
                //btnPicture.setBackgroundColor( 0xFF232323 );
                //btnFile.setBackgroundColor( 0xFF2C2C2C );
                btnUserBottom.setBackgroundColor( 0xFFD6D6D6 );
                btnPictureBottom.setBackgroundColor( 0xFF74AFEA );
                btnFileBottom.setBackgroundColor( 0xFFD6D6D6 );
                selectImage();
        }

        private void onClickFile()
        {
                layout_user.setVisibility( View.INVISIBLE );
                layout_picture.setVisibility( View.INVISIBLE );
                layout_file.setVisibility( View.VISIBLE );
                //btnUser.setBackgroundColor( 0xFF2C2C2C );
                //btnPicture.setBackgroundColor( 0xFF2C2C2C );
                //btnFile.setBackgroundColor( 0xFF232323 );
                btnUserBottom.setBackgroundColor( 0xFFD6D6D6 );
                btnPictureBottom.setBackgroundColor( 0xFFD6D6D6 );
                btnFileBottom.setBackgroundColor( 0xFF74AFEA );
                openContextMenu( add_file_comment );
        }

        private void selectUser()
        {
                String userIds = "";
                String userNames = "";
                for ( int i = 0; i < userAr.size(); i++ )
                {
                        MessageUserData data = userAr.get( i );
                        if ( !userIds.equals( "" ) ) userIds += ",";
                        if ( !userNames.equals( "" ) ) userNames += ",";
                        userIds += data.id;
                        userNames += data.name;
                }
                Bundle bundle = new Bundle();
                bundle.putString( "type", "message" );
                bundle.putString( "userIds", userIds );
                bundle.putString( "userNames", userNames );
                Intent intent = new Intent( Define.mContext.getApplicationContext(), kr.co.ultari.atsmart.basic.view.GroupSearchView.class );
                intent.putExtras( bundle );
                startActivityForResult( intent, GET_USER_LIST );
        }

        private void selectImage()
        {
                Intent intent = new Intent( Intent.ACTION_PICK );
                intent.setType( "image/*" );
                startActivityForResult( intent, GET_PICTURE_URI );
        }

        @Override
        public void onClick( View arg0 )
        {
                if ( arg0 == btnUser ) onClickUser();
                else if ( arg0 == btnPicture ) onClickPicture();
                else if ( arg0 == btnFile ) onClickFile();
                else if ( arg0 == btnClose ) finish();
                else if ( arg0 == attach_image_comment )
                {
                        selectImage();
                }
                else if ( arg0 == attach_image )
                {
                        String path = ( String ) attach_image.getTag();
                        if ( path == null || path.equals( "" ) )
                        {
                                Intent intent = new Intent( Intent.ACTION_PICK );
                                intent.setType( "image/*" );
                                startActivityForResult( intent, GET_PICTURE_URI );
                        }
                        else
                        {
                                Intent intent = new Intent();
                                intent.setAction( Intent.ACTION_VIEW );
                                intent.setDataAndType( Uri.parse( "file://" + attach_image.getTag() ), "image/*" );
                                startActivity( intent );
                        }
                }
                else if ( arg0 == btnDeleteImage )
                {
                        attach_image.setImageResource( R.drawable.noti_picture_noimg );
                        attach_image.setTag( "" );
                }
                else if ( arg0 == add_user_comment )
                {
                        selectUser();
                }
                else if ( arg0 == add_file_comment )
                {
                        openContextMenu( add_file_comment );
                }
                else if ( arg0 == btnSend )
                {
                        if ( prog != null ) return;
                        if ( subject.getText().toString().equals( "" ) )
                        {
                                alert( "제목을 입력하세요." );
                                return;
                        }
                        if ( content.getText().toString().equals( "" ) )
                        {
                                alert( "내용을 입력하세요." );
                                return;
                        }
                        if ( userAr.size() == 0 )
                        {
                                alert( "수신자를 선택하세요." );
                                return;
                        }
                        msgId = Define.getMyId() + "_" + StringUtil.getNowDateTime();
                        if ( attach_image.getTag().equals( "" ) && fileAr.size() == 0 )
                        {
                                Message m = msgHandler.obtainMessage( Define.AM_UPLOAD_COMPLETE, null );
                                msgHandler.sendMessage( m );
                                return;
                        }
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
                                        if ( uploader != null )
                                        {
                                                uploader.finish();
                                                uploader = null;
                                        }
                                }
                        } );
                        ArrayList<String> files = new ArrayList<String>();
                        for ( int i = 0; i < fileAr.size(); i++ )
                        {
                                try
                                {
                                        files.add( fileAr.get( i ).getCanonicalPath() );
                                }
                                catch ( IOException e )
                                {}
                        }
                        uploader = new Uploader( msgId, ( String ) attach_image.getTag(), files, msgHandler );
                }
        }

        private void alert( String msg )
        {
                AlertDialog.Builder alert = new AlertDialog.Builder( this );
                alert.setPositiveButton( "확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick( DialogInterface dialog, int which )
                        {
                                dialog.dismiss(); // 닫기
                        }
                } );
                alert.setMessage( msg );
                alert.show();
        }
        private Handler msgHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        if ( msg.what == Define.AM_UPLOAD_RESULT )
                        {
                                if ( msg.arg1 < 0 )
                                {
                                        prog.dismiss();
                                        prog = null;
                                }
                                else
                                {
                                        prog.setProgress( msg.arg1 );
                                        if ( msg.arg1 == 100 )
                                        {
                                                prog.dismiss();
                                                prog = null;
                                                Message m = msgHandler.obtainMessage( Define.AM_UPLOAD_COMPLETE, null );
                                                msgHandler.sendMessage( m );
                                        }
                                }
                        }
                        else if ( msg.what == Define.AM_UPLOAD_COMPLETE )
                        {
                                String[] parse = Define.totalName.split( "#" );
                                String subj = subject.getText().toString();
                                String cont = content.getText().toString();
                                String attachImg = ( String ) attach_image.getTag();
                                if ( attachImg == null ) attachImg = "";
                                MainActivity.sendMessage( msgId, Define.getMyId(), Define.getMyName(), parse[2], subj, cont, fileAr, userAr, attachImg );
                                finish();
                        }
                }
        };

        @Override
        protected void onActivityResult( int requestCode, int resultCode, Intent data )
        {
                super.onActivityResult( requestCode, resultCode, data );
                Log.d( TAG, requestCode + ":" + resultCode + ":" + Activity.RESULT_OK );
                if ( requestCode == GET_PICTURE_URI && resultCode == Activity.RESULT_OK )
                {
                        String url = data.getData().toString();
                        FileInputStream fis = null;
                        try
                        {
                                File m_nowSelectedFile = new File( URLDecoder.decode( getPath( url ), "KSC5601" ) );
                                fis = new FileInputStream( m_nowSelectedFile );
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inSampleSize = 2;
                                Bitmap bitmap = BitmapFactory.decodeStream( fis, null, options );
                                fis.close();
                                attach_image.setImageBitmap( bitmap );
                                attach_image.setTag( m_nowSelectedFile.getCanonicalPath() );
                        }
                        catch ( Exception e )
                        {
                                Log.e( TAG, "onActivityResult", e );
                        }
                        finally
                        {
                                if ( fis != null )
                                {
                                        try
                                        {
                                                fis.close();
                                                fis = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                        }
                }
                else if ( requestCode == GET_USER_LIST && resultCode == Activity.RESULT_OK )
                {
                        Bundle b = data.getExtras();
                        String userIds = b.getString( "userIds" );
                        String userNames = b.getString( "userNames" );
                        userAr.clear();
                        StringTokenizer st1 = new StringTokenizer( userIds, "," );
                        StringTokenizer st2 = new StringTokenizer( userNames, "," );
                        while ( st1.hasMoreElements() && st2.hasMoreElements() )
                        {
                                String id = st1.nextToken();
                                String name = st2.nextToken();
                                if ( id.equals( Define.getMyId() ) )
                                {
                                        continue;
                                }
                                userAr.add( new MessageUserData( id, name ) );
                        }
                        userListAdapter.notifyDataSetChanged(); // 2015-12-14
                }
                else if ( requestCode == SELECT_IMAGE && resultCode == Activity.RESULT_OK )
                {
                        Uri _uri = data.getData();
                        String filePath = null;
                        Cursor c = getContentResolver().query( _uri, null, null, null, null );
                        if ( c.moveToNext() )
                        {
                                filePath = c.getString( c.getColumnIndex( MediaStore.MediaColumns.DATA ) );
                        }
                        c.close();
                        fileAr.add( new File( filePath ) );
                        fileListAdapter.notifyDataSetChanged();
                }
                else if ( requestCode == SELECT_VIDEO && resultCode == Activity.RESULT_OK )
                {
                        Uri _uri = data.getData();
                        String filePath = null;
                        Cursor c = getContentResolver().query( _uri, null, null, null, null );
                        if ( c.moveToNext() )
                        {
                                filePath = c.getString( c.getColumnIndex( MediaStore.MediaColumns.DATA ) );
                        }
                        c.close();
                        fileAr.add( new File( filePath ) );
                        fileListAdapter.notifyDataSetChanged();
                }
                else if ( requestCode == SELECT_CAMERA && resultCode == Activity.RESULT_OK )
                {
                        Uri _uri = data.getData();
                        String filePath = null;
                        Cursor c = getContentResolver().query( _uri, null, null, null, null );
                        if ( c.moveToNext() )
                        {
                                filePath = c.getString( c.getColumnIndex( MediaStore.MediaColumns.DATA ) );
                        }
                        c.close();
                        fileAr.add( new File( filePath ) );
                        fileListAdapter.notifyDataSetChanged();
                }
                else if ( requestCode == SELECT_FILE && resultCode == Activity.RESULT_OK )
                {
                        String filePath = data.getStringExtra( "PATH" );
                        fileAr.add( new File( filePath ) );
                        fileListAdapter.notifyDataSetChanged();
                }
        }

        private String getPath( String url )
        {
                return getRealPathFromURI( Uri.parse( url ) );
        }

        public String getRealPathFromURI( Uri contentUri )
        {
                try
                {
                        String filePath = "";
                        Cursor c = getContentResolver().query( contentUri, null, null, null, null );
                        if ( c.moveToNext() )
                        {
                                filePath = c.getString( c.getColumnIndex( MediaStore.MediaColumns.DATA ) );
                        }
                        c.close();
                        return filePath;
                }
                catch ( Exception e )
                {
                        Log.e( TAG, "getRealPathFromURI", e );
                        return null;
                }
        }
        public class MessageUserData {
                public String id;
                public String name;

                public MessageUserData( String id, String name )
                {
                        this.id = id;
                        this.name = name;
                }
        }
        class MessageUserList extends ArrayAdapter<MessageUserData> implements OnClickListener {
                Context context;

                public MessageUserList( Context context, ArrayList<MessageUserData> users )
                {
                        super( context, R.layout.write_message_user_list, users );
                        this.context = context;
                }

                public View getView( int position, View convertView, ViewGroup viewGroup )
                {
                        View view = convertView;
                        if ( view == null )
                        {
                                LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                                view = inflater.inflate( R.layout.write_message_user_list, viewGroup, false );
                        }
                        ;
                        MessageUserData data = getItem( position );
                        // ImageView userImage = (ImageView)view.findViewById(R.id.messageWriteUserImage);
                        TextView userName = ( TextView ) view.findViewById( R.id.messageWriteUserName );
                        ImageButton userButton = ( ImageButton ) view.findViewById( R.id.messageWriteUserDelete );
                        userName.setText( data.name );
                        userButton.setOnClickListener( this );
                        userButton.setTag( data.id );
                        return view;
                }

                @Override
                public void onClick( View arg0 )
                {
                        String clickUserId = ( String ) arg0.getTag();
                        for ( int i = 0; i < getCount(); i++ )
                        {
                                if ( clickUserId.equals( getItem( i ).id ) )
                                {
                                        this.remove( getItem( i ) );
                                        break;
                                }
                        }
                }
        }
        class MessageFileList extends ArrayAdapter<File> implements OnClickListener {
                Context context;

                public MessageFileList( Context context, ArrayList<File> files )
                {
                        super( context, R.layout.message_write_file_list, files );
                        this.context = context;
                }

                public View getView( int position, View convertView, ViewGroup viewGroup )
                {
                        View view = convertView;
                        if ( view == null )
                        {
                                LayoutInflater inflater = ( LayoutInflater ) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
                                view = inflater.inflate( R.layout.message_write_file_list, viewGroup, false );
                        }
                        ;
                        File data = getItem( position );
                        ImageView fileImage = ( ImageView ) view.findViewById( R.id.messageWriteFileIcon );
                        TextView fileName = ( TextView ) view.findViewById( R.id.messageWriteFileName );
                        ImageButton fileButton = ( ImageButton ) view.findViewById( R.id.messageWriteFileDelete );
                        fileName.setText( data.getName() );
                        fileButton.setOnClickListener( this );
                        String path = "";
                        try
                        {
                                path = data.getCanonicalPath();
                        }
                        catch ( Exception e )
                        {}
                        fileButton.setTag( path );
                        short fileType = getType( path );
                        if ( fileType == Define.TYPE_MOVIE ) fileImage.setImageResource( R.drawable.icon_movie );
                        else if ( fileType == Define.TYPE_AUDIO ) fileImage.setImageResource( R.drawable.icon_audio );
                        else if ( fileType == Define.TYPE_TEXT ) fileImage.setImageResource( R.drawable.icon_text );
                        else if ( fileType == Define.TYPE_EXCEL ) fileImage.setImageResource( R.drawable.icon_excel );
                        else if ( fileType == Define.TYPE_PPT ) fileImage.setImageResource( R.drawable.icon_powerpoint );
                        else if ( fileType == Define.TYPE_DOC ) fileImage.setImageResource( R.drawable.icon_word );
                        else if ( fileType == Define.TYPE_PDF ) fileImage.setImageResource( R.drawable.icon_pdf );
                        else if ( fileType == Define.TYPE_HWP ) fileImage.setImageResource( R.drawable.icon_han );
                        else fileImage.setImageResource( R.drawable.icon_file );
                        return view;
                }

                @Override
                public void onClick( View arg0 )
                {
                        String clickUserId = ( String ) arg0.getTag();
                        for ( int i = 0; i < getCount(); i++ )
                        {
                                try
                                {
                                        if ( clickUserId.equals( getItem( i ).getCanonicalPath() ) )
                                        {
                                                this.remove( getItem( i ) );
                                                break;
                                        }
                                }
                                catch ( IOException e )
                                {}
                        }
                }

                private short getType( String content )
                {
                        String ext = content.substring( content.lastIndexOf( '.' ) + 1 ).toLowerCase();
                        if ( ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" ) || ext.equalsIgnoreCase( "png" )
                                        || ext.equalsIgnoreCase( "bmp" ) ) return Define.TYPE_IMAGE;
                        else if ( ext.equalsIgnoreCase( "mp4" ) || ext.equalsIgnoreCase( "avi" ) || ext.equalsIgnoreCase( "mpeg" )
                                        || ext.equalsIgnoreCase( "mpg" ) || ext.equalsIgnoreCase( "mov" ) ) return Define.TYPE_MOVIE;
                        else if ( ext.equalsIgnoreCase( "mp3" ) || ext.equalsIgnoreCase( "wav" ) || ext.equalsIgnoreCase( "au" ) ) return Define.TYPE_AUDIO;
                        else if ( ext.equalsIgnoreCase( "txt" ) ) return Define.TYPE_TEXT;
                        else if ( ext.equalsIgnoreCase( "doc" ) || ext.equalsIgnoreCase( "docx" ) ) return Define.TYPE_DOC;
                        else if ( ext.equalsIgnoreCase( "xls" ) || ext.equalsIgnoreCase( "xlsx" ) ) return Define.TYPE_EXCEL;
                        else if ( ext.equalsIgnoreCase( "ppt" ) || ext.equalsIgnoreCase( "pptx" ) ) return Define.TYPE_PPT;
                        else if ( ext.equalsIgnoreCase( "pdf" ) ) return Define.TYPE_PDF;
                        else if ( ext.equalsIgnoreCase( "hwp" ) || ext.equalsIgnoreCase( "x-hwp" ) ) return Define.TYPE_HWP;
                        else return Define.TYPE_FILE;
                }
        }
        class Uploader extends Thread {
                private ArrayList<File> files;
                private String msgId;
                private boolean isFirstIsDefaultImage = false;
                private boolean m_bFinished = false;
                private Socket nowSocket = null;
                private Handler msgHandler;

                public Uploader( String msgId, String defaultFilePath, ArrayList<String> fileAr, Handler msgHandler )
                {
                        files = new ArrayList<File>();
                        this.msgHandler = msgHandler;
                        if ( !defaultFilePath.equals( "" ) )
                        {
                                files.add( new File( defaultFilePath ) );
                                isFirstIsDefaultImage = true;
                        }
                        for ( int i = 0; i < fileAr.size(); i++ )
                        {
                                files.add( new File( fileAr.get( i ) ) );
                        }
                        this.msgId = msgId;
                        this.start();
                }

                public void finish()
                {
                        if ( nowSocket != null )
                        {
                                if ( nowSocket != null )
                                {
                                        try
                                        {
                                                nowSocket.close();
                                                nowSocket = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                        }
                        m_bFinished = true;
                }

                public void run()
                {
                        long totalFileSize = 0;
                        long sndLength = 0;
                        int oldPercent = 0;
                        byte[] buf = new byte[4096];
                        char[] cbuf = new char[1024];
                        for ( int i = 0; i < files.size(); i++ )
                        {
                                totalFileSize += files.get( i ).length();
                        }
                        for ( int i = 0; i < files.size(); i++ )
                        {
                                if ( m_bFinished ) continue;
                                String uploadFileName = files.get( i ).getName();
                                Socket sc = null;
                                InputStream is = null;
                                OutputStream os = null;
                                FileInputStream fi = null;
                                InputStreamReader ir = null;
                                OutputStreamWriter ow = null;
                                try
                                {
                                        fi = new FileInputStream( files.get( i ) );
                                        long nowFileLength = files.get( i ).length();
                                        sc = UltariSocketUtil.getProxySocket();
                                        nowSocket = sc;
                                        is = sc.getInputStream();
                                        os = sc.getOutputStream();
                                        ow = new OutputStreamWriter( os, "EUC-KR" );
                                        ir = new InputStreamReader( is, "EUC-KR" );
                                        String sndMsg;
                                        if ( Define.useUnicode )
                                        {
                                                if ( isFirstIsDefaultImage && i == 0 ) sndMsg = "PUTA\t" + msgId + "\t" + "default.png" + "\tM";
                                                else sndMsg = "PUTA\t" + msgId + "\t" + uploadFileName + "\tM";
                                        }
                                        else
                                        {
                                                if ( Define.useOldFileTransferProtocol ) sndMsg = "PUTA\t" + msgId + "\t"
                                                                + URLEncoder.encode( uploadFileName, "MS949" ) + "\tM"; // 93
                                                else sndMsg = "PUTA\t" + msgId + "\t" + uploadFileName + "\tM"; // scm
                                        }
                                        sndMsg = sndMsg.trim();
                                        ow.write( sndMsg );
                                        ow.flush();
                                        int rcv = ir.read( cbuf, 0, 1024 );
                                        if ( rcv < 0 || new String( cbuf, 0, rcv ).indexOf( "ready" ) < 0 )
                                        {
                                                return;
                                        }
                                        while ( (rcv = fi.read( buf, 0, 4096 )) >= 0 )
                                        {
                                                sndLength += rcv;
                                                os.write( buf, 0, rcv );
                                                os.flush();
                                                Log.d( TAG, "Upload : " + sndLength + "/" + nowFileLength );
                                                int percent = ( int ) (( double ) sndLength / ( double ) totalFileSize * ( double ) 100);
                                                if ( oldPercent != percent )
                                                {
                                                        oldPercent = percent;
                                                        if ( percent != 100 )
                                                        {
                                                                Message m = msgHandler.obtainMessage( Define.AM_UPLOAD_RESULT, null );
                                                                m.arg1 = percent;
                                                                msgHandler.sendMessage( m );
                                                        }
                                                }
                                        }
                                        sndMsg = "finish\f";
                                        ow.write( sndMsg );
                                        ow.flush();
                                        rcv = ir.read( cbuf, 0, 1024 );
                                }
                                catch ( Exception e )
                                {
                                        e.printStackTrace();
                                        Message m = msgHandler.obtainMessage( Define.AM_UPLOAD_RESULT, null );
                                        m.arg1 = -1;
                                        msgHandler.sendMessage( m );
                                        return;
                                }
                                finally
                                {
                                        if ( fi != null )
                                        {
                                                try
                                                {
                                                        fi.close();
                                                        fi = null;
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
                                        nowSocket = null;
                                }
                        }
                        Message m = msgHandler.obtainMessage( Define.AM_UPLOAD_RESULT, null );
                        m.arg1 = 100;
                        msgHandler.sendMessage( m );
                }
        }
}
