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
public class DefaultEditableListModel extends DefaultListModel implements EditableListModel {

    @Override
    public boolean isCellEditable(int index) {
        return true;
    }

    @Override
    public void setValueAt(Object value, int index) {
        super.setElementAt(value, index);
    }
}
