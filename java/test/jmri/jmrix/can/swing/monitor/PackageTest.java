package jmri.jmrix.can.swing.monitor;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.swing.monitor package.
 *
 * @author Bob Jacobsen Copyright 2003, 2007, 2008, 2009
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

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

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(PackageTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
