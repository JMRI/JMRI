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
import java.util.Hashtable;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for common implementation of the Simulator
 * ConnectionConfig Currently uses the serial adapter, but this will change to
 * the simulator adapter in due course.
 *
 * @author Kevin Dickerson Copyright (C) 2001, 2003
 */
abstract public class AbstractSimulatorConnectionConfig extends AbstractConnectionConfig {

    /**
     * Ctor for an object being created during load process Currently uses the
     * serialportadapter, but this will change to a simulator port adapter in
     * due course.
     */
    public AbstractSimulatorConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        adapter = p;
    }

    public jmri.jmrix.SerialPortAdapter getAdapter() {
        return adapter;
    }

    /**
     * Ctor for a functional object with no prexisting adapter. Expect that the
     * subclass setInstance() will fill the adapter member.
     */
    public AbstractSimulatorConnectionConfig() {
        log.debug("AbstractSimulatorConnectionConfig null ctor used");
        adapter = null;
    }

    protected boolean init = false;

    @SuppressWarnings("unchecked")
    protected void checkInitDone() {
        if (log.isDebugEnabled()) {
            log.debug("init called for " + name());
        }
        if (init) {
            return;
        }

        if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }
            });
            systemPrefixField.addFocusListener(new FocusListener() {
                public void focusLost(FocusEvent e) {
                    if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }

                public void focusGained(FocusEvent e) {
                }
            });
            connectionNameField.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }
            });
            connectionNameField.addFocusListener(new FocusListener() {
                public void focusLost(FocusEvent e) {
                    if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }

                public void focusGained(FocusEvent e) {
                }
            });
            for (String i : options.keySet()) {
                final String item = i;
                if (options.get(i).getComponent() instanceof JComboBox) {
                    ((JComboBox<?>) options.get(i).getComponent()).addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            adapter.setOptionState(item, options.get(item).getItem());
                        }
                    });
                }
            }

        }
        init = true;
    }

    public void updateAdapter() {
        for (String i : options.keySet()) {
            adapter.setOptionState(i, options.get(i).getItem());
        }

        if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    protected String[] baudList;
    protected jmri.jmrix.SerialPortAdapter adapter = null;

    protected String systemPrefix;
    protected String connectionName;

    /**
     * Load the adapter with an appropriate object
     * <i>unless</I> its already been set.
     */
    abstract protected void setInstance();

    /**
     * Returns the port the simulator is connected to which is "none";
     */
    public String getInfo() {
        return rb.getString("none");
    }

    static java.util.ResourceBundle rb
            = java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixBundle");

    public void loadDetails(final JPanel details) {
        _details = details;
        setInstance();
        if (!init) {
            String[] optionsAvailable = adapter.getOptions();
            options = new Hashtable<String, Option>();
            for (String i : optionsAvailable) {
                JComboBox<String> opt = new JComboBox<String>(adapter.getOptionChoices(i));
                opt.setSelectedItem(adapter.getOptionState(i));
                // check that it worked
                if (!adapter.getOptionState(i).equals(opt.getSelectedItem())) {
                    // no, set 1st option choice
                    opt.setSelectedIndex(0);
                    adapter.setOptionState(i, (String) opt.getSelectedItem());
                    log.warn("Loading found invalid value for option {}, found \"{}\", setting to \"{}\"", i, adapter.getOptionState(i), opt.getSelectedItem());
                }
                options.put(i, new Option(adapter.getOptionDisplayName(i), opt, adapter.isOptionAdvanced(i)));
            }
        }

        if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
        NUMOPTIONS = NUMOPTIONS + options.size();

        showAdvanced.setFont(showAdvanced.getFont().deriveFont(9f));
        showAdvanced.setForeground(Color.blue);
        showAdvanced.addItemListener(
                new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        showAdvancedItems();
                    }
                });
        showAdvancedItems();
        init = false;		// need to reload action listeners
        checkInitDone();
    }

    protected void showAdvancedItems() {
        _details.removeAll();
        cL.anchor = GridBagConstraints.WEST;
        cL.insets = new Insets(2, 5, 0, 5);
        cR.insets = new Insets(2, 0, 0, 5);
        cR.anchor = GridBagConstraints.WEST;
        cR.gridx = 1;
        cL.gridx = 0;
        _details.setLayout(gbLayout);
        int i = 0;

        boolean incAdvancedOptions = false;
        for (String item : options.keySet()) {
            if (options.get(item).isAdvanced()) {
                incAdvancedOptions = true;
            }
        }

        i = addStandardDetails(adapter, incAdvancedOptions, i);

        if (showAdvanced.isSelected()) {
            for (String item : options.keySet()) {
                if (options.get(item).isAdvanced()) {
                    cR.gridy = i;
                    cL.gridy = i;
                    gbLayout.setConstraints(options.get(item).getLabel(), cL);
                    gbLayout.setConstraints(options.get(item).getComponent(), cR);
                    _details.add(options.get(item).getLabel());
                    _details.add(options.get(item).getComponent());
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

    public String getManufacturer() {
        return adapter.getManufacturer();
    }

    public void setManufacturer(String manufacturer) {
        adapter.setManufacturer(manufacturer);
    }

    public String getConnectionName() {
        if (adapter.getSystemConnectionMemo() != null) {
            return adapter.getSystemConnectionMemo().getUserName();
        } else {
            return null;
        }
    }

    public boolean getDisabled() {
        if (adapter == null) {
            return true;
        }
        return adapter.getDisabled();
    }

    public void setDisabled(boolean disabled) {
        adapter.setDisabled(disabled);
    }

    public void dispose() {
        super.dispose();
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractSimulatorConnectionConfig.class.getName());
}
