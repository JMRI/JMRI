package jmri.jmrix.bidib.bidibovertcp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BiDiBOverTcpAdapter class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class BiDiBOverTcpAdapterTest {

    @Test
    public void testCTor() {
        BiDiBOverTcpAdapter t = new BiDiBOverTcpAdapter();
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
