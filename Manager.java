public class Manager {

    private int[][] floors = new int[5][5];
    private double bill;

    private int newWaterCounter;
    private int newElectricCounter;

    public Manager(int[][] floors, double bill, int floor, int newWaterCounter, int newElectricCounter) {
        this.floors = floors;
        this.bill = bill;
        this.newWaterCounter = newWaterCounter;
        this.newElectricCounter = newElectricCounter;
    }

    public void InsertToRoom() {};
    public void displayReportByMonth(){};
    public void displayAllReport(){};
    public void UpdateRoom(){};
    public void searchRoom(String RoomID){};


}
