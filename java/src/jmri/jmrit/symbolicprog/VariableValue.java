package jmri.jmrit.symbolicprog;

import java.awt.Component;
import java.util.HashMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single Variable value; abstract base class.
 * <p>
 * The "changed" parameter (non-bound, accessed via isChanged) indicates whether
 * a "write changes" or "read changes" operation should handle this object.
 * <br><br>
 * The mask shown below comes in two forms:
 * <ul>
 * <li> A character-by-character bit mask of 8 or 16 binary digits, e.g.
 * "XXVVVVXXX"
 * <p>
 * In this case, the "V" bits denote a continuous bit field that contains the
 * datum
 * <li>A small decimal value, i.e. "9"
 * <p>
 * In this case, the mask forms the multiplier (N) which combines with the
 * maximum value (maxVal, defined in a subclass) to break the CV into three
 * parts:
 * <ul>
 * <li>lowest part, stored as 1 times a value 0-(N-1)
 * <li> datum stored as datum*N (datum is limited to maxVal)
 * <li> highest part, which stored as N*(maxVal+1) times the value
 * </ul>
 * As an example, consider storing two decimal digits as a decimal value. You
 * can't use a bit mask changing the 2nd digit from 1 to 7, for example with a
 * total value of 14 to 74, changes bits that are also used by the first digit.
 * Instead, code this as
 * <ul>
 * <li> mask="1" maxVal="9"
 * <li> mask="10" maxVal="9"
 * </ul>
 * and you'll get the desired effect. (This requires Schema
 * <a href="http://jmri.org/xml/schema/decoder-4-15-2.xsd">xml/schema/decoder-4-15-2.xsd</a>
 * for validation)
 * </ul>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2003, 2004, 2005, 2013
 * @author Howard G. Penny Copyright (C) 2005
 */
public abstract class VariableValue extends AbstractValue implements java.beans.PropertyChangeListener {

    private String _label;
    private String _item;
    private String _cvName;

    /**
     * A vector of CV objects used to look up CVs.
     */
    protected HashMap<String, CvValue> _cvMap;

    /**
     * Field holds the current status.
     */
    protected JLabel _status = null;

    /**
     * Field holds the current tool tip text.
     */
    protected String _tooltipText = null;
    // and thus can be called without limit
    // and thus should be called a limited number of times

    /**
     * Get a display representation {@code Object} of this variable.
     * <br><br>
     * The actual stored value of a variable is not the most interesting thing.
     * Instead, you usually get an {@code Object} representation for display in
     * a table, etc. Modification of the state of that object then gets
     * reflected back, causing the underlying CV objects to change.
     *
     * @return the {@code Object} representation for display purposes
     */
    public abstract Component getCommonRep(); // and thus should be called a limited number of times

    /**
     * Creates a new {@code Object} representation for display purposes, using
     * the specified format.
     *
     * @param format a name representing
     * @return an {@code Object} representation for display purposes
     */
    public abstract Component getNewRep(String format); // this one is returning a new object

    /**
     * @return String that can (usually) be interpreted as an integer
     */
    public abstract String getValueString();

    /**
     * @return Value as a native-form Object
     */
    public abstract Object getValueObject();

    /**
     * @return User-desired value, which may or may not be an integer
     */
    public String getTextValue() {
        return getValueString();
    }

    /**
     * Provide a user-readable description of the CVs accessed by this variable.
     * <p>
     * Default is a single CV number
     *
     * @return a user-readable description
     */
    public String getCvDescription() {
        return "CV" + _cvNum;
    }

    /**
     * Set the value from a single number.
     * <p>
     * In some cases, e.g. speed tables, this will result in complex behavior,
     * where setIntValue(getIntValue()) results in something unexpected.
     *
     * @param i the integer value to set
     */
    public abstract void setIntValue(int i);

    /**
     * Set value from a String value.
     * <p>
     * The current implementation is a stand-in. Note that e.g. Speed Tables
     * don't use a single Int, so will just be skipped. The solution to that is
     * to overload this in complicated variable types.
     *
     * @param value the String value to set
     */
    public void setValue(String value) {
        try {
            int val = Integer.parseInt(value);
            setIntValue(val);
        } catch (NumberFormatException e) {
            log.debug("skipping set of non-integer value \"{}\"", value);
        }
    }

    /**
     * Get the value as a single integer.
     * <p>
     * In some cases, e.g. speed tables, this will result in complex behavior,
     * where setIntValue(getIntValue()) results in something unexpected.
     *
     * @return the value as an integer
     */
    public abstract int getIntValue();

