// SRCPOpsModeProgrammer.java

package jmri.jmrix.srcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the SRCP command
 * station object.
 * <P>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see             jmri.Programmer
 * @author			Bob Jacobsen Copyright (C) 2002, 2008
 * @version			$Revision$
 */
public class SRCPOpsModeProgrammer extends SRCPProgrammer  {

    int mAddress;
    boolean mLongAddr;

    public SRCPOpsModeProgrammer(int pAddress, boolean pLongAddr,
           SRCPSystemConnectionMemo memo) {
        super(memo);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /**
     * Forward a write request to an ops-mode write operation
     */
    public void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) log.debug("write CV="+CV+" val="+val);
        // create the message and fill it,
        byte[] contents = NmraPacket.opsCvWriteByte(mAddress, mLongAddr, CV, val );
        SRCPMessage msg = new SRCPMessage(4+3*contents.length);
        msg.setOpCode('S');
        msg.setElement(1, ' ');
        msg.setElement(2, '0');
        msg.setElement(3, '5');
        int j = 4;
        for (int i=0; i<contents.length; i++) {
            msg.setElement(j++, ' ');
            msg.addIntAsTwoHex(contents[i]&0xFF, j);
            j = j+2;
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
        controller().sendSRCPMessage(msg, this);
    }

    public void readCV(int CV, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) log.debug("read CV="+CV);
        log.error("readCV not available in this protocol");
        throw new ProgrammerException();
    }

    public void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
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

    /**
     * Can this ops-mode programmer read back values?  For now, no,
     * but maybe later.
     * @return always false for now
     */
    @Override
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
    static Logger log = LoggerFactory.getLogger(SRCPOpsModeProgrammer.class.getName());

}

/* @(#)SRCPOpsModeProgrammer.java */
