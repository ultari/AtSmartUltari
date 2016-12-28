package kr.co.ultari.atsmart.basic.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;
import android.content.Context;
import android.os.Environment;

public class SendMessage extends Thread {
        private Socket mProxySocket;
        private InputStreamReader ir;
        private OutputStreamWriter ow;
        private StringBuffer mSb;
        private String mReceiveId = "";
        private String mSendId = "";
        private String mSendName = "";
        private String mSubject = "";
        private String mContent = "";
        private Context mContext;

        public SendMessage( Context context, String receiveId, String sendId, String sendName, String subject, String msg ) throws Exception
        {
                this.mReceiveId = receiveId;
                this.mSendId = sendId;
                this.mSendName = sendName;
                this.mSubject = subject;
                this.mContent = msg;
                this.mContext = context;
                start();
        }

        public void write( String msg, OutputStreamWriter ow ) throws Exception
        {
                ow.write( msg, 0, msg.length() );
                ow.flush();
        }

        public void readAndWait( String str, InputStreamReader ir ) throws Exception
        {
                char[] buf = new char[1024];
                int rcv = 0;
                while ( (rcv = ir.read( buf, 0, 1023 )) >= 0 )
                {
                        mSb.append( new String( buf, 0, rcv ) );
                        if ( mSb.indexOf( str ) >= 0 && mSb.charAt( mSb.length() - 1 ) == '\f' )
                        {
                                mSb.delete( 0, mSb.length() );
                                return;
                        }
                }
                if ( rcv < 0 ) throw new Exception( "socket closed" );
        }

        @Override
        public void run()
        {
                FileInputStream fis = null;
                File f = null;
                this.mSb = new StringBuffer();
                this.mSb.delete( 0, mSb.length() );
                try
                {
                        mProxySocket = UltariSocketUtil.getProxySocket();
                        ir = new InputStreamReader( mProxySocket.getInputStream(), "EUC-KR" );
                        ow = new OutputStreamWriter( mProxySocket.getOutputStream(), "EUC-KR" );
                        String path = "";
                        Vector<File> fileAr = new Vector<File>();
                        MakeSendMessage em = new MakeSendMessage( mContext, mReceiveId, mSendId, mSendName, mSubject, mContent, fileAr, "./" );
                        path = em.makeSendFile();
                        if ( path.equals( "" ) ) return;
                        f = new File( Environment.getExternalStoragePublicDirectory( android.os.Environment.DIRECTORY_DOWNLOADS ) + File.separator + path );
                        String sendMsg = "putm\t" + mReceiveId + "\t" + f.length();
                        write( sendMsg, ow );
                        readAndWait( "ready", ir );
                        fis = new FileInputStream( f );
                        int sendSize = 4096;
                        if ( sendSize > ( int ) f.length() ) sendSize = ( int ) f.length();
                        byte[] buf = new byte[sendSize];
                        int s = -1;
                        while ( (s = fis.read( buf, 0, sendSize )) >= 0 )
                        {
                                if ( s == 0 ) continue;
                                write( new String( buf ), ow );
                                readAndWait( "ok", ir );
                        }
                        write( "\f", ow );
                        readAndWait( "ok", ir );
                        if ( f.exists() ) f.delete();
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                finally
                {
                        if ( ir != null ) try
                        {
                                ir.close();
                                ir = null;
                        }
                        catch ( Exception ee )
                        {}
                        if ( ow != null ) try
                        {
                                ow.close();
                                ow = null;
                        }
                        catch ( Exception ee )
                        {}
                        if ( mProxySocket != null ) try
                        {
                                mProxySocket.close();
                                mProxySocket = null;
                        }
                        catch ( Exception ee )
                        {}
                        if ( fis != null ) try
                        {
                                fis.close();
                                fis = null;
                        }
                        catch ( Exception ee )
                        {}
                        if ( f.exists() )
                        {
                                f.delete();
                        }
                }
        }
}
