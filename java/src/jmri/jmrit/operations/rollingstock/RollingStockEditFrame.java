package jmri.jmrit.operations.rollingstock;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarEditFrame;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.swing.NamedBeanComboBox;
/**
 * Frame for edit of rolling stock. The common elements are: road, road number,
 * type, blocking, length, location and track, groups (Kernel or Consist)
 * weight, color, built, owner, comment.
 * 
 * The edit engine frame currently doesn't show blocking or color.
 * 
 * Engines and cars have different type, length, and group managers.
 *
 * @author Dan Boudreau Copyright (C) 2018
 */
public abstract class RollingStockEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    protected static final boolean IS_SAVE = true;

    protected RollingStock _rs;

    protected LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    JLabel textWeightTons = new JLabel(Bundle.getMessage("WeightTons"));

    // major buttons
    public JButton editRoadButton = new JButton(Bundle.getMessage("ButtonEdit"));
    public JButton clearRoadNumberButton = new JButton(Bundle.getMessage("ButtonClear"));
    public JButton editTypeButton = new JButton(Bundle.getMessage("ButtonEdit"));
    public JButton editLengthButton = new JButton(Bundle.getMessage("ButtonEdit"));
    public JButton editGroupButton = new JButton(Bundle.getMessage("ButtonEdit"));
    public JButton editOwnerButton = new JButton(Bundle.getMessage("ButtonEdit"));

    public JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));
    public JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete"));
    public JButton addButton = new JButton(Bundle.getMessage("ButtonAdd")); // TODO have button state item to add

    // check boxes
    public JCheckBox autoTrackCheckBox = new JCheckBox(Bundle.getMessage("Auto"));

    // text field
    public JTextField roadNumberTextField = new JTextField(Control.max_len_string_road_number);
    public JTextField builtTextField = new JTextField(Control.max_len_string_built_name + 3);
    public JTextField blockingTextField = new JTextField(4);
    public JTextField weightTextField = new JTextField(Control.max_len_string_weight_name);
    public JTextField weightTonsTextField = new JTextField(Control.max_len_string_weight_name);
    public JTextField commentTextField = new JTextField(35);
    
    // text area
    public JTextArea valueTextArea = new JTextArea(3, 35);
    JScrollPane valueScroller = new JScrollPane(valueTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // combo boxes
    public JComboBox<String> roadComboBox = InstanceManager.getDefault(CarRoads.class).getComboBox();
    public JComboBox<String> typeComboBox = getTypeManager().getComboBox();
    public JComboBox<String> lengthComboBox = getLengthManager().getComboBox();
    public JComboBox<String> ownerComboBox = InstanceManager.getDefault(CarOwners.class).getComboBox();
    public JComboBox<String> groupComboBox;
    public JComboBox<String> modelComboBox; // for engines
    public JComboBox<Location> locationBox = locationManager.getComboBox();
    public JComboBox<Track> trackLocationBox = new JComboBox<>();

    public NamedBeanComboBox<IdTag> rfidComboBox;

    // panels
    public JPanel pTypeOptions = new JPanel(); // options dependent on car or engine
    public JPanel pGroup = new JPanel(); // Kernel or Consist

    // panels for car edit
    public JPanel pBlocking = new JPanel();
    public JPanel pColor = new JPanel();
    public JPanel pLoad = new JPanel();
    public JPanel pWeightOz = new JPanel();

    // panels for engine edit
    public JPanel pModel = new JPanel();
    public JPanel pHp = new JPanel();

    public RollingStockEditFrame(String title) {
        super(title);
        //instanceManager = InstanceManger.getInstance();
    }

    abstract protected RollingStockAttribute getTypeManager();

    abstract protected RollingStockAttribute getLengthManager();

    abstract protected void buttonEditActionPerformed(java.awt.event.ActionEvent ae);

    abstract protected ResourceBundle getRb();

    abstract protected void save(boolean isSave);
    
    abstract protected void delete();

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Checks for null")
    @Override
    public void initComponents() {

        // disable delete and save buttons
        deleteButton.setEnabled(false);
        saveButton.setEnabled(false);

        editRoadButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("road")})); // initial caps for some languages i.e. German
        editTypeButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("type")})); // initial caps for some languages i.e. German
        editLengthButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("length")})); // initial caps for some languages i.e. German
        editOwnerButton.setToolTipText(MessageFormat.format(Bundle.getMessage("TipAddDeleteReplace"),
                new Object[]{Bundle.getMessage("Owner").toLowerCase()}));
        
        autoTrackCheckBox.setToolTipText(getRb().getString("rsTipAutoTrack"));

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

        // only engines have a model name
        pPanel.add(pModel);
        pModel.setVisible(false);

        // row 3
        JPanel pType = new JPanel();
        pType.setLayout(new GridBagLayout());
        pType.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Type")));
        addItem(pType, typeComboBox, 0, 0);
        addItem(pType, editTypeButton, 1, 0);

        // type options dependent on car or engine rolling stock
        addItemWidth(pType, pTypeOptions, 3, 0, 1);
        pPanel.add(pType);
        
        // row 4
        pBlocking.setLayout(new GridBagLayout());
        pBlocking.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutBlockingOrder")));
        addItem(pBlocking, blockingTextField, 0, 0);
        blockingTextField.setText("0");
        pPanel.add(pBlocking);
        pBlocking.setVisible(false); // default is blocking order not shown

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
        addItem(pLocation, autoTrackCheckBox, 3, 0);
        pPanel.add(pLocation);

        // optional panel
        JPanel pOptional = new JPanel();
        pOptional.setLayout(new BoxLayout(pOptional, BoxLayout.Y_AXIS));
        JScrollPane optionPane = new JScrollPane(pOptional);
        optionPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOptional")));

        // row 7
        JPanel pWeight = new JPanel();
        pWeight.setLayout(new BoxLayout(pWeight, BoxLayout.Y_AXIS));
        pWeight.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Weight")));

        // weight in oz only shown for cars
        pWeight.add(pWeightOz);

        JPanel pWeightTons = new JPanel();
        pWeightTons.setLayout(new GridBagLayout());
        addItem(pWeightTons, textWeightTons, 0, 0);
        addItem(pWeightTons, weightTonsTextField, 1, 0);
        addItem(pWeightTons, new JLabel(), 2, 0);
        addItem(pWeightTons, new JLabel(), 3, 0);
        addItem(pWeightTons, new JLabel(), 4, 0);
        pWeight.add(pWeightTons);
        pOptional.add(pWeight);

        // row 8 for cars
        pOptional.add(pColor);
        pColor.setVisible(false);

        // row 9 for cars
        pOptional.add(pLoad);
        pLoad.setVisible(false);

        // for engines
        pOptional.add(pHp);
        pHp.setVisible(false);

        // row 10 
        pGroup.setLayout(new GridBagLayout());
        addItem(pGroup, groupComboBox, 1, 0);
        addItem(pGroup, editGroupButton, 2, 0);
        pOptional.add(pGroup);

        // row 11
        JPanel pBuilt = new JPanel();
        pBuilt.setLayout(new GridBagLayout());
        pBuilt.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Built")));
        addItem(pBuilt, builtTextField, 1, 0);
        pOptional.add(pBuilt);

        // row 12
        JPanel pOwner = new JPanel();
        pOwner.setLayout(new GridBagLayout());
        pOwner.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Owner")));
        addItem(pOwner, ownerComboBox, 1, 0);
        addItem(pOwner, editOwnerButton, 2, 0);
        pOptional.add(pOwner);

        // row 13
        if (Setup.isValueEnabled()) {
            JPanel pValue = new JPanel();
            pValue.setLayout(new GridBagLayout());
            pValue.setBorder(BorderFactory.createTitledBorder(Setup.getValueLabel()));
            addItem(pValue, valueScroller, 1, 0);
            pOptional.add(pValue);
            
            // adjust text area width based on window size
            adjustTextAreaColumnWidth(valueScroller, valueTextArea);
        }

        // row 14
        IdTagManager tagManager = InstanceManager.getNullableDefault(IdTagManager.class);
        if (Setup.isRfidEnabled() && tagManager != null) {
            JPanel pRfid = new JPanel();
            pRfid.setLayout(new GridBagLayout());
            pRfid.setBorder(BorderFactory.createTitledBorder(Setup.getRfidLabel()));
            rfidComboBox = new NamedBeanComboBox<IdTag>(tagManager);
            rfidComboBox.setAllowNull(true);
            rfidComboBox.setToolTipText(Bundle.getMessage("TipIdTag"));
            addItem(pRfid, rfidComboBox, 1, 0);
            pOptional.add(pRfid);
        }

        // row 15
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
        addEditButtonAction(editTypeButton);
        addEditButtonAction(editLengthButton);
        addEditButtonAction(editGroupButton);
        addEditButtonAction(editOwnerButton);

        addButtonAction(clearRoadNumberButton);
        addButtonAction(deleteButton);
        addButtonAction(addButton);
        addButtonAction(saveButton);

        // setup combobox
        addComboBoxAction(typeComboBox);
        addComboBoxAction(lengthComboBox);
        addComboBoxAction(locationBox);
        
        addCheckBoxAction(autoTrackCheckBox);
        autoTrackCheckBox.setEnabled(false);

        // get notified if combo box gets modified
        addPropertyChangeListeners();

        initMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
    }

    protected void load(RollingStock rs) {
        _rs = rs;

        // engines and cars share the same road database
        if (!InstanceManager.getDefault(CarRoads.class).containsName(rs.getRoadName())) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("roadNameNotExist"),
                    new Object[]{rs.getRoadName()}), Bundle.getMessage("addRoad"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                InstanceManager.getDefault(CarRoads.class).addName(rs.getRoadName());
            }
        }
        roadComboBox.setSelectedItem(rs.getRoadName());

        roadNumberTextField.setText(rs.getNumber());

        if (!getTypeManager().containsName(rs.getTypeName())) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("typeNameNotExist"),
                    new Object[]{rs.getTypeName()}), Bundle.getMessage("addType"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                getTypeManager().addName(rs.getTypeName());
            }
        }
        typeComboBox.setSelectedItem(rs.getTypeName());
        blockingTextField.setText(Integer.toString(rs.getBlocking()));

        if (!getLengthManager().containsName(rs.getLength())) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("lengthNameNotExist"),
                    new Object[]{rs.getLength()}), Bundle.getMessage("addLength"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                getLengthManager().addName(rs.getLength());
            }
        }
        //        }
        lengthComboBox.setSelectedItem(rs.getLength());

        weightTextField.setText(rs.getWeight());
        weightTonsTextField.setText(rs.getWeightTons());
        locationBox.setSelectedItem(rs.getLocation());
        updateTrackLocationBox();

        builtTextField.setText(rs.getBuilt());

        // Engines and cars share the owner database
        if (!InstanceManager.getDefault(CarOwners.class).containsName(rs.getOwner())) {
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("ownerNameNotExist"),
                    new Object[]{rs.getOwner()}), Bundle.getMessage("addOwner"),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                InstanceManager.getDefault(CarOwners.class).addName(rs.getOwner());
            }
        }
        ownerComboBox.setSelectedItem(rs.getOwner());

        commentTextField.setText(rs.getComment());
        valueTextArea.setText(rs.getValue());
        if(rfidComboBox != null) {
           rfidComboBox.setSelectedItem(rs.getIdTag());
        }
        // enable delete and save buttons
        deleteButton.setEnabled(true);
        saveButton.setEnabled(true);
        autoTrackCheckBox.setEnabled(true);
    }

    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == typeComboBox && typeComboBox.getSelectedItem() != null) {
            // turn off auto for location tracks
            autoTrackCheckBox.setSelected(false);
            autoTrackCheckBox.setEnabled(false);
            updateTrackLocationBox();
        }
        if (ae.getSource() == locationBox) {
            updateTrackLocationBox();
        }
    }
    
    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == autoTrackCheckBox) {
            updateTrackLocationBox();
        }
    }

    // Save, Delete, Add, Clear, Calculate, Edit Load buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            // log.debug("car save button pressed");
            if (!check(_rs)) {
                return;
            }
            save(IS_SAVE);
            // save car file
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == addButton) {
            if (!check(null)) {
                return;
            }

            // enable delete and save buttons
            deleteButton.setEnabled(true);
            saveButton.setEnabled(true);

            save(!IS_SAVE);
            // save car file
            OperationsXml.save();
        }
        if (ae.getSource() == deleteButton) {
            log.debug("car delete button activated");
            // disable delete and save buttons
            deleteButton.setEnabled(false);
            saveButton.setEnabled(false);
            if (_rs != null) {
                _rs.removePropertyChangeListener(this);
            }
            delete();
            _rs = null;
            OperationsXml.save();
        }
        if (ae.getSource() == clearRoadNumberButton) {
            roadNumberTextField.setText("");
            roadNumberTextField.requestFocus();
        }
    }

    protected void updateTrackLocationBox() {
        if (locationBox.getSelectedItem() == null) {
            trackLocationBox.removeAllItems();
        } else {
            log.debug("Update tracks for location: " + locationBox.getSelectedItem());
            Location loc = ((Location) locationBox.getSelectedItem());
            loc.updateComboBox(trackLocationBox, _rs, autoTrackCheckBox.isSelected(), false);
            if (_rs != null && _rs.getLocation() == loc) {
                trackLocationBox.setSelectedItem(_rs.getTrack());
            }
        }
    }

    protected boolean check(RollingStock rs) {
        String roadNum = roadNumberTextField.getText();
        if (!OperationsXml.checkFileName(roadNum)) { // NOI18N
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NameResChar") + NEW_LINE + Bundle.getMessage("ReservedChar"),
                    Bundle.getMessage("roadNumNG"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (roadNum.length() > Control.max_len_string_road_number) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString("RoadNumMustBeLess"),
                    new Object[]{Control.max_len_string_road_number + 1}), getRb().getString("RoadNumTooLong"),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // check rolling stock's weight in tons has proper format
        if (!weightTonsTextField.getText().trim().isEmpty()) {
            try {
                Integer.parseInt(weightTonsTextField.getText());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, getRb().getString("WeightFormatTon"),
                        getRb().getString("WeightTonError"),
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected <T extends RollingStock> void save(RollingStockManager<T> manager, boolean isSave) {
        // if the rolling stock's road or number changes, it needs a new id
        if (isSave &&
                _rs != null &&
                (!_rs.getRoadName().equals(roadComboBox.getSelectedItem()) ||
                        !_rs.getNumber().equals(roadNumberTextField.getText()))) {
            String road = (String) roadComboBox.getSelectedItem();
            String number = roadNumberTextField.getText();
            _rs.setRoadName(road);
            _rs.setNumber(number);
        }
        if (_rs == null ||
                !_rs.getRoadName().equals(roadComboBox.getSelectedItem()) ||
                !_rs.getNumber().equals(roadNumberTextField.getText())) {
            _rs = manager.newRS((String) roadComboBox.getSelectedItem(), roadNumberTextField.getText());
            _rs.addPropertyChangeListener(this);
        }
        // engine model must be set before type, length, weight and HP
        if (Engine.class.isInstance(_rs) && modelComboBox.getSelectedItem() != null) {
            ((Engine) _rs).setModel((String) modelComboBox.getSelectedItem());
        }
        if (typeComboBox.getSelectedItem() != null) {
            _rs.setTypeName((String) typeComboBox.getSelectedItem());
        }
        
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
        blockingTextField.setText(Integer.toString(blocking));
        
        if (lengthComboBox.getSelectedItem() != null) {
            _rs.setLength((String) lengthComboBox.getSelectedItem());
        }
        try {
            _rs.setWeight(NumberFormat.getNumberInstance().parse(weightTextField.getText()).toString());
        } catch (ParseException e1) {
            log.debug("Weight not a number");
        }
        _rs.setWeightTons(weightTonsTextField.getText());
        _rs.setBuilt(builtTextField.getText());
        if (ownerComboBox.getSelectedItem() != null) {
            _rs.setOwner((String) ownerComboBox.getSelectedItem());
        }
        _rs.setComment(commentTextField.getText());
        _rs.setValue(valueTextArea.getText());
        if(rfidComboBox!=null) {
            // save the IdTag for this rolling stock
            _rs.setIdTag(rfidComboBox.getSelectedItem());
        }
        autoTrackCheckBox.setEnabled(true);

        if (locationBox.getSelectedItem() != null && trackLocationBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, getRb().getString("rsFullySelect"), getRb().getString("rsCanNotLoc"),
                    JOptionPane.ERROR_MESSAGE);
            // update location only if it has changed
        } else if (_rs.getLocation() == null ||
                !_rs.getLocation().equals(locationBox.getSelectedItem()) ||
                _rs.getTrack() == null ||
                !_rs.getTrack().equals(trackLocationBox.getSelectedItem())) {
            setLocationAndTrack(_rs);
        }
    }

    protected void setLocationAndTrack(RollingStock rs) {
        if (locationBox.getSelectedItem() == null) {
            rs.setLocation(null, null);
        } else {
            rs.setLastRouteId(RollingStock.NONE); // clear last route id
            String status = rs.setLocation((Location) locationBox.getSelectedItem(), (Track) trackLocationBox
                    .getSelectedItem());
            if (!status.equals(Track.OKAY)) {
                log.debug("Can't set rolling stock's location because of {}", status);
                JOptionPane.showMessageDialog(this, MessageFormat.format(getRb().getString("rsCanNotLocMsg"),
                        new Object[]{rs.toString(), status}), getRb().getString("rsCanNotLoc"),
                        JOptionPane.ERROR_MESSAGE);
                // does the user want to force the rolling stock to this track?
                int results = JOptionPane.showOptionDialog(this, MessageFormat.format(getRb().getString("rsForce"),
                        new Object[]{rs.toString(), (Track) trackLocationBox.getSelectedItem()}),
                        MessageFormat
                                .format(getRb().getString("rsOverride"), new Object[]{status}),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (results == JOptionPane.YES_OPTION) {
                    log.debug("Force rolling stock to track");
                    rs.setLocation((Location) locationBox.getSelectedItem(), (Track) trackLocationBox
                            .getSelectedItem(), RollingStock.FORCE);
                }
            }
        }
    }

    // for the AttributeEditFrame edit buttons
    protected void addEditButtonAction(JButton b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                buttonEditActionPerformed(e);
            }
        });
    }

    @Override
    public void dispose() {
        removePropertyChangeListeners();
        super.dispose();
    }

    protected void addPropertyChangeListeners() {
        InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);
        getTypeManager().addPropertyChangeListener(this);
        getLengthManager().addPropertyChangeListener(this);
        InstanceManager.getDefault(CarOwners.class).addPropertyChangeListener(this);
        locationManager.addPropertyChangeListener(this);
    }

    protected void removePropertyChangeListeners() {
        InstanceManager.getDefault(CarRoads.class).removePropertyChangeListener(this);
        getTypeManager().removePropertyChangeListener(this);
        getLengthManager().removePropertyChangeListener(this);
        InstanceManager.getDefault(CarOwners.class).removePropertyChangeListener(this);
        locationManager.removePropertyChangeListener(this);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals(CarRoads.CARROADS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(CarRoads.class).updateComboBox(roadComboBox);
            if (_rs != null) {
                roadComboBox.setSelectedItem(_rs.getRoadName());
            }
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(EngineTypes.ENGINETYPES_CHANGED_PROPERTY)) {
            getTypeManager().updateComboBox(typeComboBox);
            if (_rs != null) {
                typeComboBox.setSelectedItem(_rs.getTypeName());
            }
        }
        if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(CarOwners.class).updateComboBox(ownerComboBox);
            if (_rs != null) {
                ownerComboBox.setSelectedItem(_rs.getOwner());
            }
        }
        if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(RollingStock.TRACK_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(LocationManager.class).updateComboBox(locationBox);
            updateTrackLocationBox();
            if (_rs != null && _rs.getLocation() != null) {
                locationBox.setSelectedItem(_rs.getLocation());
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(CarEditFrame.class);
}
