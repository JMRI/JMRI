package jmri.implementation;

import jmri.DccLocoAddress;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


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
        JUnitUtil.setUp();        jmri.util.JUnitUtil.initDebugCommandStation();
        c = new NmraConsist(new DccLocoAddress(12, true));
    }
   
    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
