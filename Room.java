public class Room {

    private String roomType;
    private String roomID;
    private double roomPrice;
    private boolean isOccupied;
    private int waterCounterUsage = 0;
    private int electricCounterUsage = 0;
    private  int currentElectricCounter;
    private  int currentWaterCounter;
    private final double electricRate = 620; // per kWh
    private final double waterRate = 2500;   // per cubic meter
    private final double KHR_TO_USD_RATE = 4100;

    public Room(String roomID, String roomSizeInput, boolean isOccupied,int currentElectricCounter, int currentWaterCounter) {
        this.roomID = roomID;
        this.roomType = roomSizeInput.equalsIgnoreCase("SMALL") ? "Small" : roomSizeInput.equalsIgnoreCase("MEDIUM") ? "Medium" : roomSizeInput.equalsIgnoreCase("LARGE") ? "Large" : "UNKNOWN";
        this.isOccupied = isOccupied;
        this.roomPrice = setPriceBasedOnSize();
        this.currentElectricCounter = Math.max(currentElectricCounter, 0);
        this.currentWaterCounter = Math.max(currentWaterCounter, 0);
        // Automatically set price based on size
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

public void updateUsage(int newElectricCounter, int newWaterCounter) {

    if (newElectricCounter < this.currentElectricCounter) {
        System.out.println("Error: New electric counter must be bigger then current electric counter.");
        return;
    }

    if (newWaterCounter < this.currentWaterCounter) {
        System.out.println("Error: New water counter must be bigger then current water counter.");
        return;
    }

    // If no change in usage, return early
    if (newElectricCounter == this.currentElectricCounter && newWaterCounter == this.currentWaterCounter) {
        System.out.println("No change in usage. No update needed.");
        return;
    }

    this.electricCounterUsage = newElectricCounter - this.currentElectricCounter;
    this.waterCounterUsage = newWaterCounter - this.currentWaterCounter;
    this.currentElectricCounter = newElectricCounter;
    this.currentWaterCounter = newWaterCounter;
}

    public double calculateElectricPrice() {
        return electricCounterUsage * electricRate;
    }

    public double calculateWaterPrice() {
        return waterCounterUsage * waterRate;
    }
//
//
//    public void displayRoomInfo() {
//        System.out.println("Room ID: " + roomID);
//        System.out.println("Room Size: " + roomType);
//        System.out.println("Price: " + roomPrice + " KHR ($" + convertToUSD(roomPrice) + " USD)");
//        System.out.println("Occupied: " + (isOccupied ? "Yes" : "No"));
//        System.out.println("Current Water Counter: " + currentWaterCounter);
//        System.out.println("Current Electric Counter: " + currentElectricCounter);
//        System.out.println();
//    }
//
//    public void displayRoomBilling() {
//        double electricPrice = calculateElectricPrice();
//        double waterPrice = calculateWaterPrice();
//        double totalPrice = roomPrice + electricPrice + waterPrice;
//        double totalPriceUSD = convertToUSD(totalPrice);
//
//        System.out.println("Room ID: " + roomID);
//        System.out.println("Room Size: " + roomType);
//        System.out.println("Room Price: " + roomPrice + " KHR ($" + convertToUSD(roomPrice) + " USD)");
//        System.out.println("Occupied: " + (isOccupied ? "Yes" : "No"));
//        System.out.println("Water Usage: " + waterCounterUsage + " m³");
//        System.out.println("Electric Usage: " + electricCounterUsage + " kWh");
//        System.out.println("Water Price: " + waterPrice + " KHR ($" + convertToUSD(waterPrice) + " USD)");
//        System.out.println("Electric Price: " + electricPrice + " KHR ($" + convertToUSD(electricPrice) + " USD)");
//        System.out.println("Total Expense: " + totalPrice + " KHR ($" + totalPriceUSD + " USD)");
//        System.out.println();
//    }
public void displayRoomInfo() {
    System.out.println("Room ID: " + roomID);
    System.out.println("Room Size: " + roomType);
    System.out.println("Room Price: " + roomPrice + " KHR ($" + convertToUSD(roomPrice) + " USD)");
    System.out.println("Occupied: " + (isOccupied ? "Yes" : "No"));
    System.out.println("Current Water Counter: " + currentWaterCounter);
    System.out.println("Current Electric Counter: " + currentElectricCounter);

    // If usage has been updated, show billing
    if (electricCounterUsage > 0 || waterCounterUsage > 0) {
        double electricPrice = calculateElectricPrice();
        double waterPrice = calculateWaterPrice();
        double totalPrice = roomPrice + electricPrice + waterPrice;
        double totalPriceUSD = convertToUSD(totalPrice);

        System.out.println("Water Usage: " + waterCounterUsage + " m³");
        System.out.println("Electric Usage: " + electricCounterUsage + " kWh");
        System.out.println("Water Price: " + waterPrice + " KHR ($" + convertToUSD(waterPrice) + " USD)");
        System.out.println("Electric Price: " + electricPrice + " KHR ($" + convertToUSD(electricPrice) + " USD)");
        System.out.println("Total Expense: " + totalPrice + " KHR ($" + totalPriceUSD + " USD)");
    }
    System.out.println();
}


}
