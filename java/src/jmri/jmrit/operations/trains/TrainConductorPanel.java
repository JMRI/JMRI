// TrainConductorPanel.java
package jmri.jmrit.operations.trains;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import jmri.jmrit.operations.CommonConductorYardmasterPanel;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conductor Panel. Shows work for a train one location at a time.
 *
 * @author Dan Boudreau Copyright (C) 2011, 2013
 * @version $Revision: 18630 $
 */
public class TrainConductorPanel extends CommonConductorYardmasterPanel {

    protected static final boolean IS_MANIFEST = true;

    // labels
    JLabel textTrainName = new JLabel();
    JLabel textTrainDepartureTime = new JLabel();
    JLabel textNextLocationName = new JLabel();

    // panels
    JPanel pTrainDepartureTime = new JPanel();

    // major buttons
    /**
     * Default constructor required to use as JavaBean.
     */
    public TrainConductorPanel() {
        this(null);
    }

    public TrainConductorPanel(Train train) {
        super();
        initComponents();

        _train = train;

        // row 2
        JPanel pRow2 = new JPanel();
        pRow2.setLayout(new BoxLayout(pRow2, BoxLayout.X_AXIS));

        // row 2a (train name)
        JPanel pTrainName = new JPanel();
        pTrainName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Train")));
        pTrainName.add(textTrainName);

        pRow2.add(pTrainName);
        pRow2.add(pTrainDescription);
        pRow2.add(pRailRoadName);

        JPanel pLocation = new JPanel();
        pLocation.setLayout(new BoxLayout(pLocation, BoxLayout.X_AXIS));

        // row 10b (train departure time)
        pTrainDepartureTime.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("DepartTime")));
        pTrainDepartureTime.add(textTrainDepartureTime);

        // row 10c (next location name)
        JPanel pNextLocationName = new JPanel();
        pNextLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("NextLocation")));
        pNextLocationName.add(textNextLocationName);

        pLocation.add(pLocationName); // location name
        pLocation.add(pTrainDepartureTime);
        pLocation.add(pNextLocationName);

        // row 14
        JPanel pRow14 = new JPanel();
        pRow14.setLayout(new BoxLayout(pRow14, BoxLayout.X_AXIS));
        pRow14.setMaximumSize(new Dimension(2000, 200));

        // row 14b
        JPanel pMoveButton = new JPanel();
        pMoveButton.setLayout(new GridBagLayout());
        pMoveButton.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Train")));
        addItem(pMoveButton, moveButton, 1, 0);

        pRow14.add(pButtons);
        pRow14.add(pMoveButton);

        update();

        add(pRow2);
        add(pLocation);
        add(textTrainCommentPane);
        add(textTrainRouteCommentPane); // train route comment
        add(textTrainRouteLocationCommentPane); // train route location comment
        add(textLocationCommentPane);
        add(locoPane);
        add(pWorkPanes);
        add(movePane);
        add(pStatus);
        add(pRow14);

        // setup buttons
        addButtonAction(moveButton);

        if (_train != null) {
            textTrainDescription.setText(_train.getDescription());
            // show train comment box only if there's a comment
            if (_train.getComment().equals(Train.NONE)) {
                textTrainCommentPane.setVisible(false);
            } else {
                textTrainCommentPane.setText(_train.getComment());
            }
            // show route comment box only if there's a route comment
            if (_train.getRoute() != null) {
                textTrainRouteCommentPane.setVisible(!_train.getRoute().getComment().equals(Route.NONE)
                        && Setup.isPrintRouteCommentsEnabled());
                textTrainRouteCommentPane.setText(_train.getRoute().getComment());
            }

            // Does this train have a unique railroad name?
            if (!_train.getRailroadName().equals(Train.NONE)) {
                textRailRoadName.setText(_train.getRailroadName());
            } else {
                textRailRoadName.setText(Setup.getRailroadName());
            }

            // listen for train changes
            _train.addPropertyChangeListener(this);
        }

        setVisible(true);

    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == moveButton) {
            _train.move();
            return;
        }
        super.buttonActionPerformed(ae);
        update();
    }

    private void clearAndUpdate() {
        trainCommon.clearUtilityCarTypes(); // reset the utility car counts
        carCheckBoxes.clear();
        isSetMode = false;
        update();
    }

    private void update() {
        log.debug("queue update");
        // use invokeLater to prevent deadlock
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                log.debug("update, setMode " + isSetMode);
                initialize();
                if (_train != null && _train.getRoute() != null) {
                    textTrainName.setText(_train.getIconName());
                    RouteLocation rl = _train.getCurrentLocation();
                    if (rl != null) {
                        textTrainRouteLocationCommentPane.setVisible(!rl.getComment().equals(RouteLocation.NONE));
                        textTrainRouteLocationCommentPane.setText(rl.getComment());
                        textLocationName.setText(rl.getLocation().getName());
                        pTrainDepartureTime.setVisible(_train.isShowArrivalAndDepartureTimesEnabled()
                                && !rl.getDepartureTime().equals(RouteLocation.NONE));
                        textTrainDepartureTime.setText(rl.getFormatedDepartureTime());
                        textLocationCommentPane.setVisible(!rl.getLocation().getComment().equals(Location.NONE)
                                && Setup.isPrintLocationCommentsEnabled());
                        textLocationCommentPane.setText(rl.getLocation().getComment());
                        textNextLocationName.setText(_train.getNextLocationName());

                        // check for locos
                        updateLocoPanes(rl);

                        // now update the car pick ups and set outs
                        blockCars(rl, IS_MANIFEST);

                        textStatus.setText(getStatus(rl, IS_MANIFEST));

                    } else {
                        moveButton.setEnabled(false);
                        setButton.setEnabled(false);
                    }
                    updateComplete();
                }
            }
        });
    }

    @Override
    public void dispose() {
        removePropertyChangeListerners();
        if (_train != null) {
            _train.removePropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.showProperty) {
            log.debug("Property change ({}) for: ({}) old: {} new: {}",
                    e.getPropertyName(), e.getSource().toString(),
                    e.getOldValue(), e.getNewValue());
        }
        if (e.getPropertyName().equals(Train.TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY)) {
            clearAndUpdate();
        }
        if ((e.getPropertyName().equals(RollingStock.ROUTE_LOCATION_CHANGED_PROPERTY) && e.getNewValue() == null)
                || (e.getPropertyName().equals(RollingStock.ROUTE_DESTINATION_CHANGED_PROPERTY) && e
                .getNewValue() == null)
                || e.getPropertyName().equals(RollingStock.TRAIN_CHANGED_PROPERTY)) {
            // remove car from list
            if (e.getSource().getClass().equals(Car.class)) {
                Car car = (Car) e.getSource();
                carCheckBoxes.remove("p" + car.getId());
                carCheckBoxes.remove("s" + car.getId());
                carCheckBoxes.remove("m" + car.getId());
                log.debug("Car ({}) removed from list", car.toString());
            }
            update();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainConductorPanel.class.getName());
}
