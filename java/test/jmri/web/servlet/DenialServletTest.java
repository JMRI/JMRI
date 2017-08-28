package jmri.web.servlet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.servlet.DenialServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class DenialServletTest {

    @Test
    public void testCtor() {
        DenialServlet a = new DenialServlet();
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
