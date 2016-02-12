package jmri.jmrix.lenz;

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
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision$
 */
public class XNetLightManagerTest extends jmri.managers.AbstractLightMgrTest {

    XNetInterfaceScaffold xnis = null;

    public String getSystemName(int i) {
        return "XL" + i;
    }

    public void testAsAbstractFactory() {
        // create and register the manager object
        XNetLightManager xlm = new XNetLightManager(xnis, "X");
        jmri.InstanceManager.setLightManager(xlm);

        // ask for a Light, and check type
        LightManager lm = jmri.InstanceManager.lightManagerInstance();

        Light tl = lm.newLight("XL21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received light value " + tl);
        }
        Assert.assertTrue(null != (XNetLight) tl);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + lm.getBySystemName("XL21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + lm.getByUserName("my name"));
        }

        Assert.assertTrue(null != lm.getBySystemName("XL21"));
        Assert.assertTrue(null != lm.getByUserName("my name"));
    }

    // from here down is testing infrastructure
    public XNetLightManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetLightManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetLightManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface, register
        xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object
        l = new XNetLightManager(xnis, "X");
        jmri.InstanceManager.setLightManager(l);

    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(XNetLightManagerTest.class.getName());

}
