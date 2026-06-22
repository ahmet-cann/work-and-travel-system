package org.example.model;

public class JobOffer {
    public int    jobID;
    public String positionTitle;
    public double hourlyWage;
    public String city;
    public String state;
    public boolean taken;

    public JobOffer(int jobID, String positionTitle, double hourlyWage, String city, String state, boolean taken) {
        this.jobID         = jobID;
        this.positionTitle = positionTitle;
        this.hourlyWage    = hourlyWage;
        this.city          = city;
        this.state         = state;
        this.taken         = taken;
    }

    @Override
    public String toString() {
        return taken
                ? positionTitle + "  ($" + String.format("%.2f", hourlyWage) + "/hr)  [DOLU]"
                : positionTitle + "  ($" + String.format("%.2f", hourlyWage) + "/hr)";
    }
}