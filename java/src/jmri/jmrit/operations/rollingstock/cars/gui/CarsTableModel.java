package jmri.jmrit.operations.rollingstock.cars.gui;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.*;
import javax.swing.table.TableCellEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTableModel;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of cars used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012, 2016, 2025
 */
public class CarsTableModel extends OperationsTableModel implements PropertyChangeListener {

    CarManager carManager = InstanceManager.getDefault(CarManager.class); // There is only one manager

    // Defines the columns
    private static final int SELECT_COLUMN = 0;
    private static final int NUMBER_COLUMN = 1;
    private static final int ROAD_COLUMN = 2;
    private static final int TYPE_COLUMN = 3;
    private static final int LENGTH_COLUMN = 4;
    private static final int LOAD_COLUMN = 5;
    private static final int RWE_LOAD_COLUMN = 6;
    private static final int RWL_LOAD_COLUMN = 7;
    private static final int COLOR_COLUMN = 8;
    private static final int KERNEL_COLUMN = 9;
    private static final int LOCATION_COLUMN = 10;
    private static final int RFID_WHERE_LAST_SEEN_COLUMN = 11;
    private static final int RFID_WHEN_LAST_SEEN_COLUMN = 12;
    private static final int DESTINATION_COLUMN = 13;
    private static final int FINAL_DESTINATION_COLUMN = 14;
    private static final int RWE_DESTINATION_COLUMN = 15;
    private static final int RWL_DESTINATION_COLUMN = 16;
    private static final int ROUTE_COLUMN = 17;
    private static final int LAST_LOCATION_COLUMN = 18;
    private static final int DIVISION_COLUMN = 19;
    private static final int TRAIN_COLUMN = 20;
    private static final int LAST_TRAIN_COLUMN = 21;
    private static final int MOVES_COLUMN = 22;
    private static final int BUILT_COLUMN = 23;
    private static final int OWNER_COLUMN = 24;
    private static final int VALUE_COLUMN = 25;
    private static final int RFID_COLUMN = 26;
    private static final int WAIT_COLUMN = 27;
    private static final int PICKUP_COLUMN = 28;
    private static final int SETOUT_COLUMN = 29;
    private static final int LAST_COLUMN = 30;
    private static final int COMMENT_COLUMN = 31;
    private static final int SET_COLUMN = 32;
    private static final int EDIT_COLUMN = 33;

    private static final int HIGHESTCOLUMN = EDIT_COLUMN + 1;

    public final int SORTBY_NUMBER = 0;
    public final int SORTBY_ROAD = 1;
    public final int SORTBY_TYPE = 2;
    public final int SORTBY_LOCATION = 3;
    public final int SORTBY_DESTINATION = 4;
    public final int SORTBY_TRAIN = 5;
    public final int SORTBY_MOVES = 6;
    public final int SORTBY_KERNEL = 7;
    public final int SORTBY_LOAD = 8;
    public final int SORTBY_COLOR = 9;
    public final int SORTBY_BUILT = 10;
    public final int SORTBY_OWNER = 11;
    public final int SORTBY_RFID = 12;
    public final int SORTBY_RWE = 13; // return when empty
    public final int SORTBY_RWL = 14; // return when loaded
    public final int SORTBY_ROUTE = 15;
    public final int SORTBY_DIVISION = 16;
    public final int SORTBY_FINALDESTINATION = 17;
    public final int SORTBY_VALUE = 18;
    public final int SORTBY_WAIT = 19;
    public final int SORTBY_PICKUP = 20;
    public final int SORTBY_LAST = 21;
    public final int SORTBY_COMMENT = 22; // also used by PrintCarRosterAction

    private int _sort = SORTBY_NUMBER;

    List<Car> carList = null; // list of cars
    //    JTable _table;
    CarsTableFrame _frame;

    public CarsTableModel(boolean showAllCars, String locationName, String trackName) {
        super();
        showAll = showAllCars;
        this.locationName = locationName;
        this.trackName = trackName;
        carManager.addPropertyChangeListener(this);
        updateList();
    }

