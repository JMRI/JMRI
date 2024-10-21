package jmri.jmrit.operations.locations;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import jmri.*;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.tools.*;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.*;
import jmri.swing.NamedBeanComboBox;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user edit of tracks. Base for edit of all track types.
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012, 2013, 2023
 */
public abstract class TrackEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    // where in the tool menu new items are inserted
    protected static final int TOOL_MENU_OFFSET = 6;

    // Managers
    TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
    RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);

    public Location _location = null;
    public Track _track = null;
    String _type = "";
    JMenu _toolMenu = new JMenu(Bundle.getMessage("MenuTools"));

    List<JCheckBox> checkBoxes = new ArrayList<>();

    // panels
    JPanel panelCheckBoxes = new JPanel();
    JScrollPane paneCheckBoxes = new JScrollPane(panelCheckBoxes);
    JPanel panelTrainDir = new JPanel();
    JPanel pShipLoadOption = new JPanel();
    JPanel pDestinationOption = new JPanel();
    JPanel panelOrder = new JPanel();

    // Alternate tool buttons
    JButton loadOptionButton = new JButton(Bundle.getMessage("AcceptsAllLoads"));
    JButton shipLoadOptionButton = new JButton(Bundle.getMessage("ShipsAllLoads"));
    JButton roadOptionButton = new JButton(Bundle.getMessage("AcceptsAllRoads"));
    JButton destinationOptionButton = new JButton();

    // major buttons
    JButton clearButton = new JButton(Bundle.getMessage("ClearAll"));
    JButton setButton = new JButton(Bundle.getMessage("SelectAll"));
    JButton autoSelectButton = new JButton(Bundle.getMessage("AutoSelect"));
    
    JButton saveTrackButton = new JButton(Bundle.getMessage("SaveTrack"));
    JButton deleteTrackButton = new JButton(Bundle.getMessage("DeleteTrack"));
    JButton addTrackButton = new JButton(Bundle.getMessage("AddTrack"));

    JButton deleteDropButton = new JButton(Bundle.getMessage("ButtonDelete"));
    JButton addDropButton = new JButton(Bundle.getMessage("Add"));
    JButton deletePickupButton = new JButton(Bundle.getMessage("ButtonDelete"));
    JButton addPickupButton = new JButton(Bundle.getMessage("Add"));

    // check boxes
    JCheckBox northCheckBox = new JCheckBox(Bundle.getMessage("North"));
    JCheckBox southCheckBox = new JCheckBox(Bundle.getMessage("South"));
    JCheckBox eastCheckBox = new JCheckBox(Bundle.getMessage("East"));
    JCheckBox westCheckBox = new JCheckBox(Bundle.getMessage("West"));
    JCheckBox autoDropCheckBox = new JCheckBox(Bundle.getMessage("Auto"));
    JCheckBox autoPickupCheckBox = new JCheckBox(Bundle.getMessage("Auto"));

    // car pick up order controls
    JRadioButton orderNormal = new JRadioButton(Bundle.getMessage("Normal"));
    JRadioButton orderFIFO = new JRadioButton(Bundle.getMessage("DescriptiveFIFO"));
    JRadioButton orderLIFO = new JRadioButton(Bundle.getMessage("DescriptiveLIFO"));

    JRadioButton anyDrops = new JRadioButton(Bundle.getMessage("Any"));
    JRadioButton trainDrop = new JRadioButton(Bundle.getMessage("Trains"));
    JRadioButton routeDrop = new JRadioButton(Bundle.getMessage("Routes"));
    JRadioButton excludeTrainDrop = new JRadioButton(Bundle.getMessage("ExcludeTrains"));
    JRadioButton excludeRouteDrop = new JRadioButton(Bundle.getMessage("ExcludeRoutes"));

    JRadioButton anyPickups = new JRadioButton(Bundle.getMessage("Any"));
    JRadioButton trainPickup = new JRadioButton(Bundle.getMessage("Trains"));
    JRadioButton routePickup = new JRadioButton(Bundle.getMessage("Routes"));
    JRadioButton excludeTrainPickup = new JRadioButton(Bundle.getMessage("ExcludeTrains"));
    JRadioButton excludeRoutePickup = new JRadioButton(Bundle.getMessage("ExcludeRoutes"));

    JComboBox<Train> comboBoxDropTrains = trainManager.getTrainComboBox();
    JComboBox<Route> comboBoxDropRoutes = routeManager.getComboBox();
    JComboBox<Train> comboBoxPickupTrains = trainManager.getTrainComboBox();
    JComboBox<Route> comboBoxPickupRoutes = routeManager.getComboBox();

    // text field
    JTextField trackNameTextField = new JTextField(Control.max_len_string_track_name);
    JTextField trackLengthTextField = new JTextField(Control.max_len_string_track_length_name);

    // text area
    JTextArea commentTextArea = new JTextArea(2, 60);
    JScrollPane commentScroller = new JScrollPane(commentTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // optional panel for spurs, staging, and interchanges
    JPanel dropPanel = new JPanel();
    JPanel pickupPanel = new JPanel();
    JPanel panelOpt3 = new JPanel(); // not currently used
    JPanel panelOpt4 = new JPanel();

    // Reader selection dropdown.
    NamedBeanComboBox<Reporter> readerSelector;

    public static final String DISPOSE = "dispose"; // NOI18N
    public static final int MAX_NAME_LENGTH = Control.max_len_string_track_name;

    public TrackEditFrame(String title) {
        super(title);
    }

    protected abstract void initComponents(Track track);

    public void initComponents(Location location, Track track) {
        _location = location;
        _track = track;

        // tool tips
        autoDropCheckBox.setToolTipText(Bundle.getMessage("TipAutoTrack"));
        autoPickupCheckBox.setToolTipText(Bundle.getMessage("TipAutoTrack"));
        trackLengthTextField.setToolTipText(Bundle.getMessage("TipTrackLength",
                Setup.getLengthUnit().toLowerCase()));

        // property changes
        _location.addPropertyChangeListener(this);
        // listen for car road name and type changes
        InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarLoads.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(Setup.class).addPropertyChangeListener(this);
        trainManager.addPropertyChangeListener(this);
        routeManager.addPropertyChangeListener(this);

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // place all panels in a scroll pane.
        JPanel panels = new JPanel();
        panels.setLayout(new BoxLayout(panels, BoxLayout.Y_AXIS));
        JScrollPane pane = new JScrollPane(panels);

        // row 1
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        JScrollPane p1Pane = new JScrollPane(p1);
        p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        p1Pane.setMinimumSize(new Dimension(300, 3 * trackNameTextField.getPreferredSize().height));
        p1Pane.setBorder(BorderFactory.createTitledBorder(""));

        // row 1a
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
        addItem(pName, trackNameTextField, 0, 0);

        // row 1b
        JPanel pLength = new JPanel();
        pLength.setLayout(new GridBagLayout());
        pLength.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Length")));
        pLength.setMinimumSize(new Dimension(60, 1));
        addItem(pLength, trackLengthTextField, 0, 0);

        // row 1c
        panelTrainDir.setLayout(new GridBagLayout());
        panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainTrack")));
        panelTrainDir.setPreferredSize(new Dimension(200, 10));
        addItem(panelTrainDir, northCheckBox, 1, 1);
        addItem(panelTrainDir, southCheckBox, 2, 1);
        addItem(panelTrainDir, eastCheckBox, 3, 1);
        addItem(panelTrainDir, westCheckBox, 4, 1);

        p1.add(pName);
        p1.add(pLength);
        p1.add(panelTrainDir);

        // row 2
        paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesTrack")));
        panelCheckBoxes.setLayout(new GridBagLayout());

        // status panel for roads and loads
        JPanel panelRoadAndLoadStatus = new JPanel();
        panelRoadAndLoadStatus.setLayout(new BoxLayout(panelRoadAndLoadStatus, BoxLayout.X_AXIS));

        // row 3
        JPanel pRoadOption = new JPanel();
        pRoadOption.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RoadOption")));
        pRoadOption.add(roadOptionButton);
        roadOptionButton.addActionListener(new TrackRoadEditAction(this));

        JPanel pLoadOption = new JPanel();
        pLoadOption.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LoadOption")));
        pLoadOption.add(loadOptionButton);
        loadOptionButton.addActionListener(new TrackLoadEditAction(this));

        pShipLoadOption.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ShipLoadOption")));
        pShipLoadOption.add(shipLoadOptionButton);
        shipLoadOptionButton.addActionListener(new TrackLoadEditAction(this));

        pDestinationOption.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Destinations")));
        pDestinationOption.add(destinationOptionButton);
        destinationOptionButton.addActionListener(new TrackDestinationEditAction(this));

        panelRoadAndLoadStatus.add(pRoadOption);
        panelRoadAndLoadStatus.add(pLoadOption);
        panelRoadAndLoadStatus.add(pShipLoadOption);
        panelRoadAndLoadStatus.add(pDestinationOption);

        // only staging uses the ship load option
        pShipLoadOption.setVisible(false);
        // only classification/interchange tracks use the destination option
        pDestinationOption.setVisible(false);

        // row 4, order panel
        panelOrder.setLayout(new GridBagLayout());
        panelOrder.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("PickupOrder")));
        panelOrder.add(orderNormal);
        panelOrder.add(orderFIFO);
        panelOrder.add(orderLIFO);

        ButtonGroup orderGroup = new ButtonGroup();
        orderGroup.add(orderNormal);
        orderGroup.add(orderFIFO);
        orderGroup.add(orderLIFO);

        // row 5, drop panel
        dropPanel.setLayout(new GridBagLayout());
        dropPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainsOrRoutesDrops")));

        // row 6, pickup panel
        pickupPanel.setLayout(new GridBagLayout());
        pickupPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainsOrRoutesPickups")));

        // row 9
        JPanel panelComment = new JPanel();
        panelComment.setLayout(new GridBagLayout());
        panelComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        addItem(panelComment, commentScroller, 0, 0);

        // adjust text area width based on window size
        adjustTextAreaColumnWidth(commentScroller, commentTextArea);

        // row 10, reader row
        JPanel readerPanel = new JPanel();
        if (Setup.isRfidEnabled()) {
            readerPanel.setLayout(new GridBagLayout());
            readerPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("idReporter")));
            ReporterManager reporterManager = InstanceManager.getDefault(ReporterManager.class);
            readerSelector = new NamedBeanComboBox<>(reporterManager);
            readerSelector.setAllowNull(true);
            addItem(readerPanel, readerSelector, 0, 0);
        } else {
            readerPanel.setVisible(false);
        }

        // row 11
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new GridBagLayout());

        addItem(panelButtons, deleteTrackButton, 0, 0);
        addItem(panelButtons, addTrackButton, 1, 0);
        addItem(panelButtons, saveTrackButton, 2, 0);

        panels.add(p1Pane);
        panels.add(paneCheckBoxes);
        panels.add(panelRoadAndLoadStatus);
        panels.add(panelOrder);
        panels.add(dropPanel);
        panels.add(pickupPanel);

        // add optional panels
        panels.add(panelOpt3);
        panels.add(panelOpt4);

        panels.add(panelComment);
        panels.add(readerPanel);
        panels.add(panelButtons);

        getContentPane().add(pane);

        // setup buttons
        addButtonAction(setButton);
        addButtonAction(clearButton);

        addButtonAction(deleteTrackButton);
        addButtonAction(addTrackButton);
        addButtonAction(saveTrackButton);

        addButtonAction(deleteDropButton);
        addButtonAction(addDropButton);
        addButtonAction(deletePickupButton);
        addButtonAction(addPickupButton);

        addRadioButtonAction(orderNormal);
        addRadioButtonAction(orderFIFO);
        addRadioButtonAction(orderLIFO);

        addRadioButtonAction(anyDrops);
        addRadioButtonAction(trainDrop);
        addRadioButtonAction(routeDrop);
        addRadioButtonAction(excludeTrainDrop);
        addRadioButtonAction(excludeRouteDrop);

        addRadioButtonAction(anyPickups);
        addRadioButtonAction(trainPickup);
        addRadioButtonAction(routePickup);
        addRadioButtonAction(excludeTrainPickup);
        addRadioButtonAction(excludeRoutePickup);

        // addComboBoxAction(comboBoxTypes);
        addCheckBoxAction(autoDropCheckBox);
        addCheckBoxAction(autoPickupCheckBox);

        autoDropCheckBox.setSelected(true);
        autoPickupCheckBox.setSelected(true);

        // load fields and enable buttons
        if (_track != null) {
            _track.addPropertyChangeListener(this);
            trackNameTextField.setText(_track.getName());
            commentTextArea.setText(_track.getComment());
            trackLengthTextField.setText(Integer.toString(_track.getLength()));
            enableButtons(true);
            if (Setup.isRfidEnabled()) {
                readerSelector.setSelectedItem(_track.getReporter());
            }
        } else {
            enableButtons(false);
        }

        // build menu
        JMenuBar menuBar = new JMenuBar();
        // spurs, interchanges, and staging insert menu items here
        _toolMenu.add(new TrackLoadEditAction(this));
        _toolMenu.add(new TrackRoadEditAction(this));
        _toolMenu.add(new PoolTrackAction(this));
        _toolMenu.add(new IgnoreUsedTrackAction(this));
        _toolMenu.add(new TrackEditCommentsAction(this));
        _toolMenu.addSeparator();
        // spurs, interchanges, and yards insert menu items here
        _toolMenu.add(new TrackCopyAction(_track, _location));
        _toolMenu.addSeparator();
        _toolMenu.add(new ShowCarsByLocationAction(false, _location, _track));
        _toolMenu.add(new ShowTrainsServingLocationAction(_location, _track));

        menuBar.add(_toolMenu);
        setJMenuBar(menuBar);

        // load
        updateCheckboxes();
        updateTrainDir();
        updateCarOrder();
        updateDropOptions();
        updatePickupOptions();
        updateRoadOption();
        updateLoadOption();
        updateDestinationOption();
        updateTrainComboBox();
        updateRouteComboBox();

        setMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight600));
    }

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addTrackButton) {
            addNewTrack();
        }
        if (_track == null) {
            return; // not possible
        }
        if (ae.getSource() == saveTrackButton) {
            if (!checkUserInputs(_track)) {
                return;
            }
            saveTrack(_track);
            checkTrackPickups(_track); // warn user if there are car types that
                                       // will be stranded
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == deleteTrackButton) {
            deleteTrack();
        }
        if (ae.getSource() == setButton) {
            selectCheckboxes(true);
        }
        if (ae.getSource() == clearButton) {
            selectCheckboxes(false);
        }
        if (ae.getSource() == addDropButton) {
            addDropId();
        }
        if (ae.getSource() == deleteDropButton) {
            deleteDropId();
        }
        if (ae.getSource() == addPickupButton) {
            addPickupId();
        }
        if (ae.getSource() == deletePickupButton) {
            deletePickupId();
        }
    }

    private void addDropId() {
        String id = "";
        if (trainDrop.isSelected() || excludeTrainDrop.isSelected()) {
            if (comboBoxDropTrains.getSelectedItem() == null) {
                return;
            }
            Train train = ((Train) comboBoxDropTrains.getSelectedItem());
            Route route = train.getRoute();
            id = train.getId();
            if (!checkRoute(route)) {
                JmriJOptionPane.showMessageDialog(this,
                        Bundle.getMessage("TrackNotByTrain", train.getName()),
                        Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            selectNextItemComboBox(comboBoxDropTrains);
        } else {
            if (comboBoxDropRoutes.getSelectedItem() == null) {
                return;
            }
            Route route = ((Route) comboBoxDropRoutes.getSelectedItem());
            id = route.getId();
            if (!checkRoute(route)) {
                JmriJOptionPane.showMessageDialog(this,
                        Bundle.getMessage("TrackNotByRoute", route.getName()),
                        Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            selectNextItemComboBox(comboBoxDropRoutes);
        }
        _track.addDropId(id);
    }

    private void deleteDropId() {
        String id = "";
        if (trainDrop.isSelected() || excludeTrainDrop.isSelected()) {
            if (comboBoxDropTrains.getSelectedItem() == null) {
                return;
            }
            id = ((Train) comboBoxDropTrains.getSelectedItem()).getId();
            selectNextItemComboBox(comboBoxDropTrains);
        } else {
            if (comboBoxDropRoutes.getSelectedItem() == null) {
                return;
            }
            id = ((Route) comboBoxDropRoutes.getSelectedItem()).getId();
            selectNextItemComboBox(comboBoxDropRoutes);
        }
        _track.deleteDropId(id);
    }

    private void addPickupId() {
        String id = "";
        if (trainPickup.isSelected() || excludeTrainPickup.isSelected()) {
            if (comboBoxPickupTrains.getSelectedItem() == null) {
                return;
            }
            Train train = ((Train) comboBoxPickupTrains.getSelectedItem());
            Route route = train.getRoute();
            id = train.getId();
            if (!checkRoute(route)) {
                JmriJOptionPane.showMessageDialog(this,
                        Bundle.getMessage("TrackNotByTrain", train.getName()),
                        Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            selectNextItemComboBox(comboBoxPickupTrains);
        } else {
            if (comboBoxPickupRoutes.getSelectedItem() == null) {
                return;
            }
            Route route = ((Route) comboBoxPickupRoutes.getSelectedItem());
            id = route.getId();
            if (!checkRoute(route)) {
                JmriJOptionPane.showMessageDialog(this,
                        Bundle.getMessage("TrackNotByRoute", route.getName()),
                        Bundle.getMessage("ErrorTitle"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            selectNextItemComboBox(comboBoxPickupRoutes);
        }
        _track.addPickupId(id);
    }

    private void deletePickupId() {
        String id = "";
        if (trainPickup.isSelected() || excludeTrainPickup.isSelected()) {
            if (comboBoxPickupTrains.getSelectedItem() == null) {
                return;
            }
            id = ((Train) comboBoxPickupTrains.getSelectedItem()).getId();
            selectNextItemComboBox(comboBoxPickupTrains);
        } else {
            if (comboBoxPickupRoutes.getSelectedItem() == null) {
                return;
            }
            id = ((Route) comboBoxPickupRoutes.getSelectedItem()).getId();
            selectNextItemComboBox(comboBoxPickupRoutes);
        }
        _track.deletePickupId(id);
    }

    protected void addNewTrack() {
        // check that track name is valid
        if (!checkName(Bundle.getMessage("add"))) {
            return;
        }
        // check to see if track already exists
        Track check = _location.getTrackByName(trackNameTextField.getText(), null);
        if (check != null) {
            reportTrackExists(Bundle.getMessage("add"));
            return;
        }
        // add track to this location
        _track = _location.addTrack(trackNameTextField.getText(), _type);
        // check track length
        checkLength(_track);

        // save window size so it doesn't change during the following updates
        setPreferredSize(getSize());

        // reset all of the track's attributes
        updateTrainDir();
        updateCheckboxes();
        updateDropOptions();
        updatePickupOptions();
        updateRoadOption();
        updateLoadOption();
        updateDestinationOption();

        _track.addPropertyChangeListener(this);

        // setup check boxes
        selectCheckboxes(true);
        // store comment
        _track.setComment(commentTextArea.getText());
        // enable
        enableButtons(true);
        // save location file
        OperationsXml.save();
    }

    protected void deleteTrack() {
        if (_track != null) {
            int rs = _track.getNumberRS();
            if (rs > 0) {
                if (JmriJOptionPane.showConfirmDialog(this,
                        Bundle.getMessage("ThereAreCars", Integer.toString(rs)),
                        Bundle.getMessage("deleteTrack?"),
                        JmriJOptionPane.YES_NO_OPTION) != JmriJOptionPane.YES_OPTION) {
                    return;
                }
            }
            selectCheckboxes(false);
            _location.deleteTrack(_track);
            _track = null;
            enableButtons(false);
            // save location file
            OperationsXml.save();
        }
    }

    // check to see if the route services this location
    private boolean checkRoute(Route route) {
        if (route == null) {
            return false;
        }
        return route.getLastLocationByName(_location.getName()) != null;
    }

    protected void saveTrack(Track track) {
        saveTrackDirections(track);
        track.setName(trackNameTextField.getText());
        track.setComment(commentTextArea.getText());

        if (Setup.isRfidEnabled()) {
            _track.setReporter(readerSelector.getSelectedItem());
        }

        // save current window size so it doesn't change during updates
        setPreferredSize(getSize());

        // enable
        enableButtons(true);
        // save location file
        OperationsXml.save();
    }

    private void saveTrackDirections(Track track) {
        // save train directions serviced by this location
        int direction = 0;
        if (northCheckBox.isSelected()) {
            direction += Track.NORTH;
        }
        if (southCheckBox.isSelected()) {
            direction += Track.SOUTH;
        }
        if (eastCheckBox.isSelected()) {
            direction += Track.EAST;
        }
        if (westCheckBox.isSelected()) {
            direction += Track.WEST;
        }
        track.setTrainDirections(direction);
    }

    private boolean checkUserInputs(Track track) {
        // check that track name is valid
        if (!checkName(Bundle.getMessage("save"))) {
            return false;
        }
        // check to see if track already exists
        Track check = _location.getTrackByName(trackNameTextField.getText(), null);
        if (check != null && check != track) {
            reportTrackExists(Bundle.getMessage("save"));
            return false;
        }
        // check track length
        if (!checkLength(track)) {
            return false;
        }
        // check trains and route option
        if (!checkService(track)) {
            return false;
        }

        return true;
    }

    /**
     * @return true if name is less than 26 characters
     */
    private boolean checkName(String s) {
        String trackName = trackNameTextField.getText().trim();
        if (trackName.isEmpty()) {
            log.debug("Must enter a track name");
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"),
                    Bundle.getMessage("CanNotTrack", s),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        String[] check = trackName.split(TrainCommon.HYPHEN);
        if (check.length == 0) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("HyphenFeature"),
                    Bundle.getMessage("CanNotTrack", s),
                    JmriJOptionPane.ERROR_MESSAGE);

            return false;
        }
        if (TrainCommon.splitString(trackName).length() > MAX_NAME_LENGTH) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("TrackNameLengthMax", Integer.toString(MAX_NAME_LENGTH + 1)),
                    Bundle.getMessage("CanNotTrack", s),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean checkLength(Track track) {
        // convert track length if in inches
        String length = trackLengthTextField.getText();
        if (length.endsWith("\"")) { // NOI18N
            length = length.substring(0, length.length() - 1);
            try {
                double inches = Double.parseDouble(length);
                int feet = (int) (inches * Setup.getScaleRatio() / 12);
                length = Integer.toString(feet);
            } catch (NumberFormatException e) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("CanNotConvertFeet"),
                        Bundle.getMessage("ErrorTrackLength"), JmriJOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        if (length.endsWith("cm")) { // NOI18N
            length = length.substring(0, length.length() - 2);
            try {
                double cm = Double.parseDouble(length);
                int meter = (int) (cm * Setup.getScaleRatio() / 100);
                length = Integer.toString(meter);
            } catch (NumberFormatException e) {
                // log.error("Can not convert from cm to meters");
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("CanNotConvertMeter"),
                        Bundle.getMessage("ErrorTrackLength"), JmriJOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        // confirm that length is a number and less than 10000 feet
        int trackLength = 0;
        try {
            trackLength = Integer.parseInt(length);
            if (length.length() > Control.max_len_string_track_length_name) {
                JmriJOptionPane.showMessageDialog(this,
                        Bundle.getMessage("TrackMustBeLessThan",
                                Math.pow(10, Control.max_len_string_track_length_name),
                                Setup.getLengthUnit().toLowerCase()),
                        Bundle.getMessage("ErrorTrackLength"), JmriJOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            // log.error("Track length not an integer");
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("TrackMustBeNumber"),
                    Bundle.getMessage("ErrorTrackLength"), JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        // track length can not be less than than the sum of used and reserved
        // length
        if (trackLength != track.getLength() && trackLength < track.getUsedLength() + track.getReserved()) {
            // log.warn("Track length should not be less than used and
            // reserved");
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("TrackMustBeGreater"),
                    Bundle.getMessage("ErrorTrackLength"), JmriJOptionPane.ERROR_MESSAGE);
            // does the user want to force the track length?
            if (JmriJOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("TrackForceLength", track.getLength(), trackLength,
                            Setup.getLengthUnit().toLowerCase()),
                    Bundle.getMessage("ErrorTrackLength"),
                    JmriJOptionPane.YES_NO_OPTION) != JmriJOptionPane.YES_OPTION) {
                return false;
            }
        }
        // if everything is okay, save length
        track.setLength(trackLength);
        return true;
    }

    private boolean checkService(Track track) {
        // check train and route restrictions
        if ((trainDrop.isSelected() || routeDrop.isSelected()) && track.getDropIds().length == 0) {
            log.debug("Must enter trains or routes for this track");
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("UseAddTrainsOrRoutes"),
                    Bundle.getMessage("SetOutDisabled"), JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        if ((trainPickup.isSelected() || routePickup.isSelected()) && track.getPickupIds().length == 0) {
            log.debug("Must enter trains or routes for this track");
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("UseAddTrainsOrRoutes"),
                    Bundle.getMessage("PickUpsDisabled"), JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean checkTrackPickups(Track track) {
        // check to see if all car types can be pulled from this track
        String status = track.checkPickups();
        if (!status.equals(Track.PICKUP_OKAY) && !track.getPickupOption().equals(Track.ANY)) {
            JmriJOptionPane.showMessageDialog(this, status, Bundle.getMessage("ErrorStrandedCar"),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void reportTrackExists(String s) {
        JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("TrackAlreadyExists"),
                Bundle.getMessage("CanNotTrack", s), JmriJOptionPane.ERROR_MESSAGE);
    }

    protected void enableButtons(boolean enabled) {
        _toolMenu.setEnabled(enabled);
        northCheckBox.setEnabled(enabled);
        southCheckBox.setEnabled(enabled);
        eastCheckBox.setEnabled(enabled);
        westCheckBox.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        setButton.setEnabled(enabled);
        deleteTrackButton.setEnabled(enabled);
        saveTrackButton.setEnabled(enabled);
        roadOptionButton.setEnabled(enabled);
        loadOptionButton.setEnabled(enabled);
        shipLoadOptionButton.setEnabled(enabled);
        destinationOptionButton.setEnabled(enabled);
        anyDrops.setEnabled(enabled);
        trainDrop.setEnabled(enabled);
        routeDrop.setEnabled(enabled);
        excludeTrainDrop.setEnabled(enabled);
        excludeRouteDrop.setEnabled(enabled);
        anyPickups.setEnabled(enabled);
        trainPickup.setEnabled(enabled);
        routePickup.setEnabled(enabled);
        excludeTrainPickup.setEnabled(enabled);
        excludeRoutePickup.setEnabled(enabled);
        orderNormal.setEnabled(enabled);
        orderFIFO.setEnabled(enabled);
        orderLIFO.setEnabled(enabled);
        enableCheckboxes(enabled);
        if (readerSelector != null) {
            // enable readerSelect.
            readerSelector.setEnabled(enabled && Setup.isRfidEnabled());
        }
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        if (ae.getSource() == orderNormal) {
            _track.setServiceOrder(Track.NORMAL);
        }
        if (ae.getSource() == orderFIFO) {
            _track.setServiceOrder(Track.FIFO);
        }
        if (ae.getSource() == orderLIFO) {
            _track.setServiceOrder(Track.LIFO);
        }
        if (ae.getSource() == anyDrops) {
            _track.setDropOption(Track.ANY);
            updateDropOptions();
        }
        if (ae.getSource() == trainDrop) {
            _track.setDropOption(Track.TRAINS);
            updateDropOptions();
        }
        if (ae.getSource() == routeDrop) {
            _track.setDropOption(Track.ROUTES);
            updateDropOptions();
        }
        if (ae.getSource() == excludeTrainDrop) {
            _track.setDropOption(Track.EXCLUDE_TRAINS);
            updateDropOptions();
        }
        if (ae.getSource() == excludeRouteDrop) {
            _track.setDropOption(Track.EXCLUDE_ROUTES);
            updateDropOptions();
        }
        if (ae.getSource() == anyPickups) {
            _track.setPickupOption(Track.ANY);
            updatePickupOptions();
        }
        if (ae.getSource() == trainPickup) {
            _track.setPickupOption(Track.TRAINS);
            updatePickupOptions();
        }
        if (ae.getSource() == routePickup) {
            _track.setPickupOption(Track.ROUTES);
            updatePickupOptions();
        }
        if (ae.getSource() == excludeTrainPickup) {
            _track.setPickupOption(Track.EXCLUDE_TRAINS);
            updatePickupOptions();
        }
        if (ae.getSource() == excludeRoutePickup) {
            _track.setPickupOption(Track.EXCLUDE_ROUTES);
            updatePickupOptions();
        }
    }

    // TODO only update comboBox when train or route list changes.
    private void updateDropOptions() {
        dropPanel.removeAll();
        int numberOfItems = getNumberOfCheckboxesPerLine();

        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.add(anyDrops);
        p.add(trainDrop);
        p.add(routeDrop);
        p.add(excludeTrainDrop);
        p.add(excludeRouteDrop);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridwidth = numberOfItems + 1;
        dropPanel.add(p, gc);

        int y = 1; // vertical position in panel

        if (_track != null) {
            // set radio button
            anyDrops.setSelected(_track.getDropOption().equals(Track.ANY));
            trainDrop.setSelected(_track.getDropOption().equals(Track.TRAINS));
            routeDrop.setSelected(_track.getDropOption().equals(Track.ROUTES));
            excludeTrainDrop.setSelected(_track.getDropOption().equals(Track.EXCLUDE_TRAINS));
            excludeRouteDrop.setSelected(_track.getDropOption().equals(Track.EXCLUDE_ROUTES));

            if (!anyDrops.isSelected()) {
                p = new JPanel();
                p.setLayout(new FlowLayout());
                if (trainDrop.isSelected() || excludeTrainDrop.isSelected()) {
                    p.add(comboBoxDropTrains);
                } else {
                    p.add(comboBoxDropRoutes);
                }
                p.add(addDropButton);
                p.add(deleteDropButton);
                p.add(autoDropCheckBox);
                gc.gridy = y++;
                dropPanel.add(p, gc);
                y++;

                String[] dropIds = _track.getDropIds();
                int x = 0;
                for (String id : dropIds) {
                    JLabel names = new JLabel();
                    String name = "<deleted>"; // NOI18N
                    if (trainDrop.isSelected() || excludeTrainDrop.isSelected()) {
                        Train train = trainManager.getTrainById(id);
                        if (train != null) {
                            name = train.getName();
                        }
                    } else {
                        Route route = routeManager.getRouteById(id);
                        if (route != null) {
                            name = route.getName();
                        }
                    }
                    if (name.equals("<deleted>")) // NOI18N
                    {
                        _track.deleteDropId(id);
                    }
                    names.setText(name);
                    addItem(dropPanel, names, x++, y);
                    if (x > numberOfItems) {
                        y++;
                        x = 0;
                    }
                }
            }
        } else {
            anyDrops.setSelected(true);
        }
        dropPanel.revalidate();
        dropPanel.repaint();
        revalidate();
    }

    private void updatePickupOptions() {
        log.debug("update pick up options");
        pickupPanel.removeAll();
        int numberOfCheckboxes = getNumberOfCheckboxesPerLine();

        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.add(anyPickups);
        p.add(trainPickup);
        p.add(routePickup);
        p.add(excludeTrainPickup);
        p.add(excludeRoutePickup);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridwidth = numberOfCheckboxes + 1;
        pickupPanel.add(p, gc);

        int y = 1; // vertical position in panel

        if (_track != null) {
            // set radio button
            anyPickups.setSelected(_track.getPickupOption().equals(Track.ANY));
            trainPickup.setSelected(_track.getPickupOption().equals(Track.TRAINS));
            routePickup.setSelected(_track.getPickupOption().equals(Track.ROUTES));
            excludeTrainPickup.setSelected(_track.getPickupOption().equals(Track.EXCLUDE_TRAINS));
            excludeRoutePickup.setSelected(_track.getPickupOption().equals(Track.EXCLUDE_ROUTES));

            if (!anyPickups.isSelected()) {
                p = new JPanel();
                p.setLayout(new FlowLayout());
                if (trainPickup.isSelected() || excludeTrainPickup.isSelected()) {
                    p.add(comboBoxPickupTrains);
                } else {
                    p.add(comboBoxPickupRoutes);
                }
                p.add(addPickupButton);
                p.add(deletePickupButton);
                p.add(autoPickupCheckBox);
                gc.gridy = y++;
                pickupPanel.add(p, gc);
                y++;

                int x = 0;
                for (String id : _track.getPickupIds()) {
                    JLabel names = new JLabel();
                    String name = "<deleted>"; // NOI18N
                    if (trainPickup.isSelected() || excludeTrainPickup.isSelected()) {
                        Train train = trainManager.getTrainById(id);
                        if (train != null) {
                            name = train.getName();
                        }
                    } else {
                        Route route = routeManager.getRouteById(id);
                        if (route != null) {
                            name = route.getName();
                        }
                    }
                    if (name.equals("<deleted>")) // NOI18N
                    {
                        _track.deletePickupId(id);
                    }
                    names.setText(name);
                    addItem(pickupPanel, names, x++, y);
                    if (x > numberOfCheckboxes) {
                        y++;
                        x = 0;
                    }
                }
            }
        } else {
            anyPickups.setSelected(true);
        }
        pickupPanel.revalidate();
        pickupPanel.repaint();
        revalidate();
    }

    protected void updateTrainComboBox() {
        trainManager.updateTrainComboBox(comboBoxPickupTrains);
        if (autoPickupCheckBox.isSelected()) {
            autoTrainComboBox(comboBoxPickupTrains);
        }
        trainManager.updateTrainComboBox(comboBoxDropTrains);
        if (autoDropCheckBox.isSelected()) {
            autoTrainComboBox(comboBoxDropTrains);
        }
    }

    // filter all trains not serviced by this track
    private void autoTrainComboBox(JComboBox<Train> box) {
        for (int i = 0; i < box.getItemCount(); i++) {
            Train train = box.getItemAt(i);
            if (train == null || !checkRoute(train.getRoute())) {
                box.removeItemAt(i--);
            }
        }
    }

    protected void updateRouteComboBox() {
        routeManager.updateComboBox(comboBoxPickupRoutes);
        if (autoPickupCheckBox.isSelected()) {
            autoRouteComboBox(comboBoxPickupRoutes);
        }
        routeManager.updateComboBox(comboBoxDropRoutes);
        if (autoDropCheckBox.isSelected()) {
            autoRouteComboBox(comboBoxDropRoutes);
        }
    }

    // filter out all routes not serviced by this track
    private void autoRouteComboBox(JComboBox<Route> box) {
        for (int i = 0; i < box.getItemCount(); i++) {
            Route route = box.getItemAt(i);
            if (!checkRoute(route)) {
                box.removeItemAt(i--);
            }
        }
    }

    private void enableCheckboxes(boolean enable) {
        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setEnabled(enable);
        }
    }

    private void selectCheckboxes(boolean enable) {
        for (int i = 0; i < checkBoxes.size(); i++) {
            JCheckBox checkBox = checkBoxes.get(i);
            checkBox.setSelected(enable);
            if (_track != null) {
                if (enable) {
                    _track.addTypeName(checkBox.getText());
                } else {
                    _track.deleteTypeName(checkBox.getText());
                }
            }
        }
    }
    
    // car and loco types
    private void updateCheckboxes() {
        // log.debug("Update all checkboxes");
        checkBoxes.clear();
        panelCheckBoxes.removeAll();
        numberOfCheckBoxes = getNumberOfCheckboxesPerLine();
        x = 0;
        y = 0; // vertical position in panel
        loadTypes(InstanceManager.getDefault(CarTypes.class).getNames());

        // add space between car and loco types
        checkNewLine();

        loadTypes(InstanceManager.getDefault(EngineTypes.class).getNames());
        enableCheckboxes(_track != null);

        JPanel p = new JPanel();
        p.add(clearButton);
        p.add(setButton);
        if (_track != null && _track.isSpur()) {
            p.add(autoSelectButton);
        }
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridwidth = getNumberOfCheckboxesPerLine() + 1;
        gc.gridy = ++y;
        panelCheckBoxes.add(p, gc);

        panelCheckBoxes.revalidate();
        panelCheckBoxes.repaint();
    }

    int x = 0;
    int y = 0; // vertical position in panel

    private void loadTypes(String[] types) {
        for (String type : types) {
            if (_location.acceptsTypeName(type)) {
                JCheckBox checkBox = new JCheckBox();
                checkBoxes.add(checkBox);
                checkBox.setText(type);
                addCheckBoxAction(checkBox);
                addItemLeft(panelCheckBoxes, checkBox, x, y);
                if (_track != null && _track.isTypeNameAccepted(type)) {
                    checkBox.setSelected(true);
                }
                checkNewLine();
            }
        }
    }

    int numberOfCheckBoxes;

    private void checkNewLine() {
        if (++x > numberOfCheckBoxes) {
            y++;
            x = 0;
        }
    }

    private void updateRoadOption() {
        if (_track != null) {
            roadOptionButton.setText(_track.getRoadOptionString());
        }
    }

    private void updateLoadOption() {
        if (_track != null) {
            loadOptionButton.setText(_track.getLoadOptionString());
            shipLoadOptionButton.setText(_track.getShipLoadOptionString());
        }
    }

    private void updateTrainDir() {
        northCheckBox.setVisible(((Setup.getTrainDirection() & Setup.NORTH) &
                (_location.getTrainDirections() & Location.NORTH)) == Location.NORTH);
        southCheckBox.setVisible(((Setup.getTrainDirection() & Setup.SOUTH) &
                (_location.getTrainDirections() & Location.SOUTH)) == Location.SOUTH);
        eastCheckBox.setVisible(((Setup.getTrainDirection() & Setup.EAST) &
                (_location.getTrainDirections() & Location.EAST)) == Location.EAST);
        westCheckBox.setVisible(((Setup.getTrainDirection() & Setup.WEST) &
                (_location.getTrainDirections() & Location.WEST)) == Location.WEST);

        if (_track != null) {
            northCheckBox.setSelected((_track.getTrainDirections() & Track.NORTH) == Track.NORTH);
            southCheckBox.setSelected((_track.getTrainDirections() & Track.SOUTH) == Track.SOUTH);
            eastCheckBox.setSelected((_track.getTrainDirections() & Track.EAST) == Track.EAST);
            westCheckBox.setSelected((_track.getTrainDirections() & Track.WEST) == Track.WEST);
        }
        panelTrainDir.revalidate();
        revalidate();
    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == autoDropCheckBox || ae.getSource() == autoPickupCheckBox) {
            updateTrainComboBox();
            updateRouteComboBox();
            return;
        }
        JCheckBox b = (JCheckBox) ae.getSource();
        log.debug("checkbox change {}", b.getText());
        if (b.isSelected()) {
            _track.addTypeName(b.getText());
        } else {
            _track.deleteTypeName(b.getText());
        }
    }

    // set the service order
    private void updateCarOrder() {
        if (_track != null) {
            orderNormal.setSelected(_track.getServiceOrder().equals(Track.NORMAL));
            orderFIFO.setSelected(_track.getServiceOrder().equals(Track.FIFO));
            orderLIFO.setSelected(_track.getServiceOrder().equals(Track.LIFO));
        }
    }

    protected void updateDestinationOption() {
        if (_track != null) {
            if (_track.getDestinationOption().equals(Track.INCLUDE_DESTINATIONS)) {
                pDestinationOption.setVisible(true);
                destinationOptionButton.setText(Bundle.getMessage("AcceptOnly") +
                        " " +
                        _track.getDestinationListSize() +
                        " " +
                        Bundle.getMessage("Destinations"));
            } else if (_track.getDestinationOption().equals(Track.EXCLUDE_DESTINATIONS)) {
                pDestinationOption.setVisible(true);
                destinationOptionButton.setText(Bundle.getMessage("Exclude") +
                        " " +
                        (InstanceManager.getDefault(LocationManager.class).getNumberOfLocations() -
                                _track.getDestinationListSize()) +
                        " " +
                        Bundle.getMessage("Destinations"));
            } else {
                destinationOptionButton.setText(Bundle.getMessage("AcceptAll"));
            }
        }
    }

    @Override
    public void dispose() {
        if (_track != null) {
            _track.removePropertyChangeListener(this);
        }
        if (_location != null) {
            _location.removePropertyChangeListener(this);
        }
        InstanceManager.getDefault(CarRoads.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarLoads.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        trainManager.removePropertyChangeListener(this);
        routeManager.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
        }
        if (e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.TYPES_CHANGED_PROPERTY)) {
            updateCheckboxes();
        }
        if (e.getPropertyName().equals(Location.TRAIN_DIRECTION_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.TRAIN_DIRECTION_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Setup.TRAIN_DIRECTION_PROPERTY_CHANGE)) {
            updateTrainDir();
        }
        if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateTrainComboBox();
            updateDropOptions();
            updatePickupOptions();
        }
        if (e.getPropertyName().equals(RouteManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateRouteComboBox();
            updateDropOptions();
            updatePickupOptions();
        }
        if (e.getPropertyName().equals(Track.ROADS_CHANGED_PROPERTY)) {
            updateRoadOption();
        }
        if (e.getPropertyName().equals(Track.LOADS_CHANGED_PROPERTY)) {
            updateLoadOption();
        }
        if (e.getPropertyName().equals(Track.DROP_CHANGED_PROPERTY)) {
            updateDropOptions();
        }
        if (e.getPropertyName().equals(Track.PICKUP_CHANGED_PROPERTY)) {
            updatePickupOptions();
        }
        if (e.getPropertyName().equals(Track.SERVICE_ORDER_CHANGED_PROPERTY)) {
            updateCarOrder();
        }
        if (e.getPropertyName().equals(Track.DESTINATIONS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Track.DESTINATION_OPTIONS_CHANGED_PROPERTY)) {
            updateDestinationOption();
        }
        if (e.getPropertyName().equals(Track.LENGTH_CHANGED_PROPERTY)) {
            trackLengthTextField.setText(Integer.toString(_track.getLength()));
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackEditFrame.class);
}
