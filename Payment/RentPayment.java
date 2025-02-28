package Payment;
import java.time.LocalDate;

public class RentPayment {
    private String tenantID;
    private double amount;
    private LocalDate paymentDate;
    private boolean rentPayment;
    private boolean utilitiesPayment;

    // Constructor for Payment
    public RentPayment(String tenantID, double amount, boolean rentPayment, boolean utilitiesPayment) {
        this.tenantID = tenantID;
        this.amount = amount;
        this.paymentDate = LocalDate.now();
        this.rentPayment = rentPayment;
        this.utilitiesPayment = utilitiesPayment;
    }

//    // Display payment details
//    public void displayPaymentInfo() {
//        System.out.println("Payment Details:");
//        System.out.println("Tenant ID: " + tenantID);
//        System.out.println("Amount: " + amount);
//        System.out.println("Payment Date: " + paymentDate);
//        System.out.println("Rent Payment: " + (rentPayment ? "Yes" : "No"));
//        System.out.println("Utilities Payment: " + (utilitiesPayment ? "Yes" : "No"));
//    }

    @Override
    public String toString() {
        return "Payment [Tenant ID: " + tenantID + ", Amount: " + amount +
                ", Date: " + paymentDate + ", Rent: " + rentPayment + ", Utilities: " + utilitiesPayment + "]";
    }



}