    /**
     * Not all columns in the Cars table are shown. This was done to limit the
     * width of the table. Only one column from the following groups is shown at
     * any one time.
     * <p>
     * Load, Color, and RWE Load are grouped together.
     * <p>
     * Destination, Final Destination, and RWE Destination are grouped together.
     * <p>
     * Moves, Built, Owner, Value, RFID, Wait, Pickup, and Last are grouped
     * together.
     * 
     * @param sort The integer sort to use.
     */
    public void setSort(int sort) {
        _sort = sort;
        updateList();
        XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
        if (sort == SORTBY_COLOR || sort == SORTBY_LOAD || sort == SORTBY_RWE || sort == SORTBY_RWL) {
            tcm.setColumnVisible(tcm.getColumnByModelIndex(LOAD_COLUMN), sort == SORTBY_LOAD);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(COLOR_COLUMN), sort == SORTBY_COLOR);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RWE_LOAD_COLUMN), sort == SORTBY_RWE);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RWL_LOAD_COLUMN), sort == SORTBY_RWL);
        }
        if (sort == SORTBY_DIVISION) {
            tcm.setColumnVisible(tcm.getColumnByModelIndex(DIVISION_COLUMN), true);
        }
        if (sort == SORTBY_TRAIN) {
            tcm.setColumnVisible(tcm.getColumnByModelIndex(TRAIN_COLUMN), true);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_TRAIN_COLUMN), false);
        }
        if (sort == SORTBY_DESTINATION ||
                sort == SORTBY_FINALDESTINATION ||
                sort == SORTBY_RWE ||
                sort == SORTBY_RWL ||
                sort == SORTBY_ROUTE) {
            tcm.setColumnVisible(tcm.getColumnByModelIndex(DESTINATION_COLUMN), sort == SORTBY_DESTINATION);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(FINAL_DESTINATION_COLUMN), sort == SORTBY_FINALDESTINATION);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RWE_DESTINATION_COLUMN), sort == SORTBY_RWE);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RWL_DESTINATION_COLUMN), sort == SORTBY_RWL);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RWE_LOAD_COLUMN), sort == SORTBY_RWE);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RWL_LOAD_COLUMN), sort == SORTBY_RWL);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(ROUTE_COLUMN), sort == SORTBY_ROUTE);

            // show load column if color column isn't visible.
            tcm.setColumnVisible(tcm.getColumnByModelIndex(LOAD_COLUMN),
                    sort != SORTBY_RWE &&
                            sort != SORTBY_RWL &&
                            !tcm.isColumnVisible(tcm.getColumnByModelIndex(COLOR_COLUMN)));
        } else if (sort == SORTBY_MOVES ||
                sort == SORTBY_BUILT ||
                sort == SORTBY_OWNER ||
                sort == SORTBY_VALUE ||
                sort == SORTBY_RFID ||
                sort == SORTBY_WAIT ||
                sort == SORTBY_PICKUP ||
                sort == SORTBY_LAST ||
                sort == SORTBY_COMMENT) {
            tcm.setColumnVisible(tcm.getColumnByModelIndex(MOVES_COLUMN), sort == SORTBY_MOVES);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(BUILT_COLUMN), sort == SORTBY_BUILT);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(OWNER_COLUMN), sort == SORTBY_OWNER);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(VALUE_COLUMN), sort == SORTBY_VALUE);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_COLUMN), sort == SORTBY_RFID);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_WHEN_LAST_SEEN_COLUMN), sort == SORTBY_RFID);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_WHERE_LAST_SEEN_COLUMN), sort == SORTBY_RFID);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(WAIT_COLUMN), sort == SORTBY_WAIT);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(PICKUP_COLUMN), sort == SORTBY_PICKUP);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(SETOUT_COLUMN), sort == SORTBY_PICKUP);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_LOCATION_COLUMN), sort == SORTBY_LAST);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_COLUMN), sort == SORTBY_LAST);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(TRAIN_COLUMN), sort != SORTBY_LAST);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_TRAIN_COLUMN), sort == SORTBY_LAST);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(COMMENT_COLUMN), sort == SORTBY_COMMENT);
        }
        fireTableDataChanged();
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
            case SORTBY_TYPE:
                return Bundle.getMessage("Type");
            case SORTBY_COLOR:
                return Bundle.getMessage("Color");
            case SORTBY_LOAD:
                return Bundle.getMessage("Load");
            case SORTBY_KERNEL:
                return Bundle.getMessage("Kernel");
            case SORTBY_LOCATION:
                return Bundle.getMessage("Location");
            case SORTBY_DESTINATION:
                return Bundle.getMessage("Destination");
            case SORTBY_DIVISION:
                return Bundle.getMessage("HomeDivision");
            case SORTBY_TRAIN:
                return Bundle.getMessage("Train");
            case SORTBY_FINALDESTINATION:
                return Bundle.getMessage("FinalDestination");
            case SORTBY_RWE:
                return Bundle.getMessage("ReturnWhenEmpty");
            case SORTBY_RWL:
                return Bundle.getMessage("ReturnWhenLoaded");
            case SORTBY_ROUTE:
                return Bundle.getMessage("Route");
            case SORTBY_MOVES:
                return Bundle.getMessage("Moves");
            case SORTBY_BUILT:
                return Bundle.getMessage("Built");
            case SORTBY_OWNER:
                return Bundle.getMessage("Owner");
            case SORTBY_VALUE:
                return Setup.getValueLabel();
            case SORTBY_RFID:
                return Setup.getRfidLabel();
            case SORTBY_WAIT:
                return Bundle.getMessage("Wait");
            case SORTBY_PICKUP:
                return Bundle.getMessage("Pickup");
            case SORTBY_LAST:
                return Bundle.getMessage("Last");
            case SORTBY_COMMENT:
                return Bundle.getMessage("Comment");
            default:
                return "Error"; // NOI18N
        }
    }

    @Override
    protected Color getForegroundColor(int row) {
        Car car = carList.get(row);
        if (car.getLocation() != null && car.getTrack() == null) {
            return Color.red;
        }
        return super.getForegroundColor(row);
    }

    public void toggleSelectVisible() {
        XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(SELECT_COLUMN),
                !tcm.isColumnVisible(tcm.getColumnByModelIndex(SELECT_COLUMN)));
    }

    public void resetCheckboxes() {
        for (Car car : carList) {
            car.setSelected(false);
        }
    }

    /**
     * Search for car by road number
     * 
     * @param roadNumber The string road number to search for.
     * @return -1 if not found, table row number if found
     */
    public int findCarByRoadNumber(String roadNumber) {
        return findRollingStockByRoadNumber(roadNumber, carList);
    }

    public Car getCarAtIndex(int index) {
        return carList.get(index);
    }

    private void updateList() {
        // first, remove listeners from the individual objects
        removePropertyChangeCars();
        carList = getSelectedCarList();
        // and add listeners back in
        addPropertyChangeCars();
    }

    public List<Car> getSelectedCarList() {
        return getCarList(_sort);
    }

    @SuppressFBWarnings(value = "DB_DUPLICATE_SWITCH_CLAUSES", justification = "default case is sort by number") // NOI18N
    public List<Car> getCarList(int sort) {
        List<Car> list;
        switch (sort) {
            case SORTBY_NUMBER:
                list = carManager.getByNumberList();
                break;
            case SORTBY_ROAD:
                list = carManager.getByRoadNameList();
                break;
            case SORTBY_TYPE:
                list = carManager.getByTypeList();
                break;
            case SORTBY_COLOR:
                list = carManager.getByColorList();
                break;
            case SORTBY_LOAD:
                list = carManager.getByLoadList();
                break;
            case SORTBY_KERNEL:
                list = carManager.getByKernelList();
                break;
            case SORTBY_LOCATION:
                list = carManager.getByLocationList();
                break;
            case SORTBY_DESTINATION:
                list = carManager.getByDestinationList();
                break;
            case SORTBY_TRAIN:
                list = carManager.getByTrainList();
                break;
            case SORTBY_FINALDESTINATION:
                list = carManager.getByFinalDestinationList();
                break;
            case SORTBY_RWE:
                list = carManager.getByRweList();
                break;
            case SORTBY_RWL:
                list = carManager.getByRwlList();
                break;
            case SORTBY_ROUTE:
                list = carManager.getByRouteList();
                break;
            case SORTBY_DIVISION:
                list = carManager.getByDivisionList();
                break;
            case SORTBY_MOVES:
                list = carManager.getByMovesList();
                break;
            case SORTBY_BUILT:
                list = carManager.getByBuiltList();
                break;
            case SORTBY_OWNER:
                list = carManager.getByOwnerList();
                break;
            case SORTBY_VALUE:
                list = carManager.getByValueList();
                break;
            case SORTBY_RFID:
                list = carManager.getByRfidList();
                break;
            case SORTBY_WAIT:
                list = carManager.getByWaitList();
                break;
            case SORTBY_PICKUP:
                list = carManager.getByPickupList();
                break;
            case SORTBY_LAST:
                list = carManager.getByLastDateList();
                break;
            case SORTBY_COMMENT:
                list = carManager.getByCommentList();
                break;
            default:
                list = carManager.getByNumberList();
        }
        filterCarList(list);
        return list;
    }

    private void filterCarList(List<Car> list) {
        if (!Control.showCloneCars) {
            for (int i = 0; i < list.size(); i++) {
                Car car = list.get(i);
                if (car.isClone()) {
                    list.remove(i--);
                    continue;
                }
            }
        }
        filterList(list);
    }

    void initTable(JTable table, CarsTableFrame frame) {
        super.initTable(table);
        _frame = frame;
        initTable();
    }

    // Cars frame table column widths, starts with Select column and ends with Edit
    private final int[] tableColumnWidths = {60, 60, 60, 65, 35, 75, 75, 75, 75, 65, 190, 190, 140, 190, 190, 190, 190,
            190, 190, 190, 65, 90, 50, 50, 50, 50, 100, 50, 100, 60, 100, 100, 65, 70};

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
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            tcm.getColumn(i).setPreferredWidth(tableColumnWidths[i]);
        }
        _frame.loadTableDetails(_table);

        // turn off columns
        tcm.setColumnVisible(tcm.getColumnByModelIndex(COLOR_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(FINAL_DESTINATION_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(RWE_DESTINATION_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(RWE_LOAD_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(RWL_DESTINATION_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(RWL_LOAD_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(ROUTE_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(BUILT_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OWNER_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(VALUE_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_WHEN_LAST_SEEN_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(RFID_WHERE_LAST_SEEN_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(WAIT_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(PICKUP_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(SETOUT_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_LOCATION_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(LAST_TRAIN_COLUMN), false);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(COMMENT_COLUMN), false);

        // turn on defaults
        tcm.setColumnVisible(tcm.getColumnByModelIndex(LOAD_COLUMN), true);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(DESTINATION_COLUMN), true);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(MOVES_COLUMN), true);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(TRAIN_COLUMN), true);

        tcm.setColumnVisible(tcm.getColumnByModelIndex(DIVISION_COLUMN), carManager.isThereDivisions());
    }

    @Override
    public int getRowCount() {
        return carList.size();
    }

    @Override
    public int getColumnCount() {
        return HIGHESTCOLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case SELECT_COLUMN:
                return Bundle.getMessage("ButtonSelect");
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
            case RFID_WHERE_LAST_SEEN_COLUMN:
                return Bundle.getMessage("WhereLastSeen");
            case RFID_WHEN_LAST_SEEN_COLUMN:
                return Bundle.getMessage("WhenLastSeen");
            case DESTINATION_COLUMN:
                return Bundle.getMessage("Destination");
            case FINAL_DESTINATION_COLUMN:
                return Bundle.getMessage("FinalDestination");
            case RWE_DESTINATION_COLUMN:
                return Bundle.getMessage("RWELocation");
            case RWE_LOAD_COLUMN:
                return Bundle.getMessage("RWELoad");
            case RWL_DESTINATION_COLUMN:
                return Bundle.getMessage("RWLLocation");
            case RWL_LOAD_COLUMN:
                return Bundle.getMessage("RWLLoad");
            case ROUTE_COLUMN:
                return Bundle.getMessage("Route");
            case LAST_LOCATION_COLUMN:
                return Bundle.getMessage("LastLocation");
            case DIVISION_COLUMN:
                return Bundle.getMessage("HomeDivision");
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
            case WAIT_COLUMN:
                return Bundle.getMessage("Wait");
            case PICKUP_COLUMN:
                return Bundle.getMessage("Pickup");
            case SETOUT_COLUMN:
                return Bundle.getMessage("SetOut");
            case LAST_COLUMN:
                return Bundle.getMessage("LastMoved");
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
            case WAIT_COLUMN:
                return Integer.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        Car car = carList.get(row);
        if (car.isClone()) {
            return false;
        }
        switch (col) {
            case SELECT_COLUMN:
            case SET_COLUMN:
            case EDIT_COLUMN:
            case MOVES_COLUMN:
            case WAIT_COLUMN:
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
        Car car = carList.get(row);
        if (car == null) {
            return "ERROR car unknown " + row; // NOI18N
        }
        switch (col) {
            case SELECT_COLUMN:
                return car.isSelected();
            case NUMBER_COLUMN:
                return car.getNumber();
            case ROAD_COLUMN:
                return car.getRoadName();
            case LOAD_COLUMN:
                return getLoadNameString(car);
            case COLOR_COLUMN:
                return car.getColor();
            case LENGTH_COLUMN:
                return car.getLengthInteger();
            case TYPE_COLUMN:
                return car.getTypeName() + car.getTypeExtensions();
            case KERNEL_COLUMN:
                if (car.isLead()) {
                    return car.getKernelName() + "*";
                }
                return car.getKernelName();
            case LOCATION_COLUMN:
                if (car.getLocation() != null) {
                    return car.getStatus() + car.getLocationName() + " (" + car.getTrackName() + ")";
                }
                return car.getStatus();
            case RFID_WHERE_LAST_SEEN_COLUMN:
                return car.getWhereLastSeenName() +
                        (car.getTrackLastSeenName().equals(Car.NONE) ? "" : " (" + car.getTrackLastSeenName() + ")");
            case RFID_WHEN_LAST_SEEN_COLUMN: {
                return car.getWhenLastSeenDate();
            }
            case DESTINATION_COLUMN:
            case FINAL_DESTINATION_COLUMN: {
                String s = "";
                if (car.getDestination() != null) {
                    s = car.getDestinationName() + " (" + car.getDestinationTrackName() + ")";
                }
                if (log.isDebugEnabled() &&
                        car.getDestinationTrack() != null &&
                        car.getDestinationTrack().getSchedule() != null) {
                    s = s + " " + car.getScheduleItemId() + " ";
                }
                if (car.getFinalDestination() != null) {
                    s = s + "->" + car.getFinalDestinationName(); // NOI18N
                }
                if (car.getFinalDestinationTrack() != null) {
                    s = s + " (" + car.getFinalDestinationTrackName() + ")";
                }
                if (log.isDebugEnabled() &&
                        !s.contains(car.getScheduleItemId()) &&
                        car.getFinalDestinationTrack() != null &&
                        car.getFinalDestinationTrack().getSchedule() != null) {
                    s = s + " " + car.getScheduleItemId();
                }
                return s;
            }
            case RWE_DESTINATION_COLUMN: {
                String s = car.getReturnWhenEmptyDestinationName();
                if (car.getReturnWhenEmptyDestTrack() != null) {
                    s = s + " (" + car.getReturnWhenEmptyDestTrackName() + ")";
                }
                return s;
            }
            case RWE_LOAD_COLUMN:
                return car.getReturnWhenEmptyLoadName();
            case RWL_DESTINATION_COLUMN: {
                String s = car.getReturnWhenLoadedDestinationName();
                if (car.getReturnWhenLoadedDestTrack() != null) {
                    s = s + " (" + car.getReturnWhenLoadedDestTrackName() + ")";
                }
                return s;
            }
            case RWL_LOAD_COLUMN:
                return car.getReturnWhenLoadedLoadName();
            case ROUTE_COLUMN:
                return car.getRoutePath();
            case DIVISION_COLUMN:
                return car.getDivisionName();
            case LAST_LOCATION_COLUMN: {
                String s = "";
                if (!car.getLastLocationName().equals(Car.NONE)) {
                    s = car.getLastLocationName() + " (" + car.getLastTrackName() + ")";
                }
                return s;
            }
            case TRAIN_COLUMN: {
                // if train was manually set by user add an asterisk
                if (car.getTrain() != null && car.getRouteLocation() == null) {
                    return car.getTrainName() + "*";
                }
                return car.getTrainName();
            }
            case LAST_TRAIN_COLUMN:
                return car.getLastTrainName();
            case MOVES_COLUMN:
                return car.getMoves();
            case BUILT_COLUMN:
                return car.getBuilt();
            case OWNER_COLUMN:
                return car.getOwnerName();
            case VALUE_COLUMN:
                return car.getValue();
            case RFID_COLUMN:
                return car.getRfid();
            case WAIT_COLUMN:
                return car.getWait();
            case PICKUP_COLUMN:
                return car.getPickupScheduleName();
            case SETOUT_COLUMN:
                return car.getSetoutTime();
            case LAST_COLUMN:
                return car.getSortDate();
            case COMMENT_COLUMN:
                return car.getComment();
            case SET_COLUMN:
                return Bundle.getMessage("Set");
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    private String getLoadNameString(Car car) {
        StringBuffer sb = new StringBuffer(car.getLoadName());
        if (car.getLoadPriority().equals(CarLoad.PRIORITY_HIGH)) {
            sb.append(" " + Bundle.getMessage("(P)"));
        } else if (car.getLoadPriority().equals(CarLoad.PRIORITY_MEDIUM)) {
            sb.append(" " + Bundle.getMessage("(M)"));
        }
        if (car.isCarLoadHazardous()) {
            sb.append(" " + Bundle.getMessage("(H)"));
        }
        return sb.toString();
    }

    CarEditFrame cef = null;
    CarSetFrame csf = null;

    @Override
    public void setValueAt(Object value, int row, int col) {
        Car car = carList.get(row);
        switch (col) {
            case SELECT_COLUMN:
                car.setSelected(((Boolean) value).booleanValue());
                break;
            case SET_COLUMN:
                if (csf != null) {
                    csf.dispose();
                }
                // use invokeLater so new window appears on top
                SwingUtilities.invokeLater(() -> {
                    csf = new CarSetFrame();
                    csf.initComponents();
                    csf.load(car);
                });
                break;
            case EDIT_COLUMN:
                if (cef != null) {
                    cef.dispose();
                }
                // use invokeLater so new window appears on top
                SwingUtilities.invokeLater(() -> {
                    cef = new CarEditFrame();
                    cef.initComponents();
                    cef.load(car);
                });
                break;
            case MOVES_COLUMN:
                try {
                    car.setMoves(Integer.parseInt(value.toString()));
                } catch (NumberFormatException e) {
                    log.error("move count must be a number");
                }
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
            default:
                break;
        }
    }

    public void dispose() {
        carManager.removePropertyChangeListener(this);
        removePropertyChangeCars();
        if (csf != null) {
            csf.dispose();
        }
        if (cef != null) {
            cef.dispose();
        }
    }

    private void addPropertyChangeCars() {
        for (Car car : carManager.getList()) {
            car.addPropertyChangeListener(this);
        }
    }

    private void removePropertyChangeCars() {
        for (Car car : carManager.getList()) {
            car.removePropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
        }
        if (e.getPropertyName().equals(CarManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        } // must be a car change
        else if (e.getSource().getClass().equals(Car.class)) {
            Car car = (Car) e.getSource();
            int row = carList.indexOf(car);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update car table row: {}", row);
            }
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
                // next is needed when only showing cars at a location or track
            } else if (e.getPropertyName().equals(Car.TRACK_CHANGED_PROPERTY)) {
                updateList();
                fireTableDataChanged();
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CarsTableModel.class);
}
