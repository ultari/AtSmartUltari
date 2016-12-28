package kr.co.ultari.atsmart.basic.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import kr.co.ultari.atsmart.basic.Define;

public class URLRunner extends Thread
{
	String url;
	Handler handler;
	
	public URLRunner(String url, Handler handler)
	{
		this.url = url;
		this.handler = handler;
		
		this.start();
	}
	
    public void run()
    {
            URL url = null;
            HttpURLConnection sc = null;
            InputStream is = null;
            
            try
            {
				url = new URL(this.url);
				sc = (HttpURLConnection) url.openConnection();
				is = sc.getInputStream();
				
				String rcvText = "";
				int read = -1;
				
				byte[] buf = new byte[4096];
				while ((read = is.read(buf, 0, 4096)) >= 0)
				{
					rcvText += new String(buf, 0, read);
				}
				
				Message m = handler.obtainMessage(1111);
				m.obj = rcvText;
				handler.sendMessage(m);
				
				buf = null;
            }
            catch ( Exception e )
            {
                    android.util.Log.e("URLRunner", "URLRunner", e);
            }
            finally
            {
            	if ( sc != null ) { try { sc.disconnect(); sc = null; } catch ( Exception ee ) {} }
            	if ( is != null ) { try { is.close(); is = null; } catch ( Exception ee ) {} }
            }
    }
}