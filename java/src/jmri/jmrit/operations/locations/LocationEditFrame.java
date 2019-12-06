package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.tools.*;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainCommon;
import jmri.swing.NamedBeanComboBox;

/**
 * Frame for user edit of location
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012, 2013
 */
public class LocationEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    YardTableModel yardModel = new YardTableModel();
    JTable yardTable = new JTable(yardModel);
    JScrollPane yardPane;
    SpurTableModel spurModel = new SpurTableModel();
    JTable spurTable = new JTable(spurModel);
    JScrollPane spurPane;
    InterchangeTableModel interchangeModel = new InterchangeTableModel();
    JTable interchangeTable = new JTable(interchangeModel);
    JScrollPane interchangePane;
    StagingTableModel stagingModel = new StagingTableModel();
    JTable stagingTable = new JTable(stagingModel);
    JScrollPane stagingPane;

    LocationManager locationManager = InstanceManager.getDefault(LocationManager.class);

    public Location _location = null;
    ArrayList<JCheckBox> checkBoxes = new ArrayList<>();
    JPanel panelCheckBoxes = new JPanel();
    JScrollPane typePane;
    JPanel directionPanel = new JPanel();

    // major buttons
    JButton clearButton = new JButton(Bundle.getMessage("ClearAll"));
    JButton setButton = new JButton(Bundle.getMessage("SelectAll"));
    JButton autoSelectButton = new JButton(Bundle.getMessage("AutoSelect"));
    JButton saveLocationButton = new JButton(Bundle.getMessage("SaveLocation"));
    JButton deleteLocationButton = new JButton(Bundle.getMessage("DeleteLocation"));
    JButton addLocationButton = new JButton(Bundle.getMessage("AddLocation"));
    JButton addYardButton = new JButton(Bundle.getMessage("AddYard"));
    JButton addSpurButton = new JButton(Bundle.getMessage("AddSpur"));
    JButton addInterchangeButton = new JButton(Bundle.getMessage("AddInterchange"));
    JButton addStagingButton = new JButton(Bundle.getMessage("AddStaging"));

    // check boxes
    JCheckBox northCheckBox = new JCheckBox(Bundle.getMessage("North"));
    JCheckBox southCheckBox = new JCheckBox(Bundle.getMessage("South"));
    JCheckBox eastCheckBox = new JCheckBox(Bundle.getMessage("East"));
    JCheckBox westCheckBox = new JCheckBox(Bundle.getMessage("West"));

    // radio buttons
    JRadioButton stageRadioButton = new JRadioButton(Bundle.getMessage("StagingOnly"));
    JRadioButton interchangeRadioButton = new JRadioButton(Bundle.getMessage("Interchange"));
    JRadioButton yardRadioButton = new JRadioButton(Bundle.getMessage("Yards"));
    JRadioButton spurRadioButton = new JRadioButton(Bundle.getMessage("Spurs"));

    // text field
    JTextField locationNameTextField = new JTextField(Control.max_len_string_location_name);

    // text area
    JTextArea commentTextArea = new JTextArea(2, 60);
    JScrollPane commentScroller = new JScrollPane(commentTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // Reader selection dropdown.
    NamedBeanComboBox<Reporter> readerSelector;
    
    JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));

    public static final String NAME = Bundle.getMessage("Name");
    public static final int MAX_NAME_LENGTH = Control.max_len_string_location_name;
    public static final String DISPOSE = "dispose"; // NOI18N

    public LocationEditFrame(Location location) {
        super(Bundle.getMessage("TitleLocationEdit"));

        _location = location;

        // Set up the jtable in a Scroll Pane..
        typePane = new JScrollPane(panelCheckBoxes);
        typePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        typePane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Types")));

        yardPane = new JScrollPane(yardTable);
        yardPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        yardPane.setBorder(BorderFactory.createTitledBorder(""));

        spurPane = new JScrollPane(spurTable);
        spurPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        spurPane.setBorder(BorderFactory.createTitledBorder(""));

        interchangePane = new JScrollPane(interchangeTable);
        interchangePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        interchangePane.setBorder(BorderFactory.createTitledBorder(""));

        stagingPane = new JScrollPane(stagingTable);
        stagingPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        stagingPane.setBorder(BorderFactory.createTitledBorder(""));

        // button group
        ButtonGroup opsGroup = new ButtonGroup();
        opsGroup.add(spurRadioButton);
        opsGroup.add(yardRadioButton);
        opsGroup.add(interchangeRadioButton);
        opsGroup.add(stageRadioButton);

        if (_location != null) {
            enableButtons(true);
            locationNameTextField.setText(_location.getName());
            commentTextArea.setText(_location.getComment());
            yardModel.initTable(yardTable, location);
            spurModel.initTable(spurTable, location);
            interchangeModel.initTable(interchangeTable, location);
            stagingModel.initTable(stagingTable, location);
            _location.addPropertyChangeListener(this);
            if (_location.getLocationOps() == Location.NORMAL) {
                if (spurModel.getRowCount() > 0) {
                    spurRadioButton.setSelected(true);
                } else if (yardModel.getRowCount() > 0) {
                    yardRadioButton.setSelected(true);
                } else if (interchangeModel.getRowCount() > 0) {
                    interchangeRadioButton.setSelected(true);
                } else if (stagingModel.getRowCount() > 0) {
                    stageRadioButton.setSelected(true);
                } else {
                    spurRadioButton.setSelected(true);
                }
            } else {
                stageRadioButton.setSelected(true);
            }
            setTrainDirectionBoxes();
            if (Setup.isRfidEnabled()) {
                readerSelector.setSelectedItem(_location.getReporter());
            }
        } else {
            enableButtons(false);
            spurRadioButton.setSelected(true);
        }

        setVisibleLocations();

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows
        // row 1
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        JScrollPane p1Pane = new JScrollPane(p1);
        p1Pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        p1Pane.setMinimumSize(new Dimension(300, 3 * locationNameTextField.getPreferredSize().height));
        p1Pane.setBorder(BorderFactory.createTitledBorder(""));

        // row 1a
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));

        addItem(pName, locationNameTextField, 0, 0);

        // row 1b
        directionPanel.setLayout(new GridBagLayout());
        directionPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainLocation")));
        addItem(directionPanel, northCheckBox, 1, 0);
        addItem(directionPanel, southCheckBox, 2, 0);
        addItem(directionPanel, eastCheckBox, 3, 0);
        addItem(directionPanel, westCheckBox, 4, 0);

        p1.add(pName);
        p1.add(directionPanel);

        // row 5
        panelCheckBoxes.setLayout(new GridBagLayout());
        updateCheckboxes();

        // row 9
        JPanel pOp = new JPanel();
        pOp.setLayout(new GridBagLayout());
        pOp.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TracksAtLocation")));
        pOp.add(spurRadioButton);
        pOp.add(yardRadioButton);
        pOp.add(interchangeRadioButton);
        pOp.add(stageRadioButton);

        // row 11
        JPanel pC = new JPanel();
        pC.setLayout(new GridBagLayout());
        pC.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        addItem(pC, commentScroller, 0, 0);

        // adjust text area width based on window size
        adjustTextAreaColumnWidth(commentScroller, commentTextArea);

        JPanel readerPanel = new JPanel();
        readerPanel.setVisible(false);
        // reader row
        if (Setup.isRfidEnabled()) {
            ReporterManager reporterManager = InstanceManager.getDefault(ReporterManager.class);
            readerSelector=new NamedBeanComboBox<Reporter>(reporterManager);
            readerSelector.setAllowNull(true);
            readerPanel.setLayout(new GridBagLayout());
            readerPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("idReader")));
            addItem(readerPanel, readerSelector, 0, 0);
            readerPanel.setVisible(true);
        }

        // row 12
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        addItem(pB, deleteLocationButton, 0, 0);
        addItem(pB, addLocationButton, 1, 0);
        addItem(pB, saveLocationButton, 3, 0);

        getContentPane().add(p1Pane);
        getContentPane().add(typePane);
        getContentPane().add(pOp);
        getContentPane().add(yardPane);
        getContentPane().add(addYardButton);
        getContentPane().add(spurPane);
        getContentPane().add(addSpurButton);
        getContentPane().add(interchangePane);
        getContentPane().add(addInterchangeButton);
        getContentPane().add(stagingPane);
        getContentPane().add(addStagingButton);
        getContentPane().add(pC);
        getContentPane().add(readerPanel);
        getContentPane().add(pB);

        // setup buttons
        addButtonAction(setButton);
        addButtonAction(clearButton);
        addButtonAction(autoSelectButton);
        addButtonAction(deleteLocationButton);
        addButtonAction(addLocationButton);
        addButtonAction(saveLocationButton);
        addButtonAction(addYardButton);
        addButtonAction(addSpurButton);
        addButtonAction(addInterchangeButton);
        addButtonAction(addStagingButton);

        // add tool tips
        autoSelectButton.setToolTipText(Bundle.getMessage("TipAutoSelect"));

        addRadioButtonAction(spurRadioButton);
        addRadioButtonAction(yardRadioButton);
        addRadioButtonAction(interchangeRadioButton);
        addRadioButtonAction(stageRadioButton);

        addCheckBoxTrainAction(northCheckBox);
        addCheckBoxTrainAction(southCheckBox);
        addCheckBoxTrainAction(eastCheckBox);
        addCheckBoxTrainAction(westCheckBox);

        // add property listeners
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(EngineTypes.class).addPropertyChangeListener(this);

        // build menu
        JMenuBar menuBar = new JMenuBar();
        
        loadToolMenu();
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_AddLocation", true); // NOI18N

        pack();
        setMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
        setVisible(true);

    }
    
    private void loadToolMenu() {
        toolMenu.removeAll();
        toolMenu.add(new TrackCopyAction(this));
        toolMenu.add(new ChangeTracksTypeAction(this));
        toolMenu.add(new ShowTrackMovesAction());
        toolMenu.add(new ModifyLocationsAction(Bundle.getMessage("TitleModifyLocation"), _location));
        toolMenu.add(new ModifyLocationsCarLoadsAction(_location));
        if (_location != null && _location.getLocationOps() == Location.NORMAL) {
            toolMenu.add(new LocationTrackBlockingOrderAction(_location));
        }
        toolMenu.add(new ShowTrainsServingLocationAction(Bundle.getMessage("MenuItemShowTrainsLocation"), _location,
                null));
        toolMenu.add(new EditCarTypeAction());
        toolMenu.add(new ShowCarsByLocationAction(false, _location, null));
        toolMenu.addSeparator();
        toolMenu.add(new PrintLocationsAction(Bundle.getMessage("MenuItemPrint"), false, _location));
        toolMenu.add(new PrintLocationsAction(Bundle.getMessage("MenuItemPreview"), true, _location));
        if (Setup.isVsdPhysicalLocationEnabled()) {
            toolMenu.add(new SetPhysicalLocationAction(Bundle.getMessage("MenuSetPhysicalLocation"), _location));
        }
    }

    YardEditFrame yef = null;
    SpurEditFrame sef = null;
    InterchangeEditFrame ief = null;
    StagingEditFrame stef = null;

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == addYardButton) {
            yef = new YardEditFrame();
            yef.initComponents(_location, null);
            yef.setTitle(Bundle.getMessage("AddYard"));
        }
        if (ae.getSource() == addSpurButton) {
            sef = new SpurEditFrame();
            sef.initComponents(_location, null);
            sef.setTitle(Bundle.getMessage("AddSpur"));
        }
        if (ae.getSource() == addInterchangeButton) {
            ief = new InterchangeEditFrame();
            ief.initComponents(_location, null);
            ief.setTitle(Bundle.getMessage("AddInterchange"));
        }
        if (ae.getSource() == addStagingButton) {
            stef = new StagingEditFrame();
            stef.initComponents(_location, null);
            stef.setTitle(Bundle.getMessage("AddStaging"));
        }

        if (ae.getSource() == saveLocationButton) {
            log.debug("location save button activated");
            Location l = locationManager.getLocationByName(locationNameTextField.getText());
            if (_location == null && l == null) {
                saveNewLocation();
            } else {
                if (l != null && l != _location) {
                    reportLocationExists(Bundle.getMessage("save"));
                    return;
                }
                saveLocation();
                if (Setup.isCloseWindowOnSaveEnabled()) {
                    dispose();
                }
            }
        }
        if (ae.getSource() == deleteLocationButton) {
            log.debug("location delete button activated");
            Location l = locationManager.getLocationByName(locationNameTextField.getText());
            if (l == null) {
                return;
            }
            int rs = l.getNumberRS();
            if (rs > 0) {
                if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("ThereAreCars"),
                        new Object[]{Integer.toString(rs)}), Bundle.getMessage("deletelocation?"),
                        JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            } else {
                if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle
                        .getMessage("DoYouWantToDeleteLocation"), new Object[]{locationNameTextField.getText()}),
                        Bundle.getMessage("deletelocation?"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            yardModel.dispose();
            spurModel.dispose();
            interchangeModel.dispose();
            stagingModel.dispose();

            if (yef != null) {
                yef.dispose();
            }
            if (sef != null) {
                sef.dispose();
            }
            if (ief != null) {
                ief.dispose();
            }
            if (stef != null) {
                stef.dispose();
            }

            locationManager.deregister(l);
            _location = null;
            selectCheckboxes(false);
            enableCheckboxes(false);
            enableButtons(false);
            // save location file
            OperationsXml.save();
        }
        if (ae.getSource() == addLocationButton) {
            Location l = locationManager.getLocationByName(locationNameTextField.getText());
            if (l != null) {
                reportLocationExists(Bundle.getMessage("add"));
                return;
            }
            saveNewLocation();
        }
        if (ae.getSource() == setButton) {
            selectCheckboxes(true);
        }
        if (ae.getSource() == clearButton) {
            selectCheckboxes(false);
        }
        if (ae.getSource() == autoSelectButton) {
            log.debug("auto select button pressed");
            if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("autoSelectCarTypes?"), Bundle
                    .getMessage("autoSelectLocations?"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            autoSelectCheckboxes();
        }
    }

    private void saveNewLocation() {
        if (!checkName(Bundle.getMessage("add"))) {
            return;
        }
        Location location = locationManager.newLocation(locationNameTextField.getText());
        yardModel.initTable(yardTable, location);
        spurModel.initTable(spurTable, location);
        interchangeModel.initTable(interchangeTable, location);
        stagingModel.initTable(stagingTable, location);
        _location = location;
        _location.addPropertyChangeListener(this);

        updateCheckboxes();
        enableButtons(true);
        setTrainDirectionBoxes();
        saveLocation();
        loadToolMenu();
    }

    private void saveLocation() {
        if (!checkName(Bundle.getMessage("save"))) {
            return;
        }
        // stop table editing so "Moves" are properly saved
        if (spurTable.isEditing()) {
            spurTable.getCellEditor().stopCellEditing();
        }
        if (yardTable.isEditing()) {
            yardTable.getCellEditor().stopCellEditing();
        }
        if (interchangeTable.isEditing()) {
            interchangeTable.getCellEditor().stopCellEditing();
        }
        if (stagingTable.isEditing()) {
            stagingTable.getCellEditor().stopCellEditing();
        }
        _location.setName(locationNameTextField.getText());
        _location.setComment(commentTextArea.getText());
        if (Setup.isRfidEnabled()) {
            _location.setReporter(readerSelector.getSelectedItem());
        }
        setLocationOps();
        // save location file
        OperationsXml.save();
    }

    /**
     *
     * @return true if name OK and is less than the maximum allowed length
     */
    private boolean checkName(String s) {
        if (locationNameTextField.getText().trim().equals("")) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"), MessageFormat.format(Bundle
                    .getMessage("CanNotLocation"), new Object[]{s}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (TrainCommon.splitString(locationNameTextField.getText()).length() > MAX_NAME_LENGTH) {
            // log.error("Location name must be less than "+ Integer.toString(MAX_NAME_LENGTH+1) +" characters");
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("LocationNameLengthMax"),
                    new Object[]{Integer.toString(MAX_NAME_LENGTH + 1)}), MessageFormat.format(Bundle
                            .getMessage("CanNotLocation"), new Object[]{s}),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!OperationsXml.checkFileName(locationNameTextField.getText())) { // NOI18N
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NameResChar") + NEW_LINE + Bundle.getMessage("ReservedChar"),
                    MessageFormat.format(Bundle.getMessage("CanNotLocation"), new Object[]{s}),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void setLocationOps() {
        if (stageRadioButton.isSelected()) {
            _location.setLocationOps(Location.STAGING);
        } else {
            _location.setLocationOps(Location.NORMAL);
        }
    }

    private void reportLocationExists(String s) {
        // log.info("Can not " + s + ", location already exists");
        JOptionPane.showMessageDialog(this, Bundle.getMessage("LocationAlreadyExists"), MessageFormat.format(Bundle
                .getMessage("CanNotLocation"), new Object[]{s}), JOptionPane.ERROR_MESSAGE);
    }

    private void enableButtons(boolean enabled) {
        toolMenu.setEnabled(enabled);
        northCheckBox.setEnabled(enabled);
        southCheckBox.setEnabled(enabled);
        eastCheckBox.setEnabled(enabled);
        westCheckBox.setEnabled(enabled);
        clearButton.setEnabled(enabled);
        setButton.setEnabled(enabled);
        autoSelectButton.setEnabled(enabled);
        addYardButton.setEnabled(enabled);
        addSpurButton.setEnabled(enabled);
        addInterchangeButton.setEnabled(enabled);
        addStagingButton.setEnabled(enabled);
        saveLocationButton.setEnabled(enabled);
        deleteLocationButton.setEnabled(enabled);
        // the inverse!
        addLocationButton.setEnabled(!enabled);
        // enable radio buttons
        spurRadioButton.setEnabled(enabled);
        yardRadioButton.setEnabled(enabled);
        interchangeRadioButton.setEnabled(enabled);
        stageRadioButton.setEnabled(enabled);
        //
        yardTable.setEnabled(enabled);
        if(readerSelector!=null) {
           // enable readerSelect.
           readerSelector.setEnabled(enabled && Setup.isRfidEnabled());
        }
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        setLocationOps();
        setVisibleLocations();
    }

    private void setVisibleLocations() {
        setEnabledLocations();
        interchangePane.setVisible(interchangeRadioButton.isSelected());
        addInterchangeButton.setVisible(interchangeRadioButton.isSelected());
        stagingPane.setVisible(stageRadioButton.isSelected());
        addStagingButton.setVisible(stageRadioButton.isSelected());
        yardPane.setVisible(yardRadioButton.isSelected());
        addYardButton.setVisible(yardRadioButton.isSelected());
        spurPane.setVisible(spurRadioButton.isSelected());
        addSpurButton.setVisible(spurRadioButton.isSelected());
    }

    private void setEnabledLocations() {
        if (spurModel.getRowCount() > 0 || yardModel.getRowCount() > 0 || interchangeModel.getRowCount() > 0) {
            if (stageRadioButton.isSelected()) {
                spurRadioButton.setSelected(true);
            }
            stageRadioButton.setEnabled(false);
        } else if (stagingModel.getRowCount() > 0) {
            stageRadioButton.setSelected(true);
            spurRadioButton.setEnabled(false);
            yardRadioButton.setEnabled(false);
            interchangeRadioButton.setEnabled(false);
        } else if (_location != null) {
            spurRadioButton.setEnabled(true);
            yardRadioButton.setEnabled(true);
            interchangeRadioButton.setEnabled(true);
            stageRadioButton.setEnabled(true);
        }
    }

    private void enableCheckboxes(boolean enable) {
        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setEnabled(enable);
        }
    }

    private void selectCheckboxes(boolean select) {
        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setSelected(select);
            if (_location != null) {
                if (select) {
                    _location.addTypeName(checkBoxes.get(i).getText());
                } else {
                    _location.deleteTypeName(checkBoxes.get(i).getText());
                }
            }
        }
    }

    private void updateCheckboxes() {
        x = 0;
        y = 0;
        checkBoxes.clear();
        panelCheckBoxes.removeAll();
        loadTypes(InstanceManager.getDefault(CarTypes.class).getNames());
        loadTypes(InstanceManager.getDefault(EngineTypes.class).getNames());
        JPanel p = new JPanel();
        p.add(clearButton);
        p.add(setButton);
        p.add(autoSelectButton);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridwidth = getNumberOfCheckboxesPerLine() + 1;
        gc.gridy = ++y;
        panelCheckBoxes.add(p, gc);
        panelCheckBoxes.revalidate();
        repaint();
    }

    int x = 0;
    int y = 0; // vertical position in panel

    private void loadTypes(String[] types) {
        int numberOfCheckBoxes = getNumberOfCheckboxesPerLine();
        for (String type : types) {
            JCheckBox checkBox = new JCheckBox();
            checkBoxes.add(checkBox);
            checkBox.setText(type);
            addCheckBoxAction(checkBox);
            addItemLeft(panelCheckBoxes, checkBox, x++, y);
            if (_location != null) {
                if (_location.acceptsTypeName(type)) {
                    checkBox.setSelected(true);
                }
            } else {
                checkBox.setEnabled(false);
            }
            // default is seven types per row
            if (x > numberOfCheckBoxes) {
                y++;
                x = 0;
            }
        }
    }

    /**
     * Adjust the location's car service types to only reflect the car types
     * serviced by the location's tracks.
     */
    private void autoSelectCheckboxes() {
        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setSelected(false);
            // check each track to determine which car types are serviced by this location
            List<Track> tracks = _location.getTrackList();
            for (Track track : tracks) {
                if (track.acceptsTypeName(checkBoxes.get(i).getText())) {
                    checkBoxes.get(i).setSelected(true);
                }
            }
            // this type of car isn't serviced by any of the tracks, so delete
            if (!checkBoxes.get(i).isSelected()) {
                _location.deleteTypeName(checkBoxes.get(i).getText());
            }
        }
    }

    LocationsByCarTypeFrame lctf = null;

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        JCheckBox b = (JCheckBox) ae.getSource();
        log.debug("checkbox change {}", b.getText());
        if (_location == null) {
            return;
        }
        _location.removePropertyChangeListener(this);
        if (b.isSelected()) {
            _location.addTypeName(b.getText());
            // show which tracks will service this car type
            if (InstanceManager.getDefault(CarTypes.class).containsName(b.getText())) {
                if (lctf != null) {
                    lctf.dispose();
                }
                lctf = new LocationsByCarTypeFrame();
                lctf.initComponents(_location, b.getText());
            }
        } else {
            _location.deleteTypeName(b.getText());
        }
        _location.addPropertyChangeListener(this);
    }

    private void addCheckBoxTrainAction(JCheckBox b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                checkBoxActionTrainPerformed(e);
            }
        });
    }

    private void checkBoxActionTrainPerformed(java.awt.event.ActionEvent ae) {
        // save train directions serviced by this location
        if (_location == null) {
            return;
        }
        int direction = 0;
        if (northCheckBox.isSelected()) {
            direction += Location.NORTH;
        }
        if (southCheckBox.isSelected()) {
            direction += Location.SOUTH;
        }
        if (eastCheckBox.isSelected()) {
            direction += Location.EAST;
        }
        if (westCheckBox.isSelected()) {
            direction += Location.WEST;
        }
        _location.setTrainDirections(direction);

    }

    private void setTrainDirectionBoxes() {
        northCheckBox.setVisible((Setup.getTrainDirection() & Setup.NORTH) == Setup.NORTH);
        southCheckBox.setVisible((Setup.getTrainDirection() & Setup.SOUTH) == Setup.SOUTH);
        eastCheckBox.setVisible((Setup.getTrainDirection() & Setup.EAST) == Setup.EAST);
        westCheckBox.setVisible((Setup.getTrainDirection() & Setup.WEST) == Setup.WEST);

        northCheckBox.setSelected((_location.getTrainDirections() & Location.NORTH) == Location.NORTH);
        southCheckBox.setSelected((_location.getTrainDirections() & Location.SOUTH) == Location.SOUTH);
        eastCheckBox.setSelected((_location.getTrainDirections() & Location.EAST) == Location.EAST);
        westCheckBox.setSelected((_location.getTrainDirections() & Location.WEST) == Location.WEST);
    }

    @Override
    public void dispose() {
        if (_location != null) {
            _location.removePropertyChangeListener(this);
        }
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineTypes.class).removePropertyChangeListener(this);
        yardModel.dispose();
        spurModel.dispose();
        interchangeModel.dispose();
        stagingModel.dispose();
        if (lctf != null) {
            lctf.dispose();
        }
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(EngineTypes.ENGINETYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)) {
            updateCheckboxes();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LocationEditFrame.class);
}
