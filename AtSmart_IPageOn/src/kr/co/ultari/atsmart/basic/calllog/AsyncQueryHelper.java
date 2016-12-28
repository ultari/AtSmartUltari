package kr.co.ultari.atsmart.basic.calllog;

import java.lang.ref.WeakReference;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class AsyncQueryHelper extends AsyncQueryHandler {
        private WeakReference<NotifyingAsyncQueryListener> mListener;
        public interface NotifyingAsyncQueryListener {
                void onQueryComplete( int token, Object cookie, Cursor cursor );
        }

        public AsyncQueryHelper( ContentResolver resolver, NotifyingAsyncQueryListener listener )
        {
                super( resolver );
                setQueryListener( listener );
        }

        public void setQueryListener( NotifyingAsyncQueryListener listener )
        {
                mListener = (listener != null) ? new WeakReference<NotifyingAsyncQueryListener>( listener ) : null;
        }

        public void clearQueryListener()
        {
                mListener = null;
        }

        public void startQuery( Uri uri, String[] projection )
        {
                startQuery( -1, null, uri, projection, null, null, null );
        }

        public void startQuery( Uri uri, String[] projection, String sortOrder )
        {
                startQuery( -1, null, uri, projection, null, null, sortOrder );
        }

        @Override
        protected void onQueryComplete( int token, Object cookie, Cursor cursor )
        {
                final NotifyingAsyncQueryListener listener = (mListener == null) ? null : mListener.get();
                if ( listener != null )
                {
                        listener.onQueryComplete( token, cookie, cursor );
                }
                else if ( cursor != null )
                {
                        cursor.close();
                }
        }
}
