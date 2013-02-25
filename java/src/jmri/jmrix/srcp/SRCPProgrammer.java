// SRCPProgrammer.java

package jmri.jmrix.srcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;
import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Implements the jmri.Programmer interface via commands for the SRCP powerstation
 *
 * @author			Bob Jacobsen  Copyright (C) 2001, 2008
 * @version			$Revision$
 */
public class SRCPProgrammer extends AbstractProgrammer implements SRCPListener {

    protected SRCPSystemConnectionMemo _memo=null;

    public SRCPProgrammer(SRCPSystemConnectionMemo memo) {
        _memo=memo;
        // need a longer LONG_TIMEOUT
        LONG_TIMEOUT=180000;
    }

    // handle mode
    protected int _mode = Programmer.DIRECTBYTEMODE;

    /**
     * Switch to a new programming mode.  Note that SRCP can only
     * do register and page mode. If you attempt to switch to
     * any others, the new mode will set & notify, then
     * set back to the original.  This lets the listeners
     * know that a change happened, and then was undone.
     * @param mode The new mode, use values from the jmri.Programmer interface
     */
    public void setMode(int mode) {
        int oldMode = _mode;  // preserve this in case we need to go back
        if (mode != _mode) {
            notifyPropertyChange("Mode", _mode, mode);
            _mode = mode;
        }
        if (_mode != Programmer.DIRECTBYTEMODE && _mode != Programmer.REGISTERMODE) {
            // attempt to switch to unsupported mode, switch back to previous
            _mode = oldMode;
            notifyPropertyChange("Mode", mode, _mode);
        }
    }
    /**
     * Signifies mode's available
     * @param mode
     * @return True if paged or register mode
     */
    public boolean hasMode(int mode) {
        if ( mode == Programmer.DIRECTBYTEMODE ||
             mode == Programmer.REGISTERMODE ) {
            log.debug("hasMode request on mode "+mode+" returns true");
            return true;
        }
        log.debug("hasMode returns false on mode "+mode);
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
    static final int COMMANDSENT = 2; 	// read/write command sent, waiting reply
    boolean  _progRead = false;
    boolean  _progConfirm = false;
    int _confirmVal;  // remember the value to be confirmed for reply
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) log.debug("writeCV "+CV+" listens "+p);
        useProgrammer(p);
        _progRead = false;
	_progConfirm = false;
        // set commandPending state
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

        try {
	    SRCPMessage m;
            // start the error timer
            startLongTimer();

            // write
            if (getMode() == Programmer.DIRECTBYTEMODE)
                m = SRCPMessage.getWriteDirectCV(_cv, _val);
            else
                m = SRCPMessage.getWriteRegister(registerFromCV(_cv), _val);
            // format and send the write message
            controller().sendSRCPMessage(m, this);
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }
    }

    public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) log.debug("confirmCV "+CV+" val "+val+" listens "+p);
        useProgrammer(p);
	_progRead = false;
	_progConfirm = true;

        progState = COMMANDSENT;
        _cv = CV;
        _confirmVal = val;

        try {
	    SRCPMessage m;
            // start the error timer
            startLongTimer();
	    
            if (getMode() == Programmer.DIRECTBYTEMODE)
                m = SRCPMessage.getConfirmDirectCV(_cv, _confirmVal);
            else
                m = SRCPMessage.getConfirmRegister(registerFromCV(_cv), _confirmVal);

            // format and send the confirm message
            controller().sendSRCPMessage(m, this);
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }
	
        //readCV(CV, p);
    }

    public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) log.debug("readCV "+CV+" listens "+p);
        useProgrammer(p);
        _progRead = true;
	_progConfirm = false;

        progState = COMMANDSENT;
        _cv = CV;

        try {
	    SRCPMessage m;
            // start the error timer
            startLongTimer();

            // format and send the write message

            if (getMode() == Programmer.DIRECTBYTEMODE)
                m = SRCPMessage.getReadDirectCV(_cv);
            else
                m = SRCPMessage.getReadRegister(registerFromCV(_cv));

            controller().sendSRCPMessage(m, this);
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }

    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isDebugEnabled()) log.debug("programmer already in use by "+_usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        }
        else {
            _usingProgrammer = p;
            return;
        }
    }

    public void message(SRCPMessage m) {
        log.error("message received unexpectedly: "+m.toString());
    }

    synchronized public void reply(SRCPReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            if (log.isDebugEnabled()) log.debug("reply in NOTPROGRAMMING state");
	    if (!m.isResponseOK()) log.warn("Reply \""+m.toString()+"\"");
            return;
        } else if (progState == COMMANDSENT) {
            if (log.isDebugEnabled()) log.debug("reply in COMMANDSENT state");
            // operation done, capture result, then have to leave programming mode
            progState = NOTPROGRAMMING;
            // check for errors
            if (!m.isResponseOK()) {
                if (log.isDebugEnabled()) log.debug("handle error reply "+m);
		log.warn("Reply \""+m.toString()+"\"");
		if (_progConfirm && m.getResponseCode().equals("412")) {
		    // handle the Verify return message "412 ERROR wrong value"
		    notifyProgListenerEnd(_val, jmri.ProgListener.ConfirmFailed);
		    return;
		}
                // perhaps no loco present? Fail back to end of programming
                notifyProgListenerEnd(-1, jmri.ProgListener.NoLocoDetected);
            } else {
                // see why waiting
                if (_progRead) {
                // read was in progress - get return value
                    _val = m.value();
                }
		if (_progConfirm) {
		    _val = _confirmVal;
		}
                // If this was a read or verify, we retreived the value above. 
                // If its a write, we're to return the original write value.
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            }
        }
    }

    synchronized public void reply(jmri.jmrix.srcp.parser.SimpleNode n) {
       if(log.isDebugEnabled())
          log.debug("reply called with simpleNode " + n.jjtGetValue());
       //if(n.jjtGetChild(3).getClass()==jmri.jmrix.srcp.parser.ASTsm.class)
          reply(new SRCPReply(n));
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
            cleanup();
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    /**
     * Internal method to send a cleanup message (if needed) on timeout.
     * <P>
     * Here, it sends a request to exit from programming mode.  But
     * subclasses, e.g. ops mode, may redefine that.
     */
    void cleanup() {
        controller().sendSRCPMessage(SRCPMessage.getExitProgMode(), this);
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

    SRCPTrafficController _controller = null;

    protected SRCPTrafficController controller() {
        // connect the first time
        if (_controller == null) {
            _controller = _memo.getTrafficController();
        }
        return _controller;
    }

    static Logger log = LoggerFactory.getLogger(SRCPProgrammer.class.getName());

}


/* @(#)SRCPProgrammer.java */
