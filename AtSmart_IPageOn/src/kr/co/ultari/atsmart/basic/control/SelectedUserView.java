package kr.co.ultari.atsmart.basic.control;

import java.util.ArrayList;
import kr.co.ultari.atsmart.basic.control.UserButton.OnDeleteUserListener;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class SelectedUserView extends ScrollView implements OnDeleteUserListener {
        private Context context;
        private OnDeleteUserListener deleteUserListener;
        public LinearLayout layout;
        private OnRecalcHeightListener recalcHeightListener;
        private LinearLayout nowHorizontalLayout = null;
        private final float horizontalMargin = 60.0f;
        private final float verticalMargin = 10.0f;
        private float buttonHeight = 60.0f;
        public ScrollView scrollView;
        public ArrayList<UserData> selectedUsers;

        public SelectedUserView( Context context )
        {
                super( context );
                scrollView = this;
                this.context = context;
                this.selectedUsers = new ArrayList<UserData>();
                this.setBackgroundColor( Color.WHITE );
                init();
        }

        private void init()
        {
                this.deleteUserListener = null;
                this.recalcHeightListener = null;
                this.layout = new LinearLayout( context );
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
                params.setMargins( 0, 0, 0, 0 );
                this.layout.setLayoutParams( params );
                this.layout.setOrientation( LinearLayout.VERTICAL );
                this.addView( layout );
                layout.setBackgroundColor( Color.WHITE );
        }

        private LinearLayout getHorizontalLayout( UserButton newButton )
        {
                if ( nowHorizontalLayout == null )
                {
                        nowHorizontalLayout = new LinearLayout( context );
                        nowHorizontalLayout.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ) );
                        nowHorizontalLayout.setOrientation( LinearLayout.HORIZONTAL );
                        layout.addView( nowHorizontalLayout );
                        return nowHorizontalLayout;
                }
                else
                {
                        float widthSum = 0;
                        for ( int i = 0; i < nowHorizontalLayout.getChildCount(); i++ )
                        {
                                UserButton btn = ( UserButton ) nowHorizontalLayout.getChildAt( i );
                                widthSum += btn.getItemWidth();
                        }
                        float screenWidth = getResources().getDisplayMetrics().widthPixels - (horizontalMargin * 2);
                        if ( (widthSum + horizontalMargin + newButton.getItemWidth()) > screenWidth )
                        {
                                nowHorizontalLayout = new LinearLayout( context );
                                nowHorizontalLayout.setLayoutParams( new LinearLayout.LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT ) );
                                nowHorizontalLayout.setOrientation( LinearLayout.HORIZONTAL );
                                layout.addView( nowHorizontalLayout );
                                return nowHorizontalLayout;
                        }
                        else
                        {
                                return nowHorizontalLayout;
                        }
                }
        }

        public void addUser( String id, String name )
        {
                if ( isExist( id ) ) return;
                selectedUsers.add( new UserData( id, name ) );
                recalcAllViews( true );
        }

        public void setEditable( String id, boolean editable )
        {
                for ( int i = 0; i < layout.getChildCount(); i++ )
                {
                        LinearLayout row = ( LinearLayout ) layout.getChildAt( i );
                        for ( int j = 0; j < row.getChildCount(); j++ )
                        {
                                UserButton btn = ( UserButton ) row.getChildAt( j );
                                if ( btn.getUserId().equals( id ) )
                                {
                                        btn.setEditable( editable );
                                }
                        }
                }
                for ( int i = 0; i < selectedUsers.size(); i++ )
                {
                        if ( selectedUsers.get( i ).userId.equals( id ) ) selectedUsers.get( i ).isEditable = editable;
                }
        }

        public UserButton getButton( String id )
        {
                for ( int i = 0; i < layout.getChildCount(); i++ )
                {
                        LinearLayout row = ( LinearLayout ) layout.getChildAt( i );
                        for ( int j = 0; j < row.getChildCount(); j++ )
                        {
                                UserButton btn = ( UserButton ) row.getChildAt( j );
                                if ( btn.getUserId().equals( id ) )
                                {
                                        return btn;
                                }
                        }
                }
                return null;
        }

        public void removeUser( String id )
        {
                UserButton btn = getButton( id );
                if ( btn != null && !btn.isEditable() ) return;
                for ( int i = 0; i < selectedUsers.size(); i++ )
                {
                        if ( selectedUsers.get( i ).userId.equals( id ) )
                        {
                                selectedUsers.remove( i );
                        }
                }
                recalcAllViews( false );
        }

        public boolean isExist( String id )
        {
                for ( int i = 0; i < selectedUsers.size(); i++ )
                {
                        if ( selectedUsers.get( i ).userId.equals( id ) )
                        {
                                return true;
                        }
                }
                return false;
        }

        public ArrayList<UserData> getUsers()
        {
                return selectedUsers;
        }

        private UserButton insertSelectedUser( String id, String name, boolean scroll )
        {
                UserButton btn = new UserButton( context, id, name, 10 );
                btn.setOnDeleteUserListener( this );
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams( ( int ) btn.getItemWidth(), ( int ) btn.getItemHeight() );
                LinearLayout parentLayout = getHorizontalLayout( btn );
                parentLayout.addView( btn, params );
                buttonHeight = btn.getItemHeight();
                if ( scroll )
                {
                        this.post( new Runnable() {
                                @Override
                                public void run()
                                {
                                        scrollView.scrollTo( 0, layout.getHeight() );
                                }
                        } );
                }
                return btn;
        }

        private void notifyRecalcSize()
        {
                if ( recalcHeightListener != null )
                {
                        int maxHeight = ( int ) buttonHeight * 3;
                        int idealHeight = ( int ) ((layout.getChildCount() * buttonHeight) + (verticalMargin * 2));
                        if ( idealHeight > maxHeight )
                        {
                                recalcHeightListener.onRecalcHeight( maxHeight );
                        }
                        else
                        {
                                if ( layout.getChildCount() == 0 )
                                {
                                        recalcHeightListener.onRecalcHeight( 0 );
                                }
                                else
                                {
                                        recalcHeightListener.onRecalcHeight( ( int ) ((layout.getChildCount() * buttonHeight)) );
                                }
                        }
                }
        }

        public void recalcAllViews( boolean scroll )
        {
                layout.removeAllViews();
                nowHorizontalLayout = null;
                for ( int i = 0; i < selectedUsers.size(); i++ )
                {
                        UserData user = selectedUsers.get( i );
                        UserButton btn = insertSelectedUser( user.userId, user.userName, scroll );
                        btn.setEditable( user.isEditable );
                }
                notifyRecalcSize();
        }

        public void setOnDeleteUserListener( OnDeleteUserListener listener )
        {
                this.deleteUserListener = listener;
        }

        public void setOnRecalcHeightListener( OnRecalcHeightListener listener )
        {
                this.recalcHeightListener = listener;
        }

        @Override
        public void onDeleteUser( View view, String userId )
        {
                removeUser( userId );
                if ( deleteUserListener != null ) deleteUserListener.onDeleteUser( view, userId );
        }
        public interface OnRecalcHeightListener {
                public void onRecalcHeight( int height );
        }
        public class UserData {
                public String userId;
                public String userName;
                public boolean isEditable;

                public UserData( String userId, String userName )
                {
                        this.userId = userId;
                        this.userName = userName;
                        this.isEditable = true;
                }
        }
}
