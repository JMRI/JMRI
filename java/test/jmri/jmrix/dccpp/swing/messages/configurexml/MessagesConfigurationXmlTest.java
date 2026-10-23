package jmri.jmrix.dccpp.swing.messages.configurexml;

import java.awt.Color;
import java.awt.Dimension;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.dccpp.DCCppCommandStation;
import jmri.jmrix.dccpp.DCCppInterfaceScaffold;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.swing.messages.MessagesPanel;
import jmri.util.JUnitUtil;

import org.jdom2.Element;
import org.junit.jupiter.api.*;

/** Tests for MessagesConfigurationXml. */
public class MessagesConfigurationXmlTest {

    private DCCppInterfaceScaffold tc;
    private DCCppSystemConnectionMemo memo;

    private MessagesPanel newPanel() {
        MessagesPanel p = new MessagesPanel(null, false);
        p.setMemo(memo);
        return p;
    }

    private Element wrap(Element config) {
        return new Element("parent").addContent(config);
    }

    @Test
    public void testRoundTripAllFields() throws JmriConfigureXmlException {
        MessagesPanel src = newPanel();
        src.setExpirySeconds(45);
        src.setMaxLines(4);
        src.setAutoOpen(false);
        src.setPlaySound(true);
        src.setSoundFile("program:resources/sounds/Bell.wav");
        src.setMessageForeground(Color.RED);
        src.setMessageBackground(new Color(255, 255, 0));
        src.setMessagesSize(new Dimension(30, 3));

        Element stored = MessagesConfigurationXml.store(src);
        Assertions.assertEquals("dccex_messages_config", stored.getName());
        Assertions.assertNull(stored.getChild("raiseOnMessage"));

        MessagesPanel dst = new MessagesPanel(null, false);
        MessagesConfigurationXml.load(dst, wrap(stored), false);

        Assertions.assertSame(memo, dst.getMemo());
        Assertions.assertEquals(45, dst.getExpirySeconds());
        Assertions.assertEquals(4, dst.getMaxLines());
        Assertions.assertFalse(dst.isAutoOpen());
        Assertions.assertTrue(dst.isPlaySound());
        Assertions.assertEquals("program:resources/sounds/Bell.wav", dst.getSoundFile());
        Assertions.assertEquals(Color.RED, dst.getMessageForeground());
        Assertions.assertEquals(new Color(255, 255, 0), dst.getMessageBackground());
        Assertions.assertEquals(new Dimension(30, 3), dst.getMessagesSize());
        src.dispose();
        dst.dispose();
    }

    @Test
    public void testMissingChildrenKeepDefaults() throws JmriConfigureXmlException {
        Element config = new Element("dccex_messages_config")
                .addContent(new Element("systemConnection").addContent("D"));
        MessagesPanel dst = new MessagesPanel(null, false);
        MessagesConfigurationXml.load(dst, wrap(config), false);
        Assertions.assertSame(memo, dst.getMemo());
        Assertions.assertEquals(30, dst.getExpirySeconds());
        Assertions.assertEquals(10, dst.getMaxLines());
        Assertions.assertTrue(dst.isAutoOpen());
        Assertions.assertFalse(dst.isPlaySound());
        Assertions.assertEquals(MessagesPanel.DEFAULT_SOUND_FILE, dst.getSoundFile());
        Assertions.assertEquals(Color.WHITE, dst.getMessageForeground());
        Assertions.assertEquals(Color.BLACK, dst.getMessageBackground());
        Assertions.assertNull(dst.getMessagesSize());
        dst.dispose();
    }

    @Test
    public void testIgnoreMemoLeavesMemoAlone() throws JmriConfigureXmlException {
        MessagesPanel src = newPanel();
        src.setExpirySeconds(12);
        Element stored = MessagesConfigurationXml.store(src);
        MessagesPanel dst = newPanel();
        MessagesConfigurationXml.load(dst, wrap(stored), true);
        Assertions.assertSame(memo, dst.getMemo());
        Assertions.assertEquals(12, dst.getExpirySeconds());
        src.dispose();
        dst.dispose();
    }

    @Test
    public void testUnknownConnectionThrows() {
        Element config = new Element("dccex_messages_config")
                .addContent(new Element("systemConnection").addContent("Q"));
        MessagesPanel dst = new MessagesPanel(null, false);
        Assertions.assertThrows(JmriConfigureXmlException.class,
                () -> MessagesConfigurationXml.load(dst, wrap(config), false));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tc = new DCCppInterfaceScaffold(new DCCppCommandStation());
        memo = new DCCppSystemConnectionMemo(tc);
    }

    @AfterEach
    public void tearDown() {
        memo.getDCCppTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        tc = null;
        JUnitUtil.tearDown();
    }

}
