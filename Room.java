public class Room {

    private String roomID;
    private double roomPrice;
    private String roomType;
    private boolean status ;
    private int waterCounterUsage;
    private int electricCounterUsage;
    private int currentElectricCounter;
    private int currentWaterCounter;
    private double electricPrice = 620;


    private double waterPrice = 2500;




    public Room(String roomID, double roomPrice, String roomType, boolean status, int waterCounterUsage, int electricCounterUsage) {
        this.roomID = roomID;
        this.roomPrice = roomPrice;
        this.roomType = roomType;
        this.status = status;
        this.waterCounterUsage = waterCounterUsage;
        this.electricCounterUsage = electricCounterUsage;
    }

    //    public void CalculateWaterPrice(){};
//    public void CalculateElectricPrice(){};

}
