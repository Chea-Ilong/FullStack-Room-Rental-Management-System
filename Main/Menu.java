package Main;

import DataBase.*;
import Exceptions.TenantException;
import Payment.UtilityUsage;
import Properties.Building;
import Properties.Floor;
import Properties.Room;
import Users.Landlord;
import Users.Tenant;


import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Menu {

    // Tenant Menu
    public static void tenantMenu(Scanner scanner, Tenant tenant, Landlord landlord) throws TenantException {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("\n===== Tenant Menu =====");
            System.out.println("1. Display Room Information");
            System.out.println("2. Pay Rent");
            System.out.println("3. Pay Utilities");
            System.out.println("4. Display Payment History");
            System.out.println("5. Logout");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    if (tenant.getAssignedRoom() != null) {
                        System.out.println(tenant.getAssignedRoom().toString());
                    } else {
                        System.out.println("No room assigned to display billing.");
                    }
                    break;
                case 2:
                    tenant.payRent(scanner);
                    break;
                case 3:
                    System.out.print("Enter the amount to pay for utilities: ");
                    double utilityAmount = scanner.nextDouble();
                    scanner.nextLine(); // Consume newline
                    tenant.payUtilities(utilityAmount);
                    break;
                case 4:
                    tenant.displayPaymentHistory();
                    break;
                case 5:
                    inMenu = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }


    // Landlord Menu
    public static void landlordMenu(Scanner scanner, Landlord landlord) {
        while (true) {
            try {
                System.out.println("\n=== Landlord Menu ===");
                System.out.println("1. Building Management");
                System.out.println("2. Floor Management");
                System.out.println("3. Room Management");
                System.out.println("4. Tenant Management");
                System.out.println("5. Utility Management");
                System.out.println("6. View Reports");
                System.out.println("7. Logout");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        // Building Management
                        buildingManagementMenu(scanner, landlord);
                        break;
                    case 2:
                        // Floor Management
                        floorManagementMenu(scanner, landlord);
                        break;
                    case 3:
                        // Room Management
                        roomManagementMenu(scanner, landlord);
                        break;
                    case 4:
                        // Tenant Management
                        tenantManagementMenu(scanner, landlord);
                        break;
                    case 5:
                        // Utility Management
                        utilityManagementMenu(scanner, landlord);
                        break;
                    case 6:
                        // View Reports
                        reportsMenu(scanner, landlord);
                        break;
                    case 7:
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
    // Building Management Submenu
    static void buildingManagementMenu(Scanner scanner, Landlord landlord) {
        while (true) {
            try {
                System.out.println("\n=== Building Management ===");
                System.out.println("1. Add Building");
                System.out.println("2. Remove Building");
                System.out.println("3. Update Building Name");
                System.out.println("4. View All Buildings");
                System.out.println("5. Back to Main Menu");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        // Add Building
                        System.out.print("Enter building name: ");
                        String buildingName = scanner.nextLine();

                        System.out.print("Enter building address: ");
                        String buildingAddress = scanner.nextLine();

                        Building building = new Building(buildingName, buildingAddress);
                        landlord.addBuilding(building);

                        // Save to database
                        BuildingDML buildingDML = new BuildingDML();
                        buildingDML.saveBuilding(building);
                        break;

                    case 2:
                        // Remove Building
                        System.out.print("Enter building name to remove: ");
                        buildingName = scanner.nextLine();
                        landlord.removeBuilding(buildingName);
                        break;

                    case 3:
                        // Update Building Name and Address
                        System.out.print("Enter current building name: ");
                        String oldBuildingName = scanner.nextLine();

                        System.out.print("Enter new building name: ");
                        String newBuildingName = scanner.nextLine();

                        System.out.print("Enter new building address: ");
                        String newBuildingAddress = scanner.nextLine();

                        landlord.updateBuilding(oldBuildingName, newBuildingName, newBuildingAddress);
                        break;

                    case 4:
                        // View All Buildings
                        landlord.displayAllBuildings();
                        break;

                    case 5:
                        return; // Back to main menu

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

    // Floor Management Submenu
    static void floorManagementMenu(Scanner scanner, Landlord landlord) {
        while (true) {
            try {
                System.out.println("\n=== Floor Management ===");
                System.out.println("1. Add Floor to Building");
                System.out.println("2. Remove Floor from Building");
                System.out.println("3. View Floors in Building");
                System.out.println("4. Back to Main Menu");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        // Add Floor to Building
                        System.out.print("Enter building name: ");
                        String buildingName = scanner.nextLine();
                        System.out.print("Enter floor number: ");
                        String floorNumber = scanner.nextLine();

                        Properties.Building building = landlord.getBuildingByName(buildingName);
                        if (building != null) {
                            landlord.addFloorToBuilding(buildingName, new Properties.Floor(floorNumber));
                        } else {
                            System.out.println("Building not found.");
                        }
                        break;
                    case 2:
                        // Remove Floor from Building
                        System.out.print("Enter building name: ");
                        buildingName = scanner.nextLine();
                        System.out.print("Enter floor number to remove: ");
                        floorNumber = scanner.nextLine();

                        landlord.removeFloorFromBuilding(buildingName, floorNumber);
                        break;
                    case 3:
                        // View Floors in Building
                        System.out.print("Enter building name: ");
                        buildingName = scanner.nextLine();

                        building = landlord.getBuildingByName(buildingName);
                        if (building != null) {
                            building.displayAllFloors();
                        } else {
                            System.out.println("Building not found.");
                        }
                        break;
                    case 4:
                        return; // Back to main menu
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

    // Room Management Submenu
    static void roomManagementMenu(Scanner scanner, Landlord landlord) {
        while (true) {
            try {
                System.out.println("\n=== Room Management ===");
                System.out.println("1. Add Room to Floor");
                System.out.println("2. Remove Room from Floor");
                System.out.println("3. View Room Details");
                System.out.println("4. View All Rooms");
                System.out.println("5. Back to Main Menu");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        // Add Room to Floor
                        System.out.print("Enter building name: ");
                        String buildingName = scanner.nextLine();
                        System.out.print("Enter floor number: ");
                        String floorNumber = scanner.nextLine();
                        System.out.print("Enter room number: ");
                        String roomNumber = scanner.nextLine();
                        System.out.print("Enter rent amount: ");
                        double rent = scanner.nextDouble();
                        System.out.print("Enter initial electric counter: ");
                        int electricCounter = scanner.nextInt();
                        System.out.print("Enter initial water counter: ");
                        int waterCounter = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        // Check if building exists in database
                        BuildingDML buildingDML = new BuildingDML();
                        int buildingId = buildingDML.getBuildingIdByName(buildingName);

                        if (buildingId == -1) {
                            // Building doesn't exist, create it
                            System.out.println("Building not found. Creating new building...");
                            System.out.print("Enter building address: ");
                            String address = scanner.nextLine();

                            Building newBuilding = new Building(buildingName, address);
                            buildingDML.saveBuilding(newBuilding);

                            // Get the newly created building's ID
                            buildingId = buildingDML.getBuildingIdByName(buildingName);
                        }

                        // Check if floor exists in database
                        FloorDML floorDML = new FloorDML();
                        int floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, floorNumber);

                        if (floorId == -1) {
                            // Floor doesn't exist, create it
                            System.out.println("Floor not found. Creating new floor...");

                            Floor newFloor = new Floor(floorNumber);
                            floorDML.saveFloor(newFloor, buildingId);

                            // Get the newly created floor's ID
                            floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, floorNumber);
                        }

                        // Create and add room to database
                        Room newRoom = new Room(roomNumber, electricCounter, waterCounter);
                        newRoom.setRent(rent);

                        // Set utility usage with today's date to ensure it's properly initialized
                        LocalDate today = LocalDate.now();
                        newRoom.setUtilityUsage(electricCounter, waterCounter, today);

                        RoomDML roomDML = new RoomDML();
                        roomDML.saveRoom(newRoom, floorId);

                        // Additionally, you can explicitly save the utility usage
                        if (floorId != -1) {
                            int roomId = roomDML.getRoomIdByRoomNumber(roomNumber);
                            if (roomId != -1) {
                                UtilityUsageDML utilityDML = new UtilityUsageDML();
                                UtilityUsage usage = new UtilityUsage(electricCounter, waterCounter, today);
                                utilityDML.saveUtilityUsage(usage, roomId);
                            }
                        }

                        System.out.println("Room added successfully to database.");
                        break;
                    case 2:
                        // Remove Room from Floor
//                        System.out.print("Enter building name: ");
//                        buildingName = scanner.nextLine();
//                        System.out.print("Enter floor number: ");
//                        floorNumber = scanner.nextLine();
//                        System.out.print("Enter room number to remove: ");
//                        roomNumber = scanner.nextLine();
//
//                        building = landlord.getBuildingByName(buildingName);
//                        if (building != null) {
//                            floor = building.getFloorByNumber(floorNumber);
//                            if (floor != null) {
//                                floor.removeRoom(roomNumber);
//                            } else {
//                                System.out.println("Floor not found.");
//                            }
//                        } else {
//                            System.out.println("Building not found.");
//                        }
                        break;
                    case 3:
                        // View Room Details
                        System.out.print("Enter room number: ");
                        roomNumber = scanner.nextLine();

                        Properties.Room room = landlord.getRoomAcrossAllBuildings(roomNumber);
                        if (room != null) {
                            System.out.println(room.toString());
                        } else {
                            System.out.println("Room not found.");
                        }
                        break;
                    case 4:
                        // View All Rooms (across all buildings)
                        for (Properties.Building b : landlord.getBuildings()) {
                            for (Properties.Floor f : b.getFloors()) {
                                f.displayAllRooms();
                            }
                        }
                        break;
                    case 5:
                        return; // Back to main menu
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

    // Tenant Management Submenu
    static void tenantManagementMenu(Scanner scanner, Landlord landlord) {
        while (true) {
            try {
                System.out.println("\n=== Tenant Management ===");
                System.out.println("1. Add Tenant");
                System.out.println("2. Remove Tenant");
                System.out.println("3. Assign Tenant to Room");
                System.out.println("4. View Tenant Details");
                System.out.println("5. View All Tenants");
                System.out.println("6. Back to Main Menu");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        // Add Tenant
                        System.out.print("Enter tenant name: ");
                        String tenantName = scanner.nextLine();
                        System.out.print("Enter tenant ID: ");
                        String tenantID = scanner.nextLine();
                        System.out.print("Enter tenant contact: ");
                        String tenantContact = scanner.nextLine();

                        Users.Tenant newTenant = new Users.Tenant(tenantName, tenantID, tenantContact);
                        TenantDML tenantDML = new TenantDML();
                        tenantDML.saveTenant(newTenant);
                        landlord.addTenant(newTenant);
                        break;
                    case 2:
                        // Remove Tenant
                        System.out.print("Enter tenant ID to remove: ");
                        tenantID = scanner.nextLine();
                        landlord.removeTenant(tenantID);
                        break;
                    case 3:
                        // Assign tenant to room
                        System.out.print("Enter tenant ID: ");
                        tenantID = scanner.nextLine();
                        System.out.print("Enter room number: ");
                        String roomNumber = scanner.nextLine();

                        landlord.assignedTenantRoom(tenantID, roomNumber);
                        break;
                    case 4:
                        // View Tenant Details
                        System.out.print("Enter tenant ID: ");
                        tenantID = scanner.nextLine();

                        Users.Tenant tenant = landlord.getTenantByID(tenantID);
                        if (tenant != null) {
                            System.out.println(tenant.toString());
                        } else {
                            System.out.println("Tenant not found.");
                        }
                        break;
                    case 5:
                        // View All Tenants
                        landlord.displayAllTenants();
                        break;
                    case 6:
                        return; // Back to main menu
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

    // Utility Management Submenu
    static void utilityManagementMenu(Scanner scanner, Landlord landlord) {
        while (true) {
            try {
                System.out.println("\n=== Utility Management ===");
                System.out.println("1. Set Utility Usage for Room");
                System.out.println("2. View Utility Usage");
                System.out.println("3. Back to Main Menu");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        // Set Utility Usage for Room
                        System.out.print("Enter room number: ");
                        String roomNumber = scanner.nextLine();
                        System.out.print("Enter electric usage: ");
                        int electricUsage = scanner.nextInt();
                        System.out.print("Enter water usage: ");
                        int waterUsage = scanner.nextInt();
                        scanner.nextLine(); // Consume newline

                        Properties.Room room = landlord.getRoomAcrossAllBuildings(roomNumber);
                        if (room != null) {
                            landlord.setUtilityUsage(room, electricUsage, waterUsage);
                        } else {
                            System.out.println("Room not found.");
                        }
                        break;
                    case 2:
                        // View Utility Usage
                        System.out.print("Enter room number: ");
                        roomNumber = scanner.nextLine();
                        System.out.print("Enter date (YYYY-MM-DD): ");
                        String dateStr = scanner.nextLine();
                        java.time.LocalDate date = java.time.LocalDate.parse(dateStr);

                        room = landlord.getRoomAcrossAllBuildings(roomNumber);
                        if (room != null) {
                            Payment.UtilityUsage usage = landlord.getUtilityUsageForRoom(room, date);
                            if (usage != null) {
                                System.out.println("Utility usage for Room " + roomNumber + " on " + date + ":");
                                System.out.println("Electric usage: " + usage.getElectricUsage() + " kWh");
                                System.out.println("Water usage: " + usage.getWaterUsage() + " m³");
                            } else {
                                System.out.println("No utility data available for this room on " + date);
                            }
                        } else {
                            System.out.println("Room not found.");
                        }
                        break;
                    case 3:
                        return; // Back to main menu
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

    // Reports Menu
    static void reportsMenu(Scanner scanner, Landlord landlord) {
        while (true) {
            try {
                System.out.println("\n=== Reports ===");
                System.out.println("1. View All Buildings");
                System.out.println("2. View All Tenants");
                System.out.println("3. View Available Rooms");
                System.out.println("4. View Occupied Rooms");
                System.out.println("5. Back to Main Menu");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        // View All Buildings
                        landlord.displayAllBuildings();
                        break;
                    case 2:
                        // View All Tenants
                        landlord.displayAllTenants();
                        break;
                    case 3:
                        // View Available Rooms
                        System.out.println("\n========= Available Rooms =========");
                        boolean foundAvailable = false;
                        for (Properties.Building building : landlord.getBuildings()) {
                            for (Properties.Floor floor : building.getFloors()) {
                                List<Room> availableRooms = floor.getAvailableRooms();
                                if (!availableRooms.isEmpty()) {
                                    foundAvailable = true;
                                    System.out.println("Building: " + building.getBuildingName() + " ,Address: "  + building.getAddress() + " , Floor: " + floor.getFloorNumber());
                                    for (Properties.Room room : availableRooms) {
                                        System.out.println("  Room: " + room.getRoomNumber());
                                    }
                                }
                            }
                        }
                        if (!foundAvailable) {
                            System.out.println("No available rooms found.");
                        }
                        break;
                    case 4:
                        // View Occupied Rooms
                        System.out.println("\n===== Occupied Rooms =====");
                        boolean foundOccupied = false;
                        for (Properties.Building building : landlord.getBuildings()) {
                            for (Properties.Floor floor : building.getFloors()) {
                                List<Properties.Room> occupiedRooms = floor.getOccupiedRooms();
                                if (!occupiedRooms.isEmpty()) {
                                    foundOccupied = true;
                                    System.out.println("Building: " + building.getBuildingName() + " ,Address: "  + building.getAddress() + " , Floor: " + floor.getFloorNumber());
                                    for (Properties.Room room : occupiedRooms) {
                                        System.out.println("  Room: " + room.getRoomNumber() + ", Tenant: " + room.getTenant().getName());
                                    }
                                }
                            }
                        }
                        if (!foundOccupied) {
                            System.out.println("No occupied rooms found.");
                        }
                        break;
                    case 5:
                        return; // Back to main menu
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
