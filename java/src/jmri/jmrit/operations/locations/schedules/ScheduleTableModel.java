package jmri.jmrit.operations.locations.schedules;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Control;
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
public class ScheduleTableModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    // Defines the columns
    private static final int ID_COLUMN = 0;
    private static final int CURRENT_COLUMN = ID_COLUMN + 1;
    private static final int TYPE_COLUMN = CURRENT_COLUMN + 1;
    private static final int RANDOM_COLUMN = TYPE_COLUMN + 1;
    private static final int SETOUT_DAY_COLUMN = RANDOM_COLUMN + 1;
    private static final int ROAD_COLUMN = SETOUT_DAY_COLUMN + 1;
    private static final int LOAD_COLUMN = ROAD_COLUMN + 1;
    private static final int SHIP_COLUMN = LOAD_COLUMN + 1;
    private static final int DEST_COLUMN = SHIP_COLUMN + 1;
    private static final int TRACK_COLUMN = DEST_COLUMN + 1;
    private static final int PICKUP_DAY_COLUMN = TRACK_COLUMN + 1;
    private static final int COUNT_COLUMN = PICKUP_DAY_COLUMN + 1;
    private static final int HIT_COLUMN = COUNT_COLUMN + 1;
    private static final int WAIT_COLUMN = HIT_COLUMN + 1;
    private static final int UP_COLUMN = WAIT_COLUMN + 1;
    private static final int DOWN_COLUMN = UP_COLUMN + 1;
    private static final int DELETE_COLUMN = DOWN_COLUMN + 1;

    private static final int HIGHEST_COLUMN = DELETE_COLUMN + 1;

    public ScheduleTableModel() {
        super();
    }

    Schedule _schedule;
    Location _location;
    Track _track;
    JTable _table;
    ScheduleEditFrame _frame;
    boolean _matchMode = false;

    private void updateList() {
        if (_schedule == null) {
            return;
        }
        // first, remove listeners from the individual objects
        removePropertyChangeScheduleItems();
        _list = _schedule.getItemsBySequenceList();
        // and add them back in
        for (ScheduleItem si : _list) {
            si.addPropertyChangeListener(this);
            // TODO the following two property changes could be moved to ScheduleItem
            // covers the cases where destination or track is deleted
            if (si.getDestination() != null) {
                si.getDestination().addPropertyChangeListener(this);
            }
            if (si.getDestinationTrack() != null) {
                si.getDestinationTrack().addPropertyChangeListener(this);
            }
        }
    }

    List<ScheduleItem> _list = new ArrayList<>();

    protected void initTable(ScheduleEditFrame frame, JTable table, Schedule schedule, Location location, Track track) {
        _schedule = schedule;
        _location = location;
        _track = track;
        _table = table;
        _frame = frame;

        // add property listeners
        if (_schedule != null) {
            _schedule.addPropertyChangeListener(this);
        }
        _location.addPropertyChangeListener(this);
        _track.addPropertyChangeListener(this);
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
        _table.setDefaultRenderer(JComboBox.class, new jmri.jmrit.symbolicprog.ValueRenderer());
        _table.setDefaultEditor(JComboBox.class, new jmri.jmrit.symbolicprog.ValueEditor());

        // set column preferred widths
        _table.getColumnModel().getColumn(ID_COLUMN).setPreferredWidth(35);
        _table.getColumnModel().getColumn(CURRENT_COLUMN).setPreferredWidth(50);
        _table.getColumnModel().getColumn(TYPE_COLUMN).setPreferredWidth(90);
        _table.getColumnModel().getColumn(RANDOM_COLUMN).setPreferredWidth(60);
        _table.getColumnModel().getColumn(SETOUT_DAY_COLUMN).setPreferredWidth(90);
        _table.getColumnModel().getColumn(ROAD_COLUMN).setPreferredWidth(90);
        _table.getColumnModel().getColumn(LOAD_COLUMN).setPreferredWidth(90);
        _table.getColumnModel().getColumn(SHIP_COLUMN).setPreferredWidth(90);
        _table.getColumnModel().getColumn(DEST_COLUMN).setPreferredWidth(130);
        _table.getColumnModel().getColumn(TRACK_COLUMN).setPreferredWidth(130);
        _table.getColumnModel().getColumn(PICKUP_DAY_COLUMN).setPreferredWidth(90);
        _table.getColumnModel().getColumn(COUNT_COLUMN).setPreferredWidth(45);
        _table.getColumnModel().getColumn(HIT_COLUMN).setPreferredWidth(45);
        _table.getColumnModel().getColumn(WAIT_COLUMN).setPreferredWidth(40);
        _table.getColumnModel().getColumn(UP_COLUMN).setPreferredWidth(60);
        _table.getColumnModel().getColumn(DOWN_COLUMN).setPreferredWidth(70);
        _table.getColumnModel().getColumn(DELETE_COLUMN).setPreferredWidth(70);

        _frame.loadTableDetails(_table);
        // turn off columns
        tcm.setColumnVisible(tcm.getColumnByModelIndex(HIT_COLUMN), false);

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
            case CURRENT_COLUMN:
                return Bundle.getMessage("Current");
            case TYPE_COLUMN:
                return Bundle.getMessage("Type");
            case RANDOM_COLUMN:
                return Bundle.getMessage("Random");
            case SETOUT_DAY_COLUMN:
                return Bundle.getMessage("Delivery");
            case ROAD_COLUMN:
                return Bundle.getMessage("Road");
            case LOAD_COLUMN:
                return Bundle.getMessage("Receive");
            case SHIP_COLUMN:
                return Bundle.getMessage("Ship");
            case DEST_COLUMN:
                return Bundle.getMessage("Destination");
            case TRACK_COLUMN:
                return Bundle.getMessage("Track");
            case PICKUP_DAY_COLUMN:
                return Bundle.getMessage("Pickup");
            case COUNT_COLUMN:
                return Bundle.getMessage("Count");
            case HIT_COLUMN:
                return Bundle.getMessage("Hits");
            case WAIT_COLUMN:
                return Bundle.getMessage("Wait");
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
            case CURRENT_COLUMN:
            case TYPE_COLUMN:
                return String.class;
            case RANDOM_COLUMN:
            case SETOUT_DAY_COLUMN:
            case ROAD_COLUMN:
            case LOAD_COLUMN:
            case SHIP_COLUMN:
            case DEST_COLUMN:
            case TRACK_COLUMN:
            case PICKUP_DAY_COLUMN:
                return JComboBox.class;
            case COUNT_COLUMN:
            case HIT_COLUMN:
            case WAIT_COLUMN:
                return Integer.class;
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
            case RANDOM_COLUMN:
            case SETOUT_DAY_COLUMN:
            case ROAD_COLUMN:
            case LOAD_COLUMN:
            case SHIP_COLUMN:
            case DEST_COLUMN:
            case TRACK_COLUMN:
            case PICKUP_DAY_COLUMN:
            case COUNT_COLUMN:
            case HIT_COLUMN:
            case WAIT_COLUMN:
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
        ScheduleItem si = _list.get(row);
        if (si == null) {
            return "ERROR schedule item unknown " + row; // NOI18N
        }
        switch (col) {
            case ID_COLUMN:
                return si.getId();
            case CURRENT_COLUMN:
                return getCurrentPointer(si);
            case TYPE_COLUMN:
                return getType(si);
            case RANDOM_COLUMN:
                return getRandomComboBox(si);
            case SETOUT_DAY_COLUMN:
                return getSetoutDayComboBox(si);
            case ROAD_COLUMN:
                return getRoadComboBox(si);
            case LOAD_COLUMN:
                return getLoadComboBox(si);
            case SHIP_COLUMN:
                return getShipComboBox(si);
            case DEST_COLUMN:
                return getDestComboBox(si);
            case TRACK_COLUMN:
                return getTrackComboBox(si);
            case PICKUP_DAY_COLUMN:
                return getPickupDayComboBox(si);
            case COUNT_COLUMN:
                return si.getCount();
            case HIT_COLUMN:
                return si.getHits();
            case WAIT_COLUMN:
                return si.getWait();
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
        switch (col) {
            case RANDOM_COLUMN:
                setRandom(value, row);
                break;
            case SETOUT_DAY_COLUMN:
                setSetoutDay(value, row);
                break;
            case ROAD_COLUMN:
                setRoad(value, row);
                break;
            case LOAD_COLUMN:
                setLoad(value, row);
                break;
            case SHIP_COLUMN:
                setShip(value, row);
                break;
            case DEST_COLUMN:
                setDestination(value, row);
                break;
            case TRACK_COLUMN:
                setTrack(value, row);
                break;
            case PICKUP_DAY_COLUMN:
                setPickupDay(value, row);
                break;
            case COUNT_COLUMN:
                setCount(value, row);
                break;
            case HIT_COLUMN:
                setHit(value, row);
                break;
            case WAIT_COLUMN:
                setWait(value, row);
                break;
            case UP_COLUMN:
                moveUpScheduleItem(row);
                break;
            case DOWN_COLUMN:
                moveDownScheduleItem(row);
                break;
            case DELETE_COLUMN:
                deleteScheduleItem(row);
                break;
            default:
                break;
        }
    }

    private String getCurrentPointer(ScheduleItem si) {
        if (_track.getCurrentScheduleItem() == si) {
            if (_track.getScheduleMode() == Track.SEQUENTIAL && si.getCount() > 1) {
                return " " + _track.getScheduleCount() + " -->"; // NOI18N
            } else {
                return "    -->"; // NOI18N
            }
        } else {
            return "";
        }
    }

    private String getType(ScheduleItem si) {
        if (_track.acceptsTypeName(si.getTypeName())) {
            return si.getTypeName();
        } else {
            return MessageFormat.format(Bundle.getMessage("NotValid"), new Object[]{si.getTypeName()});
        }
    }

    private JComboBox<String> getRoadComboBox(ScheduleItem si) {
        // log.debug("getRoadComboBox for ScheduleItem "+si.getType());
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem(ScheduleItem.NONE);
        for (String roadName : InstanceManager.getDefault(CarRoads.class).getNames()) {
            if (_track.acceptsRoadName(roadName)) {
                Car car = InstanceManager.getDefault(CarManager.class).getByTypeAndRoad(si.getTypeName(), roadName);
                if (car != null) {
                    cb.addItem(roadName);
                }
            }
        }
        cb.setSelectedItem(si.getRoadName());
        if (!cb.getSelectedItem().equals(si.getRoadName())) {
            String notValid = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[]{si.getRoadName()});
            cb.addItem(notValid);
            cb.setSelectedItem(notValid);
        }
        return cb;
    }

    String[] randomValues = {ScheduleItem.NONE, "50", "30", "25", "20", "15", "10", "5", "2", "1"}; // NOI18N

    private JComboBox<String> getRandomComboBox(ScheduleItem si) {
        JComboBox<String> cb = new JComboBox<>();
        for (String item : randomValues) {
            cb.addItem(item);
        }
        cb.setSelectedItem(si.getRandom());
        return cb;
    }

    private JComboBox<TrainSchedule> getSetoutDayComboBox(ScheduleItem si) {
        JComboBox<TrainSchedule> cb = InstanceManager.getDefault(TrainScheduleManager.class).getSelectComboBox();
        TrainSchedule sch =
                InstanceManager.getDefault(TrainScheduleManager.class).getScheduleById(si.getSetoutTrainScheduleId());
        if (sch != null) {
            cb.setSelectedItem(sch);
        } else if (!si.getSetoutTrainScheduleId().equals(ScheduleItem.NONE)) {
            // error user deleted this set out day
            String notValid = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[]{si
                    .getSetoutTrainScheduleId()});
            TrainSchedule errorSchedule = new TrainSchedule(si.getSetoutTrainScheduleId(), notValid);
            cb.addItem(errorSchedule);
            cb.setSelectedItem(errorSchedule);
        }
        return cb;
    }

    private JComboBox<TrainSchedule> getPickupDayComboBox(ScheduleItem si) {
        JComboBox<TrainSchedule> cb = InstanceManager.getDefault(TrainScheduleManager.class).getSelectComboBox();
        TrainSchedule sch =
                InstanceManager.getDefault(TrainScheduleManager.class).getScheduleById(si.getPickupTrainScheduleId());
        if (sch != null) {
            cb.setSelectedItem(sch);
        } else if (!si.getPickupTrainScheduleId().equals(ScheduleItem.NONE)) {
            // error user deleted this pick up day
            String notValid = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[]{si
                    .getPickupTrainScheduleId()});
            TrainSchedule errorSchedule = new TrainSchedule(si.getSetoutTrainScheduleId(), notValid);
            cb.addItem(errorSchedule);
            cb.setSelectedItem(errorSchedule);
        }
        return cb;
    }

    private JComboBox<String> getLoadComboBox(ScheduleItem si) {
        // log.debug("getLoadComboBox for ScheduleItem "+si.getType());
        JComboBox<String> cb = InstanceManager.getDefault(CarLoads.class).getSelectComboBox(si.getTypeName());
        filterLoads(si, cb); // remove loads not accepted by this track
        cb.setSelectedItem(si.getReceiveLoadName());
        if (!cb.getSelectedItem().equals(si.getReceiveLoadName())) {
            String notValid = MessageFormat.format(Bundle.getMessage("NotValid"), new Object[]{si
                    .getReceiveLoadName()});
            cb.addItem(notValid);
            cb.setSelectedItem(notValid);
        }
        return cb;
    }

    private JComboBox<String> getShipComboBox(ScheduleItem si) {
        // log.debug("getShipComboBox for ScheduleItem "+si.getType());
        JComboBox<String> cb = InstanceManager.getDefault(CarLoads.class).getSelectComboBox(si.getTypeName());
        cb.setSelectedItem(si.getShipLoadName());
        if (!cb.getSelectedItem().equals(si.getShipLoadName())) {
            String notValid = MessageFormat
                    .format(Bundle.getMessage("NotValid"), new Object[]{si.getShipLoadName()});
            cb.addItem(notValid);
            cb.setSelectedItem(notValid);
        }
        return cb;
    }

    private JComboBox<Location> getDestComboBox(ScheduleItem si) {
        // log.debug("getDestComboBox for ScheduleItem "+si.getType());
        JComboBox<Location> cb = InstanceManager.getDefault(LocationManager.class).getComboBox();
        filterDestinations(cb, si.getTypeName());
        cb.setSelectedItem(si.getDestination());
        if (si.getDestination() != null && cb.getSelectedIndex() == -1) {
            // user deleted destination, this is self correcting, when user restarts program, destination
            // assignment will be gone.
            cb.addItem(si.getDestination());
            cb.setSelectedItem(si.getDestination());
        }
        return cb;
    }

    private JComboBox<Track> getTrackComboBox(ScheduleItem si) {
        // log.debug("getTrackComboBox for ScheduleItem "+si.getType());
        JComboBox<Track> cb = new JComboBox<>();
        if (si.getDestination() != null) {
            Location dest = si.getDestination();
            dest.updateComboBox(cb);
            filterTracks(dest, cb, si.getTypeName(), si.getRoadName(), si.getShipLoadName());
            cb.setSelectedItem(si.getDestinationTrack());
            if (si.getDestinationTrack() != null && cb.getSelectedIndex() == -1) {
                // user deleted track at destination, this is self correcting, when user restarts program, track
                // assignment will be gone.
                cb.addItem(si.getDestinationTrack());
                cb.setSelectedItem(si.getDestinationTrack());
            }
        }
        return cb;
    }

    // set the count or hits if in match mode
    private void setCount(Object value, int row) {
        ScheduleItem si = _list.get(row);
        int count;
        try {
            count = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.error("Schedule count must be a number");
            return;
        }
        if (count < 1) {
            log.error("Schedule count must be greater than 0");
            return;
        }
        if (count > 100) {
            log.warn("Schedule count must be 100 or less");
            count = 100;
        }
        si.setCount(count);
    }

    // set the count or hits if in match mode
    private void setHit(Object value, int row) {
        ScheduleItem si = _list.get(row);
        int count;
        try {
            count = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.error("Schedule hits must be a number");
            return;
        }
        // we don't care what value the user sets the hit count
        si.setHits(count);
    }

    private void setWait(Object value, int row) {
        ScheduleItem si = _list.get(row);
        int wait;
        try {
            wait = Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.error("Schedule wait must be a number");
            return;
        }
        if (wait < 0) {
            log.error("Schedule wait must be a positive number");
            return;
        }
        if (wait > 10) {
            log.warn("Schedule wait must be 10 or less");
            wait = 10;
        }
        si.setWait(wait);
    }

    private void setRandom(Object value, int row) {
        ScheduleItem si = _list.get(row);
        String random = (String) ((JComboBox<?>) value).getSelectedItem();
        si.setRandom(random);

    }

    private void setSetoutDay(Object value, int row) {
        ScheduleItem si = _list.get(row);
        Object obj = ((JComboBox<?>) value).getSelectedItem();
        if (obj == null) {
            si.setSetoutTrainScheduleId(ScheduleItem.NONE);
        } else if (obj.getClass().equals(TrainSchedule.class)) {
            si.setSetoutTrainScheduleId(((TrainSchedule) obj).getId());
        }
    }

    private void setPickupDay(Object value, int row) {
        ScheduleItem si = _list.get(row);
        Object obj = ((JComboBox<?>) value).getSelectedItem();
        if (obj == null) {
            si.setPickupTrainScheduleId(ScheduleItem.NONE);
        } else if (obj.getClass().equals(TrainSchedule.class)) {
            si.setPickupTrainScheduleId(((TrainSchedule) obj).getId());
        }
    }

    // note this method looks for String "Not Valid <>"
    private void setRoad(Object value, int row) {
        ScheduleItem si = _list.get(row);
        String road = (String) ((JComboBox<?>) value).getSelectedItem();
        if (checkForNotValidString(road)) {
            si.setRoadName(road);
        }
    }

    // note this method looks for String "Not Valid <>"
    private void setLoad(Object value, int row) {
        ScheduleItem si = _list.get(row);
        String load = (String) ((JComboBox<?>) value).getSelectedItem();
        if (checkForNotValidString(load)) {
            si.setReceiveLoadName(load);
        }
    }

    // note this method looks for String "Not Valid <>"
    private void setShip(Object value, int row) {
        ScheduleItem si = _list.get(row);
        String load = (String) ((JComboBox<?>) value).getSelectedItem();
        if (checkForNotValidString(load)) {
            si.setShipLoadName(load);
        }
    }

    /*
     * Returns true if string is okay, doesn't have the string "Not Valid <>".
     */
    private boolean checkForNotValidString(String s) {
        if (s.length() < 12) {
            return true;
        }
        String test = s.substring(0, 11);
        if (test.equals(Bundle.getMessage("NotValid").substring(0, 11))) {
            return false;
        }
        return true;
    }

    private void setDestination(Object value, int row) {
        ScheduleItem si = _list.get(row);
        si.setDestinationTrack(null);
        Location dest = (Location) ((JComboBox<?>) value).getSelectedItem();
        si.setDestination(dest);
        fireTableCellUpdated(row, TRACK_COLUMN);
    }

    private void setTrack(Object value, int row) {
        ScheduleItem si = _list.get(row);
        Track track = (Track) ((JComboBox<?>) value).getSelectedItem();
        si.setDestinationTrack(track);
    }

    private void moveUpScheduleItem(int row) {
        log.debug("move schedule item up");
        _schedule.moveItemUp(_list.get(row));
    }

    private void moveDownScheduleItem(int row) {
        log.debug("move schedule item down");
        _schedule.moveItemDown(_list.get(row));
    }

    private void deleteScheduleItem(int row) {
        log.debug("Delete schedule item");
        _schedule.deleteItem(_list.get(row));
    }

    // remove destinations that don't service the car's type
    private void filterDestinations(JComboBox<Location> cb, String carType) {
        for (int i = 1; i < cb.getItemCount(); i++) {
            Location dest = cb.getItemAt(i);
            if (!dest.acceptsTypeName(carType)) {
                cb.removeItem(dest);
                i--;
            }
        }
    }

    // remove destination tracks that don't service the car's type, road, or load
    private void filterTracks(Location loc, JComboBox<Track> cb, String carType, String carRoad, String carLoad) {
        List<Track> tracks = loc.getTrackList();
        for (Track track : tracks) {
            if (!track.acceptsTypeName(carType) ||
                    track.isStaging() ||
                    (!carRoad.equals(ScheduleItem.NONE) && !track.acceptsRoadName(carRoad)) ||
                    (!carLoad.equals(ScheduleItem.NONE) && !track.acceptsLoad(carLoad, carType))) {
                cb.removeItem(track);
            }
        }
    }

    // remove receive loads not serviced by track
    private void filterLoads(ScheduleItem si, JComboBox<String> cb) {
        for (int i = cb.getItemCount() - 1; i > 0; i--) {
            String loadName = cb.getItemAt(i);
            if (!loadName.equals(CarLoads.NONE) && !_track.acceptsLoad(loadName, si.getTypeName())) {
                cb.removeItem(loadName);
            }
        }
    }

    public void setMatchMode(boolean mode) {
        if (mode != _matchMode) {
            _matchMode = mode;
            XTableColumnModel tcm = (XTableColumnModel) _table.getColumnModel();
            tcm.setColumnVisible(tcm.getColumnByModelIndex(HIT_COLUMN), mode);
            tcm.setColumnVisible(tcm.getColumnByModelIndex(COUNT_COLUMN), !mode);
        }
    }

    // this table listens for changes to a schedule and its car types
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(Schedule.LISTCHANGE_CHANGED_PROPERTY)) {
            updateList();
            fireTableDataChanged();
        }
        if (e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.ROADS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.LOADS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.SCHEDULE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.DISPOSE_CHANGED_PROPERTY)) {
            fireTableDataChanged();
        }
        // update hit count or other schedule item?
        if (e.getSource().getClass().equals(ScheduleItem.class)) {
            ScheduleItem item = (ScheduleItem) e.getSource();
            int row = _list.indexOf(item);
            if (Control.SHOW_PROPERTY) {
                log.debug("Update schedule item table row: {}", row);
            }
            if (row >= 0) {
                fireTableRowsUpdated(row, row);
            }
        }
    }

    private void removePropertyChangeScheduleItems() {
        for (ScheduleItem si : _list) {
            si.removePropertyChangeListener(this);
            if (si.getDestination() != null) {
                si.getDestination().removePropertyChangeListener(this);
            }
            if (si.getDestinationTrack() != null) {
                si.getDestinationTrack().removePropertyChangeListener(this);
            }
        }
    }

    public void dispose() {
        if (_schedule != null) {
            removePropertyChangeScheduleItems();
            _schedule.removePropertyChangeListener(this);
        }
        _location.removePropertyChangeListener(this);
        _track.removePropertyChangeListener(this);

    }

    private final static Logger log = LoggerFactory.getLogger(ScheduleTableModel.class);
}
