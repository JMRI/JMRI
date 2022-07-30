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
public class CompDecVariableValue extends DecVariableValue {

    public CompDecVariableValue(String name, String comment, String cvName,
                                boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                                String cvNum, String mask,
                                HashMap<String, CvValue> v, JLabel status, String stdname, int min, int max,
                                int offset, int factor) {
        // specify min, max value explicitly.
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, min, max, v, status, stdname);
        _mask = mask;
        mFactor = factor;
        mOffset = offset;
    }

    String _mask; // full string as provided, use _maskArray to access one of multiple masks
    int mFactor;
    int mOffset;

    /**
     * Get the current value (part) from the CV, using the mask _on digits_ as needed.
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
//            int decOffset = offsetVal(maskString)*10;
//            int maskedDec;
            return maskedDec(Cv, maskString);
        } else {
            log.error("Can't handle Radix mask");
            return -1;
        }
    }

    /**
     * Fetch the relevant digits from a value, using a String bit mask like XXXVVVXX.
     *
     * @param Cv the value to process
     * @param maskString the textual (XXXVVVXX style) mask
     * @return digits that remain after masking
     */
    protected int maskedDec(int Cv, String maskString) {
        // convert String mask to int
        int length = 0;
        for (int i = 0; i < maskString.length(); i++) {
            try {
                if (maskString.charAt(i) == 'V') {
                    length++;
                }
            } catch (StringIndexOutOfBoundsException e) {
                log.error("mask \"{}\" could not be handled for variable {}", maskString, label());
            }
        }
        double decOffset = Cv/Math.pow(10,offsetVal(maskString))%Math.pow(10, length);
        return (int) decOffset;
    }

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
    private final static Logger log = LoggerFactory.getLogger(CompDecVariableValue.class);

}
