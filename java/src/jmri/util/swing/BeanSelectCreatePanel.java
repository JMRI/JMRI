package jmri.util.swing;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
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
import jmri.ProvidingManager;
// import jmri.NamedBean;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.UserPreferencesManager;
import jmri.NamedBean.DisplayOptions;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.managers.AbstractProxyManager;
import jmri.swing.NamedBeanComboBox;
import jmri.util.ConnectionNameFromSystemName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanSelectCreatePanel<E extends jmri.NamedBean> extends JPanel {

    Manager<E> _manager;
    E _defaultSelect;
    String _reference = null;
    JRadioButton existingItem = new JRadioButton();
    JRadioButton newItem;
    ButtonGroup selectcreate = new ButtonGroup();

    NamedBeanComboBox<E> existingCombo;
    JTextField hardwareAddress = new JTextField(8);
    JComboBox<String> prefixBox = new JComboBox<>();
    String systemSelectionCombo = this.getClass().getName() + ".SystemSelected";

    /**
     * Create a JPanel that provides the option to the user to either select an
     * already created bean, or to create one on the fly. This only currently
     * works with Turnouts, Sensors, Memories and Blocks.
     *
     * @param manager       the bean manager
     * @param defaultSelect the bean that is selected by default
     */
    public BeanSelectCreatePanel(Manager<E> manager, E defaultSelect) {
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
        existingCombo = new NamedBeanComboBox<E>(_manager, defaultSelect, DisplayOptions.USERNAME_SYSTEMNAME);
        LayoutEditor.setupComboBox(existingCombo, false, true);
        // If the combo list is empty we go straight to creation.
        if (existingCombo.getItemCount() == 0) {
            newItem.setSelected(true);
        }
        existingCombo.setAllowNull(true);

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
        // existingCombo.refreshCombo();
    }

    /**
     * Get the display name of the bean that has either been selected in the
     * drop down list or was asked to be created.
     *
     * @return the name of the bean
     */
    public String getDisplayName() {
        if (existingItem.isSelected()) {
            return existingCombo.getSelectedItemDisplayName();
        } else {
            try {
                E nBean = createBean();
                return nBean.getDisplayName();
            } catch (JmriException e) {
                return "";
            }
        }
    }

    /**
     * Get the named bean that has either been selected in the drop down list or
     * was asked to be created.
     *
     * @return the selected bean or a new bean
     * @throws JmriException if a bean needs to be created but can't be
     */
    public E getNamedBean() throws JmriException {
        if (existingItem.isSelected()) {
            return existingCombo.getSelectedItem();
        }
        try {
            return createBean();
        } catch (JmriException e) {
            throw e;
        }
    }

    private E createBean() throws JmriException {
        String prefix = ConnectionNameFromSystemName.getPrefixFromName((String) prefixBox.getSelectedItem());
        E nBean = null;
        if (prefix != null && _manager instanceof ProvidingManager) {
            ProvidingManager<E> provider = (ProvidingManager<E>) _manager;
            if (provider instanceof TurnoutManager) {
                String sName = "";
                try {
                    sName = ((TurnoutManager) provider).createSystemName(hardwareAddress.getText(), prefix);
                } catch (JmriException e) {
                    throw e;
                }
                try {
                    nBean = provider.provide(sName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    throw new JmriException("ErrorTurnoutAddFailed");
                }
            } else if (provider instanceof SensorManager) {
                String sName = "";
                try {
                    sName = ((SensorManager) provider).createSystemName(hardwareAddress.getText(), prefix);
                } catch (JmriException e) {
                    throw e;
                }
                try {
                    nBean = provider.provide(sName);
                } catch (IllegalArgumentException ex) {
                    // user input no good
                    throw new JmriException("ErrorSensorAddFailed");
                }
            } else {
                String sName = provider.makeSystemName(hardwareAddress.getText());
                if (provider instanceof MemoryManager) {
                    try {
                        nBean = provider.provide(sName);
                    } catch (IllegalArgumentException ex) {
                        // user input no good
                        throw new JmriException("ErrorMemoryAddFailed");
                    }
                } else if (provider instanceof BlockManager) {
                    try {
                        nBean = provider.provide(sName);
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
        updateComment(nBean, _reference);
        setDefaultNamedBean(nBean);
        return nBean;
    }

    /**
     * Set a reference that can be set against the comment for a bean.
     *
     * @param ref the default comment for a bean without a comment
     */
    public void setReference(String ref) {
        _reference = ref;
    }

    /**
     * Set the default selected item in the combo box. After it has been set,
     * the combo box becomes active and the Add Hardware box details are then
     * hidden.
     *
     * @param nBean the bean that is selected by default
     */
    public void setDefaultNamedBean(E nBean) {
        _defaultSelect = nBean;
        existingCombo.setSelectedItem(_defaultSelect);
        existingItem.setSelected(true);
        update();
    }

    /**
     * Check that the user selected something in this BeanSelectCreatePanel.
     *
     * @return true if not empty
     */
    public boolean isEmpty() {
        if (existingItem.isSelected() && existingCombo.getSelectedItem() != null) { // use existing
            log.debug("existingCombo.getSelectedBean() = {}", existingCombo.getSelectedItem().getDisplayName());
            return false;
        } else if (newItem.isSelected() && // create new
                !hardwareAddress.getText().isEmpty() && hardwareAddress.getText() != null) {
            log.debug("newBeanEntry = {}", hardwareAddress.getText());
            return false;
        }
        return true;
    }

    /**
     * Update comment on bean if there's content AND there's not already a comment.
     *
     * @param nBean   the bean to edit
     * @param content comment to add
     */
    public void updateComment(@Nonnull E nBean, String content) {
        String comment = nBean.getComment();
        log.debug((comment == null || comment.isEmpty()) ? "comment was empty" : "comment already filled");
        if((content != null && !content.isEmpty()) && (comment ==null || comment.isEmpty())) {
            log.debug("new comment added to bean {}", nBean.getDisplayName());
            nBean.setComment(content);
        } else {
            log.debug("empty _reference received");
        }
    }

    public void dispose() {
        existingCombo.dispose();
    }

    //initialize logging
    private final static Logger log = LoggerFactory.getLogger(BeanSelectCreatePanel.class.getName());

}
