// HexVariableValue.java
package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LIke DecVariableValue, except that the string representation is in
 * hexadecimal
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2014
 * @author	Dave Heap Copyright (C) 2015
 * @version $Revision$
 */
public class HexVariableValue extends DecVariableValue {

    public HexVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname);
    }

    @Override
    int textToValue(String s) {
        return (Integer.valueOf(s, 16));
    }

    @Override
    String valueToText(int v) {
        return (Integer.toHexString(v));
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(HexVariableValue.class.getName());

}
