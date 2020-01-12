package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.CommonConductorYardmasterPanel;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainSwitchListText;

/**
 * Yardmaster frame by track. Shows work at one location listed by track.
 *
 * @author Dan Boudreau Copyright (C) 2015
 *
 */
public class YardmasterByTrackPanel extends CommonConductorYardmasterPanel {

    protected static final boolean IS_MANIFEST = false;

    protected Track _track = null;

    // text panes
    JTextPane textSwitchListCommentPane = new JTextPane();
    JTextPane textTrackCommentPane = new JTextPane();
    JTextPane textTrackCommentWorkPane = new JTextPane();

    // combo boxes
    JComboBox<Track> trackComboBox = new JComboBox<>();

    // buttons
    JButton nextButton = new JButton(Bundle.getMessage("Next"));

    // panel
    JPanel pTrack = new JPanel();
    JScrollPane pTrackPane;

    public YardmasterByTrackPanel() {
        this(null);
    }

    public YardmasterByTrackPanel(Location location) {
        super();
        initComponents();

        // this window doesn't use the set button
        modifyButton.setVisible(false);

        _location = location;

        textSwitchListCommentPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        textSwitchListCommentPane.setBackground(null);
        textSwitchListCommentPane.setEditable(false);
        textSwitchListCommentPane.setMaximumSize(new Dimension(2000, 200));

        textTrackCommentPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrackComment")));
        textTrackCommentPane.setBackground(null);
        textTrackCommentPane.setEditable(false);
        textTrackCommentPane.setMaximumSize(new Dimension(2000, 200));

        textTrackCommentWorkPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("MenuItemComments")));
        textTrackCommentWorkPane.setBackground(null);
        textTrackCommentWorkPane.setEditable(false);
        textTrackCommentWorkPane.setMaximumSize(new Dimension(2000, 200));

        JPanel pTrackSelect = new JPanel();
        pTrackSelect.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Track")));
        pTrackSelect.add(trackComboBox);
        // add next button for web server
        pTrackSelect.add(nextButton);

        // work at this location by track
        pTrack.setLayout(new BoxLayout(pTrack, BoxLayout.Y_AXIS));
        pTrackPane = new JScrollPane(pTrack);

        pLocationName.setMaximumSize(new Dimension(2000, 200));
        pTrackSelect.setMaximumSize(new Dimension(2000, 200));
        pButtons.setMaximumSize(new Dimension(2000, 200));

        add(pLocationName);
        add(textLocationCommentPane);
        add(textSwitchListCommentPane);
        add(pTrackSelect);
        add(textTrackCommentPane);
        add(textTrackCommentWorkPane);
        add(pTrackPane);
        add(pButtons);

        if (_location != null) {
            textLocationName.setText(_location.getName());
            textLocationCommentPane.setText(_location.getComment());
            textLocationCommentPane.setVisible(!_location.getComment().equals(Location.NONE)
                    && Setup.isPrintLocationCommentsEnabled());
            textSwitchListCommentPane.setText(_location.getSwitchListComment());
            textSwitchListCommentPane.setVisible(!_location.getSwitchListComment().equals(Location.NONE));
            updateTrackComboBox();
            _location.addPropertyChangeListener(this);
        }

        update();

        addComboBoxAction(trackComboBox);
        addButtonAction(nextButton);

        setVisible(true);

    }

    // Select, Clear, and Next Buttons
    @Override
    public void buttonActionPerformed(ActionEvent ae) {
        if (ae.getSource() == nextButton) {
            nextButtonAction();
        }
        super.buttonActionPerformed(ae);
    }

    private void nextButtonAction() {
        log.debug("next button activated");
        if (trackComboBox.getItemCount() > 1) {
            int index = trackComboBox.getSelectedIndex();
            // index = -1 if first item (null) in trainComboBox
            if (index == -1) {
                index = 1;
            } else {
                index++;
            }
            if (index >= trackComboBox.getItemCount()) {
                index = 0;
            }
            trackComboBox.setSelectedIndex(index);
        }
    }

    @Override
    protected void comboBoxActionPerformed(ActionEvent ae) {
        // made the combo box not visible during updates, so ignore if not visible
        if (ae.getSource() == trackComboBox && trackComboBox.isVisible()) {
            _track = null;
            if (trackComboBox.getSelectedItem() != null) {
                _track = (Track) trackComboBox.getSelectedItem();
            }
            update();
        }
    }

    @Override
    protected void update() {
        // use invokeLater to prevent deadlock
        SwingUtilities.invokeLater(() -> {
            runUpdate();
        });
    }

    private void runUpdate() {
        log.debug("run update");
        removePropertyChangeListerners();
        trainCommon.clearUtilityCarTypes(); // reset the utility car counts
        checkBoxes.clear();
        pTrack.removeAll();
        boolean pickup = false;
        boolean setout = false;
        if (_track != null) {
            pTrackPane.setBorder(BorderFactory.createTitledBorder(_track.getName()));
            textTrackCommentPane.setText(_track.getComment());
            textTrackCommentPane.setVisible(!_track.getComment().equals(Track.NONE));
            textTrackCommentWorkPane.setText("");
            for (Train train : trainManager.getTrainsArrivingThisLocationList(_track.getLocation())) {
                JPanel pTrain = new JPanel();
                pTrain.setLayout(new BoxLayout(pTrain, BoxLayout.Y_AXIS));
                pTrain.setBorder(BorderFactory.createTitledBorder(MessageFormat.format(TrainSwitchListText
                        .getStringScheduledWork(), new Object[]{train.getName(), train.getDescription()})));
                // List locos first
                List<Engine> engList = engManager.getByTrainBlockingList(train);
                if (Setup.isPrintHeadersEnabled()) {
                    for (Engine engine : engList) {
                        if (engine.getTrack() == _track) {
                            JLabel header = new JLabel(Tab + trainCommon.getPickupEngineHeader());
                            setLabelFont(header);
                            pTrain.add(header);
                            break;
                        }
                    }
                }
                for (Engine engine : engList) {
                    if (engine.getTrack() == _track) {
                        engine.addPropertyChangeListener(this);
                        rollingStock.add(engine);
                        JCheckBox checkBox = new JCheckBox(trainCommon.pickupEngine(engine));
                        setCheckBoxFont(checkBox);
                        pTrain.add(checkBox);
                        checkBoxes.put(engine.getId(), checkBox);
                        pTrack.add(pTrain);
                    }
                }
                // now do locomotive set outs
                if (Setup.isPrintHeadersEnabled()) {
                    for (Engine engine : engList) {
                        if (engine.getDestinationTrack() == _track) {
                            JLabel header = new JLabel(Tab + trainCommon.getDropEngineHeader());
                            setLabelFont(header);
                            pTrain.add(header);
                            break;
                        }
                    }
                }
                for (Engine engine : engList) {
                    if (engine.getDestinationTrack() == _track) {
                        engine.addPropertyChangeListener(this);
                        rollingStock.add(engine);
                        JCheckBox checkBox = new JCheckBox(trainCommon.dropEngine(engine));
                        setCheckBoxFont(checkBox);
                        pTrain.add(checkBox);
                        checkBoxes.put(engine.getId(), checkBox);
                        pTrack.add(pTrain);
                    }
                }
                // now cars
                List<Car> carList = carManager.getByTrainDestinationList(train);
                if (Setup.isPrintHeadersEnabled()) {
                    for (Car car : carList) {
                        if (car.getTrack() == _track && car.getRouteDestination() != car.getRouteLocation()) {
                            JLabel header = new JLabel(Tab +
                                    trainCommon.getPickupCarHeader(!IS_MANIFEST, !TrainCommon.IS_TWO_COLUMN_TRACK));
                            setLabelFont(header);
                            pTrain.add(header);
                            break;
                        }
                    }
                }
                // sort car pick ups by their destination
                List<RouteLocation> routeList = train.getRoute().getLocationsBySequenceList();
                for (RouteLocation rl : routeList) {
                    for (Car car : carList) {
                        if (car.getTrack() == _track &&
                                car.getRouteDestination() != car.getRouteLocation() &&
                                car.getRouteDestination() == rl) {
                            car.addPropertyChangeListener(this);
                            rollingStock.add(car);
                            String text;
                            if (car.isUtility()) {
                                text = trainCommon.pickupUtilityCars(carList, car, !IS_MANIFEST, !TrainCommon.IS_TWO_COLUMN_TRACK);
                                if (text == null) {
                                    continue; // this car type has already been processed
                                }
                            } else {
                                text = trainCommon.pickupCar(car, !IS_MANIFEST, !TrainCommon.IS_TWO_COLUMN_TRACK);
                            }
                            pickup = true;
                            JCheckBox checkBox = new JCheckBox(text);
                            setCheckBoxFont(checkBox);
                            pTrain.add(checkBox);
                            checkBoxes.put(car.getId(), checkBox);
                            pTrack.add(pTrain);
                        }
                    }
                }
                // now do car set outs
                if (Setup.isPrintHeadersEnabled()) {
                    for (Car car : carList) {
                        if (car.getDestinationTrack() == _track &&
                                car.getRouteDestination() != car.getRouteLocation()) {
                            JLabel header = new JLabel(Tab +
                                    trainCommon.getDropCarHeader(!IS_MANIFEST, !TrainCommon.IS_TWO_COLUMN_TRACK));
                            setLabelFont(header);
                            pTrain.add(header);
                            break;
                        }
                    }
                }
                for (Car car : carList) {
                    if (car.getDestinationTrack() == _track &&
                            car.getRouteLocation() != car.getRouteDestination()) {
                        car.addPropertyChangeListener(this);
                        rollingStock.add(car);
                        String text;
                        if (car.isUtility()) {
                            text = trainCommon.setoutUtilityCars(carList, car, !TrainCommon.LOCAL, !IS_MANIFEST);
                            if (text == null) {
                                continue; // this car type has already been processed
                            }
                        } else {
                            text = trainCommon.dropCar(car, !IS_MANIFEST, !TrainCommon.IS_TWO_COLUMN_TRACK);
                        }
                        setout = true;
                        JCheckBox checkBox = new JCheckBox(text);
                        setCheckBoxFont(checkBox);
                        pTrain.add(checkBox);
                        checkBoxes.put(car.getId(), checkBox);
                        pTrack.add(pTrain);
                    }
                }
                // now do local car moves
                if (Setup.isPrintHeadersEnabled()) {
                    for (Car car : carList) {
                        if ((car.getTrack() == _track || car.getDestinationTrack() == _track) &&
                                car.getRouteDestination() == car.getRouteLocation()) {
                            JLabel header = new JLabel(Tab + trainCommon.getLocalMoveHeader(!IS_MANIFEST));
                            setLabelFont(header);
                            pTrain.add(header);
                            break;
                        }
                    }
                }
                for (Car car : carList) {
                    if ((car.getTrack() == _track || car.getDestinationTrack() == _track) &&
                            car.getRouteLocation() != null && car.getRouteLocation() == car.getRouteDestination()) {
                        car.addPropertyChangeListener(this);
                        rollingStock.add(car);
                        String text;
                        if (car.isUtility()) {
                            text = trainCommon.setoutUtilityCars(carList, car, TrainCommon.LOCAL, !IS_MANIFEST);
                            if (text == null) {
                                continue; // this car type has already been processed
                            }
                        } else {
                            text = trainCommon.localMoveCar(car, !IS_MANIFEST);
                        }
                        setout = true;
                        JCheckBox checkBox = new JCheckBox(text);
                        setCheckBoxFont(checkBox);
                        pTrain.add(checkBox);
                        checkBoxes.put(car.getId(), checkBox);
                        pTrack.add(pTrain);
                    }
                }
                pTrackPane.validate();
                pTrain.setMaximumSize(new Dimension(2000, pTrain.getHeight()));
                pTrain.revalidate();
            }
            // now do car holds
            // we only need the cars on this track
            List<Car> rsList = carManager.getByTrainList();
            List<Car> carList = new ArrayList<Car>();
            for (Car rs : rsList) {
                if (rs.getTrack() != _track || rs.getRouteLocation() != null)
                    continue;
                carList.add(rs);
            }
            JPanel pHoldCars = new JPanel();
            pHoldCars.setLayout(new BoxLayout(pHoldCars, BoxLayout.Y_AXIS));
            pHoldCars.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("HoldCars")));
            for (Car car : carList) {
                String text;
                if (car.isUtility()) {
                    String s = trainCommon.pickupUtilityCars(carList, car, !IS_MANIFEST, !TrainCommon.IS_TWO_COLUMN_TRACK);
                    if (s == null)
                        continue;
                    text = TrainSwitchListText.getStringHoldCar().split("\\{")[0] + s.trim();
                } else {
                    text = MessageFormat.format(TrainSwitchListText.getStringHoldCar(),
                            new Object[]{TrainCommon.padAndTruncateString(car.getRoadName(), InstanceManager.getDefault(CarRoads.class).getMaxNameLength()),
                                    TrainCommon.padAndTruncateString(TrainCommon.splitString(car.getNumber()), Control.max_len_string_print_road_number),
                                    TrainCommon.padAndTruncateString(car.getTypeName().split("-")[0], InstanceManager.getDefault(CarTypes.class).getMaxNameLength()),
                                    TrainCommon.padAndTruncateString(car.getLength() + TrainCommon.LENGTHABV, Control.max_len_string_length_name),
                                    TrainCommon.padAndTruncateString(car.getLoadName(), InstanceManager.getDefault(CarLoads.class).getMaxNameLength()),
                                    TrainCommon.padAndTruncateString(_track.getName(), InstanceManager.getDefault(LocationManager.class).getMaxTrackNameLength()),
                                    TrainCommon.padAndTruncateString(car.getColor(), InstanceManager.getDefault(CarColors.class).getMaxNameLength())});

                }
                JCheckBox checkBox = new JCheckBox(text);
                setCheckBoxFont(checkBox);
                pHoldCars.add(checkBox);
                checkBoxes.put(car.getId(), checkBox);
                pTrack.add(pHoldCars);
            }
            pTrackPane.validate();
            pHoldCars.setMaximumSize(new Dimension(2000, pHoldCars.getHeight()));
            pHoldCars.revalidate();
            if (pickup && !setout) {
                textTrackCommentWorkPane.setText(_track.getCommentPickup());
            } else if (!pickup && setout) {
                textTrackCommentWorkPane.setText(_track.getCommentSetout());
            } else if (pickup && setout) {
                textTrackCommentWorkPane.setText(_track.getCommentBoth());
            }
            textTrackCommentWorkPane.setVisible(!textTrackCommentWorkPane.getText().equals(""));
        } else {
            pTrackPane.setBorder(BorderFactory.createTitledBorder(""));
            textTrackCommentPane.setVisible(false);
            textTrackCommentWorkPane.setVisible(false);
        }
    }

    private void updateTrackComboBox() {
        Object selectedItem = trackComboBox.getSelectedItem();
        trackComboBox.setVisible(false); // used as a flag to ignore updates
        if (_location != null) {
            _location.updateComboBox(trackComboBox);
        }
        if (selectedItem != null) {
            trackComboBox.setSelectedItem(selectedItem);
        }
        trackComboBox.setVisible(true);
    }

    @Override
    public void dispose() {
        if (_location != null)
            _location.removePropertyChangeListener(this);
        removePropertyChangeListerners();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        //        if (Control.showProperty) {
        log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                .getNewValue());
        //        }
        if (e.getPropertyName().equals(RollingStock.ROUTE_LOCATION_CHANGED_PROPERTY)) {
            update();
        }
        if (e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY)) {
            updateTrackComboBox();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(YardmasterByTrackPanel.class);
}
