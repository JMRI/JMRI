package jmri.jmrix;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JButton;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for common implementation of the NetworkConnectionConfig.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
abstract public class AbstractNetworkConnectionConfig extends AbstractConnectionConfig {

    /**
     * Create a connection configuration with a preexisting adapter. This is
     * used principally when loading a configuration that defines this
     * connection.
     *
     * @param p the adapter to create a connection configuration for
     */
    public AbstractNetworkConnectionConfig(NetworkPortAdapter p) {
        adapter = p;
    }

    /**
     * Ctor for a functional object with no preexisting adapter. Expect that the
     * subclass setInstance() will fill the adapter member.
     */
    public AbstractNetworkConnectionConfig() {
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
        hostNameField.addActionListener(e -> {
            adapter.setHostName(hostNameField.getText());
            p.setComboBoxLastSelection(adapter.getClass().getName() + ".hostname", hostNameField.getText());
        });
        hostNameField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                adapter.setHostName(hostNameField.getText());
                p.setComboBoxLastSelection(adapter.getClass().getName() + ".hostname", hostNameField.getText());
            }

            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }
        });
        portField.addActionListener(e -> {
            try {
                adapter.setPort(Integer.parseInt(portField.getText()));
            } catch (NumberFormatException ex) {
                log.warn("Could not parse port attribute");
            }
        });

        portField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                try {
                    adapter.setPort(Integer.parseInt(portField.getText()));
                } catch (java.lang.NumberFormatException ex) {
                    log.warn("Could not parse port attribute");
                }
            }

            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }
        });

        adNameField.addActionListener(e -> adapter.setAdvertisementName(adNameField.getText()));

        adNameField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                adapter.setAdvertisementName(adNameField.getText());
            }

            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }
        });

        serviceTypeField.addActionListener(e -> adapter.setServiceType(serviceTypeField.getText()));

        serviceTypeField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                adapter.setServiceType(serviceTypeField.getText());
            }

            @Override
            public void keyTyped(KeyEvent keyEvent) {
            }
        });

        options.entrySet().forEach(entry -> {
            final String item = entry.getKey();
            if (entry.getValue().getComponent() instanceof JComboBox) {
                ((JComboBox<?>) entry.getValue().getComponent()).addActionListener((ActionEvent e) -> {
                    log.debug("option combo box changed to {}", options.get(item).getItem());
                    adapter.setOptionState(item, options.get(item).getItem());
                });
            } else if (entry.getValue().getComponent() instanceof JTextField) {
                // listen for enter
                ((JTextField) entry.getValue().getComponent()).addActionListener((ActionEvent e) -> {
                    log.debug("option text field changed to {}", options.get(item).getItem());
                    adapter.setOptionState(item, options.get(item).getItem());
                });
                // listen for key press so you don't have to hit enter
                (entry.getValue().getComponent()).addKeyListener(new KeyListener() {
                    @Override
                    public void keyPressed(KeyEvent keyEvent) {
                    }

                    @Override
                    public void keyReleased(KeyEvent keyEvent) {
                        adapter.setOptionState(item, options.get(item).getItem());
                    }

                    @Override
                    public void keyTyped(KeyEvent keyEvent) {
                    }
                });
            }
        });

        addNameEntryCheckers(adapter);

        // set/change delay interval between (actually before) output (Turnout) commands
        outputIntervalSpinner.addChangeListener(e -> adapter.getSystemConnectionMemo().setOutputInterval((Integer) outputIntervalSpinner.getValue()));

        init = true;
    }

    @Override
    public void updateAdapter() {
        if (adapter.getMdnsConfigure()) {
            // set the hostname if it is not blank
            if (!(hostNameField.getText().isEmpty())) {
                adapter.setHostName(hostNameField.getText());
            }
            // set the advertisement name if it is not blank
            if (!(adNameField.getText().isEmpty())) {
                adapter.setAdvertisementName(adNameField.getText());
            }
            // set the Service Type if it is not blank.
            if (!(serviceTypeField.getText().isEmpty())) {
                adapter.setServiceType(serviceTypeField.getText());
            }
            // and get the host IP and port number
            // via mdns
            adapter.autoConfigure();
        } else {
            adapter.setHostName(hostNameField.getText());
            adapter.setPort(Integer.parseInt(portField.getText()));
        }
        options.entrySet().forEach(entry -> {
            adapter.setOptionState(entry.getKey(), entry.getValue().getItem());
        });
        if (adapter.getSystemConnectionMemo() != null && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    UserPreferencesManager p = InstanceManager.getDefault(UserPreferencesManager.class);
    protected JTextField hostNameField = new JTextField(15);
    protected JLabel hostNameFieldLabel;
    protected JTextField portField = new JTextField(10);
    protected JLabel portFieldLabel;

    protected JCheckBox showAutoConfig = new JCheckBox(Bundle.getMessage("AutoConfigLabel"));
    protected JTextField adNameField = new JTextField(15);
    protected JLabel adNameFieldLabel;
    protected JTextField serviceTypeField = new JTextField(15);
    protected JLabel serviceTypeFieldLabel;

    protected SpinnerNumberModel intervalSpinner = new SpinnerNumberModel(250, 0, 10000, 1); // 10 sec max seems long enough
    protected JSpinner outputIntervalSpinner = new JSpinner(intervalSpinner);
    protected JLabel outputIntervalLabel;
    protected JButton outputIntervalReset = new JButton(Bundle.getMessage("ButtonReset"));

    protected NetworkPortAdapter adapter = null;

    @Override
    public NetworkPortAdapter getAdapter() {
        return adapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    abstract protected void setInstance();

    @Override
    public String getInfo() {
        return adapter.getCurrentPortName();
    }

    protected void checkOptionValueValidity(String i, JComboBox<String> opt) {
        if (!adapter.getOptionState(i).equals(opt.getSelectedItem())) {
            // no, set 1st option choice
            opt.setSelectedIndex(0);
            // log before setting new value to show old value
            log.warn("Loading found invalid value for option {}, found \"{}\", setting to \"{}\"", i, adapter.getOptionState(i), opt.getSelectedItem());
            adapter.setOptionState(i, (String) opt.getSelectedItem());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadDetails(final JPanel details) {
        _details = details;
        setInstance();
        if (!init) {
            //Build up list of options
            //Hashtable<String, AbstractPortController.Option> adapterOptions = ((AbstractPortController)adapter).getOptionList();
            String[] optionsAvailable = adapter.getOptions();
            options.clear();
            for (String i : optionsAvailable) {
                if (adapter.isOptionTypeText(i) ) {
                    JTextField opt = new JTextField(15);
                    opt.setText(adapter.getOptionState(i));
                    options.put(i, new Option(adapter.getOptionDisplayName(i), opt, adapter.isOptionAdvanced(i)));
                } else if (adapter.isOptionTypePassword(i) ) {
                    JTextField opt = new JPasswordField(15);
                    opt.setText(adapter.getOptionState(i));
                    options.put(i, new Option(adapter.getOptionDisplayName(i), opt, adapter.isOptionAdvanced(i)));
                } else {
                    JComboBox<String> opt = new JComboBox<>(adapter.getOptionChoices(i));
                    opt.setSelectedItem(adapter.getOptionState(i));
                
                    // check that it worked
                    checkOptionValueValidity(i, opt);
                
                    options.put(i, new Option(adapter.getOptionDisplayName(i), opt, adapter.isOptionAdvanced(i)));
                }
            }
        }

        if (hostNameField.getActionListeners().length > 0) {
            hostNameField.removeActionListener(hostNameField.getActionListeners()[0]);
        }

        if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
            NUMOPTIONS = NUMOPTIONS + 2;
        }
        NUMOPTIONS = NUMOPTIONS + options.size();

        hostNameField.setText(adapter.getHostName());
        hostNameFieldLabel = new JLabel(Bundle.getMessage("HostFieldLabel"));
        hostNameField.setToolTipText(Bundle.getMessage("HostFieldToolTip"));
        if (adapter.getHostName() == null || adapter.getHostName().isEmpty()) {
            hostNameField.setText(p.getComboBoxLastSelection(adapter.getClass().getName() + ".hostname"));
            adapter.setHostName(hostNameField.getText());
        }

        portField.setToolTipText(Bundle.getMessage("PortFieldToolTip"));
        portField.setEnabled(true);
        portField.setText("" + adapter.getPort());
        portFieldLabel = new JLabel(Bundle.getMessage("PortFieldLabel"));

        adNameField.setToolTipText(Bundle.getMessage("AdNameFieldToolTip"));
        adNameField.setEnabled(false);
        adNameField.setText("" + adapter.getAdvertisementName());
        adNameFieldLabel = new JLabel(Bundle.getMessage("AdNameFieldLabel"));
        adNameFieldLabel.setEnabled(false);

        serviceTypeField.setToolTipText(Bundle.getMessage("ServiceTypeFieldToolTip"));
        serviceTypeField.setEnabled(false);
        serviceTypeField.setText("" + adapter.getServiceType());
        serviceTypeFieldLabel = new JLabel(Bundle.getMessage("ServiceTypeFieldLabel"));
        serviceTypeFieldLabel.setEnabled(false);

        // connection (memo) specific output command delay option, calls jmri.jmrix.SystemConnectionMemo#setOutputInterval(int)
        outputIntervalLabel = new JLabel(Bundle.getMessage("OutputIntervalLabel"));
        outputIntervalSpinner.setToolTipText(Bundle.getMessage("OutputIntervalTooltip",
                adapter.getSystemConnectionMemo().getDefaultOutputInterval(),adapter.getManufacturer()));
        JTextField field = ((JSpinner.DefaultEditor) outputIntervalSpinner.getEditor()).getTextField();
        field.setColumns(6);
        outputIntervalSpinner.setMaximumSize(outputIntervalSpinner.getPreferredSize()); // set spinner JTextField width
        outputIntervalSpinner.setValue(adapter.getSystemConnectionMemo().getOutputInterval());
        outputIntervalSpinner.setEnabled(true);
        outputIntervalReset.addActionListener((ActionEvent event) -> {
            outputIntervalSpinner.setValue(adapter.getSystemConnectionMemo().getDefaultOutputInterval());
            adapter.getSystemConnectionMemo().setOutputInterval(adapter.getSystemConnectionMemo().getDefaultOutputInterval());
        });

        showAutoConfig.setFont(showAutoConfig.getFont().deriveFont(9f));
        showAutoConfig.setForeground(Color.blue);
        showAutoConfig.addItemListener(e -> setAutoNetworkConfig());
        showAutoConfig.setSelected(adapter.getMdnsConfigure());
        setAutoNetworkConfig();

        showAdvanced.setFont(showAdvanced.getFont().deriveFont(9f));
        showAdvanced.setForeground(Color.blue);
        showAdvanced.addItemListener(e -> showAdvancedItems());
        showAdvancedItems();

        init = false;  // need to reload action listeners
        checkInitDone();
    }

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE",
        justification = "type was checked before casting")
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
        if (!isPortAdvanced()) {
            stdrows++;
        }
        if (!isHostNameAdvanced()) {
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
            if (isHostNameAdvanced()) {
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(hostNameFieldLabel, cL);
                gbLayout.setConstraints(hostNameField, cR);
                _details.add(hostNameFieldLabel);
                _details.add(hostNameField);
                i++;
            }

            if (isPortAdvanced()) {
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(portFieldLabel, cL);
                gbLayout.setConstraints(portField, cR);
                _details.add(portFieldLabel);
                _details.add(portField);
                i++;
            }

            if (showAutoConfig.isSelected()) {
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(adNameFieldLabel, cL);
                gbLayout.setConstraints(adNameField, cR);
                _details.add(adNameFieldLabel);
                _details.add(adNameField);
                i++;
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(serviceTypeFieldLabel, cL);
                gbLayout.setConstraints(serviceTypeField, cR);
                _details.add(serviceTypeFieldLabel);
                _details.add(serviceTypeField);
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
            // interval config field
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(outputIntervalLabel, cL);
            _details.add(outputIntervalLabel);
            JPanel intervalPanel = new JPanel();
            gbLayout.setConstraints(intervalPanel, cR);
            intervalPanel.add(outputIntervalSpinner);
            intervalPanel.add(outputIntervalReset);
            _details.add(intervalPanel);
            i++;
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

        if (isAutoConfigPossible()) {
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(showAutoConfig, cR);
            _details.add(showAutoConfig);
            _details.add(showAutoConfig);
            i++;
        }

        if (!isHostNameAdvanced()) {
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(hostNameFieldLabel, cL);
            gbLayout.setConstraints(hostNameField, cR);
            _details.add(hostNameFieldLabel);
            _details.add(hostNameField);
            i++;
        }

        if (!isPortAdvanced()) {
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(portFieldLabel, cL);
            gbLayout.setConstraints(portField, cR);
            _details.add(portFieldLabel);
            _details.add(portField);
            i++;
        }
        return addStandardDetails(adapter, incAdvanced, i);
    }

    public boolean isHostNameAdvanced() {
        return false;
    }

    /**
     * Determine whether to display port in Advanced options.
     * <p>
     * Default in Abstract Net Conn Config. Abstract True.
     * @return true to display port in advanced options.
     */
    public boolean isPortAdvanced() {
        return true;
    }

    public boolean isAutoConfigPossible() {
        return false;
    }

    public void setAutoNetworkConfig() {
        if (showAutoConfig.isSelected()) {
            portField.setEnabled(false);
            portFieldLabel.setEnabled(false);
            adapter.setMdnsConfigure(true);
        } else {
            portField.setEnabled(true);
            portFieldLabel.setEnabled(true);
            adapter.setMdnsConfigure(false);
        }
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
        if (adapter.getSystemConnectionMemo() != null) {
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
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractNetworkConnectionConfig.class);

}
