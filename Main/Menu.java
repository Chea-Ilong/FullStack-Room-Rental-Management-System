package Main;

import DataBase.*;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Payment.Bill;
import Payment.BillRecord;
import Properties.Building;
import Properties.Floor;
import Properties.Room;
import Users.Landlord;
import Users.Tenant;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import static Main.Menu.RoomManagement.roomManagementMenu;

public class Menu {

    public static void tenantMenu(Scanner scanner, Tenant tenant, Landlord landlord) throws RoomException, TenantException {
        boolean running = true;

        while (running) {
            System.out.println("\n===== Tenant Menu =====");
            System.out.println("1. Pay Bill");
            System.out.println("2. Logout");
//            System.out.println("3. Logout");
            System.out.print("Choose an option: ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        if (tenant.getAssignedRoom() == null) {
                            System.out.println("You are not assigned to any room.");
                        } else {
                            // Get current month and year
                            LocalDate today = LocalDate.now();
                            int currentYear = today.getYear();
                            int currentMonth = today.getMonthValue();

                            // Check if tenant already paid for current month
                            if (tenant.isBillPaid(today)) {
                                System.out.println("You have already paid your bill for this month.");
                            } else {
                                // Use a PrintStream to suppress unwanted output
                                PrintStream originalOut = System.out;
                                ByteArrayOutputStream devNull = new ByteArrayOutputStream();
                                PrintStream nullPrintStream = new PrintStream(devNull);

                                // Temporarily redirect System.out to suppress messages
                                System.setOut(nullPrintStream);

                                // Find bills for this tenant (output will be suppressed)
                                List<Bill> tenantBills = landlord.getBillRecord().getBillHistoryForTenant(tenant.getIdCard());

                                // Restore original System.out
                                System.setOut(originalOut);

                                List<Bill> unpaidBills = tenantBills.stream()
                                        .filter(bill -> !bill.isPaid())
                                        .collect(Collectors.toList());

                                if (unpaidBills.isEmpty()) {
                                    System.out.println("You have no pending bills to pay.");
                                } else {
                                    // Clear previous console output with ANSI escape codes
                                    System.out.print("\033[H\033[2J");
                                    System.out.flush();

                                    System.out.println("\n===== Your Unpaid Bills =====");
                                    for (int i = 0; i < unpaidBills.size(); i++) {
                                        Bill bill = unpaidBills.get(i);
                                        System.out.printf("%d. Bill ID: %d - Date: %s - Amount: %.0f KHR (%.2f USD)\n",
                                                i + 1,
                                                bill.getBillID(),
                                                bill.getBillDate(),
                                                bill.getTotalAmount(),
                                                bill.getTotalAmount() / 4100.00);
                                    }

                                    System.out.print("\nSelect a bill to pay (1-" + unpaidBills.size() + ") or 0 to cancel: ");
                                    if (scanner.hasNextInt()) {
                                        int selection = scanner.nextInt();
                                        scanner.nextLine(); // Consume newline

                                        if (selection == 0) {
                                            break;
                                        }

                                        if (selection >= 1 && selection <= unpaidBills.size()) {
                                            Bill selectedBill = unpaidBills.get(selection - 1);

                                            System.out.println("\n" + selectedBill);
                                            System.out.print("Confirm payment (Y/N)? ");
                                            String confirm = scanner.nextLine().trim().toUpperCase();

                                            // In the tenant menu case for bill payment
                                            if (confirm.equals("Y")) {
                                                try {
                                                    // Redirect System.out to suppress messages during these operations
                                                    System.setOut(nullPrintStream);

                                                    // Mark bill as paid in memory (output suppressed)
                                                    selectedBill.markAsPaid(selectedBill.getTotalAmount());
                                                    tenant.markBillAsPaid(selectedBill.getBillDate());

                                                    // Restore original System.out
                                                    System.setOut(originalOut);

                                                    // Get tenant and landlord database IDs
                                                    int tenantDbId = 0;
                                                    int landlordDbId = 0;

                                                    // Create connection
                                                    Connection conn = DataBaseConnection.getConnection();

                                                    try {
                                                        // Get tenant ID from database using IdCard
                                                        String tenantQuery = "SELECT tenant_id FROM Tenants t JOIN Users u ON t.user_id = u.user_id WHERE u.IdCard = ?";
                                                        PreparedStatement tenantStmt = conn.prepareStatement(tenantQuery);
                                                        tenantStmt.setString(1, tenant.getIdCard());
                                                        ResultSet tenantRs = tenantStmt.executeQuery();
                                                        if (tenantRs.next()) {
                                                            tenantDbId = tenantRs.getInt("tenant_id");
                                                        } else {
                                                            throw new SQLException("Tenant not found in database");
                                                        }

                                                        // Get landlord ID from database
                                                        String landlordQuery = "SELECT landlord_id FROM Landlords WHERE user_id = ?";
                                                        PreparedStatement landlordStmt = conn.prepareStatement(landlordQuery);
                                                        landlordStmt.setInt(1, 1); // Assuming landlord has a userId
                                                        ResultSet landlordRs = landlordStmt.executeQuery();
                                                        if (landlordRs.next()) {
                                                            landlordDbId = landlordRs.getInt("landlord_id");
                                                        } else {
                                                            throw new SQLException("Landlord not found in database");
                                                        }

                                                        // Close resources
                                                        tenantRs.close();
                                                        tenantStmt.close();
                                                        landlordRs.close();
                                                        landlordStmt.close();

                                                        // Use the existing recordPayment method for recording the payment
                                                        BillRecordDML billRecordDML = new BillRecordDML();
                                                        int recordId = billRecordDML.recordPayment(
                                                                selectedBill.getBillID(),
                                                                tenantDbId,
                                                                landlordDbId,
                                                                selectedBill.getTotalAmount()
                                                        );

                                                        if (recordId > 0) {
                                                            // Redirect System.out to suppress messages during this operation
                                                            System.setOut(nullPrintStream);

                                                            // Update the bill in the BillRecord (output suppressed)
                                                            landlord.getBillRecord().updateBill(selectedBill);

                                                            // Restore original System.out
                                                            System.setOut(originalOut);

                                                            System.out.println("Bill payment for " + selectedBill.getBillDate() + " marked as paid.");
                                                            System.out.println("Payment successful! Thank you.");
                                                        } else {
                                                            System.out.println("Payment failed. Please try again later.");
                                                        }
                                                    } finally {
                                                        if (conn != null) {
                                                            conn.close();
                                                        }
                                                    }
                                                } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
                                                    // Ensure System.out is restored in case of exceptions
                                                    System.setOut(originalOut);
                                                    System.out.println("Payment error: " + e.getMessage());
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                System.out.println("Payment cancelled.");
                                            }
                                        } else {
                                            System.out.println("Invalid selection.");
                                        }
                                    } else {
                                        System.out.println("Please enter a number.");
                                        scanner.nextLine(); // Consume invalid input
                                    }
                                }
                            }
                        }
                        break;


                    case 2:
//                        System.out.println("\n===== Your Payment History =====");
//
//                        List<Bill> paymentHistory = landlord.getBillRecord().getBillHistoryForTenant(tenant.getIdCard());
//                        if (paymentHistory.isEmpty()) {
//                            System.out.println("No payment records found.");
//                        } else {
//                            paymentHistory.forEach(bill ->
//                                    System.out.println("Bill - ID: " + bill.getBillID() + ", Paid: " + bill.isPaid() + ", Date: " + bill.getBillDate())
//                            );
//                        }
                        System.out.println("Logging out...");
                        running = false;
                        break;
//                    case 3:
//                        System.out.println("Logging out...");
//                        running = false;
//                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } else {
                System.out.println("Please enter a number.");
                scanner.nextLine(); // Consume invalid input
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
                System.out.println("5. Bill Management");
                System.out.println("6. Logout");
//                System.out.println("7. Logout");
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
                    case 5: // Bill Management
                        billManagementMenu(scanner, landlord);
                        break;
//                    case 6:
//                        // View Reports
//                        reportsMenu(scanner, landlord);
//                        break;
                    case 6:
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

                    case 2: // Remove Building
                        System.out.print("Enter building name to remove: ");
                        buildingName = scanner.nextLine();

                        int buildingId = buildingDML.getBuildingIdByName(buildingName);
                        if (buildingId == -1) {
                            System.out.println("Building not found: " + buildingName);
                            break;
                        }

                        buildingDML.deleteBuilding(buildingId);
                        // Refresh the in-memory list after database operation
                        landlord.refreshBuildingList();
                        System.out.println("Building removed successfully.");
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
                            landlord.refreshBuildingList();
                            System.out.println("Building updated in database successfully.");
                        } else {
                            System.out.println("Building not found in database.");
                        }
                        break;

                    case 4:
                        // View All Buildings
                        System.out.println("\n--- All Buildings (from memory) ---");
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
                    System.out.println("1. Add Room ");
                    System.out.println("2. Remove Room ");
                    System.out.println("3. View Room Details");
                    System.out.println("4. View All Rooms");
                    System.out.println("5. Update Room");
                    System.out.println("6. Back to Main Menu");
                    System.out.print("Choose an option: ");
                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    // Declare variables outside switch to avoid redeclaration
                    String buildingName, floorNumber, roomNumber;
                    int buildingId, floorId, roomId;
                    BuildingDML buildingDML;
                    FloorDML floorDML;
                    RoomDML roomDML;

                    switch (choice) {
                        case 1:
                            // Add Room to Floor
                            System.out.print("Enter building name: ");
                            buildingName = scanner.nextLine();
                            System.out.print("Enter floor number: ");
                            floorNumber = scanner.nextLine();
                            System.out.print("Enter room number: ");
                            roomNumber = scanner.nextLine();
                            System.out.print("Enter initial electric counter: ");
                            int electricCounter = scanner.nextInt();
                            System.out.print("Enter initial water counter: ");
                            int waterCounter = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            buildingDML = new BuildingDML();
                            floorDML = new FloorDML();
                            roomDML = new RoomDML();

                            buildingId = buildingDML.getBuildingIdByName(buildingName);
                            if (buildingId == -1) {
                                System.out.println("Building not found. Creating new building...");
                                System.out.print("Enter building address: ");
                                String address = scanner.nextLine();
                                Building newBuilding = new Building(buildingName, address);
                                buildingDML.saveBuilding(newBuilding);
                                buildingId = buildingDML.getBuildingIdByName(buildingName);
                            }

                            floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, floorNumber);
                            if (floorId == -1) {
                                System.out.println("Floor not found. Creating new floor...");
                                Floor newFloor = new Floor(floorNumber);
                                floorDML.saveFloor(newFloor, buildingId);
                                floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, floorNumber);
                            }

