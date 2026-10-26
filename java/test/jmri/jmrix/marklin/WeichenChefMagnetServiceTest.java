package jmri.jmrix.marklin;

import jmri.jmrix.marklin.mcan.cdb.CdBConfigCommands;
import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link WeichenChefMagnetService}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
class WeichenChefMagnetServiceTest {

    // ---- helpers -----------------------------------------------------------

    private static McanDevice makeDev(int moduleId) {
        return new McanDevice("M", McanDeviceIdentity.forCdB(0x22, moduleId));
    }

    /**
     * A captured scheduled action: the runnable plus the delay it was scheduled with.
     */
    static class ScheduledAction {
        final Runnable action;
        final long delayMs;
        ScheduledAction(Runnable action, long delayMs) {
            this.action = action;
            this.delayMs = delayMs;
        }
    }

    /**
     * Test scheduler that captures delayed runnables instead of actually sleeping.
     * Call {@link #flush()} to run them in order.
     */
    static class TestScheduler {
        final List<ScheduledAction> pending = new ArrayList<>();

        WeichenChefMagnetService.DelayScheduler asDelayScheduler() {
            return (r, d) -> pending.add(new ScheduledAction(r, d));
        }

        void flush() {
            // copy to allow re-entrant scheduling
            List<ScheduledAction> toRun = new ArrayList<>(pending);
            pending.clear();
            toRun.forEach(a -> a.action.run());
        }
    }

    // ---- generation probe frame-order tests --------------------------------

    @Test
    void sendGenerationProbe_firstFrameIsSelect() {
        List<int[]> sent = new ArrayList<>();
        TestScheduler sched = new TestScheduler();
        McanDevice dev = makeDev(5);
        WeichenChefMagnetService svc = new WeichenChefMagnetService(dev, sent::add, sched.asDelayScheduler());

        svc.sendGenerationProbe();
        sched.flush(); // fires select
        sched.flush(); // fires read
        sched.flush(); // fires deselect

        assertFalse(sent.isEmpty(), "probe must send at least one frame");
        // select: DLC=4, reg byte[6]=0xF7 (REG_SELECT)
        assertEquals(0xF7, sent.get(0)[6] & 0xFF, "first frame must be a select (reg=0xF7)");
    }

    @Test
    void sendGenerationProbe_secondFrameIsRead_slot0_reg1() {
        List<int[]> sent = new ArrayList<>();
        TestScheduler sched = new TestScheduler();
        McanDevice dev = makeDev(5);
        WeichenChefMagnetService svc = new WeichenChefMagnetService(dev, sent::add, sched.asDelayScheduler());

        svc.sendGenerationProbe();
        sched.flush();
        sched.flush();
        sched.flush();

        assertTrue(sent.size() >= 2, "probe must send at least select + read frames");
        int[] readFrame = sent.get(1);
        // read frame: DLC=2, frame[5]=slot=0, frame[6]=reg=1
        assertEquals(2, readFrame[4] & 0xFF, "read frame DLC must be 2");
        assertEquals(0, readFrame[5] & 0xFF, "read frame slot must be 0");
        assertEquals(1, readFrame[6] & 0xFF, "read frame reg must be 1");
    }

    @Test
    void sendGenerationProbe_thirdFrameIsDeselect() {
        List<int[]> sent = new ArrayList<>();
        TestScheduler sched = new TestScheduler();
        McanDevice dev = makeDev(5);
        WeichenChefMagnetService svc = new WeichenChefMagnetService(dev, sent::add, sched.asDelayScheduler());

        svc.sendGenerationProbe();
        sched.flush();
        sched.flush();
        sched.flush();

        assertEquals(3, sent.size(), "probe must send exactly select + read + deselect frames");
        // deselect: DLC=2, reg byte[6]=0xFE (REG_DESELECT)
        assertEquals(0xFE, sent.get(2)[6] & 0xFF, "third frame must be a deselect (reg=0xFE)");
    }

    @Test
    void sendGenerationProbe_framesTargetCorrectModule() {
        List<int[]> sent = new ArrayList<>();
        TestScheduler sched = new TestScheduler();
        McanDevice dev = makeDev(7);
        WeichenChefMagnetService svc = new WeichenChefMagnetService(dev, sent::add, sched.asDelayScheduler());

        svc.sendGenerationProbe();
        sched.flush();
        sched.flush();
        sched.flush();

        // module 7 → hash low byte = 0x80 + 7 = 0x87
        for (int i = 0; i < 3; i++) {
            assertEquals(0x87, sent.get(i)[3] & 0xFF,
                "frame " + i + " must target module 7 (byte[3]=0x87)");
        }
    }

