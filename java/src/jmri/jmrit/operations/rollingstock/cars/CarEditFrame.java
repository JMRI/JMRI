// CarEditFrame.java
package jmri.jmrit.operations.rollingstock.cars;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import jmri.IdTag;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of car
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2014
 * @version $Revision: 29493 $
 */
public class CarEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    CarManager carManager = CarManager.instance();
    CarManagerXml managerXml = CarManagerXml.instance();
    LocationManager locationManager = LocationManager.instance();

    Car _car;

    // labels
    JLabel textWeightOz = new JLabel(Bundle.getMessage("WeightOz"));
    JLabel textWeightTons = new JLabel(Bundle.getMessage("WeightTons"));

    // major buttons
    JButton editRoadButton = new JButton(Bundle.getMessage("Edit"));
    JButton clearRoadNumberButton = new JButton(Bundle.getMessage("Clear"));
    JButton editTypeButton = new JButton(Bundle.getMessage("Edit"));
    JButton editColorButton = new JButton(Bundle.getMessage("Edit"));
    JButton editLengthButton = new JButton(Bundle.getMessage("Edit"));
    JButton fillWeightButton = new JButton(Bundle.getMessage("Calculate"));
    JButton editLoadButton = new JButton(Bundle.getMessage("Edit"));
    JButton editKernelButton = new JButton(Bundle.getMessage("Edit"));
    JButton editOwnerButton = new JButton(Bundle.getMessage("Edit"));

    JButton saveButton = new JButton(Bundle.getMessage("Save"));
    JButton deleteButton = new JButton(Bundle.getMessage("Delete"));
    JButton addButton = new JButton(Bundle.getMessage("Add"));

    // check boxes
    JCheckBox autoCheckBox = new JCheckBox(Bundle.getMessage("Auto"));
    JCheckBox autoTrackCheckBox = new JCheckBox(Bundle.getMessage("Auto"));
    JCheckBox passengerCheckBox = new JCheckBox(Bundle.getMessage("Passenger"));
    JCheckBox cabooseCheckBox = new JCheckBox(Bundle.getMessage("Caboose"));
    JCheckBox fredCheckBox = new JCheckBox(Bundle.getMessage("Fred"));
    JCheckBox utilityCheckBox = new JCheckBox(Bundle.getMessage("Utility"));
    JCheckBox hazardousCheckBox = new JCheckBox(Bundle.getMessage("Hazardous"));

    // text field
    JTextField roadNumberTextField = new JTextField(Control.max_len_string_road_number);
    JTextField blockingTextField = new JTextField(4);
    JTextField builtTextField = new JTextField(Control.max_len_string_built_name + 3);
    JTextField weightTextField = new JTextField(Control.max_len_string_weight_name);
    JTextField weightTonsTextField = new JTextField(Control.max_len_string_weight_name);
    JTextField commentTextField = new JTextField(35);
    JTextField valueTextField = new JTextField(8);

    // combo boxes
    JComboBox<String> roadComboBox = CarRoads.instance().getComboBox();
    JComboBox<String> typeComboBox = CarTypes.instance().getComboBox();
    JComboBox<String> colorComboBox = CarColors.instance().getComboBox();
    JComboBox<String> lengthComboBox = CarLengths.instance().getComboBox();
    JComboBox<String> ownerComboBox = CarOwners.instance().getComboBox();
    JComboBox<Location> locationBox = locationManager.getComboBox();
    JComboBox<Track> trackLocationBox = new JComboBox<>();
    JComboBox<String> loadComboBox = CarLoads.instance().getComboBox(null);
    JComboBox<String> kernelComboBox = carManager.getKernelComboBox();
    JComboBox<IdTag> rfidComboBox = new JComboBox<IdTag>();

    // panels
    JPanel pBlocking = new JPanel();

    CarLoadEditFrame lef = null;

    public static final String ROAD = Bundle.getMessage("Road");
    public static final String TYPE = Bundle.getMessage("Type");
    public static final String COLOR = Bundle.getMessage("Color");
    public static final String LENGTH = Bundle.getMessage("Length");
    public static final String OWNER = Bundle.getMessage("Owner");
    public static final String KERNEL = Bundle.getMessage("Kernel");

    public CarEditFrame() {
        super();
    }

    public void initComponents() {
        // the following code sets the frame's initial state

        // load tool tips
        weightTextField.setToolTipText(Bundle.getMessage("TipCarWeightOz"));
        weightTonsTextField.setToolTipText(Bundle.getMessage("TipCarWeightTons"));
        autoCheckBox.setToolTipText(Bundle.getMessage("TipCarAutoCalculate"));
        autoTrackCheckBox.setToolTipText(Bundle.getMessage("rsTipAutoTrack"));
        passengerCheckBox.setToolTipText(Bundle.getMessage("TipCarPassenger"));
        cabooseCheckBox.setToolTipText(Bundle.getMessage("TipCarCaboose"));
        fredCheckBox.setToolTipText(Bundle.getMessage("TipCarFred"));
        utilityCheckBox.setToolTipText(Bundle.getMessage("TipCarUtility"));
        hazardousCheckBox.setToolTipText(Bundle.getMessage("TipCarHazardous"));
        blockingTextField.setToolTipText(Bundle.getMessage("TipPassengerCarBlocking"));
        fillWeightButton.setToolTipText(Bundle.getMessage("TipCalculateCarWeight"));
        builtTextField.setToolTipText(Bundle.getMessage("TipBuildDate"));
        valueTextField.setToolTipText(Bundle.getMessage("TipValue"));

        editRoadButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Road").toLowerCase()}));
        editTypeButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Type").toLowerCase()}));
        editColorButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Color").toLowerCase()}));
        editLengthButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Length").toLowerCase()}));
        editLoadButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Load").toLowerCase()}));
        editOwnerButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Owner").toLowerCase()}));
        editKernelButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Kernel").toLowerCase()}));

        // default check box selections
        autoCheckBox.setSelected(true);
        passengerCheckBox.setSelected(false);
        cabooseCheckBox.setSelected(false);
        fredCheckBox.setSelected(false);
        hazardousCheckBox.setSelected(false);

        // create panel
        JPanel pPanel = new JPanel();
        pPanel.setLayout(new BoxLayout(pPanel, BoxLayout.Y_AXIS));

        // Layout the panel by rows
        // row 1
        JPanel pRoad = new JPanel();
        pRoad.setLayout(new GridBagLayout());
        pRoad.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Road")));
        addItem(pRoad, roadComboBox, 1, 0);
        addItem(pRoad, editRoadButton, 2, 0);
        pPanel.add(pRoad);

        // row 2
        JPanel pRoadNumber = new JPanel();
        pRoadNumber.setLayout(new GridBagLayout());
        pRoadNumber.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RoadNumber")));
        addItem(pRoadNumber, roadNumberTextField, 1, 0);
        addItem(pRoadNumber, clearRoadNumberButton, 2, 0);
        pPanel.add(pRoadNumber);

        // row 3
        JPanel pType = new JPanel();
        pType.setLayout(new GridBagLayout());
        pType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
        addItem(pType, typeComboBox, 0, 0);
        addItem(pType, editTypeButton, 1, 0);
        
        JPanel pTypeOptions = new JPanel();   
        addItem(pTypeOptions, passengerCheckBox, 0, 1);
        addItem(pTypeOptions, cabooseCheckBox, 1, 1);
        addItem(pTypeOptions, fredCheckBox, 2, 1);
        addItem(pTypeOptions, utilityCheckBox, 3, 1);
        addItem(pTypeOptions, hazardousCheckBox, 4, 1);
        addItemWidth(pType, pTypeOptions, 3, 0, 1);     
        pPanel.add(pType);

        // row 3a
        pBlocking.setLayout(new GridBagLayout());
        pBlocking.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutPassengerBlocking")));
        addItem(pBlocking, blockingTextField, 0, 0);
        blockingTextField.setText("0");
        pPanel.add(pBlocking);
        pBlocking.setVisible(false);

        // row 4
        JPanel pLength = new JPanel();
        pLength.setLayout(new GridBagLayout());
        pLength.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Length")));
        addItem(pLength, lengthComboBox, 1, 0);
        addItem(pLength, editLengthButton, 2, 0);
        pPanel.add(pLength);

        // row 5
        // row 7
        JPanel pWeight = new JPanel();
        pWeight.setLayout(new GridBagLayout());
        pWeight.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Weight")));
        addItem(pWeight, textWeightOz, 0, 0);
        addItem(pWeight, weightTextField, 1, 0);
        addItem(pWeight, fillWeightButton, 2, 0);
        addItem(pWeight, autoCheckBox, 3, 0);
        addItem(pWeight, textWeightTons, 0, 1);
        addItem(pWeight, weightTonsTextField, 1, 1);
        pPanel.add(pWeight);

        // row 11
        JPanel pLocation = new JPanel();
        pLocation.setLayout(new GridBagLayout());
        pLocation.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocationAndTrack")));
        addItem(pLocation, locationBox, 1, 0);
        addItem(pLocation, trackLocationBox, 2, 0);
        addItem(pLocation, autoTrackCheckBox, 3, 0);
        pPanel.add(pLocation);

        // optional panel
        JPanel pOptional = new JPanel();
        pOptional.setLayout(new BoxLayout(pOptional, BoxLayout.Y_AXIS));
        JScrollPane optionPane = new JScrollPane(pOptional);
        optionPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOptional")));

        // row 12
        JPanel pColor = new JPanel();
        pColor.setLayout(new GridBagLayout());
        pColor.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Color")));
        addItem(pColor, colorComboBox, 1, 0);
        addItem(pColor, editColorButton, 2, 0);
        pOptional.add(pColor);

        // row 13
        JPanel pLoad = new JPanel();
        pLoad.setLayout(new GridBagLayout());
        pLoad.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Load")));
        addItem(pLoad, loadComboBox, 1, 0);
        addItem(pLoad, editLoadButton, 2, 0);
        pOptional.add(pLoad);

        // row 15
        JPanel pKernel = new JPanel();
        pKernel.setLayout(new GridBagLayout());
        pKernel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Kernel")));
        addItem(pKernel, kernelComboBox, 1, 0);
        addItem(pKernel, editKernelButton, 2, 0);
        pOptional.add(pKernel);

        // row 17
        JPanel pBuilt = new JPanel();
        pBuilt.setLayout(new GridBagLayout());
        pBuilt.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Built")));
        addItem(pBuilt, builtTextField, 1, 0);
        pOptional.add(pBuilt);

        // row 19
        JPanel pOwner = new JPanel();
        pOwner.setLayout(new GridBagLayout());
        pOwner.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Owner")));
        addItem(pOwner, ownerComboBox, 1, 0);
        addItem(pOwner, editOwnerButton, 2, 0);
        pOptional.add(pOwner);

        // row 20
        if (Setup.isValueEnabled()) {
            JPanel pValue = new JPanel();
            pValue.setLayout(new GridBagLayout());
            pValue.setBorder(BorderFactory.createTitledBorder(Setup.getValueLabel()));
            addItem(pValue, valueTextField, 1, 0);
            pOptional.add(pValue);
        }

        // row 22
        if (Setup.isRfidEnabled()) {
            JPanel pRfid = new JPanel();
            pRfid.setLayout(new GridBagLayout());
            pRfid.setBorder(BorderFactory.createTitledBorder(Setup.getRfidLabel()));
            addItem(pRfid, rfidComboBox, 1, 0);
            jmri.InstanceManager.getDefault(jmri.IdTagManager.class).getNamedBeanList().forEach((tag) -> rfidComboBox.addItem((jmri.IdTag) tag));
            pOptional.add(pRfid);
        }

        // row 24
        JPanel pComment = new JPanel();
        pComment.setLayout(new GridBagLayout());
        pComment.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        addItem(pComment, commentTextField, 1, 0);
        pOptional.add(pComment);

        // button panel
        JPanel pButtons = new JPanel();
        pButtons.setLayout(new GridBagLayout());
        addItem(pButtons, deleteButton, 0, 25);
        addItem(pButtons, addButton, 1, 25);
        addItem(pButtons, saveButton, 3, 25);

        // add panels
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        getContentPane().add(pPanel);
        getContentPane().add(optionPane);
        getContentPane().add(pButtons);

        // setup buttons
        addEditButtonAction(editRoadButton);
        addButtonAction(clearRoadNumberButton);
        addEditButtonAction(editTypeButton);
        addEditButtonAction(editLengthButton);
        addEditButtonAction(editColorButton);
        addEditButtonAction(editKernelButton);
        addEditButtonAction(editOwnerButton);

        addButtonAction(deleteButton);
        addButtonAction(addButton);
        addButtonAction(saveButton);
        addButtonAction(fillWeightButton);
        addButtonAction(editLoadButton);

        // setup combobox
        addComboBoxAction(typeComboBox);
        addComboBoxAction(lengthComboBox);
        addComboBoxAction(locationBox);

        // setup checkbox
        addCheckBoxAction(cabooseCheckBox);
        addCheckBoxAction(fredCheckBox);
        addCheckBoxAction(passengerCheckBox);
        addCheckBoxAction(autoTrackCheckBox);
        autoTrackCheckBox.setEnabled(false);

        // build menu
        // JMenuBar menuBar = new JMenuBar();
        // JMenu toolMenu = new JMenu(Bundle.getMessage("Tools"));
        // menuBar.add(toolMenu);
        // setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_CarsEdit", true); // NOI18N

        // get notified if combo box gets modified
        CarRoads.instance().addPropertyChangeListener(this);
        CarLoads.instance().addPropertyChangeListener(this);
        CarTypes.instance().addPropertyChangeListener(this);
        CarLengths.instance().addPropertyChangeListener(this);
        CarColors.instance().addPropertyChangeListener(this);
        CarOwners.instance().addPropertyChangeListener(this);
        locationManager.addPropertyChangeListener(this);
        carManager.addPropertyChangeListener(this);

        pack();
        setMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
        setVisible(true);
    }

    public void loadCar(Car car) {
        _car = car;

        if (!CarRoads.instance().containsName(car.getRoadName())) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("roadNameNotExist"),
                    new Object[]{car.getRoadName()}), Bundle.getMessage("carAddRoad"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                CarRoads.instance().addName(car.getRoadName());
            }
        }
        roadComboBox.setSelectedItem(car.getRoadName());

        roadNumberTextField.setText(car.getNumber());

        if (!CarTypes.instance().containsName(car.getTypeName())) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("typeNameNotExist"),
                    new Object[]{car.getTypeName()}), Bundle.getMessage("carAddType"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                CarTypes.instance().addName(car.getTypeName());
            }
        }
        typeComboBox.setSelectedItem(car.getTypeName());

        if (!CarLengths.instance().containsName(car.getLength())) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("lengthNameNotExist"),
                    new Object[]{car.getLength()}), Bundle.getMessage("carAddLength"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                CarLengths.instance().addName(car.getLength());
            }
        }
        lengthComboBox.setSelectedItem(car.getLength());

        if (!CarColors.instance().containsName(car.getColor())) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("colorNameNotExist"),
                    new Object[]{car.getColor()}), Bundle.getMessage("carAddColor"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                CarColors.instance().addName(car.getColor());
            }
        }
        colorComboBox.setSelectedItem(car.getColor());
        weightTextField.setText(car.getWeight());
        weightTonsTextField.setText(car.getWeightTons());
        passengerCheckBox.setSelected(car.isPassenger());
        cabooseCheckBox.setSelected(car.isCaboose());
        utilityCheckBox.setSelected(car.isUtility());
        fredCheckBox.setSelected(car.hasFred());
        hazardousCheckBox.setSelected(car.isHazardous());

        pBlocking.setVisible(passengerCheckBox.isSelected() || car.getKernel() != null);

        locationBox.setSelectedItem(car.getLocation());
        updateTrackLocationBox();

        builtTextField.setText(car.getBuilt());

        if (!CarOwners.instance().containsName(car.getOwner())) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("ownerNameNotExist"),
                    new Object[]{car.getOwner()}), Bundle.getMessage("addOwner"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                CarOwners.instance().addName(car.getOwner());
            }
        }
        ownerComboBox.setSelectedItem(car.getOwner());

        if (!CarLoads.instance().containsName(car.getTypeName(), car.getLoadName())) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("loadNameNotExist"),
                    new Object[]{car.getLoadName()}), Bundle.getMessage("addLoad"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                CarLoads.instance().addName(car.getTypeName(), car.getLoadName());
            }
        }
        // listen for changes in car load
        car.addPropertyChangeListener(this);
        CarLoads.instance().updateComboBox(car.getTypeName(), loadComboBox);
        loadComboBox.setSelectedItem(car.getLoadName());

        kernelComboBox.setSelectedItem(car.getKernelName());

        commentTextField.setText(car.getComment());
        valueTextField.setText(car.getValue());
        rfidComboBox.setSelectedItem(car.getIdTag());
        autoTrackCheckBox.setEnabled(true);
        blockingTextField.setText(Integer.toString(car.getBlocking()));

        setTitle(Bundle.getMessage("TitleCarEdit"));
    }

    // combo boxes
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == typeComboBox && typeComboBox.getSelectedItem() != null) {
            log.debug("Type comboBox sees change, update car loads");
            CarLoads.instance().updateComboBox((String) typeComboBox.getSelectedItem(), loadComboBox);
            // turnout off auto for location tracks
            autoTrackCheckBox.setSelected(false);
            autoTrackCheckBox.setEnabled(false);
            updateTrackLocationBox();
        }
        if (ae.getSource() == locationBox) {
            updateTrackLocationBox();
        }
        if (ae.getSource() == lengthComboBox && autoCheckBox.isSelected()) {
            calculateWeight();
        }
    }

    private void updateTrackLocationBox() {
        if (locationBox.getSelectedItem() == null) {
            trackLocationBox.removeAllItems();
        } else {
            log.debug("Update tracks for location: " + locationBox.getSelectedItem());
            Location loc = ((Location) locationBox.getSelectedItem());
            loc.updateComboBox(trackLocationBox, _car, autoTrackCheckBox.isSelected(), false);
            if (_car != null && _car.getLocation() == loc) {
                trackLocationBox.setSelectedItem(_car.getTrack());
            }
        }
    }

    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        JCheckBox b = (JCheckBox) ae.getSource();
        log.debug("checkbox change {}", b.getText());
        if (ae.getSource() == cabooseCheckBox && cabooseCheckBox.isSelected()) {
            fredCheckBox.setSelected(false);
        }
        if (ae.getSource() == fredCheckBox && fredCheckBox.isSelected()) {
            cabooseCheckBox.setSelected(false);
        }
        if (ae.getSource() == autoTrackCheckBox) {
            updateTrackLocationBox();
        }
        if (ae.getSource() == passengerCheckBox) {
            pBlocking.setVisible(passengerCheckBox.isSelected());
        }
    }

    // Save, Delete, Add, Clear, Calculate, Edit Load buttons
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            // log.debug("car save button pressed");
            if (!checkCar(_car)) {
                return;
            }
            // if the car's road or number changes, the car needs a new id
            if (_car != null
                    && !_car.getRoadName().equals(Car.NONE)
                    && (!_car.getRoadName().equals(roadComboBox.getSelectedItem()) || !_car.getNumber()
                            .equals(roadNumberTextField.getText()))) {
                String road = (String) roadComboBox.getSelectedItem();
                String number = roadNumberTextField.getText();
                carManager.changeId(_car, road, number);
                _car.setRoadName(road);
                _car.setNumber(number);
            }
            saveCar(true);
            // save car file
            writeFiles();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == deleteButton) {
            log.debug("car delete button activated");
            if (_car != null && _car.getRoadName().equals(roadComboBox.getSelectedItem())
                    && _car.getNumber().equals(roadNumberTextField.getText())) {
                carManager.deregister(_car);
                _car = null;
                // save car file
                writeFiles();
            } else {
                Car car = carManager.getByRoadAndNumber((String) roadComboBox.getSelectedItem(), roadNumberTextField
                        .getText());
                if (car != null) {
                    carManager.deregister(car);
                    // save car file
                    writeFiles();
                }
            }
        }
        if (ae.getSource() == addButton) {
            if (!checkCar(null)) {
                return;
            }
            saveCar(false);
            // save car file
            writeFiles();
        }
        if (ae.getSource() == clearRoadNumberButton) {
            roadNumberTextField.setText("");
            roadNumberTextField.requestFocus();
        }

        if (ae.getSource() == fillWeightButton) {
            calculateWeight();
        }
        if (ae.getSource() == editLoadButton) {
            if (lef != null) {
                lef.dispose();
            }
            lef = new CarLoadEditFrame();
            lef.setLocationRelativeTo(this);
            lef.initComponents((String) typeComboBox.getSelectedItem(), (String) loadComboBox.getSelectedItem());
        }
    }

    /**
     * Need to also write the location and train files if a road name was
     * deleted. Need to also write files if car type was changed.
     */
    private void writeFiles() {
        OperationsXml.save();
    }

    private boolean checkCar(Car car) {
        String roadNum = roadNumberTextField.getText();
        if (!OperationsXml.checkFileName(roadNum)) { // NOI18N
            log.error("Road number must not contain reserved characters");
            JOptionPane.showMessageDialog(this, Bundle.getMessage("NameResChar") + NEW_LINE
                    + Bundle.getMessage("ReservedChar"), Bundle.getMessage("roadNumNG"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (roadNum.length() > Control.max_len_string_road_number) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("carRoadNum"),
                    new Object[]{Control.max_len_string_road_number + 1}), Bundle.getMessage("carRoadLong"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // check to see if car with road and number already exists
        Car existingCar = carManager.getByRoadAndNumber((String) roadComboBox.getSelectedItem(), roadNumberTextField
                .getText());
        if (existingCar != null) {
            // new car?
            if (car == null) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("carRoadExists"), Bundle
                        .getMessage("carCanNotAdd"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
            // old car with new road or number?
            if (!existingCar.getId().equals(car.getId())) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("carRoadExists"), Bundle
                        .getMessage("carCanNotUpdate"), JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        // check car's weight has proper format
        try {
            Number number = NumberFormat.getNumberInstance().parse(weightTextField.getText());
            log.debug("Car weight in oz: {}", number);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("carWeightFormat"), Bundle
                    .getMessage("carActualWeight"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // check car's weight in tons has proper format
        try {
            Integer.parseInt(weightTonsTextField.getText());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("carWeightFormatTon"), Bundle
                    .getMessage("carWeightTon"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void calculateWeight() {
        if (lengthComboBox.getSelectedItem() != null) {
            String length = (String) lengthComboBox.getSelectedItem();
            try {
                double carLength = Double.parseDouble(length) * 12 / Setup.getScaleRatio();
                double carWeight = (Setup.getInitalWeight() + carLength * Setup.getAddWeight()) / 1000;
                NumberFormat nf = NumberFormat.getNumberInstance();
                nf.setMaximumFractionDigits(1);
                weightTextField.setText((nf.format(carWeight))); // car weight in ounces.
                int tons = (int) (carWeight * Setup.getScaleTonRatio());
                // adjust weight for caboose
                if (cabooseCheckBox.isSelected() || passengerCheckBox.isSelected()) {
                    tons = (int) (Double.parseDouble(length) * .9); // .9 tons/foot
                }
                weightTonsTextField.setText(Integer.toString(tons));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("carLengthMustBe"), Bundle
                        .getMessage("carWeigthCanNot"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveCar(boolean isSave) {
        if (roadComboBox.getSelectedItem() == null) {
            return;
        }
        if (_car == null || !_car.getRoadName().equals(roadComboBox.getSelectedItem())
                || !_car.getNumber().equals(roadNumberTextField.getText())) {
            _car = carManager.newCar((String) roadComboBox.getSelectedItem(), roadNumberTextField.getText());
            _car.addPropertyChangeListener(this);
        }
        if (typeComboBox.getSelectedItem() != null) {
            _car.setTypeName((String) typeComboBox.getSelectedItem());
        }
        if (lengthComboBox.getSelectedItem() != null) {
            _car.setLength((String) lengthComboBox.getSelectedItem());
        }
        if (colorComboBox.getSelectedItem() != null) {
            _car.setColor((String) colorComboBox.getSelectedItem());
        }
        try {
            _car.setWeight(NumberFormat.getNumberInstance().parse(weightTextField.getText()).toString());
        } catch (ParseException e1) {

        }
        _car.setWeightTons(weightTonsTextField.getText());

        // ask if all cars of this type should be passenger
        if (isSave && _car.isPassenger() ^ passengerCheckBox.isSelected()) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(passengerCheckBox.isSelected() ? Bundle
                    .getMessage("carModifyTypePassenger") : Bundle.getMessage("carRemoveTypePassenger"),
                    new Object[]{_car.getTypeName()}), MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                    new Object[]{_car.getTypeName()}), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                // go through the entire list and change the passenger setting
                // for all cars of this type
                for (RollingStock rs : carManager.getList()) {
                    Car c = (Car) rs;
                    if (c.getTypeName().equals(_car.getTypeName())) {
                        c.setPassenger(passengerCheckBox.isSelected());
                    }
                }
            }
        }
        _car.setPassenger(passengerCheckBox.isSelected());
        int blocking = 0;
        try {
            blocking = Integer.parseInt(blockingTextField.getText());
            // only allow numbers between 0 and 100
            if (blocking < 0 || blocking > 100) {
                blocking = 0;
            }
        } catch (Exception e) {
            log.warn("Blocking must be a number between 0 and 100");
        }
        // ask if blocking order should be the same
        if (isSave && _car.getKernel() == null && passengerCheckBox.isSelected() && _car.getBlocking() != blocking) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("carChangeBlocking"),
                    new Object[]{blocking, _car.getTypeName()}), MessageFormat.format(Bundle
                    .getMessage("carModifyAllType"), new Object[]{_car.getTypeName()}), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                // go through the entire list and change the passenger setting
                // for all cars of this type
                for (RollingStock rs : carManager.getList()) {
                    Car c = (Car) rs;
                    if (c.isPassenger() && c.getTypeName().equals(_car.getTypeName())) {
                        c.setBlocking(blocking);
                    }
                }
            }
        }
        _car.setBlocking(blocking);
        // ask if all cars of this type should be caboose
        if (isSave && _car.isCaboose() ^ cabooseCheckBox.isSelected()) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(cabooseCheckBox.isSelected() ? Bundle
                    .getMessage("carModifyTypeCaboose") : Bundle.getMessage("carRemoveTypeCaboose"),
                    new Object[]{_car.getTypeName()}), MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                    new Object[]{_car.getTypeName()}), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                // go through the entire list and change the caboose setting for all cars of this type
                for (RollingStock rs : carManager.getList()) {
                    Car c = (Car) rs;
                    if (c.getTypeName().equals(_car.getTypeName())) {
                        c.setCaboose(cabooseCheckBox.isSelected());
                    }
                }
            }
        }
        _car.setCaboose(cabooseCheckBox.isSelected());
        // ask if all cars of this type should be utility
        if (isSave && _car.isUtility() ^ utilityCheckBox.isSelected()) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(utilityCheckBox.isSelected() ? Bundle
                    .getMessage("carModifyTypeUtility") : Bundle.getMessage("carRemoveTypeUtility"),
                    new Object[]{_car.getTypeName()}), MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                    new Object[]{_car.getTypeName()}), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                // go through the entire list and change the utility for all cars of this type
                for (RollingStock rs : carManager.getList()) {
                    Car c = (Car) rs;
                    if (c.getTypeName().equals(_car.getTypeName())) {
                        c.setUtility(utilityCheckBox.isSelected());
                    }
                }
            }
        }
        _car.setUtility(utilityCheckBox.isSelected());
        // ask if all cars of this type should be hazardous
        if (isSave && _car.isHazardous() ^ hazardousCheckBox.isSelected()) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(hazardousCheckBox.isSelected() ? Bundle
                    .getMessage("carModifyTypeHazardous") : Bundle.getMessage("carRemoveTypeHazardous"),
                    new Object[]{_car.getTypeName()}), MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                    new Object[]{_car.getTypeName()}), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                // go through the entire list and change the hazardous setting for all cars of this type
                for (RollingStock rs : carManager.getList()) {
                    Car c = (Car) rs;
                    if (c.getTypeName().equals(_car.getTypeName())) {
                        c.setHazardous(hazardousCheckBox.isSelected());
                    }
                }
            }
        }
        _car.setHazardous(hazardousCheckBox.isSelected());
        _car.setFred(fredCheckBox.isSelected());
        _car.setBuilt(builtTextField.getText());
        if (ownerComboBox.getSelectedItem() != null) {
            _car.setOwner((String) ownerComboBox.getSelectedItem());
        }
        if (kernelComboBox.getSelectedItem() != null) {
            if (kernelComboBox.getSelectedItem().equals(CarManager.NONE)) {
                _car.setKernel(null);
            } else if (!_car.getKernelName().equals(kernelComboBox.getSelectedItem())) {
                _car.setKernel(carManager.getKernelByName((String) kernelComboBox.getSelectedItem()));
                // if car has FRED or caboose make lead
                if (_car.hasFred() || _car.isCaboose()) {
                    _car.getKernel().setLead(_car);
                }
                _car.setBlocking(_car.getKernel().getSize());
            }
            pBlocking.setVisible(!kernelComboBox.getSelectedItem().equals(CarManager.NONE));
        }
        if (loadComboBox.getSelectedItem() != null && !_car.getLoadName().equals(loadComboBox.getSelectedItem())) {
            _car.setLoadName((String) loadComboBox.getSelectedItem());
            // check to see if car is part of kernel, and ask if all the other cars in the kernel should be changed
            if (_car.getKernel() != null) {
                List<Car> cars = _car.getKernel().getCars();
                if (cars.size() > 1) {
                    if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("carInKernel"),
                            new Object[]{_car.toString()}), MessageFormat.format(Bundle.getMessage("carPartKernel"),
                            new Object[]{_car.getKernelName()}), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        // go through the entire list and change the loads for all cars
                        for (Car car : cars) {
                            if (CarLoads.instance().containsName(car.getTypeName(), _car.getLoadName())) {
                                car.setLoadName(_car.getLoadName());
                            }
                        }
                    }
                }
            }
        }
        _car.setComment(commentTextField.getText());
        _car.setValue(valueTextField.getText());
        _car.setIdTag((IdTag) rfidComboBox.getSelectedItem());
        autoTrackCheckBox.setEnabled(true);

        // update blocking
        blockingTextField.setText(Integer.toString(_car.getBlocking()));

        if (locationBox.getSelectedItem() != null && trackLocationBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("rsFullySelect"), Bundle
                    .getMessage("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);
        } else {
            // update location only if it has changed
            if (_car.getLocation() == null || !_car.getLocation().equals(locationBox.getSelectedItem())
                    || _car.getTrack() == null || !_car.getTrack().equals(trackLocationBox.getSelectedItem())) {
                setLocation(_car);
                // is this car part of a kernel?
                if (_car.getKernel() != null) {
                    List<Car> cars = _car.getKernel().getCars();
                    if (cars.size() > 1) {
                        if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle
                                .getMessage("carInKernel"), new Object[]{_car.toString()}), MessageFormat
                                .format(Bundle.getMessage("carPartKernel"), new Object[]{_car.getKernelName()}),
                                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            // go through the entire list and change the location for all cars
                            for (Car car : cars) {
                                if (car != _car) {
                                    setLocation(car);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void setLocation(Car car) {
        if (locationBox.getSelectedItem() == null) {
            car.setLocation(null, null);
        } else {
            car.setSavedRouteId(RollingStock.NONE); // clear last route id
            String status = car.setLocation((Location) locationBox.getSelectedItem(), (Track) trackLocationBox
                    .getSelectedItem());
            if (!status.equals(Track.OKAY)) {
                log.debug("Can't set car's location because of {}", status);
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("rsCanNotLocMsg"),
                        new Object[]{car.toString(), status}), Bundle.getMessage("rsCanNotLoc"),
                        JOptionPane.ERROR_MESSAGE);
                // does the user want to force the rolling stock to this track?
                int results = JOptionPane.showOptionDialog(this, MessageFormat.format(Bundle.getMessage("rsForce"),
                        new Object[]{car.toString(), (Track) trackLocationBox.getSelectedItem()}), MessageFormat
                        .format(Bundle.getMessage("rsOverride"), new Object[]{status}), JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (results == JOptionPane.YES_OPTION) {
                    log.debug("Force rolling stock to track");
                    car.setLocation((Location) locationBox.getSelectedItem(), (Track) trackLocationBox
                            .getSelectedItem(), true);
                }
            }
        }
    }

    private void addEditButtonAction(JButton b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonEditActionPerformed(e);
            }
        });
    }

    private boolean editActive = false;
    CarAttributeEditFrame f;

    // edit buttons only one frame active at a time
    public void buttonEditActionPerformed(java.awt.event.ActionEvent ae) {
        if (editActive) {
            f.dispose();
        }
        f = new CarAttributeEditFrame();
        f.setLocationRelativeTo(this);
        f.addPropertyChangeListener(this);
        editActive = true;

        if (ae.getSource() == editRoadButton) {
            f.initComponents(ROAD, (String) roadComboBox.getSelectedItem());
        }
        if (ae.getSource() == editTypeButton) {
            f.initComponents(TYPE, (String) typeComboBox.getSelectedItem());
        }
        if (ae.getSource() == editColorButton) {
            f.initComponents(COLOR, (String) colorComboBox.getSelectedItem());
        }
        if (ae.getSource() == editLengthButton) {
            f.initComponents(LENGTH, (String) lengthComboBox.getSelectedItem());
        }
        if (ae.getSource() == editOwnerButton) {
            f.initComponents(OWNER, (String) ownerComboBox.getSelectedItem());
        }
        if (ae.getSource() == editKernelButton) {
            f.initComponents(KERNEL, (String) kernelComboBox.getSelectedItem());
        }
    }

    public void dispose() {
        removePropertyChangeListeners();
        super.dispose();
    }

    private void removePropertyChangeListeners() {
        CarRoads.instance().removePropertyChangeListener(this);
        CarLoads.instance().removePropertyChangeListener(this);
        CarTypes.instance().removePropertyChangeListener(this);
        CarLengths.instance().removePropertyChangeListener(this);
        CarColors.instance().removePropertyChangeListener(this);
        CarOwners.instance().removePropertyChangeListener(this);
        locationManager.removePropertyChangeListener(this);
        carManager.removePropertyChangeListener(this);
        if (_car != null) {
            _car.removePropertyChangeListener(this);
        }
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarRoads.CARROADS_CHANGED_PROPERTY)) {
            CarRoads.instance().updateComboBox(roadComboBox);
            if (_car != null) {
                roadComboBox.setSelectedItem(_car.getRoadName());
            }
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY)) {
            CarTypes.instance().updateComboBox(typeComboBox);
            if (_car != null) {
                typeComboBox.setSelectedItem(_car.getTypeName());
            }
        }
        if (e.getPropertyName().equals(CarColors.CARCOLORS_CHANGED_PROPERTY)) {
            CarColors.instance().updateComboBox(colorComboBox);
            if (_car != null) {
                colorComboBox.setSelectedItem(_car.getColor());
            }
        }
        if (e.getPropertyName().equals(CarLengths.CARLENGTHS_CHANGED_PROPERTY)) {
            CarLengths.instance().updateComboBox(lengthComboBox);
            if (_car != null) {
                lengthComboBox.setSelectedItem(_car.getLength());
            }
        }
        if (e.getPropertyName().equals(CarManager.KERNEL_LISTLENGTH_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Car.KERNEL_NAME_CHANGED_PROPERTY)) {
            carManager.updateKernelComboBox(kernelComboBox);
            if (_car != null) {
                kernelComboBox.setSelectedItem(_car.getKernelName());
            }
        }
        if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY)) {
            CarOwners.instance().updateComboBox(ownerComboBox);
            if (_car != null) {
                ownerComboBox.setSelectedItem(_car.getOwner());
            }
        }
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)
                || e.getPropertyName().equals(RollingStock.TRACK_CHANGED_PROPERTY)) {
            LocationManager.instance().updateComboBox(locationBox);
            updateTrackLocationBox();
            if (_car != null && _car.getLocation() != null) {
                locationBox.setSelectedItem(_car.getLocation());
            }
        }
        if (e.getPropertyName().equals(Car.LOAD_CHANGED_PROPERTY)) {
            if (_car != null) {
                loadComboBox.setSelectedItem(_car.getLoadName());
            }
        }
        if (e.getPropertyName().equals(CarLoads.LOAD_CHANGED_PROPERTY)) {
            if (_car != null) {
                CarLoads.instance().updateComboBox((String) typeComboBox.getSelectedItem(), loadComboBox);
                loadComboBox.setSelectedItem(_car.getLoadName());
            }
        }
        if (e.getPropertyName().equals(CarAttributeEditFrame.DISPOSE)) {
            editActive = false;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CarEditFrame.class.getName());
}
