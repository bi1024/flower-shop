package com.lamvo.groupproject_flowershop.dao;

public class OrderViewDao {
    private long orderId;
    private String orderStatus;
    private double total;
    private String customerName;

    public OrderViewDao(String orderStatus, double total, String customerName) {

        this.orderStatus = orderStatus;
        this.total = total;
        this.customerName = customerName;
    }

    public OrderViewDao(long orderId, String orderStatus, double total, String customerName) {
        this.orderId = orderId;
        this.orderStatus = orderStatus;
        this.total = total;
        this.customerName = customerName;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public long getOrderId(){return orderId;}
}