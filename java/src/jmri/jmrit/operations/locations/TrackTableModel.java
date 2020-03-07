package jmri.jmrit.operations.locations;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of tracks used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008, 2011, 2012
 */
public class TrackTableModel extends AbstractTableModel implements PropertyChangeListener {

    public static final int SORTBYNAME = 1;
    public static final int SORTBYID = 2;

    protected Location _location;
    protected List<Track> tracksList = new ArrayList<>();
    protected int _sort = SORTBYNAME;
    protected String _trackType = "";
    protected boolean _showPoolColumn = false;
    protected JTable _table;

    // Defines the columns
    protected static final int ID_COLUMN = 0;
    protected static final int NAME_COLUMN = 1;
    protected static final int LENGTH_COLUMN = 2;
    protected static final int USED_LENGTH_COLUMN = 3;
    protected static final int RESERVED_COLUMN = 4;
    protected static final int MOVES_COLUMN = 5;
    protected static final int CARS_COLUMN = 6;
    protected static final int LOCOS_COLUMN = 7;
    protected static final int PICKUPS_COLUMN = 8;
    protected static final int SETOUT_COLUMN = 9;
    protected static final int SCHEDULE_COLUMN = 10;
    protected static final int ROAD_COLUMN = 11;
    protected static final int LOAD_COLUMN = 12;
    protected static final int SHIP_COLUMN = 13;
    protected static final int RESTRICTION_COLUMN = 14;
    protected static final int DESTINATION_COLUMN = 15;
    protected static final int POOL_COLUMN = 16;
    protected static final int PLANPICKUP_COLUMN = 17;
    protected static final int ALT_TRACK_COLUMN = 18;
    protected static final int ORDER_COLUMN = 19;
    protected static final int EDIT_COLUMN = 20;

    protected static final int HIGHESTCOLUMN = EDIT_COLUMN + 1;

    public TrackTableModel() {
        super();
    }

    public void setSort(int sort) {
        _sort = sort;
        updateList();
        fireTableDataChanged();
    }

    private void updateList() {
        if (_location == null) {
            return;
        }
        // first, remove listeners from the individual objects
        removePropertyChangeTracks();

        tracksList = _location.getTrackByNameList(_trackType);
        // and add them back in
        for (Track track : tracksList) {
            track.addPropertyChangeListener(this);
        }
        if (_location.hasPools() && !_showPoolColumn) {
            _showPoolColumn = true;
            fireTableStructureChanged();
            initTable();
        }
    }

    protected void initTable(JTable table, Location location, String trackType) {
        _table = table;
        _location = location;
        _trackType = trackType;
        if (_location != null) {
            _location.addPropertyChangeListener(this);
        }
        Setup.addPropertyChangeListener(this);
        initTable();
        table.setRowHeight(new JComboBox<>().getPreferredSize().height);
        // have to shut off autoResizeMode to get horizontal scroll to work (JavaSwing p 541)
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        updateList();
    }

