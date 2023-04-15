import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    //Hash table to store connected users
    private static Hashtable<String,String> connectedUsers = new Hashtable<String,String>();
    private static ArrayList<String> ipAddr = new ArrayList<String>();
    public static ServerSocket welcomeSocket;
    public static ServerSocket welcomeSocket2;
    private static ArrayList<String> messageQueue = new ArrayList<String>();
    public static void main(String argv[]) throws Exception {

        // Create a thread pool with a fixed number of threads
        int maxThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
        
        Thread welcomeSocketThread = new Thread(() -> {
            try {
                ServerSocket welcomeSocket = new ServerSocket(8080);
                System.out.println(InetAddress.getLocalHost());
                while (true) {
                    Socket clientSocket = welcomeSocket.accept();
                    System.out.println("Client connected: " + clientSocket.getInetAddress() + "Port" + clientSocket.getPort());
                    // Handle client connection on a separate thread
                    executorService.execute(new ClientHandler(clientSocket));
                }
            } catch (IOException e) {
                // Handle error when accepting client connections on port 8080
                e.printStackTrace();
            }
        });
        welcomeSocketThread.start();
    
        // Start server on port 8050 in a separate thread
        Thread welcomeSocket2Thread = new Thread(() -> {
            try {
                ServerSocket welcomeSocket2 = new ServerSocket(8050);
                System.out.println("Server listening on port 8050");
                while (true) {
                    Socket clientSocket2 = welcomeSocket2.accept();
                    System.out.println("Message Handler starting");
                    // Handle client connection on a separate thread
                    executorService.execute(new MessageHandler(clientSocket2));
                }
            } catch (IOException e) {
                // Handle error when accepting client connections on port 8050
                e.printStackTrace();
            }
        });
        welcomeSocket2Thread.start();
        
    }

    private static void printMessageQueue() {
        System.out.println("Printing Message Queue:");
        synchronized (messageQueue) {
            for (String message : messageQueue) {
                System.out.println(message);
            }
        }
    }

    //Handle client connections if it is their first time connecting (Port 8080)
    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private boolean running = true; // boolean flag to control the loop

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            while (running) {
                try {
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
    
                    String username = inFromClient.readLine();
                    System.out.println(username);
                    if (username != null && !username.isEmpty()) {
                        synchronized (connectedUsers) {
                            if (connectedUsers.containsValue(username)) {
                                // Handle duplicate username, e.g., send an error message to the client
                                String errorMsg = "Error: Duplicate Username";
                                outToClient.writeBytes(errorMsg + "\n");
                            } else {
                                // Add the client to the map with username as key and socket as value
                                connectedUsers.put(clientSocket.getInetAddress().toString(), username);
                                ipAddr.add("localhost");
                                System.out.println("User has connected: " + username);
                                outToClient.writeBytes("Successfully connected. Session id:" + generateSessionId() + "\n");
                            }
                        }
                    }
                    clientSocket.close();
                    
                } catch (IOException e) {
                    // Handle error when accepting client connections
                    e.printStackTrace();
                }
                stop();
            }
        }
        public void stop() {
            running = false;
        }

        // Generates a unique session ID for the client
        private String generateSessionId() {
            // Implement your session ID generation logic here
            // Example: return a random UUID as session ID
            return java.util.UUID.randomUUID().toString();
        }
    }

    //Handle messages when user sends (Port 8050)
    static class MessageHandler implements Runnable {
        private Socket clientSocket;
        private boolean running = true; // boolean flag to control the loop
    
        public MessageHandler(Socket socket) {
            this.clientSocket = socket;
        }
    
        public void run() {
            try {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String message = null;
                while (running) {
                    // Add the received message to the messageQueue
                    message = inFromClient.readLine();
                    synchronized (messageQueue) {
                        messageQueue.add(message);
                        stop();
                    }
                }
                System.out.println("Received message.");
                printMessageQueue();
                clientSocket.close();
            } catch (IOException e) {
                // Handle error when reading from client
                e.printStackTrace();
            }
        }

        public void stop() {
            running = false;
        }
    }

    //While queue has messages, iterate through ipaddress list and send message.
    static class MessageBroadcaster implements Runnable {
        String message;

        public MessageBroadcaster(String message){
            this.message = message;
        }
        public void run(){
            //Add code to parse message: First 36 char is UUID and rest is message
            System.out.println("MessageBroadcasterStarting");
            //Broadcast Message
            for (String value : ipAddr) {
                // Do something with the value
                try {
                    Socket clientSocket = new Socket(value, 8001);
                    DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    outToServer.writeBytes("MessageBroadcaster" + "\n");
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}