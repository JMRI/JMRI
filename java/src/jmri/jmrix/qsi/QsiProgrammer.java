package jmri.jmrix.qsi;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;

/**
 * Implements the jmri.Programmer interface via commands for the QSI programmer.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class QsiProgrammer extends AbstractProgrammer implements QsiListener {

    private QsiSystemConnectionMemo _memo = null;

    protected QsiProgrammer(QsiSystemConnectionMemo memo) {
        _memo = memo;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(ProgrammingMode.PAGEMODE);
        ret.add(ProgrammingMode.DIRECTBITMODE);
        return ret;
    }

    // members for handling the programmer interface
    int progState = 0;
    static final int NOTPROGRAMMING = 0;// is notProgramming
    static final int COMMANDSENT = 2; 	// read/write command sent, waiting ack
    static final int WAITRESULT = 4; 	// waiting reply with data
    static final int WAITRESETSTATUS = 6; 	// waiting reply from reseting status
    boolean _progRead = false;
    int _val;	// remember the value being read/written for confirmative reply
    int _cv;	// remember the cv being read/written

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void writeCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("writeCV {} listens {}", CV, p);
        useProgrammer(p);
        _progRead = false;
        // set commandPending state
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

        // start the error timer
        startShortTimer();

        // format and send message to do write
        controller().sendQsiMessage(QsiMessage.getWriteCV(CV, val, getMode()), this);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void readCV(String CVname, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("writeCV {} listens {}", CV, p);
        useProgrammer(p);
        _progRead = true;
        // set commandPending state
        progState = COMMANDSENT;
        _cv = CV;

        // start the error timer
        startShortTimer();

        // format and send message to do read
        // QSI programer is in program mode by default but this doesn't do any harm
        controller().sendQsiMessage(QsiMessage.getReadCV(CV, getMode()), this);
    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            log.info("programmer already in use by {}", _usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
            return;
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void message(QsiMessage m) {
        log.error("message received unexpectedly: {}", m);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void reply(QsiReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            log.debug("reply in NOTPROGRAMMING state");
            return;
        } else if (progState == COMMANDSENT) {
            log.debug("reply in COMMANDSENT state");
            // operation started, move to next mode
            progState = WAITRESULT;
            startLongTimer();
        } else if (progState == WAITRESULT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in WAITRESULT state");
            }
            stopTimer();
            // send QSI ack
            controller().sendQsiMessage(QsiReply.getAck(m), null);
            // operation done, capture result, then leave programming mode
            progState = NOTPROGRAMMING;
            // check for errors
            if (m.getElement(4) != 0) {
                // status present
                log.debug("handle non-zero status in reply {}", m);
                // perhaps no loco present? 
                // reset status
                progState = WAITRESETSTATUS;
                startShortTimer();
                controller().sendQsiMessage(QsiMessage.getClearStatus(), this);
            } else {
                // ended OK!
                if (_progRead == true) {
                    _val = m.value();
                }
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            }
        } else if (progState == WAITRESETSTATUS) {
            log.debug("reply in WAITRESETSTATUS state");
            // all done, notify listeners of completion
            progState = NOTPROGRAMMING;
            stopTimer();
            // notify of default error (not timeout)
            notifyProgListenerEnd(-1, jmri.ProgListener.NoLocoDetected);
        } else {
            log.debug("reply in un-decoded state");
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized protected void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            log.debug("timeout!");
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            // send message to clear error
            controller().sendQsiMessage(QsiMessage.getClearStatus(), null);
            // report timeout
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    // internal method to notify of the final result
    protected void notifyProgListenerEnd(int value, int status) {
        log.debug("notifyProgListenerEnd value {} status {}", value, status);
        // the programmingOpReply handler might send an immediate reply, so
        // clear the current listener _first_
        jmri.ProgListener temp = _usingProgrammer;
        _usingProgrammer = null;
        notifyProgListenerEnd(temp, value, status);
    }

    QsiTrafficController _controller = null;

    protected QsiTrafficController controller() {
        // connect the first time
        if (_controller == null) {
            _controller = _memo.getQsiTrafficController();
        }
        return _controller;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QsiProgrammer.class);

}
