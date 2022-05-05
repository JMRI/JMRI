package jmri.jmrix.mqtt.logixng;

import jmri.jmrit.logixng.actions.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectString;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrix.mqtt.MqttSystemConnectionMemo;
import jmri.util.ThreadingUtil;

/**
 * This action publishes a message to MQTT.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class Publish extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectString _selectTopic =
            new LogixNG_SelectString(this, this);
    private final LogixNG_SelectString _selectMessage =
            new LogixNG_SelectString(this, this);

    private MqttSystemConnectionMemo _memo;


    public Publish(String sys, String user, MqttSystemConnectionMemo memo)
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
        Publish copy = new Publish(sysName, userName, _memo);
        copy.setComment(getComment());
        _selectTopic.copy(copy._selectTopic);
        _selectMessage.copy(copy._selectMessage);
        return manager.registerAction(copy);
    }

    public void setMemo(MqttSystemConnectionMemo memo) {
        assertListenersAreNotRegistered(log, "setMemo");
        _memo = memo;
    }

    public MqttSystemConnectionMemo getMemo() {
        return _memo;
    }

    public LogixNG_SelectString getSelectTopic() {
        return _selectTopic;
    }

    public LogixNG_SelectString getSelectMessage() {
        return _selectMessage;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

        String topic = _selectTopic.evaluateValue(getConditionalNG());
        String data = _selectMessage.evaluateValue(getConditionalNG());

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            _memo.getMqttAdapter().publish(topic, data);
        });
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
        return Bundle.getMessage(locale, "Publish_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Publish_Long",
                _selectTopic.getDescription(locale),
                _selectMessage.getDescription(locale));
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _selectTopic.registerListeners();
        _selectMessage.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectTopic.unregisterListeners();
        _selectMessage.unregisterListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Publish.class);

}
