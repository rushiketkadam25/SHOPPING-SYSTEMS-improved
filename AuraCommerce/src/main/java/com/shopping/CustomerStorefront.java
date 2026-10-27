package com.shopping;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;

public class CustomerStorefront extends JPanel {
    private final int activeCustomerId;
    private final String activeCustomerName;
    private JTabbedPane mainTabs;

    // Catalog Fields
    private JTable catalogTable;
    private DefaultTableModel catalogModel;
    private JTextField searchField;
    private JComboBox<String> categoryCombo;
    private JLabel selectedProductLabel;
    private JLabel selectedPriceLabel;
    private JLabel selectedStockLabel;
    private JSpinner quantitySpinner;
    private JLabel subtotalLabel;
    private JComboBox<String> paymentMethodBox;
    private JComboBox<String> carrierBox;
    private JButton placeOrderBtn;
    private JButton addToCartBtn;
    private JButton buyNowBtn;

    // Quick Checkout Fields
    private JLabel timerLabel;
    private Timer checkoutTimer;
    private int remainingSeconds = 300;
    private CardLayout paymentCardLayout;
    private JPanel paymentDetailsPanel;
    private JTextField upiField;
    private JTextField cardNumberField;
    private JTextField cardExpiryField;
    private JPasswordField cardCvvField;
    private JComboBox<String> bankSelectBox;

    // Multi-Item Shopping Cart Fields
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel cartSubtotalLabel;
    private JLabel cartDiscountLabel;
    private JLabel cartTaxLabel;
    private JLabel cartGrandTotalLabel;
    private JTextField cartCouponField;
    private JLabel cartCouponStatusLabel;
    private JComboBox<String> cartCarrierBox;
    private JComboBox<String> cartPaymentMethodBox;
    private CardLayout cartPaymentCardLayout;
    private JPanel cartPaymentDetailsPanel;
    private JTextField cartUpiField;
    private JTextField cartCardNumberField;
    private JTextField cartCardExpiryField;
    private JPasswordField cartCardCvvField;
    private JComboBox<String> cartBankSelectBox;
    private JButton cartCheckoutBtn;

    // Orders & Reviews Fields
    private JTable myOrdersTable;
    private DefaultTableModel myOrdersModel;
    private JTable reviewsTable;
    private DefaultTableModel reviewsModel;

    public CustomerStorefront() {
        this.activeCustomerId = UserSession.getAssociatedId() > 0 ? UserSession.getAssociatedId() : 1;
        this.activeCustomerName = UserSession.getDisplayName() != null ? UserSession.getDisplayName() : "Customer";

        this.setLayout(new BorderLayout(14, 14));
        this.setBackground(UIUtils.BG_CANVAS);
        this.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Top Customer Identity Bar
        JPanel topCard = UIUtils.createCard("");
        JPanel identityPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        identityPanel.setOpaque(false);

        JLabel userIconLabel = new JLabel("\ud83d\udc64 Logged in as:");
        userIconLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userIconLabel.setForeground(UIUtils.TEXT_DARK);
        identityPanel.add(userIconLabel);

        JLabel userBadge = new JLabel("  " + this.activeCustomerName + " (Account #" + this.activeCustomerId + ")  ");
        userBadge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        userBadge.setOpaque(true);
        userBadge.setBackground(new Color(224, 231, 255));
        userBadge.setForeground(UIUtils.PRIMARY_DARK);
        userBadge.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(199, 210, 254), 1, true),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        identityPanel.add(userBadge);

        JButton refreshStoreBtn = UIUtils.createButton("\ud83d\udd04 Refresh Store", new Color(241, 245, 249), UIUtils.TEXT_DARK);
        refreshStoreBtn.addActionListener(e -> this.refreshAllData());
        identityPanel.add(refreshStoreBtn);

        topCard.add(identityPanel, BorderLayout.CENTER);
        this.add(topCard, BorderLayout.NORTH);

        // Main Tabbed Navigation
        this.mainTabs = new JTabbedPane();
        this.mainTabs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        this.mainTabs.addTab("  \ud83d\udecd\ufe0f  Product Catalog  ", this.createStorefrontTab());
        this.mainTabs.addTab("  \ud83d\uded2  Shopping Cart (0)  ", this.createCartTab());
        this.mainTabs.addTab("  \ud83d\udce6  My Orders & Live Tracking  ", this.createMyOrdersTab());
        this.mainTabs.addTab("  \u270d\ufe0f  My Reviews & Ratings  ", this.createReviewsTab());
        this.add(this.mainTabs, BorderLayout.CENTER);

        // Register ShoppingCart listener for live badges & dynamic totals
        ShoppingCart.getInstance().addChangeListener(() -> {
            int count = ShoppingCart.getInstance().getTotalQuantity();
            this.mainTabs.setTitleAt(1, "  \ud83d\uded2  Shopping Cart (" + count + ")  ");
            this.refreshCartView();
        });

