package Main;

import DataBase.DataBaseConnection;
import DataBase.LandlordDML;
import DataBase.TenantDML;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Users.Landlord;
import Users.Tenant;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import static Main.Menu.*;

public class App {


    //building need real time config
    //tenant pay rent and utility
    //add usage to room and display to each tenant corresponding to the room


    // Add a class variable to keep track of the currently logged in tenant
    private static Tenant currentLoggedInTenant = null;

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

        // Create a LandlordDML class to fetch the landlord from the database
        LandlordDML landlordDML = new LandlordDML();
        Landlord landlord = landlordDML.getLandlordByIdCard("123");

        if (landlord == null) {
            System.err.println("Could not find landlord in database. Please check your data.");
            return;
        }

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
                // Use the currentLoggedInTenant instead of tenants.get(0)
                tenantMenu(scanner, currentLoggedInTenant, landlord);
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

            if (landlord.login(scanner, username, password)) {
                return 1;
            }

            for (Tenant tenant : tenants) {
                if (tenant.login(username, password)) {
                    // Print once here
                    System.out.println("\nLogin successful as Tenant: " + tenant.getName() + "!\n");
                    currentLoggedInTenant = tenant;
                    return 2;
                }
            }

            TenantDML tenantDML = new TenantDML();
            Tenant databaseTenant = tenantDML.getTenantByCredentials(username, password);
            if (databaseTenant != null) {
                // Remove the duplicate "Login successful for" line
                System.out.println("\nLogin successful as Tenant: " + databaseTenant.getName() + "!\n");
                tenants.add(databaseTenant);
                currentLoggedInTenant = databaseTenant;
                return 2;
            }

            System.out.print("Login failed. Press X to exit or any other key to try again: ");
            String choice = scanner.nextLine();
            if (choice.equalsIgnoreCase("X")) {
                return 0;
            }
        }
    }
}