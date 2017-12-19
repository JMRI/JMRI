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
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.UserPreferencesManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.managers.AbstractProxyManager;
import jmri.util.ConnectionNameFromSystemName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanSelectCreatePanel<E extends NamedBean> extends JPanel {

    Manager<E> _manager;
    NamedBean _defaultSelect;
    String _reference = null;
    JRadioButton existingItem = new JRadioButton();
    JRadioButton newItem;
    ButtonGroup selectcreate = new ButtonGroup();

    JmriBeanComboBox existingCombo;
    JTextField hardwareAddress = new JTextField(8);
    JComboBox<String> prefixBox = new JComboBox<>();
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";

    /**
     * Create a JPanel, that provides the option to the user to either select an
     * already created bean, or to create one on the fly. This only currently
     * works with Turnouts, Sensors, Memories and Blocks.
     *
     * @param manager       the bean manager
     * @param defaultSelect the bean that is selected by default
     */
    public BeanSelectCreatePanel(Manager<E> manager, NamedBean defaultSelect) {
        _manager = manager;
        _defaultSelect = defaultSelect;
        UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
        existingItem = new JRadioButton(Bundle.getMessage("UseExisting"), true);
        newItem = new JRadioButton(Bundle.getMessage("CreateNew"));
        existingItem.addActionListener((ActionEvent e) -> {
            update();
        });
        newItem.addActionListener((ActionEvent e) -> {
            update();
        });

        selectcreate.add(existingItem);
        selectcreate.add(newItem);
        existingCombo = new JmriBeanComboBox(_manager, defaultSelect, JmriBeanComboBox.DisplayOptions.USERNAMESYSTEMNAME);
        LayoutEditor.setupComboBox(existingCombo, false, true);
        // If the combo list is empty we go straight to creation.
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

        List<String> manuNameList = new ArrayList<>();

        // get list of system manufacturer names if there are multiple managers inside a proxy
        if (manager instanceof AbstractProxyManager) {
            List<Manager<E>> managerList = ((AbstractProxyManager<E>) _manager).getManagerList();
            managerList.forEach((mgr) -> {
                manuNameList.add(ConnectionNameFromSystemName.getConnectionName(mgr.getSystemPrefix()));
            });

            manuNameList.forEach((manuName) -> {
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
            });
            if (p.getComboBoxLastSelection(systemSelectionCombo) != null) {
                prefixBox.setSelectedItem(p.getComboBoxLastSelection(systemSelectionCombo));
            }
        } else { // not a proxy, just one, e.g. Block
            prefixBox.addItem(ConnectionNameFromSystemName.getConnectionName(_manager.getSystemPrefix()));
        }

        bean.add(existingCombo);
        bean.add(prefixBox);
        bean.add(hardwareAddress);
        hardwareAddress.setToolTipText(Bundle.getMessage("EnterHWaddressAsIntTooltip"));
        super.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        super.add(radio);
        super.add(bean);
        BeanSelectCreatePanel.this.update();
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

    @Override
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
     * Get the display name of the bean that has either been selected in the
     * drop down list or has been created.
     *
     * @return the name of the bean
     */
    public String getDisplayName() {
        if (existingItem.isSelected()) {
            return existingCombo.getSelectedDisplayName();
        } else {
            try {
                NamedBean nBean = createBean();
                return nBean.getDisplayName();
            } catch (JmriException e) {
                return "";
            }
        }
    }

    /**
     * Get the named bean that has either been selected in the drop down list or
     * has been created.
     *
     * @return the selected bean or a new bean
     * @throws JmriException if a bean needs to be created but can't be
     */
    public NamedBean getNamedBean() throws JmriException {
        if (existingItem.isSelected()) {
            return existingCombo.getSelectedBean();
        }
        try {
            return createBean();
        } catch (JmriException e) {
            throw e;
        }
    }

    private NamedBean createBean() throws JmriException {
        String prefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
        NamedBean nBean = null;
        if (prefix != null) {
            if (_manager instanceof TurnoutManager) {
                String sName = "";
                try {
                    sName = ((TurnoutManager) _manager).createSystemName(hardwareAddress.getText(), prefix);
                } catch (JmriException e) {
                    throw e;
                }
                try {
                    nBean = ((TurnoutManager) _manager).provideTurnout(sName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    throw new JmriException("ErrorTurnoutAddFailed");
                }
            } else if (_manager instanceof SensorManager) {
                String sName = "";
                try {
                    sName = ((SensorManager) _manager).createSystemName(hardwareAddress.getText(), prefix);
                } catch (JmriException e) {
                    throw e;
                }
                try {
                    nBean = ((SensorManager) _manager).provideSensor(sName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    throw new JmriException("ErrorSensorAddFailed");
                }
            } else {
                String sName = _manager.makeSystemName(hardwareAddress.getText());
                if (_manager instanceof MemoryManager) {
                    try {
                        nBean = ((MemoryManager) _manager).provideMemory(sName);
                    } catch (IllegalArgumentException ex) {
                        // user input no good
                        throw new JmriException("ErrorMemoryAddFailed");
                    }
                } else if (_manager instanceof BlockManager) {
                    try {
                        nBean = ((BlockManager) _manager).provideBlock(sName);
                    } catch (IllegalArgumentException ex) {
                        // user input no good
                        throw new JmriException("ErrorBlockAddFailed");
                    }
                }
            }
        }
        if (nBean == null) {
            throw new JmriException("Unable to create bean");
        }
        // Update comment if there's content, and there's not already a comment
        String comment = nBean.getComment();
        if ((_reference != null && !_reference.isEmpty()) && (comment == null || comment.isEmpty())) {
            nBean.setComment(_reference);
        }
        setDefaultNamedBean(nBean);
        return nBean;
    }

    /**
     * Set a reference that can be set against the comment for a bean, only if
     * the bean has no previous comment.
     *
     * @param ref the default comment for a bean without a comment
     */
    public void setReference(String ref) {
        _reference = ref;
    }

    /**
     * Sets the default selected item in the combo box, when this is set the
     * combo box becomes active and the add hardware box details are then
     * hidden.
     *
     * @param nBean the bean that is selected by default
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

    //initialize logging
    private final static Logger log = LoggerFactory.getLogger(BeanSelectCreatePanel.class.getName());
}