        SwingUtilities.invokeLater(this::refreshAllData);
    }

    private JPanel createStorefrontTab() {
        JPanel container = new JPanel(new BorderLayout(16, 16));
        container.setOpaque(false);

        // Catalog Card
        JPanel catalogCard = UIUtils.createCard("\ud83d\udecd\ufe0f Master Product Catalog");
        catalogCard.setLayout(new BorderLayout(10, 10));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Search:"));
        this.searchField = new JTextField(12);
        filterPanel.add(this.searchField);
        filterPanel.add(new JLabel("Category:"));
        this.categoryCombo = new JComboBox<>(new String[]{"All Categories"});
        filterPanel.add(this.categoryCombo);

        JButton filterBtn = UIUtils.createButton("Filter", UIUtils.PRIMARY, Color.WHITE);
        JButton resetBtn = new JButton("Reset");
        filterBtn.addActionListener(e -> this.loadCatalog());
        resetBtn.addActionListener(e -> {
            this.searchField.setText("");
            this.categoryCombo.setSelectedIndex(0);
            this.loadCatalog();
        });
        filterPanel.add(filterBtn);
        filterPanel.add(resetBtn);
        catalogCard.add(filterPanel, BorderLayout.NORTH);

        String[] headers = new String[]{"ID", "Product Name", "Description", "Stock", "Price (INR)", "Category"};
        this.catalogModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        this.catalogTable = new JTable(this.catalogModel);
        UIUtils.formatTable(this.catalogTable);
        this.catalogTable.setSelectionMode(0);
        this.catalogTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                this.updateCheckoutCard();
            }
        });
        catalogCard.add(new JScrollPane(this.catalogTable), BorderLayout.CENTER);
        container.add(catalogCard, BorderLayout.CENTER);

        // Right Quick Action / Checkout Sidebar
        JPanel rightCard = UIUtils.createCard("\u26a1 Quick Order / Cart Actions");
        rightCard.setPreferredSize(new Dimension(360, 600));
        rightCard.setLayout(new BoxLayout(rightCard, BoxLayout.Y_AXIS));

        // Reservation Timer Bar
        JPanel timerBar = new JPanel(new BorderLayout());
        timerBar.setOpaque(false);
        timerBar.setMaximumSize(new Dimension(340, 28));
        this.timerLabel = new JLabel("\u23f1\ufe0f Cart Hold: 05:00");
        this.timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        this.timerLabel.setForeground(UIUtils.PRIMARY_DARK);
        JButton resetTimerBtn = new JButton("\u21ba Reset");
        resetTimerBtn.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        resetTimerBtn.setMargin(new java.awt.Insets(1, 6, 1, 6));
        resetTimerBtn.addActionListener(e -> this.resetReservationTimer());
        timerBar.add(this.timerLabel, BorderLayout.WEST);
        timerBar.add(resetTimerBtn, BorderLayout.EAST);

        this.checkoutTimer = new Timer(1000, e -> this.onTimerTick());

        this.selectedProductLabel = new JLabel("Select an item from the catalog");
        this.selectedProductLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        this.selectedPriceLabel = new JLabel("Price: \u20b90.00");
        this.selectedPriceLabel.setForeground(UIUtils.TEXT_MUTED);
        this.selectedStockLabel = new JLabel("Stock: -");
        this.selectedStockLabel.setForeground(UIUtils.TEXT_MUTED);

        this.quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        this.quantitySpinner.setMaximumSize(new Dimension(340, 32));
        this.quantitySpinner.addChangeListener(e -> this.recalculateTotal());

        this.carrierBox = new JComboBox<>(new String[]{"BlueDart Express", "FedEx Priority", "Delhivery Surface", "DTDC Express"});
        this.carrierBox.setMaximumSize(new Dimension(340, 32));

        this.paymentMethodBox = new JComboBox<>(new String[]{"UPI / QR Instant Pay", "Credit / Debit Card", "Cash on Delivery (COD)", "Net Banking"});
        this.paymentMethodBox.setMaximumSize(new Dimension(340, 32));

        this.paymentCardLayout = new CardLayout();
        this.paymentDetailsPanel = new JPanel(this.paymentCardLayout);
        this.paymentDetailsPanel.setOpaque(false);
        this.paymentDetailsPanel.setMaximumSize(new Dimension(340, 75));

        // Payment sub-forms
        JPanel upiPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        upiPanel.setOpaque(false);
        JLabel upiLbl = new JLabel("Enter UPI ID (e.g. user@okhdfcbank):");
        upiLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        upiLbl.setForeground(UIUtils.TEXT_MUTED);
        this.upiField = new JTextField("customer@okhdfcbank");
        upiPanel.add(upiLbl);
        upiPanel.add(this.upiField);

        JPanel cardPanel = new JPanel(new GridLayout(2, 2, 6, 4));
        cardPanel.setOpaque(false);
        this.cardNumberField = new JTextField("4532750012345678");
        this.cardExpiryField = new JTextField("12/28");
        this.cardCvvField = new JPasswordField("123");
        cardPanel.add(new JLabel("Card Number:"));
        cardPanel.add(this.cardNumberField);
        JPanel cardSub = new JPanel(new GridLayout(1, 4, 4, 0));
        cardSub.setOpaque(false);
        cardSub.add(new JLabel("Exp:"));
        cardSub.add(this.cardExpiryField);
        cardSub.add(new JLabel("CVV:"));
        cardSub.add(this.cardCvvField);
        cardPanel.add(new JLabel("Details:"));
        cardPanel.add(cardSub);

        JPanel codPanel = new JPanel(new BorderLayout());
        codPanel.setOpaque(false);
        JLabel codLbl = new JLabel("<html><font color='#10b981'>\u2714 Cash on Delivery eligible.</font><br><font color='#64748b' size='2'>Pay in cash/UPI upon door delivery.</font></html>");
        codPanel.add(codLbl, BorderLayout.CENTER);

        JPanel netPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        netPanel.setOpaque(false);
        JLabel bankLbl = new JLabel("Select Bank:");
        bankLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bankLbl.setForeground(UIUtils.TEXT_MUTED);
        this.bankSelectBox = new JComboBox<>(new String[]{"HDFC Bank", "State Bank of India (SBI)", "ICICI Bank", "Axis Bank", "Kotak Mahindra Bank"});
        netPanel.add(bankLbl);
        netPanel.add(this.bankSelectBox);

        this.paymentDetailsPanel.add(upiPanel, "UPI");
        this.paymentDetailsPanel.add(cardPanel, "CARD");
        this.paymentDetailsPanel.add(codPanel, "COD");
        this.paymentDetailsPanel.add(netPanel, "NET");

        this.paymentMethodBox.addActionListener(e -> {
            int sel = this.paymentMethodBox.getSelectedIndex();
            if (sel == 0) this.paymentCardLayout.show(this.paymentDetailsPanel, "UPI");
            else if (sel == 1) this.paymentCardLayout.show(this.paymentDetailsPanel, "CARD");
            else if (sel == 2) this.paymentCardLayout.show(this.paymentDetailsPanel, "COD");
            else if (sel == 3) this.paymentCardLayout.show(this.paymentDetailsPanel, "NET");
        });

        this.subtotalLabel = new JLabel("Total: \u20b90.00");
        this.subtotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        this.subtotalLabel.setForeground(UIUtils.SUCCESS);

        // Action Buttons
        this.addToCartBtn = UIUtils.createButton("\ud83d\uded2 Add to Cart", UIUtils.PRIMARY_DARK, Color.WHITE);
        this.addToCartBtn.setMaximumSize(new Dimension(340, 36));
        this.addToCartBtn.setEnabled(false);
        this.addToCartBtn.addActionListener(e -> this.addItemToCart(false));

        this.buyNowBtn = UIUtils.createButton("\u26a1 Buy Now (Go to Cart)", new Color(139, 92, 246), Color.WHITE);
        this.buyNowBtn.setMaximumSize(new Dimension(340, 36));
        this.buyNowBtn.setEnabled(false);
        this.buyNowBtn.addActionListener(e -> this.addItemToCart(true));

        this.placeOrderBtn = UIUtils.createButton("\u2605 Instant Single Checkout", UIUtils.SUCCESS, Color.WHITE);
        this.placeOrderBtn.setMaximumSize(new Dimension(340, 36));
        this.placeOrderBtn.setEnabled(false);
        this.placeOrderBtn.addActionListener(e -> this.executeTCLCheckout());

        // Assembly
        rightCard.add(timerBar);
        rightCard.add(Box.createVerticalStrut(8));
        rightCard.add(this.selectedProductLabel);
        rightCard.add(Box.createVerticalStrut(4));
        rightCard.add(this.selectedPriceLabel);
        rightCard.add(this.selectedStockLabel);
        rightCard.add(Box.createVerticalStrut(10));
        rightCard.add(new JLabel("Order Quantity:"));
        rightCard.add(this.quantitySpinner);
        rightCard.add(Box.createVerticalStrut(10));
        rightCard.add(this.addToCartBtn);
        rightCard.add(Box.createVerticalStrut(6));
        rightCard.add(this.buyNowBtn);
        rightCard.add(Box.createVerticalStrut(12));
        rightCard.add(new JLabel("--- Single Item Instant Checkout ---"));
        rightCard.add(Box.createVerticalStrut(8));
        rightCard.add(new JLabel("Delivery Courier:"));
        rightCard.add(this.carrierBox);
        rightCard.add(Box.createVerticalStrut(6));
        rightCard.add(new JLabel("Payment Method:"));
        rightCard.add(this.paymentMethodBox);
        rightCard.add(Box.createVerticalStrut(6));
        rightCard.add(this.paymentDetailsPanel);
        rightCard.add(Box.createVerticalStrut(8));
        rightCard.add(this.subtotalLabel);
        rightCard.add(Box.createVerticalStrut(8));
        rightCard.add(this.placeOrderBtn);

        JScrollPane scroll = new JScrollPane(rightCard);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(375, 520));
        container.add(scroll, BorderLayout.EAST);

        return container;
    }

    private JPanel createCartTab() {
        JPanel container = new JPanel(new BorderLayout(16, 16));
        container.setOpaque(false);

        // Cart Table Card
        JPanel tableCard = UIUtils.createCard("\ud83d\uded2 Shopping Cart Ledger");
        String[] cartHeaders = new String[]{"Product ID", "Item Description", "Category", "Unit Price (\u20b9)", "Quantity", "Item Subtotal (\u20b9)"};
        this.cartModel = new DefaultTableModel(cartHeaders, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        this.cartTable = new JTable(this.cartModel);
        UIUtils.formatTable(this.cartTable);
        this.cartTable.setSelectionMode(0);

        tableCard.add(new JScrollPane(this.cartTable), BorderLayout.CENTER);

        // Cart Buttons Panel
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        actionsPanel.setOpaque(false);

        JButton btnAddQty = UIUtils.createButton("\u2795 Qty (+1)", new Color(241, 245, 249), UIUtils.TEXT_DARK);
        JButton btnSubQty = UIUtils.createButton("\u2796 Qty (-1)", new Color(241, 245, 249), UIUtils.TEXT_DARK);
        JButton btnRemove = UIUtils.createButton("\ud83d\uddd1\ufe0f Remove Item", UIUtils.DANGER, Color.WHITE);
        JButton btnClear = UIUtils.createButton("\ud83e\uddf9 Clear Cart", new Color(241, 245, 249), UIUtils.TEXT_DARK);

        btnAddQty.addActionListener(e -> {
            int row = this.cartTable.getSelectedRow();
            if (row >= 0) {
                int pId = (Integer)this.cartModel.getValueAt(row, 0);
                int currentQty = (Integer)this.cartModel.getValueAt(row, 4);
                ShoppingCart.getInstance().updateQuantity(pId, currentQty + 1);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item from the cart table.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnSubQty.addActionListener(e -> {
            int row = this.cartTable.getSelectedRow();
            if (row >= 0) {
                int pId = (Integer)this.cartModel.getValueAt(row, 0);
                int currentQty = (Integer)this.cartModel.getValueAt(row, 4);
                ShoppingCart.getInstance().updateQuantity(pId, currentQty - 1);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item from the cart table.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnRemove.addActionListener(e -> {
            int row = this.cartTable.getSelectedRow();
            if (row >= 0) {
                int pId = (Integer)this.cartModel.getValueAt(row, 0);
                ShoppingCart.getInstance().removeItem(pId);
                Window win = SwingUtilities.getWindowAncestor(this);
                ToastNotification.show(win, "Item Removed", "Item was removed from your cart.", ToastNotification.Type.INFO);
            } else {
                JOptionPane.showMessageDialog(this, "Please select an item to remove.", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnClear.addActionListener(e -> {
            if (ShoppingCart.getInstance().getItemCount() > 0) {
                ShoppingCart.getInstance().clear();
                Window win = SwingUtilities.getWindowAncestor(this);
                ToastNotification.show(win, "Cart Cleared", "All items have been removed from your cart.", ToastNotification.Type.INFO);
            }
        });

        actionsPanel.add(btnAddQty);
        actionsPanel.add(btnSubQty);
        actionsPanel.add(btnRemove);
        actionsPanel.add(btnClear);
        tableCard.add(actionsPanel, BorderLayout.SOUTH);
        container.add(tableCard, BorderLayout.CENTER);

        // Right Checkout Card
        JPanel checkoutCard = UIUtils.createCard("\ud83d\udcb3 Multi-Item Checkout");
        checkoutCard.setPreferredSize(new Dimension(360, 600));
        checkoutCard.setLayout(new BoxLayout(checkoutCard, BoxLayout.Y_AXIS));

        this.cartSubtotalLabel = new JLabel("Subtotal: \u20b90.00");
        this.cartSubtotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Coupon code box
        JPanel couponPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        couponPanel.setOpaque(false);
        couponPanel.setMaximumSize(new Dimension(340, 36));
        this.cartCouponField = new JTextField(9);
        JButton applyCouponBtn = UIUtils.createButton("Apply", UIUtils.PRIMARY_DARK, Color.WHITE);
        applyCouponBtn.setPreferredSize(new Dimension(75, 30));
        applyCouponBtn.addActionListener(e -> {
            String code = this.cartCouponField.getText().trim();
            boolean ok = ShoppingCart.getInstance().applyCoupon(code);
            Window win = SwingUtilities.getWindowAncestor(this);
            if (ok && !code.isEmpty()) {
                this.cartCouponStatusLabel.setText("\u2714 Applied: " + code.toUpperCase());
                this.cartCouponStatusLabel.setForeground(UIUtils.SUCCESS);
                ToastNotification.show(win, "Coupon Applied", "Discount code " + code.toUpperCase() + " applied successfully!", ToastNotification.Type.SUCCESS);
            } else if (code.isEmpty()) {
                this.cartCouponStatusLabel.setText("Promos: SAVE10, AURA20, FIRST50");
                this.cartCouponStatusLabel.setForeground(UIUtils.TEXT_MUTED);
            } else {
                this.cartCouponStatusLabel.setText("\u2716 Invalid Coupon Code");
                this.cartCouponStatusLabel.setForeground(UIUtils.DANGER);
                ToastNotification.show(win, "Invalid Coupon", "Promo code not recognized.", ToastNotification.Type.WARNING);
            }
        });
        couponPanel.add(new JLabel("Promo:"));
        couponPanel.add(this.cartCouponField);
        couponPanel.add(applyCouponBtn);

        this.cartCouponStatusLabel = new JLabel("Promos: SAVE10, AURA20, FIRST50");
        this.cartCouponStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        this.cartCouponStatusLabel.setForeground(UIUtils.TEXT_MUTED);

        this.cartDiscountLabel = new JLabel("Discount: \u20b90.00");
        this.cartDiscountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        this.cartDiscountLabel.setForeground(UIUtils.TEXT_MUTED);

        this.cartTaxLabel = new JLabel("GST (5%): +\u20b90.00");
        this.cartTaxLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        this.cartTaxLabel.setForeground(UIUtils.TEXT_MUTED);

        this.cartGrandTotalLabel = new JLabel("Grand Total: \u20b90.00");
        this.cartGrandTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        this.cartGrandTotalLabel.setForeground(UIUtils.SUCCESS);

        this.cartCarrierBox = new JComboBox<>(new String[]{"BlueDart Express", "FedEx Priority", "Delhivery Surface", "DTDC Express"});
        this.cartCarrierBox.setMaximumSize(new Dimension(340, 32));

        this.cartPaymentMethodBox = new JComboBox<>(new String[]{"UPI / QR Instant Pay", "Credit / Debit Card", "Cash on Delivery (COD)", "Net Banking"});
        this.cartPaymentMethodBox.setMaximumSize(new Dimension(340, 32));

        this.cartPaymentCardLayout = new CardLayout();
        this.cartPaymentDetailsPanel = new JPanel(this.cartPaymentCardLayout);
        this.cartPaymentDetailsPanel.setOpaque(false);
        this.cartPaymentDetailsPanel.setMaximumSize(new Dimension(340, 75));

        JPanel cartUpiPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        cartUpiPanel.setOpaque(false);
        this.cartUpiField = new JTextField("customer@okhdfcbank");
        cartUpiPanel.add(new JLabel("UPI ID / VPA:"));
        cartUpiPanel.add(this.cartUpiField);

        JPanel cartCardPanel = new JPanel(new GridLayout(2, 2, 6, 4));
        cartCardPanel.setOpaque(false);
        this.cartCardNumberField = new JTextField("4532750012345678");
        this.cartCardExpiryField = new JTextField("12/28");
        this.cartCardCvvField = new JPasswordField("123");
        cartCardPanel.add(new JLabel("Card:"));
        cartCardPanel.add(this.cartCardNumberField);
        JPanel cartSub = new JPanel(new GridLayout(1, 4, 4, 0));
        cartSub.setOpaque(false);
        cartSub.add(new JLabel("Exp:"));
        cartSub.add(this.cartCardExpiryField);
        cartSub.add(new JLabel("CVV:"));
        cartSub.add(this.cartCardCvvField);
        cartCardPanel.add(new JLabel("Auth:"));
        cartCardPanel.add(cartSub);

        JPanel cartCodPanel = new JPanel(new BorderLayout());
        cartCodPanel.setOpaque(false);
        cartCodPanel.add(new JLabel("<html><font color='#10b981'>\u2714 Free COD Available</font></html>"), BorderLayout.CENTER);

        JPanel cartNetPanel = new JPanel(new GridLayout(2, 1, 2, 2));
        cartNetPanel.setOpaque(false);
        this.cartBankSelectBox = new JComboBox<>(new String[]{"HDFC Bank", "State Bank of India (SBI)", "ICICI Bank", "Axis Bank", "Kotak Mahindra Bank"});
        cartNetPanel.add(new JLabel("Select Bank:"));
        cartNetPanel.add(this.cartBankSelectBox);

        this.cartPaymentDetailsPanel.add(cartUpiPanel, "UPI");
        this.cartPaymentDetailsPanel.add(cartCardPanel, "CARD");
        this.cartPaymentDetailsPanel.add(cartCodPanel, "COD");
        this.cartPaymentDetailsPanel.add(cartNetPanel, "NET");

        this.cartPaymentMethodBox.addActionListener(e -> {
            int sel = this.cartPaymentMethodBox.getSelectedIndex();
            if (sel == 0) this.cartPaymentCardLayout.show(this.cartPaymentDetailsPanel, "UPI");
            else if (sel == 1) this.cartPaymentCardLayout.show(this.cartPaymentDetailsPanel, "CARD");
            else if (sel == 2) this.cartPaymentCardLayout.show(this.cartPaymentDetailsPanel, "COD");
            else if (sel == 3) this.cartPaymentCardLayout.show(this.cartPaymentDetailsPanel, "NET");
        });

        this.cartCheckoutBtn = UIUtils.createButton("\u2605 Place Order (Multi-Item TCL Checkout)", UIUtils.SUCCESS, Color.WHITE);
        this.cartCheckoutBtn.setMaximumSize(new Dimension(340, 40));
        this.cartCheckoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        this.cartCheckoutBtn.setEnabled(false);
        this.cartCheckoutBtn.addActionListener(e -> this.executeMultiItemCheckout());

        // Assembly
        checkoutCard.add(this.cartSubtotalLabel);
        checkoutCard.add(Box.createVerticalStrut(8));
        checkoutCard.add(couponPanel);
        checkoutCard.add(this.cartCouponStatusLabel);
        checkoutCard.add(Box.createVerticalStrut(6));
        checkoutCard.add(this.cartDiscountLabel);
        checkoutCard.add(this.cartTaxLabel);
        checkoutCard.add(Box.createVerticalStrut(6));
        checkoutCard.add(this.cartGrandTotalLabel);
        checkoutCard.add(Box.createVerticalStrut(12));
        checkoutCard.add(new JLabel("Delivery Courier:"));
        checkoutCard.add(this.cartCarrierBox);
        checkoutCard.add(Box.createVerticalStrut(8));
        checkoutCard.add(new JLabel("Payment Method:"));
        checkoutCard.add(this.cartPaymentMethodBox);
        checkoutCard.add(Box.createVerticalStrut(6));
        checkoutCard.add(this.cartPaymentDetailsPanel);
        checkoutCard.add(Box.createVerticalStrut(14));
        checkoutCard.add(this.cartCheckoutBtn);

        JScrollPane scroll = new JScrollPane(checkoutCard);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(375, 520));
        container.add(scroll, BorderLayout.EAST);

        return container;
    }

    private JPanel createMyOrdersTab() {
        JPanel card = UIUtils.createCard("\ud83d\udce6 Order History & Shipping Status");
        String[] headers = new String[]{"Order ID", "Date", "Amount (INR)", "Order Status", "Carrier", "Tracking No", "Delivery Status"};
        this.myOrdersModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        this.myOrdersTable = new JTable(this.myOrdersModel);
        UIUtils.formatTable(this.myOrdersTable);
        card.add(new JScrollPane(this.myOrdersTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actions.setOpaque(false);

        JButton viewInvoiceBtn = UIUtils.createButton("\ud83d\udcc4 Download / View PDF Invoice", UIUtils.PRIMARY_DARK, Color.WHITE);
        viewInvoiceBtn.addActionListener(e -> {
            int row = this.myOrdersTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an order from your history to download its invoice.", "Select Order", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int orderId = (Integer)this.myOrdersModel.getValueAt(row, 0);
            InvoicePdfGenerator.exportOrderToPdf(orderId, this);
        });

        JButton refreshOrdersBtn = UIUtils.createButton("\ud83d\udd04 Refresh Orders", new Color(241, 245, 249), UIUtils.TEXT_DARK);
        refreshOrdersBtn.addActionListener(e -> this.loadMyOrders());

        actions.add(viewInvoiceBtn);
        actions.add(refreshOrdersBtn);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createReviewsTab() {
        JPanel card = UIUtils.createCard("\u270d\ufe0f Verified Customer Reviews & Feedback");
        String[] headers = new String[]{"Review ID", "Product", "Rating", "Comment", "Review Date"};
        this.reviewsModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        this.reviewsTable = new JTable(this.reviewsModel);
        UIUtils.formatTable(this.reviewsTable);
        card.add(new JScrollPane(this.reviewsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        actions.setOpaque(false);

        JButton addReviewBtn = UIUtils.createButton("\u270d\ufe0f Write New Review", UIUtils.PRIMARY, Color.WHITE);
        addReviewBtn.addActionListener(e -> this.showAddReviewDialog());
        actions.add(addReviewBtn);

        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private void addItemToCart(boolean goToCart) {
        int row = this.catalogTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product from the catalog first.", "Select Product", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int productId = (Integer)this.catalogModel.getValueAt(row, 0);
        String name = (String)this.catalogModel.getValueAt(row, 1);
        int stock = (Integer)this.catalogModel.getValueAt(row, 3);
        double price = Double.parseDouble((String)this.catalogModel.getValueAt(row, 4));
        String category = (String)this.catalogModel.getValueAt(row, 5);
        int qty = (Integer)this.quantitySpinner.getValue();

        if (stock <= 0) {
            Window win = SwingUtilities.getWindowAncestor(this);
            ToastNotification.show(win, "Out of Stock", "'" + name + "' is currently out of stock.", ToastNotification.Type.ERROR);
            return;
        }
        if (qty > stock) {
            Window win = SwingUtilities.getWindowAncestor(this);
            ToastNotification.show(win, "Stock Limit", "Only " + stock + " units available in inventory.", ToastNotification.Type.WARNING);
            return;
        }

        ShoppingCart.getInstance().addOrUpdateItem(productId, name, price, qty, stock, category);
        Window win = SwingUtilities.getWindowAncestor(this);
        ToastNotification.show(win, "Added to Cart", name + " (x" + qty + ") added to your shopping cart!", ToastNotification.Type.SUCCESS);

        if (goToCart) {
            this.mainTabs.setSelectedIndex(1);
        }
    }

    private void refreshCartView() {
        if (this.cartModel == null) return;
        this.cartModel.setRowCount(0);
        ShoppingCart cart = ShoppingCart.getInstance();
        for (CartItem item : cart.getItems()) {
            Vector<Object> row = new Vector<>();
            row.add(item.getProductId());
            row.add(item.getProductName());
            row.add(item.getCategory());
            row.add(String.format("%.2f", item.getUnitPrice()));
            row.add(item.getQuantity());
            row.add(String.format("%.2f", item.getSubtotal()));
            this.cartModel.addRow(row);
        }
        double subtotal = cart.getSubtotal();
        double discount = cart.getDiscount();
        double tax = cart.getTax(0.05);
        double grandTotal = cart.getGrandTotal(0.05);

        if (this.cartSubtotalLabel != null) {
            this.cartSubtotalLabel.setText("Subtotal: \u20b9" + String.format("%.2f", subtotal));
        }
        if (this.cartDiscountLabel != null) {
            if (discount > 0) {
                this.cartDiscountLabel.setText("Discount (" + cart.getAppliedCoupon() + "): -\u20b9" + String.format("%.2f", discount));
                this.cartDiscountLabel.setForeground(UIUtils.SUCCESS);
            } else {
                this.cartDiscountLabel.setText("Discount: \u20b90.00");
                this.cartDiscountLabel.setForeground(UIUtils.TEXT_MUTED);
            }
        }
        if (this.cartTaxLabel != null) {
            this.cartTaxLabel.setText("GST (5%): +\u20b9" + String.format("%.2f", tax));
        }
        if (this.cartGrandTotalLabel != null) {
            this.cartGrandTotalLabel.setText("Grand Total: \u20b9" + String.format("%.2f", grandTotal));
        }
        if (this.cartCheckoutBtn != null) {
            this.cartCheckoutBtn.setEnabled(cart.getItemCount() > 0);
        }
    }

    private void resetReservationTimer() {
        this.remainingSeconds = 300;
        if (this.timerLabel != null) {
            this.timerLabel.setText("\u23f1\ufe0f Cart Hold: 05:00");
            this.timerLabel.setForeground(UIUtils.PRIMARY_DARK);
        }
        if (this.checkoutTimer != null && !this.checkoutTimer.isRunning()) {
            this.checkoutTimer.start();
        }
    }

    private void onTimerTick() {
        this.remainingSeconds--;
        if (this.remainingSeconds < 0) {
            this.remainingSeconds = 0;
            this.checkoutTimer.stop();
            if (this.timerLabel != null) {
                this.timerLabel.setText("\u23f1\ufe0f Cart Hold: EXPIRED");
                this.timerLabel.setForeground(UIUtils.DANGER);
            }
            if (this.placeOrderBtn != null) {
                this.placeOrderBtn.setEnabled(false);
            }
            return;
        }
        int min = this.remainingSeconds / 60;
        int sec = this.remainingSeconds % 60;
        String timeStr = String.format("%02d:%02d", min, sec);
        if (this.timerLabel != null) {
            this.timerLabel.setText("\u23f1\ufe0f Cart Hold: " + timeStr);
            if (this.remainingSeconds <= 60) {
                this.timerLabel.setForeground(UIUtils.DANGER);
            } else if (this.remainingSeconds <= 120) {
                this.timerLabel.setForeground(UIUtils.WARNING);
            } else {
                this.timerLabel.setForeground(UIUtils.PRIMARY_DARK);
            }
        }
    }

    private void updateCheckoutCard() {
        int row = this.catalogTable.getSelectedRow();
        if (row < 0) {
            this.selectedProductLabel.setText("Select an item from the catalog");
            this.selectedPriceLabel.setText("Price: \u20b90.00");
            this.selectedStockLabel.setText("Stock: -");
            this.subtotalLabel.setText("Total: \u20b90.00");
            this.placeOrderBtn.setEnabled(false);
            this.addToCartBtn.setEnabled(false);
            this.buyNowBtn.setEnabled(false);
            if (this.checkoutTimer != null) {
                this.checkoutTimer.stop();
            }
            if (this.timerLabel != null) {
                this.timerLabel.setText("\u23f1\ufe0f Cart Hold: --:--");
                this.timerLabel.setForeground(UIUtils.TEXT_MUTED);
            }
            return;
        }

        String name = (String)this.catalogModel.getValueAt(row, 1);
        int stock = (Integer)this.catalogModel.getValueAt(row, 3);
        double price = Double.parseDouble((String)this.catalogModel.getValueAt(row, 4));

        this.selectedProductLabel.setText(name);
        this.selectedPriceLabel.setText("Unit Price: \u20b9" + String.format("%.2f", price));
        this.selectedStockLabel.setText("Available In Stock: " + stock + " units");
        this.quantitySpinner.setModel(new SpinnerNumberModel(1, 1, Math.max(1, stock), 1));
        this.placeOrderBtn.setEnabled(stock > 0);
        this.addToCartBtn.setEnabled(stock > 0);
        this.buyNowBtn.setEnabled(stock > 0);
        this.recalculateTotal();
        if (stock > 0) {
            this.resetReservationTimer();
        }
    }

    private void recalculateTotal() {
        int row = this.catalogTable.getSelectedRow();
        if (row < 0) return;
        double price = Double.parseDouble((String)this.catalogModel.getValueAt(row, 4));
        int qty = (Integer)this.quantitySpinner.getValue();
        double total = price * qty;
        this.subtotalLabel.setText("Total: \u20b9" + String.format("%.2f", total));
    }

    private void executeTCLCheckout() {
        int row = this.catalogTable.getSelectedRow();
        if (row < 0) return;
        if (this.remainingSeconds <= 0) {
            JOptionPane.showMessageDialog(this, "Your checkout reservation has expired!\nPlease click '\u21ba Reset' to continue.", "Session Expired", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int productId = (Integer)this.catalogModel.getValueAt(row, 0);
        String pName = (String)this.catalogModel.getValueAt(row, 1);
        int stock = (Integer)this.catalogModel.getValueAt(row, 3);
        double unitPrice = Double.parseDouble((String)this.catalogModel.getValueAt(row, 4));
        int qty = (Integer)this.quantitySpinner.getValue();
        String carrier = (String)this.carrierBox.getSelectedItem();

        if (qty > stock) {
            JOptionPane.showMessageDialog(this, "Not enough stock! Available: " + stock);
            return;
        }

        // Validate payment
        int selPayment = this.paymentMethodBox.getSelectedIndex();
        String paymentTypeStr = "Cash on Delivery";
        if (selPayment == 0) {
            String upi = this.upiField.getText().trim();
            if (upi.isEmpty() || !upi.contains("@") || upi.startsWith("@") || upi.endsWith("@")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid UPI ID (e.g. user@okhdfcbank)!", "Invalid UPI ID", JOptionPane.WARNING_MESSAGE);
                this.upiField.requestFocus();
                return;
            }
            paymentTypeStr = "UPI: " + upi;
        } else if (selPayment == 1) {
            String cardNum = this.cardNumberField.getText().trim().replaceAll("\\s+", "");
            if (cardNum.length() < 12 || !cardNum.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid Card Number (12 to 19 digits)!", "Invalid Card", JOptionPane.WARNING_MESSAGE);
                this.cardNumberField.requestFocus();
                return;
            }
            String exp = this.cardExpiryField.getText().trim();
            if (!exp.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Please enter Expiry in MM/YY format (e.g. 12/28)!", "Invalid Expiry", JOptionPane.WARNING_MESSAGE);
                this.cardExpiryField.requestFocus();
                return;
            }
            String cvv = new String(this.cardCvvField.getPassword()).trim();
            if (!cvv.matches("\\d{3,4}")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid 3 or 4 digit CVV code!", "Invalid CVV", JOptionPane.WARNING_MESSAGE);
                this.cardCvvField.requestFocus();
                return;
            }
            String last4 = cardNum.length() > 4 ? cardNum.substring(cardNum.length() - 4) : cardNum;
            paymentTypeStr = "Card: ****" + last4;
        } else if (selPayment == 2) {
            paymentTypeStr = "Cash on Delivery (COD)";
        } else {
            paymentTypeStr = "Net Banking: " + this.bankSelectBox.getSelectedItem();
        }

        if (paymentTypeStr.length() > 50) {
            paymentTypeStr = paymentTypeStr.substring(0, 50);
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            double totalAmt = unitPrice * qty;
            int shipmentId = 1;
            String trackingNo = "TRK" + (System.currentTimeMillis() % 1000000L);

            try (PreparedStatement psShip = conn.prepareStatement("INSERT INTO shipping (address_id, carrier, tracking_no, status) VALUES (1, ?, ?, 'Pending')", Statement.RETURN_GENERATED_KEYS)) {
                psShip.setString(1, carrier);
                psShip.setString(2, trackingNo);
                psShip.executeUpdate();
                try (ResultSet rs = psShip.getGeneratedKeys()) {
                    if (rs.next()) shipmentId = rs.getInt(1);
                }
            }

            int orderId = 0;
            try (PreparedStatement psOrder = conn.prepareStatement("INSERT INTO orders (customer_id, shipment_id, total_amount, status) VALUES (?, ?, ?, 'Processing')", Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, this.activeCustomerId);
                psOrder.setInt(2, shipmentId);
                psOrder.setDouble(3, totalAmt);
                psOrder.executeUpdate();
                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) orderId = rs.getInt(1);
                }
            }

            try (PreparedStatement psItem = conn.prepareStatement("INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)")) {
                psItem.setInt(1, orderId);
                psItem.setInt(2, productId);
                psItem.setInt(3, qty);
                psItem.setDouble(4, unitPrice);
                psItem.executeUpdate();
            }

            try (PreparedStatement psStock = conn.prepareStatement("UPDATE product SET stock_qty = stock_qty - ? WHERE product_id = ?")) {
                psStock.setInt(1, qty);
                psStock.setInt(2, productId);
                psStock.executeUpdate();
            }

            try (PreparedStatement psPay = conn.prepareStatement("INSERT INTO payment (order_id, amount, payment_type, status) VALUES (?, ?, ?, 'Completed')")) {
                psPay.setInt(1, orderId);
                psPay.setDouble(2, totalAmt);
                psPay.setString(3, paymentTypeStr);
                psPay.executeUpdate();
            }

            conn.commit();

            if (this.checkoutTimer != null) {
                this.checkoutTimer.stop();
            }
            this.cardCvvField.setText("");

            Window win = SwingUtilities.getWindowAncestor(this);
            ToastNotification.show(win, "Order Placed Successfully", "Order #" + orderId + " placed for \u20b9" + String.format("%.2f", totalAmt), ToastNotification.Type.SUCCESS);

            // Auto-generate PDF Invoice
            InvoicePdfGenerator.exportOrderToPdf(orderId, this);

            this.refreshAllData();
            this.mainTabs.setSelectedIndex(2);

        } catch (Exception ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignore) {}
            }
            JOptionPane.showMessageDialog(this, "Order Transaction ROLLED BACK:\n" + ex.getMessage(), "Order Failed", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignore) {}
            }
        }
    }

    private void executeMultiItemCheckout() {
        ShoppingCart cart = ShoppingCart.getInstance();
        if (cart.getItemCount() == 0) {
            Window win = SwingUtilities.getWindowAncestor(this);
            ToastNotification.show(win, "Empty Cart", "Your cart is empty. Add items from the catalog.", ToastNotification.Type.WARNING);
            return;
        }

        int selPayment = this.cartPaymentMethodBox.getSelectedIndex();
        String paymentTypeStr = "Cash on Delivery";
        if (selPayment == 0) { // UPI
            String upi = this.cartUpiField.getText().trim();
            if (upi.isEmpty() || !upi.contains("@") || upi.startsWith("@") || upi.endsWith("@")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid UPI ID (e.g. user@okhdfcbank)!", "Invalid UPI ID", JOptionPane.WARNING_MESSAGE);
                this.cartUpiField.requestFocus();
                return;
            }
            paymentTypeStr = "UPI: " + upi;
        } else if (selPayment == 1) { // Card
            String cardNum = this.cartCardNumberField.getText().trim().replaceAll("\\s+", "");
            if (cardNum.length() < 12 || !cardNum.matches("\\d+")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid Card Number (12 to 19 digits)!", "Invalid Card", JOptionPane.WARNING_MESSAGE);
                this.cartCardNumberField.requestFocus();
                return;
            }
            String exp = this.cartCardExpiryField.getText().trim();
            if (!exp.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                JOptionPane.showMessageDialog(this, "Please enter Expiry in MM/YY format (e.g. 12/28)!", "Invalid Expiry", JOptionPane.WARNING_MESSAGE);
                this.cartCardExpiryField.requestFocus();
                return;
            }
            String cvv = new String(this.cartCardCvvField.getPassword()).trim();
            if (!cvv.matches("\\d{3,4}")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid 3 or 4 digit CVV code!", "Invalid CVV", JOptionPane.WARNING_MESSAGE);
                this.cartCardCvvField.requestFocus();
                return;
            }
            String last4 = cardNum.length() > 4 ? cardNum.substring(cardNum.length() - 4) : cardNum;
            paymentTypeStr = "Card: ****" + last4;
        } else if (selPayment == 2) {
            paymentTypeStr = "Cash on Delivery (COD)";
        } else {
            paymentTypeStr = "Net Banking: " + this.cartBankSelectBox.getSelectedItem();
        }

        if (paymentTypeStr.length() > 50) {
            paymentTypeStr = paymentTypeStr.substring(0, 50);
        }

        String selectedCarrier = (String)this.cartCarrierBox.getSelectedItem();
        double grandTotal = cart.getGrandTotal(0.05);

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Verify stock for each cart item
            for (CartItem item : cart.getItems()) {
                try (PreparedStatement psStock = conn.prepareStatement("SELECT stock_qty, name FROM product WHERE product_id = ?")) {
                    psStock.setInt(1, item.getProductId());
                    try (ResultSet rs = psStock.executeQuery()) {
                        if (rs.next()) {
                            int stock = rs.getInt("stock_qty");
                            if (stock < item.getQuantity()) {
                                throw new SQLException("Insufficient stock for '" + rs.getString("name") + "' (Available: " + stock + ", requested: " + item.getQuantity() + ")");
                            }
                        } else {
                            throw new SQLException("Product ID " + item.getProductId() + " not found.");
                        }
                    }
                }
            }

            // 2. Insert shipping
            int shipmentId = 1;
            String trackingNo = "TRK" + (System.currentTimeMillis() % 1000000L);
            try (PreparedStatement psShip = conn.prepareStatement("INSERT INTO shipping (address_id, carrier, tracking_no, status) VALUES (1, ?, ?, 'Pending')", Statement.RETURN_GENERATED_KEYS)) {
                psShip.setString(1, selectedCarrier);
                psShip.setString(2, trackingNo);
                psShip.executeUpdate();
                try (ResultSet rs = psShip.getGeneratedKeys()) {
                    if (rs.next()) {
                        shipmentId = rs.getInt(1);
                    }
                }
            }

            // 3. Insert order
            int orderId = 0;
            try (PreparedStatement psOrder = conn.prepareStatement("INSERT INTO orders (customer_id, shipment_id, total_amount, status) VALUES (?, ?, ?, 'Processing')", Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, this.activeCustomerId);
                psOrder.setInt(2, shipmentId);
                psOrder.setDouble(3, grandTotal);
                psOrder.executeUpdate();
                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        orderId = rs.getInt(1);
                    }
                }
            }

            // 4. Insert each order_item and decrement stock
            for (CartItem item : cart.getItems()) {
                try (PreparedStatement psItem = conn.prepareStatement("INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)")) {
                    psItem.setInt(1, orderId);
                    psItem.setInt(2, item.getProductId());
                    psItem.setInt(3, item.getQuantity());
                    psItem.setDouble(4, item.getUnitPrice());
                    psItem.executeUpdate();
                }
                try (PreparedStatement psDec = conn.prepareStatement("UPDATE product SET stock_qty = stock_qty - ? WHERE product_id = ?")) {
                    psDec.setInt(1, item.getQuantity());
                    psDec.setInt(2, item.getProductId());
                    psDec.executeUpdate();
                }
            }

            // 5. Insert payment
            try (PreparedStatement psPay = conn.prepareStatement("INSERT INTO payment (order_id, amount, payment_type, status) VALUES (?, ?, ?, 'Completed')")) {
                psPay.setInt(1, orderId);
                psPay.setDouble(2, grandTotal);
                psPay.setString(3, paymentTypeStr);
                psPay.executeUpdate();
            }

            conn.commit();

            // Clear cart
            cart.clear();
            this.cartCardCvvField.setText("");

            Window win = SwingUtilities.getWindowAncestor(this);
            ToastNotification.show(win, "Order Confirmed!", "Order #" + orderId + " placed successfully. Total: \u20b9" + String.format("%.2f", grandTotal), ToastNotification.Type.SUCCESS);

            // Export and open PDF Invoice
            InvoicePdfGenerator.exportOrderToPdf(orderId, this);

            this.refreshAllData();
            this.mainTabs.setSelectedIndex(2); // Switch to My Orders tab
        } catch (Exception ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignore) {}
            }
            JOptionPane.showMessageDialog(this, "Order Transaction Failed & Rolled Back:\n" + ex.getMessage(), "Order Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception ignore) {}
            }
        }
    }

    public void refreshAllData() {
        this.loadCategories();
        this.loadCatalog();
        this.loadMyOrders();
        this.loadReviews();
        this.refreshCartView();
    }

    private void loadCategories() {
        this.categoryCombo.removeAllItems();
        this.categoryCombo.addItem("All Categories");
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM category ORDER BY name")) {
            while (rs.next()) {
                this.categoryCombo.addItem(rs.getString("name"));
            }
        } catch (Exception ignored) {}
    }

    private void loadCatalog() {
        this.catalogModel.setRowCount(0);
        StringBuilder sb = new StringBuilder("SELECT p.product_id, p.name, p.description, p.stock_qty, p.price, c.name AS category_name FROM product p LEFT JOIN category c ON p.category_id = c.category_id WHERE 1=1 ");
        String search = this.searchField.getText().trim();
        String cat = (String)this.categoryCombo.getSelectedItem();
        if (!search.isEmpty()) {
            sb.append("AND p.name LIKE ? ");
        }
        if (cat != null && !cat.equals("All Categories")) {
            sb.append("AND c.name = ? ");
        }
        sb.append("ORDER BY p.product_id");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sb.toString())) {
            int idx = 1;
            if (!search.isEmpty()) {
                ps.setString(idx++, "%" + search + "%");
            }
            if (cat != null && !cat.equals("All Categories")) {
                ps.setString(idx++, cat);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("product_id"));
                    row.add(rs.getString("name"));
                    row.add(rs.getString("description"));
                    row.add(rs.getInt("stock_qty"));
                    row.add(String.format("%.2f", rs.getDouble("price")));
                    row.add(rs.getString("category_name"));
                    this.catalogModel.addRow(row);
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadMyOrders() {
        this.myOrdersModel.setRowCount(0);
        String sql = "SELECT o.order_id, o.order_date, o.total_amount, o.status AS order_status, "
                   + "COALESCE(s.carrier, 'Standard') AS carrier, "
                   + "COALESCE(s.tracking_no, 'Pending') AS tracking_no, "
                   + "COALESCE(s.status, 'Pending') AS shipping_status "
                   + "FROM orders o "
                   + "LEFT JOIN shipping s ON o.shipment_id = s.shipment_id "
                   + "WHERE o.customer_id = ? "
                   + "ORDER BY o.order_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.activeCustomerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("order_id"));
                    row.add(rs.getString("order_date"));
                    row.add(String.format("%.2f", rs.getDouble("total_amount")));
                    row.add(rs.getString("order_status"));
                    row.add(rs.getString("carrier"));
                    row.add(rs.getString("tracking_no"));
                    row.add(rs.getString("shipping_status"));
                    this.myOrdersModel.addRow(row);
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadReviews() {
        this.reviewsModel.setRowCount(0);
        String sql = "SELECT r.review_id, p.name AS product_name, r.rating, r.comment, r.review_date "
                   + "FROM review r "
                   + "LEFT JOIN product p ON r.product_id = p.product_id "
                   + "WHERE r.customer_id = ? "
                   + "ORDER BY r.review_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, this.activeCustomerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vector<Object> row = new Vector<>();
                    row.add(rs.getInt("review_id"));
                    row.add(rs.getString("product_name"));
                    row.add(rs.getInt("rating") + " \u2605");
                    row.add(rs.getString("comment"));
                    row.add(rs.getString("review_date"));
                    this.reviewsModel.addRow(row);
                }
            }
        } catch (Exception ignored) {}
    }

    private void showAddReviewDialog() {
        JTextField prodIdField = new JTextField("1");
        JComboBox<Integer> ratingCombo = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5});
        ratingCombo.setSelectedItem(5);
        JTextField commentField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        panel.add(new JLabel("Product ID:"));
        panel.add(prodIdField);
        panel.add(new JLabel("Rating (1-5):"));
        panel.add(ratingCombo);
        panel.add(new JLabel("Comment:"));
        panel.add(commentField);

        int choice = JOptionPane.showConfirmDialog(this, panel, "Submit Review for Customer #" + this.activeCustomerId, JOptionPane.OK_CANCEL_OPTION);
        if (choice == JOptionPane.OK_OPTION) {
            String sql = "INSERT INTO review (customer_id, product_id, rating, comment) VALUES (?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, this.activeCustomerId);
                ps.setInt(2, Integer.parseInt(prodIdField.getText().trim()));
                ps.setInt(3, (Integer)ratingCombo.getSelectedItem());
                ps.setString(4, commentField.getText().trim());
                ps.executeUpdate();
                Window win = SwingUtilities.getWindowAncestor(this);
                ToastNotification.show(win, "Review Submitted", "Thank you for reviewing!", ToastNotification.Type.SUCCESS);
                this.loadReviews();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to submit review:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
