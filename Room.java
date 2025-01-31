public class Room {

    private String roomID;
    private double roomPrice;
    private String roomType;
    private boolean status;
    private int waterCounterUsage;
    private int electricCounterUsage;
    private int currentElectricCounter;
    private int currentWaterCounter;
    private final int electricRate = 620;       //cubicmeter
    private final int waterRate = 2500;         //cublicmeter


    public Room(String roomID, double roomPrice, String roomType, boolean status) {

        this.roomID = roomID;
        this.roomPrice = roomPrice;
        this.roomType = roomType;
        this.status = status;

    }

    public double calculateElectricPrice(int currentElectricCounter,int electricRate,int electricCounterUsage) {
        return (currentElectricCounter - electricCounterUsage) * electricRate;
    };
    public double calculateWaterPrice(int currentWaterCounter,int waterRate,int waterCounterUsage) {
        return (currentWaterCounter - waterCounterUsage) * waterRate;
    };

}
