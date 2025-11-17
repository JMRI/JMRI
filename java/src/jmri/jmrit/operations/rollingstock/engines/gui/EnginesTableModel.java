package jmri.jmrit.operations.rollingstock.engines.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;
import javax.swing.table.TableCellEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTableModel;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.engines.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of engines used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2012, 2025
 */
public class EnginesTableModel extends OperationsTableModel implements PropertyChangeListener {

    EngineManager engineManager = InstanceManager.getDefault(EngineManager.class); // There is only one manager

    // Defines the columns
    private static final int SELECT_COLUMN = 0;
    private static final int NUM_COLUMN = 1;
    private static final int ROAD_COLUMN = 2;
    private static final int MODEL_COLUMN = 3;
    private static final int HP_COLUMN = 4;
    private static final int WEIGHT_COLUMN = 5;
    private static final int TYPE_COLUMN = 6;
    private static final int LENGTH_COLUMN = 7;
    private static final int CONSIST_COLUMN = 8;
    private static final int LOCATION_COLUMN = 9;
    private static final int RFID_WHERE_LAST_SEEN_COLUMN = 10;
    private static final int RFID_WHEN_LAST_SEEN_COLUMN = 11;
    private static final int DESTINATION_COLUMN = 12;
    private static final int PREVIOUS_LOCATION_COLUMN = 13;
    private static final int TRAIN_COLUMN = 14;
    private static final int LAST_TRAIN_COLUMN = 15;
    private static final int MOVES_COLUMN = 16;
    private static final int BUILT_COLUMN = 17;
    private static final int OWNER_COLUMN = 18;
    private static final int VALUE_COLUMN = 19;
    private static final int RFID_COLUMN = 20;
    private static final int LAST_COLUMN = 21;
    private static final int DCC_ADDRESS_COLUMN = 22;
    private static final int COMMENT_COLUMN = 23;
    private static final int SET_COLUMN = 24;
    private static final int EDIT_COLUMN = 25;

    private static final int HIGHEST_COLUMN = EDIT_COLUMN + 1;

    public EnginesTableModel(boolean showAllLocos, String locationName, String trackName) {
        super();
        showAll = showAllLocos;
        this.locationName = locationName;
        this.trackName = trackName;
        engineManager.addPropertyChangeListener(this);
        updateList();
    }

    public final int SORTBY_NUMBER = 0;
    public final int SORTBY_ROAD = 1;
    public final int SORTBY_MODEL = 2;
    public final int SORTBY_LOCATION = 3;
    public final int SORTBY_DESTINATION = 4;
    public final int SORTBY_TRAIN = 5;
    public final int SORTBY_MOVES = 6;
    public final int SORTBY_CONSIST = 7;
    public final int SORTBY_BUILT = 8;
    public final int SORTBY_OWNER = 9;
    public final int SORTBY_VALUE = 10;
    public final int SORTBY_RFID = 11;
    public final int SORTBY_LAST = 12;
    public final int SORTBY_HP = 13;
    public final int SORTBY_DCC_ADDRESS = 14;
    public final int SORTBY_COMMENT = 15;

    private int _sort = SORTBY_NUMBER;