    /**
     * Get the value as an Unsigned Long.
     * <p>
     * Some subclasses (e.g. {@link SplitVariableValue}) store the value as a
     * {@code long} rather than an {@code integer}. This method should be used
     * in cases where such a class may be queried (e.g. by
     * {@link ArithmeticQualifier}).
     * <p>
     * If not overridden by a subclass, it will return an
     * {@link Integer#toUnsignedLong UnsignedLong} conversion of the value
     * returned by {@link #getIntValue getIntValue()}.
     *
     * @return the value as a long
     */
    public long getLongValue() {
        return Integer.toUnsignedLong(getIntValue());
    }

    /**
     * This should be overridden by any implementation.
     */
    void updatedTextField() {
        log.error("unexpected use of updatedTextField()", new Exception("traceback"));
    }

    /**
     * Always read the contents of this Variable.
     */
    public abstract void readAll();

    /**
     * Always write the contents of this Variable.
     */
    public abstract void writeAll();

    /**
     * Confirm the contents of this Variable.
     */
    public void confirmAll() {
        log.error("should never execute this");
    }

    /**
     * Read the contents of this Variable if it's in a state that indicates it
     * was "changed".
     *
     * @see #isChanged
     */
    public abstract void readChanges();

    /**
     * Write the contents of this Variable if it's in a state that indicates it
     * was "changed".
     *
     * @see #isChanged
     */
    public abstract void writeChanges();

    /**
     * Determine whether this Variable is "changed", so that "read changes" and
     * "write changes" will act on it.
     *
     * @see #considerChanged
     * @return true if Variable is "changed"
     */
    public abstract boolean isChanged();

    /**
     * Default implementation for subclasses to tell if a CV meets a common
     * definition of "changed". This implementation will only consider a
     * variable to be changed if the underlying CV(s) state is EDITTED, e.g. if
     * the CV(s) has been manually editted.
     *
     * @param c CV to be examined
     * @return true if to be considered changed
     */
    public static boolean considerChanged(CvValue c) {
        int state = c.getState();
        return state == CvValue.EDITED || state == CvValue.UNKNOWN;
    }

    // handle incoming parameter notification
    @Override
    public abstract void propertyChange(java.beans.PropertyChangeEvent e);

    /**
     * Dispose of the object.
     */
    public abstract void dispose();

    /**
     * Gets a (usually text) description of the variable type and range.
     *
     * @return description of the variable type and range
     */
    public abstract Object rangeVal();

    // methods implemented here:
    /**
     *
     * @param label     the displayed label for the Variable
     * @param comment   for information only, generally not displayed
     * @param cvName    the name for the CV. Seems to be generally ignored and
     *                  set to "".
     * @param readOnly  true if the variable is to be readable-only
     * @param infoOnly  true if the variable is to be for information only (a
     *                  fixed value that is neither readable or writable)
     * @param writeOnly true if the variable is to be writable-only
     * @param opsOnly   true if the variable is to be programmable in ops mode
     *                  only
     * @param cvNum     the CV number
     * @param mask      a bit mask like XXXVVVXX (converts to a value like
     *                  0b00011100)
     * @param v         a vector of CV objects used to look up CVs
     * @param status    a field that holds the current status
     * @param item      the unique name for this Variable
     * @see VariableValue
     */
    public VariableValue(String label, String comment, String cvName,
            boolean readOnly, boolean infoOnly, boolean writeOnly, boolean opsOnly,
            String cvNum, String mask, HashMap<String, CvValue> v, JLabel status, String item) {
        _label = label;
        _comment = comment;
        _cvName = cvName;
        _readOnly = readOnly;
        _infoOnly = infoOnly;
        _writeOnly = writeOnly;
        _opsOnly = opsOnly;
        _cvNum = cvNum;
        _mask = mask;
        _cvMap = v;
        _status = status;
        _item = item;
    }

    /**
     * Create a null object. Normally only used for tests and to pre-load
     * classes.
     */
    protected VariableValue() {
    }

    // common information - none of these are bound
    /**
     * Gets the displayed label for the Variable.
     *
     * @return the displayed label for the Variable
     */
    public String label() {
        return _label;
    }

    /**
     * Gets the unique name for this Variable.
     *
     * @return the unique name for this Variable
     */
    public String item() {
        return _item;
    }

    /**
     * Get the CV name.
     *
     * @return the name for the CV
     */
    public String cvName() {
        return _cvName;
    }

    /**
     * Set tooltip text to be used by both the "value" and representations of
     * this Variable.
     * <p>
     * This is expected to be overridden in subclasses to change their internal
     * info.
     *
     * @param t the tooltip text to be used
     * @see #updateRepresentation
     */
    public void setToolTipText(String t) {
        _tooltipText = t;
    }

