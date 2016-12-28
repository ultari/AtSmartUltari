package kr.co.ultari.atsmart.basic.view;

import java.util.ArrayList;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.MainActivity;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.NotifyData;
import kr.co.ultari.atsmart.basic.subview.NotifyItem;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

@SuppressLint( { "HandlerLeak", "InflateParams" } )
public class NotifyView extends Fragment implements OnClickListener {
        private static final String TAG = "/AtSmart/NotifyView";
        private static NotifyView notifyViewInstance = null;
        public LayoutInflater inflater;
        private NotifyItem itemList;
        private ListView list;
        private View view;
        private Button m_btnDeleteAll;

        public static NotifyView instance()
        {
                if ( notifyViewInstance == null ) notifyViewInstance = new NotifyView();
                return notifyViewInstance;
        }

        @Override
        public void onDestroy()
        {
                super.onDestroy();
                notifyViewInstance = null;
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                this.inflater = inflater;
                view = inflater.inflate( R.layout.activity_notify, null );
                try
                {
                        itemList = new NotifyItem( getActivity().getApplicationContext(), this );
                        list = ( ListView ) view.findViewById( R.id.notifyList );
                        list.setAdapter( itemList );
                        m_btnDeleteAll = ( Button ) view.findViewById( R.id.deleteAllButton );
                        m_btnDeleteAll.setTypeface( Define.tfRegular );
                        m_btnDeleteAll.setOnClickListener( this );
                        resetData();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return view;
        }
        class WebClient extends WebViewClient {
                public boolean shouldOverrideUrlLoading( WebView view, String url )
                {
                        view.loadUrl( url );
                        return true;
                }
        }

        @Override
        public void onActivityCreated( Bundle savedInstanceState )
        {
                super.onActivityCreated( savedInstanceState );
        }

        /*
         * public void resetData()
         * {
         * Database.instance(getActivity().getApplicationContext()).deleteAll();
         * notifyViewInstance.resetData();
         * }
         */
        public void onClick( View view )
        {
                if ( view == m_btnDeleteAll ) ActionManager.confirm( getActivity(), getString( R.string.deleteAll ), getString( R.string.delAllAlarm ),
                                alertHandler );
        }
        public Handler alertHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_CONFIRM_YES )
                                {
                                        Database.instance( getActivity().getApplicationContext() ).deleteAll();
                                        notifyViewInstance.resetData();
                                }
                                else if ( msg.what == Define.AM_REFRESH )
                                {
                                        notifyViewInstance.resetData();
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        };

        public void recalcUnreadCount()
        {
                int unread = 0;
                for ( int i = 0; i < itemList.getCount(); i++ )
                {
                        if ( !itemList.getItem( i ).read ) unread++;
                }
                ActionManager.notifyTabButton.setNumber( unread );
                resetUnreadCount( unread );
                resetData();
        }

        public void resetData()
        {
                if ( itemList == null ) return;
                itemList.clear();
                int unRead = 0;
                ArrayList<ArrayList<String>> array = Database.instance( Define.getContext() ).selectAlarm( null );
                if ( array != null )
                {
                        for ( int j = 0; j < array.size(); j++ )
                        {
                                ArrayList<String> ar = array.get( j );
                                if ( ar.get( 7 ).equals( "Y" ) ) itemList.add( new NotifyData( ar.get( 0 ), ar.get( 1 ), ar.get( 2 ), ar.get( 3 ), ar.get( 4 ),
                                                ar.get( 5 ), ar.get( 6 ), true ) );
                                else
                                {
                                        unRead++;
                                        itemList.add( new NotifyData( ar.get( 0 ), ar.get( 1 ), ar.get( 2 ), ar.get( 3 ), ar.get( 4 ), ar.get( 5 ),
                                                        ar.get( 6 ), false ) );
                                }
                        }
                        itemList.notifyDataSetChanged();
                        Message m = MainActivity.mainHandler.obtainMessage( Define.AM_NEW_NOTIFY, null );
                        m.arg1 = unRead;
                        MainActivity.mainHandler.sendMessage( m );
                        resetUnreadCount( unRead );
                }
        }

        public void resetUnreadCount( int unRead )
        {
                String msg = unRead + getString( R.string.notReadAlarm );
                if ( unRead == 0 ) msg = getString( R.string.allReadAlarm );
                TextView tv = ( TextView ) view.findViewById( R.id.countLabel );
                if ( tv != null )
                {
                        tv.setTypeface( Define.tfRegular );
                        tv.setText( msg );
                        tv.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 14 );
                }
        }

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
