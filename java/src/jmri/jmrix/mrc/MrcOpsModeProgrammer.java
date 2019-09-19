package jmri.jmrix.mrc;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the MRC command
 * station object.
 * <p>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see jmri.Programmer
 * @author Bob Jacobsen Copyright (C) 2002
 * @author Ken Cameron Copyright (C) 2014
 * @author Kevin Dickerson Copyright (C) 2014
 */
public class MrcOpsModeProgrammer extends MrcProgrammer implements jmri.AddressedProgrammer {

    int mAddress;
    boolean mLongAddr;

    public MrcOpsModeProgrammer(MrcSystemConnectionMemo memo, int pAddress, boolean pLongAddr) {
        super(memo);
        log.debug("MRC ops mode programmer " + pAddress + " " + pLongAddr); //IN18N
        if (pLongAddr) {
            addressLo = pAddress;
            addressHi = pAddress >> 8;
            addressHi = addressHi + 0xc0; //We add 0xc0 to the high byte.
        } else {
            addressLo = pAddress;
        }
    }

    int addressLo = 0x00;
    int addressHi = 0x00;

    /** 
     * {@inheritDoc}
     *
     * Forward a write request to an ops-mode write operation
     */
    @Override
    public synchronized void writeCV(String CVname, int val, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("write CV={} val={}", CV, val); //IN18N
        MrcMessage msg = MrcMessage.getPOM(addressLo, addressHi, CV, val);

        useProgrammer(p);
        _progRead = false;
        progState = POMCOMMANDSENT;
        _val = val;
        _cv = CV;

        // start the error timer
        startShortTimer();

        memo.getMrcTrafficController().sendMrcMessage(msg);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void readCV(String CVname, ProgListener p) throws ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("read CV={}", CV);
        log.error("readCV not available in this protocol"); //IN18N
        throw new ProgrammerException();
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public synchronized void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        log.debug("confirm CV={}", CV);
        log.error("confirmCV not available in this protocol"); //IN18N
        throw new ProgrammerException();
    }

    /** 
     * {@inheritDoc}
     */
    // add 200mSec between commands, so MRC command station queue doesn't get overrun
    @Override
    protected void notifyProgListenerEnd(int value, int status) {
        log.debug("MrcOpsModeProgrammer adds 200mSec delay to response"); //IN18N
        try {
            wait(200);
        } catch (InterruptedException e) {
            log.debug("unexpected exception " + e); //IN18N
        }
        super.notifyProgListenerEnd(value, status);
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
    private final static Logger log = LoggerFactory.getLogger(MrcOpsModeProgrammer.class);

}
