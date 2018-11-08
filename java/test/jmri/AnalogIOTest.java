package jmri;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the Light class
 *
 * @author	Daniel Bergqvist Copyright (C) 2018
 */
public class AnalogIOTest {

    @Test
    public void testStateConstants() {
        Assert.assertTrue("MIN_VALUE less than MAX_VALUE", AnalogIO.MIN_VALUE < AnalogIO.MAX_VALUE);
        Assert.assertTrue("MIN_VALUE is the smallest number", (AnalogIO.MIN_VALUE-1) == AnalogIO.MAX_VALUE);
        Assert.assertTrue("MAX_VALUE is the biggest number", AnalogIO.MIN_VALUE == (AnalogIO.MAX_VALUE+1));
    }

    @Before
    public void setUp() {
          jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
          jmri.util.JUnitUtil.tearDown();
    }

}
