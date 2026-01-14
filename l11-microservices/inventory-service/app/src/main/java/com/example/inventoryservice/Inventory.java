package com.example.inventoryservice;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.spring.data.firestore.Document;

@Document(collectionName = "inventory")
public class Inventory {

    @DocumentId
    private String productId;
    private int quantity;

    public Inventory() {
    }

    public Inventory(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
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
}
