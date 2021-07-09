package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.divisions.Division;
import jmri.jmrit.operations.locations.divisions.DivisionEditFrame;
import jmri.jmrit.operations.locations.divisions.DivisionManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.RollingStockManager;
import jmri.jmrit.operations.rollingstock.RollingStockSetFrame;
import jmri.jmrit.operations.rollingstock.cars.tools.CarAttributeEditFrame;
import jmri.jmrit.operations.rollingstock.cars.tools.CarLoadEditFrame;
import jmri.jmrit.operations.rollingstock.cars.tools.EnableDestinationAction;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.tools.TrainByCarTypeFrame;

/**
 * Frame for user to place car on the layout
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2013, 2014, 2021
 */
public class CarSetFrame extends RollingStockSetFrame<Car> {

    protected static final ResourceBundle rb = ResourceBundle
            .getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
    private static final String IGNORE = "Ignore";
    private static final String KERNEL = "Kernel";
    private static final String TIP_IGNORE = "TipIgnore";

    CarManager carManager = InstanceManager.getDefault(CarManager.class);
    CarLoads carLoads = InstanceManager.getDefault(CarLoads.class);

    Car _car;

    // combo boxes
    protected JComboBox<Division> divisionComboBox = InstanceManager.getDefault(DivisionManager.class).getComboBox();
    protected JComboBox<Location> destReturnWhenEmptyBox = InstanceManager.getDefault(LocationManager.class)
            .getComboBox();
    protected JComboBox<Track> trackReturnWhenEmptyBox = new JComboBox<>();
    protected JComboBox<String> loadReturnWhenEmptyBox = carLoads.getComboBox(null);
    protected JComboBox<Location> destReturnWhenLoadedBox = InstanceManager.getDefault(LocationManager.class)
            .getComboBox();
    protected JComboBox<Track> trackReturnWhenLoadedBox = new JComboBox<>();
    protected JComboBox<String> loadReturnWhenLoadedBox = carLoads.getComboBox(null);
    JComboBox<String> loadComboBox = carLoads.getComboBox(null);
    JComboBox<String> kernelComboBox = carManager.getKernelComboBox();

    // buttons
    JButton editDivisionButton = new JButton(Bundle.getMessage("ButtonEdit"));
    JButton editLoadButton = new JButton(Bundle.getMessage("ButtonEdit"));
    JButton editKernelButton = new JButton(Bundle.getMessage("ButtonEdit"));

    // check boxes
    protected JCheckBox ignoreDivisionCheckBox = new JCheckBox(Bundle.getMessage(IGNORE));
    protected JCheckBox ignoreRWECheckBox = new JCheckBox(Bundle.getMessage(IGNORE));
    protected JCheckBox autoReturnWhenEmptyTrackCheckBox = new JCheckBox(Bundle.getMessage("Auto"));
    protected JCheckBox ignoreRWLCheckBox = new JCheckBox(Bundle.getMessage(IGNORE));
    protected JCheckBox autoReturnWhenLoadedTrackCheckBox = new JCheckBox(Bundle.getMessage("Auto"));
    protected JCheckBox ignoreLoadCheckBox = new JCheckBox(Bundle.getMessage(IGNORE));
    protected JCheckBox ignoreKernelCheckBox = new JCheckBox(Bundle.getMessage(IGNORE));

    // Auto checkbox state
    private static boolean autoReturnWhenEmptyTrackCheckBoxSelected = false;
    private static boolean autoReturnWhenLoadedTrackCheckBoxSelected = false;

    // frames
    CarLoadEditFrame lef = null;
    DivisionEditFrame def = null;

    private static boolean enableDestination = false;

    public CarSetFrame() {
        super(Bundle.getMessage("TitleCarSet"));
    }

    @Override
    public void initComponents() {
        super.initComponents();

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new EnableDestinationAction(this));
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_CarsSet", true); // NOI18N

