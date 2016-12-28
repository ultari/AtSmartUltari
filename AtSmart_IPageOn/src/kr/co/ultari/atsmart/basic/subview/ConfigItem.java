package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.UserImageView;
import kr.co.ultari.atsmart.basic.subdata.ConfigData;
import kr.co.ultari.atsmart.basic.view.ConfigView;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

@SuppressLint( { "InflateParams", "ViewHolder" } )
public class ConfigItem extends ArrayAdapter<ConfigData> implements OnClickListener 
{
        ConfigView parent;
        private Context context;

        public ConfigItem( Context context, ConfigView parent )
        {
                super( context, android.R.layout.simple_list_item_1 );
                this.context = context;
                this.parent = parent;
        }

        public View getView( int position, View convertView, ViewGroup viewGroup )
        {
                LayoutInflater inflater = parent.inflater;
                View row = ( View ) inflater.inflate( R.layout.sub_config_list, null );
                ConfigData data = getItem( position );
                try
                {
                        TextView title = ( TextView ) row.findViewById( R.id.custom_title2 );
                        title.setTypeface( Define.tfRegular );
                        title.setText( data.title );
                        TextView info = ( TextView ) row.findViewById( R.id.info );
                        info.setTypeface( Define.tfRegular );
                        info.setText( data.info );
                        TextView value = ( TextView ) row.findViewById( R.id.value );
                        
                        if ( data.value.indexOf( "USERID://" ) >= 0 )
                        {
                                title.setOnClickListener( this );
                                info.setOnClickListener( this );
                                ViewGroup.LayoutParams params = value.getLayoutParams();
                                params.width = 0;
                                value.setLayoutParams( params );
                                title.setTextColor( Color.rgb( 58, 66, 98 ) );
                                UserImageView img = ( UserImageView ) row.findViewById( R.id.UserIcon );
                                params = img.getLayoutParams();
                                
                                params.width = Define.displayWidth / 2;
                                params.height = Define.displayWidth / 2;
                                //params.width = Define.getDpFromPx( context, 537 ); 
                                //params.height = Define.getDpFromPx( context, 547 ); 
                                img.setLayoutParams( params );
                                img.setMyPhoto();
                                img.setUserId( data.value.substring( 9 ) );
                                //img.setUserIdFromFile( data.value.substring( 9 ) );
                                img.setVisibility( View.VISIBLE );
                                img.setScaleType( ScaleType.FIT_XY );
                                img.setOnClickListener( this );
                                
                                ImageView iv = ( ImageView ) row.findViewById( R.id.arrow );
                                iv.setBackgroundResource( R.drawable.icon_arrow_mystatus_normal );
                                iv.setOnClickListener( this );
                                 
                                RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
                                //saveButtonParams.addRule( RelativeLayout.ALIGN_PARENT_BOTTOM );
                                //saveButtonParams.addRule( RelativeLayout.ALIGN_PARENT_LEFT );
                                param.addRule( RelativeLayout.ALIGN_PARENT_RIGHT );
                                param.topMargin = Define.getDpFromPx( context, 450 );
                                param.rightMargin = Define.getDpFromPx( context, 36 );
                                iv.setLayoutParams( param );
                                
                                info.setTextColor( Color.rgb( 116, 123, 150 ) );
                                
                                String[] parse = Define.totalName.split( "#" );
                                if ( parse != null && parse.length > 2 )
                                {
                                        String myPart = parse[2];
                                        title.setTypeface( Define.tfRegular );
                                        info.setTypeface( Define.tfRegular );
                                        title.setText( parse[0] );
                                        if ( parse.length >= 2 ) info.setText( parse[1] + " / " + myPart );
                                        else info.setText( "" );
                                }
                                else
                                {
                                        title.setTypeface( Define.tfRegular );
                                        info.setTypeface( Define.tfRegular );
                                        title.setText( "" );
                                        info.setText( "" );
                                }
                                
                                LinearLayout layout = ( LinearLayout ) row.findViewById( R.id.config_line );
                                layout.setVisibility( View.GONE );
                                TextView title_edit = ( TextView ) row.findViewById( R.id.config_edit_title );
                                title_edit.setTypeface( Define.tfRegular );
                                title_edit.setVisibility( View.VISIBLE );
                                params = title_edit.getLayoutParams();
                                params.height = Define.getDpFromPx( context, 97 );  
                                title_edit.setLayoutParams( params );
                                title_edit.setOnClickListener( this );
                                TextView title_sub = ( TextView ) row.findViewById( R.id.custom_title_sub );
                                title_sub.setVisibility( View.VISIBLE );
                                title_sub.setTypeface( Define.tfRegular );
                                title_sub.setText( Define.getMyNickName() );
                                title_sub.setOnClickListener( this );
                                TextView info_sub = ( TextView ) row.findViewById( R.id.info_sub );
                                info_sub.setVisibility( View.VISIBLE );
                                info_sub.setTypeface( Define.tfRegular );
                                info_sub.setText( context.getString( R.string.changeMyNick ) );
                                info_sub.setOnClickListener( this );
                                info_sub.setTextColor( 0xFF74AFEA );  
                                LinearLayout divide = ( LinearLayout ) row.findViewById( R.id.config_divide );
                                divide.setVisibility( View.GONE );
                                LinearLayout top = ( LinearLayout ) row.findViewById( R.id.photo_top_back );
                                top.setVisibility( View.VISIBLE );
                                top.setBackgroundColor( 0xFFDEECFA );  
                                top.setGravity( Gravity.CENTER_VERTICAL );
                                params = top.getLayoutParams();
                                
                                 
                                params.width = Define.displayWidth / 2;
                                params.height = ( int ) ((Define.displayWidth / 2) * 0.5); 
                                //params.width = Define.getDpFromPx( context, Define.displayWidth - 537 ); 
                                //params.height = Define.getDpFromPx( context, 274 ); 
                                top.setLayoutParams( params ); 
                                
                                top.setOnClickListener( this );
                                LinearLayout bottom = ( LinearLayout ) row.findViewById( R.id.photo_bottom_back );
                                bottom.setVisibility( View.VISIBLE );
                                bottom.setBackgroundColor( 0xFF003E7C );  
                                bottom.setGravity( Gravity.CENTER_VERTICAL );
                                params = bottom.getLayoutParams();
                                 
                                params.width = Define.displayWidth / 2; 
                                params.height = ( int ) ((Define.displayWidth / 2) * 0.5); 
                                //params.width = Define.getDpFromPx( context, Define.displayWidth - 537 ); 
                                //params.height = Define.getDpFromPx( context, 273 ); 
                                bottom.setLayoutParams( params ); 
                        }
                        else if ( data.key.equals( "account_title" ) )
                        {
                                 
                                RelativeLayout rl = ( RelativeLayout ) row.findViewById( R.id.config_row );
                                rl.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,Define.getDpFromPx( context, 118))); 
                                
                                ImageView iv = ( ImageView ) row.findViewById( R.id.arrow );
                                iv.setVisibility( View.GONE );
                                row.setBackgroundColor( 0xFFF1F1F1 );  
                                title.setTextColor( 0xFF333333 );  
                                info.setVisibility( View.GONE );
                                LinearLayout layout = ( LinearLayout ) row.findViewById( R.id.config_line );
                                layout.setVisibility( View.VISIBLE );
                        }
                        else if ( data.key.equals( "setting" ) )
                        {
                                 
                                RelativeLayout rl = ( RelativeLayout ) row.findViewById( R.id.config_row );
                                rl.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,Define.getDpFromPx( context, 118))); 
                                
                                ImageView iv = ( ImageView ) row.findViewById( R.id.arrow );
                                iv.setVisibility( View.GONE );
                                row.setBackgroundColor( 0xFFF1F1F1 );  
                                title.setTextColor( 0xFF333333 );      
                                info.setVisibility( View.GONE );
                                LinearLayout layout = ( LinearLayout ) row.findViewById( R.id.config_line );
                                layout.setVisibility( View.VISIBLE );
                        }
                        else if ( data.key.equals( "update" ) )
                        {
                                 
                                RelativeLayout rl = ( RelativeLayout ) row.findViewById( R.id.config_row );
                                rl.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,Define.getDpFromPx( context, 200))); 
                                
                                ImageView iv = ( ImageView ) row.findViewById( R.id.arrow );
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams ((int) LayoutParams.WRAP_CONTENT, (int) LayoutParams.WRAP_CONTENT);
                                params.addRule(RelativeLayout.CENTER_VERTICAL);
                                params.addRule( RelativeLayout.ALIGN_PARENT_RIGHT );
                                params.rightMargin = Define.getDpFromPx( context, 36 );
                                iv.setLayoutParams(params);
                                
                                value.setTypeface( Define.tfRegular );
                                value.setText( data.value );
                                if ( Float.parseFloat( Define.NEW_VERSION ) > Float.parseFloat( Define.VERSION ) )
                                        info.setTextColor( Color.RED );
                        }
                        else
                        {
                                 
                                if( data.key.equals( "account" ))
                                {
                                        RelativeLayout rl = ( RelativeLayout ) row.findViewById( R.id.config_row );
                                        rl.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,Define.getDpFromPx( context, 183))); 
                                }
                                else
                                {
                                        RelativeLayout rl = ( RelativeLayout ) row.findViewById( R.id.config_row );
                                        rl.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT,Define.getDpFromPx( context, 200))); 
                                }
                                
                                ImageView iv = ( ImageView ) row.findViewById( R.id.arrow );
                                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams ((int) LayoutParams.WRAP_CONTENT, (int) LayoutParams.WRAP_CONTENT);

                                params.addRule(RelativeLayout.CENTER_VERTICAL);
                                params.addRule( RelativeLayout.ALIGN_PARENT_RIGHT );
                                params.rightMargin = Define.getDpFromPx( context, 36 );
                                iv.setLayoutParams(params);
                                
                                value.setTypeface( Define.tfRegular );
                                value.setText( data.value );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
                return row;
        }
        private static final String TAG = "/AtSmart/ConfigItem";

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
        public void onClick( View v )
        {
                if ( v.getId() == R.id.info_sub || v.getId() == R.id.custom_title_sub || v.getId() == R.id.arrow )
                {
                        Intent selectWindow = new Intent( context, kr.co.ultari.atsmart.basic.subview.ConfigNickName.class );
                        selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                        context.startActivity( selectWindow );
                }
                else if ( v.getId() == R.id.config_edit_title )
                {
                        Intent selectWindow = new Intent( context, kr.co.ultari.atsmart.basic.subview.ConfigPhoto.class );
                        selectWindow.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                        context.startActivity( selectWindow );
                }
                else
                {
                        Log.d( "Config", "View : " + v.getId() );
                }
        }
}
