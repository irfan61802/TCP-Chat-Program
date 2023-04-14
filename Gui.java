import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Gui extends JFrame {
    private JTextArea chatTextArea;
    private JTextField messageTextField;
    private JButton sendButton;
    private JList<String> memberList;
    private DefaultListModel<String> memberListModel;
    
    public Gui() {
        super("Chat Application");
        setLayout(new BorderLayout());

        // create the chat text area
        chatTextArea = new JTextArea();
        chatTextArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatTextArea);
        chatScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // add padding

        // create the member list
        memberListModel = new DefaultListModel<>();
        memberList = new JList<>(memberListModel);
        JScrollPane memberScrollPane = new JScrollPane(memberList);
        memberScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // add padding

        // create the message input area
        JPanel messagePanel = new JPanel(new BorderLayout());
        messageTextField = new JTextField();
        messagePanel.add(messageTextField, BorderLayout.CENTER);
        sendButton = new JButton("Send");
        messagePanel.add(sendButton, BorderLayout.EAST);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // add padding

        // add the components to the split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, chatScrollPane, memberScrollPane);
        splitPane.setResizeWeight(0.75);

        // calculate the pixel value for the divider location
        
        int dividerLocation = (int)(550);
        splitPane.setDividerLocation(dividerLocation);

        // add the components to the frame
        add(splitPane, BorderLayout.CENTER);
        add(messagePanel, BorderLayout.SOUTH);

        // set the window properties
        setSize(800, 600); // set the size of the window
        setLocationRelativeTo(null); // center the window on the screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // exit the program when the window is closed
        setVisible(true); // show the window
    }

    public void addMessage(String message) {
        chatTextArea.append(message + "\n"); // add the message to the chat area
    }

    public void setMembers(ArrayList<String> members) {
        memberListModel.clear(); // clear the member list
        for (String member : members) {
            memberListModel.addElement(member); // add each member to the list
        }
    }
    public JButton getSendButton(){
        return sendButton;
    }

    public String getMessage() {
        return messageTextField.getText(); // get the message text from the input field
    }

    
}
