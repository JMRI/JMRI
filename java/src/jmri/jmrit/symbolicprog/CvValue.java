// CvValue.java

package jmri.jmrit.symbolicprog;

import jmri.Programmer;
import jmri.ProgListener;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextField;


/**
 * Encapsulate a single CV value and provide programming access to the decoder.
 *<P>Since this is a single CV in a single decoder, the Programmer used to get
 * access is part of the state.  This allows us to specify a specific ops-mode
 * programmer aimed at a particular decoder.
 *<P>There are three relevant parameters:  Busy, Value, State.  Busy == true means
 * that a read or write operation is going on.  When it transitions to "false", the
 * operation is complete, and the Value and State are stable.  During a read
 * operation, Value changes before State, so you can assume that Value is stable
 * if notified of a State change.
 *
 * @author    Bob Jacobsen   Copyright (C) 2001, 2003, 2004
 * @author    Howard G. Penny   Copyright (C) 2005
 * @version   $Revision$
 */
public class CvValue extends AbstractValue implements ProgListener {

    public CvValue(int num, Programmer pProgrammer) {
        _num = num;
        mProgrammer = pProgrammer;
        _tableEntry = new JTextField("0", 3);
        _defaultColor = _tableEntry.getBackground();
        _tableEntry.setBackground(COLOR_UNKNOWN);
    }

    public CvValue(int num, String cvName, int piCv, int piVal, int siCv, int siVal, int iCv, Programmer pProgrammer) {
        _num   = num;
        _cvName  = cvName;
        if (cvName == null) log.error("cvName == null in ctor num: "+num);
        _piCv  = piCv;
        _piVal = piVal;
        _siCv  = siCv;
        _siVal = siVal;
        _iCv    = iCv;
        mProgrammer = pProgrammer;
        _tableEntry = new JTextField("0", 3);
        _defaultColor = _tableEntry.getBackground();
        _tableEntry.setBackground(COLOR_UNKNOWN);
    }

    public String toString() {
        return "CvValue _num="+_num+" _cvName="+_cvName+" _piCv="+_piCv+" _siCv="+_siCv
                +" _iCv="+_iCv;
    }
    
    public int number() { return _num; }
    private int _num;

    public String cvName() { return _cvName; }
    private String _cvName = "";

    public int piCv() { return _piCv; }
    private int _piCv;

    public int piVal() { return _piVal; }
    private int _piVal;

    public int siCv() { return _siCv; }
    private int _siCv;

    public int siVal() { return _siVal; }
    private int _siVal;

    public int iCv() { return _iCv; }
    private int _iCv;

    private JLabel _status = null;

    private Programmer mProgrammer;

    public int getValue()  { return _value; }

    Color getColor() { return _tableEntry.getBackground(); }

    protected void notifyValueChange(int value) {
        prop.firePropertyChange("Value", null, Integer.valueOf(value));
    }
    /**
     * Edit a new value into the CV. Only use this for external edits, e.g. set form a GUI,
     * not for internal uses, as it sets the state to EDITED
     */
    public void setValue(int value) {
        if (log.isDebugEnabled()) log.debug("CV "+number()+" value changed from "+_value+" to "+value);
        setState(EDITED);
        if (_value != value) {
            _value = value;
            _tableEntry.setText(""+value);
            notifyValueChange(value);
        }
    }
    private int _value = 0;
    
    /**
     * Get the decoder value read during compare
     * @return _decoderValue
     */
    public int getDecoderValue()  { return _decoderValue; }
    
    private int _decoderValue = 0;

    public int getState()  { return _state; }
    
    /**
     * Set state value and send notification.  Also sets GUI color as needed.
     */
    public void setState(int state) {
        if (log.isDebugEnabled()) log.debug("cv "+number()+" set state from "+_state+" to "+state);
        int oldstate = _state;
        _state = state;
        switch (state) {
        case UNKNOWN : setColor(COLOR_UNKNOWN ); break;
        case EDITED  : setColor(COLOR_EDITED ); break;
        case READ    : setColor(COLOR_READ    ); break;
        case STORED  : setColor(COLOR_STORED  ); break;
        case FROMFILE: setColor(COLOR_FROMFILE); break;
        case SAME: setColor(COLOR_SAME); break;
        case DIFF: setColor(COLOR_DIFF); break;
        default:      log.error("Inconsistent state: "+_state);
        }
        if (oldstate != state) prop.firePropertyChange("State", Integer.valueOf(oldstate), Integer.valueOf(state));
    }

