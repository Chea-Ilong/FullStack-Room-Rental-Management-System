import java.util.ArrayList;
import java.util.List;

public class Landlord {

    private List<Floor> floors; // Use ArrayList for floors
    private double bill;
    private String landlordName;
    private String landlordId;
    private long landlordPhoneNumber;
    private int updateWaterCounter;
    private int updateElectricCounter;

    public Landlord(String landlordName, String landlordId, long landlordPhoneNumber, double bill, int updateWaterCounter, int updateElectricCounter) {
        this.landlordName = landlordName;
        this.landlordId = landlordId;
        this.landlordPhoneNumber = landlordPhoneNumber;
        this.bill = bill;
        this.updateWaterCounter = updateWaterCounter;
        this.updateElectricCounter = updateElectricCounter;
        this.floors = new ArrayList<>(); // Initialize empty ArrayList
    }



    public void addFloor(int floorNumber) {}; //How many floor?
    public void insertToRoomToFloor(int floorNumber, Room room) {}; //Add how many rooms per floor
    public void displayReportByMonth(){}; //Display report by user input month
    public void displayAllReport(){}; //Display all report
    public void updateRoom(String roomId, Room updatedRoom){}; //Update room
    public void searchRoom(String RoomID){};
//
//    @Override
//    public boolean login() {
//        return false;
//    }
//
//    @Override
//    public void signUp() {
//
//    } // Search customer details or room details by using room number

}
