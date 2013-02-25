// IndexedEnumVariableValue.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.Vector;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Extends VariableValue to represent a enumerated indexed variable.
 *
 * @author    Howard G. Penny   Copyright (C) 2005
 * @version   $Revision$
 *
 */
public class IndexedEnumVariableValue extends VariableValue
    implements ActionListener, PropertyChangeListener {

    public IndexedEnumVariableValue(int row, String name, String comment, String cvName,
                                    boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                                    int cvNum, String mask,
                                    Vector<CvValue> v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _row    = row;
    }

    /**
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    protected IndexedEnumVariableValue() {}

    int _row;
    int _minVal;
    int _maxVal;

    public CvValue[] usesCVs() {
        return new CvValue[]{
            _cvVector.elementAt(_row)};
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
        if (_nstored == 0) {
            _minVal = value;
        }
        _valueArray[_nstored] = value;
        _itemArray[_nstored++] = s;
    }

    public void lastItem() {
        // we now know the maxVal, store it for whatever reason
        _maxVal = _valueArray[_nstored-1];
        _value = new JComboBox(_itemArray);
        // finish initialization
        _value.setActionCommand("8");
        _defaultColor = _value.getBackground();
        _value.setBackground(COLOR_UNKNOWN);
        // connect to the JComboBox model and the CV so we'll see changes.
        _value.addActionListener(this);
        CvValue cv = (_cvVector.elementAt(_row));
        cv.addPropertyChangeListener(this);
        if (cv.getInfoOnly()) {
            cv.setState(CvValue.READ);
        } else {
            cv.setState(CvValue.FROMFILE);
        }
    }

    public void setToolTipText(String t) {
        super.setToolTipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    // stored value
    JComboBox _value = null;

    // place to keep the items & associated numbers
    String[] _itemArray = null;
    int[] _valueArray = null;
    int _nstored;

    Color _defaultColor;

    public Object rangeVal() {
        return "enum: "+_minVal+" - "+_maxVal;
    }

    public void actionPerformed(ActionEvent e) {
        // see if this is from _value itself, or from an alternate rep.
        // if from an alternate rep, it will contain the value to select
        if (!(e.getActionCommand().equals("8"))) {
            // is from alternate rep
            _value.setSelectedItem(e.getActionCommand());
        }
        if (log.isDebugEnabled()) log.debug("action event: "+e);

        // called for new values - set the CV as needed
        CvValue cv = _cvVector.elementAt(_row);
        // compute new cv value by combining old and request
        int oldCv = cv.getValue();
        int newVal = getIntValue();
        int newCv = newValue(oldCv, newVal, getMask());
        if (newCv != oldCv) {
            cv.setValue(newCv); // to prevent CV going EDITED during loading of decoder file
            // notify
            prop.firePropertyChange("Value", null, Integer.valueOf(getIntValue()));
        }
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

    /**
     * Set to a specific value.
     *
     * This searches for the displayed value, and sets the
     * enum to that particular one.
     *
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
        if ((_value.getSelectedIndex()>=_valueArray.length) || _value.getSelectedIndex()<0)
            log.error("trying to get value "+_value.getSelectedIndex()+" too large"
                    +" for array length "+_valueArray.length);
        return _valueArray[_value.getSelectedIndex()];
    }

    public Object getValueObject() {
        return Integer.valueOf(_value.getSelectedIndex());
    }

    public Component getCommonRep()  { return _value; }

    public void setValue(int value) {
        int oldVal = getIntValue();
        selectValue(value);

        if ((oldVal != value) || (getState() == VariableValue.UNKNOWN))
            prop.firePropertyChange("Value", null, Integer.valueOf(value));
    }

    public Component getNewRep(String format) {
        // sort on format type
        if (format.equals("checkbox")) {
            // this only makes sense if there are exactly two options
            IndexedComboCheckBox b = new IndexedComboCheckBox(_value, this);
            comboCBs.add(b);
            updateRepresentation(b);
            if (!getAvailable()) b.setVisible(false);
            return b;
        }
        else if (format.equals("radiobuttons")) {
            ComboRadioButtons b = new ComboRadioButtons(_value, this);
            comboRBs.add(b);
            updateRepresentation(b);
            return b;
        }
        else if (format.equals("onradiobutton")) {
            ComboRadioButtons b = new ComboOnRadioButton(_value, this);
            comboRBs.add(b);
            updateRepresentation(b);
            return b;
        }
        else if (format.equals("offradiobutton")) {
            ComboRadioButtons b = new ComboOffRadioButton(_value, this);
            comboRBs.add(b);
            updateRepresentation(b);
            return b;
        } else {
            // return a new JComboBox representing the same model
            IVarComboBox b = new IVarComboBox(_value.getModel(), this);
            //b.setPreferredSize(new Dimension(284, b.getPreferredSize().height));
            comboVars.add(b);
            updateRepresentation(b);
            return b;
        }
    }

    List<IndexedComboCheckBox> comboCBs = new ArrayList<IndexedComboCheckBox>();
    List<IVarComboBox> comboVars = new ArrayList<IVarComboBox>();
    private List<ComboRadioButtons> comboRBs = new ArrayList<ComboRadioButtons>();

    // implement an abstract member to set colors
    void setColor(Color c) {
        if (_value != null) {
            if (c != null) {
                _value.setBackground(c);
            }
            else {
                _value.setBackground(_defaultColor);
            }
        }
    }

    public void setAvailable(boolean a) {
        for (IndexedComboCheckBox c : comboCBs) c.setVisible(a);
        for (IVarComboBox c : comboVars) c.setVisible(a);
        for (ComboRadioButtons c : comboRBs) c.setVisible(a);
        super.setAvailable(a);
    }
    
    private int _progState = 0;
    private static final int IDLE = 0;
    private static final int WRITING_PI4R = 1;
    private static final int WRITING_PI4W = 2;
    private static final int WRITING_SI4R = 3;
    private static final int WRITING_SI4W = 4;
    private static final int READING_CV = 5;
    private static final int WRITING_CV = 6;
    private static final int WRITING_PI4C = 7;
    private static final int WRITING_SI4C = 8;
    private static final int COMPARE_CV = 9;

   /**
     * Count number of retries done
     */
    private int retries = 0;
    
    /**
     * Define maximum number of retries of read/write operations before moving on
     */
    private static final int RETRY_MAX = 2;

    /**
     * Notify the connected CVs of a state change from above
     * @param state
     */
    public void setCvState(int state) {
        (_cvVector.elementAt(getCvNum())).setState(state);
    }

    public void setToRead(boolean state) {
        if (getInfoOnly() || getWriteOnly()) state = false;
        (_cvVector.elementAt(_row)).setToRead(state);
    }
    public boolean isToRead() { return getAvailable() && (_cvVector.elementAt(_row)).isToRead(); }

    public void setToWrite(boolean state) {
        if (getInfoOnly() || getReadOnly()) state = false;
        (_cvVector.elementAt(_row)).setToWrite(state);
    }
    public boolean isToWrite() { return getAvailable() && (_cvVector.elementAt(_row)).isToWrite(); }

    public boolean isChanged() {
        CvValue cv = (_cvVector.elementAt(_row));
        return considerChanged(cv);
    }

    public void readChanges() {
         if (isChanged()) readAll();
    }

    public void writeChanges() {
         if (isChanged()) writeAll();
    }

    public void readAll() {
        setBusy(true);  // will be reset when value changes
        setToRead(false);
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in read()");
         // lets skip the SI step if SI is not used
        if ((_cvVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4R;
        } else { // lets skip this step if SI is not used
            _progState = WRITING_SI4R;
        }
        retries = 0;
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV read");
        // to read any indexed CV we must write the PI
        (_cvVector.elementAt(_row)).writePI(_status);
    }

    public void writeAll() {
        if (getReadOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }
        setBusy(true);  // will be reset when value changes
        setToWrite(false);
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in write()");
        // lets skip the SI step if SI is not used
        if ((_cvVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4W;
        } else {
            _progState = WRITING_SI4W;
        }
        retries = 0;
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV write");
        // to write any indexed CV we must write the PI first
        (_cvVector.elementAt(_row)).writePI(_status);
    }
    
    public void confirmAll() {
        setBusy(true);  // will be reset when value changes
        setToRead(false);
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in read()");
        // lets skip the SI step if SI is not used
        if ((_cvVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4C;
        } else {
            _progState = WRITING_SI4C;
        }
        retries = 0;
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV confirm");
        // to read any indexed CV we must write the PI
        (_cvVector.elementAt(_row)).writePI(_status);
    }

    // handle incoming parameter notification
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("Property changed: "+e.getPropertyName());
        // notification from CV; check for Value being changed
        if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE)) {
            // busy transitions drive the state
            switch (_progState) {
            case IDLE:  // no, just an Indexed CV update
                if (log.isDebugEnabled()) log.error("Busy goes false with state IDLE");
                return;
            case WRITING_PI4R:   // have written the PI, now write SI if needed
            case WRITING_PI4C:
            case WRITING_PI4W:
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_PI");
                // check for success
                if ((retries < RETRY_MAX)
                    && ( (_cvVector.elementAt(_row)).getState() != CvValue.STORED) ) {
                    // need to retry on error; leave progState as it was
                    log.debug("retry");
                    retries++;
                    (_cvVector.elementAt(_row)).writePI(_status);
                    return;
                }
                // success, move on to next
                retries = 0;

                if (_progState == WRITING_PI4R )
                	_progState = WRITING_SI4R;
                else if (_progState == WRITING_PI4C )
                	_progState = WRITING_SI4C;
                else
                	_progState = WRITING_SI4W;
                (_cvVector.elementAt(_row)).writeSI(_status);
                return;
            case WRITING_SI4R:  // have written the SI if needed, now read or write CV
            case WRITING_SI4C:
            case WRITING_SI4W:
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_SI");
                // check for success
                if ((retries < RETRY_MAX)
                    && ( (_cvVector.elementAt(_row)).getState() != CvValue.STORED) ) {
                    // need to retry on error; leave progState as it was
                    log.debug("retry");
                    retries++;
                    (_cvVector.elementAt(_row)).writeSI(_status);
                    return;
                }
                // success, move on to next
                retries = 0;
                if (_progState == WRITING_SI4R ) {
                    _progState = READING_CV;
                    (_cvVector.elementAt(_row)).readIcV(_status);
                } else if (_progState == WRITING_SI4C ) {
                    _progState = COMPARE_CV;
                    (_cvVector.elementAt(_row)).confirmIcV(_status);
                 } else {
                    _progState = WRITING_CV;
                    (_cvVector.elementAt(_row)).writeIcV(_status);
                }
                return;
            case READING_CV:  // now done with the read request
                if (log.isDebugEnabled()) log.debug("Finished reading the Indexed CV");
                // check for success
                if ((retries < RETRY_MAX)
                    && ( (_cvVector.elementAt(_row)).getState() != CvValue.READ) ) {
                    // need to retry on error; leave progState as it was
                    log.debug("retry");
                    retries++;
                    (_cvVector.elementAt(_row)).readIcV(_status);
                    return;
                }
                // success, move on to next
                retries = 0;
                _progState = IDLE;
                setBusy(false);
                return;
            case COMPARE_CV:  // now done with the read request
                if (log.isDebugEnabled()) log.debug("Finished reading the Indexed CV for compare");

                // check for success SAME or DIFF?
                if ((retries < RETRY_MAX)
						&& (( _cvVector.elementAt(_row))
								.getState() != CvValue.SAME)
						&& (( _cvVector.elementAt(_row))
								.getState() != CvValue.DIFF)) {
					// need to retry on error; leave progState as it was
                    log.debug("retry");
                    retries++;
                    (_cvVector.elementAt(_row)).confirmIcV(_status);
                    return;
                }
                // success, move on to next
                retries = 0;

                _progState = IDLE;
                setBusy(false);
                return;
            case WRITING_CV:  // now done with the write request
                if (log.isDebugEnabled()) log.debug("Finished writing the Indexed CV");
                // check for success
                if ((retries < RETRY_MAX)
                    && ( (_cvVector.elementAt(_row)).getState() != CvValue.STORED) ) {
                    // need to retry on error; leave progState as it was
                    log.debug("retry");
                    retries++;
                    (_cvVector.elementAt(_row)).writeIcV(_status);
                    return;
                }
                // success, move on to next
                retries = 0;
                _progState = IDLE;
                super.setState(STORED);
                setBusy(false);
                return;
            default:  // unexpected!
                log.error("Unexpected state found: "+_progState);
                _progState = IDLE;
                return;
            }
        } else if (e.getPropertyName().equals("State")) {
            CvValue cv = _cvVector.elementAt(_row);
            setState(cv.getState());
        } else if (e.getPropertyName().equals("Value")) {
            // update value of Variable
            CvValue cv = _cvVector.elementAt(_row);
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
     * @author  Bob Jacobsen   Copyright (C) 2001
     * @version $Revision$
     */
    public class IVarComboBox extends JComboBox {

        IndexedEnumVariableValue _var;
        transient java.beans.PropertyChangeListener _l = null;

        IVarComboBox(ComboBoxModel m, IndexedEnumVariableValue var) {
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
            // listen for changes to original state
            _var.addPropertyChangeListener(_l);
        }

        void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
            // update this color from original state
            if (e.getPropertyName().equals("State")) {
                setBackground(_var._value.getBackground());
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
        if (_value != null) _value.removeActionListener(this);
        (_cvVector.elementAt(_row)).removePropertyChangeListener(this);

        for (int i = 0; i<comboCBs.size(); i++) {
            comboCBs.get(i).dispose();
        }
        for (int i = 0; i<comboVars.size(); i++) {
            comboVars.get(i).dispose();
        }
        for (int i = 0; i<comboRBs.size(); i++) {
            comboRBs.get(i).dispose();
        }

        _value = null;
    }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(IndexedEnumVariableValue.class.getName());
}
