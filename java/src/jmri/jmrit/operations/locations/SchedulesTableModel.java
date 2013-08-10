// SchedulesTableModel.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.*;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import java.text.MessageFormat;
import java.util.List;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import java.util.Hashtable;

/**
 * Table Model for edit of schedules used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2009, 2011, 2013
 * @version $Revision$
 */
public class SchedulesTableModel extends javax.swing.table.AbstractTableModel implements
		PropertyChangeListener {

	ScheduleManager scheduleManager; // There is only one manager

	// Defines the columns
	private static final int ID_COLUMN = 0;
	private static final int NAME_COLUMN = ID_COLUMN + 1;
	private static final int SCHEDULE_STATUS_COLUMN = NAME_COLUMN + 1;
	private static final int SPUR_NUMBER_COLUMN = SCHEDULE_STATUS_COLUMN + 1;
	private static final int SPUR_COLUMN = SPUR_NUMBER_COLUMN + 1;
	private static final int STATUS_COLUMN = SPUR_COLUMN + 1;
	private static final int MODE_COLUMN = STATUS_COLUMN + 1;
	private static final int EDIT_COLUMN = MODE_COLUMN + 1;
	private static final int DELETE_COLUMN = EDIT_COLUMN + 1;

	private static final int HIGHEST_COLUMN = DELETE_COLUMN + 1;

	public SchedulesTableModel() {
		super();
		scheduleManager = ScheduleManager.instance();
		scheduleManager.addPropertyChangeListener(this);
		updateList();
	}

	public final int SORTBYNAME = 1;
	public final int SORTBYID = 2;

	private int _sort = SORTBYNAME;

	public void setSort(int sort) {
		synchronized (this) {
			_sort = sort;
		}
		updateList();
		fireTableDataChanged();
	}

	synchronized void updateList() {
		// first, remove listeners from the individual objects
		removePropertyChangeSchedules();
		removePropertyChangeTracks();

		if (_sort == SORTBYID)
			sysList = scheduleManager.getSchedulesByIdList();
		else
			sysList = scheduleManager.getSchedulesByNameList();
		// and add them back in
		for (int i = 0; i < sysList.size(); i++) {
			// log.debug("schedule ids: " + (String) sysList.get(i));
			scheduleManager.getScheduleById(sysList.get(i)).addPropertyChangeListener(this);
		}
		addPropertyChangeTracks();
	}

	List<String> sysList = null;

	void initTable(SchedulesTableFrame frame, JTable table) {
		// Install the button handlers
		TableColumnModel tcm = table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(EDIT_COLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDIT_COLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(DELETE_COLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(DELETE_COLUMN).setCellEditor(buttonEditor);
		table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
		table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

		setPreferredWidths(frame, table);

		// set row height
		table.setRowHeight(new JComboBox().getPreferredSize().height);
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	private void setPreferredWidths(SchedulesTableFrame frame, JTable table) {
		if (frame.loadTableDetails(table))
			return; // done
		log.debug("Setting preferred widths");
		// set column preferred widths
		table.getColumnModel().getColumn(ID_COLUMN).setPreferredWidth(40);
		table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(200);
		table.getColumnModel().getColumn(SCHEDULE_STATUS_COLUMN).setPreferredWidth(80);
		table.getColumnModel().getColumn(SPUR_NUMBER_COLUMN).setPreferredWidth(40);
		table.getColumnModel().getColumn(SPUR_COLUMN).setPreferredWidth(350);
		table.getColumnModel().getColumn(STATUS_COLUMN).setPreferredWidth(150);
		table.getColumnModel().getColumn(MODE_COLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(EDIT_COLUMN).setPreferredWidth(70);
		table.getColumnModel().getColumn(DELETE_COLUMN).setPreferredWidth(90);
	}

	public int getRowCount() {
		return sysList.size();
	}

	public int getColumnCount() {
		return HIGHEST_COLUMN;
	}

	public String getColumnName(int col) {
		switch (col) {
		case ID_COLUMN:
			return Bundle.getMessage("Id");
		case NAME_COLUMN:
			return Bundle.getMessage("Name");
		case SCHEDULE_STATUS_COLUMN:
			return Bundle.getMessage("Status");
		case SPUR_NUMBER_COLUMN:
			return Bundle.getMessage("Number");
		case SPUR_COLUMN:
			return Bundle.getMessage("Spurs");
		case STATUS_COLUMN:
			return Bundle.getMessage("StatusSpur");
		case MODE_COLUMN:
			return Bundle.getMessage("ScheduleMode");
		case EDIT_COLUMN:
			return Bundle.getMessage("Edit");
		case DELETE_COLUMN:
			return Bundle.getMessage("Delete");
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
		case SCHEDULE_STATUS_COLUMN:
			return String.class;
		case SPUR_NUMBER_COLUMN:
			return String.class;
		case SPUR_COLUMN:
			return JComboBox.class;
		case STATUS_COLUMN:
			return String.class;
		case MODE_COLUMN:
			return String.class;
		case EDIT_COLUMN:
			return JButton.class;
		case DELETE_COLUMN:
			return JButton.class;
		default:
			return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
		case EDIT_COLUMN:
		case DELETE_COLUMN:
		case SPUR_COLUMN:
			return true;
		default:
			return false;
		}
	}

	public Object getValueAt(int row, int col) {
		// Funky code to put the sef frame in focus after the edit table buttons is used.
		// The button editor for the table does a repaint of the button cells after the setValueAt code
		// is called which then returns the focus back onto the table. We need the edit frame
		// in focus.
		if (focusSef) {
			focusSef = false;
			sef.requestFocus();
		}
		if (row >= sysList.size())
			return "ERROR row " + row; // NOI18N
		String id = sysList.get(row);
		Schedule s = scheduleManager.getScheduleById(id);
		if (s == null)
			return "ERROR schedule unknown " + row; // NOI18N
		switch (col) {
		case ID_COLUMN:
			return s.getId();
		case NAME_COLUMN:
			return s.getName();
		case SCHEDULE_STATUS_COLUMN:
			return getScheduleStatus(row);
		case SPUR_NUMBER_COLUMN:
			return scheduleManager.getSpursByScheduleComboBox(s).getItemCount();
		case SPUR_COLUMN: {
			JComboBox box = scheduleManager.getSpursByScheduleComboBox(s);
			String index = comboSelect.get(sysList.get(row));
			if (index != null) {
				box.setSelectedIndex(Integer.parseInt(index));
			}
			return box;
		}
		case STATUS_COLUMN:
			return getSpurStatus(row);
		case MODE_COLUMN:
			return getSpurMode(row);
		case EDIT_COLUMN:
			return Bundle.getMessage("Edit");
		case DELETE_COLUMN:
			return Bundle.getMessage("Delete");
		default:
			return "unknown " + col; // NOI18N
		}
	}

	public void setValueAt(Object value, int row, int col) {
		switch (col) {
		case EDIT_COLUMN:
			editSchedule(row);
			break;
		case DELETE_COLUMN:
			deleteSchedule(row);
			break;
		case SPUR_COLUMN:
			selectJComboBox(value, row);
			break;
		default:
			break;
		}
	}

	boolean focusSef = false;
	ScheduleEditFrame sef = null;

	private void editSchedule(int row) {
		log.debug("Edit schedule");
		if (sef != null)
			sef.dispose();
		Schedule s = scheduleManager.getScheduleById(sysList.get(row));
		LocationTrackPair ltp = getLocationTrackPair(row);
		if (ltp == null) {
			log.debug("Need location track pair");
			JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("AssignSchedule"),
					new Object[] { s.getName() }), MessageFormat.format(Bundle.getMessage("CanNotSchedule"),
					new Object[] { Bundle.getMessage("Edit") }), JOptionPane.ERROR_MESSAGE);
			return;
		}
		sef = new ScheduleEditFrame();
		sef.setTitle(MessageFormat.format(Bundle.getMessage("TitleScheduleEdit"), new Object[] { ltp
				.getTrack().getName() }));
		sef.initComponents(s, ltp.getLocation(), ltp.getTrack());
		focusSef = true;
	}

	private void deleteSchedule(int row) {
		log.debug("Delete schedule");
		Schedule s = scheduleManager.getScheduleById(sysList.get(row));
		if (JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
				.getMessage("DoYouWantToDeleteSchedule"), new Object[] { s.getName() }), Bundle
				.getMessage("DeleteSchedule?"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
			scheduleManager.deregister(s);
			OperationsXml.save();
		}
	}

	protected Hashtable<String, String> comboSelect = new Hashtable<String, String>();

	private void selectJComboBox(Object value, int row) {
		String id = sysList.get(row);
		JComboBox box = (JComboBox) value;
		comboSelect.put(id, Integer.toString(box.getSelectedIndex()));
		fireTableRowsUpdated(row, row);
	}

	private LocationTrackPair getLocationTrackPair(int row) {
		Schedule s = scheduleManager.getScheduleById(sysList.get(row));
		JComboBox box = scheduleManager.getSpursByScheduleComboBox(s);
		String index = comboSelect.get(sysList.get(row));
		LocationTrackPair ltp;
		if (index != null) {
			ltp = (LocationTrackPair) box.getItemAt(Integer.parseInt(index));
		} else {
			ltp = (LocationTrackPair) box.getItemAt(0);
		}
		return ltp;
	}

	private String getScheduleStatus(int row) {
		Schedule sch = scheduleManager.getScheduleById(sysList.get(row));
		JComboBox box = scheduleManager.getSpursByScheduleComboBox(sch);
		for (int i = 0; i < box.getItemCount(); i++) {
			LocationTrackPair ltp = (LocationTrackPair) box.getItemAt(i);
			String status = ltp.getTrack().checkScheduleValid();
			if (!status.equals(""))
				return Bundle.getMessage("Error");
		}
		return Bundle.getMessage("Okay");
	}

	private String getSpurStatus(int row) {
		LocationTrackPair ltp = getLocationTrackPair(row);
		if (ltp == null)
			return "";
		String status = ltp.getTrack().checkScheduleValid();
		if (!status.equals(""))
			return status;
		return Bundle.getMessage("Okay");
	}

	private String getSpurMode(int row) {
		LocationTrackPair ltp = getLocationTrackPair(row);
		if (ltp == null)
			return "";
		String mode = Bundle.getMessage("Sequential");
		if (ltp.getTrack().getScheduleMode() == Track.MATCH)
			mode = Bundle.getMessage("Match");
		return mode;
	}

	private void removePropertyChangeSchedules() {
		if (sysList != null) {
			for (int i = 0; i < sysList.size(); i++) {
				// if object has been deleted, it's not here; ignore it
				Schedule sch = scheduleManager.getScheduleById(sysList.get(i));
				if (sch != null)
					sch.removePropertyChangeListener(this);
			}
		}
	}
	
	private void addPropertyChangeTracks() {
		// only spurs have schedules
		List<Track> tracks = LocationManager.instance().getTracks(Track.SPUR);
		for (int i=0; i<tracks.size(); i++) {
			Track track = tracks.get(i);
			track.addPropertyChangeListener(this);
		}
	}
	
	private void removePropertyChangeTracks() {
		List<Track> tracks = LocationManager.instance().getTracks(Track.SPUR);
		for (int i=0; i<tracks.size(); i++) {
			Track track = tracks.get(i);
			track.removePropertyChangeListener(this);
		}
	}

	public void dispose() {
		if (log.isDebugEnabled())
			log.debug("dispose");
		if (sef != null)
			sef.dispose();
		scheduleManager.removePropertyChangeListener(this);
		removePropertyChangeSchedules();
		removePropertyChangeTracks();
	}

	// check for change in number of schedules, or a change in a schedule
	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
					+ e.getNewValue());
		if (e.getPropertyName().equals(ScheduleManager.LISTLENGTH_CHANGED_PROPERTY)) {
			updateList();
			fireTableDataChanged();
		} else if (e.getSource().getClass().equals(Schedule.class)) {
			String id = ((Schedule) e.getSource()).getId();
			int row = sysList.indexOf(id);
			if (Control.showProperty && log.isDebugEnabled())
				log.debug("Update schedule table row: " + row + " id: " + id);
			if (row >= 0)
				fireTableRowsUpdated(row, row);
		}
		if (e.getPropertyName().equals(Track.TRACK_SCHEDULE_MODE_CHANGED_PROPERTY)) {
			fireTableDataChanged();
		}
	}

	static Logger log = LoggerFactory.getLogger(SchedulesTableModel.class.getName());
}
