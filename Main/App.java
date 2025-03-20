package Main;
import DataBase.DataBaseConnection;
import DataBase.LandlordDML;
import DataBase.TenantDML;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Users.Landlord;
import Users.Tenant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import static Main.Menu.*;

public class App {
    private static Tenant currentLoggedInTenant = null;

    public static void main(String[] args) throws RoomException, TenantException {
        try (Connection connection = DataBaseConnection.getConnection()) {
            if (connection != null) {
                System.out.println("Database connection successful!");
            } else {
                System.out.println("Failed to make connection!");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
            return;
        }

        // Get the first landlord from the database instead of using a hardcoded ID
        String landlordIdCard = getFirstLandlordIdCard();
        if (landlordIdCard == null) {
            System.err.println("No landlords found in database. Please create a landlord first.");
            return;
        }

        // Create a LandlordDML class to fetch the landlord from the database
        LandlordDML landlordDML = new LandlordDML();
        Landlord landlord = landlordDML.getLandlordByIdCard(landlordIdCard);
        if (landlord == null) {
            System.err.println("Could not find landlord in database. Please check your data.");
            return;
        }

        System.out.println("Successfully loaded landlord: " + landlord.getName());

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

    // Helper method to get the first landlord ID card from the database
    private static String getFirstLandlordIdCard() {
        String query = "SELECT u.IdCard FROM Users u JOIN Landlords l ON u.user_id = l.user_id WHERE u.role = 'Landlord' LIMIT 1";

        try (Connection conn = DataBaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getString("IdCard");
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving landlord: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
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
                    // Print once herex
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