        // initial caps for some languages i.e. German
        editLoadButton.setToolTipText(
                MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"), Bundle.getMessage("load")));
        editKernelButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                Bundle.getMessage(KERNEL).toLowerCase()));

        // optional panel load, return when empty, return when loaded, division, and
        // kernel
        paneOptional.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOptional")));
        pOptional.setLayout(new BoxLayout(pOptional, BoxLayout.Y_AXIS));

        // add load fields
        JPanel pLoad = new JPanel();
        pLoad.setLayout(new GridBagLayout());
        pLoad.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Load")));
        addItemLeft(pLoad, ignoreLoadCheckBox, 1, 0);
        addItem(pLoad, loadComboBox, 2, 0);
        addItem(pLoad, editLoadButton, 3, 0);
        pOptional.add(pLoad);

        // row 5
        JPanel pReturnWhenEmpty = new JPanel();
        pReturnWhenEmpty.setLayout(new GridBagLayout());
        pReturnWhenEmpty.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutReturnWhenEmpty")));
        addItem(pReturnWhenEmpty, new JLabel(Bundle.getMessage("Location")), 1, 0);
        addItem(pReturnWhenEmpty, new JLabel(Bundle.getMessage("Track")), 2, 0);
        addItem(pReturnWhenEmpty, new JLabel(Bundle.getMessage("Load")), 3, 0);
        addItemLeft(pReturnWhenEmpty, ignoreRWECheckBox, 0, 1);
        addItem(pReturnWhenEmpty, destReturnWhenEmptyBox, 1, 1);
        addItem(pReturnWhenEmpty, trackReturnWhenEmptyBox, 2, 1);
        addItem(pReturnWhenEmpty, loadReturnWhenEmptyBox, 3, 1);
        addItem(pReturnWhenEmpty, autoReturnWhenEmptyTrackCheckBox, 4, 1);
        pOptional.add(pReturnWhenEmpty);

        // row 6
        JPanel pReturnWhenLoaded = new JPanel();
        pReturnWhenLoaded.setLayout(new GridBagLayout());
        pReturnWhenLoaded
                .setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutReturnWhenLoaded")));
        addItem(pReturnWhenLoaded, new JLabel(Bundle.getMessage("Location")), 1, 0);
        addItem(pReturnWhenLoaded, new JLabel(Bundle.getMessage("Track")), 2, 0);
        addItem(pReturnWhenLoaded, new JLabel(Bundle.getMessage("Load")), 3, 0);
        addItemLeft(pReturnWhenLoaded, ignoreRWLCheckBox, 0, 1);
        addItem(pReturnWhenLoaded, destReturnWhenLoadedBox, 1, 1);
        addItem(pReturnWhenLoaded, trackReturnWhenLoadedBox, 2, 1);
        addItem(pReturnWhenLoaded, loadReturnWhenLoadedBox, 3, 1);
        addItem(pReturnWhenLoaded, autoReturnWhenLoadedTrackCheckBox, 4, 1);
        pOptional.add(pReturnWhenLoaded);

        // division field
        JPanel pDivision = new JPanel();
        pDivision.setLayout(new GridBagLayout());
        pDivision.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("HomeDivision")));
        addItemLeft(pDivision, ignoreDivisionCheckBox, 1, 0);
        addItem(pDivision, divisionComboBox, 2, 0);
        addItem(pDivision, editDivisionButton, 3, 0);
        pOptional.add(pDivision);

        // add kernel fields
        JPanel pKernel = new JPanel();
        pKernel.setLayout(new GridBagLayout());
        pKernel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage(KERNEL)));
        addItemLeft(pKernel, ignoreKernelCheckBox, 1, 0);
        addItem(pKernel, kernelComboBox, 2, 0);
        addItem(pKernel, editKernelButton, 3, 0);
        pOptional.add(pKernel);

        // don't show ignore checkboxes
        ignoreDivisionCheckBox.setVisible(false);
        ignoreRWECheckBox.setVisible(false);
        ignoreRWLCheckBox.setVisible(false);
        ignoreLoadCheckBox.setVisible(false);
        ignoreKernelCheckBox.setVisible(false);

        autoReturnWhenEmptyTrackCheckBox.setSelected(autoReturnWhenEmptyTrackCheckBoxSelected);
        autoReturnWhenLoadedTrackCheckBox.setSelected(autoReturnWhenLoadedTrackCheckBoxSelected);

        // setup combobox
        addComboBoxAction(destReturnWhenEmptyBox);
        addComboBoxAction(destReturnWhenLoadedBox);
        addComboBoxAction(loadComboBox);
        addComboBoxAction(divisionComboBox);

        // setup button
        addButtonAction(editLoadButton);
        addButtonAction(editDivisionButton);
        addButtonAction(editKernelButton);

        // setup checkboxes
        addCheckBoxAction(ignoreRWECheckBox);
        addCheckBoxAction(ignoreRWLCheckBox);
        addCheckBoxAction(autoReturnWhenEmptyTrackCheckBox);
        addCheckBoxAction(autoReturnWhenLoadedTrackCheckBox);
        addCheckBoxAction(ignoreLoadCheckBox);
        addCheckBoxAction(ignoreDivisionCheckBox);
        addCheckBoxAction(ignoreKernelCheckBox);

        // tool tips
        ignoreRWECheckBox.setToolTipText(Bundle.getMessage(TIP_IGNORE));
        ignoreRWLCheckBox.setToolTipText(Bundle.getMessage(TIP_IGNORE));
        ignoreLoadCheckBox.setToolTipText(Bundle.getMessage(TIP_IGNORE));
        ignoreDivisionCheckBox.setToolTipText(Bundle.getMessage(TIP_IGNORE));
        ignoreKernelCheckBox.setToolTipText(Bundle.getMessage(TIP_IGNORE));
        outOfServiceCheckBox.setToolTipText(Bundle.getMessage("TipCarOutOfService"));
        autoReturnWhenEmptyTrackCheckBox.setToolTipText(Bundle.getMessage("rsTipAutoTrack"));

        // get notified if combo box gets modified
        carLoads.addPropertyChangeListener(this);
        carManager.addPropertyChangeListener(this);
        InstanceManager.getDefault(DivisionManager.class).addPropertyChangeListener(this);

        packFrame();
    }

    public void loadCar(Car car) {
        _car = car;
        load(car);
        updateLoadComboBox();
        updateRweLoadComboBox();
        updateRwlLoadComboBox();
        updateDivisionComboBox();
        updateKernelComboBox();
    }

    @Override
    protected ResourceBundle getRb() {
        return rb;
    }

    @Override
    protected void updateComboBoxes() {
        super.updateComboBoxes();

        locationManager.updateComboBox(destReturnWhenEmptyBox);
        locationManager.updateComboBox(destReturnWhenLoadedBox);

        updateFinalDestinationComboBoxes();
        updateReturnWhenEmptyComboBoxes();
        updateReturnWhenLoadedComboBoxes();
    }

    @Override
    protected void enableComponents(boolean enabled) {
        // If routing is disabled, the RWE and Final Destination fields do not work
        if (!Setup.isCarRoutingEnabled()) {
            ignoreRWECheckBox.setSelected(true);
            ignoreRWLCheckBox.setSelected(true);
            ignoreFinalDestinationCheckBox.setSelected(true);
        }

        super.enableComponents(enabled);

        ignoreRWECheckBox.setEnabled(Setup.isCarRoutingEnabled() && enabled);
        destReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected() && enabled);
        trackReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected() && enabled);
        loadReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected() && enabled);
        autoReturnWhenEmptyTrackCheckBox.setEnabled(!ignoreRWECheckBox.isSelected() && enabled);

        ignoreRWLCheckBox.setEnabled(Setup.isCarRoutingEnabled() && enabled);
        destReturnWhenLoadedBox.setEnabled(!ignoreRWLCheckBox.isSelected() && enabled);
        trackReturnWhenLoadedBox.setEnabled(!ignoreRWLCheckBox.isSelected() && enabled);
        loadReturnWhenLoadedBox.setEnabled(!ignoreRWLCheckBox.isSelected() && enabled);
        autoReturnWhenLoadedTrackCheckBox.setEnabled(!ignoreRWLCheckBox.isSelected() && enabled);

        ignoreLoadCheckBox.setEnabled(enabled);
        loadComboBox.setEnabled(!ignoreLoadCheckBox.isSelected() && enabled);
        editLoadButton.setEnabled(!ignoreLoadCheckBox.isSelected() && enabled && _car != null);
        
        ignoreDivisionCheckBox.setEnabled(enabled);
        divisionComboBox.setEnabled(!ignoreDivisionCheckBox.isSelected() && enabled);
        editDivisionButton.setEnabled(!ignoreDivisionCheckBox.isSelected() && enabled && _car != null);

        ignoreKernelCheckBox.setEnabled(enabled);
        kernelComboBox.setEnabled(!ignoreKernelCheckBox.isSelected() && enabled);
        editKernelButton.setEnabled(!ignoreKernelCheckBox.isSelected() && enabled && _car != null);

        enableDestinationFields(enabled);
    }

    private void enableDestinationFields(boolean enabled) {
        // if car in a built train, enable destination fields
        boolean enableDest = enableDestination ||
                destinationBox.getSelectedItem() != null ||
                (_car != null && _car.getTrain() != null && _car.getTrain().isBuilt());

        destinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected() && enableDest && enabled);
        trackDestinationBox.setEnabled(!ignoreDestinationCheckBox.isSelected() && enableDest && enabled);
        autoDestinationTrackCheckBox.setEnabled(!ignoreDestinationCheckBox.isSelected() && enableDest && enabled);
    }

    // combo boxes
    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        super.comboBoxActionPerformed(ae);
        if (ae.getSource() == finalDestinationBox) {
            updateFinalDestination();
        }
        if (ae.getSource() == destReturnWhenEmptyBox) {
            updateReturnWhenEmpty();
        }
        if (ae.getSource() == destReturnWhenLoadedBox) {
            updateReturnWhenLoaded();
        }
    }

    CarAttributeEditFrame f;

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        super.buttonActionPerformed(ae);
        if (ae.getSource() == editLoadButton && _car != null) {
            if (lef != null) {
                lef.dispose();
            }
            lef = new CarLoadEditFrame();
            lef.setLocationRelativeTo(this);
            lef.initComponents(_car.getTypeName(), (String) loadComboBox.getSelectedItem());
        }
        if (ae.getSource() == editKernelButton) {
            if (f != null) {
                f.dispose();
            }
            f = new CarAttributeEditFrame();
            f.setLocationRelativeTo(this);
            f.addPropertyChangeListener(this);
            f.initComponents(CarAttributeEditFrame.KERNEL, (String) kernelComboBox.getSelectedItem());
        }
        if (ae.getSource() == editDivisionButton) {
            if (def != null) {
                def.dispose();
            }
            def = new DivisionEditFrame((Division) divisionComboBox.getSelectedItem());
        }
    }

    @Override
    protected boolean save() {
        if (change(_car)) {
            OperationsXml.save();
            return true;
        }
        return false;
    }

    TrainByCarTypeFrame tctf = null;
    protected boolean askKernelChange = true;

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    protected boolean change(Car car) {
        // save the auto button
        autoReturnWhenEmptyTrackCheckBoxSelected = autoReturnWhenEmptyTrackCheckBox.isSelected();
        autoReturnWhenLoadedTrackCheckBoxSelected = autoReturnWhenLoadedTrackCheckBox.isSelected();

        // car load
        if (!ignoreLoadCheckBox.isSelected() && loadComboBox.getSelectedItem() != null) {
            String load = (String) loadComboBox.getSelectedItem();
            if (!car.getLoadName().equals(load)) {
                if (carLoads.containsName(car.getTypeName(), load)) {
                    car.setLoadName(load);
                    updateComboBoxesLoadChange();
                } else {
                    JOptionPane.showMessageDialog(this,
                            MessageFormat.format(Bundle.getMessage("carLoadNotValid"), load, car.getTypeName()),
                            Bundle.getMessage("carCanNotChangeLoad"), JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        // set final destination fields before destination in case there's a schedule at
        // destination
        if (!ignoreFinalDestinationCheckBox.isSelected()) {
            if (finalDestinationBox.getSelectedItem() == null) {
                car.setFinalDestination(null);
                car.setFinalDestinationTrack(null);
            } else {
                Track finalDestTrack = null;
                if (finalDestTrackBox.getSelectedItem() != null) {
                    finalDestTrack = (Track) finalDestTrackBox.getSelectedItem();
                }
                if (finalDestTrack != null &&
                        car.getFinalDestinationTrack() != finalDestTrack &&
                        finalDestTrack.isStaging()) {
                    log.debug("Destination track ({}) is staging", finalDestTrack.getName());
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("rsDoNotSelectStaging"),
                            Bundle.getMessage("rsCanNotFinal"), JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                car.setFinalDestination((Location) finalDestinationBox.getSelectedItem());
                car.setFinalDestinationTrack(finalDestTrack);
                String status = car.testDestination((Location) finalDestinationBox.getSelectedItem(), finalDestTrack);
                if (!status.equals(Track.OKAY)) {
                    JOptionPane.showMessageDialog(this,
                            MessageFormat.format(Bundle.getMessage("rsCanNotFinalMsg"), car.toString(), status),
                            Bundle.getMessage("rsCanNotFinal"), JOptionPane.WARNING_MESSAGE);
                }
            }
        }
        // division
        if (!ignoreDivisionCheckBox.isSelected()) {
            car.setDivision((Division) divisionComboBox.getSelectedItem());
        }
        // kernel
        if (!ignoreKernelCheckBox.isSelected() && kernelComboBox.getSelectedItem() != null) {
            if (kernelComboBox.getSelectedItem().equals(RollingStockManager.NONE)) {
                car.setKernel(null);
            } else if (!car.getKernelName().equals(kernelComboBox.getSelectedItem())) {
                car.setKernel(carManager.getKernelByName((String) kernelComboBox.getSelectedItem()));
                // if car has FRED or is caboose make lead
                if (car.hasFred() || car.isCaboose()) {
                    car.getKernel().setLead(car);
                }
                car.setBlocking(car.getKernel().getSize());
            }
        }
        // save car's track
        Track saveTrack = car.getTrack();
        if (!super.change(car)) {
            return false;
        }
        // return when empty fields
        if (!ignoreRWECheckBox.isSelected()) {
            // check that RWE load is valid for this car's type
            if (carLoads.getNames(car.getTypeName()).contains(loadReturnWhenEmptyBox.getSelectedItem())) {
                car.setReturnWhenEmptyLoadName((String) loadReturnWhenEmptyBox.getSelectedItem());
            } else {
                log.debug("Car ({}) type ({}) doesn't support RWE load ({})", car, car.getTypeName(),
                        loadReturnWhenEmptyBox.getSelectedItem());
                JOptionPane.showMessageDialog(this,
                        MessageFormat.format(Bundle.getMessage("carLoadNotValid"),
                                loadReturnWhenEmptyBox.getSelectedItem(), car.getTypeName()),
                        Bundle.getMessage("carCanNotChangeRweLoad"), JOptionPane.WARNING_MESSAGE);
            }
            if (destReturnWhenEmptyBox.getSelectedItem() == null) {
                car.setReturnWhenEmptyDestination(null);
                car.setReturnWhenEmptyDestTrack(null);
            } else {
                Location locationRWE = (Location) destReturnWhenEmptyBox.getSelectedItem();
                if (trackReturnWhenEmptyBox.getSelectedItem() != null) {
                    Track trackRWE = (Track) trackReturnWhenEmptyBox.getSelectedItem();
                    // warn user if they selected a staging track
                    if (trackRWE != null && trackRWE.isStaging()) {
                        log.debug("Return when empty track ({}) is staging", trackRWE.getName());
                        JOptionPane.showMessageDialog(this, Bundle.getMessage("rsDoNotSelectStaging"),
                                Bundle.getMessage("carCanNotRWE"), JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    // use a test car with a load of "E" and no length
                    String status = getTestCar(car, car.getReturnWhenEmptyLoadName()).testDestination(locationRWE,
                            trackRWE);
                    if (!status.equals(Track.OKAY)) {
                        JOptionPane.showMessageDialog(this,
                                MessageFormat.format(Bundle.getMessage("carCanNotRWEMsg"), car.toString(), status),
                                Bundle.getMessage("carCanNotRWE"), JOptionPane.WARNING_MESSAGE);
                    }
                    car.setReturnWhenEmptyDestTrack(trackRWE);
                } else {
                    car.setReturnWhenEmptyDestTrack(null);
                }
                car.setReturnWhenEmptyDestination(locationRWE);
            }
        }
        // return when loaded fields
        if (!ignoreRWLCheckBox.isSelected()) {
            // check that RWL load is valid for this car's type
            if (carLoads.getNames(car.getTypeName()).contains(loadReturnWhenLoadedBox.getSelectedItem())) {
                car.setReturnWhenLoadedLoadName((String) loadReturnWhenLoadedBox.getSelectedItem());
            } else {
                log.debug("Car ({}) type ({}) doesn't support RWL load ({})", car, car.getTypeName(),
                        loadReturnWhenLoadedBox.getSelectedItem());
                JOptionPane.showMessageDialog(this,
                        MessageFormat.format(Bundle.getMessage("carLoadNotValid"),
                                loadReturnWhenEmptyBox.getSelectedItem(), car.getTypeName()),
                        Bundle.getMessage("carCanNotChangeRwlLoad"), JOptionPane.WARNING_MESSAGE);
            }
            if (destReturnWhenLoadedBox.getSelectedItem() == null) {
                car.setReturnWhenLoadedDestination(null);
                car.setReturnWhenLoadedDestTrack(null);
            } else {
                Location locationRWL = (Location) destReturnWhenLoadedBox.getSelectedItem();
                if (trackReturnWhenLoadedBox.getSelectedItem() != null) {
                    Track trackRWL = (Track) trackReturnWhenLoadedBox.getSelectedItem();
                    // warn user if they selected a staging track
                    if (trackRWL != null && trackRWL.isStaging()) {
                        log.debug("Return when loaded track ({}) is staging", trackRWL.getName());
                        JOptionPane.showMessageDialog(this, Bundle.getMessage("rsDoNotSelectStaging"),
                                Bundle.getMessage("carCanNotRWL"), JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    // use a test car with a load of "E" and no length
                    String status = getTestCar(car, car.getReturnWhenLoadedLoadName()).testDestination(locationRWL,
                            trackRWL);
                    if (!status.equals(Track.OKAY)) {
                        JOptionPane.showMessageDialog(this,
                                MessageFormat.format(Bundle.getMessage("carCanNotRWLMsg"), car.toString(), status),
                                Bundle.getMessage("carCanNotRWL"), JOptionPane.WARNING_MESSAGE);
                    }
                    car.setReturnWhenLoadedDestTrack(trackRWL);
                } else {
                    car.setReturnWhenLoadedDestTrack(null);
                }
                car.setReturnWhenLoadedDestination(locationRWL);
            }
        }
        // check to see if there's a schedule when placing the car at a spur
        if (!ignoreLocationCheckBox.isSelected() &&
                trackLocationBox.getSelectedItem() != null &&
                saveTrack != trackLocationBox.getSelectedItem()) {
            Track track = (Track) trackLocationBox.getSelectedItem();
            if (track.getSchedule() != null) {
                if (JOptionPane
                        .showConfirmDialog(this,
                                MessageFormat.format(Bundle.getMessage("rsDoYouWantSchedule"), car.toString()),
                                MessageFormat.format(Bundle.getMessage("rsSpurHasSchedule"), track.getName(),
                                        track.getScheduleName()),
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    String results = track.checkSchedule(car);
                    if (!results.equals(Track.OKAY)) {
                        JOptionPane.showMessageDialog(this,
                                MessageFormat.format(Bundle.getMessage("rsNotAbleToApplySchedule"), results),
                                Bundle.getMessage("rsApplyingScheduleFailed"), JOptionPane.ERROR_MESSAGE);
                        // restore previous location and track so we'll ask to test schedule again
                        if (saveTrack != null) {
                            car.setLocation(saveTrack.getLocation(), saveTrack);
                        } else {
                            car.setLocation(null, null);
                        }
                        return false;
                    }
                    // now apply schedule to car
                    track.scheduleNext(car);
                    car.loadNext(track);
                }
            }
        }
        // determine if train services this car's load
        if (car.getTrain() != null) {
            Train train = car.getTrain();
            if (!train.isLoadNameAccepted(car.getLoadName(), car.getTypeName())) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("carTrainNotServLoad"),
                        car.getLoadName(), train.getName()), Bundle.getMessage("rsNotMove"), JOptionPane.ERROR_MESSAGE);
                // prevent rs from being picked up and delivered
                setRouteLocationAndDestination(car, train, null, null);
                return false;
            }
            if (car.getLocation() != null && car.getDestination() != null && !train.isServiceable(car)) {
                JOptionPane.showMessageDialog(this,
                        MessageFormat.format(Bundle.getMessage("carTrainNotService"), car.toString(), train.getName()),
                        Bundle.getMessage("rsNotMove"), JOptionPane.ERROR_MESSAGE);
                // show the train's route and car location
                if (tctf != null) {
                    tctf.dispose();
                }
                tctf = new TrainByCarTypeFrame(car);
                // prevent rs from being picked up and delivered
                setRouteLocationAndDestination(car, train, null, null);
                return false;
            }
        }
        checkTrain(car);
        // is this car part of a kernel?
        if (askKernelChange && car.getKernel() != null) {
            List<Car> list = car.getKernel().getGroup();
            if (list.size() > 1) {
                if (JOptionPane.showConfirmDialog(this,
                        MessageFormat.format(Bundle.getMessage("carInKernel"), car.toString()),
                        MessageFormat.format(Bundle.getMessage("carPartKernel"), car.getKernelName()),
                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (!updateGroup(list)) {
                        return false;
                    }
                } else if (outOfServiceCheckBox.isSelected()) {
                    car.setKernel(null); // don't leave car in kernel if out of service
                }
            }
        }
        return true;
    }

    /**
     * Update locations if load changes. New load could change which track are
     * allowed if auto selected. Return When Empty (RWE) always uses the default
     * empty load of "E".
     */
    protected void updateComboBoxesLoadChange() {
        if (autoTrackCheckBox.isSelected()) {
            updateLocationTrackComboBox();
        }
        if (autoDestinationTrackCheckBox.isSelected()) {
            updateDestinationTrackComboBox();
        }
        if (autoFinalDestTrackCheckBox.isSelected()) {
            updateFinalDestination();
        }
    }

    @Override
    protected boolean updateGroup(List<Car> list) {
        for (Car car : list) {
            if (car == _car) {
                continue;
            }
            // make all cars in kernel the same
            if (!ignoreRWECheckBox.isSelected()) {
                car.setReturnWhenEmptyDestination(_car.getReturnWhenEmptyDestination());
                car.setReturnWhenEmptyDestTrack(_car.getReturnWhenEmptyDestTrack());
            }
            if (!ignoreRWLCheckBox.isSelected()) {
                car.setReturnWhenLoadedDestination(_car.getReturnWhenLoadedDestination());
                car.setReturnWhenLoadedDestTrack(_car.getReturnWhenLoadedDestTrack());
            }
            if (!ignoreFinalDestinationCheckBox.isSelected()) {
                car.setFinalDestination(_car.getFinalDestination());
                car.setFinalDestinationTrack(_car.getFinalDestinationTrack());
            }
            // update car load
            if (!ignoreLoadCheckBox.isSelected() && carLoads.containsName(car.getTypeName(), _car.getLoadName())) {
                car.setLoadName(_car.getLoadName());
            }
            // update kernel
            if (!ignoreKernelCheckBox.isSelected()) {
                car.setKernel(_car.getKernel());
            }
            // update division
            if (!ignoreDivisionCheckBox.isSelected()) {
                car.setDivision(_car.getDivision());
            }
        }
        return super.updateGroup(list);
    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        super.checkBoxActionPerformed(ae);
        if (ae.getSource() == autoFinalDestTrackCheckBox) {
            updateFinalDestination();
        }
        if (ae.getSource() == autoReturnWhenEmptyTrackCheckBox) {
            updateReturnWhenEmpty();
        }
        if (ae.getSource() == autoReturnWhenLoadedTrackCheckBox) {
            updateReturnWhenLoaded();
        }
        if (ae.getSource() == autoTrainCheckBox) {
            updateTrainComboBox();
        }
        if (ae.getSource() == ignoreDestinationCheckBox) {
            divisionComboBox.setEnabled(!ignoreDestinationCheckBox.isSelected());
        }
        if (ae.getSource() == ignoreRWECheckBox) {
            destReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected());
            trackReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected());
            loadReturnWhenEmptyBox.setEnabled(!ignoreRWECheckBox.isSelected());
            autoReturnWhenEmptyTrackCheckBox.setEnabled(!ignoreRWECheckBox.isSelected());
        }
        if (ae.getSource() == ignoreRWLCheckBox) {
            destReturnWhenLoadedBox.setEnabled(!ignoreRWLCheckBox.isSelected());
            trackReturnWhenLoadedBox.setEnabled(!ignoreRWLCheckBox.isSelected());
            loadReturnWhenLoadedBox.setEnabled(!ignoreRWLCheckBox.isSelected());
            autoReturnWhenLoadedTrackCheckBox.setEnabled(!ignoreRWLCheckBox.isSelected());
        }
        if (ae.getSource() == ignoreLoadCheckBox) {
            loadComboBox.setEnabled(!ignoreLoadCheckBox.isSelected());
            editLoadButton.setEnabled(!ignoreLoadCheckBox.isSelected() && _car != null);
        }
        if (ae.getSource() == ignoreDivisionCheckBox) {
            divisionComboBox.setEnabled(!ignoreDivisionCheckBox.isSelected());
            editDivisionButton.setEnabled(!ignoreDivisionCheckBox.isSelected());
        }
        if (ae.getSource() == ignoreKernelCheckBox) {
            kernelComboBox.setEnabled(!ignoreKernelCheckBox.isSelected());
            editKernelButton.setEnabled(!ignoreKernelCheckBox.isSelected());
        }
    }

    protected void updateReturnWhenEmptyComboBoxes() {
        if (_car != null) {
            log.debug("Updating return when empty for car ({})", _car);
            destReturnWhenEmptyBox.setSelectedItem(_car.getReturnWhenEmptyDestination());
        }
        updateReturnWhenEmpty();
    }

    protected void updateReturnWhenEmpty() {
        if (destReturnWhenEmptyBox.getSelectedItem() == null) {
            trackReturnWhenEmptyBox.removeAllItems();
        } else {
            log.debug("CarSetFrame sees return when empty: {}", destReturnWhenEmptyBox.getSelectedItem());
            Location loc = (Location) destReturnWhenEmptyBox.getSelectedItem();
            loc.updateComboBox(trackReturnWhenEmptyBox, getTestCar(_car, _car.getReturnWhenEmptyLoadName()),
                    autoReturnWhenEmptyTrackCheckBox.isSelected(), true);
            if (_car != null &&
                    _car.getReturnWhenEmptyDestination() != null &&
                    _car.getReturnWhenEmptyDestination().equals(loc) &&
                    _car.getReturnWhenEmptyDestTrack() != null) {
                trackReturnWhenEmptyBox.setSelectedItem(_car.getReturnWhenEmptyDestTrack());
            }
        }
    }

    protected void updateReturnWhenLoadedComboBoxes() {
        if (_car != null) {
            log.debug("Updating return when loaded for car ({})", _car);
            destReturnWhenLoadedBox.setSelectedItem(_car.getReturnWhenLoadedDestination());
        }
        updateReturnWhenLoaded();
    }

    protected void updateReturnWhenLoaded() {
        if (destReturnWhenLoadedBox.getSelectedItem() == null) {
            trackReturnWhenLoadedBox.removeAllItems();
        } else {
            log.debug("CarSetFrame sees return when empty: {}", destReturnWhenLoadedBox.getSelectedItem());
            Location loc = (Location) destReturnWhenLoadedBox.getSelectedItem();
            loc.updateComboBox(trackReturnWhenLoadedBox, getTestCar(_car, _car.getReturnWhenLoadedLoadName()),
                    autoReturnWhenLoadedTrackCheckBox.isSelected(), true);
            if (_car != null &&
                    _car.getReturnWhenLoadedDestination() != null &&
                    _car.getReturnWhenLoadedDestination().equals(loc) &&
                    _car.getReturnWhenLoadedDestTrack() != null) {
                trackReturnWhenLoadedBox.setSelectedItem(_car.getReturnWhenLoadedDestTrack());
            }
        }
    }

    protected void updateFinalDestinationComboBoxes() {
        if (_car != null) {
            log.debug("Updating final destinations for car ({})", _car);
            finalDestinationBox.setSelectedItem(_car.getFinalDestination());
        }
        updateFinalDestination();
    }

    protected void updateFinalDestination() {
        if (finalDestinationBox.getSelectedItem() == null) {
            finalDestTrackBox.removeAllItems();
        } else {
            log.debug("CarSetFrame sees final destination: {}", finalDestinationBox.getSelectedItem());
            Location l = (Location) finalDestinationBox.getSelectedItem();
            l.updateComboBox(finalDestTrackBox, _car, autoFinalDestTrackCheckBox.isSelected(), true);
            if (_car != null &&
                    _car.getFinalDestination() != null &&
                    _car.getFinalDestination().equals(l) &&
                    _car.getFinalDestinationTrack() != null) {
                finalDestTrackBox.setSelectedItem(_car.getFinalDestinationTrack());
            }
        }
    }

    protected void updateLoadComboBox() {
        if (_car != null) {
            log.debug("Updating load box for car ({})", _car);
            carLoads.updateComboBox(_car.getTypeName(), loadComboBox);
            loadComboBox.setSelectedItem(_car.getLoadName());
        }
    }

    protected void updateRweLoadComboBox() {
        if (_car != null) {
            log.debug("Updating RWE load box for car ({})", _car);
            carLoads.updateRweComboBox(_car.getTypeName(), loadReturnWhenEmptyBox);
            loadReturnWhenEmptyBox.setSelectedItem(_car.getReturnWhenEmptyLoadName());
        }
    }

    protected void updateRwlLoadComboBox() {
        if (_car != null) {
            log.debug("Updating RWL load box for car ({})", _car);
            carLoads.updateRwlComboBox(_car.getTypeName(), loadReturnWhenLoadedBox);
            loadReturnWhenLoadedBox.setSelectedItem(_car.getReturnWhenLoadedLoadName());
        }
    }

    protected void updateKernelComboBox() {
        carManager.updateKernelComboBox(kernelComboBox);
        if (_car != null) {
            kernelComboBox.setSelectedItem(_car.getKernelName());
        }
    }
    
    protected void updateDivisionComboBox() {
        InstanceManager.getDefault(DivisionManager.class).updateComboBox(divisionComboBox);
        if (_car != null) {
            divisionComboBox.setSelectedItem(_car.getDivision());
        }
    }

    @Override
    protected void updateTrainComboBox() {
        log.debug("update train combo box");
        if (_car != null && autoTrainCheckBox.isSelected()) {
            log.debug("Updating train box for car ({})", _car);
            trainManager.updateTrainComboBox(trainBox, _car);
        } else {
            trainManager.updateTrainComboBox(trainBox);
        }
        if (_car != null) {
            trainBox.setSelectedItem(_car.getTrain());
        }
    }

    private Car getTestCar(Car car, String loadName) {
        Car c = car;
        // clone car and set the load to RWE and a length of zero
        if (car != null) {
            c = car.copy();
            c.setLoadName(loadName);
            c.setLength(Integer.toString(-RollingStock.COUPLERS)); // ignore car length
        }
        return c;
    }

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    public void setDestinationEnabled(boolean enable) {
        enableDestination = !enableDestination;
        enableDestinationFields(!locationUnknownCheckBox.isSelected());
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(CarLoads.class).removePropertyChangeListener(this);
        carManager.removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("PropertyChange ({}) new ({})", e.getPropertyName(), e.getNewValue());
        super.propertyChange(e);
        if (e.getPropertyName().equals(Car.FINAL_DESTINATION_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Car.FINAL_DESTINATION_TRACK_CHANGED_PROPERTY)) {
            updateFinalDestinationComboBoxes();
        }
        if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Car.LOAD_CHANGED_PROPERTY)) {
            updateLoadComboBox();
        }
        if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(CarLoads.LOAD_TYPE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Car.RWE_LOAD_CHANGED_PROPERTY)) {
            updateRweLoadComboBox();
        }
        if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(CarLoads.LOAD_TYPE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Car.RWL_LOAD_CHANGED_PROPERTY)) {
            updateRwlLoadComboBox();
        }
        if (e.getPropertyName().equals(Car.RETURN_WHEN_EMPTY_CHANGED_PROPERTY)) {
            updateReturnWhenEmptyComboBoxes();
        }
        if (e.getPropertyName().equals(Car.RETURN_WHEN_LOADED_CHANGED_PROPERTY)) {
            updateReturnWhenLoadedComboBoxes();
        }
        if (e.getPropertyName().equals(CarManager.KERNEL_LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Car.KERNEL_NAME_CHANGED_PROPERTY)) {
            updateKernelComboBox();
        }
        if (e.getPropertyName().equals(DivisionManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateDivisionComboBox();
        }
        if (e.getPropertyName().equals(RollingStock.TRAIN_CHANGED_PROPERTY)) {
            enableDestinationFields(!locationUnknownCheckBox.isSelected());
        }
        if (e.getPropertyName().equals(CarAttributeEditFrame.DISPOSE)) {
            f = null;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(CarSetFrame.class);
}
