package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarEditFrame;
import jmri.jmrit.operations.rollingstock.cars.CarSetFrame;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class TableDataModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener, ItemListener {

    private static final Logger log = LoggerFactory.getLogger(TableDataModel.class);

    protected JTable tableParent = null;
    protected static final int TIME_COLUMN = 0;
    protected static final int ROAD_COLUMN = 1;
    protected static final int CAR_NUMBER_COLUMN = 2;
    protected static final int TAG_COLUMN = 3;
    protected static final int LOCATION_COLUMN = 4;
    protected static final int TRACK_COLUMN = 5;
    protected static final int TRAIN_COLUMN = 6;
    protected static final int TRAIN_POSITION_COLUMN = 7;
    protected static final int DESTINATION_COLUMN = 8;
    protected static final int ACTION1_COLUMN = 9;
    protected static final int ACTION2_COLUMN = 10;
    protected static final int COLUMN_COUNT = ACTION2_COLUMN + 1;
    private final int[] tableColumn_widths = {60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60};

    // this is the list the represents the JTable
    List<TagCarItem> tagList = new Vector<>();

    protected List<String> locations;
    protected Hashtable<String, List<String>> trackLists;
    private DefaultCellEditor locationCellEditor = null;
    private DefaultCellEditor trackCellEditor = null;
    TagMonitorPane parentPane = null;

    public TableDataModel(LocationManager locationManager, TagMonitorPane parentPane) {
        super();
        this.parentPane = parentPane;
    }

    public TableDataModel() {
        super();
    }

    public void showTimestamps(boolean showTimestamps) {
        this.showTimestamps = showTimestamps;
        XTableColumnModel tcm = (XTableColumnModel) tableParent.getColumnModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(TIME_COLUMN), showTimestamps);
        fireTableDataChanged();
    }

    protected boolean showTimestamps = false;

    public void setRowMax(int rowMax) {
        cleanTable(rowMax, true);
        this.rowMax = rowMax;
    }


    /**
     * @param locationBox - the combBox to be used for the Location
     * @param trackBox    the ComboBox for the track
     * @param car         fill in the list of locations in the location box if the car has a location, select it when
     *                    the location item is selected, fill in the track box with the approriate list select the
     *                    track
     */
    private void setCombos(JComboBox<String> locationBox, JComboBox<String> trackBox, RollingStock car) {
        locationBox.removeItemListener(this);
        trackBox.removeItemListener(this);
        locationBox.addItem("");
        for (String loc : locations) {
            locationBox.addItem(loc);
        }
        String locName = "";
        if (car.getLocation() == null) {
            locationBox.setSelectedIndex(0);
            trackBox.addItem("");
            trackBox.setEnabled(false);
            // track box should not have an item listener if it is not enabled
        } else {
            locationBox.addItemListener(this);
            trackBox.addItemListener(this);
            locName = car.getLocationName();
            locationBox.setSelectedItem(locName);
            List<String> tracksHere = trackLists.get(locName);
            for (String thisTrack : tracksHere) {
                trackBox.addItem(thisTrack);
            }
            if (car.getTrack() != null) {
                trackBox.setSelectedItem(car.getTrack().getName());
            }
            if (tracksHere.size() == 2) {
                trackBox.setSelectedIndex(1);
            }
            trackBox.addItemListener(this);
        }
        locationBox.addItemListener(this);
    }

    private void cleanTable(int newRowMax, boolean fireChange) {
        boolean rowsRemoved = false;
        if (tagList.size() <= newRowMax) {
            return;
        }
        if (tableParent.isEditing()) {
            tableParent.getCellEditor().stopCellEditing();
        }
        while (tagList.size() > newRowMax) {
            tagList.remove(0);
            rowsRemoved = true;
        }
        if (rowsRemoved && fireChange) {
            fireTableDataChanged();
        }
    }

    public void add(TagCarItem newItem) {
        cleanTable(rowMax - 1, false);
        RollingStock newCar = newItem.getCurrentCar();
        if (newCar != null) {
            // only have a combo box if there is a car - need to build both combo boxes here
            JComboBox<String> carLocation = new JComboBox<>();
            JComboBox<String> carTrack = new JComboBox<>();
            setCombos(carLocation, carTrack, newCar);
            newItem.setLocation(carLocation);
            newItem.setTrack(carTrack);
        }
        tagList.add(newItem);
        fireTableDataChanged();
    }

    public void setLast(LocalTime newLast) {
        if (tagList.size() > 0) {
            tagList.get(tagList.size() - 1).setLastSeen(newLast);
            fireTableCellUpdated(tagList.size()-1, TIME_COLUMN);
        }
    }

    private int rowMax = 20;

   public void setParent(JTable parent) {
       this.tableParent = parent;
   }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    public int getRowCount() {
        return tagList.size();
    }

    @Override
    public int getColumnCount() {
        log.debug("get column count called - returned {}",  Integer.toString(COLUMN_COUNT));
        return COLUMN_COUNT;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= tagList.size()) {
            return "";
        }
        TagCarItem current = tagList.get(rowIndex);
        switch (columnIndex) {
            case TIME_COLUMN:
                return current.getLastSeen().toString();
            case ROAD_COLUMN:
                return current.getRoad();
            case CAR_NUMBER_COLUMN:
                return current.getCarNumber();
            case TAG_COLUMN:
                return current.getTag();
            case LOCATION_COLUMN:
                if (current.getCurrentCar() == null) {
                    return "";
                }
                if (current.getLocationCombo().getSelectedIndex() == -1) {
                    return "";
                }
                return current.getLocationCombo().getSelectedItem();
            case TRACK_COLUMN:
                if (current.getCurrentCar() == null) {
                    return "";
                }
                if (current.getLocationCombo().getSelectedIndex() == -1) {
                    return "";
                }
                return current.getTrackCombo().getSelectedItem();
            case TRAIN_COLUMN:
                if (current.getTrain() == null) {
                    return "";
                }
                return current.getTrain();
            case DESTINATION_COLUMN:
                if (current.getDestination() == null) {
                    return "";
                }
                return current.getDestination();
            case TRAIN_POSITION_COLUMN:
                if (current.getTrainPosition() == null) {
                    return "";
                }
                return current.getTrainPosition();
            case ACTION1_COLUMN:
                if (current.getCurrentCar() != null) {
                    return current.getAction1().getText();
                } else {
                    return "";
                }
            case ACTION2_COLUMN:
                if (current.getAction2() == null) {
                    return "";
                }
                return current.getAction2().getText();
            default:
                return "unknown"; //NOI18N
        }
    }

    CarSetFrame csf = null;
    CarEditFrame cef = null;


    public void setValueAt(Object value, int row, int col) {
        TagCarItem thisRowValue = tagList.get(row);
        RollingStock car = thisRowValue.getCurrentCar();
        switch (col) {
            case LOCATION_COLUMN:
                if (value instanceof String) {
                    log.debug("new value for Location column - {}", value);
                    locationItemUpdated(thisRowValue, (String) value);
                }
                break;
            case TRACK_COLUMN:
                if (value instanceof String) {
                    trackItemUpdate(thisRowValue, (String) value);
                }
                break;
            case ACTION1_COLUMN:
                // set location
                if (csf != null) {
                    csf.dispose();
                }
                SwingUtilities.invokeLater(() -> {
                    csf = new CarSetFrame();
                    csf.initComponents();
                    csf.loadCar((Car) car);
                });
                break;
            case ACTION2_COLUMN:
                if (cef != null) {
                    cef.dispose();
                }
                SwingUtilities.invokeLater(() -> {
                    cef = new CarEditFrame();
                    cef.initComponents();
                    cef.load((Car) car);
                });
                break;
            default:
                log.error("should not be setting value for column {}", Integer.toString(col));
        }

    }

    Component getLocationRowEditor(JTable table, Object value, boolean isSelected, int row, int column) {
        if (column != LOCATION_COLUMN) {
            log.error("getLocationRowEditor called for other than Location column {}", column);
            return null;
        }
        if (tagList.get(row).getCurrentCar() == null) {
            log.debug("this row does not have a car associated -- cannot edit");
            JComboBox<String> newBox = new JComboBox<>();
            newBox.addItem("");
            return newBox;
        }
        return tagList.get(row).getLocationCombo();

    }

    Component getTrackRowEditor(JTable table, Object value, boolean isSelected, int row, int column) {
        if (column != TRACK_COLUMN) {
            log.error("Track row colum called for incorrect column: {}", column);
            return null;
        }
        return tagList.get(row).getTrackCombo();
    }

    private void buildLocationValues() {
        locations = new ArrayList<String>();
        trackLists = new Hashtable<String, List<String>>();
        for (Location loc : parentPane.locationManager.getList()) {
            locations.add(loc.getName());
            List<Track> listOfTracks = loc.getTracksByNameList(null);
            List<String> tempTrack = new ArrayList<String>();
            tempTrack.add("");   // always have the empty list (not selected)
            for (Track thisTrack : listOfTracks) {
                tempTrack.add(thisTrack.getName());
            }
            trackLists.put(loc.getName(), tempTrack);
        }


    }

    void initTable() {
        buildLocationValues();
        XTableColumnModel tcm = new XTableColumnModel();
        tableParent.setColumnModel(tcm);
        tableParent.createDefaultColumnsFromModel();
        for (int i = 0; i < tcm.getColumnCount(); i++) {
            tcm.getColumn(i).setPreferredWidth(tableColumn_widths[i]);
        }
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        tcm.getColumn(ACTION1_COLUMN).setCellRenderer(buttonRenderer);
        tcm.getColumn(ACTION2_COLUMN).setCellRenderer(buttonRenderer);
        tcm.setColumnVisible(tcm.getColumnByModelIndex(TIME_COLUMN), showTimestamps);
        JComboBox<String> locationBox = new JComboBox<>();
        locationCellEditor = new EditTrackCellEditor(this, locationBox);
        trackCellEditor = new EditTrackCellEditor(this, new JComboBox<String>());
        tcm.getColumnByModelIndex(LOCATION_COLUMN).setCellEditor(locationCellEditor);
        tcm.getColumnByModelIndex(TRACK_COLUMN).setCellEditor(trackCellEditor);
        fireTableDataChanged();
    }

    @Override
    public String getColumnName(int col) {
        switch (col) {
            case TIME_COLUMN:
                return Bundle.getMessage("MonitorTimeStampCol");
            case ROAD_COLUMN:
                return Bundle.getMessage("MonitorRoadCol");
            case CAR_NUMBER_COLUMN:
                return Bundle.getMessage("MonitorCarNumCol");
            case TAG_COLUMN:
                return Bundle.getMessage("MonitorTagCol");
            case LOCATION_COLUMN:
                return Bundle.getMessage("MonitorLocation");
            case TRACK_COLUMN:
                return Bundle.getMessage("MonitorTrack");
            case TRAIN_COLUMN:
                return Bundle.getMessage("MonitorTrain");
            case TRAIN_POSITION_COLUMN:
                return Bundle.getMessage("MonitorTrainPosition");
            case DESTINATION_COLUMN:
                return Bundle.getMessage("MonitorDestination");
            case ACTION1_COLUMN:
                return Bundle.getMessage("MonitorAction1");
            case ACTION2_COLUMN:
                return Bundle.getMessage("MonitorAction2");
            default:
                return "unknown"; //NOI18N
        }
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case TRAIN_POSITION_COLUMN:
                return Integer.class;
            case LOCATION_COLUMN:
            case TRACK_COLUMN:
                return JComboBox.class;
            case ACTION1_COLUMN:
            case ACTION2_COLUMN:
                return JButton.class;
            default:
                return String.class;
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        switch (col) {
            case LOCATION_COLUMN:
            case TRACK_COLUMN:
            case ACTION1_COLUMN:
            case ACTION2_COLUMN:
                return true;
            default:
                return false;
        }
    }

    private void locationItemUpdated(TagCarItem thisRow, String newvalue) {
        if (newvalue.equals("")) {
            if (thisRow.getLocationValue() == null) {
                return;
            }
            thisRow.setUpdatedLocation("", "");
        } else {
            List<String> tracksHere = trackLists.get(newvalue);
            thisRow.getTrackCombo().removeAll();
            for (String thisTrack : tracksHere) {
                thisRow.getTrackCombo().addItem(thisTrack);
            }
            if (tracksHere.size() == 2) {
                thisRow.getTrackCombo().setSelectedIndex(1);
                thisRow.setUpdatedLocation((String) newvalue, tracksHere.get(1));
            } else {
                thisRow.setUpdatedLocation((String) newvalue, "");
            }
        }

    }

    private void trackItemUpdate(TagCarItem thisRow, String newValue) {
        thisRow.setUpdatedTrack(newValue);
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        log.debug("item event fired for {} ", e.getItem());
        Object eventSource = e.getSource();
        if (e.getStateChange() != ItemEvent.SELECTED) {
            // we only need to do something if the location of track was selected
            return;
        }
        for (TagCarItem thisRow : tagList) {
            Object locCombo = thisRow.getLocationCombo();
            Object trackCombo = thisRow.getTrackCombo();
            if (e.getSource().equals(thisRow.getLocationCombo())) {
                locationItemUpdated(thisRow, (String) e.getItem());
            } else if (e.getSource().equals(thisRow.getTrackCombo())) {
                trackItemUpdate(thisRow, (String) e.getItem());
            }
            log.error("got an ItemEvent for an unknown source");
        }
    }

    public class EditTrackCellEditor extends DefaultCellEditor {

        private TableDataModel model = null;

        public EditTrackCellEditor(TableDataModel model, JComboBox<String> comboBox) {
            super(comboBox);
            this.model = model;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (model == null) {
                log.error("was not called through the correct constructor - model is null");
            }
            if (column == TableDataModel.TRACK_COLUMN) {
                return model.getTrackRowEditor(table, value, isSelected, row, column);
            } else if (column == TableDataModel.LOCATION_COLUMN) {
                return model.getLocationRowEditor(table, value, isSelected, row, column);
            }

            return super.getTableCellEditorComponent(table, value, isSelected, row, column);
        }

    }

}

