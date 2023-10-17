package model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 16/10/2023
 * @Description:
 */
public class Message {

    
    private Date sentTime;

    // TODO: 这个我们还是依照 https://en.wikipedia.org/wiki/Pretty_Good_Privacy#:~:text=PGP%20encryption%20uses%20a%20serial,or%20an%20e%2Dmail%20address.
    private byte[] encryptedMsg; // Encrypted data + encrypted session key


    public Message(){

    }
}
