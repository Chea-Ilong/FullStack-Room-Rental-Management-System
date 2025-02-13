import java.util.ArrayList;
import java.util.List;

public class Tenant implements Authentication {

    private ReportIssue reportIssue;
    private String tenantName; // Customer name
    private String phoneNumber; // Customer phone number
    private int tenantIDCard; // Customer ID card
    private boolean isBillPaid; // Is the bill paid?

    // List to store tenants for simplicity
    private static List<Tenant> tenantList = new ArrayList<>();

    // For registration
    public Tenant(String tenantName, String phoneNumber, int tenantIDCard, boolean isBillPaid) {
        this.tenantName = tenantName;
        this.phoneNumber = phoneNumber;
        this.tenantIDCard = tenantIDCard;
        this.isBillPaid = isBillPaid;
    }

    // For login (without payment status as it's not needed)
    public Tenant(String tenantName, int tenantIDCard) {
        this.tenantName = tenantName;
        this.tenantIDCard = tenantIDCard;

    }
    @Override
    public boolean login(String tenantName,int tenantIDCard) {
        for (Tenant tenant : tenantList) {
            if (tenant.tenantName.equalsIgnoreCase(tenantName) && tenant.tenantIDCard == tenantIDCard) {
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


    public void setRentPayment(){};
    public String checkPaymentStatus() {
        return isBillPaid ? "The bill has been paid." : "The bill is not paid.";
    }


}
