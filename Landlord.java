import java.util.ArrayList;
import java.util.List;

public class Landlord extends User {

    private List<Floor> floors; // Use ArrayList for floors
    private String landlordName;
    private String landlordId;
    private long landlordPhoneNumber;

    private static List<Landlord> landlordlist = new ArrayList<>();

    public Landlord(String landlordName, String landlordId, long landlordPhoneNumber) {
        super(landlordName, landlordId, String.valueOf(landlordPhoneNumber), "Landlord");
        this.landlordName = landlordName;
        this.landlordId = landlordId;
        this.landlordPhoneNumber = landlordPhoneNumber;
        this.floors = new ArrayList<>(); // Initialize empty ArrayList
    }

    public void addFloor(int floorNumber) {
    }

    ; //How many floor?

    public void insertToRoomToFloor(int floorNumber, Room room) {
    }

    ; //Add how many rooms per floor

    public void displayReportByMonth() {
    }

    ; //Display report by user input month

    public void displayAllReport() {
    }

    ; //Display all report

    public void updateRoom(String roomId, Room updatedRoom) {
    }

    ; //Update room

    public void searchRoom(String RoomID) {
    }

}
    // Search customer details or room details by using room number

