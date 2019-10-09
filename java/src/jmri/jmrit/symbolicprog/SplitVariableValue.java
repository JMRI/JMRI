package jmri.jmrit.symbolicprog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.text.Document;
import jmri.util.CvUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends VariableValue to represent a variable split across multiple CVs.
 * <br><br>
 * The {@code mask} attribute represents the part of the value that's present in
 * each CV; higher-order bits are loaded to subsequent CVs.
 * <br><br>
 * The original use was for addresses of stationary (accessory) decoders.
 * <br><br>
 * The original version only allowed two CVs, with the second CV specified by
 * the attributes {@code highCV} and {@code upperMask}.
 * <br><br>
 * The preferred technique is now to specify all CVs in the {@code CV} attribute
 * alone, as documented at {@link CvUtil#expandCvList expandCvList(String)}.
 * <br><br>
 * Optional attributes {@code factor} and {@code offset} are applied when going
 * <i>from</i> the variable value <i>to</i> the CV values, or vice-versa:
 * <pre>
 * Value to put in CVs = ((value in text field) -{@code offset})/{@code factor}
 * Value to put in text field = ((value in CVs) *{@code factor}) +{@code offset}
 * </pre>
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2003, 2004, 2013
 * @author Dave Heap Copyright (C) 2016, 2019
 *
 */
public class SplitVariableValue extends VariableValue
        implements ActionListener, FocusListener {

    private static final int RETRY_COUNT = 2;

    public SplitVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        _minVal = 0;
        _maxVal = ~0;
        stepOneActions(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname, pSecondCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        _name = name;
        _mask = mask;
        _cvNum = cvNum;
        _textField = new JTextField("0");
        _defaultColor = _textField.getBackground();
        _textField.setBackground(COLOR_UNKNOWN);
        mFactor = pFactor;
        mOffset = pOffset;
        // legacy format variables
        mSecondCV = pSecondCV;
        _uppermask = uppermask;

        // connect to the JTextField value
        _textField.addActionListener(this);
        _textField.addFocusListener(this);

        log.debug("Variable={};comment={};cvName={};cvNum={};stdname={}", _name, comment, cvName, _cvNum, stdname);

        // upper bit offset includes lower bit offset, and MSB bits missing from upper part
        log.debug("Variable={}; upper mask {} had offsetVal={} so upperbitoffset={}", _name, _uppermask, offsetVal(_uppermask), offsetVal(_uppermask));

        // set up array of used CVs
        cvList = new ArrayList<>();

        List<String> nameList = CvUtil.expandCvList(_cvNum); // see if cvName needs expanding
        if (nameList.isEmpty()) {
            // primary CV
            cvList.add(new CvItem(_cvNum, mask));

            if (pSecondCV != null && !pSecondCV.equals("")) {
                cvList.add(new CvItem(pSecondCV, _uppermask));
            }
        } else {
            for (String s : nameList) {
                cvList.add(new CvItem(s, mask));
            }
        }

        cvCount = cvList.size();

        for (int i = 0; i < cvCount; i++) {
            cvList.get(i).startOffset = currentOffset;
            String t = cvList.get(i).cvMask;
            while (t.length() > 0) {
                if (t.startsWith("V")) {
                    currentOffset++;
                }
                t = t.substring(1);
            }
            if (log.isDebugEnabled()) {
                log.debug("cvName={};cvMask={};startOffset={}", cvList.get(i).cvName, cvList.get(i).cvMask, cvList.get(i).startOffset);
            }

            // connect CV for notification
            CvValue cv = (_cvMap.get(cvList.get(i).cvName));
            cvList.get(i).thisCV = cv;
        }

        stepTwoActions();

        _textField.setColumns(_columns);

        // have to do when list is complete
        for (int i = 0; i < cvCount; i++) {
            cvList.get(i).thisCV.addPropertyChangeListener(this);
            cvList.get(i).thisCV.setState(CvValue.FROMFILE);
        }
    }

    /**
     * Subclasses can override this to pick up constructor-specific attributes
     * and perform other actions before cvList has been built.
     */
    public void stepOneActions(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        if (extra3 != null) {
            _minVal = getValueFromText(extra3);
        }
        if (extra4 != null) {
            _maxVal = getValueFromText(extra4);
        }
    }

    /**
     * subclasses can override this to invoke further actions after cvList has
     * been built
     */
    public void stepTwoActions() {
        if (currentOffset > bitCount) {
            String eol = System.getProperty("line.separator");
            throw new Error(
                    "Decoder File parsing error:"
                    + eol + "The Decoder Definition File specified \"" + _cvNum
                    + "\" for variable \"" + _name + "\". This expands to:"
                    + eol + "\"" + getCvDescription() + "\""
                    + eol + "This requires " + currentOffset + " bits, which exceeds the " + bitCount
                    + " bit capacity of the long integer used to store the variable."
                    + eol + "The Decoder Definition File needs correction.");
        }
        _columns = cvCount * 2; //update column width now we have a better idea
    }

    @Override
    public CvValue[] usesCVs() {
        CvValue[] theseCvs = new CvValue[cvCount];
        for (int i = 0; i < cvCount; i++) {
            theseCvs[i] = cvList.get(i).thisCV;
        }
        return theseCvs;
    }

    /**
     * There are multiple masks for the CVs accessed by this variable.
     * <br>
     * Actual individual masks are returned in
     * {@link #getCvDescription getCvDescription()}.
     *
     * @return The legacy two-CV mask if {@code highCV} is specified.
     * <br>
     * The {@code mask} if {@code highCV} is not specified.
     */
    @Override
    public String getMask() {
        if (mSecondCV != null && !mSecondCV.equals("")) {
            return _uppermask + _mask;
        } else {
            return _mask;
        }
    }

    /**
     * Provide a user-readable description of the CVs accessed by this variable.
     * <br><br>
     * Actual individual masks are added to CVs in this method.
     *
     * @return A user-friendly CV(s) and bitmask(s) description.
     */
    @Override
    public String getCvDescription() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < cvCount; i++) {
            if (buf.length() > 0) {
                buf.append(" & ");
            }
            buf.append("CV");
            buf.append(cvList.get(i).cvName);
            String temp = CvUtil.getMaskDescription(cvList.get(i).cvMask);
            if (temp.length() > 0) {
                buf.append(" ");
                buf.append(temp);
            }
        }
        return buf.toString();
    }

    String mSecondCV;
    String _uppermask;
    int mFactor;
    int mOffset;
    String _name;
    String _mask;
    String _cvNum;

    List<CvItem> cvList;

    int cvCount = 0;
    int currentOffset = 0;

    @Override
    public String getCvNum() {
        String retString = "";
        if (cvCount > 0) {
            retString = cvList.get(0).cvName;
        }
        return retString;
    }

    @Deprecated
    public String getSecondCvNum() {
        String retString = "";
        if (cvCount > 1) {
            retString = cvList.get(1).cvName;
        }
        return retString;
    }

    @Override
    public void setToolTipText(String t) {
        super.setToolTipText(t);   // do default stuff
        _textField.setToolTipText(t);  // set our value
    }

    // the connection is to cvNum and cvNum+1
    long _minVal;
    long _maxVal;

    @Override
    public Object rangeVal() {
        return "Split value";
    }

    String oldContents = "";

    long getValueFromText(String s) {
        return (Long.parseUnsignedLong(s));
    }

    String getTextFromValue(long v) {
        return (Long.toUnsignedString(v));
    }

    int[] getCvValsFromTextField() {
        long newEntry;  // entered value
        try {
            newEntry = getValueFromText(_textField.getText());
        } catch (java.lang.NumberFormatException ex) {
            newEntry = 0;
        }

        // calculate resulting number
        long newVal = (newEntry - mOffset) / mFactor;
        log.debug("Variable={};newEntry={};newVal={} with Offset={} + Factor={} applied", _name, newEntry, newVal, mOffset, mFactor);

        int[] retVals = new int[cvCount];

        // extract individual values via masks
        for (int i = 0; i < cvCount; i++) {
            retVals[i] = (((int) (newVal >>> cvList.get(i).startOffset))
                    & (maskValAsInt(cvList.get(i).cvMask) >>> offsetVal(cvList.get(i).cvMask)));
        }
        return retVals;
    }

    /**
     * Contains numeric-value specific code.
     * <br><br>
     * Calculates new value for _textField and invokes
     * {@link #setLongValue(long) setLongValue(newVal)} to make and notify the
     * change
     *
     * @param intVals array of new CV values
     */
    void updateVariableValue(int[] intVals) {

        long newVal = 0;
        for (int i = 0; i < intVals.length; i++) {
            newVal = newVal | (((long) intVals[i]) << cvList.get(i).startOffset);
            log.debug("Variable={}; i={}; newVal={}", _name, i, getTextFromValue(newVal));
        }
        log.debug("Variable={}; set value to {}", _name, newVal);
        setLongValue(newVal);  // check for duplicate is done inside setLongValue
        log.debug("Variable={}; in property change after setValue call", _name);
    }

    /**
     * Saves contents of _textField to oldContents.
     */
    void enterField() {
        oldContents = _textField.getText();
    }

    /**
     * Contains numeric-value specific code.
     * <br><br>
     * firePropertyChange for "Value" with new and old contents of _textField
     */
    void exitField() {
        // there may be a lost focus event left in the queue when disposed so protect
        if (_textField != null && !oldContents.equals(_textField.getText())) {
            long newFieldVal = getValueFromText(_textField.getText());
            log.debug("_minVal = {},_maxVal = {},newFieldVal = {}", _minVal, _maxVal, newFieldVal);
            if (newFieldVal < _minVal || newFieldVal > _maxVal) {
                _textField.setText(oldContents);
            } else {
                long newVal = (newFieldVal - mOffset) / mFactor;
                long oldVal = (getValueFromText(oldContents) - mOffset) / mFactor;
                log.debug("Enter updatedTextField from exitField");
                updatedTextField();
                prop.firePropertyChange("Value", oldVal, newVal);
            }
        }
    }

    boolean _fieldShrink = false;

    @Override
    void updatedTextField() {
        log.debug("Variable='{}'; enter updatedTextField in {} with TextField='{}'", _name, (this.getClass().getSimpleName()), _textField.getText());
        // called for new values in text field - set the CVs as needed

        int[] retVals = getCvValsFromTextField();

        // combine with existing values via mask
        for (int j = 0; j < cvCount; j++) {
            int i = j;
            // special care needed if _textField is shrinking
            if (_fieldShrink) {
                i = (cvCount - 1) - j; // reverse CV updating order
            }
            log.debug("retVals[{}]={};cvList.get({}).cvMask{};offsetVal={}", i, retVals[i], i, cvList.get(i).cvMask, offsetVal(cvList.get(i).cvMask));
            int cvMask = maskValAsInt(cvList.get(i).cvMask);
            CvValue thisCV = cvList.get(i).thisCV;
            int oldCvVal = thisCV.getValue();
            int newCvVal = (oldCvVal & ~cvMask)
                    | ((retVals[i] << offsetVal(cvList.get(i).cvMask)) & cvMask);
            log.debug("{};cvMask={};oldCvVal={};retVals[{}]={};newCvVal={}", cvList.get(i).cvName, cvMask, oldCvVal, i, retVals[i], newCvVal);

            // cv updates here trigger updated property changes, which means
            // we're going to get notified sooner or later.
            if (newCvVal != oldCvVal) {
                thisCV.setValue(newCvVal);
            }
        }
        log.debug("Variable={}; exit updatedTextField", _name);
    }

    /**
     * ActionListener implementations
     */
    /**
     * Contains numeric-value specific code.
     * <br><br>
     * invokes {@link #updatedTextField updatedTextField()}
     * <br><br>
     * firePropertyChange for "Value" with new contents of _textField
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        log.debug("Variable='{}'; actionPerformed", _name);
        long newVal = (getValueFromText(_textField.getText()) - mOffset) / mFactor;
        log.debug("Enter updatedTextField from actionPerformed");
        updatedTextField();
        prop.firePropertyChange("Value", null, newVal);
    }

    /**
     * FocusListener implementations
     */
    @Override
    public void focusGained(FocusEvent e) {
        log.debug("Variable={}; focusGained", _name);
        enterField();
    }

    @Override
    public void focusLost(FocusEvent e) {
        log.debug("Variable={}; focusLost", _name);
        exitField();
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    @Override
    public String getValueString() {
        log.debug("getValueString {}", _textField.getText());
        return _textField.getText();
    }

    /**
     * Set value from a String value.
     */
    @Override
    public void setValue(String value) {
        try {
            long val = Long.parseUnsignedLong(value);
            setLongValue(val);
        } catch (NumberFormatException e) {
            log.debug("skipping set of non-long value \"{}\"", value);
        }
    }

    @Override
    public void setIntValue(int i) {
        setLongValue(i);
    }

    @Override
    public int getIntValue() {
        long x = getLongValue();
        long y = x & intMask;
        if ((Long.compareUnsigned(x, y) != 0)) {
            log.error("Value {} cannot be converted to 'int'", x);
        }
        return (int) ((getValueFromText(_textField.getText()) - mOffset) / mFactor);
    }

    /**
     * Get the value as an unsigned long.
     *
     * @return the value as a long
     */
    @Override
    public long getLongValue() {
        return ((getValueFromText(_textField.getText()) - mOffset) / mFactor);
    }

    @Override
    public Object getValueObject() {
        return getLongValue();
    }

    @Override
    public Component getCommonRep() {
        if (getReadOnly()) {
            JLabel r = new JLabel(_textField.getText());
            updateRepresentation(r);
            return r;
        } else {
            return _textField;
        }
    }

    public void setLongValue(long value) {
        log.debug("Variable={}; enter setValue {}", _name, value);
        long oldVal;
        try {
            oldVal = (getValueFromText(_textField.getText()) - mOffset) / mFactor;
        } catch (java.lang.NumberFormatException ex) {
            oldVal = -999;
        }
        log.debug("Variable={}; setValue with new value {} old value {}", _name, value, oldVal);
        _textField.setText(getTextFromValue(value * mFactor + mOffset));
        if (oldVal != value || getState() == VariableValue.UNKNOWN) {
            actionPerformed(null);
        }
        // PENDING: the code used to fire value * mFactor + mOffset, which is a text representation;
        // but 'oldValue' was converted back using mOffset / mFactor making those two (new / old) 
        // using different scales. Probably a bug, but it has been there from well before
        // the extended spltVal. Because of the risk of breaking existing
        // behaviour somewhere, deferring correction until at least the next test release.
        prop.firePropertyChange("Value", oldVal, value * mFactor + mOffset);
        log.debug("Variable={}; exit setLongValue old={} new={}", _name, oldVal, value);
    }

    Color _defaultColor;

    // implement an abstract member to set colors
    @Override
    void setColor(Color c) {
        if (c != null) {
            _textField.setBackground(c);
        } else {
            _textField.setBackground(_defaultColor);
        }
        // prop.firePropertyChange("Value", null, null);
    }

    int _columns = 1;

    @Override
    public Component getNewRep(String format) {
        JTextField value = new VarTextField(_textField.getDocument(), _textField.getText(), _columns, this);
        if (getReadOnly() || getInfoOnly()) {
            value.setEditable(false);
        }
        reps.add(value);
        return updateRepresentation(value);
    }

    @Override
    public void setAvailable(boolean a) {
        _textField.setVisible(a);
        for (Component c : reps) {
            c.setVisible(a);
        }
        super.setAvailable(a);
    }

    java.util.List<Component> reps = new java.util.ArrayList<>();

    private int retry = 0;
    private int _progState = 0;
    private static final int IDLE = 0;
    private static final int READING_FIRST = 1;
    private static final int WRITING_FIRST = -1;
    private static final int bitCount = Long.bitCount(~0);
    private static final long intMask = Integer.toUnsignedLong(~0);

    /**
     * Notify the connected CVs of a state change from above
     *
     * @param state The new state
     */
    @Override
    public void setCvState(int state) {
        for (int i = 0; i < cvCount; i++) {
            cvList.get(i).thisCV.setState(state);
        }
    }

    @Override
    public boolean isChanged() {
        boolean changed = false;
        for (int i = 0; i < cvCount; i++) {
            changed = (changed || considerChanged(cvList.get(i).thisCV));
        }
        return changed;
    }

    @Override
    public boolean isToRead() {
        boolean toRead = false;
        for (int i = 0; i < cvCount; i++) {
            toRead = (toRead || (cvList.get(i).thisCV).isToRead());
        }
        return toRead;
    }

    @Override
    public boolean isToWrite() {
        boolean toWrite = false;
        for (int i = 0; i < cvCount; i++) {
            toWrite = (toWrite || (cvList.get(i).thisCV).isToWrite());
        }
        return toWrite;
    }

    @Override
    public void readChanges() {
        if (isToRead() && !isChanged()) {
            log.debug("!!!!!!! unacceptable combination in readChanges: {}", label());
        }
        if (isChanged() || isToRead()) {
            readAll();
        }
    }

    @Override
    public void writeChanges() {
        if (isToWrite() && !isChanged()) {
            log.debug("!!!!!! unacceptable combination in writeChanges: {}", label());
        }
        if (isChanged() || isToWrite()) {
            writeAll();
        }
    }

    @Override
    public void readAll() {
        log.debug("Variable={}; splitVal read() invoked", _name);
        setToRead(false);
        setBusy(true);  // will be reset when value changes
        //super.setState(READ);
        if (_progState != IDLE) {
            log.warn("Variable={}; programming state {}, not IDLE, in read()", _name, _progState);
        }
        _textField.setText(""); // start with a clean slate
        for (int i = 0; i < cvCount; i++) { // mark all Cvs as unknown otherwise problems occur
            cvList.get(i).thisCV.setState(AbstractValue.UNKNOWN);
        }
        _progState = READING_FIRST;
        retry = 0;
        log.debug("Variable={}; invoke CV read", _name);
        (cvList.get(0).thisCV).read(_status); // kick off the read sequence
    }

    @Override
    public void writeAll() {
        log.debug("Variable={}; write() invoked", _name);
        if (getReadOnly()) {
            log.error("Variable={}; unexpected write operation when readOnly is set", _name);
        }
        setToWrite(false);
        setBusy(true);  // will be reset when value changes
        if (_progState != IDLE) {
            log.warn("Variable={}; Programming state {}, not IDLE, in write()", _name, _progState);
        }
        _progState = WRITING_FIRST;
        log.debug("Variable={}; invoke CV write", _name);
        (cvList.get(0).thisCV).write(_status); // kick off the write sequence
    }

    /**
     * Assigns a priority value to a given state.
     */
    @SuppressFBWarnings(value = {"SF_SWITCH_NO_DEFAULT", "SF_SWITCH_FALLTHROUGH"}, justification = "Intentional fallthrough to produce correct value")
    int priorityValue(int state) {
        int value = 0;
        switch (state) {
            case AbstractValue.UNKNOWN:
                value++;
            //$FALL-THROUGH$
            case AbstractValue.DIFF:
                value++;
            //$FALL-THROUGH$
            case AbstractValue.EDITED:
                value++;
            //$FALL-THROUGH$
            case AbstractValue.FROMFILE:
                value++;
            //$FALL-THROUGH$
            default:
                //$FALL-THROUGH$
                return value;
        }
    }

    // handle incoming parameter notification
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("Variable={}; property changed event - name: {}", _name, e.getPropertyName());
        // notification from CV; check for Value being changed
        if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
            // busy transitions drive the state
            if (log.isDebugEnabled() && _progState != IDLE) {
                log.debug("getState() = {}", (cvList.get(Math.abs(_progState) - 1).thisCV).getState());
            }

            if (_progState == IDLE) { // no, just a CV update
                log.error("Variable={}; Busy goes false with state IDLE", _name);
            } else if (_progState >= READING_FIRST) {   // reading CVs
                if ((cvList.get(Math.abs(_progState) - 1).thisCV).getState() == READ) {   // was the last read successful?
                    retry = 0;
                    if (Math.abs(_progState) < cvCount) {   // read next CV
                        _progState++;
                        if (log.isDebugEnabled()) {
                            log.debug("Reading CV={}", cvList.get(Math.abs(_progState) - 1).cvName);
                        }
                        (cvList.get(Math.abs(_progState) - 1).thisCV).read(_status);
                    } else {  // finally done, set not busy
                        log.debug("Variable={}; Busy goes false with success READING state {}", _name, _progState);
                        _progState = IDLE;
                        setBusy(false);
                    }
                } else {   // read failed
                    log.debug("Variable={}; Busy goes false with failure READING state {}", _name, _progState);
                    if (retry < RETRY_COUNT) { //have we exhausted retry count?
                        retry++;
                        (cvList.get(Math.abs(_progState) - 1).thisCV).read(_status);
                    } else {
                        _progState = IDLE;
                        setBusy(false);
                        if (RETRY_COUNT > 0) {
                            for (int i = 0; i < cvCount; i++) { // mark all CVs as unknown otherwise problems may occur
                                cvList.get(i).thisCV.setState(AbstractValue.UNKNOWN);
                            }
                        }

                    }
                }
            } else {  // writing CVs
                if ((cvList.get(Math.abs(_progState) - 1).thisCV).getState() == STORED) {   // was the last read successful?
                    if (Math.abs(_progState) < cvCount) {   // write next CV
                        _progState--;
                        if (log.isDebugEnabled()) {
                            log.debug("Writing CV={}", cvList.get(Math.abs(_progState) - 1).cvName);
                        }
                        (cvList.get(Math.abs(_progState) - 1).thisCV).write(_status);
                    } else {  // finally done, set not busy
                        log.debug("Variable={}; Busy goes false with success WRITING state {}", _name, _progState);
                        _progState = IDLE;
                        setBusy(false);
                    }
                } else {   // read failed we're done!
                    log.debug("Variable={}; Busy goes false with failure WRITING state {}", _name, _progState);
                    _progState = IDLE;
                    setBusy(false);
                }
            }
        } else if (e.getPropertyName().equals("State")) {
            log.debug("state change due to CV state change, so propagate that");
            int varState = getState(); // AbstractValue.SAME;
            log.debug("{} state was {}", _name, varState);
            for (int i = 0; i < cvCount; i++) {
                int state = cvList.get(i).thisCV.getState();
                if (i == 0) {
                    varState = state;
                } else if (priorityValue(state) > priorityValue(varState)) {
                    varState = AbstractValue.UNKNOWN; // or should it be = state ?
                }
            }
            setState(varState);
            log.debug("{} state set to {}", _name, varState);
        } else if (e.getPropertyName().equals("Value")) {
            // update value of Variable
            log.debug("update value of Variable");

            int[] intVals = new int[cvCount];

            for (int i = 0; i < cvCount; i++) {
                intVals[i] = (cvList.get(i).thisCV.getValue() & maskValAsInt(cvList.get(i).cvMask)) >>> offsetVal(cvList.get(i).cvMask);
            }

            updateVariableValue(intVals);

            log.debug("state change due to CV value change, so propagate that");
            int varState = AbstractValue.SAME;
            for (int i = 0; i < cvCount; i++) {
                int state = cvList.get(i).thisCV.getState();
                if (priorityValue(state) > priorityValue(varState)) {
                    varState = state;
                }
            }
            setState(varState);
        }
    }

    // stored reference to the JTextField
    JTextField _textField = null;

    /* Internal class extends a JTextField so that its color is consistent with
     * an underlying variable
     *
     * @author Bob Jacobsen   Copyright (C) 2001
     *
     */
    public class VarTextField extends JTextField {

        VarTextField(Document doc, String text, int col, SplitVariableValue var) {
            super(doc, text, col);
            _var = var;
            // get the original color right
            setBackground(_var._textField.getBackground());
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
                    log.debug("Variable={}; focusGained", _name);
                    enterField();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    log.debug("Variable={}; focusLost", _name);
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

        SplitVariableValue _var;

        void thisActionPerformed(java.awt.event.ActionEvent e) {
            // tell original
            _var.actionPerformed(e);
        }

        void originalPropertyChanged(java.beans.PropertyChangeEvent e) {
            // update this color from original state
            if (e.getPropertyName().equals("State")) {
                setBackground(_var._textField.getBackground());
            }
        }

    }

    /**
     * Class to hold CV parameters for CVs used.
     */
    static class CvItem {

        // class fields
        String cvName;
        String cvMask;
        int startOffset;
        CvValue thisCV;

        CvItem(String cvNameVal, String cvMaskVal) {
            cvName = cvNameVal;
            cvMask = cvMaskVal;
        }
    }

    // clean up connections when done
    @Override
    public void dispose() {
        if (log.isDebugEnabled()) {
            log.debug("dispose");
        }
        if (_textField != null) {
            _textField.removeActionListener(this);
        }
        for (int i = 0; i < cvCount; i++) {
            (_cvMap.get(cvList.get(i).cvName)).removePropertyChangeListener(this);
        }

        _textField = null;
        // do something about the VarTextField
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(SplitVariableValue.class);

}
