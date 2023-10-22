package model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 16/10/2023
 * @Description: Please refer to the page 8 in http://www.facweb.iitkgp.ac.in/~sourav/PGP.pdf
 * and you can know the PGP message structure.
 */
public class Message {

    /* Session key component */
    // TODO: the message is encrypted using CAST-128 according to the lecture slides.
    private byte[] sessionKey;
    // TODO: Not sure if it's needed.
    private long publicKeyIdOfRecipient;


    /* Signature */
    private Date SignatureTimestamp;
    private long publicKeyIdOfSender;
    // Any message longer than octetLength must be broken up into
    // smaller segments, each of which is mailed separately.
    private final int octetLength = 50000;
    // Message digest: the hash function used in SHA-1 which creates 160 bit message digest.
    private byte[] messageDigest;


    /* Message */
    private Date fileTimestamp;
    private String filename;
    private String Data;
    private byte[] encryptedMsg; // Encrypted data + encrypted session key


    public Message(){

    }
}
