package jmri.jmrit.operations.locations.tools;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.router.Router;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user edit of track destinations
 *
 * @author Dan Boudreau Copyright (C) 2013, 2024
 * 
 */
public class TrackDestinationEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    Track _track = null;

    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    // panels
    JPanel pControls = new JPanel();
    JPanel panelDestinations = new JPanel();
    JScrollPane paneDestinations = new JScrollPane(panelDestinations);

    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton checkDestinationsButton = new JButton(Bundle.getMessage("CheckDestinations"));

    // radio buttons
    JRadioButton destinationsAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
    JRadioButton destinationsInclude = new JRadioButton(Bundle.getMessage("AcceptOnly"));
    JRadioButton destinationsExclude = new JRadioButton(Bundle.getMessage("Exclude"));
    
    // checkboxes
    JCheckBox onlyCarsWithFD = new JCheckBox(Bundle.getMessage("OnlyCarsWithFD"));

    // labels
    JLabel trackName = new JLabel();

    public static final String DISPOSE = "dispose"; // NOI18N

    public TrackDestinationEditFrame() {
        super(Bundle.getMessage("TitleEditTrackDestinations"));
    }

    public void initComponents(TrackEditFrame tef) {
        _track = tef._track;

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows
        // row 1
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.setMaximumSize(new Dimension(2000, 250));

        // row 1a
        JPanel pTrackName = new JPanel();
        pTrackName.setLayout(new GridBagLayout());
        pTrackName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Track")));
        addItem(pTrackName, trackName, 0, 0);

        // row 1b
        JPanel pLocationName = new JPanel();
        pLocationName.setLayout(new GridBagLayout());
        pLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        addItem(pLocationName, new JLabel(_track.getLocation().getName()), 0, 0);

        p1.add(pTrackName);
        p1.add(pLocationName);

        // row 2 only for C/I and Staging
        JPanel pFD = new JPanel();
        pFD.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Options")));
        pFD.add(onlyCarsWithFD);
        pFD.setMaximumSize(new Dimension(2000, 200));

        // row 3
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
        JScrollPane pane3 = new JScrollPane(p3);
        pane3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("DestinationTrack")));
        pane3.setMaximumSize(new Dimension(2000, 400));

        JPanel pRadioButtons = new JPanel();
        pRadioButtons.setLayout(new FlowLayout());

        pRadioButtons.add(destinationsAll);
        pRadioButtons.add(destinationsInclude);
        pRadioButtons.add(destinationsExclude);

        p3.add(pRadioButtons);
        
        // row 4
        panelDestinations.setLayout(new GridBagLayout());
        paneDestinations.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Destinations")));

        ButtonGroup bGroup = new ButtonGroup();
        bGroup.add(destinationsAll);
        bGroup.add(destinationsInclude);
        bGroup.add(destinationsExclude);

        // row last
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new GridBagLayout());
        panelButtons.setBorder(BorderFactory.createTitledBorder(""));
        panelButtons.setMaximumSize(new Dimension(2000, 200));

        addItem(panelButtons, checkDestinationsButton, 0, 0);
        addItem(panelButtons, saveButton, 1, 0);

        getContentPane().add(p1);
        getContentPane().add(pFD);
        getContentPane().add(pane3);
        getContentPane().add(paneDestinations);
        getContentPane().add(panelButtons);

        // setup buttons
        addButtonAction(checkDestinationsButton);
        addButtonAction(saveButton);

        addRadioButtonAction(destinationsAll);
        addRadioButtonAction(destinationsInclude);
        addRadioButtonAction(destinationsExclude);

        // load fields and enable buttons
        if (_track != null) {
            _track.addPropertyChangeListener(this);
            trackName.setText(_track.getName());
            onlyCarsWithFD.setSelected(_track.isOnlyCarsWithFinalDestinationEnabled());
            pFD.setVisible(_track.isInterchange() || _track.isStaging());
            enableButtons(true);
        } else {
            enableButtons(false);
        }

        updateDestinations();

        locationManager.addPropertyChangeListener(this);

        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight500));
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (_track == null) {
            return;
        }
        if (ae.getSource() == saveButton) {
            log.debug("track save button activated");
            _track.setOnlyCarsWithFinalDestinationEnabled(onlyCarsWithFD.isSelected());
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == checkDestinationsButton) {
            checkDestinationsButton.setEnabled(false); // testing can take awhile, so disable
            checkDestinationsValid();
        }
    }

    protected void enableButtons(boolean enabled) {
        saveButton.setEnabled(enabled);
        checkDestinationsButton.setEnabled(enabled);
        destinationsAll.setEnabled(enabled);
        destinationsInclude.setEnabled(enabled);
        destinationsExclude.setEnabled(enabled);
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        if (ae.getSource() == destinationsAll) {
            _track.setDestinationOption(Track.ALL_DESTINATIONS);
        }
        if (ae.getSource() == destinationsInclude) {
            _track.setDestinationOption(Track.INCLUDE_DESTINATIONS);
        }
        if (ae.getSource() == destinationsExclude) {
            _track.setDestinationOption(Track.EXCLUDE_DESTINATIONS);
        }
        updateDestinations();
    }

    private void updateDestinations() {
        log.debug("Update destinations");
        panelDestinations.removeAll();
        if (_track != null) {
            destinationsAll.setSelected(_track.getDestinationOption().equals(Track.ALL_DESTINATIONS));
            destinationsInclude.setSelected(_track.getDestinationOption().equals(Track.INCLUDE_DESTINATIONS));
            destinationsExclude.setSelected(_track.getDestinationOption().equals(Track.EXCLUDE_DESTINATIONS));
        }
        List<Location> locations = locationManager.getLocationsByNameList();
        for (int i = 0; i < locations.size(); i++) {
            Location loc = locations.get(i);
            JCheckBox cb = new JCheckBox(loc.getName());
            addItemLeft(panelDestinations, cb, 0, i);
            cb.setEnabled(!destinationsAll.isSelected());
            addCheckBoxAction(cb);
            if (destinationsAll.isSelected()) {
                cb.setSelected(true);
            } else if (_track != null && _track.isDestinationAccepted(loc)
                    ^ _track.getDestinationOption().equals(Track.EXCLUDE_DESTINATIONS)) {
                cb.setSelected(true);
            }
        }
        panelDestinations.revalidate();
    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        JCheckBox b = (JCheckBox) ae.getSource();
        log.debug("checkbox change {}", b.getText());
        if (_track == null) {
            return;
        }
        Location loc = locationManager.getLocationByName(b.getText());
        if (loc != null) {
            if (b.isSelected() ^ _track.getDestinationOption().equals(Track.EXCLUDE_DESTINATIONS)) {
                _track.addDestination(loc);
            } else {
                _track.deleteDestination(loc);
            }
        }
    }

    private void checkDestinationsValid() {
        SwingUtilities.invokeLater(() -> {
            if (checkLocationsLoop())
                JmriJOptionPane.showMessageDialog(null, Bundle.getMessage("OkayMessage"));
            checkDestinationsButton.setEnabled(true);
        });
    }

    private boolean checkLocationsLoop() {
        boolean noIssues = true;
        // only report car type not serviced once
        List<String> ignoreType = new ArrayList<String>();
        for (Location destination : locationManager.getLocationsByNameList()) {
            ignoreType.clear();
            if (_track.isDestinationAccepted(destination)) {
                log.debug("Track ({}) accepts destination ({})", _track.getName(), destination.getName());
                if (_track.getLocation() == destination) {
                    continue;
                }
                // now check to see if the track's rolling stock is accepted by the destination
                checkTypes: for (String type : InstanceManager.getDefault(CarTypes.class).getNames()) {
                    if (!_track.isTypeNameAccepted(type)) {
                        continue;
                    }
                    if (!destination.acceptsTypeName(type)) {
                        noIssues = false;
                        int response = JmriJOptionPane.showConfirmDialog(this,
                                Bundle.getMessage("WarningDestinationCarType", 
                                        destination.getName(), type), Bundle.getMessage("WarningCarMayNotMove"),
                                JmriJOptionPane.OK_CANCEL_OPTION);
                        if (response == JmriJOptionPane.OK_OPTION) {
                            ignoreType.add(type);
                            continue;
                        }
                        return false; // done
                    }
                    // now determine if there's a track willing to service car type
                    for (Track track : destination.getTracksList()) {
                        if (track.isTypeNameAccepted(type)) {
                            continue checkTypes; // yes there's a track
                        }
                    }
                    noIssues = false;
                    int response = JmriJOptionPane.showConfirmDialog(this,
                            Bundle.getMessage("WarningDestinationTrackCarType",
                                    destination.getName(), type),
                            Bundle.getMessage("WarningCarMayNotMove"),
                            JmriJOptionPane.OK_CANCEL_OPTION);
                    if (response == JmriJOptionPane.OK_OPTION) {
                        ignoreType.add(type);
                        continue;
                    }
                    return false; // done
                }
                // now check road names
                for (String type : InstanceManager.getDefault(CarTypes.class).getNames()) {
                    if (!_track.isTypeNameAccepted(type) || ignoreType.contains(type)) {
                        continue;
                    }
                    checkRoads: for (String road : InstanceManager.getDefault(CarRoads.class).getNames(type)) {
                        if (!_track.isRoadNameAccepted(road)) {
                            continue;
                        }
                        // now determine if there's a track willing to service this road
                        for (Track track : destination.getTracksList()) {
                            if (!track.isTypeNameAccepted(type)) {
                                continue;
                            }
                            if (track.isRoadNameAccepted(road)) {
                                continue checkRoads; // yes there's a track
                            }
                        }
                        noIssues = false;
                        int response = JmriJOptionPane.showConfirmDialog(this,
                                Bundle.getMessage("WarningDestinationTrackCarRoad",
                                        destination.getName(), type, road),
                                Bundle.getMessage("WarningCarMayNotMove"),
                                JmriJOptionPane.OK_CANCEL_OPTION);
                        if (response == JmriJOptionPane.OK_OPTION) {
                            continue;
                        }
                        return false; // done
                    }
                }
                // now check load names
                for (String type : InstanceManager.getDefault(CarTypes.class).getNames()) {
                    if (!_track.isTypeNameAccepted(type) || ignoreType.contains(type)) {
                        continue;
                    }
                    List<String> loads = InstanceManager.getDefault(CarLoads.class).getNames(type);
                    checkLoads: for (String load : loads) {
                        if (!_track.isLoadNameAccepted(load)) {
                            continue;
                        }
                        // now determine if there's a track willing to service this load
                        for (Track track : destination.getTracksList()) {
                            if (!track.isTypeNameAccepted(type)) {
                                continue;
                            }
                            if (track.isLoadNameAccepted(load)) {
                                continue checkLoads;
                            }
                        }
                        noIssues = false;
                        int response = JmriJOptionPane.showConfirmDialog(this, Bundle
                                .getMessage("WarningDestinationTrackCarLoad", destination.getName(),
                                type, load), Bundle.getMessage("WarningCarMayNotMove"), JmriJOptionPane.OK_CANCEL_OPTION);
                        if (response == JmriJOptionPane.OK_OPTION) {
                            continue;
                        }
                        return false; // done
                    }
                    // now check car type and load combinations
                    checkLoads: for (String load : loads) {
                        if (!_track.isLoadNameAndCarTypeAccepted(load, type)) {
                            continue;
                        }
                        // now determine if there's a track willing to service this load
                        for (Track track : destination.getTracksList()) {
                            if (track.isLoadNameAndCarTypeAccepted(load, type)) {
                                continue checkLoads;
                            }
                        }
                        noIssues = false;
                        int response = JmriJOptionPane.showConfirmDialog(this, Bundle
                                .getMessage("WarningDestinationTrackCarLoad", destination.getName(),
                                type, load), Bundle.getMessage("WarningCarMayNotMove"), JmriJOptionPane.OK_CANCEL_OPTION);
                        if (response == JmriJOptionPane.OK_OPTION) {
                            continue;
                        }
                        return false; // done
                    }
                }
                // now determine if there's a train or trains that can move a car from this track to the destinations
                // need to check all car types, loads, and roads that this track services
                Car car = new Car();
                car.setLength(Integer.toString(-RollingStock.COUPLERS)); // set car length to net out to zero
                for (String type : InstanceManager.getDefault(CarTypes.class).getNames()) {
                    if (!_track.isTypeNameAccepted(type)) {
                        continue;
                    }
                    List<String> loads = InstanceManager.getDefault(CarLoads.class).getNames(type);
                    for (String load : loads) {
                        if (!_track.isLoadNameAndCarTypeAccepted(load, type)) {
                            continue;
                        }
                        for (String road : InstanceManager.getDefault(CarRoads.class).getNames(type)) {
                            if (!_track.isRoadNameAccepted(road)) {
                                continue;
                            }
                            // is there a car with this road?
                            boolean foundCar = false;
                            for (RollingStock rs : InstanceManager.getDefault(CarManager.class).getList()) {
                                if (rs.getTypeName().equals(type) && rs.getRoadName().equals(road)) {
                                    foundCar = true;
                                    break;
                                }
                            }
                            if (!foundCar) {
                                continue; // no car with this road name
                            }

                            car.setTypeName(type);
                            car.setRoadName(road);
                            car.setLoadName(load);
                            car.setTrack(_track);
                            car.setFinalDestination(destination);
                            
                            // does the destination accept this car?
                            // this checks tracks that have schedules
                            String testDest = "NO_TYPE";
                            for (Track track : destination.getTracksList()) {
                                if (!track.isTypeNameAccepted(type)) {
                                    // already reported if type not accepted
                                    continue; 
                                }
                                if (track.getScheduleMode() == Track.SEQUENTIAL) {
                                    // must test in match mode
                                    track.setScheduleMode(Track.MATCH);
                                    String itemId = track.getScheduleItemId();
                                    testDest = car.checkDestination(destination, track);
                                    track.setScheduleMode(Track.SEQUENTIAL);
                                    track.setScheduleItemId(itemId);
                                } else {
                                    testDest = car.checkDestination(destination, track);
                                }
                                if (testDest.equals(Track.OKAY)) {
                                    break; // done
                                }
                            }
                            
                            if (testDest.equals("NO_TYPE")) {
                                continue;
                            }
                            
                            if (!testDest.equals(Track.OKAY)) {
                                noIssues = false;
                                int response = JmriJOptionPane.showConfirmDialog(this, Bundle
                                        .getMessage("WarningNoTrack", destination.getName(), type, road, load,
                                        destination.getName()), Bundle.getMessage("WarningCarMayNotMove"),
                                        JmriJOptionPane.OK_CANCEL_OPTION);
                                if (response == JmriJOptionPane.OK_OPTION) {
                                    continue;
                                }
                                return false; // done
                            }
                            
                            log.debug("Find train for car type ({}), road ({}), load ({})", type, road, load);

                            boolean results = InstanceManager.getDefault(Router.class).setDestination(car, null, null);
                            car.setDestination(null, null); // clear destination if set by router
                            if (!results) {
                                noIssues = false;
                                int response = JmriJOptionPane.showConfirmDialog(this, Bundle
                                        .getMessage("WarningNoTrain", type, road, load,
                                        destination.getName()), Bundle.getMessage("WarningCarMayNotMove"),
                                        JmriJOptionPane.OK_CANCEL_OPTION);
                                if (response == JmriJOptionPane.OK_OPTION) {
                                    continue;
                                }
                                return false; // done
                            }
                            // TODO need to check owners and car built dates
                        }
                    }
                }
            }
        }
        return noIssues;
    }

    @Override
    public void dispose() {
        if (_track != null) {
            _track.removePropertyChangeListener(this);
        }
        locationManager.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.DESTINATIONS_CHANGED_PROPERTY)) {
            updateDestinations();
        }
        if (e.getPropertyName().equals(Track.ROUTED_CHANGED_PROPERTY)) {
            onlyCarsWithFD.setSelected((boolean) e.getNewValue());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackDestinationEditFrame.class);
}
