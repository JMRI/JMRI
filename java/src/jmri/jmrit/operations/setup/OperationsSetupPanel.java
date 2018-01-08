package jmri.jmrit.operations.setup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.operations.ExceptionDisplayFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.UnexpectedExceptionContext;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.web.server.WebServerPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame for user edit of operation parameters
 *
 * @author Dan Boudreau Copyright (C) 2008, 2010, 2011, 2012
 */
public class OperationsSetupPanel extends OperationsPreferencesPanel implements PropertyChangeListener {

    private final static Logger log = LoggerFactory.getLogger(OperationsSetupPanel.class);

    // labels
    private final JLabel textIconNorth = new JLabel(Bundle.getMessage("IconNorth"));
    private final JLabel textIconSouth = new JLabel(Bundle.getMessage("IconSouth"));
    private final JLabel textIconEast = new JLabel(Bundle.getMessage("IconEast"));
    private final JLabel textIconWest = new JLabel(Bundle.getMessage("IconWest"));
    private final JLabel textIconLocal = new JLabel(Bundle.getMessage("IconLocal"));
    private final JLabel textIconTerminate = new JLabel(Bundle.getMessage("IconTerminate"));

    // major buttons
    private final JButton backupButton = new JButton(Bundle.getMessage("Backup"));
    private final JButton restoreButton = new JButton(Bundle.getMessage("Restore"));
    private final JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    // radio buttons
    private final JRadioButton scaleZ = new JRadioButton("Z"); // NOI18N
    private final JRadioButton scaleN = new JRadioButton("N"); // NOI18N
    private final JRadioButton scaleTT = new JRadioButton("TT"); // NOI18N
    private final JRadioButton scaleHOn3 = new JRadioButton("HOn3"); // NOI18N
    private final JRadioButton scaleOO = new JRadioButton("OO"); // NOI18N
    private final JRadioButton scaleHO = new JRadioButton("HO"); // NOI18N
    private final JRadioButton scaleSn3 = new JRadioButton("Sn3"); // NOI18N
    private final JRadioButton scaleS = new JRadioButton("S"); // NOI18N
    private final JRadioButton scaleOn3 = new JRadioButton("On3"); // NOI18N
    private final JRadioButton scaleO = new JRadioButton("O"); // NOI18N
    private final JRadioButton scaleG = new JRadioButton("G"); // NOI18N

    private final JRadioButton typeDesc = new JRadioButton(Bundle.getMessage("Descriptive"));
    private final JRadioButton typeAAR = new JRadioButton(Bundle.getMessage("AAR"));

    private final JRadioButton feetUnit = new JRadioButton(Bundle.getMessage("Feet"));
    private final JRadioButton meterUnit = new JRadioButton(Bundle.getMessage("Meter"));

    // check boxes
    private final JCheckBox eastCheckBox = new JCheckBox(Bundle.getMessage("eastwest"));
    private final JCheckBox northCheckBox = new JCheckBox(Bundle.getMessage("northsouth"));
    private final JCheckBox mainMenuCheckBox = new JCheckBox(Bundle.getMessage("MainMenu"));
    private final JCheckBox closeOnSaveCheckBox = new JCheckBox(Bundle.getMessage("CloseOnSave"));
    private final JCheckBox autoSaveCheckBox = new JCheckBox(Bundle.getMessage("AutoSave"));
    private final JCheckBox autoBackupCheckBox = new JCheckBox(Bundle.getMessage("AutoBackup"));
    private final JCheckBox iconCheckBox = new JCheckBox(Bundle.getMessage("trainIcon"));
    private final JCheckBox appendCheckBox = new JCheckBox(Bundle.getMessage("trainIconAppend"));

    // text field
    // JTextField ownerTextField = new JTextField(10);
    JTextField panelTextField = new JTextField(30);
    JTextField railroadNameTextField = new JTextField(35);
    JTextField maxLengthTextField = new JTextField(5);
    JTextField maxEngineSizeTextField = new JTextField(3);
    JTextField hptTextField = new JTextField(3);
    JTextField switchTimeTextField = new JTextField(3);
    JTextField travelTimeTextField = new JTextField(3);
    JTextField yearTextField = new JTextField(4);

