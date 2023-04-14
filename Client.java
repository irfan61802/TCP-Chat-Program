import java.io.*;
import java.net.*;
import java.awt.event.*;

public class Client {
    private Gui gui;
    private Socket clientSocket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;

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
                            outToServer.writeBytes(message + "\n");
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
            String hostname = "localhost";
            int portNum = 8080;
            Socket socket = connect(hostname, portNum);
            if (socket != null) {
                try {
                    // Loop to continuously read input from the server
                    while (true) {
                        String input = inFromServer.readLine();
                        if (input != null) {
                            System.out.println("Received from server: " + input);
                            gui.addMessage(input);
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
    }
}
