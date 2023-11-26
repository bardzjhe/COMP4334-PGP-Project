import auth.PGP;
import model.EncryptedMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;

public class chatbox {
    private JTextArea textArea;
    private JTextField textField;
    private JTextField receiverNameField;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String clientName;
    private KeyPair senderKeyPair;  // Declare senderKeyPair as a member variable

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

    public chatbox(String clientName) {
        this.clientName = clientName;

        // Generate senderKeyPair once during object creation
        senderKeyPair = generateKeyPair();

        JFrame frame = new JFrame("GRP 23's Email Application");
        textArea = new JTextArea(20, 50);
        textArea.setEditable(false);

        // text size
        Font textAreaFont = new Font("Arial", Font.PLAIN, 16); // 16是字体大小
        textArea.setFont(textAreaFont);

        textField = new JTextField(50);
        receiverNameField = new JTextField(50);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Receiver Name:"));
        panel.add(receiverNameField);
        panel.add(new JLabel("Message:"));
        panel.add(textField);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Send clientName and receiverName only once
                    if (output != null) {
                        output.writeObject(clientName);
                        output.writeObject(receiverNameField.getText());
                    }

                    String sendText = textField.getText();
                    String msgName = "Message";

                    // ENCRYPT BEFORE SENDING MESSAGE
                    KeyPair recipientKeyPair = generateKeyPair();

                    PGP senderPGP = new PGP(128);

                    senderPGP.setMyPrivateKey(senderKeyPair.getPrivate());
                    senderPGP.setMyPublicKey(senderKeyPair.getPublic());
                    senderPGP.setTheOtherPublicKey(recipientKeyPair.getPublic());

                    EncryptedMessage encryptedMessage = senderPGP.encrypt(msgName, sendText);
                    //

                    if (output != null) {
                        output.writeObject(encryptedMessage);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                textField.setText("");
            }
        });
        panel.add(sendButton);

        // button size
        Font buttonFont = new Font("Arial", Font.PLAIN, 20);
        sendButton.setFont(buttonFont);

        frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.pack();
        Socket socket;

        try {
            socket = new Socket("localhost", 7000);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            // Send clientName at the beginning
            output.writeObject(clientName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Object receivedMessage = input.readObject();
                        if (receivedMessage == null) {
                            break; // Exit the loop when no more messages are available
                        }

                        if (receivedMessage instanceof String) {
                            System.out.printf("String received.");
                            textArea.append("Email received at " + new Date() + "\n");
                            textArea.append(receivedMessage + "\n");
                            textArea.append("---------------------\n");

                        } else {
                            System.out.printf("EncryptedMessage received.");

                            EncryptedMessage encryptedMessage = (EncryptedMessage) receivedMessage;

                            textArea.append("Email received at " + new Date() + "\n");

                            // decryption
                            KeyPair recipientKeyPair = generateKeyPair();
                            PGP receiverPGP = new PGP(128);

                            receiverPGP.setMyPrivateKey(recipientKeyPair.getPrivate());
                            receiverPGP.setMyPublicKey(recipientKeyPair.getPublic());
                            receiverPGP.setTheOtherPublicKey(senderKeyPair.getPublic());

                            String decryptedMessage = receiverPGP.decrypt(encryptedMessage);

                            if (decryptedMessage != null) {
                                System.out.println("Decrypted Message: " + decryptedMessage);
                                textArea.append(decryptedMessage + "\n");
                                textArea.append("---------------------\n");
                            } else {
                                System.out.println("Decryption failed");
                            }
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
