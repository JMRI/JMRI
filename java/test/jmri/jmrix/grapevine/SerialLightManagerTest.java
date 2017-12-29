package jmri.jmrix.grapevine;

import jmri.Light;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * SerialTurnoutManagerTest.java
 *
 * Description:	tests for the SerialLightManager class
 *
 * @author	Bob Jacobsen Copyright 2004, 2007, 2008
 */
public class SerialLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    private GrapevineSystemConnectionMemo memo = null; 

    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();

        // replace the SerialTrafficController
        SerialTrafficController t = new SerialTrafficControlScaffold();
        memo = new GrapevineSystemConnectionMemo();
        memo.setTrafficController(t);
        t.registerNode(new SerialNode(1, SerialNode.NODE2002V6));
        // create and register the manager object
        l = new SerialLightManager(memo);
        jmri.InstanceManager.setLightManager(l);
    }

    @Override
    public String getSystemName(int n) {
        return "GL" + n;
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Light, and check type
        Light o = l.newLight("GL1105", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received light value " + o);
        }
        Assert.assertTrue(null != (SerialLight) o);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("GL1105"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("GL1105"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    /**
     * Number of light to test. Use 9th output on node 1.
     */
    @Override
    protected int getNumToTest1() {
        return 1109;
    }

    @Override
    protected int getNumToTest2() {
        return 1107;
    }


    // The minimal setup for log4J
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLightManagerTest.class);

}
