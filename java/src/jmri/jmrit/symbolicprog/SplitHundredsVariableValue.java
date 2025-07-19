package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Like {@link SplitVariableValue}, except that the string representation is split out
 * into 100's in each CV (up to two in this first implementation)
 * <br><br>
 * All the attributes of {@link SplitVariableValue} are inherited.
 *
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2004, 2013, 2014
 * @author Dave Heap Copyright (C) 2016
 */
public class SplitHundredsVariableValue extends SplitVariableValue {

    public SplitHundredsVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname, pSecondCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
    }

    @Override
    public void stepOneActions(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {

        if (extra3 != null) {
            _minVal = getValueFromText(extra3);
        }
        if (extra4 != null) {
            _maxVal = getValueFromText(extra4);
        }
    }

    @Override
    long getValueFromText(String s) {
        if (s.isEmpty()) return 0L;
        long val = Long.parseUnsignedLong(s);
        long result = 0;
        long multiplier = 1L;
        final long INRADIX = 100L;
        final long OUTRADIX = 0x100;
        while (val > 0) {
            long digits = val % INRADIX;
            val = val/INRADIX;
            result = result + multiplier*digits;
            multiplier = multiplier * OUTRADIX;
        }
        return result;
    }

    @Override
    String getTextFromValue(long val) {
        String result = "";

        final long INRADIX = 0x100;
        while (val > 0) {
            long digits = val % INRADIX;
            val = val/INRADIX;
            result = "" + String.format("%02d", digits) + result;// that's a String prepend operation
        }
        result = "0"+result;  // never blank, even if the input is zero
        // remove leading zeros for appearance sake
        while (result.startsWith("0")) {
            if (result.length() <= 1) break;  // leave one zero if thats all there is
            result = result.substring(1);
        }
        return result;
    }

    /**
     * Set value from a String value.
     *
     * @param value a string representing the unsigned hundreds-based value to be set
     */
    @Override
    public void setValue(String value) {
        try {
            var result = getValueFromText(value);
            setLongValue(result);
        } catch (NumberFormatException e) {
            log.warn("handling non-numeric value \"{}\"", value);
            return;
        }

    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SplitHundredsVariableValue.class);
}
