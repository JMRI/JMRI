// EnumVariableValue.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

/**
 * Extends VariableValue to represent a enumerated variable.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2003, 2013
 * @version	$Revision$
 *
 */
public class EnumVariableValue extends VariableValue implements ActionListener, PropertyChangeListener {

    public EnumVariableValue(String name, String comment, String cvName,
                             boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                             String cvNum, String mask, int minVal, int maxVal,
                             HashMap<String, CvValue> v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _maxVal = maxVal;
        _minVal = minVal;
    }

    /**
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    public EnumVariableValue() {}

    public CvValue[] usesCVs() {
        return new CvValue[]{_cvMap.get(getCvNum())};
    }

    public void nItems(int n) {
        _itemArray = new String[n];
        _valueArray = new int[n];
        _nstored = 0;
    }

    /**
     * Create a new item in the enumeration, with an associated
     * value one more than the last item (or zero if this is the first
     * one added)
     * @param s  Name of the enumeration item
     */
    public void addItem(String s) {
        if (_nstored == 0) {
            addItem(s, 0);
        } else {
            addItem(s, _valueArray[_nstored-1]+1);
        }
    }

    /**
     * Create a new item in the enumeration, with a specified
     * associated value.
     * @param s  Name of the enumeration item
     */
    public void addItem(String s, int value) {
        _valueArray[_nstored] = value;
        _itemArray[_nstored++] = s;
    }

    public void lastItem() {
        _value = new JComboBox(_itemArray);
        // finish initialization
        _value.setActionCommand("");
        _defaultColor = _value.getBackground();
        _value.setBackground(COLOR_UNKNOWN);
        _value.setOpaque(true);
        // connect to the JComboBox model and the CV so we'll see changes.
        _value.addActionListener(this);
        CvValue cv = _cvMap.get(getCvNum());
        cv.addPropertyChangeListener(this);
        cv.setState(CvValue.FROMFILE);
    }

