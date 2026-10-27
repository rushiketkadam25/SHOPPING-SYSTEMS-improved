package com.shopping;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Pure Java PDF 1.4 compliant Invoice Generator.
 * Generates official, beautifully styled enterprise tax invoices with zero external dependencies.
 */
public class InvoicePdfGenerator {

    public static class InvoiceItem {
        public final String name;
        public final int quantity;
        public final double unitPrice;
        public final double lineTotal;

        public InvoiceItem(String name, int quantity, double unitPrice, double lineTotal) {
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.lineTotal = lineTotal;
        }
    }

    public static class InvoiceData {
        public int orderId;
        public String orderDate;
        public String customerName;
        public int customerId;
        public String carrier;
        public String trackingNo;
        public String paymentMethod;
        public String paymentStatus;
        public double subtotal;
        public double discount;
        public double tax;
        public double grandTotal;
        public List<InvoiceItem> items = new ArrayList<>();
    }

    public static void generateAndOpen(InvoiceData data, File destinationFile) throws IOException {
        generate(data, destinationFile);
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            try {
                Desktop.getDesktop().open(destinationFile);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Loads an order from the database and generates a PDF invoice, opening it immediately.
     */
    public static void exportOrderToPdf(int orderId, java.awt.Component parent) {
        try {
            InvoiceData invoice = new InvoiceData();
            invoice.orderId = orderId;

            try (java.sql.Connection conn = DBConnection.getConnection()) {
                String sqlOrder = "SELECT o.order_id, o.order_date, o.total_amount, o.status AS order_status, "
                                + "c.customer_id, c.name AS customer_name, "
                                + "COALESCE(s.carrier, 'Standard') AS carrier, "
                                + "COALESCE(s.tracking_no, 'TRK-PENDING') AS tracking_no, "
                                + "COALESCE(p.payment_type, 'Prepaid') AS payment_type, "
                                + "COALESCE(p.status, 'Completed') AS payment_status "
                                + "FROM orders o "
                                + "JOIN customer c ON o.customer_id = c.customer_id "
                                + "LEFT JOIN shipping s ON o.shipment_id = s.shipment_id "
                                + "LEFT JOIN payment p ON o.order_id = p.order_id "
                                + "WHERE o.order_id = ?";
                try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlOrder)) {
                    ps.setInt(1, orderId);
                    try (java.sql.ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            invoice.orderDate = rs.getString("order_date");
                            invoice.customerName = rs.getString("customer_name");
                            invoice.customerId = rs.getInt("customer_id");
                            invoice.carrier = rs.getString("carrier");
                            invoice.trackingNo = rs.getString("tracking_no");
                            invoice.paymentMethod = rs.getString("payment_type");
                            invoice.paymentStatus = rs.getString("payment_status");
                            invoice.grandTotal = rs.getDouble("total_amount");
                        } else {
                            javax.swing.JOptionPane.showMessageDialog(parent, "Order #" + orderId + " not found in database.", "Not Found", javax.swing.JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                }

                // Query line items
                String sqlItems = "SELECT oi.quantity, oi.unit_price, (oi.quantity * oi.unit_price) AS line_total, p.name AS product_name "
                                + "FROM order_item oi "
                                + "JOIN product p ON oi.product_id = p.product_id "
                                + "WHERE oi.order_id = ?";
                double subtotal = 0.0;
                try (java.sql.PreparedStatement psItems = conn.prepareStatement(sqlItems)) {
                    psItems.setInt(1, orderId);
                    try (java.sql.ResultSet rsItems = psItems.executeQuery()) {
                        while (rsItems.next()) {
                            int qty = rsItems.getInt("quantity");
                            double unitPrice = rsItems.getDouble("unit_price");
                            double total = rsItems.getDouble("line_total");
                            String pName = rsItems.getString("product_name");
                            invoice.items.add(new InvoiceItem(pName, qty, unitPrice, total));
                            subtotal += total;
                        }
                    }
                }

                invoice.subtotal = subtotal > 0 ? subtotal : invoice.grandTotal;
                invoice.tax = Math.max(0.0, invoice.subtotal * 0.05); // 5% GST
                if (invoice.grandTotal > 0 && invoice.subtotal > invoice.grandTotal) {
                    invoice.discount = Math.max(0.0, invoice.subtotal - invoice.grandTotal + invoice.tax);
                } else {
                    invoice.discount = 0.0;
                    if (invoice.grandTotal <= 0) {
                        invoice.grandTotal = invoice.subtotal + invoice.tax;
                    }
                }
            }

            File dir = new File("invoices");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File pdfFile = new File(dir, "Invoice_ORD_" + orderId + ".pdf");
            generateAndOpen(invoice, pdfFile);

            java.awt.Window win = javax.swing.SwingUtilities.getWindowAncestor(parent);
            ToastNotification.show(win, "Invoice Generated", "Saved to " + pdfFile.getName() + " and opened.", ToastNotification.Type.SUCCESS);

        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(parent, "Failed to generate invoice PDF:\n" + ex.getMessage(), "PDF Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void generate(InvoiceData data, File destinationFile) throws IOException {
        List<String> objects = new ArrayList<>();
        // Object 1: Catalog
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        // Object 2: Pages
        objects.add("<< /Type /Pages /Kids [ 3 0 R ] /Count 1 >>");
        // Object 4: Font Helvetica
        String fontRegular = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>";
        // Object 5: Font Helvetica-Bold
        String fontBold = "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>";

        // Generate Stream content for page
        StringBuilder stream = new StringBuilder();

        // 1. Top Decorative Brand Bar
        // Blue Banner: x=40, y=770, w=515, h=45
        stream.append("0.145 0.388 0.922 rg\n"); // #2563eb
        stream.append("40 765 515 50 re f\n");

        // White Header Text inside Banner
        stream.append("1 1 1 rg\n");
        stream.append("BT /F2 20 Tf 55 785 Td (AURACOMMERCE ENTERPRISE) Tj ET\n");
        stream.append("BT /F1 10 Tf 55 772 Td (OFFICIAL TAX INVOICE & RETAIL RECEIPT) Tj ET\n");
        stream.append("BT /F2 14 Tf 420 782 Td (PAID INVOICE) Tj ET\n");

        // 2. Invoice Meta Details
        stream.append("0.058 0.090 0.164 rg\n"); // Dark text
        stream.append("BT /F2 11 Tf 40 735 Td (Invoice Number:) Tj ET\n");
        stream.append("BT /F1 11 Tf 135 735 Td (INV-ORD-").append(data.orderId).append(") Tj ET\n");

        stream.append("BT /F2 11 Tf 40 718 Td (Date & Time:) Tj ET\n");
        String dateStr = data.orderDate != null && !data.orderDate.isEmpty() ? data.orderDate : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        stream.append("BT /F1 11 Tf 135 718 Td (").append(escapePdf(dateStr)).append(") Tj ET\n");

        stream.append("BT /F2 11 Tf 350 735 Td (Payment Method:) Tj ET\n");
        stream.append("BT /F1 11 Tf 450 735 Td (").append(escapePdf(data.paymentMethod != null ? data.paymentMethod : "Prepaid")).append(") Tj ET\n");

        stream.append("BT /F2 11 Tf 350 718 Td (Payment Status:) Tj ET\n");
        stream.append("0.062 0.725 0.505 rg\n"); // Green status
        stream.append("BT /F2 11 Tf 450 718 Td (").append(escapePdf(data.paymentStatus != null ? data.paymentStatus : "Completed")).append(") Tj ET\n");

        // 3. Customer & Shipping Info Box
        stream.append("0.96 0.97 0.98 rg\n"); // Light grey background
        stream.append("40 645 515 55 re f\n");
        stream.append("0.89 0.91 0.93 RG 1 w\n"); // Border
        stream.append("40 645 515 55 re s\n");

        stream.append("0.058 0.090 0.164 rg\n");
        stream.append("BT /F2 10 Tf 52 682 Td (CUSTOMER / BILL TO:) Tj ET\n");
        stream.append("BT /F1 10 Tf 52 667 Td (").append(escapePdf(data.customerName)).append(" (ID: #").append(data.customerId).append(")) Tj ET\n");
        stream.append("BT /F1 9 Tf 52 654 Td (Authorized E-Commerce Buyer Account) Tj ET\n");

        stream.append("BT /F2 10 Tf 330 682 Td (LOGISTICS & DISPATCH:) Tj ET\n");
        stream.append("BT /F1 10 Tf 330 667 Td (Carrier: ").append(escapePdf(data.carrier != null ? data.carrier : "Standard Express")).append(") Tj ET\n");
        stream.append("BT /F1 9 Tf 330 654 Td (Tracking No: ").append(escapePdf(data.trackingNo != null ? data.trackingNo : "TRK-PENDING")).append(") Tj ET\n");

        // 4. Items Table Header
        int tableTopY = 615;
        stream.append("0.145 0.388 0.922 rg\n");
        stream.append("40 ").append(tableTopY).append(" 515 22 re f\n");

        stream.append("1 1 1 rg\n");
        stream.append("BT /F2 9 Tf 48 ").append(tableTopY + 7).append(" Td (#) Tj ET\n");
        stream.append("BT /F2 9 Tf 75 ").append(tableTopY + 7).append(" Td (PRODUCT DESCRIPTION) Tj ET\n");
        stream.append("BT /F2 9 Tf 330 ").append(tableTopY + 7).append(" Td (UNIT PRICE) Tj ET\n");
        stream.append("BT /F2 9 Tf 415 ").append(tableTopY + 7).append(" Td (QTY) Tj ET\n");
        stream.append("BT /F2 9 Tf 475 ").append(tableTopY + 7).append(" Td (TOTAL (INR)) Tj ET\n");

        // 5. Table Rows
        int currentY = tableTopY - 20;
        int rowIdx = 1;
        for (InvoiceItem item : data.items) {
            // Row background (alternating subtle stripe)
            if (rowIdx % 2 == 0) {
                stream.append("0.98 0.98 0.99 rg\n");
                stream.append("40 ").append(currentY - 4).append(" 515 18 re f\n");
            }
            stream.append("0.89 0.91 0.93 RG 0.5 w\n");
            stream.append("40 ").append(currentY - 4).append(" 515 0 re s\n");

            stream.append("0.058 0.090 0.164 rg\n");
            stream.append("BT /F1 9 Tf 48 ").append(currentY).append(" Td (").append(rowIdx).append(") Tj ET\n");
            String safeName = escapePdf(truncate(item.name, 42));
            stream.append("BT /F2 9 Tf 75 ").append(currentY).append(" Td (").append(safeName).append(") Tj ET\n");
            stream.append("BT /F1 9 Tf 330 ").append(currentY).append(" Td (Rs. ").append(String.format("%.2f", item.unitPrice)).append(") Tj ET\n");
            stream.append("BT /F1 9 Tf 420 ").append(currentY).append(" Td (").append(item.quantity).append(") Tj ET\n");
            stream.append("BT /F2 9 Tf 475 ").append(currentY).append(" Td (Rs. ").append(String.format("%.2f", item.lineTotal)).append(") Tj ET\n");

            currentY -= 20;
            rowIdx++;
        }

        // Table Bottom Line
        stream.append("0.145 0.388 0.922 RG 1 w\n");
        stream.append("40 ").append(currentY + 12).append(" 515 0 re s\n");

        // 6. Summary Totals Box (Right Aligned)
        int summaryY = currentY - 10;
        stream.append("0.96 0.97 0.99 rg\n");
        stream.append("310 ").append(summaryY - 70).append(" 245 76 re f\n");
        stream.append("0.89 0.91 0.93 RG 1 w\n");
        stream.append("310 ").append(summaryY - 70).append(" 245 76 re s\n");

        stream.append("0.058 0.090 0.164 rg\n");
        stream.append("BT /F1 9 Tf 325 ").append(summaryY - 12).append(" Td (Items Subtotal:) Tj ET\n");
        stream.append("BT /F1 9 Tf 475 ").append(summaryY - 12).append(" Td (Rs. ").append(String.format("%.2f", data.subtotal)).append(") Tj ET\n");

        if (data.discount > 0) {
            stream.append("0.062 0.725 0.505 rg\n");
            stream.append("BT /F2 9 Tf 325 ").append(summaryY - 28).append(" Td (Coupon Discount:) Tj ET\n");
            stream.append("BT /F2 9 Tf 475 ").append(summaryY - 28).append(" Td (- Rs. ").append(String.format("%.2f", data.discount)).append(") Tj ET\n");
        } else {
            stream.append("0.4 0.45 0.5 rg\n");
            stream.append("BT /F1 9 Tf 325 ").append(summaryY - 28).append(" Td (Coupon Discount:) Tj ET\n");
            stream.append("BT /F1 9 Tf 475 ").append(summaryY - 28).append(" Td (Rs. 0.00) Tj ET\n");
        }

        stream.append("0.058 0.090 0.164 rg\n");
        stream.append("BT /F1 9 Tf 325 ").append(summaryY - 44).append(" Td (Applicable GST/Tax (5%):) Tj ET\n");
        stream.append("BT /F1 9 Tf 475 ").append(summaryY - 44).append(" Td (Rs. ").append(String.format("%.2f", data.tax)).append(") Tj ET\n");

        // Grand Total Row
        stream.append("0.145 0.388 0.922 rg\n");
        stream.append("310 ").append(summaryY - 70).append(" 245 20 re f\n");
        stream.append("1 1 1 rg\n");
        stream.append("BT /F2 10 Tf 325 ").append(summaryY - 64).append(" Td (GRAND TOTAL (INR):) Tj ET\n");
        stream.append("BT /F2 11 Tf 465 ").append(summaryY - 64).append(" Td (Rs. ").append(String.format("%.2f", data.grandTotal)).append(") Tj ET\n");

        // 7. Security & Verification Badge (Left Side)
        stream.append("0.39 0.45 0.55 rg\n");
        stream.append("BT /F2 9 Tf 40 ").append(summaryY - 20).append(" Td (Verification & Guarantee:) Tj ET\n");
        stream.append("BT /F1 8 Tf 40 ").append(summaryY - 34).append(" Td (100% Authentic Products \\225 Safe Transit Assurance) Tj ET\n");
        stream.append("BT /F1 8 Tf 40 ").append(summaryY - 46).append(" Td (Standard 7-Day Return Policy on all eligible items) Tj ET\n");

        // 8. Footer
        stream.append("0.89 0.91 0.93 RG 1 w\n");
        stream.append("40 70 515 0 re s\n");
        stream.append("0.39 0.45 0.55 rg\n");
        stream.append("BT /F2 8 Tf 40 55 Td (AuraCommerce Cloud POS & ERP \\225 Enterprise Shopping Systems) Tj ET\n");
        stream.append("BT /F1 8 Tf 40 43 Td (This is an electronically generated legal invoice. For questions, reach out to support@auracommerce.com) Tj ET\n");
        stream.append("BT /F1 8 Tf 460 55 Td (Page 1 of 1) Tj ET\n");

        byte[] streamBytes = stream.toString().getBytes(StandardCharsets.US_ASCII);

        // Object 6: Content Stream
        String streamObj = "<< /Length " + streamBytes.length + " >>\nstream\n" + stream.toString() + "\nendstream";

        // Object 3: Page Definition (References Font and Stream)
        String pageObj = "<< /Type /Page /Parent 2 0 R /MediaBox [ 0 0 595 842 ]\n"
                + "/Resources << /Font << /F1 4 0 R /F2 5 0 R >> >>\n"
                + "/Contents 6 0 R >>";

        // Write full PDF structure
        try (FileOutputStream fos = new FileOutputStream(destinationFile)) {
            List<Long> offsets = new ArrayList<>();
            fos.write("%PDF-1.4\n".getBytes(StandardCharsets.US_ASCII));
            long currentPos = 9; // length of "%PDF-1.4\n"

            // Helper to write an indirect object
            long off1 = currentPos;
            currentPos += writeObj(fos, 1, objects.get(0));
            offsets.add(off1);

            long off2 = currentPos;
            currentPos += writeObj(fos, 2, objects.get(1));
            offsets.add(off2);

            long off3 = currentPos;
            currentPos += writeObj(fos, 3, pageObj);
            offsets.add(off3);

            long off4 = currentPos;
            currentPos += writeObj(fos, 4, fontRegular);
            offsets.add(off4);

            long off5 = currentPos;
            currentPos += writeObj(fos, 5, fontBold);
            offsets.add(off5);

            long off6 = currentPos;
            currentPos += writeObj(fos, 6, streamObj);
            offsets.add(off6);

            // Cross reference table (xref)
            long xrefPos = currentPos;
            StringBuilder xref = new StringBuilder();
            xref.append("xref\n0 7\n0000000000 65535 f \n");
            for (Long off : offsets) {
                xref.append(String.format("%010d 00000 n \n", off));
            }
            xref.append("trailer\n<< /Size 7 /Root 1 0 R >>\nstartxref\n").append(xrefPos).append("\n%%EOF\n");
            fos.write(xref.toString().getBytes(StandardCharsets.US_ASCII));
        }
    }

    private static long writeObj(FileOutputStream fos, int num, String body) throws IOException {
        String full = num + " 0 obj\n" + body + "\nendobj\n";
        byte[] bytes = full.getBytes(StandardCharsets.US_ASCII);
        fos.write(bytes);
        return bytes.length;
    }

    private static String escapePdf(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("(", "\\(")
                   .replace(")", "\\)")
                   .replace("₹", "Rs. ")
                   .replaceAll("[^\\x20-\\x7E]", " ");
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() > maxLen ? text.substring(0, maxLen - 3) + "..." : text;
    }
}
