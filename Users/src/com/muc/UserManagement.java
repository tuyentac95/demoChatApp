package com.muc;

import java.io.*;
import java.util.ArrayList;

public class UserManagement{
    private static File file;
    private static ArrayList<User> users;

    public UserManagement() {
    }

    public UserManagement(String path) throws IOException, ClassNotFoundException {
        file = new File(path);
        users = new ArrayList<>();
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        UserManagement.users = users;
    }

    public void writeFile() throws IOException {
        FileOutputStream fileOS = new FileOutputStream(file);
        ObjectOutputStream objectOS = new ObjectOutputStream(fileOS);
        for (User user : users) {
            objectOS.writeObject(user);
        }
    }

    public ArrayList<User> readFile() throws IOException, ClassNotFoundException {
        FileInputStream fileIS = new FileInputStream(file);
        ObjectInputStream objectIS = new ObjectInputStream(fileIS);
        ArrayList<User> newList = new ArrayList<>();
        User user = null;
        try {
            while ((user = (User) objectIS.readObject()) != null) {
                newList.add(user);
            }
        } catch (EOFException ignored) {
        }
        return newList;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public void display() {
        System.out.println("=== List User ===");
        for (User user : users) {
            System.out.println(user);
        }
    }
}
