package jmri.jmrit.symbolicprog;

import java.util.HashMap;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Representation of a short address (CV1).
 * <p>
 * This is a decimal value, extended to modify the other CVs when written. The
 * CVs to be modified and their new values are stored in two arrays for
 * simplicity.
 * <p>
 *
 * The NMRA has decided that writing CV1 causes other CVs to update within the
 * decoder (CV19 for consisting, CV29 for short/long address). We want DP to
 * overwrite those _after_ writing CV1, so that the DP values are forced to be
 * the correct ones.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007
 *
 */
public class ShortAddrVariableValue extends DecVariableValue {

    public ShortAddrVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask,
            HashMap<String, CvValue> v, JLabel status, String stdname) {
        // specify min, max value explicitly.
        // short address 0 = alternate power source as per S9.2.2.
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, 0, 127, v, status, stdname);

        // add default overwrites as per NMRA spec
        firstFreeSpace = 0;
        setModifiedCV("19");         // consisting
        setModifiedCV("29");         // control bits
    }

    /**
     * Register a CV to be modified regardless of current value
     */
    public void setModifiedCV(String cvNum) {
        if (firstFreeSpace >= maxCVs) {
            log.error("too many CVs registered for changes!");
            return;
        }
        cvNumbers[firstFreeSpace] = cvNum;
        newValues[firstFreeSpace] = -10;
        firstFreeSpace++;
    }

    /**
     * Change CV values due to change in short address
     */
    private void updateCvForAddrChange() {
        for (int i = 0; i < firstFreeSpace; i++) {
            CvValue cv = _cvMap.get(cvNumbers[i]);
            if (cv == null) {
                continue;  // if CV not present this decoder...
            }
            if (!cvNumbers[i].equals(cv.number())) {
                log.error("CV numbers don't match: "
                        + cvNumbers[i] + " " + cv.number());
            }
            cv.setToWrite(true);
            cv.setState(EDITED);
            if (log.isDebugEnabled()) {
                log.debug("Mark to write " + cv.number());
            }
        }
    }

    int firstFreeSpace = 0;
    static final int maxCVs = 20;
    String[] cvNumbers = new String[maxCVs];
    int[] newValues = new int[maxCVs];

    @Override
    public void writeChanges() {
        if (getReadOnly()) {
            log.error("unexpected writeChanges operation when readOnly is set");
        }
        setBusy(true);  // will be reset when value changes
        // mark other CVs as possibly needing write
        updateCvForAddrChange();
        // and change the value of this one
        _cvMap.get(getCvNum()).write(_status);
    }

    @Override
    public void writeAll() {
        if (getReadOnly()) {
            log.error("unexpected writeAll operation when readOnly is set");
        }
        setBusy(true);  // will be reset when value changes
        // mark other CVs as possibly needing write
        updateCvForAddrChange();
        // and change the value of this one
        _cvMap.get(getCvNum()).write(_status);
    }

    // clean up connections when done
    @Override
    public void dispose() {
        super.dispose();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ShortAddrVariableValue.class);

}
