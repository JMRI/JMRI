package jmri.jmrix.dccpp.swing.exrail;

import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppExrailEntry;
import jmri.jmrix.dccpp.DCCppExrailEntry.State;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppMessage;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for DCCppExrailFrame.
 *
 * @author Chad Francis Copyright (C) 2026
 */
@DisabledIfHeadless
public class DCCppExrailFrameTest extends jmri.util.JmriJFrameTestBase {

    private DCCppInterfaceScaffold tc;
    private DCCppSystemConnectionMemo memo;

    @Test
    public void testTitleIncludesPrefix() {
        Assertions.assertTrue(frame.getTitle().contains("D"), "title should contain system prefix");
    }

    @Test
    public void testPopulatesOnIdListReply() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        Assertions.assertEquals(0, exrailFrame.getEntryCount(), "should start empty");

        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 2"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 2 A \"Yard Switcher\""));

        Assertions.assertEquals(2, exrailFrame.getEntryCount(), "should have two entries after replies");
    }

    @Test
    public void testCaptionUpdateApplied() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Original\""));
        exrailFrame.message(DCCppReply.parseDCCppReply("jB 1 \"Updated Caption\""));

        Assertions.assertEquals("Updated Caption", exrailFrame.getEntry(1).getDisplayName());
    }

    @Test
    public void testStateUpdateApplied() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Loop\""));
        exrailFrame.message(DCCppReply.parseDCCppReply("jB 1 2"));

        Assertions.assertEquals(State.HIDDEN, exrailFrame.getEntry(1).getState());
    }

    @Test
    public void testTriggerEnabledForActiveEntry() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        Assertions.assertTrue(exrailFrame.isRowTriggerEnabled(0),
                "row trigger should be enabled for a non-DISABLED entry regardless of power state");
    }

    @Test
    public void testTriggerEnabledWhenPowerOff() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        tc.sendTestMessage(DCCppReply.parseDCCppReply("p 0"));
        Assertions.assertTrue(exrailFrame.isRowTriggerEnabled(0),
                "row trigger should be enabled regardless of track power state");
    }

    @Test
    public void testInitSendsAutomationIDsRequest() {
        Assertions.assertFalse(tc.outbound.isEmpty(), "should have sent <JA> on initComponents");
        Assertions.assertTrue(tc.outbound.get(0).isAutomationIDsMessage(), "first outbound should be <JA>");
    }

    @Test
    public void testListenerRegisteredOnInit() {
        Assertions.assertTrue(tc.numListeners() > 0, "frame should register as listener on initComponents");
    }

    @Test
    public void testHiddenEntriesNotCounted() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 2"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Visible Route\""));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 2 A \"Hidden Auto\""));
        exrailFrame.message(DCCppReply.parseDCCppReply("jB 2 2")); // state=2 → hidden
        Assertions.assertEquals(1, exrailFrame.getEntryCount(), "hidden entry should be excluded from count");
    }

    @Test
    public void testTriggerRoute() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 2"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        DCCppExrailEntry entry = exrailFrame.getEntry(1);
        Assertions.assertNotNull(entry);
        exrailFrame.triggerEntry(entry, 0);
        Assertions.assertEquals(DCCppMessage.makeStartExrailMsg(1).toString(),
                tc.outbound.lastElement().toString(), "should send route start command");
    }

    @Test
    public void testTriggerAutomation() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 2 3"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 2 A \"Yard Switcher\""));
        DCCppExrailEntry entry = exrailFrame.getEntry(2);
        Assertions.assertNotNull(entry);
        exrailFrame.triggerEntry(entry, 1234);
        Assertions.assertEquals(DCCppMessage.makeStartExrailMsg(2, 1234).toString(),
                tc.outbound.lastElement().toString(), "should send automation start command with loco address");
    }

    @Test
    public void testReconnectClearsEntriesAndRefetches() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 2"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 2 A \"Yard Switcher\""));
        Assertions.assertEquals(2, exrailFrame.getEntryCount(), "should have entries before reconnect");

        ConnectionStatus.instance().setConnectionState(memo, ConnectionStatus.CONNECTION_UP);

        Assertions.assertEquals(0, exrailFrame.getEntryCount(), "entries should be cleared on reconnect");
        Assertions.assertTrue(tc.outbound.lastElement().isAutomationIDsMessage(),
                "should re-send <JA> on reconnect");
    }

    @Test
    public void testReconnectNotTriggeredOnDisconnect() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 2"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 2 A \"Yard Switcher\""));
        int outboundBefore = tc.outbound.size();

        ConnectionStatus.instance().setConnectionState(memo, ConnectionStatus.CONNECTION_DOWN);

        Assertions.assertEquals(2, exrailFrame.getEntryCount(), "entries should be preserved on disconnect");
        Assertions.assertEquals(outboundBefore, tc.outbound.size(), "should not re-send <JA> on disconnect");
    }

    @Test
    public void testRowClickFiresRoute() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        int outboundBefore = tc.outbound.size();

        exrailFrame.triggerRowForTest(0);

        // setValueAt wraps the trigger call in SwingUtilities.invokeLater, so wait for it.
        String expected = DCCppMessage.makeStartExrailMsg(1).toString();
        JUnitUtil.waitFor(() ->
                tc.outbound.size() > outboundBefore
                && tc.outbound.lastElement().toString().equals(expected),
                "row click should enqueue the route start command");
    }

    @Test
    public void testDisabledEntryButtonNotEditable() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        exrailFrame.message(DCCppReply.parseDCCppReply("jB 1 4")); // state=4 -> DISABLED
        Assertions.assertFalse(exrailFrame.isRowTriggerEnabled(0),
                "row trigger should be disabled for entries in DISABLED state");
    }

    @Test
    public void testButtonLabelDefaultsToSet() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        Assertions.assertEquals("Set", exrailFrame.getRowButtonLabel(0),
                "button label should default to 'Set' when no caption");
    }

    @Test
    public void testCaptionUsedAsButtonLabel() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        exrailFrame.message(DCCppReply.parseDCCppReply("jB 1 \"Go!\""));
        Assertions.assertEquals("Go!", exrailFrame.getRowButtonLabel(0),
                "caption should be used as button label");
    }

    @Test
    public void testNameColumnShowsDescriptionWhenCaptionSet() {
        DCCppExrailFrame exrailFrame = (DCCppExrailFrame) frame;
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1"));
        exrailFrame.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        exrailFrame.message(DCCppReply.parseDCCppReply("jB 1 \"Go!\""));
        Assertions.assertEquals("Station Loop", exrailFrame.getRowName(0),
                "name column should always show description, not caption");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
        frame = new DCCppExrailFrame(memo);
        frame.initComponents();
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        tc = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }
}
