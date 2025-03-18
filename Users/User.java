package Users;

import Interface.Authentication;
import java.util.ArrayList;

public abstract class User implements Authentication {

    // ====================================================================================================
    // User Information
    // ====================================================================================================
    protected String name;
    protected String IdCard;
    protected String contact;
    protected String role;

    private static final ArrayList<User> userList = new ArrayList<>();

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public User(String username, String password, String phoneNumber, String role) {
        this.name = username;
        this.IdCard = password;
        this.contact = phoneNumber;
        this.role = role;
        userList.add(this);
    }

    // ====================================================================================================
    // Getters
    // ====================================================================================================
    public String getName() {
        return name;
    }

    public String getIdCard() {
        return IdCard;
    }

    public String getContact() {
        return contact;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIdCard(String idCard) {
        IdCard = idCard;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    // ====================================================================================================
    // Login Method
    // ====================================================================================================
    public boolean login(String username, String password) {
        if (this.name.equals(username) && this.IdCard.equals(password)) {
            System.out.println("Login successful for " + this.name);
            return true;
        }
        return false;
    }

    // ====================================================================================================
    // String Representation
    // ====================================================================================================
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(name).append("\n");
        sb.append("ID Card: ").append(IdCard).append("\n");
        sb.append("Contact: ").append(contact).append("\n");
        sb.append("Role: ").append(role);
        return sb.toString();
    }

}
