package Users;

import Interface.Authentication;
import java.util.ArrayList;

public abstract class User implements Authentication {

    // ============================ User Information ============================
    protected String name;
    protected String IdCard;
    protected String contact;
    protected String role;

    private static final ArrayList<User> userList = new ArrayList<>();

    // ============================ Constructor ============================
    public User(String username, String password, String phoneNumber, String role) {
        this.name = username;
        this.IdCard = password;
        this.contact = phoneNumber;
        this.role = role;
        userList.add(this);
    }

    // ============================ Getters ============================
    public String getName() {
        return name;
    }

    public String getIdCard() {
        return IdCard;
    }

    public String getContact() {
        return contact;
    }

    // ============================ Login Method ============================
    public boolean login(String username, String password) {

        if (this.name.equals(username) && this.IdCard.equals(password)) {
            System.out.println("Login successful for " + this.name);
            return true;
        }
        return false;
    }

    // ============================ String Representation ============================
    @Override
    public String toString() {
        return "name='" + name + '\'' +
                ", IdCard='" + IdCard + '\'' +
                ", contact='" + contact + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
