package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarEditFrame;
import jmri.util.swing.XTableColumnModel;
import jmri.util.table.ButtonRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * The table model for displaying rows of incoming RFID tags and associating them with cars
 * and locations.  This is where most of the logic resides, though the actually receiving of the table
 * is done in the parent
 *
 * @author J. Scott Walton Copyright (C) 2022
 */
public class TableDataModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener, ItemListener, ActionListener {

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
    TagMonitorPane parentPane = null;
    CarEditFrame cef;
    AssociateFrame associateFrame;

    public void setForceSetLocation(boolean forceSetLocation) {
        this.forceSetLocation = forceSetLocation;
    }

    boolean forceSetLocation = false;
    private boolean addingRow = false;  // set true when in the middle of adding a row

    public TableDataModel(TagMonitorPane parentPane) {
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
        String locName;
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

    public void clearTable() {
        log.debug("clearing the RFID tag car window");
        cleanTable(0, true);
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
        addingRow = true;
        cleanTable(rowMax - 1, false);
        RollingStock newCar = newItem.getCurrentCar();
        newItem.getAction1().addActionListener(this);
        if (newCar != null) {
            // only have a combo box if there is a car - need to build both combo boxes here
            JComboBox<String> carLocation = new JComboBox<>();
            JComboBox<String> carTrack = new JComboBox<>();
            setCombos(carLocation, carTrack, newCar);
            newItem.setLocation(carLocation);
            newItem.setTrack(carTrack);
            newItem.getAction2().addActionListener(this);
        }
        tagList.add(newItem);
        parentPane.setMessageNormal("");
        fireTableDataChanged();
        addingRow = false;
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
                return current.getAction1().getText();
            case ACTION2_COLUMN:
                if (current.getAction2() == null) {
                    return "";
                }
                return current.getAction2().getText();
            default:
                return "unknown"; //NOI18N
        }
    }



    private void setCarLocation(RollingStock car, TagCarItem thisRow) {
       if (car == null) {
           log.error("attempting to set the location of a null car");
           return;
       }
        log.debug("Setting location of car {} - {}", car.getRoadName(), car.getNumber());
        if (!thisRow.isLocationReady()) {
            log.error("should not be here - this row is not yet ready");
            return;
        }
        String retValue;
        if (thisRow.getTempLocation().equals("")) {
            // removing location from this car

            retValue = car.setLocation(null, null);
        } else {
            // adding or replacing the location on this car
            Location thisLocation = parentPane.locationManager.getLocationByName(thisRow.getTempLocation());
            if (thisLocation == null) {
                log.error("Did not find location identified in ComboBox - {}", thisRow.getTempLocation());
                return;
            }
            Track thisTrack = null;
            for (Track track : thisLocation.getTracksList()) {
                if (track.getName().equals(thisRow.getTempTrack())) {
                    thisTrack = track;
                    break;
                }
            }
            if (thisTrack == null) {
                log.error("Did not find expected track at this location L - T -- {} - {}", thisRow.getTempLocation(), thisRow.getTempTrack());
                return;
            }
            retValue = car.setLocation(thisLocation, thisTrack, forceSetLocation);
        }
        if (retValue.equals("okay")) {
            parentPane.setMessageNormal(Bundle.getMessage("MonitorLocationSet"));
            thisRow.resetTempValues();
        } else {
            parentPane.setMessageError("MonitorLocationFailed");
        }
    }

    private void doEditCar(RollingStock thisCar) {
        if (cef != null) {
            cef.dispose();
        }
        SwingUtilities.invokeLater(() -> {
            cef = new CarEditFrame();
            cef.initComponents();
            cef.load((Car) thisCar);
        });
       // tableParent.getCellEditor().stopCellEditing();
    }

