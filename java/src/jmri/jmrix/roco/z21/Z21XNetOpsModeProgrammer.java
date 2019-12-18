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
import jmri.jmrix.loconet.LnConstants;
import jmri.jmrix.loconet.LocoNetListener;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LnTrafficController;

/**
 * Provides an Ops mode programming interface for Roco Z21 Currently only Byte
 * mode is implemented, though XpressNet also supports bit mode writes for POM
 *
 * @see jmri.Programmer
 * @author Paul Bender Copyright (C) 2018
 */
public class Z21XNetOpsModeProgrammer extends jmri.jmrix.lenz.XNetOpsModeProgrammer implements LocoNetListener {

    private int _cv;
    private LnTrafficController lnTC;

    public Z21XNetOpsModeProgrammer(int pAddress, XNetTrafficController controller) {
        this(pAddress,controller,null);
    }

    public Z21XNetOpsModeProgrammer(int pAddress, XNetTrafficController controller,LnTrafficController lntc) {
        super(pAddress,controller);
        // connect to listen
        controller.addXNetListener(~0,
                this);
        lnTC = lntc;
        if(lnTC!=null) {
           lnTC.addLocoNetListener(~0,this);
        }
    }

    /** 
     * {@inheritDoc}
     *
     * Send an ops-mode write request to the Xpressnet.
     */
    @Override
    synchronized public void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        XNetMessage msg = XNetMessage.getWriteOpsModeCVMsg(mAddressHigh, mAddressLow, CV, val);
        msg.setBroadcastReply(); // reply comes through a loconet message.
        tc.sendXNetMessage(msg, this);
        /* we need to save the programer and value so we can send messages 
         back to the screen when the programming screen when we receive
         something from the command station */
        progListener = p;
        _cv = 0xffff & CV;
        value = val;
        progState = REQUESTSENT;
        restartTimer(msg.getTimeout());
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
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

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void confirmCV(String CVname, int val, ProgListener p) throws ProgrammerException {
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

    /** 
     * {@inheritDoc}
     */
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
                notifyProgListenerEnd(progListener, value, jmri.ProgListener.OK);
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
                notifyProgListenerEnd(progListener, value, jmri.ProgListener.OK);
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
                    notifyProgListenerEnd(progListener, value, jmri.ProgListener.NoLocoDetected);
                    return;
                } else if (l.getElement(0) == XNetConstants.CS_INFO
                        && l.getElement(1) == XNetConstants.CS_NOT_SUPPORTED) {
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    notifyProgListenerEnd(progListener, value, jmri.ProgListener.NotImplemented);
                } else {
                    /* this is an unknown error */
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    notifyProgListenerEnd(progListener, value, jmri.ProgListener.UnknownError);
                }
            }
        }
    }

    /**
     *   {@inheritDoc}
     */
    @Override
    synchronized public void message(LocoNetMessage m){
      // the Roco Z21 responds to Operations mode write requests with a 
      // LocoNet message.
        log.debug("LocoNet message received: {}", m);

        int slot = m.getElement(2); // slot number for this request

        if(slot == LnConstants.PRG_SLOT && progState == REQUESTSENT) {
            // we are programming, and this is a programming slot message,
            // so let's see if it is for us.
            log.debug("Right message slot and programming");

            // the following 8 lines and assignment of val were copied 
            // from the loconet monitor.
            int hopsa = m.getElement(5); // Ops mode - 7 high address bits
            // of loco to program
            int lopsa = m.getElement(6); // Ops mode - 7 low address bits of
            // loco to program
            int cvh = m.getElement(8); // hi 3 bits of CV# and msb of data7
            int cvl = m.getElement(9); // lo 7 bits of CV#
            int data7 = m.getElement(10); // 7 bits of data to program, msb
            int cvNumber = (((((cvh & LnConstants.CVH_CV8_CV9) >> 3) | (cvh & LnConstants.CVH_CV7)) * 128) + (cvl & 0x7f)) + 1;
            int address =  hopsa * 128 + lopsa;

            // if we attempt to verify the cvNumber, this fails for 
            // multiple writes from the Symbolic Programmer.
            if(address!=mAddress || cvNumber != _cv ){
               log.debug("message for address {} expecting {}; cv {} expecting {}",
                          address,mAddress,cvNumber,_cv);
               return; // not for us
            }

            int val = -1;

            if ((m.getElement(2) & 0x20) != 0) {
               val = (((cvh & LnConstants.CVH_D7) << 6) | (data7 & 0x7f));
            }
  
            log.debug("received value {} for cv {} on address {}",val,cvNumber,address);

            // successful read if LACK return status is not 0x7F
            int code = ProgListener.OK;
            if ((m.getElement(2) == 0x7f)) {
               code = ProgListener.UnknownError;
            }

            progState = NOTPROGRAMMING;
            stopTimer();
            log.debug("sending code {} val {} to programmer",code,val);
            notifyProgListenerEnd(progListener, val, code);
        }
    }


    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Z21XNetOpsModeProgrammer.class);

}
