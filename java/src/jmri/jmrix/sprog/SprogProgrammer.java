// SprogProgrammer.java

package jmri.jmrix.sprog;

import org.apache.log4j.Logger;
import jmri.PowerManager;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;

/**
 * Implements the jmri.Programmer interface via commands for the Sprog programmer.
 *
 * @author      Bob Jacobsen  Copyright (C) 2001
 * @version	$Revision$
 */
public class SprogProgrammer extends AbstractProgrammer implements SprogListener {

    public SprogProgrammer() {
//        // error if more than one constructed?
//        if (self != null)
//            log.error("Creating too many SprogProgrammer objects");

//        // register this as the default, register as the Programmer
//        self = this;
//        jmri.InstanceManager.setProgrammerManager(new jmri.DefaultProgrammerManager(this));

    }

    /*
     * method to find the existing SprogProgrammer object, if need be creating one
     */
    static public final SprogProgrammer instance() {
        if (self == null) self = new SprogProgrammer();
        // change default
        if (jmri.InstanceManager.programmerManagerInstance().isAddressedModePossible())
            _mode = Programmer.OPSBYTEMODE;
        return self;
    }
    static volatile private SprogProgrammer self = null;

    // handle mode
    static int _mode = Programmer.DIRECTBITMODE;

    /**
     * Switch to a new programming mode.  SPROG currently supports bit
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
        if (jmri.InstanceManager.programmerManagerInstance().isAddressedModePossible()){
            log.debug("hasMode request on mode "+mode+" returns false");
            return false;
        }else{
            if ( mode == Programmer.DIRECTBITMODE ||
                 mode == Programmer.PAGEMODE ) {
                log.debug("hasMode request on mode "+mode+" returns true");
                return true;
            }
            log.debug("hasMode request on mode "+mode+" returns false");
            return false;
        }
    }
    public int getMode() { return _mode; }

    public boolean getCanRead() {
        if (jmri.InstanceManager.programmerManagerInstance().isAddressedModePossible())
            return false;
        else 
            return true;
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
    static final int MODESENT = 1; 		// waiting reply to command to go into programming mode
    static final int COMMANDSENT = 2; 	// read/write command sent, waiting reply
    static final int RETURNSENT = 4; 	// waiting reply to go back to ops mode
    boolean  _progRead = false;
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
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
        // SPROG is in program mode by default but this doesn't do any harm
        controller().sendSprogMessage(SprogMessage.getProgMode(), this);
    }

    synchronized public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    synchronized public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
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
        // SPROG is in program mode by default but this doesn't do any harm
        controller().sendSprogMessage(SprogMessage.getProgMode(), this);

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

    // internal method to create the SprogMessage for programmer task start
    protected SprogMessage progTaskStart(int mode, int val, int cvnum) {
        // val = -1 for read command; mode is direct, etc
        if (val < 0) {
            return SprogMessage.getReadCV(cvnum, mode);
        } else {
            return SprogMessage.getWriteCV(cvnum, val, mode);
        }
    }

    public void notifyMessage(SprogMessage m) {}
    
    synchronized public void notifyReply(SprogReply reply) {
    	
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            if (log.isDebugEnabled()) log.debug("reply in NOTPROGRAMMING state");
            return;
        } else if (progState == MODESENT) {
            if (log.isDebugEnabled()) log.debug("reply in MODESENT state");
            // see if reply is the acknowledge of program mode; if not, wait
            if ( reply.match("P") == -1 ) return;
            // here ready to send the read/write command
            progState = COMMANDSENT;
            // see why waiting
            try {
                startLongTimer();
                if (_progRead) {
                    // read was in progress - send read command
                    controller().sendSprogMessage(progTaskStart(getMode(), -1, _cv), this);
                } else {
                    // write was in progress - send write command
                    controller().sendSprogMessage(progTaskStart(getMode(), _val, _cv), this);
                }
            } catch (Exception e) {
                // program op failed, go straight to end
                log.error("program operation failed, exception "+e);
                progState = RETURNSENT;
                controller().sendSprogMessage(SprogMessage.getExitProgMode(), this);
            }
        } else if (progState == COMMANDSENT) {
            if (log.isDebugEnabled()) log.debug("reply in COMMANDSENT state");
            // operation done, capture result, then have to leave programming mode
            progState = RETURNSENT;
            // check for errors
            if (reply.match("No Ack") >= 0) {
                if (log.isDebugEnabled()) log.debug("handle error reply "+reply);
                // perhaps no loco present? Fail back to end of programming
                progState = NOTPROGRAMMING;
                controller().sendSprogMessage(SprogMessage.getExitProgMode(), this);
                notifyProgListenerEnd(-1, jmri.ProgListener.NoLocoDetected);
            }
            else {
                // see why waiting
                if (_progRead) {
                    // read was in progress - get return value
                    _val = reply.value();
                }
                startShortTimer();
                controller().sendSprogMessage(SprogMessage.getExitProgMode(), this);
            }
            
            // SPROG always leaves power off after programming so we inform the
            // power manager of the new state
            //try {
                controller().getAdapterMemo().getPowerManager().notePowerState(PowerManager.OFF);
            //}
            //catch (JmriException e) {
            //    log.error("Exception trying to turn power off " +e);
            //}
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
            controller().sendSprogMessage(SprogMessage.getExitProgMode(), this);
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    // internal method to notify of the final result
    protected void notifyProgListenerEnd(int value, int status) {
        if (log.isDebugEnabled()) log.debug("notifyProgListenerEnd value "+value+" status "+status);
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        if (_usingProgrammer == null) {
            log.error("No listener to notify");
        } else {
            jmri.ProgListener temp = _usingProgrammer;
            _usingProgrammer = null;
            temp.programmingOpReply(value, status);
        }
    }

    SprogTrafficController _controller = null;

    protected SprogTrafficController controller() {
        // connect the first time
        if (_controller == null) {
            _controller = SprogTrafficController.instance();
        }
        return _controller;
    }

    static Logger log = Logger.getLogger(SprogProgrammer.class.getName());

}

/* @(#)SprogProgrammer.java */
