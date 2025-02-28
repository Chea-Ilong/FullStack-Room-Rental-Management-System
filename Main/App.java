package Main;

import Properties.Building;
import Properties.Floor;
import Properties.Room;
import Users.Landlord;
import Users.Tenant;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static Main.Menu.*;

public class App {
    public static void main(String[] args) {


      // Initialize some mock data
        List<Room> rooms = new ArrayList<>();
        List<Tenant> tenants = new ArrayList<>();

        // Create rooms
        Room room1 = new Room("101", 500, 200);
        Room room2 = new Room("102", 600, 250);
        Room room3 = new Room("103", 700, 300);
        Floor floor1 = new Floor("1");
        floor1.addRoom(room1);
        floor1.addRoom(room2);

        // Create tenants
        Tenant tenant1 = new Tenant("Alice", "T001", "1234567890");
        Tenant tenant2 = new Tenant("Bob", "T002", "9876543210");

        rooms.add(room1);
        rooms.add(room2);
        rooms.add(room3);

        tenants.add(tenant1);
        tenants.add(tenant2);

        // Create a Building with the rooms
        Building building1 = new Building("Building A");
        List<Building> buildings = new ArrayList<>();
        buildings.add(building1);

        // Create the landlord with buildings
        Landlord landlord1 = new Landlord("lin", "012312", "01", tenants, buildings);



        // Main login and navigation
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            int loginResult = login(scanner, landlord1, tenants);

            if (loginResult == 1) {
                landlordMenu(scanner, landlord1);
                // After returning from landlord menu, we'll come back here
            } else if (loginResult == 2) {
                tenantManagementMenu(scanner, landlord1); // Passing the landlord object
                // After returning from tenant menu, we'll come back here
            } else {
                // User chose to exit
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
            String username = scanner.nextLine().trim(); // Read username and trim
            System.out.print("Enter your password: ");
            String password = scanner.nextLine().trim(); // Read password and trim

            // First check if it's landlord
            if (landlord.login(scanner, username, password)) {
                return 1; // Landlord login successful
            }

            // Then check if it's a tenant
            for (Tenant tenant : tenants) {
                if (tenant.login(username, password)) {
                    System.out.println("\nLogin successful as Tenant: " + tenant.getName() + "!\n");
                    return 2; // Tenant login successful
                }
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
