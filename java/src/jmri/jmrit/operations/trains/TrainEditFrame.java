package jmri.jmrit.operations.trains;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.*;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.*;
import jmri.jmrit.operations.routes.*;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.tools.*;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user edit of a train
 *
 * @author Dan Boudreau Copyright (C) 2008, 2011, 2012, 2013, 2014
 */
public class TrainEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
    RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);

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
    JTextField trainNameTextField = new JTextField(Control.max_len_string_train_name);
    JTextField trainDescriptionTextField = new JTextField(30);

    // text area
    JTextArea commentTextArea = new JTextArea(2, 70);
    JScrollPane commentScroller = new JScrollPane(commentTextArea);
    JColorChooser commentColorChooser = new JColorChooser(Color.black);

    // for padding out panel
    JLabel space1 = new JLabel(" "); // before hour
    JLabel space2 = new JLabel(" "); // between hour and minute
    JLabel space3 = new JLabel(" "); // after minute
    JLabel space4 = new JLabel(" "); // between route and edit
    JLabel space5 = new JLabel(" "); // after edit

    // combo boxes
    JComboBox<String> hourBox = new JComboBox<>();
    JComboBox<String> minuteBox = new JComboBox<>();
    JComboBox<Route> routeBox = routeManager.getComboBox();
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
        hourBox.setPrototypeDisplayValue("0000"); // needed for font size 9
        minuteBox.setPrototypeDisplayValue("0000");
        for (int i = 0; i < 24; i++) {
            if (i < 10) {
                hourBox.addItem("0" + Integer.toString(i));
            } else {
                hourBox.addItem(Integer.toString(i));
            }
        }
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
        roadOptionButton.addActionListener(new TrainRoadOptionsAction(this));

        JPanel pLoadOption = new JPanel();
        pLoadOption.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("LoadOption")));
        pLoadOption.add(loadOptionButton);
        loadOptionButton.addActionListener(new TrainLoadOptionsAction(this));

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
        numEnginesBox.setMinimumSize(new Dimension(100, 20));
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

        JPanel trainLastCar = new JPanel();
        trainLastCar.setLayout(new GridBagLayout());
        trainLastCar.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainLastCar")));

        addItem(trainLastCar, noneRadioButton, 2, 2);
        noneRadioButton.setToolTipText(Bundle.getMessage("TipNoCabooseOrFRED"));
        addItem(trainLastCar, fredRadioButton, 3, 2);
        fredRadioButton.setToolTipText(Bundle.getMessage("TipFRED"));
        addItem(trainLastCar, cabooseRadioButton, 4, 2);
        cabooseRadioButton.setToolTipText(Bundle.getMessage("TipCaboose"));
        addItem(trainLastCar, textRoad3, 5, 2);
        roadCabooseBox.setMinimumSize(new Dimension(120, 20));
        roadCabooseBox.setToolTipText(Bundle.getMessage("RoadCabooseTip"));
        addItem(trainLastCar, roadCabooseBox, 6, 2);
        group.add(noneRadioButton);
        group.add(cabooseRadioButton);
        group.add(fredRadioButton);
        noneRadioButton.setSelected(true);

        // row 13 comment
        JPanel pC = new JPanel();
        pC.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        pC.setLayout(new GridBagLayout());
        addItem(pC, commentScroller, 1, 0);
        if (_train != null) {
            addItem(pC, OperationsPanel.getColorChooserPanel(_train.getCommentWithColor(), commentColorChooser), 2, 0);
        } else {
            addItem(pC, OperationsPanel.getColorChooserPanel("", commentColorChooser), 2, 0);
        }

        // adjust text area width based on window size less color chooser
        Dimension d = new Dimension(getPreferredSize().width - 100, getPreferredSize().height);
        adjustTextAreaColumnWidth(commentScroller, commentTextArea, d);

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
        getContentPane().add(trainLastCar);
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
            commentTextArea.setText(TrainCommon.getTextColorString(_train.getCommentWithColor()));
            cabooseRadioButton.setSelected(_train.isCabooseNeeded());
            fredRadioButton.setSelected(_train.isFredNeeded());
            updateDepartureTime();
            enableButtons(true);
            // listen for train changes
            _train.addPropertyChangeListener(this);

            Route route = _train.getRoute();
            if (route != null) {
                if (_train.getTrainDepartsRouteLocation() != null &&
                        _train.getTrainDepartsRouteLocation().getLocation() != null &&
                        !_train.getTrainDepartsRouteLocation().getLocation().isStaging())
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

        initMinimumSize(new Dimension(Control.panelWidth600, Control.panelHeight600));
    }

    private void loadToolMenu(JMenu toolMenu) {
        toolMenu.removeAll();
        // first 5 menu items will also close when the edit train window closes
        toolMenu.add(new TrainEditBuildOptionsAction(this));
        toolMenu.add(new TrainLoadOptionsAction(this));
        toolMenu.add(new TrainRoadOptionsAction(this));
        toolMenu.add(new TrainManifestOptionAction(this));
        toolMenu.add(new TrainCopyAction(_train));
        toolMenu.addSeparator();
        toolMenu.add(new TrainScriptAction(this));
        toolMenu.add(new TrainConductorAction(_train));
        toolMenu.addSeparator();
        toolMenu.add(new TrainByCarTypeAction(_train));
        toolMenu.addSeparator();
        toolMenu.add(new PrintTrainAction(false, _train));
        toolMenu.add(new PrintTrainAction(true, _train));
        toolMenu.add(new PrintTrainManifestAction(false, _train));
        toolMenu.add(new PrintTrainManifestAction(true, _train));
        toolMenu.add(new PrintTrainBuildReportAction(false, _train));
        toolMenu.add(new PrintTrainBuildReportAction(true, _train));
        toolMenu.add(new PrintSavedTrainManifestAction(false, _train));
        toolMenu.add(new PrintSavedTrainManifestAction(true, _train));
        toolMenu.add(new PrintSavedBuildReportAction(false, _train));
        toolMenu.add(new PrintSavedBuildReportAction(true, _train));
    }

    // Save, Delete, Add, Edit, Reset, Set, Clear
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        Train train = trainManager.getTrainByName(trainNameTextField.getText().trim());
        if (ae.getSource() == saveTrainButton) {
            log.debug("train save button activated");
            if (_train == null && train == null) {
                saveNewTrain(); // this can't happen, Save button is disabled
            } else {
                if (train != null && train != _train) {
                    reportTrainExists(Bundle.getMessage("save"));
                    return;
                }
                // check to see if user supplied a route
                if (!checkRoute() || !saveTrain()) {
                    return;
                }
            }
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
        if (ae.getSource() == deleteTrainButton) {
            log.debug("train delete button activated");
            if (train == null) {
                return;
            }
            if (!_train.reset()) {
                JmriJOptionPane.showMessageDialog(this,
                        Bundle.getMessage("TrainIsInRoute",
                                train.getTrainTerminatesName()),
                        Bundle.getMessage("CanNotDeleteTrain"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            if (JmriJOptionPane.showConfirmDialog(this,
                    Bundle.getMessage("deleteMsg", train.getName()),
                    Bundle.getMessage("deleteTrain"), JmriJOptionPane.YES_NO_OPTION) != JmriJOptionPane.YES_OPTION) {
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
                    JmriJOptionPane.showMessageDialog(this,
                            Bundle.getMessage("TrainIsInRoute",
                                    _train.getTrainTerminatesName()),
                            Bundle.getMessage("CanNotResetTrain"), JmriJOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        if (_train != null) {
            if (ae.getSource() == noneRadioButton ||
                    ae.getSource() == cabooseRadioButton ||
                    ae.getSource() == fredRadioButton) {
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

    private boolean saveTrain() {
        if (!checkName(Bundle.getMessage("save"))) {
            return false;
        }
        if (!checkModel() || !checkEngineRoad() || !checkCabooseRoad()) {
            return false;
        }
        if (!_train.getName().equals(trainNameTextField.getText().trim()) ||
                !_train.getRawDescription().equals(trainDescriptionTextField.getText()) ||
                !_train.getCommentWithColor().equals(
                        TrainCommon.formatColorString(commentTextArea.getText(), commentColorChooser.getColor()))) {
            _train.setModified(true);
        }
        _train.setDepartureTime(hourBox.getSelectedItem().toString(), minuteBox.getSelectedItem().toString());
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
        _train.setName(trainNameTextField.getText().trim());
        _train.setDescription(trainDescriptionTextField.getText());
        _train.setComment(TrainCommon.formatColorString(commentTextArea.getText(), commentColorChooser.getColor()));
        // save train file
        OperationsXml.save();
        return true;
    }

    /**
     *
     * @return true if name isn't too long and is at least one character
     */
    private boolean checkName(String s) {
        String trainName = trainNameTextField.getText().trim();
        if (trainName.isEmpty()) {
            log.debug("Must enter a train name");
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"),
                    Bundle.getMessage("CanNot", s), JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (trainName.length() > Control.max_len_string_train_name) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("TrainNameLess",
                            Control.max_len_string_train_name + 1),
                    Bundle.getMessage("CanNot", s), JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!OperationsXml.checkFileName(trainName)) { // NOI18N
            log.error("Train name must not contain reserved characters");
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NameResChar") + NEW_LINE + Bundle.getMessage("ReservedChar"),
                    Bundle.getMessage("CanNot", s), JmriJOptionPane.ERROR_MESSAGE);
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
        if (!_train.isTypeNameAccepted(type)) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("TrainModelService", model, type),
                    Bundle.getMessage("CanNot", Bundle.getMessage("save")),
                    JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (roadEngineBox.getItemCount() == 1) {
            log.debug("No locos available that match the model selected!");
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("NoLocosModel", model),
                    Bundle.getMessage("TrainWillNotBuild", _train.getName()),
                    JmriJOptionPane.WARNING_MESSAGE);
        }
        return true;
    }

    private boolean checkEngineRoad() {
        String road = (String) roadEngineBox.getSelectedItem();
        if (numEnginesBox.getSelectedItem().equals("0") || road.equals(NONE)) {
            return true;
        }
        if (!road.equals(NONE) && !_train.isLocoRoadNameAccepted(road)) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("TrainNotThisRoad", _train.getName(), road),
                    Bundle.getMessage("TrainWillNotBuild", _train.getName()),
                    JmriJOptionPane.WARNING_MESSAGE);
            return false;
        }
        for (RollingStock rs : InstanceManager.getDefault(EngineManager.class).getList()) {
            if (!_train.isTypeNameAccepted(rs.getTypeName())) {
                continue;
            }
            if (rs.getRoadName().equals(road)) {
                return true;
            }
        }
        JmriJOptionPane.showMessageDialog(this,
                Bundle.getMessage("NoLocoRoad", road),
                Bundle.getMessage("TrainWillNotBuild", _train.getName()),
                JmriJOptionPane.WARNING_MESSAGE);
        return false; // couldn't find a loco with the selected road
    }

    private boolean checkCabooseRoad() {
        String road = (String) roadCabooseBox.getSelectedItem();
        if (!road.equals(NONE) && cabooseRadioButton.isSelected() && !_train.isCabooseRoadNameAccepted(road)) {
            JmriJOptionPane.showMessageDialog(this,
                    Bundle.getMessage("TrainNotCabooseRoad", _train.getName(), road),
                    Bundle.getMessage("TrainWillNotBuild", _train.getName()),
                    JmriJOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean checkRoute() {
        if (_train.getRoute() == null) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("TrainNeedsRoute"), Bundle.getMessage("TrainNoRoute"),
                    JmriJOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;

    }

    private void reportTrainExists(String s) {
        log.debug("Can not {}, train already exists", s);
        JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("TrainNameExists"),
                Bundle.getMessage("CanNot", s), JmriJOptionPane.ERROR_MESSAGE);
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
                updateDepartureTime();
                pack();
                repaint();
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
        log.debug("checkbox change {}", b.getText());
        if (_train == null) {
            return;
        }
        String id = b.getName();
        if (b.isSelected()) {
            _train.deleteTrainSkipsLocation(id);
        } else {
            // check to see if skipped location is staging
            if (_train.getRoute().getLocationById(id).getLocation().isStaging()) {
                int result = JmriJOptionPane.showConfirmDialog(this,
                        Bundle.getMessage("TrainRouteStaging",
                                _train.getName(), _train.getRoute().getLocationById(id).getName()),
                        Bundle.getMessage("TrainRouteNotStaging"), JmriJOptionPane.OK_CANCEL_OPTION);
                if (result != JmriJOptionPane.OK_OPTION ) {
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
            if (_train != null && _train.isTypeNameAccepted(type)) {
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
            if (_train != null && _train.isTypeNameAccepted(type)) {
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
        OperationsPanel.padComboBox(roadCabooseBox);
    }

    private void updateEngineRoadComboBox() {
        String engineModel = (String) modelEngineBox.getSelectedItem();
        if (engineModel == null) {
            return;
        }
        InstanceManager.getDefault(EngineManager.class).updateEngineRoadComboBox(engineModel, roadEngineBox);
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
        log.debug("checkbox change {}", b.getText());
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
            for (RouteLocation rl : routeList) {
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
                    else if (_train.isLocalSwitcher() || rl == _train.getTrainTerminatesRouteLocation()) {
                        services = true;
                    }
                    // check can drop and pick up, and moves > 0
                    if (services && (rl.isDropAllowed() || rl.isPickUpAllowed()) && rl.getMaxCarMoves() > 0) {
                        checkBox.setSelected(!_train.isLocationSkipped(rl.getId()));
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
                            Bundle.getMessage("TrainIsBuilt",
                                    train.getName(), route.getName()));
                }
            }
            JmriJOptionPane.showMessageDialog(this, buf.toString(), Bundle.getMessage("BuiltTrain"),
                    JmriJOptionPane.WARNING_MESSAGE);
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
            if (_train.getCarRoadOption().equals(Train.INCLUDE_ROADS)) {
                roadOptionButton.setText(Bundle.getMessage(
                        "AcceptOnly") + " " + _train.getCarRoadNames().length + " " + Bundle.getMessage("RoadsCar"));
            } else if (_train.getCarRoadOption().equals(Train.EXCLUDE_ROADS)) {
                roadOptionButton.setText(Bundle.getMessage(
                        "Exclude") + " " + _train.getCarRoadNames().length + " " + Bundle.getMessage("RoadsCar"));
            } else if (_train.getCabooseRoadOption().equals(Train.INCLUDE_ROADS)) {
                roadOptionButton.setText(Bundle.getMessage(
                        "AcceptOnly") +
                        " " +
                        _train.getCabooseRoadNames().length +
                        " " +
                        Bundle.getMessage("RoadsCaboose"));
            } else if (_train.getCabooseRoadOption().equals(Train.EXCLUDE_ROADS)) {
                roadOptionButton.setText(Bundle.getMessage(
                        "Exclude") +
                        " " +
                        _train.getCabooseRoadNames().length +
                        " " +
                        Bundle.getMessage("RoadsCaboose"));
            } else if (_train.getLocoRoadOption().equals(Train.INCLUDE_ROADS)) {
                roadOptionButton.setText(Bundle.getMessage(
                        "AcceptOnly") + " " + _train.getLocoRoadNames().length + " " + Bundle.getMessage("RoadsLoco"));
            } else if (_train.getLocoRoadOption().equals(Train.EXCLUDE_ROADS)) {
                roadOptionButton.setText(Bundle.getMessage(
                        "Exclude") + " " + _train.getLocoRoadNames().length + " " + Bundle.getMessage("RoadsLoco"));
            } else {
                roadOptionButton.setText(Bundle.getMessage("AcceptAll"));
            }
            // load options
            if (_train.getLoadOption().equals(Train.ALL_LOADS)) {
                loadOptionButton.setText(Bundle.getMessage("AcceptAll"));
            } else if (_train.getLoadOption().equals(Train.INCLUDE_LOADS)) {
                loadOptionButton.setText(Bundle.getMessage(
                        "AcceptOnly") + " " + _train.getLoadNames().length + " " + Bundle.getMessage("Loads"));
            } else {
                loadOptionButton.setText(Bundle.getMessage(
                        "Exclude") + " " + _train.getLoadNames().length + " " + Bundle.getMessage("Loads"));
            }
            if (!_train.getCarRoadOption().equals(Train.ALL_ROADS) ||
                    !_train.getCabooseRoadOption().equals(Train.ALL_ROADS) ||
                    !_train.getLocoRoadOption().equals(Train.ALL_ROADS) ||
                    !_train.getLoadOption().equals(Train.ALL_LOADS)) {
                roadAndLoadStatusPanel.setVisible(true);
            }
        }
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
            log.debug("Property change ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(),
                    e.getNewValue()); // NOI18N
        }
        if (e.getPropertyName().equals(CarTypes.CARTYPES_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.TYPES_CHANGED_PROPERTY)) {
            updateCarTypeCheckboxes();
        }
        if (e.getPropertyName().equals(EngineTypes.ENGINETYPES_CHANGED_PROPERTY)) {
            updateEngineTypeCheckboxes();
        }
        if (e.getPropertyName().equals(RouteManager.LISTLENGTH_CHANGED_PROPERTY)) {
            updateRouteComboBox();
        }
        if (e.getPropertyName().equals(Route.LISTCHANGE_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.NAME_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.TRAIN_DIRECTION_CHANGED_PROPERTY)) {
            updateLocationCheckboxes();
            pack();
            repaint();
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
        if (e.getPropertyName().equals(Train.ROADS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.LOADS_CHANGED_PROPERTY)) {
            updateRoadAndLoadStatus();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainEditFrame.class);
}
