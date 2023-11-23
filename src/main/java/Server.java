import common.TrustLevel;

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

    // used to send messages to the clients
    private Map<String, PrintWriter> clients = new HashMap<>();
    // PGP trust model implementation
    private Map<String, Map<String, TrustLevel>> trustNetwork;

    public Server() {
        trustNetwork = new HashMap<>();
    }

    public void addTrust(String from, String to, TrustLevel level){
        trustNetwork.putIfAbsent(from, new HashMap<>());
        trustNetwork.get(from).put(to, level);
    }

    public TrustLevel getTrustLevel(String from, String to) {
        if(trustNetwork.containsKey(from)){
            Map<String, TrustLevel> tempMap = trustNetwork.get(from);
            if(tempMap.containsKey(to)){
                return tempMap.get(to);
            }
        }
        return TrustLevel.NONE;
    }

    public Set<String> getTrustedBy(String user, TrustLevel level) {
        Set<String> result = new HashSet<>();
        for (String other : trustNetwork.keySet()) {
            TrustLevel trustLevel = trustNetwork.get(other).getOrDefault(user, TrustLevel.NONE);
            if (trustLevel == level) {
                result.add(other);
            }
        }
        return result;
    }

    public void start(){
        // thread pool
        ExecutorService pool = Executors.newFixedThreadPool(30);
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                try {
                    Socket connection = server.accept();
                    Callable<Void> task = new ClientHandler(connection);
                    // submit the task to the thread pool for execution.
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

    private class ClientHandler implements Callable<Void> {

        private Socket connection;

        ClientHandler(Socket connection) {
            this.connection = connection;
        }

        @Override
        public Void call() throws Exception {
            PrintWriter out = null;
            try {

                // input stream and output streams for communication with clients.
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                out = new PrintWriter(connection.getOutputStream(), true);

                // once establish one connection, send a welcome message.
                String name = in.readLine();
                clients.put(name, out);
                out.println("Welcome " + name);

                // forward message
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

                // the client's PrintWriter is remove once connection is closed.
                if (out != null) {
                    clients.values().remove(out);
                }
            }
            return null;
        }
    }

    public static void main(String[] args) {

        Server server = new Server();

        // Alice fully trusts Bob
        server.addTrust("Alice", "Bob", TrustLevel.FULL);
        // Alice partially trusts Carmen and Jane
        server.addTrust("Alice", "Carmen", TrustLevel.PARTIAL);
        server.addTrust("Alice", "Jane", TrustLevel.PARTIAL);
        // Alice doesn't trust John
        server.addTrust("Alice", "John", TrustLevel.NONE);


        server.start();
    }
}


