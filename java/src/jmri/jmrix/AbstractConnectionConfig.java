package jmri.jmrix;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import jmri.InstanceManager;

/**
 * Abstract base class for common implementation of the ConnectionConfig
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
abstract public class AbstractConnectionConfig implements ConnectionConfig {

    /**
     * Ctor for a functional object with no prexisting adapter. Expect that the
     * subclass setInstance() will fill the adapter member.
     */
    public AbstractConnectionConfig() {
        try {
            // systemPrefixField = new JFormattedTextField(new jmri.util.swing.RegexFormatter("[A-Za-z]\\d*"));
            systemPrefixField = new JFormattedTextField(new SystemPrefixFormatter());
            
            systemPrefixField.setPreferredSize(new JTextField("P123").getPreferredSize());
            systemPrefixField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        } catch (java.util.regex.PatternSyntaxException e) {
            log.error("unexpected parse exception during setup", e);
        }
    }

    static public class SystemPrefixFormatter extends javax.swing.text.DefaultFormatter {
        public Object stringToValue(String text) throws java.text.ParseException {
            try {
                if (jmri.Manager.getSystemPrefixLength(text)!= text.length()) {
                    throw new java.text.ParseException("Pattern did not match", 0);
                }
            } catch (jmri.NamedBean.BadSystemNameException e) {
                throw new java.text.ParseException("Pattern did not match", 0);
            }
            return text;
        }
    }

    abstract protected void checkInitDone();

    abstract public void updateAdapter();

    protected int NUMOPTIONS = 2;

    // Load localized field names
    protected JCheckBox showAdvanced = new JCheckBox(Bundle.getMessage("AdditionalConnectionSettings"));
    protected JLabel systemPrefixLabel = new JLabel(Bundle.getMessage("ConnectionPrefix"));
    protected JLabel connectionNameLabel = new JLabel(Bundle.getMessage("ConnectionName"));
    protected JFormattedTextField systemPrefixField;
    protected JTextField connectionNameField = new JTextField(15);

    protected JPanel _details = null;

    protected final HashMap<String, Option> options = new HashMap<>();

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
        return (this.getAdapter() != null) ? this.getAdapter().isDirty() : true;
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
        return (this.getAdapter() != null) ? this.getAdapter().isRestartRequired() : true;
    }

    protected static class Option {

        String optionDisplayName;
        JComponent optionSelection;
        Boolean advanced = true;
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
     * <i>unless</I> its already been set.
     */
    abstract protected void setInstance();

    @Override
    abstract public String getInfo();

    protected ArrayList<JComponent> additionalItems = new ArrayList<>(0);

    /**
     * Load the Swing widgets needed to configure this connection into a
     * specified JPanel. Used during the configuration process to fill out the
     * preferences window with content specific to this Connection type. The
     * JPanel contents need to handle their own gets/sets to the underlying
     * Connection content.
     *
     * @param details The specific Swing object to be configured and filled.
     */
    @Override
    abstract public void loadDetails(final JPanel details);

    protected GridBagLayout gbLayout = new GridBagLayout();
    protected GridBagConstraints cL = new GridBagConstraints();
    protected GridBagConstraints cR = new GridBagConstraints();

    abstract protected void showAdvancedItems();

    protected int addStandardDetails(PortAdapter adapter, boolean incAdvanced, int i) {
        for (String item : options.keySet()) {
            if (!options.get(item).isAdvanced()) {
                cR.gridy = i;
                cL.gridy = i;
                gbLayout.setConstraints(options.get(item).getLabel(), cL);
                gbLayout.setConstraints(options.get(item).getComponent(), cR);
                _details.add(options.get(item).getLabel());
                _details.add(options.get(item).getComponent());
                i++;
            }
        }

        if (adapter.getSystemConnectionMemo() != null) {
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(systemPrefixLabel, cL);
            gbLayout.setConstraints(systemPrefixField, cR);
            _details.add(systemPrefixLabel);
            _details.add(systemPrefixField);
            i++;
            cR.gridy = i;
            cL.gridy = i;
            gbLayout.setConstraints(connectionNameLabel, cL);
            gbLayout.setConstraints(connectionNameField, cR);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractThrottle.class);
}
