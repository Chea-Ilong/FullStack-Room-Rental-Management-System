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
        this.leaseStatus = leaseStatus ;
    }
//
//    public long calculateLeaseDurationInMonths() {};
//    public void terminateLease(LocalDate terminationDate){};
//    public String leaseSummary() {};
}
