// TrainsTableModel.java

package jmri.jmrit.operations.trains;

import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import jmri.jmrit.beantable.EnablingCheckboxRenderer;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of trains used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2012
 * @version $Revision$
 */
public class TrainsTableModel extends javax.swing.table.AbstractTableModel implements
		PropertyChangeListener {

	TrainManager manager = TrainManager.instance(); // There is only one manager

	// Defines the columns
	private static final int IDCOLUMN = 0;
	private static final int BUILDBOXCOLUMN = IDCOLUMN + 1;
	private static final int BUILDCOLUMN = BUILDBOXCOLUMN + 1;
	private static final int NAMECOLUMN = BUILDCOLUMN + 1;
	private static final int DESCRIPTIONCOLUMN = NAMECOLUMN + 1;
	private static final int ROUTECOLUMN = DESCRIPTIONCOLUMN + 1;
	private static final int DEPARTSCOLUMN = ROUTECOLUMN + 1;
	private static final int TERMINATESCOLUMN = DEPARTSCOLUMN + 1;
	private static final int CURRENTCOLUMN = TERMINATESCOLUMN + 1;
	private static final int STATUSCOLUMN = CURRENTCOLUMN + 1;
	private static final int ACTIONCOLUMN = STATUSCOLUMN + 1;
	private static final int EDITCOLUMN = ACTIONCOLUMN + 1;

	private static final int HIGHESTCOLUMN = EDITCOLUMN + 1;

	public TrainsTableModel() {
		super();
		manager.addPropertyChangeListener(this);
		updateList();
	}

	public final int SORTBYNAME = 1;
	public final int SORTBYTIME = 2;
	public final int SORTBYDEPARTS = 3;
	public final int SORTBYTERMINATES = 4;
	public final int SORTBYROUTE = 5;
	public final int SORTBYSTATUS = 6;
	public final int SORTBYID = 7;

	private int _sort = SORTBYTIME;

	public void setSort(int sort) {
		synchronized (this) {
			_sort = sort;
		}
		updateList();
		fireTableStructureChanged();
		initTable();
	}

	private boolean _showAll = true;

	public void setShowAll(boolean showAll) {
		_showAll = showAll;
		updateList();
		fireTableStructureChanged();
		initTable();
	}

	public boolean isShowAll() {
		return _showAll;
	}

	private synchronized void updateList() {
		// first, remove listeners from the individual objects
		removePropertyChangeTrains();

		if (_sort == SORTBYID)
			sysList = manager.getTrainsByIdList();
		else
			sysList = manager.getTrainsByTimeList();
		/*
		 * else if (_sort == SORTBYNAME) sysList = manager.getTrainsByNameList(); else if (_sort == SORTBYTIME) sysList
		 * = manager.getTrainsByTimeList(); else if (_sort == SORTBYDEPARTS) sysList =
		 * manager.getTrainsByDepartureList(); else if (_sort == SORTBYTERMINATES) sysList =
		 * manager.getTrainsByTerminatesList(); else if (_sort == SORTBYROUTE) sysList = manager.getTrainsByRouteList();
		 * else if (_sort == SORTBYSTATUS) sysList = manager.getTrainsByStatusList();
		 */

		if (!_showAll) {
			// filter out trains not checked
			for (int i = sysList.size() - 1; i >= 0; i--) {
				Train train = manager.getTrainById(sysList.get(i));
				if (!train.isBuildEnabled()) {
					sysList.remove(i);
				}
			}
		}

		// and add listeners back in
		addPropertyChangeTrains();
	}

	List<String> sysList = null;
	JTable _table = null;
	TrainsTableFrame _frame = null;

	void initTable(JTable table, TrainsTableFrame frame) {
		_table = table;
		_frame = frame;
		initTable();
	}

	void initTable() {
		// Install the button handlers
		TableColumnModel tcm = _table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(ACTIONCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(ACTIONCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(BUILDCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(BUILDCOLUMN).setCellEditor(buttonEditor);
		_table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());

		// set column preferred widths
		if (!_frame.loadTableDetails(_table)) {
			// load defaults, xml file data not found
			int[] tableColumnWidths = manager.getTrainsFrameTableColumnWidths();
			for (int i = 0; i < tcm.getColumnCount(); i++)
				tcm.getColumn(i).setPreferredWidth(tableColumnWidths[i]);
		}
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	public synchronized int getRowCount() {
		return sysList.size();
	}

	public int getColumnCount() {
		return HIGHESTCOLUMN;
	}

	public static final String IDCOLUMNNAME = Bundle.getMessage("Id");
	public static final String TIMECOLUMNNAME = Bundle.getMessage("Time");
	public static final String BUILDBOXCOLUMNNAME = Bundle.getMessage("Build");
	public static final String BUILDCOLUMNNAME = Bundle.getMessage("Function");
	public static final String NAMECOLUMNNAME = Bundle.getMessage("Name");
	public static final String DESCRIPTIONCOLUMNNAME = Bundle.getMessage("Description");
	public static final String ROUTECOLUMNNAME = Bundle.getMessage("Route");
	public static final String DEPARTSCOLUMNNAME = Bundle.getMessage("Departs");
	public static final String CURRENTCOLUMNNAME = Bundle.getMessage("Current");
	public static final String TERMINATESCOLUMNNAME = Bundle.getMessage("Terminates");
	public static final String STATUSCOLUMNNAME = Bundle.getMessage("Status");
	public static final String ACTIONCOLUMNNAME = Bundle.getMessage("Action");
	public static final String EDITCOLUMNNAME = Bundle.getMessage("Edit");

	public String getColumnName(int col) {
		switch (col) {
		case IDCOLUMN:
			synchronized (this) {
				if (_sort == SORTBYID)
					return IDCOLUMNNAME;
				return TIMECOLUMNNAME;
			}
		case BUILDBOXCOLUMN:
			return BUILDBOXCOLUMNNAME;
		case BUILDCOLUMN:
			return BUILDCOLUMNNAME;
		case NAMECOLUMN:
			return NAMECOLUMNNAME;
		case DESCRIPTIONCOLUMN:
			return DESCRIPTIONCOLUMNNAME;
		case ROUTECOLUMN:
			return ROUTECOLUMNNAME;
		case DEPARTSCOLUMN:
			return DEPARTSCOLUMNNAME;
		case CURRENTCOLUMN:
			return CURRENTCOLUMNNAME;
		case TERMINATESCOLUMN:
			return TERMINATESCOLUMNNAME;
		case STATUSCOLUMN:
			return STATUSCOLUMNNAME;
		case ACTIONCOLUMN:
			return ACTIONCOLUMNNAME;
		case EDITCOLUMN:
			return EDITCOLUMNNAME;
		default:
			return "unknown";
		}
	}

	public Class<?> getColumnClass(int col) {
		switch (col) {
		case IDCOLUMN:
			return String.class;
		case BUILDBOXCOLUMN:
			return Boolean.class;
		case BUILDCOLUMN:
			return JButton.class;
		case NAMECOLUMN:
			return String.class;
		case DESCRIPTIONCOLUMN:
			return String.class;
		case ROUTECOLUMN:
			return String.class;
		case DEPARTSCOLUMN:
			return String.class;
		case CURRENTCOLUMN:
			return String.class;
		case TERMINATESCOLUMN:
			return String.class;
		case STATUSCOLUMN:
			return String.class;
		case ACTIONCOLUMN:
			return JButton.class;
		case EDITCOLUMN:
			return JButton.class;
		default:
			return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
		case BUILDCOLUMN:
		case BUILDBOXCOLUMN:
		case ACTIONCOLUMN:
		case EDITCOLUMN:
			return true;
		default:
			return false;
		}
	}

	public synchronized Object getValueAt(int row, int col) {
		// Funky code to put the tef frame in focus after the edit table buttons is used.
		// The button editor for the table does a repaint of the button cells after the setValueAt code
		// is called which then returns the focus back onto the table. We need the edit frame
		// in focus.
		if (focusTef) {
			focusTef = false;
			tef.requestFocus();
		}
		// more funkyness for the conductor window.
		if (tcf != null) {
			tcf.requestFocus();
			tcf = null;
		}
		if (row >= sysList.size())
			return "ERROR row " + row;
		Train train = manager.getTrainById(sysList.get(row));
		if (train == null)
			return "ERROR train unknown " + row;
		switch (col) {
		case IDCOLUMN: {
			if (_sort == SORTBYID) {
				return train.getId();
			}
			return train.getDepartureTime();
		}
		case NAMECOLUMN:
			return train.getIconName();
		case DESCRIPTIONCOLUMN:
			return train.getDescription();
		case BUILDBOXCOLUMN: {
			return Boolean.valueOf(train.isBuildEnabled());
		}
		case ROUTECOLUMN:
			return train.getTrainRouteName();
		case DEPARTSCOLUMN: {
			if (train.getDepartureTrack() == null)
				return train.getTrainDepartsName();
			else
				return train.getTrainDepartsName() + " (" + train.getDepartureTrack().getName()
						+ ")";
		}
		case CURRENTCOLUMN:
			return train.getCurrentLocationName();
		case TERMINATESCOLUMN: {
			if (train.getTerminationTrack() == null)
				return train.getTrainTerminatesName();
			else
				return train.getTrainTerminatesName() + " ("
						+ train.getTerminationTrack().getName() + ")";
		}
		case STATUSCOLUMN:
			return train.getStatus();
		case BUILDCOLUMN: {
			if (train.isBuilt())
				if (manager.isOpenFileEnabled())
					return Bundle.getMessage("OpenFile");
				else if (manager.isPrintPreviewEnabled())
					return Bundle.getMessage("Preview");
				else if (train.isPrinted())
					return Bundle.getMessage("Printed");
				else
					return Bundle.getMessage("Print");
			return Bundle.getMessage("Build");
		}
		case ACTIONCOLUMN: {
			if (train.getBuildFailed())
				return Bundle.getMessage("Report");
			return manager.getTrainsFrameTrainAction();
		}
		case EDITCOLUMN:
			return Bundle.getMessage("Edit");
		default:
			return "unknown " + col;
		}
	}

	public synchronized void setValueAt(Object value, int row, int col) {
		switch (col) {
		case EDITCOLUMN:
			editTrain(row);
			break;
		case BUILDCOLUMN:
			buildTrain(row);
			break;
		case ACTIONCOLUMN:
			actionTrain(row);
			break;
		case BUILDBOXCOLUMN: {
			Train train = manager.getTrainById(sysList.get(row));
			train.setBuildEnabled(((Boolean) value).booleanValue());
			break;
		}
		default:
			break;
		}
	}

	boolean focusTef = false;
	TrainEditFrame tef = null;

	private synchronized void editTrain(int row) {
		if (tef != null)
			tef.dispose();
		tef = new TrainEditFrame();
		Train train = manager.getTrainById(sysList.get(row));
		log.debug("Edit train (" + train.getName() + ")");
		tef.setTitle(Bundle.getMessage("TitleTrainEdit"));
		tef.initComponents(train);
		focusTef = true;
	}

	private synchronized void buildTrain(int row) {
		Train train = manager.getTrainById(sysList.get(row));
		if (!train.isBuilt()) {
			train.build();
			// print or open file
		} else {
			if (manager.isBuildReportEnabled())
				train.printBuildReport();
			if (manager.isOpenFileEnabled())
				train.openFile();
			else
				train.printManifestIfBuilt();
		}
	}

	// one of four buttons: Report, Move, Conductor or Terminate
	private synchronized void actionTrain(int row) {
		Train train = manager.getTrainById(sysList.get(row));
		// move button becomes report if failure
		if (train.getBuildFailed()) {
			train.printBuildReport();
		} else if (manager.getTrainsFrameTrainAction().equals(TrainsTableFrame.RESET)) {
			if (log.isDebugEnabled())
				log.debug("Reset train (" + train.getName() + ")");
			// check to see if departure track was reused
			if (Setup.isStagingTrackImmediatelyAvail()
					&& !train.isTrainInRoute()
					&& train.getDepartureTrack() != null
					&& train.getDepartureTrack().getLocType().equals(Track.STAGING)
					&& (train.getDepartureTrack().getNumberRS() != train.getDepartureTrack()
							.getPickupRS()
							|| (train.getDepartureTrack().getDropRS() > 0 && train
									.getTerminationTrack() == null) || (train.getDepartureTrack()
							.getDropRS() > 0 && train.getTerminationTrack() != null && train
							.getDepartureTrack() != train.getTerminationTrack()))) {
				log.debug("Train is departing staging that already has inbound cars");
				JOptionPane.showMessageDialog(null, MessageFormat.format(
						Bundle.getMessage("StagingTrackUsed"), new Object[] { train.getDepartureTrack()
								.getName() }), Bundle.getMessage("CanNotResetTrain"),
						JOptionPane.INFORMATION_MESSAGE);
			} else if (!train.reset())
				JOptionPane.showMessageDialog(
						null,
						MessageFormat.format(Bundle.getMessage("TrainIsInRoute"),
								new Object[] { train.getTrainTerminatesName() }),
						Bundle.getMessage("CanNotResetTrain"), JOptionPane.ERROR_MESSAGE);
		} else if (!train.isBuilt()) {
			JOptionPane.showMessageDialog(
					null,
					MessageFormat.format(Bundle.getMessage("TrainNeedsBuild"),
							new Object[] { train.getName() }), Bundle.getMessage("CanNotPerformAction"),
					JOptionPane.INFORMATION_MESSAGE);
		} else if (train.isBuilt()
				&& manager.getTrainsFrameTrainAction().equals(TrainsTableFrame.MOVE)) {
			if (log.isDebugEnabled())
				log.debug("Move train (" + train.getName() + ")");
			train.move();
		} else if (train.isBuilt()
				&& manager.getTrainsFrameTrainAction().equals(TrainsTableFrame.TERMINATE)) {
			if (log.isDebugEnabled())
				log.debug("Terminate train (" + train.getName() + ")");
			int status = JOptionPane.showConfirmDialog(
					null,
					MessageFormat.format(Bundle.getMessage("TerminateTrain"),
							new Object[] { train.getName(), train.getDescription() }),
					MessageFormat.format(Bundle.getMessage("DoYouWantToTermiate"),
							new Object[] { train.getName() }), JOptionPane.YES_NO_OPTION);
			if (status == JOptionPane.YES_OPTION)
				train.terminate();
		} else if (train.isBuilt()
				&& manager.getTrainsFrameTrainAction().equals(TrainsTableFrame.CONDUCTOR)) {
			if (log.isDebugEnabled())
				log.debug("Enable conductor for train (" + train.getName() + ")");
			lauchConductor(train);
		}
	}

	TrainConductorFrame tcf = null;
	private static Hashtable<String, TrainConductorFrame> _trainConductorHashTable = new Hashtable<String, TrainConductorFrame>();

	private void lauchConductor(Train train) {
		TrainConductorFrame f = _trainConductorHashTable.get(train.getId());
		// create a copy train frame
		if (f == null || !f.isVisible()) {
			f = new TrainConductorFrame();
			f.initComponents(train);
			_trainConductorHashTable.put(train.getId(), f);
		} else {
			f.setExtendedState(Frame.NORMAL);
		}
		tcf = f;
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue());
		if (e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY)) {
			_frame.setModifiedFlag(true);
		}
		if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(TrainManager.PRINTPREVIEW_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(TrainManager.OPEN_FILE_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(TrainManager.TRAIN_ACTION_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Train.DEPARTURETIME_CHANGED_PROPERTY)
				|| (e.getPropertyName().equals(Train.BUILD_CHANGED_PROPERTY) && !isShowAll())) {
			updateList();
			fireTableDataChanged();
		} else if (e.getSource().getClass().equals(Train.class)) {
			synchronized (this) {
				String trainId = ((Train) e.getSource()).getId();
				int row = sysList.indexOf(trainId);
				if (Control.showProperty && log.isDebugEnabled())
					log.debug("Update train table row: " + row + " id: " + trainId);
				if (row >= 0)
					fireTableRowsUpdated(row, row);
			}
		}
	}

	private synchronized void removePropertyChangeTrains() {
		List<String> trains = manager.getTrainsByIdList();
		for (int i = 0; i < trains.size(); i++) {
			// if object has been deleted, it's not here; ignore it
			Train t = manager.getTrainById(trains.get(i));
			if (t != null)
				t.removePropertyChangeListener(this);
		}
	}

	private synchronized void addPropertyChangeTrains() {
		List<String> trains = manager.getTrainsByIdList();
		for (int i = 0; i < trains.size(); i++) {
			// if object has been deleted, it's not here; ignore it
			Train t = manager.getTrainById(trains.get(i));
			if (t != null)
				t.addPropertyChangeListener(this);
		}
	}

	public void dispose() {
		if (log.isDebugEnabled())
			log.debug("dispose");
		if (tef != null)
			tef.dispose();
		manager.removePropertyChangeListener(this);
		removePropertyChangeTrains();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TrainsTableModel.class
			.getName());
}
