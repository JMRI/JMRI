package jmri.jmrit.operations.rollingstock.cars;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.*;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.divisions.*;
import jmri.jmrit.operations.rollingstock.*;
import jmri.jmrit.operations.rollingstock.cars.tools.*;
import jmri.jmrit.operations.router.Router;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.tools.TrainByCarTypeFrame;
import jmri.util.swing.JmriJOptionPane;

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

    public Car _car;

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
    JComboBox<String> kernelComboBox = InstanceManager.getDefault(KernelManager.class).getComboBox();

    // buttons
    JButton editDivisionButton = new JButton(Bundle.getMessage("ButtonEdit"));
    protected JButton editLoadButton = new JButton(Bundle.getMessage("ButtonEdit"));
    JButton editKernelButton = new JButton(Bundle.getMessage("ButtonEdit"));

    // check boxes
    public JCheckBox ignoreDivisionCheckBox = new JCheckBox(Bundle.getMessage(IGNORE));
    public JCheckBox ignoreRWECheckBox = new JCheckBox(Bundle.getMessage(IGNORE));
    protected JCheckBox autoReturnWhenEmptyTrackCheckBox = new JCheckBox(Bundle.getMessage("Auto"));
    public JCheckBox ignoreRWLCheckBox = new JCheckBox(Bundle.getMessage(IGNORE));
    protected JCheckBox autoReturnWhenLoadedTrackCheckBox = new JCheckBox(Bundle.getMessage("Auto"));
    public JCheckBox ignoreLoadCheckBox = new JCheckBox(Bundle.getMessage(IGNORE));
    public JCheckBox ignoreKernelCheckBox = new JCheckBox(Bundle.getMessage(IGNORE));

    // Auto checkbox state
    private static boolean autoReturnWhenEmptyTrackCheckBoxSelected = false;
    private static boolean autoReturnWhenLoadedTrackCheckBoxSelected = false;

    private static boolean enableDestination = false;
    
    private String _help = "package.jmri.jmrit.operations.Operations_CarsSet";

    public CarSetFrame() {
        super(Bundle.getMessage("TitleCarSet"));
    }
    
    public void initComponents(String help) {
        _help = help;
        initComponents();
    }

    @Override
    public void initComponents() {
        super.initComponents();

        // build menu
        JMenuBar menuBar = new JMenuBar();
        JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));
        toolMenu.add(new EnableDestinationAction(this));
        toolMenu.add(new CarRoutingReportAction(this, true)); // preview
        toolMenu.add(new CarRoutingReportAction(this, false)); // print
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        addHelpMenu(_help, true); // NOI18N

        // initial caps for some languages i.e. German
        editLoadButton.setToolTipText(
                Bundle.getMessage("TipAddDeleteReplace", Bundle.getMessage("load")));
        editKernelButton.setToolTipText(Bundle.getMessage("TipAddDeleteReplace",
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
        loadComboBox.setName("loadComboBox");
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
        kernelComboBox.setName("kernelComboBox"); // NOI18N for UI Test
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
        autoReturnWhenLoadedTrackCheckBox.setToolTipText(Bundle.getMessage("rsTipAutoTrack"));

        // get notified if combo box gets modified
        carLoads.addPropertyChangeListener(this);
        carManager.addPropertyChangeListener(this);
        InstanceManager.getDefault(KernelManager.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(DivisionManager.class).addPropertyChangeListener(this);

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
    }

    public void load(Car car) {
        _car = car;
        super.load(car);
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
            ignoreDivisionCheckBox.setSelected(true);
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
        
        ignoreDivisionCheckBox.setEnabled(Setup.isCarRoutingEnabled() && enabled);
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
            updateFinalDestinationTrack();
        }
        if (ae.getSource() == destReturnWhenEmptyBox) {
            updateReturnWhenEmptyTrack();
        }
        if (ae.getSource() == destReturnWhenLoadedBox) {
            updateReturnWhenLoadedTrack();
        }
    }

    CarLoadEditFrame lef;
    CarAttributeEditFrame cef;
    DivisionEditFrame def;    

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        super.buttonActionPerformed(ae);
        if (ae.getSource() == editLoadButton && _car != null) {
            if (lef != null) {
                lef.dispose();
            }
            lef = new CarLoadEditFrame();
            lef.initComponents(_car.getTypeName(), (String) loadComboBox.getSelectedItem());
        }
        if (ae.getSource() == editKernelButton) {
            if (cef != null) {
                cef.dispose();
            }
            cef = new CarAttributeEditFrame();
            cef.addPropertyChangeListener(this);
            cef.initComponents(CarAttributeEditFrame.KERNEL, (String) kernelComboBox.getSelectedItem());
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

    protected boolean askKernelChange = true;

    @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", justification = "GUI ease of use")
    protected boolean change(Car car) {
        // save the auto button
        autoReturnWhenEmptyTrackCheckBoxSelected = autoReturnWhenEmptyTrackCheckBox.isSelected();
        autoReturnWhenLoadedTrackCheckBoxSelected = autoReturnWhenLoadedTrackCheckBox.isSelected();

        // save car's track in case there's a schedule
        Track saveTrack = car.getTrack();
        // update location
        if (!changeLocation(car)) {
            return false;
        }
        // car load
        setCarLoad(car);
        // set final destination fields before destination in case there's a schedule at
        // destination
        if (!setCarFinalDestination(car)) {
            return false;
        }
        // division
        if (!ignoreDivisionCheckBox.isSelected()) {
            car.setDivision((Division) divisionComboBox.getSelectedItem());
        }
        // kernel
        setCarKernel(car);
        if (!super.change(car)) {
            return false;
        }
        // return when empty fields
        if (!setCarRWE(car)) {
            return false;
        }
        // return when loaded fields
        if (!setCarRWL(car)) {
            return false;
        }
        // check to see if there's a schedule when placing the car at a spur
        if (!applySchedule(car, saveTrack)) {
            return false;
        }
        // determine if train services this car's load
        if (!checkTrainLoad(car)) {
            return false;
        }
        // determine if train's route can service car
        if (!checkTrainRoute(car)) {
            return false;
        }
        checkTrain(car);
        // is this car part of a kernel?
        if (askKernelChange && car.getKernel() != null) {
            List<Car> list = car.getKernel().getCars();
            if (list.size() > 1) {
                if (JmriJOptionPane.showConfirmDialog(this,
                        Bundle.getMessage("carInKernel", car.toString()),
                        Bundle.getMessage("carPartKernel", car.getKernelName()),
                        JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
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
    
    private void setCarLoad(Car car) {
        if (!ignoreLoadCheckBox.isSelected() && loadComboBox.getSelectedItem() != null) {
            String load = (String) loadComboBox.getSelectedItem();
            if (!car.getLoadName().equals(load)) {
                if (carLoads.containsName(car.getTypeName(), load)) {
                    car.setLoadName(load);
                    car.setWait(0); // car could be at spur with schedule
                    car.setScheduleItemId(Car.NONE);
                    updateComboBoxesLoadChange();
                } else {
                    JmriJOptionPane.showMessageDialog(this,
                            Bundle.getMessage("carLoadNotValid", load, car.getTypeName()),
                            Bundle.getMessage("carCanNotChangeLoad"), JmriJOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }
    
    private boolean setCarFinalDestination(Car car) {
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
                    JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("rsDoNotSelectStaging"),
                            Bundle.getMessage("rsCanNotFinal"), JmriJOptionPane.ERROR_MESSAGE);
                    return false;
                }
                car.setFinalDestination((Location) finalDestinationBox.getSelectedItem());
                car.setFinalDestinationTrack(finalDestTrack);
                String status = getTestCar(car, car.getLoadName())
                        .checkDestination(car.getFinalDestination(), finalDestTrack);
                // ignore custom load warning
                if (!status.equals(Track.OKAY) && !status.contains(Track.CUSTOM)) {
                    JmriJOptionPane.showMessageDialog(this,
                            Bundle.getMessage("rsCanNotFinalMsg", car.toString(), status),
                            Bundle.getMessage("rsCanNotFinal"), JmriJOptionPane.WARNING_MESSAGE);
                    return false;
                } else {
                    // check to see if car can be routed to final destination
                    Router router = InstanceManager.getDefault(Router.class);
                    if (!router.isCarRouteable(car, null, car.getFinalDestination(), finalDestTrack, null)) {
                        JmriJOptionPane.showMessageDialog(this,
                                Bundle.getMessage("rsCanNotRouteMsg", car.toString(),
                                        car.getLocationName(), car.getTrackName(), car.getFinalDestinationName(),
                                        car.getFinalDestinationTrackName()),
                                Bundle.getMessage("rsCanNotFinal"), JmriJOptionPane.WARNING_MESSAGE);
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private void setCarKernel(Car car) {
        if (!ignoreKernelCheckBox.isSelected() && kernelComboBox.getSelectedItem() != null) {
            if (kernelComboBox.getSelectedItem().equals(RollingStockManager.NONE)) {
                car.setKernel(null);
                if (!car.isPassenger()) {
                    car.setBlocking(Car.DEFAULT_BLOCKING_ORDER);
                }
            } else if (!car.getKernelName().equals(kernelComboBox.getSelectedItem())) {
                car.setKernel(InstanceManager.getDefault(KernelManager.class).getKernelByName((String) kernelComboBox.getSelectedItem()));
                // if car has FRED or is caboose make lead
                if (car.hasFred() || car.isCaboose()) {
                    car.getKernel().setLead(car);
                }
                car.setBlocking(car.getKernel().getSize());
            }
        }
    }
    
    private boolean setCarRWE(Car car) {
        if (!ignoreRWECheckBox.isSelected()) {
            // check that RWE load is valid for this car's type
            if (carLoads.getNames(car.getTypeName()).contains(loadReturnWhenEmptyBox.getSelectedItem())) {
                car.setReturnWhenEmptyLoadName((String) loadReturnWhenEmptyBox.getSelectedItem());
            } else {
                log.debug("Car ({}) type ({}) doesn't support RWE load ({})", car, car.getTypeName(),
                        loadReturnWhenEmptyBox.getSelectedItem());
                JmriJOptionPane.showMessageDialog(this,
                        Bundle.getMessage("carLoadNotValid",
                                loadReturnWhenEmptyBox.getSelectedItem(), car.getTypeName()),
                        Bundle.getMessage("carCanNotChangeRweLoad"), JmriJOptionPane.WARNING_MESSAGE);
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
                        JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("rsDoNotSelectStaging"),
                                Bundle.getMessage("carCanNotRWE"), JmriJOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    // use a test car with a load of "RWE" and no length
                    String status = getTestCar(car, car.getReturnWhenEmptyLoadName()).checkDestination(locationRWE,
                            trackRWE);
                    if (!status.equals(Track.OKAY)) {
                        JmriJOptionPane.showMessageDialog(this,
                                Bundle.getMessage("carCanNotRWEMsg", car.toString(), locationRWE,
                                        trackRWE, status),
                                Bundle.getMessage("carCanNotRWE"), JmriJOptionPane.WARNING_MESSAGE);
                    }
                    car.setReturnWhenEmptyDestTrack(trackRWE);
                } else {
                    car.setReturnWhenEmptyDestTrack(null);
                }
                car.setReturnWhenEmptyDestination(locationRWE);
            }
        }
        return true;
    }
    
    private boolean setCarRWL(Car car) {
        if (!ignoreRWLCheckBox.isSelected()) {
            // check that RWL load is valid for this car's type
            if (carLoads.getNames(car.getTypeName()).contains(loadReturnWhenLoadedBox.getSelectedItem())) {
                car.setReturnWhenLoadedLoadName((String) loadReturnWhenLoadedBox.getSelectedItem());
            } else {
                log.debug("Car ({}) type ({}) doesn't support RWL load ({})", car, car.getTypeName(),
                        loadReturnWhenLoadedBox.getSelectedItem());
                JmriJOptionPane.showMessageDialog(this,
                        Bundle.getMessage("carLoadNotValid",
                                loadReturnWhenEmptyBox.getSelectedItem(), car.getTypeName()),
                        Bundle.getMessage("carCanNotChangeRwlLoad"), JmriJOptionPane.WARNING_MESSAGE);
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
                        JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("rsDoNotSelectStaging"),
                                Bundle.getMessage("carCanNotRWL"), JmriJOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                    // use a test car with a load of "RWL" and no length
                    String status = getTestCar(car, car.getReturnWhenLoadedLoadName()).checkDestination(locationRWL,
                            trackRWL);
                    if (!status.equals(Track.OKAY)) {
                        JmriJOptionPane.showMessageDialog(this,
                                Bundle.getMessage("carCanNotRWLMsg", car.toString(), locationRWL,
                                        trackRWL, status),
                                Bundle.getMessage("carCanNotRWL"), JmriJOptionPane.WARNING_MESSAGE);
                    }
                    car.setReturnWhenLoadedDestTrack(trackRWL);
                } else {
                    car.setReturnWhenLoadedDestTrack(null);
                }
                car.setReturnWhenLoadedDestination(locationRWL);
            }
        }
        return true;
    }
    
    private boolean applySchedule(Car car, Track saveTrack) {
        if (!ignoreLocationCheckBox.isSelected() &&
                trackLocationBox.getSelectedItem() != null &&
                saveTrack != trackLocationBox.getSelectedItem()) {
            Track track = (Track) trackLocationBox.getSelectedItem();
            if (track.getSchedule() != null) {
                if (JmriJOptionPane
                        .showConfirmDialog(this,
                                Bundle.getMessage("rsDoYouWantSchedule", car.toString()),
                                Bundle.getMessage("rsSpurHasSchedule", track.getName(),
                                        track.getScheduleName()),
                                JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION) {
                    String results = track.checkSchedule(car);
                    if (!results.equals(Track.OKAY)) {
                        JmriJOptionPane.showMessageDialog(this,
                                Bundle.getMessage("rsNotAbleToApplySchedule", results),
                                Bundle.getMessage("rsApplyingScheduleFailed"), JmriJOptionPane.ERROR_MESSAGE);
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
        return true;
    }
    
    private boolean checkTrainLoad(Car car) {
        if (car.getTrain() != null) {
            Train train = car.getTrain();
            if (!train.isLoadNameAccepted(car.getLoadName(), car.getTypeName())) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("carTrainNotServLoad",
                        car.getLoadName(), train.getName()), Bundle.getMessage("rsNotMove"), JmriJOptionPane.ERROR_MESSAGE);
                // prevent rs from being picked up and delivered
                setRouteLocationAndDestination(car, train, null, null);
                return false;
            }
        }
        return true;
    }

    TrainByCarTypeFrame tctf = null;
    
    private boolean checkTrainRoute(Car car) {
        if (car.getTrain() != null) {
            Train train = car.getTrain();
            if (car.getLocation() != null && car.getDestination() != null && !train.isServiceable(car)) {
                JmriJOptionPane.showMessageDialog(this,
                        Bundle.getMessage("carTrainNotService", car.toString(), train.getName()),
                        Bundle.getMessage("rsNotMove"), JmriJOptionPane.ERROR_MESSAGE);
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
        return true;
    }

    /**
     * Update locations if load changes. New load could change which track are
     * allowed if auto selected.
     */
    protected void updateComboBoxesLoadChange() {
        if (autoTrackCheckBox.isSelected()) {
            updateLocationTrackComboBox();
        }
        if (autoDestinationTrackCheckBox.isSelected()) {
            updateDestinationTrackComboBox();
        }
        if (autoFinalDestTrackCheckBox.isSelected()) {
            updateFinalDestinationTrack();
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
                car.setWait(0); // car could be at spur with schedule
                car.setScheduleItemId(Car.NONE);
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
            updateFinalDestinationTrack();
        }
        if (ae.getSource() == autoReturnWhenEmptyTrackCheckBox) {
            updateReturnWhenEmptyTrack();
        }
        if (ae.getSource() == autoReturnWhenLoadedTrackCheckBox) {
            updateReturnWhenLoadedTrack();
        }
        if (ae.getSource() == autoTrainCheckBox) {
            updateTrainComboBox();
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
        updateReturnWhenEmptyTrack();
    }

    protected void updateReturnWhenEmptyTrack() {
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
        updateReturnWhenLoadedTrack();
    }

    protected void updateReturnWhenLoadedTrack() {
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
        updateFinalDestinationTrack();
    }

    protected void updateFinalDestinationTrack() {
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
        InstanceManager.getDefault(KernelManager.class).updateComboBox(kernelComboBox);
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
        // clone car and set the load and a length of zero
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
        if (lef != null) {
            lef.dispose();
        }
        if (cef != null) {
            cef.dispose();
        }
        if (def != null) {
            def.dispose();
        }
        if (tctf != null) {
            tctf.dispose();
        }
        InstanceManager.getDefault(CarLoads.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(KernelManager.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(DivisionManager.class).removePropertyChangeListener(this);
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
        if (e.getPropertyName().equals(KernelManager.LISTLENGTH_CHANGED_PROPERTY) ||
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
            cef = null;
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CarSetFrame.class);
}
