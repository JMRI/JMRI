package jmri.jmrix.dccpp;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides an Ops mode programming interface for DCC++. Currently only Byte
 * mode is implemented, though XPressNet also supports bit mode writes for POM
 *
 * @see jmri.Programmer
 * @author Paul Bender Copyright (C) 2003-2010
 * @author Girgio Terdina Copyright (C) 2007
 * @author Mark Underwood Copyright (C) 2015
 *
 * Based on XNetOpsModeProgrammer by Paul Bender and Girgio Terdina
 */
public class DCCppOpsModeProgrammer extends jmri.jmrix.AbstractProgrammer implements DCCppListener, AddressedProgrammer {

    int mAddressHigh;
    int mAddressLow;
    int mAddress;
    int progState = 0;
    int value;
    ProgListener progListener = null;

    protected DCCppTrafficController tc = null;

    public DCCppOpsModeProgrammer(int pAddress, DCCppTrafficController controller) {
        tc = controller;
        if (log.isDebugEnabled()) {
            log.debug("Creating Ops Mode Programmer for Address " + pAddress);
        }
        mAddressLow = DCCppCommandStation.getDCCAddressLow(pAddress);
        mAddressHigh = DCCppCommandStation.getDCCAddressHigh(pAddress);
        mAddress = pAddress;
        if (log.isDebugEnabled()) {
            log.debug("High Address: " + mAddressHigh + " Low Address: " + mAddressLow);
        }
        // register as a listener
        tc.addDCCppListener(DCCppInterface.COMMINFO | DCCppInterface.CS_INFO, this);
    }

    /** 
     * {@inheritDoc}
     *
     * Send an ops-mode write request to the DCC++.
     */
    @Override
    synchronized public void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        DCCppMessage msg = DCCppMessage.makeWriteOpsModeCVMsg(mAddress, CV, val);
        tc.sendDCCppMessage(msg, this);
        /* we need to save the programer and value so we can send messages 
         back to the screen when the programming screen when we receive
         something from the command station */
        progListener = p;
        value = val;
        progState = DCCppProgrammer.REQUESTSENT;
        restartTimer(msg.getTimeout());
        /* Issue #2423 (GitHub) -- DCC++ base station does not respond to Ops Mode
         * writes, so waiting for a response just means JMRI times out after a long delay.
        /* Proposed Fix: Don't go to REQUESTSENT state... just stay in NOTPROGRAMMING
         * Risk... the state change introduces a 250ms delay to keep the UI from sending
         * write commands too frequently... so we'd have to do that here too.
        */
        // Before we set the programmer state to not programming, 
        // delay for a short time to give the decoder a chance to 
        // process the request.
        try {
            this.wait(250);
        } catch (java.lang.InterruptedException ie) {
            log.debug("Interrupted During Delay");
        }
        progState = DCCppProgrammer.NOTPROGRAMMING;
        stopTimer();
        notifyProgListenerEnd(progListener,value,ProgListener.OK);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        //DCCppMessage msg = DCCppMessage.getVerifyOpsModeCVMsg(mAddressHigh, mAddressLow, CV, value);
        //tc.sendDCCppMessage(msg, this);
        /* We can trigger a read to an LRC120, but the information is not
         currently sent back to us via the XPressNet */
        notifyProgListenerEnd(p,CV,ProgListener.NotImplemented);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        //DCCppMessage msg = DCCppMessage.getVerifyOpsModeCVMsg(mAddressHigh, mAddressLow, CV, val);
        //tc.sendDCCppMessage(msg, this);
        /* We can trigger a read to an LRC120, but the information is not
         currently sent back to us via the XPressNet */
        notifyProgListenerEnd(p,val,ProgListener.NotImplemented);
    }

    /** 
     * {@inheritDoc}
     *
     * Types implemented here.
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(ProgrammingMode.OPSBYTEMODE);
        ret.add(ProgrammingMode.OPSBITMODE);
        return ret;
    }

    /** 
     * {@inheritDoc}
     *
     * This method is leftover from the initial implementation based on
     * XPressNet.  This always sends a NotImplemented message to the progammer
     * listener, regardless of the reply.  Does the DCC++ command station 
     * really send a response to Ops Mode programming instructions?  If the 
     * answer is no, this should be removed.
     */
    @Override
    synchronized public void message(DCCppReply l) {
        notifyProgListenerEnd(progListener,value,ProgListener.NotImplemented);
        if (progState == DCCppProgrammer.NOTPROGRAMMING) {
            // We really don't care about any messages unless we send a 
            // request, so just ignore anything that comes in
            return;
        } else if (progState == DCCppProgrammer.REQUESTSENT) {
     
            if (l.isProgramReply()) {
                // Before we set the programmer state to not programming, 
                // delay for a short time to give the decoder a chance to 
                // process the request.
                try {
                    this.wait(250);
                } catch (java.lang.InterruptedException ie) {
                    log.debug("Interrupted During Delay");
                }
                progState = DCCppProgrammer.NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(progListener,value,ProgListener.OK);
            } else {
                return;
            }
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
    public synchronized void message(DCCppMessage l) {
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        if (log.isDebugEnabled()) {
            log.debug("Notified of timeout on message" + msg.toString());
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized protected void timeout() {
        progState = DCCppProgrammer.NOTPROGRAMMING;
        stopTimer();
        notifyProgListenerEnd(progListener,value,ProgListener.FailedTimeout);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(DCCppOpsModeProgrammer.class);

}
