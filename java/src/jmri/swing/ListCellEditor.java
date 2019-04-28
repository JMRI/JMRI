package jmri.swing;

import java.awt.Component;
import javax.swing.CellEditor;
import javax.swing.JList;

/**
 * An editor for cells in a list.
 * 
 * @author Randall Wood
 * @param <E> the class of object in the supported list
 */
public interface ListCellEditor<E> extends CellEditor {

    Component getListCellEditorComponent(JList<E> list, E value,
            boolean isSelected,
            int index);

    @Override
    public E getCellEditorValue();
}
