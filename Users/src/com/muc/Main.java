package com.muc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class Main extends JFrame {
    private final ChatClient client;
    JTextField loginField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("Login");
    JButton signUpButton = new JButton("Sign Up");

    public Main() {
        super("Login");

        this.client = new ChatClient("localhost",8819);
        client.connect();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel loginPanel = new JPanel();
        loginPanel.setLayout(new BorderLayout());

        JPanel headPanel = new JPanel();
        headPanel.add(new JLabel("Login"));

        JPanel bodyPanel = new JPanel();
        bodyPanel.setLayout(new GridLayout(2,2));
        bodyPanel.setSize(400,100);
        bodyPanel.add(new JLabel("User: "));
        bodyPanel.add(loginField);
        bodyPanel.add(new JLabel("Password: "));
        bodyPanel.add(passwordField);

        JPanel footPanel = new JPanel();
        footPanel.add(loginButton);
        footPanel.add(signUpButton);

        loginPanel.add(headPanel,BorderLayout.NORTH);
        loginPanel.add(bodyPanel,BorderLayout.CENTER);
        loginPanel.add(footPanel,BorderLayout.SOUTH);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doLogin();
            }
        });

        signUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doSignUp(loginPanel);
            }
        });

        getContentPane().add(loginPanel);
        pack();
        setVisible(true);
    }

    private void doSignUp(JPanel p) {
        JPanel signUpPane = new JPanel();
        signUpPane.setLayout(new BorderLayout());

        JPanel headPane = new JPanel();
        JLabel headLabel = new JLabel("Sign up",JLabel.CENTER);
        headPane.add(headLabel);

        JTextField newUserLogin = new JTextField();
        JPasswordField newUserPassword = new JPasswordField();
        JTextField newUserName = new JTextField();
        JTextField newUserAge = new JTextField();

        JPanel bodyPane = new JPanel(new GridLayout(0,2));
        bodyPane.setBorder(BorderFactory.createEmptyBorder(20,15,20,15));
        bodyPane.add(new JLabel("Login: ",JLabel.RIGHT));
        bodyPane.add(newUserLogin);
        bodyPane.add(new JLabel("Password: ",JLabel.RIGHT));
        bodyPane.add(newUserPassword);
        bodyPane.add(new JLabel("Name: ",JLabel.RIGHT));
        bodyPane.add(newUserName);
        bodyPane.add(new JLabel("Age: ",JLabel.RIGHT));
        bodyPane.add(newUserAge);

        JPanel footPane = new JPanel();
        JButton button = new JButton("Sign Up");
        footPane.add(button);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newLogin = newUserLogin.getText();
                String newPassword = newUserPassword.getText();
                String newName = newUserName.getText();
                String newAge = newUserAge.getText();

                String newUserSign = newLogin + " " + newPassword + " " + newAge + " " + newName;

                try {
                    if (client.signUp(newUserSign)) {
                        JOptionPane.showMessageDialog(signUpPane,"Sign up successful. Please log in");
                        p.setVisible(true);
                        signUpPane.setVisible(false);

                    } else {
                        JOptionPane.showMessageDialog(signUpPane,"User is exist. Please change your login");
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        signUpPane.add(headPane,BorderLayout.NORTH);
        signUpPane.add(bodyPane,BorderLayout.CENTER);
        signUpPane.add(footPane,BorderLayout.SOUTH);

        getContentPane().add(signUpPane);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        signUpPane.setVisible(true);

        p.setVisible(false);
    }

    private void doLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();

        try {
            int check = client.login(login,password);
            if (check > 0) {
                UserListPane userListPane = new UserListPane(client,login);

                JFrame frame = new JFrame("User List");
                frame.setLayout(new BorderLayout());
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                frame.setSize(400,600);
                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        frame.setVisible(false);
                        frame.dispose();
//                        setVisible(true);
                    }
                });

                JPanel headPanel = new JPanel();
                headPanel.add(new JLabel("User: " + login));
                frame.getContentPane().add(headPanel,BorderLayout.NORTH);

                frame.getContentPane().add(userListPane, BorderLayout.CENTER);
                frame.setVisible(true);

                setVisible(false);
            } else if (check < 0){
                JOptionPane.showMessageDialog(this,"Invalid login/password");
            } else {
                JOptionPane.showMessageDialog(this,"User is already login");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Main main = new Main();
        main.setVisible(true);
    }
}
