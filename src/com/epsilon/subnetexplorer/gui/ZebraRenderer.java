package com.epsilon.subnetexplorer.gui;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Component;


public class ZebraRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                     boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        c.setFont(UITheme.FONT_BODY);
        if (!isSelected) {
            c.setBackground(row % 2 == 0 ? UITheme.CARD_BG : UITheme.ROW_ALT);
            c.setForeground(UITheme.TEXT_DARK);
        } else {
            c.setBackground(UITheme.ACCENT);
            c.setForeground(UITheme.TEXT_LIGHT);
        }
        setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 10, 2, 10));
        return c;
    }
}
