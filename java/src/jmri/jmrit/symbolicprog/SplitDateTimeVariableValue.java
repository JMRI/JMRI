package jmri.jmrit.symbolicprog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.HashMap;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Like {@link SplitVariableValue}, except that the string representation is
 * a date and time relative to a given epoch start date.
 * <ul>
 * <li>
 * A {@code base} attribute is required to indicate the epoch (zero) date and
 * must be in XML dateTime format {@code "YYYY-MM-DDThh:mm:ss"}(all components
 * are required).
 * For example, the RailCom (S9.3.2) epoch is "2000-01-01T00:00:00",
 * while the Java epoch is "1970-01-01T00:00:00"
 * </li>
 * <li>
 * A {@code unit} attribute specifies the time unit of the value stored in the
 * CVs. The default is {@code "Seconds"} and the available units are
 * {@code "Nanos"}, {@code "Micros"}, {@code "Millis"}, {@code "Seconds"},
 * {@code "Minutes"}, {@code "Hours"}, {@code "HalfDays"}, {@code "Days"},
 * {@code "Weeks"}, {@code "Months"}, {@code "Years"}, {@code "Decades"},
 * {@code "Centuries"}, {@code "Millennia"} as per
 * {@link java.time.temporal.ChronoUnit#values()}
 * </li>
 * <li>
 * A {@code factor} attribute can be used to specify that the stored value is in
 * multiples of a {@code unit}. For example, if the stored value is in tenths of
 * a second, use {@code unit="Millis", factor="100"}. Large values of
 * {@code factor} should be avoided, due to the possibility of multiplication
 * overflow.
 * </li>
 * <li>
 * A {@code display} attribute specifies the what is returned in the string representation
 * The default is to return both date and time and the available displays are
 * {@code "dateOnly"}, {@code "timeOnly"} and {@code "default"}.
 * </li>
 * </ul>
 * Due to the difficulties in parsing date and time values, together with the
 * loss of information in the display format and back conversion, the string
 * representation will always be {@code readOnly}, even though the underlying CV
 * values may not be {@code readOnly}.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2004, 2013, 2014
 * @author Dave Heap Copyright (C) 2016
 */
public class SplitDateTimeVariableValue extends SplitVariableValue {

    public SplitDateTimeVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname, pSecondCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
    }

    LocalDateTime base;
    long factor;
    String unit;
    String display;

    @Override
    public void stepOneActions(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        base = LocalDateTime.parse(extra1);
        factor = Long.parseLong(extra2);
        unit = extra3;
        display = extra4;
        _minVal = 0;
        _maxVal = ~0;
    }

    @Override
    public void stepTwoActions() {
        log.debug(_name + " SplitDateTimeVariableValue stepTwoActions");
        super.stepTwoActions(); // need to do base level checks
        _columns = cvCount * 4; //new default column width
        switch (display) {
            case "dateOnly":
            case "timeOnly":
                _columns = cvCount * 2; //new column width
                break;
            default:
                _columns = cvCount * 4; //new column width
        }
    }

    /**
     * Since we are not parsing text to value, we need to save the current value
     * to return with {@link #getValueFromText getValueFromText}.
     */
    long storedValue = 0;

    @Override
    long getValueFromText(String s) {
        return storedValue;
    }

    @Override
    String getTextFromValue(long v) {
        storedValue = v; // save the current value
        for (ChronoUnit theUnit : ChronoUnit.values()) {
            if (theUnit.toString().equals(unit)) {
                return getTextFromDateTime(base.plus((v * factor), ChronoUnit.valueOf(theUnit.name())));
            }
        }
        throw new UnsupportedTemporalTypeException("Invalid time unit '" + unit + "'.");
    }

    /**
     *
     * @param dateTime a {@link LocalDateTime} value.
     * @return a string representation of {@code dateTime}.
     */
    String getTextFromDateTime(LocalDateTime dateTime) {
        switch (display) {
            case "dateOnly":
                return dateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
            case "timeOnly":
                return dateTime.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM));
            default:
                return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
        }
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SplitDateTimeVariableValue.class);

}
