/* MrcOpsModeProgrammer.java */

package jmri.jmrix.mrc;

import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.*;

/**
 * Provide an Ops Mode Programmer via a wrapper what works with the MRC command
 * station object.
 * <P>
 * Functionally, this just creates packets to send via the command station.
 *
 * @see             jmri.Programmer
 * @author			Bob Jacobsen Copyright (C) 2002
 * @author	Ken Cameron Copyright (C) 2014
 * @author  Kevin Dickerson Copyright (C) 2014
 * @version			$Revision: 24270 $
 */
public class MrcOpsModeProgrammer extends MrcProgrammer  {
	
    static ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.mrc.MrcOpsModeProgrammerBundle");

    int mAddress;
    boolean mLongAddr;
    
    public MrcOpsModeProgrammer(MrcTrafficController tc, int pAddress, boolean pLongAddr) {
    	super(tc);
        log.debug("MRC ops mode programmer "+pAddress+" "+pLongAddr);
        if(pLongAddr){
            addressLo = pAddress;
            addressHi = pAddress>>8;
            addressHi = addressHi + 0xc0; //We add 0xc0 to the high byte.
        } else {
            addressLo = pAddress;
        }
    }
    
    int addressLo = 0x00;
    int addressHi = 0x00;

    /**
     * Forward a write request to an ops-mode write operation
     */
    public synchronized void writeCV(int CV, int val, ProgListener p) throws ProgrammerException {
        log.debug("write CV={} val={}", CV, val);
        MrcMessage msg = MrcMessage.getPOM(addressLo, addressHi,CV, val);
        
        useProgrammer(p);
        _progRead = false;
        progState = POMCOMMANDSENT;
        _val = val;
        _cv = CV;
        
        // start the error timer
        startShortTimer();
        
        tc.sendMrcMessage(msg);
    }

    public synchronized void readCV(int CV, ProgListener p) throws ProgrammerException {
        log.debug("read CV={}", CV);
        log.error(rb.getString("LogMrcOpsModePgmReadCvModeError"));
        throw new ProgrammerException();
    }

    public synchronized void confirmCV(int CV, int val, ProgListener p) throws ProgrammerException {
        log.debug("confirm CV={}", CV);
        log.error(rb.getString("LogMrcOpsModeProgrammerConfirmCvModeError"));
        throw new ProgrammerException();
    }
    
    // add 200mSec between commands, so MRC command station queue doesn't get overrun
    protected void notifyProgListenerEnd(int value, int status) {
    	log.debug("MrcOpsModeProgrammer adds 200mSec delay to response");
		try{
			wait(200);
		}catch (InterruptedException e){
			log.debug("unexpected exception "+e);
		}
    	super.notifyProgListenerEnd(value, status);
    }

    public void setMode(int mode) {
        if (mode!=Programmer.OPSBYTEMODE)
            log.error(rb.getString("LogMrcOpsModeProgrammerModeSwitchError"), mode);
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
    static Logger log = LoggerFactory.getLogger(MrcOpsModeProgrammer.class.getName());

}

/* @(#)MrcOpsModeProgrammer.java */
