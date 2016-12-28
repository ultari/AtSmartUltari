package kr.co.ultari.atsmart.basic.control.tree;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.subdata.Contact;
import kr.co.ultari.atsmart.basic.subdata.FavoriteData;
import kr.co.ultari.atsmart.basic.subdata.SearchResultItemData;
import kr.co.ultari.atsmart.basic.subview.BuddyAddUser;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import kr.co.ultari.atsmart.basic.view.BuddyView;
import kr.co.ultari.atsmart.basic.view.ContactView;
import kr.co.ultari.atsmart.basic.view.FavoriteView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint( { "HandlerLeak", "Assert" } )
public class MessengerTree extends Fragment implements OnClickListener {
        private LinearLayout layout = null;
        public ArrayList<TreeItem> items;
        public TreeItem m_TopItem;
        public static Bitmap depthExpandBitmap;
        public static Bitmap depthCollapseBitmap;
        public static Bitmap expandBitmap;
        public static Bitmap collapseBitmap;
        public static Bitmap talkBitmap;
        public static Bitmap statusOnlineBitmap;
        public static Bitmap statusAwayBitmap;
        public static Bitmap statusBusyBitmap;
        public static Bitmap statusPhoneBitmap;
        public static Bitmap statusMeetingBitmap;
        public static Bitmap statusOfflineBitmap;
        public static Bitmap checkUserBitmap;
        public static Bitmap uncheckUserBitmap;
        public static Bitmap statusMoblieOnBitmap;
        public static Bitmap statusMoblieOffBitmap;
        public static Bitmap groupChatButtonBitmap;
        public static Bitmap statusUcOnBitmap;
        public static Bitmap statusUcRingBitmap;
        public static Bitmap statusUcOffBitmap;
        
        public static Bitmap defaultUserImage;
        
        public static Bitmap statusFmcOnBitmap;
        public static Bitmap statusFmcOffBitmap;
        public static Bitmap statusFmcRingBitmap;
        
        public static Bitmap arrow;
        
        public int phoneNumberIndex;
        public int layoutResource;
        public View view = null;
        public HashMap<String, String> userMap = null;
        protected Context context;
        public Handler customHandler = null;
        private static final int PART = 1000;
        private static final int USER = 1001;
        private static final int OTHER = 1002;
        private static final int ADDUSER = 1003;
        private static final int UCMENU = 1004; // 2015-05-18
        public String nowSelectedPartId = null;
        public String nowSelectedPartName = null;
        public String nowSelectedUserId = null;
        public String nowSelectedPartHigh = null;
        public String nowSelectedUserName = null;
        
        public ArrayList<String> selectedIdAr;
        public ArrayList<String> selectedNameAr;

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
                context = getActivity();
                
                selectedIdAr = new ArrayList<String>();
                selectedNameAr = new ArrayList<String>();
                
                this.view = inflater.inflate( layoutResource, null );
                layout = ( LinearLayout ) view.findViewById( R.id.TreeLayout );
				
