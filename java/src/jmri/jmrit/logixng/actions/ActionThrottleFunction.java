package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.LogixNG_SelectInteger;

/**
 * Sets a function on a throttle
 *
 * @author Daniel Bergqvist Copyright 2023
 */
public final class ActionThrottleFunction extends AbstractDigitalAction
        implements PropertyChangeListener {

    private SystemConnectionMemo _memo;
    private ThrottleManager _throttleManager;
    private ThrottleManager _oldThrottleManager;

    // The throttle if we have one or if a request is sent, null otherwise
    private DccThrottle _throttle;
    private ThrottleListener _throttleListener;

    private final LogixNG_SelectInteger _selectAddress = new LogixNG_SelectInteger(this, this);
    private final LogixNG_SelectInteger _selectFunction = new LogixNG_SelectInteger(this, this);
    private final LogixNG_SelectEnum<FunctionState> _selectOnOff =
            new LogixNG_SelectEnum<>(this, FunctionState.values(), FunctionState.On, this);


    public ActionThrottleFunction(String sys, String user) {
        super(sys, user);

        // Set the _throttleManager variable
        setMemo(null);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionThrottleFunction copy = new ActionThrottleFunction(sysName, userName);
        copy.setComment(getComment());
        copy.setMemo(_memo);
        _selectAddress.copy(copy._selectAddress);
        _selectFunction.copy(copy._selectFunction);
        _selectOnOff.copy(copy._selectOnOff);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectInteger getSelectAddress() {
        return _selectAddress;
    }

    public LogixNG_SelectInteger getSelectFunction() {
        return _selectFunction;
    }

    public LogixNG_SelectEnum<FunctionState> getSelectOnOff() {
        return _selectOnOff;
    }

    public void setMemo(SystemConnectionMemo memo) {
        assertListenersAreNotRegistered(log, "setMemo");
        _memo = memo;
        if (_memo != null) {
            _throttleManager = _memo.get(jmri.ThrottleManager.class);
            if (_throttleManager == null) {
                throw new IllegalArgumentException("Memo "+memo.getUserName()+" doesn't have a ThrottleManager");
            }
        } else {
            _throttleManager = InstanceManager.getDefault(ThrottleManager.class);
        }
    }

    public SystemConnectionMemo getMemo() {
        return _memo;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

        ConditionalNG conditionalNG = this.getConditionalNG();

        int currentLocoAddress = -1;
        int newLocoAddress = -1;

        if (_throttle != null) {
            currentLocoAddress = _throttle.getLocoAddress().getNumber();
        }

        newLocoAddress = _selectAddress.evaluateValue(conditionalNG);

        if (_throttleManager != _oldThrottleManager) {
            currentLocoAddress = -1;    // Force request of new throttle
            _oldThrottleManager = _throttleManager;
        }

        if (newLocoAddress != currentLocoAddress) {

            if (_throttle != null) {
                // Release the loco
                _throttleManager.releaseThrottle(_throttle, _throttleListener);
                _throttle = null;
            }

            if (newLocoAddress != -1) {

                _throttleListener =  new ThrottleListener() {
                    @Override
                    public void notifyThrottleFound(DccThrottle t) {
                        _throttle = t;
                        executeConditionalNG();
                    }

                    @Override
                    public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
                        log.warn("loco {} cannot be aquired", address.getNumber());
                    }

                    @Override
                    public void notifyDecisionRequired(LocoAddress address, ThrottleListener.DecisionType question) {
                        log.warn("Loco {} cannot be aquired. Decision required.", address.getNumber());
                    }
                };

                boolean result = _throttleManager.requestThrottle(newLocoAddress, _throttleListener);

                if (!result) {
                    log.warn("loco {} cannot be aquired", newLocoAddress);
                }
            }

        }

        // We have a throttle if _throttle is not null
        if (_throttle != null) {

            int function = _selectFunction.evaluateValue(conditionalNG);
            boolean isFunctionOn = _selectOnOff.evaluateEnum(conditionalNG)._value;

            DccThrottle throttle = _throttle;
            int func = function;
            boolean funcState = isFunctionOn;
            jmri.util.ThreadingUtil.runOnLayoutWithJmriException(() -> {
                throttle.setFunction(func, funcState);
            });
        }
    }

    private void executeConditionalNG() {
        if (_listenersAreRegistered) {
            ConditionalNG c = getConditionalNG();
            if (c != null) {
                c.execute();
            }
        }
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionThrottleFunction_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        if (_memo != null) {
            return Bundle.getMessage(locale, "ActionThrottleFunction_LongConnection",
                    _selectAddress.getDescription(locale),
                    _selectFunction.getDescription(locale),
                    _selectOnOff.getDescription(locale),
                    _memo.getUserName());
        } else {
            return Bundle.getMessage(locale, "ActionThrottleFunction_Long",
                    _selectAddress.getDescription(locale),
                    _selectFunction.getDescription(locale),
                    _selectOnOff.getDescription(locale));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _listenersAreRegistered = true;
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        if (_throttle != null) {
            _throttleManager.releaseThrottle(_throttle, _throttleListener);
        }
    }

    public enum FunctionState {
        Off(false, Bundle.getMessage("StateOff")),
        On(true, Bundle.getMessage("StateOn"));

        private final boolean _value;
        private final String _text;

        private FunctionState(boolean value, String text) {
            this._value = value;
            this._text = text;
        }

        public boolean getValue() {
            return _value;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionThrottleFunction.class);

}
