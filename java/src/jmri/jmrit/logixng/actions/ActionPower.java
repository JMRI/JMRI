package jmri.jmrit.logixng.actions;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.ThreadingUtil;

/**
 * This action turns power on or off.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionPower extends AbstractDigitalAction {

    private PowerState _powerState = PowerState.On;
    
    public ActionPower(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionPower copy = new ActionPower(sysName, userName);
        copy.setComment(getComment());
        copy.setBeanState(_powerState);
        return manager.registerAction(copy);
    }
    
    public void setBeanState(PowerState state) {
        _powerState = state;
    }
    
    public PowerState getBeanState() {
        return _powerState;
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        AtomicReference<JmriException> exception = new AtomicReference<>();
        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            try {
                InstanceManager.getDefault(PowerManager.class).setPower(_powerState.getID());
            } catch (JmriException e) {
                exception.set(e);
            }
        });
        if (exception.get() != null) throw exception.get();
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
        return Bundle.getMessage(locale, "Power_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Power_Long", _powerState._text);
    }
    
    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    public enum PowerState {
        Off(PowerManager.OFF, Bundle.getMessage("PowerStateOff")),
        On(PowerManager.ON, Bundle.getMessage("PowerStateOn"));
        
        private final int _id;
        private final String _text;
        
        private PowerState(int id, String text) {
            this._id = id;
            this._text = text;
        }
        
        static public PowerState get(int id) {
            switch (id) {
                case PowerManager.OFF:
                    return Off;
                    
                case PowerManager.ON:
                    return On;
                    
                default:
                    throw new IllegalArgumentException("invalid power state");
            }
        }
        
        public int getID() {
            return _id;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionPower.class);
    
}
