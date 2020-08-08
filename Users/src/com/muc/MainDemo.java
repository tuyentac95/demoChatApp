package com.muc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class MainDemo {
    private static final String PATH = "Database\\src\\usersProfile.txt";

    private static UserManagement userManagement = null;

    private static ArrayList<User> users = null;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        userManagement = new UserManagement(PATH);
        users = userManagement.readFile();

        Scanner scanner = new Scanner(System.in);

        System.out.println("___Login/SignUp___");
        System.out.println("1. Login");
        System.out.println("2. SignUp");

        int choice = scanner.nextInt();
        scanner.nextLine();

        switch (choice) {
            case 1:
                userLogin(scanner);
                break;
            case 2:
                userSignUp(scanner);
                break;
        }

    }

    public static void userLogin(Scanner scanner) {
        System.out.println("___LOGIN___");
        System.out.println("login: ");
        String login = scanner.nextLine();

        System.out.println("password: ");
        String password = scanner.nextLine();

        System.out.println("Checking...");

        boolean check = false;
        int position = -1;
        for (User user : users) {
            if (user.getLogin().equals(login) && user.getPassword().equals(password)) {
                check = true;
                position = users.indexOf(user);
                break;
            }
        }
        if (check) {
            System.out.println("You're logged in");
            System.out.println(users.get(position));
        } else {
            System.out.println("Invalid login/password");
        }
    }

    public static void userSignUp(Scanner scanner) throws IOException {
        System.out.println("___SIGN UP___");
        System.out.println("login: ");
        String login = scanner.nextLine();

        System.out.println("password: ");
        String password = scanner.nextLine();

        System.out.println("Checking...");

        boolean check = true;
        for (User user : users) {
            if (user.getLogin().equals(login)) {
                check = false;
                break;
            }
        }

        if (check) {
            System.out.println("Sign up successfully");
            User newUser = new User(login,password);
            System.out.println("Enter your name: ");
            String name = scanner.nextLine();
            newUser.setName(name);
            System.out.println("Enter your age: ");
            int age = scanner.nextInt();
            newUser.setAge(age);

            users.add(newUser);
            userManagement.setUsers(users);

            userManagement.writeFile();
        } else {
            System.out.println("Account is exist!");
        }
    }
}
