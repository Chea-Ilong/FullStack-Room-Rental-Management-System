package Main;

import DataBase.DataBaseConnection;
import DataBase.LandlordDML;
import DataBase.TenantDML;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Properties.Building;
import Properties.Floor;
import Properties.Room;
import Users.Landlord;
import Users.Tenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import static Main.Menu.*;

public class App {
    public static void main(String[] args) throws RoomException, TenantException {

        try (Connection connection = DataBaseConnection.getConnection()) {
            if (connection != null) {
                System.out.println("Database connection successful!");
            } else {
                System.out.println("Failed to make connection!");
            }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        }

        // Initialize some mock data
//        List<Room> rooms = new ArrayList<>();
//        List<Tenant> tenants = new ArrayList<>();
//
//        // Create rooms
//        Room room1 = new Room("101", 500, 200);
//        Room room2 = new Room("102", 600, 250);
//
//        Floor floor1 = new Floor("1");
//        floor1.addRoom(room1);
//        floor1.addRoom(room2);
//
//        // Create tenants
//        Tenant tenant1 = new Tenant("Alice", "T001", "1234567890");
//        Tenant tenant2 = new Tenant("Bob", "T002", "9876543210");
//
//        tenants.add(tenant1);
//        tenants.add(tenant2);
//
//        // Create a Building with the rooms
//        Building building1 = new Building("Building A","Phnom Penh City");
//        List<Building> buildings = new ArrayList<>();
//        buildings.add(building1);
//        building1.addFloor(floor1);
//
//        // Create the landlord with buildings
//        Landlord landlord1 = new Landlord("lin", "123", "01", tenants, buildings);
//        landlord1.assignedTenantRoom("T001","101");
//
//
//        // Main login and navigation
//        Scanner scanner = new Scanner(System.in);
//        boolean running = true;
//
//        while (running) {
//            int loginResult = login(scanner, landlord1, tenants);
//
//            if (loginResult == 1) {
//                landlordMenu(scanner, landlord1);
//                // After returning from landlord menu, we'll come back here
//            } else if (loginResult == 2) {
//                tenantMenu(scanner, tenant1, landlord1); // Passing the landlord object
//                // After returning from tenant menu, we'll come back here
//            } else {
//                // User chose to exit
//                running = false;
//            }
//        }
//
//        scanner.close();
//        System.out.println("\nExiting... Goodbye!\n");
//    }


        // Create a LandlordDML class to fetch the landlord from the database
        LandlordDML landlordDML = new LandlordDML();
        Landlord landlord = landlordDML.getLandlordByIdCard("123");

        if (landlord == null) {
            System.err.println("Could not find landlord in database. Please check your data.");
            return;
        }

        // Load tenants for this landlord
        // Load all tenants for this landlord
        TenantDML tenantDML = new TenantDML();
        List<Tenant> tenants = tenantDML.getAllTenantsForLandlord();

        if (tenants.isEmpty()) {
            System.out.println("Warning: No tenants found in database.");
        }
        // Set up scanner and menu system
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            int loginResult = login(scanner, landlord, tenants);

            if (loginResult == 1) {
                landlordMenu(scanner, landlord);
            } else if (loginResult == 2) {
                tenantMenu(scanner, tenants.get(0), landlord); // Adjust as needed
            } else {
                running = false;
            }
        }

        scanner.close();
        System.out.println("\nExiting... Goodbye!\n");
    }
    public static int login(Scanner scanner, Landlord landlord, List<Tenant> tenants) {
        while (true) {
            System.out.println("\n===== Welcome to House Rental System =====");
            System.out.print("Enter your username: ");
            String username = scanner.nextLine().trim();
            System.out.print("Enter your password: ");
            String password = scanner.nextLine().trim();

            // First check if it's landlord
            if (landlord.login(scanner, username, password)) {
                return 1; // Landlord login successful
            }

            // Check in-memory tenants first
            for (Tenant tenant : tenants) {
                if (tenant.login(username, password)) {
                    System.out.println("\nLogin successful as Tenant: " + tenant.getName() + "!\n");
                    return 2; // Tenant login successful
                }
            }

            // If not found in memory, check database
            TenantDML tenantDML = new TenantDML();
            Tenant databaseTenant = tenantDML.getTenantByCredentials(username, password);
            if (databaseTenant != null) {
                // Add to in-memory list for future reference
                tenants.add(databaseTenant);
                System.out.println("\nLogin successful as Tenant: " + databaseTenant.getName() + "!\n");
                return 2; // Tenant login successful
            }

            // If we reach here, login failed
            System.out.print("Login failed. Press X to exit, or press any other key to try again: ");
            String choice = scanner.nextLine();
            if (choice.equalsIgnoreCase("X")) {
                return 0; // Exit
            }
        }
    }


}
