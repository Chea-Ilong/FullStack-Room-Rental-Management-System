public class ReportIssue {

    private static int totalIssues = 0;
    private int issueID;
    private String roomID;
    private String reporterName;
    private String tenantID;
    private String description;
    private boolean isResolved;

    // Constructor
    public ReportIssue(String roomID, String reporterName, String tenantID, String description) {
        this.issueID = ++totalIssues;
        this.roomID = roomID;
        this.reporterName = reporterName;
        this.tenantID = tenantID;
        this.description = description;
        this.isResolved = false;  // Default status: not resolved
    }

    // Method to mark the issue as resolved
    public void resolveIssue() {
        this.isResolved = true;
        System.out.println("Issue " + issueID + " has been resolved.");
    }

    // Method to display issue details
    public void displayIssue() {


        System.out.println("Issue ID: " + issueID);
        System.out.println("Room ID: " + roomID);
        System.out.println("Reporter Name: " + reporterName);
        System.out.println("Tenant ID: " + tenantID);
        System.out.println("Description: " + description);
        System.out.println("Status: " + (isResolved ? "Resolved" : "Pending"));
        System.out.println("---------------------------------");
    }
}
