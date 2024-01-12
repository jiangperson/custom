package com.example.customer;

import java.time.LocalDate;

public class Customer {
    private String id;
    private String name;
    private String contact;
    private String email;
    private String phoneNumber;
    private LocalDate registrationDate;
    private String companyName;
    private String industry;
    private String status;

    @Override
    public String toString() {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", registrationDate=" + registrationDate +
                ", companyName='" + companyName + '\'' +
                ", industry='" + industry + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public Customer(String id, String name, String contact, String email, String phoneNumber, LocalDate registrationDate, String companyName, String industry, String status) {
        this.id = id;
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.registrationDate = registrationDate;
        this.companyName = companyName;
        this.industry = industry;
        this.status = status;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getEmail() {
        if (email=="")
            return null;
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        if (phoneNumber=="")
            return null;
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getCompanyName() {
        if (companyName=="")
            return null;
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustry() {
        if (industry=="")
            return null;
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getStatus() {
        if (status=="")
            return null;
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}