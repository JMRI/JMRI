package jmri.jmrix.grapevine;

import jmri.Light;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SerialTurnoutManagerTest.java
 *
 * Description:	tests for the SerialLightManager class
 *
 * @author	Bob Jacobsen Copyright 2004, 2007, 2008
 */
public class SerialLightManagerTest extends jmri.managers.AbstractLightMgrTest {

    public void setUp() {
        apps.tests.Log4JFixture.setUp();

        // replace the SerialTrafficController
        SerialTrafficController t = new SerialTrafficController() {
            SerialTrafficController test() {
                setInstance();
                return this;
            }

            synchronized protected void sendMessage(AbstractMRMessage m, AbstractMRListener reply) {
            }
        }.test();
        t.registerNode(new SerialNode(1, SerialNode.NODE2002V6));
        // create and register the manager object
        l = new SerialLightManager();
        jmri.InstanceManager.setLightManager(l);
    }

    public String getSystemName(int n) {
        return "GL" + n;
    }

    public void testAsAbstractFactory() {
        // ask for a Light, and check type
        Light o = l.newLight("GL1105", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received light value " + o);
        }
        assertTrue(null != (SerialLight) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("GL1105"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        assertTrue(null != l.getBySystemName("GL1105"));
        assertTrue(null != l.getByUserName("my name"));

    }

    /**
     * Number of light to test. Use 9th output on node 1.
     */
    protected int getNumToTest1() {
        return 1109;
    }

    protected int getNumToTest2() {
        return 1107;
    }

    // from here down is testing infrastructure
    public SerialLightManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialLightManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(SerialLightManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLightManagerTest.class.getName());

}
