package auth;

import model.EncryptedMessage;

import javax.crypto.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 22/10/2023
 * @Description:
 */
public class PGP {

    private static final Logger LOGGER = Logger.getLogger( PGP.class.getName() );
    private int keySize;
    // my keys
    private PublicKey senderPublicKey;
    private PrivateKey senderPrivateKey;
    // others' keys
    private PublicKey recipientPublicKey;


    public PGP(int keySize) {
        this.keySize = keySize;
    }

    public void setSenderPrivateKey(PrivateKey senderPrivateKey) {
        this.senderPrivateKey = senderPrivateKey;
    }

    public void setSenderPublicKey(PublicKey senderPublicKey) {
        this.senderPublicKey = senderPublicKey;
    }

    public void setRecipientPublicKey(PublicKey recipientPublicKey) {
        this.recipientPublicKey = recipientPublicKey;
    }

    private boolean checkAllKeysExist(){
        if (senderPublicKey != null
                && senderPrivateKey != null
                && recipientPublicKey != null){
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

            // Session key based on CAST-128
            KeyGenerator keyGen = KeyGenerator.getInstance("CAST5", "BC");
            keyGen.init(128); // Initialize the key generator to create a key of size 128 bits.
            SecretKey sessionKey = keyGen.generateKey();

            // Digital Signature based on the combination of SHA-1 and RSA
            // The hash code of a message is created using SHA-1.
            Signature signature = Signature.getInstance("SHA1withRSA");
            // The message digest is then encrypted using RSA with sender's private key.
            signature.initSign(senderPrivateKey);

            // the file to send should be converted to byte format.
            byte[] messageBytes = msg.getBytes(StandardCharsets.UTF_8);
            signature.update(messageBytes);
            byte[] digitalSignature = signature.sign();

            // Encrypt the byte message using session key.
            Cipher cast128Cipher = Cipher.getInstance("CAST5", "BC");
            cast128Cipher.init(Cipher.ENCRYPT_MODE, sessionKey);

            // TODO: please note iv should be transmitted together. it's not a secret so we dont have to encrypt it.
            // https://stackoverflow.com/questions/38059749/handling-transfer-of-iv-initialization-vectors
            byte[] iv = cast128Cipher.getIV();
            byte[] ciphertext = cast128Cipher.doFinal(messageBytes);

            // Encrypt session key with the public key of receiver.
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey);
            byte[] encryptedCast128Key = rsaCipher.doFinal(sessionKey.getEncoded());

            return new EncryptedMessage(msgName, encryptedCast128Key, iv, digitalSignature, ciphertext);

        }catch (NoSuchPaddingException | IllegalBlockSizeException |
        NoSuchAlgorithmException | BadPaddingException | NoSuchProviderException |
        InvalidKeyException | SignatureException ex) {
            LOGGER.log( Level.SEVERE, ex.toString(), ex );
            return null;
        }

    }


    public String decrypt()
}
