// ScheduleTableModel.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainSchedule;
import jmri.jmrit.operations.trains.TrainScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;

/**
 * Table Model for edit of a schedule used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2009, 2014
 * @version $Revision$
 */
public class ScheduleTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2920410151470878120L;
	// Defines the columns
	private static final int IDCOLUMN = 0;
	private static final int CURRENTCOLUMN = IDCOLUMN + 1;
	private static final int TYPECOLUMN = CURRENTCOLUMN + 1;
	private static final int SETOUT_DAY_COLUMN = TYPECOLUMN + 1;
	private static final int ROADCOLUMN = SETOUT_DAY_COLUMN + 1;
	private static final int LOADCOLUMN = ROADCOLUMN + 1;
	private static final int SHIPCOLUMN = LOADCOLUMN + 1;
	private static final int DESTCOLUMN = SHIPCOLUMN + 1;
	private static final int TRACKCOLUMN = DESTCOLUMN + 1;
	private static final int PICKUP_DAY_COLUMN = TRACKCOLUMN + 1;
	private static final int COUNTCOLUMN = PICKUP_DAY_COLUMN + 1;
	private static final int WAITCOLUMN = COUNTCOLUMN + 1;
	private static final int UPCOLUMN = WAITCOLUMN + 1;
	private static final int DOWNCOLUMN = UPCOLUMN + 1;
	private static final int DELETECOLUMN = DOWNCOLUMN + 1;

	private static final int HIGHESTCOLUMN = DELETECOLUMN + 1;

	public ScheduleTableModel() {
		super();
	}

	Schedule _schedule;
	Location _location;
	Track _track;
	JTable _table;
	ScheduleEditFrame _frame;
	boolean _matchMode = false;

	synchronized void updateList() {
		if (_schedule == null)
			return;
		// first, remove listeners from the individual objects
		removePropertyChangeScheduleItems();
		_list = _schedule.getItemsBySequenceList();
		// and add them back in
		for (ScheduleItem si : _list) {
			si.addPropertyChangeListener(this);
		}
	}

	List<ScheduleItem> _list = new ArrayList<ScheduleItem>();

	void initTable(ScheduleEditFrame frame, JTable table, Schedule schedule, Location location, Track track) {
		_schedule = schedule;
		_location = location;
		_track = track;
		_table = table;
		_frame = frame;

		// add property listeners
		if (_schedule != null)
			_schedule.addPropertyChangeListener(this);
		_location.addPropertyChangeListener(this);
		_track.addPropertyChangeListener(this);
		initTable(table);
	}

	private void initTable(JTable table) {
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(UPCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(UPCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(DOWNCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(DOWNCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(DELETECOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(DELETECOLUMN).setCellEditor(buttonEditor);
		table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
		table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

		setPreferredWidths(table);

		// set row height
		table.setRowHeight(new JComboBox<Object>().getPreferredSize().height);
		updateList();
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	private void setPreferredWidths(JTable table) {
		if (_frame.loadTableDetails(table))
			return; // done
		log.debug("Setting preferred widths");
		// set column preferred widths
		table.getColumnModel().getColumn(IDCOLUMN).setPreferredWidth(35);
		table.getColumnModel().getColumn(CURRENTCOLUMN).setPreferredWidth(50);
		table.getColumnModel().getColumn(TYPECOLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(SETOUT_DAY_COLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(ROADCOLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(LOADCOLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(SHIPCOLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(DESTCOLUMN).setPreferredWidth(130);
		table.getColumnModel().getColumn(TRACKCOLUMN).setPreferredWidth(130);
		table.getColumnModel().getColumn(PICKUP_DAY_COLUMN).setPreferredWidth(90);
		table.getColumnModel().getColumn(COUNTCOLUMN).setPreferredWidth(45);
		table.getColumnModel().getColumn(WAITCOLUMN).setPreferredWidth(40);
		table.getColumnModel().getColumn(UPCOLUMN).setPreferredWidth(60);
		table.getColumnModel().getColumn(DOWNCOLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(DELETECOLUMN).setPreferredWidth(70);
	}

	public int getRowCount() {
		return _list.size();
	}

	public int getColumnCount() {
		return HIGHESTCOLUMN;
	}

	public String getColumnName(int col) {
		switch (col) {
		case IDCOLUMN:
			return Bundle.getMessage("Id");
		case CURRENTCOLUMN:
			return Bundle.getMessage("Current");
		case TYPECOLUMN:
			return Bundle.getMessage("Type");
		case SETOUT_DAY_COLUMN:
			return Bundle.getMessage("Delivery");
		case ROADCOLUMN:
			return Bundle.getMessage("Road");
		case LOADCOLUMN:
			return Bundle.getMessage("Receive");
		case SHIPCOLUMN:
			return Bundle.getMessage("Ship");
		case DESTCOLUMN:
			return Bundle.getMessage("Destination");
		case TRACKCOLUMN:
			return Bundle.getMessage("Track");
		case PICKUP_DAY_COLUMN:
			return Bundle.getMessage("Pickup");
		case COUNTCOLUMN:
			if (_matchMode)
				return Bundle.getMessage("Hits");
			return Bundle.getMessage("Count");
		case WAITCOLUMN:
			return Bundle.getMessage("Wait");
		case UPCOLUMN:
			return Bundle.getMessage("Up");
		case DOWNCOLUMN:
			return Bundle.getMessage("Down");
		case DELETECOLUMN:
			return Bundle.getMessage("Delete");
		default:
			return "unknown"; // NOI18N
		}
	}

	public Class<?> getColumnClass(int col) {
		switch (col) {
		case IDCOLUMN:
			return String.class;
		case CURRENTCOLUMN:
			return String.class;
		case TYPECOLUMN:
			return String.class;
		case SETOUT_DAY_COLUMN:
			return JComboBox.class;
		case ROADCOLUMN:
			return JComboBox.class;
		case LOADCOLUMN:
			return JComboBox.class;
		case SHIPCOLUMN:
			return JComboBox.class;
		case DESTCOLUMN:
			return JComboBox.class;
		case TRACKCOLUMN:
			return JComboBox.class;
		case PICKUP_DAY_COLUMN:
			return JComboBox.class;
		case COUNTCOLUMN:
			return String.class;
		case WAITCOLUMN:
			return String.class;
		case UPCOLUMN:
			return JButton.class;
		case DOWNCOLUMN:
			return JButton.class;
		case DELETECOLUMN:
			return JButton.class;
		default:
			return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
		case SETOUT_DAY_COLUMN:
		case ROADCOLUMN:
		case LOADCOLUMN:
		case SHIPCOLUMN:
		case DESTCOLUMN:
		case TRACKCOLUMN:
		case PICKUP_DAY_COLUMN:
		case COUNTCOLUMN:
		case WAITCOLUMN:
		case UPCOLUMN:
		case DOWNCOLUMN:
		case DELETECOLUMN:
			return true;
		default:
			return false;
		}
	}

	public Object getValueAt(int row, int col) {
		if (row >= _list.size())
			return "ERROR row " + row; // NOI18N
		ScheduleItem si = _list.get(row);
		if (si == null)
			return "ERROR schedule item unknown " + row; // NOI18N
		switch (col) {
		case IDCOLUMN:
			return si.getId();
		case CURRENTCOLUMN:
			return getCurrentPointer(si);
		case TYPECOLUMN:
			return getType(si);
		case SETOUT_DAY_COLUMN:
			return getSetoutDayComboBox(si);
		case ROADCOLUMN:
			return getRoadComboBox(si);
		case LOADCOLUMN:
			return getLoadComboBox(si);
		case SHIPCOLUMN:
			return getShipComboBox(si);
		case DESTCOLUMN:
			return getDestComboBox(si);
		case TRACKCOLUMN:
			return getTrackComboBox(si);
		case PICKUP_DAY_COLUMN:
			return getPickupDayComboBox(si);
		case COUNTCOLUMN:
			if (_matchMode)
				return si.getHits();
			return si.getCount();
		case WAITCOLUMN:
			return si.getWait();
		case UPCOLUMN:
			return Bundle.getMessage("Up");
		case DOWNCOLUMN:
			return Bundle.getMessage("Down");
		case DELETECOLUMN:
			return Bundle.getMessage("Delete");
		default:
			return "unknown " + col; // NOI18N
		}
	}

	public void setValueAt(Object value, int row, int col) {
		if (value == null) {
			log.debug("Warning schedule table row {} still in edit", row);
			return;
		}
		switch (col) {
		case SETOUT_DAY_COLUMN:
			setSetoutDay(value, row);
			break;
		case ROADCOLUMN:
			setRoad(value, row);
			break;
		case LOADCOLUMN:
			setLoad(value, row);
			break;
		case SHIPCOLUMN:
			setShip(value, row);
			break;
		case DESTCOLUMN:
			setDestination(value, row);
			break;
		case TRACKCOLUMN:
			setTrack(value, row);
			break;
		case PICKUP_DAY_COLUMN:
			setPickupDay(value, row);
			break;
		case COUNTCOLUMN:
			setCount(value, row);
			break;
		case WAITCOLUMN:
			setWait(value, row);
			break;
		case UPCOLUMN:
			moveUpScheduleItem(row);
			break;
		case DOWNCOLUMN:
			moveDownScheduleItem(row);
			break;
		case DELETECOLUMN:
			deleteScheduleItem(row);
			break;
		default:
			break;
		}
	}

	private String getCurrentPointer(ScheduleItem si) {
		if (_track.getCurrentScheduleItem() == si)
			if (si.getCount() > 1)
				return " " + _track.getScheduleCount() + " -->"; // NOI18N
			else
				return "    -->"; // NOI18N
		else
			return "";
	}

	private String getType(ScheduleItem si) {
		if (_track.acceptsTypeName(si.getTypeName()))
			return si.getTypeName();
		else
			return MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si.getTypeName() });
	}

	private JComboBox<String> getRoadComboBox(ScheduleItem si) {
		// log.debug("getRoadComboBox for ScheduleItem "+si.getType());
		JComboBox<String> cb = new JComboBox<>();
		cb.addItem(ScheduleItem.NONE);
		for (String roadName : CarRoads.instance().getNames()) {
			if (_track.acceptsRoadName(roadName)) {
				Car car = CarManager.instance().getByTypeAndRoad(si.getTypeName(), roadName);
				if (car != null)
					cb.addItem(roadName);
			}
		}
		cb.setSelectedItem(si.getRoadName());
		if (!cb.getSelectedItem().equals(si.getRoadName())) {
			String notValid = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si.getRoadName() });
			cb.addItem(notValid);
			cb.setSelectedItem(notValid);
		}
		return cb;
	}

	private JComboBox getSetoutDayComboBox(ScheduleItem si) {
		JComboBox cb = TrainScheduleManager.instance().getSelectComboBox();
		TrainSchedule sch = TrainScheduleManager.instance().getScheduleById(si.getSetoutTrainScheduleId());
		if (sch != null) {
			cb.setSelectedItem(sch);
		}
		if (sch == null && !si.getSetoutTrainScheduleId().equals(ScheduleItem.NONE)) {
			String notValid = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si
					.getSetoutTrainScheduleId() });
			cb.addItem(notValid);
			cb.setSelectedItem(notValid);
		}
		return cb;
	}
	
	private JComboBox getPickupDayComboBox(ScheduleItem si) {
		JComboBox cb = TrainScheduleManager.instance().getSelectComboBox();
		TrainSchedule sch = TrainScheduleManager.instance().getScheduleById(si.getPickupTrainScheduleId());
		if (sch != null) {
			cb.setSelectedItem(sch);
		}
		if (sch == null && !si.getPickupTrainScheduleId().equals(ScheduleItem.NONE)) {
			String notValid = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si
					.getPickupTrainScheduleId() });
			cb.addItem(notValid);
			cb.setSelectedItem(notValid);
		}
		return cb;
	}


	private JComboBox<String> getLoadComboBox(ScheduleItem si) {
		// log.debug("getLoadComboBox for ScheduleItem "+si.getType());
		JComboBox<String> cb = CarLoads.instance().getSelectComboBox(si.getTypeName());
		filterLoads(si, cb); // remove loads not accepted by this track
		cb.setSelectedItem(si.getReceiveLoadName());
		if (!cb.getSelectedItem().equals(si.getReceiveLoadName())) {
			String notValid = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si
					.getReceiveLoadName() });
			cb.addItem(notValid);
			cb.setSelectedItem(notValid);
		}
		return cb;
	}

	private JComboBox<String> getShipComboBox(ScheduleItem si) {
		// log.debug("getShipComboBox for ScheduleItem "+si.getType());
		JComboBox<String> cb = CarLoads.instance().getSelectComboBox(si.getTypeName());
		cb.setSelectedItem(si.getShipLoadName());
		if (!cb.getSelectedItem().equals(si.getShipLoadName())) {
			String notValid = MessageFormat
					.format(Bundle.getMessage("NotValid"), new Object[] { si.getShipLoadName() });
			cb.addItem(notValid);
			cb.setSelectedItem(notValid);
		}
		return cb;
	}

	private JComboBox getDestComboBox(ScheduleItem si) {
		// log.debug("getDestComboBox for ScheduleItem "+si.getType());
		JComboBox cb = LocationManager.instance().getComboBox();
		filterDestinations(cb, si.getTypeName());
		cb.setSelectedItem(si.getDestination());
		if (si.getDestination() != null && !cb.getSelectedItem().equals(si.getDestination())) {
			String notValid = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si.getDestination() });
			cb.addItem(notValid);
			cb.setSelectedItem(notValid);
		}
		return cb;
	}

	private JComboBox getTrackComboBox(ScheduleItem si) {
		// log.debug("getTrackComboBox for ScheduleItem "+si.getType());
		JComboBox cb = new JComboBox();
		if (si.getDestination() != null) {
			Location dest = si.getDestination();
			dest.updateComboBox(cb);
			filterTracks(dest, cb, si.getTypeName(), si.getRoadName(), si.getShipLoadName());
			cb.setSelectedItem(si.getDestinationTrack());
			if (si.getDestinationTrack() != null && !cb.getSelectedItem().equals(si.getDestinationTrack())) {
				String notValid = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[] { si
						.getDestinationTrack() });
				cb.addItem(notValid);
				cb.setSelectedItem(notValid);
			}
		}
		return cb;
	}

	// set the count or hits if in match mode
	private void setCount(Object value, int row) {
		ScheduleItem si = _list.get(row);
		int count;
		try {
			count = Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			log.error("Schedule count or hits must be a number");
			return;
		}
		// we don't care what value the user sets the hit count
		if (_matchMode) {
			si.setHits(count);
			return;
		}
		if (count < 1) {
			log.error("Schedule count must be greater than 0");
			return;
		}
		if (count > 100) {
			log.warn("Schedule count must be 100 or less");
			count = 100;
		}
		si.setCount(count);
	}

	private void setWait(Object value, int row) {
		ScheduleItem si = _list.get(row);
		int wait;
		try {
			wait = Integer.parseInt(value.toString());
		} catch (NumberFormatException e) {
			log.error("Schedule wait must be a number");
			return;
		}
		if (wait < 0) {
			log.error("Schedule wait must be a positive number");
			return;
		}
		if (wait > 10) {
			log.warn("Schedule wait must be 10 or less");
			wait = 10;
		}
		si.setWait(wait);
	}

	private void setSetoutDay(Object value, int row) {
		ScheduleItem si = _list.get(row);
		Object obj = ((JComboBox<?>) value).getSelectedItem();
		if (obj == null) {
			si.setSetoutTrainScheduleId(ScheduleItem.NONE);
		} else if (obj.getClass().equals(TrainSchedule.class)) {
			si.setSetoutTrainScheduleId(((TrainSchedule) obj).getId());
		}
	}
	
	private void setPickupDay(Object value, int row) {
		ScheduleItem si = _list.get(row);
		Object obj = ((JComboBox<?>) value).getSelectedItem();
		if (obj == null) {
			si.setPickupTrainScheduleId(ScheduleItem.NONE);
		} else if (obj.getClass().equals(TrainSchedule.class)) {
			si.setPickupTrainScheduleId(((TrainSchedule) obj).getId());
		}
	}

	// note this method looks for String "Not Valid <>"
	private void setRoad(Object value, int row) {
		ScheduleItem si = _list.get(row);
		String road = (String) ((JComboBox<?>) value).getSelectedItem();
		if (checkForNotValidString(road))
			si.setRoadName(road);
	}

	// note this method looks for String "Not Valid <>"
	private void setLoad(Object value, int row) {
		ScheduleItem si = _list.get(row);
		String load = (String) ((JComboBox<?>) value).getSelectedItem();
		if (checkForNotValidString(load))
			si.setReceiveLoadName(load);
	}

	// note this method looks for String "Not Valid <>"
	private void setShip(Object value, int row) {
		ScheduleItem si = _list.get(row);
		String load = (String) ((JComboBox<?>) value).getSelectedItem();
		if (checkForNotValidString(load))
			si.setShipLoadName(load);
	}

	/*
	 * Returns true if string is okay, doesn't have the string "Not Valid <>".
	 */
	private boolean checkForNotValidString(String s) {
		if (s.length() < 12)
			return true;
		String test = s.substring(0, 11);
		if (test.equals(Bundle.getMessage("NotValid").substring(0, 11)))
			return false;
		return true;
	}

	private void setDestination(Object value, int row) {
		ScheduleItem si = _list.get(row);
		si.setDestinationTrack(null);
		Location dest = (Location) ((JComboBox<?>) value).getSelectedItem();
		si.setDestination(dest);
		fireTableCellUpdated(row, TRACKCOLUMN);
	}

	private void setTrack(Object value, int row) {
		ScheduleItem si = _list.get(row);
		Track track = (Track) ((JComboBox<?>) value).getSelectedItem();
		si.setDestinationTrack(track);
	}

	private void moveUpScheduleItem(int row) {
		log.debug("move schedule item up");
		_schedule.moveItemUp(_list.get(row));
	}

	private void moveDownScheduleItem(int row) {
		log.debug("move schedule item down");
		_schedule.moveItemDown(_list.get(row));
	}

	private void deleteScheduleItem(int row) {
		log.debug("Delete schedule item");
		_schedule.deleteItem(_list.get(row));
	}

	// remove destinations that don't service the car's type
	private void filterDestinations(JComboBox<Location> cb, String carType) {
		for (int i = 1; i < cb.getItemCount(); i++) {
			Location dest = cb.getItemAt(i);
			if (!dest.acceptsTypeName(carType)) {
				cb.removeItem(dest);
				i--;
			}
		}
	}

	// remove destination tracks that don't service the car's type, road, or load
	private void filterTracks(Location loc, JComboBox<Track> cb, String carType, String carRoad, String carLoad) {
		List<Track> tracks = loc.getTrackList();
		for (Track track : tracks) {
			if (!track.acceptsTypeName(carType) || track.getTrackType().equals(Track.STAGING)
					|| (!carRoad.equals(ScheduleItem.NONE) && !track.acceptsRoadName(carRoad))
					|| (!carLoad.equals(ScheduleItem.NONE) && !track.acceptsLoad(carLoad, carType)))
				cb.removeItem(track);
		}
	}

	// remove receive loads not serviced by track
	private void filterLoads(ScheduleItem si, JComboBox<String> cb) {
		for (int i = cb.getItemCount() - 1; i > 0; i--) {
			String loadName = cb.getItemAt(i);
			if (!loadName.equals(CarLoads.NONE) && !_track.acceptsLoad(loadName, si.getTypeName()))
				cb.removeItem(loadName);
		}
	}

	private int _trainDirection = Setup.getDirectionInt(Setup.getComboBox().getItemAt(0));

	public int getLastTrainDirection() {
		return _trainDirection;
	}

	public void setMatchMode(boolean mode) {
		if (mode != _matchMode) {
			_matchMode = mode;
			fireTableStructureChanged();
			initTable(_table);
		}
	}

	// this table listens for changes to a schedule and it's car types
	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
					.getNewValue());
		if (e.getPropertyName().equals(Schedule.LISTCHANGE_CHANGED_PROPERTY)) {
			updateList();
			fireTableDataChanged();
		}
		if (e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.ROADS_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.LOADS_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.SCHEDULE_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)) {
			fireTableDataChanged();
		}
		// update hit count or other schedule item?
		if (e.getSource().getClass().equals(ScheduleItem.class)) {
			ScheduleItem item = (ScheduleItem) e.getSource();
			int row = _list.indexOf(item);
			if (Control.showProperty && log.isDebugEnabled())
				log.debug("Update schedule item table row: {}", row);
			if (row >= 0)
				fireTableRowsUpdated(row, row);
		}
	}

	private void removePropertyChangeScheduleItems() {
		for (ScheduleItem si : _list) {
			// if object has been deleted, it's not here; ignore it
			if (si != null)
				si.removePropertyChangeListener(this);
		}
	}

	public void dispose() {
		if (log.isDebugEnabled())
			log.debug("dispose");
		if (_schedule != null) {
			removePropertyChangeScheduleItems();
			_schedule.removePropertyChangeListener(this);
		}
		_location.removePropertyChangeListener(this);
		_track.removePropertyChangeListener(this);

	}

	static Logger log = LoggerFactory.getLogger(ScheduleTableModel.class.getName());
}
