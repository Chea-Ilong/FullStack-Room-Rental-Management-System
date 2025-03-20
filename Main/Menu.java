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

    // ====================================================================================================
    // Tenant Menu
    // ====================================================================================================

    public static void tenantMenu(Scanner scanner, Tenant tenant, Landlord landlord) {
        boolean running = true;

        while (running) {
            System.out.println("\n===== Tenant Menu =====");
            System.out.println("1. Pay Bill");
            System.out.println("2. Logout");
            System.out.print("Choose an option: ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1: // Pay Bill as Tenant
                        if (tenant.getAssignedRoom() == null) {
                            System.out.println("You are not assigned to any room.");
                        } else {
                            LocalDate today = LocalDate.now();

                            if (tenant.isBillPaid(today)) {
                                System.out.println("You have already paid your bill for this month.");
                            } else {
                                PrintStream originalOut = System.out;
                                ByteArrayOutputStream devNull = new ByteArrayOutputStream();
                                PrintStream nullPrintStream = new PrintStream(devNull);

                                System.setOut(nullPrintStream);

                                List<Bill> tenantBills = landlord.getBillRecord().getBillHistoryForTenant(tenant.getIdCard());

                                System.setOut(originalOut);

                                List<Bill> unpaidBills = tenantBills.stream()
                                        .filter(bill -> !bill.isPaid())
                                        .collect(Collectors.toList());

                                if (unpaidBills.isEmpty()) {
                                    System.out.println("You have no pending bills to pay.");
                                } else {
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

                                            System.out.println("\nSelected Bill Details:");
                                            System.out.println(selectedBill.toString());

                                            System.out.print("Confirm payment (Y/N)? ");
                                            String confirm = scanner.nextLine().trim().toUpperCase();

                                            if (confirm.equals("Y")) {
                                                try {
                                                    System.setOut(nullPrintStream);

                                                    selectedBill.markAsPaid(selectedBill.getTotalAmount());
                                                    tenant.markBillAsPaid(selectedBill.getBillDate());

                                                    System.setOut(originalOut);

                                                    int tenantDbId = 0;
                                                    int landlordDbId = 0;

                                                    try (Connection conn = DataBaseConnection.getConnection()) {
                                                        String tenantQuery = "SELECT tenant_id FROM Tenants t JOIN Users u ON t.user_id = u.user_id WHERE u.IdCard = ?";
                                                        try (PreparedStatement tenantStmt = conn.prepareStatement(tenantQuery)) {
                                                            tenantStmt.setString(1, tenant.getIdCard());
                                                            try (ResultSet tenantRs = tenantStmt.executeQuery()) {
                                                                if (tenantRs.next()) {
                                                                    tenantDbId = tenantRs.getInt("tenant_id");
                                                                } else {
                                                                    throw new SQLException("Tenant not found in database");
                                                                }
                                                            }
                                                        }

                                                        String landlordQuery = "SELECT landlord_id FROM Landlords WHERE user_id = ?";
                                                        try (PreparedStatement landlordStmt = conn.prepareStatement(landlordQuery)) {
                                                            landlordStmt.setInt(1, 1);
                                                            try (ResultSet landlordRs = landlordStmt.executeQuery()) {
                                                                if (landlordRs.next()) {
                                                                    landlordDbId = landlordRs.getInt("landlord_id");
                                                                } else {
                                                                    throw new SQLException("Landlord not found in database");
                                                                }
                                                            }
                                                        }

                                                        BillRecordDML billRecordDML = new BillRecordDML();
                                                        BillDML billDML = new BillDML();

                                                        int recordId = billRecordDML.recordPayment(
                                                                selectedBill.getBillID(),
                                                                tenantDbId,
                                                                landlordDbId,
                                                                selectedBill.getTotalAmount()
                                                        );

                                                        if (recordId > 0) {
                                                            boolean updated = billDML.markBillAsPaid(selectedBill.getBillID());
                                                            if (updated) {
                                                                System.setOut(nullPrintStream);
                                                                landlord.getBillRecord().updateBill(selectedBill);
                                                                System.setOut(originalOut);

                                                                System.out.println("Bill payment for " + selectedBill.getBillDate() + " marked as paid.");
                                                                System.out.println("Payment successful! Thank you.");

                                                                System.out.println("\nUpdated Bill Details:");
                                                                System.out.println(selectedBill.toString());
                                                            } else {
                                                                System.out.println("Failed to update bill status in database.");
                                                            }
                                                        } else {
                                                            System.out.println("Payment recording failed. Please try again later.");
                                                        }
                                                    }
                                                } catch (SQLException | IllegalArgumentException | IllegalStateException e) {
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
                        System.out.println("Logging out...");
                        running = false;
                        break;

                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } else {
                System.out.println("Please enter a number.");
                scanner.nextLine(); // Consume invalid input
            }
        }
    }

    // ====================================================================================================
    // Landlord Menu
    // ====================================================================================================

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
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
                        buildingManagementMenu(scanner, landlord);
                        break;
                    case 2:
                        floorManagementMenu(scanner, landlord);
                        break;
                    case 3:
                        roomManagementMenu(scanner, landlord);
                        break;
                    case 4:
                        tenantManagementMenu(scanner, landlord);
                        break;
                    case 5:
                        billManagementMenu(scanner, landlord);
                        break;
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

    // ====================================================================================================
    // Building Management Submenu
    // ====================================================================================================

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
                        System.out.print("Enter building name: ");
                        String buildingName = scanner.nextLine();
                        System.out.print("Enter building address: ");
                        String buildingAddress = scanner.nextLine();

                        Building building = new Building(buildingName, buildingAddress);
                        landlord.addBuilding(building);
                        buildingDML.saveBuilding(building);
                        break;

                    case 2:
                        System.out.print("Enter building name to remove: ");
                        buildingName = scanner.nextLine();

                        int buildingId = buildingDML.getBuildingIdByName(buildingName);
                        if (buildingId == -1) {
                            System.out.println("Building not found: " + buildingName);
                            break;
                        }

                        buildingDML.deleteBuilding(buildingId);
                        landlord.refreshBuildingList();
                        System.out.println("Building removed successfully.");
                        break;

                    case 3:
                        System.out.print("Enter current building name: ");
                        String oldBuildingName = scanner.nextLine();
                        System.out.print("Enter new building name: ");
                        String newBuildingName = scanner.nextLine();
                        System.out.print("Enter new building address: ");
                        String newBuildingAddress = scanner.nextLine();

                        int buildingIdToUpdate = buildingDML.getBuildingIdByName(oldBuildingName);
                        if (buildingIdToUpdate != -1) {
                            Building updatedBuilding = new Building(newBuildingName, newBuildingAddress);
                            buildingDML.updateBuilding(buildingIdToUpdate, updatedBuilding);
                            landlord.updateBuilding(oldBuildingName, newBuildingName, newBuildingAddress);
                            landlord.refreshBuildingList();
                            System.out.println("Building updated in database successfully.");
                        } else {
                            System.out.println("Building not found in database.");
                        }
                        break;

                    case 4:
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

    // ====================================================================================================
    // Floor Management Submenu
    // ====================================================================================================

    static void floorManagementMenu(Scanner scanner, Landlord landlord) {
        FloorDML floorDML = new FloorDML();
        BuildingDML buildingDML = new BuildingDML();

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
                        System.out.print("Enter building name: ");
                        String buildingName = scanner.nextLine();
                        System.out.print("Enter floor number: ");
                        String floorNumber = scanner.nextLine();

                        Properties.Building building = landlord.getBuildingByName(buildingName);
                        if (building != null) {
                            int buildingId = buildingDML.getBuildingIdByName(building.getName());
                            if (buildingId != -1) {
                                Properties.Floor newFloor = new Properties.Floor(floorNumber);
                                boolean success = floorDML.saveFloor(newFloor, buildingId);

                                if (success) {
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
                        System.out.print("Enter building name: ");
                        buildingName = scanner.nextLine();
                        System.out.print("Enter floor number to remove: ");
                        floorNumber = scanner.nextLine();

                        building = landlord.getBuildingByName(buildingName);
                        if (building != null) {
                            int buildingId = buildingDML.getBuildingIdByName(building.getName());
                            if (buildingId != -1) {
                                boolean success = floorDML.deleteFloorByBuildingAndNumber(buildingId, floorNumber);

                                if (success) {
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
                        System.out.print("Enter building name: ");
                        buildingName = scanner.nextLine();

                        building = landlord.getBuildingByName(buildingName);
                        if (building != null) {
                            int buildingId = buildingDML.getBuildingIdByName(building.getName());
                            if (buildingId != -1) {
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
                                building.displayAllFloors();
                            }
                        } else {
                            System.out.println("Building not found.");
                        }
                        break;

                    case 4:
                        System.out.print("Enter building name: ");
                        buildingName = scanner.nextLine();
                        System.out.print("Enter current floor number: ");
                        String currentFloorNumber = scanner.nextLine();
                        System.out.print("Enter new floor number: ");
                        String newFloorNumber = scanner.nextLine();

                        building = landlord.getBuildingByName(buildingName);
                        if (building != null) {
                            int buildingId = buildingDML.getBuildingIdByName(building.getName());
                            if (buildingId != -1) {
                                int floorId = floorDML.getFloorIdByBuildingAndNumber(buildingId, currentFloorNumber);

                                if (floorId != -1) {
                                    Properties.Floor updatedFloor = new Properties.Floor(newFloorNumber);
                                    boolean success = floorDML.updateFloor(floorId, updatedFloor);

                                    if (success) {
                                        System.out.println("Floor updated successfully in database.");
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

    // ====================================================================================================
    // Room Management Submenu
    // ====================================================================================================

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

                    String buildingName, floorNumber, roomNumber;
                    int buildingId, floorId, roomId;
                    BuildingDML buildingDML;
                    FloorDML floorDML;
                    RoomDML roomDML;

                    switch (choice) {
                        case 1:
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

                            Room roomToRemove = roomDML.getRoomById(roomId);
                            if (roomToRemove != null && roomToRemove.isOccupied()) {
                                System.out.println("Cannot remove room that is currently occupied. Please remove tenant first.");
                            } else {
                                roomDML.deleteRoom(roomId);
                                System.out.println("Room removed successfully.");
                            }
                            break;

                        case 3:
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

                            String query = "SELECT r.room_id, r.room_number, r.current_electric_counter, r.current_water_counter, r.is_occupied, " +
                                    "b.building_name, f.floor_number, u.name AS tenant_name " +
                                    "FROM Rooms r " +
                                    "LEFT JOIN Floors f ON r.floor_id = f.floor_id " +
                                    "LEFT JOIN Buildings b ON f.building_id = b.building_id " +
                                    "LEFT JOIN Tenants t ON t.assigned_room_id = r.room_id " +
                                    "LEFT JOIN Users u ON t.user_id = u.user_id " +
                                    "WHERE r.room_id = ?";

                            try (Connection conn = DataBaseConnection.getConnection();
                                 PreparedStatement ps = conn.prepareStatement(query)) {

                                ps.setInt(1, roomId);

                                try (ResultSet rs = ps.executeQuery()) {
                                    if (rs.next()) {
                                        RoomDML.RoomDetails details = new RoomDML.RoomDetails();
                                        details.roomId = rs.getInt("room_id");
                                        details.roomNumber = rs.getString("room_number");
                                        details.electricCounter = rs.getInt("current_electric_counter");
                                        details.waterCounter = rs.getInt("current_water_counter");
                                        details.isOccupied = rs.getBoolean("is_occupied");
                                        details.buildingName = rs.getString("building_name");
                                        details.floorNumber = rs.getString("floor_number");
                                        details.tenantName = rs.getString("tenant_name") != null ? rs.getString("tenant_name") : "N/A";

                                        System.out.println("\n======= Room Details =======");
                                        System.out.println("Room ID: " + details.roomId);
                                        System.out.println("Room Number: " + details.roomNumber);
                                        System.out.println("Electric Counter: " + details.electricCounter);
                                        System.out.println("Water Counter: " + details.waterCounter);
                                        System.out.println("Occupied: " + (details.isOccupied ? "Yes" : "No"));
                                        System.out.println("Building Name: " + details.buildingName);
                                        System.out.println("Floor Number: " + details.floorNumber);
                                        System.out.println("Tenant Name: " + details.tenantName);
                                        System.out.println("============================");
                                    } else {
                                        System.out.println("Error retrieving room details.");
                                    }
                                }
                            } catch (SQLException e) {
                                System.out.println("SQL Error retrieving room details: " + e.getMessage());
                                e.printStackTrace();
                            }
                            break;

                        case 4:
                            RoomDML roomDML1 = new RoomDML();
                            List<RoomDML.RoomDetails> roomDetailsList = roomDML1.getAllRoomsWithDetails();

                            for (RoomDML.RoomDetails details : roomDetailsList) {
                                System.out.println("Room ID: " + details.roomId);
                                System.out.println("Room Number: " + details.roomNumber);
                                System.out.println("Electric Counter: " + details.electricCounter);
                                System.out.println("Water Counter: " + details.waterCounter);
                                System.out.println("Occupied: " + (details.isOccupied ? "Yes" : "No"));
                                System.out.println("Building Name: " + details.buildingName);
                                System.out.println("Floor Number: " + details.floorNumber);
                                System.out.println("Tenant Name: " + details.tenantName);
                                System.out.println("-----------------------------");
                            }
                            break;

                        case 5:
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

                            Room roomToUpdate = roomDML.getRoomById(roomId);
                            if (roomToUpdate == null) {
                                System.out.println("Error retrieving room details.");
                                break;
                            }

                            System.out.println("\n=== Update Room ===");
                            System.out.println("1. Update Room Number");
                            System.out.println("2. Update Utility Counters");
                            System.out.println("3. Back");
                            System.out.print("Choose an option: ");
                            int updateChoice = scanner.nextInt();
                            scanner.nextLine(); // Consume newline

                            switch (updateChoice) {
                                case 1:
                                    System.out.println("Current Room Number: " + roomToUpdate.getRoomNumber());
                                    System.out.print("Enter new Room Number: ");
                                    String newRoomNumber = scanner.nextLine();

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
                                    System.out.println("Current Electric Counter: " + roomToUpdate.getCurrentElectricCounter());
                                    System.out.print("Enter new Electric Counter: ");
                                    int newElectricCounter = scanner.nextInt();

                                    System.out.println("Current Water Counter: " + roomToUpdate.getCurrentWaterCounter());
                                    System.out.print("Enter new Water Counter: ");
                                    int newWaterCounter = scanner.nextInt();
                                    scanner.nextLine(); // Consume newline

                                    if (newElectricCounter < 0 || newWaterCounter < 0) {
                                        System.out.println("Counter values cannot be negative.");
                                        break;
                                    }

                                    try {
                                        if (roomToUpdate.isOccupied()) {
                                            roomToUpdate.updateUsage(newElectricCounter, newWaterCounter);
                                        } else {
                                            roomDML.updateRoomCounters(roomId, newElectricCounter, newWaterCounter);
                                        }
                                        System.out.println("Utility counters updated successfully.");
                                    } catch (RoomException e) {
                                        System.out.println("Error updating counters: " + e.getMessage());
                                    }
                                    break;

                                case 3:
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

    // ====================================================================================================
    // Tenant Management Submenu
    // ====================================================================================================

    static void tenantManagementMenu(Scanner scanner, Landlord landlord) {
        while (true) {
            try {
                System.out.println("\n=== Tenant Management ===");
                System.out.println("1. Add Tenant");
                System.out.println("2. Remove Tenant");
                System.out.println("3. Assign Tenant to Room");
                System.out.println("4. View Tenant Details");
                System.out.println("5. View All Tenants");
                System.out.println("6. Update Tenant Information");
                System.out.println("7. Back to Main Menu");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                switch (choice) {
                    case 1:
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
                        if (tenantDML.deleteTenant(tenantID)) {
                            landlord.removeTenant(tenantID);
                            System.out.println("Tenant removed successfully.");
                        } else {
                            System.out.println("Failed to remove tenant from database.");
                        }
                        break;
                    case 3:
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
                        landlord.displayAllTenants();
                        break;
                    case 6:
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

                            tenantDML = new TenantDML();
                            if (tenantDML.updateTenant(tenantToUpdate, updateIdCard ? oldIdCard : null)) {
                                landlord.updateTenant(tenantToUpdate);
                                System.out.println("Tenant information updated successfully.");
                            } else {
                                System.out.println("Failed to update tenant information in database.");
                            }
                        } else {
                            System.out.println("Tenant not found with ID: " + updateID);
                        }
                        break;
                    case 7:
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

    // ====================================================================================================
    // Bill Management Submenu
    // ====================================================================================================

    public static void billManagementMenu(Scanner scanner, Landlord landlord) {
        boolean running = true;
        BillDML billDML = new BillDML();
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

                    RoomDML roomDML = new RoomDML();

                    int roomId = billDML.getRoomIdByBuildingFloorAndNumber(buildingName, floorNumber, roomNumber);
                    if (roomId == -1) {
                        System.out.println("Room not found.");
                        break;
                    }

                    Room room = roomDML.getRoomById(roomId);
                    if (room == null) {
                        System.out.println("Error retrieving room details.");
                        break;
                    }

                    if (!room.isOccupied() || room.getTenant() == null) {
                        System.out.println("Cannot create bill: Room " + roomNumber + " is vacant.");
                        break;
                    }

                    System.out.print("Enter base rent amount (KHR): ");
                    double rentAmount = Double.parseDouble(scanner.nextLine());
                    System.out.print("Electric usage for room " + roomNumber + " (kWh): ");
                    int electricUsage = Integer.parseInt(scanner.nextLine());
                    System.out.print("Water usage for room " + roomNumber + " (m³): ");
                    int waterUsage = Integer.parseInt(scanner.nextLine());

                    Bill bill = new Bill(room, buildingName, floorNumber, rentAmount, electricUsage, waterUsage);
                    if (billDML.saveBill(bill)) {
                        System.out.println("Bill created successfully for room " + roomNumber);
                        System.out.println(bill.toString());
                    } else {
                        System.out.println("Failed to create bill.");
                    }
                    break;

                case 2:
                    System.out.print("Enter year (YYYY): ");
                    int year = scanner.nextInt();

                    System.out.print("Enter month (1-12): ");
                    int month = scanner.nextInt();
                    scanner.nextLine(); // consume newline

                    List<Bill> monthlyBills = landlord.viewBillsForMonth(year, month);

                    if (monthlyBills.isEmpty()) {
                        System.out.println("No bills found for " + month + "/" + year);
                    } else {
                        System.out.println("\n===== Bills for " + month + "/" + year + " =====");
                        for (Bill monthBill : monthlyBills) {
                            System.out.println(monthBill);
                            System.out.println("-----------------------------------");
                        }
                        System.out.println("Total bills: " + monthlyBills.size());
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
                        for (Bill tenantBill : tenantBills) {
                            System.out.println(tenantBill);
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
                        for (Bill unpaidBill : unpaidBills) {
                            System.out.println(unpaidBill);
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
}
