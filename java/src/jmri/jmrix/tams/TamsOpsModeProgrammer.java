package jmri.jmrix.tams;

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
 * Provide an Ops Mode Programmer via a wrapper what works with the TAMS command
 * station object.
 * <p>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see jmri.Programmer Based on work by Bob Jacobsen
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public class TamsOpsModeProgrammer extends TamsProgrammer implements AddressedProgrammer {

    int mAddress;
    boolean mLongAddr;

    public TamsOpsModeProgrammer(TamsTrafficController tc, int pAddress, boolean pLongAddr) {
        super(tc);
        log.debug("TAMs ops mode programmer " + pAddress + " " + pLongAddr);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /** 
     * {@inheritDoc}
     *
     * Forward a write request to an ops-mode write operation
     */
    @Override
    public synchronized void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        if (log.isDebugEnabled()) {
            log.debug("write CV=" + CV + " val=" + val);
        }
        useProgrammer(p);
        _progRead = false;
        _val = val;
        _cv = CV;
        progState = COMMANDSENT;

        // start the error timer
        startShortTimer();

        // format and send the write message
        tc.sendTamsMessage(TamsMessage.getWriteOpsModeCVMsg(mAddress, CV, val), this);

    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void readCV(String CVname, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        if (log.isDebugEnabled()) {
            log.debug("read CV=" + CV);
        }
        log.error("readCV not available in this protocol");
        throw new ProgrammerException();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("confirm CV=" + CV);
        }
        log.error("confirmCV not available in this protocol");
        throw new ProgrammerException();
    }

    /** 
     * {@inheritDoc}
     */
    // add 200mSec between commands, so NCE command station queue doesn't get overrun
    @Override
    protected void notifyProgListenerEnd(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("TamsOpsModeProgrammer adds 200mSec delay to response");
        }
        try {
            wait(200);
        } catch (InterruptedException e) {
            log.debug("unexpected exception " + e);
        }
        super.notifyProgListenerEnd(value, status);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        ret.add(ProgrammingMode.OPSBYTEMODE);
        return ret;
    }

    /** 
     * {@inheritDoc}
     *
     * Can this ops-mode programmer read back values? For now, no, but maybe
     * later.
     *
     * @return always false for now
     */
    @Override
    public boolean getCanRead() {
        return false;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean getLongAddress() {
        return mLongAddr;
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
     *
     * Ops-mode programming doesn't put the command station in programming mode,
     * so we don't have to send an exit-programming command at end. Therefore,
     * this routine does nothing except to replace the parent routine that does
     * something.
     */
    @Override
    void cleanup() {
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(TamsOpsModeProgrammer.class);

}
