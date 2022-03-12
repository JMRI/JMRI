package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.TimerUtil;

/**
 * This expression returns the number of minutes since midnight for the fast
 * clock or the system clock.
 *
 * @author Daniel Bergqvist Copyright 2020
 * @author Dave Sand Copyright 2021
 */
public class TimeSinceMidnight extends AbstractAnalogExpression implements PropertyChangeListener {

    private Type _type = Type.FastClock;
    private Timebase _fastClock;

    TimerTask timerTask = null;
    private final int millisInAMinute = 60000;


    public TimeSinceMidnight(String sys, String user) {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        AnalogExpressionManager manager = InstanceManager.getDefault(AnalogExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        TimeSinceMidnight copy = new TimeSinceMidnight(sysName, userName);
        copy.setComment(getComment());
        copy.setType(_type);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
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

    /** {@inheritDoc} */
    @Override
    public double evaluate() {
        Calendar currentTime = null;

        switch (_type) {
            case SystemClock:
                currentTime = Calendar.getInstance();
                break;

            case FastClock:
                if (_fastClock == null) return 0;
                currentTime = Calendar.getInstance();
                currentTime.setTime(_fastClock.getTime());
                break;

            default:
                throw new UnsupportedOperationException("_type has unknown value: " + _type.name());
        }

        return (currentTime.get(Calendar.HOUR_OF_DAY) * 60) + currentTime.get(Calendar.MINUTE);
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
        return Bundle.getMessage(locale, "TimeSinceMidnight_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        switch (_type) {
            case SystemClock:
                return Bundle.getMessage(locale, "TimeSinceMidnight_Long_SystemClock");

            case FastClock:
                return Bundle.getMessage(locale, "TimeSinceMidnight_Long_FastClock");

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
        TimerUtil.schedule(timerTask, System.currentTimeMillis() % millisInAMinute, millisInAMinute);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeSinceMidnight.class);

}