    /**
     * Not all columns are visible at the same time.
     *
     * @param sort which sort is active
     */
    public void setSort(int sort) {
        _sort = sort;
        updateList();
        if (sort == SORTBY_MOVES ||
                sort == SORTBY_BUILT ||
                sort == SORTBY_OWNER ||
                sort == SORTBY_VALUE ||
                sort == SORTBY_RFID ||
                sort == SORTBY_LAST ||
                sort == SORTBY_DCC_ADDRESS ||
                sort == SORTBY_COMMENT) {
            XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
            tcm.setColumnVisible(tcm.getColumnByModelIndex(MOVES_COLUMN), sort == SORTBY_MOVES);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(BUILT_COLUMN), sort == SORTBY_BUILT);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(OWNER_COLUMN), sort == SORTBY_OWNER);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(VALUE_COLUMN), sort == SORTBY_VALUE);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_COLUMN), sort == SORTBY_RFID);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_WHEN_LAST_SEEN_COLUMN), sort == SORTBY_RFID);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_WHERE_LAST_SEEN_COLUMN), sort == SORTBY_RFID);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(PREVIOUS_LOCATION_COLUMN), sort == SORTBY_LAST);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_COLUMN), sort == SORTBY_LAST);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_TRAIN_COLUMN), sort == SORTBY_LAST);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(TRAIN_COLUMN), sort != SORTBY_LAST);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(DCC_ADDRESS_COLUMN), sort == SORTBY_DCC_ADDRESS);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(COMMENT_COLUMN), sort == SORTBY_COMMENT);
        }
        fireTableDataChanged();
    }

    public void toggleSelectVisible() {
        XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(SELECT_COLUMN),
                !tcm.isColumnVisible(tcm.getColumnByModelIndex(SELECT_COLUMN)));
    }

    public void resetCheckboxes() {
        for (Engine engine : engineList) {
            engine.setSelected(false);
        }
    }

    public String getSortByName() {
        return getSortByName(_sort);
    }

    public String getSortByName(int sort) {
        switch (sort) {
            case SORTBY_NUMBER:
                return Bundle.getMessage("Number");
            case SORTBY_ROAD:
                return Bundle.getMessage("Road");
            case SORTBY_MODEL:
                return Bundle.getMessage("Model");
            case SORTBY_LOCATION:
                return Bundle.getMessage("Location");
            case SORTBY_DESTINATION:
                return Bundle.getMessage("Destination");
            case SORTBY_TRAIN:
                return Bundle.getMessage("Train");
            case SORTBY_MOVES:
                return Bundle.getMessage("Moves");
            case SORTBY_CONSIST:
                return Bundle.getMessage("Consist");
            case SORTBY_BUILT:
                return Bundle.getMessage("Built");
            case SORTBY_OWNER:
                return Bundle.getMessage("Owner");
            case SORTBY_DCC_ADDRESS:
                return Bundle.getMessage("DccAddress");
            case SORTBY_HP:
                return Bundle.getMessage("HP");
            case SORTBY_VALUE:
                return Setup.getValueLabel();
            case SORTBY_RFID:
                return Setup.getRfidLabel();
            case SORTBY_LAST:
                return Bundle.getMessage("Last");
            case SORTBY_COMMENT:
                return Bundle.getMessage("Comment");
            default:
                return "Error"; // NOI18N
        }
    }

    /**
     * Search for engine by road number
     * 
     * @param roadNumber The string road number to search for.
     *
     * @return -1 if not found, table row number if found
     */
    public int findEngineByRoadNumber(String roadNumber) {
        return findRollingStockByRoadNumber(roadNumber, engineList);
    }

    public Engine getEngineAtIndex(int index) {
        return engineList.get(index);
    }

    private void updateList() {
        // first, remove listeners from the individual objects
        removePropertyChangeEngines();
        engineList = getSelectedEngineList();
        // and add listeners back in
        addPropertyChangeEngines();
    }

    public List<Engine> getSelectedEngineList() {
        return getEngineList(_sort);
    }

    public List<Engine> getEngineList(int sort) {
        List<Engine> list;
        switch (sort) {
            case SORTBY_ROAD:
                list = engineManager.getByRoadNameList();
                break;
            case SORTBY_MODEL:
                list = engineManager.getByModelList();
                break;
            case SORTBY_LOCATION:
                list = engineManager.getByLocationList();
                break;
            case SORTBY_DESTINATION:
                list = engineManager.getByDestinationList();
                break;
            case SORTBY_TRAIN:
                list = engineManager.getByTrainList();
                break;
            case SORTBY_MOVES:
                list = engineManager.getByMovesList();
                break;
            case SORTBY_CONSIST:
                list = engineManager.getByConsistList();
                break;
            case SORTBY_OWNER:
                list = engineManager.getByOwnerList();
                break;
            case SORTBY_BUILT:
                list = engineManager.getByBuiltList();
                break;
            case SORTBY_VALUE:
                list = engineManager.getByValueList();
                break;
            case SORTBY_RFID:
                list = engineManager.getByRfidList();
                break;
            case SORTBY_LAST:
                list = engineManager.getByLastDateList();
                break;
            case SORTBY_COMMENT:
                list = engineManager.getByCommentList();
                break;
            case SORTBY_NUMBER:
            default:
                list = engineManager.getByNumberList();
        }
        filterList(list);
        return list;
    }

    List<Engine> engineList = null;

    EnginesTableFrame _frame;

    void initTable(JTable table, EnginesTableFrame frame) {
        _table = table;
        _frame = frame;
        initTable();
    }

    // Default engines frame table column widths, starts with Number column and ends with Edit
    private final int[] _enginesTableColumnWidths =
            {60, 60, 60, 65, 50, 65, 65, 35, 75, 190, 190, 190, 140, 190, 65, 90, 50, 50, 50, 50, 100, 130, 50, 100, 65,
                    70};

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
        // load defaults, xml file data not found
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            tcm.getColumn(i).setPreferredWidth(_enginesTableColumnWidths[i]);
        }
        _frame.loadTableDetails(_table);

        // turn off columns
        tcm.setColumnVisible(tcm.getColumnByModelIndex(BUILT_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OWNER_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(VALUE_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_WHEN_LAST_SEEN_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_WHERE_LAST_SEEN_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(PREVIOUS_LOCATION_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_TRAIN_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(DCC_ADDRESS_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(COMMENT_COLUMN), false);

        // turn on default
        tcm.setColumnVisible(tcm.getColumnByModelIndex(MOVES_COLUMN), true);
    }

    @Override
    public int getRowCount() {
        return engineList.size();
    }

    @Override
    public int getColumnCount() {
        return HIGHEST_COLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case SELECT_COLUMN:
                return Bundle.getMessage("ButtonSelect");
            case NUM_COLUMN:
                return Bundle.getMessage("Number");
            case ROAD_COLUMN:
                return Bundle.getMessage("Road");
            case MODEL_COLUMN:
                return Bundle.getMessage("Model");
            case HP_COLUMN:
                return Bundle.getMessage("HP");
            case TYPE_COLUMN:
                return Bundle.getMessage("Type");
            case LENGTH_COLUMN:
                return Bundle.getMessage("Len");
            case WEIGHT_COLUMN:
                return Bundle.getMessage("Weight");
            case CONSIST_COLUMN:
                return Bundle.getMessage("Consist");
            case LOCATION_COLUMN:
                return Bundle.getMessage("Location");
            case RFID_WHERE_LAST_SEEN_COLUMN:
                return Bundle.getMessage("WhereLastSeen");
            case RFID_WHEN_LAST_SEEN_COLUMN:
                return Bundle.getMessage("WhenLastSeen");
            case DESTINATION_COLUMN:
                return Bundle.getMessage("Destination");
            case PREVIOUS_LOCATION_COLUMN:
                return Bundle.getMessage("LastLocation");
            case TRAIN_COLUMN:
                return Bundle.getMessage("Train");
            case LAST_TRAIN_COLUMN:
                return Bundle.getMessage("LastTrain");
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
            case LAST_COLUMN:
                return Bundle.getMessage("LastMoved");
            case DCC_ADDRESS_COLUMN:
                return Bundle.getMessage("DccAddress");
            case COMMENT_COLUMN:
                return Bundle.getMessage("Comment");
            case SET_COLUMN:
                return Bundle.getMessage("Set");
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit"); // titles above all columns
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case SELECT_COLUMN:
                return Boolean.class;
            case SET_COLUMN:
            case EDIT_COLUMN:
                return JButton.class;
            case LENGTH_COLUMN:
            case MOVES_COLUMN:
                return Integer.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case SELECT_COLUMN:
            case SET_COLUMN:
            case EDIT_COLUMN:
            case MOVES_COLUMN:
            case VALUE_COLUMN:
            case RFID_COLUMN:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row >= getRowCount()) {
            return "ERROR row " + row; // NOI18N
        }
        Engine engine = engineList.get(row);
        if (engine == null) {
            return "ERROR engine unknown " + row; // NOI18N
        }
        switch (col) {
            case SELECT_COLUMN:
                return engine.isSelected();
            case NUM_COLUMN:
                return engine.getNumber();
            case ROAD_COLUMN:
                return engine.getRoadName();
            case LENGTH_COLUMN:
                return engine.getLengthInteger();
            case MODEL_COLUMN:
                return engine.getModel();
            case HP_COLUMN:
                return engine.getHp();
            case TYPE_COLUMN: {
                if (engine.isBunit()) {
                    return engine.getTypeName() + " " + Bundle.getMessage("(B)");
                }
                return engine.getTypeName();
            }
            case WEIGHT_COLUMN:
                return engine.getWeightTons();
            case CONSIST_COLUMN: {
                if (engine.isLead()) {
                    return engine.getConsistName() + "*";
                }
                return engine.getConsistName();
            }
            case LOCATION_COLUMN: {
                String s = engine.getStatus();
                if (!engine.getLocationName().equals(Engine.NONE)) {
                    s = engine.getStatus() + engine.getLocationName() + " (" + engine.getTrackName() + ")";
                }
                return s;
            }
            case RFID_WHERE_LAST_SEEN_COLUMN: {
                return engine.getWhereLastSeenName() +
                        (engine.getTrackLastSeenName().equals(Engine.NONE) ? "" : " (" + engine.getTrackLastSeenName() + ")");
            }
            case RFID_WHEN_LAST_SEEN_COLUMN: {
                return engine.getWhenLastSeenDate();
            }
            case DESTINATION_COLUMN: {
                String s = "";
                if (!engine.getDestinationName().equals(Engine.NONE)) {
                    s = engine.getDestinationName() + " (" + engine.getDestinationTrackName() + ")";
                }
                return s;
            }
            case PREVIOUS_LOCATION_COLUMN: {
                String s = "";
                if (!engine.getLastLocationName().equals(Engine.NONE)) {
                    s = engine.getLastLocationName() + " (" + engine.getLastTrackName() + ")";
                }
                return s;
            }
            case TRAIN_COLUMN: {
                // if train was manually set by user add an asterisk
                if (engine.getTrain() != null && engine.getRouteLocation() == null) {
                    return engine.getTrainName() + "*";
                }
                return engine.getTrainName();
            }
            case LAST_TRAIN_COLUMN:
                return engine.getLastTrainName();
            case MOVES_COLUMN:
                return engine.getMoves();
            case BUILT_COLUMN:
                return engine.getBuilt();
            case OWNER_COLUMN:
                return engine.getOwnerName();
            case VALUE_COLUMN:
                return engine.getValue();
            case RFID_COLUMN:
                return engine.getRfid();
            case LAST_COLUMN:
                return engine.getSortDate();
            case DCC_ADDRESS_COLUMN:
                return engine.getDccAddress();
            case COMMENT_COLUMN:
                return engine.getComment();
            case SET_COLUMN:
                return Bundle.getMessage("Set");
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    EngineEditFrame engineEditFrame = null;
    EngineSetFrame engineSetFrame = null;

    @Override
    public void setValueAt(Object value, int row, int col) {
        Engine engine = engineList.get(row);
        switch (col) {
            case SELECT_COLUMN:
                engine.setSelected(((Boolean) value).booleanValue());
                break;
            case MOVES_COLUMN:
                try {
                    engine.setMoves(Integer.parseInt(value.toString()));
                } catch (NumberFormatException e) {
                    log.error("move count must be a number");
                }
                break;
            case BUILT_COLUMN:
                engine.setBuilt(value.toString());
                break;
            case OWNER_COLUMN:
                engine.setOwnerName(value.toString());
                break;
            case VALUE_COLUMN:
                engine.setValue(value.toString());
                break;
            case RFID_COLUMN:
                engine.setRfid(value.toString());
                break;
            case SET_COLUMN:
                log.debug("Set engine location");
                if (engineSetFrame != null) {
                    engineSetFrame.dispose();
                }
                // use invokeLater so new window appears on top
                SwingUtilities.invokeLater(() -> {
                    engineSetFrame = new EngineSetFrame();
                    engineSetFrame.initComponents();
                    engineSetFrame.load(engine);
                });
                break;
            case EDIT_COLUMN:
                log.debug("Edit engine");
                if (engineEditFrame != null) {
                    engineEditFrame.dispose();
                }
                // use invokeLater so new window appears on top
                SwingUtilities.invokeLater(() -> {
                    engineEditFrame = new EngineEditFrame();
                    engineEditFrame.initComponents();
                    engineEditFrame.load(engine);
                });
                break;
            default:
                break;
        }
    }

    public void dispose() {
        log.debug("dispose EngineTableModel");
        engineManager.removePropertyChangeListener(this);
        removePropertyChangeEngines();
        if (engineSetFrame != null) {
            engineSetFrame.dispose();
        }
        if (engineEditFrame != null) {
            engineEditFrame.dispose();
        }
    }

    private void addPropertyChangeEngines() {
        for (RollingStock rs : engineManager.getList()) {
            rs.addPropertyChangeListener(this);
        }
    }

    private void removePropertyChangeEngines() {
        for (RollingStock rs : engineManager.getList()) {
            rs.removePropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(EngineManager.LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(ConsistManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        }
        // Engine length, type, and HP are based on model, so multiple changes
        else if (e.getPropertyName().equals(Engine.LENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Engine.TYPE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Engine.HP_CHANGED_PROPERTY)) {
            fireTableDataChanged();
        }
        // must be a engine change
        else if (e.getSource().getClass().equals(Engine.class)) {
            Engine engine = (Engine) e.getSource();
            int row = engineList.indexOf(engine);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update engine table row: {}", row);
            }
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
                // next is needed when only showing engines at a location or track
            } else if (e.getPropertyName().equals(Engine.TRACK_CHANGED_PROPERTY)) {
                updateList();
                fireTableDataChanged();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EnginesTableModel.class);
}
