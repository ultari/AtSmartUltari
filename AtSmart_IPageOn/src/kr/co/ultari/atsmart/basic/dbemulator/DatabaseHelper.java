package kr.co.ultari.atsmart.basic.dbemulator;

import kr.co.ultari.atsmart.basic.Define;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper( Context context )
        {
                super( context, Define.DB_NAME, null, Define.DB_VERSION );
                initQuery();
        }

        private void initQuery()
        {
                if ( Define.queryCreateFavorite.equals( "" ) )
                {
                        Define.queryCreateFavorite += "CREATE TABLE IF NOT EXISTS tblFavorite ";
                        Define.queryCreateFavorite += "( ";
                        Define.queryCreateFavorite += "    sUserId         TEXT     NOT NULL, ";
                        Define.queryCreateFavorite += "    sParentId       TEXT     NOT NULL, ";
                        Define.queryCreateFavorite += "    sUserName       TEXT     NOT NULL, ";
                        Define.queryCreateFavorite += "    sUserInfo       TEXT     NOT NULL, ";
                        Define.queryCreateFavorite += "    sIcon           TEXT     NOT NULL, ";
                        Define.queryCreateFavorite += "    sCheck          TEXT     NOT NULL, ";
                        Define.queryCreateFavorite += "    sOrder          TEXT     NOT NULL ";
                        Define.queryCreateFavorite += "); ";
                }
                if ( Define.queryCreateConfig.equals( "" ) )
                {
                        Define.queryCreateConfig += "CREATE TABLE IF NOT EXISTS tblConfig ";
                        Define.queryCreateConfig += "( ";
                        Define.queryCreateConfig += "	sKEY		VARCHAR(32)	NOT NULL, ";
                        Define.queryCreateConfig += "	sVALUE		VARCHAR(128)	NOT NULL ";
                        Define.queryCreateConfig += "); ";
                }
                if ( Define.queryCreateAlarm.equals( "" ) )
                {
                        Define.queryCreateAlarm += "CREATE TABLE IF NOT EXISTS tblAlarm ";
                        Define.queryCreateAlarm += "( ";
                        Define.queryCreateAlarm += "	sMsgId		VARCHAR(32)	NOT NULL, ";
                        Define.queryCreateAlarm += "	sReceiverId	VARCHAR(64)	NOT NULL, ";
                        Define.queryCreateAlarm += "	sSenderName	VARCHAR(256)	NOT NULL, ";
                        Define.queryCreateAlarm += "	sReceiveDate	VARCHAR(32)	NOT NULL, ";
                        Define.queryCreateAlarm += "	sMsgTitle	TEXT		NOT NULL, ";
                        Define.queryCreateAlarm += "	sMsgContent	TEXT		NOT NULL, ";
                        Define.queryCreateAlarm += "	sMsgUrl	VARCHAR(1024)		NOT NULL, ";
                        Define.queryCreateAlarm += "	cRead	CHAR(1)		NOT NULL ";
                        Define.queryCreateAlarm += "); ";
                }
                if ( Define.queryCreateChatRoomInfo.equals( "" ) )
                {
                        Define.queryCreateChatRoomInfo += "CREATE TABLE IF NOT EXISTS tblChatRoomInfo ";
                        Define.queryCreateChatRoomInfo += "( ";
                        Define.queryCreateChatRoomInfo += "	sRoomId		VARCHAR(64)	NOT NULL, ";
                        Define.queryCreateChatRoomInfo += "	sReceiverId		VARCHAR(64)	NOT NULL, ";
                        Define.queryCreateChatRoomInfo += "	sUserIds	TEXT		NOT NULL, ";
                        Define.queryCreateChatRoomInfo += "	sUserNames	TEXT		NOT NULL, ";
                        Define.queryCreateChatRoomInfo += "	sChatDate	VARCHAR(32)	NOT NULL, ";
                        Define.queryCreateChatRoomInfo += "	sLastMessage	TEXT	NOT NULL, ";
                        Define.queryCreateChatRoomInfo += "	cRead	VARCHAR(1)		NOT NULL ";
                        Define.queryCreateChatRoomInfo += "); ";
                }
                if ( Define.queryCreateChatContent.equals( "" ) )
                {
                        Define.queryCreateChatContent += "CREATE TABLE IF NOT EXISTS tblChatContent ";
                        Define.queryCreateChatContent += "( ";
                        Define.queryCreateChatContent += "	sChatId		VARCHAR(64)	NOT NULL, ";
                        Define.queryCreateChatContent += "	sRoomId		VARCHAR(64)	NOT NULL, ";
                        Define.queryCreateChatContent += "	sTalkerId	VARCHAR(64)	NOT NULL, ";
                        Define.queryCreateChatContent += "	sTalkerName	VARCHAR(256)	NOT NULL, ";
                        Define.queryCreateChatContent += "	sTalkerNickName	VARCHAR(128)	NOT NULL, ";
                        Define.queryCreateChatContent += "	sTalkDate	VARCHAR(32)	NOT NULL, ";
                        Define.queryCreateChatContent += "	sTalkerContent	TEXT		NOT NULL, ";
                        Define.queryCreateChatContent += "	sSendComplete	CHAR(1)		NOT NULL, ";
                        Define.queryCreateChatContent += "	sUnReadUserIds	TEXT		NOT NULL, ";
                        Define.queryCreateChatContent += "	sReserved		TEXT		NOT NULL ";
                        Define.queryCreateChatContent += "); ";
                }
                if ( Define.queryCreateMessageContent.equals( "" ) )
                {
                        Define.queryCreateMessageContent += "CREATE TABLE IF NOT EXISTS tblMessage ";
                        Define.queryCreateMessageContent += "( ";
                        Define.queryCreateMessageContent += "	sMsgId		VARCHAR(79)	NOT NULL, ";
                        Define.queryCreateMessageContent += "	sSenderId	VARCHAR(64)	NOT NULL, ";
                        Define.queryCreateMessageContent += "	sSenderName	VARCHAR(256)	NOT NULL, ";
                        Define.queryCreateMessageContent += "	sSenderPart	VARCHAR(256)	NOT NULL, ";
                        Define.queryCreateMessageContent += "	sDate		CHAR(14)	NOT NULL, ";
                        Define.queryCreateMessageContent += "	cIsSend		CHAR(1)		NOT NULL, "; // N:receivev Y:send
                        Define.queryCreateMessageContent += "	sSubject	VARCHAR(1024)	NOT NULL, ";
                        Define.queryCreateMessageContent += "	sImage		VARCHAR(512)	NOT NULL, ";
                        Define.queryCreateMessageContent += "	cRead		CHAR(1)		NOT NULL, "; // Y:read
                        Define.queryCreateMessageContent += "	sContent	TEXT		NOT NULL, ";
                        Define.queryCreateMessageContent += "	sAttach		TEXT		NOT NULL, ";
                        Define.queryCreateMessageContent += "	sReceivers	TEXT		NOT NULL ";
                        Define.queryCreateMessageContent += ") ";
                }
                if ( Define.queryCreateCallLog.equals("") )
                {
						Define.queryCreateCallLog += "CREATE TABLE IF NOT EXISTS tblCallLog";
						Define.queryCreateCallLog += "(";
						Define.queryCreateCallLog += "	sCallId		CHAR(14)	NOT NULL, ";
						Define.queryCreateCallLog += "	sNumber		VARCHAR(16)	NOT NULL, ";
						Define.queryCreateCallLog += "	sName		VARCHAR(64)	NOT NULL, ";
						Define.queryCreateCallLog += "	sCallType	CHAR(1)		NOT NULL, ";
						Define.queryCreateCallLog += "	sDuration	VARCHAR(6)	NOT NULL, ";
						Define.queryCreateCallLog += "	sUserId		VARCHAR(64) ";
						Define.queryCreateCallLog += "); ";
                }
        }

        @Override
        public void onCreate( SQLiteDatabase db )
        {
                try
                {
                        db.execSQL( Define.queryCreateConfig );
                        db.execSQL( Define.queryCreateAlarm );
                        db.execSQL( Define.queryCreateChatRoomInfo );
                        db.execSQL( Define.queryCreateChatContent );
                        db.execSQL( Define.queryCreateFavorite );
                        db.execSQL( Define.queryCreateMessageContent );
                        db.execSQL( Define.queryCreateCallLog );
                        android.util.Log.d( "DBHelper", "CreateMessage" );
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
        }

        @Override
        public void onUpgrade( SQLiteDatabase db, int oldVersion, int newVersion )
        {
                try
                {
                        db.execSQL( "DROP TABLE tblAlarm" );
                }
                catch ( Exception e )
                {}
                try
                {
                        db.execSQL( "DROP TABLE tblChatRoomInfo" );
                }
                catch ( Exception e )
                {}
                try
                {
                        db.execSQL( "DROP TABLE tblChatContent" );
                }
                catch ( Exception e )
                {}
                try
                {
                        db.execSQL( "DROP TABLE tblConfig" );
                }
                catch ( Exception e )
                {}
                try
                {
                        db.execSQL( "DROP TABLE tblFavorite" );
                }
                catch ( Exception e )
                {}
                try
                {
                        onCreate( db );
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
        }
}
