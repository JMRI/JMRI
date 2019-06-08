package jmri.jmrix;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
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
        hostNameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adapter.setHostName(hostNameField.getText());
                p.setComboBoxLastSelection(adapter.getClass().getName() + ".hostname", hostNameField.getText());
            }
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
        portField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    adapter.setPort(Integer.parseInt(portField.getText()));
                } catch (java.lang.NumberFormatException ex) {
                    log.warn("Could not parse port attribute");
                }
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

        adNameField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adapter.setAdvertisementName(adNameField.getText());
            }
        });

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

        serviceTypeField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                adapter.setServiceType(serviceTypeField.getText());
            }
        });

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

        for (Map.Entry<String, Option> entry : options.entrySet()) {
            final String item = entry.getKey();
            if (entry.getValue().getComponent() instanceof JComboBox) {
                ((JComboBox<?>) entry.getValue().getComponent()).addActionListener((ActionEvent e) -> {
                    adapter.setOptionState(item, options.get(item).getItem());
                });
            }
        }

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
        init = true;
    }

    @Override
    public void updateAdapter() {
        if (adapter.getMdnsConfigure()) {
            // set the hostname if it is not blank
            if (!(hostNameField.getText().equals(""))) {
                adapter.setHostName(hostNameField.getText());
            }
            // set the advertisement name if it is not blank
            if (!(adNameField.getText().equals(""))) {
                adapter.setAdvertisementName(adNameField.getText());
            }
            // set the Service Type if it is not blank.
            if (!(serviceTypeField.getText().equals(""))) {
                adapter.setServiceType(serviceTypeField.getText());
            }
            // and get the host IP and port number
            // via mdns
            adapter.autoConfigure();
        } else {
            adapter.setHostName(hostNameField.getText());
            adapter.setPort(Integer.parseInt(portField.getText()));
        }
        for (Map.Entry<String, Option> entry : options.entrySet()) {
            adapter.setOptionState(entry.getKey(), entry.getValue().getItem());
        }
        if (adapter.getSystemConnectionMemo() != null && !adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
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
                JComboBox<String> opt = new JComboBox<String>(adapter.getOptionChoices(i));
                opt.setSelectedItem(adapter.getOptionState(i));
                
                // check that it worked
                checkOptionValueValidity(i, opt);
                
                options.put(i, new Option(adapter.getOptionDisplayName(i), opt, adapter.isOptionAdvanced(i)));
            }
        }

        if (hostNameField.getActionListeners().length > 0) {
            hostNameField.removeActionListener(hostNameField.getActionListeners()[0]);
        }

        if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.setValue(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
            NUMOPTIONS = NUMOPTIONS + 2;
        }
        NUMOPTIONS = NUMOPTIONS + options.size();

        hostNameField.setText(adapter.getHostName());
        hostNameFieldLabel = new JLabel(Bundle.getMessage("HostFieldLabel"));
        hostNameField.setToolTipText(Bundle.getMessage("HostFieldToolTip"));
        if (adapter.getHostName() == null || adapter.getHostName().equals("")) {
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

        showAutoConfig.setFont(showAutoConfig.getFont().deriveFont(9f));
        showAutoConfig.setForeground(Color.blue);
        showAutoConfig.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                setAutoNetworkConfig();
            }
        });
        showAutoConfig.setSelected(adapter.getMdnsConfigure());
        setAutoNetworkConfig();

        showAdvanced.setFont(showAdvanced.getFont().deriveFont(9f));
        showAdvanced.setForeground(Color.blue);
        showAdvanced.addItemListener(
                new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                showAdvancedItems();
            }
        });
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
