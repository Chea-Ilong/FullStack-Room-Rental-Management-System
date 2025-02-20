import java.time.LocalDate;

public class Lease {
    private static int totalLease = 0;
    private int leaseId;
    private int tenantId;
    private int roomID;
    private LocalDate startDate;
    private LocalDate endDate;
    private double monthlyRent;
    private boolean leaseStatus;

    public Lease(int tenantId, int roomID, LocalDate startDate, LocalDate endDate, double monthlyRent, boolean leaseStatus) {
        this.leaseId = ++totalLease;
        this.tenantId = tenantId;
        this.roomID = roomID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlyRent = monthlyRent;
        this.leaseStatus = leaseStatus;
    }
//
//    public void calculateLeaseDurationInMonths(){
//        // Method to calculate lease duration in months - Placeholder method
//
//    };
//
//    public void terminateLease(LocalDate terminationDate){
//        // Method to terminate the lease by setting the end date and changing lease status - Placeholder method
//
//    };
//
//    // Method to return a summary of the lease - Placeholder method
//    public String toString() {};
}
