package jmri.jmrit.symbolicprog;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends VariableValue to represent a NMRA long address
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2016
 *
 */
public class LongAddrVariableValue extends VariableValue
        implements ActionListener, FocusListener {

    public LongAddrVariableValue(@Nonnull String name, @Nonnull String comment, @Nonnull String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            @Nonnull String cvNum, @Nonnull String mask, int minVal, int maxVal,
            @Nonnull HashMap<String, CvValue> v, @Nonnull JLabel status, 
            @Nonnull String stdname, @Nonnull CvValue mHighCV) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _maxVal = maxVal;
        _minVal = minVal;
        _value = new JTextField("0", 5);
        _defaultColor = _value.getBackground();
        _value.setBackground(COLOR_UNKNOWN);
        // connect to the JTextField value, cv
        _value.addActionListener(this);
        _value.addFocusListener(this);
        // connect for notification
        CvValue cv = (_cvMap.get(getCvNum()));
        cv.addPropertyChangeListener(this);
        cv.setState(CvValue.FROMFILE);

        highCV = mHighCV;
        highCV.addPropertyChangeListener(this);
        highCV.setState(CvValue.FROMFILE);
    }

    CvValue highCV;

    @Override
    public CvValue[] usesCVs() {
        return new CvValue[]{
            _cvMap.get(getCvNum()),
            highCV};
    }

    /**
     * Provide a user-readable description of the CVs accessed by this variable.
     */
    @Override
    public String getCvDescription() {
        return "CV" + getCvNum() + " & CV" + (highCV.number());
    }

    @Override
    public void setToolTipText(String t) {
        super.setToolTipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    // the connection is to cvNum and highCV
    int _maxVal;
    int _minVal;

    @Override
    public Object rangeVal() {
        return "Long address";
    }

    String oldContents = "";

    void enterField() {
        oldContents = _value.getText();
    }

    void exitField() {
        // this _can_ be invoked after dispose, so protect
        if (_value != null && !oldContents.equals(_value.getText())) {
            int newVal = Integer.parseInt(_value.getText());
            int oldVal = Integer.parseInt(oldContents);
            updatedTextField();
            prop.firePropertyChange("Value", Integer.valueOf(oldVal), Integer.valueOf(newVal));
        }
    }

    @Override
    void updatedTextField() {
        if (log.isDebugEnabled()) {
            log.debug("actionPerformed");
        }
        // called for new values - set the CV as needed
        CvValue cv17 = _cvMap.get(getCvNum());
        CvValue cv18 = highCV;
        // no masking involved for long address
        int newVal;
        try {
            newVal = Integer.parseInt(_value.getText());
        } catch (java.lang.NumberFormatException ex) {
            newVal = 0;
        }

        // no masked combining of old value required, as this fills the two CVs
        int newCv17 = ((newVal / 256) & 0x3F) | 0xc0;
        int newCv18 = newVal & 0xFF;
        cv17.setValue(newCv17);
        cv18.setValue(newCv18);
        if (log.isDebugEnabled()) {
            log.debug("new value " + newVal + " gives CV17=" + newCv17 + " CV18=" + newCv18);
        }
    }

    /**
     * ActionListener implementations
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("actionPerformed");
        }
        int newVal = Integer.parseInt(_value.getText());
        updatedTextField();
        prop.firePropertyChange("Value", null, Integer.valueOf(newVal));
    }

    /**
     * FocusListener implementations
     */
    @Override
    public void focusGained(FocusEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("focusGained");
        }
        enterField();
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("focusLost");
        }
        exitField();
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    @Override
    public String getValueString() {
        return _value.getText();
    }

    @Override
    public void setIntValue(int i) {
        setValue(i);
    }

    @Override
    public int getIntValue() {
        return Integer.parseInt(_value.getText());
    }

    @Override
    public Object getValueObject() {
        return Integer.valueOf(_value.getText());
    }

    @Override
    public Component getCommonRep() {
        if (getReadOnly()) {
            JLabel r = new JLabel(_value.getText());
            updateRepresentation(r);
            return r;
        } else {
            return _value;
        }
    }

    public void setValue(int value) {
        int oldVal;
        try {
            oldVal = Integer.parseInt(_value.getText());
        } catch (java.lang.NumberFormatException ex) {
            oldVal = -999;
        }
        if (log.isDebugEnabled()) {
            log.debug("setValue with new value " + value + " old value " + oldVal);
        }
        _value.setText("" + value);
        if (oldVal != value || getState() == VariableValue.UNKNOWN) {
            actionPerformed(null);
        }
        prop.firePropertyChange("Value", Integer.valueOf(oldVal), Integer.valueOf(value));
    }

    Color _defaultColor;

    // implement an abstract member to set colors
    @Override
    void setColor(Color c) {
        if (c != null) {
            _value.setBackground(c);
        } else {
            _value.setBackground(_defaultColor);
        }
        // prop.firePropertyChange("Value", null, null);
    }

    @Override
    public Component getNewRep(String format) {
        return updateRepresentation(new VarTextField(_value.getDocument(), _value.getText(), 5, this));
    }
    private int _progState = 0;
    private static final int IDLE = 0;
    private static final int READING_FIRST = 1;
    private static final int READING_SECOND = 2;
    private static final int WRITING_FIRST = 3;
    private static final int WRITING_SECOND = 4;

    /**
     * Notify the connected CVs of a state change from above
     *
     */
    @Override
    public void setCvState(int state) {
        (_cvMap.get(getCvNum())).setState(state);
    }

    @Override
    public boolean isChanged() {
        CvValue cv1 = _cvMap.get(getCvNum());
        CvValue cv2 = highCV;
        return (considerChanged(cv1) || considerChanged(cv2));
    }

    @Override
    public void setToRead(boolean state) {
        _cvMap.get(getCvNum()).setToRead(state);
        highCV.setToRead(state);
    }

    @Override
    public boolean isToRead() {
        return _cvMap.get(getCvNum()).isToRead() || highCV.isToRead();
    }

    @Override
    public void setToWrite(boolean state) {
        _cvMap.get(getCvNum()).setToWrite(state);
        highCV.setToWrite(state);
    }

    @Override
    public boolean isToWrite() {
        return _cvMap.get(getCvNum()).isToWrite() || highCV.isToWrite();
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

    @Override
    public void readAll() {
        if (log.isDebugEnabled()) {
            log.debug("longAddr read() invoked");
        }
        setToRead(false);
        setBusy(true);  // will be reset when value changes
        if (_progState != IDLE) {
            log.warn("Programming state " + _progState + ", not IDLE, in read()");
        }
        _progState = READING_FIRST;
        if (log.isDebugEnabled()) {
            log.debug("invoke CV read");
        }
        (_cvMap.get(getCvNum())).read(_status);
    }

    @Override
    public void writeAll() {
        if (log.isDebugEnabled()) {
            log.debug("write() invoked");
        }
        if (getReadOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }
        setToWrite(false);
        setBusy(true);  // will be reset when value changes
        if (_progState != IDLE) {
            log.warn("Programming state " + _progState + ", not IDLE, in write()");
        }
        _progState = WRITING_FIRST;
        if (log.isDebugEnabled()) {
            log.debug("invoke CV write");
        }
        (_cvMap.get(getCvNum())).write(_status);
    }

    // handle incoming parameter notification
    @Override
    public void propertyChange(@Nonnull java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("property changed event - name: "
                    + e.getPropertyName());
        }
        // notification from CV; check for Value being changed
        if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
            // busy transitions drive the state
            switch (_progState) {
                case IDLE:  // no, just a CV update
                    if (log.isDebugEnabled()) {
                        log.error("Busy goes false with state IDLE");
                    }
                    return;
                case READING_FIRST:   // read first CV, now read second
                    if (log.isDebugEnabled()) {
                        log.debug("Busy goes false with state READING_FIRST");
                    }
                    _progState = READING_SECOND;
                    highCV.read(_status);
                    return;
                case READING_SECOND:  // finally done, set not busy
                    if (log.isDebugEnabled()) {
                        log.debug("Busy goes false with state READING_SECOND");
                    }
                    _progState = IDLE;
                    (_cvMap.get(getCvNum())).setState(READ);
                    highCV.setState(READ);
                    //super.setState(READ);
                    setBusy(false);
                    return;
                case WRITING_FIRST:  // no, just a CV update
                    if (log.isDebugEnabled()) {
                        log.debug("Busy goes false with state WRITING_FIRST");
                    }
                    _progState = WRITING_SECOND;
                    highCV.write(_status);
                    return;
                case WRITING_SECOND:  // now done with complete request
                    if (log.isDebugEnabled()) {
                        log.debug("Busy goes false with state WRITING_SECOND");
                    }
                    _progState = IDLE;
                    super.setState(STORED);
                    setBusy(false);
                    return;
                default:  // unexpected!
                    log.error("Unexpected state found: " + _progState);
                    _progState = IDLE;
                    return;
            }
        } else if (e.getPropertyName().equals("State")) {
            CvValue cv = _cvMap.get(getCvNum());
            if (log.isDebugEnabled()) {
                log.debug("CV State changed to " + cv.getState());
            }
            setState(cv.getState());
        } else if (e.getPropertyName().equals("Value")) {
            // update value of Variable
            CvValue cv0 = _cvMap.get(getCvNum());
            CvValue cv1 = highCV;
            int newVal = (cv0.getValue() & 0x3f) * 256 + cv1.getValue();
            setValue(newVal);  // check for duplicate done inside setVal
            // state change due to CV state change, so propagate that
            setState(cv0.getState());
            // see if this was a read or write operation
            switch (_progState) {
                case IDLE:  // no, just a CV update
                    if (log.isDebugEnabled()) {
                        log.debug("Value changed with state IDLE");
                    }
                    return;
                case READING_FIRST:  // yes, now read second
                    if (log.isDebugEnabled()) {
                        log.debug("Value changed with state READING_FIRST");
                    }
                    return;
                case READING_SECOND:  // now done with complete request
                    if (log.isDebugEnabled()) {
                        log.debug("Value changed with state READING_SECOND");
                    }
                    return;
                default:  // unexpected!
                    log.error("Unexpected state found: " + _progState);
                    _progState = IDLE;
                    return;
            }
        }
    }

    // stored value
    JTextField _value = null;

    /* Internal class extends a JTextField so that its color is consistent with
     * an underlying variable
     *
     * @author   Bob Jacobsen   Copyright (C) 2001
     */
    public class VarTextField extends JTextField {

        VarTextField(Document doc, String text, int col, LongAddrVariableValue var) {
            super(doc, text, col);
            _var = var;
            // get the original color right
            setBackground(_var._value.getBackground());
            // listen for changes to ourself
            addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    thisActionPerformed(e);
                }
            });
            addFocusListener(new java.awt.event.FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (log.isDebugEnabled()) {
                        log.debug("focusGained");
                    }
                    enterField();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (log.isDebugEnabled()) {
                        log.debug("focusLost");
                    }
                    exitField();
                }
            });
            // listen for changes to original state
            _var.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                @Override
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    originalPropertyChanged(e);
                }
            });
        }

        LongAddrVariableValue _var;

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
    @Override
    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }
        if (_value != null) {
            _value.removeActionListener(this);
        }
        (_cvMap.get(getCvNum())).removePropertyChangeListener(this);
        highCV.removePropertyChangeListener(this);

        _value = null;
        // do something about the VarTextField
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LongAddrVariableValue.class);

}
