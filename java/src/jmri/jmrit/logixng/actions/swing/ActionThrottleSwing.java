package jmri.jmrit.logixng.actions.swing;

import java.awt.Color;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.ActionThrottle;

/**
 * Configures an ActionThrottle object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionThrottleSwing extends AbstractDigitalActionSwing {

    private JComboBox<Connection> _connection;
    private JCheckBox _stopLocoWhenSwitchingLoco;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ActionThrottle)) {
            throw new IllegalArgumentException("object must be an ActionThrottle but is a: "+object.getClass().getName());
        }

        ActionThrottle action = (ActionThrottle)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel queryPanel = new JPanel();
        queryPanel.setBorder(BorderFactory.createLineBorder(Color.black));

        JPanel connectionPanel = new JPanel();
        connectionPanel.add(new JLabel(Bundle.getMessage("ActionThrottleSwing_Connection")));

        _connection = new JComboBox<>();
        _connection.addItem(new Connection(null));
        List<SystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(SystemConnectionMemo.class);
        for (SystemConnectionMemo connection : systemConnections) {
            if (!connection.provides(ThrottleManager.class)) continue;
            Connection c = new Connection(connection);
            _connection.addItem(c);
            if ((action != null) && (action.getMemo() == connection)) {
                _connection.setSelectedItem(c);
            }
        }
        connectionPanel.add(_connection);
        panel.add(connectionPanel);

        _stopLocoWhenSwitchingLoco = new JCheckBox(Bundle.getMessage("ActionThrottleSwing_StopLocoWhenSwitchingLoco"));
        if (action != null) {
            _stopLocoWhenSwitchingLoco.setSelected(action.isStopLocoWhenSwitchingLoco());
        } else {
            _stopLocoWhenSwitchingLoco.setSelected(true);
        }
        panel.add(_stopLocoWhenSwitchingLoco);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionThrottle action = new ActionThrottle(systemName, userName);
        updateObject(action);

        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionThrottle)) {
            throw new IllegalArgumentException("object must be an ActionThrottle but is a: "+object.getClass().getName());
        }

        ActionThrottle action = (ActionThrottle)object;

        action.setMemo(_connection.getItemAt(_connection.getSelectedIndex())._memo);
        action.setStopLocoWhenSwitchingLoco(_stopLocoWhenSwitchingLoco.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionThrottle_Short");
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
                return Bundle.getMessage("ActionThrottleSwing_DefaultConnection");
            }
            return _memo.getUserName();
        }
    }

}
