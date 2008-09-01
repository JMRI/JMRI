// LocationsTableModel.java

package jmri.jmrit.operations.locations;

import java.awt.event.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import jmri.*;
import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of locations used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.1 $
 */
public class LocationsTableModel extends javax.swing.table.AbstractTableModel implements ActionListener, PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
   
    LocationManager manager;						// There is only one manager
 
    // Defines the columns
    private static final int IDCOLUMN   = 0;
    private static final int NAMECOLUMN   = 1;
    private static final int LENGTHCOLUMN = 2;
    private static final int USEDLENGTHCOLUMN = 3;
    private static final int NUMBEROFCARS = 4;
    private static final int CARSPICKUP = 5;
    private static final int CARSDROP = 6;
    private static final int EDITCOLUMN = 7;
    
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
    	_sort = sort;
        updateList();
        fireTableDataChanged();
    }
     
    synchronized void updateList() {
		// first, remove listeners from the individual objects
    	removePropertyChangeLocations();
    	
		if (_sort == SORTBYID)
			sysList = manager.getLocationsByIdList();
		else
			sysList = manager.getLocationsByNameList();
		// and add them back in
		for (int i = 0; i < sysList.size(); i++){
//			log.debug("location ids: " + (String) sysList.get(i));
			manager.getLocationById((String) sysList.get(i))
					.addPropertyChangeListener(this);
		}
	}

	List sysList = null;
    
	void initTable(JTable table) {
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(200);
		table.getColumnModel().getColumn(LENGTHCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(USEDLENGTHCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(NUMBEROFCARS).setPreferredWidth(60);
		table.getColumnModel().getColumn(CARSPICKUP).setPreferredWidth(60);
		table.getColumnModel().getColumn(CARSDROP).setPreferredWidth(60);
		table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(70);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}
    
    public int getRowCount() { return sysList.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: return rb.getString("Id");
        case NAMECOLUMN: return rb.getString("Name");
        case LENGTHCOLUMN: return rb.getString("Length");
        case USEDLENGTHCOLUMN: return rb.getString("Used");
        case NUMBEROFCARS: return rb.getString("Cars");
        case CARSPICKUP: return rb.getString("Pickup");
        case CARSDROP: return rb.getString("Drop");
        case EDITCOLUMN: return "";		//edit column
        default: return "unknown";
        }
    }

    public Class getColumnClass(int col) {
        switch (col) {
        case IDCOLUMN: return String.class;
        case NAMECOLUMN: return String.class;
        case LENGTHCOLUMN: return String.class;
        case USEDLENGTHCOLUMN: return String.class;
        case NUMBEROFCARS: return String.class;
        case CARSPICKUP: return String.class;
        case CARSDROP: return String.class;
        case EDITCOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col) {
        switch (col) {
        case EDITCOLUMN: 
        	return true;
        default: 
        	return false;
        }
    }

    public String getName(int row) {  // name is text number
        return "";
    }

    public String getValString(int row) {
        return "";
    }

    public Object getValueAt(int row, int col) {
    	String locId = (String)sysList.get(row);
    	Location l = manager.getLocationById(locId);
        switch (col) {
        case IDCOLUMN: return l.getId();
        case NAMECOLUMN: return l.getName();
        case LENGTHCOLUMN: return Integer.toString(l.getLength());
        case USEDLENGTHCOLUMN: return Integer.toString(l.getUsedLength());
        case NUMBEROFCARS: return Integer.toString(l.getNumberCars());
        case CARSPICKUP: return Integer.toString(l.getPickupCars());
        case CARSDROP: return Integer.toString(l.getDropCars());
        case EDITCOLUMN: return "Edit";
        default: return "unknown "+col;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        switch (col) {
        case EDITCOLUMN: editLocation (row);
        	break;
        default:
            break;
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) log.debug("action command: "+e.getActionCommand());
        char b = e.getActionCommand().charAt(0);
        int row = Integer.valueOf(e.getActionCommand().substring(1)).intValue();
        if (log.isDebugEnabled()) log.debug("event on "+b+" row "+row);
    }
    
    LocationsEditFrame lef = null;
    private void editLocation (int row){
    	log.debug("Edit location");
    	if (lef != null)
    		lef.dispose();
    	lef = new LocationsEditFrame();
    	Location loc = manager.getLocationById((String)sysList.get(row));
     	lef.setTitle("Edit Location");
    	lef.initComponents(loc);
   }

    public void propertyChange(PropertyChangeEvent e) {
    	if (Control.showProperty && log.isDebugEnabled()) log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	 if (e.getPropertyName().equals("listLength")) {
             updateList();
             fireTableDataChanged();
    	 }
    	 else {
    		 String locId = ((Location) e.getSource()).getId();
    		 int row = sysList.indexOf(locId);
    		 if (Control.showProperty && log.isDebugEnabled()) log.debug("Update location table row: "+row + " id: " + locId);
    		 if (row >= 0)
    			 fireTableRowsUpdated(row, row);
    	 }
    }
    
    private void removePropertyChangeLocations() {
    	if (sysList != null) {
    		for (int i = 0; i < sysList.size(); i++) {
    			// if object has been deleted, it's not here; ignore it
    			Location l = manager.getLocationById((String) sysList.get(i));
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocationsTableModel.class.getName());
}

