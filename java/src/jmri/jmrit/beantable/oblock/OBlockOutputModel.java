package jmri.jmrit.beantable.oblock;

import jmri.InstanceManager;
import jmri.jmrit.beantable.routetable.RouteAddFrame;

import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeListener;

/**
 * Base table model for selecting outputs (Turnouts in OPaths).
 *
 * Adapted from jmri.jmrit.beantable.routetable.RouteOutputModel
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016, 2020
 * @author Paul Bender Copyright (C) 2020
 */
abstract class OBlockOutputModel extends AbstractTableModel implements PropertyChangeListener {

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
        return BlockPathEditFrame.COLUMN_NAMES[c];
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
