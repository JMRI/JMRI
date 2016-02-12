// LnCommandStationTypeTest.java
package jmri.jmrix.loconet;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.loconet.LnSensor class.
 *
 * @author	Bob Jacobsen Copyright 2001, 2002
 * @version $Revision$
 */
public class LnCommandStationTypeTest extends TestCase {

    public void testLnCommandStationTypeName() {
        Assert.assertEquals("DCS200", LnCommandStationType.COMMAND_STATION_DCS200.getName());
    }

    public void testFind() {
        Assert.assertEquals(LnCommandStationType.COMMAND_STATION_DCS200, LnCommandStationType.getByName("DCS200"));
    }

    public void testThrottleManager() {
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(null, new SlotManager(new LocoNetInterfaceScaffold()));

        jmri.ThrottleManager tm = LnCommandStationType.COMMAND_STATION_DCS200.getThrottleManager(memo);
        Assert.assertEquals(LnThrottleManager.class, tm.getClass());
    }

    // from here down is testing infrastructure
    public LnCommandStationTypeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LnCommandStationTypeTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LnCommandStationTypeTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(LnCommandStationTypeTest.class.getName());

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
