/* EasyDccOpsModeProgrammer.java */

package jmri.jmrix.easydcc;

import java.beans.*;

import jmri.*;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the EasyDcc command
 * station object.
 * <P>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see             jmri.Programmer
 * @author			Bob Jacobsen Copyright (C) 2002
 * @version			$Revision: 1.1 $
 */
public class EasyDccOpsModeProgrammer extends EasyDccProgrammer  {

    int mAddress;
    boolean mLongAddr;
    public EasyDccOpsModeProgrammer(int pAddress, boolean pLongAddr) {

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
        EasyDccMessage msg = new EasyDccMessage(4+3*contents.length);
        msg.setOpCode('S');
        msg.setElement(1, ' ');
        msg.setElement(2, '0');
        msg.setElement(3, '5');
        int j = 4;
        for (int i=0; i<contents.length; i++) {
            msg.setElement(j++, ' ');
            EasyDccMessage.addIntAsTwoHex(((int)contents[i])&0xFF, msg, j);
            j = j+2;
        }

        // record state.  RETURNSENT is just waiting for a reply...
        useProgrammer(p);
        _progRead = false;
        progState = RETURNSENT;
        _val = val;
        _cv = CV;

        // start the error timer
        startShortTimer();

        // send it
        controller().sendEasyDccMessage(msg, this);
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
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccOpsModeProgrammer.class.getName());

}

/* @(#)EasyDccOpsModeProgrammer.java */
