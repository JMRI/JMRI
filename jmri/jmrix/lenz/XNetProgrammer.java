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
 * <P>
 * The read operation state sequence is:
 * <UL>
 * <LI>Send Register Mode / Paged mode /Direct Mode read request
 * <LI>Wait for Broadcast Service Mode Entry message
 * <LI>Send Request for Service Mode Results request
 * <LI>Wait for results reply, interpret
 * <LI>Send Resume Operations request
 * <LI>Wait for Normal Operations Resumed broadcast
 * </UL>
 * @author Bob Jacobsen  Copyright (c) 2002
 * @author Paul Bender  Copyright (c) 2003,2004
 * @version $Revision: 2.2 $
 */
public class XNetProgrammer extends AbstractProgrammer implements XNetListener {

	static private final int XNetProgrammerTimeout = 20000;

	public XNetProgrammer() {
		// error if more than one constructed?
		if (self != null)
			log.error("Creating too many XNetProgrammer objects");

		// register this as the default, register as the Programmer
		self = this;

        // connect to listen
        controller().addXNetListener(~0, this);

    }

	/*
	 * method to find the existing XNetProgrammer object, if need be creating one
	 */
	static public final XNetProgrammer instance() {
		if (self == null) self = new XNetProgrammer();
		return self;
		}
	static XNetProgrammer self = null;  // needs to be accessible from tests

	// handle mode
	protected int _mode = Programmer.PAGEMODE;

    /**
     * Switch to a new programming mode.  Lenz can now only
     * do register, page, and direct mode. If you attempt to 
     * switch to any others, the new mode will set & notify, 
     * then set back to the original.  This lets the listeners
     * know that a change happened, and then was undone.
     * @param mode The new mode, use values from the jmri.Programmer interface
     */
	public void setMode(int mode) {
        int oldMode = _mode;  // preserve this in case we need to go back
		if (mode != _mode) {
			notifyPropertyChange("Mode", _mode, mode);
			_mode = mode;
		}
		if (_mode != Programmer.PAGEMODE && _mode != Programmer.REGISTERMODE
                && mode != Programmer.DIRECTBITMODE && mode != Programmer.DIRECTBYTEMODE ) {
            // attempt to switch to unsupported mode, switch back to previous
			_mode = oldMode;
			notifyPropertyChange("Mode", mode, _mode);
		}
	}
	public int getMode() { return _mode; }
    /**
     * Signifies mode's available
     * @param mode
     * @return True if paged,register,or Direct Mode (Bit or Byte) mode
     */
    public boolean hasMode(int mode) {
        if ( mode == Programmer.PAGEMODE ||
             mode == Programmer.REGISTERMODE ||
             mode == Programmer.DIRECTBITMODE ||
             mode == Programmer.DIRECTBYTEMODE ) {
            log.debug("hasMode request on mode "+mode+" returns true");
            return true;
        }
        log.debug("hasMode returns false on mode "+mode);
        return false;
    }

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
		static final int NOTPROGRAMMING = 0; // is notProgramming
		static final int REQUESTSENT    = 1; // waiting reply to command to go into programming mode
		static final int INQUIRESENT    = 2; // read/write command sent, waiting reply
	boolean  _progRead = false;
	int _val;	// remember the value being read/written for confirmative reply
	int _cv;	// remember the cv being read/written

	// programming interface
	public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
		if (log.isDebugEnabled()) log.debug("writeCV "+CV+" listens "+p);
		useProgrammer(p);
		_progRead = false;
		// set new state & save values
		progState = REQUESTSENT;
		_val = val;
		_cv = CV;
		
