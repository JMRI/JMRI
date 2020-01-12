package jmri.jmrit.symbolicprog;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JTextField;
import jmri.ProgListener;
import jmri.Programmer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulate a single CV value and provide programming access to the decoder.
 * <p>
 * Since this is a single CV in a single decoder, the Programmer used to get
 * access is part of the state. This allows us to specify a specific ops-mode
 * programmer aimed at a particular decoder.
 * <p>
 * There are three relevant parameters: Busy, Value, State. Busy == true means
 * that a read or write operation is going on. When it transitions to "false",
 * the operation is complete, and the Value and State are stable. During a read
 * operation, Value changes before State, so you can assume that Value is stable
 * if notified of a State change.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2004, 2013
 * @author Howard G. Penny Copyright (C) 2005
 */
public class CvValue extends AbstractValue implements ProgListener {

    public CvValue(String num, Programmer pProgrammer) {
        _num = num;
        mProgrammer = pProgrammer;
        _tableEntry = new JTextField("0", 3);
        _defaultColor = _tableEntry.getBackground();
        _tableEntry.setBackground(COLOR_UNKNOWN);
    }

    public CvValue(String num, String cvName, Programmer pProgrammer) {
        _num = num;
        _cvName = cvName;
        if (cvName == null) {
            log.error("cvName == null in ctor num: " + num); // NOI18N
        }
        mProgrammer = pProgrammer;
        _tableEntry = new JTextField("0", 3);
        _defaultColor = _tableEntry.getBackground();
        _tableEntry.setBackground(COLOR_UNKNOWN);
    }

    @Override
    public String toString() {
        return "CvValue _num=" + _num + " _cvName=" + _cvName;
    }

    void setProgrammer(Programmer p) {
        mProgrammer = p;
    }

    public String number() {
        return _num;
    }
    private String _num;

    public String cvName() {
        return _cvName;
    }
    private String _cvName = "";

    private JLabel _status = null;

    private Programmer mProgrammer;

    public int getValue() {
        return _value;
    }

    Color getDefaultColor() {
        return _defaultColor;
    }

    Color getColor() {
        return _tableEntry.getBackground();
    }

    protected void notifyValueChange(int value) {
        prop.firePropertyChange("Value", null, Integer.valueOf(value));
    }

    /**
     * Edit a new value into the CV. Only use this for external edits, e.g. set
     * form a GUI, not for internal uses, as it sets the state to EDITED
     */
    public void setValue(int value) {
        log.debug("CV {} value changed from {} to {}", number(), _value, value); // NOI18N

        setState(EDITED);
        if (_value != value) {
            _value = value;
            _tableEntry.setText("" + value);
            notifyValueChange(value);
        }
    }
    private int _value = 0;

    /**
     * Get the decoder value read during compare
     *
     * @return _decoderValue
     */
    public int getDecoderValue() {
        return _decoderValue;
    }

    private int _decoderValue = 0;

    public int getState() {
        return _state;
    }

