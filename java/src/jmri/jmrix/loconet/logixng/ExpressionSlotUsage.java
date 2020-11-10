package jmri.jmrix.loconet.logixng;

import java.util.Locale;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.expressions.*;
import jmri.jmrix.loconet.LocoNetSlot;
import jmri.jmrix.loconet.SlotListener;
import jmri.jmrix.loconet.SlotManager;

/**
 * This expression compares the number of slots that are currently in use with
 * a threshold number.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionSlotUsage extends AbstractDigitalExpression
        implements SlotListener {

    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
//    private TurnoutState _turnoutState = TurnoutState.Thrown;
    private int _threshold = 0;
    
    
    public ExpressionSlotUsage(String sys, String user) {
        super(sys, user);
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
/*    
    public void setTimerType(TimerType timerType) {
        assertListenersAreNotRegistered(log, "setTimerType");
        _timerType = timerType;
    }
    
    public TimerType getTimerType() {
        return _timerType;
    }
*/    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }
    
    public void setThreshold(int threshold) {
        assertListenersAreNotRegistered(log, "setThreshold");
        _threshold = threshold;
    }
    
    public long getThreshold() {
        return _threshold;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() {
        boolean result = false;
/*        
        switch (_timerType) {
            case WAIT_ONCE_TRIG_ONCE:
                if (_timerStatusRef.get() == TimerStatus.NOT_STARTED) {
                    startTimer();
                } else if (_timerStatusRef.get() == TimerStatus.FINISHED) {
                    _timerStatusRef.set(TimerStatus.WAIT_FOR_RESET);
                    result = true;
                }
                break;
                
            case WAIT_ONCE_TRIG_UNTIL_RESET:
                if (_timerStatusRef.get() == TimerStatus.NOT_STARTED) {
                    startTimer();
                } else if (_timerStatusRef.get() == TimerStatus.FINISHED) {
                    // Don't set _timerStatus to WAIT_FOR_RESET since we want
                    // to keep returning true until reset()
                    result = true;
                }
                break;
                
            case REPEAT_SINGLE_DELAY:
                if (_timerStatusRef.get() == TimerStatus.NOT_STARTED) {
                    startTimer();
                } else if (_timerStatusRef.get() == TimerStatus.FINISHED) {
                    startTimer();
                    result = true;
                }
                break;
                
            case REPEAT_DOUBLE_DELAY:
                if (_timerStatusRef.get() == TimerStatus.NOT_STARTED) {
                    startTimer();
                } else if (_timerStatusRef.get() == TimerStatus.FINISHED) {
                    _onOrOff = ! _onOrOff;
                    startTimer();
                }
                result = _onOrOff;
                break;
                
            default:
                throw new RuntimeException("_timerType has unknown value: "+_timerType.name());
        }
*/        
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
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
        return Bundle.getMessage(locale, "ExpressionSlotUsage_Short");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
/*        
        switch (_timerType) {
            case WAIT_ONCE_TRIG_ONCE:
                return Bundle.getMessage(locale, "Timer_Long_WaitOnceTrigOnce", _threshold);
                
            case WAIT_ONCE_TRIG_UNTIL_RESET:
                return Bundle.getMessage(locale, "Timer_Long_WaitOnceTrigUntilReset", _threshold);
                
            case REPEAT_SINGLE_DELAY:
                return Bundle.getMessage(locale, "Timer_Long_RepeatSingleDelay", _threshold);
                
            case REPEAT_DOUBLE_DELAY:
                return Bundle.getMessage(locale, "Timer_Long_RepeatDoubleDelay", _threshold, _delayOn);
                
            default:
                throw new RuntimeException("Unknown value of _timerType: "+_timerType.name());
        }
*/        
        return Bundle.getMessage(locale, "ExpressionSlotUsage_Long");
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
            
            // The LocoNet simulator doesn't have a slot manager
            SlotManager slotManager = InstanceManager.getNullableDefault(SlotManager.class);
            if (slotManager != null) {
                slotManager.addSlotListener(this);
            }
//            InstanceManager.getDefault(SlotManager.class).addSlotListener(this);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        // The LocoNet simulator doesn't have a slot manager
        SlotManager slotManager = InstanceManager.getNullableDefault(SlotManager.class);
        if (slotManager != null) {
            slotManager.removeSlotListener(this);
        }
//        InstanceManager.getDefault(SlotManager.class).removeSlotListener(this);
        _listenersAreRegistered = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    @Override
    public void notifyChangedSlot(LocoNetSlot s) {
        if (_listenersAreRegistered) {
            getConditionalNG().execute();
        }
    }
    
    
    
    public enum TimerType {
        WAIT_ONCE_TRIG_ONCE(Bundle.getMessage("TimerType_WaitOnceTrigOnce"), Bundle.getMessage("TimerType_Explanation_WaitOnceTrigOnce")),
        WAIT_ONCE_TRIG_UNTIL_RESET(Bundle.getMessage("TimerType_WaitOnceTrigUntilReset"), Bundle.getMessage("TimerType_Explanation_WaitOnceTrigUntilReset")),
        REPEAT_SINGLE_DELAY(Bundle.getMessage("TimerType_RepeatSingleDelay"), Bundle.getMessage("TimerType_Explanation_RepeatSingleDelay")),
        REPEAT_DOUBLE_DELAY(Bundle.getMessage("TimerType_RepeatDoubleDelay"), Bundle.getMessage("TimerType_Explanation_RepeatDoubleDelay"));
        
        private final String _text;
        private final String _explanation;
        
        private TimerType(String text, String explanation) {
            this._text = text;
            this._explanation = explanation;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
        public String getExplanation() {
            return _explanation;
        }
        
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSlotUsage.class);
    
}
