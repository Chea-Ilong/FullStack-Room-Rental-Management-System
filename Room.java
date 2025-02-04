public class Room {

    private String roomID;
    private String roomType;
    private boolean status;
    private int waterCounterUsage;
    private int electricCounterUsage;
    private int currentElectricCounter;
    private int currentWaterCounter;
    private final int electricRate = 620;       //cubicmeter
    private final int waterRate = 2500;         //cublicmeter





    public Room(String roomID, String roomType) {

        this.roomID = roomID;
        this.roomType = roomType;

    }

    public String checkRoomStatus() {
        return status ? "Room is Occupied" : "Room is Available";
    }
    public double calculateElectricPrice(int currentElectricCounter,int electricRate,int electricCounterUsage) {
        return (electricCounterUsage - currentElectricCounter) * electricRate;
    };
    public double calculateWaterPrice(int currentWaterCounter,int waterRate,int waterCounterUsage) {
        return (waterCounterUsage - currentWaterCounter) * waterRate;
    };
    public double calculateRoomPrice(){};
}
