package com.muc;

import java.io.IOException;
import java.util.ArrayList;

public class ReadFileDemo {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        String path = "Database\\src\\usersProfile.txt";
        UserManagement userManagement = new UserManagement(path);
        userManagement.setUsers(userManagement.readFile());
        userManagement.display();
    }
}
