package jmri.jmrix.nce;

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
 * Provide an Ops Mode Programmer via a wrapper what works with the NCE command
 * station object.
 * <P>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see jmri.Programmer
 * @author Bob Jacobsen Copyright (C) 2002, 2014
 * @author kcameron Copyright (C) 2014
 */
public class NceOpsModeProgrammer extends NceProgrammer implements AddressedProgrammer {

    int mAddress;
    boolean mLongAddr;

    public NceOpsModeProgrammer(NceTrafficController tc, int pAddress, boolean pLongAddr) {
        super(tc);
        log.debug("NCE ops mode programmer " + pAddress + " " + pLongAddr);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
        setMode(ProgrammingMode.OPSBYTEMODE);
    }

    /**
     * Forward a write request to an ops-mode write operation
     */
    @Override
    public synchronized void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("write CV=" + CV + " val=" + val);
        }
        NceMessage msg;
        // USB can't send a NMRA packet, must use new ops mode command
        if (tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_POWERCAB
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB3
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_SB5
                || tc.getUsbSystem() == NceTrafficController.USB_SYSTEM_TWIN) {
            int locoAddr = mAddress;
            if (mLongAddr) {
                locoAddr += 0xC000;
            }
            byte[] bl = NceBinaryCommand.usbOpsModeLoco(tc, locoAddr, CV, val);
            msg = NceMessage.createBinaryMessage(tc, bl);

        } else {
            // create the message and fill it,
            byte[] contents = NmraPacket.opsCvWriteByte(mAddress, mLongAddr,
                    CV, val);
            if (contents == null) {
                throw new ProgrammerException();
            }
            msg = NceMessage.sendPacketMessage(tc, contents, 5); // retry 5 times
        }
        // record state. COMMANDSENT is just waiting for a reply...
        useProgrammer(p);
        _progRead = false;
        progState = COMMANDSENT_2;
        _val = val;
        _cv = CV;

        // start the error timer
        startShortTimer();

        // send it twice (2x5) so NCE CS will send at least two consecutive commands to decoder
        tc.sendNceMessage(msg, this);
        tc.sendNceMessage(msg, this);
    }

    @Override
    public synchronized void readCV(int CV, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("read CV=" + CV);
        }
        log.error("readCV not available in this protocol");
        throw new ProgrammerException();
    }

    @Override
    public synchronized void confirmCV(String CV, int val, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) {
            log.debug("confirm CV={}", CV);
        }
        log.error("confirmCV not available in this protocol");
        throw new ProgrammerException();
    }

    // add 200mSec between commands, so NCE command station queue doesn't get overrun
    @Override
    protected void notifyProgListenerEnd(int value, int status) {
        if (log.isDebugEnabled()) {
            log.debug("NceOpsModeProgrammer adds 200mSec delay to response");
        }
        try {
            wait(200);
        } catch (InterruptedException e) {
            log.debug("unexpected exception " + e);
        }
        super.notifyProgListenerEnd(value, status);
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

    /**
     * Ops-mode programming doesn't put the command station in programming mode,
     * so we don't have to send an exit-programming command at end. Therefore,
     * this routine does nothing except to replace the parent routine that does
     * something.
     */
    @Override
    void cleanup() {
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

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(NceOpsModeProgrammer.class);

}
