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
    private final TimerType _timerType = TimerType.WAIT_ONCE_TRIG_ONCE;
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
    
    public void setTimerDelay(long delayOff, long delayOn) {
        if (_listenersAreRegistered) {
            RuntimeException e = new RuntimeException("setSensor must not be called when listeners are registered");
            log.error("setSensor must not be called when listeners are registered", e);
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
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                timerTaskMethod();
            }
        };
        
        switch (_timerType) {
            case WAIT_ONCE_TRIG_ONCE:
                //$FALL-THROUGH$
            case WAIT_ONCE_TRIG_UNTIL_RESET:
                //$FALL-THROUGH$
            case REPEAT_SINGLE_DELAY:
                _timer.schedule(timerTask, _delayOff);
                break;
                
            case REPEAT_DOUBLE_DELAY:
                _timer.schedule(timerTask, _onOrOff ? _delayOn : _delayOff);
                break;
                
            default:
                throw new RuntimeException("_timerType has unknown value: "+_timerType.name());
        }
    }
    
    private void stopTimer() {
        _timer.cancel();
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
        return Bundle.getMessage(locale, "Timer_Long");
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
            startTimer();
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
        _timer.cancel();
    }
    
    
    public void timerTaskMethod() {
        System.out.println("Task performed: Daniel");
//        System.out.println("Task performed on " + new Date());
    }
    
    
    
    // * Wait some time and then return 'true' once. Timer is restarted upon reset.
    // * Wait some time and then return 'true' until reset.
    // * Wait some time and then return 'true' once. Once evaluate() is called, the timer is reset and starts again.
    
    public enum TimerType {
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
        
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(Timer.class);
    
}
