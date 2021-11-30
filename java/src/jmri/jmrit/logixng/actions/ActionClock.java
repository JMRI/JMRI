package jmri.jmrit.logixng.actions;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.ThreadingUtil;

/**
 * This action provides the ability to set the fast clock time and start and stop the fast clock.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionClock extends AbstractDigitalAction {

    private ClockState _clockState = ClockState.SetClock;
    private int _clockTime = 0;

    public ActionClock(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionClock copy = new ActionClock(sysName, userName);
        copy.setComment(getComment());
        copy.setBeanState(_clockState);
        copy.setClockTime(_clockTime);
        return manager.registerAction(copy);
    }

    public void setBeanState(ClockState state) {
        _clockState = state;
    }

    public ClockState getBeanState() {
        return _clockState;
    }

    public void setClockTime(int minutes) {
        _clockTime = minutes;
    }

    public int getClockTime() {
        return _clockTime;
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
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        ClockState theState = _clockState;
        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            switch(theState) {
                case SetClock:
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(InstanceManager.getDefault(jmri.Timebase.class).getTime());
                    cal.set(Calendar.HOUR_OF_DAY, _clockTime / 60);
                    cal.set(Calendar.MINUTE, _clockTime % 60);
                    cal.set(Calendar.SECOND, 0);
                    InstanceManager.getDefault(jmri.Timebase.class).userSetTime(cal.getTime());
                    break;
                case StartClock:
                    InstanceManager.getDefault(jmri.Timebase.class).setRun(true);
                    break;
                case StopClock:
                    InstanceManager.getDefault(jmri.Timebase.class).setRun(false);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid clock state: " + theState.name());
            }
        });
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
        return Bundle.getMessage(locale, "ActionClock_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        if (getBeanState() == ClockState.SetClock) {
            return Bundle.getMessage(locale, "ActionClock_LongTime", _clockState._text, ActionClock.formatTime(getClockTime()));
        }
        return Bundle.getMessage(locale, "ActionClock_Long", _clockState._text);
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

    public enum ClockState {
        SetClock(Bundle.getMessage("ActionClock_SetClock")),
        StartClock(Bundle.getMessage("ActionClock_StartClock")),
        StopClock(Bundle.getMessage("ActionClock_StopClock"));

        private final String _text;

        private ClockState(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionPower.class);

}
