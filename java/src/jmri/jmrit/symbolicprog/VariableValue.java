// VariableValue.java

package jmri.jmrit.symbolicprog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Vector;
import java.awt.Component;
import javax.swing.*;
import java.util.HashMap;
/**
 * Represents a single Variable value; abstract base class.
 *
 * <p>The "changed" parameter (non-bound, accessed via isChanged)
 * indicates whether a "write changes" or "read changes" operation
 * should handle this object.
 *
 * @author   Bob Jacobsen   Copyright (C) 2001, 2002, 2003, 2004, 2005, 2013
 * @author   Howard G. Penny Copyright (C) 2005
 * @version  $Revision$
 */
public abstract class VariableValue extends AbstractValue implements java.beans.PropertyChangeListener {

    private String _label;
    private String _item;
    private String _cvName;

    protected HashMap<String, CvValue> _cvMap;   // Vector of CV objects used to look up CVs
    protected JLabel _status = null;

    protected String _tooltipText = null;
                                                // and thus can be called without limit
    // and thus should be called a limited number of times

    // The actual stored value is not the most interesting thing
    // Instead, you usually get a (Object) representation for display in
    // a table, etc. Modification of the state of that object then
    // gets reflected back, causing the underlying CV objects to change.
    abstract public Component getCommonRep();	// and thus should be called a limited number of times
    abstract public Component getNewRep(String format); // this one is returning a new object
    
    /**
     * @return String that can (usually) be interpreted as an integer
     */
    abstract public String getValueString();
    /**
     * @return Value as a native-form Object
     */
    abstract public Object getValueObject();
    /**
     * @return User-desired value, which may or may not be an integer
     */
    public String getTextValue() { return getValueString(); }

    /** 
     * Provide a user-readable description of
     * the CVs accessed by this variable.
     * <p>
     * Default is a single CV number
     */
     
     public String getCvDescription() {
        return "CV"+_cvNum;
     }
     
    /**
     * Set the value from a single number.
     *
     * In some cases, e.g. speed tables, this will result in 
     * complex behavior, where setIntValue(getIntValue()) results 
     * in something unexpected.
     */
    abstract public void setIntValue(int i);

    /**
     * Get the value as a single number.
     *
     * In some cases, e.g. speed tables, this will result in 
     * complex behavior, where setIntValue(getIntValue()) results 
     * in something unexpected.
     */
    abstract public int getIntValue();

    void updatedTextField() {
        log.error("unexpected use of updatedTextField()", new Exception("traceback"));
    }
    
    /**
     * Always read the contents of this Variable
     */
    abstract public void readAll();
    /**
     * Always write the contents of this Variable
     */
    abstract public void writeAll();
    /**
     * Confirm the contents of this Variable
     */
    public void confirmAll(){
    	log.error("should never execute this");
    }
    

    /**
     * Read the contents of this Variable if it's in a state
     * that indicates it was "changed"
     * @see #isChanged
     */
    abstract public void readChanges();

    /**
     * Write the contents of this Variable if it's in a state
     * that indicates it was "changed"
     * @see #isChanged
     */
    abstract public void writeChanges();

    /**
     * Determine whether this Variable is "changed", so that
     * "read changes" and "write changes" will act on it.
     * @see #considerChanged
     */
    abstract public boolean isChanged();

    /**
     * Default implementation for subclasses to tell if a CV meets a common definition
     * of "changed".  This implementation will only
     * consider a variable to be changed if the underlying CV(s) state is
     * EDITTED, e.g. if the CV(s) has been manually editted.
     * @param c CV to be examined
     * @return true if to be considered changed
     */
    static public boolean considerChanged(CvValue c) {
        int state = c.getState();
        if (state == CvValue.EDITED || state == CvValue.UNKNOWN) {
            return true;
        } else {
            return false;
        }
    }

    // handle incoming parameter notification
    abstract public void propertyChange(java.beans.PropertyChangeEvent e);
    abstract public void dispose();

    abstract public Object rangeVal();

    // methods implemented here:
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
     * Create a null object.  Normally only used for tests and to pre-load classes.
     */
    protected VariableValue() {}

    // common information - none of these are bound
    public String label() { return _label; }
    public String item() { return _item; }
    public String cvName() { return _cvName; }

    /**
     * Set tooltip text to be used by both the "value" and
     * representations of this Variable.
     * <P>This is expected to be overridden in subclasses to
     * change their internal info.
     * @see #updateRepresentation
     * @param t
     */
    public void setToolTipText(String t) {
        _tooltipText = t;
    }
    /**
     * Add the proper tooltip text to a graphical rep
     * before returning it, sets the visibility
     * @param c
     */
    protected JComponent updateRepresentation(JComponent c) {
        c.setToolTipText(_tooltipText);
        c.setVisible(getAvailable());
        return c;
    }

    public String getComment() { return _comment; }
    private String _comment;

    public boolean getReadOnly() { return _readOnly; }
    private boolean _readOnly;

    public boolean getInfoOnly() { return _infoOnly; }
    private boolean _infoOnly;

    public boolean getWriteOnly() { return _writeOnly; }
    private boolean _writeOnly;

