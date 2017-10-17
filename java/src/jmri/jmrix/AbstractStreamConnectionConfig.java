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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for common implementation of the Stream Port
 * ConnectionConfig
 *
 * @author Kevin Dickerson Copyright (C) 2001, 2003
 */
abstract public class AbstractStreamConnectionConfig extends AbstractConnectionConfig {

    /**
     * Ctor for an object being created during load process.
     */
    public AbstractStreamConnectionConfig(jmri.jmrix.AbstractStreamPortController p) {
        adapter = p;
    }

    @Override
    public jmri.jmrix.AbstractStreamPortController getAdapter() {
        return adapter;
    }

    /**
     * Ctor for a functional object with no prexisting adapter. Expect that the
     * subclass setInstance() will fill the adapter member.
     */
    public AbstractStreamConnectionConfig() {
        adapter = null;
    }

    protected boolean init = false;

    @SuppressWarnings("unchecked")
    @Override
    protected void checkInitDone() {
        if (log.isDebugEnabled()) {
            log.debug("init called for " + name());
        }
        if (init) {
            return;
        }

        if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
                    }
                }
            });
            systemPrefixField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
                        JOptionPane.showMessageDialog(null, "System Prefix " + systemPrefixField.getText() + " is already assigned");
                        systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
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
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }
            });
            connectionNameField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
                        JOptionPane.showMessageDialog(null, "Connection Name " + connectionNameField.getText() + " is already assigned");
                        connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                }
            });
            for (String i : options.keySet()) {
                final String item = i;
                if (options.get(i).getComponent() instanceof JComboBox) {
                    ((JComboBox<?>) options.get(i).getComponent()).addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            adapter.setOptionState(item, options.get(item).getItem());
                        }
                    });
                }
            }

        }
        init = true;
    }

    @Override
    public void updateAdapter() {
        for (String i : options.keySet()) {
            adapter.setOptionState(i, options.get(i).getItem());
        }

        if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    protected jmri.jmrix.AbstractStreamPortController adapter = null;

    /**
     * Load the adapter with an appropriate object
     * <i>unless</I> its already been set.
     */
    @Override
    abstract protected void setInstance();

    /**
     * {@inheritDoc}
     * <p>
     * This implementation always returns the localized value for "none".
     *
     * @return the localized value for "none"
     */
    @Override
    public String getInfo() {
        return Bundle.getMessage("none");
    }

    @Override
    public void loadDetails(final JPanel details) {
        _details = details;
        setInstance();
        if (!init) {
            String[] optionsAvailable = adapter.getOptions();
            options.clear();
            for (String i : optionsAvailable) {
                JComboBox<String> opt = new JComboBox<String>(adapter.getOptionChoices(i));
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

        if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
        NUMOPTIONS = NUMOPTIONS + options.size();

        showAdvanced.setFont(showAdvanced.getFont().deriveFont(9f));
        showAdvanced.setForeground(Color.blue);
        showAdvanced.addItemListener(new ItemListener() {
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

    @Override
    public String getManufacturer() {
        return adapter.getManufacturer();
    }

    @Override
    public void setManufacturer(String manufacturer) {
        adapter.setManufacturer(manufacturer);
    }

    @Override
    public String getConnectionName() {
        if (adapter.getSystemConnectionMemo() != null) {
            return adapter.getSystemConnectionMemo().getUserName();
        } else {
            return null;
        }
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
        adapter.setDisabled(disabled);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (adapter != null) {
            adapter.dispose();
            adapter = null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractStreamConnectionConfig.class);
}
