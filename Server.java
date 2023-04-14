import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    //Hash table to store connected users
    private static Hashtable<String,Socket> connectedUsers = new Hashtable<String,Socket>();
    public static ServerSocket welcomeSocket;
    public static void main(String argv[]) throws Exception {
        //Start server
        welcomeSocket = new ServerSocket(8080);
        System.out.println(InetAddress.getLocalHost());

        // Create a thread pool with a fixed number of threads
        int maxThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
        while (true) {
            try {
                Socket clientSocket = welcomeSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                executorService.submit(new ClientHandler(clientSocket));
            } catch (IOException e) {
                // Handle error when accepting client connections
                e.printStackTrace();
            }
        }
    }

    //Define what to do with clients
    static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            while (true) {
                try {
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
    
                    String username = inFromClient.readLine();
                    System.out.println(username);
                    if (username != null && !username.isEmpty()) {
                        synchronized (connectedUsers) {
                            if (connectedUsers.containsKey(username)) {
                                // Handle duplicate username, e.g., send an error message to the client
                                String errorMsg = "Error: Duplicate Username";
                                outToClient.writeBytes(errorMsg + "\n");
                            } else {
                                // Add the client to the map with username as key and socket as value
                                connectedUsers.put(username, clientSocket);
                                System.out.println("User has connected: " + username);
                                outToClient.writeBytes("Successfully connected.");
                            }
                        }
                    }
                    
                } catch (IOException e) {
                    // Handle error when accepting client connections
                    e.printStackTrace();
                }
            }
        }
    }

    static class MessageBroadcaster implements Runnable {
        
        public void run(){

        }
    }
}