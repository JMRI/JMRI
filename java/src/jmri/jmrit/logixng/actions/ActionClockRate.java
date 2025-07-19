package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.LogixNG_SelectDouble;
import jmri.jmrit.logixng.util.parser.ParserException;
import static jmri.jmrit.simpleclock.SimpleTimebase.MAXIMUM_RATE;
import static jmri.jmrit.simpleclock.SimpleTimebase.MINIMUM_RATE;
import jmri.util.ThreadingUtil;

/**
 * This action provides the ability to set the fast clock speed.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionClockRate extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectEnum<ClockState> _selectEnum =
            new LogixNG_SelectEnum<>(this, ClockState.values(), ClockState.SetClockRate, this);
    private final LogixNG_SelectDouble _selectSpeed =
            new LogixNG_SelectDouble(this, 3, this, new DefaultFormatterParserValidator());


    public ActionClockRate(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionClockRate copy = new ActionClockRate(sysName, userName);
        copy.setComment(getComment());
        _selectEnum.copy(copy._selectEnum);
        _selectSpeed.copy(copy._selectSpeed);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectEnum<ClockState> getSelectEnum() {
        return _selectEnum;
    }

    public LogixNG_SelectDouble getSelectSpeed() {
        return _selectSpeed;
    }

    /**
     * Convert speed to an I18N decimal string.
     * @param locale The Locale to use for the String conversion.
     * @param speed The speed
     * @return speed formatted as %1.3f
     */
    public static String formatSpeed(Locale locale, double speed) {
        return String.format(locale,"%1.3f", speed);
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

        ClockState theState = _selectEnum.evaluateEnum(getConditionalNG());
        double theValue = _selectSpeed.evaluateValue(getConditionalNG());

        jmri.Timebase timebase = InstanceManager.getDefault(jmri.Timebase.class);

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            switch(theState) {
                case SetClockRate:
                    try {
                        timebase.userSetRate(theValue);
                    } catch (TimebaseRateException e) {
                        // Do nothing. This error is already logged as an error
                    }
                    break;

                case IncreaseClockRate:
                    try {
                        timebase.userSetRate(timebase.userGetRate() + theValue);
                    } catch (TimebaseRateException e) {
                        // Do nothing. This error is already logged as an error
                    }
                    break;

                case DecreaseClockRate:
                    try {
                        timebase.userSetRate(timebase.userGetRate() - theValue);
                    } catch (TimebaseRateException e) {
                        // Do nothing. This error is already logged as an error
                    }
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
        return Bundle.getMessage(locale, "ActionClockRate_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String value;
        if (_selectSpeed.isDirectAddressing()) {
            value = formatSpeed( locale, _selectSpeed.getValue());
        } else {
            value = _selectSpeed.getDescription(locale);
        }
        if (_selectEnum.isDirectAddressing()) {
            if (_selectEnum.getEnum() == ClockState.SetClockRate) {
                return Bundle.getMessage(locale, "ActionClockRate_LongTo", _selectEnum.getDescription(locale), value);
            }
            return Bundle.getMessage(locale, "ActionClockRate_LongWith", _selectEnum.getDescription(locale), value);
        } else {
            return Bundle.getMessage(locale, "ActionClockRate_LongTo", _selectEnum.getDescription(locale), value);
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
        _selectSpeed.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectEnum.unregisterListeners();
        _selectSpeed.unregisterListeners();
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
        SetClockRate(Bundle.getMessage("ActionClockRate_SetClockRate")),
        IncreaseClockRate(Bundle.getMessage("ActionClockRate_IncreaseClockRate")),
        DecreaseClockRate(Bundle.getMessage("ActionClockRate_DecreaseClockRate"));

        private final String _text;

        private ClockState(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    private static class DefaultFormatterParserValidator
            extends LogixNG_SelectDouble.DefaultFormatterParserValidator {

        @Override
        public double getInitialValue() {
            return 1.0;
        }

        @Override
        public double parse(String str) {
            try {
                double value = Double.parseDouble(str);
                if (value < MINIMUM_RATE || value > MAXIMUM_RATE) {
                    return MINIMUM_RATE;
                }
                return value;
            } catch (NumberFormatException ex) {
                return MINIMUM_RATE;
            }
        }

        @Override
        public String validate(String str) {
            try {
                double value = Double.parseDouble(str);
                if (value < MINIMUM_RATE || value > MAXIMUM_RATE) {
                    return Bundle.getMessage("ActionClockRate_RangeError",
                            MINIMUM_RATE, MAXIMUM_RATE);
                }
                return null;
            } catch (NumberFormatException ex) {
                return Bundle.getMessage("ActionClockRate_ParseError", str);
            }
        }

    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionPower.class);

}
