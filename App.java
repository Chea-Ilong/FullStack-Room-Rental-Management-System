public class App {
    public static void main(String[] args) {
//
//        Room room = new Room("A101", "SMAsLL", false);
//        Room room3 = new Room("A102", "sMaLL", false);
//        Room room43 = new Room("A103", "medium", false);
//// Display room info
//        room.displayRoomInfo();   // No need to wrap in System.out.println()
//        room3.displayRoomInfo();
//        room43.displayRoomInfo();
////
//        Tenant tenant2 = new Tenant("long", "555-1234", 111, true);
//        Tenant tenant1 = new Tenant("John Doe", "555-1234", 101, true);
//        tenant1.signUp();
//        tenant2.signUp();
//        tenant2.login("John Doe",101);
//        tenant1.login("long",111);
////
//        Tenant tenant4 = new Tenant("io right", "5555-1234", 1011, true);
//        tenant4.signUp();
//        tenant4.login();


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
//        // Display the issue again after resolving
//        issue1.displayIssue();

        Room room = new Room("A101", "Medium", true, 100, 100);
        room.updateUsage(100, 100); // This should trigger the error for electric counter



//        // Display Room Info
//        room1.displayRoomInfo();
//
//        room1.updateUsage(190, 160);
//

        // Display Room Info
        room.displayRoomInfo();
    }
}