// MrcProgrammer.java

package jmri.jmrix.mrc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import java.util.Date;
//import static jmri.jmrix.mrc.MrcReply.READCVHEADERREPLY;

/**
 * Convert the jmri.Programmer interface into commands for the MRC power house.
 * <P>
 * This has two states:  NOTPROGRAMMING, and COMMANDSENT.  The transitions
 * to and from programming mode are now handled in the TrafficController code.
 * @author			Bob Jacobsen Copyright (C) 2002
 * @author	Ken Cameron Copyright (C) 2014
 * @author  Kevin Dickerson Copyright (C) 2014
 * @version     $Revision: 24290 $
 */
public class MrcProgrammer extends AbstractProgrammer implements MrcTrafficListener {
	
    protected MrcTrafficController tc;

    public MrcProgrammer(MrcTrafficController tc) {
    	this.tc = tc;
        super.SHORT_TIMEOUT = 15000;
        super.LONG_TIMEOUT = 700000;
    }

    // handle mode
    protected int _mode = Programmer.PAGEMODE;
    int PACKET_TIMEOUT = 5000;
    int PACKET_READTIMEOUT = 650000;

    /**
     * Switch to a new programming mode.  Note that MRC 
     * doesn't support the setting of mode, the command station works it out.
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
        return true;
    }

    public int getMode() { return _mode; }

    @Override
    public boolean getCanRead() {
    	return true;
    }
    /*Need to check this out */
    public boolean getCanWrite(int mode, String cv) {
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
    static final int READCOMMANDSENT = 2; 	// read command sent, waiting reply
    static final int WRITECOMMANDSENT = 4; // POM write command sent 
    static final int POMCOMMANDSENT = 6;	// ops programming mode, send msg twice
    boolean  _progRead = false;
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    public synchronized void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("writeCV {} listens {}", CV, p);
        useProgrammer(p);
        _progRead = false;
        // set state
        progState = WRITECOMMANDSENT;
        _val = val;
        _cv = CV;

        try {
            // start the error timer
            startShortTimer();//we get no confirmation back that the packet has been read.
            // format and send the write message
            tc.addTrafficListener(MrcInterface.PROGRAMMING, this);
            tc.sendMrcMessage(progTaskStart(getMode(), _val, _cv));
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }
    }

    public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    public synchronized void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("readCV {} listens {}", CV, p);
        useProgrammer(p);
        _progRead = true;

        // set commandPending state
        progState = READCOMMANDSENT;
        _cv = CV;

        try {
            // start the error timer
            startLongTimer();
            
            // format and send the write message
            tc.addTrafficListener(MrcInterface.PROGRAMMING, this);
            tc.sendMrcMessage(progTaskStart(getMode(), -1, _cv));
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

    // internal method to create the MrcMessage for programmer task start
    /* todo MRC doesn't set the prog mode the command station sorts it out.*/
    protected MrcMessage progTaskStart(int mode, int val, int cvnum) throws jmri.ProgrammerException {
        // val = -1 for read command; mode is direct, etc
        MrcMessage m;
        if (val < 0) {
            // read
            
            m = MrcMessage.getReadCV(cvnum);
        } else {
            m = MrcMessage.getWriteCV((byte)cvnum, (byte)val);
        }
        m.setTimeout(PACKET_TIMEOUT);
        m.setSource(this);
        return m;
    }
    
    public synchronized void notifyXmit(Date timestamp, MrcMessage m){ }
    public synchronized void notifyFailedXmit(Date timestamp, MrcMessage m){
        if(progState == NOTPROGRAMMING && m.getMessageClass()!=MrcInterface.PROGRAMMING){
            return;
        }
        timeout();
    }

    public synchronized void notifyRcv(Date timestamp, MrcMessage m){
    //public synchronized void message(MrcMessage m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            log.debug("reply in NOTPROGRAMMING state");
            return;
        }
        if(m.getMessageClass()!=MrcInterface.PROGRAMMING){
            return;
        }
        if (MrcPackets.startsWith(m, MrcPackets.PROGCMDSENT)){
            progState = NOTPROGRAMMING;
            notifyProgListenerEnd(_val, jmri.ProgListener.OK);
        } else if (MrcPackets.startsWith(m, MrcPackets.READCVHEADERREPLY) && progState == READCOMMANDSENT) {
            progState = NOTPROGRAMMING;
            //Currently we have no way to know if the write was sucessful or not.
            if (_progRead) {
                log.debug("prog Read " + _cv);
                // read was in progress - get return value
                _val = m.value();
            }
            // if this was a read, we retrieved the value above.  If its a
            // write, we're to return the original write value
            log.debug("Has value " + _val);
            notifyProgListenerEnd(_val, jmri.ProgListener.OK);
        
        } else {
            log.debug("reply in un-decoded state cv:" + _cv + " " + m.toString());
        }
    }

    /**
     * Internal routine to handle a timeout
     */
    protected synchronized void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            log.debug("timeout!" + _cv);
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
        log.debug("notifyProgListenerEnd value {} status {}", value, status);
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        tc.removeTrafficListener(MrcInterface.PROGRAMMING, this);
        jmri.ProgListener temp = _usingProgrammer;
        _usingProgrammer = null;
        temp.programmingOpReply(value, status);
    }

    static Logger log = LoggerFactory.getLogger(MrcProgrammer.class.getName());

}


/* @(#)MrcProgrammer.java */

