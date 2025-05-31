package jmri.jmrix.mqtt.logixng;

import jmri.jmrit.logixng.actions.*;

import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrix.mqtt.MqttEventListener;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;
import jmri.util.ThreadingUtil;

/**
 * This action subscribes to a topic to MQTT.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class Subscribe extends AbstractDigitalAction
        implements MqttEventListener {

    private MqttSystemConnectionMemo _memo;
    private String _subscribeToTopic;
    private String _lastTopic;
    private String _lastMessage;
    private String _lastTopicLocalVariable;
    private boolean _removeChannelFromLastTopic;
    private String _lastMessageLocalVariable;


    public Subscribe(String sys, String user, MqttSystemConnectionMemo memo)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _memo = memo;
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        Subscribe copy = new Subscribe(sysName, userName, _memo);
        copy.setComment(getComment());
        copy._subscribeToTopic = _subscribeToTopic;
        copy._lastTopicLocalVariable = _lastTopicLocalVariable;
        copy._removeChannelFromLastTopic = _removeChannelFromLastTopic;
        copy._lastMessageLocalVariable = _lastMessageLocalVariable;
        return manager.registerAction(copy);
    }

    public void setMemo(MqttSystemConnectionMemo memo) {
        assertListenersAreNotRegistered(log, "setMemo");
        _memo = memo;
    }

    public MqttSystemConnectionMemo getMemo() {
        return _memo;
    }

    public String getSubscribeToTopic() {
        return _subscribeToTopic;
    }

    public void setSubscribeToTopic(String topic) {
        _subscribeToTopic = topic;
    }

    public String getLastTopicLocalVariable() {
        return _lastTopicLocalVariable;
    }

    public void setLastTopicLocalVariable(String variable) {
        _lastTopicLocalVariable = variable;
    }

    public boolean getRemoveChannelFromLastTopic() {
        return _removeChannelFromLastTopic;
    }

    public void setRemoveChannelFromLastTopic(boolean value) {
        _removeChannelFromLastTopic = value;
    }


    public String getLastMessageLocalVariable() {
        return _lastMessageLocalVariable;
    }

    public void setLastMessageLocalVariable(String variable) {
        _lastMessageLocalVariable = variable;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return CategoryMqtt.MQTT;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        if ((_lastTopicLocalVariable != null) && (!_lastTopicLocalVariable.isBlank())) {
            getConditionalNG().getSymbolTable().setValue(_lastTopicLocalVariable, _lastTopic);
        }
        if ((_lastMessageLocalVariable != null) && (!_lastMessageLocalVariable.isBlank())) {
            getConditionalNG().getSymbolTable().setValue(_lastMessageLocalVariable, _lastMessage);
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Subscribe_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Subscribe_Long", _subscribeToTopic);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (! _listenersAreRegistered) {
            if (_subscribeToTopic == null) return;
            ThreadingUtil.runOnLayout(() -> {
                _memo.getMqttAdapter().subscribe(_subscribeToTopic, this);
            });
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            if (_subscribeToTopic == null) return;
            ThreadingUtil.runOnLayout(() -> {
                _memo.getMqttAdapter().unsubscribe(_subscribeToTopic, this);
            });
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notifyMqttMessage(String topic, String message) {
        _lastTopic = topic;
        if (_removeChannelFromLastTopic && _lastTopic.startsWith(_memo.getMqttAdapter().baseTopic)) {
            _lastTopic = _lastTopic.substring(_memo.getMqttAdapter().baseTopic.length());
        }
        _lastMessage = message;
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Subscribe.class);

}
