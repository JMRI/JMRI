package jmri.jmrix.acela;

import jmri.Light;
import jmri.LightManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.acela.AcelaTurnoutManager class.
 *
 * @author	Bob Coleman Copyright 2008
 */
public class AcelaLightManagerTest extends jmri.managers.AbstractLightMgrTest {

    public String getSystemName(int i) {
        return "AL" + i;
    }

    public void testAsAbstractFactory() {
        // create and register the manager object
        AcelaLightManager alm = new AcelaLightManager();
        jmri.InstanceManager.setLightManager(alm);

        // ask for a Light, and check type
        LightManager lm = jmri.InstanceManager.lightManagerInstance();

        Light tl = lm.newLight("AL21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received light value " + tl);
        }
        Assert.assertTrue(null != (AcelaLight) tl);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + lm.getBySystemName("AL21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + lm.getByUserName("my name"));
        }

        Assert.assertTrue(null != lm.getBySystemName("AL21"));
        Assert.assertTrue(null != lm.getByUserName("my name"));

    }

    // from here down is testing infrastructure
    public AcelaLightManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AcelaLightManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AcelaLightManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // create and register the manager object
        l = new AcelaLightManager();
        jmri.InstanceManager.setLightManager(l);
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaLightManagerTest.class.getName());

}
