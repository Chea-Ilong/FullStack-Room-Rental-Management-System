public class Room {

    private String RoomID;
    private double RoomPrice;
    private String RoomType;
    private int oldWaterCounter;
    private int oldElectricCounter;
    private double electricPrice = 0;
    private double waterPrice = 0;

    public Room( String roomID, double roomPrice, String roomType, int oldWaterCounter, int oldElectricCounter) {
        this.RoomID = roomID;
        this.RoomPrice = roomPrice;
        this.RoomType = roomType;
        this.oldWaterCounter = oldWaterCounter;
        this.oldElectricCounter = oldElectricCounter;

    }

    public void CalculateWaterPrice(){};
    public void CalculateElectricPrice(){};

}
