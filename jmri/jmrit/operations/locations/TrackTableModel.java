// TrackTableModel.java

package jmri.jmrit.operations.locations;

import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of tracks used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 * @version   $Revision: 1.14 $
 */
public class TrackTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
    
    public static final int SORTBYNAME = 1;
    public static final int SORTBYID = 2;
	
	protected Location _location;
	protected List<String> tracksList = new ArrayList<String>();
	protected int _sort = SORTBYNAME;
	protected String _trackType = "";
	
	// Defines the columns
    protected static final int IDCOLUMN   = 0;
    protected static final int NAMECOLUMN   = 1;
    protected static final int LENGTHCOLUMN = 2;
    protected static final int USEDLENGTHCOLUMN = 3;
    protected static final int RESERVEDCOLUMN = 4;  
    protected static final int CARS = 5;
    protected static final int ENGINES = 6;
    protected static final int PICKUPS = 7;
    protected static final int DROPS = 8;
    protected static final int EDITCOLUMN = 9;
    
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

    	tracksList = _location.getTracksByNameList(_trackType);
    	// and add them back in
    	for (int i = 0; i < tracksList.size(); i++){
    		log.debug("tracks ids: " + tracksList.get(i));
    		_location.getTrackById(tracksList.get(i))
    		.addPropertyChangeListener(this);
    	}
    }
    
	protected void initTable(JTable table, Location location, String trackType) {
		_location = location;
		synchronized (this){
			_trackType = trackType;
		}
		if (_location != null)
			_location.addPropertyChangeListener(this);
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(40);
		table.getColumnModel().getColumn(NAMECOLUMN).setPreferredWidth(200);
		table.getColumnModel().getColumn(LENGTHCOLUMN).setPreferredWidth(50);
		table.getColumnModel().getColumn(USEDLENGTHCOLUMN).setPreferredWidth(45);
		table.getColumnModel().getColumn(RESERVEDCOLUMN).setPreferredWidth(65);
		table.getColumnModel().getColumn(ENGINES).setPreferredWidth(60);
		table.getColumnModel().getColumn(CARS).setPreferredWidth(40);
		table.getColumnModel().getColumn(PICKUPS).setPreferredWidth(55);
		table.getColumnModel().getColumn(DROPS).setPreferredWidth(55);
		table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(60);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
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
        case ENGINES: return rb.getString("Engines");
        case CARS: return rb.getString("Cars");
        case PICKUPS: return rb.getString("Pickup");
        case DROPS: return rb.getString("Drop");
        case EDITCOLUMN: return "";		//edit column
        default: return "unknown";
        }
    }

    public Class<?> getColumnClass(int col) {
        switch (col) {
        case IDCOLUMN: return String.class;
        case NAMECOLUMN: return String.class;
        case LENGTHCOLUMN: return String.class;
        case USEDLENGTHCOLUMN: return String.class;
        case RESERVEDCOLUMN: return String.class;
        case ENGINES: return String.class;
        case CARS: return String.class;
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
       	// Funky code to put the edit frame in focus after the edit table buttons is used.
    	// The button editor for the table does a repaint of the button cells after the setValueAt code
    	// is called which then returns the focus back onto the table.  We need the edit frame
    	// in focus.
    	if (focusEditFrame){
    		focusEditFrame = false;
    		tef.requestFocus();
    	}
    	if(row >= tracksList.size())
    		return "ERROR row "+row;
    	String tracksId = tracksList.get(row);
    	Track track = _location.getTrackById(tracksId);
    	if (track == null)
    		return "ERROR track unknown "+row;
        switch (col) {
        case IDCOLUMN: return track.getId();
        case NAMECOLUMN: return track.getName();
        case LENGTHCOLUMN: return Integer.toString(track.getLength());
        case USEDLENGTHCOLUMN: return Integer.toString(track.getUsedLength());
        case RESERVEDCOLUMN: return Integer.toString(track.getReserved());
        case ENGINES: return Integer.toString(track.getNumberEngines());
        case CARS: return Integer.toString(track.getNumberCars());
        case PICKUPS: return Integer.toString(track.getPickupRS());
        case DROPS: return Integer.toString(track.getDropRS());
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

    boolean focusEditFrame = false;
    TrackEditFrame tef = null;
    
    protected void editTrack (int row){
    	log.debug("Edit tracks");
    	if (tef != null){
    		tef.dispose();
    	}
    	tef = new TrackEditFrame();
		String tracksId = tracksList.get(row);
    	Track tracks = _location.getTrackById(tracksId);
    	tef.initComponents(_location, tracks);
    	tef.setTitle(rb.getString("EditTrack"));
    	focusEditFrame = true;
    }

    // this table listens for changes to a location and it's tracks
    public void propertyChange(PropertyChangeEvent e) {
    	if (log.isDebugEnabled()) 
    		log.debug("Unexpected Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    }
    
    protected void removePropertyChangeTracks() {
    	for (int i = 0; i < tracksList.size(); i++) {
    		// if object has been deleted, it's not here; ignore it
    		Track y = _location.getTrackById(tracksList.get(i));
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
        tracksList.clear();
        fireTableDataChanged();
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrackTableModel.class.getName());
}

