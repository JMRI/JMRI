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
    
    private Retain _retain;


    public Publish(String sys, String user, MqttSystemConnectionMemo memo)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _memo = memo;
        _retain = Retain.Default;
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
        copy.setRetain(_retain);
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

    public void setRetain(Retain retain) {
        assertListenersAreNotRegistered(log, "setRetain");
        _retain = retain;
    }

    public Retain getRetain() {
        return _retain;
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
        
        boolean retain = _retain.getRetainValue(_memo);

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            _memo.getMqttAdapter().publish(topic, data, retain);
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
                _selectMessage.getDescription(locale),
                _retain.toString());
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


    public enum Retain {
        Default(Bundle.getMessage("Publish_Retain_Default")),
        Yes(Bundle.getMessage("Publish_Retain_Yes")),
        No(Bundle.getMessage("Publish_Retain_No"));

        private final String _text;

        private Retain(String text) {
            this._text = text;
        }
        
        public boolean getRetainValue(MqttSystemConnectionMemo memo) {
            switch (this) {
                case Default:
                    return memo.getMqttAdapter().retained;
                case Yes:
                    return true;
                case No:
                    return false;
                default:
                    throw new IllegalArgumentException("invalid retain");
            }
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Publish.class);

}
