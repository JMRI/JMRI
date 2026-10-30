package jmri.jmrix.marklin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.SwingUtilities;
import jmri.jmrix.marklin.WeichenChefMagnetService.SaveScope;
import jmri.jmrix.marklin.WeichenChefMagnetService.State;
import jmri.jmrix.marklin.mcan.cdb.CdBConfigCommands;
import jmri.jmrix.marklin.mcan.cdb.CdBFrameParser;
import jmri.jmrix.marklin.mcan.classify.ProtocolClassifier;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;
import jmri.util.ThreadingUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WeichenChefMagnetServiceTest {
    private final List<int[]> sent = new ArrayList<>();
    private final TreeMap<Long, List<Runnable>> scheduled = new TreeMap<>();
    private final Map<Integer, Integer> registers = new HashMap<>();
    private final Set<Integer> missing = new HashSet<>();
    private WeichenChefMagnetService service;
    private McanDevice device;
    private long clock;
    private boolean applyWrites = true;

    @BeforeEach
    void setUp() {
        JUnitUtil.setUp();
        device = new McanDevice("M", McanDeviceIdentity.forCdB(0x22, 3));
        service = new WeichenChefMagnetService(device, this::send, (action, delay) ->
                scheduled.computeIfAbsent(clock + delay, k -> new ArrayList<>()).add(action));
    }

    @AfterEach
    void tearDown() {
        service.dispose();
        JUnitUtil.tearDown();
    }

    private int key(int reg, int slot) {
        return (reg << 8) | slot;
    }

    private void send(int[] frame) {
        sent.add(frame);
        int reg = frame[6];
        int slot = frame[5];
        int k = key(reg, slot);
        if (frame[1] == 0x46 && frame[4] == 4 && applyWrites) {
            registers.put(k, (frame[7] << 8) | frame[8]);
        }
        if (frame[1] != 0x44 || missing.contains(k) || reg == 224) {
            return;
        }
        int value = registers.getOrDefault(k, reg == 1 ? 0x0215 : reg == 6 || reg == 8
                ? 0x3800 + slot : reg == 7 ? 0x123 : reg == 2 ? 100 : reg == 16 ? 40 : reg == 17 ? 35 : 1);
        reply(reg, slot, value, 3, 4);
    }

    private void reply(int reg, int slot, int value, int module, int dlc) {
        int wireValue = reg == 6 || reg == 7 || reg == 8
                ? ((value & 0xFF) << 8) | (value >> 8) : value;
        int[] raw = CdBConfigCommands.buildReadReply(0x22, module, slot, reg, wireValue);
        raw[4] = dlc;
        service.handleCdBReply(CdBFrameParser.parse(ProtocolClassifier.classify(raw)), new MarklinReply(raw));
    }

    private void runNext() {
        Map.Entry<Long, List<Runnable>> entry = scheduled.pollFirstEntry();
        assertNotNull(entry);
        clock = entry.getKey();
        entry.getValue().forEach(Runnable::run);
    }

    private void runAll() {
        int limit = 1000;
        while (!scheduled.isEmpty()) {
            assertTrue(--limit > 0, "bounded retry/scheduler chain");
            runNext();
        }
    }

    private void select(WeichenChefMagnetGeneration profile) {
        ThreadingUtil.runOnGUI(() -> service.useProfile(profile));
        runAll();
        assertEquals(State.READY, service.getState());
    }

    @Test
    void positiveCapabilitiesIdentifyNewerEvenWithFirmwareTwo() {
        service.sendGenerationProbe();
        runAll();
        assertEquals(WeichenChefMagnetGeneration.GEN_002_PLUS, service.getDetectedGeneration());
        assertEquals(WeichenChefMagnetGeneration.GEN_002_PLUS, service.getGeneration());
        assertEquals(0x0215, service.getFirmware());
        assertEquals(McanDeviceState.ONLINE, device.getDeviceState());
        assertEquals(State.READY, service.getState());
        assertTrue(sent.stream().allMatch(f -> f[1] == 0x44), "capability test sends reads only");
    }

    @Test
    void incompletePositiveEvidenceNeverIdentifiesOlderAndRetestCanRecover() {
        missing.add(key(17, 8));
        service.sendGenerationProbe();
        runAll();
        assertEquals(WeichenChefMagnetGeneration.UNKNOWN, service.getGeneration());
        assertEquals(State.UNRESOLVED, service.getState());
        assertThrows(IllegalStateException.class, () -> service.testOutput(1, false));
        missing.clear();
        service.sendGenerationProbe();
        runAll();
        assertEquals(WeichenChefMagnetGeneration.GEN_002_PLUS, service.getGeneration());
    }

    @Test
    void timeoutDoesNotOverwritePositiveDetectionWithOlder() {
        service.sendGenerationProbe();
        runAll();
        for (int reg : new int[]{2, 16, 17}) {
            for (int slot = 5; slot <= 8; slot++) {
                missing.add(key(reg, slot));
            }
        }
        service.sendGenerationProbe();
        runAll();
        assertEquals(WeichenChefMagnetGeneration.UNKNOWN, service.getDetectedGeneration());
        assertEquals(State.UNRESOLVED, service.getState());
    }

    @Test
    void invalidShortAndWrongTargetRepliesDoNotEstablishCapabilities() {
        missing.add(key(2, 5));
        service.sendGenerationProbe();
        while (sent.size() < 5) {
            runNext();
        }
        reply(2, 5, 0, 3, 4);
        JUnitAppender.assertWarnMessage("Magnet module 3: invalid reg 2 slot 5 value 0");
        reply(2, 5, 100, 9, 4);
        reply(2, 5, 100, 3, 2);
        runAll();
        assertEquals(State.UNRESOLVED, service.getState());
    }

    @Test
    void olderOverridesUseFourTimingSlotsAndSeparatePropertyScope() {
        select(WeichenChefMagnetGeneration.GEN_001);
        assertTrue(sent.stream().filter(f -> f[6] == 2 || f[6] == 16 || f[6] == 17)
                .allMatch(f -> f[5] <= 4));
        Map<Integer, Integer> config = service.configuration(SaveScope.PROPERTIES);
        config.put(key(2, 1), 0x1234);
        sent.clear();
        ThreadingUtil.runOnGUI(() -> service.save(SaveScope.PROPERTIES, config));
        runAll();
        List<int[]> writes = writes();
        assertEquals(32, writes.size());
        assertArrayEquals(new int[]{0, 0x46, 0x2A, 0x83, 4, 1, 2, 0x12, 0x34, 0, 0, 0, 0}, writes.get(0));
        assertTrue(writes.stream().noneMatch(f -> f[6] == 254 || f[6] == 6 || f[6] == 7 || f[6] == 8));
        assertEquals(State.READY, service.getState());
    }

    private List<int[]> writes() {
        List<int[]> result = new ArrayList<>();
        sent.stream().filter(f -> f[1] == 0x46).forEach(result::add);
        return result;
    }

    @Test
    void newerWritesInterleaveDirectionsAndSnapshotQueuedValues() {
        select(WeichenChefMagnetGeneration.GEN_002_PLUS);
        Map<Integer, Integer> config = service.configuration(SaveScope.PROPERTIES);
        config.put(key(2, 5), 0x1234);
        sent.clear();
        ThreadingUtil.runOnGUI(() -> service.save(SaveScope.PROPERTIES, config));
        config.put(key(2, 5), 7);
        runAll();
        List<int[]> writes = writes();
        assertEquals(44, writes.size());
        assertEquals(1, writes.get(0)[5]);
        assertEquals(5, writes.get(1)[5]);
        assertEquals(0x12, writes.get(1)[7]);
        assertEquals(0x34, writes.get(1)[8]);
        assertEquals(State.READY, service.getState());
    }

    @Test
    void addressSaveUsesBigEndianWritesLittleEndianRepliesAndDlcTwoEndSave() {
        select(WeichenChefMagnetGeneration.GEN_001);
        assertEquals(0x3801, service.getValue(6, 1));
        Map<Integer, Integer> config = service.configuration(SaveScope.ADDRESSES);
        config.put(key(8, 2), 0x3834);
        sent.clear();
        ThreadingUtil.runOnGUI(() -> service.save(SaveScope.ADDRESSES, config));
        runAll();
        List<int[]> writes = writes();
        assertEquals(10, writes.size());
        assertArrayEquals(new int[]{0, 0x46, 0x2A, 0x83, 2, 0, 254, 0, 0, 0, 0, 0, 0}, writes.get(9));
        assertEquals(0x38, writes.get(6)[7]);
        assertEquals(0x34, writes.get(6)[8]);
        assertEquals(0x3834, service.getValue(8, 2));
        assertEquals(State.READY, service.getState());
    }

    @Test
    void feedbackSaveUsesOnlyEightFeedbackRegistersThenEndSave() {
        select(WeichenChefMagnetGeneration.GEN_001);
        Map<Integer, Integer> config = service.configuration(SaveScope.FEEDBACK);
        config.put(key(7, 1), 0x234);
        sent.clear();
        ThreadingUtil.runOnGUI(() -> service.save(SaveScope.FEEDBACK, config));
        runAll();
        List<int[]> writes = writes();
        assertEquals(9, writes.size());
        assertEquals(7, writes.get(0)[6]);
        assertEquals(2, writes.get(0)[7]);
        assertEquals(0x34, writes.get(0)[8]);
        assertEquals(2, writes.get(8)[4]);
        assertEquals(0x234, service.getValue(7, 1));
    }

    @Test
    void saveMismatchIsReportedInsteadOfClaimingPersistence() {
        select(WeichenChefMagnetGeneration.GEN_001);
        Map<Integer, Integer> config = service.configuration(SaveScope.PROPERTIES);
        config.put(key(2, 1), 200);
        applyWrites = false;
        ThreadingUtil.runOnGUI(() -> service.save(SaveScope.PROPERTIES, config));
        runAll();
        assertEquals(State.VERIFY_FAILED, service.getState());
        JUnitAppender.assertWarnMessage("Magnet module 3: 1 register replies missing or unverified");
    }

    @Test
    void partialLoadBlocksSaveAndReportsFailure() {
        missing.add(key(9, 4));
        ThreadingUtil.runOnGUI(() -> service.useProfile(WeichenChefMagnetGeneration.GEN_001));
        runAll();
        assertEquals(State.LOAD_FAILED, service.getState());
        JUnitAppender.assertWarnMessage("Magnet module 3: 1 register replies missing or unverified");
    }

    @Test
    void closingCancelsWritesAndDeactivatesCoilsInBothProfiles() {
        for (WeichenChefMagnetGeneration profile : new WeichenChefMagnetGeneration[]{
                WeichenChefMagnetGeneration.GEN_001, WeichenChefMagnetGeneration.GEN_002_PLUS}) {
            select(profile);
            sent.clear();
            ThreadingUtil.runOnGUI(() -> service.testOutput(3, true));
            ThreadingUtil.runOnGUI(service::closeConfiguration);
            runAll();
            assertEquals(2, sent.size());
            boolean older = profile == WeichenChefMagnetGeneration.GEN_001;
            int[] expected = {0, 0x16, older ? 0xFF : 3, older ? 0x3C : 0,
                6, 1, 0, 0x38, 3, 1, 1, 0, 0};
            assertArrayEquals(expected, sent.get(0), "reference frame, including first-address low byte");
            expected[10] = 0;
            assertArrayEquals(expected, sent.get(1));
        }
        select(WeichenChefMagnetGeneration.GEN_001);
        Map<Integer, Integer> config = service.configuration(SaveScope.PROPERTIES);
        sent.clear();
        ThreadingUtil.runOnGUI(() -> service.save(SaveScope.PROPERTIES, config));
        runNext();
        ThreadingUtil.runOnGUI(service::closeConfiguration);
        runAll();
        assertEquals(1, sent.size());
    }

    @Test
    void scheduledDeactivationOccursAfterFiveHundredMilliseconds() {
        select(WeichenChefMagnetGeneration.GEN_001);
        sent.clear();
        long start = clock;
        ThreadingUtil.runOnGUI(() -> service.testOutput(2, false));
        assertEquals(start + 500, scheduled.firstKey());
        runNext();
        assertEquals(2, sent.size());
        assertEquals(0, sent.get(1)[10]);
    }

    @Test
    void disposalStopsCoilsCancelsTrafficAndPreventsReopening() {
        select(WeichenChefMagnetGeneration.GEN_002_PLUS);
        sent.clear();
        ThreadingUtil.runOnGUI(() -> service.testOutput(1, true));
        service.dispose();
        runAll();
        assertEquals(2, sent.size());
        assertEquals(0, sent.get(1)[10]);
        assertTrue(service.isDisposed());
        assertFalse(service.isConfigurationOpen());
        assertEquals(State.RECONNECT, service.getState());
        assertThrows(IllegalStateException.class, service::openConfiguration);
        assertThrows(IllegalStateException.class, service::sendGenerationProbe);
        assertThrows(IllegalStateException.class, () -> service.writeNewModuleId(4));

        service = new WeichenChefMagnetService(device, this::send, (action, delay) ->
                scheduled.computeIfAbsent(clock + delay, k -> new ArrayList<>()).add(action));
        service.sendGenerationProbe();
        sent.clear();
        service.dispose();
        runAll();
        assertTrue(sent.isEmpty(), "queued reads must not outlive the connection");
    }

    @Test
    void resetAndModuleIdFollowReferenceAndCancelPendingReads() {
        select(WeichenChefMagnetGeneration.GEN_001);
        sent.clear();
        ThreadingUtil.runOnGUI(service::resetModule);
        runAll();
        assertEquals(1, sent.size());
        assertEquals(2, sent.get(0)[4]);
        assertEquals(255, sent.get(0)[6]);
        sent.clear();
        ThreadingUtil.runOnGUI(() -> service.writeNewModuleId(63));
        assertArrayEquals(new int[]{0, 0x46, 0x2A, 0x83, 4, 0, 24, 0, 63, 0, 0, 0, 0}, sent.get(0));
        assertThrows(IllegalArgumentException.class, () -> service.writeNewModuleId(64));
    }

    @Test
    void statusAndConfigurationCallbacksUseEdtAndCorrectSenderFilter() {
        List<Integer> statuses = new ArrayList<>();
        service.addListener(new WeichenChefMagnetService.Listener() {
            @Override
            public void valueReceived(int register, int slot, int value) {
                assertTrue(SwingUtilities.isEventDispatchThread());
            }

            @Override
            public void positionReceived(int turnout, int status) {
                assertTrue(SwingUtilities.isEventDispatchThread());
                assertEquals(1, turnout);
                statuses.add(status);
            }
        });
        ThreadingUtil.runOnGUI(service::openConfiguration);
        runAll();
        service.handleStatusFrame(status(48, 0x3801, 253));
        service.handleStatusFrame(status(3, 0x3801, 254));
        service.handleStatusFrame(status(3, 0x3801, 255));
        service.handleStatusFrame(status(9, 0x3801, 253));
        service.handleStatusFrame(status(3, 0x7777, 253));
        assertEquals(List.of(253, 254, 255), statuses);
        select(WeichenChefMagnetGeneration.GEN_001);
        service.handleStatusFrame(status(48, 0x3801, 253));
        assertEquals(3, statuses.size());
        ThreadingUtil.runOnGUI(service::closeConfiguration);
        service.handleStatusFrame(status(3, 0x3801, 253));
        assertEquals(3, statuses.size());
    }

    private MarklinReply status(int source, int address, int status) {
        return new MarklinReply(new int[]{0, 23, 3, source, 6, 0, 0,
            address >> 8, address & 0xFF, status, 0, 0, 0});
    }
}
