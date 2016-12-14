package jmri.jmrix.grapevine;

import jmri.Light;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


/**
 * SerialTurnoutManagerTest.java
 *
 * Description:	tests for the SerialLightManager class
 *
 * @author	Bob Jacobsen Copyright 2004, 2007, 2008
 */
public class SerialLightManagerTest extends jmri.managers.AbstractLightMgrTest {

    @Before
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
    protected int getNumToTest1() {
        return 1109;
    }

    protected int getNumToTest2() {
        return 1107;
    }


    // The minimal setup for log4J
    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLightManagerTest.class.getName());

}
