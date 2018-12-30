package jmri.jmrix.sprog;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the SPROG
 * command station object.
 * <p>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see jmri.Programmer
 * @author	Andrew Crosland Copyright (C) 2006
 */
public class SprogOpsModeProgrammer extends SprogProgrammer implements AddressedProgrammer {

    int mAddress;
    boolean mLongAddr;
    private SprogSystemConnectionMemo _memo = null;

    public SprogOpsModeProgrammer(int pAddress, boolean pLongAddr, SprogSystemConnectionMemo memo) {
        super(memo);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
        _memo = memo;
    }

    /** 
     * {@inheritDoc}
     *
     * Forward a write request to an ops-mode write operation
     */
    @Override
    synchronized public void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("write CV=" + CV + " val=" + val);

        // record state.  COMMANDSENT is just waiting for a reply...
        useProgrammer(p);
        progState = COMMANDSENT;
        _val = val;

        // Add the packet to the queue rather than send it directly
        // [AC 23/01/16] Check that there is a free slot for the ops mode packet.
        // Delay the reply to allow time for the ops mode packet to be sent and prevent all slots from filling up
        // when writing multiple CVs, e.g. writing a sheet in the comprehensive programmer.
        if (_memo.getCommandStation().opsModepacket(mAddress, mLongAddr, CV, val) != null) {
            javax.swing.Timer t = new javax.swing.Timer(250, (java.awt.event.ActionEvent evt)->{notifyProgListenerEnd(_val, jmri.ProgListener.OK);});
            t.setRepeats(false);
            t.start();
        } else {
            progState = NOTPROGRAMMING;
            notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
        }
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, ProgListener p) throws ProgrammerException {
        // final int CV = Integer.parseInt(CVname);
        log.error("readCV not available in this protocol");
        throw new ProgrammerException();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    synchronized public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        log.error("confirmCV not available in this protocol");
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
    synchronized public void notifyReply(SprogReply m) {
        // We will not see any replies
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
     * Ops-mode programming doesn't put the command station in programming mode,
     * so we don't have to send an exit-programming command at end. Therefore,
     * this routine does nothing except to replace the parent routine that does
     * something.
     */
    void cleanup() {
    }

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SprogOpsModeProgrammer.class);

}
