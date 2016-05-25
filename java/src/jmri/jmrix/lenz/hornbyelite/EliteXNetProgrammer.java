/*
 * EliteXNetProgrammer.java
 */
 // Convert the jmri.Programmer interface into commands for the Lenz XpressNet
package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetProgrammer;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer support for Hornby Elite implementationn of XpressNet.
 * <P>
 * The read operation state sequence is:
 * <UL>
 * <LI>Send Register Mode / Paged mode /Direct Mode read request
 * <LI>Wait for Broadcast Service Mode Entry message -- not happening on elite
 * <LI>Send Request for Service Mode Results request
 * <LI>Wait for results reply, interpret
 * <LI>Send Resume Operations request -- The Elite does not seem to require this
 * step.
 * <LI>Wait for Normal Operations Resumed broadcast -- The Elite does not seem
 * to require this step.
 * </UL>
 *
 * @author Paul Bender Copyright (c) 2008
 * @version $Revision$
 */
public class EliteXNetProgrammer extends XNetProgrammer implements XNetListener {

    static private final int RETURNSENT = 3;
    // Message timeout lengths.  These have been determined by
    // experimentation, and may need to be adjusted
    static private final int ELITEMESSAGETIMEOUT = 10000;
    static private final int EliteXNetProgrammerTimeout = 20000;

    public EliteXNetProgrammer(XNetTrafficController tc) {
        super(tc);
    }

    // programming interface
    synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("writeCV " + CV + " listens " + p);
        }
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
            if (getMode().equals(DefaultProgrammerManager.PAGEMODE)) {
                XNetMessage msg = XNetMessage.getWritePagedCVMsg(CV, val);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                msg.setTimeout(ELITEMESSAGETIMEOUT);
                controller().sendXNetMessage(msg, this);
            } else if (getMode().equals(DefaultProgrammerManager.DIRECTBITMODE) || getMode().equals(DefaultProgrammerManager.DIRECTBYTEMODE)) {
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

        // On the Elite, we're not getting a broadcast message
        // saying we're in service mode, so go ahead and request
        // the results.
        progState = INQUIRESENT;
        //start the error timer
        restartTimer(EliteXNetProgrammerTimeout);
        XNetMessage resultMsg = XNetMessage.getServiceModeResultsMsg();
        resultMsg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
        resultMsg.setTimeout(ELITEMESSAGETIMEOUT);
        controller().sendXNetMessage(resultMsg, this);
    }

    synchronized public void confirmCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    synchronized public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("readCV " + CV + " listens " + p);
        }

        if (!getCanRead()) {
            // should not invoke this if cant read, but if done anyway set NotImplemented error
            p.programmingOpReply(CV, jmri.ProgListener.NotImplemented);
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
            if (getMode().equals(DefaultProgrammerManager.PAGEMODE)) {
                XNetMessage msg = XNetMessage.getReadPagedCVMsg(CV);
                msg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                msg.setTimeout(ELITEMESSAGETIMEOUT);
                controller().sendXNetMessage(msg, this);
            } else if (getMode().equals(DefaultProgrammerManager.DIRECTBITMODE) || getMode().equals(DefaultProgrammerManager.DIRECTBYTEMODE)) {
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

        // On the Elite, we're not getting a broadcast message
        // saying we're in service mode, so go ahead and request
        // the results.
        progState = INQUIRESENT;
        //start the error timer
        restartTimer(EliteXNetProgrammerTimeout);
        XNetMessage resultMsg = XNetMessage.getServiceModeResultsMsg();
        resultMsg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
        resultMsg.setTimeout(ELITEMESSAGETIMEOUT);
        controller().sendXNetMessage(resultMsg, this);
    }

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
            if (log.isDebugEnabled()) {
                log.debug("reply in REQUESTSENT state");
            }
            // see if reply is the acknowledge of program mode; if not, wait for next
            if ((_service_mode && m.isOkMessage())
                    || (m.getElement(0) == XNetConstants.CS_INFO
                    && (m.getElement(1) == XNetConstants.BC_SERVICE_MODE_ENTRY
                    || m.getElement(1) == XNetConstants.PROG_CS_READY))) {
                stopTimer();

                if (!getCanRead()) {
                    // should not read here if cant read, because read shouldnt be invoked, but still attempt to handle
                    if (log.isDebugEnabled()) {
                        log.debug("CV reading not supported, exiting REQUESTSENT state");
                    }
                    stopTimer();
                    notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                    return;
                }

                // here ready to request the results
                progState = INQUIRESENT;
                //start the error timer
                restartTimer(EliteXNetProgrammerTimeout);
                XNetMessage resultMsg = XNetMessage.getServiceModeResultsMsg();

                resultMsg.setNeededMode(jmri.jmrix.AbstractMRTrafficController.NORMALMODE);
                resultMsg.setTimeout(ELITEMESSAGETIMEOUT);
                controller().sendXNetMessage(resultMsg, this);
                return;
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.CS_NOT_SUPPORTED) {
                // programming operation not supported by this command station
                progState = NOTPROGRAMMING;
                notifyProgListenerEnd(_val, jmri.ProgListener.NotImplemented);
                return;
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
                // We Exited Programming Mode early
                log.error("Service mode exited before sequence complete.");
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.SequenceError);
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
            if (log.isDebugEnabled()) {
                log.debug("reply in INQUIRESENT state");
            }
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
                        log.debug(" result for CV " + m.getServiceModeCVNumber()
                                + " expecting " + _cv);
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
                    log.debug(" result for CV " + m.getServiceModeCVNumber()
                            + " expecting " + _cv);
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

        } else if (progState == RETURNSENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in RETURNSENT state");
            }
            if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
                progState = NOTPROGRAMMING;
                stopTimer();
                //notifyProgListenerEnd(_val, jmri.ProgListener.UnknownError);
                return;
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("reply in un-decoded state");
            }
        }
    }

    // listen for the messages to the Elite 
    synchronized public void message(XNetMessage l) {
    }

    private final static Logger log = LoggerFactory.getLogger(EliteXNetProgrammer.class.getName());

}


/* @(#)XNetProgrammer.java */
