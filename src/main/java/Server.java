import com.sun.xml.internal.ws.addressing.WsaActionUtil;
import model.EncryptedMessage;
import model.Message;
import trustmodel.TrustLevel;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
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
 *
 * When server forward message, it forwards three things:
 * 1. Inform the recipient of the sender name
 * 2. Trust level after calculation.
 * 3. public key of the sender.
 * 4. Encrypted message.
 */
public class Server {

    final static int PORT = 7000;

    // used to send messages to the clients
    private Map<String, ObjectOutputStream> clients = new HashMap<>();
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
            ObjectOutputStream out = null;
            try {

                // input stream and output streams for communication with clients.
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());

                // once establish one connection, send a welcome message.
                String name = (String) in.readObject();
                System.out.println("Name:" + name);
                clients.put(name, out);
                out.writeObject("Message from the server: Welcome " + name + ", you have successfully connected to the server!");

                // forward message
                while ((name = (String) in.readObject()) != null) {
                    String receiverName = (String) in.readObject();
                    System.out.println("Receiver name: " + receiverName);
//                    String message = (String) in.readObject();

                    EncryptedMessage encryptedMessage = (EncryptedMessage) in.readObject();
//                    byte[] ciphertext = encryptedMessage.getCiphertext();
//                    EncryptedMessage encryptedMessage = (EncryptedMessage) in readObject();


                    String formattedContent = String.format("From: %s\nTrust Level: %s\n",
                            name, getTrustLevel(name, receiverName)
                    );

                    System.out.println(formattedContent);

                    Message messageToTransfer = new Message(name, formattedContent, encryptedMessage);

                    System.out.println(receiverName + "'s message is received by the server. ");
                    if (clients.containsKey(receiverName)) {
                        clients.get(receiverName).writeObject(messageToTransfer);

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

    public PublicKey getPublicKey(String filename) throws Exception {
        String key = new String(Files.readAllBytes(Paths.get(filename)));
        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll(System.lineSeparator(), "")
                .replace("-----END PUBLIC KEY-----", "");

        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
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

        System.out.println("Server starts running...");
        server.start();
    }
}