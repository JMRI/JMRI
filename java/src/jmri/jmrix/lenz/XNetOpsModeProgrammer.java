package jmri.jmrix.lenz;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 * Provides an Ops mode programming interface for XpressNet Currently only Byte
 * mode is implemented, though XpressNet also supports bit mode writes for POM
 *
 * @see jmri.Programmer
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Girgio Terdina Copyright (C) 2007
 */
public class XNetOpsModeProgrammer extends jmri.jmrix.AbstractProgrammer implements XNetListener, AddressedProgrammer {

    protected final int mAddressHigh;
    protected final int mAddressLow;
    protected final int mAddress;
    protected int progState = NOTPROGRAMMING;
    protected int progMethod = WRITE;
    protected int value;
    protected jmri.ProgListener progListener = null;

    // possible states.
    static protected final int NOTPROGRAMMING = 0; // is notProgramming
    static protected final int REQUESTSENT = 1; // read/write command sent, waiting reply
    static protected final int RESULTREQUESTED = 2; // result request sent, waiting reply
    static protected final int WRITE = 0; // write mode
    static protected final int READ = 1; // read mode

    protected XNetTrafficController tc;

    public XNetOpsModeProgrammer(int pAddress, XNetTrafficController controller) {
        tc = controller;
        if (log.isDebugEnabled()) {
            log.debug("Creating Ops Mode Programmer for Address {}", pAddress);
        }
        mAddressLow = LenzCommandStation.getDCCAddressLow(pAddress);
        mAddressHigh = LenzCommandStation.getDCCAddressHigh(pAddress);
        mAddress = pAddress;
        if (log.isDebugEnabled()) {
            log.debug("High Address: {} Low Address: {}", mAddressHigh, mAddressLow);
        }
        // register as a listener
        tc.addXNetListener(XNetInterface.COMMINFO | XNetInterface.CS_INFO, this);
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
        tc.sendXNetMessage(msg, this);
        /* we need to save the programer and value so we can send messages
         back to the screen when the programming screen when we receive
         something from the command station */
        progListener = p;
        value = val;
        progState = REQUESTSENT;
        progMethod = WRITE;
        restartTimer(msg.getTimeout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        XNetMessage msg = XNetMessage.getVerifyOpsModeCVMsg(mAddressHigh, mAddressLow, CV, value);
        tc.sendXNetMessage(msg, this);
        /* we need to save the programer and value so we can send messages 
         back to the screen when the programming screen when we receive
         something from the command station */
        progListener = p;
        progState = REQUESTSENT;
        progMethod = READ;
        restartTimer(msg.getTimeout());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void confirmCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        int CV = Integer.parseInt(CVname);
        XNetMessage msg = XNetMessage.getVerifyOpsModeCVMsg(mAddressHigh, mAddressLow, CV, val);
        tc.sendXNetMessage(msg, this);
        /* we need to save the programer and value so we can send messages 
         back to the screen when the programming screen when we receive
         something from the command station */
        progListener = p;
        value = val;
        progState = REQUESTSENT;
        progMethod = READ;
        restartTimer(msg.getTimeout());
    }

    /**
     * {@inheritDoc}
     *
     * Types implemented here.
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<>();
        ret.add(ProgrammingMode.OPSBYTEMODE);
        return ret;
    }

    /**
     * {@inheritDoc}
     *
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
        // LZV100, MultiMouse).  Whether or not the operation produces
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


    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void message(XNetReply l) {
        if (progState == NOTPROGRAMMING) {
            // We really don't care about any messages unless we send a
            // request, so just ignore anything that comes in
        } else if (progState == REQUESTSENT) {
            if (l.isOkMessage()) {
                // Before we set the programmer state to not programming,
                // delay for a short time to give the decoder a chance to
                // process the request.
                if (progMethod == WRITE) {
                    new jmri.util.WaitHandler(this, 250);
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    notifyProgListenerEnd(progListener, value, jmri.ProgListener.OK);
                } else if (progMethod == READ) {
                    stopTimer();
                    new jmri.util.WaitHandler(this, 1000); // XPressNet documentations say to wait up to 0.5 to 1 second before requesting the result
                    progState = RESULTREQUESTED;
                    XNetMessage msg = XNetMessage.getOpsModeResultsMsg();
                    tc.sendXNetMessage(msg, this);
                    restartTimer(msg.getTimeout());
                }
            } else {
                handleXNetError(l);
            }
        } else if(progState == RESULTREQUESTED) {
            if (l.isOpsModeResultMessage()) {
                // We got a result message, so we can stop the timer
                int address = l.getOpsModeResultAddress();
                if (address != mAddress && address != 0) {
                    // This is not the address we are looking for, so ignore it
                    if (log.isDebugEnabled()) {
                        log.debug("Received result message for address {}, expected {}", address, mAddress);
                    }
                    return;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Received result message: {}", l);
                }
                if (address == 0) {
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    // this indicates that there was no result from the read, so notify the programmer listener of that
                    notifyProgListenerEnd(progListener, value, ProgListener.NoAck);
                } else {
                    progState = NOTPROGRAMMING;
                    stopTimer();
                    // Notify the listener of the result
                    notifyProgListenerEnd(progListener, l.getOpsModeResultValue(), jmri.ProgListener.OK);
                }
            } else {
                handleXNetError(l);
            }
        }
    }

    private void handleXNetError(XNetReply l) {
        /* this is an error */
        if (l.isRetransmittableErrorMsg()) {
            // just ignore this, since we are retransmitting
            // the message.
        } else if (l.getElement(0) == XNetConstants.CS_INFO
                    && l.getElement(1) == XNetConstants.CS_NOT_SUPPORTED) {
            progState = NOTPROGRAMMING;
            stopTimer();
            notifyProgListenerEnd(progListener,value, ProgListener.NotImplemented);
        } else {
            /* this is an unknown error */
            progState = NOTPROGRAMMING;
            stopTimer();
            notifyProgListenerEnd(progListener,value, ProgListener.UnknownError);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getLongAddress() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getAddressNumber() {
        return mAddress;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAddress() {
        return "" + getAddressNumber() + " " + getLongAddress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void message(XNetMessage l) {
    }

    /**
     * {@inheritDoc}
     *
     * Handle a timeout notification
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message{}", msg.toString());
        }
    }

    /**
     * {@inheritDoc}
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
            if (getCanRead()) {
               notifyProgListenerEnd(progListener,value,jmri.ProgListener.FailedTimeout);
            } else {
               notifyProgListenerEnd(progListener,value,jmri.ProgListener.OK);
            }
        }
    }

    @Override
    public Configurator getConfigurator() {
        return new XNetOpsConfigurator();
    }

    /**
     * This class is used by tests.
     */
    public static class XNetOpsConfigurator implements Configurator {
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XNetOpsModeProgrammer.class);

}
