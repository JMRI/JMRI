package jmri.web.servlet.config;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.web.servlet.config.ConfigServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class ConfigServletTest {

    @Test
    public void testCtor() {
        ConfigServlet a = new ConfigServlet();
        Assert.assertNotNull(a);
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
