public class Room extends Manager{

    private String RoomID;
    private double RoomPrice;
    private String RoomType;
    private int oldWaterCounter;
    private int oldElectricCounter;
    private double electricPrice;
    private double waterPrice;

    public Room(String roomID, String roomType, double roomPrice, int oldWaterCounter, int oldElectricCounter) {
        this.RoomID = roomID;
        this.RoomType = roomType;
        this.RoomPrice = roomPrice;
        this.oldWaterCounter = oldWaterCounter;
        this.oldElectricCounter = oldElectricCounter;

    }

    public double CalculateWaterPrice () {
        return waterPrice = (super.newWaterCounter - oldWaterCounter) * 2500;
    }

    public double CalculateElectricPrice () {
        return electricPrice = (super.newElectricCounter - oldElectricCounter) * 1200;
    };


}
