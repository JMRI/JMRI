package jmri.jmrix;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ListCellRenderer;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for common implementation of the ConnectionConfig
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author George Warner Copyright (c) 2017-2018
 */
abstract public class AbstractUsbConnectionConfig extends AbstractConnectionConfig {

    /**
     * Create a connection configuration with a preexisting adapter. This is
     * used principally when loading a configuration that defines this
     * connection.
     *
     * @param p the adapter to create a connection configuration for
     */
    public AbstractUsbConnectionConfig(UsbPortAdapter p) {
        adapter = p;
        //addToActionList();
        log.debug("*	AbstractUSBConnectionConfig({})", p);
    }

    /**
     * Ctor for a functional object with no preexisting adapter. Expect that the
     * subclass setInstance() will fill the adapter member.
     */
    public AbstractUsbConnectionConfig() {
        this(null);
        log.debug("*	AbstractUSBConnectionConfig()");
    }

    protected UsbPortAdapter adapter = null;

    @Override
    public UsbPortAdapter getAdapter() {
        log.debug("*	getAdapter()");
        return adapter;
    }

    protected boolean init = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkInitDone() {
        log.debug("init called for {}", name());
        if (!init) {
            if (adapter.getSystemConnectionMemo() != null) {
                systemPrefixField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("ConnectionPrefixDialog", systemPrefixField.getText()));
                            systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
                        }
                    }
                });
                systemPrefixField.addFocusListener(new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("ConnectionPrefixDialog", systemPrefixField.getText()));
                            systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
                        }
                    }

                    @Override
                    public void focusGained(FocusEvent e) {
                    }
                });
                connectionNameField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("ConnectionNameDialog", connectionNameField.getText()));
                            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                        }
                    }
                });
                connectionNameField.addFocusListener(new FocusListener() {
                    @Override
                    public void focusLost(FocusEvent e) {
                        if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
                            JOptionPane.showMessageDialog(null, Bundle.getMessage("ConnectionNameDialog", connectionNameField.getText()));
                            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                        }
                    }

                    @Override
                    public void focusGained(FocusEvent e) {
                    }
                });
            }
            portBox.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    refreshPortBox();
                }

                @Override
                public void focusLost(FocusEvent e) {
                }

            });

            for (Map.Entry<String, Option> entry : options.entrySet()) {
                final String item = entry.getKey();
                if (entry.getValue().getComponent() instanceof JComboBox) {
                    ((JComboBox<?>) entry.getValue().getComponent()).addActionListener((ActionEvent e) -> {
                        adapter.setOptionState(item, options.get(item).getItem());
                    });
                }
            }

            init = true;
        }
    }

    @Override
    public void updateAdapter() {
        log.debug("*	updateAdapter()");
    }

    protected UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
    protected JComboBox<String> portBox = new JComboBox<>();
    protected JLabel portBoxLabel;

    @Override
    public String getInfo() {
        log.debug("*	getInfo()");
        String t = (String) portBox.getSelectedItem();
        if (t != null) {
            return t;
        } else if ((adapter != null) && (adapter.getCurrentPortName() != null)) {
            return adapter.getCurrentPortName();
        }

        return JmrixConfigPane.NONE;
    }

    List<String> newList = null;
    List<String> originalList = null;
    String invalidPort = null;

    public void refreshPortBox() {
        log.debug("*	refreshPortBox()");
        if (!init) {
            newList = getPortNames();
            portBox.setRenderer(new ComboBoxRenderer());
            // Add this line to ensure that the combo box header isn't made too narrow
            portBox.setPrototypeDisplayValue("A fairly long port name of 40 characters"); //NO18N
        } else {
            List<String> v2 = getPortNames();
            if (v2.equals(originalList)) {
                log.debug("List of valid Ports has not changed, therefore we will not refresh the port list");
                // but we will insist on setting the current value into the port
                adapter.setPort((String) portBox.getSelectedItem());
                return;
            }
            log.debug("List of valid Ports has been changed, therefore we will refresh the port list");
            newList = new ArrayList<>(v2);
        }

        if (newList == null) {
            log.error("port name List v is null!");
            return;
        }

        /* As we make amendments to the list of ports in newList, we keep a copy of it before
         modification. This copy is then used to validate against any changes in the port lists.
         */
        originalList = new ArrayList<>(newList);
        if (portBox.getActionListeners().length > 0) {
            portBox.removeActionListener(portBox.getActionListeners()[0]);
        }
        portBox.removeAllItems();
        log.debug("getting fresh list of available Serial Ports");

        if (newList.isEmpty()) {
            newList.add(0, Bundle.getMessage("noPortsFound"));
        }
        String portName = adapter.getCurrentPortName();
        if (portName != null && !portName.equals(Bundle.getMessage("noneSelected")) && !portName.equals(Bundle.getMessage("noPortsFound"))) {
            if (!newList.contains(portName)) {
                newList.add(0, portName);
                invalidPort = portName;
                portBox.setForeground(Color.red);
            } else if (invalidPort != null && invalidPort.equals(portName)) {
                invalidPort = null;
            }
        } else {
            if (!newList.contains(portName)) {
                newList.add(0, Bundle.getMessage("noneSelected"));
            } else if (p.getComboBoxLastSelection(adapter.getClass().getName() + ".port") == null) {
                newList.add(0, Bundle.getMessage("noneSelected"));
            }
        }

        updateUsbPortNames(portName, portBox, newList);

        // If no name is selected, select one that seems most likely
        boolean didSetName = false;
        if ((portName == null)
                || portName.equals(Bundle.getMessage("noneSelected"))
                || portName.equals(Bundle.getMessage("noPortsFound"))) {
//            for (int i = 0; i < portBox.getItemCount(); i++) {
//                for (String friendlyName : getPortFriendlyNames()) {
//                    if ((portBox.getItemAt(i)).contains(friendlyName)) {
//                        portBox.setSelectedIndex(i);
//                        adapter.setPort(portBox.getItemAt(i));
//                        didSetName = true;
//                        break;
//                    }
//                }
//            }
            // if didn't set name, don't leave it hanging
            if (!didSetName) {
                portBox.setSelectedIndex(0);
            }
        }
        // finally, insist on synchronization of selected port name with underlying port

        adapter.setPort((String) portBox.getSelectedItem());

        // add a listener for later changes
        portBox.addActionListener(
                (ActionEvent e) -> {
                    String port = (String) portBox.getSelectedItem();
                    adapter.setPort(port);
                }
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(final JPanel details) {
        log.debug("*	loadDetails()");
        _details = details;
        setInstance();
        if (!init) {
            //Build up list of options
            String[] optionsAvailable = adapter.getOptions();
            options.clear();
            for (String i : optionsAvailable) {
                JComboBox<String> opt = new JComboBox<>(adapter.getOptionChoices(i));
                opt.setSelectedItem(adapter.getOptionState(i));
                // check that it worked
                if (!adapter.getOptionState(i).equals(opt.getSelectedItem())) {
                    // no, set 1st option choice
                    opt.setSelectedIndex(0);
                    // log before setting new value to show old value
                    log.warn("Loading found invalid value for option {}, found \"{}\", setting to \"{}\"", i, adapter.getOptionState(i), opt.getSelectedItem());
                    adapter.setOptionState(i, (String) opt.getSelectedItem());
                }
                options.put(i, new Option(adapter.getOptionDisplayName(i), opt, adapter.isOptionAdvanced(i)));
            }
        }

        try {
            newList = getPortNames();
            if (log.isDebugEnabled()) {
                log.debug("loadDetails called in class {}", this.getClass().getName());
                log.debug("adapter class: {}", adapter.getClass().getName());
                log.debug("loadDetails called for {}", name());
                if (newList != null) {
                    log.debug("Found {} ports", newList.size());
                } else {
                    log.debug("Zero-length port List");
                }
            }
        } catch (UnsatisfiedLinkError e1) {
            log.error("UnsatisfiedLinkError - the serial library has not been installed properly");
            log.error("java.library.path=" + System.getProperty("java.library.path", "<unknown>"));
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ErrorComLibLoad"));
            return;
        }

        if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
            NUMOPTIONS = NUMOPTIONS + 2;
        }

        refreshPortBox();

        NUMOPTIONS = NUMOPTIONS + options.size();

        portBoxLabel = new JLabel(Bundle.getMessage("UsbPortLocationLabel"));

        showAdvanced.setFont(showAdvanced.getFont().deriveFont(9f));
        showAdvanced.setForeground(Color.blue);
        showAdvanced.addItemListener((ItemEvent e) -> {
            showAdvancedItems();
        });
        showAdvancedItems();
        init = false;       // need to reload action listeners
        checkInitDone();
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE",
        justification = "Type is checked before casting")
    protected void showAdvancedItems() {
        log.debug("*	showAdvancedItems()");
        _details.removeAll();
        cL.anchor = GridBagConstraints.WEST;
        cL.insets = new Insets(2, 5, 0, 5);
        cR.insets = new Insets(2, 0, 0, 5);
        cR.anchor = GridBagConstraints.WEST;
        cR.gridx = 1;
        cL.gridx = 0;
        int i = 0;

        boolean incAdvancedOptions = isPortAdvanced();

        if (!incAdvancedOptions) {
            for (Map.Entry<String, Option> entry : options.entrySet()) {
                if (entry.getValue().isAdvanced()) {
                    incAdvancedOptions = true;
                    break;
                }
            }
        }

        _details.setLayout(gbLayout);

        i = addStandardDetails(incAdvancedOptions, i);

        showAdvanced.setVisible(incAdvancedOptions);

        if (incAdvancedOptions && showAdvanced.isSelected()) {
            if (isPortAdvanced()) {
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(portBoxLabel, cL);
                gbLayout.setConstraints(portBox, cR);

                //panel.add(row1Label);
                _details.add(portBoxLabel);
                _details.add(portBox);
                i++;
            }

            for (Map.Entry<String, Option> entry : options.entrySet()) {
                if (entry.getValue().isAdvanced()) {
                    cR.gridy = i;
                    cL.gridy = i;
                    gbLayout.setConstraints(entry.getValue().getLabel(), cL);
                    gbLayout.setConstraints(entry.getValue().getComponent(), cR);
                    _details.add(entry.getValue().getLabel());
                    _details.add(entry.getValue().getComponent());
                    i++;
                }
            }
        }
        cL.gridwidth = 2;
        for (JComponent item : additionalItems) {
            cL.gridy = i;
            gbLayout.setConstraints(item, cL);
            _details.add(item);
            i++;
        }
        cL.gridwidth = 1;

        if ((_details.getParent() != null) && (_details.getParent() instanceof JViewport)) {
            JViewport vp = (JViewport) _details.getParent();
            vp.revalidate();
            vp.repaint();
        }
    }

    protected int addStandardDetails(boolean incAdvanced, int i) {
        log.debug("*	addStandardDetails()");
        if (!isPortAdvanced()) {
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(portBoxLabel, cL);
            gbLayout.setConstraints(portBox, cR);
            _details.add(portBoxLabel);
            _details.add(portBox);
            i++;
        }

        return addStandardDetails(adapter, incAdvanced, i);
    }

    public boolean isPortAdvanced() {
        log.debug("*	isPortAdvanced()");
        return false;
    }

    @Override
    public String getManufacturer() {
        log.debug("*	getManufacturer()");
        return adapter.getManufacturer();
    }

    @Override
    public void setManufacturer(String manufacturer) {
        setInstance();
        log.debug("*	setManufacturer('{}')", manufacturer);
        adapter.setManufacturer(manufacturer);
    }

    @Override
    public boolean getDisabled() {
        log.debug("*	getDisabled()");
        if (adapter == null) {
            return true;
        }
        return adapter.getDisabled();
    }

    @Override
    public void setDisabled(boolean disabled) {
        log.debug("*	setDisabled({})", disabled ? "True" : "False");
        if (adapter != null) {
            adapter.setDisabled(disabled);
        }
    }

    @Override
    public String getConnectionName() {
        log.debug("*	getConnectionName()");
        if ((adapter != null) && (adapter.getSystemConnectionMemo() != null)) {
            return adapter.getSystemConnectionMemo().getUserName();
        } else {
            return name();
        }
    }

    @Override
    public void dispose() {
        log.debug("*	dispose()");
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
        //removeFromActionList();
        super.dispose();

    }

    class ComboBoxRenderer extends JLabel
            implements ListCellRenderer<String> {

        public ComboBoxRenderer() {
            setHorizontalAlignment(LEFT);
            setVerticalAlignment(CENTER);
        }

        /*
         * This method finds the image and text corresponding
         * to the selected value and returns the label, set up
         * to display the text and image.
         */
        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list,
                String name,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            setOpaque(index > -1);
            setForeground(Color.black);
            list.setSelectionForeground(Color.black);
            if (isSelected && index > -1) {
                setBackground(list.getSelectionBackground());
            } else {
                setBackground(list.getBackground());
            }
            if (invalidPort != null) {
                if ((name == null) || name.isEmpty() || name.equals(invalidPort)) {
                    list.setSelectionForeground(Color.red);
                    setForeground(Color.red);
                }
            }

            setText(name);

            return this;
        }
    }

    /**
     * Handle friendly port names. Note that this changes the selection in
     * portCombo, so that should be tracked after this returns.
     *
     * @param portName  The currently-selected port name
     * @param portCombo The combo box that's displaying the available ports
     * @param portList  The list of valid (unfriendly) port names
     */
    protected synchronized static void updateUsbPortNames(String portName, JComboBox<String> portCombo, List<String> portList) {
        for (int i = 0; i < portList.size(); i++) {
            String commPort = portList.get(i);
            portCombo.addItem(commPort);
            if (commPort.equals(portName)) {
                portCombo.setSelectedIndex(i);
            }
        }
    }

    @Nonnull
    protected List<String> getPortNames() {
        log.error("getPortNames() called in abstract class; should be overridden.");
        return new ArrayList<>();
    }

    private final static Logger log
            = LoggerFactory.getLogger(AbstractUsbConnectionConfig.class);

}
