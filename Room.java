public class Room {
    private String RoomID;
    private double RoomPrice;
    private int oldWaterCounter;
    private int oldElectricCounter;
    private int usedWater;
    private int usedElectric;
    private int newWaterCounter;
    private int newElectricCounter;
    private double electricPrice;
    private double waterPrice;

    public double CalculateWaterPrice () {
        return waterPrice = (newWaterCounter - oldWaterCounter) * 2500;
    }
}
