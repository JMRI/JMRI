package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.IdTag;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
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
import java.awt.*;
import java.time.LocalTime;
import java.util.Vector;

public class TagMonitorPane extends JmriPanel implements RfidListener, RfidPanelInterface  {
    private static final Logger log = LoggerFactory.getLogger(TagMonitorPane.class);
    // panel members
    private JTable tagMonitorTable = null;
    TableDataModel dataModel = null;
    protected Integer currentRowCount = 15;
    private String lastTagSeen = "";
    private Vector<RollingStock> lastTrainCars = new Vector<RollingStock>();
    private Train lastTrain = null;


    CarManager carManager = InstanceManager.getDefault(CarManager.class);
    RfidSystemConnectionMemo memo = null;

    @Override
    public String getTitle() {
        return Bundle.getMessage("MonitorRFIDTagCars", "RFID Device");
    }

    @Override
    public void message(RfidMessage m) {
        log.debug("got a new tag {}", m.toString());
    }

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
        Car thisCar = findCarByTag(thisTag);
        TagCarItem newCar = new TagCarItem(thisTag, LocalTime.now());
        if (thisCar != null) {
            newCar.setRoad(thisCar.getRoadName());
            newCar.setCarNumber(thisCar.getNumber());
            newCar.setCurrentCar(thisCar);
            if (thisCar.getLocation() != null) { 
                newCar.setLocation(thisCar.getLocationName());
                if (thisCar.getTrackName() != null) {
                    newCar.setTrack(thisCar.getTrackName());
                }
            }
            if (thisCar.getTrainName() != null) {
                newCar.setTrain(thisCar.getTrainName());
                newCar.setTrainPosition(getCarTrainPosition(thisCar, thisCar.getTrain()));
            }           
        } else {
            newCar.setAction1(null);
            newCar.setAction2(null);
        }
        dataModel.add(newCar);
    }

    public Car findCarByTag(String tag) {
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

    public int getCarTrainPosition(Car thisCar, Train thisTrain) {
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


    @Override
    public void initComponents(RfidSystemConnectionMemo memo) {
        this.memo = memo;
        memo.getTrafficController().addRfidListener(this);
        log.debug("added self as RFID listener");
    }

    @Override
    public void dispose() {
        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);
        pm.setSimplePreferenceState(timeCheck, showTimestamps.isSelected());
        pm.setSimplePreferenceState(dupeCheck, showDuplicates.isSelected());
        pm.setProperty(rowCountField, rowCountField, currentRowCount.toString());
        super.dispose();
    }


    protected JCheckBox showTimestamps = new JCheckBox();
    protected JCheckBox showDuplicates = new JCheckBox();
    protected String timeCheck = this.getClass().getName() + "Times";
    protected String dupeCheck = this.getClass().getName() + "Duplicates";
    protected JLabel rowCountLabel = new JLabel();
    protected JTextField rowCount = new JTextField();
    protected JButton setRowCount = new JButton();
    protected String rowCountField = this.getClass().getName() + "RowCount";

    @Override
    public void initComponents() {
        dataModel = new TableDataModel();
        tagMonitorTable = new JTable(dataModel);
        dataModel.setParent(tagMonitorTable);
        dataModel.initTable();
        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);
        showTimestamps.setText(Bundle.getMessage("MonitorTimestamps"));
        showTimestamps.setVisible(true);
        showTimestamps.setToolTipText(Bundle.getMessage("MonitorTimeToolTip"));
        showTimestamps.setSelected(pm.getSimplePreferenceState(timeCheck));
        showDuplicates.setText(Bundle.getMessage("MonitorShowDupes"));
        showDuplicates.setVisible(true);
        showDuplicates.setToolTipText(Bundle.getMessage("MonitorDupesToolTip"));
        showDuplicates.setSelected(pm.getSimplePreferenceState(dupeCheck));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JScrollPane tablePane = new JScrollPane(tagMonitorTable);
        tablePane.setPreferredSize(new Dimension(100, 600));
        tagMonitorTable.setFillsViewportHeight(true);
        add(tablePane);
        JPanel checkBoxPanel = new JmriPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.X_AXIS));
        rowCountLabel.setText(Bundle.getMessage("MonitorRowCount"));
        checkBoxPanel.add(rowCountLabel);
        try {
            currentRowCount = Integer.valueOf(pm.getProperty(rowCountField, rowCountField).toString());
        } catch (NullPointerException nulls) {
            currentRowCount = 15;
        }
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
    }

}
