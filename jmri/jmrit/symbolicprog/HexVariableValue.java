/**
 * HexVariableValue.java
 *
 * Description:		Extends VariableValue to represent a hexadecimal variable
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version
 *
 */

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.text.Document;

/**
 * LIke DecVariableValue, except that the string representation is in hexadecimal
 */
public class HexVariableValue extends DecVariableValue {

        public HexVariableValue(String name, String comment,
                                boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                                int cvNum, String mask, int minVal, int maxVal,
                                Vector v, JLabel status, String stdname) {
            super(name, comment, readOnly,  infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname);
        }

        void updatedTextField() {
                if (log.isDebugEnabled()) log.debug("updatedTextField");
                // called for new values - set the CV as needed
                CvValue cv = (CvValue)_cvVector.elementAt(getCvNum());
                // compute new cv value by combining old and request
                int oldCv = cv.getValue();
                int newVal;
                try {
                        newVal = Integer.valueOf(_value.getText(), 16).intValue();
                        }
                        catch (java.lang.NumberFormatException ex) { newVal = 0; }
                int newCv = newValue(oldCv, newVal, getMask());
                if (oldCv != newCv)
                        cv.setValue(newCv);
        }

        /** ActionListener implementations */
        public void actionPerformed(ActionEvent e) {
                if (log.isDebugEnabled()) log.debug("actionPerformed");
                int newVal = Integer.valueOf(_value.getText(),16).intValue();
                updatedTextField();
                prop.firePropertyChange("Value", null, new Integer(newVal));
        }

        public void setValue(int value) {
                int oldVal;
                try { oldVal = Integer.valueOf(_value.getText(), 16).intValue(); }
                        catch (java.lang.NumberFormatException ex) { oldVal = -999; }
                if (log.isDebugEnabled()) log.debug("setValue with new value "+value+" old value "+oldVal);
                _value.setText(Integer.toHexString(value));
                if (oldVal != value || getState() == VariableValue.UNKNOWN)
                        actionPerformed(null);
                        prop.firePropertyChange("Value", new Integer(oldVal), new Integer(value));
        }

        // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(HexVariableValue.class.getName());

}
