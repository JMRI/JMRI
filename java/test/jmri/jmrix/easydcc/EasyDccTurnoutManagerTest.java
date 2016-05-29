/**
 * EasyDccTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.easydcc.EasyDccTurnoutManager class
 *
 * @author	Bob Jacobsen
 * @version
 */
package jmri.jmrix.easydcc;

import jmri.Turnout;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasyDccTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTest {

    public void setUp() {
        // create and register the manager object
        l = new EasyDccTurnoutManager();
        jmri.InstanceManager.setTurnoutManager(l);
    }

    public String getSystemName(int n) {
        return "ET" + n;
    }

    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("ET21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        assertTrue(null != (EasyDccTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("ET21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        assertTrue(null != l.getBySystemName("ET21"));
        assertTrue(null != l.getByUserName("my name"));

    }

    // from here down is testing infrastructure
    public EasyDccTurnoutManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EasyDccTurnoutManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(EasyDccTurnoutManagerTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccTurnoutManagerTest.class.getName());

}
