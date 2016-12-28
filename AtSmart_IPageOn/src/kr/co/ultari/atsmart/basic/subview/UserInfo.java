package kr.co.ultari.atsmart.basic.subview;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.util.FmcSendBroadcast;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class UserInfo extends MessengerActivity implements OnClickListener {
        TextView phoneLabel;
        TextView mobileLabel;
        TextView emailLabel;
        TextView partLabel;
        // TextView noteLabel;
        private UserImageView img;
        private String id;
        private String name;
        private String nick;
        private Button btnChat, btnMobile, btnOffice, btnEmail;

        @Override
        public void onPause()
        {
                super.onPause();
        };

        @Override
        public void onResume()
        {
                Define.isHomeMode = false;
                super.onResume();
        };

        @Override
        protected void onUserLeaveHint()
        {
                Define.isHomeMode = true;
                finish();
                super.onUserLeaveHint();
        }

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                setContentView( R.layout.popup_user_info );
                try
                {
                        id = getIntent().getStringExtra( "ID" );
                        name = getIntent().getStringExtra( "NAME" );
                        nick = getIntent().getStringExtra( "NICK" );
                        String[] ar = StringUtil.parseName( name );
                        switch ( Define.SET_COMPANY )
                        {
                        /*
                         * case Define.REDCROSS:
                         * if ( ar.length > 0 )
                         * {
                         * TextView v = (TextView)findViewById(R.id.info_name);
                         * v.setText(ar[0]);
                         * }
                         * if ( ar.length > 1 )
                         * {
                         * TextView v = (TextView)findViewById(R.id.info_position);
                         * v.setText(ar[1]);
                         * }
                         * if ( ar.length > 2 )
                         * {
                         * TextView v = (TextView)findViewById(R.id.info_grade);
                         * v.setText(ar[2]);
                         * }
                         * if ( ar.length > 3 )
                         * {
                         * phoneLabel = (TextView)findViewById(R.id.info_phone);
                         * SpannableString content = new SpannableString(ar[3]);
                         * content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                         * phoneLabel.setText(ar[3]);
                         * phoneLabel.setOnClickListener(this);
                         * }
                         * if ( ar.length > 4 )
                         * {
                         * emailLabel = (TextView)findViewById(R.id.info_email);
                         * SpannableString content = new SpannableString(ar[5]);
                         * content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                         * emailLabel.setText(ar[5]);
                         * emailLabel.setOnClickListener(this);
                         * }
                         * if ( ar.length > 5 )
                         * {
                         * mobileLabel = (TextView)findViewById(R.id.info_mobile);
                         * SpannableString content = new SpannableString(ar[4]);
                         * content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
                         * mobileLabel.setText(ar[4]);
                         * mobileLabel.setOnClickListener(this);
                         * }
                         * if( ar.length > 6)
                         * {
                         * noteLabel = ( TextView ) findViewById( R.id.info_job );
                         * noteLabel.setText( ar[6] );
                         * }
                         * break;
                         */
                        default :
                                if ( ar.length > 0 )
                                {
                                        TextView v = ( TextView ) findViewById( R.id.info_name );
                                        v.setText( ar[0] );
                                }
                                if ( ar.length > 1 )
                                {
                                        TextView v = ( TextView ) findViewById( R.id.info_position );
                                        v.setText( ar[1] );
                                }
                                if ( ar.length > 2 )
                                {
                                        TextView v = ( TextView ) findViewById( R.id.info_grade );
                                        v.setText( nick );
                                }
                                if ( ar.length > 3 )
                                {
                                        phoneLabel = ( TextView ) findViewById( R.id.info_phone );
                                        SpannableString content = new SpannableString( ar[3] );
                                        content.setSpan( new UnderlineSpan(), 0, content.length(), 0 );
                                        phoneLabel.setText( ar[3] );
                                }
                                if ( ar.length > 4 )
                                {
                                        emailLabel = ( TextView ) findViewById( R.id.info_email );
                                        SpannableString content = new SpannableString( ar[6] );
                                        content.setSpan( new UnderlineSpan(), 0, content.length(), 0 );
                                        emailLabel.setText( ar[6] );
                                }
                                if ( ar.length > 5 )
                                {
                                        mobileLabel = ( TextView ) findViewById( R.id.info_mobile );
                                        SpannableString content = new SpannableString( ar[5] );
                                        content.setSpan( new UnderlineSpan(), 0, content.length(), 0 );
                                        mobileLabel.setText( ar[5] );
                                }
                                btnChat = ( Button ) findViewById( R.id.info_chat );
                                btnChat.setOnClickListener( this );
                                btnMobile = ( Button ) findViewById( R.id.info_call_mobile );
                                btnMobile.setOnClickListener( this );
                                if ( ar[5].equals( "" ) ) btnMobile.setVisibility( View.GONE );
                                btnOffice = ( Button ) findViewById( R.id.info_call_office );
                                btnOffice.setOnClickListener( this );
                                if ( ar[3].equals( "" ) ) btnOffice.setVisibility( View.GONE );
                                btnEmail = ( Button ) findViewById( R.id.info_send_email );
                                btnEmail.setOnClickListener( this );
                                if ( ar[6].equals( "" ) ) btnEmail.setVisibility( View.GONE );
                                // noteLabel = ( TextView ) findViewById( R.id.info_job );
                                // noteLabel.setText( "" );
                                partLabel = ( TextView ) findViewById( R.id.info_part );
                                partLabel.setText( ar[2] );
                                break;
                        }
                        img = ( UserImageView ) findViewById( R.id.UserIcon );
                        img.setUserId( id );
                        img.setOnClickListener( this );
                        Define.nowTopActivity = this;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void onClick( View view )
        {
                if ( view == btnOffice )
                {
                        /*
                         * Uri uri = Uri.parse("tel:" + phoneLabel.getText());
                         * Intent it = new Intent(Intent.ACTION_CALL, uri);
                         * startActivity(it);
                         */
                        // FMC call
                        String number = phoneLabel.getText().toString().trim();
                        if ( number.equals( "" ) ) return;
                        FmcSendBroadcast.FmcSendCall( phoneLabel.getText().toString().trim() ,0, getApplicationContext()); //2016-03-31
                }
                else if ( view == btnMobile )
                {
                        // FMC call
                        String number = mobileLabel.getText().toString().trim();
                        if ( number.equals( "" ) ) return;
                        FmcSendBroadcast.FmcSendCall( mobileLabel.getText().toString().trim() ,0, getApplicationContext()); //2016-03-31
                }
                else if ( view == btnEmail )
                {
                        String emailAddress = emailLabel.getText().toString();
                        if ( emailAddress.equals( "" ) ) return;
                        Uri uri = Uri.parse( "mailto:" + emailLabel.getText() );
                        Intent it = new Intent( Intent.ACTION_SENDTO, uri );
                        startActivity( it );
                }
                else if ( view == btnChat )
                {
                        String userId = id;
                        String[] ar = StringUtil.parseName( name );
                        String userName = ar[0] + ar[1];
                        if ( !userId.equalsIgnoreCase( Define.getMyId( getApplicationContext() ) ) )
                        {
                                String oUserIds = userId + "," + Define.getMyId( getApplicationContext() );
                                String userIds = StringUtil.arrange( oUserIds );
                                String userNames = userName + "," + StringUtil.getNamePosition( Define.getMyName() );
                                userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                                String roomId = userIds.replace( ",", "_" );
                                ArrayList<ArrayList<String>> array = Database.instance( getApplicationContext() ).selectChatRoomInfo( roomId );
                                if ( array.size() == 0 ) Database.instance( getApplicationContext() ).insertChatRoomInfo( roomId, userIds, userNames,
                                                StringUtil.getNowDateTime(), getString( R.string.newRoom ) );
                                ActionManager.openChat( getApplicationContext(), roomId, userIds, userNames );
                        }
                }
                else if ( view == img )
                {
                        String filename = "userImg.jpg";
                        File sd = Environment.getExternalStorageDirectory();
                        File dest = new File( sd, filename );
                        Bitmap bitmap = Define.getBitmap( id );
                        FileOutputStream out = null;
                        try
                        {
                                out = new FileOutputStream( dest );
                                bitmap.compress( Bitmap.CompressFormat.PNG, 90, out );
                                out.flush();
                                out.close();
                                Intent intent = new Intent();
                                intent.setAction( Intent.ACTION_VIEW );
                                Uri uriFromImageFile = Uri.fromFile( dest );
                                intent.setDataAndType( uriFromImageFile, "image/*" );
                                startActivity( intent );
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                        finally
                        {
                                if ( out != null )
                                {
                                        try
                                        {
                                                out.close();
                                                out = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                                try
                                {
                                        if ( bitmap != null )
                                        {
                                                bitmap = null;
                                        }
                                        if ( dest != null )
                                        {
                                                dest = null;
                                        }
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                }
        }
        private static final String TAG = "/AtSmart/UserInfo";

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
