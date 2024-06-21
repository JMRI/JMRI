package jmri.jmrix.mqtt.logixng.swing;

import jmri.jmrit.logixng.actions.swing.AbstractDigitalActionSwing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;
import jmri.jmrix.mqtt.logixng.Publish;
import jmri.jmrix.mqtt.logixng.Publish.Retain;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectStringSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an Publish object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class PublishSwing extends AbstractDigitalActionSwing {

    private JComboBox<MqttConnection> _mqttConnection;

    private LogixNG_SelectStringSwing _selectTopicSwing;
    private LogixNG_SelectStringSwing _selectMessageSwing;

    private JComboBox<Retain> _retainComboBox;


    public PublishSwing() {
    }

    public PublishSwing(JDialog dialog) {
        super.setJDialog(dialog);
    }

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        Publish action = (Publish) object;
        if (action == null) {
            // Create a temporary action
            action = new Publish("IQDA1", null, null);
        }

        _selectTopicSwing = new LogixNG_SelectStringSwing(getJDialog(), this);
        _selectMessageSwing = new LogixNG_SelectStringSwing(getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel tabbedPaneTopic;
        JPanel tabbedPaneData;

        tabbedPaneTopic = _selectTopicSwing.createPanel(action.getSelectTopic());
        tabbedPaneData = _selectMessageSwing.createPanel(action.getSelectMessage());


        JPanel internalPanel = new JPanel();

        JComponent[] components = new JComponent[]{
            tabbedPaneTopic,
            tabbedPaneData};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("Publish_Components"), components);

        for (JComponent c : componentList) internalPanel.add(c);

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

        JPanel retainPanel = new JPanel();
        retainPanel.add(new JLabel(Bundle.getMessage("PublishSwing_Retain")));

        _retainComboBox = new JComboBox<>();
        for (Retain e : Retain.values()) {
            _retainComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_retainComboBox);
        _retainComboBox.setSelectedItem(action.getRetain());
        retainPanel.add(_retainComboBox);

        panel.add(internalPanel);
        panel.add(retainPanel);
        panel.add(mqttPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        Publish action = new Publish("IQDA1", null, null);

        _selectTopicSwing.validate(action.getSelectTopic(), errorMessages);
        _selectMessageSwing.validate(action.getSelectMessage(), errorMessages);

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
        MqttSystemConnectionMemo memo =
                _mqttConnection.getItemAt(_mqttConnection.getSelectedIndex())._memo;

        Publish action = new Publish(systemName, userName, memo);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof Publish)) {
            throw new IllegalArgumentException("object must be an Publish but is a: "+object.getClass().getName());
        }
        Publish action = (Publish) object;

        _selectTopicSwing.updateObject(action.getSelectTopic());
        _selectMessageSwing.updateObject(action.getSelectMessage());

        action.setMemo(_mqttConnection.getItemAt(_mqttConnection.getSelectedIndex())._memo);
        action.setRetain(_retainComboBox.getItemAt(_retainComboBox.getSelectedIndex()));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Publish_Short");
    }

    @Override
    public void dispose() {
        _selectTopicSwing.dispose();
        _selectMessageSwing.dispose();
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
