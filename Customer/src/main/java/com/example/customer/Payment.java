package com.example.customer;

import javafx.beans.property.*;

import java.time.LocalDate;

public class Payment {

    private final StringProperty paymentId = new SimpleStringProperty();
    private final StringProperty orderId = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> paymentDate = new SimpleObjectProperty<>();
    private final StringProperty paymentMethod = new SimpleStringProperty();
    private final StringProperty amountPaid = new SimpleStringProperty();

    public Payment(String paymentId, String orderId, LocalDate paymentDate, String paymentMethod, String amountPaid) {
        setPaymentId(paymentId);
        setOrderId(orderId);
        setPaymentDate(paymentDate);
        setPaymentMethod(paymentMethod);
        setAmountPaid(amountPaid);
    }

    // Getter and Setter methods for properties
    public String getPaymentId() {
        return paymentId.get();
    }
    public void setPaymentId(String paymentId) {
        this.paymentId.set(paymentId);
    }

    public String getOrderId() {
        return orderId.get();
    }
    public void setOrderId(String orderId) {
        this.orderId.set(orderId);
    }

    public LocalDate getPaymentDate() {
        return paymentDate.get();
    }
    public ObjectProperty<LocalDate> paymentDateProperty() {
        return paymentDate;
    }
    public void setPaymentDate(LocalDate date) {
        paymentDate.set(date);
    }

    public String getPaymentMethod() {
        return paymentMethod.get();
    }
    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod.set(paymentMethod);
    }

    public String  getAmountPaid() {
        return amountPaid.get();
    }
    public void setAmountPaid(String amountPaid) {
        this.amountPaid.set(amountPaid);
    }
}