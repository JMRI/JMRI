package jmri.managers;

import jmri.Light;
import jmri.LightManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.managers.InternalLightManager class.
 *
 * @author	Bob Jacobsen Copyright 2009
 */
public class InternalLightManagerTest extends jmri.managers.AbstractLightMgrTest {

    public String getSystemName(int i) {
        return "IL" + i;
    }

    public void testAsAbstractFactory() {
        // create and register the manager object
        InternalLightManager alm = new InternalLightManager();
        jmri.InstanceManager.setLightManager(alm);

        // ask for a Light, and check type
        LightManager lm = jmri.InstanceManager.lightManagerInstance();

        Light tl = lm.newLight("IL21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received light value " + tl);
        }
        Assert.assertTrue(null != tl);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + lm.getBySystemName("IL21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + lm.getByUserName("my name"));
        }

        Assert.assertTrue(null != lm.getBySystemName("IL21"));
        Assert.assertTrue(null != lm.getByUserName("my name"));

    }

    public void testIsVariableLight() {
        // create and register the manager object
        InternalLightManager alm = new InternalLightManager();
        jmri.InstanceManager.setLightManager(alm);

        // ask for a Light, and check type
        LightManager lm = jmri.InstanceManager.lightManagerInstance();

        Assert.assertTrue(lm.newLight("IL21", "my name").isIntensityVariable());

    }

    // from here down is testing infrastructure
    public InternalLightManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", InternalLightManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(InternalLightManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // create and register the manager object
        l = new InternalLightManager();
        jmri.InstanceManager.setLightManager(l);
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(InternalLightManagerTest.class.getName());

}
