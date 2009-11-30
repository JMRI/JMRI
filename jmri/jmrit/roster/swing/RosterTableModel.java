// RosterTableModel.java

package jmri.jmrit.roster.swing;

import jmri.jmrit.roster.*;

/**
 * Table data model for display of Roster variable values.
 *<P>
 * Any desired ordering, etc, is handled outside this class.
 *<P>
 * The initial implementation doesn't automatically update when
 * roster entries change, doesn't allow updating of the entries,
 * and only shows some of the fields.  But it's a start....
 *
 * @author              Bob Jacobsen   Copyright (C) 2009
 * @version             $Revision: 1.3 $
 * @since 2.7.5
 */
public class RosterTableModel extends javax.swing.table.AbstractTableModel {

    static final int IDCOL = 0;
    static final int ROADNUMBERCOL = 1;
    static final int ROADNAMECOL = 2;
    static final int MFGCOL = 3;

    static final int NUMCOL = MFGCOL+1;
    
    public int getRowCount() {
        return Roster.instance().numEntries();
    }
    public int getColumnCount( ){
        return NUMCOL;
    }
    public String getColumnName(int col) {
        switch (col) {
        case IDCOL:         return "ID";
        case ROADNUMBERCOL: return "Road Number";
        case ROADNAMECOL:   return "Road Name";
        case MFGCOL:        return "Manufacturer";
        default:            return "<UNKNOWN>";
        }
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
        // get roster entry for row
        RosterEntry re = Roster.instance().getEntry(row);
        if (re == null){
        	log.debug("roster entry is null!");
        	return "Error";
        }    
        switch (col) {
        case IDCOL:         return re.getId();
        case ROADNUMBERCOL: return re.getRoadNumber();
        case ROADNAMECOL:   return re.getRoadName();
        case MFGCOL:        return re.getMfg();
        default:            return "<UNKNOWN>";
        }
    }

    public void setValueAt(Object value, int row, int col) {
    }

    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterTableModel.class.getName());
}
