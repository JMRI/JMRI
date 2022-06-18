package jmri.jmrix.mqtt.logixng.swing;

import jmri.jmrit.logixng.actions.swing.AbstractDigitalActionSwing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;
import jmri.jmrix.mqtt.logixng.Subscribe;

/**
 * Configures an Subscribe object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class SubscribeSwing extends AbstractDigitalActionSwing {

    private JComboBox<MqttConnection> _mqttConnection;

    JTextField _subscribeToTopicTextField;
    JTextField _lastTopicLocalVariableTextField;
    JCheckBox _removeChannelFromLastTopicCheckBox;
    JTextField _lastMessageLocalVariableTextField;


    public SubscribeSwing() {
    }

    public SubscribeSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        Subscribe action = (Subscribe) object;
        if (action == null) {
            // Create a temporary action
            action = new Subscribe("IQDA1", null, null);
            // Make "remove channel" the default for new actions
            action.setRemoveChannelFromLastTopic(true);
        }

        panel = new JPanel();

        JLabel subscribeToTopicLabel = new JLabel(Bundle.getMessage("Subscribe_subscribeToTopic"));
        JLabel lastTopicLocalVariableLabel = new JLabel(Bundle.getMessage("Subscribe_lastTopicLocalVariable"));
        JLabel lastMessageLocalVariableLabel = new JLabel(Bundle.getMessage("Subscribe_lastMessageLocalVariable"));

        _subscribeToTopicTextField = new JTextField(40);
        _lastTopicLocalVariableTextField = new JTextField(40);
        _removeChannelFromLastTopicCheckBox = new JCheckBox(Bundle.getMessage("Subscribe_removeChannelFromLastTopic"));
        _lastMessageLocalVariableTextField = new JTextField(40);

        _subscribeToTopicTextField.setText(action.getSubscribeToTopic());
        _lastTopicLocalVariableTextField.setText(action.getLastTopicLocalVariable());
        _removeChannelFromLastTopicCheckBox.setSelected(action.getRemoveChannelFromLastTopic());
        _lastMessageLocalVariableTextField.setText(action.getLastMessageLocalVariable());

        JPanel mqttPanel = new JPanel();
        mqttPanel.add(new JLabel(Bundle.getMessage("MqttConnection")));

        _mqttConnection = new JComboBox<>();
        List<MqttSystemConnectionMemo> systemConnections =
                jmri.InstanceManager.getList(MqttSystemConnectionMemo.class);
        for (MqttSystemConnectionMemo connection : systemConnections) {
            MqttConnection c = new MqttConnection(connection);
            _mqttConnection.addItem(c);
            if (action.getMemo() == connection) {
                _mqttConnection.setSelectedItem(c);
            }
        }
        mqttPanel.add(_mqttConnection);

        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        panel.add(subscribeToTopicLabel, c);
        subscribeToTopicLabel.setLabelFor(_subscribeToTopicTextField);
        c.gridy = 1;
        panel.add(lastTopicLocalVariableLabel, c);
        lastTopicLocalVariableLabel.setLabelFor(_lastTopicLocalVariableTextField);
        c.gridy = 2;
        panel.add(lastMessageLocalVariableLabel, c);
        lastMessageLocalVariableLabel.setLabelFor(_lastMessageLocalVariableTextField);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(_subscribeToTopicTextField, c);
//        _subscribeToTopicTextField.setToolTipText(Bundle.getMessage("SysNameToolTip", "Y"));
        c.gridy = 1;
        panel.add(_lastTopicLocalVariableTextField, c);
//        _lastTopicLocalVariableTextField.setToolTipText(Bundle.getMessage("SysNameToolTip", "Y"));
        c.gridy = 2;
        panel.add(_lastMessageLocalVariableTextField, c);
//        _lastMessageLocalVariableTextField.setToolTipText(Bundle.getMessage("SysNameToolTip", "Y"));
        c.gridy = 3;
        panel.add(_removeChannelFromLastTopicCheckBox, c);
        c.gridwidth = 2;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 4;
        c.anchor = GridBagConstraints.CENTER;
        panel.add(mqttPanel, c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        MqttSystemConnectionMemo memo =
                _mqttConnection.getItemAt(_mqttConnection.getSelectedIndex())._memo;

        Subscribe action = new Subscribe(systemName, userName, memo);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof Subscribe)) {
            throw new IllegalArgumentException("object must be an Subscribe but is a: "+object.getClass().getName());
        }
        Subscribe action = (Subscribe) object;

        action.setSubscribeToTopic(_subscribeToTopicTextField.getText());
        action.setLastTopicLocalVariable(_lastTopicLocalVariableTextField.getText());
        action.setRemoveChannelFromLastTopic(_removeChannelFromLastTopicCheckBox.isSelected());
        action.setLastMessageLocalVariable(_lastMessageLocalVariableTextField.getText());

        action.setMemo(_mqttConnection.getItemAt(_mqttConnection.getSelectedIndex())._memo);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Subscribe_Short");
    }

    @Override
    public void dispose() {
    }


    private static class MqttConnection {

        private MqttSystemConnectionMemo _memo;

        public MqttConnection(MqttSystemConnectionMemo memo) {
            _memo = memo;
        }

        @Override
        public String toString() {
            return _memo.getUserName();
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PublishSwing.class);

}
