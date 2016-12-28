package kr.co.ultari.atsmart.basic.util;

import java.io.PrintWriter;
import java.net.Socket;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import android.content.Context;
import android.util.Log;

public class chatRoomExit extends Thread {
        private static final String TAG = "AtSmart/chatRoomExit";
        private String sendMsg;
        private Context context;

        public chatRoomExit( Context context, String msg )
        {
                sendMsg = msg;
                this.context = context;
        }

        @Override
        public void run()
        {
                Log.v( TAG, "run()" );
                UltariSSLSocket sc = null;
                PrintWriter pw = null;
                try
                {
                        sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                        .getServerPort( Define.mContext ) ) );
                        pw = new PrintWriter( sc.getWriter() );
                        pw.print( sendMsg );
                        pw.flush();
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
