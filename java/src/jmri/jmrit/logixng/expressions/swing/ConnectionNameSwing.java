package jmri.jmrit.logixng.expressions.swing;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.ConnectionName;
import jmri.jmrix.*;
import static jmri.jmrix.JmrixConfigPane.NONE_SELECTED;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ConnectionName object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ConnectionNameSwing extends AbstractDigitalExpressionSwing {

    private final ConnectionConfigManager manager =
            InstanceManager.getDefault(ConnectionConfigManager.class);

    private JComboBox<String> _manufacturerComboBox;
    private JComboBox<String> _connectionComboBox;
    private ConnectionConfig[] _connectionConfigs;

    public ConnectionNameSwing() {
    }

    public ConnectionNameSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        // Create a temporary action if object is null
        final ConnectionName action = (object != null)
                ? (ConnectionName) object
                : new ConnectionName("IQDE1", null);

        String[] manufactureNameList = manager.getConnectionManufacturers();
        _connectionConfigs = manager.getConnections();

        panel = new JPanel();

        String selectedManufacturer = action.getManufacturer();
        if (selectedManufacturer == null
                || selectedManufacturer.isBlank()
                || NONE_SELECTED.equals(selectedManufacturer)) {
            if (_connectionConfigs.length > 0) {
                selectedManufacturer = _connectionConfigs[0].getManufacturer();
            }
        }

        JPanel manufacturerPanel = new JPanel();
        manufacturerPanel.setLayout(new BoxLayout(manufacturerPanel, BoxLayout.Y_AXIS));

        var mLabel = new JPanel();
        mLabel.add(new JLabel(Bundle.getMessage("ExpressionConnection_Manufacturer")));
        manufacturerPanel.add(mLabel);

        _manufacturerComboBox = new JComboBox<>();
        manufacturerPanel.add(_manufacturerComboBox);

        for (String manuName : manufactureNameList) {
            _manufacturerComboBox.addItem(manuName);
            if (manuName.equals(selectedManufacturer)) {
                _manufacturerComboBox.setSelectedItem(selectedManufacturer);
            }
        }
        if (_manufacturerComboBox.getSelectedIndex() == -1) {
            _manufacturerComboBox.setSelectedIndex(0);
        }

        JPanel connectionNamePanel = new JPanel();
        connectionNamePanel.setLayout(new BoxLayout(connectionNamePanel, BoxLayout.Y_AXIS));

        var cLabel = new JPanel();
        cLabel.add(new JLabel(Bundle.getMessage("ExpressionConnection_Connection")));
        connectionNamePanel.add(cLabel);

        _connectionComboBox = new JComboBox<>();
        connectionNamePanel.add(_connectionComboBox);

        _manufacturerComboBox.addActionListener((evt) -> {
            updateConnectionComboBox(action);
        });

        updateConnectionComboBox(action);

        JComboBoxUtil.setupComboBoxMaxRows(_manufacturerComboBox);
        JComboBoxUtil.setupComboBoxMaxRows(_connectionComboBox);


        panel.add(manufacturerPanel);
        panel.add(connectionNamePanel);
    }

    private void updateConnectionComboBox(ConnectionName action) {
        String selectedManufacturer = (String) _manufacturerComboBox.getSelectedItem();
        String[] classConnectionNameList = manager.getConnectionTypes(selectedManufacturer);

        String selectedConnectionName = null;

        if (selectedManufacturer.equals(action.getManufacturer())) {
            selectedConnectionName = action.getConnectionName();
        }
        if (selectedConnectionName == null
                || selectedConnectionName.isBlank()
                || NONE_SELECTED.equals(selectedConnectionName)) {
            for (int i=0; i < _connectionConfigs.length; i++) {
                if (_connectionConfigs[i].getManufacturer().equals(selectedManufacturer)) {
                    selectedConnectionName = _connectionConfigs[i].name();
                }
            }
        }

        _connectionComboBox.removeAllItems();
        _connectionComboBox.addItem(NONE_SELECTED);

        for (String className : classConnectionNameList) {
            try {
                Class<?> cl = Class.forName(className);
                ConnectionConfig config = (ConnectionConfig) cl.getDeclaredConstructor().newInstance();
                if( !(config instanceof StreamConnectionConfig)) {
                    // only include if the connection is not a
                    // StreamConnection.  Those connections require
                    // additional context.
                    if (config != null) {
                        _connectionComboBox.addItem(config.name());
                        if (config.name().equals(selectedConnectionName)) {
                            _connectionComboBox.setSelectedItem(selectedConnectionName);
                        }
                    }
                }
            } catch (NullPointerException e) {
                log.error("Attempt to load {} failed.", className, e);
            } catch (InvocationTargetException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                log.error("Attempt to load {} failed", className, e);
            }
        }

        // Resize dialog if needed
        JDialog dialog = (JDialog) SwingUtilities.getAncestorOfClass(JDialog.class, _manufacturerComboBox);
        if (dialog != null) dialog.pack();
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ConnectionName action = new ConnectionName(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ConnectionName)) {
            throw new IllegalArgumentException("object must be an ConnectionName but is a: "+object.getClass().getName());
        }
        ConnectionName action = (ConnectionName) object;

        if (_manufacturerComboBox.getSelectedIndex() != -1) {
            action.setManufacturer(_manufacturerComboBox.getItemAt(_manufacturerComboBox.getSelectedIndex()));
        }
        if (_connectionComboBox.getSelectedIndex() != -1) {
            action.setConnectionName(_connectionComboBox.getItemAt(_connectionComboBox.getSelectedIndex()));
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ConnectionName_Short");
    }

    @Override
    public void dispose() {
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionNameSwing.class);

}
