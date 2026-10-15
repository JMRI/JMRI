package jmri.jmrix.dcc4pc;

import jmri.progdebugger.DebugProgrammerManager;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcc4PcOpsModeProgrammerTest extends jmri.jmrix.AbstractOpsModeProgrammerTestBase {

    @Test
    @Override
    public void testGetCanRead(){
       Assert.assertTrue("can read",programmer.getCanRead());
    }

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.OPSBITMODE,
                programmer.getMode());        
    }

    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.OPSBITMODE,
                ((Dcc4PcOpsModeProgrammer)programmer).getBestMode());        
    }

    /**
     * The other connection's programmer manager. Its programmer sends the POM
     * read and answers with readReply, as a command station would.
     */
    private static class CommandStationManager extends jmri.managers.DefaultProgrammerManager {

        final int[] readReply;

        /**
         * @param value  value the command station answers a read with
         * @param status status the command station answers a read with
         */
        CommandStationManager(int value, int status) {
            super(null);
            readReply = new int[]{value, status};
        }

        @Override
        public boolean isAddressedModePossible() {
            return true;
        }

        @Override
        protected jmri.AddressedProgrammer getConcreteAddressedProgrammer(boolean pLongAddress, int pAddress) {
            return new jmri.progdebugger.ProgDebugger(pLongAddress, pAddress) {
                @Override
                public void readCV(String CVname, jmri.ProgListener p) {
                    p.programmingOpReply(readReply[0], readReply[1]);
                }
            };
        }
    }

    /** Value and status from the read, and how many times the listener was called. */
    private final int[] result = {-1, -1, 0};

    private final jmri.ProgListener resultListener = (value, status) -> {
        result[0] = value;
        result[1] = status;
        result[2]++;
    };

    /** What Dcc4PcReporter does with a channel 2 POM reply for this loco. */
    private void railComReply(String address, int value) {
        jmri.RailCom tag = (jmri.RailCom) jmri.InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag(address);
        tag.setCvValue(value);
    }

    // The command station sends the POM read but can't read the answer; RailCom brings it back.
    @Test
    public void testReadValueFromRailCom() throws Exception {
        Dcc4PcOpsModeProgrammer p = new Dcc4PcOpsModeProgrammer(true, 9328,
                new CommandStationManager(0, jmri.ProgListener.NotImplemented));
        p.readCV("7", resultListener);
        Assert.assertEquals("still waiting after the command station failed", 0, result[2]);
        railComReply("9328", 42);
        JUnitUtil.waitFor(() -> result[2] > 0, "read finished");
        Assert.assertEquals(jmri.ProgListener.OK, result[1]);
        Assert.assertEquals(42, result[0]);
    }

    // A command station that reads RailCom itself answers first; a later RailCom reply changes nothing.
    @Test
    public void testReadValueFromCommandStation() throws Exception {
        Dcc4PcOpsModeProgrammer p = new Dcc4PcOpsModeProgrammer(true, 9328,
                new CommandStationManager(17, jmri.ProgListener.OK));
        p.readCV("7", resultListener);
        JUnitUtil.waitFor(() -> result[2] > 0, "read finished");
        railComReply("9328", 99);
        JUnitUtil.waitFor(50);
        Assert.assertEquals(jmri.ProgListener.OK, result[1]);
        Assert.assertEquals(17, result[0]);
        Assert.assertEquals("reported once", 1, result[2]);
    }

    @Test
    public void testConfirmMismatch() throws Exception {
        Dcc4PcOpsModeProgrammer p = new Dcc4PcOpsModeProgrammer(true, 9328,
                new CommandStationManager(0, jmri.ProgListener.NotImplemented));
        p.confirmCV("7", 40, resultListener);
        railComReply("9328", 42);
        JUnitUtil.waitFor(() -> result[2] > 0, "confirm finished");
        Assert.assertEquals(jmri.ProgListener.ConfirmFailed, result[1]);
    }

    /** A command station that records the packets it's asked to send. */
    private static class PacketRecorder implements jmri.CommandStation {

        final java.util.List<byte[]> packets = new java.util.concurrent.CopyOnWriteArrayList<>();
        final java.util.List<Integer> repeats = new java.util.concurrent.CopyOnWriteArrayList<>();
        final java.util.List<Long> times = new java.util.concurrent.CopyOnWriteArrayList<>();

        @Override
        public boolean sendPacket(@javax.annotation.Nonnull byte[] packet, int repeats) {
            packets.add(packet);
            this.repeats.add(repeats);
            times.add(System.currentTimeMillis());
            return true;
        }

        @Override
        public String getUserName() {
            return "Test Station";
        }

        @Override
        @javax.annotation.Nonnull
        public String getSystemPrefix() {
            return "T";
        }
    }

    // 2026-10-13: reading through LocoNet's own ops-mode programmer left it busy for ~8 s
    // after each read ("programmer in use" on the next CV). Reads now just send the POM
    // read packet and wait for RailCom; the command station's programmer isn't asked.
    @Test
    public void testReadSendsPomPacketAndWaitsForRailCom() throws Exception {
        CommandStationManager csm = new CommandStationManager(0, jmri.ProgListener.FailedTimeout);
        PacketRecorder cs = new PacketRecorder();
        Dcc4PcOpsModeProgrammer p = new Dcc4PcOpsModeProgrammer(true, 9328, csm, cs);

        p.readCV("8", resultListener);
        Assert.assertEquals("one packet sent", 1, cs.packets.size());
        byte[] packet = cs.packets.get(0);
        Assert.assertEquals("long address high", (byte) 0xE4, packet[0]);
        Assert.assertEquals("long address low", (byte) 0x70, packet[1]);
        Assert.assertEquals("verify byte", (byte) 0xE4, packet[2]);
        Assert.assertEquals("CV 8", (byte) 0x07, packet[3]);

        railComReply("9328", 151);
        JUnitUtil.waitFor(() -> result[2] > 0, "read finished");
        Assert.assertEquals(jmri.ProgListener.OK, result[1]);
        Assert.assertEquals(151, result[0]);

        // the next read can start straight away
        p.readCV("1", resultListener);
        railComReply("9328", 3);
        JUnitUtil.waitFor(() -> result[2] > 1, "second read finished");
        Assert.assertEquals(3, result[0]);
    }

    // 2026-10-13: resending every second built a backlog in the command station; sending
    // straight after another packet lost it (a busy LocoNet command station drops it).
    // Packets are now paced, and retried once.
    @Test
    public void testPacketsPacedAndRetriedOnce() throws Exception {
        PacketRecorder cs = new PacketRecorder();
        Dcc4PcOpsModeProgrammer p = new Dcc4PcOpsModeProgrammer(false, 3,
                new CommandStationManager(0, jmri.ProgListener.FailedTimeout), cs);
        p.readCV("1024", resultListener);
        Assert.assertEquals("sent straight away", 1, cs.packets.size());
        Assert.assertEquals("CV 1024: high CV bits in the instruction", (byte) 0xE7, cs.packets.get(0)[1]);
        Assert.assertEquals("read repeats", Dcc4PcOpsModeProgrammer.READ_REPEATS, (int) cs.repeats.get(0));
        JUnitUtil.waitFor(() -> cs.packets.size() == 2, "retried");
        Assert.assertTrue("retried after RETRY_AFTER",
                cs.times.get(1) - cs.times.get(0) >= Dcc4PcOpsModeProgrammer.RETRY_AFTER - 50);
        JUnitUtil.waitFor(Dcc4PcOpsModeProgrammer.RETRY_AFTER + 300);
        Assert.assertEquals("retried only once", 2, cs.packets.size());
        railComReply("3", 42);
        JUnitUtil.waitFor(() -> result[2] > 0, "read finished");
        Assert.assertEquals(42, result[0]);

        // a write straight after a read waits for the read's repeats
        p.readCV("1", resultListener);
        railComReply("3", 3);
        JUnitUtil.waitFor(() -> result[2] > 1, "second read finished");
        p.writeCV("3", 20, resultListener);
        JUnitUtil.waitFor(() -> cs.packets.size() == 4, "write sent");
        Assert.assertEquals("write repeats", Dcc4PcOpsModeProgrammer.WRITE_REPEATS, (int) cs.repeats.get(3));
        Assert.assertTrue("write paced after the read",
                cs.times.get(3) - cs.times.get(2) >= Dcc4PcOpsModeProgrammer.READ_REPEATS * Dcc4PcOpsModeProgrammer.GAP_PER_REPEAT - 15);
    }

    // 2026-10-13: index writes (CV 31, 32) through LocoNet's programmer took ~10 s each,
    // though the decoder had answered in 100 ms. Writes now finish on the RailCom reply.
    @Test
    public void testWriteFinishesOnRailComReply() throws Exception {
        PacketRecorder cs = new PacketRecorder();
        Dcc4PcOpsModeProgrammer p = new Dcc4PcOpsModeProgrammer(true, 9328,
                new CommandStationManager(0, jmri.ProgListener.FailedTimeout), cs);
        p.writeCV("31", 16, resultListener);
        byte[] packet = cs.packets.get(0);
        Assert.assertEquals("write byte", (byte) 0xEC, packet[2]);
        Assert.assertEquals("CV 31", (byte) 0x1E, packet[3]);
        Assert.assertEquals("value", (byte) 0x10, packet[4]);

        railComReply("9328", 0); // answered before the write took effect
        JUnitUtil.waitFor(100);
        Assert.assertEquals("not finished by the old value", 0, result[2]);
        railComReply("9328", 16);
        JUnitUtil.waitFor(() -> result[2] > 0, "write finished");
        Assert.assertEquals(jmri.ProgListener.OK, result[1]);
        Assert.assertEquals(16, result[0]);
    }

    // The decoder keeps answering the old value: the write didn't take.
    @Test
    public void testWriteNotTaken() throws Exception {
        PacketRecorder cs = new PacketRecorder();
        Dcc4PcOpsModeProgrammer p = new Dcc4PcOpsModeProgrammer(true, 9328,
                new CommandStationManager(0, jmri.ProgListener.FailedTimeout), cs);
        p.writeCV("3", 20, resultListener);
        railComReply("9328", 15);
        JUnitUtil.waitFor(() -> result[2] > 0, "write ended");
        Assert.assertEquals(jmri.ProgListener.ConfirmFailed, result[1]);
        Assert.assertEquals(15, result[0]);
        jmri.util.JUnitAppender.assertWarnMessage("CV 3 of 9328 still reads 15 after writing 20");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        Dcc4PcOpsModeProgrammer.resetPacing(); // each test's first packet goes out straight away
        Dcc4PcProgrammerManager pm = new Dcc4PcProgrammerManager(new DebugProgrammerManager());
        Dcc4PcOpsModeProgrammer t = new Dcc4PcOpsModeProgrammer(false, 5, pm);
        programmer = t;
    }

    @AfterEach
    @Override
    public void tearDown() {
        programmer = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // private static final Logger log = LoggerFactory.getLogger(Dcc4PcOpsModeProgrammerTest.class);
}
