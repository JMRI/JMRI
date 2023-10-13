package jmri.jmrix.bidib.tcpserver;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import org.junit.Assert;

/**
 * Tests for the TcpServerStartupActionFactory class
 * 
 * @author  Eckart Meyer  Copyright (C) 2023
 */
public class TcpServerStartupActionFactoryTest {
    
    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCTor() {
        TcpServerStartupActionFactory t = new TcpServerStartupActionFactory();
        Assert.assertNotNull("exists",t);
    }
}
