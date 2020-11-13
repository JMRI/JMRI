package jmri.jmrix.loconet.logixng;

import java.util.*;

import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
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
    private Has_HasNot _hasHasNot = Has_HasNot.Has;
    private SimpleState _simpleState = SimpleState.InUse;
    private final Set<AdvancedState> _advancedStates = new HashSet<>();
    private Compare _compare = Compare.LessThan;
    private PercentPieces _percentPieces = PercentPieces.Pieces;
    private int _number = 0;
    
    
    public ExpressionSlotUsage(String sys, String user, LocoNetSystemConnectionMemo memo) {
        super(sys, user);
        _memo = memo;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return CategoryLocoNet.LOCONET;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
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
    
    public void set_Has_HasNot(Has_HasNot hasHasNot) {
        assertListenersAreNotRegistered(log, "setHasHasNot");
        _hasHasNot = hasHasNot;
    }
    
    public Has_HasNot get_Has_HasNot() {
        return _hasHasNot;
    }
    
    public void setSimpleState(SimpleState simpleState){
        assertListenersAreNotRegistered(log, "setSimpleState");
        _simpleState = simpleState;
    }
    
    public SimpleState getSimpleState() {
        return _simpleState;
    }
    
    public void setAdvancedStates(Set<AdvancedState> states) {
        assertListenersAreNotRegistered(log, "setTimerType");
        _advancedStates.clear();
        _advancedStates.addAll(states);
    }
    
    public Set<AdvancedState> getAdvancedStates() {
        return Collections.unmodifiableSet(_advancedStates);
    }
    
    public void setCompare(Compare compare){
        assertListenersAreNotRegistered(log, "setTimerType");
        _compare = compare;
    }
    
    public Compare getCompare() {
        return _compare;
    }
    
    public void setNumber(int number) {
        assertListenersAreNotRegistered(log, "setThreshold");
        _number = number;
    }
    
    public int getNumber() {
        return _number;
    }
    
    public void setPercentPieces(PercentPieces percentPieces) {
        assertListenersAreNotRegistered(log, "setPercentPieces");
        _percentPieces = percentPieces;
    }
    
    public PercentPieces getPercentPieces() {
        return _percentPieces;
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
            for (AdvancedState state : _advancedStates) {
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
                _percentPieces.toString(),
                _memo != null ? _memo.getSystemPrefix() : Bundle.getMessage("MemoNotSet")
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
                
//                slotManager.getInUseCount();
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
    
    
    
    public enum Has_HasNot {
        Has(Bundle.getMessage("HasHasNotType_Has")),
        HasNot(Bundle.getMessage("HasHasNotType_HasNot"));
        
        private final String _text;
        
        private Has_HasNot(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    public enum SimpleState {
        InUse(Bundle.getMessage("SimpleStateType_InUse"), new int[]{LnConstants.LOCO_IN_USE}),
        Free(Bundle.getMessage("SimpleStateType_Free"), new int[]{LnConstants.LOCO_FREE});
        
        private final String _text;
        private final int[] _states;
        
        private SimpleState(String text, int[] states) {
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
    
    
    public enum AdvancedState {
        InUse(LnConstants.LOCO_IN_USE, Bundle.getMessage("AdvancedStateType_InUse")),
        Idle(LnConstants.LOCO_IDLE, Bundle.getMessage("AdvancedStateType_Idle")),
        Common(LnConstants.LOCO_COMMON, Bundle.getMessage("AdvancedStateType_Common")),
        Free(LnConstants.LOCO_FREE, Bundle.getMessage("AdvancedStateType_Free"));
        
        private final int _state;
        private final String _text;
        
        private AdvancedState(int state, String text) {
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
    
    
    public enum Compare {
        LessThan(Bundle.getMessage("CompareType_LessThan"), (int a, int b) -> a < b),
        LessThanOrEqual(Bundle.getMessage("CompareType_LessThanOrEqual"), (int a, int b) -> a <= b),
        Equal(Bundle.getMessage("CompareType_Equal"), (int a, int b) -> a == b),
        NotEqual(Bundle.getMessage("CompareType_NotEqual"), (int a, int b) -> a != b),
        GreaterThanOrEqual(Bundle.getMessage("CompareType_GreaterThanOrEqual"), (int a, int b) -> a >= b),
        GreaterThan(Bundle.getMessage("CompareType_GreaterThan"), (int a, int b) -> a > b);
        
        private final String _text;
        private final CompareIntegers _compare;
        
        private Compare(String text, CompareIntegers compare) {
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
    
    
    public enum PercentPieces {
        Percent(Bundle.getMessage("PercentPiecesType_Percent")),
        Pieces(Bundle.getMessage("PercentPiecesType_Pieces"));
        
        private final String _text;
        
        private PercentPieces(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
        
    }
    
    
    private interface CompareIntegers {
        public boolean compare(int a, int b);
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSlotUsage.class);
    
}
