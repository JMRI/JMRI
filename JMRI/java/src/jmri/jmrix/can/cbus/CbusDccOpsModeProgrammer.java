package jmri.jmrix.can.cbus;

import java.util.ArrayList;
import java.util.List;
import jmri.AddressedProgrammer;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import jmri.jmrix.can.CanReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the CBUS command
 * station object.
 * <P>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see jmri.Programmer
 * @author Andrew Crosland Copyright (C) 2009
 */
public class CbusDccOpsModeProgrammer extends CbusDccProgrammer implements AddressedProgrammer {

    int mAddress;
    boolean mLongAddr;
    private final static Logger log = LoggerFactory.getLogger(CbusDccOpsModeProgrammer.class);

    public CbusDccOpsModeProgrammer(int pAddress, boolean pLongAddr, jmri.jmrix.can.TrafficController tc) {
        super(tc);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /**
     * Forward a write request to an ops-mode write operation
     */
    @Override
    synchronized public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        log.debug("ops mode write CV=" + CV + " val=" + val);

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

    @Override
    synchronized public void readCV(int CV, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("read CV=" + CV);
        }
        log.error("readCV not available in this protocol");
        throw new ProgrammerException();
    }

    @Override
    synchronized public void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("confirm CV=" + CV);
        }
        log.error("confirmCV not available in this protocol");
        throw new ProgrammerException();
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

    @Override
    synchronized public void reply(CanReply m) {
        // We will not see any replies
    }

    /**
     * Can this ops-mode programmer read back values?
     *
     * @return always false
     */
    @Override
    public boolean getCanRead() {
        return false;
    }

    @Override
    public boolean getLongAddress() {
        return mLongAddr;
    }

    @Override
    public int getAddressNumber() {
        return mAddress;
    }

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

}
