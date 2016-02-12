// CbusSensorManagerTest.java
package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TestTrafficController;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusSensorManager class.
 *
 * @author	Bob Jacobsen Copyright 2008
 * @version $Revision$
 */
public class CbusSensorManagerTest extends TestCase {

    public void testCreate() {
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(new TestTrafficController());
        CbusSensorManager m = new CbusSensorManager(memo);
        m.provideSensor(memo.getSystemPrefix() + "SX0A;+N15E6");
        memo.dispose();
    }

    // from here down is testing infrastructure
    public CbusSensorManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {CbusSensorManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(CbusSensorManagerTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(CbusSensorManagerTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
