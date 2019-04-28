package jmri.swing;

import javax.swing.ListModel;

/**
 * A model for an editable list.
 * 
 * @author Randall Wood
 * @param <E> the supported class in this model
 */
public interface EditableListModel<E> extends ListModel<E> {

    public boolean isCellEditable(int index);

    public void setValueAt(E value, int index);
}
