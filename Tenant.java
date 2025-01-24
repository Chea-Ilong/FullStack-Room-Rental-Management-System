public class Tenant {

    private String tenantName; //Customer name
    private int phoneNumber; //Customer phone number
    private int idCard; //Customer ID card
    private int tenantsPerRoom; //Number of people in one room
    private boolean rentPayment;
    double billAmount;
    public Tenant(String tenantName, int phoneNumber, int idCard, int tenantsPerRoom, boolean rentPayment) {
        this.tenantName = tenantName;
        this.phoneNumber = phoneNumber;
        this.idCard = idCard;
        this.tenantsPerRoom = 1; //Default value
        this.rentPayment = rentPayment;
    }

    public void tenantBill(){};
    public void setRentPayment(){};
    
}
