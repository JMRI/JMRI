package jmri.jmrix.marklin;

import jmri.jmrix.marklin.mcan.McanUid;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import jmri.jmrix.marklin.mcan.cs2.Cs2ConfigCommands;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SeniorThrottleServiceTest {
    private MarklinTrafficControlScaffold traffic;
    private MarklinSystemConnectionMemo memo;
    private SeniorThrottleService service;
    private TestClock clock;

    @BeforeEach
    void setUp() {
        JUnitUtil.setUp();
        traffic = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(traffic);
        memo.configureManagers();
        clock = new TestClock();
        service = new SeniorThrottleService(memo.getMcanDeviceManager(), clock);
    }

    @AfterEach
    void tearDown() {
        service.dispose();
        memo.dispose();
        traffic.dispose();
        JUnitUtil.tearDown();
    }

    private McanDevice discover(long uid, int type) {
        memo.getMcanDeviceManager().reply(new MarklinReply(Cs2ConfigCommands.buildPingReply(McanUid.of(uid), 0x040F, type)));
        return memo.getMcanDeviceManager().getDevice(McanDeviceIdentity.forNative(McanUid.of(uid), type));
    }

    @Test
    void choosesSerialNotArrivalHardwareOrSignedIntegerAndNeverTransmits() {
        McanDevice later = discover(0xF0000000L, 0x0032);
        McanDevice earlier = discover(0x4D533272L, 0x0033);
        discover(0x00000010L, 0x0010);
        clock.now = Instant.now().plusSeconds(1);
        assertSame(earlier, service.getMaster().orElseThrow());
        assertEquals(SeniorThrottleService.Role.JUNIOR, service.getRole(later));
        assertEquals(SeniorThrottleService.Role.MASTER, service.getRole(earlier));
        assertTrue(traffic.getSentMessages().isEmpty());
    }

    @Test
    void expiryOfflineAndRemovalExcludePreviouslySeniorDevices() {
        McanDevice older = discover(0x10000001, 0x0032);
        clock.now = Instant.now().plusSeconds(61);
        assertTrue(service.getMaster().isEmpty());
        assertEquals(SeniorThrottleService.Role.UNKNOWN, service.getRole(older));
        McanDevice junior = discover(0x20000001, 0x0033);
        clock.now = junior.getLastSeen().plusSeconds(1);
        older.setDeviceState(McanDeviceState.OFFLINE);
        assertSame(junior, service.getMaster().orElseThrow());
        memo.getMcanDeviceManager().deregister(junior);
        assertEquals(SeniorThrottleService.Role.UNKNOWN, service.getRole(junior));
        assertTrue(service.getMaster().isEmpty());
    }

    @Test
    void knownMasterPingRequestRefreshesPresenceWithoutCreatingDevices() {
        int[] ping = Cs2ConfigCommands.buildPingReply(McanUid.of(0x4D5552E2), 0x040F, 0x0033);
        ping[2] = 0xFF;
        ping[3] = 0x37;
        memo.getMcanDeviceManager().reply(new MarklinReply(ping));
        McanDevice device = memo.getMcanDeviceManager().getDevice(McanDeviceIdentity.forNative(McanUid.of(0x4D5552E2), 0x0033));
        Instant before = device.getLastSeen();
        memo.getMcanDeviceManager().reply(new MarklinReply(Cs2ConfigCommands.buildPing(0xFF37)));
        assertFalse(device.getLastSeen().isBefore(before));
        assertEquals(0xFF37, NativeDeviceService.get(device).orElseThrow().getObservedHash().orElseThrow());
        memo.getMcanDeviceManager().reply(new MarklinReply(Cs2ConfigCommands.buildPing(0x4712)));
        assertEquals(1, memo.getMcanDeviceManager().getNamedBeanSet().size());
        assertTrue(traffic.getSentMessages().isEmpty());
    }

    @Test
    void nativeDiscoveryReusesServiceAndNotifiesViewsOnHashChange() throws Exception {
        McanDevice device = discover(0x4D5552E2, 0x0033);
        NativeDeviceService service = NativeDeviceService.get(device).orElseThrow();
        java.util.concurrent.atomic.AtomicInteger notifications = new java.util.concurrent.atomic.AtomicInteger();
        device.addPropertyChangeListener(McanDevice.PROPERTY_DEVICE_SERVICE,
                event -> notifications.incrementAndGet());
        discover(0x4D5552E2, 0x0033);
        assertSame(service, device.getDeviceService());
        assertEquals(0, notifications.get());
        javax.swing.SwingUtilities.invokeAndWait(() -> service.setObservedHash(0xFF37));
        assertEquals(1, notifications.get());

        NativeDeviceService specialized = new NativeDeviceService(device) { };
        device.setDeviceService(specialized);
        assertSame(specialized, NativeDeviceService.get(device).orElseThrow());
        discover(0x4D5552E2, 0x0033);
        assertSame(specialized, device.getDeviceService(), "Rediscovery must preserve native subclasses");
        int before = notifications.get();
        javax.swing.SwingUtilities.invokeAndWait(() -> service.setObservedHash(0xFB21));
        assertEquals(before, notifications.get(), "Detached service must not notify the device");
    }

    @Test
    void predictedAndAmbiguousHashesDoNotRefreshPresence() {
        McanDeviceManager manager = memo.getMcanDeviceManager();
        McanDevice first = manager.provideDevice(McanDeviceIdentity.forNative(McanUid.of(0x4D5552E2), 0x0033));
        NativeDeviceService firstState = NativeDeviceService.get(first).orElseThrow();
        Instant before = first.getLastSeen();
        manager.reply(new MarklinReply(Cs2ConfigCommands.buildPing(firstState.getDefaultHash())));
        assertEquals(before, first.getLastSeen());
        assertTrue(firstState.getObservedHash().isEmpty());

        firstState.setObservedHash(0xFF37);
        McanDevice second = manager.provideDevice(McanDeviceIdentity.forNative(McanUid.of(0x4D533272), 0x0032));
        NativeDeviceService.get(second).orElseThrow().setObservedHash(0xFF37);
        Instant secondBefore = second.getLastSeen();
        manager.reply(new MarklinReply(Cs2ConfigCommands.buildPing(0xFF37)));
        assertEquals(before, first.getLastSeen());
        assertEquals(secondBefore, second.getLastSeen());

        int[] reply = Cs2ConfigCommands.buildPingReply(McanUid.of(0x4D5552E2), 0x040F, 0x0033);
        reply[2] = 0xFB;
        reply[3] = 0x21;
        manager.reply(new MarklinReply(reply));
        assertEquals(0xFB21, firstState.getObservedHash().orElseThrow());
        Instant refreshed = first.getLastSeen();
        manager.reply(new MarklinReply(Cs2ConfigCommands.buildPing(0xFF37)));
        assertEquals(refreshed, first.getLastSeen(), "Old hash must no longer identify first device");
        assertEquals(McanDeviceState.ONLINE, second.getDeviceState());
        assertTrue(traffic.getSentMessages().isEmpty());
    }

    @Test
    void timestampRefreshedAfterClockSnapshotDoesNotTemporarilyDemoteMaster() {
        McanDevice device = discover(0x4D5552E2, 0x0033);
        clock.now = device.getLastSeen().minusMillis(1);
        assertEquals(SeniorThrottleService.Role.MASTER, service.getRole(device));
    }

    private static final class TestClock extends Clock {
        private Instant now = Instant.now();
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }
}
