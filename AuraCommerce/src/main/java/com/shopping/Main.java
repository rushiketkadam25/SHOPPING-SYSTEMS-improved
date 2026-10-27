/*
 * Decompiled with CFR 0.152.
 */
package com.shopping;

import com.formdev.flatlaf.FlatLightLaf;
import com.shopping.ConnectionDialog;
import com.shopping.CustomerPanel;
import com.shopping.CustomerStorefront;
import com.shopping.DBConnection;
import com.shopping.LoginFrame;
import com.shopping.OrderPanel;
import com.shopping.ProductPanel;
import com.shopping.ReportsPanel;
import com.shopping.ShipperPanel;
import com.shopping.SupplierPanel;
import com.shopping.UIUtils;
import com.shopping.UserSession;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main
extends JFrame {
    private JPanel sidebarPanel;
    private JPanel contentCards;
    private CardLayout cardLayout;
    private JLabel pageTitleLabel;
    private JLabel pageSubLabel;
    private JLabel statusPill;
    private JPanel statsContainer;
    private List<JButton> sidebarButtons = new ArrayList<JButton>();
    private ProductPanel productPanel;
    private OrderPanel orderPanel;
    private CustomerPanel customerPanel;
    private ReportsPanel reportsPanel;
    private CustomerStorefront customerStorefront;
    private SupplierPanel supplierPanel;
    private ShipperPanel shipperPanel;

    public Main() {
        super("AURA COMMERCE - " + UserSession.getRole().getDisplayName());
        this.setDefaultCloseOperation(3);
        this.setSize(1260, 840);
        this.setMinimumSize(new Dimension(1020, 700));
        this.setLocationRelativeTo(null);
        this.getContentPane().setBackground(UIUtils.BG_CANVAS);
        this.setLayout(new BorderLayout());
        this.add((Component)this.createSidebar(), "West");
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setOpaque(false);
        jPanel.add((Component)this.createTopAppBar(), "North");
        this.cardLayout = new CardLayout();
        this.contentCards = new JPanel(this.cardLayout);
        this.contentCards.setOpaque(false);
        this.setupRoleViews();
        jPanel.add((Component)this.contentCards, "Center");
        this.add((Component)jPanel, "Center");
        SwingUtilities.invokeLater(this::refreshStats);
    }

    private JPanel createSidebar() {
        this.sidebarPanel = new JPanel();
        this.sidebarPanel.setPreferredSize(new Dimension(240, 800));
        this.sidebarPanel.setBackground(UIUtils.SIDEBAR_BG);
        this.sidebarPanel.setLayout(new BorderLayout());
        JPanel jPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        jPanel.setOpaque(false);
        jPanel.setBorder(BorderFactory.createEmptyBorder(22, 20, 20, 20));
        JLabel jLabel = new JLabel("\ud83d\udecd\ufe0f AURA COMMERCE");
        jLabel.setFont(new Font("Segoe UI", 1, 18));
        jLabel.setForeground(Color.WHITE);
        JLabel jLabel2 = new JLabel("Enterprise Cloud POS & ERP");
        jLabel2.setFont(new Font("Segoe UI", 0, 11));
        jLabel2.setForeground(new Color(148, 163, 184));
        jPanel.add(jLabel);
        jPanel.add(jLabel2);
        this.sidebarPanel.add((Component)jPanel, "North");
        JPanel jPanel2 = new JPanel();
        jPanel2.setLayout(new BoxLayout(jPanel2, 1));
        jPanel2.setOpaque(false);
        jPanel2.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        this.sidebarButtons.clear();
        switch (UserSession.getRole()) {
            case CUSTOMER: {
                this.addSidebarHeader(jPanel2, "STOREFRONT");
                this.addSidebarItem(jPanel2, "\ud83d\udecd\ufe0f  Product Catalog", "PAGE_STORE", true);
                break;
            }
            case SUPPLIER: {
                this.addSidebarHeader(jPanel2, "SUPPLIER PORTAL");
                this.addSidebarItem(jPanel2, "\ud83c\udfed  Supplied Inventory", "PAGE_SUPPLIER", true);
                break;
            }
            case SHIPPER: {
                this.addSidebarHeader(jPanel2, "LOGISTICS");
                this.addSidebarItem(jPanel2, "\ud83d\ude9a  Delivery Dispatch", "PAGE_SHIPPER", true);
                break;
            }
            default: {
                this.addSidebarHeader(jPanel2, "CATALOG & SALES");
                this.addSidebarItem(jPanel2, "\ud83d\udce6  Product Catalog", "PAGE_PRODUCTS", true);
                this.addSidebarItem(jPanel2, "\ud83d\uded2  Orders & Checkout", "PAGE_ORDERS", false);
                this.addSidebarHeader(jPanel2, "CUSTOMERS & REVIEWS");
                this.addSidebarItem(jPanel2, "\ud83d\udc65  Accounts & Reviews", "PAGE_CUSTOMERS", false);
                this.addSidebarHeader(jPanel2, "INTELLIGENCE");
                this.addSidebarItem(jPanel2, "\ud83d\udcca  Analytics & Views", "PAGE_REPORTS", false);
            }
        }
        this.sidebarPanel.add((Component)jPanel2, "Center");
        JPanel jPanel3 = new JPanel(new BorderLayout(8, 6));
        jPanel3.setOpaque(false);
        jPanel3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(30, 41, 59)), BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        JPanel jPanel4 = new JPanel(new GridLayout(2, 1, 0, 2));
        jPanel4.setOpaque(false);
        JLabel jLabel3 = new JLabel(UserSession.getRole().getIcon() + " " + UserSession.getDisplayName());
        jLabel3.setFont(new Font("Segoe UI", 1, 12));
        jLabel3.setForeground(Color.WHITE);
        JLabel jLabel4 = new JLabel(UserSession.getRole().getDisplayName());
        jLabel4.setFont(new Font("Segoe UI", 0, 11));
        jLabel4.setForeground(new Color(148, 163, 184));
        jPanel4.add(jLabel3);
        jPanel4.add(jLabel4);
        JButton jButton = UIUtils.createButton("\ud83d\udeaa Logout", new Color(30, 41, 59), new Color(241, 245, 249));
        jButton.setPreferredSize(new Dimension(85, 30));
        jButton.addActionListener(actionEvent -> this.logout());
        jPanel3.add((Component)jPanel4, "Center");
        jPanel3.add((Component)jButton, "East");
        this.sidebarPanel.add((Component)jPanel3, "South");
        return this.sidebarPanel;
    }

    private void addSidebarHeader(JPanel jPanel, String string) {
        JLabel jLabel = new JLabel(string);
        jLabel.setFont(new Font("Segoe UI", 1, 10));
        jLabel.setForeground(new Color(100, 116, 139));
        jLabel.setBorder(BorderFactory.createEmptyBorder(10, 8, 4, 8));
        jLabel.setAlignmentX(0.0f);
        jPanel.add(jLabel);
    }

    private void addSidebarItem(JPanel jPanel, String string, String string2, boolean bl) {
        JButton jButton = UIUtils.createSidebarButton(string, bl);
        this.sidebarButtons.add(jButton);
        jButton.addActionListener(actionEvent -> {
            for (JButton jButton2 : this.sidebarButtons) {
                jButton2.setBackground(UIUtils.SIDEBAR_BG);
                jButton2.setForeground(new Color(203, 213, 225));
                jButton2.setFont(new Font("Segoe UI", 0, 13));
            }
            jButton.setBackground(UIUtils.PRIMARY_DARK);
            jButton.setForeground(Color.WHITE);
            jButton.setFont(new Font("Segoe UI", 1, 13));
            this.cardLayout.show(this.contentCards, string2);
            this.updatePageHeader(string);
        });
        jPanel.add(jButton);
        jPanel.add(Box.createVerticalStrut(4));
    }

    private JPanel createTopAppBar() {
        JPanel jPanel = new JPanel(new BorderLayout(14, 10));
        jPanel.setBackground(UIUtils.CARD_BG);
        jPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.CARD_BORDER), BorderFactory.createEmptyBorder(12, 22, 12, 22)));
        JPanel jPanel2 = new JPanel(new GridLayout(2, 1, 0, 2));
        jPanel2.setOpaque(false);
        this.pageTitleLabel = new JLabel("System Overview");
        this.pageTitleLabel.setFont(new Font("Segoe UI", 1, 18));
        this.pageTitleLabel.setForeground(UIUtils.TEXT_DARK);
        this.pageSubLabel = new JLabel("Enterprise Portal \u2022 " + UserSession.getRole().getDisplayName());
        this.pageSubLabel.setFont(new Font("Segoe UI", 0, 12));
        this.pageSubLabel.setForeground(UIUtils.TEXT_MUTED);
        jPanel2.add(this.pageTitleLabel);
        jPanel2.add(this.pageSubLabel);
        jPanel.add((Component)jPanel2, "West");
        this.statsContainer = new JPanel(new FlowLayout(1, 10, 0));
        this.statsContainer.setOpaque(false);
        jPanel.add((Component)this.statsContainer, "Center");
        JPanel jPanel3 = new JPanel(new FlowLayout(2, 10, 4));
        jPanel3.setOpaque(false);
        this.statusPill = new JLabel("\ud83d\udfe1 Checking...");
        this.statusPill.setFont(new Font("Segoe UI", 1, 12));
        this.statusPill.setOpaque(true);
        this.statusPill.setBackground(new Color(241, 245, 249));
        this.statusPill.setForeground(UIUtils.TEXT_DARK);
        this.statusPill.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true), BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        JButton themeBtn = UIUtils.createButton(UIUtils.isDarkMode() ? "\u2600\ufe0f Light" : "\ud83c\udf19 Dark", new Color(241, 245, 249), UIUtils.TEXT_DARK);
        themeBtn.setToolTipText("Toggle Dark / Light Theme");
        themeBtn.addActionListener(actionEvent -> {
            UIUtils.toggleTheme(this);
            themeBtn.setText(UIUtils.isDarkMode() ? "\u2600\ufe0f Light" : "\ud83c\udf19 Dark");
            ToastNotification.show(this, "Theme Switched", "Active theme: " + (UIUtils.isDarkMode() ? "Dark Mode \ud83c\udf19" : "Light Mode \u2600\ufe0f"), ToastNotification.Type.INFO);
        });
        JButton jButton = UIUtils.createButton("\u2699\ufe0f DB Config", new Color(241, 245, 249), UIUtils.TEXT_DARK);
        jButton.addActionListener(actionEvent -> {
            ConnectionDialog connectionDialog = new ConnectionDialog(this);
            connectionDialog.setVisible(true);
            this.refreshAll();
        });
        JButton jButton2 = new JButton("\ud83d\udd04 Refresh");
        jButton2.addActionListener(actionEvent -> this.refreshAll());
        jPanel3.add(this.statusPill);
        jPanel3.add(themeBtn);
        jPanel3.add(jButton);
        jPanel3.add(jButton2);
        jPanel.add((Component)jPanel3, "East");
        return jPanel;
    }

    private void updatePageHeader(String string) {
        this.pageTitleLabel.setText(string.replaceAll("[^a-zA-Z &]", "").trim());
    }

    private void setupRoleViews() {
        switch (UserSession.getRole()) {
            case CUSTOMER: {
                this.customerStorefront = new CustomerStorefront();
                this.contentCards.add((Component)this.customerStorefront, "PAGE_STORE");
                this.updatePageHeader("Product Storefront");
                break;
            }
            case SUPPLIER: {
                this.supplierPanel = new SupplierPanel();
                this.contentCards.add((Component)this.supplierPanel, "PAGE_SUPPLIER");
                this.updatePageHeader("Supplied Inventory");
                break;
            }
            case SHIPPER: {
                this.shipperPanel = new ShipperPanel();
                this.contentCards.add((Component)this.shipperPanel, "PAGE_SHIPPER");
                this.updatePageHeader("Delivery Dispatch");
                break;
            }
            default: {
                this.productPanel = new ProductPanel();
                this.orderPanel = new OrderPanel();
                this.customerPanel = new CustomerPanel();
                this.reportsPanel = new ReportsPanel();
                this.contentCards.add((Component)this.productPanel, "PAGE_PRODUCTS");
                this.contentCards.add((Component)this.orderPanel, "PAGE_ORDERS");
                this.contentCards.add((Component)this.customerPanel, "PAGE_CUSTOMERS");
                this.contentCards.add((Component)this.reportsPanel, "PAGE_REPORTS");
                this.updatePageHeader("Product Catalog");
            }
        }
    }

    public void refreshStats() {
        this.statsContainer.removeAll();
        int n = 0;
        int n2 = 0;
        double d = 0.0;
        int n3 = 0;
        boolean bl = false;
        try {
            Connection connection = DBConnection.getConnection();
            try {
                Statement statement = connection.createStatement();
                try {
                    bl = true;
                    ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM product");
                    try {
                        if (resultSet.next()) {
                            n = resultSet.getInt(1);
                        }
                    }
                    finally {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                    }
                    resultSet = statement.executeQuery("SELECT COUNT(*), COALESCE(SUM(total_amount), 0) FROM orders");
                    try {
                        if (resultSet.next()) {
                            n2 = resultSet.getInt(1);
                            d = resultSet.getDouble(2);
                        }
                    }
                    finally {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                    }
                    resultSet = statement.executeQuery("SELECT COUNT(*) FROM customer");
                    try {
                        if (resultSet.next()) {
                            n3 = resultSet.getInt(1);
                        }
                    }
                    finally {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                    }
                }
                finally {
                    if (statement != null) {
                        statement.close();
                    }
                }
            }
            finally {
                if (connection != null) {
                    connection.close();
                }
            }
        }
        catch (Exception exception) {
            bl = false;
        }
        if (bl) {
            this.statusPill.setText("\ud83d\udfe2 " + DBConnection.host + " (" + DBConnection.database + ")");
            this.statusPill.setBackground(new Color(220, 252, 231));
            this.statusPill.setForeground(new Color(22, 101, 52));
        } else {
            this.statusPill.setText("\ud83d\udd34 Disconnected: " + DBConnection.host);
            this.statusPill.setBackground(new Color(254, 226, 226));
            this.statusPill.setForeground(new Color(153, 27, 27));
        }
        if (UserSession.isAdmin()) {
            this.statsContainer.add(UIUtils.createStatCard("Catalog Items", String.valueOf(n), "Active In Stock", "\ud83d\udce6", UIUtils.PRIMARY));
            this.statsContainer.add(UIUtils.createStatCard("Total Orders", String.valueOf(n2), "Processed", "\ud83d\uded2", UIUtils.WARNING));
            this.statsContainer.add(UIUtils.createStatCard("Gross Sales", "\u20b9" + String.format("%.0f", d), "Settled", "\ud83d\udcb0", UIUtils.SUCCESS));
            this.statsContainer.add(UIUtils.createStatCard("Active Users", String.valueOf(n3), "Accounts", "\ud83d\udc65", UIUtils.PURPLE));
        }
        this.statsContainer.revalidate();
        this.statsContainer.repaint();
    }

    public void refreshAll() {
        this.refreshStats();
        if (this.productPanel != null) {
            this.productPanel.loadCategories();
            this.productPanel.loadProducts();
        }
        if (this.orderPanel != null) {
            this.orderPanel.loadOrders();
        }
        if (this.customerPanel != null) {
            this.customerPanel.loadCustomers();
            this.customerPanel.loadReviews();
        }
        if (this.customerStorefront != null) {
            this.customerStorefront.refreshAllData();
        }
        if (this.supplierPanel != null) {
            this.supplierPanel.loadSupplierProducts();
        }
        if (this.shipperPanel != null) {
            this.shipperPanel.loadShipments();
        }
    }

    private void logout() {
        this.dispose();
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    public static void main(String[] stringArray) {
        try {
            System.setProperty("flatlaf.useWindowDecorations", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "true");
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.thumbArc", 10);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("TabbedPane.showTabSeparators", true);
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);
            UIManager.put("Table.intercellSpacing", new Dimension(0, 1));
        }
        catch (Throwable throwable) {
            System.err.println("Notice: Could not load FlatLaf theme, falling back to default look and feel: " + throwable.getMessage());
        }
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}