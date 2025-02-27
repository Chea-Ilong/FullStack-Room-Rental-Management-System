//import java.util.ArrayList;
//import java.util.List;
//
//public class Admin extends User {
//
//    private static List<Landlord> landlordList = new ArrayList<>();
//
//    private static final String ADMIN_USERNAME = "admin";
//    private static final String ADMIN_PASSWORD = "admin123";
//
//    public Admin() {
//        super(ADMIN_USERNAME, ADMIN_PASSWORD, "N/A", "Admin");
//    }
//
//    public boolean login(String username, String password) {
//        if (this.username.equals(username) && this.password.equals(password)) {
//            System.out.println("Admin login successful.");
//            return true;
//        } else {
//            System.out.println("Admin login failed: Invalid username or password.");
//            return false;
//        }
//    }
//
//    public void resetLandlordPassword(String landlordId, String newPassword) {
//        for (Landlord landlord : Landlord.getLandlordList()) {
//            if (landlord.getLandlordId().equals(landlordId)) {
//                landlord.setPassword(newPassword);
//                System.out.println("Password reset for landlord: " + landlordId);
//                return;
//            }
//        }
//        System.out.println("Landlord not found.");
//    }
//
//
//}
//


import java.util.ArrayList;
import java.util.List;

public class Admin extends User {

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private static Admin instance; // Singleton instance of Admin

    private static List<Landlord> landlordList = new ArrayList<>();

    private Admin() {
        super(ADMIN_USERNAME, ADMIN_PASSWORD, "N/A", "Admin");
    }

    // Singleton Pattern - Ensure only one Admin exists
    public static Admin getInstance() {
        if (instance == null) {
            instance = new Admin();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        if (this.username.equals(username) && this.password.equals(password)) {
            System.out.println("Admin login successful.");
            return true;
        } else {
            System.out.println("Admin login failed: Invalid username or password.");
            return false;
        }
    }

    // ======================= CRUD OPERATIONS FOR LANDLORD =======================

    // Create a new landlord and add to the list
    public void createLandlord(String landlordName, String landlordId, long landlordPhoneNumber, String password) {
        if (findLandlordById(landlordId) != null) {
            System.out.println("Error: Landlord with ID " + landlordId + " already exists.");
            return;
        }
        Landlord newLandlord = new Landlord(landlordName, landlordId, landlordPhoneNumber);
        newLandlord.setPassword(password);
        landlordList.add(newLandlord);
        System.out.println("Landlord " + landlordName + " created successfully.");
    }

    // Read landlord details
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

    // Update landlord details
    public void updateLandlord(String landlordId, String newName, long newPhoneNumber) {
        Landlord landlord = findLandlordById(landlordId);
        if (landlord == null) {
            System.out.println("Error: Landlord with ID " + landlordId + " not found.");
            return;
        }
        landlord.setLandlordName(newName);
        landlord.setLandlordPhoneNumber(newPhoneNumber);
        System.out.println("Landlord " + landlordId + " updated successfully.");
    }

    // Delete a landlord
    public void deleteLandlord(String landlordId) {
        Landlord landlord = findLandlordById(landlordId);
        if (landlord == null) {
            System.out.println("Error: Landlord with ID " + landlordId + " not found.");
            return;
        }
        landlordList.remove(landlord);
        System.out.println("Landlord " + landlordId + " deleted successfully.");
    }

    // Reset landlord password
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

    // View total revenue of a single landlord
    public void viewLandlordRevenue(String landlordId) {
        Landlord landlord = findLandlordById(landlordId);
        if (landlord == null) {
            System.out.println("Error: Landlord with ID " + landlordId + " not found.");
            return;
        }
        System.out.println("Total revenue for landlord " + landlord.getLandlordName() + ": $" + landlord.getRevenue());
    }

    // View total revenue from all landlords
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
