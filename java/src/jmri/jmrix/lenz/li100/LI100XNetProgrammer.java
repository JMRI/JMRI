package jmri.jmrix.lenz.li100;

import jmri.ProgrammingMode;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetProgrammer;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer support for Lenz XpressNet.
 * <p>
 * The read operation state sequence is:
 * <ul>
 * <li>Send Register Mode / Paged mode /Direct Mode read request
 * <li>Wait for Broadcast Service Mode Entry message
 * <li>Send Request for Service Mode Results request
 * <li>Wait for results reply, interpret
 * <li>Send Resume Operations request
 * <li>Wait for Normal Operations Resumed broadcast
 * </ul>
 *
 * @author Bob Jacobsen Copyright (c) 2002, 2007
 * @author Paul Bender Copyright (c) 2003, 2004, 2005, 2009
 * @author Giorgio Terdina Copyright (c) 2007
 */
public class LI100XNetProgrammer extends XNetProgrammer {

    static private final int RETURNSENT = 3;

    // save the last XpressNet message for retransmission after a
    // communication error..
    private XNetMessage lastRequestMessage = null;

    private int _error = 0;

    public LI100XNetProgrammer(XNetTrafficController tc) {
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
            restartTimer(XNetProgrammerTimeout);

            // format and send message to go to program mode
            if (getMode().equals(ProgrammingMode.PAGEMODE)) {
                XNetMessage msg = XNetMessage.getWritePagedCVMsg(CV, val);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                lastRequestMessage = new XNetMessage(msg);
                controller().sendXNetMessage(msg, this);
            } else if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
                XNetMessage msg = XNetMessage.getWriteDirectCVMsg(CV, val);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                lastRequestMessage = new XNetMessage(msg);
                controller().sendXNetMessage(msg, this);
            } else { // register mode by elimination
                XNetMessage msg = XNetMessage.getWriteRegisterMsg(registerFromCV(CV), val);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                lastRequestMessage = new XNetMessage(msg);
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
            restartTimer(XNetProgrammerTimeout);

            // format and send message to go to program mode
            if (getMode() == ProgrammingMode.PAGEMODE) {
                XNetMessage msg = XNetMessage.getReadPagedCVMsg(CV);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                lastRequestMessage = new XNetMessage(msg);
                controller().sendXNetMessage(msg, this);
            } else if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
                XNetMessage msg = XNetMessage.getReadDirectCVMsg(CV);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                lastRequestMessage = new XNetMessage(msg);
                controller().sendXNetMessage(msg, this);
            } else { // register mode by elimination
                XNetMessage msg = XNetMessage.getReadRegisterMsg(registerFromCV(CV));
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                lastRequestMessage = new XNetMessage(msg);
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
                log.debug("change _service_mode to true");
                _service_mode = true;
            } else { // _service_mode == true
                // Since we get this message as both a broadcast and
                // a directed message, ignore the message if we're
                //already in the indicated mode
                log.debug("_service_mode already true");
                return;
            }
        }
        if (m.getElement(0) == XNetConstants.CS_INFO
                && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
            if (_service_mode == true) {
                // the command station is not in service mode.  An
                // "OK" message can not trigger a request for service
                // mode results if progrstate is REQUESTSENT.
                log.debug("change _service_mode to false");
                _service_mode = false;
            } else { // _service_mode == false
                // Since we get this message as both a broadcast and
                // a directed message, ignore the message if we're
                //already in the indicated mode
                log.debug("_service_mode already false");
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
                    // on systems like the Roco MultiMaus
                    // (which does not support reading)
                    // let a timeout occur so the system
                    // has time to write data to the
                    // decoder
                    restartTimer(SHORT_TIMEOUT);
                    return;
                }

                // here ready to request the results
                progState = INQUIRESENT;
                //start the error timer
                restartTimer(XNetProgrammerTimeout);

                controller().sendXNetMessage(XNetMessage.getServiceModeResultsMsg(),
                        this);
                return;
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.CS_NOT_SUPPORTED) {
                // programming operation not supported by this command station
                progState = RETURNSENT;
                _error = jmri.ProgListener.NotImplemented;
                // create a request to exit service mode and
                // send the message to the command station
                controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                        this);
                return;
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
                // We Exited Programming Mode early
                //log.error("Service mode exited before sequence complete.");
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.SequenceError);
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.PROG_SHORT_CIRCUIT) {
                // We experienced a short Circuit on the Programming Track
                log.error("Short Circuit While Programming Decoder");
                progState = RETURNSENT;
                _error = jmri.ProgListener.ProgrammingShort;
                // create a request to exit service mode and
                // send the message to the command station
                controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                        this);
                //notifyProgListenerEnd(_val, jmri.ProgListener.ProgrammingShort);
            } else if (m.isCommErrorMessage()) {
                // We experienced a communicatiosn error
                // If this is a Timeslot error, ignore it,
                //otherwise report it as an error
                if (m.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR) {
                    return;
                } else if (!_service_mode) {
                    log.error("Communications error in REQUESTSENT state before entering service mode.  Error: " + m.toString());
                    controller().sendXNetMessage(lastRequestMessage, this);
                } else {
                    log.error("Communications error in REQUESTSENT state after entering service mode.  Error: " + m.toString());
                    progState = RETURNSENT;
                    _error = jmri.ProgListener.CommError;
                    // create a request to exit service mode and
                    // send the message to the command station
                    controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                            this);
                }
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
                progState = RETURNSENT;
                _error = jmri.ProgListener.OK;
                // create a request to exit service mode and
                // send the message to the command station
                controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                        this);
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
                progState = RETURNSENT;
                _error = jmri.ProgListener.OK;
                stopTimer();
                // create a request to exit service mode and
                // send the message to the command station
                controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                        this);
                return;
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.PROG_BYTE_NOT_FOUND) {
                // "data byte not found", e.g. no reply
                progState = RETURNSENT;
                _error = jmri.ProgListener.NoLocoDetected;
                // create a request to exit service mode and
                // send the message to the command station
                controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                        this);
                return;
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.PROG_SHORT_CIRCUIT) {
                // We experienced a short Circuit on the Programming Track
                log.error("Short Circuit While Programming Decoder");
                progState = RETURNSENT;
                _error = jmri.ProgListener.ProgrammingShort;
                // create a request to exit service mode and
                // send the message to the command station
                controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                        this);
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
                // We Exited Programming Mode early
                log.error("Service mode exited before sequence complete.");
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.SequenceError);
            } else if (m.isCommErrorMessage()) {
                // We experienced a communicatiosn error
                // If this is a Timeslot error, ignore it
                if (m.getElement(1) == XNetConstants.LI_MESSAGE_RESPONSE_TIMESLOT_ERROR) {
                    return;
                } else if (_service_mode) {
                    // If we're in service mode, retry sending the
                    // result request.
                    log.error("Communications error in INQUIRESENT state while in service mode.  Error: {}", m.toString());
                    controller().sendXNetMessage(XNetMessage.getServiceModeResultsMsg(),
                            this);
                    return;
                } else {
                    //otherwise report it as an error
                    log.error("Communications error in INQUIRESENT state after exiting service mode.  Error: {}", m.toString());
                    progState = RETURNSENT;
                    _error = jmri.ProgListener.CommError;
                    // create a request to exit service mode and
                    // send the message to the command station
                    controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                            this);
                    return;
                }
            } else {
                // nothing important, ignore
                return;
            }

        } else if (progState == RETURNSENT) {
            log.debug("reply in RETURNSENT state");
            if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
                progState = NOTPROGRAMMING;
                stopTimer();

                // We've exited service mode.  Notify the programmer of any
                // the results.
                notifyProgListenerEnd(_val, _error);

                return;
            }
        } else {
            log.debug("reply in un-decoded state");
        }
    }

    /** 
     * {@inheritDoc}
     *
     * listen for the messages to the LI100/LI101
     */
     @Override
    synchronized public void message(XNetMessage l) {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized protected void timeout() {
        // if a timeout occurs, and we are not
        // finished programming, we need to exit
        // service mode.
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            log.debug("timeout!");

            progState = RETURNSENT;
            if (!getCanRead()) {
                _error = jmri.ProgListener.OK;  //MultiMaus etc.
            } else // perhaps no loco present?
            {
                _error = jmri.ProgListener.FailedTimeout;
            }

            controller().sendXNetMessage(XNetMessage.getExitProgModeMsg(),
                    this);
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LI100XNetProgrammer.class);

}
