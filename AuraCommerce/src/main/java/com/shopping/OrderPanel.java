/*
 * Decompiled with CFR 0.152.
 */
package com.shopping;

import com.shopping.DBConnection;
import com.shopping.UIUtils;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class OrderPanel
extends JPanel {
    private JTable ordersTable;
    private DefaultTableModel ordersModel;
    private JTable itemsTable;
    private DefaultTableModel itemsModel;

    public OrderPanel() {
        this.setLayout(new GridLayout(2, 1, 14, 14));
        this.setBackground(UIUtils.BG_CANVAS);
        this.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JPanel jPanel = UIUtils.createCard("\ud83d\udce6 Global Orders Ledger");
        Object[] objectArray = new String[]{"Order ID", "Customer Name", "Order Date", "Total Amount (INR)", "Order Status", "Courier Carrier"};
        this.ordersModel = new DefaultTableModel(objectArray, 0){

            @Override
            public boolean isCellEditable(int n, int n2) {
                return false;
            }
        };
        this.ordersTable = new JTable(this.ordersModel);
        UIUtils.formatTable(this.ordersTable);
        this.ordersTable.setSelectionMode(0);
        this.ordersTable.getSelectionModel().addListSelectionListener(listSelectionEvent -> {
            if (!listSelectionEvent.getValueIsAdjusting()) {
                this.loadSelectedOrderItems();
            }
        });
        JScrollPane jScrollPane = new JScrollPane(this.ordersTable);
        jScrollPane.setBorder(BorderFactory.createEmptyBorder());
        jPanel.add((Component)jScrollPane, "Center");
        JPanel jPanel2 = new JPanel(new FlowLayout(2, 10, 8));
        jPanel2.setOpaque(false);
        JButton jButton = UIUtils.createButton("\u2605 New Order (Atomic Transaction)", UIUtils.SUCCESS, Color.WHITE);
        JButton jButtonPdf = UIUtils.createButton("\ud83d\udcc4 Export PDF Invoice", UIUtils.PRIMARY, Color.WHITE);
        JButton jButton2 = UIUtils.createButton("\u270f\ufe0f Update Status", UIUtils.PRIMARY_DARK, Color.WHITE);
        JButton jButton3 = UIUtils.createButton("\ud83d\uddd1\ufe0f Delete Order", UIUtils.DANGER, Color.WHITE);
        JButton jButton4 = new JButton("\ud83d\udd04 Refresh");
        jButton.addActionListener(actionEvent -> this.showPlaceOrderDialog());
        jButtonPdf.addActionListener(actionEvent -> {
            int row = this.ordersTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Please select an order from the ledger to generate its PDF invoice.", "Select Order", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int orderId = (Integer)this.ordersModel.getValueAt(row, 0);
            InvoicePdfGenerator.exportOrderToPdf(orderId, this);
        });
        jButton2.addActionListener(actionEvent -> this.onUpdateOrderStatus());
        jButton3.addActionListener(actionEvent -> this.onDeleteOrder());
        jButton4.addActionListener(actionEvent -> this.loadOrders());
        jPanel2.add(jButton);
        jPanel2.add(jButtonPdf);
        jPanel2.add(jButton2);
        jPanel2.add(jButton3);
        jPanel2.add(jButton4);
        jPanel.add((Component)jPanel2, "South");
        this.add(jPanel);
        JPanel jPanel3 = UIUtils.createCard("\ud83d\uded2 Items Breakdown for Selected Order");
        Object[] objectArray2 = new String[]{"Order ID", "Product Name", "Quantity", "Unit Price (INR)", "Line Total (INR)"};
        this.itemsModel = new DefaultTableModel(objectArray2, 0){

            @Override
            public boolean isCellEditable(int n, int n2) {
                return false;
            }
        };
        this.itemsTable = new JTable(this.itemsModel);
        UIUtils.formatTable(this.itemsTable);
        JScrollPane jScrollPane2 = new JScrollPane(this.itemsTable);
        jScrollPane2.setBorder(BorderFactory.createEmptyBorder());
        jPanel3.add((Component)jScrollPane2, "Center");
        this.add(jPanel3);
        SwingUtilities.invokeLater(this::loadOrders);
    }

    public void loadOrders() {
        this.ordersModel.setRowCount(0);
        this.itemsModel.setRowCount(0);
        String string = "SELECT o.order_id, c.name AS customer_name, o.order_date, o.total_amount, o.status, COALESCE(s.carrier, 'Standard') AS carrier FROM orders o LEFT JOIN customer c ON o.customer_id = c.customer_id LEFT JOIN shipping s ON o.shipment_id = s.shipment_id ORDER BY o.order_id DESC";
        try {
            Connection connection = DBConnection.getConnection();
            try {
                Statement statement = connection.createStatement();
                try {
                    ResultSet resultSet = statement.executeQuery(string);
                    try {
                        while (resultSet.next()) {
                            Vector<Object> vector = new Vector<Object>();
                            vector.add(resultSet.getInt("order_id"));
                            vector.add(resultSet.getString("customer_name"));
                            vector.add(resultSet.getString("order_date"));
                            vector.add(String.format("%.2f", resultSet.getDouble("total_amount")));
                            vector.add(resultSet.getString("status"));
                            vector.add(resultSet.getString("carrier"));
                            this.ordersModel.addRow(vector);
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
            // empty catch block
        }
    }

    private void loadSelectedOrderItems() {
        this.itemsModel.setRowCount(0);
        int n = this.ordersTable.getSelectedRow();
        if (n < 0) {
            return;
        }
        int n2 = (Integer)this.ordersModel.getValueAt(n, 0);
        String string = "SELECT oi.order_id, p.name AS product_name, oi.quantity, oi.unit_price FROM order_item oi LEFT JOIN product p ON oi.product_id = p.product_id WHERE oi.order_id = ? ORDER BY oi.product_id";
        try {
            Connection connection = DBConnection.getConnection();
            try {
                PreparedStatement preparedStatement = connection.prepareStatement(string);
                try {
                    preparedStatement.setInt(1, n2);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        while (resultSet.next()) {
                            Vector<Object> vector = new Vector<Object>();
                            int n3 = resultSet.getInt("quantity");
                            double d = resultSet.getDouble("unit_price");
                            vector.add(resultSet.getInt("order_id"));
                            vector.add(resultSet.getString("product_name"));
                            vector.add(n3);
                            vector.add(String.format("%.2f", d));
                            vector.add(String.format("%.2f", (double)n3 * d));
                            this.itemsModel.addRow(vector);
                        }
                    }
                    finally {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                    }
                }
                finally {
                    if (preparedStatement != null) {
                        preparedStatement.close();
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
            // empty catch block
        }
    }

    private void onUpdateOrderStatus() {
        int n = this.ordersTable.getSelectedRow();
        if (n < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order first.");
            return;
        }
        int n2 = (Integer)this.ordersModel.getValueAt(n, 0);
        String string = (String)this.ordersModel.getValueAt(n, 4);
        String[] stringArray = new String[]{"Processing", "Shipped", "Delivered", "Cancelled"};
        JComboBox<String> jComboBox = new JComboBox<String>(stringArray);
        jComboBox.setSelectedItem(string);
        int n3 = JOptionPane.showConfirmDialog(this, jComboBox, "Update Order #" + n2 + " Status", 2);
        if (n3 == 0) {
            String string2 = (String)jComboBox.getSelectedItem();
            String string3 = "UPDATE orders SET status = ? WHERE order_id = ?";
            try {
                Connection connection = DBConnection.getConnection();
                try {
                    PreparedStatement preparedStatement = connection.prepareStatement(string3);
                    try {
                        preparedStatement.setString(1, string2);
                        preparedStatement.setInt(2, n2);
                        preparedStatement.executeUpdate();
                        JOptionPane.showMessageDialog(this, "Order status updated to '" + string2 + "'!");
                        this.loadOrders();
                    }
                    finally {
                        if (preparedStatement != null) {
                            preparedStatement.close();
                        }
                    }
                }
                finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            }
            catch (SQLException sQLException) {
                JOptionPane.showMessageDialog(this, "Error updating order: " + sQLException.getMessage(), "Error", 0);
            }
        }
    }

    private void onDeleteOrder() {
        int n = this.ordersTable.getSelectedRow();
        if (n < 0) {
            JOptionPane.showMessageDialog(this, "Please select an order to delete.");
            return;
        }
        int n2 = (Integer)this.ordersModel.getValueAt(n, 0);
        int n3 = JOptionPane.showConfirmDialog(this, "Delete Order #" + n2 + " and its order items?", "Confirm Delete Order", 0);
        if (n3 == 0) {
            try {
                Connection connection = DBConnection.getConnection();
                try {
                    connection.setAutoCommit(false);
                    try {
                        PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM payment WHERE order_id = ?");
                        try {
                            preparedStatement.setInt(1, n2);
                            preparedStatement.executeUpdate();
                        }
                        finally {
                            if (preparedStatement != null) {
                                preparedStatement.close();
                            }
                        }
                        preparedStatement = connection.prepareStatement("DELETE FROM order_item WHERE order_id = ?");
                        try {
                            preparedStatement.setInt(1, n2);
                            preparedStatement.executeUpdate();
                        }
                        finally {
                            if (preparedStatement != null) {
                                preparedStatement.close();
                            }
                        }
                        preparedStatement = connection.prepareStatement("DELETE FROM orders WHERE order_id = ?");
                        try {
                            preparedStatement.setInt(1, n2);
                            preparedStatement.executeUpdate();
                        }
                        finally {
                            if (preparedStatement != null) {
                                preparedStatement.close();
                            }
                        }
                        connection.commit();
                        JOptionPane.showMessageDialog(this, "Order #" + n2 + " deleted successfully!");
                        this.loadOrders();
                    }
                    catch (SQLException sQLException) {
                        connection.rollback();
                        JOptionPane.showMessageDialog(this, "Rollback occurred during delete:\n" + sQLException.getMessage(), "Delete Failed", 0);
                    }
                }
                finally {
                    if (connection != null) {
                        connection.close();
                    }
                }
            }
            catch (SQLException sQLException) {
                JOptionPane.showMessageDialog(this, "Database connection error: " + sQLException.getMessage());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void showPlaceOrderDialog() {
        JTextField jTextField = new JTextField("1");
        JTextField jTextField2 = new JTextField("1");
        JTextField jTextField3 = new JTextField("1");
        JComboBox<String> jComboBox = new JComboBox<String>(new String[]{"UPI / Instant Pay", "Credit/Debit Card", "Cash on Delivery (COD)", "Net Banking"});
        JPanel jPanel = new JPanel(new GridLayout(4, 2, 8, 8));
        jPanel.add(new JLabel("Customer ID:"));
        jPanel.add(jTextField);
        jPanel.add(new JLabel("Product ID:"));
        jPanel.add(jTextField2);
        jPanel.add(new JLabel("Order Quantity:"));
        jPanel.add(jTextField3);
        jPanel.add(new JLabel("Payment Type:"));
        jPanel.add(jComboBox);
        int n = JOptionPane.showConfirmDialog(this, jPanel, "Create Order (Atomic Multi-Table Transaction)", 2);
        if (n != 0) {
            return;
        }
        Connection connection = null;
        try {
            Object object;
            String string;
            int n2;
            double d;
            String string2;
            int n3;
            int n4;
            int n5;
            block66: {
                n5 = Integer.parseInt(jTextField.getText().trim());
                n4 = Integer.parseInt(jTextField2.getText().trim());
                n3 = Integer.parseInt(jTextField3.getText().trim());
                string2 = (String)jComboBox.getSelectedItem();
                if (n3 <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be > 0!", "Validation Error", 0);
                    return;
                }
                connection = DBConnection.getConnection();
                connection.setAutoCommit(false);
                d = 0.0;
                n2 = 0;
                string = "";
                PreparedStatement preparedStatement = connection.prepareStatement("SELECT name, price, stock_qty FROM product WHERE product_id = ?");
                try {
                    preparedStatement.setInt(1, n4);
                    ResultSet resultSet = preparedStatement.executeQuery();
                    try {
                        if (resultSet.next()) {
                            string = resultSet.getString("name");
                            d = resultSet.getDouble("price");
                            n2 = resultSet.getInt("stock_qty");
                            break block66;
                        }
                        throw new SQLException("Product ID " + n4 + " does not exist!");
                    }
                    finally {
                        if (resultSet != null) {
                            resultSet.close();
                        }
                    }
                }
                finally {
                    if (preparedStatement != null) {
                        preparedStatement.close();
                    }
                }
            }
            if (n2 < n3) {
                throw new SQLException("Insufficient stock! Available: " + n2 + ", Requested: " + n3);
            }
            double d2 = d * (double)n3;
            int n6 = 0;
            String string3 = "INSERT INTO orders (customer_id, shipment_id, total_amount, status) VALUES (?, 1, ?, 'Processing')";
            try (PreparedStatement psOrder = connection.prepareStatement(string3, Statement.RETURN_GENERATED_KEYS)) {
                psOrder.setInt(1, n5);
                psOrder.setDouble(2, d2);
                psOrder.executeUpdate();
                try (ResultSet rsOrder = psOrder.getGeneratedKeys()) {
                    if (rsOrder.next()) {
                        n6 = rsOrder.getInt(1);
                    }
                }
            }
            try (PreparedStatement psItem = connection.prepareStatement("INSERT INTO order_item (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)")) {
                psItem.setInt(1, n6);
                psItem.setInt(2, n4);
                psItem.setInt(3, n3);
                psItem.setDouble(4, d);
                psItem.executeUpdate();
            }
            try (PreparedStatement psStock = connection.prepareStatement("UPDATE product SET stock_qty = stock_qty - ? WHERE product_id = ?")) {
                psStock.setInt(1, n3);
                psStock.setInt(2, n4);
                psStock.executeUpdate();
            }
            try (PreparedStatement psPay = connection.prepareStatement("INSERT INTO payment (order_id, amount, payment_type, status) VALUES (?, ?, ?, 'Completed')")) {
                psPay.setInt(1, n6);
                psPay.setDouble(2, d2);
                psPay.setString(3, string2);
                psPay.executeUpdate();
            }
            connection.commit();
            JOptionPane.showMessageDialog(this, "TRANSACTION COMMITTED ATOMICALLY!\n\n\u2022 Created Order #" + n6 + "\n\u2022 Ordered: " + string + " x" + n3 + "\n\u2022 Total: INR " + String.format("%.2f", d2) + "\n\u2022 Stock reduced by: " + n3 + "\n\u2022 Payment recorded (" + string2 + ")", "Transaction Succeeded", 1);
            this.loadOrders();
        }
        catch (Exception exception) {
            if (connection != null) {
                try {
                    connection.rollback();
                    JOptionPane.showMessageDialog(this, "TRANSACTION ROLLED BACK!\nReason: " + exception.getMessage() + "\n\nNo database changes were saved.", "Transaction Cancelled", 2);
                }
                catch (SQLException sQLException) {
                    sQLException.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + exception.getMessage(), "Error", 0);
            }
        }
        finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                }
                catch (SQLException sQLException) {
                    sQLException.printStackTrace();
                }
            }
        }
    }
}