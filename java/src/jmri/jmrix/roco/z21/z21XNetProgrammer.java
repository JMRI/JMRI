/**
 * z21XNetProgrammer.java
 */
 // Convert the jmri.Programmer interface into commands for the z21/Z21 
package jmri.jmrix.roco.z21;

import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetProgrammer;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTrafficController;
import jmri.managers.DefaultProgrammerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer support for Lenz XpressNet.
 * <P>
 * The read operation state sequence is:
 * <UL>
 * <LI>Send Register Mode / Paged mode /Direct Mode read request
 * <LI>Wait for Broadcast Service Mode Entry message
 * <LI>Send Request for Service Mode Results request
 * <LI>Wait for results reply, interpret
 * <LI>Send Resume Operations request
 * <LI>Wait for Normal Operations Resumed broadcast
 * </UL>
 *
 * @author Paul Bender Copyright (c) 2014
 * @version $Revision: 28274 $
 */
public class z21XNetProgrammer extends XNetProgrammer {

    public z21XNetProgrammer(XNetTrafficController tc) {
        super(tc);
        // connect to listen
        controller().addXNetListener(~0,
                this);
    }

    /**
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

        if (getMode().equals(DefaultProgrammerManager.DIRECTBITMODE) || getMode().equals(DefaultProgrammerManager.DIRECTBYTEMODE)) {
            return true; // z21 allows us to specify the CV in 16 bits.
        } else {
            return Integer.parseInt(addr) <= 256;
        }
    }

    /**
     * Can we write to a specific CV in the specified mode? Answer may not be
     * correct if the command station type and version sent by the command
     * station mimics one of the known command stations.
     */
    @Override
    public boolean getCanWrite(String addr) {
        if (log.isDebugEnabled()) {
            log.debug("check CV " + addr);
        }
        log.error("cs Type: " + controller().getCommandStation().getCommandStationType() + " CS Version: " + controller().getCommandStation().getCommandStationSoftwareVersion());
        if (!getCanWrite()) {
            return false; // check basic implementation first
        }
        if (getMode().equals(DefaultProgrammerManager.DIRECTBITMODE) || getMode().equals(DefaultProgrammerManager.DIRECTBYTEMODE)) {
            return true; // z21 allows us to specify the CV in 16 bits.
        } else {
            return Integer.parseInt(addr) <= 256;
        }
    }

    // programming interface
    synchronized public void writeCV(int CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (getMode().equals(DefaultProgrammerManager.DIRECTBITMODE)
                || getMode().equals(DefaultProgrammerManager.DIRECTBYTEMODE)) {
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

            XNetMessage msg = z21XNetMessage.getWriteDirectCVMsg(CV, val);
            controller().sendXNetMessage(msg, this);
        } else {
            super.writeCV(CV, val, p);
        }
    }

    synchronized public void readCV(int CV, jmri.ProgListener p) throws jmri.ProgrammerException {
        if (getMode().equals(DefaultProgrammerManager.DIRECTBITMODE)
                || getMode().equals(DefaultProgrammerManager.DIRECTBYTEMODE)) {
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
            XNetMessage msg = z21XNetMessage.getReadDirectCVMsg(CV);
            controller().sendXNetMessage(msg, this);
        } else {
            super.readCV(CV, p);
        }

    }

    synchronized public void message(XNetReply m) {
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these
            return;

        } else if (progState == REQUESTSENT
                || progState == INQUIRESENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in {} state", progState == REQUESTSENT ? "REQUESTSENT" : "INQUIRESENT");
            }
            if (m.getElement(0) == z21Constants.LAN_X_CV_RESULT_XHEADER
                    && m.getElement(1) == z21Constants.LAN_X_CV_RESULT_DB0) {
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

    private final static Logger log = LoggerFactory.getLogger(z21XNetProgrammer.class.getName());

}


/* @(#)z21XNetProgrammer.java */
