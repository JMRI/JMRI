package jmri.jmrit.operations;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarSetFrame;
import jmri.jmrit.operations.rollingstock.cars.CarsTableFrame;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineSetFrame;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManifestText;
import jmri.jmrit.operations.trains.TrainSwitchListText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common elements for the Conductor and Yardmaster Frames.
 *
 * @author Dan Boudreau Copyright (C) 2013
 *
 */
public abstract class CommonConductorYardmasterPanel extends OperationsPanel implements PropertyChangeListener {

    protected static final String Tab = "    "; // used to space out headers
    protected static final String Space = " "; // used to pad out panels

    protected Location _location = null;
    protected Train _train = null;

    protected TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
    protected EngineManager engManager = InstanceManager.getDefault(EngineManager.class);
    protected CarManager carManager = InstanceManager.getDefault(CarManager.class);
    protected TrainCommon trainCommon = new TrainCommon();

    protected JScrollPane locoPane;
    protected JScrollPane pickupPane;
    protected JScrollPane setoutPane;
    protected JScrollPane movePane;

    // labels
    protected JLabel textRailRoadName = new JLabel();
    protected JLabel textTrainDescription = new JLabel();
    protected JLabel textLocationName = new JLabel();
    protected JLabel textStatus = new JLabel();

    // major buttons
    public JButton selectButton = new JButton(Bundle.getMessage("SelectAll"));
    public JButton clearButton = new JButton(Bundle.getMessage("ClearAll"));
    public JButton modifyButton = new JButton(Bundle.getMessage("Modify")); // see setModifyButtonText()
    protected JButton moveButton = new JButton(Bundle.getMessage("Move"));

    // text panes
    protected JTextPane textLocationCommentPane = new JTextPane();
    protected JTextPane textTrainCommentPane = new JTextPane();
    protected JTextPane textTrainRouteCommentPane = new JTextPane();
    protected JTextPane textTrainRouteLocationCommentPane = new JTextPane();

    // panels
    protected JPanel pRailRoadName = new JPanel();

    protected JPanel pTrainDescription = new JPanel();

    protected JPanel pLocationName = new JPanel();

    protected JPanel pLocos = new JPanel();
    protected JPanel pPickupLocos = new JPanel();
    protected JPanel pSetoutLocos = new JPanel();

    protected JPanel pPickups = new JPanel();
    protected JPanel pSetouts = new JPanel();
    protected JPanel pWorkPanes = new JPanel(); // place car pick ups and set outs side by side using two columns
    protected JPanel pMoves = new JPanel();

    protected JPanel pStatus = new JPanel();
    protected JPanel pButtons = new JPanel();

    // check boxes
    protected Hashtable<String, JCheckBox> checkBoxes = new Hashtable<>();
    protected List<RollingStock> rollingStock = new ArrayList<>();

    // flags
    protected boolean isSetMode = false; // when true, cars that aren't selected (checkbox) can be "set"

    public CommonConductorYardmasterPanel() {
        super();
    }

    public void initComponents() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        locoPane = new JScrollPane(pLocos);
        locoPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Engines")));

        pickupPane = new JScrollPane(pPickups);
        pickupPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Pickup")));

        setoutPane = new JScrollPane(pSetouts);
        setoutPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SetOut")));

        movePane = new JScrollPane(pMoves);
        movePane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocalMoves")));

        // Set up the panels
        pPickupLocos.setLayout(new BoxLayout(pPickupLocos, BoxLayout.Y_AXIS));
        pSetoutLocos.setLayout(new BoxLayout(pSetoutLocos, BoxLayout.Y_AXIS));
        pPickups.setLayout(new BoxLayout(pPickups, BoxLayout.Y_AXIS));
        pSetouts.setLayout(new BoxLayout(pSetouts, BoxLayout.Y_AXIS));
        pMoves.setLayout(new BoxLayout(pMoves, BoxLayout.Y_AXIS));

        // railroad name
        pRailRoadName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RailroadName")));
        pRailRoadName.add(textRailRoadName);

        // location name
        pLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
        pLocationName.add(textLocationName);

