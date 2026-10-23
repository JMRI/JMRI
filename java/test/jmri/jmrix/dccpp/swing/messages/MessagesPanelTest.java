package jmri.jmrix.dccpp.swing.messages;

import java.awt.Dimension;

import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppReply;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/** Tests for MessagesPanel; runs headless since the panel has no frame. */
public class MessagesPanelTest {

    private DCCppInterfaceScaffold tc;
    private DCCppSystemConnectionMemo memo;
    private MessagesPanel panel;
    private long[] now;
    private int listenersBefore;

    private void send(String reply) {
        ThreadingUtil.runOnGUI(() -> tc.sendTestMessage(DCCppReply.parseDCCppReply(reply)));
    }

    @Test
    public void testEmptyShowsNoMessages() {
        Assertions.assertEquals(1, panel.getLineCount());
        Assertions.assertEquals(Bundle.getMessage("MessagesNoMessages"), panel.getLineText(0).trim());
    }

    @Test
    public void testReplyAddsTimestampedLine() {
        send("m \"Short on main\"");
        Assertions.assertEquals(1, panel.getLineCount());
        Assertions.assertTrue(panel.getLineText(0).matches("^\\d\\d:\\d\\d:\\d\\d  Short on main$"),
                "line was: " + panel.getLineText(0));
    }

    @Test
    public void testNewestFirst() {
        send("m \"first\"");
        send("m \"second\"");
        Assertions.assertEquals(2, panel.getLineCount());
        Assertions.assertTrue(panel.getLineText(0).endsWith("second"));
        Assertions.assertTrue(panel.getLineText(1).endsWith("first"));
    }

    @Test
    public void testIgnoresOtherReplies() {
        send("@ 0 1 \"lcd text\"");
        Assertions.assertEquals(1, panel.getLineCount());
        Assertions.assertEquals(Bundle.getMessage("MessagesNoMessages"), panel.getLineText(0).trim());
    }

    @Test
    public void testExpiryRemovesLine() {
        send("m \"going away\"");
        now[0] = 30_000;
        ThreadingUtil.runOnGUI(() -> panel.tick());
        Assertions.assertEquals(Bundle.getMessage("MessagesNoMessages"), panel.getLineText(0).trim());
    }

    @Test
    public void testTimerRunsOnlyWhileMessagesShow() {
        Assertions.assertFalse(panel.isExpiryTimerRunning());
        send("m \"x\"");
        Assertions.assertTrue(panel.isExpiryTimerRunning());
        now[0] = 30_000;
        ThreadingUtil.runOnGUI(() -> panel.tick());
        Assertions.assertFalse(panel.isExpiryTimerRunning());
    }

    @Test
    public void testFixedSizeTruncates() {
        ThreadingUtil.runOnGUI(() -> panel.setMessagesSize(new Dimension(12, 2)));
        send("m \"This message is far too long to fit in twelve\"");
        Assertions.assertEquals(2, panel.getLineCount());
        Assertions.assertEquals(12, panel.getLineText(0).length());
        Dimension before = panel.getPreferredSize();
        send("m \"x\"");
        Assertions.assertEquals(before, panel.getPreferredSize());
    }

    @Test
    public void testMissingSoundFileWarnsOnce() {
        panel.setPlaySound(true);
        panel.setSoundFile("program:resources/sounds/does-not-exist.wav");
        send("m \"one\"");
        send("m \"two\"");
        JUnitAppender.assertWarnMessageStartsWith("Unable to play DCC-EX message sound");
        Assertions.assertEquals(2, panel.getLineCount());
    }

    @Test
    public void testSettingsDefaults() {
        Assertions.assertEquals(30, panel.getExpirySeconds());
        Assertions.assertEquals(10, panel.getMaxLines());
        Assertions.assertTrue(panel.isAutoOpen());
        Assertions.assertFalse(panel.isPlaySound());
        Assertions.assertEquals(MessagesPanel.DEFAULT_SOUND_FILE, panel.getSoundFile());
        Assertions.assertEquals(java.awt.Color.WHITE, panel.getMessageForeground());
        Assertions.assertEquals(java.awt.Color.BLACK, panel.getMessageBackground());
        Assertions.assertNull(panel.getMessagesSize());
    }

    @Test
    public void testMaxLinesApplies() {
        ThreadingUtil.runOnGUI(() -> panel.setMaxLines(2));
        send("m \"1\"");
        send("m \"2\"");
        send("m \"3\"");
        Assertions.assertEquals(2, panel.getLineCount());
    }

    @Test
    public void testDisposeStopsTimerAndListener() {
        send("m \"x\"");
        ThreadingUtil.runOnGUI(() -> panel.dispose());
        Assertions.assertFalse(panel.isExpiryTimerRunning());
        Assertions.assertEquals(listenersBefore, tc.numListeners());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
        listenersBefore = tc.numListeners();
        now = new long[]{0};
        ThreadingUtil.runOnGUI(() -> {
            panel = new MessagesPanel(null, null, false, () -> now[0]);
            panel.setMemo(memo);
            panel.initComponents();
        });
    }

    @AfterEach
    public void tearDown() {
        ThreadingUtil.runOnGUI(() -> panel.dispose());
        panel = null;
        now = null;
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        tc = null;
        JUnitUtil.tearDown();
    }

}
