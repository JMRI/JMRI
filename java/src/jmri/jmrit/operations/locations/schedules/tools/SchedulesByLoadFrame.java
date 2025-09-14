package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.tools.CarLoadEditFrameAction;
import jmri.jmrit.operations.rollingstock.cars.tools.PrintCarLoadsAction;
import jmri.jmrit.operations.setup.Control;

/**
 * Frame to display spurs with schedules and their loads
 *
 * @author Dan Boudreau Copyright (C) 2012, 2015, 2021, 2025
 */
public class SchedulesByLoadFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    // managers'
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);
    CarLoads carLoads = InstanceManager.getDefault(CarLoads.class);
    CarTypes carTypes = InstanceManager.getDefault(CarTypes.class);

    // combo box
    JComboBox<String> typesComboBox = carTypes.getComboBox();
    JComboBox<String> loadsComboBox = new JComboBox<>();

    // panels
    JPanel locationsPanel;

    // checkbox
    JCheckBox allLoadsCheckBox = new JCheckBox(Bundle.getMessage("allLoads"));
    JCheckBox allTypesCheckBox = new JCheckBox(Bundle.getMessage("allTypes"));

    public SchedulesByLoadFrame() {
        super(Bundle.getMessage("MenuItemShowSchedulesByLoad"));

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // load the panel
        JPanel p1 = new JPanel();
        p1.setMaximumSize(new Dimension(2000, 200));
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

        JPanel type = new JPanel();
        type.setLayout(new GridBagLayout());
        type.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
        addItem(type, typesComboBox, 0, 0);
        addItem(type, allTypesCheckBox, 1, 0);

        JPanel load = new JPanel();
        load.setLayout(new GridBagLayout());
        load.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Load")));
        addItem(load, loadsComboBox, 0, 0);
        addItem(load, allLoadsCheckBox, 1, 0);

        p1.add(type);
        p1.add(load);

        locationsPanel = new JPanel();
        locationsPanel.setLayout(new GridBagLayout());
        JScrollPane locationsPane = new JScrollPane(locationsPanel);
        locationsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        locationsPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));

        getContentPane().add(p1);
        getContentPane().add(locationsPane);

        addComboBoxAction(typesComboBox);
        addComboBoxAction(loadsComboBox);

        addCheckBoxAction(allTypesCheckBox);
        addCheckBoxAction(allLoadsCheckBox);

        // property changes
        locationManager.addPropertyChangeListener(this);
        carTypes.addPropertyChangeListener(this);
        carLoads.addPropertyChangeListener(this);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new CarLoadEditFrameAction());
        toolMenu.addSeparator();
        toolMenu.add(new PrintCarLoadsAction(true));
        toolMenu.add(new PrintCarLoadsAction(false));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_ShowSchedulesByCarTypeAndLoad", true); // NOI18N

        // select first item to load contents
        typesComboBox.setSelectedIndex(0);

        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight250));
    }

    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == typesComboBox) {
            updateLoadComboBox();
        }
        if (ae.getSource() == loadsComboBox) {
            updateLocations();
        }

    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        typesComboBox.setEnabled(!allTypesCheckBox.isSelected());
        loadsComboBox.setEnabled(!allLoadsCheckBox.isSelected());
        updateLoadComboBox();
        updateLocations();
    }

    private void updateLoadComboBox() {
        if (allTypesCheckBox.isSelected()) {
            carLoads.updateComboBox(loadsComboBox);
        } else if (typesComboBox.getSelectedItem() != null) {
            String type = (String) typesComboBox.getSelectedItem();
            carLoads.updateComboBox(type, loadsComboBox);
        }
    }

    private void updateLocations() {
        String type = (String) typesComboBox.getSelectedItem();
        String load = (String) loadsComboBox.getSelectedItem();
        log.debug("Update locations for type ({}) load ({})", type, load);
        locationsPanel.removeAll();
        int x = 0;
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("Spur")), 1, x);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("Schedule")), 2, x);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("receiveTypeLoad")), 3, x);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("shipLoad")), 4, x);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("destinationTrack")), 5, x++);

        // determine if load is default empty or load
        boolean defaultLoad = carLoads.getDefaultLoadName().equals(load) || carLoads.getDefaultEmptyName().equals(load);

        for (Location location : locationManager.getLocationsByNameList()) {
            // only spurs have schedules
            if (!location.hasSpurs())
                continue;
            addItemLeft(locationsPanel, new JLabel(location.getName()), 0, x++);
            // now look for a spur with a schedule
            for (Track spur : location.getTracksByNameList(Track.SPUR)) {
                Schedule sch = spur.getSchedule();
                if (sch == null) {
                    continue;
                }
                // listen for changes
                spur.removePropertyChangeListener(this);
                spur.addPropertyChangeListener(this);
                sch.removePropertyChangeListener(this);
                sch.addPropertyChangeListener(this);

                // determine if schedule is requesting car type and load
                for (ScheduleItem si : sch.getItemsBySequenceList()) {
                    // skip if car type doesn't carry load name
                    if (allTypesCheckBox.isSelected() &&
                            !allLoadsCheckBox.isSelected() &&
                            !carLoads.containsName(si.getTypeName(), load)) {
                        continue;
                    }
                    if ((allTypesCheckBox.isSelected() || si.getTypeName().equals(type)) &&
                            (allLoadsCheckBox.isSelected() ||
                                    si.getReceiveLoadName().equals(load) ||
                                    si.getReceiveLoadName().equals(ScheduleItem.NONE) ||
                                    si.getShipLoadName().equals(load) ||
                                    (si.getShipLoadName().equals(ScheduleItem.NONE) && defaultLoad))) {
                        // is the schedule item valid?
                        String status = spur.checkScheduleValid();
                        if (!status.equals(Schedule.SCHEDULE_OKAY)) {
                            addItemLeft(locationsPanel, new JLabel("  " + status), 0, x);
                        }
                        addItemLeft(locationsPanel, new JLabel(spur.getName()), 1, x);
                        addItemLeft(locationsPanel, new JLabel(spur.getScheduleName()), 2, x);
                        // create string Receive(type, delivery, road, load)
                        String s = si.getTypeName() +
                                ", " +
                                si.getSetoutTrainScheduleName() +
                                ", " +
                                si.getRoadName() +
                                ", " +
                                si.getReceiveLoadName();
                        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("Receive") + " (" + s + ")"), 3, x);
                        // create string Ship(load, pickup)
                        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage(
                                "Ship") + " (" + si.getShipLoadName() + ", " + si.getPickupTrainScheduleName() + ")"),
                                4, x++);
                        // now the destination and track
                        if (si.getDestination() != null) {
                            addItemLeft(locationsPanel,
                                    new JLabel(si.getDestinationName() + " (" + si.getDestinationTrackName() + ")"), 5,
                                    x - 1);
                        }
                        // report if spur can't service the selected load
                        if (!allLoadsCheckBox.isSelected() &&
                                si.getReceiveLoadName().equals(ScheduleItem.NONE) &&
                                !spur.isLoadNameAndCarTypeAccepted(load, type)) {
                            addItemLeft(locationsPanel,
                                    new JLabel(Bundle.getMessage("spurNotTypeLoad", spur.getName(), type, load)), 3,
                                    x++);
                        }
                    }
                }
            }
        }
        locationsPanel.revalidate();
        revalidate();
        repaint();
    }

    @Override
    public void dispose() {
        locationManager.removePropertyChangeListener(this);
        carTypes.removePropertyChangeListener(this);
        carLoads.removePropertyChangeListener(this);
        for (Track spur : locationManager.getTracks(Track.SPUR)) {
            Schedule sch = spur.getSchedule();
            if (sch == null) {
                continue;
            }
            spur.removePropertyChangeListener(this);
            sch.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("Property change ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e.getNewValue()); // NOI18N

        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY)) {
            carTypes.updateComboBox(typesComboBox);
        }
        if (e.getSource().getClass().equals(CarLoads.class)) {
            carLoads.updateComboBox((String) typesComboBox.getSelectedItem(), loadsComboBox);
        }
        if (e.getSource().getClass().equals(Schedule.class) ||
                e.getSource().getClass().equals(LocationManager.class) ||
                e.getPropertyName().equals(Track.LOADS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY)) {
            updateLocations();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SchedulesByLoadFrame.class);

}
