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
    private List<Building> buildings;
    private BillRecord billRecord;
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
        this.billRecord = new BillRecord();
    }

    // ====================================================================================================
    // Login & Authentication
    // ====================================================================================================
    public boolean login(Scanner scanner, String username, String password) {
        username = username.trim();
        password = password.trim();

        if (this.name.equals(username) && this.IdCard.equals(password)) {
            System.out.println("Login successful for " + this.name);

            while (failedPinAttempts < MAX_PIN_ATTEMPTS) {
                System.out.print("Enter your 4-digit PIN: ");
                if (scanner.hasNextInt()) {
                    int pin = scanner.nextInt();
                    scanner.nextLine();

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
                    scanner.next();
                    failedPinAttempts++;
                }
            }
            System.out.println("Returning to login page.");
            return false;
        }
        return false;
    }

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

    private String hashPin(int pin) {
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
                scanner.nextLine();

                if (updatePINInDatabase(newPin)) {
                    this.failedPinAttempts = 0;
                    System.out.println("PIN reset successful. You can now log in with the new PIN.");
                } else {
                    System.out.println("Failed to update PIN in database.");
                }
            } else {
                System.out.println("Invalid input. PIN reset failed.");
                scanner.next();
            }
        } else {
            System.out.println("Incorrect Landlord ID or Contact Number. PIN reset failed.");
        }
    }

    private boolean updatePINInDatabase(int newPin) {
        String query = "UPDATE Landlords l " +
                "JOIN Users u ON l.user_id = u.user_id " +
                "SET l.landlord_pin = ?, l.failed_pin_attempts = 0 " +
                "WHERE u.IdCard = ?";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
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
    // Bill Management Methods
    // ====================================================================================================
    public void refreshBuildingList() {
        BuildingDML buildingDML = new BuildingDML();
        this.buildings = buildingDML.getAllBuildings();
    }

    public List<Bill> viewBillsForMonth(int year, int month) {
        BillDML billDML = new BillDML();
        return billDML.getBillsByMonth(year, month);
    }

    public List<Bill> viewBillsForTenant(String tenantId) {
        BillDML billDML = new BillDML();
        return billDML.getBillsByTenantId(tenantId);
    }

    public List<Bill> viewUnpaidBills() {
        BillDML billDML = new BillDML();
        return billDML.getUnpaidBills();
    }

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

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "N/A";
        }
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
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

    public void updateBuilding(String oldBuildingName, String newBuildingName, String newAddress) {
        BuildingDML buildingDML = new BuildingDML();
        Building building = buildingDML.getBuildingByName(oldBuildingName);

        if (building != null) {
            building.setBuildingName(newBuildingName);
            building.setAddress(newAddress);
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

    public void refreshBuildings() {
        buildings.clear();
        BuildingDML buildingDML = new BuildingDML();
        List<Building> allBuildings = buildingDML.getAllBuildings();
        buildings.addAll(allBuildings);
    }

    // ====================================================================================================
    // CRUD Operations for Floor
    // ====================================================================================================
    public void addFloorToBuilding(String buildingName, Floor floor) {
        BuildingDML buildingDML = new BuildingDML();
        buildingDML.addFloorToBuilding(buildingName, floor);
    }

    public void removeFloorFromBuilding(String buildingName, String floorNumber) {
        Building building = getBuildingByName(buildingName);
        if (building != null) {
            building.removeFloor(floorNumber);
        } else {
            System.out.println("Building " + buildingName + " not found.");
        }
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

    public void updateTenant(Tenant updatedTenant) {
        for (int i = 0; i < tenants.size(); i++) {
            if (tenants.get(i).getIdCard().equals(updatedTenant.getIdCard())) {
                tenants.set(i, updatedTenant);
                System.out.println("Tenant updated in Landlord's list: " + updatedTenant.getName());
                return;
            }
        }
        System.out.println("Tenant not found in Landlord's list for update: " + updatedTenant.getIdCard());
    }

    public void refreshTenants() {
        tenants.clear();
        TenantDML tenantDML = new TenantDML();
        List<Tenant> loadedTenants = tenantDML.getAllTenants();
        for (Tenant tenant : loadedTenants) {
            tenants.add(tenant);
        }
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

    public Building getBuildingByName(String buildingName) {
        BuildingDML buildingDML = new BuildingDML();
        return buildingDML.getBuildingByName(buildingName);
    }

    public Tenant getTenantByID(String tenantID) {
        for (Tenant tenant : tenants) {
            if (tenant.getIdCard().equals(tenantID)) {
                return tenant;
            }
        }
        return null;
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