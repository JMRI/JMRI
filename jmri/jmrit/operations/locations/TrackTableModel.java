// TrackTableModel.java

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
 * Table Model for edit of tracks used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.1 $
 */
public class TrackTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
    
    public static final int SORTBYNAME = 1;
    public static final int SORTBYID = 2;
	
	protected Location _location;
	protected List tracksList = new ArrayList();
	protected int _sort = SORTBYNAME;
	protected String _trackType = "";
	
	// Defines the columns
    protected static final int IDCOLUMN   = 0;
    protected static final int NAMECOLUMN   = 1;
    protected static final int LENGTHCOLUMN = 2;
    protected static final int USEDLENGTHCOLUMN = 3;
    protected static final int RESERVEDCOLUMN = 4;  
    protected static final int ROLLINGSTOCK = 5;
    protected static final int PICKUPS = 6;
    protected static final int DROPS = 7;
    protected static final int EDITCOLUMN = 8;
    
    protected static final int HIGHESTCOLUMN = EDITCOLUMN+1;

    public TrackTableModel() {
        super();
    }
     
    public void setSort (int sort){
    	_sort = sort;
        updateList();
        fireTableDataChanged();
    }
    
    synchronized void updateList() {
    	if (_location == null)
    		return;
		// first, remove listeners from the individual objects
    	removePropertyChangeTracks();
    	
//		if (_sort == SORTBYID)
//			tracksList = _location.getTracksByIdList();
//		else
			tracksList = _location.getTracksByNameList(_trackType);
		// and add them back in
		for (int i = 0; i < tracksList.size(); i++){
			log.debug("tracks ids: " + (String) tracksList.get(i));
			_location.getTrackById((String) tracksList.get(i))
					.addPropertyChangeListener(this);
		}
	}
    
	protected void initTable(JTable table, Location location, String trackType) {
		_location = location;
		_trackType = trackType;
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
    
    public int getRowCount() { return tracksList.size(); }

    public int getColumnCount( ){ return HIGHESTCOLUMN;}

    public String getColumnName(int col) {
        switch (col) {
        case IDCOLUMN: return rb.getString("Id");
        case NAMECOLUMN: return rb.getString("TrackName");
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
    	String tracksId = (String)tracksList.get(row);
    	Track sl = _location.getTrackById(tracksId);
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
        case EDITCOLUMN: editTrack(row);
        	break;
        default:
            break;
        }
    }

    TrackEditFrame tef = null;
    
    protected void editTrack (int row){
    	log.debug("Edit tracks");
    	if (tef != null){
    		tef.dispose();
    	}
    	tef = new TrackEditFrame();
		String tracksId = (String)tracksList.get(row);
    	Track tracks = _location.getTrackById(tracksId);
    	tef.initComponents(_location, tracks);
    	tef.setTitle(rb.getString("EditTrack"));
    }

    // this table listens for changes to a location and it's tracks
    public void propertyChange(PropertyChangeEvent e) {
    	if (log.isDebugEnabled()) 
    		log.debug("Unexpected Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    }
    
    protected void removePropertyChangeTracks() {
    	for (int i = 0; i < tracksList.size(); i++) {
    		// if object has been deleted, it's not here; ignore it
    		Track y = _location.getTrackById((String) tracksList.get(i));
    		if (y != null)
    			y.removePropertyChangeListener(this);
    	}
    }

    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        removePropertyChangeTracks();
        if (_location != null)
        	_location.removePropertyChangeListener(this);
        if (tef != null)
        	tef.dispose();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TrackTableModel.class.getName());
}

