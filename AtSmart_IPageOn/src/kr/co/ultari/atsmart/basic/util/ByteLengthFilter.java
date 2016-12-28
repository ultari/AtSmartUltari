package kr.co.ultari.atsmart.basic.util;

import java.io.UnsupportedEncodingException;
import android.text.InputFilter;
import android.text.Spanned;

public class ByteLengthFilter implements InputFilter {
        private String mCharset;
        protected int mMaxByte;

        public ByteLengthFilter( int maxbyte, String charset )
        {
                this.mMaxByte = maxbyte;
                this.mCharset = charset;
        }

        @Override
        public CharSequence filter( CharSequence source, int start, int end, Spanned dest, int dstart, int dend )
        {
                String expected = new String();
                expected += dest.subSequence( 0, dstart );
                expected += source.subSequence( start, end );
                expected += dest.subSequence( dend, dest.length() );
                int keep = calculateMaxLength( expected ) - (dest.length() - (dend - dstart));
                if ( keep <= 0 )
                {
                        return "";
                }
                else if ( keep >= end - start )
                {
                        return null;
                }
                else
                {
                        return source.subSequence( start, start + keep );
                }
        }

        protected int calculateMaxLength( String expected )
        {
                return mMaxByte - (getByteLength( expected ) - expected.length());
        }

        private int getByteLength( String str )
        {
                try
                {
                        return str.getBytes( mCharset ).length;
                }
                catch ( UnsupportedEncodingException e )
                {
                        // e.printStackTrace();
                }
                return 0;
        }
}