		try {
		   // start the error timer
		   restartTimer(XNetProgrammerTimeout);

		   // format and send message to go to program mode
        	   if (_mode == Programmer.PAGEMODE)
		       controller().sendXNetMessage(XNetMessage.getWritePagedCVMsg(CV, val), this);
        	   else if (_mode == Programmer.DIRECTBITMODE || _mode == Programmer.DIRECTBYTEMODE)
		       controller().sendXNetMessage(XNetMessage.getWriteDirectCVMsg(CV, val), this);
        	   else // register mode by elimination 
                       controller().sendXNetMessage(XNetMessage.getWriteRegisterMsg(registerFromCV(CV), val),this);
		} catch (jmri.ProgrammerException e) {
		  progState = NOTPROGRAMMING;
		  throw e;
	        }
	}

	public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
		readCV(CV, p);
	}

	public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
		if (log.isDebugEnabled()) log.debug("readCV "+CV+" listens "+p);
		useProgrammer(p);
		_progRead = true;
		// set new state
		progState = REQUESTSENT;
		_cv = CV;
		try {
		   // start the error timer
		   restartTimer(XNetProgrammerTimeout);

		   // format and send message to go to program mode
        	   if (_mode == Programmer.PAGEMODE)
		       controller().sendXNetMessage(XNetMessage.getReadPagedCVMsg(CV), this);
                   else if (_mode == Programmer.DIRECTBITMODE || _mode == Programmer.DIRECTBYTEMODE)
		       controller().sendXNetMessage(XNetMessage.getReadDirectCVMsg(CV), this);
        	   else // register mode by elimination		     
		       controller().sendXNetMessage(XNetMessage.getReadRegisterMsg(registerFromCV(CV)), this);
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

	synchronized public void message(XNetReply m) {
		if (progState == NOTPROGRAMMING) {
			// we get the complete set of replies now, so ignore these
			return;

		} else if (progState == REQUESTSENT) {
		   	if (log.isDebugEnabled()) log.debug("reply in REQUESTSENT state");
			// see if reply is the acknowledge of program mode; if not, wait for next
            		if (m.isOkMessage() || (m.getElement(0)==XNetConstants.CS_INFO && 
                	   (m.getElement(1)==XNetConstants.BC_SERVICE_MODE_ENTRY ||
		            m.getElement(1)==XNetConstants.PROG_CS_READY )) ) {
			       stopTimer();

			       // here ready to request the results
			       progState = INQUIRESENT;
                	       //start the error timer
			       restartTimer(XNetProgrammerTimeout);

			       controller().sendXNetMessage(XNetMessage.getServiceModeResultsMsg(),
                                                            this);
                               return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
                           m.getElement(1)==XNetConstants.CS_NOT_SUPPORTED) {
                           // programming operation not supported by this command station
			   progState = NOTPROGRAMMING;
			   notifyProgListenerEnd(_val, jmri.ProgListener.NotImplemented);
                           return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
			   m.getElement(1)==XNetConstants.BC_NORMAL_OPERATIONS) {
			   // We Exited Programming Mode early
			   log.error("Service mode exited before sequence complete.");
			   progState = NOTPROGRAMMING;
			   stopTimer();
			   notifyProgListenerEnd(_val, jmri.ProgListener.UnknownError);
			}
		} else if (progState == INQUIRESENT) {
			if (log.isDebugEnabled()) log.debug("reply in INQUIRESENT state");
            		// check for right message, else return
            		if (m.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE && 
                	    m.getElement(1)==XNetConstants.CS_SERVICE_REG_PAGE_RESPONSE) {
                	    // valid operation response, but does it belong to us?
			    if(m.getElement(2)!=_cv) return;
			    // see why waiting
			    if (_progRead) {
			        // read was in progress - get return value
				_val = m.getElement(3);
			    }
			    progState = NOTPROGRAMMING;
			    stopTimer();
			    // if this was a read, we cached the value earlier.  
			    // If its a write, we're to return the original write value
			    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                	    return;
            		} else if (m.getElement(0)==XNetConstants.CS_SERVICE_MODE_RESPONSE && 
                		   m.getElement(1)==XNetConstants.CS_SERVICE_DIRECT_RESPONSE) {
                	    // valid operation response, but does it belong to us?
			    if(m.getElement(2)!=_cv) return;
			    // see why waiting
			    if (_progRead) {
				// read was in progress - get return value
				_val = m.getElement(3);
			    }
			    progState = NOTPROGRAMMING;
			    stopTimer();
			    // if this was a read, we cached the value earlier.  If its a
			    // write, we're to return the original write value
			    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                	    return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
				   m.getElement(1)==XNetConstants.PROG_BYTE_NOT_FOUND) {
                    	   // "data byte not found", e.g. no reply
		    	   progState = NOTPROGRAMMING;
		    	   stopTimer();
		     	   notifyProgListenerEnd(_val, jmri.ProgListener.NoLocoDetected);
               	    	   return;
            		} else if (m.getElement(0)==XNetConstants.CS_INFO && 
				   m.getElement(1)==XNetConstants.BC_NORMAL_OPERATIONS) {
		  	   // We Exited Programming Mode early
		   	   log.error("Service Mode exited before sequence complete.");
		   	   progState = NOTPROGRAMMING;
		   	   stopTimer();
		   	   notifyProgListenerEnd(_val, jmri.ProgListener.UnknownError);
               	    	   return;
			} else {
                           // nothing important, ignore
                           return;
		   	}
	    } else {
		if (log.isDebugEnabled()) log.debug("reply in un-decoded state");
	    }
	}

	/*
         * Since the Lenz programming sequence requires several 
         * operations, We want to be able to check and see if we are 
         * currently programming before allowing the Traffic Controller 
         * to send a request to exit service mode
	 */
	public boolean programmerBusy() {
	    return (progState!=NOTPROGRAMMING);
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
