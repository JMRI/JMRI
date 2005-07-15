// IndexedPairVariableValue.java

package jmri.jmrit.symbolicprog;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.Document;


/**
 * Extends VariableValue to represent a indexed variable
 * split across two indexed CVs.
 *
 * Factor is the multiplier of the value in the high order CV
 *
 * Value to put in High CV = (value in text field)/Factor
 * Value to put in Low CV = (value in text field) - High CV value
 *
 * Value to put in text field = ((value in High CV) * Factor) + Low CV
 *
 * @author   Howard G. Penny  Copyright (C) 2005
 * @version  $Revision: 1.1 $
 *
 */
public class IndexedPairVariableValue extends VariableValue
    implements ActionListener, PropertyChangeListener, FocusListener {

    public IndexedPairVariableValue(int row, String name, String comment, String cvName,
                                     boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                                     int cvNum, String mask, int minVal, int maxVal,
                                     Vector v, JLabel status, String stdname,
                                     int secondCVrow, String pSecondCV, int pFactor, int pOffset, String uppermask) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _row    = row;
        _maxVal = maxVal;
        _minVal = minVal;
        _value = new JTextField("0",3);
        _secondCVrow = secondCVrow;
        _secondCV = pSecondCV;
        _Factor = pFactor;
        _Offset = pOffset;

        lowerbitmask = maskVal(mask);
        lowerbitoffset = offsetVal(mask);
        upperbitmask = maskVal(uppermask);
        upperbitoffset = offsetVal(uppermask);

        // connect for notification
        CvValue cv = ((CvValue)_cvVector.elementAt(_row));
        cv.addPropertyChangeListener(this);
        cv.setState(CvValue.FROMFILE);
        CvValue cv1 = ((CvValue)_cvVector.elementAt(_secondCVrow));
        cv1.addPropertyChangeListener(this);
    }

    public CvValue[] usesCVs() {
        return new CvValue[]{
            (CvValue) _cvVector.elementAt(_row),
            (CvValue) _cvVector.elementAt(_secondCVrow)};
    }

    String _secondCV;
    int _secondCVrow;
    int _Factor;
    int _Offset;

    int lowerbitmask;
    int lowerbitoffset;
    int upperbitmask;
    int upperbitoffset;

    int _row;
    int _maxVal;
    int _minVal;

    public void setTooltipText(String t) {
        super.setTooltipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    public Object rangeVal() {
        return new String("Split value");
    }

    String oldContents = "";

    void enterField() {
        oldContents = _value.getText();
    }

    void exitField() {
        if (!oldContents.equals(_value.getText())) {
            int newVal = Integer.valueOf(_value.getText()).intValue();
            int oldVal = Integer.valueOf(oldContents).intValue();
            updatedTextField();
            prop.firePropertyChange("Value", new Integer(oldVal), new Integer(newVal));
        }
    }

    void updatedTextField() {
        if (log.isDebugEnabled()) log.debug("enter updatedTextField");
        // called for new values - set the CV as needed
        CvValue cvLow = (CvValue)_cvVector.elementAt(_row);
        CvValue cvHigh = (CvValue)_cvVector.elementAt(_secondCVrow);

        int newEntry;  // entered value
        try { newEntry = Integer.valueOf(_value.getText()).intValue(); }
        catch (java.lang.NumberFormatException ex) { newEntry = 0; }

        // calculate resulting number
        int newHigh = (newEntry-_Offset)/_Factor;
        int newLow  = newEntry - (newHigh * _Factor);

        if (log.isDebugEnabled()) log.debug("new value "+newEntry+" gives first="+newLow+" second="+newHigh);

        // cv updates here trigger updated property changes, which means
        // we're going to get notified sooner or later.
        if (cvLow.getValue() != newLow) {
            cvLow.setValue(newLow);
        }
        if (cvHigh.getValue() != newHigh) {
            cvHigh.setValue(newHigh);
        }

        if (log.isDebugEnabled()) log.debug("exit updatedTextField");
    }

    /** ActionListener implementations */
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) log.debug("actionPerformed");
        int newVal = ((Integer.valueOf(_value.getText()).intValue())-_Offset)/_Factor;
        updatedTextField();
        prop.firePropertyChange("Value", null, new Integer(newVal));
    }

    /** FocusListener implementations */
    public void focusGained(FocusEvent e) {
        if (log.isDebugEnabled()) log.debug("focusGained");
        enterField();
    }

    public void focusLost(FocusEvent e) {
        if (log.isDebugEnabled()) log.debug("focusLost");
        exitField();
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    public String getValueString() {
        int newVal = ((Integer.valueOf(_value.getText()).intValue())-_Offset)/_Factor;
        return String.valueOf(newVal);
    }
    public void setIntValue(int i) {
        setValue((i-_Offset)/_Factor);
    }

    public Component getValue()  {
        if (getReadOnly())  {
            JLabel r = new JLabel(_value.getText());
            updateRepresentation(r);
            return r;
        } else
            return _value;
    }

    public void setValue(int value) {
        if (log.isDebugEnabled()) log.debug("setValue "+value);
        int oldVal;
        try {
            oldVal = (Integer.valueOf(_value.getText()).intValue()-_Offset)/_Factor;
        } catch (java.lang.NumberFormatException ex) { oldVal = -999; }
        if (log.isDebugEnabled()) log.debug("setValue with new value "+value+" old value "+oldVal);
        if (oldVal != value) {
            _value.setText(""+value);
            updatedTextField();
            prop.firePropertyChange("Value", new Integer(oldVal), new Integer(value));
        }
    }

    Color _defaultColor;

    // implement an abstract member to set colors
    void setColor(Color c) {
        if (c != null) _value.setBackground(c);
        else _value.setBackground(_defaultColor);
    }

    public Component getRep(String format)  {
        JTextField value = new VarTextField(_value.getDocument(),_value.getText(), 3, this);
        if (getReadOnly() || getInfoOnly()) {
            value.setEditable(false);
        }
        updateRepresentation(value);
        return value;
    }

    private int _progState = 0;
    private boolean programmingLow = true;
    private static final int IDLE = 0;
    private static final int WRITING_PI4R = 1;
    private static final int WRITING_PI4W = 2;
    private static final int WRITING_SI4R = 3;
    private static final int WRITING_SI4W = 4;
    private static final int READING_CV = 5;
    private static final int WRITING_CV = 6;

    /**
     * Notify the connected CVs of a state change from above
     * @param state
     */
    public void setCvState(int state) {
        ((CvValue)_cvVector.elementAt(_row)).setState(state);
    }

    public void setToRead(boolean state) {
        if (getInfoOnly() || getWriteOnly()) state = false;
        ((CvValue)_cvVector.elementAt(_row)).setToRead(state);
        ((CvValue)_cvVector.elementAt(_secondCVrow)).setToRead(state);
    }
    public boolean isToRead() { return ((CvValue)_cvVector.elementAt(_row)).isToRead(); }

    public void setToWrite(boolean state) {
        if (getInfoOnly() || getReadOnly()) state = false;
        ((CvValue)_cvVector.elementAt(_row)).setToWrite(state);
        ((CvValue)_cvVector.elementAt(_secondCVrow)).setToWrite(state);
    }
    public boolean isToWrite() { return ((CvValue)_cvVector.elementAt(_row)).isToWrite(); }

    public boolean isChanged() {
        CvValue cvLow = ((CvValue)_cvVector.elementAt(_row));
        CvValue cvHigh = ((CvValue)_cvVector.elementAt(_secondCVrow));
        return (considerChanged(cvLow)||considerChanged(cvHigh));
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
        if (((CvValue)_cvVector.elementAt(programmingLow ? _row : _secondCVrow)).siVal() >= 0) {
            _progState = WRITING_PI4R;
        }
        else {
            _progState = WRITING_SI4R;
        }
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV read");
        // to read any indexed CV we must write the PI
        ((CvValue)_cvVector.elementAt(programmingLow ? _row : _secondCVrow)).writePI(_status);
    }

    public void writeAll() {
        if (getReadOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }
        setBusy(true);  // will be reset when value changes
        setToWrite(false);
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in write()");
        // lets skip the SI step if SI is not used
        if (((CvValue)_cvVector.elementAt(programmingLow ? _row : _secondCVrow)).siVal() >= 0) {
            _progState = WRITING_PI4W;
        } else {
            _progState = WRITING_SI4W;
        }
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV write");
        // to write any indexed CV we must write the PI
        ((CvValue)_cvVector.elementAt(programmingLow ? _row : _secondCVrow)).writePI(_status);
    }

    // handle incoming parameter notification
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("property changed event - name: "
                                            +e.getPropertyName());
        // notification from CV; check for Value being changed
        if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE)) {
            // busy transitions drive the state
            switch (_progState) {
                case IDLE:  // no, just a CV update
                    if (log.isDebugEnabled()) log.error("Busy goes false with state IDLE");
                    return;
                case WRITING_PI4R:   // have written the PI, now write SI if needed
                case WRITING_PI4W:
                    if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_PI");
                    _progState = (_progState == WRITING_PI4R ? WRITING_SI4R : WRITING_SI4W);
                    ((CvValue)_cvVector.elementAt(programmingLow ? _row : _secondCVrow)).writeSI(_status);
                    return;
                case WRITING_SI4R:
                case WRITING_SI4W:  // have written SI if needed, now read or write CV
                    if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_SI");
                    if (_progState == WRITING_SI4R ) {
                        _progState = READING_CV;
                        ((CvValue)_cvVector.elementAt(programmingLow ? _row : _secondCVrow)).readIcV(_status);
                    } else {
                        _progState = WRITING_CV;
                        ((CvValue)_cvVector.elementAt(programmingLow ? _row : _secondCVrow)).writeIcV(_status);
                    }
                    return;
                case READING_CV:  // now done with the read request
                    if (log.isDebugEnabled()) log.debug("Finished reading the Indexed CV");
                    _progState = IDLE;
                    if (programmingLow) {
                        programmingLow = false;
                        readAll();
                    } else {
                        programmingLow = true;
                        setBusy(false);
                    }
                    return;
                case WRITING_CV:  // now done with the write request
                    if (log.isDebugEnabled()) log.debug("Finished writing the Indexed CV");
                    _progState = IDLE;
                    if (programmingLow) {
                        programmingLow = false;
                        writeAll();
                    } else {
                        programmingLow = true;
                        super.setState(STORED);
                        setBusy(false);
                    }
                    return;
                default:  // unexpected!
                    log.error("Unexpected state found: "+_progState);
                    _progState = IDLE;
                    return;
            }
        }

        else if (e.getPropertyName().equals("State")) {
            CvValue cvLow = (CvValue)_cvVector.elementAt(_row);
            CvValue cvHigh = (CvValue)_cvVector.elementAt(_secondCVrow);
            if (log.isDebugEnabled()) log.debug("CV State changed to "+cvLow.getState());
            if (cvHigh.getState() == VariableValue.UNKNOWN) {
                if (cvLow.getState() == VariableValue.EDITED) {
                    setState(VariableValue.EDITED);
                } else {
                    setState(VariableValue.UNKNOWN);
                }
            } else {
                setState(cvLow.getState());
            }
        }
        else if (e.getPropertyName().equals("Value")) {
            // update value of Variable
            CvValue cvLow = (CvValue)_cvVector.elementAt(_row);
            CvValue cvHigh = (CvValue)_cvVector.elementAt(_secondCVrow);
            int newVal = (cvLow.getValue() + (cvHigh.getValue()*_Factor));
            if (log.isDebugEnabled())
                log.debug("set value to "+newVal+" based on cv0="+cvLow.getValue()+" cv1="+cvHigh.getValue());
            setValue(newVal);  // check for duplicate done inside setVal
            // state change due to CV state change, so propagate that
            if (cvHigh.getState() == VariableValue.UNKNOWN) {
                if (cvLow.getState() == VariableValue.EDITED) {
                    setState(VariableValue.EDITED);
                } else {
                    setState(VariableValue.UNKNOWN);
                }
            } else {
                setState(cvLow.getState());
            }
        }
    }

    // stored value
    JTextField _value = null;

    /* Internal class extends a JTextField so that its color is consistent with
     * an underlying variable
     *
     * @author	Bob Jacobsen   Copyright (C) 2001
     * @version     $Revision: 1.1 $
     */
    public class VarTextField extends JTextField {

        VarTextField(Document doc, String text, int col, IndexedPairVariableValue var) {
            super(doc, text, col);
            _var = var;
            // get the original color right
            setBackground(_var._value.getBackground());
            // listen for changes to ourself
            addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    thisActionPerformed(e);
                }
            });
            addFocusListener(new java.awt.event.FocusListener() {
                public void focusGained(FocusEvent e) {
                    if (log.isDebugEnabled()) log.debug("focusGained");
                    enterField();
                }

                public void focusLost(FocusEvent e) {
                    if (log.isDebugEnabled()) log.debug("focusLost");
                    exitField();
                }
            });
            // listen for changes to original state
            _var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    originalPropertyChanged(e);
                }
            });
        }

        IndexedPairVariableValue _var;

        void thisActionPerformed(java.awt.event.ActionEvent e) {
            // tell original
            _var.actionPerformed(e);
        }

        void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
            // update this color from original state
            if (e.getPropertyName().equals("State")) {
                setBackground(_var._value.getBackground());
            }
        }

    }

    // clean up connections when done
    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
        if (_value != null) {
            _value.removeActionListener(this);
            _value.removeFocusListener(this);
            _value.removePropertyChangeListener(this);
        }
        ((CvValue)_cvVector.elementAt(_row)).removePropertyChangeListener(this);
        ((CvValue)_cvVector.elementAt(_secondCVrow)).removePropertyChangeListener(this);

        _value = null;
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(IndexedPairVariableValue.class.getName());

}
