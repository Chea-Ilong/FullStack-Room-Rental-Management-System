public class Billing {
    private double totalBill;
    private boolean isBillPaid;
    public Billing(double totalBill, boolean isBillPaid) {
        this.totalBill = totalBill;
        this.isBillPaid = isBillPaid;
    }
    public void displayBill(){
        switch(bill){
            case 1:
                System.out.println("Monthly");
                break;
            case 2:
                System.out.println("Quarterly");
                break;
            case 3:
                System.out.println("Yearly");
                break;
            default:
                System.out.println("Invalid");
        }
    };
}
