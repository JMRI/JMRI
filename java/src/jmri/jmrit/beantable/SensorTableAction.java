package jmri.jmrit.beantable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.annotation.Nonnull;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
 * @author	Bob Jacobsen Copyright (C) 2003, 2009
 */
public class SensorTableAction extends AbstractTableAction {

    /**
     * Create an action with a specific title.
     * <P>
     * Note that the argument is the Action title, not the title of the
     * resulting frame. Perhaps this should be changed?
     *
     * @param actionName
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

    protected SensorManager senManager = jmri.InstanceManager.sensorManagerInstance();

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
     * of Sensors
     */
    protected void createModel() {
        m = new jmri.jmrit.beantable.sensor.SensorTableDataModel(senManager);
    }

    protected void setTitle() {
        f.setTitle(Bundle.getMessage("TitleSensorTable"));
    }

    protected String helpTarget() {
        return "package.jmri.jmrit.beantable.SensorTable";
    }

    JmriJFrame addFrame = null;

    JTextField sysName = new JTextField(40);
    JTextField userName = new JTextField(40);
    JComboBox<String> prefixBox = new JComboBox<String>();
    JTextField numberToAdd = new JTextField(5);
    JCheckBox range = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelHardwareAddress"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    String userNameError = this.getClass().getName() + ".DuplicateUserName";
    jmri.UserPreferencesManager p;

    protected void addPressed(ActionEvent e) {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);

