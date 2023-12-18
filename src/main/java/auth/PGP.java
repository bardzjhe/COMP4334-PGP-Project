package auth;

import model.EncryptedMessage;

import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;



/**
 * @Authors: Anthony HE, anthony.zj.he@outlook.com
 * @Date 22/10/2023
 * @Description: PGP Encryption and Decryption implementation.
 */
public class PGP {
    private static final Logger LOGGER = Logger.getLogger( PGP.class.getName() );
    private int keySize;
    // my keys
    private PublicKey myPublicKey;
    private PrivateKey myPrivateKey;
    // others' keys
    private PublicKey theOtherPublicKey;

    public PGP(int keySize) {
        this.keySize = keySize;
    }

    public void setMyPrivateKey(PrivateKey myPrivateKey) {
        this.myPrivateKey = myPrivateKey;
    }

    public void setMyPublicKey(PublicKey myPublicKey) {
        this.myPublicKey = myPublicKey;
    }

    public void setTheOtherPublicKey(PublicKey theOtherPublicKey) {
        this.theOtherPublicKey = theOtherPublicKey;
    }

    private boolean checkAllKeysExist(){
        if (myPublicKey != null
                && myPrivateKey != null
                && theOtherPublicKey != null){
            return true;
        }
        return false;
    }

    public EncryptedMessage encrypt(String msgName, String msg){

        if(!checkAllKeysExist()){
            System.err.println("Keys not properly set.");
            return null;
        }

        try {

            // Session key based on AES
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(keySize); // Initialize the key generator to create a key of size 128 bits.
            SecretKey sessionKey = keyGen.generateKey();

            // Digital Signature based on the combination of SHA-1 and RSA
            // The hash code of a message is created using SHA-1.
            Signature signature = Signature.getInstance("SHA1withRSA");
            // The message digest is then encrypted using RSA with sender's private key.
            signature.initSign(myPrivateKey);

            // the file to send should be converted to byte format.
            byte[] messageBytes = msg.getBytes(StandardCharsets.UTF_8);
            signature.update(messageBytes);
            byte[] digitalSignature = signature.sign();

            // Encrypt the byte message using session key.
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey);

            // Initialization vector should be transmitted together.
            // It's not a secret, so we don't have to encrypt it.
            // Reference: https://stackoverflow.com/questions/38059749/handling-transfer-of-iv-initialization-vectors
            byte[] iv = aesCipher.getIV();

            //test code
            //System.out.println(bytesToHex(iv));

            byte[] ciphertext = aesCipher.doFinal(messageBytes);

            // Encrypt session key with the public key of receiver.
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, theOtherPublicKey);
            byte[] encryptedCast128Key = rsaCipher.doFinal(sessionKey.getEncoded());

            return new EncryptedMessage(msgName, encryptedCast128Key, iv, digitalSignature, ciphertext);

        } catch (NoSuchPaddingException | IllegalBlockSizeException |
        NoSuchAlgorithmException | BadPaddingException |
        InvalidKeyException | SignatureException ex) {
            LOGGER.log( Level.SEVERE, ex.toString(), ex );
            return null;
        }
    }

    public String decrypt(EncryptedMessage encryptedMessage) {

        if (!checkAllKeysExist()) {
            System.err.println("Keys not properly set.");
            return null;
        }

        if(encryptedMessage == null){
            System.err.println("Message is null");
            return null;
        }

        byte[] encryptedSessionKey = encryptedMessage.getEncryptedSessionKey();
        byte[] iv = encryptedMessage.getIv();
        byte[] ciphertext = encryptedMessage.getCiphertext();
        byte[] digitalSignature = encryptedMessage.getDigitalSignature();

        try {
            // Decrypt session key with the private key of the receiver
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, myPrivateKey);

            // Check for null or empty encrypted session key
            if (encryptedSessionKey == null || encryptedSessionKey.length == 0) {
                System.err.println("Encrypted session key is null or empty.");
                return null;
            }

            byte[] decryptedSessionKey = rsaCipher.doFinal(encryptedSessionKey);
            // Check for null or empty decrypted session key
            if (decryptedSessionKey == null || decryptedSessionKey.length == 0) {
                System.err.println("Decrypted session key is null or empty.");
                return null;
            }

            // Reconstruct the session key
            SecretKey sessionKey = new SecretKeySpec(decryptedSessionKey, 0, decryptedSessionKey.length, "AES");

            // Decrypt the message using the session key and IV
            Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            aesCipher.init(Cipher.DECRYPT_MODE, sessionKey, ivParameterSpec);

            byte[] decryptedMessageBytes = aesCipher.doFinal(ciphertext);

            // Verify the digital signature using the sender's public key
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initVerify(theOtherPublicKey);
            signature.update(decryptedMessageBytes);

            if (!signature.verify(digitalSignature)) {
                System.err.println("Digital signature verification failed.");
                return null;
            }

            // Return the decrypted message
            return new String(decryptedMessageBytes, StandardCharsets.UTF_8);

        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException | SignatureException |
                 InvalidAlgorithmParameterException ex) {
            LOGGER.log(Level.SEVERE, "Decryption error: " + ex.getMessage(), ex);
            return null;
        }
    }
}
