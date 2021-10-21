package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.CommonConductorYardmasterPanel;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Yardmaster Frame. Shows work at one location.
 *
 * @author Dan Boudreau Copyright (C) 2013
 * 
 */
public class YardmasterPanel extends CommonConductorYardmasterPanel {

    int _visitNumber = 1;

    // combo boxes
    JComboBox<Train> trainComboBox = new JComboBox<>();
    JComboBox<Integer> trainVisitComboBox = new JComboBox<>();

    // buttons
    JButton nextButton = new JButton(Bundle.getMessage("Next"));

    // panels
    JPanel pTrainVisit = new JPanel();

    public YardmasterPanel() {
        this(null);
    }

    public YardmasterPanel(Location location) {
        super();

        _location = location;

        // row 2
        JPanel pRow2 = new JPanel();
        pRow2.setLayout(new BoxLayout(pRow2, BoxLayout.X_AXIS));

        pRow2.add(pLocationName); // row 2a (location name)
        pRow2.add(pRailRoadName); // row 2b (railroad name)

        // row 6
        JPanel pRow6 = new JPanel();
        pRow6.setLayout(new BoxLayout(pRow6, BoxLayout.X_AXIS));

        // row 6a (train name)
        JPanel pTrainName = new JPanel();
        pTrainName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Train")));
        pTrainName.add(trainComboBox);
        // add next button for web server
        pTrainName.add(nextButton);

        // row 6b (train visit)
        pTrainVisit.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Visit")));
        pTrainVisit.add(trainVisitComboBox);

        pRow6.add(pTrainName);
        pRow6.add(pTrainVisit);
        pRow6.add(pTrainDescription); // row 6c (train description)

        pButtons.setMaximumSize(new Dimension(2000, 200));

        add(pRow2);
        add(pRow6);
        add(textLocationCommentPane);
        add(textSwitchListCommentPane);
        add(textTrainCommentPane);
        add(textTrainRouteCommentPane);
        add(textTrainRouteLocationCommentPane);
        add(locoPane);
        add(pWorkPanes);
        add(movePane);
        add(pStatus);
        add(pButtons);

        if (_location != null) {
            textLocationName.setText(_location.getName());
            loadLocationComment(_location);
            loadLocationSwitchListComment(_location);
            updateTrainsComboBox();
        }

        update();

        addComboBoxAction(trainComboBox);
        addComboBoxAction(trainVisitComboBox);

        addButtonAction(nextButton);

        // listen for trains being built
        addTrainListeners();
    }

    // Select, Clear, and Set Buttons
    @Override
    public void buttonActionPerformed(ActionEvent ae) {
        if (ae.getSource() == nextButton) {
            nextButtonAction();
        }
        super.buttonActionPerformed(ae);
    }

    private void nextButtonAction() {
        log.debug("next button activated");
        if (trainComboBox.getItemCount() > 1) {
            if (pTrainVisit.isVisible()) {
                int index = trainVisitComboBox.getSelectedIndex() + 1;
                if (index < trainVisitComboBox.getItemCount()) {
                    trainVisitComboBox.setSelectedIndex(index);
                    return; // done
                }
            }
            int index = trainComboBox.getSelectedIndex();
            // index = -1 if first item (null) in trainComboBox
            if (index == -1) {
                index = 1;
            } else {
                index++;
            }
            if (index >= trainComboBox.getItemCount()) {
                index = 0;
            }
            trainComboBox.setSelectedIndex(index);
        }
    }

    // Select Train and Visit
    @Override
    protected void comboBoxActionPerformed(ActionEvent ae) {
        // made the combo box not visible during updates, so ignore if not visible
        if (ae.getSource() == trainComboBox && trainComboBox.isVisible()) {
            _train = null;
            if (trainComboBox.getSelectedItem() != null) {
                _train = (Train) trainComboBox.getSelectedItem();
                _visitNumber = 1;
            }
            clearAndUpdate();
        }
        // made the combo box not visible during updates, so ignore if not visible
        if (ae.getSource() == trainVisitComboBox && trainVisitComboBox.isVisible()) {
            if (trainVisitComboBox.getSelectedItem() != null) {
                _visitNumber = (Integer) trainVisitComboBox.getSelectedItem();
                clearAndUpdate();
            }
        }
    }

