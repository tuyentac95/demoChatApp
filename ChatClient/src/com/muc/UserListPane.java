package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;

public class UserListPane extends JPanel implements UserStatusListener{
    private final ChatClient client;
    private JList<String> userListUI;
    private DefaultListModel<String> userListModel;
    private String login;
    private ArrayList<String> onChatting;

    public UserListPane(ChatClient client, String login) {
        this.client = client;
        this.client.addUserStatusListener(this);
        this.login = login;

        userListModel = new DefaultListModel<>();
        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);

        onChatting = new ArrayList<>();

        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    String friend = userListUI.getSelectedValue();

                    if (onChatting.indexOf(friend) < 0) {
                        onChatting.add(friend);
                        MessagePane messagePane = null;
                        try {
                            messagePane = new MessagePane(client,friend);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }

                        JFrame f = new JFrame("Message: " + friend);
                        f.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                        f.setSize(400,300);

                        f.addWindowListener(new WindowAdapter() {
                            @Override
                            public void windowClosing(WindowEvent e) {
                                onChatting.remove(friend);
                                f.setVisible(false);
                                f.dispose();
                            }
                        });

                        f.getContentPane().add(messagePane,BorderLayout.CENTER);
                        f.setVisible(true);

                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient("localhost",8819);

        UserListPane userListPane = new UserListPane(client,"guest");
        JFrame frame = new JFrame("User List");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400,600);

        frame.getContentPane().add(userListPane, BorderLayout.CENTER);
        frame.setVisible(true);

        if (client.connect()) {
            try {
                client.login("tuyen","pass");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void online(String login) {
        userListModel.addElement(login);
    }

    @Override
    public void offline(String login) {
        userListModel.removeElement(login);
    }
}
