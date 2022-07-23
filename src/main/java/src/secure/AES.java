package src.secure;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class AES {

    public static Key generateSymmetricKey(int keySize) {
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(keySize);
            return keygen.generateKey();
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException(e); }
    }

    public static byte[] encrypt(Key key, String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data.getBytes());
        } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException |
                 InvalidKeyException e) { throw new RuntimeException(e); }
    }

    public static byte[] decrypt(Key key, String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data.getBytes());
        } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException |
                 InvalidKeyException e) { throw new RuntimeException(e); }
    }

}
