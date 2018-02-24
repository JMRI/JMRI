package jmri.jmrix.maple;

import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JUnit tests for the jmri.jmrix.maple.SerialTurnoutManager class
 *
 * @author	Bob Jacobsen
 */
public class SerialTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private MapleSystemConnectionMemo memo = null;

    @Override
    public String getSystemName(int n) {
        return "KT" + n;
    }

    @Test
    public void testConstructor() {
        // create and register the manager object
        SerialTurnoutManager atm = new SerialTurnoutManager(new MapleSystemConnectionMemo());
        Assert.assertNotNull("Maple Turnout Manager creation with memo", atm);
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        Turnout o = l.newTurnout("KT21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received turnout value " + o);
        }
        Assert.assertTrue(null != (SerialTurnout) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("KT21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("KT21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    @Override
    @Before
    public void setUp(){
        apps.tests.Log4JFixture.setUp();
        // replace the SerialTrafficController
        SerialTrafficController t = new SerialTrafficController() {
            SerialTrafficController test() {
                setInstance();
                return this;
            }
        }.test();
        t.registerNode(new SerialNode(t));
        memo = new MapleSystemConnectionMemo("K", "Maple");
        // create and register the turnout manager object
        l = new SerialTurnoutManager(memo) {
            @Override
            public void notifyTurnoutCreationError(String conflict, int bitNum) {
            }
        };
        jmri.InstanceManager.setTurnoutManager(l);
    }

    @After
    public void tearDown() {
        memo.dispose();
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutManagerTest.class);

}
