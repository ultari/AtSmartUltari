package kr.co.ultari.atsmart.basic.subview;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.GcmManager;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import kr.co.ultari.atsmart.basic.view.BuddyView;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import kr.co.ultari.atsmart.basic.view.OrganizationView;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class NameLoginDialog extends MessengerActivity implements Runnable {
        private String mPassword = "";
        private String mRegId = "";
        private String mValue = "";
        private String mName = "";
        private String mNickName = "";
        private String mId = "";
        private Thread mThread;
        private ArrayAdapter<String> mAdapter;
        private ListView mList;
        private ArrayList<String> mArray;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.dialog );
                mPassword = getIntent().getStringExtra( "pass" );
                mRegId = getIntent().getStringExtra( "regid" );
                mValue = getIntent().getStringExtra( "value" );
                mName = getIntent().getStringExtra( "name" );
                String[] datas = mName.split( "\t" );
                mArray = new ArrayList<String>();
                for ( int i = 0; i < datas.length; i++ )
                {
                        mArray.add( datas[i] );
                        datas[i] = (datas[i].substring( datas[i].indexOf( "," ) + 1 )).replace( ",", "\n" + getString( R.string.mail ) + ":\t" );
                }
                (( TextView ) findViewById( R.id.custom_title2 )).setText( getString( R.string.userchoice ) );
                mList = ( ListView ) findViewById( R.id.custom_list );
                mAdapter = new ArrayAdapter<String>( this, R.layout.nameitem, datas );
                mList.setAdapter( mAdapter );
                mList.setOnItemClickListener( onClickListItem );
        }
        private OnItemClickListener onClickListItem = new OnItemClickListener() {
                @Override
                public void onItemClick( AdapterView<?> arg0, View arg1, int arg2, long arg3 )
                {
                        String findData = mAdapter.getItem( arg2 ).replace( "\n" + getString( R.string.mail ) + ":\t", "," );
                        for ( int i = 0; i < mArray.size(); i++ )
                        {
                                if ( mArray.get( i ).indexOf( findData ) >= 0 )
                                {
                                        mId = mArray.get( i ).substring( 0, mArray.get( i ).indexOf( "," ) );
                                        break;
                                }
                        }
                        mThread = new Thread( NameLoginDialog.this );
                        mThread.start();
                }
        };

        @Override
        protected void onNewIntent( Intent intent )
        {
                super.onNewIntent( intent );
                setIntent( intent );
        }
        public Handler dialogHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_NO_USER )
                                {
                                        Message m = MainActivity.mainHandler.obtainMessage( Define.AM_NO_USER, null );
                                        MainActivity.mainHandler.sendMessage( m );
                                        finish();
                                }
                                else if ( msg.what == Define.AM_NO_PASSWORD )
                                {
                                        Message m = MainActivity.mainHandler.obtainMessage( Define.AM_NO_PASSWORD, null );
                                        MainActivity.mainHandler.sendMessage( m );
                                        finish();
                                }
                                else if ( msg.what == Define.AM_LOGIN )
                                {
                                        Thread.sleep( 100 );
                                        Message m = MainActivity.mainHandler.obtainMessage( Define.AM_SELECT_TAB );
                                        m.arg1 = Define.TAB_KEYPAD;
                                        MainActivity.mainHandler.sendMessage( m );
                                        if ( mValue.equals( "0" ) ) Database.instance( getApplicationContext() ).updateConfig( "RETAIN", "N" );
                                        else Database.instance( getApplicationContext() ).updateConfig( "RETAIN", "Y" );
                                        BuddyView.instance().reloadBuddy();
                                        startServiceMobile( mId, mPassword );
                                        /*
                                         * Message message = MainActivity.mainHandler.obtainMessage( Define.AM_NAME );
                                         * message.arg1 = Define.TAB_USER_BUDDY;
                                         * MainActivity.mainHandler.sendMessage( message );
                                         */
                                        OrganizationView.instance().startProcess();
                                        finish();
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };

        public void startServiceMobile( String _id, String _pw )
        {
                try
                {
                        Intent sendIntent = new Intent( Define.MSG_RESTART_SERVICE );
                        sendIntent.putExtra( "USERID", _id.toString() );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        sendBroadcast( sendIntent );
                }
                catch ( Exception e )
                {
                        MainActivity.Instance().EXCEPTION( e );
                }
        }

        @Override
        public void run()
        {
                UltariSSLSocket sc = null;
                InputStreamReader ir = null;
                BufferedReader br = null;
                PrintWriter pw = null;
                AmCodec codec = new AmCodec();
                try
                {
                        // 2015-03-10
                        sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                        .getServerPort( Define.mContext ) ) );
                        ir = new InputStreamReader( sc.getInputStream() );
                        br = new BufferedReader( ir );
                        pw = new PrintWriter( sc.getWriter() );
                        pw.print( codec.EncryptSEED( "Login\t" + mId + "\t" + mPassword + "\t" + mRegId + "\tAndroid\t" + mValue ) + "\f" );
                        pw.flush();
                        char[] buf = new char[2048];
                        int rcv = 0;
                        String sb;
                        while ( (rcv = br.read( buf, 0, 2048 )) >= 0 )
                        {
                                sb = new String( buf, 0, rcv );
                                if ( sb.indexOf( "\f" ) >= 0 )
                                {
                                        sb = sb.substring( 0, sb.length() - 1 );
                                        String rcvStr = codec.DecryptSEED( sb );
                                        if ( rcvStr.indexOf( "NoUser" ) == 0 )
                                        {
                                                Message m = dialogHandler.obtainMessage( Define.AM_NO_USER, null );
                                                dialogHandler.sendMessage( m );
                                                break;
                                        }
                                        else if ( rcvStr.indexOf( "NoPassword" ) == 0 )
                                        {
                                                Message m = dialogHandler.obtainMessage( Define.AM_NO_PASSWORD, null );
                                                dialogHandler.sendMessage( m );
                                                break;
                                        }
                                        else if ( rcvStr.indexOf( "Passport" ) == 0 )
                                        {
                                                rcvStr = rcvStr.substring( 9 );
                                                if ( rcvStr.indexOf( "\t" ) < 0 )
                                                {
                                                        mName = rcvStr;
                                                        mNickName = "";
                                                }
                                                else
                                                {
                                                        mName = rcvStr.substring( 0, rcvStr.indexOf( '\t' ) );
                                                        mNickName = rcvStr.substring( rcvStr.indexOf( '\t' ) + 1 );
                                                }
                                                Database.instance( getApplicationContext() ).updateConfig( "USERID", mId );
                                                Database.instance( getApplicationContext() ).updateConfig( "USERPASSWORD", mPassword );
                                                Database.instance( getApplicationContext() ).updateConfig( "USERNAME", StringUtil.getNamePosition( mName ) );
                                                Database.instance( getApplicationContext() ).updateConfig( "USERNICKNAME", mNickName );
                                                Define.setMyId( mId );
                                                Define.setMyPW( mPassword );
                                                try
                                                {
                                                        if ( Define.useGcmPush )
                                                        {
                                                                if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH )
                                                                {
                                                                	Define.regid = "NoC2dm";
                                                                	android.util.Log.d( "AtSmart", "GCM regId:" + Define.regid );
                                                                }
                                                                else
                                                                {
                                                                        if ( Define.regid == null || Define.regid.equals( "" ) )
                                                                        {
                                                                                GcmManager gcm = new GcmManager( Define.mContext );
                                                                                Define.regid = gcm.getPhoneRegistrationId();
                                                                                android.util.Log.d( "AtSmart", "GCM regId:" + Define.regid );
                                                                        }
                                                                }
                                                        }
                                                        else
                                                        {
                                                                Define.regid = "NoC2dm";
                                                                android.util.Log.d( "AtSmart", "GCM regId:" + Define.regid );
                                                        }
                                                }
                                                catch ( Exception e )
                                                {
                                                        e.printStackTrace();
                                                        Define.regid = "NoC2dm";
                                                        android.util.Log.d( "AtSmart", "GCM regId:" + Define.regid );
                                                }
                                                Message m = dialogHandler.obtainMessage( Define.AM_LOGIN, null );
                                                dialogHandler.sendMessage( m );
                                                break;
                                        }
                                }
                        }
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
}