    public boolean getOpsOnly() { return _opsOnly; }
    private boolean _opsOnly;

    public String getCvNum() { return _cvNum; }
    private String _cvNum;

    public String getCvName() { return _cvName; }

    public String getMask() { return _mask; }
    private String _mask;

    public int getState()  { return _state; }
    public void setState(int state) {
        switch (state) {
        case UNKNOWN : setColor(COLOR_UNKNOWN ); break;
        case EDITED  : setColor(COLOR_EDITED  ); break;
        case READ    : setColor(COLOR_READ    ); break;
        case STORED  : setColor(COLOR_STORED  ); break;
        case FROMFILE: setColor(COLOR_FROMFILE); break;
        case SAME: 		setColor(COLOR_SAME); break;
        case DIFF: 		setColor(COLOR_DIFF); break;
        default: log.error("Inconsistent state: "+_state);
        }
        if (_state != state || _state == UNKNOWN) prop.firePropertyChange("State", Integer.valueOf(_state), Integer.valueOf(state));
        _state = state;
    }
    private int _state = UNKNOWN;

    /**
     * Simple implementation for the case of a single CV. Intended
     * to be sufficient for many subclasses.
     */
    public void setToRead(boolean state) {
        boolean newState = state;
        
        // if this variable is disabled, then don't read, unless 
        // some other variable has already set that
        if (!getAvailable() && !state) { // do want to set when state is true
            log.debug("Variable not available, skipping setToRead(false) to leave as is");
            return;
        }

        // if read not available, don't force read
        if (getInfoOnly() || getWriteOnly()) newState = false;
        
        if (log.isDebugEnabled()) log.debug("setToRead("+state+") with overrides "+getInfoOnly()+","+getWriteOnly()+","+!getAvailable()+" sets "+newState);
        _cvMap.get(getCvNum()).setToRead(newState);
    }
    /**
     * Simple implementation for the case of a single CV. Intended
     * to be sufficient for many subclasses.
     */
    public boolean isToRead() { return _cvMap.get(getCvNum()).isToRead(); }

    /**
     * Simple implementation for the case of a single CV. Intended
     * to be sufficient for many subclasses.
     */
    public void setToWrite(boolean state) {
        boolean newState = state;
        
        // if this variable is disabled, then don't write, unless 
        // some other variable has already set that
        if (!getAvailable() && !state) { // do want to set when state is true
            log.debug("Variable not available, skipping setToRead(false) to leave as is");
            return;
        }

        // if read not available, don't force read
        if (getInfoOnly() || getReadOnly()) newState = false;
        
        if (log.isDebugEnabled()) log.debug("setToRead("+state+") with overrides "+getInfoOnly()+","+getReadOnly()+","+!getAvailable()+" sets "+newState);
        _cvMap.get(getCvNum()).setToWrite(newState);
    }

    /**
     * Simple implementation for the case of a single CV. Intended
     * to be sufficient for many subclasses.
     */
    public boolean isToWrite() { return _cvMap.get(getCvNum()).isToWrite(); }

    /**
     * Propogate a state change here to the CVs that are related, which will
     * in turn propagate back to here
     */
    abstract public void setCvState(int state);

    /**
     *  A variable is busy during read, write operations
     */
    public boolean isBusy() { return _busy; }
    protected void setBusy(boolean newBusy) {
        boolean oldBusy = _busy;
        _busy = newBusy;
        if (newBusy != oldBusy) prop.firePropertyChange("Busy", Boolean.valueOf(oldBusy), Boolean.valueOf(newBusy));
    }
    private boolean _busy = false;

    // tool to handle masking, updating
    protected int maskVal(String maskString) {
        // convert String mask to int
        int mask = 0;
        for (int i=0; i<8; i++) {
            mask = mask << 1;
            try {
                if (maskString.charAt(i) == 'V') {
                    mask = mask+1;
                }
            } catch (StringIndexOutOfBoundsException e) {
                log.error("mask /"+maskString+"/ could not be handled for variable "+label());
            }
        }
        return mask;
    }

    /**
     * Find number of places to shift a value left to align
     * if with a mask.  For example, as mask of "XXVVVXXX"
     * means that the value 5 needs to be shifted left 3 places
     * before being masked and stored as XX101XXX
     */
    protected int offsetVal(String maskString) {
        // convert String mask to int
        int offset = 0;
        for (int i=0; i<8; i++) {
            if (maskString.charAt(i) == 'V') {
                offset = 7-i;  // number of places to shift left
            }
        }
        return offset;
    }

    /**
     *
     * @param oldCv Value of the CV before this update is applied
     * @param newVal Value for this variable (e.g. not the CV value)
     * @param maskString The bit mask for this variable in character form
     * @return int new value for the CV
     */
    protected int newValue(int oldCv, int newVal, String maskString) {
        int mask = maskVal(maskString);
        int offset = offsetVal(maskString);
        return (oldCv & ~mask) + ((newVal << offset) & mask);
    }

    /**
     * Provide access to CVs referenced by this operation
     */
    abstract public CvValue[] usesCVs();

    // initialize logging
    static Logger log = LoggerFactory.getLogger(VariableValue.class.getName());

}
