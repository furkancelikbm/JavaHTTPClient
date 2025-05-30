package model;

public class Department {
    private String depName;
    private double depPrice;
    private double depKdv;
    private int depCount;



    public Department(String depName, double depKdv, double depPrice, int depCount) {
        this.depName = depName;
        this.depKdv = depKdv;
        this.depPrice = depPrice;
        this.depCount = depCount;
    }

    public String getDepName() {
        return depName;
    }

    public void setDepName(String depName) {
        this.depName = depName;
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
        return String.format("%s - Fiyat: %.2f - KDV: %.0f%%", depName, depPrice, depKdv);
    }
}
