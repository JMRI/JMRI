// TrackTableModel.java

package jmri.jmrit.operations.locations;

import org.apache.log4j.Logger;
import java.beans.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import java.util.ArrayList;
import java.util.List;
import jmri.jmrit.operations.setup.Control;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of tracks used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012
 * @version   $Revision$
 */
public class TrackTableModel extends AbstractTableModel implements PropertyChangeListener {
    
    public static final int SORTBYNAME = 1;
    public static final int SORTBYID = 2;
	
	protected Location _location;
	protected List<String> tracksList = new ArrayList<String>();
	protected int _sort = SORTBYNAME;
	protected String _trackType = "";
	protected boolean _showPoolColumn = false;
	protected JTable _table;
	
	// Defines the columns
    protected static final int IDCOLUMN = 0;
    protected static final int NAMECOLUMN = 1;
    protected static final int LENGTHCOLUMN = 2;
    protected static final int USEDLENGTHCOLUMN = 3;
    protected static final int RESERVEDCOLUMN = 4;  
    protected static final int CARSCOLUMN = 5;
    protected static final int ENGINESCOLUMN = 6;
    protected static final int PICKUPSCOLUMN = 7;
    protected static final int DROPSCOLUMN = 8;
    protected static final int POOLCOLUMN = 9;
    protected static final int PLANPICKUPCOLUMN = 10;
    protected static final int EDITCOLUMN = 11;
    
    protected static final int HIGHESTCOLUMN = EDITCOLUMN+1;

    public TrackTableModel(){
        super();
    }
     
    public void setSort (int sort){
    	_sort = sort;
        updateList();
        fireTableDataChanged();
    }
    
    synchronized void updateList(){
    	if (_location == null)
    		return;
    	// first, remove listeners from the individual objects
    	removePropertyChangeTracks();

    	tracksList = _location.getTrackIdsByNameList(_trackType);
    	// and add them back in
    	for (int i = 0; i < tracksList.size(); i++){
    		//log.debug("tracks ids: " + tracksList.get(i));
    		_location.getTrackById(tracksList.get(i)).addPropertyChangeListener(this);
    	}
		if (_location.hasPools() && !_showPoolColumn){
			_showPoolColumn = true;
			fireTableStructureChanged();
			initTable();
		}
    }
    
	protected void initTable(JTable table, Location location, String trackType){
		_table = table;
		_location = location;
		synchronized (this){
			_trackType = trackType;
		}
		if (_location != null)
			_location.addPropertyChangeListener(this);		
		initTable();
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        updateList();
	}
	
	private void initTable(){
		// Use XTableColumnModel so we can control which columns are visible
		XTableColumnModel tcm = new XTableColumnModel();
		_table.setColumnModel(tcm);
		_table.createDefaultColumnsFromModel();

		// set column preferred widths
		tcm.getColumn(IDCOLUMN).setPreferredWidth(40);
		tcm.getColumn(NAMECOLUMN).setPreferredWidth(200);
		tcm.getColumn(LENGTHCOLUMN).setPreferredWidth(Math.max(50, new JLabel(getColumnName(LENGTHCOLUMN)).getPreferredSize().width+10));
		tcm.getColumn(USEDLENGTHCOLUMN).setPreferredWidth(50);
		tcm.getColumn(RESERVEDCOLUMN).setPreferredWidth(Math.max(65, new JLabel(getColumnName(RESERVEDCOLUMN)).getPreferredSize().width+10));
		tcm.getColumn(ENGINESCOLUMN).setPreferredWidth(60);
		tcm.getColumn(CARSCOLUMN).setPreferredWidth(60);
		tcm.getColumn(PICKUPSCOLUMN).setPreferredWidth(Math.max(60, new JLabel(getColumnName(PICKUPSCOLUMN)).getPreferredSize().width+10));
		tcm.getColumn(DROPSCOLUMN).setPreferredWidth(Math.max(60, new JLabel(getColumnName(DROPSCOLUMN)).getPreferredSize().width+10));
		tcm.getColumn(POOLCOLUMN).setPreferredWidth(70);
		tcm.getColumn(PLANPICKUPCOLUMN).setPreferredWidth(70);
		tcm.getColumn(EDITCOLUMN).setPreferredWidth(70);
		tcm.getColumn(EDITCOLUMN).setCellRenderer(new ButtonRenderer());
		tcm.getColumn(EDITCOLUMN).setCellEditor(new ButtonEditor(new JButton()));

		setColumnsVisible();
	}
	
	protected void setColumnsVisible(){
		XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
		// don't show planned pick ups unless there are some
		TableColumn column  = tcm.getColumnByModelIndex(PLANPICKUPCOLUMN);
		tcm.setColumnVisible(column, _location.hasPlannedPickups());
		// don't show pool column if there aren't any pools
		column  = tcm.getColumnByModelIndex(POOLCOLUMN);
		tcm.setColumnVisible(column, _location.hasPools());
	}
    
    public int getRowCount(){ return tracksList.size(); }

    public int getColumnCount(){ 
    	return HIGHESTCOLUMN;
    }

