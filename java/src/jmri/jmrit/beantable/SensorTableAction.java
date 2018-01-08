package jmri.jmrit.beantable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.util.ConnectionNameFromSystemName;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SensorTable GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2009
 */
public class SensorTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <p>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName title of the action
     */
    public SensorTableAction(String actionName) {
        super(actionName);

        // disable ourself if there is no primary sensor manager available
        if (senManager == null) {
            setEnabled(false);
        }
    }

    public SensorTableAction() {
        this(Bundle.getMessage("TitleSensorTable"));
    }

    protected SensorManager senManager = InstanceManager.sensorManagerInstance();

    @Override
    @SuppressFBWarnings("BC_UNCONFIRMED_CAST") // AbstractTableTabAction responsible for getting this right;
    public void setManager(@Nonnull Manager man) {
        senManager = (SensorManager) man;
        if (m != null) {
            m.setManager(senManager);
        }
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Sensors.
     */
    @Override
    protected void createModel() {
        m = new jmri.jmrit.beantable.sensor.SensorTableDataModel(senManager);
    }

    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleSensorTable"));
    }

    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }

    JmriJFrame addFrame = null;

    CheckedTextField hardwareAddressTextField = new CheckedTextField(20);
    // initially allow any 20 char string, updated by prefixBox selection
    JTextField userName = new JTextField(40);
    JComboBox<String> prefixBox = new JComboBox<String>();
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAdd = new JSpinner(rangeSpinner);
    JCheckBox range = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JLabel hwAddressLabel = new JLabel(Bundle.getMessage("LabelHardwareAddress"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    JButton addButton;
    PropertyChangeListener colorChangeListener;
    JLabel statusBar = new JLabel(Bundle.getMessage("HardwareAddStatusEnter"), JLabel.LEADING);
    jmri.UserPreferencesManager p;
    String connectionChoice = "";

    @Override
    protected void addPressed(ActionEvent e) {
        p = InstanceManager.getDefault(jmri.UserPreferencesManager.class);

        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddSensor"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SensorAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener createListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    createPressed(e);
                }
            };
            ActionListener cancelListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelPressed(e);
                }
            };
            ActionListener rangeListener = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    canAddRange(e);
                }
            };
            if (InstanceManager.sensorManagerInstance().getClass().getName().contains("ProxySensorManager")) {
                jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) InstanceManager.sensorManagerInstance();
                List<Manager<Sensor>> managerList = proxy.getManagerList();
                for (int x = 0; x < managerList.size(); x++) {
                    String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                    Boolean addToPrefix = true;
                    // Simple test not to add a system with a duplicate System prefix
                    for (int i = 0; i < prefixBox.getItemCount(); i++) {
                        if ((prefixBox.getItemAt(i)).equals(manuName)) {
                            addToPrefix = false;
                        }
                    }
                    if (addToPrefix) {
                        prefixBox.addItem(manuName);
                    }
                }
                if (p.getComboBoxLastSelection(systemSelectionCombo) != null) {
                    prefixBox.setSelectedItem(p.getComboBoxLastSelection(systemSelectionCombo));
                }
            } else {
                prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(InstanceManager.sensorManagerInstance().getSystemPrefix()));
            }
            userName.setName("userName"); // NOI18N
            prefixBox.setName("prefixBox"); // NOI18N
            addButton = new JButton(Bundle.getMessage("ButtonCreate"));
            addButton.addActionListener(createListener);
            // Define PropertyChangeListener
            colorChangeListener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                    String property = propertyChangeEvent.getPropertyName();
                    if ("background".equals(property)) {
                        if ((Color) propertyChangeEvent.getNewValue() == Color.white) { // valid entry
                            addButton.setEnabled(true);
                        } else { // invalid
                            addButton.setEnabled(false);
                        }
                    }
                }
            };
            hardwareAddressTextField.addPropertyChangeListener(colorChangeListener);
            // create panel
            addFrame.add(new AddNewHardwareDevicePanel(hardwareAddressTextField, userName, prefixBox,
                    numberToAdd, range, addButton, cancelListener, rangeListener, statusBar));
            // tooltip for hwAddressTextField will be assigned later by canAddRange()
            canAddRange(null);
        }
        hardwareAddressTextField.setName("hwAddressTextField"); // for GUI test NOI18N
        hardwareAddressTextField.setBackground(Color.yellow);
        addButton.setEnabled(false); // start as disabled (false) until a valid entry is typed in
        addButton.setName("createButton"); // for GUI test NOI18N
        // reset statusBar text
        statusBar.setText(Bundle.getMessage("HardwareAddStatusEnter"));
        statusBar.setForeground(Color.gray);

        addFrame.pack();
        addFrame.setVisible(true);
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
        addButton.removePropertyChangeListener(colorChangeListener);
    }

    /**
     * Respond to Create new item pressed on Add Sensor pane
     *
     * @param e the click event
     */
    void createPressed(ActionEvent e) {

        int numberOfSensors = 1;

        if (range.isSelected()) {
            numberOfSensors = (Integer) numberToAdd.getValue();
        }
        if (numberOfSensors >= 65) { // limited by JSpinnerModel to 100
            if (JOptionPane.showConfirmDialog(addFrame,
                    Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("Sensors"), numberOfSensors),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }
        String sensorPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
        String sName = null;
        String curAddress = hardwareAddressTextField.getText().trim();
        // initial check for empty entry
        if (curAddress.length() < 1) {
            statusBar.setText(Bundle.getMessage("WarningEmptyHardwareAddress"));
            statusBar.setForeground(Color.red);
            hardwareAddressTextField.setBackground(Color.red);
            return;
        } else {
            hardwareAddressTextField.setBackground(Color.white);
        }

        // Add some entry pattern checking, before assembling sName and handing it to the sensorManager
        String statusMessage = Bundle.getMessage("ItemCreateFeedback", Bundle.getMessage("BeanNameSensor"));
        String errorMessage = null;
        for (int x = 0; x < numberOfSensors; x++) {
            try {
                curAddress = InstanceManager.sensorManagerInstance().getNextValidAddress(curAddress, sensorPrefix);
            } catch (jmri.JmriException ex) {
                InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateUserName", curAddress), "" + ex, "", true, false);
                // directly add to statusBar (but never called?)
                statusBar.setText(Bundle.getMessage("ErrorConvertHW", curAddress));
                statusBar.setForeground(Color.red);
                return;
            }
            if (curAddress == null) {
                log.debug("Error converting HW or getNextValidAddress");
                errorMessage = (Bundle.getMessage("WarningInvalidEntry"));
                statusBar.setForeground(Color.red);
                // The next address returned an error, therefore we stop this attempt and go to the next address.
                break;
            }

            // Compose the proposed system name from parts:
            sName = sensorPrefix + InstanceManager.sensorManagerInstance().typeLetter() + curAddress;
            Sensor s = null;
            try {
                s = InstanceManager.sensorManagerInstance().provideSensor(sName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(sName);
                // Show error message in statusBar
                errorMessage = Bundle.getMessage("WarningInvalidEntry");
                statusBar.setText(errorMessage);
                statusBar.setForeground(Color.gray);
                return;   // return without creating
            }

            String user = userName.getText().trim();
            if ((x != 0) && user != null && !user.equals("")) {
                user = userName.getText() + ":" + x; // add :x to user name starting with 2nd item
            }
            if (user != null && !user.equals("") && (InstanceManager.sensorManagerInstance().getByUserName(user) == null)) {
                s.setUserName(user);
            } else if (user != null && !user.equals("") && InstanceManager.sensorManagerInstance().getByUserName(user) != null && !p.getPreferenceState(getClassName(), "duplicateUserName")) {
                InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateUserName", user), getClassName(), "duplicateUserName", false, true);
            }

            // add first and last names to statusMessage user feedback string
            if (x == 0 || x == numberOfSensors - 1) {
                statusMessage = statusMessage + " " + sName + " (" + user + ")";
            }
            if (x == numberOfSensors - 2) {
                statusMessage = statusMessage + " " + Bundle.getMessage("ItemCreateUpTo") + " ";
            }
            // only mention first and last of range added

            // end of for loop creating range of Sensors
        }

        // provide feedback to user
        if (errorMessage == null) {
            statusBar.setText(statusMessage);
            statusBar.setForeground(Color.gray);
        } else {
            statusBar.setText(errorMessage);
            // statusBar.setForeground(Color.red); // handled when errorMassage is set to differentiate urgency
        }

        p.addComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
        addButton.removePropertyChangeListener(colorChangeListener);
    }

    private String addEntryToolTip;

    /**
     * Activate Add a range option if manager accepts adding more than 1 Sensor
     * and set a manager specific tooltip on the AddNewHardwareDevice pane.
     */
    private void canAddRange(ActionEvent e) {
        range.setEnabled(false);
        range.setSelected(false);
        connectionChoice = (String) prefixBox.getSelectedItem(); // store in Field for CheckedTextField
        if (connectionChoice == null) {
            // Tab All or first time opening, default tooltip
            connectionChoice = "TBD";
        }
        if (senManager.getClass().getName().contains("ProxySensorManager")) {
            jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) senManager;
            List<Manager<Sensor>> managerList = proxy.getManagerList();
            String systemPrefix = ConnectionNameFromSystemName.getPrefixFromName(connectionChoice);
            for (int x = 0; x < managerList.size(); x++) {
                jmri.SensorManager mgr = (jmri.SensorManager) managerList.get(x);
                if (mgr.getSystemPrefix().equals(systemPrefix)) {
                    range.setEnabled(mgr.allowMultipleAdditions(systemPrefix));
                    // get tooltip from ProxySensorManager
                    addEntryToolTip = mgr.getEntryToolTip();
                    log.debug("S add box enabled1");
                    break;
                }
            }
        } else if (senManager.allowMultipleAdditions(ConnectionNameFromSystemName.getPrefixFromName(connectionChoice))) {
            range.setEnabled(true);
            log.debug("S add box enabled2");
            // get tooltip from sensor manager
            addEntryToolTip = senManager.getEntryToolTip();
            log.debug("SensorManager tip");
        }
        // show hwAddressTextField field tooltip in the Add Sensor pane that matches system connection selected from combobox
        hardwareAddressTextField.setToolTipText("<html>"
                + Bundle.getMessage("AddEntryToolTipLine1", connectionChoice, Bundle.getMessage("Sensors"))
                + "<br>" + addEntryToolTip + "</html>");
        hardwareAddressTextField.setBackground(Color.yellow); // reset
        addButton.setEnabled(true); // ambiguous, so start enabled
    }

    void handleCreateException(String hwAddress) {
        JOptionPane.showMessageDialog(addFrame,
                Bundle.getMessage("ErrorSensorAddFailed", hwAddress) + "\n" + Bundle.getMessage("ErrorAddFailedCheck"),
                Bundle.getMessage("ErrorTitle"),
                JOptionPane.ERROR_MESSAGE);
    }

    protected void setDefaultDebounce(JFrame _who) {
        JTextField activeField = new JTextField(String.valueOf(senManager.getDefaultSensorDebounceGoingActive()), 4);
        JTextField inActiveField = new JTextField(String.valueOf(senManager.getDefaultSensorDebounceGoingInActive()), 4);

        JPanel active = new JPanel();
        active.add(new JLabel(Bundle.getMessage("SensorActiveTimer")));
        active.add(activeField);

        JPanel inActive = new JPanel();
        inActive.add(new JLabel(Bundle.getMessage("SensorInactiveTimer")));
        inActive.add(inActiveField);

        int retval = JOptionPane.showOptionDialog(_who,
                Bundle.getMessage("SensorGlobalDebounceMessageBox"), Bundle.getMessage("SensorGlobalDebounceMessageTitle"),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonOK"), active, inActive}, null);
        if (retval != 1) {
            return;
        }

        //We will allow the sensor manager to handle checking if the values have changed
        try {
            long goingActive = Long.valueOf(activeField.getText());
            senManager.setDefaultSensorDebounceGoingActive(goingActive);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(_who, Bundle.getMessage("SensorDebounceActError") + "\n\"" + activeField.getText() + "\"", "Input Error", JOptionPane.ERROR_MESSAGE);
        }

        try {
            long goingInActive = Long.valueOf(inActiveField.getText());
            senManager.setDefaultSensorDebounceGoingInActive(goingInActive);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(_who, Bundle.getMessage("SensorDebounceActError") + "\n\"" + inActiveField.getText() + "\"", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
        m.fireTableDataChanged();
    }

    protected void setDefaultState(JFrame _who) {
        String[] sensorStates = new String[]{Bundle.getMessage("BeanStateUnknown"), Bundle.getMessage("SensorStateInactive"), Bundle.getMessage("SensorStateActive"), Bundle.getMessage("BeanStateInconsistent")};
        JComboBox<String> stateCombo = new JComboBox<String>(sensorStates);
        switch (jmri.managers.InternalSensorManager.getDefaultStateForNewSensors()) {
            case jmri.Sensor.ACTIVE:
                stateCombo.setSelectedItem(Bundle.getMessage("SensorStateActive"));
                break;
            case jmri.Sensor.INACTIVE:
                stateCombo.setSelectedItem(Bundle.getMessage("SensorStateInactive"));
                break;
            case jmri.Sensor.INCONSISTENT:
                stateCombo.setSelectedItem(Bundle.getMessage("BeanStateInconsistent"));
                break;
            default:
                stateCombo.setSelectedItem(Bundle.getMessage("BeanStateUnknown"));
        }
        int retval = JOptionPane.showOptionDialog(_who,
                Bundle.getMessage("SensorInitialStateMessageBox"), Bundle.getMessage("InitialSensorState"),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonCancel"), Bundle.getMessage("ButtonOK"), stateCombo}, null);
        if (retval != 1) {
            return;
        }
        int defaultState = jmri.Sensor.UNKNOWN;
        String selectedState = (String) stateCombo.getSelectedItem();
        if (selectedState.equals(Bundle.getMessage("SensorStateActive"))) {
            defaultState = jmri.Sensor.ACTIVE;
        } else if (selectedState.equals(Bundle.getMessage("SensorStateInactive"))) {
            defaultState = jmri.Sensor.INACTIVE;
        } else if (selectedState.equals(Bundle.getMessage("BeanStateInconsistent"))) {
            defaultState = jmri.Sensor.INCONSISTENT;
        }

        jmri.managers.InternalSensorManager.setDefaultStateForNewSensors(defaultState);

    }

    /**
     * Insert a table specific Defaults menu. Account for the Window and Help
     * menus, which are already added to the menu bar as part of the creation of
     * the JFrame, by adding the Tools menu 2 places earlier unless the table is
     * part of the ListedTableFrame, that adds the Help menu later on.
     *
     * @param f the JFrame of this table
     */
    @Override
    public void setMenuBar(BeanTableFrame f) {
        final jmri.util.JmriJFrame finalF = f; // needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        // check for menu
        boolean menuAbsent = true;
        for (int m = 0; m < menuBar.getMenuCount(); ++m) {
            String name = menuBar.getMenu(m).getAccessibleContext().getAccessibleName();
            if (name.equals(Bundle.getMessage("MenuDefaults"))) {
                // using first menu for check, should be identical to next JMenu Bundle
                menuAbsent = false;
                break;
            }
        }
        if (menuAbsent) { // create it
            JMenu optionsMenu = new JMenu(Bundle.getMessage("MenuDefaults"));
            JMenuItem item = new JMenuItem(Bundle.getMessage("GlobalDebounce"));
            optionsMenu.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setDefaultDebounce(finalF);
                }
            });
            item = new JMenuItem(Bundle.getMessage("InitialSensorState"));
            optionsMenu.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setDefaultState(finalF);
                }
            });
            int pos = menuBar.getMenuCount() - 1; // count the number of menus to insert the TableMenus before 'Window' and 'Help'
            int offset = 1;
            log.debug("setMenuBar number of menu items = " + pos);
            for (int i = 0; i <= pos; i++) {
                if (menuBar.getComponent(i) instanceof JMenu) {
                    if (((JMenu) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
                        offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
                    }
                }
            }
            menuBar.add(optionsMenu, pos + offset);
        }
    }

    void showDebounceChanged() {
        jmri.jmrit.beantable.sensor.SensorTableDataModel a = (jmri.jmrit.beantable.sensor.SensorTableDataModel) m;
        a.showDebounce(showDebounceBox.isSelected());
    }

    void showPullUpChanged() {
        jmri.jmrit.beantable.sensor.SensorTableDataModel a = (jmri.jmrit.beantable.sensor.SensorTableDataModel) m;
        a.showPullUp(showPullUpBox.isSelected());
    }

    JCheckBox showDebounceBox = new JCheckBox(Bundle.getMessage("SensorDebounceCheckBox"));
    JCheckBox showPullUpBox = new JCheckBox(Bundle.getMessage("SensorPullUpCheckBox"));

    @Override
    public void addToFrame(BeanTableFrame f) {
        f.addToBottomBox(showDebounceBox, this.getClass().getName());
        showDebounceBox.setToolTipText(Bundle.getMessage("SensorDebounceToolTip"));
        showDebounceBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDebounceChanged();
            }
        });
        f.addToBottomBox(showPullUpBox, this.getClass().getName());
        showPullUpBox.setToolTipText(Bundle.getMessage("SensorPullUpToolTip"));
        showPullUpBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPullUpChanged();
            }
        });
        showPullUpBox.setVisible(true);
    }

    @Override
    public void addToPanel(AbstractTableTabAction f) {
        String systemPrefix = ConnectionNameFromSystemName.getConnectionName(senManager.getSystemPrefix());

        if (senManager.getClass().getName().contains("ProxySensorManager")) {
            systemPrefix = "All";
        }
        f.addToBottomBox(showDebounceBox, systemPrefix);
        showDebounceBox.setToolTipText(Bundle.getMessage("SensorDebounceToolTip"));
        showDebounceBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDebounceChanged();
            }
        });
        f.addToBottomBox(showPullUpBox, systemPrefix);
        showPullUpBox.setToolTipText(Bundle.getMessage("SensorPullUpToolTip"));
        showPullUpBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPullUpChanged();
            }
        });
    }

    @Override
    public void setMessagePreferencesDetails() {
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).preferenceItemDetails(getClassName(), "duplicateUserName", Bundle.getMessage("DuplicateUserNameWarn"));
        super.setMessagePreferencesDetails();
    }

    /**
     * Extends JTextField to provide a data validation function.
     *
     * @author Egbert Broerse 2017, based on
     * jmri.jmrit.util.swing.ValidatedTextField by B. Milhaupt
     */
    public class CheckedTextField extends JTextField {

        CheckedTextField fld;
        boolean allow0Length = false; // for Add new bean item, a value that is zero-length is considered invalid.
        private MyVerifier verifier; // internal mechanism used for verifying field data before focus is lost

        /**
         * Text entry field with an active key event checker.
         *
         * @param len field length
         */
        public CheckedTextField(int len) {
            super("", len);
            fld = this;

            // configure InputVerifier
            verifier = new MyVerifier();
            fld = this;
            fld.setInputVerifier(verifier);

            fld.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    setEditable(true);
                }

                @Override
                public void focusLost(FocusEvent e) {
                    setEditable(true);
                }
            });
        }

        /**
         * Validate the field information. Does not make any GUI changes.
         * <p>
         * During validation, logging is capped at the Error level to keep the Console clean from repeated validation.
         * This is reset to default level afterwards.
         *
         * @return 'true' if current field entry is valid according to the
         *         system manager; otherwise 'false'
         */
        @Override
        public boolean isValid() {
            String value;
            String prefix = ConnectionNameFromSystemName.getPrefixFromName(connectionChoice); // connectionChoice is set by canAddRange()

            if (fld == null) {
                return false;
            }
            value = getText().trim();
            if ((value.length() < 1) && (allow0Length == false)) {
                return false;
            } else if ((allow0Length == true) && (value.length() == 0)) {
                return true;
            } else {
                boolean validFormat = false;
                    // try {
                    validFormat = (InstanceManager.sensorManagerInstance().validSystemNameFormat(prefix + "S" + value) == Manager.NameValidity.VALID);
                    // } catch (jmri.JmriException e) {
                    // use it for the status bar?
                    // }
                if (validFormat) {
                    addButton.setEnabled(true); // directly update Create button
                    return true;
                } else {
                    addButton.setEnabled(false); // directly update Create button
                    return false;
                }
            }
        }

        /**
         * Private class used in conjunction with CheckedTextField to provide
         * the mechanisms required to validate the text field data upon loss of
         * focus, and colorize the text field in case of validation failure.
         */
        private class MyVerifier extends javax.swing.InputVerifier implements java.awt.event.ActionListener {

            // set default background color for invalid field data
            Color mark = Color.orange;

            @Override
            public boolean shouldYieldFocus(javax.swing.JComponent input) {
                if (input.getClass() == CheckedTextField.class) {

                    boolean inputOK = verify(input);
                    if (inputOK) {
                        input.setBackground(Color.white);
                        return true;
                    } else {
                        input.setBackground(mark);
                        ((javax.swing.text.JTextComponent) input).selectAll();
                        return false;
                    }
                } else {
                    return false;
                }
            }

            @Override
            public boolean verify(javax.swing.JComponent input) {
                if (input.getClass() == CheckedTextField.class) {
                    return ((CheckedTextField) input).isValid();
                } else {
                    return false;
                }
            }

            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                JTextField source = (JTextField) e.getSource();
                shouldYieldFocus(source); //ignore return value
                source.selectAll();
            }
        }
    }

    @Override
    protected String getClassName() {
        return SensorTableAction.class.getName();
    }

    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleSensorTable");
    }

    private final static Logger log = LoggerFactory.getLogger(SensorTableAction.class);

}