    // combo boxes
    private final JComboBox<String> northComboBox = new JComboBox<>();
    private final JComboBox<String> southComboBox = new JComboBox<>();
    private final JComboBox<String> eastComboBox = new JComboBox<>();
    private final JComboBox<String> westComboBox = new JComboBox<>();
    private final JComboBox<String> localComboBox = new JComboBox<>();
    private final JComboBox<String> terminateComboBox = new JComboBox<>();

    // text area
    private final JTextArea commentTextArea = new JTextArea(2, 80);

    public OperationsSetupPanel() {
        super();

        // the following code sets the frame's initial state
        // create manager to load operation settings
        InstanceManager.getDefault(OperationsSetupXml.class);

        // load fields
        maxLengthTextField.setText(Integer.toString(Setup.getMaxTrainLength()));
        maxEngineSizeTextField.setText(Integer.toString(Setup.getMaxNumberEngines()));
        hptTextField.setText(Integer.toString(Setup.getHorsePowerPerTon()));
        switchTimeTextField.setText(Integer.toString(Setup.getSwitchTime()));
        travelTimeTextField.setText(Integer.toString(Setup.getTravelTime()));
        panelTextField.setText(Setup.getPanelName());
        yearTextField.setText(Setup.getYearModeled());
        commentTextArea.setText(Setup.getComment());

        // load checkboxes
        mainMenuCheckBox.setSelected(Setup.isMainMenuEnabled());
        closeOnSaveCheckBox.setSelected(Setup.isCloseWindowOnSaveEnabled());
        autoSaveCheckBox.setSelected(Setup.isAutoSaveEnabled());
        autoBackupCheckBox.setSelected(Setup.isAutoBackupEnabled());
        iconCheckBox.setSelected(Setup.isTrainIconCordEnabled());
        appendCheckBox.setSelected(Setup.isTrainIconAppendEnabled());

        // add tool tips
        backupButton.setToolTipText(Bundle.getMessage("BackupToolTip"));
        restoreButton.setToolTipText(Bundle.getMessage("RestoreToolTip"));
        saveButton.setToolTipText(Bundle.getMessage("SaveToolTip"));
        panelTextField.setToolTipText(Bundle.getMessage("EnterPanelName"));
        yearTextField.setToolTipText(Bundle.getMessage("EnterYearModeled"));
        autoSaveCheckBox.setToolTipText(Bundle.getMessage("AutoSaveTip"));
        autoBackupCheckBox.setToolTipText(Bundle.getMessage("AutoBackUpTip"));
        maxLengthTextField.setToolTipText(Bundle.getMessage("MaxLengthTip"));
        maxEngineSizeTextField.setToolTipText(Bundle.getMessage("MaxEngineTip"));
        hptTextField.setToolTipText(Bundle.getMessage("HPperTonTip"));
        switchTimeTextField.setToolTipText(Bundle.getMessage("SwitchTimeTip"));
        travelTimeTextField.setToolTipText(Bundle.getMessage("TravelTimeTip"));
        railroadNameTextField.setToolTipText(Bundle.getMessage("RailroadNameTip"));
        commentTextArea.setToolTipText(Bundle.getMessage("CommentTip"));

        // Layout the panel by rows
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        JScrollPane panelPane = new JScrollPane(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panelPane.setBorder(BorderFactory.createTitledBorder(""));

        // row 1a
        JPanel p1 = new JPanel();
        p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));

