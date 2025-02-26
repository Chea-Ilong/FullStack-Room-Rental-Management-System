import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Landlord extends User {

    private List<Floor> floors;
    private static List<Landlord> landlordList = new ArrayList<>();

    // Constructor
    public Landlord(String username, String password, String phoneNumber) {
        super(username, password, phoneNumber, "Landlord");
        this.floors = new ArrayList<>();
        landlordList.add(this); // Add to landlord list
    }

    // Getters and Setters
    public static List<Landlord> getLandlordList() {
        return Collections.unmodifiableList(landlordList);
    }

    public static void setLandlordList(List<Landlord> newList) {
        landlordList = newList;
    }

    public List<Floor> getFloors() {
        return floors;
    }

    public void setFloors(List<Floor> floors) {
        this.floors = floors;
    }

    // ========== FLOOR MANAGEMENT ==========

    // CREATE - Add a new floor
    public void addFloor(int floorNumber) {
        floors.add(new Floor(floorNumber));
        System.out.println("Floor " + floorNumber + " added.");
    }

    // READ - Get floor details
    public Floor getFloor(int floorNumber) {
        for (Floor floor : floors) {
            if (floor.getFloorNumber() == floorNumber) {
                return floor;
            }
        }
        System.out.println("Floor " + floorNumber + " not found.");
        return null;
    }

    // DELETE - Remove a floor
    public void removeFloor(int floorNumber) {
        floors.removeIf(floor -> floor.getFloorNumber() == floorNumber);
        System.out.println("Floor " + floorNumber + " removed.");
    }

    // ========== ROOM MANAGEMENT ==========

    // CREATE - Add room to floor
    public void addRoomToFloor(int floorNumber, Room room) {
        Floor floor = getFloor(floorNumber);
        if (floor != null) {
            floor.addRoom(room);
        } else {
            System.out.println("Floor " + floorNumber + " not found.");
        }
    }

    // READ - Search for room by ID
    public Room searchRoom(String roomId) {
        for (Floor floor : floors) {
            Room room = floor.searchRoom(roomId);
            if (room != null) {
                System.out.println("Room found: " + room);
                return room;
            }
        }
        System.out.println("Room " + roomId + " not found.");
        return null;
    }

    // UPDATE - Update room details
    public void updateRoom(String roomId, Room updatedRoom) {
        for (Floor floor : floors) {
            Room room = floor.searchRoom(roomId);
            if (room != null) {
                floor.updateRoom(roomId, updatedRoom);
                System.out.println("Room " + roomId + " updated.");
                return;
            }
        }
        System.out.println("Room " + roomId + " not found.");
    }

    // DELETE - Remove room from floor
    public void removeRoomFromFloor(int floorNumber, String roomId) {
        Floor floor = getFloor(floorNumber);
        if (floor != null) {
            floor.removeRoom(roomId);
        } else {
            System.out.println("Floor " + floorNumber + " not found.");
        }
    }

    // ========== TENANT MANAGEMENT ==========

    // CREATE - Assign a tenant to a room
    public void addTenantToRoom(String roomId, Tenant tenant) {
        Room room = searchRoom(roomId);
        if (room != null && !room.isOccupied()) {
            room.markAsOccupied();
            System.out.println("Tenant " + tenant.getUsername() + " assigned to Room " + roomId);
            tenant.setAssignedRoom(room);
        } else {
            System.out.println("Room " + roomId + " is occupied or not found.");
        }
    }

    // READ - Get tenant by room ID
    public void getTenantByRoom(String roomId) {
        Room room = searchRoom(roomId);
        if (room != null && room.isOccupied()) {
            System.out.println("Tenant found in Room " + roomId);
        } else {
            System.out.println("No tenant found in Room " + roomId);
        }
    }

    // UPDATE - Move tenant to a new room
    public void moveTenant(String oldRoomId, String newRoomId) {
        Room oldRoom = searchRoom(oldRoomId);
        Room newRoom = searchRoom(newRoomId);

        if (oldRoom != null && newRoom != null && !newRoom.isOccupied()) {
            oldRoom.markAsVacant();
            newRoom.markAsOccupied();
            System.out.println("Tenant moved from Room " + oldRoomId + " to Room " + newRoomId);
        } else {
            System.out.println("Move failed. Check room availability.");
        }
    }

    // DELETE - Remove tenant from a room
    public void removeTenantFromRoom(String roomId) {
        Room room = searchRoom(roomId);
        if (room != null && room.isOccupied()) {
            room.markAsVacant();
            System.out.println("Tenant removed from Room " + roomId);
        } else {
            System.out.println("No tenant found in Room " + roomId);
        }
    }

    // toString Method - Uses super.toString() to call the parent class toString method
    @Override
    public String toString() {
        return super.toString() + ", Floors Managed: " + floors.size();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}
