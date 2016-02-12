// AttributeTableModel.java
package jmri.jmrit.roster.swing.attributetable;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table data model for display of Roster attribute values.
 * <P>
 * Any desired ordering, etc, is handled outside this class.
 * <P>
 * The initial implementation doesn't automatically update when roster entries
 * change, doesn't allow updating of the entries, and only shows some of the
 * fields. But it's a start....
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @version $Revision$
 * @since 2.7.5
 */
public class AttributeTableModel extends javax.swing.table.AbstractTableModel {

    /**
     *
     */
    private static final long serialVersionUID = 59936474957867177L;

    public int getRowCount() {
        return Roster.instance().numEntries();
    }

    public int getColumnCount() {
        return Roster.instance().getAllAttributeKeys().size();
    }

    public String getColumnName(int col) {
        return (String) Roster.instance().getAllAttributeKeys().toArray()[col];
    }

    public Class<?> getColumnClass(int col) {
        return String.class;
    }

    /**
     * This implementation can't edit the values yet
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    /**
     * Provides the empty String if attribute doesn't exist.
     */
    public Object getValueAt(int row, int col) {
        // get column key
        String key = getColumnName(col);
        // get roster entry for row
        RosterEntry re = Roster.instance().getEntry(row);
        if (re == null) {
            return "";
        }
        String retval = re.getAttribute(key);
        if (retval != null) {
            return retval;
        }
        return "";
    }

    public void setValueAt(Object value, int row, int col) {
    }

    private final static Logger log = LoggerFactory.getLogger(AttributeTableModel.class.getName());
}
