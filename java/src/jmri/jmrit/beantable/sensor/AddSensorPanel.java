package jmri.jmrit.beantable.sensor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.Manager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.beantable.AddNewHardwareDevicePanel;
import jmri.util.ConnectionNameFromSystemName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPanel to create a new Sensor
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @deprecated Replaced by {@link jmri.jmrit.beantable.AddNewHardwareDevicePanel}
 */
@Deprecated
public class AddSensorPanel extends jmri.util.swing.JmriPanel {

    public AddSensorPanel() {
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);

        ActionListener okListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                okPressed(e);
            }
        };
        ActionListener cancelListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { cancelPressed(e); }
        };
        ActionListener rangeListener = new ActionListener() {
            @Override
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
        add(new AddNewHardwareDevicePanel(sysName, userName, prefixBox, numberToAdd, range, "ButtonAddSensor", okListener, cancelListener, rangeListener));
        canAddRange(null);
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

    /*public AddSensorPanel() {
     // to make location for accessibility & testing easier
     sysName.setName("sysName");
     userName.setName("userName");

     setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
     JPanel p;
     p = new JPanel();
     p.setLayout(new FlowLayout());
     p.setLayout(new java.awt.GridBagLayout());
     java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
     c.gridwidth  = 1;
     c.gridheight = 1;
     c.gridx = 0;
     c.gridy = 0;
     c.anchor = java.awt.GridBagConstraints.EAST;
     p.add(sysNameLabel,c);
     c.gridy = 1;
     p.add(userNameLabel,c);
     c.gridx = 1;
     c.gridy = 0;
     c.anchor = java.awt.GridBagConstraints.WEST;
     c.weightx = 1.0;
     c.fill = java.awt.GridBagConstraints.HORIZONTAL;  // text field will expand
     p.add(sysName,c);
     c.gridy = 1;
     p.add(userName,c);
     add(p);

     JButton ok;
     add(ok = new JButton(Bundle.getMessage("ButtonAddSensor")));
     ok.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
     okPressed(e);
     }
     });
     }
     */
    JTextField sysName = new JTextField(5);
    JTextField userName = new JTextField(5);
    JLabel sysNameLabel = new JLabel(Bundle.getMessage("LabelSystemName"));
    JLabel userNameLabel = new JLabel(Bundle.getMessage("LabelUserName"));

    protected SensorManager senManager = jmri.InstanceManager.sensorManagerInstance();

    JComboBox<String> prefixBox = new JComboBox<String>();
    SpinnerNumberModel rangeSpinner = new SpinnerNumberModel(1, 1, 100, 1); // maximum 100 items
    JSpinner numberToAdd = new JSpinner(rangeSpinner);
    JCheckBox range = new JCheckBox(Bundle.getMessage("AddRangeBox"));
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";
    String userNameError = this.getClass().getName() + ".DuplicateUserName";
    jmri.UserPreferencesManager p;

    void cancelPressed(ActionEvent e) {
    }

    void okPressed(ActionEvent e) {

        int numberOfSensors = 1;

        if (range.isSelected()) {
            numberOfSensors = (Integer) numberToAdd.getValue();
        }
        if (numberOfSensors >= 65) { // limited by JSpinnerModel to 100
            if (JOptionPane.showConfirmDialog(null,
                    Bundle.getMessage("WarnExcessBeans", Bundle.getMessage("Sensors"), numberOfSensors),
                    Bundle.getMessage("WarningTitle"),
                    JOptionPane.YES_NO_OPTION) == 1) {
                return;
            }
        }
        String sensorPrefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());

        String sName = null;
        String curAddress = senManager.normalizeSystemName(sysName.getText());

        for (int x = 0; x < numberOfSensors; x++) {
            try {
                curAddress = senManager.getNextValidAddress(curAddress, sensorPrefix);
            } catch (jmri.JmriException ex) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorConvertHW", curAddress), "" + ex, "", true, false);
                return;
            }
            if (curAddress == null) {
                //The next address is already in use, therefore we stop.
                break;
            }
            //We have found another turnout with the same address, therefore we need to go onto the next address.
            sName = sensorPrefix + senManager.typeLetter() + curAddress;
            Sensor s = null;
            try {
                s = senManager.provideSensor(sName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                handleCreateException(sName);
                return; // without creating
            }

            String user = NamedBean.normalizeUserName(userName.getText());
            if ((x != 0) && user != null && !user.equals("")) {
                user += ":" + x;
            }
            if (user != null && !user.equals("") && (senManager.getByUserName(user) == null)) {
                s.setUserName(user);
            } else if (user != null && !user.equals("") && senManager.getByUserName(user) != null && !p.getPreferenceState(AddSensorPanel.class.getName(), "duplicateUserName")) {
                jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class).
                        showErrorMessage(Bundle.getMessage("ErrorTitle"), Bundle.getMessage("ErrorDuplicateUserName", user), AddSensorPanel.class.getName(), "duplicateUserName", false, true);
            }

        }
        p.addComboBoxLastSelection(systemSelectionCombo, (String) prefixBox.getSelectedItem());
    }

    void handleCreateException(String sysName) {
        javax.swing.JOptionPane.showMessageDialog(AddSensorPanel.this,
                java.text.MessageFormat.format(
                        Bundle.getMessage("ErrorSensorAddFailed"),
                        new Object[]{sysName}),
                Bundle.getMessage("ErrorTitle"),
                javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    // private final static Logger log = LoggerFactory.getLogger(AddSensorPanel.class.getName());
}
