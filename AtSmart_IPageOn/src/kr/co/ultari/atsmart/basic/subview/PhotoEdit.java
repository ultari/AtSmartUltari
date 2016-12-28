package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.control.CropImage;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class PhotoEdit extends MessengerActivity implements OnClickListener {
        CropImage ci;
        private Button btnSave;
        private Button btnCancel;
        private Button btnRotate;
        private TextView tvTitle;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                try
                {
                        requestWindowFeature( Window.FEATURE_NO_TITLE );
                        getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
                        if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE ); //2016-12-13
                        setContentView( R.layout.photo_editor );
                        ci = ( CropImage ) findViewById( R.id.crop_view );
                        tvTitle = ( TextView ) findViewById( R.id.photo_editor_title );
                        tvTitle.setTypeface( Define.tfBold );
                        btnSave = ( Button ) findViewById( R.id.save );
                        btnCancel = ( Button ) findViewById( R.id.cancel );
                        btnRotate = ( Button ) findViewById( R.id.rotate );
                        btnSave.setTypeface( Define.tfRegular );
                        btnCancel.setTypeface( Define.tfRegular );
                        btnRotate.setTypeface( Define.tfRegular );
                        btnSave.setOnClickListener( this );
                        btnCancel.setOnClickListener( this );
                        btnRotate.setOnClickListener( this );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        @Override
        public void onDestroy()
        {
                ci.onDestroy();
                super.onDestroy();
        }

        public void onClick( View v )
        {
                Intent i = new Intent();
                if ( btnCancel == v )
                {
                        setResult( RESULT_CANCELED, i );
                        finish();
                }
                else if ( btnSave == v )
                {
                        ci.save();
                        setResult( RESULT_OK, i );
                        finish();
                }
                else if ( btnRotate == v )
                {
                        ci.rotate();
                }
        }
        private static final String TAG = "/AtSmart/PhotoEdit";

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