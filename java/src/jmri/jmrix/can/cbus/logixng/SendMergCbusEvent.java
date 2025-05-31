package jmri.jmrix.can.cbus.logixng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.AbstractDigitalAction;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.LogixNG_SelectInteger;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.CbusEvent;
import jmri.util.ThreadingUtil;

/**
 * This action sends a Cbus event.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class SendMergCbusEvent extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectInteger _selectNodeNumber =
            new LogixNG_SelectInteger(
                    this, this);

    private final LogixNG_SelectInteger _selectEventNumber =
            new LogixNG_SelectInteger(
                    this, this);

    private final LogixNG_SelectEnum<CbusEventType> _selectEventType =
            new LogixNG_SelectEnum<>(this, CbusEventType.values(), CbusEventType.Off, this);

    private CanSystemConnectionMemo _memo;


    public SendMergCbusEvent(String sys, String user, CanSystemConnectionMemo memo)
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
        SendMergCbusEvent copy = new SendMergCbusEvent(sysName, userName, _memo);
        copy.setComment(getComment());
        _selectNodeNumber.copy(copy._selectNodeNumber);
        _selectEventNumber.copy(copy._selectEventNumber);
        _selectEventType.copy(copy._selectEventType);

        return manager.registerAction(copy);
    }

    public void setMemo(CanSystemConnectionMemo memo) {
        assertListenersAreNotRegistered(log, "setMemo");
        _memo = memo;
    }

    public CanSystemConnectionMemo getMemo() {
        return _memo;
    }

    public LogixNG_SelectInteger getSelectNodeNumber() {
        return _selectNodeNumber;
    }

    public LogixNG_SelectInteger getSelectEventNumber() {
        return _selectEventNumber;
    }

    public LogixNG_SelectEnum<CbusEventType> getSelectEventType() {
        return _selectEventType;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return CategoryMergCbus.CBUS;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        ConditionalNG conditionalNG = getConditionalNG();
        int nodeNumber = _selectNodeNumber.evaluateValue(conditionalNG);
        int eventNumber = _selectEventNumber.evaluateValue(conditionalNG);
        CbusEventType state = _selectEventType.evaluateEnum(getConditionalNG());

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            CbusEvent event = new CbusEvent(_memo,nodeNumber,eventNumber);
            state._command.action(event);
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
        return Bundle.getMessage(locale, "SendCbusEvent_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String nodeNumber = _selectNodeNumber.getDescription(locale);
        String eventNumber = _selectEventNumber.getDescription(locale);
        String eventType = _selectEventType.getDescription(locale);

        return Bundle.getMessage(locale, "SendCbusEvent_Long", eventNumber, eventType, nodeNumber,
                _memo != null ? _memo.getUserName() : Bundle.getMessage("MemoNotSet"));
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _selectNodeNumber.registerListeners();
        _selectEventType.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNodeNumber.unregisterListeners();
        _selectEventType.unregisterListeners();
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


    public enum CbusEventType {
        Off((e)-> {e.sendOff();}, Bundle.getMessage("SendCbusEvent_EventType_Off")),
        On((e)-> {e.sendOn();}, Bundle.getMessage("SendCbusEvent_EventType_On")),
        Request((e)-> {e.sendRequest();}, Bundle.getMessage("SendCbusEvent_EventType_Request"));

        private interface EventCommand {
            void action(CbusEvent event);
        }

        private final EventCommand _command;
        private final String _text;

        private CbusEventType(EventCommand command, String text) {
            this._command = command;
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SendMergCbusEvent.class);

}
