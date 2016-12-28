package kr.co.ultari.atsmart.basic.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.subview.GroupUser;
import kr.co.ultari.atsmart.basic.subview.m_Adapter;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class CustomDialog extends MessengerActivity implements Runnable {
        private static final String TAG = "AtSmart/CustomDialog";
        private AmCodec codec;
        private Thread thread;
        public String explain;
        private String ids;
        private String roomId;
        private Button exitBtn, inviteBtn;
        private Context context;
        private ListView listview;
        private ArrayList<GroupUser> list;
        private m_Adapter adapter;
        private TextView tvTitle;

        // names, ids, roomid
        /*
         * public CustomDialog( Context context, String explain, String ids, String roomId)
         * {
         * super( context );
         * this.context = context;
         * this.explain = explain;
         * this.ids = ids;
         * this.roomId = roomId;
         * }
         */
        @Override
        protected void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
                setContentView( R.layout.custom_dialog );
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE ); //2016-12-13
                // getWindow().setBackgroundDrawable( new ColorDrawable( android.graphics.Color.TRANSPARENT ) );
                // getWindow().getAttributes().windowAnimations = R.style.CustomDialogAnimation;
                if ( getIntent() != null )
                {
                        this.explain = getIntent().getStringExtra( "UserNames" );
                        this.ids = getIntent().getStringExtra( "UserIds" );
                        this.roomId = getIntent().getStringExtra( "RoomId" );
                }
                tvTitle = ( TextView ) findViewById( R.id.custom_title_count );
                int count = StringUtil.getChatRoomCount( explain );
                tvTitle.setText( getString( R.string.userslist ) + "(" + count + ")" );
                tvTitle.setTypeface( Define.tfMedium );
                listview = ( ListView ) findViewById( R.id.listView );
                list = new ArrayList<GroupUser>();
                adapter = new m_Adapter( CustomDialog.this, R.layout.group_user_item, list );
                listview.setAdapter( adapter );
                codec = new AmCodec();
                thread = new Thread( this );
                thread.start();
                exitBtn = ( Button ) findViewById( R.id.exitBtn );
                exitBtn.setTypeface( Define.tfRegular );
                exitBtn.setOnClickListener( new View.OnClickListener() {
                        public void onClick( View v )
                        {
                                Intent intent = new Intent( getApplicationContext(), kr.co.ultari.atsmart.basic.view.ChatWindow.class );
                                intent.putExtra( "roomId", roomId );
                                intent.putExtra( "userIds", ids );
                                intent.putExtra( "userNames", explain );
                                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( intent );
                                finish();
                        }
                } );
                inviteBtn = ( Button ) findViewById( R.id.inviteBtn );
                inviteBtn.setTypeface( Define.tfRegular );
                inviteBtn.setOnClickListener( new View.OnClickListener() {
                        public void onClick( View v )
                        {
                                Define.oldRoomUserId = ids;
                                Define.oldRoomUserName = explain;
                                Define.oldRoomId = roomId;
                                Define.isAddUserMode = true;
                                Intent intent = new Intent( CustomDialog.this, kr.co.ultari.atsmart.basic.view.GroupSearchView.class );
                                intent.putExtra( "userIds", ids );
                                intent.putExtra( "userNames", explain );
                                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( intent );
                                finish();
                        }
                } );
        }

        @Override
        public boolean onKeyDown( int keyCode, KeyEvent event )
        {
                if ( event.getAction() == KeyEvent.ACTION_DOWN )
                {
                        if ( keyCode == KeyEvent.KEYCODE_BACK )
                        {
                                Intent intent = new Intent( getApplicationContext(), kr.co.ultari.atsmart.basic.view.ChatWindow.class );
                                intent.putExtra( "roomId", roomId );
                                intent.putExtra( "userIds", ids );
                                intent.putExtra( "userNames", explain );
                                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                                startActivity( intent );
                                finish();
                                return true;
                        }
                }
                return super.onKeyDown( keyCode, event );
        }
        @SuppressLint( "HandlerLeak" )
        public Handler customHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == 0 )
                                {
                                        String[] tmp = (( String ) msg.obj).split( "#" );
                                        String[] id = ids.split( "," );
                                        String[] name = explain.split( "," );
                                        for ( int i = 1; i < tmp.length; i++ )
                                        {
                                                String[] arr = tmp[i].split( ":" );
                                                if ( arr[1].equals( "ON" ) ) list.add( new GroupUser( id[i - 1], name[i - 1], R.drawable.icon_mobile_on,
                                                                getString( R.string.mobileOn ) ) );
                                                else if ( arr[1].equals( "OFF" ) ) list.add( new GroupUser( id[i - 1], name[i - 1], R.drawable.icon_mobile_off,
                                                                getString( R.string.mobileOff ) ) );
                                        }
                                        adapter.refresh();
                                }
                                else
                                {
                                        super.handleMessage( msg );
                                }
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
                        }
                }
        };

        @Override
        public void run()
        {
                Log.v( TAG, "run()" );
                UltariSSLSocket sc = null;
                InputStreamReader ir = null;
                BufferedReader br = null;
                PrintWriter pw = null;
                try
                {
                        sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                        .getServerPort( Define.mContext ) ) );
                        ir = new InputStreamReader( sc.getInputStream() );
                        br = new BufferedReader( ir );
                        pw = new PrintWriter( sc.getWriter(), false );
                        pw.print( getSendString( "USERSTATUS\t" + ids ) );
                        pw.flush();
                        char[] buf = new char[1024];
                        int rcv = br.read( buf, 0, 1023 );
                        String tmp = new String( buf, 0, rcv );
                        tmp = tmp.substring( 0, tmp.length() - 1 );
                        String result = codec.DecryptSEED( tmp );
                        Message m = customHandler.obtainMessage( 0, null );
                        m.obj = ( Object ) result;
                        customHandler.sendMessage( m );
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
                        if ( pw != null )
                        {
                                try
                                {
                                        pw.close();
                                        pw = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                }
        }

        public String getSendString( String msg )
        {
                msg.replaceAll( "\f", "" );
                msg = codec.EncryptSEED( msg );
                msg += "\f";
                return msg;
        }
}