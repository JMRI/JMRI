/* TamsOpsModeProgrammer.java */

package jmri.jmrix.tams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the TAMS command
 * station object.
 * <P>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see             jmri.Programmer
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version			$Revision: 19990 $
 */
public class TamsOpsModeProgrammer extends TamsProgrammer  {

    int mAddress;
    boolean mLongAddr;
    
    public TamsOpsModeProgrammer(TamsTrafficController tc, int pAddress, boolean pLongAddr) {
    	super(tc);
        log.debug("TAMs ops mode programmer "+pAddress+" "+pLongAddr);
        mAddress = pAddress;
        mLongAddr = pLongAddr;
    }

    /**
     * Forward a write request to an ops-mode write operation
     */
    public synchronized void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
       if (log.isDebugEnabled()) log.debug("write CV="+CV+" val="+val);
        useProgrammer(p);
        _progRead = false;
        _val = val;
        _cv = CV;
        progState = COMMANDSENT;
        
        // start the error timer
        startShortTimer();

        // format and send the write message
        tc.sendTamsMessage(TamsMessage.getWriteOpsModeCVMsg(mAddress, CV, val), this);

    }

    public synchronized void readCV(int CV, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) log.debug("read CV="+CV);
        log.error("readCV not available in this protocol");
        throw new ProgrammerException();
    }

    public synchronized void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        if (log.isDebugEnabled()) log.debug("confirm CV="+CV);
        log.error("confirmCV not available in this protocol");
        throw new ProgrammerException();
    }
    
    // add 200mSec between commands, so NCE command station queue doesn't get overrun
    protected void notifyProgListenerEnd(int value, int status) {
    	if (log.isDebugEnabled()) log.debug("TamsOpsModeProgrammer adds 200mSec delay to response");
		try{
			wait(200);
		}catch (InterruptedException e){
			log.debug("unexpected exception "+e);
		}
    	super.notifyProgListenerEnd(value, status);
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
    static Logger log = LoggerFactory.getLogger(TamsOpsModeProgrammer.class.getName());

}

/* @(#)TamsOpsModeProgrammer.java */
