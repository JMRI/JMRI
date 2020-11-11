package jmri.jmrix.loconet.logixng;

import java.util.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.expressions.*;
import jmri.jmrix.loconet.*;

/**
 * This expression compares the number of slots that are currently in use with
 * a threshold number.
 * 
 * @author Daniel Bergqvist Copyright 2020
 */
public class ExpressionSlotUsage extends AbstractDigitalExpression
        implements SlotListener {

    private LocoNetSystemConnectionMemo _memo;
    private boolean _advanced = false;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private HasHasNotType _hasHasNot = HasHasNotType.Has;
    private SimpleStateType _simpleState = SimpleStateType.InUse;
    private Set<AdvancedStateType> _advancedStates = new HashSet<>();
    private CompareType _compare = CompareType.LessThan;
    private PercentPiecesType _percentPieces = PercentPiecesType.Pieces;
    private int _number = 0;
    
    
    public ExpressionSlotUsage(String sys, String user, LocoNetSystemConnectionMemo memo) {
        super(sys, user);
        _memo = memo;
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
    
    public void setMemo(LocoNetSystemConnectionMemo memo) {
        assertListenersAreNotRegistered(log, "setTimerType");
        _memo = memo;
    }
    
    public LocoNetSystemConnectionMemo getMemo() {
        return _memo;
    }
    
    public void setAdvanced(boolean advanced) {
        assertListenersAreNotRegistered(log, "setHasHasNot");
        _advanced = advanced;
    }
    
    public boolean getAdvanced() {
        return _advanced;
    }
    
    public void setHasHasNot(HasHasNotType hasHasNot) {
        assertListenersAreNotRegistered(log, "setHasHasNot");
        _hasHasNot = hasHasNot;
    }
    
    public HasHasNotType getHasHasNot() {
        return _hasHasNot;
    }
    
    public void setSimpleState(SimpleStateType simpleState){
        assertListenersAreNotRegistered(log, "setSimpleState");
        _simpleState = simpleState;
    }
    
    public SimpleStateType getSimpleState() {
        return _simpleState;
    }
    
    public void setAdvancedStates(Set<AdvancedStateType> states) {
        assertListenersAreNotRegistered(log, "setTimerType");
        _advancedStates.clear();
        _advancedStates.addAll(states);
    }
    
    public Set<AdvancedStateType> getAdvancedStates() {
        return Collections.unmodifiableSet(_advancedStates);
    }
    
    public void setCompare(CompareType compare){
        assertListenersAreNotRegistered(log, "setTimerType");
        _compare = compare;
    }
    
    public CompareType getCompare() {
        return _compare;
    }
    
    public void setPercentPieces(PercentPiecesType percentPieces) {
        assertListenersAreNotRegistered(log, "setPercentPieces");
        _percentPieces = percentPieces;
    }
    
    public PercentPiecesType getPercentPieces() {
        return _percentPieces;
    }
    
    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }
    
    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }
    
    public void setNumber(int number) {
        assertListenersAreNotRegistered(log, "setThreshold");
        _number = number;
    }
    
    public int getNumber() {
        return _number;
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
        String stateStr;
        if (_advanced) {
            StringBuilder states = new StringBuilder();
            for (AdvancedStateType state : _advancedStates) {
                if (states.length() > 0) states.append(",");
                states.append(state._text);
            }
            stateStr = states.length() > 0 ? states.toString() : Bundle.getMessage("NoState");
        } else {
            stateStr = _simpleState._text;
        }
        
        return Bundle.getMessage(locale, "ExpressionSlotUsage_Long",
                _hasHasNot.toString(),
                stateStr,
                _compare.toString(),
                _number,
                _percentPieces.toString()
                );
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
//        return Bundle.getMessage(locale, "ExpressionSlotUsage_Long");
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
            
            if (_memo != null) {
                SlotManager slotManager = _memo.getSlotManager();
                slotManager.addSlotListener(this);
                
                slotManager.getInUseCount();
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_memo != null) {
            SlotManager slotManager = _memo.getSlotManager();
            slotManager.removeSlotListener(this);
        }
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
    
    
    
    public enum HasHasNotType {
        Has(Bundle.getMessage("HasHasNotType_Has")),
        HasNot(Bundle.getMessage("HasHasNotType_HasNot"));
        
        private final String _text;
        
        private HasHasNotType(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    public enum SimpleStateType {
        InUse(Bundle.getMessage("SimpleStateType_InUse"), new int[]{LnConstants.LOCO_IN_USE}),
        Free(Bundle.getMessage("SimpleStateType_Free"), new int[]{LnConstants.LOCO_FREE});
        
        private final String _text;
        private final int[] _states;
        
        private SimpleStateType(String text, int[] states) {
            this._text = text;
            this._states = states;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
        public int[] getStates() {
            return _states;
        }
        
    }
    
    
    public enum AdvancedStateType {
        InUse(LnConstants.LOCO_IN_USE, Bundle.getMessage("AdvancedStateType_InUse")),
        Idle(LnConstants.LOCO_IDLE, Bundle.getMessage("AdvancedStateType_Idle")),
        Common(LnConstants.LOCO_COMMON, Bundle.getMessage("AdvancedStateType_Common")),
        Free(LnConstants.LOCO_FREE, Bundle.getMessage("AdvancedStateType_Free"));
        
        private final int _state;
        private final String _text;
        
        private AdvancedStateType(int state, String text) {
            this._state = state;
            this._text = text;
        }
        
        public int getState() {
            return _state;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    public enum CompareType {
        LessThan(Bundle.getMessage("CompareType_LessThan"), (int a, int b) -> a < b),
        LessThanOrEqual(Bundle.getMessage("CompareType_LessThanOrEqual"), (int a, int b) -> a <= b),
        Equal(Bundle.getMessage("CompareType_Equal"), (int a, int b) -> a == b),
        GreaterThanOrEqual(Bundle.getMessage("CompareType_GreaterThanOrEqual"), (int a, int b) -> a >= b),
        GreaterThan(Bundle.getMessage("CompareType_GreaterThan"), (int a, int b) -> a > b);
        
        private final String _text;
        private final Compare _compare;
        
        private CompareType(String text, Compare compare) {
            this._text = text;
            this._compare = compare;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
        public boolean compare(int a, int b) {
            return _compare.compare(a, b);
        }
        
    }
    
    
    public enum PercentPiecesType {
        Percent(Bundle.getMessage("PercentPiecesType_Percent")),
        Pieces(Bundle.getMessage("PercentPiecesType_Pieces"));
        
        private final String _text;
        
        private PercentPiecesType(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private interface Compare {
        public boolean compare(int a, int b);
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSlotUsage.class);
    
}
