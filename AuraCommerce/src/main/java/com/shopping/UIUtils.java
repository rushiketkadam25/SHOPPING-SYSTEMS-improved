package com.shopping;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

public class UIUtils {
    public static final Color SIDEBAR_BG = new Color(11, 15, 25);
    public static final Color SIDEBAR_CARD = new Color(20, 27, 45);
    public static final Color SIDEBAR_HOVER = new Color(30, 41, 69);
    public static final Color PRIMARY = new Color(59, 130, 246);
    public static final Color PRIMARY_DARK = new Color(37, 99, 235);
    public static final Color PRIMARY_HOVER = PRIMARY_DARK;
    public static final Color SUCCESS = new Color(16, 185, 129);
    public static final Color DANGER = new Color(244, 63, 94);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color PURPLE = new Color(139, 92, 246);

    // Dynamic Theme Tokens
    public static Color BG_CANVAS = new Color(245, 247, 251);
    public static Color BG_LIGHT = BG_CANVAS;
    public static Color CARD_BG = Color.WHITE;
    public static Color CARD_BORDER = new Color(229, 231, 235);
    public static Color TEXT_DARK = new Color(15, 23, 42);
    public static Color TEXT_MUTED = new Color(100, 116, 139);

    private static boolean darkMode = false;

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void toggleTheme(Window window) {
        setDarkMode(!darkMode, window);
    }

    public static void setDarkMode(boolean dark, Window window) {
        darkMode = dark;
        if (darkMode) {
            BG_CANVAS = new Color(15, 23, 42);
            BG_LIGHT = BG_CANVAS;
            CARD_BG = new Color(30, 41, 59);
            CARD_BORDER = new Color(51, 65, 85);
            TEXT_DARK = new Color(241, 245, 249);
            TEXT_MUTED = new Color(148, 163, 184);
            FlatDarkLaf.setup();
        } else {
            BG_CANVAS = new Color(245, 247, 251);
            BG_LIGHT = BG_CANVAS;
            CARD_BG = Color.WHITE;
            CARD_BORDER = new Color(229, 231, 235);
            TEXT_DARK = new Color(15, 23, 42);
            TEXT_MUTED = new Color(100, 116, 139);
            FlatLightLaf.setup();
        }
        FlatLaf.updateUI();
        if (window != null) {
            SwingUtilities.updateComponentTreeUI(window);
            window.repaint();
        }
    }

    public static JButton createButton(String string, Color color, Color color2) {
        JButton jButton = new JButton(string);
        jButton.setFont(new Font("Segoe UI", 1, 12));
        jButton.setBackground(color);
        jButton.setForeground(color2);
        jButton.setFocusPainted(false);
        jButton.setCursor(new Cursor(12));
        jButton.putClientProperty("JButton.buttonType", "roundRect");
        jButton.setPreferredSize(new Dimension(jButton.getPreferredSize().width + 16, 36));
        return jButton;
    }

    public static JButton createSidebarButton(String string, boolean bl) {
        JButton jButton = new JButton(string);
        jButton.setFont(new Font("Segoe UI", bl ? 1 : 0, 13));
        jButton.setForeground(bl ? Color.WHITE : new Color(203, 213, 225));
        jButton.setBackground(bl ? PRIMARY_DARK : SIDEBAR_BG);
        jButton.setHorizontalAlignment(2);
        jButton.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        jButton.setFocusPainted(false);
        jButton.setCursor(new Cursor(12));
        jButton.setMaximumSize(new Dimension(240, 44));
        jButton.putClientProperty("JButton.buttonType", "roundRect");
        return jButton;
    }

    public static JPanel createCard(String string) {
        JPanel jPanel = new JPanel(new BorderLayout());
        jPanel.setBackground(CARD_BG);
        jPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(CARD_BORDER, 1, true), 
            BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        if (string != null && !string.isEmpty()) {
            JLabel jLabel = new JLabel(string);
            jLabel.setFont(new Font("Segoe UI", 1, 15));
            jLabel.setForeground(TEXT_DARK);
            jLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
            jPanel.add((Component)jLabel, "North");
        }
        return jPanel;
    }

    public static JPanel createStatCard(String string, String string2, String string3, String string4, Color color) {
        JPanel jPanel = new JPanel(new BorderLayout(14, 4));
        jPanel.setBackground(CARD_BG);
        jPanel.setPreferredSize(new Dimension(225, 82));
        jPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(CARD_BORDER, 1, true), BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        JLabel jLabel = new JLabel(string4, 0);
        jLabel.setFont(new Font("Segoe UI Emoji", 0, 24));
        jLabel.setPreferredSize(new Dimension(48, 48));
        jLabel.setOpaque(true);
        jLabel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue(), 28));
        jLabel.setForeground(color);
        JPanel jPanel2 = new JPanel(new GridLayout(2, 1, 0, 1));
        jPanel2.setOpaque(false);
        JLabel jLabel2 = new JLabel(string.toUpperCase());
        jLabel2.setFont(new Font("Segoe UI", 1, 11));
        jLabel2.setForeground(TEXT_MUTED);
        JLabel jLabel3 = new JLabel(string2);
        jLabel3.setFont(new Font("Segoe UI", 1, 20));
        jLabel3.setForeground(TEXT_DARK);
        jPanel2.add(jLabel2);
        jPanel2.add(jLabel3);
        jPanel.add((Component)jLabel, "West");
        jPanel.add((Component)jPanel2, "Center");
        return jPanel;
    }

    public static void formatTable(JTable jTable) {
        jTable.setRowHeight(36);
        jTable.setFont(new Font("Segoe UI", 0, 13));
        jTable.getTableHeader().setFont(new Font("Segoe UI", 1, 12));
        jTable.getTableHeader().setPreferredSize(new Dimension(jTable.getTableHeader().getPreferredSize().width, 38));
        jTable.setShowVerticalLines(false);
        jTable.setDefaultRenderer(Object.class, new ModernTableRenderer());
    }
}
