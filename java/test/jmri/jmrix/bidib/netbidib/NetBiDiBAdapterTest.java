package jmri.jmrix.bidib.netbidib;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the NetBiDiBAdapter class
 * 
 * @author  Eckart Meyer  Copyright (C) 2024
 */
public class NetBiDiBAdapterTest {

    @Test
    public void testCTor() {
        NetBiDiBAdapter t = new NetBiDiBAdapter();
        Assertions.assertNotNull(t, "exists");
    }

    // The minimal setup for log4J
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
