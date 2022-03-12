package jmri.swing;

import javax.swing.DefaultListModel;

/**
 *
 * @author Randall Wood
 */
public class DefaultEditableListModel<E> extends DefaultListModel<E> implements EditableListModel<E> {

    @Override
    public boolean isCellEditable(int index) {
        return true;
    }

    @Override
    public void setValueAt(E value, int index) {
        super.setElementAt(value, index);
    }
}
