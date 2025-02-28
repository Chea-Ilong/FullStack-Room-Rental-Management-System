package Users;
import Payment.UtilityUsage;
import Properties.Room;
import java.time.LocalDate;
import java.util.*;

public class Landlord extends User {
    // Fields
    private List<Tenant> tenants;
    private List<Room> rooms;
    private static Landlord instance;
    private Map<LocalDate, UtilityUsage> utilityRecords;
    private static final int LANDLORD_PIN = 1234;

    // Constructor
    public Landlord(String name, String contact, String IDcard, List<Tenant> tenants, List<Room> rooms) {
        super(name, contact, IDcard, "Landlord");
        this.name = name;
        this.contact = contact;
        this.IdCard = IDcard;
        this.tenants = tenants != null ? tenants : new ArrayList<>();
        this.rooms = rooms != null ? rooms : new ArrayList<>();
        this.utilityRecords = new HashMap<>();
    }

    // Login method override
    @Override
    public boolean login(String username, String password) {
        // Just check username and password (PIN verification handled in App)
        return super.login(username, password);
    }

    // PIN verification method
    public boolean verifyPin(int enteredPin) {
        return enteredPin == LANDLORD_PIN;
    }

    // Utility methods
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

    // Room-Tenant assignment
    public void updateTenantRoom(String tenantID, String newRoomNumber) {
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
            Room newRoom = getRoomByNumber(newRoomNumber);
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

    // CRUD Operations for Room
    public void addRoom(Room room) {
        if (room != null && !rooms.contains(room)) {
            rooms.add(room);
            System.out.println("Room added: " + room.getRoomNumber());
        } else {
            System.out.println("Room already exists or is invalid.");
        }
    }

    public void removeRoom(String roomNumber) {
        Room room = getRoomByNumber(roomNumber);
        if (room != null) {
            rooms.remove(room);
            System.out.println("Room removed: " + roomNumber);
        } else {
            System.out.println("Room not found.");
        }
    }

    public Room getRoomByNumber(String roomNumber) {
        for (Room room : rooms) {
            if (room.getRoomNumber().equals(roomNumber)) {
                return room;
            }
        }
        return null; // Room not found
    }

    // CRUD Operations for Tenant
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

    // Getter methods
    public List<Tenant> getTenants() {
        return tenants;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    // Display information methods
    public void displayAllRooms() {
        System.out.println("\n===== All Rooms =====");
        if (rooms.isEmpty()) {
            System.out.println("No rooms available.");
            return;
        }

        for (Room room : rooms) {
            System.out.println("Room Number: " + room.getRoomNumber());
            System.out.println("Status: " + (room.getTenant() != null ? "Occupied" : "Vacant"));
            if (room.getTenant() != null) {
                System.out.println("Tenant: " + room.getTenant().getName());
            }
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
}