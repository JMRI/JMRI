package jmri.jmrit.symbolicprog;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeListener;
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
 * The mask represents the part of the value that's present in each CV;
 * higher-order bits are loaded to subsequent CVs.
 * <br><br>
 * The original use was for addresses of stationary (accessory) decoders.
 * <br><br><br>
 * The original version only allowed two CVs, with the second CV specified by
 * the attributes {@code highCV} and {@code upperMask}.
 * <br><br>
 * The preferred technique is now to specify all CVs in the {@code CV} attribute
 * alone, as documented at {@link CvUtil#expandCVlist expandCVlist(String)}.
 * <br><br>
 * Attributes {@code factor} and {@code offset} are applied when going <i>to</i>
 * value of the variable
 * <i>to</i> the CV values:
 * <pre>
 * Value to put in CVs = ((value in text field) -{@code offset})/{@code factor}
 * Value to put in text field = ((value in CVs) *{@code factor}) +{@code offset}
 * </pre>
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2003, 2004, 2013
 * @author Dave Heap Copyright (C) 2016
 *
 */
public class SplitVariableValue extends VariableValue
        implements ActionListener, PropertyChangeListener, FocusListener {

    private static final int RETRY_COUNT = 2;

    public SplitVariableValue(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
        super(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, v, status, stdname);
        stepOneActions(name, comment, cvName, readOnly, infoOnly, writeOnly, opsOnly, cvNum, mask, minVal, maxVal, v, status, stdname, pSecondCV, pFactor, pOffset, uppermask, extra1, extra2, extra3, extra4);
        _name = name;
        _mask = mask;
        _maxVal = maxVal;
        _minVal = minVal;
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

        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + ";comment=" + comment + ";cvName=" + cvName + ";cvNum=" + _cvNum + ";stdname=" + stdname);
        }

        // upper bit offset includes lower bit offset, and MSB bits missing from upper part
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; upper mask " + _uppermask + " had offsetVal=" + offsetVal(_uppermask)
                    + " so upperbitoffset=" + offsetVal(_uppermask));
        }

        // set up array of used CVs
        cvList = new ArrayList<>();

        List<String> nameList = CvUtil.expandCVlist(_cvNum); // see if cvName needs expanding
        if (nameList.isEmpty()) {
            // primary CV
            cvList.add(new CvItem(_cvNum, mask));

            if (pSecondCV != null) {
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
                log.debug("cvName=" + cvList.get(i).cvName + ";cvMask=" + cvList.get(i).cvMask + ";startOffset=" + cvList.get(i).startOffset);
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
     * subclasses can override this to invoke custom pre super constructor
     * actions
     */
    public void stepOneActions(String name, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, int minVal, int maxVal,
            HashMap<String, CvValue> v, JLabel status, String stdname,
            String pSecondCV, int pFactor, int pOffset, String uppermask, String extra1, String extra2, String extra3, String extra4) {
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
     *
     * Returns the default mask for compatibility.
     * <p>
     * Actual individual masks are added in
     * {@link #getCvDescription getCvDescription()}.
     */
    @Override
    public String getMask() {
        return "VVVVVVVV";
    }

    /**
     * Provide a user-readable description of the CVs accessed by this variable.
     * <br><br>
     * <p>
     * Actual individual masks are added to CVs in this method.
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

    @Deprecated
    String mSecondCV;
    @Deprecated
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
    int _maxVal;
    int _minVal;

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
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + ";newEntry=" + newEntry + ";newVal=" + newVal + " with Offset=" + mOffset + " & Factor=" + mFactor + " applied");
        }

        int[] retVals = new int[cvCount];

        // extract individual values via masks
        for (int i = 0; i < cvCount; i++) {
            retVals[i] = (((int) (newVal >>> cvList.get(i).startOffset))
                    & (maskVal(cvList.get(i).cvMask) >>> offsetVal(cvList.get(i).cvMask)));
        }
        return retVals;
    }

    /**
     * Contains numeric-value specific code.
     * <br><br>
     * Calculates new value for _textField and invokes
     * {@link #setValue(long) setValue(newVal)} to make and notify the change
     *
     * @param intVals array of new CV values
     */
    void updateVariableValue(int[] intVals) {

        long newVal = 0;
        for (int i = 0; i < intVals.length; i++) {
//            log.debug("intVals[" + i + "]=" + intVals[i] + ";offsetVal=" + offsetVal(cvList.get(i).cvMask) + ";startOffset=" + cvList.get(i).startOffset);
            newVal = newVal + ((long) (intVals[i] << cvList.get(i).startOffset));
        }
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; set value to " + newVal);
        }
        setValue(newVal);  // check for duplicate is done inside setValue
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; in property change after setValue call");
        }
    }

    /**
     * saves contents of _textField to oldContents
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
//            log.debug("Enter updatedTextField from exitField");
                updatedTextField();
                prop.firePropertyChange("Value", oldVal, newVal);
            }
        }
    }

    boolean _fieldShrink = false;

    @Override
    void updatedTextField() {
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; enter updatedTextField in " + (this.getClass().getSimpleName()));
        }
        // called for new values in text field - set the CVs as needed

        int[] retVals = getCvValsFromTextField();

        // combine with existing values via mask
        for (int j = 0; j < cvCount; j++) {
            int i = j;
            // special care needed if _textField is shrinking 
            if (_fieldShrink) {
                i = (cvCount - 1) - j; // reverse CV updating order
            }
//            log.debug("retVals[" + i + "]=" + retVals[i] + ";cvList.get(" + i + ").cvMask" + cvList.get(i).cvMask + ";offsetVal=" + offsetVal(cvList.get(i).cvMask));
            int cvMask = maskVal(cvList.get(i).cvMask);
            CvValue thisCV = cvList.get(i).thisCV;
            int oldCvVal = thisCV.getValue();
            int newCvVal = (oldCvVal & ~cvMask)
                    | ((retVals[i] << offsetVal(cvList.get(i).cvMask)) & cvMask);
            if (log.isDebugEnabled()) {
                log.debug(cvList.get(i).cvName + ";cvMask=" + cvMask + ";oldCvVal=" + oldCvVal + ";retVals[i]=" + retVals[i] + ";newCvVal=" + newCvVal);
            }

            // cv updates here trigger updated property changes, which means
            // we're going to get notified sooner or later.
            if (newCvVal != oldCvVal) {
                thisCV.setValue(newCvVal);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; exit updatedTextField");
        }
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
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; actionPerformed");
        }
        long newVal = (getValueFromText(_textField.getText()) - mOffset) / mFactor;
//        log.debug("Enter updatedTextField from actionPerformed");
        updatedTextField();
        prop.firePropertyChange("Value", null, newVal);
    }

    /**
     * FocusListener implementations
     */
    @Override
    public void focusGained(FocusEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; focusGained");
        }
        enterField();
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; focusLost");
        }
        exitField();
    }

    // to complete this class, fill in the routines to handle "Value" parameter
    // and to read/write/hear parameter changes.
    @Override
    public String getValueString() {
        return _textField.getText();
    }

    /**
     * Set value from a String value.
     */
    @Override
    public void setValue(String value) {
        try {
            long val = Long.parseLong(value);
            setValue(val);
        } catch (NumberFormatException e) {
            log.debug("skipping set of non-long value \"{}\"", value);
        }
    }

    @Override
    public void setIntValue(int i) {
        setValue((long) i);
    }

    @Override
    public int getIntValue() {
        return (int) ((getValueFromText(_textField.getText()) - mOffset) / mFactor);
    }

    @Override
    public Object getValueObject() {
        return Integer.valueOf(_textField.getText());
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

    public void setValue(long value) {
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; enter setValue " + value);
        }
        long oldVal;
        try {
            oldVal = (getValueFromText(_textField.getText()) - mOffset) / mFactor;
        } catch (java.lang.NumberFormatException ex) {
            oldVal = -999;
        }
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; setValue with new value " + value + " old value " + oldVal);
        }
        _textField.setText(getTextFromValue(value * mFactor + mOffset));
        if (oldVal != value || getState() == VariableValue.UNKNOWN) {
            actionPerformed(null);
        }
        prop.firePropertyChange("Value", oldVal, value * mFactor + mOffset);
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; exit setValue " + value);
        }
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
            log.debug("!!!!!!! unacceptable combination in readChanges: " + label());
        }
        if (isChanged() || isToRead()) {
            readAll();
        }
    }

    @Override
    public void writeChanges() {
        if (isToWrite() && !isChanged()) {
            log.debug("!!!!!! unacceptable combination in writeChanges: " + label());
        }
        if (isChanged() || isToWrite()) {
            writeAll();
        }
    }

    @Override
    public void readAll() {
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; splitVal read() invoked");
        }
        setToRead(false);
        setBusy(true);  // will be reset when value changes
        //super.setState(READ);
        if (_progState != IDLE) {
            log.warn("Variable=" + _name + "; programming state " + _progState + ", not IDLE, in read()");
        }
        _textField.setText(""); // start with a clean slate
        for (int i = 0; i < cvCount; i++) { // mark all Cvs as unknown otherwise problems occur
            cvList.get(i).thisCV.setState(AbstractValue.UNKNOWN);
        }
        _progState = READING_FIRST;
        retry = 0;
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; invoke CV read");
        }
        (cvList.get(0).thisCV).read(_status); // kick off the read sequence
    }

    @Override
    public void writeAll() {
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; write() invoked");
        }
        if (getReadOnly()) {
            log.error("Variable=" + _name + "; unexpected write operation when readOnly is set");
        }
        setToWrite(false);
        setBusy(true);  // will be reset when value changes
        if (_progState != IDLE) {
            log.warn("Variable=" + _name + "; Programming state " + _progState + ", not IDLE, in write()");
        }
        _progState = WRITING_FIRST;
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; invoke CV write");
        }
        (cvList.get(0).thisCV).write(_status); // kick off the write sequence
    }

    /**
     * Assigns a priority value to a given state.
     */
    @SuppressWarnings({"SF_SWITCH_NO_DEFAULT", "SF_SWITCH_FALLTHROUGH"})
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
                return value;
        }
    }

    // handle incoming parameter notification
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (log.isDebugEnabled()) {
            log.debug("Variable=" + _name + "; property changed event - name: "
                    + e.getPropertyName());
        }
        // notification from CV; check for Value being changed
        if (e.getPropertyName().equals("Busy") && ((Boolean) e.getNewValue()).equals(Boolean.FALSE)) {
            // busy transitions drive the state
            if (log.isDebugEnabled() && _progState != IDLE) {
                log.debug("getState() = " + (cvList.get(Math.abs(_progState) - 1).thisCV).getState());
            }

            if (_progState == IDLE) { // no, just a CV update
                if (log.isDebugEnabled()) {
                    log.error("Variable=" + _name + "; Busy goes false with state IDLE");
                }
            } else if (_progState >= READING_FIRST) {   // reading CVs
                if ((cvList.get(Math.abs(_progState) - 1).thisCV).getState() == READ) {   // was the last read successful?
                    retry = 0;
                    if (Math.abs(_progState) < cvCount) {   // read next CV
                        _progState++;
                        if (log.isDebugEnabled()) {
                            log.debug("Reading CV=" + cvList.get(Math.abs(_progState) - 1).cvName);
                        }
                        (cvList.get(Math.abs(_progState) - 1).thisCV).read(_status);
                    } else {  // finally done, set not busy
                        if (log.isDebugEnabled()) {
                            log.debug("Variable=" + _name + "; Busy goes false with success READING state " + _progState);
                        }
                        _progState = IDLE;
                        setBusy(false);
                    }
                } else {   // read failed
                    if (log.isDebugEnabled()) {
                        log.debug("Variable=" + _name + "; Busy goes false with failure READING state " + _progState);
                    }
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
            } else if (_progState <= WRITING_FIRST) {   // writing CVs
                if ((cvList.get(Math.abs(_progState) - 1).thisCV).getState() == STORED) {   // was the last read successful?
                    if (Math.abs(_progState) < cvCount) {   // write next CV
                        _progState--;
                        if (log.isDebugEnabled()) {
                            log.debug("Writing CV=" + cvList.get(Math.abs(_progState) - 1).cvName);
                        }
                        (cvList.get(Math.abs(_progState) - 1).thisCV).write(_status);
                    } else {  // finally done, set not busy
                        if (log.isDebugEnabled()) {
                            log.debug("Variable=" + _name + "; Busy goes false with success WRITING state " + _progState);
                        }
                        _progState = IDLE;
                        setBusy(false);
                    }
                } else {   // read failed we're done!
                    if (log.isDebugEnabled()) {
                        log.debug("Variable=" + _name + "; Busy goes false with failure WRITING state " + _progState);
                    }
                    _progState = IDLE;
                    setBusy(false);
                }
//            } else {  // unexpected!
//                log.error("Variable=" + _name + "; Unexpected state found: " + _progState);
//                _progState = IDLE;
//                return;
            }
        } else if (e.getPropertyName().equals("State")) {
            // state change due to CV state change, so propagate that
            log.debug("state change due to CV state change, so propagate that");
            int varState = getState();// AbstractValue.SAME;
            log.debug(_name + " state was " + varState);
            for (int i = 0; i < cvCount; i++) {
                int state = cvList.get(i).thisCV.getState();
                if (i == 0) {
                    varState = state;
                } else if (priorityValue(state) > priorityValue(varState)) {
                    varState = AbstractValue.UNKNOWN; // or should it be = state ?
                }
//                if (priorityValue(state) > priorityValue(varState)) {
//                    varState = state;
//                }
            }
            setState(varState);
            log.debug(_name + " state set to " + varState);
        } else if (e.getPropertyName().equals("Value")) {
            // update value of Variable
            log.debug("update value of Variable");

            int[] intVals = new int[cvCount];

            for (int i = 0; i < cvCount; i++) {
                intVals[i] = (cvList.get(i).thisCV.getValue() & maskVal(cvList.get(i).cvMask)) >>> offsetVal(cvList.get(i).cvMask);
//                log.debug("intVals[" + i + "]=" + intVals[i]);
            }

            updateVariableValue(intVals);

            // state change due to CV value change, so propagate that
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
                    if (log.isDebugEnabled()) {
                        log.debug("Variable=" + _name + "; focusGained");
                    }
                    enterField();
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (log.isDebugEnabled()) {
                        log.debug("Variable=" + _name + "; focusLost");
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
     * class to hold CV parameters for CVs used
     */
    class CvItem {

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
