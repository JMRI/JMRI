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
 * @version             $Revision: 1.3 $
 *
 */
public class ShortAddrVariableValue extends DecVariableValue {

    public ShortAddrVariableValue(String name, String comment, boolean readOnly,
                            int cvNum, String mask, int minVal, int maxVal,
                            Vector v, JLabel status, String stdname) {
        super(name, comment, readOnly, cvNum, mask, minVal, maxVal, v, status, stdname);
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
        newValues[firstFreeSpace] = -1;
        firstFreeSpace++;
    }

    /**
     * Register a CV to be modified if the value is
     * different.
     */
    public void setModifiedCV(int cvNum, int newValue) {
        if (firstFreeSpace>=maxCVs) {
            log.error("too many CVs registered for changes!");
            return;
        }
        cvNumbers[firstFreeSpace] = cvNum;
        newValues[firstFreeSpace] = newValue;
        firstFreeSpace++;
    }
    /**
     * Change CV values due to change in short address
     */
    private void updateCvForAddrChange() {
        for (int i=0; i<firstFreeSpace; i++) {
            CvValue cv = ((CvValue)_cvVector.elementAt(cvNumbers[i]));
            if (cv.getValue()!=newValues[i]
                && cv.getState()!=CvValue.EDITED)
                cv.setState(CvValue.FROMFILE);
        }
    }

    int firstFreeSpace = 0;
    static final int maxCVs = 20;
    int[] cvNumbers = new int[maxCVs];
    int[] newValues = new int[maxCVs];

    /**
     * Invoked when a permanent change to the JTextField has been
     * made.  Note that this does _not_ notify property listeners;
     * that should be done by the invoker, who may or may not
     * know what the old value was. Can be overwridden in subclasses
     * that want to display the value differently.
     */
    void updatedTextField() {
        if (log.isDebugEnabled()) log.debug("updatedTextField");
        // called for new values - set the CV as needed
        CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
        // compute new cv value by combining old and request
        int oldCv = cv.getValue();
        int newVal;
        try {
            newVal = Integer.valueOf(_value.getText()).intValue();
        }
        catch (java.lang.NumberFormatException ex) { newVal = 0; }
        int newCv = newValue(oldCv, newVal, getMask());
		if (oldCv != newCv)
                    cv.setValue(newCv);
    }

    public void setIntValue(int i) {
        setValue(i);
    }


    /**
     * Set a new value, including notification as needed.  This does the
     * conversion from string to int, so if the place where formatting
     * needs to be applied
     */
    public void setValue(int value) {
        int oldVal;
        try {
            oldVal = Integer.valueOf(_value.getText()).intValue();
        } catch (java.lang.NumberFormatException ex) { oldVal = -999; }
        if (log.isDebugEnabled()) log.debug("setValue with new value "+value+" old value "+oldVal);
        if (oldVal != value) {
            _value.setText(""+value);
            updatedTextField();
            prop.firePropertyChange("Value", new Integer(oldVal), new Integer(value));
        }
    }

    public void write() {
        if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
        setBusy(true);  // will be reset when value changes
        //super.setState(STORED);
        ((CvValue)_cvVector.elementAt(getCvNum())).write(_status);
    }

    // handle incoming parameter notification
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // notification from CV; check for Value being changed
        if (log.isDebugEnabled()) log.debug("Property changed: "+e.getPropertyName());
        if (e.getPropertyName().equals("Busy")) {
            if (((Boolean)e.getNewValue()).equals(Boolean.FALSE)) setBusy(false);
        }
        else if (e.getPropertyName().equals("State")) {
            CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
            setState(cv.getState());
        }
        else if (e.getPropertyName().equals("Value")) {
            //setBusy(false);
            // update value of Variable
            CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
            int newVal = (cv.getValue() & maskVal(getMask())) >>> offsetVal(getMask());
            setValue(newVal);  // check for duplicate done inside setVal
        }
    }

    // clean up connections when done
    public void dispose() {
        super.dispose();
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ShortAddrVariableValue.class.getName());

}
