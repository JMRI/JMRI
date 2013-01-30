// LocationsTableModel.java

package jmri.jmrit.operations.locations;

import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.util.List;
import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of locations used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision$
 */
public class LocationsTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {
  
    LocationManager manager;						// There is only one manager
 
    // Defines the columns
    public static final int IDCOLUMN   = 0;
    public static final int NAMECOLUMN   = 1;
    public static final int LENGTHCOLUMN = 2;
    public static final int USEDLENGTHCOLUMN = 3;
    public static final int ROLLINGSTOCK = 4;
    public static final int PICKUPS = 5;
    public static final int DROPS = 6;
    public static final int ACTIONCOLUMN = 7;
    public static final int EDITCOLUMN = 8;
    
    private static final int HIGHESTCOLUMN = EDITCOLUMN+1;

    public LocationsTableModel() {
        super();
        manager = LocationManager.instance();
        manager.addPropertyChangeListener(this);
        updateList();
    }
    
    public final int SORTBYNAME = 1;
    public final int SORTBYID = 2;
    
    private int _sort = SORTBYNAME;
    
    public void setSort (int sort){
    	synchronized (this) {
    		_sort = sort;
    	}
    	updateList();
    	fireTableDataChanged();
    }
     
    private synchronized void updateList() {
		// first, remove listeners from the individual objects
    	removePropertyChangeLocations();
    	
		if (_sort == SORTBYID)
			sysList = manager.getLocationsByIdList();
		else
			sysList = manager.getLocationsByNameList();
		// and add them back in
		for (int i = 0; i < sysList.size(); i++){
//			log.debug("location ids: " + (String) sysList.get(i));
			manager.getLocationById(sysList.get(i))
					.addPropertyChangeListener(this);
		}
	}

	List<String> sysList = null;
    
	void initTable(JTable table) {
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(ACTIONCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(ACTIONCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(40);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(200);
		table.getColumnModel().getColumn(LENGTHCOLUMN).setPreferredWidth(Math.max(60, new JLabel(getColumnName(LENGTHCOLUMN)).getPreferredSize().width+10));
		table.getColumnModel().getColumn(USEDLENGTHCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(ROLLINGSTOCK).setPreferredWidth(Math.max(80, new JLabel(getColumnName(ROLLINGSTOCK)).getPreferredSize().width+10));
		table.getColumnModel().getColumn(PICKUPS).setPreferredWidth(Math.max(60, new JLabel(getColumnName(PICKUPS)).getPreferredSize().width+10));
		table.getColumnModel().getColumn(DROPS).setPreferredWidth(Math.max(60, new JLabel(getColumnName(DROPS)).getPreferredSize().width+10));
		table.getColumnModel().getColumn(ACTIONCOLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(70);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
    
    public synchronized int getRowCount() { return sysList.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: return Bundle.getMessage("Id");
        case NAMECOLUMN: return Bundle.getMessage("Name");
        case LENGTHCOLUMN: return Bundle.getMessage("Length");
        case USEDLENGTHCOLUMN: return Bundle.getMessage("Used");
        case ROLLINGSTOCK: return Bundle.getMessage("RollingStock");
        case PICKUPS: return Bundle.getMessage("Pickup");
        case DROPS: return Bundle.getMessage("Drop");
        case ACTIONCOLUMN: return Bundle.getMessage("Action");
        case EDITCOLUMN: return Bundle.getMessage("Edit");		//edit column
        default: return "unknown";	// NOI18N
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case IDCOLUMN: return String.class;
        case NAMECOLUMN: return String.class;
        case LENGTHCOLUMN: return String.class;
        case USEDLENGTHCOLUMN: return String.class;
        case ROLLINGSTOCK: return String.class;
        case PICKUPS: return String.class;
        case DROPS: return String.class;
        case ACTIONCOLUMN: return JButton.class;
        case EDITCOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case EDITCOLUMN: 
        case ACTIONCOLUMN:	
        	return true;
        default: 
        	return false;
        }
    }

    public synchronized Object getValueAt(int row, int col) {
    	// Funky code to put the lef frame in focus after the edit table buttons is used.
    	// The button editor for the table does a repaint of the button cells after the setValueAt code
    	// is called which then returns the focus back onto the table.  We need the edit frame
    	// in focus.
    	if (focusLef){
    		focusLef = false;
    		lef.requestFocus();
    	}
    	if (ymf != null) {
    		ymf.requestFocus();
    		ymf = null;
    	}
    	if (row >= getRowCount())
    		return "ERROR row "+row;	// NOI18N
    	String locId = sysList.get(row);
    	Location l = manager.getLocationById(locId);
    	if (l == null)
    		return "ERROR location unknown "+row;	// NOI18N
        switch (col) {
        case IDCOLUMN: return l.getId();
        case NAMECOLUMN: return l.getName();
        case LENGTHCOLUMN: return Integer.toString(l.getLength());
        case USEDLENGTHCOLUMN: return Integer.toString(l.getUsedLength());
        case ROLLINGSTOCK: return Integer.toString(l.getNumberRS());
        case PICKUPS: return Integer.toString(l.getPickupRS());
        case DROPS: return Integer.toString(l.getDropRS());
        case ACTIONCOLUMN: return Bundle.getMessage("Yardmaster");
        case EDITCOLUMN: return Bundle.getMessage("Edit");
        default: return "unknown "+col;	// NOI18N
        }
    }

    public void setValueAt(Object value, int row, int col) {
    	switch (col) {
    	case ACTIONCOLUMN: launchYardmaster (row);
    	break;
    	case EDITCOLUMN: editLocation (row);
    	break;
    	default:
    		break;
    	}
    }
    
    boolean focusLef = false;
    LocationEditFrame lef = null;
    private synchronized void editLocation (int row){
    	log.debug("Edit location");
    	if (lef != null)
    		lef.dispose();
    	lef = new LocationEditFrame();
    	Location loc = manager.getLocationById(sysList.get(row));
     	lef.setTitle(Bundle.getMessage("TitleLocationEdit"));
    	lef.initComponents(loc);
    	focusLef = true;
   }
   
    YardmasterFrame ymf = null;
    private synchronized void launchYardmaster (int row){
    	log.debug("Yardmaster");
    	ymf = new YardmasterFrame();
    	Location loc = manager.getLocationById(sysList.get(row));
    	ymf.initComponents(loc);
   }

    public void propertyChange(PropertyChangeEvent e) {
    	if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	 if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
             updateList();
             fireTableDataChanged();
    	 }
    	 else if (e.getSource().getClass().equals(Location.class)){
    		 String locId = ((Location) e.getSource()).getId();
    		 int row = sysList.indexOf(locId);
    		 if (Control.showProperty && log.isDebugEnabled()) log.debug("Update location table row: "+row + " id: " + locId);
    		 if (row >= 0)
    			 fireTableRowsUpdated(row, row);
    	 }
    }
    
    private synchronized void removePropertyChangeLocations() {
    	if (sysList != null) {
    		for (int i = 0; i < sysList.size(); i++) {
    			// if object has been deleted, it's not here; ignore it
    			Location l = manager.getLocationById(sysList.get(i));
    			if (l != null)
    				l.removePropertyChangeListener(this);
    		}
    	}
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
       	if (lef != null)
    		lef.dispose();
        manager.removePropertyChangeListener(this);
        removePropertyChangeLocations();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocationsTableModel.class.getName());
}

