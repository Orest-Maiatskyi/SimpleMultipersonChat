package src.secure;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Defender {

    private boolean secured = false;
    private boolean loggedIn = false;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    private PublicKey publicKey2;

    public Defender(int RSAKeySize) {
        KeyPair keyPair = RSA.getKeyPair(RSAKeySize);
        if (keyPair != null) {
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } else throw new NullPointerException("Unable to create a keypair.");
    }

    public String encryptMessage(String message) {
        message = RSA.encrypt(message, publicKey2);
        //message = RSA.encrypt(message, publicKey2);
        return message;
    }

    public String decryptMessage(String message) {
        message = RSA.decrypt(message, privateKey);
        //message = RSA.decrypt(message, publicKey2);
        return message;
    }

    public byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    public byte[] generateHash(String password, byte[] salt, int iterationCount, int keyLength) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterationCount, keyLength);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) { e.printStackTrace(); }
        return null;
    }

    public boolean isSecure() { return secured; }

    public boolean isLoggedIn() { return loggedIn; }

    public void setSecureState(boolean state) { this.secured = state; }

    public void setLoggedInState(boolean state) { this.loggedIn = state; }

    public PublicKey getPublicKey() { return publicKey; }

    public void setPublicKey2(PublicKey publicKey2) { this.publicKey2 = publicKey2; }

}