    @Override
    protected void update() {
        log.debug("queue update");
        // use invokeLater to prevent deadlock
        SwingUtilities.invokeLater(() -> {
            log.debug("update, setMode: {}", isSetMode);
            initialize();

            // turn everything off and re-enable if needed
            pButtons.setVisible(false);
            pTrainVisit.setVisible(false);
            trainVisitComboBox.setVisible(false); // Use visible as a flag to ignore updates
            textTrainCommentPane.setVisible(false);
            textTrainRouteCommentPane.setVisible(false);
            textTrainRouteLocationCommentPane.setVisible(false);

            textTrainDescription.setText("");
            textStatus.setText("");

            if (_train != null && _train.getRoute() != null) {
                pButtons.setVisible(true);

                loadTrainDescription();
                loadTrainComment();
                loadRouteComment();
                loadRailroadName();

                // determine how many times this train visits this location and if it is the
                // last stop
                RouteLocation rl = null;
                List<RouteLocation> routeList = _train.getRoute().getLocationsBySequenceList();
                int visitNumber = 0;
                for (int i = 0; i < routeList.size(); i++) {
                    if (TrainCommon.splitString(routeList.get(i).getName())
                            .equals(TrainCommon.splitString(_location.getName()))) {
                        visitNumber++;
                        if (visitNumber == _visitNumber) {
                            rl = routeList.get(i);
                        }
                    }
                }

                if (rl != null) {
                    // update visit numbers
                    if (visitNumber > 1) {
                        trainVisitComboBox.removeAllItems(); // this fires an action change!
                        for (int i = 0; i < visitNumber; i++) {
                            trainVisitComboBox.addItem(i + 1);
                        }
                        trainVisitComboBox.setSelectedItem(_visitNumber);
                        trainVisitComboBox.setVisible(true); // now pay attention to changes
                        pTrainVisit.setVisible(true); // show the visit panel
                    }

                    if (Setup.isSwitchListRouteLocationCommentEnabled()) {
                        loadRouteLocationComment(rl);
                    }
                    textLocationName.setText(rl.getLocation().getName()); // show name including hyphen and number

                    updateTrackComments(rl, !IS_MANIFEST);

                    // check for locos
                    updateLocoPanes(rl);

                    // now update the car pick ups and set outs
                    blockCars(rl, !IS_MANIFEST);

                    textStatus.setText(getStatus(rl, !IS_MANIFEST));
                }
                updateComplete();
            }
        });
    }

    private void updateTrainsComboBox() {
        Object selectedItem = trainComboBox.getSelectedItem();
        trainComboBox.setVisible(false); // used as a flag to ignore updates
        trainComboBox.removeAllItems();
        trainComboBox.addItem(null);
        if (_location != null) {
            List<Train> trains = trainManager.getTrainsArrivingThisLocationList(_location);
            trains.stream().filter((train) -> (TrainCommon.isThereWorkAtLocation(train, _location)))
                    .forEach((train) -> {
                        trainComboBox.addItem(train);
                    });
        }
        if (selectedItem != null) {
            trainComboBox.setSelectedItem(selectedItem);
        }
        trainComboBox.setVisible(true);
    }

    private void addTrainListeners() {
        log.debug("Adding train listerners");
        List<Train> trains = InstanceManager.getDefault(TrainManager.class).getTrainsByIdList();
        trains.stream().forEach((train) -> {
            train.addPropertyChangeListener(this);
        });
        // listen for new trains being added
        InstanceManager.getDefault(TrainManager.class).addPropertyChangeListener(this);
    }

    private void removeTrainListeners() {
        log.debug("Removing train listerners");
        List<Train> trains = InstanceManager.getDefault(TrainManager.class).getTrainsByIdList();
        trains.stream().forEach((train) -> {
            train.removePropertyChangeListener(this);
        });
        InstanceManager.getDefault(TrainManager.class).removePropertyChangeListener(this);
    }

    @Override
    public void dispose() {
        removeTrainListeners();
        removePropertyChangeListerners();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
        }
        if ((e.getPropertyName().equals(RollingStock.ROUTE_LOCATION_CHANGED_PROPERTY) && e.getNewValue() == null) ||
                (e.getPropertyName().equals(RollingStock.ROUTE_DESTINATION_CHANGED_PROPERTY) &&
                        e.getNewValue() == null) ||
                e.getPropertyName().equals(RollingStock.TRAIN_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.TRAIN_MODIFIED_CHANGED_PROPERTY)) {
            // remove car from list
            if (e.getSource().getClass().equals(Car.class)) {
                Car car = (Car) e.getSource();
                removeCarFromList(car);
            }
            update();
        }
        if (e.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY)) {
            updateTrainsComboBox();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(YardmasterPanel.class);
}
