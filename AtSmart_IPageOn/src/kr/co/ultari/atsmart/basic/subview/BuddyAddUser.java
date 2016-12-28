package kr.co.ultari.atsmart.basic.subview;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import kr.co.ultari.atsmart.basic.control.tree.MessengerTree;
import kr.co.ultari.atsmart.basic.subdata.BuddyAddUserResultItemData;
import kr.co.ultari.atsmart.basic.util.UltariSSLSocket;
import kr.co.ultari.atsmart.basic.view.BuddyView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class BuddyAddUser extends MessengerTree implements Runnable {
        private static BuddyAddUser buddyAddUserInstance = null;
        private Thread thread;
        private AmCodec codec;
        private boolean onDestroy = false;
        private UltariSSLSocket sc = null;
        private BufferedWriter bw = null;
        private String noopStr = null;
        public LayoutInflater inflater;
        private PopupWindow searchTypePopup = null;
        private Button[] btnSearchs = new Button[6];
        private final String[] searchType = new String[6];
        private SearchEdit edit = null;
        private BuddyAddUserResultItem result;
        private ImageView ivSideImage;
        private ListView list;
        private Button btnSearchType, btnAllSelect, btnClose, btnAdd, btnAddList;
        private ImageButton btnSearch;
        private AlertDialog alert = null;

        public static BuddyAddUser instance()
        {
                if ( buddyAddUserInstance == null ) buddyAddUserInstance = new BuddyAddUser();
                return buddyAddUserInstance;
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                this.inflater = inflater;
                final View mView = inflater.inflate( R.layout.myfolder_organization, null );
                super.init( mView );
                mView.setFocusableInTouchMode( true );
                mView.requestFocus();
                mView.setOnKeyListener( new View.OnKeyListener() {
                        @Override
                        public boolean onKey( View v, int keyCode, KeyEvent event )
                        {
                                if ( keyCode == KeyEvent.KEYCODE_BACK )
                                {
                                        nowSelectedPartId = null;
                                        getActivity().finish();
                                        return true;
                                }
                                else return false;
                        }
                } );
                btnClose = ( Button ) view.findViewById( R.id.myfolder_close );
                btnClose.setOnClickListener( this );
                btnAllSelect = ( Button ) view.findViewById( R.id.myfolder_select );
                btnAllSelect.setOnClickListener( this );
                btnAdd = ( Button ) view.findViewById( R.id.myfolder_add );
                btnAdd.setOnClickListener( this );
                btnSearch = ( ImageButton ) view.findViewById( R.id.myfolder_search );
                btnSearch.setOnClickListener( this );
                btnSearchType = ( Button ) view.findViewById( R.id.myfolder_searchType );
                btnSearchType.setOnClickListener( this );
                btnAddList = ( Button ) view.findViewById( R.id.myfolder_add_list );
                btnAddList.setOnClickListener( this );
                ivSideImage = ( ImageView ) view.findViewById( R.id.myfolder_searchTextImage );
                result = new BuddyAddUserResultItem( getActivity().getApplicationContext(), this );
                list = ( ListView ) view.findViewById( R.id.myfolder_result );
                list.setAdapter( result );
                list.setClickable( true );
                list.setBackgroundColor( Color.LTGRAY );
                list.setOnItemClickListener( new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick( AdapterView<?> parent, View view, int position, long id )
                        {
                                nowSelectedUserId = result.getItem( position ).id;
                                nowSelectedUserName = result.getItem( position ).name;
                                result.setCheck( nowSelectedUserId, result.getItem( position ).checked );
                                result.notifyDataSetChanged();
                        }
                } );
                searchType[0] = getString( R.string.search_type_name );
                searchType[1] = getString( R.string.search_type_part );
                searchType[2] = getString( R.string.search_type_id );
                searchType[3] = getString( R.string.search_type_position );
                searchType[4] = getString( R.string.search_type_phone );
                searchType[5] = getString( R.string.search_type_mobile );
                edit = ( SearchEdit ) view.findViewById( R.id.myfolder_searchText );
                edit.setOnEditorActionListener( new OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction( TextView v, int actionId, KeyEvent event )
                        {
                                searchStarts();
                                return false;
                        }
                } );
                return view;
        }

        @Override
        public void onActivityCreated( Bundle savedInstanceState )
        {
                if ( !(Define.getMyId( context ).equals( "" )) ) startProcess();
                super.onActivityCreated( savedInstanceState );
        }

        public void startProcess()
        {
                if ( thread == null )
                {
                        onDestroy = false;
                        codec = new AmCodec();
                        thread = new Thread( this );
                        thread.start();
                }
        }
        class NoopTimer extends TimerTask {
                public void run()
                {
                        if ( bw == null ) return;
                        try
                        {
                                bw.write( noopStr );
                                bw.flush();
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };

        public void showKeyboard()
        {
                InputMethodManager imm = ( InputMethodManager ) getActivity().getSystemService( Activity.INPUT_METHOD_SERVICE );
                imm.showSoftInput( edit, 0 );
        }

        public void hideKeyboard()
        {
                InputMethodManager imm = ( InputMethodManager ) getActivity().getSystemService( Activity.INPUT_METHOD_SERVICE );
                imm.hideSoftInputFromWindow( edit.getWindowToken(), 0 );
        }

        @Override
        public void onDestroy()
        {
                onDestroy = true;
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
                thread = null;
                btnSearch.setImageBitmap( null );
                buddyAddUserInstance = null;
                nowSelectedPartId = null;
                super.onDestroy();
        }

        public void searchStarts()
        {
                try
                {
                        if ( !Define.getMyId( context ).equals( "" ) && !edit.getText().toString().equals( "" ) )
                        {
                                // ActionManager.showProcessingDialog( getActivity(), getString( R.string.search ), getString( R.string.searchList ) );
                                Message m = MyFolderHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                MyFolderHandler.sendMessage( m );
                                short type = 0;
                                for ( int i = searchType.length - 1; i >= 0; i-- )
                                {
                                        if ( btnSearchType.getText().equals( searchType[i] ) )
                                        {
                                                type = ( short ) i;
                                                break;
                                        }
                                }
                                try
                                {
                                        send( "SearchRequest\t" + type + "\t" + edit.getText() );
                                }
                                catch ( Exception e )
                                {
                                        EXCEPTION( e );
                                }
                        }
                        if ( searchTypePopup != null )
                        {
                                searchTypePopup.dismiss();
                                searchTypePopup = null;
                                for ( int i = 0; i < btnSearchs.length; i++ )
                                        btnSearchs[i] = null;
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        private void updateSearchPopup( String type )
        {
                for ( int i = btnSearchs.length - 1; i >= 0; i-- )
                {
                        if ( type.equals( btnSearchs[i].getText() ) ) btnSearchs[i].setBackgroundColor( 0xFF5077AD );
                        else btnSearchs[i].setBackgroundColor( 0xFF3B5981 );
                }
        }

        private void updateSearchName( String name )
        {
                btnSearchType.setText( name );
                searchTypePopup.dismiss();
                searchTypePopup = null;
                for ( int i = 0; i < btnSearchs.length; i++ )
                        btnSearchs[i] = null;
        }

        @Override
        public void OnCustomClick( View view )
        {
                if ( searchTypePopup != null && view != btnSearchs[0] && view != btnSearchs[1] && view != btnSearchs[2] && view != btnSearchs[3]
                                && view != btnSearchs[4] && view != btnSearchs[5] )
                {
                        try
                        {
                                searchTypePopup.dismiss();
                                searchTypePopup = null;
                                for ( int i = 0; i < btnSearchs.length; i++ )
                                        btnSearchs[i] = null;
                                return;
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
                if ( view == btnClose )
                {
                        nowSelectedPartId = null;
                        getActivity().finish();
                }
                else if ( view == btnSearch )
                {
                        searchStarts();
                        hideKeyboard();
                }
                else if ( view == btnAddList )
                {
                        // 추가된 사용자 목록 보여주기
                        CharSequence[] items = { "aaaaaa", "bbbbbb", "cccccc", "ddddd", "eeeeeee", "ffffffff", "gggggggg", "hhhhhhhhh", "iiiiiiiii",
                                        "jjjjjjjjjjjjjj", "kkkkkkkkkkkkkkkkkkkkkkkkkkk", "hhhhhhhhhhhhhhhh" };
                        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() );
                        builder.setTitle( "추가 사용자 목록" );
                        builder.setItems( items, new DialogInterface.OnClickListener() {
                                public void onClick( DialogInterface dialog, int item )
                                {
                                        alert.dismiss();
                                }
                        } );
                        alert = builder.create();
                        alert.show();
                }
                else if ( view == btnAdd )
                {
                        for ( int i = 0; i < result.getCount(); i++ )
                        {
                                if ( result.getItem( i ).checked )
                                {
                                        ArrayList<String> arr = new ArrayList<String>();
                                        arr.add( result.getItem( i ).id );
                                        arr.add( BuddyView.instance().nowSelectedPartId );
                                        arr.add( result.getItem( i ).name );
                                        arr.add( "0" );
                                        arr.add( result.getItem( i ).nickName );
                                        arr.add( "0" );
                                        // Message m = BuddyView.instance().buddyHandler.obtainMessage( Define.AM_MYFOLDER_ADD_USER, arr );
                                        // BuddyView.instance().buddyHandler.sendMessage( m );
                                        StringBuffer message = new StringBuffer();
                                        message.append( "UserAdd\t" + Define.getMyId( context ) + "\t" + result.getItem( i ).id + "\t"
                                                        + BuddyView.instance().nowSelectedPartId + "\t" + result.getItem( i ).name + "\t0" );
                                        Intent sendIntent = new Intent( Define.MSG_MYFOLDER_USER_ADD );
                                        sendIntent.putExtra( "MESSAGE", message.toString() );
                                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                                        context.sendBroadcast( sendIntent );
                                }
                        }
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) getActivity().findViewById( R.id.custom_toast_layout ) );
                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                        text.setText( getString( R.string.menu_user_add_msg ) );
                        text.setTypeface( Define.tfRegular );
                        Toast toast = new Toast( getActivity() );
                        toast.setGravity( Gravity.CENTER, 0, 0 );
                        toast.setDuration( Toast.LENGTH_SHORT );
                        toast.setView( layout );
                        toast.show();
                        result.clear();
                        result.notifyDataSetChanged();
                        BuddyView.instance().buddyHandler.sendEmptyMessageDelayed( Define.AM_REFRESH, 1500 );
                }
                else if ( view == btnAllSelect )
                {
                        for ( int i = 0; i < result.getCount(); i++ )
                                result.setCheck( result.getItem( i ).id, true );
                }
                else if ( view == btnSearchType )
                {
                        View popupView = inflater.inflate( R.layout.search_type_popup, null );
                        searchTypePopup = new PopupWindow( popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
                        btnSearchs[0] = ( Button ) popupView.findViewById( R.id.searchTypeName );
                        btnSearchs[1] = ( Button ) popupView.findViewById( R.id.searchTypePart );
                        btnSearchs[2] = ( Button ) popupView.findViewById( R.id.searchTypeId );
                        btnSearchs[3] = ( Button ) popupView.findViewById( R.id.searchTypePosition );
                        btnSearchs[4] = ( Button ) popupView.findViewById( R.id.searchTypePhone );
                        btnSearchs[5] = ( Button ) popupView.findViewById( R.id.searchTypeMobile );
                        for ( int i = 0; i < btnSearchs.length; i++ )
                                btnSearchs[i].setOnClickListener( this );
                        updateSearchPopup( btnSearchType.getText().toString() );
                        searchTypePopup.showAsDropDown( btnSearchType, 0, 0 );
                }
                else if ( btnSearchs[0] != null && view == btnSearchs[0] ) updateSearchName( btnSearchs[0].getText().toString() );
                else if ( btnSearchs[1] != null && view == btnSearchs[1] ) updateSearchName( btnSearchs[1].getText().toString() );
                else if ( btnSearchs[2] != null && view == btnSearchs[2] ) updateSearchName( btnSearchs[2].getText().toString() );
                else if ( btnSearchs[3] != null && view == btnSearchs[3] ) updateSearchName( btnSearchs[3].getText().toString() );
                else if ( btnSearchs[4] != null && view == btnSearchs[4] ) updateSearchName( btnSearchs[4].getText().toString() );
                else if ( btnSearchs[5] != null && view == btnSearchs[5] ) updateSearchName( btnSearchs[5].getText().toString() );
        }

        @Override
        public void run()
        {
                StringBuffer sb = new StringBuffer();
                sc = null;
                InputStreamReader ir = null;
                BufferedReader br = null;
                bw = null;
                char[] buf = new char[2048];
                int rcv = 0;
                Timer noopTimer = null;
                while ( onDestroy == false )
                {
                        try
                        {
                                sb.delete( 0, sb.length() );
                                // 2015-03-10
                                sc = new UltariSSLSocket( Define.mContext, Define.getServerIp( Define.mContext ), Integer.parseInt( Define
                                                .getServerPort( Define.mContext ) ) );
                                sc.setSoTimeout( 30000 );
                                ir = new InputStreamReader( sc.getInputStream() );
                                br = new BufferedReader( ir );
                                bw = sc.getWriter();
                                if ( buddyAddUserInstance.noopStr == null ) buddyAddUserInstance.noopStr = codec.EncryptSEED( "noop" ) + "\f";
                                noopTimer = new Timer();
                                noopTimer.schedule( new NoopTimer(), 15000, 15000 );
                                if ( onDestroy == false )
                                {
                                        Message m = MyFolderHandler.obtainMessage( Define.AM_CLEAR_ITEM, null );
                                        MyFolderHandler.sendMessage( m );
                                        send( "SubOrganizationRequest\t0" );
                                        while ( (rcv = br.read( buf, 0, 2047 )) >= 0 )
                                        {
                                                sb.append( new String( buf, 0, rcv ) );
                                                int pos;
                                                while ( (pos = sb.indexOf( "\f" )) >= 0 )
                                                {
                                                        String rcvStr = codec.DecryptSEED( sb.substring( 0, pos ) );
                                                        sb.delete( 0, pos + 1 );
                                                        String command = "";
                                                        ArrayList<String> param = new ArrayList<String>();
                                                        String nowStr = "";
                                                        for ( int i = 0; i < rcvStr.length(); i++ )
                                                        {
                                                                if ( rcvStr.charAt( i ) == '\t' )
                                                                {
                                                                        if ( command.equals( "" ) ) command = nowStr;
                                                                        else param.add( nowStr );
                                                                        nowStr = "";
                                                                }
                                                                else if ( i == (rcvStr.length() - 1) )
                                                                {
                                                                        nowStr += rcvStr.charAt( i );
                                                                        if ( command.equals( "" ) ) command = nowStr;
                                                                        else param.add( nowStr );
                                                                        nowStr = "";
                                                                }
                                                                else nowStr += rcvStr.charAt( i );
                                                        }
                                                        if ( !command.equals( "noop" ) ) process( command, param );
                                                }
                                        }
                                }
                        }
                        catch ( SocketException se )
                        {
                                TRACE( se.getMessage() );
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
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
                                if ( br != null )
                                {
                                        try
                                        {
                                                br.close();
                                                br = null;
                                        }
                                        catch ( Exception e )
                                        {}
                                }
                                if ( buddyAddUserInstance != null ) buddyAddUserInstance.noopStr = null;
                                if ( noopTimer != null )
                                {
                                        noopTimer.cancel();
                                        noopTimer = null;
                                }
                        }
                        if ( onDestroy ) return;
                        try
                        {
                                Thread.sleep( 5000 );
                        }
                        catch ( InterruptedException ie )
                        {}
                }
        }

        public void send( String msg ) throws Exception
        {
                if ( bw == null )
                {
                        startProcess();
                        return;
                }
                msg.replaceAll( "\f", "" );
                bw.write( codec.EncryptSEED( msg ) + '\f' );
                bw.flush();
        }
        public Handler MyFolderHandler = new Handler() {
                @SuppressWarnings( "unused" )
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_ADD_ORGANIZATION_PART )
                                {
                                        // hideProgress();
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        if ( param.size() >= 4 )
                                        {
                                                String id = param.get( 0 );
                                                String high = param.get( 1 );
                                                String name = param.get( 2 );
                                                String order = param.get( 3 );
                                                addFolder( id, high, name, order, false );
                                        }
                                }
                                else if ( msg.what == Define.AM_ADD_ORGANIZATION_USER )
                                {
                                        // hideProgress();
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        if ( param.size() >= 5 )
                                        {
                                                while ( param.size() < 9 )
                                                        param.add( "" );
                                                String id = param.get( 0 );
                                                String high = param.get( 1 );
                                                String icon = param.get( 2 );
                                                String name = param.get( 3 );
                                                String order = param.get( 4 );
                                                String param1 = param.get( 5 );
                                                String param2 = param.get( 6 );
                                                String param3 = param.get( 7 );
                                                String mobile = param.get( 8 );
                                                addFile( id, high, name, param1, Integer.parseInt( icon ), order );
                                                if ( mobile != null && (mobile.equals( "0" ) || mobile.equals( "1" )) ) setMobileOn( param.get( 0 ),
                                                                Integer.parseInt( mobile ) ); // 2015-03-01
                                        }
                                        boolean isEmpty = true;
                                        for ( int i = 0; i < result.getCount(); i++ )
                                                if ( result.getItem( i ).id.equals( param.get( 0 ) ) ) isEmpty = false;
                                        if ( isEmpty ) result.add( new BuddyAddUserResultItemData( param.get( 0 ), param.get( 1 ), param.get( 3 ), Integer
                                                        .parseInt( param.get( 2 ) ), "", false ) );
                                }
                                else if ( msg.what == Define.AM_LIST_CLEAR )
                                {
                                        result.clear();
                                        result.notifyDataSetChanged();
                                }
                                else if ( msg.what == Define.AM_SELECT_CHANGED )
                                {
                                        @SuppressWarnings( "unchecked" )
                                        ArrayList<String> param = ( ArrayList<String> ) msg.obj;
                                        result.add( new BuddyAddUserResultItemData( param.get( 0 ), param.get( 1 ), param.get( 3 ), Integer.parseInt( param
                                                        .get( 2 ) ), "", false ) );
                                }
                                else if ( msg.what == Define.AM_CLEAR_ITEM )
                                {
                                        result.clear();
                                        result.notifyDataSetChanged();
                                }
                                else
                                {
                                        super.handleMessage( msg );
                                }
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
        };

        public void process( String command, ArrayList<String> param )
        {
                if ( command.equals( "Part" ) && param.size() >= 4 )
                {
                        Message m = MyFolderHandler.obtainMessage( Define.AM_ADD_ORGANIZATION_PART, param );
                        MyFolderHandler.sendMessage( m );
                }
                if ( command.equals( "User" ) )
                {
                        Define.searchMobileOn.put( param.get( 0 ), param.get( 8 ) );
                        Message m = MyFolderHandler.obtainMessage( Define.AM_ADD_ORGANIZATION_USER, param );
                        MyFolderHandler.sendMessage( m );
                }
        }

        public void OnFirstExpand( String str )
        {
                // showProgress( getString( R.string.organization ), getString( R.string.organizationList ) );
                try
                {
                        send( "SubOrganizationRequest\t" + str );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }
}
