package jmri.jmrix.internal;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests for the jmri.jmrix.internal.InternalLightManager class.
 *
 * @author	Bob Jacobsen Copyright 2009
 */
public class InternalLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "IL" + i;
    }

    @Test
    public void testAsAbstractFactory() {
        // create and register the manager object
        InternalLightManager alm = new InternalLightManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
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

    @Test
    public void testSystemNames() {
        Light l1 = l.provide("IL1");
        Light l2 = l.provide("IL01");
        Assert.assertEquals("Light system name is correct", "IL1", l1.getSystemName());
        Assert.assertEquals("Light system name is correct", "IL01", l2.getSystemName());
    }

    @Test
    public void testCompareTo() {
        Light l1 = l.provide("IL1");
        Light l2 = l.provide("IL01");
        Assert.assertNotEquals("Lights are different", l1, l2);
        Assert.assertNotEquals("Light compareTo returns not zero", 0, l1.compareTo(l2));
    }

    @Test
    public void testCompareSystemNameSuffix() {
        Light l1 = l.provide("IL1");
        Light l2 = l.provide("IL01");
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                -1, l1.compareSystemNameSuffix("01", "1", l2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                0, l1.compareSystemNameSuffix("1", "1", l2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                0, l1.compareSystemNameSuffix("01", "01", l2));
        Assert.assertEquals("compareSystemNameSuffix returns correct value",
                +1, l1.compareSystemNameSuffix("1", "01", l2));
    }

    @Test
    public void testIsVariableLight() {
        // ask for a Light, and check type
        LightManager lm = jmri.InstanceManager.lightManagerInstance();

        Assert.assertTrue(lm.newLight("IL21", "my name").isIntensityVariable());
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // create and register the manager object
        l = new InternalLightManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        jmri.InstanceManager.setLightManager(l);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(InternalLightManagerTest.class);
}
