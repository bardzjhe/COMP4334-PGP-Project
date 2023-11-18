import model.EncryptedMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
<<<<<<< Updated upstream
* @Author Anthony HE, anthony.zj.he@outlook.com
* @Date 15/10/2023
* @Description: Multithreaded server
*
* 1. We used a fixed thread pool to limit the potential
* resource usage. Thirty threads should be plenty in this project.
* 2. The server manages the public keys.
* 3. The server implements the trust model.
*/
public class Server {

    final static int PORT = 7000;

    private Map<String, PrintWriter> clients = new HashMap<>();
    private Map<String, Map<String, String>> trustRelationships;


    public Server() {

        trustRelationships = new HashMap<>();

    }

    public void start(){
        // thread pool
        ExecutorService pool = Executors.newFixedThreadPool(30);
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                try {
                    Socket connection = server.accept();
                    Callable<Void> task = new ClientHandler(connection);
                    pool.submit(task);
                } catch (IOException ex) {
                    System.err.println(ex);
                }
            }

        } catch (IOException ex) {
            // Exception handling for server-level issues
            System.err.println(ex);
        }
    }
    public static void main(String[] args) {

        Server server = new Server();
        server.start();
    }

    private class ClientHandler implements Callable<Void> {

        private Socket connection;

        ClientHandler(Socket connection) {
            this.connection = connection;
        }

        @Override
        public Void call() throws Exception {
            try {

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
                String name = in.readLine();
                clients.put(name, out);
                out.println("Welcome " + name);

                String receiverName;
                while ((receiverName = in.readLine()) != null) {
                    String message = in.readLine();
                    message = "From " + name + " at " + new Date() + ": " + message;
                    if (clients.containsKey(receiverName)) {
                        clients.get(receiverName).println(message);
                    }
                }


            } catch (IOException e) {
                System.err.println(e);
            } finally {
                try {
                    connection.close();
                } catch (IOException e) {
                    // ignore
                }

            }
            return null;
        }
    }
}

//import java.io.*;
//        import java.net.*;
//
//public class Client {
//    public static void main(String[] args) throws IOException {
//        Socket socket = new Socket("localhost", 7000);
//        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
//
//        output.println("ClientName"); // replace with your client's name
//
//        // To send a message
//        output.println("ReceiverName"); // replace with the receiver's name
//        output.println("Hello, this is a message."); // replace with your message
//
//        // To receive messages
//        String receivedMessage;
//        while ((receivedMessage = input.readLine()) != null) {
//            System.out.println(receivedMessage);
//        }
//    }
//}
