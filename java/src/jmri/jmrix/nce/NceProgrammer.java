// NceProgrammer.java

package jmri.jmrix.nce;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

/**
 * Convert the jmri.Programmer interface into commands for the NCE power house.
 * <P>
 * This has two states:  NOTPROGRAMMING, and COMMANDSENT.  The transitions
 * to and from programming mode are now handled in the TrafficController code.
 * @author	Bob Jacobsen  Copyright (C) 2001
 * @version     $Revision$
 */
public class NceProgrammer extends AbstractProgrammer implements NceListener {
	
    protected NceTrafficController tc;

    public NceProgrammer(NceTrafficController tc) {
    	this.tc = tc;
        super.SHORT_TIMEOUT = 4000;
        if (tc != null && tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3){
        	_mode = Programmer.OPSBYTEMODE;
        }
    }

    // handle mode
    protected int _mode = Programmer.PAGEMODE;

    /**
     * Switch to a new programming mode.  Note that NCE can only
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
        if (!hasMode(_mode)) {
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
    	if (tc != null && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_POWERCAB &&
    			tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE){
    		log.debug("NCE USB-SB3/SB5/TWIN hasMode returns false on mode "+mode);
    		return false;
    	}
        if ( mode == Programmer.PAGEMODE ||
             mode == Programmer.REGISTERMODE ) {
            log.debug("hasMode request on mode "+mode+" returns true (1)");
            return true;
        }
        if ( mode == Programmer.DIRECTBYTEMODE && tc != null &&
        		tc.getCommandOptions() >= NceTrafficController.OPTION_2006) {
        	log.debug("hasMode request on mode "+mode+" returns true (2)");
        	return true;
        }
        log.debug("hasMode returns false on mode "+mode);
        return false;
    }

    public int getMode() { return _mode; }

    @Override
    public boolean getCanRead() {
    	if (tc != null && tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_POWERCAB &&
    			tc.getUsbSystem() != NceTrafficController.USB_SYSTEM_NONE)
    		return false;
    	else
    		return true;
    	}

    public boolean getCanWrite(String cv) {
       if ((Integer.parseInt(cv) > 256) &&
               ((getMode() == Programmer.PAGEMODE) ||
                   (getMode() == Programmer.DIRECTBYTEMODE) ||
                   (getMode() == Programmer.REGISTERMODE)
               ) && ((tc != null) && (
                       (tc.getCommandOptions() == NceTrafficController.OPTION_1999) |
                       (tc.getCommandOptions() == NceTrafficController.OPTION_2004) |
                       (tc.getCommandOptions() == NceTrafficController.OPTION_2006))
                   )
               ) {
            return false;
        } else {
            return true;
        }
    }
    
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
    static final int COMMANDSENT_2 = 4;	// ops programming mode, send msg twice
    boolean  _progRead = false;
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    public synchronized void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) log.debug("writeCV "+CV+" listens "+p);
        useProgrammer(p);
        // prevent writing Op mode CV > 255 on PowerHouse 2007C and earlier
        if ((CV > 256) && 
        		((getMode() == Programmer.PAGEMODE) ||
    				(getMode() == Programmer.DIRECTBYTEMODE) ||
    				(getMode() == Programmer.REGISTERMODE)
        		) && ((tc != null) && (
        				(tc.getCommandOptions() == NceTrafficController.OPTION_1999) | 
    					(tc.getCommandOptions() == NceTrafficController.OPTION_2004) | 
    					(tc.getCommandOptions() == NceTrafficController.OPTION_2006))
					)
				)
        	throw new jmri.ProgrammerException("CV number not supported");
        _progRead = false;
        // set state
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

        try {
            // start the error timer
            startLongTimer();

            // format and send the write message
            tc.sendNceMessage(progTaskStart(getMode(), _val, _cv), this);
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }
    }

    public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    public synchronized void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) log.debug("readCV "+CV+" listens "+p);
        useProgrammer(p);
        _progRead = true;

        // set commandPending state
        progState = COMMANDSENT;
        _cv = CV;

        try {
            // start the error timer
            startLongTimer();

            // format and send the write message
            tc.sendNceMessage(progTaskStart(getMode(), -1, _cv), this);
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
            if (log.isInfoEnabled()) log.info("programmer already in use by "+_usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        }
        else {
            _usingProgrammer = p;
            return;
        }
    }

    // internal method to create the NceMessage for programmer task start
    protected NceMessage progTaskStart(int mode, int val, int cvnum) throws jmri.ProgrammerException {
        // val = -1 for read command; mode is direct, etc
        if (val < 0) {
            // read
            if (_mode == Programmer.PAGEMODE)
                return NceMessage.getReadPagedCV(tc, cvnum);
            else if (_mode == Programmer.DIRECTBYTEMODE)
                return NceMessage.getReadDirectCV(tc, cvnum);
			else
                return NceMessage.getReadRegister(tc, registerFromCV(cvnum));
        } else {
            // write
            if (_mode == Programmer.PAGEMODE)
                return NceMessage.getWritePagedCV(tc, cvnum, val);
            else if (_mode == Programmer.DIRECTBYTEMODE)
                return NceMessage.getWriteDirectCV(tc, cvnum, val);
            else
                return NceMessage.getWriteRegister(tc, registerFromCV(cvnum), val);
        }
    }

    public void message(NceMessage m) {
        log.error("message received unexpectedly: "+m.toString());
    }

    public synchronized void reply(NceReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            if (log.isDebugEnabled()) log.debug("reply in NOTPROGRAMMING state");
            return;
        } else if (progState == COMMANDSENT) {
            if (log.isDebugEnabled()) log.debug("reply in COMMANDSENT state");
            // operation done, capture result, then post response
            progState = NOTPROGRAMMING;
            // check for errors
            if ((m.match("NO FEEDBACK DETECTED") >= 0) 
                    || (m.isBinary() && !_progRead && (m.getElement(0) != '!'))
                    || (m.isBinary() && _progRead && (m.getElement(1) != '!'))) {
                if (log.isDebugEnabled()) log.debug("handle NO FEEDBACK DETECTED");
                // perhaps no loco present? Fail back to end of programming
                notifyProgListenerEnd(_val, jmri.ProgListener.NoLocoDetected);
            }
            else {
                // see why waiting
                if (_progRead) {
                    // read was in progress - get return value
                    _val = m.value();
                }
                // if this was a read, we retrieved the value above.  If its a
                // write, we're to return the original write value
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            }
        
        } else if (progState == COMMANDSENT_2) {
            if (log.isDebugEnabled()) log.debug("first reply in COMMANDSENT_2 state");
            // first message sent, now wait for second reply to arrive
            progState = COMMANDSENT;
        } else {
            if (log.isDebugEnabled()) log.debug("reply in un-decoded state");
        }
    }

    /**
     * Internal routine to handle a timeout
     */
    protected synchronized void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            if (log.isDebugEnabled()) log.debug("timeout!");
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            cleanup();
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    // Internal method to cleanup in case of a timeout. Separate routine
    // so it can be changed in subclasses.
    void cleanup() {
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

    static Logger log = LoggerFactory.getLogger(NceProgrammer.class.getName());

}


/* @(#)NceProgrammer.java */