    private void doSetTag(String thisTag, TagCarItem thisRow) {
        // associate tag
        if (associateFrame != null) {
            associateFrame.dispose();
        }
        SwingUtilities.invokeLater(() -> new AssociateFrame(new AssociateTag(thisTag),
                Bundle.getMessage("AssociateTitle") + " " + thisTag).initComponents());
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        TagCarItem thisRowValue = tagList.get(row);
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
                log.debug("setValueAt for Action1 column");
                return;
            case ACTION2_COLUMN:
                log.debug("setValueAt for Action2 column");
                break;
            default:
                log.error("should not be setting value for column {}", col);
        }

    }

    Component getLocationRowEditor(JTable table, Object value, boolean isSelected, int row, int column) {
       int tempCol = column;
       if (!parentPane.getShowTimestamps()) {
           tempCol = column + 1; // if timestamp column is not visible , need to adjust count
       }
       if (tempCol != LOCATION_COLUMN) {
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
       int tempCol = column;
       if (!parentPane.getShowTimestamps()) {
           tempCol = column + 1;   // if timestamp column is not visible, need to adjust column number to account for it
       }
       if (tempCol != TRACK_COLUMN) {
            log.error("Track row column called for incorrect column: {}", column);
            return null;
        }
        return tagList.get(row).getTrackCombo();
    }

    private void buildLocationValues() {
        locations = new ArrayList<>();
        trackLists = new Hashtable<>();
        for (Location loc : parentPane.locationManager.getList()) {
            locations.add(loc.getName());
            List<Track> listOfTracks = loc.getTracksByNameList(null);
            List<String> tempTrack = new ArrayList<>();
            tempTrack.add("");   // always have the empty list (not selected)
            for (Track thisTrack : listOfTracks) {
                tempTrack.add(thisTrack.getName());
            }
            java.util.Collections.sort(tempTrack);
            trackLists.put(loc.getName(), tempTrack);
        }
        java.util.Collections.sort(locations);

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
        TableCellEditor locationCellEditor = new EditTrackCellEditor(this);
        TableCellEditor trackCellEditor = new EditTrackCellEditor(this);
        tcm.getColumnByModelIndex(LOCATION_COLUMN).setCellEditor(locationCellEditor);
        tcm.getColumnByModelIndex(TRACK_COLUMN).setCellEditor(trackCellEditor);
        tcm.getColumnByModelIndex(ACTION1_COLUMN).setCellEditor(new EditTrackCellEditor(this));
        tcm.getColumnByModelIndex(ACTION2_COLUMN).setCellEditor(new EditTrackCellEditor(this));
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
            case CAR_NUMBER_COLUMN:
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
            thisRow.getTrackCombo().setEnabled(false);
        } else {
            thisRow.getTrackCombo().removeItemListener(this);
            List<String> tracksHere = trackLists.get(newvalue);
            thisRow.getTrackCombo().removeAllItems();
            thisRow.getTrackCombo().setEnabled(true);
            for (String thisTrack : tracksHere) {
                thisRow.getTrackCombo().addItem(thisTrack);
            }
            if (tracksHere.size() == 2) {
                thisRow.getTrackCombo().setSelectedIndex(1);
                thisRow.setUpdatedLocation(newvalue, tracksHere.get(1));
                if (thisRow.isLocationReady()) {
                    parentPane.setMessageNormal(Bundle.getMessage("MonitorReadyToSet"));
                    thisRow.getAction1().setEnabled(true);
                } else {
                    parentPane.setMessageNormal("MonitorSetTrackMsg");
                    thisRow.getAction1().setEnabled(false);
                }
            } else {
                thisRow.setUpdatedLocation(newvalue, "");
                parentPane.setMessageNormal(Bundle.getMessage("MonitorSetTrackMsg"));
                thisRow.getAction1().setEnabled(false);
            }
            thisRow.getTrackCombo().addItemListener(this);
        }

    }

    private void trackItemUpdate(TagCarItem thisRow, String newValue) {
        thisRow.setUpdatedTrack(newValue);
        if (thisRow.isLocationReady()) {
            parentPane.setMessageNormal(Bundle.getMessage("MonitorReadyToSet"));
            thisRow.getAction1().setEnabled(true);
        } else {
            thisRow.getAction1().setEnabled(false);
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        log.debug("item event fired for {} ", e.getItem());
        if (addingRow || e.getStateChange() != ItemEvent.SELECTED) {
            // we only need to do something if the location of track was selected
            return;
        }
        for (TagCarItem thisRow : tagList) {
           if (e.getSource().equals(thisRow.getLocationCombo())) {
                locationItemUpdated(thisRow, (String) e.getItem());
            } else if (e.getSource().equals(thisRow.getTrackCombo())) {
                trackItemUpdate(thisRow, (String) e.getItem());
            } else {
                log.error("got an ItemEvent for an unknown source");
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       log.debug("Got an action performed for a button");
        for (TagCarItem thisRow : tagList) {
            if (e.getSource().equals(thisRow.getAction1())) {
                if (thisRow.getCurrentCar() == null) {
                    doSetTag(thisRow.getTag(), thisRow);
                } else {
                    setCarLocation(thisRow.getCurrentCar(), thisRow);
                }
                return;
            } else if (e.getSource().equals(thisRow.getAction2())) {
                doEditCar(thisRow.getCurrentCar());
                return;
            }
        }
        log.error("Got an actionPerformed but dont recognize source");
    }

    static class EditTrackCellEditor extends AbstractCellEditor implements TableCellEditor {

        private TableDataModel model;
        private Component value;

        public EditTrackCellEditor(TableDataModel model) {
            super();
            this.model = model;
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if (model == null) {
                log.error("was not called through the correct constructor - model is null");
                return null;
            }
            int tempCol = column;
            if (model.parentPane == null) {
                log.error("parent pane pointer is missing");
                return null;
            }
            if (!model.parentPane.getShowTimestamps()) {
                tempCol = column + 1;  // if timestamps are not visible, the column counter will be off
            }
            if (tempCol == TableDataModel.TRACK_COLUMN) {
                this.value = model.tagList.get(row).getTrackCombo();
                return model.getTrackRowEditor(table, value, isSelected, row, column);
            } else if (tempCol == TableDataModel.LOCATION_COLUMN) {
                this.value = model.tagList.get(row).getLocationCombo();
                return model.getLocationRowEditor(table, value, isSelected, row, column);
            } else if (tempCol == TableDataModel.ACTION1_COLUMN) {
                this.value = model.tagList.get(row).getAction1();
                return model.tagList.get(row).getAction1();
            } else if (tempCol == TableDataModel.ACTION2_COLUMN) {
                this.value = model.tagList.get(row).getAction2();
                return model.tagList.get(row).getAction2();
            }
            log.error("unable to determine column (value {} - returning null for table editor", column);
            return null;
        }

        @Override
        public Object getCellEditorValue() {
            return this.value;
        }

    }

}