    public String getColumnName(int col){
        switch (col) {
        case IDCOLUMN: return Bundle.getMessage("Id");
        case NAMECOLUMN: return Bundle.getMessage("TrackName");
        case LENGTHCOLUMN: return Bundle.getMessage("Length");
        case USEDLENGTHCOLUMN: return Bundle.getMessage("Used");
        case RESERVEDCOLUMN: return Bundle.getMessage("Reserved");
        case ENGINESCOLUMN: return Bundle.getMessage("Engines");
        case CARSCOLUMN: return Bundle.getMessage("Cars");
        case PICKUPSCOLUMN: return Bundle.getMessage("Pickup");
        case DROPSCOLUMN: return Bundle.getMessage("Drop");
        case POOLCOLUMN: return Bundle.getMessage("Pool");
        case PLANPICKUPCOLUMN: return Bundle.getMessage("PlanPickUp");
        case EDITCOLUMN: return "";		//edit column
        default: return "unknown"; // NOI18N
        }
    }

    public Class<?> getColumnClass(int col){
        switch (col) {
        case IDCOLUMN: return String.class;
        case NAMECOLUMN: return String.class;
        case LENGTHCOLUMN: return String.class;
        case USEDLENGTHCOLUMN: return String.class;
        case RESERVEDCOLUMN: return String.class;
        case ENGINESCOLUMN: return String.class;
        case CARSCOLUMN: return String.class;
        case PICKUPSCOLUMN: return String.class;
        case DROPSCOLUMN: return String.class;
        case POOLCOLUMN: return String.class;
        case PLANPICKUPCOLUMN: return String.class;
        case EDITCOLUMN: return JButton.class;
        default: return null;
        }
    }

    public boolean isCellEditable(int row, int col){
        switch (col) {
        case EDITCOLUMN: 
        	return true;
        default: 
        	return false;
        }
    }

    public Object getValueAt(int row, int col){
       	// Funky code to put the edit frame in focus after the edit table buttons is used.
    	// The button editor for the table does a repaint of the button cells after the setValueAt code
    	// is called which then returns the focus back onto the table.  We need the edit frame
    	// in focus.
    	if (focusEditFrame){
    		focusEditFrame = false;
    		tef.requestFocus();
    	}
    	if(row >= tracksList.size())
    		return "ERROR row "+row; // NOI18N
    	String tracksId = tracksList.get(row);
    	Track track = _location.getTrackById(tracksId);
    	if (track == null)
    		return "ERROR track unknown "+row; // NOI18N
        switch (col) {
        case IDCOLUMN: return track.getId();
        case NAMECOLUMN: return track.getName();
        case LENGTHCOLUMN: return Integer.toString(track.getLength());
        case USEDLENGTHCOLUMN: return Integer.toString(track.getUsedLength());
        case RESERVEDCOLUMN: return Integer.toString(track.getReserved());
        case ENGINESCOLUMN: return Integer.toString(track.getNumberEngines());
        case CARSCOLUMN: return Integer.toString(track.getNumberCars());
        case PICKUPSCOLUMN: return Integer.toString(track.getPickupRS());
        case DROPSCOLUMN: return Integer.toString(track.getDropRS());
        case POOLCOLUMN: return track.getPoolName();
        case PLANPICKUPCOLUMN: 
        	if (track.getIgnoreUsedLengthPercentage() > 0)
        		return track.getIgnoreUsedLengthPercentage()+"%";
        	return "";
        case EDITCOLUMN: return Bundle.getMessage("Edit");
        default: return "unknown "+col; // NOI18N
        }
    }

    public void setValueAt(Object value, int row, int col){
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
    	tef.setTitle(Bundle.getMessage("EditTrack"));
    	focusEditFrame = true;
    }

    // this table listens for changes to a location and it's tracks
    public void propertyChange(PropertyChangeEvent e){
    	if (Control.showProperty && log.isDebugEnabled()) 
    		log.debug("Property change " +e.getPropertyName()+ " old: "+e.getOldValue()+ " new: "+e.getNewValue());
    	if (e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY)){
    		updateList();
    		fireTableDataChanged();
    	}
    	if (e.getSource().getClass().equals(Track.class) 
    			&& (e.getPropertyName().equals(Track.POOL_CHANGED_PROPERTY))
    			|| e.getPropertyName().equals(Track.PLANNEDPICKUPS_CHANGED_PROPERTY)){
    		setColumnsVisible();
    	}
    }
    
    protected void removePropertyChangeTracks(){
    	for (int i = 0; i < tracksList.size(); i++){
    		// if object has been deleted, it's not here; ignore it
    		Track t = _location.getTrackById(tracksList.get(i));
    		if (t != null)
    			t.removePropertyChangeListener(this);
    	}
    }

    public void dispose(){
        if (log.isDebugEnabled()) log.debug("dispose");
        removePropertyChangeTracks();
        if (_location != null)
        	_location.removePropertyChangeListener(this);
        if (tef != null)
        	tef.dispose();
        tracksList.clear();
        fireTableDataChanged();
    }

    static Logger log = Logger.getLogger(TrackTableModel.class.getName());
}

