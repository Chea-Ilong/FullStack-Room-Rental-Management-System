import java.util.ArrayList;
import java.util.List;
import java.util.Collections;


public class Landlord extends User {

    private List<Floor> floors;
    private String landlordName;
    private String landlordId;
    private long landlordPhoneNumber;
    private double revenue;

    private static List<Landlord> landlordList = new ArrayList<>();

    public Landlord(String landlordName, String landlordId, long landlordPhoneNumber) {
        super(landlordName, landlordId, String.valueOf(landlordPhoneNumber), "Landlord");
        this.landlordName = landlordName;
        this.landlordId = landlordId;
        this.landlordPhoneNumber = landlordPhoneNumber;
        this.floors = new ArrayList<>();
        this.revenue = 0.0;
    }

    //Getter and Setter

    public void setPassword(String newPassword) {
        this.password = newPassword;
    }

    public static List<Landlord> getLandlordList() {
        return Collections.unmodifiableList(landlordList);
    }

    public String getLandlordName() {
        return landlordName;
    }

    public void setLandlordName(String landlordName) {
        this.landlordName = landlordName;
    }

    public String getLandlordId() {
        return landlordId;
    }

    public void setLandlordId(String landlordId) {
        this.landlordId = landlordId;
    }

    public long getLandlordPhoneNumber() {
        return landlordPhoneNumber;
    }

    public void setLandlordPhoneNumber(long landlordPhoneNumber) {
        this.landlordPhoneNumber = landlordPhoneNumber;
    }

//    public static List<Landlord> getLandlordList() {
//        return landlordList;
//    }

    public static void setLandlordList(List<Landlord> landlordList) {
        Landlord.landlordList = landlordList;
    }

    //Function

    public void addRevenue(double amount) {
        this.revenue += amount;
    }

    public double getRevenue() {
        return this.revenue;
    }


    public void addFloor(int floorNumber) {
        floors.add(new Floor(floorNumber));
    }

    ; //How many floor?

    public void insertToRoomToFloor(int floorNumber, Room room) {
        for (Floor floor : floors) {
            if (floor.getFloorNumber() == floorNumber) {
                floor.addRoom(room);
                return;
            }
        }
        System.out.println("Floor " + floorNumber + " not found.");
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

