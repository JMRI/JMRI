// MrcProgrammer.java
package jmri.jmrix.mrc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert the jmri.Programmer interface into commands for the MRC power house.
 * <P>
 * This has two states: NOTPROGRAMMING, and COMMANDSENT. The transitions to and
 * from programming mode are now handled in the TrafficController code.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Ken Cameron Copyright (C) 2014
 * @author Kevin Dickerson Copyright (C) 2014
 * @version $Revision: 24290 $
 */
public class MrcProgrammer extends AbstractProgrammer implements MrcTrafficListener {

    protected MrcTrafficController tc;

    public MrcProgrammer(MrcTrafficController tc) {
        this.tc = tc;
        super.SHORT_TIMEOUT = 15000;
        super.LONG_TIMEOUT = 700000;
    }

    int PACKET_TIMEOUT = 5000;
    int PACKET_READTIMEOUT = 650000;

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(DefaultProgrammerManager.PAGEMODE);
        ret.add(DefaultProgrammerManager.REGISTERMODE);
        return ret;
    }

    @Override
    public boolean getCanRead() {
        return true;
    }

    @Override
    public boolean getCanWrite() {
        return true;
    }

    @Override
    /**
     * CV1 to 1024 valid
     */
    public boolean getCanWrite(String cv) {
        if (Integer.parseInt(cv) > 1024) {
            return false;
        }
        return true;
    }

    // members for handling the programmer interface
    int progState = 0;
    static final int NOTPROGRAMMING = 0;// is notProgramming
    static final int READCOMMANDSENT = 2; 	// read command sent, waiting reply
    static final int WRITECOMMANDSENT = 4; // POM write command sent 
    static final int POMCOMMANDSENT = 6;	// ops programming mode, send msg twice
    boolean _progRead = false;
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    public synchronized void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        log.debug("writeCV {} listens {}", CV, p); //IN18N
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
        log.debug("readCV {} listens {}", CV, p); //IN18N
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
            if (log.isInfoEnabled()) {
                log.info("programmer already in use by " + _usingProgrammer); //IN18N
            }
            throw new jmri.ProgrammerException("programmer in use"); //IN18N
        } else {
            _usingProgrammer = p;
            return;
        }
    }

    // internal method to create the MrcMessage for programmer task start
    /* todo MRC doesn't set the prog mode the command station sorts it out.*/
    protected MrcMessage progTaskStart(ProgrammingMode mode, int val, int cvnum) throws jmri.ProgrammerException {
        // val = -1 for read command; mode is direct, etc
        MrcMessage m;
        if (val < 0) {
            // read

            m = MrcMessage.getReadCV(cvnum);
        } else {
            m = MrcMessage.getWriteCV(cvnum, val);
        }
        m.setTimeout(PACKET_TIMEOUT);
        m.setSource(this);
        return m;
    }

    public synchronized void notifyXmit(Date timestamp, MrcMessage m) {
    }

    public synchronized void notifyFailedXmit(Date timestamp, MrcMessage m) {
        if (progState == NOTPROGRAMMING && m.getMessageClass() != MrcInterface.PROGRAMMING) {
            return;
        }
        timeout();
    }

    public synchronized void notifyRcv(Date timestamp, MrcMessage m) {
        //public synchronized void message(MrcMessage m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            log.debug("reply in NOTPROGRAMMING state"); //IN18N
            return;
        }
        if (m.getMessageClass() != MrcInterface.PROGRAMMING) {
            return;
        }
        if (MrcPackets.startsWith(m, MrcPackets.PROGCMDSENT)) {
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
            log.debug("Has value " + _val); //IN18N
            notifyProgListenerEnd(_val, jmri.ProgListener.OK);

        } else {
            log.debug("reply in un-decoded state cv:" + _cv + " " + m.toString()); //IN18N
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
        log.debug("notifyProgListenerEnd value {} status {}", value, status); //IN18N
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        tc.removeTrafficListener(MrcInterface.PROGRAMMING, this);
        jmri.ProgListener temp = _usingProgrammer;
        _usingProgrammer = null;
        temp.programmingOpReply(value, status);
    }

    private final static Logger log = LoggerFactory.getLogger(MrcProgrammer.class.getName());
}
/* @(#)MrcProgrammer.java */
