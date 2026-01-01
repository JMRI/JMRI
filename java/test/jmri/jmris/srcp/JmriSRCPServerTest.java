package jmri.jmris.srcp;

import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

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
        JUnitAppender.suppressErrorMessage("Failed to connect to port 2048");
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
