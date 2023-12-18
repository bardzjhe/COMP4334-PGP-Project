package model;

import auth.PGP;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;

public class Client {
    private JTextArea textArea;
    private JTextField textField;
    private JTextField subjectField;
    static private JTextField receiverNameField;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String clientName;

    private PublicKey myPublicKey;

    private PrivateKey myPrivateKey;

    /**
     * generates key pairs and store in the files.
     * @throws Exception
     */
    public void generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();

        try(FileOutputStream fos = new FileOutputStream("./src/main/java/publickeys/" + clientName + "Public.key")){
            fos.write(pair.getPublic().getEncoded());
        }

        try(FileOutputStream fos = new FileOutputStream("./src/main/java/" + clientName + "/" + clientName + "Private.key")){
            fos.write(pair.getPrivate().getEncoded());
        }
    }

    public PublicKey getPublicKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private PrivateKey getPrivateKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    /**
     * the other refers to the corresponding sender or recipient.
     * @param theOtherName
     * @return
     * @throws Exception
     */
    public PublicKey getTheOtherPublicKey(String theOtherName) throws Exception{
        byte[] keyBytes = Files.readAllBytes(Paths.get("./src/main/java/publickeys/" +
                theOtherName + "Public.key"));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    public Client(String clientName) throws IOException {
        this.clientName = clientName;

        try{
            File publicKeyFile = new File("./src/main/java/publickeys/" + clientName + "Public.key");
            File privateKeyFile = new File("./src/main/java/" + clientName + "/" + clientName + "Private.key");
            if(!privateKeyFile.exists() || !publicKeyFile.exists()){
                // generate the key files if it fails to read it.
                try{
                    generateKeyPair();
                }catch (Exception ex){}
            }
            myPublicKey = getPublicKey("./src/main/java/publickeys/" + clientName + "Public.key");
            myPrivateKey =  getPrivateKey("./src/main/java/" + clientName + "/" + clientName + "Private.key");
        }catch (Exception ex){
            System.err.println(ex);
        }

        JFrame frame = new JFrame("GRP 23 Email Application");
        textArea = new JTextArea(20, 50);
        textArea.setEditable(false);

        // text size
        Font textAreaFont = new Font("Arial", Font.PLAIN, 16); // 16是字体大小
        textArea.setFont(textAreaFont);

        receiverNameField = new JTextField(50);
        subjectField = new JTextField(50);
        textField = new JTextField(50);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel("Receiver Name:"));
        panel.add(receiverNameField);
        panel.add(new JLabel("Email Subject:"));
        panel.add(subjectField);
        panel.add(new JLabel("Message:"));
        panel.add(textField);

        // Two PGP instances used for encryption and decryption
        PGP senderPGP = new PGP(128);
        PGP receiverPGP = new PGP(128);

        Socket socket;
        socket = new Socket("localhost", 7000);
        try {

            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            // Send clientName at the beginning
            output.writeObject(clientName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    // Check if the socket is still connected
                    if (socket != null && !socket.isClosed() && socket.isConnected()) {

                        // Send clientName and receiverName only once
                        if (output != null) {
                            output.writeObject(clientName.trim());
                            output.writeObject(receiverNameField.getText());
                        }

                        String subject = subjectField.getText();
                        String sendText = textField.getText();
                        String msgName = "Message";

                        senderPGP.setMyPrivateKey(myPrivateKey);
                        senderPGP.setMyPublicKey(myPublicKey);
                        senderPGP.setTheOtherPublicKey(getTheOtherPublicKey(receiverNameField.getText()));

                        String message2Encrypt = "Subject: " + subject + "\n\n" +
                                sendText + "\n\n" +
                                "Best regards,\n" +
                                clientName;
                        EncryptedMessage encryptedMessage = senderPGP.encrypt(msgName, message2Encrypt);

                        if (output != null) {
                            output.writeObject(encryptedMessage);
                            System.out.println("Message sent: " + sendText);
                        }

                    } else {
                        System.err.println("Socket is not connected.");
                    }

                } catch (IOException ex) {
                    System.err.println("Cannot find public key file, please check if the recipient exists. ");
                } catch (Exception ex) {
                    System.err.println("Recipient name error");
                }

                // clean the text field.
                subjectField.setText("");
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                Object receivedMessage;
                while (true) {
                    try {
                        try {
                            receivedMessage = input.readObject();
                        } catch (EOFException e) {
                            break; // Exit the loop when no more messages are available
                        }

                        if (receivedMessage instanceof String) {
                            System.out.printf("String received.");
                            textArea.append("Email received at " + new Date() + "\n");
                            textArea.append(receivedMessage + "\n");
                            textArea.append("---------------------\n");

                        } else if (receivedMessage instanceof Message) {
                            System.out.println("Email Message received.");
                            System.out.println("Now do decryption. ");

                            Message message = (Message) receivedMessage;

                            String sender = message.getSender();
                            String envelope = message.getFormattedMessage();
                            EncryptedMessage encryptedMessage = message.getEncryptedMessage();

                            textArea.append("Email received at " + new Date() + "\n");

                            // decryption
                            receiverPGP.setMyPrivateKey(myPrivateKey);
                            receiverPGP.setMyPublicKey(myPublicKey);
                            receiverPGP.setTheOtherPublicKey(getTheOtherPublicKey(sender));

                            String decryptedMessage = receiverPGP.decrypt(encryptedMessage);

                            if (decryptedMessage != null) {
                                System.out.println("Decrypted Message: " + decryptedMessage);
                                textArea.append(envelope);
                                textArea.append("---Decrypted Content---\n\n");
                                textArea.append(decryptedMessage + "\n");
                                textArea.append("---------------------\n");
                            } else {
                                System.out.println("Decryption failed");
                            }
                        }else{
                            System.out.printf("Invalid message received.");
                            textArea.append("Invalid message received.\n");
                            textArea.append("---------------------\n");
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        System.err.println("Sender's public key not found");
                    }
                }
            }
        }).start();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
