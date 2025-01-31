public class Room {

    private String roomID;
    private String roomType;
    private boolean status;
    private int waterCounterUsage;
    private int electricCounterUsage;
    private int currentElectricCounter;
    private int currentWaterCounter;
    private final double electricRate = 620.00;       //cubicmeter
    private final double waterRate = 2500.00;         //cublicmeter


    public Room(String roomID, String roomType, boolean status) {

        this.roomID = roomID;
        this.roomType = roomType;
        this.status = status;

    }

    public double calculateElectricPrice(int currentElectricCounter,double electricRate,int electricCounterUsage) {
        return (currentElectricCounter - electricCounterUsage) * electricRate;
    };
    public double calculateWaterPrice(int currentWaterCounter,double waterRate,int waterCounterUsage) {
        return (currentWaterCounter - waterCounterUsage) * waterRate;
    };
    public double calculateRoomPrice() {};          //base on the roomtype like small,medium and large
    public String roomSummary() {};
}
