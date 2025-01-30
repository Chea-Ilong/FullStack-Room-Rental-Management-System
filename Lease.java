import java.time.LocalDate;

public class Lease {
    private int leaseCounter = 0; // Auto-generate lease IDs
    private int leaseId;
    private int tenantId;
    private int roomID;
    private LocalDate startDate;
    private LocalDate endDate;
    private double monthlyRent;
    private String leaseStatus;

    public Lease(int tenantId, int roomID, LocalDate startDate, LocalDate endDate, double monthlyRent) {

        leaseCounter++;
        this.leaseId = leaseCounter;
        this.tenantId = tenantId;
        this.roomID = roomID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlyRent = monthlyRent;
        this.leaseStatus = "Active"; // Default status
    }
}
