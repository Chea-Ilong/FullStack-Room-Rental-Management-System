public class Tenant {

    private String tenantName; //Customer name
    private String phoneNumber; //Customer phone number
    private int tenantIDCard; //Customer ID card
    private String userPassword;
//    double depositAmount;
    private boolean isBillPaid; // isPaid or Not
    public Tenant(String tenantName, String phoneNumber, int idCard, String userPassword, boolean isBillPaid) {
        this.tenantName = tenantName;
        this.phoneNumber = phoneNumber;
        this.idCard = idCard;
        this.userPassword = userPassword;
        this.isBillPaid = isBillPaid;
    }


    public void setRentPayment(){};

}
