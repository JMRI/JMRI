package jmri.jmrix.oaktree;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the SerialTurnoutManager class
 *
 * @author	Bob Jacobsen
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private OakTreeSystemConnectionMemo memo = null;

    @Override
    public String getSystemName(int n) {
        return "OT" + n;
    }

    @Test
    public void testCtor() {
        // create and register the manager object
        SerialTurnoutManager atm = new SerialTurnoutManager(memo);
        Assert.assertNotNull("Oak Tree Turnout Manager creation", atm);
    }

    @Test
    public void testConstructor() {
        // create and register the manager object
        SerialTurnoutManager atm = new SerialTurnoutManager(new OakTreeSystemConnectionMemo());
        Assert.assertNotNull("Oak Tree Turnout Manager creation with memo", atm);
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("OT21", "my name");

        log.debug("received turnout value {}", o);
        Assert.assertTrue(null != (SerialTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: {}", l.getBySystemName("OT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   {}", l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("OT21"));
        Assert.assertTrue(null != l.getByUserName("my name"));
    }

    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        SerialTrafficController t = new SerialTrafficControlScaffold();
        memo = new OakTreeSystemConnectionMemo("O", "Oak Tree");
        memo.setTrafficController(t);
        t.registerNode(new SerialNode(0, SerialNode.IO48, memo));
        // create and register the manager object
        l = new SerialTurnoutManager(memo);
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @After
    public void tearDown() {
        l.dispose();
        memo.dispose();
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManagerTest.class);

}
