package jmri.jmrix.can.cbus;

import java.util.ArrayList;
import java.util.List;
import jmri.AddressedProgrammer;
import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.TrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements the jmri.Programmer interface via commands for CBUS.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class CbusProgrammer extends AbstractProgrammer implements CanListener, AddressedProgrammer {

    public CbusProgrammer(int nodenumber, TrafficController tc) {
        this.nodenumber = nodenumber;
        // need a longer LONG_TIMEOUT
        LONG_TIMEOUT = 180000;
        this.tc = tc;
        setMode(CBUSNODEVARMODE);
    }

    TrafficController tc;

    int nodenumber;

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(CBUSNODEVARMODE);
        return ret;
    }

    final static ProgrammingMode CBUSNODEVARMODE = new ProgrammingMode("CBUSNODEVARMODE", "CBUSNODEVARMODE");

    // members for handling the programmer interface
    int progState = 0;
    static final int NOTPROGRAMMING = 0;// is notProgramming
    static final int COMMANDSENT = 2;  // read/write command sent, waiting reply
    boolean programmerReadOperation = false;  // true reading, false if writing
    int operationValue;  // remember the value being read/written for confirmative reply
    int operationVariableNumber; // remember the variable number being read/written

    // programming interface
    @Override
    synchronized public void writeCV(int varnum, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("write " + varnum + " listens " + p);
        }
        useProgrammer(p);
        programmerReadOperation = false;
        // set state
        progState = NOTPROGRAMMING;  // no reply to write
        operationValue = val;
        operationVariableNumber = varnum;

        // format and send the write message.
        int[] frame = new int[]{0x96, (nodenumber / 256) & 0xFF, nodenumber & 0xFF, operationVariableNumber & 0xFF, operationValue & 0xFF};
        CanMessage m = new CanMessage(frame, tc.getCanid());
        tc.sendCanMessage(m, this);

        // no reply, so don't want for reply
        progState = NOTPROGRAMMING;
        notifyProgListenerEnd(operationValue, jmri.ProgListener.OK);
    }

    @Override
    synchronized public void confirmCV(String varnum, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(varnum, p);
    }

    @Override
    synchronized public void readCV(int varnum, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("readCV " + varnum + " listens " + p);
        }
        useProgrammer(p);
        programmerReadOperation = true;

        progState = COMMANDSENT;
        operationVariableNumber = varnum;

        // start the error timer
        startLongTimer();

        // format and send the read message.
        int[] frame = new int[]{0x71, (nodenumber / 256) & 0xFF, nodenumber & 0xFF, operationVariableNumber & 0xFF};
        CanMessage m = new CanMessage(frame, tc.getCanid());
        tc.sendCanMessage(m, this);
    }

    private jmri.ProgListener programmerUser = null;  // null if don't have one

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (programmerUser != null && programmerUser != p) {
            if (log.isDebugEnabled()) {
                log.debug("programmer already in use by " + programmerUser);
            }
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            programmerUser = p;
            return;
        }
    }

    @Override
    public void message(CanMessage m) {
        log.debug("message received and ignored: " + m.toString());
    }

    @Override
    synchronized public void reply(jmri.jmrix.can.CanReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            if (log.isDebugEnabled()) {
                log.debug("reply in NOTPROGRAMMING state");
            }
            return;
        } else if (progState == COMMANDSENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in COMMANDSENT state");
            }
            // operation done, capture result, then have to leave programming mode
            progState = NOTPROGRAMMING;
            // check for reply
            if (m.getElement(0) == 0x97
                    && (m.getElement(1) == ((nodenumber / 256) & 0xFF))
                    && (m.getElement(2) == (nodenumber & 0xFF))) {
                // this is the OK reply
                // see why waiting
                if (programmerReadOperation) {
                    // read was in progress - get return value
                    operationValue = m.getElement(3) & 0xFF;
                }
                // if this was a read, we retrieved the value above.  If its a
                // write, we're to return the original write value
                notifyProgListenerEnd(operationValue, jmri.ProgListener.OK);
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
            cleanup();
            notifyProgListenerEnd(operationValue, jmri.ProgListener.FailedTimeout);
        }
    }

    @Override
    public boolean getLongAddress() {
        return false;
    }

    @Override
    public int getAddressNumber() {
        return nodenumber;
    }

    @Override
    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }

    /**
     * Internal method to send a cleanup message (if needed) on timeout.
     * <P>
     * Here, it sends a request to exit from programming mode. But subclasses,
     * e.g. ops mode, may redefine that.
     */
    void cleanup() {
        // tc.sendEasyDccMessage(EasyDccMessage.getExitProgMode(), this);
    }

    // internal method to notify of the final result
    protected void notifyProgListenerEnd(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("notifyProgListenerEnd value " + value + " status " + status);
        }
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        jmri.ProgListener temp = programmerUser;
        programmerUser = null;
        temp.programmingOpReply(value, status);
    }

    private final static Logger log = LoggerFactory.getLogger(CbusProgrammer.class);
}
