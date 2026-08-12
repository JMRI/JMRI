package jmri.jmrix.can.cbus;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2019
 */
public class CbusOpCodesTest {

    // no testCtor as class only supplies static methods

    @Test
    public void testDecode() {
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_RESTP },0x12 ); // request e stop
        assertEquals("",CbusOpCodes.decode(m),"CbusOpCodes.decode");
        assertEquals("RESTP",CbusOpCodes.decodeopc(m), "CbusOpCodes.decodeopc");

        m.setElement(0, 0x18);
        assertEquals("",CbusOpCodes.decode(m),"0x18 no current opc definition");
        m.setElement(0, CbusConstants.CBUS_DKEEP);
        m.setElement(1, 0x04);
        assertEquals("Session: 4",CbusOpCodes.decode(m),"CBUS_DKEEP");

        m.setElement(0, CbusConstants.CBUS_RLOC);
        m.setElement(1, 0x00);
        m.setElement(2, 0x2c);
        assertEquals("Addr: 44(S)",CbusOpCodes.decode(m),"CBUS_RLOC");


        m.setElement(0, CbusConstants.CBUS_ERR);
        m.setElement(1, 0xcc);
        m.setElement(2, 0x8f);
        m.setElement(3, 0x01);
        assertEquals("Loco stack full for address 3215(L)",CbusOpCodes.decode(m),"CBUS_ERR 1");
        m.setElement(3, 0x02);
        assertEquals("Loco address 3215(L) taken",CbusOpCodes.decode(m),"CBUS_ERR 2");
        m.setElement(3, 0x03);
        assertEquals("Session 204 not present on Command Station",CbusOpCodes.decode(m),"CBUS_ERR 3");
        m.setElement(3, 0x04);
        assertEquals("Consist empty for consist 204",CbusOpCodes.decode(m),"CBUS_ERR 4");
        m.setElement(3, 0x05);
        assertEquals("Loco not found for session 204",CbusOpCodes.decode(m),"CBUS_ERR 5");
        m.setElement(3, 0x06);
        assertEquals("CAN bus error ",CbusOpCodes.decode(m),"CBUS_ERR 6");
        m.setElement(3, 0x07);
        assertEquals("Invalid request for address 3215(L)",CbusOpCodes.decode(m),"CBUS_ERR 7");
        m.setElement(3, 0x08);
        assertEquals("Throttle cancelled for session 204",CbusOpCodes.decode(m),"CBUS_ERR 8");
        m.setElement(3, 0x09);
        assertEquals("",CbusOpCodes.decode(m),"CBUS_ERR 9");
    }

    @Test
    public void testLocoSessionSpeedDirMsg() {
        CanMessage m = new CanMessage( 3, 0x12 );
        m.setElement(0, CbusConstants.CBUS_DSPD);
        m.setElement(1, 0x01);
        m.setElement(2, 0x02);
        assertEquals("Session: 1 Speed 1 Reverse ",CbusOpCodes.decode(m),"CBUS_DSPD Translate");
    }

    @Test
    public void testTimeFromClock() {
        CanReply send = new CanReply(1);
        send.setNumDataElements(7);
        send.setElement(0, CbusConstants.CBUS_FCLK);
        send.setElement(1, 41 ); // mins
        send.setElement(2, 13 ); // hrs
        send.setElement(3, 0b001010000 ); // month 5
        send.setElement(4,  2); // time divider, 0 is stpeed, 1 is real time, 2 twice real, 3 thrice real
        send.setElement(5, 27); // day of month, 0-31
        send.setElement(6, 0xDB ); // Temperature as twos complement -127 to +127

        String testStr = CbusOpCodes.decode(send);
        assertTrue(testStr.startsWith("Speed: x2 13:41"),"CBUS_FCLK Translate");
    }

    @Test
    public void testNodeEventMessage() {
        CanMessage m = new CanMessage( 1 );
        m.setElement(0, CbusConstants.CBUS_ACON);
        m.setElement(1, 0x01);
        m.setElement(2, 0x02);
        m.setElement(3, 0xd4);
        m.setElement(4, 0xac);
        assertEquals("NN:258 EN:54444 ",CbusOpCodes.decode(m),"CBUS_ACON");
    }

    @Test
    public void testDecodeCMDERR() {
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_CMDERR,12,34,1 },0x12  );
        assertEquals("NN:3106 ERROR : Command Not Supported.",CbusOpCodes.decode(m),"CBUS_CMDERR 1");
        m.setElement(3, 0xaa);
        assertEquals("NN:3106 ",CbusOpCodes.decode(m),"CBUS_CMDERR aa");
    }

    @Test
    public void testDecodeextend() {
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_CMDERR,12,34,1 },0x12  );
        assertEquals("NN:3106 ERROR : Command Not Supported.",CbusOpCodes.decode(m),"CBUS_CMDERR false1");
        assertTrue(CbusOpCodes.isKnownOpc(m),"m known");
        m.setExtended(true);
        assertFalse(CbusOpCodes.isKnownOpc(m),"m not known when extended");
        assertEquals(Bundle.getMessage("decodeUnknownExtended"),CbusOpCodes.decode(m),"CBUS_CMDERR true");
    }

    @Test
    public void testdecodeopcReserved() {
        CanMessage m = new CanMessage( new int[]{0x18 },0x12  );
        assertEquals("Reserved opcode " + m.toMonitorString().toUpperCase(),
            CbusOpCodes.decodeopc(m), "decodeopc 1");
        m.setExtended(true);
        assertTrue(CbusOpCodes.decodeopc(m).isEmpty(),"decodeopc 3");
    }

    @Test
    public void testDecodeopc() {
        CanMessage m = new CanMessage(1,11);
        for ( int i = 0; (i<258); i++ ) {
            m.setElement(0, i);
            if (OPCMAP.containsKey(i)) {
                assertEquals(OPCMAP.get(i),CbusOpCodes.decodeopc(m),"opc short text "+i);
            } else {
                assertEquals("Reserved opcode " + m.toMonitorString().toUpperCase(),
                    CbusOpCodes.decodeopc(m),"opc short text "+i);
            }
        }
    }

    @Test
    public void testextendedFrameTranslation(){
        // Outgoing control messages
        CanMessage m = new CanMessage( new int[]{5,1,2,3,0x0d,0,6,7},0x04  );
        m.setExtended(true);
        assertEquals("Bootloader: Do nothing",CbusOpCodes.decode(m),"extended 4  0");

        m.setElement(5, 1);
        assertEquals("Bootloader: Issue soft reset, leave boot mode",CbusOpCodes.decode(m),"extended 4  1");

        m.setElement(5, 2);
        assertEquals("Bootloader: Reset checksum and set address to 131,333",CbusOpCodes.decode(m),"extended 4  2");

        m.setElement(5, 3);
        assertEquals("Bootloader: Boot Check with checksum 1,798",CbusOpCodes.decode(m),"extended 4  3");

        m.setElement(5, 4);
        assertEquals("Bootloader: Verify boot mode",CbusOpCodes.decode(m),"extended 4  4");

        m.setElement(5, 5);
        assertEquals("Bootloader: Request device ID",CbusOpCodes.decode(m),"extended 4  5");

        m.setElement(5, 6);
        assertEquals("Bootloader: Request bootloader ID",CbusOpCodes.decode(m),"extended 4  6");

        m.setElement(5, 7);
        assertEquals("Bootloader: Memory write enables",CbusOpCodes.decode(m),"extended 4  7");

        m.setElement(5, 8);
        assertEquals("Unknown Extended Frame",CbusOpCodes.decode(m),"extended 4  8");

        // Outgoing dat amessages
        m.setHeader(5);
        assertEquals("Bootloader: Data : 05 01 02 03 0D 08 06 07",CbusOpCodes.decode(m),"extended 5 data");

        // Incoming control replies
        m = new CanMessage( new int[]{0},0x10000004 );
        m.setExtended(true);
        assertEquals("Bootloader: Boot Command Error",CbusOpCodes.decode(m),"extended 10000004 0");

        m.setElement(0, 1);
        assertEquals("Bootloader: Boot Command OK",CbusOpCodes.decode(m),"extended 10000004 1");

        m.setElement(0, 2);
        assertEquals("Bootloader: Boot Confirm",CbusOpCodes.decode(m),"extended 10000004 2");

        m.setElement(0, 3);
        assertEquals("Bootloader: Boot Address Out of Range",CbusOpCodes.decode(m),"extended 10000004 3");

        m.setElement(0, 4);
        assertEquals("Unknown Extended Frame",CbusOpCodes.decode(m),"extended 10000004 4");

        m = new CanMessage( new int[]{5,4,3,2,1,0,0},0x10000004  );
        m.setExtended(true);
        assertEquals("Bootloader: Device ID",CbusOpCodes.decode(m),"extended 10000004 5");

        m = new CanMessage( new int[]{6,5,4,3,2},0x10000004  );
        m.setExtended(true);
        assertEquals("Bootloader: Bootloader ID",CbusOpCodes.decode(m),"extended 10000004 6");

        // Incoming data replies
        m = new CanMessage( new int[]{0},0x10000005 );
        m.setExtended(true);
        assertEquals("Bootloader: Boot Data Error",CbusOpCodes.decode(m),"extended 10000005 0");

        m.setElement(0, 1);
        assertEquals("Bootloader: Boot Data OK",CbusOpCodes.decode(m),"extended 10000005 1");

        m.setElement(0, 2);
        assertEquals("Unknown Extended Frame",CbusOpCodes.decode(m),"extended 10000005 2");

        m.setElement(0, 3);
        assertEquals("Bootloader: Boot Address Out of Range",CbusOpCodes.decode(m),"extended 10000005 3");

        m.setElement(0, 4);
        assertEquals("Unknown Extended Frame",CbusOpCodes.decode(m),"extended 10000005 4");
    }

    @Test
    public void testGlocTranslate(){
        CanMessage m = new CanMessage( new int[]{CbusConstants.CBUS_GLOC,2,3,0},123 );
        assertEquals("Addr: 515(S) Flags: 0 Standard Request",CbusOpCodes.decode(m),"GLOC 0");

        m.setElement(3, 1);
        assertEquals("Addr: 515(S) Flags: 1 Steal Request",CbusOpCodes.decode(m),"GLOC 1");

        m.setElement(3, 2);
        assertEquals("Addr: 515(S) Flags: 2 Share Request",CbusOpCodes.decode(m), "GLOC 2");

        m.setElement(3, 3);
        assertEquals("Addr: 515(S) Flags: 3 Invalid Flags",CbusOpCodes.decode(m),"GLOC 3");
    }

    @Test
    public void testGetMinPriority() {
        assertEquals(1,CbusOpCodes.getOpcMinPriority(CbusConstants.CBUS_RTON), "Priority Fetched");
        assertEquals(3,CbusOpCodes.getOpcMinPriority(0x0f),"Default priority for unknown"); // unknown OPC
    }

    @Test
    public void testKnownOPC() {
        CanMessage m = new CanMessage(1,11);
        m.setElement(0, CbusConstants.CBUS_ASOF);
        assertTrue(CbusOpCodes.isKnownOpc(m),"m known");
        m.setElement(0, 0x0f); // unknown OPC
        assertFalse(CbusOpCodes.isKnownOpc(m),"m NOT known");
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
                assertTrue(CbusOpCodes.isEvent(i),"opc is event "+i);
            } else {
                assertFalse(CbusOpCodes.isEvent(i),"opc not event "+i);
            }
        }
    }

    @Test
    public void testisEventNotRequest() {
        for ( int i = 0; (i<256); i++ ) {
            if (eventNotRequestOpCodes.contains(i) ) {
                assertTrue(CbusOpCodes.isEventNotRequest(i),"opc is event "+i);
            } else {
                assertFalse(CbusOpCodes.isEventNotRequest(i),"opc not event or request "+i);
            }
        }
    }

    @Test
    public void testisDcc() {
        for ( int i = 0; (i<256); i++ ) {
            if (dccOpcodes.contains(i) ) {
                assertTrue(CbusOpCodes.isDcc(i),"opc is dcc "+i);
            } else {
                assertFalse(CbusOpCodes.isDcc(i),"opc not dcc "+i);
            }
        }
    }

    @Test
    public void testisOnEvent() {
        for ( int i = 0; (i<256); i++ ) {
            if (onEvOpcodes.contains(i) ) {
                assertTrue(CbusOpCodes.isOnEvent(i),"opc is on event "+i);
            } else {
                assertFalse(CbusOpCodes.isOnEvent(i),"opc not on event "+i);
            }
        }
    }

    @Test
    public void testisEventRequest() {
        for ( int i = 0; (i<256); i++ ) {
            if (evRequestOpcodes.contains(i) ) {
                assertTrue(CbusOpCodes.isEventRequest(i),"opc is request "+i);
            } else {
                assertFalse(CbusOpCodes.isEventRequest(i),"opc not request "+i);
            }
        }
    }

    @Test
    public void testisShortEvent() {
        for ( int i = 0; (i<256); i++ ) {
            if (shortOpcodes.contains(i) ) {
                assertTrue(CbusOpCodes.isShortEvent(i),"opc is request "+i);
            } else {
                assertFalse(CbusOpCodes.isShortEvent(i),"opc not request "+i);
            }
        }
    }

    @Test
    public void testGetSpeedFromInt(){
        assertEquals("0",CbusOpCodes.getSpeedFromByte(0),"speed 0");
        assertEquals("0 E Stop ",CbusOpCodes.getSpeedFromByte(1),"speed 1");
        assertEquals("1",CbusOpCodes.getSpeedFromByte(2),"speed 2");
        assertEquals("9",CbusOpCodes.getSpeedFromByte(10),"speed 10");
        assertEquals("125",CbusOpCodes.getSpeedFromByte(126),"speed 126");
        assertEquals("126",CbusOpCodes.getSpeedFromByte(127),"speed 127");
        assertEquals("0",CbusOpCodes.getSpeedFromByte(128),"speed 128");
        assertEquals("0 E Stop ",CbusOpCodes.getSpeedFromByte(129),"speed 129");
        assertEquals("1",CbusOpCodes.getSpeedFromByte(130),"speed 130");
        assertEquals("2",CbusOpCodes.getSpeedFromByte(131),"speed 131");
        assertEquals("53",CbusOpCodes.getSpeedFromByte(182),"speed 182");
        assertEquals("126",CbusOpCodes.getSpeedFromByte(255),"speed 255");
    }

    @Test
    public void testGetDirectionFromByte() {
        assertTrue(CbusOpCodes.getDirectionFromByte(0).contains("Rev"),"0 rev");
        assertTrue(CbusOpCodes.getDirectionFromByte(1).contains("Rev"),"1 rev");
        assertTrue(CbusOpCodes.getDirectionFromByte(77).contains("Rev"),"77 rev");
        assertTrue(CbusOpCodes.getDirectionFromByte(127).contains("Rev"),"127 rev");
        assertTrue(CbusOpCodes.getDirectionFromByte(128).contains("For"),"128 for");
        assertTrue(CbusOpCodes.getDirectionFromByte(129).contains("For"),"129 for");
        assertTrue(CbusOpCodes.getDirectionFromByte(211).contains("For"),"211 for");
        assertTrue(CbusOpCodes.getDirectionFromByte(255).contains("For"),"255 for");
    }

    @Test
    public void testSpeedDirFromByte() {
        assertEquals(" Speed 0 Reverse ",CbusOpCodes.speedDirFromByte(0),"speed 0");
        assertEquals(" Speed 2 Forward ",CbusOpCodes.speedDirFromByte(131),"speed 131");
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

    // private static final Logger log = LoggerFactory.getLogger(CbusOpCodesTest.class);

}
