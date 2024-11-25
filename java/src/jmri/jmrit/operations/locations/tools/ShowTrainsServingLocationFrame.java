package jmri.jmrit.operations.locations.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

/**
 * Frame to show which trains can service this location
 *
 * @author Dan Boudreau Copyright (C) 2014
 */
public class ShowTrainsServingLocationFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    Location _location = null;
    Track _track = null;

    // panels
    JPanel pTrains = new JPanel();

    // combo boxes
    JComboBox<Location> locationComboBox = new JComboBox<>();
    JComboBox<Track> trackComboBox = new JComboBox<>();
    JComboBox<String> typeComboBox = new JComboBox<>();

    // check boxes
    JCheckBox showAllTrainsCheckBox = new JCheckBox(Bundle.getMessage("ShowAllTrains"));

    // make show all trains consistent during a session
    private static boolean isShowAllTrains = false;

    public ShowTrainsServingLocationFrame() {
        super(Bundle.getMessage("TitleShowTrains"));
    }

    public void initComponents(Location location, Track track) {

        _location = location;
        _track = track;

        // general GUI config
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        JPanel pLocations = new JPanel();
        pLocations.setLayout(new GridBagLayout());
        pLocations.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        pLocations.setMaximumSize(new Dimension(2000, 50));

        addItem(pLocations, locationComboBox, 0, 0);

        JPanel pTracks = new JPanel();
        pTracks.setLayout(new GridBagLayout());
        pTracks.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Track")));
        pTracks.setMaximumSize(new Dimension(2000, 50));

        addItem(pTracks, trackComboBox, 0, 0);

        JPanel pCarType = new JPanel();
        pCarType.setLayout(new GridBagLayout());
        pCarType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
        pCarType.setMaximumSize(new Dimension(2000, 50));

        addItem(pCarType, typeComboBox, 0, 0);
        
        JPanel pOptions = new JPanel();
        pOptions.setLayout(new GridBagLayout());
        pOptions.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Options")));

        addItem(pOptions, showAllTrainsCheckBox, 0, 0);

        pTrains.setLayout(new GridBagLayout());
        JScrollPane trainsPane = new JScrollPane(pTrains);
        trainsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        trainsPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Trains")));

        getContentPane().add(pLocations);
        getContentPane().add(pTracks);
        getContentPane().add(pCarType);
        getContentPane().add(pOptions);
        getContentPane().add(trainsPane);

        // show all trains
        showAllTrainsCheckBox.setToolTipText(Bundle.getMessage("TipDeselectedShowAllTrains"));
        addCheckBoxAction(showAllTrainsCheckBox);
        showAllTrainsCheckBox.setSelected(isShowAllTrains);

        // setup combo box
        updateLocationsComboBox();
        updateTracksComboBox();
        updateTypeComboBox();

        addComboBoxAction(locationComboBox);
        addComboBoxAction(trackComboBox);
        addComboBoxAction(typeComboBox);

        // increase width of combobox so large text names display properly
        Dimension boxsize = typeComboBox.getMinimumSize();
        if (boxsize != null) {
            boxsize.setSize(boxsize.width + 10, boxsize.height);
            typeComboBox.setMinimumSize(boxsize);
        }

        if (location != null) {
            location.addPropertyChangeListener(this);
        }
        if (track != null) {
            track.addPropertyChangeListener(this);
        }
        addPropertyChangeAllTrains();

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new PrintTrainsServingLocationAction(this, false));
        toolMenu.add(new PrintTrainsServingLocationAction(this, true));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        // add help menu to window
        addHelpMenu("package.jmri.jmrit.operations.Operations_ShowTrainsServicingThisLocation", true); // NOI18N

        setPreferredSize(null);
        initMinimumSize();
    }

    private void updateTrainPane() {
        log.debug("Updating for location ({}), Track ({})", _location, _track);
        pTrains.removeAll();
        int y = 0;
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
            Route route = train.getRoute();
            if (route == null) {
                continue;
            }
            // determine if the car type is accepted by train
            boolean typeAccepted = train.isTypeNameAccepted(_carType);
            if (_carType.equals(NONE)) {
                // determine if any available car type is accepted by train
                for (int i = 0; i < typeComboBox.getItemCount(); i++) {
                    if (train.isTypeNameAccepted(typeComboBox.getItemAt(i))) {
                        typeAccepted = true;
                        break;
                    }
                }
            }
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                if (_location != null && rl.getName().equals(_location.getName())) {
                    boolean pickup = false;
                    boolean setout = false;
                    // monitor move count in the route for this location
                    train.getRoute().removePropertyChangeListener(this);
                    train.getRoute().addPropertyChangeListener(this);
                    if (rl.isPickUpAllowed() &&
                            rl.getMaxCarMoves() > 0 &&
                            !train.isLocationSkipped(rl.getId()) &&
                            typeAccepted &&
                            (train.isLocalSwitcher() ||
                                    (rl.getTrainDirection() & _location.getTrainDirections()) != 0) &&
                            (train.isLocalSwitcher() ||
                                    _track == null ||
                                    ((rl.getTrainDirection() & _track.getTrainDirections()) != 0)) &&
                            (_track == null || _track.isPickupTrainAccepted(train))) {
                        pickup = true;
                    }
                    if (rl.isDropAllowed() &&
                            rl.getMaxCarMoves() > 0 &&
                            !train.isLocationSkipped(rl.getId()) &&
                            typeAccepted &&
                            (train.isLocalSwitcher() ||
                                    (rl.getTrainDirection() & _location.getTrainDirections()) != 0) &&
                            (train.isLocalSwitcher() ||
                                    _track == null ||
                                    ((rl.getTrainDirection() & _track.getTrainDirections()) != 0)) &&
                            (_track == null || _track.isDropTrainAccepted(train)) &&
                            (_track == null ||
                                    _carType.equals(NONE) ||
                                    _track.checkScheduleAttribute(Track.TYPE, _carType, null))) {
                        setout = true;
                    }
                    // now display results
                    if (showAllTrainsCheckBox.isSelected() || pickup || setout) {
                        addItemLeft(pTrains, new JLabel(train.getName()), 0, y);
                        // train direction when servicing this location
                        addItem(pTrains, new JLabel(rl.getTrainDirectionString()), 1, y);
                        if (pickup) {
                            addItem(pTrains, new JLabel(Bundle.getMessage("OkayPickUp")), 2, y);
                        } else {
                            addItem(pTrains, new JLabel(Bundle.getMessage("NoPickUp")), 2, y);
                        }
                        if (setout) {
                            addItem(pTrains, new JLabel(Bundle.getMessage("OkaySetOut")), 3, y);
                        } else {
                            addItem(pTrains, new JLabel(Bundle.getMessage("NoSetOut")), 3, y);
                        }
                    }
                    y++;
                }
            }
        }
        pTrains.repaint();
        pTrains.revalidate();
        pack();
    }

    @Override
    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("check box action");
        isShowAllTrains = showAllTrainsCheckBox.isSelected();
        updateTrainPane();
    }

    String _carType = NONE;

    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource().equals(locationComboBox)) {
            _location = (Location) locationComboBox.getSelectedItem();
            updateTracksComboBox();
            updateTypeComboBox();
            updateTrainPane();
        }
        if (ae.getSource().equals(trackComboBox)) {
            if (_track != null) {
                _track.removePropertyChangeListener(this);
            }
            _track = (Track) trackComboBox.getSelectedItem();
            if (_track != null) {
                _track.addPropertyChangeListener(this);
            }
            updateTypeComboBox();
            updateTrainPane();
        }
        if (typeComboBox.isEnabled() && ae.getSource().equals(typeComboBox)) {
            if (typeComboBox.getSelectedItem() != null) {
                _carType = (String) typeComboBox.getSelectedItem();
            }
            updateTrainPane();
        }
    }

    private void updateLocationsComboBox() {
        InstanceManager.getDefault(LocationManager.class).updateComboBox(locationComboBox);
        locationComboBox.setSelectedItem(_location);
    }

    private void updateTracksComboBox() {
        if (_location != null) {
            _location.updateComboBox(trackComboBox);
        }
        trackComboBox.setSelectedItem(_track);
    }

    private void updateTypeComboBox() {
        log.debug("update type combobox");
        typeComboBox.setEnabled(false);
        InstanceManager.getDefault(CarTypes.class).updateComboBox(typeComboBox);
        // remove car types not serviced by this location and track
        for (int i = typeComboBox.getItemCount() - 1; i >= 0; i--) {
            String type = typeComboBox.getItemAt(i);
            if (_location != null && !_location.acceptsTypeName(type)) {
                typeComboBox.removeItem(type);
            }
            if (_track != null && !_track.isTypeNameAccepted(type)) {
                typeComboBox.removeItem(type);
            }
        }
        typeComboBox.insertItemAt(NONE, 0);
        typeComboBox.setSelectedItem(_carType);

        updateTrainPane();
        typeComboBox.setEnabled(true);
    }

    @Override
    public void dispose() {
        if (_location != null) {
            _location.removePropertyChangeListener(this);
        }
        if (_track != null) {
            _track.removePropertyChangeListener(this);
        }
        removePropertyChangeAllTrains();
        super.dispose();
    }

    public void addPropertyChangeAllTrains() {
        InstanceManager.getDefault(TrainManager.class).addPropertyChangeListener(this);
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
            train.addPropertyChangeListener(this);
        }
    }

    public void removePropertyChangeAllTrains() {
        InstanceManager.getDefault(TrainManager.class).removePropertyChangeListener(this);
        for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByNameList()) {
            train.removePropertyChangeListener(this);
            if (train.getRoute() != null) {
                train.getRoute().removePropertyChangeListener(this);
            }
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
        }
        if (e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY)) {
            updateTypeComboBox();
        }
        if (e.getPropertyName().equals(Location.TRAIN_DIRECTION_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.TRAIN_DIRECTION_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.DROP_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.PICKUP_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.TRAIN_ROUTE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.TYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.STOPS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Route.LISTCHANGE_CHANGED_PROPERTY)) {
            updateTrainPane();
        }
        if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)) {
            removePropertyChangeAllTrains();
            addPropertyChangeAllTrains();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ShowTrainsServingLocationFrame.class);
}
