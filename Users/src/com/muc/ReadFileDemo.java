package com.muc;

import java.io.*;
import java.util.ArrayList;

public class ReadFileDemo {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String path = "Database\\src\\usersProfile.txt";
        UserManagement userManagement = new UserManagement(path);
        userManagement.setUsers(userManagement.readFile());
        userManagement.display();

//        String path = "Database\\src\\userschat.txt";
//        File file = new File(path);
//        FileInputStream fileIS = new FileInputStream(file);
//        ObjectInputStream objectIS = new ObjectInputStream(fileIS);
//        ArrayList<String> newList = new ArrayList<>();
//        String mess = null;
//        try {
//            while ((mess = (String) objectIS.readObject()) != null) {
//                newList.add(mess);
//            }
//        } catch (EOFException ignored) {
//        }
//
//        for (String message : newList) {
//            System.out.println(message);
//        }

    }
}
