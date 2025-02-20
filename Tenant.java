import java.util.Date;

public class Tenant extends User {
    private String tenantIDCard;
    private boolean isBillPaid;
    private Date lastPaymentDate;
    private Date leaseStartDate;
    private Date leaseEndDate;
    private Lease lease;
    
    public Tenant(String username, String phoneNumber, String tenantIDCard, Date leaseStartDate, Date leaseEndDate) {
        super(username, tenantIDCard, phoneNumber, "Tenant");
        this.isBillPaid = false;
        this.lastPaymentDate = null;
        this.leaseStartDate = leaseStartDate;
        this.leaseEndDate = leaseEndDate;
    }

    public void payRent() {
        this.isBillPaid = true;
        this.lastPaymentDate = new Date();
        System.out.println(username + " has paid rent on " + lastPaymentDate);
    }

    public String checkPaymentStatus() {
        if (!isBillPaid || lastPaymentDate == null) {
            return "Rent is NOT paid.";
        }
        return "Rent is paid. Last payment: " + lastPaymentDate;
    }

    public String getTenantIDCard() {
        return tenantIDCard;
    }

    public Lease getLease() {
        return lease;
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
                '}';
    }
}
