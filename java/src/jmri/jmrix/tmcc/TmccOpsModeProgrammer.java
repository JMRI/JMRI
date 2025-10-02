package jmri.jmrix.tmcc;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;

/**
 * Provide an Ops Mode Programmer via a wrapper that works with the
 * TMCC Command Station
 * <p>
 * Functionally, this just creates packets to send via the Command Station.
 *
 * @see jmri.Programmer
 * @author Bob Jacobsen Copyright (C) 2002, 2025
 */
public class TmccOpsModeProgrammer extends TmccProgrammer implements AddressedProgrammer {

    int mAddress;
    boolean mLongAddr;

    public TmccOpsModeProgrammer(int pAddress, boolean pLongAddr, TmccSystemConnectionMemo memo) {
        super(memo);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /** 
     * {@inheritDoc}
     *
     * Forward a write request to an ops-mode write operation.
     */
    @Override
    public synchronized void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("write CV={} val={}", CV, val);


        var msg = new SerialMessage();
        ////////////// change next two lines to load msg with desired content
        msg.setOpCode('S');
        msg.setElement(1, ' ');

        // send it
        tc.sendSerialMessage(msg, null);
        
        // return for next operation
        notifyProgListenerEnd(p, _val, jmri.ProgListener.OK);

    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void readCV(String CVname, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("read CV={}", CV);
        log.error("readCV not available in this protocol");
        throw new ProgrammerException();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        log.debug("confirm CV={}", CV);
        log.error("confirmCV not available in this protocol");
        throw new ProgrammerException();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<ProgrammingMode>();
        //// if you want to define a new programming mode, that goes here
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

    // initialize logging
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TmccOpsModeProgrammer.class);

}
