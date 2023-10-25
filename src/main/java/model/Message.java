package model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 16/10/2023
 * @Description: Please refer to the page 8 in http://www.facweb.iitkgp.ac.in/~sourav/PGP.pdf
 * and you can know the PGP message structure.
 *
 *
 */
public class Message {

    /* Session key component */
    // the session key is encrypted using CAST-128
    private byte[] encryptedSessionKey;

    /* Signature */
    byte[] digitalSignature;

    /* Message */
    private Date fileTimestamp;
    private String filename;
    private byte[] ciphertext;


    public Message(String filename, byte[] encryptedSessionKey,
                   byte[] digitalSignature, byte[] ciphertext){
        this.fileTimestamp = new Date();
        this.encryptedSessionKey = encryptedSessionKey;
        this.digitalSignature = digitalSignature;
        this.ciphertext = ciphertext;

    }

}
