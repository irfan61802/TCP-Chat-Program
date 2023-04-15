import java.io.*;
import java.net.*;
import java.awt.event.*;

public class Client {
    private Gui gui;
    private Socket clientSocket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private String sessionID;

    public Client(String host, int port) {
        try {
            clientSocket = connect(host, port);
            outToServer = new DataOutputStream(clientSocket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            gui = new Gui();
            gui.addMessage("Please enter a username: ");

            // Add a listener for the Send button
            gui.addSendButtonListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        String message = gui.getMessage();
                        if (!message.isEmpty()) {
                            if(sessionID != null){
                                clientSocket = connect(host, 8050);
                                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                                outToServer.writeBytes(sessionID + message + "\n");
                                clientSocket.close();
                            } else{
                                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                                outToServer.writeBytes(message + "\n");
                            }
                            gui.clearMessage();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            // Start the server listener
            new Thread(new ServerListener()).start();

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        
    }

    public static void main(String argv[]) throws Exception {
        String host = "localhost";
        int port = 8080;
        new Client(host, port);
        //new Thread(new ServerSocketListener()).start();
    }

    public static Socket connect(String host, int port) {
        try {
            Socket clientSocket = new Socket(host, port);
            System.out.println("Connected to " + host + " on port " + port);
            return clientSocket;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return null;
        }
    }

    // Define what to do with clients
    class ServerListener implements Runnable {
        public void run() {
                try {
                    // Loop to continuously read input from the server
                    String input;
                    while (true) {
                        if(((input = inFromServer.readLine()) != null)){
                            System.out.println("Received from server: " + input);
                            gui.addMessage(input);
                            sessionID = input.substring(input.indexOf("Session id:") + 11, input.length());
                            System.out.println(sessionID);
                            // Process the received input as needed
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed: " + e.getMessage());
                } finally {
                    try {
                        clientSocket.close();
                        System.exit(0);
                    } catch (IOException e) {
                        System.out.println("Error closing socket: " + e.getMessage());
                    }
                }
        }
    }

    // Define what to do with incoming connections on the server socket
    static class ServerSocketListener implements Runnable {
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(8001);
                System.out.println("Server socket listening on port 8001...");
                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("Accepted connection from " + socket.getInetAddress().getHostAddress());
                    BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String input;
                    while ((input = inFromClient.readLine()) != null) {
                        System.out.println("Received from client: " + input);
                        // Process the received input as needed
                    }
                    //socket.close();
                }
            } catch (IOException e) {
                System.out.println("Error in server socket listener: " + e.getMessage());
            }
        }
    }
}