        JPanel pRailroadName = new JPanel();
        pRailroadName.setLayout(new GridBagLayout());
        pRailroadName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("RailroadName")));
        addItem(pRailroadName, railroadNameTextField, 0, 0);
        p1.add(pRailroadName);

        // row 1b
        JPanel pTrainDir = new JPanel();
        pTrainDir.setLayout(new GridBagLayout());
        pTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("direction")));
        addItemLeft(pTrainDir, northCheckBox, 1, 2);
        addItemLeft(pTrainDir, eastCheckBox, 2, 2);
        p1.add(pTrainDir);

        setDirectionCheckBox(Setup.getTrainDirection());

        // row 3a
        JPanel p3 = new JPanel();
        p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));

        JPanel pTrainLength = new JPanel();
        pTrainLength.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("MaxLength")));
        addItem(pTrainLength, maxLengthTextField, 0, 0);
        p3.add(pTrainLength);

        // row 3b
        JPanel pMaxEngine = new JPanel();
        pMaxEngine.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("MaxEngine")));
        addItem(pMaxEngine, maxEngineSizeTextField, 0, 0);
        p3.add(pMaxEngine);

        // row 3c
        JPanel pHPT = new JPanel();
        pHPT.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("HPT")));
        addItem(pHPT, hptTextField, 0, 0);
        p3.add(pHPT);

        JPanel pSwitchTime = new JPanel();
        pSwitchTime.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("MoveTime")));
        addItem(pSwitchTime, switchTimeTextField, 0, 0);
        p3.add(pSwitchTime);

        // row 3d
        JPanel pTravelTime = new JPanel();
        pTravelTime.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TravelTime")));
        addItem(pTravelTime, travelTimeTextField, 0, 0);
        p3.add(pTravelTime);

        // row 2
        JPanel pScale = new JPanel();
        pScale.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Scale")));

        ButtonGroup scaleGroup = new ButtonGroup();
        scaleGroup.add(scaleZ);
        scaleGroup.add(scaleN);
        scaleGroup.add(scaleTT);
        scaleGroup.add(scaleHOn3);
        scaleGroup.add(scaleOO);
        scaleGroup.add(scaleHO);
        scaleGroup.add(scaleSn3);
        scaleGroup.add(scaleS);
        scaleGroup.add(scaleOn3);
        scaleGroup.add(scaleO);
        scaleGroup.add(scaleG);

        pScale.add(scaleZ);
        pScale.add(scaleN);
        pScale.add(scaleTT);
        pScale.add(scaleHOn3);
        pScale.add(scaleOO);
        pScale.add(scaleHO);
        pScale.add(scaleSn3);
        pScale.add(scaleS);
        pScale.add(scaleOn3);
        pScale.add(scaleO);
        pScale.add(scaleG);
        setScale();

        // row 4a
        JPanel p9 = new JPanel();
        p9.setLayout(new BoxLayout(p9, BoxLayout.X_AXIS));

        JPanel pCarTypeButtons = new JPanel();
        pCarTypeButtons.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("CarTypes")));
        ButtonGroup carTypeGroup = new ButtonGroup();
        carTypeGroup.add(typeDesc);
        carTypeGroup.add(typeAAR);
        pCarTypeButtons.add(typeDesc);
        pCarTypeButtons.add(typeAAR);
        p9.add(pCarTypeButtons);
        setCarTypes();

        // row 4b
        JPanel pLengthUnit = new JPanel();
        pLengthUnit.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutLength")));
        ButtonGroup lengthUnitGroup = new ButtonGroup();
        lengthUnitGroup.add(feetUnit);
        lengthUnitGroup.add(meterUnit);
        pLengthUnit.add(feetUnit);
        pLengthUnit.add(meterUnit);
        p9.add(pLengthUnit);
        setLengthUnit();

        // row 4c
        JPanel pYearModeled = new JPanel();
        pYearModeled.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutYearModeled")));
        pYearModeled.add(yearTextField);

        p9.add(pYearModeled);

        // Option panel
        JPanel options = new JPanel();
        options.setLayout(new GridBagLayout());
        options.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutOptions")));
        addItem(options, mainMenuCheckBox, 0, 0);
        addItem(options, closeOnSaveCheckBox, 1, 0);
        addItem(options, autoSaveCheckBox, 2, 0);
        addItem(options, autoBackupCheckBox, 3, 0);

        // p9.add(options);
        // 1st scroll panel
        panel.add(p1);
        panel.add(pScale);
        panel.add(p3);
        panel.add(p9);

        // Icon panel
        JPanel pIcon = new JPanel();
        pIcon.setLayout(new BoxLayout(pIcon, BoxLayout.Y_AXIS));
        JScrollPane pIconPane = new JScrollPane(pIcon);
        pIconPane.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutPanelOptions")));

        // row 1 Icon panel
        JPanel p1Icon = new JPanel();
        p1Icon.setLayout(new BoxLayout(p1Icon, BoxLayout.X_AXIS));

        JPanel pPanelName = new JPanel();
        pPanelName.setLayout(new GridBagLayout());
        pPanelName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutPanelName")));
        addItem(pPanelName, panelTextField, 0, 0);
        p1Icon.add(pPanelName);

        JPanel pIconControl = new JPanel();
        pIconControl.setLayout(new GridBagLayout());
        pIconControl.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutIconOptions")));
        addItem(pIconControl, appendCheckBox, 0, 0);
        addItem(pIconControl, iconCheckBox, 1, 0);
        p1Icon.add(pIconControl);

        pIcon.add(p1Icon);

        JPanel pIconColors = new JPanel();
        pIconColors.setLayout(new GridBagLayout());
        pIconColors.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("BorderLayoutIconColors")));

        textIconNorth.setLabelFor(northComboBox);
        addItem(pIconColors, textIconNorth, 0, 4);
        addItemLeft(pIconColors, northComboBox, 1, 4);
        textIconSouth.setLabelFor(southComboBox);
        addItem(pIconColors, textIconSouth, 0, 5);
        addItemLeft(pIconColors, southComboBox, 1, 5);
        textIconEast.setLabelFor(eastComboBox);
        addItem(pIconColors, textIconEast, 0, 8);
        addItemLeft(pIconColors, eastComboBox, 1, 8);
        textIconWest.setLabelFor(westComboBox);
        addItem(pIconColors, textIconWest, 0, 9);
        addItemLeft(pIconColors, westComboBox, 1, 9);
        textIconLocal.setLabelFor(localComboBox);
        addItem(pIconColors, textIconLocal, 0, 10);
        addItemLeft(pIconColors, localComboBox, 1, 10);
        textIconTerminate.setLabelFor(terminateComboBox);
        addItem(pIconColors, textIconTerminate, 0, 11);
        addItemLeft(pIconColors, terminateComboBox, 1, 11);

        pIcon.add(pIconColors);

        loadIconComboBox(northComboBox);
        loadIconComboBox(southComboBox);
        loadIconComboBox(eastComboBox);
        loadIconComboBox(westComboBox);
        loadIconComboBox(localComboBox);
        loadIconComboBox(terminateComboBox);
        northComboBox.setSelectedItem(Setup.getTrainIconColorNorth());
        southComboBox.setSelectedItem(Setup.getTrainIconColorSouth());
        eastComboBox.setSelectedItem(Setup.getTrainIconColorEast());
        westComboBox.setSelectedItem(Setup.getTrainIconColorWest());
        localComboBox.setSelectedItem(Setup.getTrainIconColorLocal());
        terminateComboBox.setSelectedItem(Setup.getTrainIconColorTerminate());

        // comment
        JPanel pC = new JPanel();
        pC.setLayout(new GridBagLayout());
        pC.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Comment")));
        JScrollPane commentScroller = new JScrollPane(commentTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        addItem(pC, commentScroller, 0, 0);

        pIcon.add(pC);

        // adjust text area width based on window size
//        adjustTextAreaColumnWidth(commentScroller, commentTextArea);
        // row 15
        JPanel pControl = new JPanel();
        pControl.setLayout(new GridBagLayout());
        addItem(pControl, restoreButton, 0, 9);
        addItem(pControl, backupButton, 1, 9);
        addItem(pControl, saveButton, 3, 9);

        add(panelPane);
        add(options);
        add(pIconPane);
        add(pControl);

        // setup buttons
        addButtonAction(backupButton);
        addButtonAction(restoreButton);
        addButtonAction(saveButton);
        addCheckBoxAction(eastCheckBox);
        addCheckBoxAction(northCheckBox);

        initMinimumSize(new Dimension(Control.panelWidth700, Control.panelHeight500));

        // now provide the railroad name
        railroadNameTextField.setText(Setup.getRailroadName());
        //DAB commented out these three lines to always allow user to directly change the railroad name from operations
//        if (Setup.getRailroadName().equals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName())) {
//            railroadNameTextField.setEnabled(false);
//        }
        createShutDownTask();
    }

    // Save, Delete, Add buttons
    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == backupButton) {
            // Backup and Restore dialogs are now modal. so no need to check for an existing instance
            BackupDialog bd = new BackupDialog();
            bd.pack();
            bd.setLocationRelativeTo(null);
            bd.setVisible(true);
        }
        if (ae.getSource() == restoreButton) {
            RestoreDialog rd = new RestoreDialog();
            rd.pack();
            rd.setLocationRelativeTo(null);
            rd.setVisible(true);
        }
        if (ae.getSource() == saveButton) {
            save();
        }
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "checks for instance of OperationsSetupFrame")
    private void save() {
        // check input fields
        int maxTrainLength;
        try {
            maxTrainLength = Integer.parseInt(maxLengthTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MaxLength"),
                    Bundle.getMessage("CanNotAcceptNumber"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Integer.parseInt(maxEngineSizeTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MaxEngine"),
                    Bundle.getMessage("CanNotAcceptNumber"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Integer.parseInt(hptTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("HPT"), Bundle.getMessage("CanNotAcceptNumber"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Integer.parseInt(switchTimeTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("MoveTime"), Bundle.getMessage("CanNotAcceptNumber"),
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Integer.parseInt(travelTimeTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("TravelTime"), Bundle
                    .getMessage("CanNotAcceptNumber"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (!yearTextField.getText().trim().equals("")) {
                Integer.parseInt(yearTextField.getText().trim());
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("BorderLayoutYearModeled"), Bundle
                    .getMessage("CanNotAcceptNumber"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        // if max train length has changed, check routes
        checkRoutes();

        // set car types
        if (typeDesc.isSelected() && !Setup.getCarTypes().equals(Setup.DESCRIPTIVE) || typeAAR.isSelected()
                && !Setup.getCarTypes().equals(Setup.AAR)) {

            // backup files before changing car type descriptions
            AutoBackup backup = new AutoBackup();
            try {
                backup.autoBackup();
            } catch (Exception ex) {
                UnexpectedExceptionContext context = new UnexpectedExceptionContext(ex,
                        "Auto backup before changing Car types"); // NOI18N
                new ExceptionDisplayFrame(context);
            }

            if (typeDesc.isSelected()) {
                InstanceManager.getDefault(CarTypes.class).changeDefaultNames(Setup.DESCRIPTIVE);
                Setup.setCarTypes(Setup.DESCRIPTIVE);
            } else {
                InstanceManager.getDefault(CarTypes.class).changeDefaultNames(Setup.AAR);
                Setup.setCarTypes(Setup.AAR);
            }

            // save all the modified files
            OperationsXml.save();
        }
        // main menu enabled?
        Setup.setMainMenuEnabled(mainMenuCheckBox.isSelected());
        Setup.setCloseWindowOnSaveEnabled(closeOnSaveCheckBox.isSelected());
        Setup.setAutoSaveEnabled(autoSaveCheckBox.isSelected());
        Setup.setAutoBackupEnabled(autoBackupCheckBox.isSelected());

        // add panel name to setup
        Setup.setPanelName(panelTextField.getText());

        // train Icon X&Y
        Setup.setTrainIconCordEnabled(iconCheckBox.isSelected());
        Setup.setTrainIconAppendEnabled(appendCheckBox.isSelected());

        // save train icon colors
        Setup.setTrainIconColorNorth((String) northComboBox.getSelectedItem());
        Setup.setTrainIconColorSouth((String) southComboBox.getSelectedItem());
        Setup.setTrainIconColorEast((String) eastComboBox.getSelectedItem());
        Setup.setTrainIconColorWest((String) westComboBox.getSelectedItem());
        Setup.setTrainIconColorLocal((String) localComboBox.getSelectedItem());
        Setup.setTrainIconColorTerminate((String) terminateComboBox.getSelectedItem());
        // set train direction
        int direction = 0;
        if (eastCheckBox.isSelected()) {
            direction = Setup.EAST + Setup.WEST;
        }
        if (northCheckBox.isSelected()) {
            direction += Setup.NORTH + Setup.SOUTH;
        }
        Setup.setTrainDirection(direction);
        Setup.setMaxNumberEngines(Integer.parseInt(maxEngineSizeTextField.getText()));
        Setup.setHorsePowerPerTon(Integer.parseInt(hptTextField.getText()));
        // set switch time
        Setup.setSwitchTime(Integer.parseInt(switchTimeTextField.getText()));
        // set travel time
        Setup.setTravelTime(Integer.parseInt(travelTimeTextField.getText()));
        // set scale
        if (scaleZ.isSelected()) {
            Setup.setScale(Setup.Z_SCALE);
        }
        if (scaleN.isSelected()) {
            Setup.setScale(Setup.N_SCALE);
        }
        if (scaleTT.isSelected()) {
            Setup.setScale(Setup.TT_SCALE);
        }
        if (scaleOO.isSelected()) {
            Setup.setScale(Setup.OO_SCALE);
        }
        if (scaleHOn3.isSelected()) {
            Setup.setScale(Setup.HOn3_SCALE);
        }
        if (scaleHO.isSelected()) {
            Setup.setScale(Setup.HO_SCALE);
        }
        if (scaleSn3.isSelected()) {
            Setup.setScale(Setup.Sn3_SCALE);
        }
        if (scaleS.isSelected()) {
            Setup.setScale(Setup.S_SCALE);
        }
        if (scaleOn3.isSelected()) {
            Setup.setScale(Setup.On3_SCALE);
        }
        if (scaleO.isSelected()) {
            Setup.setScale(Setup.O_SCALE);
        }
        if (scaleG.isSelected()) {
            Setup.setScale(Setup.G_SCALE);
        }
        if (!railroadNameTextField.getText().equals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName())) {
            Setup.setRailroadName(railroadNameTextField.getText());
            int results = JOptionPane.showConfirmDialog(this, MessageFormat.format(Bundle
                    .getMessage("ChangeRailroadName"), new Object[]{
                            InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), Setup.getRailroadName()}), Bundle
                    .getMessage("ChangeJMRIRailroadName"), JOptionPane.YES_NO_OPTION);
            if (results == JOptionPane.OK_OPTION) {               
                InstanceManager.getDefault(WebServerPreferences.class).setRailroadName(Setup.getRailroadName());
                InstanceManager.getDefault(WebServerPreferences.class).save();
            }
        }
        // Set Unit of Length
        if (feetUnit.isSelected()) {
            Setup.setLengthUnit(Setup.FEET);
        }
        if (meterUnit.isSelected()) {
            Setup.setLengthUnit(Setup.METER);
        }
        Setup.setYearModeled(yearTextField.getText().trim());
        // warn about train length being too short
        if (maxTrainLength != Setup.getMaxTrainLength()) {
            if (maxTrainLength < 500 && Setup.getLengthUnit().equals(Setup.FEET) || maxTrainLength < 160
                    && Setup.getLengthUnit().equals(Setup.METER)) {
                JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("LimitTrainLength"),
                        new Object[]{maxTrainLength, Setup.getLengthUnit().toLowerCase()}), Bundle
                        .getMessage("WarningTooShort"), JOptionPane.WARNING_MESSAGE);
            }
        }
        // set max train length
        Setup.setMaxTrainLength(Integer.parseInt(maxLengthTextField.getText()));
        Setup.setComment(commentTextArea.getText());

        InstanceManager.getDefault(OperationsSetupXml.class).writeOperationsFile();
        if (Setup.isCloseWindowOnSaveEnabled() && this.getTopLevelAncestor() instanceof OperationsSetupFrame) {
            ((OperationsSetupFrame) this.getTopLevelAncestor()).dispose();
        }

    }

    // if max train length has changed, check routes
    private void checkRoutes() {
        int maxLength = Integer.parseInt(maxLengthTextField.getText());
        if (maxLength > Setup.getMaxTrainLength()) {
            JOptionPane.showMessageDialog(this, Bundle.getMessage("RouteLengthNotModified"), MessageFormat.format(
                    Bundle.getMessage("MaxTrainLengthIncreased"), new Object[]{maxLength,
                Setup.getLengthUnit().toLowerCase()}), JOptionPane.INFORMATION_MESSAGE);
        }
        if (maxLength < Setup.getMaxTrainLength()) {
            StringBuilder sb = new StringBuilder();
            List<Route> routes = InstanceManager.getDefault(RouteManager.class).getRoutesByNameList();
            int count = 0;
            for (Route route : routes) {
                for (RouteLocation rl : route.getLocationsBySequenceList()) {
                    if (rl.getMaxTrainLength() > maxLength) {
                        String s = MessageFormat.format(Bundle.getMessage("RouteMaxLengthExceeds"), new Object[]{
                            route.getName(), rl.getName(), rl.getMaxTrainLength(), maxLength});
                        log.info(s);
                        sb.append(s).append(NEW_LINE);
                        count++;
                        break;
                    }
                }
                // maximum of 20 route warnings
                if (count > 20) {
                    sb.append(Bundle.getMessage("More")).append(NEW_LINE);
                    break;
                }
            }
            if (sb.length() > 0) {
                JOptionPane.showMessageDialog(this, sb.toString(), Bundle.getMessage("YouNeedToAdjustRoutes"),
                        JOptionPane.WARNING_MESSAGE);
                if (JOptionPane.showConfirmDialog(null, MessageFormat.format(Bundle
                        .getMessage("ChangeMaximumTrainDepartureLength"), new Object[]{maxLength}), Bundle
                        .getMessage("ModifyAllRoutes"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    routes.stream().forEach(
                            (route) -> {
                                route.getLocationsBySequenceList().stream().filter(
                                        (rl) -> (rl.getMaxTrainLength() > maxLength)).map(
                                        (rl) -> {
                                            log.debug("Setting route ({}) routeLocation ({}) max traim length to {}",
                                                    route.getName(), rl.getName(), maxLength); // NOI18N
                                            return rl;
                                        }).forEach((rl) -> {
                                    rl.setMaxTrainLength(maxLength);
                                });
                            });
                    // save the route changes
                    InstanceManager.getDefault(RouteManagerXml.class).writeOperationsFile();
                }
            }
        }
    }

    @Override
    public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == northCheckBox) {
            if (!northCheckBox.isSelected()) {
                eastCheckBox.setSelected(true);
            }
        }
        if (ae.getSource() == eastCheckBox) {
            if (!eastCheckBox.isSelected()) {
                northCheckBox.setSelected(true);
            }
        }
        int direction = 0;
        if (eastCheckBox.isSelected()) {
            direction += Setup.EAST;
        }
        if (northCheckBox.isSelected()) {
            direction += Setup.NORTH;
        }
        setDirectionCheckBox(direction);
    }

    private void setScale() {
        int scale = Setup.getScale();
        switch (scale) {
            case Setup.Z_SCALE:
                scaleZ.setSelected(true);
                break;
            case Setup.N_SCALE:
                scaleN.setSelected(true);
                break;
            case Setup.TT_SCALE:
                scaleTT.setSelected(true);
                break;
            case Setup.HOn3_SCALE:
                scaleHOn3.setSelected(true);
                break;
            case Setup.OO_SCALE:
                scaleOO.setSelected(true);
                break;
            case Setup.HO_SCALE:
                scaleHO.setSelected(true);
                break;
            case Setup.Sn3_SCALE:
                scaleSn3.setSelected(true);
                break;
            case Setup.S_SCALE:
                scaleS.setSelected(true);
                break;
            case Setup.On3_SCALE:
                scaleOn3.setSelected(true);
                break;
            case Setup.O_SCALE:
                scaleO.setSelected(true);
                break;
            case Setup.G_SCALE:
                scaleG.setSelected(true);
                break;
            default:
                log.error("Unknown scale");
        }
    }

    private void setCarTypes() {
        typeDesc.setSelected(Setup.getCarTypes().equals(Setup.DESCRIPTIVE));
        typeAAR.setSelected(Setup.getCarTypes().equals(Setup.AAR));
    }

    private void setDirectionCheckBox(int direction) {
        eastCheckBox.setSelected((direction & Setup.EAST) == Setup.EAST);
        textIconEast.setVisible((direction & Setup.EAST) == Setup.EAST);
        eastComboBox.setVisible((direction & Setup.EAST) == Setup.EAST);
        textIconWest.setVisible((direction & Setup.EAST) == Setup.EAST);
        westComboBox.setVisible((direction & Setup.EAST) == Setup.EAST);
        northCheckBox.setSelected((direction & Setup.NORTH) == Setup.NORTH);
        textIconNorth.setVisible((direction & Setup.NORTH) == Setup.NORTH);
        northComboBox.setVisible((direction & Setup.NORTH) == Setup.NORTH);
        textIconSouth.setVisible((direction & Setup.NORTH) == Setup.NORTH);
        southComboBox.setVisible((direction & Setup.NORTH) == Setup.NORTH);
    }

    private void setLengthUnit() {
        feetUnit.setSelected(Setup.getLengthUnit().equals(Setup.FEET));
        meterUnit.setSelected(Setup.getLengthUnit().equals(Setup.METER));
    }

    private void loadIconComboBox(JComboBox<String> comboBox) {
        for (String color : LocoIcon.getLocoColors()) {
            comboBox.addItem(color);
        }
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("propertyChange ({}), new: ({})", e.getPropertyName(), e.getNewValue());
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("TitleOperationsSetup");
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        this.save();
    }

    @Override
    public boolean isDirty() {
        if (// set car types
                (typeDesc.isSelected() && !Setup.getCarTypes().equals(Setup.DESCRIPTIVE))
                || (typeAAR.isSelected() && !Setup.getCarTypes().equals(Setup.AAR))
                // main menu enabled?
                || Setup.isMainMenuEnabled() != mainMenuCheckBox.isSelected()
                || Setup.isCloseWindowOnSaveEnabled() != closeOnSaveCheckBox.isSelected()
                || Setup.isAutoSaveEnabled() != autoSaveCheckBox.isSelected()
                || Setup.isAutoBackupEnabled() != autoBackupCheckBox.isSelected()
                // add panel name to setup
                || !Setup.getPanelName().equals(panelTextField.getText())
                // train Icon X&Y
                || Setup.isTrainIconCordEnabled() != iconCheckBox.isSelected()
                || Setup.isTrainIconAppendEnabled() != appendCheckBox.isSelected()
                // train Icon X&Y
                || Setup.isTrainIconCordEnabled() != iconCheckBox.isSelected()
                || Setup.isTrainIconAppendEnabled() != appendCheckBox.isSelected()
                // save train icon colors
                || !Setup.getTrainIconColorNorth().equals(northComboBox.getSelectedItem())
                || !Setup.getTrainIconColorSouth().equals(southComboBox.getSelectedItem())
                || !Setup.getTrainIconColorEast().equals(eastComboBox.getSelectedItem())
                || !Setup.getTrainIconColorWest().equals(westComboBox.getSelectedItem())
                || !Setup.getTrainIconColorLocal().equals(localComboBox.getSelectedItem())
                || !Setup.getTrainIconColorTerminate().equals(terminateComboBox.getSelectedItem())
                || Setup.getMaxNumberEngines() != Integer.parseInt(maxEngineSizeTextField.getText())
                || Setup.getHorsePowerPerTon() != Integer.parseInt(hptTextField.getText())
                // switch time
                || Setup.getSwitchTime() != Integer.parseInt(switchTimeTextField.getText())
                // travel time
                || Setup.getTravelTime() != Integer.parseInt(travelTimeTextField.getText())
                || !Setup.getYearModeled().equals(yearTextField.getText().trim())
                || Setup.getMaxTrainLength() != Integer.parseInt(maxLengthTextField.getText())
                || !Setup.getComment().equals(this.commentTextArea.getText())) {
            return true;
        }

        // set train direction
        int direction = 0;
        if (eastCheckBox.isSelected()) {
            direction = Setup.EAST + Setup.WEST;
        }
        if (northCheckBox.isSelected()) {
            direction += Setup.NORTH + Setup.SOUTH;
        }
        // set scale
        int scale = 0;
        if (scaleZ.isSelected()) {
            scale = Setup.Z_SCALE;
        }
        if (scaleN.isSelected()) {
            scale = Setup.N_SCALE;
        }
        if (scaleTT.isSelected()) {
            scale = Setup.TT_SCALE;
        }
        if (scaleOO.isSelected()) {
            scale = Setup.OO_SCALE;
        }
        if (scaleHOn3.isSelected()) {
            scale = Setup.HOn3_SCALE;
        }
        if (scaleHO.isSelected()) {
            scale = Setup.HO_SCALE;
        }
        if (scaleSn3.isSelected()) {
            scale = Setup.Sn3_SCALE;
        }
        if (scaleS.isSelected()) {
            scale = Setup.S_SCALE;
        }
        if (scaleOn3.isSelected()) {
            scale = Setup.On3_SCALE;
        }
        if (scaleO.isSelected()) {
            scale = Setup.O_SCALE;
        }
        if (scaleG.isSelected()) {
            scale = Setup.G_SCALE;
        }
        String lengthUnit = "";
        // Set Unit of Length
        if (feetUnit.isSelected()) {
            lengthUnit = Setup.FEET;
        }
        if (meterUnit.isSelected()) {
            lengthUnit = Setup.METER;
        }
        return ( // train direction
                Setup.getTrainDirection() != direction
                // scale
                || Setup.getScale() != scale
                || !Setup.getRailroadName().equals(this.railroadNameTextField.getText())
                // unit of length
                || !Setup.getLengthUnit().equals(lengthUnit));
    }
}
