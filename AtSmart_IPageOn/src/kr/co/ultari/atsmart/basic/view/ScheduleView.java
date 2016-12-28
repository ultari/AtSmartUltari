package kr.co.ultari.atsmart.basic.view;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ScheduleView extends Fragment {
        private static final String TAG = "/AtSmart/ScheduleView";
        private static ScheduleView scheduleViewInstance = null;
        public LayoutInflater inflater;
        private View view;
        private WebView mWebview;

        public static ScheduleView instance()
        {
                if ( scheduleViewInstance == null ) scheduleViewInstance = new ScheduleView();
                return scheduleViewInstance;
        }

        @Override
        public void onDestroy()
        {
                super.onDestroy();
                mWebview.getSettings().setBuiltInZoomControls( false );
                mWebview.destroy();
                mWebview = null;
                scheduleViewInstance = null;
        }

        @Override
        public void onStop()
        {
                super.onStop();
                mWebview.getSettings().setBuiltInZoomControls( false );
        };

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                this.inflater = inflater;
                view = inflater.inflate( R.layout.activity_schedule, null );
                mWebview = ( WebView ) view.findViewById( R.id.schedule_webview );
                mWebview.setWebViewClient( new WebClient() );
                WebSettings set = mWebview.getSettings();
                set.setJavaScriptEnabled( true );
                set.setBuiltInZoomControls( true );
                set.setSupportZoom( true );
                // mWebview.loadUrl( "http://www.google.com" );
                mWebview.loadUrl( Define.ucPresenceUrl );
                return view;
        }
        class WebClient extends WebViewClient {
                public boolean shouldOverrideUrlLoading( WebView view, String url )
                {
                        view.loadUrl( url );
                        return true;
                }
        }
}
