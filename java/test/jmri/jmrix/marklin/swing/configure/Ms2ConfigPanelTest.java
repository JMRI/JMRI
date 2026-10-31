package jmri.jmrix.marklin.swing.configure;

import javax.swing.JTable;
import jmri.jmrix.marklin.MarklinReply;
import jmri.jmrix.marklin.MarklinSystemConnectionMemo;
import jmri.jmrix.marklin.MarklinTrafficControlScaffold;
import jmri.jmrix.marklin.McanDevice;
import jmri.jmrix.marklin.McanDeviceIdentity;
import jmri.jmrix.marklin.McanDeviceState;
import jmri.jmrix.marklin.Ms2LocoInfoService;
import jmri.jmrix.marklin.Ms2LocoInfoSession;
import jmri.jmrix.marklin.mcan.cs2.Cs2ConfigCommands;
import jmri.jmrix.marklin.mcan.cs2.Cs2GuiCommands;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@jmri.util.junit.annotations.DisabledIfHeadless
class Ms2ConfigPanelTest {
    private MarklinTrafficControlScaffold traffic;
    private MarklinSystemConnectionMemo memo;
    private Ms2ConfigPanel panel;
    private McanDevice device;

    @BeforeEach
    void setUp() {
        JUnitUtil.setUp();
        traffic = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(traffic);
        memo.configureManagers();
        device = discover(0x4D5552E2, 0xFF37);
        ThreadingUtil.runOnGUI(() -> panel = new Ms2ConfigPanel(device, memo));
    }

    @AfterEach
    void tearDown() {
        ThreadingUtil.runOnGUI(() -> {
            panel.dispose();
            memo.dispose();
        });
        traffic.dispose();
        JUnitUtil.tearDown();
    }

    private McanDevice discover(long uid, int hash) {
        int[] frame = Cs2ConfigCommands.buildPingReply(uid, 0x040F, 0x0033);
        frame[2] = hash >> 8;
        frame[3] = hash & 0xFF;
        memo.getMcanDeviceManager().reply(new MarklinReply(frame));
        return memo.getMcanDeviceManager().getDevice(McanDeviceIdentity.forNative(uid, 0x0033));
    }

    @Test
    void roleChangesHideJuniorFetchAndIdentifyCurrentMasterWithoutTraffic() {
        assertTrue(panel.getRoleLabel().getText().contains("Master mode"));
        assertTrue(panel.getGetLocosButton().isEnabled());
        McanDevice master = discover(0x4D533272, 0xFB21);
        ThreadingUtil.runOnGUI(() -> memo.getSeniorThrottleService().refresh());
        assertTrue(panel.getRoleLabel().getText().contains("Junior mode"));
        assertTrue(panel.getRoleLabel().getText().contains("4D533272"));
        assertFalse(panel.getGetLocosButton().isVisible());
        ThreadingUtil.runOnGUI(() -> {
            master.setDeviceState(McanDeviceState.OFFLINE);
            memo.getSeniorThrottleService().refresh();
        });
        assertTrue(panel.getGetLocosButton().isVisible());
        assertTrue(panel.getGetLocosButton().isEnabled());
        assertTrue(traffic.getSentMessages().isEmpty());
    }

    @Test
    void showsProtocolAndRelativeIdAlongsideRawCanIdAndDetails() {
        ThreadingUtil.runOnGUI(() -> panel.getGetLocosButton().doClick());
        JUnitUtil.waitFor(() -> traffic.getSentMessages().size() == 1, "list request");
        stream("[lokliste]\n .dv=1003\n .idx=15\n .name=A\n .uid=0xc04e\n .mfxuid=0xffffffff\n");
        ThreadingUtil.runOnGUI(() -> {
            JTable table = panel.getLocoTable();
            assertEquals(6, table.getColumnCount());
            assertEquals("Raw CAN ID", table.getColumnName(1));
            assertEquals("0xc04e", table.getValueAt(0, 1));
            assertEquals("", table.getValueAt(0, 2));
            assertEquals("0xffffffff", table.getValueAt(0, 3));
            assertEquals("DCC", table.getValueAt(0, 4));
            assertEquals("78", table.getValueAt(0, 5));
        });
        JUnitUtil.waitFor(() -> traffic.getSentMessages().size() == 4, "name request");
        stream("[lokomotive]\nlok\n .name=A\n .uid=0xc04e\n .adresse=0x4e\n .typ=dcc\n"
                + " .mfxuid=0x1\n .mystery=0xAB\n .fkt\n ..typ=51\n .fkt\n ..typ=52\n");
        JUnitUtil.waitFor(() -> !memo.getMs2LocoInfoService().isBusy(), "details received");
        ThreadingUtil.runOnGUI(() -> {
            JTable table = panel.getLocoTable();
            assertEquals(1, table.getRowCount());
            assertEquals(6, table.getColumnCount());
            assertEquals("A", table.getValueAt(0, 0));
            assertEquals("0xc04e", table.getValueAt(0, 1));
            assertEquals("dcc", table.getValueAt(0, 2));
            assertEquals("0x1", table.getValueAt(0, 3));
            assertEquals("DCC", table.getValueAt(0, 4));
            assertEquals("78", table.getValueAt(0, 5));
            table.setRowSelectionInterval(0, 0);
            assertTrue(panel.getRawText().getText().contains(".mystery=0xAB"));
            assertTrue(panel.getRawText().getText().contains(".idx=15"));
            assertTrue(panel.getRawText().getText().contains("..typ=52"));
            assertFalse(table.isCellEditable(0, 1));
            assertEquals(Ms2LocoInfoService.State.COMPLETE, memo.getMs2LocoInfoService().getState());
        });
    }

    private void stream(String text) {
        byte[] data = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int[] header = Cs2GuiCommands.buildConfigDataStreamHeader(data.length, Ms2LocoInfoSession.crc16(data));
        header[2] = 0xFF;
        header[3] = 0x37;
        header[4] = 7;
        memo.getMs2LocoInfoService().reply(new MarklinReply(header));
        for (int offset = 0; offset < data.length; offset += 8) {
            int[] frame = new int[13];
            frame[1] = 0x42;
            frame[2] = 0xFF;
            frame[3] = 0x37;
            frame[4] = 8;
            for (int i = 0; i < 8 && offset + i < data.length; i++) {
                frame[5 + i] = data[offset + i] & 0xFF;
            }
            memo.getMs2LocoInfoService().reply(new MarklinReply(frame));
        }
    }
}
