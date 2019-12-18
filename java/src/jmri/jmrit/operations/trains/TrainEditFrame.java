package jmri.jmrit.operations.trains;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteEditFrame;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.tools.PrintSavedTrainManifestAction;
import jmri.jmrit.operations.trains.tools.PrintTrainAction;
import jmri.jmrit.operations.trains.tools.PrintTrainBuildReportAction;
import jmri.jmrit.operations.trains.tools.PrintTrainManifestAction;
import jmri.jmrit.operations.trains.tools.TrainByCarTypeAction;
import jmri.jmrit.operations.trains.tools.TrainCopyAction;
import jmri.jmrit.operations.trains.tools.TrainManifestOptionAction;
import jmri.jmrit.operations.trains.tools.TrainScriptAction;

/**
 * Frame for user edit of a train
 *
 * @author Dan Boudreau Copyright (C) 2008, 2011, 2012, 2013, 2014
 */
public class TrainEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    TrainManager trainManager;
    RouteManager routeManager;

    public Train _train = null;
    List<JCheckBox> typeCarCheckBoxes = new ArrayList<>();
    List<JCheckBox> typeEngineCheckBoxes = new ArrayList<>();
    List<JCheckBox> locationCheckBoxes = new ArrayList<>();
    JPanel typeCarPanelCheckBoxes = new JPanel();
    JPanel typeEnginePanelCheckBoxes = new JPanel();
    JPanel roadAndLoadStatusPanel = new JPanel();
    JPanel locationPanelCheckBoxes = new JPanel();
    JScrollPane typeCarPane;
    JScrollPane typeEnginePane;
    JScrollPane locationsPane;

    // labels
    JLabel textRouteStatus = new JLabel();
    JLabel textModel = new JLabel(Bundle.getMessage("Model"));
    JLabel textRoad2 = new JLabel(Bundle.getMessage("Road"));
    JLabel textRoad3 = new JLabel(Bundle.getMessage("Road"));
    JLabel textEngine = new JLabel(Bundle.getMessage("Engines"));

    // major buttons
    JButton editButton = new JButton(Bundle.getMessage("ButtonEdit")); // edit route
    JButton clearButton = new JButton(Bundle.getMessage("ClearAll"));
    JButton setButton = new JButton(Bundle.getMessage("SelectAll"));
    JButton resetButton = new JButton(Bundle.getMessage("ResetTrain"));
    JButton saveTrainButton = new JButton(Bundle.getMessage("SaveTrain"));
    JButton deleteTrainButton = new JButton(Bundle.getMessage("DeleteTrain"));
    JButton addTrainButton = new JButton(Bundle.getMessage("AddTrain"));
    
    // alternate buttons
    JButton loadOptionButton = new JButton(Bundle.getMessage("AcceptAll"));
    JButton roadOptionButton = new JButton(Bundle.getMessage("AcceptAll"));

    // radio buttons
    JRadioButton noneRadioButton = new JRadioButton(Bundle.getMessage("None"));
    JRadioButton cabooseRadioButton = new JRadioButton(Bundle.getMessage("Caboose"));
    JRadioButton fredRadioButton = new JRadioButton(Bundle.getMessage("FRED"));
    ButtonGroup group = new ButtonGroup();

    // text field
    JTextField trainNameTextField = new JTextField(Control.max_len_string_train_name - 5); // make slightly smaller
    JTextField trainDescriptionTextField = new JTextField(30);

    // text area
    JTextArea commentTextArea = new JTextArea(2, 70);
    JScrollPane commentScroller = new JScrollPane(commentTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // for padding out panel
    JLabel space1 = new JLabel("       ");
    JLabel space2 = new JLabel("       "); // between hour and minute
    JLabel space3 = new JLabel("       ");
    JLabel space4 = new JLabel("       ");
    JLabel space5 = new JLabel("       ");

    // combo boxes
    JComboBox<String> hourBox = new JComboBox<>();
    JComboBox<String> minuteBox = new JComboBox<>();
    JComboBox<Route> routeBox = InstanceManager.getDefault(RouteManager.class).getComboBox();
    JComboBox<String> roadCabooseBox = new JComboBox<>();
    JComboBox<String> roadEngineBox = new JComboBox<>();
    JComboBox<String> modelEngineBox = InstanceManager.getDefault(EngineModels.class).getComboBox();
    JComboBox<String> numEnginesBox = new JComboBox<>();
    
    JMenu toolMenu = new JMenu(Bundle.getMessage("MenuTools"));

    public static final String DISPOSE = "dispose"; // NOI18N

    public TrainEditFrame(Train train) {
        super(Bundle.getMessage("TitleTrainEdit"));
        // Set up the jtable in a Scroll Pane..
        locationsPane = new JScrollPane(locationPanelCheckBoxes);
        locationsPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        locationsPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Stops")));

        typeCarPane = new JScrollPane(typeCarPanelCheckBoxes);
        typeCarPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesCar")));
        typeCarPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        typeEnginePane = new JScrollPane(typeEnginePanelCheckBoxes);
        typeEnginePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        typeEnginePane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesEngine")));

        _train = train;

        // load managers
        trainManager = InstanceManager.getDefault(TrainManager.class);
        routeManager = InstanceManager.getDefault(RouteManager.class);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Set up the panels
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JScrollPane pPane = new JScrollPane(p);
        pPane.setMinimumSize(new Dimension(300, 5 * trainNameTextField.getPreferredSize().height));
        pPane.setBorder(BorderFactory.createTitledBorder(""));

        // Layout the panel by rows
        // row 1
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        // row 1a
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
        addItem(pName, trainNameTextField, 0, 0);
        // row 1b
        JPanel pDesc = new JPanel();
        pDesc.setLayout(new GridBagLayout());
        pDesc.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Description")));
        trainDescriptionTextField.setToolTipText(Bundle.getMessage("TipTrainDescription"));
        addItem(pDesc, trainDescriptionTextField, 0, 0);

        p1.add(pName);
        p1.add(pDesc);

        // row 2
        JPanel p2 = new JPanel();
        p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));
        // row 2a
        JPanel pdt = new JPanel();
        pdt.setLayout(new GridBagLayout());
        pdt.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("DepartTime")));
        // build hour and minute menus
        for (int i = 0; i < 24; i++) {
            if (i < 10) {
                hourBox.addItem("0" + Integer.toString(i));
            } else {
                hourBox.addItem(Integer.toString(i));
            }
        }
        hourBox.setMinimumSize(new Dimension(100, 25));

        for (int i = 0; i < 60; i += 1) {
            if (i < 10) {
                minuteBox.addItem("0" + Integer.toString(i));
            } else {
                minuteBox.addItem(Integer.toString(i));
            }
        }
        addItem(pdt, space1, 0, 5);
        addItem(pdt, hourBox, 1, 5);
        addItem(pdt, space2, 2, 5);
        addItem(pdt, minuteBox, 3, 5);
        addItem(pdt, space3, 4, 5);
        // row 2b
        // BUG! routeBox needs its own panel when resizing frame!
        JPanel pr = new JPanel();
        pr.setLayout(new GridBagLayout());
        pr.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Route")));
        addItem(pr, routeBox, 0, 5);
        addItem(pr, space4, 1, 5);
        addItem(pr, editButton, 2, 5);
        addItem(pr, space5, 3, 5);
        addItem(pr, textRouteStatus, 4, 5);

        p2.add(pdt);
        p2.add(pr);

        p.add(p1);
        p.add(p2);

        // row 5
        locationPanelCheckBoxes.setLayout(new GridBagLayout());

        // row 6
        typeCarPanelCheckBoxes.setLayout(new GridBagLayout());

        // row 8
        typeEnginePanelCheckBoxes.setLayout(new GridBagLayout());

        // status panel for roads and loads
        roadAndLoadStatusPanel.setLayout(new BoxLayout(roadAndLoadStatusPanel, BoxLayout.X_AXIS));
        JPanel pRoadOption = new JPanel();
        pRoadOption.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RoadOption")));
        pRoadOption.add(roadOptionButton);
        roadOptionButton.addActionListener(new TrainRoadOptionsAction(Bundle.getMessage("MenuItemRoadOptions"), this));
        
        JPanel pLoadOption = new JPanel();
        pLoadOption.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LoadOption")));
        pLoadOption.add(loadOptionButton);
        loadOptionButton.addActionListener(new TrainLoadOptionsAction(Bundle.getMessage("MenuItemLoadOptions"), this));

        roadAndLoadStatusPanel.add(pRoadOption);
        roadAndLoadStatusPanel.add(pLoadOption);
        roadAndLoadStatusPanel.setVisible(false); // don't show unless there's a restriction

        // row 10
        JPanel trainReq = new JPanel();
        trainReq.setLayout(new GridBagLayout());
        trainReq.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainRequires")));

        for (int i = 0; i < Setup.getMaxNumberEngines() + 1; i++) {
            numEnginesBox.addItem(Integer.toString(i));
        }
        numEnginesBox.addItem(Train.AUTO);
        numEnginesBox.setMinimumSize(new Dimension(65, 20));
        numEnginesBox.setToolTipText(Bundle.getMessage("TipNumberOfLocos"));
        addItem(trainReq, textEngine, 1, 1);
        addItem(trainReq, numEnginesBox, 2, 1);
        addItem(trainReq, textModel, 3, 1);
        modelEngineBox.insertItemAt(NONE, 0);
        modelEngineBox.setSelectedIndex(0);
        modelEngineBox.setMinimumSize(new Dimension(120, 20));
        modelEngineBox.setToolTipText(Bundle.getMessage("ModelEngineTip"));
        addItem(trainReq, modelEngineBox, 4, 1);
        addItem(trainReq, textRoad2, 5, 1);
        roadEngineBox.insertItemAt(NONE, 0);
        roadEngineBox.setSelectedIndex(0);
        roadEngineBox.setMinimumSize(new Dimension(120, 20));
        roadEngineBox.setToolTipText(Bundle.getMessage("RoadEngineTip"));
        addItem(trainReq, roadEngineBox, 6, 1);

        addItem(trainReq, noneRadioButton, 2, 2);
        noneRadioButton.setToolTipText(Bundle.getMessage("TipNoCabooseOrFRED"));
        addItem(trainReq, fredRadioButton, 3, 2);
        fredRadioButton.setToolTipText(Bundle.getMessage("TipFRED"));
        addItem(trainReq, cabooseRadioButton, 4, 2);
        cabooseRadioButton.setToolTipText(Bundle.getMessage("TipCaboose"));
        addItem(trainReq, textRoad3, 5, 2);
        roadCabooseBox.setMinimumSize(new Dimension(120, 20));
        roadCabooseBox.setToolTipText(Bundle.getMessage("RoadCabooseTip"));
        addItem(trainReq, roadCabooseBox, 6, 2);
        group.add(noneRadioButton);
        group.add(cabooseRadioButton);
        group.add(fredRadioButton);
        noneRadioButton.setSelected(true);

        // row 13 comment
        JPanel pC = new JPanel();
        pC.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        pC.setLayout(new GridBagLayout());
        addItem(pC, commentScroller, 1, 0);

        // adjust text area width based on window size
        adjustTextAreaColumnWidth(commentScroller, commentTextArea);

        // row 15 buttons
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        addItem(pB, deleteTrainButton, 0, 0);
        addItem(pB, resetButton, 1, 0);
        addItem(pB, addTrainButton, 2, 0);
        addItem(pB, saveTrainButton, 3, 0);

        getContentPane().add(pPane);
        getContentPane().add(locationsPane);
        getContentPane().add(typeCarPane);
        getContentPane().add(typeEnginePane);
        getContentPane().add(roadAndLoadStatusPanel);
        getContentPane().add(trainReq);
        getContentPane().add(pC);
        getContentPane().add(pB);

        // setup buttons
        addButtonAction(editButton);
        addButtonAction(setButton);
        addButtonAction(clearButton);
        addButtonAction(resetButton);
        addButtonAction(deleteTrainButton);
        addButtonAction(addTrainButton);
        addButtonAction(saveTrainButton);

        addRadioButtonAction(noneRadioButton);
        addRadioButtonAction(cabooseRadioButton);
        addRadioButtonAction(fredRadioButton);

        // tool tips
        resetButton.setToolTipText(Bundle.getMessage("TipTrainReset"));
        
        // build menu
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(toolMenu);
        loadToolMenu(toolMenu);
        setJMenuBar(menuBar);
        addHelpMenu("package.jmri.jmrit.operations.Operations_TrainEdit", true); // NOI18N

        if (_train != null) {         
            trainNameTextField.setText(_train.getName());
            trainDescriptionTextField.setText(_train.getRawDescription());
            routeBox.setSelectedItem(_train.getRoute());
            modelEngineBox.setSelectedItem(_train.getEngineModel());
            commentTextArea.setText(_train.getComment());
            cabooseRadioButton.setSelected((_train.getRequirements() & Train.CABOOSE) == Train.CABOOSE);
            fredRadioButton.setSelected((_train.getRequirements() & Train.FRED) == Train.FRED);
            updateDepartureTime();
            enableButtons(true);
            // listen for train changes
            _train.addPropertyChangeListener(this);
            // listen for route changes
            Route route = _train.getRoute();
            if (route != null) {
                route.addPropertyChangeListener(this);
                if (_train.getTrainDepartsRouteLocation() != null
                        && _train.getTrainDepartsRouteLocation().getLocation() != null
                        && !_train.getTrainDepartsRouteLocation().getLocation().isStaging())
                    numEnginesBox.addItem(Train.AUTO_HPT);
            }
           numEnginesBox.setSelectedItem(_train.getNumberEngines());
        } else {
            setTitle(Bundle.getMessage("TitleTrainAdd"));
            enableButtons(false);
        }

        modelEngineBox.setEnabled(!numEnginesBox.getSelectedItem().equals("0"));
        roadEngineBox.setEnabled(!numEnginesBox.getSelectedItem().equals("0"));

        // load route location checkboxes
        updateLocationCheckboxes();
        updateCarTypeCheckboxes();
        updateEngineTypeCheckboxes();
        updateRoadAndLoadStatus();
        updateCabooseRoadComboBox();
        updateEngineRoadComboBox();

        // setup combobox
        addComboBoxAction(numEnginesBox);
        addComboBoxAction(routeBox);
        addComboBoxAction(modelEngineBox);

        // get notified if combo box gets modified
        routeManager.addPropertyChangeListener(this);
        // get notified if car types or roads gets modified
        InstanceManager.getDefault(CarTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(CarRoads.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(EngineTypes.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(EngineModels.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(LocationManager.class).addPropertyChangeListener(this);

        packFrame();
    }
    
    private void loadToolMenu(JMenu toolMenu) {
        toolMenu.removeAll();
        // first 5 menu items will also close when the edit train window closes
        toolMenu.add(new TrainEditBuildOptionsAction(Bundle.getMessage("MenuItemBuildOptions"), this));
        toolMenu.add(new TrainLoadOptionsAction(Bundle.getMessage("MenuItemLoadOptions"), this));
        toolMenu.add(new TrainRoadOptionsAction(Bundle.getMessage("MenuItemRoadOptions"), this));
        toolMenu.add(new TrainManifestOptionAction(Bundle.getMessage("MenuItemOptions"), this));
        toolMenu.add(new TrainScriptAction(Bundle.getMessage("MenuItemScripts"), this));
        
        toolMenu.add(new TrainCopyAction(Bundle.getMessage("TitleTrainCopy"), _train));
        toolMenu.add(new TrainByCarTypeAction(Bundle.getMessage("MenuItemShowCarTypes"), _train));
        toolMenu.add(new TrainConductorAction(Bundle.getMessage("TitleTrainConductor"), _train));
        toolMenu.addSeparator();
        toolMenu.add(new PrintTrainAction(Bundle.getMessage("MenuItemPrint"), false, this));
        toolMenu.add(new PrintTrainAction(Bundle.getMessage("MenuItemPreview"), true, this));
        toolMenu.add(new PrintTrainManifestAction(Bundle.getMessage("MenuItemPrintManifest"), false, _train));
        toolMenu.add(new PrintTrainManifestAction(Bundle.getMessage("MenuItemPreviewManifest"), true, _train));
        toolMenu.add(new PrintTrainBuildReportAction(Bundle.getMessage("MenuItemPrintBuildReport"), false, _train));
        toolMenu.add(new PrintTrainBuildReportAction(Bundle.getMessage("MenuItemPreviewBuildReport"), true, _train));
        toolMenu.add(new PrintSavedTrainManifestAction(Bundle.getMessage("MenuItemPrintSavedManifest"), false, _train));
        toolMenu.add(new PrintSavedTrainManifestAction(Bundle.getMessage("MenuItemPreviewSavedManifest"), true, _train));
    }

    // Save, Delete, Add, Edit, Reset, Set, Clear
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveTrainButton) {
            log.debug("train save button activated");
            Train train = trainManager.getTrainByName(trainNameTextField.getText());
            if (_train == null && train == null) {
                saveNewTrain();
            } else {
                if (train != null && train != _train) {
                    reportTrainExists(Bundle.getMessage("save"));
                    return;
                }
                checkRoute(); // check to see if use supplied a route, just warn if no route
                saveTrain();
            }
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == deleteTrainButton) {
            log.debug("train delete button activated");
            Train train = trainManager.getTrainByName(trainNameTextField.getText());
            if (train == null) {
                return;
            }
            if (!_train.reset()) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("TrainIsInRoute"),
                        new Object[]{train.getTrainTerminatesName()}), Bundle.getMessage("CanNotDeleteTrain"),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("deleteMsg"),
                    new Object[]{train.getName()}), Bundle.getMessage("deleteTrain"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                return;
            }
            routeBox.setSelectedItem(null);
            trainManager.deregister(train);
            for (Frame frame : children) {
                frame.dispose();
            }
            _train = null;
            enableButtons(false);
            // save train file
            OperationsXml.save();
        }
        if (ae.getSource() == addTrainButton) {
            Train train = trainManager.getTrainByName(trainNameTextField.getText());
            if (train != null) {
                reportTrainExists(Bundle.getMessage("add"));
                return;
            }
            saveNewTrain();
        }
        if (ae.getSource() == editButton) {
            editAddRoute();
        }
        if (ae.getSource() == setButton) {
            selectCheckboxes(true);
        }
        if (ae.getSource() == clearButton) {
            selectCheckboxes(false);
        }
        if (ae.getSource() == resetButton) {
            if (_train != null) {
                if (!_train.reset()) {
                    JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("TrainIsInRoute"),
                            new Object[]{_train.getTrainTerminatesName()}), Bundle.getMessage("CanNotResetTrain"),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        if (_train != null) {
            if (ae.getSource() == noneRadioButton || ae.getSource() == cabooseRadioButton
                    || ae.getSource() == fredRadioButton) {
                updateCabooseRoadComboBox();
            }
        }
    }

    private void saveNewTrain() {
        if (!checkName(Bundle.getMessage("add"))) {
            return;
        }
        Train train = trainManager.newTrain(trainNameTextField.getText());
        _train = train;
        _train.addPropertyChangeListener(this);

        // update check boxes
        updateCarTypeCheckboxes();
        updateEngineTypeCheckboxes();
        // enable check boxes and buttons
        enableButtons(true);
        saveTrain();
        loadToolMenu(toolMenu);
    }

    private void saveTrain() {
        if (!checkName(Bundle.getMessage("save"))) {
            return;
        }
        if (!checkModel() || !checkEngineRoad()) {
            return;
        }
        _train.setDepartureTime((String) hourBox.getSelectedItem(), (String) minuteBox.getSelectedItem());
        _train.setNumberEngines((String) numEnginesBox.getSelectedItem());
        if (_train.getNumberEngines().equals("0")) {
            modelEngineBox.setSelectedIndex(0);
            roadEngineBox.setSelectedIndex(0);
        }
        _train.setEngineRoad((String) roadEngineBox.getSelectedItem());
        _train.setEngineModel((String) modelEngineBox.getSelectedItem());
        if (cabooseRadioButton.isSelected()) {
            _train.setRequirements(Train.CABOOSE);
        }
        if (fredRadioButton.isSelected()) {
            _train.setRequirements(Train.FRED);
        }
        if (noneRadioButton.isSelected()) {
            _train.setRequirements(Train.NO_CABOOSE_OR_FRED);
        }
        _train.setCabooseRoad((String) roadCabooseBox.getSelectedItem());        
        if (!_train.getName().equals(trainNameTextField.getText())
                || !_train.getRawDescription().equals(trainDescriptionTextField.getText())
                || !_train.getComment().equals(commentTextArea.getText())) {
            _train.setModified(true);
        }
        _train.setName(trainNameTextField.getText().trim());
        _train.setDescription(trainDescriptionTextField.getText());
        _train.setComment(commentTextArea.getText());
        // save train file
        OperationsXml.save();
    }

    /**
     *
     * @return true if name isn't too long and is at least one character
     */
    private boolean checkName(String s) {
        String trainName = trainNameTextField.getText().trim();
        if (trainName.equals("")) {
            log.debug("Must enter a train name");
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"), MessageFormat.format(Bundle
                    .getMessage("CanNot"), new Object[]{s}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (trainName.length() > Control.max_len_string_train_name) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("TrainNameLess"),
                    new Object[]{Control.max_len_string_train_name + 1}), MessageFormat.format(Bundle
                            .getMessage("CanNot"), new Object[]{s}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!OperationsXml.checkFileName(trainName)) { // NOI18N
            log.error("Train name must not contain reserved characters");
            JOptionPane.showMessageDialog(this, Bundle.getMessage("NameResChar") + NEW_LINE
                    + Bundle.getMessage("ReservedChar"), MessageFormat.format(Bundle.getMessage("CanNot"),
                            new Object[]{s}), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private boolean checkModel() {
        String model = (String) modelEngineBox.getSelectedItem();
        if (numEnginesBox.getSelectedItem().equals("0") || model.equals(NONE)) {
            return true;
        }
        String type = InstanceManager.getDefault(EngineModels.class).getModelType(model);
        if (!_train.acceptsTypeName(type)) {
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("TrainModelService"),
                    new Object[]{model, type}), MessageFormat.format(Bundle.getMessage("CanNot"),
                            new Object[]{Bundle.getMessage("save")}), JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (roadEngineBox.getItemCount() == 1) {
            log.debug("No locos available that match the model selected!");
            JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("NoLocosModel"),
                    new Object[]{model}), MessageFormat.format(Bundle.getMessage("TrainWillNotBuild"),
                            new Object[]{_train.getName()}), JOptionPane.WARNING_MESSAGE);
        }
        return true;
    }

    private boolean checkEngineRoad() {
        String road = (String) roadEngineBox.getSelectedItem();
        String model = (String) modelEngineBox.getSelectedItem();
        if (numEnginesBox.getSelectedItem().equals("0") || road.equals(NONE) || !model.equals(NONE)) {
            return true;
        }
        for (RollingStock rs : InstanceManager.getDefault(EngineManager.class).getList()) {
            if (!_train.acceptsTypeName(rs.getTypeName())) {
                continue;
            }
            if (rs.getRoadName().equals(road)) {
                return true;
            }
        }
        JOptionPane.showMessageDialog(this, MessageFormat
                .format(Bundle.getMessage("NoLocoRoad"), new Object[]{road}), MessageFormat.format(Bundle
                        .getMessage("TrainWillNotBuild"), new Object[]{_train.getName()}), JOptionPane.WARNING_MESSAGE);
        return false; // couldn't find a loco with the selected road
    }

    private boolean checkRoute() {
        if (_train.getRoute() == null) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("TrainNeedsRoute"),
                    Bundle.getMessage("TrainNoRoute"), JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;

    }

    private void reportTrainExists(String s) {
        log.info("Can not " + s + ", train already exists");
        JOptionPane.showMessageDialog(this, Bundle.getMessage("TrainNameExists"), MessageFormat.format(Bundle
                .getMessage("CanNot"), new Object[]{s}), JOptionPane.ERROR_MESSAGE);
    }

    private void enableButtons(boolean enabled) {
        toolMenu.setEnabled(enabled);
        editButton.setEnabled(enabled);
        routeBox.setEnabled(enabled && _train != null && !_train.isBuilt());
        clearButton.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        setButton.setEnabled(enabled);
        saveTrainButton.setEnabled(enabled);
        deleteTrainButton.setEnabled(enabled);
        numEnginesBox.setEnabled(enabled);
        enableCheckboxes(enabled);
        noneRadioButton.setEnabled(enabled);
        fredRadioButton.setEnabled(enabled);
        cabooseRadioButton.setEnabled(enabled);
        roadOptionButton.setEnabled(enabled);
        loadOptionButton.setEnabled(enabled);
        // the inverse!
        addTrainButton.setEnabled(!enabled);
    }

    private void selectCheckboxes(boolean enable) {
        for (int i = 0; i < typeCarCheckBoxes.size(); i++) {
            JCheckBox checkBox = typeCarCheckBoxes.get(i);
            checkBox.setSelected(enable);
            if (_train != null) {
                _train.removePropertyChangeListener(this);
                if (enable) {
                    _train.addTypeName(checkBox.getText());
                } else {
                    _train.deleteTypeName(checkBox.getText());
                }
                _train.addPropertyChangeListener(this);
            }
        }
    }

    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (_train == null) {
            return;
        }
        if (ae.getSource() == numEnginesBox) {
            modelEngineBox.setEnabled(!numEnginesBox.getSelectedItem().equals("0"));
            roadEngineBox.setEnabled(!numEnginesBox.getSelectedItem().equals("0"));
        }
        if (ae.getSource() == modelEngineBox) {
            updateEngineRoadComboBox();
        }
        if (ae.getSource() == routeBox) {
            if (routeBox.isEnabled()) {
                Route route = _train.getRoute();
                if (route != null) {
                    route.removePropertyChangeListener(this);
                }
                Object selected = routeBox.getSelectedItem();
                if (selected != null) {
                    route = (Route) selected;
                    _train.setRoute(route);
                    route.addPropertyChangeListener(this);
                } else {
                    _train.setRoute(null);
                }
                updateLocationCheckboxes();
                packFrame();
            }
        }
    }

    private void enableCheckboxes(boolean enable) {
        for (int i = 0; i < typeCarCheckBoxes.size(); i++) {
            typeCarCheckBoxes.get(i).setEnabled(enable);
        }
        for (int i = 0; i < typeEngineCheckBoxes.size(); i++) {
            typeEngineCheckBoxes.get(i).setEnabled(enable);
        }
    }

    private void addLocationCheckBoxAction(JCheckBox b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                locationCheckBoxActionPerformed(e);
            }
        });
    }

    public void locationCheckBoxActionPerformed(java.awt.event.ActionEvent ae) {
        JCheckBox b = (JCheckBox) ae.getSource();
        log.debug("checkbox change " + b.getText());
        if (_train == null) {
            return;
        }
        String id = b.getName();
        if (b.isSelected()) {
            _train.deleteTrainSkipsLocation(id);
        } else {
            // check to see if skipped location is staging
            if (_train.getRoute().getLocationById(id).getLocation().isStaging()) {
                int result = JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle.getMessage("TrainRouteStaging"),
                        new Object[]{_train.getName(), _train.getRoute().getLocationById(id).getName()}),
                        Bundle.getMessage("TrainRouteNotStaging"), JOptionPane.OK_CANCEL_OPTION);
                if (result == JOptionPane.CANCEL_OPTION) {
                    b.setSelected(true);
                    return; // don't skip staging
                }
            }
            _train.addTrainSkipsLocation(id);
        }
    }

    private void updateRouteComboBox() {
        routeBox.setEnabled(false);
        routeManager.updateComboBox(routeBox);
        if (_train != null) {
            routeBox.setSelectedItem(_train.getRoute());
        }
        routeBox.setEnabled(true);
    }

    private void updateCarTypeCheckboxes() {
        typeCarCheckBoxes.clear();
        typeCarPanelCheckBoxes.removeAll();
        loadCarTypes();
        enableCheckboxes(_train != null);
        typeCarPanelCheckBoxes.revalidate();
        repaint();
    }

    private void loadCarTypes() {
        int numberOfCheckboxes = getNumberOfCheckboxesPerLine(); // number per line
        int x = 0;
        int y = 1; // vertical position in panel
        for (String type : InstanceManager.getDefault(CarTypes.class).getNames()) {
            JCheckBox checkBox = new javax.swing.JCheckBox();
            typeCarCheckBoxes.add(checkBox);
            checkBox.setText(type);
            addTypeCheckBoxAction(checkBox);
            addItemLeft(typeCarPanelCheckBoxes, checkBox, x++, y);
            if (_train != null && _train.acceptsTypeName(type)) {
                checkBox.setSelected(true);
            }
            if (x > numberOfCheckboxes) {
                y++;
                x = 0;
            }
        }

        JPanel p = new JPanel();
        p.add(clearButton);
        p.add(setButton);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridwidth = getNumberOfCheckboxesPerLine() + 1;
        gc.gridy = ++y;
        typeCarPanelCheckBoxes.add(p, gc);

    }

    private void updateEngineTypeCheckboxes() {
        typeEngineCheckBoxes.clear();
        typeEnginePanelCheckBoxes.removeAll();
        loadEngineTypes();
        enableCheckboxes(_train != null);
        typeEnginePanelCheckBoxes.revalidate();
        repaint();
    }

    private void loadEngineTypes() {
        int numberOfCheckboxes = getNumberOfCheckboxesPerLine(); // number per line
        int x = 0;
        int y = 1;
        for (String type : InstanceManager.getDefault(EngineTypes.class).getNames()) {
            JCheckBox checkBox = new javax.swing.JCheckBox();
            typeEngineCheckBoxes.add(checkBox);
            checkBox.setText(type);
            addTypeCheckBoxAction(checkBox);
            addItemLeft(typeEnginePanelCheckBoxes, checkBox, x++, y);
            if (_train != null && _train.acceptsTypeName(type)) {
                checkBox.setSelected(true);
            }
            if (x > numberOfCheckboxes) {
                y++;
                x = 0;
            }
        }
    }

    private void updateRoadComboBoxes() {
        updateCabooseRoadComboBox();
        updateEngineRoadComboBox();
    }

    // update caboose road box based on radio selection
    private void updateCabooseRoadComboBox() {
        roadCabooseBox.removeAllItems();
        roadCabooseBox.addItem(NONE);
        if (noneRadioButton.isSelected()) {
            roadCabooseBox.setEnabled(false);
            return;
        }
        roadCabooseBox.setEnabled(true);
        List<String> roads;
        if (cabooseRadioButton.isSelected()) {
            roads = InstanceManager.getDefault(CarManager.class).getCabooseRoadNames();
        } else {
            roads = InstanceManager.getDefault(CarManager.class).getFredRoadNames();
        }
        for (String road : roads) {
            roadCabooseBox.addItem(road);
        }
        if (_train != null) {
            roadCabooseBox.setSelectedItem(_train.getCabooseRoad());
        }
    }

    private void updateEngineRoadComboBox() {
        String engineModel = (String) modelEngineBox.getSelectedItem();
        if (engineModel == null) {
            return;
        }
        roadEngineBox.removeAllItems();
        roadEngineBox.addItem(NONE);
        List<String> roads = InstanceManager.getDefault(EngineManager.class).getEngineRoadNames(engineModel);
        for (String roadName : roads) {
            roadEngineBox.addItem(roadName);
        }
        if (_train != null) {
            roadEngineBox.setSelectedItem(_train.getEngineRoad());
        }
    }

    private void addTypeCheckBoxAction(JCheckBox b) {
        b.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                typeCheckBoxActionPerformed(e);
            }
        });
    }

    public void typeCheckBoxActionPerformed(java.awt.event.ActionEvent ae) {
        JCheckBox b = (JCheckBox) ae.getSource();
        log.debug("checkbox change " + b.getText());
        if (_train == null) {
            return;
        }
        if (b.isSelected()) {
            _train.addTypeName(b.getText());
        } else {
            _train.deleteTypeName(b.getText());
        }
    }

    // the train's route shown as locations with checkboxes
    private void updateLocationCheckboxes() {
        updateRouteStatus();
        locationCheckBoxes.clear();
        locationPanelCheckBoxes.removeAll();
        int y = 0; // vertical position in panel
        Route route = null;
        if (_train != null) {
            route = _train.getRoute();
        }
        if (route != null) {
            List<RouteLocation> routeList = route.getLocationsBySequenceList();
            for (int i = 0; i < routeList.size(); i++) {
                RouteLocation rl = routeList.get(i);
                JCheckBox checkBox = new javax.swing.JCheckBox();
                locationCheckBoxes.add(checkBox);
                checkBox.setText(rl.toString());
                checkBox.setName(rl.getId());
                addItemLeft(locationPanelCheckBoxes, checkBox, 0, y++);
                Location loc = InstanceManager.getDefault(LocationManager.class).getLocationByName(rl.getName());
                // does the location exist?
                if (loc != null) {
                    // need to listen for name and direction changes
                    loc.removePropertyChangeListener(this);
                    loc.addPropertyChangeListener(this);
                    boolean services = false;
                    // does train direction service location?
                    if ((rl.getTrainDirection() & loc.getTrainDirections()) != 0) {
                        services = true;
                    } // train must service last location or single location
                    else if (i == routeList.size() - 1) {
                        services = true;
                    }
                    // check can drop and pick up, and moves > 0
                    if (services && (rl.isDropAllowed() || rl.isPickUpAllowed()) && rl.getMaxCarMoves() > 0) {
                        checkBox.setSelected(!_train.skipsLocation(rl.getId()));
                    } else {
                        checkBox.setEnabled(false);
                    }
                    addLocationCheckBoxAction(checkBox);
                } else {
                    checkBox.setEnabled(false);
                }
            }
        }
        locationPanelCheckBoxes.revalidate();
    }
    
    private void updateRouteStatus() {
        Route route = null;
        textRouteStatus.setText(NONE); // clear out previous status
        if (_train != null) {
            route = _train.getRoute();
        }
        if (route != null) {
            if (!route.getStatus().equals(Route.OKAY)) {
                textRouteStatus.setText(route.getStatus());
                textRouteStatus.setForeground(Color.RED);
            }
        }
    }

    RouteEditFrame ref;
    protected static final String NEW_LINE = "\n"; // NOI18N

    private void editAddRoute() {
        log.debug("Edit/add route");
        if (ref != null) {
            ref.dispose();
        }
        ref = new RouteEditFrame();
        setChildFrame(ref);
        Route route = null;
        Object selected = routeBox.getSelectedItem();
        if (selected != null) {
            route = (Route) selected;
        }
        // warn user if train is built that they shouldn't edit the train's route
        if (route != null && route.getStatus().equals(Route.TRAIN_BUILT)) {
            // list the built trains for this route
            StringBuffer buf = new StringBuffer(Bundle.getMessage("DoNotModifyRoute"));
            for (Train train : InstanceManager.getDefault(TrainManager.class).getTrainsByIdList()) {
                if (train.getRoute() == route && train.isBuilt()) {
                    buf.append(NEW_LINE +
                            MessageFormat.format(Bundle.getMessage("TrainIsBuilt"),
                                    new Object[]{train.getName(), route.getName()}));
                }
            }
            JOptionPane.showMessageDialog(this, buf.toString(), Bundle.getMessage("BuiltTrain"),
                    JOptionPane.WARNING_MESSAGE);
        }
        ref.initComponents(route, _train);
    }

    private void updateDepartureTime() {
        hourBox.setSelectedItem(_train.getDepartureTimeHour());
        minuteBox.setSelectedItem(_train.getDepartureTimeMinute());
        // check to see if route has a departure time from the 1st location
        RouteLocation rl = _train.getTrainDepartsRouteLocation();
        if (rl != null && !rl.getDepartureTime().equals(NONE)) {
            hourBox.setEnabled(false);
            minuteBox.setEnabled(false);
        } else {
            hourBox.setEnabled(true);
            minuteBox.setEnabled(true);
        }
    }

    private void updateRoadAndLoadStatus() {
        if (_train != null) {
            // road options
            if (_train.getRoadOption().equals(Train.ALL_ROADS)) {
                roadOptionButton.setText(Bundle.getMessage("AcceptAll"));
            } else if (_train.getRoadOption().equals(Train.INCLUDE_LOADS)) {
                roadOptionButton.setText(Bundle.getMessage("AcceptOnly") + " " + _train.getRoadNames().length + " "
                        + Bundle.getMessage("Roads"));
            } else {
                roadOptionButton.setText(Bundle.getMessage("Exclude") + " " + _train.getRoadNames().length + " "
                        + Bundle.getMessage("Roads"));
            }
            // load options
            if (_train.getLoadOption().equals(Train.ALL_ROADS)) {
                loadOptionButton.setText(Bundle.getMessage("AcceptAll"));
            } else if (_train.getLoadOption().equals(Train.INCLUDE_LOADS)) {
                loadOptionButton.setText(Bundle.getMessage("AcceptOnly") + " " + _train.getLoadNames().length + " "
                        + Bundle.getMessage("Loads"));
            } else {
                loadOptionButton.setText(Bundle.getMessage("Exclude") + " " + _train.getLoadNames().length + " "
                        + Bundle.getMessage("Loads"));
            }
            if (!_train.getRoadOption().equals(Train.ALL_ROADS) || !_train.getLoadOption().equals(Train.ALL_LOADS)) {
                roadAndLoadStatusPanel.setVisible(true);
            }
        }
    }

    private void packFrame() {
        setVisible(false);
        setMinimumSize(new Dimension(Control.panelWidth500, Control.panelHeight500));
        pack();
        setVisible(true);
    }

    List<Frame> children = new ArrayList<>();

    public void setChildFrame(Frame frame) {
        if (children.contains(frame)) {
            return;
        }
        children.add(frame);
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(LocationManager.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineModels.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarTypes.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(CarRoads.class).removePropertyChangeListener(this);
        routeManager.removePropertyChangeListener(this);
        for (Frame frame : children) {
            frame.dispose();
        }
        if (_train != null) {
            _train.removePropertyChangeListener(this);
            Route route = _train.getRoute();
            if (route != null) {
                route.removePropertyChangeListener(this);
                for (RouteLocation rl : route.getLocationsBySequenceList()) {
                    Location loc = rl.getLocation();
                    if (loc != null) {
                        loc.removePropertyChangeListener(this);
                    }
                }
            }
        }
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e.getNewValue()); // NOI18N
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Train.TYPES_CHANGED_PROPERTY)) {
            updateCarTypeCheckboxes();
        }
        if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_CHANGED_PROPERTY)) {
            updateEngineTypeCheckboxes();
        }
        if (e.getPropertyName().equals(RouteManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateRouteComboBox();
        }
        if (e.getPropertyName().equals(Route.LISTCHANGE_CHANGED_PROPERTY)
                || e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Location.TRAINDIRECTION_CHANGED_PROPERTY)) {
            updateLocationCheckboxes();
            packFrame();
        }
        if (e.getPropertyName().equals(CarRoads.CARROADS_CHANGED_PROPERTY)) {
            updateRoadComboBoxes();
        }
        if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_CHANGED_PROPERTY)) {
            InstanceManager.getDefault(EngineModels.class).updateComboBox(modelEngineBox);
            modelEngineBox.insertItemAt(NONE, 0);
            modelEngineBox.setSelectedIndex(0);
            if (_train != null) {
                modelEngineBox.setSelectedItem(_train.getEngineModel());
            }
        }
        if (e.getPropertyName().equals(Train.DEPARTURETIME_CHANGED_PROPERTY)) {
            updateDepartureTime();
        }
        if (e.getPropertyName().equals(Train.TRAIN_ROUTE_CHANGED_PROPERTY) && _train != null) {
            routeBox.setSelectedItem(_train.getRoute());
        }
        if (e.getPropertyName().equals(Route.ROUTE_STATUS_CHANGED_PROPERTY)) {
            enableButtons(_train != null);
            updateRouteStatus();
        }
        if (e.getPropertyName().equals(Train.ROADS_CHANGED_PROPERTY)
                || e.getPropertyName().equals(Train.LOADS_CHANGED_PROPERTY)) {
            updateRoadAndLoadStatus();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainEditFrame.class);
}
