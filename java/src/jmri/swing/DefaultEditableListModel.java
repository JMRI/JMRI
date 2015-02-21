/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.swing;

import javax.swing.DefaultListModel;

/**
 *
 * @author rhwood
 */
public class DefaultEditableListModel<E> extends DefaultListModel<E> implements EditableListModel<E> {

    /**
     *
     */
    private static final long serialVersionUID = -4856688370300717415L;

    @Override
    public boolean isCellEditable(int index) {
        return true;
    }

    @Override
    public void setValueAt(E value, int index) {
        super.setElementAt(value, index);
    }
}
