// DecVariableValue.java

package jmri.jmrit.symbolicprog;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.text.Document;

import com.sun.java.util.collections.ArrayList;

/**
 * Decimal representation of a value.
 *
 * @author		Bob Jacobsen   Copyright (C) 2001
 * @version             $Revision: 1.9 $
 *
 */
public class DecVariableValue extends VariableValue
    implements ActionListener, PropertyChangeListener, FocusListener {

    public DecVariableValue(String name, String comment, boolean readOnly,
                            int cvNum, String mask, int minVal, int maxVal,
                            Vector v, JLabel status, String stdname) {
        super(name, comment, readOnly, cvNum, mask, v, status, stdname);
        _maxVal = maxVal;
        _minVal = minVal;
        _value = new JTextField("0",3);
        _defaultColor = _value.getBackground();
        _value.setBackground(COLOR_UNKNOWN);
        // connect to the JTextField value, cv
        _value.addActionListener(this);
        _value.addFocusListener(this);
        CvValue cv = ((CvValue)_cvVector.elementAt(getCvNum()));
        cv.addPropertyChangeListener(this);
        cv.setState(CvValue.FROMFILE);
    }

    public void setTooltipText(String t) {
        super.setTooltipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    int _maxVal;
    int _minVal;

    public Object rangeVal() {
        return new String("Decimal: "+_minVal+" - "+_maxVal);
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

    /** ActionListener implementations */
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) log.debug("actionPerformed");
        int newVal = Integer.valueOf(_value.getText()).intValue();
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

    public Component getValue()  {
        if (getReadOnly())  {
            JLabel r = new JLabel(_value.getText());
            updateRepresentation(r);
            return r;
        } else
            return _value;
    }

    public Component getRep(String format)  {
        if (getReadOnly())  //
            return updateRepresentation(new JLabel(_value.getText()));
        else if (format.equals("vslider")) {
            DecVarSlider b = new DecVarSlider(this, _minVal, _maxVal);
            b.setOrientation(JSlider.VERTICAL);
            sliders.add(b);
            updateRepresentation(b);
            return b;
        }
        else if (format.equals("hslider")) {
            DecVarSlider b = new DecVarSlider(this, _minVal, _maxVal);
            b.setOrientation(JSlider.HORIZONTAL);
            sliders.add(b);
            updateRepresentation(b);
            return b;
        }
        else {
            JTextField value = new VarTextField(_value.getDocument(),_value.getText(), 3, this);
            updateRepresentation(value);
            return value;
        }
    }

    ArrayList sliders = new ArrayList();

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
        ((CvValue)_cvVector.elementAt(getCvNum())).setState(state);
    }

    public void read() {
        setBusy(true);  // will be reset when value changes
        //super.setState(READ);
        ((CvValue)_cvVector.elementAt(getCvNum())).read(_status);
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

    // stored value, read-only Value
    JTextField _value = null;



    /* Internal class extends a JTextField so that its color is consistent with
     * an underlying variable
     *
     * @author			Bob Jacobsen   Copyright (C) 2001
     * @version
     */
    public class VarTextField extends JTextField {

        VarTextField(Document doc, String text, int col, DecVariableValue var) {
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

        DecVariableValue _var;

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
        if (_value != null) _value.removeActionListener(this);
        ((CvValue)_cvVector.elementAt(getCvNum())).removePropertyChangeListener(this);

        _value = null;
        // do something about the VarTextField
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecVariableValue.class.getName());

}
