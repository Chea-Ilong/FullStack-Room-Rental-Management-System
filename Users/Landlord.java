package Users;

import DataBase.*;
import Exceptions.LandlordException;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Payment.Bill;
import Payment.BillRecord;
import Properties.Building;
import Properties.Floor;
import Properties.Room;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.*;

public class Landlord extends User {

    // ====================================================================================================
    // Fields
    // ====================================================================================================
    private List<Tenant> tenants;
    private List<Building> buildings; // Manage multiple buildings
    private BillRecord billRecord; // Add BillRecord reference
    private int failedPinAttempts;
    private static final int MAX_PIN_ATTEMPTS = 5;

    // ====================================================================================================
    // Constructor
    // ====================================================================================================
    public Landlord(String name, String IDcard, String contact, List<Tenant> tenants, List<Building> buildings) {
        super(name, IDcard, contact, "Landlord");
        this.failedPinAttempts = 0;
        this.tenants = tenants != null ? tenants : new ArrayList<>();
        this.buildings = buildings != null ? buildings : new ArrayList<>();
        this.billRecord = new BillRecord(); // Initialize BillRecord
    }

    // ====================================================================================================
    // Bill Management Methods
    // ====================================================================================================

    // Create and distribute bills to rooms in a specific building and floor
    public List<Bill> createBillsForFloor(String buildingName, String floorNumber,
                                          double rentAmount,
                                          Map<String, Integer> electricUsageMap,
                                          Map<String, Integer> waterUsageMap) {
        Building building = getBuildingByName(buildingName);
        if (building == null) {
            System.out.println("Building not found: " + buildingName);
            return new ArrayList<>();
        }

        Floor floor = building.getFloorByNumber(floorNumber);
        if (floor == null) {
            System.out.println("Floor not found: " + floorNumber);
            return new ArrayList<>();
        }

        List<Room> rooms = floor.getRooms();
        return billRecord.distributeBills(rooms, buildingName, floorNumber,
                rentAmount, electricUsageMap, waterUsageMap);
    }

    public void refreshBuildingList() {
        BuildingDML buildingDML = new BuildingDML();
        this.buildings = buildingDML.getAllBuildings();
    }
    // View bills for a specific month


    // ====================================================================================================
    // Login & Authentication
    // ====================================================================================================
    public boolean login(Scanner scanner, String username, String password) {
        username = username.trim();
        password = password.trim();

        if (this.name.equals(username) && this.IdCard.equals(password)) {
            System.out.println("Login successful for " + this.name);

            // Handle PIN authentication
            while (failedPinAttempts < MAX_PIN_ATTEMPTS) {
                System.out.print("Enter your 4-digit PIN: ");
                if (scanner.hasNextInt()) {
                    int pin = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline left-over

                    // Verify PIN against the database
                    if (verifyPINFromDatabase(pin)) {
                        System.out.println("PIN verified. Login successful as Landlord!");
                        failedPinAttempts = 0;
                        return true;
                    } else {
                        failedPinAttempts++;
                        System.out.println("Incorrect PIN. Attempts left: " + (MAX_PIN_ATTEMPTS - failedPinAttempts));
                        if (failedPinAttempts == MAX_PIN_ATTEMPTS) {
                            System.out.print("Too many failed PIN attempts. Press R to reset your PIN or L to go back to the login page: ");
                            String choice = scanner.nextLine().trim().toUpperCase();
                            if (choice.equals("R")) {
                                resetPIN(scanner);
                            } else if (choice.equals("L")) {
                                return false;
                            }
                        }
                    }
                } else {
                    System.out.println("Invalid input. Please enter a 4-digit PIN.");
                    scanner.next(); // Consume the invalid input
                    failedPinAttempts++;
                }
            }

            System.out.println("Returning to login page.");
            return false;
        }

        return false;
    }

