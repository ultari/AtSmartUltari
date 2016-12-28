package kr.co.ultari.atsmart.basic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import kr.co.ultari.atsmart.basic.control.TabButton;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.service.AlertDialogNotification;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.subview.Waiter;
import kr.co.ultari.atsmart.basic.util.FmcSendBroadcast;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.view.GroupSearchView;
import kr.co.ultari.atsmart.basic.view.MainViewTabFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ActionManager
{
        public static TabButton talkTabButton = null;
        public static TabButton notifyTabButton = null;
        public static TabButton messageTabButton = null;
        public static TabButton moreTabButton = null;
        public static Waiter waiter = null;
        public static MainViewTabFragment tabs = null;
        private static ProgressDialog m_WaitForConnectProgressDialog;
        public static boolean m_bShowWaitMessage = false;

        public static void openChat( Context context, String sRoomId, String sUserIds, String sUserNames )
        {
                try
                {
                        TRACE( "openChat (" + sRoomId + "," + sUserIds + "," + sUserNames + ")" );
                        Intent intent = new Intent( context, kr.co.ultari.atsmart.basic.view.ChatWindow.class );
                        intent.putExtra( "roomId", sRoomId );
                        intent.putExtra( "userIds", sUserIds );
                        intent.putExtra( "userNames", sUserNames );
                        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
                        context.startActivity( intent );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }
        static class closeDlg extends Thread {
                AlertDialog dlg;

                public closeDlg( AlertDialog dlg )
                {
                        this.dlg = dlg;
                        this.start();
                }

                public void run()
                {
                        try
                        {
                                sleep( 5000 );
                        }
                        catch ( Exception e )
                        {}
                        dlg.dismiss();
                }
        }

        public static void showWaitForConnect()
        {
                if ( !Define.isVisible ) return;
                if ( m_bShowWaitMessage == true )
                {
                        m_WaitForConnectProgressDialog.dismiss();
                        m_WaitForConnectProgressDialog = null;
                        AlertDialog dlg = alert( MainActivity.Instance(), "서버에 연결할 수 없습니다." );
                        new closeDlg( dlg );
                        return;
                }
                if ( m_WaitForConnectProgressDialog != null )
                {
                        m_WaitForConnectProgressDialog.dismiss();
                        m_WaitForConnectProgressDialog = null;
                }
                Activity topActivity = Define.nowTopActivity;
                if ( topActivity == null )
                {
                        topActivity = MainActivity.Instance();
                }
                m_WaitForConnectProgressDialog = ProgressDialog.show( topActivity, "", "서버와 연결 중입니다.", true );
                m_WaitForConnectProgressDialog.show();
                m_bShowWaitMessage = true;
        }

        public static void hideWaitForConnect()
        {
                m_bShowWaitMessage = false;
                if ( m_WaitForConnectProgressDialog != null )
                {
                        m_WaitForConnectProgressDialog.dismiss();
                        m_WaitForConnectProgressDialog = null;
                }
        }

        public static void showAlertDialog( Context context, String title, String content, String roomId, String talkId )
        {
                Log.d( "ShowDialog", "10" );
                TRACE( "showAlertDialog (" + title + "," + content + "," + roomId + "," + talkId + ")" );
                Bundle bun = new Bundle();
                bun.putString( "TITLE", title );
                bun.putString( "MESSAGE", content );
                bun.putString( "RoomId", roomId );
                bun.putString( "userId", talkId );
                Intent popupIntent = new Intent( context, kr.co.ultari.atsmart.basic.service.AlertDialog.class );
                popupIntent.putExtras( bun );
                popupIntent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP );
                PendingIntent pie = PendingIntent.getActivity( context, 0, popupIntent, PendingIntent.FLAG_UPDATE_CURRENT );
                try
                {
                        pie.send();
                }
                catch ( CanceledException e )
                {
                        EXCEPTION( e );
                }
        }

        // 2015-05-02
        public static void popupUserInfo( Context context, String id, String name, String part, String nickName )
        {
                try
                {
                        /*
                         * Intent intent = new Intent( context, kr.co.ultari.atsmart.basic.subview.UserInfo.class );
                         * intent.putExtra( "ID", id );
                         * intent.putExtra( "NAME", name );
                         * intent.putExtra( "PART", part );
                         * intent.putExtra( "NICK", nickName );
                         * intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                         * context.startActivity( intent );
                         */
                        // 2015-05-05 info
                        String[] ar = StringUtil.parseName( name );
                        Contact acontact = new Contact();
                        acontact.userId = null;
                        acontact.userName = null;
                        acontact.setPhotoid( 0 );
                        acontact.setUserid( id );
                        acontact.setType( "Org" );
                        acontact.setTelnum( ar[3] );
                        acontact.setPhonenum( ar[5] );
                        acontact.setName( ar[0] );
                        acontact.setPosition( ar[1] );
                        acontact.setCompany( ar[2] );
                        acontact.setEmail( ar[6] );
                        acontact.setJob( ar[4] ); //2016-12-16
                        acontact.setNickName( nickName );
                        acontact.userName = name;
                        
                        //contactDetail name:제갈량#차장#아이페이지온#0702003014##01012345678#hansy##1#11111111-c561-80b0-ffff-1111da199214, size:10
                        Log.d( "actionManager", "contactDetail name:"+name +", size:"+ar.length);
                        
                        Define.contactMap.put( id, acontact );
                        Intent it = new Intent( context, kr.co.ultari.atsmart.basic.subview.ContactDetail.class );
                        it.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                        it.putExtra( "contactId", id );
                        context.startActivity( it );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public static void confirm( Context context, String title, String str, final Handler handler )
        {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder( context );
                alertDialog.setTitle( title );
                alertDialog.setMessage( str );
                alertDialog.setIcon( R.drawable.icon );
                alertDialog.setPositiveButton( context.getString( R.string.ok ), new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int which )
                        {
                                Message m = handler.obtainMessage( Define.AM_CONFIRM_YES, null );
                                handler.sendMessage( m );
                                dialog.cancel();
                        }
                } );
                alertDialog.setNegativeButton( context.getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int which )
                        {
                                Message m = handler.obtainMessage( Define.AM_CONFIRM_NO, null );
                                handler.sendMessage( m );
                                dialog.cancel();
                        }
                } );
                alertDialog.show();
        }

        public static AlertDialog alert( Context context, String str )
        {
                hideProgressDialog();
                try
                {
                        AlertDialog.Builder alert = new AlertDialog.Builder( context );
                        alert.setTitle( context.getString( R.string.app_name ) );
                        alert.setMessage( str );
                        alert.setCancelable( false );
                        alert.setPositiveButton( "OK", new DialogInterface.OnClickListener() {
                                public void onClick( DialogInterface dialog, int which )
                                {
                                        dialog.dismiss();
                                }
                        } );
                        return alert.show();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
        }

        public static void callPhone( Context context, String number )
        {
                FmcSendBroadcast.FmcSendCall( number ,0, context); //2016-03-31
        }

        public static void showProcessingDialog( Context context, String title, String content )
        {
                hideProgressDialog();
                Intent intent = new Intent( context, kr.co.ultari.atsmart.basic.subview.Waiter.class );
                intent.putExtra( "TITLE", title );
                intent.putExtra( "CONTENT", content + context.getString( R.string.wait ) );
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                context.startActivity( intent );
        }

        public static void hideProgressDialog()
        {
                if ( waiter != null ) waiter.finish();
        }

        public static void resumeActivity( Context context, String roomId )
        {
                try
                {
                        // 2015-04-30
                        if ( MainActivity.search != null )
                        {
                                MainActivity.search.finish();
                        }
                        if ( !Define.isVisible )
                        {
                                Intent intent = new Intent( context.getApplicationContext(), kr.co.ultari.atsmart.basic.AtSmart.class );
                                if ( !roomId.equals( "" ) )
                                {
                                        intent.putExtra( "RoomId", roomId );
                                        intent.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
                                        context.startActivity( intent );
                                }
                                // context.startActivity( intent );
                        }
                        else
                        {
                                ArrayList<ArrayList<String>> ar = Database.instance( context ).selectChatRoomInfo( roomId );
                                if ( ar != null ) openChat( context, roomId, ar.get( 0 ).get( 1 ), ar.get( 0 ).get( 2 ) );
                                else openChat( context, roomId, "", "" );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public static void showPhoto( String userId )
        {
                Intent intent = new Intent( Define.mContext, kr.co.ultari.atsmart.basic.subview.PhotoViewer.class );
                intent.putExtra( "userId", userId );
                Define.mContext.startActivity( intent );
        }
        
        private static final String TAG = "/AtSmart/ActionManager";

        public static void TRACE( String s )
        {
                if ( !Define.useTrace ) return;
                android.util.Log.i( TAG, s );
        }

        private static void EXCEPTION( Throwable e )
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
