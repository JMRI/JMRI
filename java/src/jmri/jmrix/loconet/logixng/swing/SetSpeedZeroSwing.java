package jmri.jmrix.loconet.logixng.swing;

import java.awt.Color;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.swing.AbstractDigitalActionSwing;
import jmri.jmrix.loconet.logixng.SetSpeedZero;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;

/**
 * Configures an ExpressionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class SetSpeedZeroSwing extends AbstractDigitalActionSwing {

    private JComboBox<LocoNetConnection> _locoNetConnection;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof SetSpeedZero)) {
            throw new IllegalArgumentException("object must be an SetSpeedZero but is a: "+object.getClass().getName());
        }

        SetSpeedZero action = (SetSpeedZero)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(new JLabel(Bundle.getMessage("SetSpeedZeroInfo1")));
        panel.add(new JLabel(Bundle.getMessage("SetSpeedZeroInfo2")));
        panel.add(new JLabel(Bundle.getMessage("SetSpeedZeroInfo3")));
        panel.add(new JLabel(Bundle.getMessage("SetSpeedZeroInfo4")));
        panel.add(new JLabel(Bundle.getMessage("SetSpeedZeroInfo5")));

        JPanel queryPanel = new JPanel();
        queryPanel.setBorder(BorderFactory.createLineBorder(Color.black));

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
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        LocoNetSystemConnectionMemo memo =
                _locoNetConnection.getItemAt(_locoNetConnection.getSelectedIndex())._memo;

        SetSpeedZero action = new SetSpeedZero(systemName, userName, memo);
        updateObject(action);

        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof SetSpeedZero)) {
            throw new IllegalArgumentException("object must be an ExpressionTurnout but is a: "+object.getClass().getName());
        }

        SetSpeedZero action = (SetSpeedZero)object;

        action.setMemo(_locoNetConnection.getItemAt(_locoNetConnection.getSelectedIndex())._memo);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("SetSpeedZero_Short");
    }

    @Override
    public void dispose() {
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SetSpeedZeroSwing.class);

}
