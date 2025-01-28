/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package load.file;

/**
 *
 * @author diego
 */
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Random;
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * * Encryption and Decryption of String data; PBE(Password Based Encryption
 * and Decryption)
 *
 * @author Vikram
 */
public class CryptoUtil {

    public static String key_pwd = "SystelPass8520";
    private static Random rand = new Random((new Date()).getTime());

    Cipher ecipher;
    Cipher dcipher;
    // 8-byte Salt
    byte[] salt = {
        (byte) 0xA9, (byte) 0x9B, (byte) 0xC8, (byte) 0x32,
        (byte) 0x56, (byte) 0x35, (byte) 0xE3, (byte) 0x03
    };
    // Iteration count
    int iterationCount = 19;

//    public CryptoUtil() {
//
//    }
    /**
     *
     * @param secretKey Key used to encrypt data
     * @param plainText Text input to be encrypted
     * @return Returns encrypted text
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.spec.InvalidKeySpecException
     * @throws javax.crypto.NoSuchPaddingException
     * @throws java.security.InvalidKeyException
     * @throws java.security.InvalidAlgorithmParameterException
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws javax.crypto.BadPaddingException
     *
     */
    public String encrypt(String secretKey, String plainText)
            throws NoSuchAlgorithmException,
            InvalidKeySpecException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            UnsupportedEncodingException,
            IllegalBlockSizeException,
            BadPaddingException {
        //Key generation for enc and desc
        KeySpec keySpec = new PBEKeySpec(secretKey.toCharArray(), salt, iterationCount);
        SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
        // Prepare the parameter to the ciphers
        AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);

        //Enc process
        ecipher = Cipher.getInstance(key.getAlgorithm());
        ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
        String charSet = "UTF-8";
        byte[] in = plainText.getBytes(charSet);
        byte[] out = ecipher.doFinal(in);
        String encStr = new String(Base64.getEncoder().encode(out));
        return encStr;
    }

    /**
     * @param secretKey Key used to decrypt data
     * @param encryptedText encrypted text input to decrypt
     * @return Returns plain text after decryption
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.spec.InvalidKeySpecException
     * @throws javax.crypto.NoSuchPaddingException
     * @throws java.security.InvalidKeyException
     * @throws java.security.InvalidAlgorithmParameterException
     * @throws java.io.UnsupportedEncodingException
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws javax.crypto.BadPaddingException
     */
    public String decrypt(String secretKey, String encryptedText)
            throws NoSuchAlgorithmException,
            InvalidKeySpecException,
            NoSuchPaddingException,
            InvalidKeyException,
            InvalidAlgorithmParameterException,
            UnsupportedEncodingException,
            IllegalBlockSizeException,
            BadPaddingException,
            IOException {

        if (secretKey == null || encryptedText == null) //Key generation for enc and desc
        {
            return "";
        }
        KeySpec keySpec = new PBEKeySpec(secretKey.toCharArray(), salt, iterationCount);
        SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
        // Prepare the parameter to the ciphers
        AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
        //Decryption process; same key will be used for decr
        dcipher = Cipher.getInstance(key.getAlgorithm());
        dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        byte[] enc = Base64.getDecoder().decode(encryptedText);
        byte[] utf8 = dcipher.doFinal(enc);
        String charSet = "UTF-8";
        String plainStr = new String(utf8, charSet);
        return plainStr;
    }
//    public static void main(String[] args) throws Exception {
//        CryptoUtil cryptoUtil=new CryptoUtil();
//        String key="ezeon8547";   
//        String plain="This is an important message";
//        String enc=cryptoUtil.encrypt(key, plain);
//        System.out.println("Original text: "+plain);
//        System.out.println("Encrypted text: "+enc);
//        String plainAfter=cryptoUtil.decrypt(key, enc);
//        System.out.println("Original text after decryption: "+plainAfter);
//    }

    public  String encrypt(String str) {

        BASE64Encoder encoder = new BASE64Encoder();

        byte[] salt = new byte[8];

        rand.nextBytes(salt);

        return encoder.encode(salt) + encoder.encode(str.getBytes());
    }

    public  String decrypt(String encstr) {

        if (encstr.length() > 12) {

            String cipher = encstr.substring(12);

            BASE64Decoder decoder = new BASE64Decoder();

            try {

                return new String(decoder.decodeBuffer(cipher));

            } catch (IOException e) {

                //  throw new InvalidImplementationException(
                //Fail
            }

        }

        return null;
    }
}
