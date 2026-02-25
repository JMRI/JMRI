package jmri.jmrix.zimo;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import jmri.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Programmer support for Zimo MXULF operations mode.  
 * Provide an Ops Mode Programmer via a wrapper that works with the
 * MX1Programmer object.
 * <p>
 * Functionally, this just creates packets to send via the MXULF.
 *
 * @see jmri.Programmer
 * @author Bob Jacobsen Copyright (c) 2002
 *
 * Adapted by
 * @author Alger Pike Copyright (c) 2022
 * for use with zimo MXULF
 *
 */
public class Mx1OpsModeProgrammer extends Mx1Programmer implements AddressedProgrammer {

    int mAddress;
    boolean mLongAddr;

     public Mx1OpsModeProgrammer(int pAddress, boolean pLongAddr, Mx1TrafficController tc) {
        super(tc);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
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
    synchronized public void writeCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        if (log.isDebugEnabled()) {
            log.debug("writeCV {} listens {}", CV, p);
        }
        useProgrammer(p);
        _progRead = false;
        // set new state & save values
        progState = INQUIRESENT;
        _val = val;
        _cv = CV;
        // start the error timer
        startShortTimer();
        // format and send message to go to program mode
        if (getMode() == ProgrammingMode.OPSBYTEMODE) {
            if (tc.getProtocol() == Mx1Packetizer.ASCII) {
                // Not supporting ASCII protocol for now.
                throw new ProgrammerException();
            } else {
                tc.sendMx1Message(Mx1Message.getDecProgCmd(mAddress, _cv, val, true), this);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    synchronized public void readCV(String CVname, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);

        // The MXULF still can't read
        if (tc.getAdapterMemo().getConnectionType() != Mx1SystemConnectionMemo.KLUG) {
            log.debug("confirm CV={}", CV);
            log.error("confirmCV not available in this protocol");
            throw new ProgrammerException();
        }

        if (log.isDebugEnabled()) {
            log.debug("readCV {} listens {}", CV, p);
        }
        useProgrammer(p);
        _progRead = true;
        // set new state & save values
        progState = INQUIRESENT;
        _val = -1; // Read
        _cv = CV;
        // start the error timer
        startShortTimer();
        // format and send message to go to program mode
        if (getMode() == ProgrammingMode.OPSBYTEMODE) {
            if (tc.getProtocol() == Mx1Packetizer.ASCII) {
                // Not supporting ASCII protocol for now.
                throw new ProgrammerException();
            } else {
                tc.sendMx1Message(Mx1Message.getDecProgCmd(mAddress, _cv, _val, true), this);
            }
        }
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
     *
     * Can this ops-mode programmer read back values? The KLUG yes, the MXULF is WIP, so no
     *
     * @return true in case of KLUG, otherwise false
     */
    @Override
    public boolean getCanRead() {
        return tc.getAdapterMemo().getConnectionType() == Mx1SystemConnectionMemo.KLUG;
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

    private final static Logger log = LoggerFactory.getLogger(Mx1OpsModeProgrammer.class);

}
