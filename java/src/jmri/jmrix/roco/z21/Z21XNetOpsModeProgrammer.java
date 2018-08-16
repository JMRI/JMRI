package jmri.jmrix.roco.z21;

import java.util.ArrayList;
import java.util.List;
import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import jmri.jmrix.lenz.XNetConstants;
import jmri.jmrix.lenz.XNetListener;
import jmri.jmrix.lenz.XNetMessage;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetTrafficController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Ops mode programming interface for Roco Z21 Currently only Byte
 * mode is implemented, though XpressNet also supports bit mode writes for POM
 *
 * @see jmri.Programmer
 * @author Paul Bender Copyright (C) 2018
 */
public class Z21XNetOpsModeProgrammer extends jmri.jmrix.lenz.XNetOpsModeProgrammer implements XNetListener, AddressedProgrammer {

    private int _cv;

    public Z21XNetOpsModeProgrammer(int pAddress, XNetTrafficController controller) {
        super(pAddress,controller);
        // connect to listen
        controller.addXNetListener(~0,
                this);
    }

    @Override
    synchronized public void readCV(int CV, ProgListener p) throws ProgrammerException {
        XNetMessage msg = XNetMessage.getVerifyOpsModeCVMsg(mAddressHigh, mAddressLow, CV, value);
        /* we need to save the programer so we can send messages
         back to the programming screen when we receive
         something from the command station */
        progListener = p;
        _cv = 0xffff & CV;
        tc.sendXNetMessage(msg, this);
        progState = REQUESTSENT;
        restartTimer(msg.getTimeout());
    }

    @Override
    public void confirmCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        int CV = Integer.parseInt(CVname);
        XNetMessage msg = XNetMessage.getVerifyOpsModeCVMsg(mAddressHigh, mAddressLow, CV, val);
        tc.sendXNetMessage(msg, this);
        /* we need to save the programer so we can send messages
         back to the programming screen when we receive
         something from the command station */
        progListener = p;
        _cv = 0xffff & CV;
        progState = REQUESTSENT;
        restartTimer(msg.getTimeout());
    }

    @Override
    synchronized public void message(XNetReply l) {
        if (progState == NOTPROGRAMMING) {
            // We really don't care about any messages unless we send a 
            // request, so just ignore anything that comes in
            return;
        } else if (progState == REQUESTSENT) {
            if (l.isOkMessage()) {
                // Before we set the programmer state to not programming, 
                // delay for a short time to give the decoder a chance to 
                // process the request.
                new jmri.util.WaitHandler(this,250);
                progState = NOTPROGRAMMING;
                stopTimer();
                progListener.programmingOpReply(value, jmri.ProgListener.OK);
            } else if (l.getElement(0) == Z21Constants.LAN_X_CV_RESULT_XHEADER
                    && l.getElement(1) == Z21Constants.LAN_X_CV_RESULT_DB0) {
                // valid operation response, but does it belong to us?
                int sent_cv = (l.getElement(2) << 8) + l.getElement(3) + 1;
                if (sent_cv != _cv) {
                    return; // not for us.
                }
                value = l.getElement(4);
                progState = NOTPROGRAMMING;
                stopTimer();
                // if this was a read, we cached the value earlier.  If its a
                // write, we're to return the original write value
                progListener.programmingOpReply(value, jmri.ProgListener.OK);
                return;
            } else {
                /* this is an error */
                if (l.isRetransmittableErrorMsg()) {
                    return;  // just ignore this, since we are retransmitting 
                    // the message.
                } else if (l.getElement(0) == XNetConstants.CS_INFO
                    && l.getElement(1) == XNetConstants.PROG_BYTE_NOT_FOUND) {
                    // "data byte not found", e.g. no reply
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    progListener.programmingOpReply(value, jmri.ProgListener.NoLocoDetected);
                    return;
                } else if (l.getElement(0) == XNetConstants.CS_INFO
                        && l.getElement(1) == XNetConstants.CS_NOT_SUPPORTED) {
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    progListener.programmingOpReply(value, jmri.ProgListener.NotImplemented);
                } else {
                    /* this is an unknown error */
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    progListener.programmingOpReply(value, jmri.ProgListener.UnknownError);
                }
            }
        }
    }

    // initialize logging
    // private final static Logger log = LoggerFactory.getLogger(Z21XNetOpsModeProgrammer.class);

}
