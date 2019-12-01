package jmri.jmrix;

import apps.startup.StartupActionModelUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import jmri.InstanceManager;
import jmri.util.PortNameMapper;
import jmri.util.PortNameMapper.SerialPortFriendlyName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;

/**
 * Abstract base class for common implementation of the SerialConnectionConfig.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
abstract public class AbstractSerialConnectionConfig extends AbstractConnectionConfig {

    /**
     * Ctor for an object being created during load process.
     *
     * @param p port being configured
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Thought to be safe as default connection config")
    public AbstractSerialConnectionConfig(jmri.jmrix.PortAdapter p) {
            this((jmri.jmrix.SerialPortAdapter) p);
    }

    public AbstractSerialConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        adapter = p;
        addToActionList();
    }

    /**
     * Ctor for a functional object with no preexisting adapter. Expect that the
     * subclass setInstance() will fill the adapter member.
     */
    public AbstractSerialConnectionConfig() {
        adapter = null;
        addToActionList();
    }

    @Override
    public jmri.jmrix.SerialPortAdapter getAdapter() {
        return adapter;
    }

    protected boolean init = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void checkInitDone() {
        log.debug("init called for {}", name());
        if (init) {
            return;
        }

        baudBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adapter.configureBaudRate((String) baudBox.getSelectedItem());
                p.setComboBoxLastSelection(adapter.getClass().getName() + ".baud", (String) baudBox.getSelectedItem()); // NOI18N
            }
        });

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

    @Override
    public void updateAdapter() {
        log.debug("updateAdapter() to {}", systemPrefixField.getText());
        adapter.setPort(PortNameMapper.getPortFromName((String) portBox.getSelectedItem()));
        adapter.configureBaudRateFromIndex(baudBox.getSelectedIndex()); // manage by index, not item value
        for (Map.Entry<String, Option> entry : options.entrySet()) {
            adapter.setOptionState(entry.getKey(), entry.getValue().getItem());
        }

        if (adapter.getSystemConnectionMemo() != null && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);
    protected JComboBox<String> portBox = new JComboBox<>();
    protected JLabel portBoxLabel;
    protected JComboBox<String> baudBox = new JComboBox<>();
    protected JLabel baudBoxLabel;
    protected String[] baudList;
    protected jmri.jmrix.SerialPortAdapter adapter = null;

    /**
     * {@inheritDoc}
     */
    @Override
    abstract protected void setInstance();

    @Override
    public String getInfo() {
        String t = (String) portBox.getSelectedItem();
        if (t != null) {
            return PortNameMapper.getPortFromName(t);
            //return t;
        } else if ((adapter != null) && (adapter.getCurrentPortName() != null)) {
            return adapter.getCurrentPortName();
        }

        return JmrixConfigPane.NONE;
    }

    @SuppressWarnings("UseOfObsoleteCollectionType")
    Vector<String> v;
    @SuppressWarnings("UseOfObsoleteCollectionType")
    Vector<String> originalList;
    String invalidPort = null;

    @SuppressWarnings("UseOfObsoleteCollectionType")
    public void refreshPortBox() {
        if (!init) {
            v = getPortNames();
            portBox.setRenderer(new ComboBoxRenderer());
            // Add this line to ensure that the combo box header isn't made too narrow
            portBox.setPrototypeDisplayValue("A fairly long port name of 40 characters"); //NO18N
        } else {
            Vector<String> v2 = getPortNames();
            if (v2.equals(originalList)) {
                log.debug("List of valid Ports has not changed, therefore we will not refresh the port list");
                // but we will insist on setting the current value into the port
                adapter.setPort(PortNameMapper.getPortFromName((String) portBox.getSelectedItem()));
                return;
            }
            log.debug("List of valid Ports has been changed, therefore we will refresh the port list");
            v = new Vector<>();
            v.setSize(v2.size());
            Collections.copy(v, v2);
        }

        if (v == null) {
            log.error("port name Vector v is null!");
            return;
        }

        /* as we make amendments to the list of port in vector v, we keep a copy of it before
         modification, this copy is then used to validate against any changes in the port lists.
         */
        originalList = new Vector<>();
        originalList.setSize(v.size());
        Collections.copy(originalList, v);
        if (portBox.getActionListeners().length > 0) {
            portBox.removeActionListener(portBox.getActionListeners()[0]);
        }
        portBox.removeAllItems();
        log.debug("getting fresh list of available Serial Ports");

        if (v.isEmpty()) {
            v.add(0, Bundle.getMessage("noPortsFound"));
        }
        String portName = adapter.getCurrentPortName();
        if (portName != null && !portName.equals(Bundle.getMessage("noneSelected")) && !portName.equals(Bundle.getMessage("noPortsFound"))) {
            if (!v.contains(portName)) {
                v.add(0, portName);
                invalidPort = portName;
                portBox.setForeground(Color.red);
            } else if (invalidPort != null && invalidPort.equals(portName)) {
                invalidPort = null;
            }
        } else {
            if (!v.contains(portName)) {
                v.add(0, Bundle.getMessage("noneSelected"));
            } else if (p.getComboBoxLastSelection(adapter.getClass().getName() + ".port") == null) {
                v.add(0, Bundle.getMessage("noneSelected"));
            }
        }
        updateSerialPortNames(portName, portBox, v);

        // If there's no name selected, select one that seems most likely
        boolean didSetName = false;
        if (portName == null || portName.equals(Bundle.getMessage("noneSelected")) || portName.equals(Bundle.getMessage("noPortsFound"))) {
            for (int i = 0; i < portBox.getItemCount(); i++) {
                for (String friendlyName : getPortFriendlyNames()) {
                    if ((portBox.getItemAt(i)).contains(friendlyName)) {
                        portBox.setSelectedIndex(i);
                        adapter.setPort(PortNameMapper.getPortFromName(portBox.getItemAt(i)));
                        didSetName = true;
                        break;
                    }
                }
            }
            // if didn't set name, don't leave it hanging
            if (!didSetName) {
                portBox.setSelectedIndex(0);
            }
        }
        // finally, insist on synchronization of selected port name with underlying port
        adapter.setPort(PortNameMapper.getPortFromName((String) portBox.getSelectedItem()));

        // add a listener for later changes
        portBox.addActionListener((ActionEvent e) -> {
            String port = PortNameMapper.getPortFromName((String) portBox.getSelectedItem());
            adapter.setPort(port);
        });
    }

    String value;

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("UseOfObsoleteCollectionType")
    public void loadDetails(final JPanel details) {
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
            v = getPortNames();
            if (log.isDebugEnabled()) {
                log.debug("loadDetails called in class {}", this.getClass().getName());
                log.debug("adapter class: {}", adapter.getClass().getName());
                log.debug("loadDetails called for {}", name());
                if (v != null) {
                    log.debug("Found {} ports", v.size());
                } else {
                    log.debug("Zero-length port vector");
                }
            }
        } catch (java.lang.UnsatisfiedLinkError e1) {
            log.error("UnsatisfiedLinkError - the serial library has not been installed properly");
            log.error("java.library.path={}", System.getProperty("java.library.path", "<unknown>"));
            javax.swing.JOptionPane.showMessageDialog(null, "Failed to load comm library.\nYou have to fix that before setting preferences.");
            return;
        }

        if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
            NUMOPTIONS = NUMOPTIONS + 2;
        }

        refreshPortBox();

        baudList = adapter.validBaudRates(); // when not supported should not return null, but an empty String[] {}
        // need to remove ActionListener before addItem() or action event will occur
        if (baudBox.getActionListeners().length > 0) {
            baudBox.removeActionListener(baudBox.getActionListeners()[0]);
        }
        // rebuild baudBox combo list
        baudBox.removeAllItems();
        if (log.isDebugEnabled()) {
            log.debug("after remove, {} items, first is {}", baudBox.getItemCount(),
                    baudBox.getItemAt(0));
        }

        // empty array means: baud not supported by adapter (but extends serialConnConfig)
        if (baudList.length == 0) {
            log.debug("empty array received from adapter");
        }
        for (String baudList1 : baudList) {
            baudBox.addItem(baudList1);
        }
        if (log.isDebugEnabled()) {
            log.debug("after reload, {} items, first is {}", baudBox.getItemCount(),
                    baudBox.getItemAt(0));
        }

        if (baudList.length > 1) {
            baudBox.setToolTipText(Bundle.getMessage("TipBaudRateMatch"));
            baudBox.setEnabled(true);
        } else {
            baudBox.setToolTipText(Bundle.getMessage("TipBaudRateFixed"));
            baudBox.setEnabled(false);
        }

        NUMOPTIONS = NUMOPTIONS + options.size();

        portBoxLabel = new JLabel(Bundle.getMessage("SerialPortLabel"));
        baudBoxLabel = new JLabel(Bundle.getMessage("BaudRateLabel"));
        if (baudBox.getItemCount() > 0) { // skip when adapter returned an empty array (= spotbug's preference)
            baudBox.setSelectedIndex(adapter.getCurrentBaudIndex());
        }
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
        _details.removeAll();
        cL.anchor = GridBagConstraints.WEST;
        cL.insets = new Insets(2, 5, 0, 5);
        cR.insets = new Insets(2, 0, 0, 5);
        cR.anchor = GridBagConstraints.WEST;
        cR.gridx = 1;
        cL.gridx = 0;
        int i = 0;
        int stdrows = 0;
        boolean incAdvancedOptions = true;
        if (!isBaudAdvanced()) {
            stdrows++;
        }
        if (!isPortAdvanced()) {
            stdrows++;
        }
        for (Map.Entry<String, Option> entry : options.entrySet()) {
            if (!entry.getValue().isAdvanced()) {
                stdrows++;
            }
        }

        if (adapter.getSystemConnectionMemo() != null) {
            stdrows = stdrows + 2;
        }
        if (stdrows == NUMOPTIONS) {
            incAdvancedOptions = false;
        }
        _details.setLayout(gbLayout);
        i = addStandardDetails(incAdvancedOptions, i);
        if (showAdvanced.isSelected()) {

            if (isPortAdvanced()) {
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(portBoxLabel, cL);
                gbLayout.setConstraints(portBox, cR);

                _details.add(portBoxLabel);
                _details.add(portBox);
                i++;
            }

            if (isBaudAdvanced()) {
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(baudBoxLabel, cL);
                gbLayout.setConstraints(baudBox, cR);
                _details.add(baudBoxLabel);
                _details.add(baudBox);
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

        if (_details.getParent() != null && _details.getParent() instanceof javax.swing.JViewport) {
            javax.swing.JViewport vp = (javax.swing.JViewport) _details.getParent();
            vp.revalidate();
            vp.repaint();
        }
    }

    protected int addStandardDetails(boolean incAdvanced, int i) {
        if (!isPortAdvanced()) {
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(portBoxLabel, cL);
            gbLayout.setConstraints(portBox, cR);
            _details.add(portBoxLabel);
            _details.add(portBox);
            i++;
        }

        if (!isBaudAdvanced()) {
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(baudBoxLabel, cL);
            gbLayout.setConstraints(baudBox, cR);
            _details.add(baudBoxLabel);
            _details.add(baudBox);
            i++;
        }

        return addStandardDetails(adapter, incAdvanced, i);
    }

    public boolean isPortAdvanced() {
        return false;
    }

    public boolean isBaudAdvanced() {
        return true;
    }

    @Override
    public String getManufacturer() {
        return adapter.getManufacturer();
    }

    @Override
    public void setManufacturer(String manufacturer) {
        setInstance();
        adapter.setManufacturer(manufacturer);
    }

    @Override
    public boolean getDisabled() {
        if (adapter == null) {
            return true;
        }
        return adapter.getDisabled();
    }

    @Override
    public void setDisabled(boolean disabled) {
        if (adapter != null) {
            adapter.setDisabled(disabled);
        }
    }

    @Override
    public String getConnectionName() {
        if ((adapter != null) && (adapter.getSystemConnectionMemo() != null)) {
            return adapter.getSystemConnectionMemo().getUserName();
        } else {
            return name();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
        removeFromActionList();
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
                String port = PortNameMapper.getPortFromName(name);
                if (port.equals(invalidPort)) {
                    list.setSelectionForeground(Color.red);
                    setForeground(Color.red);
                }
            }

            setText(name);

            return this;
        }
    }

    /**
     * Handle friendly port names. Note that this
     * changes the selection in portCombo, so
     * that should be tracked after this returns.
     *
     * @param portName The currently-selected port name
     * @param portCombo The combo box that's displaying the available ports
     * @param portList The list of valid (unfriendly) port names
     */
    @SuppressWarnings("UseOfObsoleteCollectionType")
    protected synchronized static void updateSerialPortNames(String portName, JComboBox<String> portCombo, Vector<String> portList) {
        for (Map.Entry<String, SerialPortFriendlyName> en : PortNameMapper.getPortNameMap().entrySet()) {
            en.getValue().setValidPort(false);
        }
        for (int i = 0; i < portList.size(); i++) {
            String commPort = portList.elementAt(i);
            SerialPortFriendlyName port = PortNameMapper.getPortNameMap().get(commPort);
            if (port == null) {
                port = new SerialPortFriendlyName(commPort, null);
                PortNameMapper.getPortNameMap().put(commPort, port);
            }
            port.setValidPort(true);
            portCombo.addItem(port.getDisplayName());
            if (commPort.equals(portName)) {
                portCombo.setSelectedIndex(i);
            }
        }
    }

    @SuppressWarnings("UseOfObsoleteCollectionType")
    protected Vector<String> getPortNames() {
        //reloadDriver(); // Refresh the list of communication ports
        // first, check that the comm package can be opened and ports seen
        Vector<String> portNameVector = new Vector<>();
        Enumeration<CommPortIdentifier> portIDs = CommPortIdentifier.getPortIdentifiers();
        // find the names of suitable ports
        while (portIDs.hasMoreElements()) {
            CommPortIdentifier id = portIDs.nextElement();
            // filter out line printers
            if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL) // accumulate the names in a vector
            {
                portNameVector.addElement(id.getName());
            }
        }
        return portNameVector;
    }

    /**
     * This provides a method to return potentially meaningful names that are
     * used in OS to help identify ports against Hardware.
     *
     * @return array of friendly port names
     */
    protected String[] getPortFriendlyNames() {
        return new String[]{};
    }

    /**
     * This is purely here for systems that do not implement the
     * SystemConnectionMemo and can be removed once they have been migrated.
     *
     * @return Resource bundle for action model
     */
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    protected final void addToActionList() {
        StartupActionModelUtil util = InstanceManager.getNullableDefault(StartupActionModelUtil.class);
        ResourceBundle bundle = getActionModelResourceBundle();
        if (bundle == null || util == null) {
            return;
        }
        Enumeration<String> e = bundle.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                util.addAction(key, bundle.getString(key));
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class \"{}\"", key);
            }
        }
    }

    protected void removeFromActionList() {
        StartupActionModelUtil util = InstanceManager.getNullableDefault(StartupActionModelUtil.class);
        ResourceBundle bundle = getActionModelResourceBundle();
        if (bundle == null || util == null) {
            return;
        }
        Enumeration<String> e = bundle.getKeys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            try {
                util.removeAction(key);
            } catch (ClassNotFoundException ex) {
                log.error("Did not find class \"{}\"", key);
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractSerialConnectionConfig.class);

}
