package org.example.model;

public class Employer {
    public int    employerID;
    public String companyName;
    public String contactPerson;
    public String city;
    public String state;

    public Employer(int employerID, String companyName, String contactPerson, String city, String state) {
        this.employerID    = employerID;
        this.companyName   = companyName;
        this.contactPerson = contactPerson;
        this.city          = city;
        this.state         = state;
    }

    @Override
    public String toString() { return companyName; }
}