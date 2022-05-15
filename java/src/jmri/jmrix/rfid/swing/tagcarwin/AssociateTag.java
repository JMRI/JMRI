package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.util.AlphanumComparator;
import jmri.util.JmriJFrame;
import jmri.util.swing.JmriPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A panel to associate an RFID tag with a car
 *
 * @author J. Scott Walton Copyright (C) 2022
 */
public class AssociateTag extends JmriPanel implements ActionListener, ListSelectionListener {
    private static final Logger log = LoggerFactory.getLogger(AssociateTag.class);

    String tag;
    private final JButton okayButton = new JButton();
    private final JButton cancelButton = new JButton();
    private final DefaultListModel<String> roadListModel = new DefaultListModel<>();
    private final DefaultListModel<String> numberListModel = new DefaultListModel<>();
    private final JList<String> roadCombo = new JList<>();
    private final JList<String> numberCombo = new JList<>();


    private final JCheckBox includeCars = new JCheckBox();
    private final JLabel message = new JLabel("");
    private final String includeCarsString = this.getClass().getName() + "IncludeAllCars";
    private final ArrayList<String> roadList = new ArrayList<>();
    private final ArrayList<String> roadsWith = new ArrayList<>();
    private final Hashtable<String, List<String>> numbersWith = new Hashtable<>();
    private final Hashtable<String, List<String>> roadNumbers = new Hashtable<>();
    protected CarManager carManager = InstanceManager.getDefault(CarManager.class);
    protected IdTagManager tagManager = InstanceManager.getDefault(IdTagManager.class);

    public void setParentFrame(JmriJFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    JmriJFrame parentFrame;

    public AssociateTag(String thisTag) {
        super();
        tag = thisTag;
    }


    @Override
    public String getTitle(){
        return Bundle.getMessage("AssociateTitle" ) + tag;
    }

    @Override
    public void dispose() {
        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);
        pm.setSimplePreferenceState(includeCarsString, includeCars.isSelected());
    }