    public void setToolTipText(String t) {
        super.setToolTipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    // stored value
    JComboBox _value = null;

    // place to keep the items & associated numbers
    private String[] _itemArray = null;
    private int[] _valueArray = null;
    private int _nstored;

    int _maxVal;
    int _minVal;
    Color _defaultColor;

    public void setAvailable(boolean a) {
        _value.setVisible(a);
        for (ComboCheckBox c : comboCBs) c.setVisible(a);
        for (VarComboBox c : comboVars) c.setVisible(a);
        for (ComboRadioButtons c : comboRBs) c.setVisible(a);
        super.setAvailable(a);
    }

    public Object rangeVal() {
        return "enum: "+_minVal+" - "+_maxVal;
    }

    public void actionPerformed(ActionEvent e) {
        // see if this is from _value itself, or from an alternate rep.
        // if from an alternate rep, it will contain the value to select
        if (log.isDebugEnabled()) log.debug(label()+" start action event: "+e);
        if (!(e.getActionCommand().equals(""))) {
            // is from alternate rep
            _value.setSelectedItem(e.getActionCommand());
            if (log.isDebugEnabled()) log.debug(label()+" action event was from alternate rep");
        }

        int oldVal = getIntValue();
        
        // called for new values - set the CV as needed
        CvValue cv = _cvMap.get(getCvNum());
        // compute new cv value by combining old and request
        int oldCv = cv.getValue();
        int newVal = getIntValue();
        int newCv = newValue(oldCv, newVal, getMask());
        if (newCv != oldCv) {
            cv.setValue(newCv);  // to prevent CV going EDITED during loading of decoder file

            // notify  (this used to be before setting the values)
            if (log.isDebugEnabled()) log.debug(label()+" about to firePropertyChange");
            prop.firePropertyChange("Value", null, Integer.valueOf(oldVal));
            if (log.isDebugEnabled()) log.debug(label()+" returned to from firePropertyChange");
        }
        if (log.isDebugEnabled()) log.debug(label()+" end action event saw oldCv="+oldCv+" newVal="+newVal+" newCv="+newCv);
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    public String getValueString() {
        return ""+_value.getSelectedIndex();
    }
    public void setIntValue(int i) {
        selectValue(i);
    }

    public String getTextValue() {
        return _value.getSelectedItem().toString();
    }

    public Object getValueObject() {
        return Integer.valueOf(_value.getSelectedIndex());
    }

    /**
     * Set to a specific value.
     * <P>
     * This searches for the displayed value, and sets the
     * enum to that particular one.  It used to work off an index,
     * but now it looks for the value.
     * <P>
     * If the value is larger than any defined, a new one is created.
     * @param value
     */
    protected void selectValue(int value) {
        if (value>256) log.error("Saw unreasonable internal value: "+value);
        for (int i = 0; i<_valueArray.length; i++)
            if (_valueArray[i]==value) {
                //found it, select it
                _value.setSelectedIndex(i);
                return;
            }

        // We can be commanded to a number that hasn't been defined.
        // But that's OK for certain applications.  Instead, we add them as needed
        log.debug("Create new item with value "+value+" count was "+_value.getItemCount()
                        +" in "+label());
        _value.addItem("Reserved value "+value);
        // and value array is too short
        int[] oldArray = _valueArray;
        _valueArray = new int[oldArray.length+1];
        for (int i = 0; i<oldArray.length; i++) _valueArray[i] = oldArray[i];
        _valueArray[oldArray.length] = value;

        _value.setSelectedItem("Reserved value "+value);
    }

    public int getIntValue() {
        if (_value.getSelectedIndex()>=_valueArray.length || _value.getSelectedIndex()<0)
            log.error("trying to get value "+_value.getSelectedIndex()+" too large"
                    +" for array length "+_valueArray.length+" in var "+label());
        return _valueArray[_value.getSelectedIndex()];
    }

    public Component getCommonRep()  { return _value; }

    public void setValue(int value) {
        int oldVal = getIntValue();
        selectValue(value);

        if (oldVal != value || getState() == VariableValue.UNKNOWN)
            prop.firePropertyChange("Value", null, Integer.valueOf(value));
    }

    public Component getNewRep(String format) {
        // sort on format type
        if (format.equals("checkbox")) {
            // this only makes sense if there are exactly two options
            ComboCheckBox b = new ComboCheckBox(_value, this);
            comboCBs.add(b);
            if (getReadOnly()) b.setEnabled(false);
            updateRepresentation(b);
            return b;
        }
        else if (format.equals("radiobuttons")) {
            ComboRadioButtons b = new ComboRadioButtons(_value, this);
            comboRBs.add(b);
            if (getReadOnly()) b.setEnabled(false);
            updateRepresentation(b);
            return b;
        }
        else if (format.equals("onradiobutton")) {
            ComboRadioButtons b = new ComboOnRadioButton(_value, this);
            comboRBs.add(b);
            if (getReadOnly()) b.setEnabled(false);
            updateRepresentation(b);
            return b;
        }
        else if (format.equals("offradiobutton")) {
            ComboRadioButtons b = new ComboOffRadioButton(_value, this);
            comboRBs.add(b);
            if (getReadOnly()) b.setEnabled(false);
            updateRepresentation(b);
            return b;
        }
        else {
            // return a new JComboBox representing the same model
            VarComboBox b = new VarComboBox(_value.getModel(), this);
            comboVars.add(b);
            if (getReadOnly()) b.setEnabled(false);
            updateRepresentation(b);
            return b;
        }
    }

    private List<ComboCheckBox> comboCBs = new ArrayList<ComboCheckBox>();
    private List<VarComboBox> comboVars = new ArrayList<VarComboBox>();
    private List<ComboRadioButtons> comboRBs = new ArrayList<ComboRadioButtons>();

    // implement an abstract member to set colors
    void setColor(Color c) {
        if (c != null) {
            _value.setBackground(c);
        } else {
            _value.setBackground(_defaultColor);
        }
        _value.setOpaque(true);
    }

    /**
     * Notify the connected CVs of a state change from above
     * @param state
     */
    public void setCvState(int state) {
        _cvMap.get(getCvNum()).setState(state);
    }

    public boolean isChanged() {
        CvValue cv = _cvMap.get(getCvNum());
        return considerChanged(cv);
    }

    public void readChanges() {
         if (isToRead() && !isChanged()) 
            log.debug("!!!!!!! unacceptable combination in readChanges: "+label());
         if (isChanged() || isToRead()) readAll();
    }

    public void writeChanges() {
         if (isToWrite() && !isChanged()) 
            log.debug("!!!!!! unacceptable combination in writeChanges: "+label());
         if (isChanged() || isToWrite()) writeAll();
    }

    public void readAll() {
        setToRead(false);
        setBusy(true);  // will be reset when value changes
        _cvMap.get(getCvNum()).read(_status);
    }

    public void writeAll() {
        setToWrite(false);
        if (getReadOnly()) log.error("unexpected write operation when readOnly is set");
        setBusy(true);  // will be reset when value changes
        _cvMap.get(getCvNum()).write(_status);
    }

    // handle incoming parameter notification
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        // notification from CV; check for Value being changed
        if (e.getPropertyName().equals("Busy")) {
            if (((Boolean)e.getNewValue()).equals(Boolean.FALSE)) {
                setToRead(false);
                setToWrite(false);  // some programming operation just finished
                setBusy(false);
            }
        } else if (e.getPropertyName().equals("State")) {
            CvValue cv = _cvMap.get(getCvNum());
            if (cv.getState() == STORED) setToWrite(false);
            if (cv.getState() == READ) setToRead(false);
            setState(cv.getState());
        } else if (e.getPropertyName().equals("Value")) {
            // update value of Variable
            CvValue cv = _cvMap.get(getCvNum());
            int newVal = (cv.getValue() & maskVal(getMask())) >>> offsetVal(getMask());
            setValue(newVal);  // check for duplicate done inside setVal
        }
    }

    /* Internal class extends a JComboBox so that its color is consistent with
     * an underlying variable; we return one of these in getNewRep.
     *<P>
     * Unlike similar cases elsewhere, this doesn't have to listen to
     * value changes.  Those are handled automagically since we're sharing the same
     * model between this object and the real JComboBox value.
     *
     * @author			Bob Jacobsen   Copyright (C) 2001
     * @version         $Revision$
     */
    public static class VarComboBox extends JComboBox {

        VarComboBox(ComboBoxModel m, EnumVariableValue var) {
            super(m);
            _var = var;
            _l = new java.beans.PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                        if (log.isDebugEnabled()) log.debug("VarComboBox saw property change: "+e);
                        originalPropertyChanged(e);
                    }
                };
            // get the original color right
            setBackground(_var._value.getBackground());
            setOpaque(true);
            // listen for changes to original state
            _var.addPropertyChangeListener(_l);
        }

        EnumVariableValue _var;
        transient java.beans.PropertyChangeListener _l = null;

        void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
            // update this color from original state
            if (e.getPropertyName().equals("State")) {
                setBackground(_var._value.getBackground());
                setOpaque(true);
            }
        }

        public void dispose() {
            if (_var != null && _l != null ) _var.removePropertyChangeListener(_l);
            _l = null;
            _var = null;
        }
    }

    // clean up connections when done
    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        
        // remove connection to CV
        _cvMap.get(getCvNum()).removePropertyChangeListener(this);

        // remove connection to graphical representation
        disposeReps();

    }

    void disposeReps() {
        if (_value != null) _value.removeActionListener(this);
        for (int i = 0; i<comboCBs.size(); i++) {
            comboCBs.get(i).dispose();
        }
        for (int i = 0; i<comboVars.size(); i++) {
            comboVars.get(i).dispose();
        }
        for (int i = 0; i<comboRBs.size(); i++) {
            comboRBs.get(i).dispose();
        }
    }
    
    // initialize logging
    static Logger log = LoggerFactory.getLogger(EnumVariableValue.class.getName());

}
