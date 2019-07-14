package jmri.jmrix.ecos;

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
 * Provide an Ops Mode Programmer via a wrapper what works with the ECoS command
 * station object.
 * <p>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see jmri.Programmer Based on work by Bob Jacobsen
 * @author	Karl Johan Lisby Copyright (C) 2018
 */
public class EcosOpsModeProgrammer extends EcosProgrammer implements AddressedProgrammer {

    int mAddress;
    boolean mLongAddr;

    public EcosOpsModeProgrammer(EcosTrafficController tc, int pAddress, boolean pLongAddr) {
        super(tc);
        log.debug("ECoS ops mode programmer " + pAddress + " " + pLongAddr);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
        ecosObject = 7;
        readCommand  = "mode[readdccpomloco],addr["+pAddress+"]";
        writeCommand = "mode[writedccpomloco],addr["+pAddress+"]";
    }

    /**
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
     * Can this ops-mode programmer read back values? Yes - if the locomotive decoder supports it.
     *
     * @return always true
     */
    @Override
    public boolean getCanRead() {
        return true;
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
    private final static Logger log = LoggerFactory.getLogger(EcosOpsModeProgrammer.class);

}
