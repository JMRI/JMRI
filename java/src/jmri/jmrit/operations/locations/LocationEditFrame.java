package jmri.jmrit.operations.locations;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
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
import jmri.jmrit.operations.OperationsPanel;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.divisions.Division;
import jmri.jmrit.operations.locations.divisions.DivisionEditFrame;
import jmri.jmrit.operations.locations.divisions.DivisionManager;
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
    JTable spurTable = new JTable(spurModel) {
        // create tool tip for Hold column
        @Override
        public String getToolTipText(MouseEvent e) {
            int colIndex = columnAtPoint(e.getPoint());
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            if (realColumnIndex == TrackTableModel.HOLD_COLUMN) {
                return Bundle.getMessage("HoldCarsWithCustomLoads");
            }
            return null;
        }
    };
    JScrollPane spurPane;
    InterchangeTableModel interchangeModel = new InterchangeTableModel();
    JTable interchangeTable = new JTable(interchangeModel) {
        // create tool tip for Routed column
        @Override
        public String getToolTipText(MouseEvent e) {
            int colIndex = columnAtPoint(e.getPoint());
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            if (realColumnIndex == TrackTableModel.ROUTED_COLUMN) {
                return Bundle.getMessage("TipOnlyCarsWithFD");
            }
            return null;
        }
    };
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
    JButton editDivisionButton = new JButton(Bundle.getMessage("ButtonEdit"));
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
    JRadioButton stagingRadioButton = new JRadioButton(Bundle.getMessage("StagingOnly"));
    JRadioButton interchangeRadioButton = new JRadioButton(Bundle.getMessage("Interchange"));
    JRadioButton yardRadioButton = new JRadioButton(Bundle.getMessage("Yards"));
    JRadioButton spurRadioButton = new JRadioButton(Bundle.getMessage("Spurs"));

    // text field
    JTextField locationNameTextField = new JTextField(Control.max_len_string_location_name);

    // text area
    JTextArea commentTextArea = new JTextArea(2, 60);
    JScrollPane commentScroller = new JScrollPane(commentTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    JColorChooser commentColorChooser = new JColorChooser();

    // combo boxes
    protected JComboBox<Division> divisionComboBox = InstanceManager.getDefault(DivisionManager.class).getComboBox();

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
        typePane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesLocation")));

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
        opsGroup.add(stagingRadioButton);

        if (_location != null) {
            enableButtons(true);
            locationNameTextField.setText(_location.getName());
            commentTextArea.setText(_location.getComment());
            divisionComboBox.setSelectedItem(_location.getDivision());
            yardModel.initTable(yardTable, location);
            spurModel.initTable(spurTable, location);
            interchangeModel.initTable(interchangeTable, location);
            stagingModel.initTable(stagingTable, location);
            _location.addPropertyChangeListener(this);
            if (!_location.isStaging()) {
                if (spurModel.getRowCount() > 0) {
                    spurRadioButton.setSelected(true);
                } else if (yardModel.getRowCount() > 0) {
                    yardRadioButton.setSelected(true);
                } else if (interchangeModel.getRowCount() > 0) {
                    interchangeRadioButton.setSelected(true);
                } else if (stagingModel.getRowCount() > 0) {
                    stagingRadioButton.setSelected(true);
                } else {
                    spurRadioButton.setSelected(true);
                }
            } else {
                stagingRadioButton.setSelected(true);
            }
            setTrainDirectionBoxes();
        } else {
            enableButtons(false);
            spurRadioButton.setSelected(true);
        }

        setVisibleTracks();

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

        // division field
        JPanel pDivision = new JPanel();
        pDivision.setLayout(new GridBagLayout());
        pDivision.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Division")));
        addItem(pDivision, divisionComboBox, 2, 0);
        addItem(pDivision, editDivisionButton, 3, 0);
        setDivisionButtonText();

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
        pOp.add(stagingRadioButton);

        // row 11
        JPanel pC = new JPanel();
        pC.setLayout(new GridBagLayout());
        pC.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        addItem(pC, commentScroller, 0, 0);
        if (_location != null) {
            addItem(pC, OperationsPanel.getColorChooserPanel(_location.getCommentWithColor(), commentColorChooser), 2, 0);
        } else {
            addItem(pC, OperationsPanel.getColorChooserPanel("", commentColorChooser), 2, 0);
        }

        // adjust text area width based on window size less color chooser
        Dimension d = new Dimension(getPreferredSize().width - 100, getPreferredSize().height);
        adjustTextAreaColumnWidth(commentScroller, commentTextArea, d);

        JPanel readerPanel = new JPanel();
        readerPanel.setVisible(false);
        // reader row
        if (Setup.isRfidEnabled()) {
            ReporterManager reporterManager = InstanceManager.getDefault(ReporterManager.class);
            readerSelector = new NamedBeanComboBox<Reporter>(reporterManager);
            readerSelector.setAllowNull(true);
            readerPanel.setLayout(new GridBagLayout());
            readerPanel.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("idReporter")));
            addItem(readerPanel, readerSelector, 0, 0);
            readerPanel.setVisible(true);
            if (_location != null) {
                readerSelector.setSelectedItem(_location.getReporter());
            }
        }

        // row 12
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        addItem(pB, deleteLocationButton, 0, 0);
        addItem(pB, addLocationButton, 1, 0);
        addItem(pB, saveLocationButton, 3, 0);

        getContentPane().add(p1Pane);
        getContentPane().add(pDivision);
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
        addButtonAction(editDivisionButton);
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
        addRadioButtonAction(stagingRadioButton);

        addCheckBoxTrainAction(northCheckBox);
        addCheckBoxTrainAction(southCheckBox);
        addCheckBoxTrainAction(eastCheckBox);
        addCheckBoxTrainAction(westCheckBox);

        addComboBoxAction(divisionComboBox);

        // add property listeners
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(EngineTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(DivisionManager.class).addPropertyChangeListener(this);

        // build menu
        JMenuBar menuBar = new JMenuBar();

        loadToolMenu();
        menuBar.add(toolMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_AddLocation", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight500));
    }

    private void loadToolMenu() {
        toolMenu.removeAll();
        toolMenu.add(new TrackCopyAction(this));
        toolMenu.add(new ChangeTracksTypeAction(this));
        toolMenu.add(new ShowTrackMovesAction());
        toolMenu.add(new ModifyLocationsAction(_location));
        toolMenu.add(new ModifyLocationsCarLoadsAction(_location));
        if (_location != null && !_location.isStaging()) {
            toolMenu.add(new LocationTrackBlockingOrderAction(_location));
        }
        toolMenu.add(new ShowTrainsServingLocationAction(_location, null));
        toolMenu.add(new EditCarTypeAction());
        toolMenu.add(new ShowCarsByLocationAction(false, _location, null));
        if (Setup.isVsdPhysicalLocationEnabled()) {
            toolMenu.add(new SetPhysicalLocationAction(_location));
        }
        toolMenu.addSeparator();
        toolMenu.add(new PrintLocationsAction(false, _location));
        toolMenu.add(new PrintLocationsAction(true, _location));
    }

    // frames
    DivisionEditFrame def = null;
    YardEditFrame yef = null;
    SpurEditFrame sef = null;
    InterchangeEditFrame ief = null;
    StagingEditFrame stef = null;

    // Save, Delete, Add
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == editDivisionButton) {
            if (def != null) {
                def.dispose();
            }
            def = new DivisionEditFrame((Division) divisionComboBox.getSelectedItem());
        }
        if (ae.getSource() == addYardButton) {
            yef = new YardEditFrame();
            yef.initComponents(_location, null);
        }
        if (ae.getSource() == addSpurButton) {
            sef = new SpurEditFrame();
            sef.initComponents(_location, null);
        }
        if (ae.getSource() == addInterchangeButton) {
            ief = new InterchangeEditFrame();
            ief.initComponents(_location, null);
        }
        if (ae.getSource() == addStagingButton) {
            stef = new StagingEditFrame();
            stef.initComponents(_location, null);
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
                if (JOptionPane.showConfirmDialog(this,
                        MessageFormat.format(Bundle.getMessage("ThereAreCars"), new Object[] { Integer.toString(rs) }),
                        Bundle.getMessage("deletelocation?"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }
            } else {
                if (JOptionPane.showConfirmDialog(this,
                        MessageFormat.format(Bundle.getMessage("DoYouWantToDeleteLocation"),
                                new Object[] { locationNameTextField.getText() }),
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
            if (JOptionPane.showConfirmDialog(this, Bundle.getMessage("autoSelectCarTypes?"),
                    Bundle.getMessage("autoSelectLocations?"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
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
        _location.setComment(TrainCommon.formatColorString(commentTextArea.getText(), commentColorChooser.getColor()));
        _location.setDivision((Division) divisionComboBox.getSelectedItem());
        if (Setup.isRfidEnabled() && readerSelector != null) {
            _location.setReporter(readerSelector.getSelectedItem());
        }
        // save location file
        OperationsXml.save();
    }

    /**
     * @return true if name OK and is less than the maximum allowed length
     */
    private boolean checkName(String s) {
        String locationName = locationNameTextField.getText().trim();
        if (locationName.isEmpty()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"),
                    MessageFormat.format(Bundle.getMessage("CanNotLocation"), new Object[] { s }),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        // hyphen feature needs at least one character to work properly
        if (locationName.contains(TrainCommon.HYPHEN)) {
            String[] check = locationName.split(TrainCommon.HYPHEN);
            if (check.length == 0) {
                JOptionPane.showMessageDialog(this, Bundle.getMessage("HyphenFeature"),
                        MessageFormat.format(Bundle.getMessage("CanNotLocation"), new Object[] { s }),
                        JOptionPane.ERROR_MESSAGE);

                return false;
            }
        }
        if (TrainCommon.splitString(locationName).length() > MAX_NAME_LENGTH) {
            // log.error("Location name must be less than "+
            // Integer.toString(MAX_NAME_LENGTH+1) +" characters");
            JOptionPane.showMessageDialog(this,
                    MessageFormat.format(Bundle.getMessage("LocationNameLengthMax"),
                            new Object[] { Integer.toString(MAX_NAME_LENGTH + 1) }),
                    MessageFormat.format(Bundle.getMessage("CanNotLocation"), new Object[] { s }),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!OperationsXml.checkFileName(locationName)) { // NOI18N
            JOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NameResChar") + NEW_LINE + Bundle.getMessage("ReservedChar"),
                    MessageFormat.format(Bundle.getMessage("CanNotLocation"), new Object[] { s }),
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void reportLocationExists(String s) {
        // log.info("Can not " + s + ", location already exists");
        JOptionPane.showMessageDialog(this, Bundle.getMessage("LocationAlreadyExists"),
                MessageFormat.format(Bundle.getMessage("CanNotLocation"), new Object[] { s }),
                JOptionPane.ERROR_MESSAGE);
    }

    private void enableButtons(boolean enabled) {
        toolMenu.setEnabled(enabled);
        northCheckBox.setEnabled(enabled);
        southCheckBox.setEnabled(enabled);
        eastCheckBox.setEnabled(enabled);
        westCheckBox.setEnabled(enabled);
        divisionComboBox.setEnabled(enabled);
        editDivisionButton.setEnabled(enabled);
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
        stagingRadioButton.setEnabled(enabled);
        if (readerSelector != null) {
            // enable readerSelect.
            readerSelector.setEnabled(enabled && Setup.isRfidEnabled());
        }
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        setVisibleTracks();
    }

    private void setVisibleTracks() {
        setEnabledTracks();
        interchangePane.setVisible(interchangeRadioButton.isSelected());
        addInterchangeButton.setVisible(interchangeRadioButton.isSelected());
        stagingPane.setVisible(stagingRadioButton.isSelected());
        addStagingButton.setVisible(stagingRadioButton.isSelected());
        yardPane.setVisible(yardRadioButton.isSelected());
        addYardButton.setVisible(yardRadioButton.isSelected());
        spurPane.setVisible(spurRadioButton.isSelected());
        addSpurButton.setVisible(spurRadioButton.isSelected());
    }

    private void setEnabledTracks() {
        if (spurModel.getRowCount() > 0 || yardModel.getRowCount() > 0 || interchangeModel.getRowCount() > 0) {
            if (stagingRadioButton.isSelected()) {
                spurRadioButton.setSelected(true);
            }
            stagingRadioButton.setEnabled(false);
        } else if (stagingModel.getRowCount() > 0) {
            stagingRadioButton.setSelected(true);
            spurRadioButton.setEnabled(false);
            yardRadioButton.setEnabled(false);
            interchangeRadioButton.setEnabled(false);
        } else if (_location != null) {
            spurRadioButton.setEnabled(true);
            yardRadioButton.setEnabled(true);
            interchangeRadioButton.setEnabled(true);
            stagingRadioButton.setEnabled(true);
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

    protected void updateDivisionComboBox() {
        InstanceManager.getDefault(DivisionManager.class).updateComboBox(divisionComboBox);
        if (_location != null) {
            divisionComboBox.setSelectedItem(_location.getDivision());
        }
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
            // check each track to determine which car types are serviced by
            // this location
            List<Track> tracks = _location.getTracksList();
            for (Track track : tracks) {
                if (track.isTypeNameAccepted(checkBoxes.get(i).getText())) {
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
    protected void comboBoxActionPerformed(ActionEvent ae) {
        setDivisionButtonText();
    }

    private void setDivisionButtonText() {
        if (divisionComboBox.getSelectedItem() == null) {
            editDivisionButton.setText(Bundle.getMessage("Add"));
        } else {
            editDivisionButton.setText(Bundle.getMessage("ButtonEdit"));
        }
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
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue());
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(EngineTypes.ENGINETYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.TYPES_CHANGED_PROPERTY)) {
            updateCheckboxes();
        }
        if (e.getPropertyName().equals(DivisionManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateDivisionComboBox();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(LocationEditFrame.class);
}
