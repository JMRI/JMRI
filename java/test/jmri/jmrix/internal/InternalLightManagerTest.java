package jmri.jmrix.internal;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;
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
