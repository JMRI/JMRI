package jmri.jmrix.bidib.tcpserver;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the TcpServerStartupActionFactory class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class TcpServerStartupActionFactoryTest {

    @Test
    public void testCTor() {
        TcpServerStartupActionFactory t = new TcpServerStartupActionFactory();
        Assertions.assertNotNull(t, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