        // location comment
        textLocationCommentPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocationComment")));
        textLocationCommentPane.setBackground(null);
        textLocationCommentPane.setEditable(false);
        textLocationCommentPane.setMaximumSize(new Dimension(2000, 200));

        // train description
        pTrainDescription.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Description")));
        pTrainDescription.add(textTrainDescription);

        // train comment
        textTrainCommentPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainComment")));
        textTrainCommentPane.setBackground(null);
        textTrainCommentPane.setEditable(false);
        textTrainCommentPane.setMaximumSize(new Dimension(2000, 200));

        // train route comment
        textTrainRouteCommentPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RouteComment")));
        textTrainRouteCommentPane.setBackground(null);
        textTrainRouteCommentPane.setEditable(false);
        textTrainRouteCommentPane.setMaximumSize(new Dimension(2000, 200));

        // train route location comment
        textTrainRouteLocationCommentPane.setBorder(BorderFactory.createTitledBorder(Bundle
                .getMessage("RouteLocationComment")));
        textTrainRouteLocationCommentPane.setBackground(null);
        textTrainRouteLocationCommentPane.setEditable(false);
        textTrainRouteLocationCommentPane.setMaximumSize(new Dimension(2000, 200));

        // row 12
        if ((getPreferredSize().width > Control.panelWidth1025 && Setup.isTabEnabled()) ||
                (getPreferredSize().width > Control.panelWidth1025 - 200 && !Setup.isTabEnabled())) {
            pLocos.setLayout(new BoxLayout(pLocos, BoxLayout.X_AXIS));
            pWorkPanes.setLayout(new BoxLayout(pWorkPanes, BoxLayout.X_AXIS));
        } else {
            pLocos.setLayout(new BoxLayout(pLocos, BoxLayout.Y_AXIS));
            pWorkPanes.setLayout(new BoxLayout(pWorkPanes, BoxLayout.Y_AXIS));
        }

        pLocos.add(pPickupLocos);
        pLocos.add(pSetoutLocos);
        pWorkPanes.add(pickupPane);
        pWorkPanes.add(setoutPane);

        // row 13
        pStatus.setLayout(new GridBagLayout());
        pStatus.setBorder(BorderFactory.createTitledBorder(""));
        addItem(pStatus, textStatus, 0, 0);

        // row 14
        pButtons.setLayout(new GridBagLayout());
        pButtons.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Work")));
        addItem(pButtons, selectButton, 0, 0);
        addItem(pButtons, clearButton, 1, 0);
        addItem(pButtons, modifyButton, 2, 0);

        // setup buttons
        addButtonAction(selectButton);
        addButtonAction(clearButton);
        addButtonAction(modifyButton);

        setMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
    }

    // Select, Clear, and Set Buttons
    @Override
    public void buttonActionPerformed(ActionEvent ae) {
        if (ae.getSource() == selectButton) {
            selectCheckboxes(true);
        }
        if (ae.getSource() == clearButton) {
            selectCheckboxes(false);
        }
        if (ae.getSource() == modifyButton) {
            isSetMode = !isSetMode; // toggle setMode
            update();
            // ask if user wants to add cars to train
            if (isSetMode) {
                addCarToTrain();
            }
        }
        check();
    }

    protected void initialize() {
        removePropertyChangeListerners();
        pPickupLocos.removeAll();
        pSetoutLocos.removeAll();
        pPickups.removeAll();
        pSetouts.removeAll();
        pMoves.removeAll();

        // turn everything off and re-enable if needed
        pWorkPanes.setVisible(false);
        pickupPane.setVisible(false);
        setoutPane.setVisible(false);
        locoPane.setVisible(false);
        pPickupLocos.setVisible(false);
        pSetoutLocos.setVisible(false);
        movePane.setVisible(false);

        textTrainRouteLocationCommentPane.setVisible(false);

        setModifyButtonText();
    }

    protected void updateComplete() {
        pPickupLocos.repaint();
        pSetoutLocos.repaint();
        pPickups.repaint();
        pSetouts.repaint();
        pMoves.repaint();

        pPickupLocos.revalidate();
        pSetoutLocos.revalidate();
        pPickups.revalidate();
        pSetouts.revalidate();
        pMoves.revalidate();

        selectButton.setEnabled(checkBoxes.size() > 0 && !isSetMode);
        clearButton.setEnabled(checkBoxes.size() > 0 && !isSetMode);
        check();

        log.debug("update complete");
    }

    private void addCarToTrain() {
        if (JOptionPane.showConfirmDialog(this,
                MessageFormat.format(Bundle.getMessage("WantAddCarsToTrain?"), new Object[]{_train.getName()}),
                Bundle.getMessage("AddCarsToTrain?"),
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new CarsTableFrame(false, textLocationName.getText(), null);
        }
    }

    CarSetFrame csf = null;

    // action for set button for a car, opens the set car window
    public void carSetButtonActionPerfomed(ActionEvent ae) {
        String name = ((JButton) ae.getSource()).getName();
        log.debug("Set button for car {}", name);
        Car car = carManager.getById(name);
        if (csf != null) {
            csf.dispose();
        }
        csf = new CarSetFrame();
        csf.initComponents();
        csf.loadCar(car);
    }

    EngineSetFrame esf = null;

    // action for set button for an engine, opens the set engine window
    public void engineSetButtonActionPerfomed(ActionEvent ae) {
        String name = ((JButton) ae.getSource()).getName();
        log.debug("Set button for loco {}", name);
        Engine eng = engManager.getById(name);
        if (esf != null) {
            esf.dispose();
        }
        esf = new EngineSetFrame();
        esf.initComponents();
        esf.loadEngine(eng);
    }

    // confirm that all work is done
    @Override
    protected void checkBoxActionPerformed(ActionEvent ae) {
        check();
    }

    // Determines if all car checkboxes are selected. Disables the Set button if
    // all checkbox are selected.
    protected void check() {
        Enumeration<JCheckBox> en = checkBoxes.elements();
        while (en.hasMoreElements()) {
            JCheckBox checkBox = en.nextElement();
            if (!checkBox.isSelected()) {
                // log.debug("Checkbox (" + checkBox.getText() + ") isn't selected ");
                moveButton.setEnabled(false);
                modifyButton.setEnabled(true);
                return;
            }
        }
        // all selected, work done!
        moveButton.setEnabled(_train != null && _train.isBuilt());
        modifyButton.setEnabled(false);
        isSetMode = false;
        setModifyButtonText();
    }

    protected void selectCheckboxes(boolean enable) {
        Enumeration<JCheckBox> en = checkBoxes.elements();
        while (en.hasMoreElements()) {
            JCheckBox checkBox = en.nextElement();
            checkBox.setSelected(enable);
        }
        isSetMode = false;
    }

    /**
     * Uses "ep" prefix to denote a checkbox with an engine pick up, and "es" for an
     * engine set out.
     *
     * @param rl The routeLocation to show loco pick ups or set outs.
     */
    protected void updateLocoPanes(RouteLocation rl) {
        if (Setup.isPrintHeadersEnabled()) {
            JLabel header = new JLabel(Tab + trainCommon.getPickupEngineHeader());
            setLabelFont(header);
            pPickupLocos.add(header);
            JLabel headerDrop = new JLabel(Tab + trainCommon.getDropEngineHeader());
            setLabelFont(headerDrop);
            pSetoutLocos.add(headerDrop);
        }
        // check for locos
        List<Engine> engList = engManager.getByTrainBlockingList(_train);
        for (Engine engine : engList) {
            if (engine.getRouteLocation() == rl && engine.getTrack() != null) {
                locoPane.setVisible(true);
                pPickupLocos.setVisible(true);
                rollingStock.add(engine);
                engine.addPropertyChangeListener(this);
                JCheckBox checkBox;
                if (checkBoxes.containsKey("ep" + engine.getId())) {
                    checkBox = checkBoxes.get("ep" + engine.getId());
                } else {
                    checkBox = new JCheckBox(trainCommon.pickupEngine(engine));
                    setCheckBoxFont(checkBox);
                    addCheckBoxAction(checkBox);
                    checkBoxes.put("ep" + engine.getId(), checkBox);
                }
                if (isSetMode && !checkBox.isSelected()) {
                    pPickupLocos.add(addSet(engine));
                } else {
                    pPickupLocos.add(checkBox);
                }
            }
            if (engine.getRouteDestination() == rl) {
                locoPane.setVisible(true);
                pSetoutLocos.setVisible(true);
                rollingStock.add(engine);
                engine.addPropertyChangeListener(this);
                JCheckBox checkBox;
                if (checkBoxes.containsKey("es" + engine.getId())) {
                    checkBox = checkBoxes.get("es" + engine.getId());
                } else {
                    checkBox = new JCheckBox(trainCommon.dropEngine(engine));
                    setCheckBoxFont(checkBox);
                    addCheckBoxAction(checkBox);
                    checkBoxes.put("es" + engine.getId(), checkBox);
                }
                if (isSetMode && !checkBox.isSelected()) {
                    pSetoutLocos.add(addSet(engine));
                } else {
                    pSetoutLocos.add(checkBox);
                }
            }
        }
        // pad the panels in case the horizontal scroll bar appears
        pPickupLocos.add(new JLabel(Space));
        pSetoutLocos.add(new JLabel(Space));
    }

    /**
     * Block cars by track (optional), then pick up and set out for each location in
     * a train's route. This shows each car with a check box or with a set button.
     * The set button is displayed when the checkbox isn't selected and the display
     * is in "set" mode. If the car is a utility. Show the number of cars that have
     * the same attributes, and not the car's road and number. Each car is displayed
     * only once in one of three panes. The three panes are pick up, set out, or
     * local move. To keep track of each car and which pane to use, they are placed
     * in the list "rollingStock" with the prefix "p", "s" or "m" and the car's
     * unique id.
     *
     * @param rl The RouteLocation
     * @param isManifest True if manifest, false if switch list
     *
     */
    protected void blockCars(RouteLocation rl, boolean isManifest) {
        if (Setup.isPrintHeadersEnabled()) {
            JLabel header =
                    new JLabel(Tab + trainCommon.getPickupCarHeader(isManifest, !TrainCommon.IS_TWO_COLUMN_TRACK));
            setLabelFont(header);
            pPickups.add(header);
            header = new JLabel(Tab + trainCommon.getDropCarHeader(isManifest, !TrainCommon.IS_TWO_COLUMN_TRACK));
            setLabelFont(header);
            pSetouts.add(header);
            header = new JLabel(Tab + trainCommon.getLocalMoveHeader(isManifest));
            setLabelFont(header);
            pMoves.add(header);
        }
        List<Track> tracks = rl.getLocation().getTrackByNameList(null);
        List<RouteLocation> routeList = _train.getRoute().getLocationsBySequenceList();
        List<Car> carList = carManager.getByTrainDestinationList(_train);
        for (Track track : tracks) {
            for (RouteLocation rld : routeList) {
                for (Car car : carList) {
                    // determine if car is a pick up from the right track
                    // caboose or FRED is placed at end of the train
                    // passenger trains are already blocked in the car list
                    if (car.getTrack() != null &&
                            car.getRouteLocation() == rl &&
                            car.getRouteDestination() != rl &&
                            (!Setup.isSortByTrackNameEnabled() || car.getTrackName().equals(track.getName())) &&
                            ((car.getRouteDestination() == rld && !car.isCaboose() && !car.hasFred()) ||
                                    (rld == routeList.get(routeList.size() - 1) &&
                                            (car.isCaboose() || car.hasFred())) ||
                                    car.isPassenger())) {
                        // yes we have a pick up
                        pWorkPanes.setVisible(true);
                        pickupPane.setVisible(true);
                        if (!rollingStock.contains(car)) {
                            rollingStock.add(car);
                            car.addPropertyChangeListener(this);
                        }
                        // did we already process this car?
                        if (checkBoxes.containsKey("p" + car.getId())) {
                            if (isSetMode && !checkBoxes.get("p" + car.getId()).isSelected()) {
                                // change to set button so user can remove car from train
                                pPickups.add(addSet(car));
                            } else {
                                pPickups.add(checkBoxes.get("p" + car.getId()));
                            }
                            // figure out the checkbox text, either single car or utility
                        } else {
                            String text;
                            if (car.isUtility()) {
                                text = trainCommon.pickupUtilityCars(carList, car, isManifest,
                                        !TrainCommon.IS_TWO_COLUMN_TRACK);
                                if (text == null) {
                                    continue; // this car type has already been processed
                                }
                            } else {
                                text = trainCommon.pickupCar(car, isManifest, !TrainCommon.IS_TWO_COLUMN_TRACK);
                            }
                            JCheckBox checkBox = new JCheckBox(text);
                            setCheckBoxFont(checkBox);
                            addCheckBoxAction(checkBox);
                            pPickups.add(checkBox);
                            checkBoxes.put("p" + car.getId(), checkBox);
                        }
                    }
                }
            }
            // set outs and local moves
            for (Car car : carList) {
                if (car.getRouteDestination() != rl || car.getDestinationTrack() == null) {
                    continue;
                }
                // car in train if track null, second check is for yard master window
                if (car.getTrack() == null || car.getTrack() != null && (car.getRouteLocation() != rl)) {
                    if (Setup.isSortByTrackNameEnabled() &&
                            !car.getDestinationTrack().getName().equals(track.getName())) {
                        continue;
                    }
                    // we have set outs
                    pWorkPanes.setVisible(true);
                    setoutPane.setVisible(true);
                    if (!rollingStock.contains(car)) {
                        rollingStock.add(car);
                        car.addPropertyChangeListener(this);
                    }
                    if (checkBoxes.containsKey("s" + car.getId())) {
                        if (isSetMode && !checkBoxes.get("s" + car.getId()).isSelected()) {
                            // change to set button so user can remove car from train
                            pSetouts.add(addSet(car));
                        } else {
                            pSetouts.add(checkBoxes.get("s" + car.getId()));
                        }
                    } else {
                        String text;
                        if (car.isUtility()) {
                            text = trainCommon.setoutUtilityCars(carList, car, !TrainCommon.LOCAL, isManifest);
                            if (text == null) {
                                continue; // this car type has already been processed
                            }
                        } else {
                            text = trainCommon.dropCar(car, isManifest, !TrainCommon.IS_TWO_COLUMN_TRACK);
                        }
                        JCheckBox checkBox = new JCheckBox(text);
                        setCheckBoxFont(checkBox);
                        addCheckBoxAction(checkBox);
                        pSetouts.add(checkBox);
                        checkBoxes.put("s" + car.getId(), checkBox);
                    }
                    // local move?
                } else if (car.getTrack() != null &&
                        car.getRouteLocation() == rl &&
                        (!Setup.isSortByTrackNameEnabled() || car.getTrack().getName().equals(track.getName()))) {
                    movePane.setVisible(true);
                    if (!rollingStock.contains(car)) {
                        rollingStock.add(car);
                        car.addPropertyChangeListener(this);
                    }
                    if (checkBoxes.containsKey("m" + car.getId())) {
                        if (isSetMode && !checkBoxes.get("m" + car.getId()).isSelected()) {
                            // change to set button so user can remove car from train
                            pMoves.add(addSet(car));
                        } else {
                            pMoves.add(checkBoxes.get("m" + car.getId()));
                        }
                    } else {
                        String text;
                        if (car.isUtility()) {
                            text = trainCommon.setoutUtilityCars(carList, car, TrainCommon.LOCAL, isManifest);
                            if (text == null) {
                                continue; // this car type has already been processed
                            }
                        } else {
                            text = trainCommon.localMoveCar(car, isManifest);
                        }
                        JCheckBox checkBox = new JCheckBox(text);
                        setCheckBoxFont(checkBox);
                        addCheckBoxAction(checkBox);
                        pMoves.add(checkBox);
                        checkBoxes.put("m" + car.getId(), checkBox);
                    }
                }
            }
            // if not sorting by track, we're done
            if (!Setup.isSortByTrackNameEnabled()) {
                break;
            }
        }
        // pad the panels in case the horizontal scroll bar appears
        pPickups.add(new JLabel(Space));
        pSetouts.add(new JLabel(Space));
        pMoves.add(new JLabel(Space));
    }

    // replace the car or engine checkbox and text with only the road and number and a Set button
    protected JPanel addSet(RollingStock rs) {
        JPanel pSet = new JPanel();
        pSet.setLayout(new GridBagLayout());
        JButton setButton = new JButton(Bundle.getMessage("Set"));
        setButton.setToolTipText(Bundle.getMessage("SetButtonToolTip"));
        setButton.setName(rs.getId());
        setButton.addActionListener((ActionEvent e) -> {
            if (Car.class.isInstance(rs)) {
                carSetButtonActionPerfomed(e);
            } else {
                engineSetButtonActionPerfomed(e);
            }
        });
        JLabel label = new JLabel(TrainCommon.padString(rs.toString(),
                Control.max_len_string_attibute + Control.max_len_string_road_number));
        setLabelFont(label);
        addItem(pSet, label, 0, 0);
        addItemLeft(pSet, setButton, 1, 0);
        pSet.setAlignmentX(LEFT_ALIGNMENT);
        return pSet;
    }

    protected void setCheckBoxFont(JCheckBox checkBox) {
        if (Setup.isTabEnabled()) {
            Font font = new Font(Setup.getFontName(), Font.PLAIN, checkBox.getFont().getSize());
            checkBox.setFont(font);
        }
    }

    protected void setLabelFont(JLabel label) {
        if (Setup.isTabEnabled()) {
            Font font = new Font(Setup.getFontName(), Font.PLAIN, label.getFont().getSize());
            label.setFont(font);
        }
    }

    protected void setModifyButtonText() {
        if (isSetMode) {
            modifyButton.setText(Bundle.getMessage("Done"));
        } else {
            modifyButton.setText(Bundle.getMessage("Modify"));
        }
    }

    // returns one of two possible departure strings for a train
    protected String getStatus(RouteLocation rl, boolean isManifest) {
        if (rl == _train.getRoute().getTerminatesRouteLocation()) {
            return MessageFormat.format(TrainManifestText.getStringTrainTerminates(), new Object[]{_train
                    .getTrainTerminatesName()});
        }
        if (rl != _train.getCurrentLocation() && _train.getExpectedArrivalTime(rl).equals(Train.ALREADY_SERVICED)) {
            return MessageFormat.format(TrainSwitchListText.getStringTrainDone(), new Object[]{_train
                    .getName()});
        }
        if (!_train.isBuilt()) {
            return _train.getStatus();
        }
        if (Setup.isPrintLoadsAndEmptiesEnabled()) {
            int emptyCars = _train.getNumberEmptyCarsInTrain(rl);
            String text;
            if (isManifest) {
                text = TrainManifestText.getStringTrainDepartsLoads();
            } else {
                text = TrainSwitchListText.getStringTrainDepartsLoads();
            }
            return MessageFormat.format(text, new Object[]{TrainCommon.splitString(rl.getName()),
                    rl.getTrainDirectionString(), _train.getNumberCarsInTrain(rl) - emptyCars, emptyCars,
                    _train.getTrainLength(rl), Setup.getLengthUnit().toLowerCase(), _train.getTrainWeight(rl),
                    _train.getTrainTerminatesName(), _train.getName()});
        } else {
            String text;
            if (isManifest) {
                text = TrainManifestText.getStringTrainDepartsCars();
            } else {
                text = TrainSwitchListText.getStringTrainDepartsCars();
            }
            return MessageFormat.format(text, new Object[]{TrainCommon.splitString(rl.getName()),
                    rl.getTrainDirectionString(), _train.getNumberCarsInTrain(rl), _train.getTrainLength(rl),
                    Setup.getLengthUnit().toLowerCase(), _train.getTrainWeight(rl), _train.getTrainTerminatesName(),
                    _train.getName()});
        }
    }

    protected void clearAndUpdate() {
        trainCommon.clearUtilityCarTypes(); // reset the utility car counts
        checkBoxes.clear();
        isSetMode = false;
        update();
    }

    // to be overridden
    protected abstract void update();

    protected void removePropertyChangeListerners() {
        rollingStock.stream().forEach((rs) -> {
            rs.removePropertyChangeListener(this);
        });
        rollingStock.clear();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        log.debug("Property change {} for: {} old: {} new: {}", e.getPropertyName(), e.getSource(),
                e.getOldValue(), e.getNewValue()); // NOI18N
    }

    private static final Logger log = LoggerFactory.getLogger(CommonConductorYardmasterPanel.class);
}
