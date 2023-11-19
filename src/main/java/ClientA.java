import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 19/11/2023
 * @Description:
 */



public class ClientA {
    private JTextArea textArea;
    private JTextField textField;
    private JTextField receiverNameField;
    private BufferedReader input;
    private PrintWriter output;
    private String clientName = "Client A"; // replace with your client's name

    public ClientA() {
        JFrame frame = new JFrame("Chat Application");
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
                output.println(clientName);
                output.println(receiverNameField.getText());
                output.println(textField.getText());
                textField.setText("");
            }
        });
        panel.add(sendButton);

        frame.getContentPane().add(new JScrollPane(textArea), BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.pack();

        try {
            Socket socket = new Socket("localhost", 7000);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println(clientName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        String receivedMessage;
                        while ((receivedMessage = input.readLine()) != null) {
                            textArea.append(receivedMessage + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        new ClientA();
    }
}