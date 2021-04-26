package jmri.web.servlet.simple;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.web.servlet.simple.SimpleServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class SimpleServletTest {

    @Test
    public void testCtor() {
        SimpleServlet a = new SimpleServlet();
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
