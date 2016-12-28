package kr.co.ultari.atsmart.basic.util;

public class KoreanChar {
        private final char _value;
        private final boolean _useCompatibilityJamo;
        static final int CHOSEONG_COUNT = 19;
        static final int JUNGSEONG_COUNT = 21;
        static final int JONGSEONG_COUNT = 28;
        static final int HANGUL_SYLLABLE_COUNT = CHOSEONG_COUNT * JUNGSEONG_COUNT * JONGSEONG_COUNT;
        static final int HANGUL_SYLLABLES_BASE = 0xAC00;
        static final int HANGUL_SYLLABLES_END = HANGUL_SYLLABLES_BASE + HANGUL_SYLLABLE_COUNT;
        static final int[] compatibilityChoseong = new int[] { 0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145, 0x3146, 0x3147,
                        0x3148, 0x3149, 0x314A, 0x314B, 0x314C, 0x314D, 0x314E };

        public KoreanChar( char c, boolean useCompatibilityJamo )
        {
                _value = c;
                _useCompatibilityJamo = useCompatibilityJamo;
        }

        public static boolean isHangulChoseong( char c )
        {
                return 0x1100 <= c && c <= 0x1112;
        }

        public static boolean isHangulCompatibilityChoseong( char c )
        {
                return 0x3131 <= c && c <= 0x314E;
        }

        public static boolean isHangulSyllable( char c )
        {
                return HANGUL_SYLLABLES_BASE <= c && c < HANGUL_SYLLABLES_END;
        }

        public char getChoseong()
        {
                final int index = _value - HANGUL_SYLLABLES_BASE;
                final int choseongIndex = index / (JUNGSEONG_COUNT * JONGSEONG_COUNT);
                if ( _useCompatibilityJamo ) return ( char ) compatibilityChoseong[choseongIndex];
                else return ( char ) (choseongIndex + 0x1100);
        }
}
