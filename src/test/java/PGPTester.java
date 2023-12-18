import java.security.KeyPair;
import java.security.KeyPairGenerator;

import auth.PGP;
import model.EncryptedMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


/**
 * This class tests the encryption and decryption methods in our PGP.
 *
 * We provide a wide range of testing cases to verify the effectiveness of
 * our PGP implementation.
 */
public class PGPTester {

    PGP senderPGP;
    PGP receiverPGP;

    @BeforeEach
    public void test(){

        // generate keypairs
        KeyPair senderKeyPair = generateKeyPair();
        KeyPair recipientKeyPair = generateKeyPair();

        // init PGP
        senderPGP = new PGP(128);
        senderPGP.setMyPrivateKey(senderKeyPair.getPrivate());
        senderPGP.setMyPublicKey(senderKeyPair.getPublic());
        senderPGP.setTheOtherPublicKey(recipientKeyPair.getPublic());

        receiverPGP = new PGP(128);
        receiverPGP.setMyPrivateKey(recipientKeyPair.getPrivate());
        receiverPGP.setMyPublicKey(recipientKeyPair.getPublic());
        receiverPGP.setTheOtherPublicKey(senderKeyPair.getPublic());

    }

    /**
     * We provide a wide range of testing cases.
     */
    @Test
    void testEquals(){
        // basic case
        assertEquals("String", encryptAndDecrypt("String"));
        // Empty String
        assertEquals("", encryptAndDecrypt(""));
        // Single Character
        assertEquals("A", encryptAndDecrypt("A"));
        // Numeric Input
        assertEquals("12345", encryptAndDecrypt("12345"));
        // Special Characters
        assertEquals("!@#$%^&*()", encryptAndDecrypt("!@#$%^&*()"));
        // Whitespace Characters
        assertEquals("   ", encryptAndDecrypt("   "));
        // Multiple Words
        assertEquals("Hello World", encryptAndDecrypt("Hello World"));
        // Long Input
        assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.", encryptAndDecrypt("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."));
        // Non-ASCII Characters ()
        assertEquals("こんにちは", encryptAndDecrypt("こんにちは"));
        assertEquals("你好", encryptAndDecrypt("你好"));
        // Case with Numbers, Letters, and Special Characters
        assertEquals("abc123!@#", encryptAndDecrypt("abc123!@#"));
    }

    String encryptAndDecrypt(String originalMessage){
        String msgName = "Email";

        // encryption
        System.out.println("Original Message: " + originalMessage);
        EncryptedMessage encryptedMessage = senderPGP.encrypt(msgName, originalMessage);
        String decryptedMessage = null;
        if (encryptedMessage != null) {
            System.out.println("Encryption successful.");

            //System.out.println("Encrypted Message Name: " + encryptedMessage.getMsgName());
            System.out.println("Encrypted Session Key: " + bytesToHex(encryptedMessage.getEncryptedSessionKey()));
            System.out.println("IV: " + bytesToHex(encryptedMessage.getIv()));
            System.out.println("Digital Signature: " + bytesToHex(encryptedMessage.getDigitalSignature()));
            System.out.println("Ciphertext: " + bytesToHex(encryptedMessage.getCiphertext()));

            // decryption
            decryptedMessage = receiverPGP.decrypt(encryptedMessage);
            if (decryptedMessage != null) {
                System.out.println("Decryption successful.");
                System.out.println("Decrypted Message: " + decryptedMessage);
            } else {
                System.err.println("Decryption failed.");
            }
        } else {
            System.err.println("Encryption failed.");
        }

        return decryptedMessage;

    }


    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "null";
        }

        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString();
    }
}
