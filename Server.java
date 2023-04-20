import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Date;
import java.text.SimpleDateFormat;

public class Server {

    //Hash table to store connected users
    private static Hashtable<String,String> connectedUsers = new Hashtable<String,String>();
    private static ArrayList<String> usernames = new ArrayList<String>();
    public static ServerSocket welcomeSocket;
    public static ServerSocket welcomeSocket2;
    private static ArrayList<String> messageQueue = new ArrayList<String>();
    private static long lastConnectTime = 0;
    public static void main(String argv[]) throws Exception {

        // Create a thread pool with a fixed number of threads
        int maxThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(maxThreads);
        
        Thread welcomeSocketThread = new Thread(() -> {
            try {
                ServerSocket welcomeSocket = new ServerSocket(5000);
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
    
        // Listen for clients that are connecting to send message
        Thread welcomeSocket2Thread = new Thread(() -> {
            try {
                ServerSocket welcomeSocket2 = new ServerSocket(5001);
                System.out.println("Server listening on port 5001");
                while (true) {
                    Socket clientSocket2 = welcomeSocket2.accept();
                    // Handle client connection on a separate thread
                    executorService.execute(new MessageHandler(clientSocket2));
                }
            } catch (IOException e) {
                // Handle error when accepting client connections on port 8050
                e.printStackTrace();
            }
        });
        welcomeSocket2Thread.start();
        
        //Listen on port 5002 for polling connections
        Thread queueProcessor = new Thread(() -> {
            try {
                ServerSocket welcomeSocket3 = new ServerSocket(5002);
                while (true) {
                    Socket clientSocket3 = welcomeSocket3.accept();
                    
                    executorService.execute(new MessageBroadcaster(clientSocket3));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        queueProcessor.start();
    }

    //Print entire message queue (For testing)
    private static void printMessageQueue() {
        System.out.println("Printing Message Queue:");
        synchronized (messageQueue) {
            for (String message : messageQueue) {
                System.out.println(message);
            }
        }
    }

    //Format date value to MM/dd hh:mm
    private static String dateFormat(String time){
        long timestamp = Long.parseLong(time);
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd hh:mm a");
        String formattedDate = formatter.format(date);
        return formattedDate;
    }

    //Handle client connections if it is their first time connecting
    static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private boolean running = true;

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
                                // Add the client to the map with sessionID as key and username as value
                                String sessionID = generateSessionId();
                                connectedUsers.put(sessionID, username);
                                usernames.add(username);
                                lastConnectTime = System.currentTimeMillis();
                                System.out.println("User has connected: " + username);
                                outToClient.writeBytes("Successfully connected. Session id:" + sessionID + Integer.toString(messageQueue.size()) + "\n");
                            }
                        }
                    }
                    clientSocket.close();
                    inFromClient.close();
                    outToClient.close();
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
            return java.util.UUID.randomUUID().toString();
        }
    }

    //Handle messages when message is received(Port 5001)
    static class MessageHandler implements Runnable {
        private Socket clientSocket;
        private boolean running = true; // boolean flag to control the loop
    
        public MessageHandler(Socket socket) {
            this.clientSocket = socket;
        }
    
        public void run() {
            try {
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
                String message;
                String msg;
                String uuid;
                while (running) {
                    // Add the received message to the messageQueue
                    message = inFromClient.readLine();
                    msg = message.substring(49, message.length());
                    uuid = message.substring(0, 36);
                    //Handle user disconnect
                    if (msg.equals(".")){
                        System.out.println("starting disconnect");
                        outToClient.writeBytes("Disconnect successful" + "\n");
                        //Remove username and Session ID
                        usernames.remove(connectedUsers.get(uuid));
                        System.out.println("User list:"+usernames.size());
                        connectedUsers.remove(uuid);
                    }else{
                        synchronized (messageQueue) {
                            for (int i = messageQueue.size() - 1; i >= 0; i--) {
                                if (getMessageTimestamp(message) > getMessageTimestamp(messageQueue.get(i))) {
                                    messageQueue.add(i + 1, message);
                                    break;
                                }
                            }
                            stop();
                        }
                    }
                }
                clientSocket.close();
                inFromClient.close();
                outToClient.close();
            } catch (IOException e) {
                // Handle error when reading from client
                e.printStackTrace();
            }
        }

        public void stop() {
            running = false;
        }

        private long getMessageTimestamp(String message){
            String time = message.substring(36, 49);
            long timeL = Long.parseLong(time);
            return timeL;
        }
    }

    //While queue has messages, iterate through ipaddress list and send message.
    static class MessageBroadcaster implements Runnable {
        private Socket clientSocket;
        

        public MessageBroadcaster(Socket clientSocket){
            this.clientSocket = clientSocket;
        }
        public void run(){
            try{
                String message;
                String uuid;
                String msg;
                String time;
                BufferedReader inFromClient = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream());
                //Client sends the current message that they have
                int currMessage = Integer.parseInt(inFromClient.readLine());
                //Add code to parse message: First 36 char is UUID and rest is message
                for(int i = currMessage; i < messageQueue.size(); i++){
                    message = messageQueue.get(i);
                    uuid = message.substring(0, 36);
                    time = dateFormat(message.substring(36, 49));
                    msg = message.substring(49, message.length());
                    outToClient.writeBytes(time + "   -   From " + connectedUsers.get(uuid) + ": " + msg +"\n");
                }
                outToClient.writeBytes("USERS"+String.join(",", usernames));

                inFromClient.close();
                outToClient.close();
                clientSocket.close();
            //Broadcast Message
            } catch(IOException e){
                e.printStackTrace();
            }
            
        }

    }
}