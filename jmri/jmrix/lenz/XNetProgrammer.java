/**
 * XNetProgrammer.java
 */

 // Convert the jmri.Programmer interface into commands for the Lenz XpressNet

package jmri.jmrix.lenz;

import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;
import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Programmer support for Lenz XpressNet.
 * Currently only register and paged mode is implemented.
 * <P>
 * The read operation state sequence is:
 * <UL>
 * <LI>Send Register Mode / Paged mode read request
 * <LI>Wait for specific reply
 * <LI>Wait for Broadcast Service Mode Entry message
 * <LI>Send Request for Service Mode Results request
 * <LI>Wait for results reply, interpret
 * <LI>Send Resume Operations request
 * <LI>Wait for specific reply
 * <LI>Wait for Normal Operations Resumed broadcast
 * </UL>
 * @author Bob Jacobsen  Copyright (c) 2002
 * @version $Revision: 1.2 $
 */
public class XNetProgrammer extends AbstractProgrammer implements XNetListener {

	public XNetProgrammer() {
		// error if more than one constructed?
		if (self != null)
			log.error("Creating too many XNetProgrammer objects");

		// register this as the default, register as the Programmer
		self = this;
		jmri.InstanceManager.setProgrammer(this);

		}

	/*
	 * method to find the existing NceProgrammer object, if need be creating one
	 */
	static public final XNetProgrammer instance() {
		if (self == null) self = new XNetProgrammer();
		return self;
		}
	static XNetProgrammer self = null;  // needs to be accessible from tests

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
	public int getMode() { return _mode; }
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
		controller().sendXNetMessage(XNetTrafficController.instance()
                                    .getCommandStation().getEnterProgModeMsg());
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
		controller().sendXNetMessage(XNetTrafficController.instance()
                                    .getCommandStation().getEnterProgModeMsg());

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

	// internal method to create the XNetMessage for programmer task start
	protected XNetMessage progTaskStart(int mode, int val, int cvnum) throws jmri.ProgrammerException {
		// val = -1 for read command; mode is direct, etc
		if (val < 0) {
			// read
            if (_mode == Programmer.PAGEMODE)
    			return XNetTrafficController.instance()
                                    .getCommandStation().getReadPagedCVMsg(cvnum);
            else
    			return XNetTrafficController.instance()
                                    .getCommandStation().getReadRegisterMsg(registerFromCV(cvnum));
		} else {
			// write
            if (_mode == Programmer.PAGEMODE)
                return XNetTrafficController.instance()
                                    .getCommandStation().getWritePagedCVMsg(cvnum, val);
            else
			    return XNetTrafficController.instance()
                                    .getCommandStation().getWriteRegisterMsg(registerFromCV(cvnum), val);
		}
	}

	public void message(XNetMessage m) {
		log.error("message received unexpectedly: "+m.toString());
	}

	synchronized public void reply(XNetMessage m) {  // was of reply type
		if (progState == NOTPROGRAMMING) {
			// we get the complete set of replies now, so ignore these
			if (log.isDebugEnabled()) log.debug("reply in NOTPROGRAMMING state");
			return;
		} else if (progState == MODESENT) {
			if (log.isDebugEnabled()) log.debug("reply in MODESENT state");
			// see if reply is the acknowledge of program mode; if not, wait

            // ...

			// here ready to send the read/write command
			progState = COMMANDSENT;
			// see why waiting
			try {
				startLongTimer();
				if (_progRead) {
					// read was in progress - send read command
					controller().sendXNetMessage(progTaskStart(getMode(), -1, _cv));
				} else {
					// write was in progress - send write command
					controller().sendXNetMessage(progTaskStart(getMode(), _val, _cv));
				}
			} catch (Exception e) {
				// program op failed, go straight to end
				log.error("program operation failed, exception "+e);
				progState = RETURNSENT;
				controller().sendXNetMessage(XNetTrafficController.instance()
                                    .getCommandStation().getExitProgModeMsg());
			}
		} else if (progState == COMMANDSENT) {
			if (log.isDebugEnabled()) log.debug("reply in COMMANDSENT state");
			// operation done, capture result, then have to leave programming mode
			progState = RETURNSENT;
			// check for errors
			    //if (m.match("NO FEEDBACK DETECTED") >= 0) {
				//if (log.isDebugEnabled()) log.debug("handle NO FEEDBACK DETECTED");
				//// perhaps no loco present? Fail back to end of programming
				//progState = NOTPROGRAMMING;
				//controller().sendXNetMessage(XNetTrafficController.getInstance()
                //                    .getCommandStation().getExitProgMode(), this);
				//notifyProgListenerEnd(_val, jmri.ProgListener.NoLocoDetected);
			    //}
			/*else*/ {
				// see why waiting
				if (_progRead) {
					// read was in progress - get return value
					_val = m.getElement(3);
				}
				startShortTimer();
				controller().sendXNetMessage(XNetTrafficController.instance()
                                    .getCommandStation().getExitProgModeMsg());
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
			controller().sendXNetMessage(XNetTrafficController.instance()
                                    .getCommandStation().getExitProgModeMsg());
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

	XNetTrafficController _controller = null;

	protected XNetTrafficController controller() {
		// connect the first time
		if (_controller == null) {
			_controller = XNetTrafficController.instance();
		}
		return _controller;
	}

   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetProgrammer.class.getName());

}


/* @(#)XNetProgrammer.java */
