package kr.co.ultari.atsmart.basic.codec;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import kr.co.ultari.atsmart.basic.Define;

public class AmCodec {
        public AmCodec()
        {
        }

        public String EncryptSEED( byte[] plainData, int offset, int len )
        {
                StringBuffer encryptString = new StringBuffer();
                byte[] pbData = new byte[16];
                byte[] outData = new byte[16];
                byte[] pbUserKey = { 0x75, 0x01, 0x65, 0x41, 0x56, 0x73, ( byte ) /*-86*/0xAA, ( byte ) /*-16*/0xF0, 0x02, 0x78,
                                ( byte ) /*-124*/0x84, 0x24, ( byte ) /*-128*/0x80, 0x01, 0x13, 0x01 };
                int[] pdwRoundKey = new int[32];
                try
                {
                        int length = len;
                        // Key Schedule Algorithm
                        SEED.SeedRoundKey( pdwRoundKey, pbUserKey );
                        // CBase64 base64;
                        int nCopyDataLength = 0;
                        for ( int nIndex = offset; nIndex < length; nIndex += 16 )
                        {
                                Arrays.fill( pbData, ( byte ) 0 );
                                Arrays.fill( outData, ( byte ) 0 );
                                nCopyDataLength = (length - nIndex >= 16 ? 16 : length - nIndex);
                                for ( int i = 0; i < nCopyDataLength; i++ )
                                {
                                        pbData[i] = plainData[i + nIndex];
                                }
                                // Encryption Algorithm
                                SEED.SeedEncrypt( pbData, pdwRoundKey, outData );
                                // BASE64 ���ڵ��� �����Ͽ� ��ȣ���� ���Ѵ�.
                                encryptString.append( Base64.encodeBytes( outData ) );
                        }
                }
                catch ( Exception e )
                {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                }
                return encryptString.toString();
        }

        public String EncryptSEED( String plainText )
        {
                String encryptString = "";
                byte[] pbData = new byte[16];
                byte[] outData = new byte[16];
                byte[] pbUserKey = { 0x75, 0x01, 0x65, 0x41, 0x56, 0x73, ( byte ) /*-86*/0xAA, ( byte ) /*-16*/0xF0, 0x02, 0x78,
                                ( byte ) /*-124*/0x84, 0x24, ( byte ) /*-128*/0x80, 0x01, 0x13, 0x01 };
                int[] pdwRoundKey = new int[32];
                try
                {
                        byte[] plainData;
                        if ( Define.useUnicode ) plainData = plainText.getBytes();
                        else plainData = plainText.getBytes( "EUC-KR" );
                        int length = plainData.length;
                        // Key Schedule Algorithm
                        SEED.SeedRoundKey( pdwRoundKey, pbUserKey );
                        // CBase64 base64;
                        int nCopyDataLength = 0;
                        for ( int nIndex = 0; nIndex < length; nIndex += 16 )
                        {
                                Arrays.fill( pbData, ( byte ) 0 );
                                Arrays.fill( outData, ( byte ) 0 );
                                nCopyDataLength = (length - nIndex >= 16 ? 16 : length - nIndex);
                                for ( int i = 0; i < nCopyDataLength; i++ )
                                {
                                        pbData[i] = plainData[i + nIndex];
                                }
                                // Encryption
                                SEED.SeedEncrypt( pbData, pdwRoundKey, outData );
                                // BASE64 encode
                                encryptString += Base64.encodeBytes( outData );
                        }
                }
                catch ( UnsupportedEncodingException e )
                {
                        e.printStackTrace();
                }
                return encryptString;
        }

        public String DecryptSEED( String cipherText )
        {
                byte[] outData = new byte[16];
                byte[] pbUserKey = { 0x75, 0x01, 0x65, 0x41, 0x56, 0x73, ( byte ) /*-86*/0xAA, ( byte ) /*-16*/0xF0, 0x02, 0x78,
                                ( byte ) /*-124*/0x84, 0x24, ( byte ) /*-128*/0x80, 0x01, 0x13, 0x01 };
                int[] pdwRoundKey = new int[32];
                String token = "==";
                StringBuffer DecryptData = new StringBuffer();
                String[] cipherArray = cipherText.split( token );
                // Key Schedule Algorithm
                SEED.SeedRoundKey( pdwRoundKey, pbUserKey );
                try
                {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        for ( String aString : cipherArray )
                        {
                                if ( aString.length() == 0 ) break;
                                String tempString = aString + token;
                                try
                                {
                                        byte[] bytes = tempString.getBytes( "UTF-8" );
                                        byte[] bytes2 = Base64.decode( bytes );
                                        SEED.SeedDecrypt( bytes2, pdwRoundKey, outData );
                                        baos.write( outData );
                                }
                                catch ( Exception e )
                                {
                                        return null;
                                }
                        }
                        byte[] oo = baos.toByteArray();
                        String str = "";
                        if ( Define.useUnicode ) str = new String( oo );
                        else str = new String( oo, "EUC-KR" );
                        DecryptData.append( str );
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                // DecryptData
                String retrunStr = DecryptData.toString();
                return retrunStr.trim();
        }

        public byte[] DecryptSEEDToByte( String cipherText )
        {
                byte[] outData = new byte[16];
                byte[] pbUserKey = { 0x75, 0x01, 0x65, 0x41, 0x56, 0x73, ( byte ) /*-86*/0xAA, ( byte ) /*-16*/0xF0, 0x02, 0x78,
                                ( byte ) /*-124*/0x84, 0x24, ( byte ) /*-128*/0x80, 0x01, 0x13, 0x01 };
                int[] pdwRoundKey = new int[32];
                byte[] oo = null;
                String token = "==";
                String[] cipherArray = cipherText.split( token );
                // Key Schedule Algorithm
                SEED.SeedRoundKey( pdwRoundKey, pbUserKey );
                try
                {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        for ( String aString : cipherArray )
                        {
                                if ( aString.length() == 0 ) break;
                                String tempString = aString + token;
                                try
                                {
                                        byte[] bytes = tempString.getBytes( "UTF-8" );
                                        byte[] bytes2 = Base64.decode( bytes );
                                        SEED.SeedDecrypt( bytes2, pdwRoundKey, outData );
                                        baos.write( outData );
                                }
                                catch ( Exception e )
                                {
                                        return null;
                                }
                        }
                        oo = baos.toByteArray();
                }
                catch ( Exception e )
                {
                        e.printStackTrace();
                }
                return oo;
        }
}