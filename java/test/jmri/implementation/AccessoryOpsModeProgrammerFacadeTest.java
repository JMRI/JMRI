package jmri.implementation;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.ProgListener;
import jmri.Programmer;
import jmri.progdebugger.ProgDebugger;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test the AccessoryOpsModeProgrammerFacade class.
 *
 * @author	Bob Jacobsen Copyright 2014
 *
 */
 // @ToDo("transform to annotations requires e.g. http://alchemy.grimoire.ca/m2/sites/ca.grimoire/todo-annotations/")
// @ToDo("test mode handling")
// @ToDo("test packet contents in each mode")
// @ToDo("test address handling")
public class AccessoryOpsModeProgrammerFacadeTest extends TestCase {

    public void testWriteDirect() throws jmri.ProgrammerException, InterruptedException {

        ProgDebugger dp = new ProgDebugger(true, 123);
        Programmer p = new AccessoryOpsModeProgrammerFacade(dp);
        ProgListener l = new ProgListener() {
            public void programmingOpReply(int value, int status) {
                log.debug("callback value=" + value + " status=" + status);
                replied = true;
                readValue = value;
            }
        };

        p.writeCV("4", 12, l);
        waitReply();
        Assert.assertTrue("target not directly written", !dp.hasBeenWritten(12));
        Assert.assertTrue("index not written", !dp.hasBeenWritten(81));
        Assert.assertNotNull("packet sent", lastPacket);
    }

    public void testCvLimit() {
        ProgDebugger dp = new ProgDebugger(true, 123);
        dp.setTestReadLimit(1024);
        dp.setTestWriteLimit(1024);

        Programmer p = new AccessoryOpsModeProgrammerFacade(dp);

        Assert.assertTrue("CV limit read OK", p.getCanRead("1024"));
        Assert.assertTrue("CV limit write OK", p.getCanWrite("1024"));
        Assert.assertTrue("CV limit read fail", !p.getCanRead("1025"));
        Assert.assertTrue("CV limit write fail", !p.getCanWrite("1025"));
    }

    // from here down is testing infrastructure
    class MockCommandStation implements CommandStation {

        public void sendPacket(byte[] packet, int repeats) {
            lastPacket = packet;
        }

        public String getUserName() {
            return "I";
        }

        public String getSystemPrefix() {
            return "I";
        }
    }

    byte[] lastPacket;
    int readValue = -2;
    boolean replied = false;

    synchronized void waitReply() throws InterruptedException {
        while (!replied) {
            wait(200);
        }
        replied = false;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        InstanceManager.setCommandStation(new MockCommandStation());
        lastPacket = null;
    }

    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    // from here down is testing infrastructure
    public AccessoryOpsModeProgrammerFacadeTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AccessoryOpsModeProgrammerFacadeTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(AccessoryOpsModeProgrammerFacadeTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(AccessoryOpsModeProgrammerFacadeTest.class.getName());

}
