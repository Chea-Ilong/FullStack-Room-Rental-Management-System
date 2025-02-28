package Users;

import Payment.UtilityUsage;
import Properties.Building;
import Properties.Floor;
import Properties.Room;

import java.time.LocalDate;
import java.util.*;

public class Landlord extends User {
    // ============================ Fields ==========================================
    private List<Tenant> tenants;
    private List<Building> buildings; // Manage multiple buildings
    private Map<LocalDate, UtilityUsage> utilityRecords;
    private static final int LANDLORD_PIN = 1234;

    // ============================ Constructor ====================================
    public Landlord(String name, String contact, String IDcard, List<Tenant> tenants, List<Building> buildings) {
        super(name, IDcard,contact, "Landlord");
        this.tenants = tenants != null ? tenants : new ArrayList<>();
        this.buildings = buildings != null ? buildings : new ArrayList<>();
        this.utilityRecords = new HashMap<>();
    }

    // ============================ Login & Authentication =========================


    public boolean login(Scanner scanner, String username, String password) {
        username = username.trim();
        password = password.trim();

        if (this.name.equals(username) && this.IdCard.equals(password)) {
            System.out.println("Login successful for " + this.name);

            // Prompt for PIN
            System.out.print("Enter your 4-digit PIN: ");
            int pin = scanner.nextInt();
            scanner.nextLine(); // Consume the newline left-over

            if (pin == LANDLORD_PIN) {
                System.out.println("PIN verified. Login successful as Landlord!");
                return true;
            } else {
                System.out.println("Incorrect PIN. Login failed for " + username);
                return false;
            }
        } else {
            System.out.println("Login failed for " + username);
            return false;
        }
    }

    // ============================ Utility Methods =================================
    public UtilityUsage getUtilityUsageForRoom(Room room, LocalDate date) {
        if (room.getUtilityUsage() != null && room.getUtilityUsage().getDate().equals(date)) {
            return room.getUtilityUsage();
        }
        return null;
    }

    public void setUtilityUsage(Room room, int electricUsage, int waterUsage) {
        // Use current date by default
        LocalDate today = LocalDate.now();
        setUtilityUsage(room, electricUsage, waterUsage, today);
    }

    public void setUtilityUsage(Room room, int electricUsage, int waterUsage, LocalDate date) {
        room.setUtilityUsage(electricUsage, waterUsage, date);
        System.out.println("Utility usage for Room " + room.getRoomNumber() + " on " + date + " has been set.");
    }

    public UtilityUsage getUtilityUsage(LocalDate date) {
        return utilityRecords.get(date);
    }

    public void displayUtilityUsage(LocalDate date) {
        UtilityUsage usage = getUtilityUsage(date);
        if (usage != null) {
            System.out.println("Utility usage for " + date + ": " + usage.toString());
        } else {
            System.out.println("No utility data available for " + date);
        }
    }

    // ============================ Room-Tenant Assignment =========================
    public void assignedTenantRoom(String tenantID, String newRoomNumber) {
        // Find the tenant by ID
        Tenant tenant = getTenantByID(tenantID);
        if (tenant != null) {
            // Find the current room the tenant is assigned to
            Room currentRoom = tenant.getAssignedRoom();

            // Check if the tenant is already assigned to a room and remove them
            if (currentRoom != null) {
                currentRoom.removeTenant();
                System.out.println(tenant.getName() + " has been removed from Room " + currentRoom.getRoomNumber());
            }

            // Find the new room by room number
            Room newRoom = getRoomAcrossAllBuildings(newRoomNumber);
            if (newRoom != null) {
                // Assign the tenant to the new room
                tenant.assignRoom(newRoom);
                newRoom.assignTenant(tenant);
                System.out.println(tenant.getName() + " has been assigned to Room " + newRoomNumber);
            } else {
                System.out.println("New Room not found.");
            }
        } else {
            System.out.println("Tenant not found.");
        }
    }


    // ============================ CRUD Operations for Room ========================
    // Find room by room number across all buildings and floors
    public Room getRoomAcrossAllBuildings(String roomNumber) {
        for (Building building : buildings) {
            for (Floor floor : building.getFloors()) {
                for (Room room : floor.getRooms()) {
                    if (room.getRoomNumber().equals(roomNumber)) {
                        return room; // Room found
                    }
                }
            }
        }
        return null; // Room not found
    }

