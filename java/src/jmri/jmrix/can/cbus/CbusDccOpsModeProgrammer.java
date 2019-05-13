package jmri.jmrix.can.cbus;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import jmri.jmrix.can.CanReply;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the CBUS command
 * station object.
 * <p>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see jmri.Programmer
 * @author Andrew Crosland Copyright (C) 2009
 */
public class CbusDccOpsModeProgrammer extends CbusDccProgrammer implements AddressedProgrammer {

    int mAddress;
    boolean mLongAddr;

    public CbusDccOpsModeProgrammer(int pAddress, boolean pLongAddr, jmri.jmrix.can.TrafficController tc) {
        super(tc);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /** 
     * {@inheritDoc}
     *
     * Forward a write request to an ops-mode write operation
     */
    @Override
    synchronized public void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("ops mode write CV={} val={}", CV, val);

        // record state.  COMMANDSENT is just waiting for a reply...
        useProgrammer(p);
        _progRead = false;
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

        // send the programming command
        tc.sendCanMessage(CbusMessage.getOpsModeWriteCV(mAddress, mLongAddr, CV, val, tc.getCanid()), this);
        notifyProgListenerEnd(_val, jmri.ProgListener.OK);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, ProgListener p) throws ProgrammerException {
        log.error("readCV {} not available for MERG CBUS, a query to track would return all locos",CVname);
        throw new ProgrammerException();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        log.error("confirmCV {} not available for MERG CBUS, a query to track would return all locos",CV);
        throw new ProgrammerException();
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
        return ret;
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void reply(CanReply m) {
        // We will not see any replies
    }

    /** 
     * {@inheritDoc}
     *
     * Can this ops-mode programmer read back values?
     *
     * @return always false
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
     * Ops-mode programming doesn't put the command station in programming mode,
     * so we don't have to send an exit-programming command at end. Therefore,
     * this routine does nothing except to replace the parent routine that does
     * something.
     */
    void cleanup() {
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusDccOpsModeProgrammer.class);
}
