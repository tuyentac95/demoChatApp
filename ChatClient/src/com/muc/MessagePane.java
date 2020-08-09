package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

public class MessagePane extends JPanel implements MessageListener{

    private final ChatClient client;
    private final String friend;

    private DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();

    public MessagePane(ChatClient client, String friend) throws IOException {
        this.client = client;
        this.friend = friend;

        client.addMessageListener(this);

        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField,BorderLayout.SOUTH);

        client.loadMessage(friend);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String text = inputField.getText();
                    client.msg(friend,text);
                    listModel.addElement("You: " + text);
                    inputField.setText("");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onMessage(String fromLogin, String messageBody) {
        if (friend.equalsIgnoreCase(fromLogin)) {
            String line = fromLogin + ": " + messageBody;
            listModel.addElement(line);
        } else {
            String line = "You: " + messageBody;
            listModel.addElement(line);
        }
    }
}
