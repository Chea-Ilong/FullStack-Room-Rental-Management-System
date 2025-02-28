package Users;

import java.util.ArrayList;

public abstract class User {
    protected String name;
    protected String IdCard;
    protected String contact;
    protected String role;

    private static final ArrayList<User> userList = new ArrayList<>();
    public User(String username, String password, String phoneNumber, String role) {
        this.name = username;
        this.IdCard = password;
        this.contact = phoneNumber;
        this.role = role;
        userList.add(this);
    }

    // Getters for user details
    public String getName() {
        return name;
    }

    public String getIdCard() {
        return IdCard;
    }

    public String getContact() {
        return contact;
    }
    public boolean login(String username, String password) {
        if (this.name.equals(username) && this.IdCard.equals(password)) {
            System.out.println("Login successful for " + this.name);
            return true;
        } else {
            System.out.println("Login failed for " + username);
            return false;
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", IdCard='" + IdCard + '\'' +
                ", contact='" + contact + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
