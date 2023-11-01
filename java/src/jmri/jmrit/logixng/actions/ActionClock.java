package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.LogixNG_SelectInteger;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.ThreadingUtil;

/**
 * This action provides the ability to set the fast clock time and start and stop the fast clock.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionClock extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectEnum<ClockState> _selectEnum =
            new LogixNG_SelectEnum<>(this, ClockState.values(), ClockState.SetClock, this);
    private final LogixNG_SelectInteger _selectValue =
            new LogixNG_SelectInteger(this, this, new TimeFormatterParserValidator());


    public ActionClock(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionClock copy = new ActionClock(sysName, userName);
        copy.setComment(getComment());
        _selectEnum.copy(copy._selectEnum);
        _selectValue.copy(copy._selectValue);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectEnum<ClockState> getSelectEnum() {
        return _selectEnum;
    }

    public LogixNG_SelectInteger getSelectTime() {
        return _selectValue;
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

        ClockState theState = _selectEnum.evaluateEnum(getConditionalNG());
        int theValue = _selectValue.evaluateValue(getConditionalNG());

        jmri.Timebase timebase = InstanceManager.getDefault(jmri.Timebase.class);

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            switch(theState) {
                case SetClock:
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(timebase.getTime());
                    cal.set(Calendar.HOUR_OF_DAY, theValue / 60);
                    cal.set(Calendar.MINUTE, theValue % 60);
                    cal.set(Calendar.SECOND, 0);
                    timebase.userSetTime(cal.getTime());
                    break;

                case StartClock:
                    timebase.setRun(true);
                    break;

                case StopClock:
                    timebase.setRun(false);
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
        String value;
        if (_selectValue.isDirectAddressing()) {
            value = formatTime(_selectValue.getValue());
        } else {
            value = _selectValue.getDescription(locale);
        }
        if (_selectEnum.isDirectAddressing()) {
            if (_selectEnum.getEnum() == ClockState.SetClock) {
                return Bundle.getMessage(locale, "ActionClock_LongTime", _selectEnum.getDescription(locale), value);
            }
            return Bundle.getMessage(locale, "ActionClock_Long", _selectEnum.getDescription(locale), value);
        } else {
            return Bundle.getMessage(locale, "ActionClock_LongTime", _selectEnum.getDescription(locale), value);
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
        _selectEnum.registerListeners();
        _selectValue.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectEnum.unregisterListeners();
        _selectValue.unregisterListeners();
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


    private static class TimeFormatterParserValidator
            implements LogixNG_SelectInteger.FormatterParserValidator {

        @Override
        public int getInitialValue() {
            return 0;
        }

        @Override
        public String format(int value) {
            return ActionClock.formatTime(value);
        }

        @Override
        public int parse(String str) {
            int minutes;

            try {
                minutes = Integer.parseInt(str);
                if (minutes < 0 || minutes > 1439) {
                    return 0;
                }
                return minutes;
            } catch (NumberFormatException e) {
                // Do nothing
            }

            LocalTime newHHMM;
            try {
                newHHMM = LocalTime.parse(str.trim(), DateTimeFormatter.ofPattern("H:mm"));
                minutes = newHHMM.getHour() * 60 + newHHMM.getMinute();
                if (minutes < 0 || minutes > 1439) {
                    return 0;
                }
                return minutes;
            } catch (DateTimeParseException ex) {
                return 0;
            }
        }

        @Override
        public String validate(String str) {
            int minutes;

            try {
                minutes = Integer.parseInt(str);
                if (minutes < 0 || minutes > 1439) {
                    return Bundle.getMessage("ActionClock_RangeError");
                }
                return null;
            } catch (NumberFormatException e) {
                // Do nothing
            }

            LocalTime newHHMM;
            try {
                newHHMM = LocalTime.parse(str.trim(), DateTimeFormatter.ofPattern("H:mm"));
                minutes = newHHMM.getHour() * 60 + newHHMM.getMinute();
                if (minutes < 0 || minutes > 1439) {
                    return Bundle.getMessage("ActionClock_RangeError");
                }
            } catch (DateTimeParseException ex) {
                return Bundle.getMessage("ActionClock_ParseError", ex.getParsedString());
            }
            return null;
        }

    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionPower.class);

}
