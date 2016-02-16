// SchedulesByLoadFrame.java
package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.PrintCarLoadsAction;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.TrainScheduleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame to display spurs with schedules and their loads
 *
 * @author Dan Boudreau Copyright (C) 2012, 2015
 * @version $Revision: 17977 $
 */
public class SchedulesByLoadFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    // combo box
    JComboBox<String> typesComboBox = CarTypes.instance().getComboBox();
    JComboBox<String> loadsComboBox = new JComboBox<>();

    // panels
    JPanel locationsPanel;

    // checkbox
    JCheckBox allLoadsCheckBox = new JCheckBox(Bundle.getMessage("allLoads"));
    JCheckBox allTypesCheckBox = new JCheckBox(Bundle.getMessage("allTypes"));

    // managers'
    LocationManager locationManager = LocationManager.instance();

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
        locationsPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Locations")));

        getContentPane().add(p1);
        getContentPane().add(locationsPane);

        addComboBoxAction(typesComboBox);
        addComboBoxAction(loadsComboBox);

        addCheckBoxAction(allTypesCheckBox);
        addCheckBoxAction(allLoadsCheckBox);

        // property changes
        locationManager.addPropertyChangeListener(this);
        CarTypes.instance().addPropertyChangeListener(this);
        CarLoads.instance().addPropertyChangeListener(this);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
        toolMenu.add(new PrintCarLoadsAction(Bundle.getMessage("MenuItemCarLoadsPreview"), true, this));
        toolMenu.add(new PrintCarLoadsAction(Bundle.getMessage("MenuItemCarLoadsPrint"), false, this));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_ShowSchedulesByCarTypeAndLoad", true); // NOI18N

        // select first item to load contents
        typesComboBox.setSelectedIndex(0);

        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight250));
    }

    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == typesComboBox) {
            updateLoadComboBox();
        }
        if (ae.getSource() == loadsComboBox) {
            updateLocations();
        }

    }

    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        typesComboBox.setEnabled(!allTypesCheckBox.isSelected());
        loadsComboBox.setEnabled(!allLoadsCheckBox.isSelected());
        updateLoadComboBox();
        updateLocations();
    }

    private void updateLoadComboBox() {
        if (allTypesCheckBox.isSelected()) {
            CarLoads.instance().updateComboBox(loadsComboBox);
        }
        else if (typesComboBox.getSelectedItem() != null) {
            String type = (String) typesComboBox.getSelectedItem();
            CarLoads.instance().updateComboBox(type, loadsComboBox);
        }
    }

    private void updateLocations() {
        String type = (String) typesComboBox.getSelectedItem();
        String load = (String) loadsComboBox.getSelectedItem();
        log.debug("Update locations for type ({}) load ({})", type, load);
        locationsPanel.removeAll();
        int x = 0;
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("trackSchedule")), 1, x);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("receiveTypeLoad")), 2, x);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("shipLoad")), 3, x);
        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("destinationTrack")), 4, x++);

        for (Location location : locationManager.getLocationsByNameList()) {
            // only spurs have schedules
            if (!location.hasSpurs())
                continue;
            addItemLeft(locationsPanel, new JLabel(location.getName()), 0, x++);
            // now look for a spur with a schedule
            for (Track spur : location.getTrackByNameList(Track.SPUR)) {
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
                    if ((allTypesCheckBox.isSelected() || si.getTypeName().equals(type))
                            && (allLoadsCheckBox.isSelected()
                                    || si.getReceiveLoadName().equals(load)
                                    || si.getReceiveLoadName().equals(ScheduleItem.NONE)
                                    || si.getShipLoadName().equals(load)
                                    || si.getShipLoadName().equals(ScheduleItem.NONE))) {
                        // is the schedule item valid?
                        String status = spur.checkScheduleValid();
                        if (!status.equals(Track.SCHEDULE_OKAY)) {
                            addItemLeft(locationsPanel, new JLabel("  " + status), 0, x);
                        }
                        addItemLeft(locationsPanel,
                                new JLabel(spur.getName() + " (" + spur.getScheduleName() + ")"), 1, x);
                        // create string Receive(type, delivery, road, load)
                        String s = si.getTypeName();
                        if (!si.getSetoutTrainScheduleId().equals(ScheduleItem.NONE)
                                && TrainScheduleManager.instance().getScheduleById(si.getSetoutTrainScheduleId()) != null) {
                            s = s  + ", " + TrainScheduleManager.instance().getScheduleById(si.getSetoutTrainScheduleId()).getName();
                        } else {
                            s = s + ",";
                        }
                        if (!si.getRoadName().equals(ScheduleItem.NONE)) {
                            s = s + ", " + si.getRoadName();
                        } else {
                            s = s + ",";
                        }
                        s = s + ", " + si.getReceiveLoadName();
                        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("Receive") + " (" + s + ")"), 2, x);
                        // create string Ship(load, pickup)
                        s = "";
                        if (!si.getPickupTrainScheduleId().equals(ScheduleItem.NONE)
                                && TrainScheduleManager.instance().getScheduleById(si.getPickupTrainScheduleId()) != null) {
                            s = ", "+ TrainScheduleManager.instance().getScheduleById(si.getPickupTrainScheduleId()).getName();
                        }
                        addItemLeft(locationsPanel, new JLabel(Bundle.getMessage("Ship") +
                                " (" + si.getShipLoadName() + s + ")"), 3, x++);
                        // now the destination and track
                        if (si.getDestination() != null) {
                            addItemLeft(locationsPanel, new JLabel(si.getDestinationName() + " ("
                                    + si.getDestinationTrackName() + ")"), 4, x - 1);
                        }
                        // report if spur can't service the selected load
                        if (!allLoadsCheckBox.isSelected() &&
                                si.getReceiveLoadName().equals(ScheduleItem.NONE) &&
                                !spur.acceptsLoad(load, type)) {
                            addItemLeft(locationsPanel, new JLabel(MessageFormat.format(Bundle.getMessage("spurNotTypeLoad"),
                                    new Object[]{spur.getName(), type, load})), 2, x++);
                        }
                    }
                }
            }
        }
        locationsPanel.revalidate();
        revalidate();
        repaint();
    }

    public void dispose() {
        locationManager.removePropertyChangeListener(this);
        CarTypes.instance().removePropertyChangeListener(this);
        CarLoads.instance().removePropertyChangeListener(this);
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

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled())
            log.debug("Property change ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e.getNewValue()); // NOI18N

        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY)) {
            CarTypes.instance().updateComboBox(typesComboBox);
        }
        if (e.getSource().getClass().equals(CarLoads.class)) {
            CarLoads.instance().updateComboBox((String) typesComboBox.getSelectedItem(), loadsComboBox);
        }
        if (e.getSource().getClass().equals(Schedule.class) || e.getSource().getClass().equals(LocationManager.class)
                || e.getPropertyName().equals(Track.LOADS_CHANGED_PROPERTY)) {
            updateLocations();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LocationsByCarTypeFrame.class.getName());

}
