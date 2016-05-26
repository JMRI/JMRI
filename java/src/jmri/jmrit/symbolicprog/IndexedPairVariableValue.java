package jmri.jmrit.symbolicprog;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.text.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends VariableValue to represent a indexed variable split across two
 * indexed CVs.
 *
 * Factor is the multiplier of the value in the high order CV
 *
 * Value to put in High CV = (value in text field)/Factor Value to put in Low CV
 * = (value in text field) - High CV value
 *
 * Value to put in text field = ((value in High CV) * Factor) + Low CV
 *
 * @author Howard G. Penny Copyright (C) 2005
 * @author Bob Jacobsen Copyright (C) 2013
 * @deprecated // since 3.7.1
 *
 */
@Deprecated // since 3.7.1
public class IndexedPairVariableValue extends VariableValue
        implements ActionListener, PropertyChangeListener, FocusListener {

    public IndexedPairVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask,
            boolean upperFirst) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _mask = mask;
        _uppermask = uppermask;
        _maxVal = maxVal;
        _minVal = minVal;
        mSecondCVname = pSecondCV;
        int len = 4;
        if (maxVal > 999) {
            len = 5;
        }
        if (maxVal > 9999) {
            len = 6;
        }
        _value = new JTextField("0", len);
        _defaultColor = _value.getBackground();
        _value.setBackground(COLOR_UNKNOWN);
        _upperFirst = upperFirst;
        mFactor = pFactor;
        mOffset = pOffset;
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " mfactor " + mFactor + " and mOffset=" + mOffset);
        }

        // connect to the JTextField value, cv
        _value.addActionListener(this);
        _value.addFocusListener(this);

        lowerbitmask = maskVal(mask);
        lowerbitoffset = offsetVal(mask);
        upperbitmask = maskVal(uppermask);

        // upper bit offset includes lower bit offset, and MSB bits missing from upper part
        upperbitoffset = offsetVal(uppermask);
        String t = mask;
        while (t.length() > 0) {
            if (!t.startsWith("V")) {
                upperbitoffset++;
            }
            t = t.substring(1);
        }
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " upper mask " + uppermask + " had offsetVal=" + offsetVal(uppermask)
                    + " so upperbitoffset=" + upperbitoffset);
        }

        // connect for notification
        CvValue cv = (_cvMap.get(getCvName()));
        cv.addPropertyChangeListener(this);
        cv.setState(CvValue.FROMFILE);
        CvValue cv1 = (_cvMap.get(mSecondCVname));
        cv1.addPropertyChangeListener(this);
        cv1.setState(CvValue.FROMFILE);
    }

    public CvValue[] usesCVs() {
        return new CvValue[]{
            _cvMap.get(getCvName()),
            _cvMap.get(mSecondCVname)};
    }

    String mSecondCVname;  // string because 1.2.3 form
    int mFactor;
    int mOffset;
    String _mask;
    String _uppermask;

    public String getSecondCvNum() {
        return mSecondCVname;
    }

    int lowerbitmask;
    int lowerbitoffset;
    int upperbitmask;
    // number of bits to shift _left_ the 8-16 bits in 2nd CV
    // e.g. multiply by 256, then shift by this
    int upperbitoffset;

    public void setToolTipText(String t) {
        super.setToolTipText(t);   // do default stuff
        _value.setToolTipText(t);  // set our value
    }

    // the connection is to cvNum and cvNum+1
    int _maxVal;
    int _minVal;

    boolean _upperFirst;

    public Object rangeVal() {
        return "Split value";
    }

    public String getMask() {
        return _uppermask + _mask;
    }

    /**
     * Provide a user-readable description of the CVs accessed by this variable.
     */
    public String getCvDescription() {
        return "CV" + getCvName() + " & CV" + mSecondCVname;
    }

    String oldContents = "";

    void enterField() {
        oldContents = _value.getText();
    }

    void exitField() {
        // there may be a lost focus event left in the queue when disposed so protect
        if (_value != null && !oldContents.equals(_value.getText())) {
            int newVal = Integer.valueOf(_value.getText()).intValue();
            int oldVal = Integer.valueOf(oldContents).intValue();
            updatedTextField();
            prop.firePropertyChange("Value", Integer.valueOf(oldVal),
                    Integer.valueOf(newVal));
        }
    }

    void updatedTextField() {
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " enter updatedTextField in SplitVal");
        }
        // called for new values - set the CV as needed
        CvValue cv1 = _cvMap.get(getCvName());
        CvValue cv2 = _cvMap.get(mSecondCVname);

        int newEntry;  // entered value
        try {
            newEntry = Integer.valueOf(_value.getText()).intValue();
        } catch (java.lang.NumberFormatException ex) {
            newEntry = 0;
        }

        // calculate resulting number
        int newVal = (newEntry - mOffset) / mFactor;

        // combine with existing values via mask
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " lo cv was " + cv1.getValue() + " mask=" + lowerbitmask + " offset=" + lowerbitoffset);
        }
        int newCv1 = ((newVal << lowerbitoffset) & lowerbitmask)
                | (~lowerbitmask & cv1.getValue());

        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " hi cv was " + cv2.getValue() + " mask=" + upperbitmask + " offset=" + upperbitoffset);
        }
        int newCv2 = (((newVal << upperbitoffset) >> 8) & upperbitmask)
                | (~upperbitmask & cv2.getValue());
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " new value " + newVal + " gives first=" + newCv1 + " second=" + newCv2);
        }

        // cv updates here trigger updated property changes, which means
        // we're going to get notified sooner or later.
        cv1.setValue(newCv1);
        cv2.setValue(newCv2);
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " exit updatedTextField");
        }

    }

    /**
     * ActionListener implementations
     */
    public void actionPerformed(ActionEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " actionPerformed");
        }
        int newVal = ((Integer.valueOf(_value.getText()).intValue()) - mOffset) / mFactor;
        updatedTextField();
        prop.firePropertyChange("Value", null, Integer.valueOf(newVal));
    }

    /**
     * FocusListener implementations
     */
    public void focusGained(FocusEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " focusGained");
        }
        enterField();
    }

    public void focusLost(FocusEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " focusLost");
        }
        exitField();
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    public String getValueString() {
        // until 2.13.1-dev, this was just the 1st CV value
        //int newVal = ((Integer.valueOf(_value.getText()).intValue())-mOffset)/mFactor;
        return _value.getText();
    }

    public void setIntValue(int i) {
        // until 2.13.1-dev, this was just the 1st CV value
        //setValue((i-mOffset)/mFactor);
        setValue(i);
    }

    public int getIntValue() {
        return ((Integer.valueOf(_value.getText()).intValue()) - mOffset) / mFactor;
    }

    public Object getValueObject() {
        return Integer.valueOf(_value.getText());
    }

    public Component getCommonRep() {
        if (getReadOnly()) {
            JLabel r = new JLabel(_value.getText());
            updateRepresentation(r);
            return r;
        } else {
            return _value;
        }
    }

    ArrayList<IndexedPairVarSlider> sliders = new ArrayList<IndexedPairVarSlider>();

    public void setValue(int value) {
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " enter setValue " + value);
        }
        int oldVal;
        try {
            oldVal = (Integer.valueOf(_value.getText()).intValue() - mOffset) / mFactor;
        } catch (java.lang.NumberFormatException ex) {
            oldVal = -999;
        }
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " setValue with new value " + value + " old value " + oldVal);
        }
        _value.setText(String.valueOf(value * mFactor + mOffset));
        if (oldVal != value || getState() == VariableValue.UNKNOWN) {
            actionPerformed(null);
        }
        prop.firePropertyChange("Value", Integer.valueOf(oldVal), Integer.valueOf(value * mFactor + mOffset));
        if (log.isDebugEnabled()) {
            log.debug("CV " + getCvName() + "," + getSecondCvNum() + " exit setValue " + value);
        }
    }

    Color _defaultColor;

    // implement an abstract member to set colors
    Color getDefaultColor() {
        return _defaultColor;
    }

    Color getColor() {
        return _value.getBackground();
    }

    void setColor(Color c) {
        if (c != null) {
            _value.setBackground(c);
        } else {
            _value.setBackground(_defaultColor);
        }
    }

    public Component getNewRep(String format) {
        if (format.equals("vslider")) {
            IndexedPairVarSlider b = new IndexedPairVarSlider(this, _minVal, _maxVal);
            b.setOrientation(JSlider.VERTICAL);
            sliders.add(b);
            reps.add(b);
            updateRepresentation(b);
            return b;
        } else if (format.equals("hslider")) {
            IndexedPairVarSlider b = new IndexedPairVarSlider(this, _minVal, _maxVal);
            b.setOrientation(JSlider.HORIZONTAL);
            sliders.add(b);
            reps.add(b);
            updateRepresentation(b);
            return b;
        } else if (format.equals("hslider-percent")) {
            IndexedPairVarSlider b = new IndexedPairVarSlider(this, _minVal, _maxVal);
            b.setOrientation(JSlider.HORIZONTAL);
            if (_maxVal > 20) {
                b.setMajorTickSpacing(_maxVal / 2);
                b.setMinorTickSpacing((_maxVal + 1) / 8);
            } else {
                b.setMajorTickSpacing(5);
                b.setMinorTickSpacing(1); // because JSlider does not SnapToValue
                b.setSnapToTicks(true);   // like it should, we fake it here
            }
            b.setSize(b.getWidth(), 28);
            Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
            labelTable.put(Integer.valueOf(0), new JLabel("0%"));
            if (_maxVal == 63) {   // this if for the QSI mute level, not very universal, needs work
                labelTable.put(Integer.valueOf(_maxVal / 2), new JLabel("25%"));
                labelTable.put(Integer.valueOf(_maxVal), new JLabel("50%"));
            } else {
                labelTable.put(Integer.valueOf(_maxVal / 2), new JLabel("50%"));
                labelTable.put(Integer.valueOf(_maxVal), new JLabel("100%"));
            }
            b.setLabelTable(labelTable);
            b.setPaintTicks(true);
            b.setPaintLabels(true);
            sliders.add(b);
            updateRepresentation(b);
            if (!getAvailable()) {
                b.setVisible(false);
            }
            return b;
        } else {
            JTextField value = new VarTextField(_value.getDocument(), _value.getText(), 5, this);
            if (getReadOnly() || getInfoOnly()) {
                value.setEditable(false);
            }
            reps.add(value);
            updateRepresentation(value);
            return value;
        }
    }

    public void setAvailable(boolean a) {
        _value.setVisible(a);
        for (Component c : sliders) {
            c.setVisible(a);
        }
        for (Component c : reps) {
            c.setVisible(a);
        }
        super.setAvailable(a);
    }

    ArrayList<Component> reps = new ArrayList<Component>();

    private int _progState = 0;

    // if true, we're read/writing/confirming the low CV
    private boolean programmingLow = true;

    // these mark which operations are needed
    private boolean doLow = false;
    private boolean doHigh = false;

    private static final int IDLE = 0;
    private static final int WRITING_PI4R = 1;  // R, W, C suffix is Read, Write, Confirm
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
     * Define maximum number of retries of read/write operations before moving
     * on
     */
    private static final int RETRY_MAX = 2;

    /**
     * Notify the connected CVs of a state change from above
     *
     */
    public void setCvState(int state) {
        (_cvMap.get(getCvName())).setState(state);
    }

    public void setToRead(boolean state) {
        if (getInfoOnly() || getWriteOnly() || !getAvailable()) {
            state = false;
        }
        (_cvMap.get(getCvName())).setToRead(state);
        (_cvMap.get(mSecondCVname)).setToRead(state);
    }

    public boolean isToRead() {
        return getAvailable()
                && ((_cvMap.get(getCvName())).isToRead() || (_cvMap.get(mSecondCVname)).isToRead());
    }

    public void setToWrite(boolean state) {
        if (getInfoOnly() || getReadOnly() || !getAvailable()) {
            state = false;
        }
        (_cvMap.get(getCvName())).setToWrite(state);
        (_cvMap.get(mSecondCVname)).setToWrite(state);
    }

    public boolean isToWrite() {
        return getAvailable()
                && ((_cvMap.get(getCvName())).isToWrite() || (_cvMap.get(mSecondCVname)).isToWrite());
    }

    public boolean isChanged() {
        CvValue cv1 = (_cvMap.get(getCvName()));
        CvValue cv2 = (_cvMap.get(mSecondCVname));
        return (considerChanged(cv1) || considerChanged(cv2));
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

    public void readAll() {
        if (_upperFirst) {
            programmingLow = false;
            doLow = true;
            doHigh = false;
        } else {
            programmingLow = true;
            doLow = false;
            doHigh = true;
        }
        doRead();
    }

    public void doRead() {
        setBusy(true);  // will be reset when value changes
        setToRead(false);
        if (_progState != IDLE) {
            log.warn("Programming state " + _progState + ", not IDLE, in read()");
        }
        // lets skip the SI step if SI is not used
        if (log.isDebugEnabled()) {
            log.debug("doRead with programmingLow " + programmingLow + " " + getCvName() + " " + mSecondCVname);
        }
        if ((_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).siVal() >= 0) {
            _progState = WRITING_PI4R;
        } else {
            _progState = WRITING_SI4R;
        }
        retries = 0;
        if (log.isDebugEnabled()) {
            log.debug("invoke PI write for CV read");
        }
        // to read any indexed CV we must write the PI
        (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).writePI(_status);
    }

    public void writeAll() {
        if (getReadOnly()) {
            log.error("unexpected write operation when readOnly is set");
        }

        if (_upperFirst) {
            programmingLow = false;
            doLow = true;
            doHigh = false;
        } else {
            programmingLow = true;
            doLow = false;
            doHigh = true;
        }
        doWrite();
    }

    public void doWrite() {
        setBusy(true);  // will be reset when value changes
        setToWrite(false);
        if (_progState != IDLE) {
            log.warn("Programming state " + _progState + ", not IDLE, in write()");
        }
        // lets skip the SI step if SI is not used
        if ((_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).siVal() >= 0) {
            _progState = WRITING_PI4W;
        } else {
            _progState = WRITING_SI4W;
        }
        retries = 0;
        if (log.isDebugEnabled()) {
            log.debug("invoke PI write for CV write " + programmingLow + " " + _progState);
        }
        // to write any indexed CV we must write the PI
        (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).writePI(_status);
    }

    public void confirmAll() {
        if (_upperFirst) {
            programmingLow = false;
            doLow = true;
            doHigh = false;
        } else {
            programmingLow = true;
            doLow = false;
            doHigh = true;
        }

        setBusy(true);  // will be reset when value changes
        setToRead(false);
        if (_progState != IDLE) {
            log.warn("Programming state " + _progState + ", not IDLE, in confirm()");
        }
        // lets skip the SI step if SI is not used
        if ((_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).siVal() >= 0) {
            _progState = WRITING_PI4C;
        } else {
            _progState = WRITING_SI4C;
        }
        retries = 0;
        if (log.isDebugEnabled()) {
            log.debug("invoke PI write for CV confirm");
        }
        // to read any indexed CV we must write the PI
        (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).writePI(_status);
    }

    // handle incoming parameter notification
    public void propertyChange(java.beans.PropertyChangeEvent e) {
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
                case WRITING_PI4R:   // have written the PI, now write SI if needed
                case WRITING_PI4C:
                case WRITING_PI4W:
                    if (log.isDebugEnabled()) {
                        log.debug("Busy goes false with state WRITING_PI");
                    }

                    // check for success
                    if ((retries < RETRY_MAX)
                            && ((_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).getState() != CvValue.STORED)) {
                        // need to retry on error; leave progState as it was
                        log.debug("retry");
                        retries++;
                        (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).writePI(_status);
                        return;
                    }
                    // success, move on to next
                    retries = 0;

                    if (_progState == WRITING_PI4R) {
                        _progState = WRITING_SI4R;
                    } else if (_progState == WRITING_PI4C) {
                        _progState = WRITING_SI4C;
                    } else {
                        _progState = WRITING_SI4W;
                    }
                    (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).writeSI(_status);
                    return;
                case WRITING_SI4R:
                case WRITING_SI4C:
                case WRITING_SI4W:  // have written SI if needed, now read or write CV
                    if (log.isDebugEnabled()) {
                        log.debug("Busy goes false with state WRITING_SI");
                    }

                    // check for success
                    if ((retries < RETRY_MAX)
                            && ((_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).getState() != CvValue.STORED)) {
                        // need to retry on error; leave progState as it was
                        log.debug("retry");
                        retries++;
                        (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).writeSI(_status);
                        return;
                    }
                    // success, move on to next
                    retries = 0;

                    if (_progState == WRITING_SI4R) {
                        _progState = READING_CV;
                        (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).readIcV(_status);
                    } else if (_progState == WRITING_SI4C) {
                        _progState = COMPARE_CV;
                        (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).confirmIcV(_status);
                    } else {
                        _progState = WRITING_CV;
                        (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).writeIcV(_status);
                    }
                    return;
                case READING_CV:  // now done with the read request
                    if (log.isDebugEnabled()) {
                        log.debug("Finished reading the Indexed CV");
                    }

                    // check for success
                    if ((retries < RETRY_MAX)
                            && ((_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).getState() != CvValue.READ)) {
                        // need to retry on error; leave progState as it was
                        log.debug("retry");
                        retries++;
                        (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).readIcV(_status);
                        return;
                    }
                    // success, move on to next
                    retries = 0;

                    _progState = IDLE;
                    if (doHigh) {
                        doHigh = false;
                        programmingLow = false;
                        doRead();
                    } else if (doLow) {
                        doLow = false;
                        programmingLow = true;
                        doRead();
                    } else {
                        programmingLow = true;
                        setBusy(false);
                    }
                    return;
                case COMPARE_CV:  // now done with the read request
                    if (log.isDebugEnabled()) {
                        log.debug("Finished reading the Indexed CV for compare");
                    }

                    // check for success SAME or DIFF?
                    if ((retries < RETRY_MAX)
                            && ((_cvMap.get(programmingLow ? getCvName() : mSecondCVname))
                            .getState() != CvValue.SAME)
                            && ((_cvMap.get(programmingLow ? getCvName() : mSecondCVname))
                            .getState() != CvValue.DIFF)) {
                        // need to retry on error; leave progState as it was
                        log.debug("retry");
                        retries++;
                        (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).confirmIcV(_status);
                    }
                    return;
                case WRITING_CV:  // now done with the write request
                    if (log.isDebugEnabled()) {
                        log.debug("Finished writing the Indexed CV");
                    }

                    // check for success
                    if ((retries < RETRY_MAX)
                            && ((_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).getState() != CvValue.STORED)) {
                        // need to retry on error; leave progState as it was
                        log.debug("retry");
                        retries++;
                        (_cvMap.get(programmingLow ? getCvName() : mSecondCVname)).writeIcV(_status);
                        return;
                    }
                    // success, move on to next
                    retries = 0;

                    _progState = IDLE;
                    if (doHigh) {
                        doHigh = false;
                        programmingLow = false;
                        doWrite();
                    } else if (doLow) {
                        doLow = false;
                        programmingLow = true;
                        doWrite();
                    } else {
                        super.setState(STORED);
                        setBusy(false);
                    }
                    return;
                default:  // unexpected!
                    log.error("Unexpected state found: " + _progState);
                    _progState = IDLE;
                    return;
            }
        } else if (e.getPropertyName().equals("State")) {
            CvValue cvLow = _cvMap.get(getCvName());
            CvValue cvHigh = _cvMap.get(mSecondCVname);
            if (log.isDebugEnabled()) {
                log.debug("  found cvLow=" + cvLow + "   cvHigh=" + cvHigh);
            }
            if (log.isDebugEnabled()) {
                log.debug("CV State changed to " + cvLow.getState());
            }
            if (cvHigh.getState() == VariableValue.UNKNOWN) {
                if (cvLow.getState() == VariableValue.EDITED) {
                    setState(VariableValue.EDITED);
                } else {
                    setState(VariableValue.UNKNOWN);
                }
            } else {
                setState(cvLow.getState());
            }
        } else if (e.getPropertyName().equals("Value")) {
            // update value of Variable
            CvValue cv0 = _cvMap.get(getCvName());
            CvValue cv1 = _cvMap.get(mSecondCVname);
            if (log.isDebugEnabled()) {
                log.debug("  found cv0=" + cv0 + "   cv1=" + cv1);
            }
            int newVal = ((cv0.getValue() & lowerbitmask) >> lowerbitoffset)
                    + (((cv1.getValue() & upperbitmask) * 256) >> upperbitoffset);
            if (log.isDebugEnabled()) {
                log.debug("CV " + getCvName() + "," + getSecondCvNum() + " set value to " + newVal + " based on cv0=" + cv0.getValue() + " cv1=" + cv1.getValue());
            }
            setValue(newVal);  // check for duplicate done inside setVal
            if (log.isDebugEnabled()) {
                log.debug("CV " + getCvName() + "," + getSecondCvNum() + " in property change after setValue call, cv0=" + cv0.getValue() + " cv1=" + cv1.getValue());
            }
            // state change due to CV state change, so propagate that
            if (cv1.getState() == VariableValue.UNKNOWN) {
                if (cv0.getState() == VariableValue.EDITED) {
                    setState(VariableValue.EDITED);
                } else {
                    setState(VariableValue.UNKNOWN);
                }
            } else {
                setState(cv0.getState());
            }
        }
    }

    // stored value
    JTextField _value = null;

    /* Internal class extends a JTextField so that its color is consistent with
     * an underlying variable
     *
     * @author	Bob Jacobsen   Copyright (C) 2001
     * @version     $Revision$
     */
    public class VarTextField extends JTextField {

        /**
         *
         */
        private static final long serialVersionUID = 1338720766156931900L;

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
                    if (log.isDebugEnabled()) {
                        log.debug("focusGained");
                    }
                    enterField();
                }

                public void focusLost(FocusEvent e) {
                    if (log.isDebugEnabled()) {
                        log.debug("focusLost");
                    }
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
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }
        if (_value != null) {
            _value.removeActionListener(this);
            _value.removeFocusListener(this);
            _value.removePropertyChangeListener(this);
            _value = null;
        }
        (_cvMap.get(getCvName())).removePropertyChangeListener(this);
        (_cvMap.get(mSecondCVname)).removePropertyChangeListener(this);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(IndexedPairVariableValue.class.getName());

}
