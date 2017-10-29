package jmri.web.servlet.home;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.web.servlet.home.HomeServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class HomeServletTest {

    @Test
    public void testCtor() {
        HomeServlet a = new HomeServlet();
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
