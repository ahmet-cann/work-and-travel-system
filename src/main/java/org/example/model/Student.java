package org.example.model;

public class Student {
    public int    studentID;
    public String firstName;
    public String lastName;
    public String major;
    public String email;

    public Student(int studentID, String firstName, String lastName, String major, String email) {
        this.studentID = studentID;
        this.firstName = firstName;
        this.lastName  = lastName;
        this.major     = major;
        this.email     = email;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}