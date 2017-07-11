package jmri.implementation;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import jmri.DccLocoAddress;


/**
 * Test simple functioning of DccConsist
 *
 * @author	Paul Copyright (C) 2011, 2016
 */
public class DccConsistTest extends AbstractConsistTestBase {

    @Test public void testCtor2() {
        // integer constructor test.
        DccConsist c = new DccConsist(12);
        Assert.assertNotNull(c);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDebugProgrammerManager();
        c = new DccConsist(new DccLocoAddress(12, true));
    }
   
    @After
    @Override
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        c = null;
    }

}
