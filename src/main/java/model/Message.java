package model;

import java.io.Serializable;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 26/11/2023
 * @Description:
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 42L;
    private String sender;
    private String formattedMessage;
    private EncryptedMessage encryptedMessage;
    public Message(String sender, String formattedMessage, EncryptedMessage encryptedMessage) {
        this.sender = sender;
        this.formattedMessage = formattedMessage;
        this.encryptedMessage = encryptedMessage;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }



    public String getFormattedMessage() {
        return formattedMessage;
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }

    public EncryptedMessage getEncryptedMessage() {
        return encryptedMessage;
    }

    public void setEncryptedMessage(EncryptedMessage encryptedMessage) {
        this.encryptedMessage = encryptedMessage;
    }
}
