//import model.EncryptedMessage;
//
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.List;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
///**
// * @Author Anthony HE, anthony.zj.he@outlook.com
// * @Date 15/10/2023
// * @Description: Multithreaded server
// *
// * 1. We used a fixed thread pool to limit the potential
// * resource usage. Thirty threads should be plenty in this project.
// *
// */
//public class Server {
//
//    final static int PORT = 7000;
//    private ConcurrentHashMap<String, List<EncryptedMessage>> messageMap;
//
//    public Server(){
//        messageMap = new ConcurrentHashMap<>();
//    }
//
//    public static void main(String[] args){
//
//        // thread pool
//        ExecutorService pool = Executors.newFixedThreadPool(30);
//        try(ServerSocket server = new ServerSocket(PORT)){
//            while(true){
//                try{
//                    Socket connection = server.accept();
//                    Callable<Void> task = new MessageProcess(connection);
//                }catch (IOException ex){}
//
//            }
//
//        }catch (IOException ex){
//            // Exception handling for server-level issues
//            System.err.println(ex);
//        }
//
//    }
//
//    private static class MessageProcess implements Callable<Void>{
//
//        private Socket connection;
//
//        MessageProcess(Socket connection){
//            this.connection = connection;
//        }
//        @Override
//        public Void call() throws Exception {
//            try{
//
//                // Output stream for sending data to the client.
//                ObjectOutputStream outputStream = new ObjectOutputStream(connection.getOutputStream());
//                // Input stream for receiving data from the client.
//                ObjectInputStream inputStream = new ObjectInputStream(connection.getInputStream());
//
//
//
//            }catch (IOException e){
//                System.err.println(e);
//            }finally {
//                try{
//                    connection.close();
//                }catch (IOException e){
//                    // ignore
//                }
//
//            }
//            return null;
//        }
//    }
//}
