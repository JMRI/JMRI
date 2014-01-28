// CarsTableModel.java

package jmri.jmrit.operations.rollingstock.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.awt.Frame;

import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Table Model for edit of cars used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012
 * @version $Revision$
 */
public class CarsTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

	CarManager manager = CarManager.instance(); // There is only one manager

	// Defines the columns
	private static final int SELECT_COLUMN = 0;
	private static final int NUMBER_COLUMN = 1;
	private static final int ROAD_COLUMN = 2;
	private static final int TYPE_COLUMN = 3;
	private static final int LENGTH_COLUMN = 4;
	private static final int LOAD_COLUMN = 5;
	private static final int COLOR_COLUMN = 6;
	private static final int KERNEL_COLUMN = 7;
	private static final int LOCATION_COLUMN = 8;
	private static final int DESTINATION_COLUMN = 9;
	private static final int FINAL_DESTINATION_COLUMN = 10;
	private static final int RWE_COLUMN = 11;
	private static final int TRAIN_COLUMN = 12;
	private static final int MOVES_COLUMN = 13;
	private static final int BUILT_COLUMN = 14;
	private static final int OWNER_COLUMN = 15;
	private static final int VALUE_COLUMN = 16;
	private static final int RFID_COLUMN = 17;
	private static final int WAIT_COLUMN = 18;
	private static final int LAST_COLUMN = 19;
	private static final int SET_COLUMN = 20;
	private static final int EDIT_COLUMN = 21;

	private static final int HIGHESTCOLUMN = EDIT_COLUMN + 1;

	public final int SORTBYNUMBER = 1;
	public final int SORTBYROAD = 2;
	public final int SORTBYTYPE = 3;
	public final int SORTBYLOCATION = 4;
	public final int SORTBYDESTINATION = 5;
	public final int SORTBYTRAIN = 6;
	public final int SORTBYMOVES = 7;
	public final int SORTBYKERNEL = 8;
	public final int SORTBYLOAD = 9;
	public final int SORTBYCOLOR = 10;
	public final int SORTBYBUILT = 11;
	public final int SORTBYOWNER = 12;
	public final int SORTBYRFID = 13;
	public final int SORTBYRWE = 14; // return when empty
	public final int SORTBYFINALDESTINATION = 15;
	public final int SORTBYVALUE = 16;
	public final int SORTBYWAIT = 17;
	public final int SORTBYLAST = 18;

	private int _sort = SORTBYNUMBER;

	List<RollingStock> sysList = null; // list of cars
	boolean showAllCars = true; // when true show all cars
	String locationName = null; // only show cars with this location
	String trackName = null; // only show cars with this track
	JTable _table;
	CarsTableFrame _frame;

	public CarsTableModel(boolean showAllCars, String locationName, String trackName) {
		super();
		this.showAllCars = showAllCars;
		this.locationName = locationName;
		this.trackName = trackName;
		manager.addPropertyChangeListener(this);
		updateList();
	}

	public void setSort(int sort) {
		_sort = sort;
		updateList();
		XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
		if (sort == SORTBYCOLOR || sort == SORTBYLOAD) {
			tcm.setColumnVisible(tcm.getColumnByModelIndex(LOAD_COLUMN), sort == SORTBYLOAD);
			tcm.setColumnVisible(tcm.getColumnByModelIndex(COLOR_COLUMN), sort == SORTBYCOLOR);
		} else if (sort == SORTBYDESTINATION || sort == SORTBYFINALDESTINATION || sort == SORTBYRWE) {
			tcm.setColumnVisible(tcm.getColumnByModelIndex(DESTINATION_COLUMN), sort == SORTBYDESTINATION);
			tcm.setColumnVisible(tcm.getColumnByModelIndex(FINAL_DESTINATION_COLUMN), sort == SORTBYFINALDESTINATION);
			tcm.setColumnVisible(tcm.getColumnByModelIndex(RWE_COLUMN), sort == SORTBYRWE);
		} else if (sort == SORTBYMOVES || sort == SORTBYBUILT || sort == SORTBYOWNER || sort == SORTBYVALUE
				|| sort == SORTBYRFID || sort == SORTBYWAIT || sort == SORTBYLAST) {
			tcm.setColumnVisible(tcm.getColumnByModelIndex(MOVES_COLUMN), sort == SORTBYMOVES);
			tcm.setColumnVisible(tcm.getColumnByModelIndex(BUILT_COLUMN), sort == SORTBYBUILT);
			tcm.setColumnVisible(tcm.getColumnByModelIndex(OWNER_COLUMN), sort == SORTBYOWNER);
			tcm.setColumnVisible(tcm.getColumnByModelIndex(VALUE_COLUMN), sort == SORTBYVALUE);
			tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_COLUMN), sort == SORTBYRFID);
			tcm.setColumnVisible(tcm.getColumnByModelIndex(WAIT_COLUMN), sort == SORTBYWAIT);
			tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_COLUMN), sort == SORTBYLAST);
		} else {
			fireTableDataChanged();
		}
	}

	public String getSortByName() {
		switch (_sort) {
		case SORTBYNUMBER:
			return Bundle.getMessage("Number");
		case SORTBYROAD:
			return Bundle.getMessage("Road");
		case SORTBYTYPE:
			return Bundle.getMessage("Type");
		case SORTBYCOLOR:
			return Bundle.getMessage("Color");
		case SORTBYLOAD:
			return Bundle.getMessage("Load");
		case SORTBYKERNEL:
			return Bundle.getMessage("Kernel");
		case SORTBYLOCATION:
			return Bundle.getMessage("Location");
		case SORTBYDESTINATION:
			return Bundle.getMessage("Destination");
		case SORTBYTRAIN:
			return Bundle.getMessage("Train");
		case SORTBYFINALDESTINATION:
			return Bundle.getMessage("FinalDestination");
		case SORTBYRWE:
			return Bundle.getMessage("ReturnWhenEmpty");
		case SORTBYMOVES:
			return Bundle.getMessage("Moves");
		case SORTBYBUILT:
			return Bundle.getMessage("Built");
		case SORTBYOWNER:
			return Bundle.getMessage("Owner");
		case SORTBYVALUE:
			return Setup.getValueLabel();
		case SORTBYRFID:
			return Setup.getRfidLabel();
		case SORTBYWAIT:
			return Bundle.getMessage("Wait");
		case SORTBYLAST:
			return Bundle.getMessage("Last");
		default:
			return "Error"; // NOI18N
		}
	}

	public void toggleSelectVisible() {
		XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
		boolean isVisible = tcm.isColumnVisible(tcm.getColumnByModelIndex(SELECT_COLUMN));
		tcm.setColumnVisible(tcm.getColumnByModelIndex(SELECT_COLUMN), !isVisible);
	}
	
	public void resetCheckboxes() {
		for ( RollingStock car : sysList ) {
			car.setSelected(false);		
		}
	}

	String _roadNumber = "";
	int _index = 0;

	/**
	 * Search for car by road number
	 * 
	 * @param roadNumber
	 * @return -1 if not found, table row number if found
	 */
	public int findCarByRoadNumber(String roadNumber) {
		if (sysList != null) {
			if (!roadNumber.equals(_roadNumber))
				return getIndex(0, roadNumber);
			int index = getIndex(_index, roadNumber);
			if (index > 0)
				return index;
			return getIndex(0, roadNumber);
		}
		return -1;
	}

	private int getIndex(int start, String roadNumber) {
		for (int index = start; index < sysList.size(); index++) {
			Car c = (Car) sysList.get(index);
			if (c != null) {
				String[] number = c.getNumber().split("-");
				// check for wild card '*'
				if (roadNumber.startsWith("*")) {
					String rN = roadNumber.substring(1);
					if (c.getNumber().endsWith(rN) || number[0].endsWith(rN)) {
						_roadNumber = roadNumber;
						_index = index + 1;
						return index;
					}
				} else if (roadNumber.endsWith("*")) {
					String rN = roadNumber.substring(0, roadNumber.length() - 1);
					if (c.getNumber().startsWith(rN)) {
						_roadNumber = roadNumber;
						_index = index + 1;
						return index;
					}
				} else if (c.getNumber().equals(roadNumber) || number[0].equals(roadNumber)) {
					_roadNumber = roadNumber;
					_index = index + 1;
					return index;
				}
			}
		}
		_roadNumber = "";
		return -1;
	}

	public Car getCarAtIndex(int index) {
		return (Car) sysList.get(index);
	}

	synchronized void updateList() {
		// first, remove listeners from the individual objects
		removePropertyChangeCars();
		sysList = getSelectedCarList();
		// and add listeners back in
		addPropertyChangeCars();
	}

	public List<RollingStock> getSelectedCarList() {
		List<RollingStock> list;
		if (_sort == SORTBYROAD)
			list = manager.getByRoadNameList();
		else if (_sort == SORTBYTYPE)
			list = manager.getByTypeList();
		else if (_sort == SORTBYLOCATION)
			list = manager.getByLocationList();
		else if (_sort == SORTBYDESTINATION)
			list = manager.getByDestinationList();
		else if (_sort == SORTBYTRAIN)
			list = manager.getByTrainList();
		else if (_sort == SORTBYMOVES)
			list = manager.getByMovesList();
		else if (_sort == SORTBYKERNEL)
			list = manager.getByKernelList();
		else if (_sort == SORTBYLOAD)
			list = manager.getByLoadList();
		else if (_sort == SORTBYCOLOR)
			list = manager.getByColorList();
		else if (_sort == SORTBYOWNER)
			list = manager.getByOwnerList();
		else if (_sort == SORTBYBUILT)
			list = manager.getByBuiltList();
		else if (_sort == SORTBYVALUE)
			list = manager.getByValueList();
		else if (_sort == SORTBYRFID)
			list = manager.getByRfidList();
		else if (_sort == SORTBYRWE)
			list = manager.getByRweList();
		else if (_sort == SORTBYFINALDESTINATION)
			list = manager.getByFinalDestinationList();
		else if (_sort == SORTBYWAIT)
			list = manager.getByWaitList();
		else if (_sort == SORTBYLAST)
			list = manager.getByLastDateList();
		else
			list = manager.getByNumberList();
		filterList(list);
		return list;
	}

	private void filterList(List<RollingStock> list) {
		if (showAllCars)
			return;
		for (int i = 0; i < list.size(); i++) {
			Car car = (Car) list.get(i);
			if (car.getLocationName().equals("")) {
				list.remove(i--);
				continue;
			}
			// filter out cars that don't have a location name that matches
			if (locationName != null) {
				if (car.getLocationName().equals(locationName)) {
					if (trackName != null) {
						if (car.getTrackName().equals(trackName)) {
							continue;
						} else {
							list.remove(i--);
						}
					} else {
						continue;
					}
				} else {
					list.remove(i--);
				}
			}
		}
	}

	void initTable(JTable table, CarsTableFrame frame) {
		_table = table;
		_frame = frame;
		initTable();
	}

	void initTable() {
		// Use XTableColumnModel so we can control which columns are visible
		XTableColumnModel tcm = new XTableColumnModel();
		_table.setColumnModel(tcm);
		_table.createDefaultColumnsFromModel();

		// Install the button handlers
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		tcm.getColumn(SET_COLUMN).setCellRenderer(buttonRenderer);
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(SET_COLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(EDIT_COLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDIT_COLUMN).setCellEditor(buttonEditor);

		// set column preferred widths
		if (!_frame.loadTableDetails(_table)) {
			// load defaults, xml file data not found
			// Cars frame table column widths, starts with Select column and ends with Edit
			int[] tableColumnWidths = { 60, 60, 60, 65, 35, 75, 75, 65, 190, 190, 190, 190, 65, 50, 50, 50, 50, 50, 50,
					50, 65, 70 };
			for (int i = 0; i < tcm.getColumnCount(); i++)
				tcm.getColumn(i).setPreferredWidth(tableColumnWidths[i]);
		}
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// turn off columns
		tcm.setColumnVisible(tcm.getColumnByModelIndex(SELECT_COLUMN), false);
		tcm.setColumnVisible(tcm.getColumnByModelIndex(COLOR_COLUMN), false);

		tcm.setColumnVisible(tcm.getColumnByModelIndex(FINAL_DESTINATION_COLUMN), false);
		tcm.setColumnVisible(tcm.getColumnByModelIndex(RWE_COLUMN), false);

		tcm.setColumnVisible(tcm.getColumnByModelIndex(BUILT_COLUMN), false);
		tcm.setColumnVisible(tcm.getColumnByModelIndex(OWNER_COLUMN), false);
		tcm.setColumnVisible(tcm.getColumnByModelIndex(VALUE_COLUMN), false);
		tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_COLUMN), false);
		tcm.setColumnVisible(tcm.getColumnByModelIndex(WAIT_COLUMN), false);
		tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_COLUMN), false);
	}

	public int getRowCount() {
		return sysList.size();
	}

	public int getColumnCount() {
		return HIGHESTCOLUMN;
	}

	public String getColumnName(int col) {
		switch (col) {
		case SELECT_COLUMN:
			return Bundle.getMessage("Select");
		case NUMBER_COLUMN:
			return Bundle.getMessage("Number");
		case ROAD_COLUMN:
			return Bundle.getMessage("Road");
		case LOAD_COLUMN:
			return Bundle.getMessage("Load");
		case COLOR_COLUMN:
			return Bundle.getMessage("Color");
		case TYPE_COLUMN:
			return Bundle.getMessage("Type");
		case LENGTH_COLUMN:
			return Bundle.getMessage("Len");
		case KERNEL_COLUMN:
			return Bundle.getMessage("Kernel");
		case LOCATION_COLUMN:
			return Bundle.getMessage("Location");
		case DESTINATION_COLUMN:
			return Bundle.getMessage("Destination");
		case FINAL_DESTINATION_COLUMN:
			return Bundle.getMessage("FinalDestination");
		case RWE_COLUMN:
			return Bundle.getMessage("ReturnWhenEmpty");
		case TRAIN_COLUMN:
			return Bundle.getMessage("Train");
		case MOVES_COLUMN:
			return Bundle.getMessage("Moves");
		case BUILT_COLUMN:
			return Bundle.getMessage("Built");
		case OWNER_COLUMN:
			return Bundle.getMessage("Owner");
		case VALUE_COLUMN:
			return Setup.getValueLabel();
		case RFID_COLUMN:
			return Setup.getRfidLabel();
		case WAIT_COLUMN:
			return Bundle.getMessage("Wait");
		case LAST_COLUMN:
			return Bundle.getMessage("LastMoved");
		case SET_COLUMN:
			return Bundle.getMessage("Set");
		case EDIT_COLUMN:
			return Bundle.getMessage("Edit");
		default:
			return "unknown"; // NOI18N
		}
	}

	public Class<?> getColumnClass(int col) {
		switch (col) {
		case SELECT_COLUMN:
			return Boolean.class;
		case SET_COLUMN:
		case EDIT_COLUMN:
			return JButton.class;
		default:
			return String.class;
		}
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
		case SELECT_COLUMN:
		case SET_COLUMN:
		case EDIT_COLUMN:
		case MOVES_COLUMN:
		case WAIT_COLUMN:
			return true;
		default:
			return false;
		}
	}

	public Object getValueAt(int row, int col) {
		// Funky code to put the csf and cef frames in focus after set and edit table buttons are used.
		// The button editor for the table does a repaint of the button cells after the setValueAt code
		// is called which then returns the focus back onto the table. We need the set and edit frames
		// in focus.
		if (focusCsf) {
			focusCsf = false;
			csf.requestFocus();
		}
		if (focusCef) {
			focusCef = false;
			cef.requestFocus();
		}
		if (row >= sysList.size())
			return "ERROR row " + row; // NOI18N
		Car car = (Car) sysList.get(row);
		if (car == null)
			return "ERROR car unknown " + row; // NOI18N
		switch (col) {
		case SELECT_COLUMN:
			return car.isSelected();
		case NUMBER_COLUMN:
			return car.getNumber();
		case ROAD_COLUMN:
			return car.getRoadName();
		case LOAD_COLUMN:
			if (car.getLoadPriority().equals(CarLoad.PRIORITY_HIGH))
				return car.getLoadName() + " " + Bundle.getMessage("(P)");
			else
				return car.getLoadName();
		case COLOR_COLUMN:
			return car.getColor();
		case LENGTH_COLUMN:
			return car.getLength();
		case TYPE_COLUMN: {
			StringBuffer buf = new StringBuffer(car.getTypeName());
			if (car.isCaboose())
				buf.append(" " + Bundle.getMessage("(C)"));
			if (car.hasFred())
				buf.append(" " + Bundle.getMessage("(F)"));
			if (car.isPassenger())
				buf.append(" " + Bundle.getMessage("(P)") + " " + car.getBlocking());
			if (car.isUtility())
				buf.append(" " + Bundle.getMessage("(U)"));
			if (car.isHazardous())
				buf.append(" " + Bundle.getMessage("(H)"));
			return buf.toString();
		}
		case KERNEL_COLUMN: {
			if (car.getKernel() != null && car.getKernel().isLead(car))
				return car.getKernelName() + "*";
			return car.getKernelName();
		}
		case LOCATION_COLUMN: {
			String s = car.getStatus();
			if (!car.getLocationName().equals(""))
				s = car.getStatus() + car.getLocationName() + " (" + car.getTrackName() + ")";
			return s;
		}
		case DESTINATION_COLUMN:
		case FINAL_DESTINATION_COLUMN: {
			String s = "";
			if (!car.getDestinationName().equals(""))
				s = car.getDestinationName() + " (" + car.getDestinationTrackName() + ")";
			if (!car.getFinalDestinationName().equals(""))
				s = s + "->" + car.getFinalDestinationName(); // NOI18N
			if (!car.getFinalDestinationTrackName().equals(""))
				s = s + " (" + car.getFinalDestinationTrackName() + ")";
			return s;
		}
		case RWE_COLUMN:
			return car.getReturnWhenEmptyDestName();

		case TRAIN_COLUMN: {
			// if train was manually set by user add an asterisk
			if (car.getTrain() != null && car.getRouteLocation() == null)
				return car.getTrainName() + "*";
			return car.getTrainName();
		}
		case MOVES_COLUMN:
			return car.getMoves();
		case BUILT_COLUMN:
			return car.getBuilt();
		case OWNER_COLUMN:
			return car.getOwner();
		case VALUE_COLUMN:
			return car.getValue();
		case RFID_COLUMN:
			return car.getRfid();
		case WAIT_COLUMN:
			return car.getWait();
		case LAST_COLUMN:
			return car.getLastDate();
		case SET_COLUMN:
			return Bundle.getMessage("Set");
		case EDIT_COLUMN:
			return Bundle.getMessage("Edit");
		default:
			return "unknown " + col; // NOI18N
		}
	}

	boolean focusCef = false;
	boolean focusCsf = false;
	CarEditFrame cef = null;
	CarSetFrame csf = null;

	public void setValueAt(Object value, int row, int col) {
		Car car = (Car) sysList.get(row);
		switch (col) {
		case SELECT_COLUMN:
			car.setSelected(((Boolean) value).booleanValue());
			break;
		case SET_COLUMN:
			log.debug("Set car location");
			if (csf != null)
				csf.dispose();
			csf = new CarSetFrame();
			csf.initComponents();
			csf.loadCar(car);
			csf.setVisible(true);
			csf.setExtendedState(Frame.NORMAL);
			focusCsf = true;
			break;
		case EDIT_COLUMN:
			log.debug("Edit car");
			if (cef != null)
				cef.dispose();
			cef = new CarEditFrame();
			cef.initComponents();
			cef.loadCar(car);
			cef.setTitle(Bundle.getMessage("TitleCarEdit"));
			cef.setExtendedState(Frame.NORMAL);
			focusCef = true;
			break;
		case MOVES_COLUMN:
			try {
				car.setMoves(Integer.parseInt(value.toString()));
			} catch (NumberFormatException e) {
				log.error("move count must be a number");
			}
			break;
		case BUILT_COLUMN:
			car.setBuilt(value.toString());
			break;
		case OWNER_COLUMN:
			car.setOwner(value.toString());
			break;
		case VALUE_COLUMN:
			car.setValue(value.toString());
			break;
		case RFID_COLUMN:
			car.setRfid(value.toString());
			break;
		case WAIT_COLUMN:
			try {
				car.setWait(Integer.parseInt(value.toString()));
			} catch (NumberFormatException e) {
				log.error("wait count must be a number");
			}
			break;
		case LAST_COLUMN:
			// do nothing
			break;
		default:
			break;
		}
	}

	public void dispose() {
		if (log.isDebugEnabled())
			log.debug("dispose CarTableModel");
		manager.removePropertyChangeListener(this);
		removePropertyChangeCars();
		if (csf != null)
			csf.dispose();
		if (cef != null)
			cef.dispose();
	}

	private void addPropertyChangeCars() {
		List<RollingStock> list = manager.getList();
		for (int i = 0; i < list.size(); i++) {
			// if object has been deleted, it's not here; ignore it
			RollingStock car = list.get(i);
			if (car != null)
				car.addPropertyChangeListener(this);
		}
	}

	private void removePropertyChangeCars() {
		List<RollingStock> list = manager.getList();
		for (int i = 0; i < list.size(); i++) {
			// if object has been deleted, it's not here; ignore it
			RollingStock car = list.get(i);
			if (car != null)
				car.removePropertyChangeListener(this);
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
					+ e.getNewValue()); // NOI18N
		if (e.getPropertyName().equals(CarManager.LISTLENGTH_CHANGED_PROPERTY)) {
			updateList();
			fireTableDataChanged();
		}
		// must be a car change
		else if (e.getSource().getClass().equals(Car.class)) {
			Car car = (Car) e.getSource();
			int row = sysList.indexOf(car);
			if (Control.showProperty && log.isDebugEnabled())
				log.debug("Update car table row: " + row);
			if (row >= 0)
				fireTableRowsUpdated(row, row);
		}
	}

	static Logger log = LoggerFactory.getLogger(CarsTableModel.class.getName());
}
