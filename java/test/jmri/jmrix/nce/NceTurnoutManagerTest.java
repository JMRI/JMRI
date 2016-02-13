/**
 * NceTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.nce.NceTurnoutManager class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
package jmri.jmrix.nce;

import jmri.Turnout;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NceTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTest {

    private NceInterfaceScaffold nis = null;

    public void setUp() {
        // prepare an interface, register
        nis = new NceInterfaceScaffold();
        // create and register the manager object
        l = new NceTurnoutManager(nis, "N");
        jmri.InstanceManager.setTurnoutManager(l);
    }

    public String getSystemName(int n) {
        return "NT" + n;
    }

    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("NT21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        assertTrue(null != (NceTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("NT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        assertTrue(null != l.getBySystemName("NT21"));
        assertTrue(null != l.getByUserName("my name"));

    }

    // from here down is testing infrastructure
    public NceTurnoutManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceTurnoutManager.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(NceTurnoutManagerTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(NceTurnoutManagerTest.class.getName());

}
