package jmri.jmrix.marklin.swing.devicetable;

import jmri.jmrix.marklin.mcan.McanUid;

import java.util.List;
import jmri.jmrix.marklin.*;
import jmri.jmrix.marklin.mcan.cs2.Cs2ConfigCommands;
import jmri.jmrix.marklin.mcan.cdb.CdBDeviceClass;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link McanDevicePanel}.
 *
 * @author Ralf Lang (ralf.lang@ralf-lang.de)
 */
class McanDevicePanelTest {

    private MarklinSystemConnectionMemo memo;
    private MarklinTrafficControlScaffold tc;

    @BeforeEach
    void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        memo.configureManagers();
    }

    @AfterEach
    void tearDown() {
        tc.dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testMagnetConfigureAndRetestEnabled() {
        McanDevicePanel panel = new McanDevicePanel();
        panel.initComponents(memo);
        McanDevice device = memo.getMcanDeviceManager().provideDevice(McanDeviceIdentity.forCdB(0x22, 3));
        device.setDeviceState(McanDeviceState.ONLINE);
        panel.getDeviceTable().setRowSelectionInterval(0, 0);
        assertTrue(panel.getConfigureButton().isEnabled());
        panel.dispose();
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testMs2ConfigureEnabledButNotGleisbox() {
        McanDevicePanel panel = new McanDevicePanel();
        panel.initComponents(memo);
        McanDevice device = memo.getMcanDeviceManager().provideDevice(
                McanDeviceIdentity.forNative(McanUid.of(0x4D5552E2), 0x0033));
        device.setDeviceState(McanDeviceState.ONLINE);
        panel.getDeviceTable().setRowSelectionInterval(0, 0);
        assertTrue(panel.getConfigureButton().isEnabled());
        assertFalse(panel.getProbeButton().isEnabled());
        jmri.util.ThreadingUtil.runOnGUI(() -> {
            panel.getConfigureButton().doClick();
            jmri.util.JmriJFrame frame = jmri.util.JmriJFrame.getFrame(
                    "Mobile Station 2 - " + device.getSystemName());
            assertNotNull(frame);
            assertInstanceOf(jmri.jmrix.marklin.swing.configure.Ms2ConfigPanel.class,
                    frame.getContentPane().getComponent(0));
            frame.dispatchEvent(new java.awt.event.WindowEvent(frame, java.awt.event.WindowEvent.WINDOW_CLOSING));
            frame.dispose();
        });
        memo.getMcanDeviceManager().deregister(device);
        memo.getMcanDeviceManager().provideDevice(McanDeviceIdentity.forNative(McanUid.of(0x47460304), 0x0010));
        panel.getDeviceTable().setRowSelectionInterval(0, 0);
        assertFalse(panel.getConfigureButton().isEnabled());
        panel.dispose();
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testPingClassCheckboxesWrapWhenWindowNarrows() throws Exception {
        McanDevicePanel panel = new McanDevicePanel();
        javax.swing.JFrame frame = new javax.swing.JFrame();
        int[] dimensions = new int[3];
        try {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                panel.initComponents(memo);
                frame.setContentPane(panel);
                frame.pack();
                frame.setVisible(true);
                java.awt.Container checkboxRow = panel.getCdbClassCheckBoxes().values()
                        .iterator().next().getParent();
                // The packed window may already wrap when the screen is narrow (e.g. CI).
                dimensions[0] = frame.getWidth();
                dimensions[1] = checkboxRow.getHeight();
                dimensions[2] = rowCount(checkboxRow);

                frame.setSize(dimensions[0] / 2, frame.getHeight());
                frame.validate();
            });
            javax.swing.SwingUtilities.invokeAndWait(() -> { });
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                frame.validate();
                java.awt.Container checkboxRow = panel.getCdbClassCheckBoxes().values()
                        .iterator().next().getParent();
                assertTrue(checkboxRow.getHeight() > dimensions[1],
                        "Checkbox area must grow to accommodate wrapped rows");
                assertTrue(rowCount(checkboxRow) > dimensions[2], "Checkboxes must wrap onto more rows");
                for (java.awt.Component checkbox : checkboxRow.getComponents()) {
                    assertTrue(checkbox.getX() >= 0);
                    assertTrue(checkbox.getX() + checkbox.getWidth() <= checkboxRow.getWidth());
                    assertTrue(checkbox.getY() + checkbox.getHeight() <= checkboxRow.getHeight(),
                            "Wrapped checkboxes must not be clipped");
                }

                frame.setSize(dimensions[0], frame.getHeight());
                frame.validate();
            });
            javax.swing.SwingUtilities.invokeAndWait(() -> { });
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                frame.validate();
                java.awt.Container checkboxRow = panel.getCdbClassCheckBoxes().values()
                        .iterator().next().getParent();
                assertEquals(dimensions[1], checkboxRow.getHeight());
                assertEquals(dimensions[2], rowCount(checkboxRow));
            });
        } finally {
            javax.swing.SwingUtilities.invokeAndWait(() -> {
                frame.dispose();
                panel.dispose();
            });
        }
    }

    private static int rowCount(java.awt.Container row) {
        return (int) java.util.Arrays.stream(row.getComponents()).mapToInt(java.awt.Component::getY)
                .distinct().count();
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testPanelInitialization() {
        McanDevicePanel panel = new McanDevicePanel();
        panel.initComponents(memo);

        assertNotNull(panel.getDiscoverMarklinButton());
        assertNotNull(panel.getDiscoverCdbButton());
        assertNotNull(panel.getDeviceTable());
        assertNotNull(panel.getDataModel());
        assertEquals("MCAN Devices (" + memo.getUserName() + ")", panel.getTitle());
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testDiscoverMarklinButtonAction() {
        McanDevicePanel panel = new McanDevicePanel();
        panel.initComponents(memo);

        int initialSent = tc.getSentMessages().size();
        panel.getDiscoverMarklinButton().doClick();

        assertEquals(initialSent + 1, tc.getSentMessages().size());
        MarklinMessage sent = tc.getLastMessageSent();
        assertNotNull(sent);
        int command = ((sent.getElement(0) & 0x0F) << 4) | ((sent.getElement(1) & 0xFE) >> 1);
        assertEquals(Cs2ConfigCommands.COMMAND_PING, command, "Must send a PING command");
        assertFalse((sent.getElement(1) & 0x01) != 0, "Must not have response flag set");
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testDiscoverCdbButtonAction() {
        McanDevicePanel panel = new McanDevicePanel();
        panel.initComponents(memo);

        int initialSent = tc.getSentMessages().size();
        panel.getDiscoverCdbButton().doClick();

        // NEW-protocol classes send 1 message each, staggered 250 ms apart via runOnGUIDelayed.
        // Count how many NEW-protocol classes are enabled (all checked by default).
        long newProtocolCount = java.util.Arrays.stream(CdBDeviceClass.values())
                .filter(c -> c.getProtocolGeneration() == CdBDeviceClass.ProtocolGeneration.NEW)
                .count();
        final int expectedMinMessages = (int) newProtocolCount;

        // Wait until at least the NEW-protocol pings have been dispatched
        jmri.util.JUnitUtil.waitFor(
                () -> tc.getSentMessages().size() >= initialSent + expectedMinMessages,
                "CdB discovery pings sent");

        assertTrue(tc.getSentMessages().size() >= initialSent + expectedMinMessages,
                "Expected at least " + expectedMinMessages + " pings, got " + (tc.getSentMessages().size() - initialSent));
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testCdbPingFormat() {
        McanDevicePanel panel = new McanDevicePanel();
        panel.initComponents(memo);

        tc.clearSentMessages();
        panel.getDiscoverCdbButton().doClick();

        // Only NEW-protocol classes send a single class-ping; OLD-protocol classes send 128
        // addressed pings and can be checked separately. Count expected NEW pings.
        CdBDeviceClass[] newClasses = java.util.Arrays.stream(CdBDeviceClass.values())
                .filter(c -> c.getProtocolGeneration() == CdBDeviceClass.ProtocolGeneration.NEW)
                .toArray(CdBDeviceClass[]::new);

        // Wait for all NEW-protocol pings to arrive (staggered 250 ms apart)
        jmri.util.JUnitUtil.waitFor(
                () -> tc.getSentMessages().size() >= newClasses.length,
                "All NEW-protocol CdB pings sent");

        // Collect only NEW-protocol pings (DLC=0) in arrival order
        List<MarklinMessage> newPings = tc.getSentMessages().stream()
                .filter(m -> m.getElement(4) == 0)
                .collect(java.util.stream.Collectors.toList());

        assertEquals(newClasses.length, newPings.size(),
                "Expected one ping per NEW-protocol class");

        // Verify each NEW-class ping has the correct CAN ID
        for (int i = 0; i < newClasses.length; i++) {
            MarklinMessage msg = newPings.get(i);
            CdBDeviceClass deviceClass = newClasses[i];
            int expectedPingId = deviceClass.getPingId();

            int actualId = ((msg.getElement(0) & 0xFF) << 24) |
                          ((msg.getElement(1) & 0xFF) << 16) |
                          ((msg.getElement(2) & 0xFF) << 8) |
                          (msg.getElement(3) & 0xFF);
            assertEquals(expectedPingId, actualId,
                String.format("Ping ID mismatch for %s (0x%02X)",
                    deviceClass.getDisplayName(), deviceClass.getClassId()));

            assertEquals(0, msg.getElement(4),
                String.format("DLC should be 0 for %s ping", deviceClass.getDisplayName()));
        }
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testTableReactsToNewDevices() {
        McanDevicePanel panel = new McanDevicePanel();
        panel.initComponents(memo);

        // Initially no devices
        assertEquals(0, panel.getDataModel().getRowCount());

        // Simulate a device being discovered on the bus
        McanDeviceIdentity.CdB identity = McanDeviceIdentity.forCdB(0x27, 0x01);
        McanDevice device = memo.getMcanDeviceManager().provideDevice(identity);
        device.setDeviceState(McanDeviceState.ONLINE);

        // Table should now show the device
        assertEquals(1, panel.getDataModel().getRowCount());
        assertEquals(device.getSystemName(), panel.getDataModel().getValueAt(0, McanDeviceTableDataModel.COLUMN_SYS_NAME));
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testTableReactsToDeviceUpdates() {
        McanDevicePanel panel = new McanDevicePanel();
        panel.initComponents(memo);

        // Add a device
        McanDeviceIdentity.CdB identity = McanDeviceIdentity.forCdB(0x22, 0x02);
        McanDevice device = memo.getMcanDeviceManager().provideDevice(identity);
        device.setDeviceState(McanDeviceState.DISCOVERED);

        assertEquals(1, panel.getDataModel().getRowCount());
        assertEquals(McanDeviceState.DISCOVERED, panel.getDataModel().getValueAt(0, McanDeviceTableDataModel.COLUMN_STATE));

        // Update device state
        device.setDeviceState(McanDeviceState.ONLINE);

        // Table should reflect the change
        assertEquals(McanDeviceState.ONLINE, panel.getDataModel().getValueAt(0, McanDeviceTableDataModel.COLUMN_STATE));
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testConfigureButtonDisabledWithNoSelection() {
        McanDevicePanel panel = new McanDevicePanel();
        panel.initComponents(memo);
        assertFalse(panel.getConfigureButton().isEnabled(),
            "Configure button must be disabled when no device is selected");
    }

    @Test
    @jmri.util.junit.annotations.DisabledIfHeadless
    void testConfigureButtonEnabledWhenConfigurableDeviceSelected() {
        McanDevicePanel panel = new McanDevicePanel();
        panel.initComponents(memo);

        // Add a WeichenChef Kombi device (class 0x27 = configurable)
        McanDeviceIdentity.CdB identity = McanDeviceIdentity.forCdB(0x27, 0x03);
        McanDevice device = memo.getMcanDeviceManager().provideDevice(identity);
        device.setDeviceState(McanDeviceState.ONLINE);

        // Select the row in the table
        panel.getDeviceTable().setRowSelectionInterval(0, 0);

        assertTrue(panel.getConfigureButton().isEnabled(),
            "Configure button must be enabled for a configurable CdB device");
    }
}
