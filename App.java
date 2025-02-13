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
//
        Tenant tenant1 = new Tenant("John Doe", "555-1234", 101, true);
        Tenant tenant2 = new Tenant("long", "555-1234", 111, true);
        tenant1.signUp();
        tenant2.signUp();
        // tenant2.login("John Doe",101);
        // tenant1.login("long",111);
        tenant1.login("John Doe",101);
        tenant2.login("long",111);
//
//        Tenant tenant4 = new Tenant("io right", "5555-1234", 1011, true);
//        tenant4.signUp();
//        tenant4.login();


        // Checking payment status before paying
        System.out.println("Username: " + tenant1.getTenantName()); // Output: "Rent is NOT paid."
        System.out.println(tenant1.checkPaymentStatus()); // Output: "Rent is NOT paid."

        // Paying rent
        tenant1.payRent();

        // Checking payment status after paying
        System.out.println(tenant1.checkPaymentStatus()); // Output: "Rent is paid. Last payment: [date]"

        // Checking payment status before paying
        System.out.println("Username: " + tenant2.getTenantName()); // Output: "Rent is NOT paid."
        System.out.println(tenant2.checkPaymentStatus()); // Output: "Rent is NOT paid."

        // Paying rent
        tenant2.payRent();

        // Checking payment status after paying
        System.out.println(tenant2.checkPaymentStatus()); // Output: "Rent is paid. Last payment: [date]"
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
    }
}