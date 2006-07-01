// ConstantValue.java

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.InstanceManager;
import jmri.ProgListener;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.*;
import java.awt.Color;

import com.sun.java.util.collections.List;
import com.sun.java.util.collections.ArrayList;

/**
 * Extends VariableValue to represent a constant enum-like-thing
 *
 * @author    Bob Jacobsen   Copyright (C) 2001
 * @version   $Revision: 1.4 $
 *
 */
public class ConstantValue extends VariableValue {

    public ConstantValue(String name, String comment, boolean readOnly,
                         int cvNum, String mask, int minVal, int maxVal,
                         Vector v, JLabel status, String stdname) {
        super(name, comment, readOnly, cvNum, mask, v, status, stdname);
        _maxVal = maxVal;
        _minVal = minVal;
        _value = new JComboBox();
        for (int i=0; i<=maxVal; i++) {
            _value.addItem(""+0);
        }
    }

    /**
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    public ConstantValue() {}

    // stored value
    JComboBox _value = null;

    // place to keep the items
    String[] _itemArray = null;
    int _nstored;

    private int _maxVal;
    private int _minVal;
    Color _defaultColor;

    public Object rangeVal() {
        return new String("constant: "+_minVal+" - "+_maxVal);
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    public String getValueString() {
        return ""+_value.getSelectedIndex();
    }
    public void setIntValue(int i) {
        _value.setSelectedIndex(i);  // automatically fires a change event
    }
    public int getIntValue() {
        return _value.getSelectedIndex();
    }

    public Component getValue()  { return _value; }
    public void setValue(int value) {
        int oldVal = _value.getSelectedIndex();
        _value.setSelectedIndex(value);
        if (oldVal != value || getState() == VariableValue.UNKNOWN)
            prop.firePropertyChange("Value", null, new Integer(value));
    }

    public Component getRep(String format) {
        // sort on format type
        if (format.equals("checkbox")) {
            // this only makes sense if there are exactly two options
            JCheckBox b = new JCheckBox();
            b.setEnabled(false);
            b.setSelected(true);
            comboCBs.add(b);
            return b;
        }
        else if (format.equals("radiobuttons")) {
            JRadioButton b = new JRadioButton();
            comboRBs.add(b);
            return b;
        }
        else if (format.equals("onradiobutton")) {
            JRadioButton b = new JRadioButton();
            comboRBs.add(b);
            return b;
        }
        else if (format.equals("offradiobutton")) {
            JRadioButton b = new JRadioButton();
            comboRBs.add(b);
            return b;
        }
        else {
            log.error("Did not recognize a value format: "+format);
            return null;
        }
    }

    List comboCBs = new ArrayList();
    List comboRBs = new ArrayList();

    // implement an abstract member to set colors
    void setColor(Color c) {
    }

    /**
     * Notify the connected CVs of a state change from above
     * @param state
     */
    public void setCvState(int state) {
        CvValue cv = ((CvValue)_cvVector.elementAt(getCvNum()));
        if (cv!=null) cv.setState(state);
        else log.error("try to set state on CV "+getCvNum()+" with null entry");
    }

    /**
     * read() sets the state to READ so that you can
     * have algorithms like "read all variables that aren't in READ state".
     * This is different from the 'normal' VariableValue objects, which
     * rely on the associated CV objects to drive state changes at the
     * end of the read.
     */
    public void read() {
        if (log.isDebugEnabled()) log.debug("read invoked");
        setState(READ);
        setBusy(true);
        setBusy(false);
    }
    /**
     * write() sets the state to STORED so that you can
     * have algorithms like "write all variables that aren't in STORED state"
     * This is different from the 'normal' VariableValue objects, which
     * rely on the associated CV objects to drive state changes at the
     * end of the write.
     */
    public void write() {
        if (log.isDebugEnabled()) log.debug("write invoked");
        setState(STORED);
        setBusy(true);
        setBusy(false);
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.warn("Unexpected propertyChange: "+e);
    }

    // clean up connections when done
    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");

        _value = null;
        // do something about the VarComboBox
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConstantValue.class.getName());

}
