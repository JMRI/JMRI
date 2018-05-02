package jmri.jmrix.zimo;

import java.util.ArrayList;
import java.util.List;
import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer support for Zimo Mx-1. Currently paged mode is implemented.
 * <P>
 * The read operation state sequence is:
 * <UL>
 * <LI>Reset Mx-1
 * <LI>Send paged mode read/write request
 * <LI>Wait for results reply, interpret
 * <LI>Send Resume Operations request
 * <LI>Wait for Normal Operations Resumed broadcast
 * </UL>
 *
 * @author Bob Jacobsen Copyright (c) 2002
 *
 * Adapted by Sip Bosch for use with zimo Mx-1
 *
 */
public class Mx1Programmer extends AbstractProgrammer implements Mx1Listener {

    protected Mx1TrafficController tc;

    protected Mx1Programmer(Mx1TrafficController _tc) {
        this.tc = _tc;
        SHORT_TIMEOUT = 4000; // length default timeout
        // connect to listen
        log.info("" + this.tc);
        if(this.tc!=null)
            this.tc.addMx1Listener(~0, this);
    }

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(ProgrammingMode.PAGEMODE);
        return ret;
    }

    // members for handling the programmer interface
    int progState = 0;
    boolean firstTime = true;
    static final int NOTPROGRAMMING = 0; // is notProgramming
    static final int INQUIRESENT = 2; // read/write command sent, waiting reply
    boolean _progRead = false;
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    // programming interface
    @Override
    synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("writeCV " + CV + " listens " + p);
        }
        useProgrammer(p);
        _progRead = false;
        // set new state & save values
        progState = INQUIRESENT;
        _val = val;
        _cv = CV;
        // start the error timer
        startShortTimer();
        // format and send message to go to program mode
        if (getMode() == ProgrammingMode.PAGEMODE) {
            if (tc.getProtocol() == Mx1Packetizer.ASCII) {
                if (firstTime) {
                    tc.sendMx1Message(tc.getCommandStation().resetModeMsg(), this);
                    firstTime = false;
                }
                tc.sendMx1Message(tc.getCommandStation().getWritePagedCVMsg(CV, val), this);
            } else {
                tc.sendMx1Message(Mx1Message.getDecProgCmd(0, _cv, val, true), this);
            }
        }
    }

    @Override
    public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    @Override
    synchronized public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("readCV " + CV + " listens " + p);
        }
        useProgrammer(p);
        _progRead = true;
        // set new state
        progState = INQUIRESENT;
        _cv = CV;
        // start the error timer
        startShortTimer();
        // format and send message to go to program mode
        if (getMode() == ProgrammingMode.PAGEMODE) {
            if (tc.getProtocol() == Mx1Packetizer.ASCII) {
                if (firstTime) {
                    tc.sendMx1Message(tc.getCommandStation().resetModeMsg(), this);
                    firstTime = false;
                }
                tc.sendMx1Message(tc.getCommandStation().getReadPagedCVMsg(CV), this);
            } else {
                tc.sendMx1Message(Mx1Message.getDecProgCmd(0, _cv, -1, true), this);
            }
        }
    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            if (log.isInfoEnabled()) {
                log.info("programmer already in use by " + _usingProgrammer);
            }
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
            return;
        }
    }

    @Override
    synchronized public void message(Mx1Message m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            return;
        } else if (progState == INQUIRESENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in INQUIRESENT state");
            }
            if (tc.getProtocol() == Mx1Packetizer.ASCII) {
                //check for right message, else return
                if (m.getElement(0) == 0x51 && m.getElement(1) == 0x4E
                        && m.getElement(2) == 0x30 && m.getElement(3) == 0x30) {
                    // valid operation response
                    // see why waiting
                    if (_progRead) {
                        // read was in progress - get return value
                        // convert asci into ebcdic
                        int highVal = ascToBcd(m.getElement(6));
                        highVal = highVal * 16 & 0xF0;
                        int lowVal = ascToBcd(m.getElement(7));
                        _val = (highVal | lowVal);
                    }
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    // if this was a read, we cached the value earlier.  If its a
                    // write, we're to return the original write value
                    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                    tc.sendMx1Message(tc.getCommandStation().resetModeMsg(), this);
                    return;
                    // faulty message
                } else {
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    tc.sendMx1Message(tc
                            .getCommandStation().resetModeMsg(), this);
                    notifyProgListenerEnd(_val, jmri.ProgListener.NoLocoDetected);
                    return;
                }
            } else {
                if (m.getPrimaryMessage() == Mx1Message.PROGCMD && m.getMessageType() == Mx1Message.REPLY2) {
                    if (_progRead) {
                        _val = m.getCvValue();
                    }
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    // if this was a read, we cached the value earlier.  If its a
                    // write, we're to return the original write value
                    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                    /*tc.sendMx1Message(tc.getCommandStation().resetModeMsg(), this);*/
                    return;
                }
            }
        }
    }

    /**
     * Internal routine to handle a timeout
     */
    @Override
    synchronized protected void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            if (log.isDebugEnabled()) {
                log.debug("timeout!");
            }
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            if (tc.getProtocol() == Mx1Packetizer.ASCII) {
                tc.sendMx1Message(tc.getCommandStation().resetModeMsg(),
                        this);
            }
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    // internal method to notify of the final result
    protected void notifyProgListenerEnd(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value " + value + " status " + status);
        }
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        jmri.ProgListener temp = _usingProgrammer;
        _usingProgrammer = null;
        temp.programmingOpReply(value, status);
    }

    public int ascToBcd(int hex) {
        switch (hex) {
            case 0x46:
                return 0x0F;
            case 0x45:
                return 0x0E;
            case 0x65:
                return 0x0E;
            case 0x44:
                return 0x0D;
            case 0x43:
                return 0x0C;
            case 0x42:
                return 0x0B;
            case 0x41:
                return 0x0A;
            case 0x39:
                return 0x09;
            case 0x38:
                return 0x08;
            case 0x37:
                return 0x07;
            case 0x36:
                return 0x06;
            case 0x35:
                return 0x05;
            case 0x34:
                return 0x04;
            case 0x33:
                return 0x03;
            case 0x32:
                return 0x02;
            case 0x31:
                return 0x01;
            default:
                return 0x00;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(Mx1Programmer.class);

}
