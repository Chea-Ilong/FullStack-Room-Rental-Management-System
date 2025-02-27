package Users;

import Interface.Authentication;
import java.util.ArrayList;

public abstract class User implements Authentication {
    protected String username;
    protected String password;
    protected String phoneNumber;
    protected String role; // "Users.Tenant" or "Users.Landlord"

    // ArrayList to store all users
    private static ArrayList<User> userList = new ArrayList<>();

    public User(String username, String password, String phoneNumber, String role) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        userList.add(this);
//        signUp();
    }

    public boolean login(String username, String password) {
        for (User user : userList) {
            if (user.username.equals(username) && user.password.equals(password)) {
                System.out.println("Login successful for " + user.username);
                return true;
            }
        }
        System.out.println("Login failed!");
        return false;
    }

    public static void displayAllUsers() {
        System.out.println("Registered Users:");
        for (User user : userList) {
            System.out.println(user.username + " (" + user.role + ")");
        }
    }

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

}
