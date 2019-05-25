package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2018 2019
 */
public class CbusMessageTest {

    @Test
    public void testCTor() {
        CbusMessage t = new CbusMessage();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testOpcRangeToSTL() {
        CanReply r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x93); // ARON OPC
        CanReply m = CbusMessage.opcRangeToStl(r);
        Assert.assertTrue("ARON OPC Changed", m.getElement(0) == 0x90); // ACON OPC

        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x94); // AROF OPC
        m = CbusMessage.opcRangeToStl(r);
        Assert.assertTrue("AROF OPC Changed", m.getElement(0) == 0x91); // ACOF OPC

        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x9d); // ARSON OPC
        m = CbusMessage.opcRangeToStl(r);
        Assert.assertTrue("ARSON OPC Changed", m.getElement(0) == 0x98); // ASON OPC

        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x9e); // ARSOF OPC
        m = CbusMessage.opcRangeToStl(r);
        Assert.assertTrue("ARSOF OPC Changed", m.getElement(0) == 0x99); // ASOF OPC
        
        r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x95); // EVULN OPC
        m = CbusMessage.opcRangeToStl(r);
        Assert.assertTrue("Other OPCs do not change", m.getElement(0) == 0x95); // EVULN OPC
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
        Assert.assertTrue("Node calculated OK", CbusMessage.getNodeNumber(m) == 61014);
        m.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Not an event returns node 0", CbusMessage.getNodeNumber(m) == 0 );
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
        Assert.assertTrue("Node calculated OK", CbusMessage.getNodeNumber(r) == 61014);
        r.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Not an event returns node 0", CbusMessage.getNodeNumber(r) == 0 );
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
        Assert.assertTrue("Event calculated OK", CbusMessage.getEvent(m) == 4374);
        m.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Not an event returns node 0", CbusMessage.getEvent(m) == 0 );
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
        Assert.assertTrue("Event calculated OK", CbusMessage.getEvent(r) == 4374);
        r.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Not an event returns node 0", CbusMessage.getEvent(r) == 0 );
    }

    @Test
    public void testgetEventTypeMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Event Type On", CbusMessage.getEventType(m) == CbusConstants.EVENT_ON);
        m.setElement(0, 0x99); // ASOF OPC
        Assert.assertTrue("Event Type Off", CbusMessage.getEventType(m) == CbusConstants.EVENT_OFF );
    }

    @Test
    public void testgetEventTypeReply() {
        CanReply r = new CanReply();
        r.setNumDataElements(1);
        r.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Event Type On", CbusMessage.getEventType(r) == CbusConstants.EVENT_ON);
        r.setElement(0, 0x99); // ASOF OPC
        Assert.assertTrue("Event Type Off", CbusMessage.getEventType(r) == CbusConstants.EVENT_OFF );
    }

    @Test
    public void testisEventMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Is Event", CbusMessage.isEvent(m) == true);
        m.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Is Not Event", CbusMessage.isEvent(m) == false );
    }

    @Test
    public void testisEventReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Is Event", CbusMessage.isEvent(r) == true);
        r.setElement(0, 0x95); // EVULN OPC
        Assert.assertTrue("Is Not Event", CbusMessage.isEvent(r) == false );
    }

    @Test
    public void testisShortMessage() {
        CanMessage m = new CanMessage(0x12);
        m.setNumDataElements(1);
        m.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Is Not Short", CbusMessage.isShort(m) == false);
        m.setElement(0, 0x99); // ASOF OPC
        Assert.assertTrue("Is Short", CbusMessage.isShort(m) == true );
    }

    @Test
    public void testisShortReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x90); // ACON OPC
        Assert.assertTrue("Is Not Short", CbusMessage.isShort(r) == false);
        r.setElement(0, 0x99); // ASOF OPC
        Assert.assertTrue("Is Short", CbusMessage.isShort(r) == true );
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

        Assert.assertEquals("string toAddressMessageAcon", CbusMessage.toAddress(m),"+n56747e19379");
        m.setElement(0, 0x91); // ACOF OPC
        Assert.assertEquals("toAddressMessageAcof", CbusMessage.toAddress(m),"-n56747e19379" );
        m.setElement(0, 0x98); // ASON OPC
        Assert.assertEquals("toAddressMessageAson", CbusMessage.toAddress(m),"+19379" );
        m.setElement(0, 0x99); // ASOF OPC
        Assert.assertEquals("toAddressMessageAsof", CbusMessage.toAddress(m),"-19379" );
        m.setElement(0, 0x9e); // ARSON OPC
        Assert.assertEquals("toAddressMessageArson", CbusMessage.toAddress(m),"X9EDDAB4BB3" );
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

        Assert.assertEquals("toAddressReplyAcon", CbusMessage.toAddress(r),"+n56747e19379");
        r.setElement(0, 0x91); // ACOF OPC
        Assert.assertEquals("toAddressReplyAcof", CbusMessage.toAddress(r),"-n56747e19379" );
        r.setElement(0, 0x98); // ASON OPC
        Assert.assertEquals("toAddressReplyAson", CbusMessage.toAddress(r),"+19379" );
        r.setElement(0, 0x99); // ASOF OPC
        Assert.assertEquals("toAddressReplyAsof", CbusMessage.toAddress(r),"-19379" );
        r.setElement(0, 0x9e); // ARSON OPC
        Assert.assertEquals("toAddressReplyArson", CbusMessage.toAddress(r),"X9EDDAB4BB3" );
    }    
    
    @Test
    public void testisRequestTrackOffMessage() {
        CanMessage m = new CanMessage(0x12,1);
        m.setElement(0, 0x08); // RTOF OPC
        Assert.assertEquals("isRequestTrackOff Good Message", CbusMessage.isRequestTrackOff(m),true);
        m = new CanMessage(0x12,1);
        m.setElement(0, 0x09); // RTON OPC    
        Assert.assertEquals("isRequestTrackOff Bad Message", CbusMessage.isRequestTrackOff(m),false);
    }
    
    @Test
    public void testisRequestTrackOnMessage() {
        CanMessage m = new CanMessage(0x12,1);
        m.setElement(0, 0x09); // RTON OPC
        Assert.assertEquals("isRequestTrackOn Good Message", CbusMessage.isRequestTrackOn(m),true);
        m = new CanMessage(0x12,1);
        m.setElement(0, 0x08); // RTOF OPC    
        Assert.assertEquals("isRequestTrackOn Bad Message", CbusMessage.isRequestTrackOn(m),false);
    }

    @Test
    public void testisTrackOnReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x05); // TON OPC
        Assert.assertEquals("isRequestTrackOn Good Reply", CbusMessage.isTrackOn(r),true);
        r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x04); // TOF OPC    
        Assert.assertEquals("isRequestTrackOn Bad Reply", CbusMessage.isTrackOn(r),false);
    }

    @Test
    public void testisTrackOffReply() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x04); // TOF OPC
        Assert.assertEquals("isRequestTrackOff Good Reply", CbusMessage.isTrackOff(r),true);
        r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x05); // TON OPC    
        Assert.assertEquals("isRequestTrackOff Bad Reply", CbusMessage.isTrackOff(r),false);
    }
    
    @Test
    public void testgetRequestTrackOnMessage() {
        CanMessage m = CbusMessage.getRequestTrackOn(0x12);
        Assert.assertTrue("getRequestTrackOn OPC", m.getElement(0) == 0x09); // RTON OPC
        Assert.assertTrue("getRequestTrackOn Length", m.getNumDataElements() == 1);
    }
    
    @Test
    public void testgetRequestTrackOffMessage() {
        CanMessage m = CbusMessage.getRequestTrackOff(0x12);
        Assert.assertTrue("getRequestTrackOff OPC", m.getElement(0) == 0x08); // RTON OPC
        Assert.assertTrue("getRequestTrackOff Length", m.getNumDataElements() == 1);
    }

    @Test
    public void testgetDataLength() {
        CanReply r = new CanReply(0x12);
        CanMessage m = new CanMessage(0x12);
        r.setElement(0, 0x04); // TOF OPC
        m.setElement(0, 0x04); // TOF OPC
        Assert.assertEquals("Data Length 0 r",0,CbusMessage.getDataLength(r));
        Assert.assertEquals("Data Length 0 m",0,CbusMessage.getDataLength(m));
        r.setElement(0, 0x11); // RQMN
        m.setElement(0, 0x11); // RQMN
        Assert.assertEquals("Data Length 0 r",0,CbusMessage.getDataLength(r));
        Assert.assertEquals("Data Length 0 m",0,CbusMessage.getDataLength(m));

        r.setElement(0, 0x83); // WCVB OPC
        m.setElement(0, 0x83); // WCVB OPC
        Assert.assertEquals("Data Length 4 r",4,CbusMessage.getDataLength(r));
        Assert.assertEquals("Data Length 4 m",4,CbusMessage.getDataLength(m));
        r.setElement(0, 0xe2); // NAME
        m.setElement(0, 0xe2); // NAME
        Assert.assertEquals("Data Length 7 r",7,CbusMessage.getDataLength(r));
        Assert.assertEquals("Data Length 7 m",7,CbusMessage.getDataLength(m));
        
        r = null;
        m = null;
    }
    
    @Test
    public void testsetgetPriority() {
        CanReply r = new CanReply(0x00);
        CanMessage m = new CanMessage(0x00);
        Assert.assertEquals("Priority 0 r",0,CbusMessage.getPri(r));
        Assert.assertEquals("Priority 0 m",0,CbusMessage.getPri(m));
        
        try {
            CbusMessage.setPri(r,0xff);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("invalid CBUS Pri value: 255", e.getMessage());
        }
        
        try {
            CbusMessage.setPri(m,0xff);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalArgumentException e) {
            Assert.assertEquals("Invalid CBUS Priority value: 255", e.getMessage());
        }
        
        CbusMessage.setPri(m,CbusConstants.DEFAULT_MINOR_PRIORITY);
        CbusMessage.setPri(r,CbusConstants.DEFAULT_MINOR_PRIORITY);
        Assert.assertEquals("Priority DEFAULT_MINOR_PRIORITY r",3,CbusMessage.getPri(r));
        Assert.assertEquals("Priority DEFAULT_MINOR_PRIORITY m",3,CbusMessage.getPri(m));

        CbusMessage.setPri(m,CbusConstants.DEFAULT_DYNAMIC_PRIORITY);
        CbusMessage.setPri(r,CbusConstants.DEFAULT_DYNAMIC_PRIORITY);
        Assert.assertEquals("Priority DEFAULT_DYNAMIC_PRIORITY r",2,CbusMessage.getPri(r));
        Assert.assertEquals("Priority DEFAULT_DYNAMIC_PRIORITY m",2,CbusMessage.getPri(m));
        
        r.setExtended(true);
        m.setExtended(true);
        
        Assert.assertEquals("Priority setExtended r",0,CbusMessage.getPri(r));
        Assert.assertEquals("Priority setExtended m",0,CbusMessage.getPri(m));        

        CbusMessage.setPri(m,CbusConstants.DEFAULT_MINOR_PRIORITY);
        CbusMessage.setPri(r,CbusConstants.DEFAULT_MINOR_PRIORITY);
        Assert.assertEquals("Priority setExtended DEFAULT_MINOR_PRIORITY r",3,CbusMessage.getPri(r));
        Assert.assertEquals("Priority setExtended DEFAULT_MINOR_PRIORITY m",3,CbusMessage.getPri(m));
        
    }
    
    @Test
    public void testsetgetId() {
        
        CanReply r = new CanReply(0x00);
        CanMessage m = new CanMessage(0x00);
        Assert.assertEquals("getId 0 r",0,CbusMessage.getId(r));
        Assert.assertEquals("getId 0 m",0,CbusMessage.getId(m));
        CbusMessage.setId(r,0x01);
        CbusMessage.setId(m,0x01);
        Assert.assertEquals("getId 1 r",1,CbusMessage.getId(r));
        Assert.assertEquals("getId 1 m",1,CbusMessage.getId(m));
        CbusMessage.setId(r,120);
        CbusMessage.setId(m,120);
        Assert.assertEquals("getId 120 r",120,CbusMessage.getId(r));
        Assert.assertEquals("getId 120 m",120,CbusMessage.getId(m));
        try {
            CbusMessage.setId(r,0xff);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
        }
        
        try {
            CbusMessage.setId(m,0xff);
            Assert.fail("Should have thrown an exception");
        } catch (Exception e) {
        }
        
        r.setExtended(true);
        m.setExtended(true);        
        
        CbusMessage.setId(r,0x01);
        CbusMessage.setId(m,0x01);
        Assert.assertEquals("getId 1 r",1,CbusMessage.getId(r));
        Assert.assertEquals("getId 1 m",1,CbusMessage.getId(m));
        CbusMessage.setId(r,120);
        CbusMessage.setId(m,120);
        Assert.assertEquals("getId 120 r",120,CbusMessage.getId(r));
        Assert.assertEquals("getId 120 m",120,CbusMessage.getId(m));
        try {
            CbusMessage.setId(r,0xffffff);
            Assert.fail("r Should have thrown an exception");
        } catch (Exception e) {
        }
        
        try {
            CbusMessage.setId(m,0xffffff);
            Assert.fail("m Should have thrown an exception");
        } catch (Exception e) {
        }        
        
    }
    
    @Test
    public void testisArst() {
        CanReply r = new CanReply(0x12);
        r.setNumDataElements(1);
        r.setElement(0, 0x07); // Arst OPC
        Assert.assertTrue(CbusMessage.isArst(r));
        r.setElement(0, 0x06);
        Assert.assertFalse(CbusMessage.isArst(r));
        r = null;
    }
    
    @Test
    public void testgetReadCV() {
        CanMessage m = CbusMessage.getReadCV(1,jmri.ProgrammingMode.PAGEMODE,0x12);
        Assert.assertEquals("PAGEMODE","[592] 84 FF 00 01 02",m.toString());
        m = CbusMessage.getReadCV(255,jmri.ProgrammingMode.DIRECTBITMODE,0x12);
        Assert.assertEquals("DIRECTBITMODE","[592] 84 FF 00 FF 01",m.toString());
        m = CbusMessage.getReadCV(214,jmri.ProgrammingMode.DIRECTBYTEMODE,0x12);
        Assert.assertEquals("DIRECTBYTEMODE","[592] 84 FF 00 D6 00",m.toString());
        m = CbusMessage.getReadCV(214,jmri.ProgrammingMode.REGISTERMODE,0x12);
        Assert.assertEquals("REGISTERMODE","[592] 84 FF 00 D6 03",m.toString());
        m = null;
    }
    
    @Test
    public void testgetgetWriteCV() {
        CanMessage m = CbusMessage.getWriteCV(1,211,jmri.ProgrammingMode.PAGEMODE,0x12);
        Assert.assertEquals("PAGEMODE","[592] A2 FF 00 01 02 D3",m.toString());
        m = CbusMessage.getWriteCV(255,1,jmri.ProgrammingMode.DIRECTBITMODE,0x12);
        Assert.assertEquals("DIRECTBITMODE","[592] A2 FF 00 FF 01 01",m.toString());
        m = CbusMessage.getWriteCV(214,0,jmri.ProgrammingMode.DIRECTBYTEMODE,0x12);
        Assert.assertEquals("DIRECTBYTEMODE","[592] A2 FF 00 D6 00 00",m.toString());
        m = CbusMessage.getWriteCV(214,123,jmri.ProgrammingMode.REGISTERMODE,0x12);
        Assert.assertEquals("REGISTERMODE","[592] A2 FF 00 D6 03 7B",m.toString());
        m = null;
    }    
    
    
    @Test
    public void testgetOpsModeWriteCV() {
        CanMessage m = CbusMessage.getOpsModeWriteCV(22,false,211,255,0x12);
        Assert.assertEquals("getOpsModeWriteCV","[592] C1 00 16 00 D3 05 FF",m.toString());
    }        
    
    @Test
    public void testgetBootEntry() {
        CanMessage m = CbusMessage.getBootEntry(43215,0x12);
        Assert.assertEquals("getBootEntry","[592] 5C A8 CF",m.toString());
    }

    @Test
    public void testgetBootNop() {
        CanMessage m = CbusMessage.getBootNop(0x3D,0x12);
        Assert.assertEquals("getBootNop","[16000004] 00 00 3D 00 0D 00 00 00",m.toString());
    }    

    @Test
    public void testgetBootReset() {
        CanMessage m = CbusMessage.getBootReset(0x12);
        Assert.assertEquals("getBootReset","[16000004] 00 00 00 00 0D 01 00 00",m.toString());
    }

    @Test
    public void testgetBootInitialise() {
        CanMessage m = CbusMessage.getBootInitialise(202,0x12);
        Assert.assertEquals("getBootInitialise","[16000004] 00 00 CA 00 0D 02 00 00",m.toString());
    }

    @Test
    public void testgetBootCheck() {
        CanMessage m = CbusMessage.getBootCheck(123,0x12);
        Assert.assertEquals("getBootCheck","[16000004] 00 00 00 00 0D 03 00 7B",m.toString());
    }

    @Test
    public void testgetBootTest() {
        CanMessage m = CbusMessage.getBootTest(0x12);
        Assert.assertEquals("getBootTest","[16000004] 00 00 00 00 0D 04 00 00",m.toString());
    }

    @Test
    public void testgetBootWriteData() {
        CanMessage m = CbusMessage.getBootWriteData( new int[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08},0x12);
        Assert.assertEquals("getBootWriteData","[16000005] 01 02 03 04 05 06 07 08",m.toString());
        
        m = CbusMessage.getBootWriteData( new int[]{0x01,0x02},0x12);
        JUnitAppender.assertErrorMessageStartsWith("Exception in bootloader data");
        m = null;
    }

    @Test
    public void testisBootError() {
        CanReply r = new CanReply(0x14);
        r.setExtended(false);
        r.setElement(0,7);
        Assert.assertEquals("isBootError fff",false,CbusMessage.isBootError(r)); // false false false
        r.setElement(0,0);
        Assert.assertEquals("isBootError ffp",false,CbusMessage.isBootError(r)); // ffp
        r.setHeader(0x10000004);
        Assert.assertEquals("isBootError fpp",false,CbusMessage.isBootError(r)); // fpp
        r.setElement(0,7);
        Assert.assertEquals("isBootError fpf",false,CbusMessage.isBootError(r)); // fpf
        r.setExtended(true);
        Assert.assertEquals("isBootError ppf",false,CbusMessage.isBootError(r)); // ppf
        r.setHeader(0x14);
        Assert.assertEquals("isBootError pff",false,CbusMessage.isBootError(r)); // pff
        r.setHeader(0x10000004);
        r.setElement(0,0);
        Assert.assertEquals("isBootError ppp",true,CbusMessage.isBootError(r)); // ppp
        r = null;
    }

    @Test
    public void testisBootOK() {
        CanReply r = new CanReply(0x14);
        r.setExtended(false);
        r.setElement(0,7);
        Assert.assertEquals("isBootOK fff",false,CbusMessage.isBootOK(r)); // false false false
        r.setElement(0,1);
        Assert.assertEquals("isBootOK ffp",false,CbusMessage.isBootOK(r)); // ffp
        r.setHeader(0x10000004);
        Assert.assertEquals("isBootOK fpp",false,CbusMessage.isBootOK(r)); // fpp
        r.setElement(0,7);
        Assert.assertEquals("isBootOK fpf",false,CbusMessage.isBootOK(r)); // fpf
        r.setExtended(true);
        Assert.assertEquals("isBootOK ppf",false,CbusMessage.isBootOK(r)); // ppf
        r.setHeader(0x14);
        Assert.assertEquals("isBootOK pff",false,CbusMessage.isBootOK(r)); // pff
        r.setHeader(0x10000004);
        r.setElement(0,1);
        Assert.assertEquals("isBootOK ppp",true,CbusMessage.isBootOK(r)); // ppp
        r = null;
    }



    @Test
    public void testisBootConfirm() {
        CanReply r = new CanReply(0x14);
        r.setExtended(false);
        r.setElement(0,7);
        Assert.assertEquals("isBootConfirm fff",false,CbusMessage.isBootConfirm(r)); // false false false
        r.setElement(0,2);
        Assert.assertEquals("isBootConfirm ffp",false,CbusMessage.isBootConfirm(r)); // ffp
        r.setHeader(0x10000004);
        Assert.assertEquals("isBootConfirm fpp",false,CbusMessage.isBootConfirm(r)); // fpp
        r.setElement(0,7);
        Assert.assertEquals("isBootConfirm fpf",false,CbusMessage.isBootConfirm(r)); // fpf
        r.setExtended(true);
        Assert.assertEquals("isBootConfirm ppf",false,CbusMessage.isBootConfirm(r)); // ppf
        r.setHeader(0x14);
        Assert.assertEquals("isBootConfirm pff",false,CbusMessage.isBootConfirm(r)); // pff
        r.setHeader(0x10000004);
        r.setElement(0,2);
        Assert.assertEquals("isBootConfirm ppp",true,CbusMessage.isBootConfirm(r)); // ppp
        r = null;
    }
    
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(CbusMessageTest.class);

}
