import java.io.*; 
import java.net.*; 

public class Client {

    public Client() {
        
    }

    public static void main(String argv[]) throws Exception {
        String hostname = "localhost";
        int portNum = 8080;
        //Socket socket = connect(hostname, portNum);
        //DataOutputStream outToServer = new DataOutputStream(socket.getOutputStream());
        //BufferedReader inFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        Gui gui = new Gui();

        

        
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

        public void run() {
            
        }
    }
}
