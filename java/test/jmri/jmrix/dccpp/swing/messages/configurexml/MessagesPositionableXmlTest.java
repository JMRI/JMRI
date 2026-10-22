package jmri.jmrix.dccpp.swing.messages.configurexml;

import java.awt.Color;
import java.awt.Dimension;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.Positionable;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.swing.messages.MessagesPanel;
import jmri.jmrix.dccpp.swing.messages.MessagesPositionable;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.jdom2.Element;
import org.junit.jupiter.api.*;

/** Tests for MessagesPositionableXml. */
@DisabledIfHeadless
public class MessagesPositionableXmlTest {

    private DCCppInterfaceScaffold tc;
    private DCCppSystemConnectionMemo memo;
    private EditorScaffold editor;

    @Test
    public void testStoreLoadRoundTrip() throws jmri.JmriException {
        MessagesPositionable[] src = new MessagesPositionable[1];
        ThreadingUtil.runOnGUI(() -> {
            src[0] = new MessagesPositionable(editor);
            MessagesPanel panel = src[0].getMessagesPanel();
            panel.setMemo(memo);
            panel.setExpirySeconds(45);
            panel.setMaxLines(4);
            panel.setPlaySound(true);
            panel.setSoundFile("program:resources/sounds/Bell.wav");
            panel.setMessageForeground(Color.RED);
            panel.setMessageBackground(Color.YELLOW);
            panel.setMessagesSize(new Dimension(30, 3));
            src[0].initComponents();
            src[0].setLocation(40, 60);
        });

        Element stored = new MessagesPositionableXml().store(src[0]);
        Assertions.assertEquals("dccex_messages", stored.getName());
        Assertions.assertNotNull(stored.getChild("dccex_messages_config"));
        src[0].dispose();

        ThreadingUtil.runOnGUIWithJmriException(() -> new MessagesPositionableXml().load(stored, editor));

        MessagesPositionable loaded = null;
        for (Positionable p : editor.getContents()) {
            if (p instanceof MessagesPositionable) {
                loaded = (MessagesPositionable) p;
            }
        }
        Assertions.assertNotNull(loaded);
        Assertions.assertEquals(40, loaded.getX());
        Assertions.assertEquals(60, loaded.getY());
        MessagesPanel copy = loaded.getMessagesPanel();
        Assertions.assertSame(memo, copy.getMemo());
        Assertions.assertEquals(45, copy.getExpirySeconds());
        Assertions.assertEquals(4, copy.getMaxLines());
        Assertions.assertTrue(copy.isPlaySound());
        Assertions.assertEquals("program:resources/sounds/Bell.wav", copy.getSoundFile());
        Assertions.assertEquals(Color.RED, copy.getMessageForeground());
        Assertions.assertEquals(Color.YELLOW, copy.getMessageBackground());
        Assertions.assertEquals(new Dimension(30, 3), copy.getMessagesSize());
        loaded.dispose();
    }

    @Test
    public void testUnknownConnectionThrows() {
        Element element = new Element("dccex_messages")
                .setAttribute("x", "0")
                .setAttribute("y", "0")
                .addContent(new Element("dccex_messages_config")
                        .addContent(new Element("systemConnection").addContent("Q")));
        Assertions.assertThrows(JmriConfigureXmlException.class,
                () -> new MessagesPositionableXml().load(element, editor));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
        ThreadingUtil.runOnGUI(() -> editor = new EditorScaffold("MessagesPositionableXmlTest"));
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.dispose(editor);
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        editor = null;
        memo = null;
        tc = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
