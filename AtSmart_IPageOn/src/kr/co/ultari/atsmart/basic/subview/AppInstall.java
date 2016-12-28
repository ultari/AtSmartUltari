package kr.co.ultari.atsmart.basic.subview;

import kr.co.ultari.atsmart.basic.R;
import kr.co.ultari.atsmart.basic.util.AppUtil;
import kr.co.ultari.atsmart.basic.view.MessengerActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

public class AppInstall extends MessengerActivity implements OnClickListener {
        private Button btnMultiview, btnMdm;
        private static final String TAG = "AppInstall";

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
                super.onCreate( savedInstanceState );
                requestWindowFeature( Window.FEATURE_NO_TITLE );
                getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
                setContentView( R.layout.install );
                btnMdm = ( Button ) findViewById( R.id.install_mdm );
                btnMdm.setOnClickListener( this );
                btnMultiview = ( Button ) findViewById( R.id.install_multiview );
                btnMultiview.setOnClickListener( this );
                try
                {
                        Intent startLink = getPackageManager().getLaunchIntentForPackage( "com.ssomon.remotelock.som" );
                        if ( startLink == null )
                        {
                                btnMdm.setEnabled( true );
                                btnMdm.setText( "Install" );
                        }
                        else
                        {
                                btnMdm.setEnabled( false );
                                btnMdm.setText( "Installed" );
                        }
                        Intent intent = new Intent( android.content.Intent.ACTION_VIEW, Uri.parse( "svcviewer://" ) );
                        intent.addCategory( android.content.Intent.CATEGORY_BROWSABLE );
                        PackageManager pm = getPackageManager();
                        boolean isInstalled = !pm.queryIntentActivities( intent, PackageManager.MATCH_DEFAULT_ONLY ).isEmpty();
                        if ( !isInstalled )
                        {
                                btnMultiview.setEnabled( true );
                                btnMultiview.setText( "Install" );
                        }
                        else
                        {
                                btnMultiview.setEnabled( false );
                                btnMultiview.setText( "Installed" );
                        }
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
        }

        @Override
        public void onClick( View v )
        {
                if ( v == btnMdm )
                {
                        Log.d( TAG, "MDM click!" );
                        Intent startLink = getPackageManager().getLaunchIntentForPackage( "com.ssomon.remotelock.som" );
                        if ( startLink == null ) AppUtil.install( AppUtil.MDM, getApplicationContext() );
                        finish();
                }
                else if ( v == btnMultiview )
                {
                        Log.d( TAG, "MultiView click!" );
                        Intent intent = new Intent( android.content.Intent.ACTION_VIEW, Uri.parse( "svcviewer://" ) );
                        intent.addCategory( android.content.Intent.CATEGORY_BROWSABLE );
                        PackageManager pm = getPackageManager();
                        boolean isInstalled = !pm.queryIntentActivities( intent, PackageManager.MATCH_DEFAULT_ONLY ).isEmpty();
                        if ( !isInstalled ) AppUtil.install( AppUtil.MULTIVIEW, getApplicationContext() );
                        finish();
                }
        }
}
