import java.io.*;
import java.net.*;
/**
 * @Author Anthony HE, anthony.zj.he@outlook.com
 * @Date 19/11/2023
 * @Description:
 */


public class ClientB {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 7000);
        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

        output.println("Client B"); // replace with your client's name

//        // To send a message
//        output.println("Client B"); // replace with the receiver's name
//        output.println("Hello, this is a message."); // replace with your message

        // To receive messages
        String receivedMessage;
        while ((receivedMessage = input.readLine()) != null) {
            System.out.println(receivedMessage);
        }
    }
}