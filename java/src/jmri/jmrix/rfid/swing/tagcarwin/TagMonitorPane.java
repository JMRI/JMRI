package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.IdTag;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrix.rfid.RfidListener;
import jmri.jmrix.rfid.RfidMessage;
import jmri.jmrix.rfid.RfidReply;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.jmrix.rfid.swing.RfidPanelInterface;
import jmri.util.swing.JmriPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;

public class TagMonitorPane extends JmriPanel implements RfidListener, RfidPanelInterface, TableModelListener {
    private static final Logger log = LoggerFactory.getLogger(TagMonitorPane.class);
    // panel members
    private JTable tagMonitorTable = null;
    TableDataModel dataModel = null;
    protected Integer currentRowCount = 15;
    private String lastTagSeen = "";
    private Vector<RollingStock> lastTrainCars = new Vector<RollingStock>();
    private Train lastTrain = null;

    CarManager carManager = InstanceManager.getDefault(CarManager.class);
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);
    RfidSystemConnectionMemo memo = null;


    /**
     * If the System Prefix is available, append it to the title to identify which reader this belongs to
     * @return
     */
    @Override
    public String getTitle() {
        if (memo != null) {
            return Bundle.getMessage("MonitorRFIDTagCars", "RFID Device") + " - " + memo.getSystemPrefix();
        } else {
            return Bundle.getMessage("MonitorRFIDTagCars", "RFID Device");
        }
    }

    /**
     * RFID typically don't send messages, so this is ignored
     * @param m
     */
    @Override
    public void message(RfidMessage m) {
        log.debug("got a new tag {}", m.toString());
    }


    /**
     * Process an RFID message (typically a tag was seen)
     * Tags in JMRI are identified with the string "ID" + the tag number
     * if this is the same tag as was last seen, suppress the display unless the option
     * to display duplicates has been set (in that case, update the timestamp)
     * Pass the tag to createNewItem to build the new display
     * @param m the message
     */
    @Override
    public void reply(RfidReply m) {
        log.debug("got a new Reply msg {}", m.toString());
        String thisTag = "ID" + memo.getProtocol().getTag(m);
        log.debug("This this tag is -{}-", thisTag);
        if (!showDuplicates.isSelected()) {
            if (thisTag.equals(lastTagSeen)) {
                dataModel.setLast(LocalTime.now());
                return;
            }
        }
        lastTagSeen = thisTag;
        createNewItem(thisTag);
    }


    /**
     * Create most of the new row data
     * Look up the car, if found we add the car to row, and set up most of the data
     * the combo boxes will be added in the data model
     * @param tag
     * @return
     */
    private TagCarItem createNewItem(String tag) {
        TagCarItem newTag = new TagCarItem(tag, LocalTime.now());
        RollingStock thisCar = findCarByTag(tag);
        JButton action1Button = new JButton();
        if (thisCar != null) {
            newTag.setRoad(thisCar.getRoadName());
            newTag.setCarNumber(thisCar.getNumber());
            newTag.setCurrentCar(thisCar);
            newTag.setAction1(action1Button);
            action1Button.setText(Bundle.getMessage("MonitorSetLocation"));
            action1Button.setEnabled(false); // not enabled until location is changed
            JButton action2 = new JButton();
            action2.setText(Bundle.getMessage("MonitorEditCar"));
            newTag.setAction2(action2);
            if (thisCar.getTrainName() != null) {
                newTag.setTrain(thisCar.getTrainName());
                newTag.setTrainPosition(getCarTrainPosition(thisCar, thisCar.getTrain()));
            }
        } else {
            newTag.setAction1(action1Button);
            action1Button.setText(Bundle.getMessage("MonitorAssoicate"));
            newTag.setAction2(null);
        }
        dataModel.add(newTag);
        return newTag;
    }

    /**
     * Look up this car in the list of cars
     * It could be a car or an Engine
     * @param tag the String representing the RFID tag
     * @return the entry which was found, or null for not found
     */
    public RollingStock findCarByTag(String tag) {
        for (Car thisCar : carManager.getByIdList()) {
            IdTag foundTag = thisCar.getIdTag();
            if (foundTag != null) {
                if (foundTag.toString().equals(tag)) {
                    log.debug("car matched tag of {}", tag);
                    return thisCar;
                }
            }
        }
        log.debug("no car found for tag {}", tag);
        return null;
    }

    /**
     * If this car (engine or car) is in a train, determine what the car position is
     * @param thisCar
     * @param thisTrain
     * @return the position of car within the train
     */
    public int getCarTrainPosition(RollingStock thisCar, Train thisTrain) {
        String carRoad = thisCar.getRoadName();
        String carNumber = thisCar.getNumber();
        if (thisTrain == null) {
            log.debug("train is null in getCarTrainPosition");
            return 0;
        }
        log.debug("finding car {} {} in Train {}", carRoad, carNumber, thisTrain.getName());
        if (!thisTrain.equals(lastTrain)) {
            log.debug("new train - retrieving it");
            lastTrainCars.removeAllElements();
            lastTrainCars.addAll(carManager.getByTrainDestinationList(thisTrain));
        }
        int positionCounter = 0;
        for (RollingStock trainElement : lastTrainCars) {
            if (trainElement instanceof Car) {
                positionCounter++;
                if (carRoad.equals(trainElement.getRoadName().equals(carRoad) && carNumber.equals(trainElement.getNumber()))) {
                    return positionCounter;
                }
            }
        }
        log.error("Expected to find car {} {} in train {} and did not", carRoad, carNumber, thisTrain.getName());
        return 0;
    }

    /**
     * Save the connection identifier to use for the System Prefix and to get the tag protocol
     * add this class as a listener to get the RFID replies
     * @param memo SystemConnectionMemo for configured RFID system
     */
    @Override
    public void initComponents(RfidSystemConnectionMemo memo) {
        this.memo = memo;

        memo.getTrafficController().addRfidListener(this);
        log.debug("added self as RFID listener");
    }


    /**
     * Save the preferences for use later
     * They will apply to all instances of this class (regardless of which connnections uses it)
     */
    @Override
    public void dispose() {
        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);
        pm.setSimplePreferenceState(timeCheck, showTimestamps.isSelected());
        pm.setSimplePreferenceState(dupeCheck, showDuplicates.isSelected());
        pm.setProperty(rowCountField, rowCountField, rowCount.getText());
        super.dispose();
    }

    /**
     * Replace the current message on the panel with a new one
     * @param newMessage
     */
    public void setMessage(String newMessage) {
        panelMessage.setText(newMessage);
    }

    // elements for the UI
    protected JCheckBox showTimestamps = new JCheckBox();
    protected JCheckBox showDuplicates = new JCheckBox();
    protected String timeCheck = this.getClass().getName() + "Times";
    protected String dupeCheck = this.getClass().getName() + "Duplicates";
    protected JLabel rowCountLabel = new JLabel();
    protected JTextField rowCount = new JTextField();
    protected JButton setRowCount = new JButton();
    protected String rowCountField = this.getClass().getName() + "RowCount";
    JLabel panelMessage = new JLabel("");

    @Override
    public void initComponents() {
        dataModel = new TableDataModel(locationManager, this);
        tagMonitorTable = new JTable(dataModel);
        tagMonitorTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tagMonitorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        dataModel.setParent(tagMonitorTable);
        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);
        showTimestamps.setText(Bundle.getMessage("MonitorTimestamps"));
        showTimestamps.setVisible(true);
        showTimestamps.setToolTipText(Bundle.getMessage("MonitorTimeToolTip"));
        showTimestamps.setSelected(pm.getSimplePreferenceState(timeCheck));
        showDuplicates.setText(Bundle.getMessage("MonitorShowDupes"));
        showDuplicates.setVisible(true);
        showDuplicates.setToolTipText(Bundle.getMessage("MonitorDupesToolTip"));
        showDuplicates.setSelected(pm.getSimplePreferenceState(dupeCheck));
        dataModel.showTimestamps = showTimestamps.isSelected();

        dataModel.initTable();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JScrollPane tablePane = new JScrollPane(tagMonitorTable);
        tablePane.setPreferredSize(new Dimension(100, 400));
        tagMonitorTable.setFillsViewportHeight(true);
        add(tablePane);
        JPanel checkBoxPanel = new JmriPanel();
        JPanel messagePanel = new JmriPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
        JLabel msgLabel = new JLabel(Bundle.getMessage("MonitorMessageLabel"));
        msgLabel.setPreferredSize(new Dimension(30, 15));
        messagePanel.add(msgLabel);
        panelMessage.setText(""); // no message to start
        panelMessage.setPreferredSize(new Dimension(170, 15));
        messagePanel.add(panelMessage);
        add(messagePanel);
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.X_AXIS));
        rowCountLabel.setText(Bundle.getMessage("MonitorRowCount"));
        checkBoxPanel.add(rowCountLabel);
        try {
            currentRowCount = Integer.valueOf(pm.getProperty(rowCountField, rowCountField).toString());
        } catch (NullPointerException nulls) {
            currentRowCount = 15;
        }
        dataModel.setRowMax(currentRowCount);
        checkBoxPanel.setPreferredSize(new Dimension(400, 30));
        checkBoxPanel.setMaximumSize(new Dimension(600, 30));
        rowCount.setPreferredSize(new Dimension(40, 15));
        rowCount.setText(currentRowCount.toString());
        checkBoxPanel.add(rowCount);
        checkBoxPanel.add(setRowCount);
        setRowCount.setText(Bundle.getMessage("MonitorSetRowCount"));
        setRowCount.setToolTipText(Bundle.getMessage("MonitorRowToolTip"));
        checkBoxPanel.add(showTimestamps);
        checkBoxPanel.add(showDuplicates);
        add(checkBoxPanel);
        rowCount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!rowCount.getText().isEmpty()) {
                    String text = rowCount.getText();
                    try {
                        int newValue = Integer.valueOf(text);
                        if (newValue > 0 && newValue < 100) {
                            setRowCount.setEnabled(true);
                        } else {
                            setRowCount.setEnabled(false);
                        }
                    } catch (NumberFormatException exception) {
                        setRowCount.setEnabled(false);
                    }
                } else {
                    setRowCount.setEnabled(false);
                }
            }
        });
        setRowCount.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int newValue = Integer.valueOf(rowCount.getText());
                    dataModel.setRowMax(newValue);
                } catch (NumberFormatException exception) {
                    log.error("error interpreting new number of lines");
                }
            }
        });
        ActionListener checkListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource().equals(showTimestamps)) {
                    dataModel.showTimestamps(showTimestamps.isSelected());
                    log.debug("switching show timestamps now");
                } else if (e.getSource().equals(showDuplicates)) {
                    log.debug("changing show duplicates now");
                }
            }
        };
        showDuplicates.addActionListener(checkListener);
        showTimestamps.addActionListener(checkListener);
        dataModel.addTableModelListener(this);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getType() == TableModelEvent.UPDATE) {
            log.debug("table was updated");
            int row = e.getFirstRow();
            int column = e.getColumn();
            if (column == TableDataModel.LOCATION_COLUMN) {
                log.debug("Location was changed");
                int thisRow = e.getFirstRow();
                while (thisRow <= e.getLastRow()) {
                    log.debug("Updated location column row {}", thisRow);
                    thisRow++;
                }
            } else if (column == TableDataModel.TRACK_COLUMN) {
                int thisRow = e.getFirstRow();
                while (thisRow <= e.getLastRow()) {
                    log.debug("Track column row {}", thisRow);
                    thisRow++;
                }
            }
        }
    }

}
