package jmri.jmrix.dccpp.swing.messages;

import java.io.File;
import java.io.IOException;

import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

/** Tests for MessagesFrame. */
@DisabledIfHeadless
public class MessagesFrameTest {

    @TempDir
    File folder;

    private DCCppInterfaceScaffold tc;
    private DCCppSystemConnectionMemo memo;

    private MessagesFrame show(String initialMessage) {
        MessagesFrame[] f = new MessagesFrame[1];
        ThreadingUtil.runOnGUI(() -> f[0] = MessagesFrame.showFrame(memo, initialMessage));
        return f[0];
    }

    @Test
    public void testTitleIncludesPrefix() {
        MessagesFrame f = show(null);
        Assertions.assertEquals(Bundle.getMessage("MessagesFrameTitle") + " (D)", f.getTitle());
    }

    @Test
    public void testShowFrameReusesWindow() {
        MessagesFrame first = show(null);
        MessagesFrame second = show(null);
        Assertions.assertSame(first, second);
        Assertions.assertEquals(1, JmriJFrame.getFrameList(MessagesFrame.class).size());
    }

    @Test
    public void testFindFrame() {
        Assertions.assertNull(MessagesFrame.findFrame(memo));
        MessagesFrame f = show(null);
        Assertions.assertSame(f, MessagesFrame.findFrame(memo));
    }

    @Test
    public void testInitialMessageOnlyOnCreate() {
        MessagesFrame f = show("x");
        show("y");
        Assertions.assertEquals(1, f.getMessagesPanel().getLineCount());
        Assertions.assertTrue(f.getMessagesPanel().getLineText(0).endsWith("x"));
    }

    @Test
    public void testReceivesMessagesWhileOpen() {
        MessagesFrame f = show(null);
        ThreadingUtil.runOnGUI(() -> tc.sendTestMessage(jmri.jmrix.dccpp.DCCppReply.parseDCCppReply("m \"hello\"")));
        Assertions.assertTrue(f.getMessagesPanel().getLineText(0).endsWith("hello"));
    }

    @Test
    public void testSettingsPersistAcrossReopen() {
        MessagesFrame f = show(null);
        ThreadingUtil.runOnGUI(() -> {
            f.getMessagesPanel().setExpirySeconds(45);
            f.getMessagesPanel().setAutoOpen(false);
            f.dispose();
        });
        Assertions.assertNull(MessagesFrame.findFrame(memo));
        MessagesFrame reopened = show(null);
        Assertions.assertNotSame(f, reopened);
        Assertions.assertEquals(45, reopened.getMessagesPanel().getExpirySeconds());
        Assertions.assertFalse(MessagesFrame.isAutoOpen(memo));
    }

    @Test
    public void testIsAutoOpenDefaultsTrue() {
        Assertions.assertTrue(MessagesFrame.isAutoOpen(memo));
    }

    @BeforeEach
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager(new jmri.profile.NullProfile(folder));
        JUnitUtil.initDefaultUserMessagePreferences();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
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
