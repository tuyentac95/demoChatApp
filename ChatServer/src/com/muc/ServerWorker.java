package com.muc;

import org.apache.commons.lang3.StringUtils;
import ch.qos.logback.classic.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class ServerWorker extends Thread{
    private final Socket clientSocket;
    private final Server server;
    private String login = null;
    private OutputStream outputStream;
    private HashSet<String> topicSet = new HashSet<>();
    private static final String PATH = "Database\\src\\usersProfile.txt";

    private UserManagement userManagement = new UserManagement(PATH);
    private ArrayList<User> users;

    public ServerWorker(Server server, Socket clientSocket) throws IOException, ClassNotFoundException {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            handleClientSocket();
        } catch (IOException | InterruptedException e) {
            if (e.getMessage().equalsIgnoreCase("Connection reset")) {
                System.out.println("Client disconnected.. Waiting for another connection");
                if (login != null) {
                    try {
                        handleLogoff();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            } else {
                e.printStackTrace();
            }
        }
    }

    private void handleClientSocket() throws IOException, InterruptedException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = bReader.readLine()) != null) {
            String[] tokens = StringUtils.split(line);
            if (tokens != null && tokens.length > 0) {
                String cmd = tokens[0];
                if (cmd.equals("logoff") || cmd.equalsIgnoreCase("quit")) {
                    handleLogoff();
                    break;
                } else if (cmd.equalsIgnoreCase("login")) {
                    handleLogin(outputStream, tokens);
                } else if (cmd.equalsIgnoreCase("sign")) {
                    String[] tokensMsg = line.split(" ",5);
                    handleSignUp(tokensMsg);
                } else if (tokens.length >= 3 && cmd.equalsIgnoreCase("msg")) {
                    String[] tokensMsg = line.split(" ",3);
                    handleMessage(tokensMsg);
                } else if (cmd.equalsIgnoreCase("join")) {
                    handleJoin(tokens);
                } else if (cmd.equalsIgnoreCase("leave")) {
                    handleLeave(tokens);
                } else {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    private void handleSignUp(String[] tokens) throws IOException {
        if (tokens.length == 5) {
            String newLogin = tokens[1];
            if (!isLoginExist(newLogin)) {
                String msg;
                msg = "ok sign up\n";
                outputStream.write(msg.getBytes());
                System.out.println("Sign up successful for " + newLogin);

                String newPassword = tokens[2];
                String newName = tokens[4];
                String newAge = tokens[3];

                User newUser = new User(newLogin,newPassword);
                newUser.setName(newName);
                newUser.setAge(Integer.parseInt(newAge));

                try {
                    users = userManagement.readFile();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                users.add(newUser);
                userManagement.setUsers(users);
                userManagement.writeFile();

            } else {
                String msg;
                msg = "user is exist\n";
                outputStream.write(msg.getBytes());
                System.err.println("Sign up fail for " + newLogin);
            }
        }
    }

    private void handleLeave(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.remove(topic);
        }
    }

    public boolean isMemberOfTopic(String topic) {
        return topicSet.contains(topic);
    }

    private void handleJoin(String[] tokens) {
        if (tokens.length > 1) {
            String topic = tokens[1];
            topicSet.add(topic);
        }
    }

    // format: "msg" "login" body...
    // format: "msg" "#topic" body...
    private void handleMessage(String[] tokens) throws IOException {
        String sendTo = tokens[1];
        String body = tokens[2];

        boolean isTopic = sendTo.charAt(0) == '#';

        List<ServerWorker> workerList = server.getWorkerList();
        for (ServerWorker worker : workerList) {
            if (isTopic) {
                if (this.isMemberOfTopic(sendTo)) {
                    if (worker.isMemberOfTopic(sendTo)) {
                        String outMsg = "msg " + login + " to " + sendTo + ": " + body + "\n";
                        worker.send(outMsg);
                    }
                } else {
                    String msg;
                    msg = "You are not in " + sendTo + " group\n";
                    outputStream.write(msg.getBytes());
                }
            } else {
                if (worker.getLogin().equalsIgnoreCase(sendTo)) {
                    String outMsg = "msg " + login + " " + body + "\n";
                    saveMessages(outMsg,login,sendTo);
                    worker.send(outMsg);
                }
            }
        }
    }

    private void saveMessages(String newMessage, String user1, String user2) throws IOException {
        String first, last;
        if (user1.compareTo(user2) > 0) {
            first = user1;
            last = user2;
        } else {
            first = user2;
            last = user1;
        }
        String path = "E:\\Codegym\\DemoChatApp\\Database\\src\\" + first + last + ".txt";
        File file = new File(path);

        BufferedWriter brWrite = new BufferedWriter(new FileWriter(file,true));
        brWrite.write(newMessage);
        brWrite.close();
    }

    private void handleLogoff() throws IOException {
        System.out.println("User is logged off: " + login);
        server.removeWorker(this);
        List<ServerWorker> workerList = server.getWorkerList();

        // send other online users current user's status
        String onlineMsg = "offline " + login + "\n";
        for (ServerWorker worker : workerList) {
            if (!login.equals(worker.getLogin()))
                worker.send(onlineMsg);
        }
        clientSocket.close();
    }

    public String getLogin() {
        return login;
    }

    private void handleLogin(OutputStream outputStream, String[] tokens) throws IOException {
        if (tokens.length == 3) {
            String login = tokens[1];
            String password = tokens[2];

            if (checkLogin(login,password)) {
                List<ServerWorker> workerList = server.getWorkerList();

                for (ServerWorker worker : workerList) {
                    if (worker.getLogin() != null && worker.getLogin().equals(login)) {
                        String msg;
                        msg = "already login\n";
                        outputStream.write(msg.getBytes());
                        return;
                    }
                }

                String msg;
                msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.login = login;
                System.out.println("User logged in successfully: " + login);



                // send current user all other online logins
                for (ServerWorker worker : workerList) {
                    if (worker.getLogin() != null) {
                        if (!login.equals(worker.getLogin())) {
                            String msg2 = "online " + worker.getLogin() + "\n";
                            send(msg2);
                        }
                    }
                }

                // send other online users current user's status
                String onlineMsg = "online " + login + "\n";
                for (ServerWorker worker : workerList) {
                    if (!login.equals(worker.getLogin()))
                    worker.send(onlineMsg);
                }
            } else {
                String msg;
                msg = "error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Login failed for " + login);
            }
        }
    }

    private void send(String onlineMsg) throws IOException {
        if (login != null) {
            try {
                outputStream.write(onlineMsg.getBytes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkLogin(String login, String password) {
        try {
            users = userManagement.readFile();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        boolean check = false;
        for (User nextUser : users) {
            boolean loginMatch = nextUser.getLogin().equals(login);
            boolean passwordMatch = nextUser.getPassword().equals(password);
            if (loginMatch && passwordMatch) {
                check = true;
                break;
            }
        }
        return check;
    }

    public boolean isLoginExist(String login) {
        try {
            users = userManagement.readFile();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        boolean check = false;
        for (User nextUser : users) {
            if (nextUser.getLogin().equals(login)) {
                check = true;
                break;
            }
        }
        return check;
    }
}
