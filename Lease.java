import java.time.LocalDate;

public class Lease {
    private int leaseId;
    private int tenantId;
    private int roomID;
    private LocalDate startDate;
    private LocalDate endDate;
    private double monthlyRent;
    private String leaseStatus;


    public Lease(int tenantId, int roomID, LocalDate startDate, LocalDate endDate, double monthlyRent) {
        this.tenantId = tenantId;
        this.roomID = roomID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlyRent = monthlyRent;
    }
}
