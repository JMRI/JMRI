package apps.startup;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import jmri.util.startup.StartupModel;

/**
 * Render a {@link StartupModel} in a table.
 * <p>
 * An action that cannot be run, for example because its connection is not
 * available, is shown in red so it stands out in the list. A selected row
 * keeps the normal selection colours.
 *
 * @author Randall Wood Copyright (C) 2015, 2016
 */
class StartupModelCellRenderer extends DefaultTableCellRenderer {

    static final Color INVALID_COLOR = Color.RED;

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        // clear any colour left from the previous cell, as the component is reused
        setForeground(null);
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        // a selected row keeps the selection colours so it stays readable
        if (!isSelected && value instanceof StartupModel && !((StartupModel) value).isValid()) {
            c.setForeground(INVALID_COLOR);
        }
        return c;
    }

    @Override
    protected void setValue(Object value) {
        // null anything is rendered as an empty cell.
        if (value != null) {
            String text = ((StartupModel) value).getName();
            setText(text != null ? text : value.toString()); // NOI18N
        } else {
            setText(""); // NOI18N
        }
    }
}
