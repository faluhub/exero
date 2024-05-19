package me.falu.exero.gui;

import com.formdev.flatlaf.icons.FlatOptionPaneInformationIcon;

import javax.swing.*;
import java.awt.*;

public class LoadingScreenWindow extends JFrame {
    private final JLabel statusLabel;

    public LoadingScreenWindow(String initialText) {
        this.setTitle("Loading...");
        this.setSize(500, 100);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setUndecorated(true);
        FlatOptionPaneInformationIcon icon = (FlatOptionPaneInformationIcon) UIManager.getIcon("OptionPane.informationIcon");
        JPanel panel = new JPanel(new BorderLayout());
        this.getContentPane().add(panel);
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(iconLabel, BorderLayout.WEST);
        JLabel loadingLabel = new JLabel(initialText);
        loadingLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.statusLabel = new JLabel("Loading...");
        panel.add(loadingLabel, BorderLayout.CENTER);
        panel.add(this.statusLabel, BorderLayout.SOUTH);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    public void setStatusText(String text) {
        this.statusLabel.setText(text);
    }
}
