package jmri.web.servlet.config;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void setUp() {
        JUnitUtil.setUp();

    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
