package com.muc;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ChatClient {
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedIn;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost",8819);
        client.addUserStatusListener(new UserStatusListener() {
            @Override
            public void online(String login) {
                System.out.println("ONLINE: " + login);
            }

            @Override
            public void offline(String login) {
                System.out.println("OFFLINE: " + login);
            }
        });

        client.addMessageListener(new MessageListener() {
            @Override
            public void onMessage(String fromLogin, String messageBody) {
                System.out.println("You got a message from " + fromLogin + " ==> " + messageBody);
            }
        });

        if (!client.connect()) {
            System.err.println("Connect failed");
        } else {
            System.out.println("Connect successful");
            if (client.login("guest","pass") == 1) {
                System.out.println("Login successful");
            } else {
                System.err.println("Login failed");
            }
        }
    }

    public void msg(String sendTo, String messageBody) throws IOException {
        String cmd = "msg " + sendTo + " " + messageBody + "\n";
        serverOut.write(cmd.getBytes());
    }

    public int login(String login, String password) throws IOException {
        String cmd = "login " + login + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line: " + response);

        if (response.equalsIgnoreCase("ok login")) {
            startMessageReader();
            return 1;
        } else if (response.equalsIgnoreCase("already login")) {
            return 0;
        } else {
            return -1;
        }
    }

    public boolean signUp(String newUserSign) throws IOException {
        String cmd = "sign " + newUserSign + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedIn.readLine();
        System.out.println("Response Line: " + response);

        if (response.equalsIgnoreCase("ok sign up")) {
            return true;
        } else {
            return false;
        }
    }

    public void logoff() throws IOException{
        String cmd = "logoff\n";
        serverOut.write(cmd.getBytes());
    }

    public void loadMessage(String friend) throws IOException {
        String cmd = "load " + friend + "\n";
        serverOut.write(cmd.getBytes());

        //startMessageReader();
    }

    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedIn.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if (cmd.equalsIgnoreCase("online")) {
                        handleOnline(tokens);
                    } else if (cmd.equalsIgnoreCase("offline")) {
                        handleOffline(tokens);
                    } else if (cmd.equalsIgnoreCase("msg")) {
                        String[] tokensMsg = line.split(" ", 3);
                        handleMessage(tokensMsg);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void handleMessage(String[] tokensMsg) {
        String login = tokensMsg[1];
        String msgBody = tokensMsg[2];
        for (MessageListener listener : messageListeners) {
            listener.onMessage(login,msgBody);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.online(login);
        }
    }

    public boolean connect() {
        try {
            this.socket = new Socket(serverName,serverPort);
            System.out.println("Client port is " + socket.getLocalPort());
            this.serverOut = socket.getOutputStream();
            this.serverIn = socket.getInputStream();
            this.bufferedIn = new BufferedReader(new InputStreamReader(serverIn));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void addUserStatusListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeUserStatusListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }
}
