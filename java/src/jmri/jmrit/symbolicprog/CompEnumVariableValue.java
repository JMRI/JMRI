package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.HashMap;

/**
 * Representation of a partial CV with a decimal value.
 * <p>
 * Extended to mask and modify specific digits (not bits) in the CV value.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007
 * @author Egbert Broerse Copyright (C) 2022
 */
public class CompEnumVariableValue extends EnumVariableValue {

    public CompEnumVariableValue(String name, String comment, String cvName,
                                 boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                                 String cvNum, String mask, int min, int max,
                                 HashMap<String, CvValue> v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, min, max, v, status, stdname);
    }

    /**
     * Get the current value (part) from the CV, using the mask _on decimal digits_ as needed.
     *
     * @param Cv         the CV of interest
     * @param maskString the (XXXVVVXX style) mask for extracting the Variable
     *                   value from this CV
     * @param maxVal     the maximum possible value for this Variable part
     * @return the current value of the Variable part
     */
    @Override
    protected int getValueInCV(int Cv, String maskString, int maxVal) {
        if (isBitMask(maskString)) {
            int val = extractVal(Cv, maskString, 0, 1);
//            if (val < _minVal) {
//                log.error("New value {} for {} is out of bounds", val, label());
//                return _minVal;
//            } else if (val > maxVal + 1) {
//                log.error("New value {} for {} is out of bounds", val, label());
//                return _maxVal;
//            } else {
                log.warn("getValueInCV returns: {}", val);
                return val;
//            }
        } else {
            log.error("Can't handle Radix mask");
            return -1;
        }
    }

    /**
     * Set a value into a CV, using the mask _on decimal digits_ as needed.
     *
     * @param oldCv      Value of the CV before this update is applied
     * @param newVal     Value for this variable (e.g. not the CV value)
     * @param maskString The (XXXVVVXX style ; NOT small int) mask for this variable in character form
     * @param maxVal     the maximum possible value for this Variable
     * @return int new value for the CV
     */
    @Override
    protected int setValueInCV(int oldCv, int newVal, String maskString, int maxVal) {
        if (isBitMask(maskString)) {
            log.debug("setValueInCV to {}. maxVal ={}", newVal, maxVal); // bounds apply to entry/value
            // super sends (maxVal - 1) but this is the index. Combo protects bounds assuming that
            // author checks offset and factor against min and max
            return insertVal(oldCv, newVal, maskString, 0, 1);
        } else {
            // see CompDecVariableValue#setValueInCV()
            log.error("Can't handle Radix mask on CompEnumVariableValue");
            return oldCv;
        }
    }

//    @Override
//    public void setValue(int value) {
//        int oldVal = getIntValue();
//        log.warn("setValue in CompEnumVariableValue from {} to {}", oldVal, value);
//        // do the math to extract Variable value from cv value
//        int varValue = getValueInCV(value, getMask(), _maxVal - 1);
//        log.warn("varValue = {}", varValue);
//        selectValue(varValue); // from here same as EnumVariableValue
//
//        if (oldVal != value || getState() == VariableValue.UNKNOWN) {
//            prop.firePropertyChange("Value", null, value);
//        }
//    }

    @Override
    public void writeChanges() {
        if (getReadOnly()) {
            log.error("unexpected writeChanges operation when readOnly is set");
        }
        setBusy(true);  // will be reset when value changes
        // change the value
        _cvMap.get(getCvNum()).write(_status);
    }

    @Override
    public void writeAll() {
        if (getReadOnly()) {
            log.error("unexpected writeAll operation when readOnly is set");
        }
        setBusy(true);  // will be reset when value changes
        // change the value
        _cvMap.get(getCvNum()).write(_status);
    }

    // clean up connections when done
    @Override
    public void dispose() {
        super.dispose();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(CompEnumVariableValue.class);

}
