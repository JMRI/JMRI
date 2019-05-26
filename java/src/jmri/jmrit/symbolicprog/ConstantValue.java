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

    @Override
    public CvValue[] usesCVs() {
        return new CvValue[]{};
    }

    /**
     * Provide a user-readable description of the CVs accessed by this variable.
     */
    @Override
    public String getCvDescription() {
        return null;
    }

    // stored value
    JComboBox<Integer> _value = null;

    @Override
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

    @Override
    public Object rangeVal() {
        return "constant: " + _minVal + " - " + _maxVal;
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    @Override
    public String getValueString() {
        return "" + _value.getSelectedIndex();
    }

    @Override
    public void setIntValue(int i) {
        _value.setSelectedIndex(i);  // automatically fires a change event
    }

    @Override
    public int getIntValue() {
        return _value.getSelectedIndex();
    }

    @Override
    public Object getValueObject() {
        return Integer.valueOf(_value.getSelectedIndex());
    }

    @Override
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

    @Override
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
    @Override
    void setColor(Color c) {
    }

    /**
     * No connected CV, so this notify does nothing
     *
     */
    @Override
    public void setCvState(int state) {
    }

    @Override
    public boolean isChanged() {
        return false;
    }

    @Override
    public void setToRead(boolean state) {
    }

    @Override
    public boolean isToRead() {
        return false;
    }

    @Override
    public void setToWrite(boolean state) {
    }

    @Override
    public boolean isToWrite() {
        return false;
    }

    @Override
    public void readChanges() {
        if (isChanged()) {
            readAll();
        }
    }

    @Override
    public void writeChanges() {
        if (isChanged()) {
            writeAll();
        }
    }

    /**
     * Skip actually reading, but set states and notifications anyway.
     * <p>
     * This sets the state to READ so that you can have algorithms like "write
     * all variables that aren't in READ state" This is different from the
     * 'normal' VariableValue objects, which rely on the associated CV objects
     * to drive state changes at the end of the write.
     */
    @Override
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
     * <p>
     * This sets the state to STORED so that you can have algorithms like "write
     * all variables that aren't in STORED state" This is different from the
     * 'normal' VariableValue objects, which rely on the associated CV objects
     * to drive state changes at the end of the write.
     */
    @Override
    public void writeAll() {
        if (log.isDebugEnabled()) {
            log.debug("write invoked");
        }
        setToWrite(false);
        setState(STORED);
        setBusy(true);
        setBusy(false);
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.warn("Unexpected propertyChange: " + e);
    }

    // clean up connections when done
    @Override
    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }

        _value = null;
        // do something about the VarComboBox
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(ConstantValue.class);

}
