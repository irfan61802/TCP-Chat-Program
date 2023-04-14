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

        return clientSocket;
    }

    //Define what to do with clients
    static class ServerListener implements Runnable {
        private Gui gui;
        public ServerListener(Gui gui) {
            this.gui = gui;
        }
        public void run() {
            
            String hostname = "localhost";
            int portNum = 8080;
            Socket socket = connect(hostname, portNum);
            if (socket != null) {
                try {
                    outToServer = new DataOutputStream(socket.getOutputStream());
                    inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    // Your code to communicate with the server goes here
                    JButton sendButton = gui.getSendButton();
                    sendButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String username = gui.getMessage();
                            try {
                                outToServer.writeBytes(username + "\n");
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                // Handle the exception as needed, e.g. show an error message to the user
                            }
                            gui.clearMessage();
                            System.out.println("Send button clicked!");
                        }
                    });
                    // Prompt the user for input
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Enter a message to send to the server: ");
                    String userInput = scanner.nextLine();
                    outToServer.writeBytes(userInput + "\n");
                    // Loop to continuously read input from the server
                    String message;
                    try {
                        while ((message = inFromServer.readLine()) != null) {
                            gui.addMessage(message);
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
