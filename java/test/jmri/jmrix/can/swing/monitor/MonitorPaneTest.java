package jmri.jmrix.can.swing.monitor;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.can.swing.monitor.MonitorPane class.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008, 2009
 */
public class MonitorPaneTest {

    @Test
    public void testDisplay() throws Exception {
        TrafficControllerScaffold tcs = new TrafficControllerScaffold();

        MonitorPane f = new MonitorPane();
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        f.initComponents(memo);

        //pane.MonitorPane.Default;
        /*MonitorFrame f = new MonitorFrame(){
         { rawCheckBox.setSelected(true);}
         };
         f.initComponents();
         f.setVisible(true);*/
        // show std message
        CanMessage m = new CanMessage(0x123);
        m.setNumDataElements(3);
        m.setElement(0, (byte) 0x02);
        m.setElement(1, (byte) 0xA2);
        m.setElement(2, (byte) 0x31);

        f.message(m);

        // show ext message
        m = new CanMessage(0x654321);
        m.setExtended(true);
        m.setNumDataElements(3);
        m.setElement(0, (byte) 0x02);
        m.setElement(1, (byte) 0xA2);
        m.setElement(2, (byte) 0x31);

        f.message(m);

        // show reply
        CanReply r = new CanReply();
        r.setNumDataElements(3);
        r.setElement(0, (byte) 0x11);
        r.setElement(1, (byte) 0x82);
        r.setElement(2, (byte) 0x33);

        f.reply(r);

        // close panel
        f.dispose();
        memo.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
