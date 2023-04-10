import java.io.*;
import java.net.*;
public class Server {

    public Server() {
        
    }
    public static void main(String argv[]) throws Exception {
        //Start server
        ServerSocket welcomeSocket = new ServerSocket(8080);
        System.out.println(InetAddress.getLocalHost());
        

        //Listen for client connections
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            ClientHandler client = new ClientHandler(connectionSocket);
            new Thread(client).start();
        }
    }

    //Define what to do with clients
    static class ClientHandler implements Runnable {
        private Socket connectionSocket;

        public ClientHandler(Socket connectionSocket) {
            this.connectionSocket = connectionSocket;
        }

        public void run() {
            
        }
    }
}