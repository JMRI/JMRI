package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.util.Date;

import java.util.Locale;

import jmri.InstanceManager;
import jmri.Timebase;
import jmri.jmrit.logixng.*;

/**
 * This expression is a clock.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionClock extends AbstractDigitalExpression implements PropertyChangeListener {

    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.IS;
    private Type _type = Type.FastClock;
    private Timebase fastClock;
    private int _beginTime = 0;
    private int _endTime = 0;
    
    
    public ExpressionClock(String sys, String user) {
        super(sys, user);
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }
    
    public void setType(Type type) {
        assertListenersAreNotRegistered(log, "setType");
        _type = type;
        
        if (_type == Type.FastClock) {
            fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        } else {
            fastClock = null;
        }
    }
    
    public Type getType() {
        return _type;
    }
    
    public void setRange(int beginTime, int endTime) {
        assertListenersAreNotRegistered(log, "setRange");
        _beginTime = beginTime;
        _endTime = endTime;
    }
    
    public int getBeginTime() {
        return _beginTime;
    }
    
    public int getEndTime() {
        return _endTime;
    }
    
    /** {@inheritDoc} */
    @SuppressWarnings("deprecation")        // Date.getMinutes, Date.getHours
    @Override
    public boolean evaluate() {
        boolean result;
        
        Date currentTime;
        
        switch (_type) {
            case SystemClock:
                currentTime = Date.from(Instant.now());
                break;
                
            case FastClock:
                if (fastClock == null) return false;
                currentTime = fastClock.getTime();
                break;
                
            default:
                throw new UnsupportedOperationException("_type has unknown value: " + _type.name());
        }
        
        int currentMinutes = (currentTime.getHours() * 60) + currentTime.getMinutes();
        // check if current time is within range specified
        if (_beginTime <= _endTime) {
            // range is entirely within one day
            result = (_beginTime <= currentMinutes) && (currentMinutes <= _endTime);
        } else {
            // range includes midnight
            result = _beginTime <= currentMinutes || currentMinutes <= _endTime;
        }
        
        if (_is_IsNot == Is_IsNot_Enum.IS) {
            return result;
        } else {
            return !result;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        // Do nothing.
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
        switch (_type) {
            case SystemClock:
                return Bundle.getMessage(locale, "Clock_Short_SystemClock");
                
            case FastClock:
                return Bundle.getMessage(locale, "Clock_Short_FastClock");
                
            default:
                throw new RuntimeException("Unknown value of _timerType: "+_type.name());
        }
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        switch (_type) {
            case SystemClock:
                return Bundle.getMessage(locale, "Clock_Long_SystemClock", _is_IsNot.toString(), _beginTime, _endTime);
                
            case FastClock:
                return Bundle.getMessage(locale, "Clock_Long_FastClock", _is_IsNot.toString(), _beginTime, _endTime);
                
            default:
                throw new RuntimeException("Unknown value of _timerType: "+_type.name());
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
            switch (_type) {
                case SystemClock:
                    throw new UnsupportedOperationException("Not implemented yet");
                    
                case FastClock:
                    fastClock.addPropertyChangeListener("time", this);
                    break;
                    
                default:
                    throw new UnsupportedOperationException("_type has unknown value: " + _type.name());
            }
            
            _listenersAreRegistered = true;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            switch (_type) {
                case SystemClock:
                    throw new UnsupportedOperationException("Not implemented yet");
                    
                case FastClock:
                    fastClock.removePropertyChangeListener("time", this);
                    break;
                    
                default:
                    throw new UnsupportedOperationException("_type has unknown value: " + _type.name());
            }
            
            _listenersAreRegistered = false;
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (getTriggerOnChange()) {
            getConditionalNG().execute();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    
    public enum Type {
        SystemClock(Bundle.getMessage("ClockTypeSystemClock")),
        FastClock(Bundle.getMessage("ClockTypeFastClock"));
        
        private final String _text;
        
        private Type(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionClock.class);
    
}
