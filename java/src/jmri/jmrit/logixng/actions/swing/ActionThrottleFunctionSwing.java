package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionThrottleFunction;
import jmri.jmrit.logixng.actions.ActionThrottleFunction.FunctionState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectIntegerSwing;

/**
 * Configures an ActionThrottleFunction object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2023
 */
public class ActionThrottleFunctionSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectIntegerSwing _selectAddressSwing;
    private LogixNG_SelectIntegerSwing _selectFunctionSwing;
    private LogixNG_SelectEnumSwing<FunctionState> _selectOnOffSwing;
    private JComboBox<Connection> _connection;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionThrottleFunction action = (ActionThrottleFunction)object;
        if (action == null) {
            // Create a temporary action
            action = new ActionThrottleFunction("IQDA1", null);
        }

        _selectAddressSwing = new LogixNG_SelectIntegerSwing(getJDialog(), this);
        _selectFunctionSwing = new LogixNG_SelectIntegerSwing(getJDialog(), this);
        _selectOnOffSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel selectPanel = new JPanel();
        JPanel _tabbedPaneAddress;
        JPanel _tabbedPaneFunction;
        JPanel _tabbedPaneOnOff;

        _tabbedPaneAddress = _selectAddressSwing.createPanel(action.getSelectAddress());
        _tabbedPaneFunction = _selectFunctionSwing.createPanel(action.getSelectFunction());
        _tabbedPaneOnOff = _selectOnOffSwing.createPanel(action.getSelectOnOff(), FunctionState.values());

        JComponent[] components = new JComponent[]{
            _tabbedPaneAddress,
            _tabbedPaneFunction,
            _tabbedPaneOnOff};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionThrottleFunction_Components"), components);

        for (JComponent c : componentList) selectPanel.add(c);

        JPanel connectionPanel = new JPanel();
        connectionPanel.add(new JLabel(Bundle.getMessage("ActionThrottleFunctionSwing_Connection")));

        _connection = new JComboBox<>();
        _connection.addItem(new Connection(null));
        List<SystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(SystemConnectionMemo.class);
        for (SystemConnectionMemo connection : systemConnections) {
            if (!connection.provides(ThrottleManager.class)) continue;
            Connection c = new Connection(connection);
            _connection.addItem(c);
            if ((object != null) && (action.getMemo() == connection)) {
                _connection.setSelectedItem(c);
            }
        }
        connectionPanel.add(_connection);

        panel.add(selectPanel);
        panel.add(connectionPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionThrottleFunction action = new ActionThrottleFunction("IQDA1", null);

        _selectAddressSwing.validate(action.getSelectAddress(), errorMessages);
        _selectFunctionSwing.validate(action.getSelectFunction(), errorMessages);
        _selectOnOffSwing.validate(action.getSelectOnOff(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionThrottleFunction action = new ActionThrottleFunction(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionThrottleFunction)) {
            throw new IllegalArgumentException("object must be an ActionThrottleFunction but is a: "+object.getClass().getName());
        }
        ActionThrottleFunction action = (ActionThrottleFunction)object;
        _selectAddressSwing.updateObject(action.getSelectAddress());
        _selectFunctionSwing.updateObject(action.getSelectFunction());
        _selectOnOffSwing.updateObject(action.getSelectOnOff());

        action.setMemo(_connection.getItemAt(_connection.getSelectedIndex())._memo);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionThrottleFunction_Short");
    }

    @Override
    public void dispose() {
        _selectAddressSwing.dispose();
        _selectFunctionSwing.dispose();
        _selectOnOffSwing.dispose();
    }



    private static class Connection {

        private SystemConnectionMemo _memo;

        public Connection(SystemConnectionMemo memo) {
            _memo = memo;
        }

        @Override
        public String toString() {
            if (_memo == null) {
                return Bundle.getMessage("ActionThrottleFunctionSwing_DefaultConnection");
            }
            return _memo.getUserName();
        }
    }

}
