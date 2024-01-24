package jmri.jmrit.logixng.actions.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ActionRequestUpdateAllSensors;

/**
 * Configures an ActionRequestUpdateAllSensors object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionRequestUpdateAllSensorsSwing extends AbstractDigitalActionSwing {

    private JComboBox<Connection> _connection;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ActionRequestUpdateAllSensors)) {
            throw new IllegalArgumentException("object must be an ActionRequestUpdateAllSensors but is a: "+object.getClass().getName());
        }

        ActionRequestUpdateAllSensors action = (ActionRequestUpdateAllSensors)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel connectionPanel = new JPanel();
        connectionPanel.add(new JLabel(Bundle.getMessage("ActionRequestUpdateAllSensorsSwing_Connection")));

        _connection = new JComboBox<>();
        _connection.addItem(new Connection(null));
        List<SystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(SystemConnectionMemo.class);
        for (SystemConnectionMemo connection : systemConnections) {
            if (!connection.provides(SensorManager.class)) continue;
            Connection c = new Connection(connection);
            _connection.addItem(c);
            if ((action != null) && (action.getMemo() == connection)) {
                _connection.setSelectedItem(c);
            }
        }
        connectionPanel.add(_connection);

        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel(Bundle.getMessage("ActionRequestUpdateAllSensorsSwing_Info")));

        panel.add(connectionPanel);
        panel.add(infoPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        SystemConnectionMemo memo = _connection.getItemAt(_connection.getSelectedIndex())._memo;

        ActionRequestUpdateAllSensors action = new ActionRequestUpdateAllSensors(systemName, userName, memo);
        updateObject(action);

        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionRequestUpdateAllSensors)) {
            throw new IllegalArgumentException("object must be an ActionRequestUpdateAllSensors but is a: "+object.getClass().getName());
        }

        ActionRequestUpdateAllSensors action = (ActionRequestUpdateAllSensors)object;

        action.setMemo(_connection.getItemAt(_connection.getSelectedIndex())._memo);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionRequestUpdateAllSensors_Short");
    }

    @Override
    public void dispose() {
    }



    private static class Connection {

        private SystemConnectionMemo _memo;

        public Connection(SystemConnectionMemo memo) {
            _memo = memo;
        }

        @Override
        public String toString() {
            if (_memo == null) {
                return Bundle.getMessage("ActionRequestUpdateAllSensorsSwing_AllConnections");
            }
            return _memo.getUserName();
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionRequestUpdateAllSensorsSwing.class);

}
