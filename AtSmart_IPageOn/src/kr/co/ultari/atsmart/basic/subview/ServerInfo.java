package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class ServerInfo extends MessengerActivity implements OnClickListener {
        private EditText etPublicServerIp, etPublicServerPort, etPublicProxyIp, etPublicProxyPort;
        private EditText etPrivateServerIp, etPrivateServerPort, etPrivateProxyIp, etPrivateProxyPort;
        private Button btnOk, btnCancel;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.server_info );
                try
                {
                        etPublicServerIp = ( EditText ) findViewById( R.id.publicServerIp );
                        etPublicServerPort = ( EditText ) findViewById( R.id.publicServerPort );
                        etPublicProxyIp = ( EditText ) findViewById( R.id.publicProxyIp );
                        etPublicProxyPort = ( EditText ) findViewById( R.id.publicProxyPort );
                        etPrivateServerIp = ( EditText ) findViewById( R.id.privateServerIp );
                        etPrivateServerPort = ( EditText ) findViewById( R.id.privateServerPort );
                        etPrivateProxyIp = ( EditText ) findViewById( R.id.privateProxyIp );
                        etPrivateProxyPort = ( EditText ) findViewById( R.id.privateProxyPort );
                        etPublicServerIp.setText( Define.PUBLIC_SERVER_IP );
                        etPublicServerPort.setText( Define.PUBLIC_SERVER_PORT );
                        etPublicProxyIp.setText( Define.PUBLIC_PROXY_IP );
                        etPublicProxyPort.setText( Define.PUBLIC_PROXY_PORT );
                        etPrivateServerIp.setText( Define.PRIVATE_SERVER_IP );
                        etPrivateServerPort.setText( Define.PRIVATE_SERVER_PORT );
                        etPrivateProxyIp.setText( Define.PRIVATE_PROXY_IP );
                        etPrivateProxyPort.setText( Define.PRIVATE_PROXY_PORT );
                        btnOk = ( Button ) findViewById( R.id.btnServerSave );
                        btnCancel = ( Button ) findViewById( R.id.btnServerCancel );
                        btnOk.setOnClickListener( this );
                        btnCancel.setOnClickListener( this );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void hideKeyboard()
        {
                InputMethodManager imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                imm.hideSoftInputFromWindow( etPublicServerIp.getWindowToken(), 0 );
                imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                imm.hideSoftInputFromWindow( etPublicServerPort.getWindowToken(), 0 );
                imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                imm.hideSoftInputFromWindow( etPublicProxyIp.getWindowToken(), 0 );
                imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                imm.hideSoftInputFromWindow( etPublicProxyPort.getWindowToken(), 0 );
                imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                imm.hideSoftInputFromWindow( etPrivateServerIp.getWindowToken(), 0 );
                imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                imm.hideSoftInputFromWindow( etPrivateServerPort.getWindowToken(), 0 );
                imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                imm.hideSoftInputFromWindow( etPrivateProxyIp.getWindowToken(), 0 );
                imm = ( InputMethodManager ) getSystemService( Activity.INPUT_METHOD_SERVICE );
                imm.hideSoftInputFromWindow( etPrivateProxyPort.getWindowToken(), 0 );
        }

        public void onClick( View view )
        {
                this.hideKeyboard();
                if ( btnOk == view )
                {
                        setData();
                        finish();
                }
                else if ( btnCancel == view ) finish();
        }

        public void setData()
        {
                Define.PUBLIC_SERVER_IP = etPublicServerIp.getText().toString();
                Define.PUBLIC_SERVER_PORT = etPublicServerPort.getText().toString();
                Define.PUBLIC_PROXY_IP = etPublicProxyIp.getText().toString();
                Define.PUBLIC_PROXY_PORT = etPublicProxyPort.getText().toString();
                Define.PRIVATE_SERVER_IP = etPrivateServerIp.getText().toString();
                Define.PRIVATE_SERVER_PORT = etPrivateServerPort.getText().toString();
                Define.PRIVATE_PROXY_IP = etPrivateProxyIp.getText().toString();
                Define.PRIVATE_PROXY_PORT = etPrivateProxyPort.getText().toString();
                Database.instance( Define.mContext ).updateConfig( "PUBLIC_SERVER_IP", Define.PUBLIC_SERVER_IP );
                Database.instance( Define.mContext ).updateConfig( "PUBLIC_SERVER_PORT", Define.PUBLIC_SERVER_PORT );
                Database.instance( Define.mContext ).updateConfig( "PUBLIC_PROXY_IP", Define.PUBLIC_PROXY_IP );
                Database.instance( Define.mContext ).updateConfig( "PUBLIC_PROXY_PORT", Define.PUBLIC_PROXY_PORT );
                Database.instance( Define.mContext ).updateConfig( "PRIVATE_SERVER_IP", Define.PRIVATE_SERVER_IP );
                Database.instance( Define.mContext ).updateConfig( "PRIVATE_SERVER_PORT", Define.PRIVATE_SERVER_PORT );
                Database.instance( Define.mContext ).updateConfig( "PRIVATE_PROXY_IP", Define.PRIVATE_PROXY_IP );
                Database.instance( Define.mContext ).updateConfig( "PRIVATE_PROXY_PORT", Define.PRIVATE_PROXY_PORT );
        }
        private static final String TAG = "/AtSmart/ServerInfo";

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