    private int _state = 0;

    // read, write operations
    public boolean isBusy() { return _busy; }

    /**
     * set the busy state and send notification. Should be used _only_ if
     * this is the only thing changing
     */
    private void setBusy(boolean busy) {
        if (log.isDebugEnabled()) log.debug("setBusy from "+_busy+" to "+busy+" state "+_state);
        boolean oldBusy = _busy;
        _busy = busy;
        notifyBusyChange(oldBusy, busy);
    }
    /**
     * Notify of changes to the busy state
     */
    private void notifyBusyChange(boolean oldBusy, boolean newBusy) {
        if (log.isDebugEnabled()) log.debug("notifyBusy from "+oldBusy+" to "+newBusy+" current state "+_state);
        if (oldBusy != newBusy) prop.firePropertyChange("Busy",
                                                        oldBusy ? Boolean.TRUE : Boolean.FALSE,
                                                        newBusy ? Boolean.TRUE : Boolean.FALSE);
    }
    private boolean _busy = false;

    // color management
    Color _defaultColor;
    void setColor(Color c) {
        if (c != null) _tableEntry.setBackground(c);
        else _tableEntry.setBackground(_defaultColor);
        //prop.firePropertyChange("Value", null, null);
    }

    // object for Table entry
    JTextField _tableEntry = null;
    JTextField getTableEntry() {
        return _tableEntry;
    }

    /**
     * Set bean keeping track of whether this CV is intended to be
     * read-only.  Does not otherwise affect behaviour!
     * Default is "false".
     */    public void setReadOnly(boolean is) {
        _readOnly = is;
    }

    private boolean _readOnly = false;
    /**
     * Retrieve bean keeping track of whether this CV is intended to be
     * read-only.  Does not otherwise affect behaviour!
     * Default is "false".
     */
    public boolean getReadOnly() {
        return _readOnly;
    }

    /**
     * Set bean keeping track of whether this CV is intended to
     * be used as info-only.  Does not otherwise affect behaviour!
     * Default is "false".
     */
    public void setInfoOnly(boolean is) {
        _infoOnly = is;
    }

    private boolean _infoOnly = false;
    /**
     * Retrieve bean keeping track of whether this CV is intended to
     * be used as info-only.  Does not otherwise affect behaviour!
     * Default is "false".
     */
    public boolean getInfoOnly() {
        return _infoOnly;
    }

    /**
     * Set bean keeping track of whether this CV is intended to
     * be used as write-only.  Does not otherwise affect behaviour!
     * Default is "false".
     */
    public void setWriteOnly(boolean is) {
        _writeOnly = is;
    }

    private boolean _writeOnly = false;
    /**
     * Retrieve bean keeping track of whether this CV is intended to
     * be used as write-only.  Does not otherwise affect behaviour!
     * Default is "false".
     */
    public boolean getWriteOnly() {
        return _writeOnly;
    }

    public void setToRead(boolean state) {
        if (getInfoOnly() || getWriteOnly()) state = false;
        _toRead = state;
    }
    public boolean isToRead() { return _toRead; }
    private boolean _toRead = false;

    public void setToWrite(boolean state) {
        if (getInfoOnly() || getReadOnly()) state = false;
        _toWrite = state;
    }
    public boolean isToWrite() { return _toWrite; }
    private boolean _toWrite = false;

    // read, write support
    private boolean _reading = false;
    private boolean _confirm = false;

