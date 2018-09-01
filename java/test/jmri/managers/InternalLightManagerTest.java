package jmri.managers;

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
 * Tests for the jmri.managers.InternalLightManager class.
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

        Light tl = l.newLight("IL21", "my name");

        if (log.isDebugEnabled()) {
            log.debug("received light value " + tl);
        }
        Assert.assertTrue(null != tl);

        // make sure loaded into tables
        if (log.isDebugEnabled()) {
            log.debug("by system name: " + l.getBySystemName("IL21"));
        }
        if (log.isDebugEnabled()) {
            log.debug("by user name:   " + l.getByUserName("my name"));
        }

        Assert.assertTrue(null != l.getBySystemName("IL21"));
        Assert.assertTrue(null != l.getByUserName("my name"));

    }

    @Test
    public void testIsVariableLight() {

        Assert.assertTrue(l.newLight("IL21", "my name").isIntensityVariable());

    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.resetInstanceManager();
        l = new InternalLightManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(InternalLightManagerTest.class);

}