    private void initTable() {
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        _table.setColumnModel(tcm);
        _table.createDefaultColumnsFromModel();

        // set column preferred widths
        tcm.getColumn(ID_COLUMN).setPreferredWidth(40);
        tcm.getColumn(NAME_COLUMN).setPreferredWidth(200);
        tcm.getColumn(LENGTH_COLUMN).setPreferredWidth(
                Math.max(50, new JLabel(getColumnName(LENGTH_COLUMN)).getPreferredSize().width + 10));
        tcm.getColumn(USED_LENGTH_COLUMN).setPreferredWidth(50);
        tcm.getColumn(RESERVED_COLUMN).setPreferredWidth(
                Math.max(65, new JLabel(getColumnName(RESERVED_COLUMN)).getPreferredSize().width + 10));
        tcm.getColumn(MOVES_COLUMN).setPreferredWidth(60);
        tcm.getColumn(LOCOS_COLUMN).setPreferredWidth(60);
        tcm.getColumn(CARS_COLUMN).setPreferredWidth(60);
        tcm.getColumn(PICKUPS_COLUMN).setPreferredWidth(
                Math.max(60, new JLabel(getColumnName(PICKUPS_COLUMN)).getPreferredSize().width + 10));
        tcm.getColumn(SETOUT_COLUMN).setPreferredWidth(
                Math.max(60, new JLabel(getColumnName(SETOUT_COLUMN)).getPreferredSize().width + 10));
        tcm.getColumn(SCHEDULE_COLUMN).setPreferredWidth(
                Math.max(90, new JLabel(getColumnName(SCHEDULE_COLUMN)).getPreferredSize().width + 10));
        tcm.getColumn(RESTRICTION_COLUMN).setPreferredWidth(90);
        tcm.getColumn(LOAD_COLUMN).setPreferredWidth(50);
        tcm.getColumn(SHIP_COLUMN).setPreferredWidth(50);
        tcm.getColumn(ROAD_COLUMN).setPreferredWidth(50);
        tcm.getColumn(DESTINATION_COLUMN).setPreferredWidth(50);
        tcm.getColumn(POOL_COLUMN).setPreferredWidth(70);
        tcm.getColumn(PLANPICKUP_COLUMN).setPreferredWidth(70);
        tcm.getColumn(ALT_TRACK_COLUMN).setPreferredWidth(120);
        tcm.getColumn(ORDER_COLUMN)
                .setPreferredWidth(Math.max(50, new JLabel(getColumnName(ORDER_COLUMN)).getPreferredSize().width + 10));
        tcm.getColumn(EDIT_COLUMN).setPreferredWidth(80);
        tcm.getColumn(EDIT_COLUMN).setCellRenderer(new ButtonRenderer());
        tcm.getColumn(EDIT_COLUMN).setCellEditor(new ButtonEditor(new JButton()));

        setColumnsVisible();
    }

