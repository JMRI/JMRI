package jmri.jmrix.tmcc.serialmon;

import jmri.jmrix.tmcc.SerialMessage;
import jmri.jmrix.tmcc.SerialReply;
import jmri.jmrix.tmcc.SerialTrafficControlScaffold;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.tmcc.serialmon.SerialMonFrame class
 *
 * @author Bob Jacobsen
 */
public class SerialMonFrameTest {

    @Test
    @DisabledIfHeadless
    public void testCreateAndShow() {

        TmccSystemConnectionMemo memo = new TmccSystemConnectionMemo("T", "TMCC via Serial");
        memo.setTrafficController(new SerialTrafficControlScaffold(memo));
        SerialMonFrame f = new SerialMonFrame(memo);
        // MonFrame needs a TrafficController for dispose()

        f.initComponents();
        
        f.pack();
        f.setVisible(true);
        Assertions.assertTrue(f.isVisible());

        SerialReply m = new SerialReply();
        m.setOpCode(0xFE);
        m.setElement(1, 0x21);
        m.setElement(2, 0x43);
        f.reply(m);

        m = new SerialReply();
        m.setElement(0, 0x21);
        f.reply(m);

        SerialMessage mm = new SerialMessage();
        mm.setOpCode(0xFE);
        mm.setElement(1, 0x21);
        mm.setElement(2, 0x43);
        f.message(mm);

        JUnitUtil.dispose(f);
        // cleanup
        memo.getTrafficController().terminateThreads();
        memo.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialMonFrameTest.class);

}
