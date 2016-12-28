package kr.co.ultari.atsmart.basic.control.tree;

import java.io.BufferedInputStream;
import java.util.ArrayList;

import kr.co.ultari.atsmart.basic.ActionManager;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.util.ImageUtil;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.util.UltariSocketUtil;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

@SuppressLint( { "ViewConstructor", "Assert", "HandlerLeak" } )
public class TreeItem extends View implements Runnable, OnTouchListener
{
        public boolean isCheck;
        public String id;
        public boolean isFolder;
        public int fmcIcon;
        public int icon;
        public int ucIcon;
        public int mobile;
        public String text;
        public String info;
        public String parentId;
        public int depth;
        public boolean isVisible;
        public ArrayList<TreeItem> m_child;
        public String sortOrder;
        public boolean isFirstExpand = true;
        public boolean m_bOpenOnInit = false;
        private String userNum = "";
        private int groupNum, userOn;
        public int mobileOn;
        public String status = "";
        Paint textPaint = null;
        Paint subtextPaint = null;
        Paint infoPaint = null;
        Paint borderPaint = null;
        Paint backPaint = null;
        public boolean isSelected;
        public boolean isExpanded;
        private MessengerTree parent;
        public boolean m_bMenuRegistered = false;
        private TreeItem instance;
        private int nameLeft = 0;
        private String strUrl = "";
        private Thread thread;
        private Bitmap pic = null;
        private Bitmap arrow = null;
        private int IMAGE_MAX_WIDTH = 0;
        private int IMAGE_MAX_HEIGHT = 0;
        private Rect arrowBounds;

