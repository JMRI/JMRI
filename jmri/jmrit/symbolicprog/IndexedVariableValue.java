// IndexedVariableValue.java

package jmri.jmrit.symbolicprog;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.text.Document;

import com.sun.java.util.collections.ArrayList;

/**
 * Extends VariableValue to represent an indexed variable
 *
 * @author    Howard G. Penny   Copyright (C) 2005
 * @version   $Revision: 1.7 $
 */
public class IndexedVariableValue extends VariableValue
    implements ActionListener, PropertyChangeListener, FocusListener {

    public IndexedVariableValue(int row, String name, String comment, String cvName,
                                boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
                                int cvNum, String mask, int minVal, int maxVal,
                                Vector v, JLabel status, String stdname) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _row    = row;
        _maxVal = maxVal;
        _minVal = minVal;
        _value = new JTextField("0", 3);
        _defaultColor = _value.getBackground();
        CvValue cv = ((CvValue)_cvVector.elementAt(_row));
        cv.addPropertyChangeListener(this);
        if (cv.getInfoOnly()) {
            cv.setState(CvValue.READ);
        } else {
            cv.setState(CvValue.FROMFILE);
        }
    }

    /**
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    protected IndexedVariableValue() {}

    int _row;
    int _maxVal;
    int _minVal;

    public void setTooltipText(String t) {
        super.setTooltipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    public CvValue[] usesCVs() {
        return new CvValue[]{(CvValue)_cvVector.elementAt(_row)};
    }

    public Object rangeVal() {
        return new String("Decimal: "+_minVal+" - "+_maxVal);
    }

    String oldContents = "";

    void enterField() {
        oldContents = _value.getText();
    }
    void exitField() {
        // there may be a lost focus event left in the queue when disposed so protect
        if (_value != null && !oldContents.equals(_value.getText())) {
            int newVal = (Integer.valueOf(_value.getText()).intValue());
            int oldVal = (Integer.valueOf(oldContents).intValue());
            updatedTextField();
            prop.firePropertyChange("Value", new Integer(oldVal),
                                    new Integer(newVal));
        }
    }

    void updatedTextField() {
        if (log.isDebugEnabled()) log.debug("enter updatedTextField");
        // called for new values - set the Indexed CV as needed
        CvValue cv = (CvValue)_cvVector.elementAt(_row);
        //
        int oldVal = cv.getValue();
        int newVal;  // entered value
        try {
            newVal = Integer.valueOf(_value.getText()).intValue();
        }
        catch (java.lang.NumberFormatException ex) { newVal = 0; }
        int newCv = newValue(oldVal, newVal, getMask());
        if (oldVal != newVal)
            cv.setValue(newCv);
    }

    /** ActionListener implementations */
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) log.debug("actionPerformed");
        int newVal = (Integer.valueOf(_value.getText()).intValue());
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
        return _value.getText();
    }

    public void setIntValue(int i) {
        setValue(i);
    }

    public int getIntValue() {
        return (Integer.valueOf(_value.getText()).intValue());
    }

    public Component getValue()  {
        if (getReadOnly())  {
            JLabel r = new JLabel(_value.getText());
            updateRepresentation(r);
            return r;
        } else
            return _value;
    }

    public Component getRep(String format)  {
        if (format.equals("vslider")) {
            IndexedVarSlider b = new IndexedVarSlider(this, _minVal, _maxVal);
            b.setOrientation(JSlider.VERTICAL);
            sliders.add(b);
            updateRepresentation(b);
            return b;
        }
        else if (format.equals("hslider")) {
            IndexedVarSlider b = new IndexedVarSlider(this, _minVal, _maxVal);
            b.setOrientation(JSlider.HORIZONTAL);
            if (_maxVal > 20) {
                b.setMajorTickSpacing(_maxVal/2);
                b.setMinorTickSpacing((_maxVal+1)/8);
            } else {
                b.setMajorTickSpacing(5);
                b.setMinorTickSpacing(1); // because JSlider does not SnapToValue
                b.setSnapToTicks(true);   // like it should, we fake it here
            }
            b.setSize(b.getWidth(),28);
            Hashtable labelTable = new Hashtable();
            labelTable.put( new Integer( 0 ), new JLabel("Off") );
            if ( _maxVal == 63 ) {   // this if for the QSI mute level, not very universal, needs work
                labelTable.put( new Integer( _maxVal/2 ), new JLabel("25%") );
                labelTable.put( new Integer( _maxVal ), new JLabel("50%") );
            } else {
                labelTable.put( new Integer( _maxVal/2 ), new JLabel("50%") );
                labelTable.put( new Integer( _maxVal ), new JLabel("100%") );
            }
            b.setLabelTable( labelTable );
            b.setPaintTicks(true);
            b.setPaintLabels(true);
            sliders.add(b);
            updateRepresentation(b);
            return b;
        }
        else {
            JTextField value = new VarTextField(_value.getDocument(),_value.getText(), 3, this);
            if (getReadOnly() || getInfoOnly()) {
                value.setEditable(false);
            }
            updateRepresentation(value);
            return value;
        }
    }

    ArrayList sliders = new ArrayList();
    private int _progState = 0;
    private static final int IDLE = 0;
    private static final int WRITING_PI4R = 1;
    private static final int WRITING_PI4W = 2;
    private static final int WRITING_SI4R = 3;
    private static final int WRITING_SI4W = 4;
    private static final int READING_CV = 5;
    private static final int WRITING_CV = 6;

    /**
     * Set a new value, including notification as needed.  This does the
     * conversion from string to int, so if the place where formatting
     * needs to be applied
     */
    public void setValue(int value) {
        int oldVal;
        try {
            oldVal = (Integer.valueOf(_value.getText()).intValue());
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
    Color getColor() { return _value.getBackground(); }
    void setColor(Color c) {
        if (c != null) _value.setBackground(c);
        else _value.setBackground(_defaultColor);
        // prop.firePropertyChange("Value", null, null);
    }

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
    }
    public boolean isToRead() { return ((CvValue)_cvVector.elementAt(_row)).isToRead(); }

    public void setToWrite(boolean state) {
        if (getInfoOnly() || getReadOnly()) state = false;
        ((CvValue)_cvVector.elementAt(_row)).setToWrite(state);
    }
    public boolean isToWrite() { return ((CvValue)_cvVector.elementAt(_row)).isToWrite(); }

    public boolean isChanged() {
        CvValue cv = ((CvValue)_cvVector.elementAt(_row));
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
        if (((CvValue)_cvVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4R;
        } else {
            _progState = WRITING_SI4R;
        }
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV read");
        // to read any indexed CV we must write the PI
        ((CvValue)_cvVector.elementAt(_row)).writePI(_status);
    }

    public void writeAll() {
        if (getReadOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }
        setBusy(true);  // will be reset when value changes
        setToWrite(false);
        if (_progState != IDLE) log.warn("Programming state "+_progState+", not IDLE, in write()");
        // lets skip the SI step if SI is not used
        if (((CvValue)_cvVector.elementAt(_row)).siVal() >= 0) {
            _progState = WRITING_PI4W;
        } else {
            _progState = WRITING_SI4W;
        }
        if (log.isDebugEnabled()) log.debug("invoke PI write for CV write");
        // to write any indexed CV we must write the PI
        ((CvValue)_cvVector.elementAt(_row)).writePI(_status);
    }

    // handle incoming parameter notification
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) log.debug("Property changed: "+e.getPropertyName());
        // notification from Indexed CV; check for Value being changed
        if (e.getPropertyName().equals("Busy") && ((Boolean)e.getNewValue()).equals(Boolean.FALSE)) {
            // busy transitions drive the state
            switch (_progState) {
            case IDLE:  // no, just an Indexed CV update
                if (log.isDebugEnabled()) log.error("Busy goes false with state IDLE");
                return;
            case WRITING_PI4R:   // have written the PI, now write SI if needed
            case WRITING_PI4W:
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_PI");
                _progState = (_progState == WRITING_PI4R ? WRITING_SI4R : WRITING_SI4W);
                ((CvValue)_cvVector.elementAt(_row)).writeSI(_status);
                return;
            case WRITING_SI4R:
            case WRITING_SI4W:  // have written SI if needed, now read or write CV
                if (log.isDebugEnabled()) log.debug("Busy goes false with state WRITING_SI");
                if (_progState == WRITING_SI4R ) {
                    _progState = READING_CV;
                    ((CvValue)_cvVector.elementAt(_row)).readIcV(_status);
                } else {
                    _progState = WRITING_CV;
                    ((CvValue)_cvVector.elementAt(_row)).writeIcV(_status);
                }
                return;
            case READING_CV:  // now done with the read request
                if (log.isDebugEnabled()) log.debug("Finished reading the Indexed CV");
                _progState = IDLE;
                setBusy(false);
                return;
            case WRITING_CV:  // now done with the write request
                if (log.isDebugEnabled()) log.debug("Finished writing the Indexed CV");
                _progState = IDLE;
                super.setState(STORED);
                setBusy(false);
                return;
            default:  // unexpected!
                log.error("Unexpected state found: "+_progState);
                _progState = IDLE;
                return;
            }
        }
        else if (e.getPropertyName().equals("State")) {
            CvValue cv = (CvValue)_cvVector.elementAt(_row);
            setState(cv.getState());
        }
        else if (e.getPropertyName().equals("Value")) {
            // update value of Variable
            CvValue cv = (CvValue)_cvVector.elementAt(_row);
            int newVal = (cv.getValue() & maskVal(getMask())) >>> offsetVal(getMask());
            setValue(newVal);  // check for duplicate done inside setVal
        }
    }

    // stored value
    JTextField _value = null;



    /* Internal class extends a JTextField so that its color is consistent with
     * an underlying variable
     *
     * @author	Bob Jacobsen   Copyright (C) 2001
     * @version     $Revision: 1.7 $
     */
    public class VarTextField extends JTextField {

        VarTextField(Document doc, String text, int col, IndexedVariableValue var) {
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

       IndexedVariableValue _var;

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
           _value = null;
       }
       ((CvValue)_cvVector.elementAt(_row)).removePropertyChangeListener(this);
   }

   // initialize logging
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(IndexedVariableValue.class.getName());
}