    // ============================ CRUD Operations for Floor ========================
    public void addFloorToBuilding(String buildingName, Floor floor) {
        Building building = getBuildingByName(buildingName);
        if (building != null) {
            building.addFloor(floor);
            System.out.println("Floor " + floor.getFloorNumber() + " added to Building " + buildingName);
        } else {
            System.out.println("Building " + buildingName + " not found.");
        }
    }

    public void removeFloorFromBuilding(String buildingName, String floorNumber) {
        Building building = getBuildingByName(buildingName);
        if (building != null) {
            building.removeFloor(floorNumber);
        } else {
            System.out.println("Building " + buildingName + " not found.");
        }
    }

    // ============================ CRUD Operations for Building =====================
    public void addBuilding(Building building) {
        if (building != null && !buildings.contains(building)) {
            buildings.add(building);
            System.out.println("Building " + building.getBuildingName() + " has been added.");
        } else {
            System.out.println("Building already exists or is invalid.");
        }
    }

    public void removeBuilding(String buildingName) {
        Building building = getBuildingByName(buildingName);
        if (building != null) {
            buildings.remove(building);
            System.out.println("Building " + buildingName + " has been removed.");
        } else {
            System.out.println("Building not found.");
        }
    }

    public void updateBuildingName(String oldBuildingName, String newBuildingName) {
        Building building = getBuildingByName(oldBuildingName);
        if (building != null) {
            building.setBuildingName(newBuildingName);
            System.out.println("Building " + oldBuildingName + " has been renamed to " + newBuildingName);
        } else {
            System.out.println("Building not found.");
        }
    }

    public Building getBuildingByName(String buildingName) {
        for (Building building : buildings) {
            if (building.getBuildingName().equals(buildingName)) {
                return building;
            }
        }
        return null; // Building not found
    }

    // ============================ CRUD Operations for Tenant =======================
    public void addTenant(Tenant tenant) {
        if (tenant != null && !tenants.contains(tenant)) {
            tenants.add(tenant);
            System.out.println("Tenant added: " + tenant.getName());
        } else {
            System.out.println("Tenant already exists or is invalid.");
        }
    }

    public void removeTenant(String tenantID) {
        Tenant tenant = getTenantByID(tenantID);
        if (tenant != null) {
            // If tenant has an assigned room, remove them from it
            Room assignedRoom = tenant.getAssignedRoom();
            if (assignedRoom != null) {
                assignedRoom.removeTenant();
                System.out.println(tenant.getName() + " has been removed from Room " + assignedRoom.getRoomNumber());
            }

            tenants.remove(tenant);
            System.out.println("Tenant removed: " + tenant.getName());
        } else {
            System.out.println("Tenant not found.");
        }
    }

    public Tenant getTenantByID(String tenantID) {
        for (Tenant tenant : tenants) {
            if (tenant.getIdCard().equals(tenantID)) {
                return tenant;
            }
        }
        return null; // Tenant not found
    }

    // ============================ Getter Methods ===================================
    public List<Tenant> getTenants() {
        return tenants;
    }

    public List<Building> getBuildings() {
        return buildings;
    }

    // ============================ Display Information Methods ======================
    public void displayAllBuildings() {
        System.out.println("\n===== All Buildings =====");
        if (buildings.isEmpty()) {
            System.out.println("No buildings available.");
            return;
        }

        for (Building building : buildings) {
            System.out.println("Building Name: " + building.getBuildingName());
            building.displayAllFloors();
            System.out.println("---------------------");
        }
    }

    public void displayAllTenants() {
        System.out.println("\n===== All Tenants =====");
        if (tenants.isEmpty()) {
            System.out.println("No tenants registered.");
            return;
        }

        for (Tenant tenant : tenants) {
            System.out.println("Name: " + tenant.getName());
            System.out.println("ID: " + tenant.getIdCard());
            System.out.println("Contact: " + tenant.getContact());
            Room room = tenant.getAssignedRoom();
            System.out.println("Room: " + (room != null ? room.getRoomNumber() : "None"));
            System.out.println("---------------------");
        }
    }

    // ============================ toString Method =================================
    @Override
    public String toString() {
        return super.toString() +
                "Landlord{" +
                "tenants=" + tenants +
                ", buildings=" + buildings +
                '}';
    }
}
