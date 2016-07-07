package jmri.util.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import jmri.InstanceManager;
import jmri.Manager;
import jmri.NamedBean;
import jmri.util.ConnectionNameFromSystemName;

public class BeanSelectCreatePanel extends JPanel {

    Manager _manager;
    NamedBean _defaultSelect;
    String _reference = null;
    JRadioButton existingItem = new JRadioButton();
    JRadioButton newItem;
    ButtonGroup selectcreate = new ButtonGroup();

    JmriBeanComboBox existingCombo;
    JTextField hardwareAddress = new JTextField(8);
    JComboBox<String> prefixBox = new JComboBox<String>();
    jmri.UserPreferencesManager p;
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";

    /**
     * Create a JPanel, that provides the option to the user to either select an
     * already created bean, or to create one on the fly. This only currently
     * works with Turnouts, Sensors, Memories and Blocks.
     */
    public BeanSelectCreatePanel(Manager manager, NamedBean defaultSelect) {
        _manager = manager;
        _defaultSelect = defaultSelect;
        p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
        existingItem = new JRadioButton(Bundle.getMessage("UseExisting"), true);
        newItem = new JRadioButton(Bundle.getMessage("CreateNew"));
        existingItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });
        newItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                update();
            }
        });

        selectcreate.add(existingItem);
        selectcreate.add(newItem);
        existingCombo = new JmriBeanComboBox(_manager, defaultSelect, JmriBeanComboBox.USERNAMESYSTEMNAME);
        //If the combo list is empty we go straight to creation.
        if (existingCombo.getItemCount() == 0) {
            newItem.setSelected(true);
        }
        existingCombo.setFirstItemBlank(true);
        JPanel radio = new JPanel();
        radio.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        JPanel bean = new JPanel();
        bean.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
        radio.add(existingItem);
        radio.add(newItem);

        if (_manager instanceof jmri.managers.AbstractProxyManager) {
            List<Manager> managerList = new ArrayList<Manager>();
            if (_manager instanceof jmri.TurnoutManager) {
                jmri.managers.ProxyTurnoutManager proxy = (jmri.managers.ProxyTurnoutManager) InstanceManager.turnoutManagerInstance();
                managerList = proxy.getManagerList();
            } else if (_manager instanceof jmri.SensorManager) {
                jmri.managers.ProxySensorManager proxy = (jmri.managers.ProxySensorManager) InstanceManager.sensorManagerInstance();
                managerList = proxy.getManagerList();
            } else if (_manager instanceof jmri.LightManager) {
                jmri.managers.ProxyLightManager proxy = (jmri.managers.ProxyLightManager) InstanceManager.lightManagerInstance();
                managerList = proxy.getManagerList();
            } else if (_manager instanceof jmri.ReporterManager) {
                jmri.managers.ProxyReporterManager proxy = (jmri.managers.ProxyReporterManager) InstanceManager.getDefault(jmri.ReporterManager.class);
                managerList = proxy.getManagerList();
            }
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
            prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(_manager.getSystemPrefix()));
        }

        bean.add(existingCombo);
        bean.add(prefixBox);
        bean.add(hardwareAddress);
        hardwareAddress.setToolTipText("Enter in the Hardware address");
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(radio);
        add(bean);
        update();
    }

    void update() {
        boolean select = true;
        if (newItem.isSelected()) {
            select = false;
        }
        prefixBox.setVisible(false);
        hardwareAddress.setVisible(false);
        existingCombo.setVisible(false);
        if (select) {
            existingCombo.setVisible(true);
        } else {
            prefixBox.setVisible(true);
            hardwareAddress.setVisible(true);
        }
    }

    public void setEnabled(boolean enabled) {
        existingItem.setEnabled(enabled);
        hardwareAddress.setEnabled(enabled);
        prefixBox.setEnabled(enabled);
        newItem.setEnabled(enabled);
        existingCombo.setEnabled(enabled);
        super.setEnabled(enabled);
    }

    public void refresh() {
        existingCombo.refreshCombo();
    }

    /**
     * get the display name of the bean that has either been selected in the
     * drop down list or has been created
     */
    public String getDisplayName() {
        if (existingItem.isSelected()) {
            return existingCombo.getSelectedDisplayName();
        } else {
            try {
                NamedBean nBean = createBean();
                return nBean.getDisplayName();
            } catch (jmri.JmriException e) {
                return "";
            }
        }
    }

    /**
     * get the named bean that has either been selected in the drop down list or
     * has been created
     */
    public NamedBean getNamedBean() throws jmri.JmriException {
        if (existingItem.isSelected()) {
            return existingCombo.getSelectedBean();
        }
        try {
            return createBean();
        } catch (jmri.JmriException e) {
            throw e;
        }
    }

    private NamedBean createBean() throws jmri.JmriException {
        String prefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
        NamedBean nBean = null;
        if (_manager instanceof jmri.TurnoutManager) {
            String sName = "";
            try {
                sName = InstanceManager.turnoutManagerInstance().createSystemName(hardwareAddress.getText(), prefix);
            } catch (jmri.JmriException e) {
                throw e;
            }
            try {
                nBean = InstanceManager.turnoutManagerInstance().provideTurnout(sName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                throw new jmri.JmriException("ErrorTurnoutAddFailed");
            }
        } else if (_manager instanceof jmri.SensorManager) {
            String sName = "";
            try {
                sName = InstanceManager.sensorManagerInstance().createSystemName(hardwareAddress.getText(), prefix);
            } catch (jmri.JmriException e) {
                throw e;
            }
            try {
                nBean = InstanceManager.sensorManagerInstance().provideSensor(sName);
            } catch (IllegalArgumentException ex) {
                // user input no good
                throw new jmri.JmriException("ErrorSensorAddFailed");
            }
        } else {
            String sName = _manager.makeSystemName(hardwareAddress.getText());
            if (_manager instanceof jmri.MemoryManager) {
                try {
                    nBean = InstanceManager.memoryManagerInstance().provideMemory(sName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    throw new jmri.JmriException("ErrorMemoryAddFailed");
                }
            } else if (_manager instanceof jmri.Block) {
                try {
                    nBean = InstanceManager.blockManagerInstance().provideBlock(sName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    throw new jmri.JmriException("ErrorBlockAddFailed");
                }
            }
        }
        if (nBean == null) {
            throw new jmri.JmriException("Unable to create bean");
        }
        if ((_reference != null && !_reference.equals("")) && (nBean.getComment() == null || nBean.getComment().equals(""))) {
            nBean.setComment(_reference);
        }
        setDefaultNamedBean(nBean);
        return nBean;
    }

    /**
     * Set a reference that can be set against the comment for a bean, only if
     * the bean has no previous comment.
     */
    public void setReference(String ref) {
        _reference = ref;
    }

    /**
     * Sets the default selected item in the combo box, when this is set the
     * combo box becomes active and the add hardware box details are then hidden
     */
    public void setDefaultNamedBean(NamedBean nBean) {
        _defaultSelect = nBean;
        existingCombo.setSelectedBean(_defaultSelect);
        existingItem.setSelected(true);
        update();
    }

    public void dispose() {
        existingCombo.dispose();
    }

}
