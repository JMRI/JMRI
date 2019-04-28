package jmri.swing;

import javax.swing.DefaultListModel;

/**
 * A simple implementation of an {@link EditableListModel}. A more complex
 * implementation would override {@link #isCellEditable(int)} to selectively
 * allow the editing of cells in the list.
 *
 * @author Randall Wood
 * @param <E> the object class supported by the model
 */
public class DefaultEditableListModel<E> extends DefaultListModel<E> implements EditableListModel<E> {

    /**
     * {@inheritDoc}
     * 
     * A simple implementation that always returns true.
     * 
     * @return true
     */
    @Override
    public boolean isCellEditable(int index) {
        return true;
    }

    @Override
    public void setValueAt(E value, int index) {
        super.setElementAt(value, index);
    }
}
