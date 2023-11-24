import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.util.Date;

/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 19/11/2023
 * @Description:
 */

public class ClientB {
    private JTextArea textArea;
    private JTextField textField;
    private JTextField receiverNameField;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private String clientName = "Bob"; // replace with your client's name

    public ClientB() {
        JFrame frame = new JFrame("Email Application");
        textArea = new JTextArea(20, 50);
        textArea.setEditable(false);
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
                    output.writeObject(clientName);
                    output.writeObject(receiverNameField.getText());
                    output.writeObject(textField.getText());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                textField.setText("");
            }
        });
        panel.add(sendButton);

        frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.pack();
        Socket socket;

        try {

            socket = new Socket("localhost", 7000);

            output = new ObjectOutputStream(socket.getOutputStream());

            input = new ObjectInputStream(socket.getInputStream());

            output.writeObject(clientName);
        } catch (IOException e) {
            e.printStackTrace();
        }


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String receivedMessage;
                        while ((receivedMessage = (String) input.readObject()) != null) {
                            textArea.append("Email received at " + new Date() + "\n");
                            textArea.append(receivedMessage + "\n");
                            textArea.append("---------------------\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

    }

    public static void main(String[] args) {
        new ClientB();
    }
}