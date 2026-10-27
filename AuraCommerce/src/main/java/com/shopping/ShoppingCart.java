package com.shopping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the multi-item shopping cart state, discounts, and calculation logic.
 */
public class ShoppingCart {
    private static final ShoppingCart instance = new ShoppingCart();
    private final List<CartItem> items = new ArrayList<>();
    private final List<Runnable> listeners = new ArrayList<>();
    private String appliedCoupon = "";

    public static ShoppingCart getInstance() {
        return instance;
    }

    private ShoppingCart() {}

    public synchronized void addOrUpdateItem(int productId, String productName, double price, int qty, int maxStock, String category) {
        for (CartItem item : items) {
            if (item.getProductId() == productId) {
                int newQty = Math.min(item.getQuantity() + qty, maxStock);
                item.setQuantity(newQty);
                notifyListeners();
                return;
            }
        }
        items.add(new CartItem(productId, productName, price, qty, maxStock, category));
        notifyListeners();
    }

    public synchronized void updateQuantity(int productId, int newQty) {
        for (CartItem item : items) {
            if (item.getProductId() == productId) {
                if (newQty <= 0) {
                    items.remove(item);
                } else {
                    item.setQuantity(newQty);
                }
                notifyListeners();
                return;
            }
        }
    }

    public synchronized void removeItem(int productId) {
        items.removeIf(item -> item.getProductId() == productId);
        notifyListeners();
    }

    public synchronized void clear() {
        items.clear();
        appliedCoupon = "";
        notifyListeners();
    }

    public synchronized List<CartItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public synchronized int getTotalQuantity() {
        int sum = 0;
        for (CartItem item : items) {
            sum += item.getQuantity();
        }
        return sum;
    }

    public synchronized int getItemCount() {
        return items.size();
    }

    public synchronized double getSubtotal() {
        double sum = 0.0;
        for (CartItem item : items) {
            sum += item.getSubtotal();
        }
        return sum;
    }

    public synchronized String getAppliedCoupon() {
        return appliedCoupon;
    }

    public synchronized boolean applyCoupon(String code) {
        if (code == null) code = "";
        code = code.trim().toUpperCase();
        if (code.equals("SAVE10") || code.equals("AURA20") || code.equals("FIRST50")) {
            this.appliedCoupon = code;
            notifyListeners();
            return true;
        } else if (code.isEmpty()) {
            this.appliedCoupon = "";
            notifyListeners();
            return true;
        }
        return false;
    }

    public synchronized double getDiscount() {
        double subtotal = getSubtotal();
        if (appliedCoupon.equalsIgnoreCase("SAVE10")) {
            return subtotal * 0.10;
        } else if (appliedCoupon.equalsIgnoreCase("AURA20")) {
            return Math.min(subtotal * 0.20, 500.0);
        } else if (appliedCoupon.equalsIgnoreCase("FIRST50")) {
            return subtotal >= 200 ? 50.0 : 0.0;
        }
        return 0.0;
    }

    public synchronized double getTax(double taxRate) {
        double taxableAmount = Math.max(0.0, getSubtotal() - getDiscount());
        return taxableAmount * taxRate;
    }

    public synchronized double getGrandTotal(double taxRate) {
        double subtotal = getSubtotal();
        double discount = getDiscount();
        double taxable = Math.max(0.0, subtotal - discount);
        double tax = taxable * taxRate;
        return taxable + tax;
    }

    public void addChangeListener(Runnable listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    private void notifyListeners() {
        for (Runnable listener : listeners) {
            try {
                listener.run();
            } catch (Exception ignored) {}
        }
    }
}

