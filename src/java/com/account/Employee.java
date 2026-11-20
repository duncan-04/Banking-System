package com.account;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;

public class Employee{

    private String employeeId;
    private String companyName;
    private String companyAddress;
    private String occupation;

    public Employee(String employeeId,
                    String companyName,
                    String companyAddress,
                    String occupation) {


        this.employeeId = employeeId;
        this.companyName = companyName;
        this.companyAddress = companyAddress;
        this.occupation = occupation;
    }

    // Getters

    public String getEmployeeId() {
        return employeeId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public String getOccupation() {
        return occupation;
    }

    // Setters

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }
}
