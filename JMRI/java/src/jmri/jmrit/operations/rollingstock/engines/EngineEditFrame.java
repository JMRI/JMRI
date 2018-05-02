package jmri.jmrit.operations.rollingstock.engines;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import jmri.IdTag;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of engine
 *
 * @author Dan Boudreau Copyright (C) 2008, 2011
 */
public class EngineEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    EngineManager manager = InstanceManager.getDefault(EngineManager.class);
    EngineManagerXml managerXml = InstanceManager.getDefault(EngineManagerXml.class);
    EngineModels engineModels = InstanceManager.getDefault(EngineModels.class);
    EngineTypes engineTypes = InstanceManager.getDefault(EngineTypes.class);
    EngineLengths engineLengths = InstanceManager.getDefault(EngineLengths.class);
    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    Engine _engine;

    // major buttons
    JButton editRoadButton = new JButton(Bundle.getMessage("ButtonEdit"));
    JButton clearRoadNumberButton = new JButton(Bundle.getMessage("ButtonClear"));
    JButton editModelButton = new JButton(Bundle.getMessage("ButtonEdit"));
    JButton editTypeButton = new JButton(Bundle.getMessage("ButtonEdit"));
    JButton editLengthButton = new JButton(Bundle.getMessage("ButtonEdit"));
    JButton fillWeightButton = new JButton();
    JButton editConsistButton = new JButton(Bundle.getMessage("ButtonEdit"));
    JButton editOwnerButton = new JButton(Bundle.getMessage("ButtonEdit"));

    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
    JButton addButton = new JButton(Bundle.getMessage("TitleEngineAdd")); // have button state item to add

    // check boxes
    JCheckBox bUnitCheckBox = new JCheckBox(Bundle.getMessage("BUnit"));

    // text field
    JTextField roadNumberTextField = new JTextField(Control.max_len_string_road_number);
    JTextField builtTextField = new JTextField(Control.max_len_string_built_name + 3);
    JTextField hpTextField = new JTextField(8);
    JTextField weightTextField = new JTextField(Control.max_len_string_weight_name);
    JTextField commentTextField = new JTextField(35);
    JTextField valueTextField = new JTextField(8);

    // combo boxes
    JComboBox<String> roadComboBox = InstanceManager.getDefault(CarRoads.class).getComboBox();
    JComboBox<String> modelComboBox = engineModels.getComboBox();
    JComboBox<String> typeComboBox = engineTypes.getComboBox();
    JComboBox<String> lengthComboBox = engineLengths.getComboBox();
    JComboBox<String> ownerComboBox = InstanceManager.getDefault(CarOwners.class).getComboBox();
    JComboBox<Location> locationBox = locationManager.getComboBox();
    JComboBox<Track> trackLocationBox = new JComboBox<>();
    JComboBox<String> consistComboBox = manager.getConsistComboBox();
    JComboBox<IdTag> rfidComboBox = new JComboBox<IdTag>();

    public static final String ROAD = Bundle.getMessage("Road");
    public static final String MODEL = Bundle.getMessage("Model");
    public static final String TYPE = Bundle.getMessage("Type");
    public static final String COLOR = Bundle.getMessage("Color");
    public static final String LENGTH = Bundle.getMessage("Length");
    public static final String OWNER = Bundle.getMessage("Owner");
    public static final String CONSIST = Bundle.getMessage("Consist");

    public EngineEditFrame() {
        super(Bundle.getMessage("TitleEngineAdd")); // default is add engine
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Checks for null")
    @Override
    public void initComponents() {
        // set tips
        builtTextField.setToolTipText(Bundle.getMessage("buildDateTip"));
        rfidComboBox.setToolTipText(Bundle.getMessage("TipRfid"));
        editRoadButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Road").toLowerCase()}));
        editTypeButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Type").toLowerCase()}));
        editModelButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Model").toLowerCase()}));
        editLengthButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Length").toLowerCase()}));
        editOwnerButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Owner").toLowerCase()}));
        editConsistButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Consist").toLowerCase()}));
        bUnitCheckBox.setToolTipText(Bundle.getMessage("TipBoosterUnit"));

        deleteButton.setToolTipText(Bundle.getMessage("TipDeleteButton"));
        addButton.setToolTipText(Bundle.getMessage("TipAddButton"));
        saveButton.setToolTipText(Bundle.getMessage("TipSaveButton"));

        // disable delete and save buttons
        deleteButton.setEnabled(false);
        saveButton.setEnabled(false);

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
        JPanel pModel = new JPanel();
        pModel.setLayout(new GridBagLayout());
        pModel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Model")));
        addItem(pModel, modelComboBox, 1, 0);
        addItem(pModel, editModelButton, 2, 0);
        pPanel.add(pModel);

        // row 4
        JPanel pType = new JPanel();
        pType.setLayout(new GridBagLayout());
        pType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
        addItem(pType, typeComboBox, 1, 0);
        addItem(pType, editTypeButton, 2, 0);
        addItem(pType, bUnitCheckBox, 1, 1);
        pPanel.add(pType);

        // row 5
        JPanel pLength = new JPanel();
        pLength.setLayout(new GridBagLayout());
        pLength.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Length")));
        addItem(pLength, lengthComboBox, 1, 0);
        addItem(pLength, editLengthButton, 2, 0);
        pPanel.add(pLength);

        // row 6
        JPanel pLocation = new JPanel();
        pLocation.setLayout(new GridBagLayout());
        pLocation.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LocationAndTrack")));
        addItem(pLocation, locationBox, 1, 0);
        addItem(pLocation, trackLocationBox, 2, 0);
        pPanel.add(pLocation);

        // optional panel
        JPanel pOptional = new JPanel();
        pOptional.setLayout(new BoxLayout(pOptional, BoxLayout.Y_AXIS));
        JScrollPane optionPane = new JScrollPane(pOptional);
        optionPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOptional")));

        // row 11
        JPanel pWeightTons = new JPanel();
        pWeightTons.setLayout(new GridBagLayout());
        pWeightTons.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("WeightTons")));
        addItem(pWeightTons, weightTextField, 0, 0);
        pOptional.add(pWeightTons);

        // row 12
        JPanel pHp = new JPanel();
        pHp.setLayout(new GridBagLayout());
        pHp.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Hp")));
        addItem(pHp, hpTextField, 0, 0);
        pOptional.add(pHp);

        // row 13
        JPanel pConsist = new JPanel();
        pConsist.setLayout(new GridBagLayout());
        pConsist.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Consist")));
        addItem(pConsist, consistComboBox, 1, 0);
        addItem(pConsist, editConsistButton, 2, 0);
        pOptional.add(pConsist);

        // row 14
        JPanel pBuilt = new JPanel();
        pBuilt.setLayout(new GridBagLayout());
        pBuilt.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Built")));
        addItem(pBuilt, builtTextField, 1, 0);
        pOptional.add(pBuilt);

        // row 15
        JPanel pOwner = new JPanel();
        pOwner.setLayout(new GridBagLayout());
        pOwner.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Owner")));
        addItem(pOwner, ownerComboBox, 1, 0);
        addItem(pOwner, editOwnerButton, 2, 0);
        pOptional.add(pOwner);

        // row 18
        if (Setup.isValueEnabled()) {
            JPanel pValue = new JPanel();
            pValue.setLayout(new GridBagLayout());
            pValue.setBorder(BorderFactory.createTitledBorder(Setup.getValueLabel()));
            addItem(pValue, valueTextField, 1, 0);
            pOptional.add(pValue);
        }

        // row 20
        if (Setup.isRfidEnabled() && jmri.InstanceManager.getNullableDefault(jmri.IdTagManager.class) != null) {
            JPanel pRfid = new JPanel();
            pRfid.setLayout(new GridBagLayout());
            pRfid.setBorder(BorderFactory.createTitledBorder(Setup.getRfidLabel()));
            addItem(pRfid, rfidComboBox, 1, 0);
            jmri.InstanceManager.getDefault(jmri.IdTagManager.class).getNamedBeanList()
                    .forEach((tag) -> rfidComboBox.addItem(tag));
            rfidComboBox.insertItemAt((jmri.IdTag)null,0); // must have a blank in the list, for the default.
            rfidComboBox.setSelectedIndex(0);
            pOptional.add(pRfid);
        }

        // row 22
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
        addEditButtonAction(editModelButton);
        addEditButtonAction(editTypeButton);
        addEditButtonAction(editLengthButton);
        addEditButtonAction(editConsistButton);
        addEditButtonAction(editOwnerButton);

        addButtonAction(deleteButton);
        addButtonAction(addButton);
        addButtonAction(saveButton);
        addButtonAction(fillWeightButton);

        // setup combobox
        addComboBoxAction(modelComboBox);
        addComboBoxAction(locationBox);

        addHelpMenu("package.jmri.jmrit.operations.Operations_LocomotivesAdd", true); // NOI18N

        // get notified if combo box gets modified
        InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);
        engineModels.addPropertyChangeListener(this);
        engineTypes.addPropertyChangeListener(this);
        engineLengths.addPropertyChangeListener(this);
        InstanceManager.getDefault(CarOwners.class).addPropertyChangeListener(this);
        locationManager.addPropertyChangeListener(this);
        manager.addPropertyChangeListener(this);

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
    }

    public void loadEngine(Engine engine) {
        _engine = engine;

        // enable delete and save buttons
        deleteButton.setEnabled(true);
        saveButton.setEnabled(true);

        if (!InstanceManager.getDefault(CarRoads.class).containsName(engine.getRoadName())) {
            String msg = MessageFormat.format(Bundle.getMessage("roadNameNotExist"), new Object[]{engine
                    .getRoadName()});
            if (JOptionPane.showConfirmDialog(this, msg, Bundle.getMessage("rsAddRoad"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                InstanceManager.getDefault(CarRoads.class).addName(engine.getRoadName());
            }
        }
        roadComboBox.setSelectedItem(engine.getRoadName());

        roadNumberTextField.setText(engine.getNumber());

        if (!engineModels.containsName(engine.getModel())) {
            String msg = MessageFormat.format(Bundle.getMessage("modelNameNotExist"),
                    new Object[]{engine.getModel()});
            if (JOptionPane
                    .showConfirmDialog(this, msg, Bundle.getMessage("engineAddModel"),
                            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                engineModels.addName(engine.getModel());
            }
        }
        modelComboBox.setSelectedItem(engine.getModel());

        if (!engineTypes.containsName(engine.getTypeName())) {
            String msg = MessageFormat.format(Bundle.getMessage("typeNameNotExist"), new Object[]{engine
                    .getTypeName()});
            if (JOptionPane.showConfirmDialog(this, msg, Bundle.getMessage("engineAddType"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                engineTypes.addName(engine.getTypeName());
            }
        }
        typeComboBox.setSelectedItem(engine.getTypeName());
        bUnitCheckBox.setSelected(engine.isBunit());

        if (!engineLengths.containsName(engine.getLength())) {
            String msg = MessageFormat.format(Bundle.getMessage("lengthNameNotExist"), new Object[]{engine
                    .getLength()});
            if (JOptionPane.showConfirmDialog(this, msg, Bundle.getMessage("engineAddLength"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                engineLengths.addName(engine.getLength());
            }
        }
        lengthComboBox.setSelectedItem(engine.getLength());
        weightTextField.setText(engine.getWeightTons());
        hpTextField.setText(engine.getHp());

        locationBox.setSelectedItem(engine.getLocation());
        Location l = locationManager.getLocationById(engine.getLocationId());
        if (l != null) {
            l.updateComboBox(trackLocationBox);
            trackLocationBox.setSelectedItem(engine.getTrack());
        } else {
            trackLocationBox.removeAllItems();
        }

        builtTextField.setText(engine.getBuilt());

        if (!InstanceManager.getDefault(CarOwners.class).containsName(engine.getOwner())) {
            String msg = MessageFormat.format(Bundle.getMessage("ownerNameNotExist"),
                    new Object[]{engine.getOwner()});
            if (JOptionPane.showConfirmDialog(this, msg, Bundle.getMessage("addOwner"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                InstanceManager.getDefault(CarOwners.class).addName(engine.getOwner());
            }
        }
        consistComboBox.setSelectedItem(engine.getConsistName());

        ownerComboBox.setSelectedItem(engine.getOwner());
        valueTextField.setText(engine.getValue());
        rfidComboBox.setSelectedItem(engine.getIdTag());
        commentTextField.setText(engine.getComment());

        setTitle(Bundle.getMessage("TitleEngineEdit"));
    }

    // combo boxes
    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == modelComboBox) {
            if (modelComboBox.getSelectedItem() != null) {
                String model = (String) modelComboBox.getSelectedItem();
                // load the default hp and length for the model selected
                hpTextField.setText(engineModels.getModelHorsepower(model));
                weightTextField.setText(engineModels.getModelWeight(model));
                if (engineModels.getModelLength(model) != null && !engineModels.getModelLength(model).equals("")) {
                    lengthComboBox.setSelectedItem(engineModels.getModelLength(model));
                }
                if (engineModels.getModelType(model) != null && !engineModels.getModelType(model).equals("")) {
                    typeComboBox.setSelectedItem(engineModels.getModelType(model));
                }
            }
        }
        if (ae.getSource() == locationBox) {
            if (locationBox.getSelectedItem() == null) {
                trackLocationBox.removeAllItems();
            } else {
                log.debug("EnginesSetFrame sees location: " + locationBox.getSelectedItem());
                Location l = ((Location) locationBox.getSelectedItem());
                l.updateComboBox(trackLocationBox);
            }
        }
    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        JCheckBox b = (JCheckBox) ae.getSource();
        log.debug("checkbox change " + b.getText());
    }

    // Save, Delete, Add, Clear, Calculate buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            // log.debug("engine save button activated");
            String roadNum = roadNumberTextField.getText();
            if (!checkRoadNumber(roadNum)) {
                return;
            }
            // check to see if engine with road and number already exists
            Engine engine = manager.getByRoadAndNumber((String) roadComboBox.getSelectedItem(), roadNumberTextField
                    .getText());
            if (engine != null) {
                if (_engine == null || !engine.getId().equals(_engine.getId())) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("engineExists"), Bundle
                            .getMessage("engineCanNotUpdate"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // if the road or number changes, the loco needs a new id
            if (_engine != null &&
                    _engine.getRoadName() != null &&
                    !_engine.getRoadName().equals(Engine.NONE) &&
                    (!_engine.getRoadName().equals(roadComboBox.getSelectedItem()) || !_engine
                            .getNumber().equals(roadNumberTextField.getText()))) {
                String road = (String) roadComboBox.getSelectedItem();
                String number = roadNumberTextField.getText();
                manager.changeId(_engine, road, number);
                _engine.setRoadName(road);
                _engine.setNumber(number);
            }
            saveEngine();
            OperationsXml.save(); // save engine file
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == deleteButton) {
            log.debug("engine delete button activated");
            if (_engine != null &&
                    _engine.getRoadName().equals(roadComboBox.getSelectedItem()) &&
                    _engine.getNumber().equals(roadNumberTextField.getText())) {
                manager.deregister(_engine);
                _engine = null;
                OperationsXml.save(); // save engine file
            } else {
                Engine e = manager.getByRoadAndNumber((String) roadComboBox.getSelectedItem(), roadNumberTextField
                        .getText());
                if (e != null) {
                    manager.deregister(e);
                    OperationsXml.save(); // save engine file
                }
            }
        }
        if (ae.getSource() == addButton) {
            if (!checkRoadNumber(roadNumberTextField.getText())) {
                return;
            }

            // check to see if engine already exists
            Engine existingEngine =
                    manager.getByRoadAndNumber((String) roadComboBox.getSelectedItem(), roadNumberTextField
                            .getText());
            if (existingEngine != null) {
                log.info("Can not add, engine already exists");
                JOptionPane.showMessageDialog(this, Bundle.getMessage("engineExists"), Bundle
                        .getMessage("engineCanNotUpdate"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            // enable delete and save buttons
            deleteButton.setEnabled(true);
            saveButton.setEnabled(true);

            saveEngine();
            OperationsXml.save(); // save engine file
        }
        if (ae.getSource() == clearRoadNumberButton) {
            roadNumberTextField.setText("");
            roadNumberTextField.requestFocus();
        }
    }

    private boolean checkRoadNumber(String roadNum) {
        if (!OperationsXml.checkFileName(roadNum)) { // NOI18N
            log.error("Road number must not contain reserved characters");
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NameResChar") + NEW_LINE + Bundle.getMessage("ReservedChar"),
                    Bundle.getMessage("roadNumNG"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (roadNum.length() > Control.max_len_string_road_number) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("engineRoadNum"), Bundle
                    .getMessage("engineRoadLong"), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void saveEngine() {
        if (roadComboBox.getSelectedItem() != null && !roadComboBox.getSelectedItem().equals("")) {
            if (_engine == null ||
                    !_engine.getRoadName().equals(roadComboBox.getSelectedItem()) ||
                    !_engine.getNumber().equals(roadNumberTextField.getText())) {
                _engine = manager.newEngine((String) roadComboBox.getSelectedItem(), roadNumberTextField.getText());
            }
            if (modelComboBox.getSelectedItem() != null) {
                _engine.setModel((String) modelComboBox.getSelectedItem());
            }
            if (typeComboBox.getSelectedItem() != null) {
                _engine.setTypeName((String) typeComboBox.getSelectedItem());
            }
            _engine.setBunit(bUnitCheckBox.isSelected());
            if (lengthComboBox.getSelectedItem() != null) {
                _engine.setLength((String) lengthComboBox.getSelectedItem());
            }
            _engine.setBuilt(builtTextField.getText());
            if (ownerComboBox.getSelectedItem() != null) {
                _engine.setOwner((String) ownerComboBox.getSelectedItem());
            }
            if (consistComboBox.getSelectedItem() != null) {
                if (consistComboBox.getSelectedItem().equals(EngineManager.NONE)) {
                    _engine.setConsist(null);
                    if (_engine.isBunit())
                        _engine.setBlocking(Engine.B_UNIT_BLOCKING);
                    else
                        _engine.setBlocking(Engine.DEFAULT_BLOCKING_ORDER);
                } else {
                    _engine.setConsist(manager.getConsistByName((String) consistComboBox.getSelectedItem()));
                    if (_engine.getConsist() != null) {
                        _engine.setBlocking(_engine.getConsist().getSize());
                    }
                }
            }
            // confirm that weight is a number
            if (!weightTextField.getText().equals("")) {
                try {
                    Integer.parseInt(weightTextField.getText());
                    _engine.setWeightTons(weightTextField.getText());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("engineWeight"), Bundle
                            .getMessage("engineCanNotWeight"), JOptionPane.ERROR_MESSAGE);
                }
            }
            // confirm that horsepower is a number
            if (!hpTextField.getText().equals("")) {
                try {
                    Integer.parseInt(hpTextField.getText());
                    _engine.setHp(hpTextField.getText());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("engineHorsepower"), Bundle
                            .getMessage("engineCanNotHp"), JOptionPane.ERROR_MESSAGE);
                }
            }
            if (locationBox.getSelectedItem() == null) {
                _engine.setLocation(null, null);
            } else {
                if (trackLocationBox.getSelectedItem() == null) {
                    JOptionPane.showMessageDialog(this, Bundle.getMessage("rsFullySelect"), Bundle
                            .getMessage("rsCanNotLoc"), JOptionPane.ERROR_MESSAGE);

                } else {
                    String status = _engine.setLocation((Location) locationBox.getSelectedItem(),
                            (Track) trackLocationBox.getSelectedItem());
                    if (!status.equals(Track.OKAY)) {
                        log.debug("Can't set engine's location because of {}", status);
                        JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle
                                .getMessage("rsCanNotLocMsg"), new Object[]{_engine.toString(), status}), Bundle
                                        .getMessage("rsCanNotLoc"),
                                JOptionPane.ERROR_MESSAGE);
                        // does the user want to force the rolling stock to this track?
                        int results = JOptionPane.showOptionDialog(this, MessageFormat.format(Bundle
                                .getMessage("rsForce"), new Object[]{_engine.toString(),
                                        (Track) trackLocationBox.getSelectedItem()}),
                                MessageFormat.format(Bundle
                                        .getMessage("rsOverride"), new Object[]{status}),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.QUESTION_MESSAGE, null, null, null);
                        if (results == JOptionPane.YES_OPTION) {
                            log.debug("Force rolling stock to track");
                            _engine.setLocation((Location) locationBox.getSelectedItem(), (Track) trackLocationBox
                                    .getSelectedItem(), RollingStock.FORCE);
                        }
                    }
                }
            }
            _engine.setComment(commentTextField.getText());
            _engine.setValue(valueTextField.getText());
            // save the IdTag for this engine
            IdTag idTag = (IdTag) rfidComboBox.getSelectedItem();
            if (idTag != null) {
                _engine.setRfid(idTag.toString());
            }
        }
    }

    // for the engineAttributeEditFrame edit buttons
    private void addEditButtonAction(JButton b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonEditActionPerformed(e);
            }
        });
    }

    EngineAttributeEditFrame engineAttributeEditFrame;

    // edit buttons only one frame active at a time
    public void buttonEditActionPerformed(java.awt.event.ActionEvent ae) {
        if (engineAttributeEditFrame != null) {
            engineAttributeEditFrame.dispose();
        }
        engineAttributeEditFrame = new EngineAttributeEditFrame();
        engineAttributeEditFrame.setLocationRelativeTo(this);
        engineAttributeEditFrame.addPropertyChangeListener(this);

        if (ae.getSource() == editRoadButton) {
            engineAttributeEditFrame.initComponents(ROAD, (String) roadComboBox.getSelectedItem());
        }
        if (ae.getSource() == editModelButton) {
            engineAttributeEditFrame.initComponents(MODEL, (String) modelComboBox.getSelectedItem());
        }
        if (ae.getSource() == editTypeButton) {
            engineAttributeEditFrame.initComponents(TYPE, (String) typeComboBox.getSelectedItem());
        }
        if (ae.getSource() == editLengthButton) {
            engineAttributeEditFrame.initComponents(LENGTH, (String) lengthComboBox.getSelectedItem());
        }
        if (ae.getSource() == editOwnerButton) {
            engineAttributeEditFrame.initComponents(OWNER, (String) ownerComboBox.getSelectedItem());
        }
        if (ae.getSource() == editConsistButton) {
            engineAttributeEditFrame.initComponents(CONSIST, (String) consistComboBox.getSelectedItem());
        }
    }

    @Override
    public void dispose() {
        removePropertyChangeListeners();
        super.dispose();
    }

    private void removePropertyChangeListeners() {
        InstanceManager.getDefault(CarRoads.class).removePropertyChangeListener(this);
        engineModels.removePropertyChangeListener(this);
        engineTypes.removePropertyChangeListener(this);
        engineLengths.removePropertyChangeListener(this);
        InstanceManager.getDefault(CarOwners.class).removePropertyChangeListener(this);
        locationManager.removePropertyChangeListener(this);
        manager.removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarRoads.CARROADS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(CarRoads.class).updateComboBox(roadComboBox);
            if (_engine != null) {
                roadComboBox.setSelectedItem(_engine.getRoadName());
            }
        }
        if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_CHANGED_PROPERTY)) {
            engineModels.updateComboBox(modelComboBox);
            if (_engine != null) {
                modelComboBox.setSelectedItem(_engine.getModel());
            }
        }
        if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_CHANGED_PROPERTY)) {
            engineTypes.updateComboBox(typeComboBox);
            if (_engine != null) {
                typeComboBox.setSelectedItem(_engine.getTypeName());
            }
        }
        if (e.getPropertyName().equals(EngineLengths.ENGINELENGTHS_CHANGED_PROPERTY)) {
            engineLengths.updateComboBox(lengthComboBox);
            if (_engine != null) {
                lengthComboBox.setSelectedItem(_engine.getLength());
            }
        }
        if (e.getPropertyName().equals(EngineManager.CONSISTLISTLENGTH_CHANGED_PROPERTY)) {
            manager.updateConsistComboBox(consistComboBox);
            if (_engine != null) {
                consistComboBox.setSelectedItem(_engine.getConsistName());
            }
        }
        if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(CarOwners.class).updateComboBox(ownerComboBox);
            if (_engine != null) {
                ownerComboBox.setSelectedItem(_engine.getOwner());
            }
        }
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(LocationManager.class).updateComboBox(locationBox);
            if (_engine != null) {
                locationBox.setSelectedItem(_engine.getLocation());
            }
        }
        if (e.getPropertyName().equals(EngineAttributeEditFrame.DISPOSE)) {
            engineAttributeEditFrame = null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(EngineEditFrame.class);
}
