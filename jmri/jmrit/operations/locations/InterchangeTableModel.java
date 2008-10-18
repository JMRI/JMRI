// InterchangeTableModel.java

package jmri.jmrit.operations.locations;

import java.awt.event.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;

import jmri.*;
import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of interchange locations used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.3 $
 */
public class InterchangeTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
    
    // Defines the columns
    private static final int IDCOLUMN   = 0;
    private static final int NAMECOLUMN   = 1;
    private static final int LENGTHCOLUMN = 2;
    private static final int USEDLENGTHCOLUMN = 3;
    private static final int RESERVEDCOLUMN = 4;  
    private static final int ROLLINGSTOCK = 5;
    private static final int PICKUPS = 6;
    private static final int DROPS = 7;
    private static final int EDITCOLUMN = 8;
    
    private static final int HIGHESTCOLUMN = EDITCOLUMN+1;

    public InterchangeTableModel() {
        super();
    }
    
    public final int SORTBYNAME = 1;
    public final int SORTBYID = 2;
    
    private int _sort = SORTBYNAME;
    
    public void setSort (int sort){
    	_sort = sort;
        updateList();
        fireTableDataChanged();
    }
     
    Location _location;
    
    synchronized void updateList() {
    	if (_location == null)
    		return;
		// first, remove listeners from the individual objects
    	removePropertyChangeInterchanges();
    	
//		if (_sort == SORTBYID)
//			interchangeList = _location.getInterchangesByIdList();
//		else
			interchangeList = _location.getTracksByNameList(Track.INTERCHANGE);
		// and add them back in
		for (int i = 0; i < interchangeList.size(); i++){
			log.debug("interchange ids: " + (String) interchangeList.get(i));
			_location.getTrackById((String) interchangeList.get(i))
					.addPropertyChangeListener(this);
		}
	}

	List interchangeList = new ArrayList();
    
	void initTable(JTable table, Location location) {
		_location = location;
		if (_location != null)
			_location.addPropertyChangeListener(this);
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(2);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(150);
		table.getColumnModel().getColumn(LENGTHCOLUMN).setPreferredWidth(4);
		table.getColumnModel().getColumn(USEDLENGTHCOLUMN).setPreferredWidth(4);
		table.getColumnModel().getColumn(RESERVEDCOLUMN).setPreferredWidth(4);
		table.getColumnModel().getColumn(ROLLINGSTOCK).setPreferredWidth(4);
		table.getColumnModel().getColumn(PICKUPS).setPreferredWidth(4);
		table.getColumnModel().getColumn(DROPS).setPreferredWidth(4);
		table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(30);
        updateList();
	}
    
    public int getRowCount() { return interchangeList.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: return rb.getString("Id");
        case NAMECOLUMN: return rb.getString("InterchangeName");
        case LENGTHCOLUMN: return rb.getString("Length");
        case USEDLENGTHCOLUMN: return rb.getString("Used");
        case RESERVEDCOLUMN: return rb.getString("Reserved");
        case ROLLINGSTOCK: return rb.getString("RollingStock");
        case PICKUPS: return rb.getString("Pickup");
        case DROPS: return rb.getString("Drop");
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
        case RESERVEDCOLUMN: return String.class;
        case ROLLINGSTOCK: return String.class;
        case PICKUPS: return String.class;
        case DROPS: return String.class;
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

    public Object getValueAt(int row, int col) {
    	String interchangeId = (String)interchangeList.get(row);
    	Track sl = _location.getTrackById(interchangeId);
        switch (col) {
        case IDCOLUMN: return sl.getId();
        case NAMECOLUMN: return sl.getName();
        case LENGTHCOLUMN: return Integer.toString(sl.getLength());
        case USEDLENGTHCOLUMN: return Integer.toString(sl.getUsedLength());
        case RESERVEDCOLUMN: return Integer.toString(sl.getReserved());
        case ROLLINGSTOCK: return Integer.toString(sl.getNumberRS());
        case PICKUPS: return Integer.toString(sl.getPickupRS());
        case DROPS: return Integer.toString(sl.getDropRS());
        case EDITCOLUMN: return rb.getString("Edit");
        default: return "unknown "+col;
        }
    }

    public void setValueAt(Object value, int row, int col) {
        switch (col) {
        case EDITCOLUMN: editInterchange(row);
        	break;
        default:
            break;
        }
    }

    InterchangeEditFrame yef = null;
    
    private void editInterchange (int row){
    	log.debug("Edit interchange");
    	if (yef != null)
    		yef.dispose();
    	yef = new InterchangeEditFrame();
		String interchangeId = (String)interchangeList.get(row);
    	Track interchange = _location.getTrackById(interchangeId);
    	yef.initComponents(_location, interchange);
    	yef.setTitle(rb.getString("EditInterchange"));
    }

    // this table listens for changes to a location and it's interchanges
    public void propertyChange(PropertyChangeEvent e) {
    	if (Control.showProperty && log.isDebugEnabled()) 
    		log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(Location.INTERCHANGELISTLENGTH)) {
    		updateList();
    		fireTableDataChanged();
    	}

    	if (e.getSource() != _location){
    		String type = ((Track) e.getSource()).getLocType();
    		if (type.equals(Track.INTERCHANGE)){
    			String interchangeId = ((Track) e.getSource()).getId();
    			int row = interchangeList.indexOf(interchangeId);
    			if (Control.showProperty && log.isDebugEnabled()) 
    				log.debug("Update interchange table row: "+ row + " id: " + interchangeId);
    			if (row >= 0)
    				fireTableRowsUpdated(row, row);
    		}
    	}
    }
    
    private void removePropertyChangeInterchanges() {
    	for (int i = 0; i < interchangeList.size(); i++) {
    		// if object has been deleted, it's not here; ignore it
    		Track y = _location.getTrackById((String) interchangeList.get(i));
    		if (y != null)
    			y.removePropertyChangeListener(this);
    	}
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        removePropertyChangeInterchanges();
        if (_location != null)
        	_location.removePropertyChangeListener(this);
        if (yef != null)
        	yef.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(InterchangeTableModel.class.getName());
}

