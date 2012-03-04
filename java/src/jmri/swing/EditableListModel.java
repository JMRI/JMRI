/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.swing;

import javax.swing.ListModel;

/**
 *
 * @author rhwood
 */
public interface EditableListModel extends ListModel {

    public boolean isCellEditable(int index);

    public void setValueAt(Object value, int index);
}
