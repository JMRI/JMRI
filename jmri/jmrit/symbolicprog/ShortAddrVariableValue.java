// ShortAddrVariableValue.java

package jmri.jmrit.symbolicprog;

import java.util.*;

import javax.swing.*;

/**
 * Representation of a short address (CV1).
 * <P>
 * This is a decimal value, extended to modify the other CVs when
 * written.  The CVs to be modified and there new values are
 * stored in two arrays for simplicity.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version             $Revision: 1.5 $
 *
 */
public class ShortAddrVariableValue extends DecVariableValue {

    public ShortAddrVariableValue(String name, String comment, boolean readOnly,
                            int cvNum, String mask,
                            Vector v, JLabel status, String stdname) {
        // specify min, max value explicitly
        super(name, comment, readOnly, cvNum, mask, 1, 127, v, status, stdname);

        // add default overwrites as per NMRA spec
        firstFreeSpace = 0;
        setModifiedCV(19);         // consisting
        setModifiedCV(29);         // control bits
    }

    /**
     * Register a CV to be modified regardless of
     * current value
     */
    public void setModifiedCV(int cvNum) {
        if (firstFreeSpace>=maxCVs) {
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
        for (int i=0; i<firstFreeSpace; i++) {
            CvValue cv = ((CvValue)_cvVector.elementAt(cvNumbers[i]));
            if (cvNumbers[i]!=cv.number())
                log.error("CV numbers don't match: "
                            +cvNumbers[i]+" "+cv.number());
            if (cv!=null && cv.getValue()!=newValues[i]
                && cv.getState()!=CvValue.EDITED)
                cv.setState(CvValue.EDITED);
        }
    }

    int firstFreeSpace = 0;
    static final int maxCVs = 20;
    int[] cvNumbers = new int[maxCVs];
    int[] newValues = new int[maxCVs];

    public void write() {
        if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
        setBusy(true);  // will be reset when value changes
        // mark other CVs as possibly needing write
        updateCvForAddrChange();
        // and change the value of this one
        ((CvValue)_cvVector.elementAt(getCvNum())).write(_status);
    }

    // clean up connections when done
    public void dispose() {
        super.dispose();
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ShortAddrVariableValue.class.getName());

}
