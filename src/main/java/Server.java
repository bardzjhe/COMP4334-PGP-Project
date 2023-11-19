import model.EncryptedMessage;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
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
            PrintWriter out = null;
            try {

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                out = new PrintWriter(connection.getOutputStream(), true);
                String name = in.readLine();
                clients.put(name, out);
                out.println("Welcome " + name);


                while ((name = in.readLine()) != null) {
                    String receiverName = in.readLine();
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
                if (out != null) {
                    clients.values().remove(out);
                }
            }
            return null;
        }
    }
}


