// JSpinnerUtil.java

package jmri.util;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JComponent;
import javax.swing.JTextField;

/**
 * Common utility methods for working with Swing JSpinners.
 * <P>
 * Because the JSpinner is not available in Java 1.1.8 and Java 1.3
 * this class was created to provide ways of working with them. 
 * It's more of a library of procedures
 * than a real class, as (so far) all of the operations have needed no state
 * information.
 * <P>
 * @author Bob Jacobsen  Copyright 2005
 * @version $Revision: 1.4 $
 */

public class JSpinnerUtil {

    static public boolean isJSpinnerAvailable() {
        return null!=getJSpinner();
    }
    
    /**
     * Provide a JSpinner if possible.
     * <P>
     * The JSpinner is returned as a JComponent, it's Swing parent class
     *
     * @return null if JSpinner support not available, otherwise a JSpinner object
     */
    static public JComponent getJSpinner() {
        try {
            return new JSpinner();
        } catch (Throwable e) { return null; }  // if not available
    }
    
    /** 
     * Update the value of a JSpinner
     */
    static public void setValue(Object spinner, Object value) {
        if (spinner == null) {
            log.error("setValue should not be called with null reference");
            new Exception().printStackTrace();
            return;
        }
        if (JTextField.class.equals(spinner.getClass())) ((JTextField)spinner).setText(value.toString());
        else if (JSpinner.class.equals(spinner.getClass())) ((JSpinner)spinner).setValue(value);
        else {
            log.error("Can't set value in setValue");
            new Exception().printStackTrace();
            return;
        }
    }
    
    /** 
     * Obtain the value of a JSpinner
     */
    static public Object getValue(Object spinner) {
        if (spinner == null) {
            log.error("getValue should not be called with null reference");
            new Exception().printStackTrace();
            return null;
        }
        if (JTextField.class.equals(spinner.getClass())) {
            String val = ((JTextField)spinner).getText();
            return new Integer(val);
        } else if (JSpinner.class.equals(spinner.getClass())) return ((JSpinner)spinner).getValue();
        else 
        log.error("Can't return result from getValue");
        new Exception().printStackTrace();
        return null;
    }
    
    /** 
     * Obtain the maximum value in a JSpinner's model.
     * <P>
     * Note this implementation assumes that a SpinnerNumberModel
     * is in use.
     */
    static public Object getModelMaximum(Object spinner) {
        if (spinner == null) {
            log.error("getModelMaximum should not be called with null reference");
            new Exception().printStackTrace();
            return null;
        }
        SpinnerNumberModel model=(SpinnerNumberModel)((JSpinner)spinner).getModel();
        return model.getMaximum();
    }
    
    /** 
     * Update the maximum value in a JSpinner's model.
     * <P>
     * Note this implementation assumes that a SpinnerNumberModel
     * is in use.
     */
    static public void setModelMaximum(Object spinner, Integer value) {
        if (spinner == null) {
            log.error("setModelMaximum should not be called with null reference");
            new Exception().printStackTrace();
            return;
        }
        SpinnerNumberModel model=(SpinnerNumberModel)((JSpinner)spinner).getModel();
        model.setMaximum(value);
        ((JSpinner)spinner).setModel(model);
    }
    
    /** 
     * Obtain the minimum value in a JSpinner's model.
     * <P>
     * Note this implementation assumes that a SpinnerNumberModel
     * is in use.
     */
    static public Object getModelMinimum(Object spinner) {
        if (spinner == null) {
            log.error("getModelMinimum should not be called with null reference");
            new Exception().printStackTrace();
            return null;
        }
        SpinnerNumberModel model=(SpinnerNumberModel)((JSpinner)spinner).getModel();
        return model.getMinimum();
    }
    
    /** 
     * Update the minimum value in a JSpinner's model.
     * <P>
     * Note this implementation assumes that a SpinnerNumberModel
     * is in use.
     */
    static public void setModelMinimum(Object spinner, Integer value) {
        if (spinner == null) {
            log.error("setModelMinimum should not be called with null reference");
            new Exception().printStackTrace();
            return;
        }
        SpinnerNumberModel model=(SpinnerNumberModel)((JSpinner)spinner).getModel();
        model.setMinimum(value);
        ((JSpinner)spinner).setModel(model);
    }
    
    /** 
     * Update the step size value in a JSpinner's model.
     * <P>
     * Note this implementation assumes that a SpinnerNumberModel
     * is in use.
     */
    static public void setModelStepSize(Object spinner, Number value) {
        if (spinner == null) {
            log.error("setModelStepSize should not be called with null reference");
            new Exception().printStackTrace();
            return;
        }
        SpinnerNumberModel model=(SpinnerNumberModel)((JSpinner)spinner).getModel();
        model.setStepSize(value);
        ((JSpinner)spinner).setModel(model);
    }
    
    /** 
     * Add an ChangeListener to a JSpinner
     * <P>
     * Note this implementation assumes that a SpinnerNumberModel
     * is in use.
     */
    static public void addChangeListener(Object spinner, javax.swing.event.ChangeListener listener) {
        if (spinner == null) {
            log.error("addChangeListener should not be called with null reference");
            new Exception().printStackTrace();
            return;
        }
        try {
            ((JSpinner)spinner).addChangeListener(listener);
        } catch (Throwable e) {
            // silently fails
            log.debug("Can't add change listener");
        }
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JSpinnerUtil.class.getName());
}