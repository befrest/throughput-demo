package rest.bef.demo.util;

import org.apache.commons.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptUtil {
    public static String md5(String input) {
        return encrypt("MD5", input);
    }

    public static byte[] md5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.update(input.getBytes());
        return messageDigest.digest();
    }

    public static String sha1(String input) {
        return encrypt("SHA1", input);
    }

    public static String encodeBase64(String input) {
        if (input == null)
            return null;

        return new String(Base64.encodeBase64(input.getBytes()));
    }

    public static String decodeBase64(String input) {
        if (input == null)
            return null;

        return new String(Base64.decodeBase64(input));
    }

    private static String encrypt(String algorithm, String input) {
        StringBuilder encryptedString = new StringBuilder();

        try {
            byte[] inputAsByte = input.getBytes();
            MessageDigest messageDigest = MessageDigest.getInstance(algorithm.toUpperCase());
            messageDigest.update(inputAsByte);

            for (byte b : messageDigest.digest())
                encryptedString.append(String.format("%02X", b & 0xff));

        } catch (Exception e) {
            // DO NOTHING
        }

        return encryptedString.toString();
    }
}