                items = new ArrayList<TreeItem>();
                m_TopItem = new TreeItem( this, "0", 6, getString( R.string.top ), null, "IDTOP", -1, "0", false );
                items.add( m_TopItem );
                m_TopItem.setOnClickListener( this );
                talkBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.talk_img );
                if ( depthExpandBitmap == null ) depthExpandBitmap = BitmapFactory.decodeResource( getResources(), R.drawable._icon_tree_minus );
                if ( depthCollapseBitmap == null ) depthCollapseBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_tree_plus );
                if ( expandBitmap == null ) expandBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_tree_up );
                if ( collapseBitmap == null ) collapseBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_tree_down );
                if ( Define.useStatusPcIcon )
                {
                        if ( statusOnlineBitmap == null ) statusOnlineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_online );
                        if ( statusAwayBitmap == null ) statusAwayBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_away );
                        if ( statusBusyBitmap == null ) statusBusyBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_busy );
                        if ( statusPhoneBitmap == null ) statusPhoneBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_phone );
                        if ( statusMeetingBitmap == null ) statusMeetingBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_meeting );
                        if ( statusOfflineBitmap == null ) statusOfflineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_offline );
                }
                else
                {
                        if ( statusOnlineBitmap == null ) statusOnlineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        if ( statusAwayBitmap == null ) statusAwayBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        if ( statusBusyBitmap == null ) statusBusyBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        if ( statusPhoneBitmap == null ) statusPhoneBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        if ( statusMeetingBitmap == null ) statusMeetingBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        if ( statusOfflineBitmap == null ) statusOfflineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_off );
                }
                if ( statusUcOnBitmap == null ) statusUcOnBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_uc_on );
                if ( statusUcRingBitmap == null ) statusUcRingBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_uc_ring );
                if ( statusUcOffBitmap == null ) statusUcOffBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_uc_off );
                if ( checkUserBitmap == null ) checkUserBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.btn_blackbg_checked );
                if ( uncheckUserBitmap == null ) uncheckUserBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.btn_blackbg_uncheck );
                if ( statusMoblieOnBitmap == null ) statusMoblieOnBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_status_mobile_online );
                if ( statusMoblieOffBitmap == null ) statusMoblieOffBitmap = BitmapFactory.decodeResource( getResources(),
                                R.drawable.icon_status_mobile_offline );
                if ( groupChatButtonBitmap == null ) groupChatButtonBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.buddygroup );
                
                if( defaultUserImage == null ) defaultUserImage = BitmapFactory.decodeResource( getResources(), R.drawable.img_profile_100x100 );
                
                if ( statusFmcOnBitmap == null )   statusFmcOnBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_voip_on );
                if ( statusFmcOffBitmap == null )  statusFmcOffBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_voip_off );
                if ( statusFmcRingBitmap == null ) statusFmcRingBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_voip_ring );
                
                if ( arrow == null ) arrow = BitmapFactory.decodeResource(getResources(), R.drawable.multi_select_arrow_normal);
                
                userMap = new HashMap<String, String>();
                return view;
        }

        public Bitmap resizeBitmapImage( Bitmap source )
        {
                int width = source.getWidth();
                int height = source.getHeight();
                int newWidth = ( int ) (width * 0.9);
                int newHeight = ( int ) (height * 0.7);
                return Bitmap.createScaledBitmap( source, newWidth, newHeight, true );
        }

        @Override
        public void onDestroy()
        {
                super.onDestroy();
                TRACE( "onDestory" );
                clear();
                /*
                 * if ( expandBitmap != null ) { expandBitmap.recycle(); expandBitmap =
                 * null; }
                 * if ( collapseBitmap != null ) { collapseBitmap.recycle();
                 * collapseBitmap = null; }
                 * if ( statusOnlineBitmap != null ) { statusOnlineBitmap.recycle();
                 * statusOnlineBitmap = null; }
                 * if ( statusAwayBitmap != null ) { statusAwayBitmap.recycle();
                 * statusAwayBitmap = null; }
                 * if ( statusBusyBitmap != null ) { statusBusyBitmap.recycle();
                 * statusBusyBitmap = null; }
                 * if ( statusPhoneBitmap != null ) { statusPhoneBitmap.recycle();
                 * statusPhoneBitmap = null; }
                 * if ( statusMeetingBitmap != null ) { statusMeetingBitmap.recycle();
                 * statusMeetingBitmap = null; }
                 * if ( statusOfflineBitmap != null ) { statusOfflineBitmap.recycle();
                 * statusOfflineBitmap = null; }
                 * if ( checkUserBitmap != null ) { checkUserBitmap.recycle();
                 * checkUserBitmap = null; }
                 * if ( uncheckUserBitmap != null ) { uncheckUserBitmap.recycle();
                 * uncheckUserBitmap = null; }
                 * if ( statusMoblieOnBitmap != null ) { statusMoblieOnBitmap.recycle();
                 * statusMoblieOnBitmap = null; }
                 * if ( talkBitmap != null ) { talkBitmap.recycle(); talkBitmap = null;
                 * }
                 * if ( groupChatButtonBitmap != null ) {
                 * groupChatButtonBitmap.recycle(); groupChatButtonBitmap = null; }
                 */
        }

        public void init( View view )
        {
                this.view = view;
                
                selectedIdAr = new ArrayList<String>();
                selectedNameAr = new ArrayList<String>();
                
                context = getActivity();
                layout = ( LinearLayout ) view.findViewById( R.id.TreeLayout );
                items = new ArrayList<TreeItem>();
                m_TopItem = new TreeItem( this, "0", 6, getString( R.string.top ), null, "IDTOP", -1, "0", false );
                items.add( m_TopItem );
                m_TopItem.setOnClickListener( this );
                talkBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.talk_img );
                depthExpandBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_tree_minus );
                depthCollapseBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_tree_plus );
                expandBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_tree_up );
                collapseBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_tree_down );
                if ( Define.useStatusPcIcon )
                {
                        statusOnlineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_online );
                        statusAwayBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_away );
                        statusBusyBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_busy );
                        statusPhoneBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_phone );
                        statusMeetingBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_meeting );
                        statusOfflineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.status_offline );
                }
                else
                {
                        statusOnlineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        statusAwayBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        statusBusyBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        statusPhoneBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        statusMeetingBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_on );
                        statusOfflineBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pc_off );
                }
                statusUcOnBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_uc_on );
                statusUcRingBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_uc_ring );
                statusUcOffBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_uc_off );
                checkUserBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.btn_blackbg_checked );
                uncheckUserBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.btn_blackbg_uncheck );
                statusMoblieOnBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_status_mobile_online );
                statusMoblieOffBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_status_mobile_offline );
                groupChatButtonBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.buddygroup );
                
                defaultUserImage = BitmapFactory.decodeResource( getResources(), R.drawable.img_profile_100x100 );
                
                statusFmcOnBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_voip_on );
                statusFmcOffBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_voip_off );
                statusFmcRingBitmap = BitmapFactory.decodeResource( getResources(), R.drawable.icon_voip_ring );
                
                if ( arrow == null ) arrow = BitmapFactory.decodeResource(getResources(), R.drawable.multi_select_arrow_normal);
                
                userMap = new HashMap<String, String>();
        }

        @Override
        public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo menuInfo )
        {
                super.onCreateContextMenu( menu, v, menuInfo );
                if ( v.getId() == USER )
                {
                        menu.setHeaderTitle( nowSelectedUserName );
                        if ( Define.useMyFolderEdit )
                        {
                                menu.add( 0, Define.MENU_ID_GROUP_ADD, Menu.NONE, getString( R.string.menu_group_add ) );
                                menu.add( 0, Define.MENU_ID_USER_DEL, Menu.NONE, getString( R.string.menu_user_del ) );
                        }
                        // menu.add(0, Define.MENU_ID_MOVE, Menu.NONE, getString(R.string.menu_move ));
                        menu.add( 0, Define.MENU_ID_CHAT, Menu.NONE, getString( R.string.chat ) );
                        menu.add( 0, Define.MENU_ID_INFO, Menu.NONE, getString( R.string.info ) );
                        menu.add( 0, Define.MENU_ID_CALL, Menu.NONE, getString( R.string.calling ) );
                        if ( Define.useFavoriteAdd ) menu.add( 0, Define.MENU_ID_FAVORITE, Menu.NONE, getString( R.string.favorite_add ) );
                        if ( Define.useSendNoteMsg ) menu.add( 0, Define.MENU_ID_NOTE, Menu.NONE, getString( R.string.send ) );
                        if ( Define.support_file_send ) menu.add( 0, Define.MENU_ID_FILE, Menu.NONE, getString( R.string.sendFile ) );
                        if ( Define.useOrgUserToContactSave ) menu.add( 0, Define.MENU_ID_SAVE, Menu.NONE, getString( R.string.contact_save ) );
                }
                else if ( v.getId() == PART )
                {
                        menu.setHeaderTitle( nowSelectedPartName );
                        menu.add( 0, Define.MENU_ID_GROUP_ADD, Menu.NONE, getString( R.string.menu_group_add ) );
                        menu.add( 0, Define.MENU_ID_SUB_GROUP_ADD, Menu.NONE, getString( R.string.menu_sub_group_add ) );
                        menu.add( 0, Define.MENU_ID_GROUP_DEL, Menu.NONE, getString( R.string.menu_group_del ) );
                        menu.add( 0, Define.MENU_ID_GROUP_MOD, Menu.NONE, getString( R.string.menu_group_mod ) );
                        menu.add( 0, Define.MENU_ID_USER_ADD, Menu.NONE, getString( R.string.menu_user_add ) );
                        // menu.add(0, Define.MENU_ID_MOVE, Menu.NONE, getString(
                        // R.string.menu_move ));
                }
                else if ( v.getId() == OTHER )
                {
                        menu.setHeaderTitle( nowSelectedUserName );
                        menu.add( 0, Define.MENU_ID_CHAT, Menu.NONE, getString( R.string.chat ) );
                        menu.add( 0, Define.MENU_ID_INFO, Menu.NONE, getString( R.string.info ) );
                        menu.add( 0, Define.MENU_ID_CALL, Menu.NONE, getString( R.string.calling ) );
                        if ( Define.useFavoriteAdd ) menu.add( 0, Define.MENU_ID_FAVORITE, Menu.NONE, getString( R.string.favorite_add ) );
                        if ( Define.useSendNoteMsg ) menu.add( 0, Define.MENU_ID_NOTE, Menu.NONE, getString( R.string.send ) );
                        if ( Define.support_file_send ) menu.add( 0, Define.MENU_ID_FILE, Menu.NONE, getString( R.string.sendFile ) );
                        if ( Define.useOrgUserToContactSave ) menu.add( 0, Define.MENU_ID_SAVE, Menu.NONE, getString( R.string.contact_save ) );
                }
                // 2015-05-18
                else if ( v.getId() == UCMENU )
                {
                        menu.setHeaderTitle( nowSelectedUserName );
                        menu.add( 0, Define.MENU_ID_INCOMING_CALL, Menu.NONE, getString( R.string.incoming_call ) );
                        menu.add( 0, Define.MENU_ID_INFO, Menu.NONE, getString( R.string.info ) );
                }
                else
                {}
        }

        public void addFolder( String id, String parentId, String text, String order, boolean m_bOpened )
        {
                TreeItem m_ParentItem = getItem( parentId );
                if ( m_ParentItem == null )
                {
                        /*
                         * switch ( Define.SET_COMPANY )
                         * {
                         * case Define.BASIC :
                         * if ( !text.equals( getString( R.string.top ) ) )
                         * {
                         * String tmpPartId = "";
                         * if ( id.indexOf( "/" ) >= 0 ) tmpPartId = id.substring( id.indexOf( "/" ) + 1 );
                         * else tmpPartId = id;
                         * Log.d(TAG, "addFolder Group_DEL id:" + tmpPartId + ", high:" + parentId );
                         * sendMessage( Define.MENU_ID_GROUP_DEL, Define.getMyId( context ), tmpPartId, parentId, "", "6" );
                         * }
                         * break;
                         * default :
                         * break;
                         * }
                         */
                        return;
                }
                boolean isEmpty = true;
                int size = m_ParentItem.m_child.size();
                for ( int i = 0; i < size; i++ )
                {
                        if ( id.equals( m_ParentItem.m_child.get( i ).id ) ) isEmpty = false;
                }
                if ( isEmpty )
                {
                        int depth = 0;
                        if ( m_ParentItem != null ) depth = m_ParentItem.depth + 1;
                        int position = 0;
                        if ( m_ParentItem != null ) position = getItemIndex( m_ParentItem ) + 1;
                        TreeItem item = new TreeItem( this, id, 6, text, null, parentId, depth, order, m_bOpened );
                        if ( Define.selectedTreeNumber == 0 )
                        {
                                if ( depth == 0 ) position += m_ParentItem.addChild( item );
                                else m_ParentItem.addChild( item );
                        }
                        else position += m_ParentItem.addChild( item );
                        LinearLayout.LayoutParams viewParam = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, Define.getDpFromPx(
                                        context, Define.TREE_PART_ITEM_HEIGHT ) );
                        layout.addView( item, position, viewParam );
                        items.add( item );
                        item.setOnClickListener( this );
                        if ( Define.useMyFolderEdit ) item.setOnLongClickListener( mOnLongClickListener );
                }
        }
        private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
                @Override
                public boolean onLongClick( View v )
                {
                        v.setId( OTHER );
                        return true;
                }
        };

        public void addFile( String id, String parentId, String text, String info, int icon, String order )
        {
                TreeItem m_ParentItem = getItem( parentId );
                if ( m_ParentItem == null )
                {
                        /*
                         * switch ( Define.SET_COMPANY )
                         * {
                         * case Define.BASIC :
                         * String tmpUserId = "";
                         * if ( id.indexOf( "/" ) >= 0 ) tmpUserId = id.substring( id.indexOf( "/" ) + 1 );
                         * else tmpUserId = id;
                         * Log.d(TAG, "addFile User_Del id:" + tmpUserId + ", high:" + parentId );
                         * sendMessage( Define.MENU_ID_USER_DEL, Define.getMyId( context ), tmpUserId, parentId, "", "0" );
                         * break;
                         * default :
                         * break;
                         * }
                         */
                        return;
                }
                boolean isEmpty = true;
                int size = m_ParentItem.m_child.size();
                for ( int i = 0; i < size; i++ )
                {
                        if ( id.equals( m_ParentItem.m_child.get( i ).id ) ) isEmpty = false;
                }
                if ( isEmpty )
                {
                        int depth = 0;
                        if ( m_ParentItem != null ) depth = m_ParentItem.depth + 1;
                        int position = 0;
                        if ( m_ParentItem != null )
                        {
                                position = getItemIndex( m_ParentItem ) + 1;
                        }
                        if ( id.indexOf( '/' ) >= 0 ) userMap.put( id.substring( id.lastIndexOf( '/' ) + 1 ), text );
                        else userMap.put( id, text );
                        TreeItem item = new TreeItem( this, id, icon, StringUtil.getNamePosition( text ), info, parentId, depth, order, false );
                        position += m_ParentItem.addChild( item );
                        LinearLayout.LayoutParams viewParam = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, Define.getDpFromPx(
                                        context, Define.TREE_USER_ITEM_HEIGHT ) );
                        if ( m_ParentItem.isExpanded ) layout.addView( item, position, viewParam );
                        items.add( item );
                        item.setOnClickListener( this );
                        m_ParentItem.setUserCount();
                }
        }

        public void clear()
        {
                for ( int i = (items.size() - 1); i >= 1; i-- )
                {
                        items.remove( i );
                }
                m_TopItem.m_child.clear();
                layout.removeAllViewsInLayout();
        }

        public void setMobileOn( String id, int icon )
        {
                for ( int i = 0; i < items.size(); i++ )
                {
                        if ( items.get( i ).id.indexOf( "/" + id ) >= 0 || items.get( i ).id.equals( id ) ) items.get( i ).setMobileOn( icon );
                }
        }

        public void setUc( String id, int icon )
        {
                for ( int i = 0; i < items.size(); i++ )
                {
                        if ( items.get( i ).id.indexOf( "/" + id ) >= 0 || items.get( i ).id.equals( id ) ) items.get( i ).setUc( icon );
                }
        }

        public void setMobileIcon( String id )
        {
                for ( int i = 0; i < items.size(); i++ )
                {
                        if ( items.get( i ).id.indexOf( "/" + id ) >= 0 || items.get( i ).id.equals( id ) )
                        {
                                items.get( i ).setMobileIcon( 1 );
                        }
                        else
                        {
                                items.get( i ).setMobileIcon( 0 );
                        }
                }
        }

        public void setIcon( String id, int icon )
        {
                for ( int i = 0; i < items.size(); i++ )
                {
                        if ( items.get( i ).id.indexOf( "/" + id ) >= 0 || items.get( i ).id.equals( id ) )
                        {
                                items.get( i ).setIcon( icon );
                        }
                }
        }

        public void setUcIcon( String id, int icon )
        {
                //Log.d( "TREE", "setUcIcon id:" + id + ", icon:" + icon );
                for ( int i = 0; i < items.size(); i++ )
                {
                        if ( items.get( i ).id.indexOf( "/" + id ) >= 0 || items.get( i ).id.equals( id ) )
                        {
                                items.get( i ).setUcIcon( icon );
                        }
                }
        }

        public void updateCheckBtn()
        {
                for ( int i = 0; i < items.size(); i++ )
                {
                        items.get( i ).updateCheck();
                }
        }

        public void updateFontSize()
        {
        	try
        	{
        		if ( items == null ) return;
                for ( int i = 0; i < items.size(); i++ )
                        items.get( i ).changeFontSize();
        
        	}
        	catch ( Exception e )
            {
                    Define.EXCEPTION( e );
            }
        }

        public void clearCheckBtn()
        {
                try
                {
                        if ( items == null ) return;
                        for ( int i = 0; i < items.size(); i++ )
                        {
                                items.get( i ).isCheck = false;
                                items.get( i ).updateCheck();
                        }
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
        }

        public void setName( String id, String name )
        {
                for ( int i = 0; i < items.size(); i++ )
                {
                        if ( items.get( i ).id.indexOf( "/" + id ) >= 0 || items.get( i ).id.equals( id ) ) items.get( i ).setText( name );
                }
        }

        public void setInfo( String id, String info )
        {
                for ( int i = 0; i < items.size(); i++ )
                {
                        if ( items.get( i ).id.indexOf( "/" + id ) >= 0 || items.get( i ).id.equals( id ) ) items.get( i ).setInfo( info );
                }
        }

        public void hideChild( TreeItem item )
        {
                try
                {
                        layout.removeViewAt( getItemIndex( item ) );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public void showChild( TreeItem item, TreeItem m_ParentItem )
        {
                try
                {
                        int position = 0;
                        if ( m_ParentItem != null )
                        {
                                position = getItemIndex( m_ParentItem ) + 1;
                        }
                        position += m_ParentItem.getItemIndex( item );
                        LinearLayout.LayoutParams viewParam = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, Define.getDpFromPx(
                                        context, Define.TREE_USER_ITEM_HEIGHT ) );
                        layout.addView( item, position, viewParam );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        public void onClick( View view )
        {
                try
                {
                        if ( !items.contains( view ) )
                        {
                                OnCustomClick( view );
                                return;
                        }
                        TreeItem v = ( TreeItem ) view;
                        if ( v.isFolder == false )
                        {
                                onUserClicked( v );
                                nowSelectedUserId = v.id;
                                nowSelectedPartHigh = v.parentId;
                                while ( nowSelectedUserId.indexOf( "/" ) >= 0 )
                                {
                                        nowSelectedUserId = nowSelectedUserId.substring( nowSelectedUserId.indexOf( '/' ) + 1 );
                                }
                                nowSelectedUserName = v.text;
                                if ( !Define.isBuddyAddMode )
                                {
                                        // if ( Define.selectedTreeNumber == Define.TAB_USER_BUDDY ) v.setId( USER );
                                        // if ( Define.selectedTreeNumber == Define.TAB_OTHER ) v.setId( ADDUSER );
                                        // else v.setId( OTHER );
                                        /*
                                         * registerForContextMenu( v );
                                         * getActivity().openContextMenu( v );
                                         * unregisterForContextMenu( v );
                                         */
                                        // TRACE( "UserInfo : " + nowSelectedUserId + ":" + nowSelectedUserName );
                                        // ActionManager.popupUserInfo( context, nowSelectedUserId, userMap.get( nowSelectedUserId ),Define.getPartNameByUserId(
                                        // nowSelectedUserName ) , v.info);
                                        // 2015-05-18
                                        if ( Define.useIncomingCall )
                                        {
                                                int state = v.ucIcon;
                                                v.setId( UCMENU );
                                                if ( state == 2 || state == 3 )
                                                {
                                                        // MSG, USERINFO
                                                        registerForContextMenu( v );
                                                        getActivity().openContextMenu( v );
                                                        unregisterForContextMenu( v );
                                                }
                                                else
                                                {
                                                	Log.d("TREEITEM", "PopupUserInfo1");
                                                        // UserInfo
                                                        ActionManager.popupUserInfo( context, nowSelectedUserId, userMap.get( nowSelectedUserId ),
                                                                        Define.getPartNameByUserId( nowSelectedUserName ), v.info );
                                                }
                                        }
                                        else
                                        {
                                                // UserInfo
                                                /*ActionManager.popupUserInfo( context, nowSelectedUserId, userMap.get( nowSelectedUserId ),
                                                                Define.getPartNameByUserId( nowSelectedUserName ), v.info );*/
                                        	if ( v.isSelected )
                                        		v.onNormal();
                                        	else
                                        		v.onClick();
                                        	
                                        	onChangeSelected();
                                        }
                                }
                                else
                                {
                                        for ( int i = 0; i < items.size(); i++ )
                                        {
                                                if ( items.get( i ) == v )
                                                {
                                                        if ( Define.isBuddyAddMode )
                                                        {
                                                                items.get( i ).setCheck();
                                                                if ( items.get( i ).isCheck )
                                                                {
                                                                        boolean exist = false;
                                                                        for ( int j = 0; j < BuddyView.selected.getCount(); j++ )
                                                                        {
                                                                                if ( BuddyView.selected.getItem( j ).id.equals( v.id ) ) exist = true;
                                                                        }
                                                                        if ( !exist )
                                                                        {
                                                                                TreeItem tr = getItem( v.id );
                                                                                BuddyView.selected.addItem( new SearchResultItemData( tr.id.substring( v.id
                                                                                                .indexOf( "/" ) + 1 ), tr.parentId, tr.text, tr.icon, tr.info,
                                                                                                true ) );
                                                                        }
                                                                }
                                                                else
                                                                {
                                                                        for ( int k = (BuddyView.selected.getCount() - 1); k >= 0; k-- )
                                                                        {
                                                                                if ( BuddyView.selected.getItem( k ).id.equals( v.id.substring( v.id
                                                                                                .indexOf( "/" ) + 1 ) ) )
                                                                                {
                                                                                        BuddyView.selected.remove( BuddyView.selected.getItem( k ) );
                                                                                }
                                                                        }
                                                                }
                                                                BuddyView.selected.notifyDataSetChanged();
                                                                Message m = customHandler.obtainMessage( Define.AM_SELECT_CHANGED, null );
                                                                customHandler.sendMessage( m );
                                                        }
                                                }
                                        }
                                }
                        }
                        else
                        {
                                nowSelectedPartId = v.id;
                                nowSelectedPartName = v.text;
                                nowSelectedPartHigh = v.parentId;
                                if ( Define.isBuddyAddMode && v.isExpanded )
                                {
                                        v.setCheck();
                                        if ( v.isCheck )
                                        {
                                                for ( TreeItem tr : v.m_child )
                                                {
                                                        if ( !tr.isFolder )
                                                        {
                                                                for ( int k = (BuddyView.selected.getCount() - 1); k >= 0; k-- )
                                                                {
                                                                        if ( BuddyView.selected.getItem( k ).id
                                                                                        .equals( tr.id.substring( tr.id.indexOf( "/" ) + 1 ) ) )
                                                                        {
                                                                                tr.cancelCheck();
                                                                                BuddyView.selected.remove( BuddyView.selected.getItem( k ) );
                                                                        }
                                                                }
                                                                tr.allCheck();
                                                                BuddyView.selected.addItem( new SearchResultItemData(
                                                                                tr.id.substring( tr.id.indexOf( "/" ) + 1 ), tr.parentId, tr.text, tr.icon,
                                                                                tr.info, true ) );
                                                        }
                                                }
                                        }
                                        else
                                        {
                                                for ( TreeItem tr : v.m_child )
                                                {
                                                        for ( int k = (BuddyView.selected.getCount() - 1); k >= 0; k-- )
                                                        {
                                                                if ( BuddyView.selected.getItem( k ).id.equals( tr.id.substring( tr.id.indexOf( "/" ) + 1 ) ) )
                                                                {
                                                                        tr.cancelCheck();
                                                                        BuddyView.selected.remove( BuddyView.selected.getItem( k ) );
                                                                }
                                                        }
                                                        Define.isBuddyAddMode = false;
                                                }
                                        }
                                        BuddyView.selected.notifyDataSetChanged();
                                        Message m = customHandler.obtainMessage( Define.AM_SELECT_CHANGED, null );
                                        customHandler.sendMessage( m );
                                }
                        }
                        
                        if ( v.isFolder )
                        {
                        	v.onClick();
                        }
                        /*for ( int i = 0; i < items.size(); i++ )
                        {
                                if ( items.get( i ) == v ) items.get( i ).onClick();
                                else items.get( i ).onNormal();
                        }*/
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }
        
        public void popupUserInfo(TreeItem v)
        {
        	Log.d("TREEITEM", "PopupUserInfo2");
        	ActionManager.popupUserInfo( context, v.id, userMap.get( v.id ), Define.getPartNameByUserId( v.text ), v.info );
        }
        
        public void onChangeSelected() {}

        public String getPartName( String id, String high )
        {
                for ( int i = 0; i < items.size(); i++ )
                {
                        if ( items.get( i ).id.indexOf( id ) >= 0 ) return getHigh( items.get( i ).parentId );
                }
                return "";
        }

        public String getHigh( String id )
        {
                for ( int i = 0; i < items.size(); i++ )
                {
                        if ( items.get( i ).id.indexOf( id ) >= 0 ) return items.get( i ).text;
                }
                return "";
        }

        @Override
        public boolean onContextItemSelected( MenuItem item )
        {
                if ( item.getItemId() == Define.MENU_ID_CALL )
                {
                        if ( nowSelectedUserId != null )
                        {
                                switch ( Define.SET_COMPANY )
                                {
                                case Define.REDCROSS :
                                        String[] ar = StringUtil.parseName( userMap.get( nowSelectedUserId ) );
                                        if ( !ar[3].equals( "" ) || !ar[4].equals( "" ) )
                                        {
                                                final String tmp[] = { getString( R.string.info_phone ) + ": " + ar[3],
                                                                getString( R.string.info_mobile ) + ": " + ar[4] };
                                                final String items[] = { ar[3], ar[4] };
                                                phoneNumberIndex = 0;
                                                AlertDialog.Builder ab = new AlertDialog.Builder( getActivity() );
                                                ab.setTitle( getString( R.string.select ) );
                                                ab.setSingleChoiceItems( tmp, 0, new DialogInterface.OnClickListener() {
                                                        public void onClick( DialogInterface dialog, int whichButton )
                                                        {
                                                                phoneNumberIndex = whichButton;
                                                        }
                                                } ).setPositiveButton( getString( R.string.call ), new DialogInterface.OnClickListener() {
                                                        public void onClick( DialogInterface dialog, int whichButton )
                                                        {
                                                                if ( !items[phoneNumberIndex].equals( "" ) ) ActionManager.callPhone( context,
                                                                                items[phoneNumberIndex] );
                                                        }
                                                } ).setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                                                        public void onClick( DialogInterface dialog, int whichButton )
                                                        {
                                                        }
                                                } );
                                                ab.show();
                                        }
                                        break;
                                default :
                                        String[] ar2 = StringUtil.parseName( userMap.get( nowSelectedUserId ) );
                                        if ( !ar2[3].equals( "" ) || !ar2[5].equals( "" ) )
                                        {
                                                final String tmp[] = { getString( R.string.info_phone ) + ": " + ar2[3],
                                                                getString( R.string.info_mobile ) + ": " + ar2[5] };
                                                final String items[] = { ar2[3], ar2[5] };
                                                phoneNumberIndex = 0;
                                                AlertDialog.Builder ab = new AlertDialog.Builder( getActivity() );
                                                ab.setTitle( getString( R.string.select ) );
                                                ab.setSingleChoiceItems( tmp, 0, new DialogInterface.OnClickListener() {
                                                        public void onClick( DialogInterface dialog, int whichButton )
                                                        {
                                                                phoneNumberIndex = whichButton;
                                                        }
                                                } ).setPositiveButton( getString( R.string.call ), new DialogInterface.OnClickListener() {
                                                        public void onClick( DialogInterface dialog, int whichButton )
                                                        {
                                                                if ( !items[phoneNumberIndex].equals( "" ) ) ActionManager.callPhone( context,
                                                                                items[phoneNumberIndex] );
                                                        }
                                                } ).setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                                                        public void onClick( DialogInterface dialog, int whichButton )
                                                        {
                                                        }
                                                } );
                                                ab.show();
                                        }
                                        break;
                                }
                        }
                        nowSelectedUserId = null;
                        nowSelectedUserName = null;
                }
                // 2015-05-18
                else if ( item.getItemId() == Define.MENU_ID_INCOMING_CALL )
                {
                        if ( !nowSelectedUserId.equals( Define.getMyId( context ) ) )
                        {
                                // 상대방에게 통화 신청을 알리기 위하여 모바일 대화를 보내준다.
                                String dateTime = StringUtil.getNowDateTime() + "";
                                String msgId = Define.getMyId( context ) + "_" + dateTime;
                                String oUserIds = nowSelectedUserId + "," + Define.getMyId( context );
                                String userIds = StringUtil.arrange( oUserIds );
                                String roomId = userIds.replace( ",", "_" );
                                sendBroadcastChat( msgId, roomId, nowSelectedUserId, nowSelectedUserName,
                                                Define.getMyName() + context.getString( R.string.incoming_call_msg ) );
                        }
                }
                else if ( item.getItemId() == Define.MENU_ID_INFO )
                {
                        if ( nowSelectedUserId != null && nowSelectedUserName != null )
                        {
                                TRACE( "UserInfo : " + nowSelectedUserId + ":" + nowSelectedUserName );
                                ActionManager.popupUserInfo( context, nowSelectedUserId, userMap.get( nowSelectedUserId ),
                                                Define.getPartNameByUserId( nowSelectedUserName ), "" );
                        }
                        nowSelectedUserId = null;
                        nowSelectedUserName = null;
                }
                else if ( item.getItemId() == Define.MENU_ID_CHAT )
                {
                        if ( nowSelectedUserId != null && nowSelectedUserName != null )
                        {
                                if ( !nowSelectedUserId.equalsIgnoreCase( Define.getMyId( context ) ) )
                                {
                                        String oUserIds = nowSelectedUserId + "," + Define.getMyId( context );
                                        String userIds = StringUtil.arrange( oUserIds );
                                        String userNames = nowSelectedUserName + "," + StringUtil.getNamePosition( Define.getMyName() );
                                        userNames = StringUtil.arrangeNamesByIds( userNames, oUserIds );
                                        String roomId = userIds.replace( ",", "_" );
                                        ArrayList<ArrayList<String>> array = Database.instance( context ).selectChatRoomInfo( roomId );
                                        if ( array.size() == 0 ) Database.instance( context ).insertChatRoomInfo( roomId, userIds, userNames,
                                                        StringUtil.getNowDateTime(), getString( R.string.newRoom ) );
                                        ActionManager.openChat( context, roomId, userIds, userNames );
                                }
                        }
                        nowSelectedUserId = null;
                        nowSelectedUserName = null;
                }
                else if ( item.getItemId() == Define.MENU_ID_NOTE )
                {
                        if ( nowSelectedUserId != null && nowSelectedUserName != null )
                        {
                                Intent i = new Intent( getActivity(), kr.co.ultari.atsmart.basic.subview.SendNote.class );
                                i.putExtra( "USERID", nowSelectedUserId );
                                i.putExtra( "USERNAME", nowSelectedUserName );
                                startActivity( i );
                        }
                }
                // 2015-03-01 myFolder edit
                else if ( item.getItemId() == Define.MENU_ID_GROUP_ADD )
                {
                        if ( nowSelectedPartId != null || nowSelectedUserId != null ) addGroupFolder();
                }
                else if ( item.getItemId() == Define.MENU_ID_GROUP_MOD )
                {
                        if ( nowSelectedPartId != null && nowSelectedPartName != null ) modGroupFolder();
                }
                else if ( item.getItemId() == Define.MENU_ID_GROUP_DEL )
                {
                        if ( nowSelectedPartId != null && nowSelectedPartName != null ) removeFolder();
                }
                else if ( item.getItemId() == Define.MENU_ID_SUB_GROUP_ADD )
                {
                        if ( nowSelectedPartId != null && nowSelectedPartName != null ) addSubGroupFolder();
                }
                else if ( item.getItemId() == Define.MENU_ID_USER_ADD )
                {
                        try
                        {
                                if ( !isNumber( nowSelectedPartId ) ) nowSelectedPartId = null;
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
                        }
                }
                else if ( item.getItemId() == Define.MENU_ID_USER_DEL )
                {
                        try
                        {
                                if ( nowSelectedUserId != null && nowSelectedUserName != null )
                                {
                                        // String partName = getPartName( nowSelectedUserId , nowSelectedPartHigh);
                                        if ( nowSelectedPartHigh.equals( getString( R.string.myPart ) ) )
                                        {
                                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                                View layout = inflater.inflate( R.layout.custom_toast,
                                                                ( ViewGroup ) getActivity().findViewById( R.id.custom_toast_layout ) );
                                                TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                                text.setText( getString( R.string.mypart_delete ) );
                                                text.setTypeface( Define.tfRegular );
                                                Toast toast = new Toast( getActivity() );
                                                toast.setGravity( Gravity.CENTER, 0, 0 );
                                                toast.setDuration( Toast.LENGTH_SHORT );
                                                toast.setView( layout );
                                                toast.show();
                                        }
                                        else removeFile();
                                }
                        }
                        catch ( Exception e )
                        {
                                Define.EXCEPTION( e );
                        }
                }
                else if ( item.getItemId() == Define.MENU_ID_FAVORITE )
                {
                        try
                        {
                                if ( nowSelectedUserId != null )
                                {
                                        boolean isExits = false;
                                        TreeItem userItem = getItemFavoriteId( nowSelectedUserId );
                                        /*
                                         * for ( int i = 0; i < FavoriteView.instance().adapter.getCount(); i++ )
                                         * {
                                         * FavoriteData data = FavoriteView.instance().adapter.getItem( i );
                                         * if ( data.getType().equals( "Organization" ) )
                                         * {
                                         * if( data.getUser() == null) continue;
                                         * if ( data.getUser().getUserId().equals( userItem.id ) )
                                         * {
                                         * isExits = true;
                                         * break;
                                         * }
                                         * }
                                         * }
                                         */
                                        ArrayList<ArrayList<String>> arr = Database.instance( getActivity() ).selectFavorite();
                                        if ( arr != null )
                                        {
                                                for ( ArrayList<String> tmp : arr )
                                                {
                                                        if ( tmp.get( 0 ).equals( userItem.id ) )
                                                        {
                                                                isExits = true;
                                                                break;
                                                        }
                                                }
                                        }
                                        //
                                        if ( !isExits )
                                        {
                                                Database.instance( context ).insertFavorite( userItem.id, userItem.parentId, userMap.get( nowSelectedUserId ),
                                                                userItem.info, Integer.toString( userItem.icon ), userItem.sortOrder );
                                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                                View layout = inflater.inflate( R.layout.custom_toast,
                                                                ( ViewGroup ) getActivity().findViewById( R.id.custom_toast_layout ) );
                                                TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                                text.setText( getString( R.string.favorite_add_msg ) );
                                                text.setTypeface( Define.tfRegular );
                                                Toast toast = new Toast( getActivity() );
                                                toast.setGravity( Gravity.CENTER, 0, 0 );
                                                toast.setDuration( Toast.LENGTH_SHORT );
                                                toast.setView( layout );
                                                toast.show();
                                        }
                                        else
                                        {
                                                LayoutInflater inflater = getActivity().getLayoutInflater();
                                                View layout = inflater.inflate( R.layout.custom_toast,
                                                                ( ViewGroup ) getActivity().findViewById( R.id.custom_toast_layout ) );
                                                TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                                text.setText( getString( R.string.favorite_exist ) );
                                                text.setTypeface( Define.tfRegular );
                                                Toast toast = new Toast( getActivity() );
                                                toast.setGravity( Gravity.CENTER, 0, 0 );
                                                toast.setDuration( Toast.LENGTH_SHORT );
                                                toast.setView( layout );
                                                toast.show();
                                        }
                                }
                                nowSelectedUserId = null;
                                nowSelectedUserName = null;
                        }
                        catch ( Exception e )
                        {
                                e.printStackTrace();
                        }
                }
                else if ( item.getItemId() == Define.MENU_ID_SAVE )
                {
                        if ( nowSelectedUserId != null )
                        {
                                String name = userMap.get( nowSelectedUserId );
                                String[] ar = StringUtil.parseName( name );
                                saveUserInfo( ar[0], ar[5], ar[3], ar[6], ar[2], ar[1] );
                        }
                }
                /*
                 * else if ( item.getItemId() == Define.MENU_ID_MOVE) {
                 * moveFolderAndFile(); }
                 */
                return super.onContextItemSelected( item );
        }

        private void saveUserInfo( String name, String mobile, String telephone, String email, String company, String position )
        {
                String DisplayName = name;
                String MobileNumber = mobile;
                String HomeNumber = telephone;
                String emailID = email;
                String companyName = company;
                String userPosition = position;
                ContentResolver cr = getActivity().getContentResolver();
                Cursor cur = cr.query( ContactsContract.Contacts.CONTENT_URI, null, null, null, null );
                if ( cur.getCount() > 0 )
                {
                        while ( cur.moveToNext() )
                        {
                                String existName = cur.getString( cur.getColumnIndex( ContactsContract.Contacts.DISPLAY_NAME ) );
                                if ( existName.contains( name ) )
                                {
                                        LayoutInflater inflater = getActivity().getLayoutInflater();
                                        View layout = inflater.inflate( R.layout.custom_toast,
                                                        ( ViewGroup ) getActivity().findViewById( R.id.custom_toast_layout ) );
                                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                        text.setText( getString( R.string.favorite_exist ) );
                                        text.setTypeface( Define.tfRegular );
                                        Toast toast = new Toast( getActivity() );
                                        toast.setGravity( Gravity.CENTER, 0, 0 );
                                        toast.setDuration( Toast.LENGTH_SHORT );
                                        toast.setView( layout );
                                        toast.show();
                                        return;
                                }
                        }
                }
                ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                ops.add( ContentProviderOperation.newInsert( ContactsContract.RawContacts.CONTENT_URI )
                                .withValue( ContactsContract.RawContacts.ACCOUNT_TYPE, null ).withValue( ContactsContract.RawContacts.ACCOUNT_NAME, null )
                                .build() );
                // ------------------------------------------------------ Names
                if ( DisplayName != null )
                {
                        ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE )
                                        .withValue( ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, DisplayName ).build() );
                }
                // ------------------------------------------------------ Mobile Number
                if ( MobileNumber != null )
                {
                        ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ).build() );
                }
                // ------------------------------------------------------ Home Numbers
                if ( HomeNumber != null )
                {
                        ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.NUMBER, HomeNumber )
                                        .withValue( ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME ).build() );
                }
                // ------------------------------------------------------ Email
                if ( emailID != null )
                {
                        ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                        .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                        .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE )
                                        .withValue( ContactsContract.CommonDataKinds.Email.DATA, emailID )
                                        .withValue( ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK ).build() );
                }
                // ---------------------------------------------------- Organization
                ops.add( ContentProviderOperation.newInsert( ContactsContract.Data.CONTENT_URI )
                                .withValueBackReference( ContactsContract.Data.RAW_CONTACT_ID, 0 )
                                .withValue( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE )
                                .withValue( ContactsContract.CommonDataKinds.Organization.COMPANY, companyName )
                                .withValue( ContactsContract.CommonDataKinds.Organization.TITLE, userPosition )
                                .withValue( ContactsContract.CommonDataKinds.Organization.TYPE, ContactsContract.CommonDataKinds.Organization.TYPE_WORK )
                                .build() );
                try
                {
                        context.getContentResolver().applyBatch( ContactsContract.AUTHORITY, ops );
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View layout = inflater.inflate( R.layout.custom_toast, ( ViewGroup ) getActivity().findViewById( R.id.custom_toast_layout ) );
                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                        text.setText( getString( R.string.input_success ) );
                        text.setTypeface( Define.tfRegular );
                        Toast toast = new Toast( getActivity() );
                        toast.setGravity( Gravity.CENTER, 0, 0 );
                        toast.setDuration( Toast.LENGTH_SHORT );
                        toast.setView( layout );
                        toast.show();
                        String raw_contact_id = "";
                        Bitmap tempBitMap = Define.getBitmap( nowSelectedUserId );
                        if ( tempBitMap != null )
                        {
                                ByteArrayOutputStream image = new ByteArrayOutputStream();
                                tempBitMap.compress( Bitmap.CompressFormat.JPEG, 100, image );
                                if ( MobileNumber != null )
                                {
                                        raw_contact_id = Define.getPhoneNumberToRawContactId( MobileNumber );
                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getActivity().getContentResolver(),
                                                        image.toByteArray(), Long.parseLong( raw_contact_id ) );
                                }
                                else if ( HomeNumber != null )
                                {
                                        raw_contact_id = Define.getPhoneNumberToRawContactId( HomeNumber );
                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getActivity().getContentResolver(),
                                                        image.toByteArray(), Long.parseLong( raw_contact_id ) );
                                }
                                nowSelectedUserId = null;
                                nowSelectedUserName = null;
                        }
                        else new getUserPhotoData( MobileNumber, HomeNumber );
                        // 2015-05-07
                        /*
                         * Contact acontact = new Contact();
                         * acontact.userId = null;
                         * acontact.userName = null;
                         * if(raw_contact_id != null && !raw_contact_id.equals(""))
                         * acontact.setPhotoid(Long.parseLong(raw_contact_id) );
                         * acontact.setUserid( "0" );
                         * acontact.setType( "Device" );
                         * acontact.setTelnum( HomeNumber.replaceAll("-", "") );
                         * acontact.setPhonenum( MobileNumber.replaceAll("-", "") );
                         * acontact.setName( name );
                         * acontact.setPosition( userPosition );
                         * acontact.setCompany( companyName );
                         * acontact.setEmail( email );
                         * Define.contactArray.add( acontact );
                         */
                        ContactView.instance().isLoadComplete = true;
                        ContactView.instance().displayListBasic();
                }
                catch ( Exception e )
                {
                        Define.EXCEPTION( e );
                }
        }
        private class getUserPhotoData extends Thread {
                String mobileNumber = "";
                String officeNumber = "";

                private getUserPhotoData( String mobile, String home )
                {
                        this.mobileNumber = mobile;
                        this.officeNumber = home;
                        this.start();
                }

                public void run()
                {
                        Bitmap pic = UltariSocketUtil.getUserImage( nowSelectedUserId, 200, 200 );
                        if ( pic != null )
                        {
                                ByteArrayOutputStream image = new ByteArrayOutputStream();
                                pic.compress( Bitmap.CompressFormat.JPEG, 100, image );
                                if ( mobileNumber != null )
                                {
                                        String raw_contact_id = Define.getPhoneNumberToRawContactId( mobileNumber );
                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getActivity().getContentResolver(),
                                                        image.toByteArray(), Long.parseLong( raw_contact_id ) );
                                }
                                else if ( officeNumber != null )
                                {
                                        String raw_contact_id = Define.getPhoneNumberToRawContactId( officeNumber );
                                        if ( !raw_contact_id.equals( "" ) && raw_contact_id != null ) setContactPhoto( getActivity().getContentResolver(),
                                                        image.toByteArray(), Long.parseLong( raw_contact_id ) );
                                }
                                nowSelectedUserId = null;
                                nowSelectedUserName = null;
                        }
                }
        }

        public void setContactPhoto( ContentResolver c, byte[] bytes, long rawContactId )
        {
                try
                {
                        ContentValues values = new ContentValues();
                        int photoRow = -1;
                        String where = ContactsContract.Data.RAW_CONTACT_ID + " = " + rawContactId + "                     AND "
                                        + ContactsContract.Data.MIMETYPE + "=='" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";
                        Cursor cursor = c.query( ContactsContract.Data.CONTENT_URI, null, where, null, null );
                        int idIdx = cursor.getColumnIndexOrThrow( ContactsContract.Data._ID );
                        if ( cursor.moveToFirst() )
                        {
                                photoRow = cursor.getInt( idIdx );
                        }
                        cursor.close();
                        values.put( ContactsContract.Data.RAW_CONTACT_ID, rawContactId );
                        values.put( ContactsContract.Data.IS_SUPER_PRIMARY, 1 );
                        values.put( ContactsContract.CommonDataKinds.Photo.PHOTO, bytes );
                        values.put( ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE );
                        if ( photoRow >= 0 )
                        {
                                c.update( ContactsContract.Data.CONTENT_URI, values, ContactsContract.Data._ID + " = " + photoRow, null );
                        }
                        else
                        {
                                c.insert( ContactsContract.Data.CONTENT_URI, values );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public static boolean isNumber( String number )
        {
                boolean flag = true;
                if ( number == null || "".equals( number ) ) return false;
                int size = number.length();
                int st_no = 0;
                if ( number.charAt( 0 ) == 45 ) st_no = 1;
                for ( int i = st_no; i < size; ++i )
                {
                        if ( !(48 <= (( int ) number.charAt( i )) && 57 >= (( int ) number.charAt( i ))) )
                        {
                                flag = false;
                                break;
                        }
                }
                return flag;
        }

        // 2015-05-18
        public void sendBroadcastChat( String msgId, String roomId, String userIds, String userNames, String talk )
        {
                try
                {
                        StringBuffer message = new StringBuffer();
                        message.append( Define.getMyId( context ) );
                        message.append( "\t" );
                        message.append( Define.getMyName() );
                        message.append( "\t" );
                        message.append( Define.getMyNickName() );
                        message.append( "\t" );
                        message.append( userIds );
                        message.append( "\t" );
                        message.append( roomId );
                        message.append( "\n" );
                        message.append( userIds );
                        message.append( "\n" );
                        message.append( userNames );
                        message.append( "\n" );
                        message.append( msgId );
                        message.append( "\t" );
                        message.append( StringUtil.getNowDateTime() );
                        message.append( "\t" );
                        message.append( talk );
                        Intent sendIntent = new Intent( Define.MSG_NEW_CHAT );
                        sendIntent.putExtra( "MESSAGE", message.toString() );
                        sendIntent.putExtra( "MESSAGEID", msgId.toString() );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        context.sendBroadcast( sendIntent );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        private void sendMessage( int mode, String senderId, String userId, String userHigh, String name, String icon )
        {
                StringBuffer message = new StringBuffer();
                Intent sendIntent;
                switch ( mode )
                {
                case Define.MENU_ID_GROUP_ADD :
                        /*
                         * command:GroupAdd, param[0] Ggwtest03 command:GroupAdd, param[1] 5
                         * command:GroupAdd, param[2] 0 command:GroupAdd, param[3] test123
                         * command:GroupAdd, param[4] 6
                         */
                        message = new StringBuffer();
                        message.append( "GroupAdd\t" + senderId + "\t" + userId + "\t" + userHigh + "\t" + name + "\t" + icon );
                        sendIntent = new Intent( Define.MSG_MYFOLDER_GROUP_ADD );
                        sendIntent.putExtra( "MESSAGE", message.toString() );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        context.sendBroadcast( sendIntent );
                        break;
                case Define.MENU_ID_GROUP_MOD :
                        /*
                         * command:GroupMod, param[0] Ggwtest03 command:GroupMod, param[1] 5
                         * command:GroupMod, param[2] 0 command:GroupMod, param[3] test
                         */
                        message = new StringBuffer();
                        message.append( "GroupMod\t" + senderId + "\t" + userId + "\t" + userHigh + "\t" + name );
                        sendIntent = new Intent( Define.MSG_MYFOLDER_GROUP_MOD );
                        sendIntent.putExtra( "MESSAGE", message.toString() );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        context.sendBroadcast( sendIntent );
                        break;
                case Define.MENU_ID_GROUP_DEL :
                        /*
                         * command:GroupDel, param[0] Ggwtest03 command:GroupDel, param[1] 6
                         * command:GroupDel, param[2] 5
                         */
                        message = new StringBuffer();
                        message.append( "GroupDel\t" + senderId + "\t" + userId + "\t" + userHigh );
                        sendIntent = new Intent( Define.MSG_MYFOLDER_GROUP_DEL );
                        sendIntent.putExtra( "MESSAGE", message.toString() );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        context.sendBroadcast( sendIntent );
                        break;
                case Define.MENU_ID_SUB_GROUP_ADD :
                        /*
                         * command:GroupAdd, param[0] Ggwtest03 command:GroupAdd, param[1] 6
                         * command:GroupAdd, param[2] 5 command:GroupAdd, param[3] test456
                         * command:GroupAdd, param[4] 6
                         */
                        message = new StringBuffer();
                        message.append( "GroupAdd\t" + senderId + "\t" + userId + "\t" + userHigh + "\t" + name + "\t" + icon );
                        sendIntent = new Intent( Define.MSG_MYFOLDER_SUB_GROUP_ADD );
                        sendIntent.putExtra( "MESSAGE", message.toString() );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        context.sendBroadcast( sendIntent );
                        break;
                case Define.MENU_ID_USER_ADD :
                        /*
                         * command:UserAdd, param[0] Ggwtest03 command:UserAdd, param[1]
                         * D1210226 command:UserAdd, param[2] 3 command:UserAdd, param[3]
                         * test100#02-827-2336##010-9111-6122#khr7643@nongshim.co.kr##CD#
                         * command:UserAdd, param[4] 0
                         */
                        break;
                case Define.MENU_ID_USER_DEL :
                        /*
                         * command:UserDel, param[0] Ggwtest03 command:UserDel, param[1]
                         * D1210226 command:UserDel, param[2] 3
                         */
                        message = new StringBuffer();
                        message.append( "UserDel\t" + senderId + "\t" + userId + "\t" + userHigh );
                        sendIntent = new Intent( Define.MSG_MYFOLDER_USER_DEL );
                        sendIntent.putExtra( "MESSAGE", message.toString() );
                        sendIntent.addFlags( Intent.FLAG_RECEIVER_REGISTERED_ONLY );
                        context.sendBroadcast( sendIntent );
                        break;
                case Define.MENU_ID_MOVE :
                        /*
                         * user move command:UserMove, param[0] Ggwtest03 command:UserMove,
                         * param[1] D1210044 command:UserMove, param[2] 2 command:UserMove,
                         * param[3] 7
                         * group move command:UserMove, param[0] Ggwtest03 command:UserMove,
                         * param[1] 7 command:UserMove, param[2] 0 command:UserMove,
                         * param[3] 5
                         */
                        break;
                }
        }

        //
        private void addGroupFolder()
        {
                AlertDialog.Builder alert = new AlertDialog.Builder( context );
                alert.setTitle( getString( R.string.menu_group_add ) );
                alert.setMessage( getString( R.string.menu_group_add_msg ) );
                final EditText input = new EditText( context );
                alert.setView( input );
                alert.setPositiveButton( getString( R.string.ok ), new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int whichButton )
                        {
                                String value = input.getText().toString();
                                int partId = 0;
                                boolean isDuplication = false;
                                for ( int i = 0; i < items.size(); i++ )
                                {
                                        if ( items.get( i ).isFolder )
                                        {
                                                if ( value.equals( items.get( i ).text ) )
                                                {
                                                        isDuplication = true;
                                                        break;
                                                }
                                                if ( items.get( i ).text.equals( getString( R.string.myPart ) ) )
                                                {}
                                                else if ( partId < Integer.parseInt( items.get( i ).id ) ) partId = Integer.parseInt( items.get( i ).id );
                                        }
                                }
                                if ( isDuplication )
                                {
                                        LayoutInflater inflater = getActivity().getLayoutInflater();
                                        View layout = inflater.inflate( R.layout.custom_toast,
                                                        ( ViewGroup ) getActivity().findViewById( R.id.custom_toast_layout ) );
                                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                        text.setText( getString( R.string.group_duplication ) );
                                        text.setTypeface( Define.tfRegular );
                                        Toast toast = new Toast( getActivity() );
                                        toast.setGravity( Gravity.CENTER, 0, 0 );
                                        toast.setDuration( Toast.LENGTH_SHORT );
                                        toast.setView( layout );
                                        toast.show();
                                }
                                /*
                                 * else
                                 * {
                                 * addFolder( Integer.toString( ++partId ), "0", value.toString(), "6", false );
                                 * }
                                 */
                                IBinder token = input.getWindowToken();
                                (( InputMethodManager ) context.getSystemService( Context.INPUT_METHOD_SERVICE )).hideSoftInputFromWindow( token, 0 );
                                sendMessage( Define.MENU_ID_GROUP_ADD, Define.getMyId( context ), Integer.toString( ++partId ), "0", value.toString(), "6" );
                                nowSelectedPartId = null;
                                nowSelectedPartName = null;
                                dialog.dismiss();
                                if ( !isDuplication ) BuddyView.instance().buddyHandler.sendEmptyMessageDelayed( Define.AM_REFRESH, 1500 );
                        }
                } );
                alert.setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int whichButton )
                        {
                                nowSelectedPartId = null;
                                nowSelectedPartName = null;
                                IBinder token = input.getWindowToken();
                                (( InputMethodManager ) context.getSystemService( Context.INPUT_METHOD_SERVICE )).hideSoftInputFromWindow( token, 0 );
                                dialog.dismiss();
                        }
                } );
                alert.show();
                TRACE( "Group ADD" );
        }

        private void addSubGroupFolder()
        {
                AlertDialog.Builder alert = new AlertDialog.Builder( context );
                alert.setTitle( getString( R.string.menu_sub_group_add ) );
                alert.setMessage( getString( R.string.menu_group_add_msg ) );
                final EditText input = new EditText( context );
                alert.setView( input );
                alert.setPositiveButton( getString( R.string.ok ), new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int whichButton )
                        {
                                String value = input.getText().toString();
                                int partId = 0;
                                boolean isDuplication = false;
                                for ( int i = 0; i < items.size(); i++ )
                                {
                                        if ( items.get( i ).isFolder )
                                        {
                                                if ( value.equals( items.get( i ).text ) )
                                                {
                                                        isDuplication = true;
                                                        break;
                                                }
                                                if ( items.get( i ).text.equals( getString( R.string.myPart ) ) )
                                                {}
                                                else
                                                {
                                                        if ( partId < Integer.parseInt( items.get( i ).id ) ) partId = Integer.parseInt( items.get( i ).id );
                                                }
                                        }
                                }
                                if ( isDuplication )
                                {
                                        LayoutInflater inflater = getActivity().getLayoutInflater();
                                        View layout = inflater.inflate( R.layout.custom_toast,
                                                        ( ViewGroup ) getActivity().findViewById( R.id.custom_toast_layout ) );
                                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                        text.setText( getString( R.string.group_duplication ) );
                                        text.setTypeface( Define.tfRegular );
                                        Toast toast = new Toast( getActivity() );
                                        toast.setGravity( Gravity.CENTER, 0, 0 );
                                        toast.setDuration( Toast.LENGTH_SHORT );
                                        toast.setView( layout );
                                        toast.show();
                                }
                                // else addFolder( Integer.toString( ++partId ), nowSelectedPartId, value.toString(), "6", false );
                                IBinder token = input.getWindowToken();
                                (( InputMethodManager ) context.getSystemService( Context.INPUT_METHOD_SERVICE )).hideSoftInputFromWindow( token, 0 );
                                sendMessage( Define.MENU_ID_SUB_GROUP_ADD, Define.getMyId( context ), Integer.toString( ++partId ), nowSelectedPartId,
                                                value.toString(), "6" );
                                nowSelectedPartId = null;
                                nowSelectedPartName = null;
                                dialog.dismiss();
                                if ( !isDuplication ) BuddyView.instance().buddyHandler.sendEmptyMessageDelayed( Define.AM_REFRESH, 1500 );
                        }
                } );
                alert.setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int whichButton )
                        {
                                nowSelectedPartId = null;
                                nowSelectedPartName = null;
                                IBinder token = input.getWindowToken();
                                (( InputMethodManager ) context.getSystemService( Context.INPUT_METHOD_SERVICE )).hideSoftInputFromWindow( token, 0 );
                                dialog.dismiss();
                        }
                } );
                alert.show();
                TRACE( "SUB GROUP ADD" );
        }

        private void modGroupFolder()
        {
                AlertDialog.Builder alert = new AlertDialog.Builder( context );
                alert.setTitle( getString( R.string.menu_group_mod ) );
                alert.setMessage( getString( R.string.menu_group_mod_msg ) );
                final EditText input = new EditText( context );
                alert.setView( input );
                alert.setPositiveButton( getString( R.string.ok ), new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int whichButton )
                        {
                                String value = input.getText().toString();
                                String partHigh = "";
                                int pos = 0;
                                boolean isDuplication = false;
                                for ( int i = 0; i < items.size(); i++ )
                                {
                                        if ( items.get( i ).isFolder )
                                        {
                                                if ( items.get( i ).id.equals( nowSelectedPartId ) )
                                                {
                                                        partHigh = items.get( i ).parentId;
                                                        pos = i;
                                                }
                                                if ( value.equals( items.get( i ).text ) )
                                                {
                                                        isDuplication = true;
                                                        break;
                                                }
                                        }
                                }
                                if ( isDuplication )
                                {
                                        LayoutInflater inflater = getActivity().getLayoutInflater();
                                        View layout = inflater.inflate( R.layout.custom_toast,
                                                        ( ViewGroup ) getActivity().findViewById( R.id.custom_toast_layout ) );
                                        TextView text = ( TextView ) layout.findViewById( R.id.tv );
                                        text.setText( getString( R.string.group_duplication ) );
                                        text.setTypeface( Define.tfRegular );
                                        Toast toast = new Toast( getActivity() );
                                        toast.setGravity( Gravity.CENTER, 0, 0 );
                                        toast.setDuration( Toast.LENGTH_SHORT );
                                        toast.setView( layout );
                                        toast.show();
                                }
                                /*
                                 * else
                                 * {
                                 * items.get( pos ).text = value.toString();
                                 * items.get( pos ).invalidate();
                                 * }
                                 */
                                IBinder token = input.getWindowToken();
                                (( InputMethodManager ) context.getSystemService( Context.INPUT_METHOD_SERVICE )).hideSoftInputFromWindow( token, 0 );
                                sendMessage( Define.MENU_ID_GROUP_MOD, Define.getMyId( context ), nowSelectedPartId, partHigh, value.toString(), "6" );
                                nowSelectedPartId = null;
                                nowSelectedPartName = null;
                                dialog.dismiss();
                                BuddyView.instance().buddyHandler.sendEmptyMessageDelayed( Define.AM_REFRESH, 1500 );
                        }
                } );
                alert.setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int whichButton )
                        {
                                nowSelectedPartId = null;
                                nowSelectedPartName = null;
                                IBinder token = input.getWindowToken();
                                (( InputMethodManager ) context.getSystemService( Context.INPUT_METHOD_SERVICE )).hideSoftInputFromWindow( token, 0 );
                                dialog.dismiss();
                        }
                } );
                alert.show();
                TRACE( "Group MOD" );
        }

        private void removeFolder()
        {
                AlertDialog.Builder alert = new AlertDialog.Builder( context );
                alert.setTitle( getString( R.string.menu_group_del ) );
                alert.setMessage( getString( R.string.menu_group_del_msg ) );
                alert.setPositiveButton( getString( R.string.ok ), new DialogInterface.OnClickListener() {
                        @SuppressWarnings( "unchecked" )
                        public void onClick( DialogInterface dialog, int whichButton )
                        {
                                /*
                                 * ArrayList<TreeItem> tempItem = new ArrayList<TreeItem>();
                                 * tempItem = ( ArrayList<TreeItem> ) items.clone();
                                 * String partHigh = "";
                                 * for ( int i = 0; i < tempItem.size(); i++ )
                                 * {
                                 * String tmpPartId = "";
                                 * if ( tempItem.get( i ).id.indexOf( "/" ) >= 0 ) tmpPartId = tempItem.get( i ).id.substring( tempItem.get( i ).id
                                 * .indexOf( "/" ) + 1 );
                                 * else tmpPartId = tempItem.get( i ).id;
                                 * if ( nowSelectedPartId.equals( tmpPartId ) )
                                 * {
                                 * partHigh = tempItem.get( i ).parentId;
                                 * for ( int j = 0; j < tempItem.get( i ).m_child.size(); j++ )
                                 * tempItem.remove( tempItem.get( i ).m_child.get( j ) );
                                 * tempItem.remove( tempItem.get( i ) );
                                 * }
                                 * }
                                 * clear();
                                 * for ( TreeItem tmp : tempItem )
                                 * {
                                 * if ( tmp.isFolder ) addFolder( tmp.id, tmp.parentId, tmp.text, "6", false );
                                 * else
                                 * {
                                 * String name = tmp.id.substring( tmp.id.indexOf( "/" ) + 1 );
                                 * addFile( tmp.id, tmp.parentId, userMap.get( name ), tmp.info, tmp.icon, tmp.sortOrder );
                                 * String mIcon = Define.searchMobileOn.get( tmp.id );
                                 * if ( mIcon != null && (mIcon.equals( "0" ) || mIcon.equals( "1" )) ) setMobileOn( tmp.id,
                                 * Integer.parseInt( Define.searchMobileOn.get( tmp.id ) ) );
                                 * }
                                 * }
                                 */
                                sendMessage( Define.MENU_ID_GROUP_DEL, Define.getMyId( context ), nowSelectedPartId, nowSelectedPartHigh, "", "6" );
                                nowSelectedPartId = null;
                                nowSelectedPartName = null;
                                nowSelectedPartHigh = null;
                                dialog.dismiss();
                                BuddyView.instance().buddyHandler.sendEmptyMessageDelayed( Define.AM_REFRESH, 1500 );
                        }
                } );
                alert.setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int whichButton )
                        {
                                nowSelectedPartId = null;
                                nowSelectedPartName = null;
                                dialog.dismiss();
                        }
                } );
                alert.show();
                TRACE( "Group DEL" );
        }

        private void removeFile()
        {
                AlertDialog.Builder alert = new AlertDialog.Builder( context );
                alert.setTitle( getString( R.string.menu_user_del ) );
                alert.setMessage( getString( R.string.menu_user_del_msg ) );
                alert.setPositiveButton( getString( R.string.ok ), new DialogInterface.OnClickListener() {
                        @SuppressWarnings( "unchecked" )
                        public void onClick( DialogInterface dialog, int whichButton )
                        {
                                // ArrayList<TreeItem> tempItem = new ArrayList<TreeItem>();
                                // tempItem = ( ArrayList<TreeItem> ) items.clone();
                                // String partHigh = "";
                                /*
                                 * for ( int i = 0; i < tempItem.size(); i++ )
                                 * {
                                 * String tmpUserId = "";
                                 * if ( tempItem.get( i ).id.indexOf( "/" ) >= 0 ) tmpUserId = tempItem.get( i ).id.substring( tempItem.get( i ).id
                                 * .indexOf( "/" ) + 1 );
                                 * else tmpUserId = tempItem.get( i ).id;
                                 * if ( nowSelectedUserId.equals( tmpUserId ) )
                                 * {
                                 * partHigh = tempItem.get( i ).parentId;
                                 * tempItem.remove( tempItem.get( i ) );
                                 * }
                                 * }
                                 */
                                /*
                                 * int size = items.size();
                                 * for(int i=0; i<size; i++)
                                 * {
                                 * String tmpUserId = "";
                                 * if ( items.get( i ).id.indexOf( "/" ) >= 0 )
                                 * tmpUserId = items.get( i ).id.substring( items.get( i ).id.indexOf( "/" ) + 1 );
                                 * else
                                 * tmpUserId = items.get( i ).id;
                                 * if(nowSelectedUserId.equals( tmpUserId ))
                                 * partHigh = items.get( i ).parentId;
                                 * }
                                 */
                                // clear();
                                /*
                                 * for ( TreeItem tmp : tempItem )
                                 * {
                                 * if ( tmp.isFolder ) addFolder( tmp.id, tmp.parentId, tmp.text, "6", false );
                                 * else
                                 * {
                                 * String name = tmp.id.substring( tmp.id.indexOf( "/" ) + 1 );
                                 * addFile( tmp.id, tmp.parentId, userMap.get( name ), tmp.info, tmp.icon, tmp.sortOrder );
                                 * String mIcon = Define.searchMobileOn.get( tmp.id );
                                 * if ( mIcon != null && (mIcon.equals( "0" ) || mIcon.equals( "1" )) ) setMobileOn( tmp.id,
                                 * Integer.parseInt( Define.searchMobileOn.get( tmp.id ) ) );
                                 * }
                                 * }
                                 */
                                TRACE( "REMOVE user id:" + nowSelectedUserId + ", high:" + nowSelectedPartHigh );
                                sendMessage( Define.MENU_ID_USER_DEL, Define.getMyId( context ), nowSelectedUserId, nowSelectedPartHigh, "", "0" );
                                nowSelectedUserId = null;
                                nowSelectedUserName = null;
                                nowSelectedPartHigh = null;
                                dialog.dismiss();
                                BuddyView.instance().buddyHandler.sendEmptyMessageDelayed( Define.AM_REFRESH, 1500 );
                        }
                } );
                alert.setNegativeButton( getString( R.string.cancel ), new DialogInterface.OnClickListener() {
                        public void onClick( DialogInterface dialog, int whichButton )
                        {
                                nowSelectedUserId = null;
                                nowSelectedUserName = null;
                                dialog.dismiss();
                        }
                } );
                alert.show();
                TRACE( "USER DEL" );
        }

        public TreeItem getItem( String id )
        {
                try
                {
                        if ( id == null || id.equals( "" ) ) return null;
                        else
                        {
                                for ( int i = 0; i < items.size(); i++ )
                                        if ( id.equals( items.get( i ).id ) ) return items.get( i );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return null;
        }

        private int getItemIndex( TreeItem item )
        {
                for ( int i = 0; i < layout.getChildCount(); i++ )
                {
                        if ( layout.getChildAt( i ) == item ) return i;
                }
                assert (false);
                return -1;
        }
        public Handler treeHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_FIRST_EXPAND )
                                {
                                        TRACE( "FirstExpand goto Or" );
                                        OnFirstExpand( ( String ) msg.obj );
                                }
                                else if ( msg.what == Define.AM_SEARCH_COMPLETE )
                                {
                                        OnSearchComplete();
                                }
                                else if ( msg.what == Define.AM_SHOW_PROGRESS )
                                {
                                        String[] arg = ( String[] ) msg.obj;
                                        TableLayout alert = ( TableLayout ) view.findViewById( R.id.Waiter );
                                        TextView ctlTitle = ( TextView ) getActivity().findViewById( R.id.custom_title2 );
                                        TextView ctlContent = ( TextView ) getActivity().findViewById( R.id.content2 );
                                        ctlTitle.setText( arg[0] );
                                        ctlContent.setText( arg[1] );
                                        alert.setVisibility( View.VISIBLE );
                                }
                                else if ( msg.what == Define.AM_HIDE_PROGRESS )
                                {
                                        TableLayout alert = ( TableLayout ) view.findViewById( R.id.Waiter );
                                        alert.setVisibility( View.INVISIBLE );
                                }
                                else
                                {
                                        super.handleMessage( msg );
                                }
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                        }
                }
        };

        public void OnSearchComplete()
        {
        }

        public void OnFirstExpand( String str )
        {
        }

        public void onUserClicked( View view )
        {
        }

        public void OnCustomClick( View view )
        {
        }

        public void showProgress( String title, String content )
        {
                String[] ar = new String[2];
                ar[0] = title;
                ar[1] = content;
                Message m = treeHandler.obtainMessage( Define.AM_SHOW_PROGRESS, ar );
                treeHandler.sendMessage( m );
        }

        public void hideProgress()
        {
                Message m = treeHandler.obtainMessage( Define.AM_HIDE_PROGRESS, null );
                treeHandler.sendMessage( m );
        }

        public TreeItem getItemFavoriteId( String id )
        {
                try
                {
                        if ( id == null || id.equals( "" ) ) return null;
                        else
                        {
                                for ( int i = 0; i < items.size(); i++ )
                                        if ( items.get( i ).id.indexOf( id ) >= 0 ) return items.get( i );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return null;
        }
        private static final String TAG = "/AtSmart/MessengerTree";

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
