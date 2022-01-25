package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.jmrit.operations.locations.Location;
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
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class TableDataModel extends javax.swing.table.AbstractTableModel implements PropertyChangeListener {

    private static final Logger log = LoggerFactory.getLogger(TableDataModel.class);

    protected JTable tableParent = null;
    private static final int TIME_COLUMN = 0;
    private static final int ROAD_COLUMN = 1;
    private static final int CAR_NUMBER_COLUMN = 2;
    private static final int TAG_COLUMN = 3;
    private static final int LOCATION_COLUMN = 4;
    private static final int TRACK_COLUMN = 5;
    private static final int TRAIN_COLUMN = 6;
    private static final int TRAIN_POSITION_COLUMN = 7;
    private static final int DESTINATION_COLUMN = 8;
    private static final int ACTION1_COLUMN = 9;
    private static final int ACTION2_COLUMN = 10;
    private static final int COLUMN_COUNT = ACTION2_COLUMN + 1;
    private final int[] tableColumn_widths = {60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60};
    List<TagCarItem> tagList = new Vector<TagCarItem>();
    protected List<Location> locations;
    protected List<Track> tracks;
    private DefaultCellEditor locationCellEditor = null;
    private DefaultCellEditor trackCellEditor = null;

    public void showTimestamps(boolean showTimestamps) {
        this.showTimestamps = showTimestamps;
        XTableColumnModel tcm = (XTableColumnModel)tableParent.getColumnModel();
        tcm.setColumnVisible(tcm.getColumnByModelIndex(TIME_COLUMN), showTimestamps);
        fireTableDataChanged();
    }

    protected boolean showTimestamps = false;

    public void setRowMax(int rowMax) {
        this.rowMax = rowMax;
        if (tagList.size() > rowMax) {
            while (tagList.size() > rowMax) {
                tagList.remove(0);
            }
            fireTableDataChanged();
        }
    }

    public void add(TagCarItem newItem) {
        while (tagList.size() >= rowMax) {
            tagList.remove(0);
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
                if (current.getCurrentCar().getLocation() != null) {
                    return current.getLocationCombo().getSelectedItem();
                }
                return current.getLocationName();
            case TRACK_COLUMN:
                if (current.getTrack() == null) {
                    return "";
                }
                return current.getTrack();
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
                    return Bundle.getMessage("MonitorSetLocation");
                } else {
                    return "";
                }
            case ACTION2_COLUMN:
                if (current.getCurrentCar() == null) {
                    return Bundle.getMessage("MonitorShowCars");
                } else {
                    return Bundle.getMessage("MonitorEditCar");
                }
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
               JComboBox<String>  thisCell = (JComboBox<String>) locationCellEditor.getComponent();
               thisCell.removeAllItems();
               for (Location location: locations) {
                   thisCell.addItem(location.getName());
               }
               if (car != null) {
                   if (car.getLocationName() != null ) {
                       thisCell.setSelectedItem(car.getLocationName());
                   }
               }
               break;
           case TRACK_COLUMN:
               JComboBox trackCell = (JComboBox) trackCellEditor.getComponent();

               trackCell.removeAllItems();

               break;
           case ACTION1_COLUMN:
               // set location
               if (csf != null) {
                   csf.dispose();
               }
               SwingUtilities.invokeLater( () -> {
                   csf = new CarSetFrame();
                   csf.initComponents();
                   csf.loadCar((Car)car);
               });
               break;
           case ACTION2_COLUMN:
               if (cef != null) {
                   cef.dispose();
               }
               SwingUtilities.invokeLater(()-> {
                   cef = new CarEditFrame();
                   cef.initComponents();
                   cef.load((Car)car);
               });
               break;
           default:
               log.error("should not be setting value for column {}", Integer.toString(col));
       }

   }

    void initTable() {
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
        locationCellEditor = new DefaultCellEditor(new JComboBox<String>());
        trackCellEditor = new DefaultCellEditor(new JComboBox<String>());
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
}
