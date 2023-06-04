package jmri.jmrit.consisttool;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;

import jmri.InstanceManager;
import jmri.jmrit.roster.*;

/**
 * A TableCellRender to graphicaly display a consists in the consist table
 * 
 * @author Lionel Jeanson - 2023
 * 
 */

public class ConsistTableRosterEntryColumnCellRenderer implements TableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JPanel retPanel = new JPanel();
        retPanel.setLayout(new BorderLayout());

        if (value == null) {
            return retPanel;
        }
        if (value instanceof String) {
            String reName = (String) value;        
            JLabel label = new JLabel(reName);
            ImageIcon icon;
            Boolean dir = (Boolean) table.getModel().getValueAt(row, ConsistDataModel.DIRECTIONCOLUMN);
            if (dir) {
                icon = InstanceManager.getDefault(RosterIconFactory.class).getIcon(reName);
            } else {
                icon = InstanceManager.getDefault(RosterIconFactory.class).getReversedIcon(reName);
            }
            if (icon != null) {
                icon.setImageObserver(table);
                label.setIcon(icon);
            }                    
            retPanel.add(label, BorderLayout.CENTER);
        }
        return retPanel;
    }

}
