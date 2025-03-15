package Main;

import DataBase.*;
import Exceptions.RoomException;
import Exceptions.TenantException;
import Payment.Bill;
import Properties.Building;
import Properties.Floor;
import Properties.Room;
import Users.Landlord;
import Users.Tenant;
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
            System.out.println("2. Display Payment History");
            System.out.println("3. Logout");
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
                                // Find bills for this tenant
                                List<Bill> tenantBills = landlord.getBillRecord().getBillHistoryForTenant(tenant.getIdCard());
                                List<Bill> unpaidBills = tenantBills.stream()
                                        .filter(bill -> !bill.isPaid())
                                        .collect(Collectors.toList());

                                if (unpaidBills.isEmpty()) {
                                    System.out.println("You have no pending bills to pay.");
                                } else {
                                    System.out.println("\n===== Your Unpaid Bills =====");
                                    for (int i = 0; i < unpaidBills.size(); i++) {
                                        Bill bill = unpaidBills.get(i);
                                        System.out.printf("%d. Bill ID: %s - Date: %s - Amount: %.0f KHR (%.2f USD)\n",
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

                                            if (confirm.equals("Y")) {
                                                try {
                                                    selectedBill.markAsPaid(selectedBill.getTotalAmount());
                                                    tenant.markBillAsPaid(selectedBill.getBillDate());
                                                    System.out.println("Payment successful! Thank you.");
                                                } catch (IllegalArgumentException | IllegalStateException e) {
                                                    System.out.println("Payment error: " + e.getMessage());
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
                        System.out.println("\n===== Your Payment History =====");

                        // Iterate through all months of the year
                        int currentYear = LocalDate.now().getYear();

                        boolean hasPayments = false;
                        for (int month = 1; month <= 12; month++) {
                            if (tenant.checkBillPaymentStatus(currentYear, month)) {
                                hasPayments = true;
                                System.out.println("Paid for " + YearMonth.of(currentYear, month).toString());
                            }
                        }

                        if (!hasPayments) {
                            System.out.println("No payment records found for this year.");
                        }
                        break;
                    case 3:
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
                    case 5: // Bill Management
                        billManagementMenu(scanner, landlord);
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
//                            newRoom.setRent(rent);
//                            newRoom.setUtilityUsage(electricCounter, waterCounter, LocalDate.now());

                            roomDML.saveRoom(newRoom, floorId);
                            System.out.println("Room added successfully to database.");
                            break;

                        case 2:
//                            // Remove Room from Floor
//                            System.out.print("Enter room number to remove: ");
//                            roomNumber = scanner.nextLine();
//
//                            roomDML = new RoomDML();
//                            int roomId = roomDML.getRoomIdByRoomNumber(roomNumber);
//
//                            if (roomId != -1) {
//                                roomDML.deleteRoom(roomId);
//                                System.out.println("Room removed successfully.");
//                            } else {
//                                System.out.println("Room not found.");
//                            }
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

    public static void billManagementMenu(Scanner scanner, Landlord landlord) {
        boolean running = true;

        while (running) {
            System.out.println("\n===== Bill Management =====");
            System.out.println("1. Create Bills for Floor");
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

                    System.out.print("Enter base rent amount (KHR): ");
                    double rentAmount = scanner.nextDouble();
                    scanner.nextLine(); // consume newline

                    // Get electric usage for each room
                    Map<String, Integer> electricUsageMap = new HashMap<>();
                    Map<String, Integer> waterUsageMap = new HashMap<>();


                        System.out.print("Room number: ");
                        String roomNumber = scanner.nextLine();

                        if (roomNumber.equalsIgnoreCase("done")) {
                            break;
                        }

                        Room room = landlord.getRoomAcrossAllBuildings(roomNumber);
                        if (room == null) {
                            System.out.println("Room not found. Please try again.");
                            continue;
                        }

                        if (!room.isOccupied()) {
                            System.out.println("Room " + roomNumber + " is not occupied. Skipping.");
                            continue;
                        }

                        System.out.print("Electric usage for room " + roomNumber + " (kWh): ");
                        int electricUsage = scanner.nextInt();

                        System.out.print("Water usage for room " + roomNumber + " (m³): ");
                        int waterUsage = scanner.nextInt();
                        scanner.nextLine(); // consume newline

                        electricUsageMap.put(roomNumber, electricUsage);
                        waterUsageMap.put(roomNumber, waterUsage);


                    List<Bill> generatedBills = landlord.createBillsForFloor(buildingName, floorNumber,
                            rentAmount, electricUsageMap, waterUsageMap);

                    System.out.println("\nGenerated " + generatedBills.size() + " bills.");
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
