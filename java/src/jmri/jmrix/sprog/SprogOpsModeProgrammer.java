/* SprogOpsModeProgrammer.java */

package jmri.jmrix.sprog;

import org.apache.log4j.Logger;
import jmri.*;
import jmri.jmrix.sprog.SprogCommandStation;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the SPROG command
 * station object.
 * <P>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see             jmri.Programmer
 * @author			Andrew Crosland Copyright (C) 2006
 * @version			$Revision$
 */
public class SprogOpsModeProgrammer extends SprogProgrammer  {

    int mAddress;
    boolean mLongAddr;
    public SprogOpsModeProgrammer(int pAddress, boolean pLongAddr) {

        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /**
     * Forward a write request to an ops-mode write operation
     */
    synchronized public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        log.debug("write CV="+CV+" val="+val);

        // record state.  COMMANDSENT is just waiting for a reply...
        useProgrammer(p);
        _progRead = false;
        progState = COMMANDSENT;
        _val = val;
        _cv = CV;

        // Add the packet to the queue rather than send it directly
        SprogCommandStation.instance().opsModepacket(mAddress, mLongAddr, CV, val);
        notifyProgListenerEnd(_val, jmri.ProgListener.OK);
    }

    synchronized public void readCV(int CV, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) log.debug("read CV="+CV);
        log.error("readCV not available in this protocol");
        throw new ProgrammerException();
    }

    synchronized public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) log.debug("confirm CV="+CV);
        log.error("confirmCV not available in this protocol");
        throw new ProgrammerException();
    }

    public void setMode(int mode) {
        if (mode!=Programmer.OPSBYTEMODE)
            log.error("Can't switch to mode "+mode);
    }

    public int  getMode() {
        return Programmer.OPSBYTEMODE;
    }

    public boolean hasMode(int mode) {
        return (mode==Programmer.OPSBYTEMODE);
    }

    synchronized public void notifyReply(SprogReply m) {
        // We will not see any replies
    }

    /**
     * Can this ops-mode programmer read back values?  For now, no,
     * but maybe later.
     * @return always false for now
     */
    public boolean getCanRead() {
        return false;
    }


    /**
     * Ops-mode programming doesn't put the command station in programming
     * mode, so we don't have to send an exit-programming command at end.
     * Therefore, this routine does nothing except to replace the parent
     * routine that does something.
     */
    void cleanup() {
    }

    // initialize logging
    static Logger log = Logger.getLogger(SprogOpsModeProgrammer.class.getName());

}

/* @(#)SprogOpsModeProgrammer.java */
