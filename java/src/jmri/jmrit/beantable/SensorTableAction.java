package jmri.jmrit.beantable;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.swing.ManagerComboBox;
import jmri.swing.SystemNameValidator;
import jmri.jmrit.beantable.sensor.SensorTableDataModel;
import jmri.util.JmriJFrame;
import jmri.util.swing.TriStateJCheckBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SensorTable GUI.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2009
 */
public class SensorTableAction extends AbstractTableAction<Sensor> {

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
        if (sensorManager == null) {
            super.setEnabled(false);
        }
    }

    public SensorTableAction() {
        this(Bundle.getMessage("TitleSensorTable"));
    }

    protected SensorManager sensorManager = InstanceManager.getDefault(SensorManager.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void setManager(@Nonnull Manager<Sensor> s) {
        if (s instanceof SensorManager) {
            log.debug("setting manager of ST Action{} to {}",this,s.getClass());
            sensorManager = (SensorManager) s;
            if (m != null) {
                m.setManager(sensorManager);
            }
        }
    }

    /**
     * Create the JTable DataModel, along with the changes for the specific case
     * of Sensors.
     */
    @Override
    protected void createModel() {
        m = new jmri.jmrit.beantable.sensor.SensorTableDataModel(sensorManager);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleSensorTable"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }

    JmriJFrame addFrame = null;

    JTextField hardwareAddressTextField = new JTextField(20);
    // initially allow any 20 char string, updated by prefixBox selection
    JTextField userNameField = new JTextField(40);
    ManagerComboBox<Sensor> prefixBox = new ManagerComboBox<>();
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAddSpinner = new JSpinner(rangeSpinner);
    JCheckBox rangeBox = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JLabel hwAddressLabel = new JLabel(Bundle.getMessage("LabelHardwareAddress"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    JButton addButton;
    JLabel statusBarLabel = new JLabel(Bundle.getMessage("HardwareAddStatusEnter"), JLabel.LEADING);
    jmri.UserPreferencesManager p;
    Manager<Sensor> connectionChoice = null;
    SystemNameValidator hardwareAddressValidator;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPressed(ActionEvent e) {
        p = InstanceManager.getDefault(jmri.UserPreferencesManager.class);

        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddSensor"));
            addFrame.addHelpMenu("package.jmri.jmrit.beantable.SensorAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener createListener = this::createPressed;
            ActionListener cancelListener = this::cancelPressed;
            ActionListener rangeListener = this::canAddRange;
            configureManagerComboBox(prefixBox, sensorManager, SensorManager.class);
            userNameField.setName("userName"); // NOI18N
            prefixBox.setName("prefixBox"); // NOI18N
            addButton = new JButton(Bundle.getMessage("ButtonCreate"));
            addButton.addActionListener(createListener);
            
            log.debug("add frame hwAddValidator is {} prefix box is {}",hardwareAddressValidator, prefixBox.getSelectedItem());
            if (hardwareAddressValidator==null){
                hardwareAddressValidator = new SystemNameValidator(hardwareAddressTextField, prefixBox.getSelectedItem(), true);
            } else {
                hardwareAddressValidator.setManager(prefixBox.getSelectedItem());
            }

            // create panel
            addFrame.add(new AddNewHardwareDevicePanel(hardwareAddressTextField, hardwareAddressValidator, userNameField, prefixBox,
                    numberToAddSpinner, rangeBox, addButton, cancelListener, rangeListener, statusBarLabel));
            // tooltip for hwAddressTextField will be assigned later by canAddRange()
            canAddRange(null);
            
            addFrame.setEscapeKeyClosesWindow(true);
            addFrame.getRootPane().setDefaultButton(addButton);
            
        }
        hardwareAddressTextField.setName("hwAddressTextField"); // for GUI test NOI18N
        addButton.setName("createButton"); // for GUI test NOI18N
        // reset statusBarLabel text
        statusBarLabel.setText(Bundle.getMessage("HardwareAddStatusEnter"));
        statusBarLabel.setForeground(Color.gray);

        addFrame.pack();
        addFrame.setVisible(true);
    }

    void cancelPressed(ActionEvent e) {
        removePrefixBoxListener(prefixBox);
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    /**
     * Respond to Create new item button pressed on Add Sensor pane.
     *
     * @param e the click event
     */
    void createPressed(ActionEvent e) {

        int numberOfSensors = 1;

        if (rangeBox.isSelected()) {
            numberOfSensors = (Integer) numberToAddSpinner.getValue();
        }
        if (numberOfSensors >= 65) { // limited by JSpinnerModel to 100
            if (JOptionPane.showConfirmDialog(addFrame,
                    Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("Sensors"), numberOfSensors),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }
        String sensorPrefix = prefixBox.getSelectedItem().getSystemPrefix();
        String sName;
        String uName = userNameField.getText();
        String curAddress = hardwareAddressTextField.getText();
        // initial check for empty entry
        if (curAddress.length() < 1) {
            statusBarLabel.setText(Bundle.getMessage("WarningEmptyHardwareAddress"));
            statusBarLabel.setForeground(Color.red);
            hardwareAddressTextField.setBackground(Color.red);
            return;
        } else {
            hardwareAddressTextField.setBackground(Color.white);
        }

        // Add some entry pattern checking, before assembling sName and handing it to the SensorManager
        String statusMessage = Bundle.getMessage("ItemCreateFeedback", Bundle.getMessage("BeanNameSensor"));
        for (int x = 0; x < numberOfSensors; x++) {
            log.debug("b4 next valid addr for prefix {} conn choice mgr {}",sensorPrefix,connectionChoice);
            try {
                curAddress = InstanceManager.getDefault(SensorManager.class).getNextValidAddress(curAddress, sensorPrefix, false);
            } catch (jmri.JmriException ex) {
                displayHwError(curAddress, ex);
                // directly add to statusBarLabel (but never called?)
                statusBarLabel.setText(Bundle.getMessage("ErrorConvertHW", curAddress));
                statusBarLabel.setForeground(Color.red);
                return;
            }

            // Compose the proposed system name from parts:
            sName = sensorPrefix + InstanceManager.getDefault(SensorManager.class).typeLetter() + curAddress;
            Sensor s;
            try {
                s = InstanceManager.getDefault(SensorManager.class).provideSensor(sName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(ex, sName);
                return;   // return without creating
            }

            if (!uName.isEmpty()) {
                if (InstanceManager.getDefault(SensorManager.class).getByUserName(uName) == null) {
                    s.setUserName(uName);
                } else {
                    InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                            showErrorMessage(Bundle.getMessage("ErrorTitle"),
                                    Bundle.getMessage("ErrorDuplicateUserName", uName),
                                    getClassName(), "duplicateUserName", false, true);
                }
            }

            // add first and last names to statusMessage uName feedback string
            // only mention first and last of rangeBox added
            if (x == 0 || x == numberOfSensors - 1) {
                statusMessage = statusMessage + " " + sName + " (" + uName + ")";
            }
            if (x == numberOfSensors - 2) {
                statusMessage = statusMessage + " " + Bundle.getMessage("ItemCreateUpTo") + " ";
            }

            // bump user name
            if (!uName.isEmpty()) {
                uName = nextName(uName);
            }
            // end of for loop creating rangeBox of Sensors
        }

        // provide success feedback to user
        statusBarLabel.setText(statusMessage);
        statusBarLabel.setForeground(Color.gray);

        p.setComboBoxLastSelection(systemSelectionCombo, prefixBox.getSelectedItem().getMemo().getUserName());
        removePrefixBoxListener(prefixBox);
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    private String addEntryToolTip;

    /**
     * Activate Add a rangeBox option if manager accepts adding more than 1
     * Sensor and set a manager specific tooltip on the AddNewHardwareDevice
     * pane.
     */
    private void canAddRange(ActionEvent e) {
        rangeBox.setEnabled(false);
        rangeBox.setSelected(false);
        if (prefixBox.getSelectedIndex() == -1) {
            prefixBox.setSelectedIndex(0);
        }
        connectionChoice = prefixBox.getSelectedItem(); // store in Field for CheckedTextField
        String systemPrefix = connectionChoice.getSystemPrefix();
        rangeBox.setEnabled(((SensorManager) connectionChoice).allowMultipleAdditions(systemPrefix));
        addEntryToolTip = connectionChoice.getEntryToolTip();
        // show hwAddressTextField field tooltip in the Add Sensor pane that matches system connection selected from combobox
        hardwareAddressTextField.setToolTipText(
                Bundle.getMessage("AddEntryToolTipLine1",
                        connectionChoice.getMemo().getUserName(),
                        Bundle.getMessage("Sensors"),
                        addEntryToolTip));
        hardwareAddressValidator.setToolTipText(hardwareAddressTextField.getToolTipText());
        hardwareAddressValidator.verify(hardwareAddressTextField);
    }

    void handleCreateException(Exception ex, String hwAddress) {
        statusBarLabel.setText(ex.getLocalizedMessage());
        String err = Bundle.getMessage("ErrorBeanCreateFailed",
            InstanceManager.getDefault(SensorManager.class).getBeanTypeHandled(),hwAddress);
        JOptionPane.showMessageDialog(addFrame, err + "\n" + ex.getLocalizedMessage(),
                err, JOptionPane.ERROR_MESSAGE);
    }

    protected void setDefaultDebounce(JFrame _who) {
        SpinnerNumberModel activeSpinnerModel = new SpinnerNumberModel((Long)sensorManager.getDefaultSensorDebounceGoingActive(), (Long)0L, Sensor.MAX_DEBOUNCE, (Long)1L); // MAX_DEBOUNCE is a Long; casts are to force needed signature
        JSpinner activeSpinner = new JSpinner(activeSpinnerModel);
        activeSpinner.setPreferredSize(new JTextField(Long.toString(Sensor.MAX_DEBOUNCE).length()+1).getPreferredSize());
        SpinnerNumberModel inActiveSpinnerModel = new SpinnerNumberModel((Long)sensorManager.getDefaultSensorDebounceGoingInActive(), (Long)0L, Sensor.MAX_DEBOUNCE, (Long)1L); // MAX_DEBOUNCE is a Long; casts are to force needed signature
        JSpinner inActiveSpinner = new JSpinner(inActiveSpinnerModel);
        inActiveSpinner.setPreferredSize(new JTextField(Long.toString(Sensor.MAX_DEBOUNCE).length()+1).getPreferredSize());

        JPanel input = new JPanel(); // panel to hold formatted input for dialog
        input.setLayout(new BoxLayout(input, BoxLayout.Y_AXIS));

        JTextArea message = new JTextArea(Bundle.getMessage("SensorGlobalDebounceMessageBox")); // multi line
        message.setEditable(false);
        message.setOpaque(false);
        input.add(message);

        JPanel active = new JPanel();
        active.add(new JLabel(Bundle.getMessage("SensorActiveTimer")));
        active.add(activeSpinner);
        input.add(active);

        JPanel inActive = new JPanel();
        inActive.add(new JLabel(Bundle.getMessage("SensorInactiveTimer")));
        inActive.add(inActiveSpinner);
        input.add(inActive);

        int retval = JOptionPane.showOptionDialog(_who,
                input, Bundle.getMessage("SensorGlobalDebounceMessageTitle"),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonCancel")}, null);
        log.debug("dialog retval={}", retval);
        if (retval != 0) {
            return;
        }

        // Allow the sensor manager to handle checking if the values have changed
        sensorManager.setDefaultSensorDebounceGoingActive((Long) activeSpinner.getValue());
        sensorManager.setDefaultSensorDebounceGoingInActive((Long) inActiveSpinner.getValue());
        m.fireTableDataChanged();
    }

    protected void setDefaultState(JFrame _who) {
        String[] sensorStates = new String[]{Bundle.getMessage("BeanStateUnknown"), Bundle.getMessage("SensorStateInactive"), Bundle.getMessage("SensorStateActive"), Bundle.getMessage("BeanStateInconsistent")};
        JComboBox<String> stateCombo = new JComboBox<>(sensorStates);
        switch (jmri.jmrix.internal.InternalSensorManager.getDefaultStateForNewSensors()) {
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

        JPanel input = new JPanel(); // panel to hold formatted input for dialog
        input.add(new JLabel(Bundle.getMessage("SensorInitialStateMessageBox")));
        JPanel stateBoxPane = new JPanel();
        stateBoxPane.add(stateCombo);
        input.add(stateBoxPane);

        int retval = JOptionPane.showOptionDialog(_who,
                input, Bundle.getMessage("InitialSensorState"),
                0, JOptionPane.INFORMATION_MESSAGE, null,
                new Object[]{Bundle.getMessage("ButtonOK"), Bundle.getMessage("ButtonCancel")}, null);
        if (retval != 0) {
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

        jmri.jmrix.internal.InternalSensorManager.setDefaultStateForNewSensors(defaultState);
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
    public void setMenuBar(BeanTableFrame<Sensor> f) {
        final jmri.util.JmriJFrame finalF = f; // needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        // check for menu
        boolean menuAbsent = true;
        for (int i = 0; i < menuBar.getMenuCount(); ++i) {
            String name = menuBar.getMenu(i).getAccessibleContext().getAccessibleName();
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
            item.addActionListener((ActionEvent e) -> {
                setDefaultDebounce(finalF);
            });
            item = new JMenuItem(Bundle.getMessage("InitialSensorState"));
            optionsMenu.add(item);
            item.addActionListener((ActionEvent e) -> {
                setDefaultState(finalF);
            });
            int pos = menuBar.getMenuCount() - 1; // count the number of menus to insert the TableMenus before 'Window' and 'Help'
            int offset = 1;
            log.debug("setMenuBar number of menu items = {}", pos);
            for (int i = 0; i <= pos; i++) {
                if (menuBar.getComponent(i) instanceof JMenu) {
                    if (((AbstractButton) menuBar.getComponent(i)).getText().equals(Bundle.getMessage("MenuHelp"))) {
                        offset = -1; // correct for use as part of ListedTableAction where the Help Menu is not yet present
                    }
                }
            }
            menuBar.add(optionsMenu, pos + offset);
        }
    }
    
    @Override
    protected void configureTable(JTable table){
        super.configureTable(table);
        showDebounceBox.addActionListener((ActionEvent e) -> { ((SensorTableDataModel)m).showDebounce(showDebounceBox.isSelected(), table); });
        showPullUpBox.addActionListener((ActionEvent e) -> { ((SensorTableDataModel)m).showPullUp(showPullUpBox.isSelected(), table); });
        showStateForgetAndQueryBox.addActionListener((ActionEvent e) -> { ((SensorTableDataModel)m).showStateForgetAndQuery(showStateForgetAndQueryBox.isSelected(), table); });
    }

    private final TriStateJCheckBox showDebounceBox = new TriStateJCheckBox(Bundle.getMessage("SensorDebounceCheckBox"));
    private final TriStateJCheckBox showPullUpBox = new TriStateJCheckBox(Bundle.getMessage("SensorPullUpCheckBox"));
    private final TriStateJCheckBox showStateForgetAndQueryBox = new TriStateJCheckBox(Bundle.getMessage("ShowStateForgetAndQuery"));

    /**
     * {@inheritDoc}
     */
    @Override
    public void addToFrame(BeanTableFrame<Sensor> f) {
        f.addToBottomBox(showDebounceBox, this.getClass().getName());
        showDebounceBox.setToolTipText(Bundle.getMessage("SensorDebounceToolTip"));
        f.addToBottomBox(showPullUpBox, this.getClass().getName());
        showPullUpBox.setToolTipText(Bundle.getMessage("SensorPullUpToolTip"));
        f.addToBottomBox(showStateForgetAndQueryBox, this.getClass().getName());
        showStateForgetAndQueryBox.setToolTipText(Bundle.getMessage("StateForgetAndQueryBoxToolTip"));
    }
    
    /**
     * Override to update showDebounceBox, showPullUpBox, showStateForgetAndQueryBox.
     * {@inheritDoc}
     */
    @Override
    protected void columnsVisibleUpdated(boolean[] colsVisible){
        log.debug("columns updated {}",colsVisible);
        showDebounceBox.setState(new boolean[]{
            colsVisible[SensorTableDataModel.ACTIVEDELAY],
            colsVisible[SensorTableDataModel.INACTIVEDELAY],
            colsVisible[SensorTableDataModel.USEGLOBALDELAY] });
        showPullUpBox.setState(new boolean[]{
            colsVisible[SensorTableDataModel.PULLUPCOL]});
        showStateForgetAndQueryBox.setState(new boolean[]{
            colsVisible[SensorTableDataModel.FORGETCOL],
            colsVisible[SensorTableDataModel.QUERYCOL] });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addToPanel(AbstractTableTabAction<Sensor> f) {
        String connectionName = sensorManager.getMemo().getUserName();

        if (sensorManager.getClass().getName().contains("ProxySensorManager")) {
            connectionName = "All";
        }
        f.addToBottomBox(showDebounceBox, connectionName);
        showDebounceBox.setToolTipText(Bundle.getMessage("SensorDebounceToolTip"));
        f.addToBottomBox(showPullUpBox, connectionName);
        showPullUpBox.setToolTipText(Bundle.getMessage("SensorPullUpToolTip"));
        f.addToBottomBox(showStateForgetAndQueryBox, connectionName);
        showStateForgetAndQueryBox.setToolTipText(Bundle.getMessage("StateForgetAndQueryBoxToolTip"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMessagePreferencesDetails() {
        InstanceManager.getDefault(jmri.UserPreferencesManager.class).setPreferenceItemDetails(getClassName(), "duplicateUserName", Bundle.getMessage("DuplicateUserNameWarn"));
        super.setMessagePreferencesDetails();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getClassName() {
        return SensorTableAction.class.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClassDescription() {
        return Bundle.getMessage("TitleSensorTable");
    }

    private final static Logger log = LoggerFactory.getLogger(SensorTableAction.class);

}
