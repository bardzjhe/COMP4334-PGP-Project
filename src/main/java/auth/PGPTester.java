package auth;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import model.EncryptedMessage;

public class PGPTester {

    public static void main(String[] args) {
        // generate keypairs
        KeyPair senderKeyPair = generateKeyPair();
        KeyPair recipientKeyPair = generateKeyPair();

        // init PGP
        PGP pgp = new PGP(128);
        pgp.setSenderPrivateKey(senderKeyPair.getPrivate());
        pgp.setSenderPublicKey(senderKeyPair.getPublic());
        pgp.setRecipientPublicKey(recipientKeyPair.getPublic());

        String msgName = "Test Message";
        String originalMessage = "Hello, this is a test message.";

        // encryption
        System.out.println("Original Message: " + originalMessage);
        EncryptedMessage encryptedMessage = pgp.encrypt(msgName, originalMessage);
        if (encryptedMessage != null) {
            System.out.println("Encryption successful.");

            //System.out.println("Encrypted Message Name: " + encryptedMessage.getMsgName());
            System.out.println("Encrypted Session Key: " + bytesToHex(encryptedMessage.getEncryptedSessionKey()));
            System.out.println("IV: " + bytesToHex(encryptedMessage.getIv()));
            System.out.println("Digital Signature: " + bytesToHex(encryptedMessage.getDigitalSignature()));
            System.out.println("Ciphertext: " + bytesToHex(encryptedMessage.getCiphertext()));

            // decryption
            String decryptedMessage = pgp.decrypt(encryptedMessage);
            if (decryptedMessage != null) {
                System.out.println("Decryption successful.");
                System.out.println("Decrypted Message: " + decryptedMessage);
            } else {
                System.err.println("Decryption failed.");
            }
        } else {
            System.err.println("Encryption failed.");
        }
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

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString();
    }
}