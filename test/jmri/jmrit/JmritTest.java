// JmritTest.java

package jmri.jmrit;

import junit.framework.*;

/**
 * Invokes complete set of tests in the jmri.jmrit tree
 *
 * @author	    Bob Jacobsen  Copyright 2001, 2003
 * @version         $Revision: 1.7 $
 */
public class JmritTest extends TestCase {

    // from here down is testing infrastructure
    public JmritTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {JmritTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.JmritTest");   // no tests in this class itself
        suite.addTest(jmri.jmrit.beantable.BeanTableTest.suite());
        suite.addTest(jmri.jmrit.simpleclock.SimpleClockTest.suite());
        suite.addTest(jmri.jmrit.display.DisplayTest.suite());
        suite.addTest(jmri.jmrit.AbstractIdentifyTest.suite());
        suite.addTest(jmri.jmrit.decoderdefn.DecoderDefnTest.suite());
        suite.addTest(jmri.jmrit.XmlFileTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.SymbolicProgTest.suite());
        suite.addTest(jmri.jmrit.powerpanel.PowerPanelTest.suite());
        suite.addTest(jmri.jmrit.roster.RosterTest.suite());
        suite.addTest(jmri.jmrit.sendpacket.SendPacketTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
