package com.shopping;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Modern non-blocking floating Toast Notification for Swing.
 * Appears smoothly at the bottom-right of the active application window.
 */
public class ToastNotification {

    public enum Type {
        SUCCESS(new Color(16, 185, 129), "\u2714", "Success"),
        INFO(new Color(59, 130, 246), "\u2139", "Information"),
        WARNING(new Color(245, 158, 11), "\u26a0", "Warning"),
        ERROR(new Color(239, 68, 68), "\u2716", "Error");

        final Color color;
        final String icon;
        final String defaultTitle;

        Type(Color color, String icon, String defaultTitle) {
            this.color = color;
            this.icon = icon;
            this.defaultTitle = defaultTitle;
        }
    }

    public static void show(Window parent, String message, Type type) {
        show(parent, type.defaultTitle, message, type, 3500);
    }

    public static void show(Window parent, String title, String message, Type type) {
        show(parent, title, message, type, 3500);
    }

    public static void show(Window parent, String title, String message, Type type, int durationMs) {
        SwingUtilities.invokeLater(() -> {
            try {
                final JWindow window = new JWindow(parent);
                window.setAlwaysOnTop(true);
                window.setFocusableWindowState(false);

                boolean isDark = UIUtils.isDarkMode();
                Color bg = isDark ? new Color(30, 41, 59) : new Color(255, 255, 255);
                Color textPrimary = isDark ? new Color(241, 245, 249) : new Color(15, 23, 42);
                Color textSecondary = isDark ? new Color(148, 163, 184) : new Color(100, 116, 139);
                Color borderColor = isDark ? new Color(51, 65, 85) : new Color(226, 232, 240);

                JPanel root = new JPanel(new BorderLayout(12, 0));
                root.setBackground(bg);
                root.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(borderColor, 1, true),
                        BorderFactory.createMatteBorder(0, 5, 0, 0, type.color)
                    ),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)
                ));

                // Left Icon Badge
                JLabel iconLabel = new JLabel(type.icon, JLabel.CENTER);
                iconLabel.setFont(new Font("Segoe UI Symbol", Font.BOLD, 18));
                iconLabel.setForeground(type.color);
                iconLabel.setPreferredSize(new Dimension(28, 28));
                root.add(iconLabel, BorderLayout.WEST);

                // Center Text (Title + Message)
                JPanel textPanel = new JPanel();
                textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
                textPanel.setOpaque(false);

                JLabel titleLabel = new JLabel(title);
                titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
                titleLabel.setForeground(textPrimary);
                textPanel.add(titleLabel);
                textPanel.add(Box.createVerticalStrut(2));

                JLabel msgLabel = new JLabel("<html><body style='width: 230px'>" + message + "</body></html>");
                msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                msgLabel.setForeground(textSecondary);
                textPanel.add(msgLabel);

                root.add(textPanel, BorderLayout.CENTER);

                // Right Close button
                JButton closeBtn = new JButton("\u2715");
                closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                closeBtn.setForeground(textSecondary);
                closeBtn.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
                closeBtn.setContentAreaFilled(false);
                closeBtn.setFocusPainted(false);
                closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                closeBtn.addActionListener(e -> window.dispose());
                root.add(closeBtn, BorderLayout.EAST);

                // Click to dismiss
                MouseAdapter dismissListener = new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        window.dispose();
                    }
                };
                root.addMouseListener(dismissListener);
                textPanel.addMouseListener(dismissListener);
                msgLabel.addMouseListener(dismissListener);
                titleLabel.addMouseListener(dismissListener);

                window.setContentPane(root);
                window.pack();

                // Compute Position at Bottom-Right of parent
                if (parent != null && parent.isShowing()) {
                    Point p = parent.getLocationOnScreen();
                    int x = p.x + parent.getWidth() - window.getWidth() - 24;
                    int y = p.y + parent.getHeight() - window.getHeight() - 28;
                    window.setLocation(Math.max(10, x), Math.max(10, y));
                } else {
                    Dimension screen = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                    window.setLocation(screen.width - window.getWidth() - 30, screen.height - window.getHeight() - 60);
                }

                window.setVisible(true);

                // Auto-close timer
                Timer timer = new Timer(durationMs, e -> {
                    window.dispose();
                });
                timer.setRepeats(false);
                timer.start();

            } catch (Exception ignored) {}
        });
    }
}

