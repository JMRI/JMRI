/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.swing;

import java.awt.Component;
import javax.swing.CellEditor;
import javax.swing.JList;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 *
 * @author Randall Wood
 */
@API(status = EXPERIMENTAL)
public interface ListCellEditor<E> extends CellEditor {

    Component getListCellEditorComponent(JList<E> list, E value,
            boolean isSelected,
            int index);

    @Override
    public E getCellEditorValue();
}
