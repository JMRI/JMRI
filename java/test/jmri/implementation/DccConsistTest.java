package jmri.implementation;

import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


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
        JUnitUtil.setUp();        jmri.util.JUnitUtil.initDebugProgrammerManager();
        c = new DccConsist(new DccLocoAddress(12, true));
    }
   
    @After
    @Override
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        c = null;
    }

}
