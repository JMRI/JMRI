// QsiProgrammer.java

package jmri.jmrix.qsi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

/**
 * Implements the jmri.Programmer interface via commands for the QSI programmer.
 *
 * @author      Bob Jacobsen  Copyright (C) 2001, 2008
 * @version	$Revision$
 */
public class QsiProgrammer extends AbstractProgrammer implements QsiListener {

    protected QsiProgrammer() {
        // error if more than one constructed?
        if (self != null)
            log.error("Creating too many QsiProgrammer objects");
    }

    /*
     * method to find the existing QsiProgrammer object, if need be creating one
     */
    static public final QsiProgrammer instance() {
        if (self == null) self = new QsiProgrammer();
        return self;
    }
    static volatile private QsiProgrammer self = null;

    // handle mode
    protected int _mode = Programmer.DIRECTBITMODE;

    /**
     * Switch to a new programming mode.  QSI currently supports bit
     * direct and paged mode. If you attempt to switch to any others, the
     * new mode will set & notify, then set back to the original.  This
     * lets the listeners know that a change happened, and then was undone.
     * @param mode The new mode, use values from the jmri.Programmer interface
     */
    public void setMode(int mode) {
        int oldMode = _mode;  // preserve this in case we need to go back
        if (mode != _mode) {
            log.debug("change to mode "+mode);
            notifyPropertyChange("Mode", _mode, mode);
            _mode = mode;
        }
        if (_mode != Programmer.DIRECTBITMODE && _mode != Programmer.PAGEMODE) {
            // attempt to switch to unsupported mode, switch back to previous
            _mode = oldMode;
            log.debug("switching back to old supported mode "+_mode);
            notifyPropertyChange("Mode", mode, _mode);
        }
    }

    /**
     * Signifies mode's available
     * @param mode
     * @return True if paged or direct mode
     */
    public boolean hasMode(int mode) {
        if ( mode == Programmer.DIRECTBITMODE ||
             mode == Programmer.PAGEMODE ) {
            log.debug("hasMode request on mode "+mode+" returns true");
            return true;
        }
        log.debug("hasMode request on mode "+mode+" returns false");
        return false;
    }
    public int getMode() { return _mode; }

    public boolean getCanRead() { return true; }

    // notify property listeners - see AbstractProgrammer for more

    @SuppressWarnings("unchecked")
	protected void notifyPropertyChange(String name, int oldval, int newval) {
        // make a copy of the listener vector to synchronized not needed for transmit
        Vector<PropertyChangeListener> v;
        synchronized(this) {
            v = (Vector<PropertyChangeListener>) propListeners.clone();
        }
        // forward to all listeners
        int cnt = v.size();
        for (int i=0; i < cnt; i++) {
            PropertyChangeListener client = v.elementAt(i);
            client.propertyChange(new PropertyChangeEvent(this, name, Integer.valueOf(oldval), Integer.valueOf(newval)));
        }
    }

    // members for handling the programmer interface

    int progState = 0;
    static final int NOTPROGRAMMING = 0;// is notProgramming
    static final int COMMANDSENT = 2; 	// read/write command sent, waiting ack
    static final int WAITRESULT = 4; 	// waiting reply with data
    static final int WAITRESETSTATUS = 6; 	// waiting reply from reseting status
    boolean  _progRead = false;
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    public synchronized void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) log.debug("writeCV "+CV+" listens "+p);
        useProgrammer(p);
        _progRead = false;
        // set commandPending state
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

        // start the error timer
        startShortTimer();

        // format and send message to do write
        controller().sendQsiMessage(QsiMessage.getWriteCV(CV, val, getMode()), this);
    }

    public synchronized void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    public synchronized void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) log.debug("readCV "+CV+" listens "+p);
        useProgrammer(p);
        _progRead = true;
        // set commandPending state
        progState = COMMANDSENT;
        _cv = CV;

        // start the error timer
        startShortTimer();

        // format and send message to do read
        // QSI programer is in program mode by default but this doesn't do any harm
        controller().sendQsiMessage(QsiMessage.getReadCV(CV, getMode()), this);

    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isInfoEnabled()) log.info("programmer already in use by "+_usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        }
        else {
            _usingProgrammer = p;
            return;
        }
    }

    public void message(QsiMessage m) {
        log.error("message received unexpectedly: "+m.toString());
    }

    synchronized public void reply(QsiReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            if (log.isDebugEnabled()) log.debug("reply in NOTPROGRAMMING state");
            return;
        } else if (progState == COMMANDSENT) {
            if (log.isDebugEnabled()) log.debug("reply in COMMANDSENT state");
            // operation started, move to next mode
            progState = WAITRESULT;
            startLongTimer();
       } else if (progState == WAITRESULT) {
            if (log.isDebugEnabled()) log.debug("reply in WAITRESULT state");
            stopTimer();
            // send QSI ack
            controller().sendQsiMessage(QsiReply.getAck(m), null);
            // operation done, capture result, then leave programming mode
            progState = NOTPROGRAMMING;
            // check for errors
            if (m.getElement(4) != 0) {
                // status present
                if (log.isDebugEnabled()) log.debug("handle non-zero status in reply "+m);
                // perhaps no loco present? 
                // reset status
                progState = WAITRESETSTATUS;
                startShortTimer();
                controller().sendQsiMessage(QsiMessage.getClearStatus(), this);
            }
            else {
                // ended OK!
                if (_progRead == true)
                    _val = m.value();
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            }
        } else if (progState == WAITRESETSTATUS) {
            if (log.isDebugEnabled()) log.debug("reply in WAITRESETSTATUS state");
            // all done, notify listeners of completion
            progState = NOTPROGRAMMING;
            stopTimer();
            // notify of default error (not timeout)
            notifyProgListenerEnd(-1, jmri.ProgListener.NoLocoDetected);
        } else {
            if (log.isDebugEnabled()) log.debug("reply in un-decoded state");
        }
    }

    /**
     * Internal routine to handle a timeout
     */
    synchronized protected void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            if (log.isDebugEnabled()) log.debug("timeout!");
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            // send message to clear error
            controller().sendQsiMessage(QsiMessage.getClearStatus(), null);
            // report timeout
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    // internal method to notify of the final result
    protected void notifyProgListenerEnd(int value, int status) {
        if (log.isDebugEnabled()) log.debug("notifyProgListenerEnd value "+value+" status "+status);
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        jmri.ProgListener temp = _usingProgrammer;
        _usingProgrammer = null;
        temp.programmingOpReply(value, status);
    }

    QsiTrafficController _controller = null;

    protected QsiTrafficController controller() {
        // connect the first time
        if (_controller == null) {
            _controller = QsiTrafficController.instance();
        }
        return _controller;
    }

    static Logger log = LoggerFactory.getLogger(QsiProgrammer.class.getName());

}

/* @(#)QsiProgrammer.java */
