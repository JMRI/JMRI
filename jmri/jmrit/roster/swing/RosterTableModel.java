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
 * @author              Bob Jacobsen   Copyright (C) 2009, 2010
 * @version             $Revision: 1.4 $
 * @since 2.7.5
 */
public class RosterTableModel extends javax.swing.table.AbstractTableModel {

    static final int IDCOL = 0;
    static final int ADDRESSCOL = 1;
    static final int DECODERCOL = 2;
    static final int ROADNAMECOL = 3;
    static final int ROADNUMBERCOL = 4;
    static final int MFGCOL = 5;
    static final int OWNERCOL = 6;
    static final int DATEUPDATECOL = 7;

    static final int NUMCOL = 7+1;
    
    public int getRowCount() {
        return Roster.instance().numEntries();
    }
    public int getColumnCount( ){
        return NUMCOL;
    }
    public String getColumnName(int col) {
        switch (col) {
        case IDCOL:         return "ID";
        case ADDRESSCOL:    return "DCC Address";
        case DECODERCOL:    return "Decoder";
        case ROADNAMECOL:   return "Road Name";
        case ROADNUMBERCOL: return "Road Number";
        case MFGCOL:        return "Manufacturer";
        case OWNERCOL:      return "Owner";
        case DATEUPDATECOL: return "Last Updated";
        default:            return "<UNKNOWN>";
        }
    }
    
    public Class<?> getColumnClass(int col) {
        if (col == ADDRESSCOL) return Integer.class;
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
        case ADDRESSCOL:    return new Integer(re.getDccLocoAddress().getNumber());
        case DECODERCOL:    return re.getDecoderModel();
        case ROADNAMECOL:   return re.getRoadName();
        case ROADNUMBERCOL: return re.getRoadNumber();
        case MFGCOL:        return re.getMfg();
        case OWNERCOL:      return re.getOwner();
        case DATEUPDATECOL: return re.getDateUpdated();
        default:            return "<UNKNOWN>";
        }
    }

    public void setValueAt(Object value, int row, int col) {
    }

    public int getPreferredWidth(int column) {
        int retval = 20; // always take some width
        retval = Math.max(retval, new javax.swing.JLabel(getColumnName(column)).getPreferredSize().width+15);  // leave room for sorter arrow
        for (int row = 0 ; row < getRowCount(); row++)
            retval = Math.max(retval, new javax.swing.JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            
        return retval+5;
    }
    
    // drop listeners
    public void dispose() {
    }
    
    static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RosterTableModel.class.getName());
}
