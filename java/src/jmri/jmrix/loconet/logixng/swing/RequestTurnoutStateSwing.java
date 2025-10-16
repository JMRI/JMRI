package jmri.jmrix.loconet.logixng.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.swing.AbstractDigitalActionSwing;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.jmrix.loconet.logixng.RequestTurnoutState;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class RequestTurnoutStateSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<Turnout> _selectNamedBeanSwing;
    private JComboBox<LocoNetConnection> _locoNetConnection;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof RequestTurnoutState)) {
            throw new IllegalArgumentException("object must be an RequestTurnoutState but is a: "+object.getClass().getName());
        }

        RequestTurnoutState action = (RequestTurnoutState)object;

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(TurnoutManager.class), getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel _tabbedPaneNamedBean;

        if (action != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }

        panel.add(_tabbedPaneNamedBean);

        JPanel locoNetPanel = new JPanel();
        locoNetPanel.add(new JLabel(Bundle.getMessage("LocoNetConnection")));

        _locoNetConnection = new JComboBox<>();
        List<LocoNetSystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(LocoNetSystemConnectionMemo.class);
        for (LocoNetSystemConnectionMemo connection : systemConnections) {
            LocoNetConnection c = new LocoNetConnection(connection);
            _locoNetConnection.addItem(c);
            if ((action != null) && (action.getMemo() == connection)) {
                _locoNetConnection.setSelectedItem(c);
            }
        }
        locoNetPanel.add(_locoNetConnection);

        panel.add(locoNetPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        LocoNetSystemConnectionMemo memo =
                _locoNetConnection.getItemAt(_locoNetConnection.getSelectedIndex())._memo;

        // Create a temporary action to test formula
        RequestTurnoutState action = new RequestTurnoutState("IQDA1", null, memo);

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        LocoNetSystemConnectionMemo memo =
                _locoNetConnection.getItemAt(_locoNetConnection.getSelectedIndex())._memo;

        RequestTurnoutState action = new RequestTurnoutState(systemName, userName, memo);
        updateObject(action);

        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof RequestTurnoutState)) {
            throw new IllegalArgumentException("object must be an ExpressionTurnout but is a: "+object.getClass().getName());
        }

        RequestTurnoutState action = (RequestTurnoutState)object;

        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());

        action.setMemo(_locoNetConnection.getItemAt(_locoNetConnection.getSelectedIndex())._memo);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("RequestTurnoutState_Short");
    }

    @Override
    public void dispose() {
        _selectNamedBeanSwing.dispose();
    }



    private static class LocoNetConnection {

        private LocoNetSystemConnectionMemo _memo;

        public LocoNetConnection(LocoNetSystemConnectionMemo memo) {
            _memo = memo;
        }

        @Override
        public String toString() {
            return _memo.getUserName();
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequestTurnoutStateSwing.class);

}
