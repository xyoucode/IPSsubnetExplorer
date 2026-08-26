package com.epsilon.subnetexplorer.gui;

import com.epsilon.subnetexplorer.logic.IPUtils;
import com.epsilon.subnetexplorer.logic.VLSMPlanner;
import com.epsilon.subnetexplorer.logic.VLSMPlanner.Allocation;
import com.epsilon.subnetexplorer.logic.VLSMPlanner.Requirement;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class VLSMPlannerPanel extends JPanel {

    private final JTextField baseIpField = new JTextField();
    private final JTextField baseCidrField = new JTextField();
    private final JLabel statusLabel = new JLabel(" ");

    private final DefaultTableModel requirementsModel =
            new DefaultTableModel(new Object[]{"Department / Segment", "Hosts Needed"}, 0);
    private final JTable requirementsTable = new JTable(requirementsModel);

    private final DefaultTableModel resultsModel = new DefaultTableModel(
            new Object[]{"Name", "Hosts", "CIDR", "Subnet Mask", "Network", "Broadcast",
                    "First Usable", "Last Usable", "Usable Hosts"}, 0) {
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };
    private final JTable resultsTable = new JTable(resultsModel);

    public VLSMPlannerPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildInputCard(), BorderLayout.NORTH);
        add(buildResultsCard(), BorderLayout.CENTER);

        requirementsModel.addRow(new Object[]{"Engineering", 50});
        requirementsModel.addRow(new Object[]{"Sales", 20});
    }

    private JComponent buildInputCard() {
        RoundedPanel card = new RoundedPanel(14);
        card.setLayout(new BorderLayout(0, 12));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("VLSM Planner");
        title.setFont(UITheme.FONT_SECTION);
        title.setForeground(UITheme.PRIMARY);

        JPanel baseNetworkRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        baseNetworkRow.setOpaque(false);

        baseNetworkRow.add(styledLabel("Base Network:"));
        baseIpField.setPreferredSize(new Dimension(160, 30));
        styleField(baseIpField, "e.g. 192.168.1.0");
        baseNetworkRow.add(baseIpField);

        baseNetworkRow.add(styledLabel("/"));
        baseCidrField.setPreferredSize(new Dimension(60, 30));
        styleField(baseCidrField, "e.g. 24");
        baseNetworkRow.add(baseCidrField);

        requirementsTable.setFont(UITheme.FONT_BODY);
        requirementsTable.setRowHeight(28);
        requirementsTable.getTableHeader().setFont(UITheme.FONT_HEADER);
        requirementsTable.getTableHeader().setBackground(UITheme.PRIMARY);
        requirementsTable.getTableHeader().setForeground(UITheme.TEXT_LIGHT);
        requirementsTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        JScrollPane reqScroll = new JScrollPane(requirementsTable);
        reqScroll.setPreferredSize(new Dimension(400, 110));
        reqScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        JPanel rowButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        rowButtons.setOpaque(false);
        RoundedButton addRowBtn = new RoundedButton("+ Add Segment", UITheme.PRIMARY, UITheme.PRIMARY_DARK);
        RoundedButton removeRowBtn = new RoundedButton("- Remove Selected", UITheme.TEXT_MUTED, UITheme.ERROR);
        addRowBtn.addActionListener(e -> requirementsModel.addRow(new Object[]{"", ""}));
        removeRowBtn.addActionListener(e -> {
            int row = requirementsTable.getSelectedRow();
            if (row >= 0) requirementsModel.removeRow(row);
        });
        rowButtons.add(addRowBtn);
        rowButtons.add(removeRowBtn);

        RoundedButton planButton = new RoundedButton("Plan Subnets");
        planButton.addActionListener(e -> plan());

        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setOpaque(false);
        bottomRow.add(rowButtons, BorderLayout.WEST);
        JPanel planWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        planWrap.setOpaque(false);
        planWrap.add(planButton);
        bottomRow.add(planWrap, BorderLayout.EAST);

        JPanel middle = new JPanel(new BorderLayout(0, 8));
        middle.setOpaque(false);
        middle.add(reqScroll, BorderLayout.CENTER);
        middle.add(bottomRow, BorderLayout.SOUTH);

        statusLabel.setFont(UITheme.FONT_BODY);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(baseNetworkRow, BorderLayout.CENTER);

        card.add(top, BorderLayout.NORTH);
        card.add(middle, BorderLayout.CENTER);
        card.add(statusLabel, BorderLayout.SOUTH);

        return card;
    }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY_BOLD);
        l.setForeground(UITheme.TEXT_DARK);
        return l;
    }

    private void styleField(JTextField field, String placeholder) {
        field.setFont(UITheme.FONT_BODY);
        field.setToolTipText(placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
    }

    private JComponent buildResultsCard() {
        RoundedPanel card = new RoundedPanel(14);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Allocation Plan");
        title.setFont(UITheme.FONT_SECTION);
        title.setForeground(UITheme.PRIMARY);

        resultsTable.setFont(UITheme.FONT_BODY);
        resultsTable.setRowHeight(28);
        resultsTable.getTableHeader().setFont(UITheme.FONT_HEADER);
        resultsTable.getTableHeader().setBackground(UITheme.PRIMARY);
        resultsTable.getTableHeader().setForeground(UITheme.TEXT_LIGHT);
        resultsTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        JScrollPane scroll = new JScrollPane(resultsTable);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        card.add(title, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private void plan() {
        resultsModel.setRowCount(0);
        try {
            String baseIp = baseIpField.getText().trim();
            String cidrRaw = baseCidrField.getText().trim().replace("/", "");

            if (!IPUtils.isValidIPv4(baseIp)) {
                throw new IllegalArgumentException("Enter a valid base network IP, e.g. 192.168.1.0");
            }
            if (cidrRaw.isEmpty()) {
                throw new IllegalArgumentException("Enter a base CIDR, e.g. 24");
            }
            int baseCidr = Integer.parseInt(cidrRaw);

            List<Requirement> requirements = new ArrayList<>();
            for (int row = 0; row < requirementsModel.getRowCount(); row++) {
                Object nameObj = requirementsModel.getValueAt(row, 0);
                Object hostsObj = requirementsModel.getValueAt(row, 1);
                String name = nameObj == null ? "" : nameObj.toString().trim();
                String hostsStr = hostsObj == null ? "" : hostsObj.toString().trim();
                if (name.isEmpty() && hostsStr.isEmpty()) continue; // skip fully blank rows
                if (name.isEmpty()) {
                    throw new IllegalArgumentException("Row " + (row + 1) + " is missing a name.");
                }
                long hosts;
                try {
                    hosts = Long.parseLong(hostsStr);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("Row " + (row + 1) + " (\"" + name + "\") needs a numeric host count.");
                }
                requirements.add(new Requirement(name, hosts));
            }

            List<Allocation> allocations = VLSMPlanner.plan(baseIp, baseCidr, requirements);

            for (Allocation a : allocations) {
                resultsModel.addRow(new Object[]{
                        a.name, a.hostsNeeded, "/" + a.cidr, a.subnetMask,
                        a.networkAddress, a.broadcastAddress,
                        a.firstUsableHost, a.lastUsableHost, a.usableHosts
                });
            }

            statusLabel.setForeground(UITheme.SUCCESS);
            statusLabel.setText("Planned " + allocations.size() + " subnet(s) successfully.");
        } catch (Exception ex) {
            statusLabel.setForeground(UITheme.ERROR);
            statusLabel.setText("Error: " + ex.getMessage());
        }
    }
}
