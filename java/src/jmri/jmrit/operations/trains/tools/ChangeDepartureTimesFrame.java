package jmri.jmrit.operations.trains.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Change Departure Time frame for operations.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2013
 */
public class ChangeDepartureTimesFrame extends OperationsFrame {

    // major buttons
    javax.swing.JButton changeButton = new javax.swing.JButton(Bundle.getMessage("Change"));

    // combo boxes
    javax.swing.JComboBox<Integer> hourBox = new javax.swing.JComboBox<>();

    javax.swing.JCheckBox routesCheckBox = new javax.swing.JCheckBox(Bundle.getMessage("ModifyRouteTimes"));

    public ChangeDepartureTimesFrame() {
        // general GUI config

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // load hour combobox
        for (int i = 1; i < 24; i++) {
            hourBox.addItem(i);
        }

        // row 2
        JPanel pHour = new JPanel();
        pHour.setLayout(new GridBagLayout());
        pHour.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectHours")));
        addItem(pHour, hourBox, 0, 0);
        addItem(pHour, routesCheckBox, 1, 0);

        // row 4
        JPanel pButton = new JPanel();
        pButton.add(changeButton);

        getContentPane().add(pHour);
        getContentPane().add(pButton);

        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_ChangeTrainDepartureTimes", true); // NOI18N

        pack();
        setMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight200));

        setTitle(Bundle.getMessage("TitleChangeDepartureTime"));

        // setup buttons
        addButtonAction(changeButton);
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == changeButton) {
            log.debug("change button activated");
            TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
            List<Train> trains = trainManager.getTrainsByIdList();
            for (Train train : trains) {
                train.setDepartureTime(adjustHour(train.getDepartureTimeHour()), train.getDepartureTimeMinute());
            }
            // now check every route to see if there are any departure times that need
            // adjustment
            if (routesCheckBox.isSelected()) {
                RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
                List<Route> routes = routeManager.getRoutesByNameList();
                for (Route route : routes) {
                    for (RouteLocation rl : route.getLocationsBySequenceList()) {
                        if (!rl.getDepartureTime().equals(RouteLocation.NONE))
                            rl.setDepartureTime(adjustHour(rl.getDepartureTimeHour()), rl.getDepartureTimeMinute());
                    }
                }
            }
        }
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
