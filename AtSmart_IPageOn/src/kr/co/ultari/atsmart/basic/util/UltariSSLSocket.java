package kr.co.ultari.atsmart.basic.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.KeyStore;
import java.util.ArrayList;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;

public class UltariSSLSocket extends Thread {
        Context context = null;
        SSLSocket sslSocket = null;
        Socket sc = null;
        ArrayList<String> sendAr = null;
        OutputStreamWriter ow = null;
        BufferedWriter bw = null;
        public static KeyStore keystore = null;
        public static TrustManagerFactory tmf = null;
        public static KeyManagerFactory kmf = null;
        private int randomIpIndex = -1;
        private static final String TAG = "UltariSSLSocket";

        public UltariSSLSocket( Context context, String ipAddress, int portNumber ) throws Exception
        {
                this.context = context;
                if ( Define.isSSL )
                {
                        String ip = ipAddress;
                        char[] passphrase = "ultari".toCharArray();
                        
                        int bks_version;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            bks_version = R.raw.android; //The BKS file
                        } else {
                            bks_version = R.raw.android_v1; //The BKS (v-1) file
                        }
                        
                        if ( keystore == null )
                        {
                                keystore = KeyStore.getInstance( "BKS" );
                                InputStream is = null;
                                try
                                {
                                        is = context.getResources().openRawResource(bks_version);
                                        //is = context.getResources().openRawResource(R.raw.android);
                                        keystore.load( is, passphrase );
                                }
                                finally
                                {
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
                                }
                        }
                        if ( tmf == null )
                        {
                                tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
                                tmf.init( keystore );
                        }
                        if ( kmf == null )
                        {
                                kmf = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
                                kmf.init( keystore, passphrase );
                        }
                        SSLContext sslContext = SSLContext.getInstance( "TLSv1.2" );
                        sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
                        SSLSocketFactory sf = sslContext.getSocketFactory();
                        String nowIp = getSingleIpFromCommaString( ip );
                        int loop = 0;
                        while ( loop < getCountCommnaIp( ip ) )
                        {
                                loop++;
                                try
                                {
                                        //Log.d( TAG, "Connect to " + nowIp );
                                        sslSocket = ( SSLSocket ) sf.createSocket();
                                        sslSocket.connect( new InetSocketAddress( nowIp, portNumber ), 5000 );
                                        sendAr = new ArrayList<String>();
                                        sslSocket.startHandshake();
                                        ow = new OutputStreamWriter( sslSocket.getOutputStream() );
                                        bw = new BufferedWriter( ow );
                                        //Log.d( TAG, "Connected to " + nowIp );
                                        break;
                                }
                                catch ( ConnectException e )
                                {
                                        nowIp = getNextIpFromCommaString( ip );
                                        if ( sslSocket != null )
                                        {
                                                sslSocket.close();
                                                sslSocket = null;
                                                
                                                ow.close(); 
                                                bw.close(); 
                                        }
                                        if ( nowIp == null ) throw new Exception( "Connot connect to server" );
                                }
                        }
                        this.start();
                }
                else
                {
                        sc = new Socket( ipAddress, portNumber );
                        bw = new BufferedWriter( new OutputStreamWriter( sc.getOutputStream() ) );
                }
        }

        private int getCountCommnaIp( String commaString )
        {
                if ( commaString.indexOf( ',' ) < 0 ) return 1;
                else
                {
                        String[] str = commaString.split( "," );
                        return str.length;
                }
        }

        private synchronized String getSingleIpFromCommaString( String commaString )
        {
                if ( commaString.indexOf( ',' ) < 0 ) return commaString;
                else
                {
                        String[] str = commaString.split( "," );
                        if ( randomIpIndex >= 0 )
                        {
                                if ( randomIpIndex >= str.length ) randomIpIndex = 0;
                                return str[randomIpIndex];
                        }
                        else
                        {
                                int randomNumber = ( int ) (Math.random() * 10);
                                randomIpIndex = randomNumber % str.length;
                                //Log.d( TAG, "Randomindex : " + randomIpIndex );
                                return str[randomIpIndex];
                        }
                }
        }

        private synchronized String getNextIpFromCommaString( String commaString )
        {
                if ( commaString.indexOf( ',' ) < 0 ) return null;
                else
                {
                        String[] str = commaString.split( "," );
                        randomIpIndex++;
                        if ( randomIpIndex >= str.length ) randomIpIndex = 0;
                        //Log.d( TAG, "next Randomindex : " + randomIpIndex );
                        return str[randomIpIndex];
                }
        }

        public BufferedWriter getWriter()
        {
                return bw;
        }

        public void send( String msg )
        {
                if ( Define.isSSL )
                {
                        synchronized ( sendAr )
                        {
                                sendAr.add( msg );
                        }
                }
                else
                {
                        try
                        {
                                bw.write( msg );
                                bw.flush();
                        }
                        catch ( Exception e )
                        {}
                }
        }

        public void run()
        {
                try
                {
                        if ( Define.isSSL )
                        {
                                while ( sslSocket != null && sslSocket.isConnected() )
                                {
                                        if ( sendAr.size() > 0 )
                                        {
                                                String msg = null;
                                                synchronized ( sendAr )
                                                {
                                                        msg = sendAr.remove( 0 );
                                                }
                                                if ( msg.equals( "[CLOSESOCKET]" ) )
                                                {
                                                        sslSocket.close();
                                                        sslSocket = null;
                                                        
                                                        ow.close();
                                                        bw.close();
                                                        
                                                        return;
                                                }
                                                else
                                                {
                                                        bw.write( msg );
                                                        bw.flush();
                                                }
                                        }
                                        else
                                        {
                                                sleep( 10 );
                                        }
                                }
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public void setSoTimeout( int milli )
        {
                try
                {
                        if ( Define.isSSL ) sslSocket.setSoTimeout( milli );
                        else sc.setSoTimeout( milli );
                }
                catch ( Exception e )
                {}
        }

        public InputStream getInputStream() throws IOException
        {
                if ( Define.isSSL ) return sslSocket.getInputStream();
                else return sc.getInputStream();
        }

        public void close()
        {
                if ( Define.isSSL )
                {
                        if ( sslSocket != null )
                        {
                                try
                                {
                                        sslSocket.shutdownInput();
                                }
                                catch ( Exception e )
                                {}
                                try
                                {
                                        sslSocket.shutdownOutput();
                                }
                                catch ( Exception e )
                                {}
                                try
                                {
                                        sslSocket.close();
                                        sslSocket = null;
                                }
                                catch ( Exception e )
                                {}
                        }
                        if ( bw != null )
                        {
                                try
                                {
                                        bw.close();
                                        bw = null;
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
                }
                else
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
                        if ( bw != null )
                        {
                                try
                                {
                                        bw.close();
                                        bw = null;
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
                }
        }
}
