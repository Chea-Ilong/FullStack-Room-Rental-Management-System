import java.util.ArrayList;

public abstract class User implements Authentication {
    protected String username;
    protected String password;
    protected String phoneNumber;
    protected String role; // "Tenant" or "Landlord"

    // ArrayList to store all users
    private static ArrayList<User> userList = new ArrayList<>();

    public User(String username, String password, String phoneNumber, String role) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        signUp();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
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

    @Override
    public void signUp() {
        // Check if username already exists
        for (User user : userList) {
            if (user.username.equals(this.username)) {
                System.out.println("Sign-up failed: Username already exists.");
                return;
            }
        }
        // Add new user to list
        userList.add(this);
        System.out.println(username + " has been successfully registered as " + role);
    }

    // Display all registered users
    public static void displayAllUsers() {
        System.out.println("Registered Users:");
        for (User user : userList) {
            System.out.println(user.username + " (" + user.role + ")");
        }
    }
}
