import java.util.List;
import java.util.ArrayList;

public class Landlord {

    private List<Floor> floors;
    private double bill;
    private String landlordName;
    private String landlordId; //Uses for reset password
    private long landlordPhoneNumber; // Uses for password
    private int UpdateWaterCounter;
    private int UpdateElectricCounter;


    public Landlord(String landlordName, String landlordId, long landlordPhoneNumber, double bill, int UpdateWaterCounter, int UpdateElectricCounter) {
        this.landlordName = landlordName;
        this.landlordId = landlordId;
        this.landlordPhoneNumber = landlordPhoneNumber;
        this.bill = bill;
        this.UpdateWaterCounter = UpdateWaterCounter;
        this.UpdateElectricCounter = UpdateElectricCounter;
        this.floors = new ArrayList<>();
    }



    public void addFloor(int floorNumber) {}; //How many floor?
    public void InsertToRoomToFloor(int floorNumber, Room room) {}; //Add how many rooms per floor
    public void displayReportByMonth(){}; //Display report by user input month
    public void displayAllReport(){}; //Display all report
    public void UpdateRoom(String roomId, Room updatedRoom){}; //Update room
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
