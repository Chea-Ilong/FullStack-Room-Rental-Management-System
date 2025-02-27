package Users;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Admin extends User {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";
    private static final int ADMIN_PIN = 1234;  // 4-digit security PIN
    private static final int MAX_LOGIN_ATTEMPTS = 5;

    private static Admin instance;  // Singleton instance
    private static List<Landlord> landlordList = new ArrayList<>();
    private int loginAttempts = 0;
    private boolean isLocked = false; // Track if admin is locked out

    private Admin() {
        super(ADMIN_USERNAME, ADMIN_PASSWORD, "N/A", "Users.Admin");
    }

    // Singleton Pattern - Ensure only one Admin exists
    public static synchronized Admin getInstance() {
        if (instance == null) {
            instance = new Admin();
        }
        return instance;
    }

    @Override
    public boolean login(String username, String password) {
        if (isLocked) {
            System.out.println("Admin account is locked due to too many failed login attempts.");
            return false;
        }

        if (this.username.equals(username) && this.password.equals(password)) {
            if (verifyPin()) {
                System.out.println("Admin login successful.");
                loginAttempts = 0;  // Reset login attempts
                return true;
            } else {
                System.out.println("Incorrect PIN. Login failed.");
                return false;
            }
        } else {
            loginAttempts++;
            System.out.println("Admin login failed: Invalid username or password. Attempts left: " + (MAX_LOGIN_ATTEMPTS - loginAttempts));
            if (loginAttempts >= MAX_LOGIN_ATTEMPTS) {
                isLocked = true;
                System.out.println("Admin account locked due to too many failed login attempts.");
            }
            return false;
        }
    }

    private boolean verifyPin() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter 4-digit PIN: ");
        int enteredPin = scanner.nextInt();
        return enteredPin == ADMIN_PIN;
    }

    // ======================= CRUD OPERATIONS FOR LANDLORD =======================

    public void createLandlord(String landlordName, String landlordId, String landlordPhoneNumber, String password) {
        if (findLandlordById(landlordId) != null) {
            System.out.println("Error: Landlord with ID " + landlordId + " already exists.");
            return;
        }
        Landlord newLandlord = new Landlord(landlordName, landlordId, landlordPhoneNumber);
        newLandlord.setPassword(password);
        landlordList.add(newLandlord);
        System.out.println("Landlord " + landlordName + " created successfully.");
    }

    public void viewAllLandlords() {
        if (landlordList.isEmpty()) {
            System.out.println("No landlords available.");
            return;
        }
        System.out.println("===== Landlord List =====");
        for (Landlord landlord : landlordList) {
            System.out.println("Landlord Name: " + landlord.getLandlordName() +
                    " | ID: " + landlord.getLandlordId() +
                    " | Phone: " + landlord.getLandlordPhoneNumber() +
                    " | Revenue: $" + landlord.getRevenue());
        }
    }

    public void updateLandlord(String landlordId, String newName, String newPhoneNumber) {
        Landlord landlord = findLandlordById(landlordId);
        if (landlord == null) {
            System.out.println("Error: Landlord with ID " + landlordId + " not found.");
            return;
        }
        landlord.setLandlordName(newName);
        landlord.setLandlordPhoneNumber(newPhoneNumber);
        System.out.println("Landlord " + landlordId + " updated successfully.");
    }

    public void deleteLandlord(String landlordId) {
        Landlord landlord = findLandlordById(landlordId);
        if (landlord == null) {
            System.out.println("Error: Landlord with ID " + landlordId + " not found.");
            return;
        }
        landlordList.remove(landlord);
        System.out.println("Landlord " + landlordId + " deleted successfully.");
    }

    public void resetLandlordPassword(String landlordId, String newPassword) {
        Landlord landlord = findLandlordById(landlordId);
        if (landlord == null) {
            System.out.println("Error: Landlord with ID " + landlordId + " not found.");
            return;
        }
        landlord.setPassword(newPassword);
        System.out.println("Password reset for landlord: " + landlordId);
    }

    // ======================= REVENUE MANAGEMENT =======================

    public void viewLandlordRevenue(String landlordId) {
        Landlord landlord = findLandlordById(landlordId);
        if (landlord == null) {
            System.out.println("Error: Landlord with ID " + landlordId + " not found.");
            return;
        }
        System.out.println("Total revenue for landlord " + landlord.getLandlordName() + ": $" + landlord.getRevenue());
    }

    public void viewTotalRevenue() {
        double totalRevenue = 0;
        for (Landlord landlord : landlordList) {
            totalRevenue += landlord.getRevenue();
        }
        System.out.println("Total revenue from all buildings: $" + totalRevenue);
    }

    // ======================= HELPER FUNCTION =======================

    private Landlord findLandlordById(String landlordId) {
        for (Landlord landlord : landlordList) {
            if (landlord.getLandlordId().equals(landlordId)) {
                return landlord;
            }
        }
        return null;
    }
}