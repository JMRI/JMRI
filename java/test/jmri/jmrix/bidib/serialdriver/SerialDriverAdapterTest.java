package jmri.jmrix.bidib.serialdriver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.util.JUnitUtil;
import org.junit.Assert;

/**
 * Tests for the BiDiBSimulatorAdapter class
 * 
 * @author  Eckart Meyer  Copyright (C) 2020
 */
public class SerialDriverAdapterTest {

    @Test
    public void testCTor() {
        SerialDriverAdapter t = new SerialDriverAdapter();
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
