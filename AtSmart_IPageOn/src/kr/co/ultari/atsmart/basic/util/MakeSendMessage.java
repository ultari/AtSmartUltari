package kr.co.ultari.atsmart.basic.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Vector;
import android.content.Context;
import android.os.Environment;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;

public class MakeSendMessage {
        private String senderId = null;
        private String senderName = null;
        private String message = null;
        private String subject = null;
        private Vector<File> ar = null;
        private File outFile = null;
        private File dir = null;
        private AmCodec m_codec = null;
        private String sendDate = null;
        private Context mContext;

        public MakeSendMessage( Context context, String receiveId, String senderId, String senderName, String subject, String message, Vector<File> ar,
                        String baseDir )
        {
                mContext = context;
                String path = baseDir;
                Calendar cal = Calendar.getInstance();
                File outFile = new File( Environment.getExternalStoragePublicDirectory( android.os.Environment.DIRECTORY_DOWNLOADS ) + File.separator
                                + cal.getTimeInMillis() );
                ar = new Vector<File>();
                try
                {
                        this.senderId = new String( senderId.getBytes(), "EUC-KR" );
                        this.message = message;
                        this.senderName = senderName;
                        this.subject = subject;
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                Vector<File> fileAr = new Vector<File>();
                this.outFile = outFile;
                this.m_codec = new AmCodec();
        }

        public String getSendDate()
        {
                return this.sendDate;
        }

        public String makeSendFile()
        {
                FileOutputStream fo = null;
                OutputStreamWriter ow = null;
                try
                {
                        String rtfData = makeRTFData( message );
                        StringBuffer header = new StringBuffer();
                        header.append( senderId );
                        header.append( "\n" );
                        header.append( senderName );
                        header.append( "\t" );
                        header.append( getMemoTimeStamp() );
                        header.append( "\t" );
                        header.append( rtfData.length() + "" );
                        header.append( "\t" + subject + "{{{TACSS}}}" );
                        header.append( message );
                        header.append( "\t" );
                        if ( ar != null )
                        {
                                for ( int i = 0; i < ar.size(); i++ )
                                {
                                        if ( i > 0 ) header.append( '\013' + "" );
                                        try
                                        {
                                                String fileName = ar.get( i ).getName();
                                                String fileSize = ar.get( i ).length() + "";
                                                header.append( fileName );
                                                header.append( "\n" );
                                                header.append( fileSize + "" );
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                        }
                        header.append( "\f" );
                        String headerSize = header.toString().getBytes( "EUC-KR" ).length + "";
                        header.append( rtfData );
                        while ( headerSize.length() < 16 )
                                headerSize = "0" + headerSize;
                        fo = new FileOutputStream( outFile + ".dec" );
                        ow = new OutputStreamWriter( fo, "EUC-KR" );
                        ow.write( headerSize );
                        ow.flush();
                        ow.write( header.toString() );
                        ow.flush();
                        byte[] buf = new byte[4096];
                        int rcv = 0;
                        if ( ar != null )
                        {
                                for ( int i = 0; i < ar.size(); i++ )
                                {
                                        FileInputStream fi = null;
                                        try
                                        {
                                                fi = new FileInputStream( ar.get( i ) );
                                                while ( (rcv = fi.read( buf, 0, 4096 )) >= 0 )
                                                {
                                                        fo.write( buf, 0, rcv );
                                                        fo.flush();
                                                }
                                        }
                                        catch ( Exception e )
                                        {
                                                e.printStackTrace();
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
                                        }
                                }
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                        return "";
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
                FileInputStream fi = null;
                ow = null;
                fo = null;
                try
                {
                        fi = new FileInputStream( outFile + ".dec" );
                        fo = new FileOutputStream( outFile );
                        ow = new OutputStreamWriter( fo, "EUC-KR" );
                        byte[] buf = new byte[2048];
                        int rcv = 0;
                        while ( (rcv = fi.read( buf, 0, 2048 )) >= 0 )
                        {
                                ow.write( m_codec.EncryptSEED( buf, 0, rcv ) );
                                ow.flush();
                        }
                        File f = new File( Environment.getExternalStoragePublicDirectory( android.os.Environment.DIRECTORY_DOWNLOADS ) + File.separator
                                        + outFile.getName() + ".dec" );
                        if ( f.exists() ) f.delete();
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
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
                return this.outFile.getName();
        }

        public String makeRTFData( String content )
        {
                String rtfHeader = "";
                String rtfContnet = "";
                String rtfFooter = "";
                String rtfResult = "";
                rtfHeader += "{\\rtf1\\ansi\\ansicpg949\\deff0\\deflang1033\\deflangfe1042";
                rtfHeader += "{\\fonttbl{\\f0\\fnil\\fcharset129 \\'b1\\'bc\\'b8\\'b2;}}\r\n\r\n";
                rtfHeader += "{\\colortbl ;\\red0\\green0\\blue0;}\r\n\r\n";
                rtfHeader += "\\viewkind4\\uc1\\pard\\cf1\\lang1042\\f0\\fs20 ";
                rtfContnet = quotedPrintableEncoding( content );
                rtfFooter += "\\par\r\n}\r\n";
                rtfResult += rtfHeader;
                rtfResult += rtfContnet;
                rtfResult += rtfFooter;
                return rtfResult;
        }

        public String getMemoTimeStamp()
        {
                Calendar cal = Calendar.getInstance();
                String timeStamp = "";
                timeStamp += formatString( cal.get( Calendar.YEAR ), 4 );
                timeStamp += mContext.getString( R.string.year ) + " ";
                timeStamp += formatString( cal.get( Calendar.MONTH ) + 1, 2 );
                timeStamp += mContext.getString( R.string.month ) + " ";
                timeStamp += formatString( cal.get( Calendar.DAY_OF_MONTH ), 2 );
                timeStamp += mContext.getString( R.string.day ) + " ";
                timeStamp += formatString( cal.get( Calendar.HOUR_OF_DAY ), 2 );
                timeStamp += ":";
                timeStamp += formatString( cal.get( Calendar.MINUTE ), 2 );
                timeStamp += ":";
                timeStamp += formatString( cal.get( Calendar.SECOND ), 2 );
                this.sendDate = timeStamp;
                return timeStamp;
        }

        public String formatString( int i, int l )
        {
                String ret = i + "";
                while ( ret.length() < l )
                {
                        ret = "0" + ret;
                }
                return ret;
        }

        public String quotedPrintableEncoding( String content )
        {
                StringBuilder outData = new StringBuilder();
                try
                {
                        byte[] rawData = content.getBytes( "EUC_KR" );
                        for ( int i = 0; i < rawData.length; i++ )
                        {
                                int temp = (0xFF & rawData[i]);
                                if ( temp > 128 )
                                {
                                        String append = "\\'" + String.format( "%x", temp );
                                        outData.append( append );
                                }
                                else
                                {
                                        outData.append( String.format( "%c", temp ) );
                                }
                        }
                        return outData.toString();
                }
                catch ( UnsupportedEncodingException e )
                {
                        e.printStackTrace();
                        return content;
                }
        }

        public String getFilePath( String userId )
        {
                String dirPath = "";
                if ( userId == null ) return dirPath;
                if ( userId.length() > 2 )
                {
                        dirPath += File.separator;
                        dirPath += userId.substring( 0, 2 );
                }
                if ( userId.length() > 4 )
                {
                        dirPath += File.separator;
                        dirPath += userId.substring( 2, 4 );
                }
                if ( userId.length() > 6 )
                {
                        dirPath += File.separator;
                        dirPath += userId.substring( 4, 6 );
                }
                dirPath += File.separator;
                dirPath += userId;
                return dirPath;
        }

        public String formatSize( int size, int length )
        {
                StringBuffer sb = new StringBuffer();
                for ( int i = 0; i < length - size; i++ )
                {
                        sb.append( "0" );
                }
                sb.append( "" + size );
                return sb.toString();
        }

        public String getAttachFileContent( String filePath ) throws Exception
        {
                FileInputStream fis = null;
                try
                {
                        fis = new FileInputStream( filePath );
                        int rcv = 0;
                        byte[] buf = new byte[1024];
                        StringBuffer sb = new StringBuffer();
                        while ( (fis.read( buf )) != -1 )
                        {
                                sb.append( new String( buf ) );
                        }
                        return sb.toString();
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                finally
                {
                        fis.close();
                }
                return "";
        }

        public String getFileSize( String filePath ) throws IOException
        {
                FileInputStream fis = null;
                try
                {
                        fis = new FileInputStream( filePath );
                        int totalSize = 0;
                        int rcv = 0;
                        byte[] buf = new byte[1024];
                        while ( (rcv = fis.read( buf )) != -1 )
                        {
                                if ( rcv == -1 ) continue;
                                totalSize += rcv;
                        }
                        return "" + totalSize;
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                finally
                {
                        fis.close();
                }
                return "";
        }
}
