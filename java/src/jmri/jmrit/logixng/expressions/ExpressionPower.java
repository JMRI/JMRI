package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.*;

/**
 * This expression sets the state of a power.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionPower extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private PowerState _powerState = PowerState.On;
    
    public ExpressionPower(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionPower copy = new ExpressionPower(sysName, userName);
        copy.setComment(getComment());
        copy.set_Is_IsNot(_is_IsNot);
        copy.setBeanState(_powerState);
        return manager.registerExpression(copy);
    }
    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
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
    public boolean evaluate() throws JmriException {
        
        PowerState checkPowerState = _powerState;
        
        PowerState currentPowerState =
                PowerState.get(InstanceManager.getDefault(PowerManager.class)
                        .getPower());
        
        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return currentPowerState == checkPowerState;
        } else {
            return currentPowerState != checkPowerState;
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
        return Bundle.getMessage(locale, "Power_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Power_Long", _is_IsNot.toString(), _powerState._text);
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
            InstanceManager.getDefault(PowerManager.class)
                    .addPropertyChangeListener(PowerManager.POWER, this);
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            InstanceManager.getDefault(PowerManager.class)
                    .removePropertyChangeListener(PowerManager.POWER, this);
            _listenersAreRegistered = false;
        }
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
    
    
    public enum PowerState {
        On(PowerManager.ON, Bundle.getMessage("PowerStateOn")),
        Off(PowerManager.OFF, Bundle.getMessage("PowerStateOff")),
        Other(-1, Bundle.getMessage("PowerStateOther"));
        
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
                    return Other;
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
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionPower.class);
    
}
