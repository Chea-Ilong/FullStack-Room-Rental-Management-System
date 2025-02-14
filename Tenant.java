import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Tenant implements Authentication {

    private ReportIssue reportIssue;
    private String tenantName; // Customer name
    private String phoneNumber; // Customer phone number
    private String tenantIDCard; // Customer ID card
    private boolean isBillPaid; // Is the bill paid?
    private Date lastPaymentDate;
    // List to store tenants for simplicity
    private static List<Tenant> tenantList = new ArrayList<>();

    // For registration
    public Tenant(String tenantName, String phoneNumber, String tenantIDCard, boolean isBillPaid) {
        this.tenantName = tenantName;
        this.phoneNumber = phoneNumber;
        this.tenantIDCard = tenantIDCard;
        this.isBillPaid = isBillPaid;
    }

    // For login (without payment status as it's not needed)
    public Tenant(String tenantName, String tenantIDCard) {
        this.tenantName = tenantName;
        this.tenantIDCard = tenantIDCard;

    }

    // Check Payment, History...
        public Tenant(String tenantName, String phoneNumber, String tenantIDCard) {
        this.tenantName = tenantName;
        this.phoneNumber = phoneNumber;
        this.tenantIDCard = tenantIDCard;
        this.isBillPaid = false; // Default: unpaid
        this.lastPaymentDate = null;
    }
    @Override
    public boolean login(String tenantName,String tenantIDCard) {
        for (Tenant tenant : tenantList) {
            if (tenant.tenantName.equalsIgnoreCase(tenantName) && tenant.tenantIDCard.equalsIgnoreCase(tenantIDCard)) {
                System.out.println("Login successful for " + tenant.tenantName);
                return true;
            }
        }
        System.out.println("Login failed: Invalid tenant name or ID card.");
        return false;
    }

    @Override
    public void signUp() {
        // Prevent duplicate signups
        for (Tenant tenant : tenantList) {
            if (tenant.tenantName.equalsIgnoreCase(this.tenantName) && tenant.tenantIDCard == this.tenantIDCard) {
                System.out.println("Sign-up failed: Tenant already exists.");
                return;
            }
        }

        // Add tenant to list
        tenantList.add(this);
        System.out.println(tenantName + " has been successfully registered with ID Card: " + tenantIDCard);
        System.out.println("Phone number: " + phoneNumber);
    }


 public void payRent() {
        this.isBillPaid = true;
        this.lastPaymentDate = new Date(); // Save the date of payment
        System.out.println(tenantName + " has paid rent on " + lastPaymentDate);
    }

    // Method to check payment status
    public String checkPaymentStatus() {
        if (!isBillPaid || lastPaymentDate == null) {
            return "Rent is NOT paid.";
        }
        return "Rent is paid. Last payment: " + lastPaymentDate;
    }
public String getTenantName() {
    return tenantName;
}



}
