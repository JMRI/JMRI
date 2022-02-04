package jmri.jmrix.rfid.swing.tagcarwin;

import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
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
//    private final JList<String> roadCombo = new JList<>(roadListModel);
//    private final JList<String> numberCombo = new JList<>(numberListModel);
    private final JList<String> roadCombo = new JList<>(roadListModel);
    private final JList<String> numberCombo = new JList<>(numberListModel);


    private final JCheckBox includeCars = new JCheckBox();
    private final JLabel message = new JLabel("");
    private final String includeCarsString = this.getClass().getName() + "IncludeAllCars";
    private final ArrayList<String> roadList = new ArrayList<>();
    private final ArrayList<String> roadsWith = new ArrayList<>();
    private final Hashtable<String, List<String>> numbersWith = new Hashtable<>();
    private final Hashtable<String, List<String>> roadNumbers = new Hashtable<>();
    CarManager carManager = InstanceManager.getDefault(CarManager.class);
    IdTagManager tagManager = InstanceManager.getDefault(IdTagManager.class);

    public void setParentFrame(JmriJFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    JmriJFrame parentFrame;

    public AssociateTag(String thisTag) {
        super();
        tag = thisTag;
        this.parentFrame = parentFrame;
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

        JLabel someText = new JLabel("This is some text");
        this.add(someText, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(5, 5,5, 5), 6, 6));
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new GridBagLayout());
        topPanel.setPreferredSize(new Dimension(350, 250));
        topPanel.setMinimumSize(new Dimension(300, 200));

        GridBagConstraints topPanelConstraints = new GridBagConstraints(0, 2, 3, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10),
                10, 10);
        this.add(topPanel, topPanelConstraints);

        UserPreferencesManager pm = InstanceManager.getDefault(UserPreferencesManager.class);
        includeCars.setSelected(pm.getSimplePreferenceState(includeCarsString));
        GridBagConstraints c1 = new GridBagConstraints();
        c1.anchor = GridBagConstraints.LINE_START;
        c1.fill = GridBagConstraints.BOTH;
        c1.gridx = 0;
        c1.gridy = 0;
        JScrollPane roadScroll = new JScrollPane();
        roadScroll.setMinimumSize(new Dimension(75, 75));
        roadScroll.setViewportView(roadCombo);
        roadCombo.setVisibleRowCount(-1);
        roadCombo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roadCombo.setLayoutOrientation(JList.VERTICAL);
        roadCombo.addListSelectionListener(this);
        roadCombo.setMinimumSize(new Dimension(75, 75));
        setRoads(roadListModel);
        topPanel.add(roadScroll, c1);

        JScrollPane numberScroll = new JScrollPane();
        numberScroll.setViewportView(numberCombo);
        numberScroll.setMinimumSize(new Dimension(75, 75));
        numberCombo.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        numberCombo.setVisibleRowCount(-1);
        numberCombo.setLayoutOrientation(JList.VERTICAL);
        numberCombo.setMinimumSize(new Dimension(75, 75));
        numberCombo.setEnabled(false);
        GridBagConstraints c2 = new GridBagConstraints();
        c2.anchor = GridBagConstraints.LINE_END;
        c2.gridx = 3;
        c2.gridy = 0;
        c2.fill = GridBagConstraints.BOTH;
        topPanel.add(numberScroll, c2);
        this.add(topPanel);
        GridBagConstraints c3 = new GridBagConstraints();
        c3.anchor = GridBagConstraints.LINE_END;
        c3.fill = GridBagConstraints.HORIZONTAL;
        c3.gridx = 0;
        c3.gridy = 2;
        JLabel tagLabel = new JLabel();
        tagLabel.setText(Bundle.getMessage("AssociateTag"));
        this.add(tagLabel, c3);

        GridBagConstraints c4 = new GridBagConstraints();
        c4.anchor = GridBagConstraints.LINE_START;
        c4.fill = GridBagConstraints.HORIZONTAL;
        c4.gridy = 2;
        c4.gridx = 1;
        JLabel thisTag = new JLabel(tag);
        this.add(thisTag,c4);

        GridBagConstraints c5 = new GridBagConstraints();
        c5.fill = GridBagConstraints.HORIZONTAL;
        c5.gridx = 0;
        c5.gridy = 3;
        includeCars.setText(Bundle.getMessage("AssociateIncludeAll"));
        includeCars.setToolTipText(Bundle.getMessage("AssociateAllToolTip"));
        this.add(includeCars, c5);



        GridBagConstraints c6 = new GridBagConstraints();
        c6.fill = GridBagConstraints.NONE;
        c6.gridy = 4;
        c6.gridx = 0;
        this.add(message, c6);

        okayButton.setText(Bundle.getMessage("AssociateOkay"));
        okayButton.addActionListener(this);
        okayButton.setEnabled(false);
        GridBagConstraints c7 = new GridBagConstraints();
        c7.fill = GridBagConstraints.BOTH;
        c7.gridx = 0;
        c7.gridy = 5;
        this.add(okayButton, c7);

        cancelButton.setText(Bundle.getMessage("AssociateCancel"));
        cancelButton.addActionListener(this);
        GridBagConstraints c8 = new GridBagConstraints();
        c8.fill = GridBagConstraints.BOTH;
        c8.gridy = 5;
        c8.gridx = 3;
        this.add(cancelButton, c8);

        if (roadList.size() < 1 && !includeCars.isSelected()) {
            message.setText(Bundle.getMessage("AssociateNoRoads"));
        } else if (roadList.size() == 1) {
            doNumbers(roadList.get(0));
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
            java.util.Collections.sort(roadNumbers.get(road));
        }
        for (String road : roadsWith) {
            java.util.Collections.sort(numbersWith.get(road));
        }
    }

    private void setRoads(DefaultListModel<String> carInfo) {
        carInfo.clear();
        List<String> theList;
        if (includeCars.isSelected()) {
            theList = roadsWith;
        } else {
            theList = roadList;
        }
        for (String road : theList) {
            carInfo.addElement(road);
        }
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
        }
        } else if (!e.getSource().equals(cancelButton)){
            log.error("got an action message which we don't recognize");
            return;
        }
        dispose();
        parentFrame.dispose();
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
        for (String thisNumber : numberList) {
            numberListModel.addElement(thisNumber);
        }
        numberCombo.addListSelectionListener(this);
        numberCombo.setEnabled(true);
        if (numberList.size() == 1) {
            numberCombo.setSelectedIndex(0);
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
            if (roadCombo.getSelectedIndex() == -1) {
                log.debug("no selection - turning off numbers combo");
            } else {
                doNumbers(roadCombo.getSelectedValue());
                numberCombo.addListSelectionListener(this);
                numberCombo.setEnabled(true);
            }
        } else if (e.getSource().equals(numberCombo)) {
            if (numberCombo.getSelectedIndex() == -1 ) {
                log.debug("road number was deselected - turning off okay button");
                okayButton.setEnabled(false);
            } {
                okayButton.setEnabled(true);
            }
        } else {
            log.error("don't recognize the source of the event");
        }
    }

}
