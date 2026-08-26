package com.epsilon.subnetexplorer.gui;

import javax.swing.*;
import java.awt.*;


public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("IP Subnet Explorer");
        setSize(980, 720);
        setMinimumSize(new Dimension(860, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UITheme.BACKGROUND);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JLabel title = new JLabel("IP Subnet Explorer");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(UITheme.TEXT_LIGHT);

        JLabel subtitle = new JLabel("Subnet calculation & VLSM network planning");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(new Color(210, 220, 235));

        JPanel textStack = new JPanel();
        textStack.setOpaque(false);
        textStack.setLayout(new BoxLayout(textStack, BoxLayout.Y_AXIS));
        textStack.add(title);
        textStack.add(subtitle);

        header.add(textStack, BorderLayout.WEST);
        return header;
    }

    private JComponent buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_HEADER);
        tabs.setBackground(UITheme.BACKGROUND);
        tabs.addTab("  Subnet Calculator  ", new SubnetCalculatorPanel());
        tabs.addTab("  VLSM Planner  ", new VLSMPlannerPanel());
        return tabs;
    }
}
