/**
 * Mx1Programmer.java
 */

 // Convert the jmri.Programmer interface into commands for the MX-1

package jmri.jmrix.zimo;

import org.apache.log4j.Logger;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;
import java.util.Vector;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * Programmer support for Zimo Mx-1.
 * Currently paged mode is implemented.
 * <P>
 * The read operation state sequence is:
 * <UL>
 * <LI>Reset Mx-1
 * <LI>Send paged mode read/write request
  *<LI>Wait for results reply, interpret
 * <LI>Send Resume Operations request
 * <LI>Wait for Normal Operations Resumed broadcast
 * </UL>
 * @author Bob Jacobsen  Copyright (c) 2002
 * @version $Revision$
 *
 * Adapted by Sip Bosch for use with zimo Mx-1
 *
 */
public class Mx1Programmer extends AbstractProgrammer implements Mx1Listener {

	protected Mx1Programmer() {
		// error if more than one constructed?
		if (self != null)
			log.error("Creating too many Mx1Programmer objects");
		// register this as the default, register as the Programmer

        // connect to listen
        controller().addMx1Listener(~0, this);
        }

	/*
	 * method to find the existing Mx1Programmer object, if need be creating one
	 */
	static public final Mx1Programmer instance() {
		if (self == null) self = new Mx1Programmer();
		return self;
		}
	static volatile private Mx1Programmer self = null;  // needs to be accessible from tests

	// handle mode
	protected int _mode = Programmer.PAGEMODE;

    /**
     * Note that zimo can now only do page mode. If you attempt to switch to
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
		if (_mode != Programmer.PAGEMODE) {
                // attempt to switch to unsupported mode, switch back to previous
			_mode = oldMode;
			notifyPropertyChange("Mode", mode, _mode);
		}
	}
	public int getMode() { return _mode; }
    /**
     * Signifies mode's available
     * @param mode
     * @return True if paged mode
     */
    public boolean hasMode(int mode) {
        if ( mode == Programmer.PAGEMODE) {
            log.debug("hasMode request on mode "+mode+" returns true");
            return true;
        }
        log.debug("hasMode returns false on mode "+mode);
        return false;
    }

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
        boolean firstTime = true;
		static final int NOTPROGRAMMING = 0; // is notProgramming
		static final int INQUIRESENT    = 2; // read/write command sent, waiting reply
		boolean  _progRead = false;
	int _val;	// remember the value being read/written for confirmative reply
	int _cv;	// remember the cv being read/written

	// programming interface
	synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
		if (log.isDebugEnabled()) log.debug("writeCV "+CV+" listens "+p);
		useProgrammer(p);
		_progRead = false;
		// set new state & save values
		progState = INQUIRESENT;
                _val = val;
		_cv = CV;
		// start the error timer
		startShortTimer();
                // format and send message to go to program mode
                if (_mode == Programmer.PAGEMODE){
                  if (firstTime){
                   controller().sendMx1Message(Mx1TrafficController.instance()
                       .getCommandStation().resetModeMsg(), this);
                   firstTime = false;
                  }
                   controller().sendMx1Message(Mx1TrafficController.instance()
                       .getCommandStation().getWritePagedCVMsg(CV, val), this);
                }
	}

	public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
		readCV(CV, p);
	}

	synchronized public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
          	if (log.isDebugEnabled()) log.debug("readCV "+CV+" listens "+p);
		useProgrammer(p);
		_progRead = true;
		// set new state
		progState = INQUIRESENT;
                _cv = CV;
                // start the error timer
		startShortTimer();
                // format and send message to go to program mode
                if (_mode == Programmer.PAGEMODE){
                  if (firstTime){
                    controller().sendMx1Message(Mx1TrafficController.instance()
                        .getCommandStation().resetModeMsg(), this);
                    firstTime = false;
                  }
                  controller().sendMx1Message(Mx1TrafficController.instance()
                      .getCommandStation().getReadPagedCVMsg(CV), this);
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

	synchronized public void message(Mx1Message m) {
                if (progState == NOTPROGRAMMING) {
                  // we get the complete set of replies now, so ignore these
                  return;
		}
                else if (progState == INQUIRESENT) {
                  if (log.isDebugEnabled()) log.debug("reply in INQUIRESENT state");
                  //check for right message, else return
                  if (m.getElement(0)==0x51 && m.getElement(1)==0x4E
                      && m.getElement(2)==0x30 && m.getElement(3)==0x30){
                    // valid operation response
                    // see why waiting
                    if (_progRead) {
                      // read was in progress - get return value
                      // convert asci into ebcdic
                      int highVal = ascToBcd(m.getElement(6));
                      highVal = highVal*16&0xF0;
                      int lowVal = ascToBcd(m.getElement(7));
                      _val = (highVal | lowVal);
                    }
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    // if this was a read, we cached the value earlier.  If its a
                    // write, we're to return the original write value
                    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                    controller().sendMx1Message(Mx1TrafficController.instance()
                        .getCommandStation().resetModeMsg(), this);
                    return;
                  // faulty message
		} else {
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    controller().sendMx1Message(Mx1TrafficController.instance()
                        .getCommandStation().resetModeMsg(), this);
                    notifyProgListenerEnd(_val, jmri.ProgListener.NoLocoDetected);
                    return;
                    }
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
			controller().sendMx1Message(Mx1TrafficController.instance()
                                    .getCommandStation().resetModeMsg(),
                                    this);
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

	Mx1TrafficController _controller = null;

	protected Mx1TrafficController controller() {
		// connect the first time
		if (_controller == null) {
			_controller = Mx1TrafficController.instance();
		}
		return _controller;
	}

        public int ascToBcd(int hex) {
                switch (hex) {
                  case 0x46: return 0x0F;
                  case 0x45: return 0x0E;
                  case 0x65: return 0x0E;
                  case 0x44: return 0x0D;
                  case 0x43: return 0x0C;
                  case 0x42: return 0x0B;
                  case 0x41: return 0x0A;
                  case 0x39: return 0x09;
                  case 0x38: return 0x08;
                  case 0x37: return 0x07;
                  case 0x36: return 0x06;
                  case 0x35: return 0x05;
                  case 0x34: return 0x04;
                  case 0x33: return 0x03;
                  case 0x32: return 0x02;
                  case 0x31: return 0x01;
                  default: return 0x00;
            }
        }

   static Logger log = Logger.getLogger(Mx1Programmer.class.getName());

}


/* @(#)Mx1Programmer.java */