    @Test
    void sendGenerationProbe_delaysAre300msEach() {
        TestScheduler sched = new TestScheduler();
        McanDevice dev = makeDev(5);
        WeichenChefMagnetService svc = new WeichenChefMagnetService(dev, f -> {}, sched.asDelayScheduler());

        svc.sendGenerationProbe();

        // After initial call: select fires immediately (delay 0 or synchronously), then
        // 300ms to read, 300ms to deselect.
        // Flush once to fire first batch; inspect what gets scheduled.
        assertEquals(1, sched.pending.size(), "one action should be pending after sendGenerationProbe");

        sched.flush(); // fires select + schedules read
        assertEquals(1, sched.pending.size(), "read should be scheduled after select fires");
        assertEquals(300L, sched.pending.get(0).delayMs, "read delay must be 300 ms");

        sched.flush(); // fires read + schedules deselect
        assertEquals(1, sched.pending.size(), "deselect should be scheduled after read fires");
        assertEquals(300L, sched.pending.get(0).delayMs, "deselect delay must be 300 ms");
    }

    // ---- handleCdBReply tests (existing behavior, unchanged) ---------------

    @Test
    void handleCdBReply_classifiesGen001_fromLowFirmware() {
        McanDevice dev = makeDev(3);
        dev.setDeviceState(McanDeviceState.INITIALIZING);
        WeichenChefMagnetService svc = new WeichenChefMagnetService(dev, f -> {}, (r, d) -> {});

        int[] raw = CdBConfigCommands.buildReadReply(0x22, 3, 0, 1, 0x0204); // fw=2.4
        MarklinReply reply = toReply(raw);
        CdBFrameParser.CdBFrame frame = parseFrame(raw);

        svc.handleCdBReply(frame, reply);

        assertEquals(WeichenChefMagnetGeneration.GEN_001, svc.getGeneration());
        assertEquals(McanDeviceState.ONLINE, dev.getDeviceState());
    }

    @Test
    void handleCdBReply_classifiesGen002Plus_fromHighFirmware() {
        McanDevice dev = makeDev(3);
        dev.setDeviceState(McanDeviceState.INITIALIZING);
        WeichenChefMagnetService svc = new WeichenChefMagnetService(dev, f -> {}, (r, d) -> {});

        int[] raw = CdBConfigCommands.buildReadReply(0x22, 3, 0, 1, 0x0305); // fw=3.5
        MarklinReply reply = toReply(raw);
        CdBFrameParser.CdBFrame frame = parseFrame(raw);

        svc.handleCdBReply(frame, reply);

        assertEquals(WeichenChefMagnetGeneration.GEN_002_PLUS, svc.getGeneration());
        assertEquals(McanDeviceState.ONLINE, dev.getDeviceState());
    }

    @Test
    void handleCdBReply_ignoredForWrongModule() {
        McanDevice dev = makeDev(3);
        dev.setDeviceState(McanDeviceState.INITIALIZING);
        WeichenChefMagnetService svc = new WeichenChefMagnetService(dev, f -> {}, (r, d) -> {});

        // reply addressed to module 9, not 3
        int[] raw = CdBConfigCommands.buildReadReply(0x22, 9, 0, 1, 0x0204);
        svc.handleCdBReply(parseFrame(raw), toReply(raw));

        assertEquals(WeichenChefMagnetGeneration.UNKNOWN, svc.getGeneration());
        assertEquals(McanDeviceState.INITIALIZING, dev.getDeviceState());
    }

    @Test
    void handleCdBReply_ignoredForWrongReg() {
        McanDevice dev = makeDev(3);
        dev.setDeviceState(McanDeviceState.INITIALIZING);
        WeichenChefMagnetService svc = new WeichenChefMagnetService(dev, f -> {}, (r, d) -> {});

        // reg=2, not reg=1
        int[] raw = CdBConfigCommands.buildReadReply(0x22, 3, 0, 2, 0x0204);
        svc.handleCdBReply(parseFrame(raw), toReply(raw));

        assertEquals(WeichenChefMagnetGeneration.UNKNOWN, svc.getGeneration());
    }

    // ---- helpers -----------------------------------------------------------

    private static MarklinReply toReply(int[] raw) {
        MarklinReply r = new MarklinReply();
        for (int i = 0; i < raw.length; i++) r.setElement(i, raw[i] & 0xFF);
        return r;
    }

    private static CdBFrameParser.CdBFrame parseFrame(int[] raw) {
        jmri.jmrix.marklin.mcan.classify.ClassifiedFrame cf =
            jmri.jmrix.marklin.mcan.classify.ProtocolClassifier.classify(raw);
        return CdBFrameParser.parse(cf);
    }
}
