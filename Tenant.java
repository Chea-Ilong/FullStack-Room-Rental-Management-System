import java.time.LocalDate;
import java.util.Date;

public class Tenant extends User {
    private String tenantIDCard;
    private boolean isBillPaid;
    private Date lastPaymentDate;
    private Date leaseStartDate;
    private Date leaseEndDate;
    private Payment payment; // Add Payment object

    public Tenant(String username, String phoneNumber, String tenantIDCard) {
        super(username, tenantIDCard, phoneNumber, "Tenant");
//        this.isBillPaid = false;
//        this.lastPaymentDate = null;
    }

    public void payRent(double amount) {
        this.payment = new Payment(1, LocalDate.now(), amount); // Create Payment object
        this.payment.markAsPaid(); // Mark payment as paid
        this.isBillPaid = true;
        this.lastPaymentDate = new Date();
        System.out.println(username + " has paid rent on " + lastPaymentDate);
    }

    @Override
    public String toString() {
        return "Tenant{" +
                "username='" + username + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", tenantIDCard='" + tenantIDCard + '\'' +
                ", isBillPaid=" + isBillPaid +
                ", lastPaymentDate=" + lastPaymentDate +
                ", leaseStartDate=" + leaseStartDate +
                ", leaseEndDate=" + leaseEndDate +
                ", payment=" + (payment != null ? payment.toString() : "No payment") +
                '}';
    }
}