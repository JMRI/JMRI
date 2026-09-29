package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2018 2019
 */
public class CbusMessageTest {

    // no testCtor as class only supplies static methods

    @Test
    public void testOpcRangeToSTL() {
        CanReply r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x93); // ARON OPC
        CanReply m = CbusMessage.opcRangeToStl(r);
        assertEquals(0x90, m.getElement(0), "ARON OPC Changed"); // ACON OPC

        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x94); // AROF OPC
        m = CbusMessage.opcRangeToStl(r);
        assertEquals(0x91, m.getElement(0),"AROF OPC Changed"); // ACOF OPC

        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x9d); // ARSON OPC
        m = CbusMessage.opcRangeToStl(r);
        assertEquals(0x98, m.getElement(0),"ARSON OPC Changed"); // ASON OPC

        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x9e); // ARSOF OPC
        m = CbusMessage.opcRangeToStl(r);
        assertEquals(0x99, m.getElement(0),"ARSOF OPC Changed"); // ASOF OPC

        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x95); // EVULN OPC
        m = CbusMessage.opcRangeToStl(r);
        assertEquals(0x95, m.getElement(0),"Other OPCs do not change"); // EVULN OPC
    }

    @Test
    public void testgetNodeNumberMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(5);
        m.setElement(0, 0x90); // ACON OPC
        m.setElement(1, 0xee);
        m.setElement(2, 0x56);
        m.setElement(3, 0x11);
        m.setElement(4, 0x16);
        assertEquals(61014, CbusMessage.getNodeNumber(m),"Node calculated OK");
        m.setElement(0, 0x95); // EVULN OPC
        assertEquals(0,CbusMessage.getNodeNumber(m),"Not an event returns node 0");
    }

    @Test
    public void testgetNodeNumberReply() {
        CanReply r = new CanReply();
        r.setNumDataElements(5);
        r.setElement(0, 0x90); // ACON OPC
        r.setElement(1, 0xee);
        r.setElement(2, 0x56);
        r.setElement(3, 0x11);
        r.setElement(4, 0x16);
        assertEquals(61014, CbusMessage.getNodeNumber(r),"Node calculated OK");
        r.setElement(0, 0x95); // EVULN OPC
        assertEquals(0, CbusMessage.getNodeNumber(r),"Not an event returns node 0");
    }

    @Test
    public void testgetEventMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(5);
        m.setElement(0, 0x90); // ACON OPC
        m.setElement(1, 0xee);
        m.setElement(2, 0x56);
        m.setElement(3, 0x11);
        m.setElement(4, 0x16);
        assertEquals(4374,CbusMessage.getEvent(m),"Event calculated OK");
        m.setElement(0, 0x95); // EVULN OPC
        assertEquals(-1,CbusMessage.getEvent(m),"Not an event returns -1");
    }

    @Test
    public void testgetEventReply() {
        CanReply r = new CanReply();
        r.setNumDataElements(5);
        r.setElement(0, 0x90); // ACON OPC
        r.setElement(1, 0xee);
        r.setElement(2, 0x56);
        r.setElement(3, 0x11);
        r.setElement(4, 0x16);
        assertEquals(4374,CbusMessage.getEvent(r),"Event calculated OK");
        r.setElement(0, 0x95); // EVULN OPC
        assertEquals(-1,CbusMessage.getEvent(r),"Not an event returns -1");
    }

    @Test
    public void testgetEventTypeMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, 0x90); // ACON OPC
        assertEquals(CbusConstants.EVENT_ON,CbusMessage.getEventType(m),"Event Type On");
        m.setElement(0, 0x99); // ASOF OPC
        assertEquals(CbusConstants.EVENT_OFF, CbusMessage.getEventType(m),"Event Type Off");
    }

    @Test
    public void testgetEventTypeReply() {
        CanReply r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x90); // ACON OPC
        assertEquals(CbusConstants.EVENT_ON, CbusMessage.getEventType(r),"Event Type On");
        r.setElement(0, 0x99); // ASOF OPC
        assertEquals(CbusConstants.EVENT_OFF, CbusMessage.getEventType(r),"Event Type Off");
    }

    @Test
    public void testisEventMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, 0x90); // ACON OPC
        assertTrue(CbusMessage.isEvent(m),"Is Event");
        m.setElement(0, 0x95); // EVULN OPC
        assertFalse(CbusMessage.isEvent(m),"Is Not Event");
    }

    @Test
    public void testisEventReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x90); // ACON OPC
        assertTrue(CbusMessage.isEvent(r),"Is Event");
        r.setElement(0, 0x95); // EVULN OPC
        assertFalse(CbusMessage.isEvent(r),"Is Not Event");
    }

    @Test
    public void testisShortMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, 0x90); // ACON OPC
        assertFalse(CbusMessage.isShort(m),"Is Not Short");
        m.setElement(0, 0x99); // ASOF OPC
        assertTrue(CbusMessage.isShort(m),"Is Short");
    }

    @Test
    public void testisShortReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x90); // ACON OPC
        assertFalse(CbusMessage.isShort(r),"Is Not Short");
        r.setElement(0, 0x99); // ASOF OPC
        assertTrue(CbusMessage.isShort(r),"Is Short");
    }

    @Test
    public void testtoAddressMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(5);
        m.setElement(0, 0x90); // ACON OPC
        m.setElement(1, 0xdd);
        m.setElement(2, 0xab);
        m.setElement(3, 0x4b);
        m.setElement(4, 0xb3);

        assertEquals("+n56747e19379",CbusMessage.toAddress(m),"string toAddressMessageAcon");
        m.setElement(0, 0x91); // ACOF OPC
        assertEquals("-n56747e19379",CbusMessage.toAddress(m),"toAddressMessageAcof");
        m.setElement(0, 0x98); // ASON OPC
        assertEquals("+19379",CbusMessage.toAddress(m),"toAddressMessageAson");
        m.setElement(0, 0x99); // ASOF OPC
        assertEquals("-19379",CbusMessage.toAddress(m),"toAddressMessageAsof");
        m.setElement(0, 0x9e); // ARSON OPC
        assertEquals("X9EDDAB4BB3", CbusMessage.toAddress(m),"toAddressMessageArson");
    }

    @Test
    public void testtoAddressReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(5);
        r.setElement(0, 0x90); // ACON OPC
        r.setElement(1, 0xdd);
        r.setElement(2, 0xab);
        r.setElement(3, 0x4b);
        r.setElement(4, 0xb3);

        assertEquals("+n56747e19379",CbusMessage.toAddress(r),"toAddressReplyAcon");
        r.setElement(0, 0x91); // ACOF OPC
        assertEquals("-n56747e19379",CbusMessage.toAddress(r),"toAddressReplyAcof");
        r.setElement(0, 0x98); // ASON OPC
        assertEquals("+19379",CbusMessage.toAddress(r),"toAddressReplyAson");
        r.setElement(0, 0x99); // ASOF OPC
        assertEquals("-19379",CbusMessage.toAddress(r),"toAddressReplyAsof");
        r.setElement(0, 0x9e); // ARSON OPC
        assertEquals("X9EDDAB4BB3",CbusMessage.toAddress(r),"toAddressReplyArson");
    }

    @Test
    public void testisRequestTrackOffMessage() {
        CanMessage m = new CanMessage(0x12,1);
        m.setElement(0, 0x08); // RTOF OPC
        assertTrue(CbusMessage.isRequestTrackOff(m),"isRequestTrackOff Good Message");
        m = new CanMessage(0x12,1);
        m.setElement(0, 0x09); // RTON OPC
        assertFalse(CbusMessage.isRequestTrackOff(m),"isRequestTrackOff Bad Message");
    }

    @Test
    public void testisRequestTrackOnMessage() {
        CanMessage m = new CanMessage(0x12,1);
        m.setElement(0, 0x09); // RTON OPC
        assertTrue(CbusMessage.isRequestTrackOn(m),"isRequestTrackOn Good Message");
        m = new CanMessage(0x12,1);
        m.setElement(0, 0x08); // RTOF OPC
        assertFalse(CbusMessage.isRequestTrackOn(m),"isRequestTrackOn Bad Message");
    }

    @Test
    public void testisTrackOnReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x05); // TON OPC
        assertTrue(CbusMessage.isTrackOn(r),"isRequestTrackOn Good Reply");
        r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x04); // TOF OPC
        assertFalse(CbusMessage.isTrackOn(r),"isRequestTrackOn Bad Reply");
    }

    @Test
    public void testisTrackOffReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x04); // TOF OPC
        assertTrue(CbusMessage.isTrackOff(r),"isRequestTrackOff Good Reply");
        r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x05); // TON OPC
        assertFalse(CbusMessage.isTrackOff(r),"isRequestTrackOff Bad Reply");
    }

    @Test
    public void testgetRequestTrackOnMessage() {
        CanMessage m = CbusMessage.getRequestTrackOn(0x12);
        assertEquals(0x09, m.getElement(0),"getRequestTrackOn OPC"); // RTON OPC
        assertEquals(1,m.getNumDataElements(),"getRequestTrackOn Length");
    }

    @Test
    public void testgetRequestTrackOffMessage() {
        CanMessage m = CbusMessage.getRequestTrackOff(0x12);
        assertEquals(0x08,m.getElement(0),"getRequestTrackOff OPC"); // RTON OPC
        assertEquals(1,m.getNumDataElements(),"getRequestTrackOff Length");
    }

    @Test
    public void testgetDataLength() {
        CanReply r = new CanReply(0x12);
        CanMessage m = new CanMessage(0x12);
        r.setElement(0, 0x04); // TOF OPC
        m.setElement(0, 0x04); // TOF OPC
        assertEquals(0,CbusMessage.getDataLength(r),"Data Length 0 r");
        assertEquals(0,CbusMessage.getDataLength(m),"Data Length 0 m");
        r.setElement(0, 0x11); // RQMN
        m.setElement(0, 0x11); // RQMN
        assertEquals(0,CbusMessage.getDataLength(r),"Data Length 0 r");
        assertEquals(0,CbusMessage.getDataLength(m),"Data Length 0 m");

        r.setElement(0, 0x83); // WCVB OPC
        m.setElement(0, 0x83); // WCVB OPC
        assertEquals(4,CbusMessage.getDataLength(r),"Data Length 4 r");
        assertEquals(4,CbusMessage.getDataLength(m),"Data Length 4 m");
        r.setElement(0, 0xe2); // NAME
        m.setElement(0, 0xe2); // NAME
        assertEquals(7,CbusMessage.getDataLength(r),"Data Length 7 r");
        assertEquals(7,CbusMessage.getDataLength(m),"Data Length 7 m");

    }

    @Test
    public void testsetgetPriority() {

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setPri(null,0x01), "Should have thrown an exception");
        assertEquals("null is Not a CanMutableFrame", e.getMessage());

        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.getPri(null), "Should have thrown an exception");
        assertEquals("null is Not a CanFrame", e.getMessage());


        CanReply r = new CanReply(0x00);
        CanMessage m = new CanMessage(0x00);
        assertEquals(0,CbusMessage.getPri(r),"Priority 0 r");
        assertEquals(0,CbusMessage.getPri(m),"Priority 0 m");

        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setPri(r,0xff), "Should have thrown an exception");
        assertEquals("Invalid CBUS Priority value: 255", e.getMessage());

        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setPri(m,0xff), "Should have thrown an exception");
        assertEquals("Invalid CBUS Priority value: 255", e.getMessage());


        CbusMessage.setPri(m,CbusConstants.DEFAULT_MINOR_PRIORITY);
        CbusMessage.setPri(r,CbusConstants.DEFAULT_MINOR_PRIORITY);
        assertEquals(3,CbusMessage.getPri(r),"Priority DEFAULT_MINOR_PRIORITY r");
        assertEquals(3,CbusMessage.getPri(m),"Priority DEFAULT_MINOR_PRIORITY m");

        CbusMessage.setPri(m,CbusConstants.DEFAULT_DYNAMIC_PRIORITY);
        CbusMessage.setPri(r,CbusConstants.DEFAULT_DYNAMIC_PRIORITY);
        assertEquals(2,CbusMessage.getPri(r),"Priority DEFAULT_DYNAMIC_PRIORITY r");
        assertEquals(2,CbusMessage.getPri(m),"Priority DEFAULT_DYNAMIC_PRIORITY m");

        r.setExtended(true);
        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setPri(r,CbusConstants.DEFAULT_MINOR_PRIORITY), "Should have thrown an exception");
        assertEquals("Extended CBUS CAN Frames do not have a priority concept.", e.getMessage());

        m.setExtended(true);
        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setPri(m,CbusConstants.DEFAULT_MINOR_PRIORITY), "Should have thrown an exception");
        assertEquals("Extended CBUS CAN Frames do not have a priority concept.", e.getMessage());
    }

    @Test
    public void testsetgetId() {

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setId(null,0x01), "Should have thrown an exception");
        assertEquals("null is Not a CanMutableFrame", e.getMessage());

        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.getId(null), "Should have thrown an exception");
        assertEquals("null is Not a CanFrame", e.getMessage());


        CanReply r = new CanReply(0x00);
        CanMessage m = new CanMessage(0x00);
        assertEquals(0,CbusMessage.getId(r),"getId 0 r");
        assertEquals(0,CbusMessage.getId(m),"getId 0 m");
        CbusMessage.setId(r,0x01);
        CbusMessage.setId(m,0x01);
        assertEquals(1,CbusMessage.getId(r),"getId 1 r");
        assertEquals(1,CbusMessage.getId(m),"getId 1 m");
        CbusMessage.setId(r,120);
        CbusMessage.setId(m,120);
        assertEquals(120,CbusMessage.getId(r),"getId 120 r");
        assertEquals(120,CbusMessage.getId(m),"getId 120 m");

        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setId(r,0xff), "Should have thrown an exception");
        assertNotNull(e);
        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setId(m,0xff), "Should have thrown an exception");
        assertNotNull(e);

        r.setExtended(true);
        m.setExtended(true);

        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setId(r,0x05), "Should have thrown an exception");
        assertEquals("No CAN ID Concept on Extended CBUS CAN Frame.", e.getMessage());
        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setId(m,0x05), "Should have thrown an exception");
        assertEquals("No CAN ID Concept on Extended CBUS CAN Frame.", e.getMessage());

        r.setExtended(false);
        m.setExtended(false);

        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setId(r,0xffffff), "Should have thrown an exception");
        assertEquals("invalid standard ID value: 16777215", e.getMessage());
        e = assertThrows(IllegalArgumentException.class,
            () -> CbusMessage.setId(m,0xffffff), "Should have thrown an exception");
        assertEquals("invalid standard ID value: 16777215", e.getMessage());
    }

    @Test
    public void testisArst() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x07); // Arst OPC
        assertTrue(CbusMessage.isArst(r));
        r.setElement(0, 0x06);
        assertFalse(CbusMessage.isArst(r));
    }

    @Test
    public void testgetReadCV() {
        CanMessage m = CbusMessage.getReadCV(1,jmri.ProgrammingMode.PAGEMODE,0x12);
        assertEquals("[592] 84 FF 00 01 02",m.toString(),"PAGEMODE");
        m = CbusMessage.getReadCV(255,jmri.ProgrammingMode.DIRECTBITMODE,0x12);
        assertEquals("[592] 84 FF 00 FF 01",m.toString(),"DIRECTBITMODE");
        m = CbusMessage.getReadCV(214,jmri.ProgrammingMode.DIRECTBYTEMODE,0x12);
        assertEquals("[592] 84 FF 00 D6 00",m.toString(),"DIRECTBYTEMODE");
        m = CbusMessage.getReadCV(214,jmri.ProgrammingMode.REGISTERMODE,0x12);
        assertEquals("[592] 84 FF 00 D6 03",m.toString(),"REGISTERMODE");
    }

    @Test
    public void testgetVerifyCV() {
        CanMessage m = CbusMessage.getVerifyCV(1,jmri.ProgrammingMode.PAGEMODE,0x57,0x12);
        assertEquals("[592] A4 FF 00 01 02 57",m.toString(),"PAGEMODE");
        m = CbusMessage.getVerifyCV(255,jmri.ProgrammingMode.DIRECTBITMODE,0x63,0x12);
        assertEquals("[592] A4 FF 00 FF 01 63",m.toString(),"DIRECTBITMODE");
        m = CbusMessage.getVerifyCV(214,jmri.ProgrammingMode.DIRECTBYTEMODE,0x13,0x12);
        assertEquals("[592] A4 FF 00 D6 00 13",m.toString(),"DIRECTBYTEMODE");
        m = CbusMessage.getVerifyCV(213,jmri.ProgrammingMode.REGISTERMODE,0xB9,0x12);
        assertEquals("[592] A4 FF 00 D5 03 B9",m.toString(),"REGISTERMODE");
    }

    @Test
    public void testgetgetWriteCV() {
        CanMessage m = CbusMessage.getWriteCV(1,211,jmri.ProgrammingMode.PAGEMODE,0x12);
        assertEquals("[592] A2 FF 00 01 02 D3",m.toString(),"PAGEMODE");
        m = CbusMessage.getWriteCV(255,1,jmri.ProgrammingMode.DIRECTBITMODE,0x12);
        assertEquals("[592] A2 FF 00 FF 01 01",m.toString(),"DIRECTBITMODE");
        m = CbusMessage.getWriteCV(214,0,jmri.ProgrammingMode.DIRECTBYTEMODE,0x12);
        assertEquals("[592] A2 FF 00 D6 00 00",m.toString(),"DIRECTBYTEMODE");
        m = CbusMessage.getWriteCV(214,123,jmri.ProgrammingMode.REGISTERMODE,0x12);
        assertEquals("[592] A2 FF 00 D6 03 7B",m.toString(),"REGISTERMODE");
    }

    @Test
    public void testgetOpsModeWriteCV() {
        CanMessage m = CbusMessage.getOpsModeWriteCV(22,false,211,255,0x12);
        assertEquals("[592] C1 00 16 00 D3 05 FF",m.toString(),"getOpsModeWriteCV");
    }

    @Test
    public void testgetBootEntry() {
        CanMessage m = CbusMessage.getBootEntry(43215,0x12);
        assertEquals("[592] 5C A8 CF",m.toString(),"getBootEntry");
    }

    @Test
    public void testgetBootNop() {
        CanMessage m = CbusMessage.getBootNop(0x123456,0x12);
        assertEquals("[4] 56 34 12 00 1D 00 00 00",m.toString(),"getBootNop");
    }

    @Test
    public void testgetBootReset() {
        CanMessage m = CbusMessage.getBootReset(0x12);
        assertEquals("[4] 00 00 00 00 1D 01 00 00",m.toString(),"getBootReset");
    }

    @Test
    public void testgetBootInitialise() {
        CanMessage m = CbusMessage.getBootInitialise(0x123456,0x12);
        assertEquals("[4] 56 34 12 00 1D 02 00 00",m.toString(),"getBootInitialise");
    }

    @Test
    public void testgetBootCheck() {
        CanMessage m = CbusMessage.getBootCheck(123,0x12);
        assertEquals("[4] 00 00 00 00 1D 03 7B 00",m.toString(),"getBootCheck");
    }

    @Test
    public void testgetBootTest() {
        CanMessage m = CbusMessage.getBootTest(0x12);
        assertEquals("[4] 00 00 00 00 1D 04 00 00",m.toString(),"getBootTest");
    }

    @Test
    public void testgetBootDevId() {
        CanMessage m = CbusMessage.getBootDevId(0x12);
        assertEquals("[4] 00 00 00 00 1D 05 00 00",m.toString(),"getBootDevid");
    }

    @Test
    public void testgetBootId() {
        CanMessage m = CbusMessage.getBootId(0x12);
        assertEquals("[4] 00 00 00 00 1D 06 00 00",m.toString(),"getBootBootid");
    }

    @Test
    public void testgetBootEnables() {
        CanMessage m = CbusMessage.getBootEnables(0x3, 0x12);
        assertEquals("[4] 00 00 00 00 1D 07 03 00",m.toString(),"getBootEnables");
    }

    @Test
    public void testgetBootWriteData() {
        CanMessage m = CbusMessage.getBootWriteData( new int[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08},0x12);
        assertEquals("[5] 01 02 03 04 05 06 07 08",m.toString(),"getBootWriteData");

        m = CbusMessage.getBootWriteData( new int[]{0x01,0x02},0x12);
        assertEquals("[5] 01 02",m.toString(),"getBootWriteData");

        m = CbusMessage.getBootWriteData( new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08},0x12);
        assertEquals("[5] 01 02 03 04 05 06 07 08",m.toString(),"getBootWriteData");

        m = CbusMessage.getBootWriteData( new byte[]{0x01,0x02},0x12);
        assertEquals("[5] 01 02",m.toString(),"getBootWriteData");
    }

    @Test
    public void testisBootError() {
        CanReply r = new CanReply(1);
        r.setExtended(false);
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootError(r),"isBootError fff"); // false false false
        r.setElement(0,0);
        assertFalse(CbusMessage.isBootError(r),"isBootError ffp"); // ffp
        r.setHeader(0x10000004);
        assertFalse(CbusMessage.isBootError(r),"isBootError fpp"); // fpp
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootError(r),"isBootError fpf"); // fpf
        r.setExtended(true);
        assertFalse(CbusMessage.isBootError(r),"isBootError ppf"); // ppf
        r.setHeader(0x14);
        assertFalse(CbusMessage.isBootError(r),"isBootError pff"); // pff
        r.setHeader(0x10000004);
        r.setElement(0,0);
        assertTrue(CbusMessage.isBootError(r),"isBootError ppp"); // ppp

    }

    @Test
    public void testisBootDataError() {
        CanReply r = new CanReply(1);
        r.setExtended(false);
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootDataError(r),"isBootDataError fff"); // false false false
        r.setElement(0,0);
        assertFalse(CbusMessage.isBootDataError(r),"isBootDataError ffp"); // ffp
        r.setHeader(0x10000005);
        assertFalse(CbusMessage.isBootDataError(r),"isBootDataError fpp"); // fpp
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootDataError(r),"isBootDataError fpf"); // fpf
        r.setExtended(true);
        assertFalse(CbusMessage.isBootDataError(r),"isBootDataError ppf"); // ppf
        r.setHeader(0x14);
        assertFalse(CbusMessage.isBootDataError(r),"isBootDataError pff"); // pff
        r.setHeader(0x10000005);
        r.setElement(0,0);
        assertTrue(CbusMessage.isBootDataError(r),"isBootDataError ppp"); // ppp

    }

    @Test
    public void testisBootOK() {
        CanReply r = new CanReply(1);
        r.setExtended(false);
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootOK(r),"isBootOK fff"); // false false false
        r.setElement(0,1);
        assertFalse(CbusMessage.isBootOK(r),"isBootOK ffp"); // ffp
        r.setHeader(0x10000004);
        assertFalse(CbusMessage.isBootOK(r),"isBootOK fpp"); // fpp
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootOK(r),"isBootOK fpf"); // fpf
        r.setExtended(true);
        assertFalse(CbusMessage.isBootOK(r),"isBootOK ppf"); // ppf
        r.setHeader(0x14);
        assertFalse(CbusMessage.isBootOK(r),"isBootOK pff"); // pff
        r.setHeader(0x10000004);
        r.setElement(0,1);
        assertTrue(CbusMessage.isBootOK(r),"isBootOK ppp"); // ppp

    }

    @Test
    public void testisBootDataOK() {
        CanReply r = new CanReply(1);
        r.setExtended(false);
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootDataOK(r),"isBootDataOK fff"); // false false false
        r.setElement(0,1);
        assertFalse(CbusMessage.isBootDataOK(r),"isBootDataOK ffp"); // ffp
        r.setHeader(0x10000005);
        assertFalse(CbusMessage.isBootDataOK(r),"isBootDataOK fpp"); // fpp
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootDataOK(r),"isBootDataOK fpf"); // fpf
        r.setExtended(true);
        assertFalse(CbusMessage.isBootDataOK(r),"isBootDataOK ppf"); // ppf
        r.setHeader(0x14);
        assertFalse(CbusMessage.isBootDataOK(r),"isBootDataOK pff"); // pff
        r.setHeader(0x10000005);
        r.setElement(0,1);
        assertTrue(CbusMessage.isBootDataOK(r),"isBootDataOK ppp"); // ppp

    }

    @Test
    public void testisBootOutOfRange() {
        CanReply r = new CanReply(1);
        r.setExtended(false);
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootOutOfRange(r),"isBootOutOfRange fff"); // false false false
        r.setElement(0,1);
        assertFalse(CbusMessage.isBootOutOfRange(r),"isBootOutOfRange ffp"); // ffp
        r.setHeader(0x10000004);
        assertFalse(CbusMessage.isBootOutOfRange(r),"isBootOutOfRange fpp"); // fpp
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootOutOfRange(r),"isBootOutOfRange fpf"); // fpf
        r.setExtended(true);
        assertFalse(CbusMessage.isBootOutOfRange(r),"isBootOutOfRange ppf"); // ppf
        r.setHeader(0x14);
        assertFalse(CbusMessage.isBootOutOfRange(r),"isBootOutOfRange pff"); // pff
        r.setHeader(0x10000004);
        r.setElement(0,3);
        assertTrue(CbusMessage.isBootOutOfRange(r),"isBootOutOfRange ppp"); // ppp

    }

    @Test
    public void testisBootDataOutOfRange() {
        CanReply r = new CanReply(1);
        r.setExtended(false);
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootDataOutOfRange(r),"isBootDataOutOfRange fff"); // false false false
        r.setElement(0,1);
        assertFalse(CbusMessage.isBootDataOutOfRange(r),"isBootDataOutOfRange ffp"); // ffp
        r.setHeader(0x10000004);
        assertFalse(CbusMessage.isBootDataOutOfRange(r),"isBootDataOutOfRange fpp"); // fpp
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootDataOutOfRange(r),"isBootDataOutOfRange fpf"); // fpf
        r.setExtended(true);
        assertFalse(CbusMessage.isBootDataOutOfRange(r),"isBootDataOutOfRange ppf"); // ppf
        r.setHeader(0x14);
        assertFalse(CbusMessage.isBootDataOutOfRange(r),"isBootDataOutOfRange pff"); // pff
        r.setHeader(0x10000005);
        r.setElement(0,3);
        assertTrue(CbusMessage.isBootDataOutOfRange(r),"isBootDataOutOfRange ppp"); // ppp
    }



    @Test
    public void testisBootConfirm() {
        CanReply r = new CanReply(1);
        r.setExtended(false);
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootConfirm(r),"isBootConfirm fff"); // false false false
        r.setElement(0,2);
        assertFalse(CbusMessage.isBootConfirm(r),"isBootConfirm ffp"); // ffp
        r.setHeader(0x10000004);
        assertFalse(CbusMessage.isBootConfirm(r),"isBootConfirm fpp"); // fpp
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootConfirm(r),"isBootConfirm fpf"); // fpf
        r.setExtended(true);
        assertFalse(CbusMessage.isBootConfirm(r),"isBootConfirm ppf"); // ppf
        r.setHeader(0x14);
        assertFalse(CbusMessage.isBootConfirm(r),"isBootConfirm pff"); // pff
        r.setHeader(0x10000004);
        r.setElement(0,2);
        assertTrue(CbusMessage.isBootConfirm(r),"isBootConfirm ppp"); // ppp

    }

    @Test
    public void testisBootDevId() {
        CanReply r = new CanReply(7);
        r.setExtended(false);
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootDevId(r),"isBootDevId fff"); // false false false
        r.setElement(0,2);
        assertFalse(CbusMessage.isBootDevId(r),"isBootDevId ffp"); // ffp
        r.setHeader(0x10000004);
        assertFalse(CbusMessage.isBootDevId(r),"isBootDevId fpp"); // fpp
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootDevId(r),"isBootDevId fpf"); // fpf
        r.setExtended(true);
        assertFalse(CbusMessage.isBootDevId(r),"isBootDevId ppf"); // ppf
        r.setHeader(0x14);
        assertFalse(CbusMessage.isBootDevId(r),"isBootDevId pff"); // pff
        r.setHeader(0x10000004);
        r.setElement(0,5);
        assertTrue(CbusMessage.isBootDevId(r),"isBootDevId ppp"); // ppp

    }

    @Test
    public void testisBootBootId() {
        CanReply r = new CanReply(5);
        r.setExtended(false);
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootId(r),"isBootBootId fff"); // false false false
        r.setElement(0,2);
        assertFalse(CbusMessage.isBootId(r),"isBootBootId ffp"); // ffp
        r.setHeader(0x10000004);
        assertFalse(CbusMessage.isBootId(r),"isBootBootId fpp"); // fpp
        r.setElement(0,7);
        assertFalse(CbusMessage.isBootId(r),"isBootBootId fpf"); // fpf
        r.setExtended(true);
        assertFalse(CbusMessage.isBootId(r),"isBootBootId ppf"); // ppf
        r.setHeader(0x14);
        assertFalse(CbusMessage.isBootId(r),"isBootBootId pff"); // pff
        r.setHeader(0x10000004);
        r.setElement(0,6);
        assertTrue(CbusMessage.isBootId(r),"isBootBootId ppp"); // ppp

    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(CbusMessageTest.class);

}
