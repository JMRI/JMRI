package jmri.jmrit.operations.rollingstock;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.tools.CarsSetFrame;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user to place RollingStock on the layout
 *
 * @author Dan Boudreau Copyright (C) 2010, 2011, 2012, 2013
 * @param <T> the type of RollingStock supported by this frame
 */
public abstract class RollingStockSetFrame<T extends RollingStock> extends OperationsFrame implements java.beans.PropertyChangeListener {

    protected LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);
    protected TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);

    RollingStock _rs;

    // labels
    JLabel textRoad = new JLabel();
    JLabel textType = new JLabel();

    // major buttons
    public JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    public JButton ignoreAllButton = new JButton(Bundle.getMessage("IgnoreAll"));

    // combo boxes
    public JComboBox<Location> locationBox = locationManager.getComboBox();
    public JComboBox<Track> trackLocationBox = new JComboBox<>();
    public JComboBox<Location> destinationBox = locationManager.getComboBox();
    public JComboBox<Track> trackDestinationBox = new JComboBox<>();
    public JComboBox<Location> finalDestinationBox = locationManager.getComboBox();
    public JComboBox<Track> finalDestTrackBox = new JComboBox<>();
    public JComboBox<Train> trainBox = trainManager.getTrainComboBox();

    // check boxes
    public JCheckBox autoTrackCheckBox = new JCheckBox(Bundle.getMessage("Auto"));
    public JCheckBox autoDestinationTrackCheckBox = new JCheckBox(Bundle.getMessage("Auto"));
    public JCheckBox autoFinalDestTrackCheckBox = new JCheckBox(Bundle.getMessage("Auto"));
    public JCheckBox autoTrainCheckBox = new JCheckBox(Bundle.getMessage("Auto"));

    public JCheckBox locationUnknownCheckBox = new JCheckBox(Bundle.getMessage("LocationUnknown"));
    public JCheckBox outOfServiceCheckBox = new JCheckBox(Bundle.getMessage("OutOfService"));

    public JCheckBox ignoreStatusCheckBox = new JCheckBox(Bundle.getMessage("Ignore"));
    public JCheckBox ignoreLocationCheckBox = new JCheckBox(Bundle.getMessage("Ignore"));
    public JCheckBox ignoreDestinationCheckBox = new JCheckBox(Bundle.getMessage("Ignore"));
    public JCheckBox ignoreFinalDestinationCheckBox = new JCheckBox(Bundle.getMessage("Ignore"));
    public JCheckBox ignoreTrainCheckBox = new JCheckBox(Bundle.getMessage("Ignore"));

    // optional panels
    protected JPanel pOptional = new JPanel();
    protected JScrollPane paneOptional = new JScrollPane(pOptional);
    protected JPanel pFinalDestination = new JPanel();

    // Auto checkbox states
    private static boolean autoTrackCheckBoxSelected = false;
    private static boolean autoDestinationTrackCheckBoxSelected = false;
    private static boolean autoFinalDestTrackCheckBoxSelected = false;
    private static boolean autoTrainCheckBoxSelected = false;

    public RollingStockSetFrame(String title) {
        super(title);
    }

    @Override
    public void initComponents() {
        // the following code sets the frame's initial state
        // create panel
        JPanel pPanel = new JPanel();
        pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.Y_AXIS));

        // Layout the panel by rows
        // row 1
        JPanel pRow1 = new JPanel();
        pRow1.setLayout(new BoxLayout(pRow1, BoxLayout.X_AXIS));
        // row 1a
        JPanel pRs = new JPanel();
        pRs.setLayout(new GridBagLayout());
        pRs.setBorder(BorderFactory.createTitledBorder(getRb().getString("rsType")));
        addItem(pRs, textRoad, 1, 0);
        pRow1.add(pRs);

        // row 1b
        JPanel pType = new JPanel();
        pType.setLayout(new GridBagLayout());
        pType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
        addItem(pType, textType, 1, 0);
        pRow1.add(pType);

        // row 1c
        JPanel pStatus = new JPanel();
        pStatus.setLayout(new GridBagLayout());
        pStatus.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Status")));
        addItemLeft(pStatus, ignoreStatusCheckBox, 0, 0);
        addItemLeft(pStatus, locationUnknownCheckBox, 1, 1);
        addItemLeft(pStatus, outOfServiceCheckBox, 1, 0);
        pRow1.add(pStatus);

        pPanel.add(pRow1);

        // row 2
        JPanel pLocation = new JPanel();
        pLocation.setLayout(new GridBagLayout());
        pLocation.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocationAndTrack")));
        addItemLeft(pLocation, ignoreLocationCheckBox, 0, 1);
        addItem(pLocation, locationBox, 1, 1);
        trackLocationBox.setName("trackLocationBox");
        addItem(pLocation, trackLocationBox, 2, 1);
        addItem(pLocation, autoTrackCheckBox, 3, 1);
        pPanel.add(pLocation);

        // optional panel 2
        JPanel pOptional2 = new JPanel();
        JScrollPane paneOptional2 = new JScrollPane(pOptional2);
        pOptional2.setLayout(new BoxLayout(pOptional2, BoxLayout.Y_AXIS));
        paneOptional2.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("BorderLayoutOptionalProgram")));

        // row 6
        JPanel pDestination = new JPanel();
        pDestination.setLayout(new GridBagLayout());
        pDestination.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("DestinationAndTrack")));
        addItemLeft(pDestination, ignoreDestinationCheckBox, 0, 1);
        addItem(pDestination, destinationBox, 1, 1);
        addItem(pDestination, trackDestinationBox, 2, 1);
        addItem(pDestination, autoDestinationTrackCheckBox, 3, 1);
        pOptional2.add(pDestination);

        // row 7
        pFinalDestination.setLayout(new GridBagLayout());
        pFinalDestination.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("FinalDestinationAndTrack")));
        addItemLeft(pFinalDestination, ignoreFinalDestinationCheckBox, 0, 1);
        addItem(pFinalDestination, finalDestinationBox, 1, 1);
        addItem(pFinalDestination, finalDestTrackBox, 2, 1);
        addItem(pFinalDestination, autoFinalDestTrackCheckBox, 3, 1);
        pOptional2.add(pFinalDestination);

        // row 8
        JPanel pTrain = new JPanel();
        pTrain.setLayout(new GridBagLayout());
        pTrain.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Train")));
        addItemLeft(pTrain, ignoreTrainCheckBox, 0, 0);
        addItem(pTrain, trainBox, 1, 0);
        addItem(pTrain, autoTrainCheckBox, 2, 0);
        pOptional2.add(pTrain);

        // button panel
        JPanel pButtons = new JPanel();
        pButtons.setLayout(new GridBagLayout());
        addItem(pButtons, ignoreAllButton, 1, 0);
        addItem(pButtons, saveButton, 2, 0);

        // add panels
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(pPanel);
        getContentPane().add(paneOptional);
        getContentPane().add(paneOptional2);
        getContentPane().add(pButtons);

        // Don't show ignore buttons
        ignoreStatusCheckBox.setVisible(false);
        ignoreLocationCheckBox.setVisible(false);
        ignoreDestinationCheckBox.setVisible(false);
        ignoreFinalDestinationCheckBox.setVisible(false);
        ignoreTrainCheckBox.setVisible(false);
        ignoreAllButton.setVisible(false);

        // setup buttons
        addButtonAction(ignoreAllButton);
        addButtonAction(saveButton);

        // setup combobox
        addComboBoxAction(locationBox);
        addComboBoxAction(destinationBox);
        addComboBoxAction(finalDestinationBox);
        addComboBoxAction(trainBox);

        // setup checkbox
        addCheckBoxAction(locationUnknownCheckBox);
        addCheckBoxAction(outOfServiceCheckBox);
        addCheckBoxAction(autoTrackCheckBox);
        addCheckBoxAction(autoDestinationTrackCheckBox);
        addCheckBoxAction(autoFinalDestTrackCheckBox);
        addCheckBoxAction(autoTrainCheckBox);

        addCheckBoxAction(ignoreStatusCheckBox);
        addCheckBoxAction(ignoreLocationCheckBox);
        addCheckBoxAction(ignoreDestinationCheckBox);
        addCheckBoxAction(ignoreFinalDestinationCheckBox);
        addCheckBoxAction(ignoreTrainCheckBox);

        // set auto check box selected
        autoTrackCheckBox.setSelected(autoTrackCheckBoxSelected);
        autoDestinationTrackCheckBox.setSelected(autoDestinationTrackCheckBoxSelected);
        autoFinalDestTrackCheckBox.setSelected(autoFinalDestTrackCheckBoxSelected);
        autoTrainCheckBox.setSelected(autoTrainCheckBoxSelected);

        // add tool tips
        autoTrackCheckBox.setToolTipText(getRb().getString("rsTipAutoTrack"));
        autoDestinationTrackCheckBox.setToolTipText(getRb().getString("rsTipAutoTrack"));
        autoFinalDestTrackCheckBox.setToolTipText(getRb().getString("rsTipAutoTrack"));
        autoTrainCheckBox.setToolTipText(Bundle.getMessage("rsTipAutoTrain"));
        locationUnknownCheckBox.setToolTipText(Bundle.getMessage("TipLocationUnknown"));

        ignoreStatusCheckBox.setToolTipText(Bundle.getMessage("TipIgnore"));
        ignoreLocationCheckBox.setToolTipText(Bundle.getMessage("TipIgnore"));
        ignoreDestinationCheckBox.setToolTipText(Bundle.getMessage("TipIgnore"));
        ignoreFinalDestinationCheckBox.setToolTipText(Bundle.getMessage("TipIgnore"));
        ignoreTrainCheckBox.setToolTipText(Bundle.getMessage("TipIgnore"));

        // get notified if combo box gets modified
        locationManager.addPropertyChangeListener(this);
        // get notified if train combo box gets modified
        trainManager.addPropertyChangeListener(this);
    }

    protected void load(RollingStock rs) {
        _rs = rs;
        textRoad.setText(_rs.getRoadName() + " " + _rs.getNumber());
        textType.setText(_rs.getTypeName());
        locationUnknownCheckBox.setSelected(_rs.isLocationUnknown());
        outOfServiceCheckBox.setSelected(_rs.isOutOfService());
        updateComboBoxes(); // load the location, destination, and final destination combo boxes
        updateTrainComboBox(); // load the train combo box
        enableComponents(!locationUnknownCheckBox.isSelected());
        // has the program generated a pick up and set out for this rolling stock?
        if (_rs.getRouteLocation() != null || _rs.getRouteDestination() != null) {
            if (_rs.getRouteLocation() != null) {
                log.debug("rs ({}) has a pick up location ({})", _rs.toString(), _rs.getRouteLocation().getName());
            }
            if (_rs.getRouteDestination() != null) {
                log.debug("rs ({}) has a destination ({})", _rs.toString(), _rs.getRouteDestination().getName());
            }
            if (getClass() == CarsSetFrame.class) {
                JmriJOptionPane.showMessageDialog(this, getRb().getString("rsPressChangeWill"), getRb().getString(
                        "rsInRoute"), JmriJOptionPane.WARNING_MESSAGE);
            } else {
                JmriJOptionPane.showMessageDialog(this, getRb().getString("rsPressSaveWill"), getRb().getString(
                        "rsInRoute"), JmriJOptionPane.WARNING_MESSAGE);
            }
        }
        _rs.addPropertyChangeListener(this);
    }

    // Save button
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            if (save() && Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    abstract protected ResourceBundle getRb();

    protected boolean save() {
        return change(_rs);
    }

    // change(RollingStock rs) will load the route location and the route destination if possible
    RouteLocation rl;
    RouteLocation rd;

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    protected boolean change(RollingStock rs) {
        log.debug("Change button action for rs ({})", rs.toString());
        // save the auto buttons
        autoTrackCheckBoxSelected = autoTrackCheckBox.isSelected();
        autoDestinationTrackCheckBoxSelected = autoDestinationTrackCheckBox.isSelected();
        autoFinalDestTrackCheckBoxSelected = autoFinalDestTrackCheckBox.isSelected();
        autoTrainCheckBoxSelected = autoTrainCheckBox.isSelected();

        // save the statuses
        if (!ignoreStatusCheckBox.isSelected()) {
            rs.setLocationUnknown(locationUnknownCheckBox.isSelected());
            rs.setOutOfService(outOfServiceCheckBox.isSelected());
        }
        // update location
        if (!changeLocation(rs)) {
            return false;
        }
        // check to see if rolling stock is in staging and out of service (also location unknown)
        if (outOfServiceCheckBox.isSelected() && rs.getTrack() != null && rs.getTrack().isStaging()) {
            JmriJOptionPane.showMessageDialog(this, getRb().getString("rsNeedToRemoveStaging"), getRb()
                    .getString("rsInStaging"), JmriJOptionPane.WARNING_MESSAGE);
            // clear the rolling stock's location
            rs.setLocation(null, null);
        }

        loadTrain(rs);

        // update destination
        if (!changeDestination(rs)) {
            return false;
        }

        updateTrainComboBox();

        // check if train can service this rolling stock
        if (!ignoreTrainCheckBox.isSelected()) {
            Train train = rs.getTrain();
            if (train != null) {
                // determine if train services this rs's type
                if (!train.isTypeNameAccepted(rs.getTypeName())) {
                    JmriJOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString(
                            "rsTrainNotServType"), new Object[]{rs.getTypeName(), train.getName()}), getRb()
                                    .getString("rsNotMove"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    // prevent rs from being picked up and delivered
                    setRouteLocationAndDestination(rs, train, null, null);
                    return false;
                }
                // determine if train services this rs's road
                if (rs.getClass() == Car.class) {
                    Car car = (Car) rs;
                    if (!car.isCaboose() && !train.isCarRoadNameAccepted(car.getRoadName()) ||
                            car.isCaboose() && !train.isCabooseRoadNameAccepted(car.getRoadName())) {
                        JmriJOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString(
                                "rsTrainNotServRoad"), new Object[]{rs.getRoadName(), train.getName()}), getRb()
                                        .getString("rsNotMove"),
                                JmriJOptionPane.ERROR_MESSAGE);
                        // prevent rs from being picked up and delivered
                        setRouteLocationAndDestination(rs, train, null, null);
                        return false;
                    }
                }
                // determine if train services this rs's built date
                if (!train.isBuiltDateAccepted(rs.getBuilt())) {
                    JmriJOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString(
                            "rsTrainNotServBuilt"), new Object[]{rs.getBuilt(), train.getName()}), getRb()
                                    .getString("rsNotMove"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    // prevent rs from being picked up and delivered
                    setRouteLocationAndDestination(rs, train, null, null);
                    return false;
                }
                // determine if train services this rs's owner
                if (!train.isOwnerNameAccepted(rs.getOwnerName())) {
                    JmriJOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString(
                            "rsTrainNotServOwner"), new Object[]{rs.getOwnerName(), train.getName()}), getRb()
                                    .getString("rsNotMove"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    // prevent rs from being picked up and delivered
                    setRouteLocationAndDestination(rs, train, null, null);
                    return false;
                }
                // determine if train services the location and destination selected by user
                rl = null;
                rd = null;
                if (rs.getLocation() != null) {
                    Route route = train.getRoute();
                    if (route != null) {
                        // this is a quick check, the actual rl and rd are set later in this routine.
                        rl = route.getLastLocationByName(rs.getLocationName());
                        rd = route.getLastLocationByName(rs.getDestinationName());
                    }
                    if (rl == null) {
                        JmriJOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString(
                                "rsLocNotServ"), new Object[]{rs.getLocationName(), train.getName()}),
                                getRb().getString("rsNotMove"), JmriJOptionPane.ERROR_MESSAGE);
                        // prevent rs from being picked up and delivered
                        setRouteLocationAndDestination(rs, train, null, null);
                        return false;
                    }
                    if (rd == null && !rs.getDestinationName().equals(RollingStock.NONE)) {
                        JmriJOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString(
                                "rsDestNotServ"), new Object[]{rs.getDestinationName(), train.getName()}),
                                getRb().getString("rsNotMove"), JmriJOptionPane.ERROR_MESSAGE);
                        // prevent rs from being picked up and delivered
                        setRouteLocationAndDestination(rs, train, null, null);
                        return false;
                    }
                    if (rd != null && route != null) {
                        // now determine if destination is after location
                        List<RouteLocation> routeSequence = route.getLocationsBySequenceList();
                        boolean foundTrainLoc = false; // when true, found the train's location
                        boolean foundLoc = false; // when true, found the rs's location in the route
                        boolean foundDes = false;
                        for (RouteLocation rlocation : routeSequence) {
                            if (train.isTrainEnRoute() && !foundTrainLoc) {
                                if (train.getCurrentRouteLocation() != rlocation) {
                                    continue;
                                }
                                foundTrainLoc = true;
                            }
                            if (rs.getLocationName().equals(rlocation.getName())) {
                                rl = rlocation;
                                foundLoc = true;
                            }
                            if (rs.getDestinationName().equals(rlocation.getName()) && foundLoc) {
                                rd = rlocation;
                                foundDes = true;
                                if (rs.getDestinationTrack() != null &&
                                        (rlocation.getTrainDirection() &
                                                rs.getDestinationTrack().getTrainDirections()) == 0) {
                                    continue; // destination track isn't serviced by the train's direction
                                }
                                break;
                            }
                        }
                        if (!foundLoc) {
                            JmriJOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString(
                                    "rsTrainEnRoute"),
                                    new Object[]{rs.toString(), train.getName(),
                                            rs.getLocationName()}),
                                    getRb().getString("rsNotMove"),
                                    JmriJOptionPane.ERROR_MESSAGE);
                            // prevent rs from being picked up and delivered
                            setRouteLocationAndDestination(rs, train, null, null);
                            return false;
                        }
                        if (!foundDes) {
                            JmriJOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString(
                                    "rsLocOrder"),
                                    new Object[]{rs.getDestinationName(),
                                            rs.getLocationName(), train.getName()}),
                                    getRb().getString("rsNotMove"),
                                    JmriJOptionPane.ERROR_MESSAGE);
                            // prevent rs from being picked up and delivered
                            setRouteLocationAndDestination(rs, train, null, null);
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    protected boolean changeLocation(RollingStock rs) {
        if (!ignoreLocationCheckBox.isSelected()) {
            if (locationBox.getSelectedItem() == null) {
                rs.setLocation(null, null);
            } else {
                if (trackLocationBox.getSelectedItem() == null) {
                    JmriJOptionPane.showMessageDialog(this, getRb().getString("rsFullySelect"), getRb()
                            .getString("rsCanNotLoc"), JmriJOptionPane.ERROR_MESSAGE);
                    return false;
                }
                // update location only if it has changed
                if (rs.getLocation() == null ||
                        !rs.getLocation().equals(locationBox.getSelectedItem()) ||
                        rs.getTrack() == null ||
                        !rs.getTrack().equals(trackLocationBox.getSelectedItem())) {
                    String status = rs.setLocation((Location) locationBox.getSelectedItem(),
                            (Track) trackLocationBox.getSelectedItem());
                    rs.setLastRouteId(RollingStock.NONE); // clear last route id
                    if (!status.equals(Track.OKAY)) {
                        log.debug("Can't set rs's location because of {}", status);
                        JmriJOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString(
                                "rsCanNotLocMsg"), new Object[]{rs.toString(), status}), getRb()
                                        .getString("rsCanNotLoc"),
                                JmriJOptionPane.ERROR_MESSAGE);
                        // does the user want to force the rolling stock to this track?
                        int results = JmriJOptionPane.showOptionDialog(this, MessageFormat.format(getRb()
                                .getString("rsForce"),
                                new Object[]{rs.toString(),
                                        (Track) trackLocationBox.getSelectedItem()}),
                                MessageFormat.format(getRb()
                                        .getString("rsOverride"), new Object[]{status}),
                                JmriJOptionPane.YES_NO_OPTION, JmriJOptionPane.QUESTION_MESSAGE, null, null, null);
                        if (results == JmriJOptionPane.YES_OPTION) {
                            log.debug("Force rolling stock to track");
                            rs.setLocation((Location) locationBox.getSelectedItem(), (Track) trackLocationBox
                                    .getSelectedItem(), RollingStock.FORCE);
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private void loadTrain(RollingStock rs) {
        if (!ignoreTrainCheckBox.isSelected()) {
            if (trainBox.getSelectedItem() == null) {
                if (rs.getTrain() != null) {
                    // prevent rs from being picked up and delivered
                    setRouteLocationAndDestination(rs, rs.getTrain(), null, null);
                }
                rs.setTrain(null);
            } else {
                Train train = (Train) trainBox.getSelectedItem();
                if (rs.getTrain() != null && !rs.getTrain().equals(train)) {
                    // prevent rs from being picked up and delivered
                    setRouteLocationAndDestination(rs, rs.getTrain(), null, null);
                }
                rs.setTrain(train);
            }
        }
    }

    private boolean changeDestination(RollingStock rs) {
        if (!ignoreDestinationCheckBox.isSelected()) {
            if (destinationBox.getSelectedItem() == null) {
                rs.setDestination(null, null);
            } else {
                Track destTrack = null;
                if (trackDestinationBox.getSelectedItem() != null) {
                    destTrack = (Track) trackDestinationBox.getSelectedItem();
                }
                log.debug("changeDestination: {}, ({})", destinationBox.getSelectedItem(),
                        destTrack);
                if (destTrack != null &&
                        rs.getDestinationTrack() != destTrack &&
                        destTrack.isStaging() &&
                        (rs.getTrain() == null || !rs.getTrain().isBuilt())) {
                    log.debug("Destination track ({}) is staging", destTrack.getName());
                    JmriJOptionPane.showMessageDialog(this, getRb().getString("rsDoNotSelectStaging"), getRb()
                            .getString("rsCanNotDest"), JmriJOptionPane.ERROR_MESSAGE);
                    return false;
                }
                // determine is user changed the destination track and is part of train
                if (destTrack != null &&
                        rs.getDestinationTrack() != destTrack &&
                        rs.getTrain() != null &&
                        rs.getTrain().isBuilt() &&
                        rs.getRouteLocation() != null) {
                    log.debug("Rolling stock ({}) has new track destination in built train ({})",
                            rs.toString(), rs.getTrainName());
                    rs.getTrain().setModified(true);
                }
                String status = rs.setDestination((Location) destinationBox.getSelectedItem(), destTrack);
                if (!status.equals(Track.OKAY)) {
                    log.debug("Can't set rs's destination because of {}", status);
                    JmriJOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString(
                            "rsCanNotDestMsg"), new Object[]{rs.toString(), status}), getRb().getString(
                                    "rsCanNotDest"),
                            JmriJOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * Checks to see if rolling stock's location or destination has changed, and
     * if so, removes the rolling stock from the train. Also allows user to add or
     * remove rolling stock to or from train.
     */
    protected void checkTrain(RollingStock rs) {
        Train train = rs.getTrain();
        if (train != null && train.isBuilt()) {
            if (rs.getRouteLocation() != null &&
                    rs.getRouteDestination() != null &&
                    rl != null &&
                    rd != null &&
                    (!rs.getRouteLocation().getName().equals(rl.getName()) ||
                            !rs.getRouteDestination().getName().equals(rd.getName()) ||
                            rs.getDestinationTrack() == null)) {
                // user changed rolling stock location or destination or no destination track
                setRouteLocationAndDestination(rs, train, null, null);
            }
            if (rs.getRouteLocation() != null || rs.getRouteDestination() != null) {
                if (JmriJOptionPane.showConfirmDialog(this, MessageFormat.format(getRb().getString(
                        "rsRemoveRsFromTrain"), new Object[]{rs.toString(), train.getName()}), getRb()
                                .getString("rsInRoute"),
                        JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
                    // prevent rs from being picked up and delivered
                    setRouteLocationAndDestination(rs, train, null, null);
                }
            } else if (rl != null && rd != null && rs.getDestinationTrack() != null) {
                if (rs.getDestinationTrack().getLocation().isStaging() &&
                        !rs.getDestinationTrack().equals(train.getTerminationTrack())) {
                    log.debug("Rolling stock destination track is staging and not the same as train");
                    JmriJOptionPane.showMessageDialog(this, 
                            Bundle.getMessage("rsMustSelectSameTrack", train.getTerminationTrack()
                                    .getName()),
                            Bundle.getMessage("rsStagingTrackError"), JmriJOptionPane.ERROR_MESSAGE);
                } else if (JmriJOptionPane.showConfirmDialog(this, MessageFormat.format(
                        getRb().getString("rsAddRsToTrain"), new Object[]{rs.toString(), train.getName()}),
                        getRb().getString("rsAddManuallyToTrain"),
                        JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
                    // set new pick up and set out locations
                    setRouteLocationAndDestination(rs, train, rl, rd);
                    log.debug("Add rolling stock ({}) to train ({}) route pick up {} drop {}", rs.toString(), train
                            .getName(), rl.getId(), rd.getId()); // NOI18N
                }
            }
        }
    }

    protected void setRouteLocationAndDestination(RollingStock rs, Train train, RouteLocation rl,
            RouteLocation rd) {
        if (rs.getRouteLocation() != null || rl != null) {
            train.setModified(true);
        }
        // check destination track is staging
        if (rl == null &&
                rd == null &&
                rs.getDestinationTrack() != null &&
                rs.getDestinationTrack().getLocation().isStaging()) {
            log.debug("Rolling stock destination track is staging");
            rs.setDestination(null, null);
        }
        rs.setRouteLocation(rl);
        rs.setRouteDestination(rd);
    }

    protected void updateComboBoxes() {
        log.debug("update combo boxes");
        locationManager.updateComboBox(locationBox);
        locationManager.updateComboBox(destinationBox);
        locationManager.updateComboBox(finalDestinationBox);

        updateLocationComboBoxes();
        updateDestinationComboBoxes();
    }

    protected boolean updateGroup(List<T> list) {
        for (RollingStock rs : list) {
            if (rs == _rs) {
                continue;
            }
            // Location status and out of service
            if (!ignoreStatusCheckBox.isSelected()) {
                rs.setLocationUnknown(locationUnknownCheckBox.isSelected());
                rs.setOutOfService(outOfServiceCheckBox.isSelected());
            }
            // update location and destination
            if (!changeLocation(rs)) {
                return false;
            }
            if (!changeDestination(rs)) {
                return false;
            }

            if (!ignoreTrainCheckBox.isSelected()) {
                if (trainBox.getSelectedItem() == null) {
                    rs.setTrain(null);
                } else {
                    rs.setTrain((Train) trainBox.getSelectedItem());
                }
            }
            // set the route location and destination to match
            rs.setRouteLocation(_rs.getRouteLocation());
            rs.setRouteDestination(_rs.getRouteDestination());
        }
        return true;
    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("checkbox action ");
        if (ae.getSource() == locationUnknownCheckBox) {
            outOfServiceCheckBox.setSelected(locationUnknownCheckBox.isSelected());
            enableComponents(!locationUnknownCheckBox.isSelected());
        }
        if (ae.getSource() == autoTrackCheckBox) {
            updateLocationTrackComboBox();
        }
        if (ae.getSource() == autoDestinationTrackCheckBox) {
            updateDestinationTrackComboBox();
        }
        if (ae.getSource() == ignoreStatusCheckBox) {
            locationUnknownCheckBox.setEnabled(!ignoreStatusCheckBox.isSelected());
            outOfServiceCheckBox.setEnabled(!ignoreStatusCheckBox.isSelected());
        }
        if (ae.getSource() == ignoreLocationCheckBox) {
            locationBox.setEnabled(!ignoreLocationCheckBox.isSelected());
            trackLocationBox.setEnabled(!ignoreLocationCheckBox.isSelected());
            autoTrackCheckBox.setEnabled(!ignoreLocationCheckBox.isSelected());
        }
        if (ae.getSource() == ignoreDestinationCheckBox) {
            destinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected());
            trackDestinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected());
            autoDestinationTrackCheckBox.setEnabled(!ignoreDestinationCheckBox.isSelected());
        }
        if (ae.getSource() == ignoreFinalDestinationCheckBox) {
            finalDestinationBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected());
            finalDestTrackBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected());
            autoFinalDestTrackCheckBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected());
        }
        if (ae.getSource() == ignoreTrainCheckBox) {
            trainBox.setEnabled(!ignoreTrainCheckBox.isSelected());
            autoTrainCheckBox.setEnabled(!ignoreTrainCheckBox.isSelected());
        }
    }

    protected void enableComponents(boolean enabled) {
        // combo boxes
        locationBox.setEnabled(!ignoreLocationCheckBox.isSelected() & enabled);
        trackLocationBox.setEnabled(!ignoreLocationCheckBox.isSelected() & enabled);
        destinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected() & enabled);
        trackDestinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected() & enabled);
        finalDestinationBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected() & enabled);
        finalDestTrackBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected() & enabled);
        trainBox.setEnabled(!ignoreTrainCheckBox.isSelected() & enabled);
        // checkboxes
        autoTrackCheckBox.setEnabled(!ignoreLocationCheckBox.isSelected() & enabled);
        autoDestinationTrackCheckBox.setEnabled(!ignoreDestinationCheckBox.isSelected() & enabled);
        autoFinalDestTrackCheckBox.setEnabled(!ignoreFinalDestinationCheckBox.isSelected() & enabled);
        autoTrainCheckBox.setEnabled(!ignoreTrainCheckBox.isSelected() & enabled);
        locationUnknownCheckBox.setEnabled(!ignoreStatusCheckBox.isSelected());
        outOfServiceCheckBox.setEnabled(!ignoreStatusCheckBox.isSelected() & enabled);

        ignoreStatusCheckBox.setEnabled(enabled);
        ignoreLocationCheckBox.setEnabled(enabled);
        ignoreDestinationCheckBox.setEnabled(enabled);
        ignoreFinalDestinationCheckBox.setEnabled(Setup.isCarRoutingEnabled() & enabled);
        ignoreTrainCheckBox.setEnabled(enabled);
    }

    // location combo box
    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == locationBox) {
            updateLocationTrackComboBox();
        }
        if (ae.getSource() == destinationBox || ae.getSource() == trainBox) {
            updateDestinationTrackComboBox();
        }
    }

    protected void updateLocationComboBoxes() {
        log.debug("update location combo boxes");
        if (_rs != null) {
            locationBox.setSelectedItem(_rs.getLocation());
        }
        // now update track combo boxes
        updateLocationTrackComboBox();
    }

    protected void updateLocationTrackComboBox() {
        log.debug("update location track combobox");
        if (locationBox.getSelectedItem() == null) {
            trackLocationBox.removeAllItems();
        } else {
            log.debug("RollingStockFrame sees location: {}", locationBox.getSelectedItem());
            Location l = (Location) locationBox.getSelectedItem();
            l.updateComboBox(trackLocationBox, _rs, autoTrackCheckBox.isSelected(), false);
            if (_rs != null && _rs.getLocation() != null && _rs.getLocation().equals(l) && _rs.getTrack() != null) {
                trackLocationBox.setSelectedItem(_rs.getTrack());
            }
        }
    }

    protected void updateDestinationComboBoxes() {
        log.debug("update destination combo boxes");
        if (_rs != null) {
            destinationBox.setSelectedItem(_rs.getDestination());
        }
        // now update track combo boxes
        updateDestinationTrackComboBox();
    }

    protected void updateDestinationTrackComboBox() {
        log.debug("update destination track combobox");
        if (destinationBox.getSelectedItem() == null) {
            trackDestinationBox.removeAllItems();
        } else {
            log.debug("updateDestinationTrackComboBox destination: {}, ({})", destinationBox.getSelectedItem(),
                    trackDestinationBox.getSelectedItem());
            Location destination = (Location) destinationBox.getSelectedItem();
            Track track = null;
            if (trackDestinationBox.getSelectedItem() != null) {
                track = (Track) trackDestinationBox.getSelectedItem();
            }
            destination.updateComboBox(trackDestinationBox, _rs, autoDestinationTrackCheckBox.isSelected(), true);
            // check for staging, add track if train is built and terminates into staging
            if (autoDestinationTrackCheckBox.isSelected() && trainBox.getSelectedItem() != null) {
                Train train = (Train) trainBox.getSelectedItem();
                if (train.isBuilt() &&
                        train.getTerminationTrack() != null &&
                        train.getTerminationTrack().getLocation() == destination) {
                    trackDestinationBox.addItem(train.getTerminationTrack());
                    trackDestinationBox.setSelectedItem(track);
                }
            }
            if (_rs != null &&
                    _rs.getDestination() != null &&
                    _rs.getDestination().equals(destination) &&
                    _rs.getDestinationTrack() != null) {
                trackDestinationBox.setSelectedItem(_rs.getDestinationTrack());
            } else if (track != null) {
                trackDestinationBox.setSelectedItem(track);
            }
        }
    }

    protected void updateTrainComboBox() {
        log.debug("update train combo box");
        trainManager.updateTrainComboBox(trainBox);
        if (_rs != null) {
            trainBox.setSelectedItem(_rs.getTrain());
        }
    }

    @Override
    public void dispose() {
        if (_rs != null) {
            _rs.removePropertyChangeListener(this);
        }
        locationManager.removePropertyChangeListener(this);
        trainManager.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("PropertyChange ({}) new: ({})", e.getPropertyName(), e.getNewValue());
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateComboBoxes();
        }
        if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateTrainComboBox();
        }
        if (e.getPropertyName().equals(RollingStock.TRACK_CHANGED_PROPERTY)) {
            updateLocationComboBoxes();
        }
        if (e.getPropertyName().equals(RollingStock.DESTINATION_TRACK_CHANGED_PROPERTY)) {
            updateDestinationComboBoxes();
        }
        if (e.getPropertyName().equals(RollingStock.TRAIN_CHANGED_PROPERTY)) {
            if (_rs != null) {
                trainBox.setSelectedItem(_rs.getTrain());
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RollingStockSetFrame.class);
}
