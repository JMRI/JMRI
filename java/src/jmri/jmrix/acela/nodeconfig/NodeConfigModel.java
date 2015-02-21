/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrix.acela.nodeconfig;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Bob
 */
public abstract class NodeConfigModel extends AbstractTableModel {

    /**
     *
     */
    private static final long serialVersionUID = -8887616188889211179L;
    protected int numrows = 16;          // Trying to make a property here
    protected boolean editmode = true;  // Trying to make a property here

    public int getRowCount() {
        return numrows;
    }

    public void setNumRows(int r) {
        numrows = r;
    }

    public void setEditMode(boolean b) {
        editmode = b;
    }

    public boolean getEditMode() {
        return editmode;
    }

}
