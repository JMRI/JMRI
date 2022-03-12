package jmri.jmrit.beantable;

import com.alexandriasoftware.swing.Validation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.border.Border;

import jmri.*;
import jmri.jmrit.beantable.light.LightControlPane;
import jmri.jmrit.beantable.light.LightIntensityPane;
import jmri.jmrit.beantable.light.LightTableDataModel;
import jmri.NamedBean.DisplayOptions;
import jmri.swing.ManagerComboBox;
import jmri.swing.SystemNameValidator;
import jmri.util.JmriJFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a LightTable GUI.
 * <p>
 * Based on SignalHeadTableAction.java
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Egbert Broerse Copyright (C) 2017
 */
public class LightTableAction extends AbstractTableAction<Light> {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param s title of the action
     */
    public LightTableAction(String s) {
        super(s);
        // disable ourself if there is no primary Light manager available
        if (lightManager == null) {
            super.setEnabled(false);
        }
    }

    public LightTableAction() {
        this(Bundle.getMessage("TitleLightTable"));
    }

    protected LightManager lightManager = InstanceManager.getNullableDefault(jmri.LightManager.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setManager(@Nonnull Manager<Light> man) {
        if (man instanceof LightManager) {
            lightManager = (LightManager) man;
        }
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Lights.
     */
    @Override
    protected void createModel() {
        m = new LightTableDataModel(lightManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleLightTable"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.LightTable";
    }

    JmriJFrame addFrame = null;
    Light curLight = null;
    boolean lightCreatedOrUpdated = false;
    boolean noWarn = false;

    // items for Add/Edit Light frame
    JLabel systemLabel = new JLabel(Bundle.getMessage("SystemConnectionLabel"));
    ManagerComboBox<Light> prefixBox = new ManagerComboBox<>();
    JCheckBox addRangeBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JTextField hardwareAddressTextField = new JTextField(10);
    SystemNameValidator hardwareAddressValidator;
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 50, 1); // maximum 50 items
    JSpinner numberToAdd = new JSpinner(rangeSpinner);
    JLabel labelNumToAdd = new JLabel("   " + Bundle.getMessage("LabelNumberToAdd"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";

    JLabel systemNameLabel = new JLabel(Bundle.getMessage("LabelSystemName") + " ");
    JTextField userName = new JTextField(20);
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName") + " ");
    JButton create;
    JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));
    PropertyChangeListener colorChangeListener;

    JLabel status1 = new JLabel(Bundle.getMessage("LightCreateInst"));
    JLabel status2 = new JLabel("");
    Manager<Light> connectionChoice = null;
    private LightIntensityPane lightIntensityPanel;
    private LightControlPane lightControlPanel;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPressed(ActionEvent e) {
        cancelPressed(null);

        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddLight"), false, true);
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.LightAddEdit", true);
            addFrame.setLocation(100, 30);
            Container contentPane = addFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            configureManagerComboBox(prefixBox, lightManager, LightManager.class);
            panel1.add(systemLabel);
            panel1.add(prefixBox);
            panel1.add(new JLabel("   "));
            panel1.add(addRangeBox);
            addRangeBox.setVisible(true); // reset after Edit Light
            addRangeBox.setToolTipText(Bundle.getMessage("LightAddRangeHint"));
            addRangeBox.addActionListener((ActionEvent e1) -> addRangeChanged());
            panel1.add(systemNameLabel);
            systemNameLabel.setVisible(false);
            prefixBox.setToolTipText(Bundle.getMessage("LightSystemHint"));
            prefixBox.addActionListener((ActionEvent e1) -> prefixChanged());
            contentPane.add(panel1);
            JPanel hardwareAddressPanel = new JPanel();
            hardwareAddressPanel.setLayout(new FlowLayout());
            hardwareAddressPanel.add(new JLabel(Bundle.getMessage("LabelHardwareAddress")));
            hardwareAddressPanel.add(hardwareAddressTextField);
            hardwareAddressTextField.setText(""); // reset from possible previous use
            hardwareAddressTextField.setToolTipText(Bundle.getMessage("LightHardwareAddressHint"));
            hardwareAddressTextField.setName("hwAddressTextField"); // for GUI test NOI18N
            
            if (hardwareAddressValidator==null){
                hardwareAddressValidator = new SystemNameValidator(hardwareAddressTextField, Objects.requireNonNull(prefixBox.getSelectedItem()), true);
            } else {
                hardwareAddressValidator.setManager(prefixBox.getSelectedItem());
            }
            
            hardwareAddressTextField.setInputVerifier(hardwareAddressValidator);
            prefixBox.addActionListener((evt) -> hardwareAddressValidator.setManager(prefixBox.getSelectedItem()));
            hardwareAddressValidator.addPropertyChangeListener("validation", (evt) -> { // NOI18N
                Validation validation = hardwareAddressValidator.getValidation();
                Validation.Type type = validation.getType();
                create.setEnabled(type != Validation.Type.WARNING && type != Validation.Type.DANGER);
                String message = validation.getMessage();
                if (message == null) {
                    status1.setText("");
                } else {
                    message = message.trim();
                    if (message.startsWith("<html>") && message.contains("<br>")) {
                        message = message.substring(0, message.indexOf("<br>"));
                        if (!message.endsWith("</html>")) {
                            message = message + "</html>";
                        }
                    }
                    status1.setText(message);
                }
            });
            // tooltip and entry mask for sysNameTextField will be assigned later by prefixChanged()
            hardwareAddressPanel.add(labelNumToAdd);
            hardwareAddressPanel.add(numberToAdd);
            numberToAdd.setToolTipText(Bundle.getMessage("LightNumberToAddHint"));
            contentPane.add(hardwareAddressPanel);
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            panel2.add(userNameLabel);
            panel2.add(userName);
            userName.setText(""); // reset from possible previous use
            userName.setToolTipText(Bundle.getMessage("LightUserNameHint"));
            userName.setName("userName"); // for GUI test NOI18N
            prefixBox.setName("prefixBox"); // for GUI test NOI18N
            contentPane.add(panel2);
            lightIntensityPanel = new LightIntensityPane(false);
            Border varPanelTitled = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Bundle.getMessage("LightVariableBorder"));
            lightIntensityPanel.setBorder(varPanelTitled);
            contentPane.add(lightIntensityPanel);
            // light control table
            lightControlPanel = new LightControlPane();
            Border panel3Titled = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                Bundle.getMessage("LightControllerTitlePlural"));
            lightControlPanel.setBorder(panel3Titled);
            contentPane.add(lightControlPanel);
            // message items
            JPanel panel4 = new JPanel();
            panel4.setLayout(new BoxLayout(panel4, BoxLayout.Y_AXIS));
            JPanel panel41 = new JPanel();
            panel41.setLayout(new FlowLayout());
            // add status bar above buttons
            panel41.add(status1);
            status1.setText(Bundle.getMessage("LightCreateInst"));
            status1.setFont(status1.getFont().deriveFont(0.9f * systemNameLabel.getFont().getSize())); // a bit smaller
            status1.setForeground(Color.gray);
            JPanel panel42 = new JPanel();
            panel42.setLayout(new FlowLayout());
            panel42.add(status2);
            status2.setText(Bundle.getMessage("LightCreateInst"));
            status2.setFont(status2.getFont().deriveFont(0.9f * systemNameLabel.getFont().getSize())); // a bit smaller
            status2.setForeground(Color.gray);
            status2.setText("");
            status2.setVisible(false);
            panel4.add(panel41);
            panel4.add(panel42);
            contentPane.add(panel4);
            // buttons at bottom of window
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout(FlowLayout.TRAILING));
            panel5.add(cancel);
            cancel.setText(Bundle.getMessage("ButtonCancel"));
            cancel.addActionListener(this::cancelPressed);
            cancel.setToolTipText(Bundle.getMessage("LightCancelButtonHint"));
            panel5.add(create = new JButton(Bundle.getMessage("ButtonCreate")));
            create.addActionListener(this::createPressed);
            create.setToolTipText(Bundle.getMessage("LightCreateButtonHint"));
            create.setName("createButton"); // for GUI test NOI18N
            create.setVisible(true);
            contentPane.add(panel5);
            hardwareAddressValidator.verify(hardwareAddressTextField);
        }
        prefixChanged();
        addFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cancelPressed(null);
            }
        });
        create.setEnabled(false); // start as disabled (false) until a valid entry is typed in
        // reset statusBar text
        status1.setText(Bundle.getMessage("LightCreateInst"));
        status1.setForeground(Color.gray);
        
        addFrame.setEscapeKeyClosesWindow(true);
        addFrame.getRootPane().setDefaultButton(create);

        addFrame.pack();
        addFrame.setVisible(true);
    }

    private String addEntryToolTip;

    protected void prefixChanged() {
        if (prefixBox.getSelectedItem() != null) {
            lightIntensityPanel.setVisible(supportsVariableLights());
            // behaves like the AddNewHardwareDevice pane (dim if not available, do not hide)
            addRangeBox.setEnabled(canAddRange());
            addRangeBox.setSelected(false);
            numberToAdd.setValue(1);
            numberToAdd.setEnabled(false);
            labelNumToAdd.setEnabled(false);
            // show tooltip for selected system connection
            connectionChoice = prefixBox.getSelectedItem(); // store in Field for CheckedTextField
            // Update tooltip in the Add Light pane to match system connection selected from combobox.
            log.debug("Connection choice = [{}]", connectionChoice);
            // get tooltip from ProxyLightManager
            String systemPrefix = connectionChoice.getSystemPrefix();
            addEntryToolTip = connectionChoice.getEntryToolTip();
            addRangeBox.setEnabled(((LightManager) connectionChoice).allowMultipleAdditions(systemPrefix));
            log.debug("DefaultLightManager tip: {}", addEntryToolTip);
            // show Hardware address field tooltip in the Add Light pane to match system connection selected from combobox
            if (addEntryToolTip != null) {
                hardwareAddressTextField.setToolTipText(
                        Bundle.getMessage("AddEntryToolTipLine1",
                                connectionChoice.getMemo().getUserName(),
                                Bundle.getMessage("Lights"),
                                addEntryToolTip));
                hardwareAddressValidator.setToolTipText(hardwareAddressTextField.getToolTipText());
                hardwareAddressValidator.verify(hardwareAddressTextField);
            }
            create.setEnabled(true); // too severe to start as disabled (false) until we fully support validation
            addFrame.pack();
            addFrame.setVisible(true);
        }
    }

    protected void addRangeChanged() {
        numberToAdd.setEnabled(addRangeBox.isSelected());
        labelNumToAdd.setEnabled(addRangeBox.isSelected());
    }

    /**
     * Activate Add a range option if manager accepts adding more than 1 Light.
     * TODO: Will only verify against formats which accept "11" as a Hardware address.
     */
    private boolean canAddRange() {
        String testSysName = Objects.requireNonNull(prefixBox.getSelectedItem()).getSystemPrefix() + "L11";
        return lightManager.allowMultipleAdditions(testSysName);
    }

    /**
     * Check if LightManager supports variable Lights.
     * TODO: Will only verify against formats which accept "11" as a Hardware address.
     * 
     * @return true if system can support variable lights.
     */
    boolean supportsVariableLights() {
        String testSysName = Objects.requireNonNull(prefixBox.getSelectedItem()).getSystemPrefix() + "L11";
        return lightManager.supportsVariableLights(testSysName);
    }

    /**
     * Create lights when the Create New button on the Add/Create pane is
     * pressed and entry is valid.
     *
     * @param e the button press action
     */
    void createPressed(ActionEvent e) {

        status1.setForeground(Color.gray); // reset
        status1.setText("");
        String lightPrefix = Objects.requireNonNull(prefixBox.getSelectedItem()).getSystemPrefix() + "L";
        String turnoutPrefix = prefixBox.getSelectedItem().getSystemPrefix() + "T";
        String curAddress = hardwareAddressTextField.getText();
        // first validation is provided by HardwareAddress ValidatedTextField on yield focus
        if (curAddress.length() < 1) {
            log.warn("Hardware Address was not entered");
            status1.setText(Bundle.getMessage("LightError17"));
            status1.setForeground(Color.red);
            status2.setVisible(false);
            hardwareAddressTextField.setBackground(Color.red);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        } else {
            hardwareAddressTextField.setBackground(Color.white);
        }
        String suName = lightPrefix + curAddress;
        String uName = userName.getText();
        if (uName.isEmpty()) {
            uName = null;   // a blank field means no user name
        }
        // Does System Name have a valid format?
        if (InstanceManager.getDefault(LightManager.class).validSystemNameFormat(suName) != Manager.NameValidity.VALID) {
            // Invalid System Name format
            log.warn("Invalid Light system name format entered: {}", suName);
            status1.setText(Bundle.getMessage("LightError3"));
            status1.setForeground(Color.red);
            status2.setText(Bundle.getMessage("LightError6"));
            status2.setVisible(true);
            hardwareAddressTextField.setBackground(Color.red);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        } else {
            hardwareAddressTextField.setBackground(Color.white);
        }
        // check if a Light with this name already exists
        Light g = InstanceManager.getDefault(LightManager.class).getBySystemName(suName);
        if (g != null) {
            // Light already exists
            status1.setText(Bundle.getMessage("LightError1"));
            status1.setForeground(Color.red);
            status2.setText(Bundle.getMessage("LightError2"));
            status2.setVisible(true);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        }
        // check if Light exists under an alternate name if an alternate name exists
        String altName = InstanceManager.getDefault(LightManager.class).convertSystemNameToAlternate(suName);
        if (!altName.isEmpty()) {
            g = InstanceManager.getDefault(LightManager.class).getBySystemName(altName);
            if (g != null) {
                // Light already exists
                status1.setText(Bundle.getMessage("LightError10", altName));
                status1.setForeground(Color.red);
                status2.setVisible(false);
                addFrame.pack();
                addFrame.setVisible(true);
                return;
            }
        }
        // check if a Light with the same user name exists
        if (uName != null) {
            g = InstanceManager.getDefault(LightManager.class).getByUserName(uName);
            if (g != null) {
                // Light with this user name already exists
                status1.setText(Bundle.getMessage("LightError8"));
                status1.setForeground(Color.red);
                status2.setText(Bundle.getMessage("LightError9"));
                status2.setVisible(true);
                addFrame.pack();
                addFrame.setVisible(true);
                return;
            }
        }
        // check if System Name corresponds to configured hardware
        if (!InstanceManager.getDefault(LightManager.class).validSystemNameConfig(suName)) {
            // System Name not in configured hardware
            status1.setText(Bundle.getMessage("LightError5"));
            status1.setForeground(Color.red);
            status2.setText(Bundle.getMessage("LightError6"));
            status2.setVisible(true);
            addFrame.pack();
            addFrame.setVisible(true);
            return;
        }
        // check if requested Light uses the same address as a Turnout
        String testSN = turnoutPrefix + curAddress;
        Turnout testT = InstanceManager.turnoutManagerInstance().
                getBySystemName(testSN);
        if (testT != null) {
            // Address (number) is already used as a Turnout
            log.warn("Requested Light {} uses same address as Turnout {}", suName, testT);
            if (!noWarn) {
                int selectedValue = JOptionPane.showOptionDialog(addFrame,
                        Bundle.getMessage("LightWarn5", suName, testSN),
                        Bundle.getMessage("WarningTitle"),
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                        new Object[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonYesPlus"),
                                Bundle.getMessage("ButtonNo")}, Bundle.getMessage("ButtonNo")); // default choice = No
                if (selectedValue == 1) {
                    return;   // return without creating on "No" response
                }
                if (selectedValue == 2) {
                    // Suppress future warnings, and continue
                    noWarn = true;
                }
            }
            // Light with this system name address (number) already exists as a turnout
            status2.setText(Bundle.getMessage("LightWarn4") + " " + testSN + ".");
            status1.setForeground(Color.red);
            status2.setVisible(true);
        }
        // Check multiple Light creation request, if supported
        int numberOfLights = 1;
        int startingAddress = 0;
        if ((InstanceManager.getDefault(LightManager.class).allowMultipleAdditions(suName))
                && addRangeBox.isSelected()) {
            // get number requested
            numberOfLights = (Integer) numberToAdd.getValue();

            // convert numerical hardware address
            try {
                startingAddress = Integer.parseInt(hardwareAddressTextField.getText());

            } catch (NumberFormatException ex) {
                status1.setText(Bundle.getMessage("LightError18"));
                status2.setVisible(false);
                addFrame.pack();
                addFrame.setVisible(true);
                log.error("Unable to convert '{}' to a number.", hardwareAddressTextField.getText());
                return;
            }
            // check that requested address range is available
            int add = startingAddress;
            String testAdd;
            for (int i = 0; i < numberOfLights; i++) {
                testAdd = lightPrefix + add;
                if (InstanceManager.getDefault(LightManager.class).getBySystemName(testAdd) != null) {
                    status1.setText(Bundle.getMessage("LightError19"));
                    status2.setVisible(true);
                    addFrame.pack();
                    addFrame.setVisible(true);
                    log.error("Range not available - {} already exists.", testAdd);
                    return;
                }
                testAdd = turnoutPrefix + add;
                if (InstanceManager.turnoutManagerInstance().getBySystemName(testAdd) != null) {
                    status1.setText(Bundle.getMessage("LightError19"));
                    status1.setForeground(Color.red);
                    status2.setVisible(true);
                    addFrame.pack();
                    addFrame.setVisible(true);
                    log.error("Range not available - {} already exists.", testAdd);
                    return;
                }
                add++;
            }
        }

        // Create a single new Light, or the first Light of a range
        try {
            g = InstanceManager.getDefault(LightManager.class).newLight(suName, uName);
        } catch (IllegalArgumentException ex) {
            // user input no good
            handleCreateException(ex, suName);
            return; // without creating
        }
        lightControlPanel.setLightFromControlTable(g);
        if (g instanceof VariableLight) {
            lightIntensityPanel.setLightFromPane((VariableLight)g);
        }
        g.activateLight();
        lightCreatedOrUpdated = true;

        status2.setText("");
        status2.setVisible(false);
        
        // provide feedback to user
        String feedback = Bundle.getMessage("LightCreateFeedback") + " " + g.getDisplayName(DisplayOptions.USERNAME_SYSTEMNAME);
        // create additional lights if requested
        if (numberOfLights > 1) {
            String sxName = "";
            String uxName;
            uxName = uName;
            for (int i = 1; i < numberOfLights; i++) {
                sxName = lightPrefix + (startingAddress + i); // normalize once more to allow specific connection formatting
                if (uxName != null) {
                    uxName = nextName(uxName);
                }
                try {
                    g = lightManager.newLight(sxName, uxName);
                    log.debug("Light {} created",g);
                    // set up this light the same as the first light
                    lightControlPanel.setLightFromControlTable(g);
                    if (g instanceof VariableLight) {
                        lightIntensityPanel.setLightFromPane((VariableLight)g);
                    }
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    handleCreateException(ex, suName);
                    return; // without creating any more Lights
                }
            }
            feedback = feedback + " - " + sxName + ", " + uxName;
        }
        create.setEnabled(false);
        status1.setText(feedback);
        status1.setForeground(Color.gray);
        cancel.setText(Bundle.getMessage("ButtonClose")); // when Create/Apply has been clicked at least once, this is not Revert/Cancel
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void handleCreateException(Exception ex, String sysName) {
        status1.setText(ex.getLocalizedMessage());
        String err = Bundle.getMessage("ErrorBeanCreateFailed",
            InstanceManager.getDefault(LightManager.class).getBeanTypeHandled(),sysName);
        JOptionPane.showMessageDialog(addFrame, err + "\n" + ex.getLocalizedMessage(),
                err, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Respond to the Cancel/Close button on the Add/Edit Light pane.
     *
     * @param e the button press action
     */
    void cancelPressed(ActionEvent e) {

        if (addFrame != null) {
            addFrame.setVisible(false); // hide first for cleaner display
        }
        
        // remind to save, if Light was created or edited
        if (lightCreatedOrUpdated) {
            InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                    showInfoMessage(Bundle.getMessage("ReminderTitle"), Bundle.getMessage("ReminderSaveString",
                            Bundle.getMessage("MenuItemLightTable")),
                            getClassName(),
                            "remindSaveLight"); // NOI18N
        }
        lightCreatedOrUpdated = false;
        // finally, get rid of the add/edit Frame
        if (addFrame != null) {
            
            lightControlPanel.dispose(); // closes any popup windows
            
            removePrefixBoxListener(prefixBox);
            InstanceManager.getDefault(UserPreferencesManager.class).setComboBoxLastSelection(systemSelectionCombo, prefixBox.getSelectedItem().getMemo().getUserName()); // store user pref
            addFrame.dispose();
            addFrame = null;
            create.removePropertyChangeListener(colorChangeListener);
            
        }
    }


    // TODO: Move the next few static String methods to more appropriate place.
    // LightControl.java ?  Multiple Bundle property files will need changing.
    
    public static String lightControlTitle = Bundle.getMessage("LightControlBorder");
    
    /**
     * Get the description of the type of Light Control.
     *
     * @param lc   the light control
     * @param type the type of lc
     * @return description of the type of lc or an empty string if type is not
     *         recognized
     */
    public static String getDescriptionText(LightControl lc, int type) {
        switch (type) {
            case Light.SENSOR_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightSensorControlDes"), lc.getControlSensorName(), getControlSensorSenseText(lc));
            case Light.FAST_CLOCK_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightFastClockDes"),
                        // build 00:00 from 2 fields
                        String.format("%02d:%02d", lc.getFastClockOnHour(), lc.getFastClockOnMin()), String.format("%02d:%02d", lc.getFastClockOffHour(), lc.getFastClockOffMin()));
            case Light.TURNOUT_STATUS_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightTurnoutControlDes"), lc.getControlTurnoutName(), getControlTurnoutStateText(lc));
            case Light.TIMED_ON_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightTimedOnControlDes"), "" + lc.getTimedOnDuration(), lc.getControlTimedOnSensorName(), getControlSensorSenseText(lc));
            case Light.TWO_SENSOR_CONTROL:
                return java.text.MessageFormat.format(Bundle.getMessage("LightTwoSensorControlDes"), lc.getControlSensorName(), lc.getControlSensor2Name(), getControlSensorSenseText(lc));
            default:
                return "";
        }
    }

    private static String getControlSensorSenseText(LightControl lc) {
        if (lc.getControlSensorSense() == Sensor.ACTIVE) {
            return Bundle.getMessage("SensorStateActive");
        }
        return Bundle.getMessage("SensorStateInactive");
    }

    private static String getControlTurnoutStateText(LightControl lc) {
        if (lc.getControlTurnoutState() == Turnout.CLOSED) {
            return InstanceManager.turnoutManagerInstance().getClosedText();
        }
        return InstanceManager.turnoutManagerInstance().getThrownText();
    }

    /**
     * Validates that a physical turnout exists.
     *
     * @param inTurnoutName the (system or user) name of the turnout
     * @param inOpenPane    the pane over which to show dialogs (null to
     *                      suppress dialogs)
     * @return true if valid turnout was entered, false otherwise
     */
    public boolean validatePhysicalTurnout(String inTurnoutName, Component inOpenPane) {
        //check if turnout name was entered
        if (inTurnoutName.isEmpty()) {
            //no turnout entered
            log.debug("no turnout was selected");
            return false;
        }
        //check that the turnout name corresponds to a defined physical turnout
        Turnout t = InstanceManager.turnoutManagerInstance().getTurnout(inTurnoutName);
        if (t == null) {
            //There is no turnout corresponding to this name
            if (inOpenPane != null) {
                JOptionPane.showMessageDialog(inOpenPane,
                        java.text.MessageFormat.format(Bundle.getMessage("LightWarn2"), inTurnoutName),
                        Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
        log.debug("validatePhysicalTurnout('{}')", inTurnoutName);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleLightTable");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClassName() {
        return LightTableAction.class.getName();
    }

    private final static Logger log = LoggerFactory.getLogger(LightTableAction.class);

}
