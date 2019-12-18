package jmri.jmrix.lenz.hornbyelite;

import jmri.ProgrammingMode;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetProgrammer;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTrafficController;

/**
 * Programmer support for Hornby Elite implementationn of XpressNet.
 * <p>
 * The read operation state sequence is:
 * <ul>
 * <li>Send Register Mode / Paged mode /Direct Mode read request
 * <li>Wait for Broadcast Service Mode Entry message -- not happening on elite
 * <li>Send Request for Service Mode Results request
 * <li>Wait for results reply, interpret
 * <li>Send Resume Operations request -- The Elite does not seem to require this
 * step.
 * <li>Wait for Normal Operations Resumed broadcast -- The Elite does not seem
 * to require this step.
 * </ul>
 *
 * @author Paul Bender Copyright (c) 2008
 */
public class EliteXNetProgrammer extends XNetProgrammer {

    // Message timeout lengths.  These have been determined by
    // experimentation, and may need to be adjusted
    static private final int ELITEMESSAGETIMEOUT = 10000;
    static private final int EliteXNetProgrammerTimeout = 20000;

    public EliteXNetProgrammer(XNetTrafficController tc) {
        super(tc);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void writeCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("writeCV {} listens {}", CV, p);
        useProgrammer(p);
        _progRead = false;
        // set new state & save values
        progState = REQUESTSENT;
        _val = val;
        _cv = 0xffff & CV;

        try {
            // start the error timer
            restartTimer(EliteXNetProgrammerTimeout);

            // format and send message to go to program mode
            if (getMode().equals(ProgrammingMode.PAGEMODE)) {
                XNetMessage msg = XNetMessage.getWritePagedCVMsg(CV, val);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                msg.setTimeout(ELITEMESSAGETIMEOUT);
                controller().sendXNetMessage(msg, this);
            } else if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
                XNetMessage msg = XNetMessage.getWriteDirectCVMsg(CV, val);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                msg.setTimeout(ELITEMESSAGETIMEOUT);
                controller().sendXNetMessage(msg, this);
            } else { // register mode by elimination
                XNetMessage msg = XNetMessage.getWriteRegisterMsg(registerFromCV(CV), val);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                msg.setTimeout(ELITEMESSAGETIMEOUT);
                controller().sendXNetMessage(msg, this);
            }
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }

    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("readCV {} listens {}", CV, p);

        if (!getCanRead()) {
            // should not invoke this if cant read, but if done anyway set NotImplemented error
            notifyProgListenerEnd(p,CV,jmri.ProgListener.NotImplemented);
            return;
        }

