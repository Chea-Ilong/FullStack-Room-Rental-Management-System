package Properties;

import java.util.ArrayList;
import java.util.List;

public class Floor {
    private int floorNumber;
    private List<Room> rooms; // Use ArrayList for rooms

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.rooms = new ArrayList<>(); // Initialize empty ArrayList
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public int getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(int floorNumber) {
        this.floorNumber = floorNumber;
    }

    //     Add a room to the floor
    public void addRoom(Room room) {
        rooms.add(room);
        System.out.println("Properties.Room " + room.getRoomID() + " added to floor " + floorNumber);
    }
//
//     Remove a room from the floor
public void removeRoom(String roomId) {
    Room room = searchRoom(roomId);
    if (room != null) {
        if (!room.isOccupied()) {
            rooms.remove(room);
            System.out.println("Room " + roomId + " removed from floor " + floorNumber);
        } else {
            System.out.println("Cannot remove occupied room " + roomId);
        }
    } else {
        System.out.println("Room " + roomId + " not found.");
    }
}


    //     Update a room on the floor
    public void updateRoom(String roomId, Room updatedRoom) {
        for (int i = 0; i < rooms.size(); i++) {
            if (rooms.get(i).getRoomID().equals(roomId)) {
                rooms.set(i, updatedRoom);
                System.out.println("Properties.Room " + roomId + " updated on floor " + floorNumber);
                return;
            }
        }
        System.out.println("Properties.Room " + roomId + " not found.");
    }

//     Search for a room by ID
    public Room searchRoom(String roomId) {
        for (Room room : rooms) {
            if (room.getRoomID().equals(roomId)) {
                return room;
            }
        }
        return null;
    }
//
//     Display all rooms on the floor
    public void displayFloorDetails() {
        for (Room room : rooms) {
            room.displayRoomInfo();
        }
    }
}
