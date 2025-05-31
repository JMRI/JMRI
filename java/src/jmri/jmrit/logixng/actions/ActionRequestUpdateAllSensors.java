package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Sets all engine slots to status common
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionRequestUpdateAllSensors extends AbstractDigitalAction {

    private SystemConnectionMemo _memo;

    public ActionRequestUpdateAllSensors(String sys, String user, SystemConnectionMemo memo) {
        super(sys, user);
        _memo = memo;
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionRequestUpdateAllSensors copy = new ActionRequestUpdateAllSensors(sysName, userName, _memo);
        copy.setComment(getComment());
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    public void setMemo(SystemConnectionMemo memo) {
        assertListenersAreNotRegistered(log, "setMemo");
        _memo = memo;
    }

    public SystemConnectionMemo getMemo() {
        return _memo;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() {
        if (_memo != null) {
            if (_memo.provides(SensorManager.class)) {
                _memo.get(SensorManager.class).updateAll();
            } else {
                throw new RuntimeException("_memo doesn't provide a sensor manager");
            }
        } else {
            InstanceManager.getDefault(SensorManager.class).updateAll();
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
        return Bundle.getMessage(locale, "ActionRequestUpdateAllSensors_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        if (_memo != null) {
            return Bundle.getMessage(locale, "ActionRequestUpdateAllSensors_LongConnection",
                    _memo.getUserName());
        } else {
            return Bundle.getMessage(locale, "ActionRequestUpdateAllSensors_Long");
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
        if (!_listenersAreRegistered) {
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionRequestUpdateAllSensors.class);

}
