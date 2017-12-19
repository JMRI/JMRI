package jmri.jmrix.lenz;

import jmri.Light;
import jmri.LightManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.acela.AcelaTurnoutManager class.
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class XNetLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    XNetInterfaceScaffold xnis = null;

    @Override
    public String getSystemName(int i) {
        return "XL" + i;
    }

    @Test
    public void testctor(){
        // create and register the manager object
        XNetLightManager xlm = new XNetLightManager(xnis, "X");
        Assert.assertNotNull(xlm);
    }

    @Test
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

    @Test
    public void testGetSystemPrefix(){
        // create and register the manager object
        XNetLightManager xlm = new XNetLightManager(xnis, "X");
        Assert.assertEquals("prefix","X",xlm.getSystemPrefix());
    }

    @Test
    public void testAllowMultipleAdditions(){
        // create and register the manager object
        XNetLightManager xlm = new XNetLightManager(xnis, "X");
        Assert.assertTrue(xlm.allowMultipleAdditions("foo"));
    }

    @Test
    public void testValidSystemNameConfig(){
        // create and register the manager object
        XNetLightManager xlm = new XNetLightManager(xnis, "X");
        Assert.assertTrue(xlm.validSystemNameConfig("foo"));
    }



    // from here down is testing infrastructure
    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface, register
        xnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object
        l = new XNetLightManager(xnis, "X"); // l is defined in AbstractLightMgrTestBase.
        jmri.InstanceManager.setLightManager(l);
        
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(XNetLightManagerTest.class);

}
