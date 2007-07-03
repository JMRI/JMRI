// JmritTest.java

package jmri.jmrit;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.jmrit tree
 *
 * @author	    Bob Jacobsen  Copyright 2001, 2003
 * @version         $Revision: 1.15 $
 */
public class JmritTest extends TestCase {

    // from here down is testing infrastructure
    public JmritTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", JmritTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.JmritTest");   // no tests in this class itself
        suite.addTest(jmri.jmrit.ussctc.UssCtcTest.suite());
        suite.addTest(jmri.jmrit.blockboss.BlockBossTest.suite());
        suite.addTest(jmri.jmrit.beantable.BeanTableTest.suite());
        suite.addTest(jmri.jmrit.decoderdefn.DecoderDefnTest.suite());
        suite.addTest(jmri.jmrit.display.DisplayTest.suite());
        suite.addTest(jmri.jmrit.powerpanel.PowerPanelTest.suite());
        suite.addTest(jmri.jmrit.roster.RosterTest.suite());
        suite.addTest(jmri.jmrit.sendpacket.SendPacketTest.suite());
        suite.addTest(jmri.jmrit.sensorgroup.SensorGroupTest.suite());
        suite.addTest(jmri.jmrit.simpleclock.SimpleClockTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.SymbolicProgTest.suite());
        suite.addTest(jmri.jmrit.tracker.TrackerTest.suite());
        suite.addTest(jmri.jmrit.AbstractIdentifyTest.suite());
        suite.addTest(jmri.jmrit.DccLocoAddressSelectorTest.suite());
        suite.addTest(jmri.jmrit.XmlFileTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
