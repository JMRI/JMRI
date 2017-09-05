package jmri.jmrix.nce;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.nce.NceTurnoutManager class
 *
 * @author	Bob Jacobsen
 */
public class NceTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private NceInterfaceScaffold nis = null;

    @After 
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();

        // prepare an interface, register
        nis = new NceInterfaceScaffold();
        // create and register the manager object
        l = new NceTurnoutManager(nis, "N");
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @Override
    public String getSystemName(int n) {
        return "NT" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("NT21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        Assert.assertTrue(null != (NceTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("NT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("NT21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }


    private final static Logger log = LoggerFactory.getLogger(NceTurnoutManagerTest.class);

}
