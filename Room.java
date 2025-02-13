import java.util.Scanner;
public class Room {

    private String roomType;

    private String roomID;
    private double roomPrice;
    private boolean isOccupied;
    private int waterCounterUsage;
    private int electricCounterUsage;
    private int currentElectricCounter;
    private static int currentWaterCounter;
    private final double electricRate = 620; // per kWh
    private final double waterRate = 2500;   // per cubic meter
    private final double KHR_TO_USD_RATE = 4100;

    public Room(String roomID, String roomSizeInput, boolean isOccupied) {
        this.roomID = roomID;
        this.roomType = roomSizeInput.equalsIgnoreCase("SMALL") ? "Small" : roomSizeInput.equalsIgnoreCase("MEDIUM") ? "Medium" : roomSizeInput.equalsIgnoreCase("LARGE") ? "Large" : "UNKNOWN";
        this.isOccupied = isOccupied;
        this.roomPrice = setPriceBasedOnSize();  // Automatically set price based on size
    }

    private double convertToUSD(double khrPrice) {
        return Math.round(khrPrice / KHR_TO_USD_RATE * 100.0) / 100.0;  // Rounds to two decimal places
    }

    // Set price based on the room size
    private double setPriceBasedOnSize() {
        return switch (this.roomType) {
            case "Small" -> 30000;
            case "Medium" -> 50000;
            case "Large" -> 80000;
            default -> 0;
        };
    }
    public double calculateElectricPrice(int currentElectricCounter,int electricCounterUsage) {
        return (currentElectricCounter - electricCounterUsage) * electricRate;
    };


    public double calculateWaterPrice(int waterCounterUsage) {
        int previousWaterCounter = currentWaterCounter; // Store previous reading
        double price = (waterCounterUsage - previousWaterCounter) * waterRate; // Calculate price

        currentWaterCounter = waterCounterUsage; // Update counter after calculation
        return price;
    }

    public void displayRoomInfo() {
        System.out.println("Room ID: " + roomID);
        System.out.println("Room Size: " + roomType);  // Capitalize enum value for display
        System.out.println("Price: " + roomPrice + " KHR ($" + convertToUSD(roomPrice) + " USD)");
        System.out.println("Occupied: " + (isOccupied ? "Yes" : "No"));
        System.out.println(" ");
    }
}