    /**
     * Set state value and send notification. Also sets GUI color as needed.
     */
    public void setState(int state) {
        if (log.isDebugEnabled()) {  // stateToString overhead
            log.debug("cv " + number() + " set state from " + stateToString(_state) + " to " + stateToString(state)); // NOI18N
        }
        int oldstate = _state;
        _state = state;
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
                log.error("Inconsistent state: " + _state); // NOI18N
        }
        if (oldstate != state) {
            prop.firePropertyChange("State", Integer.valueOf(oldstate), Integer.valueOf(state));
        }
    }

    /**
     * Intended for debugging only, don't translate
     */
    String stateToString(int state) {
        switch (state) {
            case UNKNOWN:
                return "UNKNOWN";
            case EDITED:
                return "EDITED";
            case READ:
                return "READ";
            case STORED:
                return "STORED";
            case FROMFILE:
                return "FROMFILE";
            case SAME:
                return "SAME";
            case DIFF:
                return "DIFF";
            default:
                log.error("Inconsistent state: " + _state); // NOI18N
                return "ERROR!!";
        }
    }

    private int _state = 0;

    // read, write operations
    public boolean isBusy() {
        return _busy;
    }

    /**
     * set the busy state and send notification. Should be used _only_ if this
     * is the only thing changing
     */
    private void setBusy(boolean busy) {
        log.debug("setBusy from {} to {} state {}", _busy, busy, _state); // NOI18N

        boolean oldBusy = _busy;
        _busy = busy;
        notifyBusyChange(oldBusy, busy);
    }

    /**
     * Notify of changes to the busy state
     */
    private void notifyBusyChange(boolean oldBusy, boolean newBusy) {
        log.debug("notifyBusyChange from {} to {} current state {}", oldBusy, newBusy, _state); // NOI18N

        if (oldBusy != newBusy) {
            prop.firePropertyChange("Busy",
                    oldBusy ? Boolean.TRUE : Boolean.FALSE,
                    newBusy ? Boolean.TRUE : Boolean.FALSE);
        }
    }
    private boolean _busy = false;

    // color management
    Color _defaultColor;

    @Override
    void setColor(Color c) {
        if (c != null) {
            _tableEntry.setBackground(c);
        } else {
            _tableEntry.setBackground(_defaultColor);
        }
        //prop.firePropertyChange("Value", null, null);
    }

    // object for Table entry
    JTextField _tableEntry = null;

    JTextField getTableEntry() {
        return _tableEntry;
    }

    /**
     * Set bean keeping track of whether this CV is intended to be read-only.
     * Does not otherwise affect behaviour! Default is "false".
     */
    public void setReadOnly(boolean is) {
        _readOnly = is;
    }

    private boolean _readOnly = false;

    /**
     * Retrieve bean keeping track of whether this CV is intended to be
     * read-only. Does not otherwise affect behaviour! Default is "false".
     */
    public boolean getReadOnly() {
        return _readOnly;
    }

    /**
     * Set bean keeping track of whether this CV is intended to be used as
     * info-only. Does not otherwise affect behaviour! Default is "false".
     */
    public void setInfoOnly(boolean is) {
        _infoOnly = is;
    }

    private boolean _infoOnly = false;

    /**
     * Retrieve bean keeping track of whether this CV is intended to be used as
     * info-only. Does not otherwise affect behaviour! Default is "false".
     */
    public boolean getInfoOnly() {
        return _infoOnly;
    }

    /**
     * Set bean keeping track of whether this CV is intended to be used as
     * write-only. Does not otherwise affect behaviour! Default is "false".
     */
    public void setWriteOnly(boolean is) {
        _writeOnly = is;
    }

    private boolean _writeOnly = false;

    /**
     * Retrieve bean keeping track of whether this CV is intended to be used as
     * write-only. Does not otherwise affect behaviour! Default is "false".
     */
    public boolean getWriteOnly() {
        return _writeOnly;
    }

    @Override
    public void setToRead(boolean state) {
        if (getInfoOnly() || getWriteOnly()) {
            state = false;
        }
        _toRead = state;
    }

    @Override
    public boolean isToRead() {
        return _toRead;
    }
    private boolean _toRead = false;

    @Override
    public void setToWrite(boolean state) {
        if (getInfoOnly() || getReadOnly()) {
            state = false;
        }
        _toWrite = state;
    }

    @Override
    public boolean isToWrite() {
        return _toWrite;
    }
    private boolean _toWrite = false;

    // read, write support
    private boolean _reading = false;
    private boolean _confirm = false;

    public void read(JLabel status) {
        log.debug("read call with Cv number {} and programmer {}", _num, mProgrammer); // NOI18N

        setToRead(false);
        // get a programmer reference and write
        _status = status;

        if (status != null) {
            status.setText(
                    java.text.MessageFormat.format(
                            Bundle.getMessage("StateReadingCV"),
                            new Object[]{"" + _num}));
        }

        if (mProgrammer != null) {
            setBusy(true);
            _reading = true;
            _confirm = false;
            try {
                mProgrammer.readCV(_num, this);
            } catch (Exception e) {
                if (status != null) {
                    status.setText(
                            java.text.MessageFormat.format(
                                    Bundle.getMessage("StateExceptionDuringRead"),
                                    new Object[]{e.toString()}));
                }

                log.warn("Exception during CV read: " + e); // NOI18N
                setBusy(false);
            }
        } else {
            if (status != null) {
                status.setText(Bundle.getMessage("StateNoProgrammer"));
            }
            log.error("No programmer available!"); // NOI18N
        }
    }

    public void confirm(JLabel status) {
        log.debug("confirm call with Cv number {}", _num); // NOI18N

        // get a programmer reference and write
        _status = status;

        if (status != null) {
            status.setText(
                    java.text.MessageFormat.format(
                            Bundle.getMessage("StateConfirmingCV"),
                            new Object[]{"" + _num}));
        }

        if (mProgrammer != null) {
            setBusy(true);
            _reading = false;
            _confirm = true;
            try {
                mProgrammer.confirmCV(_num, _value, this);
            } catch (Exception e) {
                if (status != null) {
                    status.setText(
                            java.text.MessageFormat.format(
                                    Bundle.getMessage("StateExceptionDuringConfirm"),
                                    new Object[]{e.toString()}));
                }
                log.warn("Exception during CV confirm: " + e); // NOI18N
                setBusy(false);
            }
        } else {
            if (status != null) {
                status.setText(Bundle.getMessage("StateNoProgrammer"));
            }
            log.error("No programmer available!"); // NOI18N
        }
    }

    public void write(JLabel status) {
        log.debug("write call with Cv number {}", _num); // NOI18N

        setToWrite(false);
        // get a programmer reference and write
        _status = status;

        if (status != null) {
            status.setText(
                    java.text.MessageFormat.format(
                            Bundle.getMessage("StateWritingCV"),
                            new Object[]{"" + _num}));
        }
        if (mProgrammer != null) {
            setBusy(true);
            _reading = false;
            _confirm = false;
            try {
                setState(UNKNOWN);
                mProgrammer.writeCV(_num, _value, this);
            } catch (Exception e) {
                setState(UNKNOWN);
                if (status != null) {
                    status.setText(
                            java.text.MessageFormat.format(
                                    Bundle.getMessage("StateExceptionDuringWrite"),
                                    new Object[]{e.toString()}));
                }
                log.warn("Exception during write CV '" + _num + "' to '" + _value + "'", e); // NOI18N
                setBusy(false);
            }
        } else {
            if (status != null) {
                status.setText(Bundle.getMessage("StateNoProgrammer"));
            }
            log.error("No programmer available!"); // NOI18N
        }
    }

    @Override
    public void programmingOpReply(int value, int retval) {
        if (log.isDebugEnabled()) {
            log.debug("CV progOpReply for CV " + _num + " with retval " + retval
                    + " during "
                    + (_reading ? "read sequence"
                            : (_confirm ? "confirm sequence" : "write sequence")));  // NOI18N
        }
        if (!_busy) {
            log.error("opReply when not busy!"); // NOI18N
        }
        boolean oldBusy = _busy;
        if (retval == OK) {
            if (_status != null) {
                _status.setText(Bundle.getMessage("StateOK"));
            }
            if (_reading) {
                // set & notify value directly to avoid state going to EDITED
                _value = value;
                _tableEntry.setText(Integer.toString(value));
                notifyValueChange(value);
                setState(READ);
                log.debug("CV setting not busy on end read"); // NOI18N
                _busy = false;
                notifyBusyChange(oldBusy, _busy);
            } else if (_confirm) {
                // _value doesn't change, just the state, and save the value read 
                _decoderValue = value;
                // does the decoder value match the file value
                if (value == _value) {
                    setState(SAME);
                } else {
                    setState(DIFF);
                }
                _busy = false;
                notifyBusyChange(oldBusy, _busy);
            } else {  // writing
                setState(STORED);
                _busy = false;
                notifyBusyChange(oldBusy, _busy);
            }
        } else {
            if (_status != null) {
                _status.setText(
                        java.text.MessageFormat.format(
                                Bundle.getMessage("StateProgrammerError"),
                                new Object[]{mProgrammer.decodeErrorCode(retval)}));
            }

            // delay setting not Busy to ensure that the message appears to the user
            javax.swing.Timer timer = new javax.swing.Timer(1000, new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    errorTimeout();
                }
            });
            timer.setInitialDelay(1000);
            timer.setRepeats(false);
            timer.start();
        }
            
        log.debug("CV progOpReply end of handling CV {}", _num); // NOI18N
    }

    void errorTimeout() {
        setState(UNKNOWN);
        log.debug("CV setting not busy on error reply"); // NOI18N
        _busy = false;
        notifyBusyChange(true, _busy);
    }

    // clean up connections when done
    public void dispose() {
        log.debug("dispose"); // NOI18N
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(CvValue.class);

}
