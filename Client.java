import java.io.*; 
import java.net.*; 
import java.util.Scanner;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Client {
    public static DataOutputStream outToServer;
    public static BufferedReader inFromServer;
    public static void main(String argv[]) throws Exception {
        Gui gui = new Gui();
        gui.addMessage("Please enter a username: ");
        ServerListener serverList = new ServerListener(gui);
        new Thread(serverList).start(); 
    }

    public static Socket connect(String host, int port) {
        Socket clientSocket;
        String hostname = host;
        int portNum = port;

        try {
            clientSocket = new Socket(hostname, portNum);
            System.out.println("Connected to " + hostname + " on port " + portNum);
            // Your code to communicate with the server goes here

            return clientSocket;
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            clientSocket = null;
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
                            System.out.println("Send button clicked!");
                        }
                    });
                    // Prompt the user for input
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Enter a message to send to the server: ");
                    String userInput = scanner.nextLine();
                    outToServer.writeBytes(userInput + "\n");
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
                    e.printStackTrace();
                    // Handle the exception as needed
                }       
            }
        }
    }
}
