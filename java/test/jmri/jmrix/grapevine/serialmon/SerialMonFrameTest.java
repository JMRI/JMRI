package jmri.jmrix.grapevine.serialmon;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.SerialTrafficController;
import jmri.jmrix.grapevine.SerialTrafficControlScaffold;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.SerialReply;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.grapevine.serialmon package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008
 */
@DisabledIfHeadless
public class SerialMonFrameTest {

    private GrapevineSystemConnectionMemo memo = null; 

    @Test
    public void testCTor() {
        SerialMonFrame t = new SerialMonFrame(memo);
        assertNotNull( t, "exists");
    }

    @Test
    public void testDisplay() {

        // create a SerialMonFrame
        SerialMonFrame f = new SerialMonFrame(memo) {
            {
                rawCheckBox.setSelected(true);
            }
        };
        f.initComponents();
        f.setVisible(true);

        // show stuff
        SerialMessage m = new SerialMessage();
        m.setOpCode(0x81);
        m.setElement(1, (byte) 0x02);
        m.setElement(2, (byte) 0xA2);
        m.setElement(3, (byte) 0x31);

        f.message(m);

        // show stuff
        SerialReply r = new SerialReply();
        r.setOpCode(0x81);
        r.setElement(1, (byte) 0x02);
        r.setElement(2, (byte) 0xA2);
        r.setElement(3, (byte) 0x31);

        f.reply(r);

        //close frame
        JUnitUtil.dispose(f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        JUnitUtil.initDefaultUserMessagePreferences();
        memo = new GrapevineSystemConnectionMemo();
        SerialTrafficController tc = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tc);
    }

    @AfterEach
    public void tearDown() {
        memo.getTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();

    }

}
