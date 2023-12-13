package jmri.jmrit.operations;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Common table model methods for operations.
 * 
 * @author Daniel Boudreau Copyright (C) 2023
 *
 */
public abstract class OperationsTableModel extends javax.swing.table.AbstractTableModel {
    
    private JTable _table;
    
    public void initTable(JTable table) {
        _table = table;
        table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());
        table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
    }

    protected Color getForegroundColor(int row) {
        return _table.getForeground();
    }
    
    public class MyTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                int modelRow = table.convertRowIndexToModel(row);
                component.setForeground(getForegroundColor(modelRow));
            }
            return component;
        }
    }
}
