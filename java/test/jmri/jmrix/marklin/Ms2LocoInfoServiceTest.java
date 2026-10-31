package jmri.jmrix.marklin;

import jmri.jmrix.marklin.mcan.McanUid;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.SwingUtilities;
import jmri.jmrix.marklin.mcan.cs2.Cs2ConfigCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2GuiCommands;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class Ms2LocoInfoServiceTest {
    private MarklinTrafficControlScaffold traffic;
    private MarklinSystemConnectionMemo memo;
    private Ms2LocoInfoService service;
    private McanDevice device;

    @BeforeEach
    void setUp() {
        JUnitUtil.setUp();
        traffic = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(traffic);
        memo.configureManagers();
        device = discover(0x4D5552E2, 0xFF37);
        service = memo.getMs2LocoInfoService();
    }

    @AfterEach
    void tearDown() {
        ThreadingUtil.runOnGUI(() -> {
            if (service.isBusy()) {
                service.cancel("Test cleanup");
                JUnitAppender.assertWarnMessage("MS2 " + device.getSystemName() + ": Test cleanup");
            }
            memo.dispose();
        });
        traffic.dispose();
        JUnitUtil.tearDown();
    }

    private McanDevice discover(long uid, int hash) {
        int[] frame = Cs2ConfigCommands.buildPingReply(McanUid.of(uid), 0x040F, 0x0033);
        frame[2] = hash >> 8;
        frame[3] = hash & 0xFF;
        memo.getMcanDeviceManager().reply(new MarklinReply(frame));
        return memo.getMcanDeviceManager().getDevice(McanDeviceIdentity.forNative(McanUid.of(uid), 0x0033));
    }

    private void awaitSent(int count) {
        JUnitUtil.waitFor(() -> traffic.getSentMessages().size() >= count, "paced MS2 request frames");
    }

    private void stream(String text) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        int[] header = Cs2GuiCommands.buildConfigDataStreamHeader(data.length, Ms2LocoInfoSession.crc16(data));
        header[2] = 0xFF;
        header[3] = 0x37;
        header[4] = 7;
        service.reply(new MarklinReply(header));
        for (int offset = 0; offset < data.length; offset += 8) {
            int[] frame = new int[13];
            frame[1] = 0x42;
            frame[2] = 0xFF;
            frame[3] = 0x37;
            frame[4] = 8;
            for (int i = 0; i < 8 && offset + i < data.length; i++) {
                frame[5 + i] = data[offset + i] & 0xFF;
            }
            service.reply(new MarklinReply(frame));
        }
    }

    @Test
    void connectionOwnedReaderFetchesListAndUnknownRepeatedFieldsOnEdt() {
        AtomicBoolean callbacksOnEdt = new AtomicBoolean(true);
        service.addPropertyChangeListener(e -> callbacksOnEdt.compareAndSet(true, SwingUtilities.isEventDispatchThread()));
        ThreadingUtil.runOnGUI(() -> service.requestLocos(device));
        awaitSent(1);
        assertEquals(0x40, traffic.getLastMessageSent().getElement(1));
        stream("[lokliste]\n .dv=1003\n .idx=15\n .llindex=15\n .name=A\n .crc=123\n .uid=0x18\n");
        JUnitUtil.waitFor(() -> service.getLocos(device).size() == 1, "list entry visible before details");
        assertFalse(service.getLocos(device).get(0).hasInfo());
        awaitSent(4);
        stream("[lokomotive]\nlok\n .name=A\n .uid=0x18\n .newThing=0xFEDC\n"
                + " .fkt\n ..typ=51\n ..wert=0\n .fkt\n ..typ=52\n ..wert=1\n");
        JUnitUtil.waitFor(() -> !service.isBusy(), "roster complete");
        assertEquals(Ms2LocoInfoService.State.COMPLETE, service.getState());
        Ms2LocoRecord record = service.getLocos(device).get(0);
        assertTrue(record.hasInfo());
        assertEquals("0xFEDC", record.getFields().get("info:.newThing[1]"));
        assertEquals("51", record.getFields().get("info:..typ[1]"));
        assertEquals("52", record.getFields().get("info:..typ[2]"));
        assertEquals("15", record.getFields().get("list:.llindex[1]"));
        assertTrue(callbacksOnEdt.get());
        assertSame(service, memo.getMs2LocoInfoService());
        assertEquals(List.of(), service.getLocos(new McanDevice("M", McanDeviceIdentity.forNative(McanUid.of(0x1234), 0x0033))));
    }

    @Test
    void roleChangeCancelsOutstandingReaderAndRejectsJuniorRequest() {
        ThreadingUtil.runOnGUI(() -> service.requestLocos(device));
        awaitSent(1);
        discover(0x4D533272, 0xFB21);
        JUnitUtil.waitFor(() -> !service.isBusy(), "cancel on master change");
        assertEquals(Ms2LocoInfoService.State.CANCELLED, service.getState());
        JUnitAppender.assertWarnMessage("MS2 " + device.getSystemName()
                + ": MS2 master is no longer current or its sender hash changed");
        ThreadingUtil.runOnGUI(() -> assertThrows(IllegalArgumentException.class, () -> service.requestLocos(device)));
    }

    @Test
    void disposesListenerAndStopsRequests() {
        int before = traffic.getNumberListeners();
        assertTrue(before > 0);
        ThreadingUtil.runOnGUI(() -> {
            service.requestLocos(device);
            service.cancel("Closed");
            service.dispose();
        });
        JUnitAppender.assertWarnMessage("MS2 " + device.getSystemName() + ": Closed");
        assertEquals(before - 1, traffic.getNumberListeners());
        ThreadingUtil.runOnGUI(() -> assertThrows(IllegalStateException.class, () -> service.requestLocos(device)));
    }

    @Test
    void connectionDisposalAlsoReleasesDeviceDiscoveryAndPassiveRoleTimer() {
        McanDeviceManager manager = memo.getMcanDeviceManager();
        SeniorThrottleService senior = memo.getSeniorThrottleService();
        int listeners = traffic.getNumberListeners();
        ThreadingUtil.runOnGUI(memo::dispose);
        assertTrue(manager.getNamedBeanSet().isEmpty());
        assertTrue(traffic.getNumberListeners() < listeners);
        assertTrue(senior.getMaster().isEmpty());
        ThreadingUtil.runOnGUI(() -> assertThrows(IllegalStateException.class, () -> service.requestLocos(device)));
    }

    @Test
    void listParserPreservesIndexMetadataAndAllUnknownData() {
        Map<String, Ms2LocoRecord> records = Ms2LocoInfoService.parseList(
                "[lokliste]\n .dv=1003\n .idx=15\n .llindex=15\n .name=A\n .crc=123\n .uid=0x18\n"
                + " .idx=16\n .llindex=16\n .name=B\n .unknown=x=y\n .uid=0xc04e\n");
        assertEquals(2, records.size());
        assertEquals("15", records.get("A").getFields().get("list:.idx[1]"));
        assertEquals("16", records.get("B").getFields().get("list:.idx[1]"));
        assertEquals("x=y", records.get("B").getFields().get("list:.unknown[1]"));
        assertTrue(records.get("B").getListText().contains(".uid=0xc04e"));
    }
}
