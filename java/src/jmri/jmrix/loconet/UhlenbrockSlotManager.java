/* UhlenbrockSlotManager.java */

package jmri.jmrix.loconet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.CommandStation;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.jmrix.AbstractProgrammer;
import jmri.jmrix.loconet.*;


/**
 * Only change compared to standard Loconet SlotManager is CV programming.
 * The Uhlenbrock IB-COM / Intellibox II uses some special and undocumented means (OPC_IMM_PACKET
 * 
 * PC -> IB: BB 7F 00 3B                                     OPC_RQ_SL_DATA, 127(Command Station Options ), 0
 * IB -> PC: B4 3B 00 70                                     OPC_LONG_ACK, on OPC_RQ_SL_DATA, 0
 * 
 * # start of programming session
 * PC -> IB: E5 07 01 49 42 41 56                            OPC_PEER_XFER, src=7, dst=9345, ??
 * PC -> IB: 82 7D                                           OPC_GPOFF
 * 
 * # read cv 1                  R CV CV
 * PC -> IB: ED 1F 01 49 42 71 72 01 00 00 70 00 00 00 00 10 OPC_IMM_PACKET
 *           00 00 00 00 00 00 00 00 00 00 00 00 00 00 65
 * IB -> PC: B4 6D 01 27                                     OPC_LONG_ACK, on OPC_IMM_PACKET
 * # cv 1 has value 3                      VV
 * IB -> PC: E7 0E 7C 00 00 00 72 06 00 00 03 00 00 1D       OPC_SL_RD_DATA, len, PT slot,
 * 
 * # end off programming session
 * PC -> IB: E5 07 01 49 42 40 57                            OPC_PEER_XFER, src=7, dst=
 * 
 * 
 * # start of programming session
 * PC -> IB: E5 07 01 49 42 41 56                            OPC_PEER_XFER, src=7, dst=
 * PC -> IB: 82 7D                                           OPC_GPOFF
 * 
 * # write cv 1                 W CV CV VV
 * PC -> IB: ED 1F 01 49 42 71 71 01 00 03 70 00 00 00 00 10 OPC_IMM_PACKET
 *           00 00 00 00 00 00 00 00 00 00 00 00 00 00 65
 * IB -> PC: B4 6D 01 27                                     OPC_LONG_ACK, on OPC_IMM_PACKET
 * # cv 1 has value 3                      VV
 * IB -> PC: E7 0E 7C 00 00 00 71 06 00 00 03 00 00 1E       OPC_SL_RD_DATA, len, PT slot,
 * 
 * # end off programming session
 * PC -> IB: E5 07 01 49 42 40 57                            OPC_PEER_XFER, src=7, dst=
 * 
 * 
 * 
 * # write 254 in cv 27
 *                HB  W CV CV VV
 * ED 1F 01 49 42 79 71 1B 00 7E 70 00 00 00 00 10
 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0A
 * # HB = high bit for CV value
 * 
 * # response
 *                         HB    VV
 * E7 0E 7C 00 00 00 71 06 02 00 7E 00 00 61
 * 
 * 
 * 
 * # write 255 in cv 545
 *                HB  W CV CV VV
 * ED 1F 01 49 42 79 71 21 02 7F 70 00 00 00 00 10
 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 33
 * 
 * # read cv 393
 *                HB  R CV CV VV
 * ED 1F 01 49 42 73 72 09 02 00 70 00 00 00 00 10
 * 00 00 00 00 00 00 00 00 00 00 00 00 00 00 6D
 * 
 * 
 * <P>
 * @author	Lisby  Copyright (C) 2014
 * @version     $Revision: 27215 $
 */
public class UhlenbrockSlotManager extends SlotManager implements LocoNetListener, CommandStation {

    public UhlenbrockSlotManager(LnTrafficController tc) {
        super(tc);
    }

    /**
     * Internal method to create the LocoNetMessage for programmer task start
     */
    protected LocoNetMessage progTaskStart(int pcmd, int val, int cvnum, boolean write){
 //       new Exception("About to create read/write CV command for IB-COM. Call tree").printStackTrace();

        LocoNetMessage m = new LocoNetMessage(0x1F);
        int Element5;
        int Element7;
        int Element9;
//log.info("--------SENDING OPC_IMM_PACKET TO IB-COM PROGRAMMING TRACK. pcmd=" + pcmd + "val="+val+" cvnum="+cvnum+" write="+write);
        m.setOpCode(LnConstants.OPC_IMM_PACKET);
        m.setElement(1, 0x1F);
        m.setElement(2, 0x01);
        m.setElement(3, 0x49);
        m.setElement(4, 0x42);
        Element5 = 0x71;
        if (write) {
            //write
            m.setElement(6, 0x71);
            Element9 = val;
        } else {
            //read
            m.setElement(6, 0x72);
            Element9 = 0;
        }
        Element7 = cvnum % 256;
        m.setElement(8, cvnum / 256);
        m.setElement(10, 0x70);
        m.setElement(15, 0x10);
        
        if ((Element7 & 0x80) == 0x80) {
            Element5 |= 0x02;
            Element7 &= 0x7F;
        }
        if ((Element9 & 0x80) == 0x80) {
            Element5 |= 0x08;
            Element9 &= 0x7F;
        }
        m.setElement(5, Element5);
        m.setElement(7, Element7);
        m.setElement(9, Element9);
 
        return m;
    }
    
    /**
     * Internal method to create the LocoNetMessage for enabling programming track in IB-COM / Intellibox II
     * Note: This method is specific to Uhlenbrock
     */
    protected LocoNetMessage startIBComPT() {
 //       new Exception("About to initiate programming track for IB-COM. Call tree").printStackTrace();

        LocoNetMessage m = new LocoNetMessage(7);
//log.info("--------startIBComPT");        
        m.setOpCode(LnConstants.OPC_PEER_XFER);
        m.setElement(1, 0x07);
        m.setElement(2, 0x01);
        m.setElement(3, 0x49);
        m.setElement(4, 0x42);
        m.setElement(5, 0x41);
        return m;
    }

    /**
     * Internal method to create the LocoNetMessage for disabling programming track in IB-COM / Intellibox II
     * Note: This method is currently not used
     */
    protected LocoNetMessage stopIBComPT() {
 //       new Exception("About to stop using programming track for IB-COM. Call tree").printStackTrace();
        
        LocoNetMessage m = new LocoNetMessage(7);
        
        m.setOpCode(LnConstants.OPC_PEER_XFER);
        m.setElement(1, 0x07);
        m.setElement(2, 0x01);
        m.setElement(3, 0x49);
        m.setElement(4, 0x42);
        m.setElement(5, 0x40);
        return m;
    }
    
    // internal method to remember who's using the programmer
    // Note: Overridden in order to also call the startIBComPT method
    @Override
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        super.useProgrammer (p);
        tc.sendLocoNetMessage(startIBComPT());
    }
    
    // A couple of seconds after the last programming command, power is meant to be turned on.
    // However, for the Uhlenbrock IB-COM / Intellibox II, the command station is taken out of programming mode instead.
    @Override
    synchronized protected void doEndOfProgramming () {
        log.debug("Uhlenbrock doEndOfProgramming");
        tc.sendLocoNetMessage(stopIBComPT());
     }

    // initialize logging
    static Logger log = LoggerFactory.getLogger(UhlenbrockSlotManager.class.getName());

}
/* @(#)UhlenbrockSlotManager.java */
