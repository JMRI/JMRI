// SerialTurnoutManagerTest.java
package jmri.jmrix.secsi;

import jmri.Turnout;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SerialTurnoutManagerTest.java
 *
 * Description:	tests for the SerialTurnoutManager class
 *
 * @author	Bob Jacobsen Copyright 2004, 2008
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTest {

    public void setUp() {
        apps.tests.Log4JFixture.setUp();

        // replace the SerialTrafficController
        SerialTrafficController t = new SerialTrafficController() {
            SerialTrafficController test() {
                setInstance();
                return this;
            }
        }.test();
        t.registerNode(new SerialNode(0, SerialNode.DAUGHTER));
        // create and register the manager object
        l = new SerialTurnoutManager();
        jmri.InstanceManager.setTurnoutManager(l);
    }

    public String getSystemName(int n) {
        return "VT" + n;
    }

    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("VT21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        assertTrue(null != (SerialTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("VT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        assertTrue(null != l.getBySystemName("VT21"));
        assertTrue(null != l.getByUserName("my name"));

    }

    // from here down is testing infrastructure
    public SerialTurnoutManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialTurnoutManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(SerialTurnoutManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManagerTest.class.getName());

}
