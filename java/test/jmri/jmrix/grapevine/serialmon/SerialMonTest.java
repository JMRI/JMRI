package jmri.jmrix.grapevine.serialmon;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.grapevine.SerialMessage;
import jmri.jmrix.grapevine.SerialReply;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.grapevine.serialmon package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008
 */
public class SerialMonTest {

    @Test
    public void testDisplay() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // create a SerialMonFrame
        SerialMonFrame f = new SerialMonFrame() {
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
        f.dispose();
    }

    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
