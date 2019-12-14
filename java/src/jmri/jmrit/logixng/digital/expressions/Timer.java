package jmri.jmrit.logixng.digital.expressions;

import java.util.Locale;
import java.util.TimerTask;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This expression is a timer.
 * It can be of these types:
 * * Wait some time and then return 'true' once. Timer is restarted upon reset.
 * * Wait some time and then return 'true' until reset.
 * * Wait some time and then return 'true' once. Once evaluate() is called, the timer is reset and starts again.
 * * Wait some time and then return 'true'. Wait some time and then return 'false'. Once evaluate() is called, the timer is reset and starts again.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class Timer extends AbstractDigitalExpression {

    // This variable is protected since the test class TimerTest replaces it
    // with its own timer class.
//    protected java.util.Timer _timer;
    private final java.util.Timer _timer;
    private TimerTask _timerTask;
    private TimerType _timerType = TimerType.WAIT_ONCE_TRIG_ONCE;
    private boolean _listenersAreRegistered = false;
    private boolean _hasTimePassed = false;
    private boolean _onOrOff = false;
    private long _delayOff = 0;
    private long _delayOn = 0;
    
    
    public Timer(String sys, String user) {
        super(sys, user);
        _timer = new java.util.Timer("LogixNG ExpressionTimer timer thread", true);
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
    
    public void setTimerType(TimerType timerType) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setTimerType must not be called when listeners are registered");
            log.error("setTimerType must not be called when listeners are registered", e);
            throw e;
        }
        _timerType = timerType;
    }
    
    public TimerType getTimerType() {
        return _timerType;
    }
    
    public void setTimerDelay(long delayOff, long delayOn) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setTimerDelay must not be called when listeners are registered");
            log.error("setTimerDelay must not be called when listeners are registered", e);
            throw e;
        }
        _delayOff = delayOff;
        _delayOn = delayOn;
    }
    
    public long getTimerDelayOff() {
        return _delayOff;
    }
    
    public long getTimerDelayOn() {
        return _delayOn;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() {
        if (_hasTimePassed) {
            switch (_timerType) {
                case WAIT_ONCE_TRIG_ONCE:
                    _hasTimePassed = false;
                    return true;
                    
                case WAIT_ONCE_TRIG_UNTIL_RESET:
                    // Don't clear _hasTimePassed since we want to keep
                    // returning true until reset()
                    return true;
                    
                case REPEAT_SINGLE_DELAY:
                    _hasTimePassed = false;
                    startTimer();
                    return true;
                    
                case REPEAT_DOUBLE_DELAY:
                    _hasTimePassed = false;
                    _onOrOff = ! _onOrOff;
                    startTimer();
                    return true;
                    
                default:
                    throw new RuntimeException("_timerType has unknown value: "+_timerType.name());
            }
        }
        
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void reset() {
        stopTimer();
        startTimer();
    }
    
    private void startTimer() {
        final Timer t = this;
        
        // Ensure timer is not running
        if (_timerTask != null) _timerTask.cancel();
//        _timer.cancel();
        
        // Clear flag
        _hasTimePassed = false;
        
        _timerTask = new TimerTask() {
            @Override
            public void run() {
                t.getConditionalNG().execute();
            }
        };
        
        switch (_timerType) {
            case WAIT_ONCE_TRIG_ONCE:
                // fall through
            case WAIT_ONCE_TRIG_UNTIL_RESET:
                // fall through
            case REPEAT_SINGLE_DELAY:
                _timer.schedule(_timerTask, _delayOff);
                break;
                
            case REPEAT_DOUBLE_DELAY:
                _timer.schedule(_timerTask, _onOrOff ? _delayOn : _delayOff);
                break;
                
            default:
                throw new RuntimeException("_timerType has unknown value: "+_timerType.name());
        }
    }
    
    private void stopTimer() {
        if (_timerTask != null) _timerTask.cancel();
//        _timer.cancel();
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
        return Bundle.getMessage(locale, "Timer_Short");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        switch (_timerType) {
            case WAIT_ONCE_TRIG_ONCE:
                return Bundle.getMessage(locale, "Timer_Long_WaitOnceTrigOnce", _delayOff);
                
            case WAIT_ONCE_TRIG_UNTIL_RESET:
                return Bundle.getMessage(locale, "Timer_Long_WaitOnceTrigUntilReset", _delayOff);
                
            case REPEAT_SINGLE_DELAY:
                return Bundle.getMessage(locale, "Timer_Long_RepeatSingleDelay", _delayOff);
                
            case REPEAT_DOUBLE_DELAY:
                return Bundle.getMessage(locale, "Timer_Long_RepeatDoubleDelay", _delayOff, _delayOn);
                
            default:
                throw new RuntimeException("Unknown value of _timerType: "+_timerType.name());
        }
//        return Bundle.getMessage(locale, "Timer_Long");
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_delayOff != 0)) {
            _listenersAreRegistered = true;
//            startTimer();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        stopTimer();
        _listenersAreRegistered = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        if (_timerTask != null) _timerTask.cancel();
//        _timer.cancel();
    }
    
    
    
    public enum TimerType {
        WAIT_ONCE_TRIG_ONCE,
        WAIT_ONCE_TRIG_UNTIL_RESET,
        REPEAT_SINGLE_DELAY,
        REPEAT_DOUBLE_DELAY;
/*        
        WAIT_ONCE_TRIG_ONCE(Bundle.getMessage("TimerType_WaitOnceTrigOnce")),
        WAIT_ONCE_TRIG_UNTIL_RESET(Bundle.getMessage("TimerType_WaitOnceTrigUntilReset")),
        REPEAT_SINGLE_DELAY(Bundle.getMessage("TimerType_RepeatSingleDelay")),
        REPEAT_DOUBLE_DELAY(Bundle.getMessage("TimerType_RepeatDoubleDelay"));
        
        private final String _text;
        
        private TimerType(String text) {
            this._text = text;
        }
        
        @Override
        public String toString() {
            return _text;
        }
*/        
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(Timer.class);
    
}
