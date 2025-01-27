import java.time.LocalDate;

public class Payment {
    private int paymentId;
    private int tenantId;
    private int leaseId;
    private LocalDate paymentDate;
    private double amount;
    private String paymentMethod;
    private String paymentStatus;


    public Payment(int tenantId, int leaseId, double amount, String paymentMethod) {
        this.tenantId = tenantId;
        this.leaseId = leaseId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }
}
