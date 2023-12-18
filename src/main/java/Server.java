import model.EncryptedMessage;
import model.Message;
import trustmodel.TrustLevel;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

/**
 * @Author Anthony HE AND Melvin WANG
 * @Date 15/10/2023
 * @Description: Multithreaded email server
 *
 * Implementation introduction:
 * 1. We used a fixed thread pool to limit the potential
 * resource usage.
 * 2. The server forwards the encrypted message.
 * 3. The server implements the trust model.
 *
 * When the server forwards message, it forwards three items:
 * 1. Encrypted message, which the server is not allowed to decrypt,
 * to prevent insider threat. --> Confidentiality
 * 2. Trust level after calculation (To what extent should the recipient
 * trust the sender). --> Trust Model
 * 3. Inform the recipient of other information, including actual
 * sender name verified by to server.  --> Help with Authentication.
 */
public class Server {

    final static int PORT = 7000;

    // used to send messages to the clients
    private Map<String, ObjectOutputStream> clients = new HashMap<>();

    private Map<String, Map<String, TrustLevel>> trustNetwork;
    private final static Logger LOGGER = Logger.getLogger(Server.class.getName());

    public Server() {
        trustNetwork = new HashMap<>();
    }

    static {
        try {
            FileHandler fileHandler = new FileHandler("server.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "File logger not working.", e);
        }
    }

    public void addTrust(String from, String to, TrustLevel level){
        trustNetwork.putIfAbsent(from, new HashMap<>());
        trustNetwork.get(from).put(to, level);
    }

    /**
     * Note that the trust relationship is in one way, not bidirectional.
     * For example: if A fully trusts B, it doesn't necessarily mean B fully trusts A.
     *
     * Here we illustrate our implementation of trust model.
     * Case 1:
     *      If to has direct knowledge of from, then it will return the TrustLevel directly.
     *
     * Case 2:
     *      If to has no directly knowledge of from, then it will try to check whether any of its
     *      fully/partially trusted parties fully trust from.
     *          If yes, then it will compute and return the Trust level.
     *          If no, then it will return NONE trust.
     *
     * Reference: Course slides.
     * @param from
     * @param to
     * @return the level that to should trust from.
     */
    public TrustLevel getTrustLevel(String to, String from) {

        if(trustNetwork.containsKey(to)){
            Map<String, TrustLevel> tempMap = trustNetwork.get(to);
            // if from directly fully trusts to, then return directly.

            // Case 1
            if(tempMap.containsKey(from)){
                return tempMap.get(from);

                // Case
            }else{

                double sum = 0;
                Map<String, TrustLevel> innerMap = trustNetwork.get(to);

                for (Map.Entry<String, TrustLevel> entry : innerMap.entrySet()) {
                    if(entry.getValue() != TrustLevel.NONE){
                        Set<String> fullyTrustedParties = getFullyTrustedParties(entry.getKey());
                        if(fullyTrustedParties.contains(from) && entry.getValue()==TrustLevel.FULL){
                            sum = 1;
                        }
                        else if(fullyTrustedParties.contains(from) && entry.getValue()==TrustLevel.PARTIAL) {
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
        LOGGER.info("Server is starting.");
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
                    LOGGER.log(Level.SEVERE, "Error accepting connection", ex);
                }
            }

        } catch (IOException ex) {
            // Exception handling for server-level issues
            System.err.println(ex);
            LOGGER.log(Level.SEVERE, "Could not listen on port: " + PORT, ex);
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
            String name = "";
            try {

                // input stream and output streams for communication with clients.
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                out = new ObjectOutputStream(connection.getOutputStream());

                // once establish one connection, send a welcome message.
                name = (String) in.readObject();

                // log connection activity
                LOGGER.info("Established connection: User '" + name + "' has connected to the server.");

                // send a welcome message
                clients.put(name, out);
                out.writeObject("Message from the server: Welcome " + name + ", you have successfully connected to the server!");

                // forward message
                while ((name = (String) in.readObject()) != null) {
                    String receiverName = (String) in.readObject();

                    // test code
//                    System.out.println("Receiver name: " + receiverName);
//                    String message = (String) in.readObject();

                    EncryptedMessage encryptedMessage = (EncryptedMessage) in.readObject();
//                    byte[] ciphertext = encryptedMessage.getCiphertext();
//                    EncryptedMessage encryptedMessage = (EncryptedMessage) in readObject();

                    String formattedContent = String.format("From: %s\nTrust Level: %s\n",
                            name, getTrustLevel(receiverName, name)
                    );

                    // 1. Log sender activity and trust level to identify
                    // potential spam for accountability
                    // 2. Only log sender info, not recipient info, to maintain confidentiality.
                    LOGGER.info("Sender: " + name + " | Trust Level by Recipient" +
                             ": " + getTrustLevel(receiverName, name));

                    // test code
                    // System.out.println(formattedContent);

                    Message messageToTransfer = new Message(name, formattedContent, encryptedMessage);

                    // test code
                    // System.out.println(receiverName + "'s message is received by the server. ");
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
                    LOGGER.info("Disconnected: User '" + name + "' has disconnected from the server.");
                    clients.values().remove(out);
                }
            }
            return null;
        }
    }

//    public PublicKey getPublicKey(String filename) throws Exception {
//        String key = new String(Files.readAllBytes(Paths.get(filename)));
//        String publicKeyPEM = key
//                .replace("-----BEGIN PUBLIC KEY-----", "")
//                .replaceAll(System.lineSeparator(), "")
//                .replace("-----END PUBLIC KEY-----", "");
//
//        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);
//
//        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
//        PublicKey publicKey = keyFactory.generatePublic(keySpec);
//        return publicKey;
//    }

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