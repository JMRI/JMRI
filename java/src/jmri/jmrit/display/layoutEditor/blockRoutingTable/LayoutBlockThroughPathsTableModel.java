// LayoutBlockNeighbourTableModel.java

package jmri.jmrit.display.layoutEditor.blockRoutingTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.display.layoutEditor.*;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;

/**
 * Table data model for display the through path of a layoutblock
 *<P>
 * Any desired ordering, etc, is handled outside this class.
 *
 * @author              Kevin Dickerson   Copyright (C) 2011
 * @version             $Revision$
 */
public class LayoutBlockThroughPathsTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    public static final int SOURCECOL= 0;
    static final int DESTINATIONCOL = 1;
    static final int ACTIVECOL = 2;

    static final int NUMCOL = 2+1;
    
    boolean editable = false;
    
    public LayoutBlockThroughPathsTableModel(boolean editable, LayoutBlock lBlock) {
        this.editable = editable;
        this.lBlock = lBlock;
        lBlock.addPropertyChangeListener(this);
    }
    
    public int getRowCount() {
        return lBlock.getNumberOfThroughPaths();
    }

    public int getColumnCount( ){
        return NUMCOL;
    }
    @Override
    public String getColumnName(int col) {
        switch (col) {
        case SOURCECOL:         return rb.getString("Source");
        case DESTINATIONCOL:    return rb.getString("Destination");
        case ACTIVECOL:         return rb.getString("Active");
        
        default:            return "<UNKNOWN>";
        }
    }
    
    @Override
    public Class<?> getColumnClass(int col) {
        return String.class;
    }
    
    /**
     * Editable state must be set in ctor.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("length")) {
            fireTableDataChanged();
        } else if (e.getPropertyName().equals("through-path-removed")) {
            fireTableDataChanged();
        } else if (e.getPropertyName().equals("through-path-added")) {
            fireTableDataChanged();
        } else if (matchPropertyName(e)){
            // a value changed.  Find it, to avoid complete redraw
            int row;
            row = (Integer)e.getNewValue();
            // since we can add columns, the entire row is marked as updated
            //int row = sysNameList.indexOf(name);
            fireTableRowsUpdated(row, row);
        }
    }
    
    	protected boolean matchPropertyName(java.beans.PropertyChangeEvent e) {
		return (e.getPropertyName().indexOf("path")>=0);
	}
    
    /**
     * Provides the empty String if attribute doesn't exist.
     */
    public Object getValueAt(int row, int col) {
        // get roster entry for row
        if (lBlock == null){
        	log.debug("layout Block is null!");
        	return "Error";
        }    
        switch (col) {
        case SOURCECOL:    return lBlock.getThroughPathSource(row).getDisplayName();
        case ACTIVECOL:    Boolean mutual = lBlock.isThroughPathActive(row);
                            if(mutual)
                                return rb.getString("Yes");
                            return rb.getString("No");
        case DESTINATIONCOL:   return lBlock.getThroughPathDestination(row).getDisplayName();
        default:            return "<UNKNOWN>";
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        return;
    }

    public int getPreferredWidth(int column) {
        int retval = 20; // always take some width
        retval = Math.max(retval, new javax.swing.JLabel(getColumnName(column)).getPreferredSize().width+15);  // leave room for sorter arrow
        for (int row = 0 ; row < getRowCount(); row++) {
            if (getColumnClass(column).equals(String.class))
                retval = Math.max(retval, new javax.swing.JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
            else if (getColumnClass(column).equals(Integer.class))
                retval = Math.max(retval, new javax.swing.JLabel(getValueAt(row, column).toString()).getPreferredSize().width);
        }    
        return retval+5;
    }
    
    // drop listeners
    public void dispose() {
    }

    public jmri.Manager getManager() { return jmri.InstanceManager.layoutBlockManagerInstance(); }

    LayoutBlock lBlock;
    
    static final Logger log = LoggerFactory.getLogger(LayoutBlockThroughPathsTableModel.class.getName());
}