    // Verify PIN against the database
    private boolean verifyPINFromDatabase(int enteredPin) {
        String query = "SELECT landlord_pin FROM Landlords l " +
                "JOIN Users u ON l.user_id = u.user_id " +
                "WHERE u.IdCard = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, this.IdCard);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String storedPin = rs.getString("landlord_pin");

                    // Compare the entered PIN with the stored PIN
                    // Note: This assumes the PIN is stored as a hash
                    // You may need to adjust this based on how the PIN is actually stored
                    return storedPin.equals(String.valueOf(enteredPin)) ||
                            storedPin.equals(hashPin(enteredPin));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error verifying PIN: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Hash the PIN for secure comparison
    private String hashPin(int pin) {
        // This is a placeholder - in a real implementation,
        // you would use a proper hashing algorithm like SHA-256
        // For now, we'll use the same hashing method as in the database schema
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(String.valueOf(pin).getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println("Error hashing PIN: " + e.getMessage());
            return "";
        }
    }

    // ====================================================================================================
    // PIN Reset
    // ====================================================================================================
    public void resetPIN(Scanner scanner) {
        System.out.print("Enter your Landlord ID: ");
        String inputID = scanner.nextLine().trim();
        System.out.print("Enter your Contact Number: ");
        String inputContact = scanner.nextLine().trim();

        if (this.IdCard.equals(inputID) && this.contact.equals(inputContact)) {
            System.out.print("Enter a new 4-digit PIN: ");
            if (scanner.hasNextInt()) {
                int newPin = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                // Update PIN in database
                if (updatePINInDatabase(newPin)) {
                    this.failedPinAttempts = 0;
                    System.out.println("PIN reset successful. You can now log in with the new PIN.");
                } else {
                    System.out.println("Failed to update PIN in database.");
                }
            } else {
                System.out.println("Invalid input. PIN reset failed.");
                scanner.next(); // Consume the invalid input
            }
        } else {
            System.out.println("Incorrect Landlord ID or Contact Number. PIN reset failed.");
        }
    }

    // Update PIN in database
    private boolean updatePINInDatabase(int newPin) {
        String query = "UPDATE Landlords l " +
                "JOIN Users u ON l.user_id = u.user_id " +
                "SET l.landlord_pin = ?, l.failed_pin_attempts = 0 " +
                "WHERE u.IdCard = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            // Hash the new PIN before storing
            ps.setString(1, hashPin(newPin));
            ps.setString(2, this.IdCard);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (SQLException e) {
            System.out.println("Error updating PIN: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ====================================================================================================
    // CRUD Operations for Room
    // ====================================================================================================
    // Find room by room number across all buildings and floors
    public Room getRoomAcrossAllBuildings(String roomNumber) {
        for (Building building : buildings) {
            for (Floor floor : building.getFloors()) {
                for (Room room : floor.getRooms()) {
                    if (room.getRoomNumber().equals(roomNumber)) {
                        return room; // Room found
                    }
                }
            }
        }
        return null; // Room not found
    }

    // ====================================================================================================
    // CRUD Operations for Floor
    // ====================================================================================================

    public void removeFloorFromBuilding(String buildingName, String floorNumber) {
        Building building = getBuildingByName(buildingName);
        if (building != null) {
            building.removeFloor(floorNumber);
        } else {
            System.out.println("Building " + buildingName + " not found.");
        }
    }

    // ====================================================================================================
    // CRUD Operations for Building
    // ====================================================================================================
    public void addBuilding(Building building) throws LandlordException {
        if (building == null) {
            throw new LandlordException("Cannot add a null building.");
        }

        if (buildingExists(building)) {
            throw new LandlordException("Building already exists: " + building.getBuildingName());
        }

        buildings.add(building);
        System.out.println("Building " + building.getBuildingName() + " has been added.");
    }

    public void removeBuilding(String buildingName) {
        BuildingDML buildingDML = new BuildingDML();
        int buildingId = buildingDML.getBuildingIdByName(buildingName);

        if (buildingId != -1) {
            buildingDML.deleteBuilding(buildingId);
            // Also remove from local collection if needed
            Building building = getBuildingByName(buildingName);
            if (building != null) {
                buildings.remove(building);
            }
            System.out.println("Building " + buildingName + " has been removed.");
        } else {
            System.out.println("Building not found.");
        }
    }

    public void updateBuilding(String oldBuildingName, String newBuildingName, String newAddress) {
        BuildingDML buildingDML = new BuildingDML();
        Building building = buildingDML.getBuildingByName(oldBuildingName);

        if (building != null) {
            // Update the building object
            building.setBuildingName(newBuildingName);
            building.setAddress(newAddress);

            // Get the building ID and update it in the database
            int buildingId = buildingDML.getBuildingIdByName(oldBuildingName);
            if (buildingId != -1) {
                buildingDML.updateBuilding(buildingId, building);
                System.out.println("Building " + oldBuildingName + " has been updated to " + newBuildingName + " at " + newAddress);
            } else {
                System.out.println("Building ID could not be found in database.");
            }
        } else {
            System.out.println("Building not found.");
        }
    }

    private boolean buildingExists(Building newBuilding) {
        BuildingDML buildingDML = new BuildingDML();
        Building existingBuilding = buildingDML.getBuildingByName(newBuilding.getBuildingName());
        return existingBuilding != null;
    }

    // ====================================================================================================
    // CRUD Operations for Tenant
    // ====================================================================================================
    public void addTenant(Tenant tenant) {
        TenantDML tenantDML = new TenantDML();
        if (!tenantDML.tenantExists(tenant.getIdCard())) {
            tenantDML.saveTenant(tenant);
            System.out.println("Tenant added: " + tenant.getName());
        }
    }

    public void removeTenant(String tenantID) throws TenantException, RoomException {
        Tenant tenant = getTenantByID(tenantID);
        if (tenant != null) {
            // If tenant has an assigned room, remove them from it
            Room assignedRoom = tenant.getAssignedRoom();
            if (assignedRoom != null) {
                assignedRoom.removeTenant();
                System.out.println(tenant.getName() + " has been removed from Room " + assignedRoom.getRoomNumber());
            }

            if (tenants.remove(tenant)) {
                System.out.println("Tenant removed from Landlord's list: " + tenant.getName());
            } else {
                System.out.println("Failed to remove tenant from Landlord's list: " + tenant.getName());
            }
        } else {
            throw new TenantException("Tenant not found with ID: " + tenantID);
        }
    }

    public Building getBuildingByName(String buildingName) {
        BuildingDML buildingDML = new BuildingDML();
        return buildingDML.getBuildingByName(buildingName);
    }

    public void addFloorToBuilding(String buildingName, Floor floor) {
        BuildingDML buildingDML = new BuildingDML();
        buildingDML.addFloorToBuilding(buildingName, floor);
    }

    public Tenant getTenantByID(String tenantID) {
        for (Tenant tenant : tenants) {
            if (tenant.getIdCard().equals(tenantID)) {
                return tenant;
            }
        }
        return null; // Tenant not found
    }

    // ====================================================================================================
    // Getter Methods
    // ====================================================================================================
    public List<Tenant> getTenants() {
        return tenants;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    public BillRecord getBillRecord() {
        return billRecord;
    }

    // ====================================================================================================
    // Display Information Methods
    // ====================================================================================================
    public void displayAllBuildings() {
        System.out.println("\n===== All Buildings =====");
        if (buildings.isEmpty()) {
            System.out.println("No buildings available.");
            return;
        }

        for (Building building : buildings) {
            System.out.println("Building Name: " + building.getBuildingName() +
                    " | Address: " + building.getAddress());

            List<Floor> floors = building.getFloors();
            if (floors.isEmpty()) {
                System.out.println("  No floors available.");
            } else {
                for (Floor floor : floors) {
                    System.out.println("  Floor: " + floor.getFloorNumber());

                    List<Room> rooms = floor.getRooms();
                    if (rooms.isEmpty()) {
                        System.out.println("    No rooms available.");
                    } else {
                        System.out.println("    Rooms: ");
                        for (Room room : rooms) {
                            System.out.println("      - Room: " + room.getRoomNumber());
                        }
                    }
                }
            }
            System.out.println("---------------------");
        }
    }
    // Add these methods to your Landlord class:
// Add this to the Landlord class if it doesn't exist
    // Add this to the Landlord class
    public void refreshBuildings() {
        // Clear existing buildings
        buildings.clear();

        // Fetch all buildings from database
        BuildingDML buildingDML = new BuildingDML();
        List<Building> allBuildings = buildingDML.getAllBuildings();

        // Add them to the landlord's building list
        buildings.addAll(allBuildings);
    }
    /**
     * Refreshes data for all tenants from the database
     */
    public void refreshAllTenants() {
        TenantDML tenantDML = new TenantDML();
        List<Tenant> freshTenants = tenantDML.getAllTenants(this.getIdCard());

        // Update our tenants list
        this.tenants.clear();
        if (freshTenants != null) {
            this.tenants.addAll(freshTenants);
        }
    }
    public List<Bill> viewBillsForMonth(int year, int month) {
        BillDML billDML = new BillDML();
        return billDML.getBillsByMonth(year, month);
    }

    // Function to view bills for a specific tenant
    public List<Bill> viewBillsForTenant(String tenantId) {
        BillDML billDML = new BillDML();
        return billDML.getBillsByTenantId(tenantId);
    }

    // Function to view all unpaid bills
    public List<Bill> viewUnpaidBills() {
        BillDML billDML = new BillDML();
        return billDML.getUnpaidBills();
    }

    // Function to generate a monthly billing report
    public String generateMonthlyBillingReport(int year, int month) {
        BillDML billDML = new BillDML();
        List<Bill> bills = billDML.getBillsByMonth(year, month);

        StringBuilder report = new StringBuilder();
        report.append("\n===== Monthly Billing Report: ").append(month).append("/").append(year).append(" =====\n\n");

        if (bills.isEmpty()) {
            report.append("No bills found for ").append(month).append("/").append(year);
            return report.toString();
        }

        report.append(String.format("%-12s | %-8s | %-8s | %-20s | %14s | %14s | %14s | %14s | %-8s\n",
                "Building", "Floor", "Room", "Tenant", "Rent (KHR)", "Electric (KHR)", "Water (KHR)", "Total (KHR)", "Status"));
        report.append("----------------------------------------------------------------------------------------------------------------------\n");

        double totalRent = 0;
        double totalElectric = 0;
        double totalWater = 0;
        double totalAmount = 0;
        int totalPaid = 0;
        int totalUnpaid = 0;

        for (Bill bill : bills) {
            String tenantName = bill.getTenant() != null ? bill.getTenant().getName() : "N/A";
            if (tenantName.length() > 20) {
                tenantName = tenantName.substring(0, 17) + "...";
            }

            report.append(String.format("%-12s | %-8s | %-8s | %-20s | %,14.2f | %,14.2f | %,14.2f | %,14.2f | %-8s\n",
                    truncate(bill.getBuildingName(), 12),
                    truncate(bill.getFloorNumber(), 8),
                    truncate(bill.getRoom().getRoomNumber(), 8),
                    tenantName,
                    bill.getRentAmount(),
                    bill.getElectricAmount(),
                    bill.getWaterAmount(),
                    bill.getTotalAmount(),
                    bill.isPaid() ? "PAID" : "UNPAID"));

            totalRent += bill.getRentAmount();
            totalElectric += bill.getElectricAmount();
            totalWater += bill.getWaterAmount();
            totalAmount += bill.getTotalAmount();
            if (bill.isPaid()) totalPaid++; else totalUnpaid++;
        }

        report.append("----------------------------------------------------------------------------------------------------------------------\n");
        report.append(String.format("%-44s | %,14.2f | %,14.2f | %,14.2f | %,14.2f |\n",
                "TOTALS:", totalRent, totalElectric, totalWater, totalAmount));

        report.append("\nSUMMARY:\n");
        report.append("Total Bills: ").append(bills.size()).append("\n");
        report.append("Paid Bills: ").append(totalPaid).append("\n");
        report.append("Unpaid Bills: ").append(totalUnpaid).append("\n");
        if (totalPaid + totalUnpaid > 0) {
            report.append("Collection Rate: ").append(String.format("%.1f%%", (double)totalPaid / (totalPaid + totalUnpaid) * 100)).append("\n");
        }

        return report.toString();
    }

    // Helper method to truncate strings for table formatting
    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "N/A";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }

    // Additional helper function to mark a bill as paid
    public boolean payBill(int billId) {
        BillDML billDML = new BillDML();
        return billDML.markBillAsPaid(billId);
    }
    /**
     * Refreshes data for a specific tenant from the database
     * @param tenantId The ID of the tenant to refresh
     * @return true if tenant was found and refreshed, false otherwise
     */
    public boolean refreshTenant(String tenantId) {
        TenantDML tenantDML = new TenantDML();
        Tenant freshTenant = tenantDML.getTenantById(tenantId, this.getIdCard());

        if (freshTenant != null) {
            // Find and replace the tenant in our list
            for (int i = 0; i < this.tenants.size(); i++) {
                if (this.tenants.get(i).getIdCard().equals(tenantId)) {
                    this.tenants.set(i, freshTenant);
                    return true;
                }
            }
            // If not found in our list, add it
            this.tenants.add(freshTenant);
            return true;
        }
        return false;
    }
    public void refreshTenants() {
        // Clear existing tenants
        tenants.clear();

        // Reload all tenants from database
        TenantDML tenantDML = new TenantDML();
        List<Tenant> loadedTenants = tenantDML.getAllTenants();

        // Add all loaded tenants
        for (Tenant tenant : loadedTenants) {
            tenants.add(tenant);
        }
    }
    public void updateTenant(Tenant updatedTenant) {
        for (int i = 0; i < tenants.size(); i++) {
            if (tenants.get(i).getIdCard().equals(updatedTenant.getIdCard())) {
                tenants.set(i, updatedTenant); // Replace the old tenant with the updated one
                System.out.println("Tenant updated in Landlord's list: " + updatedTenant.getName());
                return;
            }
        }
        System.out.println("Tenant not found in Landlord's list for update: " + updatedTenant.getIdCard());
    }
    // Display tenant details
    public void displayAllTenants() {
        TenantDML tenantDML = new TenantDML();
        List<Tenant> tenants = tenantDML.getAllTenantsForLandlord();

        System.out.println("===== All Tenants =====");
        for (Tenant tenant : tenants) {
            System.out.println("Name: " + tenant.getName());
            System.out.println("ID: " + tenant.getIdCard());
            System.out.println("Contact: " + tenant.getContact());
            Room room = tenant.getAssignedRoom();
            if (room != null) {
                System.out.println("Room: " + room.getRoomNumber());
            } else {
                System.out.println("Room: Not assigned");
            }
            System.out.println("-----------------------");
        }
    }

    // ====================================================================================================
    // toString Method
    // ====================================================================================================
    @Override
    public String toString() {
        return super.toString() +
                "Landlord{" +
                "tenants=" + tenants +
                ", buildings=" + buildings +
                '}';
    }
}