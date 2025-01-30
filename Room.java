public class Room {

    private String roomID;
    private double roomPrice;
    private String roomType;
    private boolean status;
    private int waterCounterUsage ;
    private int electricCounterUsage ;
    private int currentElectricCounter ;
    private int currentWaterCounter ;
    private final int electricPrice = 620;
    private final int waterPrice = 2500;

    public Room(String roomID, double roomPrice, String roomType, boolean status) {

        this.roomID = roomID;
        this.roomPrice = roomPrice;
        this.roomType = roomType;
        this.status = status;

    }


}
