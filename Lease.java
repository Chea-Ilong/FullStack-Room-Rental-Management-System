//import java.time.LocalDate;
//import java.time.temporal.ChronoUnit;
//
//public class Lease {
//    private static int totalLease = 0;
//    private int leaseId;
//    private int tenantId;
//    private int roomID;
//    private LocalDate startDate;
//    private LocalDate endDate;
//    private double monthlyRent;
//    private boolean leaseStatus;
//
//    public Lease(int tenantId, int roomID, LocalDate startDate, LocalDate endDate, double monthlyRent, boolean leaseStatus) {
//        this.leaseId = ++totalLease;
//        this.tenantId = tenantId;
//        this.roomID = roomID;
//        this.startDate = startDate;
//        this.endDate = endDate;
//        this.monthlyRent = monthlyRent;
//        this.leaseStatus = leaseStatus;
//    }
//
//    public long calculateLeaseDurationInMonths() {
//        return ChronoUnit.MONTHS.between(startDate, endDate);
//    }
//
//    public void terminateLease(LocalDate terminationDate) {
//        this.endDate = terminationDate;
//        this.leaseStatus = false;
//    }
//
//    public Invoice generateInvoice() {
//        LocalDate invoiceDate = LocalDate.now();
//        return new Invoice(leaseId, invoiceDate, monthlyRent);
//    }
//
//    @Override
//    public String toString() {
//        return "Lease{" +
//                "leaseId=" + leaseId +
//                ", tenantId=" + tenantId +
//                ", roomID=" + roomID +
//                ", startDate=" + startDate +
//                ", endDate=" + endDate +
//                ", monthlyRent=" + monthlyRent +
//                ", leaseStatus=" + leaseStatus +
//                '}';
//    }
//}