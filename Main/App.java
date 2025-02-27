package Main;

import Properties.Floor;
import Properties.Room;
import Users.Landlord;
import Users.Tenant;

import java.util.Scanner;

public class App {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Create a sample landlord (with predefined login details)
        Landlord landlord = new Landlord("admin", "password123", "0987654321");

        boolean loggedIn = false;

        while (!loggedIn) {
            System.out.println("\n===== Welcome to the Rental System =====");
            System.out.print("Enter your username: ");
            String username = scanner.nextLine();
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();

            if (username.equals(landlord.getUsername()) && password.equals(landlord.getPassword())) {
                loggedIn = true;
                System.out.println("\nLogin successful as Users.Landlord!\n");

                while (true) {
                    System.out.println("===== Main Menu =====");
                    System.out.println("1. Users.Tenant Operations");
                    System.out.println("2. Properties.Floor Operations");
                    System.out.println("3. Properties.Room Operations");
                    System.out.println("4. View All Tenants");
                    System.out.println("5. View All Floors");
                    System.out.println("6. Exit");
                    System.out.print("\nChoose an option: ");

                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    if (choice == 1) {
                        while (true) {
                            System.out.println("\n===== Users.Tenant Operations =====");
                            System.out.println("1. Add Users.Tenant");
                            System.out.println("2. Remove Users.Tenant");
                            System.out.println("3. View Users.Tenant Details");
                            System.out.println("4. Go Back");
                            System.out.print("\nChoose an option: ");
                            int tenantChoice = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            if (tenantChoice == 1) {
                                System.out.print("\nEnter tenant username: ");
                                String tenantUsername = scanner.nextLine();
                                System.out.print("Enter tenant password (ID Card): ");
                                String tenantPassword = scanner.nextLine();
                                System.out.print("Enter tenant phone number: ");
                                String tenantPhone = scanner.nextLine();
                                Tenant newTenant = new Tenant(tenantUsername, tenantPhone, tenantPassword);
                                System.out.print("Enter room ID to assign tenant to: ");
                                String roomId = scanner.nextLine();
                                landlord.addTenantToRoom(roomId, newTenant);
                                System.out.println("\nUsers.Tenant added successfully!\n");
                            } else if (tenantChoice == 2) {
                                System.out.print("\nEnter room ID to remove tenant from: ");
                                String roomId = scanner.nextLine();
                                landlord.removeTenantFromRoom(roomId);
                                System.out.println("\nUsers.Tenant removed successfully!\n");
                            } else if (tenantChoice == 3) {
                                System.out.print("\nEnter tenant username to view details: ");
                                String tenantUsername = scanner.nextLine();
                                Tenant tenant = findTenantByUsername(tenantUsername);
                                if (tenant != null) {
                                    tenant.viewDetails();
                                } else {
                                    System.out.println("\nUsers.Tenant not found.\n");
                                }
                            } else if (tenantChoice == 4) {
                                break;
                            } else {
                                System.out.println("\nInvalid option. Please try again.\n");
                            }
                        }
                    } else if (choice == 2) {
                        while (true) {
                            System.out.println("\n===== Properties.Floor Operations =====");
                            System.out.println("1. Add Properties.Floor");
                            System.out.println("2. Remove Properties.Floor");
                            System.out.println("3. View Properties.Floor Details");
                            System.out.println("4. Go Back");
                            System.out.print("\nChoose an option: ");
                            int floorChoice = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            if (floorChoice == 1) {
                                System.out.print("\nEnter floor number to add: ");
                                int floorNumber = scanner.nextInt();
                                landlord.addFloor(floorNumber);
                                System.out.println("\nProperties.Floor added successfully!\n");
                            } else if (floorChoice == 2) {
                                System.out.print("\nEnter floor number to remove: ");
                                int floorNumber = scanner.nextInt();
                                landlord.removeFloor(floorNumber);
                                System.out.println("\nProperties.Floor removed successfully!\n");
                            } else if (floorChoice == 3) {
                                System.out.print("\nEnter floor number to view details: ");
                                int floorNumber = scanner.nextInt();
                                Floor floor = landlord.getFloor(floorNumber);
                                if (floor != null) {
                                    System.out.println("\nProperties.Floor " + floorNumber + " Details:");
                                    floor.getRooms().forEach(room -> System.out.println("  - Properties.Room: " + room.getRoomID()));
                                    System.out.println();
                                } else {
                                    System.out.println("\nProperties.Floor not found.\n");
                                }
                            } else if (floorChoice == 4) {
                                break;
                            } else {
                                System.out.println("\nInvalid option. Please try again.\n");
                            }
                        }
                    } else if (choice == 3) {
                        while (true) {
                            System.out.println("\n===== Properties.Room Operations =====");
                            System.out.println("1. Add Properties.Room to Properties.Floor");
                            System.out.println("2. Remove Properties.Room from Properties.Floor");
                            System.out.println("3. View Properties.Room Details");
                            System.out.println("4. Go Back");
                            System.out.print("\nChoose an option: ");
                            int roomChoice = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            if (roomChoice == 1) {
                                System.out.print("\nEnter room ID: ");
                                String roomId = scanner.nextLine();
                                System.out.print("Enter room type (SMALL, MEDIUM, LARGE): ");
                                String roomType = scanner.nextLine();
                                Room newRoom = new Room(roomId, roomType, false, 0, 0);
                                System.out.print("Enter floor number to add the room to: ");
                                int floorNumber = scanner.nextInt();
                                landlord.addRoomToFloor(floorNumber, newRoom);
                                System.out.println("\nProperties.Room added successfully!\n");
                            } else if (roomChoice == 2) {
                                System.out.print("\nEnter room ID to remove: ");
                                String roomId = scanner.nextLine();
                                System.out.print("Enter floor number: ");
                                int floorNumber = scanner.nextInt();
                                landlord.removeRoomFromFloor(floorNumber, roomId);
                                System.out.println("\nProperties.Room removed successfully!\n");
                            } else if (roomChoice == 3) {
                                System.out.print("\nEnter room ID to view details: ");
                                String roomId = scanner.nextLine();
                                Room room = landlord.searchRoom(roomId);
                                if (room != null) {
                                    room.displayRoomBilling();
                                } else {
                                    System.out.println("\nProperties.Room not found.\n");
                                }
                            } else if (roomChoice == 4) {
                                break;
                            } else {
                                System.out.println("\nInvalid option. Please try again.\n");
                            }
                        }
                    } else if (choice == 4) {
                        System.out.println("\n===== All Tenants =====");
                        for (Tenant tenant : Tenant.getTenantList()) {
                            tenant.viewDetails();
                        }
                    } else if (choice == 5) {
                        System.out.println("\n===== All Floors =====");
                        for (Floor floor : landlord.getFloors()) {
                            System.out.println("\nProperties.Floor " + floor.getFloorNumber());
                            for (Room room : floor.getRooms()) {
                                System.out.println("  - Properties.Room: " + room.getRoomID());
                            }
                        }
                    } else if (choice == 6) {
                        System.out.println("\nExiting... Goodbye!\n");
                        break;
                    } else {
                        System.out.println("\nInvalid option. Please try again.\n");
                    }
                }
            } else {
                System.out.println("\nInvalid login. Please try again.\n");
            }
        }

        scanner.close();
    }

    // Helper method to find tenant by username
    public static Tenant findTenantByUsername(String username) {
        for (Tenant tenant : Tenant.getTenantList()) {
            if (tenant.getUsername().equals(username)) {
                return tenant;
            }
        }
        return null;
    }
}
