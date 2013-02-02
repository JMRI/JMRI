// EnginesTableModel.java

package jmri.jmrit.operations.rollingstock.engines;

import org.apache.log4j.Logger;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of engines used by operations
 * 
 * @author Daniel Boudreau Copyright (C) 2008, 2012
 * @version $Revision$
 */
public class EnginesTableModel extends javax.swing.table.AbstractTableModel implements
		PropertyChangeListener {

	EngineManager manager = EngineManager.instance(); // There is only one manager

	// Defines the columns
	private static final int NUMCOLUMN = 0;
	private static final int ROADCOLUMN = 1;
	private static final int MODELCOLUMN = 2;
	private static final int TYPECOLUMN = 3;
	private static final int LENGTHCOLUMN = 4;
	private static final int CONSISTCOLUMN = 5;
	private static final int LOCATIONCOLUMN = 6;
	private static final int DESTINATIONCOLUMN = 7;
	private static final int TRAINCOLUMN = 8;
	private static final int MOVESCOLUMN = 9;
	private static final int SETCOLUMN = 10;
	private static final int EDITCOLUMN = 11;

	private static final int HIGHESTCOLUMN = EDITCOLUMN + 1;

	private static final int SHOWMOVES = 0;
	private static final int SHOWBUILT = 1;
	private static final int SHOWOWNER = 2;
	private static final int SHOWVALUE = 3;
	private static final int SHOWRFID = 4;
	private int showMoveCol = SHOWMOVES;

	public EnginesTableModel() {
		super();
		manager.addPropertyChangeListener(this);
		updateList();
	}

	public final int SORTBYNUMBER = 1;
	public final int SORTBYROAD = 2;
	public final int SORTBYMODEL = 3;
	public final int SORTBYLOCATION = 4;
	public final int SORTBYDESTINATION = 5;
	public final int SORTBYTRAIN = 6;
	public final int SORTBYMOVES = 7;
	public final int SORTBYCONSIST = 8;
	public final int SORTBYBUILT = 9;
	public final int SORTBYOWNER = 10;
	public final int SORTBYVALUE = 11;
	public final int SORTBYRFID = 12;

	private int _sort = SORTBYNUMBER;

	public void setSort(int sort) {
		_sort = sort;
		updateList();
		if (sort == SORTBYMOVES) {
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
		} else
			fireTableDataChanged();
	}

	String _roadNumber = "";
	int _index = 0;

	/**
	 * Search for engine by road number
	 * 
	 * @param roadNumber
	 * @return -1 if not found, table row number if found
	 */
	public int findEngineByRoadNumber(String roadNumber) {
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
			Engine e = manager.getById(sysList.get(index));
			if (e != null) {
				String[] number = e.getNumber().split("-");
				// check for wild card '*'
				if (roadNumber.startsWith("*")) {
					String rN = roadNumber.substring(1);
					if (e.getNumber().endsWith(rN) || number[0].endsWith(rN)) {
						_roadNumber = roadNumber;
						_index = index + 1;
						return index;
					}
				} else if (roadNumber.endsWith("*")) {
					String rN = roadNumber.substring(0, roadNumber.length() - 1);
					if (e.getNumber().startsWith(rN)) {
						_roadNumber = roadNumber;
						_index = index + 1;
						return index;
					}
				} else if (e.getNumber().equals(roadNumber) || number[0].equals(roadNumber)) {
					_roadNumber = roadNumber;
					_index = index + 1;
					return index;
				}
			}
		}
		_roadNumber = "";
		return -1;
	}

	synchronized void updateList() {
		// first, remove listeners from the individual objects
		removePropertyChangeEngines();
		sysList = getSelectedEngineList();
		// and add listeners back in
		for (int i = 0; i < sysList.size(); i++)
			manager.getById(sysList.get(i)).addPropertyChangeListener(this);
	}

	public List<String> getSelectedEngineList() {
		List<String> list;
		if (_sort == SORTBYROAD)
			list = manager.getByRoadNameList();
		else if (_sort == SORTBYMODEL)
			list = manager.getByModelList();
		else if (_sort == SORTBYLOCATION)
			list = manager.getByLocationList();
		else if (_sort == SORTBYDESTINATION)
			list = manager.getByDestinationList();
		else if (_sort == SORTBYTRAIN)
			list = manager.getByTrainList();
		else if (_sort == SORTBYMOVES)
			list = manager.getByMovesList();
		else if (_sort == SORTBYCONSIST)
			list = manager.getByConsistList();
		else if (_sort == SORTBYOWNER)
			list = manager.getByOwnerList();
		else if (_sort == SORTBYBUILT)
			list = manager.getByBuiltList();
		else if (_sort == SORTBYVALUE)
			list = manager.getByValueList();
		else if (_sort == SORTBYRFID)
			list = manager.getByRfidList();
		else
			list = manager.getByNumberList();
		return list;
	}

	List<String> sysList = null;

	JTable _table;
	EnginesTableFrame _frame;

	void initTable(JTable table, EnginesTableFrame frame) {
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
			int[] tableColumnWidths = manager.getEnginesFrameTableColumnWidths();
			for (int i = 0; i < tcm.getColumnCount(); i++)
				tcm.getColumn(i).setPreferredWidth(tableColumnWidths[i]);
		}
		/*
		 * table.getColumnModel().getColumn(NUMCOLUMN).setPreferredWidth(60);
		 * table.getColumnModel().getColumn(ROADCOLUMN).setPreferredWidth(60);
		 * table.getColumnModel().getColumn(MODELCOLUMN).setPreferredWidth(65);
		 * table.getColumnModel().getColumn(TYPECOLUMN).setPreferredWidth(65);
		 * table.getColumnModel().getColumn(LENGTHCOLUMN).setPreferredWidth(35);
		 * table.getColumnModel().getColumn(CONSISTCOLUMN).setPreferredWidth(75);
		 * table.getColumnModel().getColumn(LOCATIONCOLUMN).setPreferredWidth(190);
		 * table.getColumnModel().getColumn(DESTINATIONCOLUMN).setPreferredWidth(190);
		 * table.getColumnModel().getColumn(TRAINCOLUMN).setPreferredWidth(65);
		 * table.getColumnModel().getColumn(MOVESCOLUMN).setPreferredWidth(50);
		 * table.getColumnModel().getColumn(SETCOLUMN).setPreferredWidth(65);
		 * table.getColumnModel().getColumn(EDITCOLUMN).setPreferredWidth(70);
		 */
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
		case MODELCOLUMN:
			return Bundle.getMessage("Model");
		case TYPECOLUMN:
			return Bundle.getMessage("Type");
		case LENGTHCOLUMN:
			return Bundle.getMessage("Len");
		case CONSISTCOLUMN:
			return Bundle.getMessage("Consist");
		case LOCATIONCOLUMN:
			return Bundle.getMessage("Location");
		case DESTINATIONCOLUMN:
			return Bundle.getMessage("Destination");
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
			else
				return Bundle.getMessage("Moves");
		}
		case SETCOLUMN:
			return Bundle.getMessage("Set");
		case EDITCOLUMN:
			return Bundle.getMessage("Edit");
		default:
			return "unknown"; // NOI18N
		}
	}

	public Class<?> getColumnClass(int col) {
		switch (col) {
		case NUMCOLUMN:
			return String.class;
		case ROADCOLUMN:
			return String.class;
		case LENGTHCOLUMN:
			return String.class;
		case MODELCOLUMN:
			return String.class;
		case TYPECOLUMN:
			return String.class;
		case CONSISTCOLUMN:
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
		// Funky code to put the esf and eef frames in focus after set and edit table buttons are used.
		// The button editor for the table does a repaint of the button cells after the setValueAt code
		// is called which then returns the focus back onto the table. We need the set and edit frames
		// in focus.
		if (focusEsf) {
			focusEsf = false;
			esf.requestFocus();
		}
		if (focusEef) {
			focusEef = false;
			eef.requestFocus();
		}
		if (row >= sysList.size())
			return "ERROR row " + row; // NOI18N
		String engineId = sysList.get(row);
		Engine eng = manager.getById(engineId);
		if (eng == null)
			return "ERROR engine unknown " + row; // NOI18N
		switch (col) {
		case NUMCOLUMN:
			return eng.getNumber();
		case ROADCOLUMN:
			return eng.getRoad();
		case LENGTHCOLUMN:
			return eng.getLength();
		case MODELCOLUMN:
			return eng.getModel();
		case TYPECOLUMN:
			return eng.getType();
		case CONSISTCOLUMN: {
			if (eng.getConsist() != null && eng.getConsist().isLead(eng))
				return eng.getConsistName() + "*";
			return eng.getConsistName();
		}
		case LOCATIONCOLUMN: {
			String s = eng.getStatus();
			if (!eng.getLocationName().equals(""))
				s = eng.getStatus() + eng.getLocationName() + " (" + eng.getTrackName() + ")";
			return s;
		}
		case DESTINATIONCOLUMN: {
			String s = "";
			if (!eng.getDestinationName().equals(""))
				s = eng.getDestinationName() + " (" + eng.getDestinationTrackName() + ")";
			return s;
		}
		case TRAINCOLUMN: {
			// if train was manually set by user add an asterisk
			if (eng.getTrain() != null && eng.getRouteLocation() == null)
				return eng.getTrainName() + "*";
			return eng.getTrainName();
		}
		case MOVESCOLUMN: {
			if (showMoveCol == SHOWBUILT)
				return eng.getBuilt();
			else if (showMoveCol == SHOWOWNER)
				return eng.getOwner();
			else if (showMoveCol == SHOWVALUE)
				return eng.getValue();
			else if (showMoveCol == SHOWRFID)
				return eng.getRfid();
			else
				return eng.getMoves();
		}
		case SETCOLUMN:
			return Bundle.getMessage("Set");
		case EDITCOLUMN:
			return Bundle.getMessage("Edit");
		default:
			return "unknown " + col; // NOI18N
		}
	}

	boolean focusEef = false;
	boolean focusEsf = false;
	EngineEditFrame eef = null;
	EngineSetFrame esf = null;

	public void setValueAt(Object value, int row, int col) {
		String engineId = sysList.get(row);
		Engine engine = manager.getById(engineId);
		switch (col) {
		case SETCOLUMN:
			log.debug("Set engine location");
			if (esf != null)
				esf.dispose();
			esf = new EngineSetFrame();
//			esf.setTitle(Bundle.getMessage("TitleEngineSet"));
			esf.initComponents();
			esf.loadEngine(engine);
			esf.setVisible(true);
			esf.setExtendedState(java.awt.Frame.NORMAL);
			focusEsf = true;
			break;
		case EDITCOLUMN:
			log.debug("Edit engine");
			if (eef != null)
				eef.dispose();
			eef = new EngineEditFrame();
			eef.initComponents();
			eef.loadEngine(engine);
			eef.setTitle(Bundle.getMessage("TitleEngineEdit"));
			eef.setVisible(true);
			eef.setExtendedState(java.awt.Frame.NORMAL);
			focusEef = true;
			break;
		case MOVESCOLUMN:
			if (showMoveCol == SHOWBUILT)
				engine.setBuilt(value.toString());
			else if (showMoveCol == SHOWOWNER)
				engine.setOwner(value.toString());
			else if (showMoveCol == SHOWVALUE)
				engine.setValue(value.toString());
			else if (showMoveCol == SHOWRFID)
				engine.setRfid(value.toString());
			else {
				try {
					engine.setMoves(Integer.parseInt(value.toString()));
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
			log.debug("dispose EngineTableModel");
		manager.removePropertyChangeListener(this);
		removePropertyChangeEngines();
		if (esf != null)
			esf.dispose();
		if (eef != null)
			eef.dispose();
	}

	private void removePropertyChangeEngines() {
		if (sysList != null) {
			for (int i = 0; i < sysList.size(); i++) {
				// if object has been deleted, it's not here; ignore it
				Engine engine = manager.getById(sysList.get(i));
				if (engine != null)
					engine.removePropertyChangeListener(this);
			}
		}
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue()
					+ " new: " + e.getNewValue()); // NOI18N
		if (e.getPropertyName().equals(EngineManager.LISTLENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH_CHANGED_PROPERTY)) {
			updateList();
			fireTableDataChanged();
		}
		// Engine lengths are based on model, so multiple changes
		else if (e.getPropertyName().equals(Engine.LENGTH_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Engine.TYPE_CHANGED_PROPERTY)) {
			fireTableDataChanged();
		}
		// must be a engine change
		else if (e.getSource().getClass().equals(Engine.class)) {
			String engineId = ((Engine) e.getSource()).getId();
			int row = sysList.indexOf(engineId);
			if (Control.showProperty && log.isDebugEnabled())
				log.debug("Update engine table row: " + row);
			if (row >= 0)
				fireTableRowsUpdated(row, row);
		}
	}

	static Logger log = Logger.getLogger(EnginesTableModel.class
			.getName());
}
