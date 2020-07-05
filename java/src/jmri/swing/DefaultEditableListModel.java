package jmri.swing;

import javax.swing.DefaultListModel;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood
 */
@API(status = EXPERIMENTAL)
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
