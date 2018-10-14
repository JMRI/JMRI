package jmri.jmrix.acela.nodeconfig;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Bob
 */
public abstract class NodeConfigModel extends AbstractTableModel {

    protected int numrows = 16;          // Trying to make a property here
    protected boolean editmode = true;   // Trying to make a property here

    @Override
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
