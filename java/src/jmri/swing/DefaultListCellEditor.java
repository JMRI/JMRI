package jmri.swing;

import java.awt.Component;
import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTextField;

/**
 *
 * @author rhwood
 */
public class DefaultListCellEditor<E> extends DefaultCellEditor implements ListCellEditor<E> {

    public DefaultListCellEditor(final JCheckBox checkBox) {
        super(checkBox);
    }

    public DefaultListCellEditor(final JComboBox<?> comboBox) {
        super(comboBox);
    }

    public DefaultListCellEditor(final JTextField textField) {
        super(textField);
    }

    @Override
    public Component getListCellEditorComponent(JList<E> list, E value, boolean isSelected, int index) {
        delegate.setValue(value);
        return editorComponent;
    }

    @Override
    @SuppressWarnings("unchecked") // made safe by construction
    public E getCellEditorValue() {
        return (E) super.getCellEditorValue();
    }

}
