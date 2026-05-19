package jmri.jmrix.dccpp.swing.exrail;

import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppExrailEntry;
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
        DCCppExrailFrame f = (DCCppExrailFrame) frame;
        Assertions.assertEquals(0, f.getEntryCount(), "should start empty");

        f.message(DCCppReply.parseDCCppReply("jA 1 2"));
        f.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        f.message(DCCppReply.parseDCCppReply("jA 2 A \"Yard Switcher\""));

        Assertions.assertEquals(2, f.getEntryCount(), "should have two entries after replies");
    }

    @Test
    public void testCaptionUpdateApplied() {
        DCCppExrailFrame f = (DCCppExrailFrame) frame;
        f.message(DCCppReply.parseDCCppReply("jA 1"));
        f.message(DCCppReply.parseDCCppReply("jA 1 R \"Original\""));
        f.message(DCCppReply.parseDCCppReply("jB 1 \"Updated Caption\""));

        Assertions.assertEquals("Updated Caption", f.getEntry(1).getDisplayName());
    }

    @Test
    public void testStateUpdateApplied() {
        DCCppExrailFrame f = (DCCppExrailFrame) frame;
        f.message(DCCppReply.parseDCCppReply("jA 1"));
        f.message(DCCppReply.parseDCCppReply("jA 1 R \"Loop\""));
        f.message(DCCppReply.parseDCCppReply("jB 1 2"));

        Assertions.assertEquals(2, f.getEntry(1).getState());
    }

    @Test
    public void testTriggerDisabledAtStartup() {
        Assertions.assertFalse(((DCCppExrailFrame) frame).isTriggerEnabled(), "trigger should be disabled when power is unknown");
    }

    @Test
    public void testTriggerEnabledWhenPowerOn() {
        tc.sendTestMessage(DCCppReply.parseDCCppReply("p 1"));
        Assertions.assertTrue(((DCCppExrailFrame) frame).isTriggerEnabled(), "trigger should be enabled when power is on");
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
        DCCppExrailFrame f = (DCCppExrailFrame) frame;
        f.message(DCCppReply.parseDCCppReply("jA 1 2"));
        f.message(DCCppReply.parseDCCppReply("jA 1 R \"Visible Route\""));
        f.message(DCCppReply.parseDCCppReply("jA 2 A \"Hidden Auto\""));
        f.message(DCCppReply.parseDCCppReply("jB 2 2")); // state=2 → hidden
        Assertions.assertEquals(1, f.getEntryCount(), "hidden entry should be excluded from count");
    }

    @Test
    public void testTriggerRoute() {
        DCCppExrailFrame f = (DCCppExrailFrame) frame;
        f.message(DCCppReply.parseDCCppReply("jA 1 2"));
        f.message(DCCppReply.parseDCCppReply("jA 1 R \"Station Loop\""));
        DCCppExrailEntry entry = f.getEntry(1);
        Assertions.assertNotNull(entry);
        f.triggerEntry(entry, 0);
        Assertions.assertEquals(DCCppMessage.makeStartExrailMsg(1).toString(),
                tc.outbound.lastElement().toString(), "should send route start command");
    }

    @Test
    public void testTriggerAutomation() {
        DCCppExrailFrame f = (DCCppExrailFrame) frame;
        f.message(DCCppReply.parseDCCppReply("jA 2 3"));
        f.message(DCCppReply.parseDCCppReply("jA 2 A \"Yard Switcher\""));
        DCCppExrailEntry entry = f.getEntry(2);
        Assertions.assertNotNull(entry);
        f.triggerEntry(entry, 1234);
        Assertions.assertEquals(DCCppMessage.makeStartExrailMsg(2, 1234).toString(),
                tc.outbound.lastElement().toString(), "should send automation start command with loco address");
    }

    @Test
    public void testTriggerDisabledWhenPowerOff() {
        tc.sendTestMessage(DCCppReply.parseDCCppReply("p 1"));
        tc.sendTestMessage(DCCppReply.parseDCCppReply("p 0"));
        Assertions.assertFalse(((DCCppExrailFrame) frame).isTriggerEnabled(), "trigger should be disabled when power is off");
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
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }
}
