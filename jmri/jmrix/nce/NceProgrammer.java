/**
 * NceProgrammer.java
 *
 * Description:		<describe the NceProgrammer class here>
 * @author			Bob Jacobsen  Copyright (C) 2001
 * @version
 */

 // Convert the jmri.Programmer interface into commands for the NCE powerstation

package jmri.jmrix.nce;

import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;
import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class NceProgrammer extends AbstractProgrammer implements NceListener {

	public NceProgrammer() {
		// error if more than one constructed?
		if (self != null)
			log.error("Creating too many NceProgrammer objects");

		// register this as the default, register as the Programmer
		self = this;
		jmri.InstanceManager.setProgrammer(this);

		}

	/*
	 * method to find the existing NceProgrammer object, if need be creating one
	 */
	static public final NceProgrammer instance() {
		if (self == null) self = new NceProgrammer();
		return self;
		}
	static private NceProgrammer self = null;

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
		if (_mode != Programmer.PAGEMODE && _mode != Programmer.REGISTERMODE) {
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
        if ( mode == Programmer.PAGEMODE ||
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

	protected void notifyPropertyChange(String name, int oldval, int newval) {
		// make a copy of the listener vector to synchronized not needed for transmit
		Vector v;
		synchronized(this) {
			v = (Vector) propListeners.clone();
		}
		// forward to all listeners
		int cnt = v.size();
		for (int i=0; i < cnt; i++) {
			PropertyChangeListener client = (PropertyChangeListener) v.elementAt(i);
			client.propertyChange(new PropertyChangeEvent(this, name, new Integer(oldval), new Integer(newval)));
		}
	}

	// members for handling the programmer interface

	int progState = 0;
		static final int NOTPROGRAMMING = 0;// is notProgramming
		static final int MODESENT = 1; 		// waiting reply to command to go into programming mode
		static final int COMMANDSENT = 2; 	// read/write command sent, waiting reply
		static final int RETURNSENT = 4; 	// waiting reply to go back to ops mode
	boolean  _progRead = false;
	int _val;	// remember the value being read/written for confirmative reply
	int _cv;	// remember the cv being read/written

	// programming interface
	public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
		if (log.isDebugEnabled()) log.debug("writeCV "+CV+" listens "+p);
		useProgrammer(p);
		_progRead = false;
		// set commandPending state
		progState = MODESENT;
		_val = val;
		_cv = CV;

		// start the error timer
		startShortTimer();

		// format and send message to go to program mode
		controller().sendNceMessage(NceMessage.getProgMode(), this);
	}

	public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
		readCV(CV, p);
	}

	public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
		if (log.isDebugEnabled()) log.debug("readCV "+CV+" listens "+p);
		useProgrammer(p);
		_progRead = true;
		// set commandPending state
		// set commandPending state
		progState = MODESENT;
		_cv = CV;

		// start the error timer
		startShortTimer();

		// format and send message to go to program mode
		controller().sendNceMessage(NceMessage.getProgMode(), this);

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
    			return NceMessage.getReadPagedCV(cvnum);
            else
    			return NceMessage.getReadRegister(registerFromCV(cvnum));
		} else {
			// write
            if (_mode == Programmer.PAGEMODE)
                return NceMessage.getWritePagedCV(cvnum, val);
            else
			    return NceMessage.getWriteRegister(registerFromCV(cvnum), val);
		}
	}

	public void message(NceMessage m) {
		log.error("message received unexpectedly: "+m.toString());
	}

	synchronized public void reply(NceReply m) {
		if (progState == NOTPROGRAMMING) {
			// we get the complete set of replies now, so ignore these
			if (log.isDebugEnabled()) log.debug("reply in NOTPROGRAMMING state");
			return;
		} else if (progState == MODESENT) {
			if (log.isDebugEnabled()) log.debug("reply in MODESENT state");
			// see if reply is the acknowledge of program mode; if not, wait
			if ( (m.match("PROGRAMMING MODE") == -1) &&
				 (m.match("COMMAND NOT UNDERSTOOD") == -1)  // indicates already in program mode
					) return;
			// here ready to send the read/write command
			progState = COMMANDSENT;
			// see why waiting
			try {
				startLongTimer();
				if (_progRead) {
					// read was in progress - send read command
					controller().sendNceMessage(progTaskStart(getMode(), -1, _cv), this);
				} else {
					// write was in progress - send write command
					controller().sendNceMessage(progTaskStart(getMode(), _val, _cv), this);
				}
			} catch (Exception e) {
				// program op failed, go straight to end
				log.error("program operation failed, exception "+e);
				progState = RETURNSENT;
				controller().sendNceMessage(NceMessage.getExitProgMode(), this);
			}
		} else if (progState == COMMANDSENT) {
			if (log.isDebugEnabled()) log.debug("reply in COMMANDSENT state");
			// operation done, capture result, then have to leave programming mode
			progState = RETURNSENT;
			// check for errors
			if (m.match("NO FEEDBACK DETECTED") >= 0) {
				if (log.isDebugEnabled()) log.debug("handle NO FEEDBACK DETECTED");
				// perhaps no loco present? Fail back to end of programming
				progState = NOTPROGRAMMING;
				controller().sendNceMessage(NceMessage.getExitProgMode(), this);
				notifyProgListenerEnd(_val, jmri.ProgListener.NoLocoDetected);
			}
			else {
				// see why waiting
				if (_progRead) {
					// read was in progress - get return value
					_val = m.value();
				}
				startShortTimer();
				controller().sendNceMessage(NceMessage.getExitProgMode(), this);
			}
		} else if (progState == RETURNSENT) {
			if (log.isDebugEnabled()) log.debug("reply in RETURNSENT state");
			// all done, notify listeners of completion
			progState = NOTPROGRAMMING;
			stopTimer();
			// if this was a read, we cached the value earlier.  If its a
			// write, we're to return the original write value
			notifyProgListenerEnd(_val, jmri.ProgListener.OK);
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
			controller().sendNceMessage(NceMessage.getExitProgMode(), this);
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

	NceTrafficController _controller = null;

	protected NceTrafficController controller() {
		// connect the first time
		if (_controller == null) {
			_controller = NceTrafficController.instance();
		}
		return _controller;
	}

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceProgrammer.class.getName());

}


/* @(#)NceProgrammer.java */
