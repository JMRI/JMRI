package jmri.jmrix.lenz;

import java.util.ArrayList;
import java.util.List;
import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Ops mode programing interface for XpressNet Currently only Byte
 * mode is implemented, though XpressNet also supports bit mode writes for POM
 *
 * @see jmri.Programmer
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Girgio Terdina Copyright (C) 2007
 */
public class XNetOpsModeProgrammer extends jmri.jmrix.AbstractProgrammer implements XNetListener, AddressedProgrammer {

    int mAddressHigh;
    int mAddressLow;
    int mAddress;
    int progState = 0;
    int value;
    jmri.ProgListener progListener = null;

    protected XNetTrafficController tc = null;

    public XNetOpsModeProgrammer(int pAddress, XNetTrafficController controller) {
        tc = controller;
        if (log.isDebugEnabled()) {
            log.debug("Creating Ops Mode Programmer for Address " + pAddress);
        }
        mAddressLow = LenzCommandStation.getDCCAddressLow(pAddress);
        mAddressHigh = LenzCommandStation.getDCCAddressHigh(pAddress);
        mAddress = pAddress;
        if (log.isDebugEnabled()) {
            log.debug("High Address: " + mAddressHigh + " Low Address: " + mAddressLow);
        }
        // register as a listener
        tc.addXNetListener(XNetInterface.COMMINFO | XNetInterface.CS_INFO, this);
    }

    /**
     * Send an ops-mode write request to the Xpressnet.
     */
    @Override
    synchronized public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        XNetMessage msg = XNetMessage.getWriteOpsModeCVMsg(mAddressHigh, mAddressLow, CV, val);
        tc.sendXNetMessage(msg, this);
        /* we need to save the programer and value so we can send messages 
         back to the screen when the programing screen when we receive
         something from the command station */
        progListener = p;
        value = val;
        progState = XNetProgrammer.REQUESTSENT;
        restartTimer(msg.getTimeout());
    }

    @Override
    synchronized public void readCV(int CV, ProgListener p) throws ProgrammerException {
        XNetMessage msg = XNetMessage.getVerifyOpsModeCVMsg(mAddressHigh, mAddressLow, CV, value);
        tc.sendXNetMessage(msg, this);
        /* We can trigger a read to an LRC120, but the information is not
         currently sent back to us via the XpressNet */
        p.programmingOpReply(CV, jmri.ProgListener.NotImplemented);
    }

    @Override
    public void confirmCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        int CV = Integer.parseInt(CVname);
        XNetMessage msg = XNetMessage.getVerifyOpsModeCVMsg(mAddressHigh, mAddressLow, CV, val);
        tc.sendXNetMessage(msg, this);
        /* We can trigger a read to an LRC120, but the information is not
         currently sent back to us via the XpressNet */
        p.programmingOpReply(val, jmri.ProgListener.NotImplemented);
    }

    /**
     * Types implemented here.
     */
    @Override
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(ProgrammingMode.OPSBYTEMODE);
        return ret;
    }

    /**
     * Can this ops-mode programmer read back values?
     * Indirectly we can, though this requires an external display 
     * (a Lenz LRC120) and enabling railcom.
     *
     * @return true to allow us to trigger an ops mode read
     */
    @Override
    public boolean getCanRead() {
        // An operations mode read can be triggered on command 
        // stations which support Operations Mode Writes (LZ100,
        // LZV100,MultiMouse).  Whether or not the operation produces
        // a result depends on additional external hardware (a booster 
        // with an enabled  RailCom cutout (LV102 or similar) and a 
        // RailCom receiver circuit (LRC120 or similar)).
        // We have no way of determining if the required external 
        // hardware is present, so we return true for all command 
        // stations on which the Operations Mode Programmer is enabled.

        // yes, we just call the superclass method.  Leave this in place
        // so the comments and javadoc above make sense.
        return super.getCanRead();
    }


    @Override
    synchronized public void message(XNetReply l) {
        if (progState == XNetProgrammer.NOTPROGRAMMING) {
            // We really don't care about any messages unless we send a 
            // request, so just ignore anything that comes in
            return;
        } else if (progState == XNetProgrammer.REQUESTSENT) {
            if (l.isOkMessage()) {
                // Before we set the programmer state to not programming, 
                // delay for a short time to give the decoder a chance to 
                // process the request.
                new jmri.util.WaitHandler(this,250);
                progState = XNetProgrammer.NOTPROGRAMMING;
                stopTimer();
                progListener.programmingOpReply(value, jmri.ProgListener.OK);
            } else {
                /* this is an error */
                if (l.isRetransmittableErrorMsg()) {
                    return;  // just ignore this, since we are retransmitting 
                    // the message.
                } else if (l.getElement(0) == XNetConstants.CS_INFO
                        && l.getElement(1) == XNetConstants.CS_NOT_SUPPORTED) {
                    progState = XNetProgrammer.NOTPROGRAMMING;
                    stopTimer();
                    progListener.programmingOpReply(value, jmri.ProgListener.NotImplemented);
                } else {
                    /* this is an unknown error */
                    progState = XNetProgrammer.NOTPROGRAMMING;
                    stopTimer();
                    progListener.programmingOpReply(value, jmri.ProgListener.UnknownError);
                }
            }
        }
    }

    @Override
    public boolean getLongAddress() {
        return true;
    }

    @Override
    public int getAddressNumber() {
        return mAddress;
    }

    @Override
    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }

    // listen for the messages to the LI100/LI101
    @Override
    public synchronized void message(XNetMessage l) {
    }

    /**
     * Handle a timeout notification
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    @Override
    synchronized protected void timeout() {
        progState = XNetProgrammer.NOTPROGRAMMING;
        stopTimer();
        progListener.programmingOpReply(value, jmri.ProgListener.FailedTimeout);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(XNetOpsModeProgrammer.class);

}
