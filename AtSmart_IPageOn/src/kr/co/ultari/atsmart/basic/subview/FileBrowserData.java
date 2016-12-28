package kr.co.ultari.atsmart.basic.subview;

import java.io.File;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.util.StringUtil;
import kr.co.ultari.atsmart.basic.view.FileBrowser;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileBrowserData extends ArrayAdapter<File> {
        FileBrowser parent;
        File parentFile = null;
        private int selectedPos = -1;

        public FileBrowserData( FileBrowser context )
        {
                super( context, android.R.layout.simple_list_item_1 );
                parent = context;
        }

        public void setSelectedPosition( int pos )
        {
                selectedPos = pos;
                notifyDataSetChanged();
        }

        public int getSelectedPosition()
        {
                return selectedPos;
        }

        public void setParentFile( File f )
        {
                this.parentFile = f;
        }

        @SuppressLint( { "ViewHolder", "InflateParams" } )
        public View getView( int position, View convertView, ViewGroup viewGroup )
        {
                try
                {
                        LayoutInflater inflater = (( Activity ) parent).getLayoutInflater();
                        View row = ( View ) inflater.inflate( R.layout.file_browser_item, null );
                        TextView fileName = ( TextView ) row.findViewById( R.id.fileName );
                        fileName.setTypeface( Define.tfRegular ); //2016-03-31
                        if ( this.parentFile == getItem( position ) )
                        {
                                fileName.setText( " .." );
                        }
                        else fileName.setText( " " + getItem( position ).getName() );
                        ImageView imgView = ( ImageView ) row.findViewById( R.id.fileTypeIcon );
                        if ( getItem( position ).isDirectory() )
                        {
                                imgView.setImageBitmap( parent.m_folderIcon );
                        }
                        else
                        {
                                //2016-03-31
                                String ext = getItem( position ).getName();
                                ext = ext.substring( ext.lastIndexOf( '.' ) + 1 );
                                if ( ext.equalsIgnoreCase( "jpg" ) || ext.equalsIgnoreCase( "jpeg" ) || ext.equalsIgnoreCase( "gif" ) || ext.equalsIgnoreCase( "png" ) || ext.equalsIgnoreCase( "bmp" ) ) 
                                        imgView.setImageBitmap( parent.m_filesIcon );
                                else if ( ext.equalsIgnoreCase( "mp4" ) || ext.equalsIgnoreCase( "avi" ) || ext.equalsIgnoreCase( "mpeg" ) || ext.equalsIgnoreCase( "mpg" ) || ext.equalsIgnoreCase( "mov" ) ) 
                                        imgView.setImageBitmap( parent.m_movieIcon );
                                else if ( ext.equalsIgnoreCase( "mp3" ) || ext.equalsIgnoreCase( "wav" ) || ext.equalsIgnoreCase( "au" ) || ext.equalsIgnoreCase( "amr" ) || ext.equalsIgnoreCase( "m4a" ) ) 
                                        imgView.setImageBitmap( parent.m_audioIcon );
                                else if ( ext.equalsIgnoreCase( "txt" ) ) 
                                        imgView.setImageBitmap( parent.m_textIcon );
                                else if ( ext.equalsIgnoreCase( "doc" ) || ext.equalsIgnoreCase( "docx" ) ) 
                                        imgView.setImageBitmap( parent.m_wordIcon );
                                else if ( ext.equalsIgnoreCase( "xls" ) || ext.equalsIgnoreCase( "xlsx" ) ) 
                                        imgView.setImageBitmap( parent.m_excelIcon );
                                else if ( ext.equalsIgnoreCase( "ppt" ) || ext.equalsIgnoreCase( "pptx" ) ) 
                                        imgView.setImageBitmap( parent.m_powerpointIcon );
                                else if ( ext.equalsIgnoreCase( "pdf" ) ) 
                                        imgView.setImageBitmap( parent.m_pdfIcon );
                                else if ( ext.equalsIgnoreCase( "hwp" ) || ext.equalsIgnoreCase( "x-hwp" ) ) 
                                        imgView.setImageBitmap( parent.m_hanIcon );
                                else 
                                        imgView.setImageBitmap( parent.m_filesIcon );
                                
                                //imgView.setImageBitmap( parent.m_fileIcon );
                                //
                        }
                        TextView fileSize = ( TextView ) row.findViewById( R.id.fileSize );
                        fileSize.setTypeface( Define.tfRegular ); //2016-03-31
                        fileSize.setText( StringUtil.getFileSizeText( getItem( position ).length() ) );
                        if ( selectedPos == position )
                        {
                                //2016-03-31
                                //row.setBackgroundColor( Color.CYAN );
                                row.setBackgroundColor( 0xFFE1EBF1 );
                        }
                        else
                        {
                                //2016-03-31
                                //row.setBackgroundColor( Color.DKGRAY );
                                row.setBackgroundColor( 0xFFFFFFFF );
                        }
                        return row;
                }
                catch ( Exception e )
                {
                        EXCEPTION( e );
                        return null;
                }
        }
        private static final String TAG = "/AtSmart/FileBrowserData";

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
