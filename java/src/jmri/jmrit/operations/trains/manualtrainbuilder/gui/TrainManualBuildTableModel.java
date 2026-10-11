package jmri.jmrit.operations.trains.manualtrainbuilder.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.table.TableCellEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTableModel;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.manualtrainbuilder.TrainManualBuild;
import jmri.jmrit.operations.trains.manualtrainbuilder.TrainManualBuildItem;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Table Model for edit of a schedule used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2009, 2014
 */
public class TrainManualBuildTableModel extends OperationsTableModel implements PropertyChangeListener {

    // Defines the columns
    private static final int ID_COLUMN = 0;
    private static final int TYPE_COLUMN = ID_COLUMN + 1;
    private static final int ROAD_COLUMN = TYPE_COLUMN + 1;
    private static final int LOAD_COLUMN = ROAD_COLUMN + 1;
    private static final int LOCATION_COLUMN = LOAD_COLUMN + 1;
    private static final int LOC_TRACK_COLUMN = LOCATION_COLUMN + 1;
    private static final int DESTINATION_COLUMN = LOC_TRACK_COLUMN + 1;
    private static final int DEST_TRACK_COLUMN = DESTINATION_COLUMN + 1;
    private static final int PICKUP_DAY_COLUMN = DEST_TRACK_COLUMN + 1;
    private static final int COUNT_COLUMN = PICKUP_DAY_COLUMN + 1;
    private static final int WARN_COLUMN = COUNT_COLUMN + 1;
    private static final int FAIL_COLUMN = WARN_COLUMN + 1;
    private static final int REMOVE_COLUMN = FAIL_COLUMN + 1;
    private static final int UP_COLUMN = REMOVE_COLUMN + 1;
    private static final int DOWN_COLUMN = UP_COLUMN + 1;
    private static final int DELETE_COLUMN = DOWN_COLUMN + 1;

    private static final int HIGHEST_COLUMN = DELETE_COLUMN + 1;

    private static final String NONE = "";

    public TrainManualBuildTableModel() {
        super();
    }

    TrainManualBuild _manualBuild;
    TrainManualBuildEditFrame _frame;
    Train _train;

    private void updateList() {
        // first, remove listeners from the individual objects
        removePropertyChangeManualBuildItems();
        _list = _manualBuild.getItemsBySequenceList();
        // and add them back in
        for (TrainManualBuildItem mbi : _list) {
            mbi.addPropertyChangeListener(this);
            // covers the cases where destination or track is deleted
            if (mbi.getDestination() != null) {
                mbi.getDestination().addPropertyChangeListener(this);
            }
            if (mbi.getDestinationTrack() != null) {
                mbi.getDestinationTrack().addPropertyChangeListener(this);
            }
        }
    }

    List<TrainManualBuildItem> _list = new ArrayList<>();

    protected void initTable(TrainManualBuildEditFrame frame, JTable table, TrainManualBuild tmb) {
        super.initTable(table);
        _manualBuild = tmb;
        _frame = frame;
        _train = InstanceManager.getDefault(TrainManager.class).getTrainById(_manualBuild.getTrainId());

        // add property listeners
        _manualBuild.addPropertyChangeListener(this);
        initTable();
    }

