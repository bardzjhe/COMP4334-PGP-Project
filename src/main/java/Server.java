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
 * @Description: Multithreaded email server
 *
 * Implementation introduction:
 * 1. We used a fixed thread pool to limit the potential
 * resource usage.
 * 2. The server forwards the encrypted message.
 * 3. The server implements the trust model.
 *
 * When server forward message, it forwards three things:
 * 1. Encrypted message.
 * 2. Trust level after calculation.
 * 3. Inform the recipient of other information, including sender name.
 */
public class Server {

    final static int PORT = 7000;

    // used to send messages to the clients
    private Map<String, ObjectOutputStream> clients = new HashMap<>();

    private Map<String, Map<String, TrustLevel>> trustNetwork;

    public Server() {
        trustNetwork = new HashMap<>();
    }

    public void addTrust(String from, String to, TrustLevel level){
        trustNetwork.putIfAbsent(from, new HashMap<>());
        trustNetwork.get(from).put(to, level);
    }

    /**
     * Note that the trust relationship is in one way, not bidirectional.
     * Say if A fully trusts B, it doesn't necessarily mean B fully trusts A.
     *
     * Here we illustrate our implementation of trust model.
     * Case 1:
     *      If from has direct knowledge of to, then it will return the TrustLevel directly.
     *
     * Case 2:
     *      If from has no directly knowledge of to, then it will try to check if its
     *      fully trusts parties have directly knowledge of to.
     *          If yes, then it will compute and return the Trust level.
     *          If no, then it will return NONE trust.
     * Reference: Course slides.
     * @param from
     * @param to
     * @return the level that from trusts to.
     */
    public TrustLevel getTrustLevel(String from, String to) {

        if(trustNetwork.containsKey(from)){
            Map<String, TrustLevel> tempMap = trustNetwork.get(from);
            // if from directly fully trusts to, then return directly.

            // Case 1
            if(tempMap.containsKey(to)){
                return tempMap.get(to);

                // Case
            }else{

                double sum = 0;
                Map<String, TrustLevel> innerMap = trustNetwork.get(from);

                for (Map.Entry<String, TrustLevel> entry : innerMap.entrySet()) {
                    if(entry.getValue() != TrustLevel.NONE){
                        Set<String> fullyTrustedParties = getFullyTrustedParties(entry.getKey());
                        if(fullyTrustedParties.contains(to) && entry.getValue()==TrustLevel.FULL){
                            sum = 1;
                        }
                        else if(fullyTrustedParties.contains(to) && entry.getValue()==TrustLevel.PARTIAL) {
                            sum += 0.5;
                        }
                        if(sum == 1){
                            break;
                        }
                    }
                }

                if(sum == 1){
                    return TrustLevel.FULL;
                }
                if(sum == 0.5){
                    return TrustLevel.PARTIAL;
                }
            }
        }


        return TrustLevel.NONE;
    }

    /**
     * In our design, only fully trusted party can certificate the public key.
     * @param from
     * @return a set of fully trusted parties of from
     */
    public Set<String> getFullyTrustedParties(String from) {
        Set<String> fullyTrustedParties = new HashSet<>();
        if (trustNetwork.containsKey(from)) {
            for (Map.Entry<String, TrustLevel> entry : trustNetwork.get(from).entrySet()) {
                if (entry.getValue() == TrustLevel.FULL) {
                    fullyTrustedParties.add(entry.getKey());
                    fullyTrustedParties.addAll(getFullyTrustedParties(entry.getKey()));
                }
            }
        }
        return fullyTrustedParties;
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

        server.addTrust("Bob", "Cali", TrustLevel.FULL);
        server.addTrust("Carmen", "David", TrustLevel.FULL);
        server.addTrust("Jane", "David", TrustLevel.FULL);
        server.addTrust("Jane", "Eve", TrustLevel.FULL);


        System.out.println("Server starts running...");
        server.start();
    }
}