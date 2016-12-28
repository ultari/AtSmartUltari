package kr.co.ultari.atsmart.basic.subdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.URLEncoder;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import kr.co.ultari.atsmart.basic.view.ChatWindow;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class ChatData implements Runnable {
        public String msgId;
        public String roomId;
        public String talkerId;
        public String talkerName;
        public String talkerNickName;
        public String talkDate;
        public String talkContent;
        public int percent;
        public boolean sendComplete;
        public String unreadUserIds;
        public View view = null;
        public boolean needUpload = false;
        public String thumbnailFilePath = null;
        public String thumbnailFileName = null;
        public String thumbnailWidth = "100";
        public String thumbnailHeight = "100";
        public String uploadFilePath = null;
        public String uploadFileName = null;
        public String downloadFilePath = null;
        public String downloadFileName = null;
        public String userIds = null;
        public String userNames = null;
        public Bitmap bmp = null;
        private Thread thread = null;
        private ChatWindow window = null;
        private Handler msgHandler = null;
        public static final short THREAD_TYPE_UPLOAD = 0x01;
        public static final short THREAD_TYPE_DOWNLOAD = 0x02;
        private short threadType;
        public short dataType;
        
        public boolean downloadSuccess = true; //2016-02-23

        public ChatData( String msgId, String roomId, String talkerId, String talkerName, String talkerNickName, String talkDate, String talkContent,
                        int percent, boolean sendComplete, String unreadUserIds )
        {
                this.msgId = msgId;
                this.roomId = roomId;
                this.talkerId = talkerId;
                this.talkerName = talkerName;
                this.talkerNickName = talkerNickName;
                this.talkDate = talkDate;
                this.talkContent = talkContent;
                this.percent = percent;
                this.unreadUserIds = unreadUserIds;
                this.sendComplete = sendComplete;
        }

        public void upload( ChatWindow window, Handler msgHandler )
        {
                this.window = window;
                this.msgHandler = msgHandler;
                threadType = THREAD_TYPE_UPLOAD;
                thread = new Thread( this );
                thread.start();
        }

        public File getAttachFile( Handler msgHandler )
        {
                if ( talkContent.indexOf( "FILE://" ) >= 0 )
                {
                        return new File( talkContent.substring( 7 ) );
                }
                if ( talkContent.indexOf( "." ) < 0 )
                {
                        return null;
                }
                downloadFilePath = Define.getContext().getFilesDir() + File.separator + msgId + talkContent.substring( talkContent.lastIndexOf( '.' ) );
                downloadFileName = talkContent.substring( talkContent.lastIndexOf( '/' ) + 1 );
                File f = new File( downloadFilePath );
                Log.d( "DownloadPath", f.exists() + ":" + downloadFileName + ":" + downloadFilePath );
                if ( f.exists() ) return f;
                else
                {
                        threadType = THREAD_TYPE_DOWNLOAD;
                        this.msgHandler = msgHandler;
                        return null;
                }
        }

        public boolean copy( File from, File to )
        {
                if ( from.equals( to ) ) return true;
                FileInputStream fi = null;
                FileOutputStream fo = null;
                byte[] buf = new byte[4096];
                int rcv = 0;
                try
                {
                        fi = new FileInputStream( from );
                        fo = new FileOutputStream( to );
                        while ( (rcv = fi.read( buf, 0, 4096 )) >= 0 )
                        {
                                fo.write( buf, 0, rcv );
                        }
                }
                catch ( Exception e )
                {
                        Log.e( "kr.co.ultari.atsmart.basic", "Copy", e );
                        return false;
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
                                catch ( Exception ee )
                                {}
                        }
                        if ( fo != null )
                        {
                                try
                                {
                                        fo.close();
                                        fo = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                }
                return true;
        }

        public void runAttach( String mimeType )
        {
                //Log.d( "RunAttach", downloadFilePath + "/" + uploadFilePath );
                
                try
                {
                        if ( downloadFilePath != null )
                        {
                                File of = new File( downloadFilePath );
                                File tf = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DOWNLOADS ), downloadFileName );
                                boolean copyResult = copy( of, tf );
                                try
                                {
                                        Log.d( "Run", tf.getCanonicalPath() + ":" + of.exists() + ":" + copyResult );
                                        Log.d( "Run", tf.getCanonicalPath() + ":" + tf.exists() + ":" + copyResult );
                                }
                                catch ( Exception e )
                                {}
                                Intent intent = new Intent( android.content.Intent.ACTION_VIEW );
                                Uri uri = Uri.fromFile( tf );
                                intent.setDataAndType( uri, mimeType );
                                Define.getContext().startActivity( intent );
                        }
                        else if ( uploadFilePath != null )
                        {
                                Intent intent = new Intent( android.content.Intent.ACTION_VIEW );
                                Uri uri = Uri.fromFile( new File( uploadFilePath ) );
                                intent.setDataAndType( uri, mimeType );
                                Define.getContext().startActivity( intent );
                        }
                        else if ( talkContent.indexOf( "FILE://" ) >= 0 )
                        {
                                String tFile = talkContent.substring( 7 );
                                
                                Intent intent = new Intent( android.content.Intent.ACTION_VIEW );
                                Uri uri = Uri.fromFile( new File( tFile ) );
                                intent.setDataAndType( uri, mimeType );
                                Define.getContext().startActivity( intent );
                        }
                }
                catch(ActivityNotFoundException e)
                {
                        Toast.makeText( Define.getContext(), "해당파일을 실행할 수 있는 어플리케이션이 없습니다.", Toast.LENGTH_SHORT ).show();
                        e.printStackTrace();
                }
                catch(Exception e)
                {
                        e.printStackTrace();
                }
        }

        public void download()
        {
                if ( threadType == THREAD_TYPE_DOWNLOAD && msgHandler != null )
                {
                        thread = new Thread( this );
                        thread.start();
                }
        }

        public void run()
        {
                Log.d( "threadType", "threadType : " + threadType );
                if ( threadType == THREAD_TYPE_UPLOAD )
                {
                        boolean complete = false;
                        Socket sc = null;
                        FileInputStream fi = null;
                        InputStream is = null;
                        OutputStream os = null;
                        InputStreamReader ir = null;
                        OutputStreamWriter ow = null;
                        byte[] buf = null;
                        char[] strBuffer = new char[1024];
                        File f = null;
                        Log.i( "Upload", thumbnailFilePath + ":" + thumbnailFileName + ":" + msgId );
                        if ( thumbnailFilePath != null && thumbnailFileName != null )
                        {
                                try
                                {
                                        fi = new FileInputStream( thumbnailFilePath );
                                        sc = UltariSocketUtil.getProxySocket();
                                        is = sc.getInputStream();
                                        os = sc.getOutputStream();
                                        ir = new InputStreamReader( is, "EUC-KR" );
                                        ow = new OutputStreamWriter( os, "EUC-KR" );
                                        String sndMsg;
                                        if ( Define.useUnicode ) sndMsg = "PUTA\t" + msgId + "\tsmall_" + msgId + "\tM";
                                        else
                                        {
                                                if ( Define.useOldFileTransferProtocol ) sndMsg = "PUTA\t" + msgId + "\t"
                                                                + URLEncoder.encode( uploadFileName, "MS949" ) + "\tM"; // 93
                                                else sndMsg = "PUTA\t" + msgId + "\t" + uploadFileName + "\tM"; // scm
                                        }
                                        sndMsg = sndMsg.trim();
                                        ow.write( sndMsg );
                                        ow.flush();
                                        buf = new byte[4096];
                                        Define.TRACE( "Send : " + sndMsg );
                                        int rcv = ir.read( strBuffer, 0, 1024 );
                                        Define.TRACE( "RCV : " + new String( strBuffer, 0, rcv ) );
                                        if ( rcv < 0 || new String( strBuffer, 0, rcv ).indexOf( "ready" ) < 0 )
                                        {
                                                return;
                                        }
                                        f = new File( uploadFilePath );
                                        while ( (rcv = fi.read( buf, 0, 4096 )) >= 0 )
                                        {
                                                os.write( buf, 0, rcv );
                                                os.flush();
                                        }
                                        sndMsg = "finish\f";
                                        os.write( sndMsg.getBytes() );
                                        os.flush();
                                        complete = true;
                                        rcv = is.read( buf, 0, 4096 );
                                }
                                catch ( Exception e )
                                {
                                        Define.EXCEPTION( e );
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
                                        buf = null;
                                }
                        }
                        Log.d( "threadType", "complete : " + complete );
                        if ( thumbnailFilePath != null && thumbnailFileName != null && complete == false ) return;
                        try
                        {
                                Log.i( "Upload", uploadFilePath + ":" + uploadFileName + ":" + msgId );
                                fi = new FileInputStream( uploadFilePath );
                                sc = UltariSocketUtil.getProxySocket();
                                is = sc.getInputStream();
                                os = sc.getOutputStream();
                                ir = new InputStreamReader( is, "EUC-KR" );
                                ow = new OutputStreamWriter( os, "EUC-KR" );
                                String sndMsg;
                                if ( Define.useUnicode ) sndMsg = "PUTA\t" + msgId + "\t" + msgId + "\tM";
                                else
                                {
                                        if ( Define.useOldFileTransferProtocol ) sndMsg = "PUTA\t" + msgId + "\t" + URLEncoder.encode( uploadFileName, "MS949" )
                                                        + "\tM"; // 93
                                        else sndMsg = "PUTA\t" + msgId + "\t" + uploadFileName + "\tM"; // scm
                                }
                                sndMsg = sndMsg.trim();
                                ow.write( sndMsg );
                                ow.flush();
                                buf = new byte[4096];
                                Define.TRACE( "Send : " + sndMsg );
                                int rcv = ir.read( strBuffer, 0, 1024 );
                                Define.TRACE( "RCV : " + new String( strBuffer, 0, rcv ) );
                                if ( rcv < 0 || new String( strBuffer, 0, rcv ).indexOf( "ready" ) < 0 )
                                {
                                        return;
                                }
                                long sndLength = 0;
                                f = new File( uploadFilePath );
                                long totalLength = f.length();
                                int oldPercent = 0;
                                while ( (rcv = fi.read( buf, 0, 4096 )) >= 0 )
                                {
                                        sndLength += rcv;
                                        os.write( buf, 0, rcv );
                                        os.flush();
                                        int percent = ( int ) (( double ) sndLength / ( double ) totalLength * ( double ) 100);
                                        if ( oldPercent != percent )
                                        {
                                                oldPercent = percent;
                                                Message m = msgHandler.obtainMessage( Define.AM_UPLOAD_COMPLETE, this );
                                                m.arg1 = percent;
                                                msgHandler.sendMessage( m );
                                        }
                                }
                                sndMsg = "finish\f";
                                ow.write( sndMsg );
                                ow.flush();
                                complete = true;
                                rcv = ir.read( strBuffer, 0, 1024 );
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
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
                                buf = null;
                        }
                        Log.i( "UploadResult", "complete : " + complete );
                        if ( complete )
                        {
                                if ( thumbnailFilePath != null && thumbnailFileName != null ) window.sendBroadcastChat( msgId, roomId, userIds, userNames,
                                                StringUtil.setDataType( msgId, uploadFileName, f.length() + "", thumbnailWidth, thumbnailHeight ) );
                                else window.sendBroadcastChat( msgId, roomId, userIds, userNames,
                                                StringUtil.setDataType( msgId, uploadFileName, f.length() + "", "100", "100" ) );
                                Database.instance( Define.getContext() ).updateChatContentComplete( msgId );
                        }
                }
                else if ( threadType == THREAD_TYPE_DOWNLOAD )
                {
                        Socket sc = null;
                        FileOutputStream fo = null;
                        InputStream is = null;
                        OutputStream os = null;
                        InputStreamReader ir = null;
                        OutputStreamWriter ow = null;
                        long totalLength = 0;
                        long gotLength = 0;
                        byte[] buf = new byte[4096];
                        
                        //2016-02-23
                        downloadSuccess = false;
                        //boolean downloadSuccess = false;
                        //
                        
                        File targetFile = new File( downloadFilePath );
                        try
                        {
                                fo = new FileOutputStream( downloadFilePath );
                                sc = UltariSocketUtil.getProxySocket();
                                is = sc.getInputStream();
                                os = sc.getOutputStream();
                                ir = new InputStreamReader( is, "EUC-KR" );
                                ow = new OutputStreamWriter( os, "EUC-KR" );
                                String sndMsg;
                                if ( Define.useUnicode ) sndMsg = "GETA\t" + msgId + "\t" + msgId + "\tM";
                                else
                                {
                                        if ( Define.useOldFileTransferProtocol ) sndMsg = "GETA\t" + msgId + "\t"
                                                        + URLEncoder.encode( downloadFileName, "MS949" ) + "\tM"; // 93
                                        else sndMsg = "GETA\t" + msgId + "\t" + downloadFileName + "\tM"; // scm
                                }
                                sndMsg = sndMsg.trim();
                                Log.d( "DOWNLOAD", sndMsg );
                                ow.write( sndMsg );
                                ow.flush();
                                char[] cbuf = new char[1024];
                                int rcv = ir.read( cbuf, 0, 1024 );
                                if ( rcv < 0 ) return;
                                String rcvStr = new String( cbuf, 0, rcv );
                                rcvStr = rcvStr.substring( 0, rcvStr.indexOf( '\f' ) + 1 );
                                if ( rcvStr.indexOf( "ready" ) < 0 ) return;
                                Log.d( "DOWNLOAD", "ready" );
                                totalLength = Long.parseLong( rcvStr.substring( rcvStr.lastIndexOf( "\t" ) + 1 ).trim() );
                                Log.i( "Downloading", "TotalLength : " + totalLength );
                                sndMsg = "ok\t0\f";
                                ow.write( sndMsg );
                                ow.flush();
                                int oldPercent = 0;
                                while ( (rcv = is.read( buf, 0, 4096 )) >= 0 )
                                {
                                        gotLength += rcv;
                                        if ( gotLength > totalLength )
                                        {
                                                rcv -= (gotLength - totalLength);
                                                gotLength = totalLength;
                                        }
                                        fo.write( buf, 0, rcv );
                                        fo.flush();
                                        int percent = ( int ) (( double ) gotLength / ( double ) totalLength * ( double ) 100);
                                        if ( oldPercent != percent )
                                        {
                                                oldPercent = percent;
                                                if ( percent == 100 && gotLength < totalLength ) percent = 99;
                                                Message m = msgHandler.obtainMessage( Define.AM_DOWNLOAD_RESULT, this );
                                                m.arg1 = percent;
                                                msgHandler.sendMessage( m );
                                        }
                                        Log.d( "DOWNLOAD", gotLength + "<" + totalLength );
                                        // sndMsg = "ok\t" + gotLength + "\f";
                                        // os.write(sndMsg.getBytes());
                                        // os.flush();
                                        if ( gotLength == totalLength )
                                        {
                                                downloadSuccess = true;
                                                // sndMsg = "ok\t" + gotLength + "\f";
                                                // os.write(sndMsg.getBytes());
                                                // os.flush();
                                                android.util.Log.i( "AtSmartChatDownload", "Success" );
                                                return;
                                        }
                                }
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
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
                                if ( !downloadSuccess )
                                {
                                        targetFile.delete();
                                }
                                else
                                {
                                        Message m = msgHandler.obtainMessage( Define.AM_DOWNLOAD_RESULT, this );
                                        m.arg1 = 100;
                                        msgHandler.sendMessage( m );
                                }
                                buf = null;
                        }
                }
        }
}
