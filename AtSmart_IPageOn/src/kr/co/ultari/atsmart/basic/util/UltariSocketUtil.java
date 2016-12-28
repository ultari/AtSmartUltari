package kr.co.ultari.atsmart.basic.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import kr.co.ultari.atsmart.basic.Define;

public class UltariSocketUtil {
        private static int randomProxyIpIndex = -1;

        public static Socket getProxySocket() throws Exception
        {
                String ip = Define.getProxyIp( Define.mContext );
                String port = Define.getProxyPort( Define.mContext );
                String nowIp = getSingleIpFromCommaString( ip );
                Socket sc = null;
                while ( true )
                {
                        try
                        {
                                sc = new Socket();
                                sc.connect( new InetSocketAddress( nowIp, Integer.parseInt( port ) ), 2000 );
                                break;
                        }
                        catch ( Exception e )
                        {
                                nowIp = getNextIpFromCommaString( ip );
                                if ( nowIp == null ) throw new Exception( "Connot connect to server" );
                        }
                }
                return sc;
        }

        private static String getSingleIpFromCommaString( String commaString )
        {
                if ( commaString.indexOf( ',' ) < 0 ) return commaString;
                else
                {
                        String[] str = commaString.split( "," );
                        if ( randomProxyIpIndex >= 0 )
                        {
                                if ( randomProxyIpIndex >= str.length ) randomProxyIpIndex = 0;
                                return str[randomProxyIpIndex];
                        }
                        else
                        {
                                int randomNumber = ( int ) (Math.random() * 10);
                                randomProxyIpIndex = randomNumber % str.length;
                                return str[randomProxyIpIndex];
                        }
                }
        }

        private static String getNextIpFromCommaString( String commaString )
        {
                if ( commaString.indexOf( ',' ) < 0 ) return null;
                else
                {
                        String[] str = commaString.split( "," );
                        randomProxyIpIndex++;
                        if ( randomProxyIpIndex >= str.length ) randomProxyIpIndex = 0;
                        return str[randomProxyIpIndex];
                }
        }

        public synchronized static byte[] getProxyContent( String uri ) throws Exception
        {
                byte[] buffer = new byte[4096];
                int rcv = -1;
                byte[] rcvByteArray = null;
                StringBuffer gotHeader = new StringBuffer();
                Socket sc = null;
                InputStream is = null;
                OutputStream os = null;
                OutputStreamWriter ow = null;
                ByteArrayOutputStream bo = null;
                try
                {
                        sc = getProxySocket();
                        is = sc.getInputStream();
                        os = sc.getOutputStream();
                        ow = new OutputStreamWriter( os, "EUC-KR" );
                        bo = new ByteArrayOutputStream();
                        String header = "GET " + uri + " HTTP/1.1\r\n\r\n";
                        ow.write( header );
                        ow.flush();
                        while ( (rcv = is.read( buffer, 0, 1024 )) >= 0 )
                        {
                                String s = new String( buffer, 0, rcv, "EUC-KR" );
                                gotHeader.append( s );
                                int pos = -1;
                                if ( (pos = gotHeader.indexOf( "\r\n\r\n" )) >= 0 )
                                {
                                        pos += 4;
                                        if ( pos < rcv )
                                        {
                                                bo.write( buffer, pos, rcv - pos );
                                        }
                                        break;
                                }
                        }
                        while ( (rcv = is.read( buffer, 0, 1024 )) >= 0 )
                        {
                                bo.write( buffer, 0, rcv );
                        }
                        bo.flush();
                        rcvByteArray = bo.toByteArray();
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                        rcvByteArray = null;
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
                return rcvByteArray;
        }

        public synchronized static Bitmap getUserImage( String userId )
        {
                return getUserImage( userId, 0, 0 );
        }

        public static Bitmap getUserImage( String userId, int maxWidth, int maxHeight )
        {
                if ( userId == null ) return null;
                Bitmap picture = Define.getBitmap( userId );
                if ( picture != null ) return picture;
                byte[] buffer = new byte[4096];
                int rcv = -1;
                byte[] rcvByteArray = null;
                StringBuffer gotHeader = new StringBuffer();
                Socket sc = null;
                InputStream is = null;
                OutputStream os = null;
                OutputStreamWriter ow = null;
                ByteArrayOutputStream bo = null;
                try
                {
                        sc = getProxySocket();
                        is = sc.getInputStream();
                        os = sc.getOutputStream();
                        ow = new OutputStreamWriter( os, "EUC-KR" );
                        bo = new ByteArrayOutputStream();
                        String header = "GET /" + userId + " HTTP/1.1\r\n\r\n";
                        ow.write( header );
                        ow.flush();
                        while ( (rcv = is.read( buffer, 0, 1024 )) >= 0 )
                        {
                                String s = new String( buffer, 0, rcv, "EUC-KR" );
                                gotHeader.append( s );
                                int pos = -1;
                                if ( (pos = gotHeader.indexOf( "\r\n\r\n" )) >= 0 )
                                {
                                        pos += 4;
                                        if ( pos < rcv )
                                        {
                                                bo.write( buffer, pos, rcv - pos );
                                        }
                                        break;
                                }
                        }
                        while ( (rcv = is.read( buffer, 0, 1024 )) >= 0 )
                        {
                                bo.write( buffer, 0, rcv );
                        }
                        bo.flush();
                        rcvByteArray = bo.toByteArray();
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                        rcvByteArray = null;
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
                if ( rcvByteArray != null )
                {
                        BufferedInputStream bi = null;
                        ByteArrayInputStream ba = null;
                        try
                        {
                                ba = new ByteArrayInputStream( rcvByteArray );
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                if ( maxWidth != 0 && maxHeight != 0 )
                                {
                                        bi = new BufferedInputStream( ba );
                                        options.inJustDecodeBounds = true;
                                        picture = BitmapFactory.decodeStream( bi, null, options );
                                        if ( options.outHeight * options.outWidth >= maxWidth * maxHeight )
                                        {
                                                if ( options.outHeight > maxHeight ) options.inSampleSize = ( int ) Math.pow(
                                                                2,
                                                                ( int ) Math.round( Math.log( maxHeight
                                                                                / ( double ) Math.max( options.outHeight, options.outWidth ) )
                                                                                / Math.log( 0.5 ) ) );
                                                else if ( options.outWidth > maxWidth ) options.inSampleSize = ( int ) Math.pow(
                                                                2,
                                                                ( int ) Math.round( Math.log( maxWidth
                                                                                / ( double ) Math.max( options.outHeight, options.outWidth ) )
                                                                                / Math.log( 0.5 ) ) );
                                        }
                                        else
                                        {
                                                options.inSampleSize = 1;
                                        }
                                        if ( bi != null )
                                        {
                                                try
                                                {
                                                        bi.close();
                                                        bi = null;
                                                }
                                                catch ( Exception e )
                                                {}
                                        }
                                        if ( ba != null )
                                        {
                                                try
                                                {
                                                        ba.close();
                                                        ba = null;
                                                }
                                                catch ( Exception e )
                                                {}
                                        }
                                        ba = new ByteArrayInputStream( rcvByteArray );
                                }
                                options.inJustDecodeBounds = false;
                                bi = new BufferedInputStream( ba, rcvByteArray.length );
                                picture = BitmapFactory.decodeStream( bi, null, options );
                                Define.setBitmap( userId, picture );
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                        finally
                        {
                                if ( bi != null )
                                {
                                        try
                                        {
                                                bi.close();
                                                bi = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                                if ( ba != null )
                                {
                                        try
                                        {
                                                ba.close();
                                                ba = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                        }
                }
                return picture;
        }
}
