package src.secure;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSA {

    public static KeyPair getKeyPair(int RSAKeySize) {
        try {
            KeyPairGenerator RSAKeyGen = KeyPairGenerator.getInstance("RSA");
            RSAKeyGen.initialize(RSAKeySize);
            return RSAKeyGen.generateKeyPair();
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }

        return null;
    }

    public static String convertPublicKeyToString(PublicKey publicKey) {
        //converting public key to byte
        return encodeBase64(publicKey.getEncoded());
    }

    public static PublicKey getPublicKeyFromString(String publicKeyString) {
        try {
            byte[] keyBytes = decodeBase64(publicKeyString);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) { e.printStackTrace(); }

        return null;
    }

    public static String encodeBase64(byte[] data){
        return Base64.getEncoder().encodeToString(data);
    }

    public static byte[] decodeBase64(String data){
        return Base64.getDecoder().decode(data);
    }

    public static String encrypt(String message, Key key) {
        try {
            byte[] bytesMessage = message.getBytes();
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedBytes = cipher.doFinal(bytesMessage);
            return encodeBase64(encryptedBytes);
        } catch (Exception e){ e.printStackTrace(); }

        return null;
    }

    public static String decrypt(String encryptedMessage, Key key) {
        try {
            byte[] encryptedBytes = decodeBase64(encryptedMessage);
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decryptedMessage = cipher.doFinal(encryptedBytes);
            return new String(decryptedMessage, StandardCharsets.UTF_8);
        } catch (Exception e) { e.printStackTrace(); }

        return null;
    }

}
