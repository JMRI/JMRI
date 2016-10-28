package jmri.jmrit.symbolicprog;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends VariableValue to represent a constant enum-like-thing Note that
 * there's no CV associated with this.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 *
 */
public class ConstantValue extends VariableValue {

    public ConstantValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _maxVal = maxVal;
        _minVal = minVal;
        _value = new JComboBox<Integer>();
        for (int i = 0; i <= maxVal; i++) {
            _value.addItem(i);
        }
    }

    /**
     * Create a null object. Normally only used for tests and to pre-load
     * classes.
     */
    public ConstantValue() {
    }

    public CvValue[] usesCVs() {
        return new CvValue[]{};
    }

    /**
     * Provide a user-readable description of the CVs accessed by this variable.
     */
    public String getCvDescription() {
        return null;
    }

    // stored value
    JComboBox<Integer> _value = null;

    public void setToolTipText(String t) {
        super.setToolTipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    // place to keep the items
    String[] _itemArray = null;
    int _nstored;

    private int _maxVal;
    private int _minVal;
    Color _defaultColor;

    public Object rangeVal() {
        return "constant: " + _minVal + " - " + _maxVal;
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    public String getValueString() {
        return "" + _value.getSelectedIndex();
    }

    public void setIntValue(int i) {
        _value.setSelectedIndex(i);  // automatically fires a change event
    }

    public int getIntValue() {
        return _value.getSelectedIndex();
    }

    public Object getValueObject() {
        return Integer.valueOf(_value.getSelectedIndex());
    }

    public Component getCommonRep() {
        return _value;
    }

    public void setValue(int value) {
        int oldVal = _value.getSelectedIndex();
        _value.setSelectedIndex(value);
        if (oldVal != value || getState() == VariableValue.UNKNOWN) {
            prop.firePropertyChange("Value", null, Integer.valueOf(value));
        }
    }

    public Component getNewRep(String format) {
        // sort on format type
        if (format.equals("checkbox")) {
            // this only makes sense if there are exactly two options
            JCheckBox b = new JCheckBox();
            b.setEnabled(false);
            b.setSelected((getIntValue() == 1));
            comboCBs.add(b);
            updateRepresentation(b);
            return b;
        } else if (format.equals("radiobuttons")) {
            JRadioButton b = new JRadioButton();
            comboRBs.add(b);
            updateRepresentation(b);
            return b;
        } else if (format.equals("onradiobutton")) {
            JRadioButton b = new JRadioButton();
            comboRBs.add(b);
            updateRepresentation(b);
            return b;
        } else if (format.equals("offradiobutton")) {
            JRadioButton b = new JRadioButton();
            comboRBs.add(b);
            updateRepresentation(b);
            return b;
        } else {
            log.error("Did not recognize a value format: " + format);
            return null;
        }
    }

    List<JCheckBox> comboCBs = new ArrayList<JCheckBox>();
    List<JRadioButton> comboRBs = new ArrayList<JRadioButton>();

    // implement an abstract member to set colors
    void setColor(Color c) {
    }

    /**
     * No connected CV, so this notify does nothing
     *
     */
    public void setCvState(int state) {
    }

    public boolean isChanged() {
        return false;
    }

    public void setToRead(boolean state) {
    }

    public boolean isToRead() {
        return false;
    }

    public void setToWrite(boolean state) {
    }

    public boolean isToWrite() {
        return false;
    }

    public void readChanges() {
        if (isChanged()) {
            readAll();
        }
    }

    public void writeChanges() {
        if (isChanged()) {
            writeAll();
        }
    }

    /**
     * Skip actually reading, but set states and notifications anyway.
     * <P>
     * This sets the state to READ so that you can have algorithms like "write
     * all variables that aren't in READ state" This is different from the
     * 'normal' VariableValue objects, which rely on the associated CV objects
     * to drive state changes at the end of the write.
     */
    public void readAll() {
        if (log.isDebugEnabled()) {
            log.debug("read invoked");
        }
        setToRead(false);
        setState(READ);
        setBusy(true);
        setBusy(false);
    }

    /**
     * Skip actually writing, but set states and notifications anyway.
     * <P>
     * This sets the state to STORED so that you can have algorithms like "write
     * all variables that aren't in STORED state" This is different from the
     * 'normal' VariableValue objects, which rely on the associated CV objects
     * to drive state changes at the end of the write.
     */
    public void writeAll() {
        if (log.isDebugEnabled()) {
            log.debug("write invoked");
        }
        setToWrite(false);
        setState(STORED);
        setBusy(true);
        setBusy(false);
    }

    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.warn("Unexpected propertyChange: " + e);
    }

    // clean up connections when done
    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }

        _value = null;
        // do something about the VarComboBox
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ConstantValue.class.getName());

}
