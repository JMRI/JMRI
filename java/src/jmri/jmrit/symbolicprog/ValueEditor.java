package jmri.jmrit.symbolicprog;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.EventObject;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JTable editor for cells representing CV values. This is a somewhat
 * unconventional thing in several ways:
 * <ul>
 * <li>The returned value is not the String edited into the cell, but an Integer
 * value. It is not clear how that arose, and it should probably be changed at
 * some point.
 * <li>This is also a focus listener. People are not used to having to hit
 * return/enter to "set" their value in place, and are rather expecting that the
 * value they typed will be in effect as soon as they type it. We use a
 * focusListener to do that.
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class ValueEditor extends JComboBox<Object> implements TableCellEditor, FocusListener {

    protected transient Vector<CellEditorListener> listeners;
    protected transient String originalValue = null;
    protected Object mValue;

    public ValueEditor() {
        super();
        listeners = new Vector<CellEditorListener>();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value,
            boolean isSelected,
            int row, int column) {
        mValue = value;
        if (log.isDebugEnabled()) {
            log.debug("getTableCellEditorComponent " + row + " "
                    + column + " " + isSelected + " "
                    + value.getClass());
        }
        table.setRowSelectionInterval(row, row);
        table.setColumnSelectionInterval(column, column);
        if (value instanceof Component) {
            if (value instanceof JTextField) {
                JTextField tempField = (JTextField) value;
                originalValue = tempField.getText();
                tempField.addFocusListener(this);
                return tempField;
            } else {
                return (Component) value;
            }
        } else if (value instanceof String) {
            return new JLabel((String) value);
        } else {
            return new JLabel("Unknown value type!");
        }
    }

    /**
     * FocusListener implementations
     */
    @Override
    public void focusGained(FocusEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("focusGained");
        }
        if (mValue instanceof JTextField) {
            JTextField tempField = (JTextField) mValue;
            originalValue = tempField.getText();
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("focusLost");
        }
        if (!(mValue instanceof JTextField)
                || !(originalValue.equals(((JTextField) mValue).getText()))) {
            fireEditingStopped();
        }
    }

    void removeFocusListener() {
        if (mValue instanceof JTextField) {
            JTextField tempField = (JTextField) mValue;
            originalValue = null;
            tempField.removeFocusListener(this);
        }
    }

    // CellEditor methods
    @Override
    public void cancelCellEditing() {
        if (log.isDebugEnabled()) {
            log.debug("cancelCellEditing");
        }
        removeFocusListener();
        fireEditingCanceled();
    }

    @Override
    public Object getCellEditorValue() {
        if (log.isDebugEnabled()) {
            log.debug("getCellEditorValue with 'value' object of type " + mValue.getClass());
        }
        if (mValue instanceof JTextField) {
            // extract the string from the JTextField and return it
            return Integer.valueOf(((JTextField) mValue).getText());
        } else if (mValue instanceof Component) {
            // extract the string from the JTextField and return it
            return mValue;
        } else {
            log.error("getCellEditorValue unable to return a value from unknown type "
                    + mValue.getClass());
            return null;
        }
    }

    @Override
    public boolean isCellEditable(EventObject eo) {
        return true;
    }

    @Override
    public boolean shouldSelectCell(EventObject eo) {
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        if (log.isDebugEnabled()) {
            log.debug("stopCellEditing");
        }
        removeFocusListener();
        fireEditingStopped();
        return true;
    }

    @Override
    public void addCellEditorListener(CellEditorListener cel) {
        listeners.addElement(cel);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener cel) {
        listeners.removeElement(cel);
    }

    protected void fireEditingCanceled() {
        if (log.isDebugEnabled()) {
            log.debug("fireEditingCancelled, but we are not setting back the old value");
        }
        Vector<CellEditorListener> local;
        synchronized (listeners) {
            local = new Vector<CellEditorListener>(listeners);
        }
        ChangeEvent ce = new ChangeEvent(this);
        for (int i = local.size() - 1; i >= 0; i--) {
            local.elementAt(i).editingCanceled(ce);
        }
    }

    protected void fireEditingStopped() {
        if (log.isDebugEnabled()) {
            log.debug("fireEditingStopped");
        }
        Vector<CellEditorListener> local;
        synchronized (listeners) {
            local = new Vector<CellEditorListener>(listeners);
        }
        ChangeEvent ce = new ChangeEvent(this);
        for (int i = local.size() - 1; i >= 0; i--) {
            local.elementAt(i).editingStopped(ce);
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ValueEditor.class);
}