        if (addFrame == null) {
            addFrame = new JmriJFrame(Bundle.getMessage("TitleAddSensor"));
            //addFrame.addHelpMenu("package.jmri.jmrit.beantable.SensorAddEdit", true);
            addFrame.getContentPane().setLayout(new BoxLayout(addFrame.getContentPane(), BoxLayout.Y_AXIS));

            ActionListener okListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    okPressed(e);
                }
            };
            ActionListener cancelListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) { cancelPressed(e); }
            };
            ActionListener rangeListener = new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    canAddRange(e);
                }
            };
            if (jmri.InstanceManager.sensorManagerInstance().getClass().getName().contains("ProxySensorManager")) {
                jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) jmri.InstanceManager.sensorManagerInstance();
                List<Manager> managerList = proxy.getManagerList();
                for (int x = 0; x < managerList.size(); x++) {
                    String manuName = ConnectionNameFromSystemName.getConnectionName(managerList.get(x).getSystemPrefix());
                    Boolean addToPrefix = true;
                    //Simple test not to add a system with a duplicate System prefix
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
                prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(jmri.InstanceManager.sensorManagerInstance().getSystemPrefix()));
            }
            sysName.setName("sysName");
            userName.setName("userName");
            prefixBox.setName("prefixBox");
            addFrame.add(new AddNewHardwareDevicePanel(sysName, userName, prefixBox, numberToAdd, range, "ButtonOK", okListener, cancelListener, rangeListener));
            canAddRange(null);
        }
        addFrame.pack();
        addFrame.setVisible(true);
    }

    void cancelPressed(ActionEvent e) {
        addFrame.setVisible(false);
        addFrame.dispose();
        addFrame = null;
    }

    void okPressed(ActionEvent e) {
        /*String user = userName.getText();
         if (user.equals("")) user=null;*/

        int numberOfSensors = 1;

        if (range.isSelected()) {
            try {
                numberOfSensors = Integer.parseInt(numberToAdd.getText());
            } catch (NumberFormatException ex) {
                log.error("Unable to convert " + numberToAdd.getText() + " to a number");
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage("Error", "Number to Sensors to Add must be a number!", "" + ex, "", true, false);
                return;
            }
        }
        if (numberOfSensors >= 65) {
            if (JOptionPane.showConfirmDialog(addFrame,
                    "You are about to add " + numberOfSensors + " Sensors into the configuration\nAre you sure?", "Warning",
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }
        String sensorPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());

        String sName = null;
        String curAddress = sysName.getText();

        for (int x = 0; x < numberOfSensors; x++) {
            try {
                curAddress = jmri.InstanceManager.sensorManagerInstance().getNextValidAddress(curAddress, sensorPrefix);
            } catch (jmri.JmriException ex) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage("Error", "Unable to convert '" + curAddress + "' to a valid Hardware Address", "" + ex, "", true, false);
                return;
            }
            if (curAddress == null) {
                //The next address is already in use, therefore we stop.
                break;
            }
            //We have found another turnout with the same address, therefore we need to go onto the next address.
            sName = sensorPrefix + jmri.InstanceManager.sensorManagerInstance().typeLetter() + curAddress;
            Sensor s = null;
            try {
                s = jmri.InstanceManager.sensorManagerInstance().provideSensor(sName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(sName);
                return; // without creating       
            }
            if (s != null) {
                String user = userName.getText();
                if ((x != 0) && user != null && !user.equals("")) {
                    user = userName.getText() + ":" + x;
                }
                if (user != null && !user.equals("") && (jmri.InstanceManager.sensorManagerInstance().getByUserName(user) == null)) {
                    s.setUserName(user);
                } else if (jmri.InstanceManager.sensorManagerInstance().getByUserName(user) != null && !p.getPreferenceState(getClassName(), "duplicateUserName")) {
                    // I18N TODO
                    jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                            showErrorMessage("Duplicate UserName", "The username " + user + " specified is already in use and therefore will not be set", getClassName(), "duplicateUserName", false, true);
                }
            }
        }
        p.addComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
    }

    private void canAddRange(ActionEvent e) {
        range.setEnabled(false);
        range.setSelected(false);
        if (senManager.getClass().getName().contains("ProxySensorManager")) {
            jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) senManager;
            List<Manager> managerList = proxy.getManagerList();
            String systemPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
            for (int x = 0; x < managerList.size(); x++) {
                jmri.SensorManager mgr = (jmri.SensorManager) managerList.get(x);
                if (mgr.getSystemPrefix().equals(systemPrefix) && mgr.allowMultipleAdditions(systemPrefix)) {
                    range.setEnabled(true);
                    return;
                }
            }
        } else if (senManager.allowMultipleAdditions(ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem()))) {
            range.setEnabled(true);
        }
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(addFrame,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorSensorAddFailed"),
                        new Object[]{sysName}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
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

        //We will allow the turnout manager to handle checking if the values have changed
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

    public void setMenuBar(BeanTableFrame f) {
        final jmri.util.JmriJFrame finalF = f;			// needed for anonymous ActionListener class
        JMenuBar menuBar = f.getJMenuBar();
        // check for menu
        boolean menuAbsent = true;
        for(int m = 0; m < menuBar.getMenuCount(); ++m) {
            String name = menuBar.getMenu(m).getAccessibleContext().getAccessibleName();
            if(name.equals(Bundle.getMessage("MenuDefaults"))) {
                // using first menu for check, should be identical to next JMenu Bundle
                menuAbsent = false;
                break;
            }
        }
        if(menuAbsent) { // create it
            JMenu optionsMenu = new JMenu(Bundle.getMessage("MenuDefaults"));
            JMenuItem item = new JMenuItem(Bundle.getMessage("GlobalDebounce"));
            optionsMenu.add(item);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setDefaultDebounce(finalF);
                }
            });
            item = new JMenuItem(Bundle.getMessage("InitialSensorState"));
            optionsMenu.add(item);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setDefaultState(finalF);
                }
            });
            menuBar.add(optionsMenu);
        }
    }

    void showDebounceChanged() {
        jmri.jmrit.beantable.sensor.SensorTableDataModel a = (jmri.jmrit.beantable.sensor.SensorTableDataModel) m;
        a.showDebounce(showDebounceBox.isSelected());
    }

    JCheckBox showDebounceBox = new JCheckBox(Bundle.getMessage("SensorDebounceCheckBox"));

    public void addToFrame(BeanTableFrame f) {
        f.addToBottomBox(showDebounceBox, this.getClass().getName());
        showDebounceBox.setToolTipText(Bundle.getMessage("SensorDebounceToolTip"));
        showDebounceBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDebounceChanged();
            }
        });
    }

    public void addToPanel(AbstractTableTabAction f) {
        String systemPrefix = ConnectionNameFromSystemName.getConnectionName(senManager.getSystemPrefix());

        if (senManager.getClass().getName().contains("ProxySensorManager")) {
            systemPrefix = "All";
        }
        f.addToBottomBox(showDebounceBox, systemPrefix);
        showDebounceBox.setToolTipText(Bundle.getMessage("SensorDebounceToolTip"));
        showDebounceBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showDebounceChanged();
            }
        });
    }

    public void setMessagePreferencesDetails() {
        jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).preferenceItemDetails(getClassName(), "duplicateUserName", Bundle.getMessage("DuplicateUserNameWarn"));
        super.setMessagePreferencesDetails();
    }

    protected String getClassName() {
        return SensorTableAction.class.getName();
    }

    public String getClassDescription() {
        return Bundle.getMessage("TitleSensorTable");
    }

    private final static Logger log = LoggerFactory.getLogger(SensorTableAction.class.getName());
}
