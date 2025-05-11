package jmri.jmrix.can.cbus.logixng.swing;

import jmri.jmrit.logixng.actions.swing.*;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrix.can.cbus.logixng.SendMergCbusEvent;
import jmri.jmrix.can.cbus.logixng.SendMergCbusEvent.CbusEventType;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectIntegerSwing;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.logixng.CategoryMergCbus;

/**
 * Configures an SendCbusEvent object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class SendMergCbusEventSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectIntegerSwing _selectNodeNumberSwing;
    private LogixNG_SelectIntegerSwing _selectEventNumberSwing;
    private LogixNG_SelectEnumSwing<CbusEventType> _selectEventTypeSwing;
    private JComboBox<CbusConnection> _cbusConnection;


    public SendMergCbusEventSwing() {
    }

    public SendMergCbusEventSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        SendMergCbusEvent action = (SendMergCbusEvent) object;
        if (action == null) {
            // Create a temporary action
            action = new SendMergCbusEvent("IQDA1", null, null);
        }

        _selectNodeNumberSwing = new LogixNG_SelectIntegerSwing(getJDialog(), this);
        _selectEventNumberSwing = new LogixNG_SelectIntegerSwing(getJDialog(), this);
        _selectEventTypeSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

        panel = new JPanel(new java.awt.GridBagLayout());
        JPanel tabbedPaneNodeNumber;
        JPanel tabbedPaneEventNumber;
        JPanel tabbedPaneCbusEventType;

        tabbedPaneNodeNumber = _selectNodeNumberSwing.createPanel(action.getSelectNodeNumber());
        tabbedPaneEventNumber = _selectEventNumberSwing.createPanel(action.getSelectEventNumber());
        tabbedPaneCbusEventType = _selectEventTypeSwing.createPanel(action.getSelectEventType(), CbusEventType.values());


        _cbusConnection = new JComboBox<>();
        List<CanSystemConnectionMemo> systemConnections = CategoryMergCbus.getMergConnections();
        for (CanSystemConnectionMemo connection : systemConnections) {
            CbusConnection c = new CbusConnection(connection);
            _cbusConnection.addItem(c);
            if (action.getMemo() == connection) {
                _cbusConnection.setSelectedItem(c);
            }
        }


        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.EAST;
        panel.add(new JLabel(Bundle.getMessage("SendCbusEventSwing_NodeNumber")), constraints);
        constraints.gridy = 1;
        panel.add(new JLabel(Bundle.getMessage("SendCbusEventSwing_EventNumber")), constraints);
        constraints.gridy = 2;
        panel.add(new JLabel(Bundle.getMessage("SendCbusEventSwing_EventType")), constraints);
        constraints.gridy = 3;
        panel.add(new JLabel(Bundle.getMessage("SendCbusEventSwing_Memo")), constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
        panel.add(tabbedPaneNodeNumber, constraints);
        constraints.gridy = 1;
        panel.add(tabbedPaneEventNumber, constraints);
        constraints.gridy = 2;
        panel.add(tabbedPaneCbusEventType, constraints);
        constraints.gridy = 3;
        panel.add(_cbusConnection, constraints);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        SendMergCbusEvent action = new SendMergCbusEvent("IQDA1", null, null);

        _selectNodeNumberSwing.validate(action.getSelectNodeNumber(), errorMessages);
        _selectEventNumberSwing.validate(action.getSelectEventNumber(), errorMessages);
        _selectEventTypeSwing.validate(action.getSelectEventType(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        SendMergCbusEvent action = new SendMergCbusEvent(systemName, userName, null);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof SendMergCbusEvent)) {
            throw new IllegalArgumentException("object must be an SendCbusEvent but is a: "+object.getClass().getName());
        }
        SendMergCbusEvent action = (SendMergCbusEvent) object;

        _selectNodeNumberSwing.updateObject(action.getSelectNodeNumber());
        _selectEventNumberSwing.updateObject(action.getSelectEventNumber());
        _selectEventTypeSwing.updateObject(action.getSelectEventType());

        action.setMemo(_cbusConnection.getItemAt(_cbusConnection.getSelectedIndex())._memo);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("SendCbusEvent_Short");
    }

    @Override
    public void dispose() {
        _selectEventTypeSwing.dispose();
        _selectNodeNumberSwing.dispose();
    }


    private static class CbusConnection {

        private CanSystemConnectionMemo _memo;

        public CbusConnection(CanSystemConnectionMemo memo) {
            _memo = memo;
        }

        @Override
        public String toString() {
            return _memo.getUserName();
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SendCbusEventSwing.class);

}
