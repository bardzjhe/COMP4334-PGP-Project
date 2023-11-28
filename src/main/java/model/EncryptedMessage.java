package model;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 16/10/2023
 * @Description: Please refer to the page 8 in http://www.facweb.iitkgp.ac.in/~sourav/PGP.pdf
 * and you can know the PGP message structure. We're implementing in a simplified version.
 *
 * Referred to https://en.wikipedia.org/wiki/Pretty_Good_Privacy,
 * PGP uses a serialization format for storage of both keys and encrypted data.
 */
public class EncryptedMessage implements Serializable {

    private static final long serialVersionUID = 42L;
    /* Session key component */
    // the session key is encrypted using CAST-128
    private byte[] encryptedSessionKey;
    private byte[] iv;

    /* Signature */
    private byte[] digitalSignature;

    /* Message */
    private Date fileTimestamp;
    private String filename; // indicate email type (email/server notification), no need to encrypt
    private byte[] ciphertext;


    public EncryptedMessage(String filename, byte[] encryptedSessionKey, byte[] iv,
                            byte[] digitalSignature, byte[] ciphertext) {
        this.fileTimestamp = new Date();
        this.encryptedSessionKey = encryptedSessionKey;
        this.iv = iv;
        this.digitalSignature = digitalSignature;
        this.ciphertext = ciphertext;
    }

    public byte[] getEncryptedSessionKey() {
        return encryptedSessionKey;
    }

    public byte[] getIv() {
        return iv;
    }

    public byte[] getDigitalSignature() {
        return digitalSignature;
    }

    public Date getFileTimestamp() {
        return fileTimestamp;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getCiphertext() {
        return ciphertext;
    }

}