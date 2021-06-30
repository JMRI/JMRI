package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.TimerUtil;

/**
 * This expression is a clock.
 *
 * @author Daniel Bergqvist Copyright 2020
 * @author Dave Sand Copyright 2021
 */
public class ExpressionClock extends AbstractDigitalExpression implements PropertyChangeListener {

    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private Type _type = Type.FastClock;
    private Timebase _fastClock;
    private int _beginTime = 0;
    private int _endTime = 0;

    TimerTask timerTask = null;
    private int milisInAMinute = 60000;


    public ExpressionClock(String sys, String user) {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionClock copy = new ExpressionClock(sysName, userName);
        copy.setComment(getComment());
        copy.set_Is_IsNot(_is_IsNot);
        copy.setType(_type);
        copy.setRange(_beginTime, _endTime);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
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
            _fastClock = InstanceManager.getDefault(jmri.Timebase.class);
        } else {
            _fastClock = null;
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

    /**
     * Convert minutes since midnight to hh:mm.
     * @param minutes The number of minutes from 0 to 1439.
     * @return time formatted as hh:mm.
     */
    public static String formatTime(int minutes) {
        String hhmm = "00:00";
        if (minutes >= 0 && minutes < 1440) {
            hhmm = String.format("%02d:%02d",
                    minutes / 60,
                    minutes % 60);
        }
        return hhmm;
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() {
        boolean result;

        Calendar currentTime = null;

        switch (_type) {
            case SystemClock:
                currentTime = Calendar.getInstance();
                break;

            case FastClock:
                if (_fastClock == null) return false;
                currentTime = Calendar.getInstance();
                currentTime.setTime(_fastClock.getTime());
                break;

            default:
                throw new UnsupportedOperationException("_type has unknown value: " + _type.name());
        }

        int currentMinutes = (currentTime.get(Calendar.HOUR_OF_DAY) * 60) + currentTime.get(Calendar.MINUTE);
        // check if current time is within range specified
        if (_beginTime <= _endTime) {
            // range is entirely within one day
            result = (_beginTime <= currentMinutes) && (currentMinutes <= _endTime);
        } else {
            // range includes midnight
            result = _beginTime <= currentMinutes || currentMinutes <= _endTime;
        }

        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return result;
        } else {
            return !result;
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
        return Bundle.getMessage(locale, "Clock_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        switch (_type) {
            case SystemClock:
                return Bundle.getMessage(locale, "Clock_Long_SystemClock", _is_IsNot.toString(),
                        ExpressionClock.formatTime(_beginTime),
                        ExpressionClock.formatTime(_endTime));

            case FastClock:
                return Bundle.getMessage(locale, "Clock_Long_FastClock", _is_IsNot.toString(),
                        ExpressionClock.formatTime(_beginTime),
                        ExpressionClock.formatTime(_endTime));

            default:
                throw new RuntimeException("Unknown value of _timerType: "+_type.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc}
     * The SystemClock listener creates a timer on the first call.  Subsequent calls
     * enabled timer processing.
     */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            switch (_type) {
                case SystemClock:
                    scheduleTimer();
                    break;

                case FastClock:
                    _fastClock.addPropertyChangeListener("time", this);
                    break;

                default:
                    throw new UnsupportedOperationException("_type has unknown value: " + _type.name());
            }

            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc}
     * The SystemClock timer flag is set false to suspend processing of timer events.  The
     * timer keeps running for the duration of the JMRI session.
     */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            switch (_type) {
                case SystemClock:
                    if (timerTask != null) timerTask.cancel();
                    break;

                case FastClock:
                    if (_fastClock != null) _fastClock.removePropertyChangeListener("time", this);
                    break;

                default:
                    throw new UnsupportedOperationException("_type has unknown value: " + _type.name());
            }

            _listenersAreRegistered = false;
        }
    }

    private void scheduleTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                propertyChange(null);
            }
        };
        TimerUtil.schedule(timerTask, System.currentTimeMillis() % milisInAMinute, milisInAMinute);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        if (timerTask != null) timerTask.cancel();
    }

    public enum Type {
        FastClock(Bundle.getMessage("ClockTypeFastClock")),
        SystemClock(Bundle.getMessage("ClockTypeSystemClock"));

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
