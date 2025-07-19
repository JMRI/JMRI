package jmri.jmrix.loconet;

import jmri.JmriException;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

public class SlotManagerAndPowerManTest {

        private LocoNetInterfaceScaffold lnis;
        private LnPowerManager pwr;
        private SlotManager sm;
        private LocoNetSystemConnectionMemo memo;

        @Test
        public void testPowerManagerSlotZero() throws JmriException {
            JUnitUtil.waitFor(()->{return lnis.outbound.size() == 1;},"Power Query Start");

            Assert.assertEquals("Request Slot Zero",
                    "BB 00 00 00",
                    lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

            LocoNetMessage csAnswer = new LocoNetMessage(new int[] {
                    0xE7, 0x0E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x47, 0x00, 0x00, 0x00, 0x00, 0x00, 0x51});
            lnis.sendTestMessage(csAnswer);
            JUnitUtil.waitFor(()->{return lnis.outbound.size() == 3;},"Query Slot 248 & 250");

            Assert.assertEquals("Request Slot 248",
                    "BB 78 41 00",
                    lnis.outbound.elementAt(lnis.outbound.size() - 2).toString());

            Assert.assertEquals("Request Slot 250",
                    "BB 7A 41 00",
                    lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

            csAnswer = new LocoNetMessage(new int[] {
                    0xE6, 0x15, 0x01, 0x78, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x15, 0x00,
                    0x02, 0x01, 0x12, 0x0B, 0x7A});  // not a CS a DB210
            lnis.sendTestMessage(csAnswer);
            Assert.assertEquals("CS Type Blank","<unknown>",sm.getSlot248CommandStationType());

            csAnswer = new LocoNetMessage(new int[] {
                    0xE6, 0x15, 0x01, 0x78, 0x08, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1A, 0x00,
                    0x01, 0x00, 0x37, 0x03, 0x12}); // a CS but not in Booster mode
            lnis.sendTestMessage(csAnswer);
            Assert.assertEquals("CS Type Blank","<unknown>",sm.getSlot248CommandStationType());

            csAnswer = new LocoNetMessage(new int[] {
                    0xE6, 0x15, 0x01, 0x7A, 0x00, 0x00, 0x14, 0x00,
                    0x50, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x1A, 0x7F, 0x37, 0x03, 0x62});  // a cs but not booster, with old slot info
            lnis.sendTestMessage(csAnswer);
            Assert.assertEquals("CS Slots 0",0,sm.getSlot250CSSlots());

            csAnswer = new LocoNetMessage(new int[] {
                    0xE6, 0x15, 0x01, 0x78, 0x08, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1A, 0x00,
                    0x01, 0x00, 0x37, 0x43, 0x12}); // a CS in command station mode
            lnis.sendTestMessage(csAnswer);
            Assert.assertEquals("CS Type Blank","DCS210PLUS",sm.getSlot248CommandStationType());

            csAnswer = new LocoNetMessage(new int[] {
                    0xE6, 0x15, 0x01, 0x7A, 0x00, 0x00, 0x14, 0x00,
                    0x50, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x1A, 0x7F, 0x37, 0x01, 0x62});  // a cs but wrong serial
            lnis.sendTestMessage(csAnswer);
            Assert.assertEquals("CS Slots 100",0,sm.getSlot250CSSlots());

            csAnswer = new LocoNetMessage(new int[] {
                    0xE6, 0x15, 0x01, 0x7A, 0x00, 0x00, 0x14, 0x00,
                    0x50, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x1A, 0x7F, 0x37, 0x03, 0x62});  // a cs but right serial
            lnis.sendTestMessage(csAnswer);
            Assert.assertEquals("CS Slots 100",100,sm.getSlot250CSSlots());

        }

        @BeforeEach
        public void setUp() {
            JUnitUtil.setUp();
            memo = new LocoNetSystemConnectionMemo();
            lnis = new LocoNetInterfaceScaffold(memo);
            memo.setLnTrafficController(lnis);
            memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS240,false,false,false, false, false);
            sm = new SlotManager(lnis);
            pwr = new LnPowerManager(memo);
        }

        @AfterEach
        public void tearDown() {
            Assertions.assertNotNull(memo);
            Assertions.assertNotNull(sm);
            Assertions.assertNotNull(pwr);
            memo.dispose();
            memo= null;
            sm.dispose();
            sm = null;
            pwr.dispose();
            pwr=null;
            lnis = null;
            JUnitUtil.tearDown();
        }

    }
