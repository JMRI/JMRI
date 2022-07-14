package jmri.jmrit.logixng.expressions.swing;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.awt.Dimension;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.ConnectionName;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectStringSwing;
import jmri.jmrix.*;

/**
 * Configures an ConnectionName object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ConnectionNameSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectStringSwing _selectConnectionNameSwing;

    public ConnectionNameSwing() {
    }

    public ConnectionNameSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ConnectionName action = (ConnectionName) object;
        if (action == null) {
            // Create a temporary action
            action = new ConnectionName("IQDE1", null);
        }

        _selectConnectionNameSwing = new LogixNG_SelectStringSwing(getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel tabbedPaneTopic = _selectConnectionNameSwing.createPanel(action.getSelectConnectionName());
        panel.add(tabbedPaneTopic);


        ConnectionConfigManager manager = InstanceManager.getDefault(ConnectionConfigManager.class);

        JPanel connections = new JPanel();

        JPanel currentConnections = new JPanel();
        currentConnections.setLayout(new BoxLayout(currentConnections, BoxLayout.Y_AXIS));

        currentConnections.add(new JLabel("Current connections"));
        StringBuilder sb = new StringBuilder();
        JTextArea currentConnectionsTextBox = new JTextArea();
        currentConnectionsTextBox.setEditable(false);
        JScrollPane connectionsScrollPane = new JScrollPane(currentConnectionsTextBox);
        connectionsScrollPane.setPreferredSize(new Dimension(400, 300));
        currentConnections.add(connectionsScrollPane);
        for (ConnectionConfig cc : manager.getConnections()) {
            sb.append(cc.name());
            sb.append('\n');
        }
        currentConnectionsTextBox.setText(sb.toString());
        currentConnectionsTextBox.setCaretPosition(0);
        connections.add(currentConnections);


        JPanel availableConnections = new JPanel();
        availableConnections.setLayout(new BoxLayout(availableConnections, BoxLayout.Y_AXIS));

        availableConnections.add(new JLabel("Available connections"));
        sb = new StringBuilder();
        JTextArea availableConnectionsTextBox = new JTextArea();
        availableConnectionsTextBox.setEditable(false);
        JScrollPane availableConnectionsScrollPane = new JScrollPane(availableConnectionsTextBox);
        availableConnectionsScrollPane.setPreferredSize(new Dimension(400, 300));
        availableConnections.add(availableConnectionsScrollPane);

        String[] manufactureNameList = manager.getConnectionManufacturers();
        for (String manuName : manufactureNameList) {
            if (sb.length() != 0) {
                sb.append("\n===============================\n\n");
            }

            sb.append(manuName);
            sb.append("\n-------------------------------\n");

            String[] classConnectionNameList;
            classConnectionNameList = manager.getConnectionTypes(manuName);

            for (String className : classConnectionNameList) {
                try {
                    ConnectionConfig config;
                    Class<?> cl = Class.forName(className);
                    config = (ConnectionConfig) cl.getDeclaredConstructor().newInstance();
                    if( !(config instanceof StreamConnectionConfig)) {
                        // only include if the connection is not a
                        // StreamConnection.  Those connections require
                        // additional context.
                        if (config != null) {
                            sb.append(config.name());
                            sb.append('\n');
                        }
                    }
                } catch (NullPointerException e) {
                    log.error("Attempt to load {} failed.", className, e);
                } catch (InvocationTargetException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                    log.error("Attempt to load {} failed", className, e);
                }
            }
        }

        for (ConnectionConfig cc : manager.getConnections()) {
            sb.append(cc.name());
            sb.append('\n');
        }
        availableConnectionsTextBox.setText(sb.toString());
        availableConnectionsTextBox.setCaretPosition(0);
        connections.add(availableConnections);


        panel.add(connections);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ConnectionName action = new ConnectionName("IQDE1", null);

        _selectConnectionNameSwing.validate(action.getSelectConnectionName(), errorMessages);

        return errorMessages.isEmpty();
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

        _selectConnectionNameSwing.updateObject(action.getSelectConnectionName());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ConnectionName_Short");
    }

    @Override
    public void dispose() {
        _selectConnectionNameSwing.dispose();
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionNameSwing.class);

}
