import java.time.LocalDate;
import java.util.ArrayList;
//import java.util.Comparator;
import java.util.List;

public class ReportIssue {

    private static int totalIssues = 0;
    private int issueID;
    private String roomID;
    private String reporterName;
    private String tenantID;
    private String description;
    private boolean isResolved;
    private LocalDate reportedDate;

    // List to store all issues
    private static final ArrayList<ReportIssue> issues = new ArrayList<>();

    // Constructor (Automatically adds the issue to the list)
    public ReportIssue(String roomID, String reporterName, String tenantID, String description) {
        this.issueID = ++totalIssues;
        this.roomID = roomID;
        this.reporterName = reporterName;
        this.tenantID = tenantID;
        this.description = description;
        this.isResolved = false;
        this.reportedDate = LocalDate.now(); // Store current date when created
        issues.add(this); // Automatically add to the list
    }

    // Method to mark the issue as resolved
    public void resolveIssue() {
        if (!isResolved) {
            this.isResolved = true;
            System.out.println("Issue " + issueID + " has been resolved.");
        } else {
            System.out.println("Issue " + issueID + " is already resolved.");
        }
    }

    // Display pending issues
    public static void displayPendingIssues() {
        System.out.println("\nPending Issues:");

        // Sort issues first by reported date (oldest to newest)
//        issues.sort(Comparator.comparing(issue -> issue.reportedDate));

        // Display pending issues
        boolean hasPending = false;
        for (ReportIssue issue : issues) {
            if (!issue.isResolved) {
                System.out.println(issue.toString());  // Use toString() instead of displayIssue()
                hasPending = true;
            }
        }
        if (!hasPending) {
            System.out.println("No pending issues.");
        }
    }

    // Display resolved issues
    public static void displayResolvedIssues() {
        System.out.println("\nResolved Issues:");

        // Sort issues first by reported date (oldest to newest)
//        issues.sort(Comparator.comparing(issue -> issue.reportedDate));

        // Display resolved issues
        boolean hasResolved = false;
        for (ReportIssue issue : issues) {
            if (issue.isResolved) {
                System.out.println(issue.toString());  // Use toString() instead of displayIssue()
                hasResolved = true;
            }
        }
        if (!hasResolved) {
            System.out.println("No resolved issues.");
        }

    }

    // Method to return issue details as a String
    @Override
    public String toString() {
        return "Issue ID: " + issueID + "\n" +
                "Room ID: " + roomID + "\n" +
                "Reporter Name: " + reporterName + "\n" +
                "Tenant ID: " + tenantID + "\n" +
                "Description: " + description + "\n" +
                "Reported Date: " + reportedDate + "\n" +
                "Status: " + (isResolved ? "Resolved" : "Pending") + "\n" +
                "---------------------------------";
    }
}
