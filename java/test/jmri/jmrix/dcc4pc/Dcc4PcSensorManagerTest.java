package jmri.jmrix.dcc4pc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcc4PcSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private Dcc4PcReporterManager rm = null;

    @Override
    public String getSystemName(int i) {
        return "DS0:" + i;
    }
    
    @Override
    protected String getASystemNameWithNoPrefix() {
        return "0:1";
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create -- requires module:contact form.
        jmri.Sensor t = l.provideSensor("DS0:" + getNumToTest1());
        // check
        assertNotNull( t, "real object returned ");
        assertEquals( t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct ");
    }

    @Override
    @Test
    public void testProvideName() {
        // create -- requires module:contact form.
        jmri.Sensor t = l.provideSensor("DS0:" + getNumToTest1());
        // check
        assertNotNull( t, "real object returned ");
        assertTrue( t == l.getBySystemName(getSystemName(getNumToTest1())), "system name correct ");
    }

    @Test
    public void checkReceiveRawRailComData() {
        // Raw RailCom message copied from section 9.3 of the 
        // DCC4PC omnibus specification.
        // packet contains both occupancy information and RailCom data.
        byte packetData[] = {(byte) 0x0b, (byte) 0x00, (byte) 0x42, (byte) 0x01,
            (byte) 0x3F, (byte) 0xA8, (byte) 0x90, (byte) 0x90, (byte) 0xC0,
            (byte) 0x00, (byte) 0x00, (byte) 0x42, (byte) 0x02, (byte) 0xA3,
            (byte) 0xAC, (byte) 0x8B, (byte) 0x69, (byte) 0x4E, (byte) 0xA5,
            (byte) 0x99, (byte) 0x9A, (byte) 0x01, (byte) 0x01, (byte) 0x91,
            (byte) 0x04, (byte) 0x22, (byte) 0x99, (byte) 0xA5, (byte) 0xA3,
            (byte) 0xAC, (byte) 0x81, (byte) 0x42, (byte) 0x03, (byte) 0x3F,
            (byte) 0xBC, (byte) 0x80, (byte) 0x90, (byte) 0xC0, (byte) 0x00,
            (byte) 0x20, (byte) 0x24, (byte) 0x8E, (byte) 0xD4, (byte) 0x4E,
            (byte) 0x59, (byte) 0xA3, (byte) 0xAC};
        Dcc4PcReply rep = new Dcc4PcReply(packetData);
        rep.setOriginalRequest(Dcc4PcMessage.pollBoard(0));
        ((Dcc4PcSensorManager) l).addActiveBoard(0, "1", 16, 0);
        l.provideSensor("DS0:1");
        l.provideSensor("DS0:2");
        l.provideSensor("DS0:3");
        l.provideSensor("DS0:4");
        // creae a reporter for DR0:4, because it has railcom data.
        rm.provideReporter("DR0:4");
        ((Dcc4PcSensorManager) l).reply(rep);
        JUnitUtil.waitThreadTerminated("Dcc4PCSensor Process Packet for 0");
        
        /* Asserts below fail. The Sensor does not go active */
        
        // JUnitUtil.waitFor(() -> {
        //     return l.provideSensor("DS0:4").getState() == jmri.Sensor.ACTIVE;
        // },"Sensor goes active");
        // Assert.assertEquals("sensor DS0:1 state after packet", jmri.Sensor.INACTIVE, l.provideSensor("DS0:1").getState());
        // Assert.assertEquals("sensor DS0:2 state after packet", jmri.Sensor.INACTIVE, l.provideSensor("DS0:2").getState());

        // packet above has input 2 (sensor 3) occupied
        //Assert.assertEquals("sensor DS0:3 state after packet",jmri.Sensor.ACTIVE,l.getSensor("DS0:3").getState());
        // packet above has input 3 (sensor 4) occupied with railcom data.
        //Assert.assertEquals("DR0:4 report","",rm.provideReporter("DR0:4").getCurrentReport());
        //Assert.assertEquals("sensor DS0:4 state after packet",jmri.Sensor.ACTIVE,l.getSensor("DS0:4").getState());
    }

    @Test
    @Disabled("it appears this packet is not yet decoded")
    public void checkReceiveCookedRailComData() {
        // Cooked RailCom message copied from section 9.4 of the 
        // DCC4PC omnibus specification.
        // This packet contains occupancy information only.
        byte packetData[] = {(byte) 0x0b, (byte) 0x00, (byte) 0x02, (byte) 0x01, (byte) 0x00};
        Dcc4PcReply rep = new Dcc4PcReply(packetData);
        rep.setOriginalRequest(Dcc4PcMessage.pollBoard(0));
        ((Dcc4PcSensorManager) l).addActiveBoard(0, "1", 16, 0);
        l.provideSensor("DS0:1");
        l.provideSensor("DS0:2");
        l.provideSensor("DS0:3");
        l.provideSensor("DS0:4");
        ((Dcc4PcSensorManager) l).reply(rep);
        JUnitUtil.waitFor(() -> {
            return l.provideSensor("DS0:3").getState() == jmri.Sensor.ACTIVE;}, "Sensor goes active");
        assertEquals( jmri.Sensor.ACTIVE, l.provideSensor("DS0:3").getState(), "sensor state after packet");

    }

    // 2026-10-12: a short cut track power for about 70 s. Each attempt at 62.5 kbps during
    // the outage failed and doubled the retry delay, so once power was back the board stayed
    // at 8 kbps (with Get Data failing) for 5 minutes.
    @Test
    public void testSpeedRetriedAsSoonAsBoardAnswersAgain() {
        Dcc4PcSensorManager sm = (Dcc4PcSensorManager) l;
        sm.addActiveBoard(1, "1", 16, 0);
        Dcc4PcSensorManager.ActiveBoard board = sm.activeBoards.get(1);
        board.speedChecked = true;
        board.busSpeed = Dcc4PcMessage.SPEED_62K5;

        // track power off: the board answers nothing
        for (int i = 0; i < 9; i++) {
            board.resetReplied(false);
        }
        jmri.util.JUnitAppender.assertWarnMessage("Board 1 not answering, waiting for it at 8 kbps");
        assertTrue(board.lostContact);
        assertEquals(0, board.busSpeed, "back at 8 kbps");
        Assertions.assertFalse(board.speedPending, "no speed change while it can't answer");
        assertEquals(Dcc4PcSensorManager.ActiveBoard.FIRST_SPEED_RETRY, board.speedRetryDelay, "no backoff");

        // power back: the next reset succeeds
        long before = System.currentTimeMillis();
        board.resetReplied(true);
        Assertions.assertFalse(board.lostContact);
        assertTrue(board.nextSpeedRetry != 0 && board.nextSpeedRetry <= System.currentTimeMillis()
                && board.nextSpeedRetry >= before, "62.5 kbps retried straight away");
    }

    // 2026-10-13: a POM read of CV 8 on 9328 (packet E4 70 E4 07 00) was answered, but the
    // packet decoded as address 0 (taken for an accessory packet), so the value reached no loco.
    @Test
    public void testDecodeDccPacketAddresses() {
        Dcc4PcSensorManager sm = (Dcc4PcSensorManager) l;
        sm.addActiveBoard(1, "1", 16, 0);
        Dcc4PcSensorManager.ActiveBoard board = sm.activeBoards.get(1);

        board.decodeDCCPacket(new int[]{0xE4, 0x70, 0xE4, 0x07, 0x00}); // POM verify byte, CV 8
        assertEquals(9328, board.addr, "long address");
        assertEquals(Dcc4PcSensorManager.LONG_ADDRESS, board.dcc_addr_type);
        assertEquals(8, board.cvNumber, "CV");

        board.decodeDCCPacket(new int[]{0xE5, 0x07, 0xE4, 0x07, 0x00}); // CV 8 on long address 9479
        assertEquals(9479, board.addr);
        board.decodeDCCPacket(new int[]{0x03, 0xEC, 0x01, 0x05}); // POM write CV 2 on short address 3
        assertEquals(3, board.addr, "short address");
        assertEquals(2, board.cvNumber);

        board.decodeDCCPacket(new int[]{0x81, 0xF8, 0x79}); // basic accessory packet
        assertEquals(0, board.addr, "accessory packets have no loco address");
    }

    private Dcc4PcSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        Dcc4PcTrafficController tc = new Dcc4PcTrafficController() {
            @Override
            public void sendDcc4PcMessage(Dcc4PcMessage m, Dcc4PcListener reply) {
            }
        };
        memo = new Dcc4PcSystemConnectionMemo(tc);
        memo.configureManagers();
        l = memo.getSensorManager();
        rm = memo.getReporterManager();
    }

    @AfterEach
    public void tearDown() {
        l.dispose();
        assertNotNull(memo);
        memo.getDcc4PcTrafficController().terminateThreads();
        memo.dispose();
        l = null;
        JUnitUtil.tearDown();

    }

    // private static final Logger log = LoggerFactory.getLogger(Dcc4PcSensorManagerTest.class);
}
