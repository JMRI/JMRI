// TrackTableModel.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

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
 * @version $Revision$
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
	protected static final int ID_COLUMN = 0;
	protected static final int NAME_COLUMN = 1;
	protected static final int LENGTH_COLUMN = 2;
	protected static final int USED_LENGTH_COLUMN = 3;
	protected static final int RESERVED_COLUMN = 4;
	protected static final int CARS_COLUMN = 5;
	protected static final int LOCOS_COLUMN = 6;
	protected static final int PICKUPS_COLUMN = 7;
	protected static final int SETOUT_COLUMN = 8;
	protected static final int ROAD_COLUMN = 9;
	protected static final int LOAD_COLUMN = 10;
	protected static final int SHIP_COLUMN = 11;
	protected static final int DESTINATION_COLUMN = 12;
	protected static final int POOL_COLUMN = 13;
	protected static final int PLANPICKUP_COLUMN = 14;
	protected static final int EDIT_COLUMN = 15;

	protected static final int HIGHESTCOLUMN = EDIT_COLUMN + 1;

	public TrackTableModel() {
		super();
	}

	public void setSort(int sort) {
		_sort = sort;
		updateList();
		fireTableDataChanged();
	}

	synchronized void updateList() {
		if (_location == null)
			return;
		// first, remove listeners from the individual objects
		removePropertyChangeTracks();

		tracksList = _location.getTrackIdsByNameList(_trackType);
		// and add them back in
		for (int i = 0; i < tracksList.size(); i++) {
			// log.debug("tracks ids: " + tracksList.get(i));
			_location.getTrackById(tracksList.get(i)).addPropertyChangeListener(this);
		}
		if (_location.hasPools() && !_showPoolColumn) {
			_showPoolColumn = true;
			fireTableStructureChanged();
			initTable();
		}
	}

	protected void initTable(JTable table, Location location, String trackType) {
		_table = table;
		_location = location;
		synchronized (this) {
			_trackType = trackType;
		}
		if (_location != null)
			_location.addPropertyChangeListener(this);
		initTable();
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		updateList();
	}

	private void initTable() {
		// Use XTableColumnModel so we can control which columns are visible
		XTableColumnModel tcm = new XTableColumnModel();
		_table.setColumnModel(tcm);
		_table.createDefaultColumnsFromModel();

		// set column preferred widths
		tcm.getColumn(ID_COLUMN).setPreferredWidth(40);
		tcm.getColumn(NAME_COLUMN).setPreferredWidth(200);
		tcm.getColumn(LENGTH_COLUMN).setPreferredWidth(
				Math.max(50, new JLabel(getColumnName(LENGTH_COLUMN)).getPreferredSize().width + 10));
		tcm.getColumn(USED_LENGTH_COLUMN).setPreferredWidth(50);
		tcm.getColumn(RESERVED_COLUMN).setPreferredWidth(
				Math.max(65, new JLabel(getColumnName(RESERVED_COLUMN)).getPreferredSize().width + 10));
		tcm.getColumn(LOCOS_COLUMN).setPreferredWidth(60);
		tcm.getColumn(CARS_COLUMN).setPreferredWidth(60);
		tcm.getColumn(PICKUPS_COLUMN).setPreferredWidth(
				Math.max(60, new JLabel(getColumnName(PICKUPS_COLUMN)).getPreferredSize().width + 10));
		tcm.getColumn(SETOUT_COLUMN).setPreferredWidth(
				Math.max(60, new JLabel(getColumnName(SETOUT_COLUMN)).getPreferredSize().width + 10));
		tcm.getColumn(LOAD_COLUMN).setPreferredWidth(50);
		tcm.getColumn(SHIP_COLUMN).setPreferredWidth(50);
		tcm.getColumn(ROAD_COLUMN).setPreferredWidth(50);
		tcm.getColumn(DESTINATION_COLUMN).setPreferredWidth(50);
		tcm.getColumn(POOL_COLUMN).setPreferredWidth(70);
		tcm.getColumn(PLANPICKUP_COLUMN).setPreferredWidth(70);
		tcm.getColumn(EDIT_COLUMN).setPreferredWidth(70);
		tcm.getColumn(EDIT_COLUMN).setCellRenderer(new ButtonRenderer());
		tcm.getColumn(EDIT_COLUMN).setCellEditor(new ButtonEditor(new JButton()));

		setColumnsVisible();
	}

	// only show "Load", "Ship", "Road", "Destination", "Planned" and "Pool" if they are needed
	protected void setColumnsVisible() {
		XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
		tcm.setColumnVisible(tcm.getColumnByModelIndex(LOAD_COLUMN), _location.hasLoadRestrications());
		tcm.setColumnVisible(tcm.getColumnByModelIndex(SHIP_COLUMN), _location.hasShipLoadRestrications());
		tcm.setColumnVisible(tcm.getColumnByModelIndex(ROAD_COLUMN), _location.hasRoadRestrications());
		tcm.setColumnVisible(tcm.getColumnByModelIndex(DESTINATION_COLUMN), _location.hasDestinationRestrications());
		tcm.setColumnVisible(tcm.getColumnByModelIndex(PLANPICKUP_COLUMN), _location.hasPlannedPickups());
		tcm.setColumnVisible(tcm.getColumnByModelIndex(POOL_COLUMN), _location.hasPools());
	}

	public int getRowCount() {
		return tracksList.size();
	}

	public int getColumnCount() {
		return HIGHESTCOLUMN;
	}

	public String getColumnName(int col) {
		switch (col) {
		case ID_COLUMN:
			return Bundle.getMessage("Id");
		case NAME_COLUMN:
			return Bundle.getMessage("TrackName");
		case LENGTH_COLUMN:
			return Bundle.getMessage("Length");
		case USED_LENGTH_COLUMN:
			return Bundle.getMessage("Used");
		case RESERVED_COLUMN:
			return Bundle.getMessage("Reserved");
		case LOCOS_COLUMN:
			return Bundle.getMessage("Engines");
		case CARS_COLUMN:
			return Bundle.getMessage("Cars");
		case PICKUPS_COLUMN:
			return Bundle.getMessage("Pickup");
		case SETOUT_COLUMN:
			return Bundle.getMessage("Drop");
		case LOAD_COLUMN:
			return Bundle.getMessage("Load");
		case SHIP_COLUMN:
			return Bundle.getMessage("Ship");
		case ROAD_COLUMN:
			return Bundle.getMessage("Road");
		case DESTINATION_COLUMN:
			return Bundle.getMessage("Dest");
		case POOL_COLUMN:
			return Bundle.getMessage("Pool");
		case PLANPICKUP_COLUMN:
			return Bundle.getMessage("PlanPickUp");
		case EDIT_COLUMN:
			return Bundle.getMessage("Edit");
		default:
			return "unknown"; // NOI18N
		}
	}

	public Class<?> getColumnClass(int col) {
		switch (col) {
		case ID_COLUMN:
			return String.class;
		case NAME_COLUMN:
			return String.class;
		case LENGTH_COLUMN:
			return String.class;
		case USED_LENGTH_COLUMN:
			return String.class;
		case RESERVED_COLUMN:
			return String.class;
		case LOCOS_COLUMN:
			return String.class;
		case CARS_COLUMN:
			return String.class;
		case PICKUPS_COLUMN:
			return String.class;
		case SETOUT_COLUMN:
			return String.class;
		case LOAD_COLUMN:
			return String.class;
		case SHIP_COLUMN:
			return String.class;
		case ROAD_COLUMN:
			return String.class;
		case DESTINATION_COLUMN:
			return String.class;
		case POOL_COLUMN:
			return String.class;
		case PLANPICKUP_COLUMN:
			return String.class;
		case EDIT_COLUMN:
			return JButton.class;
		default:
			return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
		case EDIT_COLUMN:
			return true;
		default:
			return false;
		}
	}

	public Object getValueAt(int row, int col) {
		// Funky code to put the edit frame in focus after the edit table buttons is used.
		// The button editor for the table does a repaint of the button cells after the setValueAt code
		// is called which then returns the focus back onto the table. We need the edit frame
		// in focus.
		if (focusEditFrame) {
			focusEditFrame = false;
			tef.requestFocus();
		}
		if (row >= tracksList.size())
			return "ERROR row " + row; // NOI18N
		String tracksId = tracksList.get(row);
		Track track = _location.getTrackById(tracksId);
		if (track == null)
			return "ERROR track unknown " + row; // NOI18N
		switch (col) {
		case ID_COLUMN:
			return track.getId();
		case NAME_COLUMN:
			return track.getName();
		case LENGTH_COLUMN:
			return Integer.toString(track.getLength());
		case USED_LENGTH_COLUMN:
			return Integer.toString(track.getUsedLength());
		case RESERVED_COLUMN:
			return Integer.toString(track.getReserved());
		case LOCOS_COLUMN:
			return Integer.toString(track.getNumberEngines());
		case CARS_COLUMN:
			return Integer.toString(track.getNumberCars());
		case PICKUPS_COLUMN:
			return Integer.toString(track.getPickupRS());
		case SETOUT_COLUMN:
			return Integer.toString(track.getDropRS());
		case LOAD_COLUMN:
			return getModifiedString(track.getLoadNames().length, track.getLoadOption().equals(Track.ALL_LOADS), track
					.getLoadOption().equals(Track.INCLUDE_LOADS));
		case SHIP_COLUMN:
			return getModifiedString(track.getShipLoadNames().length,
					track.getShipLoadOption().equals(Track.ALL_LOADS), track.getShipLoadOption().equals(
							Track.INCLUDE_LOADS));
		case ROAD_COLUMN:
			return getModifiedString(track.getRoadNames().length, track.getRoadOption().equals(Track.ALL_ROADS), track
					.getRoadOption().equals(Track.INCLUDE_ROADS));
		case DESTINATION_COLUMN: {
			int length = track.getDestinationListSize();
			if (track.getDestinationOption().equals(Track.EXCLUDE_DESTINATIONS))
				length = LocationManager.instance().getLocationsByIdList().size() - length;
			return getModifiedString(length, track.getDestinationOption().equals(Track.ALL_DESTINATIONS), track
					.getDestinationOption().equals(Track.INCLUDE_DESTINATIONS));
		}
	case POOL_COLUMN:
			return track.getPoolName();
		case PLANPICKUP_COLUMN:
			if (track.getIgnoreUsedLengthPercentage() > 0)
				return track.getIgnoreUsedLengthPercentage() + "%";
			return "";
		case EDIT_COLUMN:
			return Bundle.getMessage("Edit");
		default:
			return "unknown " + col; // NOI18N
		}
	}

	private String getModifiedString(int number, boolean all, boolean accept) {
		if (all)
			return "";
		if (accept)
			return "A " + Integer.toString(number); // NOI18N
		return "E " + Integer.toString(number); // NOI18N
	}

	public void setValueAt(Object value, int row, int col) {
		switch (col) {
		case EDIT_COLUMN:
			editTrack(row);
			break;
		default:
			break;
		}
	}

	boolean focusEditFrame = false;
	TrackEditFrame tef = null;

	protected void editTrack(int row) {
		log.debug("Edit tracks");
		if (tef != null) {
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
	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
					+ e.getNewValue());
		if (e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY)) {
			updateList();
			fireTableDataChanged();
		}
		if (e.getSource().getClass().equals(Track.class)
				&& (e.getPropertyName().equals(Track.LOADS_CHANGED_PROPERTY)
						|| e.getPropertyName().equals(Track.ROADS_CHANGED_PROPERTY)
						|| e.getPropertyName().equals(Track.DESTINATION_OPTIONS_CHANGED_PROPERTY)
						|| e.getPropertyName().equals(Track.POOL_CHANGED_PROPERTY) || e.getPropertyName().equals(
						Track.PLANNEDPICKUPS_CHANGED_PROPERTY))) {
			setColumnsVisible();
		}
	}

	protected void removePropertyChangeTracks() {
		for (int i = 0; i < tracksList.size(); i++) {
			// if object has been deleted, it's not here; ignore it
			Track t = _location.getTrackById(tracksList.get(i));
			if (t != null)
				t.removePropertyChangeListener(this);
		}
	}

	public void dispose() {
//		if (log.isDebugEnabled())
//			log.debug("dispose");
		removePropertyChangeTracks();
		if (_location != null)
			_location.removePropertyChangeListener(this);
		if (tef != null)
			tef.dispose();
		tracksList.clear();
		fireTableDataChanged();
	}

	static Logger log = LoggerFactory.getLogger(TrackTableModel.class.getName());
}
