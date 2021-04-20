package jmri.jmrix;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.util.swing.ValidatedTextField;

/**
 * Abstract base class for common implementation of the ConnectionConfig.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
abstract public class AbstractConnectionConfig implements ConnectionConfig {

    /**
     * Ctor for a functional object with no preexisting adapter. Expect that the
     * subclass setInstance() will fill the adapter member.
     * {@link AbstractConnectionConfigXml}loadCommon
     */
    public AbstractConnectionConfig() {
        try {
            systemPrefixField = new ValidatedTextField(4,
                    true,
                    "[A-Za-z]\\d*",
                    Bundle.getMessage("TipPrefixFormat"));
            // see the "Prefix Needs Migration" dialog in jmri.jmrix.configurexml.AbstractConnectionConfigXml#loadCommon
        } catch (java.util.regex.PatternSyntaxException e) {
            log.error("Prefix unexpected parse exception during setup", e);
        }
    }

    /**
     * Complete connection adapter initialization, adding desired options to the
     * Connection Configuration pane. Required action: set init to true.
     * Optional actions:
     * <ul>
     *     <li>fill in connectionNameField</li>
     *     <li>add ActionListeners to config fields eg. systemPrefixField to update adapter after change by the user</li>
     * </ul>
     */
    abstract protected void checkInitDone();

    abstract public void updateAdapter();

    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "Field used by implementing classes")
    protected int NUMOPTIONS = 2;

    // Load localized field names
    protected JCheckBox showAdvanced = new JCheckBox(Bundle.getMessage("AdditionalConnectionSettings"));
    protected JLabel systemPrefixLabel = new JLabel(Bundle.getMessage("ConnectionPrefix"));
    protected JLabel connectionNameLabel = new JLabel(Bundle.getMessage("ConnectionName"));
    protected ValidatedTextField systemPrefixField;
    protected JTextField connectionNameField = new JTextField(15);

    protected JPanel _details = null;

    protected final Map<String, Option> options = new TreeMap<>();

    /**
     * Determine if configuration needs to be written to disk.
     * <p>
     * This default implementation always returns true to maintain the existing
     * behavior.
     *
     * @return true if configuration need to be saved, false otherwise
     */
    @Override
    public boolean isDirty() {
        return (this.getAdapter() == null || this.getAdapter().isDirty());
    }

    /**
     * Determine if application needs to be restarted for configuration changes
     * to be applied.
     * <p>
     * The default implementation always returns true to maintain the existing
     * behavior.
     *
     * @return true if application needs to restart, false otherwise
     */
    @Override
    public boolean isRestartRequired() {
        return (this.getAdapter() == null || this.getAdapter().isRestartRequired());
    }

    protected static class Option {

        String optionDisplayName;
        JComponent optionSelection;
        Boolean advanced;
        JLabel label = null;

        public Option(String name, JComponent optionSelection, Boolean advanced) {
            this.optionDisplayName = name;
            this.optionSelection = optionSelection;
            this.advanced = advanced;
        }

        protected String getDisplayName() {
            return optionDisplayName;
        }

        public JLabel getLabel() {
            if (label == null) {
                label = new JLabel(getDisplayName(), JLabel.LEFT);
            }
            return label;
        }

        public JComponent getComponent() {
            return optionSelection;
        }

        protected Boolean isAdvanced() {
            return advanced;
        }

        protected void setAdvanced(Boolean boo) {
            advanced = boo;
        }

        @SuppressWarnings("unchecked")
        public String getItem() {
            if (optionSelection instanceof JComboBox) {
                return (String) ((JComboBox<String>) optionSelection).getSelectedItem();
            } else if (optionSelection instanceof JTextField) {
                return ((JTextField) optionSelection).getText();
            }
            return null;
        }
    }

    /**
     * Load the adapter with an appropriate object
     * <i>unless</i> it's already been set.
     */
    abstract protected void setInstance();

    @Override
    abstract public String getInfo();

    @SuppressFBWarnings(value = "URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD", justification = "Field used by implementing classes")
    protected ArrayList<JComponent> additionalItems = new ArrayList<>(0);

    /**
     * Load the Swing widgets needed to configure this connection into a
     * specified JPanel. Used during the configuration process to fill out the
     * preferences window with content specific to this Connection type. The
     * JPanel contents need to handle their own gets/sets to the underlying
     * Connection content.
     *
     * @param details the specific Swing object to be configured and filled
     */
    @Override
    abstract public void loadDetails(final JPanel details);

    protected GridBagLayout gbLayout = new GridBagLayout();
    protected GridBagConstraints cL = new GridBagConstraints();
    protected GridBagConstraints cR = new GridBagConstraints();

    abstract protected void showAdvancedItems();

    protected int addStandardDetails(PortAdapter adapter, boolean incAdvanced, int i) {
        for (Map.Entry<String, Option> entry : options.entrySet()) {
            if (!entry.getValue().isAdvanced()) {
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(entry.getValue().getLabel(), cL);
                gbLayout.setConstraints(entry.getValue().getComponent(), cR);
                _details.add(entry.getValue().getLabel());
                _details.add(entry.getValue().getComponent());
                i++;
            }
        }

        if (adapter.getSystemConnectionMemo() != null) {
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(systemPrefixLabel, cL);
            gbLayout.setConstraints(systemPrefixField, cR);
            systemPrefixLabel.setLabelFor(systemPrefixField);
            _details.add(systemPrefixLabel);
            _details.add(systemPrefixField);
            systemPrefixField.setToolTipText(Bundle.getMessage("TipPrefixFormat"));
            i++;
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(connectionNameLabel, cL);
            gbLayout.setConstraints(connectionNameField, cR);
            connectionNameLabel.setLabelFor(connectionNameField);
            _details.add(connectionNameLabel);
            _details.add(connectionNameField);
            i++;
        }
        if (incAdvanced) {
            cL.gridwidth = 2;
            cL.gridy = i;
            cR.gridy = i;
            gbLayout.setConstraints(showAdvanced, cL);
            _details.add(showAdvanced);
            cL.gridwidth = 1;
            i++;
        }
        return i;
    }

    @Override
    abstract public String getManufacturer();

    @Override
    abstract public void setManufacturer(String manufacturer);

    @Override
    abstract public String getConnectionName();

    @Override
    abstract public boolean getDisabled();

    @Override
    abstract public void setDisabled(boolean disable);

    /**
     * {@inheritDoc}
     */
    @Override
    public void register() {
        this.setInstance();
        InstanceManager.getDefault(jmri.ConfigureManager.class).registerPref(this);
        ConnectionConfigManager ccm = InstanceManager.getNullableDefault(ConnectionConfigManager.class);
        if (ccm != null) {
            ccm.add(this);
        }
    }

    @Override
    public void dispose() {
        ConnectionConfigManager ccm = InstanceManager.getNullableDefault(ConnectionConfigManager.class);
        if (ccm != null) {
            ccm.remove(this);
        }
    }

    protected void addNameEntryCheckers(@Nonnull PortAdapter adapter) {
        if (adapter.getSystemConnectionMemo() != null) {
            systemPrefixField.addActionListener(e -> checkPrefixEntry(adapter));
            systemPrefixField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    checkPrefixEntry(adapter);
                }

                @Override
                public void focusGained(FocusEvent e) {
                }
            });
            connectionNameField.addActionListener(e -> checkNameEntry(adapter));
            connectionNameField.addFocusListener(new FocusListener() {
                @Override
                public void focusLost(FocusEvent e) {
                    checkNameEntry(adapter);
                }

                @Override
                public void focusGained(FocusEvent e) {
                }
            });
        }
    }

    private void checkPrefixEntry(@Nonnull PortAdapter adapter) {
        if (!systemPrefixField.isValid()) { // invalid prefix format entry, actually can't lose focus until valid
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
        }
        if (!adapter.getSystemConnectionMemo().setSystemPrefix(systemPrefixField.getText())) { // in use
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ConnectionPrefixDialog", systemPrefixField.getText()));
            systemPrefixField.setText(adapter.getSystemConnectionMemo().getSystemPrefix());
        }
    }

    private void checkNameEntry(@Nonnull PortAdapter adapter) {
        if (!adapter.getSystemConnectionMemo().setUserName(connectionNameField.getText())) {
            JOptionPane.showMessageDialog(null, Bundle.getMessage("ConnectionNameDialog", connectionNameField.getText()));
            connectionNameField.setText(adapter.getSystemConnectionMemo().getUserName());
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractConnectionConfig.class);

}
