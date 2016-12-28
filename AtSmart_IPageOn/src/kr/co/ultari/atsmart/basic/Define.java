package kr.co.ultari.atsmart.basic;

import java.net.Socket;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.service.NetworkStatusReceiver;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;

public class Define
{
        public static Context mContext;
        public static final String TAG = "/AtSmart/Define";
        public static String PRIVATE_SERVER_IP = "";
        public static String PRIVATE_SERVER_PORT = "0";
        public static String PUBLIC_SERVER_IP = "";
        public static String PUBLIC_SERVER_PORT = "0";
        public static String PRIVATE_PROXY_IP = "";
        public static String PRIVATE_PROXY_PORT = "0";
        public static String PUBLIC_PROXY_IP = "";
        public static String PUBLIC_PROXY_PORT = "0";
        public static boolean useFMCProvisioning = false;
        public static final short TYPE_CHAT = 0x00;
        public static final short TYPE_IMAGE = 0x01;
        public static final short TYPE_MOVIE = 0x02;
        public static final short TYPE_AUDIO = 0x03;
        public static final short TYPE_TEXT = 0x04;
        public static final short TYPE_FILE = 0x05;
        public static final short TYPE_EXCEL = 0x06;
        public static final short TYPE_PPT = 0x07;
        public static final short TYPE_DOC = 0x08;
        public static final short TYPE_PDF = 0x09;
        public static final short TYPE_HWP = 0x11;
        public static int ChatSelectRowsPerOneTime = 10;
        public static final String noticeUrl = "http://www.google.com";
        public static final String ADMIN_PIN = "----"; // 9876 //2016-12-19
        public static boolean USE_PIN_MAIN = false;
        public static String PIN_MAIN_CODE = "";
        public static String PIN_PWD_COUNT = ""; //2016-12-19
        public static int PIN_MAX_COUNT = 10; //2016-12-19 gone : -1
        public static final boolean usePhoneNumberLogin = false; 
        public static String ssoScheme = "smartplace://sso?key="; 

        public static String quotesLessSSID( String SSID )
        {
                if ( SSID != null )
                {
                        if ( SSID.length() > 3 )
                        {
                                char first = SSID.charAt( 0 );
                                char last = SSID.charAt( SSID.length() - 1 );
                                if ( first == '\"' && last == '\"' )
                                {
                                        return SSID.substring( 1, SSID.length() - 1 );
                                }
                                else return SSID;
                        }
                        else return SSID;
                }
                else return SSID;
        }

        public static void resetServerInfo( Context context )
        {
                /*
                 * Define.PRIVATE_SERVER_IP = Database.instance(context).selectConfig("PRIVATE_SERVER_IP");
                 * Define.PRIVATE_SERVER_PORT = Database.instance(context).selectConfig("PRIVATE_SERVER_PORT");
                 * Define.PUBLIC_SERVER_IP = Database.instance(context).selectConfig("PUBLIC_SERVER_IP");
                 * Define.PUBLIC_SERVER_PORT = Database.instance(context).selectConfig("PUBLIC_SERVER_PORT");
                 * Define.PRIVATE_PROXY_IP = Database.instance(context).selectConfig("PRIVATE_PROXY_IP");
                 * Define.PRIVATE_PROXY_PORT = Database.instance(context).selectConfig("PRIVATE_PROXY_PORT");
                 * Define.PUBLIC_PROXY_IP = Database.instance(context).selectConfig("PUBLIC_PROXY_IP");
                 * Define.PUBLIC_PROXY_PORT = Database.instance(context).selectConfig("PUBLIC_PROXY_PORT");
                 * if ( Define.PRIVATE_SERVER_IP.equals("") ) Define.PRIVATE_SERVER_IP = context.getString(R.string.PRIVATE_SERVER_IP);
                 * if ( Define.PRIVATE_SERVER_PORT.equals("") ) Define.PRIVATE_SERVER_PORT = context.getString(R.string.PRIVATE_SERVER_PORT);
                 * if ( Define.PUBLIC_SERVER_IP.equals("") ) Define.PUBLIC_SERVER_IP = context.getString(R.string.PUBLIC_SERVER_IP);
                 * if ( Define.PUBLIC_SERVER_PORT.equals("") ) Define.PUBLIC_SERVER_PORT = context.getString(R.string.PUBLIC_SERVER_PORT);
                 * if ( Define.PRIVATE_PROXY_IP.equals("") ) Define.PRIVATE_PROXY_IP = context.getString(R.string.PRIVATE_PROXY_IP);
                 * if ( Define.PRIVATE_PROXY_PORT.equals("") ) Define.PRIVATE_PROXY_PORT = context.getString(R.string.PRIVATE_PROXY_PORT);
                 * if ( Define.PUBLIC_PROXY_IP.equals("") ) Define.PUBLIC_PROXY_IP = context.getString(R.string.PUBLIC_PROXY_IP);
                 * if ( Define.PUBLIC_PROXY_PORT.equals("") ) Define.PUBLIC_PROXY_PORT = context.getString(R.string.PUBLIC_PROXY_PORT);
                 */
                Define.PRIVATE_SERVER_IP = context.getString( R.string.PRIVATE_SERVER_IP );
                Define.PRIVATE_SERVER_PORT = context.getString( R.string.PRIVATE_SERVER_PORT );
                Define.PUBLIC_SERVER_IP = context.getString( R.string.PUBLIC_SERVER_IP );
                Define.PUBLIC_SERVER_PORT = context.getString( R.string.PUBLIC_SERVER_PORT );
                Define.PRIVATE_PROXY_IP = context.getString( R.string.PRIVATE_PROXY_IP );
                Define.PRIVATE_PROXY_PORT = context.getString( R.string.PRIVATE_PROXY_PORT );
                Define.PUBLIC_PROXY_IP = context.getString( R.string.PUBLIC_PROXY_IP );
                Define.PUBLIC_PROXY_PORT = context.getString( R.string.PUBLIC_PROXY_PORT );
        }

