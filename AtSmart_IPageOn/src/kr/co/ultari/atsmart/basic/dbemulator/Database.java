package kr.co.ultari.atsmart.basic.dbemulator;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class Database {
        private static Database dbInstance = null;
        private static Context context;
        private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
        private final Lock r = rwl.readLock();
        private final Lock w = rwl.writeLock();

        public static Database instance( Context mContext )
        {
                if ( mContext == null ) return null;
                if ( dbInstance == null )
                {
                        dbInstance = new Database();
                }
                if ( mContext != null )
                {
                        context = mContext;
                }
                return dbInstance;
        }

        public synchronized SQLiteDatabase open()
        {
                if ( context != null )
                {
                        DatabaseHelper m_Helper = new DatabaseHelper( context );
                        return m_Helper.getWritableDatabase();
                }
                return null;
        }

        public ArrayList<ArrayList<String>> getResultArray( Cursor cs )
        {
                ArrayList<ArrayList<String>> returnAr = new ArrayList<ArrayList<String>>();
                cs.moveToFirst();
                while ( !cs.isAfterLast() )
                {
                        ArrayList<String> result = new ArrayList<String>();
                        for ( int j = 0; j < cs.getColumnCount(); j++ )
                        {
                                result.add( cs.getString( j ) );
                        }
                        returnAr.add( result );
                        cs.moveToNext();
                }
                cs.close();
                return returnAr;
        }

        public void insertFavorite( String sUserId, String sParentId, String sUserName, String sUserInfo, String sIcon, String sOrder )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] args = new String[7];
                        args[0] = sUserId;
                        args[1] = sParentId;
                        args[2] = sUserName;
                        args[3] = sUserInfo;
                        args[4] = sIcon;
                        args[5] = "false";
                        args[6] = sOrder;
                        TRACE( "0:" + args[0] + ", 1:" + args[1] + ", 2:" + args[2] + ", 3:" + args[3] + ", 4:" + args[4] + ", 5:" + args[5] );
                        mDb.execSQL( "INSERT INTO tblFavorite (sUserId, sParentId, sUserName, sUserInfo, sIcon, SCheck, sOrder) VALUES (?,?,?,?,?,?,?);", args );
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }
        
        //2016-11-29 HHJ 
        public int selectFavorite(String userId)
        {
        	if ( userId == null ) return 0;
        	SQLiteDatabase mDb = null;
            Cursor cs = null;
            r.lock();
            try
            {
                mDb = open();
                cs = mDb.query( "tblFavorite", new String[] { "sUserId" }, "sUserId=?", new String[] { userId }, null, null, null );
                int ret = cs.getCount();
                return ret;
            }
            catch ( Exception e )
            {
                EXCEPTION( e );
                return 0;
            }
            finally
            {
                if ( cs != null )
                {
                        try
                        {
                                cs.close();
                                cs = null;
                        }
                        catch ( Exception ee )
                        {}
                }
                if ( mDb != null )
                {
                        try
                        {
                                mDb.close();
                                mDb = null;
                        }
                        catch ( Exception ee )
                        {}
                }
                r.unlock();
            }
        }
        //

        public ArrayList<ArrayList<String>> selectFavorite()
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblFavorite", new String[] { "sUserId", "sParentId", "sUserName", "sUserInfo", "sIcon", "sOrder" }, null, null, null,
                                        null, "sUserName ASC" );
                        TRACE( "SelectFavorite load ok" );
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public void deleteFavorite( String sUserId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sUserId;
                        mDb.execSQL( "DELETE FROM tblFavorite WHERE sUserId=?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void deleteFavoriteAll()
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[0];
                        mDb.execSQL( "DELETE FROM tblFavorite;", arg );
                        Log.i( TAG, "DELETE FROM tblFavoriteAll" );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public String selectConfig( String key )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblConfig", new String[] { "sKey", "sValue" }, "sKey=?", new String[] { key }, null, null, null );
                        if ( cs == null ) return "";
                        cs.moveToFirst();
                        String ret;
                        if ( cs.getCount() == 0 ) ret = "";
                        else ret = cs.getString( 1 );
                        cs.close();
                        return ret;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return "";
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public void updateConfig( String key, String value )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                w.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblConfig", new String[] { "sKey", "sValue" }, "sKey=?", new String[] { key }, null, null, null );
                        if ( cs == null || cs.getCount() == 0 )
                        {
                                if ( cs != null )
                                {
                                        try
                                        {
                                                cs.close();
                                                cs = null;
                                        }
                                        catch ( Exception ee )
                                        {}
                                }
                                mDb.execSQL( "INSERT INTO tblConfig  (sKey, sValue) VALUES (?,?);", new String[] { key, value } );
                        }
                        else
                        {
                                if ( cs != null )
                                {
                                        try
                                        {
                                                cs.close();
                                                cs = null;
                                        }
                                        catch ( Exception ee )
                                        {}
                                }
                                mDb.execSQL( "UPDATE tblConfig SET sValue=? WHERE sKey=?;", new String[] { value, key } );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void insertAlarm( String sMsgId, String sReceiverId, String sSenderName, String sReceiveDate, String sMsgTitle, String sMsgContent,
                        String sMsgUrl )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] args = new String[8];
                        args[0] = sMsgId;
                        args[1] = sReceiverId;
                        args[2] = sSenderName;
                        args[3] = sReceiveDate;
                        args[4] = sMsgTitle;
                        args[5] = sMsgContent;
                        args[6] = sMsgUrl;
                        args[7] = "N";
                        if ( sMsgUrl.indexOf( "</S_INFO>" ) >= 0 )
                        {
                                sMsgUrl = sMsgUrl.substring( sMsgUrl.indexOf( "</S_INFO>" ) + 9 );
                        }
                        sMsgTitle = sMsgTitle.replaceAll( "<br>", "\n" );
                        sMsgContent = sMsgContent.replaceAll( "<br>", "\n" );
                        mDb.execSQL( "INSERT INTO tblAlarm (sMsgId, sReceiverId, sSenderName, sReceiveDate, sMsgTitle, sMsgContent, sMsgUrl, cRead) VALUES (?,?,?,?,?,?,?,?);",
                                        args );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public ArrayList<ArrayList<String>> selectAlarm( String sMsgId )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        if ( sMsgId == null ) cs = mDb.query( "tblAlarm", new String[] { "sMsgId", "sReceiverId", "sSenderName", "sReceiveDate", "sMsgTitle",
                                        "sMsgContent", "sMsgUrl", "cRead" }, "sReceiverId=?", new String[] { Define.getMyId( context ) }, null, null,
                                        "sReceiveDate DESC" );
                        else cs = mDb.query( "tblAlarm", new String[] { "sMsgId", "sReceiverId", "sSenderName", "sReceiveDate", "sMsgTitle", "sMsgContent",
                                        "sMsgUrl", "cRead" }, "sMsgId=? and sReceiverId=?", new String[] { sMsgId, Define.getMyId( context ) }, null, null,
                                        "sReceiveDate DESC" );
                        // distinct, sort
                        /*
                         * if ( sMsgId == null )
                         * cs = mDb.query(true, "tblAlarm", new String[] {"sMsgId", "sReceiverId", "sSenderName", "sReceiveDate", "sMsgTitle", "sMsgContent",
                         * "sMsgUrl", "cRead"}, "sReceiverId=?", new String[] {Define.getMyId(context)}, "sMsgId", null, "sReceiveDate DESC", null);
                         * else
                         * cs = mDb.query(true, "tblAlarm", new String[] {"sMsgId", "sReceiverId", "sSenderName", "sReceiveDate", "sMsgTitle", "sMsgContent",
                         * "sMsgUrl", "cRead"}, "sMsgId=? and sReceiverId=?", new String[] {sMsgId, Define.getMyId(context)}, "sMsgId", null,
                         * "sReceiveDate DESC", null);
                         */
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public ArrayList<ArrayList<String>> selectAlarmByUnderDate( String sReceiveDate )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblAlarm", new String[] { "sMsgId", "sReceiverId", "sSenderName", "sReceiveDate", "sMsgTitle", "sMsgContent",
                                        "sMsgUrl", "cRead" }, "sReceiveDate<? and sReceiverId=?", new String[] { sReceiveDate, Define.getMyId( context ) },
                                        null, null, "sReceiveDate DESC" );
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public void deleteAlarm( String sMsgId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sMsgId;
                        mDb.execSQL( "DELETE FROM tblAlarm WHERE sMsgId=?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void deleteAlarmUnderDate( String date )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = date;
                        mDb.execSQL( "DELETE FROM tblAlarm WHERE sReceiveDate < ?;", arg );
                        Log.i( TAG, "DELETE FROM tblAlarm WHERE sReceiveDate < " + date );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void deleteAll()
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[0];
                        mDb.execSQL( "DELETE FROM tblAlarm;", arg );
                        Log.i( TAG, "DELETE FROM tblAlarm" );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void deleteGabage()
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = "[READ_COMPLETE]";
                        mDb.execSQL( "DELETE FROM tblChatContent WHERE sTalkerContent = ?;", arg );
                        Log.i( TAG, "DELETE FROM tblChatContent WHERE sTalkerContent = '[READ_COMPLETE]'" );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void updateAlarm( String sMsgId, String field, String value )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        mDb.execSQL( "UPDATE tblAlarm SET " + field + "='" + value + "' WHERE sMsgId='" + sMsgId + "'" );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public int selectUnreadAlarmCount()
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblAlarm", new String[] { "sMsgId", "sReceiverId", "sSenderName", "sReceiveDate", "sMsgTitle", "sMsgContent",
                                        "sMsgUrl", "cRead" }, "cRead=? and sReceiverId=?", new String[] { "N", Define.getMyId( context ) }, null, null,
                                        "sReceiveDate DESC" );
                        int ret = cs.getCount();
                        return ret;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return 0;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public void insertChatRoomInfo( String sRoomId, String sUserIds, String sUserNames, String sChatDate, String sLastMessage )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] args = new String[6];
                        args[0] = sRoomId;
                        args[1] = Define.getMyId( context );
                        args[2] = sUserIds;
                        args[3] = sUserNames;
                        args[4] = sChatDate;
                        args[5] = sLastMessage;
                        mDb.execSQL( "INSERT INTO tblChatRoomInfo (sRoomId, sReceiverId, sUserIds, sUserNames, sChatDate, sLastMessage, cRead) VALUES (?,?,?,?,?,?,'N');",
                                        args );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void updateChatRoomInfo( String sRoomId, String sChatDate, String sLastMessage, boolean isMine )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[3];
                        arg[0] = sChatDate;
                        arg[1] = sLastMessage;
                        arg[2] = sRoomId;
                        if ( isMine ) mDb.execSQL( "UPDATE tblChatRoomInfo SET sChatDate=?, sLastMessage=?, cRead='Y' WHERE sRoomId = ?;", arg );
                        else mDb.execSQL( "UPDATE tblChatRoomInfo SET sChatDate=?, sLastMessage=?, cRead='N' WHERE sRoomId = ?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        // 2015-05-10
        public void updateChatRoomUserNames( String sRoomId, String sUserNames )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[2];
                        arg[0] = sUserNames;
                        arg[1] = sRoomId;
                        Log.e( "@@@", "DB update -> updateChatRoomUserNames call! roomId:" + sRoomId + ", change names:" + sUserNames );
                        mDb.execSQL( "UPDATE tblChatRoomInfo SET sUserNames=? WHERE sRoomId = ?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void updateChatRoomUsers( String sRoomId, String sUserIds, String sUserNames )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[3];
                        arg[0] = sUserIds;
                        arg[1] = sUserNames;
                        arg[2] = sRoomId;
                        mDb.execSQL( "UPDATE tblChatRoomInfo SET sUserIds=?, sUserNames=? WHERE sRoomId = ?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public int selectUnreadChatContentAll()
        {
                ArrayList<ArrayList<String>> ar = selectChatRoomInfo( null );
                int count = 0;
                if ( ar != null )
                {
                        for ( int i = 0; i < ar.size(); i++ )
                        {
                                ArrayList<String> result = ar.get( i );
                                count += selectUnreadChatContentCount( result.get( 0 ) );
                        }
                        return count;
                }
                else return 0;
        }

        public int selectUnreadChatContentCount( String sRoomId )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sRoomId;
                        cs = mDb.query( "tblChatContent", new String[] { "sUnReadUserIds", "sTalkerContent" }, "sRoomId=? and sTalkerId <> ?", new String[] {
                                        sRoomId, Define.getMyId( context ) }, null, null, "" );
                        ArrayList<ArrayList<String>> resultAr = getResultArray( cs );
                        int unReadCount = 0;
                        for ( int i = 0; i < resultAr.size(); i++ )
                        {
                                String unReadUserIds = resultAr.get( i ).get( 0 );
                                StringTokenizer st = new StringTokenizer( unReadUserIds, "," );
                                boolean m_bExist = false;
                                while ( st.hasMoreTokens() )
                                {
                                        if ( st.nextToken().equals( Define.getMyId( context ) ) )
                                        {
                                                m_bExist = true;
                                                TRACE( "UnRead : " + unReadUserIds + ":" + resultAr.get( i ).get( 1 ) );
                                                break;
                                        }
                                }
                                if ( m_bExist ) unReadCount++;
                        }
                        return unReadCount;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return 0;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public ArrayList<ArrayList<String>> selectUnreadChatContent( String sRoomId )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sRoomId;
                        cs = mDb.query( "tblChatContent", new String[] { "sChatId", "sUnReadUserIds", "sTalkerContent" }, "sRoomId=? and sTalkerId<>?",
                                        new String[] { sRoomId, Define.getMyId( context ) }, null, null, "" );
                        ArrayList<ArrayList<String>> returnAr = new ArrayList<ArrayList<String>>();
                        ArrayList<ArrayList<String>> resultAr = getResultArray( cs );
                        for ( int i = 0; i < resultAr.size(); i++ )
                        {
                                StringTokenizer st = new StringTokenizer( resultAr.get( i ).get( 1 ), "," );
                                boolean myIdExist = false;
                                while ( st.hasMoreTokens() )
                                {
                                        if ( st.nextToken().equals( Define.getMyId( context ) ) )
                                        {
                                                myIdExist = true;
                                                break;
                                        }
                                }
                                if ( myIdExist )
                                {
                                        TRACE( "SelectUnRead : " + resultAr.get( i ).get( 2 ) );
                                        returnAr.add( resultAr.get( i ) );
                                }
                        }
                        return returnAr;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public void updateChatRoomRead( String sRoomId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sRoomId;
                        mDb.execSQL( "UPDATE tblChatRoomInfo SET cRead='Y' WHERE sRoomId = ?;", arg );
                        ArrayList<ArrayList<String>> returnAr = selectUnreadChatContent( sRoomId );
                        for ( int i = 0; i < returnAr.size(); i++ )
                        {
                                String sChatId = returnAr.get( i ).get( 0 );
                                String sUnReadUserIds = returnAr.get( i ).get( 1 );
                                StringTokenizer st = new StringTokenizer( sUnReadUserIds, "," );
                                String resultStr = "";
                                while ( st.hasMoreTokens() )
                                {
                                        String nowId = st.nextToken();
                                        nowId = nowId.trim();
                                        if ( !nowId.equals( Define.getMyId( context ) ) )
                                        {
                                                if ( !resultStr.equals( "" ) ) resultStr += ",";
                                                resultStr += nowId;
                                        }
                                }
                                TRACE( "UPDATE tblChatContent SET sUnReadUserIds='" + resultStr + "' WHERE sRoomId = '" + sRoomId + "' and sChatId='" + sChatId
                                                + "';" );
                                mDb.execSQL( "UPDATE tblChatContent SET sUnReadUserIds='" + resultStr + "' WHERE sRoomId = ? and sChatId='" + sChatId + "';",
                                                arg );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public ArrayList<ArrayList<String>> selectChatRoomInfo( String sRoomId )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        if ( sRoomId == null )
                        {
                                cs = mDb.query( "tblChatRoomInfo", new String[] { "sRoomId", "sUserIds", "sUserNames", "sChatDate", "sLastMessage", "cRead" },
                                                "sReceiverId=?", new String[] { Define.getMyId( context ) }, null, null, "sChatDate ASC" );
                        }
                        else
                        {
                                cs = mDb.query( "tblChatRoomInfo", new String[] { "sRoomId", "sUserIds", "sUserNames", "sChatDate", "sLastMessage", "cRead" },
                                                "sRoomId=? and sReceiverId=?", new String[] { sRoomId, Define.getMyId( context ) }, null, null, "sChatDate ASC" );
                        }
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public ArrayList<ArrayList<String>> selectChatRoomInfoByIds( String sIds )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblChatRoomInfo", new String[] { "sRoomId", "sUserIds", "sUserNames", "sChatDate", "sLastMessage", "cRead" },
                                        "sUserIds=? and sReceiverId=?", new String[] { sIds, Define.getMyId( context ) }, null, null, null );
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public void deleteChatRoomById( String sRoomId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sRoomId;
                        mDb.execSQL( "DELETE FROM tblChatRoomInfo WHERE sRoomId = ?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void deleteChatRoomInfo( String sTalkDate )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sTalkDate;
                        mDb.execSQL( "DELETE FROM tblChatRoomInfo WHERE sChatDate < ?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public int selectUnreadChatRoomCount()
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblChatRoomInfo", new String[] { "sRoomId" }, "cRead=? and sReceiverId=?",
                                        new String[] { "N", Define.getMyId( context ) }, null, null, null );
                        int ret = cs.getCount();
                        return ret;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return 0;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public String insertChatContent( String sChatId, String sRoomId, String sTalkerId, String sTalkerName, String sTalkerNickName, String sTalkDate,
                        String sTalkerContent, String sUnReadUserIds, boolean m_bSendComplete, boolean m_reserved )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] args = new String[10];
                        args[0] = sChatId;
                        args[1] = sRoomId;
                        args[2] = sTalkerId;
                        args[3] = sTalkerName;
                        args[4] = sTalkerNickName;
                        args[5] = sTalkDate;
                        args[6] = sTalkerContent;
                        if ( m_bSendComplete )
                        {
                                args[7] = "Y";
                        }
                        else
                        {
                                args[7] = "N";
                        }
                        args[8] = sUnReadUserIds;
                        if ( m_reserved )
                        {
                                args[9] = "Y";
                        }
                        else
                        {
                                args[9] = "N";
                        }
                        mDb.execSQL( "INSERT INTO tblChatContent (sChatId, sRoomId, sTalkerId, sTalkerName, sTalkerNickName, sTalkDate, sTalkerContent, sSendComplete, sUnReadUserIds, sReserved) VALUES (?,?,?,?,?,?,?,?,?,?);",
                                        args );
                        return sChatId;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
                return null;
        }

        public void updateChatContentComplete( String msgId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = msgId;
                        mDb.execSQL( "UPDATE tblChatContent SET sSendComplete='Y' WHERE sChatId = ?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void updateReservedChatContentComplete( String msgId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = msgId;
                        mDb.execSQL( "UPDATE tblChatContent SET sReserved='Y' WHERE sChatId = ?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void deleteChat( String sTalkDate )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sTalkDate;
                        mDb.execSQL( "DELETE FROM tblChatContent WHERE sTalkDate < ?;", arg );
                        TRACE( "DELETE FROM tblChatContent WHERE sTalkDate < " + sTalkDate );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void deleteAllData()
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[0];
                        mDb.execSQL( "DELETE FROM tblChatContent;", arg );
                        mDb.execSQL( "DELETE FROM tblChatRoomInfo;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void deleteChatBysRoomId( String sRoomId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sRoomId;
                        mDb.execSQL( "DELETE FROM tblChatContent WHERE sRoomId = ?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public void deleteChatBysChatId( String sChatId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sChatId;
                        mDb.execSQL( "DELETE FROM tblChatContent WHERE sChatId = ?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public int selectChatContentCount( String sRoomId )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblChatContent", new String[] { "sChatId" }, "sRoomId=?", new String[] { sRoomId }, null, null, null );
                        int cnt = getResultArray( cs ).size();
                        return cnt;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return 0;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        // 2015-09-05
        public ArrayList<ArrayList<String>> selectChatContentOrder( String sRoomId )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        if ( sRoomId == null ) cs = mDb.query( true, "tblChatContent", new String[] { "sChatId", "sRoomId", "sTalkerId", "sTalkerName",
                                        "sTalkerNickName", "sTalkDate", "sTalkerContent", "sSendComplete", "sUnReadUserIds", "sReserved" }, null, null, null,
                                        null, "sTalkDate asc", null );
                        else cs = mDb.query( true, "tblChatContent", new String[] { "sChatId", "sRoomId", "sTalkerId", "sTalkerName", "sTalkerNickName",
                                        "sTalkDate", "sTalkerContent", "sSendComplete", "sUnReadUserIds", "sReserved" }, "sRoomId=?", new String[] { sRoomId },
                                        null, null, "sTalkDate asc", null );
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        Log.e( TAG, "selectChatContent", e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        //
        public ArrayList<ArrayList<String>> searchChat( String sRoomId, String keyword )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( true, "tblChatContent", new String[] { "sChatId", "sRoomId", "sTalkerId", "sTalkerName", "sTalkerNickName",
                                        "sTalkDate", "sTalkerContent", "sSendComplete", "sUnReadUserIds", "sReserved" },
                                        "sRoomId = ? and sTalkerContent like ?", new String[] { sRoomId, "%" + keyword + "%" }, null, null, "sTalkDate desc",
                                        null );
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public ArrayList<ArrayList<String>> selectChatContentOrder( String sRoomId, int offset )
        {
                return selectChatContentOrder( sRoomId, offset, Define.ChatSelectRowsPerOneTime + 1 );
        }

        public ArrayList<ArrayList<String>> selectChatContentOrder( String sRoomId, int offset, String sTalkDate )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( true, "tblChatContent", new String[] { "sChatId", "sRoomId", "sTalkerId", "sTalkerName", "sTalkerNickName",
                                        "sTalkDate", "sTalkerContent", "sSendComplete", "sUnReadUserIds", "sReserved" }, "sRoomId=? and sTalkDate >= ?",
                                        new String[] { sRoomId, sTalkDate }, null, null, "sTalkDate desc", offset + "," + 300 );
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public ArrayList<ArrayList<String>> selectChatContentOrder( String sRoomId, int offset, int length )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        if ( sRoomId == null ) cs = mDb.query( true, "tblChatContent", new String[] { "sChatId", "sRoomId", "sTalkerId", "sTalkerName",
                                        "sTalkerNickName", "sTalkDate", "sTalkerContent", "sSendComplete", "sUnReadUserIds", "sReserved" }, null, null, null,
                                        null, "sTalkDate desc", null );
                        else cs = mDb.query( true, "tblChatContent", new String[] { "sChatId", "sRoomId", "sTalkerId", "sTalkerName", "sTalkerNickName",
                                        "sTalkDate", "sTalkerContent", "sSendComplete", "sUnReadUserIds", "sReserved" }, "sRoomId=?", new String[] { sRoomId },
                                        null, null, "sTalkDate desc", offset + "," + length );
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public ArrayList<ArrayList<String>> selectChatData( String sChatId )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblChatContent", new String[] { "sChatId", "sRoomId", "sTalkerId", "sTalkerName", "sTalkerNickName", "sTalkDate",
                                        "sTalkerContent", "sSendComplete", "sUnReadUserIds", "sReserved" }, "sChatId=?", new String[] { sChatId }, null, null,
                                        null );
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public ArrayList<ArrayList<String>> selectChatContent( String sRoomId )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        if ( sRoomId == null ) cs = mDb.query( "tblChatContent", new String[] { "sChatId", "sRoomId", "sTalkerId", "sTalkerName",
                                        "sTalkerNickName", "sTalkDate", "sTalkerContent", "sSendComplete", "sUnReadUserIds", "sReserved" }, null, null, null,
                                        null, null );
                        else cs = mDb.query( "tblChatContent", new String[] { "sChatId", "sRoomId", "sTalkerId", "sTalkerName", "sTalkerNickName", "sTalkDate",
                                        "sTalkerContent", "sSendComplete", "sUnReadUserIds", "sReserved" }, "sRoomId=?", new String[] { sRoomId }, null, null,
                                        null );
                        // distinct, sort
                        /*
                         * if ( sRoomId == null )
                         * cs = mDb.query(true, "tblChatContent", new String[] {"sChatId", "sRoomId", "sTalkerId", "sTalkerName", "sTalkerNickName",
                         * "sTalkDate", "sTalkerContent", "sSendComplete", "sUnReadUserIds", "sReserved"}, null, null, "sChatId", null, "sTalkDate asc", null);
                         * else
                         * cs = mDb.query(true, "tblChatContent", new String[] {"sChatId", "sRoomId", "sTalkerId", "sTalkerName", "sTalkerNickName",
                         * "sTalkDate", "sTalkerContent", "sSendComplete", "sUnReadUserIds", "sReserved"}, "sRoomId=?", new String[] {sRoomId}, "sChatId", null,
                         * "sTalkDate asc", null);
                         */
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public int updateChatReadComplete( String sChatId, String userId )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        ArrayList<String> msgIds = new ArrayList<String>();
                        ArrayList<String> unReadUsers = new ArrayList<String>();
                        cs = mDb.query( "tblChatContent", new String[] { "sChatId", "sUnReadUserIds" }, "sChatId=?", new String[] { sChatId }, null, null, null );
                        cs.moveToFirst();
                        while ( !cs.isAfterLast() )
                        {
                                msgIds.add( cs.getString( 0 ) );
                                unReadUsers.add( cs.getString( 1 ) );
                                cs.moveToNext();
                        }
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        for ( int i = 0; i < msgIds.size(); i++ )
                        {
                                String[] arg = new String[2];
                                arg[0] = StringUtil.makeString( StringUtil.getOtherIds( unReadUsers.get( i ), userId ) );
                                arg[1] = msgIds.get( i );
                                mDb.execSQL( "UPDATE tblChatContent SET sUnReadUserIds=? WHERE sChatId = ?;", arg );
                        }
                        return msgIds.size();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return 0;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public void test()
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblMessage", new String[] { "sMsgId", "sSenderId", "sSenderName", "sSenderPart", "sDate", "sSubject", "sImage",
                                        "cRead", "sContent", "sAttach", "sReceivers", "cIsSend" }, "cIsSend=?", new String[] { "Y" }, null, null, "sDate desc",
                                        "1,20" );
                        ArrayList<ArrayList<String>> rows = getResultArray( cs );
                        for ( int i = 0; i < rows.size(); i++ )
                        {
                                ArrayList<String> cells = rows.get( i );
                                for ( int j = 0; j < cells.size(); j++ )
                                {
                                        System.out.print( cells.get( j ) + "||" );
                                }
                                System.out.println( "" );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public int getUnreadMessageCount()
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblMessage", new String[] { "sMsgId" }, "cRead=?", new String[] { "N" }, null, null, null );
                        int ret = cs.getCount();
                        return ret;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return 0;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public ArrayList<ArrayList<String>> selectMessage( boolean isReceive )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        if ( isReceive )
                        // cs = mDb.query("tblMessage", new String[] {"sMsgId", "sSenderId", "sSenderName", "sSenderPart", "sDate", "sSubject", "sImage",
                        // "cRead", "sContent", "sAttach", "sReceivers"}, "cIsSend=?", new String[] {"N"}, null, null, "sDate desc", offset + "," +
                        // Define.messageDisplayCount);
                        cs = mDb.query( "tblMessage", new String[] { "sMsgId", "sSenderId", "sSenderName", "sSenderPart", "sDate", "sSubject", "sImage",
                                        "cRead", "sContent", "sAttach", "sReceivers" }, "cIsSend=?", new String[] { "N" }, null, null, "sDate desc", null );
                        else cs = mDb.query( "tblMessage", new String[] { "sMsgId", "sSenderId", "sSenderName", "sSenderPart", "sDate", "sSubject", "sImage",
                                        "cRead", "sContent", "sAttach", "sReceivers" }, "cIsSend=?", new String[] { "Y" }, null, null, "sDate desc", null );
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }

        public boolean insertMessage( String msgId, String senderId, String senderName, String senderPart, String subject, String content, String attach,
                        String receivers, String image )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String date = StringUtil.getNowDateTime();
                        String[] args = new String[12];
                        args[0] = msgId;
                        args[1] = senderId;
                        args[2] = senderName;
                        args[3] = senderPart;
                        args[4] = date;
                        if ( senderId.equals( Define.getMyId() ) )
                        {
                                args[5] = "Y";
                                args[8] = "Y";
                        }
                        else
                        {
                                args[5] = "N";
                                args[8] = "N";
                        }
                        args[6] = subject;
                        args[7] = image;
                        args[9] = content;
                        args[10] = attach;
                        args[11] = receivers;
                        mDb.execSQL( "INSERT INTO tblMessage (sMsgId, sSenderId, sSenderName, sSenderPart, sDate, cIsSend, sSubject, sImage, cRead, sContent, sAttach, sReceivers) VALUES (?,?,?,?,?,?,?,?,?,?,?,?);",
                                        args );
                        return true;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
                return false;
        }

        public void deleteMessageFile( String msgId )
        {
                File folder = new File( Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "AtSmart" + File.separator
                                + "MessageAttach" + File.separator + msgId );
                if ( !folder.exists() ) return;
                File[] fileList = folder.listFiles();
                for ( int i = 0; i < fileList.length; i++ )
                {
                        fileList[i].delete();
                }
                folder.delete();
        }

        public boolean deleteAllMessage( boolean isSend )
        {
                ArrayList<ArrayList<String>> rows = selectMessage( !isSend );
                for ( int i = 0; i < rows.size(); i++ )
                {
                        ArrayList<String> cells = rows.get( i );
                        deleteMessageFile( cells.get( 0 ) );
                }
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        if ( isSend ) mDb.execSQL( "DELETE FROM tblMessage WHERE cIsSend='Y';" );
                        else mDb.execSQL( "DELETE FROM tblMessage WHERE cIsSend='N';" );
                        return true;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
                return false;
        }

        public boolean deleteMessage( String sMsgId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sMsgId;
                        mDb.execSQL( "DELETE FROM tblMessage WHERE sMsgId = ?;", arg );
                        deleteMessageFile( sMsgId );
                        return true;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
                return false;
        }

        public void updateMessageRead( String sMsgId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[1];
                        arg[0] = sMsgId;
                        mDb.execSQL( "UPDATE tblMessage SET cRead='Y' WHERE sMsgId = ?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        w.unlock();
                }
        }

        public ArrayList<ArrayList<String>> selectMessageData( String sMsgId )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        cs = mDb.query( "tblMessage", new String[] { "sSenderId", "sSenderName", "sSenderPart", "sDate", "cIsSend", "sSubject", "sImage",
                                        "sContent", "sAttach", "sReceivers" }, "sMsgId=?", new String[] { sMsgId }, null, null, null );
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null )
                        {
                                try
                                {
                                        cs.close();
                                        cs = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        if ( mDb != null )
                        {
                                try
                                {
                                        mDb.close();
                                        mDb = null;
                                }
                                catch ( Exception ee )
                                {}
                        }
                        r.unlock();
                }
        }
        private static final String TAG = "/AtSmart/Database";

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
        
        public void insertCallLog( String sCallId, String sNumber, String sName, short callType, int duration, String sUserId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] args = new String[6];
                        args[0] = sCallId;
                        args[1] = sNumber;
                        args[2] = sName;
                        args[3] = callType + "";
                        args[4] = duration + "";
                        args[5] = sUserId;
                        
                        Log.d(TAG, "InsertCallLog : " + sCallId + "," + callType);
                        
                        mDb.execSQL( "INSERT INTO tblCallLog (sCallId, sNumber, sName, sCallType, sDuration, sUserId) VALUES (?,?,?,?,?,?);",                                        args );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null ) { try { mDb.close(); mDb = null; } catch ( Exception ee ) {} }
                        w.unlock();
                }
        }
        
        public void deleteCallLog( String sCallId )
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        
                        String[] arg = new String[1];
                        arg[0] = sCallId;
                        
                        mDb.execSQL( "DELETE FROM tblCallLog WHERE sCallId=?;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null ) { try { mDb.close(); mDb = null; } catch ( Exception ee ) {} }
                        w.unlock();
                }
        }
        
        public void deleteAllCallLog()
        {
                SQLiteDatabase mDb = null;
                w.lock();
                try
                {
                        mDb = open();
                        String[] arg = new String[0];
                        mDb.execSQL( "DELETE FROM tblCallLog;", arg );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                finally
                {
                        if ( mDb != null ) { try { mDb.close(); mDb = null; } catch ( Exception ee ) {} }
                        w.unlock();
                }
        }
        
        public ArrayList<ArrayList<String>> selectCallLog( short iCallType )
        {
                SQLiteDatabase mDb = null;
                Cursor cs = null;
                r.lock();
                try
                {
                        mDb = open();
                        
                        if ( iCallType == Define.CALL_TYPE_ALL )
                        	cs = mDb.query( "tblCallLog", new String[] { "sCallId", "sNumber", "sName", "sCallType", "sDuration", "sUserId" }, null, null, null, null, "sCallId desc", "300" );
                        else
                        	cs = mDb.query( "tblCallLog", new String[] { "sCallId", "sNumber", "sName", "sCallType", "sDuration", "sUserId" }, "sCallType=?", new String[] { iCallType + "" }, null, null, "sCallId desc", "300" );
                        
                        return getResultArray( cs );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
                finally
                {
                        if ( cs != null ) { try { cs.close(); cs = null; } catch ( Exception ee ) {} }
                        if ( mDb != null ) { try { mDb.close(); mDb = null; } catch ( Exception ee ) {} }
                        r.unlock();
                }
        }
}