    @Override
    public void initComponents() {
        log.debug("setting up the AssociateTag panel");
        initRoads();
        this.setLayout(new GridBagLayout());
        this.setPreferredSize(new Dimension(500, 500));
        this.setMinimumSize(new Dimension(500, 450));


        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);
        includeCars.setSelected(pm.getSimplePreferenceState(includeCarsString));
        JScrollPane roadScroll = new JScrollPane();
        roadScroll.setMinimumSize(new Dimension(150, 250));
        roadScroll.setViewportView(roadCombo);
        roadCombo.setVisibleRowCount(10);
        roadCombo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roadCombo.setLayoutOrientation(JList.VERTICAL);
        roadCombo.addListSelectionListener(this);
        roadCombo.setMinimumSize(new Dimension(150, 250));
        roadCombo.setModel(roadListModel);
        this.add(roadScroll, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0,
                GridBagConstraints.LINE_START, GridBagConstraints.BOTH,
                new Insets(10, 10, 10, 10), 10, 10));

        JScrollPane numberScroll = new JScrollPane();
        numberScroll.setViewportView(numberCombo);
        numberScroll.setMinimumSize(new Dimension(150, 250));
        numberCombo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        numberCombo.setVisibleRowCount(10);
        numberCombo.setLayoutOrientation(JList.VERTICAL);
        numberCombo.setMinimumSize(new Dimension(75, 75));
        numberCombo.setEnabled(false);
        numberCombo.setModel(numberListModel);

        this.add(numberScroll, new GridBagConstraints( 2, 0, 1, 2, 0.0, 0.0,
                GridBagConstraints.LINE_END, GridBagConstraints.BOTH,
                new Insets(10, 10, 10, 10), 10, 10));
        JLabel tagLabel = new JLabel();
        tagLabel.setText(Bundle.getMessage("AssociateTag"));
        this.add(tagLabel, new GridBagConstraints(0, 4, 2, 1, 0.0, 0.0,
                GridBagConstraints.LINE_END, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 5, 5));
        JLabel thisTag = new JLabel(tag);
        this.add(thisTag, new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0,
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 5, 5));
        includeCars.setText(Bundle.getMessage("AssociateIncludeAll"));
        includeCars.setToolTipText(Bundle.getMessage("AssociateAllToolTip"));
        includeCars.addActionListener(this);
        this.add(includeCars, new GridBagConstraints(0, 5, 3, 1, 0.0, 0.0,
                GridBagConstraints.LINE_START, GridBagConstraints.HORIZONTAL,
                new Insets(5, 5, 5, 5), 0, 0));
        message.setMinimumSize(new Dimension(400,10));
        this.add(message, new GridBagConstraints(0, 6, 3, 1, 0.0, 0.0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                new Insets(10, 10, 10, 10), 5, 5));

        message.setText("  ");
        okayButton.setText(Bundle.getMessage("AssociateOkay"));
        okayButton.addActionListener(this);
        okayButton.setEnabled(false);
        this.add(okayButton, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0,
                GridBagConstraints.LINE_START, GridBagConstraints.NONE,
                new Insets(20, 20, 20, 20), 10, 10));
        cancelButton.setText(Bundle.getMessage("AssociateCancel"));
        cancelButton.addActionListener(this);
        this.add(cancelButton, new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0,
                GridBagConstraints.LINE_END, GridBagConstraints.NONE,
                new Insets(20, 20, 20, 20), 10, 10));
        setRoads();
        if (includeCars.isSelected()) {
            if (roadsWith.size() < 1) {
                message.setText(Bundle.getMessage("AssociateNoCars"));
            }
        }
        if (roadList.size() < 1 ) {
            message.setText(Bundle.getMessage("AssociateNoRoads"));
        }
    }

    private void initRoads() {
        log.debug("building a list of road number with their cars");
        roadList.clear();
        roadNumbers.clear();
        roadsWith.clear();
        numbersWith.clear();
        List<Car> carList = carManager.getList();
        for (Car thisCar : carList) {
            if (numbersWith.containsKey(thisCar.getRoadName())) {
                numbersWith.get(thisCar.getRoadName()).add(thisCar.getNumber());
            } else {
                roadsWith.add(thisCar.getRoadName());
                ArrayList<String> tempArray = new ArrayList<>();
                tempArray.add(thisCar.getNumber());
                numbersWith.put(thisCar.getRoadName(), tempArray);
            }
            if ( "".equals(thisCar.getRfid()) ) {
                if (roadNumbers.containsKey(thisCar.getRoadName())) {
                    roadNumbers.get(thisCar.getRoadName()).add(thisCar.getNumber());
                } else {
                    roadList.add(thisCar.getRoadName());
                    ArrayList<String> tempArray = new ArrayList<>();
                    tempArray.add(thisCar.getNumber());
                    roadNumbers.put(thisCar.getRoadName(), tempArray);
                }
            }
        }
        java.util.Collections.sort(roadsWith);
        java.util.Collections.sort(roadList);
        for (String road : roadList) {
            java.util.Collections.sort(roadNumbers.get(road), new AlphanumComparator());
        }
        for (String road : roadsWith) {
            java.util.Collections.sort(numbersWith.get(road), new AlphanumComparator());
        }
    }

    private void setOneRoad() {
        message.setText(Bundle.getMessage("AssociateReady"));
        numberCombo.setEnabled(true);
        roadCombo.setSelectedIndex(0);
    }

    private void setRoads() {
        roadCombo.removeListSelectionListener(this);
        numberCombo.removeListSelectionListener(this);
        message.setText(" ");
        numberListModel.clear(); // no numbers until we have a selected road
        okayButton.setEnabled(false); // can't okay until we have selected both
        roadListModel.clear();
        List<String> theList;
        if (includeCars.isSelected()) {
            theList = roadsWith;
        } else {
            theList = roadList;
        }
        for (String road : theList) {
            roadListModel.addElement(road);
        }
        roadCombo.addListSelectionListener(this);
        if (theList.size() == 1) {
            // we have only 1 item select it
            setOneRoad();

        }
    }

    private void closePage() {
        dispose();
        parentFrame.dispose();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(okayButton)) {
        String thisRoad =  roadCombo.getSelectedValue();
        String thisNumber =  numberCombo.getSelectedValue();
        if (thisRoad == null || thisNumber == null) {
            message.setText(Bundle.getMessage("AssociateNeedSelected"));
            return;
        }
        Car thisCar = carManager.getByRoadAndNumber(thisRoad, thisNumber);
        if (thisCar == null) {
            log.error("Car was pulled from combo but was not found {} {}", thisRoad, thisNumber);
            return;
        }
        if (e.getSource().equals(okayButton)) {
            log.debug("setting this tag ({}) to car {} with number {}", tag, thisRoad, thisNumber);
            tagManager.provideIdTag(tag);
            thisCar.setRfid(tag);
            closePage();
        }
        } else if (e.getSource().equals(cancelButton)){
            log.debug("closing the Associate panel");
            closePage();
        } else if (e.getSource().equals(includeCars)) {
            log.debug("the includeCars checkbox is changing - rebuilding lists");
            setRoads();

        } else {
            log.error("action performed for an unrecognized source");
            return;
        }

    }

    private void doNumbers(String road) {
        numberCombo.removeListSelectionListener(this);
        numberListModel.clear();
        List<String> numberList;
        if (includeCars.isSelected()) {
            numberList = numbersWith.get(road);
        } else {
            numberList = roadNumbers.get(road);
        }
        if (numberList == null) {
            log.error("didn't find the road in the list");
            return;
        }
        for (String thisNumber : numberList) {
            numberListModel.addElement(thisNumber);
        }
        numberCombo.addListSelectionListener(this);
        numberCombo.setEnabled(true);
        if (numberList.size() == 1) {
            numberCombo.setSelectedIndex(0);
            message.setText(Bundle.getMessage("AssociateOkayReady"));
        } else {
            message.setText("  ");
        }

    }


    @Override
    public void valueChanged(ListSelectionEvent e) {
        log.debug("got a list selection event");
        if (e.getValueIsAdjusting()) {
            return;
        }
        if (e.getSource().equals(roadCombo)) {
            numberCombo.setEnabled(false);
            numberCombo.removeListSelectionListener(this);
            okayButton.setEnabled(false);
            if (roadCombo.getSelectedIndex() == -1) {
                log.debug("no selection - turning off numbers combo");
                message.setText(" ");
                okayButton.setEnabled(false);
                numberCombo.setEnabled(false);
                numberListModel.clear();
            } else {
                doNumbers(roadCombo.getSelectedValue());
                message.setText(Bundle.getMessage("AssociateReady"));
                numberCombo.addListSelectionListener(this);
                numberCombo.setEnabled(true);
            }
        } else if (e.getSource().equals(numberCombo)) {
            if (numberCombo.getSelectedIndex() == -1 ) {
                log.debug("road number was deselected - turning off okay button");
                message.setText(Bundle.getMessage("AssociateReady"));
                okayButton.setEnabled(false);
            } {
                okayButton.setEnabled(true);
                message.setText(Bundle.getMessage("AssociateOkayReady"));
            }
        } else {
            log.error("don't recognize the source of the event");
        }
    }

}
