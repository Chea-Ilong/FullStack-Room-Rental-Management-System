package Main;

import DataBase.*;
import Exceptions.RoomException;
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

import static Main.Menu.RoomManagement.roomManagementMenu;

public class Menu {

    public static void tenantMenu(Scanner scanner, Tenant tenant, Landlord landlord) throws TenantException, RoomException {
//        // Add at the beginning of tenantMenu method
//        System.out.println("DEBUG: Tenant name: " + tenant.getName());
//        System.out.println("DEBUG: Assigned room: " +
//                (tenant.getAssignedRoom() != null ? tenant.getAssignedRoom().getRoomNumber() : "None"));

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
                case 1: // View Room Information
                    System.out.println("\n===== Room Information =====");
                    Room assignedRoom = tenant.getAssignedRoom();

                    if (assignedRoom != null) {
                        // Get a fresh copy of the room from the database
                        RoomDML roomDML = new RoomDML();
                        Room freshRoom = roomDML.getRoomByRoomNumber(assignedRoom.getRoomNumber());

                        if (freshRoom != null) {
                            // Update the tenant's assigned room information without trying to reassign
                            tenant.updateRoomInformation(freshRoom);
                            System.out.println(freshRoom.toString());
                        } else {
                            System.out.println(assignedRoom.toString());
                        }
                    } else {
                        System.out.println("You don't have an assigned room. Please contact the landlord.");
                    }
                    break;

                case 2:
                    tenant.payRent(scanner);
                    break;
                case 3:
                    if (tenant.getAssignedRoom() != null) {
                        System.out.print("Enter the amount to pay for utilities: ");
                        double utilityAmount = scanner.nextDouble();
                        scanner.nextLine(); // Consume newline
                        tenant.payUtilities(utilityAmount);
                    } else {
                        System.out.println("No room assigned to pay utilities for. Please contact the landlord.");
                    }
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

                BuildingDML buildingDML = new BuildingDML();

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
                        buildingDML.saveBuilding(building);
                        break;

                    case 2:
                        // Remove Building
                        System.out.print("Enter building name to remove: ");
                        buildingName = scanner.nextLine();

                        // Get building ID from database
                        int buildingId = buildingDML.getBuildingIdByName(buildingName);
                        if (buildingId != -1) {
                            // Remove from database
                            buildingDML.deleteBuilding(buildingId);
                            // Remove from landlord object
                            landlord.removeBuilding(buildingName);
                            System.out.println("Building removed from database successfully.");
                        } else {
                            System.out.println("Building not found in database.");
                        }
                        break;

                    case 3:
                        // Update Building Name and Address
                        System.out.print("Enter current building name: ");
                        String oldBuildingName = scanner.nextLine();

                        System.out.print("Enter new building name: ");
                        String newBuildingName = scanner.nextLine();

                        System.out.print("Enter new building address: ");
                        String newBuildingAddress = scanner.nextLine();

                        // Update in database
                        int buildingIdToUpdate = buildingDML.getBuildingIdByName(oldBuildingName);
                        if (buildingIdToUpdate != -1) {
                            Building updatedBuilding = new Building(newBuildingName, newBuildingAddress);
                            buildingDML.updateBuilding(buildingIdToUpdate, updatedBuilding);

                            // Update in landlord object
                            landlord.updateBuilding(oldBuildingName, newBuildingName, newBuildingAddress);
                            System.out.println("Building updated in database successfully.");
                        } else {
                            System.out.println("Building not found in database.");
                        }
                        break;

                    case 4:
                        // View All Buildings
                        System.out.println("\n--- All Buildings (from memory) ---");
                        landlord.displayAllBuildings();

                        // You could also implement a method to display buildings directly from database
                        // For example:
                        // System.out.println("\n--- All Buildings (from database) ---");
                        // List<Building> buildings = buildingDML.getAllBuildings();
                        // for (Building b : buildings) {
                        //     System.out.println("Name: " + b.getBuildingName() + ", Address: " + b.getAddress());
                        // }
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
        FloorDML floorDML = new FloorDML();
        BuildingDML buildingDML = new BuildingDML(); // Assuming there's a BuildingDML class

        while (true) {
            try {
                System.out.println("\n=== Floor Management ===");
                System.out.println("1. Add Floor to Building");
                System.out.println("2. Remove Floor from Building");
                System.out.println("3. View Floors in Building");
                System.out.println("4. Update Floor Information");
                System.out.println("5. Back to Main Menu");
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
                            // Get building ID from database
                            int buildingId = buildingDML.getBuildingIdByName(building.getName());
                            if (buildingId != -1) {
                                // Create and save the floor using FloorDML
                                Properties.Floor newFloor = new Properties.Floor(floorNumber);
                                boolean success = floorDML.saveFloor(newFloor, buildingId);

                                if (success) {
                                    // Also update the in-memory model
                                    landlord.addFloorToBuilding(buildingName, newFloor);
                                }
                            } else {
                                System.out.println("Building not found in database.");
                            }
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

                        building = landlord.getBuildingByName(buildingName);
                        if (building != null) {
                            // Get building ID from database
                            int buildingId = buildingDML.getBuildingIdByName(building.getName());
                            if (buildingId != -1) {
                                // Delete from database
                                boolean success = floorDML.deleteFloorByBuildingAndNumber(buildingId, floorNumber);

                                if (success) {
                                    // Also update the in-memory model
                                    landlord.removeFloorFromBuilding(buildingName, floorNumber);
                                }
                            } else {
                                System.out.println("Building not found in database.");
                            }
                        } else {
                            System.out.println("Building not found.");
                        }
                        break;

                    case 3:
                        // View Floors in Building
                        System.out.print("Enter building name: ");
                        buildingName = scanner.nextLine();

                        building = landlord.getBuildingByName(buildingName);
                        if (building != null) {
                            // Get building ID from database
                            int buildingId = buildingDML.getBuildingIdByName(building.getName());
                            if (buildingId != -1) {
                                // Get floors from database
                                List<Properties.Floor> floors = floorDML.getFloorsByBuildingId(buildingId);

                                if (floors.isEmpty()) {
                                    System.out.println("No floors found for this building.");
                                } else {
                                    System.out.println("Floors in Building " + building.getName() + ":");
                                    for (Properties.Floor floor : floors) {
                                        System.out.println("- Floor: " + floor.getFloorNumber() +
                                                " (Rooms: " +
                                                (floor.getRooms() != null ? floor.getRooms().size() : 0) +
                                                ")");
                                    }
                                }
                            } else {
                                // Fallback to in-memory display if not in database
                                building.displayAllFloors();
                            }
                        } else {
                            System.out.println("Building not found.");
                        }
                        break;

                    case 4:
                        // Update Floor Information
                        System.out.print("Enter building name: ");
                        buildingName = scanner.nextLine();
                        System.out.print("Enter current floor number: ");
                        String currentFloorNumber = scanner.nextLine();
                        System.out.print("Enter new floor number: ");
                        String newFloorNumber = scanner.nextLine();

                        building = landlord.getBuildingByName(buildingName);
                        if (building != null) {
                            // Get building ID and floor ID from database
                            int buildingId = buildingDML.getBuildingIdByName(building.getName());
                            if (buildingId != -1) {
                                int floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, currentFloorNumber);

                                if (floorId != -1) {
                                    // Update in database
                                    Properties.Floor updatedFloor = new Properties.Floor(newFloorNumber);
                                    boolean success = floorDML.updateFloor(floorId, updatedFloor);

                                    if (success) {
                                        System.out.println("Floor updated successfully in database.");
                                        // You might want to update the in-memory model as well
                                        // This would need a method in the Landlord class to update a floor
                                    } else {
                                        System.out.println("Failed to update floor in database.");
                                    }
                                } else {
                                    System.out.println("Floor not found in database.");
                                }
                            } else {
                                System.out.println("Building not found in database.");
                            }
                        } else {
                            System.out.println("Building not found.");
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

    public class RoomManagement {

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

                            BuildingDML buildingDML = new BuildingDML();
                            FloorDML floorDML = new FloorDML();
                            RoomDML roomDML = new RoomDML();

                            int buildingId = buildingDML.getBuildingIdByName(buildingName);
                            if (buildingId == -1) {
                                System.out.println("Building not found. Creating new building...");
                                System.out.print("Enter building address: ");
                                String address = scanner.nextLine();
                                Building newBuilding = new Building(buildingName, address);
                                buildingDML.saveBuilding(newBuilding);
                                buildingId = buildingDML.getBuildingIdByName(buildingName);
                            }

                            int floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, floorNumber);
                            if (floorId == -1) {
                                System.out.println("Floor not found. Creating new floor...");
                                Floor newFloor = new Floor(floorNumber);
                                floorDML.saveFloor(newFloor, buildingId);
                                floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, floorNumber);
                            }

                            Room newRoom = new Room(roomNumber, electricCounter, waterCounter);
                            newRoom.setRent(rent);
                            newRoom.setUtilityUsage(electricCounter, waterCounter, LocalDate.now());

                            roomDML.saveRoom(newRoom, floorId);
                            System.out.println("Room added successfully to database.");
                            break;

                        case 2:
                            // Remove Room from Floor
                            System.out.print("Enter room number to remove: ");
                            roomNumber = scanner.nextLine();

                            roomDML = new RoomDML();
                            int roomId = roomDML.getRoomIdByRoomNumber(roomNumber);

                            if (roomId != -1) {
                                roomDML.deleteRoom(roomId);
                                System.out.println("Room removed successfully.");
                            } else {
                                System.out.println("Room not found.");
                            }
                            break;

                        case 3:
                            // View Room Details
                            System.out.print("Enter room number: ");
                            roomNumber = scanner.nextLine();

                            Room room = landlord.getRoomAcrossAllBuildings(roomNumber);
                            if (room != null) {
                                System.out.println(room.toString());
                            } else {
                                System.out.println("Room not found.");
                            }
                            break;

                        case 4:
                            // View All Rooms (across all buildings)
                            for (Building building : landlord.getBuildings()) {
                                for (Floor floor : building.getFloors()) {
                                    floor.displayAllRooms();
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

                        TenantDML tenantDML = new TenantDML();
                        if (!tenantDML.tenantExists(tenantID)) {
                            Tenant newTenant = new Tenant(tenantName, tenantID, tenantContact);
                            tenantDML.saveTenant(newTenant);
                            landlord.addTenant(newTenant);
                        } else {
                            System.out.println("Error: Tenant with ID " + tenantID + " already exists.");
                        }
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

                        LandlordDML landlordDML = new LandlordDML();
                        landlordDML.assignRoomToTenant(tenantID, roomNumber);
                        break;
                    case 4:
                        // View Tenant Details
                        System.out.print("Enter tenant ID: ");
                        tenantID = scanner.nextLine();

                        Tenant tenant = landlord.getTenantByID(tenantID);
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

//     Utility Management Submenu
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
                    System.out.print("Enter Room Number: ");
                    String roomNumber = scanner.nextLine().trim();

// Instead of using landlord.getRoomAcrossAllBuildings, use the database directly
                    RoomDML roomDML = new RoomDML();
                    Room room = roomDML.getRoomByRoomNumber(roomNumber);

                    if (room == null) {
                        System.out.println("Room not found in database.");
                        return;
                    }

                    System.out.print("Enter Electric Usage: ");
                    int electricUsage;
                    try {
                        electricUsage = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Electric usage must be a number.");
                        return;
                    }

                    System.out.print("Enter Water Usage: ");
                    int waterUsage;
                    try {
                        waterUsage = Integer.parseInt(scanner.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Water usage must be a number.");
                        return;
                    }

                    try {
                        // Use the direct method to set utility usage
                        roomDML.setUtilityUsageDirectly(roomNumber, electricUsage, waterUsage);
                        System.out.println("Utility usage for Room " + roomNumber + " has been set.");

                        // If you still need to update the in-memory model:
                        room.setUtilityUsage(electricUsage, waterUsage, LocalDate.now());
                    } catch (Exception e) {
                        System.out.println("Error setting utility usage: " + e.getMessage());
                    }
                    break;

                case 2:
                    // View Utility Usage
                    System.out.print("Enter Room Number: ");
                    roomNumber = scanner.nextLine(); // Reuse roomNumber variable

                    room = landlord.getRoomAcrossAllBuildings(roomNumber);
                    if (room == null) {
                        System.out.println("Room not found.");
                        break; // Don't return, continue the loop
                    }

                    UtilityUsage usage = room.getUtilityUsage();
                    if (usage == null) {
                        System.out.println("No utility data available for Room " + roomNumber);
                    } else {
                        System.out.println("Room " + roomNumber + " utility usage:");
                        System.out.println("Date: " + usage.getDate());
                        System.out.println("Electric Usage: " + usage.getElectricUsage() + " kWh");
                        System.out.println("Water Usage: " + usage.getWaterUsage() + " cubic meters");

                        // Calculate costs
                        double electricCost = usage.getElectricUsage() * Room.getElectricRate();
                        double waterCost = usage.getWaterUsage() * Room.getWaterRate();
                        double totalCost = electricCost + waterCost;

                        System.out.println("Electric Cost: " + String.format("%.0f KHR", electricCost));
                        System.out.println("Water Cost: " + String.format("%.0f KHR", waterCost));
                        System.out.println("Total Cost: " + String.format("%.0f KHR", totalCost));
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
