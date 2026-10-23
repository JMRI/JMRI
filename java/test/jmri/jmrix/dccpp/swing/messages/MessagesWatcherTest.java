package jmri.jmrix.dccpp.swing.messages;

import java.io.File;
import java.io.IOException;

import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/** Tests for MessagesWatcher. */
public class MessagesWatcherTest {

    @TempDir
    File folder;

    private DCCppInterfaceScaffold tc;
    private DCCppSystemConnectionMemo memo;

    private void send(String reply) {
        ThreadingUtil.runOnGUI(() -> tc.sendTestMessage(DCCppReply.parseDCCppReply(reply)));
    }

    @Test
    public void testIgnoresNonMessage() {
        send("@ 0 \"hello\"");
        Assertions.assertNull(MessagesFrame.findFrame(memo));
    }

    @Test
    @DisabledIfHeadless
    public void testAutoOpensWindow() {
        send("m \"hello\"");
        JUnitUtil.waitFor(() -> MessagesFrame.findFrame(memo) != null, "messages window opened");
        MessagesFrame frame = MessagesFrame.findFrame(memo);
        Assertions.assertNotNull(frame);
        Assertions.assertEquals(1, frame.getMessagesPanel().getLineCount());
        Assertions.assertTrue(frame.getMessagesPanel().getLineText(0).endsWith("hello"));
    }

    @Test
    @DisabledIfHeadless
    public void testAutoOpenOffDoesNothing() {
        ThreadingUtil.runOnGUI(() -> {
            MessagesFrame frame = MessagesFrame.showFrame(memo, null);
            frame.getMessagesPanel().setAutoOpen(false);
            frame.dispose();
        });
        send("m \"hello\"");
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        Assertions.assertNull(MessagesFrame.findFrame(memo));
    }

    @Test
    @DisabledIfHeadless
    public void testBurstOpensOneWindowNoDuplicates() {
        send("m \"1\"");
        send("m \"2\"");
        send("m \"3\"");
        JUnitUtil.waitFor(() -> {
            MessagesFrame f = MessagesFrame.findFrame(memo);
            return f != null && f.getMessagesPanel().getLineCount() >= 3;
        }, "three messages shown");
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        Assertions.assertEquals(1, JmriJFrame.getFrameList(MessagesFrame.class).size());
        MessagesPanel panel = MessagesFrame.findFrame(memo).getMessagesPanel();
        Assertions.assertEquals(3, panel.getLineCount());
        Assertions.assertTrue(panel.getLineText(0).endsWith("3"));
        Assertions.assertTrue(panel.getLineText(1).endsWith("2"));
        Assertions.assertTrue(panel.getLineText(2).endsWith("1"));
    }

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        JUnitUtil.initDefaultUserMessagePreferences();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
        Assertions.assertNotNull(new MessagesWatcher(memo));
    }

    @AfterEach
    public void tearDown() {
        for (MessagesFrame f : JmriJFrame.getFrameList(MessagesFrame.class)) {
            JUnitUtil.dispose(f);
        }
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        tc = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