    private void initTable() {
        // Use XTableColumnModel so we can control which columns are visible
        XTableColumnModel tcm = new XTableColumnModel();
        _table.setColumnModel(tcm);
        _table.createDefaultColumnsFromModel();

        // Install the button handlers
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        TableCellEditor buttonEditor = new ButtonEditor(new javax.swing.JButton());
        tcm.getColumn(UP_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(UP_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(DOWN_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(DOWN_COLUMN).setCellEditor(buttonEditor);
        tcm.getColumn(DELETE_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(DELETE_COLUMN).setCellEditor(buttonEditor);

        // set column preferred widths
        _table.getColumnModel().getColumn(ID_COLUMN).setPreferredWidth(35);
        _table.getColumnModel().getColumn(TYPE_COLUMN).setPreferredWidth(90);
        _table.getColumnModel().getColumn(ROAD_COLUMN).setPreferredWidth(90);
        _table.getColumnModel().getColumn(LOAD_COLUMN).setPreferredWidth(90);
        _table.getColumnModel().getColumn(LOCATION_COLUMN).setPreferredWidth(130);
        _table.getColumnModel().getColumn(LOC_TRACK_COLUMN).setPreferredWidth(130);
        _table.getColumnModel().getColumn(DESTINATION_COLUMN).setPreferredWidth(130);
        _table.getColumnModel().getColumn(DEST_TRACK_COLUMN).setPreferredWidth(130);
        _table.getColumnModel().getColumn(PICKUP_DAY_COLUMN).setPreferredWidth(90);
        _table.getColumnModel().getColumn(COUNT_COLUMN).setPreferredWidth(45);
        _table.getColumnModel().getColumn(WARN_COLUMN).setPreferredWidth(45);
        _table.getColumnModel().getColumn(FAIL_COLUMN).setPreferredWidth(45);
        _table.getColumnModel().getColumn(REMOVE_COLUMN).setPreferredWidth(60);
        _table.getColumnModel().getColumn(UP_COLUMN).setPreferredWidth(60);
        _table.getColumnModel().getColumn(DOWN_COLUMN).setPreferredWidth(70);
        _table.getColumnModel().getColumn(DELETE_COLUMN).setPreferredWidth(70);

        _frame.loadTableDetails(_table);

        // does not use a table sorter
        _table.setRowSorter(null);

        updateList();
    }

    @Override
    public int getRowCount() {
        return _list.size();
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
            case TYPE_COLUMN:
                return Bundle.getMessage("Type");
            case ROAD_COLUMN:
                return Bundle.getMessage("Road");
            case LOAD_COLUMN:
                return Bundle.getMessage("Load");
            case LOCATION_COLUMN:
                return Bundle.getMessage("Location");
            case LOC_TRACK_COLUMN:
                return Bundle.getMessage("Track");
            case DESTINATION_COLUMN:
                return Bundle.getMessage("Destination");
            case DEST_TRACK_COLUMN:
                return Bundle.getMessage("DestTrack");
            case PICKUP_DAY_COLUMN:
                return Bundle.getMessage("Pickup");
            case COUNT_COLUMN:
                return Bundle.getMessage("Count");
            case WARN_COLUMN:
                return Bundle.getMessage("Warn");
            case FAIL_COLUMN:
                return Bundle.getMessage("Fail");
            case REMOVE_COLUMN:
                return Bundle.getMessage("Remove");
            case UP_COLUMN:
                return Bundle.getMessage("Up");
            case DOWN_COLUMN:
                return Bundle.getMessage("Down");
            case DELETE_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            default:
                return "unknown"; // NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case ID_COLUMN:
                return String.class;
            case TYPE_COLUMN:
            case ROAD_COLUMN:
            case LOAD_COLUMN:
            case LOCATION_COLUMN:
            case LOC_TRACK_COLUMN:
            case DESTINATION_COLUMN:
            case DEST_TRACK_COLUMN:
            case PICKUP_DAY_COLUMN:
                return JComboBox.class;
            case COUNT_COLUMN:
                return Integer.class;
            case WARN_COLUMN:
            case FAIL_COLUMN:
            case REMOVE_COLUMN:
                return Boolean.class;
            case UP_COLUMN:
            case DOWN_COLUMN:
            case DELETE_COLUMN:
                return JButton.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case TYPE_COLUMN:
            case ROAD_COLUMN:
            case LOAD_COLUMN:
            case LOCATION_COLUMN:
            case LOC_TRACK_COLUMN:
            case DESTINATION_COLUMN:
            case DEST_TRACK_COLUMN:
            case PICKUP_DAY_COLUMN:
            case COUNT_COLUMN:
            case WARN_COLUMN:
            case FAIL_COLUMN:
            case REMOVE_COLUMN:
            case UP_COLUMN:
            case DOWN_COLUMN:
            case DELETE_COLUMN:
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
        TrainManualBuildItem mbi = _list.get(row);
        if (mbi == null) {
            return "ERROR schedule item unknown " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return mbi.getId();
            case TYPE_COLUMN:
                return getTypeComboBox(mbi);
            case ROAD_COLUMN:
                return getRoadComboBox(mbi);
            case LOAD_COLUMN:
                return getLoadComboBox(mbi);
            case LOCATION_COLUMN:
                return getRouteLocationComboBox(mbi);
            case LOC_TRACK_COLUMN:
                return getLocationTrackComboBox(mbi);
            case DESTINATION_COLUMN:
                return getDestinationComboBox(mbi);
            case DEST_TRACK_COLUMN:
                return getDestTrackComboBox(mbi);
            case PICKUP_DAY_COLUMN:
                return getPickupDayComboBox(mbi);
            case COUNT_COLUMN:
                return mbi.getCount();
            case WARN_COLUMN:
                return mbi.isWarnEnabled();
            case FAIL_COLUMN:
                return mbi.isFailEnabled();
            case REMOVE_COLUMN:
                return mbi.isRemoveEnabled();
            case UP_COLUMN:
                return Bundle.getMessage("Up");
            case DOWN_COLUMN:
                return Bundle.getMessage("Down");
            case DELETE_COLUMN:
                return Bundle.getMessage("ButtonDelete");
            default:
                return "unknown " + col; // NOI18N
        }
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (value == null) {
            log.debug("Warning schedule table row {} still in edit", row);
            return;
        }
        TrainManualBuildItem mbi = _list.get(row);
        switch (col) {
            case TYPE_COLUMN:
                setType(value, mbi);
                break;
            case ROAD_COLUMN:
                setRoad(value, mbi);
                break;
            case LOAD_COLUMN:
                setLoad(value, mbi);
                break;
            case LOCATION_COLUMN:
                setRouteLocation(value, mbi);
                break;
            case LOC_TRACK_COLUMN:
                setLocTrack(value, mbi);
                break;
            case DESTINATION_COLUMN:
                setDestination(value, mbi);
                break;
            case DEST_TRACK_COLUMN:
                setDestTrack(value, mbi);
                break;
            case PICKUP_DAY_COLUMN:
                setPickupDay(value, mbi);
                break;
            case COUNT_COLUMN:
                setCount(value, mbi);
                break;
            case WARN_COLUMN:
                setWarn(value, mbi);
                break;
            case FAIL_COLUMN:
                setFail(value, mbi);
                break;
            case REMOVE_COLUMN:
                mbi.setRemoveEnabled(((Boolean) value).booleanValue());
                break;
            case UP_COLUMN:
                moveUpManualBuildItem(row);
                break;
            case DOWN_COLUMN:
                moveDownManualBuildItem(row);
                break;
            case DELETE_COLUMN:
                deleteManualBuildItem(row);
                break;
            default:
                break;
        }
    }

    private JComboBox<String> getTypeComboBox(TrainManualBuildItem mbi) {
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem(NONE);
        for (String typeName : InstanceManager.getDefault(CarTypes.class).getNames()) {
            if (_train.isTypeNameAccepted(typeName)) {
                cb.addItem(typeName);
            }
        }
        cb.setSelectedItem(mbi.getTypeName());
        // fix if type no longer accepted by train
        if (!cb.getSelectedItem().equals(mbi.getTypeName())) {
            mbi.setTypeName(NONE);
        }
        return cb;
    }

    private JComboBox<String> getRoadComboBox(TrainManualBuildItem mbi) {
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem(NONE);
        for (String roadName : InstanceManager.getDefault(CarRoads.class).getNames(mbi.getTypeName())) {
            if (_train.isCarRoadNameAccepted(roadName)) {
                cb.addItem(roadName);
            }
        }
        cb.setSelectedItem(mbi.getRoadName());
        // fix if road no longer accepted by train
        if (!cb.getSelectedItem().equals(mbi.getRoadName())) {
            mbi.setRoadName(NONE);
        }
        return cb;
    }

    protected JComboBox<String> getLoadComboBox(TrainManualBuildItem mbi) {
        JComboBox<String> cb = InstanceManager.getDefault(CarLoads.class).getSelectComboBox(mbi.getTypeName());
        for (String loadName : InstanceManager.getDefault(CarLoads.class).getNames(mbi.getTypeName())) {
            if (!_train.isLoadNameAccepted(loadName)) {
                cb.removeItem(loadName);
            }
        }
        cb.setSelectedItem(mbi.getLoadName());
        // fix if load no longer accepted by train
        if (!cb.getSelectedItem().equals(mbi.getLoadName())) {
            mbi.setLoadName(NONE);
        }
        return cb;
    }

    protected JComboBox<RouteLocation> getRouteLocationComboBox(TrainManualBuildItem mbi) {
        JComboBox<RouteLocation> cb = new JComboBox<>();
        cb.addItem(null);
        Route route = _train.getRoute();
        for (RouteLocation rl : route.getLocationsBySequenceList()) {
            cb.addItem(rl);
        }
        filterRouteLocations(cb, mbi.getTypeName());
        cb.setSelectedItem(mbi.getRouteLocation());
        // fix if route location no longer exists
        if (cb.getSelectedItem() != mbi.getRouteLocation()) {
            mbi.setRouteLocation(null);
        }
        return cb;
    }

    protected JComboBox<Track> getLocationTrackComboBox(TrainManualBuildItem mbi) {
        // log.debug("getTrackComboBox for ManualBuildItem "+si.getType());
        JComboBox<Track> cb = new JComboBox<>();
        if (mbi.getRouteLocation() != null) {
            Location loc = mbi.getRouteLocation().getLocation();
            loc.updateComboBox(cb);
            filterTracks(loc, cb, mbi.getTypeName(), mbi.getRoadName(), mbi.getLoadName());
        }
        cb.setSelectedItem(mbi.getLocationTrack());
        // fix if track no longer exists
        if (cb.getSelectedItem() != mbi.getLocationTrack()) {
            mbi.setLocationTrack(null);
        }
        return cb;
    }

    protected JComboBox<Location> getDestinationComboBox(TrainManualBuildItem mbi) {
        JComboBox<Location> cb = InstanceManager.getDefault(LocationManager.class).getComboBox();
        filterLocations(cb, mbi.getTypeName());
        cb.setSelectedItem(mbi.getDestination());
        // fix if destination no longer exists
        if (cb.getSelectedItem() != mbi.getDestination()) {
            mbi.setDestination(null);
        }
        return cb;
    }

    protected JComboBox<Track> getDestTrackComboBox(TrainManualBuildItem mbi) {
        // log.debug("getTrackComboBox for ManualBuildItem "+si.getType());
        JComboBox<Track> cb = new JComboBox<>();
        if (mbi.getDestination() != null) {
            Location dest = mbi.getDestination();
            dest.updateComboBox(cb);
            filterTracks(dest, cb, mbi.getTypeName(), mbi.getRoadName(), mbi.getLoadName());
        }
        cb.setSelectedItem(mbi.getDestinationTrack());
        // fix if track no longer exists
        if (cb.getSelectedItem() != mbi.getDestinationTrack()) {
            mbi.setDestinationTrack(null);
        }
        return cb;
    }

    private JComboBox<TrainSchedule> getPickupDayComboBox(TrainManualBuildItem mbi) {
        JComboBox<TrainSchedule> cb = InstanceManager.getDefault(TrainScheduleManager.class).getSelectComboBox();
        TrainSchedule sch =
                InstanceManager.getDefault(TrainScheduleManager.class).getScheduleById(mbi.getTrainScheduleId());
        cb.setSelectedItem(sch);
        // fix if schedule no longer exists
        if (cb.getSelectedItem() != sch) {
            mbi.setTrainScheduleId(NONE);
        }
        return cb;
    }

    private void setType(Object value, TrainManualBuildItem mbi) {
        String type = (String) ((JComboBox<?>) value).getSelectedItem();
        mbi.setTypeName(type);
    }

    private void setRoad(Object value, TrainManualBuildItem mbi) {
        String road = (String) ((JComboBox<?>) value).getSelectedItem();
        mbi.setRoadName(road);
    }

    private void setLoad(Object value, TrainManualBuildItem mbi) {
        String load = (String) ((JComboBox<?>) value).getSelectedItem();
        mbi.setLoadName(load);
    }

    private void setRouteLocation(Object value, TrainManualBuildItem mbi) {
        mbi.setLocationTrack(null);
        RouteLocation rl = (RouteLocation) ((JComboBox<?>) value).getSelectedItem();
        mbi.setRouteLocation(rl);
    }

    private void setLocTrack(Object value, TrainManualBuildItem mbi) {
        Track track = (Track) ((JComboBox<?>) value).getSelectedItem();
        mbi.setLocationTrack(track);
    }

    private void setDestination(Object value, TrainManualBuildItem mbi) {
        mbi.setDestinationTrack(null);
        Location dest = (Location) ((JComboBox<?>) value).getSelectedItem();
        mbi.setDestination(dest);
    }

    private void setDestTrack(Object value, TrainManualBuildItem mbi) {
        Track track = (Track) ((JComboBox<?>) value).getSelectedItem();
        mbi.setDestinationTrack(track);
    }

    private void setPickupDay(Object value, TrainManualBuildItem mbi) {
        Object obj = ((JComboBox<?>) value).getSelectedItem();
        if (obj == null) {
            mbi.setTrainScheduleId(TrainManualBuildItem.NONE);
        } else if (obj.getClass().equals(TrainSchedule.class)) {
            mbi.setTrainScheduleId(((TrainSchedule) obj).getId());
        }
    }

    private void setCount(Object value, TrainManualBuildItem mbi) {
        mbi.setCount((int) value);
    }

    private void setWarn(Object value, TrainManualBuildItem mbi) {
        mbi.setWarnEnabled(((Boolean) value).booleanValue());
        if (mbi.isWarnEnabled()) {
            mbi.setFailEnabled(false);
        }
    }

    private void setFail(Object value, TrainManualBuildItem mbi) {
        mbi.setFailEnabled(((Boolean) value).booleanValue());
        if (mbi.isFailEnabled()) {
            mbi.setWarnEnabled(false);
        }
    }

    private void moveUpManualBuildItem(int row) {
        log.debug("move schedule item up");
        _manualBuild.moveItemUp(_list.get(row));
    }

    private void moveDownManualBuildItem(int row) {
        log.debug("move schedule item down");
        _manualBuild.moveItemDown(_list.get(row));
    }

    private void deleteManualBuildItem(int row) {
        log.debug("Delete schedule item");
        _manualBuild.deleteItem(_list.get(row));
    }

    // remove route locations that don't service the car's type
    private void filterRouteLocations(JComboBox<RouteLocation> cb, String carType) {
        for (int i = 1; i < cb.getItemCount(); i++) {
            RouteLocation rl = cb.getItemAt(i);
            if (!carType.equals(NONE) && !rl.getLocation().acceptsTypeName(carType)) {
                cb.removeItem(rl);
                i--;
            }
        }
    }

    // remove destinations that don't service the car's type
    private void filterLocations(JComboBox<Location> cb, String carType) {
        for (int i = 1; i < cb.getItemCount(); i++) {
            Location loc = cb.getItemAt(i);
            if (!carType.equals(NONE) && !loc.acceptsTypeName(carType)) {
                cb.removeItem(loc);
                i--;
            }
        }
    }

    // remove tracks that don't service the car's type, road, or load
    private void filterTracks(Location loc, JComboBox<Track> cb, String carType, String carRoad, String carLoad) {
        List<Track> tracks = loc.getTracksList();
        for (Track track : tracks) {
            if (track.isStaging() ||
                    (!carType.equals(NONE) && !track.isTypeNameAccepted(carType)) ||
                    (!carRoad.equals(NONE) && !track.isRoadNameAccepted(carRoad)) ||
                    (!carLoad.equals(NONE) && !track.isLoadNameAndCarTypeAccepted(carLoad, carType))) {
                cb.removeItem(track);
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(TrainManualBuild.LISTCHANGE_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        }
        if (e.getSource().getClass().equals(TrainManualBuildItem.class)) {
            TrainManualBuildItem item = (TrainManualBuildItem) e.getSource();
            int row = _list.indexOf(item);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update table row: {}", row);
            }
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

    private void removePropertyChangeManualBuildItems() {
        for (TrainManualBuildItem mbi : _list) {
            mbi.removePropertyChangeListener(this);
            if (mbi.getDestination() != null) {
                mbi.getDestination().removePropertyChangeListener(this);
            }
            if (mbi.getDestinationTrack() != null) {
                mbi.getDestinationTrack().removePropertyChangeListener(this);
            }
        }
    }

    public void dispose() {
        if (_manualBuild != null) {
            removePropertyChangeManualBuildItems();
            _manualBuild.removePropertyChangeListener(this);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(TrainManualBuildTableModel.class);
}
