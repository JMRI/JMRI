package jmri.jmrit.operations.trains.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsPanel;
import jmri.jmrit.operations.routes.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * Change Departure Time frame for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2013, 2026
 */
public class ChangeDepartureTimesFrame extends OperationsFrame {

    // major buttons
    JButton changeButton = new JButton(Bundle.getMessage("Change"));

    // combo boxes
    JComboBox<Integer> hourBox = new JComboBox<>();
    JComboBox<Integer> dayBox = new JComboBox<>();

    JCheckBox routesCheckBox = new JCheckBox(Bundle.getMessage("ModifyRouteTimes"));

    public ChangeDepartureTimesFrame() {
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // load day combobox
        for (int i = 0; i < Control.numberOfDays; i++) {
            dayBox.addItem(i);
        }

        // load hour combobox
        for (int i = 0; i < 24; i++) {
            hourBox.addItem(i);
        }

        OperationsPanel.padComboBox(dayBox, 3);
        OperationsPanel.padComboBox(hourBox, 3);

        // row 2
        JPanel pHour = new JPanel();
        pHour.setLayout(new GridBagLayout());
        pHour.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectHours")));
        addItem(pHour, dayBox, 0, 0);
        addItem(pHour, hourBox, 1, 0);
        addItem(pHour, routesCheckBox, 2, 0);

        // row 4
        JPanel pButton = new JPanel();
        pButton.add(changeButton);

        getContentPane().add(pHour);
        getContentPane().add(pButton);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_ChangeTrainDepartureTimes", true); // NOI18N

        setTitle(Bundle.getMessage("TitleChangeDepartureTime"));

        // setup buttons
        addButtonAction(changeButton);

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight200));
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == changeButton) {
            log.debug("change button activated");
            TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
            if (trainManager.isAnyTrainBuilt()) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("CanNotChangeTime"),
                        Bundle.getMessage("MustTerminateOrReset"), JmriJOptionPane.ERROR_MESSAGE);
            } else {
                List<Train> trains = trainManager.getTrainsByIdList();
                for (Train train : trains) {
                    train.setDepartureTime(adjustDay(train.getDepartureTimeDay()),
                            adjustHour(train.getDepartureTimeHour()), train.getDepartureTimeMinute());
                }
                // now check every route to see if there are any departure times that need
                // adjustment
                if (routesCheckBox.isSelected()) {
                    RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
                    List<Route> routes = routeManager.getRoutesByNameList();
                    for (Route route : routes) {
                        for (RouteLocation rl : route.getLocationsBySequenceList()) {
                            if (!rl.getDepartureTimeHourMinutes().equals(RouteLocation.NONE)) {
                                rl.setDepartureTime(adjustDay(rl.getDepartureTimeDay()),
                                        adjustHour(rl.getDepartureTimeHour()), rl.getDepartureTimeMinute());
                            }
                        }
                    }
                }
            }
        }
    }

    private String adjustDay(String day) {
        int d = (int) dayBox.getSelectedItem() + Integer.parseInt(day);
        if (d > Control.numberOfDays - 1) {
            d = d - Control.numberOfDays;
        }
        return Integer.toString(d);
    }

    private String adjustHour(String time) {
        int hour = (int) hourBox.getSelectedItem() + Integer.parseInt(time);
        if (hour > 23) {
            hour = hour - 24;
        }
        return Integer.toString(hour);
    }

    private final static Logger log = LoggerFactory.getLogger(ChangeDepartureTimesFrame.class);
}
