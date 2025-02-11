import java.util.Scanner;
public class Room {

    public enum RoomType { SMALL, MEDIUM, LARGE };

    private String roomID;
    private double roomPrice;
    private RoomType roomType;
    private boolean isOccupied;
    private int waterCounterUsage;
    private int electricCounterUsage;
    private int currentElectricCounter;
    private int currentWaterCounter;
    private final double electricRate = 620; // per kWh
    private final double waterRate = 2500;   // per cubic meter
    private final double KHR_TO_USD_RATE = 4150;


    public Room(String roomID, String roomSizeInput, boolean isOccupied) {
        this.roomID = roomID;
        this.roomType = getValidRoomSize(roomSizeInput);  // Validate room size
        this.isOccupied = isOccupied;
        this.roomPrice = setPriceBasedOnSize(this.roomType);  // Automatically set price based on size
    }
    private RoomType getValidRoomSize(String roomSizeInput) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            try {
                return RoomType.valueOf(roomSizeInput.toUpperCase());  // Validate if the input matches an enum value
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid room size. Please enter 'small', 'medium', or 'large'.");
                System.out.print("Enter valid room size: ");
                roomSizeInput = scanner.nextLine();  // Prompt for valid input
            }
        }
    }
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
    private double convertToUSD(double khrPrice) {
        return Math.round(khrPrice / KHR_TO_USD_RATE * 100.0) / 100.0;  // Rounds to two decimal places
    }

    // Set price based on the room size
    private double setPriceBasedOnSize(RoomType size) {
        return switch (size) {
            case SMALL -> 30000;
            case MEDIUM -> 50000;
            case LARGE -> 80000;
            default -> 0;
        };
    }
    public double calculateElectricPrice(int currentElectricCounter,int electricCounterUsage) {
        return (currentElectricCounter - electricCounterUsage) * electricRate;
    };
    public double calculateWaterPrice(int currentWaterCounter,int waterCounterUsage) {
        return (currentWaterCounter - waterCounterUsage) * waterRate;
    };

    public void displayRoomInfo() {
        System.out.println("Room ID: " + roomID);
        System.out.println("Room Size: " + capitalize(roomType.name()));  // Capitalize enum value for display
        System.out.println("Price: " + roomPrice + " KHR ($" + convertToUSD(roomPrice) + " USD)");
        System.out.println("Occupied: " + (isOccupied ? "Yes" : "No"));
        System.out.println(" ");
    }
}
