package com.muc;

import java.io.*;
import java.util.ArrayList;

public class AddUserDemo {
    private static final String PATH = "Database\\src\\usersProfile.txt";

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        User user1 = new User("tuyen","pass");
        user1.setName("Tuyen Pham");
        user1.setAge(25);
        User user2 = new User("lien","pass");
        user2.setName("Lien Nguyen");
        user2.setAge(23);
        User user3 = new User("tuan","pass");
        user3.setName("Tuan Trinh");
        user3.setAge(19);
        User user4 = new User("mai","pass");
        user4.setName("Mai Tran");
        user4.setAge(20);

        UserManagement userManagement = new UserManagement(PATH);
        userManagement.addUser(user1);
        userManagement.addUser(user2);
        userManagement.addUser(user3);
        userManagement.addUser(user4);

        userManagement.writeFile();

        ArrayList<User> newList = (ArrayList<User>) userManagement.readFile();
        System.out.println(newList);
    }
}
