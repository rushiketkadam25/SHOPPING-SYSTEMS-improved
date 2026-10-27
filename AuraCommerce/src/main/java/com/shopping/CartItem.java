package com.shopping;

/**
 * Represents an individual line item in the shopping cart.
 */
public class CartItem {
    private final int productId;
    private final String productName;
    private final double unitPrice;
    private int quantity;
    private final int maxStock;
    private final String category;

    public CartItem(int productId, String productName, double unitPrice, int quantity, int maxStock, String category) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.maxStock = maxStock;
        this.category = category;
    }

    public int getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.min(Math.max(1, quantity), maxStock);
    }

    public int getMaxStock() {
        return maxStock;
    }

    public String getCategory() {
        return category;
    }

    public double getSubtotal() {
        return unitPrice * quantity;
    }
}