    /**
     * Add the proper tooltip text to a graphical rep before returning it, sets
     * the visibility.
     *
     * @param c the current graphical representation
     * @return the updated graphical representation
     */
    protected JComponent updateRepresentation(JComponent c) {
        c.setToolTipText(_tooltipText);
        c.setVisible(getAvailable());
        return c;
    }

    /**
     *
     * @return the comment
     */
    public String getComment() {
        return _comment;
    }
    private String _comment;

    /**
     *
     * @return the value of the readOnly attribute
     */
    public boolean getReadOnly() {
        return _readOnly;
    }
    private boolean _readOnly;

    /**
     *
     * @return the value of the infoOnly attribute
     */
    public boolean getInfoOnly() {
        return _infoOnly;
    }
    private boolean _infoOnly;

    /**
     *
     * @return the value of the writeOnly attribute
     */
    public boolean getWriteOnly() {
        return _writeOnly;
    }
    private boolean _writeOnly;

    /**
     *
     * @return the value of the opsOnly attribute
     */
    public boolean getOpsOnly() {
        return _opsOnly;
    }
    private boolean _opsOnly;

    /**
     *
     * @return the CV number
     */
    public String getCvNum() {
        return _cvNum;
    }
    private String _cvNum;

    /**
     *
     * @return the CV name
     */
    public String getCvName() {
        return _cvName;
    }

    /**
     *
     * @return the CV bitmask in the form XXXVVVXX
     */
    public String getMask() {
        return _mask;
    }
    private String _mask;

    /**
     *
     * @return the current state of the Variable
     */
    public int getState() {
        return _state;
    }

    /**
     * Sets the current state of the variable.
     *
     * @param state the desired state as per definitions in AbstractValue
     * @see AbstractValue
     */
    public void setState(int state) {
        switch (state) {
            case UNKNOWN:
                setColor(COLOR_UNKNOWN);
                break;
            case EDITED:
                setColor(COLOR_EDITED);
                break;
            case READ:
                setColor(COLOR_READ);
                break;
            case STORED:
                setColor(COLOR_STORED);
                break;
            case FROMFILE:
                setColor(COLOR_FROMFILE);
                break;
            case SAME:
                setColor(COLOR_SAME);
                break;
            case DIFF:
                setColor(COLOR_DIFF);
                break;
            default:
                log.error("Inconsistent state: " + _state);
        }
        if (_state != state || _state == UNKNOWN) {
            prop.firePropertyChange("State", Integer.valueOf(_state), Integer.valueOf(state));
        }
        _state = state;
    }
    private int _state = UNKNOWN;

