package jmri.jmrix.dccpp.swing.messages;

import java.awt.Color;
import java.awt.Dimension;

import jmri.jmrit.display.EditorScaffold;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/** Tests for MessagesPositionable. */
@DisabledIfHeadless
public class MessagesPositionableTest {

    private DCCppInterfaceScaffold tc;
    private DCCppSystemConnectionMemo memo;
    private EditorScaffold editor;
    private MessagesPositionable pos;
    private MessagesPositionable clone;

    @Test
    public void testTypeString() {
        Assertions.assertEquals(Bundle.getMessage("PositionableType_Messages"), pos.getTypeString());
    }

    @Test
    public void testLinesAreLaidOutAfterNewMessage() {
        MessagesPanel panel = pos.getMessagesPanel();
        ThreadingUtil.runOnGUI(() -> panel.addMessage("hello"));
        java.awt.Component line = panel.getComponent(0);
        Assertions.assertTrue(line.getWidth() > 0 && line.getHeight() > 0, "line has no size: " + line.getSize());
    }

    @Test
    public void testRemoveStopsListening() {
        int listenersBefore = tc.numListeners();
        ThreadingUtil.runOnGUI(() -> pos.getMessagesPanel().setMemo(memo));
        Assertions.assertEquals(listenersBefore + 1, tc.numListeners());
        ThreadingUtil.runOnGUI(() -> pos.remove());
        Assertions.assertEquals(listenersBefore, tc.numListeners());
    }

    @Test
    public void testDeepCloneCopiesAllSettings() {
        MessagesPanel panel = pos.getMessagesPanel();
        ThreadingUtil.runOnGUI(() -> {
            panel.setMemo(memo);
            panel.setExpirySeconds(45);
            panel.setMaxLines(4);
            panel.setAutoOpen(false);
            panel.setPlaySound(true);
            panel.setSoundFile("program:resources/sounds/Bell.wav");
            panel.setMessageForeground(Color.RED);
            panel.setMessageBackground(Color.YELLOW);
            panel.setMessagesSize(new Dimension(30, 3));
            clone = (MessagesPositionable) pos.deepClone();
        });
        MessagesPanel copy = clone.getMessagesPanel();
        Assertions.assertNotSame(panel, copy);
        Assertions.assertSame(memo, copy.getMemo());
        Assertions.assertEquals(45, copy.getExpirySeconds());
        Assertions.assertEquals(4, copy.getMaxLines());
        Assertions.assertFalse(copy.isAutoOpen());
        Assertions.assertTrue(copy.isPlaySound());
        Assertions.assertEquals("program:resources/sounds/Bell.wav", copy.getSoundFile());
        Assertions.assertEquals(Color.RED, copy.getMessageForeground());
        Assertions.assertEquals(Color.YELLOW, copy.getMessageBackground());
        Assertions.assertEquals(new Dimension(30, 3), copy.getMessagesSize());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
        ThreadingUtil.runOnGUI(() -> {
            editor = new EditorScaffold("MessagesPositionableTest");
            pos = new MessagesPositionable(editor);
            pos.initComponents();
        });
    }

    @AfterEach
    public void tearDown() {
        if (clone != null) {
            clone.dispose();
        }
        pos.dispose();
        JUnitUtil.dispose(editor);
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        clone = null;
        pos = null;
        editor = null;
        memo = null;
        tc = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
