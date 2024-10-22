package jmri.jmrit.operations.trains;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import jmri.InstanceManager;
import jmri.jmrit.beantable.EnablingCheckboxRenderer;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.JmriJOptionPane;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of trains used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2012
 */
public class TrainsTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    TrainManager trainManager = InstanceManager.getDefault(TrainManager.class); // There is only one manager
    volatile List<Train> sysList = trainManager.getTrainsByTimeList();
    JTable _table = null;
    TrainsTableFrame _frame = null;
    
    // Defines the columns
    private static final int ID_COLUMN = 0;
    private static final int TIME_COLUMN = ID_COLUMN + 1;
    private static final int BUILDBOX_COLUMN = TIME_COLUMN + 1;
    private static final int BUILD_COLUMN = BUILDBOX_COLUMN + 1;
    private static final int NAME_COLUMN = BUILD_COLUMN + 1;
    private static final int DESCRIPTION_COLUMN = NAME_COLUMN + 1;
    private static final int BUILT_COLUMN = DESCRIPTION_COLUMN + 1;
    private static final int CAR_ROAD_COLUMN = BUILT_COLUMN + 1;
    private static final int CABOOSE_ROAD_COLUMN = CAR_ROAD_COLUMN + 1;
    private static final int LOCO_ROAD_COLUMN = CABOOSE_ROAD_COLUMN + 1;
    private static final int LOAD_COLUMN = LOCO_ROAD_COLUMN + 1;
    private static final int OWNER_COLUMN = LOAD_COLUMN + 1;
    private static final int ROUTE_COLUMN = OWNER_COLUMN + 1;
    private static final int DEPARTS_COLUMN = ROUTE_COLUMN + 1;
    private static final int TERMINATES_COLUMN = DEPARTS_COLUMN + 1;
    private static final int CURRENT_COLUMN = TERMINATES_COLUMN + 1;
    private static final int STATUS_COLUMN = CURRENT_COLUMN + 1;
    private static final int ACTION_COLUMN = STATUS_COLUMN + 1;
    private static final int EDIT_COLUMN = ACTION_COLUMN + 1;

    private static final int HIGHESTCOLUMN = EDIT_COLUMN + 1;

    public TrainsTableModel() {
        super();
        trainManager.addPropertyChangeListener(this);
        Setup.getDefault().addPropertyChangeListener(this);
        updateList();
    }

    public final int SORTBYTIME = 2;
    public final int SORTBYID = 7;

    private int _sort = SORTBYTIME;

    public void setSort(int sort) {
        _sort = sort;
        updateList();
        updateColumnVisible();
    }

    private boolean _showAll = true;

    public void setShowAll(boolean showAll) {
        _showAll = showAll;
        updateList();
        fireTableDataChanged();
    }

    public boolean isShowAll() {
        return _showAll;
    }

    private void updateList() {
        // first, remove listeners from the individual objects
        removePropertyChangeTrains();

        List<Train> tempList;
        if (_sort == SORTBYID) {
            tempList = trainManager.getTrainsByIdList();
        } else {
            tempList = trainManager.getTrainsByTimeList();
        }

        if (!isShowAll()) {
            // filter out trains not checked
            for (int i = tempList.size() - 1; i >= 0; i--) {
                if (!tempList.get(i).isBuildEnabled()) {
                    tempList.remove(i);
                }
            }
        }
        sysList = tempList;

        // and add listeners back in
        addPropertyChangeTrains();
    }

    private Train getTrainByRow(int row) {
        return sysList.get(row);
    }

    void initTable(JTable table, TrainsTableFrame frame) {
        _table = table;
        _frame = frame;
        // allow row color to be controlled
        table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
        initTable();
    }

    // Train frame table column widths, starts with id column and ends with edit
    private final int[] _tableColumnWidths =
            {50, 50, 50, 72, 100, 140, 50, 50, 50, 50, 50, 50, 120, 120, 120, 120, 120, 90,
            70};

    void initTable() {
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        _table.setColumnModel(tcm);
        _table.createDefaultColumnsFromModel();

        // Install the button handlers
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        ButtonRenderer buttonRenderer2 = new ButtonRenderer(); // for tool tips
        TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
        tcm.getColumn(EDIT_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(EDIT_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(ACTION_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(ACTION_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(BUILD_COLUMN).setCellRenderer(buttonRenderer2);
        tcm.getColumn(BUILD_COLUMN).setCellEditor(buttonEditor);
        _table.setDefaultRenderer(Boolean.class, new EnablingCheckboxRenderer());

        // set column preferred widths
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            tcm.getColumn(i).setPreferredWidth(_tableColumnWidths[i]);
        }
        _frame.loadTableDetails(_table);

        // turn off column
        updateColumnVisible();
    }

    private void updateColumnVisible() {
        XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(ID_COLUMN), _sort == SORTBYID);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(TIME_COLUMN), _sort == SORTBYTIME);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(BUILT_COLUMN), trainManager.isBuiltRestricted());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(CAR_ROAD_COLUMN), trainManager.isCarRoadRestricted());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(CABOOSE_ROAD_COLUMN), trainManager.isCabooseRoadRestricted());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(LOCO_ROAD_COLUMN), trainManager.isLocoRoadRestricted());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(LOAD_COLUMN), trainManager.isLoadRestricted());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(OWNER_COLUMN), trainManager.isOwnerRestricted());
    }

    @Override
    public int getRowCount() {
        return sysList.size();
    }

    @Override
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
    public static final String EDITCOLUMNNAME = Bundle.getMessage("ButtonEdit");

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case ID_COLUMN:
                return IDCOLUMNNAME;
            case TIME_COLUMN:
                return TIMECOLUMNNAME;
            case BUILDBOX_COLUMN:
                return BUILDBOXCOLUMNNAME;
            case BUILD_COLUMN:
                return BUILDCOLUMNNAME;
            case NAME_COLUMN:
                return NAMECOLUMNNAME;
            case DESCRIPTION_COLUMN:
                return DESCRIPTIONCOLUMNNAME;
            case BUILT_COLUMN:
                return Bundle.getMessage("Built");
            case CAR_ROAD_COLUMN:
                return Bundle.getMessage("RoadsCar");
            case CABOOSE_ROAD_COLUMN:
                return Bundle.getMessage("RoadsCaboose");
            case LOCO_ROAD_COLUMN:
                return Bundle.getMessage("RoadsLoco");
            case LOAD_COLUMN:
                return Bundle.getMessage("Load");
            case OWNER_COLUMN:
                return Bundle.getMessage("Owner");
            case ROUTE_COLUMN:
                return ROUTECOLUMNNAME;
            case DEPARTS_COLUMN:
                return DEPARTSCOLUMNNAME;
            case CURRENT_COLUMN:
                return CURRENTCOLUMNNAME;
            case TERMINATES_COLUMN:
                return TERMINATESCOLUMNNAME;
            case STATUS_COLUMN:
                return STATUSCOLUMNNAME;
            case ACTION_COLUMN:
                return ACTIONCOLUMNNAME;
            case EDIT_COLUMN:
                return EDITCOLUMNNAME;
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case BUILDBOX_COLUMN:
                return Boolean.class;
            case ID_COLUMN:
                return Integer.class;
            case TIME_COLUMN:
            case NAME_COLUMN:
            case DESCRIPTION_COLUMN:
            case BUILT_COLUMN:
            case CAR_ROAD_COLUMN:
            case CABOOSE_ROAD_COLUMN:
            case LOCO_ROAD_COLUMN:
            case LOAD_COLUMN:
            case OWNER_COLUMN:
            case ROUTE_COLUMN:
            case DEPARTS_COLUMN:
            case CURRENT_COLUMN:
            case TERMINATES_COLUMN:
            case STATUS_COLUMN:
                return String.class;
            case BUILD_COLUMN:
            case ACTION_COLUMN:
            case EDIT_COLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case BUILD_COLUMN:
            case BUILDBOX_COLUMN:
            case ACTION_COLUMN:
            case EDIT_COLUMN:
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
        Train train = getTrainByRow(row);
        if (train == null) {
            return "ERROR train unknown " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return Integer.parseInt(train.getId());
            case TIME_COLUMN:
                return train.getDepartureTime();
            case NAME_COLUMN:
                return train.getIconName();
            case DESCRIPTION_COLUMN:
                return train.getDescription();
            case BUILDBOX_COLUMN:
                return Boolean.valueOf(train.isBuildEnabled());
            case BUILT_COLUMN:
                return getBuiltString(train);
            case CAR_ROAD_COLUMN:
                return getModifiedString(train.getCarRoadNames().length, train.getCarRoadOption().equals(Train.ALL_ROADS),
                        train.getCarRoadOption().equals(Train.INCLUDE_ROADS));
            case CABOOSE_ROAD_COLUMN:
                return getModifiedString(train.getCabooseRoadNames().length,
                        train.getCabooseRoadOption().equals(Train.ALL_ROADS),
                        train.getCabooseRoadOption().equals(Train.INCLUDE_ROADS));
            case LOCO_ROAD_COLUMN:
                return getModifiedString(train.getLocoRoadNames().length, train.getLocoRoadOption().equals(Train.ALL_ROADS),
                        train.getLocoRoadOption().equals(Train.INCLUDE_ROADS));
            case LOAD_COLUMN:
                return getModifiedString(train.getLoadNames().length, train.getLoadOption().equals(Train.ALL_LOADS),
                        train.getLoadOption().equals(Train.INCLUDE_LOADS));
            case OWNER_COLUMN:
                return getModifiedString(train.getOwnerNames().length, train.getOwnerOption().equals(Train.ALL_OWNERS),
                        train.getOwnerOption().equals(Train.INCLUDE_OWNERS));
            case ROUTE_COLUMN:
                return train.getTrainRouteName();
            case DEPARTS_COLUMN: {
                if (train.getDepartureTrack() == null) {
                    return train.getTrainDepartsName();
                } else {
                    return train.getTrainDepartsName() + " (" + train.getDepartureTrack().getName() + ")";
                }
            }
            case CURRENT_COLUMN:
                return train.getCurrentLocationName();
            case TERMINATES_COLUMN: {
                if (train.getTerminationTrack() == null) {
                    return train.getTrainTerminatesName();
                } else {
                    return train.getTrainTerminatesName() + " (" + train.getTerminationTrack().getName() + ")";
                }
            }
            case STATUS_COLUMN:
                return train.getStatus();
            case BUILD_COLUMN: {
                if (train.isBuilt()) {
                    if (Setup.isGenerateCsvManifestEnabled() && trainManager.isOpenFileEnabled()) {
                        setToolTip(Bundle.getMessage("OpenTrainTip",
                                train.getName()), row, col);
                        return Bundle.getMessage("OpenFile");
                    }
                    if (Setup.isGenerateCsvManifestEnabled() && trainManager.isRunFileEnabled()) {
                        setToolTip(Bundle.getMessage("RunTrainTip",
                                train.getName()), row, col);
                        return Bundle.getMessage("RunFile");
                    }
                    setToolTip(Bundle.getMessage("PrintTrainTip"), row, col);
                    if (trainManager.isPrintPreviewEnabled()) {
                        return Bundle.getMessage("Preview");
                    } else if (train.isPrinted()) {
                        return Bundle.getMessage("Printed");
                    } else {
                        return Bundle.getMessage("Print");
                    }
                }
                setToolTip(Bundle.getMessage("BuildTrainTip", train.getName()),
                        row, col);
                return Bundle.getMessage("Build");
            }
            case ACTION_COLUMN: {
                if (train.isBuildFailed()) {
                    return Bundle.getMessage("Report");
                }
                if (train.getCurrentRouteLocation() == train.getTrainTerminatesRouteLocation() &&
                        trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.MOVE)) {
                    return Bundle.getMessage("Terminate");
                }
                return trainManager.getTrainsFrameTrainAction();
            }
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    private void setToolTip(String text, int row, int col) {
        XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
        ButtonRenderer buttonRenderer = (ButtonRenderer) tcm.getColumnByModelIndex(col).getCellRenderer();
        if (buttonRenderer != null) {
            buttonRenderer.setToolTipText(text);
        }
    }

    private String getBuiltString(Train train) {
        if (!train.getBuiltStartYear().equals(Train.NONE) && train.getBuiltEndYear().equals(Train.NONE)) {
            return "A " + train.getBuiltStartYear();
        }
        if (train.getBuiltStartYear().equals(Train.NONE) && !train.getBuiltEndYear().equals(Train.NONE)) {
            return "B " + train.getBuiltEndYear();
        }
        if (!train.getBuiltStartYear().equals(Train.NONE) && !train.getBuiltEndYear().equals(Train.NONE)) {
            return "R " + train.getBuiltStartYear() + ":" + train.getBuiltEndYear();
        }
        return "";
    }

    private String getModifiedString(int number, boolean all, boolean accept) {
        if (all) {
            return "";
        }
        if (accept) {
            return "A " + Integer.toString(number); // NOI18N
        }
        return "E " + Integer.toString(number); // NOI18N
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case EDIT_COLUMN:
                editTrain(row);
                break;
            case BUILD_COLUMN:
                buildTrain(row);
                break;
            case ACTION_COLUMN:
                actionTrain(row);
                break;
            case BUILDBOX_COLUMN: {
                Train train = getTrainByRow(row);
                train.setBuildEnabled(((Boolean) value).booleanValue());
                break;
            }
            default:
                break;
        }
    }

    public Color getRowColor(int row) {
        Train train = getTrainByRow(row);
        return train.getTableRowColor();
    }

    TrainEditFrame tef = null;

    private void editTrain(int row) {
        if (tef != null) {
            tef.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(() -> {
            Train train = getTrainByRow(row);
            log.debug("Edit train ({})", train.getName());
            tef = new TrainEditFrame(train);
        });
    }

    Thread build;

    private void buildTrain(int row) {
        final Train train = getTrainByRow(row);
        if (!train.isBuilt()) {
            // only one train build at a time
            if (build != null && build.isAlive()) {
                return;
            }
            // use a thread to allow table updates during build
            build = jmri.util.ThreadingUtil.newThread(new Runnable() {
                @Override
                public void run() {
                    train.build();
                }
            });
            build.setName("Build Train (" + train.getName() + ")"); // NOI18N
            build.start();
            // print build report, print manifest, run or open file
        } else {
            if (trainManager.isBuildReportEnabled()) {
                train.printBuildReport();
            }
            if (Setup.isGenerateCsvManifestEnabled() && trainManager.isOpenFileEnabled()) {
                train.openFile();
            } else if (Setup.isGenerateCsvManifestEnabled() && trainManager.isRunFileEnabled()) {
                train.runFile();
            } else {
                if (!train.printManifestIfBuilt()) {
                    log.debug("Manifest file for train ({}) not found", train.getName());
                    int result = JmriJOptionPane.showConfirmDialog(null,
                            Bundle.getMessage("TrainManifestFileMissing",
                                    train.getName()),
                            Bundle.getMessage("TrainManifestFileError"), JmriJOptionPane.YES_NO_OPTION);
                    if (result == JmriJOptionPane.YES_OPTION) {
                        train.setModified(true);
                        if (!train.printManifestIfBuilt()) {
                            log.error("Unable to create manifest for train ({})", train.getName());
                        }
                    }
                }
            }
        }
    }

    // one of five buttons: Report, Move, Reset, Conductor or Terminate
    private void actionTrain(int row) {
        // no actions while a train is being built
        if (build != null && build.isAlive()) {
            return;
        }
        Train train = getTrainByRow(row);
        // move button becomes report if failure
        if (train.isBuildFailed()) {
            train.printBuildReport();
        } else if (trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.RESET)) {
            log.debug("Reset train ({})", train.getName());
            // check to see if departure track was reused
            if (checkDepartureTrack(train)) {
                log.debug("Train is departing staging that already has inbound cars");
                JmriJOptionPane.showMessageDialog(null,
                        Bundle.getMessage("StagingTrackUsed",
                                train.getDepartureTrack().getName()),
                        Bundle.getMessage("CanNotResetTrain"), JmriJOptionPane.INFORMATION_MESSAGE);
            } else if (!train.reset()) {
                JmriJOptionPane.showMessageDialog(null,
                        Bundle.getMessage("TrainIsInRoute",
                                train.getTrainTerminatesName()),
                        Bundle.getMessage("CanNotResetTrain"), JmriJOptionPane.ERROR_MESSAGE);
            }
        } else if (!train.isBuilt()) {
            JmriJOptionPane.showMessageDialog(null,
                    Bundle.getMessage("TrainNeedsBuild", train.getName()),
                    Bundle.getMessage("CanNotPerformAction"), JmriJOptionPane.INFORMATION_MESSAGE);
        } else if (train.isBuilt() && trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.MOVE)) {
            log.debug("Move train ({})", train.getName());
            train.move();
        } else if (train.isBuilt() && trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.TERMINATE)) {
            log.debug("Terminate train ({})", train.getName());
            int status = JmriJOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("TerminateTrain",
                            train.getName(), train.getDescription()),
                    Bundle.getMessage("DoYouWantToTermiate", train.getName()),
                    JmriJOptionPane.YES_NO_OPTION);
            if (status == JmriJOptionPane.YES_OPTION) {
                train.terminate();
            }
        } else if (train.isBuilt() && trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.CONDUCTOR)) {
            log.debug("Enable conductor for train ({})", train.getName());
            launchConductor(train);
        }
    }

    /*
     * Check to see if the departure track in staging has been taken by another
     * train. return true if track has been allocated to another train.
     */
    private boolean checkDepartureTrack(Train train) {
        return (Setup.isStagingTrackImmediatelyAvail() &&
                !train.isTrainEnRoute() &&
                train.getDepartureTrack() != null &&
                train.getDepartureTrack().isStaging() &&
                train.getDepartureTrack() != train.getTerminationTrack() &&
                train.getDepartureTrack().getIgnoreUsedLengthPercentage() == Track.IGNORE_0 &&
                train.getDepartureTrack().getDropRS() > 0);
    }

    private static Hashtable<String, TrainConductorFrame> _trainConductorHashTable = new Hashtable<>();

    private void launchConductor(Train train) {
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(() -> {
            TrainConductorFrame f = _trainConductorHashTable.get(train.getId());
            // create a copy train frame
            if (f == null || !f.isVisible()) {
                f = new TrainConductorFrame(train);
                _trainConductorHashTable.put(train.getId(), f);
            } else {
                f.setExtendedState(Frame.NORMAL);
            }
            f.setVisible(true); // this also brings the frame into focus
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change {} old: {} new: {}", e.getPropertyName(), e.getOldValue(), e.getNewValue()); // NOI18N
        }
        if (e.getPropertyName().equals(Train.BUILT_YEAR_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.ROADS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.LOADS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.OWNERS_CHANGED_PROPERTY)) {
            updateColumnVisible();
        }
        if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(TrainManager.PRINTPREVIEW_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(TrainManager.OPEN_FILE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(TrainManager.RUN_FILE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Setup.MANIFEST_CSV_PROPERTY_CHANGE) ||
                e.getPropertyName().equals(TrainManager.TRAIN_ACTION_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.DEPARTURETIME_CHANGED_PROPERTY) ||
                (e.getPropertyName().equals(Train.BUILD_CHANGED_PROPERTY) && !isShowAll())) {
            SwingUtilities.invokeLater(() -> {
                updateList();
                fireTableDataChanged();
            });
        } else if (e.getSource().getClass().equals(Train.class)) {
            Train train = ((Train) e.getSource());
            SwingUtilities.invokeLater(() -> {
                int row = sysList.indexOf(train);
                if (row >= 0 && _table != null) {
                    fireTableRowsUpdated(row, row);
                    int viewRow = _table.convertRowIndexToView(row);
                    log.debug("Scroll table to row: {}, property: {}", viewRow, e.getPropertyName());
                    _table.scrollRectToVisible(_table.getCellRect(viewRow, 0, true));
                }
            });
        }
    }

    private void removePropertyChangeTrains() {
        for (Train train : trainManager.getTrainsByIdList()) {
            train.removePropertyChangeListener(this);
        }
    }

    private void addPropertyChangeTrains() {
        for (Train train : trainManager.getTrainsByIdList()) {
            train.addPropertyChangeListener(this);
        }
    }

    public void dispose() {
        if (tef != null) {
            tef.dispose();
        }
        trainManager.removePropertyChangeListener(this);
        Setup.getDefault().removePropertyChangeListener(this);
        removePropertyChangeTrains();
    }

    class MyTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                int modelRow = table.convertRowIndexToModel(row);
                // log.debug("View row: {} Column: {} Model row: {}", row, column, modelRow);
                Color background = getRowColor(modelRow);
                component.setBackground(background);
                component.setForeground(getForegroundColor(background));
            }
            return component;
        }

        Color[] darkColors = { Color.BLACK, Color.BLUE, Color.GRAY, Color.RED, Color.MAGENTA };

        /**
         * Dark colors need white lettering
         *
         */
        private Color getForegroundColor(Color background) {
            if (background == null) {
                return null;
            }
            for (Color color : darkColors) {
                if (background == color) {
                    return Color.WHITE;
                }
            }
            return Color.BLACK; // all others get black lettering
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainsTableModel.class);
}
