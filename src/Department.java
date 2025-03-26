import java.util.UUID;

public class Department {
    private String depName;
    private String depId;  // Use String to hold UUID
    private double depKdv;
    private double depPrice;
    private int depCount;

    // Constructor
    public Department(String depName, double depKdv, double depPrice, int depCount) {
        this.depName = depName;
        this.depId = UUID.randomUUID().toString();  // Generate unique depId using UUID
        this.depKdv = depKdv;
        this.depPrice = depPrice;
        this.depCount = depCount;
    }

    // Getters and Setters
    public String getDepName() {
        return depName;
    }

    public void setDepName(String depName) {
        this.depName = depName;
    }

    public String getDepId() {
        return depId;
    }

    public void setDepId(String depId) {
        this.depId = depId;
    }

    public double getDepKdv() {
        return depKdv;
    }

    public void setDepKdv(double depKdv) {
        this.depKdv = depKdv;
    }

    public double getDepPrice() {
        return depPrice;
    }

    public void setDepPrice(double depPrice) {
        this.depPrice = depPrice;
    }

    public int getDepCount() {
        return depCount;
    }

    public void setDepCount(int depCount) {
        this.depCount = depCount;
    }

    @Override
    public String toString() {
        return "Department{" +
                "depName='" + depName + '\'' +
                ", depId='" + depId + '\'' +
                ", depKdv=" + depKdv +
                ", depPrice=" + depPrice +
                ", depCount=" + depCount +
                '}';
    }
}
