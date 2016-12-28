package kr.co.ultari.atsmart.basic.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import kr.co.ultari.atsmart.basic.Define;
import kr.co.ultari.atsmart.basic.codec.AmCodec;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

public class AppUtil {
        private static final String TAG = "AppUtil";
        private static final String SAEHA_APK_NAME = "multiview.apk";
        private static final String MDM_APK_NAME = "remotelock.apk";
        public static final int MULTIVIEW = 0;
        public static final int MDM = 1;

        public static void ssoSmartPlace(Context context)
        {
                AmCodec codec = new AmCodec();
                String customUrl = Define.ssoScheme + codec.EncryptSEED( Define.getMyId( context ) );
                                
                Intent intent = new Intent();
                intent.setAction( Intent.ACTION_VIEW );
                intent.addCategory( Intent.CATEGORY_BROWSABLE );
                intent.addCategory( Intent.CATEGORY_DEFAULT );
                intent.setData( Uri.parse( customUrl ) );
                context.startActivity(intent);
        }
        
        public static void install( int Select, final Context context )
        {
                String path = copyFile( Select, context );
                File apkFile = new File( path );
                if ( !apkFile.exists() )
                {
                        Log.d( TAG, "Saeha apk failed to copy" );
                        return;
                }
                Uri apkUri = Uri.fromFile( new File( path ) );
                Intent packageinstaller = new Intent( Intent.ACTION_VIEW );
                packageinstaller.setDataAndType( apkUri, "application/vnd.android.package-archive" );
                packageinstaller.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                context.startActivity( packageinstaller );
        }

        public static boolean isSaehaViewerInstalledCheck( Context context )
        {
                try
                {
                        /*
                         * Intent startLink = context.getPackageManager().getLaunchIntentForPackage("com.ssomon.remotelock.som");
                         * if(startLink == null)
                         * {
                         * Log.d( TAG, "MDM not install!" );
                         * return false;
                         * }
                         * else
                         * {
                         * Log.d( TAG, "MDM installed!" );
                         * return true;
                         * }
                         */
                        Intent intent = new Intent( android.content.Intent.ACTION_VIEW, Uri.parse( "svcviewer://" ) );
                        intent.addCategory( android.content.Intent.CATEGORY_BROWSABLE );
                        PackageManager pm = context.getPackageManager();
                        boolean isInstalled = !pm.queryIntentActivities( intent, PackageManager.MATCH_DEFAULT_ONLY ).isEmpty();
                        if ( !isInstalled )
                        {
                                Log.d( TAG, "MultiView not install!" );
                                return false;
                        }
                        else
                        {
                                Log.d( TAG, "MultiView installed!" );
                                return true;
                        }
                        /*
                         * if(startLink == null || !isInstalled)
                         * return false;
                         * else
                         * return true;
                         */
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                return false;
        }

        private static String copyFile( int select, Context context )
        {
                InputStream in = null;
                String path = null;
                switch ( select )
                {
                /*
                 * case MULTIVIEW:
                 * in = context.getResources().openRawResource(R.raw.multiview);
                 * path = Environment.getExternalStorageDirectory() + File.separator + SAEHA_APK_NAME;
                 * break;
                 */
                /*
                 * case MDM:
                 * in = context.getResources().openRawResource(R.raw.remotelock);
                 * path = Environment.getExternalStorageDirectory() + File.separator + MDM_APK_NAME;
                 * break;
                 */
                }
                try
                {
                        FileOutputStream out = new FileOutputStream( path );
                        byte[] buff = new byte[1024];
                        int read = 0;
                        try
                        {
                                while ( (read = in.read( buff )) > 0 )
                                {
                                        out.write( buff, 0, read );
                                }
                        }
                        catch ( IOException e )
                        {
                                e.printStackTrace();
                        }
                        finally
                        {
                                try
                                {
                                        in.close();
                                }
                                catch ( IOException e )
                                {
                                        e.printStackTrace();
                                }
                                try
                                {
                                        out.close();
                                }
                                catch ( IOException e )
                                {
                                        e.printStackTrace();
                                }
                        }
                }
                catch ( FileNotFoundException e )
                {
                        e.printStackTrace();
                }
                return path;
        }
}