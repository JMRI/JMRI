package jmri.implementation;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import jmri.DccLocoAddress;


/**
 * Test simple functioning of NmraConsist
 *
 * @author	Paul Copyright (C) 2016
 */
public class NmraConsistTest extends AbstractConsistTestBase {

    @Test public void testCtor2() {
        // integer constructor test.
        NmraConsist c = new NmraConsist(12);
        Assert.assertNotNull(c);
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDebugCommandStation();
        c = new NmraConsist(new DccLocoAddress(12, true));
    }
   
    @After
    @Override
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

}
