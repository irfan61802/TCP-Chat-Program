import java.io.*;
import java.net.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Client {
    private static Gui gui;
    private Socket clientSocket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private String sessionID;
    public static int currMessage = 0;

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
                            //Handle messages after connect
                            if(sessionID != null){
                                clientSocket = connect(host, 5001);
                                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                                
                                // Send UUID, Time, and message
                                outToServer.writeBytes(sessionID + Long.toString(System.currentTimeMillis()) + message + "\n");
                                //Check to exit
                                if(message.equals(".")){
                                    inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                                    String response = inFromServer.readLine();
                                    if(response.equals("Disconnect successful")){
                                        gui.addMessage(message + "\n" + "Initiating disconnect.");
                                        gui.dispose();
                                        System.exit(0);
                                    }
                                }
                                clientSocket.close();
                            //Handle initial connection username message
                            } else{
                                outToServer = new DataOutputStream(clientSocket.getOutputStream());
                                outToServer.writeBytes(message + "\n");
                            }
                            gui.clearMessage();
                        }
                    } catch (Exception ex) {
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
        int port = 5000;
        new Client(host, port);
    }

    public static ArrayList<String> parseUserList(String userListString) {
        String[] userListArray = userListString.split(",");
        ArrayList<String> userList = new ArrayList<String>(Arrays.asList(userListArray));
        return userList;
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

    //Listen for connection information (UUID and Acknowledgement)
    class ServerListener implements Runnable {
        public void run() {
                try {
                    // Loop to continuously read input from the server
                    String input;
                    while (((input = inFromServer.readLine()) != null)) {
                        System.out.println("Received from server: " + input);
                        gui.addMessage(input);
                        sessionID = input.substring(input.indexOf("Session id:") + 11, input.indexOf("Session id:") + 47);
                        currMessage = Integer.parseInt(input.substring(input.indexOf("Session id:") + 47, input.length()));
                        System.out.println(sessionID);
                        System.out.println(Integer.toString(currMessage));
                        new Thread(new serverPoll()).start();   
                    }
                } catch (IOException e) {
                    System.out.println("Connection closed: " + e.getMessage());
                } finally {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        System.out.println("Error closing socket: " + e.getMessage());
                    }
                }
        }
    }

    //Poll server every 8 seconds to watch for updates
    static class serverPoll implements Runnable {
        private Socket clientSocket;
        private DataOutputStream outToServer;
        private BufferedReader inFromServer;
    
        public void run() {
            try {
                while (true) {
                    System.out.println("Polling starting");
                    clientSocket = connect("localhost", 5002);
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());
                    inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    // Send Currmessage
                    outToServer.writeBytes(Integer.toString(currMessage) + "\n");
    
                    // Read input from server
                    String input;
                    while ((input = inFromServer.readLine()) != null) {
                        if(input.substring(0, 5).equals("USERS")){
                            gui.setMembers(parseUserList(input.substring(5, input.length())));
                        }else{
                            gui.addMessage(input);
                            currMessage += 1;
                        }
                    }
                    inFromServer.close();
                    outToServer.close();
                    // Wait for 5 seconds if no input
                    Thread.sleep(8000);
                }
    
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
