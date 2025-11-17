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
 * Frame to display the staging tracks and spurs with schedules. Lists staging
 * tracks that will service the car type and the spurs that will accept the
 * selected load name.
 *
 * @author Dan Boudreau Copyright (C) 2023
 */
public class SchedulesAndStagingFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

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
    JCheckBox generatedLoadsCheckBox = new JCheckBox(Bundle.getMessage("generatedLoads"));
    JCheckBox allLoadsCheckBox = new JCheckBox(Bundle.getMessage("allLoads"));

    public SchedulesAndStagingFrame() {
        super(Bundle.getMessage("MenuItemStagingSchedules"));

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

        JPanel load = new JPanel();
        load.setLayout(new GridBagLayout());
        load.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Load")));
        addItem(load, loadsComboBox, 0, 0);
        addItem(load, generatedLoadsCheckBox, 1, 0);
        addItem(load, allLoadsCheckBox, 2, 0);

        p1.add(type);
        p1.add(load);

        locationsPanel = new JPanel();
        locationsPanel.setLayout(new GridBagLayout());
        JScrollPane locationsPane = new JScrollPane(locationsPanel);
        locationsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        locationsPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Staging")));

        getContentPane().add(p1);
        getContentPane().add(locationsPane);

        addComboBoxAction(typesComboBox);
        addComboBoxAction(loadsComboBox);

        generatedLoadsCheckBox.setSelected(true);
        generatedLoadsCheckBox.setToolTipText(Bundle.getMessage("generatedLoadsTip"));

        addCheckBoxAction(generatedLoadsCheckBox);
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
        addHelpMenu("package.jmri.jmrit.operations.Operations_ShowStagingAndSchedulesByCarTypeAndLoad", true); // NOI18N

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
        loadsComboBox.setEnabled(!allLoadsCheckBox.isSelected());
        updateLocations();
    }

    private void updateLoadComboBox() {
        String type = (String) typesComboBox.getSelectedItem();
        carLoads.updateComboBox(type, loadsComboBox);
    }

    int x;

    private void updateLocations() {
        locationsPanel.removeAll();
        // create header
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("Track")), 1, 0);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("CarLoadOptions")), 2, 0);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("ShipLoadOption")), 3, 0);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("Load")), 4, 0);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("destinationTrack")), 5, 0);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("LoadOption")), 6, 0);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("Schedule")), 7, 0);

        x = 1;
        for (Location location : locationManager.getLocationsByNameList()) {
            if (!location.isStaging())
                continue;
            location.removePropertyChangeListener(this);
            location.addPropertyChangeListener(this);
            addItemLeft(locationsPanel, new JLabel(location.getName()), 0, x++);
            // list staging tracks
            for (Track track : location.getTracksByNameList(Track.STAGING)) {
                // listen for changes
                track.removePropertyChangeListener(this);
                track.addPropertyChangeListener(this);
                addItemLeft(locationsPanel, new JLabel(track.getName()), 1, x);
                addItemLeft(locationsPanel, new JLabel(getTrackCarLoadOptions(track)), 2, x);
                addItemLeft(locationsPanel, new JLabel(track.getShipLoadOptionString()), 3, x);
                listSpurs(track);
                x++;
            }
        }
        locationsPanel.revalidate();
        revalidate();
        repaint();
    }

    private String getTrackCarLoadOptions(Track track) {
        StringBuffer options = new StringBuffer();
        if (track.isLoadSwapEnabled()) {
            options.append(Bundle.getMessage("ABV_SwapDefaultLoads") + " ");
        }
        if (track.isLoadEmptyEnabled()) {
            options.append(Bundle.getMessage("ABV_EmptyDefaultLoads") + " ");
        }
        if (track.isRemoveCustomLoadsEnabled()) {
            options.append(Bundle.getMessage("ABV_EmptyCustomLoads") + " ");
        }
        if (track.isAddCustomLoadsEnabled()) {
            options.append(Bundle.getMessage("ABV_GenerateCustomLoad") + " ");
        }
        if (track.isAddCustomLoadsAnySpurEnabled()) {
            options.append(Bundle.getMessage("ABV_GenerateCustomLoadAnySpur") + " ");
        }
        if (track.isAddCustomLoadsAnyStagingTrackEnabled()) {
            options.append(Bundle.getMessage("ABV_GereateCustomLoadStaging"));
        }
        return options.toString();
    }

    /*
     * List spurs that this staging track can service
     */
    private void listSpurs(Track track) {
        // is this staging track configured to generate custom loads?
        if (!track.isAddCustomLoadsAnySpurEnabled() && !track.isAddCustomLoadsEnabled()) {
            return;
        }
        String type = (String) typesComboBox.getSelectedItem();
        if (!track.isTypeNameAccepted(type)) {
            return;
        }
        if (allLoadsCheckBox.isSelected()) {
            for (String load : carLoads.getNames(type)) {
                listSpurs(track, type, load);
            }
        } else {
            String load = (String) loadsComboBox.getSelectedItem();
            listSpurs(track, type, load);
        }
    }

    private void listSpurs(Track track, String type, String load) {
        if (load == null || !track.isLoadNameAndCarTypeShipped(load, type)) {
            return;
        }
        // ignore default empty and load names
        if (generatedLoadsCheckBox.isSelected() &&
                (load.equals(carLoads.getDefaultEmptyName()) || load.equals(carLoads.getDefaultLoadName()))) {
            return;
        }
        // now list all of the spurs with schedules for this type and load
        for (Location location : locationManager.getLocationsByNameList()) {
            // only spurs have schedules
            if (!location.hasSpurs())
                continue;
            // find spurs with a schedule
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
                if (spur.isLoadNameAndCarTypeAccepted(load, type)) {
                    for (ScheduleItem si : sch.getItemsBySequenceList()) {
                        if (si.getTypeName().equals(type) &&
                                (si.getReceiveLoadName().equals(load) ||
                                        (si.getReceiveLoadName().equals(ScheduleItem.NONE) &&
                                                !generatedLoadsCheckBox.isSelected()))) {
                            addItemLeft(locationsPanel, new JLabel(load), 4, x);
                            addItemLeft(locationsPanel, new JLabel(location.getName() + " (" + spur.getName() + ")"), 5,
                                    x);
                            addItemLeft(locationsPanel, new JLabel(spur.getLoadOptionString()), 6, x);
                            addItemLeft(locationsPanel, new JLabel(sch.getName() + " " + si.getId()), 7, x++);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void dispose() {
        locationManager.removePropertyChangeListener(this);
        carTypes.removePropertyChangeListener(this);
        carLoads.removePropertyChangeListener(this);
        for (Location location : locationManager.getLocationsByNameList()) {
            location.removePropertyChangeListener(this);
        }
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
                e.getPropertyName().equals(Track.LOAD_OPTIONS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY)) {
            updateLocations();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SchedulesAndStagingFrame.class);

}