    // only show "Schedule", "Load", "Ship", "Road", "Destination", "Planned", "Pool" "Alternate" "Order" if they are needed
    protected void setColumnsVisible() {
        XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(SCHEDULE_COLUMN), _location.hasSchedules());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(RESTRICTION_COLUMN), _location.hasServiceRestrictions());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(LOAD_COLUMN), _location.hasLoadRestrictions());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(SHIP_COLUMN), _location.hasShipLoadRestrictions());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(ROAD_COLUMN), _location.hasRoadRestrictions());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(DESTINATION_COLUMN), _location.hasDestinationRestrictions());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(PLANPICKUP_COLUMN), _location.hasPlannedPickups());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(POOL_COLUMN), _location.hasPools());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(ALT_TRACK_COLUMN), _location.hasAlternateTracks());
        tcm.setColumnVisible(tcm.getColumnByModelIndex(ORDER_COLUMN), _location.hasOrderRestrictions());

        tcm.setColumnVisible(tcm.getColumnByModelIndex(MOVES_COLUMN), Setup.isShowTrackMovesEnabled());
    }

    @Override
    public int getRowCount() {
        return tracksList.size();
    }

    @Override
    public int getColumnCount() {
        return HIGHESTCOLUMN;
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case ID_COLUMN:
                return Bundle.getMessage("Id");
            case NAME_COLUMN:
                return Bundle.getMessage("TrackName");
            case LENGTH_COLUMN:
                return Bundle.getMessage("Length");
            case USED_LENGTH_COLUMN:
                return Bundle.getMessage("Used");
            case RESERVED_COLUMN:
                return Bundle.getMessage("Reserved");
            case MOVES_COLUMN:
                return Bundle.getMessage("Moves");
            case LOCOS_COLUMN:
                return Bundle.getMessage("Engines");
            case CARS_COLUMN:
                return Bundle.getMessage("Cars");
            case PICKUPS_COLUMN:
                return Bundle.getMessage("Pickups");
            case SETOUT_COLUMN:
                return Bundle.getMessage("Drop");
            case SCHEDULE_COLUMN:
                return Bundle.getMessage("Schedule");
            case RESTRICTION_COLUMN:
                return Bundle.getMessage("Restrictions");
            case LOAD_COLUMN:
                return Bundle.getMessage("Load");
            case SHIP_COLUMN:
                return Bundle.getMessage("Ship");
            case ROAD_COLUMN:
                return Bundle.getMessage("Road");
            case DESTINATION_COLUMN:
                return Bundle.getMessage("Dest");
            case POOL_COLUMN:
                return Bundle.getMessage("Pool");
            case PLANPICKUP_COLUMN:
                return Bundle.getMessage("PlanPickUp");
            case ALT_TRACK_COLUMN:
                return Bundle.getMessage("AlternateTrack");
            case ORDER_COLUMN:
                return Bundle.getMessage("ServiceOrder");
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit"); // titles above all columns
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case ID_COLUMN:
                return String.class;
            case NAME_COLUMN:
                return String.class;
            case LENGTH_COLUMN:
                return String.class;
            case USED_LENGTH_COLUMN:
                return String.class;
            case RESERVED_COLUMN:
                return String.class;
            case MOVES_COLUMN:
                return String.class;
            case LOCOS_COLUMN:
                return String.class;
            case CARS_COLUMN:
                return String.class;
            case PICKUPS_COLUMN:
                return String.class;
            case SETOUT_COLUMN:
                return String.class;
            case SCHEDULE_COLUMN:
                return String.class;
            case RESTRICTION_COLUMN:
                return String.class;
            case LOAD_COLUMN:
                return String.class;
            case SHIP_COLUMN:
                return String.class;
            case ROAD_COLUMN:
                return String.class;
            case DESTINATION_COLUMN:
                return String.class;
            case POOL_COLUMN:
                return String.class;
            case PLANPICKUP_COLUMN:
                return String.class;
            case ALT_TRACK_COLUMN:
                return String.class;
            case ORDER_COLUMN:
                return String.class;
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
            case MOVES_COLUMN:
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
        Track track = tracksList.get(row);
        if (track == null) {
            return "ERROR track unknown " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return track.getId();
            case NAME_COLUMN:
                return track.getName();
            case LENGTH_COLUMN:
                return Integer.toString(track.getLength());
            case USED_LENGTH_COLUMN:
                return Integer.toString(track.getUsedLength());
            case RESERVED_COLUMN:
                return Integer.toString(track.getReserved());
            case MOVES_COLUMN:
                return track.getMoves();
            case LOCOS_COLUMN:
                return Integer.toString(track.getNumberEngines());
            case CARS_COLUMN:
                return Integer.toString(track.getNumberCars());
            case PICKUPS_COLUMN:
                return Integer.toString(track.getPickupRS());
            case SETOUT_COLUMN:
                return Integer.toString(track.getDropRS());
            case SCHEDULE_COLUMN:
                return track.getScheduleName();
            case RESTRICTION_COLUMN:
                return getRestrictions(track);
            case LOAD_COLUMN:
                return getModifiedString(track.getLoadNames().length, track.getLoadOption().equals(Track.ALL_LOADS),
                        track
                                .getLoadOption().equals(Track.INCLUDE_LOADS)) +
                        (track.isSpur() && track.isHoldCarsWithCustomLoadsEnabled() ? " H"
                                : "");
            case SHIP_COLUMN:
                return getModifiedString(track.getShipLoadNames().length,
                        track.getShipLoadOption().equals(Track.ALL_LOADS), track.getShipLoadOption().equals(
                                Track.INCLUDE_LOADS));
            case ROAD_COLUMN:
                return getModifiedString(track.getRoadNames().length, track.getRoadOption().equals(Track.ALL_ROADS),
                        track
                                .getRoadOption().equals(Track.INCLUDE_ROADS));
            case DESTINATION_COLUMN: {
                int size = track.getDestinationListSize();
                if (track.getDestinationOption().equals(Track.EXCLUDE_DESTINATIONS)) {
                    size = InstanceManager.getDefault(LocationManager.class).getNumberOfLocations() - size;
                }
                return getModifiedString(size, track.getDestinationOption().equals(Track.ALL_DESTINATIONS), track
                        .getDestinationOption().equals(Track.INCLUDE_DESTINATIONS));
            }
            case POOL_COLUMN:
                return track.getPoolName();
            case PLANPICKUP_COLUMN:
                if (track.getIgnoreUsedLengthPercentage() > 0) {
                    return track.getIgnoreUsedLengthPercentage() + "%";
                }
                return "";
            case ALT_TRACK_COLUMN:
                if (track.getAlternateTrack() != null) {
                    return track.getAlternateTrack().getName();
                }
                if (track.isAlternate()) {
                    return Bundle.getMessage("ButtonYes");
                }
                return "";
            case ORDER_COLUMN:
                return track.getServiceOrder();
            case EDIT_COLUMN:
                return Bundle.getMessage("ButtonEdit");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    private String getRestrictions(Track track) {
        StringBuffer restrictions = new StringBuffer();
        if (!track.getDropOption().equals(Track.ANY)) {
            String suffix = " ";
            if (track.getDropOption().equals(Track.EXCLUDE_ROUTES) ||
                    track.getDropOption().equals(Track.EXCLUDE_TRAINS)) {
                suffix = "x";
            }
            restrictions.append(Bundle.getMessage("AbbreviationSetOut") + suffix + track.getDropIds().length);
        }
        if (!track.getPickupOption().equals(Track.ANY)) {
            String suffix = " ";
            if (track.getPickupOption().equals(Track.EXCLUDE_ROUTES) ||
                    track.getPickupOption().equals(Track.EXCLUDE_TRAINS)) {
                suffix = "x";
            }
            restrictions.append(" " + Bundle.getMessage("AbbreviationPickUp") + suffix + track.getPickupIds().length);
        }
        return restrictions.toString();
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
                editTrack(row);
                break;
            case MOVES_COLUMN:
                setMoves(row, value);
                break;
            default:
                break;
        }
    }

    TrackEditFrame tef = null;

    protected void editTrack(int row) {
        log.debug("Edit tracks");
        if (tef != null) {
            tef.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(() -> {
            tef = new TrackEditFrame();
            Track track = tracksList.get(row);
            tef.initComponents(_location, track);
            tef.setTitle(Bundle.getMessage("EditTrack"));
        });
    }

    protected void setMoves(int row, Object value) {
        Track track = tracksList.get(row);
        try {
            track.setMoves((Integer.parseInt((String) value)));
        } catch (NumberFormatException e) {
            log.error("Moves ({}) for track ({}) not a number", value, track.getName());
        }
    }

    // this table listens for changes to a location and it's tracks
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        }
        if (e.getPropertyName().equals(Setup.SHOW_TRACK_MOVES_PROPERTY_CHANGE)) {
            setColumnsVisible();
        }
        if (e.getSource().getClass().equals(Track.class) &&
                (e.getPropertyName().equals(Track.DROP_CHANGED_PROPERTY) ||
                        e.getPropertyName().equals(Track.PICKUP_CHANGED_PROPERTY) ||
                        e.getPropertyName().equals(Track.SCHEDULE_ID_CHANGED_PROPERTY) ||
                        e.getPropertyName().equals(Track.LOADS_CHANGED_PROPERTY) ||
                        e.getPropertyName().equals(Track.ROADS_CHANGED_PROPERTY) ||
                        e.getPropertyName().equals(Track.DESTINATION_OPTIONS_CHANGED_PROPERTY) ||
                        e.getPropertyName().equals(Track.POOL_CHANGED_PROPERTY) ||
                        e.getPropertyName().equals(Track.PLANNEDPICKUPS_CHANGED_PROPERTY) ||
                        e.getPropertyName().equals(Track.ALTERNATE_TRACK_CHANGED_PROPERTY) ||
                        e.getPropertyName().equals(Track.SERVICE_ORDER_CHANGED_PROPERTY))) {
            setColumnsVisible();
        }
    }

    protected void removePropertyChangeTracks() {
        for (Track t : tracksList) {
            t.removePropertyChangeListener(this);
        }
    }

    public void dispose() {
        removePropertyChangeTracks();
        if (_location != null) {
            _location.removePropertyChangeListener(this);
        }
        if (tef != null) {
            tef.dispose();
        }
        Setup.removePropertyChangeListener(this);
        tracksList.clear();
        fireTableDataChanged();
    }

    private final static Logger log = LoggerFactory.getLogger(TrackTableModel.class);
}
