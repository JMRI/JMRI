package jmri.jmrix.roco.z21;

import jmri.ProgrammingMode;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetProgrammer;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTrafficController;

/**
 * Z21 Programmer support for Lenz XpressNet.
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
 * @author Paul Bender Copyright (c) 2014
 */
public class Z21XNetProgrammer extends XNetProgrammer {

    public Z21XNetProgrammer(XNetTrafficController tc) {
        super(tc);
        // connect to listen
        controller().addXNetListener(~0,
                this);
    }

    /** 
     * {@inheritDoc}
     *
     * Can we read from a specific CV in the specified mode? Answer may not be
     * correct if the command station type and version sent by the command
     * station mimics one of the known command stations.
     */
    @Override
    public boolean getCanRead(String addr) {
        if (log.isDebugEnabled()) {
            log.debug("check mode " + getMode() + " CV " + addr);
        }
        if (!getCanRead()) {
            return false; // check basic implementation first
        }
        // Multimaus cannot read CVs, unless Rocomotion interface is used, assume other Command Stations do.
        // To be revised if and when a Rocomotion adapter is introduced!!!
        if (controller().getCommandStation().getCommandStationType() == 0x10) {
            return false;
        }

        if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
            return true; // z21 allows us to specify the CV in 16 bits.
        } else {
            return Integer.parseInt(addr) <= 256;
        }
    }

    /** 
     * {@inheritDoc}
     *
     * Can we write to a specific CV in the specified mode? Answer may not be
     * correct if the command station type and version sent by the command
     * station mimics one of the known command stations.
     */
    @Override
    public boolean getCanWrite(String addr) {
        if (log.isDebugEnabled()) {
            log.debug("check CV " + addr);
            log.debug("cs Type: " + controller().getCommandStation().getCommandStationType() + " CS Version: " + controller().getCommandStation().getCommandStationSoftwareVersion());
        }
        if (!getCanWrite()) {
            return false; // check basic implementation first
        }
        if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
            return true; // z21 allows us to specify the CV in 16 bits.
        } else {
            return Integer.parseInt(addr) <= 256;
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void writeCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        if (getMode().equals(ProgrammingMode.DIRECTBITMODE)
                || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
            if (log.isDebugEnabled()) {
                log.debug("writeCV " + CV + " listens " + p);
            }
            useProgrammer(p);
            _progRead = false;
            // set new state & save values
            progState = REQUESTSENT;
            _val = val;
            _cv = 0xffff & CV;

            // start the error timer
            restartTimer(XNetProgrammerTimeout);

            XNetMessage msg = Z21XNetMessage.getZ21WriteDirectCVMsg(CV, val);
            controller().sendXNetMessage(msg, this);
        } else {
            super.writeCV(CVname, val, p);
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        if (getMode().equals(ProgrammingMode.DIRECTBITMODE)
                || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
            if (log.isDebugEnabled()) {
                log.debug("readCV " + CV + " listens " + p);
            }

            useProgrammer(p);
            _cv = 0xffff & CV;
            _progRead = true;
            // set new state
            progState = REQUESTSENT;
            // start the error timer
            restartTimer(XNetProgrammerTimeout);

            // format and send message to go to program mode
            XNetMessage msg = Z21XNetMessage.getZ21ReadDirectCVMsg(CV);
            controller().sendXNetMessage(msg, this);
        } else {
            super.readCV(CVname, p);
        }

    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void message(XNetReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            return;

        } else if (progState == REQUESTSENT
                || progState == INQUIRESENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in {} state", progState == REQUESTSENT ? "REQUESTSENT" : "INQUIRESENT");
            }
            if (m.getElement(0) == Z21Constants.LAN_X_CV_RESULT_XHEADER
                    && m.getElement(1) == Z21Constants.LAN_X_CV_RESULT_DB0) {
                // valid operation response, but does it belong to us?
                int sent_cv = (m.getElement(2) << 8) + m.getElement(3) + 1;
                if (sent_cv != _cv) {
                    return; // not for us.
                }			    // see why waiting
                if (_progRead) {
                    // read was in progress - get return value
                    _val = m.getElement(4);
                }
                progState = NOTPROGRAMMING;
                stopTimer();
                // if this was a read, we cached the value earlier.  If its a
                // write, we're to return the original write value
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
                return;
            } else {
                super.message(m);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("reply in un-decoded state");
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Z21XNetProgrammer.class);

}
