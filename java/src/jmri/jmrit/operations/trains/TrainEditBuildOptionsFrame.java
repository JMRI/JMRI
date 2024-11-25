package jmri.jmrit.operations.trains;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame for user edit of a train's build options
 *
 * @author Dan Boudreau Copyright (C) 2010, 2012, 2013, 2024
 */
public class TrainEditBuildOptionsFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    Train _train = null;

    JPanel panelOwnerNames = new JPanel();
    JPanel panelBuilt = new JPanel();
    JPanel panelTrainReq1 = new JPanel();
    JPanel panelTrainReq2 = new JPanel();

    JScrollPane ownerPane;
    JScrollPane builtPane;
    JScrollPane trainReq1Pane;
    JScrollPane trainReq2Pane;

    JPanel engine1Option = new JPanel();
    JPanel engine1DropOption = new JPanel();
    JPanel engine1caboose = new JPanel();

    JPanel engine2Option = new JPanel();
    JPanel engine2DropOption = new JPanel();
    JPanel engine2caboose = new JPanel();

    // labels
    JLabel trainName = new JLabel();
    JLabel trainDescription = new JLabel();
    JLabel before = new JLabel(Bundle.getMessage("Before"));
    JLabel after = new JLabel(Bundle.getMessage("After"));

    // major buttons
    JButton addOwnerButton = new JButton(Bundle.getMessage("AddOwner"));
    JButton deleteOwnerButton = new JButton(Bundle.getMessage("DeleteOwner"));
    JButton saveTrainButton = new JButton(Bundle.getMessage("SaveTrain"));

    // radio buttons
    JRadioButton ownerNameAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
    JRadioButton ownerNameInclude = new JRadioButton(Bundle.getMessage("AcceptOnly"));
    JRadioButton ownerNameExclude = new JRadioButton(Bundle.getMessage("Exclude"));

    JRadioButton builtDateAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
    JRadioButton builtDateAfter = new JRadioButton(Bundle.getMessage("After"));
    JRadioButton builtDateBefore = new JRadioButton(Bundle.getMessage("Before"));
    JRadioButton builtDateRange = new JRadioButton(Bundle.getMessage("Range"));

    ButtonGroup ownerGroup = new ButtonGroup();
    ButtonGroup builtGroup = new ButtonGroup();

    // train requirements 1st set
    JRadioButton none1 = new JRadioButton(Bundle.getMessage("None"));
    JRadioButton change1Engine = new JRadioButton(Bundle.getMessage("EngineChange"));
    JRadioButton add1Engine = new JRadioButton(Bundle.getMessage("AddEngines"));
    JRadioButton remove1Engine = new JRadioButton(Bundle.getMessage("RemoveEngines"));
    JRadioButton modify1Caboose = new JRadioButton(Bundle.getMessage("ChangeCaboose"));
    JRadioButton helper1Service = new JRadioButton(Bundle.getMessage("HelperService"));
    JRadioButton remove1Caboose = new JRadioButton(Bundle.getMessage("RemoveCaboose"));
    JRadioButton keep1Caboose = new JRadioButton(Bundle.getMessage("KeepCaboose"));
    JRadioButton change1Caboose = new JRadioButton(Bundle.getMessage("ChangeCaboose"));

    ButtonGroup trainReq1Group = new ButtonGroup();
    ButtonGroup cabooseOption1Group = new ButtonGroup();

    // train requirements 2nd set
    JRadioButton none2 = new JRadioButton(Bundle.getMessage("None"));
    JRadioButton change2Engine = new JRadioButton(Bundle.getMessage("EngineChange"));
    JRadioButton add2Engine = new JRadioButton(Bundle.getMessage("AddEngines"));
    JRadioButton remove2Engine = new JRadioButton(Bundle.getMessage("RemoveEngines"));
    JRadioButton modify2Caboose = new JRadioButton(Bundle.getMessage("ChangeCaboose"));
    JRadioButton helper2Service = new JRadioButton(Bundle.getMessage("HelperService"));
    JRadioButton remove2Caboose = new JRadioButton(Bundle.getMessage("RemoveCaboose"));
    JRadioButton keep2Caboose = new JRadioButton(Bundle.getMessage("KeepCaboose"));
    JRadioButton change2Caboose = new JRadioButton(Bundle.getMessage("ChangeCaboose"));

    ButtonGroup trainReq2Group = new ButtonGroup();
    ButtonGroup cabooseOption2Group = new ButtonGroup();

    // check boxes
    JCheckBox buildNormalCheckBox = new JCheckBox(Bundle.getMessage("NormalModeWhenBuilding"));
    JCheckBox sendToTerminalCheckBox = new JCheckBox();
    JCheckBox returnStagingCheckBox = new JCheckBox(Bundle.getMessage("AllowCarsToReturn"));
    JCheckBox allowLocalMovesCheckBox = new JCheckBox(Bundle.getMessage("AllowLocalMoves"));
    JCheckBox allowThroughCarsCheckBox = new JCheckBox(Bundle.getMessage("AllowThroughCars"));
    JCheckBox serviceAllCarsCheckBox = new JCheckBox(Bundle.getMessage("ServiceAllCars"));
    JCheckBox sendCustomStagngCheckBox = new JCheckBox(Bundle.getMessage("SendCustomToStaging"));
    JCheckBox buildConsistCheckBox = new JCheckBox(Bundle.getMessage("BuildConsist"));

    // text field
    JTextField builtAfterTextField = new JTextField(10);
    JTextField builtBeforeTextField = new JTextField(10);

    // combo boxes
    JComboBox<String> ownerBox = InstanceManager.getDefault(CarOwners.class).getComboBox();

    // train requirements 1st set
    JComboBox<RouteLocation> routePickup1Box = new JComboBox<>();
    JComboBox<RouteLocation> routeDrop1Box = new JComboBox<>();
    JComboBox<String> roadCaboose1Box = new JComboBox<>();
    JComboBox<String> roadEngine1Box = InstanceManager.getDefault(CarRoads.class).getComboBox();
    JComboBox<String> modelEngine1Box = InstanceManager.getDefault(EngineModels.class).getComboBox();
    JComboBox<String> numEngines1Box = new JComboBox<>();

    // train requirements 2nd set
    JComboBox<RouteLocation> routePickup2Box = new JComboBox<>();
    JComboBox<RouteLocation> routeDrop2Box = new JComboBox<>();
    JComboBox<String> roadCaboose2Box = new JComboBox<>();
    JComboBox<String> roadEngine2Box = InstanceManager.getDefault(CarRoads.class).getComboBox();
    JComboBox<String> modelEngine2Box = InstanceManager.getDefault(EngineModels.class).getComboBox();
    JComboBox<String> numEngines2Box = new JComboBox<>();

    public static final String DISPOSE = "dispose"; // NOI18N

    public TrainEditBuildOptionsFrame() {
        super(Bundle.getMessage("MenuItemBuildOptions"));
    }

    public void initComponents(TrainEditFrame parent) {

        ownerPane = new JScrollPane(panelOwnerNames);
        ownerPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        ownerPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("OwnersTrain")));

        builtPane = new JScrollPane(panelBuilt);
        builtPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        builtPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BuiltDatesTrain")));

        trainReq1Pane = new JScrollPane(panelTrainReq1);
        trainReq1Pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        trainReq1Pane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainRequires")));

        trainReq2Pane = new JScrollPane(panelTrainReq2);
        trainReq2Pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        trainReq2Pane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainRequires")));

        parent.setChildFrame(this);
        _train = parent._train;

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Layout the panel by rows
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
        p1.setMaximumSize(new Dimension(2000, 250));

        // Layout the panel by rows
        // row 1a
        JPanel pName = new JPanel();
        pName.setLayout(new GridBagLayout());
        pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Name")));
        addItem(pName, trainName, 0, 0);

        // row 1b
        JPanel pDesc = new JPanel();
        pDesc.setLayout(new GridBagLayout());
        pDesc.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Description")));
        addItem(pDesc, trainDescription, 0, 0);

        p1.add(pName);
        p1.add(pDesc);

        // row 2
        JPanel pOption = new JPanel();
        pOption.setLayout(new GridBagLayout());
        pOption.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Options")));
        addItemLeft(pOption, buildNormalCheckBox, 0, 0);
        addItemLeft(pOption, sendToTerminalCheckBox, 1, 0);
        addItemLeft(pOption, returnStagingCheckBox, 0, 1);
        addItemLeft(pOption, allowLocalMovesCheckBox, 1, 1);
        addItemLeft(pOption, allowThroughCarsCheckBox, 0, 2);
        addItemLeft(pOption, serviceAllCarsCheckBox, 1, 2);
        addItemLeft(pOption, sendCustomStagngCheckBox, 0, 3);
        addItemLeft(pOption, buildConsistCheckBox, 1, 3);
        pOption.setMaximumSize(new Dimension(2000, 250));

        buildNormalCheckBox.setEnabled(Setup.isBuildAggressive());
        returnStagingCheckBox.setEnabled(false); // only enable if train departs and returns to same staging loc

        // row 7
        panelOwnerNames.setLayout(new GridBagLayout());
        ownerGroup.add(ownerNameAll);
        ownerGroup.add(ownerNameInclude);
        ownerGroup.add(ownerNameExclude);

        // row 9
        panelBuilt.setLayout(new GridBagLayout());
        builtAfterTextField.setToolTipText(Bundle.getMessage("EnterYearTip"));
        builtBeforeTextField.setToolTipText(Bundle.getMessage("EnterYearTip"));
        addItem(panelBuilt, builtDateAll, 0, 0);
        addItem(panelBuilt, builtDateAfter, 1, 0);
        addItem(panelBuilt, builtDateBefore, 2, 0);
        addItem(panelBuilt, builtDateRange, 3, 0);
        addItem(panelBuilt, after, 1, 1);
        addItem(panelBuilt, builtAfterTextField, 2, 1);
        addItem(panelBuilt, before, 1, 2);
        addItem(panelBuilt, builtBeforeTextField, 2, 2);
        builtGroup.add(builtDateAll);
        builtGroup.add(builtDateAfter);
        builtGroup.add(builtDateBefore);
        builtGroup.add(builtDateRange);

        // row 11
        panelTrainReq1.setLayout(new BoxLayout(panelTrainReq1, BoxLayout.Y_AXIS));

        JPanel trainOption1 = new JPanel();
        trainOption1.setLayout(new GridBagLayout());
        addItem(trainOption1, none1, 0, 0);
        addItem(trainOption1, change1Engine, 1, 0);
        addItem(trainOption1, add1Engine, 2, 0);
        addItem(trainOption1, remove1Engine, 3, 0);
        addItem(trainOption1, modify1Caboose, 1, 1);
        addItem(trainOption1, helper1Service, 2, 1);
        panelTrainReq1.add(trainOption1);

        trainReq1Group.add(none1);
        trainReq1Group.add(change1Engine);
        trainReq1Group.add(add1Engine);
        trainReq1Group.add(remove1Engine);
        trainReq1Group.add(modify1Caboose);
        trainReq1Group.add(helper1Service);

        // engine options
        engine1Option.setLayout(new GridBagLayout());

        for (int i = 0; i < Setup.getMaxNumberEngines() + 1; i++) {
            numEngines1Box.addItem(Integer.toString(i));
        }
        numEngines1Box.addItem(Train.AUTO_HPT);
        numEngines1Box.setMinimumSize(new Dimension(50, 20));
        modelEngine1Box.insertItemAt("", 0);
        modelEngine1Box.setSelectedIndex(0);
        modelEngine1Box.setMinimumSize(new Dimension(120, 20));
        modelEngine1Box.setToolTipText(Bundle.getMessage("ModelEngineTip"));
        roadEngine1Box.insertItemAt("", 0);
        roadEngine1Box.setSelectedIndex(0);
        roadEngine1Box.setMinimumSize(new Dimension(120, 20));
        roadEngine1Box.setToolTipText(Bundle.getMessage("RoadEngineTip"));
        panelTrainReq1.add(engine1Option);

        // caboose options
        engine1caboose.setLayout(new GridBagLayout());
        engine1caboose.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ChangeCaboose")));
        roadCaboose1Box.setMinimumSize(new Dimension(120, 20));
        roadCaboose1Box.setToolTipText(Bundle.getMessage("RoadCabooseTip"));
        panelTrainReq1.add(engine1caboose);

        cabooseOption1Group.add(remove1Caboose);
        cabooseOption1Group.add(keep1Caboose);
        cabooseOption1Group.add(change1Caboose);

        // drop engine panel
        addItem(engine1DropOption, new JLabel(Bundle.getMessage("DropEnginesAt")), 0, 0);
        addItem(engine1DropOption, routeDrop1Box, 1, 0);
        panelTrainReq1.add(engine1DropOption);

        // row 13
        panelTrainReq2.setLayout(new BoxLayout(panelTrainReq2, BoxLayout.Y_AXIS));

        JPanel trainOption2 = new JPanel();
        trainOption2.setLayout(new GridBagLayout());
        addItem(trainOption2, none2, 0, 0);
        addItem(trainOption2, change2Engine, 1, 0);
        addItem(trainOption2, add2Engine, 2, 0);
        addItem(trainOption2, remove2Engine, 3, 0);
        addItem(trainOption2, modify2Caboose, 1, 1);
        addItem(trainOption2, helper2Service, 2, 1);
        panelTrainReq2.add(trainOption2);

        trainReq2Group.add(none2);
        trainReq2Group.add(change2Engine);
        trainReq2Group.add(add2Engine);
        trainReq2Group.add(remove2Engine);
        trainReq2Group.add(modify2Caboose);
        trainReq2Group.add(helper2Service);

        // engine options
        engine2Option.setLayout(new GridBagLayout());

        for (int i = 0; i < Setup.getMaxNumberEngines() + 1; i++) {
            numEngines2Box.addItem(Integer.toString(i));
        }
        numEngines2Box.addItem(Train.AUTO_HPT);
        numEngines2Box.setMinimumSize(new Dimension(50, 20));
        modelEngine2Box.insertItemAt("", 0);
        modelEngine2Box.setSelectedIndex(0);
        modelEngine2Box.setMinimumSize(new Dimension(120, 20));
        modelEngine2Box.setToolTipText(Bundle.getMessage("ModelEngineTip"));
        roadEngine2Box.insertItemAt("", 0);
        roadEngine2Box.setSelectedIndex(0);
        roadEngine2Box.setMinimumSize(new Dimension(120, 20));
        roadEngine2Box.setToolTipText(Bundle.getMessage("RoadEngineTip"));
        panelTrainReq2.add(engine2Option);

        // caboose options
        engine2caboose.setLayout(new GridBagLayout());
        engine2caboose.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ChangeCaboose")));
        roadCaboose2Box.setMinimumSize(new Dimension(120, 20));
        roadCaboose2Box.setToolTipText(Bundle.getMessage("RoadCabooseTip"));
        panelTrainReq2.add(engine2caboose);

        cabooseOption2Group.add(remove2Caboose);
        cabooseOption2Group.add(keep2Caboose);
        cabooseOption2Group.add(change2Caboose);

        // drop engine panel
        addItem(engine2DropOption, new JLabel(Bundle.getMessage("DropEnginesAt")), 0, 0);
        addItem(engine2DropOption, routeDrop2Box, 1, 0);
        panelTrainReq2.add(engine2DropOption);

        // row 15 buttons
        JPanel pB = new JPanel();
        pB.setLayout(new GridBagLayout());
        //  pB.setMaximumSize(new Dimension(2000, 250));
        addItem(pB, saveTrainButton, 3, 0);

        getContentPane().add(p1);
        getContentPane().add(pOption);
        getContentPane().add(ownerPane);
        getContentPane().add(builtPane);
        getContentPane().add(trainReq1Pane);
        getContentPane().add(trainReq2Pane);
        getContentPane().add(pB);

        // setup buttons
        addButtonAction(deleteOwnerButton);
        addButtonAction(addOwnerButton);
        addButtonAction(saveTrainButton);

        addRadioButtonAction(ownerNameAll);
        addRadioButtonAction(ownerNameInclude);
        addRadioButtonAction(ownerNameExclude);

        addRadioButtonAction(builtDateAll);
        addRadioButtonAction(builtDateAfter);
        addRadioButtonAction(builtDateBefore);
        addRadioButtonAction(builtDateRange);

        addRadioButtonAction(none1);
        addRadioButtonAction(change1Engine);
        addRadioButtonAction(add1Engine);
        addRadioButtonAction(remove1Engine);
        addRadioButtonAction(modify1Caboose);
        addRadioButtonAction(helper1Service);
        addRadioButtonAction(remove1Caboose);
        addRadioButtonAction(keep1Caboose);
        addRadioButtonAction(change1Caboose);

        addRadioButtonAction(none2);
        addRadioButtonAction(change2Engine);
        addRadioButtonAction(add2Engine);
        addRadioButtonAction(remove2Engine);
        addRadioButtonAction(modify2Caboose);
        addRadioButtonAction(helper2Service);
        addRadioButtonAction(remove2Caboose);
        addRadioButtonAction(keep2Caboose);
        addRadioButtonAction(change2Caboose);

        addComboBoxAction(numEngines1Box);
        addComboBoxAction(modelEngine1Box);
        addComboBoxAction(numEngines2Box);
        addComboBoxAction(modelEngine2Box);

        if (_train != null) {
            trainName.setText(_train.getName());
            trainDescription.setText(_train.getDescription());
            buildNormalCheckBox.setSelected(_train.isBuildTrainNormalEnabled());
            sendToTerminalCheckBox.setSelected(_train.isSendCarsToTerminalEnabled());
            allowLocalMovesCheckBox.setSelected(_train.isAllowLocalMovesEnabled());
            allowThroughCarsCheckBox.setSelected(_train.isAllowThroughCarsEnabled());
            serviceAllCarsCheckBox.setSelected(_train.isServiceAllCarsWithFinalDestinationsEnabled());
            sendCustomStagngCheckBox.setSelected(_train.isSendCarsWithCustomLoadsToStagingEnabled());
            buildConsistCheckBox.setSelected(_train.isBuildConsistEnabled());
            sendToTerminalCheckBox.setText(Bundle.getMessage("SendToTerminal",
                    _train.getTrainTerminatesName()));
            builtAfterTextField.setText(_train.getBuiltStartYear());
            builtBeforeTextField.setText(_train.getBuiltEndYear());
            setBuiltRadioButton();
            enableButtons(true);
            // does train depart and return to same staging location?
            updateReturnToStagingCheckbox();
            // remove engines feature disabled when departing staging
            updateRemoveEnginesRadioButtons();
            // listen for train changes
            _train.addPropertyChangeListener(this);
        } else {
            enableButtons(false);
        }
        addHelpMenu("package.jmri.jmrit.operations.Operations_TrainBuildOptions", true); // NOI18N
        updateOwnerNames();
        updateBuilt();
        updateTrainRequires1Option();
        updateTrainRequires2Option();

        // get notified if car owners or engine models gets modified
        InstanceManager.getDefault(CarOwners.class).addPropertyChangeListener(this);
        InstanceManager.getDefault(EngineModels.class).addPropertyChangeListener(this);

        // get notified if return to staging option changes
        Setup.getDefault().addPropertyChangeListener(this);

        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight600));
    }

    // Save
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (_train != null) {
            if (ae.getSource() == saveTrainButton) {
                log.debug("train save button activated");
                saveTrain();
            }
            if (ae.getSource() == addOwnerButton) {
                if (_train.addOwnerName((String) ownerBox.getSelectedItem())) {
                    updateOwnerNames();
                }
                selectNextItemComboBox(ownerBox);
            }
            if (ae.getSource() == deleteOwnerButton) {
                if (_train.deleteOwnerName((String) ownerBox.getSelectedItem())) {
                    updateOwnerNames();
                }
                selectNextItemComboBox(ownerBox);
            }
        }
    }

    @Override
    public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
        log.debug("radio button activated");
        if (_train != null) {
            if (ae.getSource() == ownerNameAll) {
                _train.setOwnerOption(Train.ALL_OWNERS);
                updateOwnerNames();
            }
            if (ae.getSource() == ownerNameInclude) {
                _train.setOwnerOption(Train.INCLUDE_OWNERS);
                updateOwnerNames();
            }
            if (ae.getSource() == ownerNameExclude) {
                _train.setOwnerOption(Train.EXCLUDE_OWNERS);
                updateOwnerNames();
            }
            if (ae.getSource() == builtDateAll ||
                    ae.getSource() == builtDateAfter ||
                    ae.getSource() == builtDateBefore ||
                    ae.getSource() == builtDateRange) {
                updateBuilt();
            }
            if (ae.getSource() == none1) {
                _train.setSecondLegOptions(Train.NO_CABOOSE_OR_FRED);
                updateTrainRequires1Option();
                updateTrainRequires2Option();
            }
            if (ae.getSource() == change1Engine) {
                _train.setSecondLegOptions(Train.CHANGE_ENGINES);
                updateTrainRequires1Option();
                updateTrainRequires2Option();
            }
            if (ae.getSource() == add1Engine) {
                _train.setSecondLegOptions(Train.ADD_ENGINES);
                updateTrainRequires1Option();
                updateTrainRequires2Option();
            }
            if (ae.getSource() == remove1Engine) {
                _train.setSecondLegOptions(Train.REMOVE_ENGINES);
                updateTrainRequires1Option();
                updateTrainRequires2Option();
            }
            if (ae.getSource() == modify1Caboose) {
                _train.setSecondLegOptions(Train.ADD_CABOOSE);
                updateTrainRequires1Option();
                updateTrainRequires2Option();
            }
            if (ae.getSource() == helper1Service) {
                _train.setSecondLegOptions(Train.HELPER_ENGINES);
                updateTrainRequires1Option();
            }
            if (ae.getSource() == keep1Caboose ||
                    ae.getSource() == change1Caboose ||
                    ae.getSource() == remove1Caboose) {
                roadCaboose1Box.setEnabled(change1Caboose.isSelected());
                updateTrainRequires2Option();
            }
            if (ae.getSource() == none2) {
                _train.setThirdLegOptions(Train.NO_CABOOSE_OR_FRED);
                updateTrainRequires2Option();
            }
            if (ae.getSource() == change2Engine) {
                _train.setThirdLegOptions(Train.CHANGE_ENGINES);
                updateTrainRequires2Option();
            }
            if (ae.getSource() == add2Engine) {
                _train.setThirdLegOptions(Train.ADD_ENGINES);
                updateTrainRequires2Option();
            }
            if (ae.getSource() == remove2Engine) {
                _train.setThirdLegOptions(Train.REMOVE_ENGINES);
                updateTrainRequires2Option();
            }
            if (ae.getSource() == modify2Caboose) {
                _train.setThirdLegOptions(Train.ADD_CABOOSE);
                updateTrainRequires2Option();
            }
            if (ae.getSource() == helper2Service) {
                _train.setThirdLegOptions(Train.HELPER_ENGINES);
                updateTrainRequires2Option();
            }
            if (ae.getSource() == keep2Caboose ||
                    ae.getSource() == change2Caboose ||
                    ae.getSource() == remove2Caboose) {
                roadCaboose2Box.setEnabled(change2Caboose.isSelected());
            }
        }
    }

    // Car type combo box has been changed, show loads associated with this car type
    @Override
    public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == numEngines1Box) {
            modelEngine1Box.setEnabled(!numEngines1Box.getSelectedItem().equals("0"));
            roadEngine1Box.setEnabled(!numEngines1Box.getSelectedItem().equals("0"));
        }
        if (ae.getSource() == modelEngine1Box) {
            updateEngineRoadComboBox(roadEngine1Box, (String) modelEngine1Box.getSelectedItem());
            if (_train != null) {
                roadEngine1Box.setSelectedItem(_train.getSecondLegEngineRoad());
            }
        }
        if (ae.getSource() == numEngines2Box) {
            modelEngine2Box.setEnabled(!numEngines2Box.getSelectedItem().equals("0"));
            roadEngine2Box.setEnabled(!numEngines2Box.getSelectedItem().equals("0"));
        }
        if (ae.getSource() == modelEngine2Box) {
            updateEngineRoadComboBox(roadEngine2Box, (String) modelEngine2Box.getSelectedItem());
            if (_train != null) {
                roadEngine2Box.setSelectedItem(_train.getThirdLegEngineRoad());
            }
        }
    }

    private void updateOwnerNames() {
        panelOwnerNames.removeAll();

        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());
        p.add(ownerNameAll, 0);
        p.add(ownerNameInclude, 1);
        p.add(ownerNameExclude, 2);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridwidth = 6;
        panelOwnerNames.add(p, gc);

        int y = 1; // vertical position in panel

        if (_train != null) {
            // set radio button
            ownerNameAll.setSelected(_train.getOwnerOption().equals(Train.ALL_OWNERS));
            ownerNameInclude.setSelected(_train.getOwnerOption().equals(Train.INCLUDE_OWNERS));
            ownerNameExclude.setSelected(_train.getOwnerOption().equals(Train.EXCLUDE_OWNERS));

            if (!ownerNameAll.isSelected()) {
                p = new JPanel();
                p.setLayout(new FlowLayout());
                p.add(ownerBox);
                p.add(addOwnerButton);
                p.add(deleteOwnerButton);
                gc.gridy = y++;
                panelOwnerNames.add(p, gc);

                int x = 0;
                for (String ownerName : _train.getOwnerNames()) {
                    JLabel owner = new JLabel();
                    owner.setText(ownerName);
                    addItem(panelOwnerNames, owner, x++, y);
                    if (x > 6) {
                        y++;
                        x = 0;
                    }
                }
            }
        } else {
            ownerNameAll.setSelected(true);
        }
        panelOwnerNames.revalidate();
        panelOwnerNames.repaint();
        revalidate();
    }

    private void setBuiltRadioButton() {
        if (_train.getBuiltStartYear().equals(Train.NONE) && _train.getBuiltEndYear().equals(Train.NONE)) {
            builtDateAll.setSelected(true);
        } else if (!_train.getBuiltStartYear().equals(Train.NONE) && !_train.getBuiltEndYear().equals(Train.NONE)) {
            builtDateRange.setSelected(true);
        } else if (!_train.getBuiltStartYear().equals(Train.NONE)) {
            builtDateAfter.setSelected(true);
        } else if (!_train.getBuiltEndYear().equals(Train.NONE)) {
            builtDateBefore.setSelected(true);
        }
    }

    private void updateBuilt() {
        builtAfterTextField.setVisible(false);
        builtBeforeTextField.setVisible(false);
        after.setVisible(false);
        before.setVisible(false);
        if (builtDateAll.isSelected()) {
            builtAfterTextField.setText("");
            builtBeforeTextField.setText("");
        } else if (builtDateAfter.isSelected()) {
            builtBeforeTextField.setText("");
            builtAfterTextField.setVisible(true);
            after.setVisible(true);
        } else if (builtDateBefore.isSelected()) {
            builtAfterTextField.setText("");
            builtBeforeTextField.setVisible(true);
            before.setVisible(true);
        } else if (builtDateRange.isSelected()) {
            after.setVisible(true);
            before.setVisible(true);
            builtAfterTextField.setVisible(true);
            builtBeforeTextField.setVisible(true);
        }
        revalidate();
    }

    private void updateTrainRequires1Option() {
        none1.setSelected(true);
        if (_train != null) {

            updateCabooseRoadComboBox(roadCaboose1Box);
            updateEngineRoadComboBox(roadEngine1Box, (String) modelEngine1Box.getSelectedItem());
            if (_train.getRoute() != null) {
                _train.getRoute().updateComboBox(routePickup1Box);
                _train.getRoute().updateComboBox(routeDrop1Box);
            }

            change1Engine.setSelected((_train.getSecondLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES);
            add1Engine.setSelected((_train.getSecondLegOptions() & Train.ADD_ENGINES) == Train.ADD_ENGINES);
            remove1Engine.setSelected((_train.getSecondLegOptions() & Train.REMOVE_ENGINES) == Train.REMOVE_ENGINES);
            helper1Service.setSelected((_train.getSecondLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES);
            if (!change1Engine.isSelected() &&
                    !add1Engine.isSelected() &&
                    !remove1Engine.isSelected() &&
                    !helper1Service.isSelected()) {
                modify1Caboose.setSelected((_train.getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE ||
                        (_train.getSecondLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE);
            }
            numEngines1Box.setSelectedItem(_train.getSecondLegNumberEngines());
            modelEngine1Box.setSelectedItem(_train.getSecondLegEngineModel());
            routePickup1Box.setSelectedItem(_train.getSecondLegStartRouteLocation());
            routeDrop1Box.setSelectedItem(_train.getSecondLegEndRouteLocation());
            roadEngine1Box.setSelectedItem(_train.getSecondLegEngineRoad());
            keep1Caboose.setSelected(true);
            remove1Caboose.setSelected((_train.getSecondLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE);
            change1Caboose.setSelected((_train.getSecondLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE);
            roadCaboose1Box.setEnabled(change1Caboose.isSelected());
            roadCaboose1Box.setSelectedItem(_train.getSecondLegCabooseRoad());
            // adjust radio button text
            if (_train.isCabooseNeeded()) {
                change1Caboose.setText(Bundle.getMessage("ChangeCaboose"));
                remove1Caboose.setEnabled(true);
            } else {
                change1Caboose.setText(Bundle.getMessage("AddCaboose"));
                remove1Caboose.setEnabled(false);
            }
        }
        engine1Option.setVisible(change1Engine.isSelected() ||
                add1Engine.isSelected() ||
                remove1Engine.isSelected() ||
                helper1Service.isSelected());
        engine1caboose.setVisible(change1Engine.isSelected() ||
                add1Engine.isSelected() ||
                remove1Engine.isSelected() ||
                modify1Caboose.isSelected());
        engine1DropOption.setVisible(helper1Service.isSelected());
        if (change1Engine.isSelected()) {
            engine1Option.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("EngineChange")));
        } else if (add1Engine.isSelected()) {
            engine1Option.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AddEngines")));
        } else {
            engine1Option.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RemoveEngines")));
        }
        if (helper1Service.isSelected()) {
            engine1Option.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AddHelpers")));
        }
        if (change1Engine.isSelected() ||
                add1Engine.isSelected() ||
                remove1Engine.isSelected() ||
                helper1Service.isSelected()) {
            createEngine1Panel();
        }
        if (change1Engine.isSelected() ||
                add1Engine.isSelected() ||
                remove1Engine.isSelected() ||
                modify1Caboose.isSelected()) {
            createCaboose1Panel(modify1Caboose.isSelected());
        }
        revalidate();
    }

    private void updateTrainRequires2Option() {
        none2.setSelected(true);
        if (_train != null) {

            updateCabooseRoadComboBox(roadCaboose2Box);
            updateEngineRoadComboBox(roadEngine2Box, (String) modelEngine2Box.getSelectedItem());
            if (_train.getRoute() != null) {
                _train.getRoute().updateComboBox(routePickup2Box);
                _train.getRoute().updateComboBox(routeDrop2Box);
            }

            change2Engine.setSelected((_train.getThirdLegOptions() & Train.CHANGE_ENGINES) == Train.CHANGE_ENGINES);
            add2Engine.setSelected((_train.getThirdLegOptions() & Train.ADD_ENGINES) == Train.ADD_ENGINES);
            remove2Engine.setSelected((_train.getThirdLegOptions() & Train.REMOVE_ENGINES) == Train.REMOVE_ENGINES);
            helper2Service.setSelected((_train.getThirdLegOptions() & Train.HELPER_ENGINES) == Train.HELPER_ENGINES);
            if (!change2Engine.isSelected() &&
                    !add2Engine.isSelected() &&
                    !remove2Engine.isSelected() &&
                    !helper2Service.isSelected()) {
                modify2Caboose.setSelected((_train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE ||
                        (_train.getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE);
            }
            numEngines2Box.setSelectedItem(_train.getThirdLegNumberEngines());
            modelEngine2Box.setSelectedItem(_train.getThirdLegEngineModel());
            routePickup2Box.setSelectedItem(_train.getThirdLegStartRouteLocation());
            routeDrop2Box.setSelectedItem(_train.getThirdLegEndRouteLocation());
            roadEngine2Box.setSelectedItem(_train.getThirdLegEngineRoad());
            keep2Caboose.setSelected(true);
            remove2Caboose.setSelected((_train.getThirdLegOptions() & Train.REMOVE_CABOOSE) == Train.REMOVE_CABOOSE);
            change2Caboose.setSelected((_train.getThirdLegOptions() & Train.ADD_CABOOSE) == Train.ADD_CABOOSE);
            roadCaboose2Box.setEnabled(change2Caboose.isSelected());
            roadCaboose2Box.setSelectedItem(_train.getThirdLegCabooseRoad());
            // adjust radio button text
            if ((_train.isCabooseNeeded() || change1Caboose.isSelected()) &&
                    !remove1Caboose.isSelected()) {
                change2Caboose.setText(Bundle.getMessage("ChangeCaboose"));
                remove2Caboose.setEnabled(true);
            } else {
                change2Caboose.setText(Bundle.getMessage("AddCaboose"));
                remove2Caboose.setEnabled(false);
            }
        }
        engine2Option.setVisible(change2Engine.isSelected() ||
                add2Engine.isSelected() ||
                remove2Engine.isSelected() ||
                helper2Service.isSelected());
        engine2caboose.setVisible(change2Engine.isSelected() ||
                add2Engine.isSelected() ||
                remove2Engine.isSelected() ||
                modify2Caboose.isSelected());
        engine2DropOption.setVisible(helper2Service.isSelected());
        if (change2Engine.isSelected()) {
            engine2Option.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("EngineChange")));
        } else if (add2Engine.isSelected()) {
            engine2Option.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AddEngines")));
        } else {
            engine2Option.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RemoveEngines")));
        }
        if (helper2Service.isSelected()) {
            engine2Option.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AddHelpers")));
        }
        if (change2Engine.isSelected() ||
                add2Engine.isSelected() ||
                remove2Engine.isSelected() ||
                helper2Service.isSelected()) {
            createEngine2Panel();
        }
        if (change2Engine.isSelected() ||
                add2Engine.isSelected() ||
                remove2Engine.isSelected() ||
                modify2Caboose.isSelected()) {
            createCaboose2Panel(modify2Caboose.isSelected());
        }
        revalidate();
    }

    private void saveTrain() {
        if (!checkInput()) {
            return;
        }
        _train.setBuildTrainNormalEnabled(buildNormalCheckBox.isSelected());
        _train.setSendCarsToTerminalEnabled(sendToTerminalCheckBox.isSelected());
        if (returnStagingCheckBox.isEnabled()) {
            _train.setAllowReturnToStagingEnabled(returnStagingCheckBox.isSelected());
        }
        _train.setAllowLocalMovesEnabled(allowLocalMovesCheckBox.isSelected());
        _train.setAllowThroughCarsEnabled(allowThroughCarsCheckBox.isSelected());
        _train.setServiceAllCarsWithFinalDestinationsEnabled(serviceAllCarsCheckBox.isSelected());
        _train.setSendCarsWithCustomLoadsToStagingEnabled(sendCustomStagngCheckBox.isSelected());
        _train.setBuildConsistEnabled(buildConsistCheckBox.isSelected());
        _train.setBuiltStartYear(builtAfterTextField.getText().trim());
        _train.setBuiltEndYear(builtBeforeTextField.getText().trim());

        int options1 = Train.NO_CABOOSE_OR_FRED;
        if (change1Engine.isSelected()) {
            options1 = options1 | Train.CHANGE_ENGINES;
        }
        if (add1Engine.isSelected()) {
            options1 = options1 | Train.ADD_ENGINES;
        }
        if (remove1Engine.isSelected()) {
            options1 = options1 | Train.REMOVE_ENGINES;
        }
        if (remove1Caboose.isSelected()) {
            options1 = options1 | Train.REMOVE_CABOOSE;
        } else if (change1Caboose.isSelected()) {
            options1 = options1 | Train.ADD_CABOOSE | Train.REMOVE_CABOOSE;
        }
        if (helper1Service.isSelected()) {
            options1 = options1 | Train.HELPER_ENGINES;
        }
        _train.setSecondLegOptions(options1);
        if (routePickup1Box.getSelectedItem() != null) {
            _train.setSecondLegStartRouteLocation((RouteLocation) routePickup1Box.getSelectedItem());
        } else {
            _train.setSecondLegStartRouteLocation(null);
        }
        if (routeDrop1Box.getSelectedItem() != null) {
            _train.setSecondLegEndRouteLocation((RouteLocation) routeDrop1Box.getSelectedItem());
        } else {
            _train.setSecondLegEndRouteLocation(null);
        }
        _train.setSecondLegNumberEngines((String) numEngines1Box.getSelectedItem());
        _train.setSecondLegEngineModel((String) modelEngine1Box.getSelectedItem());
        _train.setSecondLegEngineRoad((String) roadEngine1Box.getSelectedItem());
        _train.setSecondLegCabooseRoad((String) roadCaboose1Box.getSelectedItem());

        int options2 = Train.NO_CABOOSE_OR_FRED;
        if (change2Engine.isSelected()) {
            options2 = options2 | Train.CHANGE_ENGINES;
        }
        if (add2Engine.isSelected()) {
            options2 = options2 | Train.ADD_ENGINES;
        }
        if (remove2Engine.isSelected()) {
            options2 = options2 | Train.REMOVE_ENGINES;
        }
        if (remove2Caboose.isSelected()) {
            options2 = options2 | Train.REMOVE_CABOOSE;
        } else if (change2Caboose.isSelected()) {
            options2 = options2 | Train.ADD_CABOOSE | Train.REMOVE_CABOOSE;
        }
        if (helper2Service.isSelected()) {
            options2 = options2 | Train.HELPER_ENGINES;
        }
        _train.setThirdLegOptions(options2);
        if (routePickup2Box.getSelectedItem() != null) {
            _train.setThirdLegStartRouteLocation((RouteLocation) routePickup2Box.getSelectedItem());
        } else {
            _train.setThirdLegStartRouteLocation(null);
        }
        if (routeDrop2Box.getSelectedItem() != null) {
            _train.setThirdLegEndRouteLocation((RouteLocation) routeDrop2Box.getSelectedItem());
        } else {
            _train.setThirdLegEndRouteLocation(null);
        }
        _train.setThirdLegNumberEngines((String) numEngines2Box.getSelectedItem());
        _train.setThirdLegEngineModel((String) modelEngine2Box.getSelectedItem());
        _train.setThirdLegEngineRoad((String) roadEngine2Box.getSelectedItem());
        _train.setThirdLegCabooseRoad((String) roadCaboose2Box.getSelectedItem());

        OperationsXml.save();
        if (Setup.isCloseWindowOnSaveEnabled()) {
            dispose();
        }
    }

    private boolean checkInput() {
        if ((!none1.isSelected() && routePickup1Box.getSelectedItem() == null) ||
                (!none2.isSelected() && routePickup2Box.getSelectedItem() == null)) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("SelectLocationEngChange"), Bundle
                    .getMessage("CanNotSave"), JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        if ((helper1Service.isSelected() && routeDrop1Box.getSelectedItem() == null) ||
                (helper2Service.isSelected() && routeDrop2Box.getSelectedItem() == null)) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("SelectLocationEndHelper"), Bundle
                    .getMessage("CanNotSave"), JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        try {
            if (!builtAfterTextField.getText().trim().isEmpty()) {
                Integer.parseInt(builtAfterTextField.getText().trim());
            }
            if (!builtBeforeTextField.getText().trim().isEmpty()) {
                Integer.parseInt(builtBeforeTextField.getText().trim());
            }
        } catch (NumberFormatException e) {
            JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("EnterFourDigitYear"), Bundle
                    .getMessage("CanNotSave"), JmriJOptionPane.ERROR_MESSAGE);
            return false;
        }
        if ((change1Engine.isSelected() && !checkModel(modelEngine1Box, numEngines1Box, roadEngine1Box)) ||
                (change2Engine.isSelected() && !checkModel(modelEngine2Box, numEngines2Box, roadEngine2Box))) {
            return false;
        }
        if ((add1Engine.isSelected() && !checkModel(modelEngine1Box, numEngines1Box, roadEngine1Box)) ||
                (add2Engine.isSelected() && !checkModel(modelEngine2Box, numEngines2Box, roadEngine2Box))) {
            return false;
        }
        if ((remove1Engine.isSelected() && !checkModel(modelEngine1Box, numEngines1Box, roadEngine1Box)) ||
                (remove2Engine.isSelected() && !checkModel(modelEngine2Box, numEngines2Box, roadEngine2Box))) {
            return false;
        }
        if (!checkCabooseRoad(roadCaboose1Box, change1Caboose)) {
            return false;
        }
        if (!checkCabooseRoad(roadCaboose2Box, change2Caboose)) {
            return false;
        }

        return true;
    }

    private boolean checkModel(JComboBox<String> modelEngineBox, JComboBox<String> numEnginesBox,
            JComboBox<String> roadEngineBox) {
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

    private boolean checkCabooseRoad(JComboBox<String> roadCabooseBox, JRadioButton cabooseRadioButton) {
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

    private void enableButtons(boolean enabled) {
        ownerNameAll.setEnabled(enabled);
        ownerNameInclude.setEnabled(enabled);
        ownerNameExclude.setEnabled(enabled);

        builtDateAll.setEnabled(enabled);
        builtDateAfter.setEnabled(enabled);
        builtDateBefore.setEnabled(enabled);
        builtDateRange.setEnabled(enabled);

        none1.setEnabled(enabled);
        change1Engine.setEnabled(enabled);
        add1Engine.setEnabled(enabled);
        remove1Engine.setEnabled(enabled);
        modify1Caboose.setEnabled(enabled);
        helper1Service.setEnabled(enabled);

        none2.setEnabled(enabled);
        change2Engine.setEnabled(enabled);
        add2Engine.setEnabled(enabled);
        remove2Engine.setEnabled(enabled);
        modify2Caboose.setEnabled(enabled);
        helper2Service.setEnabled(enabled);

        saveTrainButton.setEnabled(enabled);
    }

    private void updateModelComboBoxes() {
        InstanceManager.getDefault(EngineModels.class).updateComboBox(modelEngine1Box);
        InstanceManager.getDefault(EngineModels.class).updateComboBox(modelEngine2Box);
        modelEngine1Box.insertItemAt("", 0);
        modelEngine2Box.insertItemAt("", 0);
        if (_train != null) {
            modelEngine1Box.setSelectedItem(_train.getSecondLegEngineModel());
            modelEngine2Box.setSelectedItem(_train.getThirdLegEngineModel());
        }
    }

    private void updateOwnerComboBoxes() {
        InstanceManager.getDefault(CarOwners.class).updateComboBox(ownerBox);
    }

    // update caboose road box based on radio selection
    private void updateCabooseRoadComboBox(JComboBox<String> box) {
        box.removeAllItems();
        box.addItem(NONE);
        List<String> roads = InstanceManager.getDefault(CarManager.class).getCabooseRoadNames();
        for (String road : roads) {
            box.addItem(road);
        }
    }

    private void updateEngineRoadComboBox(JComboBox<String> box, String engineModel) {
        if (engineModel == null) {
            return;
        }
        InstanceManager.getDefault(EngineManager.class).updateEngineRoadComboBox(engineModel, box);
    }

    private void updateReturnToStagingCheckbox() {
        if (_train != null &&
                _train.getTrainDepartsRouteLocation() != null &&
                _train.getTrainTerminatesRouteLocation() != null &&
                _train.getTrainTerminatesRouteLocation().getLocation() != null &&
                _train.getTrainTerminatesRouteLocation().getLocation().isStaging() &&
                _train.getTrainDepartsRouteLocation().getName().equals(
                        _train.getTrainTerminatesRouteLocation().getName())) {
            allowThroughCarsCheckBox.setEnabled(false);
            if (Setup.isStagingAllowReturnEnabled()) {
                returnStagingCheckBox.setEnabled(false);
                returnStagingCheckBox.setSelected(true);
                returnStagingCheckBox.setToolTipText(Bundle.getMessage("TipReturnToStaging"));
            } else {
                returnStagingCheckBox.setEnabled(true);
                returnStagingCheckBox.setSelected(_train.isAllowReturnToStagingEnabled());
                returnStagingCheckBox.setToolTipText(NONE);
            }
        }
    }

    private void updateRemoveEnginesRadioButtons() {
        if (_train != null &&
                _train.getTrainDepartsRouteLocation() != null &&
                _train.getTrainDepartsRouteLocation().getLocation() != null &&
                _train.getTrainDepartsRouteLocation().getLocation().isStaging()) {
            remove1Engine.setEnabled(false);
            remove2Engine.setEnabled(false);
        }
    }

    private void createEngine1Panel() {
        engine1Option.removeAll();
        addItem(engine1Option, new JLabel(Bundle.getMessage("ChangeEnginesAt")), 0, 0);
        addItem(engine1Option, routePickup1Box, 1, 0);
        addItem(engine1Option, new JLabel(Bundle.getMessage("Engines")), 2, 0);
        addItem(engine1Option, numEngines1Box, 3, 0);
        addItem(engine1Option, new JLabel(Bundle.getMessage("Model")), 4, 0);
        addItem(engine1Option, modelEngine1Box, 5, 0);
        addItem(engine1Option, new JLabel(Bundle.getMessage("Road")), 6, 0);
        addItem(engine1Option, roadEngine1Box, 7, 0);
    }

    private void createEngine2Panel() {
        engine2Option.removeAll();
        addItem(engine2Option, new JLabel(Bundle.getMessage("ChangeEnginesAt")), 0, 0);
        addItem(engine2Option, routePickup2Box, 1, 0);
        addItem(engine2Option, new JLabel(Bundle.getMessage("Engines")), 2, 0);
        addItem(engine2Option, numEngines2Box, 3, 0);
        addItem(engine2Option, new JLabel(Bundle.getMessage("Model")), 4, 0);
        addItem(engine2Option, modelEngine2Box, 5, 0);
        addItem(engine2Option, new JLabel(Bundle.getMessage("Road")), 6, 0);
        addItem(engine2Option, roadEngine2Box, 7, 0);
    }

    private void createCaboose1Panel(boolean withCombox) {
        engine1caboose.removeAll();
        addItem(engine1caboose, remove1Caboose, 2, 6);
        addItem(engine1caboose, change1Caboose, 4, 6);
        addItem(engine1caboose, new JLabel(Bundle.getMessage("Road")), 5, 6);
        addItem(engine1caboose, roadCaboose1Box, 6, 6);
        if (withCombox) {
            addItem(engine1caboose, new JLabel(Bundle.getMessage("ChangeEnginesAt")), 0, 6);
            addItem(engine1caboose, routePickup1Box, 1, 6);
        } else {
            addItem(engine1caboose, keep1Caboose, 3, 6);
        }
    }

    private void createCaboose2Panel(boolean withCombox) {
        engine2caboose.removeAll();
        addItem(engine2caboose, remove2Caboose, 2, 6);
        addItem(engine2caboose, change2Caboose, 4, 6);
        addItem(engine2caboose, new JLabel(Bundle.getMessage("Road")), 5, 6);
        addItem(engine2caboose, roadCaboose2Box, 6, 6);
        if (withCombox) {
            addItem(engine2caboose, new JLabel(Bundle.getMessage("ChangeEnginesAt")), 0, 6);
            addItem(engine2caboose, routePickup2Box, 1, 6);
        } else {
            addItem(engine2caboose, keep2Caboose, 3, 6);
        }
    }

    @Override
    public void dispose() {
        InstanceManager.getDefault(CarOwners.class).removePropertyChangeListener(this);
        InstanceManager.getDefault(EngineModels.class).removePropertyChangeListener(this);
        Setup.getDefault().removePropertyChangeListener(this);
        if (_train != null) {
            _train.removePropertyChangeListener(this);
        }
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(CarOwners.CAROWNERS_CHANGED_PROPERTY)) {
            updateOwnerComboBoxes();
            updateOwnerNames();
        }
        if (e.getPropertyName().equals(EngineModels.ENGINEMODELS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.TYPES_CHANGED_PROPERTY)) {
            updateModelComboBoxes();
        }
        if (e.getPropertyName().equals(Train.TRAIN_REQUIREMENTS_CHANGED_PROPERTY)) {
            updateTrainRequires1Option();
            updateTrainRequires2Option();
        }
        if (e.getPropertyName().equals(Setup.ALLOW_CARS_TO_RETURN_PROPERTY_CHANGE)) {
            updateReturnToStagingCheckbox();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrainEditBuildOptionsFrame.class);
}
