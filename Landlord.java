import java.util.ArrayList;
import java.util.List;

public class Landlord implements Authentication {

    private List<Floor> floors; // Use ArrayList for floors
    private double bill;
    private String landlordName;
    private String landlordId;
    private long landlordPhoneNumber;

    private static  List<Landlord> landlordlist = new ArrayList<>();

    public Landlord(String landlordName, String landlordId, long landlordPhoneNumber, double bill, int updateWaterCounter, int updateElectricCounter) {
        this.landlordName = landlordName;
        this.landlordId = landlordId;
        this.landlordPhoneNumber = landlordPhoneNumber;
        this.bill = bill;
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

    ;

    @Override
    public boolean login(String landlordName, String landlordId) {
        for (Landlord landlord : landlordlist) {
            if (landlord.landlordName.equalsIgnoreCase(landlordName) && landlord.landlordId == landlordId) {
                System.out.println("Login successful for " + landlord.landlordName);
                return true;
            }
        }
        System.out.println("Login failed: Invalid Landlord name or ID card.");
        return false;
    }

    @Override
    public void signUp() {
        // Prevent duplicate signups
        for (Landlord landlord : landlordlist) {
            if (landlord.landlordName.equalsIgnoreCase(this.landlordName) && landlord.landlordId == this.landlordId) {
                System.out.println("Sign-up failed: Tenant already exists.");
                return;
            }
        }

        // Add tenant to list
        landlordlist.add(this);
        System.out.println(landlordName + " has been successfully registered with ID Card: " + landlordId);
        System.out.println("Phone number: " + landlordPhoneNumber);

    }



}
    // Search customer details or room details by using room number