        useProgrammer(p);
        _progRead = true;
        // set new state
        progState = REQUESTSENT;
        _cv = 0xffff & CV;
        try {
            // start the error timer
            restartTimer(EliteXNetProgrammerTimeout);

            // format and send message to go to program mode
            if (getMode().equals(ProgrammingMode.PAGEMODE)) {
                XNetMessage msg = XNetMessage.getReadPagedCVMsg(CV);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                msg.setTimeout(ELITEMESSAGETIMEOUT);
                controller().sendXNetMessage(msg, this);
            } else if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
                XNetMessage msg = XNetMessage.getReadDirectCVMsg(CV);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                msg.setTimeout(ELITEMESSAGETIMEOUT);
                controller().sendXNetMessage(msg, this);
            } else { // register mode by elimination
                XNetMessage msg = XNetMessage.getReadRegisterMsg(registerFromCV(CV));
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                msg.setTimeout(ELITEMESSAGETIMEOUT);
                controller().sendXNetMessage(msg, this);
            }
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }

    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void message(XNetReply m) {
        if (m.getElement(0) == XNetConstants.CS_INFO
                && m.getElement(1) == XNetConstants.BC_SERVICE_MODE_ENTRY) {
            if (_service_mode == false) {
                // the command station is in service mode.  An "OK"
                // message can trigger a request for service mode
                // results if progrstate is REQUESTSENT.
                _service_mode = true;
            } else {  // _service_mode == true
                // Since we get this message as both a broadcast and
                // a directed message, ignore the message if we're
                //already in the indicated mode
                return;
            }
        }
        if (m.getElement(0) == XNetConstants.CS_INFO
                && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
            if (_service_mode == true) {
                // the command station is not in service mode.  An
                // "OK" message can not trigger a request for service
                // mode results if progrstate is REQUESTSENT.
                _service_mode = false;
            } else { // _service_mode == false
                // Since we get this message as both a broadcast and
                // a directed message, ignore the message if we're
                //already in the indicated mode
                return;
            }
        }

        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            return;

        } else if (progState == REQUESTSENT) {
            log.debug("reply in REQUESTSENT state");
            // see if reply is the acknowledge of program mode; if not, wait for next
            if ((_service_mode && m.isOkMessage())
                    || (m.getElement(0) == XNetConstants.CS_INFO
                    && (m.getElement(1) == XNetConstants.BC_SERVICE_MODE_ENTRY
                    || m.getElement(1) == XNetConstants.PROG_CS_READY))) {
                stopTimer();

                if (!getCanRead()) {
                    // should not read here if cant read, because read shouldnt be invoked, but still attempt to handle
                    log.debug("CV reading not supported, exiting REQUESTSENT state");
                    stopTimer();
                    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                    return;
                }
                return;
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.CS_NOT_SUPPORTED) {
                // programming operation not supported by this command station
                progState = NOTPROGRAMMING;
                notifyProgListenerEnd(_val, jmri.ProgListener.NotImplemented);
                return;
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
                    // On the Elite, the broadcast exit service mode message
                    // needs to triger the request for results.
                    progState = INQUIRESENT;
                    //start the error timer
                    restartTimer(EliteXNetProgrammerTimeout);
                    XNetMessage resultMsg = XNetMessage.getServiceModeResultsMsg();
                    resultMsg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                    resultMsg.setTimeout(ELITEMESSAGETIMEOUT);
                    controller().sendXNetMessage(resultMsg, this);
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.PROG_SHORT_CIRCUIT) {
                // We experienced a short Circuit on the Programming Track
                log.error("Short Circuit While Programming Decoder");
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.ProgrammingShort);
            } else if (m.isCommErrorMessage()) {
                // We experienced a communicatiosn error
                // If this is a Timeslot error, ignore it,
                //otherwise report it as an error
                if (m.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR) {
                    return;
                }
                log.error("Communications error in REQUESTSENT state while programming.  Error: " + m.toString());
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.CommError);
            }
        } else if (progState == INQUIRESENT) {
            log.debug("reply in INQUIRESENT state");
            // check for right message, else return
            if (m.isPagedModeResponse()) {
                // valid operation response, but does it belong to us?
                try {
                    // we always save the cv number, but if
                    // we are using register mode, there is
                    // at least one case (CV29) where the value
                    // returned does not match the value we saved.
                    if (m.getServiceModeCVNumber() != _cv
                            && m.getServiceModeCVNumber() != registerFromCV(_cv)) {
                        log.debug(" result for CV {} expecting {}", m.getServiceModeCVNumber(), _cv);
                        return;
                    }
                } catch (jmri.ProgrammerException e) {
                    progState = NOTPROGRAMMING;
                    notifyProgListenerEnd(_val, jmri.ProgListener.UnknownError);
                }

                // see why waiting
                if (_progRead) {
                    // read was in progress - get return value
                    _val = m.getServiceModeCVValue();
                }
                progState = NOTPROGRAMMING;
                stopTimer();
                // if this was a read, we cached the value earlier.
                // If its a write, we're to return the original write value
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                return;
            } else if (m.isDirectModeResponse()) {
                // valid operation response, but does it belong to us?
                if (m.getServiceModeCVNumber() != _cv) {
                    log.debug(" result for CV {} expecting {}", m.getServiceModeCVNumber(), _cv);
                    return;
                }
                // see why waiting
                if (_progRead) {
                    // read was in progress - get return value
                    _val = m.getServiceModeCVValue();
                }
                progState = NOTPROGRAMMING;
                stopTimer();
                // if this was a read, we cached the value earlier.  If its a
                // write, we're to return the original write value
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                return;
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.PROG_BYTE_NOT_FOUND) {
                // "data byte not found", e.g. no reply
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.NoLocoDetected);
                return;
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.PROG_SHORT_CIRCUIT) {
                // We experienced a short Circuit on the Programming Track
                log.error("Short Circuit While Programming Decoder");
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.ProgrammingShort);
            } else if (m.isCommErrorMessage()) {
                // We experienced a communicatiosn error
                // If this is a Timeslot error, ignore it,
                //otherwise report it as an error
                if (m.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR) {
                    return;
                }
                log.error("Communications error in INQUIRESENT state while programming.  Error: " + m.toString());
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.CommError);
            } else {
                // nothing important, ignore
                return;
            }
        } else {
            log.debug("reply in un-decoded state");
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void message(XNetMessage l) {
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EliteXNetProgrammer.class);

}
