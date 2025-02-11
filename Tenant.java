import java.util.ArrayList;
import java.util.List;

public class Tenant implements Authentication {

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
        this.isBillPaid = false; // Default value for bill payment (could be updated later)
    }

    @Override
    public boolean login() {
        // Simulating a login by matching tenant name and ID card
        for (Tenant tenant : tenantList) {
            if (tenant.getTenantName().equals(this.tenantName) && tenant.getTenantIDCard() == this.tenantIDCard) {
                System.out.println("Login successful for " + tenant.getTenantName());
                return true; // Found the tenant, login successful
            }
        }
        System.out.println("Login failed: Invalid tenant name or ID card.");
        return false; // Tenant not found, login failed
    }

    @Override
    public void signUp() {
        // Adding new tenant to the list (simulating registration)
        tenantList.add(this);
        System.out.println(tenantName + " has been successfully registered with ID Card: " + tenantIDCard);
    }

    public void setRentPayment(){};
    public String checkPaymentStatus() {
        return isBillPaid ? "The bill has been paid." : "The bill is not paid.";
    }

    public String getTenantName() {
        return tenantName;
    }

    public int getTenantIDCard() {
        return tenantIDCard;
    }
}
