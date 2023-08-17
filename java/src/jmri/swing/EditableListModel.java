/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.swing;

import javax.swing.ListModel;

/**
 *
 * @author Randall Wood
 */
public interface EditableListModel<E> extends ListModel<E> {

    boolean isCellEditable(int index);

    void setValueAt(E value, int index);
}
