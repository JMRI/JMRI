package jmri.jmrit.operations.locations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of locations used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2013
 */
public class LocationsTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    LocationManager locationManager; // There is only one manager
    protected JTable _table;

    // Define the columns
    public static final int ID_COLUMN = 0;
    public static final int NAME_COLUMN = 1;
    public static final int TRACK_COLUMN = 2;
    public static final int NUMBER_COLUMN = 3;
    public static final int LENGTH_COLUMN = 4;
    public static final int USED_LENGTH_COLUMN = 5;
    public static final int ROLLINGSTOCK_COLUMN = 6;
    public static final int PICKUPS_COLUMN = 7;
    public static final int DROPS_COLUMN = 8;
    public static final int REPORTER_COLUMN = 9;
    public static final int ACTION_COLUMN = 10;
    public static final int EDIT_COLUMN = 11;

    private static final int HIGHEST_COLUMN = EDIT_COLUMN + 1;

    public LocationsTableModel() {
        super();
        locationManager = InstanceManager.getDefault(LocationManager.class);
        locationManager.addPropertyChangeListener(this);
        updateList();
    }

    public final int SORTBYNAME = 1;
    public final int SORTBYID = 2;

    private int _sort = SORTBYNAME;

    public void setSort(int sort) {
        _sort = sort;
        updateList();
        fireTableDataChanged();
    }

    private void updateList() {
        // first, remove listeners from the individual objects
        removePropertyChangeLocations();

        if (_sort == SORTBYID) {
            locationsList = locationManager.getLocationsByIdList();
        } else {
            locationsList = locationManager.getLocationsByNameList();
        }
        // and add them back in
        for (Location loc : locationsList) {
            loc.addPropertyChangeListener(this);
        }
    }

    List<Location> locationsList = null;

    void initTable(LocationsTableFrame frame, JTable table) {
        _table = table;
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        table.setColumnModel(tcm);
        table.createDefaultColumnsFromModel();
        // Install the button handlers
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
        tcm.getColumn(ACTION_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(ACTION_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(EDIT_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(EDIT_COLUMN).setCellEditor(buttonEditor);

        // set column preferred widths
        table.getColumnModel().getColumn(ID_COLUMN).setPreferredWidth(40);
        table.getColumnModel().getColumn(NAME_COLUMN).setPreferredWidth(200);
        table.getColumnModel().getColumn(TRACK_COLUMN).setPreferredWidth(
                Math.max(60,
                        new JLabel(Bundle.getMessage("Class/Interchange") +
                                Bundle.getMessage("Spurs") +
                                Bundle.getMessage("Yards")).getPreferredSize().width + 20));
        table.getColumnModel().getColumn(NUMBER_COLUMN).setPreferredWidth(40);
        table.getColumnModel().getColumn(LENGTH_COLUMN).setPreferredWidth(
                Math.max(60, new JLabel(getColumnName(LENGTH_COLUMN)).getPreferredSize().width + 10));
        table.getColumnModel().getColumn(USED_LENGTH_COLUMN).setPreferredWidth(60);
        table.getColumnModel().getColumn(ROLLINGSTOCK_COLUMN).setPreferredWidth(
                Math.max(80, new JLabel(getColumnName(ROLLINGSTOCK_COLUMN)).getPreferredSize().width + 10));
        table.getColumnModel().getColumn(PICKUPS_COLUMN).setPreferredWidth(
                Math.max(60, new JLabel(getColumnName(PICKUPS_COLUMN)).getPreferredSize().width + 10));
        table.getColumnModel().getColumn(DROPS_COLUMN).setPreferredWidth(
                Math.max(60, new JLabel(getColumnName(DROPS_COLUMN)).getPreferredSize().width + 10));
        table.getColumnModel().getColumn(ACTION_COLUMN).setPreferredWidth(
                Math.max(80, new JLabel(Bundle.getMessage("Yardmaster")).getPreferredSize().width + 40));
        table.getColumnModel().getColumn(EDIT_COLUMN).setPreferredWidth(80);
        
        frame.loadTableDetails(table);
        setColumnsVisible();
    }
    
    protected void setColumnsVisible() {
        XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(REPORTER_COLUMN), Setup.isRfidEnabled() && locationManager.hasReporters());
    }

    @Override
    public int getRowCount() {
        return locationsList.size();
    }

    @Override
    public int getColumnCount() {
        return HIGHEST_COLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case ID_COLUMN:
                return Bundle.getMessage("Id");
            case NAME_COLUMN:
                return Bundle.getMessage("Name");
            case TRACK_COLUMN:
                return Bundle.getMessage("Track");
            case NUMBER_COLUMN:
                return Bundle.getMessage("Number");
            case LENGTH_COLUMN:
                return Bundle.getMessage("Length");
            case USED_LENGTH_COLUMN:
                return Bundle.getMessage("Used");
            case ROLLINGSTOCK_COLUMN:
                return Bundle.getMessage("RollingStock");
            case PICKUPS_COLUMN:
                return Bundle.getMessage("Pickups");
            case DROPS_COLUMN:
                return Bundle.getMessage("Drop");
            case REPORTER_COLUMN:
                return Bundle.getMessage("Reporters");
            case ACTION_COLUMN:
                return Bundle.getMessage("Action");
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit"); // titles above all columns
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case NAME_COLUMN:
            case TRACK_COLUMN:
            case REPORTER_COLUMN:
                return String.class;
            case ID_COLUMN:
            case NUMBER_COLUMN:
            case LENGTH_COLUMN:
            case USED_LENGTH_COLUMN:
            case ROLLINGSTOCK_COLUMN:
            case PICKUPS_COLUMN:
            case DROPS_COLUMN:
                return Integer.class;
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
            case EDIT_COLUMN:
            case ACTION_COLUMN:
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
        Location location = locationsList.get(row);
        if (location == null) {
            return "ERROR location unknown " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return Integer.parseInt(location.getId());
            case NAME_COLUMN:
                return location.getName();
            case TRACK_COLUMN:
                return getTrackTypes(location);
            case NUMBER_COLUMN:
                return location.getTracksList().size();
            case LENGTH_COLUMN:
                return location.getLength();
            case USED_LENGTH_COLUMN:
                return location.getUsedLength();
            case ROLLINGSTOCK_COLUMN:
                return location.getNumberRS();
            case PICKUPS_COLUMN:
                return location.getPickupRS();
            case DROPS_COLUMN:
                return location.getDropRS();
            case REPORTER_COLUMN:
                return location.getReporterName();
            case ACTION_COLUMN:
                return Bundle.getMessage("Yardmaster");
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    private String getTrackTypes(Location location) {
        if (location.isStaging()) {
            return (Bundle.getMessage("Staging"));
        } else {
            StringBuffer sb = new StringBuffer();
            if (location.hasInterchanges()) {
                sb.append(Bundle.getMessage("Class/Interchange") + " ");
            }
            if (location.hasSpurs()) {
                sb.append(Bundle.getMessage("Spurs") + " ");
            }
            if (location.hasYards()) {
                sb.append(Bundle.getMessage("Yards"));
            }
            return sb.toString();
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        switch (col) {
            case ACTION_COLUMN:
                launchYardmaster(row);
                break;
            case EDIT_COLUMN:
                editLocation(row);
                break;
            default:
                break;
        }
    }

    List<LocationEditFrame> frameList = new ArrayList<LocationEditFrame>();

    private void editLocation(int row) {
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(() -> {
            Location loc = locationsList.get(row);
            log.debug("Edit location ({})", loc.getName());
            for (LocationEditFrame lef : frameList) {
                if (lef._location == loc) {
                    lef.dispose();
                    frameList.remove(lef);
                    break;
                }
            }
            LocationEditFrame lef = new LocationEditFrame(loc);
            frameList.add(lef);
        });
    }

    private void launchYardmaster(int row) {
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(() -> {
            log.debug("Yardmaster");
            Location loc = locationsList.get(row);
            new YardmasterFrame(loc);
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        } else if (e.getSource().getClass().equals(Location.class)) {
            Location loc = (Location) e.getSource();
            int row = locationsList.indexOf(loc);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update location table row: {} name: {}", row, loc.getName());
            }
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            }
            if (e.getPropertyName().equals(Location.LOCATION_REPORTER_PROPERTY)) {
                setColumnsVisible();
            }
        }
    }

    private void removePropertyChangeLocations() {
        if (locationsList != null) {
            for (Location loc : locationsList) {
                loc.removePropertyChangeListener(this);
            }
        }
    }

    public void dispose() {
        for (LocationEditFrame lef : frameList) {
            lef.dispose();
        }
        locationManager.removePropertyChangeListener(this);
        removePropertyChangeLocations();
    }

    private final static Logger log = LoggerFactory.getLogger(LocationsTableModel.class);
}
