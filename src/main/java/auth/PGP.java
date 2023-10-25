package auth;

import model.Message;

import javax.crypto.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 22/10/2023
 * @Description:
 */
public class PGP {

    private int keySize;
    // my keys
    private PublicKey myPublicKey;
    private PrivateKey myPrivateKey;
    // others' keys
    private PublicKey othersPublicKey;


    public PGP(int keySize) {
        this.keySize = keySize;
    }

    public void setMyPrivateKey(PrivateKey myPrivateKey) {
        this.myPrivateKey = myPrivateKey;
    }

    public void setMyPublicKey(PublicKey myPublicKey) {
        this.myPublicKey = myPublicKey;
    }

    public void setOthersPublicKey(PublicKey othersPublicKey) {
        this.othersPublicKey = othersPublicKey;
    }

    private boolean checkAllKeysExist(){
        if (myPublicKey != null
                && myPrivateKey != null
                && othersPublicKey != null){
            return true;
        }
        return false;
    }

    public Message encrypt(String fileName){

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
            // refer to https://www.baeldung.com/java-digital-signature
            Signature signature = Signature.getInstance("SHA1withRSA");
            // initialize the signature with our private key.
            signature.initSign(myPrivateKey);
            // TODO: Define what message to read.
            // TODO: If we should use hash function here.
            // the file to send should be converted to byte format.
            byte[] messageBytes = Files.readAllBytes(Paths.get("message.txt"));
            signature.update(messageBytes);
            byte[] digitalSignature = signature.sign();

            // Encrypt the byte message using session key.
            Cipher cast128Cipher = Cipher.getInstance("CAST5", "BC");
            cast128Cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
            // TODO: not sure what the role of IV is here.
            byte[] iv = cast128Cipher.getIV();
            byte[] ciphertext = cast128Cipher.doFinal(messageBytes);

            // Encrypt session key with the public key of receiver.
            Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.ENCRYPT_MODE, othersPublicKey);
            byte[] encryptedCast128Key = rsaCipher.doFinal(sessionKey.getEncoded());

            return new Message(fileName, encryptedCast128Key, digitalSignature, ciphertext);

        }catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        }

        

    }
}
