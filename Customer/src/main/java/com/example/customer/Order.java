package com.example.customer;
import java.time.LocalDate;

public class Order {

    private String id;
    private String customerId;
    private LocalDate orderDate;
    private String productName;
    private Integer quantity;
    private Double amount;

    public Order(String id, String customerId, LocalDate orderDate, String productName, Integer quantity, Double amount) {
        this.id = id;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.productName = productName;
        this.quantity = quantity;
        this.amount = amount;
    }

    // Getter和Setter方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}