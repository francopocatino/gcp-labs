package com.example.orderservice;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

@Document(collectionName = "orders")
public class Order {

    @DocumentId
    private String id;
    private String customerId;
    private String productId;
    private int quantity;
    private String status;
    private long timestamp;

    public Order() {
    }

    public Order(String customerId, String productId, int quantity, String status) {
        this.customerId = customerId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status;
        this.timestamp = System.currentTimeMillis();
    }

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

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
