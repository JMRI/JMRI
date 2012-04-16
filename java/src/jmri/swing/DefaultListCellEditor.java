/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.swing;

import java.awt.Component;
import javax.swing.*;

/**
 *
 * @author rhwood
 */
public class DefaultListCellEditor extends DefaultCellEditor implements ListCellEditor {

    public DefaultListCellEditor(final JCheckBox checkBox) {
        super(checkBox);
    }

    public DefaultListCellEditor(final JComboBox comboBox) {
        super(comboBox);
    }

    public DefaultListCellEditor(final JTextField textField) {
        super(textField);
    }

    @Override
    public Component getListCellEditorComponent(JList list, Object value, boolean isSelected, int index) {
        delegate.setValue(value);
        return editorComponent;
    }
}