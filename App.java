public class App {
    public static void main(String[] args) {


//
//        // Creating issue reports
//        ReportIssue issue1 = new ReportIssue("A101", "John Doe", "T12345", "Water leakage in bathroom.");
//        ReportIssue issue2 = new ReportIssue( "B202", "Jane Smith", "T67890", "Air conditioning not working.");
//
//        // Display issues
//        issue1.displayIssue();
//        issue2.displayIssue();
//
//        // Resolve an issue
//        issue1.resolveIssue();
//
        // Display the issue again after resolving


        // Create 3 issues with different reported dates
//        new ReportIssue("Room101", "John Doe", "T001", "Leaky faucet");
//        new ReportIssue("Room102", "Alice", "T002", "No hot water");
//        new ReportIssue("Room103", "Bob", "T003", "Broken window");
//
//        // Resolve an issue
//        ReportIssue issue4 = new ReportIssue("Room104", "John", "T004", "Electrical outage");
//        issue4.resolveIssue();
//
//        // Display pending issues
//        ReportIssue.displayPendingIssues();
//
//        // Display resolved issues
//        ReportIssue.displayResolvedIssues();
        // Display Room Info
//        room.displayRoomInfo();
//        room.displayRoomBilling();
//
//        room.updateUsage(200, 200);
//
//
//        room.displayRoomInfo();
//        room.displayRoomBilling();

//         Create rooms with initial details
        Room room1 = new Room("101", "Small", false, 0, 0);
        Room room2 = new Room("102", "Medium", false, 100, 100);
        Room room3 = new Room("103", "Large", true, 200, 150);

        // Display room details
        System.out.println("Displaying Room Information:\n");
        room1.displayRoomBilling();
        room2.displayRoomBilling();
        room3.displayRoomBilling();

        // Mark rooms as occupied or vacant
        System.out.println("Marking rooms as occupied:\n");
        room1.markAsOccupied();  // Room 101 will be marked occupied
        room2.markAsOccupied();  // Room 102 will be marked occupied
        room3.markAsVacant();    // Room 103 will be marked vacant (this resets its utility usage)

        // Update usage for occupied rooms
        System.out.println("Updating utility usage for rooms:\n");
        room2.updateUsage(150, 100);  // Update electric counter and water counter for room 101
        room1.updateUsage(200, 200);  // Update electric counter and water counter for room 102

        // Display updated room information and billing details
        System.out.println("Displaying Updated Room Information and Billing:\n");
        room1.displayRoomBilling();
        room1.displayRoomBilling();

        room2.displayRoomBilling();
        room2.displayRoomBilling();

        room3.displayRoomBilling();  // Room 103 is vacant, so no billing info will be shown
        room3.displayRoomBilling();  // No billing for a vacant room
    }
}