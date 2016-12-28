package kr.co.ultari.atsmart.basic.subview;


import java.io.File;
import java.io.IOException;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class backgroundDialog extends Activity implements OnClickListener {
        private static final String TEMP_PHOTO_FILE = "AtTalkTemp.jpg";
        private static final int REQ_CODE_PICK_IMAGE = 0;
        private static final int IMAGE = 1, COLOR = 0;
        private LinearLayout layout_colorList;
        private ImageView image;
        private int select = COLOR, colorValue = 0;
        private TextView tvTitle;
        private static final int[] BUTTONS = { R.id.background_color1, R.id.background_color2, R.id.background_color3, R.id.background_color4,
                        R.id.background_color5, R.id.background_color6, R.id.background_color7, R.id.background_color8, R.id.background_color9,
                        R.id.background_color10, R.id.background_color11, R.id.background_color12, R.id.background_color13, R.id.background_color14,
                        R.id.background_color15 };
        /*private static final int[] COLORS = { Color.rgb( 190, 209, 221 ), Color.rgb( 83, 106, 143 ), Color.rgb( 19, 176, 164 ), Color.rgb( 165, 205, 192 ),
                        Color.rgb( 155, 191, 95 ), Color.rgb( 231, 222, 110 ), Color.rgb( 247, 144, 109 ), Color.rgb( 228, 120, 133 ),
                        Color.rgb( 237, 170, 178 ), Color.rgb( 123, 99, 104 ), Color.rgb( 237, 237, 237 ), Color.rgb( 158, 158, 158 ),
                        Color.rgb( 63, 67, 138 ), Color.rgb( 0, 44, 66 ), Color.rgb( 129, 139, 156 ) };*/
        //2016-04-04
        private static final int[] COLORS = { 0xFFC3E0F2, 0xFFC7D1E1, 0xFFAED6DF, 0xFFBBD9CF, 0xFFBEDFAE, 0xFFE7DE6E, 0xFFFFDFD4, 0xFFFFB7C0,
                                              0xFFFFDCE0, 0xFFDAC2C7, 0xFFEDEDED, 0xFFBDBDBD, 0xFFDDDEFA, 0xFFE4F3CA, 0xFFB2C9D9};
        //
        
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.background_dialog );
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE );
                layout_colorList = ( LinearLayout ) findViewById( R.id.background_colorlist );
                layout_colorList.setVisibility( View.GONE );
                image = ( ImageView ) findViewById( R.id.background_image );
                image.setVisibility( View.VISIBLE );
                tvTitle = ( TextView ) findViewById( R.id.background_title );
                tvTitle.setTypeface( Define.tfBold );
                for ( int btnId : BUTTONS )
                {
                        Button btn = ( Button ) findViewById( btnId );
                        btn.setOnClickListener( this );
                }
                String filePath = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ) + java.io.File.separator + TEMP_PHOTO_FILE;
                if ( Define.SELCT_BACKGROUND_MODE.equals( "IMAGE" ) )
                {
                        Bitmap selectedImage = BitmapFactory.decodeFile( filePath );
                        if ( selectedImage == null ) image.setBackgroundColor( Color.rgb( 190, 209, 221 ) );
                        else
                        {
                                // Drawable img = new BitmapDrawable(selectedImage);
                                image.setBackground( getDrawableFromBitmap( selectedImage ) );
                        }
                }
                else image.setBackgroundColor( Define.SELECT_BACKGROUND_COLOR );
                // album
                Button btnAlbum = ( Button ) findViewById( R.id.background_btn_album );
                btnAlbum.setTypeface( Define.tfRegular );
                btnAlbum.setOnClickListener( new OnClickListener() {
                        @Override
                        public void onClick( View v )
                        {
                                select = IMAGE;
                                Intent intent = new Intent( Intent.ACTION_GET_CONTENT, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI );
                                intent.setType( "image/*" );
                                intent.putExtra( "crop", "true" );
                                intent.putExtra( MediaStore.EXTRA_OUTPUT, getTempUri() );
                                intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
                                startActivityForResult( intent, REQ_CODE_PICK_IMAGE );
                        }
                } );
                // color
                Button btnColor = ( Button ) findViewById( R.id.background_btn_color );
                btnColor.setTypeface( Define.tfRegular );
                btnColor.setOnClickListener( new OnClickListener() {
                        @Override
                        public void onClick( View v )
                        {
                                select = COLOR;
                                image.setVisibility( View.GONE );
                                layout_colorList.setVisibility( View.VISIBLE );
                        }
                } );
                Button btnSave = ( Button ) findViewById( R.id.background_btn_save );
                btnSave.setTypeface( Define.tfRegular );
                btnSave.setOnClickListener( new OnClickListener() {
                        @Override
                        public void onClick( View v )
                        {
                                if ( select == COLOR )
                                {
                                        // color
                                        Define.SELCT_BACKGROUND_MODE = "COLOR";
                                        Define.SELECT_BACKGROUND_COLOR = colorValue;
                                        Database.instance( getApplicationContext() ).updateConfig( "backgroundMode", Define.SELCT_BACKGROUND_MODE );
                                        Database.instance( getApplicationContext() ).updateConfig( "colorValue",
                                                        Integer.toString( Define.SELECT_BACKGROUND_COLOR ) );
                                }
                                else
                                {
                                        // image
                                        Define.SELCT_BACKGROUND_MODE = "IMAGE";
                                        Database.instance( getApplicationContext() ).updateConfig( "backgroundMode", Define.SELCT_BACKGROUND_MODE );
                                }
                                finish();
                        }
                } );
                Button btnClose = ( Button ) findViewById( R.id.background_close );
                btnClose.setOnClickListener( new OnClickListener() {
                        @Override
                        public void onClick( View v )
                        {
                                finish();
                        }
                } );
        }

        public Drawable getDrawableFromBitmap( Bitmap bitmap )
        {
                Drawable d = new BitmapDrawable( getResources(), bitmap );
                return d;
        }

        private Uri getTempUri()
        {
                return Uri.fromFile( getTempFile() );
        }

        private File getTempFile()
        {
                if ( isSDCARDMOUNTED() )
                {
                        File f = new File( Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES ), TEMP_PHOTO_FILE );
                        try
                        {
                                f.createNewFile();
                        }
                        catch ( IOException e )
                        {}
                        return f;
                }
                else return null;
        }

        private boolean isSDCARDMOUNTED()
        {
                String status = Environment.getExternalStorageState();
                if ( status.equals( Environment.MEDIA_MOUNTED ) ) return true;
                return false;
        }

        @SuppressWarnings( "deprecation" )
        protected void onActivityResult( int requestCode, int resultCode, Intent imageData )
        {
                super.onActivityResult( requestCode, resultCode, imageData );
                switch ( requestCode )
                {
                case REQ_CODE_PICK_IMAGE :
                        if ( resultCode == RESULT_OK )
                        {
                                if ( imageData != null )
                                {
                                        String filePath = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES )
                                                        + java.io.File.separator + TEMP_PHOTO_FILE;
                                        Bitmap selectedImage = BitmapFactory.decodeFile( filePath );
                                        // Drawable img = new BitmapDrawable(selectedImage);
                                        ImageView _image = ( ImageView ) findViewById( R.id.background_image );
                                        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) _image
                                                        .setBackground( getDrawableFromBitmap( selectedImage ) );
                                        else _image.setBackgroundDrawable( getDrawableFromBitmap( selectedImage ) );
                                }
                        }
                        break;
                }
        }

        @Override
        public boolean onKeyDown( int keyCode, KeyEvent event )
        {
                if ( event.getAction() == KeyEvent.ACTION_DOWN )
                {
                        if ( keyCode == KeyEvent.KEYCODE_BACK )
                        {
                                finish();
                                return true;
                        }
                }
                return super.onKeyDown( keyCode, event );
        }

        @Override
        public void onClick( View v )
        {
                for ( int i = 0; i < BUTTONS.length; i++ )
                {
                        if ( v.getId() == BUTTONS[i] )
                        {
                                colorValue = COLORS[i];
                                image.setBackgroundColor( colorValue );
                                layout_colorList.setVisibility( View.GONE );
                                image.setVisibility( View.VISIBLE );
                        }
                }
        }
}
