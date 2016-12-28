package kr.co.ultari.atsmart.basic.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class StringUtil {
        public static short getChatType( String content )
        {
                if ( content.indexOf( "ATTACH://" ) < 0 && content.indexOf( "FILE://" ) < 0 ) return Define.TYPE_CHAT;
                else
                {
                        if ( content.indexOf( '.' ) < 0 ) return Define.TYPE_FILE;
                        String ext = content.substring( content.lastIndexOf( '.' ) + 1 ).toLowerCase();
                        if ( ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" ) || ext.equalsIgnoreCase( "png" )
                                        || ext.equalsIgnoreCase( "bmp" ) ) return Define.TYPE_IMAGE;
                        else if ( ext.equalsIgnoreCase( "mp4" ) || ext.equalsIgnoreCase( "avi" ) || ext.equalsIgnoreCase( "mpeg" )
                                        || ext.equalsIgnoreCase( "mpg" ) || ext.equalsIgnoreCase( "mov" ) ) return Define.TYPE_MOVIE;
                        else if ( ext.equalsIgnoreCase( "mp3" ) || ext.equalsIgnoreCase( "wav" ) || ext.equalsIgnoreCase( "au" ) ) return Define.TYPE_AUDIO;
                        else if ( ext.equalsIgnoreCase( "txt" ) ) return Define.TYPE_TEXT;
                        else if ( ext.equalsIgnoreCase( "doc" ) || ext.equalsIgnoreCase( "docx" ) ) return Define.TYPE_DOC;
                        else if ( ext.equalsIgnoreCase( "xls" ) || ext.equalsIgnoreCase( "xlsx" ) ) return Define.TYPE_EXCEL;
                        else if ( ext.equalsIgnoreCase( "ppt" ) || ext.equalsIgnoreCase( "pptx" ) ) return Define.TYPE_PPT;
                        else if ( ext.equalsIgnoreCase( "pdf" ) ) return Define.TYPE_PDF;
                        else if ( ext.equalsIgnoreCase( "hwp" ) || ext.equalsIgnoreCase( "x-hwp" ) ) return Define.TYPE_HWP;
                        else return Define.TYPE_FILE;
                }
        }

        public static String getChatTypeString( String content )
        {
                short type = getChatType( content );
                Log.d( "ChatTypeString", type + ":" + content );
                switch ( type )
                {
                case Define.TYPE_IMAGE :
                        return "사진";
                case Define.TYPE_AUDIO :
                        return "음악";
                case Define.TYPE_DOC :
                        return "문서파일";
                case Define.TYPE_EXCEL :
                        return "엑셀파일";
                case Define.TYPE_FILE :
                        return "파일";
                case Define.TYPE_HWP :
                        return "한글파일";
                case Define.TYPE_MOVIE :
                        return "영상";
                case Define.TYPE_PDF :
                        return "PDF 파일";
                case Define.TYPE_PPT :
                        return "파워포인트";
                case Define.TYPE_TEXT :
                        return "텍스트파일";
                default :
                        return content;
                }
        }

        public static int getByteLength( String str )
        {
                int strLength = 0;
                char tempChar[] = new char[str.length()];
                for ( int i = 0; i < tempChar.length; i++ )
                {
                        tempChar[i] = str.charAt( i );
                        if ( tempChar[i] < 128 ) strLength++;
                        else strLength += 2;
                }
                return strLength;
        }

        public static String getNamePosition( String str )
        {
                ArrayList<String> ar = new ArrayList<String>();
                String nowStr = "";
                if ( str == null ) return nowStr;
                for ( int i = 0; i < str.length(); i++ )
                {
                        if ( str.charAt( i ) == '#' )
                        {
                                ar.add( nowStr );
                                nowStr = "";
                        }
                        else if ( i == (str.length() - 1) )
                        {
                                nowStr += str.charAt( i );
                                ar.add( nowStr );
                        }
                        else
                        {
                                nowStr += str.charAt( i );
                        }
                }
                if ( ar.size() >= 2 )
                {
                        return (ar.get( 0 ) + " " + ar.get( 1 )).trim();
                }
                else if ( ar.size() == 1 )
                {
                        return ar.get( 0 );
                }
                return nowStr;
        }

        public static String setDataType( String msgId, String fileName, String fileSize, String imgWidth, String imgHeight )
        {
                String resultPath = "ATTACH://";
                if ( fileName.indexOf( '.' ) < 0 ) resultPath += fileSize + "/" + fileName;
                else
                {
                        String ext = fileName.substring( fileName.lastIndexOf( '.' ) + 1 );
                        if ( ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" ) || ext.equalsIgnoreCase( "png" )
                                        || ext.equalsIgnoreCase( "bmp" ) ) resultPath += imgWidth + ":" + imgHeight + "/" + fileName;
                        else resultPath += fileSize + "/" + fileName;
                }
                return resultPath;
        }

        public static String getArea( String str )
        {
                ArrayList<String> ar = new ArrayList<String>();
                String nowStr = "";
                for ( int i = 0; i < str.length(); i++ )
                {
                        if ( str.charAt( i ) == '#' )
                        {
                                ar.add( nowStr );
                                nowStr = "";
                        }
                        else if ( i == (str.length() - 1) )
                        {
                                nowStr += str.charAt( i );
                                ar.add( nowStr );
                        }
                        else
                        {
                                nowStr += str.charAt( i );
                        }
                }
                if ( ar.size() >= 2 )
                {
                        return ar.get( 2 ).trim();
                }
                else
                {
                        return "";
                }
        }

        public static String getName( String str )
        {
                if ( str.indexOf( "#" ) > 0 )
                {
                        return str.substring( 0, str.indexOf( "#" ) );
                }
                else return str;
        }

        public static String getNickName( String nickName, int icon )
        {
                if ( nickName == null || nickName.equals( "" ) ) return "";
                else return nickName;
        }

        public static String getStatus( Context context, int icon )
        {
                String result = "(PC " + context.getString( R.string.state ) + " : ";
                if ( icon == 0 ) result += context.getString( R.string.offline );
                else if ( icon == 1 ) result += context.getString( R.string.online );
                else if ( icon == 2 ) result += context.getString( R.string.leftSeat );
                else if ( icon == 5 ) result += context.getString( R.string.busy );
                else if ( icon == 4 ) result += context.getString( R.string.lineEngaged );
                else if ( icon == 3 ) result += context.getString( R.string.meeting );
                result += ")";
                return result;
        }

        public static String arrangeNamesByIds( String userNames, String userIds )
        {
                HashMap<String, String> map = new HashMap<String, String>();
                StringTokenizer st1 = new StringTokenizer( userIds, "," );
                StringTokenizer st2 = new StringTokenizer( userNames, "," );
                while ( st1.hasMoreTokens() && st2.hasMoreTokens() )
                {
                        String id = st1.nextToken();
                        String name = st2.nextToken();
                        map.put( id, name );
                }
                ArrayList<String> ar = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer( userIds, "," );
                while ( st.hasMoreTokens() )
                {
                        String addItem = st.nextToken();
                        boolean added = false;
                        for ( int i = 0; i < ar.size(); i++ )
                        {
                                if ( addItem.compareTo( ar.get( i ) ) < 0 )
                                {
                                        ar.add( i, addItem );
                                        added = true;
                                        break;
                                }
                                else if ( addItem.compareTo( ar.get( i ) ) == 0 )
                                {
                                        added = true;
                                        break;
                                }
                        }
                        if ( added == false ) ar.add( addItem );
                }
                for ( int i = 0; i < ar.size(); i++ )
                {
                        Log.i( "AtSmart", "ArrangedId(" + i + ") : " + ar.get( i ) );
                }
                String retStr = "";
                for ( int i = 0; i < ar.size(); i++ )
                {
                        if ( !retStr.equals( "" ) ) retStr += ",";
                        retStr += map.get( ar.get( i ) );
                }
                return retStr;
        }

        public static ArrayList<String> toArray( String joiners )
        {
                ArrayList<String> ar = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer( joiners, "," );
                while ( st.hasMoreTokens() )
                {
                        ar.add( st.nextToken() );
                }
                return ar;
        }

        public static String arrange( String joiners )
        {
                ArrayList<String> ar = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer( joiners, "," );
                while ( st.hasMoreTokens() )
                {
                        String addItem = st.nextToken();
                        boolean added = false;
                        for ( int i = 0; i < ar.size(); i++ )
                        {
                                if ( addItem.compareTo( ar.get( i ) ) < 0 )
                                {
                                        ar.add( i, addItem );
                                        added = true;
                                        break;
                                }
                                else if ( addItem.compareTo( ar.get( i ) ) == 0 )
                                {
                                        added = true;
                                        break;
                                }
                        }
                        if ( added == false ) ar.add( addItem );
                }
                StringBuffer sb = new StringBuffer();
                for ( int i = 0; i < ar.size(); i++ )
                {
                        if ( sb.length() > 0 ) sb.append( "," );
                        sb.append( ar.get( i ) );
                }
                return sb.toString();
        }

        public static String[] getOtherIds( String joiners )
        {
                return getOtherIds( joiners, null );
        }

        public static String makeString( String[] args )
        {
                String retStr = "";
                for ( int i = 0; i < args.length; i++ )
                {
                        if ( !args[i].equals( "" ) )
                        {
                                if ( !retStr.equals( "" ) ) retStr += ",";
                                retStr += args[i];
                        }
                }
                return retStr;
        }

        public static String[] getOtherNames( String joiners, String tId )
        {
                ArrayList<String> ar = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer( joiners, "," );
                while ( st.hasMoreTokens() )
                {
                        String addItem = st.nextToken();
                        addItem = addItem.trim();
                        if ( addItem.equals( tId ) ) continue;
                        boolean added = false;
                        for ( int i = 0; i < ar.size(); i++ )
                        {
                                if ( ar.get( i ).compareTo( addItem ) > 0 )
                                {
                                        ar.add( i, addItem );
                                        added = true;
                                        break;
                                }
                        }
                        if ( added == false ) ar.add( addItem );
                }
                String[] array = new String[ar.size()];
                for ( int i = 0; i < ar.size(); i++ )
                {
                        array[i] = ar.get( i );
                }
                return array;
        }

        public static String[] getOtherIds( String joiners, String tId )
        {
                ArrayList<String> ar = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer( joiners, "," );
                while ( st.hasMoreTokens() )
                {
                        String addItem = st.nextToken();
                        addItem = addItem.trim();
                        if ( addItem.equals( tId ) ) continue;
                        boolean added = false;
                        for ( int i = 0; i < ar.size(); i++ )
                        {
                                if ( ar.get( i ).compareTo( addItem ) > 0 )
                                {
                                        ar.add( i, addItem );
                                        added = true;
                                        break;
                                }
                        }
                        if ( added == false ) ar.add( addItem );
                }
                String[] array = new String[ar.size()];
                for ( int i = 0; i < ar.size(); i++ )
                {
                        array[i] = ar.get( i );
                }
                return array;
        }

        public static int getChatRoomCount( String joiners )
        {
                // try
                // {
                if ( joiners == null ) return 0;
                String[] ar = joiners.split( "," );
                return ar.length;
                /*
                 * }
                 * catch(Exception e)
                 * {
                 * e.printStackTrace();
                 * }
                 * return 0;
                 */
        }

        // 2015-06-09
        public static String getChatRoomName( String joiners, String userIds )
        {
                // try
                // {
                ArrayList<String> ar = new ArrayList<String>();
                StringTokenizer st = new StringTokenizer( joiners, "," );
                StringTokenizer st2 = new StringTokenizer( userIds, "," ); // 2015-06-09
                while ( st.hasMoreTokens() )
                {
                        String addItem = st.nextToken();
                        String addItem2 = st2.nextToken(); // 2015-06-09
                        addItem = addItem.trim();
                        // 2015-06-09
                        if ( Define.getMyId( Define.mContext ).equals( addItem2 ) ) continue;
                        boolean added = false;
                        for ( int i = 0; i < ar.size(); i++ )
                        {
                                if ( ar.get( i ).compareTo( addItem ) > 0 )
                                {
                                        ar.add( i, addItem );
                                        added = true;
                                        break;
                                }
                        }
                        if ( added == false ) ar.add( addItem );
                }
                StringBuffer sb = new StringBuffer();
                for ( int i = 0; i < ar.size(); i++ )
                {
                        if ( sb.length() > 0 ) sb.append( "," );
                        sb.append( ar.get( i ) );
                }
                return sb.toString();
                /*
                 * }
                 * catch(Exception e)
                 * {
                 * e.printStackTrace();
                 * }
                 * return "";
                 */
        }

        public static String getNoEnterStr( String str )
        {
                str = str.replaceAll( "\r", "" );
                str = str.replaceAll( "\n", " " );
                return str;
        }

        public static String getDateStr( String str )
        {
                if ( str.length() < 12 ) return str;
                String retStr = "";
                try
                {
                        String strYear = str.substring( 0, 4 );
                        String strMonth = str.substring( 4, 6 );
                        String strDay = str.substring( 6, 8 );
                        String strHour = str.substring( 8, 10 );
                        String strMinute = str.substring( 10, 12 );
                        Calendar cal = Calendar.getInstance();
                        if ( cal.get( Calendar.YEAR ) == Integer.parseInt( strYear ) && (cal.get( Calendar.MONTH ) + 1) == Integer.parseInt( strMonth )
                                        && cal.get( Calendar.DAY_OF_MONTH ) == Integer.parseInt( strDay ) )
                        {
                                if ( Integer.parseInt( strHour ) > 12 )
                                {
                                        int retHour = Integer.parseInt( strHour ) - 12;
                                        if ( retHour < 10 ) retStr = Define.mContext.getString( R.string.pm ) + " 0" + retHour + ":" + strMinute;
                                        else retStr = Define.mContext.getString( R.string.pm ) + " " + retHour + ":" + strMinute;
                                }
                                else if ( Integer.parseInt( strHour ) == 12 )
                                {
                                        retStr = Define.mContext.getString( R.string.pm ) + " 12:" + strMinute;
                                }
                                else
                                {
                                        int retHour = Integer.parseInt( strHour );
                                        if ( retHour < 10 ) retStr = Define.mContext.getString( R.string.am ) + " 0" + retHour + ":" + strMinute;
                                        else retStr = Define.mContext.getString( R.string.am ) + " " + retHour + ":" + strMinute;
                                }
                        }
                        else
                        {
                                retStr = strYear + "." + strMonth + "." + strDay + " ";
                        }
                        /*
                         * else if ( cal.get(Calendar.YEAR) == Integer.parseInt(strYear) )
                         * {
                         * retStr = strMonth + "/" + strDay + " ";
                         * if ( Integer.parseInt(strHour) > 12 )
                         * {
                         * int retHour = Integer.parseInt(strHour) - 12;
                         * if ( retHour < 10 )
                         * retStr += "PM 0" + retHour + ":" + strMinute;
                         * else
                         * retStr += "PM " + retHour + ":" + strMinute;
                         * }
                         * else if ( Integer.parseInt(strHour) == 12 )
                         * {
                         * retStr += "PM 12:" + strMinute;
                         * }
                         * else
                         * {
                         * int retHour = Integer.parseInt(strHour);
                         * if ( retHour < 10 )
                         * retStr += "AM 0" + retHour + ":" + strMinute;
                         * else
                         * retStr += "AM " + retHour + ":" + strMinute;
                         * }
                         * }
                         * else
                         * {
                         * retStr = strYear + "/" + strMonth + "/ " + strDay + " ";
                         * if ( Integer.parseInt(strHour) > 12 )
                         * {
                         * int retHour = Integer.parseInt(strHour) - 12;
                         * if ( retHour < 10 )
                         * retStr += "PM 0" + retHour + ":" + strMinute;
                         * else
                         * retStr += "PM " + retHour + ":" + strMinute;
                         * }
                         * else if ( Integer.parseInt(strHour) == 12 )
                         * {
                         * retStr += "PM 12:" + strMinute;
                         * }
                         * else
                         * {
                         * int retHour = Integer.parseInt(strHour);
                         * if ( retHour < 10 )
                         * retStr += "AM 0" + retHour + ":" + strMinute;
                         * else
                         * retStr += "AM " + retHour + ":" + strMinute;
                         * }
                         * }
                         */
                }
                catch ( Exception e )
                {
                        retStr = str;
                }
                return retStr;
        }

        public static String getTimeStr( String str )
        {
                if ( str.length() < 12 ) return str;
                String retStr = "";
                String strHour = str.substring( 8, 10 );
                String strMinute = str.substring( 10, 12 );
                if ( Integer.parseInt( strHour ) > 12 )
                {
                        int retHour = Integer.parseInt( strHour ) - 12;
                        if ( retHour < 10 ) retStr += "오후 0" + retHour + ":" + strMinute;
                        else retStr += "오후 " + retHour + ":" + strMinute;
                }
                else if ( Integer.parseInt( strHour ) == 12 )
                {
                        retStr += "오후 12:" + strMinute;
                }
                else
                {
                        int retHour = Integer.parseInt( strHour );
                        if ( retHour < 10 ) retStr += "오전 0" + retHour + ":" + strMinute;
                        else retStr += "오전 " + retHour + ":" + strMinute;
                }
                return retStr;
        }

        public static String getYMD( Context context, String str )
        {
                if ( str.length() < 12 ) return str;
                String strYear = str.substring( 0, 4 );
                String strMonth = str.substring( 4, 6 );
                String strDay = str.substring( 6, 8 );
                return strYear + context.getString( R.string.year ) + strMonth + context.getString( R.string.month ) + strDay
                                + context.getString( R.string.day );
        }

        public static String getNotifyTime( String str )
        {
                if ( str.length() < 12 ) return str;
                String strYear = str.substring( 0, 4 );
                String strMonth = str.substring( 4, 6 );
                String strDay = str.substring( 6, 8 );
                String strHour = str.substring( 8, 10 );
                String strMinute = str.substring( 10, 12 );
                return strYear + "-" + strMonth + "-" + strDay + " " + strHour + ":" + strMinute;
        }

        public static String getDateDay( String date, String dateType ) throws Exception
        {
                String day = "";
                SimpleDateFormat dateFormat = new SimpleDateFormat( dateType );
                Date nDate = dateFormat.parse( date );
                Calendar cal = Calendar.getInstance();
                cal.setTime( nDate );
                int dayNum = cal.get( Calendar.DAY_OF_WEEK );
                switch ( dayNum )
                {
                case 1 :
                        day = "일";
                        break;
                case 2 :
                        day = "월";
                        break;
                case 3 :
                        day = "화";
                        break;
                case 4 :
                        day = "수";
                        break;
                case 5 :
                        day = "목";
                        break;
                case 6 :
                        day = "금";
                        break;
                case 7 :
                        day = "토";
                        break;
                }
                return day;
        }

        public static String getNowDateTime()
        {
                Calendar cal = Calendar.getInstance();
                int year = cal.get( Calendar.YEAR );
                int month = cal.get( Calendar.MONTH ) + 1;
                int day = cal.get( Calendar.DAY_OF_MONTH );
                int hour = cal.get( Calendar.HOUR_OF_DAY );
                int minute = cal.get( Calendar.MINUTE );
                int second = cal.get( Calendar.SECOND );
                int milliSecond = cal.get( Calendar.MILLISECOND );
                String retStr = year + "";
                if ( month < 10 ) retStr += "0" + month;
                else retStr += month;
                if ( day < 10 ) retStr += "0" + day;
                else retStr += day;
                if ( hour < 10 ) retStr += "0" + hour;
                else retStr += hour;
                if ( minute < 10 ) retStr += "0" + minute;
                else retStr += minute;
                if ( second < 10 ) retStr += "0" + second;
                else retStr += second;
                if ( milliSecond > 99 ) retStr += Integer.toString( milliSecond ).substring( 0, 2 );
                else if ( milliSecond > 9 && milliSecond < 100 ) retStr += Integer.toString( milliSecond );
                else if ( milliSecond < 10 ) retStr += "0" + Integer.toString( milliSecond );
                return retStr;
        }

        public static String getUnderDateString( int dayInterval )
        {
                Calendar cal = Calendar.getInstance();
                cal.add( Calendar.DAY_OF_YEAR, dayInterval * -1 );
                int year = cal.get( Calendar.YEAR );
                int month = cal.get( Calendar.MONTH ) + 1;
                int day = cal.get( Calendar.DAY_OF_MONTH );
                int hour = cal.get( Calendar.HOUR_OF_DAY );
                int minute = cal.get( Calendar.MINUTE );
                int second = cal.get( Calendar.SECOND );
                String retStr = year + "";
                if ( month < 10 ) retStr += "0" + month;
                else retStr += month;
                if ( day < 10 ) retStr += "0" + day;
                else retStr += day;
                if ( hour < 10 ) retStr += "0" + hour;
                else retStr += hour;
                if ( minute < 10 ) retStr += "0" + minute;
                else retStr += minute;
                if ( second < 10 ) retStr += "0" + second;
                else retStr += second;
                return retStr;
        }

        public static String[] parseName( String name )
        {
                String nowStr = "";
                if ( name == null ) return null;
                ArrayList<String> ar = new ArrayList<String>();
                for ( int i = 0; i < name.length(); i++ )
                {
                        if ( name.charAt( i ) == '#' )
                        {
                                ar.add( nowStr );
                                nowStr = "";
                        }
                        else
                        {
                                nowStr += name.charAt( i );
                        }
                }
                ar.add( nowStr );
                String[] retAr = new String[ar.size()];
                for ( int i = 0; i < ar.size(); i++ )
                {
                        retAr[i] = ar.get( i );
                }
                return retAr;
        }

        public static String getNick( Context context, String nick, int icon )
        {
                if ( nick == null ) return "";
                else return nick;
        }

        public static String getFileSizeText( long len )
        {
                String s = len + "";
                int type = 0;
                if ( s.length() <= 3 ) type = 0;
                else if ( s.length() <= 6 ) type = 1;
                else type = 2;
                String retStr = "";
                int comma = 0;
                int startPosition = 0;
                if ( type == 0 ) startPosition = s.length() - 1;
                else if ( type == 1 ) startPosition = s.length() - 4;
                else startPosition = s.length() - 7;
                for ( int i = startPosition; i >= 0; i-- )
                {
                        if ( (comma % 3) == 0 && retStr.length() > 0 ) retStr = "," + retStr;
                        retStr = s.charAt( i ) + retStr;
                        comma++;
                }
                if ( type == 0 ) retStr += " Bytes";
                else if ( type == 1 ) retStr += " KB";
                else retStr += " MB";
                return retStr;
        }

        public static String getWrapedString( String str, float maxWidth, Paint paint )
        {
                String retStr = "";
                String nowStr = "";
                for ( int i = 0; i < str.length(); i++ )
                {
                        if ( str.charAt( i ) == '\n' )
                        {
                                nowStr = "";
                        }
                        else
                        {
                                nowStr += str.charAt( i );
                        }
                        if ( paint.measureText( nowStr ) >= maxWidth )
                        {
                                retStr += "\n";
                                nowStr = "";
                        }
                        retStr += str.charAt( i );
                }
                return retStr;
        }

        public static String getCommaString( String str, float maxWidth, Paint paint )
        {
                String retStr = "";
                String nowStr = "";
                for ( int i = 0; i < str.length(); i++ )
                {
                        nowStr += str.charAt( i );
                        if ( paint.measureText( nowStr ) >= maxWidth )
                        {
                                break;
                        }
                        retStr += str.charAt( i );
                }
                if ( !retStr.equals( str ) ) return retStr + "...";
                else return str;
        }

        @SuppressLint( "NewApi" )
        @SuppressWarnings( "deprecation" )
        public static float getWidth( Context context )
        {
                Display display = (( WindowManager ) context.getSystemService( Context.WINDOW_SERVICE )).getDefaultDisplay();
                Point point = new Point();
                try
                {
                        display.getSize( point );
                }
                catch ( java.lang.NoSuchMethodError ignore )
                {
                        point.x = display.getWidth();
                        point.y = display.getHeight();
                }
                return point.x;
        }
        public static String number[] = { "010", "011", "019", "016", "017" };
        private static String link[] = { "http://", "www.", "mail", "010", "011", "019", "02", "051", "053", "032", "062", "042", "052", "044", "031", "033",
                        "043", "041", "063", "061", "054", "055", "064" };

        public static boolean getLinkState( String content )
        {
                for ( int i = link.length - 1; i >= 0; i-- )
                {
                        if ( content.contains( link[i] ) ) return true;
                }
                return false;
        }

        public static int getJoinersCount( String joiners )
        {
                StringTokenizer st = new StringTokenizer( joiners, "," );
                return st.countTokens();
        }

        public static String getParseJoinersName( Context context, String joiners )
        {
                StringTokenizer st = new StringTokenizer( joiners, "," );
                String res = "";
                int count = st.countTokens() + 1;
                Log.e( "TEST", "joiner:" + joiners + ", count:" + count );
                // joiner:김애라 사원,테스트102 대리,테스트103 대리, count:4
                if ( count == 1 ) // 2015-05-10, 2->1
                res = joiners + " (" + count + context.getString( R.string.people ) + ")";
                else if ( count > 1 )// 2015-05-10, 2->1
                {
                        for ( int i = 0; i < 1; i++ )
                                // 2015-05-10, 2->1
                                res += st.nextToken() + ",";
                        res = res.substring( 0, res.lastIndexOf( "," ) );
                        // if(count == 3)
                        // res += " (" + count + context.getString(R.string.people)+")";
                        // else if(count > 3)
                        // res += "...(" + count + context.getString(R.string.people)+")";
                        res += " " + context.getString( R.string.person_etc ) + " (" + count + context.getString( R.string.people ) + ")";
                }
                // name:김애라 사원,테스트102 대리...(4명)
                return res;
        }

        public static boolean startsWith( String key, String _value ) // 초성검색
        {
                if ( key == null ) return false;
                if ( _value.length() < key.length() ) return false;
                for ( int i = 0; i < key.length(); i++ )
                {
                        final char c;
                        if ( KoreanChar.isHangulChoseong( key.charAt( i ) ) && KoreanChar.isHangulSyllable( _value.charAt( i ) ) ) c = new KoreanChar(
                                        _value.charAt( i ), false ).getChoseong();
                        else if ( KoreanChar.isHangulCompatibilityChoseong( key.charAt( i ) ) && KoreanChar.isHangulSyllable( _value.charAt( i ) ) ) c = new KoreanChar(
                                        _value.charAt( i ), true ).getChoseong();
                        else c = _value.charAt( i );
                        if ( c != key.charAt( i ) ) return false;
                }
                return true;
        }

        public static String getMessageDate( String dateStr )
        {
                if ( dateStr.length() < 8 ) return dateStr;
                return dateStr.substring( 2, 4 ) + "." + dateStr.substring( 4, 6 ) + "." + dateStr.substring( 6, 8 );
        }
}