        public TreeItem( MessengerTree context, String id, int icon, String text, String info, String parentId, int depth, String sortOrder,
                        boolean m_bOpenOnInit )
        {
                super( context.getActivity() );
                
                this.setOnTouchListener(this);
                
                try
                {
                        this.parent = context;
                        this.id = id;
                        if ( icon >= 6 ) this.isFolder = true;
                        else
                        {
                                this.isFolder = false;
                                this.icon = icon;
                        }
                        ucIcon = 5;
                        mobile = 0;
                        fmcIcon = 0;
                        this.text = text;
                        this.info = StringUtil.getNickName( info, icon );
                        if ( this.info.equals( "" ) )
                        {
                                this.info = StringUtil.getNick( Define.mContext, info, icon );
                        }
                        this.parentId = parentId;
                        this.depth = depth;
                        this.isVisible = true;
                        this.sortOrder = sortOrder;
                        this.m_bOpenOnInit = m_bOpenOnInit;
                        if ( isFolder ) m_child = new ArrayList<TreeItem>();
                        init();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void init()
        {
        	arrowBounds = new Rect(0,0,0,0);
        	
                isSelected = false;
                isExpanded = m_bOpenOnInit;
                textPaint = new Paint();
                textPaint.setAntiAlias( true );
                textPaint.setTextSize( getResources().getDimensionPixelSize( R.dimen.measurefontsize ) );
                textPaint.setTypeface( Define.tfRegular );
                subtextPaint = new Paint();
                subtextPaint.setAntiAlias( true );
                subtextPaint.setTextSize( getResources().getDimensionPixelSize( R.dimen.infofontsize ) );
                subtextPaint.setTypeface( Define.tfRegular );
                if ( isFolder )
                {
                        textPaint.setColor( Define.TREE_NORMAL_FOLDER_TEXT_COLOR );
                        subtextPaint.setColor( Define.TREE_NORMAL_FOLDER_TEXT_COLOR );
                }
                else
                {
                        textPaint.setColor( Define.TREE_NORMAL_TEXT_COLOR );
                        subtextPaint.setColor( 0xFF7F7F7F );
                }
                infoPaint = new Paint();
                infoPaint.setAntiAlias( true );
                infoPaint.setTextSize( getResources().getDimensionPixelSize( R.dimen.infofontsize ) );
                infoPaint.setColor( Define.TREE_INFO_TEXT_COLOR );
                infoPaint.setTypeface( Define.tfRegular );
                borderPaint = new Paint();
                borderPaint.setColor( Define.TREE_BORDER_COLOR );
                backPaint = new Paint();
                if ( isFolder ) backPaint.setColor( Define.TREE_FOLDER_BACK_COLOR );
                else backPaint.setColor( Define.TREE_NORMAL_BACK_COLOR );
                // USERIMAGEICON
                if ( !isFolder )
                {
                        pic = null;
                        IMAGE_MAX_WIDTH = Define.getDpFromPx( parent.context, 180 );  // Define.TREE_USER_ITEM_HEIGHT
                        IMAGE_MAX_HEIGHT = Define.getDpFromPx( parent.context, 180 ); // Define.TREE_USER_ITEM_HEIGHT
                        instance = this;
                        String imgId = id;
                        if ( id.indexOf( "/" ) >= 0 ) imgId = id.substring( id.indexOf( "/" ) + 1 );
                        if ( Define.getSmallBitmap( imgId ) == null )
                        {
                                thread = new Thread( this );
                                thread.start();
                        }
                        else 
                        {
                                pic = Define.getSmallBitmap( imgId );
                                pic = ImageUtil.getDrawOval( pic );
                        }
                }
        }

        // USERIMAGEICON
        public void run()
        {
                String imgId = id;
                if ( id.indexOf( "/" ) >= 0 ) imgId = id.substring( id.indexOf( "/" ) + 1 );
                pic = UltariSocketUtil.getUserImage( imgId, 180, 180 );
                if ( pic != null )
                {
                        Message m = treeItemHandler.obtainMessage( Define.AM_REDRAW_IMAGE, null );
                        treeItemHandler.sendMessage( m );
                        pic = ImageUtil.getDrawOval( pic );
                        Define.setSmallBitmap( imgId, pic );
                }
        }

        public int addChild( TreeItem child )
        {
                try
                {
                        int index = -1;
                        for ( int i = 0; i < m_child.size(); i++ )
                        {
                                // person
                                if ( m_child.get( i ).isFolder == true && child.isFolder == false )
                                {
                                        index = i;
                                        break;
                                }
                                // online
                                /*
                                 * else if ( m_child.get(i).isFolder == false && child.isFolder == false && m_child.get(i).icon == 0 && child.icon > 0 )
                                 * {
                                 * index = i;
                                 * break;
                                 * }
                                 */
                                // sort
                                else if ( m_child.get( i ).sortOrder.compareTo( child.sortOrder ) > 0 )
                                {
                                        index = i;
                                        break;
                                }
                        }
                        if ( index >= 0 )
                        {
                                m_child.add( index, child );
                                return index;
                        }
                        else
                        {
                                m_child.add( child );
                                return m_child.size() - 1;
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return 0;
                }
        }

        // 모바일 표시만 나옴
        /*
         * @Override
         * public void onDraw(Canvas canvas)
         * {
         * try
         * {
         * textPaint.setColor( 0xFFD2D2D2 );
         * subtextPaint.setColor(0xFF949494);//598BBB
         * canvas.drawRect(0,0,getWidth(),getHeight(), borderPaint);
         * int mobileLeft = 0;
         * int mobileTop = 0;
         * if(depth==0)
         * {
         * //backPaint.setColor(0xFF2c2c2c);
         * backPaint.setColor(0x232323);
         * canvas.drawRect(1,1,getWidth()-1,getHeight()-2, backPaint);
         * }
         * else if(depth==1)
         * {
         * backPaint.setColor(0x232323);
         * canvas.drawRect(1,1,getWidth()-1,getHeight()-2, backPaint);
         * }
         * else if(depth > 1)
         * {
         * //backPaint.setColor(0x202020);
         * backPaint.setColor(0x232323);
         * canvas.drawRect(1,1,getWidth()-1,getHeight()-2, backPaint);
         * }
         * int left = Define.DPFromPixel(Define.TREE_DEPTH_TAB_SIZE) * depth;
         * int iconTop = 0;
         * int checkButtonHeight = (Define.getDpFromPx( parent.context, Define.TREE_ITEM_HEIGHT) - MessengerTree.checkUserBitmap.getHeight()) / 2 +
         * Define.DPFromPixel( 15 );
         * int checkButtonWidth = getWidth() - MessengerTree.checkUserBitmap.getWidth() - Define.DPFromPixel(10);
         * if ( isFolder )
         * iconTop = Define.DPFromPixel(Define.PixelFromDP(( getHeight() / 2 ) - ( MessengerTree.expandBitmap.getHeight() / 2 )));
         * else
         * iconTop = Define.DPFromPixel(Define.PixelFromDP(( getHeight() / 2 ) - ( MessengerTree.statusOnlineBitmap.getHeight() / 2 )));
         * if ( isFolder && isExpanded )
         * {
         * if(parentId.equals( "0" ))
         * canvas.drawBitmap(MessengerTree.expandBitmap, left + Define.DPFromPixel(10), iconTop, null);
         * else
         * canvas.drawBitmap(MessengerTree.depthExpandBitmap, left + Define.DPFromPixel(10), iconTop, null);
         * iconTop = Define.DPFromPixel(5);
         * if(Define.isBuddyAddMode)
         * {
         * if(isCheck)
         * canvas.drawBitmap(MessengerTree.checkUserBitmap, checkButtonWidth, checkButtonHeight, null);
         * else
         * canvas.drawBitmap(MessengerTree.uncheckUserBitmap, checkButtonWidth, checkButtonHeight, null);
         * }
         * }
         * else if ( isFolder )
         * {
         * if(parentId.equals( "0" ))
         * canvas.drawBitmap(MessengerTree.collapseBitmap, left + Define.DPFromPixel(10), iconTop, null);
         * else
         * canvas.drawBitmap(MessengerTree.depthCollapseBitmap, left + Define.DPFromPixel(10), iconTop, null);
         * iconTop = Define.DPFromPixel(5);
         * if(Define.isBuddyAddMode)
         * {
         * if(isCheck)
         * canvas.drawBitmap(MessengerTree.checkUserBitmap, checkButtonWidth, checkButtonHeight, null);
         * else
         * canvas.drawBitmap(MessengerTree.uncheckUserBitmap, checkButtonWidth, checkButtonHeight, null);
         * }
         * }
         * else
         * {
         * //USERIMAGE
         * if ( pic != null )
         * {
         * Rect src = new Rect();
         * src.left = 0;
         * src.top = 0;
         * src.right = pic.getWidth();
         * src.bottom = pic.getHeight();
         * Rect dst = new Rect();
         * dst.left = left - IMAGE_MAX_WIDTH/3;
         * dst.top = 0;
         * dst.right = left - IMAGE_MAX_WIDTH/3 + IMAGE_MAX_WIDTH;
         * dst.bottom = 0 + IMAGE_MAX_HEIGHT -3;
         * canvas.drawBitmap(pic, src, dst, null);
         * }
         * left += IMAGE_MAX_WIDTH + MessengerTree.statusMoblieOnBitmap.getWidth();
         * iconTop += Define.DPFromPixel(10);
         * nameLeft = left-(MessengerTree.statusMoblieOnBitmap.getWidth()) + Define.DPFromPixel(10);
         * int padding = Define.DPFromPixel(10);
         * if ( mobile == 1 )
         * canvas.drawBitmap(MessengerTree.statusMoblieOnBitmap, left-(MessengerTree.statusMoblieOnBitmap.getWidth() * 2) + padding , iconTop - padding, null);
         * else
         * canvas.drawBitmap(MessengerTree.statusMoblieOffBitmap, left-(MessengerTree.statusMoblieOffBitmap.getWidth() * 2) + padding, iconTop - padding, null);
         * mobileLeft = left - MessengerTree.statusMoblieOnBitmap.getWidth() + Define.DPFromPixel( 15 );
         * mobileTop = iconTop + Define.DPFromPixel(15);
         * iconTop = Define.DPFromPixel(5);
         * if(Define.isBuddyAddMode)
         * {
         * if(isCheck)
         * canvas.drawBitmap(MessengerTree.checkUserBitmap, checkButtonWidth, checkButtonHeight, null);
         * else
         * canvas.drawBitmap(MessengerTree.uncheckUserBitmap, checkButtonWidth, checkButtonHeight, null);
         * }
         * }
         * left += MessengerTree.statusOnlineBitmap.getWidth();
         * if ( isFolder )
         * left += MessengerTree.expandBitmap.getWidth() + Define.DPFromPixel(5);
         * else
         * left += MessengerTree.statusOnlineBitmap.getWidth() + Define.DPFromPixel(5);
         * if ( isFolder )
         * {
         * if(parentId.equals( "0" )) textPaint.setColor( 0xFF7A8CC5 );
         * else textPaint.setColor( 0xFFD2D2D2 );
         * canvas.drawText(text + userNum, left, ( getHeight() / 2 ) - ( Define.TREE_TEXT_SIZE / 2 ) + Define.TREE_TEXT_SIZE - 2, textPaint);
         * textPaint.setColor( Color.rgb( 19, 19, 19 ) );
         * canvas.drawRect( 0, getHeight()-1, getWidth(), getHeight(), textPaint );
         * }
         * else
         * {
         * iconTop += Define.DPFromPixel(5);
         * if(info.equals("")) {
         * canvas.drawText(text, mobileLeft, mobileTop, textPaint);
         * } else {
         * float textWidth = textPaint.measureText(text + " (" + info + ")") + left - IMAGE_MAX_WIDTH - Define.DPFromPixel(15);
         * int screenWidth = getWidth();
         * if(textWidth > screenWidth)
         * {
         * float fiducial = textWidth / (float)info.length();
         * float multiply = (float) (fiducial * 2);
         * float nameWidth = textPaint.measureText(text) + left;
         * String measureInfo = "";
         * int count = textPaint.breakText(info, true, getWidth() - nameWidth - multiply + (Define.DPFromPixel(Define.TREE_DEPTH_TAB_SIZE) * depth), null);
         * for(int j=0; j < count; j++)
         * measureInfo += info.charAt(j);
         * measureInfo += "...";
         * textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.measurefontsize));
         * canvas.drawText(text + " (" + measureInfo + ")", mobileLeft, mobileTop, textPaint);
         * }
         * else
         * {
         * textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.measurefontsize));
         * switch(Define.SET_COMPANY)
         * {
         * case Define.SAEHA:
         * canvas.drawText(text, left, Define.spToPixels(Define.TREE_TEXT_SIZE) + iconTop, textPaint);
         * break;
         * default:
         * canvas.drawText(text + " (" + info + ")", mobileLeft, mobileTop, textPaint);
         * break;
         * }
         * }
         * }
         * subtextPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.infofontsize));
         * textPaint.setColor( Color.rgb( 19, 19, 19 ) );
         * canvas.drawRect( 0, getHeight()-1, getWidth(), getHeight(), textPaint );
         * }
         * }
         * catch(Exception e)
         * {
         * EXCEPTION(e);
         * }
         * }
         */
        
        // 원본 -> PC상태, 모바일 상태 나옴
        @Override
        public void onDraw( Canvas canvas )
        {
                try
                {
                        // textPaint.setColor( 0xFFD2D2D2 );
                        // subtextPaint.setColor( 0xFF949494 );
                        canvas.drawRect( 0, 0, getWidth(), getHeight(), borderPaint );
                        
                        if ( depth == 0 )
                        {
                                if ( isFolder )
                                {
                                        textPaint.setColor( 0xFF74AFEA );
                                        subtextPaint.setColor( 0xFF74AFEA );
                                }
                                else
                                {
                                        textPaint.setColor( 0xFF333333 );
                                        subtextPaint.setColor( 0xFF7F7F7F );
                                        
                                        canvas.drawRect( 1, 1, getWidth() - 1, getHeight() - 2, backPaint );
                                }
                        }
                        else if ( depth == 1 )
                        {
                                if ( isFolder )
                                {
                                        textPaint.setColor( 0xFF333333 );
                                        subtextPaint.setColor( 0xFF333333 );
                                }
                                else
                                {
                                        textPaint.setColor( 0xFF333333 );
                                        subtextPaint.setColor( 0xFF7F7F7F );
                                        
                                        canvas.drawRect( 1, 1, getWidth() - 1, getHeight() - 2, backPaint );
                                }
                        }
                        else if ( depth > 1 )
                        {
                                if ( isFolder )
                                {
                                        textPaint.setColor( 0xFF333333 );
                                        subtextPaint.setColor( 0xFF333333 );
                                }
                                else
                                {
                                        textPaint.setColor( 0xFF333333 );
                                        subtextPaint.setColor( 0xFF7F7F7F );
                                        
                                        canvas.drawRect( 1, 1, getWidth() - 1, getHeight() - 2, backPaint );
                                }
                        }
                        int left = Define.DPFromPixel( Define.TREE_DEPTH_TAB_SIZE ) * depth;
                        int iconTop = 0;
                        int checkButtonHeight = (Define.getDpFromPx( parent.context, Define.TREE_USER_ITEM_HEIGHT ) - MessengerTree.checkUserBitmap.getHeight())
                                        / 2 + Define.DPFromPixel( 15 );
                        int checkButtonWidth = getWidth() - MessengerTree.checkUserBitmap.getWidth() - Define.DPFromPixel( 10 );
                        if ( isFolder ) iconTop = Define.DPFromPixel( Define.PixelFromDP( (getHeight() / 2) - (MessengerTree.expandBitmap.getHeight() / 2) ) );
                        else iconTop = Define.DPFromPixel( Define.PixelFromDP( (getHeight() / 2) - (MessengerTree.statusOnlineBitmap.getHeight() / 2) ) );
                        int infoLeft = 0;
                        if ( isFolder && isExpanded )
                        {
                                if ( parentId.equals( "0" ) ) canvas.drawBitmap( MessengerTree.expandBitmap, left + Define.DPFromPixel( 10 ), iconTop, null );
                                else canvas.drawBitmap( MessengerTree.depthExpandBitmap, left + Define.DPFromPixel( 10 ), iconTop, null );
                                iconTop = Define.DPFromPixel( 5 );
                                if ( Define.isBuddyAddMode )
                                {
                                        if ( isCheck ) canvas.drawBitmap( MessengerTree.checkUserBitmap, checkButtonWidth, checkButtonHeight, null );
                                        else canvas.drawBitmap( MessengerTree.uncheckUserBitmap, checkButtonWidth, checkButtonHeight, null );
                                }
                        }
                        else if ( isFolder && !isExpanded )
                        {
                                if ( parentId.equals( "0" ) ) canvas.drawBitmap( MessengerTree.collapseBitmap, left + Define.DPFromPixel( 10 ), iconTop, null );
                                else canvas.drawBitmap( MessengerTree.depthCollapseBitmap, left + Define.DPFromPixel( 10 ), iconTop, null );
                                iconTop = Define.DPFromPixel( 5 );
                                if ( Define.isBuddyAddMode )
                                {
                                        if ( isCheck ) canvas.drawBitmap( MessengerTree.checkUserBitmap, checkButtonWidth, checkButtonHeight, null );
                                        else canvas.drawBitmap( MessengerTree.uncheckUserBitmap, checkButtonWidth, checkButtonHeight, null );
                                }
                        }
                        else
                        {
                                //image
                                if ( pic != null )
                                {
                                        Rect src = new Rect();
                                        src.left = 0;
                                        src.top = 0;
                                        src.right = pic.getWidth();
                                        src.bottom = pic.getHeight();
                                        Rect dst = new Rect();
                                        dst.left = left - 3;
                                        dst.top = 0;
                                        dst.right = left - 3 + IMAGE_MAX_WIDTH;
                                        dst.bottom = 0 + IMAGE_MAX_HEIGHT - 3;
                                        canvas.drawBitmap( pic, src, dst, null );
                                        
                                        //Log.d( "treeItem", "pic ok! id:"+id );
                                }
                                else
                                {
                                        pic = ImageUtil.getDrawOval(MessengerTree.defaultUserImage);
                                        
                                        Rect src = new Rect();
                                        src.left = 0;
                                        src.top = 0;
                                        src.right = pic.getWidth();
                                        src.bottom = pic.getHeight();
                                        Rect dst = new Rect();
                                        dst.left = left - 3;
                                        dst.top = 0;
                                        dst.right = left - 3 + IMAGE_MAX_WIDTH;
                                        dst.bottom = 0 + IMAGE_MAX_HEIGHT - 3;
                                        canvas.drawBitmap( pic, src, dst, null );
                                        
                                        //Log.d( "treeItem", "pic null! defaultImage draw id:"+id );
                                }
                                
                                left += IMAGE_MAX_WIDTH + MessengerTree.statusMoblieOnBitmap.getWidth() + Define.DPFromPixel( 5 );
                                iconTop += Define.DPFromPixel( 20 );
                                nameLeft = left - (MessengerTree.statusMoblieOnBitmap.getWidth()) + Define.DPFromPixel( 10 );
                                infoLeft = nameLeft;
                                
                                //mobile
                                if ( mobile == 1 ) 
                                        canvas.drawBitmap( MessengerTree.statusMoblieOnBitmap,  left - (MessengerTree.statusMoblieOnBitmap.getWidth()) + Define.DPFromPixel( 10 ), iconTop, null );
                                else 
                                        canvas.drawBitmap( MessengerTree.statusMoblieOffBitmap, left - (MessengerTree.statusMoblieOffBitmap.getWidth()) + Define.DPFromPixel( 10 ), iconTop, null );
                                
                                //uc
                                if ( Define.usePhoneState )
                                {
                                        infoLeft += Define.DPFromPixel( 12 ) + MessengerTree.statusUcOnBitmap.getWidth();
                                        
                                        if ( ucIcon == 1 ) 
                                                canvas.drawBitmap( MessengerTree.statusUcOnBitmap,   left + Define.DPFromPixel( 12 ),  iconTop, null );
                                        if ( ucIcon == 2 || ucIcon == 3 ) 
                                                canvas.drawBitmap( MessengerTree.statusUcRingBitmap, left + Define.DPFromPixel( 12 ),  iconTop, null );
                                        if ( ucIcon == 0 || ucIcon == 5 ) 
                                                canvas.drawBitmap( MessengerTree.statusUcOffBitmap,  left + Define.DPFromPixel( 12 ),  iconTop, null );
                                } 
                                
                                //pc
                                if ( Define.usePcState )
                                {
                                        if(Define.usePhoneState)
                                        {
                                                infoLeft += Define.DPFromPixel( 14 ) + MessengerTree.statusOnlineBitmap.getWidth();
                                                
                                                if ( icon == 1 ) 
                                                        canvas.drawBitmap( MessengerTree.statusOnlineBitmap,  left + Define.DPFromPixel( 14 ) + MessengerTree.statusUcOnBitmap.getWidth(), iconTop, null );
                                                if ( icon == 2 ) 
                                                        canvas.drawBitmap( MessengerTree.statusAwayBitmap,    left + Define.DPFromPixel( 14 ) + MessengerTree.statusUcOnBitmap.getWidth(), iconTop, null );
                                                if ( icon == 3 ) 
                                                        canvas.drawBitmap( MessengerTree.statusMeetingBitmap, left + Define.DPFromPixel( 14 ) + MessengerTree.statusUcOnBitmap.getWidth(), iconTop, null );
                                                if ( icon == 5 ) 
                                                        canvas.drawBitmap( MessengerTree.statusBusyBitmap,    left + Define.DPFromPixel( 14 ) + MessengerTree.statusUcOnBitmap.getWidth(), iconTop, null );
                                                if ( icon == 4 ) 
                                                        canvas.drawBitmap( MessengerTree.statusPhoneBitmap,   left + Define.DPFromPixel( 14 ) + MessengerTree.statusUcOnBitmap.getWidth(), iconTop, null );
                                                if ( icon == 0 ) 
                                                        canvas.drawBitmap( MessengerTree.statusOfflineBitmap, left + Define.DPFromPixel( 14 ) + MessengerTree.statusUcOnBitmap.getWidth(), iconTop, null );
                                        }
                                        //UC상태 없을시
                                        else
                                        {
                                                infoLeft += Define.DPFromPixel( 26 ) + MessengerTree.statusOnlineBitmap.getWidth();
                                                
                                                if ( icon == 1 ) 
                                                        canvas.drawBitmap( MessengerTree.statusOnlineBitmap,  left + Define.DPFromPixel( 12 ), iconTop, null );
                                                if ( icon == 2 ) 
                                                        canvas.drawBitmap( MessengerTree.statusAwayBitmap,    left + Define.DPFromPixel( 12 ), iconTop, null );
                                                if ( icon == 3 ) 
                                                        canvas.drawBitmap( MessengerTree.statusMeetingBitmap, left + Define.DPFromPixel( 12 ), iconTop, null );
                                                if ( icon == 5 ) 
                                                        canvas.drawBitmap( MessengerTree.statusBusyBitmap,    left + Define.DPFromPixel( 12 ), iconTop, null );
                                                if ( icon == 4 ) 
                                                        canvas.drawBitmap( MessengerTree.statusPhoneBitmap,   left + Define.DPFromPixel( 12 ), iconTop, null );
                                                if ( icon == 0 ) 
                                                        canvas.drawBitmap( MessengerTree.statusOfflineBitmap, left + Define.DPFromPixel( 12 ), iconTop, null );
                                        }
                                }
                                
                                //fmc
                                if ( Define.useFmcState )
                                {
                                        infoLeft += MessengerTree.statusFmcOnBitmap.getWidth() + Define.DPFromPixel( 2 );
                                        
                                        if ( fmcIcon == 0 ) 
                                                canvas.drawBitmap( MessengerTree.statusFmcOffBitmap, infoLeft - MessengerTree.statusFmcOnBitmap.getWidth(), iconTop, null );
                                        if ( fmcIcon == 1 ) 
                                                canvas.drawBitmap( MessengerTree.statusFmcOnBitmap, infoLeft - MessengerTree.statusFmcOnBitmap.getWidth(), iconTop, null );
                                        if ( fmcIcon == 2 ) 
                                                canvas.drawBitmap( MessengerTree.statusFmcRingBitmap, infoLeft - MessengerTree.statusFmcOnBitmap.getWidth(), iconTop, null );
                                        
                                        infoLeft += Define.DPFromPixel( 5 );
                                }
                                else
                                        infoLeft += Define.DPFromPixel( 5 );
                                
                                //check
                                iconTop = Define.DPFromPixel( 5 );
                                if ( Define.isBuddyAddMode )
                                {
                                        infoLeft += Define.DPFromPixel( 20 ) + MessengerTree.checkUserBitmap.getWidth();
                                        if ( isCheck ) 
                                                canvas.drawBitmap( MessengerTree.checkUserBitmap, checkButtonWidth, checkButtonHeight, null );
                                        else 
                                                canvas.drawBitmap( MessengerTree.uncheckUserBitmap, checkButtonWidth, checkButtonHeight, null );
                                }
                                
                                if ( MessengerTree.arrow != null )
                                {
                                	canvas.drawBitmap(MessengerTree.arrow, getWidth() - MessengerTree.arrow.getWidth() - 20, (getHeight() / 2 ) - ( MessengerTree.arrow.getHeight() / 2 ), null);
                                	
                                	arrowBounds.left = getWidth() - MessengerTree.arrow.getWidth() - 20;
                                	arrowBounds.top = (getHeight() / 2 ) - ( MessengerTree.arrow.getHeight() / 2);
                                	arrowBounds.right = arrowBounds.left + MessengerTree.arrow.getWidth();
                                	arrowBounds.bottom = arrowBounds.left + MessengerTree.arrow.getHeight();
                                }
                        }
                        
                        left += MessengerTree.statusOnlineBitmap.getWidth();
                        if ( isFolder ) left += MessengerTree.expandBitmap.getWidth() + Define.DPFromPixel( 5 );
                        else left += MessengerTree.statusOnlineBitmap.getWidth() + Define.DPFromPixel( 5 );
                        if ( isFolder )
                        {
                                if ( parentId.equals( "0" ) ) 
                                        textPaint.setColor( 0xFF74AFEA );
                                else 
                                        textPaint.setColor( 0xFF333333 );
                                
                                canvas.drawText( text + userNum, left,  (getHeight() / 2) - (Define.TREE_TEXT_SIZE / 2) + Define.TREE_TEXT_SIZE + Define.DPFromPixel( 5 ), textPaint );
                                textPaint.setColor( 0xFFD6D6D6 );
                                canvas.drawRect( 0, getHeight() - 1, getWidth(), getHeight(), textPaint );
                        }
                        else
                        {
                                iconTop += Define.DPFromPixel( 22 );
                                if ( info.equals( "" ) )
                                        canvas.drawText( text, nameLeft, Define.spToPixels( 10 ) + iconTop, textPaint );
                                else
                                {
                                        canvas.drawText( text, nameLeft, Define.spToPixels( 10 ) + iconTop, textPaint );
                                        
                                        float textWidth = subtextPaint.measureText( info ) + left - IMAGE_MAX_WIDTH;
                                        int screenWidth = getWidth();
                                        if ( textWidth > screenWidth )
                                        {
                                                float fiducial = textWidth / ( float ) info.length();
                                                float multiply = ( float ) (fiducial * 2);
                                                float nameWidth = left;
                                                String measureInfo = "";
                                                int count = subtextPaint.breakText( info, true, getWidth() - nameWidth - multiply + (Define.DPFromPixel( Define.TREE_DEPTH_TAB_SIZE ) * depth), null );
                                                
                                                for ( int j = 0; j < count; j++ )
                                                        measureInfo += info.charAt( j );
                                                
                                                measureInfo += "...";
                                                info = measureInfo;
                                        }
                                        else
                                                textPaint.setTextSize( getResources().getDimensionPixelSize( R.dimen.measurefontsize ) );
                                        
                                        float result = Math.abs( subtextPaint.ascent() + subtextPaint.descent() ) + Define.spToPixels( Define.TREE_TEXT_SIZE ) + (iconTop * 2);
                                        result -= Define.DPFromPixel( 15 );
                                        left -= MessengerTree.statusOnlineBitmap.getWidth() + Define.DPFromPixel( 10 );
                                        canvas.drawText( info, infoLeft, result, subtextPaint );
                                }
                                subtextPaint.setTextSize( getResources().getDimensionPixelSize( R.dimen.infofontsize ) );
                                textPaint.setColor( 0xFFD6D6D6 );
                                //canvas.drawRect( nameLeft, getHeight() - 1, getWidth(), getHeight(), textPaint );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void onClick()
        {
        	Log.d("TREEITEM", "onClick : " + isSelected);
                try
                {
                        if ( !isSelected )
                        {
                        	Log.d("TREEITEM", "onClick" + isSelected);
                                isSelected = true;
                                if ( isFolder ) textPaint.setColor( Define.TREE_SELECT_FOLDER_TEXT_COLOR );
                                else textPaint.setColor( Define.TREE_SELECT_TEXT_COLOR );
                                infoPaint.setColor( Define.TREE_SELECT_INFO_COLOR );
                                if ( isFolder ) backPaint.setColor( Define.TREE_SELECT_FOLDER_BACK_COLOR );
                                else backPaint.setColor( Define.TREE_SELECT_BACK_COLOR );
                                invalidate();
                        }
                        if ( isFolder && !isExpanded )
                        {
                                Expand();
                        }
                        else if ( isFolder )
                        {
                                if ( !Define.isBuddyAddMode ) Collapse();
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void onNormal()
        {
        	Log.d("TREEITEM", "onNormal : " + isSelected);
                try
                {
                        if ( isSelected )
                        {
                                isSelected = false;
                                if ( isFolder ) textPaint.setColor( Define.TREE_NORMAL_FOLDER_TEXT_COLOR );
                                else textPaint.setColor( Define.TREE_NORMAL_TEXT_COLOR );
                                infoPaint.setColor( Define.TREE_INFO_TEXT_COLOR );
                                if ( isFolder ) backPaint.setColor( Define.TREE_FOLDER_BACK_COLOR );
                                else backPaint.setColor( Define.TREE_NORMAL_BACK_COLOR );
                                invalidate();
                                
                                Log.d("TREEITEM", "onNormal" + isSelected);
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public int getItemIndex( TreeItem item )
        {
                try
                {
                        for ( int i = 0; i < m_child.size(); i++ )
                        {
                                if ( m_child.get( i ) == item ) return i;
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                assert (false);
                return -1;
        }

        public void Expand()
        {
                TRACE( "EXPAND" );
                try
                {
                        if ( isFirstExpand )
                        {
                                Message m = parent.treeHandler.obtainMessage( Define.AM_FIRST_EXPAND, id );
                                parent.treeHandler.sendMessage( m );
                                isFirstExpand = false;
                        }
                        if ( !isFolder ) return;
                        if ( !isExpanded )
                        {
                                isExpanded = true;
                                userOn = 0;
                                groupNum = 0;
                                for ( int i = 0; i < m_child.size(); i++ )
                                {
                                        parent.showChild( m_child.get( i ), this );
                                        if ( m_child.get( i ).icon != 0 ) userOn++;
                                        if ( m_child.get( i ).isFolder ) groupNum++;
                                }
                                for ( int i = 0; i < m_child.size(); i++ )
                                {
                                        m_child.get( i ).ExpandTemporary();
                                }
                        }
                        invalidate();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void ExpandTemporary()
        {
                try
                {
                        if ( !isFolder ) return;
                        if ( isExpanded )
                        {
                                for ( int i = 0; i < m_child.size(); i++ )
                                {
                                        parent.showChild( m_child.get( i ), this );
                                }
                                for ( int i = 0; i < m_child.size(); i++ )
                                {
                                        m_child.get( i ).ExpandTemporary();
                                }
                        }
                        invalidate();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void Collapse()
        {
                try
                {
                        if ( !isFolder ) return;
                        if ( isExpanded == true )
                        {
                                isExpanded = false;
                                for ( int i = 0; i < m_child.size(); i++ )
                                {
                                        m_child.get( i ).CollapseTemporary();
                                }
                                for ( int i = 0; i < m_child.size(); i++ )
                                {
                                        parent.hideChild( m_child.get( i ) );
                                }
                        }
                        invalidate();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void CollapseTemporary()
        {
                try
                {
                        if ( !isFolder ) return;
                        if ( isExpanded == true )
                        {
                                for ( int i = 0; i < m_child.size(); i++ )
                                {
                                        m_child.get( i ).CollapseTemporary();
                                }
                                for ( int i = 0; i < m_child.size(); i++ )
                                {
                                        parent.hideChild( m_child.get( i ) );
                                }
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void setMobileOn( int icon )
        {
                this.mobile = icon;
                invalidate();
        }

        public void setUc( int icon )
        {
                this.ucIcon = icon;
                invalidate();
        }

        public void setIcon( int icon )
        {
                this.icon = icon;
                this.parent.getItem( this.parentId ).setUserCount();
                this.status = StringUtil.getStatus( parent.context, icon );
                invalidate();
        }

        public void setUcIcon( int icon )
        {
                this.ucIcon = icon;
                this.parent.getItem( this.parentId ).setUserCount();
                invalidate();
        }

        public void setFmcIcon( int icon )
        {
                this.fmcIcon = icon;
                invalidate();
        }

        public void setMobileIcon( int status )
        {
                this.mobileOn = status;
                invalidate();
        }
        public Handler treeItemHandler = new Handler() {
                public void handleMessage( Message msg )
                {
                        try
                        {
                                if ( msg.what == Define.AM_REDRAW_IMAGE )
                                {
                                        instance.invalidate();
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

        public void updateCheck()
        {
                invalidate();
        }

        public void setCheck()
        {
                isCheck = !isCheck;
                invalidate();
        }

        public void allCheck()
        {
                isCheck = true;
                invalidate();
        }

        public void cancelCheck()
        {
                isCheck = false;
                invalidate();
        }

        public void setText( String text )
        {
                this.text = text;
                invalidate();
        }

        public void setInfo( String info )
        {
                this.info = info;
                invalidate();
        }

        public void setUserCount()
        {
                userOn = 0;
                groupNum = 0;
                for ( int i = 0; i < m_child.size(); i++ )
                {
                        if ( m_child.get( i ).icon != 0 ) userOn++;
                        if ( m_child.get( i ).isFolder ) groupNum++;
                }
                userNum = " (" + userOn + "/" + (m_child.size() - groupNum) + ")";
                invalidate();
        }
        

        public void changeFontSize()
        {
        }
        private static final String TAG = "/AtSmart/TreeItem";

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

		@Override
		public boolean onTouch(View v, MotionEvent event)
		{
			if ( event.getAction() == MotionEvent.ACTION_UP && !isFolder && event.getX() >= arrowBounds.left && event.getY() >= arrowBounds.top && event.getY() <= arrowBounds.bottom )
			{
				parent.popupUserInfo(this);
			}
			
			return false;
		}
}
