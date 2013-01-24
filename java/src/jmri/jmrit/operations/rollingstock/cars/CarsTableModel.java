// CarsTableModel.java

package jmri.jmrit.operations.rollingstock.cars;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.awt.Frame;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

/**
 * Table Model for edit of cars used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012
 * @version $Revision$
 */
public class CarsTableModel extends javax.swing.table.AbstractTableModel implements
		PropertyChangeListener {

	CarManager manager = CarManager.instance(); // There is only one manager

	// Defines the columns
	private static final int NUMCOLUMN = 0;
	private static final int ROADCOLUMN = 1;
	private static final int TYPECOLUMN = 2;
	private static final int LENGTHCOLUMN = 3;
	private static final int COLORCOLUMN = 4; // also the Load column
	private static final int KERNELCOLUMN = 5;
	private static final int LOCATIONCOLUMN = 6;
	private static final int DESTINATIONCOLUMN = 7; // also the return when empty column
	private static final int TRAINCOLUMN = 8;
	private static final int MOVESCOLUMN = 9; // also the Owner and RFID column
	private static final int SETCOLUMN = 10;
	private static final int EDITCOLUMN = 11;

	private static final int HIGHESTCOLUMN = EDITCOLUMN + 1;

	private boolean showColor = false; // show color if true, show load if false

	private static final int SHOWDEST = 0;
	private static final int SHOWFD = 1;
	private static final int SHOWRWE = 2;
	private int showDest = SHOWDEST; // one of three possible columns to show

	private static final int SHOWMOVES = 0;
	private static final int SHOWBUILT = 1;
	private static final int SHOWOWNER = 2;
	private static final int SHOWVALUE = 3;
	private static final int SHOWRFID = 4;
	private static final int SHOWWAIT = 5;
	private static final int SHOWLAST = 6;
	private int showMoveCol = SHOWMOVES; // one of six possible columns to show

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

	List<String> sysList = null; // list of car ids
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
		if (sort == SORTBYCOLOR) {
			showColor = true;
			fireTableStructureChanged();
			initTable();
		} else if (sort == SORTBYLOAD) {
			showColor = false;
			fireTableStructureChanged();
			initTable();
		} else if (sort == SORTBYDESTINATION) {
			showDest = SHOWDEST;
			fireTableStructureChanged();
			initTable();
		} else if (sort == SORTBYFINALDESTINATION) {
			showDest = SHOWFD;
			fireTableStructureChanged();
			initTable();
		} else if (sort == SORTBYRWE) {
			showDest = SHOWRWE;
			fireTableStructureChanged();
			initTable();
		} else if (sort == SORTBYMOVES) {
			showMoveCol = SHOWMOVES;
			fireTableStructureChanged();
			initTable();
		} else if (sort == SORTBYBUILT) {
			showMoveCol = SHOWBUILT;
			fireTableStructureChanged();
			initTable();
		} else if (sort == SORTBYOWNER) {
			showMoveCol = SHOWOWNER;
			fireTableStructureChanged();
			initTable();
		} else if (sort == SORTBYVALUE) {
			showMoveCol = SHOWVALUE;
			fireTableStructureChanged();
			initTable();
		} else if (sort == SORTBYRFID) {
			showMoveCol = SHOWRFID;
			fireTableStructureChanged();
			initTable();
		} else if (sort == SORTBYWAIT) {
			showMoveCol = SHOWWAIT;
			fireTableStructureChanged();
			initTable();
		} else if (sort == SORTBYLAST) {
			showMoveCol = SHOWLAST;
			fireTableStructureChanged();
			initTable();
		} else
			fireTableDataChanged();
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
			return "Error";	// NOI18N
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
			Car c = manager.getById(sysList.get(index));
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
		return manager.getById(sysList.get(index));
	}

	synchronized void updateList() {
		// first, remove listeners from the individual objects
		removePropertyChangeCars();
		sysList = getSelectedCarList();
		// and add listeners back in
		addPropertyChangeCars();
	}

	public List<String> getSelectedCarList() {
		List<String> list;
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

	private void filterList(List<String> list) {
		if (showAllCars)
			return;
		for (int i = 0; i < list.size(); i++) {
			Car car = manager.getById(list.get(i));
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
		// Install the button handlers
		TableColumnModel tcm = _table.getColumnModel();
		ButtonRenderer buttonRenderer = new ButtonRenderer();
		tcm.getColumn(SETCOLUMN).setCellRenderer(buttonRenderer);
		TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
		tcm.getColumn(SETCOLUMN).setCellEditor(buttonEditor);
		tcm.getColumn(EDITCOLUMN).setCellRenderer(buttonRenderer);
		tcm.getColumn(EDITCOLUMN).setCellEditor(buttonEditor);

		// set column preferred widths
		if (!_frame.loadTableDetails(_table)) {
			// load defaults, xml file data not found
			int[] tableColumnWidths = manager.getCarsFrameTableColumnWidths();
			for (int i = 0; i < tcm.getColumnCount(); i++)
				tcm.getColumn(i).setPreferredWidth(tableColumnWidths[i]);
		}
		// have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
		_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	}

	public int getRowCount() {
		return sysList.size();
	}

	public int getColumnCount() {
		return HIGHESTCOLUMN;
	}

	public String getColumnName(int col) {
		switch (col) {
		case NUMCOLUMN:
			return Bundle.getMessage("Number");
		case ROADCOLUMN:
			return Bundle.getMessage("Road");
		case COLORCOLUMN: {
			if (showColor)
				return Bundle.getMessage("Color");
			else
				return Bundle.getMessage("Load");
		}
		case TYPECOLUMN:
			return Bundle.getMessage("Type");
		case LENGTHCOLUMN:
			return Bundle.getMessage("Len");
		case KERNELCOLUMN:
			return Bundle.getMessage("Kernel");
		case LOCATIONCOLUMN:
			return Bundle.getMessage("Location");
		case DESTINATIONCOLUMN: {
			if (showDest == SHOWFD)
				return Bundle.getMessage("FinalDestination");
			else if (showDest == SHOWDEST)
				return Bundle.getMessage("Destination");
			else
				return Bundle.getMessage("ReturnWhenEmpty");
		}
		case TRAINCOLUMN:
			return Bundle.getMessage("Train");
		case MOVESCOLUMN: {
			if (showMoveCol == SHOWBUILT)
				return Bundle.getMessage("Built");
			else if (showMoveCol == SHOWOWNER)
				return Bundle.getMessage("Owner");
			else if (showMoveCol == SHOWVALUE)
				return Setup.getValueLabel();
			else if (showMoveCol == SHOWRFID)
				return Setup.getRfidLabel();
			else if (showMoveCol == SHOWWAIT)
				return Bundle.getMessage("Wait");
			else if (showMoveCol == SHOWLAST)
				return Bundle.getMessage("LastMove");
			else
				return Bundle.getMessage("Moves");
		}
		case SETCOLUMN:
			return Bundle.getMessage("Set");
		case EDITCOLUMN:
			return Bundle.getMessage("Edit");
		default:
			return "unknown";	// NOI18N
		}
	}

	public Class<?> getColumnClass(int col) {
		switch (col) {
		case NUMCOLUMN:
			return String.class;
		case ROADCOLUMN:
			return String.class;
		case COLORCOLUMN:
			return String.class;
		case LENGTHCOLUMN:
			return String.class;
		case TYPECOLUMN:
			return String.class;
		case KERNELCOLUMN:
			return String.class;
		case LOCATIONCOLUMN:
			return String.class;
		case DESTINATIONCOLUMN:
			return String.class;
		case TRAINCOLUMN:
			return String.class;
		case MOVESCOLUMN:
			return String.class;
		case SETCOLUMN:
			return JButton.class;
		case EDITCOLUMN:
			return JButton.class;
		default:
			return null;
		}
	}

	public boolean isCellEditable(int row, int col) {
		switch (col) {
		case SETCOLUMN:
		case EDITCOLUMN:
		case MOVESCOLUMN:
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
			return "ERROR row " + row;	// NOI18N
		Car c = manager.getById(sysList.get(row));
		if (c == null)
			return "ERROR car unknown " + row;	// NOI18N
		switch (col) {
		case NUMCOLUMN:
			return c.getNumber();
		case ROADCOLUMN:
			return c.getRoad();
		case COLORCOLUMN: {
			if (showColor)
				return c.getColor();
			else if (c.getPriority().equals(CarLoad.PRIORITY_HIGH))
				return c.getLoad() + " " + Bundle.getMessage("(P)");
			else
				return c.getLoad();
		}
		case LENGTHCOLUMN:
			return c.getLength();
		case TYPECOLUMN: {
			if (c.isCaboose())
				return c.getType() + " " + Bundle.getMessage("(C)");
			else if (c.hasFred())
				return c.getType() + " " + Bundle.getMessage("(F)");
			else if (c.isPassenger())
				return c.getType() + " " + Bundle.getMessage("(P)");
			else if (c.isUtility())
				return c.getType() + " " + Bundle.getMessage("(U)");
			else if (c.isHazardous())
				return c.getType() + " " + Bundle.getMessage("(H)");
			else
				return c.getType();
		}
		case KERNELCOLUMN: {
			if (c.getKernel() != null && c.getKernel().isLead(c))
				return c.getKernelName() + "*";
			return c.getKernelName();
		}
		case LOCATIONCOLUMN: {
			String s = c.getStatus();
			if (!c.getLocationName().equals(""))
				s = c.getStatus() + c.getLocationName() + " (" + c.getTrackName() + ")";
			return s;
		}
		case DESTINATIONCOLUMN: {
			String s = "";
			if (showDest == SHOWDEST || showDest == SHOWFD) {
				if (!c.getDestinationName().equals(""))
					s = c.getDestinationName() + " (" + c.getDestinationTrackName() + ")";
				if (!c.getFinalDestinationName().equals(""))
					s = s + "->" + c.getFinalDestinationName();	// NOI18N
				if (!c.getFinalDestinationTrackName().equals(""))
					s = s + " (" + c.getFinalDestinationTrackName() + ")";
			} else {
				s = c.getReturnWhenEmptyDestName();
			}
			return s;
		}
		case TRAINCOLUMN: {
			// if train was manually set by user add an asterisk
			if (c.getTrain() != null && c.getRouteLocation() == null)
				return c.getTrainName() + "*";
			return c.getTrainName();
		}
		case MOVESCOLUMN: {
			if (showMoveCol == SHOWBUILT)
				return c.getBuilt();
			else if (showMoveCol == SHOWOWNER)
				return c.getOwner();
			else if (showMoveCol == SHOWVALUE)
				return c.getValue();
			else if (showMoveCol == SHOWRFID)
				return c.getRfid();
			else if (showMoveCol == SHOWWAIT)
				return c.getWait();
			else if (showMoveCol == SHOWLAST)
				return c.getLastDate();
			else
				return c.getMoves();
		}
		case SETCOLUMN:
			return Bundle.getMessage("Set");
		case EDITCOLUMN:
			return Bundle.getMessage("Edit");

		default:
			return "unknown " + col;	// NOI18N
		}
	}

	boolean focusCef = false;
	boolean focusCsf = false;
	CarEditFrame cef = null;
	CarSetFrame csf = null;

	public void setValueAt(Object value, int row, int col) {
		Car car = manager.getById(sysList.get(row));
		switch (col) {
		case SETCOLUMN:
			log.debug("Set car location");
			if (csf != null)
				csf.dispose();
			csf = new CarSetFrame();
//			csf.setTitle(Bundle.getMessage("TitleCarSet"));
			csf.initComponents();
			csf.loadCar(car);
			csf.setVisible(true);
			csf.setExtendedState(Frame.NORMAL);
			focusCsf = true;
			break;
		case EDITCOLUMN:
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
		case MOVESCOLUMN:
			if (showMoveCol == SHOWBUILT)
				car.setBuilt(value.toString());
			else if (showMoveCol == SHOWOWNER)
				car.setOwner(value.toString());
			else if (showMoveCol == SHOWVALUE)
				car.setValue(value.toString());
			else if (showMoveCol == SHOWRFID)
				car.setRfid(value.toString());
			else if (showMoveCol == SHOWWAIT) {
				try {
					car.setWait(Integer.parseInt(value.toString()));
				} catch (NumberFormatException e) {
					log.error("wait count must be a number");
				}
			} else {
				try {
					car.setMoves(Integer.parseInt(value.toString()));
				} catch (NumberFormatException e) {
					log.error("move count must be a number");
				}
			}
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
		List<String> list = manager.getList();
		for (int i = 0; i < list.size(); i++) {
			// if object has been deleted, it's not here; ignore it
			Car car = manager.getById(list.get(i));
			if (car != null)
				car.addPropertyChangeListener(this);
		}
	}

	private void removePropertyChangeCars() {
		List<String> list = manager.getList();
		for (int i = 0; i < list.size(); i++) {
			// if object has been deleted, it's not here; ignore it
			Car car = manager.getById(list.get(i));
			if (car != null)
				car.removePropertyChangeListener(this);
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue());	// NOI18N
		if (e.getPropertyName().equals(CarManager.LISTLENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(CarManager.KERNELLISTLENGTH_CHANGED_PROPERTY)) {
			updateList();
			fireTableDataChanged();
		}
		// must be a car change
		else if (e.getSource().getClass().equals(Car.class)) {
			String carId = ((Car) e.getSource()).getId();
			int row = sysList.indexOf(carId);
			if (Control.showProperty && log.isDebugEnabled())
				log.debug("Update car table row: " + row);
			if (row >= 0)
				fireTableRowsUpdated(row, row);
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CarsTableModel.class
			.getName());
}
