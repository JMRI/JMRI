package jmri.jmrit.operations.trains;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import jmri.InstanceManager;
import jmri.jmrit.beantable.EnablingCheckboxRenderer;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.routes.RouteEditFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Table Model for edit of trains used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2012
 */
public class TrainsTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    TrainManager trainManager = InstanceManager.getDefault(TrainManager.class); // There is only one manager

    // Defines the columns
    private static final int IDCOLUMN = 0;
    private static final int TIME_COLUMN = IDCOLUMN + 1;
    private static final int BUILDBOXCOLUMN = TIME_COLUMN + 1;
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
        trainManager.addPropertyChangeListener(this);
        Setup.addPropertyChangeListener(this);
        updateList();
    }

    public final int SORTBYTIME = 2;
    public final int SORTBYID = 7;

    private int _sort = SORTBYTIME;

    public void setSort(int sort) {
        _sort = sort;
        updateList();
        XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(IDCOLUMN), sort == SORTBYID);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(TIME_COLUMN), sort == SORTBYTIME);
    }

    private boolean _showAll = true;

    public void setShowAll(boolean showAll) {
        _showAll = showAll;
        updateList();
        //fireTableStructureChanged();
        initTable();
    }

    public boolean isShowAll() {
        return _showAll;
    }

    private void updateList() {
        // first, remove listeners from the individual objects
        removePropertyChangeTrains();

        if (_sort == SORTBYID) {
            sysList = trainManager.getTrainsByIdList();
        } else {
            sysList = trainManager.getTrainsByTimeList();
        }

        if (!_showAll) {
            // filter out trains not checked
            for (int i = sysList.size() - 1; i >= 0; i--) {
                if (!sysList.get(i).isBuildEnabled()) {
                    sysList.remove(i);
                }
            }
        }

        // and add listeners back in
        addPropertyChangeTrains();
    }

    List<Train> sysList = null;
    JTable _table = null;
    TrainsTableFrame _frame = null;

    void initTable(JTable table, TrainsTableFrame frame) {
        _table = table;
        _frame = frame;
        // allow row color to be controlled
        table.setDefaultRenderer(Object.class, new MyTableCellRenderer());
        initTable();
    }
    
    // Train frame table column widths (13), starts with id column and ends with edit
    private int[] _tableColumnWidths = {50, 50, 50, 72, 100, 140, 120, 120, 120, 120, 120, 90, 70};

    void initTable() {
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        _table.setColumnModel(tcm);
        _table.createDefaultColumnsFromModel();
        
        // Install the button handlers
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
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            tcm.getColumn(i).setPreferredWidth(_tableColumnWidths[i]);
        }
        _frame.loadTableDetails(_table);

        // turn off column
        tcm.setColumnVisible(tcm.getColumnByModelIndex(IDCOLUMN), _sort == SORTBYID);
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
            case IDCOLUMN:
                return IDCOLUMNNAME;
            case TIME_COLUMN:
                return TIMECOLUMNNAME;
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
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case BUILDBOXCOLUMN:
                return Boolean.class;
            case IDCOLUMN:
            case TIME_COLUMN:
            case NAMECOLUMN:
            case DESCRIPTIONCOLUMN:
            case ROUTECOLUMN:
            case DEPARTSCOLUMN:
            case CURRENTCOLUMN:
            case TERMINATESCOLUMN:
            case STATUSCOLUMN:
                return String.class;
            case BUILDCOLUMN:
            case ACTIONCOLUMN:
            case EDITCOLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case BUILDCOLUMN:
            case BUILDBOXCOLUMN:
            case ROUTECOLUMN:
            case ACTIONCOLUMN:
            case EDITCOLUMN:
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
        Train train = sysList.get(row);
        if (train == null) {
            return "ERROR train unknown " + row; // NOI18N
        }
        switch (col) {
            case IDCOLUMN:
                return train.getId();
            case TIME_COLUMN:
                return train.getDepartureTime();
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
                if (train.getDepartureTrack() == null) {
                    return train.getTrainDepartsName();
                } else {
                    return train.getTrainDepartsName() + " (" + train.getDepartureTrack().getName() + ")";
                }
            }
            case CURRENTCOLUMN:
                return train.getCurrentLocationName();
            case TERMINATESCOLUMN: {
                if (train.getTerminationTrack() == null) {
                    return train.getTrainTerminatesName();
                } else {
                    return train.getTrainTerminatesName() + " (" + train.getTerminationTrack().getName() + ")";
                }
            }
            case STATUSCOLUMN:
                return train.getStatus();
            case BUILDCOLUMN: {
                if (train.isBuilt()) {
                    if (Setup.isGenerateCsvManifestEnabled() && trainManager.isOpenFileEnabled()) {
                        return Bundle.getMessage("OpenFile");
                    } else if (Setup.isGenerateCsvManifestEnabled() && trainManager.isRunFileEnabled()) {
                        return Bundle.getMessage("RunFile");
                    } else if (trainManager.isPrintPreviewEnabled()) {
                        return Bundle.getMessage("Preview");
                    } else if (train.isPrinted()) {
                        return Bundle.getMessage("Printed");
                    } else {
                        return Bundle.getMessage("Print");
                    }
                }
                return Bundle.getMessage("Build");
            }
            case ACTIONCOLUMN: {
                if (train.getBuildFailed()) {
                    return Bundle.getMessage("Report");
                }
                if (train.getCurrentLocation() == train.getTrainTerminatesRouteLocation() &&
                        trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.MOVE)) {
                    return Bundle.getMessage("Terminate");
                }
                return trainManager.getTrainsFrameTrainAction();
            }
            case EDITCOLUMN:
                return Bundle.getMessage("ButtonEdit");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case EDITCOLUMN:
                editTrain(row);
                break;
            case BUILDCOLUMN:
                buildTrain(row);
                break;
            case ROUTECOLUMN:
                editRoute(row);
                break;
            case ACTIONCOLUMN:
                actionTrain(row);
                break;
            case BUILDBOXCOLUMN: {
                Train train = sysList.get(row);
                train.setBuildEnabled(((Boolean) value).booleanValue());
                break;
            }
            default:
                break;
        }
    }

    public Color getRowColor(int row) {
        Train train = sysList.get(row);
        //  log.debug("Row: {} train: {} color: {}", row, train.getName(), train.getTableRowColorName());
        return train.getTableRowColor();
    }

    TrainEditFrame tef = null;

    private void editTrain(int row) {
        if (tef != null) {
            tef.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Train train = sysList.get(row);
                log.debug("Edit train ({})", train.getName());
                tef = new TrainEditFrame(train);
            }
        });
    }

    RouteEditFrame ref = null;

    private void editRoute(int row) {
        if (ref != null) {
            ref.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ref = new RouteEditFrame();
                Train train = sysList.get(row);
                log.debug("Edit route for train (" + train.getName() + ")");
                ref.initComponents(train.getRoute(), train);
            }
        });
    }

    Thread build;

    private void buildTrain(int row) {
        final Train train = sysList.get(row);
        if (!train.isBuilt()) {
            // only one train build at a time
            if (build != null && build.isAlive()) {
                return;
            }
            // use a thread to allow table updates during build
            build = new Thread(new Runnable() {
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
                    int result = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle.getMessage("TrainManifestFileMissing"),
                            new Object[]{train.getName()}), Bundle.getMessage("TrainManifestFileError"),
                            JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        train.setModified(true);
                        if (!train.printManifestIfBuilt()) {
                            log.error("Not able to create manifest for train ({})", train.getName());
                        }
                    }
                }
            }
        }
    }

    // one of four buttons: Report, Move, Conductor or Terminate
    private void actionTrain(int row) {
        // no actions while a train is being built
        if (build != null && build.isAlive()) {
            return;
        }
        Train train = sysList.get(row);
        // move button becomes report if failure
        if (train.getBuildFailed()) {
            train.printBuildReport();
        } else if (trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.RESET)) {
            log.debug("Reset train ({})", train.getName());
            // check to see if departure track was reused
            if (checkDepartureTrack(train)) {
                log.debug("Train is departing staging that already has inbound cars");
                JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("StagingTrackUsed"),
                        new Object[]{train.getDepartureTrack().getName()}), Bundle.getMessage("CanNotResetTrain"),
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (!train.reset()) {
                JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("TrainIsInRoute"),
                        new Object[]{train.getTrainTerminatesName()}), Bundle.getMessage("CanNotResetTrain"),
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (!train.isBuilt()) {
            JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle.getMessage("TrainNeedsBuild"),
                    new Object[]{train.getName()}), Bundle.getMessage("CanNotPerformAction"),
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (train.isBuilt() && trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.MOVE)) {
            log.debug("Move train ({})", train.getName());
            train.move();
        } else if (train.isBuilt() && trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.TERMINATE)) {
            log.debug("Terminate train ({})", train.getName());
            int status = JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle.getMessage("TerminateTrain"),
                    new Object[]{train.getName(), train.getDescription()}), MessageFormat.format(Bundle
                            .getMessage("DoYouWantToTermiate"), new Object[]{train.getName()}),
                    JOptionPane.YES_NO_OPTION);
            if (status == JOptionPane.YES_OPTION) {
                train.terminate();
            }
        } else if (train.isBuilt() && trainManager.getTrainsFrameTrainAction().equals(TrainsTableFrame.CONDUCTOR)) {
            log.debug("Enable conductor for train (" + train.getName() + ")");
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
                train.getDepartureTrack().getTrackType().equals(Track.STAGING) &&
                train.getDepartureTrack() != train.getTerminationTrack() &&
                train.getDepartureTrack().getDropRS() > 0);
    }

    private static Hashtable<String, TrainConductorFrame> _trainConductorHashTable =
            new Hashtable<String, TrainConductorFrame>();

    private void launchConductor(Train train) {
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TrainConductorFrame f = _trainConductorHashTable.get(train.getId());
                // create a copy train frame
                if (f == null || !f.isVisible()) {
                    f = new TrainConductorFrame(train);
                    _trainConductorHashTable.put(train.getId(), f);
                } else {
                    f.setExtendedState(Frame.NORMAL);
                }
                f.setVisible(true); // this also brings the frame into focus
            }
        });
    }

    @Override
 // removed synchronized from propertyChange, it caused a thread lock, see _table.scrollRectToVisible(_table.getCellRect(row, 0, true));
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change {} old: {} new: {}",
                    e.getPropertyName(), e.getOldValue(), e.getNewValue()); // NOI18N
        }
        if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(TrainManager.PRINTPREVIEW_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(TrainManager.OPEN_FILE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(TrainManager.RUN_FILE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Setup.MANIFEST_CSV_PROPERTY_CHANGE) ||
                e.getPropertyName().equals(TrainManager.TRAIN_ACTION_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.DEPARTURETIME_CHANGED_PROPERTY) ||
                (e.getPropertyName().equals(Train.BUILD_CHANGED_PROPERTY) && !isShowAll())) {
            updateList();
            fireTableDataChanged();
        } else if (e.getSource().getClass().equals(Train.class)) {
            Train train = ((Train) e.getSource());
            int row = sysList.indexOf(train);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update train table row: {} name: {}", row, train.getName());
            }
            if (row >= 0) {
                int viewRow = _table.convertRowIndexToView(row);
                // if there are issues with thread locking here, this needs to
                // be refactored so the panel holding the table is listening for
                // this changes so it can instruct the table to scroll
                // adding "synchronized" to this propertyChange can lock up thread                
                _table.scrollRectToVisible(_table.getCellRect(viewRow, 0, true));
                fireTableRowsUpdated(row, row);
            }
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
        Setup.removePropertyChangeListener(this);
        removePropertyChangeTrains();
    }

    class MyTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                int modelRow = table.convertRowIndexToModel(row);
                //    log.debug("View row: {} Column: {} Model row: {}", row, column, modelRow);
                Color background = getRowColor(modelRow);
                component.setBackground(background);
                component.setForeground(getForegroundColor(background));
            }
            return component;
        }

        Color[] darkColors = {Color.BLACK, Color.BLUE, Color.GRAY, Color.RED, Color.MAGENTA};

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

    private final static Logger log = LoggerFactory.getLogger(TrainsTableModel.class);
}
