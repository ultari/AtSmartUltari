package kr.co.ultari.atsmart.basic.view;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.subview.FileBrowserData;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class FileBrowser extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {
        public Bitmap m_folderIcon;
        public Bitmap m_fileIcon;
        
        //2016-03-31
        public Bitmap m_hanIcon;
        public Bitmap m_pdfIcon;
        public Bitmap m_powerpointIcon;
        public Bitmap m_excelIcon;
        public Bitmap m_audioIcon;
        public Bitmap m_filesIcon;
        public Bitmap m_movieIcon;
        public Bitmap m_textIcon;
        public Bitmap m_wordIcon;
        private TextView m_tvTitle; 
        //
        
        FileBrowserData m_list;
        ListView m_List;
        TextView m_PathName;
        Button m_btnOk;
        Button m_btnCancel;
        File nowFile = null;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                setContentView( R.layout.file_browser );
                
                if ( Define.useSecureCapture ) getWindow().addFlags( WindowManager.LayoutParams.FLAG_SECURE );
                m_folderIcon = BitmapFactory.decodeResource( getResources(), R.drawable.folder );
                m_fileIcon = BitmapFactory.decodeResource( getResources(), R.drawable.file );
                
                //2016-03-31
                m_hanIcon = BitmapFactory.decodeResource( getResources(), R.drawable.icon_han );
                m_pdfIcon = BitmapFactory.decodeResource( getResources(), R.drawable.icon_pdf );
                m_powerpointIcon = BitmapFactory.decodeResource( getResources(), R.drawable.icon_powerpoint );
                m_excelIcon = BitmapFactory.decodeResource( getResources(), R.drawable.icon_excel );
                m_audioIcon = BitmapFactory.decodeResource( getResources(), R.drawable.icon_audio );
                m_filesIcon = BitmapFactory.decodeResource( getResources(), R.drawable.icon_file );
                m_movieIcon = BitmapFactory.decodeResource( getResources(), R.drawable.icon_movie );
                m_textIcon = BitmapFactory.decodeResource( getResources(), R.drawable.icon_text );
                m_wordIcon = BitmapFactory.decodeResource( getResources(), R.drawable.icon_word );
                
                m_tvTitle = ( TextView ) findViewById( R.id.file_browser_title );
                m_tvTitle.setTypeface( Define.tfRegular ); //2016-03-31
                //
                
                m_PathName = ( TextView ) findViewById( R.id.path );
                m_PathName.setTypeface( Define.tfRegular ); //2016-03-31
                m_list = new FileBrowserData( this );
                m_List = ( ListView ) findViewById( R.id.fileList );
                m_list.setNotifyOnChange( true );
                m_List.setAdapter( m_list );
                m_List.setOnItemClickListener( this );
                m_btnOk = ( Button ) findViewById( R.id.okButton );
                m_btnOk.setTypeface( Define.tfRegular ); //2016-03-31
                m_btnCancel = ( Button ) findViewById( R.id.cancelButton );
                m_btnCancel.setTypeface( Define.tfRegular ); //2016-03-31
                m_btnOk.setOnClickListener( this );
                m_btnCancel.setOnClickListener( this );
                String lastFileDirectory = getIntent().getStringExtra( "DEFAULTPATH" );
                if ( lastFileDirectory == null )
                {
                        File toDir = Environment.getExternalStorageDirectory();
                        try
                        {
                                lastFileDirectory = toDir.getCanonicalPath();
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                                return;
                        }
                }
                else
                {
                        m_btnOk.setVisibility( View.INVISIBLE );
                }
                Browse( lastFileDirectory );
        }

        public void onClick( View view )
        {
                if ( view == m_btnCancel )
                {
                        finish();
                }
                else if ( view == m_btnOk )
                {
                        if ( nowFile != null )
                        {
                                String nowFilePath = "";
                                try
                                {
                                        nowFilePath = nowFile.getCanonicalPath();
                                }
                                catch ( Exception e )
                                {
                                        onPrompt( getString( R.string.choiceRunFile ) );
                                        return;
                                }
                                //android.util.Log.i( "FileBrowser", nowFilePath );
                                Intent i = new Intent();
                                i.putExtra( "PATH", nowFilePath );
                                setResult( RESULT_OK, i );
                                finish();
                        }
                }
        }

        public void onPrompt( String str )
        {
                try
                {
                        AlertDialog.Builder alert = new AlertDialog.Builder( getApplicationContext() );
                        alert.setTitle( getString( R.string.app_name ) );
                        alert.setIcon( R.drawable.icon );
                        alert.setMessage( str );
                        alert.setCancelable( false );
                        alert.setPositiveButton( getString( R.string.ok ), new DialogInterface.OnClickListener() {
                                public void onClick( DialogInterface dialog, int which )
                                {
                                        dialog.dismiss();
                                }
                        } );
                        alert.show();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        protected void onActivityResult( int requestCode, int resultCode, Intent data )
        {
                super.onActivityResult( requestCode, resultCode, data );
        }

        public void onItemClick( AdapterView<?> parent, View view, int position, long id )
        {
                try
                {
                        //android.util.Log.i( "PATH", m_list.getItem( position ).getCanonicalPath() );
                        if ( m_list.getItem( position ).isDirectory() )
                        {
                                Browse( m_list.getItem( position ) );
                        }
                        else
                        {
                                nowFile = m_list.getItem( position );
                                m_list.setSelectedPosition( position );
                        }
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                }
        }

        public void Browse( File f )
        {
                String path = null;
                try
                {
                        path = f.getCanonicalPath();
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return;
                }
                Browse( path );
        }

        public void Browse( String path )
        {
                m_list.clear();
                nowFile = null;
                File f = new File( path );
                if ( !f.exists() || f.listFiles() == null )
                {
                        f = Environment.getExternalStorageDirectory();
                        if ( !f.exists() || f.listFiles() == null ) f = new File( "/" );
                }
                
                //2016-03-31
                //android.util.Log.i( "SetText", f.getName() );
                //m_PathName.setText( getString( R.string.attachFile ) );
                try
                {
                        m_PathName.setText( f.getCanonicalPath() );
                }
                catch ( IOException e1 )
                {
                        e1.printStackTrace();
                }
                //
                
                File parent = f.getParentFile();
                if ( parent != null )
                {
                        m_list.setParentFile( parent );
                        m_list.add( parent );
                }
                File[] fl = f.listFiles();
                if ( fl == null )
                {
                        return;
                }
                for ( int i = 0; i < fl.length; i++ )
                {
                        insertFileToList( fl[i] );
                        /*try
                        {
                                android.util.Log.i( "LIST", fl[i].getCanonicalPath() );
                        }
                        catch ( Exception e )
                        {
                                EXCEPTION( e );
                                return;
                        }*/
                }
        }

        private void insertFileToList( File f )
        {
                boolean m_bAdded = false;
                for ( int i = 0; i < m_list.getCount(); i++ )
                {
                        if ( f.isDirectory() && m_list.getItem( i ).isFile() )
                        {
                                m_list.insert( f, i );
                                m_bAdded = true;
                                break;
                        }
                        else if ( f.isFile()
                                        && m_list.getItem( i ).isFile()
                                        && f.getName().toLowerCase( Locale.getDefault() )
                                                        .compareTo( m_list.getItem( i ).getName().toLowerCase( Locale.getDefault() ) ) < 0 )
                        {
                                m_list.insert( f, i );
                                m_bAdded = true;
                                break;
                        }
                        else if ( f.isDirectory()
                                        && m_list.getItem( i ).isDirectory()
                                        && f.getName().toLowerCase( Locale.getDefault() )
                                                        .compareTo( m_list.getItem( i ).getName().toLowerCase( Locale.getDefault() ) ) < 0 )
                        {
                                m_list.insert( f, i );
                                m_bAdded = true;
                                break;
                        }
                }
                if ( !m_bAdded )
                {
                        m_list.add( f );
                }
        }
        private static final String TAG = "/AtSmart/FileBrowser";

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