                            Room newRoom = new Room(roomNumber, electricCounter, waterCounter);
                            roomDML.saveRoom(newRoom, floorId);
                            System.out.println("Room added successfully to database.");
                            break;

                        case 2:
                            // Remove Room from Floor
                            System.out.print("Enter building name: ");
                            buildingName = scanner.nextLine();
                            System.out.print("Enter floor number: ");
                            floorNumber = scanner.nextLine();
                            System.out.print("Enter room number: ");
                            roomNumber = scanner.nextLine();

                            buildingDML = new BuildingDML();
                            floorDML = new FloorDML();
                            roomDML = new RoomDML();

                            buildingId = buildingDML.getBuildingIdByName(buildingName);
                            if (buildingId == -1) {
                                System.out.println("Building not found: " + buildingName);
                                break;
                            }

                            floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, floorNumber);
                            if (floorId == -1) {
                                System.out.println("Floor not found: " + floorNumber + " in building: " + buildingName);
                                break;
                            }

                            roomId = roomDML.getRoomIdByFloorAndNumber(floorId, roomNumber);
                            if (roomId == -1) {
                                System.out.println("Room not found: " + roomNumber + " on floor: " + floorNumber + " in building: " + buildingName);
                                break;
                            }

                            // Check if room is occupied
                            Room roomToRemove = roomDML.getRoomById(roomId);
                            if (roomToRemove != null && roomToRemove.isOccupied()) {
                                System.out.println("Cannot remove room that is currently occupied. Please remove tenant first.");
                            } else {
                                roomDML.deleteRoom(roomId);
                                System.out.println("Room removed successfully.");
                            }
                            break;

                        case 3:
                            // View Room Details
                            System.out.print("Enter building name: ");
                            buildingName = scanner.nextLine();
                            System.out.print("Enter floor number: ");
                            floorNumber = scanner.nextLine();
                            System.out.print("Enter room number: ");
                            roomNumber = scanner.nextLine();

                            buildingDML = new BuildingDML();
                            floorDML = new FloorDML();
                            roomDML = new RoomDML();

                            buildingId = buildingDML.getBuildingIdByName(buildingName);
                            if (buildingId == -1) {
                                System.out.println("Building not found: " + buildingName);
                                break;
                            }

                            floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, floorNumber);
                            if (floorId == -1) {
                                System.out.println("Floor not found: " + floorNumber + " in building: " + buildingName);
                                break;
                            }

                            roomId = roomDML.getRoomIdByFloorAndNumber(floorId, roomNumber);
                            if (roomId == -1) {
                                System.out.println("Room not found: " + roomNumber + " on floor: " + floorNumber + " in building: " + buildingName);
                                break;
                            }

                            Room room = roomDML.getRoomById(roomId);
                            if (room != null) {
                                System.out.println("\n======= Room Details =======");
                                System.out.println("Building: " + buildingName);
                                System.out.println("Floor: " + floorNumber);
                                System.out.println("Room Number: " + room.getRoomNumber());
                                System.out.println("Status: " + (room.isOccupied() ? "Occupied" : "Vacant"));
                                System.out.println("Electric Counter: " + room.getCurrentElectricCounter());
                                System.out.println("Water Counter: " + room.getCurrentWaterCounter());

                                // Display tenant information if room is occupied
                                if (room.isOccupied() && room.getTenant() != null) {
                                    Tenant tenant = room.getTenant();
                                    System.out.println("\nTenant Information:");
                                    System.out.println("Name: " + tenant.getName());
                                    System.out.println("ID Card: " + tenant.getIdCard());
                                    System.out.println("Contact: " + tenant.getContact());
                                }
                                System.out.println("============================");
                            } else {
                                System.out.println("Error retrieving room details.");
                            }
                            break;

                        case 4:
                            // View All Rooms with building and floor information
                            buildingDML = new BuildingDML();

                            // Get all buildings
                            List<Building> buildings = buildingDML.getAllBuildings();

                            if (buildings.isEmpty()) {
                                System.out.println("No buildings found in database.");
                            } else {
                                System.out.println("\n======= All Rooms =======");
                                for (Building building : buildings) {
                                    System.out.println("\nBuilding: " + building.getName());

                                    // Get floors for this building
                                    List<Floor> floors = building.getFloors();
                                    if (floors.isEmpty()) {
                                        System.out.println("  No floors in this building.");
                                        continue;
                                    }

                                    for (Floor floor : floors) {
                                        System.out.println("  Floor: " + floor.getFloorNumber());

                                        // Get rooms for this floor
                                        List<Room> rooms = floor.getRooms();
                                        if (rooms.isEmpty()) {
                                            System.out.println("    No rooms on this floor.");
                                            continue;
                                        }

                                        for (Room r : rooms) {
                                            System.out.println("    Room: " + r.getRoomNumber() +
                                                    " | Status: " + (r.isOccupied() ? "Occupied" : "Vacant") +
                                                    " | Tenant: " + (r.getTenant() != null ? r.getTenant().getName() : "None"));
                                        }
                                    }
                                }
                                System.out.println("=========================");
                            }
                            break;

                        case 5:
                            // Update Room
                            System.out.print("Enter building name: ");
                            buildingName = scanner.nextLine();
                            System.out.print("Enter floor number: ");
                            floorNumber = scanner.nextLine();
                            System.out.print("Enter room number: ");
                            roomNumber = scanner.nextLine();

                            buildingDML = new BuildingDML();
                            floorDML = new FloorDML();
                            roomDML = new RoomDML();

                            buildingId = buildingDML.getBuildingIdByName(buildingName);
                            if (buildingId == -1) {
                                System.out.println("Building not found: " + buildingName);
                                break;
                            }

                            floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, floorNumber);
                            if (floorId == -1) {
                                System.out.println("Floor not found: " + floorNumber + " in building: " + buildingName);
                                break;
                            }

                            roomId = roomDML.getRoomIdByFloorAndNumber(floorId, roomNumber);
                            if (roomId == -1) {
                                System.out.println("Room not found: " + roomNumber + " on floor: " + floorNumber + " in building: " + buildingName);
                                break;
                            }

                            // Get current room details
                            Room roomToUpdate = roomDML.getRoomById(roomId);
                            if (roomToUpdate == null) {
                                System.out.println("Error retrieving room details.");
                                break;
                            }

                            // Show update options
                            System.out.println("\n=== Update Room ===");
                            System.out.println("1. Update Room Number");
                            System.out.println("2. Update Utility Counters");
                            System.out.println("3. Back");
                            System.out.print("Choose an option: ");
                            int updateChoice = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            switch (updateChoice) {
                                case 1:
                                    // Update Room Number
                                    System.out.println("Current Room Number: " + roomToUpdate.getRoomNumber());
                                    System.out.print("Enter new Room Number: ");
                                    String newRoomNumber = scanner.nextLine();

                                    // Check if the new room number already exists
                                    int existingRoomId = roomDML.getRoomIdByFloorAndNumber(floorId, newRoomNumber);
                                    if (existingRoomId != -1 && existingRoomId != roomId) {
                                        System.out.println("Room number already exists on this floor. Please choose a different room number.");
                                        break;
                                    }

                                    roomToUpdate.setRoomNumber(newRoomNumber);
                                    roomDML.updateRoom(roomId, roomToUpdate);
                                    System.out.println("Room number updated successfully.");
                                    break;

                                case 2:
                                    // Update Utility Counters
                                    System.out.println("Current Electric Counter: " + roomToUpdate.getCurrentElectricCounter());
                                    System.out.print("Enter new Electric Counter: ");
                                    int newElectricCounter = scanner.nextInt();

                                    System.out.println("Current Water Counter: " + roomToUpdate.getCurrentWaterCounter());
                                    System.out.print("Enter new Water Counter: ");
                                    int newWaterCounter = scanner.nextInt();
                                    scanner.nextLine(); // Consume newline

                                    // Validate counter values
                                    if (newElectricCounter < 0 || newWaterCounter < 0) {
                                        System.out.println("Counter values cannot be negative.");
                                        break;
                                    }

                                    try {
                                        if (roomToUpdate.isOccupied()) {
                                            // If room is occupied, use the updateUsage method
                                            roomToUpdate.updateUsage(newElectricCounter, newWaterCounter);
                                        } else {
                                            // If room is vacant, directly update the counters in the database
                                            roomDML.updateRoomCounters(roomId, newElectricCounter, newWaterCounter);
                                        }
                                        System.out.println("Utility counters updated successfully.");
                                    } catch (RoomException e) {
                                        System.out.println("Error updating counters: " + e.getMessage());
                                    }
                                    break;

                                case 3:
                                    // Back to previous menu
                                    break;

                                default:
                                    System.out.println("Invalid choice! Please try again.");
                                    break;
                            }
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
                System.out.println("6. Update Tenant Information"); // New option
                System.out.println("7. Back to Main Menu");         // Changed from 6 to 7
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
                        System.out.print("Enter tenant ID to remove: ");
                        tenantID = scanner.nextLine();
                        tenantDML = new TenantDML();
                        if (tenantDML.deleteTenant(tenantID)) { // Delete from database first
                            landlord.removeTenant(tenantID);    // Then remove from Landlord's in-memory collection
                            System.out.println("Tenant removed successfully.");
                        } else {
                            System.out.println("Failed to remove tenant from database.");
                        }
                        break;
                    case 3:
                        // Assign tenant to room
                        System.out.print("Enter tenant ID: ");
                        tenantID = scanner.nextLine();

                        System.out.print("Enter building name: ");
                        String buildingName = scanner.nextLine();

                        System.out.print("Enter floor number: ");
                        String floorNumber = scanner.nextLine();

                        System.out.print("Enter room number: ");
                        String roomNumber = scanner.nextLine();

                        LandlordDML landlordDML = new LandlordDML();
                        landlordDML.assignRoomToTenant(tenantID, buildingName, floorNumber, roomNumber);
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
                        // Update Tenant Information
                        System.out.print("Enter tenant ID to update: ");
                        String updateID = scanner.nextLine();
                        Tenant tenantToUpdate = landlord.getTenantByID(updateID);
                        if (tenantToUpdate != null) {
                            System.out.println("Current tenant information:");
                            System.out.println(tenantToUpdate.toString());

                            System.out.print("Enter new name (leave blank to keep current): ");
                            String newName = scanner.nextLine();
                            System.out.print("Enter new contact (leave blank to keep current): ");
                            String newContact = scanner.nextLine();
                            System.out.print("Enter new ID card (leave blank to keep current): ");
                            String newIdCard = scanner.nextLine();

                            String oldIdCard = tenantToUpdate.getIdCard();
                            boolean updateIdCard = !newIdCard.trim().isEmpty() && !newIdCard.equals(oldIdCard);

                            // Update tenant object in memory
                            if (!newName.trim().isEmpty()) {
                                tenantToUpdate.setName(newName);
                            }
                            if (!newContact.trim().isEmpty()) {
                                tenantToUpdate.setContact(newContact);
                            }
                            if (updateIdCard) {
                                TenantDML checkDML = new TenantDML();
                                if (checkDML.tenantExists(newIdCard)) {
                                    System.out.println("Error: Tenant with ID " + newIdCard + " already exists. ID card not updated.");
                                } else {
                                    tenantToUpdate.setIdCard(newIdCard);
                                }
                            }

                            // Persist to database and sync with Landlord
                            tenantDML = new TenantDML();
                            if (tenantDML.updateTenant(tenantToUpdate, updateIdCard ? oldIdCard : null)) {
                                landlord.updateTenant(tenantToUpdate); // Sync updated tenant back to Landlord
                                System.out.println("Tenant information updated successfully.");
                            } else {
                                System.out.println("Failed to update tenant information in database.");
                            }
                        } else {
                            System.out.println("Tenant not found with ID: " + updateID);
                        }
                        break;
                    case 7: // Changed from 6 to 7
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

    public static void billManagementMenu(Scanner scanner, Landlord landlord) {
        boolean running = true;

        while (running) {
            System.out.println("\n===== Bill Management =====");
            System.out.println("1. Create Bill for Room");
            System.out.println("2. View Bills for Month");
            System.out.println("3. View Bills for Tenant");
            System.out.println("4. View Unpaid Bills");
            System.out.println("5. Generate Monthly Report");
            System.out.println("6. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = -1;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
            } catch (Exception e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine(); // consume invalid input
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter building name: ");
                    String buildingName = scanner.nextLine();

                    System.out.print("Enter floor number: ");
                    String floorNumber = scanner.nextLine();

                    System.out.print("Enter room number: ");
                    String roomNumber = scanner.nextLine();

                    // Validate that the room exists in the specific building and floor
                    Room room = null;
                    Building building = landlord.getBuildingByName(buildingName);
                    if (building != null) {
                        Floor floor = building.getFloorByNumber(floorNumber);
                        if (floor != null) {
                            room = floor.getRoomByNumber(roomNumber);
                        }
                    }

                    if (room == null) {
                        System.out.println("Room not found in " + buildingName + ", Floor " + floorNumber + ". Please try again.");
                        break;
                    }

                    // Check if room is occupied
                    if (!room.isOccupied()) {
                        System.out.println("Room " + roomNumber + " in " + buildingName + ", Floor " + floorNumber + " is not occupied. Cannot create a bill.");
                        break;
                    }

                    System.out.print("Enter base rent amount (KHR): ");
                    double rentAmount = scanner.nextDouble();
                    scanner.nextLine(); // consume newline

                    System.out.print("Electric usage for room " + roomNumber + " (kWh): ");
                    int electricUsage = scanner.nextInt();

                    System.out.print("Water usage for room " + roomNumber + " (m³): ");
                    int waterUsage = scanner.nextInt();
                    scanner.nextLine(); // consume newline

                    // Create maps with just this single room
                    Map<String, Integer> electricUsageMap = new HashMap<>();
                    Map<String, Integer> waterUsageMap = new HashMap<>();
                    electricUsageMap.put(roomNumber, electricUsage);
                    waterUsageMap.put(roomNumber, waterUsage);

                    Bill bill1 = new Bill(room, buildingName, floorNumber, rentAmount, electricUsage, waterUsage);
                    BillDML billDML = new BillDML();
                    boolean saved = billDML.saveBill(bill1);

                    if (saved) {
                        System.out.println("\nBill created successfully for room " + roomNumber);
                        System.out.println(bill1.toString());

                    } else {
                        System.out.println("\nFailed to create bill for room " + roomNumber);
                    }

                    break;

                case 2:
                    System.out.print("Enter year (YYYY): ");
                    int year = scanner.nextInt();

                    System.out.print("Enter month (1-12): ");
                    int month = scanner.nextInt();
                    scanner.nextLine(); // consume newline

                    List<Bill> bills = landlord.viewBillsForMonth(year, month);

                    if (bills.isEmpty()) {
                        System.out.println("No bills found for " + month + "/" + year);
                    } else {
                        System.out.println("\n===== Bills for " + month + "/" + year + " =====");
                        for (Bill bill : bills) {
                            System.out.println(bill);
                            System.out.println("-----------------------------------");
                        }
                        System.out.println("Total bills: " + bills.size());
                    }
                    break;

                case 3:
                    System.out.print("Enter tenant ID: ");
                    String tenantId = scanner.nextLine();

                    List<Bill> tenantBills = landlord.viewBillsForTenant(tenantId);

                    if (tenantBills.isEmpty()) {
                        System.out.println("No bills found for tenant with ID: " + tenantId);
                    } else {
                        System.out.println("\n===== Bills for Tenant ID: " + tenantId + " =====");
                        for (Bill bill : tenantBills) {
                            System.out.println(bill);
                            System.out.println("-----------------------------------");
                        }
                        System.out.println("Total bills: " + tenantBills.size());
                    }
                    break;

                case 4:
                    List<Bill> unpaidBills = landlord.viewUnpaidBills();

                    if (unpaidBills.isEmpty()) {
                        System.out.println("No unpaid bills found.");
                    } else {
                        System.out.println("\n===== Unpaid Bills =====");
                        for (Bill bill : unpaidBills) {
                            System.out.println(bill);
                            System.out.println("-----------------------------------");
                        }
                        System.out.println("Total unpaid bills: " + unpaidBills.size());
                    }
                    break;

                case 5:
                    System.out.print("Enter year (YYYY): ");
                    year = scanner.nextInt();

                    System.out.print("Enter month (1-12): ");
                    month = scanner.nextInt();
                    scanner.nextLine(); // consume newline

                    String report = landlord.generateMonthlyBillingReport(year, month);
                    System.out.println(report);
                    break;

                case 6:
                    running = false;
                    break;

                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        }
    }

    // Reports Menu
//    static void reportsMenu(Scanner scanner, Landlord landlord) {
//        while (true) {
//            try {
//                System.out.println("\n=== Reports ===");
//                System.out.println("1. View All Buildings");
//                System.out.println("2. View All Tenants");
//                System.out.println("3. View Available Rooms");
//                System.out.println("4. View Occupied Rooms");
//                System.out.println("5. Back to Main Menu");
//                System.out.print("Choose an option: ");
//                int choice = scanner.nextInt();
//                scanner.nextLine(); // Consume newline
//
//                switch (choice) {
//                    case 1:
//                        // View All Buildings
//                        landlord.displayAllBuildings();
//                        break;
//                    case 2:
//                        // View All Tenants
//                        landlord.displayAllTenants();
//                        break;
//                    case 3:
//                        // View Available Rooms
//                        System.out.println("\n========= Available Rooms =========");
//                        boolean foundAvailable = false;
//                        for (Properties.Building building : landlord.getBuildings()) {
//                            for (Properties.Floor floor : building.getFloors()) {
//                                List<Room> availableRooms = floor.getAvailableRooms();
//                                if (!availableRooms.isEmpty()) {
//                                    foundAvailable = true;
//                                    System.out.println("Building: " + building.getBuildingName() + " ,Address: "  + building.getAddress() + " , Floor: " + floor.getFloorNumber());
//                                    for (Properties.Room room : availableRooms) {
//                                        System.out.println("  Room: " + room.getRoomNumber());
//                                    }
//                                }
//                            }
//                        }
//                        if (!foundAvailable) {
//                            System.out.println("No available rooms found.");
//                        }
//                        break;
//                    case 4:
//                        // View Occupied Rooms
//                        System.out.println("\n===== Occupied Rooms =====");
//                        boolean foundOccupied = false;
//                        for (Properties.Building building : landlord.getBuildings()) {
//                            for (Properties.Floor floor : building.getFloors()) {
//                                List<Properties.Room> occupiedRooms = floor.getOccupiedRooms();
//                                if (!occupiedRooms.isEmpty()) {
//                                    foundOccupied = true;
//                                    System.out.println("Building: " + building.getBuildingName() + " ,Address: "  + building.getAddress() + " , Floor: " + floor.getFloorNumber());
//                                    for (Properties.Room room : occupiedRooms) {
//                                        System.out.println("  Room: " + room.getRoomNumber() + ", Tenant: " + room.getTenant().getName());
//                                    }
//                                }
//                            }
//                        }
//                        if (!foundOccupied) {
//                            System.out.println("No occupied rooms found.");
//                        }
//                        break;
//                    case 5:
//                        return; // Back to main menu
//                    default:
//                        System.out.println("Invalid choice! Please try again.");
//                        break;
//                }
//            } catch (Exception e) {
//                System.out.println("An error occurred: " + e.getMessage());
//                scanner.nextLine(); // Consume invalid input
//            }
//        }
//    }
}
