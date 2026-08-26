package com.epsilon.subnetexplorer.gui;

import com.epsilon.subnetexplorer.logic.IPUtils;
import com.epsilon.subnetexplorer.logic.OUILookup;
import com.epsilon.subnetexplorer.logic.SubnetInfo;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Tab 1 — takes an IP + CIDR/mask (and optionally a MAC address) and
 * displays every value that can be derived from them, including a
 * binary breakdown of the address vs. the mask.
 */
public class SubnetCalculatorPanel extends JPanel {

    private final JTextField ipField = new JTextField();
    private final JTextField cidrField = new JTextField();
    private final JTextField macField = new JTextField();
    private final JLabel statusLabel = new JLabel(" ");

    private final DefaultTableModel resultsModel =
            new DefaultTableModel(new Object[]{"Property", "Value"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) {
                    return false;
                }
            };
    private final JTable resultsTable = new JTable(resultsModel);
    private final JTextArea binaryArea = new JTextArea(4, 40);

    public SubnetCalculatorPanel() {
        setLayout(new BorderLayout(0, 14));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildInputCard(), BorderLayout.NORTH);
        add(buildResultsCard(), BorderLayout.CENTER);
    }

    private JComponent buildInputCard() {
        RoundedPanel card = new RoundedPanel(14);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Subnet Calculator");
        title.setFont(UITheme.FONT_SECTION);
        title.setForeground(UITheme.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        card.add(title, gbc);

        gbc.gridwidth = 1;

        addField(card, gbc, 1, "IP Address", ipField, "e.g. 192.168.1.10");
        addField(card, gbc, 2, "CIDR or Mask", cidrField, "e.g. /24 or 255.255.255.0");
        addField(card, gbc, 3, "MAC Address (optional)", macField, "e.g. 00:1A:2B:3C:4D:5E");

        RoundedButton calcButton = new RoundedButton("Calculate");
        calcButton.addActionListener(e -> calculate());

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.WEST;
        card.add(calcButton, gbc);

        statusLabel.setFont(UITheme.FONT_BODY);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        card.add(statusLabel, gbc);

        return card;
    }

    private void addField(JPanel parent, GridBagConstraints gbc, int row, String label,
                           JTextField field, String placeholder) {
        JLabel l = new JLabel(label);
        l.setFont(UITheme.FONT_BODY_BOLD);
        l.setForeground(UITheme.TEXT_DARK);

        field.setFont(UITheme.FONT_BODY);
        field.setToolTipText(placeholder);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        field.setPreferredSize(new Dimension(220, 30));

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        parent.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        parent.add(field, gbc);
    }

    private JComponent buildResultsCard() {
        RoundedPanel card = new RoundedPanel(14);
        card.setLayout(new BorderLayout(0, 10));
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Results");
        title.setFont(UITheme.FONT_SECTION);
        title.setForeground(UITheme.PRIMARY);

        resultsTable.setFont(UITheme.FONT_BODY);
        resultsTable.setRowHeight(28);
        resultsTable.setShowGrid(false);
        resultsTable.setIntercellSpacing(new Dimension(0, 0));
        resultsTable.getTableHeader().setFont(UITheme.FONT_HEADER);
        resultsTable.getTableHeader().setBackground(UITheme.PRIMARY);
        resultsTable.getTableHeader().setForeground(UITheme.TEXT_LIGHT);
        resultsTable.setDefaultRenderer(Object.class, new ZebraRenderer());
        resultsTable.getColumnModel().getColumn(0).setPreferredWidth(220);

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER));

        binaryArea.setFont(UITheme.FONT_MONO);
        binaryArea.setEditable(false);
        binaryArea.setBackground(new Color(250, 250, 252));
        binaryArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        binaryArea.setText("Binary breakdown will appear here after you calculate.");

        JPanel resultsAndBinary = new JPanel(new BorderLayout(0, 10));
        resultsAndBinary.setOpaque(false);
        resultsAndBinary.add(tableScroll, BorderLayout.CENTER);

        JLabel binaryTitle = new JLabel("Binary Breakdown (network bits vs. host bits)");
        binaryTitle.setFont(UITheme.FONT_BODY_BOLD);
        binaryTitle.setForeground(UITheme.TEXT_MUTED);

        JPanel binaryWrap = new JPanel(new BorderLayout(0, 4));
        binaryWrap.setOpaque(false);
        binaryWrap.add(binaryTitle, BorderLayout.NORTH);
        binaryWrap.add(binaryArea, BorderLayout.CENTER);

        resultsAndBinary.add(binaryWrap, BorderLayout.SOUTH);

        card.add(title, BorderLayout.NORTH);
        card.add(resultsAndBinary, BorderLayout.CENTER);
        return card;
    }

    private void calculate() {
        resultsModel.setRowCount(0);
        try {
            String ip = ipField.getText().trim();
            String cidrRaw = cidrField.getText().trim();

            if (!IPUtils.isValidIPv4(ip)) {
                throw new IllegalArgumentException("Enter a valid IPv4 address, e.g. 192.168.1.10");
            }
            if (cidrRaw.isEmpty()) {
                throw new IllegalArgumentException("Enter a CIDR (e.g. /24) or a subnet mask (e.g. 255.255.255.0)");
            }

            int cidr;
            if (IPUtils.looksLikeDottedMask(cidrRaw)) {
                cidr = IPUtils.cidrFromMask(cidrRaw);
            } else {
                cidr = Integer.parseInt(cidrRaw.replace("/", "").trim());
            }

            SubnetInfo info = SubnetInfo.calculate(ip, cidr);

            resultsModel.addRow(new Object[]{"IP Address", info.ipAddress});
            resultsModel.addRow(new Object[]{"CIDR Notation", "/" + info.cidr});
            resultsModel.addRow(new Object[]{"Subnet Mask", info.subnetMask});
            resultsModel.addRow(new Object[]{"Wildcard Mask", info.wildcardMask});
            resultsModel.addRow(new Object[]{"Network Address", info.networkAddress});
            resultsModel.addRow(new Object[]{"Broadcast Address", info.broadcastAddress});
            resultsModel.addRow(new Object[]{"First Usable Host", info.firstUsableHost});
            resultsModel.addRow(new Object[]{"Last Usable Host", info.lastUsableHost});
            resultsModel.addRow(new Object[]{"Total Addresses", info.totalAddresses});
            resultsModel.addRow(new Object[]{"Usable Hosts", info.usableHosts});
            resultsModel.addRow(new Object[]{"IP Class", info.ipClass});
            resultsModel.addRow(new Object[]{"Private / Reserved?", info.privateOrReserved ? "Yes" : "No (Public)"});

            String mac = macField.getText().trim();
            if (!mac.isEmpty()) {
                String vendor = OUILookup.lookup(mac);
                resultsModel.addRow(new Object[]{"MAC Vendor (sample DB)", vendor});
            }

            binaryArea.setText(
                    "IP:   " + info.binaryIp + "\n" +
                    "Mask: " + info.binaryMask + "\n\n" +
                    "Bits set to 1 in the mask = network portion. Bits set to 0 = host portion."
            );

            statusLabel.setForeground(UITheme.SUCCESS);
            statusLabel.setText("Calculated successfully.");
        } catch (Exception ex) {
            binaryArea.setText("Binary breakdown will appear here after you calculate.");
            statusLabel.setForeground(UITheme.ERROR);
            statusLabel.setText("Error: " + ex.getMessage());
        
        }
    }
}
