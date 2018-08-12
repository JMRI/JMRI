package jmri.jmrix.easydcc;

import java.util.ArrayList;
import java.util.List;
import jmri.AddressedProgrammer;
import jmri.NmraPacket;
import jmri.ProgListener;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an Ops Mode Programmer via a wrapper that works with the
 * EasyDccCommandStation object.
 * <p>
 * Functionally, this just creates packets to send via the Command Station.
 *
 * @see jmri.Programmer
 * @author Bob Jacobsen Copyright (C) 2002
 */
public class EasyDccOpsModeProgrammer extends EasyDccProgrammer implements AddressedProgrammer {

    int mAddress;
    boolean mLongAddr;

    public EasyDccOpsModeProgrammer(int pAddress, boolean pLongAddr, EasyDccSystemConnectionMemo memo) {
        super(memo);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /**
     * Forward a write request to an ops-mode write operation.
     */
    @Override
    public synchronized void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        log.debug("write CV={} val={}", CV, val);
        // create the message and fill it,
        byte[] contents = NmraPacket.opsCvWriteByte(mAddress, mLongAddr, CV, val);
        EasyDccMessage msg = new EasyDccMessage(4 + 3 * contents.length);
        msg.setOpCode('S');
        msg.setElement(1, ' ');
        msg.setElement(2, '0');
        msg.setElement(3, '5');
        int j = 4;
        for (int i = 0; i < contents.length; i++) {
            msg.setElement(j++, ' ');
            msg.addIntAsTwoHex(contents[i] & 0xFF, j);
            j = j + 2;
        }

        // record state.  COMMANDSENT is just waiting for a reply...
        useProgrammer(p);
        _progRead = false;
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

        // start the error timer
        startShortTimer();

        // send it
        controller().sendEasyDccMessage(msg, this);
    }

    @Override
    public synchronized void readCV(int CV, ProgListener p) throws ProgrammerException {
        log.debug("read CV={}", CV);
        log.error("readCV not available in this protocol");
        throw new ProgrammerException();
    }

    @Override
    public synchronized void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        log.debug("confirm CV={}", CV);
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

    /**
     * Can this ops-mode programmer read back values? For now, no, but maybe
     * later.
     *
     * @return always false for now
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
    @Override
    void cleanup() {
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(EasyDccOpsModeProgrammer.class);

}