        public static synchronized String getServerIp( Context context )
        {
                //if ( !NetworkStatusReceiver.isConnected( context ) ) return null; 
                String mode = Database.instance( context ).selectConfig( "NETWORKMODE" );
                if ( mode.equals( Integer.toString( Define.NETWORK_MODE_LTE ) ) )
                {
                         
                        return context.getString( R.string.PUBLIC_SERVER_IP );
                        //return Define.PUBLIC_SERVER_IP;
                }
                else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_WIFI ) ) )
                {
                         
                        return context.getString( R.string.PRIVATE_SERVER_IP );
                        //return Define.PRIVATE_SERVER_IP;
                }
                else
                {
                        if ( NetworkStatusReceiver.isWifi( context ) && NetworkStatusReceiver.isCompanyWifi( context ) )
                        {
                                 
                                return context.getString( R.string.PRIVATE_SERVER_IP );
                                //return Define.PRIVATE_SERVER_IP;
                        }
                        else
                        {
                                 
                                return context.getString( R.string.PUBLIC_SERVER_IP );
                                //return Define.PUBLIC_SERVER_IP;
                        }
                }
        }

        public synchronized static String getServerPort( Context context )
        {
                //if ( !NetworkStatusReceiver.isConnected( context ) ) return null; 2016-04-07
                String mode = Database.instance( context ).selectConfig( "NETWORKMODE" );
                if ( mode.equals( Integer.toString( Define.NETWORK_MODE_LTE ) ) )
                {
                         
                        return context.getString( R.string.PUBLIC_SERVER_PORT );
                        //return Define.PUBLIC_SERVER_PORT;
                }
                else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_WIFI ) ) )
                {
                         
                        return context.getString( R.string.PRIVATE_SERVER_PORT );
                        //return Define.PRIVATE_SERVER_PORT;
                }
                else
                {
                        if ( NetworkStatusReceiver.isWifi( context ) && NetworkStatusReceiver.isCompanyWifi( context ) )
                        {
                                 
                                return context.getString( R.string.PRIVATE_SERVER_PORT );
                                //return Define.PRIVATE_SERVER_PORT;
                        }
                        else
                        {
                                 
                                return context.getString( R.string.PUBLIC_SERVER_PORT );
                                //return Define.PUBLIC_SERVER_PORT;
                        }
                }
        }

        public static String getProxyIp( Context context )
        {
                //if ( !NetworkStatusReceiver.isConnected( context ) ) return null;
                String mode = Database.instance( context ).selectConfig( "NETWORKMODE" );
                if ( mode.equals( Integer.toString( Define.NETWORK_MODE_LTE ) ) )
                {
                         
                        return context.getString( R.string.PUBLIC_PROXY_IP );
                        //return Define.PUBLIC_PROXY_IP;
                }
                else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_WIFI ) ) )
                {
                         
                        return context.getString( R.string.PRIVATE_PROXY_IP );
                        //return Define.PRIVATE_PROXY_IP;
                }
                else
                {
                        if ( NetworkStatusReceiver.isWifi( context ) && NetworkStatusReceiver.isCompanyWifi( context ) )
                        {
                                 
                                return context.getString( R.string.PRIVATE_PROXY_IP );
                                //return Define.PRIVATE_PROXY_IP;
                        }
                        else
                        {
                                 
                                return context.getString( R.string.PUBLIC_PROXY_IP );
                                //return Define.PUBLIC_PROXY_IP;
                        }
                }
        }

        public synchronized static String getProxyPort( Context context )
        {
                //if ( !NetworkStatusReceiver.isConnected( context ) ) return null; 
                String mode = Database.instance( context ).selectConfig( "NETWORKMODE" );
                if ( mode.equals( Integer.toString( Define.NETWORK_MODE_LTE ) ) )
                {
                         
                        return context.getString( R.string.PUBLIC_PROXY_PORT );
                        //return Define.PUBLIC_PROXY_PORT;
                }
                else if ( mode.equals( Integer.toString( Define.NETWORK_MODE_WIFI ) ) )
                {
                         
                        return context.getString( R.string.PRIVATE_PROXY_PORT );
                        //return Define.PRIVATE_PROXY_PORT;
                }
                else
                {
                        if ( NetworkStatusReceiver.isWifi( context ) && NetworkStatusReceiver.isCompanyWifi( context ) )
                        {
                                 
                                return context.getString( R.string.PRIVATE_PROXY_PORT );
                                //return Define.PRIVATE_PROXY_PORT;
                        }
                        else
                        {
                                 
                                return context.getString( R.string.PUBLIC_PROXY_PORT );
                                //return Define.PUBLIC_PROXY_PORT;
                        }
                }
        }
        public static String deviceBrand = "";
        public static String cameraTempFilePath = "";
        public static String totalName = "";
        public static ConcurrentHashMap<String, String> rawIdMap = null;
        public static final String ucPresenceUrl = "http://211.190.4.85:8090/account.html?userId=leesh&page=1";
        public static final boolean useGcmPush = true;
        public static final boolean useFmcState = false;
        public static final boolean usePcState = true;
        public static final boolean usePhoneState = false;
        public static final boolean useIncomingCall = false;
        public static final boolean useMyTopPartVisible = false;
        public static final boolean useResetChatRoomNamePosition = false;
        public static final boolean useOldFileTransferProtocol = false;
        public static final boolean usePresenceTab = false;
        public static final boolean useManualLoginMenu = true;
        public static final boolean useOrgUserToContactSave = true;
        public static final boolean useChatRoomIconDivide = false;
        public static final boolean useNotificationLargeIcon = true;
        public static final boolean useFavoriteAdd = true;
        public static final boolean useRootingCheck = true; //루팅 
        public static final boolean useSecureCapture = true; //캡처
        public static final boolean useMessageSearch = true;
        public static final boolean useSendNoteMsg = false;
        public static final boolean useStatusPcIcon = false;
        public static final boolean useBackgroundImage = true;
        public static final boolean useLoginMode = false;
        public static final boolean useCustomToast = true;
        public static final boolean useOvalUserPic = true;
        public static final boolean useMyFolderEdit = false;
        public static final boolean useFileLog = false;
        public static final boolean support_file_send = false;
        public static final boolean useTrace = true;
        public static final boolean saveErrorLog = false;
        public static final boolean useUnicode = true;
        public static final boolean isSSL = true;
        public static final boolean useEmoticon = false;
        public static boolean isResetChatRoomNamePosition = false;
        public static boolean isMovingForward = false;
        public static int mLastVisitedPageIndex = 0;
        public static int SELECT_BACKGROUND_COLOR = 0;
        public static int NETWORK_MODE = 0;
        public static String LOGIN_MODE = "ID";
        public static String SELCT_BACKGROUND_MODE = "IMAGE";
        public static String NEW_VERSION = "1.0";
        public static String UPDATE_URL = "";
        public static final String VERSION = "5.0";
        public static String regid = "";
        public static boolean isFmcConnected = false;
        public static Typeface tfRegular = null;
        public static Typeface tfBold = null;
        public static Typeface tfMedium = null;
        public static Typeface tfLight = null;

        public static void fontInit()
        {
                tfRegular = Typeface.createFromAsset( mContext.getAssets(), "Regular.otf.mp3" );
                tfBold = Typeface.createFromAsset( mContext.getAssets(), "Bold.otf.mp3" );
                tfMedium = Typeface.createFromAsset( mContext.getAssets(), "Medium.otf.mp3" );
                tfLight = Typeface.createFromAsset( mContext.getAssets(), "Light.otf.mp3" );
        }
        public static ArrayList<Contact> contactArray = null;
        public static ConcurrentHashMap<String, Contact> contactMap = null;
        
        public static final int BASIC           = 0;
        public static final int MOORIM          = 1; 
        public static final int MODETOUR        = 2;
        public static final int POLICE          = 3;
        public static final int NEC             = 4; 
        public static final int SAMSUNG         = 5;
        public static final int UST             = 6; 
        public static final int NONGSHIM        = 7; 
        public static final int CU              = 8;
        public static final int REDCROSS        = 9;
        public static final int KEAD            = 10;
        public static final int ENERGY          = 11;
        public static final int CNUH            = 12;
        public static final int SAMYANG         = 13;
        public static final int KORAIL          = 14;
        public static final int SAEHA           = 15;
        public static final int GG              = 16;
        public static final int MBC             = 17;
        public static final int NAMDONG         = 18;
        public static final int EX              = 19;
        public static final int SSL92           = 20;
        public static final int DEMO            = 21;
        public static final int AMOTECH         = 22;
        public static final int IPAGEON         = 23;
        
        public static final int SET_COMPANY = IPAGEON;
        public static final String DB_NAME = "AtSmart.db";
        public static final int DB_VERSION = 10;
        private static String myId = "", myPw = "";
        public static boolean isBuddyAddMode = false;
        public static int selectedTreeNumber = 0;
        public static String push = "";
        public static String keyboard = "";
        public static String vibrator = "";
        public static String sound = "";
        public static int notoficationNumber = 1;
        public static int displayWidth = 400;
        public static int displayHeight = 800;
        public static final int AtSmartServiceFinished = 0x7f040002;
        public static final int AtSmartPushNotification = 1;
        private static final float DEFAULT_HDIP_DENSITY_SCALE = 1.5f;
        public static String chatSoundUri = "";
        public static String alarmSoundUri = "";
        public static String selectedFontSize = "";
        public static String requestPictureSizeChatRoom = "[100:100]";
        public static String requestPictureSizeShowNotification = "[100:100]";
        public static String requestPictureSizeAlertDialog = "[100:100]";
        public static String requestPictureSizeUserImageView = "[200:200]";
        public static String requestPictureSizeTreeItem = "[190:190]";
        public static String requestPictureSizeChatItem = "[200:200]";
        public static String queryCreateFavorite = "";
        public static String queryCreateConfig = "";
        public static String queryCreateAlarm = "";
        public static String queryCreateChatRoomInfo = "";
        public static String queryCreateChatContent = "";
        public static String queryCreateMessageContent = "";
        public static String queryCreateCallLog = "";
        public static boolean isVisible;
        
        public static boolean isCallPaused = false;

        public static boolean isScreenVisible( Context context )
        {
                // 스크린이 꺼져 있으면 false 리턴
                PowerManager pm = ( PowerManager ) context.getSystemService( Context.POWER_SERVICE );
                @SuppressWarnings( "deprecation" )
                boolean isScreenOn = pm.isScreenOn();
                if ( !isScreenOn ) return false;
                // 백그라운드 실행중인지 리턴
                ActivityManager am = ( ActivityManager ) context.getSystemService( Context.ACTIVITY_SERVICE );
                List<RunningTaskInfo> tasks = am.getRunningTasks( 1 );
                if ( !tasks.isEmpty() )
                {
                        if ( !tasks.get( 0 ).topActivity.getPackageName().equals( context.getPackageName() ) )
                        {
                                return false;
                        }
                }
                // MainActivity가 실행 중이면 true 리턴
                if ( Define.isVisible ) return true;
                else return false;
        }
        public static boolean isHomeMode;
        public static boolean isAddUserMode = false;
        public static String oldRoomUserId = "";
        public static String oldRoomUserName = "";
        public static String oldRoomId = "";
        public static Activity nowTopActivity = null;
        public static float tabWidth = 0;
        public static float tabHeight = 0;
        public static int NOTIFY_DEL = 0;
        public static int NOTIFY_READ = 1;
        public static int NOTIFY_CHAT = 2;
        public static int NOTIFY_NOTE = 3;
        public static int CONTACT_CHAT = 0;
        public static int CONTACT_CALL = 1;
        public static int CONTACT_DEL = 2;
        public static int CONTACT_CHECK = 3;
        public static int CONTACT_DETAIL = 4;
        public static int messageDisplayCount = 20;
        // SAMSUNG: BOOK:0, KEYPAD:4
        // GG: BOOK:4, KETPAD:0
        public static final int TAB_KEYPAD = 0;
        public static final int TAB_CONTACT = 1;
        public static final int TAB_USER_BUDDY = 2;
        public static final int TAB_ORGANIZATION = 3;
        public static final int TAB_CHAT = 4;
        public static final int TAB_BOOKMARK = 5;
        public static final int TAB_CALL_LOG = 6;
        public static final int TAB_MESSAGE = 7;
        public static final int TAB_NOTIFY = 8;
        public static final int TAB_SETTING = 9;
        public static final int pageCount = 10;
        public static int MAIN_TAB_HEIGHT = 205;
        public static int TREE_PART_ITEM_HEIGHT = 130;
        public static int TREE_USER_ITEM_HEIGHT = 190;
        public static float TREE_TEXT_SIZE = 14;
        public static float TREE_INFO_SIZE = 11;
        public static int TREE_NORMAL_TEXT_COLOR = 0xFF333333;
        public static int TREE_NORMAL_FOLDER_TEXT_COLOR = 0xFF333333;
        public static int TREE_INFO_TEXT_COLOR = 0xFF7B94C0;
        public static int TREE_SELECT_TEXT_COLOR = 0xFF333333;
        public static int TREE_SELECT_FOLDER_TEXT_COLOR = 0xFF74AFEA;
        public static int TREE_SELECT_INFO_COLOR = 0xFF7B94C0;
        public static int TREE_NORMAL_BACK_COLOR = 0xFFFFFFFF;
        public static int TREE_FOLDER_BACK_COLOR = 0xFF000000;
        public static int TREE_SELECT_BACK_COLOR = 0xFFE1EBF1;
        public static int TREE_SELECT_FOLDER_BACK_COLOR = 0xFFEDF5F9;
        public static int TREE_BORDER_COLOR = 0xFFFCFCFC; 
        public static int TREE_DEPTH_TAB_SIZE = 10; //2016-04-26
        public static final int NETWORK_MODE_LTE = 0;
        public static final int NETWORK_MODE_WIFI = 1;
        public static final int NETWORK_MODE_DISABLE = 2;
        public static final int CALL_TYPE_MODE_UDP = 0; //2016-12-20
        public static final int CALL_TYPE_MODE_TLS = 1; //2016-12-20
        public static final String UC_IDLE = "1";
        public static final String UC_RINGING = "2";
        public static final String UC_LINE_ENGAGED = "3";
        public static final String UC_OFFHOOK = "5";
        public static final String MENU_TITLE_CHAT = "MENU_CHAT";
        public static final String MENU_TITLE_INFO = "MENU_INFO";
        public static final String MENU_TITLE_CALL = "MENU_CALL";
        public static final String MENU_TITLE_ENTER = "MENU_ENTER";
        public static final String MENU_TITLE_DELETE = "MENU_DELETE";
        public static final String MENU_TITLE_FILE = "MENU_FILE";
        public static final int MENU_ID_CHAT = 0x00;
        public static final int MENU_ID_INFO = 0x01;
        public static final int MENU_ID_CALL = 0x02;
        public static final int MENU_ID_ENTER = 0x03;
        public static final int MENU_ID_DELETE = 0x04;
        public static final int MENU_ID_FILE = 0x05;
        public static final int MENU_ID_NOTE = 0x13;
        public static final int MENU_ID_FAVORITE = 0x14;
        public static final int MENU_ID_SAVE = 0x15;
        public static final int MENU_ID_INCOMING_CALL = 0x16; // 2015-05-18
        // myFolder edit
        public static final int MENU_ID_GROUP_ADD = 0x06;
        public static final int MENU_ID_GROUP_MOD = 0x07;
        public static final int MENU_ID_GROUP_DEL = 0x08;
        public static final int MENU_ID_SUB_GROUP_ADD = 0x09;
        public static final int MENU_ID_USER_ADD = 0x10;
        public static final int MENU_ID_USER_DEL = 0x11;
        public static final int MENU_ID_MOVE = 0x12;
        public static final int AM_ADD_BUDDY_PART = 0x01;
        public static final int AM_ADD_ORGANIZATION_PART = 0x02;
        public static final int AM_ADD_ORGANIZATION_USER = 0x36;
        public static final int AM_ADD_SEARCH = 0x03;
        public static final int AM_FIRST_EXPAND = 0x04;
        public static final int AM_SEARCH_COMPLETE = 0x05;
        public static final int AM_CLEAR_ITEM = 0x06;
        public static final int AM_SEARCH_END = 0x07;
        public static final int AM_SELECT_CHANGED = 0x08;
        public static final int AM_REDRAW_IMAGE = 0x09;
        public static final int AM_OPEN_CHAT = 0x10;
        public static final int AM_CLOSE_CHAT = 0x11;
        public static final int AM_REGISTER_CLIENT = 0x12;
        public static final int AM_UNREGISTER_CLIENT = 0x13;
        public static final int AM_NEW_CHAT = 0x14;
        public static final int AM_NEW_NOTIFY = 0x15;
        public static final int AM_UPLOAD_RESULT = 0x18;
        public static final int AM_UPLOAD_COMPLETE = 0x19;
        public static final int AM_DOWNLOAD_RESULT = 0x20;
        public static final int AM_CLEAR_POPUP = 0x21;
        public static final int AM_SELECT_TAB = 0x22;
        public static final int AM_HONEY_SEND = 0x24;
        public static final int AM_NO_USER = 0x25;
        public static final int AM_NO_PASSWORD = 0x26;
        public static final int AM_LOGIN = 0x27;
        public static final int AM_NICK = 0x28;
        public static final int AM_SEND_COMPLETE = 0x29;
        public static final int AM_REDRAW = 0x30;
        public static final int AM_POPUP_MENU = 0x31;
        public static final int AM_CONFIRM_YES = 0x32;
        public static final int AM_CONFIRM_NO = 0x33;
        public static final int AM_RESTART_SERVICE = 0x34;
        public static final int AM_NEW_VERSION = 0x35;
        public static final int AM_REFRESH = 0x36;
        public static final int AM_READ_COMPLETE = 0x37;
        public static final int AM_SEARCH = 0x38;
        public static final int AM_USER_INFO = 0x39;
        public static final int AM_SHOW_PROGRESS = 0x40;
        public static final int AM_HIDE_PROGRESS = 0x41;
        public static final int AM_ICON = 0x42;
        public static final int AM_STOP_SERVICE = 0x43;
        public static final int AM_MOORIM_CHECK = 0x48;
        public static final int AM_BLOCK_USER = 0x49;
        public static final int AM_NAME = 0x50;
        public static final int AM_USER_CHANGED = 0x51;
        public static final int AM_SEARCH_ITEM_FOCUS = 0x52;
        public static final int AM_SEARCH_REDRAW = 0x53;
        public static final int AM_SEARCH_INIT_DRAW = 0x54;
        public static final int AM_FILE_DELETE = 0;
        public static final int AM_COPY = 0;
        public static final int AM_DELETE = 1;
        public static final int AM_RESEND_CHAT = 2;
        public static final int AM_RESERVED = 0x47;
        public static final int AR_ADD_USER = 0x36;
        public static final int AM_MULTIUSER = 0x55;
        public static final int AM_SAEHA_INSTALL = 0x60;
        public static final int AM_TAB_REFRESH = 0x61;
        public static final int AM_CALL_STATE_OFFHOOK = 0x62;
        public static final int AM_CALL_STATE_IDLE = 0x63;
        public static final int AM_MAIN_GROUPTAB_REFRESH = 0x64;
        public static final int AM_TAB_SHOW = 0x65;
        public static final int AM_TAB_HIDE = 0x66;
        public static final int AM_UC_ICON = 0x67;
        public static final int AM_FAVORITE_ADD = 0x68;
        public static final int AM_FAVORITE_REMOVE = 0x69;
        public static final int AM_WIFI_MODE = 0x70;
        public static final int AM_LTE_MODE = 0x71;
        public static final int AM_LIST_SET_ID = 0x72;
        public static final int AM_MOVE_BUDDY = 0x56;
        public static final int AM_MOVE_MYFOLDER_ADDUSER = 0x57;
        public static final int AM_MYFOLDER_ADD_USER = 0x58;
        public static final int AM_LIST_CLEAR = 0x59;
        public static final int AM_COMPLETE = 0x61;
        public static final int AM_NEW_MESSAGE = 0x62;
        public static final int AM_RECEIVE_COMPLETE = 0x63;
        public static final int AM_RUN_ATTACH = 0x64;
        public static final int AM_DELETE_ALL = 0x65;
        public static final int AM_SHOW_WAIT = 0x66;
        public static final int AM_HIDE_WAIT = 0x67;
        public static final int AM_SEARCH_START = 0x68;
        public static final int AM_CALL_POPUP = 0x69;
        public static final int AM_ADD_BUDDY_USER = 0x70;
        public static final String AM_LOGOUT = "logout";
        public static HashMap<String, Bitmap> small_userImages = null;
        public static HashMap<String, Bitmap> userImages = null;
        public static HashMap<String, String> searchMobileOn = new HashMap<String, String>();
        public static HashMap<String, String> searchUcOn = new HashMap<String, String>();
        public static HashMap<String, String> mEmoticonMappingNameMap = new HashMap<String, String>();
        public static Integer[] mThumbIds = { R.drawable.emoticon_adore, R.drawable.emoticon_ah, R.drawable.emoticon_amazing, R.drawable.emoticon_angel,
                        R.drawable.emoticon_angry, R.drawable.emoticon_baby, R.drawable.emoticon_bad_egg, R.drawable.emoticon_baffle,
                        R.drawable.emoticon_batman, R.drawable.emoticon_beaten, R.drawable.emoticon_bigsmile, R.drawable.emoticon_bubblegum,
                        R.drawable.emoticon_bye_bye, R.drawable.emoticon_confuse, R.drawable.emoticon_cool, R.drawable.emoticon_crazy, R.drawable.emoticon_cry,
                        R.drawable.emoticon_cyclops, R.drawable.emoticon_doubt, R.drawable.emoticon_exciting, R.drawable.emoticon_eyes_droped,
                        R.drawable.emoticon_face_monkey, R.drawable.emoticon_face_panda, R.drawable.emoticon_greedy, R.drawable.emoticon_grin,
                        R.drawable.emoticon_happy, R.drawable.emoticon_horror, R.drawable.emoticon_hungry, R.drawable.emoticon_love, R.drawable.emoticon_mad,
                        R.drawable.emoticon_medic, R.drawable.emoticon_misdoubt, R.drawable.emoticon_mummy, R.drawable.emoticon_question,
                        R.drawable.emoticon_red, R.drawable.emoticon_sad, R.drawable.emoticon_shame, R.drawable.emoticon_shocked, R.drawable.emoticon_silent,
                        R.drawable.emoticon_sleep, R.drawable.emoticon_smile, R.drawable.emoticon_spiderman, R.drawable.emoticon_star,
                        R.drawable.emoticon_surrender, R.drawable.emoticon_tire, R.drawable.emoticon_tongue, R.drawable.emoticon_waaaht,
                        R.drawable.emoticon_what, R.drawable.emoticon_whist, R.drawable.emoticon_wink, R.drawable.emoticon_ghost, R.drawable.emoticon_folder,
                        R.drawable.emoticon_add, R.drawable.emoticon_dead };
        public static String[] mThumbNames = { "emoticon_adore", "emoticon_ah", "emoticon_amazing", "emoticon_angel", "emoticon_angry", "emoticon_baby",
                        "emoticon_bad_egg", "emoticon_baffle", "emoticon_batman", "emoticon_beaten", "emoticon_bigsmile", "emoticon_bubblegum",
                        "emoticon_bye_bye", "emoticon_confuse", "emoticon_cool", "emoticon_crazy", "emoticon_cry", "emoticon_cyclops", "emoticon_doubt",
                        "emoticon_exciting", "emoticon_eyes_droped", "emoticon_face_monkey", "emoticon_face_panda", "emoticon_greedy", "emoticon_grin",
                        "emoticon_happy", "emoticon_horror", "emoticon_hungry", "emoticon_love", "emoticon_mad", "emoticon_medic", "emoticon_misdoubt",
                        "emoticon_mummy", "emoticon_question", "emoticon_red", "emoticon_sad", "emoticon_shame", "emoticon_shocked", "emoticon_silent",
                        "emoticon_sleep", "emoticon_smile", "emoticon_spiderman", "emoticon_star", "emoticon_surrender", "emoticon_tire", "emoticon_tongue",
                        "emoticon_waaaht", "emoticon_what", "emoticon_whist", "emoticon_wink", "emoticon_ghost", "emoticon_folder", "emoticon_add",
                        "emoticon_dead" };

        public static String getPartNameByUserId( String name )
        {
                String[] ar = StringUtil.parseName( name );
                if ( ar.length > 2 ) return ar[2];
                return "";
        }

        public static void clearUserImages()
        {
                TRACE( "ClearUsersImage" );
                if ( userImages != null )
                {
                        /*
                         * Iterator<Bitmap> it = userImages.values().iterator();
                         * while ( it.hasNext() )
                         * {
                         * Bitmap b = it.next();
                         * it.remove();
                         * if ( b != null )
                         * {
                         * b.recycle();
                         * b = null;
                         * }
                         * }
                         */
                        userImages.clear();
                        userImages = null;
                }
                if ( small_userImages != null )
                {
                        Iterator<Bitmap> it = small_userImages.values().iterator();
                        while ( it.hasNext() )
                        {
                                Bitmap b = it.next();
                                if ( b != null )
                                {
                                        b.recycle();
                                        b = null;
                                }
                        }
                        small_userImages.clear();
                        small_userImages = null;
                }
        }

        public static Bitmap getSmallBitmap( String id )
        {
                if ( small_userImages == null ) small_userImages = new HashMap<String, Bitmap>();
                if ( small_userImages.get( id ) == null ) return null;
                else return small_userImages.get( id );
        }

        public static void setSmallBitmap( String id, Bitmap bmp )
        {
                if ( small_userImages == null ) small_userImages = new HashMap<String, Bitmap>();
                small_userImages.put( id, bmp );
        }

        public static void setBitmap( String id, Bitmap bmp )
        {
                if ( userImages == null )
                {
                        userImages = new HashMap<String, Bitmap>();
                }
                userImages.put( id, bmp );
        }

        public static void removeBitmap( String id )
        {
                TRACE( "RemoveUserImage : " + id );
                Bitmap bmp = userImages.get( id );
                if ( bmp != null )
                {
                        bmp.recycle();
                        bmp = null;
                }
                userImages.remove( id );
        }

        public static Bitmap getBitmap( String id )
        {
                if ( userImages == null )
                {
                        userImages = new HashMap<String, Bitmap>();
                }
                if ( userImages.get( id ) == null ) return null;
                else return userImages.get( id );
        }

        public static String getMyId( Context context )
        {
                if ( !myId.equals( "" ) )
                {
                        return myId;
                }
                myId = Database.instance( context ).selectConfig( "USERID" );
                return myId;
        }

        public static String getMyId()
        {
                if ( !myId.equals( "" ) )
                {
                        return myId;
                }
                return "";
        }

        public static String getMyPw( Context context )
        {
                if ( !myPw.equals( "" ) )
                {
                        return myPw;
                }
                myPw = Database.instance( context ).selectConfig( "USERPASSWORD" );
                return myPw;
        }

        public static String getRetain( Context context )
        {
                return Database.instance( context ).selectConfig( "RETAIN" );
        }

        public static void setMyId( String id )
        {
                myId = id;
        }

        public static void setMyPW( String pw )
        {
                myPw = pw;
        }

        public static void setMyNameWithInfo( String name )
        {
                totalName = name;
        }

        public static String getMyName()
        {
                return Database.instance( mContext ).selectConfig( "USERNAME" );
        }

        public static String getMyNickName()
        {
                return Database.instance( mContext ).selectConfig( "USERNICKNAME" );
        }

        public static void setMyNickName( String nick )
        {
                if ( nick == null ) nick = "";
                Log.d( "SetMyNickName", nick );
                Database.instance( mContext ).updateConfig( "USERNICKNAME", nick );
        }

        public static int DPFromPixel( int pixel )
        {
                float scale = mContext.getResources().getDisplayMetrics().density;
                return ( int ) (pixel / DEFAULT_HDIP_DENSITY_SCALE * scale);
        }

        public static int PixelFromDP( int DP )
        {
                float scale = mContext.getResources().getDisplayMetrics().density;
                return ( int ) (DP / scale * DEFAULT_HDIP_DENSITY_SCALE);
        }

        public static float pixelsToSp( Float px )
        {
                float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
                return px / scaledDensity;
        }

        public static float spToPixels( float sp )
        {
                float scaledDensity = mContext.getResources().getDisplayMetrics().scaledDensity;
                return sp * scaledDensity;
        }

        public static void setContext( Context context )
        {
                mContext = context;
        }

        public static Context getContext()
        {
                return mContext;
        }

        public static void TRACE( String s )
        {
                if ( !Define.useTrace ) return;
                android.util.Log.i( TAG, s );
        }

        public static void EXCEPTION( Throwable e )
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
        public static final String TITLE = "title";
        public static final String CONTENT = "content";
        public static final String USERIDS = "userIds";
        public static final String USERID = "userId";
        public static final String USERNAMES = "userNames";
        public static final String USERNAME = "userName";
        public static final String TALKID = "talkId";
        public static final String ROOMID = "RoomId";
        public static final String CHATID = "ChatId";
        public static final String SENDMSG = "mySender.app.sendmessage";
        public static final String NAMES = "name";
        public static final String MSGID = "msgId";
        public static final String NICK = "NICK";
        public static final String MESSAGE = "MESSAGE";
        public static final String MESSAGEID = "MESSAGEID";
        public static final String MSG_NEW_CHAT = "msg_newChat_broadcast";
        public static final String MSG_CHAT = "msg_chat_broadcast";
        public static final String MSG_NOTIFY = "msg_notify_broadcast";
        public static final String MSG_MY_NAME = "msg_my_name_broadcast";
        public static final String MSG_SEND_COMPLETE = "msg_sendComplete_broadcast";
        public static final String MSG_READ_COMPLETE = "msg_readComplete_broadcast";
        public static final String MSG_USERINFO = "msg_userinfo";
        public static final String MSG_USER_POPUP = "msg_userpopup";
        public static final String MSG_SEND_NICK = "msg_send_nick";
        public static final String FMC_PROVISIONING = "we_work_svr_info";
        public static final String FMC_CALL_STATE = "voip_call_state";
        public static final String MSG_MYFOLDER_USER_ADD = "msg_myfolder_user_add";
        public static final String MSG_MYFOLDER_USER_DEL = "msg_myfolder_user_del";
        public static final String MSG_MYFOLDER_GROUP_ADD = "msg_myfolder_group_add";
        public static final String MSG_MYFOLDER_GROUP_MOD = "msg_myfolder_group_mod";
        public static final String MSG_MYFOLDER_GROUP_DEL = "msg_myfolder_group_del";
        public static final String MSG_MYFOLDER_SUB_GROUP_ADD = "msg_myfolder_sub_group_add";
        public static final String MSG_RESTART_SERVICE = "msg_restart_service";
        public static final String MSG_NETWORK_CHANGE = "msg_change_network";
        public static final String MSG_PASSWORD_CHANGE = "msg_password_change";

        public static String getPhoneNumberToRawContactId( String number )
        {
                if ( rawIdMap == null )
                {
                        rawIdMap = new ConcurrentHashMap<String, String>();
                }
                String rawId = rawIdMap.get( number );
                if ( rawId != null )
                {
                        Log.d( "ContactId", number + " Exist1 : " + rawId );
                        return rawId;
                }
                else
                {
                        String _id = "";
                        String raw_id = "";
                        final Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                                        ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID };
                        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + "=?";
                        String[] select_args = { number };
                        Cursor is_c = mContext.getContentResolver().query( uri, projection, selection, select_args, null );
                        Log.d( "ContactCount", "" + is_c.getCount() );
                        try
                        {
                                if ( is_c.getCount() > 0 )
                                {
                                        is_c.moveToFirst();
                                        _id = is_c.getString( 0 );
                                        raw_id = is_c.getString( 1 );
                                        Log.d( "ContactId", _id + ":" + raw_id );
                                        if ( raw_id != null )
                                        {
                                                Log.d( "ContactId", number + " Not Exist2 : " + _id );
                                                return _id;
                                        }
                                }
                        }
                        catch ( Exception e )
                        {
                                Log.e( "getPhoneNumberToRawContactId", number, e );
                        }
                        finally
                        {
                                is_c.close();
                                if ( raw_id == null ) raw_id = "0";
                                rawIdMap.put( number, raw_id );
                        }
                }
                return "0";
        }

        public static String getContactIdFromName( String DisplayName )
        {
                String orgWhere = ContactsContract.Contacts.DISPLAY_NAME + " = ?";
                String[] orgWhereParams = new String[] { DisplayName };
                String contactId = null;
                Cursor orgCur = mContext.getContentResolver().query( ContactsContract.Contacts.CONTENT_URI, null, orgWhere, orgWhereParams, null );
                if ( orgCur.moveToFirst() )
                {
                        contactId = orgCur.getString( orgCur.getColumnIndex( ContactsContract.Contacts._ID ) );
                }
                orgCur.close();
                return contactId;
        }

        public static String getRawIdWithContactId( String contactId )
        {
                String raw_id = "";
                final Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID };
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?";
                String[] select_args = { contactId };
                Cursor is_c = mContext.getContentResolver().query( uri, projection, selection, select_args, null );
                try
                {
                        if ( is_c.getCount() > 0 )
                        {
                                is_c.moveToFirst();
                                raw_id = is_c.getString( 0 );
                                if ( raw_id != null )
                                {
                                        Log.d( "ContactId", raw_id + " Not Exist4 : " + contactId );
                                        return raw_id;
                                }
                        }
                }
                catch ( Exception e )
                {
                        Log.e( "getPhoneNumberToRawContactId", contactId, e );
                }
                finally
                {
                        is_c.close();
                }
                return "0";
        }

        public static String getPhoneNumberToRawContactRawId( String number )
        {
                String _id = "";
                String raw_id = "";
                final Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.CONTACT_ID, ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID };
                String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + "=?";
                String[] select_args = { number };
                Cursor is_c = mContext.getContentResolver().query( uri, projection, selection, select_args, null );
                try
                {
                        if ( is_c.getCount() > 0 )
                        {
                                is_c.moveToFirst();
                                _id = is_c.getString( 0 );
                                raw_id = is_c.getString( 1 );
                                if ( raw_id != null )
                                {
                                        Log.d( "ContactId", number + " Not Exist3 : " + _id );
                                        return raw_id;
                                }
                        }
                }
                catch ( Exception e )
                {
                        Log.e( "getPhoneNumberToRawContactId", number, e );
                }
                finally
                {
                        is_c.close();
                }
                return "0";
        }

        public static int getDpFromPx( Context context, int pixel )
        {
                int result = 0;
                // float scale = context.getResources().getDisplayMetrics().density;
                int px = ( int ) (pixel / 3.0f);
                result = ( int ) TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics() );
                return result;
        }

        public static String getBrandsApkName()
        {
                switch ( SET_COMPANY )
                {
                        case BASIC :
                                return "AtSmart_basic.apk";
                        case SAMSUNG :
                                return "AtSmart_sec.apk";
                        case CU :
                                return "AtSmart_cu.apk";
                        case ENERGY :
                                return "AtSmart_energy.apk";
                        case CNUH :
                                return "AtSmart_cnuh.apk";
                        case SAMYANG :
                                return "AtSmart_samyang.apk";
                        case KORAIL :
                                return "AtSmart_korail.apk";
                        case MOORIM :
                                return "AtSmart_moorim.apk";
                        case MODETOUR :
                                return "AtSmart_modetour.apk";
                        case POLICE :
                                return "AtSmart_police.apk";
                        case NEC :
                                return "AtSmart_nec.apk";
                        case UST :
                                return "AtSmart_ust.apk";
                        case NONGSHIM :
                                return "AtSmart_nongshim.apk";
                        case REDCROSS :
                                return "AtSmart_redcross.apk";
                        case KEAD :
                                return "AtSmart_kead.apk";
                        case SAEHA :
                                return "AtSmart_saeha.apk";
                        case GG :
                                return "AtSmart_gg.apk";
                        case MBC :
                                return "AtSmart_mbc.apk";
                        case NAMDONG : 
                                return "AtSmart_namdong.apk";
                        case EX :
                                return "AtSmart_ex.apk";
                        case SSL92:
                                return "WEVoIPTalk.apk";
                        case DEMO:
                                return "WEVoIPTalk.apk";
                        case AMOTECH:
                                return "AtSmart_amotech.apk";
                        case IPAGEON:
                        		return "AtSmart_IPageOn.apk";
                        default :
                                return "AtSmart_basic.apk";
                }
        }
        
        // IPageOn
        public static final String IPG_P701_USER_AGENT = "IPageOn_nTalkA";

    	public static final String IPG_P701_SAMPLE_ROOT_CERT = "rootcert.pem";
        public static final String IPG_P701_SAMPLE_USER_CERT = "endcert.pem";
        public static final String IPG_P701_SAMPLE_USER_KEY = "endkey.pem";
        public static final String IPG_P701_SAMPLE_RINGTONE = "oldphone_mono.wav"; 
        public static final String IPG_P701_SAMPLE_RINGBACK = "ringback.wav";
        public static final String IPG_P701_SAMPLE_HOLDTONE = "hold.mkv";
        
        public static final int IPG_P701_SAMPLE_KEEP_ALIVE_TIME = 20000;
        public static final int IPG_P701_SAMPLE_HEARTBEAT = 3600;
        
        public static final String IPG_CALL_STATE_CHANGED = "kr.co.ultari.atsmart.ipocall.callstatechanged";
        public static final String IPG_CALL_ACTION = "kr.co.ultari.atsmart.ipocall.callaction";
        public static final String IPG_CALL_NUMBER_INFO = "kr.co.ultari.atsmart.ipocall.callNumber";
        public static final String IPG_CALL_USER_INFO = "kr.co.ultari.atsmart.ipocall.callUserInfo";
        
        public static final int CALL_CONNECTED		= 0x01;
        public static final int CALL_DISCONNECTED	= 0x02;
        public static final int CALL_TIME_CHANGED	= 0x03;
        
        public static final short CALL_TYPE_ALL			= 0x00;
        public static final short CALL_TYPE_INCOMING	= 0x01;
        public static final short CALL_TYPE_OUTGOING	= 0x02;
        public static final short CALL_TYPE_ABSENT		= 0x03;
        
        public static boolean phoneCallState = false; 
        
    	public static String getUserPw(String deviceUUID)
    	{
    		if ( deviceUUID.length() >= 13 ) return deviceUUID.substring(0, 13);
    		else return deviceUUID;
    	}
    	
    	public static String getDomain()
    	{
    		return "fmc.com";
    	}
    	
    	public static String getProxy()
    	{
    		return "27.1.48.131";
    	}
    	
    	public static String getUserNumber(String deviceUUID)
    	{
    		if ( deviceUUID.indexOf("00000000") == 0 )
    			return "0702003001";
    		else
    			return "0702003000";
    	}
    	
    	public static String udpGetPort()
    	{
    		return "5060";
    	}
    	
    	public static String tlsGetPort()
    	{
    		return "5061";
    	}
}
