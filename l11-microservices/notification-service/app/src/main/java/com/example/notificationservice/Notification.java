package com.example.notificationservice;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

@Document(collectionName = "notifications")
public class Notification {

    @DocumentId
    private String id;
    private String orderId;
    private String customerId;
    private String message;
    private long timestamp;
    private String status;

    public Notification() {
    }

    public Notification(String orderId, String customerId, String message) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
        this.status = "SENT";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
