package jmri.jmrix.can.cbus;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jmri.jmrix.can.CanMessage;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusOpCodesTest {

    @Test
    public void testCTor() {
        CbusOpCodes t = new CbusOpCodes();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testDecode() {
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_RESTP },0x12 ); // request e stop
        Assert.assertEquals("CbusOpCodes.decode","",CbusOpCodes.decode(m));
        Assert.assertEquals("CbusOpCodes.decodeopc","RESTP",CbusOpCodes.decodeopc(m));

        m.setElement(0, 0x18);
        Assert.assertEquals("0x18 no current opc definition","",CbusOpCodes.decode(m));
        m.setElement(0, CbusConstants.CBUS_DKEEP);
        m.setElement(1, 0x04);
        Assert.assertEquals("CBUS_DKEEP","Session: 4",CbusOpCodes.decode(m));

        m.setElement(0, CbusConstants.CBUS_RLOC);
        m.setElement(1, 0x00);
        m.setElement(2, 0x2c);
        Assert.assertEquals("CBUS_RLOC","Addr: 44(S)",CbusOpCodes.decode(m));

        
     
        m.setElement(0, CbusConstants.CBUS_ERR);
        m.setElement(1, 0xcc);
        m.setElement(2, 0x8f);
        m.setElement(3, 0x01);
        Assert.assertEquals("CBUS_ERR 1","Loco stack full for address 3215(L)",CbusOpCodes.decode(m));
        m.setElement(3, 0x02);
        Assert.assertEquals("CBUS_ERR 2","Loco address 3215(L) taken",CbusOpCodes.decode(m));
        m.setElement(3, 0x03);
        Assert.assertEquals("CBUS_ERR 3","Session 204 not present on Command Station",CbusOpCodes.decode(m));
        m.setElement(3, 0x04);
        Assert.assertEquals("CBUS_ERR 4","Consist empty for consist 204",CbusOpCodes.decode(m));
        m.setElement(3, 0x05);
        Assert.assertEquals("CBUS_ERR 5","Loco not found for session 204",CbusOpCodes.decode(m));
        m.setElement(3, 0x06);
        Assert.assertEquals("CBUS_ERR 6","CAN bus error ",CbusOpCodes.decode(m));
        m.setElement(3, 0x07);
        Assert.assertEquals("CBUS_ERR 7","Invalid request for address 3215(L)",CbusOpCodes.decode(m));
        m.setElement(3, 0x08);
        Assert.assertEquals("CBUS_ERR 8","Throttle cancelled for session 204",CbusOpCodes.decode(m));
        m.setElement(3, 0x09);
        Assert.assertEquals("CBUS_ERR 9","",CbusOpCodes.decode(m));
    }
    
    @Test
    public void testNodeEventMessage() {
    
        CanMessage m = new CanMessage( 0x12 );
        m.setElement(0, CbusConstants.CBUS_ACON);
        m.setElement(1, 0x01);
        m.setElement(2, 0x02);
        m.setElement(3, 0xd4);
        m.setElement(4, 0xac);
        Assert.assertEquals("CBUS_ACON","NN:258 EN:54444 ",CbusOpCodes.decode(m));
    
    }
    
    @Test
    public void testDecodeCMDERR() {
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_CMDERR,12,34,01 },0x12  );
        Assert.assertEquals("CBUS_CMDERR 1","NN:3106 ERROR : Command Not Supported.",CbusOpCodes.decode(m));
        m.setElement(3, 0xaa);
        Assert.assertEquals("CBUS_CMDERR aa","NN:3106 ",CbusOpCodes.decode(m));
    }    

    @Test
    public void testDecodeextend() {
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_CMDERR,12,34,01 },0x12  );
        Assert.assertEquals("CBUS_CMDERR false1","NN:3106 ERROR : Command Not Supported.",CbusOpCodes.decode(m));
        Assert.assertTrue("m known",CbusOpCodes.isKnownOpc(m));
        m.setExtended(true);
        Assert.assertFalse("m not known when extended",CbusOpCodes.isKnownOpc(m));
        Assert.assertEquals("CBUS_CMDERR true",Bundle.getMessage("decodeUnknownExtended"),CbusOpCodes.decode(m));
    }

    @Test
    public void testdecodeopc() {
        CanMessage m = new CanMessage( new int[]{0x18 },0x12  );
        Assert.assertEquals("decodeopc 1","Reserved opcode",CbusOpCodes.decodeopc(m));
        Assert.assertEquals("decodeopc 2","Reserved opcode",CbusOpCodes.decodeopc(m));
        m.setExtended(true);
        Assert.assertTrue("decodeopc 3",CbusOpCodes.decodeopc(m).isEmpty());
    }
    
    @Test
    public void testDecodeopc() {
        CanMessage m = new CanMessage(1,11);
        for ( int i = 0; (i<258); i++ ) {
            m.setElement(0, i);
            if (OPCMAP.containsKey(i)) {
                Assert.assertEquals("opc short text "+i,OPCMAP.get(i),CbusOpCodes.decodeopc(m));
            } else {
                Assert.assertEquals("opc short text "+i,Bundle.getMessage("OPC_RESERVED"),CbusOpCodes.decodeopc(m));
            }
        }
    }
    
    @Test
    public void testextendedFrameTranslation(){
    
        CanMessage m = new CanMessage( new int[]{5,1,2,3,0x0d,0,6,7},0x04  );
        m.setExtended(true);
        Assert.assertEquals("extended 4  0","Bootloader: Do nothing",CbusOpCodes.decode(m));
        
        m.setElement(5, 1);
        Assert.assertEquals("extended 4  1","Bootloader: Issue soft reset, leave boot mode",CbusOpCodes.decode(m));
        
        m.setElement(5, 2);
        Assert.assertEquals("extended 4  2","Bootloader: Reset checksum to 131,333 and verify",CbusOpCodes.decode(m));
        
        m.setElement(5, 3);
        Assert.assertEquals("extended 4  3","Bootloader: Boot Check with checksum 1,798",CbusOpCodes.decode(m));
        
        m.setElement(5, 4);
        Assert.assertEquals("extended 4  4","Bootloader: Verify boot mode",CbusOpCodes.decode(m));
    
        m.setElement(5, 5);
        Assert.assertEquals("extended 4  5","Bootloader: Request device ID",CbusOpCodes.decode(m));
        
        m.setElement(5, 6);
        Assert.assertEquals("extended 4  6","Bootloader: Request bootloader ID",CbusOpCodes.decode(m));
        
        m.setElement(5, 7);
        Assert.assertEquals("extended 4  7","Bootloader: Memory write enables",CbusOpCodes.decode(m));
        
        m.setElement(5, 8);
        Assert.assertEquals("extended 4  8","Unknown Extended Frame",CbusOpCodes.decode(m));
        
        m.setHeader(5);
        Assert.assertEquals("extended 5 data","Bootloader: Data : 05 01 02 03 0D 08 06 07",CbusOpCodes.decode(m));
        
        
        m = new CanMessage( new int[]{0},0x10000004 );
        m.setExtended(true);
        Assert.assertEquals("extended 10000004 0","Bootloader: Boot Error",CbusOpCodes.decode(m));
        
        m.setElement(0, 1);
        Assert.assertEquals("extended 10000004 1","Bootloader: Boot OK",CbusOpCodes.decode(m));
        
        m.setElement(0, 2);
        Assert.assertEquals("extended 10000004 1","Bootloader: Boot Confirm",CbusOpCodes.decode(m));
        
        m.setElement(0, 3);
        Assert.assertEquals("extended 10000004 1","Unknown Extended Frame",CbusOpCodes.decode(m));
        
    }
    
    @Test
    public void testGlocTranslate(){
    
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_GLOC,02,03,00},123 );
        Assert.assertEquals("GLOC 0","Addr: 515(S) Flags: 0 Standard Request",CbusOpCodes.decode(m));
    
        m.setElement(3, 1);
        Assert.assertEquals("GLOC 1","Addr: 515(S) Flags: 1 Steal Request",CbusOpCodes.decode(m));
        
        m.setElement(3, 2);
        Assert.assertEquals("GLOC 2","Addr: 515(S) Flags: 2 Share Request",CbusOpCodes.decode(m));
        
        m.setElement(3, 3);
        Assert.assertEquals("GLOC 3","Addr: 515(S) Flags: 3 Invalid Flags",CbusOpCodes.decode(m));
        
    }
    
    @Test
    public void testGetMinPriority() {
        Assert.assertTrue("Priority Fetched",CbusOpCodes.getOpcMinPriority(CbusConstants.CBUS_RTON)==1);
        Assert.assertTrue("Default priority for unknown",CbusOpCodes.getOpcMinPriority(0x0f)==3); // unknown OPC
    }
    
    @Test
    public void testKnownOPC() {
        CanMessage m = new CanMessage(1,11);
        m.setElement(0, CbusConstants.CBUS_ASOF);
        Assert.assertTrue("m known",CbusOpCodes.isKnownOpc(m));
        m.setElement(0, 0x0f); // unknown OPC
        Assert.assertFalse("m NOT known",CbusOpCodes.isKnownOpc(m));
    
    }
    
    private static final Map<Integer, String> OPCMAP = createoMap();

    private static Map<Integer, String> createoMap() {
        Map<Integer, String> result = new HashMap<>();
        // Opcodes with no data
        result.put(CbusConstants.CBUS_ACK, "ACK"); // NOI18N
        result.put(CbusConstants.CBUS_NAK, "NAK"); // NOI18N
        result.put(CbusConstants.CBUS_HLT, "HLT"); // NOI18N
        result.put(CbusConstants.CBUS_BON, "BON"); // NOI18N
        result.put(CbusConstants.CBUS_TOF, "TOF"); // NOI18N
        result.put(CbusConstants.CBUS_TON, "TON"); // NOI18N
        result.put(CbusConstants.CBUS_ESTOP, "ESTOP"); // NOI18N
        result.put(CbusConstants.CBUS_ARST, "ARST"); // NOI18N
        result.put(CbusConstants.CBUS_RTOF, "RTOF"); // NOI18N
        result.put(CbusConstants.CBUS_RTON, "RTON"); // NOI18N
        result.put(CbusConstants.CBUS_RESTP, "RESTP"); // NOI18N
        result.put(CbusConstants.CBUS_RSTAT, "RSTAT"); // NOI18N
        result.put(CbusConstants.CBUS_QNN,   "QNN"); // NOI18N
        result.put(CbusConstants.CBUS_RQNP,  "RQNP"); // NOI18N
        result.put(CbusConstants.CBUS_RQMN,  "RQMN"); // NOI18N
        result.put(CbusConstants.CBUS_KLOC,  "KLOC"); // NOI18N
        result.put(CbusConstants.CBUS_QLOC,  "QLOC"); // NOI18N
        result.put(CbusConstants.CBUS_DKEEP, "DKEEP"); // NOI18N
        result.put(CbusConstants.CBUS_DBG1,  "DBG1"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC,  "EXTC"); // NOI18N
        result.put(CbusConstants.CBUS_RLOC,  "RLOC"); // NOI18N
        result.put(CbusConstants.CBUS_QCON,  "QCON"); // NOI18N
        result.put(CbusConstants.CBUS_SNN,   "SNN"); // NOI18N
        result.put(CbusConstants.CBUS_ALOC,  "ALOC"); // NOI18N
        result.put(CbusConstants.CBUS_STMOD, "STMOD"); // NOI18N
        result.put(CbusConstants.CBUS_PCON,  "PCON"); // NOI18N
        result.put(CbusConstants.CBUS_KCON,  "KCON"); // NOI18N
        result.put(CbusConstants.CBUS_DSPD,  "DSPD"); // NOI18N
        result.put(CbusConstants.CBUS_DFLG,  "DFLG"); // NOI18N
        result.put(CbusConstants.CBUS_DFNON, "DFNON"); // NOI18N
        result.put(CbusConstants.CBUS_DFNOF, "DFNOF"); // NOI18N
        result.put(CbusConstants.CBUS_SSTAT, "SSTAT"); // NOI18N
        result.put(CbusConstants.CBUS_NNRSM, "NNRSM"); // NOI18N
        result.put(CbusConstants.CBUS_RQNN,  "RQNN"); // NOI18N
        result.put(CbusConstants.CBUS_NNREL, "NNREL"); // NOI18N
        result.put(CbusConstants.CBUS_NNACK, "NNACK"); // NOI18N
        result.put(CbusConstants.CBUS_NNLRN, "NNLRN"); // NOI18N
        result.put(CbusConstants.CBUS_NNULN, "NNULN"); // NOI18N
        result.put(CbusConstants.CBUS_NNCLR, "NNCLR"); // NOI18N
        result.put(CbusConstants.CBUS_NNEVN, "NNEVN"); // NOI18N
        result.put(CbusConstants.CBUS_NERD,  "NERD"); // NOI18N
        result.put(CbusConstants.CBUS_RQEVN, "RQEVN"); // NOI18N
        result.put(CbusConstants.CBUS_WRACK, "WRACK"); // NOI18N
        result.put(CbusConstants.CBUS_RQDAT, "RQDAT"); // NOI18N
        result.put(CbusConstants.CBUS_RQDDS, "RQDDS"); // NOI18N
        result.put(CbusConstants.CBUS_BOOTM, "BOOTM"); // NOI18N
        result.put(CbusConstants.CBUS_ENUM, "ENUM"); // NOI18N
        result.put(CbusConstants.CBUS_NNRST,"NNRST"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC1, "EXTC1"); // NOI18N
        result.put(CbusConstants.CBUS_DFUN, "DFUN"); // NOI18N
        result.put(CbusConstants.CBUS_GLOC, "GLOC"); // NOI18N
        result.put(CbusConstants.CBUS_ERR, "ERR"); // NOI18N
        result.put(CbusConstants.CBUS_CMDERR, "CMDERR"); // NOI18N
        result.put(CbusConstants.CBUS_EVNLF, "EVNLF"); // NOI18N
        result.put(CbusConstants.CBUS_NVRD, "NVRD"); // NOI18N
        result.put(CbusConstants.CBUS_NENRD, "NENRD"); // NOI18N
        result.put(CbusConstants.CBUS_RQNPN, "RQNPN"); // NOI18N
        result.put(CbusConstants.CBUS_NUMEV, "NUMEV"); // NOI18N
        result.put(CbusConstants.CBUS_CANID, "CANID"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC2, "EXTC2"); // NOI18N
        result.put(CbusConstants.CBUS_RDCC3, "RDCC3"); // NOI18N
        result.put(CbusConstants.CBUS_WCVO, "WCVO"); // NOI18N
        result.put(CbusConstants.CBUS_WCVB, "WCVB"); // NOI18N
        result.put(CbusConstants.CBUS_QCVS, "QCVS"); // NOI18N
        result.put(CbusConstants.CBUS_PCVS, "PCVS"); // NOI18N
        result.put(CbusConstants.CBUS_ACON, "ACON"); // NOI18N
        result.put(CbusConstants.CBUS_ACOF, "ACOF"); // NOI18N
        result.put(CbusConstants.CBUS_AREQ, "AREQ"); // NOI18N
        result.put(CbusConstants.CBUS_ARON, "ARON"); // NOI18N
        result.put(CbusConstants.CBUS_AROF, "AROF"); // NOI18N
        result.put(CbusConstants.CBUS_EVULN, "EVULN"); // NOI18N
        result.put(CbusConstants.CBUS_NVSET, "NVSET"); // NOI18N
        result.put(CbusConstants.CBUS_NVANS, "NVANS"); // NOI18N
        result.put(CbusConstants.CBUS_ASON, "ASON"); // NOI18N
        result.put(CbusConstants.CBUS_ASOF, "ASOF"); // NOI18N
        result.put(CbusConstants.CBUS_ASRQ, "ASRQ"); // NOI18N
        result.put(CbusConstants.CBUS_PARAN, "PARAN"); // NOI18N
        result.put(CbusConstants.CBUS_REVAL, "REVAL"); // NOI18N
        result.put(CbusConstants.CBUS_ARSON, "ARSON"); // NOI18N
        result.put(CbusConstants.CBUS_ARSOF, "ARSOF"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC3, "EXTC3"); // NOI18N
        result.put(CbusConstants.CBUS_RDCC4, "RDCC4"); // NOI18N
        result.put(CbusConstants.CBUS_WCVS, "WCVS"); // NOI18N
        result.put(CbusConstants.CBUS_VCVS, "VCVS"); // NOI18N
        result.put(CbusConstants.CBUS_CABDAT, "CABDAT"); // NOI18N
        result.put(CbusConstants.CBUS_ACON1, "ACON1"); // NOI18N
        result.put(CbusConstants.CBUS_ACOF1, "ACOF1"); // NOI18N
        result.put(CbusConstants.CBUS_REQEV, "REQEV"); // NOI18N
        result.put(CbusConstants.CBUS_ARON1, "ARON1"); // NOI18N
        result.put(CbusConstants.CBUS_AROF1, "AROF1"); // NOI18N
        result.put(CbusConstants.CBUS_NEVAL, "NEVAL"); // NOI18N
        result.put(CbusConstants.CBUS_PNN, "PNN"); // NOI18N
        result.put(CbusConstants.CBUS_ASON1, "ASON1"); // NOI18N
        result.put(CbusConstants.CBUS_ASOF1, "ASOF1"); // NOI18N
        result.put(CbusConstants.CBUS_ARSON1, "ARSON1"); // NOI18N
        result.put(CbusConstants.CBUS_ARSOF1, "ARSOF1"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC4, "EXTC4"); // NOI18N
        result.put(CbusConstants.CBUS_RDCC5, "RDCC5"); // NOI18N
        result.put(CbusConstants.CBUS_WCVOA, "WCVOA"); // NOI18N
        result.put(CbusConstants.CBUS_FCLK, "FCLK"); // NOI18N
        result.put(CbusConstants.CBUS_ACON2, "ACON2"); // NOI18N
        result.put(CbusConstants.CBUS_ACOF2, "ACOF2"); // NOI18N
        result.put(CbusConstants.CBUS_EVLRN, "EVLRN"); // NOI18N
        result.put(CbusConstants.CBUS_EVANS, "EVANS"); // NOI18N
        result.put(CbusConstants.CBUS_ARON2, "ARON2"); // NOI18N
        result.put(CbusConstants.CBUS_AROF2, "AROF2"); // NOI18N
        result.put(CbusConstants.CBUS_ASON2, "ASON2"); // NOI18N
        result.put(CbusConstants.CBUS_ASOF2, "ASOF2"); // NOI18N
        result.put(CbusConstants.CBUS_ARSON2, "ARSON2"); // NOI18N
        result.put(CbusConstants.CBUS_ARSOF2, "ARSOF2"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC5, "EXTC5"); // NOI18N
        result.put(CbusConstants.CBUS_RDCC6, "RDCC6"); // NOI18N
        result.put(CbusConstants.CBUS_PLOC, "PLOC"); // NOI18N
        result.put(CbusConstants.CBUS_NAME, "NAME"); // NOI18N
        result.put(CbusConstants.CBUS_STAT, "STAT"); // NOI18N
        result.put(CbusConstants.CBUS_PARAMS, "PARAMS"); // NOI18N
        result.put(CbusConstants.CBUS_ACON3, "ACON3"); // NOI18N
        result.put(CbusConstants.CBUS_ACOF3, "ACOF3"); // NOI18N
        result.put(CbusConstants.CBUS_ENRSP, "ENRSP"); // NOI18N
        result.put(CbusConstants.CBUS_ARON3, "ARON3"); // NOI18N
        result.put(CbusConstants.CBUS_AROF3, "AROF3"); // NOI18N
        result.put(CbusConstants.CBUS_EVLRNI, "EVLRNI"); // NOI18N
        result.put(CbusConstants.CBUS_ACDAT, "ACDAT"); // NOI18N
        result.put(CbusConstants.CBUS_ARDAT, "ARDAT"); // NOI18N
        result.put(CbusConstants.CBUS_ASON3, "ASON3"); // NOI18N
        result.put(CbusConstants.CBUS_ASOF3, "ASOF3"); // NOI18N
        result.put(CbusConstants.CBUS_DDES, "DDES"); // NOI18N
        result.put(CbusConstants.CBUS_DDRS, "DDRS"); // NOI18N
        result.put(CbusConstants.CBUS_DDWS, "DDWS"); // NOI18N
        result.put(CbusConstants.CBUS_ARSON3, "ARSON3"); // NOI18N
        result.put(CbusConstants.CBUS_ARSOF3, "ARSOF3"); // NOI18N
        result.put(CbusConstants.CBUS_EXTC6, "EXTC6"); // NOI18N
        
        return Collections.unmodifiableMap(result);
    }
    
    @Test
    public void testAllOpcForEvents() {
        for ( int i = 0; (i<256); i++ ) {
            if (eventOpcodes.contains(i) ) {
                Assert.assertTrue("opc is event "+i,CbusOpCodes.isEvent(i));
            } else {
                Assert.assertFalse("opc not event "+i,CbusOpCodes.isEvent(i));
            }
        }
    }
    
    @Test
    public void testisEventNotRequest() {
        for ( int i = 0; (i<256); i++ ) {
            if (eventNotRequestOpCodes.contains(i) ) {
                Assert.assertTrue("opc is event "+i,CbusOpCodes.isEventNotRequest(i));
            } else {
                Assert.assertFalse("opc not event or request "+i,CbusOpCodes.isEventNotRequest(i));
            }
        }
    }
    
    @Test
    public void testisDcc() {
        for ( int i = 0; (i<256); i++ ) {
            if (dccOpcodes.contains(i) ) {
                Assert.assertTrue("opc is dcc "+i,CbusOpCodes.isDcc(i));
            } else {
                Assert.assertFalse("opc not dcc "+i,CbusOpCodes.isDcc(i));
            }
        }
    }
    
    @Test
    public void testisOnEvent() {
        for ( int i = 0; (i<256); i++ ) {
            if (onEvOpcodes.contains(i) ) {
                Assert.assertTrue("opc is on event "+i,CbusOpCodes.isOnEvent(i));
            } else {
                Assert.assertFalse("opc not on event "+i,CbusOpCodes.isOnEvent(i));
            }
        }
    }
    
    @Test
    public void testisEventRequest() {
        for ( int i = 0; (i<256); i++ ) {
            if (evRequestOpcodes.contains(i) ) {
                Assert.assertTrue("opc is request "+i,CbusOpCodes.isEventRequest(i));
            } else {
                Assert.assertFalse("opc not request "+i,CbusOpCodes.isEventRequest(i));
            }
        }
    }
    
    @Test
    public void testisShortEvent() {
        for ( int i = 0; (i<256); i++ ) {
            if (shortOpcodes.contains(i) ) {
                Assert.assertTrue("opc is request "+i,CbusOpCodes.isShortEvent(i));
            } else {
                Assert.assertFalse("opc not request "+i,CbusOpCodes.isShortEvent(i));
            }
        }
    }
    
    
    private static final Set<Integer> eventOpcodes = createEventOPC();
    
    private static Set<Integer> createEventOPC() {
        Set<Integer> result = new HashSet<>();

        result.add(CbusConstants.CBUS_ACON);
        result.add(CbusConstants.CBUS_ACOF);
        result.add(CbusConstants.CBUS_AREQ);
        result.add(CbusConstants.CBUS_ARON);
        result.add(CbusConstants.CBUS_AROF);
        result.add(CbusConstants.CBUS_ASON);
        result.add(CbusConstants.CBUS_ASOF);
        result.add(CbusConstants.CBUS_ASRQ);
        result.add(CbusConstants.CBUS_ARSON);
        result.add(CbusConstants.CBUS_ARSOF);
        
        result.add(CbusConstants.CBUS_ACON1);
        result.add(CbusConstants.CBUS_ACOF1);
        result.add(CbusConstants.CBUS_ARON1);
        result.add(CbusConstants.CBUS_AROF1);
        result.add(CbusConstants.CBUS_ASON1);
        result.add(CbusConstants.CBUS_ASOF1);
        result.add(CbusConstants.CBUS_ARSON1);
        result.add(CbusConstants.CBUS_ARSOF1);
        
        result.add(CbusConstants.CBUS_ACON2);
        result.add(CbusConstants.CBUS_ACOF2);
        result.add(CbusConstants.CBUS_ARON2);
        result.add(CbusConstants.CBUS_AROF2);        
        result.add(CbusConstants.CBUS_ASON2);        
        result.add(CbusConstants.CBUS_ASOF2);
        result.add(CbusConstants.CBUS_ARSON2);
        result.add(CbusConstants.CBUS_ARSOF2);
        
        result.add(CbusConstants.CBUS_ACON3);
        result.add(CbusConstants.CBUS_ACOF3);
        result.add(CbusConstants.CBUS_ARON3);
        result.add(CbusConstants.CBUS_AROF3);
        result.add(CbusConstants.CBUS_ASON3);
        result.add(CbusConstants.CBUS_ASOF3);
        result.add(CbusConstants.CBUS_ARSON3);
        result.add(CbusConstants.CBUS_ARSOF3);
        
        return Collections.unmodifiableSet(result);
    }

    private static final Set<Integer> eventNotRequestOpCodes = createEventNROPC();

    /*
     * Populate hashset with list of event opcodes
     * Excludes fastclock + response requests.
     */
    private static Set<Integer> createEventNROPC() {
        Set<Integer> result = new HashSet<>();

        result.add(CbusConstants.CBUS_ACON);
        result.add(CbusConstants.CBUS_ACOF);
        result.add(CbusConstants.CBUS_ARON);
        result.add(CbusConstants.CBUS_AROF);
        result.add(CbusConstants.CBUS_ASON);
        result.add(CbusConstants.CBUS_ASOF);
        result.add(CbusConstants.CBUS_ARSON);
        result.add(CbusConstants.CBUS_ARSOF);
        
        result.add(CbusConstants.CBUS_ACON1);
        result.add(CbusConstants.CBUS_ACOF1);
        result.add(CbusConstants.CBUS_ARON1);
        result.add(CbusConstants.CBUS_AROF1);
        result.add(CbusConstants.CBUS_ASON1);
        result.add(CbusConstants.CBUS_ASOF1);
        result.add(CbusConstants.CBUS_ARSON1);
        result.add(CbusConstants.CBUS_ARSOF1);
        
        result.add(CbusConstants.CBUS_ACON2);
        result.add(CbusConstants.CBUS_ACOF2);
        result.add(CbusConstants.CBUS_ARON2);
        result.add(CbusConstants.CBUS_AROF2);        
        result.add(CbusConstants.CBUS_ASON2);        
        result.add(CbusConstants.CBUS_ASOF2);
        result.add(CbusConstants.CBUS_ARSON2);
        result.add(CbusConstants.CBUS_ARSOF2);
        
        result.add(CbusConstants.CBUS_ACON3);
        result.add(CbusConstants.CBUS_ACOF3);
        result.add(CbusConstants.CBUS_ARON3);
        result.add(CbusConstants.CBUS_AROF3);
        result.add(CbusConstants.CBUS_ASON3);
        result.add(CbusConstants.CBUS_ASOF3);
        result.add(CbusConstants.CBUS_ARSON3);
        result.add(CbusConstants.CBUS_ARSOF3);
        
        return Collections.unmodifiableSet(result);
    }

    private static final Set<Integer> dccOpcodes = createDccOPC();

    private static Set<Integer> createDccOPC() {
        Set<Integer> result = new HashSet<>();

        result.add(CbusConstants.CBUS_TOF);
        result.add(CbusConstants.CBUS_TON);
        result.add(CbusConstants.CBUS_ESTOP);
        result.add(CbusConstants.CBUS_RTOF);
        result.add(CbusConstants.CBUS_RTON);
        result.add(CbusConstants.CBUS_RESTP);
        result.add(CbusConstants.CBUS_KLOC);
        result.add(CbusConstants.CBUS_QLOC);
        result.add(CbusConstants.CBUS_DKEEP);
        
        result.add(CbusConstants.CBUS_RLOC);
        result.add(CbusConstants.CBUS_QCON);
        result.add(CbusConstants.CBUS_ALOC);
        result.add(CbusConstants.CBUS_STMOD);
        result.add(CbusConstants.CBUS_PCON);
        result.add(CbusConstants.CBUS_KCON);
        result.add(CbusConstants.CBUS_DSPD);
        result.add(CbusConstants.CBUS_DFLG);
        result.add(CbusConstants.CBUS_DFNON);
        result.add(CbusConstants.CBUS_DFNOF);
        result.add(CbusConstants.CBUS_SSTAT);
        
        result.add(CbusConstants.CBUS_DFUN);
        result.add(CbusConstants.CBUS_GLOC);
        result.add(CbusConstants.CBUS_ERR);
        
        result.add(CbusConstants.CBUS_RDCC3);        
        result.add(CbusConstants.CBUS_WCVO);        
        result.add(CbusConstants.CBUS_WCVB);
        result.add(CbusConstants.CBUS_QCVS);
        result.add(CbusConstants.CBUS_PCVS);
        
        result.add(CbusConstants.CBUS_RDCC4);
        result.add(CbusConstants.CBUS_WCVS);
        result.add(CbusConstants.CBUS_VCVS);
        
        result.add(CbusConstants.CBUS_RDCC5);
        result.add(CbusConstants.CBUS_WCVOA);
        
        result.add(CbusConstants.CBUS_RDCC6);
        result.add(CbusConstants.CBUS_PLOC);
        result.add(CbusConstants.CBUS_STAT);
        result.add(CbusConstants.CBUS_RSTAT);
        
        return Collections.unmodifiableSet(result);
    }

    private static final Set<Integer> onEvOpcodes = createOnEv();
    
    private static Set<Integer> createOnEv() {
        Set<Integer> result = new HashSet<>();
        // Opcodes with 4 data
        result.add(CbusConstants.CBUS_ACON);
        result.add(CbusConstants.CBUS_ARON);
        result.add(CbusConstants.CBUS_ASON);
        result.add(CbusConstants.CBUS_ARSON);

        // Opcodes with 5 data
        result.add(CbusConstants.CBUS_ACON1);
        result.add(CbusConstants.CBUS_ARON1);
        result.add(CbusConstants.CBUS_ASON1);
        result.add(CbusConstants.CBUS_ARSON1);

        // Opcodes with 6 data
        result.add(CbusConstants.CBUS_ACON2);
        result.add(CbusConstants.CBUS_ARON2);
        result.add(CbusConstants.CBUS_ASON2);
        result.add(CbusConstants.CBUS_ARSON2);

        // Opcodes with 7 data
        result.add(CbusConstants.CBUS_ACON3);
        result.add(CbusConstants.CBUS_ARON3);
        result.add(CbusConstants.CBUS_ASON3);
        result.add(CbusConstants.CBUS_ARSON3);

        return Collections.unmodifiableSet(result);
    }

    private static final Set<Integer> evRequestOpcodes = createRequests();
    
    private static Set<Integer> createRequests() {
        Set<Integer> result = new HashSet<>();
        // Opcodes with 4 data
        result.add(CbusConstants.CBUS_AREQ);
        result.add(CbusConstants.CBUS_ASRQ);

        return Collections.unmodifiableSet(result);
    }

    private static final Set<Integer> shortOpcodes = createShort();
    
    private static Set<Integer> createShort() {
        Set<Integer> result = new HashSet<>();
        // Opcodes with 4 data
        result.add(CbusConstants.CBUS_ASON);
        result.add(CbusConstants.CBUS_ASOF);
        result.add(CbusConstants.CBUS_ASRQ);
        result.add(CbusConstants.CBUS_ARSON);
        result.add(CbusConstants.CBUS_ARSOF);

        // Opcodes with 5 data
        result.add(CbusConstants.CBUS_ASON1);
        result.add(CbusConstants.CBUS_ASOF1);
        result.add(CbusConstants.CBUS_ARSON1);
        result.add(CbusConstants.CBUS_ARSOF1);

        // Opcodes with 6 data
        result.add(CbusConstants.CBUS_ASON2);
        result.add(CbusConstants.CBUS_ASOF2);
        result.add(CbusConstants.CBUS_ARSON2);
        result.add(CbusConstants.CBUS_ARSOF2);

        // Opcodes with 7 data
        result.add(CbusConstants.CBUS_ASON3);
        result.add(CbusConstants.CBUS_ASOF3);
        result.add(CbusConstants.CBUS_ARSON3);
        result.add(CbusConstants.CBUS_ARSOF3);

        return Collections.unmodifiableSet(result);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusOpCodesTest.class);

}
