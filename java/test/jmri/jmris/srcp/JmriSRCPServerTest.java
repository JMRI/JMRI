package jmri.jmris.srcp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServer class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class JmriSRCPServerTest {

    @Test
    public void testCtor() {
        JmriSRCPServer a = new JmriSRCPServer();
        assertNotNull(a);
    }

    @Test
    public void testCtorwithParameter() {
        JmriSRCPServer a = new JmriSRCPServer(2048);
        assertNotNull(a);
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
