package jmri.jmrit.beantable.routetable;

import jmri.InstanceManager;

import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeListener;

/**
 * Base table model for selecting outputs.
 */
abstract class RouteOutputModel extends AbstractTableModel implements PropertyChangeListener {

    @Override
    public Class<?> getColumnClass(int c) {
        if (c == INCLUDE_COLUMN) {
            return Boolean.class;
        } else {
            return String.class;
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            // a new NamedBean is available in the manager
            fireTableDataChanged();
        }
    }

    void dispose() {
        InstanceManager.turnoutManagerInstance().removePropertyChangeListener(this);
    }

    @Override
    public String getColumnName(int c) {
        return RouteAddFrame.COLUMN_NAMES[c];
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public boolean isCellEditable(int r, int c) {
        return ((c == INCLUDE_COLUMN) || (c == STATE_COLUMN));
    }

    static final int SNAME_COLUMN = 0;
    static final int UNAME_COLUMN = 1;
    static final int INCLUDE_COLUMN = 2;
    static final int STATE_COLUMN = 3;

}
