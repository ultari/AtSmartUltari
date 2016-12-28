package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.dbemulator.Database;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.view.View;
import android.view.Window;

public class ConfigAutoDelete extends MessengerActivity implements RadioButton.OnClickListener {
        private RadioButton NoDelete;
        private RadioButton OneDay;
        private RadioButton OneWeek;
        private RadioButton OneMonth;
        private RadioButton ThreeMonth;
        private RadioButton SixMonth;
        private RadioButton OneYear;
        private Button btnSave;
        private Button btnCancel;
        private int date;
        private String key;
        private TextView tvTitle;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setBackgroundDrawable( new ColorDrawable( Color.TRANSPARENT ) );
                setContentView( R.layout.config_autodelete );
                try
                {
                        tvTitle = ( TextView ) findViewById( R.id.configauto_title );
                        tvTitle.setTypeface( Define.tfBold );  
                        NoDelete = ( RadioButton ) findViewById( R.id._0day );
                        OneDay = ( RadioButton ) findViewById( R.id._1day );
                        OneWeek = ( RadioButton ) findViewById( R.id._7day );
                        OneMonth = ( RadioButton ) findViewById( R.id._30day );
                        ThreeMonth = ( RadioButton ) findViewById( R.id._90day );
                        SixMonth = ( RadioButton ) findViewById( R.id._180day );
                        OneYear = ( RadioButton ) findViewById( R.id._365day );
                        NoDelete.setTypeface( Define.tfRegular );  
                        OneDay.setTypeface( Define.tfRegular );  
                        OneWeek.setTypeface( Define.tfRegular );  
                        OneMonth.setTypeface( Define.tfRegular );  
                        ThreeMonth.setTypeface( Define.tfRegular );  
                        SixMonth.setTypeface( Define.tfRegular );  
                        OneYear.setTypeface( Define.tfRegular );  
                        NoDelete.setOnClickListener( this );
                        OneDay.setOnClickListener( this );
                        OneWeek.setOnClickListener( this );
                        OneMonth.setOnClickListener( this );
                        ThreeMonth.setOnClickListener( this );
                        SixMonth.setOnClickListener( this );
                        OneYear.setOnClickListener( this );
                        key = getIntent().getStringExtra( "KEY" );
                        String value = Database.instance( this ).selectConfig( key );
                        if ( value.equals( "" ) ) value = "0";
                        btnSave = ( Button ) findViewById( R.id.configauto_ok );
                        btnCancel = ( Button ) findViewById( R.id.configauto_cancel );
                        btnSave.setTypeface( Define.tfRegular );  
                        btnCancel.setTypeface( Define.tfRegular );  
                        btnSave.setOnClickListener( this );
                        btnCancel.setOnClickListener( this );
                        date = Integer.parseInt( value );
                        if ( value.equals( "0" ) ) NoDelete.setChecked( true );
                        else if ( value.equals( "1" ) ) OneDay.setChecked( true );
                        else if ( value.equals( "7" ) ) OneWeek.setChecked( true );
                        else if ( value.equals( "30" ) ) OneMonth.setChecked( true );
                        else if ( value.equals( "90" ) ) ThreeMonth.setChecked( true );
                        else if ( value.equals( "180" ) ) SixMonth.setChecked( true );
                        else if ( value.equals( "365" ) ) OneYear.setChecked( true );
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void onClick( View v )
        {
                try
                {
                        if ( v == NoDelete )
                        {
                                setData( 0 );
                        }
                        else if ( v == OneDay )
                        {
                                setData( 1 );
                        }
                        else if ( v == OneWeek )
                        {
                                setData( 7 );
                        }
                        else if ( v == OneMonth )
                        {
                                setData( 30 );
                        }
                        else if ( v == ThreeMonth )
                        {
                                setData( 90 );
                        }
                        else if ( v == SixMonth )
                        {
                                setData( 180 );
                        }
                        else if ( v == OneYear )
                        {
                                setData( 365 );
                        }
                        else if ( v == btnCancel )
                        {
                                finish();
                        }
                        else if ( v == btnSave )
                        {
                                Database.instance( this ).updateConfig( key, this.date + "" );
                                finish();
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void setData( int date )
        {
                this.date = date;
        }
        private static final String TAG = "/AtSmart/ConfigAutoDelete";

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
