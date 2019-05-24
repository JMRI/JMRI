package jmri.jmrit.roster.swing.attributetable;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;

/**
 * Table data model for display of Roster attribute values.
 * <p>
 * Any desired ordering, etc. is handled outside this class.
 * <p>
 * The initial implementation doesn't automatically update when roster entries
 * change, doesn't allow updating of the entries, and only shows some of the
 * fields. But it's a start....
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @since 2.7.5
 */
public class AttributeTableModel extends javax.swing.table.AbstractTableModel {

    @Override
    public int getRowCount() {
        return Roster.getDefault().numEntries();
    }

    @Override
    public int getColumnCount() {
        return Roster.getDefault().getAllAttributeKeys().size();
    }

    @Override
    public String getColumnName(int col) {
        return (String) Roster.getDefault().getAllAttributeKeys().toArray()[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    /**
     * This implementation can't edit the values yet.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    /**
     * Provides the empty String if attribute doesn't exist.
     */
    @Override
    public Object getValueAt(int row, int col) {
        // get column key
        String key = getColumnName(col);
        // get roster entry for row
        RosterEntry re = Roster.getDefault().getEntry(row);
        String retval = re.getAttribute(key);
        if (retval != null) {
            return retval;
        }
        return "";
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
    }
}
