package jmri.jmrix.bidib.bidibovertcp;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jmri.util.JUnitUtil;
import org.junit.Assert;

/**
 * Tests for the BiDiBOverTcpAdapter class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class BiDiBOverTcpAdapterTest {

    @Test
    public void testCTor() {
        BiDiBOverTcpAdapter t = new BiDiBOverTcpAdapter();
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
