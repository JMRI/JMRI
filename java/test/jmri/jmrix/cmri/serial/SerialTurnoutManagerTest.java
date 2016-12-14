package jmri.jmrix.cmri.serial;

import jmri.Turnout;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SerialTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.cmri.SerialTurnoutManager class
 *
 * @author	Bob Jacobsen
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTest {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold stcs = null;

    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        // replace the SerialTrafficController
        stcs = new SerialTrafficControlScaffold();
        stcs.registerNode(new SerialNode(stcs));
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(stcs);
        // create and register the turnout manager object
        l = new SerialTurnoutManager(memo) {
            public void notifyTurnoutCreationError(String conflict, int bitNum) {
            }
        };
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
        stcs = null;
        memo = null;
    }

    @Override
    public String getSystemName(int n) {
        return "CT" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("CT21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        Assert.assertTrue(null != (SerialTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("CT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("CT21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManagerTest.class.getName());

}
