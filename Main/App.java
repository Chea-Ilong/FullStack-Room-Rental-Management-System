package Main;

import Properties.Room;
import Users.Landlord;
import Users.Tenant;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        // Sample data for testing
        List<Room> rooms = new ArrayList<>();
        List<Tenant> tenants = new ArrayList<>();

        // Create rooms with initial meter readings
        Room room1 = new Room("101", 500, 200);
        Room room2 = new Room("102", 600, 250);
        Room room3 = new Room("103", 700, 300);

        Tenant tenant1 = new Tenant("Alice", "T001", "1234567890");
        Tenant tenant2 = new Tenant("Bob", "T002", "9876543210");

        // Change this line in main method
        Landlord landlord1 = new Landlord("long", "012312", "12", tenants, rooms);  // Added PIN

        rooms.add(room1);
        rooms.add(room2);
        rooms.add(room3);

        tenants.add(tenant1);
        tenants.add(tenant2);

        // Assign rooms to tenants
        landlord1.updateTenantRoom("T001", "101");
        landlord1.updateTenantRoom("T002", "102");

        // Main login and navigation
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            int loginResult = login(scanner, landlord1, tenants);

            if (loginResult == 1) {
                landlordMenu(scanner, landlord1);
                // After returning from landlord menu, we'll come back here
            } else if (loginResult == 2) {
                tenantMenu(scanner, tenant1); // You can replace tenant1 with a dynamic tenant
                // After returning from tenant menu, we'll come back here
            } else {
                // User chose to exit
                running = false;
            }
        }

        scanner.close();
        System.out.println("\nExiting... Goodbye!\n");
    }

    private static int login(Scanner scanner, Landlord landlord, List<Tenant> tenants) {
        while (true) {
            System.out.println("\n===== Welcome to the Rental System =====");
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();

            // First check if it's landlord
            if (username.equals(landlord.getName()) && password.equals(landlord.getIdCard())) {
                // Ask for PIN after username and password validation
                System.out.print("Enter your 4-digit PIN: ");
                int pin = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                // Verify the PIN
                if (landlord.verifyPin(pin)) {
                    System.out.println("\nLogin successful as Landlord!\n");
                    return 1; // Landlord login successful
                } else {
                    System.out.println("\nIncorrect PIN! Try again.");
                    continue; // Retry login
                }
            }

            // Then check if it's a tenant
            for (Tenant tenant : tenants) {
                if (tenant.getName().equals(username) && tenant.getIdCard().equals(password)) {
                    System.out.println("\nLogin successful as Tenant: " + tenant.getName() + "!\n");
                    return 2; // Tenant login successful
                }
            }

            // If we reach here, login failed

            System.out.print("Press X to exit\n Press anything else to try again");
            String choice = scanner.nextLine();
            if (choice.equalsIgnoreCase("X")) {
                return 0; // Exit
            }
        }
    }

    // Tenant Menu
    public static void tenantMenu(Scanner scanner, Tenant tenant) {
        while (true) {
            try {
                System.out.println("\n=== Tenant Menu for " + tenant.getName() + " ===");
                System.out.println("1. View Payment History");
                System.out.println("2. Pay Rent");
                System.out.println("3. Pay Utilities");
                System.out.println("4. View Room Billing");
                System.out.println("5. View Tenant Info");
                System.out.println("6. Exit Tenant Menu");
                System.out.print("Choose an option: ");
                int tenantChoice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (tenantChoice) {
                    case 1:
                        tenant.displayPaymentHistory();
                        break;
                    case 2:
                        System.out.print("Enter the amount(riel) to pay for rent: ");
                        double amount = scanner.nextDouble();
                        tenant.payRent(amount);
                        break;
                    case 3:
                        System.out.print("Enter the amount(riel) to pay for utilities: ");
                        amount = scanner.nextDouble();
                        tenant.payUtilities(amount);
                        break;
                    case 4:
                        if (tenant.getAssignedRoom() != null) {
                            tenant.getAssignedRoom().displayRoomBilling();
                        } else {
                            System.out.println("No room assigned.");
                        }
                        break;
                    case 5:
                        tenant.displayTenantInfo();
                        break;
                    case 6:
                        return; // Exit Tenant Menu
                    default:
                        System.out.println("Invalid choice! Please try again.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine(); // Consume invalid input
            }
        }
    }

    // Landlord Menu
    public static void landlordMenu(Scanner scanner, Landlord landlord) {
        while (true) {
            try {
                System.out.println("\n=== Landlord Menu ===");
                System.out.println("1. Add Room");
                System.out.println("2. Remove Room");
                System.out.println("3. Assign Tenant to Room");
                System.out.println("4. View Room Billing");
                System.out.println("5. Set Utility Usage for Room");
                System.out.println("6. Add Tenant");
                System.out.println("7. Remove Tenant");
                System.out.println("8. View Tenant Info");
                System.out.println("9. Exit Landlord Menu");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        // Add Room
                        System.out.print("Enter room number to add: ");
                        String roomNumber = scanner.nextLine();
                        System.out.print("Enter initial electric counter: ");
                        int electricCounter = scanner.nextInt();
                        System.out.print("Enter initial water counter: ");
                        int waterCounter = scanner.nextInt();
                        scanner.nextLine(); // Consume newline
                        Room newRoom = new Room(roomNumber, electricCounter, waterCounter);
                        landlord.addRoom(newRoom);
                        System.out.println("Room added successfully.");
                        break;
                    case 2:
                        // Remove Room
                        System.out.print("Enter room number to remove: ");
                        roomNumber = scanner.nextLine();
                        landlord.removeRoom(roomNumber);
                        System.out.println("Room removed successfully.");
                        break;
                    case 3:
                        // Assign Tenant to Room
                        System.out.print("Enter tenant ID: ");
                        String tenantID = scanner.nextLine();
                        System.out.print("Enter new room number: ");
                        String newRoomNumber = scanner.nextLine();
                        landlord.updateTenantRoom(tenantID, newRoomNumber);
                        break;
                    case 4:
                        // View Room Billing
                        System.out.print("Enter room number to view billing: ");
                        roomNumber = scanner.nextLine();
                        Room room = landlord.getRoomByNumber(roomNumber);
                        if (room != null) {
                            room.displayRoomBilling();
                        } else {
                            System.out.println("Room not found.");
                        }
                        break;
                    case 5:
                        // Set Utility Usage for Room
                        System.out.print("Enter room number to set utility usage: ");
                        roomNumber = scanner.nextLine();
                        System.out.print("Enter electricity usage (kWh): ");
                        int electricUsage = scanner.nextInt();
                        System.out.print("Enter water usage (Liters): ");
                        int waterUsage = scanner.nextInt();
                        Room roomToUpdate = landlord.getRoomByNumber(roomNumber);
                        if (roomToUpdate != null) {
                            landlord.setUtilityUsage(roomToUpdate, electricUsage, waterUsage);
                        } else {
                            System.out.println("Room not found.");
                        }
                        break;
                    case 6:
                        // Add Tenant
                        System.out.print("Enter tenant name: ");
                        String tenantName = scanner.nextLine();
                        System.out.print("Enter tenant ID: ");
                        tenantID = scanner.nextLine();
                        System.out.print("Enter tenant phone number: ");
                        String tenantPhone = scanner.nextLine();
                        Tenant newTenant = new Tenant(tenantName, tenantID, tenantPhone);
                        landlord.addTenant(newTenant);
                        System.out.println("Tenant added successfully.");
                        break;
                    case 7:
                        // Remove Tenant
                        System.out.print("Enter tenant ID to remove: ");
                        tenantID = scanner.nextLine();
                        landlord.removeTenant(tenantID);
                        System.out.println("Tenant removed successfully.");
                        break;
                    case 8:
                        // View Tenant Info
                        System.out.print("Enter tenant ID to view info: ");
                        tenantID = scanner.nextLine();
                        Tenant tenant = landlord.getTenantByID(tenantID);
                        if (tenant != null) {
                            tenant.displayTenantInfo();
                        } else {
                            System.out.println("Tenant not found.");
                        }
                        break;
                    case 9:
                        return; // Exit Landlord Menu
                    default:
                        System.out.println("Invalid choice! Please try again.");
                        break;
                }
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                scanner.nextLine(); // Consume invalid input
            }
        }
    }
}
