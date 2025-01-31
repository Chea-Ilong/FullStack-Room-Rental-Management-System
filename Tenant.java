public class Tenant {

    private String tenantName; //Customer name
    private String phoneNumber; //Customer phone number
    private int tenantIDCard; //Customer ID card
    private boolean isBillPaid; // isPaid or Not

    //for register
    public Tenant(String tenantName, String phoneNumber, int tenantIDCard, boolean isBillPaid) {
        this.tenantName = tenantName;
        this.phoneNumber = phoneNumber;
        this.tenantIDCard = tenantIDCard;
        this.isBillPaid = isBillPaid;
    }
    //for login
    public Tenant(String tenantName,int tenantIDCard, String phoneNumber) {
        this.tenantName = tenantName;
        this.tenantIDCard = tenantIDCard;
        this.phoneNumber = phoneNumber;
    };

    public void setRentPayment(){};
    public String checkPaymentStatus() {
        return isBillPaid ? "The bill has been paid." : "The bill is not paid.";
    }

}
