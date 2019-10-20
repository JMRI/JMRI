package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Like {@link SplitVariableValue}, except that the string representation is in
 * hexadecimal
 * <br><br>
 * All the attributes of {@link SplitVariableValue} are inherited.
 * <br><br>
 * An optional {@code case} attribute can be used to force the hex characters to
 * display in {@code "upper"} or {@code "lower"} case.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2004, 2013, 2014
 * @author Dave Heap Copyright (C) 2016
 */
public class SplitHexVariableValue extends SplitVariableValue {

    public SplitHexVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname, pSecondCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
    }

    String _case;

    @Override
    public void stepOneActions(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        _case = extra1;
        if (extra3 != null) {
            _minVal = getValueFromText(extra3);
        }
        if (extra4 != null) {
            _maxVal = getValueFromText(extra4);
        }
    }

    @Override
    long getValueFromText(String s) {
        return (Long.parseUnsignedLong(s, 16));
    }

    @Override
    String getTextFromValue(long v) {
        String ret = Long.toHexString(v);
        if (_case.equals("upper")) {
            ret = ret.toUpperCase();
        } else if (_case.equals("lower")) {
            ret = ret.toLowerCase();
        }
        return ret;
    }

    // initialize logging
//    private final static Logger log = LoggerFactory.getLogger(SplitHexVariableValue.class);
}