    public void read(JLabel status) {
        if (log.isDebugEnabled()) log.debug("read call with Cv number "+_num);
        setToRead(false);
        // get a programmer reference and write
        _status = status;

        if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateReadingCV"),
                                    new Object[]{""+_num}));

        if (mProgrammer != null) {
            setBusy(true);
            _reading = true;
            _confirm = false;
            try {
                mProgrammer.readCV(_num, this);
            } catch (Exception e) {
                if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateExceptionDuringRead"),
                                    new Object[]{e.toString()}));

                log.warn("Exception during CV read: "+e);
                setBusy(false);
            }
        } else {
            if (status != null) status.setText(SymbolicProgBundle.getMessage("StateNoProgrammer"));
            log.error("No programmer available!");
        }
    }

    public void readIcV(JLabel status) {
        setToRead(false);
        // get a programmer reference and write an indexed CV
        _status = status;

        if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateReadingIndexedCV"),
                                    new Object[]{""+_iCv, ""+_piVal+(_siVal>=0?"."+_siVal:"")}));

        if (mProgrammer != null) {
            setBusy(true);
            _reading = true;
            _confirm = false;
            try {
                setState(UNKNOWN);
                mProgrammer.readCV(_iCv, this);
            }
            catch (Exception e) {
                setState(UNKNOWN);
                if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateExceptionDuringIndexedRead"),
                                    new Object[]{e.toString()}));
                log.warn("Exception during IndexedCV read: " + e);
                setBusy(false);
            }
        } else {
            if (status != null) status.setText(SymbolicProgBundle.getMessage("StateNoProgrammer"));
            log.error("No programmer available!");
        }
    }
    
    public void confirmIcV(JLabel status) {
        setToRead(false);
        // get a programmer reference and write an indexed CV
        _status = status;

        if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateConfirmIndexedCV"),
                                    new Object[]{""+_iCv, ""+_piVal+(_siVal>=0?"."+_siVal:"")}));

        if (mProgrammer != null) {
            setBusy(true);
            _reading = false;
            _confirm = true;
            try {
                setState(UNKNOWN);
                mProgrammer.readCV(_iCv, this);
            }
            catch (Exception e) {
                setState(UNKNOWN);
                if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateExceptionDuringIndexedRead"),
                                    new Object[]{e.toString()}));
                log.warn("Exception during IndexedCV read: " + e);
                setBusy(false);
            }
        } else {
            if (status != null) status.setText(SymbolicProgBundle.getMessage("StateNoProgrammer"));
            log.error("No programmer available!");
        }
    }


    public void confirm(JLabel status) {
        if (log.isDebugEnabled()) log.debug("confirm call with Cv number "+_num);
        // get a programmer reference and write
        _status = status;

        if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateConfirmingCV"),
                                    new Object[]{""+_num}));

        if (mProgrammer != null) {
            setBusy(true);
            _reading = false;
            _confirm = true;
            try {
                mProgrammer.confirmCV(_num, _value, this);
            } catch (Exception e) {
                if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateExceptionDuringConfirm"),
                                    new Object[]{e.toString()}));
                log.warn("Exception during CV read: "+e);
                setBusy(false);
            }
        } else {
            if (status != null) status.setText(SymbolicProgBundle.getMessage("StateNoProgrammer"));
            log.error("No programmer available!");
        }
    }

    public void write(JLabel status) {
        if (log.isDebugEnabled()) log.debug("write call with Cv number "+_num);
        setToWrite(false);
        // get a programmer reference and write
        _status = status;

        if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateWritingCV"),
                                    new Object[]{""+_num}));
        if (mProgrammer != null) {
            setBusy(true);
            _reading = false;
            _confirm = false;
            try {
                setState(UNKNOWN);
                mProgrammer.writeCV(_num, _value, this);
            } catch (Exception e) {
                setState(UNKNOWN);
                if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateExceptionDuringWrite"),
                                    new Object[]{e.toString()}));
                log.warn("Exception during CV write: "+e);
                setBusy(false);
            }
        } else {
            if (status != null) status.setText(SymbolicProgBundle.getMessage("StateNoProgrammer"));
            log.error("No programmer available!");
        }
    }

    public void writePI(JLabel status) {
        if (log.isDebugEnabled()) log.debug("write call with PI number "+_piVal);
        // get a programmer reference and write to the primary index
        _status = status;
        if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateWritingPICV"),
                                    new Object[]{""+_num}));
        if (mProgrammer != null) {
            setBusy(true);
            _reading = false;
            _confirm = false;
            try {
                setState(UNKNOWN);
                mProgrammer.writeCV(_piCv, _piVal, this);
            } catch (Exception e) {
                setState(UNKNOWN);
                if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateExceptionDuringWrite"),
                                    new Object[]{e.toString()}));
                log.warn("Exception during CV write: "+e);
                setBusy(false);
            }
        } else {
            if (status != null) status.setText(SymbolicProgBundle.getMessage("StateNoProgrammer"));
            log.error("No programmer available!");
        }
    }

    public void writeSI(JLabel status) {
        if (log.isDebugEnabled()) log.debug("write call with SI number " +_siVal);
        // get a programmer reference and write to the secondary index
        _status = status;
        if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateWritingSICV"),
                                    new Object[]{""+_num}));
        if (mProgrammer != null) {
            setBusy(true);
            _reading = false;
            _confirm = false;
                try {
                    setState(UNKNOWN);
                    if ( _siVal >= 0 ) {
                        mProgrammer.writeCV(_siCv, _siVal, this);
                    } else { // just in case we get called without a real SI value
                        mProgrammer.writeCV(_siCv, 0, this);
                    }
                }
                catch (Exception e) {
                    setState(UNKNOWN);
                    if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateExceptionDuringWrite"),
                                    new Object[]{e.toString()}));
                    log.warn("Exception during CV write: " + e);
                    setBusy(false);
                }
        }
        else {
            if (status != null) status.setText(SymbolicProgBundle.getMessage("StateNoProgrammer"));
            log.error("No programmer available!");
        }
    }

    public void writeIcV(JLabel status) {
        if (log.isDebugEnabled()) log.debug("write call with IndexedCv number "+_iCv);
        setToWrite(false);
        // get a programmer reference and write the indexed CV
        _status = status;

        if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateWritingIndexedCV"),
                                    new Object[]{""+_iCv, ""+_piVal+(_siVal>=0?"."+_siVal:"")}));

        if (mProgrammer != null) {
            setBusy(true);
            _reading = false;
            _confirm = false;
            try {
                setState(UNKNOWN);
                mProgrammer.writeCV(_iCv, _value, this);
            } catch (Exception e) {
                setState(UNKNOWN);
                if (status != null) status.setText(
                                java.text.MessageFormat.format(
                                    SymbolicProgBundle.getMessage("StateExceptionDuringIndexedWrite"),
                                    new Object[]{e.toString()}));
                log.warn("Exception during CV write: "+e);
                setBusy(false);
            }
        } else {
            if (status != null) status.setText(SymbolicProgBundle.getMessage("StateNoProgrammer"));
            log.error("No programmer available!");
        }
    }

    public void programmingOpReply(int value, int retval) {
        if (log.isDebugEnabled()) log.debug("CV progOpReply for CV "+_num+" with retval "+retval
                                            +" during "
                                            +(_reading?"read sequence":
                                              (_confirm?"confirm sequence":"write sequence")));
        if (!_busy) log.error("opReply when not busy!");
        boolean oldBusy = _busy;
        if (retval == OK) {
            if (_status != null) _status.setText(SymbolicProgBundle.getMessage("StateOK"));
            if (_reading) {
                // set & notify value directly to avoid state going to EDITED
                _value = value;
                _tableEntry.setText(Integer.toString(value));
                notifyValueChange(value);
                setState(READ);
                if (log.isDebugEnabled()) log.debug("CV setting not busy on end read");
                _busy = false;
                notifyBusyChange(oldBusy, _busy);
            }
            else if (_confirm) {
                // _value doesn't change, just the state, and save the value read 
            	_decoderValue = value;
            	// does the decoder value match the file value
            	if (value == _value)
            		setState(SAME);
            	else
            		setState(DIFF);
                _busy = false;
                notifyBusyChange(oldBusy, _busy);
            } else {  // writing
                setState(STORED);
                _busy = false;
                notifyBusyChange(oldBusy, _busy);
            }
        } else {
            if (_status != null) _status.setText(
                        java.text.MessageFormat.format(
                            SymbolicProgBundle.getMessage("StateProgrammerError"),
                            new Object[]{mProgrammer.decodeErrorCode(retval)}));

            // delay to ensure that the message appears!
            javax.swing.Timer timer = new javax.swing.Timer(1000, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        errorTimeout();
                    }
                });
            timer.setInitialDelay(1000);
            timer.setRepeats(false);
            timer.start();
        }
        if (log.isDebugEnabled()) log.debug("CV progOpReply end of handling CV "+_num);
    }

    void errorTimeout() {
        setState(UNKNOWN);
        if (log.isDebugEnabled()) log.debug("CV setting not busy on error reply");
        _busy = false;
        notifyBusyChange(true, _busy);
    }

    // clean up connections when done
    public void dispose() {
        if (log.isDebugEnabled()) log.debug("dispose");
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(CvValue.class.getName());

}