    /**
     * {@inheritDoc}
     * <p>
     * Simple implementation for the case of a single CV. Intended to be
     * sufficient for many subclasses.
     */
    @Override
    public void setToRead(boolean state) {
        boolean newState = state;

        // if this variable is disabled, then don't read, unless
        // some other variable has already set that
        if (!getAvailable() && !state) { // do want to set when state is true
            log.debug("Variable not available, skipping setToRead(false) to leave as is");
            return;
        }

        // if read not available, don't force read
        if (getInfoOnly() || getWriteOnly()) {
            newState = false;
        }

        if (log.isDebugEnabled()) {
            log.debug("setToRead(" + state + ") with overrides " + getInfoOnly() + "," + getWriteOnly() + "," + !getAvailable() + " sets " + newState);
        }
        _cvMap.get(getCvNum()).setToRead(newState);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Simple implementation for the case of a single CV. Intended to be
     * sufficient for many subclasses.
     */
    @Override
    public boolean isToRead() {
        return _cvMap.get(getCvNum()).isToRead();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Simple implementation for the case of a single CV. Intended to be
     * sufficient for many subclasses.
     */
    @Override
    public void setToWrite(boolean state) {
        boolean newState = state;

        // if this variable is disabled, then don't write, unless
        // some other variable has already set that
        if (!getAvailable() && !state) { // do want to set when state is true
            log.debug("Variable not available, skipping setToRead(false) to leave as is");
            return;
        }

        // if read not available, don't force read
        if (getInfoOnly() || getReadOnly()) {
            newState = false;
        }

        if (log.isDebugEnabled()) {
            log.debug("setToRead(" + state + ") with overrides " + getInfoOnly() + "," + getReadOnly() + "," + !getAvailable() + " sets " + newState);
        }
        _cvMap.get(getCvNum()).setToWrite(newState);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Simple implementation for the case of a single CV. Intended to be
     * sufficient for many subclasses.
     */
    @Override
    public boolean isToWrite() {
        return _cvMap.get(getCvNum()).isToWrite();
    }

    /**
     * Propagate a state change here to the CVs that are related, which will in
     * turn propagate back to here.
     *
     * @param state the new state to set
     */
    public abstract void setCvState(int state);

    /**
     * Check if a variable is busy (during read, write operations).
     *
     * @return {@code true} if busy
     */
    public boolean isBusy() {
        return _busy;
    }

    /**
     *
     * @param newBusy the desired state
     */
    protected void setBusy(boolean newBusy) {
        boolean oldBusy = _busy;
        _busy = newBusy;
        if (newBusy != oldBusy) {
            prop.firePropertyChange("Busy", Boolean.valueOf(oldBusy), Boolean.valueOf(newBusy));
        }
    }
    private boolean _busy = false;

    /**
     * Convert a String bit mask like XXXVVVXX to an int like 0b00011100.
     *
     * @param maskString the textual (XXXVVVXX style) mask
     * @return the binary integer (0b00011100 style) mask
     */
    protected int maskValAsInt(String maskString) {
        // convert String mask to int
        int mask = 0;
        for (int i = 0; i < maskString.length(); i++) {
            mask = mask << 1;
            try {
                if (maskString.charAt(i) == 'V') {
                    mask = mask | 1;
                }
            } catch (StringIndexOutOfBoundsException e) {
                log.error("mask \"{}\" could not be handled for variable {}", maskString, label());
            }
        }
        return mask;
    }

    /**
     * Is this a bit mask (such as XVVVXXXX form) vice radix mask (small
     * integer)?
     *
     * @param mask the bit mask to check
     * @return {@code true} if XVVVXXXX form
     */
    protected boolean isBitMask(String mask) {
        return mask.isEmpty() || mask.startsWith("X") || mask.startsWith("V");
    }

    /**
     * Find number of places to shift a value left to align it with a mask.
     * <p>
     * For example, a mask of "XXVVVXXX" means that the value 5 needs to be
     * shifted left 3 places before being masked and stored as XX101XXX
     *
     * @param maskString the (XXXVVVXX style) mask
     * @return the number of places to shift left before masking
     */
    protected int offsetVal(String maskString) {
        // convert String mask to int
        int offset = 0;
        for (int i = 0; i < maskString.length(); i++) {
            if (maskString.charAt(i) == 'V') {
                offset = maskString.length() - 1 - i;  // number of places to shift left
            }
        }
        return offset;
    }

    /**
     * Get the current value from the CV, using the mask as needed.
     *
     * @param Cv         the CV of interest
     * @param maskString the (XXXVVVXX style) mask for extracting the Variable
     *                   value from this CV
     * @param maxVal     the maximum possible value for this Variable
     * @return the current value of the Variable
     */
    protected int getValueInCV(int Cv, String maskString, int maxVal) {
        if (isBitMask(maskString)) {
            return (Cv & maskValAsInt(maskString)) >>> offsetVal(maskString);
        } else {
            int radix = Integer.parseInt(maskString);
            log.trace("get value {} radix {} returns {}", Cv, radix, Cv / radix);
            return (Cv / radix) % (maxVal + 1);
        }
    }

    /**
     * Set a value into a CV, using the mask as needed.
     *
     * @param oldCv      Value of the CV before this update is applied
     * @param newVal     Value for this variable (e.g. not the CV value)
     * @param maskString The bit mask for this variable in character form
     * @param maxVal     the maximum possible value for this Variable
     * @return int new value for the CV
     */
    protected int setValueInCV(int oldCv, int newVal, String maskString, int maxVal) {
        if (isBitMask(maskString)) {
            int mask = maskValAsInt(maskString);
            int offset = offsetVal(maskString);
            return (oldCv & ~mask) + ((newVal << offset) & mask);
        } else {
            int radix = Integer.parseInt(maskString);
            int lowPart = oldCv % radix;
            int newPart = newVal % (maxVal + 1) * radix;
            int highPart = (oldCv / (radix * (maxVal + 1))) * (radix * (maxVal + 1));
            int retval = highPart + newPart + lowPart;
            log.trace("Set sees oldCv {} radix {}, lowPart {}, newVal {}, highPart {}, does {}", oldCv, radix, lowPart, newVal, highPart, retval);
            return retval;
        }
    }

    /**
     * Provide access to CVs used by this Variable.
     *
     * @return an array of CVs used by this Variable
     */
    public abstract CvValue[] usesCVs();

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(VariableValue.class);

}
