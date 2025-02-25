import java.util.ArrayList;
import java.util.List;

public class Admin extends User {

    private static List<Landlord> landlordList = new ArrayList<>();

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    public Admin() {
        super(ADMIN_USERNAME, ADMIN_PASSWORD, "N/A", "Admin");
    }

    public boolean login(String username, String password) {
        if (this.username.equals(username) && this.password.equals(password)) {
            System.out.println("Admin login successful.");
            return true;
        } else {
            System.out.println("Admin login failed: Invalid username or password.");
            return false;
        }
    }

    public void resetLandlordPassword(String landlordId, String newPassword) {
        for (Landlord landlord : Landlord.getLandlordList()) {
            if (landlord.getLandlordId().equals(landlordId)) {
                landlord.setPassword(newPassword);
                System.out.println("Password reset for landlord: " + landlordId);
                return;
            }
        }
        System.out.println("Landlord not found.");
    }


}

