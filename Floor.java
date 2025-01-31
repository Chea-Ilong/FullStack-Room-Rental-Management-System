import java.util.List;
import java.util.ArrayList;

public class Floor {
    private int floorNumber;
    private List<Room> rooms;

    public Floor(int floorNumber) {
        this.floorNumber = floorNumber;
        this.rooms = new ArrayList<>();
    }

    public void AddRoom(Room room) {};
    public void removeRoom(String roomId) {};
    public void updateRoom(String roomId, Room updatedRoom) {};
    public void searchRoom(String roomId){};
    public void displayFloorDetails() {};
}
